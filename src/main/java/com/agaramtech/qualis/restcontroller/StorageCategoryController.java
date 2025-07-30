package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import com.agaramtech.qualis.storagemanagement.service.storagecategory.StorageCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/storagecategory")
public class StorageCategoryController {

	private RequestContext requestContext;
	private final StorageCategoryService storageCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext         RequestContext to hold the request
	 * @param storageCategoryService StorageCategoryService
	 */
	public StorageCategoryController(RequestContext requestContext, StorageCategoryService storageCategoryService) {
		super();
		this.requestContext = requestContext;
		this.storageCategoryService = storageCategoryService;
	}

	/**
	 * This method is used to retrieve list of available storagecategory(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         storagecategory
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageCategory")
	public ResponseEntity<List<StorageCategory>> getStorageCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return storageCategoryService.getStorageCategory(userInfo);
	}

	/**
	 * This method will is used to make a new entry to storagecategory table.
	 * 
	 * @param inputMap map object holding params ( storagecategory [StorageCategory]
	 *                 object holding details to be added in storagecategory table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "storagecategory": { "sstoragecategoryname": "cm"},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         storagecategory already exists/ list of storagecategory along with
	 *         the newly added storagecategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createStorageCategory")
	public ResponseEntity<? extends Object> createStorageCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final StorageCategory objStorageCategory = objMapper.convertValue(inputMap.get("storagecategory"),
				new TypeReference<StorageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return storageCategoryService.createStorageCategory(objStorageCategory, userInfo);
	}

	/**
	 * This method is used to update selected storagecategory details.
	 * 
	 * @param inputMap [map object holding params( storagecategory [StorageCategory]
	 *                 object holding details to be updated in storagecategory
	 *                 table, userinfo [UserInfo] holding logged in user details)
	 *                 Input:{ "storagecategory":
	 *                 {"nstoragecategorycode":1,"sstoragecategoryname": "m",
	 *                 "sdescription": "m" }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         storagecategory record is not available/ list of all storagecategory
	 *         and along with the updated storagecategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateStorageCategory")
	public ResponseEntity<? extends Object> updateStorageCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final StorageCategory objStorageCategory = objMapper.convertValue(inputMap.get("storagecategory"),
				new TypeReference<StorageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return storageCategoryService.updateStorageCategory(objStorageCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in storagecategory table
	 * 
	 * @param inputMap [Map] object with keys of StorageCategory entity and UserInfo
	 *                 object. Input:{ "storagecategory":
	 *                 {"nstoragecategorycode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         storagecategory record is not available/ string message as 'Record is
	 *         used in....' when the storagecategory is associated in transaction /
	 *         list of all storagecategory excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteStorageCategory")
	public ResponseEntity<? extends Object> deleteStorageCategory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final StorageCategory objStorageCategory = objMapper.convertValue(inputMap.get("storagecategory"),
				new TypeReference<StorageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return storageCategoryService.deleteStorageCategory(objStorageCategory, userInfo);
	}

	/**
	 * This method is used to retrieve a specific storagecategory record.
	 * 
	 * @param inputMap [Map] map object with "nstoragecategorycode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nstoragecategorycode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with StorageCategory object for the specified primary
	 *         key / with string message as 'Deleted' if the storagecategory record
	 *         is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveStorageCategoryById")
	public ResponseEntity<Object> getActiveStorageCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nstorageCategoryCode = (int) inputMap.get("nstoragecategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return storageCategoryService.getActiveStorageCategoryById(nstorageCategoryCode, userInfo);
	}
}
