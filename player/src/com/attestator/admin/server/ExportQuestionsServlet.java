package com.attestator.admin.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.server.Singletons;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public class ExportQuestionsServlet extends HttpServlet {
    
    private static final long serialVersionUID = 8097344922644172597L;
    
    private static final Logger logger = Logger
            .getLogger(ExportQuestionsServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int maxAnswersCount = 0;
            
            // Find max questions count
            FilterPagingLoadConfigBean loadConfig = new FilterPagingLoadConfigBean();                        
            loadConfig.setOffset(0);
            loadConfig.setLimit(100);            
            while(true) {
                PagingLoadResult<QuestionVO> loadResult = Singletons.al().loadPage(QuestionVO.class, loadConfig);
                if (NullHelper.isEmptyOrNull(loadResult.getData())) {
                    break;
                }
                
                for (QuestionVO question: loadResult.getData()) {
                    int answersCount = NullHelper.nullSafeSize(((SingleChoiceQuestionVO) question).getChoices());
                    maxAnswersCount = Math.max(maxAnswersCount, answersCount);
                }
                
                if ((loadConfig.getOffset() + loadConfig.getLimit()) >= loadResult.getTotalLength()) {
                    break;
                }
                
                loadConfig.setOffset(loadConfig.getOffset() + 100);
            }
            
            response.setHeader("Content-Disposition", 
                    "attachment; filename=" + 
                    "\"" + 
                    LoginManager.getThreadLocalLoggedUser().getUsername() + "-" + 
                    StringHelper.toTranslit("вопросы") + ".csv" + "\";" );
            
            response.setContentType("text/csv; charset=utf-8");
            
            // Print header
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
            pw.print("Тема;");
            pw.print("№;");
            pw.print("Вопрос;");
            for (int i = 0; i < maxAnswersCount; i++) {
                pw.print("Вариант ");
                pw.print(ReportHelper.getLatinBullet(i + 1));
                pw.print(";");
            }
            pw.print("Ответ");
            pw.println();
            
            // Print questions
            loadConfig = new FilterPagingLoadConfigBean();                        
            loadConfig.setOffset(0);
            loadConfig.setLimit(100);
            
            while(true) {
                PagingLoadResult<QuestionVO> loadResult = Singletons.al().loadPage(QuestionVO.class, loadConfig);
                if (NullHelper.isEmptyOrNull(loadResult.getData())) {
                    break;
                }
                
                for (QuestionVO question: loadResult.getData()) {
                    SingleChoiceQuestionVO scq = (SingleChoiceQuestionVO) question;
                    
                    pw.print("\"" + scq.getGroupId() + "\"");
                    pw.print(";");
                    pw.print("\"" + scq.getId() + "\"");
                    pw.print(";");
                    pw.print("\"" + scq.getText() + "\"");
                    pw.print(";");
                    
                    int rightAnswerNo = -1;
                    for (int i = 0; i < maxAnswersCount; i++) {
                        if (i < scq.getChoices().size()) {
                            pw.print("\"" 
                                    + ReportHelper.getLatinBullet(i + 1) + ") " 
                                    + scq.getChoices().get(i).getText() + "\"");
                            if (scq.getChoices().get(i).isThisRight()) {
                                rightAnswerNo = i;
                            }
                        }
                        pw.print(";");
                    }
                    
                    if (rightAnswerNo >= 0) {
                        pw.print(ReportHelper.getLatinBullet(rightAnswerNo + 1));
                    }
                    
                    pw.println();
                }
                
                if ((loadConfig.getOffset() + loadConfig.getLimit()) >= loadResult.getTotalLength()) {
                    break;
                }
                
                loadConfig.setOffset(loadConfig.getOffset() + 100);
            }
            
            pw.close();
            
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }    
}
