package com.attestator.admin.server.helper.print;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import com.attestator.common.server.helper.ServerHelper;
import com.attestator.common.shared.helper.HtmlBuilder;
import com.attestator.common.shared.helper.HtmlBuilder.Attribute;
import com.attestator.common.shared.helper.MathHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.server.Singletons;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lowagie.text.pdf.PdfReader;


public class PrintHelper {
    private static final Logger logger = Logger
            .getLogger(PrintHelper.class);
    
    private static final String CSS = getPrintCss();
    
    public static enum Mode {
        singlePage,
        doublePage
    }
    
    public abstract static class PagesIterator implements Iterator<String> {
        protected Mode mode;
        
        public PagesIterator(Mode mode) {
            this.mode = mode;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }        
    }
    
    public static class AnswersPagesIterator extends PagesIterator {
        public static int ANSWERS_PER_PAGE = 200;
        private List<QuestionVO> questions;
        private String variant;
        private String testName;
        private int page = 0;
        private int start = 0;
        
        public AnswersPagesIterator(Mode mode, List<QuestionVO> questions, String testName, String variant) {
            super(mode);
            this.questions = questions;
            this.variant = variant;
            this.testName = testName;
        }
        
        @Override
        public boolean hasNext() {
            return start < questions.size() || (MathHelper.isOdd(page) && mode == Mode.doublePage);
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            HtmlBuilder hb = new HtmlBuilder();
            
            int end = Math.min(start + ANSWERS_PER_PAGE, questions.size());
            for (int i = start; i < end; i++) {
                printAnswer(hb, questions.get(i), "" + (i + 1), variant);
            }            
            String result = makePage(hb.toString());
            
            start = end;
            page++;            
            
            return result;
        }
        
        private String makePage(String content) {
            if (StringHelper.isEmptyOrNull(content)) {
                return "";
            }
            String headerText = "<b>Вариант " + variant + ". Ответы.</b> " + testName;
            String footerText = "" + (page + 1);
            HtmlBuilder hb = new HtmlBuilder();            
            printPageHeader(hb, headerText);
            hb.startTag("div", "questions-area");
            hb.appendText(content);
            hb.endTag("div");
            printPageFooter(hb, footerText);
            return hb.toString();
        }
    }
    
    public static class QuestionsPagesIterator extends PagesIterator {
        private List<QuestionVO> questions;       
        private int page = 0;
        private int start = 0;
        private String testName;
        private String variant;
        
        public QuestionsPagesIterator(Mode mode, List<QuestionVO> questions, String testName, String variant) {
            super(mode);
            this.questions = questions;
            this.testName = testName;
            this.variant = variant;            
        }
        
        @Override
        public boolean hasNext() {
            return start < questions.size() || (MathHelper.isOdd(page) && mode == Mode.doublePage);
        }
        
        private String makePage(String content, boolean finalPage) {
            if (StringHelper.isEmptyOrNull(content)) {
                return "";
            }
            String headerText = "<b>Вариант " + variant + ".</b> " + testName;
            String footerText = "" + (page + 1);
            HtmlBuilder hb = new HtmlBuilder();            
            printPageHeader(hb, headerText);
            hb.startTag("div", finalPage ? "questions-area" : "questions-area-floating-height");
            hb.appendText(content);
            hb.endTag("div");
            printPageFooter(hb, footerText);
            if (!finalPage) {
                // Simulate page-breaker
                hb.startTag("div").appendText("aaa;").endTag("div");
            }
            return hb.toString();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            HtmlBuilder hb = new HtmlBuilder();
            String pageContent = "";   
            
            for (;start < questions.size(); start++) {                               
                
                
                printQuestion(hb, mode, questions.get(start), start + 1, variant);
                
                String pageCandidat = makePage(hb.toString(), false);
                pageCandidat = makeHtmlDocument(pageCandidat, CSS);
                
                int numberOfPagesInPdf = getNumberOfPagesInPdf(pageCandidat);
                if (numberOfPagesInPdf > 1) {
                    break;
                }
                
                pageContent = hb.toString();
            }
            
            //If current question bigger than page
            if (StringHelper.isEmptyOrNull(pageContent)) {
                pageContent = hb.toString();
                start++;
            }
            
            pageContent = makePage(pageContent, true);
            
            page++;            
            return pageContent;
        }        
    }
    
    
    private static void printSingleTestVariant(MetaTestVO metatest, PrintingPropertiesVO properties, HtmlBuilder answersHb, HtmlBuilder variantsHb, Mode mode, String varantNo) {
        String variant = "" + properties.getPrintAttempt() + "-" + varantNo;
        
        List<QuestionVO> questions = Singletons.al().generateQuestionsList(metatest, properties.isThisRandomQuestionsOrder());            
        
        printAnswers(answersHb, mode, questions, metatest.getName(), variant);
        printPageBreaker(answersHb);
        printTitlePage(variantsHb, mode, properties.getTitlePage(), metatest, variant);            
        printQuestions(variantsHb, mode, questions, metatest.getName(), variant);
    }
    
