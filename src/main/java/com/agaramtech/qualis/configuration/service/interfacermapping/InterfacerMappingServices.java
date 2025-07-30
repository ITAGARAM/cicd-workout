package com.agaramtech.qualis.configuration.service.interfacermapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.configuration.model.InterfacerMapping;
/**
 * @author AT-E234
 *
 */
public interface InterfacerMappingServices {
	/**
	 * This interface declaration is used to get the over all interfacermapping with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of interfacermapping with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInterfacerMapping(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to interfacermapping
	 * table.
	 * 
	 * @param objinterfacermapping [interfacermapping] object holding details to be
	 *                             added in interfacermapping table
	 * @return response entity object holding response status and data of added
	 *         interfacermapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in interfacermapping
	 * table.
	 * 
	 * @param objinterfacermapping [interfacermapping] object holding detail to be
	 *                             deleted in interfacermapping table
	 * @return response entity object holding response status and data of deleted
	 *         interfacermapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in interfacermapping
	 * table.
	 * 
	 * @param objinterfacermapping [interfacermapping] object holding details to be
	 *                             updated in interfacermapping table
	 * @return response entity object holding response status and data of updated
	 *         interfacermapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active interfacermapping
	 * object based on the specified ninterfacermappingCode.
	 * 
	 * @param ninterfacermappingCode [int] primary key of interfacermapping object
	 * @return response entity object holding response status and data of
	 *         interfacermapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveInterfacerMappingById(int ninterfacemappingcode, UserInfo userInfo)
			throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception;

	/**
	 * @param inputMap
	 * @param userInfo
	 * @return
	 */
	public ResponseEntity<Object> getTestMaster(Map<String, Object> inputMap, UserInfo userInfo);

}
