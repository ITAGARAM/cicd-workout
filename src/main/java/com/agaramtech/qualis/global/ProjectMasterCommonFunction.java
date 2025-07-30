package com.agaramtech.qualis.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.agaramtech.qualis.project.model.ProjectMaster;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ProjectMasterCommonFunction {
	
	protected final JdbcTemplate jdbcTemplate;
	
	public ResponseEntity<Object> getApprovedProjectByProjectType(int projectTypeCode, UserInfo objUserInfo) {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "select pm.nprojecttypecode,pm.nprojectmastercode,pm.sprojecttitle,pm.sprojectcode"
				+ " from projectmaster pm,projectmasterhistory pmh where pmh.nprojectmastercode=pm.nprojectmastercode and "
				+ " pm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " pmh.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and pmh.nprojectmasterhistorycode = any "
				+ "(select max(nprojectmasterhistorycode) from   projectmasterhistory ph 	where ph.nprojectmastercode = pm.nprojectmastercode "
				+ " group  by ph.nprojectmastercode)  and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pmh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nprojecttypecode="
				+ projectTypeCode + " union " 
				+ " select pm.nprojecttypecode, pm.nprojectmastercode,pm.sprojecttitle,pm.sprojectcode from  projecttype pt, projectmaster pm,testgroupspecification tgs"
				+ "	where  pt.nstatus = pm.nstatus and pm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode = pm.nprojecttypecode and pm.nprojectmastercode=tgs.nprojectmastercode "
				+ " and  pt.nprojecttypecode>0 and  tgs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nprojecttypecode = "
				+ projectTypeCode;

		final List<ProjectMaster> listProjectMaster = jdbcTemplate.query(strQuery, new ProjectMaster());
		outputMap.put("ProjectMasterList", listProjectMaster);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}
