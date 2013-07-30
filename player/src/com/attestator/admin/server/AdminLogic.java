package com.attestator.admin.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.attestator.common.server.CommonLogic;
import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class AdminLogic extends CommonLogic {
    private static final Logger logger = Logger.getLogger(AdminLogic.class);

    public List<GroupVO> getAllGroups() {
        Query<GroupVO> q = Singletons.ds().createQuery(GroupVO.class).order("name, _id");
        List<GroupVO> result = q.asList();
        
        // Put default group to the top of list
        int defaultIndex = 0;
        GroupVO defaultGroup = null;
        for (GroupVO group: result) {
            if (LoginManager.getThreadLocalLoggedUser().getDefaultGroupId().equals(group.getId())) {
                defaultGroup = group;
                break;
            }
            defaultIndex++;
        }
        
        if (defaultGroup != null) {
            result.remove(defaultIndex);
            result.add(0, defaultGroup);
        }
        
        return result;
    }
    
    private <T> void addOrders(Query<T> q, List<? extends SortInfo> orders) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        boolean idAdded = false;
        for (SortInfo order: orders) {
            if (StringHelper.isEmptyOrNull(order.getSortField())) {
                logger.warn("Incorrect SortInfo");
            }
            if (i > 0) {
                sb.append(",");
            }
            if (order.getSortDir() == SortDir.DESC) {
                sb.append("-");
            }
            sb.append(order.getSortField());
            
            if ("_id".equals(order.getSortField())) {
                idAdded = true;
            }
            
            i++;
        }
        
        // To prevent order of equal elements
        if (!idAdded) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("_id");
        }
        
        q.order(sb.toString());
    }
    
    private <T> void addFilters(Query<T> q, List<FilterConfig> filters) {
        for (FilterConfig filter: filters) {
            if (StringHelper.isEmptyOrNull(filter.getValue())
            ||  StringHelper.isEmptyOrNull(filter.getField())) {
                logger.warn("Incorrect FilterConfig");
                continue;
            }
            
            if ("string".equals(filter.getType())) {
                List<Pattern> keywordPatterns = new ArrayList<Pattern>();
                String[] keywords = filter.getValue().split("\\s+");
                for (String keyword: keywords) {
                    if (StringHelper.isEmptyOrNull(keyword)) {
                        continue;
                    }
                    keywordPatterns.add(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE));                    
                }
                q.field(filter.getField()).hasAllOf(keywordPatterns);
            }
        }
    }

    public PagingLoadResult<ReportVO> loadReports(FilterPagingLoadConfig loadConfig) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        // Create query
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);

        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
            addOrders(q, loadConfig.getSortInfo());
        }
        else {
            q.order("-start, _id");
        }
        
        
        // Get total count (without offset and limit)
        long count = q.countAll();        
                
        q.offset(loadConfig.getOffset());
        q.limit(loadConfig.getLimit());
        
        List<ReportVO> qRes = q.asList();
        
        PagingLoadResultBean<ReportVO> result = new PagingLoadResultBean<ReportVO>();
        result.setData(qRes);
        result.setOffset(loadConfig.getOffset());
        result.setTotalLength((int)count);
        
        return result;
    }

    public PagingLoadResult<QuestionVO> loadQuestions(FilterPagingLoadConfig loadConfig) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        
        // Create query
        Query<QuestionVO> q = Singletons.ds().createQuery(QuestionVO.class);
        
        // Add filters
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getFilters())) {
            addFilters(q, loadConfig.getFilters());
        }
        
        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
            addOrders(q, loadConfig.getSortInfo());
        }
        else {
            q.order("_id");
        }

        // Get total count (without offset and limit)
        long count = q.countAll();        
                
        q.offset(loadConfig.getOffset());
        q.limit(loadConfig.getLimit());
        
        List<QuestionVO> qRes = q.asList();
        
        PagingLoadResultBean<QuestionVO> result = new PagingLoadResultBean<QuestionVO>();
        result.setData(qRes);
        result.setOffset(loadConfig.getOffset());
        result.setTotalLength((int)count);
        
        return result;
    }
    
    public void saveQuestion(QuestionVO question) {
        CheckHelper.throwIfNull(question, "question");
        
        Singletons.ds().save(question);
    }
    
    public void saveGroup(GroupVO group) {
        CheckHelper.throwIfNull(group, "group");
        
        Singletons.ds().save(group);
    }
    
    public GroupVO getDeafultGroup() {
        return getById(GroupVO.class, LoginManager.getThreadLocalLoggedUser().getDefaultGroupId());
    }
    
    public void setGroups(List<GroupVO> groupsToSave) {
        CheckHelper.throwIfNullOrEmpty(groupsToSave, "groupsToSave");
        
        GroupVO defaultGroup = getDeafultGroup(); 
                
        List<GroupVO> groupsToDelete = getAllGroups();
        groupsToDelete.removeAll(groupsToSave);
        groupsToDelete.remove(defaultGroup);
        
        for (GroupVO groupToDelete: groupsToDelete) {
            // Switch questions to default group
            Query<QuestionVO> qQuestion = Singletons.ds().createQuery(QuestionVO.class);
            qQuestion.field("groupId").equal(groupToDelete.getId());
            
            UpdateOperations<QuestionVO> uoQuestion = Singletons.ds().createUpdateOperations(QuestionVO.class);
            uoQuestion.set("groupId", defaultGroup.getId());
            uoQuestion.set("groupName", defaultGroup.getName());
            
            Singletons.ds().update(qQuestion, uoQuestion);
            
            // Switch metatests to default group 
            // Leave this code as positioning array access example
//            Query<MetaTestVO> qMetatest = Singletons.ds().createQuery(MetaTestVO.class);
//            qMetatest.disableValidation().filter("entries.groupId", groupToDelete.getId()).enableValidation();
//            
//            UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
//            uoMetatest.disableValidation().set("entries.$.groupId", GroupVO.DEFAULT_GROUP_ID).enableValidation();
//            
//            Singletons.ds().update(qMetatest, uoMetatest);
            
            
            //Delete metatest entries with deleted group            
            Query<MetaTestVO> qMetatest = Singletons.ds().createQuery(MetaTestVO.class);
            qMetatest.disableValidation().filter("entries.groupId", groupToDelete.getId()).enableValidation();
          
            MTEGroupVO mteTemplate = ReflectionHelper.createEmpty(MTEGroupVO.class);
            mteTemplate.setGroupId(groupToDelete.getId());
            
            UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
            uoMetatest.removeAll("entries", mteTemplate);
          
            Singletons.ds().update(qMetatest, uoMetatest);
            
            // Delete group
            Singletons.ds().delete(groupToDelete);
        }
        
        for (GroupVO groupToSave: groupsToSave) {
            // Switch questions to new group name
            Query<QuestionVO> qQuestion = Singletons.ds().createQuery(QuestionVO.class);
            qQuestion.field("groupId").equal(groupToSave.getId());
            
            UpdateOperations<QuestionVO> uoQuestion = Singletons.ds().createUpdateOperations(QuestionVO.class);
            uoQuestion.set("groupName", groupToSave.getName());
            
            Singletons.ds().update(qQuestion, uoQuestion);
            
            // Save group
            Singletons.ds().save(groupToSave);
        }
    }
    
    public void deleteQuestions(List<String> questionIds) {
        CheckHelper.throwIfNull(questionIds, "questionIds");
        
        if (questionIds.isEmpty()) {
            return;
        }
        
        // Remove all metatest entries referanced to this question
        Query<MetaTestVO> qMetatest = Singletons.ds().createQuery(MetaTestVO.class);
        qMetatest.disableValidation().field("entries.groupId").in(questionIds).enableValidation();        
        
        ArrayList<MTEQuestionVO> mteTemplates = new ArrayList<MTEQuestionVO>();
        for (String questionId: questionIds) {
            MTEQuestionVO mteTemplate = ReflectionHelper.createEmpty(MTEQuestionVO.class);
            mteTemplate.setQuestionId(questionId);
            mteTemplates.add(mteTemplate);
        }
        
        UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
        uoMetatest.removeAll("entries", mteTemplates);

        Singletons.ds().update(qMetatest, uoMetatest);
        
        // Remove questions
        Query<QuestionVO> qQuestion = Singletons.ds().createQuery(QuestionVO.class);
        qQuestion.field("_id").in(questionIds);
        
        
        Singletons.ds().delete(qQuestion);
    }
    
    public void setQuestionsGroup(List<String> questionIds, String groupId) {
        CheckHelper.throwIfNull(questionIds, "questionIds");
        CheckHelper.throwIfNull(groupId, "groupId");
        
        if (questionIds.isEmpty()) {
            return;
        }
        
        Query<GroupVO> qGroup = Singletons.ds().createQuery(GroupVO.class);
        qGroup.field("_id").equal(groupId);
        GroupVO group = qGroup.get();
        if (group == null) {
            return;
        }
                
        Query<QuestionVO> q = Singletons.ds().createQuery(QuestionVO.class);
        q.field("_id").in(questionIds);
        
        UpdateOperations<QuestionVO> uo = Singletons.ds().createUpdateOperations(QuestionVO.class);
        uo.set("groupId", group.getId());
        uo.set("groupName", group.getName());
        
        Singletons.ds().update(q, uo);
    }

    public UserVO createNewUser(String email, String password) {
        return createNewUser(email, password, null); 
    }
    
    public UserVO createNewUser(String email, String password, String tenantId) {
        CheckHelper.throwIfNullOrEmpty(email, "email");
        CheckHelper.throwIfNullOrEmpty(password, "password");
        
        UserVO result = new UserVO();
        result.setEmail(email);
        result.setPassword(password);
        
        if (tenantId != null) {
            result.setId(tenantId);
            result.setTenantId(tenantId);
            result.setDefaultGroupId(tenantId);
        }
        
        Singletons.rawDs().save(result);
        
        GroupVO defaultGroup = new GroupVO();
        defaultGroup.setId(result.getDefaultGroupId());
        defaultGroup.setTenantId(result.getTenantId());
        defaultGroup.setName(GroupVO.DEFAULT_GROUP_INITIAL_NAME);
        
        Singletons.rawDs().save(defaultGroup);
        
        return result;
    }
    
    
    public void deleteReports(List<String> reportsIds) {
        CheckHelper.throwIfNull(reportsIds, "reportsIds");
        
        if (reportsIds.isEmpty()) {
            return;
        }
        
        // Remove reports
        Query<ReportVO> qReport = Singletons.ds().createQuery(ReportVO.class);
        qReport.field("_id").in(reportsIds);
        
        
        Singletons.ds().delete(qReport);
    }
    
    public ReportVO getReport(String reportId) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        
        ReportVO result = q.get();
        return result;
    }
}
