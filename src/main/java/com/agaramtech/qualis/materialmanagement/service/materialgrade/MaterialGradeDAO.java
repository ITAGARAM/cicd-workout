package com.agaramtech.qualis.materialmanagement.service.materialgrade;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialGrade;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'materialgrade' table through its implementation class.
 * 
 * @author ATE203
 * @version 9.0.0.1
 * @since 27- July- 2022
 */
public interface MaterialGradeDAO {

	/**
	 * This interface declaration is used to retrieve list of all active
	 * materialgrade for the specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         materialgrade
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getMaterialGrade(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active materialgrade object
	 * based on the specified nmaterialgradecode.
	 * 
	 * @param nmaterialgradecode [int] primary key of materialgrade object
	 * @return response entity object holding response status and data of
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public MaterialGrade getActiveMaterialGradeById(final int nmaterialgradecode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to materialgrade table.
	 * 
	 * @param materialgrade [MaterialGrade] object holding details to be added in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of added
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in materialgrade table.
	 * 
	 * @param materialgrade [MaterialGrade] object holding details to be updated in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of updated
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in materialgrade table.
	 * 
	 * @param materialgrade [MaterialGrade] object holding detail to be deleted in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of deleted
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
