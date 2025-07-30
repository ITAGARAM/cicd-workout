package com.agaramtech.qualis.credential.service.userrole;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "userrole" table by
 * implementing methods from its interface.
 * 
 * @author ATE090
 * @version
 * @since 01- Jul- 2020
 */
@AllArgsConstructor
@Repository
public class UserRoleDAOImpl implements UserRoleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	
	/**
	 * This method is used to retrieve list of all active userrole for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         userrole
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUserRole(final int nmasterSiteCode) throws Exception {

		final String strQuery = "select a.nuserrolecode, a.suserrolename, a.sdescription, a.dmodifieddate, a.nsitecode, a.nstatus from userrole a where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode = " + nmasterSiteCode
				+ " and nuserrolecode > 0 order by nuserrolecode";
		LOGGER.info("At getUserRole DAO Impl");
		return new ResponseEntity<>((List<UserRole>) jdbcTemplate.query(strQuery, new UserRole()), HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve active userrole object based on the
	 * specified nuserrolecode.
	 * 
	 * @param nuserrolecode [int] primary key of userrole object
	 * @return response entity object holding data of
	 *         userrole object 
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public UserRole getActiveUserRoleById(final int nuserRoleCode) throws Exception {

		final String strQuery = "select a.nuserrolecode, a.suserrolename, a.sdescription, a.dmodifieddate, a.nsitecode, a.nstatus from userrole a where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nuserrolecode = "
				+ nuserRoleCode;

		return (UserRole) jdbcUtilityFunction.queryForObject(strQuery, UserRole.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to userrole table. Need to
	 * check for duplicate entry of User Role Name for the specified
	 * site before saving into database.
	 * User Role Name is unique across the database. 
	 * @param userrole [UserRole] object holding details to be
	 *                           added in userorle table.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted userrole object with HTTP Status.
	 *  @return saved userrole object with status code 200 if saved successfully else if the userrole already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createUserRole(final UserRole userRole, final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockuserrole " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final UserRole userRoleListByName = getUserRoleByName(userRole.getSuserrolename(), userRole.getNsitecode());

		if (userRoleListByName == null) {

			final UserRoleConfig userRoleConfig = new UserRoleConfig();

			final String sequenceQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userrole' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceQuery, Integer.class);
			nsequenceNo++;
			String insertQuery = "Insert  into userrole (nuserrolecode,suserrolename,sdescription,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceNo + ",N'" + stringUtilityFunction.replaceQuote(userRole.getSuserrolename())
					+ "',N'" + stringUtilityFunction.replaceQuote(userRole.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertQuery);

			String updatequery = "update seqnocredentialmanagement set nsequenceno=" + nsequenceNo
					+ " where stablename ='userrole' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

			jdbcTemplate.execute(updatequery);

			userRoleConfig.setNuserrolecode(userRole.getNuserrolecode());

			final String seqnoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='userroleconfig' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			int nseqNo = jdbcTemplate.queryForObject(seqnoQuery, Integer.class);
			;
			nseqNo++;
			final String userroleconfigquery = "insert  into userroleconfig (nuserrolecode,nneedapprovalflow,nneedresultflow,dmodifieddate	,nsitecode,nstatus) "
					+ "values(" + nseqNo + "," + userRoleConfig.getNneedapprovalflow() + ","
					+ userRoleConfig.getNneedresultflow() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + "," + " "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(userroleconfigquery);

			final String updateconfigQuery = "update seqnocredentialmanagement set nsequenceno=" + nseqNo
					+ " where stablename ='userroleconfig' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(updateconfigQuery);

			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_ADDUSERROLE");

			final List<Object> savedUserRoleList = new ArrayList<>();
			userRole.setNuserrolecode(nseqNo);
			savedUserRoleList.add(userRole);

			auditUtilityFunction.fnInsertAuditAction(savedUserRoleList, 1, null, multilingualIDList, userInfo);

			return getUserRole(userRole.getNsitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active userrole objects for the
	 * specified User Role Name.
	 * 
	 * @param suserrolename [String] it to be checked the duplicate in
	 *                        the userrole table.
	 * @param nmasterSiteCode [int] site code of the userrole
	 * @return active userrole based on the specified User Role Name
	 * @throws Exception
	 */
	private UserRole getUserRoleByName(final String suserRoleName, final int nmasterSiteCode) throws Exception {

		final String strQuery = "select  suserrolename from userrole where suserrolename = N'"
				+ stringUtilityFunction.replaceQuote(suserRoleName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode ="
				+ nmasterSiteCode;

		return (UserRole) jdbcUtilityFunction.queryForObject(strQuery, UserRole.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in userrole table. Need to
	 * validate that the userrole object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * User Role Name for the specified site before saving into
	 * database.
	 * 
	 * @param userrole [User Role] object holding details to be
	 *                           updated in userrole table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         userrole object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateUserRole(UserRole userRole, UserInfo userInfo) throws Exception {

		final UserRole userRoleByID = (UserRole) getActiveUserRoleById(userRole.getNuserrolecode());

		if (userRoleByID == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {

			final String queryString = "select nuserrolecode from userrole where suserrolename = '"
					+ stringUtilityFunction.replaceQuote(userRole.getSuserrolename()) + "' and nuserrolecode <> "
					+ userRole.getNuserrolecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final UserRole objUserRole = (UserRole) jdbcUtilityFunction.queryForObject(queryString, UserRole.class, jdbcTemplate);

			if (objUserRole == null) {

				final String updateQueryString = "update userrole set suserrolename='"
						+ stringUtilityFunction.replaceQuote(userRole.getSuserrolename()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(userRole.getSdescription()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nuserrolecode="
						+ userRole.getNuserrolecode();

				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITUSERROLE");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(userRole);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(userRoleByID);

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getUserRole(userRole.getNsitecode());

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in userrole table.
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in other tables  such as usermultirole, userrolepolicy
	 * @param userrole [UserRole] an Object holds the record to be
	 *                           deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         userrole object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteUserRole(UserRole userRole, UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUserRoleList = new ArrayList<>();

		final UserRole userRoleByID = (UserRole) getActiveUserRoleById(userRole.getNuserrolecode());

		if (userRoleByID == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {

			final String query = "select 'IDS_USERS' as Msg from usermultirole where nuserrolecode="
					+ userRole.getNuserrolecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" union all select 'IDS_USERSCREENHIDE' as Msg from usersrolescreenhide where nuserrolecode=" + userRole.getNuserrolecode()
//							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+"  union all select  'IDS_PROJECTMASTER' as Msg from  projectmaster pm where "
//							+ " pm.nuserrolecode = "+userRole.getNuserrolecode()+" "
//							+ " and pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
//							+ " pm.nsitecode = "+userInfo.getNtranssitecode()
//							+" union all select 'IDS_USERROLETEMPLATE' as Msg from treetemplatetransactionrole where nuserrolecode=" + userRole.getNuserrolecode()
//							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" union all select 'IDS_APPROVALCONFIGROLE' as Msg from approvalconfigrole where nuserrolecode=" + userRole.getNuserrolecode()
//							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_PASSWORDPOLICY' as Msg from userrolepolicy where nuserrolecode="
					+ userRole.getNuserrolecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(userRole.getNuserrolecode()),
						userInfo);

				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final String updateQueryString = "update userrole set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nuserrolecode="
						+ userRole.getNuserrolecode();

				jdbcTemplate.execute(updateQueryString);

				final String updateQueryStringForConfig = "update userroleconfig set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nuserrolecode="
						+ userRole.getNuserrolecode();

				jdbcTemplate.execute(updateQueryStringForConfig);

				userRole.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedUserRoleList.add(userRole);

				multilingualIDList.add("IDS_DELETEUSERROLE");
				auditUtilityFunction.fnInsertAuditAction(savedUserRoleList, 1, null, multilingualIDList, userInfo);

				return getUserRole(userRole.getNsitecode());
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}
		}
	}

	/**
	 * This method id used to get userrole table.
	 * Need to check the record is already deleted or not
	 * @param users [Users] an Object holds the record to be used in this method
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         userrole object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getUserRoleComboData(final Users objUsers, final int nuserSiteCode,
			final int nuserMultiRoleCode, final int nsiteCode, UserInfo userInfo) throws Exception {
		String queryString = "";
		String checkdatapresent = "";
		Users objDelUsers = null;

		if (objUsers != null) {

			checkdatapresent = "select nusercode from users where nusercode=" + objUsers.getNusercode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";

			objDelUsers = (Users) jdbcUtilityFunction.queryForObject(checkdatapresent, Users.class, jdbcTemplate);

			if (objDelUsers == null) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		if (objUsers == null) {

			queryString = "select ur.nuserrolecode, ur.suserrolename, ur.sdescription, ur.dmodifieddate, ur.nsitecode, ur.nstatus from userrole ur join userrolepolicy up on "
					+ " ur.nuserrolecode = up.nuserrolecode and ur.nstatus =  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and up.nuserrolecode > 0 and ur.nsitecode = " + nsiteCode;

		} else {

			if (objUsers.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {

				if (nuserMultiRoleCode == -2) {

					queryString = "select ur.nuserrolecode, ur.suserrolename, ur.sdescription, ur.dmodifieddate, ur.nsitecode, ur.nstatus from userrole ur join userrolepolicy up on "
							+ " ur.nuserrolecode = up.nuserrolecode and ur.nstatus =  "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and up.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and up.ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ur.nsitecode = "
							+ nsiteCode + " and up.nuserrolecode > 0 and not exists ("
							+ " select 1 from usermultirole umr where umr.nuserrolecode = ur.nuserrolecode "
							+ " and umr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nusersitecode =" + nuserSiteCode + " and umr.nsitecode="
							+ userInfo.getNmastersitecode() + ")";

				} else {
					queryString = "select ur.nuserrolecode, ur.suserrolename, ur.sdescription, ur.dmodifieddate, ur.nsitecode, ur.nstatus from userrole ur join userrolepolicy up on ur.nuserrolecode = up.nuserrolecode "
							+ " and ur.nstatus = " + +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and up.nstatus = " + +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and up.ntransactionstatus ="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ur.nsitecode = "
							+ nsiteCode + " and up.nuserrolecode > 0 and not exists (select 1 from usermultirole umr "
							+ " where umr.nuserrolecode = ur.nuserrolecode and umr.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nusermultirolecode <> " + nuserMultiRoleCode + " and umr.nusersitecode = "
							+ nuserSiteCode + " and nsitecode=" + userInfo.getNmastersitecode() + ")";
				}
			} else {
				queryString = "select ur.nuserrolecode, ur.suserrolename, ur.sdescription, ur.dmodifieddate, ur.nsitecode, ur.nstatus from userrole ur where ur.nuserrolecode > 0 and ur.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode = "
						+ nsiteCode + " and not exists (select 1 from usermultirole umr "
						+ " where umr.nuserrolecode = ur.nuserrolecode and umr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nusermultirolecode <> "
						+ nuserMultiRoleCode + " and umr.nusersitecode = " + nuserSiteCode + " and nsitecode="
						+ userInfo.getNmastersitecode() + ")";
			}
		}
		return new ResponseEntity<>((List<UserRole>) jdbcTemplate.query(queryString, new UserRole()), HttpStatus.OK);

	}
}
