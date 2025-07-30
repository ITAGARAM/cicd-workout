package com.agaramtech.qualis.control.service;

/**
 * This class is used to perform CRUD Operation on "userrolescreen and userrolescreencontrol" table by implementing
* methods from its interface. 
* @author ATE234
* @version 9.0.0.1
 *@since 04-04-2020
 */

import java.util.ArrayList;
import java.util.Arrays;
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

import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.SeqNoCredentialManagement;
import com.agaramtech.qualis.credential.model.SiteControlMaster;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "screenrights" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class ControlDAOImpl implements ControlDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	// private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	// private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified site.
	 * 
	 * @param objUserInfo         object is used for fetched the list of active
	 *                            records based on site
	 * @param nuserrolescreencode [int] is used to fectched the specified
	 *                            userrolescreen record.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getScreenRights(Integer nuserrolescreencode, UserInfo objUserInfo) throws Exception {

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		UserRole selectedUserRole = null;
		Integer nuserrolecode = null;
		if (nuserrolescreencode == null) {
			String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nsitecode="
					+ objUserInfo.getNmastersitecode() + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			// List<Map<String, Object>> lstuserrole = jdbcTemplate.queryForList(query);
			LOGGER.info("Get Query -->" + query);

			List<UserRole> lstuserrole = (List<UserRole>) jdbcTemplate.query(query, new UserRole());
			objmap.put("userrole", lstuserrole);
			if (lstuserrole.size() == 0) {
				objmap.put("SelectedUserRole", null);
				objmap.put("SelectedScreenRights", lstuserrole);
				objmap.put("ScreenRights", lstuserrole);
				objmap.put("ControlRights", lstuserrole);
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			} else {
				if (nuserrolescreencode == null) {
					selectedUserRole = ((UserRole) lstuserrole.get(lstuserrole.size() - 1));
					nuserrolecode = selectedUserRole.getNuserrolecode();
					objmap.putAll(
							(Map<String, Object>) getScreenRightsByUserRoleCode(nuserrolecode, objUserInfo).getBody());
				}
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}
		} else {
			final String query = "select * from usersrolescreen where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolescreencode="
					+ nuserrolescreencode;
			List<UsersRoleScreen> selectedScreenRights = (List<UsersRoleScreen>) jdbcTemplate.query(query,
					new UsersRoleScreen ());
			if (!selectedScreenRights.isEmpty()) {
				final String sQuery = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode,"
						+ " coalesce(q.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
						+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
						+ " us.nuserrolescreencode from qualisforms q,"
						+ " usersrolescreen us,userrole ur,sitequalisforms sf"
						+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode  and us.nformcode=q.nformcode and ur.nuserrolecode=us.nuserrolecode "
						+ " and sf.nsitecode=" + objUserInfo.getNmastersitecode() + " and sf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode="
						+ selectedScreenRights.get(selectedScreenRights.size() - 1).getNuserrolecode()
						+ " order by nuserrolescreencode asc";
				List<UsersRoleScreen> userrolescreenList = (List<UsersRoleScreen>) jdbcTemplate.query(sQuery,
						new UsersRoleScreen());

				objmap.put("ScreenRights", userrolescreenList);
				objmap.put("SelectedScreenRights", selectedScreenRights);
				objmap.putAll((Map<String, Object>) getControlRights(objUserInfo,
						selectedScreenRights.get(selectedScreenRights.size() - 1).getNuserrolescreencode()).getBody());
			}
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified site.
	 * 
	 * @param nuserrolecode [int] is used to fetch the list of records based on
	 *                      nuserrolecode.
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getScreenRightsByUserRoleCode(Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String query = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode,"
				+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " us.nuserrolescreencode from qualisforms q," + " usersrolescreen us,userrole ur,sitequalisforms sf"
				+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode and ur.nuserrolecode=us.nuserrolecode "
				+ " and sf.nsitecode=" + userInfo.getNmastersitecode() + " and sf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode=" + nuserrolecode
				+ "  order by nuserrolescreencode asc";

		List<UsersRoleScreen> userrolescreenList = (List<UsersRoleScreen>) jdbcTemplate.query(query,
				new UsersRoleScreen());

		String querys = "select * from userrole where nuserrolecode=" + nuserrolecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UserRole> userroleList = (List<UserRole>) jdbcTemplate.query(querys, new UserRole());

		outputMap.put("ScreenRights", userrolescreenList);
		outputMap.put("SelectedUserRole", userroleList.get(0));
		if (userrolescreenList.size() > 0) {
			final String Query = "select urs.*," + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
					+ "  from usersrolescreen urs , qualisforms q  where" + " q.nformcode=urs.nformcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and urs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and urs.nuserrolescreencode="
					+ userrolescreenList.get(userrolescreenList.size() - 1).getNuserrolescreencode();

			List<UsersRoleScreen> selectScreenRights = (List<UsersRoleScreen>) jdbcTemplate.query(Query,
					new UsersRoleScreen());

			outputMap.put("SelectedScreenRights", selectScreenRights);
			outputMap.putAll((Map<String, Object>) getControlRights(userInfo,
					selectScreenRights.get(selectScreenRights.size() - 1).getNuserrolescreencode()).getBody());
		} else {
			outputMap.put("SelectedScreenRights", userrolescreenList);
			outputMap.put("ControlRights", userrolescreenList);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active qualisformname for the
	 * specified site.
	 * 
	 * @param nuserrolecode [int] is used to fetch the list of records based on
	 *                      nuserrolecode.
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and list of all active
	 *         qualisformname
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> getAvailableScreen(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		if (nuserrolecode != null) {
			final String query = "select q.nformcode ," + nuserrolecode + " as nuserrolecode,"
					+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " q.jsondata->'sdisplayname'->>'en-US') as label, " + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " q.jsondata->'sdisplayname'->>'en-US') as value, "
					+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, " + userInfo.getNmastersitecode()
					+ " as nsitecode,q.nstatus from qualisforms q,sitequalisforms sq"
					+ " where sq.nformcode = q.nformcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sq.nsitecode="
					+ userInfo.getNmastersitecode() + " and sq.nformcode not in  "
					+ "(select urs.nformcode from  usersrolescreen urs  where urs.nformcode = sq.nformcode "
					+ "  and urs.nuserrolecode =" + nuserrolecode + " and urs.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " and sq.nformcode in  "
					+ "(select urs.nformcode from  usersrolescreen urs  where urs.nformcode = sq.nformcode "
					+ "  and urs.nuserrolecode =" + userInfo.getNuserrole() + " and urs.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ";

			return new ResponseEntity<>(jdbcTemplate.query(query, new UsersRoleScreen ()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTUSERROLE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to add a new entry to usersrolescreen table and
	 * userrolescreencontrol. Need to check for duplicate entry of nformcode for the
	 * specified site and specified userrole before saving into database and after
	 * complete insert of usersrolescreen then insert userrolescreencontrol based on
	 * usersrolescreen records default add nneedrights as 4 .
	 * lstacAvailableScreenArrayCollection [List] holding details of screenrights to
	 * be added in usersrolescreen table
	 * 
	 * @param inputMap [Map] map object with "screenrights" as key for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         screenrights object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createScreenRights(Map<String, Object> inputMap) throws Exception {

		final String sQueryLock = " lock  table lockscreenrights " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		String userrolescreencode = "";
		final List<Object> savedScreenRightsuserroleList = new ArrayList<>();
		final List<Object> beforeScreenRightsList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		List<UsersRoleScreen> lstacAvailableScreenArrayCollection = (List<UsersRoleScreen>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleScreen>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		userrolescreencode = stringUtilityFunction.fnDynamicListToString(lstacAvailableScreenArrayCollection,
				"getNformcode");

		int nuserrole = (int) inputMap.get("nuserrolecontrolcode");

		String existingRecordFormCode = "";
		List<UsersRoleScreen> lstNewAvailableScreenArrayCollection = new ArrayList<UsersRoleScreen>();

		final String query = "select * from usersrolescreen where nformcode in (" + userrolescreencode + ")"
				+ " and nuserrolecode=" + nuserrolecode;
		List<UsersRoleScreen> lstUser = (List<UsersRoleScreen>) jdbcTemplate.query(query, new UsersRoleScreen ());

		List<UsersRoleScreen> lstOldAvailableScreenArrayCollection = lstacAvailableScreenArrayCollection.stream()
				.filter(lstAvailableScreen -> lstUser.stream()
						.anyMatch(lstUsers -> lstUsers.getNformcode() == lstAvailableScreen.getNformcode()))
				.collect(Collectors.toList());

		existingRecordFormCode = stringUtilityFunction.fnDynamicListToString(lstOldAvailableScreenArrayCollection,
				"getNformcode");

		lstNewAvailableScreenArrayCollection = lstacAvailableScreenArrayCollection;

		lstNewAvailableScreenArrayCollection.removeAll(lstOldAvailableScreenArrayCollection);

		if (lstNewAvailableScreenArrayCollection.size() > 0) {

			final String sGetSeqNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename = 'usersrolescreen';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);

			String addstring = "";
			String sInsertQuery = "insert into usersrolescreen (nuserrolescreencode, nformcode, nuserrolecode, dmodifieddate, nstatus) values ";
			for (int i = 0; i < lstNewAvailableScreenArrayCollection.size(); i++) {

				String addstring1 = " ";
				nSeqNo++;
				if (i < lstNewAvailableScreenArrayCollection.size() - 1) {
					addstring1 = ",";
				}

				addstring = "(" + nSeqNo + ", " + lstNewAvailableScreenArrayCollection.get(i).getNformcode() + ", "
						+ lstNewAvailableScreenArrayCollection.get(i).getNuserrolecode() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + addstring1;
				lstNewAvailableScreenArrayCollection.get(i).setNuserrolescreencode(nSeqNo);

				sInsertQuery = sInsertQuery + addstring;
			}
			jdbcTemplate.execute(sInsertQuery);
			final String sUpdateSeqNoQuery = "update seqnocredentialmanagement set nsequenceno =" + nSeqNo
					+ " where stablename = 'usersrolescreen';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);

			savedScreenRightsuserroleList.add(lstNewAvailableScreenArrayCollection);
			userrolescreencode = stringUtilityFunction.fnDynamicListToString(lstNewAvailableScreenArrayCollection,
					"getNuserrolescreencode");

			String querys = "insert into userrolescreencontrol  "
					+ " (nuserrolecontrolcode,ncontrolcode,nformcode,nuserrolecode,nstatus,dmodifieddate,nneedrights,nneedesign "
					+ " )select " + nuserrole + " +RANK() over(order by ncontrolcode)"
					+ " nuserrolecontrolcode ,s.ncontrolcode,u.nformcode,u.nuserrolecode," + "u.nstatus,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nneedrights," + ""
					+ Enumeration.TransactionStatus.NO.gettransactionstatus()
					+ "  from  usersrolescreen u,sitecontrolmaster s where u.nuserrolescreencode in ("
					+ userrolescreencode + ") and " + " s.nformcode=u.nformcode and s.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and s.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(querys);

			querys = "select max(nuserrolecontrolcode) from userrolescreencontrol";
			Integer nuserrolecontrolcode = jdbcTemplate.queryForObject(querys, Integer.class);

			querys = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolecontrolcode) + ""
					+ " where stablename ='userrolescreencontrol'";
			jdbcTemplate.execute(querys);

		}
		if (lstOldAvailableScreenArrayCollection.size() > 0) {

			String findNewControls = "select ncontrolcode, nformcode from controlmaster where nformcode in ("
					+ existingRecordFormCode + ") and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<ControlMaster> newControls = (List<ControlMaster>) jdbcTemplate.query(findNewControls,
					new ControlMaster ());
			String findOldControls = "select ncontrolcode, nformcode from userrolescreencontrol where nformcode in ("
					+ existingRecordFormCode + ") and " + "nuserrolecode =" + nuserrolecode;
			List<ControlMaster> oldControls = (List<ControlMaster>) jdbcTemplate.query(findOldControls,
					new ControlMaster());
			List<ControlMaster> lstNewAvailableControlCollection = new ArrayList<ControlMaster>();

			List<ControlMaster> lstNewControlCollection = newControls.stream()
					.filter(NewControls -> oldControls.stream()
							.anyMatch(OldControls -> OldControls.getNcontrolcode() == NewControls.getNcontrolcode()))
					.collect(Collectors.toList());
			lstNewAvailableControlCollection = newControls;

			lstNewAvailableControlCollection.removeAll(lstNewControlCollection);

			if (lstNewAvailableControlCollection.size() > 0) {
				final String sGetSeqNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename = 'userrolescreencontrol';";
				int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);

				String addstring = "";
				String sInsertQuery = "insert into userrolescreencontrol (nuserrolecontrolcode,ncontrolcode,nformcode,nuserrolecode,"
						+ "nstatus,dmodifieddate,nneedrights,nneedesign) values ";
				for (int i = 0; i < lstNewAvailableControlCollection.size(); i++) {

					String addstring1 = " ";
					nSeqNo++;
					if (i < lstNewAvailableControlCollection.size() - 1) {
						addstring1 = ",";
					}

					addstring = "(" + nSeqNo + ", " + lstNewAvailableControlCollection.get(i).getNcontrolcode() + ", "
							+ lstNewAvailableControlCollection.get(i).getNformcode() + ", " + nuserrolecode + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", "
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ")" + addstring1;

					sInsertQuery = sInsertQuery + addstring;
				}
				jdbcTemplate.execute(sInsertQuery);
				final String sUpdateSeqNoQuery = "update seqnocredentialmanagement set nsequenceno =" + nSeqNo
						+ " where stablename = 'userrolescreencontrol';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);

			}

			final String userRoleScreenQuery = "update usersrolescreen set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nuserrolecode ="
					+ nuserrolecode + " and nformcode in (" + existingRecordFormCode + ")";
			jdbcTemplate.execute(userRoleScreenQuery);
			final String userRoleScreenControlQuery = "update userrolescreencontrol set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nformcode in ("
					+ existingRecordFormCode + ") and nuserrolecode =" + nuserrolecode;
			jdbcTemplate.execute(userRoleScreenControlQuery);
			final String auditGetQuery = "select * from usersrolescreen where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode ="
					+ nuserrolecode + " and nformcode in (" + existingRecordFormCode + ")";
			List<UsersRoleScreen> lstUserRoleScreen = (List<UsersRoleScreen>) jdbcTemplate.query(auditGetQuery,
					new UsersRoleScreen());
			savedScreenRightsuserroleList.add(lstUserRoleScreen);

		}

		auditUtilityFunction.fnInsertListAuditAction(savedScreenRightsuserroleList, 1, beforeScreenRightsList,
				Arrays.asList("IDS_ADDSCREENRIGHTS"), userInfo);

		return getScreenRightsByUserRoleCode(nuserrolecode, userInfo);

	}

	/**
	 * This method is used to retrieve list of all active controlrights for the
	 * specified site.
	 * 
	 * @param nuserrolescreencode [int] primary key of userrolescreen object for
	 *                            which the list is to be fetched
	 * @param userInfo            object is used for fetched the list of active
	 *                            records based on site.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getControlRights(UserInfo userInfo, Integer nuserrolescreencode) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " qs.jsondata->'sdisplayname'->>'en-US') as screenname, " + " coalesce(c.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "urs.nuserrolecontrolcode,u.nuserrolescreencode, urs.nneedesign,"
				+ "s.nsitecontrolcode,urs.nneedrights,c.*,u.nuserrolecode"
				+ " from sitecontrolmaster s,usersrolescreen u,controlmaster c,qualisforms qs,userrolescreencontrol urs"
				+ " where qs.nformcode=s.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
				+ " and s.nformcode=u.nformcode" + " and u.nuserrolescreencode=" + nuserrolescreencode
				+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode and urs.nformcode=u.nformcode and s.ncontrolcode=urs.ncontrolcode"
				+ " and urs.nuserrolecode=u.nuserrolecode and urs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<ControlMaster> controlmaster = (List<ControlMaster>) (jdbcTemplate.query(query, new ControlMaster()));
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	/**
	 * This method is used to add control rights . This method is used to to update
	 * nneedrights 4 is not control rights for specified screen and 3 is
	 * controlrights for specified screen.
	 * 
	 * @param userInfo              object is used for fetched the list of active
	 *                              records based on site.
	 * @param userRoleScreenControl object is used for fetched the list of active
	 *                              records based on nuserrolescreencode.
	 * @param lstusersrolescreen    List is used to take all nuserrolescreencode in
	 *                              list as string.
	 * @param nflag                 [int] is used to refer auto save control rights
	 *                              or manual save .
	 * @param nneedrights           [int] is used passing value.if passing 3 is
	 *                              enable controlrights and 4 is disable
	 *                              controlrights .
	 * @return response entity object holding response status and data of added
	 *         controlrights object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createControlRights(UserInfo userInfo, UserRoleScreenControl userRoleScreenControl,
			List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedrights) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		List<UserRoleScreenControl> lstBeforeSave = new ArrayList<>();
		Map<String, Object> objMap = new HashMap<>();
		List<String> columnids = new ArrayList<>();
		if (nflag == 1) {
			final String querys = " Select * from  userrolescreencontrol  where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nuserrolecontrolcode="
					+ userRoleScreenControl.getNuserrolecontrolcode();
			// status code:200
			UserRoleScreenControl lstBeforeSaveUserRoleScreenControl = (UserRoleScreenControl) jdbcUtilityFunction
					.queryForObject(querys, UserRoleScreenControl.class, jdbcTemplate);
			if (lstBeforeSaveUserRoleScreenControl != null) {
				final String query = "update userrolescreencontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedrights=" + nneedrights + " where "
						+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " nuserrolecontrolcode=" + userRoleScreenControl.getNuserrolecontrolcode();
				jdbcTemplate.execute(query);
				savedControlRightsList.add(userRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLECONTROL");
				} else {
					columnids.add("IDS_DISABLECONTROL");
				}
				auditUtilityFunction.fnInsertAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);
				String suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
						"getNuserrolescreencode");
				objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			int needright = 0;
			String suserrolescreencode = "";
			suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
					"getNuserrolescreencode");
			lstBeforeSave = getControlRightsActiveID(userInfo, suserrolescreencode);

			if (lstBeforeSave.size() > 0) {
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLEALLCONTROL");
					needright = 4;
				} else {
					columnids.add("IDS_DISABLEALLCONTROL");
					needright = 3;
				}

				String query = " update  userrolescreencontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nneedrights=" + nneedrights + " where "
						+ " nuserrolecontrolcode in (select us.nuserrolecontrolcode from usersrolescreen u,sitecontrolmaster s,"
						+ " userrolescreencontrol us where us.nneedrights=" + needright + " and"
						+ " u.nuserrolecode=us.nuserrolecode and u.nuserrolescreencode in (" + suserrolescreencode
						+ ") and " + " us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and "
						+ " us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and  s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(query);

				List<UserRoleScreenControl> lstUserRoleScreenControl = (List<UserRoleScreenControl>) jdbcTemplate.query("select us.* from usersrolescreen u,"
								+ "sitecontrolmaster s,userrolescreencontrol us,controlmaster c where"
								+ " c.ncontrolcode=s.ncontrolcode and c.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolescreencode in ("
								+ suserrolescreencode + ") "
								+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode "
								+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and  " + " s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
								+ " and  " + " s.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " u.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								new UserRoleScreenControl ());
				savedControlRightsList.add(lstUserRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSave);

				auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);
				objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	private ResponseEntity<Object> getControlMaster(UserInfo userInfo, String userRoleScreenCode) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		final String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ " u.nuserrolescreencode,us.nuserrolecontrolcode , "
				+ " us.nneedesign,s.nsitecontrolcode,us.nneedrights,c.*,u.nuserrolecode from "
				+ " sitecontrolmaster s,usersrolescreen u,controlmaster c,qualisforms qs,"
				+ " userrolescreencontrol us where qs.nformcode=s.nformcode and s.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.nformcode=u.nformcode " + " and u.nuserrolescreencode in("
				+ userRoleScreenCode + ") and " + " s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode"
				+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<ControlMaster> controlmaster = jdbcTemplate.query(query, new ControlMaster());
		objMap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active controlrights object based on the
	 * specified suserrolescreencode.
	 * 
	 * @param userInfo            object is used for fetched the list of active
	 *                            records based on site.
	 * @param suserrolescreencode [String] is used to fetch the list of records.
	 * @return response entity object holding response status and data of
	 *         lstSiteControlMaster list
	 * @throws Exception that are thrown from this DAO layer
	 */

	public List<UserRoleScreenControl> getControlRightsActiveID(UserInfo userInfo, String suserrolescreencode)
			throws Exception {
		final String query = "select us.* from usersrolescreen u,sitecontrolmaster s,userrolescreencontrol us,controlmaster c"
				+ " where c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolescreencode in (" + suserrolescreencode + ")"
				+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and" + " us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
				+ "  s.nformcode=u.nformcode and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		// and s.nsitecode=" + userInfo.getNmastersitecode() + "
		List<UserRoleScreenControl> lstUserRoleScreenControl = (List<UserRoleScreenControl>) jdbcTemplate.query
				(query, new UserRoleScreenControl());
		return lstUserRoleScreenControl;
	}

	/**
	 * This method is used to add Esignature rights . This method is used to update
	 * nneedesign in sitecontrolmaster table. 4 is not Esignature rights for
	 * specified screen and 3 is Esignature rights for specified screen.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site.
	 * @param controlMaster      object is used for fetched the list of active
	 *                           records based on ncontrolcode.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nflag              [int] is used to refer auto save control rights or
	 *                           manual save .
	 * @param nneedesign         [int] is used passing value.if passing 3 is enable
	 *                           esign rights and 4 is disable esign rights .
	 * @return response entity object holding response status and data of added
	 *         controlrights object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createEsign(UserInfo userInfo, UserRoleScreenControl userRoleScreenControl,
			List<UsersRoleScreen> lstusersrolescreen, int nflag, int nneedesign) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		List<UserRoleScreenControl> lstBeforeSave = new ArrayList<>();
		Map<String, Object> objMap = new HashMap<>();
		List<String> columnids = new ArrayList<>();
		if (nflag == 1) {
			final String querys = " Select * from  userrolescreencontrol  where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nuserrolecontrolcode="
					+ userRoleScreenControl.getNuserrolecontrolcode();

			// status code:200
			UserRoleScreenControl lstBeforeSaveUserRoleScreenControl = (UserRoleScreenControl) jdbcUtilityFunction
					.queryForObject(querys, UserRoleScreenControl.class, jdbcTemplate);

			// ALPD-5358 - Screen Rights Screen Multi-tab delete issue - added by gowtham on
			// 08-02-2025
			if (lstBeforeSaveUserRoleScreenControl != null) {
				final String query = " update userrolescreencontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedesign=" + nneedesign + " where "
						+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " nuserrolecontrolcode=" + userRoleScreenControl.getNuserrolecontrolcode();
				beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
				jdbcTemplate.execute(query);
				savedControlRightsList.add(userRoleScreenControl);

				if (nneedesign == 3) {
					columnids.add("IDS_ENABLEESIGN");
				} else {
					columnids.add("IDS_DISABLEESIGN");
				}
				auditUtilityFunction.fnInsertAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);
				String suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
						"getNuserrolescreencode");
				objMap.putAll((Map<String, Object>) getEsign(userInfo, suserrolescreencode).getBody());
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				// ALPD-5358 - Screen Rights Screen Multi-tab delete issue - added by gowtham on
				// 08-02-2025
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			int needesign = 0;
			String suserrolescreencode = "";
			suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
					"getNuserrolescreencode");
			lstBeforeSave = getControlRightsActiveID(userInfo, suserrolescreencode);
			if (lstBeforeSave.size() > 0) {
				if (nneedesign == 3) {

					columnids.add("IDS_ENABLEALLESIGN");
					needesign = 4;
				} else {

					columnids.add("IDS_DISABLEALLESIGN");
					needesign = 3;
				}
				String query = "update  userrolescreencontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,nneedesign=" + nneedesign + " where "
						+ " nuserrolecontrolcode in (select us.nuserrolecontrolcode from usersrolescreen u,sitecontrolmaster s,"
						+ " userrolescreencontrol us,controlmaster cm where cm.nisesigncontrol" + " not in ("
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ")"
						+ " and  cm.ncontrolcode=s.ncontrolcode and cm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and us.nneedesign="
						+ needesign + " and u.nuserrolecode=us.nuserrolecode" + " and u.nuserrolescreencode in ("
						+ suserrolescreencode + ") and "
						+ " us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode" + " and us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and  s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				lstBeforeSave = (List<UserRoleScreenControl>) jdbcTemplate.query("select us.* from usersrolescreen u,"
								+ "sitecontrolmaster s,userrolescreencontrol us,controlmaster c where"
								+ " c.nisesigncontrol not in ("
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ ") and c.ncontrolcode=s.ncontrolcode and" + " c.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nuserrolecode=us.nuserrolecode" + " and u.nuserrolescreencode in ("
								+ suserrolescreencode + ") "
								+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode "
								+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and  " + " s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
								+ " and  " + " s.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " u.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " order by us.nformcode,us.ncontrolcode", new UserRoleScreenControl());
				jdbcTemplate.execute(query);

				List<UserRoleScreenControl> lstUserRoleScreenControl = (List<UserRoleScreenControl>) jdbcTemplate.query("select us.* from usersrolescreen u,"
								+ "sitecontrolmaster s,userrolescreencontrol us,controlmaster c where"
								+ " c.nisesigncontrol not in ("
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ ") and c.ncontrolcode=s.ncontrolcode and" + " c.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nuserrolecode=us.nuserrolecode" + " and u.nuserrolescreencode in ("
								+ suserrolescreencode + ") "
								+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode "
								+ " and us.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and  " + " s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
								+ " and  " + " s.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " u.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " order by us.nformcode,us.ncontrolcode",new  UserRoleScreenControl ());
				savedControlRightsList.add(lstUserRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSave);
				auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);
				suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
						"getNuserrolescreencode");
				objMap.putAll((Map<String, Object>) getEsign(userInfo, suserrolescreencode).getBody());
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
		}
	}

	private ResponseEntity<Object> getEsign(UserInfo userInfo, String userRoleScreenCode) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		final String query = "select" + " coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "u.nuserrolescreencode,us.nuserrolecontrolcode , "
				+ " us.nneedesign,s.nsitecontrolcode,us.nneedrights,c.*,u.nuserrolecode from "
				+ " sitecontrolmaster s,usersrolescreen u,controlmaster c,qualisforms qs,"
				+ " userrolescreencontrol us where qs.nformcode=s.nformcode and s.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.nformcode=u.nformcode " + " and u.nuserrolescreencode in("
				+ userRoleScreenCode + ") and " + " s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode"
				+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		objMap.put("ControlRights", jdbcTemplate.query(query, new ControlMaster()));
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active Esignature rights object based on the
	 * specified ncontrolcode.
	 * 
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site.
	 * @param ncontrolcode [int] is used for fetch the record.
	 * @return response entity object holding response status and data of
	 *         lstSiteControlMaster list
	 * @throws Exception that are thrown from this DAO layer
	 */
	public SiteControlMaster getEsignActiveList(UserInfo userInfo, int ncontrolcode) throws Exception {

		SiteControlMaster lstSiteControlMaster = (SiteControlMaster) jdbcUtilityFunction.queryForObject(
				"select s.* from sitecontrolmaster s" + " where s.ncontrolcode=" + ncontrolcode + " and    s.nsitecode="
						+ userInfo.getNmastersitecode() + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				SiteControlMaster.class, jdbcTemplate);
		return lstSiteControlMaster;
	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified nuserrolescreencode.
	 * 
	 * @param objUserInfo        object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nuserrolecode      [int] is used to fetch the list of records based on
	 *                           nuserrolecode.
	 * @return response entity object holding response status and list of all active
	 *         screenrights
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getScreenRightsSelectAll(List<UsersRoleScreen> lstusersrolescreen, int nuserrolecode,
			UserInfo objUserInfo) throws Exception {

		Map<String, Object> objMap = new HashMap<>();
		String sQuery = "";

		String suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
				"getNuserrolescreencode");

		sQuery = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode,"
				+ " coalesce(q.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
				+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " us.nuserrolescreencode from qualisforms q," + "usersrolescreen us,userrole ur,sitequalisforms sf"
				+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode  and"
				+ " us.nformcode=q.nformcode and ur.nuserrolecode=us.nuserrolecode " + "and sf.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and sf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode=" + nuserrolecode
				+ " and  us.nuserrolescreencode in (" + suserrolescreencode + ") order by nuserrolescreencode asc";
		List<UsersRoleScreen> userrolescreenList = (List<UsersRoleScreen>) jdbcTemplate.query(sQuery,
				new UsersRoleScreen() );

		final ObjectMapper mapper = new ObjectMapper();

		final List<UsersRoleScreen> modifiedControlMaster = mapper
				.convertValue(commonFunction.getMultilingualMessageList(userrolescreenList, Arrays.asList("scontrolid"),
						objUserInfo.getSlanguagefilename()), new TypeReference<List<UsersRoleScreen>>() {
						});
		objMap.put("SelectedScreenRights", modifiedControlMaster);

		sQuery = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
				+ " qs.jsondata->'sdisplayname'->>'en-US') as screenname, " + " coalesce(c.jsondata->'scontrolids'->>'"
				+ objUserInfo.getSlanguagetypecode() + "'," + " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "u.nuserrolescreencode, us.nneedesign,"
				+ " s.nsitecontrolcode,us.nuserrolecontrolcode,us.nneedrights,c.*,u.nuserrolecode"
				+ " from sitecontrolmaster s,usersrolescreen u,controlmaster c,userrole r,qualisforms qs,"
				+ " userrolescreencontrol us where qs.nformcode=s.nformcode and s.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode" + " and"
				+ "  u.nuserrolecode=r.nuserrolecode and r.nuserrolecode=" + nuserrolecode + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and  u.nuserrolescreencode in (" + suserrolescreencode + ")"
				+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and u.nuserrolecode=us.nuserrolecode"
				+ " and us.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<ControlMaster> controlmaster = (List<ControlMaster>) (jdbcTemplate.query(sQuery,
				new ControlMaster()));
		objMap.put("ControlRights", controlmaster);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This method is used to delete entry in usersrolescreen table.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen List is used to take all nuserrolescreencode in
	 *                           list as string.
	 * @param nuserrolecode      [int] is used to fetch the list of records based on
	 *                           nuserrolecode.
	 * @return response entity object holding response status and data of deleted
	 *         screenrights object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteScreenRights(List<UsersRoleScreen> lstusersrolescreen, UserInfo userInfo,
			Integer nuserrolecode) throws Exception {

		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();

		String suserrolescreencode = "";
		suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNuserrolescreencode");

		// ALPD-5358 - Screen Rights Screen Multi-tab delete issue - added by gowtham on
		// 08-02-2025
		final String squery = "select * from usersrolescreen where nuserrolescreencode in (" + suserrolescreencode + ")"
				+ "and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UsersRoleScreen> lstuserrolescreen = (List<UsersRoleScreen>) jdbcTemplate.query(squery,
				new UsersRoleScreen());
		if (!lstuserrolescreen.isEmpty()) {
			StringBuffer sB = new StringBuffer();
			sB.append("update userrolescreencontrol set  dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", nneedrights = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", nneedesign ="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nuserrolecontrolcode "
					+ " in (select nuserrolecontrolcode from usersrolescreen u,sitecontrolmaster s,"
					+ " userrolescreencontrol c where s.ncontrolcode=c.ncontrolcode"
					+ "  and u.nuserrolescreencode in (" + suserrolescreencode + ")"
					+ " and u.nformcode=s.nformcode and c.nformcode=s.nformcode and"
					+ " u.nuserrolecode=c.nuserrolecode and u.nuserrolecode=" + nuserrolecode + "" + " and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
					+ userInfo.getNmastersitecode() + ");");

			sB.append("update usersrolescreen set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ " where nuserrolescreencode in (" + suserrolescreencode + ");");
			jdbcTemplate.execute(sB.toString());
			savedControlRightsList.add(lstusersrolescreen);
			auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 1, beforeControlRightsList,
					Arrays.asList("IDS_DELETESCREENRIGHTS"), userInfo);
		} else {
			// ALPD-5358 - Screen Rights Screen Multi-tab delete issue - added by gowtham on
			// 08-02-2025
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getScreenRightsByUserRoleCode(nuserrolecode, userInfo);

	}

	/**
	 * This method is used to retrieve list of all active screenrights for specified
	 * UsersRoleScreen.
	 * 
	 * @param objUserInfo        object is used for fetched the list of active
	 *                           records based on site.
	 * @param lstusersrolescreen this list contain list of UsersRoleScreen.fetech
	 *                           record from lstusersrolescreen.
	 * @return response entity object holding response status and screenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getSingleSelectScreenRights(List<UsersRoleScreen> lstusersrolescreen,
			UserInfo objUserInfo, Integer nuserrolecode) throws Exception {

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String query = "";

		String suserrolescreencode = "";
		suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNuserrolescreencode");

		String strUsersRoleScreen = "select * from usersrolescreen where nuserrolescreencode in (" + suserrolescreencode
				+ ") and nuserrolecode =" + nuserrolecode + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UsersRoleScreen> checkActiveUsersRoleScreen = (List<UsersRoleScreen>) (jdbcTemplate.query
				(strUsersRoleScreen, new UsersRoleScreen()));

		if (checkActiveUsersRoleScreen.size() > 0) {

			query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
					+ " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
					+ " coalesce(c.jsondata->'scontrolids'->>'" + objUserInfo.getSlanguagetypecode() + "',"
					+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
					+ " u.nuserrolescreencode,us.nuserrolecontrolcode , "
					+ " us.nneedesign,s.nsitecontrolcode,us.nneedrights,c.*,u.nuserrolecode from "
					+ " sitecontrolmaster s,usersrolescreen u,controlmaster c,qualisforms qs,"
					+ " userrolescreencontrol us where qs.nformcode=s.nformcode and s.nsitecode="
					+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode "
					+ " and u.nuserrolescreencode in(" + suserrolescreencode + ") and " + " s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
					+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode"
					+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			List<ControlMaster> controlmaster = (List<ControlMaster>) (jdbcTemplate.query(query,
					new ControlMaster()));
			objmap.put("ControlRights", controlmaster);

			// objmap.putAll((Map<String,
			// Object>)getControlRights(objUserInfo,selectedScreenRights.get(selectedScreenRights.size()-1).getNuserrolescreencode()).getBody());
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrive list of all active userrole for specified
	 * site.
	 * 
	 * @param nuserrolecode [int] fetch record from userrole without specified
	 *                      nuserrolecode .
	 * @param objUserInfo   object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and userrole object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getUserRole(Integer nuserrolecode, UserInfo objUserInfo) throws Exception {

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and"
				+ " nuserrolecode!=" + nuserrolecode + " and nsitecode=" + objUserInfo.getNmastersitecode()
				+ "   and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UserRole> lstuserrole = (List<UserRole>) jdbcTemplate.query(query, new UserRole());
		objmap.put("Userrole", lstuserrole);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This method is used to copy screen from specified nuserrolecode. this
	 * userrolecode record deleted in usersrolescreen and userrolescreencontrol then
	 * this nuserrolecode records added in this tables usersrolescreen and
	 * userrolescreencontrol.
	 * 
	 * @param inputMap [Map] map object with "userinfo,userrolecode,nuserrolecode"
	 *                 as key for which the list is to be fetched
	 * @return response entity object holding response status and screenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> CopyScreenRights(Map<String, Object> inputMap) throws Exception {

		final String sQueryLock = " lock  table lockscreenrights " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		String query = "";
		String sformcode = "";
		String suserrolescreencode = "";
		String nformcode = "";
		final List<Object> savedScreenRightsuserroleList = new ArrayList<>();
		final List<Object> savedScreenRightsControl = new ArrayList<>();
		final List<Object> beforeScreenRightsList = new ArrayList<>();

		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		Integer combouserrolecode = null;

		final UserInfo objUserInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		combouserrolecode = (Integer) inputMap.get("userrolecode");
		StringBuffer sB = new StringBuffer();
		query = "select u.nformcode from usersrolescreen u,sitequalisforms s where " + "  nuserrolecode="
				+ combouserrolecode + " and s.nsitecode=" + objUserInfo.getNmastersitecode() + ""
				+ "  and s.nformcode=u.nformcode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<String> lstFormcode = jdbcTemplate.queryForList(query, String.class);
		sformcode = lstFormcode.stream().collect(Collectors.joining(","));

		if (lstFormcode.size() > 0) {
			sB.append("delete from usersrolescreen where nuserrolescreencode "
					+ " in (select u.nuserrolescreencode from usersrolescreen u,sitequalisforms s"
					+ "  where nuserrolecode=" + combouserrolecode + " and s.nsitecode="
					+ objUserInfo.getNmastersitecode() + "" + "  and s.nformcode=u.nformcode and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

			sB.append("delete from userrolescreencontrol where nuserrolecontrolcode in"
					+ " (select u.nuserrolecontrolcode from userrolescreencontrol u,sitequalisforms s,"
					+ " sitecontrolmaster c where c.nformcode=u.nformcode and s.nformcode in (" + sformcode + ") "
					+ " and  s.nformcode=c.nformcode and c.nsitecode=s.nsitecode and nuserrolecode=" + combouserrolecode
					+ " and " + " s.nsitecode=" + objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode"
					+ "  and c.ncontrolcode=u.ncontrolcode and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");

			jdbcTemplate.execute(sB.toString());
		}

		query = "select u.nuserrolescreencode from usersrolescreen u,sitequalisforms s" + "  where nuserrolecode="
				+ nuserrolecode + " and s.nsitecode=" + objUserInfo.getNmastersitecode() + ""
				+ "  and s.nformcode=u.nformcode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<String> lstUserrolescreencode = jdbcTemplate.queryForList(query, String.class);
		suserrolescreencode = lstUserrolescreencode.stream().collect(Collectors.joining(","));

		query = "select u.nstatus," + combouserrolecode
				+ " as nuserrolecode,u.nformcode,nuserrolescreencode from usersrolescreen u "
				+ " ,sitequalisforms s where nuserrolecode=" + nuserrolecode + " and" + "  s.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode" + "  and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<UsersRoleScreen> lstFormcodes = (List<UsersRoleScreen>) jdbcTemplate.query(query,
				new UsersRoleScreen ());

		// lstUserrolescreencode.stream().collect(Collectors.joining(","));
		suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstFormcodes, "getNuserrolescreencode");

		lstFormcodes.stream().forEach(x -> {
			try {
				x.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(objUserInfo));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		if (lstFormcodes.size() > 0) {
			lstFormcodes = (List<UsersRoleScreen>) jdbcUtilityFunction.saveBatchOfObjects(lstFormcodes,
					SeqNoCredentialManagement.class, jdbcTemplate, "nsequenceno");
			savedScreenRightsuserroleList.add(lstFormcodes);
			query = "insert into userrolescreencontrol  "
					+ " (nuserrolecontrolcode,ncontrolcode,nformcode, nuserrolecode,nstatus,dmodifieddate,nneedrights,nneedesign "
					+ " )select " + inputMap.get("nuserrolecontrolcode") + " +RANK() over(order by s.ncontrolcode)"
					+ " nuserrolecontrolcode,  c.ncontrolcode,u.nformcode," + combouserrolecode
					+ " as nuserrolecode,s.nstatus,'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
					+ " c.nneedrights,c.nneedesign from usersrolescreen u,sitecontrolmaster s"
					+ " ,userrolescreencontrol c where c.nformcode=u.nformcode and"
					+ "  c.nuserrolecode=u.nuserrolecode and s.ncontrolcode=c.ncontrolcode" + "  and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ "  and u.nuserrolescreencode in (" + suserrolescreencode + ")"
					+ "  and s.nformcode=u.nformcode and s.nsitecode=" + objUserInfo.getNmastersitecode() + ""
					+ "  and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ "  and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(query);

			query = "select max(nuserrolecontrolcode) from userrolescreencontrol";
			Integer nuserrolecontrolcode = jdbcTemplate.queryForObject(query, Integer.class);

			query = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolecontrolcode) + ""
					+ " where stablename ='userrolescreencontrol'";
			jdbcTemplate.execute(query);
			auditUtilityFunction.fnInsertListAuditAction(savedScreenRightsuserroleList, 1, beforeScreenRightsList,
					Arrays.asList("IDS_COPYSCREENRIGHTS"), objUserInfo);

		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active screenrights for the
	 * specified nuserrolecode.
	 * 
	 * @param nuserrolecode [int] fetch record based on nuserrolecode .
	 * @param userInfo      object is used for fetched the list of active records
	 *                      based on site.
	 * @return response entity object holding response status and copyscreenrights
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getCopyUserRoleCode(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode"
				+ " as nuserrole ," + " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " us.nuserrolescreencode from qualisforms q," + " usersrolescreen us,userrole ur,sitequalisforms sf"
				+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode and ur.nuserrolecode=us.nuserrolecode "
				+ " and sf.nsitecode=" + userInfo.getNmastersitecode() + " and sf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode=" + nuserrolecode
				+ "  order by nuserrolescreencode asc;";

		List<UsersRoleScreen> userrolescreenList = (List<UsersRoleScreen>) jdbcTemplate.query(query,
				new UsersRoleScreen ());

		outputMap.put("copyScreenRights", userrolescreenList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * @param objmap [Map] map object added into nuserrolecontrolcode seqno.
	 * @return response entity object holding response status and
	 *         nuserrolecontrolcode
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public Map<String, Object> getControlRightsSeqno(Map<String, Object> objmap) throws Exception {
		Map<String, Object> mapSeq = new HashMap<String, Object>();

		String sQuery = " LOCK TABLE lockscreenrights";
		jdbcTemplate.execute(sQuery);

		String StrQuery = "select nsequenceno from seqnocredentialmanagement where stablename = 'userrolescreencontrol'; ";
		int nuserrolecontrolcode = jdbcTemplate.queryForObject(StrQuery, Integer.class);
		String strUpdate = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolecontrolcode) + ""
				+ " where stablename ='userrolescreencontrol';";
		jdbcTemplate.execute(strUpdate);
		mapSeq.put("nuserrolecontrolcode", (nuserrolecontrolcode));
		mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return mapSeq;
	}

	@Override
	public ResponseEntity<Object> getSearchScreenRights(String nuserrolescreencode, UserInfo objUserInfo)
			throws Exception {

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'"
				+ objUserInfo.getSlanguagetypecode() + "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + objUserInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "u.nuserrolescreencode,us.nuserrolecontrolcode , "
				+ " us.nneedesign,s.nsitecontrolcode,us.nneedrights,c.*,u.nuserrolecode from "
				+ " sitecontrolmaster s,usersrolescreen u,controlmaster c,qualisforms qs,"
				+ " userrolescreencontrol us where qs.nformcode=s.nformcode and s.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode " + " and u.nuserrolescreencode in("
				+ nuserrolescreencode + ") and " + " s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode"
				+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		List<ControlMaster> controlmaster = (List<ControlMaster>) (jdbcTemplate.query(query,
				new ControlMaster ()));
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}
}
