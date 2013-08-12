package com.attestator.player.client.rpc;

import java.util.List;

import com.attestator.common.client.helper.SerializationHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SCQAnswerVO;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.attestator.player.shared.dto.TestDTO;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PlayerStorageServiceAsync implements PlayerServiceAsync {
    private static final String METHODS_LIST_KEY = "METHODS_LIST_KEY";
    private Storage localStorage;
    private PlayerServiceAsync rpc;
    
        
    public PlayerStorageServiceAsync(PlayerServiceAsync arpc) {
        this.rpc = arpc;
        this.localStorage = Storage.getLocalStorageIfSupported(); 
        
        if (localStorage == null) {
            return;
        }
        
        RpcDefferdCalls calls = loadDefferdCalls();
        if (calls == null) {
            saveDefferedCalls(new RpcDefferdCalls());
        }        
        
        new Timer() {
            
            private boolean buzzy = false;
            
            @Override
            public void run() {
                if (buzzy) {
                    return;
                }
                buzzy = true;
                
                RpcDefferdCalls calls = loadDefferdCalls();
                
                if (calls.getCalls().isEmpty()) {
                    buzzy = false;
                    return;
                }
                
                final RpcCallDescriptor descriptor = calls.getCalls().get(0);
                
                if ("addAnswer".equals(descriptor.getMethod())) {                    
                    String   tenantId = descriptor.getArguments().get(0); 
                    String   reportId = descriptor.getArguments().get(1); 
                    AnswerVO answer   = SerializationHelper.deserialize(SCQAnswerVO.class, descriptor.getArguments().get(2));

                    rpc.addAnswer(tenantId, reportId, answer, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            buzzy = false;                            
                        }

                        @Override
                        public void onSuccess(Void result) {
                            RpcDefferdCalls calls = loadDefferdCalls();
                            calls.getCalls().remove(descriptor);
                            saveDefferedCalls(calls);
                            buzzy = false;
                        }
                    });                    
                }
                else if ("finishReport".equals(descriptor.getMethod())) {
                    String   tenantId = descriptor.getArguments().get(0); 
                    String   reportId = descriptor.getArguments().get(1); 
                    boolean  interrupted = Boolean.parseBoolean(descriptor.getArguments().get(2));
                    
                    rpc.finishReport(tenantId, reportId, interrupted, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            buzzy = false;                            
                        }

                        @Override
                        public void onSuccess(Void result) {
                            RpcDefferdCalls calls = loadDefferdCalls();
                            calls.getCalls().remove(descriptor);
                            saveDefferedCalls(calls);
                            buzzy = false;
                        }
                    });                    
                }
            }
        }.scheduleRepeating(2000);
    }
    
    private RpcDefferdCalls loadDefferdCalls() {
        String serializedCalls = localStorage.getItem(METHODS_LIST_KEY);
        if (serializedCalls != null) {
            RpcDefferdCalls result = SerializationHelper.deserialize(RpcDefferdCalls.class, serializedCalls);
            return result;
        }
        return null;
    }
    
    private void saveDefferedCalls(RpcDefferdCalls calls) {
        if (calls != null) {
            String serializedCalls = SerializationHelper.serialize(calls);
            localStorage.setItem(METHODS_LIST_KEY, serializedCalls);
        }
    }
    
    @Override
    public void getActivePulications(String tenantId,
            AsyncCallback<List<ActivePublicationDTO>> callback)
            throws IllegalStateException {
        rpc.getActivePulications(tenantId, callback);
    }


    @Override
    public void getReport(String tenantId, String reportId,
            AsyncCallback<ReportVO> callback) throws IllegalStateException {
        rpc.getReport(tenantId, reportId, callback);
    }

    @Override
    public void startReport(String tenantId, ReportVO report,
            AsyncCallback<Void> callback) throws IllegalStateException {        
        rpc.startReport(tenantId, report, callback);
    }

    @Override
    public void addAnswer(String tenantId, String reportId, AnswerVO answer,
            AsyncCallback<Void> callback) throws IllegalStateException {
        if (localStorage == null) {
            rpc.addAnswer(tenantId, reportId, answer, callback);
        }        
        
        RpcCallDescriptor descriptor = new RpcCallDescriptor("addAnswer", 
                tenantId, 
                reportId, 
                SerializationHelper.serialize(answer));
        
        RpcDefferdCalls calls = loadDefferdCalls();
        calls.getCalls().add(descriptor);
        saveDefferedCalls(calls);
        
        callback.onSuccess(null);
    }

    @Override
    public void finishReport(String tenantId, String reportId, boolean interrupted,
            AsyncCallback<Void> callback) throws IllegalStateException {
        if (localStorage == null) {
            rpc.finishReport(tenantId, reportId, interrupted, callback);
        }
        
        RpcCallDescriptor descriptor = new RpcCallDescriptor("finishReport", 
                tenantId, 
                reportId, 
                Boolean.toString(interrupted));
        
        RpcDefferdCalls calls = loadDefferdCalls();
        calls.getCalls().add(descriptor);
        saveDefferedCalls(calls);
        
        callback.onSuccess(null);
    }

    @Override
    public void getActiveTest(String tenantId, String publicationId,
            AsyncCallback<TestDTO> callback) throws IllegalStateException {
        rpc.getActiveTest(tenantId, publicationId, callback);
    }
}
