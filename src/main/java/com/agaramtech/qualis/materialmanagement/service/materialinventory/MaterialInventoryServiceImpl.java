package com.agaramtech.qualis.materialmanagement.service.materialinventory;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class MaterialInventoryServiceImpl implements MaterialInventoryService {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialInventoryServiceImpl.class);
	
	final private MaterialInventoryDAO objMaterialInventoryDAO;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param MaterialInventoryService MaterialInventoryService
	 */
	public MaterialInventoryServiceImpl( MaterialInventoryDAO objMaterialInventoryDAO) {
		super();
		//this.commonFunction = commonFunction;
		this.objMaterialInventoryDAO = objMaterialInventoryDAO;
	}


	public ResponseEntity<Object> getMaterialInventory(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getMaterialInventory(userInfo);
	}
	public ResponseEntity<Object> getMaterialInventorycombo(Integer nmaterialtypecode,Integer nmaterialcatcode,UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getMaterialInventorycombo(nmaterialtypecode,nmaterialcatcode,  objUserInfo);
	}
	@Override
	public ResponseEntity<Object> getMaterialInventoryByID(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return objMaterialInventoryDAO.getMaterialInventoryByID(inputMap, userInfo);
	}
	public ResponseEntity<Object> getMaterialInventoryAdd(Integer nmaterialtypecode,UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getMaterialInventoryAdd(nmaterialtypecode,  objUserInfo);
	}
	@Transactional
	public ResponseEntity<Object> createMaterialInventory(Map<String, Object> inputMap,UserInfo objUserInfo) throws Exception{
		//Map<String,Object> seqnoMap = new HashMap<String, Object>();
		try {
				return objMaterialInventoryDAO.createMaterialInventory(inputMap,objUserInfo);
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	@Override
	public ResponseEntity<Object> getMaterialInventoryEdit(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return objMaterialInventoryDAO.getMaterialInventoryEdit(inputMap, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> UpdateMaterialInventory(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return objMaterialInventoryDAO.UpdateMaterialInventory(inputMap, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> deleteMaterialInventory(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return objMaterialInventoryDAO.deleteMaterialInventory(inputMap, userInfo);
	}
	public ResponseEntity<Object> getMaterialInventoryDetails(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getMaterialInventoryDetails(inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> updateMaterialStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.updateMaterialStatus(inputMap,userInfo);
	}
	public ResponseEntity<Object> getMaterialInventorySearchByID(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getMaterialInventorySearchByID(inputMap,userInfo);
	}
	public ResponseEntity<Object> getQuantityTransaction(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.getQuantityTransaction(inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> createMaterialInventoryTrans(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return objMaterialInventoryDAO.createMaterialInventoryTrans(inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> editMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		return objMaterialInventoryDAO.editMaterialInventoryFile(inputMap, objUserInfo);
	}
	@Override
	public ResponseEntity<Object> getMaterialInventoryLinkMaster(Map<String, Object> inputMap,UserInfo objUserInfo) throws Exception {
		return objMaterialInventoryDAO.getMaterialInventoryLinkMaster(inputMap,objUserInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialInventoryFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialInventoryDAO.createMaterialInventoryFile(request, objUserInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterialInventoryFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialInventoryDAO.updateMaterialInventoryFile(request, objUserInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> deleteMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {

		return objMaterialInventoryDAO.deleteMaterialInventoryFile(inputMap, objUserInfo);
	}
	@Override
	public Map<String, Object> viewAttachedMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		return objMaterialInventoryDAO.viewAttachedMaterialInventoryFile(inputMap, objUserInfo);
	}
	@Override
	public ResponseEntity<Object> getChildValuesMaterial(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		return objMaterialInventoryDAO.getChildValuesMaterial(inputMap, userInfo);
	}
}
