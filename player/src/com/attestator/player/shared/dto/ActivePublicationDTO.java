package com.attestator.player.shared.dto;

import com.attestator.common.shared.vo.PublicationVO;

public class ActivePublicationDTO extends BaseDTO{
    private static final long serialVersionUID = 4868465484264452397L;
    private PublicationVO publication;
    private long          numberOfAttempts;
    private String        lastFullReportId;
    
    public PublicationVO getPublication() {
        return publication;
    }
    public void setPublication(PublicationVO publication) {
        this.publication = publication;
    }
    public long getNumberOfAttempts() {
        return numberOfAttempts;
    }
    public void setNumberOfAttempts(long numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }
    public String getLastFullReportId() {
        return lastFullReportId;
    }
    public void setLastFullReportId(String lastFullReportId) {
        this.lastFullReportId = lastFullReportId;
    }
}
