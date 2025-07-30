package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.configuration.service.ftpconfig.FTPConfigurationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the FTPConfigurationService Service methods
 */
@RestController
@RequestMapping("/ftpconfig")
public class FTPConfigurationController {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPConfigurationController.class);
	
	private FTPConfigurationService objFTPConfigurationService;
	private RequestContext requestContext;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public FTPConfigurationController(RequestContext requestContext, FTPConfigurationService objFTPConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.objFTPConfigurationService = objFTPConfigurationService;
	}


	/**
	 * This method is used to get List of FTP Configured from ftpconfig table
	 * 
	 * @param inputMap hold the value of userInfo
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 */
	@PostMapping(value = "/getFTPConfig")
	public ResponseEntity<Object> getFTPConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.getFTPConfig(userInfo);
	}

	/**
	 * This method is used to get a FTP Configured from ftpconfig table using
	 * primary key
	 * 
	 * @param inputMap hold the value of userInfo and ftpno primary key of
	 *                 'ftpconfig table'
	 * @return ResponseEntity which have a FTP configured and HTTP Status
	 */
	@PostMapping(value = "/getActiveFTPConfigById")
	public ResponseEntity<Object> getActiveFTPConfigById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int ftpNo = (int) inputMap.get("nftpno");

		return objFTPConfigurationService.getActiveFTPConfigById(ftpNo, userInfo);
	}

	/**
	 * This method is used to add a new entry in ftpconfig table
	 * 
	 * @param inputMap which holds FTPConfig object to be inserted and userInfo
	 *                 Object
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 */
	@PostMapping(value = "/createFTPConfig")
	public ResponseEntity<Object> createFTPConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		FTPConfig ftpConfig = mapper.convertValue(inputMap.get("ftpconfig"), new TypeReference<FTPConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.createFTPConfig(ftpConfig, userInfo);

	}

	/**
	 * This method is used to update an entry in ftpconfig table
	 * 
	 * @param inputMap which holds FTPConfig object to be updated and userInfo
	 *                 Object
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 */
	@PostMapping(value = "/updateFTPConfig")
	public ResponseEntity<Object> updateFTPConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final FTPConfig ftpConfig = mapper.convertValue(inputMap.get("ftpconfig"), new TypeReference<FTPConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.updateFTPConfig(ftpConfig, userInfo);

	}

	/**
	 * This method is used to delete an entry in ftpconfig table
	 * 
	 * @param inputMap which holds FTPConfig object to be deleted and userInfo
	 *                 Object
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 */
	@PostMapping(value = "/deleteFTPConfig")
	public ResponseEntity<Object> deleteFTPConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final FTPConfig ftpConfig = mapper.convertValue(inputMap.get("ftpconfig"), new TypeReference<FTPConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.deleteFTPConfig(ftpConfig, userInfo);

	}

	/**
	 * This method is used to set a configured ftp as default in ftpconfig table
	 * 
	 * @param inputMap which holds FTPConfig object to be set default and userInfo
	 *                 Object
	 * @return ResponseEntity which have list of FTP configured and HTTP Status
	 */
	@PostMapping(value = "/setDefaultFTPConfig")
	public ResponseEntity<Object> setDefaultFTPConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final FTPConfig ftpConfig = mapper.convertValue(inputMap.get("ftpconfig"), new TypeReference<FTPConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.setDefaultFTPConfig(ftpConfig, userInfo);

	}
	
	@PostMapping(value = "/getFTPType")
	public ResponseEntity<Object> getFTPType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return objFTPConfigurationService.getFTPType(userInfo);
	}
}
