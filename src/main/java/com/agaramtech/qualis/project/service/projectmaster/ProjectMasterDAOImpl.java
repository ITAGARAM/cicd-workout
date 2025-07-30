package com.agaramtech.qualis.project.service.projectmaster;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectMasterFile;
import com.agaramtech.qualis.project.model.ProjectMasterHistory;
import com.agaramtech.qualis.project.model.ProjectMember;
import com.agaramtech.qualis.project.model.ProjectQuotation;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.model.ReportInfoProject;
import com.agaramtech.qualis.quotation.model.Quotation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ProjectMasterDAOImpl implements ProjectMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMasterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	public ResponseEntity<Map<String, Object>> getProjectMasterByName(final ProjectMaster projectmaster,
			final UserInfo userInfo, final int nflag) throws Exception {

		final Map<String, Object> map = new HashMap<String, Object>();
		final String strconcat = nflag == 1 ? " and nprojectmastercode <>" + projectmaster.getNprojectmastercode() + ""
				: "";
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		final String strtitleunique = "select count(nprojectmastercode) from projectmaster where sprojecttitle = N'"
				+ stringUtilityFunction.replaceQuote(projectmaster.getSprojecttitle()) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ userInfo.getNtranssitecode() + strconcat;
		LOGGER.info("getProjectMasterListByName() called");
		final int titleuniquecount = jdbcTemplate.queryForObject(strtitleunique, Integer.class);
		int titlecodecount = -1;
		if (projectmaster.getNautoprojectcode() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final String strcodeunique = "select count(nprojectmastercode) from projectmaster where sprojectcode = N'"
					+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectcode()) + "' and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNtranssitecode() + strconcat;
			titlecodecount = jdbcTemplate.queryForObject(strcodeunique, Integer.class);
		}

		final String projectnameunique = "select count(nprojectmastercode) from projectmaster where "
				+ " sprojectname=N'" + stringUtilityFunction.replaceQuote(projectmaster.getSprojectname())
				+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ userInfo.getNtranssitecode() + strconcat;
		final int projectnamecount = jdbcTemplate.queryForObject(projectnameunique, Integer.class);
		if (titleuniquecount > 0) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTTITLEALREADYEXISTS");
		} else if (titlecodecount > 0) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTCODEALREADYEXISTS");
		} else if (projectnamecount > 0) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTNAMEALREADYEXISTS");
		}
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createProjectMaster(final ProjectMaster projectmaster, final UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedProjectMasterList = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		map = getProjectMasterByName(projectmaster, userInfo, 0).getBody();
		final String strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();

		if (projectmaster.getDrfwdate() != null) {
			projectmaster.setSrfwdate(dateUtilityFunction.instantDateToString(projectmaster.getDrfwdate())
					.replace("T", " ").replace("Z", ""));
			lstDateField.add("srfwdate");
			lstDatecolumn.add("ntzrfwdate");
		}
		if (projectmaster.getDprojectstartdate() != null) {
			projectmaster.setSprojectstartdate(dateUtilityFunction
					.instantDateToString(projectmaster.getDprojectstartdate()).replace("T", " ").replace("Z", ""));
			lstDateField.add("sprojectstartdate");
			lstDatecolumn.add("ntzprojectstartdate");
		}

		String rfwDate;
		String expectedcompletionDate;

		if (projectmaster.getDrfwdate() == null) {
			rfwDate = null;
		} else {
			rfwDate = "'" + projectmaster.getSrfwdate() + "'";
		}

		if (projectmaster.getDexpectcompletiondate() == null) {
			expectedcompletionDate = null;
		} else {
			expectedcompletionDate = "'" + projectmaster.getDexpectcompletiondate() + "'";
		}

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ProjectMaster convertedObject = objmapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(
				projectmaster, lstDateField, lstDatecolumn, true, userInfo), new TypeReference<ProjectMaster>() {
		});
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == strstatus) {
			if (projectmaster.getDexpectcompletiondate() == null || convertedObject.getDexpectcompletiondate()
					.isBefore(convertedObject.getDprojectstartdate()) != true) {
				final String sQuery = " lock  table projectmaster "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);
				final List<ProjectType> activeprojecttype = getProjectTypeByprojectmaster(
						projectmaster.getNprojecttypecode());
				if (!activeprojecttype.isEmpty()) {
					final String getSeqNo = "select stablename,nsequenceno from seqnoprojectmanagement where stablename in (N'projectmaster',N'projectmasterhistory') and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					final List<ProjectMaster> lstSeqNo = jdbcTemplate.query(getSeqNo, new ProjectMaster());
					final Map<String, Integer> seqMap = lstSeqNo.stream()
							.collect(Collectors.toMap(ProjectMaster::getStablename, ProjectMaster::getNsequenceno));
					int nSeqNo = seqMap.get("projectmaster");
					int nprojectmasterhistorycode = seqMap.get("projectmasterhistory");
					nSeqNo++;
					nprojectmasterhistorycode++;
					String projectmasterInsert = "insert into projectmaster(nprojectmastercode,nprojecttypecode,sprojecttitle,sprojectcode,sprojectdescription,nuserrolecode,nusercode,"
							+ "nclientcatcode,nclientcode,srfwid,drfwdate,ntzrfwdate,noffsetdrfwdate, dprojectstartdate,ntzprojectstartdate,noffsetdprojectstartdate,dexpectcompletiondate,ntzexpectcompletiondate,noffsetdexpectcompletiondate,"
							+ "nprojectduration,nperiodcode,sfilename,ssystemfilename,nsitecode,dmodifieddate,nstatus,sprojectname)"
							+ "values(" + nSeqNo + "," + projectmaster.getNprojecttypecode() + ",N'"
							+ stringUtilityFunction.replaceQuote(projectmaster.getSprojecttitle()) + "',N'"
							+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectcode()) + "',N'"
							+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectdescription()) + "',"
							+ projectmaster.getNuserrolecode() + "," + projectmaster.getNusercode() + ","
							+ projectmaster.getNclientcatcode() + "," + projectmaster.getNclientcode() + ",N'"
							+ stringUtilityFunction.replaceQuote(projectmaster.getSrfwid()) + "'," + rfwDate + ", "
							+ convertedObject.getNtzrfwdate() + ", " + convertedObject.getNoffsetdrfwdate() + ",'"
							+ convertedObject.getSprojectstartdate() + "', " + convertedObject.getNtzprojectstartdate()
							+ ", " + convertedObject.getNoffsetdprojectstartdate() + "," + expectedcompletionDate + ","
							+ convertedObject.getNtzexpectcompletiondate() + ","
							+ convertedObject.getNoffsetdexpectcompletiondate() + ","
							+ projectmaster.getNprojectduration() + "," + projectmaster.getNperiodcode() + ",NULL,NULL,"
							+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",N'"
							+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectname()) + "');";

					final String historyQuery = insertProjectMasterHistory(nprojectmasterhistorycode, nSeqNo,
							dateUtilityFunction.getCurrentDateTime(userInfo), "-",
							dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()),
							userInfo.getNtimezonecode(), userInfo,
							Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

					projectmasterInsert = projectmasterInsert + historyQuery;
					jdbcTemplate.execute(projectmasterInsert);

					final String headerComments = "Samples";
					final String footerComments1 = "Note: The test results in this report relate only to the test sample(s) analysed.";
					final String footerComments2 = "The Manufacturer, Product Name, Description, Catalogue & Batch Numbers were provided by the client.";
					final String footerComments3 = "None";
					final String footerComments4 = "All testing was performed at SMTL premises.";
					final String samplingdetails = "All samples were selected and supplied by the client.";
					final String uncertainitymeasurement = "If any one test is not accredited need to display as NONE";
					final int nreporttemplatecode = -1;

					final String reportinfoproject = "insert into reportinfoproject(nprojectmastercode,nreporttemplatecode,sreporttemplateversion,srevision,srevisionauthor,sintroduction,stestproductheadercomments,"
							+ "stestproductfootercomments1,stestproductfootercomments2,stestproductfootercomments3,stestproductfootercomments4,ssamplingdetails,suncertainitymeasurement,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + nSeqNo + "," + nreporttemplatecode + ",N'None',N'None',N'None',N'None',N'"
							+ headerComments + "',N'" + footerComments1 + "',N'" + footerComments2 + "',N'"
							+ footerComments3 + "',N'" + footerComments4 + "',N'" + samplingdetails + "',N'"
							+ uncertainitymeasurement + "'," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(reportinfoproject);
					jdbcTemplate.execute("update seqnoprojectmanagement set nsequenceno = " + nSeqNo
							+ " where stablename='projectmaster' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "");
					projectmaster.setNprojectmastercode(nSeqNo);
					final String strnew = "select COALESCE(TO_CHAR(pm.drfwdate,'" + userInfo.getSsitedate()
					+ "'),'') as srfwdate, " + "COALESCE(TO_CHAR(pm.dprojectstartdate,'"
					+ userInfo.getSsitedate() + "'),'') as sprojectstartdate, "
					+ "COALESCE(TO_CHAR(pm.dexpectcompletiondate,'" + userInfo.getSsitedate()
					+ "'),'') as sexpectcompletiondate from projectmaster pm where pm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " pm.nprojectmastercode=" + nSeqNo + " and pm.nsitecode=" + userInfo.getNtranssitecode();
					final List<ProjectMaster> projectmasterlist = jdbcTemplate.query(strnew, new ProjectMaster());
					projectmaster.setSrfwdate(projectmasterlist.get(0).getSrfwdate());
					projectmaster.setSprojectstartdate(projectmasterlist.get(0).getSprojectstartdate());
					projectmaster.setSexpectcompletiondate(projectmasterlist.get(0).getSexpectcompletiondate());
					savedProjectMasterList.add(projectmaster);
					projectmaster
					.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
					multilingualIDList.add("IDS_ADDPROJECTMASTER");
					auditUtilityFunction.fnInsertAuditAction(savedProjectMasterList, 1, null, multilingualIDList,
							userInfo);
					return getProjectMasterByProjectType(projectmaster.getNprojecttypecode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STARTDATEBEFOREEXPECTDATE",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(strstatus, userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveProjectMasterById(final int nprojectmastercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final String strQuery = "select pm.sprojectname, pt.nprojecttypecode,pm.nprojectmastercode,pt.sprojecttypename,pm.sprojecttitle,pm.sprojectcode,case when (pm.sprojectdescription='' or pm.sprojectdescription=NULL) then '-' else pm.sprojectdescription end,CONCAT(us1.sfirstname,' ',us1.slastname) as susername,pm.nuserrolecode, "
				+ " to_char(pm.drfwdate,'" + userInfo.getSpgsitedatetime()
				+ "') as srfwdate,to_char(pm.dprojectstartdate,'" + userInfo.getSpgsitedatetime()
				+ "') as sprojectstartdate,to_char(pm.dexpectcompletiondate,'" + userInfo.getSpgsitedatetime()
				+ "') as sexpectcompletiondate,pm.nperiodcode,ur.suserrolename,cl.sclientname,cc.sclientcatname,pm.srfwid, "
				+ " pm.nprojectduration,p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname,pm.nsitecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "') sversionstatus,"
				+ " ph.ntransactionstatus,pm.nusercode,pm.nclientcode,pm.nclientcatcode,case when (pm.sfilename=NULL or pm.sfilename='') then '-' else pm.sfilename end,pm.drfwdate,pm.dprojectstartdate,pm.dexpectcompletiondate,"
				+ " CONCAT(pm.nprojectduration,' ',p.jsondata->'speriodname'->>'en-US') as sprojectduration,pm.noffsetdrfwdate,pm.noffsetdprojectstartdate,pm.noffsetdexpectcompletiondate,ph.noffsetdtransactiondate"
				+ " from projecttype pt, projectmaster pm, users us1,period p,transactionstatus ts,userrole ur,client cl,clientcategory cc,projectmasterhistory ph where "
				+ " ur.nuserrolecode=pm.nuserrolecode  and us1.nusercode=pm.nusercode "
				+ " and ph.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from"
				+ " projectmasterhistory ph where ph.nprojectmastercode=" + nprojectmastercode
				+ " group by nprojectmastercode) and "
				+ " pm.nperiodcode = p.nperiodcode and ph.ntransactionstatus=ts.ntranscode and cl.nclientcode=pm.nclientcode and cc.nclientcatcode=pm.nclientcatcode and  ph.nprojectmastercode=pm.nprojectmastercode and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and "
				+ " pm.nprojecttypecode = pt.nprojecttypecode and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  pm.nprojectmastercode="
				+ nprojectmastercode + " and pm.nsitecode=ph.nsitecode and ph.nsitecode=" + userInfo.getNtranssitecode()
				+ "" + " and pm.nsitecode=" + userInfo.getNtranssitecode() + " and pm.nsitecode="
				+ userInfo.getNtranssitecode() + "";
		final List<ProjectMaster> listProjectMaster = jdbcTemplate.query(strQuery, new ProjectMaster());
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<ProjectMaster> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(listProjectMaster,
						Arrays.asList("srfwdate", "sprojectstartdate", "sexpectcompletiondate",
								"sprojectcompletiondate", "sprojectretiredate", "sprojectclosuredate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ProjectMaster>>() {
				});

		if (lstUTCConvertedDate.size() > 0) {
			map.put("SelectedProjectMaster",
					dateUtilityFunction.getSiteLocalTimeFromUTC(listProjectMaster,
							Arrays.asList("srfwdate", "sprojectstartdate", "sexpectcompletiondate"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false).get(0));
		} else {
			map.put("SelectedProjectMaster", null);
		}
		final List<ProjectMember> lstProjectMember = getProjectMember(nprojectmastercode,
				userInfo).getBody();
		map.put("ProjectMember", lstProjectMember);
		final List<ProjectMasterFile> lstFile = getProjectMasterFile(nprojectmastercode,
				userInfo).getBody();
		map.put("projectmasterFile", lstFile);
		final List<ProjectMasterHistory> lstProjectMasterVersionHistory = getProjectMasterHistory(
				nprojectmastercode, userInfo).getBody();
		map.put("ProjectMasterHistory", lstProjectMasterVersionHistory);
		map.put("ProjectQuotation", getProjectQuotation(nprojectmastercode, userInfo));
		return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
	}

	@Override
	public List<ProjectType> getProjectTypeForProjectMaster(final UserInfo objUserInfo) throws Exception {
		final String sQuery = "select * from ProjectType where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nprojecttypecode <> -1 and nsitecode = " + objUserInfo.getNmastersitecode()
				+ " order by nprojecttypecode desc";
		return (jdbcTemplate.query(sQuery, new ProjectType()));
	}

	@Override
	public ResponseEntity<Object> getProjectMasterById(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		final ProjectMaster ProjectMaster = new ProjectMaster();
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(nprojectmastercode, objUserInfo).getBody());
		final int nprojecttypecode = ProjectMaster.getNprojecttypecode();
		final List<ProjectType> lstProjectType = getProjectTypeForProjectMaster(objUserInfo);
		map.put("filterProjectType", lstProjectType);
		if (lstProjectType != null && lstProjectType.size() > 0) {
			final List<ProjectType> SelectedProjectType = getProjectTypeByprojectmaster(nprojecttypecode);
			map.put("SelectedProjectType", SelectedProjectType.get(0));
			final List<ProjectMaster> lstProjectMaster = getProjectMaster(nprojecttypecode, objUserInfo);
			map.put("ProjectMaster", lstProjectMaster);
			map.put("SelectedProjectMaster", projectmasterResponseEntity.get("SelectedProjectMaster"));
			map.put("ProjectMember", projectmasterResponseEntity.get("ProjectMember"));
			map.put("projectmasterFile", projectmasterResponseEntity.get("projectmasterFile"));
			map.put("ProjectQuotation", projectmasterResponseEntity.get("ProjectQuotation"));
		} else {
			map.put("SelectedProjectType", null);
			map.put("ProjectMaster", null);
			map.put("SelectedProjectMaster", null);
			map.put("ProjectMember", null);
			map.put("projectmasterFile", null);
			map.put("ProjectQuotation", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	private List<ProjectType> getProjectTypeByprojectmaster(final int nprojecttypecode) throws Exception {

		final String strQuery = "Select * from projecttype where nprojecttypecode = " + nprojecttypecode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.query(strQuery, new ProjectType());
	}

	@Override
	public ResponseEntity<Object> getProjectMaster(final UserInfo objUserInfo) throws Exception {
		int nprojectmastercode = -1;
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final List<ProjectType> lstProjectType = getProjectTypeForProjectMaster(objUserInfo);
		map.put("filterProjectType", lstProjectType);
		if (lstProjectType != null && lstProjectType.size() > 0) {
			final int nprojecttypecode = lstProjectType.get(0).getNprojecttypecode();
			map.put("SelectedProjectType", lstProjectType.get(0));
			final List<ProjectMaster> lstProjectMaster = getProjectMaster(nprojecttypecode, objUserInfo);
			final ObjectMapper objMapper = new ObjectMapper();
			final List<ProjectMaster> lstUTCConvertedDate = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(lstProjectMaster,
							Arrays.asList("srfwdate", "sprojectstartdate", "sexpectcompletiondate"),
							Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
					new TypeReference<List<ProjectMaster>>() {
					});
			map.put("ProjectMaster", lstUTCConvertedDate);
			if (lstProjectMaster.size() > 0) {
				final ProjectMaster selectedProjectMaster = lstUTCConvertedDate.get(lstUTCConvertedDate.size() - 1);
				map.put("SelectedProjectMaster", selectedProjectMaster);
				nprojectmastercode = selectedProjectMaster.getNprojectmastercode();
				final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
						nprojectmastercode, objUserInfo).getBody();
				map.put("ProjectMasterHistory", lstProjectMasterHistory);
			} else {
				map.put("SelectedProjectMaster", null);
			}
			final List<ProjectMember> lstProjectMember = getProjectMember(nprojectmastercode,
					objUserInfo).getBody();
			map.put("ProjectMember", lstProjectMember);
			final List<ProjectMasterFile> lstProjectMasterFile = getProjectMasterFile(
					nprojectmastercode, objUserInfo).getBody();
			map.put("projectmasterFile", lstProjectMasterFile);
			map.put("ProjectQuotation", getProjectQuotation(nprojectmastercode, objUserInfo));
		} else {
			map.put("SelectedProjectMaster", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	private List<ProjectMaster> getProjectMaster(final int nprojecttypecode, final UserInfo objUserInfo) {

		final String strQuery = "select pm.sprojectname,pt.nprojecttypecode,pm.nprojectmastercode,pt.sprojecttypename,pm.sprojecttitle,pm.sprojectcode,case when (pm.sprojectdescription='' or pm.sprojectdescription=NULL) then '-' else pm.sprojectdescription end,CONCAT(us1.sfirstname,' ',us1.slastname) as susername,pm.nuserrolecode, "
				+ " to_char(pm.drfwdate,'" + objUserInfo.getSdatetimeformat()
				+ "') as srfwdate,to_char(pm.dprojectstartdate,'" + objUserInfo.getSdatetimeformat()
				+ "') as sprojectstartdate,to_char(pm.dexpectcompletiondate,'" + objUserInfo.getSdatetimeformat()
				+ "') as sexpectcompletiondate," + "pm.nperiodcode,ur.suserrolename,cl.sclientname,cc.sclientcatname, "
				+ "pm.nprojectduration,p.jsondata->'speriodname'->>'" + objUserInfo.getSlanguagetypecode()
				+ "' as speriodname,pm.nsitecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "') sversionstatus,case when (pm.srfwid='' or pm.srfwid=NULL) then '-' else pm.srfwid end,"
				+ "ph.ntransactionstatus,pm.nusercode,cl.nclientcode,cc.nclientcatcode,case when (pm.sfilename=NULL or pm.sfilename='') then '-' else pm.sfilename end,CONCAT(pm.nprojectduration,' ',p.jsondata->'speriodname'->>'en-US') as sprojectduration, pm.noffsetdrfwdate,"
				+ " pm.noffsetdprojectstartdate,pm.noffsetdexpectcompletiondate,ph.noffsetdtransactiondate"
				+ " from projecttype pt, projectmaster pm, users us1,period p,transactionstatus ts,userrole ur,client cl,clientcategory cc,projectmasterhistory ph where "
				+ "ur.nuserrolecode=pm.nuserrolecode  and us1.nusercode=pm.nusercode and "
				+ "pm.nperiodcode = p.nperiodcode and ph.ntransactionstatus=ts.ntranscode and ph.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from"
				+ "	 projectmasterhistory ph  group by nprojectmastercode) and cl.nclientcode=pm.nclientcode and cc.nclientcatcode=pm.nclientcatcode and ph.nprojectmastercode=pm.nprojectmastercode and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "pm.nprojecttypecode = pt.nprojecttypecode and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and	cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nprojecttypecode="
				+ nprojecttypecode + " and ph.nsitecode=pm.nsitecode  and pm.nsitecode="
				+ objUserInfo.getNtranssitecode() + "  and ph.nsitecode=" + objUserInfo.getNtranssitecode() + ""
				+ " order by nprojectmastercode asc";
		final ObjectMapper objMapper = new ObjectMapper();
		final List<ProjectMaster> listProjectMaster = jdbcTemplate.query(strQuery, new ProjectMaster());
		return objMapper.convertValue(listProjectMaster, new TypeReference<List<ProjectMaster>>() {
		});
	}

	@Override
	public ResponseEntity<Object> getProjectMasterByProjectType(final Integer nprojecttypecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		ProjectMaster selectedProjectMaster = null;
		final ObjectMapper objmapper = new ObjectMapper();
		final List<ProjectMaster> lstProjectMaster = getProjectMaster(nprojecttypecode, userInfo);
		final List<ProjectMaster> lstUTCConvertedDate = objmapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstProjectMaster,
						Arrays.asList("srfwdate", "sprojectstartdate", "sexpectcompletiondate",
								"sprojectcompletiondate", "sprojectretiredate", "sprojectclosuredate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ProjectMaster>>() {
				});
		map.put("ProjectMaster", lstUTCConvertedDate);

		if (!lstUTCConvertedDate.isEmpty()) {
			selectedProjectMaster = lstUTCConvertedDate.get(lstUTCConvertedDate.size() - 1);
			map.put("SelectedProjectMaster", selectedProjectMaster);
			map.put("SelectedProjectType", selectedProjectMaster);
			final List<ProjectMember> lstProjectMember = getProjectMember(
					selectedProjectMaster.getNprojectmastercode(), userInfo).getBody();
			map.put("ProjectMember", lstProjectMember);
			final List<ProjectMasterFile> lstProjectMasterFile = getProjectMasterFile(
					selectedProjectMaster.getNprojectmastercode(), userInfo).getBody();
			map.put("projectmasterFile", lstProjectMasterFile);
			final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
					selectedProjectMaster.getNprojectmastercode(), userInfo).getBody();
			map.put("ProjectMasterHistory", lstProjectMasterHistory);
			map.put("ProjectQuotation", getProjectQuotation(selectedProjectMaster.getNprojectmastercode(), userInfo));

		} else {
			final String sQuery = "select * from ProjectType where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode="
					+ nprojecttypecode + "";
			final List<ProjectType> selectedProjectType = jdbcTemplate.query(sQuery, new ProjectType());
			map.put("SelectedProjectType", selectedProjectType.get(0));
			map.put("SelectedProjectMaster", selectedProjectMaster);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStudyDirector(final UserInfo userInfo) throws Exception {
		final String strQuery = "select ur.nuserrolecode,CONCAT(us.sfirstname,' ',us.slastname) as susername,us.nusercode,umr.nusermultirolecode,ur.suserrolename "
				+ "from usermultirole umr, userrole ur, users us,userssite usite  where umr.nuserrolecode=ur.nuserrolecode "
				+ "and usite.nusercode=us.nusercode and umr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ntransactionstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ur.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umr.nusersitecode=usite.nusersitecode and usite.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ "and ur.nuserrolecode in (select nuserrolecode from userroleconfig where nneedprojectflow="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK); // status
	}

	@Override
	public ResponseEntity<Object> getTeammembers(final UserInfo userInfo) throws Exception {
		final String strQuery = "select u.nusercode,concat(u.sfirstname,' ',u.slastname) steammembername from users u where u.nusercode>0 "
				+ "and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode ="
				+ userInfo.getNtranssitecode() + " order by nusercode ";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK); // status
	}

	@Override
	public ResponseEntity<Object> getPeriodByControl(final Integer ncontrolCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select p.nperiodcode," + " coalesce(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + " p.jsondata->'speriodname'->>'en-US') as speriodname"
				+ " from period p,periodconfig pc where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and p.nperiodcode=pc.nperiodcode and pc.nformcode=" + userInfo.getNformcode()
				+ " and pc.ncontrolcode=" + ncontrolCode + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Period()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<? extends Object> updateProjectMaster(final ProjectMaster projectmaster,
			final UserInfo userInfo) throws Exception {
		final List<Object> beforeSavedProjectMasterList = new ArrayList<>();
		final List<Object> savedProjectMasterList = new ArrayList<>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		String query = "";
		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
		objMapper.registerModule(new JavaTimeModule());
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(projectmaster.getNprojectmastercode(), userInfo)
				.getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (projectmaster.getDrfwdate() != null) {
				projectmaster.setSrfwdate(dateUtilityFunction.instantDateToString(projectmaster.getDrfwdate())
						.replace("T", " ").replace("Z", ""));
				lstDateField.add("srfwdate");
				lstDatecolumn.add("ntzrfwdate");
			}
			if (projectmaster.getDprojectstartdate() != null) {
				projectmaster.setSprojectstartdate(dateUtilityFunction
						.instantDateToString(projectmaster.getDprojectstartdate()).replace("T", " ").replace("Z", ""));
				lstDateField.add("sprojectstartdate");
				lstDatecolumn.add("ntzprojectstartdate");
			}
			if (projectmaster.getDexpectcompletiondate() != null) {
				projectmaster.setSexpectcompletiondate(
						dateUtilityFunction.instantDateToString(projectmaster.getDexpectcompletiondate())
						.replace("T", " ").replace("Z", ""));
				lstDateField.add("sexpectcompletiondate");
				lstDatecolumn.add("ntzexpectcompletiondate");
			}
			//final ObjectMapper objmapper = new ObjectMapper();
			final ProjectMaster convertedObject = objMapper.convertValue(dateUtilityFunction
					.convertInputDateToUTCByZone(projectmaster, lstDateField, lstDatecolumn, true, userInfo),
					new TypeReference<ProjectMaster>() {
			});
			final Map<String, Object> objprojectmaster = objMapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				map = getProjectMasterByName(projectmaster, userInfo, 1).getBody();
				final String strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == strstatus) {
					if (projectmaster.getDexpectcompletiondate() == null || convertedObject.getDprojectstartdate()
							.isBefore(convertedObject.getDexpectcompletiondate())) {
						final List<ProjectType> activeprojecttype = getProjectTypeByprojectmaster(
								projectmaster.getNprojecttypecode());
						if (!activeprojecttype.isEmpty()) {
							final String strold = "select COALESCE(TO_CHAR(pm.drfwdate,'" + userInfo.getSsitedate()
							+ "'),'') as srfwdate, " + "COALESCE(TO_CHAR(pm.dprojectstartdate,'"
							+ userInfo.getSsitedate() + "'),'') as sprojectstartdate,"
							+ "COALESCE(TO_CHAR(pm.dexpectcompletiondate,'" + userInfo.getSsitedate()
							+ "'),'') as sexpectcompletiondate from projectmaster pm where pm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ " pm.nprojectmastercode=" + projectmaster.getNprojectmastercode() + "";
							final List<ProjectMaster> projectmasterlistold = jdbcTemplate.query(strold,
									new ProjectMaster());
							objprojectmaster.put("srfwdate", projectmasterlistold.get(0).getSrfwdate());
							objprojectmaster.put("sprojectstartdate",
									projectmasterlistold.get(0).getSprojectstartdate());
							objprojectmaster.put("sexpectcompletiondate",
									projectmasterlistold.get(0).getSexpectcompletiondate());
							String rfwDate;
							String expectedcompletionDate;
							if (projectmaster.getDrfwdate() == null) {
								rfwDate = null;
							} else {
								rfwDate = "'" + projectmaster.getSrfwdate() + "'";
							}
							if (projectmaster.getDrfwdate() == null) {
								rfwDate = null;
							} else {
								rfwDate = "'" + projectmaster.getSrfwdate() + "'";
							}
							if (projectmaster.getDexpectcompletiondate() == null) {
								expectedcompletionDate = null;
							} else {
								expectedcompletionDate = "'" + projectmaster.getDexpectcompletiondate() + "'";
							}
							query = "update projectmaster set " + "nprojecttypecode="
									+ projectmaster.getNprojecttypecode() + ",sprojecttitle='"
									+ stringUtilityFunction.replaceQuote(projectmaster.getSprojecttitle())
									+ "',sprojectcode='"
									+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectcode()) + "', "
									+ "sprojectdescription='"
									+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectdescription())
									+ "',nuserrolecode=" + projectmaster.getNuserrolecode() + ",nusercode="
									+ projectmaster.getNusercode() + ",nclientcatcode= "
									+ projectmaster.getNclientcatcode() + ",srfwid='"
									+ stringUtilityFunction.replaceQuote(projectmaster.getSrfwid())
									+ "' ,  nclientcode=" + projectmaster.getNclientcode() + "," + "drfwdate=" + rfwDate
									+ ",dprojectstartdate='" + convertedObject.getSprojectstartdate()
									+ "',dexpectcompletiondate=" + expectedcompletionDate + "," + "nprojectduration="
									+ projectmaster.getNprojectduration() + ", " + "nperiodcode="
									+ projectmaster.getNperiodcode() + ",dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',sprojectname='"
									+ stringUtilityFunction.replaceQuote(projectmaster.getSprojectname()) + ","
									+ "'  where nprojectmastercode=" + projectmaster.getNprojectmastercode() + " ";
							jdbcTemplate.execute(query);
							final String strnew = "select COALESCE(TO_CHAR(pm.drfwdate,'" + userInfo.getSsitedate()
							+ "'),'') as srfwdate, " + "COALESCE(TO_CHAR(pm.dprojectstartdate,'"
							+ userInfo.getSsitedate() + "'),'') as sprojectstartdate, "
							+ "COALESCE(TO_CHAR(pm.dexpectcompletiondate,'" + userInfo.getSsitedate()
							+ "'),'') as sexpectcompletiondate from projectmaster pm where pm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ "pm.nprojectmastercode=" + projectmaster.getNprojectmastercode()
							+ " and pm.nsitecode=" + userInfo.getNtranssitecode() + "";
							final List<ProjectMaster> projectmasterlistnew = jdbcTemplate.query(strnew,
									new ProjectMaster());
							projectmaster.setSrfwdate(projectmasterlistnew.get(0).getSrfwdate());
							projectmaster.setSprojectstartdate(projectmasterlistnew.get(0).getSprojectstartdate());
							projectmaster
							.setSexpectcompletiondate(projectmasterlistnew.get(0).getSexpectcompletiondate());
							savedProjectMasterList.add(projectmaster);
							objMapper.registerModule(new JavaTimeModule());
							final ProjectMaster objprojectmaster1 = objMapper.convertValue(objprojectmaster,
									ProjectMaster.class);
							beforeSavedProjectMasterList.add(objprojectmaster1);
							auditUtilityFunction.fnInsertAuditAction(savedProjectMasterList, 2,
									beforeSavedProjectMasterList, Arrays.asList("IDS_EDITPROJECTMASTER"), userInfo);
							return getActiveProjectMasterById(projectmaster.getNprojectmastercode(), userInfo);
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								"IDS_STARTDATEBEFOREEXPECTDATE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDAPPROVEDVERSIONPROJECT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteProjectMaster(final ProjectMaster projectmaster, final UserInfo userInfo)
			throws Exception {
		final List<Object> deletedProjectMasterList = new ArrayList<>();
		final List<Object> projectMemberById = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(projectmaster.getNprojectmastercode(), userInfo)
				.getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objprojectmaster = objMapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				final String str = "select COALESCE(TO_CHAR(pm.drfwdate,'" + userInfo.getSsitedate()
				+ "'),'') as srfwdate, " + "COALESCE(TO_CHAR(pm.dprojectstartdate,'" + userInfo.getSsitedate()
				+ "'),'') as sprojectstartdate," + "COALESCE(TO_CHAR(pm.dexpectcompletiondate,'"
				+ userInfo.getSsitedate()
				+ "'),'') as sexpectcompletiondate from projectmaster pm where pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "pm.nprojectmastercode=" + projectmaster.getNprojectmastercode() + "  and pm.nsitecode="
				+ userInfo.getNtranssitecode() + "";
				final List<ProjectMaster> projectmasterlist = jdbcTemplate.query(str, new ProjectMaster());
				objprojectmaster.put("srfwdate", projectmasterlist.get(0).getSrfwdate());
				objprojectmaster.put("sprojectstartdate", projectmasterlist.get(0).getSprojectstartdate());
				objprojectmaster.put("sexpectcompletiondate", projectmasterlist.get(0).getSexpectcompletiondate());
				String deleteProjectMasterQuery = "update ProjectMaster set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + ";";
				deleteProjectMasterQuery = deleteProjectMasterQuery + "update projectmasterhistory set  " + "nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + "; ";
				deleteProjectMasterQuery += "update reportinfoproject set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + "; ";
				jdbcTemplate.execute(deleteProjectMasterQuery);
				final List<Object> projectmasterFileById = new ArrayList<>();
				projectmasterFileById.addAll(
						getProjectMasterFile(projectmaster.getNprojectmastercode(), userInfo)
						.getBody());
				final List<Object> projectmasterQuotationById = new ArrayList<>();
				projectmasterQuotationById.addAll(getProjectQuotation(projectmaster.getNprojectmastercode(), userInfo));
				final String deleteprojectmasterfileQuery = "update projectmasterfile set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + "";
				jdbcTemplate.execute(deleteprojectmasterfileQuery);
				projectMemberById
				.addAll(getProjectMember(projectmaster.getNprojectmastercode(), userInfo)
						.getBody());
				final String deleteprojectmemberQuery = "update projectmember set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + "";
				jdbcTemplate.execute(deleteprojectmemberQuery);
				final String deleteProjectQuotationQuery = "update projectquotation set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nprojectmastercode = "
						+ projectmaster.getNprojectmastercode() + "";
				jdbcTemplate.execute(deleteProjectQuotationQuery);
				objMapper.registerModule(new JavaTimeModule());
				final ProjectMaster objprojectmaster1 = objMapper.convertValue(objprojectmaster, ProjectMaster.class);
				deletedProjectMasterList.add(objprojectmaster1);
				final List<Object> lstAllObject = new ArrayList<Object>();
				lstAllObject.add(deletedProjectMasterList);
				lstAllObject.add(projectMemberById);
				lstAllObject.add(projectmasterFileById);
				lstAllObject.add(projectmasterQuotationById);
				auditUtilityFunction.fnInsertListAuditAction(lstAllObject, 1, null,
						Arrays.asList("IDS_DELETEPROJECTMASTER", "IDS_DELETEPROJECTMEMBER", "IDS_DELETEPROJECTFILE",
								"IDS_DELETEPROJECTQUOTATION"),
						userInfo);
				return getProjectMasterByProjectType(projectmaster.getNprojecttypecode(), userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTPROJECTMASTERVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> approveProjectMaster(final ProjectMaster approveProjectMaster,
			final UserInfo userInfo, final int nautoprojectcode) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		String updateQuery = "";
		final List<Object> auditList = new ArrayList<Object>();
		final List<String> multilingualIDList = new ArrayList<String>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(approveProjectMaster.getNprojectmastercode(), userInfo)
				.getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objprojectmaster = objmapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				if (nautoprojectcode != 4) {
					final String strformat = projectDAOSupport.getSeqfnFormat("projectmaster",
							"seqnoformatgeneratorprojectmaster", 0, 0, userInfo);
					updateQuery = "update projectmaster set sprojectcode='" + strformat + "' where nprojectmastercode="
							+ approveProjectMaster.getNprojectmastercode() + "; ";
					approveProjectMaster.setSprojectcode(strformat);
				}
				final int nprojectmastercode = approveProjectMaster.getNprojectmastercode();
				int nprojectmasterhistorycode = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoprojectmanagement where stablename='projectmasterhistory'",
						Integer.class);
				nprojectmasterhistorycode++;
				final String historyQuery = insertProjectMasterHistory(nprojectmasterhistorycode,
						approveProjectMaster.getNprojectmastercode(), dateUtilityFunction.getCurrentDateTime(userInfo),
						"-", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()),
						userInfo.getNtimezonecode(), userInfo,
						Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
				updateQuery = updateQuery + historyQuery;
				jdbcTemplate.execute(updateQuery);
				approveProjectMaster
				.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
				approveProjectMaster.setSfilename("");
				if (approveProjectMaster.getDprojectstartdate() != null) {
					final Date date = Date.from(approveProjectMaster.getDprojectstartdate());
					approveProjectMaster
					.setSprojectstartdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (approveProjectMaster.getDexpectcompletiondate() != null) {
					final Date date = Date.from(approveProjectMaster.getDexpectcompletiondate());
					approveProjectMaster
					.setSexpectcompletiondate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				if (approveProjectMaster.getDrfwdate() != null) {
					final Date date = Date.from(approveProjectMaster.getDrfwdate());
					approveProjectMaster.setSrfwdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				auditList.add(approveProjectMaster);
				multilingualIDList.add("IDS_APPROVEPROJECTMASTER");
				auditUtilityFunction.fnInsertAuditAction(auditList, 1, null, multilingualIDList, userInfo);
				final Map<String, Object> selectedprojectmaster = new LinkedHashMap<String, Object>();
				selectedprojectmaster.putAll(
						getActiveProjectMasterById(nprojectmastercode, userInfo).getBody());
				map.put("SelectedProjectMaster", selectedprojectmaster.get("SelectedProjectMaster"));
				final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
						nprojectmastercode, userInfo).getBody();
				map.put("ProjectMasterHistory", lstProjectMasterHistory);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTPROJECTMASTERVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getProjectUnmappedTeammember(final Integer nprojectmastercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(nprojectmastercode, userInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String strQuery = "select u.nusercode,concat(u.sfirstname,' ',u.slastname) steammembername from users u,userssite us,site s where us.nsitecode=s.nsitecode "
					+ "and us.nusercode=u.nusercode  and us.nsitecode=" + userInfo.getNtranssitecode()
					+ " and u.nusercode not in " + "(select nusercode from projectmember where nprojectmastercode="
					+ nprojectmastercode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") and u.nusercode>0" + "  and us.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.ntransactionstatus !="
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " " + " order by u.nusercode ";
			return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK); // status
		}
	}

	@Override
	public ResponseEntity<Object> createProjectMember(final List<ProjectMember> ProjectMember, final UserInfo userInfo)
			throws Exception {
		final List<Object> beforeScreenRightsList = new ArrayList<>();
		final List<Object> savedProjectMemberList = new ArrayList<>();
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(ProjectMember.get(0).getNprojectmastercode(), userInfo)
				.getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<ProjectMember> lstProjectMember = getMembersByprojectmaster(
					ProjectMember.get(0).getNprojectmastercode());
			final List<ProjectMember> filteredList = ProjectMember.stream()
					.filter(source -> lstProjectMember.stream()
							.noneMatch(check -> source.getNusercode() == check.getNusercode()))
					.collect(Collectors.toList());
			final String categorycodevalue = filteredList.stream().map(x -> String.valueOf(x.getNusercode()))
					.collect(Collectors.joining(","));
			if (filteredList != null && filteredList.size() > 0) {
				int nSeqNo = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoprojectmanagement where stablename='projectmember'",
						Integer.class);
				final String projectmemberInsert = "insert into projectmember(nprojectmembercode,nprojectmastercode,nusercode,nsitecode,dmodifieddate,nstatus)"
						+ "(select " + nSeqNo + " +rank()over(order by nusercode)as nprojectmembercode,"
						+ ProjectMember.get(0).getNprojectmastercode() + ",nusercode as nusercode,"
						+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from  users where nusercode in(" + categorycodevalue + "))";
				jdbcTemplate.execute(projectmemberInsert);
				final Integer sequnenceno = nSeqNo + filteredList.size();
				jdbcTemplate.execute("update seqnoprojectmanagement set nsequenceno = " + sequnenceno
						+ " where stablename='projectmember'");
				for (final ProjectMember a : ProjectMember) {
					a.setNprojectmembercode(nSeqNo);
					nSeqNo++;
				}
				savedProjectMemberList.add(ProjectMember);
				auditUtilityFunction.fnInsertListAuditAction(savedProjectMemberList, 1, beforeScreenRightsList,
						Arrays.asList("IDS_ADDPROJECTMEMBER"), userInfo);
				final List<ProjectMember> lst = getProjectMember(
						ProjectMember.get(0).getNprojectmastercode(), userInfo).getBody();
				map.put("ProjectMember", lst);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTMEMBERISNOTAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	private List<ProjectMember> getMembersByprojectmaster(final int nprojectmastercode) throws Exception {
		final String strQuery = "Select * from projectmember where nprojectmastercode = " + nprojectmastercode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.query(strQuery, new ProjectMember());
	}

	@Override
	public ResponseEntity<Object> deleteProjectMember(final ProjectMember projectMember, final int nprojectmembercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "select pt.nprojecttypecode,pm.nprojectmastercode,pt.sprojecttypename,pm.sprojecttitle,pm.sprojectcode,case when (pm.sprojectdescription='' or pm.sprojectdescription=NULL) then '-' else pm.sprojectdescription end,CONCAT(us1.sfirstname,' ',us1.slastname) as susername,pm.nuserrolecode, "
				+ " to_char(pm.drfwdate,'" + userInfo.getSsitedate() + "') as srfwdate,to_char(pm.dprojectstartdate,'"
				+ userInfo.getSsitedate() + "') as sprojectstartdate,pm.nperiodcode,ur.suserrolename, "
				+ "pm.nprojectduration,p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname,pm.nsitecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "') sversionstatus,"
				+ " pm.nusercode,case when pm.sfilename is null then '-' else pm.sfilename end,CONCAT(pm.nprojectduration,' ',p.jsondata->'speriodname'->>'en-US') as sprojectduration "
				+ " from projecttype pt, projectmaster pm,projectmasterhistory ph, users us1,period p,transactionstatus ts,userrole ur where "
				+ " ur.nuserrolecode=pm.nuserrolecode  and us1.nusercode=pm.nusercode and ph.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
				+ " projectmasterhistory ph where ph.nprojectmastercode=" + projectMember.getNprojectmastercode() + ""
				+ " group by nprojectmastercode) and "
				+ " pm.nperiodcode = p.nperiodcode and ph.ntransactionstatus=ts.ntranscode  and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " pm.nprojecttypecode = pt.nprojecttypecode and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and us1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and pm.nprojectmastercode="
				+ projectMember.getNprojectmastercode() + " and pm.nsitecode=" + userInfo.getNtranssitecode() + " "
				+ " and pm.nsitecode=" + userInfo.getNtranssitecode() + " ";
		final List<ProjectMaster> listProjectMaster = jdbcTemplate.query(strQuery, new ProjectMaster());
		if (listProjectMaster.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<String>();
			final List<Object> savedList = new ArrayList<>();
			String sQuery = "select * from projectmember where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojectmembercode = "
					+ nprojectmembercode;
			final ProjectMember testPriceById = (ProjectMember) jdbcUtilityFunction.queryForObject(sQuery,
					ProjectMember.class, jdbcTemplate);
			if (testPriceById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTMEMBERSALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				sQuery = "update projectmember set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nprojectmembercode = "
						+ nprojectmembercode;
				jdbcTemplate.execute(sQuery);
				projectMember.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedList.add(projectMember);
				multilingualIDList.add("IDS_DELETEPROJECTMEMBER");
				auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
				final List<ProjectMember> lstProjectMember = getProjectMember(
						projectMember.getNprojectmastercode(), userInfo).getBody();
				outputMap.put("ProjectMember", lstProjectMember);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		}
	}

	@Override
	public ResponseEntity<List<ProjectMember>> getProjectMember(final int nprojectmastercode,
			final UserInfo objUserInfo) throws Exception {
		final String projectmemberQuery = " select pm.*,us.sloginid,concat(us.sfirstname,' ',us.slastname) as steammembername "
				+ " from projectmember pm, users us where pm.nusercode = us.nusercode and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and us.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojectmastercode="
				+ nprojectmastercode + " and pm.nsitecode= " + objUserInfo.getNtranssitecode() + "";
		return new ResponseEntity<List<ProjectMember>>(jdbcTemplate.query(projectmemberQuery, new ProjectMember()),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> retireProjectMaster(final ProjectMaster retireProjectMaster, final UserInfo userInfo)
			throws Exception {
		final List<Object> auditList = new ArrayList<Object>();
		final List<String> multilingualIDList = new ArrayList<String>();
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(retireProjectMaster.getNprojectmastercode(), userInfo)
				.getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objprojectmaster = objMapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				final List<String> lstDateField = new ArrayList<String>();
				final List<String> lstDatecolumn = new ArrayList<String>();
				if (retireProjectMaster.getDtransactiondate() != null) {
					retireProjectMaster.setSprojectclosuredate(
							dateUtilityFunction.instantDateToString(retireProjectMaster.getDtransactiondate())
							.replace("T", " ").replace("Z", ""));
					lstDateField.add("sprojectretiredate");
					lstDatecolumn.add("ntzprojectretiredate");
				}
				Instant dDate = getProjectStatusDate(retireProjectMaster.getNprojectmastercode(),
						Enumeration.TransactionStatus.APPROVED.gettransactionstatus(), userInfo);
				dDate = dDate.truncatedTo(ChronoUnit.DAYS);
				if (retireProjectMaster.getDtransactiondate().isBefore(dDate) != true) {
					final ProjectMaster convertedObject = objMapper
							.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(retireProjectMaster,
									lstDateField, lstDatecolumn, true, userInfo), new TypeReference<ProjectMaster>() {
							});
					String updateQuery = "";
					int nprojectmasterhistorycode = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnoprojectmanagement where stablename='projectmasterhistory'",
							Integer.class);
					nprojectmasterhistorycode++;
					final String historyQuery = insertProjectMasterHistory(nprojectmasterhistorycode,
							retireProjectMaster.getNprojectmastercode(), retireProjectMaster.getDtransactiondate(),
							retireProjectMaster.getSretiredremarks(), convertedObject.getNoffsetdtransactiondate(),
							convertedObject.getNtransdatetimezonecode(), userInfo,
							Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
					updateQuery = updateQuery + historyQuery;
					jdbcTemplate.execute(updateQuery);
					if (retireProjectMaster.getDprojectstartdate() != null) {
						final Date date = Date.from(retireProjectMaster.getDprojectstartdate());
						retireProjectMaster
						.setSprojectstartdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (retireProjectMaster.getDexpectcompletiondate() != null) {
						final Date date = Date.from(retireProjectMaster.getDexpectcompletiondate());
						retireProjectMaster
						.setSexpectcompletiondate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (retireProjectMaster.getDtransactiondate() != null) {
						final Date date = Date.from(retireProjectMaster.getDtransactiondate());
						retireProjectMaster
						.setSprojectretiredate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (retireProjectMaster.getDrfwdate() != null) {
						final Date date = Date.from(retireProjectMaster.getDrfwdate());
						retireProjectMaster.setSrfwdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					auditList.add(retireProjectMaster);
					multilingualIDList.add("IDS_RETIREPROJECTMASTER");
					auditUtilityFunction.fnInsertAuditAction(auditList, 1, null, multilingualIDList, userInfo);
					final Map<String, Object> selectedprojectmaster = new LinkedHashMap<>();
					selectedprojectmaster.putAll(getActiveProjectMasterById(
							retireProjectMaster.getNprojectmastercode(), userInfo).getBody());
					map.put("SelectedProjectMaster", selectedprojectmaster.get("SelectedProjectMaster"));
					final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
							retireProjectMaster.getNprojectmastercode(), userInfo).getBody();
					map.put("ProjectMasterHistory", lstProjectMasterHistory);
					return new ResponseEntity<Object>(map, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_RETIREDATESHOULBEGEREATERAPPROVEDATE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.COMPLETED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYCOMPLETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYRETIRE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> createProjectMasterFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		final List<ProjectMasterFile> lstProjectMasterFile = objMapper
				.readValue(request.getParameter("projectmasterfile"), new TypeReference<List<ProjectMasterFile>>() {
				});
		final ProjectMasterFile objProjectMasterFile = lstProjectMasterFile.get(0);
		if (lstProjectMasterFile != null && lstProjectMasterFile.size() > 0) {
			projectmasterResponseEntity.putAll(getActiveProjectMasterById(
					lstProjectMasterFile.get(0).getNprojectmastercode(), objUserInfo).getBody());
			if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final Map<String, Object> objprojectmaster = objMapper.convertValue(
						projectmasterResponseEntity.get("SelectedProjectMaster"),
						new TypeReference<Map<String, Object>>() {
						});
				if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {

					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					if (lstProjectMasterFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
							.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name -
						// master
					}

					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
						final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
								.truncatedTo(ChronoUnit.SECONDS);
						final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
						final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
						lstProjectMasterFile.forEach(objtf -> {
							objtf.setDcreateddate(instantDate);
							if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								objtf.setDcreateddate(instantDate);
								objtf.setNoffsetdcreateddate(noffset);
								objtf.setScreateddate(sattachmentDate.replace("T", " "));
							}
						});
						final String sequencequery = "select nsequenceno from seqnoprojectmanagement where stablename ='projectmasterfile'";
						int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						nsequenceno++;
						final String insertquery = "Insert into projectmasterfile(nprojectmasterfilecode,nprojectmastercode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
								+ "values (" + nsequenceno + "," + objProjectMasterFile.getNprojectmastercode() + ","
								+ objProjectMasterFile.getNlinkcode() + ","
								+ objProjectMasterFile.getNattachmenttypecode() + "," + " N'"
								+ stringUtilityFunction.replaceQuote(objProjectMasterFile.getSfilename()) + "',N'"
								+ stringUtilityFunction.replaceQuote(objProjectMasterFile.getSdescription()) + "',"
								+ objProjectMasterFile.getNfilesize() + "," + " '"
								+ objProjectMasterFile.getDcreateddate() + "',"
								+ objProjectMasterFile.getNoffsetdcreateddate() + "," + objUserInfo.getNtimezonecode()
								+ ",N'" + objProjectMasterFile.getSsystemfilename() + "','"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
								+ objUserInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
						jdbcTemplate.execute(insertquery);
						final String updatequery = "update seqnoprojectmanagement set nsequenceno =" + nsequenceno
								+ " where stablename ='projectmasterfile'";
						jdbcTemplate.execute(updatequery);
						final List<String> multilingualIDList = new ArrayList<>();
						multilingualIDList.add(
								lstProjectMasterFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype() ? "IDS_ADDPROJECTFILE" : "IDS_ADDPROJECTLINK");
						objProjectMasterFile.setNprojectmasterfilecode(nsequenceno);
						auditUtilityFunction.fnInsertAuditAction(lstProjectMasterFile, 1, null, multilingualIDList,
								objUserInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString,
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

					final List<ProjectMasterFile> lstFile = getProjectMasterFile(
							lstProjectMasterFile.get(0).getNprojectmastercode(), objUserInfo).getBody();
					outputMap.put("projectmasterFile", lstFile);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTDRAFTPROJECTMASTERVERSION",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<List<ProjectMasterFile>> getProjectMasterFile(final int nprojectmastercode,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String query = "select pf.noffsetdcreateddate,pf.nprojectmasterfilecode,(select  count(nprojectmasterfilecode) from projectmasterfile where nprojectmasterfilecode>0 and nprojectmastercode = "
				+ nprojectmastercode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,pf.sdescription,"
				+ " pf.nprojectmasterfilecode as nprimarycode,pf.sfilename,pf.nprojectmastercode,pf.ssystemfilename,"
				+ " pf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when pf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, pf.nfilesize," + " case when pf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(pf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " pf.nlinkcode, case when pf.nlinkcode = -1 then pf.nfilesize::varchar(1000) else '-' end sfilesize, pf.noffsetdcreateddate "
				+ " from projectmasterfile pf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = pf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = pf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pf.nprojectmastercode="
				+ nprojectmastercode + " order by pf.nprojectmasterfilecode;";
		final List<ProjectMasterFile> listProjectMasterFile = jdbcTemplate.query(query, new ProjectMasterFile());
		final List<ProjectMasterFile> listProjectMasterFile1 = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(listProjectMasterFile, Arrays.asList("screateddate"),
						Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
				new TypeReference<List<ProjectMasterFile>>() {
				});
		return new ResponseEntity<List<ProjectMasterFile>>(listProjectMasterFile1, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateProjectMasterFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<ProjectMasterFile> lstProjectMasterFile = objMapper
				.readValue(request.getParameter("projectmasterfile"), new TypeReference<List<ProjectMasterFile>>() {
				});
		if (lstProjectMasterFile != null && lstProjectMasterFile.size() > 0) {
			final ProjectMasterFile objProjectMasterFile = lstProjectMasterFile.get(0);
			final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				if (objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
				final String sQuery = "select * from projectmasterfile where nprojectmasterfilecode = "
						+ objProjectMasterFile.getNprojectmasterfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ProjectMasterFile objPF = (ProjectMasterFile) jdbcUtilityFunction.queryForObject(sQuery,
						ProjectMasterFile.class, jdbcTemplate);
				if (objPF != null) {
					projectmasterResponseEntity.putAll(getActiveProjectMasterById(
							lstProjectMasterFile.get(0).getNprojectmastercode(), objUserInfo).getBody());
					final Map<String, Object> objprojectmaster = objMapper.convertValue(
							projectmasterResponseEntity.get("SelectedProjectMaster"),
							new TypeReference<Map<String, Object>>() {
							});
					if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()) {
						String ssystemfilename = "";
						if (objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objProjectMasterFile.getSsystemfilename();
						}
						final String sUpdateQuery = "update projectmasterfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objProjectMasterFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objProjectMasterFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objProjectMasterFile.getNattachmenttypecode() + ", nlinkcode="
								+ objProjectMasterFile.getNlinkcode() + "," + " nfilesize = "
								+ objProjectMasterFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+ "' where nprojectmasterfilecode = "
								+ objProjectMasterFile.getNprojectmasterfilecode();
						objProjectMasterFile.setDcreateddate(objPF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
						.add(objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype() ? "IDS_EDITPROJECTFILE" : "IDS_EDITPROJECTLINK");
						lstOldObject.add(objPF);
						auditUtilityFunction.fnInsertAuditAction(lstProjectMasterFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						final List<ProjectMasterFile> lstFile = getProjectMasterFile(
								lstProjectMasterFile.get(0).getNprojectmastercode(), objUserInfo).getBody();
						outputMap.put("projectmasterFile", lstFile);
						return new ResponseEntity<>(outputMap, HttpStatus.OK);
					} else {

						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SELECTDRAFTPROJECTMASTERVERSION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteProjectMasterFile(final ProjectMasterFile objProjectMasterFile,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(objProjectMasterFile.getNprojectmastercode(),
				objUserInfo).getBody());
		if (objProjectMasterFile != null) {
			final String sQuery = "select * from projectmasterfile where nprojectmasterfilecode = "
					+ objProjectMasterFile.getNprojectmasterfilecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ProjectMasterFile objPF = (ProjectMasterFile) jdbcUtilityFunction.queryForObject(sQuery,
					ProjectMasterFile.class, jdbcTemplate);
			if (objPF != null) {
				final Map<String, Object> objprojectmaster1 = objMapper.convertValue(
						projectmasterResponseEntity.get("SelectedProjectMaster"),
						new TypeReference<Map<String, Object>>() {
						});
				if ((short) objprojectmaster1.get("ntransactionstatus") == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					if (objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objProjectMasterFile.setScreateddate(null);
					}
					final String sUpdateQuery = "update projectmasterfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nprojectmasterfilecode = " + objProjectMasterFile.getNprojectmasterfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add(
							objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_DELETEPROJECTFILE"
									: "IDS_DELETEPROJECTLINK");
					lstObject.add(objProjectMasterFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVERSION",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		final List<ProjectMasterFile> lstProjectMasterFile = getProjectMasterFile(
				objProjectMasterFile.getNprojectmastercode(), objUserInfo).getBody();
		outputMap.put("projectmasterFile", lstProjectMasterFile);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> closureProjectMasterFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final List<Object> savedProjectMasterClosureFileList = new ArrayList<>();
		String query = "";
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<String> multilingualIDList = new ArrayList<>();
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final Instant instantDate = dateUtilityFunction.convertStringDateToUTC(request.getParameter("dtransactiondate"),
				objUserInfo, false);
		final ProjectMaster objProjectMasterClosureFile = objMapper.readValue(request.getParameter("projectmaster"),
				ProjectMaster.class);
		objProjectMasterClosureFile.setDtransactiondate(instantDate);
		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
		if (objProjectMasterClosureFile.getDtransactiondate() != null) {
			objProjectMasterClosureFile.setSprojectclosuredate(
					dateUtilityFunction.instantDateToString(objProjectMasterClosureFile.getDtransactiondate())
					.replace("T", " ").replace("Z", ""));
			lstDateField.add("sprojectclosuredate");
			lstDatecolumn.add("ntzprojectclosuredate");
		}
		projectmasterResponseEntity.putAll(
				getActiveProjectMasterById(objProjectMasterClosureFile.getNprojectmastercode(),
						objUserInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objprojectmaster = objMapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if (objProjectMasterClosureFile.getSfilename() != null) {
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name - master
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == sReturnString) {
				if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()
						|| (short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()) {

					final ObjectMapper objmapper = new ObjectMapper();
					objmapper.registerModule(new JavaTimeModule());
					final Instant dDate = getProjectStatusDate(objProjectMasterClosureFile.getNprojectmastercode(),
							objProjectMasterClosureFile.getNtransactionstatus(), objUserInfo);
					final ProjectMaster convertedObject = objmapper
							.convertValue(
									dateUtilityFunction.convertInputDateToUTCByZone(objProjectMasterClosureFile,
											lstDateField, lstDatecolumn, true, objUserInfo),
									new TypeReference<ProjectMaster>() {
									});
					if (convertedObject.getDtransactiondate().isBefore(dDate) != true) {
						String sfilename;
						if (objProjectMasterClosureFile.getSfilename() == null) {
							sfilename = "";
						} else {
							sfilename = objProjectMasterClosureFile.getSfilename();
						}
						if (objProjectMasterClosureFile.getSfilename() != null) {
							query = "update projectmaster set " + "ssystemfilename='"
									+ objProjectMasterClosureFile.getSsystemfilename() + "',sfilename='"
									+ stringUtilityFunction.replaceQuote(sfilename) + "'" + " where nprojectmastercode="
									+ objProjectMasterClosureFile.getNprojectmastercode() + "; ";
						}
						int nprojectmasterhistorycode = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnoprojectmanagement where stablename='projectmasterhistory'",
								Integer.class);
						nprojectmasterhistorycode++;
						final String historyQuery = insertProjectMasterHistory(nprojectmasterhistorycode,
								objProjectMasterClosureFile.getNprojectmastercode(),
								objProjectMasterClosureFile.getDtransactiondate(),
								objProjectMasterClosureFile.getSclosureremarks(),
								convertedObject.getNoffsetdtransactiondate(),
								convertedObject.getNtransdatetimezonecode(), objUserInfo,
								Enumeration.TransactionStatus.CLOSED.gettransactionstatus());
						query = query + historyQuery;
						jdbcTemplate.execute(query);
						objProjectMasterClosureFile.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.CLOSED.gettransactionstatus());
						objProjectMasterClosureFile.setSprojectclosuredate(convertedObject.getSprojectclosuredate());
						if (objProjectMasterClosureFile.getDtransactiondate() != null) {
							final Date date = Date.from(objProjectMasterClosureFile.getDtransactiondate());
							objProjectMasterClosureFile.setSprojectclosuredate(
									new SimpleDateFormat(objUserInfo.getSsitedate()).format(date));
						}
						savedProjectMasterClosureFileList.add(objProjectMasterClosureFile);
						multilingualIDList.add("IDS_PROJECTCLOSURE");
						auditUtilityFunction.fnInsertAuditAction(savedProjectMasterClosureFileList, 1, null,
								multilingualIDList, objUserInfo);
						projectmasterResponseEntity.putAll(getActiveProjectMasterById(
								objProjectMasterClosureFile.getNprojectmastercode(), objUserInfo).getBody());
						map.put("SelectedProjectMaster", projectmasterResponseEntity.get("SelectedProjectMaster"));
						final List<ProjectMaster> lstProjectMaster = getProjectMaster(
								objProjectMasterClosureFile.getNprojecttypecode(), objUserInfo);
						map.put("ProjectMaster", lstProjectMaster);
						final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
								objProjectMasterClosureFile.getNprojectmastercode(), objUserInfo).getBody();
						map.put("ProjectMasterHistory", lstProjectMasterHistory);
						return new ResponseEntity<Object>(map, HttpStatus.OK);
					} else {
						String sSatatus = "";
						if (objProjectMasterClosureFile
								.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus()) {
							sSatatus = "IDS_CLOSEDATESHOULBECOMPLETE";
						} else {
							sSatatus = "IDS_CLOSEDATESHOULBERETIREDATE";
						}
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sSatatus, objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTCOMPLETEORRETIREACTION",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> editProjectMasterFile(final ProjectMasterFile objProjectMasterFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select pf.nprojectmasterfilecode, pf.nprojectmastercode, pf.nlinkcode, pf.nattachmenttypecode, pf.sfilename, pf.sdescription, pf.nfilesize,"
				+ " pf.ssystemfilename, lm.jsondata->>'slinkname' as slinkname"
				+ " from projectmasterfile pf, linkmaster lm where lm.nlinkcode = pf.nlinkcode" + " and pf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pf.nprojectmasterfilecode = "
				+ objProjectMasterFile.getNprojectmasterfilecode();
		final ProjectMasterFile objPF = (ProjectMasterFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				ProjectMasterFile.class, jdbcTemplate);
		if (objPF != null) {
			return new ResponseEntity<Object>(objPF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewAttachedProjectMasterFile(final ProjectMasterFile objProjectMasterFile,
			final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String sQuery = "select * from projectmasterfile where nprojectmasterfilecode = "
				+ objProjectMasterFile.getNprojectmasterfilecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProjectMasterFile objPF = (ProjectMasterFile) jdbcUtilityFunction.queryForObject(sQuery,
				ProjectMasterFile.class, jdbcTemplate);
		if (objPF != null) {
			if (objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

				map = ftpUtilityFunction.FileViewUsingFtp(objPF.getSsystemfilename(), -1, objUserInfo, "", "");
			} else {
				sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
						+ objPF.getNlinkcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
						LinkMaster.class, jdbcTemplate);
				map.put("AttachLink", objlinkmaster.getSlinkname() + objPF.getSfilename());
				objProjectMasterFile.setScreateddate(null);
			}
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
			.add(objProjectMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
			? "IDS_VIEWPROJECTMASTERFILE"
					: "IDS_VIEWPROJECTMASTERLINK");
			lstObject.add(objProjectMasterFile);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserrole(final UserInfo userInfo) throws Exception {
		final String strQuery = "select ur.nuserrolecode,ur.suserrolename from userroleconfig urc,userrole ur where "
				+ " urc.nuserrolecode=ur.nuserrolecode and urc.nneedprojectflow="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + " and  urc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode="
				+ userInfo.getNmastersitecode() + " ";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK); // status
	}

	@Override
	public ResponseEntity<Object> getUsers(final Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select ur.nuserrolecode,CONCAT(us.sfirstname,' ',us.slastname) as susername,us.nusercode,umr.nusermultirolecode,ur.suserrolename "
				+ "from usermultirole umr, userrole ur, users us,userssite usite  where umr.nuserrolecode=ur.nuserrolecode "
				+ " and usite.nusercode=us.nusercode and umr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ntransactionstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umr.nusersitecode=usite.nusersitecode and usite.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode in ("
				+ nuserrolecode + ") and usite.nsitecode =" + userInfo.getNsitecode() + " ";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApprovedProjectMasterByProjectType(final Integer nprojecttypecode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		int nprojectmastercode = 0;
		List<ProjectMaster> lstprojectmaster = new ArrayList<>();
		final String sProjectCodeQuery = "select * from projectmaster where nprojecttypecode=" + nprojecttypecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		final List<ProjectMaster> lstProjectCode = jdbcTemplate.query(sProjectCodeQuery, new ProjectMaster());
		if (lstProjectCode.size() > 0) {
			nprojectmastercode = lstProjectCode.get(0).getNprojectmastercode();
			final String sprojectmasterQuery = "select pm.*,CONCAT(u.sfirstname,' ',u.slastname) as susername,ur.suserrolename,to_char(pm.drfwdate,'"
					+ userInfo.getSsitedate() + "') as srfwdate from projectmaster pm,userrole ur,users u "
					+ " where pm.nprojectmastercode=" + nprojectmastercode + " and pm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
					+ " and ur.nuserrolecode=pm.nuserrolecode and u.nusercode=pm.nusercode and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			lstprojectmaster = jdbcTemplate.query(sprojectmasterQuery, new ProjectMaster());
			map.put("SelectedProjectCode", lstProjectCode.get(0));
			map.put("ProjectCode", lstProjectCode);
			map.put("Projectmaster", lstprojectmaster);
		} else {
			map.put("SelectedProjectCode", null);
			map.put("ProjectCode", null);
			map.put("Projectmaster", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApprovedProjectByProjectType(final int projectTypeCode,
			final UserInfo objUserInfo) {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "select pm.nprojecttypecode,pm.nprojectmastercode,pm.sprojecttitle,pm.sprojectcode"
				+ " from projectmaster pm,projectmasterhistory pmh where pmh.nprojectmastercode=pm.nprojectmastercode and "
				+ " pm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " pmh.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and pmh.nprojectmasterhistorycode = any "
				+ "(select max(nprojectmasterhistorycode) from   projectmasterhistory ph 	where ph.nprojectmastercode = pm.nprojectmastercode "
				+ " group  by ph.nprojectmastercode)  and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pmh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nprojecttypecode="
				+ projectTypeCode + " union "
				+ " select pm.nprojecttypecode, pm.nprojectmastercode,pm.sprojecttitle,pm.sprojectcode from  projecttype pt, projectmaster pm,testgroupspecification tgs"
				+ "	where  pt.nstatus = pm.nstatus and pm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode = pm.nprojecttypecode and pm.nprojectmastercode=tgs.nprojectmastercode "
				+ " and  pt.nprojecttypecode>0 and  tgs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nprojecttypecode = "
				+ projectTypeCode;
		final List<ProjectMaster> listProjectMaster = jdbcTemplate.query(strQuery, new ProjectMaster());
		outputMap.put("ProjectMasterList", listProjectMaster);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> viewAttachedProjectMasterClosureFile(final ProjectMaster objProjectMaster,
			final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final String sQuery = "select * from projectmaster where nprojectmastercode = "
				+ objProjectMaster.getNprojectmastercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProjectMaster objPF = (ProjectMaster) jdbcUtilityFunction.queryForObject(sQuery, ProjectMaster.class,
				jdbcTemplate);

		if (objPF != null) {
			map = ftpUtilityFunction.FileViewUsingFtp(objPF.getSsystemfilename(), -1, objUserInfo, "", "");
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add("IDS_VIEWPROJECTMASTERCLOSUREFILE");
			lstObject.add(objProjectMaster);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEditReportInfoProject(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		final int nprojectmastercode = (int) inputMap.get("nprojectmastercode");
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(nprojectmastercode, objUserInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String getEditQuery = "select * from reportinfoproject rp  where " + " rp.nprojectmastercode ="
					+ nprojectmastercode + " and rp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ReportInfoProject lst = (ReportInfoProject) jdbcUtilityFunction.queryForObject(getEditQuery,
					ReportInfoProject.class, jdbcTemplate);
			if (lst == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THISRECORDHASNOREPORTFOUND",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				map.put("ReportInfoProject", lst);
			}
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<? extends Object> updateReportInfoProject(final ReportInfoProject objReportInfoProject,
			final UserInfo objUserInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(objReportInfoProject.getNprojectmastercode(),
				objUserInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String oldquery = "select * from reportinfoproject where nprojectmastercode ="
					+ objReportInfoProject.getNprojectmastercode() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final ReportInfoProject unit = (ReportInfoProject) jdbcUtilityFunction.queryForObject(oldquery,
					ReportInfoProject.class, jdbcTemplate);
			final String updatequery = "update reportinfoproject set  nreporttemplatecode="
					+ objReportInfoProject.getNreporttemplatecode() + ",   sreporttemplateversion =N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getSreporttemplateversion()) + "',"
					+ "srevision=N'" + stringUtilityFunction.replaceQuote(objReportInfoProject.getSrevision())
					+ "',srevisionauthor=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getSrevisionauthor()) + "',"
					+ "sintroduction=N'" + stringUtilityFunction.replaceQuote(objReportInfoProject.getSintroduction())
					+ "', stestproductheadercomments=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getStestproductheadercomments()) + "',"
					+ "stestproductfootercomments1=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getStestproductfootercomments1())
					+ "',stestproductfootercomments2=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getStestproductfootercomments2()) + "',"
					+ "stestproductfootercomments3=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getStestproductfootercomments3())
					+ "',stestproductfootercomments4=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getStestproductfootercomments4()) + "',"
					+ "ssamplingdetails=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getSsamplingdetails())
					+ "',suncertainitymeasurement=N'"
					+ stringUtilityFunction.replaceQuote(objReportInfoProject.getSuncertainitymeasurement()) + "' "
					+ "where nprojectmastercode =" + objReportInfoProject.getNprojectmastercode() + "";
			jdbcTemplate.execute(updatequery);
			final String newquery = "select * from reportinfoproject where nprojectmastercode ="
					+ objReportInfoProject.getNprojectmastercode() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final ReportInfoProject objUnit = (ReportInfoProject) jdbcUtilityFunction.queryForObject(newquery,
					ReportInfoProject.class, jdbcTemplate);
			listAfterUpdate.add(objUnit);
			listBeforeUpdate.add(unit);
			multilingualIDList.add("IDS_REPORTINFOPROJECT");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					objUserInfo);
			return getActiveProjectMasterById(objReportInfoProject.getNprojectmastercode(), objUserInfo);
		}
	}

	@Override
	public ResponseEntity<Object> getQuotaionNoByClient(final int nclientcatcode, final int nclientcode,
			final UserInfo objUserInfo) throws Exception {
		final String strQuery = " select q.nquotationcode,q.squotationno from quotation q,clientcategory cc,client cl,quotationversionhistory qv,transactionstatus t where cc.nclientcatcode=q.nclientcatcode and "
				+ " cl.nclientcode=q.nclientcode and qv.nquotationcode=q.nquotationcode and qv.ntransactionstatus=t.ntranscode "
				+ " and  q.nclientcatcode=" + nclientcatcode + " and q.nquotationcode>0 and qv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ " and qv.nquotationhistorycode=any("
				+ "	select max(qh.nquotationhistorycode) from   quotationversionhistory qh where "
				+ "	qh.nquotationcode = q.nquotationcode " + " and nsitecode=" + objUserInfo.getNtranssitecode() + ""
				+ "	group  by qh.nquotationcode) and  q.nclientcode=" + nclientcode + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  cl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qv.nsitecode="
				+ objUserInfo.getNtranssitecode() + " and  q.nsitecode=" + objUserInfo.getNtranssitecode() + "";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> completeProjectMaster(final ProjectMaster completeProjectMaster,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
		final Map<String, Object> selectedprojectmaster = new LinkedHashMap<String, Object>();
		final List<Object> auditList = new ArrayList<Object>();
		final List<String> multilingualIDList = new ArrayList<String>();
		final Map<String, Object> projectmasterResponseEntity = new LinkedHashMap<String, Object>();
		projectmasterResponseEntity
		.putAll(getActiveProjectMasterById(completeProjectMaster.getNprojectmastercode(),
				userInfo).getBody());
		if (projectmasterResponseEntity.get("SelectedProjectMaster") == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objprojectmaster = objmapper.convertValue(
					projectmasterResponseEntity.get("SelectedProjectMaster"), new TypeReference<Map<String, Object>>() {
					});
			if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				final int nprojectmastercode = completeProjectMaster.getNprojectmastercode();
				if (completeProjectMaster.getDtransactiondate() != null) {
					completeProjectMaster.setSprojectcompletiondate(
							dateUtilityFunction.instantDateToString(completeProjectMaster.getDtransactiondate())
							.replace("T", " ").replace("Z", ""));
					lstDateField.add("sprojectcompletiondate");
					lstDatecolumn.add("ntzprojectcompletiondate");
				}
				final ProjectMaster convertedObject = objmapper
						.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(completeProjectMaster,
								lstDateField, lstDatecolumn, true, userInfo), new TypeReference<ProjectMaster>() {
						});
				final Instant dDate = getProjectStatusDate(completeProjectMaster.getNprojectmastercode(),
						Enumeration.TransactionStatus.APPROVED.gettransactionstatus(), userInfo);
				if (convertedObject.getDtransactiondate().isBefore(dDate) != true) {
					String updateQuery = "";
					int nprojectmasterhistorycode = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnoprojectmanagement where stablename='projectmasterhistory'",
							Integer.class);
					nprojectmasterhistorycode++;
					final String historyQuery = insertProjectMasterHistory(nprojectmasterhistorycode,
							completeProjectMaster.getNprojectmastercode(), completeProjectMaster.getDtransactiondate(),
							completeProjectMaster.getScompletionremarks(), convertedObject.getNoffsetdtransactiondate(),
							convertedObject.getNtransdatetimezonecode(), userInfo,
							Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
					updateQuery = updateQuery + historyQuery;
					jdbcTemplate.execute(updateQuery);
					completeProjectMaster.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
					completeProjectMaster.setSfilename("");
					completeProjectMaster.setSclosureremarks("");
					completeProjectMaster.setSretiredremarks("");
					if (completeProjectMaster.getDprojectstartdate() != null) {
						final Date date = Date.from(completeProjectMaster.getDprojectstartdate());
						completeProjectMaster
						.setSprojectstartdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (completeProjectMaster.getDexpectcompletiondate() != null) {
						final Date date = Date.from(completeProjectMaster.getDexpectcompletiondate());
						completeProjectMaster
						.setSexpectcompletiondate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (completeProjectMaster.getDtransactiondate() != null) {
						final Date date = Date.from(completeProjectMaster.getDtransactiondate());
						completeProjectMaster
						.setSprojectcompletiondate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					if (completeProjectMaster.getDrfwdate() != null) {
						final Date date = Date.from(completeProjectMaster.getDrfwdate());
						completeProjectMaster.setSrfwdate(new SimpleDateFormat(userInfo.getSsitedate()).format(date));
					}
					auditList.add(completeProjectMaster);
					multilingualIDList.add("IDS_COMPLETEPROJECTMASTER");
					auditUtilityFunction.fnInsertAuditAction(auditList, 1, null, multilingualIDList, userInfo);
					selectedprojectmaster.putAll(
							getActiveProjectMasterById(nprojectmastercode, userInfo).getBody());
					map.put("SelectedProjectMaster", selectedprojectmaster.get("SelectedProjectMaster"));
					final List<ProjectMasterHistory> lstProjectMasterHistory = getProjectMasterHistory(
							nprojectmastercode, userInfo).getBody();
					map.put("ProjectMasterHistory", lstProjectMasterHistory);
					return new ResponseEntity<Object>(map, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_COMPLETIONDATESHOULDBEAPPROVEDDATE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.COMPLETED
					.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYRETIRE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else if ((short) objprojectmaster.get("ntransactionstatus") == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTALREADYRETIRE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTPROJECTMASTERVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	public Instant getProjectStatusDate(final int nprojectmastercode, final int nversionstatus,
			final UserInfo userinfo) {
		final String strQuery = "select dtransactiondate from projectmasterhistory where nprojectmastercode="
				+ nprojectmastercode + " and ntransactionstatus=" + nversionstatus + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userinfo.getNtranssitecode();
		final Instant projectMasterHistory = jdbcTemplate.queryForObject(strQuery, Instant.class);
		return projectMasterHistory;
	}

	private ResponseEntity<List<ProjectMasterHistory>> getProjectMasterHistory(final int nprojectmastercode,
			final UserInfo userInfo) throws Exception {
		final String queryformat = "TO_CHAR(ph.dtransactiondate,'" + userInfo.getSpgsitedatetime() + "') ";
		final String historyQuery = "select ph.nprojectmasterhistorycode, ph.ntransactionstatus,case when(ph.sremarks=NULL OR ph.sremarks='') then '-' else ph.sremarks end, ph.nusercode, ph.nuserrolecode, "
				+ "" + queryformat + " as sdtransactiondate,"
				+ " CONCAT( u.sfirstname,' ',u.slastname) as susername, ur.suserrolename, "
				+ " ur.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "	 ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from projectmasterhistory ph, users u, userrole ur, transactionstatus ts"
				+ " where u.nusercode = ph.nusercode and ur.nuserrolecode = ph.nuserrolecode and ts.ntranscode = ph.ntransactionstatus"
				+ " and u.nstatus = ph.nstatus and ur.nstatus = ph.nstatus and ts.nstatus = ph.nstatus"
				+ " and ph.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ph.nprojectmastercode = " + nprojectmastercode + " order by ph.nprojectmasterhistorycode desc";
		@SuppressWarnings("unchecked")
		final List<ProjectMasterHistory> listProjectMasterHistory = (List<ProjectMasterHistory>) dateUtilityFunction
		.getSiteLocalTimeFromUTC(jdbcTemplate.query(historyQuery, new ProjectMasterHistory()),
				Arrays.asList("sdtransactiondate"), null, userInfo, true, Arrays.asList("stransdisplaystatus"),
				false);
		return new ResponseEntity<List<ProjectMasterHistory>>(listProjectMasterHistory, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReportTemplate(final UserInfo userInfo) throws Exception {
		final String strQuery = "select rt.nreporttemplatecode," + "coalesce(rt.jsondata ->'stemplatename'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "rt.jsondata ->'stemplatename'->>'en-US') as stemplatename  from  reporttemplate rt   where rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nreporttemplatecode>0 and rt.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ReportInfoProject()), HttpStatus.OK); // status
	}

	@Override
	public ResponseEntity<Object> getActiveReportTemplate(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();
		final String getEditQuery = "select *,coalesce(rt.jsondata ->'stemplatename'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "',rt.jsondata ->'stemplatename'->>'en-US') as stemplatename  from reportinfoproject rp,reporttemplate rt where rt.nreporttemplatecode=rp.nreporttemplatecode and rt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nprojectmastercode ="
				+ nprojectmastercode + " and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ReportInfoProject lst = (ReportInfoProject) jdbcUtilityFunction.queryForObject(getEditQuery,
				ReportInfoProject.class, jdbcTemplate);
		map.put("ReportTemplate", lst);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public String insertProjectMasterHistory(final int nprojectmasterhistorycode, final int nprojectmastercode,
			final Instant dDate, final String sremarks, final int noffsettransactioncode, final int ntimezonecode,
			final UserInfo userInfo, final int nstatuscode) throws Exception {
		String query = "";
		query = " insert into projectmasterhistory(nprojectmasterhistorycode, nprojectmastercode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, dtransactiondate,sremarks, noffsetdtransactiondate, ntransdatetimezonecode,dmodifieddate, nsitecode, nstatus) "
				+ "values (" + nprojectmasterhistorycode + "," + nprojectmastercode + "," + userInfo.getNusercode()
				+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
				+ userInfo.getNdeputyuserrole() + ", " + nstatuscode + ",'" + dDate + "',N'"
				+ stringUtilityFunction.replaceQuote(sremarks) + "'" + "," + noffsettransactioncode + ","
				+ ntimezonecode + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";
		query = query + "update seqnoprojectmanagement set nsequenceno = " + nprojectmasterhistorycode
				+ " where stablename='projectmasterhistory'";
		return query;
	}

	public ProjectMaster checkProjectIsPresent(final int nprojectmastercode) throws Exception {
		final String strQuery = "select nprojectmastercode from projectmaster where nprojectmastercode = "
				+ nprojectmastercode + " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProjectMaster objTest = (ProjectMaster) jdbcUtilityFunction.queryForObject(strQuery, ProjectMaster.class,
				jdbcTemplate);
		return objTest;
	}

	@Override
	public ResponseEntity<Object> getAvailableProjectQuotation(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		final ProjectMaster projectQuotationById = checkProjectIsPresent(nprojectmastercode);
		if (projectQuotationById == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTALREDYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String sQuery = "select  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " as nstatus," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " as ntransactionstatus," + " " + Enumeration.TransactionStatus.NA.gettransactionstatus()
			+ " as nprojectquotationcode," + " " + Enumeration.TransactionStatus.NO.gettransactionstatus()
			+ " as ndefaultstatus, q.nquotationcode,q.squotationno "
			+ " from quotation q,quotationversionhistory qvh where q.nstatus = "
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qvh.nstatus="
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " and q.nclientcode=any(select pm.nclientcode from projectmaster pm where pm.nprojectmastercode = "
			+ nprojectmastercode + " and pm.nstatus="
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pm.nsitecode="
			+ objUserInfo.getNtranssitecode() + ")" + " and q.nsitecode=qvh.nsitecode and q.nsitecode="
			+ objUserInfo.getNtranssitecode() + "" + " and qvh.ntransactionstatus="
			+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
			+ " and q.nquotationcode>0 and q.nquotationcode=qvh.nquotationcode and qvh.nquotationhistorycode= any(select max(nquotationhistorycode) from quotationversionhistory where "
			+ " nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
			+ objUserInfo.getNtranssitecode()
			+ " group by nquotationcode) and not exists (select 1 from projectquotation pq "
			+ " where pq.nquotationcode= q.nquotationcode and pq.nstatus= "
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pq.nprojectmastercode = "
			+ nprojectmastercode + " and pq.nsitecode=" + objUserInfo.getNtranssitecode() + ")";
			final List<ProjectQuotation> lstAvailablelist = jdbcTemplate.query(sQuery, new ProjectQuotation());
			if (!lstAvailablelist.isEmpty()) {
				return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_QUOTATIONNOTAVAILABLE",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	public List<ProjectQuotation> getProjectQuotation(final int nprojectmastercode, final UserInfo objUserInfo)
			throws Exception {
		final String sQuery = " select pm.nprojectmastercode,pm.nprojectquotationcode,q.squotationno ,pm.sremarks ,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + objUserInfo.getSlanguagetypecode()
				+ "') stransdisplaystatus,"
				+ " pm.ndefaultstatus,pm.nquotationcode from projectquotation pm ,projectmaster p,quotation q ,transactionstatus ts "
				+ " where pm.nprojectmastercode = p.nprojectmastercode "
				+ " and pm.nquotationcode=q.nquotationcode and ts.ntranscode=pm.ndefaultstatus and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pm.nprojectmastercode="
				+ nprojectmastercode + " and q.nsitecode=p.nsitecode "
				+ " and p.nsitecode=pm.nsitecode and pm.nsitecode= " + objUserInfo.getNtranssitecode() + ""
				+ " group by pm.nprojectquotationcode,pm.nprojectmastercode,q.squotationno,pm.ndefaultstatus,pm.nquotationcode,stransdisplaystatus,pm.sremarks";
		return jdbcTemplate.query(sQuery, new ProjectQuotation());
	}

	@Override
	public ResponseEntity<Object> createProjectQuotation(final ProjectQuotation objProjectQuotation,
			final UserInfo objUserInfo) throws Exception {
		final List<String> strProjectQuotation = new ArrayList<>();
		final List<Object> objlstProjectQuotation = new ArrayList<>();
		final int nprojectmastercode = objProjectQuotation.getNprojectmastercode();
		final ProjectMaster projectMasterById = checkProjectIsPresent(nprojectmastercode);
		if (projectMasterById == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTALREDYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String validateQuery = "select * from projectquotation where nprojectmastercode="
					+ objProjectQuotation.getNprojectmastercode() + " and nquotationcode="
					+ objProjectQuotation.getNquotationcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final List<ProjectQuotation> projectQuotationList = jdbcTemplate.query(validateQuery,
					new ProjectQuotation());
			if (projectQuotationList.isEmpty()) {
				final String strQuery = "select * from projectmaster pm,projectmasterhistory pmh where pm.nprojectmastercode=pmh.nprojectmastercode and"
						+ " pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from projectmasterhistory where "
						+ " nprojectmastercode=" + objProjectQuotation.getNprojectmastercode() + " and nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
						+ objUserInfo.getNtranssitecode() + " group by nprojectmastercode)"
						+ " and pmh.ntransactionstatus in ("
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "," + " "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + " and pmh.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and pmh.nsitecode=pm.nsitecode and pm.nsitecode=" + objUserInfo.getNtranssitecode()
						+ " and pm.nprojectmastercode=" + objProjectQuotation.getNprojectmastercode();
				final ProjectQuotation objValidate = (ProjectQuotation) jdbcUtilityFunction.queryForObject(strQuery,
						ProjectQuotation.class, jdbcTemplate);

				if (objValidate != null) {
					if (objProjectQuotation.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final ProjectQuotation defaultProjectQuotation = getProjectQuotationDefaultStatus(objUserInfo);

						if (defaultProjectQuotation != null) {
							final ProjectQuotation objBeforeSave = SerializationUtils.clone(defaultProjectQuotation);
							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(objBeforeSave);
							defaultProjectQuotation
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = " update projectquotation set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus()
									+ " where nprojectquotationcode ="
									+ defaultProjectQuotation.getNprojectquotationcode();
							jdbcTemplate.execute(updateQueryString);

							final List<Object> defaultListAfterSave = new ArrayList<>();
							defaultListAfterSave.add(defaultProjectQuotation);
							strProjectQuotation.add("IDS_EDITPROJECTQUOTATION");
							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
									strProjectQuotation, objUserInfo);
							strProjectQuotation.clear();
						}
					}
					final String sQuery = "select nsequenceno from seqnoprojectmanagement where stablename = 'projectquotation'";
					final int nSeqNo = jdbcTemplate.queryForObject(sQuery, Integer.class);
					String sInstQuery = "insert into projectquotation(nprojectquotationcode, nprojectmastercode, nquotationcode, ndefaultstatus, sremarks, dmodifieddate, nsitecode, nstatus)"
							+ "(select " + nSeqNo + " +rank()over(order by nquotationcode) as nprojectquotationcode,"
							+ nprojectmastercode + ",nquotationcode as nquotationcode,"
							+ objProjectQuotation.getNdefaultstatus() + ",N'"
							+ stringUtilityFunction.replaceQuote(objProjectQuotation.getSremarks()) + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from  quotation where nquotationcode in(" + objProjectQuotation.getNquotationcode()
							+ "))";
					jdbcTemplate.execute(sInstQuery);
					sInstQuery = "update seqnoprojectmanagement set nsequenceno = " + (nSeqNo + 1)
							+ " where stablename = 'projectquotation';";
					jdbcTemplate.execute(sInstQuery);
					objProjectQuotation.setNprojectquotationcode(nSeqNo + 1);
					objlstProjectQuotation.add(objProjectQuotation);
					strProjectQuotation.add("IDS_ADDPROJECTQUOTATION");
					auditUtilityFunction.fnInsertAuditAction(objlstProjectQuotation, 1, null, strProjectQuotation,
							objUserInfo);
					//ATE234 Janakumar ALPD-5960 Project-->Whenever the quotation is added, the records disappear from view.
					final Map<String, Object> map = new LinkedHashMap<String, Object>();

					map.put("ProjectQuotation", getProjectQuotation(nprojectmastercode, objUserInfo));
					
					return new ResponseEntity<Object>(map, HttpStatus.OK);
					//return new ResponseEntity<>(getProjectQuotation(nprojectmastercode, objUserInfo), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTORAPPROVEDPROJECT",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteProjectQuotation(final ProjectQuotation objProjectQuotation,
			final UserInfo objUserInfo) throws Exception {
		final List<String> strArray = new ArrayList<>();
		final List<Object> objlst = new ArrayList<>();
		final ProjectMaster projectQuotationById = checkProjectIsPresent(objProjectQuotation.getNprojectmastercode());
		if (projectQuotationById == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTALREDYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String sQuery = "select * from projectquotation where nprojectquotationcode = "
					+ objProjectQuotation.getNprojectquotationcode() + " and nstatus  = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ProjectQuotation objActiveProjectQuotation = (ProjectQuotation) jdbcUtilityFunction
					.queryForObject(sQuery, ProjectQuotation.class, jdbcTemplate);
			if (objActiveProjectQuotation != null) {
				final String strQuery = "select * from projectmaster pm,projectmasterhistory pmh where pm.nprojectmastercode=pmh.nprojectmastercode and"
						+ " pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from projectmasterhistory where "
						+ " nprojectmastercode=" + objProjectQuotation.getNprojectmastercode() + " and nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
						+ objUserInfo.getNtranssitecode() + " group by nprojectmastercode)"
						+ " and pmh.ntransactionstatus in ("
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "," + " "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + " and pmh.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and pmh.nsitecode=pm.nsitecode and pm.nsitecode=" + objUserInfo.getNtranssitecode()
						+ " and pm.nprojectmastercode=" + objProjectQuotation.getNprojectmastercode();
				final ProjectQuotation objValidate = (ProjectQuotation) jdbcUtilityFunction.queryForObject(strQuery,
						ProjectQuotation.class, jdbcTemplate);
				if (objValidate != null) {
					final String sdeleteQuery = "update projectquotation set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nprojectquotationcode="
							+ objProjectQuotation.getNprojectquotationcode();
					jdbcTemplate.execute(sdeleteQuery);
					objlst.add(objActiveProjectQuotation);
					strArray.add("IDS_DELETEPROJECTQUOTATION");
					auditUtilityFunction.fnInsertAuditAction(objlst, 1, null, strArray, objUserInfo);
					//ATE234 Janakumar ALPD-5960 Project-->Whenever the quotation is added, the records disappear from view.
					final Map<String, Object> map = new LinkedHashMap<String, Object>();

					map.put("ProjectQuotation", getProjectQuotation(objProjectQuotation.getNprojectmastercode(), objUserInfo));
					
					return new ResponseEntity<Object>(map, HttpStatus.OK);
//					return new ResponseEntity<>(
//							getProjectQuotation(objProjectQuotation.getNprojectmastercode(), objUserInfo),
//							HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTORAPPROVEDPROJECT",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ProjectQuotation getActiveProjectQuotationById(final int nprojectquotationcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select pm.nprojectmastercode,pm.nprojectquotationcode,q.squotationno ,pm.sremarks ,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "') stransdisplaystatus"
				+ " ,pm.ndefaultstatus,pm.nquotationcode from projectquotation pm ,projectmaster p,quotation q ,transactionstatus ts where pm.nprojectmastercode = p.nprojectmastercode "
				+ " and pm.nquotationcode=q.nquotationcode and ts.ntranscode=pm.ndefaultstatus and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pm.nprojectquotationcode="
				+ nprojectquotationcode + " and pm.nsitecode=p.nsitecode "
				+ " and pm.nsitecode=q.nsitecode and pm.nsitecode= " + userInfo.getNtranssitecode() + ""
				+ " group by pm.nprojectquotationcode,pm.nprojectmastercode,q.squotationno,pm.ndefaultstatus,pm.nquotationcode,stransdisplaystatus,pm.sremarks";
		return (ProjectQuotation) jdbcUtilityFunction.queryForObject(strQuery, ProjectQuotation.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateProjectQuotation(final ProjectQuotation objProjectQuotation,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final ProjectMaster projectQuotationById = checkProjectIsPresent(objProjectQuotation.getNprojectmastercode());
		if (projectQuotationById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_PROJECTALREDYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ProjectQuotation objActiveProjectQuotation = getActiveProjectQuotationById(
					objProjectQuotation.getNprojectquotationcode(), userInfo);
			if (objActiveProjectQuotation == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else {
				final String queryString = "select nprojectquotationcode from projectquotation where nquotationcode = "
						+ objProjectQuotation.getNquotationcode() + " and nprojectquotationcode <> "
						+ objProjectQuotation.getNprojectquotationcode() + " and  nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojectmastercode="
						+ objProjectQuotation.getNprojectmastercode();
				final List<ProjectQuotation> projectQuotationList = jdbcTemplate.query(queryString,
						new ProjectQuotation());
				if (projectQuotationList.isEmpty()) {
					final String strQuery = "select * from projectmaster pm,projectmasterhistory pmh where pm.nprojectmastercode=pmh.nprojectmastercode and"
							+ " pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from projectmasterhistory where "
							+ " nprojectmastercode=" + objProjectQuotation.getNprojectmastercode() + " and nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
							+ userInfo.getNtranssitecode() + " group by nprojectmastercode)"
							+ " and pmh.ntransactionstatus in ("
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "," + " "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + " and pmh.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and pmh.nsitecode=pm.nsitecode and pm.nsitecode=" + userInfo.getNtranssitecode()
							+ " and pm.nprojectmastercode=" + objProjectQuotation.getNprojectmastercode();
					final ProjectQuotation objValidate = (ProjectQuotation) jdbcUtilityFunction.queryForObject(strQuery,
							ProjectQuotation.class, jdbcTemplate);
					if (objValidate != null) {
						if (objProjectQuotation.getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							final ProjectQuotation defaultProjectQuotation = getProjectQuotationDefaultStatus(userInfo);
							if (defaultProjectQuotation != null && defaultProjectQuotation
									.getNprojectquotationcode() != objProjectQuotation.getNprojectquotationcode()) {
								final ProjectQuotation listBeforeSave = SerializationUtils
										.clone(defaultProjectQuotation);
								listBeforeUpdate.add(listBeforeSave);
								defaultProjectQuotation.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
								final String updateQueryString = " update projectquotation set ndefaultstatus="
										+ Enumeration.TransactionStatus.NO.gettransactionstatus()
										+ " where nprojectquotationcode="
										+ defaultProjectQuotation.getNprojectquotationcode();
								jdbcTemplate.execute(updateQueryString);
								listAfterUpdate.add(defaultProjectQuotation);
							}
						}
						final String updateQueryString = "update projectquotation set nquotationcode="
								+ objProjectQuotation.getNquotationcode() + ", ndefaultstatus = "
								+ objProjectQuotation.getNdefaultstatus() + " , sremarks=N'"
								+ stringUtilityFunction.replaceQuote(objProjectQuotation.getSremarks())
								+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nprojectquotationcode=" + objProjectQuotation.getNprojectquotationcode();
						jdbcTemplate.execute(updateQueryString);
						final ProjectQuotation objNewProjectQuotation = getActiveProjectQuotationById(
								objProjectQuotation.getNprojectquotationcode(), userInfo);
						multilingualIDList.add("IDS_EDITPROJECTQUOTATION");
						listAfterUpdate.add(objNewProjectQuotation);
						listBeforeUpdate.add(objActiveProjectQuotation);
						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								multilingualIDList, userInfo);
					//ATE234 Janakumar ALPD-5960 Project-->Whenever the quotation is added, the records disappear from view.
						final Map<String, Object> map = new LinkedHashMap<String, Object>();

						map.put("ProjectQuotation", getProjectQuotation(objProjectQuotation.getNprojectmastercode(), userInfo));
						
						return new ResponseEntity<Object>(map, HttpStatus.OK);
//						return new ResponseEntity<>(
//								getProjectQuotation(objProjectQuotation.getNprojectmastercode(), userInfo),
//								HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTORAPPROVEDPROJECT",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> getQuotation(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = " select q.squotationno ,q.nquotationcode "
				+ " from quotation q ,quotationversionhistory qvh where q.nquotationcode = qvh.nquotationcode "
				+ " and qvh.nquotationhistorycode= any(select nquotationhistorycode from quotationversionhistory where "
				+ " ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsitecode=" + userInfo.getNtranssitecode() + ") and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qvh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and q.nquotationcode>0 and q.nsitecode=qvh.nsitecode and q.nsitecode= "
				+ userInfo.getNtranssitecode() + "" + " group by q.nquotationcode,q.squotationno ";
		final List<Quotation> lstQuotation = jdbcTemplate.query(strQuery, new Quotation());
		outputMap.put("Quotation", lstQuotation);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private ProjectQuotation getProjectQuotationDefaultStatus(final UserInfo userInfo) throws Exception {
		final String strQuery = "select * from projectquotation pq" + " where pq.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pq.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and pq.nsitecode = "
				+ userInfo.getNtranssitecode();
		return (ProjectQuotation) jdbcUtilityFunction.queryForObject(strQuery, ProjectQuotation.class, jdbcTemplate);
	}
}