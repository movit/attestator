package com.attestator.player.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.client.rpc.PlayerService;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PlayerServiceImpl extends RemoteServiceServlet implements
        PlayerService {
    private static final String DEFAULT_ERROR_MESSAGE = "Ошибка сервера";
    private static final Logger logger = Logger.getLogger(PlayerServiceImpl.class);    
    
    private void login(String tenantId) throws LoginException {
        UserVO user = LoginManager.setThreadLocalTenantId(tenantId);
        if (user == null) {
            throw new LoginException("Неверный банк тестов");
        }
    }
    
    @Override
    public List<ActivePublicationDTO> getActivePulications(String tenantId) throws IllegalStateException {
        try {
            login(tenantId);
            
            List<ActivePublicationDTO> result = new ArrayList<ActivePublicationDTO>();
            List<PublicationVO> activePublications = Singletons.pl().getActivePublications(); 
            String clientId = ClientIdManager.getThreadLocalClientId();
            
            for (PublicationVO publication: activePublications) {
                long numberOfAttempts = Singletons.pl().getNumberOfAttempts(publication.getId(), clientId);
                String lastFullReportId = Singletons.pl().getLatestFinishedReportId(publication.getId(), clientId);
                
                ActivePublicationDTO resultItem = new ActivePublicationDTO();
                resultItem.setPublication(publication);
                resultItem.setLastFullReportId(lastFullReportId);
                
                if (!publication.isThisUnlimitedAttempts()) {
                    ReportVO reportForRenew = Singletons.pl().getLastReportForRenew(clientId, publication.getId());
                    
                    if (reportForRenew != null) {
                        numberOfAttempts--;
                    }
                    
                    long attemptsLeft = publication.getMaxAttempts() - numberOfAttempts;
                    attemptsLeft = Math.max(attemptsLeft, 0);                    
                    resultItem.setAttemptsLeft(attemptsLeft);
                }
                
                result.add(resultItem);
            }
            
            return result;
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }    
    
    
    @Override
    public ReportVO getReport(String tenantId, String reportId) throws IllegalStateException {
        try {
            login(tenantId);

            return Singletons.pl().getReport(reportId, ClientIdManager.getThreadLocalClientId());
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public void startReport(String tenantId, ReportVO report, Date start) throws IllegalStateException {
        try {
            login(tenantId);
            
            String clientId = ClientIdManager.getThreadLocalClientId();
            String host = getThreadLocalRequest().getRemoteAddr();
            
            Singletons.pl().startReport(report, start, clientId, host);
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public void addAnswer(String tenantId, String reportId, AnswerVO answer)
            throws IllegalStateException {
        try {
            login(tenantId);

            Singletons.pl().addAnswer(reportId, ClientIdManager.getThreadLocalClientId(), answer);
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public void finishReport(String tenantId, String reportId, Date end, InterruptionCauseEnum interruptionCause) throws IllegalStateException {
        try {
            login(tenantId);            
            Singletons.pl().finishReport(reportId, ClientIdManager.getThreadLocalClientId(), end, interruptionCause);
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public List<ChangeMarkerVO> getChangesSince(String tenantId, Date time) {
        try {
            login(tenantId);
            String clientId = ClientIdManager.getThreadLocalClientId();
            List<ChangeMarkerVO> result = Singletons.pl().getChangesSince(time, clientId);
            return result;            
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public ReportVO startTest(String tenantId, String publicationId) throws IllegalStateException {        
        try {
            login(tenantId);
            String clientId = ClientIdManager.getThreadLocalClientId();
            
            // Look for active publication
            PublicationVO publication = Singletons.pl().getActivePublication(publicationId);
            
            if (publication == null) {
                return null;
            }            

            if (!publication.isThisUnlimitedAttempts()) {
                long numberOfAttempts = Singletons.pl().getNumberOfAttempts(publicationId, clientId);
                if (numberOfAttempts >= publication.getMaxAttempts()) {
                    return null;
                }
            }

            // Active publication found. Generate new test
            List<QuestionVO> questions = Singletons.pl().generateQuestionList(publication);
            
            ReportVO result = new ReportVO();
            result.setPublication(publication);
            result.setQuestions(questions);
            
            return result;
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }

    @Override
    public ReportVO renewTest(String tenantId, String publicationId)
            throws IllegalStateException {
        try {
            login(tenantId);
            
            String clientId = ClientIdManager.getThreadLocalClientId();
            
            // Look for unfinished report
            ReportVO result = Singletons.pl().getLastReportForRenew(clientId, publicationId);
            
            // We have something for renew and this is has limited attempts
            if (result != null && !result.getPublication().isThisUnlimitedAttempts()) {
                long numberOfAttempts = Singletons.pl().getNumberOfAttempts(publicationId, clientId);
                if (numberOfAttempts > result.getPublication().getMaxAttempts()) {
                    return null;
                }
            }
            
            return result;
            
        }
        catch (LoginException e) {
            logger.info(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
        }
    }
}
