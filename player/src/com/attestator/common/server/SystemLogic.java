package com.attestator.common.server;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.attestator.common.server.helper.DatastoreHelper;
import com.attestator.common.server.helper.MailHelper;
import com.attestator.common.shared.SharedConstants;
import com.attestator.common.shared.dto.UserValidationError;
import com.attestator.common.shared.helper.Base62Helper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CronTaskVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.UserVO;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

/**
 * 
 * @author Vitaly
 * Only this class is allowed use raw datastore;
 */
public class SystemLogic {
    private Datastore rawDs;

    public SystemLogic(Datastore rawDs) {
        super();
        this.rawDs = rawDs;
    }
    
    public Set<UserValidationError> validateForCreateNewUser(String email, String username, String password) {
        HashSet<UserValidationError> result = new HashSet<UserValidationError>();
        
        if (StringHelper.isEmptyOrNull(email)) {
            result.add(UserValidationError.incorrectEmail);
        }
        else if (!email.matches(SharedConstants.EMAIL_VALIDATION_REGEX)) {
            result.add(UserValidationError.incorrectEmail);
        }
        else {
            Query<UserVO> q = rawDs.createQuery(UserVO.class);
            q.field("email").equal(email);
            if (q.countAll() > 0) {
                result.add(UserValidationError.emailAlreadyExists);
            }
        }
        
        if (StringHelper.isEmptyOrNull(username)) {
            result.add(UserValidationError.incorrectUsername);
        }
        else if (!username.matches(SharedConstants.USERNAME_VALIDATION_REGEX)) {
            result.add(UserValidationError.incorrectUsername);
        }
        else {
            Query<UserVO> q = rawDs.createQuery(UserVO.class);
            q.field("username").equal(username);
            if (q.countAll() > 0) {
                result.add(UserValidationError.usernameAlreadyExists);
            }
        }
        
        return result;
    }
    
    public boolean isThisEmailExists(String email) {
        CheckHelper.throwIfNullOrEmpty(email, "email");
        
        Query<UserVO> q = rawDs.createQuery(UserVO.class);        
        q.field("email").equal(email);
        
        return q.countAll() > 0;
    }
    
    public UserVO createNewUser(String email, String username, String password) {
        return createNewUser(email, username, password, null); 
    }    
    
    
    private static final String CREATE_USER_SUBJECT = "Регистрация нового пользователя на examator.ru";
    private static final String CREATE_USER_BODY = 
            "Здравствуйте!\n" +
    		"На сайте examator.ru был зарегистрирован новый пользователь.\n" +
    		"Логин: %s\n" +
    		"Пароль: %s\n" +
    		"Используйте эти данные для входа в сервис по адресу http://examator.ru/admin\n" +
            "\n" +
            "С уважением,\n" +
            "Администрация examator.ru";    

    public UserVO createNewUser(String email, String username, String password, String id) {
        CheckHelper.throwIfNullOrEmpty(email, "email");
        CheckHelper.throwIfNullOrEmpty(password, "password");
        CheckHelper.throwIfNullOrEmpty(username, "username");
        
        UserVO result = new UserVO();
        result.setEmail(email);
        result.setUsername(username);
        result.setPassword(password);
        
        if (id == null) {
            id = BaseVO.idString();
        }
        
        result.setId(id);
        result.setTenantId(id);
        result.setDefaultGroupId(id);
        
        rawDs.save(result);
        
        GroupVO defaultGroup = new GroupVO();
        defaultGroup.setId(result.getDefaultGroupId());
        defaultGroup.setTenantId(result.getTenantId());
        defaultGroup.setName(GroupVO.DEFAULT_GROUP_INITIAL_NAME);
        defaultGroup.getSharedForTenantIds().add(result.getTenantId());
        defaultGroup.setOwnerUsername(result.getUsername());
        
        rawDs.save(defaultGroup);
        
        String body = String.format(CREATE_USER_BODY, username, password);
        MailHelper.sendMail("support@examator.ru", email, CREATE_USER_SUBJECT, body);        
        
        return result;
    }
    
