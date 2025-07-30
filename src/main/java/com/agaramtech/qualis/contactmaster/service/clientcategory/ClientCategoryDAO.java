package com.agaramtech.qualis.contactmaster.service.clientcategory;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'clientcategory' table through its implementation class.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 08- Sep- 2020
 */
public interface ClientCategoryDAO {

	/**
	 * This interface declaration is used to retrieve list of all active
	 * clientcategory for the specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<ClientCategory>> getClientCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active clientcategory object
	 * based on the specified nclientcatcode.
	 * 
	 * @param nclientcatcode [int] primary key of clientcategory object
	 * @return response entity object holding response status and data of
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ClientCategory getActiveClientCategoryById(final int nclientcatcode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to clientcategory
	 * table.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be added in
	 *                       clientcategory table
	 * @return response entity object holding response status and data of added
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> createClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in clientcategory table.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be updated
	 *                       in clientcategory table
	 * @return response entity object holding response status and data of updated
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> updateClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in clientCategory table.
	 * 
	 * @param clientCategory [ClientCategory] object holding detail to be deleted in
	 *                       clientCategory table
	 * @return response entity object holding response status and data of deleted
	 *         clientCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> deleteClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active
	 * clientcategory for the specified site for the Portal.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getClientCategoryForPortal(final UserInfo userInfo) throws Exception;
}
