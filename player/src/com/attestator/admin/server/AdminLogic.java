package com.attestator.admin.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.attestator.admin.server.helper.print.PrintHelper;
import com.attestator.admin.server.helper.print.PrintHelper.PrintingMedia;
import com.attestator.common.server.CommonLogic;
import com.attestator.common.server.helper.DatastoreHelper;
import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.ShareableVO;
import com.attestator.common.shared.vo.SharingEntryVO;
import com.attestator.common.shared.vo.TenantableCronTaskVO;
import com.attestator.common.shared.vo.UpdateAllMetattestsVisiblityLockVO;
import com.attestator.common.shared.vo.UpdateMetatestsSharingTaskVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.mongodb.WriteResult;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class AdminLogic extends CommonLogic {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AdminLogic.class);
    
    public List<GroupVO> loadAllGroups() {
        Query<GroupVO> q = Singletons.ds().createFetchQuery(GroupVO.class).order("name, _id");
        List<GroupVO> result = q.asList();
        
        putDefaultGroupOnTop(result);
        
        return result;
    }
    
    public List<GroupVO> loadOwnGroups() {
        Query<GroupVO> q = Singletons.ds().createFetchQuery(GroupVO.class).order("name, _id");
        q.field("tenantId").equal(LoginManager.getThreadLocalTenantId());
        List<GroupVO> result = q.asList();
        
        putDefaultGroupOnTop(result);
        
        return result;
    }
    
    private void putDefaultGroupOnTop(List<GroupVO> result) {
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
    }    
    
    
    public <T> PagingLoadResult<T> loadPage(Class<T> clazz, FilterPagingLoadConfig loadConfig, String ... excludFields) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        
        // Create query
        Query<T> q = Singletons.ds().createFetchQuery(clazz);
        
        // Exclude fields if any
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }
    
        // Add load config
        DatastoreHelper.addLoadConfig(q, loadConfig);

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
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);
        
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }
        
        // Add filters
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getFilters())) {
            DatastoreHelper.addFilters(q, loadConfig.getFilters());
        }        
        
        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
        	List<? extends SortInfo> sortInfo = prepareReportSortInfo(loadConfig.getSortInfo());
        	DatastoreHelper.addOrders(q, sortInfo);
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
    
    public PrintingPropertiesVO getPrintPropertiesByMetatestId(String metatestId) {
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");
        
        Query<PrintingPropertiesVO> q = Singletons.ds().createFetchQuery(PrintingPropertiesVO.class);
        q.field("metatestId").equal(metatestId);
        
        PrintingPropertiesVO result = q.get();
        
        if (result == null) {
             Query<MetaTestVO> qm = Singletons.ds().createFetchQuery(MetaTestVO.class);
             qm.field("_id").equal(metatestId);
             qm.retrievedFields(false, "entries");
             MetaTestVO metatest = qm.get();
             if (metatest != null) {
                 result = new PrintingPropertiesVO();
                 result.setMetatestId(metatestId);
                 result.setMetatest(metatest);
             }
        }
        
        return result;
    }
    
    public List<PublicationVO> loadPublicationsByMetatestId(String metatestId, ListLoadConfig config) {
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");

        Query<PublicationVO> q = Singletons.ds().createFetchQuery(PublicationVO.class);
        q.field("metatestId").equal(metatestId);
        
        if (config != null &&  !NullHelper.nullSafeIsEmpty(config.getSortInfo())) {
            DatastoreHelper.addOrders(q, config.getSortInfo());
        }
        else {
            DatastoreHelper.addDefaultOrder(PublicationVO.class, q);
        }
        
        List<PublicationVO> result = q.asList();        
        
        return result;
    }

    public ReportVO loadReport(String reportId) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        
        ReportVO result = q.get();
        return result;
    }

    public MetaTestVO loadMetatest(String metatestId) {
        CheckHelper.throwIfNullOrEmpty(metatestId, "metatestId");
        
        Query<MetaTestVO> q = Singletons.ds().createFetchQuery(MetaTestVO.class);        
        q.field("_id").equal(metatestId);
        
        MetaTestVO result = q.get();
        return result;
    }

    public List<MetaTestVO> loadAllMetaTests(String ... excludFields) {
        Query<MetaTestVO> q = Singletons.ds().createFetchQuery(MetaTestVO.class);
        if (excludFields != null) {
            q.retrievedFields(false, excludFields);
        }        
        DatastoreHelper.addDefaultOrder(MetaTestVO.class, q);        
        List<MetaTestVO> qRes = q.asList();        
        return qRes;
    }
    
    public List<PublicationVO> loadAllPublications() {
        Query<PublicationVO> q = Singletons.ds().createFetchQuery(PublicationVO.class);
        DatastoreHelper.addDefaultOrder(PublicationVO.class, q);
        List<PublicationVO> qRes = q.asList();        
        return qRes;
    }
    
    public void savePrintigProperties(PrintingPropertiesVO properties) {
        CheckHelper.throwIfNull(properties, "properties");
        properties.setPrintAttempt(properties.getPrintAttemptOrZero() + 1);
        Singletons.ds().save(properties);
    }
    
    private void refreshAllSharing() {
        Date now = new Date();        
        updateAllMetatestsVisibilityOnDate(now);
        scheduleAllMetatestsSharingTasksAfter(now);
    }
    
    public void saveMetatestSharingEntries(String metatestId, List<SharingEntryVO> sharingEntries) {
        CheckHelper.throwIfNull(metatestId, "metatestId");        
        CheckHelper.throwIfNull(sharingEntries, "sharingEntries");
        
        Query<MetaTestVO> q = Singletons.ds().createUpdateQuery(MetaTestVO.class);
        q.field("_id").equal(metatestId);
        
        UpdateOperations<MetaTestVO> uo = Singletons.ds().createUpdateOperations(MetaTestVO.class);
        uo.set("sharingEntries", sharingEntries);
        
        Singletons.ds().update(q, uo);
        refreshAllSharing();
        
        putGlobalChangesMarker();
    }
    
    public void saveMetatest(MetaTestVO metatest) {
        CheckHelper.throwIfNull(metatest, "metatest");        
        Singletons.ds().save(metatest);
        refreshAllSharing();        
        putGlobalChangesMarker();
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
        Query<PublicationVO> q = Singletons.ds().createUpdateQuery(PublicationVO.class);
        q.field("metatestId").equal(metatestId);
        if (!NullHelper.isEmptyOrNull(publicationsIds)) {
            q.field("_id").notIn(publicationsIds);
        }
        Singletons.ds().delete(q);
        
        putGlobalChangesMarker();
    }
    
    public void savePublication(PublicationVO publication) {
        CheckHelper.throwIfNull(publication, "publication");        
        Singletons.ds().save(publication);
        putGlobalChangesMarker();
    }

    public void saveGroup(GroupVO group) {
        CheckHelper.throwIfNull(group, "group");        
        Singletons.ds().save(group);
        putGlobalChangesMarker();
    }
    
    public void setGroups(List<GroupVO> groupsToSave) {
        CheckHelper.throwIfNullOrEmpty(groupsToSave, "groupsToSave");
        
        GroupVO defaultGroup = getDeafultGroup(); 
                
        List<GroupVO> groupsToDelete = loadOwnGroups();
        
        List<String>  groupsToDeleteIds = VOHelper.getIds(groupsToDelete);
        List<String>  groupsToSaveIds = VOHelper.getIds(groupsToSave);
        
        groupsToDeleteIds.removeAll(groupsToSaveIds);
        groupsToDeleteIds.remove(defaultGroup.getId());
        
        for (String groupToDeleteId: groupsToDeleteIds) {
            // Switch questions to default group
            Query<QuestionVO> qQuestion = Singletons.ds().createUpdateQuery(QuestionVO.class);
            qQuestion.field("groupId").equal(groupToDeleteId);
            
            UpdateOperations<QuestionVO> uoQuestion = Singletons.ds().createUpdateOperations(QuestionVO.class);
            uoQuestion.set("groupId", defaultGroup.getId());
            uoQuestion.set("groupName", defaultGroup.getName());
            
            Singletons.ds().update(qQuestion, uoQuestion);
            
            //Delete metatest entries with deleted group            
            Query<MetaTestVO> qMetatest = Singletons.ds().createUpdateQuery(MetaTestVO.class);
            qMetatest.disableValidation().filter("entries.groupId", groupToDeleteId).enableValidation();
          
            MTEGroupVO mteTemplate = ReflectionHelper.createEmpty(MTEGroupVO.class);
            mteTemplate.setGroupId(groupToDeleteId);
            
            UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
            uoMetatest.removeAll("entries", mteTemplate);
          
            Singletons.ds().update(qMetatest, uoMetatest);
            
            // Delete group
            Query<GroupVO> qGroup = Singletons.ds().createUpdateQuery(GroupVO.class);
            qGroup.field("_id").equal(groupToDeleteId);
            Singletons.ds().delete(qGroup);
        }
        
        for (GroupVO groupToSave: groupsToSave) {
            // Switch questions to new group name
            Query<QuestionVO> qQuestion = Singletons.ds().createUpdateQuery(QuestionVO.class);
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
        
        Query<GroupVO> qGroup = Singletons.ds().createFetchQuery(GroupVO.class);
        qGroup.field("_id").equal(groupId);
        GroupVO group = qGroup.get();
        if (group == null) {
            return;
        }
                
        Query<QuestionVO> q = Singletons.ds().createUpdateQuery(QuestionVO.class);
        q.field("_id").in(questionIds);
        
        UpdateOperations<QuestionVO> uo = Singletons.ds().createUpdateOperations(QuestionVO.class);
        uo.set("groupId", group.getId());
        uo.set("groupName", group.getName());
        
        Singletons.ds().update(q, uo);
        
        putGlobalChangesMarker();
    }

    public GroupVO getDeafultGroup() {
        return getById(GroupVO.class, LoginManager.getThreadLocalLoggedUser().getDefaultGroupId());
    }
    
    public void deleteReports(List<String> reportsIds) {
        CheckHelper.throwIfNull(reportsIds, "reportsIds");
        
        if (reportsIds.isEmpty()) {
            return;
        }
        
        Query<ReportVO> qReport = Singletons.ds().createUpdateQuery(ReportVO.class);
        qReport.field("_id").in(reportsIds);
        
        Singletons.ds().delete(qReport);
        
        putGlobalChangesMarker();
    }

    public void deletePublications(List<String> publicationIds) {
        CheckHelper.throwIfNull(publicationIds, "publicationIds");
        
        if (publicationIds.isEmpty()) {
            return;
        }
        
        Query<PublicationVO> q = Singletons.ds().createUpdateQuery(PublicationVO.class);
        q.field("_id").in(publicationIds);
        
        Singletons.ds().delete(q);
        
        putChangesMarker(null, CacheType.getActivePublications);
    }

    public void deleteMetatests(List<String> metatestIds) {
        CheckHelper.throwIfNull(metatestIds, "metatestIds");
        
        if (metatestIds.isEmpty()) {
            return;
        }
        
        for (String metatestId: metatestIds) {
            Query<MetaTestVO> qm = Singletons.ds().createUpdateQuery(MetaTestVO.class);
            qm.field("_id").equal(metatestId);        
            WriteResult wr = Singletons.ds().delete(qm);
            if (wr.getN() == 1) {
                Singletons.sl().deletePublicationsForMetatest(metatestId);
                Singletons.sl().deletePritingPropertiesForMetatest(metatestId);   
            }
        }
        
        refreshAllSharing();        
        putChangesMarker(null, CacheType.getActivePublications);
    }
    
    public void deleteQuestions(List<String> questionIds) {
        CheckHelper.throwIfNull(questionIds, "questionIds");
        
        if (questionIds.isEmpty()) {
            return;
        }
        
        // Remove all metatest entries referenced to this question
        for (String questionId: questionIds) {
            Query<MetaTestVO> qMetatest = Singletons.ds().createUpdateQuery(MetaTestVO.class);
            qMetatest.disableValidation().field("entries.questionId").hasAnyOf(questionIds).enableValidation();        
            
            UpdateOperations<MetaTestVO> uoMetatest = Singletons.ds().createUpdateOperations(MetaTestVO.class);
            MTEQuestionVO mteTemplate = ReflectionHelper.createEmpty(MTEQuestionVO.class);
            mteTemplate.setQuestionId(questionId);
            uoMetatest.removeAll("entries", mteTemplate);
            
            Singletons.ds().update(qMetatest, uoMetatest);
        }
    
        // Remove questions
        Query<QuestionVO> qQuestion = Singletons.ds().createUpdateQuery(QuestionVO.class);
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

    public <T extends BaseVO> T copy(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        T result = getById(clazz, id);
        if (result != null) {
            result.resetIdentity();
        }
        
        return result;
    }
    
    public String getHtmlForPrinting(String printingPropertiesId) {
        CheckHelper.throwIfNullOrEmpty(printingPropertiesId, "printingPropertiesId");
        
        PrintingPropertiesVO properties = getById(PrintingPropertiesVO.class, printingPropertiesId);
        MetaTestVO metatest = getById(MetaTestVO.class, properties.getMetatestId());
        String result = PrintHelper.printTest(metatest, properties, PrintingMedia.paper);
        
        return result;
    }
    
    private Set<String> getMetatestQuestionsIds(MetaTestVO metatest) {
        CheckHelper.throwIfNull(metatest, "metatest");
        CheckHelper.throwIfNull(metatest.getEntries(), "metatest.getEntries()");
        
        Set<String> result = new HashSet<String>();
        for (MetaTestEntryVO entry: metatest.getEntries()) {
            if (entry instanceof MTEQuestionVO) {
                result.add(((MTEQuestionVO) entry).getQuestionId());
            }
            else if (entry instanceof MTEGroupVO) {
                List<String> groupQuestions = getQuestionIdsByGroupId(((MTEGroupVO) entry).getGroupId());
                result.addAll(groupQuestions);
            }
        }
        
        return result;
    }
    
    private Set<String> getMetatestGroupIds(MetaTestVO metatest) {
        CheckHelper.throwIfNull(metatest, "metatest");
        CheckHelper.throwIfNull(metatest.getEntries(), "metatest.getEntries()");
        
        Set<String> result = new HashSet<String>();
        for (MetaTestEntryVO entry: metatest.getEntries()) {
            if (entry instanceof MTEQuestionVO) {
                result.add(((MTEQuestionVO) entry).getQuestion().getGroupId());
            }
            else if (entry instanceof MTEGroupVO) {
                result.add(((MTEGroupVO) entry).getGroupId());
            }
        }
        
        return result;
    }

    
    private Set<String> getMetatestSharedOnDateTenantIds(MetaTestVO metatest, Date date) {
        CheckHelper.throwIfNull(date, "date");
        CheckHelper.throwIfNull(metatest, "metatest");
        CheckHelper.throwIfNull(metatest.getSharingEntries(), "metatest.getSharingEntries()");
        
        Set<String> result = new HashSet<String>();
        for (SharingEntryVO entry: metatest.getSharingEntries()) {
            if (DateHelper.beforeOrNull(entry.getStartTime(), date)
            &&  DateHelper.afterOrNull(entry.getEndTime(), date)) {
                result.add(entry.getTenantId());
            }
        }
        
        return result;
    }
    
    private <T extends ShareableVO> void resetSharingForObjects(Class<T> clazz) {
        resetSharingForObjects(clazz, null);
    }
    
    private <T extends ShareableVO> void resetSharingForObjects(Class<T> clazz, Collection<String> ids) {
        Query<T> q = Singletons.ds().createUpdateQuery(clazz);
        if (!NullHelper.isEmptyOrNull(ids)) {
            q.field("_id").in(ids);
        }
        
        UpdateOperations<T> uo = Singletons.ds().createUpdateOperations(clazz);
        uo.set("sharedForTenantIds", new HashSet<String>(Arrays.asList(LoginManager.getThreadLocalTenantId())));
        
        Singletons.ds().update(q, uo);
    }
    
    private <T extends ShareableVO> void addSharingForObjects(Class<T> clazz, Collection<String> ids, Collection<String> tenantIds) {
        if (NullHelper.isEmptyOrNull(tenantIds)) {
            return;
        }
        
        Query<T> q = Singletons.ds().createUpdateQuery(clazz);
        if (!NullHelper.isEmptyOrNull(ids)) {
            q.field("_id").in(ids);
        }
        
        UpdateOperations<T> uo = Singletons.ds().createUpdateOperations(clazz);
        uo.addAll("sharedForTenantIds", new ArrayList<String>(tenantIds), false);
        
        Singletons.ds().update(q, uo);
    }
    
    public void updateAllMetatestsVisibilityOnDate(Date date) {
        UpdateAllMetattestsVisiblityLockVO lock = new UpdateAllMetattestsVisiblityLockVO(LoginManager.getThreadLocalTenantId());
        try {
            LockManager.lockBlocking(lock);
            
            resetSharingForObjects(MetaTestVO.class);
            resetSharingForObjects(GroupVO.class);
            resetSharingForObjects(QuestionVO.class);
            
            List<MetaTestVO> metatests = loadAllMetaTests();
            for (MetaTestVO metatest: metatests) {
                Set<String> questionIds = getMetatestQuestionsIds(metatest);
                Set<String> groupIds = getMetatestGroupIds(metatest);
                Set<String> sharedNowTenantIds = getMetatestSharedOnDateTenantIds(metatest, date);
                
                addSharingForObjects(MetaTestVO.class, Arrays.asList(metatest.getId()), sharedNowTenantIds);
                addSharingForObjects(QuestionVO.class, questionIds, sharedNowTenantIds);
                addSharingForObjects(GroupVO.class, groupIds, sharedNowTenantIds);
            }
        }
        finally {
            LockManager.releaseLock(lock);
        }
    }
    
    public void scheduleAllMetatestsSharingTasksAfter(Date theDate) {                
        CheckHelper.throwIfNull(theDate, "theDate");
        
        List<Date> dates = new ArrayList<Date>();
        List<MetaTestVO> metatests = loadAllMetaTests("entries");        
        for (MetaTestVO metatest: metatests) {
            for (SharingEntryVO entry: metatest.getSharingEntries()) {
                if (entry.getStartTime() != null) {
                    dates.add(entry.getStartTime());
                }
                if (entry.getEndTime() != null) {
                    dates.add(entry.getEndTime());
                }
            }
        }
        
        List<UpdateMetatestsSharingTaskVO> oldCronTasks = getAllUpdateMetatestsSharingTasks();
        for (UpdateMetatestsSharingTaskVO oldCronTask: oldCronTasks) {
            removeCronTask(oldCronTask);
        }
        
        for (Date date: dates) {
            if (date.after(theDate)) {
                UpdateMetatestsSharingTaskVO newCronTask = new UpdateMetatestsSharingTaskVO(date);
                scheduleCronTask(newCronTask);
            }
        }
    }
    
    public void scheduleCronTask(TenantableCronTaskVO cronTask) {
        Singletons.ds().save(cronTask);
    }
    
    public List<UpdateMetatestsSharingTaskVO> getAllUpdateMetatestsSharingTasks() {
        Query<UpdateMetatestsSharingTaskVO> q = Singletons.ds().createFetchQuery(UpdateMetatestsSharingTaskVO.class);
        q.disableValidation().field("className").equal(UpdateMetatestsSharingTaskVO.class.getName()).enableValidation();
        List<UpdateMetatestsSharingTaskVO> result = q.asList();
        return result;
    }
    
    public void removeCronTask(TenantableCronTaskVO cronTask) {
        Singletons.ds().delete(cronTask);
    }
    
    public Boolean isThisLoggedUserPassword(String password) {
        if (StringHelper.isEmptyOrNull(password)) {
            return false;
        }
        else {
            return password.equals(LoginManager.getThreadLocalLoggedUser().getPassword());
        }
    }
    
    public void updateLoggedUser(String oldPassword, String email, String newPassword) {        
        Query<UserVO> q = Singletons.ds().createUpdateQuery(UserVO.class);
        q.field("_id").equal(LoginManager.getThreadLocalLoggedUser().getId());
        
        if (!StringHelper.isEmptyOrNull(newPassword)) {
            q.field("password").equal(oldPassword);
        }
        
        UpdateOperations<UserVO> uo = Singletons.ds().createUpdateOperations(UserVO.class);        
        boolean updateNeeded = false;        
        if (!StringHelper.isEmptyOrNull(email)) {
            uo.set("email", email);
            updateNeeded = true;
        }        
        if (!StringHelper.isEmptyOrNull(newPassword)) {
            uo.set("password", newPassword);
            updateNeeded = true;
        }
        
        if (updateNeeded) {
            Singletons.ds().update(q, uo);
        }
    }
}
