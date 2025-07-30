package com.agaramtech.qualis.configuration.service.reportsettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository

public class ReportSettingsDAOImpl implements ReportSettingsDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportSettingsDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getReportSettings(final UserInfo userInfo) throws Exception {
		final String strQuery = "select rs.nreportsettingcode, rs.ssettingname, rs.ssettingvalue,rs.nstatus"
				+ " from reportsettings rs" + " where rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rs.nreportsettingcode>0 and nisvisible = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " order by rs.nreportsettingcode asc";
		LOGGER.info("getSampleAppearance -->");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ReportSettings()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createReportSettings(final ReportSettings objReportSettings,final  UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table reportsettings " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<Object> savedReportSettingsList = new ArrayList<>();
		final ReportSettings reportSettingsListByName = getReportSettingsByName(
				objReportSettings.getSsettingname());
		if (reportSettingsListByName == null) {
			int nSeqNo = (int) jdbcUtilityFunction.queryForObject(
					"select nsequenceno from seqnoconfigurationmaster where stablename='reportsettings'",
					Integer.class,jdbcTemplate);
			nSeqNo++;
			final String strInsert = "insert into reportsettings(nreportsettingcode,ssettingname,ssettingvalue,dmodifieddate,nisvisible,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objReportSettings.getSsettingname()) + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(objReportSettings.getSsettingvalue()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(strInsert);
			jdbcTemplate.execute("update seqnoconfigurationmaster set nsequenceno = " + nSeqNo
					+ " where stablename='reportsettings'");
			objReportSettings.setNreportsettingcode((short) nSeqNo);
			savedReportSettingsList.add(objReportSettings);
			auditUtilityFunction.fnInsertAuditAction(savedReportSettingsList, 1, null,
					Arrays.asList("IDS_ADDREPORTSETTINGS"), userInfo);
			return getReportSettings(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private ReportSettings getReportSettingsByName(final String ssettingname) throws Exception {
		final String strQuery = "select nreportsettingcode from reportsettings where ssettingname = N'"
				+ stringUtilityFunction.replaceQuote(ssettingname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (ReportSettings) jdbcUtilityFunction.queryForObject(strQuery, ReportSettings.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getActiveReportSettingsById(final int nreportsettingcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select * from reportsettings rs where rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rs.nreportsettingcode = "
				+ nreportsettingcode;
		final ReportSettings reportsettings = (ReportSettings) jdbcUtilityFunction.queryForObject(strQuery,
				ReportSettings.class, jdbcTemplate);
		if (reportsettings == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(reportsettings, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> updateReportSettings(final ReportSettings objReportSettings,final  UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> settingsResponse = getActiveReportSettingsById(
				objReportSettings.getNreportsettingcode(), userInfo);
		if (settingsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return settingsResponse;
		} else {
			final String queryString = "select nreportsettingcode from reportsettings where ssettingname = N'"
					+ stringUtilityFunction.replaceQuote(objReportSettings.getSsettingname())
					+ "' and nreportsettingcode <> " + objReportSettings.getNreportsettingcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ReportSettings reportSettings = (ReportSettings) jdbcUtilityFunction.queryForObject(queryString,
					ReportSettings.class, jdbcTemplate);
			if (reportSettings == null) {
				final String updateQueryString = "update reportsettings set ssettingname=N'"
						+ stringUtilityFunction.replaceQuote(objReportSettings.getSsettingname())
						+ "', ssettingvalue=N'"
						+ stringUtilityFunction.replaceQuote(objReportSettings.getSsettingvalue()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreportsettingcode="
						+ objReportSettings.getNreportsettingcode();
				jdbcTemplate.execute(updateQueryString);
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objReportSettings);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(settingsResponse.getBody());
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
						Arrays.asList("IDS_EDITREPORTSETTINGS"), userInfo);
				return getReportSettings(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteReportSettings(final ReportSettings objReportSettings, final UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> settingsResponse = getActiveReportSettingsById(
				objReportSettings.getNreportsettingcode(), userInfo);
		if (settingsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return settingsResponse;
		} else {
			final List<Object> deletedReportSettingsList = new ArrayList<>();
			final String updateQueryString = "update reportsettings set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nreportsettingcode="
					+ objReportSettings.getNreportsettingcode();
			jdbcTemplate.execute(updateQueryString);
			objReportSettings.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedReportSettingsList.add(objReportSettings);
			auditUtilityFunction.fnInsertAuditAction(deletedReportSettingsList, 1, null,
					Arrays.asList("IDS_DELETEREPORTSETTINGS"), userInfo);
		}
		return getReportSettings(userInfo);
	}
}
