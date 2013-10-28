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
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class AdminLogic extends CommonLogic {
    private static final Logger logger = Logger.getLogger(AdminLogic.class);

    public List<GroupVO> loadAllGroups() {
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
    
    private <T> void addDefaultOrder(Class<T> clazz, Query<T> q) {
        if (ModificationDateAwareVO.class.isAssignableFrom(clazz)) {
            q.order("created, _id");
        }
        else {
            q.order("_id");
        }
    }
    
    public <T> PagingLoadResult<T> loadPage(Class<T> clazz, FilterPagingLoadConfig loadConfig, String ... excludFields) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        
        // Create query
        Query<T> q = Singletons.ds().createQuery(clazz);
        
        // Exclude fields if any
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }
    
        // Add filters
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getFilters())) {
            addFilters(q, loadConfig.getFilters());
        }
        
        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
            addOrders(q, loadConfig.getSortInfo());
        }
        else {
            addDefaultOrder(clazz, q);
        }

        // Get total count (without offset and limit)
        long count = q.countAll();        
                
        q.offset(loadConfig.getOffset());
        q.limit(loadConfig.getLimit());
        
        List<T> qRes = q.asList();
        
        PagingLoadResultBean<T> result = new PagingLoadResultBean<T>();
        result.setData(qRes);
        result.setOffset(loadConfig.getOffset());
        result.setTotalLength((int)count);
        
        return result;
    }        

    public PagingLoadResult<ReportVO> loadReports(FilterPagingLoadConfig loadConfig, String ... excludFields) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        // Create query
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);
        
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }
        
        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
        	List<? extends SortInfo> sortInfo = prepareReportSortInfo(loadConfig.getSortInfo());
            addOrders(q, sortInfo);
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

    public List<PublicationVO> loadPublicationsByMetatestId(String metatestId, ListLoadConfig config) {
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");

        Query<PublicationVO> q = Singletons.ds().createQuery(PublicationVO.class);
        q.field("metatestId").equal(metatestId);
        
        if (config != null &&  !NullHelper.nullSafeIsEmpty(config.getSortInfo())) {
            addOrders(q, config.getSortInfo());
        }
        else {
            addDefaultOrder(PublicationVO.class, q);
        }
        
        List<PublicationVO> result = q.asList();        
        
        return result;
    }

    public ReportVO loadReport(String reportId) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        
        ReportVO result = q.get();
        return result;
    }

    public MetaTestVO loadMetatest(String metatestId) {
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");
        
        Query<MetaTestVO> q = Singletons.ds().createQuery(MetaTestVO.class);        
        q.field("_id").equal(metatestId);
        
        MetaTestVO result = q.get();
        return result;
    }

    public List<MetaTestVO> loadAllMetaTests(String ... excludFields) {
        Query<MetaTestVO> q = Singletons.ds().createQuery(MetaTestVO.class);
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }        
        addDefaultOrder(MetaTestVO.class, q);        
        List<MetaTestVO> qRes = q.asList();        
        return qRes;
    }
    
    public List<PublicationVO> loadAllPublications() {
        Query<PublicationVO> q = Singletons.ds().createQuery(PublicationVO.class);
        addDefaultOrder(PublicationVO.class, q);
        List<PublicationVO> qRes = q.asList();        
        return qRes;
    }

    public void saveMetatest(MetaTestVO metatest) {
        CheckHelper.throwIfNull(metatest, "question");        
        Singletons.ds().save(metatest);
        putChangesMarker(null, CacheType.getActivePublications);
    }
    
    public void saveQuestion(QuestionVO question) {
        CheckHelper.throwIfNull(question, "question");        
        Singletons.ds().save(question);
        putGlobalChangesMarker();
    }

    public void setPublicationsForMetatest(String metatestId, List<PublicationVO> publications) {
        CheckHelper.throwIfNull(publications, "publications");        
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");
        
        Singletons.ds().save(publications);
        
        List<String> publicationsIds = VOHelper.getIds(publications);
        Query<PublicationVO> q = Singletons.ds().createQuery(PublicationVO.class);
        q.field("metatestId").equal(metatestId);
        q.field("_id").notIn(publicationsIds);
        Singletons.ds().delete(q);
        
        putChangesMarker(null, CacheType.getActivePublications);
    }
    
    public void savePublication(PublicationVO publication) {
        CheckHelper.throwIfNull(publication, "publication");        
        Singletons.ds().save(publication);
        putChangesMarker(null, CacheType.getActivePublications);
    }

    public void saveGroup(GroupVO group) {
        CheckHelper.throwIfNull(group, "group");        
        Singletons.ds().save(group);
        putGlobalChangesMarker();
    }
    
    public GroupVO getDeafultGroup() {
        return getById(GroupVO.class, LoginManager.getThreadLocalLoggedUser().getDefaultGroupId());
    }
    
    public void setGroups(List<GroupVO> groupsToSave) {
        CheckHelper.throwIfNullOrEmpty(groupsToSave, "groupsToSave");
        
        GroupVO defaultGroup = getDeafultGroup(); 
                
        List<GroupVO> groupsToDelete = loadAllGroups();
        
        List<String>  groupsToDeleteIds = VOHelper.getIds(groupsToDelete);
        List<String>  groupsToSaveIds = VOHelper.getIds(groupsToSave);
        
        groupsToDeleteIds.removeAll(groupsToSaveIds);
        groupsToDeleteIds.remove(defaultGroup.getId());
        
        for (String groupToDeleteId: groupsToDeleteIds) {
            // Switch questions to default group
            Query<QuestionVO> qQuestion = Singletons.ds().createQuery(QuestionVO.class);
            qQuestion.field("groupId").equal(groupToDeleteId);
            
            UpdateOperations<QuestionVO> uoQuestion = Singletons.ds().createUpdateOperations(QuestionVO.class);
            uoQuestion.set("groupId", defaultGroup.getId());
            uoQuestion.set("groupName", defaultGroup.getName());
            
            Singletons.ds().update(qQuestion, uoQuestion);
            
            //Delete metatest entries with deleted group            
            Query<MetaTestVO> qMetatest = Singletons.ds().createQuery(MetaTestVO.class);
            qMetatest.disableValidation().filter("entries.groupId", groupToDeleteId).enableValidation();
          
            MTEGroupVO mteTemplate = ReflectionHelper.createEmpty(MTEGroupVO.class);
            mteTemplate.setGroupId(groupToDeleteId);
            
            UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
            uoMetatest.removeAll("entries", mteTemplate);
          
            Singletons.ds().update(qMetatest, uoMetatest);
            
            // Delete group
            Query<GroupVO> qGroup = Singletons.ds().createQuery(GroupVO.class);
            qGroup.field("_id").equal(groupToDeleteId);
            Singletons.ds().delete(qGroup);
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
        putGlobalChangesMarker();
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
        
        putGlobalChangesMarker();
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
        
        Query<ReportVO> qReport = Singletons.ds().createQuery(ReportVO.class);
        qReport.field("_id").in(reportsIds);
        
        Singletons.ds().delete(qReport);
        
        putGlobalChangesMarker();
    }

    public void deletePublications(List<String> publicationIds) {
        CheckHelper.throwIfNull(publicationIds, "publicationIds");
        
        if (publicationIds.isEmpty()) {
            return;
        }
        
        Query<PublicationVO> q = Singletons.ds().createQuery(PublicationVO.class);
        q.field("_id").in(publicationIds);
        
        Singletons.ds().delete(q);
        
        putChangesMarker(null, CacheType.getActivePublications);
    }

    public void deleteMetatests(List<String> metatestIds) {
        CheckHelper.throwIfNull(metatestIds, "metatestIds");
        
        if (metatestIds.isEmpty()) {
            return;
        }

        Query<PublicationVO> qp = Singletons.ds().createQuery(PublicationVO.class);
        qp.field("metatestId").in(metatestIds);        
        Singletons.ds().delete(qp);
        
        Query<MetaTestVO> qm = Singletons.ds().createQuery(MetaTestVO.class);
        qm.field("_id").in(metatestIds);        
        Singletons.ds().delete(qm);
        
        putChangesMarker(null, CacheType.getActivePublications);
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
        putGlobalChangesMarker();
    }

    private List<? extends SortInfo> prepareReportSortInfo(List<? extends SortInfo> orders) {
    	List<SortInfo> result = new ArrayList<SortInfo>();
    	for (SortInfo si: orders) {
    		if ("fullName".equals(si.getSortField())) {
    			result.add(new SortInfoBean("lastName", si.getSortDir()));
    			result.add(new SortInfoBean("firstName", si.getSortDir()));
    			result.add(new SortInfoBean("middleName", si.getSortDir()));
    		}
    		else {
    			result.add(si);
    		}
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
                continue;
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
            
            if ("contains".equals(filter.getComparison())) {
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
            else if ("notIn".equals(filter.getComparison())) {
                q.field(filter.getField()).notIn(StringHelper.splitBySeparatorToList(filter.getValue(), ", "));
            }
        }
    }

    public <T extends BaseVO> T get(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        Query<T> q = Singletons.ds().createQuery(clazz);        
        q.field("_id").equal(id);
        
        T result = q.get();
        
        return result;
    }

    public <T extends BaseVO> T copy(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        T result = get(clazz, id);
        if (result != null) {
            result.resetIdentity();
        }
        
        return result;
    }
    
    
}
