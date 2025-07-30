package com.agaramtech.qualis.submitter.service.institutiondepartment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'institutiondepartment'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InstitutionDepartmentServiceImpl implements InstitutionDepartmentService {

	private final InstitutionDepartmentDAO institutionDepartmentDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active InstitutionDepartment for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstitutionDepartment
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception {
		return institutionDepartmentDAO.getInstitutionDepartment(userInfo);
	}

	/**
	 * This method is used to retrieve active InstitutionDepartment object based on
	 * the specified ninstitutiondeptcode.
	 * 
	 * @param ninstitutiondeptcode [int] primary key of InstitutionDepartment object
	 * @return response entity object holding response status and data of
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInstitutionDepartmentById(final int ninstitutiondeptcode,
			final UserInfo userInfo) throws Exception {
		final InstitutionDepartment objInstitutionDepartment = institutionDepartmentDAO
				.getActiveInstitutionDepartmentById(ninstitutiondeptcode);
		if (objInstitutionDepartment == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objInstitutionDepartment, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to InstitutionDepartment table.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be added in InstitutionDepartment
	 *                                 table
	 * @return inserted InstitutionDepartment object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		return institutionDepartmentDAO.createInstitutionDepartment(objInstitutionDepartment, userInfo);
	}

	/**
	 * This method is used to update entry in InstitutionDepartment table.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be updated in
	 *                                 InstitutionDepartment table
	 * @return response entity object holding response status and data of updated
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		return institutionDepartmentDAO.updateInstitutionDepartment(objInstitutionDepartment, userInfo);
	}

	/**
	 * This method id used to delete an entry in InstitutionDepartment table
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InstitutionDepartment object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		return institutionDepartmentDAO.deleteInstitutionDepartment(objInstitutionDepartment, userInfo);
	}

}
