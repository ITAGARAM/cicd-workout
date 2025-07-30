package com.agaramtech.qualis.basemaster.service.containertype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This interface holds declarations to perform CRUD operation on
 * 'containertype' table through its implementation class.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 29- Jun- 2020
 */
public interface ContainerTypeDAO {
	/**
	 * This interface declaration is used to retrieve list of all active
	 * containertype for the specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         containertype
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getContainerType(final int nmasterSiteCode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to containertype table.
	 * 
	 * @param containerType [Containertype] object holding details to be added in
	 *                      containertype table
	 * @return response entity object holding response status and data of added
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createContainerType(ContainerType containerType, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in containertype table.
	 * 
	 * @param containertype [Containertype] object holding details to be updated in
	 *                      containertype table
	 * @return response entity object holding response status and data of updated
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateContainerType(ContainerType containertype, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to retrieve active containertype object
	 * based on the specified ncontainertypecode.
	 * 
	 * @param ncontainertypecode [int] primary key of containertype object
	 * @return response entity object holding response status and data of
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ContainerType getActiveContainerTypeById(final int ncontainertypecode, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to delete entry in containertype table.
	 * 
	 * @param containertype [Containertype] object holding detail to be deleted in
	 *                      containertype table
	 * @return response entity object holding response status and data of deleted
	 *         containertype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteContainerType(ContainerType containerType, UserInfo userInfo) throws Exception;

}
