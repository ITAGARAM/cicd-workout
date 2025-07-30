package com.agaramtech.qualis.biobank.service.bioparentsamplereceiving;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.global.UserInfo;

public interface BioParentSampleReceivingService {

	ResponseEntity<Object> getBioParentSampleReceiving(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getActiveBioParentSampleReceiving(int nbioparentsamplecode, UserInfo userInfo)
			throws Exception;
	
	ResponseEntity<Object> getDiseaseforLoggedInSite(final UserInfo userInfo) throws Exception;
	
	ResponseEntity<Object> getBioProjectforLoggedInSite(final int ndiseasecode, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getCollectionSiteBasedonProject(final int nbioprojectcode,final  UserInfo userInfo)throws Exception;

	ResponseEntity<Object> getHospitalBasedonSite(final int ncollectionsitecode,final  UserInfo userInfo)throws Exception;

	ResponseEntity<Object> getStorageStructureBasedonSite(final  UserInfo userInfo)throws Exception;

	ResponseEntity<Object> validateSubjectID(final String ssubjectid, final UserInfo userInfo)throws Exception;

	ResponseEntity<Object> createBioParentSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception;

	ResponseEntity<Object> updateBioParentSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception;

	ResponseEntity<Object> getActiveBioParentSampleCollection(int nbioparentsamplecollectioncode, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> createParentSampleCollection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	ResponseEntity<Object> updateParentSampleCollection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	ResponseEntity<Object> deleteParentSampleCollection(int nbioparentsamplecollectioncode, int nbioparentsamplecode, UserInfo userInfo)
			throws Exception;
	
}
