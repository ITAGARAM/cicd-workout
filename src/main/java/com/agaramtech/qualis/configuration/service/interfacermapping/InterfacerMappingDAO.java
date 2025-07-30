package com.agaramtech.qualis.configuration.service.interfacermapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.configuration.model.InterfacerMapping;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'interfacerMapping' table
 * 
 * @author AT-E234
 *
 */
public interface InterfacerMappingDAO {

	/**
	 * This interface declaration is used to get the over all interfacerMapping with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of interfacerMapping with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInterfacerMapping(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to interfacerMapping
	 * table.
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          added in interfacerMapping table
	 * @return response entity object holding response status and data of added
	 *         interfacerMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in interfacerMapping
	 * table.
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding detail to be
	 *                          deleted in interfacerMapping table
	 * @return response entity object holding response status and data of deleted
	 *         interfacerMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in interfacerMapping
	 * table.
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          updated in interfacerMapping table
	 * @return response entity object holding response status and data of updated
	 *         interfacerMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active interfacerMapping
	 * object based on the specified ninterfacermappingCode.
	 * 
	 * @param interfacerMapping [int] primary key of interfacerMapping object
	 * @return response entity object holding response status and data of
	 *         interfacerMapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public InterfacerMapping getActiveInterfacerMappingById(int ninterfacemappingcode, UserInfo userInfo)
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
