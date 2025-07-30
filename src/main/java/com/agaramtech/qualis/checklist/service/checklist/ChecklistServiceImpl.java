package com.agaramtech.qualis.checklist.service.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.checklist.model.Checklist;
import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.checklist.model.ChecklistVersionTemplate;
import com.agaramtech.qualis.checklist.service.checklistqbcategory.QBCategoryDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;


@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ChecklistServiceImpl implements ChecklistService {

	private final ChecklistDAO checkListDAO;
	private final QBCategoryDAO qbCategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public ChecklistServiceImpl(QBCategoryDAO qbCategoryDAO, ChecklistDAO checkListDAO,
			CommonFunction commonFunction) {
		super();
		this.qbCategoryDAO = qbCategoryDAO;
		this.checkListDAO = checkListDAO;
		this.commonFunction = commonFunction;

	}

	@Override
	public ResponseEntity<Object> getChecklist(UserInfo userInfo) throws Exception {
		return checkListDAO.getChecklist(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {
		return checkListDAO.createChecklist(objChecklist, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {
		return checkListDAO.updateChecklist(objChecklist, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveChecklistById(int nchecklistCode, UserInfo userInfo) throws Exception {
		Checklist checklist = checkListDAO.getActiveChecklistById(nchecklistCode);
		if (checklist == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(checklist, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChecklist(Checklist objChecklist, UserInfo userInfo) throws Exception {
		return checkListDAO.deleteChecklist(objChecklist, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveChecklistVersionByChecklist(int nchecklistCode, UserInfo userInfo)
			throws Exception {
		return checkListDAO.getActiveChecklistVersionByChecklist(nchecklistCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveChecklistVersionById(int nchecklistVersionCode, UserInfo userInfo)
			throws Exception {
		final String validateChecklist = checkListDAO.validateCheclistVersion(nchecklistVersionCode);
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(validateChecklist)) {
			ChecklistVersion checklistVersion = checkListDAO.getActiveChecklistVersionById(nchecklistVersionCode,
					userInfo);
			if (checklistVersion == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(checklistVersion, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(validateChecklist, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getChecklistVersionQB(int nchecklistVersionCode, UserInfo userInfo) throws Exception {
		return checkListDAO.getChecklistVersionQB(nchecklistVersionCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		return checkListDAO.createChecklistVersion(objChecklistVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		return checkListDAO.updateChecklistVersion(objChecklistVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		return checkListDAO.deleteChecklistVersion(objChecklistVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)
			throws Exception {
		return checkListDAO.approveChecklistVersion(objChecklistVersion, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChecklistVersionQB(List<ChecklistVersionQB> lstChecklistVersionQB,
			UserInfo userInfo) throws Exception {
		return checkListDAO.createChecklistVersionQB(lstChecklistVersionQB, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB, UserInfo userInfo)
			throws Exception {
		return checkListDAO.updateChecklistVersionQB(objChecklistVersionQB, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB, UserInfo userInfo)
			throws Exception {
		return checkListDAO.deleteChecklistVersionQB(objChecklistVersionQB, userInfo);
	}


	@Override
	public ResponseEntity<Object> getVersionQBAddEditData(int nchecklistVersionCode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		final String validateChecklist = checkListDAO.validateCheclistVersion(nchecklistVersionCode);
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(validateChecklist)) {
			@SuppressWarnings("unchecked")
			final List<ChecklistQBCategory> availableCategory = (List<ChecklistQBCategory>) qbCategoryDAO
					.getQBCategory(userInfo.getNmastersitecode()).getBody();
			final List<Map<String, Object>> availableQB = new ArrayList<Map<String, Object>>();
			final Map<String, Object> mapQB = new HashMap<String, Object>();
			if (availableCategory != null && availableCategory.size() > 0) {
				for (ChecklistQBCategory category : availableCategory) {
					mapQB.put(category.getSchecklistqbcategoryname(), checkListDAO
							.getAvailableChecklistQB(category.getNchecklistqbcategorycode(), nchecklistVersionCode));
				}
				availableQB.add(mapQB);
				responseMap.put("qbcategory", availableCategory);
				responseMap.put("checklistqb", mapQB);
			} else {
				responseMap.put("qbcategory", availableCategory);
				responseMap.put("checklistqb", mapQB);
			}
			return new ResponseEntity<Object>(responseMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(validateChecklist, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getAvailableChecklistQB(int nchecklistQBCategoryCode, int nchecklistVersionCode)
			throws Exception {
		return new ResponseEntity<Object>(
				checkListDAO.getAvailableChecklistQB(nchecklistQBCategoryCode, nchecklistVersionCode), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveChecklistVersionQBById(int nchecklistVersionQBCode, UserInfo userInfo)
			throws Exception {
		final ChecklistVersionQB checklistVersionQB = checkListDAO.getActiveChecklistVersionQBById(nchecklistVersionQBCode);
		if (checklistVersionQB == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(checklistVersionQB, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> viewTemplate(int nchecklistVersionCode, int flag, int ntransactionResultCode,
			UserInfo userInfo) throws Exception {
		return checkListDAO.viewTemplate(nchecklistVersionCode, flag, ntransactionResultCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUpdateChecklistVersionTemplate(
			List<ChecklistVersionTemplate> lstChecklistVersionTemplate, UserInfo userInfo) throws Exception {
		return checkListDAO.createUpdateChecklistVersionTemplate(lstChecklistVersionTemplate, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovedChecklist(int nmasterSiteCode) throws Exception {
		return checkListDAO.getApprovedChecklist(nmasterSiteCode);
	}

}
