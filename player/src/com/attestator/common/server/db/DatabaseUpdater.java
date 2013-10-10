package com.attestator.common.server.db;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.AdditionalQuestionVO.AnswerTypeEnum;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.DBVersionVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;

public class DatabaseUpdater {
    private static Logger logger = Logger.getLogger(DatabaseUpdater.class);
    
    public static final int DB_VERSION = 23;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void updateDatabase() {
        DBVersionVO version = Singletons.rawDs().createQuery(DBVersionVO.class).get();
        
        if (version == null) {
            version = new DBVersionVO(0);  
        }
        
        logger.info("Current database version " + version.getVersion());
        if (version.getVersion() < DB_VERSION) {
            logger.info("Updating to " + DB_VERSION);
        }
        
        if (version.getVersion() < 1) {
            addTestData();
        }
        
        if (version.getVersion() < 2) {
            Query<ChangeMarkerVO> q = Singletons.rawDs().createQuery(ChangeMarkerVO.class);
            Singletons.rawDs().delete(q);
        }
        
        if (version.getVersion() < 10) {
            Query<PublicationVO> q = Singletons.rawDs().createQuery(PublicationVO.class);
            q.disableValidation().field("additionalQuestions.checkValue").exists();
            UpdateOperations<PublicationVO> uo = Singletons.rawDs().createUpdateOperations(PublicationVO.class);
            uo.disableValidation().set("additionalQuestions.$.answerType", AnswerTypeEnum.key).enableValidation();            
            Singletons.rawDs().update(q, uo);
            
            q = Singletons.rawDs().createQuery(PublicationVO.class);
            q.disableValidation().field("additionalQuestions._id").exists().field("additionalQuestions.checkValue").doesNotExist();
            uo = Singletons.rawDs().createUpdateOperations(PublicationVO.class);
            uo.disableValidation().set("additionalQuestions.$.answerType", AnswerTypeEnum.text).enableValidation();            
            Singletons.rawDs().update(q, uo);
        }

        if (version.getVersion() < 11) {
            Singletons.rawDs().ensureIndexes();
        }
        
        if (version.getVersion() < 21) {
            Date now = new Date();
            try {
                Set<Class<? extends ModificationDateAwareVO>> modificationAwareClasses = 
                        (new ClassesInPackageScanner()).findSubclasses("com.attestator.common.shared.vo", ModificationDateAwareVO.class);
                for (Class<? extends ModificationDateAwareVO> clazz: modificationAwareClasses) {
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    
                    if (clazz.getAnnotation(Entity.class) == null) {
                        continue;
                    }
                    
                    Query q = Singletons.rawDs().createQuery(clazz);
                    UpdateOperations uo = Singletons.rawDs().createUpdateOperations(clazz);
                    uo.set("modified", now);
                    uo.set("created", now);
                    
                    Singletons.rawDs().update(q, uo);
                }
            } catch (IOException e) {
            }

            Query<ReportVO> q = Singletons.rawDs().createQuery(ReportVO.class);
            UpdateOperations<ReportVO> uo = Singletons.rawDs().createUpdateOperations(ReportVO.class);
            uo.set("publication.created", now);
            Singletons.rawDs().update(q, uo);            
            
            q = Singletons.rawDs().createQuery(ReportVO.class);
            uo = Singletons.rawDs().createUpdateOperations(ReportVO.class);
            uo.set("publication.modified", now);
            Singletons.rawDs().update(q, uo);                        

            Query<ReportVO> qr = Singletons.rawDs().createQuery(ReportVO.class);
            for (ReportVO report: qr.asList()) {
                for (QuestionVO question: report.getQuestions()) {
                    question.setCreated(now);
                    question.setModified(now);
                }
                
                for (AdditionalQuestionVO additionalQuestion: report.getPublication().getAdditionalQuestions()) {
                    if (additionalQuestion.getCheckValue() == null) {
                        additionalQuestion.setAnswerType(AnswerTypeEnum.text);
                    }
                    else {
                        additionalQuestion.setAnswerType(AnswerTypeEnum.key);
                    }
                }
                
                Singletons.rawDs().save(report);
            }
        }

        if (version.getVersion() < 22) {
            loadAndSave(MetaTestVO.class);
        }
        
        if (version.getVersion() < DB_VERSION) {
            resetChangeMarkers();
            
            version.setVersion(DB_VERSION);
            Singletons.rawDs().save(version);
        }
        
        logger.info("Database is up to date");
    }
    
