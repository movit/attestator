package com.attestator.admin.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.query.Query;

public class LoginManager implements Filter {
    public static final String USER_ATTR_NAME = "user";    
    
    private static ThreadLocal<UserVO> loggedUser = new ThreadLocal<UserVO>(); 
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {        
        
        UserVO user = (UserVO)((HttpServletRequest) request).getSession().getAttribute(USER_ATTR_NAME);
        loggedUser.set(user);
        
        // Continue request handling
        chain.doFilter(request, response);        
    }
    
    public static UserVO login(HttpSession session, String email, String md5EncodedPassword) {        
        Query<UserVO> q = Singletons.rawDs().createQuery(UserVO.class);
        q.field("email").equal(email);
        q.field("password").equal(md5EncodedPassword);
        UserVO user = q.get();
        
        if (user != null) {
            session.setAttribute(USER_ATTR_NAME, user);
        }
        else {
            session.removeAttribute(USER_ATTR_NAME);
        }
        
        loggedUser.set(user);        
        
        return user;
    }
    
    public static void logout(HttpSession session) {        
        session.removeAttribute(USER_ATTR_NAME);
        loggedUser.set(null);
    }

    public static UserVO setThreadLocalTenantId(String tenantId) {
        Query<UserVO> q = Singletons.rawDs().createQuery(UserVO.class);
        q.field("tenantId").equal(tenantId);
        UserVO user = q.get();        
        loggedUser.set(user);
        return user;
    }

    public static void setThreadLocalLoggedUser(UserVO value) {
        loggedUser.set(value);
    }
    
    public static UserVO getThreadLocalLoggedUser() {
        return loggedUser.get();
    }
    
    public static String getThreadLocalTenatId() {
        if (loggedUser.get() != null) {
            return loggedUser.get().getTenantId(); 
        }
        return null;
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void destroy() {
    }
}
