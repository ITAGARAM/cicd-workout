package com.agaramtech.qualis.misrights.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.dashboard.model.SeqNoDashBoardManagement;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.AlertHomeRights;
import com.agaramtech.qualis.misrights.model.AlertRights;
import com.agaramtech.qualis.misrights.model.DashBoardHomeRights;
import com.agaramtech.qualis.misrights.model.DashBoardRights;
import com.agaramtech.qualis.misrights.model.ReportRights;
import com.agaramtech.qualis.reports.model.SeqNoReportManagement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class MISRightsDAOImpl implements MISRightsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MISRightsDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getMISRights(final Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (nuserrolecode == null) {
			final String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nsitecode="
					+ userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<UserRole> lstuserrole = jdbcTemplate.query(query, new UserRole());

			if (lstuserrole.isEmpty()) {
				outputMap.put("UserRole", lstuserrole);
				outputMap.put("SelectedMIS", null);
				outputMap.put("DashBoardRights", null);
				outputMap.put("ReportRights", null);
				outputMap.put("AlertRights", null);
				outputMap.put("HomeRights", null);
				outputMap.put("AlertHomeRights", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				// As above query doesnt include 'order by' - to retrieve last user's details
				final List<UserRole> modifiedUserRoleList = mapper.convertValue(lstuserrole,
						new TypeReference<List<UserRole>>() {
						});
				String querys = "Select  dt.sdashboardtypename,dr.ndashboardtypecode,dr.ndashboardtranscode,dr.nuserrolecode,"
						+ "dr.nhomechartview,dr.nsitecode  from dashboardrights dr,dashboardtype dt where "
						+ "dt.ndashboardtypecode=dr.ndashboardtypecode and dt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nsitecode="
						+ userInfo.getNmastersitecode() + " and  dr.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and dr.nuserrolecode="
						+ modifiedUserRoleList.get(modifiedUserRoleList.size() - 1).getNuserrolecode() + ";";

				querys += "Select dt.sreportname,dr.nreportcode,dr.nreportrightscode,dr.nuserrolecode,dr.nsitecode  from"
						+ " reportrights dr,reportmaster dt where dt.nreportcode=dr.nreportcode and" + " dt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dt.nsitecode="
						+ userInfo.getNmastersitecode() + " and dr.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and dr.nuserrolecode="
						+ modifiedUserRoleList.get(modifiedUserRoleList.size() - 1).getNuserrolecode() + ";";

				querys += "Select dt.sscreenheader,dr.nsqlquerycode,dr.nalertrightscode,dr.nuserrolecode,dr.nsitecode  from"
						+ " alertrights dr,sqlquery dt where dt.nquerytypecode="
						+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ""
						+ " and  dt.nsqlquerycode=dr.nsqlquerycode and dt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nsitecode="
						+ userInfo.getNmastersitecode() + " and dr.nsitecode=" + userInfo.getNmastersitecode()
						+ " and dr.nuserrolecode="
						+ modifiedUserRoleList.get(modifiedUserRoleList.size() - 1).getNuserrolecode() + ";";

				querys += "Select coalesce(dp.jsondata->'sdashboardhomepagename'->>'" + userInfo.getSlanguagetypecode()
						+ "'," + " dp.jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename , "
						+ " dr.ndashboardhomerightscode,dr.ndashboardhomeprioritycode,dr.nuserrolecode,"
						+ " dr.nsitecode  from dashboardhomerights dr,dashboardhomepriority dt,dashboardhomepages dp where"
						+ " dt.ndashboardhomepagecode=dp.ndashboardhomepagecode and" + " dp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ "  dt.ndashboardhomeprioritycode=dr.ndashboardhomeprioritycode and dt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nsitecode="
						+ userInfo.getNmastersitecode() + " and dp.nsitecode=" + userInfo.getNmastersitecode()
						+ " and dt.nsitecode=" + userInfo.getNmastersitecode() + " and dr.nuserrolecode="
						+ modifiedUserRoleList.get(modifiedUserRoleList.size() - 1).getNuserrolecode() + ";";

				querys += "Select  dt.sscreenheader,dr.nsqlquerycode,dr.nalerthomerightscode,dr.nuserrolecode,dr.nsitecode"
						+ "  from alerthomerights dr,sqlquery dt where" + " dt.nquerytypecode="
						+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " and"
						+ "  dt.nsqlquerycode=dr.nsqlquerycode and dt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and dr.nsitecode=" + userInfo.getNmastersitecode()
						+ " and dr.nuserrolecode="
						+ modifiedUserRoleList.get(modifiedUserRoleList.size() - 1).getNuserrolecode() + ";";

				LOGGER.info("Get Query -->" + querys);

				final List<?> lstMisRIghts = projectDAOSupport.getMultipleEntitiesResultSetInList(querys, jdbcTemplate,
						DashBoardRights.class, ReportRights.class, AlertRights.class, DashBoardHomeRights.class,
						AlertHomeRights.class);

				final List<DashBoardRights> lstDashBoardRights = (List<DashBoardRights>) lstMisRIghts.get(0);

				final List<ReportRights> lstReportRights = (List<ReportRights>) lstMisRIghts.get(1);

				final List<AlertRights> lstAlertRights = (List<AlertRights>) lstMisRIghts.get(2);

				final List<DashBoardHomeRights> lstDashBoardHomeRights = (List<DashBoardHomeRights>) lstMisRIghts
						.get(3);

				final List<AlertHomeRights> lstAlertHomeRights = (List<AlertHomeRights>) lstMisRIghts.get(4);

				outputMap.put("UserRole", modifiedUserRoleList);
				outputMap.put("SelectedMIS", modifiedUserRoleList.get(modifiedUserRoleList.size() - 1));
				outputMap.put("DashBoardRights", lstDashBoardRights);
				outputMap.put("ReportRights", lstReportRights);
				outputMap.put("AlertRights", lstAlertRights);
				outputMap.put("HomeRights", lstDashBoardHomeRights);
				outputMap.put("AlertHomeRights", lstAlertHomeRights);

			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode="
					+ nuserrolecode;
			final UserRole userrole = (UserRole) jdbcUtilityFunction.queryForObject(query, UserRole.class,
					jdbcTemplate);

			query = "Select dt.sdashboardtypename,dr.ndashboardtypecode,dr.ndashboardtranscode,dr.nhomechartview,dr.nuserrolecode,"
					+ "dr.nsitecode from dashboardrights dr,dashboardtype dt where dt.ndashboardtypecode=dr.ndashboardtypecode"
					+ " and dt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ "dr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " dt.nsitecode=" + userInfo.getNmastersitecode() + " and dr.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and dr.nuserrolecode=" + nuserrolecode + ";";

			query += "Select  dt.sreportname,dr.nreportcode,dr.nreportrightscode,dr.nuserrolecode,dr.nsitecode  from"
					+ " reportrights dr,reportmaster dt where dt.nreportcode=dr.nreportcode and" + " dt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dt.nsitecode="
					+ userInfo.getNmastersitecode() + " and dr.nsitecode=" + userInfo.getNmastersitecode()
					+ "  and dr.nuserrolecode=" + nuserrolecode + ";";

			query += "Select  dt.sscreenheader,dr.nsqlquerycode,dr.nalertrightscode,dr.nuserrolecode,dr.nsitecode  "
					+ "from alertrights dr,sqlquery dt where dt.nquerytypecode="
					+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ""
					+ " and  dt.nsqlquerycode=dr.nsqlquerycode and dt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nsitecode="
					+ userInfo.getNmastersitecode() + "" + "  and dr.nsitecode=" + userInfo.getNmastersitecode()
					+ "  and dr.nuserrolecode=" + nuserrolecode + ";";

			query += "Select  " + " coalesce(dp.jsondata->'sdashboardhomepagename'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " dp.jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename , "
					+ " dr.ndashboardhomerightscode,dr.ndashboardhomeprioritycode,dr.nuserrolecode,"
					+ " dr.nsitecode  from dashboardhomerights dr,dashboardhomepriority dt,dashboardhomepages dp where"
					+ " dp.ndashboardhomepagecode=dt.ndashboardhomepagecode and dp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and dt.ndashboardhomeprioritycode=dr.ndashboardhomeprioritycode and dt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dp.nsitecode="
					+ userInfo.getNmastersitecode() + " and dt.nsitecode=" + userInfo.getNmastersitecode()
					+ " and dr.nuserrolecode=" + nuserrolecode + ";";

			query += "Select  dt.sscreenheader,dr.nsqlquerycode,dr.nalerthomerightscode,dr.nuserrolecode,dr.nsitecode  from"
					+ " alerthomerights dr,sqlquery dt where dt.nquerytypecode="
					+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ""
					+ " and  dt.nsqlquerycode=dr.nsqlquerycode and dt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nsitecode="
					+ userInfo.getNmastersitecode() + "" + "  and dr.nsitecode=" + userInfo.getNmastersitecode()
					+ "  and dr.nuserrolecode=" + nuserrolecode + ";";

			final List<?> lstMisRIghts = projectDAOSupport.getMultipleEntitiesResultSetInList(query, jdbcTemplate,
					DashBoardRights.class, ReportRights.class, AlertRights.class, DashBoardHomeRights.class,
					AlertHomeRights.class);

			final List<DashBoardRights> lstDashBoardRights = (List<DashBoardRights>) lstMisRIghts.get(0);

			final List<ReportRights> lstReportRights = (List<ReportRights>) lstMisRIghts.get(1);

			final List<AlertRights> lstAlertRights = (List<AlertRights>) lstMisRIghts.get(2);

			final List<DashBoardHomeRights> lstDashBoardHomeRights = (List<DashBoardHomeRights>) lstMisRIghts.get(3);

			final List<AlertHomeRights> lstAlertHomeRights = (List<AlertHomeRights>) lstMisRIghts.get(4);

			outputMap.put("SelectedMIS", userrole);
			outputMap.put("DashBoardRights", lstDashBoardRights);
			outputMap.put("ReportRights", lstReportRights);
			outputMap.put("AlertRights", lstAlertRights);
			outputMap.put("HomeRights", lstDashBoardHomeRights);
			outputMap.put("AlertHomeRights", lstAlertHomeRights);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getDashBoardTypeByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String query = "select ndashboardtypecode,sdashboardtypename,4 as nhomechartview,nstatus," + nuserrolecode
				+ " as nuserrolecode,1 as nsorter,nsitecode from dashboardtype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode()
				+ " and ndashboardtypecode not in (select ndashboardtypecode from dashboardrights where nuserrolecode="
				+ nuserrolecode + "  and nsitecode=" + userInfo.getNmastersitecode() + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		final List<DashBoardRights> lstDashBoardRights = jdbcTemplate.query(query, new DashBoardRights());
		return new ResponseEntity<>(lstDashBoardRights, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String query = "select rm.nreportcode,rm.sreportname," + nuserrolecode
				+ " as nuserrolecode,rm.nstatus,rm.nsitecode from reportmaster rm,reportdetails rd "
				+ " where rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rm.nsitecode=" + userInfo.getNmastersitecode() + " and rm.nreportcode=rd.nreportcode "
				+ " and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ " and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rd.nsitecode=" + userInfo.getNmastersitecode() + " and rm.nreporttypecode ="
				+ Enumeration.ReportType.MIS.getReporttype() + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and rm.nreportcode not in (select nreportcode from reportrights where nuserrolecode="
				+ nuserrolecode + "  and nsitecode=" + userInfo.getNmastersitecode() + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		final List<ReportRights> lstReportRights = jdbcTemplate.query(query, new ReportRights());
		return new ResponseEntity<>(lstReportRights, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createDashBoardRights(final List<DashBoardRights> dashBoardRights,
			final UserInfo userInfo) throws Exception {
		final List<Object> saveddashBoardRightsuserroleList = new ArrayList<>();
		List<DashBoardRights> validDashboard = new ArrayList<>();
		if (!dashBoardRights.isEmpty()) {
			final String sdashboardTypeCode = stringUtilityFunction.fnDynamicListToString(dashBoardRights,
					"getNdashboardtypecode");
			final String validateDashBoard = "select ndashboardtypecode from dashboardrights " + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode =  "
					+ dashBoardRights.get(0).getNuserrolecode() + " and nsitecode = " + userInfo.getNmastersitecode()
					+ " and ndashboardtypecode in (" + sdashboardTypeCode + ")";
			final List<DashBoardRights> existingDashboard = jdbcTemplate.query(validateDashBoard,
					new DashBoardRights());
			if (existingDashboard.size() == dashBoardRights.size()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				for (int i = 0; i < dashBoardRights.size(); i++) {
					final int nTypeCode = dashBoardRights.get(i).getNdashboardtypecode();
					final boolean add = !existingDashboard.stream()
							.anyMatch(x -> x.getNdashboardtypecode() == nTypeCode);
					if (add) {
						validDashboard.add(dashBoardRights.get(i));
					}
				}
				if (validDashboard.size() > 0) {
					validDashboard.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					});
				}
				validDashboard = (List<DashBoardRights>) jdbcUtilityFunction.saveBatchOfObjects(validDashboard,
						SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
				saveddashBoardRightsuserroleList.add(validDashboard);
			}
		}
		auditUtilityFunction.fnInsertListAuditAction(saveddashBoardRightsuserroleList, 1, null,
				Arrays.asList("IDS_ADDDASHBOARDRIGHTS"), userInfo);
		return getMISRights(dashBoardRights.get(0).getNuserrolecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> createReportRights(final List<ReportRights> reportRights, final UserInfo userInfo)
			throws Exception {
		final List<Object> savedReportRightsuserroleList = new ArrayList<>();
		List<ReportRights> validReport = new ArrayList<>();
		if (!reportRights.isEmpty()) {
			final String sreportCode = stringUtilityFunction.fnDynamicListToString(reportRights, "getNreportcode");
			final String validate = "select nreportcode from reportrights " + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode =  "
					+ reportRights.get(0).getNuserrolecode() + " and nsitecode = " + userInfo.getNmastersitecode()
					+ " and nreportcode in (" + sreportCode + ")";
			final List<ReportRights> existingReport = jdbcTemplate.query(validate, new ReportRights());
			if (existingReport.size() == reportRights.size()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				for (int i = 0; i < reportRights.size(); i++) {
					final int nreportCode = reportRights.get(i).getNreportcode();
					final boolean add = !existingReport.stream().anyMatch(x -> x.getNreportcode() == nreportCode);
					if (add) {
						validReport.add(reportRights.get(i));
					}
				}
				if (validReport.size() > 0) {
					validReport.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					});
				}
				validReport = (List<ReportRights>) jdbcUtilityFunction.saveBatchOfObjects(validReport,
						SeqNoReportManagement.class, jdbcTemplate, "nsequenceno");
				savedReportRightsuserroleList.add(validReport);
			}

		}
		auditUtilityFunction.fnInsertListAuditAction(savedReportRightsuserroleList, 1, null,
				Arrays.asList("IDS_ADDREPORTRIGHTS"), userInfo);
		return getMISRights(reportRights.get(0).getNuserrolecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteReportRights(final ReportRights reportRights, final UserInfo userInfo)
			throws Exception {

		final List<Object> savedReportRightsuserroleList = new ArrayList<>();
		String query = "select * from reportrights where " + " nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nreportrightscode=" + reportRights.getNreportrightscode();
		final ReportRights dashboardrights = (ReportRights) jdbcUtilityFunction.queryForObject(query,
				ReportRights.class, jdbcTemplate);

		if (dashboardrights != null) {

			query = "delete from   reportrights where " + " nreportrightscode=" + reportRights.getNreportrightscode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(query);

			savedReportRightsuserroleList.add(dashboardrights);
			auditUtilityFunction.fnInsertAuditAction(savedReportRightsuserroleList, 1, null,
					Arrays.asList("IDS_DELETEREPORTRIGHTS"), userInfo);
			return getMISRights(reportRights.getNuserrolecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteDashBoardRights(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception {
		final List<Object> savedDashRightsuserroleList = new ArrayList<>();
		String query = "select * from dashboardrights where " + " nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ndashboardtranscode="
				+ dashBoardRights.getNdashboardtranscode();
		final DashBoardRights dashboardrights = (DashBoardRights) jdbcUtilityFunction.queryForObject(query,
				DashBoardRights.class, jdbcTemplate);

		if (dashboardrights != null) {
			query = "delete from   dashboardrights where " + " ndashboardtranscode="
					+ dashBoardRights.getNdashboardtranscode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(query);

			savedDashRightsuserroleList.add(dashboardrights);
			auditUtilityFunction.fnInsertAuditAction(savedDashRightsuserroleList, 1, null,
					Arrays.asList("IDS_DELETEDASHBOARDRIGHTS"), userInfo);
			return getMISRights(dashBoardRights.getNuserrolecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getAlertByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String query = "select nsqlquerycode,sscreenheader," + nuserrolecode
				+ " as nuserrolecode,nstatus,nsitecode from sqlquery where nquerytypecode="
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode()
				+ " and nsqlquerycode not in (select nsqlquerycode from alertrights where nuserrolecode="
				+ nuserrolecode + "  and nsitecode=" + userInfo.getNmastersitecode() + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		final List<AlertRights> lstAlertRights = jdbcTemplate.query(query, new AlertRights());
		return new ResponseEntity<>(lstAlertRights, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createAlertRights(final List<AlertRights> alertRights, final UserInfo userInfo)
			throws Exception {

		final List<Object> savedAlertRightsuserroleList = new ArrayList<>();
		List<AlertRights> validAlert = new ArrayList<>();
		if (!alertRights.isEmpty()) {
			final String ssqlQueryCode = stringUtilityFunction.fnDynamicListToString(alertRights, "getNsqlquerycode");
			final String validate = "select nsqlquerycode from alertrights " + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode =  "
					+ alertRights.get(0).getNuserrolecode() + " and nsitecode = " + userInfo.getNmastersitecode()
					+ " and nsqlquerycode in (" + ssqlQueryCode + ")";
			final List<AlertRights> existingAlerts = jdbcTemplate.query(validate, new AlertRights());
			if (existingAlerts.size() == alertRights.size()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				for (int i = 0; i < alertRights.size(); i++) {
					final int nqueryCode = alertRights.get(i).getNsqlquerycode();
					final boolean add = !existingAlerts.stream().anyMatch(x -> x.getNsqlquerycode() == nqueryCode);
					if (add) {
						validAlert.add(alertRights.get(i));
					}
				}
				if (validAlert.size() > 0) {
					validAlert.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					});
				}
				validAlert = (List<AlertRights>) jdbcUtilityFunction.saveBatchOfObjects(validAlert,
						SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
				savedAlertRightsuserroleList.add(validAlert);
			}
		}
		auditUtilityFunction.fnInsertListAuditAction(savedAlertRightsuserroleList, 1, null,
				Arrays.asList("IDS_ADDALERTRIGHTS"), userInfo);
		return getMISRights(alertRights.get(0).getNuserrolecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteAlertRights(final AlertRights alertRights, final UserInfo userInfo)
			throws Exception {

		final List<Object> savedAlertRightsuserroleList = new ArrayList<>();
		String query = "select * from alertrights where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
				+ userInfo.getNmastersitecode() + "  and nalertrightscode=" + alertRights.getNalertrightscode();
		final AlertRights alertrights = (AlertRights) jdbcUtilityFunction.queryForObject(query, AlertRights.class,
				jdbcTemplate);
		if (alertrights != null) {

			query = "delete from alertrights where nalertrightscode=" + alertRights.getNalertrightscode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(query);

			savedAlertRightsuserroleList.add(alertrights);
			auditUtilityFunction.fnInsertAuditAction(savedAlertRightsuserroleList, 1, null,
					Arrays.asList("IDS_DELETEALERTRIGHTS"), userInfo);
			return getMISRights(alertRights.getNuserrolecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> createHomeChart(final DashBoardRights dashBoardRights, final UserInfo userInfo)
			throws Exception {
		final List<Object> savedReportRightsuserroleList = new ArrayList<>();
		final List<Object> beforeReportRightsuserroleList = new ArrayList<>();
		String query = "select * from dashboardrights where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ndashboardtranscode=" + dashBoardRights.getNdashboardtranscode();
		final DashBoardRights dashboardRights = (DashBoardRights) jdbcUtilityFunction.queryForObject(query,
				DashBoardRights.class, jdbcTemplate);
		if (dashboardRights != null) {
			// query = "update dashboardrights set nhomechartview=" +
			// dashBoardRights.getNhomechartview()
			// + " where nstatus=" +
			// Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			// + " and ndashboardtranscode=" + dashBoardRights.getNdashboardtranscode();
			// jdbcTemplate.execute(query);
			query = "select * from dashboardrights where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndashboardtranscode="
					+ dashBoardRights.getNdashboardtranscode();
			final DashBoardRights dashboardRightsafter = (DashBoardRights) jdbcUtilityFunction.queryForObject(query,
					DashBoardRights.class, jdbcTemplate);
			savedReportRightsuserroleList.add(dashboardRightsafter);
			beforeReportRightsuserroleList.add(dashboardRights);
			auditUtilityFunction.fnInsertAuditAction(savedReportRightsuserroleList, 2, beforeReportRightsuserroleList,
					Arrays.asList("IDS_ADDHOMECHART"), userInfo);

			return getMISRights(dashBoardRights.getNuserrolecode(), userInfo);
		}

		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
						userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
	}

	@Override
	public ResponseEntity<Object> createHomeDashBoardRights(final List<DashBoardHomeRights> dashBoardHomeRights,
			final UserInfo userInfo) throws Exception {

		final List<Object> saveddashBoardHomeRightsuserroleList = new ArrayList<>();
		List<DashBoardHomeRights> validdashBoardHomeRights = new ArrayList<>();
		if (!dashBoardHomeRights.isEmpty()) {
			final String sdashboardHomePriorityCode = stringUtilityFunction.fnDynamicListToString(dashBoardHomeRights,
					"getNdashboardhomeprioritycode");
			final String validate = "select ndashboardhomeprioritycode from dashboardhomerights " + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode =  "
					+ dashBoardHomeRights.get(0).getNuserrolecode() + " and nsitecode = "
					+ userInfo.getNmastersitecode() + " and ndashboardhomeprioritycode in ("
					+ sdashboardHomePriorityCode + ")";
			final List<DashBoardHomeRights> existingAlerts = jdbcTemplate.query(validate, new DashBoardHomeRights());
			if (existingAlerts.size() == dashBoardHomeRights.size()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				for (int i = 0; i < dashBoardHomeRights.size(); i++) {
					final int nqueryCode = dashBoardHomeRights.get(i).getNdashboardhomeprioritycode();
					final boolean add = !existingAlerts.stream()
							.anyMatch(x -> x.getNdashboardhomeprioritycode() == nqueryCode);
					if (add) {
						validdashBoardHomeRights.add(dashBoardHomeRights.get(i));
					}
				}
				if (validdashBoardHomeRights.size() > 0) {
					validdashBoardHomeRights.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					});
				}
				validdashBoardHomeRights = (List<DashBoardHomeRights>) jdbcUtilityFunction.saveBatchOfObjects(
						validdashBoardHomeRights, SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
				saveddashBoardHomeRightsuserroleList.add(validdashBoardHomeRights);
			}

		}
		auditUtilityFunction.fnInsertListAuditAction(saveddashBoardHomeRightsuserroleList, 1, null,
				Arrays.asList("IDS_ADDDASHBOARDHOMERIGHTS"), userInfo);
		return getMISRights(dashBoardHomeRights.get(0).getNuserrolecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteDashBoardHomeRights(final DashBoardHomeRights dashBoardHomeRights,
			final UserInfo userInfo) throws Exception {

		final List<Object> savedDashHomeRightsuserroleList = new ArrayList<>();
		String query = "select * from dashboardhomerights where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ndashboardhomerightscode="
				+ dashBoardHomeRights.getNdashboardhomerightscode();
		final DashBoardHomeRights dashboardrights = (DashBoardHomeRights) jdbcUtilityFunction.queryForObject(query,
				DashBoardHomeRights.class, jdbcTemplate);

		if (dashboardrights != null) {

			query = "delete from dashboardhomerights where ndashboardhomerightscode="
					+ dashBoardHomeRights.getNdashboardhomerightscode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(query);

			savedDashHomeRightsuserroleList.add(dashboardrights);
			auditUtilityFunction.fnInsertAuditAction(savedDashHomeRightsuserroleList, 1, null,
					Arrays.asList("IDS_DELETEDASHBOARDHOMERIGHTS"), userInfo);
			return getMISRights(dashBoardHomeRights.getNuserrolecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getHomeDashBoardRightsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String query = "select dbp.ndashboardhomepagecode,"
				+ " coalesce(dbp.jsondata->'sdashboardhomepagename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " dbp.jsondata->'sdashboardhomepagename'->>'en-US')  as sdashboardhomepagename , "
				+ " dp.ndashboardhomepagecode,dp.ndashboardhomeprioritycode,dp.ndashboardhometemplatecode,dp.nuserrolecode,dp.nsitecode,dp.nstatus,"
				+ userInfo.getNmastersitecode() + " as nsitecode from dashboardhomepriority dp,dashboardhomepages dbp "
				+ " where dbp.ndashboardhomepagecode=dp.ndashboardhomepagecode" + " and dp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dp.nsitecode="
				+ userInfo.getNmastersitecode() + " and dbp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dbp.nsitecode="
				+ userInfo.getNmastersitecode() + " and nuserrolecode=" + nuserrolecode
				+ " and dp.ndashboardhomeprioritycode not in  (select ndashboardhomeprioritycode from dashboardhomerights where nuserrolecode="
				+ nuserrolecode + "" + " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  )";

		List<DashBoardHomeRights> lstDashBoardHomeRights = jdbcTemplate.query(query, new DashBoardHomeRights());

		lstDashBoardHomeRights = (List<DashBoardHomeRights>) commonFunction.getMultilingualMessageList(
				lstDashBoardHomeRights, Arrays.asList("sdashboardhomepagename"), userInfo.getSlanguagefilename());
		return new ResponseEntity<>(lstDashBoardHomeRights, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAlertHomeRigntsByUserRole(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String query = "select nsqlquerycode,sscreenheader," + nuserrolecode
				+ " as nuserrolecode,nstatus,nsitecode from sqlquery " + "where nquerytypecode="
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " and " + " nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nsitecode="
				+ userInfo.getNmastersitecode() + " and nsqlquerycode not in (select nsqlquerycode from alerthomerights"
				+ " where nuserrolecode=" + nuserrolecode + "  and nsitecode=" + userInfo.getNmastersitecode() + ""
				+ "  and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		final List<AlertHomeRights> lstAlertHomeRights = jdbcTemplate.query(query, new AlertHomeRights());
		return new ResponseEntity<>(lstAlertHomeRights, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createAlertHomeRights(final List<AlertHomeRights> alertHomeRights,
			final UserInfo userInfo) throws Exception {

		final List<Object> savedAlertRightsuserroleList = new ArrayList<>();
		List<AlertHomeRights> validAlert = new ArrayList<>();
		if (!alertHomeRights.isEmpty()) {
			final String ssqlQueryCode = stringUtilityFunction.fnDynamicListToString(alertHomeRights,
					"getNsqlquerycode");
			final String validate = "select nsqlquerycode from alerthomerights " + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode =  "
					+ alertHomeRights.get(0).getNuserrolecode() + " and nsitecode = " + userInfo.getNmastersitecode()
					+ " and nsqlquerycode in (" + ssqlQueryCode + ")";
			final List<AlertHomeRights> existingAlerts = jdbcTemplate.query(validate, new AlertHomeRights());
			if (existingAlerts.size() == alertHomeRights.size()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				for (int i = 0; i < alertHomeRights.size(); i++) {
					final int nqueryCode = alertHomeRights.get(i).getNsqlquerycode();
					final boolean add = !existingAlerts.stream().anyMatch(x -> x.getNsqlquerycode() == nqueryCode);
					if (add) {
						validAlert.add(alertHomeRights.get(i));
					}
				}
				if (validAlert.size() > 0) {
					validAlert.stream().forEach(x -> {
						try {
							x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					});
				}
				validAlert = (List<AlertHomeRights>) jdbcUtilityFunction.saveBatchOfObjects(validAlert,
						SeqNoDashBoardManagement.class, jdbcTemplate, "nsequenceno");
				savedAlertRightsuserroleList.add(validAlert);
			}
		}
		auditUtilityFunction.fnInsertListAuditAction(savedAlertRightsuserroleList, 1, null,
				Arrays.asList("IDS_ADDALERTHOMERIGHTS"), userInfo);
		return getMISRights(alertHomeRights.get(0).getNuserrolecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteAlertHomeRights(final AlertHomeRights alertHomeRights, final UserInfo userInfo)
			throws Exception {
		final List<Object> savedAlertRightsuserroleList = new ArrayList<>();
		String query = "select * from alerthomerights where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
				+ userInfo.getNmastersitecode() + "  and nalerthomerightscode="
				+ alertHomeRights.getNalerthomerightscode();
		final AlertHomeRights alerthomerights = (AlertHomeRights) jdbcUtilityFunction.queryForObject(query,
				AlertHomeRights.class, jdbcTemplate);
		if (alerthomerights != null) {

			query = "delete from alerthomerights where nalerthomerightscode="
					+ alerthomerights.getNalerthomerightscode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(query);

			savedAlertRightsuserroleList.add(alerthomerights);
			auditUtilityFunction.fnInsertAuditAction(savedAlertRightsuserroleList, 1, null,
					Arrays.asList("IDS_DELETEALERTHOMERIGHTS"), userInfo);
			return getMISRights(alerthomerights.getNuserrolecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
}