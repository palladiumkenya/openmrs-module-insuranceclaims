package org.openmrs.module.insuranceclaims.api.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.insuranceclaims.api.dao.ProvidedItemDao;
import org.openmrs.module.insuranceclaims.api.model.Bill;
import org.openmrs.module.insuranceclaims.api.model.ProcessStatus;
import org.openmrs.module.insuranceclaims.api.model.ProvidedItem;
import org.openmrs.module.insuranceclaims.api.service.ProvidedItemService;

import liquibase.pro.packaged.T;

import java.util.List;

public class ProvidedItemServiceImpl extends BaseOpenmrsDataService<ProvidedItem> implements ProvidedItemService {

    private ProvidedItemDao providedItemDao;

    public void setProvidedItemDao(ProvidedItemDao providedItemDao) {
        this.providedItemDao = providedItemDao;
    }

    public ProvidedItemDao getProvidedItemDao() {
        return providedItemDao;
    }

    @Override
    public List<ProvidedItem> getProvidedItems(Integer patientId, ProcessStatus processStatus) {
        return providedItemDao.getProvidedItems(patientId, processStatus);
    }

    @Override
    public List<ProvidedItem> getProvidedEnteredItems(Integer patientId) {
        return providedItemDao.getProvidedItems(patientId, ProcessStatus.ENTERED);
    }

    @Override
    public void updateStatusProvidedItems(List<ProvidedItem> providedItems, Bill bill) {
        for (ProvidedItem item : providedItems) {
            item.setBill(bill);
            item.setStatus(ProcessStatus.PROCESSED);
            System.err.println("Insurance Claims Module: Saving ProvidedItem: " + item);
            providedItemDao.saveOrUpdate(item);
        }
    }

    @Override
	public ProvidedItem saveOrUpdate(ProvidedItem providedItem) throws APIException {
        System.err.println("Insurance Claims Module: Saving ProvidedItem: " + providedItem);
		return providedItemDao.saveOrUpdate(providedItem);
	}
}
