/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.insuranceclaims.api.service;

import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;
// import org.openmrs.module.kenyaemrml.MLinKenyaEMRConfig;
// import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.springframework.transaction.annotation.Transactional;
// import org.openmrs.ui.framework.SimpleObject;

import java.util.Collection;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface InsuranceClaimsProvidersService extends OpenmrsService {
		
	/**
	 * Saves or updates package
	 * 
	 * @param riskScore
	 * @return
	 */
	@Authorized
	@Transactional
	InsuranceClaimPackage saveOrUpdateRiskScore(InsuranceClaimPackage pkg);
	
	/**
	 * Gets a list of packages
	 * 
	 * @return
	 */
	@Transactional
	List<InsuranceClaimPackage> getAllPatientRiskScore();


}
