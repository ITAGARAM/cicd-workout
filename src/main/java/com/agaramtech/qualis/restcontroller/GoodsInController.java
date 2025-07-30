package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.goodsin.model.GoodsInChecklist;
import com.agaramtech.qualis.goodsin.service.GoodsInService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/goodsin")
public class GoodsInController {
	
	final Log logging = LogFactory.getLog(GoodsInController.class);

	private RequestContext requestContext;
	private final GoodsInService goodsInService;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public GoodsInController(RequestContext requestContext, GoodsInService goodsInService) {
		super();
		this.requestContext = requestContext;
		this.goodsInService = goodsInService;
	}
	
	@RequestMapping(value = "/getGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> getGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getGoodsIn(inputMap,userInfo);
	}
	
	@RequestMapping(value = "/getGoodsInData", method = RequestMethod.POST)
	public ResponseEntity<Object> getGoodsInData(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getGoodsInData(inputMap,userInfo);
	}
	
	@RequestMapping(value="getGoodsInAdd", method = RequestMethod.POST)
	public ResponseEntity<Object> getGoodsInAdd(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getGoodsInAdd(inputMap,userInfo);		
	}
	
	@RequestMapping(value="getGoodsInEdit", method = RequestMethod.POST)
	public ResponseEntity<Object> getGoodsInEdit(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final int ngoodsincode = (Integer) inputMap.get("ngoodsincode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getGoodsInEdit(ngoodsincode,userInfo);		
	}
	
	@RequestMapping(value = "/getClient", method = RequestMethod.POST)
	public ResponseEntity<Object> getClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nClientCatCode = (int) inputMap.get("nclientcatcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getClient(nClientCatCode,userInfo);
	}
	
	@RequestMapping(value = "/getProjectType", method = RequestMethod.POST)
	public ResponseEntity<Object> getProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nClientCatCode = (int) inputMap.get("nclientcatcode");
		int nClientCode = (int) inputMap.get("nclientcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getProjectType(nClientCatCode,nClientCode,userInfo);
	}	
	
	@RequestMapping(value = "/getProjectMaster", method = RequestMethod.POST)
	public ResponseEntity<Object> getProjectMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		int nClientCatCode = (int) inputMap.get("nclientcatcode");
		int nClientCode = (int) inputMap.get("nclientcode");
		int nProjectTypeCode = (int) inputMap.get("nprojecttypecode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getProjectMaster(nClientCatCode,nClientCode,nProjectTypeCode,userInfo);
	}	
	
	@RequestMapping(path = "/getActiveGoodsInById",method =RequestMethod.POST)
	public ResponseEntity<Object> getActiveGoodsInById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objmapper = new ObjectMapper();
		final int ngoodsincode = (Integer) inputMap.get("ngoodsincode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return goodsInService.getActiveGoodsInById(ngoodsincode, userInfo);
	}	
	
	@RequestMapping(value = "/createGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> createGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.createGoodsIn(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/updateGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> updateGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.updateGoodsIn(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/deleteGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.deleteGoodsIn(inputMap, userInfo);
	}
	@RequestMapping(value = "/receiveGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> receivedGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.receiveGoodsIn(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/approveGoodsIn", method = RequestMethod.POST)
	public ResponseEntity<Object> approveGoodsIn(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.approveGoodsIn(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/getChecklistDesign", method = RequestMethod.POST)
	public ResponseEntity<Object> getChecklistDesign(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final int nchecklistversioncode = (Integer) inputMap.get("nchecklistversioncode");
		final int ngoodsincode = (Integer) inputMap.get("ngoodsincode");

		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.getChecklistDesign(nchecklistversioncode,ngoodsincode, userInfo);
	}
	
	
	@RequestMapping(value = "/createGoodsInChecklist", method = RequestMethod.POST)
	public ResponseEntity<Object> createGoodsInChecklist(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final int ngoodsincode = (int)inputMap.get("ngoodsincode");
		final int controlCode = (int) inputMap.get("ncontrolcode");
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo);		
		final GoodsInChecklist objGoodsInChecklist = objMapper.convertValue(inputMap.get("GoodsInCheckList"),GoodsInChecklist.class);
		return goodsInService.createGoodsInChecklist(ngoodsincode,controlCode,ndesigntemplatemappingcode,objGoodsInChecklist,userInfo);

		
	}

	@RequestMapping(path = "/getActiveGoodsInSampleById",method =RequestMethod.POST)
	public ResponseEntity<Object> getActiveGoodsInSampleById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objmapper = new ObjectMapper();
		final int ngoodsinsamplecode = (Integer) inputMap.get("ngoodsinsamplecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return goodsInService.getActiveGoodsInSampleById(ngoodsinsamplecode, userInfo);
	}
	
	@RequestMapping(value = "/createGoodsInSample", method = RequestMethod.POST)
	public ResponseEntity<Object> createGoodsInSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.createGoodsInSample(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/deleteGoodsInSample", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteGoodsInSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.deleteGoodsInSample(inputMap, userInfo);
	}
	
	@RequestMapping(value = "/importGoodsInSample", method = RequestMethod.POST)
	public ResponseEntity<Object> importMultilingualProperties(MultipartHttpServletRequest request) throws Exception{
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return goodsInService.importGoodsInSample(request,userInfo);			
	} 
	
	@RequestMapping(value = "/goodsinReport", method = RequestMethod.POST)
	public ResponseEntity<Object> goodsinReport(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return goodsInService.goodsinReport(inputMap,userInfo);			
	}
	
}
