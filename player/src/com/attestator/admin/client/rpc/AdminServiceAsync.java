package com.attestator.admin.client.rpc;

import java.util.List;
import java.util.Set;

import com.attestator.common.shared.dto.UserValidationError;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * The async counterpart of <code>AdminService</code>.
 */
public interface AdminServiceAsync {
    void loadQuestions(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<QuestionVO>> callback);
    void loadReports(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<ReportVO>> callback);
    void loadGroupsPage(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<GroupVO>> callback);
    void loadUsers(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<UserVO>> callback);
    
    void loadReport(String reportId, AsyncCallback<ReportVO> callback);
    void loadMetatest(String metatestId, AsyncCallback<MetaTestVO> callback);
    
    void loadPublications(AsyncCallback<ListLoadResult<PublicationVO>> callback);
    void loadPublicationsTree(PublicationsTreeItem root, AsyncCallback<List<PublicationsTreeItem>> callback);
    void loadPublicationsByMetatestId(String metatestId, ListLoadConfig config, AsyncCallback<ListLoadResult<PublicationVO>> callback);
    
    void getPrintPropertiesByMetatestId(String metatestId, AsyncCallback<PrintingPropertiesVO> callback);
    void getHtmlForPrinting(String printingPropertiesId,  AsyncCallback<String> callback);
    
    <T extends BaseVO> void get(String className, String id, AsyncCallback<T> callback);
    <T extends BaseVO> void copy(String className, String id, AsyncCallback<T> callback);

    void deleteQuestions(List<String> questionIds, AsyncCallback<Void> callback);
    void deleteReports(List<String> reportIds, AsyncCallback<Void> callback);
    void deletePublications(List<String> publicationIds, AsyncCallback<Void> callback);
    void deleteMetatests(List<String> metatestIds, AsyncCallback<Void> callback);

    void setQuestionsGroup(List<String> questionIds, String groupId, AsyncCallback<Void> callback);
    
    void loadAllGroups(AsyncCallback<List<GroupVO>> callback);
    void loadOwnGroups(AsyncCallback<List<GroupVO>> callback);
    void saveGroups(List<GroupVO> groups, AsyncCallback<Void> callback);
    
    void saveQuestion(QuestionVO question, AsyncCallback<Void> callback);
    void saveMetatest(MetaTestVO metatest, AsyncCallback<Void> callback);
    void saveMetatestSharingEntries(String id, List<SharingEntryVO> sharingEntries, AsyncCallback<Void> callback);
    void savePrintingProperties(PrintingPropertiesVO properties, AsyncCallback<Void> callback);
    void savePublication(PublicationVO publication, AsyncCallback<Void> callback);
    void setPublicationsForMetatest(String metatestId, List<PublicationVO> publications, AsyncCallback<Void> callback);
    void saveGroup(GroupVO question, AsyncCallback<Void> callback);
    
    void getLoggedUser(AsyncCallback<UserVO> callback);
    void login(String login, String password, AsyncCallback<UserVO> callback);
    void logout(AsyncCallback<Void> callback);  

    void validateForCreateNewUser(String email, String username, String password, AsyncCallback<Set<UserValidationError>> callback);
    void createNewUser(String email, String username, String password, AsyncCallback<UserVO> callback);
    void restorePassword(String email, AsyncCallback<Void> callback);

    void isThisLoggedUserPassword(String password, AsyncCallback<Boolean> callback);
    void isThisEmailExists(String email, AsyncCallback<Boolean> callback);
    void updateLoggedUser(String oldPassword, String newPassword, UserVO user, AsyncCallback<Void> callback);
}
