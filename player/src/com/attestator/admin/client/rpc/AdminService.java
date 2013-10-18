package com.attestator.admin.client.rpc;

import java.util.List;

import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("adminservice")
public interface AdminService extends RemoteService {
    PagingLoadResult<QuestionVO> loadQuestions(FilterPagingLoadConfig loadConfig) throws IllegalStateException;
    PagingLoadResult<ReportVO> loadReports(FilterPagingLoadConfig loadConfig) throws IllegalStateException;
    PagingLoadResult<GroupVO> loadGroupsPage(FilterPagingLoadConfig loadConfig) throws IllegalStateException;
    
    ReportVO loadReport(String reportId) throws IllegalStateException;
    
    ListLoadResult<PublicationVO> loadPublications() throws IllegalStateException;
    ListLoadResult<PublicationVO> loadPublicationsByMetatestId(String metatestId, ListLoadConfig config) throws IllegalStateException;
    List<PublicationsTreeItem> loadPublicationsTree(PublicationsTreeItem root) throws IllegalStateException;

    List<GroupVO> loadGroups() throws IllegalStateException;
    
    void saveGroup(GroupVO group) throws IllegalStateException;
    void savePublication(PublicationVO publication) throws IllegalStateException;
    void saveGroups(List<GroupVO> groups) throws IllegalStateException;
    void saveQuestion(QuestionVO question) throws IllegalStateException;
    
    void deleteQuestions(List<String> questionIds) throws IllegalStateException;
    void deleteReports(List<String> reportIds) throws IllegalStateException;
    void deletePublications(List<String> publicationIds) throws IllegalStateException;
    
    void setQuestionsGroup(List<String> questionIds, String groupId) throws IllegalStateException;
    
    UserVO getLoggedUser() throws IllegalStateException;
    UserVO login(String login, String password) throws IllegalStateException;
    void logout() throws IllegalStateException;
}
