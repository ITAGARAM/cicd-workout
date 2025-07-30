package com.agaramtech.qualis.storagemanagement.service.processtype;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.ProcessType;

/**
 * This interface holds declarations to perform CRUD operation on 'processtype'
 * table
 */
public interface ProcessTypeDAO {

	/**
	 * This interface declaration is used to get the over all processtypes with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of processtype with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProcessType(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to processtype table.
	 * 
	 * @param objprocesstype [processtype] object holding details to be added in
	 *                       processtype table
	 * @return response entity object holding response status and data of added
	 *         processtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active processtype object
	 * based on the specified nprocesstypeCode.
	 * 
	 * @param nprocesstypeCode [int] primary key of processtype object
	 * @return response entity object holding response status and data of
	 *         processtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ProcessType getActiveProcessTypeById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in processtype table.
	 * 
	 * @param objprocesstype [processtype] object holding detail to be deleted in
	 *                       processtype table
	 * @return response entity object holding response status and data of deleted
	 *         processtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in processtype table.
	 * 
	 * @param objprocesstype [processtype] object holding details to be updated in
	 *                       processtype table
	 * @return response entity object holding response status and data of updated
	 *         processtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
