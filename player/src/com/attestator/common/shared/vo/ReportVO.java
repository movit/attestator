package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.google.code.morphia.annotations.Entity;

@Entity("report")
public class ReportVO extends TenantableVO {
    private static final long serialVersionUID = -5907963588232512986L;

    private PublicationVO publication;

    private String firstName;
    private String lastName;
    private String middleName;
    private String email;

    private List<AdditionalQuestionAnswerVO> additionalAnswers = new ArrayList<AdditionalQuestionAnswerVO>();

    private Date start;
    private Date end;

    private Boolean finished;
    private Boolean interrupted;

    private String clientId;
    private String host;

    // Calculated fields
    private Double score;
    private Integer numErrors;
    private Integer numAnswers;
    private Integer numUnanswered;
    private String metatestName;

    private List<QuestionVO> questions = new ArrayList<QuestionVO>();
    private List<AnswerVO> answers = new ArrayList<AnswerVO>();

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public PublicationVO getPublication() {
        return publication;
    }

    public void setPublication(PublicationVO publication) {
        this.publication = publication;
    }

    public List<AnswerVO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerVO> answers) {
        this.answers = answers;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFullName() {
        return StringHelper.concatAllNotEmpty(" ", lastName, firstName,
                middleName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AdditionalQuestionAnswerVO> getAdditionalAnswers() {
        return additionalAnswers;
    }

    public void setAdditionalAnswers(
            List<AdditionalQuestionAnswerVO> additionalAnswers) {
        this.additionalAnswers = additionalAnswers;
    }

    public AnswerVO getAnswerByQuestionId(String questionId) {
        for (AnswerVO answer : answers) {
            if (NullHelper.nullSafeEquals(answer.getQuestionId(), questionId)) {
                return answer;
            }
        }
        return null;
    }

    public boolean isQuestionAnswered(String questionId) {
        return getAnswerByQuestionId(questionId) != null;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isThisFinished() {
        return NullHelper.nullSafeTrue(finished);
    }
    
    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getNumErrors() {
        return numErrors;
    }

    public void setNumErrors(Integer numErrors) {
        this.numErrors = numErrors;
    }

    public Integer getNumAnswers() {
        return numAnswers;
    }

    public void setNumAnswers(Integer numAnswers) {
        this.numAnswers = numAnswers;
    }

    public String getMetatestName() {
        return metatestName;
    }

    public void setMetatestName(String metatestName) {
        this.metatestName = metatestName;
    }

    public Integer getNumUnanswered() {
        return numUnanswered;
    }

    public void setNumUnanswered(Integer numUnanswered) {
        this.numUnanswered = numUnanswered;
    }

    public boolean isThisInterrupted() {
        return NullHelper.nullSafeTrue(interrupted);
    }
    
    public Boolean getInterrupted() {
        return interrupted;
    }

    public void setInterrupted(Boolean interrupted) {
        this.interrupted = interrupted;
    }

    public List<QuestionVO> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionVO> questions) {
        this.questions = questions;
    }

    public QuestionVO getQuestion(String questionId) {
        for (QuestionVO question: questions) {
            if (NullHelper.nullSafeEquals(question.getId(), questionId)) {
                return question;
            }
        }
        return null;
    }
}
