package com.agaramtech.qualis.configuration.service.adssettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.login.service.LoginDAO;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ADSSettingsDAOImpl implements ADSSettingsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final CommonFunction commonFunctionObject;
	private final LoginDAO loginDAOImpl;
	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getADSSettings(UserInfo userInfo) throws Exception {
		final String strADSSettings = "select ads.*, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') sdisplaystatus from adssettings ads, transactionstatus ts  where "
				+ " ads.ndefaultstatus=ts.ntranscode and ads.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ads.nsitecode="
				+ userInfo.getNmastersitecode() + " order by 1 desc";
		LOGGER.info(strADSSettings);
		return new ResponseEntity<>((List<ADSSettings>) jdbcTemplate.query(strADSSettings, new ADSSettings()),
				HttpStatus.OK);
	}

	@Override
	public ADSSettings getActiveADSSettingsById(int adsSettingsCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select ads.*, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from adssettings ads, transactionstatus ts "
				+ " where ads.nadssettingscode=" + adsSettingsCode
				+ " and ads.ndefaultstatus=ts.ntranscode and ads.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		ADSSettings adsSettings = (ADSSettings) jdbcUtilityFunction.queryForObject(strQuery, ADSSettings.class,
				jdbcTemplate);
		adsSettings.setSpassword("");
		return adsSettings;
	}

	@Override
	public ResponseEntity<Object> createADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table adssettings " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ADSSettings adsSettingsListByServerAndDomainName = getADSSettingsByServerAndDomainName(
				adsSettings.getSservername().toString(), adsSettings.getSdomainname().toString(),
				userInfo.getNmastersitecode(), "");
		if (adsSettingsListByServerAndDomainName == null) {
			final Map<String, Object> adsMapValue = formLDAPLinkAndCheckADSConnection(adsSettings, userInfo);
			final String rtnValue = adsMapValue.get("rtnValue").toString();
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == rtnValue) {
				ADSSettings defaultADSSettings = getADSSettingsByStatus(adsSettings.getNsitecode(), "");
				if (adsSettings.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (defaultADSSettings != null) {
						final ADSSettings oldADSSettings = SerializationUtils.clone(defaultADSSettings);
						defaultADSSettings
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = "update adssettings set ndefaultstatus = "
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nadssettingscode="
								+ defaultADSSettings.getNadssettingscode();
						jdbcTemplate.execute(updateQueryString);
						auditUtilityFunction.fnInsertAuditAction(Arrays.asList(defaultADSSettings), 2,
								Arrays.asList(oldADSSettings), Arrays.asList("IDS_EDITADSSETTINGS"), userInfo);
					}
				} else {
					if (defaultADSSettings == null) {
						adsSettings.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
					}
				}
				final String sequencequery = "select nsequenceno from Seqnoconfigurationmaster where stablename ='adssettings'"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
				nsequenceno++;
				String strAdsSettings = "insert into adssettings (nadssettingscode, sservername, sdomainname, sorganisationunit, "
						+ "sgroupname, sldaplink, suserid, spassword, ndefaultstatus, nldapstatus, dmodifieddate, nsitecode, nstatus)"
						+ " values (" + nsequenceno + ", '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSservername()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSdomainname()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSorganisationunit()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSgroupname()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsMapValue.get("sldapLink").toString()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSuserid()) + "', '"
						+ stringUtilityFunction.replaceQuote(adsSettings.getSpassword()) + "', "
						+ adsSettings.getNdefaultstatus() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				strAdsSettings = strAdsSettings + "update Seqnoconfigurationmaster set nsequenceno=" + nsequenceno
						+ " where stablename='adssettings'";
				jdbcTemplate.execute(strAdsSettings);
				projectDAOSupport.encryptPassword("adssettings", "nadssettingscode", nsequenceno,
						adsSettings.getSpassword(), "spassword");
				auditUtilityFunction.fnInsertAuditAction(Arrays.asList(adsSettings), 1, null,
						Arrays.asList("IDS_ADDADSSETTINGS"), userInfo);
				return getADSSettings(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunctionObject.getMultilingualMessage(rtnValue, userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunctionObject.getMultilingualMessage("IDS_SERVERLINKALREADYEXISTS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		final ADSSettings objADSSettings = getActiveADSSettingsById(adsSettings.getNadssettingscode(), userInfo);
		if (objADSSettings == null) {
			return new ResponseEntity<>(
					commonFunctionObject.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String additionalCondition = " and nadssettingscode <> " + adsSettings.getNadssettingscode();
			final ADSSettings adsSettingsListByServerAndDomainName = getADSSettingsByServerAndDomainName(
					adsSettings.getSservername().toString(), adsSettings.getSdomainname().toString(),
					userInfo.getNmastersitecode(), additionalCondition);
			if (adsSettingsListByServerAndDomainName == null) {
				final Map<String, Object> adsMapValue = formLDAPLinkAndCheckADSConnection(adsSettings, userInfo);
				final String rtnValue = adsMapValue.get("rtnValue").toString();
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == rtnValue) {
					ADSSettings defaultADSSettings = getADSSettingsByStatus(adsSettings.getNsitecode(),
							additionalCondition);
					if (adsSettings.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if (defaultADSSettings != null) {
							final ADSSettings oldADSSettings = SerializationUtils.clone(defaultADSSettings);
							defaultADSSettings
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = "update adssettings set ndefaultstatus = "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nadssettingscode="
									+ defaultADSSettings.getNadssettingscode();
							jdbcTemplate.execute(updateQueryString);
							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(defaultADSSettings), 2,
									Arrays.asList(oldADSSettings), Arrays.asList("IDS_EDITADSSETTINGS"), userInfo);

						}
					} else {
						if (defaultADSSettings == null) {
							return new ResponseEntity<>(
									commonFunctionObject.getMultilingualMessage(
											"IDS_ATLEASTONEADSSETTINGSMUSTBEDEFAULT", userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					}

					final String updateADSSettings = "update adssettings set sservername='"
							+ stringUtilityFunction.replaceQuote(adsSettings.getSservername()) + "', sdomainname='"
							+ stringUtilityFunction.replaceQuote(adsSettings.getSdomainname())
							+ "', sorganisationunit='"
							+ stringUtilityFunction.replaceQuote(adsSettings.getSorganisationunit()) + "', sgroupname='"
							+ stringUtilityFunction.replaceQuote(adsSettings.getSgroupname()) + "', sldaplink='"
							+ stringUtilityFunction.replaceQuote(adsMapValue.get("sldapLink").toString())
							+ "', suserid='" + stringUtilityFunction.replaceQuote(adsSettings.getSuserid())
							+ "', spassword='" + stringUtilityFunction.replaceQuote(adsSettings.getSpassword())
							+ "', ndefaultstatus=" + adsSettings.getNdefaultstatus() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nadssettingscode="
							+ adsSettings.getNadssettingscode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateADSSettings);
					projectDAOSupport.encryptPassword("adssettings", "nadssettingscode",
							adsSettings.getNadssettingscode(), adsSettings.getSpassword(), "spassword");
					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(adsSettings), 2,
							Arrays.asList(objADSSettings), Arrays.asList("IDS_EDITADSSETTINGS"), userInfo);
					return getADSSettings(userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunctionObject.getMultilingualMessage(rtnValue, userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunctionObject.getMultilingualMessage("IDS_SERVERLINKALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteADSSettings(ADSSettings adsSettings, UserInfo userInfo) throws Exception {
		final ADSSettings objADSSettings = getActiveADSSettingsById(adsSettings.getNadssettingscode(), userInfo);
		if (objADSSettings == null) {
			return new ResponseEntity<>(
					commonFunctionObject.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String additionCondition = " and nadssettingscode=" + adsSettings.getNadssettingscode();
			final ADSSettings defaultADSSettings = getADSSettingsByStatus(adsSettings.getNsitecode(),
					additionCondition);
			if (defaultADSSettings != null) {
				return new ResponseEntity<>(commonFunctionObject.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			final String deleteADSSettings = "update adssettings set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nadssettingscode="
					+ adsSettings.getNadssettingscode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(deleteADSSettings);
			auditUtilityFunction.fnInsertAuditAction(Arrays.asList(adsSettings), 1, null,
					Arrays.asList("IDS_DELETEADSSETTINGS"), userInfo);
		}
		return getADSSettings(userInfo);
	}

	private ADSSettings getADSSettingsByServerAndDomainName(final String serverName, final String domainName,
			final int nmasterSiteCode, final String additionalCondition) throws Exception {
		final String strQuery = "select nadssettingscode from adssettings where sservername=N'"
				+ stringUtilityFunction.replaceQuote(serverName) + "' and  sdomainname=N'" + domainName
				+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ nmasterSiteCode + additionalCondition;
		return (ADSSettings) jdbcUtilityFunction.queryForObject(strQuery, ADSSettings.class, jdbcTemplate);
	}

	private ADSSettings getADSSettingsByStatus(int nmasterSiteCode, String additionalCondition) throws Exception {
		final String strQuery = "Select * from adssettings where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode
				+ " and ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ additionalCondition;
		return (ADSSettings) jdbcUtilityFunction.queryForObject(strQuery, ADSSettings.class, jdbcTemplate);
	}

	private Map<String, Object> formLDAPLinkAndCheckADSConnection(ADSSettings adsSettings, UserInfo userInfo)
			throws Exception {
		Map<String, Object> rtnMap = new HashMap<>();
		String sgroupName = "";
		String sorganisationUnit = "";
		if (!adsSettings.getSgroupname().isEmpty()) {
			sgroupName = "OU=" + adsSettings.getSgroupname();
		}
		sorganisationUnit = sgroupName != "" ? sgroupName + "," : "";
		if (!adsSettings.getSorganisationunit().isEmpty()) {
			sorganisationUnit = sorganisationUnit + "OU=" + adsSettings.getSorganisationunit() + ",";
		}
		final String sdomainName = sorganisationUnit
				+ (adsSettings.getSdomainname().contains(".") ? Arrays.stream(adsSettings.getSdomainname().split("\\."))
						.map(item -> "DC=" + item).collect(Collectors.joining(","))
						: "DC=" + adsSettings.getSdomainname());
		final String sldapLink = ("LDAP://" + adsSettings.getSservername() + "/" + sdomainName).replaceAll(" ", "%20");
		adsSettings.setSldaplink(sldapLink);
		adsSettings.setSloginid(adsSettings.getSuserid());
		final String rtnValue = loginDAOImpl.getAdsconnection(adsSettings);
		rtnMap.put("sldapLink", sldapLink);
		rtnMap.put("rtnValue", rtnValue);
		return rtnMap;
	}
}
