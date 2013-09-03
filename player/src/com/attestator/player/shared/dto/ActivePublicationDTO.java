package com.attestator.player.shared.dto;

import com.attestator.common.shared.vo.PublicationVO;

public class ActivePublicationDTO extends BaseDTO {
    private static final long serialVersionUID = 4868465484264452397L;
    private PublicationVO publication;
    private Long          attemptsLeft;
    private String        lastFullReportId;
    
    public PublicationVO getPublication() {
        return publication;
    }
    public void setPublication(PublicationVO publication) {
        this.publication = publication;
    }    
    public Long getAttemptsLeft() {
        return attemptsLeft;
    }
    public void setAttemptsLeft(Long attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }
    public String getLastFullReportId() {
        return lastFullReportId;
    }
    public void setLastFullReportId(String lastFullReportId) {
        this.lastFullReportId = lastFullReportId;
    }
}
