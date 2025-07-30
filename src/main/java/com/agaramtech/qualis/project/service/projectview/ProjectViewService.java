package com.agaramtech.qualis.project.service.projectview;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ProjectViewService {

	public ResponseEntity<Object> getProjectView(String fromDate, String toDate, String currentUIDate,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getProjectViewBySampleType(String fromDate, String toDate, String currentUIDate,
			Integer nsampletypecode, Integer nprojecttypecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveProjectViewById(Integer nsampletypecode, int nprojectmastercode,
			UserInfo userInfo) throws Exception;

}
