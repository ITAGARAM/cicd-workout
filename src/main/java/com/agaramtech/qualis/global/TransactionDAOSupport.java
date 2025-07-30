package com.agaramtech.qualis.global;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.IntegrationSettings;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.model.TransactionValidation;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testmanagement.model.InterfaceType;
import com.agaramtech.qualis.transactionhistory.service.HistoryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TransactionDAOSupport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDAOSupport.class);
	
	
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final StringUtilityFunction stringUtilityFunction;
	private final AttachmentDAO attachmentDAO;
	private final CommentDAO commentDAO;
	private final HistoryDAO historyDAO;
	
	
	public List<SampleType> getSampleType(final UserInfo userInfo) throws Exception {

		final String strQuery = "select st.nsampletypecode,st.ncategorybasedflowrequired, "
								+ " coalesce(st.jsondata->'sampletypename'->>'" +  userInfo.getSlanguagetypecode() +  "'," 
								+ " st.jsondata->'sampletypename'->>'en-US') as ssampletypename,st.nsorter, "
								+ " st.nprojectspecrequired, st.nportalrequired, st.noutsourcerequired,"
								+ " st.ncategorybasedflowrequired "
								+ " from SampleType st,registrationtype rt,registrationsubtype rst,"
								+ " approvalconfig ac,approvalconfigversion acv "
								+ " where st.nsampletypecode > 0 and "
								+ " st.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and st.napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and st.nsampletypecode=rt.nsampletypecode " 
								+ " and rt.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rt.nregtypecode=rst.nregtypecode "
								+ " and rst.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rst.nregsubtypecode=ac.nregsubtypecode " 
								+ " and ac.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and acv.napprovalconfigcode=ac.napprovalconfigcode " 
								+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and acv.ntransactionstatus= " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " and st.nsitecode= " + userInfo.getNmastersitecode()
								+ " and rt.nsitecode= " + userInfo.getNmastersitecode()
								+ " and rst.nsitecode =" + userInfo.getNmastersitecode()
								+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
								+ " group by st.nsampletypecode,st.nsorter order by st.nsorter;";

		return jdbcTemplate.query(strQuery, new SampleType());
	}

	
	

	public Map<String, Object> getTransactionStatus(final UserInfo userInfo) throws Exception {
		
		final Map<String, Object> statusMap = new HashMap<String, Object>();
		final Map<String, Object> outMap = new HashMap<String, Object>();
		
		final String strQuery = "select tv.ntransactionvalidationcode,tv.nformcode,tv.ncontrolcode,"
								+ " tv.ntransactionstatus,"
								+ "coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',stransstatus) stransdisplaystatus "
								+ ",nregtypecode,nregsubtypecode from transactionvalidation tv,"
								+ " transactionstatus t where t.ntranscode=tv.ntransactionstatus "
								+ " and t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tv.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tv.nsitecode = "+ userInfo.getNmastersitecode() + ";";
		
		final List<TransactionValidation> lstCM = (List<TransactionValidation>) jdbcTemplate.query(strQuery,new TransactionValidation());
		
		Set<Short> lst = lstCM.stream().map(source -> source.getNcontrolcode()).collect(Collectors.toSet());
		
		lst.stream().forEach(objStatus -> {	List<TransactionValidation> lstControlMaster = lstCM.stream()
						.filter(source -> source.getNcontrolcode() == objStatus).collect(Collectors.toList());
						statusMap.put(String.valueOf(objStatus), lstControlMaster);
						});
		
		return outMap;
	}
	
	public List<RegistrationType> getRegistrationType(final int nsampleTypeCode, final UserInfo userInfo)
			throws Exception {
	
		String strQuery = "select * from sampletype where nsampletypecode =" + nsampleTypeCode
						 + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						 + " and nsitecode = "+ userInfo.getNmastersitecode();
		
		final SampleType sampleType = (SampleType) jdbcUtilityFunction.queryForObject(strQuery, SampleType.class, jdbcTemplate);
		
		String validationQuery = "";
	
		if( sampleType.getNtransfiltertypecode()!= -1 &&  userInfo.getNusercode()!= -1 )
		{
			int nmappingFieldCode = (sampleType.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();	
			
			validationQuery = " and rst.nregsubtypecode in( "
							+ "		SELECT rs.nregsubtypecode "
							+ "		FROM registrationsubtype rs "
							+ "		INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
							+ "		LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
							+ "		WHERE ( ttc.nneedalluser = 3  and ttc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" AND ttc.nmappingfieldcode = "+nmappingFieldCode+ ") "
							+ "	 OR "
							+ "	( ttc.nneedalluser = 4  "
							+ "	  AND ttc.nmappingfieldcode ="+nmappingFieldCode
							+ "	  AND tu.nusercode ="+userInfo.getNusercode()
							+ "   and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "   and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") "
				            + "  OR "
				            + "	( ttc.nneedalluser = 4  "
				            + " AND ttc.nmappingfieldcode = -1 "
				            + " AND tu.nusercode ="+userInfo.getNusercode() 
				            + " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + ") "
							+ "	 AND rs.nregtypecode = rt.nregtypecode) "	;
		}
		
		strQuery = "select  rt.nregtypecode,"
								+ " coalesce(rt.jsondata->'sregtypename'->>'" + userInfo.getSlanguagetypecode()
								+ "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,rt.nsorter "
								+ " from SampleType st,registrationtype rt,registrationsubtype rst,"
								+ " approvalconfig ac,approvalconfigversion acv "
								+ " where st.nsampletypecode > 0 and st.nsampletypecode =" + nsampleTypeCode 
								+ " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and st.napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and st.nsampletypecode=rt.nsampletypecode "
								+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rt.nregtypecode=rst.nregtypecode "
								+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rst.nregsubtypecode=ac.nregsubtypecode  "
								+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and acv.napprovalconfigcode=ac.napprovalconfigcode "
								+ " and acv.nstatus = " +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and acv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " and st.nsitecode = " + userInfo.getNmastersitecode()
								+ " and rt.nsitecode = " + userInfo.getNmastersitecode()
								+ " and rst.nsitecode = " + userInfo.getNmastersitecode()
								+ " and acv.nsitecode= " + userInfo.getNmastersitecode()
								+ " and rt.nregtypecode > 0 "
								+ validationQuery
								+ " group by rt.nregtypecode,"
								+ " coalesce(rt.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()
								+ "',rt.jsondata->'sregtypename'->>'en-US'),rt.nsorter order by rt.nregtypecode desc";

		return jdbcTemplate.query(strQuery, new RegistrationType());
	}
	
	public List<RegistrationSubType> getRegistrationSubType(final int nregTypeCode, final UserInfo userInfo)
			throws Exception {

		String strQuery = "select * from registrationtype rt, sampletype st "
							+ " where rt.nsampletypecode=st.nsampletypecode and rt.nregTypeCode="+nregTypeCode
							+ " and rt.nsitecode = " + userInfo.getNmastersitecode()
							+ " and st.nsitecode = " + userInfo.getNmastersitecode()
							+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final SampleType sampleType = (SampleType) jdbcUtilityFunction.queryForObject(strQuery, SampleType.class, jdbcTemplate);
				
		String validationQuery="";
		
		if(sampleType.getNtransfiltertypecode()!= -1 && userInfo.getNusercode()!= -1 ){
			
			int nmappingFieldCode = (sampleType.getNtransfiltertypecode() == 1) ? userInfo.getNdeptcode() : userInfo.getNuserrole();	
			
			validationQuery=" and rst.nregsubtypecode in( "
							+ " SELECT rs.nregsubtypecode "
							+ " FROM registrationsubtype rs "
							+ " INNER JOIN transactionfiltertypeconfig ttc ON rs.nregsubtypecode = ttc.nregsubtypecode "
							+ " LEFT JOIN transactionusers tu ON tu.ntransfiltertypeconfigcode = ttc.ntransfiltertypeconfigcode "
							+ " WHERE ( ttc.nneedalluser = 3  and ttc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" AND ttc.nmappingfieldcode = "+nmappingFieldCode+ ")"
							+ "  OR "
							+ "	( ttc.nneedalluser = 4  "
							+ " AND ttc.nmappingfieldcode ="+nmappingFieldCode 
							+ " AND tu.nusercode ="+userInfo.getNusercode() 
							+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") "
							+ "  OR "
							+ "	( ttc.nneedalluser = 4  "
							+ " AND tu.nusercode ="+userInfo.getNusercode() 
							+ " AND ttc.nmappingfieldcode = -1 "
							+ " and ttc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tu.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") "
							+ " AND rs.nregtypecode = "+nregTypeCode+")";
		}
		
		strQuery = "select max(rsc.nregsubtypeversioncode) nregsubtypeversioncode,"
							+ " max(rsc.jsondata->>'nneedsubsample' )::Boolean nneedsubsample,"
							+ " max(rsc.jsondata->>'nneedjoballocation' )::Boolean nneedjoballocation,"
							+ " max(rsc.jsondata->>'nneedmyjob' )::Boolean nneedmyjob,"
							+ " max(rsc.jsondata->>'nneedtestinitiate')::Boolean nneedtestinitiate,"
							+ " max(rsc.jsondata->>'nneedtemplatebasedflow' )::Boolean nneedtemplatebasedflow,rst.nregsubtypecode,"
							+ " coalesce(rst.jsondata->'sregsubtypename'->>'" + userInfo.getSlanguagetypecode()
							+ "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rst.nsorter, "
							//ALPD-5512--Vignesh(04-03-2025)--Sample registration screen -> Default spec is showing in Pre-registered tab. ( Lucid )
							//start
							+" case when  max((rsc.jsondata ->> 'ntestgroupspecrequired' )):: Boolean is Null then true else  max(("
							+"   rsc.jsondata ->> 'ntestgroupspecrequired')"
							+"):: Boolean end as ntestgroupspecrequired	from SampleType st,registrationtype rt,registrationsubtype rst,approvalconfig ac,approvalconfigversion acv,regsubtypeconfigversion rsc "
							//end
							+ "where st.nsampletypecode > 0 and st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.napprovalconfigview = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
							+ "and st.nsampletypecode=rt.nsampletypecode and rt.nregtypecode = " + nregTypeCode
							+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.nregtypecode=rst.nregtypecode "
							+ " and rst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rst.nregsubtypecode=ac.nregsubtypecode "
							+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and acv.napprovalconfigcode=ac.napprovalconfigcode "
							+ " and acv.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and acv.ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ "  and rst.nregsubtypecode>0 "
							+ " and rsc.napprovalconfigcode=ac.napprovalconfigcode "
							+ " and rsc.ntransactionstatus= " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " and st.nsitecode = " + userInfo.getNmastersitecode()
							+ " and rt.nsitecode = " + userInfo.getNmastersitecode()
							+ " and rst.nsitecode= " + userInfo.getNmastersitecode()
							+ " and acv.nsitecode= " + userInfo.getNmastersitecode()
							+ " and rsc.nsitecode=" + userInfo.getNmastersitecode()
							+ validationQuery
							+ " group by rst.nregsubtypecode,coalesce(rst.jsondata->'sregsubtypename'->>'"
							+ userInfo.getSlanguagetypecode()
							+ "',rst.jsondata->'sregsubtypename'->>'en-US'),rst.nsorter order by rst.nregsubtypecode desc";
		return jdbcTemplate.query(strQuery, new RegistrationSubType());
	}
	
	public List<TransactionStatus> getFilterStatus(final int nregTypeCode, final int nregSubTypeCode,
			final UserInfo userInfo) throws Exception {
		// ALPD-3541
		final String filterStatus = "select a.* from (select t.ntranscode as ntransactionstatus,t.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus"
									+ "  from approvalroleactiondetail ard,approvalconfig ac,transactionstatus t "
									+ " where ard.napprovalconfigcode=ac.napprovalconfigcode and ard.ntransactionstatus=t.ntranscode "
									+ " and ard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ac.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and t.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ard.nsitecode= " + userInfo.getNmastersitecode()
									+ " and ac.nsitecode= " + userInfo.getNmastersitecode()
									+ " and ac.nregtypecode=" + nregTypeCode
									+ " and ac.nregsubtypecode=" + nregSubTypeCode 
									+ " union all "
									+ " select t.ntranscode as ntransactionstatus ,t.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "' "
									+ " stransdisplaystatus from approvalstatusconfig ascg,transactionstatus t "
									+ " where t.ntranscode=ascg.ntranscode and ascg.nformcode=" + userInfo.getNformcode()
									+ " and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and ascg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ascg.nsitecode=" + userInfo.getNmastersitecode()
									+ " and ascg.nregtypecode=" + nregTypeCode + " and ascg.nregsubtypecode=" + nregSubTypeCode + ")a "
									+ " group by a.ntransactionstatus,a.stransdisplaystatus order by a.ntransactionstatus  ";
	
		return jdbcTemplate.query(filterStatus, new TransactionStatus());
	}
	
	public List<ApprovalConfigAutoapproval> getApprovalConfigVersion(int nregTypeCode, int nregSubTypeCode,
			UserInfo userInfo) throws Exception {

		final String getApprovalConfigVersion = "select aca.sversionname,aca.napprovalconfigcode,"
												+ " aca.napprovalconfigversioncode ,acv.ntransactionstatus,acv.ntreeversiontempcode,acv.napproveconfversioncode "
												+ " from   approvalconfigautoapproval aca, approvalconfig ac,approvalconfigversion acv "
												+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode "
												+ "  and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode "
												+ " and ac.nregtypecode =" + nregTypeCode + " and ac.nregsubtypecode =" + nregSubTypeCode
												+ " and acv.ntransactionstatus not in ( " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
												+ ") and acv.nsitecode =" + userInfo.getNmastersitecode() + " and ac.nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and acv.nsitecode=" + userInfo.getNmastersitecode() 
												+ " and aca.nsitecode=" + userInfo.getNmastersitecode()
												+ " and ac.nsitecode=" + userInfo.getNmastersitecode()
												+ " group by acv.ntransactionstatus,aca.napprovalconfigversioncode,"
												+ "acv.ntreeversiontempcode,aca.napprovalconfigcode,aca.sversionname,"
												+ " acv.napproveconfversioncode order by aca.napprovalconfigversioncode desc";

		final List<ApprovalConfigAutoapproval> approvalVersion = jdbcTemplate.query(getApprovalConfigVersion,
				new ApprovalConfigAutoapproval());
		return approvalVersion;
	}
	
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode, final int nregSubTypeCode,
			final int napproveConfigVersionCode) throws Exception {
		
		final String strQuery = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,"
							+ " '(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
							+ " reactregistrationtemplate rt,approvalconfigversion acv  where  "
							+ " acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode  and  "
							+ " rt.nreactregtemplatecode=dm.nreactregtemplatecode "
							+ " and dm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and dm.nsitecode=" + userInfo.getNmastersitecode() 
//							+ " and rt.nsitecode=" + userInfo.getNmastersitecode()
//							+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
							+ " and  acv.napproveconfversioncode="+ napproveConfigVersionCode;

		final List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(strQuery,
				new ReactRegistrationTemplate());

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}
	
	public ResponseEntity<Object> getRegistrationSubSampleTemplate(int ndesigntemplatemappingcode) throws Exception {
		
		final String strQuery = "select dm.ndesigntemplatemappingcode,rt.jsondata from designtemplatemapping dm, "
								+ " reactregistrationtemplate rt where  rt.nreactregtemplatecode=nsubsampletemplatecode"
								+ " and dm.ndesigntemplatemappingcode=" + ndesigntemplatemappingcode 
							//	+ " and nsubsampletemplatecode <> "+Enumeration.TransactionStatus.NA.gettransactionstatus()
								+" and  rt.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction.queryForObject(
				strQuery, ReactRegistrationTemplate.class, jdbcTemplate);
		
		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}
	
