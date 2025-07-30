package com.agaramtech.qualis.attachmentscomments.service.comments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestComment;
import com.agaramtech.qualis.basemaster.model.CommentSubType;
import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class CommentDAOImpl implements CommentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommentDAOImpl.class);	
	
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	

	@Override
	public ResponseEntity <Map<String,Object>> getTestComment(String ntransactiontestcode, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String sampleNo = "b";
		
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() ||
			userInfo.getNformcode() == Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms() ||
			userInfo.getNformcode() == Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}		
		final String getComment = "select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
								+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as groupingField2,"
								+ "  case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as  sarno,"
								+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as  ssamplearno,"
								+ " ra.*,stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,ra.jsondata->>'scomments' as scomments,ra.jsondata->>'stestsynonym' as stestsynonym,"
								+ "	   ra.jsondata ->> 'scommentsubtype'               AS scommentsubtype, "
								+ "       ra.jsondata ->> 'spredefinedname'            AS spredefinedname, "
								+ "	          ra.jsondata ->> 'sdescription'            AS sdescription,"
								+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'nneedreport' as nneedreport,"
								+ " ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sneedreport"
								+ " from registrationtestcomments ra,registrationarno rar,users u,userrole ur,"
								+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc,transactionstatus ts"
								+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
								+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
								+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode  and ts.ntranscode = (coalesce(ra.jsondata->>'nneedreport','4'))::int "
								+ " and rar.npreregno = sar.npreregno and sar.nstatus = ra.nstatus"
								+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
								+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
								+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.ntransactiontestcode in (" + ntransactiontestcode + ")"
								+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode  "
							//	+ " sar.nsitecode = stc.nsitecode "
								+ " and sar.nsitecode="+ userInfo.getNtranssitecode()
								+" and stc.nsitecode="+ userInfo.getNmastersitecode();;

		returnMap.put("RegistrationTestComment", jdbcTemplate.query(getComment,new RegistrationTestComment()));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	private JSONObject constructJSONObjectByTest(final String transactionTestCode) throws Exception{
		final JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
									+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
									+ "from registration where  npreregno in (select  npreregno from registrationtest where  ntransactiontestcode in ("+transactionTestCode
									+ "))  group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		return jsoncodes;
	}

	@Override
	public ResponseEntity<? extends Object> createTestComment(List<RegistrationTestComment> listTestComment,
			String ntransactiontestcode, UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		final Map<String, Object> auditMap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonTestCommentstArray = new JSONArray();

		String stestcommentcode = "";
		final JSONObject jsonCode = constructJSONObjectByTest(ntransactiontestcode);
		jdbcTemplate.execute("lock locktestcomments "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");

		int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationcomments where stablename ='registrationtestcomments'", Integer.class);
		final StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into registrationtestcomments (ntestcommentcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nsamplecommentscode,"
				+ "nformcode,nusercode,nuserrolecode,jsondata,nsitecode, nstatus) values");
		for(RegistrationTestComment testComments:listTestComment) {
			seqNo++;
			stestcommentcode+=String.valueOf(seqNo)+",";
			testComments.setNtestcommentcode(seqNo);
			insertQuery.append("("+seqNo+","+testComments.getNpreregno()+","+testComments.getNtransactionsamplecode()+","+testComments.getNtransactiontestcode()+
								","+testComments.getNsamplecommentscode()+","+userInfo.getNformcode()+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+
								",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(testComments.getJsondata()))+"',"
								+ userInfo.getNtranssitecode() + ","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
		}
		final String updateSeqNoString = "update seqnoregistrationcomments set nsequenceno = nsequenceno+"+listTestComment.size()+" where stablename = 'registrationtestcomments'";
		jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
	
		jsonTestCommentstArray =  getTestCommentAudit(ntransactiontestcode,stestcommentcode.substring(0, stestcommentcode.length()-1),true, userInfo) ;
		jsonAuditObject.put("registrationtestcomments", jsonTestCommentstArray);
		
		auditMap.put("nregtypecode", jsonCode.get("nregtypecode"));
		auditMap.put("nregsubtypecode", jsonCode.get("nregsubtypecode"));
		auditMap.put("ndesigntemplatemappingcode", jsonCode.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationtestcomments", "IDS_ADDTESTCOMMENT");
		
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditMap, false, userInfo);
				
		return getTestComment(ntransactiontestcode, userInfo);

	}

	@Override
	public ResponseEntity<Object> getEditTestComment(RegistrationTestComment objTestComment, UserInfo userInfo)
			throws Exception {
		final String getComment = "select rar.sarno,sar.ssamplearno, ra.*,stc.sdescription as ssampletestcommentname,"
								+ " u.sfirstname||' ' ||u.slastname susername,ur.suserrolename,ra.jsondata->>'scomments' as scomments,ra.jsondata->>'stestsynonym' as stestsynonym,"
								+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'nneedreport' as nneedreport "
								+ " from registrationtestcomments ra,registrationarno rar,users u,userrole ur,"
								+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc"
								+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
								+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
								+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode"
								+ " and rar.npreregno = sar.npreregno and sar.nstatus = ra.nstatus"
								+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
								+ " and stc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.ntestcommentcode =" + objTestComment.getNtestcommentcode() 
								+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
								//+ " and sar.nsitecode=stc.nsitecode "
								+ " and sar.nsitecode="
								+ userInfo.getNtranssitecode()
								+" and stc.nsitecode="+ userInfo.getNmastersitecode();;

		final RegistrationTestComment objEditTestComment = (RegistrationTestComment) jdbcUtilityFunction.queryForObject(getComment, RegistrationTestComment.class, jdbcTemplate);
		if (objEditTestComment != null) {

			return new ResponseEntity<>(objEditTestComment, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public ResponseEntity<? extends Object> updateTestComment(RegistrationTestComment objTestComment, String ntransactiontestcode,
			UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		final Map<String, Object> auditMap = new LinkedHashMap<String, Object>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObjectOld = new JSONObject();
		final JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonTestCommentstArrayOld = new JSONArray();
		JSONArray jsonTestCommentstArraynew = new JSONArray();
		
		final JSONObject jsonCodes = constructJSONObjectByTest(ntransactiontestcode);
		
		final String sQuery = "select * from registrationtestcomments where ntestcommentcode = "
							+ objTestComment.getNtestcommentcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode="+ userInfo.getNtranssitecode();
		final RegistrationTestComment validateComment = (RegistrationTestComment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationTestComment.class, jdbcTemplate);
		if (validateComment == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			jsonTestCommentstArrayOld =  getTestCommentAudit(ntransactiontestcode,String.valueOf(objTestComment.getNtestcommentcode()),true, userInfo) ;

			final String updateQueryString = "update registrationtestcomments set"
											+ " jsondata = jsondata || '"+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objTestComment.getJsondata())) + "'," 
											+ " nsamplecommentscode ="+ objTestComment.getNsamplecommentscode() + ","
											+ " nusercode = " + userInfo.getNusercode()+ " ,"
											+ " nuserrolecode = " + userInfo.getNuserrole() + ","
											+ " nformcode = " + userInfo.getNformcode()
											+ " where ntestcommentcode=" + objTestComment.getNtestcommentcode()
											+ " and nsitecode="+userInfo.getNtranssitecode();

			jdbcTemplate.execute(updateQueryString);

			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITTESTCOMMENT");

			//final List<Object> listAfterSave = new ArrayList<>();
			//listAfterSave.add(objTestComment);

			//final List<Object> listBeforeSave = new ArrayList<>();
			//listBeforeSave.add(validateComment);

			//fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

			// status code:200
			
			jsonTestCommentstArraynew =  getTestCommentAudit(ntransactiontestcode,String.valueOf(objTestComment.getNtestcommentcode()),true, userInfo) ;
			jsonAuditObjectnew.put("registrationtestcomments", jsonTestCommentstArraynew);
			jsonAuditObjectOld.put("registrationtestcomments", jsonTestCommentstArrayOld);
			auditMap.put("nregtypecode", jsonCodes.get("nregtypecode"));
			auditMap.put("nregsubtypecode", jsonCodes.get("nregsubtypecode"));
			auditMap.put("ndesigntemplatemappingcode", jsonCodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationtestcomments", "IDS_EDITTESTCOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditMap, false, userInfo);
						
			return getTestComment(ntransactiontestcode, userInfo);

		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteTestComment(RegistrationTestComment objTestComment, String ntransactionTestCode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonTestCommentstArray = new JSONArray();
		
		JSONObject jsoncodes = constructJSONObjectByTest(ntransactionTestCode);
		
		final String sQuery = "select * from registrationtestcomments where ntestcommentcode = "
				+ objTestComment.getNtestcommentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+userInfo.getNtranssitecode();
		final RegistrationTestComment validateComment = (RegistrationTestComment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationTestComment.class, jdbcTemplate);
		if (validateComment != null) {

			final String sUpdateQuery = "update registrationtestcomments set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntestcommentcode = "
					+ objTestComment.getNtestcommentcode()
					+ " and nsitecode="+userInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add("IDS_DELETETESTCOMMENT");
			lstObject.add(validateComment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			
			
			jsonTestCommentstArray =  getTestCommentAudit(ntransactionTestCode,String.valueOf(objTestComment.getNtestcommentcode()),false, userInfo) ;
			jsonAuditObject.put("registrationtestcomments", jsonTestCommentstArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationtestcomments", "IDS_DELETETESTCOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
						
			return getTestComment(ntransactionTestCode, userInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Map<String,Object>> getSampleComment(String npreregno, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		//String sampleNo = "b";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() || userInfo.getNformcode() == Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
		//	sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}		
		final String getComment = "select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
				+ " case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as sarno, ra.*,"
				+ " stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'scomments' as scomments"
				+ " from registrationcomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno "
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode "
				+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
				+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
				+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.npreregno in (" + npreregno + ")"
				+ " and ra.nsitecode=rar.nsitecode "
				//+ " and rar.nsitecode=stc.nsitecode "
				+ " and rar.nsitecode="
				+ userInfo.getNtranssitecode()
				+" and stc.nsitecode="+ userInfo.getNmastersitecode();



		returnMap.put("RegistrationComment", jdbcTemplate.query(getComment,new RegistrationComment()));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	private JSONObject constructJSONObjectBySample(final String npreRegNo) throws Exception{
		final JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
									+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
									+ "from registration where  npreregno in ("+npreRegNo+") group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		return jsoncodes;
	}

	@Override
	public ResponseEntity<? extends Object> createSampleComment(List<RegistrationComment> listSampleComment,
			String npreregno, UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonTestCommentstArray = new JSONArray();
//		ObjectMapper objmap = new ObjectMapper();
		String stestcommentcode="";
//		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
//				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
//				+ "from registration where  npreregno in ("+npreregno+") group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		JSONObject jsoncodes= constructJSONObjectBySample(npreregno);
		
		jdbcTemplate.execute("lock lockcomments "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");

//		final List<String> multilingualIDList = new ArrayList<>();
		int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationcomments where stablename ='registrationcomment'", Integer.class);
		final StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into registrationcomment (nregcommentcode,npreregno,nsamplecommentscode,"
				+ "nformcode,nusercode,nuserrolecode,jsondata,nsitecode, nstatus) values");
		for(RegistrationComment sampleComments:listSampleComment) {
			seqNo++;
			sampleComments.setNregcommentcode(seqNo);
			stestcommentcode+=String.valueOf(seqNo)+",";
			insertQuery.append("("+seqNo+","+sampleComments.getNpreregno()+
					","+sampleComments.getNsamplecommentscode()+","+userInfo.getNformcode()+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+
					",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(sampleComments.getJsondata()))+"',"
					+ userInfo.getNtranssitecode() + "," +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
		}
		final String updateSeqNoString = "update seqnoregistrationcomments set nsequenceno = nsequenceno+"+listSampleComment.size()+" where stablename = 'registrationcomment'";
		jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
		//multilingualIDList.add("IDS_ADDSAMPLECOMMENT");
		//fnInsertListAuditAction(Arrays.asList(listSampleComment), 1, null, multilingualIDList, userInfo);
		
		
		jsonTestCommentstArray =  getSampleCommentAudit(npreregno,stestcommentcode.substring(0, stestcommentcode.length()-1),true, userInfo) ;
		jsonAuditObject.put("registrationComment", jsonTestCommentstArray);
		auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
		auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationComment", "IDS_ADDSAMPLECOMMENT");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		
		return getSampleComment(npreregno, userInfo);

	}
	

	@Override
	public ResponseEntity<Object> getEditSampleComment(RegistrationComment objSampleComment, UserInfo userInfo)
			throws Exception {
		final String getComment = "select rar.sarno, ra.*,stc.sdescription as ssampletestcommentname,"
				+ " u.sfirstname||' ' ||u.slastname susername,ur.suserrolename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"'"
				+ " from registrationcomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno "
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode"
				+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
				+ " and stc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nregcommentcode =" + objSampleComment.getNregcommentcode()
				+ " and ra.nsitecode=rar.nsitecode "
				//+ " and rar.nsitecode=stc.nsitecode "
				+ " and rar.nsitecode="+ userInfo.getNtranssitecode()
				+" and stc.nsitecode="+ userInfo.getNmastersitecode();
;

		RegistrationComment objEditSampleComment = (RegistrationComment) jdbcUtilityFunction.queryForObject(getComment, RegistrationComment.class, jdbcTemplate);
		if (objEditSampleComment != null) {

			return new ResponseEntity<>(objEditSampleComment, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public ResponseEntity<? extends Object> updateSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonSampleCommentstArrayOld = new JSONArray();
		JSONArray jsonSampleCommentstArraynew = new JSONArray();
//		ObjectMapper objmap = new ObjectMapper();
//		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
//				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
//				+ "from registration where  npreregno in ("+npreregno+") group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		JSONObject jsoncodes= constructJSONObjectBySample(npreregno);
		
		final String sQuery = "select * from registrationcomment where nregcommentcode = "
				+ objSampleComment.getNregcommentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ userInfo.getNtranssitecode();
		final RegistrationComment validateComment = jdbcTemplate.queryForObject(sQuery,new RegistrationComment());
		if (validateComment == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			jsonSampleCommentstArrayOld =  getSampleCommentAudit(npreregno,String.valueOf(objSampleComment.getNregcommentcode()),true, userInfo) ;

			final String updateQueryString = "update registrationcomment set"
					+ " jsondata = jsondata || '"+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objSampleComment.getJsondata())) + "'," 
					+ " nsamplecommentscode ="+ objSampleComment.getNsamplecommentscode() + ","
					+ " nusercode = " + userInfo.getNusercode()+ " ,"
					+ " nuserrolecode = " + userInfo.getNuserrole() + ","
					+ " nformcode = " + userInfo.getNformcode()
					+ " where nregcommentcode=" + objSampleComment.getNregcommentcode()
					+ " and nsitecode="+ userInfo.getNtranssitecode();

			jdbcTemplate.execute(updateQueryString);
			jsonSampleCommentstArraynew =  getSampleCommentAudit(npreregno,String.valueOf(objSampleComment.getNregcommentcode()),true, userInfo) ;

			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITSAMPLECOMMENT");

			final List<Object> listAfterSave = new ArrayList<>();
			listAfterSave.add(objSampleComment);

			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(validateComment);

			//fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
 			jsonAuditObjectnew.put("registrationComment", jsonSampleCommentstArraynew);
			jsonAuditObjectOld.put("registrationComment", jsonSampleCommentstArrayOld);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationComment", "IDS_EDITSAMPLECOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditmap, false, userInfo);
			// status code:200
			return getSampleComment(npreregno, userInfo);

		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo userInfo) throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSampleCommentstArray = new JSONArray();
//		ObjectMapper objmap = new ObjectMapper();
//		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
//				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
//				+ "from registration where  npreregno in ("+npreregno+") group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		JSONObject jsoncodes= constructJSONObjectBySample(npreregno);
		
		final String sQuery = "select * from registrationcomment where nregcommentcode = "
				+ objSampleComment.getNregcommentcode()+ " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ userInfo.getNtranssitecode();
		final RegistrationComment validateComment =(RegistrationComment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationComment.class, jdbcTemplate);
		if (validateComment != null) {

			final String sUpdateQuery = "update registrationcomment set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nregcommentcode = "
					+ objSampleComment.getNregcommentcode()
					+ " and nsitecode="+ userInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add("IDS_DELETESAMPLECOMMENT");
			lstObject.add(validateComment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			
			
			jsonSampleCommentstArray =  getSampleCommentAudit(npreregno,String.valueOf(objSampleComment.getNregcommentcode()),false, userInfo) ;
			jsonAuditObject.put("registrationComment", jsonSampleCommentstArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationComment", "IDS_DELETESAMPLECOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			
			return getSampleComment(npreregno, userInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	@Override
	public ResponseEntity<Map<String,Object>> getSubSampleComment(String ntransactionsamplecode, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String sampleNo = "b";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}		
		final String getComment = "select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as groupingField2,"
				+ " case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as  sarno,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as ssamplearno, ra.*,stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,ra.jsondata->>'scomments' as scomments,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationsamplecomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode "
				+ " and rar.npreregno = sar.npreregno and sar.nstatus = ra.nstatus"
				+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
				+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
				+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.ntransactionsamplecode in (" + ntransactionsamplecode + ")"
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode  "
				//+ " sar.nsitecode = stc.nsitecode "
				 + " and sar.nsitecode="+ userInfo.getNtranssitecode()
				+" and stc.nsitecode="+ userInfo.getNmastersitecode();


		returnMap.put("RegistrationSampleComment", jdbcTemplate.query(getComment,new RegistrationSampleComment()));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private JSONObject constructJSONObjectBySubSample(String ntransactionSampleCode) throws Exception{
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionSampleCode
				+ "))  group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		return jsoncodes;
	}
	@Override
	public ResponseEntity<Map<String,Object>> createSubSampleComment(List<RegistrationSampleComment> listTestComment,
			String ntransactionsamplecode, UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		JSONArray jsonSubsampleCommentstArray = new JSONArray(); 
		String ssamplecommentcode="";
//		ObjectMapper objmap = new ObjectMapper();
//		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
//				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
//				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionsamplecode
//				+ "))  group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		JSONObject jsoncodes= constructJSONObjectBySubSample(ntransactionsamplecode);
		
		jdbcTemplate.execute("lock locksamplecomments "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");

		final List<String> multilingualIDList = new ArrayList<>();
		int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationcomments where stablename ='registrationsamplecomment'", Integer.class);
		final StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into registrationsamplecomment (nsamplecommentcode,npreregno,ntransactionsamplecode,nsamplecommentscode,"
				+ "nformcode,nusercode,nuserrolecode,jsondata,nsitecode,nstatus) values");
		for(RegistrationSampleComment testComments:listTestComment) {
			seqNo++;
			testComments.setNsamplecommentcode(seqNo);
			ssamplecommentcode+=String.valueOf(seqNo)+",";
			insertQuery.append("("+seqNo+","+testComments.getNpreregno()+","+testComments.getNtransactionsamplecode()
					+","+testComments.getNsamplecommentscode()+","+userInfo.getNformcode()+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+
					",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(testComments.getJsondata()))+"',"
					+ userInfo.getNtranssitecode() + ","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
		}
		final String updateSeqNoString = "update seqnoregistrationcomments set nsequenceno = nsequenceno+"+listTestComment.size()+" where stablename = 'registrationsamplecomment'";
		jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
		multilingualIDList.add("IDS_ADDSUBSAMPLECOMMENT");
		//fnInsertListAuditAction(Arrays.asList(listTestComment), 1, null, multilingualIDList, userInfo);
	
		jsonSubsampleCommentstArray =  getSubSampleCommentAudit(ntransactionsamplecode,ssamplecommentcode.substring(0, ssamplecommentcode.length()-1),true, userInfo) ;
		jsonAuditObject.put("registrationsamplecomment", jsonSubsampleCommentstArray); 
		auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
		auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationsamplecomment", "IDS_ADDSUBSAMPLECOMMENT");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
		
		return getSubSampleComment(ntransactionsamplecode, userInfo);

	}

	@Override
	public ResponseEntity<Object> getEditSubSampleComment(RegistrationSampleComment objSampleComment, UserInfo userInfo)
			throws Exception {
		final String getComment = "select rar.sarno,sar.ssamplearno, ra.*,stc.sdescription as  ssampletestcommentname,"
				+ " u.sfirstname||' ' ||u.slastname susername,ur.suserrolename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"'"
				+ " from registrationsamplecomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode"
				+ " and rar.npreregno = sar.npreregno and sar.nstatus = ra.nstatus"
				+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
				+ " and stc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nsamplecommentcode =" + objSampleComment.getNsamplecommentcode() 
				+ " and ra.nsitecode=rar.nsitecode "
				+ " and rar.nsitecode=sar.nsitecode  "
				//+ " sar.nsitecode = stc.nsitecode "
				+ " and sar.nsitecode="+ userInfo.getNtranssitecode()
				+" and stc.nsitecode="+ userInfo.getNmastersitecode();

		RegistrationSampleComment objEditRegistrationSampleComment = (RegistrationSampleComment) jdbcUtilityFunction.queryForObject(getComment, RegistrationSampleComment.class, jdbcTemplate);
		if (objEditRegistrationSampleComment != null) {

			return new ResponseEntity<>(objEditRegistrationSampleComment, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public ResponseEntity<? extends Object> updateSubSampleComment(RegistrationSampleComment objRegistrationSampleComment, String ntransactionsamplecode,
			UserInfo userInfo) throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonsubsampleCommentstArrayOld = new JSONArray();
		JSONArray jsonsubsampleCommentstArraynew = new JSONArray();
		
		JSONObject jsoncodes= constructJSONObjectBySubSample(ntransactionsamplecode);
		
		final String sQuery = "select * from registrationsamplecomment where nsamplecommentcode = "
				+ objRegistrationSampleComment.getNsamplecommentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode= "+ userInfo.getNtranssitecode();
		final RegistrationSampleComment validateComment = (RegistrationSampleComment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationSampleComment.class, jdbcTemplate);
		if (validateComment == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			jsonsubsampleCommentstArrayOld =  getSubSampleCommentAudit(ntransactionsamplecode,String.valueOf(objRegistrationSampleComment.getNsamplecommentcode()),true, userInfo) ;

			final String updateQueryString = "update registrationsamplecomment set"
					+ " jsondata = jsondata || '"+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objRegistrationSampleComment.getJsondata())) + "'," 
					+ " nsamplecommentscode ="+ objRegistrationSampleComment.getNsamplecommentscode() + ","
					+ " nusercode = " + userInfo.getNusercode()+ " ,"
					+ " nuserrolecode = " + userInfo.getNuserrole() + ","
					+ " nformcode = " + userInfo.getNformcode()
					+ " where nsamplecommentcode=" + objRegistrationSampleComment.getNsamplecommentcode()
					+ " and nsitecode="+ userInfo.getNtranssitecode();

			jdbcTemplate.execute(updateQueryString);

			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITSUBSAMPLECOMMENT");

			final List<Object> listAfterSave = new ArrayList<>();
			listAfterSave.add(objRegistrationSampleComment);

			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(validateComment);

			//fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			
			jsonsubsampleCommentstArraynew =   getSubSampleCommentAudit(ntransactionsamplecode,String.valueOf(objRegistrationSampleComment.getNsamplecommentcode()),true, userInfo) ;
			jsonAuditObjectnew.put("registrationsamplecomment", jsonsubsampleCommentstArraynew);
			jsonAuditObjectOld.put("registrationsamplecomment", jsonsubsampleCommentstArrayOld);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationsamplecomment", "IDS_EDITSUBSAMPLECOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditmap, false, userInfo);
			// status code:200
			return getSubSampleComment(ntransactionsamplecode, userInfo);

		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteSubSampleComment(RegistrationSampleComment objRegistrationSampleComment, String ntransactionsamplecode,
			UserInfo userInfo) throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonsubsampleCommentstArray = new JSONArray();
//		ObjectMapper objmap = new ObjectMapper();
//		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
//				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
//				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionsamplecode
//				+ "))  group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		JSONObject jsoncodes= constructJSONObjectBySubSample(ntransactionsamplecode);
		
		final String sQuery = "select * from registrationsamplecomment where nsamplecommentcode = "
				+ objRegistrationSampleComment.getNsamplecommentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and nsitecode="+ userInfo.getNtranssitecode();
		final RegistrationSampleComment validateComment = (RegistrationSampleComment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationSampleComment.class, jdbcTemplate);
		if (validateComment != null) {

			final String sUpdateQuery = "update registrationsamplecomment set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsamplecommentcode = "
					+ objRegistrationSampleComment.getNsamplecommentcode()
					+ " and nsitecode="+ userInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList.add("IDS_DELETESUBSAMPLECOMMENT");
			lstObject.add(validateComment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			jsonsubsampleCommentstArray =  getSubSampleCommentAudit(ntransactionsamplecode,String.valueOf(objRegistrationSampleComment.getNsamplecommentcode()),false, userInfo) ;
			jsonAuditObject.put("registrationsamplecomment", jsonsubsampleCommentstArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationsamplecomment", "IDS_DELETESUBSAMPLECOMMENT");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			return getSubSampleComment(ntransactionsamplecode, userInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}
 
	public JSONArray getTestCommentAudit(String ntransactiontestcode,String stestcommentcode,boolean nflag, UserInfo userInfo) throws Exception {

//		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String sampleNo = "b";
		String concatString="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() ||
			userInfo.getNformcode() == Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms() ||
			userInfo.getNformcode() == Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}	
		if(nflag)
		{
			concatString=" and sar.nstatus = ra.nstatus"
					+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
					+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus ";
		}
		final String getComment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,'ssamplearno',x.ssamplearno,'scomments',x.scomments ,'stestsynonym',x.stestsynonym ,'ntestcommentcode',x.ntestcommentcode)::jsonb) from  "
				+ "(select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as groupingField2,"
				+ "  case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as  sarno,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as  ssamplearno,"
				+ " ra.*,stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,ra.jsondata->>'scomments' as scomments,ra.jsondata->>'stestsynonym' as stestsynonym,"
				
				+ "	   ra.jsondata ->> 'scommentsubtype'               AS scommentsubtype, "
				+ "       ra.jsondata ->> 'spredefinedname'            AS spredefinedname, "
				+ "	          ra.jsondata ->> 'sdescription'            AS sdescription," 
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'nneedreport' as nneedreport,  "
				+ " ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sneedreport"
				+ " from registrationtestcomments ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc,transactionstatus ts"
				+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode  and ts.ntranscode = (coalesce(ra.jsondata->>'nneedreport','4'))::int "
				+ " and rar.npreregno = sar.npreregno "+concatString
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				//+ " sar.nsitecode = stc.nsitecode "
				+ " and sar.nsitecode="+ userInfo.getNtranssitecode()
				+" and stc.nsitecode="+ userInfo.getNmastersitecode()
				+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.ntransactiontestcode in (" + ntransactiontestcode + ") and ra.ntestcommentcode in ("+stestcommentcode+"))x";

		JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getComment, String.class));
		
		return returnJSONArray;
	} 
	public JSONArray getSampleCommentAudit(String npreregno,String ssamplecommentcode,boolean nflag, UserInfo userInfo) throws Exception {

		//final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		//String sampleNo = "b";
		String concatString="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() || userInfo.getNformcode() == Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
		//	sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}	
		if(nflag)
		{
			concatString=" and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
					+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
					+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		final String getComment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,'scomments',x.scomments ,'ssampletestcommentname',x.ssampletestcommentname,'nregcommentcode',x.nregcommentcode )::jsonb) from (select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
				+ " case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as sarno, ra.*,"
				+ " stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'scomments' as scomments"
				+ " from registrationcomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno "
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode "
				+ concatString
				+ " and ra.nsitecode=rar.nsitecode "
				+" and stc.nsitecode="+ userInfo.getNmastersitecode()
				//+ " and rar.nsitecode= stc.nsitecode "
				+ " and rar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ra.npreregno in (" + npreregno + ") and ra.nregcommentcode in ("+ssamplecommentcode+"))x";


		JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getComment, String.class));
		
		return returnJSONArray;
	}
	public JSONArray getSubSampleCommentAudit(String ntransactionsamplecode,String ssamplecommentcode,boolean nflag,  UserInfo userInfo) throws Exception {

//		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String sampleNo = "b";
		String concatString="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}	
		if(nflag)
		{
			concatString= " and rar.npreregno = sar.npreregno and sar.nstatus = ra.nstatus"
					+ " and ra.nstatus = rar.nstatus and u.nstatus = ur.nstatus "
					+ " and ur.nstatus = qf.nstatus and qf.nstatus = stc.nstatus"
					+ " and stc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		final String getComment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,'scomments',x.scomments ,'ssamplearno',x.ssamplearno ,'nsamplecommentcode',x.nsamplecommentcode)::jsonb) from (select case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as groupingField,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as groupingField2,"
				+ " case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo+ ") ' else rar.sarno end as  sarno,"
				+ " case when sar.ssamplearno ='-' then cast(sar.ntransactionsamplecode as character varying)||' (" + sampleNo+ ") ' else sar.ssamplearno end as ssamplearno, ra.*,stc.sdescription as ssampletestcommentname,u.sfirstname||' '||u.slastname susername,ur.suserrolename,ra.jsondata->>'scomments' as scomments,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationsamplecomment ra,registrationarno rar,users u,userrole ur,"
				+ " qualisforms qf,registrationsamplearno sar,sampletestcomments stc"
				+ " where ra.npreregno=rar.npreregno and sar.ntransactionsamplecode=ra.ntransactionsamplecode"
				+ " and ra.nsamplecommentscode=stc.nsampletestcommentscode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode "
				+concatString
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				+" and stc.nsitecode="+ userInfo.getNmastersitecode()
				//+ " sar.nsitecode = stc.nsitecode "
				+ " and sar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ra.ntransactionsamplecode in (" + ntransactionsamplecode + ") and nsamplecommentcode in ("+ssamplecommentcode+"))x";

		JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getComment, String.class));
		
		return returnJSONArray;
	}
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo,  Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		int ncommentsubtypecode = 0;
		if(inputMap.get("ncommentsubtypecode") != null) {
			ncommentsubtypecode = (int) inputMap.get("ncommentsubtypecode");
		}
		final String strQuery ="select ncommentsubtypecode,coalesce(g.jsondata->'scommentsubtype'->>'" + userInfo.getSlanguagetypecode() + "',"
				                + " g.jsondata->'scommentsubtype'->>'en-US') as scommentsubtype,coalesce(g.jsondata->'predefinedenabled','false') as spredefinedenable,ndefaultstatus,nsitecode,nstatus "
				                + " from commentsubtype g where  "//g.ncommentsubtypecode > 0 and
								+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ncommentsubtypecode";
								
		outputMap.put("CommentSubType",   jdbcTemplate.query(strQuery, new CommentSubType()));
		outputMap.put("SelectedCommentSubType",  jdbcTemplate.query(strQuery, new CommentSubType()).get(0));
		List<CommentSubType> lstCommentSubType=(List<CommentSubType>) outputMap.get("CommentSubType");
		outputMap.put("ncommentsubtypecode", ncommentsubtypecode == 0 ? lstCommentSubType.get(0).getNcommentsubtypecode() : ncommentsubtypecode);
		outputMap.putAll((Map<String, Object>) getSampleTestCommentsListById(outputMap,userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	@Override
	public ResponseEntity<Object> getSampleTestCommentsListById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {	
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>(); 
		final String strQuery = " select nsampletestcommentscode, ncommenttypecode, ncommentsubtypecode, spredefinedname, sdescription, nsitecode, nstatus"
				+ " from sampletestcomments where ncommentsubtypecode = "+ inputMap.get("ncommentsubtypecode")+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode =" + userInfo.getNmastersitecode(); 
		outputMap.put("SampleTestComments",   jdbcTemplate.query(strQuery, new SampleTestComments())); 
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}
	
	@Override
	public ResponseEntity<Object> getSampleTestCommentsDescById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {	
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>(); 
		final String strQuery = " select nsampletestcommentscode, ncommenttypecode, ncommentsubtypecode, spredefinedname, sdescription, nsitecode, nstatus"
				+ " from sampletestcomments where ncommentsubtypecode = "+ inputMap.get("ncommentsubtypecode")+ " and spredefinedname = N'"+inputMap.get("spredefinedname")+"'"
						+ " and nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode =" + userInfo.getNmastersitecode(); 
		outputMap.put("SampleTestComments",   jdbcTemplate.query(strQuery, new SampleTestComments())); 
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}
}
