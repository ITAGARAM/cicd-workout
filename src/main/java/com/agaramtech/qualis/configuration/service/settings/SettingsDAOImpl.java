package com.agaramtech.qualis.configuration.service.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SettingsDAOImpl implements SettingsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getSettings(UserInfo userInfo) throws Exception {
		String strQuery = "select s.nsettingcode,s.ssettingname,s.ssettingvalue,s.nstatus from settings s"
				+ " where s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsettingcode>0 and nisvisible = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ "" + " order by s.nsettingcode asc";
		LOGGER.info("getSettings:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Settings()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table settings " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<Object> savedSettingsList = new ArrayList<>();
		final Settings settingsListByName = getSettingsByName(objSettings.getSsettingname(), userInfo);
		if (settingsListByName == null) {
			final String sequenceNoQuery = "select nsequenceno from seqnoconfigurationmaster where stablename ='settings'";
			int nsequenceNo = (int) jdbcUtilityFunction.queryForObject(sequenceNoQuery, Integer.class, jdbcTemplate);
			nsequenceNo++;
			String strInsert = "insert into settings(nsettingcode,ssettingname,ssettingvalue,dmodifieddate,nisvisible,nstatus)"
					+ " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objSettings.getSsettingname()) + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(objSettings.getSsettingvalue()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(strInsert);
			jdbcTemplate.execute("update seqnoconfigurationmaster set nsequenceno = " + nsequenceNo
					+ " where stablename='settings'");
			objSettings.setNsettingcode((short) nsequenceNo);
			savedSettingsList.add(objSettings);
			auditUtilityFunction.fnInsertAuditAction(savedSettingsList, 1, null, Arrays.asList("IDS_ADDSETTINGS"),
					userInfo);
			return getSettings(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private Settings getSettingsByName(final String ssettingname, final UserInfo userInfo) throws Exception {
		final String strQuery = "select nsettingcode from settings where ssettingname = N'"
				+ stringUtilityFunction.replaceQuote(ssettingname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		return (Settings) jdbcUtilityFunction.queryForObject(strQuery, Settings.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getActiveSettingsById(final int nsettingcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select * from settings s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsettingcode = " + nsettingcode;
		final Settings settings = (Settings) jdbcUtilityFunction.queryForObject(strQuery, Settings.class, jdbcTemplate);
		if (settings == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(settings, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> updateSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		final ResponseEntity<Object> settingsResponse = getActiveSettingsById(objSettings.getNsettingcode(), userInfo);
		if (settingsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return settingsResponse;
		} else {
			final String queryString = "select nsettingcode from settings where ssettingname = N'"
					+ stringUtilityFunction.replaceQuote(objSettings.getSsettingname()) + "' and nsettingcode <> "
					+ objSettings.getNsettingcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Settings settings = (Settings) jdbcUtilityFunction.queryForObject(queryString, Settings.class,
					jdbcTemplate);
			if (settings == null) {
				final String updateQueryString = "update settings set ssettingname=N'"
						+ stringUtilityFunction.replaceQuote(objSettings.getSsettingname()) + "', ssettingvalue=N'"
						+ stringUtilityFunction.replaceQuote(objSettings.getSsettingvalue()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsettingcode="
						+ objSettings.getNsettingcode();
				jdbcTemplate.execute(updateQueryString);
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objSettings);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(settingsResponse.getBody());
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
						Arrays.asList("IDS_EDITSETTINGS"), userInfo);
				return getSettings(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteSettings(Settings objSettings, UserInfo userInfo) throws Exception {
		final ResponseEntity<Object> settingsResponse = getActiveSettingsById(objSettings.getNsettingcode(), userInfo);
		if (settingsResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return settingsResponse;
		} else {
			final List<Object> deletedSettingsList = new ArrayList<>();
			final String updateQueryString = "update settings set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsettingcode="
					+ objSettings.getNsettingcode();
			jdbcTemplate.execute(updateQueryString);
			objSettings.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedSettingsList.add(objSettings);
			auditUtilityFunction.fnInsertAuditAction(deletedSettingsList, 1, null, Arrays.asList("IDS_DELETESETTINGS"),
					userInfo);
		}
		return getSettings(userInfo);
	}
}
