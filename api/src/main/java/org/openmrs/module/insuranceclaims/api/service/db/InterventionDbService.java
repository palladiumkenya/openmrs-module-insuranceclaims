package org.openmrs.module.insuranceclaims.api.service.db;

import java.util.List;

import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;

public interface InterventionDbService {

    List<InsuranceClaimIntervention> findInsuranceClaimIntervention(int insuranceClaimId);
}
