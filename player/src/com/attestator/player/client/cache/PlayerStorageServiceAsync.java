package com.attestator.player.client.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheKind;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.client.cache.co.AddAnswerCO;
import com.attestator.player.client.cache.co.FinishReportCO;
import com.attestator.player.client.cache.co.StartReportCO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.rpc.PlayerServiceAsync;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PlayerStorageServiceAsync implements PlayerServiceAsync {
    public static final int CACHE_POLLING_INTERVAL = 1000 * 30;
    public static final int REPORT_POLLING_INTERVAL = 1000 * 5;
    public static final int OFFLINE_SLEEP_TIMEOUT = 1000 * 60 * 2;

    private PlayerStorageCache psc;
    private PlayerServiceAsync rpc;
    private UpdateCacheTimer updateCacheTimer;
    private SendReportsTimer sendReportsTimer;
    
    private long online = -1l;

    public class CacheReaderCallback<T> implements AsyncCallback<T> {
        private Class<T> clazz;
        private Function<T, T> preprocess;
        private String key;
        private AsyncCallback<T> callback;

        public CacheReaderCallback(Class<T> clazz, String key, Function<T, T> preprocess,
                AsyncCallback<T> callback) {
            super();
            this.clazz = clazz;
            this.key = key;
            this.callback = callback;
            this.preprocess = preprocess;
        }
        
        public CacheReaderCallback(Class<T> clazz, String key, AsyncCallback<T> callback) {
            this(clazz, key, null, callback);
        }

        @Override
        public final void onFailure(Throwable caught) {
            online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
            callCallbackOnCachedValue(clazz, key, preprocess, callback);
        }

        @Override
        public final void onSuccess(T result) {
            callback.onSuccess(result);
        }
    }

    public PlayerStorageServiceAsync(PlayerServiceAsync arpc) {
        this.psc = PlayerStorageCache.getPlayerStorageIfSupported();
        this.rpc = arpc;
        
        updateCacheTimer = new UpdateCacheTimer();
        updateCacheTimer.scheduleRepeating(CACHE_POLLING_INTERVAL);
        
        sendReportsTimer = new SendReportsTimer(); 
        sendReportsTimer.scheduleRepeating(REPORT_POLLING_INTERVAL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getActivePulications(String tenantId,
            AsyncCallback<List<ActivePublicationDTO>> callback)
            throws IllegalStateException {

        psc.setCurrentTenant(tenantId);

        String key = psc.key(CacheKind.cache, CacheType.getActivePublications,
                "tenantId", tenantId);
        Class<List<ActivePublicationDTO>> clazz = (Class<List<ActivePublicationDTO>>) (Class<?>) ArrayList.class;

        if (isOnline()) {
            rpc.getActivePulications(tenantId,
                    new CacheReaderCallback<List<ActivePublicationDTO>>(clazz,
                            key, callback));
        } else {
            callCallbackOnCachedValue(clazz, key, callback);
        }
    }

    private ReportVO getUnfinishedReportFromCache(Predicate<ReportVO> predicate) {
        Set<String> keys = psc.getKeys(psc.marker(CacheKind.renew));
        ReportVO result = null;

        for (String key : keys) {
            Map<String, String> keyMap = psc.keyMap(key);

            switch (CacheType.valueOf(keyMap.get("type"))) {

            case startReport: {
                StartReportCO item = psc.getItem(StartReportCO.class, key);
                if (predicate == null || predicate.apply(item.getReport())) {
                    result = item.getReport();
                }
                continue;
            }

            case addAnswer: {
                if (result == null) {
                    continue;
                }
                AddAnswerCO item = psc.getItem(AddAnswerCO.class, key);
                if (item.getReportId().equals(result.getId())) {
                    result.getAnswers().add(item.getAnswer());
                }
                continue;
            }

            case finishReport: {
                if (result == null) {
                    continue;
                }

                FinishReportCO item = psc.getItem(FinishReportCO.class, key);
                if (item.getReportId().equals(result.getId())) {
                    result.setFinished(true);
                    result.setEnd(item.getEnd());
                    result.setInterruptionCause(item.getInterruptionCause());
                }

                continue;
            }

            default:
                continue;
            }
        }
        return result;
    }

    @Override
    public void renewTest(String tenantId, final String publicationId,
            final AsyncCallback<ReportVO> callback)
            throws IllegalStateException {

        psc.setCurrentTenant(tenantId);

        ReportVO report = getUnfinishedReportFromCache(new Predicate<ReportVO>() {
            @Override
            public boolean apply(ReportVO obj) {
                return publicationId.equals(obj.getPublication().getId());
            }
        });

        if (report != null) {
            if (ReportHelper.isRenewAllowed(report)) {
                callback.onSuccess(report);
            } else {
                callback.onSuccess(null);
            }
            return;
        }

        if (isOnline()) {
            rpc.renewTest(tenantId, publicationId,
                    new AsyncCallback<ReportVO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onSuccess(null);
                        }

                        @Override
                        public void onSuccess(ReportVO result) {
                            callback.onSuccess(result);
                        }
                    });
        } else {
            callback.onSuccess(null);
        }
    }

    @Override
    public void getReport(String tenantId, final String reportId,
            AsyncCallback<ReportVO> callback) throws IllegalStateException {

        psc.setCurrentTenant(tenantId);

        ReportVO report = getUnfinishedReportFromCache(new Predicate<ReportVO>() {
            @Override
            public boolean apply(ReportVO report) {
                return report.getId().equals(reportId);
            }
        });

        if (report != null) {
            callback.onSuccess(report);
            return;
        }

        String key = psc.key(CacheKind.cache, CacheType.getReport, "tenantId",
                tenantId, "reportId", reportId);

        if (isOnline()) {
            rpc.getReport(tenantId, reportId,
                    new CacheReaderCallback<ReportVO>(ReportVO.class, key,
                            callback));
        } else {
            callCallbackOnCachedValue(ReportVO.class, key, callback);
        }
    }
    
    private ActivePublicationDTO getActivePublication(List<ActivePublicationDTO> activePublications, String publicationId) {
        for (ActivePublicationDTO ap : activePublications) {
            if (publicationId.equals(ap.getPublication().getId())) {
                return ap;
            }
        }
        return null;
    }
    
    
    @Override
    public void startTest(final String tenantId, final String publicationId,
            final AsyncCallback<ReportVO> callback)
            throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        
        String key = psc.key(CacheKind.cache, CacheType.startTest, "tenantId",
                tenantId, "publicationId", publicationId);

        Function<ReportVO, ReportVO> setNewId = new Function<ReportVO, ReportVO>() {            
            @Override
            public ReportVO apply(ReportVO report) {
                String apKey = psc.key(CacheKind.cache, CacheType.getActivePublications, "tenantId", tenantId);
                @SuppressWarnings("unchecked")
                List<ActivePublicationDTO> activePublications = psc.getItem((Class<List<ActivePublicationDTO>>) (Class<?>) ArrayList.class, apKey);
                ActivePublicationDTO ap = getActivePublication(activePublications, publicationId);
                if (ap != null) {
                    if (ap.getAttemptsLeft() <= 0) {
                        return null;
                    }
                }
                
                report.setId(BaseVO.idString());
                
                return report;
            }
        };
        
        if (isOnline()) {
            rpc.startTest(tenantId, publicationId,
                    new CacheReaderCallback<ReportVO>(ReportVO.class, key,
                            setNewId, callback));
        } else {
            callCallbackOnCachedValue(ReportVO.class, key, setNewId, callback);
        }
    }

    @Override
    public void startReport(String tenantId, ReportVO report, Date start,
            AsyncCallback<Void> callback) throws IllegalStateException {

        psc.setCurrentTenant(tenantId);

        report.setStart(start);
        StartReportCO item = new StartReportCO(tenantId, report, start);

        String reportKey = psc.keyWithTime(CacheKind.report,
                CacheType.startReport, "reportId", report.getId());
        psc.setItem(reportKey, item);

        psc.removeThisItemsByRegex(psc.marker(CacheKind.renew), ".*");

        String renewKey = psc.keyWithTime(CacheKind.renew,
                CacheType.startReport, "reportId", report.getId());
        psc.setItem(renewKey, item);

        sendReportsTimer.run();
        
        callback.onSuccess(null);
    }

    @Override
    public void addAnswer(String tenantId, String reportId, AnswerVO answer,
            AsyncCallback<Void> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        AddAnswerCO item = new AddAnswerCO(tenantId, reportId, answer);

        String reportKey = psc.keyWithTime(CacheKind.report,
                CacheType.addAnswer, "reportId", reportId);
        psc.setItem(reportKey, item);

        String renewKey = psc.keyWithTime(CacheKind.renew, CacheType.addAnswer,
                "reportId", reportId);
        psc.setItem(renewKey, item);

        sendReportsTimer.run();
        
        callback.onSuccess(null);
    }
    
    @Override
    public void finishReport(String tenantId, String reportId, Date end,
            InterruptionCauseEnum interruptionCause,
            AsyncCallback<Void> callback) throws IllegalStateException {

        psc.setCurrentTenant(tenantId);
        
        // Update number of attempts left in cache
        PublicationVO reportPublication = null;
        String startReportKey = psc.getKey(psc.marker(CacheKind.renew, CacheType.startReport, "reportId", reportId));
        if (startReportKey != null) {
            StartReportCO startReportCO = psc.getItem(StartReportCO.class, startReportKey);
            reportPublication = startReportCO.getReport().getPublication();
        }
        
        if (reportPublication != null && !reportPublication.isThisUnlimitedAttempts()) {
            String apKey = psc.key(CacheKind.cache, CacheType.getActivePublications, "tenantId", tenantId);
            @SuppressWarnings("unchecked")
            List<ActivePublicationDTO> activePublications = psc.getItem((Class<List<ActivePublicationDTO>>) (Class<?>) ArrayList.class, apKey);
            ActivePublicationDTO activePublication = getActivePublication(activePublications, reportPublication.getId());
            if (activePublication != null) {
                long attemptsLeft = Math.max(activePublication.getAttemptsLeft() - 1, 0);
                activePublication.setAttemptsLeft(attemptsLeft);
                psc.setItem(apKey, activePublications);
            }
        }
        
        // Put finish report record
        FinishReportCO item = new FinishReportCO(tenantId, reportId, end,
                interruptionCause);

        String reportKey = psc.keyWithTime(CacheKind.report,
                CacheType.finishReport, "reportId", reportId);
        psc.setItem(reportKey, item);

        String renewKey = psc.keyWithTime(CacheKind.renew,
                CacheType.finishReport, "reportId", reportId);
        psc.setItem(renewKey, item);
        
        sendReportsTimer.run();

        callback.onSuccess(null);
    }

    @Override
    public void getChangesSince(String tenantId, Date time,
            AsyncCallback<List<ChangeMarkerVO>> callback)
            throws IllegalStateException {
        throw new UnsupportedOperationException(
                "getChangesSince not supported in PlayerStorageServiceAsync");
    }

    private boolean isOnline() {
        return online <= 0;
    }

    private <T> void callCallbackOnCachedValue(Class<T> clazz, String key,
            AsyncCallback<T> callback) {
        callCallbackOnCachedValue(clazz, key, null, callback);
    }

    private <T> void callCallbackOnCachedValue(Class<T> clazz, String key, 
            Function<T, T> preprocess, AsyncCallback<T> callback) {
        T cachedResult = psc.getItem(clazz, key);
        if (cachedResult != null) {
            if (preprocess != null) {
                callback.onSuccess(preprocess.apply(cachedResult));
            }
            callback.onSuccess(cachedResult);
        } else {
            callback.onFailure(new IllegalStateException(
                    "Невозможно получить данные"));
        }
    }

    public class SendReportsTimer extends Timer {
        public class SendingCallback implements AsyncCallback<Void> {
            private Set<SendingCallback> callbacks;
            private String key;

            public SendingCallback(Set<SendingCallback> callbacks, String key) {
                super();
                this.key = key;
                this.callbacks = callbacks;
                this.callbacks.add(this);
            }

            private void onFinish() {
                callbacks.remove(this);
                if (callbacks.isEmpty()) {
                    buzzy = false;
                }
            }

            @Override
            public final void onFailure(Throwable caught) {
                try {
                    online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
                } finally {
                    onFinish();
                }
            }

            @Override
            public final void onSuccess(Void result) {
                try {
                    psc.removeThisItemsByRegex(psc.marker(CacheKind.report),
                            key);
                    sendNextReportItemIfAny();
                } finally {
                    onFinish();
                }
            }
        }

        private Set<SendingCallback> callbacks = new HashSet<SendingCallback>();
        private boolean buzzy = false;

        @Override
        public void run() {
            try {
                if (buzzy) {
                    return;
                }
                buzzy = true;

                // If online > 0 we in offline mode and do next check only at
                // online time
                if (online > 0) {
                    if (System.currentTimeMillis() < online) {
                        buzzy = false;
                        return;
                    } else {
                        online = -1l;
                    }
                }

                sendNextReportItemIfAny();
            } catch (Throwable e) {
            }
        }

        private String getNextReportKey() {
            Set<String> keys = psc.getKeys(psc.marker(CacheKind.report));
            if (keys.iterator().hasNext()) {
                return keys.iterator().next();
            } else {
                return null;
            }
        }

        private void sendNextReportItemIfAny() {
            String key = getNextReportKey();

            if (key == null) {
                buzzy = false;
                return;
            }

            Map<String, String> keyMap = psc.keyMap(key);
            CacheType cacheType = CacheType.valueOf(keyMap.get("type"));
            
            switch (cacheType) {

            case startReport: {
                StartReportCO p = psc.getItem(StartReportCO.class, key);
                rpc.startReport(p.getTenantId(), p.getReport(), p.getStart(),
                        new SendingCallback(callbacks, key));
            }
                break;

            case addAnswer: {
                AddAnswerCO p = psc.getItem(AddAnswerCO.class, key);
                rpc.addAnswer(p.getTenantId(), p.getReportId(), p.getAnswer(),
                        new SendingCallback(callbacks, key));
            }
                break;

            case finishReport: {
                FinishReportCO p = psc.getItem(FinishReportCO.class, key);
                rpc.finishReport(p.getTenantId(), p.getReportId(), p.getEnd(),
                        p.getInterruptionCause(), new SendingCallback(
                                callbacks, key));
            }

                break;
            default:
                throw new IllegalArgumentException("Unknown cache type: " + cacheType);                
            }
        }
    }

    public class UpdateCacheTimer extends Timer {
        public abstract class CachingCallback<T> implements AsyncCallback<T> {
            private Set<CachingCallback<?>> callbacks;
            private TenantCacheVersionCO version;

            public CachingCallback(Set<CachingCallback<?>> callbacks,
                    TenantCacheVersionCO version) {
                super();
                this.version = version;
                this.callbacks = callbacks;
                this.callbacks.add(this);
            }

            private void onFinish() {
                callbacks.remove(this);
                if (callbacks.isEmpty()) {
                    psc.pushTenantVersion(version);
                    buzzy = false;
                }
            }

            @Override
            public final void onFailure(Throwable caught) {
                try {
                    online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
                } finally {
                    onFinish();
                }
            }

            @Override
            public final void onSuccess(T result) {
                try {
                    onResult(result);
                } finally {
                    onFinish();
                }
            }

            public abstract void onResult(T result);
        }

        private Set<CachingCallback<?>> callbacks = new HashSet<CachingCallback<?>>();
        private boolean buzzy = false;

        @Override
        public void run() {
            try {
                if (buzzy) {
                    return;
                }
                buzzy = true;

                // First of all clear all what not belong to current cached
                // tenants
                psc.removeOrphanCacheTenantItems();

                // If online > 0 we in offline mode and do next check only at
                // online time
                if (online > 0) {
                    if (System.currentTimeMillis() < online) {
                        buzzy = false;
                        return;
                    } else {
                        online = -1l;
                    }
                }

                // Find current tenant version
                final TenantCacheVersionCO currentTenantVersion = psc
                        .getCurrentTenantVersion();
                if (currentTenantVersion == null) {
                    buzzy = false;
                    return;
                }

                // Prepare current tenant version
                final TenantCacheVersionCO newTenantVersion = new TenantCacheVersionCO(
                        currentTenantVersion.getTenantId(), new Date());

                // Look for changes
                rpc.getChangesSince(currentTenantVersion.getTenantId(),
                        currentTenantVersion.getTime(),
                        new CachingCallback<List<ChangeMarkerVO>>(callbacks, newTenantVersion) {
                            @Override
                            public void onResult(List<ChangeMarkerVO> result) {
                                for (ChangeMarkerVO marker : result) {
                                    cacheChanges(marker, newTenantVersion);
                                }
                            }
                        });
            } catch (Throwable e) {
            }
        }

        private void cacheChanges(final ChangeMarkerVO marker, final TenantCacheVersionCO newTenantVersion) {
            if (marker.isGlobal()) {
                psc.removeThisItemsByRegex(psc.marker(CacheKind.cache),
                        psc.marker("tenantId", marker.getTenantId()));
                cacheChanges(
                        new ChangeMarkerVO(marker.getClientId(),
                                marker.getTenantId(),
                                CacheType.getActivePublications), newTenantVersion);
            } else {
                CacheType cacheType = marker.getType(); 
                switch (cacheType) {
                case getActivePublications:
                    rpc.getActivePulications(marker.getTenantId(),
                            new CachingCallback<List<ActivePublicationDTO>>(callbacks, newTenantVersion) {
                                @Override
                                public void onResult(final List<ActivePublicationDTO> result) {
                                    // Leave only items what belong to result
                                    List<String> testsToLeave = new ArrayList<String>();
                                    List<String> reportsToLeave = new ArrayList<String>();

                                    for (ActivePublicationDTO newAp : result) {
                                        testsToLeave.add(newAp.getPublication().getId());                                        
                                        if (newAp.getLastFullReportId() != null) {
                                            reportsToLeave.add(newAp.getLastFullReportId());
                                        }
                                    }

                                    // Remove obsolete startTest cache items
                                    String startTestMarker = psc.marker(
                                            CacheKind.cache, CacheType.startTest,
                                            "tenantId", marker.getTenantId());
                                    if (testsToLeave.size() > 0) {
                                        String leaveRegex = psc
                                                .marker("publicationId",
                                                        testsToLeave.toArray(new String[0]));

                                        psc.leaveOnlyThisItemsByRegex(startTestMarker, leaveRegex);
                                    }
                                    else {
                                        psc.removeThisItemsByRegex(startTestMarker, ".*");
                                    }

                                    // Remove obsolete getReport cache items
                                    String getReportMarker = psc.marker(
                                            CacheKind.cache, CacheType.getReport,
                                            "tenantId", marker.getTenantId());
                                    if (reportsToLeave.size() > 0) {
                                        String leaveRegex = psc.marker("reportId", 
                                                reportsToLeave.toArray(new String[0]));
                                        
                                        psc.leaveOnlyThisItemsByRegex(getReportMarker, leaveRegex);
                                    }
                                    else {
                                        psc.removeThisItemsByRegex(getReportMarker, ".*");
                                    }

                                    // Cache all what not cached
                                    Set<String> cacheKeys = psc.getKeys(psc.marker(CacheKind.cache, null, "tenantId", marker.getTenantId()));
                                    
                                    for (ActivePublicationDTO newAp : result) {
                                        ChangeMarkerVO testMarker = new ChangeMarkerVO(
                                                marker.getClientId(), marker.getTenantId(), 
                                                CacheType.startTest, "publicationId", newAp.getPublication().getId());

                                        if (!cacheKeys.contains(psc.key(testMarker))) {
                                            cacheChanges(testMarker, newTenantVersion);
                                        }

                                        if (newAp.getLastFullReportId() != null) {
                                            ChangeMarkerVO reportMarker = new ChangeMarkerVO(
                                                    marker.getClientId(),marker.getTenantId(),
                                                    CacheType.getReport, "reportId", newAp.getLastFullReportId());

                                            if (!cacheKeys.contains(psc.key(reportMarker))) {
                                                cacheChanges(reportMarker, newTenantVersion);
                                            }
                                        }
                                    }
                                    // Cache getActivePublications result
                                    psc.setItem(psc.key(marker), result);
                                }
                            });
                    break;
                case startTest:
                    rpc.startTest(marker.getTenantId(),
                        marker.getKeyEntry("publicationId"),
                        new CachingCallback<ReportVO>(callbacks, newTenantVersion) {
                            @Override
                            public void onResult(ReportVO result) {
                                psc.setItem(psc.key(marker), result);
                            }
                        });
                    break;
                case getReport: 
                    rpc.getReport(marker.getTenantId(),
                            marker.getKeyEntry("reportId"),
                            new CachingCallback<ReportVO>(callbacks, newTenantVersion) {
                                @Override
                                public void onResult(ReportVO result) {
                                    psc.setItem(psc.key(marker), result);
                                }
                            });                    
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cache type: " + cacheType);
                }
            }
        }
    }
}
