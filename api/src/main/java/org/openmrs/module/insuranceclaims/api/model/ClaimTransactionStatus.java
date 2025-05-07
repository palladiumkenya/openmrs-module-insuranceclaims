
package org.openmrs.module.insuranceclaims.api.model;

import java.sql.Timestamp;

public class ClaimTransactionStatus {
    private Integer id;
    private String claimId;
    private Integer approvedAmount;
    private String status;
    private String commentFromApprover;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public Integer getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(Integer approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommentFromApprover() {
        return commentFromApprover;
    }

    public void setCommentFromApprover(String comment) {
        this.commentFromApprover = comment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

}
