package com.agaramtech.qualis.basemaster.service.containerstructure;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.ContainerStructure;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'containerstructure' table
 */
public interface ContainerStructureDAO {

	/**
	 * This interface declaration is used to get the over all containerstructure.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of containerstructure and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getContainerStructure(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active containerStructure
	 * object based on the specified ncontainerstructurecode.
	 * 
	 * @param ncontainerstructurecode [int] primary key of containerstructure object
	 * @return response entity object holding response status and data of
	 *         containerstructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ContainerStructure getActiveContainerStructureById(final int ncontainerstructurecode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to containerstructure
	 * table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding details to
	 *                              be added in containerstructure table
	 * @return response entity object holding response status and data of added
	 *         containerstructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in containerstructure
	 * table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding details to
	 *                              be updated in containerstructure table
	 * @return response entity object holding response status and data of updated
	 *         containerstructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in containerstructure
	 * table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding detail to be
	 *                              deleted in sampledonor table
	 * @return response entity object holding response status and data of deleted
	 *         containerstructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception;

}
