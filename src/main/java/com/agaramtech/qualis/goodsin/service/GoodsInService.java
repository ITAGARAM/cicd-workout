package com.agaramtech.qualis.goodsin.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.goodsin.model.GoodsInChecklist;

public interface GoodsInService {
	
	public ResponseEntity<Object> getGoodsIn(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getGoodsInData(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getGoodsInAdd(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getGoodsInEdit(int ngoodsincode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getClient(Integer nClientCatCode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProjectType(Integer nClientCatCode,Integer nClientCode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProjectMaster(Integer nClientCatCode,Integer nClientCode,Integer nProjectTypeCode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveGoodsInById(final int ngoodsincode,UserInfo userInfo) throws Exception ;
	public ResponseEntity<Object> createGoodsIn(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateGoodsIn(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteGoodsIn(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> receiveGoodsIn(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> approveGoodsIn(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getChecklistDesign(int nchecklistversioncode,int ngoodsincode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createGoodsInChecklist(final int ngoodsincode,final int controlCode,final int ndesigntemplatemappingcode,final GoodsInChecklist objGoodsInChecklist, final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveGoodsInSampleById(final int ngoodsinsamplecode,UserInfo userInfo) throws Exception ;
	public ResponseEntity<Object> createGoodsInSample(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteGoodsInSample(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> importGoodsInSample(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> goodsinReport(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;


}
