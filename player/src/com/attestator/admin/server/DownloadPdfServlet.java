package com.attestator.admin.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.attestator.admin.server.helper.print.PrintHelper;
import com.attestator.admin.server.helper.print.PrintHelper.PrintingMedia;
import com.attestator.common.server.helper.ServerHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.player.server.Singletons;

public class DownloadPdfServlet extends HttpServlet {
    
    private static final long serialVersionUID = 8097344922644172597L;
    
    private static final Logger logger = Logger
            .getLogger(DownloadPdfServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            String printingPropertiesId = request.getParameter("printingPropertiesId");
            CheckHelper.throwIfNullOrEmpty(printingPropertiesId, "printingPropertiesId");
            
            PrintingPropertiesVO properties = Singletons.al().getById(PrintingPropertiesVO.class, printingPropertiesId);
            CheckHelper.throwIfNull(properties, "properties");        
            
            MetaTestVO metatest = Singletons.al().getById(MetaTestVO.class, properties.getMetatestId());
            CheckHelper.throwIfNull(metatest, "metatest");
                        
            String fileNameBase = metatest.getName() + "_" + properties.getPrintAttemptOrZero();
            fileNameBase = fileNameBase.replaceAll("\\s+", "_");
            fileNameBase = StringHelper.toTranslit(fileNameBase);
            
            response.setHeader("Expires", "0");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.            
            response.setHeader("Pragma", "no-cache");
            
            if (properties.isThisOnePdfPerVariant()) {
                String fileName = fileNameBase + ".zip";
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=" + "\"" + fileName + "\";" );
                
                ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
                OutputStream pzout = ServerHelper.createNonClosingProxy(zout);
                
                for (int i = 0; i < properties.getVariantsCountOrZero(); i++) {
                    String pdfFileName = fileNameBase + "_" + (i+1) + ".pdf";
                    ZipEntry zipEntry = new ZipEntry(pdfFileName);
                    zout.putNextEntry(zipEntry);
                    
                    String testHtml = PrintHelper.printTest(metatest, properties, "" + (i+1), PrintingMedia.pdf);                    
                    PrintHelper.renderToPdf(testHtml, pzout);
                }
                zout.close();
            }
            else {
                String fileName = fileNameBase + ".pdf";
                response.setContentType("application/pdf");
                response.setHeader( "Content-Disposition", "attachment; filename=" + "\"" + fileName + "\";" );
                
                String testHtml = PrintHelper.printTest(metatest, properties, null, PrintingMedia.pdf);
                PrintHelper.renderToPdf(testHtml, response.getOutputStream());
                
                response.getOutputStream().close();
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }    
}