    public static String printTest(MetaTestVO metatest, PrintingPropertiesVO properties) {
        return printTest(metatest, properties, null);
    }
    
    public static String printTest(MetaTestVO metatest, PrintingPropertiesVO properties, String variantNo) {
        Mode mode = properties.isThisDoublePage() ? Mode.doublePage : Mode.singlePage;
        
        HtmlBuilder answersHb = new HtmlBuilder();
        HtmlBuilder variantsHb = new HtmlBuilder();
        
        if (variantNo == null) {
            // Print all variants
            
            for (int i = 0; i < properties.getVariantsCount(); i++) {
                printSingleTestVariant(metatest, properties, answersHb, variantsHb, mode, "" + (i + 1));
                
                if (i < (properties.getVariantsCount() - 1)) {
                    printPageBreaker(variantsHb);
                }
            }
        }
        else {
            // Print single variant
            printSingleTestVariant(metatest, properties, answersHb, variantsHb, mode, variantNo);
        }
        
        HtmlBuilder hb = new HtmlBuilder();
        
       
        hb.appendText(answersHb.toString());
        hb.appendText(variantsHb.toString());
        
        
        String result = makeHtmlDocument(hb.toString(), CSS);
        
        return result;
    }

    public static void printTitlePage(HtmlBuilder hb, Mode mode, String titlePage, MetaTestVO metatest, String variant) {        
        titlePage = titlePage.replaceAll("\\{test\\}", metatest.getName());
        titlePage = titlePage.replaceAll("\\{variant\\}", variant);
        
        hb.startTag("div", "title-page").appendText(titlePage).endTag("div");
        printPageBreaker(hb);
        
        if (mode == Mode.doublePage) {
            printPageBreaker(hb);
        }
    }
    
    public static void printSpring(HtmlBuilder hb) {
        hb.startTag("div", "spring").appendText("&nbsp;").endTag("div");
    }
    
    public static void printPageBreaker(HtmlBuilder hb) {
        hb.startTag("div", "page-breaker").appendText("&nbsp;").endTag("div");
    }
    
    public static void printPageHeader(HtmlBuilder hb, String text) {
        if (StringHelper.isEmptyOrNull(text)) {
            text = "&nbsp;";
        }
        hb.startTag("div", "header").appendText(text).endTag("div");
    }
    
    public static void printPageFooter(HtmlBuilder hb, String text) {
        if (StringHelper.isEmptyOrNull(text)) {
            text = "&nbsp;";
        }
        hb.startTag("div", "footer").appendText(text).endTag("div");
    }

