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
    
    public static UserVO login(HttpSession session, String tenantId) {
        UserVO user = Singletons.sl().getUserByTenantId(tenantId);
        
        if (session != null) {
            if (user != null) {
                session.setAttribute(USER_ATTR_NAME, user);
            }
            else {
                session.removeAttribute(USER_ATTR_NAME);
            }
        }
        
        loggedUser.set(user);        
        return user;
    }
    
    
    public static UserVO login(HttpSession session, String email, String md5EncodedPassword) {                
        UserVO user = Singletons.sl().getUserByLoginPassword(email, md5EncodedPassword);
        
        if (session != null) {
            if (user != null) {
                session.setAttribute(USER_ATTR_NAME, user);
            }
            else {
                session.removeAttribute(USER_ATTR_NAME);
            }
        }
        
        loggedUser.set(user);        
        return user;
    }
    
    public static void logout(HttpSession session) { 
        if (session != null) {
            session.removeAttribute(USER_ATTR_NAME);
        }
        loggedUser.set(null);
    }

    public static UserVO setThreadLocalTenantId(String tenantId) {
        UserVO user = Singletons.sl().getUserByTenantId(tenantId);      
        loggedUser.set(user);
        return user;
    }

    public static void setThreadLocalLoggedUser(UserVO value) {
        loggedUser.set(value);
    }
    
    public static UserVO getThreadLocalLoggedUserThrowIfNull() {
        UserVO result = loggedUser.get();
        if (result == null) {
            throw new IllegalStateException("Logged user not set. Looks like user not logged in.");
        }
        return result;
    }

    public static UserVO getThreadLocalLoggedUser() {
        UserVO result = loggedUser.get();
        if (result == null) {
            throw new IllegalStateException("Logged user not set. Looks like user not logged in.");
        }
        return result;
    }
    
    public static String getThreadLocalTenatId() {
        String result = null;
        if (loggedUser.get() != null) {
            result = loggedUser.get().getTenantId(); 
        }
        if (result == null) {
            throw new IllegalStateException("Current tenant not set. Looks like user not logged in.");
        }
        return result;
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void destroy() {
    }
}
