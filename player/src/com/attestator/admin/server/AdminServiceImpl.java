package com.attestator.admin.server;

import java.util.List;

import org.apache.log4j.Logger;

import com.attestator.admin.client.rpc.AdminService;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.ListLoadResultBean;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class AdminServiceImpl extends RemoteServiceServlet implements
        AdminService {    
    
    private static final String DEFAULT_ERROR_MESSAGE = "Ошибка сервера";
    private static final Logger logger = Logger.getLogger(AdminServiceImpl.class);
    
    @Override
    public PagingLoadResult<QuestionVO> loadQuestions(FilterPagingLoadConfig loadConfig) throws IllegalStateException {
        try {
            return Singletons.al().loadQuestions(loadConfig);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public List<GroupVO> loadGroups() throws IllegalStateException {
        try {
            return Singletons.al().getAllGroups();
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void saveQuestion(QuestionVO question) throws IllegalStateException {
        try {
            Singletons.al().saveQuestion(question);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void saveGroup(GroupVO group) throws IllegalStateException {
        try {
            Singletons.al().saveGroup(group);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void saveGroups(List<GroupVO> groups) throws IllegalStateException {
        try {
            Singletons.al().setGroups(groups);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deleteQuestions(List<String> questionIds) throws IllegalStateException {
        try {
            Singletons.al().deleteQuestions(questionIds);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void setQuestionsGroup(List<String> questionIds, String groupId) throws IllegalStateException  {
        try {
            Singletons.al().setQuestionsGroup(questionIds, groupId);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public UserVO login(String login, String password) throws IllegalStateException {
        try {
            return LoginManager.login(getThreadLocalRequest().getSession(), login, password);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void logout() throws IllegalStateException {
        try {
            LoginManager.logout(getThreadLocalRequest().getSession());        
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public UserVO getLoggedUser() throws IllegalStateException {        
        try {
            return LoginManager.getThreadLocalLoggedUser();
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public PagingLoadResult<ReportVO> loadReports(
            FilterPagingLoadConfig loadConfig) throws IllegalStateException  {
        try {
            return Singletons.al().loadReports(loadConfig, "questions", "answers");
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deleteReports(List<String> reportIds) throws IllegalStateException {
        try {
            Singletons.al().deleteReports(reportIds);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deletePublications(List<String> publicationIds) throws IllegalStateException {
        try {
            Singletons.al().deletePublications(publicationIds);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public ReportVO loadReport(String reportId) throws IllegalStateException {
        try {
            return Singletons.al().getReport(reportId);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public ListLoadResult<PublicationVO> loadPublications() throws IllegalStateException {
        try {
            return new ListLoadResultBean<PublicationVO>(Singletons.al().getAllPublications());
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<PublicationsTreeItem> loadPublicationsTree(PublicationsTreeItem root) throws IllegalStateException {
        try {
            if (root == null) {
                return (List<PublicationsTreeItem>)((List<?>)Singletons.al().getAllMetaTests()); 
            }
            else if (root instanceof MetaTestVO) {
                return (List<PublicationsTreeItem>)((List<?>)Singletons.al().loadPublicationsByMetatestId(((MetaTestVO) root).getId()));
            }
            return null;
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void savePublication(PublicationVO publication) throws IllegalStateException {
        try {
            Singletons.al().savePublication(publication);
        }
        catch (Throwable e) {
            logger.error("Error: ", e);
            throw new IllegalStateException(DEFAULT_ERROR_MESSAGE, e);
        }
    }
}
