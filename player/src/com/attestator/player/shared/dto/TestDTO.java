package com.attestator.player.shared.dto;

import java.util.List;

import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;

public class TestDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;
    private PublicationVO       publication;
    private List<QuestionVO>    questions;
    
    public PublicationVO getPublication() {
        return publication;
    }
    public void setPublication(PublicationVO publication) {
        this.publication = publication;
    }
    public List<QuestionVO> getQuestions() {
        return questions;
    }
    public void setQuestions(List<QuestionVO> questions) {
        this.questions = questions;
    }
}
