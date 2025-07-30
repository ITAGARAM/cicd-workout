package com.agaramtech.qualis.configuration.service.transactionfilterconfiguration;

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

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.TransactionFilterTypeConfig;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Department;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TransactionFilterConfigrationDAOImpl implements TransactionFilterConfigrationDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFilterConfigrationDAOImpl.class);
	

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	

	@Override
	public ResponseEntity<Object> getRegistrationSubtypeConfigration(Integer ninstCode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<>();
		final Map<String, Object> SampleType = new HashMap<>();
		final Map<String, Object> registrationtype = new HashMap<>();
		RegistrationSubType selectedRegSubType = null;
		String strQuery = "select st.ntransfiltertypecode,st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename from sampletype st where st.ntransfiltertypecode > -1 and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nsitecode = "+userInfo.getNmastersitecode()+" order by st.nsampletypecode desc";
		LOGGER.info("query: " + strQuery);
		List<SampleType> lstSampleType = (List<SampleType>) jdbcTemplate.query(strQuery, new SampleType());
         //ALPD-5276--Vignesh R(27-01-2025)--Transaction Users--Error occurs without configure the transaction filter config
		if(!lstSampleType.isEmpty()) {
		String strRegtypeQuery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
				+ " from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ " where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsampletypecode = "
				+ lstSampleType.get(0).getNsampletypecode() + " and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nregtypecode > 0 and "
				+" ac.nsitecode = "+userInfo.getNmastersitecode()+" and rt.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ " rst.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		List<RegistrationType> lstRegtype = (List<RegistrationType>) jdbcTemplate.query(strRegtypeQuery,
				new RegistrationType());
		if (lstRegtype.size() > 0) {
			String strregsubQuery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
					+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
					+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
					+ " max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
					+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
					+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter,st.nsampletypecode,st.ntransfiltertypecode,rt.nregtypecode "
					+ " from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
					+ " where st.nsampletypecode > 0 and st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
					+ " and st.nsampletypecode=rt.nsampletypecode and rt.nregtypecode = "
					+ lstRegtype.get(0).getNregtypecode() + " and rt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rst.nregsubtypecode>0 and rsc.napprovalconfigcode=ac.napprovalconfigcode and rsc.ntransactionstatus= "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
					+ " and acv.nsitecode=rsc.nsitecode and rsc.nsitecode=" + userInfo.getNmastersitecode() + " and "
					+ " st.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and "
					+ " rst.nsitecode = " + userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and "
					+ " acv.nsitecode = " + userInfo.getNmastersitecode() + " and rsc.nsitecode = " + userInfo.getNmastersitecode() + " " 
					+ " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter,st.nsampletypecode,rt.nregtypecode order by rst.nregsubtypecode desc";

			List<RegistrationSubType> lstRegSubType = (List<RegistrationSubType>) jdbcTemplate.query(strregsubQuery,
					new RegistrationSubType());
			objMap.put("RegType", lstRegtype);
			objMap.put("RegSubType", lstRegSubType);
			objMap.put("InstrumentCalibration", lstSampleType);
			SampleType.put("label", lstSampleType.get(0).getSsampletypename());
			SampleType.put("value", lstSampleType.get(0).getNsampletypecode());
			SampleType.put("item", lstSampleType.get(0));

			registrationtype.put("label", lstRegtype.get(0).getSregtypename());
			registrationtype.put("value", lstRegtype.get(0).getNregtypecode());
			registrationtype.put("item", lstRegtype.get(0));
			objMap.put("filterSampleType", lstSampleType);
			objMap.put("filterRegtype", lstRegtype);
			objMap.put("defaultSampleTypeValue", SampleType);
			objMap.put("defaultRegtypeValue", registrationtype);
			objMap.put("SelectedSampleType", lstSampleType.get(0));
			objMap.put("SelectedRegType", lstRegtype.get(0));
			if (lstRegtype.size() > 0) {
			selectedRegSubType = (RegistrationSubType) lstRegSubType.get(lstRegSubType.size() - 1);
			}
			objMap.put("selectedRegSubType", selectedRegSubType);
			objMap.putAll((Map<String, Object>) getDepartmentAndUsers(selectedRegSubType, userInfo).getBody());
			return new ResponseEntity<>(objMap, HttpStatus.OK);
		}
		objMap.put("InstrumentCalibration", lstSampleType);
		SampleType.put("label", lstSampleType.get(0).getSsampletypename());
		SampleType.put("value", lstSampleType.get(0).getNsampletypecode());
		SampleType.put("item", lstSampleType.get(0));
		objMap.put("filterSampleType", lstSampleType);
		objMap.put("SelectedSampleType", lstSampleType.get(0));
		objMap.put("defaultSampleTypeValue", SampleType);
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getBySampleType(Integer ninstcatCode, Integer nregtypecode, UserInfo userInfo)
			throws Exception {

		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		RegistrationSubType selectedRegSubType = null;
		String strQuery = "select st.ntransfiltertypecode,st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename from sampletype st where st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nsampletypecode="
				+ ninstcatCode + " order by 1 asc";
		
		SampleType lstInsCat = (SampleType) jdbcUtilityFunction.queryForObject(strQuery, SampleType.class, jdbcTemplate);
		objMap.put("SelectedSampleType", lstInsCat);
		String strregsubQuery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
				+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
				+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
				+ " max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
				+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter,st.nsampletypecode,st.ntransfiltertypecode,rt.nregtypecode "
				+ " from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
				+ " where st.nsampletypecode > 0 and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
				+ " and st.nsampletypecode=rt.nsampletypecode and rt.nregtypecode = " + nregtypecode
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nregtypecode=rst.nregtypecode and rst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nregsubtypecode=ac.nregsubtypecode and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and acv.napprovalconfigcode=ac.napprovalconfigcode and acv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "  and rst.nregsubtypecode>0 and rsc.napprovalconfigcode=ac.napprovalconfigcode and rsc.ntransactionstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and st.nsitecode= rt.nsitecode and rt.nsitecode=rst.nsitecode and rst.nsitecode=acv.nsitecode "
				+ " and acv.nsitecode=rsc.nsitecode and rsc.nsitecode=" + userInfo.getNmastersitecode() + " and "
				+ " st.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and "
				+ " rst.nsitecode = " + userInfo.getNmastersitecode() + " and ac.nsitecode = " + userInfo.getNmastersitecode() + " and "
				+ " acv.nsitecode = " + userInfo.getNmastersitecode() + " and rsc.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter,st.nsampletypecode,rt.nregtypecode order by rst.nregsubtypecode desc";

		List<RegistrationSubType> lstInstGet = (List<RegistrationSubType>) jdbcTemplate.query(strregsubQuery,
				new RegistrationSubType());

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		objMap.put("RegSubType", lstInstGet);
		if (!lstInstGet.isEmpty()) {
			selectedRegSubType = (RegistrationSubType) lstInstGet.get(lstInstGet.size() - 1);
			objMap.put("selectedRegSubType", selectedRegSubType);
			objMap.putAll((Map<String, Object>) getDepartmentAndUsers(selectedRegSubType, userInfo).getBody());
		} else {
			objMap.put("selectedRegSubType", null);
			objMap.put("DepartmentAndUser", null);
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	//@SuppressWarnings({})
	@Override
	public ResponseEntity<Object> getDepartment(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		String strQuery = "select d.* from department d,users u where d.ndeptcode=u.ndeptcode and d.ndeptcode > 0 "
				+ " and d.nsitecode = " + userInfo.getNmastersitecode() + " and u.nsitecode = " + userInfo.getNmastersitecode() + " and d.ndeptcode not in( "
				+ "select nmappingfieldcode from transactionfiltertypeconfig where nsitecode = " + userInfo.getNmastersitecode() + " and "
				+ " nneedalluser="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " nregsubtypecode=" + inputMap.get("nregsubtypecode") + " ) group by d.ndeptcode";
		final List<Department> lstDepartment = (List<Department>) jdbcTemplate.query(strQuery, new Department());
		objMap.put("Section", lstDepartment);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDepartmentBasedUser(int nregsubtypecode, int nmappingfieldcode, UserInfo userInfo)
			throws Exception {
		String strQuery = " select  u.nusercode,CONCAT(u.sfirstname,' ',u.slastname) as susername " + " from users u "
				+ " where u.ndeptcode=" + nmappingfieldcode + " and u.nsitecode = " + userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nusercode not in ( select tu.nusercode from transactionusers tu,transactionfiltertypeconfig tfc where  "
				+ " tu.ntransfiltertypeconfigcode=tfc.ntransfiltertypeconfigcode " + "	and tfc.nregsubtypecode="
				+ nregsubtypecode + " and tfc.nmappingfieldcode=" + nmappingfieldcode + " and tfc.nneedalluser= "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" "
				+ "	and tfc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tu.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tu.nsitecode = " + userInfo.getNmastersitecode() + " and tfc.nsitecode = " + userInfo.getNmastersitecode() + ")";
		
		final Map<String, Object> objMap = new HashMap<>();
		final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());

		String userlist = "  select count(tu.nusercode) from transactionusers tu,transactionfiltertypeconfig tfc where "
				+ "	tu.ntransfiltertypeconfigcode=tfc.ntransfiltertypeconfigcode " + "	and tfc.nregsubtypecode="
				+ nregsubtypecode + " and tfc.nmappingfieldcode=" + nmappingfieldcode + " and tfc.nneedalluser= "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
				+ "	 and tfc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tu.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and tu.nsitecode = " + userInfo.getNmastersitecode() + " and tfc.nsitecode = " + userInfo.getNmastersitecode()+"";
		int usercount = jdbcTemplate.queryForObject(userlist, Integer.class);

		objMap.put("isdisable", usercount == 0 ? false : true);
		objMap.put("Users", lstUsers);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	//@SuppressWarnings({})
	@Override
	public ResponseEntity<Object> getUserRole(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<>();
		final String strQuery = "select umr.nuserrolecode,ur.suserrolename from usermultirole umr,userrole ur where "
				+ " umr.nuserrolecode =ur.nuserrolecode and ur.nuserrolecode > 0 and umr.nsitecode = "+userInfo.getNmastersitecode()
				+ " and ur.nsitecode = "+ userInfo.getNmastersitecode() + " and ur.nuserrolecode not in( "
				+ "select nmappingfieldcode from transactionfiltertypeconfig where nsitecode = "+userInfo.getNmastersitecode()+" and nneedalluser= "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nstatus= "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" and nregsubtypecode="
				+ inputMap.get("nregsubtypecode") + " ) " + " group by umr.nuserrolecode,ur.suserrolename ";
		final List<UserRole> lstUserRole = (List<UserRole>) jdbcTemplate.query(strQuery, new UserRole());
		objMap.put("UserRole", lstUserRole);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserRoleBasedUser(int nregsubtypecode, int nuserrolecode, UserInfo userInfo)
			throws Exception {
       //ALPD-4267-Vignesh R-(01-06-2024)-Transaction User--->Duplicate user are showing, when try to add in user role 
		String strQuery = " select  u.nusercode,CONCAT(u.sfirstname,' ',u.slastname) as susername "
				+ " from users u,usermultirole umr,userssite us "
				+ " where umr.nusersitecode=us.nusersitecode and us.nusercode =u.nusercode and umr.nuserrolecode="
				+ nuserrolecode + " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and umr.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and us.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " and umr.nsitecode = "+userInfo.getNmastersitecode()
				+ " and us.nsitecode = "+userInfo.getNmastersitecode()
				+ " and u.nusercode not in ( select tu.nusercode from transactionusers tu,transactionfiltertypeconfig tfc where  "
				+ "	tu.ntransfiltertypeconfigcode=tfc.ntransfiltertypeconfigcode and tu.nsitecode = "+userInfo.getNmastersitecode()
				+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()
				+ "	and tfc.nregsubtypecode=" + nregsubtypecode + " and tfc.nmappingfieldcode=" + nuserrolecode
				+ " and tfc.nneedalluser=4 and tfc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")  group by u.nusercode";
		
		final Map<String, Object> objMap = new HashMap<>();
		final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());

		String userlist = "  select count(tu.nusercode) from transactionusers tu,transactionfiltertypeconfig tfc where "
				+ "	tu.ntransfiltertypeconfigcode=tfc.ntransfiltertypeconfigcode " + "	and tfc.nregsubtypecode="
				+ nregsubtypecode + " and tfc.nmappingfieldcode=" + nuserrolecode + " and tfc.nneedalluser= "+Enumeration.TransactionStatus.NO.gettransactionstatus()+"  "
				+ "	 and tfc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and tu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tu.nsitecode = "+userInfo.getNmastersitecode()+" and tfc.nsitecode = "+userInfo.getNmastersitecode();
		int usercount = jdbcTemplate.queryForObject(userlist, Integer.class);

		objMap.put("isdisable", usercount == 0 ? false : true);
		objMap.put("Users", lstUsers);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	//@SuppressWarnings({ "unchecked" })
	@Override
	public ResponseEntity<Object> createDepartment(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		RegistrationSubType selectedregsubtype = objMapper.convertValue(inputMap.get("selectedregsubtype"),
				new TypeReference<RegistrationSubType>() {
				});
		int nmappingfieldcode = (int) (inputMap.get("nmappingfieldcode"));
		int nallusers = (int) (inputMap.get("nallusers"));
		String depertSeq = "select (nsequenceno)+1 from seqnoconfigurationmaster where stablename='transactionfiltertypeconfig'";
		int deptSeqNo = jdbcTemplate.queryForObject(depertSeq, Integer.class);
		String userlistSeq = "select nsequenceno from seqnoconfigurationmaster where stablename='transactionusers'";
		int userseqNo = jdbcTemplate.queryForObject(userlistSeq, Integer.class);

		String insertstr = "insert into transactionfiltertypeconfig (ntransfiltertypeconfigcode,ntransfiltertypecode,nsampletypecode,nregtypecode,nregsubtypecode,"
				+ " nmappingfieldcode,nneedalluser,dmodifieddate,nsitecode,nstatus) values(" + deptSeqNo + ","
				+ selectedregsubtype.getNtransfiltertypecode() + "," + selectedregsubtype.getNsampletypecode() + ","
				+ selectedregsubtype.getNregtypecode() + "," + selectedregsubtype.getNregsubtypecode() + ","
				+ nmappingfieldcode + "," + nallusers + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")";
		
		jdbcTemplate.execute(insertstr);

		if (nallusers == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final List<Users> usermapping = objMapper.convertValue(inputMap.get("usermapping"),
					new TypeReference<List<Users>>() {
					});
			final String susercode = usermapping.stream().map(users -> String.valueOf(users.getNusercode()))
					.collect(Collectors.joining(","));

			String userrmappingInsert = "insert into transactionusers(ntransusercode,ntransfiltertypeconfigcode,nusercode,dmodifieddate,nsitecode,nstatus)"
					+ "(select " + userseqNo + "+rank()over(order by nusercode)as ntransusercode," + deptSeqNo
					+ ",nusercode" + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from  users where nusercode in("
					+ susercode + ")  )";
			
			jdbcTemplate.execute(userrmappingInsert);
			String Strupateqry = "update seqnoconfigurationmaster set nsequenceno="
					+ (userseqNo + susercode.split(",").length) + " where stablename='transactionusers'";
			
			jdbcTemplate.execute(Strupateqry);
		}

		String Strqry = "update seqnoconfigurationmaster set nsequenceno=" + deptSeqNo
				+ " where stablename='transactionfiltertypeconfig'";
		
		jdbcTemplate.execute(Strqry);
		objMap.putAll((Map<String, Object>) getDepartmentAndUsers(selectedregsubtype, userInfo).getBody());

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegtypeBasedSampleType(int nsampletype, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<>();

		Map<String, Object> registrationtype = new HashMap<>();
		String strRegtypeQuery = "select rt.nregtypecode,coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US')  as sregtypename "
				+ "from approvalconfig ac,approvalconfigversion acv,registrationtype rt,registrationsubtype rst "
				+ "where acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ac.napprovalconfigcode = acv.napprovalconfigcode and rt.nregtypecode = ac.nregtypecode "
				+ " and rt.nregtypecode = rst.nregtypecode and rst.nregsubtypecode = ac.nregsubtypecode "
				+ " and rt.nsampletypecode = " + nsampletype
				+ " and acv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " ac.nsitecode = "+userInfo.getNmastersitecode()+" and acv.nsitecode = "+userInfo.getNmastersitecode()
				+ " and rt.nsitecode = "+userInfo.getNmastersitecode() + " and rst.nsitecode = "+userInfo.getNmastersitecode()
				+ " and rt.nregtypecode > 0 group by rt.nregtypecode,sregtypename  order by rt.nregtypecode desc";
		List<RegistrationType> lstRegtype = (List<RegistrationType>) jdbcTemplate.query(strRegtypeQuery,
				new RegistrationType());
		if (!lstRegtype.isEmpty()) {
			objMap.put("RegType", lstRegtype);
			registrationtype.put("label", lstRegtype.get(0).getSregtypename());
			registrationtype.put("value", lstRegtype.get(0).getNregtypecode());
			registrationtype.put("item", lstRegtype.get(0));
			objMap.put("defaultRegtypeValue", registrationtype);
			objMap.put("SelectedRegType", registrationtype);
			objMap.put("filterRegtype", lstRegtype);
		} else {
			objMap.put("RegType", null);
			objMap.put("defaultRegtypeValue", null);
			objMap.put("filterRegtype", null);
			objMap.put("SelectedRegType", null);
			objMap.put("RegSubType", null);
			objMap.put("DepartmentAndUser", null);
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> deleteDepartment(TransactionFilterTypeConfig instSec, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		if (instSec.getNneedalluser() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			String Strqry = "update transactionfiltertypeconfig set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ " where ntransfiltertypeconfigcode=" + instSec.getNtransfiltertypeconfigcode();
			jdbcTemplate.execute(Strqry);
		} else {

			String Strqry = "update transactionusers set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntransusercode="
					+ instSec.getNtransusercode();
			jdbcTemplate.execute(Strqry);

			String st1 = "Select count(ntransusercode) from transactionusers where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " nsitecode = "+userInfo.getNmastersitecode()+" and ntransfiltertypeconfigcode="
					+ instSec.getNtransfiltertypeconfigcode();
			int transusercode = jdbcTemplate.queryForObject(st1, Integer.class);
			if (transusercode == 0) {
				String Strqrey = "update transactionfiltertypeconfig set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where ntransfiltertypeconfigcode=" + instSec.getNtransfiltertypeconfigcode();
				jdbcTemplate.execute(Strqrey);
			}

		}
		String str = "";
		if (instSec.getNtransfiltertypecode() == 1) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM department d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.ndeptcode and tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		} else if (instSec.getNtransfiltertypecode() == 2) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.suserrolename as sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM userrole d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.nuserrolecode and tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = 1 "
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		else {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode "
					+ " , (s.sfirstname||' '||s.slastname)  susername "
					+ " FROM  transactionfiltertypeconfig tfc,transactionusers tu,users s where   tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " and tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tfc.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		final List<TransactionFilterTypeConfig> lstUserRole = (List<TransactionFilterTypeConfig>) jdbcTemplate
				.query(str, new TransactionFilterTypeConfig());
		objMap.put("DepartmentAndUser", lstUserRole);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTabdetails(TransactionFilterTypeConfig instSec, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		String strregsubQuery = "select st.nsampletypecode,st.ntransfiltertypecode,rt.nregtypecode,rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',rst.jsondata->'sregsubtypename'->>'en-US') sregsubtypename from sampletype st,registrationtype rt,registrationsubtype rst "
				+ " where rt.nregtypecode=rst.nregtypecode and st.nsampletypecode=rt.nsampletypecode and st.nsitecode = "+userInfo.getNmastersitecode()
				+" and rt.nsitecode = "+userInfo.getNmastersitecode()+" and rst.nsitecode = "+userInfo.getNmastersitecode()+" and rst.nregsubtypecode="
				+ instSec.getNregsubtypecode();
		RegistrationSubType lstInsCat =(RegistrationSubType) jdbcUtilityFunction.queryForObject(strregsubQuery, RegistrationSubType.class, jdbcTemplate); 
						
		String str = "";
		if (lstInsCat.getNtransfiltertypecode() == 1) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM department d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.ndeptcode and tfc.nregsubtypecode="
					+ lstInsCat.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		} else if (lstInsCat.getNtransfiltertypecode() == 2) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.suserrolename as sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM userrole d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.nuserrolecode and tfc.nregsubtypecode="
					+ lstInsCat.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		else {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode "
					+ " , (s.sfirstname||' '||s.slastname)  susername "
					+ " FROM  transactionfiltertypeconfig tfc,transactionusers tu,users s where   tfc.nregsubtypecode="
					+ lstInsCat.getNregsubtypecode()
					+ " and tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tfc.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		final List<TransactionFilterTypeConfig> lstUserRole = (List<TransactionFilterTypeConfig>) jdbcTemplate
				.query(str, new TransactionFilterTypeConfig());

		objMap.put("DepartmentAndUser", lstUserRole);
		objMap.put("selectedRegSubType", lstInsCat);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDepartmentAndUsers(RegistrationSubType instSec, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		String str = "";
		if (instSec.getNtransfiltertypecode() == 1) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM department d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.ndeptcode and tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = 1 "
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(); 
					//+ " and d.nsitecode = "+userInfo.getNmastersitecode()
					//+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()//+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					//+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		} else if (instSec.getNtransfiltertypecode() == 2) {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode,d.suserrolename as sdeptname,case  when (s.sfirstname||' '||s.slastname)  is null  then 'ALL'else  (s.sfirstname||' '||s.slastname) end  as susername"
					+ " FROM userrole d "
					+ " LEFT OUTER JOIN transactionfiltertypeconfig tfc ON tfc.nmappingfieldcode = d.nuserrolecode and tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " LEFT OUTER JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " LEFT OUTER JOIN users s ON s.nusercode = tu.nusercode AND s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(); 
					//+ " and d.nsitecode = "+userInfo.getNmastersitecode()
					//+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()//+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					//+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		else {
			str = "select tfc.nregsubtypecode,tu.ntransusercode,tfc.nneedalluser,tfc.ntransfiltertypeconfigcode "
					+ " , (s.sfirstname||' '||s.slastname)  susername "
					+ " FROM  transactionfiltertypeconfig tfc,transactionusers tu,users s where tfc.nregsubtypecode="
					+ instSec.getNregsubtypecode()
					+ " and tu.ntransfiltertypeconfigcode = tfc.ntransfiltertypeconfigcode AND tu.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and s.nusercode = tu.nusercode AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and tfc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					//+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()//+" and tu.nsitecode = "+userInfo.getNmastersitecode()
					//+ " and s.nsitecode = "+userInfo.getNmastersitecode();
		}

		final List<TransactionFilterTypeConfig> lstUserRole = (List<TransactionFilterTypeConfig>) jdbcTemplate
				.query(str, new TransactionFilterTypeConfig());

		objMap.put("DepartmentAndUser", lstUserRole);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getListofUsers(int nregsubtypecode, UserInfo userInfo) throws Exception {
		String strQuery = " select  u.nusercode,CONCAT(u.sfirstname,' ',u.slastname) as susername " + " from users u "
				+ " where u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.ntransactionstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nsitecode = "+userInfo.getNmastersitecode()
				+ " and u.nusercode > 0 and u.nusercode not in ( select tu.nusercode from transactionusers tu,transactionfiltertypeconfig tfc where  "
				+ " tu.ntransfiltertypeconfigcode=tfc.ntransfiltertypeconfigcode and tu.nsitecode = "+userInfo.getNmastersitecode()
				+ " and tfc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and tfc.nregsubtypecode=" + nregsubtypecode + " and tfc.nneedalluser= "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" "
				+ " and tfc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
		Map<String, Object> objMap = new HashMap<>();
		final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());

		objMap.put("Users", lstUsers);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

}
