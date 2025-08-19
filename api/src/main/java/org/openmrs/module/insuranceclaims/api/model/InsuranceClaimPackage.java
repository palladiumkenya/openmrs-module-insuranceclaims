package org.openmrs.module.insuranceclaims.api.model;

import java.util.Date;

public class InsuranceClaimPackage extends AbstractBaseOpenmrsData {

    private Integer id;
    private String interventionCode;
    private String code;
    private String shaCategory;
    private String interventionName;
    private String accessCode;
    private String gender;
    private String subCategoryBenefitsPackage;
    private Boolean needsPreAuth;
    private String providerPaymentMechanism;
    private String appliesToSchemes;
    private Integer minAge;
    private Integer maxAge;
    private String categoryHealthFacility;
    private String tariffs;
    private Date effectiveFrom;
    private Date effectiveTill;
    private String lastModifiedBy;
    private String groupName;
    private Boolean isPerDiemClub;
    private String bedType;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getInterventionCode() {
        return interventionCode;
    }

    public void setInterventionCode(String interventionCode) {
        this.interventionCode = interventionCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShaCategory() {
        return shaCategory;
    }

    public void setShaCategory(String shaCategory) {
        this.shaCategory = shaCategory;
    }

    public String getInterventionName() {
        return interventionName;
    }

    public void setInterventionName(String interventionName) {
        this.interventionName = interventionName;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSubCategoryBenefitsPackage() {
        return subCategoryBenefitsPackage;
    }

    public void setSubCategoryBenefitsPackage(String subCategoryBenefitsPackage) {
        this.subCategoryBenefitsPackage = subCategoryBenefitsPackage;
    }

    public Boolean getNeedsPreAuth() {
        return needsPreAuth;
    }

    public void setNeedsPreAuth(Boolean needsPreAuth) {
        this.needsPreAuth = needsPreAuth;
    }

    public String getProviderPaymentMechanism() {
        return providerPaymentMechanism;
    }

    public void setProviderPaymentMechanism(String providerPaymentMechanism) {
        this.providerPaymentMechanism = providerPaymentMechanism;
    }

    public String getAppliesToSchemes() {
        return appliesToSchemes;
    }

    public void setAppliesToSchemes(String appliesToSchemes) {
        this.appliesToSchemes = appliesToSchemes;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getCategoryHealthFacility() {
        return categoryHealthFacility;
    }

    public void setCategoryHealthFacility(String categoryHealthFacility) {
        this.categoryHealthFacility = categoryHealthFacility;
    }

    public String getTariffs() {
        return tariffs;
    }

    public void setTariffs(String tariffs) {
        this.tariffs = tariffs;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTill() {
        return effectiveTill;
    }

    public void setEffectiveTill(Date effectiveTill) {
        this.effectiveTill = effectiveTill;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getIsPerDiemClub() {
        return isPerDiemClub;
    }

    public void setIsPerDiemClub(Boolean isPerDiemClub) {
        this.isPerDiemClub = isPerDiemClub;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }
}

