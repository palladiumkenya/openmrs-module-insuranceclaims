package org.openmrs.module.insuranceclaims.api.model;

import java.time.ZonedDateTime;

public class SupportingDocuments {

    private String name;
    private String type;
    private long size;
    private String base64;
    private String purpose;
    private String intervention;
    private ZonedDateTime uploadedAt;

    public SupportingDocuments() {
    }

    public SupportingDocuments(String name, String type, long size, String base64, ZonedDateTime uploadedAt) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.base64 = base64;
        this.uploadedAt = uploadedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getIntervention() {
        return intervention;
    }

    public void setIntervention(String intervention) {
        this.intervention = intervention;
    }

    public ZonedDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(ZonedDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "SupportingDocuments [name=" + name + ", type=" + type + ", size=" + size + ", purpose=" + purpose
                + ", intervention=" + intervention + ", uploadedAt=" + uploadedAt + "]";
    }
}
