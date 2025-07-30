package com.agaramtech.qualis.configuration.service.passwordpolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.PasswordPolicy;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.configuration.model.UserRolePolicy;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "passwordpolicy" table by
 * implementing methods from its interface.
 *
 * @author ATE113
 * @version 9.0.0.1
 * @since 06- Jul- 2020
 */
@AllArgsConstructor
@Repository
public class PasswordPolicyDAOImpl implements PasswordPolicyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordPolicyDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active passwordpolicy for the
	 * specified site.
	 * 
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the list is to be
	 *                 checked
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getPasswordPolicy(Integer npolicycode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		PasswordPolicy selectedPasswordPolicy = null;
		UserRole selectedUserRole = null;
		Integer nuserrolecode = null;

		if (npolicycode == null) {

			final String strQuery = "Select nuserrolecode, suserrolename, sdescription, nsitecode, nstatus from userrole where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nuserrolecode > 0 and nsitecode = " + userInfo.getNmastersitecode();
			LOGGER.info(strQuery);
			final List<UserRole> userRoleList = (List<UserRole>) jdbcTemplate.query(strQuery, new UserRole());

			outputMap.put("UserRole", userRoleList);

			if (userRoleList == null || userRoleList.isEmpty()) {
				outputMap.put("SelectedPasswordPolicy", null);
				outputMap.put("PasswordPolicy", userRoleList);
				outputMap.put("SelectedUserRole", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				{
					selectedUserRole = (userRoleList.get(userRoleList.size() - 1));
					nuserrolecode = selectedUserRole.getNuserrolecode();
					outputMap.putAll(
							(Map<String, Object>) getPasswordPolicyByUserRoleCode(nuserrolecode, userInfo).getBody());
				}
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		} else {

			selectedPasswordPolicy = getActivePasswordPolicyById(npolicycode, userInfo);

			if (selectedPasswordPolicy != null) {
				final String sQuery = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar, "
						+ "p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt, "
						+ "p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,u.nuserrolepolicycode,"
						+ " u.nuserrolecode, u.npolicycode, u.ntransactionstatus, u.nsitecode, u.nstatus,"
						+ " coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
						+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus ,"
						+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
						+ " t1.jsondata->'stransdisplaystatus'->>'en-US') as sexpirystatus "
						+ " from passwordpolicy p,userrolepolicy u,transactionstatus t,transactionstatus t1 where p.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and p.npolicycode > 0 and p.npolicycode = u.npolicycode and u.ntransactionstatus = t.ntranscode and p.nexpirypolicyrequired = t1.ntranscode and u.nuserrolecode = "
						+ selectedPasswordPolicy.getNuserrolecode() + " and u.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t1.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode="
						+ userInfo.getNmastersitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode()
						+ " order by p.npolicycode";
				List<PasswordPolicy> passwordList = (List<PasswordPolicy>) jdbcTemplate.query(sQuery,
						new PasswordPolicy());

				final String querys = "select nuserrolecode, suserrolename, sdescription, nsitecode, nstatus from userrole where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode="
						+ selectedPasswordPolicy.getNuserrolecode() + " and nsitecode=" + userInfo.getNmastersitecode();

				UserRole userRole = (UserRole) jdbcUtilityFunction.queryForObject(querys, UserRole.class, jdbcTemplate);

				outputMap.put("PasswordPolicy", passwordList);

				outputMap.put("SelectedUserRole", userRole);

				outputMap.put("SelectedPasswordPolicy", selectedPasswordPolicy);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	/**
	 * This method is used to retrieve list of all active passwordpolicy for the
	 * specified site.
	 *
	 * @param nuserrolecode [int] key of passwordpolicy object for which the list is
	 *                      to be fetched
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the list is to be
	 *                 checked
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getPasswordPolicyAfterSave(Integer nuserrolecode, Integer npolicycode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		PasswordPolicy selectedPasswordPolicy = null;

		final String sQuery = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar, "
				+ "p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt, "
				+ "p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,u.nuserrolepolicycode,"
				+ " u.nuserrolecode, u.npolicycode, u.ntransactionstatus, u.nsitecode, u.nstatus,"
				+ " coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus ,"
				+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t1.jsondata->'stransdisplaystatus'->>'en-US') as sexpirystatus "
				+ " from passwordpolicy p,userrolepolicy u,transactionstatus t,transactionstatus t1 where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npolicycode > 0 and p.npolicycode = u.npolicycode and u.ntransactionstatus = t.ntranscode"
				+ " and p.nexpirypolicyrequired = t1.ntranscode and u.nuserrolecode = " + nuserrolecode
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nsitecode="+ userInfo.getNmastersitecode()+ " and u.nsitecode="+ userInfo.getNmastersitecode()
				+ " order by u.npolicycode asc";
		List<PasswordPolicy> passwordList = (List<PasswordPolicy>) jdbcTemplate.query(sQuery, new PasswordPolicy());

		outputMap.put("PasswordPolicy", passwordList);

		selectedPasswordPolicy = getActivePasswordPolicyById(npolicycode, userInfo);
		if (!passwordList.isEmpty()) {
			if (selectedPasswordPolicy == null) {
				selectedPasswordPolicy = (passwordList.get(passwordList.size() - 1));
			}
		}
		outputMap.put("SelectedPasswordPolicy", selectedPasswordPolicy);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all active passwordpolicy for the
	 * specified site.
	 *
	 * @param nuserrolecode [int] key of passwordpolicy object for which the list is
	 *                      to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPasswordPolicyByUserRoleCode(Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		PasswordPolicy selectedPasswordPolicy = null;
		final String strQuery = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
				+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt,"
				+ " p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,u.nuserrolepolicycode,"
				+ " u.nuserrolecode, u.npolicycode, u.ntransactionstatus, u.nsitecode, u.nstatus,"
				+ " coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus ,"
				+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t1.jsondata->'stransdisplaystatus'->>'en-US') as sexpirystatus "
				+ " from passwordpolicy p,userrolepolicy u,transactionstatus t,transactionstatus t1 where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npolicycode > 0 and p.npolicycode = u.npolicycode and u.ntransactionstatus = t.ntranscode and p.nexpirypolicyrequired = t1.ntranscode and "
				+ " u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nuserrolecode = "
				+ nuserrolecode + " and p.nsitecode="+ userInfo.getNmastersitecode()+ " and u.nsitecode="
				+ userInfo.getNmastersitecode()+ " order by p.npolicycode";

		List<PasswordPolicy> passwordList = (List<PasswordPolicy>) jdbcTemplate.query(strQuery, new PasswordPolicy());

		final String querys = "select nuserrolecode, suserrolename, sdescription, nsitecode, nstatus from userrole where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode=" + nuserrolecode;
		UserRole userRole = (UserRole) jdbcUtilityFunction.queryForObject(querys, UserRole.class, jdbcTemplate);

		outputMap.put("SelectedUserRole", userRole);
		if (passwordList == null || passwordList.isEmpty()) {

			outputMap.put("SelectedPasswordPolicy", null);
			outputMap.put("PasswordPolicy", passwordList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {

			outputMap.put("PasswordPolicy", passwordList);
			selectedPasswordPolicy = (passwordList.get(passwordList.size() - 1));

			outputMap.put("SelectedPasswordPolicy", selectedPasswordPolicy);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active passwordpolicy object based on the
	 * specified npolicycode.
	 *
	 * @param npolicycode [int] primary key of passwordpolicy object
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         passwordpolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public PasswordPolicy getActivePasswordPolicyById(final int npolicycode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
				+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt,"
				+ " p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,u.nuserrolepolicycode,"
				+ " u.nuserrolecode, u.npolicycode, u.ntransactionstatus, u.nsitecode, u.nstatus,"
				+ " coalesce(t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus ,"
				+ " coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " t1.jsondata->'stransdisplaystatus'->>'en-US') as sexpirystatus "
				+ " from passwordpolicy p,userrolepolicy u,transactionstatus t,transactionstatus t1 where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npolicycode > 0 and p.npolicycode = u.npolicycode and u.ntransactionstatus = t.ntranscode and p.nexpirypolicyrequired = t1.ntranscode and "
				+ " u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.npolicycode = " + npolicycode
				+ " and u.nsitecode="+ userInfo.getNmastersitecode();

		return (PasswordPolicy) jdbcUtilityFunction.queryForObject(strQuery, PasswordPolicy.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active passwordpolicy object based on the
	 * specified npolicycode.
	 *
	 * @param nuserrolecode [int] key of passwordpolicy object
	 * @return response entity object holding response status and data of
	 *         passwordpolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public PasswordPolicy getActivePasswordPolicyByUserRoleCode(final int nuserrolecode) throws Exception {

		final String strQuery = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
				+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt,"
				+ " p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,"
				+ "t.stransdisplaystatus sexpirystatus from passwordpolicy p,userrolepolicy u,transactionstatus t where p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npolicycode > 0 and p.npolicycode = u.npolicycode and p.nexpirypolicyrequired = t.ntranscode and "
				+ " u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nuserrolecode = "
				+ nuserrolecode;

		return (PasswordPolicy) jdbcUtilityFunction.queryForObject(strQuery, PasswordPolicy.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to passwordpolicy table. Need to check
	 * for duplicate entry of passwordpolicy name for the specified site before
	 * saving into database.
	 *
	 * @param passwordpolicy [PasswordPolicy] object holding details to be added in
	 *                       passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param nuserrolecode [int] primary key of userrole object for which the passwordpolicy
	 *                    record will get
	 * @return response entity object holding response status and data of added
	 *         passwordpolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createPasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo,
			Integer nuserrolecode) throws Exception {

		final String sQuery = " lock  table lockpasswordpolicy " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedPasswordPolicyList = new ArrayList<>();
		passwordPolicy.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
		passwordPolicy.setNsitecode(userInfo.getNmastersitecode());

		PasswordPolicy passwordPolicy1 = (PasswordPolicy) jdbcUtilityFunction
				.queryForObject("select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
						+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength,"
						+ " p.nnooffailedattempt, p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays,"
						+ " p.scomments, p.nsitecode, p.nstatus, u.nuserrolepolicycode, u.nuserrolecode, u.npolicycode,"
						+ " u.ntransactionstatus, u.nsitecode, u.nstatus from passwordpolicy p , userrolepolicy u where p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and p.npolicycode = u.npolicycode and u.nuserrolecode = " + nuserrolecode + " "
						+ " and p.spolicyname=N'" + stringUtilityFunction.replaceQuote(passwordPolicy.getSpolicyname())
						+ "' and p.nsitecode="+ userInfo.getNmastersitecode()+" and u.nsitecode="+ userInfo.getNmastersitecode()
						+ "", PasswordPolicy.class, jdbcTemplate);

		if (passwordPolicy1 == null) {

			final String getSeqNo = "select stablename,nsequenceno from seqnoconfigurationmaster where stablename "
					+ "in ('passwordpolicy','userrolepolicy')";

			List<SeqNoConfigurationMaster> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqNoConfigurationMaster());
			Map<String, Integer> seqMap = lstSeqNo.stream().collect(Collectors
					.toMap(SeqNoConfigurationMaster::getStablename, SeqNoConfigurationMaster::getNsequenceno));
			int npasswordPolicySeqNo = seqMap.get("passwordpolicy");
			int nuserRolePolicySeqNo = seqMap.get("userrolepolicy");
			npasswordPolicySeqNo++;
			nuserRolePolicySeqNo++;
			String scomments = passwordPolicy.getScomments() == null ? ""
					: stringUtilityFunction.replaceQuote(passwordPolicy.getScomments());
			String strPasswordPolicy = "insert into passwordpolicy (npolicycode, spolicyname, nminnoofnumberchar,"
					+ " nminnooflowerchar, nminnoofupperchar, nminnoofspecialchar, nminpasslength, nmaxpasslength,"
					+ " nnooffailedattempt, nexpirypolicyrequired, nexpirypolicy, nremainderdays, scomments,"
					+ " dmodifieddate, nsitecode, nstatus) values (" + npasswordPolicySeqNo + ", '"
					+ passwordPolicy.getSpolicyname() + "', " + passwordPolicy.getNminnoofnumberchar() + ", "
					+ passwordPolicy.getNminnooflowerchar() + ", " + passwordPolicy.getNminnoofupperchar() + ", "
					+ passwordPolicy.getNminnoofspecialchar() + ", " + passwordPolicy.getNminpasslength() + ", "
					+ passwordPolicy.getNmaxpasslength() + ", " + passwordPolicy.getNnooffailedattempt() + ", "
					+ passwordPolicy.getNexpirypolicyrequired() + ", " + passwordPolicy.getNexpirypolicy() + ", "
					+ passwordPolicy.getNremainderdays() + ", '" + scomments + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

			strPasswordPolicy += "insert into userrolepolicy (nuserrolepolicycode, nuserrolecode, npolicycode,"
					+ " ntransactionstatus, dmodifieddate, nsitecode, nstatus) values (" + nuserRolePolicySeqNo + ", "
					+ nuserrolecode + ", " + npasswordPolicySeqNo + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

			strPasswordPolicy += "update seqnoconfigurationmaster set nsequenceno=" + npasswordPolicySeqNo
					+ " where stablename='passwordpolicy'; ";
			strPasswordPolicy += "update seqnoconfigurationmaster set nsequenceno=" + nuserRolePolicySeqNo
					+ " where stablename='userrolepolicy'; ";

			jdbcTemplate.execute(strPasswordPolicy);
			passwordPolicy.setNpolicycode(npasswordPolicySeqNo);
			savedPasswordPolicyList.add(passwordPolicy);
			multilingualIDList.add("IDS_ADDPASSWORDPOLICY");

			UserRolePolicy objUserRolePolicy = new UserRolePolicy();
			objUserRolePolicy.setNuserrolepolicycode(nuserRolePolicySeqNo);
			objUserRolePolicy.setNuserrolecode(nuserrolecode);
			objUserRolePolicy.setNpolicycode(passwordPolicy.getNpolicycode());
			objUserRolePolicy.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
			objUserRolePolicy.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
			objUserRolePolicy.setNsitecode(userInfo.getNmastersitecode());
			savedPasswordPolicyList.add(objUserRolePolicy);
			multilingualIDList.add("IDS_ADDUSERROLEPOLICY");

			auditUtilityFunction.fnInsertAuditAction(savedPasswordPolicyList, 1, null, multilingualIDList, userInfo);

			// status code:200

			return getPasswordPolicyAfterSave(nuserrolecode, passwordPolicy.getNpolicycode(), userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to update entry in passwordpolicy table. Need to validate
	 * that the passwordpolicy object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of passwordpolicy for
	 * the specified site before saving into database.
	 *
	 * @param passwordpolicy [PasswordPolicy] object holding details to be updated
	 *                       in passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         passwordpolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updatePasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();

		final PasswordPolicy passwordPolicyByID = getActivePasswordPolicyById(passwordPolicy.getNpolicycode(),
				userInfo);
		if (passwordPolicyByID == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (passwordPolicyByID.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				final String queryString = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
						+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt,"
						+ " p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus, u.nuserrolepolicycode,"
						+ " u.nuserrolecode, u.npolicycode, u.ntransactionstatus, u.nsitecode, u.nstatus from passwordpolicy p , userrolepolicy u where p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and p.npolicycode = u.npolicycode " + " and p.npolicycode <> "
						+ passwordPolicy.getNpolicycode() + "" + " and u.nuserrolecode = "
						+ passwordPolicy.getNuserrolecode() + " " + " and p.spolicyname=N'"
						+ stringUtilityFunction.replaceQuote(passwordPolicy.getSpolicyname()) + "'"
						+ " and p.nsitecode="+ userInfo.getNmastersitecode()+" and u.nsitecode="+ userInfo.getNmastersitecode();

				final List<PasswordPolicy> passwordPolicyList = (List<PasswordPolicy>) jdbcTemplate.query(queryString,
						new PasswordPolicy());

				if (passwordPolicyList.isEmpty()) {
					final String updateQueryString = "update passwordpolicy set spolicyname=N'"
							+ stringUtilityFunction.replaceQuote(passwordPolicy.getSpolicyname())
							+ "', nminnoofnumberchar =" + passwordPolicy.getNminnoofnumberchar()
							+ ",nminnooflowerchar = " + passwordPolicy.getNminnooflowerchar() + ",nminnoofupperchar = "
							+ passwordPolicy.getNminnoofupperchar() + ",nminnoofspecialchar = "
							+ passwordPolicy.getNminnoofspecialchar() + ",nminpasslength = "
							+ passwordPolicy.getNminpasslength() + ",nmaxpasslength = "
							+ passwordPolicy.getNmaxpasslength() + ",nnooffailedattempt = "
							+ passwordPolicy.getNnooffailedattempt() + " ,nexpirypolicyrequired = "
							+ passwordPolicy.getNexpirypolicyrequired() + ",nexpirypolicy = "
							+ passwordPolicy.getNexpirypolicy() + ",nremainderdays = "
							+ passwordPolicy.getNremainderdays() + ",scomments = N'"
							+ stringUtilityFunction.replaceQuote(passwordPolicy.getScomments()) + "', dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nsitecode = "
							+ userInfo.getNmastersitecode() + " where npolicycode=" + passwordPolicy.getNpolicycode();

					jdbcTemplate.execute(updateQueryString);

					multilingualIDList.add("IDS_EDITPASSWORDPOLICY");

					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(passwordPolicy);

					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(passwordPolicyByID);

					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);

					// status code:200

					return getPasswordPolicyAfterSave(passwordPolicyByID.getNuserrolecode(),
							passwordPolicy.getNpolicycode(), userInfo);

				} else {
					// Conflict = 409 - Duplicate entry
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to delete entry in passwordpolicy table. Need to validate
	 * that the specified passwordpolicy object is active and is not associated with
	 * any of its child tables before updating its nstatus to -1.
	 *
	 * @param passwordpolicy [PasswordPolicy] object holding detail to be deleted in
	 *                       passwordpolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         passwordpolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deletePasswordPolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {
		final PasswordPolicy passwordPolicyById = getActivePasswordPolicyById(passwordPolicy.getNpolicycode(),
				userInfo);
		if (passwordPolicyById == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (passwordPolicyById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedPasswordPolicyList = new ArrayList<>();
				final String updateQueryString = "update passwordpolicy set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where npolicycode="
						+ passwordPolicy.getNpolicycode();

				jdbcTemplate.execute(updateQueryString);

				passwordPolicy.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedPasswordPolicyList.add(passwordPolicy);
				multilingualIDList.add("IDS_DELETEPASSWORDPOLICY");

				final String queryString = "select nuserrolepolicycode, nuserrolecode, npolicycode,"
						+ " ntransactionstatus, nsitecode, nstatus from userrolepolicy where npolicycode="
						+ passwordPolicy.getNpolicycode()+ " and nsitecode="+ userInfo.getNmastersitecode();

				final UserRolePolicy userRolePolicyById = (UserRolePolicy) jdbcUtilityFunction
						.queryForObject(queryString, UserRolePolicy.class, jdbcTemplate);

				final String updateQuery = "update userrolepolicy set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where npolicycode="
						+ passwordPolicy.getNpolicycode(); // userRolePolicy.getNuserrolepolicycode();

				jdbcTemplate.execute(updateQuery);

				savedPasswordPolicyList.add(userRolePolicyById);
				passwordPolicy.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				multilingualIDList.add("IDS_DELETEUSERROLEPOLICY");

				auditUtilityFunction.fnInsertAuditAction(savedPasswordPolicyList, 1, null, multilingualIDList,
						userInfo);

				return getPasswordPolicyAfterSave(passwordPolicyById.getNuserrolecode(),
						passwordPolicy.getNpolicycode(), userInfo);

			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to delete entry in userrolepolicy table. Need to validate
	 * that the specified userrolepolicy object is active and is not associated with
	 * any of its child tables before updating its ntransactionstatus .
	 *
	 * @param userrolepolicy [UserRolePolicy] object holding detail to be deleted in
	 *                       userrolepolicy table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         userrolepolicy object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> approveUserRolePolicy(PasswordPolicy passwordPolicy, UserInfo userInfo)
			throws Exception {

		final UserRolePolicy userRolePolicyById = getActiveUserRolePolicyById(passwordPolicy.getNuserrolepolicycode());// .getBody();

		if (userRolePolicyById == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (userRolePolicyById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				final String sessionVal = "select nuserrolecode from sessiondetails where nuserrolecode = "
						+ userRolePolicyById.getNuserrolecode() + " and nsitecode=" + userInfo.getNmastersitecode()
						+ " and " + "nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " group by nuserrolecode";

				UserRole sessionValObj = (UserRole) jdbcUtilityFunction.queryForObject(sessionVal, UserRole.class,
						jdbcTemplate);

				if (sessionValObj != null) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_USERROLELOGUT", userInfo.getSlanguagefilename())
									+ "",
							HttpStatus.EXPECTATION_FAILED);
				}

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedUserRolePolicyList = new ArrayList<>();

				final UserRolePolicy userRoleCodeById = getActiveUserRoleCodeById(passwordPolicy.getNuserrolecode());// .getBody();

				if (userRoleCodeById != null) {

					if (userRoleCodeById.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
							.gettransactionstatus()) {
						final String updateQuery = "update userrolepolicy set ntransactionstatus = "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nuserrolepolicycode="
								+ userRoleCodeById.getNuserrolepolicycode();

						jdbcTemplate.execute(updateQuery);

						userRoleCodeById.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());

						savedUserRolePolicyList.add(userRoleCodeById);
						multilingualIDList.add("IDS_RETIREDUSERROLEPOLICY");
					}
				}

				final String updateQueryString = "update userrolepolicy set ntransactionstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nuserrolepolicycode="
						+ passwordPolicy.getNuserrolepolicycode(); // userRolePolicy.getNuserrolepolicycode();

				jdbcTemplate.execute(updateQueryString);

				final String UpdateNullString = "update usermultirole set spassword=NULL,dpasswordvalidatedate=NULL "
						+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nuserrolecode=" + passwordPolicy.getNuserrolecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultrole="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus();

				jdbcTemplate.execute(UpdateNullString);

				passwordPolicy
						.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

				final UserRolePolicy approvedPolicy = new UserRolePolicy();
				approvedPolicy.setNpolicycode(passwordPolicy.getNpolicycode());
				approvedPolicy
						.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
				approvedPolicy.setNuserrolepolicycode(passwordPolicy.getNuserrolepolicycode());
				approvedPolicy.setNuserrolecode(passwordPolicy.getNuserrolecode());

				savedUserRolePolicyList.add(approvedPolicy);
				multilingualIDList.add("IDS_APPROVEUSERROLEPOLICY");

				auditUtilityFunction.fnInsertAuditAction(savedUserRolePolicyList, 1, null, multilingualIDList,
						userInfo);

				return getPasswordPolicyAfterSave(passwordPolicy.getNuserrolecode(), passwordPolicy.getNpolicycode(),
						userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		}

	}

	/**
	 * This method is used to retrieve active UserRolePolicy object based on the
	 * specified nuserrolepolicycode.
	 *
	 * @param nuserrolepolicycode [int] primary key of UserRolePolicy object
	 * @return response entity object holding response status and data of
	 *         UserRolePolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public UserRolePolicy getActiveUserRolePolicyById(final int nuserrolepolicycode) throws Exception {

		final String strQuery = "select u.nuserrolepolicycode, u.nuserrolecode, u.npolicycode, u.ntransactionstatus,"
				+ " u.nsitecode, u.nstatus from userrolepolicy u where u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nuserrolepolicycode = "
				+ nuserrolepolicycode;

		return (UserRolePolicy) jdbcUtilityFunction.queryForObject(strQuery, UserRolePolicy.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active UserRolePolicy object based on the
	 * specified nuserrolecode.
	 *
	 * @param nuserrolecode [int] primary key of UserRolePolicy object
	 * @return response entity object holding response status and data of
	 *         UserRolePolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public UserRolePolicy getActiveUserRoleCodeById(final int nuserrolecode) throws Exception {

		final String strQuery = "select u.nuserrolepolicycode, u.nuserrolecode, u.npolicycode, u.ntransactionstatus,"
				+ " u.nsitecode, u.nstatus from userrolepolicy u where u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and u.nuserrolecode = "
				+ nuserrolecode;

		return (UserRolePolicy) jdbcUtilityFunction.queryForObject(strQuery, UserRolePolicy.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active UserRolePolicy object based on the
	 * specified nuserrolecode.
	 *
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         UserRolePolicy object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUserRolePolicy(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "Select nuserrolecode, suserrolename, sdescription, nsitecode, nstatus from userrole where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<UserRole> userRoleList = jdbcTemplate.query(strQuery, new UserRole());
		outputMap.put("UserRole", userRoleList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to copy the active passwordpolicy to the specified
	 * userrole.
	 *
	 * @param lstuserRole [UserRole] object holding list of details of userrole
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the
	 *                    reocord is copied to specified userrole
	 * @param spolicyname [String] string field of passwordpolicy object for which the passwordpolicy name to check for duplicate record
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> copyPasswordPolicyToSelectedRole(List<UserRole> lstuserRole, final int npolicycode,
			UserInfo userInfo, final String spolicyname) throws Exception {

		final String sQuery = " lock  table lockpasswordpolicy " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedPasswordPolicyList = new ArrayList<>();

		PasswordPolicy objPasswordPolicy = (PasswordPolicy) jdbcUtilityFunction.queryForObject(
				"select npolicycode, spolicyname, nminnoofnumberchar, nminnooflowerchar, nminnoofupperchar, nminnoofspecialchar,"
						+ " nminpasslength, nmaxpasslength, nnooffailedattempt, nexpirypolicyrequired, nexpirypolicy, nremainderdays,"
						+ " scomments, nsitecode, nstatus from PasswordPolicy where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npolicycode = "
						+ npolicycode + "",
				PasswordPolicy.class, jdbcTemplate);
		if (objPasswordPolicy != null) {
			SeqNoConfigurationMaster objSequence = (SeqNoConfigurationMaster) jdbcUtilityFunction.queryForObject(
					"select * from seqnoconfigurationmaster where stablename='passwordpolicy'",
					SeqNoConfigurationMaster.class, jdbcTemplate);
			int sequence = objSequence.getNsequenceno();

			SeqNoConfigurationMaster objSequenceUserRolePolicy = (SeqNoConfigurationMaster) jdbcUtilityFunction
					.queryForObject("select * from seqnoconfigurationmaster where stablename='userrolepolicy'",
							SeqNoConfigurationMaster.class, jdbcTemplate);
			int sequenceNoUserRolePolicy = objSequenceUserRolePolicy.getNsequenceno();

			List<PasswordPolicy> lstPasswordPolicy = new ArrayList<PasswordPolicy>();
			List<UserRolePolicy> lstUserrolePolicy = new ArrayList<UserRolePolicy>();

			String nuserrolecode = stringUtilityFunction.fnDynamicListToString(lstuserRole, "getNuserrolecode");

			String query = "select p.npolicycode, p.spolicyname, p.nminnoofnumberchar, p.nminnooflowerchar,"
					+ " p.nminnoofupperchar, p.nminnoofspecialchar, p.nminpasslength, p.nmaxpasslength, p.nnooffailedattempt,"
					+ " p.nexpirypolicyrequired, p.nexpirypolicy, p.nremainderdays, p.scomments, p.nsitecode, p.nstatus,"
					+ " u.nuserrolecode from PasswordPolicy p,userrolepolicy u where"
					+ " p.npolicycode=u.npolicycode  and p.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spolicyname ilike N'"
					+ stringUtilityFunction.replaceQuote(spolicyname) + "%' collate \"default\""
					+ " and nuserrolecode in ( " + nuserrolecode + ") and p.nsitecode="+ userInfo.getNmastersitecode()
					+ " and u.nsitecode="+ userInfo.getNmastersitecode();

			List<PasswordPolicy> lstdata = (List<PasswordPolicy>) jdbcTemplate.query(query, new PasswordPolicy());

			if (lstuserRole.size() > 0) {
				String strPasswordPolicy = "";
				String strUserRolePolicy = "";
				for (UserRole element : lstuserRole) {
					String policyname = stringUtilityFunction.replaceQuote(spolicyname);
					PasswordPolicy objPasswordPoliy = new PasswordPolicy();

					List<PasswordPolicy> objPasswordPolicy1 = lstdata.stream()
							.filter(x -> x.getNuserrolecode() == element.getNuserrolecode())
							.collect(Collectors.toList());
					if (!objPasswordPolicy1.isEmpty()) {
						int count = 0;
						Boolean check = false;
						int j = 0;
						Inner: for (int i = 0; i < objPasswordPolicy1.size(); i++) {
							if (!objPasswordPolicy1.get(i).getSpolicyname().equals(policyname)) {
								if (objPasswordPolicy1.get(i).getSpolicyname().equals(policyname)
										|| objPasswordPolicy1.get(i).getSpolicyname().contains(
												policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
														userInfo.getSlanguagefilename()) + "(")) {
									j = j + 1;

									String val = policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
											userInfo.getSlanguagefilename()) + "(" + j + ")";

									List<PasswordPolicy> objPasswordPolicy2 = objPasswordPolicy1.stream()
											.filter(x -> x.getSpolicyname().equals(val)).collect(Collectors.toList());

									if (objPasswordPolicy2.isEmpty()) {
										policyname = policyname + "-" + commonFunction.getMultilingualMessage(
												"IDS_COPY", userInfo.getSlanguagefilename()) + "(" + j + ")";
										check = true;
										break Inner;
									} else {
										count = count + 1;
									}
								}
							}
						}

						if (!check) {
							if (count == 0) {
								count = 1;
								policyname = policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
										userInfo.getSlanguagefilename()) + "(" + count + ")";
							} else {
								count = count + 1;
								policyname = policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
										userInfo.getSlanguagefilename()) + "(" + count + ")";
							}
						}

						objPasswordPoliy.setSpolicyname(policyname);
					} else {
						objPasswordPoliy.setSpolicyname(spolicyname);
					}

					sequence++;
					objPasswordPoliy.setNexpirypolicy(objPasswordPolicy.getNexpirypolicy());
					objPasswordPoliy.setNmaxpasslength(objPasswordPolicy.getNmaxpasslength());
					objPasswordPoliy.setNminnooflowerchar(objPasswordPolicy.getNminnooflowerchar());
					objPasswordPoliy.setNminnoofnumberchar(objPasswordPolicy.getNminnoofnumberchar());
					objPasswordPoliy.setNminnoofspecialchar(objPasswordPolicy.getNminnoofspecialchar());
					objPasswordPoliy.setNminnoofupperchar(objPasswordPolicy.getNminnoofupperchar());
					objPasswordPoliy.setNminpasslength(objPasswordPolicy.getNminpasslength());
					objPasswordPoliy.setNnooffailedattempt(objPasswordPolicy.getNnooffailedattempt());
					objPasswordPoliy.setNremainderdays(objPasswordPolicy.getNremainderdays());
					objPasswordPoliy.setNstatus(objPasswordPolicy.getNstatus());
					objPasswordPoliy.setNtransactionstatus(objPasswordPolicy.getNtransactionstatus());
					objPasswordPoliy.setScomments(objPasswordPolicy.getScomments());
					objPasswordPoliy.setNexpirypolicyrequired(objPasswordPolicy.getNexpirypolicyrequired());
					objPasswordPoliy.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
					objPasswordPoliy.setNsitecode(objPasswordPolicy.getNsitecode());
					objPasswordPoliy.setNpolicycode(sequence);

					String scomments = objPasswordPolicy.getScomments() == null ? ""
							: stringUtilityFunction.replaceQuote(objPasswordPolicy.getScomments());
					strPasswordPolicy += " (" + sequence + ", '" + objPasswordPoliy.getSpolicyname() + "', "
							+ objPasswordPolicy.getNminnoofnumberchar() + ", "
							+ objPasswordPolicy.getNminnooflowerchar() + ", " + objPasswordPolicy.getNminnoofupperchar()
							+ ", " + objPasswordPolicy.getNminnoofspecialchar() + ", "
							+ objPasswordPolicy.getNminpasslength() + ", " + objPasswordPolicy.getNmaxpasslength()
							+ ", " + objPasswordPolicy.getNnooffailedattempt() + ", "
							+ objPasswordPolicy.getNexpirypolicyrequired() + ", " + objPasswordPolicy.getNexpirypolicy()
							+ ", " + objPasswordPolicy.getNremainderdays() + ", '" + scomments + "', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ objPasswordPolicy.getNsitecode() + ", " + objPasswordPolicy.getNstatus() + "),";
					lstPasswordPolicy.add(objPasswordPoliy);

					UserRolePolicy objUserPolicy = new UserRolePolicy();
					sequenceNoUserRolePolicy++;
					objUserPolicy.setNuserrolecode(element.getNuserrolecode());
					objUserPolicy.setNpolicycode(sequence);
					objUserPolicy
							.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
					objUserPolicy.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
					objUserPolicy.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
					objUserPolicy.setNsitecode(userInfo.getNmastersitecode());
					objUserPolicy.setNuserrolepolicycode(sequenceNoUserRolePolicy);

					strUserRolePolicy += " (" + sequenceNoUserRolePolicy + ", " + element.getNuserrolecode() + ", "
							+ sequence + ", " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					lstUserrolePolicy.add(objUserPolicy);
				}

				String strPasswordPolicyInsertQry = "insert into passwordpolicy (npolicycode, spolicyname, nminnoofnumberchar,"
						+ " nminnooflowerchar, nminnoofupperchar, nminnoofspecialchar, nminpasslength, nmaxpasslength,"
						+ " nnooffailedattempt, nexpirypolicyrequired, nexpirypolicy, nremainderdays, scomments,"
						+ " dmodifieddate, nsitecode, nstatus) values "
						+ strPasswordPolicy.subSequence(0, strPasswordPolicy.length() - 1) + ";";
				strPasswordPolicyInsertQry += "update seqnoconfigurationmaster set nsequenceno=" + sequence
						+ " where stablename='passwordpolicy';";

				String strUserRolePolicyInsertQry = "insert into userrolepolicy (nuserrolepolicycode, nuserrolecode, npolicycode,"
						+ " ntransactionstatus, dmodifieddate, nsitecode, nstatus) values "
						+ strUserRolePolicy.subSequence(0, strUserRolePolicy.length() - 1) + ";";
				strUserRolePolicyInsertQry += "update seqnoconfigurationmaster set nsequenceno="
						+ sequenceNoUserRolePolicy + " where stablename='userrolepolicy';";

				jdbcTemplate.execute(strPasswordPolicyInsertQry + strUserRolePolicyInsertQry);

				multilingualIDList.add("IDS_ADDPASSWORDPOLICY");
				savedPasswordPolicyList.add(lstPasswordPolicy);
				savedPasswordPolicyList.add(lstUserrolePolicy);
				multilingualIDList.add("IDS_COPYUSERROLEPOLICY");

				auditUtilityFunction.fnInsertListAuditAction(savedPasswordPolicyList, 1, null, multilingualIDList,
						userInfo);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getPasswordPolicy(npolicycode, userInfo);
	}
}