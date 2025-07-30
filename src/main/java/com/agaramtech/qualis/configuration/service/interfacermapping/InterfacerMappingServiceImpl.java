package com.agaramtech.qualis.configuration.service.interfacermapping;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.configuration.model.InterfacerMapping;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'interfacerMapping'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InterfacerMappingServiceImpl implements InterfacerMappingServices {

	private final CommonFunction commonFunction;
	private final InterfacerMappingDAO interfacerMappingDAO;

	/**
	 * This method is used to retrieve list of all active interfacerMapping for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         interfacerMapping
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInterfacerMapping(final UserInfo userInfo) throws Exception {
		return interfacerMappingDAO.getInterfacerMapping(userInfo);
	}

	/**
	 * This method is used to add a new entry to interfacerMapping table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          added in interfacermapping table
	 * @return inserted interfacerMapping object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		return interfacerMappingDAO.createInterfacerMapping(interfacerMapping, userInfo);
	}

	/**
	 * This method id used to delete an entry in interfacerMapping table
	 * 
	 * @param interfacerMapping [interfacerMapping] an Object holds the record to be
	 *                          deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         interfacerMapping object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		return interfacerMappingDAO.deleteInterfacerMapping(interfacerMapping, userInfo);
	}

	/**
	 * This method is used to update entry in interfacerMapping table.
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          updated in interfacerMapping table
	 * @return response entity object holding response status and data of updated
	 *         interfacerMapping object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		return interfacerMappingDAO.updateInterfacerMapping(interfacerMapping, userInfo);
	}

	/**
	 * In this method we check the alredy deleted in the multiuser test.
	 */
	@Override
	public ResponseEntity<Object> getActiveInterfacerMappingById(int ninterfacemappingcode, UserInfo userInfo)
			throws Exception {
		final InterfacerMapping objinterfacermapping = interfacerMappingDAO
				.getActiveInterfacerMappingById(ninterfacemappingcode, userInfo);
		if (objinterfacermapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objinterfacermapping, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception {
		return interfacerMappingDAO.getInterfaceType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestMaster(Map<String, Object> inputMap, UserInfo userInfo) {
		return interfacerMappingDAO.getTestMaster(inputMap, userInfo);
	}
}