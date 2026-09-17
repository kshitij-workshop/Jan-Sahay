package com.govscheme.matching.dto;

public class MatchSummaryResponse {

    private long total;
    private long eligible;
    private long insufficientInformation;
    private long notEligible;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getEligible() { return eligible; }
    public void setEligible(long eligible) { this.eligible = eligible; }

    public long getInsufficientInformation() { return insufficientInformation; }
    public void setInsufficientInformation(long insufficientInformation) {
        this.insufficientInformation = insufficientInformation;
    }

    public long getNotEligible() { return notEligible; }
    public void setNotEligible(long notEligible) { this.notEligible = notEligible; }
}
