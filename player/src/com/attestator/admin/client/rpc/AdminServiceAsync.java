package com.attestator.admin.client.rpc;

import java.util.List;

import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * The async counterpart of <code>AdminService</code>.
 */
public interface AdminServiceAsync {
    void loadQuestions(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<QuestionVO>> callback);
    void loadReports(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<ReportVO>> callback);
    void deleteQuestions(List<String> questionIds, AsyncCallback<Void> callback);
    void deleteReports(List<String> reportIds, AsyncCallback<Void> callback);
    void setQuestionsGroup(List<String> questionIds, String groupId, AsyncCallback<Void> callback);
    
    void getGroups(AsyncCallback<List<GroupVO>> callback);
    void setGroups(List<GroupVO> groups, AsyncCallback<Void> callback);
    
    void saveQuestion(QuestionVO question, AsyncCallback<Void> callback);
    void saveGroup(GroupVO question, AsyncCallback<Void> callback);
    
    void getLoggedUser(AsyncCallback<UserVO> callback);
    void login(String login, String password, AsyncCallback<UserVO> callback);
    void logout(AsyncCallback<Void> callback);
    
    void getReport(String reportId, AsyncCallback<ReportVO> callback);
}
