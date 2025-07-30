package com.agaramtech.qualis.goodsin.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.goodsin.model.GoodsInChecklist;
import com.agaramtech.qualis.goodsin.model.GoodsInSample;

@Service
public class GoodsInServiceImpl implements GoodsInService{
	
	private final GoodsInDAO goodsInDAO;
	private final CommonFunction commonFunction;
	
	public GoodsInServiceImpl(GoodsInDAO goodsInDAO, CommonFunction commonFunction) {
		this.goodsInDAO = goodsInDAO;
		this.commonFunction = commonFunction;
	}
	
	@Override
	public ResponseEntity<Object> getGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.getGoodsIn(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getGoodsInData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.getGoodsInData(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getGoodsInAdd(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.getGoodsInAdd(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getGoodsInEdit(int ngoodsincode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getGoodsInEdit(ngoodsincode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getClient(Integer nClientCatCode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getClient(nClientCatCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getProjectType(Integer nClientCatCode,Integer nClientCode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getProjectType(nClientCatCode,nClientCode, userInfo);
	}	
	
	@Override
	public ResponseEntity<Object> getProjectMaster(Integer nClientCatCode,Integer nClientCode, Integer nProjectTypeCode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getProjectMaster(nClientCatCode,nClientCode,nProjectTypeCode, userInfo);
	}	

	@Override
	public ResponseEntity<Object> getActiveGoodsInById(int ngoodsincode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getActiveGoodsInById(ngoodsincode,userInfo);		
	}
	
	@Override
	public ResponseEntity<Object> createGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.createGoodsIn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> updateGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.updateGoodsIn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.deleteGoodsIn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> receiveGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.receiveGoodsIn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> approveGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.approveGoodsIn(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getChecklistDesign(int nchecklistversioncode,  int ngoodsincode, UserInfo userInfo) throws Exception {
		return goodsInDAO.getChecklistDesign(nchecklistversioncode,ngoodsincode, userInfo);
	}
	
	public ResponseEntity<Object> createGoodsInChecklist(final int ngoodsincode,final int controlCode,final int ndesigntemplatemappingcode,final GoodsInChecklist objGoodsInChecklist, final UserInfo userInfo) throws Exception	{
		return 	goodsInDAO.createGoodsInChecklist(ngoodsincode,controlCode,ndesigntemplatemappingcode,objGoodsInChecklist,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getActiveGoodsInSampleById(int ngoodsinsamplecode, UserInfo userInfo) throws Exception {
		
		final GoodsInSample goodsinsample = goodsInDAO.getActiveGoodsInSampleById(ngoodsinsamplecode,userInfo);
		if(goodsinsample==null) {			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			return new ResponseEntity<>(goodsinsample, HttpStatus.OK);
		}		
	}
	
	@Override
	public ResponseEntity<Object> createGoodsInSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.createGoodsInSample(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> deleteGoodsInSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return goodsInDAO.deleteGoodsInSample(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> importGoodsInSample(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception {
		return goodsInDAO.importGoodsInSample(request, userInfo);
	}

	public ResponseEntity<Object> goodsinReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return goodsInDAO.goodsinReport(inputMap, userInfo);
	}
	
	

}