    public static void printAnswer(HtmlBuilder hb, QuestionVO question, String questionIndex, String variant) {
        if (question instanceof SingleChoiceQuestionVO) {
            Integer rightAnswerIndex = ReportHelper.getRightAnswerIndex((SingleChoiceQuestionVO)question);
            if (rightAnswerIndex != null) {
                hb.startTag("div", "scq-answer").appendText(questionIndex + "-" + ReportHelper.getLatinBullet(rightAnswerIndex + 1)).endTag("div");
                hb.appendText(" ");
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported question type: " + question.getClass());
        }        
    }
    
    public static void printAnswers(HtmlBuilder hb, Mode mode, List<QuestionVO> questions, String metatestName, String variant) {        
        AnswersPagesIterator api = new AnswersPagesIterator(mode, questions, metatestName, variant);
        while (api.hasNext()) {
            hb.appendText(api.next());
            if (api.hasNext()) {
                printPageBreaker(hb);
            }
        }
    }
    
    public static void printQuestion(HtmlBuilder hb, Mode mode, QuestionVO question, int no, String variant) {
        hb.startTag("div", "question");
        
        hb.startTag("div", "question-text");
        hb.startTag("span", "question-no").appendText(no + " ").endTag("span");  
        hb.appendText(question.getText());
        hb.endTag("div");
        
        //hb.startTag("div", "question-description").appendText(question.getTaskDescription()).endTag("div");
        
        if (question instanceof SingleChoiceQuestionVO) {
            int j = 1;
            for (ChoiceVO choice: ((SingleChoiceQuestionVO) question).getChoices()) {
                hb.startTag("div", "question-choice");
                hb.appendText("<span class=\"square\">&#x2610;</span>&nbsp;" + ReportHelper.getLatinBullet(j) + ")&nbsp;");
                hb.appendText(choice.getText());
                hb.endTag("div");
                j++;
            }
        }
        else {
            throw new IllegalArgumentException("Unknown question type: " + question.getClass());
        }
       
        hb.endTag("div");
    }

    public static void printQuestions(HtmlBuilder hb, Mode mode, List<QuestionVO> questions, String testName, String variant) {
        QuestionsPagesIterator qpi = new QuestionsPagesIterator(mode, questions, testName, variant);
        while (qpi.hasNext()) {
            hb.appendText(qpi.next());
            if (qpi.hasNext()) {
                printPageBreaker(hb);
            }
        }
    }    
    
    public static String replaceHtmlEntitiesForXmlParser(String html) {
        html = html.replace("&nbsp;", "&#160;");
        html = html.replaceAll("<\\s*br\\s*>", "<br/>");
        return html;
    }
    
    private static String makeHtmlDocument(String bodyContent, String styleContent) {        
        HtmlBuilder hb = new HtmlBuilder();
        hb.startHtml(null);
        hb.startHead(); 
        hb.addTag("meta", Arrays.asList(new Attribute("http-equiv", "content-type"), new Attribute("charset", "UTF-8"), new Attribute("content", "text/html")));
        if (!StringHelper.isEmptyOrNull(styleContent)) {                   
            hb.addStyle(null, null, styleContent);            
        }
        hb.endHead();
        hb.startBody(null);
        hb.appendText(bodyContent);
        hb.endBody();
        hb.endHtml();
        return hb.toString();
    }
    
    private static Cache<String, byte[]> fontCache = CacheBuilder.from("maximumSize=1000, expireAfterWrite=10s").build();
    
    public static byte[] getFont(String fileName) {
        InputStream in = null;
        try {
            byte[] result = fontCache.getIfPresent(fileName);
            if (result == null) {
                in = PrintHelper.class.getResourceAsStream(fileName);
                result = IOUtils.toByteArray(in);
                fontCache.put(fileName, result);
            }
            return result;
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    private static String getPrintCss() {
        InputStream in = null;
        try {        
            in = PrintHelper.class.getResourceAsStream("print.css");
            String result = (new Scanner(in, "UTF-8")).useDelimiter("\\A").next();
            return result;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {               
            }
        }
    }
    
    public static void renderToPdf(String html, OutputStream pdfOutputStream) {
        try {
            html = replaceHtmlEntitiesForXmlParser(html);
            URL url = ServerHelper.saveToMembuffer(html.getBytes("UTF-8"));
            
            ITextRenderer renderer = new ITextRenderer();
            ITextUserAgent callback = new ITextUserAgent(renderer.getOutputDevice()) {
                @Override
                public byte[] getBinaryResource(String str) {                    
                    try {
                        URL uri = new URL(str);
                        byte[] result = PrintHelper.getFont(uri.getAuthority());
                        if (result == null) {
                            throw new IllegalArgumentException("Unable to load font: " + uri.getPath());
                        }                                           
                        return result;
                    }
                    catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                        return null;
                    }
                }                
            };
            callback.setSharedContext(renderer.getSharedContext()); 
            renderer.getSharedContext().setUserAgentCallback(callback);            
            renderer.setDocument(url.toString());
            renderer.layout();
            renderer.createPDF(pdfOutputStream);     
        }    
        catch (Throwable e) {
            throw new IllegalStateException(e);
        }    
    }
    
    private static int getNumberOfPagesInPdf(String html) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderToPdf(html, out);
            
            PdfReader pdfReader = new PdfReader(out.toByteArray());
            int result = pdfReader.getNumberOfPages();
            return result;
        }    
        catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }    
}