//	public Map<String, Object> getTemplateDesign(final int ndesignTemplateCode, final int nformCode) throws Exception {
//		
//		final String strQuery = "select jsondata->'" + nformCode + "' as jsondata from mappedtemplatefieldprops"
//								+ " where ndesigntemplatemappingcode=" + ndesignTemplateCode;
//
//		final Map<String, Object> map = jdbcTemplate.queryForMap(strQuery);
//		return map;
//
//	}
//	
	//@Override
	public ResponseEntity<Object> getRegistrationTemplate(int nregTypeCode, int nregSubTypeCode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata, rt.ndefaulttemplatecode from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregTypeCode + " and nregsubtypecode="
				+ nregSubTypeCode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  dm.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate)  (ReactRegistrationTemplate)jdbcUtilityFunction.queryForObject(
				str, ReactRegistrationTemplate.class,jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}
	
	
	public ResponseEntity<Object> getRegistrationTemplatebasedontemplatecode(int nregTypeCode, int nregSubTypeCode,
			int ndesignTemplateCode) throws Exception {
		// TODO Auto-generated method stub

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregTypeCode + " and nregsubtypecode="
				+ nregSubTypeCode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.ndesigntemplatemappingcode="
				+ ndesignTemplateCode;

		final ReactRegistrationTemplate lstReactRegistrationTemplate = (ReactRegistrationTemplate)jdbcUtilityFunction.queryForObject(
				str, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}
	
	public ResponseEntity<Object> getRegistrationTemplateList(int nregTypeCode, int nregSubTypeCode,
			boolean needTemplateBasedFlow) throws Exception {
		// TODO Auto-generated method stub
		final String stranstatus = needTemplateBasedFlow
				? Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
				: String.valueOf(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

		final String str = "select dm.ndesigntemplatemappingcode,rt.jsondata,CONCAT(rt.sregtemplatename,"
				+ " '(',cast(dm.nversionno as character varying),')') sregtemplatename from designtemplatemapping dm, "
				+ " reactregistrationtemplate rt where nregtypecode=" + nregTypeCode + " and nregsubtypecode="
				+ nregSubTypeCode + " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.ntransactionstatus in" + " ("
				+ stranstatus + ") order by dm.ntransactionstatus desc";

		final List<ReactRegistrationTemplate> lstReactRegistrationTemplate = jdbcTemplate.query(str,
				new ReactRegistrationTemplate());

		return new ResponseEntity<>(lstReactRegistrationTemplate, HttpStatus.OK);

	}
	
	public List<TestGroupTest> getTestByExpiredMethod(final String testGroupTestCode, final UserInfo userInfo)
			throws Exception {
		List<TestGroupTest> expiredMethodTestList = new ArrayList<TestGroupTest>();
		
		if (testGroupTestCode != null && testGroupTestCode.trim().length() > 0) {
			final String queryString = " select m1.nmethodcode, m1.smethodname, tgt.ntestcode,"
					+ " tm.stestsynonym,tm.stestname from testgrouptest tgt, method m1, testmaster tm "
					+ " where tgt.ntestgrouptestcode in (" + testGroupTestCode + ") "
					+ " and tgt.nmethodcode=m1.nmethodcode and tgt.ntestcode=tm.ntestcode "
					+ " and tgt.nmethodcode in (( select a.nmethodcode from ( "
					+ " select  m.nmethodcode from method m,methodvalidity md WHERE "
					+ " 	m.nmethodcode = md.nmethodcode AND m.nneedvalidity= "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and md.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and m.nsitecode= "+ userInfo.getNmastersitecode() 
					+ " and md.nsitecode = "+ userInfo.getNmastersitecode() 
					+ " and md.ntransactionstatus = "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() 
					+ " union "
					+ " select  m.nmethodcode from method m,methodvalidity md WHERE "
					+ " 	m.nmethodcode = md.nmethodcode AND m.nneedvalidity="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and md.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and md.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() 
					+ " AND md.dvalidityenddate < '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' )a )) "
					+ " and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and m1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgt.nsitecode= "+ userInfo.getNmastersitecode() 
					+ " and m1.nsitecode = "+ userInfo.getNmastersitecode()
					+ " and tm.nsitecode = "+ userInfo.getNmastersitecode();

			expiredMethodTestList = jdbcTemplate.query(queryString, new TestGroupTest());

		}
		return expiredMethodTestList;
	}

	public ResponseEntity<Object> getOutsourceDetails(String npreregno, String ntransactionsamplecode,
			UserInfo userInfo) {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		
		final String strOutsourceDetails = "select osd.noutsourcedetailcode, osd.npreregno, osd.ntransactionsamplecode, osd.ntransactiontestcode, osd.bflag, osd.ssampleid, "
											+ "case when osd.sremarks = 'null' then '-' else osd.sremarks end sremarks, case when osd.sshipmenttracking='null' then '-' else osd.sshipmenttracking end sshipmenttracking"
											+ ", osd.nsitecode, TO_CHAR(osd.doutsourcedate ,'" + userInfo.getSpgdatetimeformat()
											+ "') soutsourcedate, ran.sarno sarno, rsan.ssamplearno ssamplearno, rt.jsondata->>'stestsynonym' stestsynonym, "
											+ " s.ssitename ssourcesitename, rt.jsondata->>'stestname' stestname from outsourcedetail osd,"
											+ " registrationarno ran,registrationsamplearno rsan, registrationtest rt, "
											+ " site s, externalordersample eos where osd.npreregno in ("
											+ npreregno + ") and " + "osd.ntransactionsamplecode in (" + ntransactionsamplecode
											+ ") and osd.npreregno=ran.npreregno and osd.ntransactionsamplecode=rsan.ntransactionsamplecode "
											+ "and osd.ntransactiontestcode=rt.ntransactiontestcode and osd.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ran.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rsan.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus= "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus ="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eos.nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and osd.nsitecode=" +userInfo.getNtranssitecode() 
											+ " and ran.nsitecode ="+userInfo.getNtranssitecode() 
											+ " and rsan.nsitecode ="+userInfo.getNtranssitecode() 
											+ " and rt.nsitecode ="+userInfo.getNtranssitecode() 
											+ " and eos.nsitecode="+userInfo.getNtranssitecode() 
											+ " and s.nsitecode ="+userInfo.getNmastersitecode() 
											+ " and osd.npreregno= eos.npreregno "
											+ " and osd.ntransactionsamplecode=eos.ntransactionsamplecode";
		final List<Map<String, Object>> lstOutsourceDetails = jdbcTemplate.queryForList(strOutsourceDetails);
		objMap.put("OutsourceDetailsList", lstOutsourceDetails);
		
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	
	public Map<String, Object> seqNoSampleInsert(Map<String, Object> inputMap) throws Exception {

		final String sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		// ALPD-2047
//		final List<String> lst = Arrays.asList("registrationsamplehistory",
//				"registrationtesthistory","registrationhistory");
//		seqNoMaxUpdate(lst);

		final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration "
									// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
									+ " where stablename in ('registrationhistory','registrationsamplehistory','registrationtesthistory')"
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final List<SeqNoRegistration> lstSeqNoSampleRecord = jdbcTemplate.query(strSelectSeqno,
				new SeqNoRegistration());

		Map<String, Object> returnMap = null;
		returnMap = lstSeqNoSampleRecord.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				seqNoRegistration -> seqNoRegistration.getNsequenceno()));

		ObjectMapper objmap = new ObjectMapper();

		UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int ncontrolcode = (int) inputMap.get("ncontrolcode");
		String strNpreregno = (String) inputMap.get("npreregno");
		String[] preregnoArray = strNpreregno.split(",");

		int nregTypeCode = (int) inputMap.get("nregtypecode");
		int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

		List<String> newList = Arrays.asList(preregnoArray).stream().map(s -> s)
				.collect(Collectors.toList());

		// Updated by L.Subashini on 28/04/2022 to add regtype and regsubtype in
		// transactionvalidation
		String transValidation = "select ts.ntranscode,ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "'  stransdisplaystatus from "
								+ " transactionvalidation tv,transactionstatus ts where tv.ntransactionstatus=ts.ntranscode "
								+ " and tv.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and tv.nformcode=" + userInfo.getNformcode() + " and tv.nregtypecode=" + nregTypeCode
								+ " and tv.nregsubtypecode=" + nregSubTypeCode 
								+ " and tv.nsitecode=" + userInfo.getNmastersitecode()
								+ " and tv.ncontrolcode in(" + ncontrolcode + ")" + " ";

		String strRegistration = "select rth.ntransactionstatus from registrationhistory rth "
								+ " where  rth.nsitecode = " + userInfo.getNtranssitecode()
								+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth.nreghistorycode in (select max(nreghistorycode) from registrationhistory rth1  "
								+ " where npreregno in(" + strNpreregno + ")  "
								+ " and rth1.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rth1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by npreregno )";
		List<TransactionStatus> listTransactionStatus = jdbcTemplate.query(strRegistration, new TransactionStatus());
		List<TransactionStatus> cancelValidation = jdbcTemplate.query(transValidation, new TransactionStatus());

		List<Short> child = cancelValidation.stream().map(obj -> obj.getNtranscode()).collect(Collectors.toList());
		listTransactionStatus.removeIf(person -> child.contains((short) person.getNtransactionstatus()));
		if (listTransactionStatus.size() == 0) {

			// Updated below query by L.Subashini on 28/04/2022 to add regtype and
			// regsubtype in transactionvalidation

			final String sqlSample = "select npreregno from registrationhistory rh,transactionvalidation tv where"
								+ " rh.nreghistorycode=any(select max(rh.nreghistorycode) from registrationhistory rh where"
								+ " rh.npreregno in(" + strNpreregno + ")  and rh.nsitecode=" + userInfo.getNtranssitecode()
								+ " and rh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by rh.npreregno) " + " and rh.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rh.ntransactionstatus in(" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ "" + "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and tv.ntransactionstatus=rh.ntransactionstatus and tv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.nregtypecode = "
								+ nregTypeCode + " and tv.nregsubtypecode=" + nregSubTypeCode + " and rh.nsitecode="
								+ userInfo.getNtranssitecode() + " and tv.nsitecode=" + userInfo.getNmastersitecode()
								+ " group by npreregno";
			final List<RegistrationHistory> lstSeqNoSample = jdbcTemplate.query(sqlSample, new RegistrationHistory());

			final List<String> strListOfPreregno = lstSeqNoSample.stream().map(obj -> String.valueOf(obj.getNpreregno()))
					.collect(Collectors.toList());

			final List<String> newList1 = strListOfPreregno.stream().map(s -> s)
					.collect(Collectors.toList());

			newList.removeAll(newList1);
			int lstSeqNoSampleUpdate = newList.size();
			//String InsertPreregno = Joiner.on(",").join(newList);
			final String insertPreregno = String.join(",", newList);

			// Updated below query by L.Subashini on 28/04/2022 to add regtype and
			// regsubtype in transactionvalidation
			String sqlSubsample = "select npreregno,ntransactionsamplecode from registrationsamplehistory rsh,"
								+ " transactionvalidation tv "
								+ " where rsh.nsamplehistorycode=any(select max(rsh.nsamplehistorycode) from "
								+ " registrationsamplehistory rsh where rsh.npreregno in(" + strNpreregno + ") "
								+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rsh.nsitecode=" + userInfo.getNtranssitecode()
								+ " group by rsh.npreregno,rsh.ntransactionsamplecode) and rsh.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and	rsh.ntransactionstatus not in("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and tv.ntransactionstatus=rsh.ntransactionstatus and tv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.nregtypecode = "
								+ nregTypeCode + " and tv.nregsubtypecode=" + nregSubTypeCode + " and rsh.nsitecode="
								+ userInfo.getNtranssitecode() + " and tv.nsitecode=" + userInfo.getNmastersitecode()
								+ " group by npreregno,ntransactionsamplecode";

			final List<RegistrationSampleHistory> lstSeqNoSubSample = jdbcTemplate.query(sqlSubsample,
					new RegistrationSampleHistory());
			int lstSeqNoSubSampleUpdate = lstSeqNoSubSample.size();

			// Updated below query by L.Subashini on 28/04/2022 to add regtype and
			// regsubtype in transactionvalidation
			final String sqlTest = "select npreregno,ntransactionsamplecode,ntransactiontestcode from registrationtesthistory rth,transactionvalidation tv where rth.ntesthistorycode = any ("
								+ " select max(rth.ntesthistorycode) from registrationtesthistory rth where rth.npreregno in ("
								+ strNpreregno + ") and rth.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode="
								+ userInfo.getNtranssitecode()
								+ " group by rth.npreregno, rth.ntransactionsamplecode, rth.ntransactiontestcode) "
								+ " and rth.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and rth.ntransactionstatus not in ( "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " ,"
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ ") and tv.ntransactionstatus=rth.ntransactionstatus and tv.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.nregtypecode = "
								+ nregTypeCode + " and tv.nregsubtypecode=" + nregSubTypeCode + " and rth.nsitecode="
								+ userInfo.getNtranssitecode() + " and tv.nsitecode=" + userInfo.getNmastersitecode()
								+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode";

			final List<RegistrationTestHistory> lstSeqNoTestSample = jdbcTemplate.query(sqlTest,
					new RegistrationTestHistory());
			int lstSeqNoTestUpdate = lstSeqNoTestSample.size();

			final String StrUpdateSample = "update seqnoregistration set nsequenceno="
									+ ((int) returnMap.get("registrationhistory") + lstSeqNoSampleUpdate)
									+ " where stablename='registrationhistory';" + "update seqnoregistration set nsequenceno="
									+ ((int) returnMap.get("registrationsamplehistory") + lstSeqNoSubSampleUpdate)
									+ " where stablename='registrationsamplehistory';" + "update seqnoregistration set nsequenceno="
									+ ((int) returnMap.get("registrationtesthistory") + lstSeqNoTestUpdate)
									+ " where stablename='registrationtesthistory';";

			jdbcTemplate.execute(StrUpdateSample);

			returnMap.put("insertpreregno", insertPreregno);
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		} else {

//			ResourceBundle resourcebundle = new PropertyResourceBundle(
//					new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
//							Enumeration.Path.PROPERTIES_FILE.getPath() + userInfo.getSlanguagefilename() + ".properties"), "UTF-8"));
//			
			
			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);
			
			List<String> listString = new ArrayList<String>();
			for (int i = 0; i < cancelValidation.size(); i++) {
				listString.add(cancelValidation.get(i).getStransdisplaystatus());
			}
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					resourcebundle.getString("IDS_SELECT") + " " + String.join("/", listString) + " "
							+ resourcebundle.getString("IDS_SAMPLESTOCANCELREJECT"));
		}

		return returnMap;
	}
	
	public List<Map<String, Object>> testAuditGet(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// For Audit Test Get Start
		final String testquery2 = "  select json_agg(a.jsonuidata) from (select jrt.jsondata||jrs.jsonuidata||json_build_object('sarno',"
							+ "  case when jra.sarno='-' then concat(cast(jra.npreregno as text),' (Reg No)') "
							+ "  else jra.sarno end)::jsonb||json_build_object('stransdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "')::jsonb||"
							+ "  json_build_object('ssamplearno',case when jsa.ssamplearno='-' then concat(cast(jsa.ntransactionsamplecode as text),"
							+ " ' (Sample No)') else jsa.ssamplearno end)::jsonb||json_build_object('ntransactionstatus',jrth.ntransactionstatus)::jsonb||"
							+ " json_build_object('scolorhexcode',cm.scolorhexcode)::jsonb as jsonuidata"
							+ " from registrationtesthistory jrth, registrationtest jrt, registrationsamplearno jsa,"
							+ " registrationsample jrs, registrationarno jra, registration jr , "
							+ " transactionstatus ts,formwisestatuscolor fsc,colormaster cm "
							+ " where  jrth.ntransactiontestcode=jrt.ntransactiontestcode "
							+ " and jra.npreregno = jrt.npreregno  and jrt.npreregno=jr.npreregno and jrth.npreregno=jr.npreregno"
							+ " and jsa.ntransactionsamplecode = jrt.ntransactionsamplecode  and "
							+ " jrth.nsitecode=jrt.nsitecode and jrt.nsitecode=jsa.nsitecode and jsa.nsitecode=jrs.nsitecode "
							+ " and jrs.nsitecode = jra.nsitecode and jra.nsitecode=jr.nsitecode and jr.nsitecode="
							+ userInfo.getNtranssitecode()
							+ " and jrth.ntesthistorycode in ( select max(rth1.ntesthistorycode) from registrationtesthistory rth1"
							+ " where rth1.npreregno in (" + inputMap.get("npreregno") + ") and rth1.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth1.nsitecode="
							+ userInfo.getNtranssitecode()
							+ " group by rth1.npreregno,rth1.ntransactionsamplecode,rth1.ntransactiontestcode)"
							+ " and jsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jra.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jrt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and jrs.ntransactionsamplecode=jrt.ntransactionsamplecode  and fsc.ntranscode = jrth.ntransactionstatus "
							+ " and fsc.ncolorcode = cm.ncolorcode and fsc.nformcode = " + userInfo.getNformcode()
							+ " and fsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and jrth.ntransactionstatus = ts.ntranscode and ts.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and jrs.npreregno in ("
							+ inputMap.get("npreregno") + ") "// + stransactiontestcode
							+ "  order by jrs.npreregno,jrt.ntransactiontestcode asc)a";
	
		final String lstDatatests = jdbcTemplate.queryForObject(testquery2, String.class);
		List<Map<String, Object>> lstDataTest = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
				lstDatatests, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
		// For Audit Test Get end
		return lstDataTest;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<Object> getRegistrationTestAudit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();

		boolean updateAnalyst =false;
		String query ="select ssettingvalue from settings where nsettingcode ="
						+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);	
		if(objSettings !=null) {
			updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;
		}
		
		final String query1 = "select * from fn_registrationtestget('" + inputMap.get("npreregno") + "'::text," + "'"
							+ inputMap.get("ntransactionsamplecode") + "'::text" + ",'" + inputMap.get("ntransactiontestcode")
							+ "'::text," + "" + inputMap.get("ntype") + "," + userInfo.getNtranssitecode() + ",'"
							+ userInfo.getSlanguagetypecode() + "','"
							+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
							+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+updateAnalyst+")";
		LOGGER.info("fn_registrationtestget:" + query1);

		final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
		if (lstData2 != null) {
			// ObjectMapper obj = new ObjectMapper();
			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					lstData2, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
			objMap.put("RegistrationGetTest", lstData);
		} else {
			objMap.put("selectedTest", Arrays.asList());
			objMap.put("RegistrationParameter", Arrays.asList());
			objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			objMap.put("RegistrationGetTest", Arrays.asList());

		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}
	
	public void insertChainCustody(int nchainCustodyCode, final String sPreRegNo, final boolean needSubSample,
			final UserInfo userInfo) throws Exception {

		String insertCustodyQuery = "";
		if (needSubSample) {
			insertCustodyQuery = "insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,"
								+ "stablename,sitemno,ntransactionstatus,nusercode,nuserrolecode, "
								+ "dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
								+ " (select (" + nchainCustodyCode
								+ ") + rank() over(order by rar.npreregno, rsar.ntransactionsamplecode) nchaincustodycode," + " "
								+ Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() + " as nformcode,"
								+ "rsar.ntransactionsamplecode as ntablepkno,'ntransactionsamplecode' as stablepkcolumnname,"
								+ "'registrationsamplearno' as stablename,rsar.ssamplearno as sitemno,"
								+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " ntransactionstatus ,"
								+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + "'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate, " + userInfo.getNtimezonecode()
								+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " as noffsetdtransactiondate, " + "'"
								+ commonFunction.getMultilingualMessage(Enumeration.TransAction.SAMPLEREGISTERED.getTransAction(),
										userInfo.getSlanguagefilename())
								+ "' as sremarks," + "" + userInfo.getNtranssitecode() + " as nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
								+ " from registrationarno rar, registrationsamplearno rsar,registrationsamplehistory rth1 "
								+ " where rar.npreregno = rsar.npreregno "
								+ " and rsar.ntransactionsamplecode=rth1.ntransactionsamplecode "
								+ " and rar.nsitecode=rsar.nsitecode and rar.nsitecode="+ userInfo.getNtranssitecode() 
								+ " and rth1.nsitecode="+ userInfo.getNtranssitecode() 
								+ " and rar.nstatus=rsar.nstatus and rar.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rth1.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rar.npreregno in ("
								+ sPreRegNo + ") and rth1.ntransactionstatus not in("
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
								+ " and rth1.nsamplehistorycode in "
								+ " ( select max(nsamplehistorycode) from registrationsamplehistory where npreregno in(" + sPreRegNo
								+ ") and nsitecode="+ userInfo.getNtranssitecode() 
								+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by ntransactionsamplecode)) ";

		} else {
			insertCustodyQuery = "insert into chaincustody (nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname, "
								+ "stablename,sitemno,ntransactionstatus,nusercode,nuserrolecode, "
								+ "dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus) "
								+ "(select (" + nchainCustodyCode + ") + rank() over(order by ra.npreregno) nchaincustodycode,"
								+ " " + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() + " as nformcode,"
								+ " ra.npreregno as ntablepkno,'npreregno' as stablepkcolumnname, 'registrationarno' as stablename,ra.sarno as sitemno,"
								+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + " ntransactionstatus ,"
								+ userInfo.getNusercode() + " as nusercode," + userInfo.getNuserrole() + " as nuserrolecode," + "' "
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate, " + userInfo.getNtimezonecode()
								+ " as ntztransactiondate," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " as noffsetdtransactiondate, " + " '"
								+ commonFunction.getMultilingualMessage(Enumeration.TransAction.SAMPLEREGISTERED.getTransAction(),
										userInfo.getSlanguagefilename())
								+ "' as sremarks," + userInfo.getNtranssitecode() + " as nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus "
								+ "	from registrationarno ra,registrationhistory rth where ra.npreregno =rth.npreregno and  ra.nsitecode= "
								+ userInfo.getNtranssitecode() + " and ra.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.npreregno in (" + sPreRegNo
								+ ") and rth.ntransactionstatus not in("
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
								+ " and rth.nreghistorycode  in (select max(nreghistorycode) as nreghistorycode from registrationhistory "
								+ " where npreregno  in ("+ sPreRegNo + ") "
								+ " and nsitecode="+ userInfo.getNtranssitecode() 
								+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by npreregno))";
		}
		jdbcTemplate.execute(insertCustodyQuery);
	}

	public void createIntegrationRecord(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final String spreregno = (String) inputMap.get("sintegrationpreregno");
		final Integer status = (Integer) inputMap.get("svalidationstatus"); //Added by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		final boolean needSubSample = (boolean) inputMap.get("nneedsubsample");
		String testQuery = "";
		if(inputMap.containsKey("sintegrationtest"))		{
			testQuery = "and ntransactiontestcode in ("+inputMap.get("sintegrationtest")+")" ;
		}
		//	LOGGER.info("Date3-IG-1:"+ new Date());
		
		// ALPD-5298	Added code by Vishakh to handle Result Entry re-insert throws 500 error issue - START
		final String strConcatValueSdmsLabSheetMaster = " from registrationtesthistory rth,registration r, registrationarno ra, registrationsample rs,registrationsamplearno rsa,"+
														 " registrationtest rt,testmaster tm" +
														 " where rth.npreregno = r.npreregno and rth.ntesthistorycode =" +
														 " any (select max(ntesthistorycode) from registrationtesthistory where npreregno in ("+spreregno + ") "+testQuery+" "+
														 " and nsitecode=" + userInfo.getNtranssitecode() +
														 " group by npreregno, ntransactionsamplecode, ntransactiontestcode)" +
														 " and rs.ntransactionsamplecode = rth.ntransactionsamplecode and rt.ntransactiontestcode = rth.ntransactiontestcode"+
														 " and rsa.ntransactionsamplecode = rs.ntransactionsamplecode "+
														 " and r.npreregno = ra.npreregno  and r.npreregno = rs.npreregno and r.npreregno = rt.npreregno"+
														 " and rs.ntransactionsamplecode = rt.ntransactionsamplecode  and r.npreregno = rt.npreregno"+
														 " and rt.ntestcode  = tm.ntestcode "  +
														 " and r.nsitecode=ra.nsitecode and ra.nsitecode=rs.nsitecode and rs.nsitecode=rt.nsitecode"+
														 " and rt.nsitecode=" + userInfo.getNtranssitecode() +
														 " and r.nstatus = rs.nstatus and rs.nstatus = rt.nstatus and rt.nstatus =  ra.nstatus"+
														 " and ra.nstatus = rth.nstatus and rth.nstatus = tm.nstatus and tm.nstatus = "+
														 Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" +
														 " and rth.ntransactionstatus = " +status+
														 " and r.npreregno in (" + spreregno +") and tm.ninterfacetypecode > 0 ";
		
		final String strCheckSdmsLabSheetMaster = " select rt.ntransactiontestcode "+ strConcatValueSdmsLabSheetMaster;
	
		if (Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() != userInfo.getNformcode())
		{
			//	LOGGER.info("Date3-IG-2:"+ new Date());
		
			final String strDeleteSdmsLabSheetMaster = "delete from sdmslabsheetmaster where ntransactiontestcode in ("+ strCheckSdmsLabSheetMaster+") and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(strDeleteSdmsLabSheetMaster);
//			LOGGER.info("Date3-IG-3:"+ new Date());
		
		}
			//Modified the insert query by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
		   //JIRA: ALPD-4989-Modified by sathish for the uuid for repeat test
	
			final	String sdmsLabSheetMaster =
					"insert into sdmslabsheetmaster (ntransactiontestcode,npreregno,sarno,ntransactionsamplecode,ntestgrouptestcode,ntestcode,"+
							" nretestno,ntestrepeatcount,ninterfacetypecode,sllinterstatus,nusercode,nuserrolecode,suuid,nbatchmastercode,nsitecode,nstatus) "+

					 " select rt.ntransactiontestcode,r.npreregno,ra.sarno, "+
					 " rs.ntransactionsamplecode,rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno nretestno, rt.ntestrepeatno ntestrepeatcount,"+
					 " tm.ninterfacetypecode,'A' sllinterstatus," + userInfo.getNusercode() + " nusercode," +userInfo.getNuserrole() + " nuserrolecode,"+
					 " case when (ninterfacetypecode = "+Enumeration.InterFaceType.INTERFACER.getInterFaceType()
					 +" OR ninterfacetypecode = "+Enumeration.InterFaceType.SDMS.getInterFaceType()+") then "+
					 " case when "+needSubSample+"= false then ra.sarno else rsa.ssamplearno end "+
					 " else CONCAT(r.npreregno,'-',rs.ntransactionsamplecode,'-',rt.ntestcode,'-',rt.ntestrepeatno,'-',rt.ntestretestno,'-',r.nsitecode) end as suuid,"+
					 " -1 nbatchmastercode,r.nsitecode,1 nstatus" + strConcatValueSdmsLabSheetMaster;


			jdbcTemplate.execute(sdmsLabSheetMaster);
		
			//	LOGGER.info("Date3-IG-4:"+ new Date());
			//Modified the insert query by sonia on 06th Sep 2024 for JIRA ID:ALPD-4769
			//JIRA: ALPD-4989-Modified by sathish for the uuid for repeat test
			
			String strConcatValueSdmsLabSheetDetails = " from registrationtesthistory rth,registration r,registrationarno ra, registrationsample rs, "+
													 " registrationtest rt, registrationparameter rp,testgrouptest tgt,testgrouptestparameter tgtp, "+
													 " testmaster tm, testparameter tp,registrationsamplearno rsa " +
													 " where rth.npreregno = r.npreregno and rsa.npreregno = r.npreregno and rsa.ntransactionsamplecode = rs.ntransactionsamplecode"+
													 " and rth.ntesthistorycode =" +
													 " any (select max(ntesthistorycode) from registrationtesthistory where npreregno in ("+spreregno + ") "+testQuery+" "+
													 " and nsitecode=" + userInfo.getNtranssitecode() +
													 " group by npreregno, ntransactionsamplecode, ntransactiontestcode)" +
													 " and rs.ntransactionsamplecode = rth.ntransactionsamplecode and rt.ntransactiontestcode = rth.ntransactiontestcode"+
													 " and r.npreregno = ra.npreregno  and r.npreregno = rs.npreregno and r.npreregno = rt.npreregno"+
													 " and rs.ntransactionsamplecode = rt.ntransactionsamplecode  and r.npreregno = rt.npreregno"+
													 " and rt.ntransactiontestcode = rp.ntransactiontestcode  and r.npreregno = rp.npreregno"+
													 " and tgt.ntestgrouptestcode = rt.ntestgrouptestcode and tgt.ntestcode = rt.ntestcode"+
													 " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and tm.ntestcode = tgt.ntestcode"+
													 " and tm.ntestcode = tp.ntestcode and tp.ntestparametercode = tgtp.ntestparametercode"+
													 " and r.nsitecode=ra.nsitecode and ra.nsitecode=rs.nsitecode and rs.nsitecode=rt.nsitecode"+
													 " and rt.nsitecode=rp.nsitecode and rp.nsitecode=" +userInfo.getNtranssitecode() +
													 " and r.nstatus = rs.nstatus and rs.nstatus = rt.nstatus and rt.nstatus =  rp.nstatus"+
													 " and rp.nstatus = tgt.nstatus "+
													 " and tp.nstatus = ra.nstatus  and ra.nstatus = rth.nstatus and rth.nstatus = tgt.nstatus "+
													 " and tgt.nstatus = tgtp.nstatus and tgtp.nstatus = tp.nstatus and tp.nstatus = tm.nstatus "+
													 " and tm.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
													 " and rth.ntransactionstatus = " +status + " " +
													 " and r.npreregno in (" + spreregno +") and tm.ninterfacetypecode > 0 ";
											
			final String strCheckSdmsLabSheetDetails = " select rp.ntransactionresultcode "+ strConcatValueSdmsLabSheetDetails;
			
			if (Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms() != userInfo.getNformcode())
			{
				//	LOGGER.info("Date3-IG-5:"+ new Date());
				final String strDeleteSdmsLabSheetDetails = "delete from sdmslabsheetdetails where ntransactionresultcode in ("+ strCheckSdmsLabSheetDetails+") and nstatus="
																	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(strDeleteSdmsLabSheetDetails);
				//	LOGGER.info("Date3-IG-6:"+ new Date());		
			}
		
			final String sdmsLabSheetDetails = "insert into sdmslabsheetdetails (ntransactionresultcode,npreregno,sarno,"
										+ " ntransactionsamplecode,ntransactiontestcode,"
										+ " ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,"
										+ " ntestgrouptestparametercode,ntestparametercode,nparametertypecode,"
										+ " nroundingdigits,sresult,sllinterstatus,sfileid,nlinkcode,"
										+ " nattachedlink,suuid,nbatchmastercode,nsitecode,nstatus)"
										+ " select rp.ntransactionresultcode ntransactionresultcode,r.npreregno,ra.sarno,"
										+ " rs.ntransactionsamplecode ntransactionsamplecode,rt.ntransactiontestcode, "
										+ " rt.ntestgrouptestcode,rt.ntestcode,"
										+ " rt.ntestretestno nretestno, rt.ntestrepeatno ntestrepeatcount, rp.ntestgrouptestparametercode,"
										+ " rp.ntestparametercode, rp.nparametertypecode,(rp.jsondata->>'nroundingdigits')::int nroundingdigits, "
										+ " NULL sresult,'A' sllinterstatus, NULL sfileid,-1 nlinkcode,-1 nattachedlink,"
										+ " case when (ninterfacetypecode = "+Enumeration.InterFaceType.INTERFACER.getInterFaceType()
										+ " OR ninterfacetypecode = "+Enumeration.InterFaceType.SDMS.getInterFaceType()+") then "
										+ " case when "+needSubSample+"= false then ra.sarno else rsa.ssamplearno end "
										+ " else CONCAT(r.npreregno,'-',rs.ntransactionsamplecode,'-',rt.ntestcode,'-',"
										+ " rt.ntestrepeatno,'-',rt.ntestretestno,'-',r.nsitecode) end as suuid,"
										+ " -1 nbatchmastercode,r.nsitecode," 
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus" 
										+ strConcatValueSdmsLabSheetDetails;


			jdbcTemplate.execute(sdmsLabSheetDetails);
		    //	LOGGER.info("Date3-IG-7:"+ new Date());
			
			// ALPD-5298	Added code by Vishakh to handle Result Entry re-insert throws 500 error issue - END
			final int nsettingvalue = jdbcTemplate.queryForObject("select settings.ssettingvalue::int from settings"
										+ " where nsettingcode = "+Enumeration.Settings.INTEGRATION_SERVICE.getNsettingcode()
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";", Integer.class);
			
			//	LOGGER.info("Date3-IG-8:"+ new Date());
			
			if(nsettingvalue == Enumeration.TransactionStatus.YES.gettransactionstatus())
			{
				final String getStrQery = "select tm.ninterfacetypecode, STRING_AGG(DISTINCT rt.npreregno::text, ',') AS spreregno "
						+ " from registrationtest rt, testmaster tm where "
						+ " rt.ntestcode = tm.ntestcode and rt.npreregno in ("+spreregno+") and rt.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " group by tm.ninterfacetypecode ORDER BY tm.ninterfacetypecode;";

				final List<InterfaceType> lstInterfacertypewithPreregno =  jdbcTemplate.query(getStrQery, new InterfaceType());

				if(!lstInterfacertypewithPreregno.isEmpty())
				{
					final Map<Short, String> mapInterfaceValues = lstInterfacertypewithPreregno.stream().collect(
							Collectors.toMap(InterfaceType::getNinterfacetypecode, InterfaceType::getSpreregno));
					
					mapInterfaceValues.forEach((interfacetypecode, preregno) -> {

						switch(interfacetypecode) {

						case 1:

							break;
						case 2:

							try {
								integrationCallService(inputMap,interfacetypecode,preregno,needSubSample,userInfo);
							} catch (Exception e) {
								e.printStackTrace();
							}

							break;
						case 3:



							break;
						default :

							break;
						}
					});
				}
			//}

		}
		//	LOGGER.info("Date3-IG-9:"+ new Date());

	}

