package com.attestator.admin.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.attestator.admin.server.helper.PrintHelper;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.player.server.Singletons;

public class PrintTestServlet extends HttpServlet {
    
    private static final long serialVersionUID = 8097344922644172597L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PrintTestServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {        
        
        String id = request.getParameter("id");
        if (id == null) {
            throw new ServletException("PrintingProperties id not specified");
        }
        
        PrintingPropertiesVO properties = Singletons.al().get(PrintingPropertiesVO.class, id);
        if (properties == null) {
            throw new ServletException("Unalble to load PrintingProperties with id: " + id);
        }
        
        MetaTestVO metatest = Singletons.al().get(MetaTestVO.class, properties.getMetatestId());
        if (metatest == null) {
            throw new ServletException("Unalble to load Metatest with id: " + id);
        }
                
        String testHtml = PrintHelper.printTest(metatest, properties);
        
        response.getOutputStream().write(testHtml.getBytes());
    }    
}
