package com.attestator.common.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.attestator.player.server.PlayerLogic;
 
/**
 * @author Dermot Butterfield, Vitaly Moskovkin 
 */
public class HeaderFilter implements Filter {
 
    private static final Logger logger = Logger.getLogger(PlayerLogic.class);
    private Map<String,String> headersMap;
    private Pattern urlRegex;
 
    public void init(FilterConfig filterConfig) throws ServletException {
        String urlRegexParam = filterConfig.getInitParameter("urlRegex");
        if (urlRegexParam == null) {
            logger.info("No urlRegex parameter found in the web.xml (init-param) for the HeaderFilter!");
            return;
        }
        urlRegex = Pattern.compile(urlRegexParam);
        
        String headerParam = filterConfig.getInitParameter("header");
        if (headerParam == null) {
            logger.info("No headers were found in the web.xml (init-param) for the HeaderFilter!");
            return;
        }
 
        // Init the header list :
        headersMap = new LinkedHashMap<String, String>();
 
        if (headerParam.contains("|")) {
            String[] headers = headerParam.split("\\|");
            for (String header : headers) {
                parseHeader(header);
            }
 
        } else {
            parseHeader(headerParam);
        }
 
        logger.info("The following headers were registered in the HeaderFilter :");
        Set<Map.Entry<String, String>> headers = headersMap.entrySet();
        for (Map.Entry<String, String> item : headers) {
            logger.info(item.getKey() + ':' + item.getValue());
        }
    }
 
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request  = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;        
        
        while(true) {
            if (headersMap == null) {
                break;
            }
            if (urlRegex == null) {
                break;
            }
            if (request.getRequestURI() == null) {
                break;
            } 
            
            if (urlRegex.matcher(request.getRequestURI()).find()) {
                // Add the header to the response
                Set<Map.Entry<String, String>> headers = headersMap.entrySet();
                for (Map.Entry<String, String> header : headers) {
                    String headerName  = header.getKey();
                    String headerValue = header.getValue();
                    headerValue = substituteVariables(headerValue);
                    ((HttpServletResponse) response).setHeader(headerName, headerValue);
                }
            }
            break;
        }
        // Continue
        chain.doFilter(req, res);
    }
 
    private String substituteVariables(String headerValue) {
        if (headerValue.contains("${now}")) {
            DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = 
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                    .withZoneUTC().withLocale(Locale.US);
            String nowStr = RFC1123_DATE_TIME_FORMATTER.print(new DateTime());                       
            headerValue = headerValue.replaceAll("\\$\\{now\\}", nowStr);
        }
        return headerValue;
    }
    
    public void destroy() {        
        this.headersMap = null;
    }
 
    private void parseHeader(String header) {
        String headerName = header.substring(0, header.indexOf(":"));
        if (!headersMap.containsKey(headerName)) {
            headersMap.put(headerName, header.substring(header.indexOf(":") + 1));
        }
    }
}