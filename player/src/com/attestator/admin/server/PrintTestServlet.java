package com.attestator.admin.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class PrintTestServlet extends HttpServlet {
    
    private static final long serialVersionUID = 8097344922644172597L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PrintTestServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        
        response.getOutputStream().write("<body>This feature is disabled</body>".getBytes());
        return;
        
//        String pathInfo = request.getPathInfo();
//        if (!StringHelper.isEmptyOrNull(pathInfo)) {
//            pathInfo = pathInfo.replaceAll("[/\\\\]", "");
//        }       
//       
//        if (!StringHelper.isEmptyOrNull(pathInfo)) {
//            URLConnection membufferConnection = (new URL("membuffer://" + pathInfo)).openConnection();
//            InputStream in = membufferConnection.getInputStream();
//            OutputStream out = response.getOutputStream();
//            
//            byte[] buffer = new byte[1024];
//            int len = in.read(buffer);
//            while (len != -1) {
//                out.write(buffer, 0, len);
//                len = in.read(buffer);
//            }
//            
//            out.close();
//        }        
    }    
}
