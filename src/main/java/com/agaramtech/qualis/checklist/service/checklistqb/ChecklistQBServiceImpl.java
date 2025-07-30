package com.agaramtech.qualis.checklist.service.checklistqb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ChecklistQBServiceImpl implements ChecklistQBService {

	private final ChecklistQBDAO checkListqbDAO;

	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public ChecklistQBServiceImpl(CommonFunction commonFunction, ChecklistQBDAO checkListqbDAO) {
		super();
		this.commonFunction = commonFunction;
		this.checkListqbDAO = checkListqbDAO;
	}

	@Override
	public ResponseEntity<Object> getChecklistQB(UserInfo userInfo) throws Exception {
		return checkListqbDAO.getChecklistQB(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {
		return checkListqbDAO.createChecklistQB(objChecklistQB, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {
		return checkListqbDAO.updateChecklistQB(objChecklistQB, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChecklistQB(ChecklistQB objChecklistQB, UserInfo userInfo) throws Exception {
		return checkListqbDAO.deleteChecklistQB(objChecklistQB, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveChecklistQBById(int nchecklistQBCode, UserInfo userInfo) throws Exception {
		ChecklistQB checklistQB = checkListqbDAO.getActiveChecklistQBById(nchecklistQBCode, userInfo);
		if (checklistQB == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("scomponentname");
			lstcolumns.add("smandatory");
			return new ResponseEntity<>(commonFunction
					.getMultilingualMessageList(Arrays.asList(checklistQB), lstcolumns, userInfo.getSlanguagefilename())
					.get(0), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getAddEditData(UserInfo userInfo) throws Exception {
		return checkListqbDAO.getAddEditData(userInfo);
	}

}
