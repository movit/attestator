package com.attestator.player.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.client.rpc.PlayerService;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.attestator.player.shared.dto.TestDTO;
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
                resultItem.setNumberOfAttempts(numberOfAttempts);
                
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
    public ReportVO getLatestUnfinishedReport(String tenantId, String publicationId) {
        try {
            login(tenantId);
            String clientId = ClientIdManager.getThreadLocalClientId();
            return Singletons.pl().getLatestUnfinishedReport(clientId, publicationId);
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
    public void startReport(String tenantId, ReportVO report) throws IllegalStateException {
        try {
            login(tenantId);
            
            String clientId = ClientIdManager.getThreadLocalClientId();
            String host = getThreadLocalRequest().getRemoteAddr();
            
            Singletons.pl().startReport(report, clientId, host);
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
    public void finishReport(String tenantId, String reportId, boolean interrupted) throws IllegalStateException {
        try {
            login(tenantId);            
            Singletons.pl().finishReport(reportId, ClientIdManager.getThreadLocalClientId(), interrupted);
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
    public TestDTO getActiveTest(String tenantId, String publicationId)
            throws IllegalStateException {
        try {
            login(tenantId);
            
            PublicationVO publication = Singletons.pl().getActivePublication(publicationId);            
            if (publication == null) {
                throw new IllegalStateException("Active publication with id " + publicationId + " not found.");
            }
            
            List<QuestionVO> questions = Singletons.pl().getQuestions(publication);
            
            TestDTO result = new TestDTO();
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
}
