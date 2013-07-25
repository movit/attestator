package com.attestator.common.shared.helper;

import com.attestator.common.shared.vo.AdditionalQuestionAnswerVO;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SCQAnswerVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.google.gwt.i18n.shared.DateTimeFormat;

public class TestHelper {
    public static enum ReportType {
        onlyHeader,
        onlyErrors,
        errorsAndNotUnswered,
        full
    }    
    
    public static double getScore(ReportVO report) {
        double score = 0;        
        for (int i = 0; i < report.getPublication().getQuestions().size(); i++) {
            QuestionVO question = report.getPublication().getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            score += question.getAnswerScore(answer);
        }
        return score;
    }
    
    public static int getNumErrors(ReportVO report) {
        int numErrors = 0;
        for (int i = 0; i < report.getPublication().getQuestions().size(); i++) {
            QuestionVO question = report.getPublication().getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            if (answer != null && !question.isRightAnswer(answer)) {
                numErrors++;
            }
        }
        return numErrors;
    }

    public static String getReport(ReportVO report, ReportType type) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.startTag("div", "report");
        hb.startTag("div", "reportTitle").appendText("Отчет о прохождении теста").endTag("div");
        hb.startTag("div", "reportTestTitle").appendText(report.getPublication().getMetatest().getName()).endTag("div");
        hb.startTag("div", "reportSummary");
        
        String name = StringHelper.concatAllNotEmpty(" ", 
                report.getLastName(), 
                report.getFirstName(), 
                report.getMiddleName());
        
        if (StringHelper.isNotEmptyOrNull(name)) {
            hb.startTag("div", "reportStudentName").appendText(name).endTag("div");
        }

        int numErrors = 0;
        double score = 0;        
        for (int i = 0; i < report.getPublication().getQuestions().size(); i++) {
            QuestionVO question = report.getPublication().getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            score += question.getAnswerScore(answer);
            if (!question.isRightAnswer(answer)) {
                numErrors++;
            }
        }
        
        String totalScoreStr = "";
        if ((score - ((int)score)) == 0) {
            totalScoreStr = "" + (int)score;
        }
        else {
            totalScoreStr = "" + score;
        }
        
        
        // Score report line
        hb.appendText("Всего&nbsp;заданий:&nbsp;<b>" + report.getPublication().getQuestions().size() + "</b>, ");
        hb.appendText("выполнено:&nbsp;<b>" + report.getAnswers().size() + "</b>, ");
        hb.appendText("ошибок:&nbsp;<b>" + numErrors + "</b>, ");
        hb.appendText("набрано&nbsp;баллов:&nbsp;<b>" + totalScoreStr + "</b>");
        hb.appendText("<br/>");
        
        // Other answers report line
        boolean otherAnswers = false;
        if (StringHelper.isNotEmptyOrNull(report.getEmail())) {
            hb.appendText("Email:&nbsp;<b>"+ report.getEmail() + "</b>");
            otherAnswers = true;
        }
        
        for (AdditionalQuestionAnswerVO addAnswer: report.getAdditionalAnswers()) {            
            String answer = "<не введено>";
            if (!StringHelper.isEmptyOrNull(addAnswer.getAnswer())) {
                answer = addAnswer.getAnswer();
            }
            String question = addAnswer.getQuestion();
            question = question.replaceAll("\\s", "&nbsp;");
            if (otherAnswers) {
                hb.appendText(", ");
            }
            hb.appendText(question + ":&nbsp;<b>" + answer + "</b>");
            otherAnswers = true;
        }
        if (otherAnswers) {
            hb.appendText("<br/>");
        }        
        
        DateTimeFormat dateTimeFormatter = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
        DateTimeFormat timeFormater = DateTimeFormat.getFormat("HH:mm:ss");
        if (report.getStart() != null) {
            hb.appendText("Начало:&nbsp;").appendText("<b>" + dateTimeFormatter.format(report.getStart()) + "</b>");
            
            if (report.getEnd() != null) {
                String finish = "";
                if (DateHelper.isTheSameDate(report.getStart(), report.getEnd())) {
                    finish = timeFormater.format(report.getEnd());
                }
                else {
                    finish = dateTimeFormatter.format(report.getEnd());
                }
                hb.appendText(", " + "окончание:&nbsp;").appendText("<b>" + finish + "</b>");
                
                long duration = (report.getEnd().getTime() - report.getStart().getTime()) / 1000;
                hb.appendText(", продолжительность:&nbsp;<b>" + DateHelper.formatTimeValue(duration) + "</b>");
            }
        }
        
