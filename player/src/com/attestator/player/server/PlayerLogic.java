package com.attestator.player.server;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.server.CommonLogic;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.google.code.morphia.query.Query;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PlayerLogic extends CommonLogic{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PlayerLogic.class);
    
    public PublicationVO getActivePublication(String id) {
        CheckHelper.throwIfNullOrEmpty(id, "id");

        Query<PublicationVO> pq = Singletons.ds().createFetchQuery(PublicationVO.class);
        
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
        Query<PublicationVO> pq = Singletons.ds().createFetchQuery(PublicationVO.class);
        
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
        pq.order("metatestId, created");        
        List<PublicationVO> activePublications = pq.asList();
        
        return activePublications;
    }

    public List<ChangeMarkerVO> getChangesSince(Date time, String clientId) {
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");
        
        // If no time marker return all changes
        if (time == null) {
            return Arrays.asList(new ChangeMarkerVO(null, LoginManager.getThreadLocalTenatId(), null));
        }
        
        Query<ChangeMarkerVO> q = Singletons.ds().createFetchQuery(ChangeMarkerVO.class);
        q.field("created").greaterThan(time);
        
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
        Query<PublicationVO> qp = Singletons.ds().createFetchQuery(PublicationVO.class);
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
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);
        q.field("publication._id").equal(publicationId);
        q.field("clientId").equal(clientId);
        
        long result = q.countAll();
        return result;
    }

    public String getLatestFinishedReportId(String publicatioId, String clientId) {
        CheckHelper.throwIfNullOrEmpty(publicatioId, "publicatioId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);
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
    
    public List<QuestionVO> generateQuestionList(PublicationVO publication) {
        CheckHelper.throwIfNull(publication, "publication");
        
        if (publication.getMetatestId() == null) {
            throw new IllegalStateException("Publication " + publication.getId() + " have no metatestId");
        }
        
        MetaTestVO metatest = getById(MetaTestVO.class, publication.getMetatestId());
        if (metatest == null) {
            throw new IllegalStateException("Metatest with " + publication.getMetatestId() + " not found");
        }        
        
        List<QuestionVO> result = generateQuestionsList(metatest, publication.isThisRandomQuestionsOrder());
        
        return result;
    }
    
    public ReportVO getLastReportForRenew(String clientId, String publicationId) {
        CheckHelper.throwIfNullOrEmpty(publicationId, "publicationId");
        CheckHelper.throwIfNullOrEmpty(clientId, "clientId");        
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
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
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
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

        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
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
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
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
        
        Query<ReportVO> q = Singletons.ds().createFetchQuery(ReportVO.class);        
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
