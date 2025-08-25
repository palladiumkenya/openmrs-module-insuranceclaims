package org.openmrs.module.insuranceclaims.forms;

import java.math.BigDecimal;

public class ItemDetails {
    private String uuid;
    private String interventionCode;
    private String interventionPackage;
    private BigDecimal price;
    private Integer quantity;


    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getInterventionPackage() {
        return interventionPackage;
    }

    public void setInterventionPackage(String interventionPackage) {
        this.interventionPackage = interventionPackage;
    }

    public String getInterventionCode() {
        return interventionCode;
    }

    public void setInterventionCode(String interventionCode) {
        this.interventionCode = interventionCode;
    }
}
