package com.attestator.player.client.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.client.cache.co.AddAnswerCO;
import com.attestator.player.client.cache.co.FinishReportCO;
import com.attestator.player.client.cache.co.StartReportCO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.rpc.PlayerServiceAsync;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.attestator.player.shared.dto.TestDTO;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PlayerStorageServiceAsync implements PlayerServiceAsync {
    public static final int CACHE_POLLING_INTERVAL  = 1000 * 30;
    public static final int REPORT_POLLING_INTERVAL = 1000 * 5;
    public static final int OFFLINE_SLEEP_TIMEOUT   = 1000 * 60 * 2;
    
    private PlayerStorageCache psc;
    private PlayerServiceAsync rpc;
    
    private long online = -1l;
    
    public class CacheReaderCallback<T> implements AsyncCallback<T> {
        private Class<T>         clazz;
        private String           key;
        private AsyncCallback<T> callback;
        
        public CacheReaderCallback(Class<T> clazz, String key,
                AsyncCallback<T> callback) {
            super();
            this.clazz = clazz;
            this.key = key;
            this.callback = callback;
        }

        @Override
        public final void onFailure(Throwable caught) {
            online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
            callCallbackOnCachedValue(clazz, key, callback);
        }

        @Override
        public final void onSuccess(T result) {
            callback.onSuccess(result);
        }
    }
    
    public PlayerStorageServiceAsync(PlayerServiceAsync arpc) {
        this.psc = PlayerStorageCache.getPlayerStorageIfSupported();
        this.rpc = arpc;
        (new UpdateCacheTimer()).scheduleRepeating(CACHE_POLLING_INTERVAL);
        (new SendReportsTimer()).scheduleRepeating(REPORT_POLLING_INTERVAL);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void getActivePulications(String tenantId,
            AsyncCallback<List<ActivePublicationDTO>> callback)
            throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        String key = psc.cacheKey("tenantId", tenantId, "type", "getActivePulications");
        if (isOnline()) {
            rpc.getActivePulications(tenantId, new CacheReaderCallback<List<ActivePublicationDTO>>((Class<List<ActivePublicationDTO>>)(Class<?>)(List.class), key, callback));
        }
        else {
            callCallbackOnCachedValue((Class<List<ActivePublicationDTO>>)(Class<?>)List.class, key, callback);
        }
    }

    @Override
    public void getActiveTest(String tenantId, String publicationId,
            AsyncCallback<TestDTO> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        String key = psc.cacheKey("tenantId", tenantId, "type", "getActiveTest", "publicationId", publicationId);
        if (isOnline()) {
            rpc.getActiveTest(tenantId, publicationId, new CacheReaderCallback<TestDTO>(TestDTO.class, key, callback));
        }
        else {
            callCallbackOnCachedValue(TestDTO.class, key, callback);
        }
    }

    @Override
    public void getReport(String tenantId, String reportId,
            AsyncCallback<ReportVO> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        String key = psc.cacheKey("tenantId", tenantId, "type", "getReport", "reportId", reportId);
        if (isOnline()) {
            rpc.getReport(tenantId, reportId, new CacheReaderCallback<ReportVO>(ReportVO.class, key, callback));
        }
        else {
            callCallbackOnCachedValue(ReportVO.class, key, callback);
        }
    }     
    
    private ReportVO getUnfinishedReportFromCache(String publicationId) {
        Set<String> keys = psc.getKeys("kind=renew");
        ReportVO result = null;
        for (String key : keys) {
            Map<String, String> keyMap = psc.key(key);            
            if ("startReport".equals(keyMap.get("type"))) {
                StartReportCO item = psc.getItem(StartReportCO.class, key);
                if (item.getReport().getPublication().getId().equals(publicationId)) {
                    result = item.getReport();
                    continue;
                }
            }
            
            if (result == null) {
                continue;
            }
            
            if ("addAnswer".equals(keyMap.get("type"))) {
                AddAnswerCO item = psc.getItem(AddAnswerCO.class, key);
                if (item.getReportId().equals(result.getId())) {
                    result.getAnswers().add(item.getAnswer());
                }
            }
        }
        return result;
    }
    
    @Override
    public void getLatestUnfinishedReport(String tenantId, 
            final String publicationId, final AsyncCallback<ReportVO> callback)
            throws IllegalStateException {
        
        psc.setCurrentTenant(tenantId);
        
        // First of all look in cache for unfinished report
        ReportVO report = getUnfinishedReportFromCache(publicationId);
        if (report != null) {
            if (ReportHelper.isRenewAllowed(report)) {
                callback.onSuccess(report);
            }
            else {
                callback.onSuccess(null);
            }
            return;
        }        
        
        // Unable to load from cache before, so try got them from server
        if (isOnline()) {
            rpc.getLatestUnfinishedReport(tenantId, publicationId, new AsyncCallback<ReportVO>() {
                @Override
                public void onSuccess(ReportVO result) {
                    callback.onSuccess(result);
                }                
                @Override
                public void onFailure(Throwable caught) {
                    callback.onSuccess(null);
                }
            });
        }
        else {        
            callback.onSuccess(null);
        }
    }

    @Override
    public void startReport(String tenantId, ReportVO report,
            AsyncCallback<Void> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        
        report.setStart(new Date());
        StartReportCO item = new StartReportCO(tenantId, report);
        
        String reportKey = psc.reportKey("type", "startReport", "reportId", report.getId());
        psc.setItem(reportKey, item);        
        
        psc.removeThisItemsByRegex("kind=renew", ".*");
        String renewKey = psc.renewKey("type", "startReport", "reportId", report.getId());
        psc.setItem(renewKey, item);   
        
        callback.onSuccess(null);
    }

    @Override
    public void addAnswer(String tenantId, String reportId, AnswerVO answer,
            AsyncCallback<Void> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);        
        AddAnswerCO item = new AddAnswerCO(tenantId, reportId, answer);
        
        String reportKey = psc.reportKey("type", "addAnswer", "reportId", reportId);
        psc.setItem(reportKey, item);
        
        String renewKey = psc.renewKey("type", "addAnswer", "reportId", reportId);
        psc.setItem(renewKey, item);        
        
        callback.onSuccess(null);
    }

    @Override
    public void finishReport(String tenantId, String reportId, boolean interrupted,
            AsyncCallback<Void> callback) throws IllegalStateException {
        psc.setCurrentTenant(tenantId);
        String key = psc.reportKey("type", "finishReport", "reportId", reportId);
        FinishReportCO item = new FinishReportCO(tenantId, reportId, interrupted);        
        psc.setItem(key, item);
        psc.removeThisItemsByRegex("kind=renew", ".*");
        callback.onSuccess(null);
    }
    
    @Override
    public void getChangesSince(String tenantId, Date time,
            AsyncCallback<List<ChangeMarkerVO>> callback)
            throws IllegalStateException {
        throw new UnsupportedOperationException("getChangesSince not supported in PlayerStorageServiceAsync");
    }
    
    private boolean isOnline() {
        return online <= 0;
    }

    private <T> void callCallbackOnCachedValue(Class<T> clazz, String key, AsyncCallback<T> callback) {
        T cachedResult = psc.getItem(clazz, key);
        if (cachedResult != null) {
            callback.onSuccess(cachedResult);
        }
        else {
            callback.onFailure(new IllegalStateException("Невозможно получить данные"));
        }
    }

    public class SendReportsTimer extends Timer {
        public class SendingCallback implements AsyncCallback<Void> {
            private Set<SendingCallback> callbacks;
            private String key;
            
            public SendingCallback(
                    Set<SendingCallback> callbacks, String key) {
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
                online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
                onFinish();
            }

            @Override
            public final void onSuccess(Void result) {
                psc.removeThisItemsByRegex("kind=report", key);
                sendNextReportItemIfAny();
                onFinish();
            }
        }
        
        private Set<SendingCallback> callbacks = new HashSet<SendingCallback>();
        private boolean buzzy = false;
        
        @Override
        public void run() {
            if (buzzy) {
                return;
            }
            buzzy = true;
            
            // If online > 0 we in offline mode and do next check only at online time
            if (online > 0) {
                if (System.currentTimeMillis() < online) {
                    buzzy = false;
                    return;
                }
                else {
                    online = -1l;
                }
            }
            
            sendNextReportItemIfAny();
        }
        
        private String getNextReportKey() {
            Set<String> keys = psc.getKeys("kind=report");            
            if (keys.iterator().hasNext()) {
                return keys.iterator().next();
            }
            else {
                return null;
            }
        }
        
        private void sendNextReportItemIfAny() {
            String keyStr = getNextReportKey();
            
            if (keyStr == null) {
                buzzy = false;
                return;
            }
            
            Map<String, String> keyMap = psc.key(keyStr);
            String method = keyMap.get("type");
            
            if ("startReport".equals(method)) {
                StartReportCO p = psc.getItem(StartReportCO.class, keyStr);
                rpc.startReport(p.getTenantId(), p.getReport(), new SendingCallback(callbacks, keyStr));
            }
            else if ("addAnswer".equals(method)) {
                AddAnswerCO p = psc.getItem(AddAnswerCO.class, keyStr);
                rpc.addAnswer(p.getTenantId(), p.getReportId(), p.getAnswer(), new SendingCallback(callbacks, keyStr));
            }
            else if ("finishReport".equals(method)) {
                FinishReportCO p = psc.getItem(FinishReportCO.class, keyStr);
                rpc.finishReport(p.getTenantId(), p.getTenantId(), p.isInterrupted(), new SendingCallback(callbacks, keyStr));
            }
            else {
                buzzy = false;
            }
        }
    }
    
    public class UpdateCacheTimer extends Timer {
        public abstract class CachingCallback<T> implements AsyncCallback<T> {
            private Set<CachingCallback<?>> callbacks;
            private TenantCacheVersionCO    version;
            
            public CachingCallback(
                    Set<CachingCallback<?>> callbacks, TenantCacheVersionCO  version) {
                super();
                this.version   = version;
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
                online = System.currentTimeMillis() + OFFLINE_SLEEP_TIMEOUT;
                onFinish();
            }

            @Override
            public final void onSuccess(T result) {
                onResult(result);
                onFinish();
            }
            
            public abstract void onResult(T result);
        }
        
        private Set<CachingCallback<?>> callbacks = new HashSet<CachingCallback<?>>();
        private boolean buzzy = false;
        
        @Override
        public void run() {
            if (buzzy) {
                return;
            }
            buzzy = true;
            
            // First of all clear all what not belong to current cached tenants
            psc.removeOrphanCacheTenantItems();
            
            // If online > 0 we in offline mode and do next check only at online time
            if (online > 0) {
                if (System.currentTimeMillis() < online) {
                    buzzy = false;
                    return;
                }
                else {
                    online = -1l;
                }
            }
            
            // Find current tenant version
            final TenantCacheVersionCO currentTenantVersion = psc.getCurrentTenantVersion();
            if (currentTenantVersion == null) {
                buzzy = false;
                return;
            }
            
            // Prepare current tenant version
            final TenantCacheVersionCO newTenantVersion = new TenantCacheVersionCO(currentTenantVersion.getTenantId(), new Date());            
            
            // Look for changes
            rpc.getChangesSince(currentTenantVersion.getTenantId(), currentTenantVersion.getTime(), 
                new CachingCallback<List<ChangeMarkerVO>>(callbacks, newTenantVersion) {
                    @Override
                    public void onResult(List<ChangeMarkerVO> result) {
                        for (ChangeMarkerVO marker : result) {
                            cacheChanges(marker, newTenantVersion);
                        }                        
                    }
                });
        }
        
        private void cacheChanges(final ChangeMarkerVO marker, final TenantCacheVersionCO version) {            
            if (marker.isGlobal()) {
                psc.removeThisItemsByRegex("kind=cache", psc.marker("tenantId", marker.getTenantId()));
                cacheChanges(new ChangeMarkerVO(marker.getClientId(), marker.getTenantId(), "type", "getActivePulications"), version);
            }
            else if ("getActivePulications".equals(marker.getKeyEntry("type"))) {                
                rpc.getActivePulications(marker.getTenantId(), new CachingCallback<List<ActivePublicationDTO>>(callbacks, version) {
                    @Override
                    public void onResult(final List<ActivePublicationDTO> result) {                        
                        
                        // Leave only items what belong to result
                        List<String> publicationsToLeave = new ArrayList<String>();
                        List<String> reportsToLeave = new ArrayList<String>();
                        
                        for (ActivePublicationDTO newAp : result) {
                            publicationsToLeave.add(newAp.getPublication().getId());
                            if (newAp.getLastFullReportId() != null) {
                                reportsToLeave.add(newAp.getLastFullReportId());
                            }
                        }
                        
                        if (publicationsToLeave.size() > 0) {
                            String checkMarker = psc.marker("kind", "cache", "type", "getActiveTest", "tenantId", marker.getTenantId());
                            String leaveRegex = psc.valuesMarker("publicationId", publicationsToLeave.toArray(new String[0]));
                            psc.leaveOnlyThisItemsByRegex(checkMarker, leaveRegex);
                        }
                        
                        if (reportsToLeave.size() > 0) {
                            String checkMarker = psc.marker("kind", "cache", "type", "getReport", "tenantId", marker.getTenantId());
                            String leaveRegex = psc.valuesMarker("reportId", reportsToLeave.toArray(new String[0]));
                            psc.leaveOnlyThisItemsByRegex(checkMarker, leaveRegex);
                        }
                        
                        // Cache all what not cached
                        Set<String> cacheKeys = psc.getKeys();
                        for (ActivePublicationDTO newAp : result) {
                            ChangeMarkerVO testMarker = new ChangeMarkerVO(marker.getClientId(), 
                                    marker.getTenantId(), "type", "getActiveTest", "publicationId", newAp.getPublication().getId());
                            
                            if (!cacheKeys.contains(psc.cacheKey(testMarker))) {
                                cacheChanges(testMarker, version);
                            }
                            
                            if (newAp.getLastFullReportId() != null) {
                                ChangeMarkerVO reportMarker = new ChangeMarkerVO(marker.getClientId(), 
                                        marker.getTenantId(), "type", "getReport", "reportId", newAp.getLastFullReportId());
                                
                                if (!cacheKeys.contains(psc.cacheKey(reportMarker))) {
                                    cacheChanges(reportMarker, version);
                                }
                            }
                        }
                        // Cache getActivePublications result
                        psc.setItem(psc.cacheKey(marker), result);
                    }
                });
            }
            else if ("getActiveTest".equals(marker.getKeyEntry("type"))) {
                rpc.getActiveTest(marker.getTenantId(), marker.getKeyEntry("publicationId"), new CachingCallback<TestDTO>(callbacks, version) {
                    @Override
                    public void onResult(TestDTO result) {
                        psc.setItem(psc.cacheKey(marker), result);
                    }
                });
            }
            else if ("getReport".equals(marker.getKeyEntry("type"))) {
                rpc.getReport(marker.getTenantId(), marker.getKeyEntry("reportId"), new CachingCallback<ReportVO>(callbacks, version) {
                    @Override
                    public void onResult(ReportVO result) {
                        psc.setItem(psc.cacheKey(marker), result);
                    }
                });
            }
        }        
    }
}
