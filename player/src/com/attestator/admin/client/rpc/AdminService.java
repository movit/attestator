package com.attestator.admin.client.rpc;

import java.util.List;

import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SharingEntryVO;
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
    PagingLoadResult<UserVO> loadUsers(FilterPagingLoadConfig loadConfig) throws IllegalStateException;
    
    ReportVO loadReport(String reportId) throws IllegalStateException;
    MetaTestVO loadMetatest(String metatestId) throws IllegalStateException;
    
    ListLoadResult<PublicationVO> loadPublications() throws IllegalStateException;
    ListLoadResult<PublicationVO> loadPublicationsByMetatestId(String metatestId, ListLoadConfig config) throws IllegalStateException;
    List<PublicationsTreeItem> loadPublicationsTree(PublicationsTreeItem root) throws IllegalStateException;

    List<GroupVO> loadAllGroups() throws IllegalStateException;
    List<GroupVO> loadOwnGroups() throws IllegalStateException;
    
    PrintingPropertiesVO getPrintPropertiesByMetatestId(String metatestId) throws IllegalStateException;
    String getHtmlForPrinting(String printingPropertiesId) throws IllegalStateException;
    
    <T extends BaseVO> T get(String className, String id);
    <T extends BaseVO> T copy(String className, String id);
    
    void saveGroup(GroupVO group) throws IllegalStateException;
    void savePublication(PublicationVO publication) throws IllegalStateException;
    void saveGroups(List<GroupVO> groups) throws IllegalStateException;
    void saveQuestion(QuestionVO question) throws IllegalStateException;
    void saveMetatest(MetaTestVO metatest) throws IllegalStateException;
    void saveMetatestSharingEntries(String metatestId, List<SharingEntryVO> sharingEntries) throws IllegalStateException;
    void savePrintingProperties(PrintingPropertiesVO properties) throws IllegalStateException;
    void setPublicationsForMetatest(String metatestId, List<PublicationVO> publication) throws IllegalStateException;
    
    void deleteQuestions(List<String> questionIds) throws IllegalStateException;
    void deleteMetatests(List<String> metatestIds) throws IllegalStateException;
    void deleteReports(List<String> reportIds) throws IllegalStateException;
    void deletePublications(List<String> publicationIds) throws IllegalStateException;
    
    void setQuestionsGroup(List<String> questionIds, String groupId) throws IllegalStateException;
    
    UserVO getLoggedUser() throws IllegalStateException;
    UserVO login(String login, String password) throws IllegalStateException;
    void logout() throws IllegalStateException;
    
    Boolean isThisLoggedUserPassword(String password) throws IllegalStateException;
    void updateLoggedUser(String oldPassword, String email, String newPassword) throws IllegalStateException;
}
