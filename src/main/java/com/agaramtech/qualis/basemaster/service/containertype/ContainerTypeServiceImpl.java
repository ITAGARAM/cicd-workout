package com.agaramtech.qualis.basemaster.service.containertype;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'containertype' table
 * through its DAO layer.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 29- Jun- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ContainerTypeServiceImpl implements ContainerTypeService {
	private final ContainerTypeDAO containerTypeDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This method is used to retrieve list of all active containertype for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         containertype
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getContainerType(final int nmasterSiteCode) throws Exception {
		return containerTypeDAO.getContainerType(nmasterSiteCode);
	}

	/**
	 * This method is used to add a new entry to containertype table through its DAO
	 * layer.
	 * 
	 * @param containerType [ContainerType] object holding details to be added in
	 *                      ContainerType table
	 * @return response entity object holding response status and data of added
	 *         ContainerType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createContainerType(ContainerType containerType, UserInfo userInfo) throws Exception {
		return containerTypeDAO.createContainerType(containerType, userInfo);
	}

	/**
	 * This method is used to update entry in containertype table through its DAO
	 * layer.
	 * 
	 * @param containertype [Manufacturer] object holding details to be updated in
	 *                      containertype table
	 * @return response entity object holding response status and data of updated
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateContainerType(ContainerType containertype, UserInfo userInfo) throws Exception {
		return containerTypeDAO.updateContainerType(containertype, userInfo);
	}

	/**
	 * This method is used to retrieve active containertype object based on the
	 * specified ncontainertypecode through its DAO layer.
	 * 
	 * @param ncontainertypecode [int] primary key of containertype object
	 * @param siteCode           [int] primary key of site object
	 * @return response entity object holding response status and data of
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveContainerTypeById(final int ncontainertypecode, final UserInfo userInfo)
			throws Exception {
		final ContainerType containerType = (ContainerType) containerTypeDAO
				.getActiveContainerTypeById(ncontainertypecode, userInfo);
		if (containerType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(containerType, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to delete entry in containertype table through its DAO
	 * layer.
	 * 
	 * @param containertype [ContainerType] object holding detail to be deleted in
	 *                      containertype table
	 * @return response entity object holding response status and data of deleted
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteContainerType(ContainerType containerType, UserInfo userInfo) throws Exception {
		return containerTypeDAO.deleteContainerType(containerType, userInfo);
	}
}
