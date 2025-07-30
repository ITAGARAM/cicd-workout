package com.agaramtech.qualis.basemaster.service.containerstructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.ContainerStructure;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'conatinerstructure'
 * table through its DAO layer.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ContainerStructureServiceImpl implements ContainerStructureService {

	private final ContainerStructureDAO containerStructureDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active conatinerstructure.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         conatinerstructure
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getContainerStructure(final UserInfo userInfo) throws Exception {
		return containerStructureDAO.getContainerStructure(userInfo);
	}

	/**
	 * This method is used to retrieve active conatinerstructure object based on the
	 * specified ncontainerstructurecode.
	 * 
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @return response entity object holding response status and data of
	 *         containerstructure object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveContainerStructureById(final int ncontainerstructurecode, UserInfo userInfo)
			throws Exception {
		final ContainerStructure containerstructure = containerStructureDAO
				.getActiveContainerStructureById(ncontainerstructurecode, userInfo);
		if (containerstructure == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(containerstructure, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to add a new entry to containerstructure table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding details to
	 *                              be added in containerstructure table
	 * @return inserted objContainerStructure object with HTTP Status.
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception {
		return containerStructureDAO.createContainerStructure(objContainerStructure, userInfo);
	}

	/**
	 * This method is used to update entry in containerstructure table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding details to
	 *                              be updated in containerstructure table
	 * @return response entity object holding response status and data of updated
	 *         objContainerStructure object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception {
		return containerStructureDAO.updateContainerStructure(objContainerStructure, userInfo);
	}
	/**
	 * This method id used to delete an entry in containerstructure table
	 * 
	 * @param objContainerStructure [ContainerStructure] an Object holds the record
	 *                              to be deleted
	 * @return a response entity with corresponding HTTP status and a
	 *         ContainerStructure object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception {
		return containerStructureDAO.deleteContainerStructure(objContainerStructure, userInfo);
	}
}
