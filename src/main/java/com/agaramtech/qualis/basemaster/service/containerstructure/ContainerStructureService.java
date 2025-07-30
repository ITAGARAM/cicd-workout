package com.agaramtech.qualis.basemaster.service.containerstructure;

import com.agaramtech.qualis.basemaster.model.ContainerStructure;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'containerstructure' table
 */

public interface ContainerStructureService {
	/**
	 * This interface declaration is used to get the over all containerstructure
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of ContainerStructure and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getContainerStructure(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active containerstructure
	 * object based on the specified ncontainerstructurecode.
	 * 
	 * @param ncontainerstructurecode [int] primary key of containerStructure object
	 * @return response entity object holding response status and data of
	 *         containerStructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveContainerStructureById(final int ncontainerstructurecode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to containerstructure
	 * table.
	 * 
	 * @param objContainerStructure [sampledonor] object holding details to be added
	 *                              in containerstructure table
	 * @return response entity object holding response status and data of added
	 *         objContainerStructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in containerStructure
	 * table.
	 * 
	 * @param objContainerStructure [containerstructure] object holding details to
	 *                              be updated in containerstructure table
	 * @return response entity object holding response status and data of updated
	 *         objContainerStructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateContainerStructure(final ContainerStructure objContainerStructure,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in containerstructure
	 * table.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding detail to be
	 *                              deleted in containerstructure table
	 * @return response entity object holding response status and data of deleted
	 *         objContainerStructure object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteContainerStructure(final ContainerStructure objSampleDonor, UserInfo userInfo)
			throws Exception;
}
