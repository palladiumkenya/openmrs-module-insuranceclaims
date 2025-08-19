/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.insuranceclaims.api.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimPackagesDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimsProvidersService;
// import org.openmrs.module.kenyaemr.api.KenyaEmrService;
// import org.openmrs.module.kenyaemrml.ModuleConstants;
// import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
// import org.openmrs.module.kenyaemrml.api.ModelService;
// import org.openmrs.module.kenyaemrml.api.db.hibernate.HibernateMLinKenyaEMRDao;
// import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
// import org.openmrs.ui.framework.SimpleObject;

public class InsuranceClaimsProvidersServiceImpl extends BaseOpenmrsService implements InsuranceClaimsProvidersService {
	
	InsuranceClaimPackagesDao insuranceClaimPackagesDao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setInsuranceClaimPackagesDao(InsuranceClaimPackagesDao insuranceClaimPackagesDao) {
		this.insuranceClaimPackagesDao = insuranceClaimPackagesDao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public InsuranceClaimPackage saveOrUpdateRiskScore(InsuranceClaimPackage pkg) {
		// saveIITRiskScoreAsAnObs(riskScore);
		return insuranceClaimPackagesDao.saveOrUpdatePackage(pkg);
	}
	
	@Override
	public List<InsuranceClaimPackage> getAllPackages() {
		return insuranceClaimPackagesDao.getAllPackages();
	}

}
