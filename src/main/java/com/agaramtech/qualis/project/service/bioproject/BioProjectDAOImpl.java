package com.agaramtech.qualis.project.service.bioproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.project.model.BioProject;
import com.agaramtech.qualis.project.model.Disease;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "bioproject" table by 
 * implementing methods from its interface.
 * BGSI-3
 * @author ATE234 Janakumar 26/06/2025
 */
@Repository
@AllArgsConstructor
public class BioProjectDAOImpl implements BioProjectDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(BioProjectDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	//private final ProjectDAOSupport projectDAOSupport;
	//private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) {
		// TODO Auto-generated method stub

		final String strQuery = "SELECT CONCAT(u.sfirstname, ' ', u.slastname) AS suserName,"
				+ "dc.sdiseasecategoryname,d.sdiseasename,bp.nbioprojectcode, bp.ndiseasecategorycode, bp.ndiseasecode,  "
				+ "bp.sprojecttitle, bp.sprojectcode, bp.sdescription, bp.nusercode, bp.nsitecode, bp.nstatus "
				+ "FROM bioproject bp,diseasecategory dc,disease d,users u where bp.ndiseasecategorycode=dc.ndiseasecategorycode "
				+ "and d.ndiseasecode=bp.ndiseasecode and u.nusercode=bp.nusercode and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nsitecode="
				+ userInfo.getNmastersitecode() + " and dc.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and d.nsitecode=" + userInfo.getNmastersitecode() + " and bp.nsitecode="
				+ userInfo.getNmastersitecode() + " and  bp.nbioprojectcode>0  order by bp.nbioprojectcode desc;";

		LOGGER.info("Get Method: " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new BioProject()), HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 * inputMap:[{"userinfo":{nmastersitecode": -1},"ndiseasecategory":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getDiseaseByCatgory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "SELECT d.ndiseasecode, d.ndiseasecategorycode, d.sdiseasename, d.sdescription, "
				+ "d.nsitecode, d.nstatus, dc.sdiseasecategoryname "
				+ "FROM disease d, diseasecategory dc WHERE d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND d.ndiseasecode > 0 "
				+ "AND d.ndiseasecategorycode = dc.ndiseasecategorycode AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND dc.nsitecode ="+userInfo.getNmastersitecode()+" "
				+ "AND d.nsitecode = "+ userInfo.getNmastersitecode() + " AND dc.ndiseasecategorycode=" + inputMap.get("ndiseasecategorycode")
				+ " " + "ORDER BY d.ndiseasecode DESC";
		LOGGER.info("Get Method" + strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Disease()), HttpStatus.OK);

	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *        {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getUsers(Map<String, Object> inputMap, UserInfo userInfo) {
		// TODO Auto-generated method stub

		final String strQuery = "SELECT CONCAT(sfirstname, ' ', slastname) AS suserName,nusercode "
				+ "FROM users where nusercode >0 and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode="+userInfo.getNmastersitecode()+";";

		LOGGER.info("Get Method: " + strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new BioProject()), HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": null,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objmapper = new ObjectMapper();
		final BioProject objBioProject = objmapper.convertValue(inputMap.get("bioproject"), BioProject.class);
		//final boolean isProjectCode=projectCodeValidation(objBioProject.getSprojectcode());
		final List<BioProject> bioProjectByName = (List<BioProject>) getBioProjectValidation(objBioProject, userInfo);
		if (bioProjectByName.get(0).isIssprojectcode() == false
				&& bioProjectByName.get(0).isIssprojecttitle() == false ) {
			final String sequenceNoQuery = "select nsequenceno from seqnoprojectmanagement where stablename ='bioproject'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertquery = "INSERT INTO bioproject(nbioprojectcode, ndiseasecategorycode, ndiseasecode, sprojecttitle, sprojectcode,"
					+ " sdescription, nusercode, dmodifieddate, nsitecode, nstatus)" + "VALUES (" + nsequenceNo + ","
					+ objBioProject.getNdiseasecategorycode() + "," + objBioProject.getNdiseasecode() + ",'"
					+ stringUtilityFunction.replaceQuote(objBioProject.getSprojecttitle()) + "','"
					+ stringUtilityFunction.replaceQuote(objBioProject.getSprojectcode()) + "','"
					+ stringUtilityFunction.replaceQuote(objBioProject.getSdescription()) + "'," + ""
					+ objBioProject.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update seqnoprojectmanagement set nsequenceno =" + nsequenceNo
					+ " where stablename='bioproject' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedTestList = new ArrayList<>();
			savedTestList.add(objBioProject);
			multilingualIDList.add("IDS_ADDBIOPROJECT");
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			return getBioProject(userInfo);
		} else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
//							userInfo.getSlanguagefilename()),
//					HttpStatus.CONFLICT);
			String alert = "";
			final boolean isIssprojectcode = bioProjectByName.get(0).isIssprojectcode();
			final boolean isIssprojecttitle = bioProjectByName.get(0).isIssprojecttitle();
			if (isIssprojectcode == true && isIssprojecttitle == true) {
				alert = commonFunction.getMultilingualMessage("IDS_PROJECTTITLE", userInfo.getSlanguagefilename())
						+ " and "
						+ commonFunction.getMultilingualMessage("IDS_PROJECTCODE", userInfo.getSlanguagefilename());
			} else if (isIssprojectcode == true) {
				alert = commonFunction.getMultilingualMessage("IDS_PROJECTCODE", userInfo.getSlanguagefilename());
			} else if (isIssprojecttitle == true) {
				alert = commonFunction.getMultilingualMessage("IDS_PROJECTTITLE", userInfo.getSlanguagefilename());
			}
//			else if (isProjectCode==false) {
//				alert = commonFunction.getMultilingualMessage("IDS_PROJECTCODENOTAVAILABLE", userInfo.getSlanguagefilename());
//			}
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

//	 private static boolean projectCodeValidation(String input) {
//		 
//		 return input.matches("[A-Za-z]+");
//			        
//	}
	
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": null,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	private List<BioProject> getBioProjectValidation(BioProject objBioProject, UserInfo userInfo) {
		// TODO Auto-generated method stub
		String updateBioProjectCode = "";
		if (objBioProject.getNbioprojectcode() > 0) {
			updateBioProjectCode = " and nbioprojectcode <> " + objBioProject.getNbioprojectcode() + "";
		}

		final String validationQuery = "SELECT  CASE WHEN EXISTS (SELECT sprojecttitle "
				+ "FROM bioproject WHERE sprojecttitle = '" + stringUtilityFunction.replaceQuote(objBioProject.getSprojecttitle()) + "' " + "AND nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nsitecode = "
				+ userInfo.getNmastersitecode() + " " + updateBioProjectCode
				+ " ) THEN TRUE ELSE FALSE END AS issprojecttitle, "
				+ "CASE WHEN EXISTS (SELECT sprojectcode FROM bioproject WHERE sprojectcode = '"
				+ stringUtilityFunction.replaceQuote(objBioProject.getSprojectcode()) + "' AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nsitecode = "
				+ userInfo.getNmastersitecode() + " " + updateBioProjectCode
				+ ") THEN TRUE ELSE FALSE END AS issprojectcode;";

		return (List<BioProject>) jdbcTemplate.query(validationQuery, new BioProject());
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"bioproject":{"nbioprojectcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public BioProject getActiveBioProjectById(int nbioprojectcode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final String strQuery = "SELECT CONCAT(u.sfirstname, ' ', u.slastname) AS suserName,"
				+ "dc.sdiseasecategoryname,d.sdiseasename,bp.nbioprojectcode, bp.ndiseasecategorycode, bp.ndiseasecode,  "
				+ "bp.sprojecttitle, bp.sprojectcode, bp.sdescription, bp.nusercode, bp.nsitecode, bp.nstatus "
				+ "FROM bioproject bp,diseasecategory dc,disease d,users u where bp.ndiseasecategorycode=dc.ndiseasecategorycode "
				+ "and d.ndiseasecode=bp.ndiseasecode and u.nusercode=bp.nusercode and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and bp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nsitecode="
				+ userInfo.getNmastersitecode() + " and dc.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and d.nsitecode=" + userInfo.getNmastersitecode() + " and bp.nsitecode="
				+ userInfo.getNmastersitecode() + " and bp.nbioprojectcode=" + nbioprojectcode + ";";

		return (BioProject) jdbcUtilityTemplateFunction.queryForObject(strQuery, BioProject.class, jdbcTemplate);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> deleteBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objmapper = new ObjectMapper();
		final BioProject objBioProject = objmapper.convertValue(inputMap.get("bioproject"), BioProject.class);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedBioProject = new ArrayList<>();
		final BioProject bioProjectRecord = getActiveBioProjectById(objBioProject.getNbioprojectcode(), userInfo);

		if (bioProjectRecord == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//			final String queryDelete = "";
//
//			validatorDel = projectDAOSupport.getTransactionInfo(queryDelete, userInfo);

//			boolean validRecord = true;
//			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
//				validRecord = true;
//				validatorDel = projectDAOSupport
//						.validateDeleteRecord(Integer.toString(objBioProject.getNbioprojectcode()), userInfo);
//				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
//					validRecord = true;
//				} else {
//					validRecord = false;
//				}
//			}
//
//			if (validRecord) {
				final String deletequery = "UPDATE bioproject " + "SET nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " " + "WHERE  nbioprojectcode="
						+ objBioProject.getNbioprojectcode() + " ;";
				jdbcTemplate.execute(deletequery);
				deletedBioProject.add(objBioProject);
				multilingualIDList.add("IDS_DELETEBIOPROJECT");
				auditUtilityFunction.fnInsertAuditAction(deletedBioProject, 1, null, multilingualIDList, userInfo);
				return getBioProject(userInfo);
//			}else{
//				//status code:417
//				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
//			}
		}


	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> updateBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub

		final ObjectMapper objmapper = new ObjectMapper();
		final BioProject objBioProject = objmapper.convertValue(inputMap.get("bioproject"), BioProject.class);
		final List<BioProject> bioProjectByName = (List<BioProject>) getBioProjectValidation(objBioProject, userInfo);
		final BioProject bioProjectBeforeRecord = getActiveBioProjectById(objBioProject.getNbioprojectcode(), userInfo);

		if (bioProjectBeforeRecord != null) {
			if (bioProjectByName.get(0).isIssprojectcode() == false
					&& bioProjectByName.get(0).isIssprojecttitle() == false) {
				final String updateQuery = "UPDATE bioproject SET ndiseasecategorycode="
						+ objBioProject.getNdiseasecategorycode() + ", " + "ndiseasecode="
						+ objBioProject.getNdiseasecode() + ", " + "sprojecttitle='"
						+ stringUtilityFunction.replaceQuote(objBioProject.getSprojecttitle()) + "', "
						+ "sprojectcode='" + stringUtilityFunction.replaceQuote(objBioProject.getSprojectcode()) + "',"
						+ " sdescription='" + stringUtilityFunction.replaceQuote(objBioProject.getSdescription()) + "',"
						+ " nusercode=" + objBioProject.getNusercode() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nsitecode="
						+ userInfo.getNmastersitecode() + ", nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE nbioprojectcode="
						+ objBioProject.getNbioprojectcode() + " ;";

				jdbcTemplate.execute(updateQuery);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				listAfterUpdate.add(objBioProject);
				listBeforeUpdate.add(bioProjectBeforeRecord);
				multilingualIDList.add("IDS_EDITBIOPROJECT");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getBioProject(userInfo);
			} else {
				String alert = "";
				final boolean isIssprojectcode = bioProjectByName.get(0).isIssprojectcode();
				final boolean isIssprojecttitle = bioProjectByName.get(0).isIssprojecttitle();
				if (isIssprojectcode == true && isIssprojecttitle == true) {
					alert = commonFunction.getMultilingualMessage("IDS_PROJECTTITLE", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_PROJECTCODE", userInfo.getSlanguagefilename());
				} else if (isIssprojectcode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_PROJECTCODE", userInfo.getSlanguagefilename());
				} else if (isIssprojecttitle == true) {
					alert = commonFunction.getMultilingualMessage("IDS_PROJECTTITLE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

}
