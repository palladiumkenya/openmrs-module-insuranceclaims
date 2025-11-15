package org.openmrs.module.insuranceclaims.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openmrs.Concept;
import org.openmrs.Patient;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class that represent a provided item.
 * Contains information about the goods and services provided by the health facility to the specific patients.
 */
@Entity(name = "iclm.ProvidedItem")
@Table(name = "iclm_provided_item")
@Inheritance(strategy = InheritanceType.JOINED)
public class ProvidedItem extends AbstractBaseOpenmrsData {

    private static final long serialVersionUID = 100458655928687702L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "iclm_provided_item_id")
    private Integer id;

    @Basic
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Basic
    @Column(name = "date_of_served")
    private Date dateOfServed;

    @Basic
    @Column
    private String intervention_package;

    @Basic
    @Column
    private String intervention_code;

    @Basic
    @Column(name = "origin_uuid")
    private String originUuid;

    @Basic
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "patient", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "item", nullable = false)
    private Concept item;

    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "bill")
    private Bill bill;

    @Basic
    @Column(name = "number_of_consumptions")
    private Integer numberOfConsumptions;

    public BigDecimal getTotalPrice() {
        if (numberOfConsumptions != null) {
            return price.multiply(new BigDecimal(numberOfConsumptions));
        } else {
            return price;
        }
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getDateOfServed() {
        return dateOfServed == null ? null : (Date) dateOfServed.clone();
    }

    public void setDateOfServed(Date dateOfServed) {
        this.dateOfServed = dateOfServed == null ? null : (Date) dateOfServed.clone();
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Concept getItem() {
        return item;
    }

    public void setItem(Concept item) {
        this.item = item;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Integer getNumberOfConsumptions() {
        return numberOfConsumptions;
    }

    public void setNumberOfConsumptions(Integer numberOfConsumptions) {
        this.numberOfConsumptions = numberOfConsumptions;
    }

    public String getOriginUuid() {
        return originUuid;
    }

    public void setOriginUuid(String originUuid) {
        this.originUuid = originUuid;
    }

    public String getInterventionPackage() {
        return intervention_package;
    }

    public void setInterventionPackage(String intervention_package) {
        this.intervention_package = intervention_package;
    }

    public String getInterventionCode() {
        return intervention_code;
    }

    public void setInterventionCode(String intervention_code) {
        this.intervention_code = intervention_code;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(this, o, "id", "uuid");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "id", "uuid");
    }

    @Override
    public String toString() {
        return "ProvidedItem [id=" + id + ", price=" + price + ", dateOfServed=" + dateOfServed
                + ", intervention_package=" + intervention_package + ", intervention_code=" + intervention_code
                + ", originUuid=" + originUuid + ", status=" + status + ", patient=" + patient + ", item=" + item
                + ", bill=" + bill + ", numberOfConsumptions=" + numberOfConsumptions + "]";
    }

    
}
