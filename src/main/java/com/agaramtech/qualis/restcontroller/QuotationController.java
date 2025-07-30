package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.quotation.model.Quotation;
import com.agaramtech.qualis.quotation.model.QuotationTest;
import com.agaramtech.qualis.quotation.model.QuotationTotalAmount;
import com.agaramtech.qualis.quotation.service.quotation.QuotationService;
import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Quotation Service methods
 * 
 
 */
@RestController
@RequestMapping("/quotation")
public class QuotationController {

	private RequestContext requestContext;
	private final QuotationService quotationService;

	public QuotationController(RequestContext requestContext, QuotationService quotationService) {
		super();
		this.requestContext = requestContext;
		this.quotationService = quotationService;
	}

	/**
	 * This Method is used to get the over all quotations with respect to site
	 * 
	 * @param inputMap [Map] contains key userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of quotation with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotation")
	public ResponseEntity<Object> getProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.getQuotation(userInfo);


	}
	
	/**
	 * This method is used to add a new entry to quotation table.
	 * 
	 * @param inputMap [Map] holds the quotation object to be inserted
	 * @return inserted quotation object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createQuotation")
	public ResponseEntity<Object> createQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Quotation Quotation = objmapper.convertValue(inputMap.get("quotation"), Quotation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return quotationService.createQuotation(Quotation, userInfo);
	}
	
	/**
	 * This method is used to update entry in quotation table.
	 * 
	 * @param inputMap [Map] holds the quotation object to be updated
	 * @return response entity object holding response status and data of updated
	 *         quotation object
	 * @throws Exception
	 */
	
	@PostMapping(value = "/updateQuotation")
	public ResponseEntity<Object> updateQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Quotation Quotation = objMapper.convertValue(inputMap.get("quotation"), Quotation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.updateQuotation(Quotation, userInfo);

	}
	
	/**
	 * This method is used to get the single record in quotation table
	 * 
	 * @param inputMap [Map] holds the quotation code to get
	 * @return response entity object holding response status and data of single
	 *         quotation object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveQuotationById")
	public ResponseEntity<Object> getActiveQuotationById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final int nquotationcode = (int) inputMap.get("nquotationcode");
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return quotationService.getActiveQuotationById(nquotationcode, userInfo);

	}
	
	/**
	 * This method id used to delete an entry in Quotation table
	 * 
	 * @param inputMap [Map] holds the quotation object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         quotation object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteQuotation")
	public ResponseEntity<Object> deleteQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Quotation Quotation = objmapper.convertValue(inputMap.get("quotation"), Quotation.class);		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return quotationService.deleteQuotation(Quotation, userInfo);
	}
	
	/**
	 * This method is used to approve quotation for the specified Site.
	 * @param mapObject [Map] object with keys of quotation entity.
	 * @return response entity of approved quotation entity
	 */
	@PostMapping(value = "/approveQuotation")
	public ResponseEntity<Object> approveQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final Quotation approveQuotation = objectMapper.convertValue(inputMap.get("quotation"),
				Quotation.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return quotationService.approveQuotation(approveQuotation, userInfo);
	}
	
