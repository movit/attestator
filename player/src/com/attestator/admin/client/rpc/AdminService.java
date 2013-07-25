package com.attestator.admin.client.rpc;

import java.util.List;

import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("adminservice")
public interface AdminService extends RemoteService {
    PagingLoadResult<QuestionVO> loadQuestions(FilterPagingLoadConfig loadConfig);
    PagingLoadResult<ReportVO> loadReports(FilterPagingLoadConfig loadConfig);
    List<GroupVO> getGroups();
    void saveGroup(GroupVO group);
    void setGroups(List<GroupVO> groups);
    void saveQuestion(QuestionVO question);
    void deleteQuestions(List<String> questionIds);
    void deleteReports(List<String> reportIds);
    void setQuestionsGroup(List<String> questionIds, String groupId);
    UserVO getLoggedUser();
    UserVO login(String login, String password);
    void logout();
    ReportVO getReport(String reportId);
}
