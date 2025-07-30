package com.agaramtech.qualis.materialmanagement.service.materialgrade;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialGrade;

/**
 * This class holds methods to perform CRUD operation on 'materialgrade' table
 * through its DAO layer.
 * 
 * @author ATE203
 * @version 9.0.0.1
 * @since 27- July- 2022
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class MaterialGradeServiceImpl implements MaterialGradeService {
	private final MaterialGradeDAO materialGradeDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param unitDAO        UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public MaterialGradeServiceImpl(MaterialGradeDAO materialGradeDAO, CommonFunction commonFunction) {
		super();
		this.materialGradeDAO = materialGradeDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active materialgrade for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         materialgrade
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getMaterialGrade(final UserInfo userInfo) throws Exception {
		return materialGradeDAO.getMaterialGrade(userInfo);
	}

	/**
	 * This method is used to retrieve active materialgrade object based on the
	 * specified nmaterialgradecode through its DAO layer.
	 * 
	 * @param nmaterialgradecode [int] primary key of materialgrade object
	 * @param siteCode           [int] primary key of site object
	 * @return response entity object holding response status and data of
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveMaterialGradeById(final int nmaterialgradecode, final UserInfo userInfo)
			throws Exception {
		final MaterialGrade materialGrade = (MaterialGrade) materialGradeDAO
				.getActiveMaterialGradeById(nmaterialgradecode, userInfo);
		if (materialGrade == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(materialGrade, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to materialgrade table through its DAO
	 * layer.
	 * 
	 * @param materialgrade [MaterialGrade] object holding details to be added in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of added
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return materialGradeDAO.createMaterialGrade(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in materialgrade table through its DAO
	 * layer.
	 * 
	 * @param materialgrade [MaterialGrade] object holding details to be updated in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of updated
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return materialGradeDAO.updateMaterialGrade(inputMap, userInfo);
	}

	/**
	 * This method is used to delete entry in materialgrade table through its DAO
	 * layer.
	 * 
	 * @param materialgrade [MaterialGrade] object holding detail to be deleted in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of deleted
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return materialGradeDAO.deleteMaterialGrade(inputMap, userInfo);
	}
}
