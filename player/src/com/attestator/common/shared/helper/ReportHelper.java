package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.common.shared.vo.AdditionalQuestionAnswerVO;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.common.shared.vo.SCQAnswerVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.google.gwt.i18n.shared.DateTimeFormat;

public class ReportHelper {
    public static enum ReportType {
        onlyHeader,
        onlyErrors,
        errorsAndNotUnswered,
        full
    }    
        
    public static double getScore(ReportVO report) {
        double score = 0;        
        for (int i = 0; i < report.getQuestions().size(); i++) {
            QuestionVO question = report.getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            score += question.getAnswerScore(answer);
        }
        return score;
    }
    
    public static int getNumErrors(ReportVO report) {
        int numErrors = 0;
        for (int i = 0; i < report.getQuestions().size(); i++) {
            QuestionVO question = report.getQuestions().get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            if (answer != null && !question.isRightAnswer(answer)) {
                numErrors++;
            }
        }
        return numErrors;
    }

    public static String getReport(ReportVO report, ReportType type) {
        updateReportStats(report);
        
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
        
        if (report.getPublication().getThisMinScore() > 0) {
            String passedStr = report.getScore() >= report.getPublication().getThisMinScore() ? "да" : "нет";
            hb.appendText("Аттестован:&nbsp;<b>" + passedStr + "</b>, ");
        }
        
        hb.appendText("Тест:&nbsp;");
        if (report.getInterruptionCause() != null) {
            switch(report.getInterruptionCause()) {
                case timerExpired:
                    hb.appendText("<b>" + "прерван - время вышло" + "</b>");
                    break;    
                case toManyErrors:
                    hb.appendText("<b>" + "прерван - слишком много ошибок" + "</b>");
                    break;
                case user:
                    hb.appendText("<b>" + "прерван испытуемым" + "</b>");
                break;
                default:
                    hb.appendText("<b>" + "прерван" + "</b>");
                break;
            }
            
        }
        else {
            if (report.isThisFinished()) {
                hb.appendText("<b>" + "завершен" + "</b>");
            }
            else {
                hb.appendText("<b>" + "не завершен" + "</b>");
            }
        }
        hb.appendText("<br/>");
        
        String totalScoreStr = "";
        if ((report.getScore() - report.getScore().intValue()) == 0) {
            totalScoreStr = "" + report.getScore().intValue();
        }
        else {
            totalScoreStr = "" + report.getScore() ;
        }
         
        
        // Score report line
        hb.appendText("Всего&nbsp;заданий:&nbsp;<b>" + report.getQuestions().size() + "</b>, ");
        hb.appendText("Выполнено:&nbsp;<b>" + report.getNumAnswers() + "</b>, ");
        hb.appendText("Ошибок:&nbsp;<b>" + report.getNumErrors() + "</b>, ");
        hb.appendText("Неотвечено:&nbsp;<b>" + report.getNumUnanswered() + "</b>, ");
        hb.appendText("Набрано&nbsp;баллов:&nbsp;<b>" + totalScoreStr + "</b>");
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

        
        for (int i = 0; i < report.getQuestions().size(); i++) {
            QuestionVO question = report.getQuestions().get(i);
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
    
    public static String formatAnswer(QuestionVO question, AnswerVO answer) {
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
    
    public static String formatQuestion(QuestionVO question) {
        return formatQuestion(question, null);
    }
    
    public static String formatQuestion(QuestionVO question, String questionPrefix) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.startTag("div", "reportTask");            
        hb.startTag("div", "reportTaskText");
        if (!StringHelper.isEmptyOrNull(questionPrefix)) {
            hb.appendText(questionPrefix);
        }
        hb.appendText(question.getText());
        hb.endTag("div");
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
    
    public static String formatNumberOfQuestions(int number) {
        if ((number - ((int)number)) != 0) {
            return number + " вопроса";
        }
        else if (number < 10 || number > 19) {
            int mod = ((int)number) % 10;
            if (mod == 1) {
                return (int)number + " вопрос";
            }
            else if (mod >= 2 && mod <= 4) {
                return (int)number + " вопроса";
            }
            else {
                return (int)number + " вопросов";
            }
        }
        else {
            return (int)number + " вопросов";
        }
    }
    
    public static String formatScore(double score) {
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
    
    public static void updateReportStats(ReportVO report) {        
        report.setNumAnswers(report.getAnswers().size());
        report.setNumUnanswered(report.getQuestions().size() - report.getAnswers().size());
        report.setNumErrors(getNumErrors(report));        
        report.setScore(getScore(report));        
    }
    
    public static double getPossibleScore(ReportVO report, int firstAllowedQuestionNo) {
        List<QuestionVO> questions = new ArrayList<QuestionVO>(report.getQuestions());
        
        if (report.getPublication().isThisAllowSkipQuestions()) {
            firstAllowedQuestionNo = 0;
        }        
        else if (firstAllowedQuestionNo < 0) {
            firstAllowedQuestionNo = questions.size();
        }        
        
        double score = 0;
        for (int i = questions.size() - 1; i >= 0; i--) {
            QuestionVO question = questions.get(i);
            AnswerVO answer = report.getAnswerByQuestionId(question.getId());
            
            // Remove question user already answered 
            // or can't answer
            if (i < firstAllowedQuestionNo || answer != null) {
                score += question.getAnswerScore(answer);
                questions.remove(i);
            }
        }
        
        double possibleScore = score;
        for (QuestionVO question: questions) {
            possibleScore += question.getScore();
        }
        
        return possibleScore;
    }
    
    public static boolean isRenewAllowed(ReportVO report) {
        if (report.isThisFinished()) {
            return false;
        }
        
        Long maxTakeTestTime = report.getPublication().getMaxTakeTestTime();
        if (maxTakeTestTime != null && report.getStart() != null) {
            Date testEnd = new Date(report.getStart().getTime() + maxTakeTestTime);
            Date now = new Date();
            if (testEnd.before(now)) {
                return false;
            }
        }
        
        return true;
    }
}