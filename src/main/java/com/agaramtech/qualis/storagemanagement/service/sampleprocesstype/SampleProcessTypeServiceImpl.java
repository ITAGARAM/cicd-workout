package com.agaramtech.qualis.storagemanagement.service.sampleprocesstype;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.barcode.service.patientcategory.PatientCategoryDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleProcessType;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'sampleprocesstype' table through its DAO layer.
 * @author ATE169
 * @version 9.0.0.1
 * @since 28 - June -2024
 */

@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service  
public class SampleProcessTypeServiceImpl implements SampleProcessTypeService{
	
	private final SampleProcessTypeDAO sampleProcessTypeDAO;
	final CommonFunction commonFunction;
	
	public SampleProcessTypeServiceImpl(SampleProcessTypeDAO sampleProcessTypeDAO, CommonFunction commonFunction ) {
		this.sampleProcessTypeDAO = sampleProcessTypeDAO;
		this.commonFunction = commonFunction;
	}
	
	
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
	
	
	@Override
	public ResponseEntity<Object> getSampleProcessType(final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.getSampleProcessType(userInfo);
	}

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
	
	@Override
	public ResponseEntity<Object> getProjecttype(final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.getProjecttype(userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.getSampleType(inputMap,userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getCollectionTubeType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.getCollectionTubeType(inputMap,userInfo);
	}

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
	
	@Override
	public ResponseEntity<Object> getProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.getProcessType(inputMap,userInfo);
	}
	
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
	 * 
	 * @return inserted sampleprocesstype object and HTTP Status on successive
	 *  insert otherwise corresponding HTTP Status
	 *  
	 * @throws Exception
	 */

	@Transactional 
	@Override
	public ResponseEntity<Object> createSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.createSampleProcessType(inputMap,userInfo);
	}
	
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

	@Transactional 
	@Override
	public ResponseEntity<Object> deleteSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.deleteSampleProcessType(inputMap,userInfo);
	}
	
	/**
	 * This method is used to retrieve active sampleprocesstype object based on the
	 * specified nprocesstypeCode and to check whether the sample process type exists or not.
	 * 
	 *@param inputMap [Map] holds the following keys
	 *"userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * nsampleprocesstypecode holds the value used to identify which record to retrieve.
	 *
	 *
	 * @return response entity object holding response status and data of
	 *         sampleprocesstype object,Already Deleted alert will be thrown when the  objSampleProcessType is null.
	 *         
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getActiveSampleProcessTypeById(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		
		final SampleProcessType objSampleProcessType =(SampleProcessType) sampleProcessTypeDAO.getActiveSampleProcessTypeById(inputMap, userInfo);
	
		if(objSampleProcessType == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSampleProcessType, HttpStatus.OK);
		}
	}
	
	/**
	 * This method is used to update entry in sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type,	
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

	@Transactional 
	@Override
	public ResponseEntity<Object> updateSampleProcessType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleProcessTypeDAO.updateSampleProcessType(inputMap,userInfo);
	}
}
