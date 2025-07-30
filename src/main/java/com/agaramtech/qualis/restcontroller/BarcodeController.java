package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.basemaster.service.barcode.BarcodeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the MaterialCategory Service methods.
 */
@RestController
@RequestMapping("/barcode")
public class BarcodeController {

	private static final Log LOGGER = LogFactory.getLog(BarcodeController.class);

	private final BarcodeService objBarcodeService;

	private final RequestContext requestContext;

	public BarcodeController(RequestContext requestContext, BarcodeService objBarcodeService) {
		super();
		this.requestContext = requestContext;
		this.objBarcodeService = objBarcodeService;
	}

	/**
	 * This method is used to retrieve list of active barcode for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active barcode that are to be listed for
	 *         the specified site
	 */

	@PostMapping(value = "/getBarcode")
	public ResponseEntity<Object> getBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSampleAppearance -->");
		return objBarcodeService.getBarcode(userInfo);
	}

	/**
	 * This method is used to delete barcode for the specified Site.
	 * 
	 * @param inputMap [mapObject] object with keys of barcode entity.
	 * @return response entity of deleted barcode entity
	 */
	@PostMapping(value = "/deleteBarcode")
	public ResponseEntity<Object> deleteBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Barcode barcode = objmapper.convertValue(inputMap.get("barcode"), Barcode.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeService.deleteBarcode(barcode, userInfo);
	}

	/**
	 * This method is used to retrieve selected active barcode detail.
	 * 
	 * @param inputMap [Map] map object with "nbarcode" and "userInfo" as keys for
	 *                 which the data is to be fetched
	 * @return response object with selected active barcode
	 */
	@PostMapping(value = "/getActiveBarcodeById")
	public ResponseEntity<Object> getActiveBarcodeById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nbarcode = (Integer) inputMap.get("nbarcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeService.getActiveBarcodeById(nbarcode, userInfo);
	}

	/**
	 * This method is used to add new barcode for the specified Site.
	 * 
	 * @param request object with keys of barcode entity.
	 * @return response entity of newly added barcode entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/createBarcode")
	public ResponseEntity<Object> createBarcode(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeService.createBarcode(request, userInfo);
	}

	/**
	 * This method is used to retrieve list of active sqlquery and controlname
	 * record of specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active barcode that are to be listed for
	 *         the specified site
	 */

	@PostMapping(value = "/getSqlQuery")
	public ResponseEntity<Object> getSqlQuery(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeService.getSqlQuery(userInfo);
	}

	/**
	 * This method is used to update the barcode
	 * 
	 * @param request holds the object of barcode, userinfo
	 * @return response entity holds the list of barcode and its parameter details
	 * @throws Exception from the DAO Layer
	 */
	@PostMapping(value = "/updateBarcode")
	public ResponseEntity<Object> updateBarcode(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return objBarcodeService.updateBarcode(request, objUserInfo);
	}

	/**
	 * This method declaration is view the file attached in barcode screen
	 * 
	 * @param inputMap holds the file details to be viewed
	 * @return response entity holds the list of barcode and its parameter details
	 * @throws Exception that are thrown from this DAO layer
	 */

	@PostMapping(value = "/viewAttachedDownloadFile")
	public ResponseEntity<Object> viewAttachedDownloadFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Barcode objTestFile = objMapper.convertValue(inputMap.get("downloadfile"), Barcode.class);
		requestContext.setUserInfo(objUserInfo);
		return objBarcodeService.viewAttachedDownloadFile(objTestFile, objUserInfo);
	}

	@PostMapping(value = "/getPrinter")
	public ResponseEntity<Object> getPrinter(@RequestBody Map<String, Object> inputMap) throws Exception {
		return objBarcodeService.getPrinter();
	}

	@PostMapping(value = "/printerBarcode")
	public ResponseEntity<Object> PrintBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		return objBarcodeService.PrintBarcode(inputMap);
	}

	@PostMapping(value = "/getControlBasedBarcode")
	public ResponseEntity<Object> getControlBasedBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		return objBarcodeService.getControlBasedBarcode(ncontrolcode);
	}
}