	/**
	 * This Method is used to get the over all quotation types with respect to site
	 * 
	 * @param inputMap [Map] contains key userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of quotationtype with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotationType")
	public ResponseEntity<Object> getQuotationType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return quotationService.getQuotationType(userInfo);

	}
	
	/**
	 * This Method is used to get the over all quotation test with respect to site
	 * 
	 * @param inputMap [Map] contains key nquotationcode and userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of quotationtest with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotationUnmappedTest")
	public ResponseEntity<Object> getQuotationUnmappedTest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final Integer nquotationcode = (Integer) inputMap.get("nquotationcode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return quotationService.getQuotationUnmappedTest(nquotationcode, userInfo);
	}
	
	/**
	 * This method is used to add a new entry to quotationtest table.
	 * 
	 * @param inputMap [Map] holds the QuotationTest object to be inserted
	 * @return inserted quotationtest object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	
	@PostMapping(value = "/createQuotationTest")
	public ResponseEntity<Object> createQuotationTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final List<QuotationTest> QuotationTest = objmapper.convertValue(inputMap.get("QuotationTest"),new TypeReference<List<QuotationTest>>() {});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return quotationService.createQuotationTest(QuotationTest, userInfo);
	}
	
	/**
	 * This Method is used to get the over all quotation price with respect to site
	 * 
	 * @param inputMap [Map] contains key nquotationcode, nquotationtestcode and userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of QuotationPrice with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotationPrice")
	public ResponseEntity<Object> getQuotationPrice(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final Integer nquotationtestcode = (Integer) inputMap.get("nquotationtestcode");	
		final Integer nquotationcode = (Integer) inputMap.get("nquotationcode");
		
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.getQuotationPrice(nquotationcode, nquotationtestcode, userInfo);
		
	}
	
	/**
	 * This method is used to update entry in quotationtest table.
	 * 
	 * @param inputMap [Map] holds the QuotationTest object to be updated
	 * @return response entity object holding response status and data of updated
	 *         quotationtest object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateQuotationTest")
	public ResponseEntity<Object> updateQuotationTest(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final List<QuotationTest> QuotationTestList = objectMapper.convertValue(inputMap.get("QuotationTest"), new TypeReference<List<QuotationTest>>(){});
		final Integer nquotationcode = (Integer) inputMap.get("nquotationcode");	
		
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.updateQuotationTest(QuotationTestList, nquotationcode, userInfo);
		
	}
	
	/**
	 * This Method is used to get the over all quotationtest with respect to site
	 * 
	 * @param inputMap [Map] contains key nquotationcode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of quotationtest with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotationTest")
	public ResponseEntity<Object> getQuotationTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final int nquotationcode = (Integer) inputMap.get("nquotationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.getQuotationTest(nquotationcode, userInfo);

	}
	
	/**
	 * This Method is used to get the over all quotationgrossamount with respect to site
	 * 
	 * @param inputMap [Map] contains key nquotationcode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of quotationtotalaount with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getQuotationGrossAmount")
	public ResponseEntity<Object> getQuotationGrossAmount(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final int nquotationcode = (Integer) inputMap.get("nquotationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.getQuotationGrossAmount(nquotationcode, userInfo);

	}
	
	/**
	 * This method is used to add a new entry to quotationtotalamount table.
	 * 
	 * @param inputMap [Map] holds the GrossQuotation object to be inserted
	 * @return inserted GrossQuotation object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createGrossQuotation")
	public ResponseEntity<Object> createGrossQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final QuotationTotalAmount GrossQuotation = objmapper.convertValue(inputMap.get("GrossQuotation"), QuotationTotalAmount.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return quotationService.createGrossQuotation(GrossQuotation, userInfo);
	}
	
	/**
	 * This Method is used to get the over all grossQuotation with respect to site
	 * 
	 * @param inputMap [Map] contains key nquotationcode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of grossquotation with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getGrossQuotation")
	public ResponseEntity<Object> getGrossQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final int nquotationcode = (Integer) inputMap.get("nquotationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return quotationService.getGrossQuotation(nquotationcode, userInfo);

	}
	
	/**
	 * This method id used to delete an entry in quotationtest table
	 * 
	 * @param inputMap [Map] holds the QuotationTestPrice object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         QuotationTestPrice object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteQuotationPrice")
	public ResponseEntity<Object> deleteQuotationPrice(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final QuotationTest quotationtestPrice = objectMapper.convertValue(inputMap.get("QuotationTestPrice"), QuotationTest.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		return quotationService.deleteQuotationPrice(quotationtestPrice, userInfo);
	}	
	
	/**
	 * This Method is used to get the over all product with respect to productcategory
	 * 
	 * @param inputMap [Map] contains key nproductcatcode which holds the value of
	 *                 respective product code
	 * @return a response entity which holds the list of product with respect to productCategory
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getProductByCategory")
	public ResponseEntity<Object> getProductByCategory(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			final int nproductcatcode = (int) inputMap.get("nproductcatcode");
			requestContext.setUserInfo(userInfo);
			return quotationService.getProductByCategory(nproductcatcode, userInfo);

	}
	
	/**
	 * This Method is used to get the over all clientCategory with respect to site
	 * 
	 * @param inputMap [Map] contains key userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of clientcategory with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getClientCategory")
	public ResponseEntity<Object> getClientCategory(@RequestBody Map<String, Object> inputMap)throws Exception {

			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);			
			return quotationService.getClientCategory(userInfo);
		
	}
	
	/**
	 * This Method is used to get the over all productCategory with respect to site
	 * 
	 * @param inputMap [Map] contains key userinfo which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of productcategory with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	
	@PostMapping(value = "/getProductCategory")
	public ResponseEntity<Object> getProductCategory(@RequestBody Map<String, Object> obj) throws Exception {

		ResponseEntity<Object> lstSampleCategory = null;
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		//final int siteCode = (Integer) obj.get("nsitecode");
		lstSampleCategory= quotationService.getProductCategory(userInfo);
		 
		return lstSampleCategory;
		
	}
	
	/**
	 * This method is used to get the single record in client table
	 * 
	 * @param inputMap [Map] holds the nclientcode to get
	 * @return response entity object holding response status and data of single
	 *         client object
	 * @throws Exception
	 */
	@PostMapping(value = "/getSelectedClientDetail")
	public ResponseEntity<Object> getSelectedClientDetail(@RequestBody Map<String, Object> inputMap)  throws Exception{
		
		final ObjectMapper objmapper = new ObjectMapper();				
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		final int nclientCode = (Integer) inputMap.get("nclientcode");
		requestContext.setUserInfo(userInfo);
		return quotationService.getSelectedClientDetail(userInfo, nclientCode);
	}
	
	/**
	 * This method is used to retire quotation for the specified Site.
	 * @param mapObject [Map] object with keys of quotation entity.
	 * @return response entity of retire quotation entity
	 */
	
	@PostMapping(value = "/retireQuotation")
	public ResponseEntity<Object> retireQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final Quotation retireQuotation = objectMapper.convertValue(inputMap.get("quotation"),Quotation.class);
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);

		return quotationService.retireQuotation(retireQuotation, userInfo);
	}
	
	
	
}
