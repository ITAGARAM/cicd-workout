package com.agaramtech.qualis.checklist.service.checklistqb;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.global.UserInfo;

public interface ChecklistQBDAO {
	public ResponseEntity<Object> getChecklistQB(UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> createChecklistQB(ChecklistQB objChecklistQB,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateChecklistQB(ChecklistQB objChecklistQB,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> deleteChecklistQB(ChecklistQB objChecklistQB,UserInfo userInfo)throws Exception;
	public ChecklistQB getActiveChecklistQBById(final int nchecklistQBCode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getAddEditData(UserInfo userInfo)throws Exception;
}
