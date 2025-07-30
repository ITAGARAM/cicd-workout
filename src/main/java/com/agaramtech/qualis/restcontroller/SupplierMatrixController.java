package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.contactmaster.service.suppliermatrix.SupplierMatrixService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method to access
 * the SupplierMatrix Master Service methods.
 * @author ATE113
 * @version 
 * @since   12- Aug- 2020
 */
@RestController
@RequestMapping("/suppliermatrix")
public class SupplierMatrixController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierMatrixController.class);
	
	
	private RequestContext requestContext;
	private SupplierMatrixService supplierMatrixService;
	
	public SupplierMatrixController(RequestContext requestContext, SupplierMatrixService supplierMatrixService) {
		super();
		this.requestContext = requestContext;
		this.supplierMatrixService = supplierMatrixService;
	}
	
	/**
	 * This method is used to retrieve list of active suppliermatrix for the specified site.
	 * @param inputMap  [Map] map object with "nsuppliercode" as key for which the list is to be fetched
	 * @return response object with list of active suppliermatrix that are to be listed for the specified site
	 */
	@RequestMapping(value = "/getSupplierMatrix", method = RequestMethod.POST)
	public ResponseEntity<Object> getSupplierMatrix(@RequestBody Map<String, Object> inputMap) throws Exception{

	//	try {
			ObjectMapper objMapper = new ObjectMapper();
            UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			
			final int nsuppliercode = (Integer) inputMap.get("nsuppliercode");
			//final int siteCode = (Integer) inputMap.get("nsitecode");
			
			requestContext.setUserInfo(userInfo);
			return supplierMatrixService.getSupplierMatrix(nsuppliercode,userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	
	/**
	 * This method is used to add new suppliermatrix for the specified Site.
	 * @param inputMap [Map] object with keys of suppliermatrix entity.
	 * @return response entity of newly added suppliermatrix entity
	 */
	@RequestMapping(value = "/createSupplierMatrix", method = RequestMethod.POST)
	public ResponseEntity<Object> createSupplierMatrix(@RequestBody Map<String, Object> inputMap) throws Exception{

		//try {
			final ObjectMapper objmapper = new ObjectMapper();
			final List<SupplierMatrix> supplierMatrix = objmapper.convertValue(inputMap.get("suppliermatrix"), new TypeReference<List<SupplierMatrix>>() {});
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			
			requestContext.setUserInfo(userInfo);
			return supplierMatrixService.createSupplierMatrix(supplierMatrix, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	
	/**
	 * This method is used to retrieve selected active suppliermatrix detail.
	 * @param inputMap  [Map] map object with "nsuppliermatrixcode" and "userInfo" as keys for which the data is to be fetched
	 * @return response object with selected active suppliermatrix
	 */
	@RequestMapping(value = "/getActiveSupplierMatrixById", method = RequestMethod.POST)
	public ResponseEntity<Object> getActiveSupplierMatrixById(@RequestBody Map<String, Object> inputMap) throws Exception{

		//try {
			final ObjectMapper objmapper = new ObjectMapper();
			final int nsuppliermatrixCode = (Integer) inputMap.get("nsuppliermatrixcode");
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			
			requestContext.setUserInfo(userInfo);
			return supplierMatrixService.getActiveSupplierMatrixById(nsuppliermatrixCode, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());			
//			System.out.println(e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	
	/**
	 * This method is used to update suppliermatrix for the specified Site.
	 * @param inputMap [Map] object with keys of suppliermatrix entity.
	 * @return response entity of updated suppliermatrix entity
	 */
	@RequestMapping(value = "/updateSupplierMatrix", method = RequestMethod.POST)
	public ResponseEntity<Object> updateSupplierMatrix(@RequestBody Map<String, Object> inputMap) throws Exception {

//		try {
//			final ObjectMapper objmapper = new ObjectMapper();
//			final SupplierMatrix supplierMatrix = objmapper.convertValue(inputMap.get("suppliermatrix"), SupplierMatrix.class);
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			return supplierMatrixService.updateSupplierMatrix(supplierMatrix, userInfo);
			
			final ObjectMapper objmapper = new ObjectMapper();
			objmapper.registerModule(new JavaTimeModule());
			final List<SupplierMatrix> supplierMatrix = objmapper.convertValue(inputMap.get("suppliermatrix"), new TypeReference<List<SupplierMatrix>>() {});
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			
			requestContext.setUserInfo(userInfo);
			return supplierMatrixService.createSupplierMatrix(supplierMatrix, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	
	/**
	 * This method is used to delete suppliermatrix for the specified Site.
	 * @param mapObject [Map] object with keys of suppliermatrix entity.
	 * @return response entity of deleted suppliermatrix entity
	 */
	@RequestMapping(value = "/deleteSupplierMatrix", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteSupplierMatrix(@RequestBody Map<String, Object> inputMap) throws Exception{

	//	try {
			final ObjectMapper objmapper = new ObjectMapper();
			objmapper.registerModule(new JavaTimeModule());
			final SupplierMatrix supplierMatrix = objmapper.convertValue(inputMap.get("suppliermatrix"), SupplierMatrix.class);
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			
			requestContext.setUserInfo(userInfo);
			return supplierMatrixService.deleteSupplierMatrix(supplierMatrix, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
}

