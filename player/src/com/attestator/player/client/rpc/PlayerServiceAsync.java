package com.attestator.player.client.rpc;

import java.util.Date;
import java.util.List;

import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>AdminService</code>.
 */
public interface PlayerServiceAsync {
    void getChangesSince(String tenantId, Date time, AsyncCallback<List<ChangeMarkerVO>> callback) throws IllegalStateException;
    
    void getActivePulications(String tenantId, AsyncCallback<List<ActivePublicationDTO>> callback) throws IllegalStateException;
    void renewTest(String tenantId, String publicationId, AsyncCallback<ReportVO> callback) throws IllegalStateException;
    void startTest(String tenantId, String publicationId, AsyncCallback<ReportVO> callback) throws IllegalStateException;
    void getReport(String tenantId, String reportId, AsyncCallback<ReportVO> callback) throws IllegalStateException;
    
    void startReport(String tenantId, ReportVO report, Date start, AsyncCallback<Void> callback) throws IllegalStateException;
    void addAnswer(String tenantId, String reportId, AnswerVO answer, AsyncCallback<Void> callback) throws IllegalStateException;
    void finishReport(String tenantId, String reportId, Date end, InterruptionCauseEnum interruptionCause, AsyncCallback<Void> callback) throws IllegalStateException;
}
