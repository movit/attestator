package com.attestator.player.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.attestator.common.server.helper.ServerHelper;
import com.attestator.common.shared.SharedConstants;
import com.attestator.common.shared.helper.Base62Helper;

public class ClientIdManager implements Filter {	
    
    private static ThreadLocal<String> clientId = new ThreadLocal<String>(); 
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletResponse httpRes = (HttpServletResponse) response;
        HttpServletRequest  httpReq = (HttpServletRequest) request;
        
        String clientId = ServerHelper.getCookieValue(httpReq.getCookies(), SharedConstants.CLIENT_ID_COOKIE_NAME, null);
        
        if (clientId == null) {
            clientId = Base62Helper.getRandomBase62IntId();
            Cookie clientIdCookie = new Cookie(SharedConstants.CLIENT_ID_COOKIE_NAME, clientId);
            clientIdCookie.setMaxAge(10 * SharedConstants.SECONDS_IN_YEAR);
            clientIdCookie.setPath("/");
            httpRes.addCookie(clientIdCookie);
        }
        
        ClientIdManager.clientId.set(clientId);
        
        // Continue request handling
        chain.doFilter(request, response);
    }

    public static String getThreadLocalClientId() {
        return clientId.get();
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void destroy() {
    }
}
