package com.agaramtech.qualis.materialmanagement.service.material;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

	private final MaterialDAO objMaterialDAO;
	private final JdbcTemplate jdbcTemplate;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getMaterialType(final UserInfo userInfo, final String currentUIDate)
			throws Exception {
		return objMaterialDAO.getMaterialType(userInfo, currentUIDate);
	}

	@Override
	public ResponseEntity<Object> getMaterialcombo(final Integer nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialDAO.getMaterialcombo(nmaterialtypecode, objUserInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialAdd(final short nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialDAO.getMaterialAdd(nmaterialtypecode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createMaterial(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialDAO.createMaterial(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialDAO.getMaterialDetails(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialDAO.deleteMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialEdit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialDAO.getMaterialEdit(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialDAO.updateMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialByTypeCode(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return objMaterialDAO.getMaterialByTypeCode(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialByID(final Map<String, Object> inputMap) throws Exception {
		return objMaterialDAO.getMaterialByID(inputMap);
	}

	@Override
	public ResponseEntity<Object> getMaterialSection(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialDAO.getMaterialSection(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.createMaterialSafetyInstructions(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> getMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final int countcheck = jdbcTemplate.queryForObject(
				"select count(0) from material where nmaterialcode in (" + inputMap.get("nmaterialcode")
						+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				Integer.class);
		if (countcheck > 0) {
			return objMaterialDAO.getMaterialSafetyInstructions(inputMap, objUserInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.createMaterialMsdsAttachment(request, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> editMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.editMaterialMsdsAttachment(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.updateMaterialMsdsAttachment(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.deleteMaterialMsdsAttachment(inputMap, objUserInfo);
	}

	//ALPD-5963
	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.viewAttachedMaterialMsdsAttachment(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialLinkMaster(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		return objMaterialDAO.getMaterialLinkMaster(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialAcountingProperties(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.getMaterialAcountingProperties(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialInventory(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		return objMaterialDAO.createMaterialInventory(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialAvailableQty(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialDAO.getMaterialAvailableQty(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUraniumContentType(final UserInfo userInfo) throws Exception {
		return objMaterialDAO.getUraniumContentType(userInfo);
	}
}
