package com.attestator.player.client.rpc;

import java.util.Date;
import java.util.List;

import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("playerservice")
public interface PlayerService extends RemoteService {
    List<ChangeMarkerVO>       getChangesSince(String tenantId, Date time);
    
    List<ActivePublicationDTO> getActivePulications(String tenantId) throws IllegalStateException;

    ReportVO renewTest(String tenantId, String publicationId) throws IllegalStateException;
    ReportVO startTest(String tenantId, String publicationId) throws IllegalStateException;
    
    ReportVO getReport(String tenantId, String reportId) throws IllegalStateException;
    
    void startReport(String tenantId, ReportVO report, Date start) throws IllegalStateException;
    void addAnswer(String tenantId, String reportId, AnswerVO answer) throws IllegalStateException;
    void finishReport(String tenantId, String reportId, Date finish, InterruptionCauseEnum interruptionCause) throws IllegalStateException;
}
