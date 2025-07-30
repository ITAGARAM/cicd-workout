package com.agaramtech.qualis.misrights.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.AlertHomeRights;
import com.agaramtech.qualis.misrights.model.AlertRights;
import com.agaramtech.qualis.misrights.model.DashBoardHomeRights;
import com.agaramtech.qualis.misrights.model.DashBoardRights;
import com.agaramtech.qualis.misrights.model.ReportRights;

public interface MISRightsService {

	public ResponseEntity<Object> getMISRights(final Integer nuserrolecode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDashBoardTypeByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createDashBoardRights(final List<DashBoardRights> dashBoardRights,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createReportRights(final List<ReportRights> reportRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteReportRights(final ReportRights reportRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteDashBoardRights(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAlertByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createAlertRights(final List<AlertRights> alertRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteAlertRights(final AlertRights alertRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createHomeChart(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createHomeDashBoardRights(final List<DashBoardHomeRights> dashBoardHomeRights,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteDashBoardHomeRights(final DashBoardHomeRights dashBoardHomeRights,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getHomeDashBoardRightsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAlertHomeRigntsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createAlertHomeRights(final List<AlertHomeRights> alertHomeRights,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteAlertHomeRights(final AlertHomeRights alertHomeRights, final UserInfo userInfo)
			throws Exception;

}