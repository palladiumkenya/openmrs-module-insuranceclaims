package org.openmrs.module.insuranceclaims.forms;

import java.util.List;

public class ProvidedItemInForm {
    private List<ItemDetails> items;
    private String explanation;
    private String justification;

    public List<ItemDetails> getItems() {
        return items;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getJustification() {
        return justification;
    }

    public void setItems(List<ItemDetails> items) {
        this.items = items;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
