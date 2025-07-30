package com.agaramtech.qualis.audittrail.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.audittrail.model.AuditAction;
import com.agaramtech.qualis.audittrail.model.AuditActionFilter;
import com.agaramtech.qualis.configuration.model.FTPSubFolder;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to perform CRUD Operation on "audittrail" table by
 * implementing methods from its interface.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 21- 04- 2025
 */
@Repository
public class AuditTrailDAOImpl implements AuditTrailDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditTrailDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;

	public AuditTrailDAOImpl(CommonFunction commonFunction, JdbcTemplate jdbcTemplate,
			ProjectDAOSupport projectDAOSupport, DateTimeUtilityFunction dateUtilityFunction) {
		super();
		this.commonFunction = commonFunction;
		this.jdbcTemplate = jdbcTemplate;
		this.projectDAOSupport = projectDAOSupport;
		this.dateUtilityFunction = dateUtilityFunction;
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> getAuditTrail(final UserInfo userInfo, final String currentUIDate) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String fromDate = "";
		String toDate = "";
		AuditAction selectedAuditTrail = null;
		final ObjectMapper objmapper = new ObjectMapper();
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
		String format = "";
		String mulitilingual1 = "";
		String mulitilingual2 = "";
		final List<String> lstMulitilingual = new ArrayList<>();
		final List<String> lstDateFiled = new ArrayList<>();
		final boolean checksitemethod = false;
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			lstDateFiled.add("sauditdate");
			lstDateFiled.add("viewPeriod");
			format = "a.sauditdate";
			mulitilingual1 = "sdisplayname";
			mulitilingual2 = "sdisplayname";
			lstMulitilingual.add("sformname");
			lstMulitilingual.add("smodulename");
			// checksitemethod = true;
		} else {
			format = "shour";
			mulitilingual1 = "sformname";
			mulitilingual2 = "smodulename";
		}

		// For_PostgreSQL
		String query = "select 0+Rank() over (order by max(dauditdate)) nauditcode ,CONCAT(syear,'-',smonth,'-',sday) "
				+ "  saudittraildate,CONCAT(sday,'-',TO_CHAR(max(dauditdate),'Month'),'-',syear) sauditdate"
				+ " from auditaction " + " where dauditdate between '" + fromDate + "' and '" + toDate + "'"
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by sday, smonth , syear order by nauditcode ;";

		final List<AuditAction> lstAuditList = jdbcTemplate.query(query, new AuditAction());

		if (!lstAuditList.isEmpty()) {
			fromDate = lstAuditList.get(lstAuditList.size() - 1).getSaudittraildate() + " 00:00:00";
		}

		query = " select nmodulecode ,nmenucode,COALESCE(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as smodulename,nsorter,nstatus from qualismodule where "
				+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter;";

		query = query + " select nformcode,nmenucode,nmodulecode, " + " COALESCE(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'sdisplayname'->>'en-US') as sformname, "
				+ " sclassname,surl,nsorter,nstatus from qualisforms where " + "nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter;";

		query = query + " select nusercode,ndeptcode,ncountrycode,ndesignationcode,nlockmode,"
				+ "nlogintypecode,ntransactionstatus,sloginid,sfirstname,slastname, CONCAT(sfirstname,' ',slastname) susername,"
				+ " saddress1,semail,sphoneno,sempid,saddress2,saddress3,sbloodgroup,squalification,"
				+ "ddateofjoin,sjobdescription,smobileno,nsitecode,nstatus from users " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nusercode>0;";

		query = query + " select * from userrole where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nuserrolecode>0;";

		query = query + " select nauditactionfiltercode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') sauditactionfiltername,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') sdisplayname,nsorter from auditactionfilter where "
				+ "nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " order by nsorter;";

		final String jsonquery = "select json_agg(x.jsondata) as jsondata from (select  "
				+ "  b.jsondata as jsondata   from auditaction a,auditcomments b "
				+ " where a.nauditcode = b.nauditcode and b.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
				+ userInfo.getNtranssitecode() + " and a.dauditdate between '" + fromDate + "' and '" + toDate
				+ "' order by dauditdate desc)x; ";

		final List<?> lstAudit = projectDAOSupport.getMultipleEntitiesResultSetInList(query, jdbcTemplate,
				QualisModule.class, QualisForms.class, Users.class, UserRole.class, AuditActionFilter.class);

		outputMap.put("AuditTrail", lstAuditList);
		outputMap.put("QualisModule", lstAudit.get(0));
		outputMap.put("QualisForms", lstAudit.get(1));
		outputMap.put("Users", lstAudit.get(2));
		outputMap.put("UserRole", lstAudit.get(3));
		final List<AuditActionFilter> auditActionFilter = (List<AuditActionFilter>) lstAudit.get(4);
		outputMap.put("ViewTypeAudit", auditActionFilter);
		outputMap.put("ViewType", auditActionFilter.get(0));

		List<?> listAuditDetails = objmapper.readValue(jdbcTemplate.queryForObject(jsonquery, String.class),
				new TypeReference<List<Map<String, Object>>>() {
				});
		listAuditDetails = getSiteLocalTimeFromUTC(listAuditDetails, lstDateFiled, null, userInfo, true,
				lstMulitilingual, 2, checksitemethod);

		outputMap.put("AuditDetails", listAuditDetails);

		if (lstAuditList.isEmpty()) {
			outputMap.put("SelectedAuditTrail", null);
			outputMap.put("AuditTrail", lstAuditList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			selectedAuditTrail = (lstAuditList.get(lstAuditList.size() - 1));
			outputMap.put("SelectedAuditTrail", selectedAuditTrail);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> getAuditTrailByDate(final UserInfo userInfo, String fromDate, String toDate,
			final int moduleCode, final int formCode, final int userCode, final int userRole, final int viewTypeCode,
			final String dauditDate) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		LOGGER.info("method Start :" + java.time.LocalTime.now());
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Date tempFromDate = new Date();
		Date tempToDate = new Date();
		String sqlFormat = "";
		fromDate = fromDate.replace("T", " ");
		toDate = toDate.replace("T", " ");
		final List<String> lstDateFiled = new ArrayList<>();
		final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (viewTypeCode == 2) {
			fromDate = dauditDate + " 00:00:00";
			toDate = dauditDate + " 23:59:00";
			sqlFormat = "shour";
		} else if (viewTypeCode == 3) {
			tempFromDate = sourceFormat.parse(dauditDate + "-01 00:00:00");
			tempToDate = sourceFormat.parse(dauditDate + "-31 23:59:00");
			if (sourceFormat.parse(fromDate).compareTo(tempFromDate) < 0) {
				fromDate = sourceFormat.format(tempFromDate);
			}
			if (sourceFormat.parse(toDate).compareTo(tempToDate) > 0) {
				toDate = sourceFormat.format(tempToDate);
			}
			sqlFormat = "CONCAT('DAY','-',sday)";
		} else {
			tempFromDate = sourceFormat.parse(dauditDate + "-01-01 00:00:00");
			tempToDate = sourceFormat.parse(dauditDate + "-12-31 23:59:00");
			if (sourceFormat.parse(fromDate).compareTo(tempFromDate) < 0) {
				fromDate = sourceFormat.format(tempFromDate);
			}
			if (sourceFormat.parse(toDate).compareTo(tempToDate) > 0) {
				toDate = sourceFormat.format(tempToDate);
			}

			sqlFormat = "CONCAT(smonth,'-',TO_CHAR(dauditdate,'Month'))";
		}

		String mulitilingual1 = "";
		String mulitilingual2 = "";
		final boolean checksitemethod = false;
		final List<String> lstMulitilingual = new ArrayList<>();
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			lstDateFiled.add("sauditdate");
			lstDateFiled.add("viewPeriod");
			sqlFormat = "a.sauditdate";
			mulitilingual1 = "sdisplayname";
			mulitilingual2 = "sdisplayname";
			lstMulitilingual.add("sformname");
			lstMulitilingual.add("smodulename");
			// checksitemethod = true;
		} else {
			mulitilingual1 = "sformname";
			mulitilingual2 = "smodulename";
		}

		String queryString = "";

		String queryjsonString = "";

		queryjsonString = " select  json_agg(x.jsondata)  from (select b.jsondata as jsondata, "
				+ " a.dauditdate daudittraildate from auditaction a, auditcomments b  where "
				+ " a.nauditcode = b.nauditcode and a.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
				+ userInfo.getNtranssitecode() + " and b.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.dauditdate between '" + fromDate
				+ "' and '" + toDate + "'";

		if (moduleCode > 0) {
			queryjsonString += " and a.nmodulecode = " + moduleCode;
		}
		if (formCode > 0) {
			queryjsonString += " and a.nformcode = " + formCode;
		}
		if (userCode > 0) {
			queryjsonString += " and a.nusercode = " + userCode;
		}
		if (userRole > 0) {
			queryjsonString += " and a.nuserrolecode = " + userRole;
		}
		queryjsonString += " order by daudittraildate desc  )x;";

		AuditAction selectedAuditTrail = null;

		if (viewTypeCode == 2) {
			// For_PostgreSQL
			queryString += "select CONCAT(a.sday,'-',TO_CHAR(max(a.dauditdate),'Month'),'-',a.syear) "
					+ " sauditdate, CONCAT(a.syear,'-',a.smonth,'-',a.sday)  saudittraildate ";

		} else if (viewTypeCode == 3) {

			// For_PostgreSQL
			queryString += "select   CONCAT(TO_CHAR(max(a.dauditdate),'Month'),'-',a.syear) sauditdate, "
					+ " CONCAT(a.syear,'-',a.smonth)   saudittraildate ";

		} else {
			queryString += "select  a.syear  sauditdate,a.syear saudittraildate ";
		}

		queryString += " from auditaction a where a.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
				+ userInfo.getNtranssitecode() + " and a.dauditdate  " + " between '" + fromDate + "' and '" + toDate
				+ "' ";
		// }
		if (moduleCode > 0) {
			queryString += " and a.nmodulecode = " + moduleCode;
		}
		if (formCode > 0) {
			queryString += " and a.nformcode = " + formCode;
		}
		if (userCode > 0) {
			queryString += " and a.nusercode = " + userCode;
		}
		if (userRole > 0) {
			queryString += " and a.nuserrolecode = " + userRole;
		}
		if (viewTypeCode == 2) {
			queryString += " group by a.sday,a.smonth , a.syear;";
		} else if (viewTypeCode == 3) {
			queryString += " group by a.smonth , a.syear;";
		} else {
			queryString += " group by a.syear ;";
		}

		final List<?> lstAuditData = projectDAOSupport.getMultipleEntitiesResultSetInList(queryString, jdbcTemplate,
				AuditAction.class);

		// List<AuditAction> auditActionList = (List<AuditAction>) lstAuditData.get(0);
		final List<?> auditActionList = objmapper.readValue(jdbcTemplate.queryForObject(queryjsonString, String.class),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final List<?> listAudit = getSiteLocalTimeFromUTC(auditActionList, lstDateFiled, null, userInfo, true,
				lstMulitilingual, viewTypeCode, checksitemethod);
		outputMap.put("AuditDetails", listAudit);

		final List<AuditAction> auditList = (List<AuditAction>) lstAuditData.get(0);

		selectedAuditTrail = (auditList.get(auditList.size() - 1));
		outputMap.put("SelectedAuditTrail", selectedAuditTrail);
		LOGGER.info("method end" + java.time.LocalTime.now());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unused")
	public ResponseEntity<Object> getFilterAuditTrailRecords(final UserInfo userInfo, String FromDate, String ToDate,
			final int ModuleCode, final int FormCode, final int UserCode, final int UserRole, final int ViewTypeCode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		String queryString = "";
		AuditAction selectedAuditTrail = null;
		Date tempFromDate = new Date();
		Date tempToDate = new Date();

		final String fromDate = FromDate;
		final String toDate = ToDate;

		final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		queryString = "";

		if (ViewTypeCode == 2) {
			// For_PostgreSQL
			queryString += "select CONCAT(a.sday,'-',TO_CHAR(max(a.dauditdate),'Month'),'-',a.syear) sauditdate,"
					+ " CONCAT(a.syear,'-',a.smonth,'-',a.sday)  saudittraildate,max(a.dauditdate) dauditdate  ";

			// For_MSSQL

		} else if (ViewTypeCode == 3) {
			// For_PostgreSQL
			queryString += "select CONCAT(TO_CHAR(max(a.dauditdate),'Month'),'-',a.syear) sauditdate,"
					+ "CONCAT(a.syear,'-',a.smonth)  saudittraildate,max(a.dauditdate) dauditdate  ";

		} else {
			queryString += "select  a.syear sauditdate,a.syear saudittraildate,max(a.dauditdate) dauditdate  ";
		}

		queryString += " from auditaction a where a.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
				+ userInfo.getNtranssitecode() + " and a.dauditdate  " + " between '" + FromDate + "' and '" + ToDate
				+ "' ";

		if (ModuleCode > 0) {
			queryString += " and a.nmodulecode = " + ModuleCode;
		}
		if (FormCode > 0) {
			queryString += " and a.nformcode = " + FormCode;
		}
		if (UserCode > 0) {
			queryString += " and a.nusercode = " + UserCode;
		}
		if (UserRole > 0) {
			queryString += " and a.nuserrolecode = " + UserRole;
		}
		if (ViewTypeCode == 2) {
			queryString += " group by a.sday, a.smonth , a.syear";
		} else if (ViewTypeCode == 3) {
			queryString += " group by a.smonth  , a.syear ";
		} else {
			queryString += " group by a.syear";
		}
		queryString += "   order by dauditdate  desc ";
		final List<AuditAction> auditActionList = jdbcTemplate.query(queryString, new AuditAction());

		if (ViewTypeCode != 1) {
			outputMap.put("AuditTrail", auditActionList);
		}
		if (auditActionList.size() == 0) {

			outputMap.put("AuditTrail", null);
			outputMap.put("SelectedAuditTrail", null);
			outputMap.put("AuditDetails", null);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			selectedAuditTrail = (auditActionList.get(0));
		}

		if (ViewTypeCode != 1) {
			outputMap.put("SelectedAuditTrail", selectedAuditTrail);
		}
		String sqlFormat = "";
		if (ViewTypeCode == 2) {
			FromDate = selectedAuditTrail.getSaudittraildate() + " 00:00:00";
			ToDate = selectedAuditTrail.getSaudittraildate() + " 23:59:00";
			sqlFormat = "shour";
		} else if (ViewTypeCode == 3) {
			tempFromDate = sourceFormat.parse(selectedAuditTrail.getSaudittraildate() + "-01 00:00:00");
			tempToDate = sourceFormat.parse(selectedAuditTrail.getSaudittraildate() + "-31 23:59:00");
			if (sourceFormat.parse(FromDate.replace("T", " ")).compareTo(tempFromDate) < 0) {
				FromDate = sourceFormat.format(tempFromDate);
			}
			if (sourceFormat.parse(ToDate.replace("T", " ").replace("'T'", " ")).compareTo(tempToDate) > 0) {
				ToDate = sourceFormat.format(tempToDate);
			}

			sqlFormat = "CONCAT('DAY','-',sday)";
		} else if (ViewTypeCode == 4) {
			tempFromDate = sourceFormat
					.parse(String.valueOf(selectedAuditTrail.getSaudittraildate()) + "-01-01 00:00:00");
			tempToDate = sourceFormat
					.parse(String.valueOf(selectedAuditTrail.getSaudittraildate()) + "-12-31 23:59:00");
			if (sourceFormat.parse(FromDate.replace("T", " ")).compareTo(tempFromDate) < 0) {
				FromDate = sourceFormat.format(tempFromDate);
			}
			if (sourceFormat.parse(ToDate.replace("T", " ")).compareTo(tempToDate) > 0) {
				ToDate = sourceFormat.format(tempToDate);
			}
			// For_MSSQL
			// sqlFormat = "smonth+'-'+DATENAME(month,dauditdate)";

			// For_PostgreSQL
			sqlFormat = "CONCAT(smonth,'-',TO_CHAR(dauditdate,'Month'))";
		}
		final List<String> lstDateField = new ArrayList<String>();
		String mulitilingual1 = "";
		String mulitilingual2 = "";
		final boolean checksitemethod = false;
		final List<String> lstMulitilingual = new ArrayList<>();
		if (userInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			lstDateField.add("sauditdate");
			lstDateField.add("viewPeriod");
			mulitilingual1 = "sdisplayname";
			mulitilingual2 = "sdisplayname";
			lstMulitilingual.add("sformname");
			lstMulitilingual.add("smodulename");
			sqlFormat = "a.sauditdate";
		} else {
			mulitilingual1 = "sformname";
			mulitilingual2 = "smodulename";
		}

		queryString = "select json_agg(x.jsondata)  from (select  " + "  b.jsondata||json_build_object('viewperiod',"
				+ sqlFormat + ")::jsonb as jsondata, "
				+ " a.dauditdate daudittraildate from auditaction a,auditcomments b "
				+ " where a.nauditcode = b.nauditcode and a.nsitecode=" + userInfo.getNtranssitecode()
				+ " and a.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.dauditdate" + " between '"
				+ FromDate + "' and '" + ToDate + "'";

		if (ModuleCode > 0) {
			queryString += " and a.nmodulecode = " + ModuleCode;
		}
		if (FormCode > 0) {
			queryString += " and a.nformcode = " + FormCode;
		}
		if (UserCode > 0) {
			queryString += " and a.nusercode = " + UserCode;
		}
		if (UserRole > 0) {
			queryString += " and a.nuserrolecode = " + UserRole;
		}
		queryString += " order by daudittraildate desc)x";
		// List<AuditAction> auditDetailsList = (List<AuditAction>)
		// jdbcTemplate.query(queryString, new AuditAction());
		final List<?> auditDetailsList = objmapper.readValue(jdbcTemplate.queryForObject(queryString, String.class),
				new TypeReference<List<Map<String, Object>>>() {
				});
		if (ViewTypeCode == 1) {

			outputMap.put("AuditTrail", null);
			outputMap.put("SelectedAuditTrail", null);
			outputMap.put("AuditDetails", auditDetailsList);
		} else {
			final List<?> listAudit1 = getSiteLocalTimeFromUTC(auditDetailsList, lstDateField, null, userInfo, true,
					lstMulitilingual, ViewTypeCode, checksitemethod);

			outputMap.put("AuditDetails", listAudit1);
		}
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		final String fromDateUI = LocalDateTime.parse(fromDate.replace("T", " "), dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse(toDate.replace("T", " "), dbPattern).format(uiPattern);

		outputMap.put("FromDate", fromDateUI);
		outputMap.put("ToDate", toDateUI);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFormNameByModule(final int ModuleCode) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String queryString = " select * from qualisforms where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmodulecode = " + ModuleCode
				+ " order by nsorter";

		final List<QualisForms> qualisFormsList = jdbcTemplate.query(queryString, new QualisForms());
		outputMap.put("QualisForms", qualisFormsList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<?> getSiteLocalTimeFromUTC(final List<?> lsts, final List<String> lstsearchField,
			final List<String> lstselectedtimeZone, final UserInfo userinfo, final boolean MultilingualNeed,
			final List<String> lstMultilingualField, final int viewTypeCode, final boolean checksitemethod)
			throws Exception {
		if (checksitemethod) {
			final ObjectMapper Objmapper = new ObjectMapper();
			Objmapper.registerModule(new JavaTimeModule());

			DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userinfo.getSdatetimeformat());
			final SimpleDateFormat sourceFormat = new SimpleDateFormat(
					userinfo.getSdatetimeformat().replace("'T'", " "));

			final List<Map<String, Object>> finalList = new ArrayList<>();
			ResourceBundle resourcebundle = null;
			if (MultilingualNeed) {
				resourcebundle = commonFunction.getResourceBundle(userinfo.getSlanguagefilename(), false);
			}
			final List<Map<String, Object>> lst = Objmapper.convertValue(lsts,
					new TypeReference<List<Map<String, Object>>>() {
					});

			for (int i = 0; i < lst.size(); i++) {
				final Map<String, Object> map = lst.get(i);

				for (int j = 0; j < lstsearchField.size(); j++) {
					// commented by pravinth
					if (lstsearchField.get(j) == "sauditdate") {
						destFormat = DateTimeFormatter.ofPattern(userinfo.getSdatetimeformat());
					} else {
						if (viewTypeCode == 2) {
							destFormat = DateTimeFormatter.ofPattern("HH:00 (h a)");
						} else if (viewTypeCode == 3) {
							destFormat = DateTimeFormatter.ofPattern("'Day'" + "-dd");
						} else if (viewTypeCode == 1) {
							destFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
						} else {
							destFormat = DateTimeFormatter.ofPattern("MM (MMMM)");
						}
					}

					if (!map.get(lstsearchField.get(j)).equals("") && (String) map.get(lstsearchField.get(j)) != null) {

						if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j)))
									.toInstant().atZone(ZoneId.of(userinfo.getStimezoneid())).toLocalDateTime();
							map.put(lstsearchField.get(j), destFormat.format(ldt));
						} else {
							final LocalDateTime ldt = sourceFormat.parse((String) map.get(lstsearchField.get(j)))
									.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
							map.put(lstsearchField.get(j), destFormat.format(ldt));
						}
					} else {
						map.put(lstsearchField.get(j), "");
					}
					if (MultilingualNeed) {
						for (int l = 0; l < lstMultilingualField.size(); l++) {
							map.replace(lstMultilingualField.get(l).trim(),
									resourcebundle.containsKey((String) map.get(lstMultilingualField.get(l).trim()))
											? resourcebundle
													.getString((String) map.get(lstMultilingualField.get(l).trim()))
											: (String) map.get(lstMultilingualField.get(l).trim()));
						}
					}
				}
				finalList.add(i, map);
			}
			return finalList;
		}
		return lsts;
	}

	@Override
	@SuppressWarnings("unused")
	public ResponseEntity<Object> getexportdata(final Map<String, Object> objmap) throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Map<Short, Object> settingMap = new HashMap<>();
		final Map<String, Object> csvcapturefields = objmapper.convertValue(objmap.get("dataField"),
				new TypeReference<Map<String, Object>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		try {

			String fromDate = (String) objmap.get("fromDate");
			String toDate = (String) objmap.get("toDate");
			fromDate = fromDate.replace("T", " ");
			toDate = toDate.replace("T", " ");
			final int nviewtypecode = (int) objmap.get("viewtypecode");
			final int nmodulecode = (int) objmap.get("modulecode");
			final int nformcode = (int) objmap.get("formcode");
			final int nusercode = (int) objmap.get("usercode");
			final int nuserrole = (int) objmap.get("userrole");
			final String auditDate = (String) objmap.get("sauditdate");
			final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (nviewtypecode == 2) {
				fromDate = auditDate + " 00:00:00.000";
				toDate = auditDate + " 23:59:00.000";
			} else if (nviewtypecode == 3) {
				final Date tempFromDate = sourceFormat.parse(auditDate + "-01 00:00:00.000");
				final Date tempToDate = sourceFormat.parse(auditDate + "-31 23:59:00.000");

				if (sourceFormat.parse(fromDate).compareTo(tempFromDate) < 0) {
					fromDate = sourceFormat.format(tempFromDate);
				}
				if (sourceFormat.parse(toDate).compareTo(tempToDate) > 0) {
					toDate = sourceFormat.format(tempToDate);
				}
			} else if (nviewtypecode == 4) {
				final Date tempFromDate = sourceFormat.parse(auditDate + "-01-01 00:00:00.000");
				final Date tempToDate = sourceFormat.parse(auditDate + "-12-31 23:59:00.000");
				if (sourceFormat.parse(fromDate).compareTo(tempFromDate) < 0) {
					fromDate = sourceFormat.format(tempFromDate);
				}
				if (sourceFormat.parse(toDate).compareTo(tempToDate) > 0) {
					toDate = sourceFormat.format(tempToDate);
				}
			}

			final String dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
 
			String queryString = "(select  a.sauditdate as \"" + csvcapturefields.get("sauditdate") + "\""
					+ ", a.sauditaction as \"" + csvcapturefields.get("sauditaction") + "\"  ,b.scomments as \""
					+ csvcapturefields.get("scomments") + "\"  ,CONCAT(c.sfirstname,c.slastname)  \""
					+ csvcapturefields.get("susername") + "\" ,d.suserrolename as \""
					+ csvcapturefields.get("suserrolename") + "\",a.sactiontype as \""
					+ csvcapturefields.get("sactiontype") + "\" ,e.sformname as \"" + csvcapturefields.get("sformname")
					+ "\",f.smodulename as \"" + csvcapturefields.get("smodulename") + "\" from "
					+ "auditaction a,auditcomments b,users c," + "userrole d,";

			// ALPD-3582
			String smodulecode = "";
			String sformcode = "";
			String susercode = "";
			String suserrole = "";
			if (nmodulecode > 0) {
				smodulecode += " and a.nmodulecode = " + nmodulecode + "";
			}
			if (nformcode > 0) {
				sformcode += " and a.nformcode = " + nformcode + "";
			}
			if (nusercode > 0) {
				susercode += " and a.nusercode = " + nusercode + "";
			}
			if (nuserrole > 0) {
				suserrole += " and a.nuserrolecode = " + nuserrole + "";
			}
			queryString += "qualisforms e,qualismodule f where a.nauditcode = b.nauditcode and a.nusercode = c.nusercode"
					+ " and a.nuserrolecode = d.nuserrolecode and a.nformcode = e.nformcode and a.nmodulecode = f.nmodulecode"
					+ " and a.dauditdate between '" + fromDate + "' and '" + toDate + "' and a.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
					+ userInfo.getNtranssitecode() + " and b.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode="
					+ userInfo.getNmastersitecode() + " and c.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode="
					+ userInfo.getNmastersitecode() + " and d.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and e.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and f.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + smodulecode + susercode + sformcode
					+ suserrole + ") ";

			final List<FTPSubFolder> lstForm = jdbcTemplate.query(
					" select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode=" + userInfo.getNformcode()
							+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
					new FTPSubFolder());

			final List<Settings> lstSettings = jdbcTemplate.query(
					" select COALESCE(ssettingvalue,'') ssettingvalue,nsettingcode from settings where nsettingcode in (54,55,10,11) order by nsettingcode;",
					new Settings());
			settingMap = lstSettings.stream()
					.collect(Collectors.toMap(Settings::getNsettingcode, Settings -> Settings.getSsettingvalue()));

			if (settingMap.size() > 0 && ((String) settingMap.get((short) 11)).trim().length() > 0) {

				final SimpleDateFormat DateFor = new SimpleDateFormat("YYY-MM-dd_hh-mm-ss");
				final String stringDate = DateFor.format(new Date());

				String filePath = ((String) settingMap.get((short) 54)).trim().isEmpty()
						? ((String) settingMap.get((short) 10)).trim()
						: ((String) settingMap.get((short) 54)).trim();
				filePath = filePath + "\\AuditTrail_" + stringDate + ".csv";

				final String path = (String) settingMap.get((short) 10);

				final File f = new File(path);

				if (!(f.exists())) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PATHISNOTAVAILABLE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				else {
					final String Query = "COPY " + queryString + " TO '" + filePath + "' DELIMITER ',' CSV HEADER;";
					jdbcTemplate.execute(Query);
					LOGGER.info("Main Query:" + Query);
					// ALPD-4080
					if (!((String) settingMap.get(settingMap.keySet().toArray()[0])).trim().isEmpty()) {
						FileOutputStream outputStream = null;
						try {

							// Define your SQL query
							final String sql = "SELECT pg_read_file('" + filePath + "') AS file_content";
							LOGGER.info("File Read Query:" + sql);

							final String fileContents = jdbcTemplate.queryForObject(sql, String.class);

							final byte[] bytes = fileContents.getBytes();
							final InputStream inputStream = new ByteArrayInputStream(bytes);

							final String newpath = ((String) settingMap.get((short) 10)).trim() + "\\AuditTrail_"
									+ stringDate + ".csv";
							outputStream = new FileOutputStream(newpath);
							final byte[] buffer = new byte[4096];
							int bytesRead;
							while ((bytesRead = inputStream.read(buffer)) != -1) {
								outputStream.write(buffer, 0, bytesRead);
							}
							// Close input stream and output stream
							inputStream.close();
							outputStream.close();

						} catch (final IOException es) {
							es.printStackTrace();
						}

					}

					outputMap.put("ExportExcel", "Success");
					outputMap.put("ExportExcelPath",
							((String) settingMap.get((short) 11)).trim() + "\\AuditTrail_" + stringDate + ".csv");
				}

			} else {
				outputMap.put("ExportFile", "Failure");
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} catch (final Exception e) {
			LOGGER.info(e.getMessage());
			outputMap.put("ExportExcel", "Failure");
			return new ResponseEntity<>(outputMap, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	public List<Map<String, Object>> getexportheadercolumn(final Map<String, Object> objmap) throws Exception {
		String query = "";
		int ncontrolcode = -1;
		int nregtypecode = -1;
		int nregsubtypecode = -1;
		int nauditFormcode = -1;
		if (objmap.containsKey("ncontrolcode")) {
			ncontrolcode = (int) objmap.get("ncontrolcode");
		}
		if (objmap.containsKey("nregtypecode")) {
			nregtypecode = (int) objmap.get("nregtypecode");
		}
		if (objmap.containsKey("nregsubtypecode")) {
			nregsubtypecode = (int) objmap.get("nregsubtypecode");
		}
		if (objmap.containsKey("nauditformcode")) {
			nauditFormcode = (int) objmap.get("nauditformcode");
		}

		query = "select * from exportconfiguration where nformcode = " + nauditFormcode + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		if (ncontrolcode > 0) {
			query += " and  ncontrolcode=" + ncontrolcode;
		}
		if (nregtypecode > 0) {
			query += " and  nregtypecode=" + nregtypecode;
		}
		if (nregsubtypecode > 0) {
			query += " and  nregsubtypecode=" + nregsubtypecode;
		}

		return jdbcTemplate.queryForList(query);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ResponseEntity<Object> getAuditTrailDetails(final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List lstcountcheck = jdbcTemplate.queryForList(" select * from auditmodifiedcomments where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nauditcode = "
				+ inputMap.get("nauditcode"));
		final boolean nflag = lstcountcheck.size() > 0 ? true : false;
		final String queryString = nflag
				? " select * from auditmodifiedcomments where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nauditcode = "
						+ inputMap.get("nauditcode") + " order by 1"

				: " select nauditcode,  jsonb_array_elements(jsoncomments->'keys')->>0  AS sfieldname  ,null as soldvalue, "
						+ " jsoncomments -> 'data'->>( jsonb_array_elements(jsoncomments->'keys')->>0 ) AS snewvalue "
						+ " FROM  auditcomments WHERE jsonb_typeof(jsoncomments) = 'object'  AND  nauditcode = "
						+ inputMap.get("nauditcode");

		outputMap.put("needOldValueColumn", nflag);
		outputMap.put("AuditModifiedComments", jdbcTemplate.queryForList(queryString));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}