//	ALPD-6064	Added deleteSdmsLabSheetRecords method by Vishakh (08-07-2025)
	public void deleteSdmsLabSheetRecords(final String stransactionTestCode, UserInfo userInfo) throws Exception {
		String strDelete = "delete from sdmslabsheetmaster where ntransactiontestcode in ("+ stransactionTestCode
				+ ") and nsitecode="+ userInfo.getNtranssitecode()+ " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";
		strDelete += "delete from sdmslabsheetdetails where ntransactiontestcode in ("+ stransactionTestCode
				+ ") and nsitecode="+ userInfo.getNtranssitecode()+ " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";
		jdbcTemplate.execute(strDelete);
	}
	
	public void integrationCallService(final Map<String,Object> inputMap,final short interfacetypecode, final String preregno,final boolean needSubSample,final UserInfo userInfo) throws Exception
	{

		final String dataQuery = "select a.jsondata || jsontestdata from (SELECT max(ifs.jsonlimsmappingfield->>'default')::jsonb ||"
									+ "COALESCE(jsonb_object_agg(a_keys.a_key, b_keys.b_value) FILTER (WHERE a_keys.a_key IS NOT NULL AND b_keys.b_value IS NOT NULL),'{}'::jsonb) "
									+ "|| COALESCE(jsonb_object_agg(c_keys.c_key,d_keys.d_value) FILTER (WHERE c_keys.c_key IS NOT NULL AND d_keys.d_value IS NOT NULL),'{}'::jsonb) "
									//+ "|| jsonb_object_agg('limsreferencecode',CASE WHEN "+needSubSample+"= false THEN ra.sarno ELSE rsa.ssamplearno END) "
											+ " AS jsondata,"
											+ " CASE WHEN ifs.jsonlimsmappingfield ? 'test' THEN COALESCE(jsonb_build_object(e_keys.e_key, STRING_AGG(DISTINCT f_keys.f_value, ', ')),'{}'::jsonb) "
											+ " ELSE '{}'::jsonb END AS jsontestdata "
									+ " FROM "
									+ " registration r "
									+ "JOIN registrationsample rs ON r.npreregno = rs.npreregno and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN registrationarno ra ON r.npreregno = ra.npreregno and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN registrationsamplearno rsa ON rs.npreregno = rsa.npreregno and rsa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN registrationtest rt ON rsa.npreregno = rt.npreregno and  rs.ntransactionsamplecode = rt.ntransactionsamplecode "
									+ "and rsa.ntransactionsamplecode = rt.ntransactionsamplecode and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN testmaster tm ON rt.ntestcode = tm.ntestcode and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN testintegrationfieldmapping tfm ON tm.ntestcode = tfm.ntestcode and tfm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "JOIN integrationfieldsettings ifs ON tfm.nintegrationfieldsettingcode = ifs.nintegrationfieldsettingcode and ifs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ "LEFT JOIN LATERAL jsonb_each_text(ifs.jsonlimsmappingfield->'sample') AS a_keys(a_key, a_value) ON TRUE "
									+ "LEFT JOIN LATERAL jsonb_each_text(r.jsonuidata|| jsonb_build_object('sarno',ra.sarno)::jsonb|| jsonb_build_object('ssamplearno',rsa.ssamplearno)::jsonb) AS b_keys(b_key, b_value) ON a_keys.a_value = b_keys.b_key "
									+ "LEFT JOIN LATERAL jsonb_each_text(ifs.jsonlimsmappingfield->'subsample') AS c_keys(c_key,c_value) ON TRUE "
									+ "LEFT JOIN LATERAL jsonb_each_text(rs.jsonuidata|| jsonb_build_object('sarno',ra.sarno)::jsonb|| jsonb_build_object('ssamplearno',rsa.ssamplearno)::jsonb) AS d_keys(d_key, d_value) ON c_keys.c_value = d_keys.d_key "
									+ "LEFT JOIN lateral jsonb_each_text(ifs.jsonlimsmappingfield->'test') AS e_keys(e_key,e_value) ON true "
									+ "LEFT JOIN lateral jsonb_each_text(rt.jsondata) AS f_keys(f_key, f_value) ON e_keys.e_value = f_keys.f_key "
									+ "WHERE r.npreregno IN ("+preregno+") "
									+ "GROUP BY r.npreregno,rs.ntransactionsamplecode, tfm.nintegrationfieldsettingcode,ifs.jsonlimsmappingfield,e_keys.e_key) a ";

		final List<Map<String,Object>> interfacerMap = jdbcTemplate.queryForList(dataQuery);
		String url = "";
		final String slinkname = "slinkname";
		final String sclassurlname = "sclassurlname";
		final String methodname = "smethodname";
		ObjectMapper objMapper = new ObjectMapper();


		if(!interfacerMap.isEmpty())
		{
			final List<IntegrationSettings> lstintegration = objMapper.convertValue(inputMap.get("integrationsettings"), new TypeReference<List<IntegrationSettings>>() {
			});

			if(interfacetypecode == Enumeration.InterFaceType.INTERFACER.getInterFaceType())
			{

				final Map<String, Object> mapIntegration = objMapper.convertValue(lstintegration.get(2),new TypeReference<Map<String, Object>>() {
				});
				url = mapIntegration.get(slinkname)+"/"+mapIntegration.get(sclassurlname)+"/"+mapIntegration.get(methodname);
				interfacerRequestCall(url,interfacerMap,preregno,userInfo);
			}
			if(interfacetypecode == Enumeration.InterFaceType.ELN.getInterFaceType())
			{
				final Map<String, Object> mapIntegration = objMapper.convertValue(lstintegration.get(3),new TypeReference<Map<String, Object>>() {
				});
				url = mapIntegration.get(slinkname)+"/"+mapIntegration.get(sclassurlname)+"/"+mapIntegration.get(methodname);
				interfacerRequestCall(url,interfacerMap,preregno,userInfo);
			}
			if(interfacetypecode == Enumeration.InterFaceType.SDMS.getInterFaceType())
			{
				final Map<String, Object> mapIntegration = objMapper.convertValue(lstintegration.get(3),new TypeReference<Map<String, Object>>() {
				});
				url = mapIntegration.get(slinkname)+"/"+mapIntegration.get(sclassurlname)+"/"+mapIntegration.get(methodname);
				interfacerRequestCall(url,interfacerMap,preregno,userInfo);
			}
		}
	}
	
	public void interfacerRequestCall(final String url,final List<Map<String,Object>> interfacerMap,final String preregno,final UserInfo userInfo) throws Exception
	{
//		HttpPost request = new HttpPost(url);
//
//		ObjectMapper objectMapper = new ObjectMapper();
//		String jsonString = objectMapper.writeValueAsString(interfacerMap);
//		LOGGER.info("Interfacer order record--> "+jsonString);
//		LOGGER.info("Interfacer order record1--> "+jsonString.replace("\\\"", "\""));
//		MultipartEntityBuilder builder = MultipartEntityBuilder.create().addTextBody("sampleorder", jsonString,ContentType.TEXT_PLAIN);
//		HttpEntity entity = builder.build();
//		request.setEntity(entity);
//		HttpClient client = HttpClientBuilder.create().build();
//		HttpResponse response = client.execute(request);
//		int statusCode = response.getStatusLine().getStatusCode();
//
//		if(statusCode == 200) {
//			
//			final String updateIntegration = "Update sdmslabsheetmaster set sllinterstatus = 'I' where npreregno in ("+preregno+") and nstatus = 1;"
//					+ " Update sdmslabsheetdetails set sllinterstatus = 'I' where npreregno in ("+preregno+") and nstatus = 1; ";
//			
//			jdbcTemplate.execute(updateIntegration);			
//			LOGGER.info("Interfacer record sent succesfully");
//		} else {
//			LOGGER.info(statusCode+" Error in sending Interfacer record");
//		}

	}

	public String scheduleSkip() throws Exception{
		return (String) jdbcUtilityFunction.queryForObject("select ssettingvalue from settings where "
				+ " nsettingcode = " +Enumeration.Settings.ALLOT_JOB_OPTIONAL.getNsettingcode()
				+ " and nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), 
				 String.class, jdbcTemplate);
	}
	
	public String validateTestAutoComplete(List<String> listSample, List<TestGroupTest> listTest,
			final UserInfo userInfo) throws Exception {
		final String sQuery = "select case when tgtp.nparametertypecode="
				+ Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then tgtnp.sresultvalue else tgtcp.scharname end sresultvalue,"
				+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode from registrationtest rt "
				+ " inner join testgrouptest tgt on tgt.ntestgrouptestcode = rt.ntestgrouptestcode"
				+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
				+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where rt.ntransactionsamplecode in (" + String.join(",", listSample) + ")"
				+ " and tgt.ntestgrouptestcode in (" + stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ")"
				+ " and tgtp.nparametertypecode in(" + Enumeration.ParameterType.NUMERIC.getparametertype() + ","
				+ Enumeration.ParameterType.CHARACTER.getparametertype() + ") "
				+ " and case when tgtp.nparametertypecode = " + Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then tgtnp.sresultvalue else tgtcp.scharname end is null and tgtp.nresultmandatory = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";
		// @SuppressWarnings("unchecked")
		List<TestGroupTestNumericParameter> validateList = jdbcTemplate.query(sQuery,
				new TestGroupTestNumericParameter());
		if (validateList.isEmpty()) {
			return Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} else {
			return "IDS_DEFAULTRESULTVALUEISNOTAVAILABLE";
		}
	}
	
	public Map<String, Object> getActiveSampleTabData(String npreregno, String ntransactionSampleCode,
			String activeSampleTab, UserInfo userInfo) throws Exception {

		switch (activeSampleTab) {
		case "IDS_SAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();
		case "IDS_SAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSampleComment(npreregno, userInfo).getBody();
		default:
			return (Map<String, Object>) attachmentDAO.getSampleAttachment(npreregno, userInfo).getBody();

		}
		
	}
	

	public Map<String, Object> getActiveSubSampleTabData(String ntransactionSampleCode, String activeSampleTab,
			UserInfo userInfo) throws Exception {

		switch (activeSampleTab) {
		case "IDS_SUBSAMPLEATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getSubSampleAttachment(ntransactionSampleCode, userInfo)
					.getBody();
		case "IDS_SUBSAMPLECOMMENTS":
			return (Map<String, Object>) commentDAO.getSubSampleComment(ntransactionSampleCode, userInfo).getBody();
		default:
			return (Map<String, Object>) commentDAO.getSubSampleComment(ntransactionSampleCode, userInfo).getBody();

		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getActiveTestTabData(String ntransactionTestCode, String npreregno,
			String activeTabName, UserInfo userInfo) throws Exception {

		switch (activeTabName) {

		case "IDS_TESTATTACHMENTS":
			return (Map<String, Object>) attachmentDAO.getTestAttachment(ntransactionTestCode, userInfo).getBody();
		case "IDS_PARAMETERRESULTS":
			return (Map<String, Object>) getRegistrationParameter(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTCOMMENTS":
			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();
		case "IDS_TESTHISTORY":
			return (Map<String, Object>) historyDAO.getTestHistory(ntransactionTestCode, userInfo).getBody();
		default:
			return (Map<String, Object>) commentDAO.getTestComment(ntransactionTestCode, userInfo).getBody();

		}
	}
	
	public ResponseEntity<Object> getRegistrationParameter(final String ntransactionTestCode, UserInfo userInfo)
			throws Exception {

		final String query = "select * from fn_registrationparameterget ('" + ntransactionTestCode + "','"
								+ userInfo.getSsitedatetime() + "'," + userInfo.getNtranssitecode() + "::integer," + "'"
								+ userInfo.getSlanguagetypecode() + "'," + "'"
								+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "')";
		LOGGER.info("parameter query:" + query);
		List<?> lstSp = jdbcTemplate.queryForList(query);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("RegistrationParameter", lstSp);

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}
	
	public List<TransactionValidation> getTransactionValidation(int ncontrolcode, UserInfo userInfo, int nregTypeCode,
			int nregSubTypeCode) throws Exception {

		final String sTransValidation = " select tv.ntransactionstatus,coalesce (ts.jsondata->'salertdisplaystatus'->>" + "'"
										+ userInfo.getSlanguagetypecode()
										+ "',ts.jsondata->'salertdisplaystatus'->>'en-US') as stransdisplaystatus"
										+ " from transactionvalidation tv,transactionstatus ts "
										+ " where tv.nformcode="+ userInfo.getNformcode()
										+ " and ts.ntranscode=tv.ntransactionstatus and tv.nregtypecode="
										+ nregTypeCode + " and tv.nregsubtypecode=" + nregSubTypeCode + " and tv.ncontrolcode="
										+ ncontrolcode + " and tv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
										+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
										+ " and tv.nsitecode=" + userInfo.getNmastersitecode() + "";
		final List<TransactionValidation> lstTranscancelValidation = jdbcTemplate.query(sTransValidation,
				new TransactionValidation());
		return lstTranscancelValidation;

	}	
	
	public Map<String,Object> isValidDate(Map<String, Object> map,final String dateString,final String field, UserInfo userInfo) {

		Map<String,Object> outmap = new LinkedHashMap<String,Object>();
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		try {
			String str="";
			String spattern="yyyy-MM-dd HH:mm:ss";
			if(map.containsKey("timeonly")&&map.get("timeonly")!=null){
				if((boolean)map.get("timeonly")) {
					spattern="HH:mm";
				}
			}

			outmap=(Map<String,Object>) isDateValid(dateString,field,spattern,userInfo);
			str=(String) outmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

			if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()==str) {

				if ( map.containsKey("hideafterdate") || map.containsKey("hidebeforedate")) {


					boolean hideafterdate=false;
					boolean hidebeforedate=false;
					Instant currentdate = dateUtilityFunction.getCurrentDateTime(userInfo);
					if(map.containsKey("hideafterdate")) {
						hideafterdate = (boolean) map.get("hideafterdate");
					}

					if(map.containsKey("hidebeforedate")) {
						hidebeforedate = (boolean) map.get("hidebeforedate");

					}

					final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					final LocalDateTime ldt = LocalDateTime.parse(dateString, formatter);
					final ZoneId zoneId = ZoneId.of(userInfo.getStimezoneid());
					final ZonedDateTime zdt = ldt.atZone(zoneId);
					Instant instantDate = zdt.toInstant();
					if(map.containsKey("dateonly")) {
						if ((boolean) map.get("dateonly")) {
							currentdate = currentdate.truncatedTo(ChronoUnit.DAYS);
							instantDate = instantDate.truncatedTo(ChronoUnit.DAYS);
						}
					}


					if (map.containsKey("nperiodcode")) {
						int nperioddata = (int) map.get("nperioddata");
						int nperiodcode = (int) map.get("nperiodcode");
						int windowperiod = Integer.parseInt((String) map.get("windowperiod"));
						if ( nperiodcode == Enumeration.Period.Days.getPeriod() || nperiodcode ==  Enumeration.Period.Weeks.getPeriod() || nperiodcode == Enumeration.Period.Hours.getPeriod() ||
								nperiodcode == Enumeration.Period.Minutes.getPeriod()) {

							currentdate = currentdate.plus((nperioddata * windowperiod), ChronoUnit.MINUTES)
									.truncatedTo(ChronoUnit.SECONDS);
						} 
						else if(nperiodcode == Enumeration.Period.Month.getPeriod()) {
							int nday = 0;
							if (map.get("nday") != null) {
								nday = Integer.parseInt((String) map.get("nday"));
							}
							currentdate = currentdate.plus((nperioddata * windowperiod + 1440 * nday ), ChronoUnit.MINUTES)
									.truncatedTo(ChronoUnit.SECONDS);
						}

						else if (nperiodcode ==  Enumeration.Period.Years.getPeriod()) {
							int nday = 0;
							int nmonth = 0;
							if (map.get("nday") != null) {
								nday = Integer.parseInt((String) map.get("nday"));
							}
							if (map.get("nmonth") != null) {
								nmonth = Integer.parseInt((String) map.get("nmonth"));

							}
							currentdate = currentdate.plus((nperioddata * windowperiod + 1440 * nday + 43200 * nmonth), ChronoUnit.MINUTES)
									.truncatedTo(ChronoUnit.SECONDS);
						}
					}

					if (hideafterdate == true && hidebeforedate == true) {
						//int value1 = instantDate.compareTo(currentdate);
						//int value= currentdate.compareTo(instantDate);
						if(instantDate.isAfter(currentdate)|| currentdate.isBefore(instantDate)) {
							outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), field+ " "+dateString + " "+ commonFunction.getMultilingualMessage(
									"IDS_SHOULBEEQUALDATE", userInfo.getSlanguagefilename()) + " "+currentdate.toString());							
						}
					}
					else if (hideafterdate) {
						if (instantDate.isAfter(currentdate)) {
							outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),field+" "+dateString + " "+ commonFunction.getMultilingualMessage(
									"IDS_SHOULBEBEFOREEQUALDATE", userInfo.getSlanguagefilename()) + " "+currentdate.toString());		
						}
					}

					else if (hidebeforedate) {
						if (currentdate.isBefore(instantDate)) {
							outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),field +" "+ dateString + " "+ commonFunction.getMultilingualMessage(
									"IDS_SHOULBEAFTEREQUALDATE", userInfo.getSlanguagefilename()) + " "+currentdate.toString());		
						}
					}
				}

			}	
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			//LOGGER.error(e.getCause());

		}
		outmap.put("cellData",dateString);

		return outmap;
	}

	
	public Map<String,Object> isValidNumeric(Map<String,Object>map, String number, String sfield, UserInfo userInfo) throws Exception {
		Map<String,Object> outputMap = new HashMap<String,Object>();
		try {

			Double num = Double.parseDouble(number);	

			if(map.containsKey("precision") && (!map.get("precision").toString().isEmpty())) {
				int precisionNo= Integer.parseInt(map.get("precision").toString());
				int noAftDecimal=number.substring(number.indexOf(".")+1).length();   
				if(noAftDecimal > precisionNo)
				{
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield +" : "+ number+ " "
									+commonFunction.getMultilingualMessage("IDS_INVALIDPRECISION", userInfo.getSlanguagefilename())
									+" "+precisionNo);
					return outputMap;
				}
			}

			if(map.containsKey("min") && !map.get("min").toString().isEmpty()) {
				if(!((Double)num >= Double.parseDouble(map.get("min").toString()))) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield +" : "+ number+ " "
									+commonFunction.getMultilingualMessage("IDS_LESSTHANMINVALUE", userInfo.getSlanguagefilename())
									+" "+map.get("min").toString());
					return outputMap;
				}
			}
			if(map.containsKey("max") && !map.get("max").toString().isEmpty()) {
				if(!((Double)num <= Double.parseDouble(map.get("max").toString()))) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield +" : "+ number+ " "
									+commonFunction.getMultilingualMessage("IDS_MORETHANMAXVALUE", userInfo.getSlanguagefilename())
									+" "+map.get("max").toString());
					return outputMap;
				}
			}
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());		
			return outputMap;
		}catch (Exception e) {
			LOGGER.error(e.getMessage());
			//LOGGER.error(e.getCause());
			
		}
		outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				sfield +" : "+ number+ " "
						+commonFunction.getMultilingualMessage("IDS_INVALIDNUMBER", userInfo.getSlanguagefilename()) );
		return outputMap;
	}
	
	public Map<String, Object> isValidString(Map<String, Object> map, String strObj, String sfield, UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
// ALPD-5470 Added Spl Characters (,:,) Added by Abdul
		//String specialCharacters = ".!#$@%&'*+\\\",./=?^_`{|}~-(:)";
		String specialCharacters = ".!#$@%&'*+\\\",./=?^_`{|}~\\-(:)";
		try {
			if (map.containsKey("sfieldlength") && map.get("sfieldlength") != null
					&& !map.get("sfieldlength").toString().isEmpty()) {
				if (strObj.length() > Integer.parseInt(map.get("sfieldlength").toString())) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield + " : " + strObj + " "
									+ commonFunction.getMultilingualMessage("IDS_MORETHANMAXVALUE",
											userInfo.getSlanguagefilename())
									+ " " + map.get("sfieldlength").toString());
					return outputMap;

				}
			}
			if (map.containsKey("isnumeric") && (boolean) map.get("isnumeric")) {
				if (strObj.matches(".*[^0-9.].*")) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage("IDS_INVALIDNUMBER",
									userInfo.getSlanguagefilename()));
					return outputMap;
				}
				// if ( map.containsKey("ncustomization") && (boolean)map.get("ncustomization"))
				// {
				if (map.containsKey("precision") && (!map.get("precision").toString().isEmpty())) {
					int precisionNo = Integer.parseInt(map.get("precision").toString());
					int noAftDecimal = strObj.substring(strObj.indexOf(".") + 1).length();
					if (noAftDecimal > precisionNo) {
						outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
										"IDS_INVALIDPRECISION", userInfo.getSlanguagefilename()) + " " + precisionNo);
						return outputMap;
					}
				}
				if (map.containsKey("nmaxvalue") && map.get("nmaxvalue") != null
						&& (Double.parseDouble(map.get("nmaxvalue").toString()) < Double.parseDouble(strObj))) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield + " : " + strObj + " " + commonFunction
									.getMultilingualMessage("IDS_MORETHANMAXVALUE", userInfo.getSlanguagefilename())
									+ " " + map.get("nmaxvalue").toString());
					return outputMap;
				}
				if (map.containsKey("nminvalue") && map.get("nminvalue") != null
						&& (Double.parseDouble(map.get("nminvalue").toString()) > Double.parseDouble(strObj))) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							sfield + " : " + strObj + " " + commonFunction
									.getMultilingualMessage("IDS_LESSTHANMINVALUE", userInfo.getSlanguagefilename())
									+ " " + map.get("nminvalue").toString());
					return outputMap;
				}
				// }
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				return outputMap;
			} else if (map.containsKey("isalphabetsmall") && (boolean) map.get("isalphabetsmall")) {
				if (strObj.matches(".*[^a-z].*")) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " a to z");
					return outputMap;
				} else {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			} else if (map.containsKey("isalphabetcaptial") && (boolean) map.get("isalphabetcaptial")) {
				if (strObj.matches(".*[^A-Z].*")) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " A to Z");
					return outputMap;
				} else {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			} else if (map.containsKey("isalphabetspl") && (boolean) map.get("isalphabetspl")) {
				// ALPD-5470 Added Spl Characters (,:,) Added by Abdul
				if(strObj.matches(".*[^A-Za-z.!#$@%&'*+\",./=?^_`{|}~(:)\\-].*")) {
               //  if (strObj.matches(".*[^A-Za-z.!#$@%&'*+\",./=?^_`{|}~-(:)].*")) {
					if (map.containsKey("nsplcharnotallow") && map.get("nsplcharnotallow") != null) {
						String splChrs = map.get("nsplcharnotallow").toString();
						specialCharacters = specialCharacters.replaceAll("[" + splChrs + "]", "");

					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " A to Z, a to z, Special Characters: " + specialCharacters);
					return outputMap;
				} else {
					if (map.containsKey("ncustomization") && (boolean) map.get("ncustomization")) {

						if (map.containsKey("nsplcharnotallow") && map.get("nsplcharnotallow") != null) {
							String splChrs = map.get("nsplcharnotallow").toString();
							int noOfSplChr = strObj.replaceAll("[^" + splChrs + "]", "").length();
							if (noOfSplChr > 0) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_CONTAINSUNALLOWEDSPLCHR",
														userInfo.getSlanguagefilename())
												+ " " + splChrs);
								return outputMap;
							}
						}
						if (map.containsKey("ncasesensitive") && (boolean) map.get("ncasesensitive")) {
							if (map.containsKey("nmaxsmallletters") && map.get("nmaxsmallletters") != null) {
								Double noOfSmllLts = (double) strObj.replaceAll("[^a-z]", "").length();
								if (noOfSmllLts > Double.parseDouble(map.get("nmaxsmallletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSMALLLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxsmallletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxcapticalletters") && map.get("nmaxcapticalletters") != null) {
								Double noOfCapLts = (double) strObj.replaceAll("[^A-Z]", "").length();
								if (noOfCapLts > Double.parseDouble(map.get("nmaxcapticalletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDCAPITALLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxcapticalletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[a-zA-Z0-9]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						} else if (map.containsKey("ncaptialletters") && (boolean) map.get("ncaptialletters")) {
							if (strObj.matches(".*[a-z0-9].*")) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage(
														"IDS_ENTERVALIDINPUTDATA", userInfo.getSlanguagefilename())
												+ " "
												+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT",
														userInfo.getSlanguagefilename())
												+ " A to Z, Special Characters .!#$@%&'*+\\\",./=?^_`{|}~-");
								return outputMap;
							}
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^A-Z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[A-Z]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						} else if (map.containsKey("nsmallletters") && (boolean) map.get("nsmallletters")) {
							if (strObj.matches(".*[A-Z0-9].*")) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
														userInfo.getSlanguagefilename())
												+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT",
														userInfo.getSlanguagefilename())
												+ " a to z, Special Characters .!#$@%&'*+\\\",./=?^_`{|}~-");
								return outputMap;
							}
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^a-z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[a-z]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						} else {
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^a-zA-Z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[a-zA-Z]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						}
					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			} else if (map.containsKey("isnumericspl") && (boolean) map.get("isnumericspl")) {
				// ALPD-5470 Added Spl Characters (,:,) Added by Abdul
				if(strObj.matches(".*[^0-9.!#$@%&'*+\",./=?^_`{|}~(:)\\-].*")){
                  //if (strObj.matches(".*[^0-9.!#$@%&'*+\",./=?^_`{|}~-(:)].*")) {
					if (map.containsKey("nsplcharnotallow") && map.get("nsplcharnotallow") != null) {
						String splChrs = map.get("nsplcharnotallow").toString();
						specialCharacters = specialCharacters.replaceAll("[" + splChrs + "]", "");

					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " 0 to 9, Special Characters: " + specialCharacters);
					return outputMap;
				} else {
					if (map.containsKey("ncustomization") && (boolean) map.get("ncustomization")) {
						if (map.containsKey("nsplcharnotallow") && map.get("nsplcharnotallow") != null) {
							String splChrs = map.get("nsplcharnotallow").toString();
							int noOfSplChr = strObj.replaceAll("[^" + splChrs + "]", "").length();
							if (noOfSplChr > 0) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_CONTAINSUNALLOWEDSPLCHR",
														userInfo.getSlanguagefilename())
												+ " " + splChrs);
								return outputMap;
							}
						}
						if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
							Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
							if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_MORETHANMAXALLOWEDNUMBERS",
														userInfo.getSlanguagefilename())
												+ " " + map.get("nmaxnumeric").toString());
								return outputMap;
							}
						}
						if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
							Double noOfSplChr = (double) strObj.replaceAll("[0-9]", "").length();
							if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage(
														"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
														userInfo.getSlanguagefilename())
												+ " " + map.get("nsplchar").toString());
								return outputMap;
							}
						}
					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			} else if (map.containsKey("isalphanumeric") && (boolean) map.get("isalphanumeric")) {
				if (strObj.matches(".*[^a-zA-Z0-9].*")) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " a to z, A to Z, 0 to 9");
					return outputMap;
				} else {
					if (map.containsKey("ncustomization") && (boolean) map.get("ncustomization")) {
						if (map.containsKey("ncasesensitive") && (boolean) map.get("ncasesensitive")) {
							if (map.containsKey("nmaxsmallletters") && map.get("nmaxsmallletters") != null) {
								Double noOfSmllLts = (double) strObj.replaceAll("[^a-z]", "").length();
								if (noOfSmllLts > Double.parseDouble(map.get("nmaxsmallletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSMALLLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxsmallletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxcapticalletters") && map.get("nmaxcapticalletters") != null) {
								Double noOfCapLts = (double) strObj.replaceAll("[^A-Z]", "").length();
								if (noOfCapLts > Double.parseDouble(map.get("nmaxcapticalletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDCAPITALLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxcapticalletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
						} else if (map.containsKey("ncaptialletters") && (boolean) map.get("ncaptialletters")) {
							if (strObj.matches(".*[^A-Z0-9].*")) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
														userInfo.getSlanguagefilename())
												+ " " + commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT",
														userInfo.getSlanguagefilename())
												+ " A to Z, 0 to 9");
								return outputMap;
							}
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^A-Z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
						} else if (map.containsKey("nsmallletters") && (boolean) map.get("nsmallletters")) {
							if (strObj.matches(".*[^a-z0-9].*")) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										sfield + " : " + strObj + " "
												+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
														userInfo.getSlanguagefilename())
												+ " " + commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT",
														userInfo.getSlanguagefilename())
												+ " a to z, 0 to 9");
								return outputMap;
							}
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^a-z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
						} else {
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^a-zA-Z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
						}

					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			} else {
                // ALPD-5470 Added Spl Characters (,:,) Added by Abdul 
				if(strObj.matches(".*[^\\sA-Za-z0-9.!#$@%&'*+\",./=?^_`{|}~(:)\\-].*")) {
				//if (strObj.matches(".*[^\\sA-Za-z0-9.!#$@%&'*+\",./=?^_`{|}~(:)-].*")) {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
							+ commonFunction.getMultilingualMessage("IDS_ENTERVALIDINPUTDATA",
									userInfo.getSlanguagefilename())
							+ " "
							+ commonFunction.getMultilingualMessage("IDS_ALLOWEDINPUT", userInfo.getSlanguagefilename())
							+ " A to Z, a to z, 0 to 9, Special Characters .!#$@%&'*+\\\",./=?^_`{|}~-(:)");
					return outputMap;
				} else {
					if (map.containsKey("ncustomization") && (boolean) map.get("ncustomization")) {
						if (map.containsKey("ncasesensitive") && (boolean) map.get("ncasesensitive")) {
							if (map.containsKey("nmaxsmallletters") && map.get("nmaxsmallletters") != null) {
								Double noOfSmllLts = (double) strObj.replaceAll("[^a-z]", "").length();
								if (noOfSmllLts > Double.parseDouble(map.get("nmaxsmallletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSMALLLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxsmallletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxcapticalletters") && map.get("nmaxcapticalletters") != null) {
								Double noOfCapLts = (double) strObj.replaceAll("[^A-Z]", "").length();
								if (noOfCapLts > Double.parseDouble(map.get("nmaxcapticalletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDCAPITALLETTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxcapticalletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[a-zA-Z0-9]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						} else {
							if (map.containsKey("nmaxletters") && map.get("nmaxletters") != null) {
								Double noOfChr = (double) strObj.replaceAll("[^a-zA-Z]", "").length();
								if (noOfChr > Double.parseDouble(map.get("nmaxletters").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDLETTERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxletters").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nmaxnumeric") && map.get("nmaxnumeric") != null) {
								Double noOfNum = (double) strObj.replaceAll("[^0-9]", "").length();
								if (noOfNum > Double.parseDouble(map.get("nmaxnumeric").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " " + commonFunction.getMultilingualMessage(
													"IDS_MORETHANMAXALLOWEDNUMBERS", userInfo.getSlanguagefilename())
													+ " " + map.get("nmaxnumeric").toString());
									return outputMap;
								}
							}
							if (map.containsKey("nsplchar") && map.get("nsplchar") != null) {
								Double noOfSplChr = (double) strObj.replaceAll("[a-zA-Z0-9]", "").length();
								if (noOfSplChr > Double.parseDouble(map.get("nsplchar").toString())) {
									outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
											sfield + " : " + strObj + " "
													+ commonFunction.getMultilingualMessage(
															"IDS_MORETHANMAXALLOWEDSPECIALCHARACTERS",
															userInfo.getSlanguagefilename())
													+ " " + map.get("nsplchar").toString());
									return outputMap;
								}
							}
						}
					}
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					return outputMap;
				}
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			//LOGGER.error(e.getCause());
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sfield + " : " + strObj + " "
					+ commonFunction.getMultilingualMessage("IDS_INVALIDSTRING", userInfo.getSlanguagefilename()));
			return outputMap;
		}

	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> isPreDefinedOptionValid(Map<String,Object> map, String dataStr, String sfield, UserInfo userInfo) throws Exception{
		Map<String,Object> optionsMap= new HashMap<String,Object>();
		Map<String,Object> outputMap= new HashMap<String,Object>();
		List<Map<String,Object>> tagsList= new ArrayList<Map<String,Object>>();
		if(map.containsKey("radioOptions") && map.get("radioOptions")!=null) {
			optionsMap=(Map<String, Object>) map.get("radioOptions");
			if(optionsMap.containsKey("tags") && optionsMap.get("tags")!=null) {
				tagsList=(List<Map<String, Object>>) optionsMap.get("tags");
				if(!tagsList.isEmpty()) {
					if( tagsList.stream().map(x -> x.get("text").toString()).anyMatch(x -> x.equals(dataStr)))  {
						outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());		
						return outputMap;
					}else {
						outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								sfield+": "+dataStr
								+" "+commonFunction.getMultilingualMessage("IDS_ENTERVALIDOPTION", userInfo.getSlanguagefilename())
								);
						return outputMap;
					}
				}
			}
		}
		outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				sfield+" "+commonFunction.getMultilingualMessage("IDS_NOOPTIONSAVAILABLE", userInfo.getSlanguagefilename()));
		return outputMap;
	}

	public Map<String,Object> isManadatoryField(Map<String, Object> man, String dataString,final String field,UserInfo userInfo) throws Exception{
		Map<String,Object> outmap=new LinkedHashMap<String,Object>();
		outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		if (!man.isEmpty()) {

			//			Map<String, Object> obj = man.get(0);

			if ((boolean) man.get("mandatory")) {	

				if (dataString == null
						|| dataString.equals("")) {


					outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),field+" " 
							+ commonFunction.getMultilingualMessage(
									"IDS_MANDATORYFIELDISEMPTY",
									userInfo.getSlanguagefilename()));

				}

			}

		}
		return outmap;

	}
	
	public Map<String,Object> isComboValid(Map<String,Object> combodata,UserInfo userInfo,String field,String dataStr) throws Exception{

		Map<String,Object> outmap=new LinkedHashMap<String,Object>();

		outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		String list2="";



		if (!combodata.isEmpty()) {

			boolean multiLingual = false;
			if (combodata.containsKey("isMultiLingual")) {
				multiLingual = (boolean) combodata.get("isMultiLingual");

			}
			String masterQuery = "";

			if (multiLingual) {
				masterQuery = "select * from " + combodata.get("source")
				+ " where lower(jsondata -> '" + combodata.get("displaymember") + "'->>'"
				+ userInfo.getSlanguagetypecode() + "')='"
				+ dataStr.toLowerCase()
				+ "'" + "and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			} else {

				masterQuery = "select * from " + combodata.get("source") + " where lower(\""
						+ combodata.get("displaymember") + "\")='"
						+ dataStr.toLowerCase()
						+ "' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			}
			List<?> lstData = jdbcTemplate.queryForList(masterQuery);
			if (!lstData.isEmpty()) {
				Map<String, Object> mapData = (Map<String, Object>) lstData.get(0);
				JSONObject newData = new JSONObject();
				newData.put("pkey", combodata.get("valuemember"));
				if (multiLingual) {
					PGobject pgObject = (PGobject) mapData.get("jsondata");
					Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
					Map<String, Object> list1 = (Map<String, Object>) (list
							.get(combodata.get("displaymember")));
					list2 = (String) (list1.get(userInfo.getSlanguagetypecode()));
					newData.put("label", list2);
				} else {
					newData.put("label", mapData.get(combodata.get("displaymember")));
				}

				newData.put("value", mapData.get(combodata.get("valuemember")));
				newData.put("source",combodata.get("source"));
				newData.put("nquerybuildertablecode", combodata.get("nquerybuildertablecode"));

				String displaymember="";
				if (multiLingual) {
					PGobject pgObject = (PGobject) mapData.get("jsondata");
					Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
					Map<String, Object> list1 = (Map<String, Object>) (list
							.get(combodata.get("displaymember")));
					displaymember = (String) (list1.get(userInfo.getSlanguagetypecode()));

				}
				else {
					displaymember= mapData.get(combodata.get("displaymember")).toString();
				}


				outmap.put("objJsonData",newData);
				outmap.put("displaymember", displaymember);


			} else {

				outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), dataStr +" "+
						commonFunction.getMultilingualMessage( 
								"IDS_VALUENOTPRESENTINPARENT",
								userInfo.getSlanguagefilename()) + " (" + combodata.get("label") + ")");
			}
		}
		return outmap;

	}

	
	public Map<String,Object> isEmailValid(final String field,final String email,final UserInfo userInfo) throws Exception{

		Map<String,Object> outmap =new LinkedHashMap<>();

		outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		if (email != "") {
			if (!projectDAOSupport.patternMatches(email)) {
				outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), field + ": "+email+" "+
						commonFunction.getMultilingualMessage(
								"IDS_INVALIDEMAILID",
								userInfo.getSlanguagefilename()));
			}
		}

		return outmap;
	}
	
	
	
	
	public Map<String,Object> isDateValid(String dateString,String sfield, String pattern,UserInfo userInfo) throws Exception {
		
		Map<String,Object> map = new LinkedHashMap<String,Object>();

		try {
			//ALPD-3457
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			if (sdf.format(sdf.parse(dateString)).equals(dateString)) {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				return map;
			}else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),sfield + " "+dateString + " "+ commonFunction.getMultilingualMessage(
						"IDS_INVALIDFORMAT", userInfo.getSlanguagefilename()) +"("+pattern+")");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			//LOGGER.error(e.getCause());
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),sfield + " "+dateString + " "+ commonFunction.getMultilingualMessage(
					"IDS_INVALIDFORMAT", userInfo.getSlanguagefilename()) +"("+pattern+")");
		}

		return map;
	}
	

	
	
	public Map<String,Object> importValidData(Map<String, Object> objExportFileds, Cell cell,String sfield, UserInfo userInfo) throws Exception {

		String inputType = objExportFileds.get("inputtype").toString();

		String dataStr="";

		Map<String,Object> outmap =new LinkedHashMap<String,Object>();

		outmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());


		if(cell==null) {

			dataStr="";
		}
		else if (cell.getCellType() == CellType.STRING) {

			dataStr=cell.getStringCellValue();

		} else if (cell.getCellType() == CellType.NUMERIC) {

			if(DateUtil.isCellDateFormatted(cell)) {

				if(cell.getDateCellValue()!=null) {

					SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					dataStr= sourceFormat.format(cell.getDateCellValue());
				}
				else {

					dataStr="";
				}
			}
			else {

				dataStr=NumberToTextConverter.toText(cell.getNumericCellValue());

			}
		}
		else if(DateUtil.isCellDateFormatted(cell)) {

			if(cell.getDateCellValue()!=null) {

				SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				dataStr= sourceFormat.format(cell.getDateCellValue());
			}
			else {
				dataStr="";
			}
		}
		else {
			dataStr="";
		}
		boolean isManadatoryField=false;

		if(objExportFileds.containsKey("mandatory")){

			isManadatoryField=(boolean)objExportFileds.get("mandatory");

		}
		if(isManadatoryField) {

			outmap = isManadatoryField(objExportFileds,dataStr,sfield,userInfo);				

		}

		String responseString="";

		responseString=(String)outmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if(isManadatoryField || !dataStr.isEmpty()) {

			if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()==responseString){

				if(inputType.equalsIgnoreCase("combo")) {	

					outmap=isComboValid(objExportFileds,userInfo,sfield,dataStr);

				}
				if (inputType.equalsIgnoreCase("date")) {

					outmap=(Map<String,Object>) isValidDate(objExportFileds, dataStr,sfield ,userInfo);
				}
				if(inputType.equalsIgnoreCase("email")) {

					outmap= (Map<String,Object>) isEmailValid(sfield,dataStr,userInfo);
				}
				if(inputType.equalsIgnoreCase("textinput") || inputType.equalsIgnoreCase("textarea") ) {

					outmap=(Map<String,Object>) isValidString(objExportFileds ,  dataStr,  sfield,  userInfo);
				}
				if (inputType.equalsIgnoreCase("predefineddropdown") || inputType.equalsIgnoreCase("checkbox") || inputType.equalsIgnoreCase("radio")) {

					outmap=(Map<String,Object>) isPreDefinedOptionValid(objExportFileds, dataStr,sfield ,userInfo);
				}
				if (inputType.equalsIgnoreCase("numeric") ) {

					outmap=(Map<String,Object>) isValidNumeric(objExportFileds, dataStr,sfield ,userInfo);
				}

			}

		}
		outmap.put("cellData",dataStr);

		return outmap;
	}
	
	public Map<String, Object> getUserBasedCerticationExpiry(String stechniqueCode,UserInfo userInfo,String strainingCode) throws Exception {
		
		final Date date =new Date();
		final Map<String, Object> map = new HashMap<>();
		final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");		
		final String currentdate = sdFormat.format(date);

		final String sQuery = " select * from trainingcertification tc,trainingparticipants tp "
							+ " where dtrainingexpirydate > '"+currentdate+"'"
							+ " and tc.ntrainingcode=tp.ntrainingcode and tp.nusercode="+userInfo.getNusercode()+" "
							+ " and tc.ntechniquecode in ("+stechniqueCode+") and tc.ntrainingcode in ("+strainingCode+")"
							+ " and tc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tc.nsitecode="+userInfo.getNmastersitecode()
							+ " and tp.nsitecode="+userInfo.getNmastersitecode();
		final List<TrainingCertification> lstTrainingCert=(List<TrainingCertification>) jdbcTemplate.query(sQuery, new TrainingCertification());

		if(lstTrainingCert.size() > 0) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),(commonFunction.getMultilingualMessage("IDS_USERSCERTICATEEXPIRED",userInfo.getSlanguagefilename())));
		}
		return map;

	}
	
	public Map<Integer, String> getfnRereleaseFormat(List<String> list, int sequenceno, String sFormat, Instant lastresetdate,
			int nseqnoarno, UserInfo userInfo, Map<String, Object> jsondata, int nregsubtypeversionreleasecode,
			int nperiodcode,int updatesequenceno) throws Exception {
		Map<Integer, String> returnMap = new TreeMap<Integer, String>();
		Map<String, Object> map = new HashMap<>();
		int nseqno;
		JSONObject jsonobj = new JSONObject(jsondata);
		if(nperiodcode != Enumeration.Period.Never.getPeriod()) {
			map = checkSequenceNoRelease(list, sequenceno,lastresetdate, nseqnoarno, userInfo, jsonobj, nregsubtypeversionreleasecode, nperiodcode,updatesequenceno);
			nseqno = (int) map.get("sequenceno");
		}else {
			nseqno =sequenceno;
			String str="";			
			if(jsonobj.has("nneedsitewisearnorelease") && jsonobj.getBoolean("nneedsitewisearnorelease")) {										
				str = "update seqnositereleasegenerator set nsequenceno="+(list.size() + updatesequenceno)+" where nseqnoreleasenogencode=" + nseqnoarno+"and nsitecode="+userInfo.getNtranssitecode()+"and nregsubtypeversionreleasecode="+nregsubtypeversionreleasecode+";";
				jdbcTemplate.execute(str);				
			}		
			else {
				str = "update seqnoreleasenogenerator set nsequenceno="+(list.size() + updatesequenceno)+" where nseqnoreleasenogencode=" + nseqnoarno;
			}

			jdbcTemplate.execute(str);
		}


		for (int i = 0; i < list.size(); i++) {
			String seqFormat = sFormat;
			if (seqFormat != null) {
				while (seqFormat.contains("{")) {
					int start = seqFormat.indexOf('{');
					int end = seqFormat.indexOf('}');

					String subString = seqFormat.substring(start + 1, end);
					if (subString.equalsIgnoreCase("XXXXX")) {
						String replaceString = userInfo.getSsitecode();
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					}
					else if (subString.equals("yy") || subString.equals("YY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("yyyy") || subString.equals("YYYY")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("mm") || subString.equals("MM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("mmm") || subString.equals("MMM")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("mon") || subString.equals("MON")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MON", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.equals("dd") || subString.equals("DD")) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
						sdf.toPattern();
						Date date = new Date();
						String replaceString = sdf.format(date);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					} else if (subString.matches("9+")) {
						String seqPadding = "%0" + subString.length() + "d";
						String replaceString = String.format(seqPadding, nseqno);
						seqFormat = seqFormat.replace('{' + subString + '}', replaceString);
					}

					else {
						seqFormat = seqFormat.replace('{' + subString + '}', subString);
					}
				}
			}
			nseqno++;
			returnMap.put(Integer.parseInt(list.get(i)), seqFormat);
		}
		return returnMap;
	}

	private Map<String, Object> checkSequenceNoRelease(List<String> list,int sequenceno,Instant lastresetdate, int nseqnoreleasenogencode, UserInfo userinfo,
			JSONObject jsonobj, int nregsubtypeversionreleasecode, int nperiodcode,int updatesequenceno) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		Instant date = dateUtilityFunction.getUTCDateTime();
		if (lastresetdate!=null) {
			boolean check = false;
			int cal = 0;
			String dateFormat = "";
			if (nperiodcode == Enumeration.Period.Years.getPeriod()) {
				dateFormat = "yyyy";
			} else if (nperiodcode == Enumeration.Period.Month.getPeriod()) {
				dateFormat = "yyyy-MM";
			} else if (nperiodcode == Enumeration.Period.Weeks.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.WEEK_OF_YEAR;
			} else if (nperiodcode == Enumeration.Period.Days.getPeriod()) {
				dateFormat = "yyyy-MM-dd";
				check = true;
				cal = Calendar.DAY_OF_YEAR;
			}

			final LocalDateTime datetime = LocalDateTime.ofInstant(date.truncatedTo(ChronoUnit.SECONDS),ZoneOffset.UTC);
			String currentdate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(datetime);

			Date myDate1 = Date.from(lastresetdate);
			SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String resetDate = formatter1.format(myDate1);

			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
			sdf.toPattern();

			String formatted = "";
			String formatdate = "";
			if (userinfo.getIsutcenabled()== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				LocalDateTime ldt = sdf.parse(resetDate).toInstant().atZone(ZoneId.of(userinfo.getStimezoneid())).toLocalDateTime();
				formatted = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(currentdate).toInstant().atZone(ZoneId.of(userinfo.getStimezoneid())).toLocalDateTime();
				formatdate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			} else {

				LocalDateTime ldt = sdf.parse(resetDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				formatted = DateTimeFormatter.ofPattern(dateFormat).format(ldt);

				LocalDateTime ldt1 = sdf.parse(currentdate).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				formatdate = DateTimeFormatter.ofPattern(dateFormat).format(ldt1);
			}


			boolean bYearreset = false;
			if (check) {
				Date date2 = sdf.parse(formatdate);
				Calendar c = Calendar.getInstance();
				c.setTime(date2);
				Date date3 = sdf.parse(formatted);
				Calendar c1 = Calendar.getInstance();
				c1.setTime(date3);
				int weekandDay = c.get(cal);
				int weekandDay1 = c1.get(cal);
				if (weekandDay != weekandDay1) {
					bYearreset = true;
				}
			} else if (!(formatdate.equals(formatted))) {
				bYearreset = true;
			}
			String st="";
			if (bYearreset) {

				if(jsonobj.has("nneedsitewisearnorelease") && jsonobj.getBoolean("nneedsitewisearnorelease")) {										
					st = "update seqnositereleasegenerator set nsequenceno="+(list.size())+ " where nseqnoreleasenogencode=" + nseqnoreleasenogencode+"and nsitecode="+userinfo.getNtranssitecode()+"and nregsubtypeversionreleasecode="+nregsubtypeversionreleasecode+";";

				}
				else {
					st = "update seqnositereleasegenerator set nsequenceno= "+list.size()+"  where nseqnoreleasenogencode=" + nseqnoreleasenogencode;
				}



				jdbcTemplate.execute(st);
				sequenceno = 1;

				jsonobj.remove("dseqresetdate");
				jsonobj.put("dseqresetdate",dateUtilityFunction.instantDateToStringWithFormat(date, "yyyy-MM-dd HH:mm:ss"));

				st = "update regsubtypeconfigversionrelease set jsondata='" + jsonobj.toString() + "'::jsonb "
						+ " where nregsubtypeversionreleasecode=" + nregsubtypeversionreleasecode;
				jdbcTemplate.execute(st);
			}
			else {
				if(jsonobj.has("nneedsitewisearnorelease") && jsonobj.getBoolean("nneedsitewisearnorelease")) {										
					String str = "update seqnositereleasegenerator set nsequenceno="+(list.size() + updatesequenceno)+" where nseqnoreleasenogencode=" + nseqnoreleasenogencode+"and nsitecode="+userinfo.getNtranssitecode()+"and nregsubtypeversionreleasecode="+nregsubtypeversionreleasecode+";";
					jdbcTemplate.execute(str);				
				}

				else
				{				
					st = "update seqnoreleasenogenerator set nsequenceno="+(list.size() + updatesequenceno)+" where nseqnoreleasenogencode=" + nseqnoreleasenogencode;
					jdbcTemplate.execute(st);
				}
			}
		} else {

			String str ="";
			if(jsonobj.has("nneedsitewisearnorelease") && jsonobj.getBoolean("nneedsitewisearnorelease")) {										
				String str1 = "update seqnositereleasegenerator set nsequenceno="+(list.size() + updatesequenceno)+" where nseqnoreleasenogencode=" + nseqnoreleasenogencode+"and nsitecode="+userinfo.getNtranssitecode()+"and nregsubtypeversionreleasecode="+nregsubtypeversionreleasecode+";";
				jdbcTemplate.execute(str1);				
			}			
			else {
				str = "update seqnoreleasenogenerator set nsequenceno="+(list.size() + updatesequenceno)+"  where nseqnoreleasenogencode=" + nseqnoreleasenogencode+";";
			}
			jsonobj.put("dseqresetdate", dateUtilityFunction.instantDateToStringWithFormat(date, "yyyy-MM-dd HH:mm:ss"));
			str = str + "update regsubtypeconfigversionrelease set jsondata='" + jsonobj.toString() + "'::jsonb "
					+ " where nregsubtypeversionreleasecode=" + nregsubtypeversionreleasecode+";";
			jdbcTemplate.execute(str);
		}
		objMap.put("sequenceno", sequenceno);
		return objMap;
	}
	
	public void updateInstrumentCalibration(final String spreRegNo, final UserInfo userInfo) throws Exception
	{
		final String updateString = " update instrumentcalibration set npreregno=-1, "
									+ " dopendate =null,sopenreason='', sarno='' "
									+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
									+ " where npreregno in ("+spreRegNo+")"
									+ " and nsitecode=" + userInfo.getNtranssitecode() ;


		jdbcTemplate.execute(updateString);
	}

	
	
}
