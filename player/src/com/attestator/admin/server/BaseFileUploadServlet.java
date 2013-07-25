package com.attestator.admin.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

public abstract class BaseFileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 2190949448491169000L;

    public static class UploadException extends Exception{
        private static final long serialVersionUID = -5738709613091410195L;

        public UploadException() {
            super();
        }

        public UploadException(String message, Throwable cause) {
            super(message, cause);
        }

        public UploadException(String message) {
            super(message);
        }

        public UploadException(Throwable cause) {
            super(cause);
        }
    } 
    
    private static final Logger logger = Logger.getLogger(BaseFileUploadServlet.class);
    
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        if (!ServletFileUpload.isMultipartContent(request)) {
            logger.error("Not a multipart request");
            return;
        }

        ServletFileUpload upload = new ServletFileUpload(); // from Commons

        try {
            FileItemIterator iter = upload.getItemIterator(request);            
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    continue;
                }
                InputStream in = item.openStream();                
                process(in);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public abstract void process(InputStream upload) throws UploadException;
}
