
package com.agaramtech.qualis.storagemanagement.service.retrievalcertificate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.basemaster.service.storagecondition.StorageConditionDAO;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.storagemanagement.model.RetrievalCertificate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RetrievalCertificateDAOImpl implements RetrievalCertificateDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetrievalCertificateDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final StorageConditionDAO storageConditionDAO;
	private final ProjectDAOSupport projectDAOSupport;
	private final ReportDAOSupport reportDAOSupport;

	@Override
	public ResponseEntity<Object> getRetrievalCertificate(Integer nretrievalcertificatecode, String fromDate,
			String toDate, final UserInfo userInfo, String currentUIDate) throws Exception {
		RetrievalCertificate selectedRetrievalCertificate = null;
		ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		String strQuery = "select rc.nprojectmastercode, rc.nprojecttypecode, rc.nretrievalcertificatecode,rc.nstorageconditioncode,rc.sretrievalcertificateno,rc.sbiomaterialtype,rc.srequestid,"
				+ " to_char(rc.dretrievalcertificatedate,'" + userInfo.getSpgsitedatetime()
				+ "') as sretrievalcertificatedate,rc.semail,rc.scomment,rch.ntransactionstatus,"
				+ "rc.stestingmethod,rc.spreparationmethod,rc.sinvestigatorname,rc.sorganizationaddress,rc.sphoneno, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ " pm.sprojectname, pt.sprojecttypename, sc.sstorageconditionname"
				+ " from retrievalcertificate rc, projectmaster pm, projecttype pt, transactionstatus ts,retrievalcertificatehistory rch,storagecondition sc where "
				+ " sc.nstorageconditioncode=rc.nstorageconditioncode  and rc.nretrievalcertificatecode=rch.nretrievalcertificatecode and "
				+ " pm.nprojectmastercode = rc.nprojectmastercode and pt.nprojecttypecode=rc.nprojecttypecode and rch.ntransactionstatus=ts.ntranscode "
				+ " and rch.nretrievalcertificatehistorycode=any(select max(nretrievalcertificatehistorycode) from"
				+ "	retrievalcertificatehistory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by nretrievalcertificatecode) "
				+ " and rc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "rc.nsitecode=rch.nsitecode and  pt.nsitecode = " + userInfo.getNmastersitecode() + ""
				+ "  and sc.nsitecode = " + userInfo.getNmastersitecode() + "" + "  and pm.nsitecode = "
				+ userInfo.getNtranssitecode() + " and rc.nsitecode=" + userInfo.getNtranssitecode()
				+ " and dretrievalcertificatedate between '" + fromDate + "'  and  '" + toDate + "' "
				+ "order by rc.nretrievalcertificatecode asc";
		List<RetrievalCertificate> lstRetrievalCertificate = jdbcTemplate.query(strQuery, new RetrievalCertificate());
		final List<RetrievalCertificate> lstUTCConvertedDate = objMapper
				.convertValue(dateUtilityFunction.getSiteLocalTimeFromUTC(lstRetrievalCertificate,
						Arrays.asList("sretrievalcertificatedate"), Arrays.asList(userInfo.getStimezoneid()), userInfo,
						false, null, false), new TypeReference<List<RetrievalCertificate>>() {
				});
		if (lstUTCConvertedDate == null) {
			outputMap.put("SelectedRetrievalCertificate", null);
			outputMap.put("RetrievalCertificate", lstUTCConvertedDate);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			final List<String> columnList = new ArrayList<>();
			columnList.add("stransdisplaystatus");
			final List<RetrievalCertificate> modifiedRetrievalList = objMapper.convertValue(commonFunction
					.getMultilingualMessageList(lstUTCConvertedDate, columnList, userInfo.getSlanguagefilename()),
					new TypeReference<List<RetrievalCertificate>>() {
			});
			outputMap.put("RetrievalCertificate", modifiedRetrievalList);
			if (!lstUTCConvertedDate.isEmpty()) {
				if (nretrievalcertificatecode == null) {
					selectedRetrievalCertificate = (modifiedRetrievalList.get(modifiedRetrievalList.size() - 1));
					nretrievalcertificatecode = selectedRetrievalCertificate.getNretrievalcertificatecode();
				} else {
					RetrievalCertificate lstRetrievalCertificateById = ((RetrievalCertificate) getActiveRetrievalCertificateById(
							nretrievalcertificatecode, userInfo).getBody());
					selectedRetrievalCertificate = lstRetrievalCertificateById;
				}
			}
		}
		if (selectedRetrievalCertificate == null && !lstRetrievalCertificate.isEmpty()) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedRetrievalCertificate", selectedRetrievalCertificate);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getProject(int nprojectTypeCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select pm.sprojectname,pm.nprojectmastercode, pm.nprojecttypecode "
				+ "from projectmaster pm,transactionstatus ts,projectmasterhistory pmh "
				+ " where pm.nprojectmastercode=pmh.nprojectmastercode and pmh.ntransactionstatus=ts.ntranscode "
				+ " and pm.nsitecode=pmh.nsitecode  and pmh.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ " and pm.nprojectmastercode>0 and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pmh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pmh.nsitecode ="
				+ userInfo.getNtranssitecode() + " and pm.nprojecttypecode=" + nprojectTypeCode;
		LOGGER.info("getProject:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProjectUsers(int nprojectmastercode, UserInfo userInfo) throws Exception {
		final String strQuery = "select CONCAT(us.sfirstname,' ',us.slastname) as susername,us.nusercode,us.sphoneno,us.semail "
				+ "from projectmaster pm, users us  where  pm.nusercode=us.nusercode and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nsitecode ="
				+ userInfo.getNtranssitecode() + " and us.nsitecode =" + userInfo.getNmastersitecode()
				+ " and pm.nprojectmastercode=" + nprojectmastercode + "";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new RetrievalCertificate()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSiteAddress(UserInfo userInfo) throws Exception {
		final String strQuery = "select ssiteaddress from site where nsitecode=" + userInfo.getNtranssitecode()
		+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Site()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createRetrievalCertificate(RetrievalCertificate retrievalcertification,
			UserInfo userInfo, String fromDate, String toDate, String currentUIDate) throws Exception {
		final String sQueryLock = " lock  table lockretrievalcertificate "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRetreivalCertificationList = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		retrievalcertification.setSretrievalcertificatedate(
				dateUtilityFunction.instantDateToString(retrievalcertification.getDretrievalcertificatedate())
				.replace("T", " ").replace("Z", ""));
		StorageCondition storagecondition = storageConditionDAO
				.getActiveStorageConditionById(retrievalcertification.getNstorageconditioncode());
		if (storagecondition == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STORAGECONDITIONALEREDYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		final RetrievalCertificate convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(retrievalcertification,
						Arrays.asList("sretrievalcertificatedate"), Arrays.asList("ntzretrievalcertificatedate"), true,
						userInfo), // , false),
				new TypeReference<RetrievalCertificate>() {
				});
		String scomment = retrievalcertification.getScomment() == null ? "" : retrievalcertification.getScomment();
		final String sGetSeqNoQuery = "select nsequenceno from seqnostoragemanagement where stablename = 'retrievalcertificate';";
		int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
		retrievalcertification.setNretrievalcertificatecode(nSeqNo++);
		final String sInsertQuery = "insert into retrievalcertificate( "
				+ "nretrievalcertificatecode, nprojecttypecode, nprojectmastercode, nstorageconditioncode, sretrievalcertificateno,"
				+ "sbiomaterialtype, srequestid, stestingmethod, spreparationmethod, sinvestigatorname, sorganizationaddress,"
				+ "sphoneno, semail, scomment ,dretrievalcertificatedate, ntzretrievalcertificatedate, noffsetdretrievalcertificatedate,"
				+ "dtransactiondate, nsitecode, nstatus)" + "values (" + nSeqNo + ","
				+ retrievalcertification.getNprojecttypecode() + "," + retrievalcertification.getNprojectmastercode()
				+ "," + retrievalcertification.getNstorageconditioncode() + ",'" + "-" + "',N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSbiomaterialtype()) + "', N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSrequestid()) + "', N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getStestingmethod()) + "',  N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSpreparationmethod()) + "', N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSinvestigatorname()) + "',  N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSorganizationaddress()) + "',  N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSphoneno()) + "',  N'"
				+ stringUtilityFunction.replaceQuote(retrievalcertification.getSemail()) + "', N'"
				+ stringUtilityFunction.replaceQuote(scomment) + "', '" + convertedObject.getSretrievalcertificatedate()
				+ "'," + convertedObject.getNtzretrievalcertificatedate() + ","
				+ convertedObject.getNoffsetdretrievalcertificatedate() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		jdbcTemplate.execute(sInsertQuery);
		int nretrievalcertificatehistorycode = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnostoragemanagement where stablename='retrievalcertificatehistory'",
				Integer.class);
		nretrievalcertificatehistorycode++;
		String query = "";
		final String historyQuery = insertRetrievalCertificateHistory(nretrievalcertificatehistorycode, nSeqNo,
				userInfo, Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
		query = query + historyQuery;
		jdbcTemplate.execute(query);
		final String sUpdateSeqNoQuery = "update seqnostoragemanagement set nsequenceno = " + nSeqNo
				+ " where stablename = 'retrievalcertificate';";
		jdbcTemplate.execute(sUpdateSeqNoQuery);
		retrievalcertification.setNretrievalcertificatecode(nSeqNo);
		savedRetreivalCertificationList.add(retrievalcertification);
		multilingualIDList.add("IDS_ADDRETRIEVALCERTIFICATE");
		auditUtilityFunction.fnInsertAuditAction(savedRetreivalCertificationList, 1, null, multilingualIDList,
				userInfo);
		return getRetrievalCertificate(null, fromDate, toDate, userInfo, null);
	}

	public String insertRetrievalCertificateHistory(final int nretrievalcertificatehistorycode,
			final int nretrievalcertificatecode, UserInfo userInfo, final int nstatuscode) throws Exception {
		String query = "";
		query = " insert into retrievalcertificatehistory(nretrievalcertificatehistorycode, nretrievalcertificatecode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) "
				+ "values (" + nretrievalcertificatehistorycode + "," + nretrievalcertificatecode + ","
				+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
				+ userInfo.getNdeputyuserrole() + ", " + nstatuscode + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
				+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		query = query + "update seqnostoragemanagement set nsequenceno = " + nretrievalcertificatehistorycode
				+ " where stablename='retrievalcertificatehistory'";
		return query;
	}

	@Override
	public ResponseEntity<Object> getActiveRetrievalCertificateById(final int nretrievalcertificatecode,
			final UserInfo userInfo) throws Exception {
		String strQuery = "select rc.nprojectmastercode,rc.nprojecttypecode, rc.nretrievalcertificatecode,rc.nstorageconditioncode,rc.sretrievalcertificateno,rc.sbiomaterialtype,rc.srequestid,"
				+ " to_char(rc.dretrievalcertificatedate,'" + userInfo.getSpgsitedatetime()
				+ "') as sretrievalcertificatedate,rc.semail,rc.scomment,rch.ntransactionstatus,"
				+ "rc.stestingmethod,rc.spreparationmethod,rc.sinvestigatorname,rc.sorganizationaddress,rc.sphoneno, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ " pm.sprojectname, pt.sprojecttypename, sc.sstorageconditionname"
				+ " from retrievalcertificate rc, projectmaster pm, projecttype pt,transactionstatus ts,retrievalcertificatehistory rch,storagecondition sc where "
				+ " sc.nstorageconditioncode=rc.nstorageconditioncode  and rc.nretrievalcertificatecode=rch.nretrievalcertificatecode and "
				+ " pm.nprojectmastercode = rc.nprojectmastercode and pt.nprojecttypecode=rc.nprojecttypecode and rch.ntransactionstatus=ts.ntranscode "
				+ " and rch.nretrievalcertificatehistorycode=any(select max(nretrievalcertificatehistorycode) from"
				+ "	retrievalcertificatehistory where nretrievalcertificatecode=" + nretrievalcertificatecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by nretrievalcertificatecode) " + " and rc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rc.nsitecode=rch.nsitecode "
				+ " and rc.nsitecode=" + userInfo.getNtranssitecode() + " and rc.nretrievalcertificatecode="
				+ nretrievalcertificatecode + " order by rc.nretrievalcertificatecode asc";
		final ObjectMapper objMapper = new ObjectMapper();
		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(
				jdbcTemplate.query(strQuery, new RetrievalCertificate()), Arrays.asList("sretrievalcertificatedate"),
				Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false);
		List<RetrievalCertificate> objRetrievalCertification = objMapper.convertValue(lstUTCConvertedDate,
				new TypeReference<List<RetrievalCertificate>>() {
		});
		return new ResponseEntity<>(objRetrievalCertification.size() > 0 ? objRetrievalCertification.get(0) : null,
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateRetrievalCertificate(RetrievalCertificate retrievalcertificate, String fromDate,
			String toDate, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRetrievalCertificateList = new ArrayList<>();
		final List<Object> oldRetrievalCertificateList = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ResponseEntity<Object> retrievalcertificateByID = getActiveRetrievalCertificateById(
				retrievalcertificate.getNretrievalcertificatecode(), userInfo);
		final RetrievalCertificate retrievalCertificateID = (RetrievalCertificate) retrievalcertificateByID.getBody();
		oldRetrievalCertificateList.add(retrievalCertificateID);
		if (retrievalCertificateID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (retrievalCertificateID.getNtransactionstatus() != Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				StorageCondition storagecondition = storageConditionDAO
						.getActiveStorageConditionById(retrievalcertificate.getNstorageconditioncode());
				if (storagecondition == null) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_STORAGECONDITIONALEREDYDELETED",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
				retrievalcertificate.setSretrievalcertificatedate(
						dateUtilityFunction.instantDateToString(retrievalcertificate.getDretrievalcertificatedate())
						.replace("T", " ").replace("Z", ""));
				final RetrievalCertificate convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(retrievalcertificate,
								Arrays.asList("sretrievalcertificatedate"),
								Arrays.asList("ntzretrievalcertificatedate"), true, userInfo), // , false),
						new TypeReference<RetrievalCertificate>() {
						});
				final String updateQuery = "update retrievalcertificate set" + " nprojectmastercode="
						+ retrievalcertificate.getNprojectmastercode() + " ," + " nprojecttypecode="
						+ retrievalcertificate.getNprojecttypecode() + " ," + " nstorageconditioncode="
						+ retrievalcertificate.getNstorageconditioncode() + " ," + " sbiomaterialtype = '"
						+ stringUtilityFunction.replaceQuote(retrievalcertificate.getSbiomaterialtype())
						+ "',srequestid = '" + stringUtilityFunction.replaceQuote(retrievalcertificate.getSrequestid())
						+ "',stestingmethod = '"
						+ stringUtilityFunction.replaceQuote(retrievalcertificate.getStestingmethod())
						+ "',spreparationmethod = '"
						+ stringUtilityFunction.replaceQuote(retrievalcertificate.getSpreparationmethod())
						+ "',sinvestigatorname = '"
						+ stringUtilityFunction.replaceQuote(retrievalcertificate.getSinvestigatorname())
						+ "',sorganizationaddress = '"
						+ stringUtilityFunction.replaceQuote(retrievalcertificate.getSorganizationaddress())
						+ "',sphoneno = '" + stringUtilityFunction.replaceQuote(retrievalcertificate.getSphoneno())
						+ "',semail = '" + stringUtilityFunction.replaceQuote(retrievalcertificate.getSemail())
						+ "',scomment = '" + stringUtilityFunction.replaceQuote(retrievalcertificate.getScomment())
						+ "',dretrievalcertificatedate = '" + convertedObject.getSretrievalcertificatedate()
						+ "',noffsetdretrievalcertificatedate=" + convertedObject.getNoffsetdretrievalcertificatedate()
						+ ", dtransactiondate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nretrievalcertificatecode =" + retrievalcertificate.getNretrievalcertificatecode()
						+ ";";
				jdbcTemplate.execute(updateQuery);
				oldRetrievalCertificateList.add(retrievalCertificateID);
				savedRetrievalCertificateList.add(retrievalcertificate);
				multilingualIDList.add("IDS_EDITTRAININGCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(savedRetrievalCertificateList, 2, oldRetrievalCertificateList,
						multilingualIDList, userInfo);
				return getRetrievalCertificate(retrievalcertificate.getNretrievalcertificatecode(), fromDate, toDate,
						userInfo, null);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteRetrievalCertificate(RetrievalCertificate objRetrievalCertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {
		RetrievalCertificate lstRetrievalCertificate = ((RetrievalCertificate) getActiveRetrievalCertificateById(
				objRetrievalCertificate.getNretrievalcertificatecode(), userInfo).getBody());
		if (lstRetrievalCertificate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (lstRetrievalCertificate.getNtransactionstatus() != Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedRetrievalCertificateList = new ArrayList<>();
				final String updateQueryString = "update retrievalcertificate set dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nretrievalcertificatecode=" + objRetrievalCertificate.getNretrievalcertificatecode();
				jdbcTemplate.execute(updateQueryString);
				objRetrievalCertificate
				.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedRetrievalCertificateList.add(objRetrievalCertificate);
				multilingualIDList.add("IDS_DELETERETRIEVALCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(deletedRetrievalCertificateList, 1, null, multilingualIDList,
						userInfo);
				return getRetrievalCertificate(null, fromDate, toDate, userInfo, null);
			}
		}
	}

	@Override
	public ResponseEntity<Object> completeRetrievalCertificate(RetrievalCertificate objRetrievalCertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRetrievalCertificateList = new ArrayList<>();
		RetrievalCertificate retrievalCertificateByID = ((RetrievalCertificate) getActiveRetrievalCertificateById(
				objRetrievalCertificate.getNretrievalcertificatecode(), userInfo).getBody());
		if (retrievalCertificateByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (retrievalCertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
					.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYCOMPLETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if (retrievalCertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String sFormatNo = projectDAOSupport.getSeqfnFormat("retrievalcertificate",
						"seqnoformatgeneratorretrievalcertificate", 0, 0, userInfo);
				final String updateQuery = "update retrievalcertificate set sretrievalcertificateno = '" + sFormatNo
						+ "', dtransactiondate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where nretrievalcertificatecode =" + objRetrievalCertificate.getNretrievalcertificatecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updateQuery);
				int nretrievalcertificatehistorycode = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnostoragemanagement where stablename='retrievalcertificatehistory'",
						Integer.class);
				nretrievalcertificatehistorycode++;
				String query = "";
				final String historyQuery = insertRetrievalCertificateHistory(nretrievalcertificatehistorycode,
						objRetrievalCertificate.getNretrievalcertificatecode(), userInfo,
						Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
				query = query + historyQuery;
				jdbcTemplate.execute(query);
				savedRetrievalCertificateList.add(retrievalCertificateByID);
				multilingualIDList.add("IDS_COMPLETERETRIEVALCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(savedRetrievalCertificateList, 1, null, multilingualIDList,
						userInfo);
				return getRetrievalCertificate(objRetrievalCertificate.getNretrievalcertificatecode(), fromDate, toDate,
						userInfo, null);
			}
		}
	}

	@Override
	public ResponseEntity<Object> checkRetrievalCertificate(RetrievalCertificate objRetrievalCertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRetrievalCertificateList = new ArrayList<>();
		RetrievalCertificate retrievalCertificateByID = ((RetrievalCertificate) getActiveRetrievalCertificateById(
				objRetrievalCertificate.getNretrievalcertificatecode(), userInfo).getBody());
		if (retrievalCertificateByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (retrievalCertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.CHECKED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYCHECKED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if (retrievalCertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.COMPLETED
					.gettransactionstatus()) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCOMPLETEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				int nretrievalcertificatehistorycode = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnostoragemanagement where stablename='retrievalcertificatehistory'",
						Integer.class);
				nretrievalcertificatehistorycode++;
				String query = "";
				final String historyQuery = insertRetrievalCertificateHistory(nretrievalcertificatehistorycode,
						objRetrievalCertificate.getNretrievalcertificatecode(), userInfo,
						Enumeration.TransactionStatus.CHECKED.gettransactionstatus());
				query = query + historyQuery;
				jdbcTemplate.execute(query);
				savedRetrievalCertificateList.add(retrievalCertificateByID);
				multilingualIDList.add("IDS_CHECKRETRIEVALCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(savedRetrievalCertificateList, 1, null, multilingualIDList,
						userInfo);
				return getRetrievalCertificate(objRetrievalCertificate.getNretrievalcertificatecode(), fromDate, toDate,
						userInfo, null);
			}
		}
	}

	@Override
	public ResponseEntity<Object> approveRetrievalCertificate(RetrievalCertificate objRetrievalCertificate,
			String fromDate, String toDate, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedRetrievalCertificateList = new ArrayList<>();
		RetrievalCertificate retrievalCertificateByID = ((RetrievalCertificate) getActiveRetrievalCertificateById(
				objRetrievalCertificate.getNretrievalcertificatecode(), userInfo).getBody());
		if (retrievalCertificateByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (retrievalCertificateByID.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if (retrievalCertificateByID.getNtransactionstatus() != Enumeration.TransactionStatus.CHECKED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCHECKEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				int nretrievalcertificatehistorycode = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnostoragemanagement where stablename='retrievalcertificatehistory'",
						Integer.class);
				nretrievalcertificatehistorycode++;
				String query = "";
				final String historyQuery = insertRetrievalCertificateHistory(nretrievalcertificatehistorycode,
						objRetrievalCertificate.getNretrievalcertificatecode(), userInfo,
						Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
				query = query + historyQuery;
				jdbcTemplate.execute(query);
				savedRetrievalCertificateList.add(retrievalCertificateByID);
				multilingualIDList.add("IDS_APPROVERETRIEVALCERTIFICATE");
				auditUtilityFunction.fnInsertAuditAction(savedRetrievalCertificateList, 1, null, multilingualIDList,
						userInfo);
				return getRetrievalCertificate(objRetrievalCertificate.getNretrievalcertificatecode(), fromDate, toDate,
						userInfo, null);
			}
		}
	}

	@Override
	public ResponseEntity<Object> retrievalreportcertificate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> sNodeServerStart = null;
		sNodeServerStart = reportDAOSupport.validationCheckForNodeServer(inputMap, userInfo);
		if (sNodeServerStart.get("rtn").equals("Failed")) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final int retrievalCertificateID = (int) inputMap.get("nretrievalcertificatecode");
			if (retrievalCertificateID != 0) {
				final String getSpecDetatils = "select nretrievalcertificatecode from retrievalcertificate "
						+ " where nretrievalcertificatecode = " + inputMap.get("nretrievalcertificatecode")
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				RetrievalCertificate retrievalCertificateobj = (RetrievalCertificate) jdbcUtilityFunction
						.queryForObject(getSpecDetatils, RetrievalCertificate.class, jdbcTemplate);
				if (retrievalCertificateobj != null) {
					Map<String, Object> returnMap = new HashMap<>();
					String sfileName = "";
					String sJRXMLname = "";
					int qType = 1;
					int ncontrolCode = -1;
					String sfilesharedfolder = "";
					String fileuploadpath = "";
					String subReportPath = "";
					String imagePath = "";
					String pdfPath = "";
					String sreportingtoolURL = "";
					final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
							+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
							+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
							+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") "
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by nreportsettingcode";
					List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
					fileuploadpath = reportPaths.get(0);
					subReportPath = reportPaths.get(0);
					imagePath = reportPaths.get(0);
					pdfPath = reportPaths.get(1);
					sreportingtoolURL = reportPaths.get(2);
					if (inputMap.containsKey("ncontrolcode")) {
						ncontrolCode = (int) inputMap.get("ncontrolcode");
					}
					sJRXMLname = "SpecReport.jrxml";
					sfileName = "SpecReport_" + inputMap.get("nallottedspeccode");
					inputMap.put("ncontrolcode",
							(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
							? inputMap.get("ncontrolcode")
									: ncontrolCode);
					UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
					userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
					Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap,
							userInfoWithReportFormCode);
					String folderName = "";
					sJRXMLname = (String) dynamicReport.get("JRXMLname");
					folderName = (String) dynamicReport.get("folderName");
					fileuploadpath = fileuploadpath + folderName;
					sfilesharedfolder = fileuploadpath + sJRXMLname;
					File JRXMLFile = new File(sfilesharedfolder);
					if (sJRXMLname != null && !sJRXMLname.equals("")) {
						Map<String, Object> jasperParameter = new HashMap<>();
						jasperParameter.put("ssubreportpath", subReportPath + folderName);
						jasperParameter.put("simagepath", imagePath + folderName);
						jasperParameter.put("sreportingtoolURL", sreportingtoolURL);
						jasperParameter.put("language", userInfo.getSlanguagetypecode());
						jasperParameter.put("nretrievalcertificatecode",
								(int) inputMap.get("nretrievalcertificatecode"));
						jasperParameter.put("sprimarykeyname", (int) inputMap.get("nretrievalcertificatecode"));
						jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
						jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
						returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType,
								pdfPath, sfileName, userInfo, "", ncontrolCode, false, "", "", ""));
						String uploadStatus = (String) returnMap
								.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
						if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {
							returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							String auditAction = "";
							String comments = "";
							Map<String, Object> outputMap = new HashMap<>();
							outputMap.put("stablename", "retrievalcertificate");
							outputMap.put("sprimarykeyvalue", inputMap.get("nretrievalcertificatecode"));
							auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTVALUE",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTVALUE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTVALUE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
