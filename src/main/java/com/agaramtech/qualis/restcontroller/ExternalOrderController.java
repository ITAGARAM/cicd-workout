package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.service.unit.UnitService;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.externalorder.service.ExternalOrderService;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/externalorder")
public class ExternalOrderController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOrderController.class);

	private RequestContext requestContext;
	private final ExternalOrderService externalOrderService;
	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param externalOrderService ExternalOrderService
	 */
	public ExternalOrderController(RequestContext requestContext, ExternalOrderService externalOrderService) {
		super();
		this.requestContext = requestContext;
		this.externalOrderService = externalOrderService;
	}
	
	@PostMapping(value = "/getExternalOrder")
	public ResponseEntity<Object> getExternalOrder(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		final int nexternalOrderCode=0;
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getExternalOrder(nexternalOrderCode,userInfo);
	}
	
	@PostMapping(value = "/getExternalOrderClickDetails")
	public ResponseEntity<Object> getExternalOrderClickDetails(@RequestBody Map<String, Object> inputMap) throws Exception{
	
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	        final int nportalordercode=objmapper.convertValue(inputMap.get("nportalordercode"), Integer.class);
			requestContext.setUserInfo(userInfo);
			return externalOrderService.getExternalOrderClickDetails(nportalordercode,userInfo);
	
	}
	
	@PostMapping(value = "/createExternalOrder")
	public ResponseEntity<Object> createExternalOrder(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		int needfilter;
		if (inputMap.containsKey("noneedfilter")) {
			needfilter=(int) inputMap.get("noneedfilter");
		}else {
			needfilter=-1;
		}
		
		final ExternalOrder objExternalOrder = objMapper.convertValue(inputMap.get("externalorder"), 
				new TypeReference<ExternalOrder>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),
				new TypeReference<UserInfo>() {});
		
		requestContext.setUserInfo(userInfo);
		return externalOrderService.createExternalOrder(objExternalOrder,needfilter, userInfo);
	
	}
	
	@PostMapping(value = "/receivedExternalOrder")
	public ResponseEntity<Object> receivedExternalOrder(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		
		final ExternalOrder objOrderDetail = objMapper.convertValue(inputMap.get("portalorder"), new TypeReference<ExternalOrder>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		
		requestContext.setUserInfo(userInfo);
		return externalOrderService.receivedExternalOrder(objOrderDetail, userInfo);
	}
	
	
	
	@PostMapping(value = "/getProductCategory")
	public ResponseEntity<Object> getProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getProductCategory(userInfo);
	
	} 
	
	@PostMapping(value = "/getComponent")
	public ResponseEntity<Object> getComponent(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getComponent(userInfo);
	
	} 
	
	@PostMapping(value = "/getTestMaster")
	public ResponseEntity<Object> getTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getTestMaster(userInfo);
	
	} 
	
	@PostMapping(value = "/getDiagnosticcase")
	public ResponseEntity<Object> getDiagnosticcase(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getDiagnosticcase(userInfo);
	
	} 
	
	@PostMapping(value = "/getPriority")
	public ResponseEntity<Object> getPriority(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getPriority(userInfo);
	
	} 
	
	@PostMapping(value = "/getTestPackageTest")
	public ResponseEntity<Object> getTestPackageTest(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getTestPackageTest(userInfo);
	
	} 
	
	@PostMapping(value = "/getTestGroupProfile")
	public ResponseEntity<Object> getTestGroupProfile(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getTestGroupProfile(userInfo);
	
	}
	
	@PostMapping(value = "/updatePortalOrderStatus")
	public ResponseEntity<Object> updatePortalOrderStatus(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		final String sorderseqno = (String) inputMap.get("sorderseqno");
		
		final short ntransactionstatus = ((Integer) inputMap.get("ntransactionstatus")).shortValue();
		final String sordersampleno =  inputMap.containsKey("sordersampleno") ? (String) inputMap.get("sordersampleno"):"" ;
		requestContext.setUserInfo(userInfo); 
		return externalOrderService.updatePortalOrderStatus(sorderseqno,sordersampleno,ntransactionstatus,userInfo);
	}
	
	@PostMapping(value="/getActiveExternalOrderById")
	public ResponseEntity<Object> getActiveExternalOrderById(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objmapper = new ObjectMapper();
		int nexternalordercode=(int) inputMap.get("nexternalordercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) externalOrderService.getActiveExternalOrderById(nexternalordercode,userInfo);	
	}
	
	@PostMapping(value = "/updateExternalOrder")
	public ResponseEntity<Object> updateExternalOrder(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		int needfilter;
		if (inputMap.containsKey("noneedfilter")) {
			needfilter=(int) inputMap.get("noneedfilter");
		}else {
			needfilter=-1;
		}
		final ExternalOrder externalorder = objMapper.convertValue(inputMap.get("externalorder"), ExternalOrder.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.updateExternalOrder(externalorder,needfilter, userInfo);
	}
	
	@PostMapping(value = "/getDraftExternalOrderDetails")
	public ResponseEntity<Object> getDraftExternalOrderDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final String sexternalorderid = (String)(inputMap.get("sexternalorderid"));
		final int nexteralordertypecode = (int) inputMap.get("nexternalordertypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getDraftExternalOrderDetails(sexternalorderid,userInfo,nexteralordertypecode);
	}
	
	
	@PostMapping(value = "/onUpdateCancelExternalOrder")
	public ResponseEntity<Object> onUpdateCancelExternalOrder(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final String sexternalordersamplecode = (String)(inputMap.get("sexternalordersamplecode"));
		final String sexternalordercode = (String)(inputMap.get("sexternalordercode"));
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.onUpdateCancelExternalOrder(sexternalordercode,sexternalordersamplecode,inputMap,userInfo);
	}

	
	@PostMapping(value = "/outSourceTest")	
	public ResponseEntity<Object> outSourceTest(@RequestBody Map<String, Object> inputMap) throws Exception{
				
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		final RegistrationTest testInput = objMapper.convertValue(inputMap.get("registrationtest"), RegistrationTest.class);
		final int destinationSitecode=(int) inputMap.get("destinationsitecode");
		final int designTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
		return externalOrderService.outSourceTest(testInput, destinationSitecode, designTemplateMappingCode, userInfo);
		
	}	
	
	@PostMapping(value = "/getOutSourceSite")
	public ResponseEntity<Object> getOutSourceSite(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		final RegistrationTest testInput = objMapper.convertValue(inputMap.get("registrationtest"), RegistrationTest.class);
	
		return externalOrderService.getOutSourceSite(testInput, userInfo);		
	}
	
	
	@PostMapping(value = "/getOutSourceSiteAndTest")
	public ResponseEntity<Object> getOutSourceSiteAndTest(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		final RegistrationTest testInput = objMapper.convertValue(inputMap.get("registrationtest"), RegistrationTest.class);
	
		return externalOrderService.getOutSourceSiteAndTest(testInput, userInfo);		
	}
	
	@PostMapping(value = "/getRegion")
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {	});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getRegion(userInfo);
	}
	
	@PostMapping(value = "/getTestCategory")
	public ResponseEntity<Object> getTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getTestCategory(userInfo);
	}
	
	@PostMapping(value = "/getCity")
	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),
					new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getCity(userInfo);
	}
	
	@PostMapping(value = "/getDistrict")
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getDistrict(userInfo);
	}
	
	@PostMapping(value = "/getCountry")
	public ResponseEntity<Object> getCountry(@RequestBody Map<String, Object> inputMap)throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getCountry(userInfo);//.getNmastersitecode());
	}
	
	@PostMapping(value = "/getSubmitterDetailByAll")
	public ResponseEntity<Object> getSubmitterDetailByAll(@RequestBody Map<String, Object> inputMap)  throws Exception{
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getSubmitterDetailByAll(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getInstitutionSitebyAll")
	public ResponseEntity<Object> getInstitutionSitebyAll(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getInstitutionSitebyAll(userInfo);	
	}
	
	@PostMapping(value = "/getInstitutionValues")
	public ResponseEntity<Object> getInstitutionValues(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getInstitutionValues(userInfo);	
	}
	
	@PostMapping(value = "/getTestPackage")
	public ResponseEntity<Object> getTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getTestPackage(userInfo);
		 
	}
	
	@PostMapping(value = "/getInstitutionDepartment")
	public ResponseEntity<Object> getInstitutionDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getInstitutionDepartment(userInfo);
	}
	
	@PostMapping(value = "/getUnit")
	public ResponseEntity<Object> getUnit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getUnit(userInfo);

	}
	
