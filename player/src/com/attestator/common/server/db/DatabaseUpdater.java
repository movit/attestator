package com.attestator.common.server.db;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.attestator.admin.server.LockManager;
import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.AdditionalQuestionVO.AnswerTypeEnum;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.DBVersionVO;
import com.attestator.common.shared.vo.DatabaseUpdateLockVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.LockVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.ShareableVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.metapossum.utils.scanner.reflect.ClassesInPackageScanner;

public class DatabaseUpdater {
    private static Logger logger = Logger.getLogger(DatabaseUpdater.class);
    
    public static final int DB_VERSION = 32;
    
    private Datastore rawDs;   
    
    public DatabaseUpdater(Datastore rawDs) {
        super();
        this.rawDs = rawDs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void updateDatabase() {
        // Only one instance of server should update database
        DatabaseUpdateLockVO lock = new DatabaseUpdateLockVO();
        if (!LockManager.lock(lock)) {
            LockManager.blockUntilLockReleased(lock);
            return;
        }
        
        // First of all remove all locks if any
        removeAllLocks();
        
        DBVersionVO version = rawDs.createQuery(DBVersionVO.class).get();        
        if (version == null) {
            version = new DBVersionVO(0);  
        }
        
        logger.info("Current database version " + version.getVersion());
        if (version.getVersionOrZero() < DB_VERSION) {
            logger.info("Updating to " + DB_VERSION);
        }
        
        if (version.getVersionOrZero() < 1) {
            addTestData();
        }
        
        if (version.getVersionOrZero() < 2) {
            Query<ChangeMarkerVO> q = rawDs.createQuery(ChangeMarkerVO.class);
            rawDs.delete(q);
        }
        
        if (version.getVersionOrZero() < 10) {
            Query<PublicationVO> q = rawDs.createQuery(PublicationVO.class);
            q.disableValidation().field("additionalQuestions.checkValue").exists();
            UpdateOperations<PublicationVO> uo = rawDs.createUpdateOperations(PublicationVO.class);
            uo.disableValidation().set("additionalQuestions.$.answerType", AnswerTypeEnum.key).enableValidation();            
            rawDs.update(q, uo);
            
            q = rawDs.createQuery(PublicationVO.class);
            q.disableValidation().field("additionalQuestions._id").exists().field("additionalQuestions.checkValue").doesNotExist();
            uo = rawDs.createUpdateOperations(PublicationVO.class);
            uo.disableValidation().set("additionalQuestions.$.answerType", AnswerTypeEnum.text).enableValidation();            
            rawDs.update(q, uo);
        }

        if (version.getVersionOrZero() < 11) {
            rawDs.ensureIndexes();
        }
        
        if (version.getVersionOrZero() < 21) {
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
                    
                    Query q = rawDs.createQuery(clazz);
                    UpdateOperations uo = rawDs.createUpdateOperations(clazz);
                    uo.set("modified", now);
                    uo.set("created", now);
                    
                    rawDs.update(q, uo);
                }
            } catch (IOException e) {
            }

            Query<ReportVO> q = rawDs.createQuery(ReportVO.class);
            UpdateOperations<ReportVO> uo = rawDs.createUpdateOperations(ReportVO.class);
            uo.set("publication.created", now);
            rawDs.update(q, uo);            
            
            q = rawDs.createQuery(ReportVO.class);
            uo = rawDs.createUpdateOperations(ReportVO.class);
            uo.set("publication.modified", now);
            rawDs.update(q, uo);                        

            Query<ReportVO> qr = rawDs.createQuery(ReportVO.class);
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
                
                rawDs.save(report);
            }
        }

        if (version.getVersionOrZero() < 22) {
            loadAndSave(MetaTestVO.class);
        }
        
        if (version.getVersionOrZero() < 25) {
            Query<PrintingPropertiesVO> q = rawDs.createQuery(PrintingPropertiesVO.class);            
            rawDs.delete(q);
        }
        
        if (version.getVersionOrZero() < 26) {
            Query<PrintingPropertiesVO> q = rawDs.createQuery(PrintingPropertiesVO.class);            
            rawDs.delete(q);
        }
        
        if (version.getVersionOrZero() < 27) {
            try {
                Set<Class<? extends ShareableVO>> shareableClasses = 
                        (new ClassesInPackageScanner()).findSubclasses("com.attestator.common.shared.vo", ShareableVO.class);
                
                for (Class<? extends ShareableVO> clazz: shareableClasses) {
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    
                    if (clazz.getAnnotation(Entity.class) == null) {
                        continue;
                    }
                    
                    Query<? extends ShareableVO> q = rawDs.createQuery(clazz);
                    q.retrievedFields(true, "_id", "tenantId");
                    Iterator<? extends ShareableVO> it = q.iterator();
                    
                    while (it.hasNext()) {
                        ShareableVO shareable = it.next();
                        UpdateOperations uo = rawDs.createUpdateOperations(clazz);
                        Set<String> sharedForTenantIds = new HashSet<String>();
                        sharedForTenantIds.add(shareable.getTenantId());                        
                        uo.set("sharedForTenantIds", sharedForTenantIds);
                        rawDs.update(shareable, uo);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }            
        }
        
        if (version.getVersionOrZero() < 28) {
            rawDs.ensureIndexes();
        }
        
        if (version.getVersionOrZero() < 29) {
            Query<UserVO> q = rawDs.createQuery(UserVO.class);
            for (UserVO user: q.asList()) {
                String[] emailParts = user.getEmail().split("@", 2);
                user.setUsername(emailParts[0]);
                rawDs.save(user);
            }
        }
        
        if (version.getVersionOrZero() < 31) {
            try {
                
                List<UserVO> usersList = rawDs.createQuery(UserVO.class).asList();
                final Map<String, UserVO> usersMap = new HashMap<String, UserVO>();
                for (UserVO user: usersList) {
                    usersMap.put(user.getTenantId(), user);
                }
                
                Set<Class<? extends ShareableVO>> shareableClasses = 
                        (new ClassesInPackageScanner()).findSubclasses("com.attestator.common.shared.vo", ShareableVO.class);
                
                for (Class<? extends ShareableVO> clazz: shareableClasses) {
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    
                    if (clazz.getAnnotation(Entity.class) == null) {
                        continue;
                    }
                    
                    Query<? extends ShareableVO> q = rawDs.createQuery(clazz);
                    q.retrievedFields(true, "_id", "tenantId");
                    Iterator<? extends ShareableVO> it = q.iterator();
                    
                    while (it.hasNext()) {
                        ShareableVO shareable = it.next();
                        UpdateOperations uo = rawDs.createUpdateOperations(clazz);
                        UserVO user = usersMap.get(shareable.getTenantId());
                        if (user != null) {
                            uo.set("ownerUsername", user.getUsername());
                        }                        
                        rawDs.update(shareable, uo);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }            
        }
        
        if (version.getVersionOrZero() < 27) {
            try {
                Set<Class<? extends ShareableVO>> shareableClasses = 
                        (new ClassesInPackageScanner()).findSubclasses("com.attestator.common.shared.vo", ShareableVO.class);
                
                for (Class<? extends ShareableVO> clazz: shareableClasses) {
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    
                    if (clazz.getAnnotation(Entity.class) == null) {
                        continue;
                    }
                    
                    Query<? extends ShareableVO> q = rawDs.createQuery(clazz);
                    q.retrievedFields(true, "_id", "tenantId");
                    Iterator<? extends ShareableVO> it = q.iterator();
                    
                    while (it.hasNext()) {
                        ShareableVO shareable = it.next();
                        UpdateOperations uo = rawDs.createUpdateOperations(clazz);
                        Set<String> sharedForTenantIds = new HashSet<String>();
                        sharedForTenantIds.add(shareable.getTenantId());                        
                        uo.set("sharedForTenantIds", sharedForTenantIds);
                        rawDs.update(shareable, uo);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }            
        }
        
        if (version.getVersionOrZero() < 32) {
            try {
                Set<Class<? extends ShareableVO>> shareableClasses = 
                        (new ClassesInPackageScanner()).findSubclasses("com.attestator.common.shared.vo", ShareableVO.class);
                
                for (Class<? extends ShareableVO> clazz: shareableClasses) {
                    if (Modifier.isAbstract(clazz.getModifiers())) {
                        continue;
                    }
                    
                    if (clazz.getAnnotation(Entity.class) == null) {
                        continue;
                    }
                    
                    Query<? extends ShareableVO> q = rawDs.createQuery(clazz);
                    q.retrievedFields(true, "_id", "tenantId");
                    Iterator<? extends ShareableVO> it = q.iterator();
                    
                    while (it.hasNext()) {
                        ShareableVO shareable = it.next();
                        UpdateOperations uo = rawDs.createUpdateOperations(clazz);
                        Set<String> sharedForTenantIds = new HashSet<String>();
                        sharedForTenantIds.add(shareable.getTenantId());                        
                        uo.set("sharedForTenantIds", sharedForTenantIds);
                        rawDs.update(shareable, uo);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }            
        }
        
        if (version.getVersionOrZero() < DB_VERSION) {
            resetChangeMarkers();
            
            version.setVersion(DB_VERSION);
            rawDs.save(version);
        }
        
        logger.info("Database is up to date");
    }
    
    private void removeAllLocks() {
        Query<LockVO> q = rawDs.createQuery(LockVO.class);
        rawDs.delete(q);
    }
    
    private void resetChangeMarkers() {
        Query<ChangeMarkerVO> q = rawDs.createQuery(ChangeMarkerVO.class);            
        rawDs.delete(q);
        
        Query<UserVO> qu = rawDs.createQuery(UserVO.class);        
        for (UserVO user: qu.asList()) {
            ChangeMarkerVO cm = new ChangeMarkerVO(null, user.getTenantId(), null);
            rawDs.save(cm);
        }
    }
    
    private <T> void loadAndSave(Class<T> clazz) {
        Query<T> q = rawDs.createQuery(clazz);
        for (T obj: q.asList()) {
            rawDs.save(obj);
        }
    }
    
    private void addTestData() {
        Singletons.sl().createNewUser("test2@test.com", "test");
        
        UserVO user = Singletons.sl().createNewUser("test@test.com", "test");        
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
                scq2.setGroupName(grSign.getName());
            }
            else {
                scq2.setGroupId(grOther.getId());
                scq2.setGroupName(grOther.getName());
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
        
        user = Singletons.sl().createNewUser("e_moskovkina@fgufccs.ru", "mskvkn", "fgufccs");
        
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
