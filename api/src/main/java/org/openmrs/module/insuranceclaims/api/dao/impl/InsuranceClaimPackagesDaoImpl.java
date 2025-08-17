package org.openmrs.module.insuranceclaims.api.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimPackagesDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;

public class InsuranceClaimPackagesDaoImpl implements InsuranceClaimPackagesDao {

    private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}

    @Override
    public InsuranceClaimPackage saveOrUpdatePackage(InsuranceClaimPackage pkg) {
        getSession().saveOrUpdate(pkg);
		return pkg;
    }

    @Override
    public List<InsuranceClaimPackage> getAllPackages() {
        return (List<InsuranceClaimPackage>) getSession().createCriteria(InsuranceClaimPackage.class).list();
    }
    
}
