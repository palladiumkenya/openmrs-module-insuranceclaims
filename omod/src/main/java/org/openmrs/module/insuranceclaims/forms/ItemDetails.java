package org.openmrs.module.insuranceclaims.forms;

import java.math.BigDecimal;

public class ItemDetails {
    private String uuid;
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

    
}