        //reportSummary
        hb.endTag("div");
        
        
        if (type == ReportType.onlyHeader) {
            return hb.toString();
        }

        
        for (int i = 0; i < report.getPublication().getQuestions().size(); i++) {
            QuestionVO question = report.getPublication().getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            
            if (answer == null && type != ReportType.full && type != ReportType.errorsAndNotUnswered) {
                continue;
            }            
            if (question.isRightAnswer(answer) && type != ReportType.full) {
                continue;
            }
            
            String scoreStr = formatScore(question.getAnswerScore(answer));
            
            hb.startTag("div", "reportTaskTitle").appendText("Задание " + (i + 1)).endTag("div");
            hb.startTag("div", question.isRightAnswer(answer) ? "reportScore" : "reportScore reportIncorrect");
            if (answer == null) {
                hb.appendText("Невыполнено, " + scoreStr);
            }
            else if (!question.isRightAnswer(answer)) {
                hb.appendText("Ошибка, " + scoreStr);
            }
            else if (type == ReportType.full) {
                hb.appendText("Правильно, " + scoreStr);
            }
            hb.endTag("div");

            hb.appendText(formatQuestion(question));
            
            AnswerVO rightAnswer = question.getRightAnswer();
            hb.startTable(0, 0, "reportTaskAnswers");
            hb.startTag("tr");
            hb.startTag("th", "reportAnswerKind").appendText("Правильно:").endTag("th");
            hb.startTag("td", "reportChoice").appendText(formatAnswer(question, rightAnswer)).endTag("td");
            hb.endTag("tr");
            if (answer != null) {
                hb.startTag("tr");
                hb.startTag("th", "reportAnswerKind").appendText("Дан ответ:").endTag("th");
                hb.startTag("td", "reportChoice").appendText(formatAnswer(question, answer)).endTag("td");
                hb.endTag("tr");
            }            
            hb.endTable();
        }

        hb.endTag("div");
        
        return hb.toString();
    }
    
    private static String formatAnswer(QuestionVO question, AnswerVO answer) {
        HtmlBuilder hb = new HtmlBuilder();
        if (question instanceof SingleChoiceQuestionVO) {
            int i = 1;            
            for (ChoiceVO choice: ((SingleChoiceQuestionVO) question).getChoices()) {
                if (choice.getId() == ((SCQAnswerVO)answer).getChoiceId()) {
                    hb.startTable(0, 0, null, "0", "100%");
                    hb.startTag("tr");
                    hb.startTag("th", "reportChoiceNo").appendText(i + ".").endTag("th");
                    hb.startTag("td", "reportChoice").appendText(choice.getText()).endTag("td");
                    hb.endTag("tr");
                    hb.endTable();
                    break;
                }
                i++;
            }
        }
        return hb.toString();
    }
    
    private static String formatQuestion(QuestionVO question) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.startTag("div", "reportTask");            
        hb.startTag("div", "reportTaskText").appendText(question.getText()).endTag("div");
        hb.startTag("div", "reportTaskDescription").appendText(question.getTaskDescription()).endTag("div");
        hb.startTag("div", "reportVariants");
        hb.startTable(0, 0, null, "0", "100%");
        if (question instanceof SingleChoiceQuestionVO) {
            
            int i = 1;            
            for (ChoiceVO choice: ((SingleChoiceQuestionVO) question).getChoices()) {
                hb.startTag("tr");
                hb.startTag("th", "reportChoiceNo").appendText(i + ".").endTag("th");
                hb.startTag("td", "reportChoice").appendText(choice.getText()).endTag("td");
                hb.endTag("tr");
                i++;
            }
            
        }
        hb.endTable();
        hb.endTag("div");
        hb.endTag("div");
        return hb.toString();
    }
    
    
    private static String formatScore(double score) {
        if ((score - ((int)score)) != 0) {
            return score + " балла";
        }
        else if (score < 10 || score > 19) {
            int mod = ((int)score) % 10;
            if (mod == 1) {
                return (int)score + " балл";
            }
            else if (mod >= 2 && mod <= 4) {
                return (int)score + " балла";
            }
            else {
                return (int)score + " баллов";
            }
        }
        else {
            return (int)score + " баллов";
        }
    }
}