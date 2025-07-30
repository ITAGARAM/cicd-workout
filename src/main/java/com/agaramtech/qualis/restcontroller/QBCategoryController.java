package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
import com.agaramtech.qualis.checklist.service.checklistqbcategory.QBCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/qbcategory")
public class QBCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(QBCategoryController.class);

	private RequestContext requestContext;
	
	private final QBCategoryService objQBCategoryService;
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * @param requestContext RequestContext to hold the request
	 * @param qbcategoryService    qbcategoryService
	 */
	public QBCategoryController(RequestContext requestContext, QBCategoryService objQBCategoryService) {
		super();
		this.requestContext = requestContext;
		this.objQBCategoryService = objQBCategoryService;
	}
	

	/**
	 * This method is used to retrieve list of available qbcategory(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all qbcategorys
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getQBCategory")
	public ResponseEntity<Object> getQBCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getQBCategory --> "+userInfo);
		return objQBCategoryService.getQBCategory(userInfo.getNmastersitecode());
	}
	
	/**
	 * This method will is used to make a new entry to qbcategory  table.
	 * @param inputMap map object holding params ( 
	 * 								qbcategory [qbcategory]  object holding details to be added in qbcategory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 * @return ResponseEntity with string message as 'Already Exists' if the qbcategory already exists/ 
	 * 			list of qbcategorys along with the newly added qbcategory.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createQBCategory")
	public ResponseEntity<Object> createQBCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final ChecklistQBCategory objChecklistQBCategory = objMapper.convertValue(inputMap.get("qbcategory"),
				new TypeReference<ChecklistQBCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objQBCategoryService.createQBCategory(objChecklistQBCategory, userInfo);
	}

	
	/**
	 * This method is used to update selected qbcategory details.
	 * @param inputMap [map object holding params(
	 * 					qbcategory [qbcategory]  object holding details to be updated in qbcategory table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"qbcategory": {"nqbcategorycode":1,"sqbcategoryname": "m","sqbcategorysynonym": "m", 
 									 "sdescription": "m", "ndefaultstatus": 3      },
    						"userinfo":{...}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the qbcategory record is not available/ 
	 * 			list of all qbcategorys and along with the updated qbcategory.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateQBCategory")
	public ResponseEntity<Object> updateQBCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistQBCategory objChecklistQBCategory = objMapper.convertValue(inputMap.get("qbcategory"),
				new TypeReference<ChecklistQBCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objQBCategoryService.updateQBCategory(objChecklistQBCategory, userInfo);
	}

	
	/**
	 * This method is used to delete an entry in qbcategory table
	 * @param inputMap [Map] object with keys of qbcategory entity and UserInfo object.
	 * 					Input:{
     						"qbcategory": {"nqbcategorycode":1},
    						"userinfo":{...}}
	 * @return ResponseEntity with string message as 'Already deleted' if the qbcategory record is not available/ 
	 * 			string message as 'Record is used in....' when the qbcategory is associated in transaction /
	 * 			list of all qbcategorys excluding the deleted record 
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/deleteQBCategory")
	public ResponseEntity<Object> deleteQBCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ChecklistQBCategory objChecklistQBCategory = objMapper.convertValue(inputMap.get("qbcategory"),
				new TypeReference<ChecklistQBCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objQBCategoryService.deleteQBCategory(objChecklistQBCategory, userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific qbcategory record.
	 * @param inputMap  [Map] map object with "nqbcategorycode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nqbcategorycode": 1,
							     "userinfo":{...}}
	 * @return ResponseEntity with qbcategory object for the specified primary key / with string message as
	 * 						'Deleted' if the qbcategory record is not available
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveQBCategoryById")
	public ResponseEntity<Object> getActiveQBCategoryById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistQBCategoryCode = (int) inputMap.get("nchecklistqbcategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objQBCategoryService.getActiveQBCategoryById(nchecklistQBCategoryCode, userInfo);
	}

}
