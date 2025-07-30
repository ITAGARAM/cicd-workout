package com.agaramtech.qualis.attachmentscomments.service.attachments;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestAttachment;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class AttachmentDAOImpl implements AttachmentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentDAOImpl.class);
		
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 *
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getSampleAttachment(String npreregno, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String arNo = "b";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			arNo = commonFunction.getMultilingualMessage("IDS_ARNO", userInfo.getSlanguagefilename());
		}

		final String getAttachment = "select ra.*, case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno||' (" + arNo + ") ' end as groupingField,rar.sarno,lm.jsondata->>'slinkname' as slinkname,"
				+ " to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,u.sfirstname||' '||u.slastname susername,ur.suserrolename"
				+ " from registrationattachment ra,registrationarno rar,attachmenttype att,linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and lm.nlinkcode=ra.nlinkcode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode"
				+ " and ra.nstatus=rar.nstatus and rar.nstatus=att.nstatus"
				+ " and att.nstatus=lm.nstatus and lm.nstatus=u.nstatus"
				+ " and u.nstatus=ur.nstatus and ur.nstatus=qf.nstatus" + " and qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ra.npreregno in (" + npreregno+ ")"
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode="+userInfo.getNtranssitecode();;
		List<RegistrationAttachment> listSampleAttachment = jdbcTemplate.query(getAttachment, new RegistrationAttachment());

		returnMap.put("RegistrationAttachment", dateUtilityFunction.getSiteLocalTimeFromUTC(listSampleAttachment,
				Arrays.asList("screateddate"), null, userInfo, false, null,false));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	/**
	 *
	 */
	@Override
	public ResponseEntity<? extends Object> createSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock lockattachment "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");
		final List<RegistrationAttachment> lstSampleAttachment = mapper
				.readValue(request.getParameter("sampleattachment"), new TypeReference<List<RegistrationAttachment>>() {
				});
		final String npreregno = request.getParameter("npreregno");
		
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSampleAttachmenttArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		String stestcommentcode="";
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in ("+npreregno+") and nsitecode =" +userInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		
		if (lstSampleAttachment != null && !lstSampleAttachment.isEmpty()) {
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
			}
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
								
				int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationattachment where stablename = 'registrationattachment' and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";", Integer.class);
				final StringBuilder insertQuery = new StringBuilder();
				final String currenDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "");
				insertQuery.append("INSERT INTO registrationattachment(nregattachmentcode, npreregno,nformcode,nattachmenttypecode,nlinkcode,nusercode,nuserrolecode,jsondata,nsitecode, nstatus) values");
				for(RegistrationAttachment attactment : lstSampleAttachment) {
					seqNo++;
					attactment.setNregattachmentcode(seqNo);
					stestcommentcode+=String.valueOf(seqNo)+",";
					final Map<String,Object> jsonData = attactment.getJsondata();
					jsonData.put("dcreateddate", currenDate);
					jsonData.put("noffsetdcreateddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) );
					jsonData.put("sfilename", (jsonData.get("sfilename").toString().replaceAll("\"", "\\\\\"")));
					jsonData.put("sdescription", (jsonData.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
					attactment.setJsondata(jsonData);
					insertQuery.append("("+attactment.getNregattachmentcode()+","+attactment.getNpreregno()+","+userInfo.getNformcode()+","+attactment.getNattachmenttypecode()+","+attactment.getNlinkcode()
						+ ","+userInfo.getNusercode()+","+userInfo.getNuserrole()+",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(jsonData).toString())
						+ "'," + userInfo.getNtranssitecode() +","
						+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");

				}
				final String updateSeqNoString = "update seqnoregistrationattachment set nsequenceno = nsequenceno+"+lstSampleAttachment.size()+" where stablename = 'registrationattachment'";
				jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add(
						lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_ADDSAMPLEFILE"
								: "IDS_ADDSAMPLELINK");
				final List<Object> listObject = new ArrayList<>();
				listObject.add(lstSampleAttachment);
				//fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
		
				jsonSampleAttachmenttArray =  getSampleAttachmentAudit(npreregno,stestcommentcode.substring(0, stestcommentcode.length()-1),true, userInfo);
				jsonAuditObject.put("registrationattachment", jsonSampleAttachmenttArray);
				auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
				auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
				actionType.put("registrationattachment", lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_ADDSAMPLEFILE"
						: "IDS_ADDSAMPLELINK");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
			return getSampleAttachment(npreregno, userInfo);

		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getEditSampleAttachment(RegistrationAttachment objSampleAttachment, UserInfo userInfo)
			throws Exception {

		final String getAttachment = "select rar.sarno,lm.jsondata->>'slinkname' as slinkname,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,ra.*"
				+ " from registrationattachment ra,registrationarno rar,attachmenttype att,linkmaster lm"
				+ " where att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = ra.npreregno"
				+ " and lm.nlinkcode=ra.nlinkcode "
				+ " and ra.nstatus=att.nstatus and rar.nstatus = lm.nstatus"
				+ " and att.nstatus=lm.nstatus and lm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nregattachmentcode="+ objSampleAttachment.getNregattachmentcode() 
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode ="+ userInfo.getNtranssitecode();
		RegistrationAttachment editObjSampleAttachment = (RegistrationAttachment) jdbcUtilityFunction.queryForObject(getAttachment, RegistrationAttachment.class, jdbcTemplate);
		if (editObjSampleAttachment != null) {

			return new ResponseEntity<>(editObjSampleAttachment, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	/**
	 *
	 */
	@Override
	public ResponseEntity<? extends Object> updateSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonSampleAttachmenttArrayOld = new JSONArray();
		JSONArray jsonSampleAttachmentArraynew = new JSONArray();
		
		ObjectMapper objMapper = new ObjectMapper(); 
		final List<RegistrationAttachment> lstRegistrationAttachment = objMapper
				.readValue(request.getParameter("sampleattachment"), new TypeReference<List<RegistrationAttachment>>() {
				});
		if (lstRegistrationAttachment == null || lstRegistrationAttachment.isEmpty()) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final RegistrationAttachment objRegistrationAttachment = lstRegistrationAttachment.get(0);
		final String npreregno = request.getParameter("npreregno");
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in ("+npreregno+") "
				+ " and nsitecode="+objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		final int isFileEdited = Integer.parseInt(request.getParameter("isFileEdited"));
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus() && objRegistrationAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

			sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);

		}
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		
		}
		final String sQuery = "select * from registrationattachment where nregattachmentcode = "
							+ objRegistrationAttachment.getNregattachmentcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode="+ objUserInfo.getNtranssitecode();
		final RegistrationAttachment validateAtttachment = (RegistrationAttachment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationAttachment.class, jdbcTemplate);
		if (validateAtttachment == null) {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		
		}
		jsonSampleAttachmenttArrayOld =  getSampleAttachmentAudit(npreregno,String.valueOf(objRegistrationAttachment.getNregattachmentcode()),true, objUserInfo) ;
		objRegistrationAttachment.getJsondata().put("sfilename", objRegistrationAttachment.getJsondata().get("sfilename").toString().replaceAll("\"", "\\\\\""));
		objRegistrationAttachment.getJsondata().put("sdescription", objRegistrationAttachment.getJsondata().get("sdescription").toString().replaceAll("\"", "\\\\\""));
		final String sUpdateQuery = "update registrationattachment set nattachmenttypecode = "+ objRegistrationAttachment.getNattachmenttypecode() + ","
				+ " nlinkcode="+ objRegistrationAttachment.getNlinkcode() + ","
				+ " jsondata = jsondata || '"+stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objRegistrationAttachment.getJsondata()).toString())+ "',"
				+ " nformcode = "+objUserInfo.getNformcode()+ ","
				+ " nusercode = "+objUserInfo.getNusercode()+ ","
				+ " nuserrolecode = "+objUserInfo.getNuserrole()
				+ " where nregattachmentcode = " + objRegistrationAttachment.getNregattachmentcode()
				+ " and nsitecode="+objUserInfo.getNtranssitecode();

		jdbcTemplate.execute(sUpdateQuery);
				
		jsonSampleAttachmentArraynew =  getSampleAttachmentAudit(npreregno,String.valueOf(objRegistrationAttachment.getNregattachmentcode()),true, objUserInfo) ;

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<>();
		multilingualIDList
				.add(objRegistrationAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype() ? "IDS_EDITSAMPLEATTACHMENT" : "IDS_EDITSAMPLECOMMENT");
		lstOldObject.add(validateAtttachment);
		//fnInsertAuditAction(lstRegistrationAttachment, 2, lstOldObject, multilingualIDList, objUserInfo);
				
		jsonAuditObjectnew.put("registrationattachment", jsonSampleAttachmentArraynew);
		jsonAuditObjectOld.put("registrationattachment", jsonSampleAttachmenttArrayOld);
		auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
		auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationattachment", objRegistrationAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
				.gettype() ? "IDS_EDITSAMPLEATTACHMENT" : "IDS_EDITSAMPLELINK");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditmap, false, objUserInfo);
				
		return getSampleAttachment(npreregno, objUserInfo);
		
	}

	/**
	 *
	 */
	@Override
	public ResponseEntity<? extends Object> deleteSampleAttachment(RegistrationAttachment objSampleAttachment, String npreregno,
			UserInfo userInfo) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSampleAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in ("+npreregno+") "
				+ " and nsitecode=" +userInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		
		final String sQuery = "select * from registrationattachment where nregattachmentcode = "
				+ objSampleAttachment.getNregattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" +userInfo.getNtranssitecode();
		final RegistrationAttachment validateAtttachment = (RegistrationAttachment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationAttachment.class, jdbcTemplate);
		if (validateAtttachment != null) {
			if (objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				ftpUtilityFunction.deleteFTPFile(Arrays.asList(validateAtttachment.getSsystemfilename()), "master", userInfo);
			} else {
				objSampleAttachment.setScreateddate(null);
			}
			final String sUpdateQuery = "update registrationattachment set nstatus = "
										+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+ " where nregattachmentcode = " + objSampleAttachment.getNregattachmentcode()
										+ " and nsitecode=" +userInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_DELETESAMPLEFILE"
							: "IDS_DELETESAMPLELINK");
			lstObject.add(validateAtttachment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			
			
			jsonSampleAttachmentArray =  getSampleAttachmentAudit(npreregno,String.valueOf(objSampleAttachment.getNregattachmentcode()),false, userInfo) ;
			jsonAuditObject.put("registrationattachment", jsonSampleAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationattachment", objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_DELETESAMPLEFILE"
					: "IDS_DELETESAMPLELINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return getSampleAttachment(npreregno, userInfo);
	}

	@Override
	public ResponseEntity<Object> viewSampleAttachment(RegistrationAttachment objSampleAttachment, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSampleAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in ("+objSampleAttachment.getNpreregno()+") "
				+ " and nsitecode=" + objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		Map<String, Object> map = new HashMap<>();

		String sQuery = "select * from registrationattachment where nregattachmentcode = "
				+ objSampleAttachment.getNregattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + objUserInfo.getNtranssitecode();
		final RegistrationAttachment validateAtttachment = (RegistrationAttachment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationAttachment.class, jdbcTemplate);
		if (validateAtttachment != null) {

			if (validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				map = ftpUtilityFunction.FileViewUsingFtp(validateAtttachment.getJsondata().get("ssystemfilename").toString(), -1, objUserInfo, "", "");
			} else {
//				sQuery = "select slinkname from linkmaster where nlinkcode=" + validateAtttachment.getNlinkcode()
//						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//				LinkMaster objLinkMaster = jdbcTemplate.queryForObject(sQuery, new LinkMaster());
				map.put("AttachLink", objSampleAttachment.getJsondata().get("slinkname") +""+objSampleAttachment.getJsondata().get("sfilename"));
				validateAtttachment.setScreateddate(null);
			}
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_VIEWSAMPLEFILE"
							: "IDS_VIEWSAMPLELINK");
			lstObject.add(validateAtttachment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

			jsonSampleAttachmentArray =  getSampleAttachmentAudit(String.valueOf(objSampleAttachment.getNpreregno()),String.valueOf(objSampleAttachment.getNregattachmentcode()),true, objUserInfo) ;
			jsonAuditObject.put("registrationattachment", jsonSampleAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationattachment", validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_VIEWSAMPLEFILE"
					: "IDS_VIEWSAMPLELINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			return new ResponseEntity<>(map, HttpStatus.OK);

			
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> getTestAttachment(String ntransactiontestcode, UserInfo userInfo) throws Exception {//--

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo="";
		String sampleNo="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() || 
			userInfo.getNformcode() ==Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms() ||
			userInfo.getNformcode() ==Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}

		
				
		final String getAttachment = "select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,ra.jsondata->>'nneedreport' as nneedreport, ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sneedreport,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno,sar.ssamplearno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,(ra.jsondata->>'noffsetdcreateddate') noffsetdcreateddate,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'stestsynonym' as stestsynonym"
				+ " from registrationtestattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf,transactionstatus ts "
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno"
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode and ts.ntranscode = (coalesce(ra.jsondata->>'nneedreport','4'))::int "
				+ " and sar.nstatus = rar.nstatus"
				+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
				+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
				+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
				+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ra.ntransactiontestcode in ("+ ntransactiontestcode + ")"
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				+ " and sar.nsitecode=" +userInfo.getNtranssitecode();
		List<RegistrationTestAttachment> listSampleAttachment = jdbcTemplate.query(getAttachment, new RegistrationTestAttachment());
		returnMap.put("RegistrationTestAttachment", dateUtilityFunction.getSiteLocalTimeFromUTC(listSampleAttachment,Arrays.asList("screateddate"), null, userInfo, false, null,false));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<? extends Object> createTestAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception { 
		jdbcTemplate.execute("lock locktestattachment "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");

		final List<RegistrationTestAttachment> lstTestAttachment = mapper.readValue(
				request.getParameter("testattachment"), new TypeReference<List<RegistrationTestAttachment>>() {
				});
		final String ntransactionTestCode = request.getParameter("ntransactiontestcode");
		
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsontestattactmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		String stestcommentcode="";
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationtest where  "
				+ " ntransactiontestcode in ("+ntransactionTestCode + ")) "
				+ " and nsitecode="+userInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class));
		
		
		if (!lstTestAttachment.isEmpty()) {
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (lstTestAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
			}
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
				
				int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationattachment where stablename = 'registrationtestattachment' and nstatus =1;", Integer.class);
				final StringBuilder insertQuery = new StringBuilder();
				
				final String currenDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "");
				
				insertQuery.append("INSERT INTO registrationtestattachment(ntestattachmentcode,ntransactiontestcode, ntransactionsamplecode, npreregno, nformcode, nattachmenttypecode, nlinkcode,"
						+ " nusercode, nuserrolecode, jsondata, nsitecode, nstatus) VALUES");
				for(RegistrationTestAttachment attactment : lstTestAttachment) {
					seqNo++;
					stestcommentcode+=String.valueOf(seqNo)+",";
					attactment.setNtestattachmentcode(seqNo);
					final Map<String,Object> jsonData = attactment.getJsondata();
					jsonData.put("dcreateddate", currenDate);
					jsonData.put("noffsetdcreateddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
					//jsonData.put("sfilename", (StringEscapeUtils.unescapeJava(jsonData.get("sfilename").toString())));
					jsonData.put("sfilename", (jsonData.get("sfilename").toString().replaceAll("\"", "\\\\\"")));
					jsonData.put("sdescription", (jsonData.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
					//jsonData.put("sdescription", (Jsoup.clean(jsonData.get("sdescription").toString(),Safelist.none())));
					//jsonData.put("sdescription", (StringEscapeUtils.unescapeJava(jsonData.get("sdescription").toString())));

					attactment.setJsondata(jsonData);
					insertQuery.append("("+attactment.getNtestattachmentcode()+","+attactment.getNtransactiontestcode()+","+attactment.getNtransactionsamplecode()+","+attactment.getNpreregno()+","+userInfo.getNformcode()+","+attactment.getNattachmenttypecode()+","+attactment.getNlinkcode()
						+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(jsonData).toString())
						+"'," +userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
				
				}
				final String updateSeqNoString = "update seqnoregistrationattachment set nsequenceno = nsequenceno+"+lstTestAttachment.size()+" where stablename = 'registrationtestattachment'";
				jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add(
						lstTestAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_ADDTESTFILE"
								: "IDS_ADDTESTLINK");
				final List<Object> listObject = new ArrayList<>();
				listObject.add(lstTestAttachment);
				
				
				
				jsontestattactmentArray =  getTestAttachmentAudit(ntransactionTestCode,stestcommentcode.substring(0, stestcommentcode.length()-1),true, userInfo) ;
				jsonAuditObject.put("registrationtestattachment", jsontestattactmentArray);
				auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
				auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
				actionType.put("registrationtestattachment", 
						lstTestAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_ADDTESTFILE"
						: "IDS_ADDTESTLINK");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
				//fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
			return getTestAttachment(ntransactionTestCode, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getEditTestAttachment(RegistrationTestAttachment objTestAttachment, UserInfo objUserInfo) throws Exception {
		String preRegNo="";
		String sampleNo="";
		if (objUserInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", objUserInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", objUserInfo.getSlanguagefilename());
		}
		final String getAttachment = "select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,ra.jsondata->>'nneedreport' as nneedreport,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ objUserInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+objUserInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationtestattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno"
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode"
				+ " and sar.nstatus = rar.nstatus"
				+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
				+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
				+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
				+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.ntestattachmentcode = "+ objTestAttachment.getNtestattachmentcode() 
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode"
				+ " and ra.nsitecode="+objUserInfo.getNtranssitecode();
		RegistrationTestAttachment editObjTestAttachment = (RegistrationTestAttachment) jdbcUtilityFunction.queryForObject(getAttachment,  RegistrationTestAttachment.class, jdbcTemplate);
		if (editObjTestAttachment != null) {

			return new ResponseEntity<>(dateUtilityFunction.getSiteLocalTimeFromUTC(Arrays.asList(editObjTestAttachment),
					Arrays.asList("screateddate"), null, objUserInfo, false, null, false)
					.get(0), HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public ResponseEntity<? extends Object> updateTestAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final List<RegistrationTestAttachment> lstRegistrationAttachment = objMapper.readValue(
				request.getParameter("testattachment"), new TypeReference<List<RegistrationTestAttachment>>() {
				});
		if (lstRegistrationAttachment == null || lstRegistrationAttachment.isEmpty()) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		
		}

		final RegistrationTestAttachment objRegistrationTestAttachment = lstRegistrationAttachment.get(0);
		final String ntransactionTestCode = request.getParameter("ntransactiontestcode");

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonTestAttachmentArrayOld = new JSONArray();
		JSONArray jsonTestAttachmentArraynew = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationtest where  ntransactiontestcode in ("+ntransactionTestCode
				+ "))  and nsitecode= "+objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		final int isFileEdited = Integer.parseInt(request.getParameter("isFileEdited"));
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus() &&
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
			
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
			
		}
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String sQuery = "select * from registrationtestattachment where ntestattachmentcode = "
				+ objRegistrationTestAttachment.getNtestattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();
		final RegistrationTestAttachment validateAtttachment = jdbcTemplate.queryForObject(sQuery, new RegistrationTestAttachment());
		if (validateAtttachment == null) {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		
		}
		jsonTestAttachmentArrayOld =  getTestAttachmentAudit(ntransactionTestCode,String.valueOf(objRegistrationTestAttachment.getNtestattachmentcode()),true, objUserInfo) ;
		objRegistrationTestAttachment.getJsondata().put("sfilename", objRegistrationTestAttachment.getJsondata().get("sfilename").toString().replaceAll("\"", "\\\\\""));
		objRegistrationTestAttachment.getJsondata().put("sdescription", objRegistrationTestAttachment.getJsondata().get("sdescription").toString().replaceAll("\"", "\\\\\""));
		final String sUpdateQuery = "update registrationtestattachment set jsondata = jsondata || '"+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objRegistrationTestAttachment.getJsondata()).toString()) + "'," 
				+ " nattachmenttypecode = "+ objRegistrationTestAttachment.getNattachmenttypecode() + ","
				+ " nlinkcode = "+ objRegistrationTestAttachment.getNlinkcode()+ ","
				+ " nformcode = "+objUserInfo.getNformcode()+ ","
				+ " nusercode = "+objUserInfo.getNusercode()+ ","
				+ " nuserrolecode = "+objUserInfo.getNuserrole()
				+ " where ntestattachmentcode = " + objRegistrationTestAttachment.getNtestattachmentcode()
				+ " and nsitecode= "+ objUserInfo.getNtranssitecode();

		jdbcTemplate.execute(sUpdateQuery);
		final RegistrationTestAttachment validateNewAtttachment = jdbcTemplate.queryForObject(sQuery, new RegistrationTestAttachment());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<>();
		final List<Object> lstNewObject = new ArrayList<>();
		multilingualIDList.add(
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype() ? "IDS_EDITTESTFILE" : "IDS_EDITTESTLINK");
		lstNewObject.add(validateNewAtttachment);
		lstOldObject.add(validateAtttachment);
		//fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
		
		jsonTestAttachmentArraynew =  getTestAttachmentAudit(ntransactionTestCode,String.valueOf(objRegistrationTestAttachment.getNtestattachmentcode()),true, objUserInfo) ;

		jsonAuditObjectnew.put("registrationtestattachment", jsonTestAttachmentArraynew);
		jsonAuditObjectOld.put("registrationtestattachment", jsonTestAttachmentArrayOld);
		auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
		auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationtestattachment", 
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
				.gettype() ? "IDS_EDITTESTFILE" : "IDS_EDITTESTLINK");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditmap, false, objUserInfo);
		
		
		
		
		
		return getTestAttachment(ntransactionTestCode, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteTestAttachment(RegistrationTestAttachment objTestAttachment, String npreregno,
			UserInfo objUserInfo) throws Exception { 
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonTestAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationtest where  ntransactiontestcode in ("+objTestAttachment.getNtransactiontestcode()
				+ "))  "
				+ " and nsitecode=" +objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		final String sQuery = "select * from registrationtestattachment where ntestattachmentcode = "
				+ objTestAttachment.getNtestattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();;
		final RegistrationTestAttachment validateAtttachment = (RegistrationTestAttachment) jdbcUtilityFunction.queryForObject(sQuery,  RegistrationTestAttachment.class, jdbcTemplate);

		if (validateAtttachment != null) {
			if (objTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				ftpUtilityFunction.deleteFTPFile(Arrays.asList(validateAtttachment.getJsondata().get("ssystemfilename").toString()), "", objUserInfo);
			} else {
				objTestAttachment.setScreateddate(null);
			}
			final String sUpdateQuery = "update registrationtestattachment set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntestattachmentcode = "
					+ objTestAttachment.getNtestattachmentcode()
					+ " and nsitecode=" +objUserInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(objTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_DELETETESTFILE"
							: "IDS_DELETETESTLINK");
			lstObject.add(validateAtttachment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			
			
			
			jsonTestAttachmentArray =  getTestAttachmentAudit(String.valueOf(objTestAttachment.getNtransactiontestcode()),String.valueOf(objTestAttachment.getNtestattachmentcode()),false, objUserInfo) ;
			jsonAuditObject.put("registrationtestattachment", jsonTestAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationtestattachment", objTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_DELETETESTFILE"
					: "IDS_DELETETESTLINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			
			
			
			return getTestAttachment(npreregno, objUserInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> viewTestAttachment(RegistrationTestAttachment objTestAttachment, UserInfo objUserInfo)
			throws Exception {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonTestAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+objTestAttachment.getNtransactionsamplecode()
				+ ")) "
				+ " and nsitecode=" +objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		String sQuery = "select * from registrationtestattachment where ntestattachmentcode = "
				+ objTestAttachment.getNtestattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" +objUserInfo.getNtranssitecode();
		final RegistrationTestAttachment validateAtttachment = (RegistrationTestAttachment) jdbcUtilityFunction.queryForObject(sQuery,RegistrationTestAttachment.class,jdbcTemplate);
		if (validateAtttachment != null) {

			if (validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				map = ftpUtilityFunction.FileViewUsingFtp(validateAtttachment.getJsondata().get("ssystemfilename").toString(), -1, objUserInfo, "", "");
			} else {
//				map.put("AttachLink", objTestAttachment.getJsondata().get("slinkname") +""+objTestAttachment.getJsondata().get("sfilename"));
				map.put("AttachLink", validateAtttachment.getJsondata().get("slinkname") +""+validateAtttachment.getJsondata().get("sfilename"));
				validateAtttachment.setScreateddate(null);
			}
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_VIEWTESTFILE"
							: "IDS_VIEWTESTLINK");
			lstObject.add(validateAtttachment);
		
			jsonTestAttachmentArray =  getTestAttachmentAudit(String.valueOf(objTestAttachment.getNtransactiontestcode()),String.valueOf(objTestAttachment.getNtestattachmentcode()),true, objUserInfo) ;
			jsonAuditObject.put("registrationtestattachment", jsonTestAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationtestattachment", validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_VIEWTESTFILE"
					: "IDS_VIEWTESTLINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			return new ResponseEntity<>(map, HttpStatus.OK);


			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSubSampleAttachment(String ntransactionSampleCode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo="";
		String sampleNo="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}

		
				
		final String getAttachment = "select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno,sar.ssamplearno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat()+ "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationsampleattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno"
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode"
				+ " and sar.nstatus = rar.nstatus"
				+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
				+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
				+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
				+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ra.ntransactionsamplecode in ("+ ntransactionSampleCode + ")"
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				+ " and sar.nsitecode=" +userInfo.getNtranssitecode();;
		List<RegistrationSampleAttachment> listSampleAttachment = jdbcTemplate.query(getAttachment, new RegistrationSampleAttachment());

		returnMap.put("RegistrationSampleAttachment", dateUtilityFunction.getSiteLocalTimeFromUTC(listSampleAttachment,Arrays.asList("screateddate"), null, userInfo, false, null,false));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<? extends Object> createSubSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo)throws Exception {
		// TODO Auto-generated method stub

		jdbcTemplate.execute("lock locksampleattachment "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+";");

		final List<RegistrationSampleAttachment> lstSampleAttachment = mapper
				.readValue(request.getParameter("subsampleattachment"), new TypeReference<List<RegistrationSampleAttachment>>() {
				});
		final String ntransactionsamplecode = request.getParameter("ntransactionsamplecode");
	
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		JSONArray jsonSubsampleattachementstArray = new JSONArray(); 
		String ssamplecommentcode="";
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionsamplecode
				+ ")) "
				+ " and nsitecode=" +userInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		
		
		if (lstSampleAttachment != null && !lstSampleAttachment.isEmpty()) {
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
			}
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
								
				int seqNo = jdbcTemplate.queryForObject("select nsequenceno from seqnoregistrationattachment where stablename = 'registrationsampleattachment' and nstatus =1;", Integer.class);
				final StringBuilder insertQuery = new StringBuilder();
				final String currenDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "");
				insertQuery.append("INSERT INTO registrationsampleattachment(nsampleattachmentcode, npreregno,ntransactionsamplecode,nformcode,nattachmenttypecode,nlinkcode,nusercode,nuserrolecode,jsondata,nsitecode, nstatus) values");
				for(RegistrationSampleAttachment attactment : lstSampleAttachment) {
					seqNo++;
					ssamplecommentcode+=String.valueOf(seqNo)+",";
					attactment.setNsampleattachmentcode(seqNo);
					final Map<String,Object> jsonData = attactment.getJsondata();
					jsonData.put("dcreateddate", currenDate);
					jsonData.put("noffsetdcreateddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) );
					jsonData.put("sfilename", (jsonData.get("sfilename").toString().replaceAll("\"", "\\\\\"")));
					jsonData.put("sdescription", (jsonData.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
					attactment.setJsondata(jsonData);
					insertQuery.append("("+attactment.getNsampleattachmentcode()+","+attactment.getNpreregno()+","+attactment.getNtransactionsamplecode()+","+userInfo.getNformcode()+","+attactment.getNattachmenttypecode()+","+attactment.getNlinkcode()
						+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+",'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(jsonData))+"',"
							+userInfo.getNtranssitecode() + ","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");

				}
				final String updateSeqNoString = "update seqnoregistrationattachment set nsequenceno = nsequenceno+"+lstSampleAttachment.size()+" where stablename = 'registrationsampleattachment'";
				jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1)+";"+updateSeqNoString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add(
						lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_ADDSUBSAMPLEFILE"
								: "IDS_ADDSUBSAMPLELINK");
				final List<Object> listObject = new ArrayList<>();
				listObject.add(lstSampleAttachment);
				//fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
				jsonSubsampleattachementstArray =  getSubSampleAttachmentAudit(ntransactionsamplecode,ssamplecommentcode.substring(0, ssamplecommentcode.length()-1),true, userInfo) ;
				jsonAuditObject.put("registrationsampleattachment", jsonSubsampleattachementstArray); 
				auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
				auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
				auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
				actionType.put("registrationsampleattachment", 
						lstSampleAttachment.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_ADDSUBSAMPLEFILE"
						: "IDS_ADDSUBSAMPLELINK");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
			return getSubSampleAttachment(ntransactionsamplecode, userInfo);

		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getEditSubSampleAttachment(RegistrationSampleAttachment objSubSampleAttachment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		String preRegNo="";
		String sampleNo="";
		if (objUserInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", objUserInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", objUserInfo.getSlanguagefilename());
		}
		final String getAttachment = "select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ objUserInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+objUserInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationsampleattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno"
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode"
				+ " and sar.nstatus = rar.nstatus"
				+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
				+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
				+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
				+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nsampleattachmentcode = "+ objSubSampleAttachment.getNsampleattachmentcode() 
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode "
				+ " and sar.nsitecode=" +objUserInfo.getNtranssitecode();;

		RegistrationSampleAttachment editObjTestAttachment = (RegistrationSampleAttachment) jdbcUtilityFunction.queryForObject(getAttachment, RegistrationSampleAttachment.class,jdbcTemplate);
		if (editObjTestAttachment != null) {

			return new ResponseEntity<>(dateUtilityFunction.getSiteLocalTimeFromUTC(Arrays.asList(editObjTestAttachment),
					Arrays.asList("screateddate"), null, objUserInfo, false, null, false)
					.get(0), HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public ResponseEntity<? extends Object> updateSubSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject(); 
		JSONArray jsonsubsampleAttachmentArrayOld = new JSONArray();
		JSONArray jsonsubsampleAttachmentArraynew = new JSONArray();
		

		ObjectMapper objMapper = new ObjectMapper();
		final List<RegistrationSampleAttachment> lstRegistrationAttachment = objMapper.readValue(
				request.getParameter("subsampleattachment"), new TypeReference<List<RegistrationSampleAttachment>>() {
				});
		if (lstRegistrationAttachment == null || lstRegistrationAttachment.isEmpty()) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		
		}

		final RegistrationSampleAttachment objRegistrationTestAttachment = lstRegistrationAttachment.get(0);
		final String ntransactionsamplecode = request.getParameter("ntransactionsamplecode");
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionsamplecode
				+ ")) "
				+ " and nsitecode=" +objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		final int isFileEdited = Integer.parseInt(request.getParameter("isFileEdited"));
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus() &&
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
			
				sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
			
		}
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String sQuery = "select * from registrationsampleattachment where nsampleattachmentcode = "
				+ objRegistrationTestAttachment.getNsampleattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();
		final RegistrationSampleAttachment validateAtttachment = (RegistrationSampleAttachment) jdbcUtilityFunction.queryForObject(sQuery,  RegistrationSampleAttachment.class,jdbcTemplate);
		if (validateAtttachment == null) {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		
		}
		jsonsubsampleAttachmentArrayOld =  getSubSampleAttachmentAudit(ntransactionsamplecode,String.valueOf(objRegistrationTestAttachment.getNsampleattachmentcode()),true, objUserInfo) ;
		objRegistrationTestAttachment.getJsondata().put("sfilename", objRegistrationTestAttachment.getJsondata().get("sfilename").toString().replaceAll("\"", "\\\\\""));
		objRegistrationTestAttachment.getJsondata().put("sdescription", objRegistrationTestAttachment.getJsondata().get("sdescription").toString().replaceAll("\"", "\\\\\""));
		final String sUpdateQuery = "update registrationsampleattachment set jsondata = jsondata || '"+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objRegistrationTestAttachment.getJsondata())) + "'," 
				+ " nattachmenttypecode = "+ objRegistrationTestAttachment.getNattachmenttypecode() + ","
				+ " nlinkcode = "+ objRegistrationTestAttachment.getNlinkcode()+ ","
				+ " nformcode = "+objUserInfo.getNformcode()+ ","
				+ " nusercode = "+objUserInfo.getNusercode()+ ","
				+ " nuserrolecode = "+objUserInfo.getNuserrole()
				+ " where nsampleattachmentcode = " + objRegistrationTestAttachment.getNsampleattachmentcode()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();

		jdbcTemplate.execute(sUpdateQuery);
		final RegistrationSampleAttachment validateNewAtttachment = jdbcTemplate.queryForObject(sQuery, new RegistrationSampleAttachment());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<>();
		final List<Object> lstNewObject = new ArrayList<>();
		multilingualIDList.add(
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype() ? "IDS_EDITSUBSAMPLEATTACHMENT" : "IDS_EDITSUBSAMPLECOMMENT");
		lstNewObject.add(validateNewAtttachment);
		lstOldObject.add(validateAtttachment);
		//fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
		
		
		jsonsubsampleAttachmentArraynew =   getSubSampleAttachmentAudit(ntransactionsamplecode,String.valueOf(objRegistrationTestAttachment.getNsampleattachmentcode()),true, objUserInfo) ;
		jsonAuditObjectnew.put("registrationsampleattachment", jsonsubsampleAttachmentArraynew);
		jsonAuditObjectOld.put("registrationsampleattachment", jsonsubsampleAttachmentArrayOld);
		auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
		auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
		auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
		actionType.put("registrationsampleattachment", 
				objRegistrationTestAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
				.gettype() ? "IDS_EDITSUBSAMPLEATTACHMENT" : "IDS_EDITSUBSAMPLELINK");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, auditmap, false, objUserInfo);
				
		return getSubSampleAttachment(ntransactionsamplecode, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			String ntransactionsamplecode, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSubSampleAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+ntransactionsamplecode
				+ ")) "
				+ " and nsitecode=" +objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		
		final String sQuery = "select * from registrationsampleattachment where nsampleattachmentcode = "
				+ objSampleAttachment.getNsampleattachmentcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();
		final RegistrationSampleAttachment validateAtttachment = (RegistrationSampleAttachment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationSampleAttachment.class,jdbcTemplate);
		if (validateAtttachment != null) {
			if (objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				ftpUtilityFunction.deleteFTPFile(Arrays.asList(validateAtttachment.getJsondata().get("ssystemfilename").toString()), "", objUserInfo);
			} else {
				objSampleAttachment.setScreateddate(null);
			}
			final String sUpdateQuery = "update registrationsampleattachment set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsampleattachmentcode = "
					+ objSampleAttachment.getNsampleattachmentcode()
					+ " and nsitecode="+ objUserInfo.getNtranssitecode();
			jdbcTemplate.execute(sUpdateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_DELETESUBSAMPLEFILE"
							: "IDS_DELETESUBSAMPLELINK");
			lstObject.add(validateAtttachment);
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
		
			jsonSubSampleAttachmentArray =  getSubSampleAttachmentAudit(ntransactionsamplecode,String.valueOf(objSampleAttachment.getNsampleattachmentcode()),false, objUserInfo) ;
			jsonAuditObject.put("registrationsampleattachment", jsonSubSampleAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationsampleattachment", objSampleAttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_DELETESUBSAMPLEFILE"
					: "IDS_DELETESUBSAMPLELINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			
			
			return getSubSampleAttachment(ntransactionsamplecode, objUserInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();

		String sQuery = "select * from registrationsampleattachment where nsampleattachmentcode = "
				+ objSampleAttachment.getNsampleattachmentcode()+ " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+ objUserInfo.getNtranssitecode();
		
		final RegistrationSampleAttachment validateAtttachment = (RegistrationSampleAttachment) jdbcUtilityFunction.queryForObject(sQuery, RegistrationSampleAttachment.class,jdbcTemplate);
		
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject(); 
		JSONArray jsonSubSampleAttachmentArray = new JSONArray();
		ObjectMapper objmap = new ObjectMapper();
		JSONObject jsoncodes=new JSONObject(jdbcTemplate.queryForObject("select  json_build_object('nregtypecode',"
				+ "nregtypecode,'nregsubtypecode',nregsubtypecode,'ndesigntemplatemappingcode',ndesigntemplatemappingcode) "
				+ "from registration where  npreregno in (select  npreregno from registrationsample where  ntransactionsamplecode in ("+objSampleAttachment.getNtransactionsamplecode()
				+ "))  "
				+ " and nsitecode=" +objUserInfo.getNtranssitecode()
				+ " group by nregtypecode,nregsubtypecode,ndesigntemplatemappingcode", String.class)); 
		
		
		if (validateAtttachment != null) {

			if (validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				map = ftpUtilityFunction.FileViewUsingFtp(validateAtttachment.getJsondata().get("ssystemfilename").toString(), -1, objUserInfo, "", "");
			} else {
				map.put("AttachLink", objSampleAttachment.getJsondata().get("slinkname") +""+objSampleAttachment.getJsondata().get("sfilename"));
				validateAtttachment.setScreateddate(null);
			}
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstObject = new ArrayList<>();
			multilingualIDList
					.add(validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
							? "IDS_VIEWSUBSAMPLEFILE"
							: "IDS_VIEWSUBSAMPLELINK");
			lstObject.add(validateAtttachment); 
			jsonSubSampleAttachmentArray =  getSubSampleAttachmentAudit(String.valueOf(objSampleAttachment.getNtransactionsamplecode()),String.valueOf(objSampleAttachment.getNsampleattachmentcode()),true, objUserInfo) ;
			jsonAuditObject.put("registrationsampleattachment", jsonSubSampleAttachmentArray);
			auditmap.put("nregtypecode", jsoncodes.get("nregtypecode"));
			auditmap.put("nregsubtypecode", jsoncodes.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", jsoncodes.get("ndesigntemplatemappingcode")); 
			actionType.put("registrationsampleattachment", validateAtttachment.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_VIEWSUBSAMPLEFILE"
					: "IDS_VIEWSUBSAMPLELINK");
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			return new ResponseEntity<>(map, HttpStatus.OK);			
			//fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	public JSONArray getTestAttachmentAudit(String ntransactiontestcode,String stestattachmentcode,boolean nflag, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo="";
		String sampleNo="";
		String concatString="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() || 
			userInfo.getNformcode() ==Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms() ||
			userInfo.getNformcode() ==Enumeration.QualisForms.MYJOB.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}
		if(nflag)
		{
			concatString=" and sar.nstatus = rar.nstatus"
					+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
					+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
					+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
					+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		
			
		final String getAttachment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,'ssamplearno',x.ssamplearno, "
				+ "	  'stestsynonym',x.stestsynonym,'stypename',x.stypename,'ntestattachmentcode',x.ntestattachmentcode	 )::jsonb)  from (select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,ra.jsondata->>'nneedreport' as nneedreport,  ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sneedreport,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno,sar.ssamplearno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,ra.jsondata->>'stestsynonym' as stestsynonym"
				+ " from registrationtestattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf,transactionstatus ts "
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno and ts.ntranscode = (coalesce(ra.jsondata->>'nneedreport','4'))::int "
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode"
				+concatString
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode and sar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ra.ntransactiontestcode in ("+ ntransactiontestcode + ") and ntestattachmentcode in ("+stestattachmentcode+"))x";
			
	
				JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getAttachment, String.class));
				returnJSONArray.forEach(rjs -> {
				JSONObject jsonObj = (JSONObject) rjs;
				jsonObj.put("sfilename", jsonObj.get("sfilename").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
				jsonObj.put("sdescription", jsonObj.get("sdescription").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
				}
				);
		return returnJSONArray;
	} 
	public JSONArray getSubSampleAttachmentAudit(String ntransactionsamplecode,String ssampleattachmentcode,boolean nflag,  UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo="";
		String sampleNo="";
		String concatString="";

		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			sampleNo = commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename());
		}
		if(nflag)
		{
			concatString= " and sar.nstatus = rar.nstatus"
					+ " and ra.nstatus = rar.nstatus and rar.nstatus = att.nstatus"
					+ " and att.nstatus = lm.nstatus and lm.nstatus = u.nstatus"
					+ " and u.nstatus = ur.nstatus and ur.nstatus = qf.nstatus" 
					+ " and qf.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		}
		
				
		final String getAttachment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,'ssamplearno',x.ssamplearno, "
				+ "	   'stypename',x.stypename,'nsampleattachmentcode',x.nsampleattachmentcode )::jsonb)  from (select ra.*,case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno end as groupingField1,"
				+ " case when ssamplearno = '-' then ra.ntransactionsamplecode || ' ("+sampleNo+") ' else ssamplearno end as groupingField2,"
				+ " rar.sarno,sar.ssamplearno, to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat()+ "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname"
				+ " from registrationsampleattachment ra,registrationarno rar,registrationsamplearno sar,attachmenttype att,"
				+ " linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and rar.npreregno = sar.npreregno"
				+ " and sar.ntransactionsamplecode = ra.ntransactionsamplecode" 
				+ " and lm.nlinkcode = ra.nlinkcode and u.nusercode = ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode = ra.nformcode"
				+concatString
				+ " and ra.nsitecode=rar.nsitecode and rar.nsitecode=sar.nsitecode and sar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ra.ntransactionsamplecode in ("+ ntransactionsamplecode + ") and nsampleattachmentcode in ( "+ssampleattachmentcode+"))x";
		JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getAttachment, String.class));
		returnJSONArray.forEach(rjs -> {
			JSONObject jsonObj = (JSONObject) rjs;
			jsonObj.put("sfilename", jsonObj.get("sfilename").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
			jsonObj.put("sdescription", jsonObj.get("sdescription").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
			}
			);
		
		return returnJSONArray;
	}
	
	
	public JSONArray getSampleAttachmentAudit (String npreregno,String ssamplecommentcode,boolean nflag, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		String preRegNo = "a";
		String arNo = "b";
		String concatString="";
		if (userInfo.getNformcode() == Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()) {
			
			preRegNo = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
			arNo = commonFunction.getMultilingualMessage("IDS_ARNO", userInfo.getSlanguagefilename());
		}
		if(nflag)
		{
			concatString=" and ra.nstatus=rar.nstatus and rar.nstatus=att.nstatus"
					+ " and att.nstatus=lm.nstatus and lm.nstatus=u.nstatus"
					+ " and u.nstatus=ur.nstatus and ur.nstatus=qf.nstatus" + " and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		final String getAttachment = "select json_agg(x.jsondata||json_build_object('sarno',x.sarno,  "
				+ "	   'stypename',x.stypename,'nregattachmentcode',x.nregattachmentcode )::jsonb)  from (select ra.*, case when rar.sarno ='-' then cast(rar.npreregno as character varying)||' (" + preRegNo
				+ ") ' else rar.sarno||' (" + arNo + ") ' end as groupingField,rar.sarno,lm.jsondata->>'slinkname' as slinkname,"
				+ " to_char((ra.jsondata->>'dcreateddate')::timestamp,'"+ userInfo.getSpgdatetimeformat() + "') screateddate,"
				+ " att.jsondata->'sattachmenttype'->>'"+userInfo.getSlanguagetypecode()+"' as stypename,"
				+ " qf.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplayname,u.sfirstname||' '||u.slastname susername,ur.suserrolename"
				+ " from registrationattachment ra,registrationarno rar,attachmenttype att,linkmaster lm,users u,userrole ur,qualisforms qf"
				+ " where ra.npreregno=rar.npreregno and att.nattachmenttypecode=ra.nattachmenttypecode"
				+ " and lm.nlinkcode=ra.nlinkcode and u.nusercode=ra.nusercode"
				+ " and ur.nuserrolecode=ra.nuserrolecode and qf.nformcode=ra.nformcode"
				+ concatString
				+ " and ra.nsitecode=rar.nsitecode and ra.nsitecode="+userInfo.getNtranssitecode()+" "
			//	+ " and rar.nsitecode=sar.nsitecode and sar.nsitecode="+ userInfo.getNtranssitecode()
				+ " and ra.npreregno in (" + npreregno+ ") and nregattachmentcode in ("+ssamplecommentcode+"))x";
		JSONArray returnJSONArray=new JSONArray(jdbcTemplate.queryForObject(getAttachment, String.class));
		returnJSONArray.forEach(rjs -> {
			JSONObject jsonObj = (JSONObject) rjs;
			jsonObj.put("sfilename", jsonObj.get("sfilename").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
			jsonObj.put("sdescription", jsonObj.get("sdescription").toString().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\n", "\\\n").replaceAll("\\\\\"", "\\\""));
			}
			);
		
		return returnJSONArray;
	}
}
