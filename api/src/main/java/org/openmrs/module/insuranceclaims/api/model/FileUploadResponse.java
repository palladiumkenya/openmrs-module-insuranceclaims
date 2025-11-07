package org.openmrs.module.insuranceclaims.api.model;

public class FileUploadResponse {

    private String url;
    private String fileName;
    private String retrievalId;
    private String status;
    private Boolean success = false;

    // Default constructor
    public FileUploadResponse() {
    }

    // All-args constructor
    public FileUploadResponse(String url, String fileName, String retrievalId, String status) {
        this.url = url;
        this.fileName = fileName;
        this.retrievalId = retrievalId;
        this.status = status;
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "FileUploadResponse [url=" + url + ", fileName=" + fileName + ", retrievalId=" + retrievalId
                + ", status=" + status + ", success=" + success + "]";
    }
}

