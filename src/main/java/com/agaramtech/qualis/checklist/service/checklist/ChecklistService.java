package com.agaramtech.qualis.checklist.service.checklist;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.checklist.model.Checklist;
import com.agaramtech.qualis.checklist.model.ChecklistVersion;
import com.agaramtech.qualis.checklist.model.ChecklistVersionQB;
import com.agaramtech.qualis.checklist.model.ChecklistVersionTemplate;
import com.agaramtech.qualis.global.UserInfo;

public interface ChecklistService {
	public ResponseEntity<Object> getChecklist(UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createChecklist(Checklist objChecklist, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateChecklist(Checklist objChecklist, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> deleteChecklist(Checklist objChecklist, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> getActiveChecklistById(int nchecklistCode, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveChecklistVersionByChecklist(int nchecklistCode,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getChecklistVersionQB(int nchecklistVersionCode,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveChecklistVersionById(int nchecklistVersionCode,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createChecklistVersion(ChecklistVersion objChecklistVersion,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateChecklistVersion(ChecklistVersion objChecklistVersion, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> deleteChecklistVersion(ChecklistVersion objChecklistVersion,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> approveChecklistVersion(ChecklistVersion objChecklistVersion,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createChecklistVersionQB(List<ChecklistVersionQB> lstChecklistVersionQB,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteChecklistVersionQB(ChecklistVersionQB objChecklistVersionQB,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getVersionQBAddEditData(int nchecklistVersionCode,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> getAvailableChecklistQB(int nchecklistQBCategoryCode,int nchecklistVersionCode)throws Exception;
	public ResponseEntity<Object> getActiveChecklistVersionQBById(int nchecklistVersionQBCode, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> viewTemplate(int nchecklistVersionCode,int flag,int ntransactionResultCode,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createUpdateChecklistVersionTemplate(List<ChecklistVersionTemplate> lstChecklistVersionTemplate,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getApprovedChecklist(int nmasterSiteCode) throws Exception;
	

}
