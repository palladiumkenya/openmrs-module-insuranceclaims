package org.openmrs.module.insuranceclaims.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Model class that represents an insurance claim attachment.
 */
@Entity(name = "iclm.ClaimAttachment")
@Table(name = "iclm_claim_attachment")
@Inheritance(strategy = InheritanceType.JOINED)
public class InsuranceClaimAttachment extends AbstractBaseOpenmrsData {

    private static final long serialVersionUID = -725114567231987654L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "iclm_claim_attachment_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "iclm_claim_id", nullable = false)
    @JsonIgnore
    private InsuranceClaim claim;

    @Basic
    @Column(name = "consent_token", length = 255, nullable = false)
    private String consentToken;

    @Lob
    @Column(name = "file_blob", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileBlob;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 64, nullable = false)
    private DocumentType documentType;

    @Basic
    @Column(name = "intervention_code", length = 255, nullable = false)
    private String interventionCode;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public InsuranceClaim getClaim() {
        return claim;
    }

    public void setClaim(InsuranceClaim claim) {
        this.claim = claim;
    }

    public String getConsentToken() {
        return consentToken;
    }

    public void setConsentToken(String consentToken) {
        this.consentToken = consentToken;
    }

    public byte[] getFileBlob() {
        return fileBlob;
    }

    public void setFileBlob(byte[] fileBlob) {
        this.fileBlob = fileBlob;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getInterventionCode() {
        return interventionCode;
    }

    public void setInterventionCode(String interventionCode) {
        this.interventionCode = interventionCode;
    }

    @Override
    public String toString() {
        return "InsuranceClaimAttachment [id=" + id +
                ", claim=" + (claim != null ? claim.getId() : null) +
                ", consentToken=" + consentToken +
                ", documentType=" + documentType +
                ", interventionCode=" + interventionCode + "]";
    }

    public enum DocumentType {
        CLAIM_FORM,
        PREAUTH_FORM,
        DISCHARGE_SUMMARY,
        PRESCRIPTION,
        LAB_ORDER,
        INVOICE,
        BIO_DETAILS,
        IMAGING_ORDER
    }
}