    private static void resetChangeMarkers() {
        Query<ChangeMarkerVO> q = Singletons.rawDs().createQuery(ChangeMarkerVO.class);            
        Singletons.rawDs().delete(q);
        
        Query<UserVO> qu = Singletons.rawDs().createQuery(UserVO.class);        
        for (UserVO user: qu.asList()) {
            ChangeMarkerVO cm = new ChangeMarkerVO(null, user.getTenantId(), null);
            Singletons.rawDs().save(cm);
        }
    }
    
    private static <T> void loadAndSave(Class<T> clazz) {
        Query<T> q = Singletons.rawDs().createQuery(clazz);
        for (T obj: q.asList()) {
            Singletons.rawDs().save(obj);
        }
    }
    
    private static void addTestData() {
        Singletons.al().createNewUser("test2@test.com", "test");
        
        UserVO user = Singletons.al().createNewUser("test@test.com", "test");        
        LoginManager.setThreadLocalLoggedUser(user);
        
        GroupVO grOther = new GroupVO();
        grOther.setName("Другая");
        Singletons.ds().save(grOther);

        GroupVO grSign = new GroupVO();
        grSign.setName("Знаки");
        Singletons.ds().save(grSign);

        SingleChoiceQuestionVO scq1 = new SingleChoiceQuestionVO();
        scq1.setText("Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? Что это за знак? ");
//        scq1.setMaxQuestionAnswerTime(1l * 60 * 1000);
        
        ChoiceVO ch = new ChoiceVO();
        ch.setText("Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак Хороший знак");
        ch.setRight(true);
        scq1.getChoices().add(ch);
        
        ch = new ChoiceVO();
        ch.setText("Плохой знак");
        scq1.getChoices().add(ch);
        
        
        Singletons.ds().save(scq1);

        SingleChoiceQuestionVO scq2 = new SingleChoiceQuestionVO();
        scq2.setGroup(grSign);
        scq2.setText("Что это за светофор?");
        
        ch = new ChoiceVO();
        ch.setText("Хороший светофор");
        ch.setRight(true);
        scq2.getChoices().add(ch);
        
        ch = new ChoiceVO();
        ch.setText("Плохой светофор");
        scq2.getChoices().add(ch);
        Singletons.ds().save(scq2);

        SingleChoiceQuestionVO scq3 = new SingleChoiceQuestionVO();
        scq3.setGroup(grSign);
        scq3.setText("Что это за лошадь?");
        
        ch = new ChoiceVO();
        ch.setText("Хороший лошадь");
        ch.setRight(true);
        scq3.getChoices().add(ch);
        
        ch = new ChoiceVO();
        ch.setText("Плохой лошадь");
        scq3.getChoices().add(ch);
        Singletons.ds().save(scq3);
        
        MTEGroupVO ge  = new MTEGroupVO();
        ge.setGroupId(grSign.getId());
        ge.setNumberOfQuestions(1);
        
        MTEQuestionVO qe  = new MTEQuestionVO();
        qe.setQuestionId(scq1.getId());
        
        MetaTestVO mt = new MetaTestVO();
        mt.setName("ПДД Российской Федерации - экзамен");
        mt.getEntries().add(qe);
        mt.getEntries().add(ge);
        Singletons.ds().save(mt);
        
        PublicationVO publication = new PublicationVO();
        publication.setMaxTakeTestTime(60000l);
        publication.setMaxQuestionAnswerTime(60000l);
        publication.setMinScore(2d);
        publication.setInterruptOnFalure(true);
        publication.setAllowSkipQuestions(false);
        publication.setIntroduction("Пожалуйста заполните поля ниже. Без этого вы не сможете начать тест.");
        publication.setAskFirstName(true);
        publication.setAskFirstNameRequired(true);
        publication.setAskLastName(true);
        publication.setAskLastNameRequired(true);        
        publication.setAskMiddleName(true);
        publication.setAskMiddleNameRequired(false);
        publication.setAskEmail(true);
        
        AdditionalQuestionVO aq = new AdditionalQuestionVO();
        aq.setText("Ключ");
        aq.setRequired(true);
        aq.setCheckValue("123");
        publication.getAdditionalQuestions().add(aq);
        publication.setMaxAttempts(2);
        publication.setMetatestId(mt.getId());
        Singletons.ds().save(publication);

        
        mt = new MetaTestVO();
        mt.setName("ПДД Российской Федерации - обучение");
        for (int i = 0; i < 500; i++) {
            scq2.setId(BaseVO.idString());
            if (i < 250) {
                scq2.setGroupId(grSign.getId());
            }
            else {
                scq2.setGroupId(grOther.getId());
            }
            Singletons.ds().save(scq2);
        }
        ge  = new MTEGroupVO();
        ge.setGroupId(grSign.getId());
        ge.setNumberOfQuestions(5);
        mt.getEntries().add(ge);
        
        ge  = new MTEGroupVO();
        ge.setGroupId(grOther.getId());
        ge.setNumberOfQuestions(5);
        mt.getEntries().add(ge);

        Singletons.ds().save(mt);
        
        publication = new PublicationVO();
        publication.setMetatestId(mt.getId());
        publication.setMaxTakeTestTime(60000l);
        publication.setMinScore(9d);
        publication.setInterruptOnFalure(true);
        publication.setAllowSkipQuestions(true);
        Singletons.ds().save(publication);

        publication.setId(BaseVO.idString());
        Singletons.ds().save(publication);
        
        user = Singletons.al().createNewUser("e_moskovkina@fgufccs.ru", "mskvkn", "fgufccs");
        
        LoginManager.setThreadLocalLoggedUser(user);
        mt = new MetaTestVO();
        mt.setName("Смета (экзамен)");
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "1");
        ge.setNumberOfQuestions(30);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "2");
        ge.setNumberOfQuestions(2);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "3");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "4");
        ge.setNumberOfQuestions(2);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "5");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "6");
        ge.setNumberOfQuestions(12);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "7");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "8");
        ge.setNumberOfQuestions(14);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "9");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "10");
        ge.setNumberOfQuestions(4);
        mt.getEntries().add(ge);
        
        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "11");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "12");
        ge.setNumberOfQuestions(4);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "13");
        ge.setNumberOfQuestions(3);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "14");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "15");
        ge.setNumberOfQuestions(2);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "16");
        ge.setNumberOfQuestions(1);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "17");
        ge.setNumberOfQuestions(7);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "18");
        ge.setNumberOfQuestions(3);
        mt.getEntries().add(ge);

        ge = new MTEGroupVO();
        ge.setGroupId(user.getTenantId() + "19");
        ge.setNumberOfQuestions(10);
        mt.getEntries().add(ge);

        Singletons.ds().save(mt);
        
        publication = new PublicationVO();
        publication.setMetatestId(mt.getId());
        publication.setAllowSkipQuestions(true);

        publication.setAskFirstName(true);
        publication.setAskFirstNameRequired(true);
        
        publication.setAskLastName(true);
        publication.setAskLastNameRequired(true);

        publication.setAskMiddleName(true);
        publication.setAskMiddleNameRequired(true);
        
        try {
            publication.setEnd((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).parse("2013-08-31T00:00:00+0400"));
        }
        catch (Exception e) {
        }
        
        aq = new AdditionalQuestionVO();
        aq.setText("Ключ");
        aq.setRequired(true);
        aq.setCheckValue("fgufccs");
        publication.getAdditionalQuestions().add(aq);
        
        publication.setMaxAttempts(1);
        Singletons.ds().save(publication);        
    }
}
