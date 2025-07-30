package com.agaramtech.qualis.misrights.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.AlertHomeRights;
import com.agaramtech.qualis.misrights.model.AlertRights;
import com.agaramtech.qualis.misrights.model.DashBoardHomeRights;
import com.agaramtech.qualis.misrights.model.DashBoardRights;
import com.agaramtech.qualis.misrights.model.ReportRights;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class MISRightsServiceImpl implements MISRightsService {

	private final MISRightsDAO misrightsDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param MISRightsDAO misrightsDAO
	 */
	public MISRightsServiceImpl(final MISRightsDAO misrightsDAO) {
		super();
		this.misrightsDAO = misrightsDAO;
	}

	@Override
	public ResponseEntity<Object> getMISRights(final Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		return misrightsDAO.getMISRights(nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDashBoardTypeByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.getDashBoardTypeByUserRole(nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReportByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.getReportByUserRole(nuserrolecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDashBoardRights(final List<DashBoardRights> dashBoardRights,
			final UserInfo userInfo) throws Exception {
		return misrightsDAO.createDashBoardRights(dashBoardRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createReportRights(final List<ReportRights> reportRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.createReportRights(reportRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportRights(final ReportRights reportRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.deleteReportRights(reportRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDashBoardRights(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.deleteDashBoardRights(dashBoardRights, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAlertByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.getAlertByUserRole(nuserrolecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createAlertRights(final List<AlertRights> alertRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.createAlertRights(alertRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteAlertRights(final AlertRights alertRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.deleteAlertRights(alertRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createHomeChart(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.createHomeChart(dashBoardRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createHomeDashBoardRights(final List<DashBoardHomeRights> dashBoardHomeRights,
			final UserInfo userInfo) throws Exception {
		return misrightsDAO.createHomeDashBoardRights(dashBoardHomeRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDashBoardHomeRights(final DashBoardHomeRights dashBoardHomeRights,
			final UserInfo userInfo) throws Exception {
		return misrightsDAO.deleteDashBoardHomeRights(dashBoardHomeRights, userInfo);
	}

	@Override
	public ResponseEntity<Object> getHomeDashBoardRightsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.getHomeDashBoardRightsByUserRole(nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAlertHomeRigntsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.getAlertHomeRigntsByUserRole(nuserrolecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createAlertHomeRights(final List<AlertHomeRights> alertHomeRights,
			final UserInfo userInfo) throws Exception {
		return misrightsDAO.createAlertHomeRights(alertHomeRights, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteAlertHomeRights(final AlertHomeRights alertHomeRights, final UserInfo userInfo)
			throws Exception {
		return misrightsDAO.deleteAlertHomeRights(alertHomeRights, userInfo);
	}
}