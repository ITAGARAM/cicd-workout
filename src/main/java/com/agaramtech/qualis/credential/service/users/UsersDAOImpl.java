package com.agaramtech.qualis.credential.service.users;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.PasswordPolicy;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.UserFile;
import com.agaramtech.qualis.credential.model.UserMultiDeputy;
import com.agaramtech.qualis.credential.model.UserMultiRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.credential.service.controlmaster.ControlMasterDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "users" table by implementing
 * methods from its interface.
 */

@AllArgsConstructor
@Repository
public class UsersDAOImpl implements UsersDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ControlMasterDAO controlMasterDAO;

	/**
	 * Fetches user information including selected user, user site, multi-role, deputy, and optional FTP image paths.
	 * If {@code nuserCode} is null, retrieves all users and selects the last one as selectedUser.
	 * If {@code nFlag} = 1, image files (sign/user) are downloaded via FTP and paths returned.
	 *
	 * Keys used in returned map:
	 * - "Users" : List of all users.
	 * - "SelectedUser" : Last user from list or fetched user by ID.
	 * - "UsersSiteList" : List of site mappings for the selected user.
	 * - "UserMultiRole" : List of multi-role entries for the selected user's site.
	 * - "UserMultiDeputy" : List of deputy mappings for the selected user's site.
	 * - "SignImagePath" : Sign image path or FTP download result.
	 * - "UserImagePath" : User profile image path or FTP download result.
	 *
	 * @param nuserCode Code of the user to retrieve; if null, retrieves all.
	 * @param nFlag If 1, image paths are retrieved from FTP and resolved.
	 * @param userInfo Contains user’s site, language, and session context.
	 * @return ResponseEntity containing user details and associated roles, site, images, etc.
	 * @throws Exception If any query or FTP handling fails.
	 */
	@Override
	public ResponseEntity<Object> getUsers(Integer nuserCode, Integer nFlag, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Users selectedUser = null;
		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();
		if (nuserCode == null) {
			final String query = "select us.nusercode, us.ndeptcode, us.ncountrycode, us.ndesignationcode, us.nlockmode, us.nlogintypecode, us.ntransactionstatus, "
					+ " us.sloginid, us.sempid, us.sfirstname, us.slastname, us.sbloodgroup, us.squalification, us.ddateofjoin, us.smobileno, us.sphoneno,"
					+ " us.semail, us.sjobdescription, us.saddress1, us.saddress2, us.saddress3, us.dmodifieddate, us.nsitecode, us.nstatus, "
					+ "CONCAT(us.sfirstname,' ',us.slastname) as susername,  TO_CHAR(us.ddateofjoin,'"
					+ userInfo.getSsitedate() + "') as sdateofjoin,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus ,"
					+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as slockstatus , "
					+ " desg.sdesignationname as sdesignationname,"
					+ " d.sdeptname as sdeptname, c.scountryname as scountryname, COALESCE(uf.ssignimgname,'') as ssignimgname, "
					+ " uf.ssignimgftp , COALESCE(uf.suserimgname,'') as suserimgname, uf.suserimgftp "
					+ " from users us, userfile uf, transactionstatus ts1, transactionstatus ts2, department d, country c, designation desg "
					+ " where us.ntransactionstatus = ts1.ntranscode and us.nlockmode = ts2.ntranscode "
					+ " and us.nusercode = uf.nusercode and uf.nstatus = "
					+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts2.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and desg.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nsitecode = "
					+ userInfo.getNmastersitecode() + " and uf.nsitecode = " + userInfo.getNmastersitecode()
					+ " and c.nsitecode = " + userInfo.getNmastersitecode() + " and d.nsitecode = "
					+ userInfo.getNmastersitecode() + " and desg.nsitecode = " + userInfo.getNmastersitecode()
					+ " and desg.ndesignationcode = us.ndesignationcode " + " and c.ncountrycode = us.ncountrycode "
					+ " and d.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and d.ndeptcode = us.ndeptcode and us.nusercode > 0  "
					+ " and length(trim(sfirstname)) > 0  and length(trim(slastname)) > 0 order by nusercode";

			final List<Users> usersList = jdbcTemplate.query(query, new Users());
			if (usersList.isEmpty()) {
				outputMap.put("Users", usersList);
				outputMap.put("SelectedUser", null);
				outputMap.put("UsersSite", null);
				outputMap.put("UserMultiRole", usersList);
				outputMap.put("UserMultiDeputy", usersList);
				outputMap.put("SignImagePath", null);
				outputMap.put("UserImagePath", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("Users", usersList);
				selectedUser = usersList.get(usersList.size() - 1);
				nuserCode = selectedUser.getNusercode();

			}
		} else {
			selectedUser = getActiveUserById(nuserCode, userInfo);

		}
		if (selectedUser == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedUser", selectedUser);
			final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
					.getUploadControlsByFormCode(userInfo).getBody();
			String signImage = null;
			final Map<String, Object> imageMap = new HashMap<String, Object>();
			if (selectedUser.getSsignimgftp() != null) {

				final String absolutePath = System.getenv(homePath) + Enumeration.FTP.USERSIGN_PATH.getFTP();

				final File file = new File(absolutePath + "\\" + selectedUser.getSsignimgftp());
				if (!file.exists()) {
					imageMap.put("SignImage_fileName", selectedUser.getSsignimgftp());
					imageMap.put("SignImage_customName", "");
					imageMap.put("SignImage_path", absolutePath);
				} else {
					signImage = selectedUser.getSsignimgftp();
				}
			}
			String userImage = null;
			if (selectedUser.getSuserimgftp() != null) {

				final String absolutePath = System.getenv(homePath)// new File("").getAbsolutePath()
						+ Enumeration.FTP.USERPROFILE_PATH.getFTP();
				final File file = new File(absolutePath + "\\" + selectedUser.getSuserimgftp());
				if (!file.exists()) {
					imageMap.put("UserImage_fileName", selectedUser.getSuserimgftp());
					imageMap.put("UserImage_customName", "");
					imageMap.put("UserImage_path", absolutePath);
				} else {
					userImage = selectedUser.getSuserimgftp();
				}
			}
			if (nFlag != null && nFlag == 1) {
				final Map<String, Object> map = ftpUtilityFunction.multiPathMultiFileDownloadUsingFtp(imageMap,
						controlList, userInfo, "");

				outputMap.put("SignImagePath",
						signImage == null ? (String) map.get("SignImage_AttachFile") : signImage);
				outputMap.put("UserImagePath",
						userImage == null ? (String) map.get("UserImage_AttachFile") : userImage);
			} else {
				outputMap.put("SignImagePath", signImage);
				outputMap.put("UserImagePath", userImage);

			}
			final String userSiteQuery = "select us.nusersitecode, us.nusercode, us.ndefaultsite, us.dmodifieddate, us.nsitecode, us.nstatus , s.ssitename from userssite us, site s where us.nsitecode = s.nsitecode and "
					+ " us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nusercode = " + nuserCode;
			final List<UsersSite> userSiteList = (List<UsersSite>) jdbcTemplate.query(userSiteQuery, new UsersSite());
			outputMap.put("UsersSiteList", userSiteList);

			if (!userSiteList.isEmpty()) {
				final UsersSite objUsersSite = userSiteList.get(userSiteList.size() - 1);
				outputMap.putAll((Map<String, Object>) getRoleAndDeputyByUsersSite(objUsersSite, userInfo).getBody());
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * Helper method to aggregate multi-role and deputy assignments based on the given UsersSite object.
	 *
	 * Keys used in returned map:
	 * - "UsersSite" : The input UsersSite object.
	 * - "UserMultiRole" : List of role mappings for this user site.
	 * - "UserMultiDeputy" : List of deputy assignments.
	 *
	 * @param objUsersSite User site association object.
	 * @param userInfo Contains language and site details.
	 * @return ResponseEntity with the combined map of role and deputy data.
	 * @throws Exception if retrieval fails.
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Map<String, Object>> getRoleAndDeputyByUsersSite(final UsersSite objUsersSite, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("UsersSite", objUsersSite);
		outputMap.putAll((Map<String, Object>) getUserMultiDeputy(objUsersSite.getNusersitecode(), userInfo).getBody());
		outputMap.putAll((Map<String, Object>) getUserMultiRole(objUsersSite.getNusersitecode(), userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Retrieves multi-role information for a user by their user site code.
	 * Each record includes role, login ID, default status, and active status with multilingual labels.
	 *
	 * Keys used in returned map:
	 * - "UserMultiRole" : List of {@code UserMultiRole} objects matching the site code.
	 *
	 * @param userSiteCode Unique site code associated with the user.
	 * @param userInfo Contains multilingual and site context.
	 * @return ResponseEntity containing role details for the given user site.
	 * @throws Exception if query or result mapping fails.
	 */
	@Override
	public ResponseEntity<Object> getUserMultiRole(final int userSiteCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String queryRole = " select um.nusermultirolecode, um.nusersitecode, um.nuserrolecode, um.ndefaultrole, um.ntransactionstatus, um.spassword,"
				+ " um.dpasswordvalidatedate, um.nnooffailedattempt, um.dmodifieddate, um.nsitecode, um.nstatus, ur.suserrolename, us.sloginid,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus ,"
				+ " usite.nusersitecode, us.nusercode, " + " coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus "
				+ " from usermultirole um, userrole ur, transactionstatus ts1, transactionstatus ts2, users us, userssite usite "
				+ " where um.nuserrolecode = ur.nuserrolecode and um.ntransactionstatus = ts1.ntranscode and "
				+ " um.ndefaultrole = ts2.ntranscode  and usite.nusercode = us.nusercode and um.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and um.nusersitecode = usite.nusersitecode and usite.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts2.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and um.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ur.nsitecode = " + userInfo.getNmastersitecode()
				+ " and us.nsitecode = " + userInfo.getNmastersitecode() + " and  usite.nusersitecode = "
				+ userSiteCode;

		final List<UserMultiRole> userMultiRoleList = (List<UserMultiRole>) jdbcTemplate.query(queryRole,
				new UserMultiRole());
		outputMap.put("UserMultiRole", userMultiRoleList);
		LOGGER.info("At getUserMultiRole");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Retrieves a list of multi-deputy role mappings for a given user site code.
	 *
	 * @param nuserSiteCode The user site code for which deputy roles are to be fetched.
	 * @param userInfo      The current user's session information.
	 *                      Used keys: nmastersitecode, slanguagetypecode
	 * @return ResponseEntity containing a map with key "UserMultiDeputy" and corresponding list of deputy role mappings.
	 * @throws Exception if any database or query execution error occurs.
	 */
	@Override
	public ResponseEntity<Object> getUserMultiDeputy(final int nuserSiteCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String multiDeputyQuery = " select umd.nusermultideputycode, umd.nusersitecode, umd.ndeputyusersitecode, umd.nuserrolecode, "
				+ "umd.ntransactionstatus, umd.dmodifieddate, umd.nsitecode, umd.nstatus, u.sloginid as sdeputyid, "
				+ " CONCAT(u.sfirstname,' ',u.slastname) as sdeputyname, ur.suserrolename as suserrolename, u.nusercode, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus " + " from usermultideputy umd, "
				+ " userssite us,users u,userrole ur,transactionstatus ts where umd.nuserrolecode = ur.nuserrolecode "
				+ " and umd.ndeputyusersitecode = us.nusersitecode and  us.nusercode = u.nusercode "
				+ " and ts.ntranscode = umd.ntransactionstatus and umd.nusersitecode = " + nuserSiteCode
				+ " and umd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.ntransactionstatus<>" + Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
				+ " and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umd.nsitecode = " + userInfo.getNmastersitecode() + " and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ur.nsitecode = " + userInfo.getNmastersitecode();

		final List<UserMultiDeputy> deputyUserList = (List<UserMultiDeputy>) jdbcTemplate.query(multiDeputyQuery,
				new UserMultiDeputy());
		outputMap.put("UserMultiDeputy", deputyUserList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Fetches a user record by login ID for a specific site.
	 *
	 * @param loginId  The login ID of the user.
	 * @param siteCode The site code to filter the user record.
	 * @return A Users object if the user with the given login ID exists and is active at the given site, otherwise null.
	 * @throws Exception if query execution fails.
	 */
	private Users getUserByLoginId(final String loginId, final int siteCode) throws Exception {
		final String strQuery = "select sloginid from users where sloginid =  N'"
				+ stringUtilityFunction.replaceQuote(loginId) + "'  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = " + siteCode;
		return (Users) jdbcUtilityFunction.queryForObject(strQuery, Users.class, jdbcTemplate);
	}

	/**
	 * Creates the first site mapping for a newly created user in the userssite table and updates audit logs.
	 *
	 * @param objUsers     The Users object containing the created user's details.
	 *                     Used key: nusercode
	 * @param objUsersSite The UsersSite object containing input site details like nsitecode and ndefaultsite.
	 *                     Used keys: nsitecode, ndefaultsite
	 *                     Updated keys: nusersitecode, nusercode, dmodifieddate, nstatus
	 * @param userInfo     Session-specific user information.
	 *                     Used keys: nmastersitecode
	 *                     Used for: Getting current timestamp, language, and audit context
	 * @return The updated UsersSite object with generated site mapping.
	 * @throws Exception if any validation, insertion, or database error occurs.
	 */
	private UsersSite createFirstUserSite(final Users objUsers, UsersSite objUsersSite, final UserInfo userInfo)
			throws Exception {
		final String queryString = "select s.nsitecode, s.ntimezonecode, s.ndateformatcode, s.nregioncode, s.ndistrictcode, s.ssitename,"
				+ " s.ssitecode, s.ssiteaddress, s.scontactperson, s.sphoneno, s.sfaxno, s.semail, s.ndefaultstatus,"
				+ " s.nismultisite, s.dmodifieddate, s.nmastersitecode, s.nstatus" + " from site s where s.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = "
				+ objUsersSite.getNsitecode();
		final Site objSite = (Site) jdbcUtilityFunction.queryForObject(queryString, Site.class, jdbcTemplate);
		final List<Object> savedClientList = new ArrayList<>();
		if (objSite != null) {
			objUsersSite.setNusercode(objUsers.getNusercode());
			objUsersSite.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
			objUsersSite.setNsitecode(objSite.getNsitecode());
			objUsersSite.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

			final String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userssite' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;

			final String insertQuery = "insert into userssite( nusersitecode, nusercode, ndefaultsite, dmodifieddate, nsitecode, nstatus) "
					+ " values(" + nsequenceNo + ", " + objUsersSite.getNusercode() + ", "
					+ objUsersSite.getNdefaultsite() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ objUsersSite.getNsitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
					+ " where stablename='userssite' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQuery);

			objUsersSite.setNusersitecode(nsequenceNo);

		}
		savedClientList.add(objUsersSite);
		auditUtilityFunction.fnInsertAuditAction(savedClientList, 1, null, Arrays.asList("IDS_ADDUSERSITE"), userInfo);
		return objUsersSite;
	}

	/**
	 * Creates a new UserMultiRole record after performing multiple validations including:
	 * - Active user verification
	 * - Role duplication check
	 * - Password policy application based on login type
	 * - Default role conflict and session status check
	 * - Audit logging and sequence management
	 *
	 * If the default role is already assigned and the existing user is active in session, returns a conflict error.
	 * Otherwise, disables the previous default role and sets the new role as default.
	 *
	 * @param objUserMultiRole Object containing multi-role details to be created.
	 *                         Used keys: nusersitecode, nuserrolecode, ndefaultrole, spassword, dpasswordvalidatedate
	 *                         Set keys: nnooffailedattempt, dmodifieddate, nsitecode, nusermultirolecode
	 *
	 * @param userInfo Object containing session-related details.
	 *                 Used keys: nmastersitecode, slanguagefilename
	 *                 Used for: fetching password policy, site code, language preference, audit context
	 *
	 * @param objUsers Object containing user login type and primary user code.
	 *                 Used keys: nlogintypecode, nusercode
	 *
	 * @return ResponseEntity with the result:
	 *         - 200 OK and updated user multi-role list on success
	 *         - 417 Expectation Failed if user is deleted or retired
	 *         - 409 Conflict if the role already exists or user is actively logged in with default role
	 *
	 * @throws Exception if any SQL, query, or business logic validation fails
	 */
	@Override
	public ResponseEntity<Object> createUserMultiRole(UserMultiRole objUserMultiRole, final UserInfo userInfo,
			Users objUsers) throws Exception {

		final String sQueryLock = " lock  table lockusers " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final Users userById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final String queryString = "select nusermultirolecode from usermultirole where nusersitecode = "
						+ objUserMultiRole.getNusersitecode() + " and " + "nuserrolecode = "
						+ objUserMultiRole.getNuserrolecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				final UserMultiRole userRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(queryString,
						UserMultiRole.class, jdbcTemplate);
				if (userRole == null) {
					final List<Object> savedList = new ArrayList<>();
					if (objUsers.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
						final String qry = "select pp.nnooffailedattempt "
								+ "from passwordpolicy pp,userrolepolicy urp "
								+ "where pp.npolicycode = urp.npolicycode " + "and pp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "and urp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " " + "and urp.ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
								+ "and urp.nuserrolecode =  " + objUserMultiRole.getNuserrolecode()
								+ " and pp.nsitecode = " + userInfo.getNmastersitecode() + " and urp.nsitecode = "
								+ userInfo.getNmastersitecode();
						final PasswordPolicy passwordPolicy = (PasswordPolicy) jdbcUtilityFunction.queryForObject(qry,
								PasswordPolicy.class, jdbcTemplate);
						objUserMultiRole.setNnooffailedattempt((short) passwordPolicy.getNnooffailedattempt());

					} else {
						objUserMultiRole.setNnooffailedattempt((short) 9);
					}
					String defaultRoleQuery = "select  um.nusermultirolecode, um.nusersitecode, um.nuserrolecode, um.ndefaultrole,"
							+ " um.ntransactionstatus, um.spassword, um.dpasswordvalidatedate, um.nnooffailedattempt,"
							+ " um.dmodifieddate, um.nsitecode, um.nstatus"
							+ " from usermultirole um where um.ndefaultrole = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and um.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and um.nusersitecode = "
							+ objUserMultiRole.getNusersitecode() + " and um.nsitecode = "
							+ userInfo.getNmastersitecode();
					final UserMultiRole defaultRole = (UserMultiRole) jdbcUtilityFunction
							.queryForObject(defaultRoleQuery, UserMultiRole.class, jdbcTemplate);
					if (objUserMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						if (defaultRole != null) {
							final String sessionVal = " select u.nusercode from userssite us,users u,sessiondetails sd where "
									+ " u.nusercode = us.nusercode and sd.nusercode = u.nusercode and"
									+ " us.nusersitecode = " + objUserMultiRole.getNusersitecode()
									+ " and sd.nstatus = u.nstatus and u.nstatus = us.nstatus" + " and u.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " group by u.nusercode ";

							Users sessionValObj = (Users) jdbcUtilityFunction.queryForObject(sessionVal, Users.class,
									jdbcTemplate);
							if (sessionValObj == null) {
								final UserMultiRole defaultRoleBeforeSave = SerializationUtils.clone(defaultRole);
								final List<Object> defaultListBeforeSave = new ArrayList<>();
								defaultListBeforeSave.add(defaultRoleBeforeSave);
								defaultRole.setNdefaultrole(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
								final String resetPwdQuery = "update usermultirole set dmodifieddate = '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo)
										+ "', spassword = NULL, ndefaultrole = "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nusersitecode = " + objUserMultiRole.getNusersitecode();

								jdbcTemplate.execute(resetPwdQuery);

								final List<Object> defaultListAfterSave = new ArrayList<>();
								defaultListAfterSave.add(defaultRole);
								auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
										Arrays.asList("IDS_EDITUSERROLE"), userInfo);
							} else {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERALREDYLOGIN",
										userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
							}

						}
					} else {
						if (defaultRole == null) {
							objUserMultiRole
									.setNdefaultrole((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						}
					}
					objUserMultiRole.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
					objUserMultiRole.setNsitecode(userInfo.getNmastersitecode());

					final String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='usermultirole' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
					nsequenceNo++;

					final String sPassword = objUserMultiRole.getSpassword() != null
							? "N'" + stringUtilityFunction.replaceQuote(objUserMultiRole.getSpassword()) + "'"
							: null;
					String passwordValidateDate = null;
					if (objUserMultiRole.getDpasswordvalidatedate() != null) {
						final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						passwordValidateDate = sdFormat.format(objUserMultiRole.getDpasswordvalidatedate());
						objUserMultiRole.setDpasswordvalidatedate(sdFormat.parse(passwordValidateDate));
						passwordValidateDate= "N'"+passwordValidateDate+"' :: timestamp";
					}

					final String insertQuery = "insert into usermultirole( nusermultirolecode, nusersitecode, nuserrolecode, ndefaultrole, ntransactionstatus, spassword, dpasswordvalidatedate, nnooffailedattempt, dmodifieddate, nsitecode, nstatus) "
							+ " values(" + nsequenceNo + ", " + objUserMultiRole.getNusersitecode() + ", "
							+ objUserMultiRole.getNuserrolecode() + ", " + objUserMultiRole.getNdefaultrole() + ", "
							+ objUserMultiRole.getNtransactionstatus() + ", " + sPassword + ", " + passwordValidateDate
							+ ", " + objUserMultiRole.getNnooffailedattempt() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertQuery);

					final String updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
							+ " where stablename='usermultirole' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateQuery);

					objUserMultiRole.setNusermultirolecode(nsequenceNo);

					savedList.add(objUserMultiRole);
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, Arrays.asList("IDS_ADDUSERROLE"),
							userInfo);
					return getUserMultiRole(objUserMultiRole.getNusersitecode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}

	}

	/**
	 * Retrieves an active user record from the database based on the provided user code and site-specific filters.
	 * Also fetches department, country, designation names and user image details by joining related tables.
	 *
	 * @param nuserCode The user code to search for. Acts as the primary key for filtering.
	 * @param userInfo Object containing session/site-related data.
	 *                 Used keys: ssitedate, slanguagetypecode, nmastersitecode
	 *                 Used for: language-specific transaction status labels and site-based filtering
	 *
	 * @return Users object populated with detailed user profile if active and found; otherwise, returns null
	 *
	 * @throws Exception if query execution or result mapping fails
	 */
	public Users getActiveUserById(final int nuserCode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select us.nusercode, us.ndeptcode, us.ncountrycode, us.ndesignationcode, us.nlockmode, us.nlogintypecode, us.ntransactionstatus, us.sloginid,"
				+ " us.sempid, us.sfirstname, us.slastname, us.sbloodgroup, us.squalification, us.ddateofjoin, us.smobileno, us.sphoneno, us.semail,"
				+ " us.sjobdescription, us.saddress1, us.saddress2, us.saddress3, us.dmodifieddate, us.nsitecode, us.nstatus, "
				+ "CONCAT(us.sfirstname,' ',us.slastname) as susername, TO_CHAR(us.ddateofjoin,'"
				+ userInfo.getSsitedate() + "') as sdateofjoin," + " coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus ,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as slockstatus ,"
				+ " d.sdeptname as sdeptname,  c.scountryname as scountryname, desg.sdesignationname as sdesignationname "
				+ " , COALESCE(uf.ssignimgname,'') as ssignimgname, "
				+ " uf.ssignimgftp , COALESCE(uf.suserimgname,'') as suserimgname, uf.suserimgftp "
				+ " from users us left join userfile uf on us.nusercode = uf.nusercode and uf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and uf.nsitecode = " + userInfo.getNmastersitecode()
				+ " , transactionstatus ts1, transactionstatus ts2, department d, country c, designation desg "
				+ " where us.ntransactionstatus = ts1.ntranscode and us.nlockmode = ts2.ntranscode and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndeptcode = us.ndeptcode "
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncountrycode = us.ncountrycode " + " and desg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and d.nsitecode = " + userInfo.getNmastersitecode() + " and c.nsitecode = "
				+ userInfo.getNmastersitecode() + " and desg.nsitecode = " + userInfo.getNmastersitecode()
				+ " and desg.ndesignationcode = us.ndesignationcode " + " and us.nusercode = " + nuserCode;
		return (Users) jdbcUtilityFunction.queryForObject(strQuery, Users.class, jdbcTemplate);
	}

	/**
	 * Retrieves an active UserMultiRole record based on its primary key (multi-role code).
	 *
	 * @param nuserMultiRoleCode The primary key identifying the specific multi-role entry.
	 *
	 * @return UserMultiRole object if found and active; null if not present
	 *
	 * @throws Exception if query execution or result mapping fails
	 */
	@Override
	public UserMultiRole getActiveUserMultiRoleById(final int nuserMultiRoleCode) throws Exception {
		final String strQuery = " select nusermultirolecode, nusersitecode, nuserrolecode, ndefaultrole, ntransactionstatus,"
				+ " spassword, dpasswordvalidatedate, nnooffailedattempt, dmodifieddate, nsitecode, nstatus from usermultirole "
				+ " where nusermultirolecode = " + nuserMultiRoleCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (UserMultiRole) jdbcUtilityFunction.queryForObject(strQuery, UserMultiRole.class, jdbcTemplate);
	}

	/**
	 * Updates an existing user multi-role configuration with the provided data.
	 *
	 * This method includes multiple validations and business rules such as:
	 * - Verifying if the user exists and is not retired.
	 * - Ensuring the multi-role record to be updated is active.
	 * - Preventing deactivation of the default role.
	 * - Ensuring at least one active default role exists for the user site.
	 * - Avoiding duplicate role assignments (same role and site).
	 * - Reassigning the default role only if no active session exists for the user.
	 * - Applying failed attempt limits from password policy for internal login users.
	 * - Updating the record and logging the changes in the audit trail.
	 *
	 * @param userMultiRole The user multi-role details to be updated.
	 *                      Keys used: nusermultirolecode, nusersitecode, nuserrolecode,
	 *                      ndefaultrole, ntransactionstatus, spassword
	 * @param userInfo The logged-in user's information.
	 *                 Keys used: nmastersitecode, slanguagefilename
	 * @param objUsers The user whose multi-role is being updated.
	 *                 Keys used: nusercode, nlogintypecode
	 * @return ResponseEntity containing the updated user multi-role details on success,
	 *         or an error message and status in case of validation or conflict.
	 * @throws Exception if any system or database error occurs during the process.
	 */
	@Override
	public ResponseEntity<Object> updateUserMultiRole(UserMultiRole userMultiRole, UserInfo userInfo, Users objUsers)
			throws Exception {
		final Users userById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final UserMultiRole roleById = getActiveUserMultiRoleById(userMultiRole.getNusermultirolecode());
				if (roleById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (userMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.YES.gettransactionstatus()
							&& userMultiRole.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE
									.gettransactionstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.INACTIVEDEFAULTROLE.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String roleQuery = "select nusermultirolecode, nusersitecode, nuserrolecode, ndefaultrole, ntransactionstatus, spassword, "
								+ "dpasswordvalidatedate, nnooffailedattempt, dmodifieddate, nsitecode, nstatus "
								+ "from usermultirole where nusersitecode = " + userMultiRole.getNusersitecode()
								+ " and ndefaultrole = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						final UserMultiRole role = (UserMultiRole) jdbcUtilityFunction.queryForObject(roleQuery,
								UserMultiRole.class, jdbcTemplate);
						if (role != null && role.getNusermultirolecode() == userMultiRole.getNusermultirolecode()
								&& userMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.NO
										.gettransactionstatus()) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.DEFAULTUSERROLEMUSTBETHERE.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							final String duplicateCheckQuery = "select nusermultirolecode, nusersitecode, nuserrolecode, ndefaultrole, ntransactionstatus, spassword,"
									+ " dpasswordvalidatedate, nnooffailedattempt, dmodifieddate, nsitecode, nstatus from usermultirole"
									+ " where nusersitecode = " + userMultiRole.getNusersitecode()
									+ " and nuserrolecode = " + userMultiRole.getNuserrolecode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nusermultirolecode <>" + userMultiRole.getNusermultirolecode()
									+ " and nsitecode = " + userInfo.getNmastersitecode();

							final UserMultiRole duplicateRole = (UserMultiRole) jdbcUtilityFunction
									.queryForObject(duplicateCheckQuery, UserMultiRole.class, jdbcTemplate);
							if (duplicateRole == null) {
								final List<Object> listBeforeUpdate = new ArrayList<>();
								final List<Object> listAfterUpdate = new ArrayList<>();
								final List<String> multilingualIDList = new ArrayList<>();
								if (userMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {
									if (role != null
											&& role.getNusermultirolecode() != userMultiRole.getNusermultirolecode()) {
										final String sessionVal = "select u.nusercode from userssite us,users u,sessiondetails sd where "
												+ " u.nusercode = us.nusercode and sd.nusercode = u.nusercode and"
												+ " us.nusersitecode = " + userMultiRole.getNusersitecode()
												+ " and sd.nstatus = u.nstatus and u.nstatus = us.nstatus"
												+ " and u.nstatus = "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and u.nsitecode = " + userInfo.getNmastersitecode()
												+ " group by u.nusercode ";
										final Users sessionValObj = (Users) jdbcUtilityFunction.queryForObject(sessionVal,
												Users.class, jdbcTemplate);
										if (sessionValObj == null) {
											final UserMultiRole defaultRoleBeforeSave = SerializationUtils.clone(role);
											listBeforeUpdate.add(defaultRoleBeforeSave);
											role.setNdefaultrole(
													(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
											final String updateQueryString = "update usermultirole set dmodifieddate = '"
													+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
													+ " ndefaultrole = "
													+ Enumeration.TransactionStatus.NO.gettransactionstatus()
													+ " where nusermultirolecode  = " + role.getNusermultirolecode();
											jdbcTemplate.execute(updateQueryString);
											listAfterUpdate.add(role);
											multilingualIDList.add("IDS_EDITUSERROLE");
										} else {
											return new ResponseEntity<>(
													commonFunction.getMultilingualMessage("IDS_USERALREDYLOGIN",
															userInfo.getSlanguagefilename()),
													HttpStatus.CONFLICT);
										}

									}
								}
								if (objUsers.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
									final String qry = "select pp.nnooffailedattempt "
											+ "from passwordpolicy pp,userrolepolicy urp "
											+ "where pp.npolicycode = urp.npolicycode " + "and pp.nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											+ "and urp.nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
											+ "and urp.ntransactionstatus = "
											+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
											+ " and pp.nsitecode = " + userInfo.getNmastersitecode()
											+ " and urp.nsitecode = " + userInfo.getNmastersitecode()
											+ " and urp.nuserrolecode =  " + userMultiRole.getNuserrolecode();

									final PasswordPolicy passwordPolicy = (PasswordPolicy) jdbcUtilityFunction
											.queryForObject(qry, PasswordPolicy.class, jdbcTemplate);
									userMultiRole.setNnooffailedattempt((short) passwordPolicy.getNnooffailedattempt());

								} else {
									userMultiRole.setNnooffailedattempt((short) 9);
								}

								String updateQueryString = "update usermultirole set dmodifieddate = '"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nusersitecode = "
										+ userMultiRole.getNusersitecode() + ", nuserrolecode = "
										+ userMultiRole.getNuserrolecode() + ", ndefaultrole = "
										+ userMultiRole.getNdefaultrole() + ", nnooffailedattempt = "
										+ userMultiRole.getNnooffailedattempt() + ", ntransactionstatus = "
										+ userMultiRole.getNtransactionstatus() + " where nusermultirolecode = "
										+ userMultiRole.getNusermultirolecode();

								if (userMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()
										&& roleById.getNdefaultrole() != userMultiRole.getNdefaultrole()) {
									userMultiRole.setSpassword(null);
									updateQueryString = "update usermultirole set dmodifieddate = '"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nusersitecode = "
											+ userMultiRole.getNusersitecode() + ", nuserrolecode = "
											+ userMultiRole.getNuserrolecode() + ", ndefaultrole = "
											//BGSI-15-->Added by Vignesh(23-07-2025)--Can't able to login with newly created user 
											+ userMultiRole.getNdefaultrole() + ", spassword = "
											+ userMultiRole.getSpassword()
											+ ", nnooffailedattempt = " + userMultiRole.getNnooffailedattempt()
											+ ", ntransactionstatus = " + userMultiRole.getNtransactionstatus()
											+ " where nusermultirolecode = " + userMultiRole.getNusermultirolecode();
								}
								jdbcTemplate.execute(updateQueryString);
								listAfterUpdate.add(userMultiRole);
								listBeforeUpdate.add(roleById);
								multilingualIDList.add("IDS_EDITUSERROLE");
								auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
										multilingualIDList, userInfo);
								return getUserMultiRole(userMultiRole.getNusersitecode(), userInfo);
							} else {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage(
										Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
										userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes a user from the system after performing multiple validations and dependency checks.
	 *
	 * This method performs the following operations:
	 * - Validates if the user exists and is active.
	 * - Verifies that the user is an internal user and not already retired.
	 * - Checks for active sessions to prevent deletion of logged-in users.
	 * - Validates if the user is involved in dependent modules such as deputy mapping, 
	 *   organization roles, user mappings, project assignments, screen hiding, LIMS mapping, and project master.
	 * - Performs one-to-many validation for deletion of the user record.
	 * - If all validations pass:
	 *     - Deletes user data from users, usersite, and userfile tables by marking them as deleted.
	 *     - Fetches all related multi-role and deputy-role data for audit logging.
	 *     - Records audit actions for deleted entities using multilingual identifiers.
	 * - Returns the updated user list or an appropriate error message depending on validation results.
	 *
	 * @param objUsers Object containing the user information to be deleted.
	 *                 Keys used: nusercode
	 * @param userInfo Information about the currently logged-in user.
	 *                 Keys used: slanguagefilename, nmastersitecode, ntranssitecode
	 * @return ResponseEntity containing the updated user list after successful deletion,
	 *         or an error message if the user is already retired, logged in, used in related modules,
	 *         or has any active dependent records.
	 * @throws Exception if any database or system error occurs during the process.
	 */
	@Override
	public ResponseEntity<Object> deleteUsers(Users objUsers, final UserInfo userInfo) throws Exception {
		final Users activeUserById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (activeUserById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (activeUserById.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
				if (activeUserById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
						.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					// ALPD-3481
					final String sessionVal = "select nusercode from sessiondetails where nusercode = "
							+ objUsers.getNusercode() + " and " + "nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final List<Users> sessionValObj = (List<Users>) jdbcTemplate.query(sessionVal, new Users());

					if (!sessionValObj.isEmpty()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERALREDYLOGINDELETE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

					final String query = " Select 'IDS_USEDASDEPUTYUSER' as Msg from usermultideputy umd, userssite us where "
							+ " umd.ndeputyusersitecode = us.nusersitecode and umd.nstatus =  "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nusercode = "
							+ objUsers.getNusercode() + " union all "
							+ " select 'IDS_ORGANISATION' as Msg from sectionusers su where su.nusercode = "
							+ objUsers.getNusercode() + " and su.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
							+ " select 'IDS_USERMAPPING' as Msg from usermapping um where um.nchildusercode = "
							+ objUsers.getNusercode() + " and um.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
							+ "select  'IDS_PROJECTMEMBER' as Msg from  projectmember pm where " + "  pm.nusercode = "
							+ objUsers.getNusercode() + " " + "  and pm.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pm.nsitecode = "
							+ userInfo.getNtranssitecode() + " union all "
							+ " select 'IDS_USERSCREENHIDE' as Msg from usersrolescreenhide  where nuserrolecode = "
							+ objUsers.getNusercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " union all  select 'IDS_LIMSELNUSERMAPPING' as Msg from limselnusermapping where nlimsusercode = "
							+ objUsers.getNusercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " union all select  'IDS_PROJECTMASTER' as Msg from  projectmaster pm where"
							+ "  pm.nusercode = " + objUsers.getNusercode() + " " + "  and pm.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pm.nsitecode ="
							+ userInfo.getNtranssitecode()
							+ " union all select  'IDS_BIOPROJECT' as Msg from  bioproject bp where "
							+ "bp.nusercode = " + objUsers.getNusercode() + " and bp.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

					validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
					boolean validRecord = false;
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						validRecord = true;
						Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
						objOneToManyValidation.put("primaryKeyValue", Integer.toString(objUsers.getNusercode()));
						objOneToManyValidation.put("stablename", "users");
						validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
						if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
							validRecord = true;
						} else {
							validRecord = false;
						}
					}

					if (validRecord) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> savedList = new ArrayList<>();
						final String siteQuery = " Select nusersitecode, nusercode, ndefaultsite, dmodifieddate, nsitecode, nstatus from userssite where nusercode = "
								+ objUsers.getNusercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<UsersSite> userSiteList = (List<UsersSite>) jdbcTemplate.query(siteQuery,
								new UsersSite());

						final String userFileQuery = "select nuserfilecode, nusercode, ssignimgname, ssignimgftp, suserimgname, suserimgftp, dmodifieddate, nsitecode, nstatus from userfile where nusercode = "
								+ objUsers.getNusercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
								+ userInfo.getNmastersitecode();
						final UserFile objUserFile = (UserFile) jdbcUtilityFunction.queryForObject(userFileQuery,
								UserFile.class, jdbcTemplate);

						final String userMultirole = "select um.nusermultirolecode, um.nusersitecode, um.nuserrolecode, um.ndefaultrole, um.ntransactionstatus, um.spassword, um.dpasswordvalidatedate, um.nnooffailedattempt, um.nstatus, um.dmodifieddate, um.nsitecode, u.sloginid, u.nusercode, u.nlockmode, u.sfirstname, u.slastname from users u,userssite us,usermultirole um where u.nusercode = us.nusercode"
								+ " and us.nusersitecode = um.nusersitecode and  us.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and "
								+ " um.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nusercode = " + objUsers.getNusercode() + " and u.nsitecode = "
								+ userInfo.getNmastersitecode() + " and um.nsitecode = "
								+ userInfo.getNmastersitecode();
						final List<UserMultiRole> userMultiRole = (List<UserMultiRole>) jdbcTemplate
								.query(userMultirole, new UserMultiRole());

						final String userDeputyrole = " select um.nusermultideputycode, um.nusersitecode, um.ndeputyusersitecode, um.nuserrolecode, um.ntransactionstatus, um.nstatus, um.nsitecode, um.dmodifieddate, u.nusercode from users u,userssite us,usermultideputy um where u.nusercode = us.nusercode"
								+ " and us.nusersitecode = um.nusersitecode and  us.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and "
								+ " um.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nusercode = " + objUsers.getNusercode() + " and u.nsitecode = "
								+ userInfo.getNmastersitecode() + " and um.nsitecode = "
								+ userInfo.getNmastersitecode();
						final List<UserMultiDeputy> userMultiDeputy = (List<UserMultiDeputy>) jdbcTemplate
								.query(userDeputyrole, new UserMultiDeputy());

						final String userDeleteQuery = "update users set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nusercode = "
								+ objUsers.getNusercode() + ";" + "update userssite set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nusercode = "
								+ objUsers.getNusercode() + ";" + "update userfile set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus  = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nusercode = "
								+ objUsers.getNusercode() + " and nsitecode = " + userInfo.getNmastersitecode() + ";";

						jdbcTemplate.execute(userDeleteQuery);
						
						final String homePath = ftpUtilityFunction.getFileAbsolutePath();
						final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
								.getUploadControlsByFormCode(userInfo).getBody();
						final Map<String, Object> imageMap = new HashMap<String, Object>();

						if (objUserFile.getSsignimgftp() != null && 
								(objUserFile.getSsignimgftp() != "" || !objUserFile.getSsignimgftp().isEmpty())) {
							final String absolutePath = System.getenv(homePath) + Enumeration.FTP.USERSIGN_PATH.getFTP() + "\\"
									+ objUserFile.getSsignimgftp();
							final File file = new File(absolutePath);
							file.delete();
							imageMap.put("SignImage_fileName", objUserFile.getSsignimgftp());
						}
						if (objUserFile.getSuserimgftp() != null && 
								(objUserFile.getSuserimgftp() == "" || !objUserFile.getSuserimgftp().isEmpty())) {
							final String absolutePath = System.getenv(homePath)
									+ Enumeration.FTP.USERPROFILE_PATH.getFTP() + "\\" + objUserFile.getSuserimgftp();
							final File file = new File(absolutePath);
							file.delete();
							imageMap.put("UserImage_fileName", objUserFile.getSuserimgftp());
						}

						if (!imageMap.isEmpty()) {
							ftpUtilityFunction.multiPathDeleteFTPFile(imageMap, controlList, userInfo);
						}
						
						objUsers.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
						savedList.add(objUsers);
						final List<Object> auditActionList = new ArrayList<>();
						auditActionList.add(savedList);
						auditActionList.add(userSiteList);
						auditActionList.add(userMultiRole);
						auditActionList.add(userMultiDeputy);
						multilingualIDList.add("IDS_DELETEUSERS");
						multilingualIDList.add("IDS_DELETEUSERSITE");
						multilingualIDList.add("IDS_DELETEUSERROLE");
						multilingualIDList.add("IDS_DELETEDEPUTYUSER");
						if (objUserFile != null) {
							auditActionList.add(Arrays.asList(objUserFile));
							multilingualIDList.add("IDS_DELETEUSERFILE");
						}
						auditUtilityFunction.fnInsertListAuditAction(auditActionList, 1, null, multilingualIDList,
								userInfo);
						return getUsers(null, null, userInfo);

					} else {
						return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETEADSUSER",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * Deletes a user multi-role mapping after performing dependency and status validations.
	 *
	 * This method verifies the existence and active status of the user, checks whether the multi-role mapping 
	 * is active and not marked as a default role, and ensures there are no dependent mappings in the 
	 * user deputy and user mapping tables. If valid, the user multi-role record is marked as deleted, 
	 * audit log is recorded, and updated multi-role list is returned.
	 *
	 * @param userMultiRole The user multi-role information. Keys used:
	 *                      nusermultirolecode - ID of the multi-role record,
	 *                      nuserrolecode - role code,
	 *                      nusersitecode - site code,
	 *                      ndefaultrole - flag to identify default role.
	 * @param userInfo Contains session and site context. Keys used:
	 *                 nmastersitecode - site code for validations,
	 *                 slanguagefilename - language for multilingual messages.
	 * @param objUsers The user to validate. Key used:
	 *                 nusercode - user code to fetch and validate active user.
	 * @return ResponseEntity with updated user multi-role data or appropriate failure message if validations fail.
	 * @throws Exception If any validation, database operation, or audit logging fails.
	 */
	@Override
	public ResponseEntity<Object> deleteUserMultiRole(final UserMultiRole userMultiRole, final UserInfo userInfo,
			final Users objUsers) throws Exception {
		final Users userById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final UserMultiRole activeUserMultiRole = getActiveUserMultiRoleById(
						userMultiRole.getNusermultirolecode());
				if (activeUserMultiRole == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (userMultiRole.getNdefaultrole() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final String query = "Select 'IDS_DEPUTYUSER' as Msg from usermultideputy Where nuserrolecode = "
								+ userMultiRole.getNuserrolecode() + " and nusersitecode = "
								+ userMultiRole.getNusersitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
								+ userInfo.getNmastersitecode() + " union all "
								+ " Select 'IDS_USERMAPPING' as Msg from usermapping Where nchildrolecode = "
								+ userMultiRole.getNuserrolecode() + " and nchildusersitecode = "
								+ userMultiRole.getNusersitecode() + "  and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by nchildusersitecode, nchildrolecode";
						validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
						boolean validRecord = false;
						if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
							validRecord = true;
							validatorDel = projectDAOSupport
									.validateDeleteRecord(Integer.toString(userMultiRole.getNusersitecode()), userInfo);
							if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS
									.getReturnvalue()) {
								validRecord = true;
							} else {
								validRecord = false;
							}
						}

						if (validRecord) {
							final List<String> multilingualIDList = new ArrayList<>();
							final List<Object> savedList = new ArrayList<>();
							final String deleteQuery = "update usermultirole set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where nusermultirolecode = " + userMultiRole.getNusermultirolecode()
									+ " and nsitecode = " + userInfo.getNmastersitecode();
							jdbcTemplate.execute(deleteQuery);
							userMultiRole
									.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							savedList.add(userMultiRole);
							multilingualIDList.add("IDS_DELETEUSERROLE");
							auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
							return getUserMultiRole(userMultiRole.getNusersitecode(), userInfo);
						} else {
							return new ResponseEntity<>(validatorDel.getSreturnmessage(),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTROLECANNOTBEDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

				}
			}
		}
	}

	/**
	 * Retires an active internal user after checking user status, login type, and session activity.
	 *
	 * The method validates whether the user exists, confirms the user is of internal login type, checks
	 * if the user is not already retired, and ensures the user is not currently logged in. If all conditions 
	 * are met, the user is marked as retired, audit log is recorded, and updated user information is returned.
	 *
	 * @param objUsers The user to be retired. Key used:
	 *                 nusercode - ID of the user to retire.
	 * @param userInfo Contains the session and site context. Keys used:
	 *                 nmastersitecode - site code for validation,
	 *                 slanguagefilename - language for multilingual messages,
	 *                 ssitedate - format for displaying the date of join.
	 * @return ResponseEntity containing the updated user data or appropriate error message if validations fail.
	 * @throws Exception If a validation error, session check, or database update fails.
	 */
	@Override
	public ResponseEntity<Object> retireUser(final Users objUsers, final UserInfo userInfo) throws Exception {
		final String queryString = "select u.nusercode, u.ndeptcode, u.ncountrycode, u.ndesignationcode, u.nlockmode, u.nlogintypecode, u.ntransactionstatus,"
				+ " u.sloginid, u.sempid, u.sfirstname, u.slastname, u.sbloodgroup, u.squalification, u.ddateofjoin, u.smobileno, u.sphoneno,"
				+ " u.semail, u.sjobdescription, u.saddress1, u.saddress2, u.saddress3, u.dmodifieddate, u.nsitecode, u.nstatus,"
				+ "	CONCAT(u.sfirstname,' ',u.slastname) as susername,  TO_CHAR(u.ddateofjoin,'"
				+ userInfo.getSsitedate()
				+ "') as sdateofjoin, uf.ssignimgname, uf.ssignimgftp, uf.suserimgname, uf.suserimgftp from users u,"
				+ " userfile uf where u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and uf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nusercode = " + objUsers.getNusercode() + " and u.nusercode = uf.nusercode"
				+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " and uf.nsitecode = "
				+ userInfo.getNmastersitecode();
		final Users userById = (Users) jdbcUtilityFunction.queryForObject(queryString, Users.class, jdbcTemplate);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
				if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String sessionVal = "select nusercode from sessiondetails where nusercode = "
							+ userById.getNusercode() + " and " + "nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final List<Users> sessionValObj = (List<Users>) jdbcTemplate.query(sessionVal, new Users());

					if (!sessionValObj.isEmpty()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERALREDYLOGINRETIRE",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
					final String updateQuery = "update users set dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " where nusercode = "
							+ objUsers.getNusercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
							+ userInfo.getNmastersitecode();
					jdbcTemplate.execute(updateQuery);
					objUsers.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(userById);
					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(objUsers);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
							Arrays.asList("IDS_USERRETIRED"), userInfo);
					return getUsers(objUsers.getNusercode(), null, userInfo);

				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTRETIREADSUSER",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * Resets the password for an internal user after verifying active status, role defaults, and session.
	 *
	 * This method performs several checks before resetting the password:
	 * - Verifies the user is internal and not retired.
	 * - Confirms the user has a default multi-role mapping.
	 * - Ensures the user is not currently logged in.
	 * If valid, the password is set to null in the default multi-role record and an audit log is recorded.
	 *
	 * @param userSiteCode The user site code to find the default multi-role mapping.
	 * @param objUsers The user whose password is being reset. Key used:
	 *                 nusercode - user code of the target user,
	 *                 sloginid - login ID for audit comment,
	 *                 susername - user name for audit comment.
	 * @param userInfo The session user information. Keys used:
	 *                 nmastersitecode - for site-level filtering,
	 *                 slanguagefilename - for multilingual messages.
	 * @return ResponseEntity with multilingual success message or specific error message on failure.
	 * @throws Exception If validation or database operation fails.
	 */
	@Override
	public ResponseEntity<Object> resetPassword(final int userSiteCode, final Users objUsers, final UserInfo userInfo)
			throws Exception {
		String queryString = "select nlogintypecode, ntransactionstatus from users where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nusercode = "
				+ objUsers.getNusercode() + " and nsitecode = " + userInfo.getNmastersitecode();
		final Users userById = (Users) jdbcUtilityFunction.queryForObject(queryString, Users.class, jdbcTemplate);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
				if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					queryString = "select nusermultirolecode from usermultirole where  nusersitecode = " + userSiteCode
							+ " and ndefaultrole = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = " + userInfo.getNmastersitecode();
					final UserMultiRole defaultRole = (UserMultiRole) jdbcUtilityFunction.queryForObject(queryString,
							UserMultiRole.class, jdbcTemplate);
					if (defaultRole == null) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTUSERROLEMUSTBETHERE.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String sessionVal = "select nusercode from sessiondetails where nusercode = "
								+ objUsers.getNusercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						List<Users> objUserMultiDeputy = (List<Users>) jdbcTemplate.query(sessionVal, new Users());
						if (!objUserMultiDeputy.isEmpty()) {

							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_USERALREDYLOGINPASSWORDRESET",
											userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
						queryString = "update usermultirole set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "', spassword = null where nusermultirolecode = "
								+ defaultRole.getNusermultirolecode();
						jdbcTemplate.execute(queryString);
						final List<String> listOfMultiLingual = commonFunction.getMultilingualMultipleMessage(
								Arrays.asList("IDS_PASSWORDRESET", "IDS_LOGINID", "IDS_USERNAME", "IDS_USERROLE"),
								userInfo.getSlanguagefilename());
						final String scomments = listOfMultiLingual.get(0) + ": " + listOfMultiLingual.get(1) + ": "
								+ objUsers.getSloginid() + ";" + listOfMultiLingual.get(2) + ": "
								+ objUsers.getSusername();
						Map<String, Object> map = new HashMap<>();
						map.put("sprimarykeyvalue", defaultRole.getNusermultirolecode());
						map.put("stablename", "usermultirole");
						auditUtilityFunction.insertAuditAction(userInfo, "IDS_PASSWORDRESET", scomments, map);
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PASSWORDRESETSUCCESS",
								userInfo.getSlanguagefilename()), HttpStatus.ACCEPTED);

					}
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTRESETPWDADSUSER",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * Retrieves role and deputy information for a user site.
	 *
	 * This method first fetches the active user site by ID. If valid, it gathers and combines:
	 * - The user site information,
	 * - Deputy mappings for the user site,
	 * - Multi-role mappings for the user site.
	 *
	 * @param userSiteCode The user site code for which to retrieve role and deputy mappings.
	 * @param userInfo The session user context. Keys used:
	 *                 slanguagefilename - for multilingual error messages.
	 * @return ResponseEntity containing the user site, deputy list, and multi-role list, or error message if the user site is deleted.
	 * @throws Exception If site fetching or data aggregation fails.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getRoleDeputyByUserSite(final int userSiteCode, final UserInfo userInfo)
			throws Exception {
		final UsersSite objUsersSite = getActiveUsersSiteById(userSiteCode, userInfo);
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (objUsersSite == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_USERSITEALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("UsersSite", objUsersSite);
			outputMap.putAll(
					(Map<String, Object>) getUserMultiDeputy(objUsersSite.getNusersitecode(), userInfo).getBody());
			outputMap.putAll(
					(Map<String, Object>) getUserMultiRole(objUsersSite.getNusersitecode(), userInfo).getBody());
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * Retrieves the list of active sites not assigned to the given user.
	 *
	 * This method builds a query to return unassigned site information for a user, based on the master site and 
	 * excluding specific user site codes when applicable.
	 *
	 * @param objUsers The user object containing the ID. Key used:
	 *                 nusercode - the user whose unassigned sites are to be fetched.
	 * @param nuserSiteCode The current user site code. If -2, all unassigned sites are fetched; otherwise, sites excluding this code are returned.
	 * @param userInfo The session user details. Key used:
	 *                 nmastersitecode - for master site scope filtering.
	 * @return ResponseEntity containing the list of unassigned site objects.
	 * @throws Exception If query execution or database access fails.
	 */
	@Override
	public ResponseEntity<Object> getUnAssignedSiteListByUser(final Users objUsers, final int nuserSiteCode,
			final UserInfo userInfo) throws Exception {
		String queryString = "";
		if (nuserSiteCode == -2) {
			queryString = "select s.nsitecode, s.ntimezonecode, s.ndateformatcode, s.ssitename, s.ssitecode, s.ssiteaddress, s.scontactperson, s.sphoneno, s.sfaxno, s.semail, s.dmodifieddate, s.ndefaultstatus, s.nismultisite, s.nmastersitecode, s.nstatus, s.nregioncode, s.ndistrictcode "
					+ " from site s where s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nsitecode <> -1 and s.nmastersitecode = " + userInfo.getNmastersitecode()
					+ " and not exists (select 1 from userssite us "
					+ " where us.nsitecode = s.nsitecode and us.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nusercode = "
					+ objUsers.getNusercode() + ")";

		} else {
			queryString = "select s.nsitecode, s.ntimezonecode, s.ndateformatcode, s.ssitename, s.ssitecode, s.ssiteaddress, s.scontactperson, s.sphoneno, s.sfaxno, s.semail, s.dmodifieddate, s.ndefaultstatus, s.nismultisite, s.nmastersitecode, s.nstatus, s.nregioncode, s.ndistrictcode "
					+ " from site s where s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nsitecode <> -1 and s.nmastersitecode = " + userInfo.getNmastersitecode()
					+ " and not exists (select 1 from userssite us "
					+ " where us.nsitecode = s.nsitecode and us.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nusersitecode <> "
					+ nuserSiteCode + " and us.nusercode = " + objUsers.getNusercode() + ")";
		}
		return new ResponseEntity<>(jdbcTemplate.query(queryString, new Site()), HttpStatus.OK);
	}

	/**
	 * Creates a new user-site mapping entry for a user, after verifying active status, retirement, and duplication.
	 *
	 * Performs the following:
	 * - Validates the user is active and not retired.
	 * - Checks if a mapping with the same site and user already exists.
	 * - If `ndefaultsite` is set to YES, resets the default flag on any existing default site for the user.
	 * - Generates a new sequence number for `nusersitecode`, inserts the new mapping, and updates the sequence table.
	 * - Records the audit entry for site addition.
	 *
	 * @param objUsersSite The UsersSite object containing:
	 *                     nusercode - ID of the user,
	 *                     nsitecode - site to be linked,
	 *                     ndefaultsite - whether this is default site.
	 * @param userInfo The session user's context. Keys used:
	 *                 nmastersitecode - for site filtering,
	 *                 slanguagefilename - for multilingual messages.
	 * @return ResponseEntity with user information if added successfully, or conflict/error message if user is invalid or duplicate exists.
	 * @throws Exception If validation, DB queries, or audit insertion fails.
	 */
	@Override
	public ResponseEntity<Object> createUsersSite(UsersSite objUsersSite, final UserInfo userInfo) throws Exception {
		final Users activeUserById = getActiveUserById(objUsersSite.getNusercode(), userInfo);
		if (activeUserById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (activeUserById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String queryString = "select nusersitecode from userssite where nsitecode = "
						+ objUsersSite.getNsitecode() + " and " + "nusercode = " + objUsersSite.getNusercode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final UsersSite duplicateSite = (UsersSite) jdbcUtilityFunction.queryForObject(queryString,
						UsersSite.class, jdbcTemplate);
				if (duplicateSite == null) {
					final List<Object> savedList = new ArrayList<>();
					if (objUsersSite.getNdefaultsite() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String defaultRoleQuery = "select nusersitecode, nusercode, ndefaultsite, nsitecode, nstatus, dmodifieddate from userssite where ndefaultsite = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nusercode = "
								+ objUsersSite.getNusercode();
						final UsersSite defaultSite = (UsersSite) jdbcUtilityFunction.queryForObject(defaultRoleQuery,
								UsersSite.class, jdbcTemplate);
						if (defaultSite != null) {
							final UsersSite defaultSiteBeforeSave = SerializationUtils.clone(defaultSite);
							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(defaultSiteBeforeSave);
							defaultSite
									.setNdefaultsite((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String resetDefaultSite = "update userssite set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ndefaultsite = "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nusersitecode = " + defaultSite.getNusersitecode();

							jdbcTemplate.execute(resetDefaultSite);
							final List<Object> defaultListAfterSave = new ArrayList<>();
							defaultListAfterSave.add(defaultSite);
							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
									Arrays.asList("IDS_EDITUSERSITE"), userInfo);

						}
					}
					objUsersSite.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));

					final String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userssite' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
					nsequenceNo++;

					final String insertQuery = "insert into userssite( nusersitecode, nusercode, ndefaultsite, dmodifieddate, nsitecode, nstatus)"
							+ " values(" + nsequenceNo + ", " + objUsersSite.getNusercode() + ", "
							+ objUsersSite.getNdefaultsite() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'," + objUsersSite.getNsitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertQuery);

					final String updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
							+ " where stablename='userssite' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(updateQuery);

					objUsersSite.setNusersitecode(nsequenceNo);

					savedList.add(objUsersSite);
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, Arrays.asList("IDS_ADDUSERSITE"),
							userInfo);
					return getUsers(objUsersSite.getNusercode(), null, userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	/**
	 * Retrieves the active user-site mapping record based on site ID and master site code.
	 *
	 * Fetches a joined record from `userssite` and `site` where both records are active and belong to the same master site.
	 *
	 * @param nuserSiteCode The ID of the user-site mapping to fetch.
	 * @param userInfo The session user's context. Key used:
	 *                 nmastersitecode - to ensure site belongs to the same master group.
	 * @return UsersSite object if a valid and active mapping exists; otherwise, null.
	 * @throws Exception If database access or query execution fails.
	 */
	@Override
	public UsersSite getActiveUsersSiteById(final int nuserSiteCode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select us.nusersitecode, us.nusercode, us.ndefaultsite, us.dmodifieddate, us.nsitecode, us.nstatus, s.ssitename as ssitename from userssite us, site s where us.nsitecode = s.nsitecode and "
				+ " us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nusersitecode = "
				+ nuserSiteCode + " and s.nmastersitecode = " + userInfo.getNmastersitecode();
		return (UsersSite) jdbcUtilityFunction.queryForObject(strQuery, UsersSite.class, jdbcTemplate);
	}

	/**
	 * Updates the user's site assignment with new site details or default status.
	 * Performs validations to ensure the user and site mapping are active, and handles
	 * duplicate prevention and default site rules. If an existing default site is being
	 * changed, the old record is updated accordingly. All actions are logged in the audit.
	 *
	 * Parameters used from objUsersSite include:
	 * - nusersitecode: ID of the user-site mapping to update
	 * - nusercode: ID of the user
	 * - nsitecode: ID of the site to assign
	 * - ndefaultsite: flag indicating whether the site is default (1 = yes, 0 = no)
	 *
	 * @param objUsersSite Object containing updated user-site assignment information
	 * @param userInfo Information about the logged-in user
	 * @return ResponseEntity containing updated user role/deputy info or appropriate failure messages
	 * @throws Exception if any validation, query, or update operation fails
	 */
	@Override
	public ResponseEntity<? extends Object> updateUsersSite(UsersSite objUsersSite, UserInfo userInfo) throws Exception {
		final Users activeUserById = getActiveUserById(objUsersSite.getNusercode(), userInfo);
		if (activeUserById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (activeUserById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final UsersSite usersSiteById = getActiveUsersSiteById(objUsersSite.getNusersitecode(), userInfo);
				if (usersSiteById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String existingDefaultQuery = "select nusersitecode, nusercode, ndefaultsite, dmodifieddate, nsitecode, nstatus from userssite where nusercode = "
							+ objUsersSite.getNusercode() + " and ndefaultsite = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					final UsersSite existingDefaultSite = (UsersSite) jdbcUtilityFunction
							.queryForObject(existingDefaultQuery, UsersSite.class, jdbcTemplate);
					if (existingDefaultSite.getNusersitecode() == objUsersSite.getNusersitecode() && objUsersSite
							.getNdefaultsite() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String duplicateCheckQuery = "select nusersitecode from userssite where nsitecode = "
								+ objUsersSite.getNsitecode() + " and " + "nusercode = " + objUsersSite.getNusercode()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nusersitecode <>" + objUsersSite.getNusersitecode();
						final UsersSite duplicateSite = (UsersSite) jdbcUtilityFunction
								.queryForObject(duplicateCheckQuery, UsersSite.class, jdbcTemplate);
						if (duplicateSite == null) {
							final List<Object> listBeforeUpdate = new ArrayList<>();
							final List<Object> listAfterUpdate = new ArrayList<>();
							final List<String> multilingualIDList = new ArrayList<>();
							if (objUsersSite.getNdefaultsite() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								if (existingDefaultSite != null
										&& existingDefaultSite.getNusersitecode() != objUsersSite.getNusersitecode()) {
									final UsersSite defaultBeforeSave = SerializationUtils.clone(existingDefaultSite);
									listBeforeUpdate.add(defaultBeforeSave);
									existingDefaultSite.setNdefaultsite(
											(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
									final String updateQueryString = "update userssite set dmodifieddate = '"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
											+ " ndefaultsite = "
											+ Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ " where nusersitecode = " + existingDefaultSite.getNusersitecode();
									jdbcTemplate.execute(updateQueryString);
									listAfterUpdate.add(existingDefaultSite);
								}
							}
							final String updateQueryString = "update userssite set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nsitecode = "
									+ objUsersSite.getNsitecode() + ", nusercode = " + objUsersSite.getNusercode()
									+ ", ndefaultsite = " + objUsersSite.getNdefaultsite() + " where nusersitecode = "
									+ objUsersSite.getNusersitecode();
							jdbcTemplate.execute(updateQueryString);

							final String site = "select s.ssitename,us.nsitecode,us.ndefaultsite,us.nusersitecode,us.nusercode,us.nstatus  from userssite us,site s "
									+ " where us.nsitecode = s.nsitecode " + " and us.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and  nusersitecode = " + objUsersSite.getNusersitecode()
									+ " and s.nmastersitecode = " + userInfo.getNmastersitecode();
							UsersSite siteList = (UsersSite) jdbcUtilityFunction.queryForObject(site, UsersSite.class,
									jdbcTemplate);
							listAfterUpdate.add(objUsersSite);
							listBeforeUpdate.add(usersSiteById);
							multilingualIDList.add("IDS_EDITUSERSITE");
							auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
									multilingualIDList, userInfo);
							return getRoleAndDeputyByUsersSite(siteList, userInfo);
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes a user's site assignment. The deletion is logical (soft delete) and includes
	 * validation to ensure the user is active and not retired, and the site assignment exists and is not default.
	 * It also checks for linked multi-role and deputy entries and removes them if applicable.
	 * All deleted entities are recorded in the audit log.
	 *
	 * Parameters used from objUsersSite include:
	 * - nusersitecode: ID of the user-site mapping to delete
	 * - ndefaultsite: flag indicating if the site is default
	 *
	 * Parameters used from objUsers include:
	 * - nusercode: ID of the user
	 *
	 * @param objUsersSite Object containing the user-site mapping to delete
	 * @param userInfo Information about the logged-in user
	 * @param objUsers Object containing user details
	 * @return ResponseEntity containing refreshed user details or failure messages
	 * @throws Exception if validation or deletion operations fail
	 */
	@Override
	public ResponseEntity<Object> deleteUsersSite(UsersSite objUsersSite, UserInfo userInfo, Users objUsers)
			throws Exception {
		final Users activeUserById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (activeUserById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (activeUserById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final UsersSite activeUsersSite = getActiveUsersSiteById(objUsersSite.getNusersitecode(), userInfo);
				if (activeUsersSite == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (objUsersSite.getNdefaultsite() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final String query = "select 'IDS_USERMULTIROLE' as Msg from usermultirole umr "
								+ " where ndefaultrole = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and nusersitecode = " + objUsersSite.getNusersitecode() + " and umr.nsitecode = "
								+ userInfo.getNmastersitecode()
								+ " and spassword is NOT NULL and dpasswordvalidatedate is NOT NULL" + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
						boolean validRecord = false;
						if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
							validRecord = true;
							validatorDel = projectDAOSupport
									.validateDeleteRecord(Integer.toString(objUsersSite.getNusersitecode()), userInfo);
							if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS
									.getReturnvalue()) {
								validRecord = true;
							} else {
								validRecord = false;
							}
						}

						if (validRecord) {
							final List<Object> savedList = new ArrayList<>();
							final List<Object> AuditActionList = new ArrayList<>();
							final List<String> multilingualIDList = new ArrayList<>();

							final String deleteQuery = "update userssite set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where nusersitecode = " + objUsersSite.getNusersitecode() + ";";
							final String deleteUserRole = "update usermultirole set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where nusersitecode = " + objUsersSite.getNusersitecode() + " and nsitecode = "
									+ userInfo.getNmastersitecode() + ";";
							final String deleteDeputyUserRole = "update usermultideputy set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where nusersitecode = " + objUsersSite.getNusersitecode() + " and nsitecode = "
									+ userInfo.getNmastersitecode() + ";";
							final List<UserMultiRole> objUserMultiRole = (List<UserMultiRole>) jdbcTemplate.query(
									"select nusermultirolecode, nusersitecode, nuserrolecode, ndefaultrole, ntransactionstatus, spassword, dpasswordvalidatedate, nnooffailedattempt, dmodifieddate, nsitecode, nstatus from usermultirole where nusersitecode = "
											+ objUsersSite.getNusersitecode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode = " + userInfo.getNmastersitecode(),
									new UserMultiRole());
							final List<UserMultiDeputy> objUserMultiDeputy = (List<UserMultiDeputy>) jdbcTemplate.query(
									"select nusermultideputycode, nusersitecode, ndeputyusersitecode, nuserrolecode, ntransactionstatus, dmodifieddate, nsitecode, nstatus from usermultideputy where nusersitecode = "
											+ objUsersSite.getNusersitecode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode = " + userInfo.getNmastersitecode(),
									new UserMultiDeputy());
							jdbcTemplate.execute(deleteQuery + deleteUserRole + deleteDeputyUserRole);
							objUsersSite
									.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							savedList.add(objUsersSite);

							AuditActionList.add(Arrays.asList(objUsersSite));
							multilingualIDList.add("IDS_DELETEUSERSITE");
							AuditActionList.add(objUserMultiRole);
							multilingualIDList.add("IDS_DELETEUSERROLE");
							AuditActionList.add(objUserMultiDeputy);
							multilingualIDList.add("IDS_DELETEDEPUTY");
							auditUtilityFunction.fnInsertListAuditAction(AuditActionList, 1, null, multilingualIDList,
									userInfo);

							return getUsers(objUsersSite.getNusercode(), null, userInfo);

						} else {
							return new ResponseEntity<>(validatorDel.getSreturnmessage(),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTUSERSITECANNOTDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

				}
			}
		}
	}

	/**
	 * Fetches a list of active users along with department names for dropdown or combo box use.
	 * Filters the users based on the master site code provided in the userInfo object.
	 *
	 * @param userInfo Information about the logged-in user, including master site code
	 * @return ResponseEntity containing a list of users and department names
	 * @throws Exception if the query fails or returns invalid data
	 */
	@Override
	public ResponseEntity<Object> getUserWithDeptForCombo(final UserInfo userInfo) throws Exception {
		final String queryString = " select u.nusercode, u.ndeptcode, u.ncountrycode, u.ndesignationcode, u.nlockmode, u.nlogintypecode, u.ntransactionstatus,"
				+ " u.sloginid, u.sempid, u.sfirstname, u.slastname, u.sbloodgroup, u.squalification, u.ddateofjoin, u.smobileno, u.sphoneno,"
				+ " u.semail, u.sjobdescription, u.saddress1, u.saddress2, u.saddress3, u.dmodifieddate, u.nsitecode, u.nstatus,"
				+ " concat (u.sfirstname,' ',u.slastname,'(',u.sloginid,')')  as susername, d.sdeptname from users u, department d "
				+ " where  u.ndeptcode = d.ndeptcode and u.nusercode<>-1 " + " and u.ntransactionstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u. nsitecode = "
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(queryString, new Users()), HttpStatus.OK);
	}

	/**
	 * Creates a new user record along with associated site, multi-role, and file information.
	 * The method performs the following steps:
	 * - Locks the user creation table to prevent concurrent modifications
	 * - Parses and validates multipart request data including user, site, role, and image details
	 * - Checks for duplicate login IDs within the same master site
	 * - Uploads user image and signature files if provided
	 * - Inserts the user record with auto-generated sequence number
	 * - Creates a default site assignment and associated user role entry if applicable
	 * - Inserts file metadata into the userfile table
	 * - Logs the creation in the audit trail
	 * - Returns the updated list of users
	 *
	 * Request parameters used:
	 * - users: contains basic user data such as name, login ID, designation, contact details, address, etc.
	 * - userssite: contains site assignment details for the user
	 * - usermultirole: contains multi-role assignment details
	 * - userfile: contains user file metadata including signature and profile images
	 *
	 * @param request Multipart HTTP request containing users, userssite, usermultirole, and userfile JSON parameters
	 * @param userInfo Information about the currently logged-in user performing the operation
	 * @return ResponseEntity containing the list of all users after insertion or error message in case of failure
	 * @throws Exception if any parsing, validation, file upload, or database operation fails
	 */
	@Override
	public ResponseEntity<Object> createUsers(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final Integer nFlag = 1;
		final String sQueryLock = " lock  table lockusers " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final ObjectMapper objMapper = new ObjectMapper();
		final Users objUsers = objMapper.readValue(request.getParameter("users"), new TypeReference<Users>() {
		});
		UsersSite objUsersSite = objMapper.readValue(request.getParameter("userssite"), new TypeReference<UsersSite>() {
		});

		final UserMultiRole userMultiRole = objMapper.readValue(request.getParameter("usermultirole"),
				new TypeReference<UserMultiRole>() {
				});
		final UserFile objUserFile = objMapper.readValue(request.getParameter("userfile"), new TypeReference<UserFile>() {
		});

		final Users userByLoginId = getUserByLoginId(objUsers.getSloginid(), userInfo.getNmastersitecode());

		if (userByLoginId == null) {
			String attachmentStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (objUserFile.getSsignimgname() != null || objUserFile.getSuserimgname() != null) {
				attachmentStatus = ftpUtilityFunction.multiControlFTPFileUpload(request, userInfo);
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(attachmentStatus)) {
				objUsers.setNlogintypecode((short) Enumeration.LoginType.INTERNAL.getnlogintype());
				String doj = null;
				if (objUsers.getDdateofjoin() != null) {
					final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					doj = sdFormat.format(objUsers.getDdateofjoin());
					objUsers.setDdateofjoin(sdFormat.parse(doj));
					objUsers.setSdateofjoin(
							new SimpleDateFormat(userInfo.getSsitedate()).format(objUsers.getDdateofjoin()));
					doj = "N'"+doj+"':: timestamp";
				}
				objUsers.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
                
				final String sEmpId = objUsers.getSempid()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSempid()) + "'";
				final String sFirstName = objUsers.getSfirstname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSfirstname()) + "'";
				final String sLastName = objUsers.getSlastname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSlastname()) + "'";
				final String sBloodGroup = objUsers.getSbloodgroup()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSbloodgroup()) + "'";
				final String sQualification = objUsers.getSqualification()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSqualification()) + "'";
				final String sMobileNo = objUsers.getSmobileno()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSmobileno()) + "'";
				final String sPhoneNo = objUsers.getSphoneno()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSphoneno()) + "'";
				final String sEmail = objUsers.getSemail()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSemail()) + "'";
				final String sJobDescription = objUsers.getSjobdescription()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSjobdescription()) + "'";
				final String sAddress1 = objUsers.getSaddress1()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress1()) + "'";
				final String sAddress2 = objUsers.getSaddress2()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress2()) + "'";
				final String sAddress3 = objUsers.getSaddress3()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress3()) + "'";

				final String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='users' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
				nsequenceNo++;

				String insertQuery = "insert into users( nusercode, ndeptcode, ncountrycode, ndesignationcode, nlockmode, nlogintypecode, ntransactionstatus, sloginid, sempid, sfirstname, slastname, sbloodgroup, squalification, ddateofjoin, smobileno, sphoneno, semail, sjobdescription, saddress1, saddress2, saddress3, dmodifieddate, nsitecode, nstatus)"
						+ " values(" + nsequenceNo + ", " + objUsers.getNdeptcode() + ", " + objUsers.getNcountrycode()
						+ ", " + objUsers.getNdesignationcode() + ", " + objUsers.getNlockmode() + ", "
						+ objUsers.getNlogintypecode() + ", " + objUsers.getNtransactionstatus() + ", N'"
						+ stringUtilityFunction.replaceQuote(objUsers.getSloginid()) + "', "
						+ sEmpId + ", "+ sFirstName + ", "+ sLastName + ", "+ sBloodGroup + ", "
						+ sQualification + ", " + doj + ", "+ sMobileNo + ", "	+ sPhoneNo+ ", "
						+ sEmail + ", "+ sJobDescription+ ", "+ sAddress1 + ", "+ sAddress2+ ", "+ sAddress3+ ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertQuery);

				String updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
						+ " where stablename='users' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQuery);

				objUsers.setNusercode(nsequenceNo);

				objUsersSite = createFirstUserSite(objUsers, objUsersSite, userInfo);
				if (userMultiRole != null && objUsersSite.getNusersitecode() != 0) {
					userMultiRole.setNusersitecode(objUsersSite.getNusersitecode());
					userMultiRole.setNdefaultrole((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
					userMultiRole
							.setNtransactionstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
					createUserMultiRole(userMultiRole, userInfo, objUsers);
				}
				objUserFile.setNusercode(objUsers.getNusercode());
				objUserFile.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				objUserFile.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
				objUserFile.setNsitecode(userInfo.getNmastersitecode());

				final String sequenceNoUserQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				nsequenceNo = jdbcTemplate.queryForObject(sequenceNoUserQuery, Integer.class);
				nsequenceNo++;
                
				final String sSignImgName = objUserFile.getSsignimgname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgname()) + "'";
				final String sSignImgFtp = objUserFile.getSsignimgftp()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgftp()) + "'";
				final String sUserImgName = objUserFile.getSuserimgname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgname()) + "'";
				final String sUserImgFtp = objUserFile.getSuserimgftp()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgftp()) + "'";
				 
				insertQuery = "insert into userfile( nuserfilecode, nusercode, ssignimgname, ssignimgftp, suserimgname, suserimgftp, dmodifieddate, nsitecode, nstatus)"
						+ " values(" + nsequenceNo + ", " + objUserFile.getNusercode() + ", "
						+ sSignImgName+ ", "+ sSignImgFtp + ", "+ sUserImgName + ", "+ sUserImgFtp+ ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertQuery);

				updateQuery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceNo
						+ " where stablename='userfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQuery);

				objUserFile.setNuserfilecode(nsequenceNo);
				objUsers.setSsignimgftp(objUserFile.getSsignimgftp());
				objUsers.setSuserimgftp(objUserFile.getSuserimgftp());
				objUsers.setSsignimgname(objUserFile.getSsignimgname());
				objUsers.setSuserimgname(objUserFile.getSuserimgname());
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_ADDUSERS");
				final List<Object> savedUserList = new ArrayList<>();
				savedUserList.add(objUsers);
				auditUtilityFunction.fnInsertAuditAction(savedUserList, 1, null, multilingualIDList, userInfo);
				return getUsers(null, nFlag, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(attachmentStatus, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * Updates the user details and associated user file information.
	 * <p>
	 * This method performs validation against current transaction and lock status,
	 * verifies session status before allowing status changes, ensures uniqueness
	 * of login ID, and updates both user and user file records. It also handles file
	 * uploads (signature and user image) via FTP and maintains an audit trail for changes.
	 *
	 * @param request The multipart HTTP request containing 'users', 'userfile', 
	 *                'isSignFileEdited', and 'isUserFileEdited' parameters.
	 * @param userInfo The metadata of the user initiating the update request, 
	 *                 including site and language information.
	 * @return A {@link ResponseEntity} containing the updated user information on success,
	 *         or a multilingual error message in case of failure or conflict.
	 * @throws Exception if JSON parsing fails or database operations encounter an issue.
	 */
	@Override
	public ResponseEntity<Object> updateUsers(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Integer nFlag = 1;

		Users objUsers = objMapper.readValue(request.getParameter("users"), Users.class);
		UserFile objUserFile = objMapper.readValue(request.getParameter("userfile"), UserFile.class);

		final Users userById = getActiveUserById(objUsers.getNusercode(), userInfo);
		if (userById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_USERALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (userById.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (userById.getNtransactionstatus() != objUsers.getNtransactionstatus()
						|| userById.getNlockmode() != objUsers.getNlockmode()) {

					if (objUsers.getNtransactionstatus() == Enumeration.TransactionStatus.DEACTIVE
							.gettransactionstatus()
							|| objUsers.getNlockmode() == Enumeration.TransactionStatus.LOCK.gettransactionstatus()) {

						final String sessionVal = "select nusercode from sessiondetails where  nusercode = "
								+ objUsers.getNusercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						final List<Users> sessionValobj = (List<Users>) jdbcTemplate.query(sessionVal, new Users());
						if (!sessionValobj.isEmpty()) {
							if (userById.getNtransactionstatus() != objUsers.getNtransactionstatus()) {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_USERALREDYLOGINDEACTIVE",
												userInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							} else if (userById.getNlockmode() != objUsers.getNlockmode()) {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_USERALREDYLOGINLOCK",
												userInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							}
						}
					}
				}
				final String queryString = "select nusercode from users where sloginid = N'"
						+ stringUtilityFunction.replaceQuote(objUsers.getSloginid()) + "' and nusercode <> "
						+ objUsers.getNusercode() + " and  nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				final Users userByLoginId = (Users) jdbcUtilityFunction.queryForObject(queryString, Users.class,
						jdbcTemplate);
				if (userByLoginId == null) {
					String uploadStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					final int isSignFileEdited = Integer.valueOf(request.getParameter("isSignFileEdited"));
					final int isUserFileEdited = Integer.valueOf(request.getParameter("isUserFileEdited"));

					if (isSignFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()
							|| isUserFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						final String userFileQuery = "select uf.nuserfilecode, uf.nusercode, uf.ssignimgname, uf.ssignimgftp, uf.suserimgname, uf.suserimgftp, uf.dmodifieddate, uf.nsitecode, uf.nstatus from userfile uf where uf.nusercode = "
								+ objUsers.getNusercode() + " and uf.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nsitecode = "
								+ userInfo.getNmastersitecode();
						final UserFile userFileByUser = (UserFile) jdbcUtilityFunction.queryForObject(userFileQuery,
								UserFile.class, jdbcTemplate);

						if (objUserFile.getSsignimgname() != null || objUserFile.getSuserimgname() != null) {
							uploadStatus = ftpUtilityFunction.multiControlFTPFileUpload(request, userInfo);

							if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
								if (userFileByUser == null) {
									objUserFile.setNusercode(objUsers.getNusercode());
									objUserFile.setNstatus(
											(short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
									objUserFile.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
									objUserFile.setNsitecode(userInfo.getNmastersitecode());

									final String sequenceNoUserQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
									int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoUserQuery, Integer.class);
									nsequenceNo++;
									
									String sSignImgName = objUserFile.getSsignimgname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgname()) + "'";
									 String sSignImgFtp = objUserFile.getSsignimgftp()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgftp()) + "'";
									 String sUserImgName = objUserFile.getSuserimgname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgname()) + "'";
									 String sUserImgFtp = objUserFile.getSuserimgftp()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgftp()) + "'";

									final String insertQuery = "insert into userfile( nuserfilecode, nusercode, ssignimgname, ssignimgftp, suserimgname, suserimgftp, dmodifieddate, nsitecode, nstatus)"
											+ " values(" + nsequenceNo + ", " + objUserFile.getNusercode() + ", "
											+ sSignImgName+ ", " + sSignImgFtp+ ", "+ sUserImgName+ ", " + sUserImgFtp
											+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ userInfo.getNmastersitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
									jdbcTemplate.execute(insertQuery);

									final String updateQuery = "update seqnocredentialmanagement set nsequenceno ="
											+ nsequenceNo + " where stablename='userfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
									jdbcTemplate.execute(updateQuery);

									objUserFile.setNuserfilecode(nsequenceNo);

								} else {
									objUserFile = deleteUserFile(objUsers, objUserFile, userFileByUser, userInfo);
								}
							}
						} else {
							objUserFile = deleteUserFile(objUsers, objUserFile, userFileByUser, userInfo);
							uploadStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
						}

					}
					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
						final List<Object> listBeforeUpdate = new ArrayList<>();
						final List<Object> listAfterUpdate = new ArrayList<>();
						String doj = null;
						if (objUsers.getDdateofjoin() != null) {
							final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							doj = "N'" + sdFormat.format(objUsers.getDdateofjoin()) + "'";
							objUsers.setSdateofjoin(
									new SimpleDateFormat(userInfo.getSsitedate()).format(objUsers.getDdateofjoin()));
						}
						
						final String sEmpId = objUsers.getSempid()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSempid()) + "'";
						final String sFirstName = objUsers.getSfirstname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSfirstname()) + "'";
						final String sLastName = objUsers.getSlastname()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSlastname()) + "'";
						final String sBloodGroup = objUsers.getSbloodgroup()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSbloodgroup()) + "'";
						final String sQualification = objUsers.getSqualification()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSqualification()) + "'";
						final String sMobileNo = objUsers.getSmobileno()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSmobileno()) + "'";
						final String sPhoneNo = objUsers.getSphoneno()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSphoneno()) + "'";
						final String sEmail = objUsers.getSemail()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSemail()) + "'";
						final String sJobDescription = objUsers.getSjobdescription()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSjobdescription()) + "'";
						final String sAddress1 = objUsers.getSaddress1()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress1()) + "'";
						final String sAddress2 = objUsers.getSaddress2()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress2()) + "'";
						final String sAddress3 = objUsers.getSaddress3()== null ? null : "N'"+ stringUtilityFunction.replaceQuote(objUsers.getSaddress3()) + "'";

						final StringBuilder sb = new StringBuilder();
						final String updateQueryString = "update users set dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) 
								+ "', sloginid =  N'"+ stringUtilityFunction.replaceQuote(objUsers.getSloginid()) + "', "
								+ "sfirstname = "+ sFirstName+ ", slastname = "+ sLastName + ", sempid = "+ sEmpId + ", "
								+ "ndeptcode = "+ objUsers.getNdeptcode() + ", sbloodgroup = "+ sBloodGroup + ", "
								+ "saddress1 = "+ sAddress1 + ", saddress2 = "+ sAddress2 + ", saddress3 = "+ sAddress3+ ", "
								+ "squalification =  "+ sQualification+ ", ddateofjoin =  " + doj + "::timestamp, "
								+ "sjobdescription = "+ sJobDescription+ ", ndesignationcode = " + objUsers.getNdesignationcode() + ", "
								+ "semail = "+ sEmail + ", sphoneno = "+ sPhoneNo + ", smobileno = "+ sMobileNo + ", "
								+ "ncountrycode = "+ objUsers.getNcountrycode() + ", nlockmode = " + objUsers.getNlockmode()
								+ ", ntransactionstatus = " + objUsers.getNtransactionstatus() + ", nlogintypecode = "
								+ objUsers.getNlogintypecode() + " where nusercode = " + objUsers.getNusercode() + ";";
						sb.append(updateQueryString);

						final String sMultiRoleQuery = "select umr.nusermultirolecode,umr.nuserrolecode,pp.nnooffailedattempt "
								+ " from usermultirole umr,userssite us,passwordpolicy pp,userrolepolicy up"
								+ " where us.nusersitecode = umr.nusersitecode and pp.npolicycode = up.npolicycode and up.nuserrolecode = umr.nuserrolecode"
								+ " and us.nusercode = " + objUsers.getNusercode() + " and umr.ndefaultrole = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and up.ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and umr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nsitecode = "
								+ userInfo.getNmastersitecode() + " and pp.nsitecode = " + userInfo.getNmastersitecode()
								+ " and up.nsitecode = " + userInfo.getNmastersitecode();
						final List<UserMultiRole> listUserRole = (List<UserMultiRole>) jdbcTemplate
								.query(sMultiRoleQuery, new UserMultiRole());

						for (UserMultiRole objUserMultiRole : listUserRole) {
							final String sUpdateRole = "update usermultirole set dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nnooffailedattempt = "
									+ objUserMultiRole.getNnooffailedattempt() + " where nusermultirolecode = "
									+ objUserMultiRole.getNusermultirolecode() + ";";
							sb.append(sUpdateRole);
						}

						jdbcTemplate.execute(sb.toString());
						objUsers.setSsignimgftp(objUserFile.getSsignimgftp());
						objUsers.setSuserimgftp(objUserFile.getSuserimgftp());
						objUsers.setSsignimgname(objUserFile.getSsignimgname());
						objUsers.setSuserimgname(objUserFile.getSuserimgname());
						listAfterUpdate.add(objUsers);
						listBeforeUpdate.add(userById);
						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								Arrays.asList("IDS_EDITUSERS"), userInfo);
						return getUsers(objUsers.getNusercode(), nFlag, userInfo);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
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

	/**
	 * Deletes the existing user signature and profile images from the FTP location if they differ from the updated files.
	 * Updates the corresponding database records with new file references.
	 *
	 * @param objUsers The user whose image files are being updated.
	 * @param objUserFile The updated user file object containing new image file names and FTP paths.
	 * @param userFileByUser The existing user file object from the database for comparison.
	 * @param userInfo Contains user session details including site and user code.
	 * @return The updated UserFile object with image fields cleared if images were deleted.
	 * @throws Exception If file deletion or database update fails.
	 */
	private UserFile deleteUserFile(final Users objUsers, final UserFile objUserFile, final UserFile userFileByUser,
			final UserInfo userInfo) throws Exception {

		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
				.getUploadControlsByFormCode(userInfo).getBody();

		final Map<String, Object> imageMap = new HashMap<String, Object>();

		if (objUserFile.getSsignimgftp() == null
				|| !objUserFile.getSsignimgftp().equals(userFileByUser.getSsignimgftp())) {

			final String absolutePath = System.getenv(homePath) + Enumeration.FTP.USERSIGN_PATH.getFTP() + "\\"
					+ userFileByUser.getSsignimgftp();

			final File file = new File(absolutePath);
			file.delete();

			imageMap.put("SignImage_fileName", userFileByUser.getSsignimgftp());
		}
		if (objUserFile.getSuserimgftp() == null
				|| !objUserFile.getSuserimgftp().equals(userFileByUser.getSuserimgftp())) {

			final String absolutePath = System.getenv(homePath)// new File("").getAbsolutePath()
					+ Enumeration.FTP.USERPROFILE_PATH.getFTP() + "\\" + userFileByUser.getSuserimgftp();
			final File file = new File(absolutePath);
			file.delete();
			imageMap.put("UserImage_fileName", userFileByUser.getSuserimgftp());
		}

		if (!imageMap.isEmpty()) {
			ftpUtilityFunction.multiPathDeleteFTPFile(imageMap, controlList, userInfo);
		}
		objUserFile.setNusercode(objUsers.getNusercode());
		objUserFile.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		final String query = "update userfile set dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "', ssignimgname =  case '" + objUserFile.getSsignimgname() + "' when 'null' then NULL else N'"
				+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgname()) + "' end, suserimgname =  case'"
				+ objUserFile.getSuserimgname() + "' when 'null' then NULL else N'"
				+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgname()) + "' end, ssignimgftp =  case '"
				+ objUserFile.getSsignimgftp() + "' when 'null'then NULL else N'"
				+ stringUtilityFunction.replaceQuote(objUserFile.getSsignimgftp()) + "' end, suserimgftp =  case '"
				+ objUserFile.getSuserimgftp() + "' when 'null'then NULL else N'"
				+ stringUtilityFunction.replaceQuote(objUserFile.getSuserimgftp()) + "' end  where nusercode = "
				+ objUserFile.getNusercode() + " and nsitecode = " + userInfo.getNmastersitecode();
		jdbcTemplate.execute(query);
		if (objUserFile.getSsignimgftp() == null) {
			objUserFile.setSsignimgftp("");
			objUserFile.setSsignimgname("");
		}
		if (objUserFile.getSuserimgftp() == null) {
			objUserFile.setSuserimgftp("");
			objUserFile.setSuserimgname("");
		}
		return objUserFile;
	}

	/**
	 * Retrieves the selected user's image paths and downloads them from FTP if not available locally.
	 *
	 * @param selectedUser The user whose signature and profile images are to be fetched.
	 * @param userInfo Contains user session details including language and site information.
	 * @return ResponseEntity containing image data or download paths of signature and profile images.
	 * @throws Exception If image fetching or FTP operations fail.
	 */
	public ResponseEntity<Object> viewSelectedUserImage(final Users selectedUser, final UserInfo userInfo)
			throws Exception {
		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
				.getUploadControlsByFormCode(userInfo).getBody();
		String signImage = null;
		final Map<String, Object> imageMap = new HashMap<String, Object>();
		if (selectedUser.getSsignimgftp() != null) {

			final String absolutePath = System.getenv(homePath) + Enumeration.FTP.USERSIGN_PATH.getFTP();

			final File file = new File(absolutePath + "\\" + selectedUser.getSsignimgftp());
			if (!file.exists()) {
				imageMap.put("SignImage_fileName", selectedUser.getSsignimgftp());
				imageMap.put("SignImage_customName", "");
				imageMap.put("SignImage_path", absolutePath);
			} else {
				signImage = selectedUser.getSsignimgftp();
			}
		}
		String userImage = null;
		if (selectedUser.getSuserimgftp() != null) {

			final String absolutePath = System.getenv(homePath)// new File("").getAbsolutePath()
					+ Enumeration.FTP.USERPROFILE_PATH.getFTP();

			final File file = new File(absolutePath + "\\" + selectedUser.getSuserimgftp());
			if (!file.exists()) {
				imageMap.put("UserImage_fileName", selectedUser.getSuserimgftp());
				imageMap.put("UserImage_customName", "");
				imageMap.put("UserImage_path", absolutePath);
			} else {
				// userImage = userInfo.getSloginid() + "/" + selectedUser.getSuserimgftp();
				userImage = selectedUser.getSuserimgftp();
			}
		}
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final Map<String, Object> map = ftpUtilityFunction.multiPathMultiFileDownloadUsingFtp(imageMap, controlList,
				userInfo, "");
		outputMap.putAll(map);

		outputMap.put("SignImagePath", signImage == null ? (String) map.get("SignImage_AttachFile") : signImage);
		outputMap.put("UserImagePath", userImage == null ? (String) map.get("UserImage_AttachFile") : userImage);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Retrieves the profile picture metadata of the logged-in user from the database.
	 *
	 * @param userInfo Contains user session details including user code and site code.
	 * @return ResponseEntity containing the UserFile object with image FTP path information.
	 * @throws Exception If database query fails.
	 */
	@Override
	public ResponseEntity<Object> getUserProfilePictureData(UserInfo userInfo) throws Exception {

		final String getUserFile = "select nuserfilecode,nusercode,suserimgftp from userfile where nusercode = "
				+ userInfo.getNusercode() + " and nsitecode = " + userInfo.getNmastersitecode() + "";
		final UserFile userfile = (UserFile) jdbcUtilityFunction.queryForObject(getUserFile, UserFile.class, jdbcTemplate);
		return new ResponseEntity<Object>(userfile, HttpStatus.OK);
	}

	/**
	 * Uploads the new user profile picture to FTP and updates the database with the new file names.
	 *
	 * @param request The multipart HTTP request containing the uploaded file and metadata.
	 * @param userInfo Contains user session details including user and site code.
	 * @return ResponseEntity indicating success or failure with multilingual message.
	 * @throws Exception If file upload or database update fails.
	 */
	@Override
	public ResponseEntity<Object> updateUserProfilePicture(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		String ftpResponse = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);

		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(ftpResponse)) {
			String updateUserFile = "update userfile set suserimgname = N'"
					+ stringUtilityFunction.replaceQuote(request.getParameter("filename")) + "'," + "suserimgftp = N'"
					+ stringUtilityFunction.replaceQuote(request.getParameter("uniquefilename0"))
					+ "' where nusercode = " + userInfo.getNusercode() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			jdbcTemplate.execute(updateUserFile);
		}

		return ftpResponse.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				? new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.OK)
				: new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.FAILED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * Retrieves the profile picture file from FTP for the logged-in user if available.
	 *
	 * @param userInfo Contains user session details including user and site code.
	 * @return ResponseEntity containing the profile image path or error message in JSON format.
	 * @throws Exception If FTP or database operation fails.
	 */
	@Override
	public ResponseEntity<?> getUpdateUserProfilePicture(UserInfo userInfo) throws Exception {

		String userProfileImageFile = "";
		JSONObject returnResponse = new JSONObject();
		final String getUserFile = "select nuserfilecode,nusercode,suserimgftp from userfile where nusercode = "
				+ userInfo.getNusercode() + " and nsitecode = " + userInfo.getNmastersitecode();
		UserFile userfile = (UserFile) jdbcUtilityFunction.queryForObject(getUserFile, UserFile.class, jdbcTemplate);

		if (userfile != null && userfile.getSuserimgftp() != null) {

			final String sReportSettingQry = "select ssettingvalue from reportsettings where nreportsettingcode = 11 and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String sProfilePath = jdbcTemplate.queryForObject(sReportSettingQry, String.class);

			if (sProfilePath != null && sProfilePath != "") {
				final Map<String, Object> ftpResponse = ftpUtilityFunction.FileViewUsingFtp(userfile.getSuserimgftp(),
						-1, userInfo, sProfilePath, "");
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
						.equals(ftpResponse.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					userProfileImageFile = (String) ftpResponse.get("AttachFile");
					returnResponse.put("UserProfileImage", userProfileImageFile);
				} else {
					returnResponse.put("Error",
							ftpResponse.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
				}
			} else {
				returnResponse.put("Error", commonFunction.getMultilingualMessage("IDS_USERPROFILE_DIR_NOTEXISTS",
						userInfo.getSlanguagefilename()));
			}
		}
		return new ResponseEntity<String>(returnResponse.toString(), HttpStatus.OK);
	}

}
