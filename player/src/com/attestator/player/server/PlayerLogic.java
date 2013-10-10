package com.attestator.player.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.server.CommonLogic;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.google.code.morphia.query.Query;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PlayerLogic extends CommonLogic{
    private static final Logger logger = Logger.getLogger(PlayerLogic.class);
    
    public List<String> getQuestionIdsByGroupId(String groupId) {
        CheckHelper.throwIfNullOrEmpty(groupId, "groupId");        
        
        List<String>      result = new ArrayList<String>();
        Query<QuestionVO> q      = Singletons.ds().createQuery(QuestionVO.class);
        
        if (groupId != null) {
            q.field("groupId").equal(groupId);
        }
        else {
            q.field("groupId").doesNotExist();
        }
        
        List<QuestionVO> questions = q.asList();
        for (QuestionVO question: questions) {
            result.add(question.getId());
        }
        
        return result;
    }
    
    public PublicationVO getActivePublication(String id) {
        CheckHelper.throwIfNullOrEmpty(id, "id");

        Query<PublicationVO> pq = Singletons.ds().createQuery(PublicationVO.class);
        
        Date now = new Date();
        pq.and(
                pq.or(
                        pq.criteria("start").doesNotExist(), 
                        pq.criteria("start").lessThan(now)
                    ),
                pq.or(
                        pq.criteria("end").doesNotExist(), 
                        pq.criteria("end").greaterThanOrEq(now)
                    ),
                pq.criteria("_id").equal(id)
        );
        PublicationVO activePublication = pq.get();
        
        return activePublication;        
    }
    
    public List<PublicationVO> getActivePublications() {
        Query<PublicationVO> pq = Singletons.ds().createQuery(PublicationVO.class);
        
        Date now = new Date();
        
        pq.and(
                pq.or(
                        pq.criteria("start").doesNotExist(), 
                        pq.criteria("start").lessThan(now)
                ), 
                pq.or(
                        pq.criteria("end").doesNotExist(), 
                        pq.criteria("end").greaterThanOrEq(now)
                )
        );
        
        List<PublicationVO> activePublications = pq.asList();
        
        return activePublications;
    }

    @Override
    public <T extends BaseVO> T getById(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        Query<T> q = Singletons.ds().createQuery(clazz);
        q.field("_id").equal(id);
        T result = q.get();
        return result;
    }
    
    public List<ChangeMarkerVO> getChangesSince(Date time, String clientId) {
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");
        
        // If no time marker return all changes
        if (time == null) {
            return Arrays.asList(new ChangeMarkerVO(null, LoginManager.getThreadLocalTenatId(), null));
        }
        
        Query<ChangeMarkerVO> q = Singletons.ds().createQuery(ChangeMarkerVO.class);
        if (time != null) { 
            q.field("created").greaterThan(time);
        }
        
        // Look for changes
        q.or(
            q.criteria("clientId").doesNotExist(),
            q.criteria("clientId").equal(clientId)
        );     
        q.order("created");
        
        List<ChangeMarkerVO> allChanges = q.asList();
        
        // If global change found return only them
        ChangeMarkerVO globalChange = Iterables.find(allChanges, new Predicate<ChangeMarkerVO>() {
            @Override
            public boolean apply(ChangeMarkerVO marker) {
                return marker.isGlobal();
            }
        }, null);        
        
        if (globalChange != null) {
            return Arrays.asList(globalChange);
        }
        
        // Is some publications expired or became active since last query
        Date now = new Date();
        Query<PublicationVO> qp = Singletons.ds().createQuery(PublicationVO.class);
        qp.or(
            qp.criteria("start").greaterThan(time).criteria("start").lessThanOrEq(now),
            qp.criteria("end").greaterThan(time).criteria("end").lessThanOrEq(now)
        );
        
        if (qp.countAll() > 0) {
            ChangeMarkerVO activePublicationsMarker = new ChangeMarkerVO(null, LoginManager.getThreadLocalTenatId(), CacheType.getActivePublications);            
            allChanges.add(activePublicationsMarker);
        }
        
        return allChanges;
    }
    
    public long getNumberOfAttempts(String publicationId, String clientId) {
        CheckHelper.throwIfNullOrEmpty(publicationId, "publicatioId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);
        q.field("publication._id").equal(publicationId);
        q.field("clientId").equal(clientId);
        
        long result = q.countAll();
        return result;
    }

    public String getLatestFinishedReportId(String publicatioId, String clientId) {
        CheckHelper.throwIfNullOrEmpty(publicatioId, "publicatioId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);
        q.field("publication._id").equal(publicatioId);
        q.field("clientId").equal(clientId);
        q.field("finished").equal(true);
        q.order("-start");
        q.retrievedFields(true, "_id");
        
        ReportVO report = q.get();

        if (report != null) {
            return report.getId();
        }
        else {
            return null;
        }
    }
    
    private void prepare(List<QuestionVO> questions, PublicationVO publication) {
        for (QuestionVO question: questions) {
            prepare(question);
        }
        
        if (publication.isThisRandomQuestionsOrder()) {
            Collections.shuffle(questions);
        }
    }

    private void prepare(QuestionVO question) {
        if (question instanceof SingleChoiceQuestionVO) {
            if (((SingleChoiceQuestionVO) question).isThisRandomChoiceOrder()) {
                Collections.shuffle(((SingleChoiceQuestionVO) question).getChoices());
            }
        }
    }

    public List<QuestionVO> getQuestions(PublicationVO publication) {
        CheckHelper.throwIfNull(publication, "publication");
        
        if (publication.getMetatestId() == null) {
            throw new IllegalStateException("Publication " + publication.getId() + " have no metatestId");
        }
        
        MetaTestVO metatest = getById(MetaTestVO.class, publication.getMetatestId());
        if (metatest == null) {
            throw new IllegalStateException("Metatest with " + publication.getMetatestId() + " not found");
        }        
        
        List<QuestionVO> result = new ArrayList<QuestionVO>();
        
        // Prepare question by group mapping
        Map<String, List<String>> questionsIdsByGroup = new HashMap<String, List<String>>();
        for (MetaTestEntryVO mte: metatest.getEntries()) {
            if (mte instanceof MTEGroupVO) {
                String mteGroupId = ((MTEGroupVO) mte).getGroupId();
                if (questionsIdsByGroup.get(mteGroupId) == null) {
                    List<String> mteGroupQuestions = getQuestionIdsByGroupId(mteGroupId);
                    Collections.shuffle(mteGroupQuestions);
                    questionsIdsByGroup.put(mteGroupId, mteGroupQuestions);
                }
            }
        }
        
        // Fill questions in the test
        for (MetaTestEntryVO mte: metatest.getEntries()) {
            if (mte instanceof MTEQuestionVO) {
                String mteQuestionId = ((MTEQuestionVO) mte).getQuestionId();
                if (mteQuestionId == null) {
                    continue;
                }
                //TODO do we allow question duplication if user add one question to test several times
//                if (testQuestionIds.contains(mteQuestionId)) {
//                    continue;
//                }
                
                QuestionVO question = getById(QuestionVO.class, mteQuestionId); 
                if (question == null) {
                    continue;
                }                
                
                // Remove question from available group source
                if (question.getGroupId() != null) {
                    List<String> availableGroupQuestions = questionsIdsByGroup.get(question.getGroupId());
                    if (availableGroupQuestions != null) {
                        availableGroupQuestions.remove(question.getId());
                    }
                }
                
                result.add(question);
            } 
            else if (mte instanceof MTEGroupVO) {
                MTEGroupVO      mteGroup   = ((MTEGroupVO) mte);
                List<String>    availableGroupQuestions = questionsIdsByGroup.get(mteGroup.getGroupId());
                
                int numOfQuestionsToAdd = 0;
                if (mteGroup.getNumberOfQuestions() != null) {
                    numOfQuestionsToAdd = Math.min(mteGroup.getNumberOfQuestions(), availableGroupQuestions.size()); 
                }
                else {
                    numOfQuestionsToAdd = availableGroupQuestions.size();
                }
                        
                for (int i = numOfQuestionsToAdd - 1; i >= 0; i--) {
                    String     questionId  = availableGroupQuestions.remove(i);                    
                    QuestionVO question    = getById(QuestionVO.class, questionId);
                    
                    result.add(question);
                }   
            }
            else {
                logger.warn("Unsupported metatest entry " + mte.getClass().getName() + " skip for now.");
            }
        }
        
        // Shuffle questions, choices etc
        prepare(result, publication);
        
        return result;
    }
    
    public ReportVO getLastReportForRenew(String clientId, String publicationId) {
        CheckHelper.throwIfNullOrEmpty(publicationId, "publicationId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("publication._id").equal(publicationId);
        q.field("clientId").equal(clientId);        
        q.order("-start");
        
        ReportVO result = q.get();
        
        if (result != null) {
            if (ReportHelper.isRenewAllowed(result)) {
                return result;
            }
        }
        
        return null;
    }
    
    public ReportVO getReport(String reportId, String clientId) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        q.field("clientId").equal(clientId);        
        
        ReportVO result = q.get();
        return result;
    }
    
    public void startReport(ReportVO report, Date start, String clientId, String host) {
        CheckHelper.throwIfNull(report, "report");        
        CheckHelper.throwIfNull(start, "start");        
        CheckHelper.throwIfNull(report.getPublication(), "report.publication");        
        CheckHelper.throwIfNull(report.getPublication().getMetatest(), "report.publication.metatest");        
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");

        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(report.getId());
        long count = q.countAll();
        if (count > 0) {
            return;
        }
        
        report.setMetatestName(report.getPublication().getMetatest().getName());
        report.setClientId(clientId);
        report.setHost(host);
        report.setStart(start);
        
        ReportHelper.updateReportStats(report);
        
        Singletons.ds().save(report);
    }

    public void addAnswer(String reportId, String clientId, AnswerVO answer) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        CheckHelper.throwIfNull(answer, "answer");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        q.field("clientId").equal(clientId);
        q.field("finished").notEqual(true);
        
        ReportVO report = q.get();
        
        if (report == null) {
            return;
        }
        
        // No such question in test
        if (report.getQuestion(answer.getQuestionId()) == null) {
            return;
        }

        // This question already answered
        if (report.getAnswerByQuestionId(answer.getQuestionId()) != null) {
            return;
        }
        
        report.getAnswers().add(answer);
        report.setEnd(new Date());        
        ReportHelper.updateReportStats(report);
        
        Singletons.ds().save(report);        
    }
    
    public void finishReport(String reportId, String clientId, Date end, InterruptionCauseEnum interruptionCause) {
        CheckHelper.throwIfNullOrEmpty(reportId, "reportId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");
        CheckHelper.throwIfNull(end, "end");
        
        Query<ReportVO> q = Singletons.ds().createQuery(ReportVO.class);        
        q.field("_id").equal(reportId);
        q.field("clientId").equal(clientId);
        q.field("finished").notEqual(true);

        ReportVO report = q.get();
        
        if (report == null) {
            return;
        }
        
        if (report.isThisFinished()) {
            return;
        }
        
        report.setEnd(end);
        report.setFinished(true);
        report.setInterruptionCause(interruptionCause);
        ReportHelper.updateReportStats(report);
        
        Singletons.ds().save(report);
        putChangesMarker(clientId, CacheType.getActivePublications);
    }
}
