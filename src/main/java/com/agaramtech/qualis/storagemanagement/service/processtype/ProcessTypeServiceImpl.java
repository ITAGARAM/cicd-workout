package com.agaramtech.qualis.storagemanagement.service.processtype;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.ProcessType;

/**
 * This class holds methods to perform CRUD operation on 'processtype' table
 * through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ProcessTypeServiceImpl implements ProcessTypeService {

	private final ProcessTypeDAO objProcessTypeDAO;
	private final CommonFunction commonFunction;

	
	public ProcessTypeServiceImpl(ProcessTypeDAO objProcessTypeDAO, CommonFunction commonFunction) {
		super();
		this.objProcessTypeDAO = objProcessTypeDAO;
		this.commonFunction = commonFunction;
	}
	/**
	 * This method is used to retrieve list of all active processtypes for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         processtypes
	 * @throws Exception that are thrown from this Service layer
	 */

	public ResponseEntity<Object> getProcessType(UserInfo userInfo) throws Exception {
		return objProcessTypeDAO.getProcessType(userInfo);
	}

	/**
	 * This method is used to add a new entry to processtype table. On successive
	 * insert get the new inserted record along with default status from transaction
	 * status
	 * 
	 * @param objprocesstype [processtype] object holding details to be added in
	 *                       processtype table
	 * @return inserted processtype object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> createProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objProcessTypeDAO.createProcessType(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active processtype object based on the
	 * specified nprocesstypeCode.
	 * 
	 * @param nprocesstypeCode [int] primary key of processtype object
	 * @return response entity object holding response status and data of
	 *         processtype object
	 * @throws Exception that are thrown from this Service layer
	 */

	@Override
	public ResponseEntity<Object> getActiveProcessTypeById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ProcessType objProcessType = objProcessTypeDAO.getActiveProcessTypeById(inputMap, userInfo);
		if (objProcessType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objProcessType, HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in processtype table
	 * 
	 * @param objprocesstype [processtype] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an processtype
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objProcessTypeDAO.deleteProcessType(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in processtype table.
	 * 
	 * @param objprocesstype [processtype] object holding details to be updated in
	 *                       processtype table
	 * @return response entity object holding response status and data of updated
	 *         processtype object
	 * @throws Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> updateProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objProcessTypeDAO.updateProcessType(inputMap, userInfo);
	}
}
