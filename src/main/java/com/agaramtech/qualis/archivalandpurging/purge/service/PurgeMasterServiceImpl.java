package com.agaramtech.qualis.archivalandpurging.purge.service;

//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
//import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
//import com.agaramtech.qualis.global.CommonFunction;
//import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This class holds methods to perform CRUD operation on 'purgemaster' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class PurgeMasterServiceImpl implements PurgeMasterService {

	private final PurgeMasterDAO purgeMasterDAO;
	//private final CommonFunction commonFunction;
	
	public PurgeMasterServiceImpl(PurgeMasterDAO purgeMasterDAO) {
		this.purgeMasterDAO = purgeMasterDAO;
		//this.commonFunction = commonFunction;
	}	
	
	/**
	 * This method is used to retrieve list of all active purgemaster for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active purgemaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getPurge(UserInfo userInfo) throws Exception {
		
		return purgeMasterDAO.getPurge(userInfo);
	}

	/**
	 * This method is used to retrieve active purgemaster object based on the specified npurgemastercode.
	 * @param npurgemastercode [int] primary key of purgemaster object
	 * @return response entity  object holding response status and data of purgemaster object
	 * @throws Exception that are thrown from this Service layer
	 */
//	@Override
//	public ResponseEntity<Object> getActiveLimsPurgMasterById(int npurgmastercode,UserInfo userInfo) throws Exception {
//		
//		final LimsPurgMaster limspurgmaster = limsPurgMasterDAO.getActiveLimsPurgMasterById(npurgmastercode,userInfo);
//		if (limspurgmaster == null) {
//			
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//		}
//		else {
//			return new ResponseEntity<>(limspurgmaster, HttpStatus.OK);
//		}
//
//	}
	@Override
	public ResponseEntity<Object> getActivePurgeMasterById(int npurgemastercode,UserInfo userInfo) throws Exception {
		 
		return purgeMasterDAO.getActivePurgeMasterById(npurgemastercode,userInfo);
	}

	/**
	 * This method is used to add a new entry to purgemaster table.
	 * On successive insert get the new inserted record along with default status from transaction status 
	 * @param objPurgeMaster [PurgeMaster] object holding details to be added in purgemaster table
	 * @return inserted purgemaster object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createPurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception {

		return purgeMasterDAO.createPurgeMaster(objPurgeMaster,userInfo);
	}

	/**
	 * This method is used to update entry in purgemaster  table.
	 * @param objPurgeMaster [PurgeMaster] object holding details to be updated in purgemaster table
	 * @return response entity object holding response status and data of updated purgemaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updatePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception {
		 
		return purgeMasterDAO.updatePurgeMaster(objPurgeMaster,userInfo);
	}

	/**
	 * This method id used to delete an entry in purgemaster table
	 * @param objPurgeMaster [PurgeMaster] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an purgemaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deletePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception {		
		return purgeMasterDAO.deletePurgeMaster(objPurgeMaster,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception
	{
		return purgeMasterDAO.getSite(userInfo);
	}

}


