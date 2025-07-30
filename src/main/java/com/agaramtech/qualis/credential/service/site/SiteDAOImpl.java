package com.agaramtech.qualis.credential.service.site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.DateFormat;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.SiteConfig;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.model.District;
import com.agaramtech.qualis.submitter.model.Region;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SiteDAOImpl implements SiteDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		String strQuery = "select * from site where nsitecode > 0 and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode= "
				+ userInfo.getNmastersitecode() + " order by ndefaultstatus";
		LOGGER.info(strQuery);
		final List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());
		return new ResponseEntity<Object>(lstSite, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSiteForFTP() throws Exception {
		String strQuery = "select * from site where nsitecode in "
				+ " (select s.nsitecode from site s,siteconfig sc where s.nsitecode>0 and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsitecode=sc.nsitecode and sc.nisstandaloneserver="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ") "
				+ " or nsitecode in(select nsitecode from site where nsitecode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) order by ndefaultstatus ";

		final List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());

		// NIBSCRT-2111
		final String tempFolderPath = System.getProperty("java.io.tmpdir");
		@SuppressWarnings("serial")
		final Map<String, Object> returnMap = new HashMap<String, Object>() {
			{
				put("SiteList", lstSite);
				put("CheckSumDefaultPath", tempFolderPath);
			}
		};
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSiteScreen(final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsitecode,s.ntimezonecode,s.ndateformatcode,s.ssitename,s.ssiteaddress,sc.nissyncserver,"
				+ "case when s.scontactperson = '' then '-' else s.scontactperson end,"
				+ "case when s.ssitecode= 'NA' then '-' else s.ssitecode end,"
				+ "case when r.sregionname = 'NA' then '-' else r.sregionname end ,"
				+ "case when d.sdistrictname = 'NA' then '-' else d.sdistrictname end ,"
				+ "	s.sphoneno,s.sfaxno,s.semail,s.ndefaultstatus,s.nismultisite,s.nmastersitecode,s.nstatus,"
				+ "to_char(s.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate, " + "  coalesce(df.jsondata->'sdateformat'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "df.jsondata->'sdateformat'->>'en-US')  as sdateformat ,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as sdistributedstatus,"
				+ "coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts2.jsondata->'stransdisplaystatus'->>'en-US') as sprimarysereverstatus"
				+ ",sc.nisstandaloneserver ,s.nregioncode,s.ndistrictcode, "
				+ " case when tz.ntimezonecode=-1 then '-' else  tz.stimezoneid end,st.ssitetypename,st.nsitetypecode  from Site s,transactionstatus ts,timezone tz,siteconfig sc,dateformat df,transactionstatus ts1,"
				+ " region r ,district d ,transactionstatus ts2,sitetype st  where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and st.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and s.nsitecode>0 and s.ntimezonecode=tz.ntimezonecode and s.ndateformatcode=df.ndateformatcode "
				+ " and sc.nisstandaloneserver=ts1.ntranscode and s.nsitecode=sc.nsitecode and ts.ntranscode=s.ndefaultstatus and r.nregioncode=s.nregioncode and d.ndistrictcode =s.ndistrictcode  "
				+ " and sc.nissyncserver=ts2.ntranscode and sc.nsitetypecode = st.nsitetypecode and s.nmastersitecode=" + userInfo.getNmastersitecode()
				+ " and r.nsitecode=" + userInfo.getNmastersitecode() + " and d.nsitecode="
				+ userInfo.getNmastersitecode() + " and df.nsitecode=" + userInfo.getNmastersitecode()
				+ " order by s.nsitecode asc";
		return new ResponseEntity<Object>((List<Site>) jdbcTemplate.query(strQuery, new Site()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSiteScreen(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table locksite " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<Object> savedSiteList = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		Site objSite = objMapper.convertValue(inputMap.get("site"), new TypeReference<Site>() {
		});
		final SiteConfig objDateFormat = objMapper.convertValue(inputMap.get("sitedateformat"), SiteConfig.class);
		final List<Site> siteListByName = getSiteListByName(objSite.getSsitename(), objSite.getNmastersitecode());
		final List<Site> siteListByCode = getSiteListByCode(objSite.getSsitecode(), objSite.getNmastersitecode());

		if (siteListByName.isEmpty()) {
			if (userInfo.getNsiteadditionalinfo() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				if (!siteListByCode.isEmpty()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);

				}
			}
			if (objDateFormat.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final SiteConfig defaultprimaryserver = getSiteByPrimaryServerStatus();
				if (defaultprimaryserver != null) {
					// Copy of object before update
					final SiteConfig siteBeforeSave = new SiteConfig();

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(siteBeforeSave);

					defaultprimaryserver
							.setNissyncserver((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update Siteconfig set nissyncserver="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsitecode ="
							+ defaultprimaryserver.getNsitecode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultprimaryserver);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							Arrays.asList("IDS_EDITSITECONFIG"), userInfo);
				}
			} 
// ALPD-5899 commended this line Mullai Balaji
//	         start 
//			else {
//			//	final SiteConfig defaultprimaryserver = getSiteByPrimaryServerStatus();
//				if (defaultprimaryserver == null ) {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ATLEASTONEPRIMARYSITE",
//						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
//				}
//			}
//			End
			if (objSite.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final Site defaultSite = getSiteByDefaultStatus(objSite.getNmastersitecode());

				if (defaultSite != null) {

					// Copy of object before update
					final Site siteBeforeSave = new Site();

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(siteBeforeSave);

					defaultSite.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update Site set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsitecode ="
							+ defaultSite.getNsitecode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultSite);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							Arrays.asList("IDS_EDITSITE"), userInfo);
				}

			}

			if (objSite.getScontactperson() == null) {

				objSite.setScontactperson("");
			}
			if (objSite.getSemail() == null) {

				objSite.setSemail("");
			}
			final String contactperson = objSite.getScontactperson() == null ? "" : objSite.getScontactperson();
			final String email = objSite.getSemail() == null ? "" : objSite.getSemail();
			final String siteaddress = objSite.getSsiteaddress() == null ? "" : objSite.getSsiteaddress();
			final String faxno = objSite.getSfaxno() == null ? "" : objSite.getSfaxno();
			final String phoneno = objSite.getSphoneno() == null ? "" : objSite.getSphoneno();

			short nseqno = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobasemaster where stablename='site'", short.class);
			nseqno++;
			String insertstr = "insert into site (nsitecode,ntimezonecode,ndateformatcode,nregioncode,ndistrictcode,ssitename,ssitecode,ssiteaddress,scontactperson,"
					+ "sphoneno,sfaxno,semail,ndefaultstatus,nismultisite,dmodifieddate,nmastersitecode,nstatus) values ("
					+ nseqno + "," + objSite.getNtimezonecode() + "," + objSite.getNdateformatcode() + ","
					+ objSite.getNregioncode() + "," + objSite.getNdistrictcode() + ",N'"
					+ stringUtilityFunction.replaceQuote(objSite.getSsitename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSite.getSsitecode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(siteaddress) + "',N'"
					+ stringUtilityFunction.replaceQuote(contactperson) + "',N'"
					+ stringUtilityFunction.replaceQuote(phoneno) + "',N'" + stringUtilityFunction.replaceQuote(faxno)
					+ "',N'" + stringUtilityFunction.replaceQuote(email) + "'," + objSite.getNdefaultstatus() + ",4,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + objSite.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			insertstr = insertstr + "insert into siteconfig("
					+ "	nsitecode, ssitedatetime, ssitereportdatetime, ssitereportdate, ssitedate, spgdatetime, spgreportdatetime, nisstandaloneserver,dmodifieddate,nstatus,nissyncserver,nsitetypecode)"
					+ "	values (" + nseqno + ",'" + objDateFormat.getSsitedatetime() + "','"
					+ objDateFormat.getSsitereportdatetime() + "','" + objDateFormat.getSsitereportdate() + "','"
					+ objDateFormat.getSsitedate() + "','" + objDateFormat.getSpgdatetime() + "','"
					+ objDateFormat.getSpgreportdatetime() + "'," + objDateFormat.getNisstandaloneserver() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ objDateFormat.getNissyncserver() + ","+objDateFormat.getNsitetypecode()+");";
			jdbcTemplate.execute(insertstr);
			jdbcTemplate.execute("update seqnobasemaster set nsequenceno = " + nseqno + " where stablename='site'");
			objSite.setNsitecode(nseqno);
			objDateFormat.setNsitecode(nseqno);
			if (objDateFormat.getNisstandaloneserver() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				objSite.setSsitecode(null);
			}
			if (!objDateFormat.getNeedutcconversation().equals("true")) {
				objSite.setNdateformatcode((short) 0);
				objSite.setNtimezonecode((short) 0);
			}
			List<Site> ins = new ArrayList<Site>();
			ins.add(objSite);
			List<SiteConfig> insconig = new ArrayList<SiteConfig>();
			insconig.add(objDateFormat);
			savedSiteList.add(ins);
			savedSiteList.add(insconig);
			auditUtilityFunction.fnInsertListAuditAction(savedSiteList, 1, null,
					Arrays.asList("IDS_ADDSITE", "IDS_ADDSITECONFIG"), userInfo);

			return getSiteScreen(userInfo);

		} else {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private SiteConfig getSiteByPrimaryServerStatus() throws Exception {
		final String strQuery = "select * from siteconfig s" + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nissyncserver="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "";
		return (SiteConfig) jdbcUtilityFunction.queryForObject(strQuery, SiteConfig.class, jdbcTemplate);
	}

	private Site getSiteByDefaultStatus(int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from site s" + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and s.nmastersitecode = "
				+ nmasterSiteCode;
		return (Site) jdbcUtilityFunction.queryForObject(strQuery, Site.class, jdbcTemplate);
	}

	private List<Site> getSiteListByName(String ssitename, int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsitecode from site where ssitename = N'"
				+ stringUtilityFunction.replaceQuote(ssitename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode ="
				+ nmasterSiteCode;
		return (List<Site>) jdbcTemplate.query(strQuery, new Site());
	}

	private List<Site> getSiteListByCode(String ssitecode, int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsitecode from site where " + " ssitecode='"
				+ stringUtilityFunction.replaceQuote(ssitecode) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode ="
				+ nmasterSiteCode;
		return (List<Site>) jdbcTemplate.query(strQuery, new Site());
	}

	@Override
	public ResponseEntity<Object> updateSiteScreen(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final List<Object> beforeSavedSiteList = new ArrayList<>();
		final List<Object> savedSiteList = new ArrayList<>();
		List<String> multilingualIDList = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();

		Site basemasterSite = objMapper.convertValue(inputMap.get("site"), new TypeReference<Site>() {
		});
		final SiteConfig objDateFormat = objMapper.convertValue(inputMap.get("sitedateformat"), SiteConfig.class);

		final Site site = (Site) getActiveSiteById(basemasterSite.getNsitecode(), userInfo);

		String querys = "select nisstandaloneserver,nsitecode,nissyncserver from siteconfig where nsitecode="
				+ basemasterSite.getNsitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SiteConfig> siteList = jdbcTemplate.query(querys, new SiteConfig());

		final String strNameQuery = "select nsitecode from site where ssitename = N'"
				+ stringUtilityFunction.replaceQuote(basemasterSite.getSsitename()) + "' and nsitecode not in("
				+ basemasterSite.getNsitecode() + "," + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nmastersitecode =" + basemasterSite.getNmastersitecode();
		final List<Site> siteListByName = (List<Site>) jdbcTemplate.query(strNameQuery, new Site());

		final String strCodeQuery = "select nsitecode from site where " + " ssitecode='"
				+ stringUtilityFunction.replaceQuote(basemasterSite.getSsitecode())
				+ "' and nsitecode>0 and nsitecode <> " + basemasterSite.getNsitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode ="
				+ basemasterSite.getNmastersitecode();
		final List<Site> siteListByCode = (List<Site>) jdbcTemplate.query(strCodeQuery, new Site());

		if (site == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (basemasterSite.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final Site defaultSite = getSiteByDefaultStatus(basemasterSite.getNmastersitecode());

				if (defaultSite != null && defaultSite.getNsitecode() != basemasterSite.getNsitecode()) {

					// Copy of object before update
					final Site siteBeforeSave = SerializationUtils.clone(defaultSite);
					beforeSavedSiteList.add(siteBeforeSave);

					defaultSite.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update site set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsitecode="
							+ defaultSite.getNsitecode();
					jdbcTemplate.execute(updateQueryString);

					savedSiteList.add(defaultSite);
				}
			}
			if (objDateFormat.getNissyncserver() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				// ALPD-4180
				// Site Screen: Edit Primary Server toggle button and save throws 500 error
				final String strQuery = "select * from siteconfig s" + " where s.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode <> "
						+ basemasterSite.getNsitecode() + " and s.nissyncserver="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "";
				final SiteConfig defaultprimaryserver = (SiteConfig) jdbcUtilityFunction.queryForObject(strQuery,
						SiteConfig.class, jdbcTemplate);

				if (!(defaultprimaryserver == null)) {
					if (defaultprimaryserver.getNsitecode() != basemasterSite.getNsitecode()) {

						// Copy of object before update
						final SiteConfig siteBeforeSave = new SiteConfig();
						beforeSavedSiteList.add(siteBeforeSave);

						defaultprimaryserver
								.setNissyncserver((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update siteconfig set nissyncserver="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsitecode="
								+ defaultprimaryserver.getNsitecode();
						jdbcTemplate.execute(updateQueryString);

						savedSiteList.add(defaultprimaryserver);
					}
				}

			}
// ALPD-5899 commended this line Mullai Balaji
//         start 
//			else {
//				final SiteConfig defaultprimaryserver = getSiteByPrimaryServerStatus();
//
//				if (defaultprimaryserver != null
//						&& defaultprimaryserver.getNsitecode() == basemasterSite.getNsitecode()) {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ATLEASTONEPRIMARYSITE",
//							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
//
//				}
//			}
//       End			
			
			if (siteListByName.isEmpty()) {
				if (userInfo.getNsiteadditionalinfo() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (!siteListByCode.isEmpty()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

					}
				}
				String updateQueryString = "update site set ssitename=N'"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSsitename()) + "', ssitecode =N'"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSsitecode()) + "', ssiteaddress= N'"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSsiteaddress()) + "', scontactperson =N'"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getScontactperson()) + "', sphoneno= '"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSphoneno()) + "', sfaxno = '"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSfaxno()) + "', semail ='"
						+ stringUtilityFunction.replaceQuote(basemasterSite.getSemail()) + "', ndefaultstatus ="
						+ basemasterSite.getNdefaultstatus() + ", ntimezonecode ='" + basemasterSite.getNtimezonecode()
						+ "',ndateformatcode=" + basemasterSite.getNdateformatcode() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nregioncode="
						+ basemasterSite.getNregioncode() + ",ndistrictcode=" + basemasterSite.getNdistrictcode()
						+ " where nsitecode=" + basemasterSite.getNsitecode() + ";";

				updateQueryString = updateQueryString + "update siteconfig set " + " ssitedatetime =N'"
						+ stringUtilityFunction.replaceQuote(objDateFormat.getSsitedatetime())
						+ "', ssitereportdatetime= N'"
						+ stringUtilityFunction.replaceQuote(objDateFormat.getSsitereportdatetime())
						+ "', ssitereportdate =N'"
						+ stringUtilityFunction.replaceQuote(objDateFormat.getSsitereportdate()) + "', ssitedate= '"
						+ objDateFormat.getSsitedate() + "', spgdatetime = '" + objDateFormat.getSpgdatetime()
						+ "', nissyncserver =" + objDateFormat.getNissyncserver() + ", spgreportdatetime ='"
						+ objDateFormat.getSpgreportdatetime() + "', nisstandaloneserver="
						+ objDateFormat.getNisstandaloneserver() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitetypecode="+objDateFormat.getNsitetypecode()+" where nsitecode="
						+ basemasterSite.getNsitecode() + ";";

				jdbcTemplate.execute(updateQueryString);
				if (objDateFormat.getNisstandaloneserver() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					site.setSsitecode(null);
					basemasterSite.setSsitecode(null);
				}
				List<Site> insconig = new ArrayList<Site>();
				insconig.add(site);
				List<Site> ins = new ArrayList<Site>();
				ins.add(basemasterSite);
				List<SiteConfig> insob = new ArrayList<SiteConfig>();
				insob.add(objDateFormat);
				savedSiteList.add(ins);
				savedSiteList.add(insob);
				beforeSavedSiteList.add(insconig);
				beforeSavedSiteList.add(siteList);
				multilingualIDList.add("IDS_EDITSITE");
				multilingualIDList.add("IDS_EDITSITECONFIG");
				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(ins, insob), 2,
						Arrays.asList(insconig, siteList), Arrays.asList("IDS_EDITSITE", "IDS_EDITSITECONFIG"),
						userInfo);

				// status code:200
				return getSiteScreen(userInfo);
			}

			else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteSiteScreen(Site objSite, UserInfo userInfo) throws Exception {
		final Site site = getActiveSiteById(objSite.getNsitecode(), userInfo);
		String strquery = "select nisstandaloneserver,nissyncserver ,nsitecode from siteconfig where nsitecode="
				+ objSite.getNsitecode();
		List<SiteConfig> lstInsSite = jdbcTemplate.query(strquery, new SiteConfig());

		if (site == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_USERSSITE' as Msg from userssite where nsitecode= " + site.getNsitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_USERS' as Msg from users where nsitecode=" + site.getNsitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_INSTRUMENT' as Msg from instrument where nsitecode=" + site.getNsitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_INSTITUTIONSITE' as Msg from institutionsite where nregionalsitecode= "
					+ site.getNsitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_LIMSELNSITEMAPPING' as Msg from limselnsitemapping where nlimssitecode="
					+ site.getNsitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_SAMPLEPLANTMAPPING' as Msg from sampleplantmapping where nmappingsitecode= "
					+ site.getNsitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(site.getNsitecode()),
						userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				String updateQueryString = "update site set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsitecode="
						+ objSite.getNsitecode() + ";";

				updateQueryString = updateQueryString + "update siteconfig set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsitecode="
						+ objSite.getNsitecode() + ";";

				jdbcTemplate.execute(updateQueryString);

				objSite.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				if (objSite.getNisstandaloneserver() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					objSite.setSsitecode(null);
				}
				if (!objSite.getNeedutcconversation().equals("true")) {
					objSite.setNdateformatcode((short) 0);
					objSite.setNtimezonecode((short) 0);
				}
				List<Site> ins = new ArrayList<Site>();
				ins.add(objSite);
				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(ins, lstInsSite), 1, null,
						Arrays.asList("IDS_DELETESITE", "IDS_DELETESITECONFIG"), userInfo);

				return getSiteScreen(userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public Site getActiveSiteById(final int nsitecode, UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsitecode,s.ntimezonecode,tz.stimezoneid ," + "s.ssitename," + "s.ssitecode,"
				+ "s.ssiteaddress," + "s.scontactperson,sc.nissyncserver,"
				+ "case when s.sphoneno='null' then ' ' else s.sphoneno end as sphoneno," + "s.sfaxno," + "s.semail,"
				+ "s.ndefaultstatus," + "s.nismultisite," + "s.nstatus,"
				+ "s.nmastersitecode,coalesce(df.jsondata->'sdateformat'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "df.jsondata->'sdateformat'->>'en-US') as sdateformat,df.ndateformatcode,sc.nisstandaloneserver,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts2.jsondata->'stransdisplaystatus'->>'en-US') as sprimarysereverstatus,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as sdistributedstatus,s.nregioncode,s.ndistrictcode,r.sregionname,d.sdistrictname,st.ssitetypename,st.nsitetypecode  "
				+ " from Site s,transactionstatus ts,timezone tz,dateformat df ,siteconfig sc,transactionstatus ts1, region r,district d,transactionstatus ts2,sitetype st where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and s.ndateformatcode=df.ndateformatcode "
				+ " and ts.ntranscode=s.ndefaultstatus  and s.nsitecode=sc.nsitecode and sc.nisstandaloneserver=ts1.ntranscode and r.nregioncode=s.nregioncode and d.ndistrictcode=s.ndistrictcode and sc.nsitetypecode = st.nsitetypecode  "
				+ " and s.ntimezonecode=tz.ntimezonecode and sc.nissyncserver=ts2.ntranscode and  s.nsitecode = "
				+ nsitecode + " and d.nsitecode=" + userInfo.getNmastersitecode() + " and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and df.nsitecode=" + userInfo.getNmastersitecode();

		return (Site) jdbcUtilityFunction.queryForObject(strQuery, Site.class, jdbcTemplate);
	}

	public ResponseEntity<Object> getDateFormat(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ndateformatcode,coalesce(jsondata->'sdateformat'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sdateformat'->>'en-US') as sdateformat from dateformat where " + " nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ndateformatcode > 0 and nsitecode=" + userInfo.getNmastersitecode();
		outputMap.put("DateFormat", jdbcTemplate.query(strQuery, new DateFormat()));
		final String regionQuery = "select * from region where nregioncode > 0  " + "and  nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		outputMap.put("Region", jdbcTemplate.query(regionQuery, new Region()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select  * from district" + " where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  ndistrictcode > 0 and nregioncode =" + nregioncode + " and nsitecode="
				+ userInfo.getNmastersitecode();
		outputMap.put("districtList", jdbcTemplate.query(query, new District()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
