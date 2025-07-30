package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.service.testcategory.TestCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the TestCategory Service methods.
 */
@RestController
@RequestMapping("/testcategory")
public class TestCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCategoryController.class);

	private RequestContext requestContext;
	private final TestCategoryService testCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * @param requestContext      RequestContext to hold the request
	 * @param testCategoryService TestCategoryService
	 */
	public TestCategoryController(RequestContext requestContext, TestCategoryService testCategoryService) {
		super();
		this.requestContext = requestContext;
		this.testCategoryService = testCategoryService;
	}

	/**
	 * This method is used to retrieve list of available testcategory(s).
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         testcategorys
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getTestCategory")
	public ResponseEntity<Object> getTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return testCategoryService.getTestCategory(userInfo);
	}

	/**
	 * This method will is used to make a new entry to testcategory table.
	 * @param inputMap map object holding params ( testcategory [TestCategory]
	 *                 object holding details to be added in testcategory table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "testcategory": { "stestcategoryname": "cm", "sdescription":
	 *                 "cm","nclinicaltyperequired":4,"ndefaultstatus":4 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 25,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         testcategory already exists/ list of testcategorys along with the
	 *         newly added testcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createTestCategory")
	public ResponseEntity<Object> createTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TestCategory testCategory = objmapper.convertValue(inputMap.get("testcategory"), TestCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testCategoryService.createTestCategory(testCategory, userInfo);
	}

	/**
	 * This method is used to retrieve a specific testcategory record.
	 * @param inputMap [Map] map object with "ntestcategorycode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "ntestcategorycode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 25,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with testcategory object for the specified primary key
	 *         / with string message as 'Deleted' if the testcategory record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveTestCategoryById")
	public ResponseEntity<Object> getActiveTestCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ntestcategorycode = (Integer) inputMap.get("ntestcategorycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return testCategoryService.getActiveTestCategoryById(ntestcategorycode, userInfo);
	}

	/**
	 * This method is used to update selected testcategory details.
	 * @param inputMap [map object holding params( unit [Unit] object holding
	 *                 details to be updated in testcategory table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "testcategory": {"ntestcategorycode":1,"stestcategoryname":
	 *                 "cm", "sdescription":
	 *                 "cm","nclinicaltyperequired":4,"ndefaultstatus":4 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 25,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         testcategory record is not available/ list of all testcategorys and
	 *         along with the updated testcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateTestCategory")
	public ResponseEntity<Object> updateTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TestCategory testCategory = objmapper.convertValue(inputMap.get("testcategory"), TestCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testCategoryService.updateTestCategory(testCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in testcategory table
	 * @param inputMap [Map] object with keys of testcategory entity and UserInfo
	 *                 object. Input:{ "testcategory": {"ntestcategorycode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 25,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         testcategory record is not available/ string message as 'Record is
	 *         used in....' when the unit is associated in transaction / list of all
	 *         testcategorys excluding the deleted record
	 * @throws Exception exception
	 */
	@RequestMapping(value = "/deleteTestCategory", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TestCategory testCategory = objmapper.convertValue(inputMap.get("testcategory"), TestCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return testCategoryService.deleteTestCategory(testCategory, userInfo);
	}
}
