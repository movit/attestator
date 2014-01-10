package com.attestator.admin.server.helper.print;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    
    private static final String COMMON_CSS = getPrintCss("print_common.css");
    private static final String PDF_PAGE_DIMESIONS_CSS = getPrintCss("print_page_dimensions_pdf.css");
    private static final String PAPER_PAGE_DIMESIONS_CSS = getPrintCss("print_page_dimensions_paper.css");
    private static final String TEST_PAGE_DIMESIONS_CSS = getPrintCss("print_page_dimensions_test.css");
    private static final String PAPER_FONT_SIZE_CSS = getPrintCss("print_font_size_paper.css");
    private static final String PDF_FONT_SIZE_CSS = getPrintCss("print_font_size_pdf.css");
    
    public static enum PageMode {
        singlePage,
        doublePage
    }
    
    public static enum PrintingMedia {
        paper,
        pdf
    }
    
    public abstract static class PagesIterator implements Iterator<String> {
        protected PageMode          mode;
        protected PrintingMedia     media;
        protected List<QuestionVO>  questions;
        protected String            testCss;
        protected int               page = 0;
        protected int               questionIndex = 0;
        
        public PagesIterator(PageMode mode, PrintingMedia media, List<QuestionVO> questions) {
            this.mode      = mode;
            this.media     = media;
            this.questions = questions;
            
            testCss = COMMON_CSS + PDF_FONT_SIZE_CSS + TEST_PAGE_DIMESIONS_CSS;
        }
        
        @Override
        public boolean hasNext() {
            return questionIndex < questions.size() || (MathHelper.isOdd(page) && mode == PageMode.doublePage);
        }              
        
        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            // No more questions to iterate but we need return extra empty page for double page mode
            if (questionIndex == questions.size()) {
                page++;
                return "";
            }
            
            String result = null;
            HtmlBuilder candidate = new HtmlBuilder();                        
            
            for (;questionIndex < questions.size(); questionIndex++) {                                
                // Prepare test HTML
                addQuestionContent(candidate, questionIndex);                
                String htmlForTest = makeHtmlDocument(candidate.toString(), testCss);
                
                if (getNumberOfPagesInPdf(htmlForTest) == 1) {
                    // Store previous candidate
                    result = candidate.toString();
                }
                else {
                    // No more content should be added
                    break;
                }
            }
            
            // No questions was actually added
            // Even first question is bigger than one page
            if (result == null) {
                result = candidate.toString();
            }
            page++;
            
            return result;
        }
        
        public int getPage() {
            return page;
        }
        
        protected abstract void addQuestionContent(HtmlBuilder hb, int questionNo);

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }        
    }
    
    public static class AnswersPagesIterator extends PagesIterator {             
        public AnswersPagesIterator(PageMode mode, PrintingMedia media, List<QuestionVO> questions) {
            super(mode, media, questions);
        }

        @Override
        protected void addQuestionContent(HtmlBuilder hb, int questionNo) {
            printAnswer(hb, questions.get(questionNo), "" + (questionNo + 1));
        }
    }
    
    public static class QuestionsPagesIterator extends PagesIterator {        
        public QuestionsPagesIterator(PageMode mode, PrintingMedia media, List<QuestionVO> questions) {
            super(mode, media, questions);            
        }

        @Override
        protected void addQuestionContent(HtmlBuilder hb, int questionNo) {
            printQuestion(hb, questions.get(questionNo), "" + (questionNo + 1));
        }
    }
    
    
    private static void printSingleTestVariant(MetaTestVO metatest, PrintingPropertiesVO properties, HtmlBuilder answersHb, HtmlBuilder variantsHb, PageMode mode, PrintingMedia media, boolean breakAfterLastPage, String varantNo) {
        String variant = "" + properties.getPrintAttempt() + "-" + varantNo;
        
        List<QuestionVO> questions = Singletons.al().generateQuestionsList(metatest, properties.isThisRandomQuestionsOrder());            
        
        printAnswers(answersHb, mode, media, true, questions, metatest.getName(), variant);        
        printTitlePage(variantsHb, mode, properties.getTitlePage(), metatest, variant);            
        printQuestions(variantsHb, mode, media, breakAfterLastPage, questions, metatest.getName(), variant);
    }
    
    public static String printTest(MetaTestVO metatest, PrintingPropertiesVO properties, PrintingMedia media) {
        return printTest(metatest, properties, null, media);
    }
    
    public static String printTest(MetaTestVO metatest, PrintingPropertiesVO properties, String variantNo, PrintingMedia media) {
        PageMode mode = properties.isThisDoublePage() ? PageMode.doublePage : PageMode.singlePage;
        
        HtmlBuilder answersHb = new HtmlBuilder();
        HtmlBuilder variantsHb = new HtmlBuilder();
        
        if (variantNo == null) {
            // Print all variants            
            for (int i = 0; i < properties.getVariantsCount(); i++) {
                boolean breakAfterLastPage = i < (properties.getVariantsCount() - 1);
                printSingleTestVariant(metatest, properties, answersHb, variantsHb, mode, media, breakAfterLastPage, "" + (i + 1));
            }
        }
        else {
            // Print single variant
            printSingleTestVariant(metatest, properties, answersHb, variantsHb, mode, media, false, variantNo);
        }
        
        HtmlBuilder hb = new HtmlBuilder();
               
        hb.appendText(answersHb.toString());
        hb.appendText(variantsHb.toString());
        
        String css = (media == PrintingMedia.paper) ? 
                COMMON_CSS + PAPER_FONT_SIZE_CSS + PAPER_PAGE_DIMESIONS_CSS : 
                COMMON_CSS + PDF_FONT_SIZE_CSS + PDF_PAGE_DIMESIONS_CSS;
        
        String result = makeHtmlDocument(hb.toString(), css);
        
        return result;
    }

    public static void printPageHeader(HtmlBuilder hb, String text) {
        if (StringHelper.isEmptyOrNull(text)) {
            text = "&nbsp;";
        }
        hb.startTag("div", "header").appendText(text).endTag("div");
    }
    
    public static void printPageFooter(HtmlBuilder hb, String text, boolean breakPage) {
        if (StringHelper.isEmptyOrNull(text)) {
            text = "&nbsp;";
        }
        String clazz = breakPage ? "footer page-breaker" : "footer";
        hb.startTag("div", clazz).appendText(text).endTag("div");
    }

    public static void printPageBreaker(HtmlBuilder hb) {
        printPageFooter(hb, null, true);
    }
    
    private static void printPageContent(HtmlBuilder hb, String content) {
        hb.startTag("div", "content");
        hb.appendText(content);
        hb.endTag("div");
    }

    public static void printAnswer(HtmlBuilder hb, QuestionVO question, String questionNo) {
        if (question instanceof SingleChoiceQuestionVO) {
            Integer rightAnswerIndex = ReportHelper.getRightAnswerIndex((SingleChoiceQuestionVO)question);
            if (rightAnswerIndex != null) {
                hb.startTag("div", "scq-answer").appendText(questionNo + "-" + ReportHelper.getLatinBullet(rightAnswerIndex + 1)).endTag("div");
                hb.appendText(" ");
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported question type: " + question.getClass());
        }   
    }
    
    public static void printQuestion(HtmlBuilder hb, QuestionVO question, String questionNo) {
        hb.startTag("div", "question");
        
        hb.startTag("div", "question-text");
        hb.startTag("span", "question-no").appendText(questionNo + " ").endTag("span");  
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

    public static void printTitlePage(HtmlBuilder hb, PageMode mode, String titlePageContent, MetaTestVO metatest, String variant) {        
        titlePageContent = titlePageContent.replaceAll("\\{test\\}", metatest.getName());
        titlePageContent = titlePageContent.replaceAll("\\{variant\\}", variant);
        
        printPageContent(hb, titlePageContent);
                
        if (mode == PageMode.doublePage) {
            printPageBreaker(hb);
        }
        
        printPageBreaker(hb);
    }

    public static void printAnswers(HtmlBuilder hb, 
            PageMode mode, PrintingMedia media, boolean breakAfterLastPage, 
            List<QuestionVO> questions, String metatestName, String variantName) {
        
        String headerText = "<b>Вариант " + variantName + ", ответы </b> " + metatestName;
        AnswersPagesIterator api = new AnswersPagesIterator(mode, media, questions);
        while (api.hasNext()) {
            String pageContent = api.next();            
            printPageHeader(hb, headerText);                
            printPageContent(hb, pageContent);
            printPageFooter(hb, "" + api.getPage(), breakAfterLastPage || api.hasNext());
        }
    }
    
    public static void printQuestions(HtmlBuilder hb, 
            PageMode mode, PrintingMedia media, boolean breakAfterLastPage, 
            List<QuestionVO> questions, String metatestName, String variantName) {
        
        String headerText = "<b>Вариант " + variantName + "</b> " + metatestName;
        QuestionsPagesIterator qpi = new QuestionsPagesIterator(mode, media, questions);
        while (qpi.hasNext()) {
            String pageContent = qpi.next();            
            printPageHeader(hb, headerText);                
            printPageContent(hb, pageContent);
            printPageFooter(hb, "" + qpi.getPage(), breakAfterLastPage || qpi.hasNext());
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
    
    private static String getPrintCss(String file) {
        InputStream in = null;
        try {        
            in = PrintHelper.class.getResourceAsStream(file);
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
            
            File f = new File("C:\\\\test\\" + System.currentTimeMillis() + ".pdf");
            FileOutputStream fout = new FileOutputStream(f);
            fout.write(out.toByteArray());
            fout.close();
            
            PdfReader pdfReader = new PdfReader(out.toByteArray());
            int result = pdfReader.getNumberOfPages();
            return result;
        }    
        catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }    
}