//	@PostMapping(value="/getPackageMaster")
//	public ResponseEntity<Object> getPackageMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
//		
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
//		requestContext.setUserInfo(userInfo);
//		return externalOrderService.getPackageMaster(userInfo);
//		
//	}
	
	@PostMapping(value = "/getSiteScreen")
	public ResponseEntity<Object> getSiteScreen(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper mapper=new ObjectMapper();
		Integer nsitecode = null;
		if (inputMap.get("nsitecode") != null) {
			nsitecode = (Integer) inputMap.get("nsitecode");	
		}
	
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getSiteScreen(nsitecode,userInfo);
		
	}
	
	@PostMapping(value = "/getPatient")
	public ResponseEntity<Object> getPatient(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		
		String spatientid = null;
		if (inputMap.get("spatientid") != null) {
			spatientid = (String) inputMap.get("spatientid");
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getPatient(spatientid, userInfo);
	}
	
	@PostMapping(value = "/getActivePatientById")
	public Patient getActivePatientById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final String spatientid = (String) inputMap.get("spatientid");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return externalOrderService.getActivePatientById(spatientid, userInfo);

	}
	
	@PostMapping(value = "/getInstitutionCategory")
	public ResponseEntity<Object> getInstitutionCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getInstitutionCategory(userInfo);
	}
	
	@PostMapping(value = "/getProduct", headers = "Accept=application/json")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objmap = new ObjectMapper();
		final UserInfo userInfo = objmap.convertValue(obj.get("userinfo"), UserInfo.class);
		Integer nproductcode = null;
		
		if (obj.get("nproductcode") != null) {
			nproductcode = (Integer) obj.get("nproductcode");	
		}
		requestContext.setUserInfo(userInfo);
		return  externalOrderService.getProduct(nproductcode,userInfo);
	}
	
	@PostMapping(value = "/getsamplepriority")
	public ResponseEntity<Object> getSamplePriority(@RequestBody Map<String, Object> inputMap) throws Exception {

	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getSamplePriority(userInfo);

	}
	
	@PostMapping(value = "/getSite")
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper mapper=new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getSite(userInfo);

	}
	
	@PostMapping(value = "/outSourceSampleTest")	
	public ResponseEntity<Object> outSourceSampleTest(@RequestBody Map<String, Object> inputMap) throws Exception{
				
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final List<RegistrationTest> listTest = objMapper.convertValue(inputMap.get("selectedTest"),
				new TypeReference<List<RegistrationTest>>() {
				});
		final RegistrationTest testInput = objMapper.convertValue(inputMap.get("registrationtest"), RegistrationTest.class);
		final RegistrationTest otherdetails = objMapper.convertValue(inputMap.get("otherdetails"), RegistrationTest.class);
		final Registration outsourceSampleData = objMapper.convertValue(inputMap.get("outSourceSampleData"), Registration.class);
		final int destinationSitecode=(int) inputMap.get("destinationsitecode");
		final int designTemplateMappingCode = (int) inputMap.get("ndesigntemplatemappingcode");
		return externalOrderService.outSourceSampleTest(testInput,otherdetails,listTest, destinationSitecode, designTemplateMappingCode,outsourceSampleData, userInfo);
		
	}

	
	@PostMapping(value = "/createExternalOrderPTB")
	public ResponseEntity<Object> createExternalOrderPTB(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final ExternalOrder objExternalOrder = objMapper.convertValue(inputMap.get("externalorder"), 
				new TypeReference<ExternalOrder>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.createExternalOrderPTB(objExternalOrder, userInfo);
	
	}
	
	@PostMapping(value = "/getExternalOrderType")
	public ResponseEntity<Object> getExternalOrderType(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getExternalOrderType(userInfo);
	
	} 
	
	@PostMapping(value = "/updateOrderSampleStatus")
	public ResponseEntity<Object> updateOrderSampleStatus(@RequestBody Map<String, Object> inputMap) throws Exception{
	
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class); 
		requestContext.setUserInfo(userInfo);
		return externalOrderService.updateOrderSampleStatus(userInfo, inputMap);
	
	} 		
	
	@PostMapping(value = "/getsampleappearance")
	public ResponseEntity<Object> getSampleappearance(@RequestBody Map<String, Object> inputMap) throws Exception {

	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return externalOrderService.getSampleappearance(userInfo);

	}
	

//	@PostMapping(value = "/cancelExternalOrderPTb", method = RequestMethod.POST)
//	public ResponseEntity<Object> cancelExternalOrderPTb(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
//		requestContext.setUserInfo(userInfo);
//		return externalOrderService.cancelExternalOrderPreventTb(inputMap, userInfo);
//	
//	}
// Sonia Commented this method for ALPD-4184 	
//	@Scheduled(cron =  "0 */8 * ? * *")
//	@RequestMapping(value="/SendToPortalReport",method=RequestMethod.POST)
//	public ResponseEntity<Object> SendToPortalReport() throws Exception {	
//		final ObjectMapper objMapper = new ObjectMapper();
//		Map<String,Object> obj=new HashMap<String, Object>();
//		final UserInfo userInfo = null;
//		return externalOrderService.SendToPortalReport(userInfo);
//	}
	

//	@PostMapping(value = "/createExternalOrderOpenMrs")
//	public ResponseEntity<Object> createExternalOrderOpenMrs(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final ExternalOrder objExternalOrder = objMapper.convertValue(inputMap.get("externalorder"), new TypeReference<ExternalOrder>() {
//		});
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return externalOrderService.createExternalOrderOpenMrs(objExternalOrder, userInfo);
//	
//	}

}

