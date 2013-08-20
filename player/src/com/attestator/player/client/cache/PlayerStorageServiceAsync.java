package com.attestator.player.client.cache;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.attestator.common.shared.SharedConstants;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.rpc.PlayerServiceAsync;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.attestator.player.shared.dto.TestDTO;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PlayerStorageServiceAsync implements PlayerServiceAsync {
    public static final int CACHE_POLLING_INTERVAL = 1000 * 10;
    public static final int OFFLINE_SLEEP_TIMEOUT  = 1000 * 60 * 2;
    
    private PlayerStorageCache psc;
    private PlayerServiceAsync rpc;
    
    private long    online = -1l;
    private boolean buzzy = false;

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
                psc.pushTenantVersion(getClientId(), version);
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
        (new UpdateServerCacheTimer()).scheduleRepeating(CACHE_POLLING_INTERVAL);
    }
    
    private boolean isOnline() {
        return online <= 0;
    }
    
    private <T> void callCallbackOnCachedValue(Class<T> clazz, String key, AsyncCallback<T> callback) {
        T cachedResult = psc.getItem(clazz, key);
        callback.onSuccess(cachedResult);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void getActivePulications(String tenantId,
            AsyncCallback<List<ActivePublicationDTO>> callback)
            throws IllegalStateException {
        psc.setCurrentTenant(getClientId(), tenantId);
        String key = psc.key("clientId", getClientId(), "tenantId", tenantId, "type", "getActivePulications");
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
        psc.setCurrentTenant(getClientId(), tenantId);
        String key = psc.key("clientId", getClientId(), "tenantId", tenantId, "type", "getActiveTest", "publicationId", publicationId);
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
        psc.setCurrentTenant(getClientId(), tenantId);
        String key = psc.key("clientId", getClientId(), "tenantId", tenantId, "type", "getReport", "reportId", reportId);
        if (isOnline()) {
            rpc.getReport(tenantId, reportId, new CacheReaderCallback<ReportVO>(ReportVO.class, key, callback));
        }
        else {
            callCallbackOnCachedValue(ReportVO.class, key, callback);
        }
    }

    @Override
    public void startReport(String tenantId, ReportVO report,
            AsyncCallback<Void> callback) throws IllegalStateException {        
        rpc.startReport(tenantId, report, callback);
    }

    @Override
    public void addAnswer(String tenantId, String reportId, AnswerVO answer,
            AsyncCallback<Void> callback) throws IllegalStateException {
    }

    @Override
    public void finishReport(String tenantId, String reportId, boolean interrupted,
            AsyncCallback<Void> callback) throws IllegalStateException {
    }
    
    @Override
    public void getChangesSince(String tenantId, Date time,
            AsyncCallback<List<ChangeMarkerVO>> callback)
            throws IllegalStateException {
        throw new UnsupportedOperationException("getChangesSince not supported in PlayerStorageServiceAsync");
    }
    
    private String getClientId() {
        return Cookies.getCookie(SharedConstants.CLIENT_ID_COOKIE_NAME);
    }
        
    public class UpdateServerCacheTimer extends Timer {
        private Set<CachingCallback<?>> callbacks = new HashSet<PlayerStorageServiceAsync.CachingCallback<?>>();
        
        @Override
        public void run() {
            if (buzzy) {
                return;
            }
            buzzy = true;
            
            // First of all clear all what not belong to current client id and cached tenants
            psc.leaveOnlyThisClientTenantItems(getClientId());
            
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
            final TenantCacheVersionCO currentTenantVersion = psc.getCurrentTenantVersion(getClientId());
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
                psc.removeThisClientItems("clientId", getClientId(), "tenantId", marker.getTenantId());
                cacheChanges(new ChangeMarkerVO(marker.getTenantId(), "clientId", getClientId(), "type", "getActivePulications"), version);
            }
            else if ("getActivePulications".equals(marker.getKeyEntry("type"))) {                
                rpc.getActivePulications(marker.getTenantId(), new CachingCallback<List<ActivePublicationDTO>>(callbacks, version) {
                    @Override
                    public void onResult(final List<ActivePublicationDTO> result) {                        
                        @SuppressWarnings("unchecked")
                        List<ActivePublicationDTO> cached = psc.getItem(List.class, psc.key(marker));
                        
                        // Remove publications and reports what no longer to be cached
                        if (cached != null) {
                            Iterables.transform(cached, new Function<ActivePublicationDTO, Void>() {
                                @Override
                                public Void apply(final ActivePublicationDTO oldAp) {
                                    ActivePublicationDTO newAp = Iterables.find(result, new Predicate<ActivePublicationDTO>() {
                                        @Override
                                        public boolean apply(ActivePublicationDTO newAp) {
                                            return oldAp.getPublication().getId().equals(newAp.getPublication().getId());
                                        }
                                    });
                                    
                                    if (newAp == null) {
                                        // Remove tests and report for publication what should not be cached
                                        psc.removeThisClientItems("tenantId", marker.getTenantId(), "type", "getActiveTest", "publicationId", oldAp.getPublication().getId());
                                        psc.removeThisClientItems("tenantId", marker.getTenantId(), "type", "getReport", "reportId", oldAp.getLastFullReportId());
                                    }
                                    else if (oldAp.getLastFullReportId() != null) {                                        
                                        if (!oldAp.getLastFullReportId().equals(newAp.getLastFullReportId())) {
                                            // Remove report
                                            psc.removeThisClientItems("tenantId", marker.getTenantId(), "type", "getReport", "reportId", oldAp.getLastFullReportId());
                                        }
                                    }
                                    return null;
                                }
                            });                            
                        }                        
                        
                        // Cache all what new
                        Set<String> cacheKeys = psc.getKeys();
                        for (ActivePublicationDTO newAp : result) {
                            ChangeMarkerVO testMarker = new ChangeMarkerVO(marker.getTenantId(), 
                                    "clientId", getClientId(), "type", "getActiveTest", "publicationId", newAp.getPublication().getId());
                            
                            if (!cacheKeys.contains(psc.key(testMarker))) {
                                cacheChanges(testMarker, version);
                            }
                            
                            if (newAp.getLastFullReportId() != null) {
                                ChangeMarkerVO reportMarker = new ChangeMarkerVO(marker.getTenantId(), 
                                        "clientId", getClientId(), "type", "getReport", "reportId", newAp.getLastFullReportId());
                                
                                if (!cacheKeys.contains(psc.key(reportMarker))) {
                                    cacheChanges(reportMarker, version);
                                }
                            }
                        }
                        // Cache getActivePublications result
                        psc.setItem(psc.key(marker), result);
                    }
                });
            }
            else if ("getActiveTest".equals(marker.getKeyEntry("type"))) {
                rpc.getActiveTest(marker.getTenantId(), marker.getKeyEntry("publicationId"), new CachingCallback<TestDTO>(callbacks, version) {
                    @Override
                    public void onResult(TestDTO result) {
                        psc.setItem(psc.key(marker), result);
                    }
                });
            }
            else if ("getReport".equals(marker.getKeyEntry("type"))) {
                rpc.getReport(marker.getTenantId(), marker.getKeyEntry("reportId"), new CachingCallback<ReportVO>(callbacks, version) {
                    @Override
                    public void onResult(ReportVO result) {
                        psc.setItem(psc.key(marker), result);
                    }
                });
            }
        }        
    }
}
