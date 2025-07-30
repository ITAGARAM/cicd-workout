package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Supplier;
import com.agaramtech.qualis.contactmaster.model.SupplierContact;
import com.agaramtech.qualis.contactmaster.model.SupplierFile;
import com.agaramtech.qualis.contactmaster.service.supplier.SupplierService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Supplier Master Service methods.
 * 
 * @author ATE090
 * @version
 * @since 30- Jun- 2020
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierController.class);

	private RequestContext requestContext;
	private SupplierService supplierService;
	
	public SupplierController(RequestContext requestContext, SupplierService supplierService) {
		super();
		this.requestContext = requestContext;
		this.supplierService = supplierService;
	}

	/**
	 * This method is used to retrieve list of active suppliers for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active suppliers that are to be listed
	 *         for the specified site
	 */
	@PostMapping(value = "/getSupplier")
	public ResponseEntity<Object> getSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		ObjectMapper objMapper = new ObjectMapper();
		Integer nsupplierCode = null;
		if (inputMap.get("nsuppliercode") != null) {
			nsupplierCode = (Integer) inputMap.get("nsuppliercode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		// final int siteCode = (Integer) inputMap.get("nsitecode");

		requestContext.setUserInfo(userInfo);
		return supplierService.getSupplier(nsupplierCode, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to add new supplier for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of supplier entity.
	 * @return response entity of newly added supplier entity
	 */
	@PostMapping(value = "/createSupplier")
	public ResponseEntity<Object> createSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		// final Supplier supplier = objmapper.convertValue(inputMap.get("supplier"),
		// Supplier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		// return supplierService.createSupplier(supplier, userInfo);
		return supplierService.createSupplier(inputMap, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to retrieve selected active supplier detail.
	 * 
	 * @param inputMap [Map] map object with "nsuppliercode" and "userInfo" as keys
	 *                 for which the data is to be fetched
	 * @return response object with selected active supplier
	 */
	@PostMapping(value = "/getActiveSupplierById")
	public ResponseEntity<Object> getActiveSupplierById(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsupplierCode = (Integer) inputMap.get("nsuppliercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return supplierService.getActiveSupplierById(nsupplierCode, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());			
//			System.out.println(e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to update supplier for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of supplier entity.
	 * @return response entity of updated supplier entity
	 */
	@PostMapping(value = "/updateSupplier")
	public ResponseEntity<Object> updateSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Supplier supplier = objmapper.convertValue(inputMap.get("supplier"), Supplier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return supplierService.updateSupplier(supplier, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to delete supplier for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of supplier entity.
	 * @return response entity of deleted supplier entity
	 */
	@PostMapping(value = "/deleteSupplier")
	public ResponseEntity<Object> deleteSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Supplier supplier = objmapper.convertValue(inputMap.get("supplier"), Supplier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return supplierService.deleteSupplier(supplier, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to approve Supplier for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of Supplier entity.
	 * @return response entity of deleted supplier entity
	 */
	@PostMapping(value = "/approveSupplier")
	public ResponseEntity<Object> approveSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Supplier supplier = objmapper.convertValue(inputMap.get("supplier"), Supplier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return supplierService.approveSupplier(supplier, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to blacklist Supplier for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of Supplier entity.
	 * @return response entity of deleted supplier entity
	 */
	@PostMapping(value = "/blackListSupplier")
	public ResponseEntity<Object> blackListSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Supplier supplier = objmapper.convertValue(inputMap.get("supplier"), Supplier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return supplierService.blackListSupplier(supplier, userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}

	/**
	 * This method is used to retrieve list of active approved suppliers for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active suppliers that are to be listed
	 *         for the specified site
	 */
	@PostMapping(value = "/getApprovedSupplier")
	public ResponseEntity<Object> getApprovedSupplier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.getApprovedSupplier(userInfo);
	}

	@PostMapping(value = "/createSupplierFile")
	public ResponseEntity<Object> createSupplierFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.createSupplierFile(request, userInfo);

	}

	@PostMapping(value = "/updateSupplierFile")
	public ResponseEntity<Object> updateSupplierFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.updateSupplierFile(request, userInfo);

	}

	@PostMapping(value = "/deleteSupplierFile")
	public ResponseEntity<Object> deleteSupplierFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SupplierFile objSupplierFile = objMapper.convertValue(inputMap.get("supplierfile"), SupplierFile.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.deleteSupplierFile(objSupplierFile, userInfo);
	}

	@PostMapping(value = "/editSupplierFile")
	public ResponseEntity<Object> editSupplierFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SupplierFile objSupplierFile = objMapper.convertValue(inputMap.get("supplierfile"), SupplierFile.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.editSupplierFile(objSupplierFile, userInfo);

	}

	@PostMapping(value = "/viewAttachedSupplierFile")
	public ResponseEntity<Object> viewAttachedSupplierFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SupplierFile objSupplierFile = objMapper.convertValue(inputMap.get("supplierfile"), SupplierFile.class);
		Map<String, Object> outputMap = supplierService.viewAttachedSupplierFile(objSupplierFile, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@PostMapping(value = "/getSupplierContact")
	public ResponseEntity<Object> getSupplierContact(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nsuppliercode = (int) inputMap.get("nsuppliercode");
		requestContext.setUserInfo(userInfo);
		return supplierService.getSupplierContact(nsuppliercode, userInfo);
	}

	@PostMapping(value = "/createSupplierContact")
	public ResponseEntity<Object> createSupplierContact(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SupplierContact objSupplierContact = objmapper.convertValue(inputMap.get("suppliercontact"),
				SupplierContact.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return supplierService.createSupplierContact(objSupplierContact, userInfo);
	}

	@PostMapping(value = "/updateSupplierContact")
	public ResponseEntity<Object> updateSupplierContact(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SupplierContact objSupplierContact = objmapper.convertValue(inputMap.get("suppliercontact"),
				SupplierContact.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.updateSupplierContact(objSupplierContact, userInfo);
	}

	@PostMapping(value = "/deleteSupplierContact")
	public ResponseEntity<Object> deleteSupplierContact(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final SupplierContact objSupplierContact = objmapper.convertValue(inputMap.get("supplier"),
				SupplierContact.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.deleteSupplierContact(objSupplierContact, userInfo);
	}

	@PostMapping(value = "/getActiveSupplierContactById")
	public ResponseEntity<Object> getActiveSupplierContactById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsuppliercontactcode = (Integer) inputMap.get("nsuppliercontactcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return supplierService.getActiveSupplierContactById(nsuppliercontactcode, userInfo);
	}

}
