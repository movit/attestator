package com.attestator.admin.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.attestator.admin.server.helper.PrintHelper;
import com.attestator.admin.server.helper.PrintHelper.Mode;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.player.server.Singletons;

public class PrintTestServlet extends HttpServlet {
    
    private static final long serialVersionUID = 8097344922644172597L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PrintTestServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {        
        
        String  metatestId = request.getParameter("metatestId");
        if (metatestId == null) {
            throw new ServletException("metatestId not specified");
        }
        
        MetaTestVO metatest = Singletons.al().get(MetaTestVO.class, metatestId);
        if (metatest == null) {
            throw new ServletException("Unalble to load metatest with id: " + metatestId);
        }
        
        boolean randomQuestionsOrder = 
                Boolean.parseBoolean(request.getParameter("randomQuestionsOrder"));
        
        String testHtml = PrintHelper.printTest(Mode.doublePage, metatest, randomQuestionsOrder, 15);
        
        response.getOutputStream().write(testHtml.getBytes());
    }    
}