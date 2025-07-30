package com.agaramtech.qualis.credential.service.usersrolescreenhide;

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

import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UserRoleScreenControlHide;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.credential.model.UsersRoleScreenHide;
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

@AllArgsConstructor
@Repository
public class UsersRoleScreenHideDAOImpl implements UsersRoleScreenHideDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(UsersRoleScreenHideDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	public ResponseEntity<Object> getUserScreenhide(final Integer nuserrolescreencode, final UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		UserRole selectedUserRole = null;
		Integer nuserrolecode = null;
		List<Users> lstUsers1 = null;
		List<Users> lstUsers = null;
		if (nuserrolescreencode == null) {
			String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nsitecode="
					+ userInfo.getNmastersitecode() + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
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

					String comboget = "select u.nusercode,(u.sfirstname||' '||u.slastname || ' (' ||u.sloginid || ' )') as susername "
							+ " from users u,userrole ur,usermultirole umr,userssite usite"
							+ " where umr.nusersitecode=usite.nusersitecode"
							+ " and umr.nuserrolecode=ur.nuserrolecode and umr.nuserrolecode=ur.nuserrolecode and u.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and umr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and usite.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and usite.nusercode=u.nusercode" + " and ur.nuserrolecode=" + nuserrolecode
							+ " group by u.nusercode";
					lstUsers1 = (List<Users>) jdbcTemplate.query(comboget, new Users());
					if (!lstUsers1.isEmpty()) {
						objmap.putAll((Map<String, Object>) getUserScreenhideControlRights(userInfo, nuserrolecode,
								lstUsers1.get(lstUsers1.size() - 1).getNusercode()));
					}
				}
				// For binding onchange record initially
				if (!lstUsers1.isEmpty()) {
					String comboget = " select u.nusercode,(u.sfirstname||' '||u.slastname || ' (' ||u.sloginid || ' )') as susername "
							+ " from users u,userrole ur,usermultirole umr,userssite usite"
							+ " where umr.nusersitecode=usite.nusersitecode"
							+ " and umr.nuserrolecode=ur.nuserrolecode and u.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and umr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and usite.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and usite.nusercode=u.nusercode" + " and ur.nuserrolecode=" + nuserrolecode
							+ " group by u.nusercode";
					lstUsers = (List<Users>) jdbcTemplate.query(comboget, new Users());
					objmap.put("UsersMain", lstUsers);

					/// Main Get
					final String query1 = " select uh.nusersrolehidescreencode,q.nformcode"
							+ " ,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
							+ "',q.jsondata->'sdisplayname'->>'en-US')as sdisplayname,uh.nuserrolecode,uh.nusercode from  "
							+ " qualisforms q" + " ,usersrolescreenhide uh,userrole ur,users u" + " where"
							+ " q.nformcode=uh.nformcode " + " and u.nusercode =uh.nusercode"
							+ " and ur.nuserrolecode=uh.nuserrolecode" + " and ur.nuserrolecode=" + nuserrolecode
							+ " and u.nusercode= " + lstUsers.get(lstUsers.size() - 1).getNusercode()
							+ " and u.nstatus=1 order by nusersrolehidescreencode desc ";

					List<UsersRoleScreenHide> userQualisForms = (List<UsersRoleScreenHide>) jdbcTemplate.query(query1,
							new UsersRoleScreenHide());
					objmap.put("ScreenRights", userQualisForms);
					objmap.put("SelectedUserName", lstUsers.get(lstUsers.size() - 1));
					int userrolecode = (int) lstuserrole.get(lstuserrole.size() - 1).getNuserrolecode();
					int usercodde = lstUsers.get(lstUsers.size() - 1).getNusercode();
					objmap.putAll((Map<String, Object>) getuserscreenhideInserted(userrolecode, usercodde, userInfo)
							.getBody());
				}
				objmap.put("SelectedUserRole", lstuserrole.get(lstuserrole.size() - 1));
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}
		} else {
			final String query = "select * from usersrolescreen where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolescreencode="
					+ nuserrolescreencode;
			List<UsersRoleScreen> selectedScreenRights = (List<UsersRoleScreen>) jdbcTemplate.query(query,
					new UsersRoleScreen());
			if (!selectedScreenRights.isEmpty()) {
				final String sQuery = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode,coalesece(q.jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',q.jsondata->'sdisplayname'->>'en-US')as sdisplayname,us.nuserrolescreencode from qualisforms q,"
						+ "usersrolescreen us,userrole ur,sitequalisforms sf"
						+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode  and us.nformcode=q.nformcode and ur.nuserrolecode=us.nuserrolecode "
						+ "and sf.nsitecode=" + userInfo.getNmastersitecode() + " and sf.nstatus="
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
				objmap.putAll((Map<String, Object>) getUserScreenhideControlRights(userInfo,
						selectedScreenRights.get(selectedScreenRights.size() - 1).getNuserrolescreencode(), null));
			}
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getUserScreenhidecomboo(Integer nuserrolecode, final UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		UserRole selectedUserRole = null;
		String comboget = "select u.nusercode,(u.sfirstname||' '||u.slastname || ' (' ||u.sloginid || ' )') as susername "
				+ " from users u,userrole ur,usermultirole umr,userssite usite"
				+ " where umr.nusersitecode=usite.nusersitecode" + " and umr.nuserrolecode=ur.nuserrolecode"
				+ " and usite.nusercode=u.nusercode" + " and ur.nuserrolecode=" + nuserrolecode + " and ur.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " umr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "usite.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by u.nusercode";
		List<Users> lstUsers = (List<Users>) jdbcTemplate.query(comboget, new Users());
		if (lstUsers.isEmpty()) {
			objmap.put("UsersMain", lstUsers);
		}
		if (!lstUsers.isEmpty()) {
			objmap.put("UsersMain", lstUsers);

			// Start

			String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nsitecode="
					+ objUserInfo.getNmastersitecode() + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<UserRole> lstuserrole = (List<UserRole>) jdbcTemplate.query(query, new UserRole());
			selectedUserRole = ((UserRole) lstuserrole.get(lstuserrole.size() - 1));
			nuserrolecode = selectedUserRole.getNuserrolecode();

//			final String query1 = " select uh.nusersrolehidescreencode,q.nformcode"
//					+ " ,coalesce(q.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
//					+ "',q.jsondata->'sdisplayname'->>'en-US')as sdisplayname,uh.nuserrolecode,uh.nusercode from  "
//					+ " qualisforms q" + " ,usersrolescreenhide uh,userrole ur,users u" + " where"
//					+ " q.nformcode=uh.nformcode " + " and u.nusercode =uh.nusercode"
//					+ " and ur.nuserrolecode=uh.nuserrolecode" + " and ur.nuserrolecode=" + nuserrolecode
//					+ " and u.nusercode= " + lstUsers.get(lstUsers.size() - 1).getNusercode()
//					+ " and u.nstatus=1 order by nusersrolehidescreencode desc ";
//
//			List<UsersRoleScreenHide> userQualisForms = (List<UsersRoleScreenHide>) jdbcTemplate.query(query1,
//					new UsersRoleScreenHide());
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public Map<String, Object> getUserScreenhideControlRights(final UserInfo userInfo, final Integer nuserrolescreencode,
			final Integer nusercode) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		String query = "select cm.*,coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qs.jsondata->'sdisplayname'->>'en-US') as screenname,coalesce(cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,uhs.nneedesign,us.needrights,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent "
				+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs,userrolescreencontrol usc  "
				+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in ("
				+ nuserrolescreencode + "))" + " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
				+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
				+ " and usc.ncontrolcode=cm.ncontrolcode"
				+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
				+ nusercode + ")";
		List<ControlMaster> controlmaster = jdbcTemplate.query(query, new ControlMaster());
		objmap.put("ControlRights", controlmaster);
		return objmap;

	}

	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> getAvailableUserScreenhide(final Integer nusercode, final Integer nuserrolecode,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		if (nuserrolecode != null) {
			final String query = " select q.nformcode ," + nuserrolecode
					+ " as nuserrolecode,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "', q.jsondata->'sdisplayname'->>'en-US')  as label," + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "', q.jsondata->'sdisplayname'->>'en-US') as value,-1 as nsitecode,q.nstatus,coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "', q.jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
					+ nusercode + " as nusercode " + " from" + " qualisforms q,usersrolescreen ur" + " where"
					+ " q.nstatus=1 and" + " ur.nformcode=q.nformcode and ur.nuserrolecode=" + nuserrolecode
					+ " and q.nformcode not in (select nformcode From usersrolescreenhide where nusercode=" + nusercode
					+ " and nuserrolecode=" + nuserrolecode + " ) ";
			return new ResponseEntity<>(jdbcTemplate.query(query, new UsersRoleScreenHide()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTUSERROLE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public Map<String, Object> getUserScreenhideSeqno(final Map<String, Object> objmap) throws Exception {
		Map<String, Object> mapSeq = new HashMap<String, Object>();
		String StrQuery = "select nsequenceno from seqnocredentialmanagement where stablename = 'usersrolescreenhide' ";
		int nusersrolehidescreencode = (int) jdbcUtilityFunction.queryForObject(StrQuery, Integer.class, jdbcTemplate);

		String strUpdate = " update seqnocredentialmanagement set nsequenceno= " + (nusersrolehidescreencode)
				+ " where stablename ='usersrolescreenhide'";
		jdbcTemplate.execute(strUpdate);

		// For Control Code
		String StrQuery1 = "select nsequenceno from seqnocredentialmanagement where stablename = 'userrolescreencontrolhide' ";
		int nuserrolehidecontrolecode = (int) jdbcUtilityFunction.queryForObject(StrQuery1, Integer.class,
				jdbcTemplate);

		String strUpdate1 = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolehidecontrolecode)
				+ " where stablename ='userrolescreencontrolhide'";
		jdbcTemplate.execute(strUpdate1);
		mapSeq.put("nusersrolehidescreencode", (nusersrolehidescreencode));
		mapSeq.put("nuserrolehidecontrolecode", (nuserrolehidecontrolecode));
		mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return mapSeq;
	}

	@Override
	public ResponseEntity<Object> createUserScreenhide(final Map<String, Object> inputMap) throws Exception {
		if (!(inputMap.containsKey("nusercode"))) {
			String userrolescreencode = "";
			Integer nusersrolehidescreencode = 0;
			final List<Object> savedScreenRightsuserroleList = new ArrayList<>();
			final List<Object> beforeScreenRightsList = new ArrayList<>();
			final ObjectMapper objmapper = new ObjectMapper();

			Integer nuserrolecode = null;
			Integer nusercode = null;
			List<UsersRoleScreenHide> lstacAvailableScreenArrayCollection = (List<UsersRoleScreenHide>) objmapper
					.convertValue(inputMap.get("userscreenhide"), new TypeReference<List<UsersRoleScreenHide>>() {
					});
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
			nusercode = (Integer) inputMap.get("usercode");

			userrolescreencode = stringUtilityFunction.fnDynamicListToString(lstacAvailableScreenArrayCollection,
					"getNformcode");
			final String query = "select * from usersrolescreenhide where nformcode in (" + userrolescreencode + ")"
					+ " and nuserrolecode=" + nuserrolecode + " and nusercode=" + nusercode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<UsersRoleScreen> lstUser = (List<UsersRoleScreen>) jdbcTemplate.query(query, new UsersRoleScreen());
			if (lstUser.size() == 0) {
				if (lstacAvailableScreenArrayCollection.size() > 0) {

					String querys = "insert into usersrolescreenhide (nusersrolehidescreencode,nformcode,nusercode,nuserrolecode,nstatus,needrights,nsitecode,dmodifieddate) "
							+ " select  " + inputMap.get("nusersrolehidescreencode")
							+ " +RANK() over(order by nformcode) as nusersrolehidescreencode, nformcode as nformcode "
							+ " ," + inputMap.get("usercode") + " as nusercode, " + inputMap.get("nuserrolecode")
							+ " as nuserrolecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " as nstatus,4 as needrights ," + userInfo.getNmastersitecode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
							+ " from qualisforms where nformcode in (" + userrolescreencode + ") and nstatus=1 ;";
					querys += "insert into userrolescreencontrolhide (nuserrolehidecontrolecode,ncontrolcode,nusercode,nformcode,nuserrolecode,nstatus,needrights,nneedesign,nsitecode,dmodifieddate)  "
							+ " select " + inputMap.get("nuserrolehidecontrolecode")
							+ "+RANK() over(order by cm.ncontrolcode) as nuserrolehidecontrolecode"
							+ " ,cm.ncontrolcode," + inputMap.get("usercode") + " as nusercode,cm.nformcode,"
							+ inputMap.get("nuserrolecode") + ""
							+ " as nuserrolecode,1 as nstatus,4 as needrights,4 as nneedesign ,"
							+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "' from controlmaster cm, sitecontrolmaster scm where cm.nformcode in ( select  qf.nformcode as nformcode "
							+ " from qualisforms qf where qf.nformcode in (" + userrolescreencode + ") and qf.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")  and cm.ncontrolcode=scm.ncontrolcode and cm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and scm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(querys);

					querys = "select max(nusersrolehidescreencode) from usersrolescreenhide";
					nusersrolehidescreencode = (Integer) jdbcUtilityFunction.queryForObject(querys, Integer.class,
							jdbcTemplate);

					querys = " update seqnocredentialmanagement set nsequenceno= " + (nusersrolehidescreencode)
							+ " where stablename ='usersrolescreenhide'";
					jdbcTemplate.execute(querys);

					querys = "select max(nuserrolehidecontrolecode) from userrolescreencontrolhide";
					int nuserrolehidecontrolecode = 0;
					if (jdbcUtilityFunction.queryForObject(querys, Integer.class, jdbcTemplate) != null) {
						nuserrolehidecontrolecode = (int) jdbcUtilityFunction.queryForObject(querys, Integer.class,
								jdbcTemplate);

						querys = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolehidecontrolecode)
								+ " where stablename ='userrolescreencontrolhide'";
						jdbcTemplate.execute(querys);
					}
					int usersrolescreenhide = (int) inputMap.get("nusersrolehidescreencode");
					for (int i = 0; i < lstacAvailableScreenArrayCollection.size(); i++) {
						if (lstacAvailableScreenArrayCollection.size() > 0) {
							usersrolescreenhide = usersrolescreenhide + 1;
							lstacAvailableScreenArrayCollection.get(i).setNusersrolehidescreencode(usersrolescreenhide);
						}
					}

					savedScreenRightsuserroleList.add(lstacAvailableScreenArrayCollection);
					auditUtilityFunction.fnInsertListAuditAction(savedScreenRightsuserroleList, 1,
							beforeScreenRightsList, Arrays.asList("IDS_ADDUSERROLESCREENHIDE"), userInfo);
				}
			}
			return getuserscreenhideInserted(nuserrolecode, nusercode, userInfo);
		} else {
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			return getuserscreenhideInserted((int) inputMap.get("nuserrolecode"), (int) inputMap.get("nusercode"),
					userInfo);
		}

	}

	/// For Getting Inserted Records
	public ResponseEntity<Object> getuserscreenhideInserted(final Integer nuserrolecode, final Integer nusercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select uh.nusersrolehidescreencode,q.nformcode,ur.suserrolename,uh.nusercode,uh.nuserrolecode"
				+ " ,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',q.jsondata->'sdisplayname'->>'en-US') as sdisplayname,uh.needrights from  " + " qualisforms q"
				+ " ,usersrolescreenhide uh,userrole ur,users u" + " where" + " q.nformcode=uh.nformcode "
				+ " and u.nusercode =uh.nusercode" + " and ur.nuserrolecode=uh.nuserrolecode" + " and ur.nuserrolecode="
				+ nuserrolecode + " and u.nusercode= " + nusercode
				+ " and u.nstatus=1 order by nusersrolehidescreencode asc ";

		List<UsersRoleScreenHide> userQualisForms = (List<UsersRoleScreenHide>) jdbcTemplate.query(query,
				new UsersRoleScreenHide());

		String querys = "select * from userrole where nuserrolecode=" + nuserrolecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UserRole> userroleList = (List<UserRole>) jdbcTemplate.query(querys, new UserRole());

		// String querys2 = " select sfirstname ||' '|| slastname susername,* from users
		// where nusercode=" + nusercode + " and nstatus=" +
		// Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		String querys2 = "select u.nusercode,(u.sfirstname||' '||u.slastname || ' (' ||u.sloginid || ' )') as susername "
				+ " from users u,userrole ur,usermultirole umr,userssite usite"
				+ " where umr.nusersitecode=usite.nusersitecode" + " and umr.nuserrolecode=ur.nuserrolecode"
				+ "  and  u.nusercode=  " + nusercode + " and usite.nusercode=u.nusercode" + " and ur.nuserrolecode="
				+ nuserrolecode + " and ur.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		List<Users> userList = (List<Users>) jdbcTemplate.query(querys2, new Users());

		outputMap.put("ScreenRights", userQualisForms);
		outputMap.put("SelectedUserRole", userroleList.get(0));
		// outputMap.put("SelectedUserName", userList.get(0));
		if (userList.size() > 0) {
			outputMap.put("SelectedUserName", userList.get(0));
		} else {
			outputMap.put("SelectedUserName", userList);
		}
		if (userQualisForms.size() > 0) {
			final String Query = "select uh.nusersrolehidescreencode,q.nformcode"
					+ ",coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',q.jsondata->'sdisplayname'->>'en-US') as sdisplayname,uh.needrights,uh.nusercode,uh.nuserrolecode"
					+ " from " + " qualisforms q" + " ,usersrolescreenhide uh" + " where" + " q.nformcode=uh.nformcode"
					+ " and q.nformcode=" + userQualisForms.get(userQualisForms.size() - 1).getNformcode();

			List<UsersRoleScreenHide> selectScreenRights = (List<UsersRoleScreenHide>) jdbcTemplate.query(Query,
					new UsersRoleScreenHide());
			outputMap.put("SelectedScreenRights", selectScreenRights);

			String query2 = "select cm.*,coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',qs.jsondata->'sdisplayname'->>'en-US')as screenname,coalesce(cm.jsondata->'scontrolids'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,us.needrights,uhs.nneedesign,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent "
					+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs,userrolescreencontrol usc  "
					+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in ("
					+ userQualisForms.get(userQualisForms.size() - 1).getNformcode() + "))"
					+ " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
					+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
					+ " and usc.ncontrolcode=cm.ncontrolcode"
					+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
					+ nusercode + ")";
			List<ControlMaster> controlmaster = jdbcTemplate.query(query2, new ControlMaster());
			outputMap.put("ControlRights", controlmaster);

			/// For Multi Session Esign Bug
			String query3 = "select * from userrolescreencontrol where nformcode in ("
					+ userQualisForms.get(userQualisForms.size() - 1).getNformcode() + ")  and nuserrolecode="
					+ nuserrolecode;
			List<UserRoleScreenControl> controlmasterparent = (List<UserRoleScreenControl>) jdbcTemplate.query(query3,
					new UserRoleScreenControl());
			outputMap.put("ControlRightsParent", controlmasterparent);
		} else {
			outputMap.put("SelectedScreenRights", userQualisForms);
			outputMap.put("ControlRights", userQualisForms);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> deleteUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen, final UserInfo userInfo,
			final Integer nuserrolecode, final Integer nusercode) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		String snhidescreencode = "";
		snhidescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
				"getNusersrolehidescreencode");
		StringBuffer sB = new StringBuffer();

		List<Integer> lstcount = new ArrayList<Integer>();
		lstcount = jdbcTemplate.queryForList(
				"select nusersrolehidescreencode from usersrolescreenhide where nusersrolehidescreencode in ("
						+ snhidescreencode + ")",
				Integer.class);
		if (lstcount.size() > 0) {
			sB.append(" delete from userrolescreencontrolhide where nformcode in ("
					+ " select nformcode from usersrolescreenhide where nusersrolehidescreencode in ("
					+ snhidescreencode + "));");
			sB.append(" delete from usersrolescreenhide where nusersrolehidescreencode in (" + snhidescreencode + ");");

			jdbcTemplate.execute(sB.toString());

			savedControlRightsList.add(lstusersrolescreen);
			auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 1, beforeControlRightsList,
					Arrays.asList("IDS_DELETEUSERROLESCREENHIDE"), userInfo);
			return getuserscreenhideInserted(nuserrolecode, nusercode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getSingleSelectUserScreenhide(final List<UsersRoleScreenHide> lstusersrolescreen,
			final UserInfo objUserInfo,final  String suserrolescreencode,final  Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String query = "";
		String Nformcode = "";
		Nformcode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNformcode");
		int nusercode = (int) inputMap.get("SelectedUserName");

		query = "select cm.*,coalesce(qs.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
				+ "',qs.jsondata->'sdisplayname'->>'en-US') as screenname," + "coalesce(cm.jsondata->'scontrolids'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,us.needrights,uhs.nneedesign,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent "
				+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs,userrolescreencontrol usc  "
				+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in (" + Nformcode
				+ "))" + " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
				+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
				+ " and usc.ncontrolcode=cm.ncontrolcode"
				+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
				+ nusercode + ")";

		List<ControlMaster> controlmaster = jdbcTemplate.query(query, new ControlMaster());
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateScreenHideControlRights(final UserInfo userInfo,
			final UserRoleScreenControlHide userRoleScreenControl, final List<UsersRoleScreenHide> lstusersrolescreen, final int nflag,
			final Integer nneedrights, final Map<String, Object> inputMap) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		List<UserRoleScreenControlHide> lstBeforeSave = new ArrayList<>();
		Map<String, Object> objMap = new HashMap<>();
		List<String> columnids = new ArrayList<>();
		String userrloecode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNuserrolecode");
		String usercode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNusercode");
		if (inputMap.get("nneedesign") == null) {
			if (nflag == 1) {
				final String querys = "select * from  userrolescreencontrolhide  where  nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nuserrolecode="
						+ userRoleScreenControl.getNuserrolecode() + " and nusercode="
						+ userRoleScreenControl.getNusercode() + " and nformcode="
						+ userRoleScreenControl.getNformcode() + " and ncontrolcode="
						+ userRoleScreenControl.getNcontrolcode() + " order by nuserrolehidecontrolecode";
				UserRoleScreenControlHide lstBeforeSaveUserRoleScreenControl = (UserRoleScreenControlHide) jdbcUtilityFunction
						.queryForObject(querys, UserRoleScreenControlHide.class, jdbcTemplate);

				final String query = "update userrolescreencontrolhide set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', needrights=" + nneedrights
						+ " where  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  nuserrolecode=" + userRoleScreenControl.getNuserrolecode() + " and nusercode="
						+ userRoleScreenControl.getNusercode() + " and nformcode="
						+ userRoleScreenControl.getNformcode() + " and ncontrolcode="
						+ userRoleScreenControl.getNcontrolcode() + ";";
				jdbcTemplate.execute(query);
				if (userRoleScreenControl.getNneedesign() == 0) {
					userRoleScreenControl.setNneedesign(lstBeforeSaveUserRoleScreenControl.getNneedesign());
				}
				savedControlRightsList.add(userRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLECONTROL");
				} else {
					columnids.add("IDS_DISABLECONTROL");
				}
				auditUtilityFunction.fnInsertAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);

			} else {
				int needright = 0;
				String suserrolescreencode = "";
				suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNformcode");
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLEALLCONTROL");
					needright = 4;
				} else {
					columnids.add("IDS_DISABLEALLCONTROL");
					needright = 3;
				}
				lstBeforeSave = (List<UserRoleScreenControlHide>) jdbcTemplate
						.query("select * from " + " userrolescreencontrolhide where nformcode in "
								+ " (select nformcode from qualisforms where " + " nformcode in (" + suserrolescreencode
								+ ")) order by nuserrolehidecontrolecode", new UserRoleScreenControlHide());
				String query = "";
				if (nneedrights == 3) {
					query = "update " + " userrolescreencontrolhide set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', needrights= " + nneedrights
							+ " where " + " nformcode in (select nformcode from qualisforms where nformcode in ("
							+ suserrolescreencode + ") )   and needrights<>3 and nuserrolecode in (" + userrloecode
							+ ")";
				} else if (nneedrights == 4) {
					query = "update " + " userrolescreencontrolhide set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', needrights= " + nneedrights
							+ " where " + " nformcode in (select nformcode from qualisforms where nformcode in ("
							+ suserrolescreencode + ") )   and needrights<>4 and nuserrolecode in (" + userrloecode
							+ ")";
				}
				jdbcTemplate.execute(query);
				List<UserRoleScreenControlHide> lstUserRoleScreenControl = (List<UserRoleScreenControlHide>) jdbcTemplate
						.query("select * from " + " userrolescreencontrolhide where nformcode in "
								+ " (select nformcode from qualisforms where " + " nformcode in (" + suserrolescreencode
								+ ")) order by nuserrolehidecontrolecode", new UserRoleScreenControlHide());
				savedControlRightsList.add(lstUserRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSave);
				auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList,
						columnids, userInfo);
			}
		} else {
			if (nflag == 1) {
				final String querys = "select * from  userrolescreencontrolhide  where  nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nuserrolecode="
						+ userRoleScreenControl.getNuserrolecode() + " and nusercode="
						+ userRoleScreenControl.getNusercode() + " and nformcode="
						+ userRoleScreenControl.getNformcode() + " and ncontrolcode="
						+ userRoleScreenControl.getNcontrolcode() + " order by nuserrolehidecontrolecode";

				UserRoleScreenControlHide lstBeforeSaveUserRoleScreenControl = (UserRoleScreenControlHide) jdbcTemplate
						.queryForObject(querys, new UserRoleScreenControlHide());

				final String query = "update userrolescreencontrolhide set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedesign=" + nneedrights
						+ " where  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  nuserrolecode=" + userRoleScreenControl.getNuserrolecode() + " and nusercode="
						+ userRoleScreenControl.getNusercode() + " and nformcode="
						+ userRoleScreenControl.getNformcode() + " and ncontrolcode="
						+ userRoleScreenControl.getNcontrolcode() + ";";

				jdbcTemplate.execute(query);
				if (userRoleScreenControl.getNeedrights() == 0) {
					userRoleScreenControl.setNeedrights(lstBeforeSaveUserRoleScreenControl.getNeedrights());
				}
				savedControlRightsList.add(userRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLEESIGN");
				} else {
					columnids.add("IDS_DISABLEESIGN");
				}
				auditUtilityFunction.fnInsertAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);

			} else {
				int needright = 0;
				String suserrolescreencode = "";
				suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNformcode");
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLEALLESIGN");
					needright = 4;
				} else {
					columnids.add("IDS_DISABLEALLESIGN");
					needright = 3;
				}
				lstBeforeSave = (List<UserRoleScreenControlHide>) jdbcTemplate
						.query("select * from " + " userrolescreencontrolhide where nformcode in "
								+ " (select nformcode from qualisforms where " + " nformcode in (" + suserrolescreencode
								+ ")) order by nuserrolehidecontrolecode", new UserRoleScreenControlHide());
				String query = "";
				if (nneedrights == 3) {
					query = "update userrolescreencontrolhide usr set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedesign= " + nneedrights
							+ " from  userrolescreencontrol usc" + " where"
							+ " usr.nformcode in (select nformcode from qualisforms where nformcode in ("
							+ suserrolescreencode + ") ) " + " and usr.nuserrolecode in (" + userrloecode
							+ ") and usr.ncontrolcode=usc.ncontrolcode" + " and usr.nuserrolecode=usc.nuserrolecode"
							+ " and usr.nformcode=usc.nformcode" + " and usc.nneedesign=" + nneedrights;
				} else if (nneedrights == 4) {
					query = "update " + " userrolescreencontrolhide set dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedesign= " + nneedrights
							+ " where " + " nformcode in (select nformcode from qualisforms where nformcode in ("
							+ suserrolescreencode + ") ) and nneedesign<>4 and nuserrolecode in (" + userrloecode + ")";

				}
				jdbcTemplate.execute(query);
				List<UserRoleScreenControlHide> lstUserRoleScreenControl = (List<UserRoleScreenControlHide>) jdbcTemplate
						.query("select * from   userrolescreencontrolhide where nformcode in "
								+ " (select nformcode from qualisforms where " + " nformcode in (" + suserrolescreencode
								+ ")) order by nuserrolehidecontrolecode", new UserRoleScreenControlHide());
				savedControlRightsList.add(lstUserRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSave);
				auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList,
						columnids, userInfo);
			}
		}
		String suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNformcode");
		String query = "select cm.*,coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qs.jsondata->'sdisplayname'->>'en-US') as screenname," + "coalesce(cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,us.needrights,uhs.nneedesign,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent  "
				+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs ,userrolescreencontrol usc  "
				+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in ("
				+ suserrolescreencode + "))" + " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
				+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
				+ " and usc.ncontrolcode=cm.ncontrolcode"
				+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
				+ usercode + ")";
		List<ControlMaster> controlmaster = (List<ControlMaster>) jdbcTemplate.query(query, new ControlMaster());
		objMap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateListControlRights(final int nneedrights,final  int nusersrolehidescreencode,
			final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();
		int nuserrolecode = (int) inputMap.get("nuserrolecode");
		int nusercode = (int) inputMap.get("nusercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		List<UsersRoleScreenHide> lstusersrolescreen = (List<UsersRoleScreenHide>) objmapper
				.convertValue(inputMap.get("SelectedScreenRights"), new TypeReference<List<UsersRoleScreenHide>>() {
				});
		String updstr = "update usersrolescreenhide set dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', needrights=" + nneedrights
				+ " where nusersrolehidescreencode=" + nusersrolehidescreencode;
		jdbcTemplate.execute(updstr);

		final String query = " select uh.nusersrolehidescreencode,q.nformcode,ur.suserrolename"
				+ " ,coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',q.jsondata->'sdisplayname'->>'en-US') as sdisplayname,uh.needrights,uh.nusercode,uh.nuserrolecode from  "
				+ " qualisforms q" + " ,usersrolescreenhide uh,userrole ur,users u" + " where"
				+ " q.nformcode=uh.nformcode " + " and u.nusercode =uh.nusercode"
				+ " and ur.nuserrolecode=uh.nuserrolecode" + " and ur.nuserrolecode=" + nuserrolecode
				+ " and u.nusercode= " + nusercode + " and u.nstatus=1 order by nusersrolehidescreencode asc ";
		List<UsersRoleScreenHide> userQualisForms = (List<UsersRoleScreenHide>) jdbcTemplate.query(query,
				new UsersRoleScreenHide());
		objMap.put("ScreenRights", userQualisForms);
		String suserrolescreencode = "";
		suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNformcode");
		final String Query = " select uh.nusersrolehidescreencode,q.nformcode"
				+ ",coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',q.jsondata->'sdisplayname'->>'en-US')as sdisplayname,uh.needrights,uh.nusercode,uh.nuserrolecode "
				+ " from " + " qualisforms q" + " ,usersrolescreenhide uh" + " where" + " q.nformcode=uh.nformcode"
				+ " and q.nformcode in (" + suserrolescreencode + ")";

		List<UsersRoleScreenHide> selectScreenRights = (List<UsersRoleScreenHide>) jdbcTemplate.query(Query,
				new UsersRoleScreenHide());
		objMap.put("SelectedScreenRights", selectScreenRights);
		String query1 = "select cm.*," + "coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qs.jsondata->'sdisplayname'->>'en-US') as screenname," + "coalesce(cm.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,"
				+ "us.needrights,uhs.nneedesign,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent "
				+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs,userrolescreencontrol usc  "
				+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in ("
				+ suserrolescreencode + "))" + " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
				+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
				+ " and usc.ncontrolcode=cm.ncontrolcode"
				+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
				+ nusercode + ")";
		List<ControlMaster> controlmaster = (List<ControlMaster>) (List<ControlMaster>) jdbcTemplate.query(query1,
				new ControlMaster());
		// fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList,
		// columnids, userInfo);
		objMap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSearchScreenHide(final String nuserrolescreencode,final  UserInfo objUserInfo,
			final Integer nusercode) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String query = "select cm.*,coalesce(qs.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
				+ "',qs.jsondata->'sdisplayname'->>'en-US') as screenname," + "coalesce(cm.jsondata->'scontrolids'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')as scontrolids,us.needrights,uhs.nneedesign,uhs.needrights as nneedrights,uhs.nuserrolehidecontrolecode, usc.nneedesign as needesignsparent "
				+ " from qualisforms qs,controlmaster cm,usersrolescreenhide us,userrolescreencontrolhide uhs,userrolescreencontrol usc  "
				+ " where cm.ncontrolcode in (select ncontrolcode from controlmaster where nformcode in ("
				+ nuserrolescreencode + "))" + " and qs.nformcode=cm.nformcode" + " and us.nformcode=qs.nformcode"
				+ " and uhs.ncontrolcode=cm.ncontrolcode" + " and usc.nneedrights=3"
				+ " and usc.ncontrolcode=cm.ncontrolcode"
				+ " and usc.nuserrolecode=uhs.nuserrolecode and us.nusercode=uhs.nusercode and   uhs.nusercode in ("
				+ nusercode + ")";
		List<ControlMaster> controlmaster = jdbcTemplate.query(query, new ControlMaster());
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public void deleteUserScreenhideScreenRights(final String suserrolescreencode, final Integer nuserrolecode) throws Exception {
		StringBuffer sB = new StringBuffer();
		sB.append("delete from usersrolescreenhide where nformcode in"
				+ "  (select nformcode from usersrolescreen where nuserrolescreencode in (" + suserrolescreencode
				+ ") )" + " and nuserrolecode in (" + nuserrolecode + ")");
		sB.append("delete from userrolescreencontrolhide where nformcode in "
				+ " (select nformcode from usersrolescreen where nuserrolescreencode in (" + suserrolescreencode + ") )"
				+ " and nuserrolecode in (" + nuserrolecode + ")");
		jdbcTemplate.execute(sB.toString());
	}

	public void createUserScreenhideScreenRights(final Map<String, Object> inputMap) throws Exception {
		String userrolescreencode = "";
		Integer nusersrolehidescreencode = 0;
		final List<Object> savedScreenRightsuserroleList = new ArrayList<>();
		final List<Object> beforeScreenRightsList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		Integer nusercode = null;
		List<UsersRoleScreenHide> lstacAvailableScreenArrayCollection = (List<UsersRoleScreenHide>) objmapper
				.convertValue(inputMap.get("userscreenhide"), new TypeReference<List<UsersRoleScreenHide>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		nusercode = (Integer) inputMap.get("usercode");

		userrolescreencode = stringUtilityFunction.fnDynamicListToString(lstacAvailableScreenArrayCollection,
				"getNformcode");
		final String query = "select * from usersrolescreenhide where nformcode in (" + userrolescreencode + ")"
				+ " and nuserrolecode=" + nuserrolecode + " and nusercode=" + nusercode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<UsersRoleScreen> lstUser = (List<UsersRoleScreen>) jdbcTemplate.query(query, new UsersRoleScreen());
		if (lstUser.size() == 0) {
			if (lstacAvailableScreenArrayCollection.size() > 0) {
				savedScreenRightsuserroleList.add(lstacAvailableScreenArrayCollection);
				String querys = "insert into usersrolescreenhide (nusersrolehidescreencode,nformcode,nusercode,nuserrolecode,nstatus,needrights,nsitecode,dmodifieddate) "
						+ " select  " + inputMap.get("nusersrolehidescreencode")
						+ " +RANK() over(order by nformcode) as nusersrolehidescreencode, nformcode as nformcode "
						+ " ," + inputMap.get("usercode") + " as nusercode, " + inputMap.get("nuserrolecode")
						+ " as nuserrolecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " as nstatus,4 as needrights," + userInfo.getNmastersitecode() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
						+ " from qualisforms where nformcode in (" + userrolescreencode + ") and nstatus=1";
				querys += "insert into userrolescreencontrolhide (nuserrolehidecontrolecode,ncontrolcode,nusercode,nformcode,nuserrolecode,nstatus,nsitecode,dmodifieddate)  "
						+ " select " + inputMap.get("nuserrolehidecontrolecode")
						+ "+RANK() over(order by ncontrolcode) as nuserrolehidecontrolecode" + " ,ncontrolcode,"
						+ inputMap.get("usercode") + " as nusercode,nformcode," + inputMap.get("nuserrolecode") + ""
						+ " as nuserrolecode,1 as nstatus," + userInfo.getNmastersitecode() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'  from controlmaster where nformcode in ( select  nformcode as nformcode "
						+ " from qualisforms where nformcode in (" + userrolescreencode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				jdbcTemplate.execute(querys);

				querys = "select max(nusersrolehidescreencode) from usersrolescreenhide";
				nusersrolehidescreencode = (Integer) jdbcUtilityFunction.queryForObject(querys, Integer.class,
						jdbcTemplate);

				querys = " update seqnocredentialmanagement set nsequenceno= " + (nusersrolehidescreencode)
						+ " where stablename ='usersrolescreenhide'";
				jdbcTemplate.execute(querys);

				querys = "select max(nuserrolehidecontrolecode) from userrolescreencontrolhide";
				int nuserrolehidecontrolecode = (int) jdbcUtilityFunction.queryForObject(querys, Integer.class,
						jdbcTemplate);

				querys = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolehidecontrolecode)
						+ " where stablename ='userrolescreencontrolhide'";
				jdbcTemplate.execute(querys);

				auditUtilityFunction.fnInsertListAuditAction(savedScreenRightsuserroleList, 1, beforeScreenRightsList,
						Arrays.asList("IDS_ADDUSERROLESCREENHIDE"), userInfo);
			}
		}
	}

}
