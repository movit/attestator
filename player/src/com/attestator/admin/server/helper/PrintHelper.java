package com.attestator.admin.server.helper;

import java.util.List;

import com.attestator.common.shared.helper.HtmlBuilder;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.attestator.player.server.Singletons;

public class PrintHelper {

    public static void printTest(HtmlBuilder hb, MetaTestVO metatest, int variantsCount) {
        if (variantsCount <= 0) {
            return;
        }
        
        HtmlBuilder answersHb = new HtmlBuilder();
        HtmlBuilder variantsHb = new HtmlBuilder();
        
        for (int i = 0; i < variantsCount; i++) {
            String variant = "" + (i + 1);
            List<QuestionVO> questions = Singletons.al().generateQuestionsList(metatest, false);
            printAnswers(answersHb, questions, metatest.getName(), variant);
            printTitlePage(variantsHb, "<div>{test}</div><div>Вариант &mdash; {variant}</div>", metatest, variant);
            printQuestions(variantsHb, questions, variant);            
        }
        
        hb.startTag("div", "print");
        hb.appendText(answersHb.toString());
        hb.appendText(variantsHb.toString());
        hb.endTag("div");
    }

    public static void printTitlePage(HtmlBuilder hb, String titlePage, MetaTestVO metatest, String variant) {
        printPageBreaker(hb);
        
        titlePage = titlePage.replaceAll("\\{test\\}", metatest.getName());
        titlePage = titlePage.replaceAll("\\{variant\\}", variant);
        
        hb.startTag("div", "title-page").appendText(titlePage).endTag("div");
    }

    public static void printPageBreaker(HtmlBuilder hb) {
        hb.startTag("div", "left-page").endTag("div");
    }

    public static void printAnswers(HtmlBuilder hb, List<QuestionVO> questions, String metatestName, String variant) {        
        hb.startTag("div", "answers-test-title").appendText("Ответы к тесту &laquo;" + metatestName + "&raquo;").endTag("div");
        hb.startTag("div", "answers-variant-title").appendText("Вариант &mdash; " + variant).endTag("div");
        int i = 1;
        for (QuestionVO question: questions) {
            if (question instanceof SingleChoiceQuestionVO) {
                Integer rightAnswerIndex = ReportHelper.getRightAnswerIndex((SingleChoiceQuestionVO)question);
                if (rightAnswerIndex != null) {
                    hb.startTag("div", "scq-answer").appendText(i + "-" + ReportHelper.getLatinBullet(rightAnswerIndex + 1)).endTag("div");
                    hb.appendText(" ");
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported question type: " + question.getClass());
            }
            i++;
        }
        printPageBreaker(hb);
    }

    public static void printQuestions(HtmlBuilder hb, List<QuestionVO> questions, String variant) {
        String variantPrefix = !StringHelper.isEmptyOrNull(variant) ? variant + "-" : "";
        int i = 1;
        
        for (QuestionVO question: questions) {
            hb.startTag("div", "question");
            
            String questionNo = i + " ";
            
            hb.startTag("span", "variant-no").appendText(variantPrefix).endTag("span");  
            hb.startTag("span", "question-no").appendText(questionNo).endTag("span");  
            hb.appendText( question.getText());
            hb.startTag("div", "question-description").appendText(question.getTaskDescription()).endTag("div");
            
            if (question instanceof SingleChoiceQuestionVO) {
                int j = 1;
                for (ChoiceVO choice: ((SingleChoiceQuestionVO) question).getChoices()) {
                    hb.startTag("div", "question-choice");
                    hb.appendText("&#x2610;&nbsp;" + ReportHelper.getLatinBullet(j) + ")&nbsp;");
                    hb.appendText(choice.getText());
                    hb.endTag("div");
                    j++;
                }
            }
            else {
                throw new IllegalArgumentException("Unknown question type: " + question.getClass());
            }
           
            hb.endTag("div");
            
            i++;
        }
    }
}