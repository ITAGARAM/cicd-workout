package com.agaramtech.qualis.storagemanagement.service.sampleprocesstype;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'sampleprocesstype' table
 */
public interface SampleProcessTypeService {

	/**
	 * This Method is used to get the over all sampleprocesstype with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of sampleprocesstype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	
	public ResponseEntity<Object> getSampleProcessType(final UserInfo userInfo) throws Exception;

	/**
	 * This Method is used to get the over all Project Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of projecttype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getProjecttype(final UserInfo userInfo) throws Exception;


	/**
	 * This Method is used to get the over all Sample Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Sample Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;


	/**
	 * This Method is used to get the over all Collection Tube Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Collection Tube Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getCollectionTubeType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This Method is used to get the over all Process Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Process Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to add a new entry to sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * @return inserted sampleprocesstype object and HTTP Status on successive
	 *  insert otherwise corresponding HTTP Status
	 *  
	 * @throws Exception
	 */
	public ResponseEntity<Object> createSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This method id used to delete an entry in sampleprocesstype table
	 * 
	 * @param inputMap [Map] holds the following keys
	 * "userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * The sampleProcessType holds the objects used to delete the sample processing mapping-related data. 
	 * @return response entity object holding response status and data of deleted
	 *         sampleprocesstype object
	 * @throws Exception
	 */
	public ResponseEntity<Object> deleteSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve active sampleprocesstype object based on the
	 * specified nprocesstypeCode.
	 * 
	 *@param inputMap [Map] holds the following keys
	 *"userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * nsampleprocesstypecode holds the value used to identify which record to retrieve.
	 *
	 * @return response entity object holding response status and data of
	 *         sampleprocesstype object
	 *         
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getActiveSampleProcessTypeById(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to update entry in sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * 
	 * @return response entity object holding response status and data of updated
	 *         sampleprocesstype object
	 *         
	 * @throws Exception
	 */

	public ResponseEntity<Object> updateSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

}
