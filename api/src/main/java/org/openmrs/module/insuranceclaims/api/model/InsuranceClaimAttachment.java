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

    @Basic
    @Column(name = "url", length = 512)
    private String url;

    @Basic
    @Column(name = "filename", length = 255)
    private String filename;

    @Basic
    @Column(name = "retrieval_id", length = 255)
    private String retrievalId;

    @Basic
    @Column(name = "status", length = 64)
    private String status;

    @Basic
    @Column(name = "file_size")
    private Long fileSize;

    @Basic
    @Column(name = "mime_type", length = 255)
    private String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

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

    public String getDocumentTypeAsString() {
        return documentType == null ? null : documentType.name().toUpperCase();
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public void setDocumentType(String documentType) {
        DocumentType type = DocumentType.fromString(documentType);
        this.documentType = type;
    }

    public String getInterventionCode() {
        return interventionCode;
    }

    public void setInterventionCode(String interventionCode) {
        this.interventionCode = interventionCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getRetrievalId() {
        return retrievalId;
    }

    public void setRetrievalId(String retrievalId) {
        this.retrievalId = retrievalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public enum DocumentType {
        CLAIM_FORM,
        PREAUTH_FORM,
        DISCHARGE_SUMMARY,
        PRESCRIPTION,
        LAB_ORDER,
        INVOICE,
        BIO_DETAILS,
        IMAGING_ORDER,
        BIRTH_NOTIFICATION;

        public static DocumentType fromString(String value) {
            if (value == null) {
                return null;
            }
            try {
                return DocumentType.valueOf(value.trim().toUpperCase());
            } catch (Exception ex) {
                System.err.println("Insurance Claims Module: ERROR: Adding claims attachment. No such document type");
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return "InsuranceClaimAttachment [id=" + id + 
            ", claim=" + claim + ", consentToken=" + consentToken + 
            ", documentType=" + documentType + 
            ", interventionCode=" + interventionCode + 
            ", url=" + url + 
            ", filename=" + filename + 
            ", retrievalId=" + retrievalId + 
            ", status=" + status + 
            ", fileSize=" + fileSize + "]";
    }
}

