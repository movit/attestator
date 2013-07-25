package com.attestator.player.client.rpc;

import java.util.List;

import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>AdminService</code>.
 */
public interface PlayerServiceAsync {
    void getActivePulications(String tenantId, AsyncCallback<List<ActivePublicationDTO>> callback)  throws IllegalStateException;
    void generateTest(String tenantId, String publicationId, AsyncCallback<PublicationVO> callback) throws IllegalStateException;
    void getReport(String tenantId, String reportId, AsyncCallback<ReportVO> callback) throws IllegalStateException;
    void startReport(String tenantId, ReportVO report, AsyncCallback<Void> callback) throws IllegalStateException;
    void addAnswer(String tenantId, String reportId, AnswerVO answer, AsyncCallback<Void> callback) throws IllegalStateException;
    void finishReport(String tenantId, String reportId, AsyncCallback<Void> callback) throws IllegalStateException;    
}
