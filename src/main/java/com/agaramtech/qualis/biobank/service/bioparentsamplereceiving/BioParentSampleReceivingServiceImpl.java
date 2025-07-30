package com.agaramtech.qualis.biobank.service.bioparentsamplereceiving;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'bioparentsamplereceiving' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class BioParentSampleReceivingServiceImpl implements BioParentSampleReceivingService {
	
	private final BioParentSampleReceivingDAO bioParentSampleReceivingDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param bioParentSampleReceivingDAO BioParentSampleReceivingDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public BioParentSampleReceivingServiceImpl(BioParentSampleReceivingDAO bioParentSampleReceivingDAO, CommonFunction commonFunction) {
		this.bioParentSampleReceivingDAO = bioParentSampleReceivingDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getBioParentSampleReceiving(final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getBioParentSampleReceiving(userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioParentSampleReceiving(final int nbioparentsamplecode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getActiveBioParentSampleReceiving(nbioparentsamplecode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getDiseaseforLoggedInSite(final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getDiseaseforLoggedInSite(userInfo);
	}
	
	
	@Override
	public ResponseEntity<Object> getBioProjectforLoggedInSite(final int ndiseasecode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getBioProjectforLoggedInSite(ndiseasecode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getCollectionSiteBasedonProject(final int nbioprojectcode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getCollectionSiteBasedonProject(nbioprojectcode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getHospitalBasedonSite(final int ncollectionsitecode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getHospitalBasedonSite(ncollectionsitecode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getStorageStructureBasedonSite(final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getStorageStructureBasedonSite(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> validateSubjectID(final String ssubjectid,final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.validateSubjectID(ssubjectid, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createBioParentSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.createBioParentSampleReceiving(inputMap, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateBioParentSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.updateBioParentSampleReceiving(inputMap, userInfo);
	}
		
	@Override
	public ResponseEntity<Object> getActiveBioParentSampleCollection(final int nbioparentsamplecollectioncode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.getActiveBioParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> createParentSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.createParentSampleCollection(inputMap, userInfo);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> updateParentSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.updateParentSampleCollection(inputMap, userInfo);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> deleteParentSampleCollection(final int nbioparentsamplecollectioncode, final int nbioparentsamplecode, final UserInfo userInfo) throws Exception{
		return bioParentSampleReceivingDAO.deleteParentSampleCollection(nbioparentsamplecollectioncode, nbioparentsamplecode, userInfo);
	}

}
