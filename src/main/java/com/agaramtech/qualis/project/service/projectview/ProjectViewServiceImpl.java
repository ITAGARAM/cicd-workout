package com.agaramtech.qualis.project.service.projectview;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ProjectViewServiceImpl implements ProjectViewService {

	private final ProjectViewDAO projectviewDAO;

	@Override
	public ResponseEntity<Object> getProjectView(String fromDate, String toDate, String currentUIDate,
			UserInfo objUserInfo) throws Exception {
		return projectviewDAO.getProjectView(fromDate, toDate, currentUIDate, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getProjectViewBySampleType(String fromDate, String toDate, String currentUIDate,
			Integer nsampletypecode, Integer nprojecttypecode, UserInfo userInfo) throws Exception {
		return projectviewDAO.getProjectViewBySampleType(fromDate, toDate, currentUIDate, nsampletypecode,
				nprojecttypecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveProjectViewById(Integer nsampletypecode, int nprojectmastercode,
			UserInfo userInfo) throws Exception {
		return projectviewDAO.getActiveProjectViewById(nsampletypecode, nprojectmastercode, userInfo);
	}
}
