package com.agaramtech.qualis.checklist.service.checklistqbcategory;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'qbcategory' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class QBCategoryServiceImpl implements QBCategoryService {

	private final CommonFunction commonFunction;
	private final QBCategoryDAO qbCategoryDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param qbcategoryService    qbcategoryService
	 */
	public QBCategoryServiceImpl(CommonFunction commonFunction, QBCategoryDAO qbCategoryDAO) {
		super();
		this.commonFunction = commonFunction;
		this.qbCategoryDAO = qbCategoryDAO;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available qbcategorys with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of qbcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getQBCategory(int nmasterSiteCode) throws Exception {
		return qbCategoryDAO.getQBCategory(nmasterSiteCode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding details to be added in qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		return qbCategoryDAO.createQBCategory(objChecklistQBCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding details to be updated in qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		return qbCategoryDAO.updateQBCategory(objChecklistQBCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in qbcategory  table.
	 * @param objqbcategory [qbcategory] object holding detail to be deleted from qbcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		return qbCategoryDAO.deleteQBCategory(objChecklistQBCategory, userInfo);
	}

	
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active qbcategory object based
	 * on the specified nqbcategoryCode.
	 * @param nqbcategoryCode [int] primary key of qbcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of qbcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveQBCategoryById(int nchecklistqbcategorycode, UserInfo userInfo)
			throws Exception {
		final ChecklistQBCategory checklistQBCategory = qbCategoryDAO.getActiveQBCategoryById(nchecklistqbcategorycode);
		if (checklistQBCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(checklistQBCategory, HttpStatus.OK);
		}
	}

}
