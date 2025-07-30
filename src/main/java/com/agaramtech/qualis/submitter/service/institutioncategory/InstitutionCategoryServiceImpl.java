package com.agaramtech.qualis.submitter.service.institutioncategory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on institutioncategory
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InstitutionCategoryServiceImpl implements InstitutionCategoryService {
	private final InstitutionCategoryDAO institutioncategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available institutioncategorys with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of institutioncategory records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<List<InstitutionCategory>> getInstitutionCategory(final UserInfo userInfo) throws Exception {
		return institutioncategoryDAO.getInstitutionCategory(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to institutioncategory table.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding details to
	 *                               be added in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> createInstitutionCategory(final InstitutionCategory institutioncategory,
			final UserInfo userInfo) throws Exception {
		return institutioncategoryDAO.createInstitutionCategory(institutioncategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active institutioncategory object based on the specified
	 * ninstitutioncategoryCode.
	 * 
	 * @param ninstitutioncategoryCode [int] primary key of institutioncategory
	 *                                 object
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getActiveInstitutionCategoryById(final int ninstitutioncatcode,
			final UserInfo userInfo) throws Exception {
		final InstitutionCategory Institutioncategory = institutioncategoryDAO
				.getActiveInstitutionCategoryById(ninstitutioncatcode);
		if (Institutioncategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(Institutioncategory, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in institutioncategory table.
	 * 
	 * @param objInstitutionCategory [InstitutionCategory] object holding details to
	 *                               be updated in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateInstitutionCategory(final InstitutionCategory institutioncategory,
			final UserInfo userInfo) throws Exception {
		return institutioncategoryDAO.updateInstitutionCategory(institutioncategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in institutioncategory table.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding detail to
	 *                               be deleted from institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details and
	 *                               nmasterSiteCode [int] primary key of site
	 *                               object for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         institutioncategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteInstitutionCategory(final InstitutionCategory institutioncategory,
			final UserInfo userInfo) throws Exception {
		return institutioncategoryDAO.deleteInstitutionCategory(institutioncategory, userInfo);
	}

}