    private static final String UPDATE_PASSWORD_SUBJECT = "Новый пароль на examator.ru";
    private static final String UPDATE_PASSWORD_BODY = 
            "Здравствуйте!\n" +
            "Пароль для пользователя %s на сайте examator.ru был изменен.\n" +
            "Новый пароль: %s\n" +
            "Используйте этот пароль для входа в сервис по адресу http://examator.ru/admin\n" +
            "\n" +
            "С уважением,\n" +
            "Администрация examator.ru" ;
    
    public void restorePassword(String email) {
        CheckHelper.throwIfNullOrEmpty(email, "email");

        Query<UserVO> q = rawDs.createQuery(UserVO.class);        
        q.field("email").equal(email);

        UserVO user = q.get();
        if (user == null) {
            throw new IllegalStateException("No user with email: " + email);
        }
        
        String newPassword = Base62Helper.getRandomBase62IntId();
        
        UpdateOperations<UserVO> uo = rawDs.createUpdateOperations(UserVO.class);
        uo.set("password", newPassword);
        
        rawDs.update(user, uo);
        
        String body = String.format(UPDATE_PASSWORD_BODY, user.getUsername(), newPassword);
        MailHelper.sendMail("support@examator.ru", email, UPDATE_PASSWORD_SUBJECT, body);
    }
    
    public UserVO getUserByLoginPassword(String emailOrUsername, String password) {
        CheckHelper.throwIfNullOrEmpty(emailOrUsername, "email");
        CheckHelper.throwIfNullOrEmpty(password, "password");
        
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        q.or(q.criteria("email").equal(emailOrUsername), q.criteria("username").equal(emailOrUsername));
        q.field("password").equal(password);
        UserVO user = q.get();
        
        return user;
    }
    
    public UserVO getUserByTenantId(String tenantId) {
        CheckHelper.throwIfNullOrEmpty(tenantId, "tenantId");
        
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        q.field("tenantId").equal(tenantId);
        UserVO user = q.get();        
        
        return user;
    }
    
    public void deletePublicationsForMetatest(String metetestId) {
        CheckHelper.throwIfNullOrEmpty(metetestId, "metetestId");
        
        Query<PublicationVO> q = rawDs.createQuery(PublicationVO.class);
        q.field("metatestId").equal(metetestId);
        
        rawDs.delete(q);
    }

    public void deletePritingPropertiesForMetatest(String metetestId) {
        CheckHelper.throwIfNullOrEmpty(metetestId, "metetestId");
        
        Query<PrintingPropertiesVO> q = rawDs.createQuery(PrintingPropertiesVO.class);
        q.field("metatestId").equal(metetestId);
        
        rawDs.delete(q);
    }

    public PagingLoadResult<UserVO> loadUserPage(FilterPagingLoadConfig loadConfig, String ... excludFields) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        
        // Create query
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        
        HashSet<String> usedExcludeFields = new HashSet<String>();        
        // Exclude fields if any
        if (excludFields != null) {
            usedExcludeFields.addAll(Arrays.asList(excludFields));
        }
        // Don't load password. Never!
        usedExcludeFields.add("password");
        
        q.retrievedFields(false, usedExcludeFields.toArray(new String[0]));
        
        // Add load config
        DatastoreHelper.addLoadConfig(q, loadConfig);

        // Get total count (without offset and limit)
        long count = q.countAll();        
                
        q.offset(loadConfig.getOffset());
        q.limit(loadConfig.getLimit());
        
        List<UserVO> qRes = q.asList();
        
        PagingLoadResultBean<UserVO> result = new PagingLoadResultBean<UserVO>();
        result.setData(qRes);
        result.setOffset(loadConfig.getOffset());
        result.setTotalLength((int)count);
        
        return result;
    }
    
    public void scheduleCronTask(CronTaskVO cronTask) {
        rawDs.save(cronTask);
    }
    
    public List<CronTaskVO> getActiveCronTasks() {
        Query<CronTaskVO> q = rawDs.createQuery(CronTaskVO.class);
        q.field("time").lessThanOrEq(new Date());
        q.order("time");
        List<CronTaskVO> result = q.asList();
        return result;
    }
    
    public void removeCronTask(String cronTaskId) {
        CheckHelper.throwIfNullOrEmpty(cronTaskId, "cronTaskId");
        
        Query<CronTaskVO> q = rawDs.createQuery(CronTaskVO.class);
        q.field("_id").equal(cronTaskId);
        rawDs.delete(q);
    }
}
