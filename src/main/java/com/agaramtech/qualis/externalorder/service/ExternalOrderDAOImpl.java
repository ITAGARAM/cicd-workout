package com.agaramtech.qualis.externalorder.service;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.agaramtech.qualis.basemaster.model.Gender;
import com.agaramtech.qualis.basemaster.model.SamplePriority;
import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.contactmaster.model.PatientCaseType;
import com.agaramtech.qualis.contactmaster.service.patient.PatientDAO;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.model.SiteConfig;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.externalorder.model.ExternalOrderSample;
import com.agaramtech.qualis.externalorder.model.ExternalOrderTest;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ExternalOrderSupport;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Component;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.product.model.SampleAppearance;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationArno;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.model.RegistrationSampleArno;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.service.RegistrationDAO;
import com.agaramtech.qualis.registration.service.RegistrationDAOSupport;
import com.agaramtech.qualis.submitter.model.City;
import com.agaramtech.qualis.submitter.model.District;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;
import com.agaramtech.qualis.submitter.model.InstitutionSite;
import com.agaramtech.qualis.submitter.model.Region;
import com.agaramtech.qualis.submitter.model.Submitter;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class ExternalOrderDAOImpl implements ExternalOrderDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOrderDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final TransactionDAOSupport transactionDAOSupport;
	private final RegistrationDAOSupport registrationDAOSupport;
	private final ExternalOrderSupport externalOrderSupport;
	private final PatientDAO patientDAO;
	private final RegistrationDAO registrationDAO;
	
	
	@Override
	public ResponseEntity<Object> getExternalOrder(final int nexternalOrderCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String strQuery = "select pod.nexternalordercode,pod.sexternalorderid,pod.sorderseqno, pod.nproductcatcode, "
						+ " pod.nproductcode, pod.ninstitutioncode, pod.ninstitutionsitecode,pod.nallottedspeccode,"
						+ " to_char((pod.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "') as dcollectiondate, "
						+ " ins.sinstitutionname, inst.sinstitutionsitename, "
						+ " coalesce(dc.jsondata->'sdiagnosticcasename'->>'" + userInfo.getSlanguagetypecode()
						+ "',dc.jsondata->'sdiagnosticcasename'->>'en-US') as sdiagnosticcasename, "
						+ "pod.ndiagnosticcasecode, pod.ngendercode, pod.spatientid, pod.jsondata,pc.sproductcatname,p.sproductname,pod.spatientid,pod.ssubmittercode, "
						+ "pod.nsitecode, pod.nstatus ,coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
						+ "',g.jsondata->'sgendername'->>'en-US') as sgendername, "
						+ "concat(pm.sfirstname,' ',pm.slastname) as ssubmittername,"
						+ " pm.sfirstname spatientfirstname,pm.slastname as spatientlastname, "
						+ " s.ssubmitterid,ind.sinstitutiondeptname,ind.sinstitutiondeptcode, "
						+ "   pm.sfirstname||' '||pm.slastname as spatientname," + " s.sfirstname  as ssubmitterfirstname,"
						+ " s.slastname as ssubmitterlastname," + " s.semail as ssubmitteremail,"
						+ "   pm.sfathername as sfathername,pm.sfirstname,pm.slastname,pm.sage,"
						+ "   pm.ddob as ddob,"
						+ "    to_char(pm.ddob ,'" + userInfo.getSsitedate() + "') as sdob,"
						+ "   c.scountryname as scountryname," + "  dst.sdistrictname as sdistrictname, dst.ndistrictcode as ndistrictcode ,"
						+ "  pm.spostalcode as spostalcode," + "  pm.sphoneno as sphoneno," + "  pm.smobileno as smobileno,"
						+ "  pm.semail as semail," 
						+ "  pm.spassportno as spassportno," 
						+ "  pm.sexternalid as sexternalid,"
						+ "  pm.sstreet as sstreet," 
						+ "  pm.shouseno as shouseno,"
						+ "  rg.sregionname as sregionname,"
						+ " ind.sinstitutiondeptname as sinstitutiondeptname , "
						+ " ind.sinstitutiondeptcode as sinstitutiondeptcode , "
						+ " insc.sinstitutioncatname  as sinstitutioncatname ,"
						+ " coalesce(eot.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
						+ "',eot.jsondata->'sdisplayname'->>'en-US') as sexternalordertypename ,eot.nexternalordertypecode "
						+ " from externalorder pod , productcategory pc,product p,patientmaster pm, "
						+ " country c,city ctyn,district dst,region rg,"
						+ " gender g ,institution ins ,institutioncategory insc ,"
						+ " institutionsite inst,diagnosticcase dc,submitter s,institutiondepartment ind ,externalordertype eot"
						+ " where  pc.nproductcatcode =pod.nproductcatcode "
						+ " and p.nproductcode =pod.nproductcode " 
						+ " and pm.spatientid =pod.spatientid "
						+ " and pm.ncountrycode =c.ncountrycode " 
						+ " and pm.ncitycode =ctyn.ncitycode "
						+ " and inst.ndistrictcode =dst.ndistrictcode " 
						+ " and rg.nregioncode=pm.nregioncode"
						+ " and g.ngendercode = pod.ngendercode "
						+ " and pod.ninstitutioncode=ins.ninstitutioncode "
						+ " and ins.ninstitutioncatcode=insc.ninstitutioncatcode "
						+ " and inst.ninstitutionsitecode=pod.ninstitutionsitecode "
						+ " and pod.nexternalordertypecode=eot.nexternalordertypecode "
						+ " and ins.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and pod.ninstitutioncode=inst.ninstitutioncode "
						+ " and inst.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and ins.ninstitutioncode=inst.ninstitutioncode "
						+ " and pod.ndiagnosticcasecode=dc.ndiagnosticcasecode and dc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and pod.ssubmittercode=s.ssubmittercode and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and s.ninstitutiondeptcode=ind.ninstitutiondeptcode and ind.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pod.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
						+ " and ins.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inst.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ind.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ctyn.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		
						+ " order by pod.nexternalordercode";

		List<ExternalOrder> lstPortalOrder = jdbcTemplate.query(strQuery, new ExternalOrder());
		if (nexternalOrderCode == 0) {
			if (lstPortalOrder.isEmpty()) {
				outputMap.put("ExternalOrder", null);
				outputMap.put("selectedExternalOrder", null);
				outputMap.put("ExternalOrderTube", null);
				outputMap.put("ExternalOrderTest", null);

			} else {
				outputMap.put("ExternalOrder", lstPortalOrder);
				outputMap.put("selectedExternalOrder",
						lstPortalOrder.isEmpty() ? lstPortalOrder : lstPortalOrder.get(lstPortalOrder.size() - 1));
				if (!lstPortalOrder.isEmpty()) {
					final int nportalorderCode = lstPortalOrder.get(lstPortalOrder.size() - 1).getNexternalordercode();
					List<ExternalOrderSample> lstPortalOrderContainer = getExternalOrderSample(nportalorderCode,
							userInfo);
					List<ExternalOrderTest> lstPortalOrderTest = getExternalOrderTest(nportalorderCode, userInfo);
					outputMap.put("ExternalOrderTest", lstPortalOrderTest);
					if (!lstPortalOrderContainer.isEmpty()) {
						outputMap.put("selectedExternalOrderTube",
								lstPortalOrderContainer.get(lstPortalOrderContainer.size() - 1));

					}

					outputMap.put("ExternalOrderTube", lstPortalOrderContainer);
				}
			}
		} else {
			outputMap.put("ExternalOrder", lstPortalOrder);

			ExternalOrder selectedPortalOrderDetails = getPortalOrderDetailsById(nexternalOrderCode, userInfo);

			outputMap.put("selectedExternalOrder",
					selectedPortalOrderDetails == null
							? lstPortalOrder.isEmpty() ? lstPortalOrder : lstPortalOrder.get(lstPortalOrder.size() - 1)
							: selectedPortalOrderDetails);
			List<ExternalOrderSample> lstPortalOrderContainer = getExternalOrderSample(nexternalOrderCode, userInfo);
			if (!lstPortalOrderContainer.isEmpty()) {
				outputMap.put("selectedExternalOrderTube",
						lstPortalOrderContainer.get(lstPortalOrderContainer.size() - 1));
			}
			List<ExternalOrderTest> lstPortalOrderTest = getExternalOrderTest(nexternalOrderCode, userInfo);

			outputMap.put("ExternalOrderTube", lstPortalOrderContainer);
			outputMap.put("ExternalOrderTest", lstPortalOrderTest);

		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public List<ExternalOrderSample> getExternalOrderSample(final int nexternalordercode, UserInfo userInfo)
			throws Exception {

		final String query = " select poc.nexternalordersamplecode, poc.nexternalordercode, pod.nproductcatcode, pod.nproductcode,u.sunitname,poc.sexternalsampleid, "
							+ "poc.nunitcode, poc.nsitecode, poc.nstatus,ct.scomponentname as ssampletype, "
							+ " ins.sinstitutionname,ins.sinstitutioncode,inst.sinstitutionsitename,pod.sorderseqno,pod.ndiagnosticcasecode,pod.nallottedspeccode,dc.jsondata->'sdiagnosticcasename'->>'en-US' as sdiagnosticcasename,pod.ssubmittercode, "
							+ "   pm.sfirstname||' '||pm.slastname as spatientname, " + "   pm.sfathername as sfathername,"
							+ " to_char((pod.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "') as dcollectiondate, "
							+ "   pm.ddob as ddob," + "   c.scountryname as scountryname," + "  dst.sdistrictname as sdistrictname,"
							+ "  pm.spostalcode as spostalcode," + "  pm.sphoneno as sphoneno," + "  pm.smobileno as smobileno,"
							+ "  pm.semail as semail," + "  pm.spassportno as spassportno," + "  pm.sexternalid as sexternalid,"
							+ "  pm.sstreet as sstreet," + "  pm.shouseno as shouseno,"
							+ "  rg.sregionname as sregionname ,ind.sinstitutiondeptname as sinstitutiondeptname , ind.sinstitutiondeptcode as sinstitutiondeptcode " // ,containertype
																																										// cty
							+ "  from externalordersample poc,patientmaster pm,submitter s,externalorder pod,institutiondepartment ind,component ct,transactionstatus ts,unit u,gender g,institution ins,institutionsite inst,diagnosticcase dc,country c,city ctyn,district dst,region rg "
							+ " where ct.ncomponentcode=poc.ncomponentcode " + " and poc.nexternalordercode=pod.nexternalordercode "
							+ " and pod.ngendercode=g.ngendercode and g.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and pod.ninstitutioncode=ins.ninstitutioncode and ins.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and pod.ninstitutioncode=inst.ninstitutioncode and inst.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and ins.ninstitutioncode=inst.ninstitutioncode " + " and poc.nunitcode=u.nunitcode "
							+ " and pod.ndiagnosticcasecode=dc.ndiagnosticcasecode and dc.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and poc.nunitcode=u.nunitcode and u.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and poc.nexternalordercode="
							+ nexternalordercode + " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " " + " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and pod.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and poc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			
							+ " and ins.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and inst.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and s.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ind.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ctyn.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and u.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by poc.nexternalordersamplecode";

		return (List<ExternalOrderSample>) jdbcTemplate.query(query, new ExternalOrderSample());
	}

	@SuppressWarnings("unchecked")
	public List<ExternalOrderTest> getExternalOrderTest(final int nexternalordercode, UserInfo userInfo)
			throws Exception {
		final String query = " select eot.nexternalordertestcode, eot.nexternalordersamplecode,eot.nexternalordercode, eot.ntestcode, eot.nsitecode, eot.nstatus,tm.stestname as stestname, "
								+ "tc.stestcategoryname as stestcategoryname,tp.stestpackagename, eot.ncontainertypecode "
								+ "  from externalordertest eot,testmaster tm " + ",testcategory tc,testpackage tp "
								+ " where eot.nexternalordertestcode=eot.nexternalordertestcode " + " and tm.ntestcode=eot.ntestcode "
								+ " and tc.ntestcategorycode=tm.ntestcategorycode " + " and eot.nexternalordercode="
								+ nexternalordercode + " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "" + " and eot.ntestpackagecode=tp.ntestpackagecode and tp.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eot.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (List<ExternalOrderTest>) jdbcTemplate.query(query, new ExternalOrderTest());
	}

	// -----------
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getExternalOrderClickDetails(final int nportalOrderCode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
	
		String strQuery = " SELECT poc.nexternalordercode,  " + "       poc.nordertypecode, "
				+ "       poc.nproductcatcode, " + "       poc.nproductcode, " + "       poc.ngendercode, "
				+ "       poc.ninstitutioncode, " + "       poc.ninstitutionsitecode, "
				+ "       poc.ndiagnosticcasecode, " + "       poc.spatientid," + "		  poc.sorderseqno, "
				+ "       poc.ssubmittercode, " + "       poc.sexternalorderid, " + "       poc.jsondata, "
				+ "       poc.nsitecode, " + "       poc.nstatus,poc.nallottedspeccode, "
				+ "       pod.nexternalordersamplecode, " + "       pod.nexternalordercode, "
				+ "       pod.ncomponentcode, " + "       pod.nsampleqty, " + "       pod.nunitcode, "
				+ "       pod.sexternalsampleid, " + "       pod.nsitecode,  " + "       pod.nstatus, "
				+ "  u.sunitname, " + "	   i.sinstitutionname, " + "	   its.sinstitutionsitename, "
				+ "	   g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode() + "'  as sgendername,  "
				+ " to_char((poc.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "') as dcollectiondate, "
				+ "	    ct.scomponentname as ssampletype, "
				+ "		 pr.jsondata->'spriorityname'->>'" + userInfo.getSlanguagetypecode() + "'  as spriorityname, "
				+ "		ds.jsondata->'sdiagnosticcasename'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdiagnosticcasename,  " + "   s.sfirstname||' '||s.slastname as ssubmittername,"
				+ "   pm.sfirstname||' '||pm.slastname as spatientname," + "   pm.sfathername as sfathername,"
				+ "   pm.ddob as ddob," + "   c.scountryname as scountryname," + "  dst.sdistrictname as sdistrictname,"
				+ "  pm.spostalcode as spostalcode," + "  pm.sphoneno as sphoneno," + "  pm.smobileno as smobileno,"
				+ "  pm.semail as semail," + "  pm.spassportno as spassportno," + "  pm.sexternalid as sexternalid,"
				+ "  pm.sstreet as sstreet," + "  pm.shouseno as shouseno," + "  rg.sregionname as sregionname,"
				+ " id.sinstitutiondeptname as sinstitutiondeptname , id.sinstitutiondeptcode as sinstitutiondeptcode,"
				+ " eot.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "'  as sexternalordertypename,eot.nexternalordertypecode "
				+ " FROM   externalorder poc, " + "       externalordersample pod, "
				+ "	   productcategory pc,product p,patientmaster pm,gender g, "
				+ "  component ct, " // containertype cty ,
				+ "	   institution i,institutionsite its, " + "	   diagnosticcase ds, "
				+ "	   submitter s,institutiondepartment id,country c,city ctyn,district dst,region rg,unit u ,externalordertype eot"
				+ " WHERE  poc.nexternalordercode = pod.nexternalordercode  "
				+ " and  pc.nproductcatcode =poc.nproductcatcode "
				+ " and s.ninstitutiondeptcode=id.ninstitutiondeptcode" + " and p.nproductcode =poc.nproductcode  "
				+ " and pm.spatientid =poc.spatientid  " + "  and g.ngendercode = poc.ngendercode "
				+ " and ct.ncomponentcode=pod.ncomponentcode " + " and i.ninstitutioncode=poc.ninstitutioncode "
				+ "  and its.ninstitutionsitecode=poc.ninstitutionsitecode "
				+ " and pod.nexternalordercode= " + nportalOrderCode
				+ " and ds.ndiagnosticcasecode=poc.ndiagnosticcasecode " + " and s.ssubmittercode=poc.ssubmittercode "
				+ " and pm.ncountrycode =c.ncountrycode " + " and pm.ncitycode =ctyn.ncitycode "
				+ " and poc.nexternalordertypecode=eot.nexternalordertypecode "
				+ " and pm.ndistrictcode =dst.ndistrictcode " + " and pod.nunitcode=u.nunitcode"
				+ " and rg.nregioncode=pm.nregioncode" + " and poc.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pod.nstatus=     "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ct.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and its.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and id.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ExternalOrder> lstPortalOrder = jdbcTemplate.query(strQuery, new ExternalOrder());
		outputMap.put("selectedExternalOrder", lstPortalOrder.get(0));
	
		final List<ExternalOrderSample> lstPortalOrderContainer = getExternalOrderSample(nportalOrderCode, userInfo);
		if (!lstPortalOrderContainer.isEmpty()) {
			outputMap.put("selectedExternalOrderTube", lstPortalOrderContainer.get(lstPortalOrderContainer.size() - 1));
		}
		final List<ExternalOrderTest> lstPortalOrderTest = getExternalOrderTest(nportalOrderCode, userInfo);

		outputMap.put("ExternalOrderTube", lstPortalOrderContainer);
		outputMap.put("ExternalOrderTest", lstPortalOrderTest);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ExternalOrder getPortalOrderDetailsById(final int nportalOrderCode, UserInfo userInfo) throws Exception {

			String strQuery = " SELECT poc.nexternalordercode,  " + "       poc.nordertypecode, "
				+ "       poc.nproductcatcode, " + "       poc.nproductcode, " + "       poc.ngendercode, "
				+ "       poc.ninstitutioncode, " + "       poc.ninstitutionsitecode, "
				+ "       poc.ndiagnosticcasecode," + "		  poc.nallottedspeccode, " + "       poc.spatientid, "
				+ "       poc.ssubmittercode, " + "       poc.sexternalorderid," + "		  poc.sorderseqno, "
				+ "       poc.jsondata, " + "       poc.nsitecode, " + "       poc.nstatus, "
				+ "       pod.nexternalordersamplecode, " + "       pod.nexternalordercode, "
				+ "       pod.ncomponentcode, " + "       pod.nsampleqty, " + "       pod.nunitcode, "
				+ "       pod.sexternalsampleid, " + "       pod.nsitecode,  " + "       pod.nstatus, "
				+ "  u.sunitname, " + "	   i.sinstitutionname, " + "	   its.sinstitutionsitename, "
				+ "	   g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode() + "'  as sgendername,  "
				+ "	   eot.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "'  as sexternalordertypename,  "
				+ "	    ct.scomponentname as ssampletype, "
				+ "		ds.jsondata->'sdiagnosticcasename'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdiagnosticcasename,  " + "   s.sfirstname||' '||s.slastname as ssubmittername,"
				+ "   pm.sfirstname||' '||pm.slastname as spatientname," + "   pm.sfathername as sfathername,"
				+ "   pm.ddob as ddob," + "   c.scountryname as scountryname," + "  dst.sdistrictname as sdistrictname,"
				+ "  pm.spostalcode as spostalcode," + "  pm.sphoneno as sphoneno," + "  pm.smobileno as smobileno,"
				+ "  pm.semail as semail," + "  pm.spassportno as spassportno," + "  pm.sexternalid as sexternalid,"
				+ "  pm.sstreet as sstreet," + "  pm.shouseno as shouseno," + "  rg.sregionname as sregionname,"
				+ " id.sinstitutiondeptname as sinstitutiondeptname , id.sinstitutiondeptcode as sinstitutiondeptcode,"
				+ " to_char((poc.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "') as dcollectiondate ,eot.nexternalordertypecode "
				+ " FROM   externalorder poc, " + "       externalordersample pod, "
				+ "	   productcategory pc,product p,patientmaster pm,gender g, "
				+ "  component ct, " 
				+ "	   institution i,institutionsite its, " + "	   diagnosticcase ds, "
				+ "	   submitter s,institutiondepartment id,country c,city ctyn,district dst,region rg,unit u,externalordertype eot  "
				+ " WHERE  poc.nexternalordercode = pod.nexternalordercode  "
				+ " and  pc.nproductcatcode =poc.nproductcatcode "
				+ " and s.ninstitutiondeptcode=id.ninstitutiondeptcode" + " and p.nproductcode =poc.nproductcode  "
				+ " and pm.spatientid =poc.spatientid  " + "  and g.ngendercode = poc.ngendercode "
				+ " and ct.ncomponentcode=pod.ncomponentcode " + " and i.ninstitutioncode=poc.ninstitutioncode "
				+ "  and its.ninstitutionsitecode=poc.ninstitutionsitecode and poc.nexternalordertypecode=eot.nexternalordertypecode "
				+ " and pod.nexternalordercode= " + nportalOrderCode
				+ " and ds.ndiagnosticcasecode=poc.ndiagnosticcasecode " + " and s.ssubmittercode=poc.ssubmittercode "
				+ " and pm.ncountrycode =c.ncountrycode " + " and pm.ncitycode =ctyn.ncitycode "
				+ " and pm.ndistrictcode =dst.ndistrictcode " + " and pod.nunitcode=u.nunitcode"
				+ " and rg.nregioncode=pm.nregioncode" + " and poc.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pod.nstatus=     "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ct.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and its.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and id.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
		return (ExternalOrder) jdbcUtilityFunction.queryForObject(strQuery, ExternalOrder.class, jdbcTemplate);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createExternalOrder(ExternalOrder objExternalOrder, int needfilter, UserInfo userInfo)
			throws Exception {
		
		final String slockQuery = " lock table lockexternalorder " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";

		jdbcTemplate.execute(slockQuery);
		
		Map<String, Object> returnMap = new HashMap();
		ObjectMapper mapper = new ObjectMapper();
		
		String externalOrderSampleQuery = "";
		String externalOrderTestQuery = "";
		LOGGER.info(objExternalOrder.toString());
		
		if(objExternalOrder.getNordertypecode() == Enumeration.OrderType.EXTERNAL.getOrderType()) {

			String strTestGroupSpecQuery = " SELECT  max(tgsp.napprovalstatus),tgsp.nallottedspeccode,max(tgsp.sspecname) sspecname "
											+ " FROM testgroupspecification tgsp WHERE tgsp.napprovalstatus = any(select napprovalstatuscode from approvalconfigrole acr "
											+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.napprovalconfigcode =  "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and tgsp.nstatus =" 
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nallottedspeccode="
											+ objExternalOrder.getNallottedspeccode()+ " GROUP  BY tgsp.nallottedspeccode;";
			final TestGroupSpecification testGroupSpec = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(strTestGroupSpecQuery, TestGroupSpecification.class, jdbcTemplate);
			
			if(testGroupSpec == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECIFICATIONALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			
			}

		JSONObject jsonObject = new JSONObject(objExternalOrder.getJsondata());
		
		//JSONObject patientObject = jsonObject.getJSONObject("patient");

		Patient objPatient = mapper.readValue(jsonObject.toString(), Patient.class);

		final String strquery = "select ex.nexternalordercode,g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
							+ "' sgendername,ex.sorderseqno,ex.nordertypecode,ex.nproductcatcode,ex.nproductcode,"
							+ " ex.ngendercode,ex.ninstitutioncode,ex.ninstitutionsitecode,ex.nallottedspeccode,ex.ndiagnosticcasecode,ex.spatientid,ex.ssubmittercode,ex.sexternalorderid,"
							+ "ex.jsondata,ex.dmodifieddate,ex.nsitecode,ex.nstatus,ex.nusercode, ex.ntransactionstatus from externalorder ex,gender g where ex.sexternalorderid ='"
							+ objExternalOrder.getSexternalorderid() + "' and ex.ngendercode = g.ngendercode and ex.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		ExternalOrder order = (ExternalOrder) jdbcUtilityFunction.queryForObject(strquery, ExternalOrder.class, jdbcTemplate);
		
		String sequencenoquery = "select stablename,nsequenceno from seqnoregistration  where stablename in ('externalorder','externalordertest','externalordersample') order by stablename ";

		List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequencenoquery, new SeqNoRegistration());
		returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		int seqorderdetail = ((int) returnMap.get("externalorder")) + 1;
		int seqordertest = ((int) returnMap.get("externalordertest"));
		int seqordersample = ((int) returnMap.get("externalordersample"));
		int nexternalordertype = 1;

		if (order == null) {

			if (objExternalOrder.getNordertypecode() == 2) {
				//external
				String sDate1 = objPatient.getSdob();// .replace("T", " ").replace("Z", " ");
				Date date1 = new SimpleDateFormat(userInfo.getSsitedate()).parse(sDate1);
				objPatient.setDdob(date1);
				objExternalOrder.setNparentsitecode(objExternalOrder.getNsitecode());
			} else {
				objExternalOrder.setSexternalorderid(Integer.toString(seqorderdetail));
				objExternalOrder.setNparentsitecode(userInfo.getNsitecode());
				objExternalOrder.setNsitecode(userInfo.getNsitecode());
			}
			objPatient.setSphoneno("-");
			Map<String, Object> patientMap = new HashMap<>();

			final String sQuery = "select * from patientmaster where" + " spatientid = '"
					+ objPatient.getSpatientid() + "'" + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<Patient> existingPatient = jdbcTemplate.query(sQuery, new Patient());
			String spatientid = "";

			if (existingPatient.size() == 0) {
				patientMap = patientDAO.savePatient(objPatient, userInfo);
				spatientid = patientMap.containsKey("spatientid") ? (String) patientMap.get("spatientid") : null;
				jsonObject.put("spatientid", spatientid);
			} else {
				spatientid = existingPatient.get(0).getSpatientid();
			}
			
			if(needfilter==2) {
				
				String query="select i.scityname from city c,institutionsite i where c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and c.ncitycode=i.ncitycode and i.ninstitutionsitecode="+objExternalOrder.getNinstitutionsitecode();
				
				City c=(City) jdbcUtilityFunction.queryForObject(query, City.class, jdbcTemplate);
								
				query ="select pm.* from patientmaster pm,city cp,city ct where pm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and pm.ncitycode=cp.ncitycode and pm.ncitycodetemp=ct.ncitycode and pm.spatientid='"+spatientid+"'";
				
				 Patient eo=(Patient) jdbcUtilityFunction.queryForObject(query, Patient.class, jdbcTemplate);
				 
				
				jsonObject.put("sinstitutioncityname", c.getScityname());
				
				jsonObject.put("spatientid", eo.getSpatientid());
				jsonObject.put("sfirstname", eo.getSfirstname());
				jsonObject.put("slastname", eo.getSlastname());
				jsonObject.put("sfathername", eo.getSfathername());
				jsonObject.put("spostalcode",eo.getSpostalcode());
				jsonObject.put("sstreet", eo.getSstreet());
				jsonObject.put("shouseno", eo.getShouseno());
				jsonObject.put("sflatno", eo.getSflatno());
				jsonObject.put("smobileno", eo.getSmobileno());
				jsonObject.put("srefid",eo.getSrefid());
				jsonObject.put("sphoneno", eo.getSphoneno());
				jsonObject.put("spassportno",eo.getSpassportno());
				jsonObject.put("scityname", eo.getScityname());
				jsonObject.put("sstreettemp", eo.getSstreettemp());
				jsonObject.put("shousenotemp", eo.getShousenotemp());
				
				jsonObject.put("sflatnotemp", eo.getSflatno());
				jsonObject.put("scitynametemp", eo.getScitynametemp());
				jsonObject.put("sexternalid", eo.getSexternalid());
				jsonObject.put("ninstitutiondistrictcode", (int)objExternalOrder.getNdistrictcode());
				jsonObject.put("ninstitutioncatcode", (int)objExternalOrder.getNinstitutioncatcode());
								
			}

			if (objExternalOrder.getNordertypecode() == 1) {
				// Internal
				nexternalordertype=objExternalOrder.getNexternalordertypecode();
				final String allottedSpecString = " SELECT   max(tgsp.nallottedspeccode) nallottedspeccode "
													+ " FROM   productcategory pc, " + "       treetemplatemanipulation ttm, "
													+ "	   testgroupspecification tgsp " + " WHERE  pc.nstatus= "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus=  "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nstatus= "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode "
													+ " and pc.nproductcatcode=ttm.nproductcatcode " + " and ttm.nsampletypecode="
													+ Enumeration.SampleType.CLINICALSPEC.getType() + " and ttm.nproductcatcode="
													+ objExternalOrder.getNproductcatcode()
													+ " and tgsp.napprovalstatus in (select napprovalstatuscode from " + " approvalconfigrole acr "
													+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno =  "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ " and acr.napprovalconfigcode =  "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

				TestGroupSpecification testGroup = (TestGroupSpecification) jdbcTemplate
						.queryForObject(allottedSpecString, new TestGroupSpecification());
				//objExternalOrder.setNallottedspeccode(testGroup.getNallottedspeccode());
				objExternalOrder.setNallottedspeccode(Enumeration.TransactionStatus.NA.gettransactionstatus());
				objExternalOrder.setSorderseqno(String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus()));
			}
			String externalOrder = "Insert into externalorder(nexternalordercode,nordertypecode,nexternalordertypecode,nproductcatcode,nproductcode,ngendercode,ninstitutioncode,ninstitutionsitecode,"
					+ "ndiagnosticcasecode,nallottedspeccode,nusercode,sorderseqno,spatientid,ssubmittercode,sexternalorderid,jsondata,ndefaultstatus,dmodifieddate,ntransactionstatus,nsitecode,nstatus,nparentsitecode)"
					+ "values(" + seqorderdetail + "," + objExternalOrder.getNordertypecode() + ", "+nexternalordertype+" ,"
					+ objExternalOrder.getNproductcatcode() + "," + objExternalOrder.getNproductcode() + ","
					+ objExternalOrder.getNgendercode() + "," + objExternalOrder.getNinstitutioncode() + ","
					+ objExternalOrder.getNinstitutionsitecode() + "," + objExternalOrder.getNdiagnosticcasecode() + ","
					+ objExternalOrder.getNallottedspeccode() + "," + userInfo.getNusercode() + ",N'"
					+ objExternalOrder.getSorderseqno() + "', N'" + objPatient.getSpatientid() + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(objExternalOrder.getSsubmittercode()) + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(objExternalOrder.getSexternalorderid()) + "', '" 
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()+",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," 
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					//+ "," + userInfo.getNtranssitecode() + ", "
					+ ", " + objExternalOrder.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", " +objExternalOrder.getNparentsitecode()  +")";

			jdbcTemplate.execute(externalOrder);

			String updateSeqQuery = "update seqnoregistration  set nsequenceno = " + seqorderdetail
					+ " where stablename ='externalorder';";

			if (objExternalOrder.getNordertypecode() == 2) {
				// External
				List<ExternalOrderSample> lstContainerType = objExternalOrder.getExternalordersample();

				for (ExternalOrderSample container : lstContainerType) {
					// ALPD-3575
					String dsampleCollectionDateTime = null;
					int nsampleAppearanceCode = -1;
					if (container.getSsamplecollectiondatetime() != null) {
						dsampleCollectionDateTime = "'"+container.getSsamplecollectiondatetime()+"'";
					}		
					if(container.getNsampleappearancecode() != null) {
						nsampleAppearanceCode = (int) container.getNsampleappearancecode();
					}
					seqordersample++;
					externalOrderSampleQuery += " (" + seqordersample + "," + seqorderdetail + ","
							+ container.getNcomponentcode() + "," + container.getNsampleqty() + ","
							+ container.getNunitcode() + ",'" + stringUtilityFunction.replaceQuote(container.getSexternalsampleid()) 
							+ "',"+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + objExternalOrder.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "," + objExternalOrder.getNparentsitecode()
							+ ", "+ dsampleCollectionDateTime+", " +nsampleAppearanceCode +"),";
//							" to_char((pod.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "')
// ALPD-3575
					for (ExternalOrderTest test : container.getExternalordertest()) {
						seqordertest++;
						externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + "," + seqorderdetail + ","
								+ test.getNtestpackagecode() + "," + test.getNcontainertypecode() + " ,"
								+ test.getNtestcode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ","
								+ objExternalOrder.getNsitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ "," + objExternalOrder.getNparentsitecode() + "),";
					}
				}

				externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,ntransactionstatus,dmodifieddate,nsitecode,nstatus, nparentsitecode, dsamplecollectiondatetime, nsampleappearancecode) values "
						+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";

				externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
						+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

				updateSeqQuery = updateSeqQuery + "update seqnoregistration  set nsequenceno =" + seqordersample
						+ " where stablename ='externalordersample';update seqnoregistration  set nsequenceno = "
						+ seqordertest + "" + " where stablename ='externalordertest';";

				jdbcTemplate.execute(externalOrderSampleQuery);
				jdbcTemplate.execute(externalOrderTestQuery);
				jdbcTemplate.execute(updateSeqQuery);
			}
//				else {
//					//Internal/Manual Order
//					objExternalOrder.setNexternalordercode(seqorderdetail);
//				}

			jdbcTemplate.execute(updateSeqQuery);

			if (objExternalOrder.getNordertypecode() == 2) {
				returnMap.putAll(
						(Map<String, Object>) getExternalOrder(objExternalOrder.getNexternalordercode(), userInfo)
								.getBody());
			} else {
				returnMap.putAll((Map<String, Object>) getOrderByType(objExternalOrder.getNordertypecode(),
						needfilter == 2 ? seqorderdetail : null, userInfo).getBody());

			}
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

		} else {
			if (jsonObject.has("sfirstname") && jsonObject.has("slastname") && jsonObject.has("sdob")
					&& jsonObject.has("ngendercode")) {
				String ddob = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(jsonObject.get("sdob").toString()));
				final String concat = jsonObject.get("sfirstname") + " " + jsonObject.get("slastname")
						+ ddob + objExternalOrder.getNgendercode();
				String Spatientid = (UUID.nameUUIDFromBytes(concat.getBytes()).toString());
				String patientid = "Select * from patientmaster where spatientid='" + Spatientid + "';";
				Patient objPatientcount = (Patient) jdbcUtilityFunction.queryForObject(patientid, Patient.class, jdbcTemplate);
				if (objPatientcount == null) {
					// Patient objPatient = new Patient();
					String sDate1 = objPatient.getSdob();
					Date date1 = new SimpleDateFormat(userInfo.getSsitedate()).parse(sDate1);
					objPatient.setDdob(date1);

					objPatient.setSfirstname((String) jsonObject.get("sfirstname"));
					objPatient.setSlastname((String) jsonObject.get("slastname"));
					objPatient.setSfathername((String) jsonObject.get("sfathername"));
					objPatient.setSage((String) jsonObject.get("sage"));
					objPatient.setNgendercode(objExternalOrder.getNgendercode());
					objPatient.setSdob((String) jsonObject.get("sdob"));
					objPatient.setSphoneno("-");
					Map<String, Object> patientMap = patientDAO.savePatient(objPatient, userInfo);
				} else {
					String PatientUpdateQuery = "update patientmaster set sfirstname='"+stringUtilityFunction.replaceQuote(jsonObject.get("sfirstname").toString())+"', slastname='"+stringUtilityFunction.replaceQuote(jsonObject.get("slastname").toString())
												+ "', ddob='"+jsonObject.get("ddob")+"', sage='"+jsonObject.get("sage")+"', ngendercode="+jsonObject.get("ngendercode")+", sfathername='"+stringUtilityFunction.replaceQuote(jsonObject.get("sfathername").toString())
												+ "', nneedmigrant="+jsonObject.get("nneedmigrant")+", ncountrycode="+jsonObject.get("ncountrycode")+ ", nregioncode="+jsonObject.get("nregioncode")+", ndistrictcode="+jsonObject.get("ndistrictcode")
												+ ", ncitycode="+jsonObject.get("ncitycode")+", scityname='"+stringUtilityFunction.replaceQuote(jsonObject.get("scityname").toString())+"', spostalcode='"+stringUtilityFunction.replaceQuote(jsonObject.get("spostalcode").toString())
												+ "', sstreet='"+stringUtilityFunction.replaceQuote(jsonObject.get("sstreet").toString())+"', shouseno='"+stringUtilityFunction.replaceQuote(jsonObject.get("shouseno").toString())+"', sflatno='"+stringUtilityFunction.replaceQuote(jsonObject.get("sflatno").toString())
												+ "', nneedcurrentaddress="+jsonObject.get("nneedcurrentaddress")+", "+ "nregioncodetemp="+jsonObject.get("nregioncodetemp")+", ndistrictcodetemp="+jsonObject.get("ndistrictcodetemp")
												+ ", ncitycodetemp="+jsonObject.get("ncitycodetemp")+", scitynametemp='"+stringUtilityFunction.replaceQuote(jsonObject.get("scitynametemp").toString())+"', spostalcodetemp='"+stringUtilityFunction.replaceQuote(jsonObject.get("spostalcodetemp").toString())
												+ "', sstreettemp='"+stringUtilityFunction.replaceQuote(jsonObject.get("sstreettemp").toString())+"', shousenotemp='"+stringUtilityFunction.replaceQuote(jsonObject.get("sstreettemp").toString())+"', "+ "sflatnotemp='"+stringUtilityFunction.replaceQuote(jsonObject.get("sflatnotemp").toString())
												+ "', sphoneno='"+jsonObject.get("sphoneno")+"', smobileno='"+jsonObject.get("smobileno")+"', semail='"+stringUtilityFunction.replaceQuote(jsonObject.get("semail").toString())+"', srefid='"+stringUtilityFunction.replaceQuote(jsonObject.get("srefid").toString())
												+ "', spassportno='"+stringUtilityFunction.replaceQuote(jsonObject.get("spassportno").toString())+"', sexternalid='"+stringUtilityFunction.replaceQuote(jsonObject.get("sexternalid").toString())+"', dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"' where spatientid ="
												+ "'"+Spatientid+"';";

					String PortalUpdateQuery = "update externalorder set " + " nproductcatcode = "
												+ objExternalOrder.getNproductcatcode() + "," + " nproductcode = "
												+ objExternalOrder.getNproductcode() + ",nallottedspeccode = "
												+ objExternalOrder.getNallottedspeccode() + ", ninstitutioncode ="
												+ objExternalOrder.getNinstitutioncode() + "," + " ninstitutionsitecode = "
												+ objExternalOrder.getNinstitutionsitecode() + "," + " ndiagnosticcasecode ="
												+ objExternalOrder.getNdiagnosticcasecode() + "," + "ngendercode = "
												+ objExternalOrder.getNgendercode() + ",nusercode=" + userInfo.getNusercode()
												+ ",jsondata = '" + jsonObject + "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
												+ "' where nexternalordercode = " + order.getNexternalordercode();

					jdbcTemplate.execute(PatientUpdateQuery + PortalUpdateQuery);

				}

				final String deletequery = "update externalordertest set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nexternalordercode = "
											+ order.getNexternalordercode() + ";" + "update externalordersample set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nexternalordercode = "
											+ order.getNexternalordercode() + ";";

				List<ExternalOrderSample> lstContainerType = objExternalOrder.getExternalordersample();

				for (ExternalOrderSample container : lstContainerType) {
					// ALPD-3575
					String dsampleCollectionDateTime = null;
					int nsampleAppearanceCode = -1;
					if (container.getSsamplecollectiondatetime() != null) {
						dsampleCollectionDateTime = "'"+container.getSsamplecollectiondatetime()+"'";
					}
					if(container.getNsampleappearancecode() != null) {
						nsampleAppearanceCode = (int) container.getNsampleappearancecode();
					}
					seqordersample++;
					externalOrderSampleQuery += " (" + seqordersample + "," + order.getNexternalordercode()+","
											+ container.getNcomponentcode() + "," + container.getNsampleqty() + ","
											+ container.getNunitcode() + ",'" + container.getSexternalsampleid() + "','"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + userInfo.getNtranssitecode() + ", "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
											+ dsampleCollectionDateTime + ", " + nsampleAppearanceCode+"),";
					for (ExternalOrderTest test : container.getExternalordertest()) {
						seqordertest++;
						externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + "," + order.getNexternalordercode() + ","
													+ test.getNtestpackagecode() + "," + test.getNcontainertypecode() + " ,"
													+ test.getNtestcode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ","
													+ userInfo.getNtranssitecode() + ","
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					}
				}

				externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus, dsamplecollectiondatetime, nsampleappearancecode) values "
						+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";

				externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus) values "
						+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

				String updateSeqQuery = "update seqnoregistration  set nsequenceno =" + seqordersample
						+ " where stablename ='externalordersample';update seqnoregistration  set nsequenceno = "
						+ seqordertest + "" + " where stablename ='externalordertest';";

				jdbcTemplate
						.execute(deletequery + externalOrderSampleQuery + externalOrderTestQuery + updateSeqQuery);

				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

			}
			return getExternalOrder(objExternalOrder.getNexternalordercode(), userInfo);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> receivedExternalOrder(ExternalOrder objOrderDetail, UserInfo userInfo)
			throws Exception {
		String strquery = "select nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus from externalordersample where nexternalordersamplecode ="
				+ objOrderDetail.getNexternalordersamplecode() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		List<ExternalOrderSample> lst = (List<ExternalOrderSample>) jdbcTemplate.query(strquery,
				new ExternalOrderSample());
		if (lst.size() > 0) {
			
			String updatequery = "update externalordersample set " + ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nexternalordersamplecode  =" + objOrderDetail.getNexternalordersamplecode() + "";
			jdbcTemplate.execute(updatequery);
			
			String sQuery = "select nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus from portalordercontainer where nportalordercode ="
							+ objOrderDetail.getNexternalordercode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			List<ExternalOrderSample> lstPortalOrderDetails = (List<ExternalOrderSample>) jdbcTemplate.query(sQuery,
					new ExternalOrderSample());
			
			if (lstPortalOrderDetails.size() > 0) {
				updatequery = "update externalorder set " + " dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nexternalordercode =" + objOrderDetail.getNexternalordercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updatequery);

			} else {
				updatequery = "update externalorder set " + " dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nexternalordercode =" + objOrderDetail.getNexternalordercode() + "and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(updatequery);
			}
			return getExternalOrder(objOrderDetail.getNexternalordercode(), userInfo);

		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRECEIVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo userInfo) throws Exception {
		String strQuery = " SELECT    pc.*,max(tgsp.nallottedspeccode) nallottedspeccode "
						+ " FROM   productcategory pc, " + "       treetemplatemanipulation ttm, "
						+ "	   testgroupspecification tgsp " + " WHERE  pc.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode "
						+ " and pc.nproductcatcode=ttm.nproductcatcode " + " and ttm.nsampletypecode="
						+ Enumeration.SampleType.CLINICALSPEC.getType()
						+ " and tgsp.napprovalstatus in (select napprovalstatuscode from " + " approvalconfigrole acr "
						+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno =  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.napprovalconfigcode =  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ) and tgsp.nstatus = ttm.nstatus and ttm.nstatus = pc.nstatus " + " and pc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by pc.nproductcatcode ;";
		return new ResponseEntity<Object>((List<ProductCategory>) jdbcTemplate.query(strQuery, new ProductCategory()),
				HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception {

		final String strQuery = " SELECT    c.*,ttm.nproductcatcode" + " FROM   component c, "
								+ " testgroupspecsampletype tgss,testgroupspecification tgsp, "
								+ "	treetemplatemanipulation ttm " + " WHERE   c.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgss.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgsp.nallottedspeccode=tgss.nallottedspeccode "
								+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode "
								+ " and c.ncomponentcode=tgss.ncomponentcode "
								+ " and tgsp.napprovalstatus in (select napprovalstatuscode from " + " approvalconfigrole acr "
								+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno =   "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.napprovalconfigcode =  "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and tgss.nstatus = tgsp.nstatus "
								+ " and tgsp.nstatus = ttm.nstatus and ttm.nstatus = c.nstatus and c.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ttm.nsampletypecode="
								+ Enumeration.SampleType.CLINICALSPEC.getType() + " group by c.ncomponentcode,ttm.nproductcatcode;";

		return new ResponseEntity<>((List<Component>) jdbcTemplate.query(strQuery, new Component()),
				HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestMaster(final UserInfo objUserInfo) throws Exception {
		Map<String,Object> returnMap = new HashMap<>();
		
		String sQuery = " SELECT tgt.ntestgrouptestcode,ttm.nproductcatcode,tgss.ncomponentcode,tgsp.nallottedspeccode,max(ct.scontainertype) scontainertype,"
						+ " max(ct.ncontainertypecode) ncontainertypecode,tm.* ,tm.nstatus,ttm.ntemplatemanipulationcode  FROM "
						+ " testgroupspecsampletype tgss, testgroupspecification tgsp, "
						+ " treetemplatemanipulation ttm, testgrouptest tgt, testmaster tm "
					//	+ " left join testpackagetest tpt on  tpt.ntestcode = tm.ntestcode and tpt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					//	+ " left join testpackage tp on tp.ntestpackagecode = tpt.ntestpackagecode and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " ,containertype ct "
						+ " WHERE tm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgss.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgsp.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ttm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgt.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgsp.nallottedspeccode=tgss.nallottedspeccode "
						+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode  "
						+ " and tgt.nspecsampletypecode=tgss.nspecsampletypecode "
						+ " and  tgt.ntestcode=tm.ntestcode and ct.ncontainertypecode = tgt.ncontainertypecode "
						+ " and tgsp.napprovalstatus in (select napprovalstatuscode from " + " approvalconfigrole acr "
						+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.napprovalconfigcode =  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " and ttm.nsampletypecode="
						+ Enumeration.SampleType.CLINICALSPEC.getType()
						+ " and tm.nstatus = tgss.nstatus and tgss.nstatus = tgsp.nstatus "
						+ " and tgsp.nstatus = ttm.nstatus and ttm.nstatus = tgt.nstatus and tgt.nstatus = ct.nstatus "
						+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " GROUP  BY tm.ntestcode,tgss.ncomponentcode,ttm.nproductcatcode,tgsp.nallottedspeccode,tgt.ntestgrouptestcode,ttm.ntemplatemanipulationcode" + 
						"   order by tgss.ncomponentcode,ttm.nproductcatcode asc;";
		
		returnMap.put("TestMaster", (List<TestMaster>) jdbcTemplate.query(sQuery, new TestMaster()));
		returnMap.put("TestPackage", getTestMasterPackage(objUserInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	
	public ResponseEntity<Object> getTestMasterPackage(final UserInfo objUserInfo) throws Exception {
		Map<String,Object> returnMap = new HashMap<>();
		
		final String sQuery=" SELECT tgt.*, COALESCE (tp.ntestpackagecode,-1) ntestpackagecode, COALESCE(tp.stestpackagename,'NA') stestpackagename,tgt.ntestgrouptestcode,ttm.nproductcatcode,tgss.ncomponentcode,tgsp.nallottedspeccode,max(ct.scontainertype) scontainertype, "
						+ " max(ct.ncontainertypecode) ncontainertypecode,ttm.ntemplatemanipulationcode FROM  testgroupspecsampletype tgss, testgroupspecification tgsp,  treetemplatemanipulation ttm, "
						+ " testgrouptest tgt,testpackage tp,containertype ct WHERE tgss.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tgsp.nstatus=  "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttm.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgt.nstatus=  "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tp.ntestpackagecode = tgt.ntestpackagecode   and tgsp.nallottedspeccode=tgss.nallottedspeccode  and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode  "
						+ " and tgt.nspecsampletypecode=tgss.nspecsampletypecode  and ct.ncontainertypecode = tgt.ncontainertypecode and tgsp.napprovalstatus in (select napprovalstatuscode from  approvalconfigrole acr  "
						+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode  and acr.nlevelno = 1 and acr.napprovalconfigcode =  1 "
						+ "	and acr.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")  and ttm.nsampletypecode="+Enumeration.SampleType.CLINICALSPEC.getType() +" and  tgss.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgss.nstatus = tgsp.nstatus "
						+ "	and tgsp.nstatus = ttm.nstatus and ttm.nstatus = tgt.nstatus and tgt.nstatus = ct.nstatus  and ct.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "	GROUP  BY tgss.ncomponentcode,ttm.nproductcatcode,tgsp.nallottedspeccode, tgt.ntestgrouptestcode,ttm.ntemplatemanipulationcode, tp.ntestpackagecode,tp.stestpackagename order by tgss.ncomponentcode,ttm.nproductcatcode asc;";
		return new ResponseEntity<>((List<TestMaster>) jdbcTemplate.query(sQuery, new TestMaster()), HttpStatus.OK);
	}

	public ResponseEntity<Object> getDiagnosticcase(final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();

		final String strQuery = "select ndiagnosticcasecode,coalesce(g.jsondata->'sdiagnosticcasename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sdiagnosticcasename'->>'en-US') as sdiagnosticcasename "
				+ " from diagnosticcase g;";

		final List<Map<String, Object>> lstdiagnosticcaseQuery = jdbcTemplate.queryForList(strQuery);

		returnMap.put("Diagnosticcase", lstdiagnosticcaseQuery);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getPriority(final UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();

		final String strQuery = "select nprioritycode,coalesce(g.jsondata->'spriorityname'->>'"
								+ userInfo.getSlanguagetypecode() + "'," + " g.jsondata->'spriorityname'->>'en-US') as spriorityname "
								+ " from priority g where " + " g.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nprioritycode > 0";
		final List<Map<String, Object>> priorityList = jdbcTemplate.queryForList(strQuery);

		returnMap.put("Priority", priorityList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getOrderByType(final int norderTypeCode, Integer nexternalordercode,
			UserInfo userInfo) throws Exception {
	
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String conditionalQuery = "";
		if (nexternalordercode != null)
			conditionalQuery = " and pod.nexternalordercode=" + nexternalordercode;

		String strQuery = "select pod.nexternalordercode,pod.sexternalorderid,pod.sorderseqno, pod.nproductcatcode, "
				+ " pod.nproductcode, pod.ninstitutioncode, pod.ninstitutionsitecode, "
				+ " ins.sinstitutionname, inst.sinstitutionsitename, pod.nallottedspeccode,"
				+ " to_char((pod.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime()+ "') as dcollectiondate, "
				+ " coalesce(dc.jsondata->'sdiagnosticcasename'->>'" + userInfo.getSlanguagetypecode()
				+ "',dc.jsondata->'sdiagnosticcasename'->>'en-US') as sdiagnosticcasename, "
				+ " pod.nordertypecode, pod.ndiagnosticcasecode, pod.ngendercode, pod.spatientid, pod.jsondata,pc.sproductcatname,p.sproductname,pod.spatientid,pod.ssubmittercode, "
				+ "pod.nsitecode, pod.nstatus ,coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
				+ "',g.jsondata->'sgendername'->>'en-US') as sgendername, "
				+" coalesce(eot.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',eot.jsondata->'sdisplayname'->>'en-US') as sexternalordertypename ,"
				+ "concat(pm.sfirstname,' ',pm.slastname) as ssubmittername,"
				+ " pm.sfirstname spatientfirstname,pm.slastname as spatientlastname, "
				+ " s.ssubmitterid,ind.sinstitutiondeptname,ind.sinstitutiondeptcode, "
				+ "   pm.sfirstname||' '||pm.slastname as spatientname," 
				+ " s.sfirstname  as ssubmitterfirstname,"
				+ " s.slastname as ssubmitterlastname," 
				+ " s.semail as ssubmitteremail,"
				+ "   pm.sfathername as sfathername," 
				+ "   pm.sfathername as spatientfathername,"
				//ALPD-5703--Added by Vignesh R(08-05-2025)-->The instant date was not fetched properly.I have corrected this issue by changing the string format.
				+"  to_char(pm.ddob,'"+userInfo.getSpgsitedatetime()+"') as ddob,"
				//+ "   pm.ddob as ddob,"
				+ " pm.ddob as sdob," 
				+ "   c.scountryname as scountryname,"
				+ "  insd.sdistrictname as sdistrictname," 
				+ "  pm.spostalcode as spostalcode,"
				+ "  pm.sphoneno as sphoneno," 
				+ "  pm.smobileno as smobileno," 
				+ "  pm.semail as semail,"
				+ "  pm.spassportno as spassportno," 
				+ "  pm.sexternalid as sexternalid,"
				+ "  pm.sstreet as sstreet,"
				+ "  pm.shouseno as shouseno,"
				+ " instc.sinstitutioncatname  as sinstitutioncatname, "
				+ "  rg.sregionname as sregionname,ind.sinstitutiondeptname as sinstitutiondeptname ,"
				+ " ind.sinstitutiondeptcode as sinstitutiondeptcode, "
				+ " case when pm.sstreet='' then '-' else pm.sstreet end||','||case when pm.shouseno='' then '-' else pm.shouseno end ||','||case when pm.sflatno='' then '-' else pm.sflatno end||','||pm.scityname as spermanentadd, "
				+ " case when pm.sstreettemp='' then '-' else pm.sstreettemp end||','||case when pm.shousenotemp='' then '-' else pm.shousenotemp end ||','||case when pm.sflatnotemp='' then '-' else pm.sflatnotemp end||','||pm.scityname as scurrentadd, "
				+ " insd.sdistrictname||','||inst.scityname as sinsdistrictcity, "
				+ " insd.ndistrictcode as ndistrictcode ,s.stelephone submittertelephone,ins.sinstitutioncode,ins.ninstitutioncatcode,eot.nexternalordertypecode "
				+ "  from externalorder pod ,patientmaster pm, "
				+ " country c,city ctyn,city ctemp,city insc,district insd,district dst,region rg,"
				+ " gender g , productcategory pc,product p,institution ins,institutionsite inst,diagnosticcase dc,submitter s,"
				+ " institutiondepartment ind, institutioncategory instc,externalordertype eot "
				+ " where  pc.nproductcatcode =pod.nproductcatcode "
				+ " and p.nproductcode =pod.nproductcode "
				+ " and pm.spatientid =pod.spatientid "
				+ " and pm.ncountrycode =c.ncountrycode " 
				+ " and pm.ncitycode =ctyn.ncitycode "
				+ " and pm.ncitycodetemp =ctemp.ncitycode " 
				+ " and inst.ncitycode =insc.ncitycode "
				+ " and inst.ndistrictcode =insd.ndistrictcode "
				+ " and pm.ndistrictcode =dst.ndistrictcode "
				+ "  and rg.nregioncode=pm.nregioncode" 
				+ " and g.ngendercode = pod.ngendercode "
				+ " and pod.ninstitutioncode=ins.ninstitutioncode "
				+ " and pod.nexternalordertypecode=eot.nexternalordertypecode "
				+ " and ins.ninstitutioncatcode=instc.ninstitutioncatcode "
				+ " and ins.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and pod.ninstitutioncode=inst.ninstitutioncode and inst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ins.ninstitutioncode=inst.ninstitutioncode and  inst.ninstitutionsitecode=pod.ninstitutionsitecode "
				+ " and pod.ndiagnosticcasecode=dc.ndiagnosticcasecode and dc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and pod.ssubmittercode=s.ssubmittercode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and s.ninstitutiondeptcode=ind.ninstitutiondeptcode and ind.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pod.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inst.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ind.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ctyn.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and instc.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
			//	+ " and pod.nordertypecode=" + norderTypeCode + ""
				+ " " + conditionalQuery + " order by pod.nexternalordercode desc";

		List<ExternalOrder> lstPortalOrder = jdbcTemplate.query(strQuery, new ExternalOrder());
		// if(nexternalOrderCode == 0) {
		if (lstPortalOrder.isEmpty()) {
			outputMap.put("ExternalOrder", null);
			outputMap.put("selectedExternalOrder", null);
			outputMap.put("ExternalOrderTube", null);
			outputMap.put("ExternalOrderTest", null);
		} else {
			outputMap.put("ExternalOrder", lstPortalOrder);
			outputMap.put("selectedExternalOrder",
					lstPortalOrder.isEmpty() ? lstPortalOrder : lstPortalOrder.get(lstPortalOrder.size() - 1));

			if (!lstPortalOrder.isEmpty()) {
				final int nportalorderCode = lstPortalOrder.get(lstPortalOrder.size() - 1).getNexternalordercode();
				List<ExternalOrderSample> lstPortalOrderContainer = getExternalOrderSample(nportalorderCode, userInfo);
				List<ExternalOrderTest> lstPortalOrderTest = getExternalOrderTest(nportalorderCode, userInfo);
				outputMap.put("ExternalOrderTest", lstPortalOrderTest);
				if (!lstPortalOrderContainer.isEmpty()) {
					outputMap.put("selectedExternalOrderTube",
							lstPortalOrderContainer.get(lstPortalOrderContainer.size() - 1));

				}

				outputMap.put("ExternalOrderTube", lstPortalOrderContainer);
			}
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getTestPackageTest(final UserInfo userInfo) throws Exception {

		final String strQuery = "select tpt.ntestpackagetestcode, tpt.ntestcode, tpt.ntestpackagecode, "
								+ " tpt.ndefaultstatus, tpt.nsitecode, tpt.nstatus, "
								+ "tm.ntestcategorycode, tm.stestname, tm.stestsynonym, tm.sshortname, tm.sdescription, tm.ncost, tm.nchecklistversioncode,"
								+ " tm.naccredited, tm.ntrainingneed, tm.stestplatform, tm.ntat, tm.ntatperiodcode, tm.ninterfacetypecode, "
								+ "tm.ntransactionstatus, to_char(tpt.dmodifieddate, '"
								+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate, tp.stestpackagename "
								+ " from testpackagetest tpt, testmaster tm, testpackage tp where tpt.ntestcode=tm.ntestcode and "
								+ "tpt.ntestpackagecode=tp.ntestpackagecode";

		return new ResponseEntity<>(jdbcTemplate.queryForList(strQuery), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestGroupProfile(final UserInfo objUserInfo) throws Exception {
		String sQuery = " SELECT  tm.nstatus,max(ttm.ntemplatemanipulationcode) ntemplatemanipulationcode,tgsp.napprovalstatus, "
						+ " ttm.nproductcatcode,ttm.sleveldescription,tgsp.nallottedspeccode,max(tgsp.sspecname) sspecname "
						+ " FROM   testmaster tm, " + "    testgroupspecsampletype tgss, "
						+ "	   testgroupspecification tgsp, " + " treetemplatemanipulation ttm, "
						+ "	   testgrouptest tgt, containertype ct " + " WHERE tm.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgss.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgsp.nallottedspeccode=tgss.nallottedspeccode "
						+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode  "
						+ " and tgt.nspecsampletypecode=tgss.nspecsampletypecode "
						+ " and  tgt.ntestcode=tm.ntestcode and ct.ncontainertypecode = tgt.ncontainertypecode "
						+ " and tgsp.napprovalstatus in ((select napprovalstatuscode from " + " approvalconfigrole acr "
						+ " where acr.napproveconfversioncode = tgsp.napproveconfversioncode " + " and acr.nlevelno = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.napprovalconfigcode =  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),"
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") " + " and ttm.nsampletypecode="
						+ Enumeration.SampleType.CLINICALSPEC.getType()
						+ " and tm.nstatus = tgss.nstatus and tgss.nstatus = tgsp.nstatus "
						+ " and tgsp.nstatus = ttm.nstatus and ttm.nstatus = tgt.nstatus and tgt.nstatus = ct.nstatus "
						+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " GROUP  BY ttm.nproductcatcode,ttm.sleveldescription,tgsp.nallottedspeccode,tm.nstatus order by ttm.nproductcatcode asc;";
		return new ResponseEntity<>(
				(List<TreeTemplateManipulation>) jdbcTemplate.query(sQuery, new TreeTemplateManipulation()),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveExternalOrderById(int nexternalordercode, UserInfo userInfo)
			throws Exception {
		
		String strQuery = "select pod.nexternalordercode,pod.sexternalorderid,pod.sorderseqno, pod.nproductcatcode, "
						+ " pod.nproductcode, pod.ninstitutioncode, pod.ninstitutionsitecode,pod.nallottedspeccode, "
						+ " ins.sinstitutionname, inst.sinstitutionsitename, "
						+ " coalesce(dc.jsondata->'sdiagnosticcasename'->>'" + userInfo.getSlanguagetypecode()
						+ "',dc.jsondata->'sdiagnosticcasename'->>'en-US') as sdiagnosticcasename, "
						+ "pod.ndiagnosticcasecode, pod.ngendercode, pod.spatientid, pod.jsondata,pc.sproductcatname,p.sproductname,pod.spatientid,pod.ssubmittercode, "
						+ "pod.nsitecode, pod.nstatus ,coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
						+ "',g.jsondata->'sgendername'->>'en-US') as sgendername, "
						//+ "concat(pm.sfirstname,' ',pm.slastname) as ssubmittername,"
						+ " pm.sfirstname spatientfirstname,pm.slastname as spatientlastname, "
						+ " s.ssubmitterid,ind.sinstitutiondeptname,ind.sinstitutiondeptcode, "
						+ "   pm.sfirstname||' '||pm.slastname as spatientname," + " s.sfirstname  as ssubmitterfirstname,"
								+ "s.sfirstname||' '||s.slastname||'('||s.ssubmitterid ||')'  as ssubmittername,"
						+ " s.slastname as ssubmitterlastname," + " s.semail as ssubmitteremail,"
						+ "   pm.sfathername as sfathername,pm.sfirstname,pm.slastname,pm.sage," + "   pm.ddob as ddob,"
						+ "   to_char(pm.ddob ,'" + userInfo.getSsitedate() + "') as sdob,"
						+ "   to_char((pod.jsondata->>'dcollectiondate')::timestamp,'" + userInfo.getSpgsitedatetime() + "') as dcollectiondate,"
						+ "   c.scountryname as scountryname, (pod.jsondata->>'ninstitutiondistrictcode')::int as ndistrictcode, (pod.jsondata->>'sinstitutiondistrictname') as sdistrictname, " 
						//+ "  dst.sdistrictname as sdistrictname,"+ "  dst.ndistrictcode as ndistrictcode,"
						+ "  pm.spostalcode as spostalcode," + "  pm.sphoneno as sphoneno," + "  pm.smobileno as smobileno,"
						+ "  pm.semail as semail," + "  pm.spassportno as spassportno," + "  pm.sexternalid as sexternalid,"
						+ "  pm.sstreet as sstreet," + "  pm.shouseno as shouseno,"
						+ "  rg.sregionname as sregionname,ind.sinstitutiondeptname as sinstitutiondeptname , ind.sinstitutiondeptcode as sinstitutiondeptcode,ins.ninstitutioncatcode,insc.sinstitutioncatname as sinstitutioncatname,"
						+ " coalesce(eot.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
						+ "',eot.jsondata->'sdisplayname'->>'en-US') as sexternalordertypename ,eot.nexternalordertypecode "
						+ "from externalorder pod , productcategory pc,product p,patientmaster pm,externalordertype eot, "
						+ " country c,city ctyn,district dst,region rg,"
						+ " gender g ,institution ins ,institutioncategory insc, institutionsite inst,diagnosticcase dc,submitter s,institutiondepartment ind where  pc.nproductcatcode =pod.nproductcatcode "
						+ "and p.nproductcode =pod.nproductcode " + " and pm.spatientid =pod.spatientid "
						+ " and pm.ncountrycode =c.ncountrycode " + " and pm.ncitycode =ctyn.ncitycode "
						+ " and pm.ndistrictcode =dst.ndistrictcode " + "  and rg.nregioncode=pm.nregioncode"
						+ " and g.ngendercode = pod.ngendercode and pod.nexternalordertypecode=eot.nexternalordertypecode "
						+ " and pod.ninstitutioncode=ins.ninstitutioncode and ins.ninstitutioncatcode=insc.ninstitutioncatcode and ins.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and pod.ninstitutioncode=inst.ninstitutioncode and inst.ninstitutionsitecode=pod.ninstitutionsitecode and inst.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and ins.ninstitutioncode=inst.ninstitutioncode "
						+ " and pod.ndiagnosticcasecode=dc.ndiagnosticcasecode and dc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and pod.ssubmittercode=s.ssubmittercode and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and s.ninstitutiondeptcode=ind.ninstitutiondeptcode and ind.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and pod.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ins.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inst.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ind.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ctyn.nstatus=  "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nexternalordercode ="
						+ nexternalordercode + " order by pod.nexternalordercode";

		final ExternalOrder eo = (ExternalOrder) jdbcUtilityFunction.queryForObject(strQuery, ExternalOrder.class, jdbcTemplate);
		return new ResponseEntity<>(eo, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateExternalOrder(ExternalOrder externalorder, int needfilter, UserInfo userInfo)
			throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		final String allottedSpecString = " SELECT   max(tgsp.nallottedspeccode) nallottedspeccode "
									+ " FROM   productcategory pc, " + "       treetemplatemanipulation ttm, "
									+ "	   testgroupspecification tgsp " + " WHERE  pc.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus=  "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsp.nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgsp.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode "
									+ " and pc.nproductcatcode=ttm.nproductcatcode " + " and ttm.nsampletypecode="
									+ Enumeration.SampleType.CLINICALSPEC.getType() + " and ttm.nproductcatcode="
									+ externalorder.getNproductcatcode() + " and tgsp.napprovalstatus in (select napprovalstatuscode from "
									+ " approvalconfigrole acr " + " where acr.napproveconfversioncode = tgsp.napproveconfversioncode "
									+ " and acr.nlevelno =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acr.napprovalconfigcode =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " );";

		TestGroupSpecification testGroup = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(allottedSpecString,
				TestGroupSpecification.class, jdbcTemplate);
		externalorder.setNallottedspeccode(testGroup.getNallottedspeccode());
		JSONObject jsonObject = new JSONObject(externalorder.getJsondata());
		Patient objPatient = mapper.readValue(jsonObject.toString(), Patient.class);
		Map<String, Object> patientMap = new HashMap<>();

		final String sQuery = "select * from patientmaster where" + " spatientid = '"
				+ objPatient.getSpatientid() + "'" + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<Patient> existingPatient = jdbcTemplate.query(sQuery, new Patient());
		String spatientid = "";

		if (existingPatient.size() == 0) {
			patientMap = patientDAO.savePatient(objPatient, userInfo);
			spatientid = patientMap.containsKey("spatientid") ? (String) patientMap.get("spatientid") : null;
		} else {
			spatientid = existingPatient.get(0).getSpatientid();
		}
		if(needfilter==2) {
			
			String query="select i.scityname from city c,institutionsite i where c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+" and c.ncitycode=i.ncitycode and i.ninstitutionsitecode="+externalorder.getNinstitutionsitecode();
			
			City c=(City) jdbcUtilityFunction.queryForObject(query, City.class, jdbcTemplate);
			
			
			
			 query="select pm.* from patientmaster pm,city cp,city ct where pm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			 +" and pm.ncitycode=cp.ncitycode and pm.ncitycodetemp=ct.ncitycode and pm.spatientid='"+spatientid+"'";
			
			 Patient eo=(Patient) jdbcUtilityFunction.queryForObject(query, Patient.class, jdbcTemplate);
			 
			
			jsonObject.put("sinstitutioncityname", c.getScityname());
			
			jsonObject.put("spatientid", eo.getSpatientid());
			jsonObject.put("sfirstname", eo.getSfirstname());
			jsonObject.put("slastname", eo.getSlastname());
			jsonObject.put("sfathername", eo.getSfathername());
			//jsonObject.put("sdob", c.getScityname());
			jsonObject.put("spostalcode",eo.getSpostalcode());
			jsonObject.put("sstreet", eo.getSstreet());
			jsonObject.put("shouseno", eo.getShouseno());
			jsonObject.put("sflatno", eo.getSflatno());
			jsonObject.put("smobileno", eo.getSmobileno());
			jsonObject.put("srefid",eo.getSrefid());
			jsonObject.put("sphoneno", eo.getSphoneno());
			jsonObject.put("spassportno",eo.getSpassportno());
			jsonObject.put("scityname", eo.getScityname());
			jsonObject.put("sstreettemp", eo.getSstreettemp());
			jsonObject.put("shousenotemp", eo.getShousenotemp());
			
			jsonObject.put("sflatnotemp", eo.getSflatno());
			jsonObject.put("scitynametemp", eo.getScitynametemp());
			jsonObject.put("sexternalid", eo.getSexternalid());
			jsonObject.put("ninstitutiondistrictcode", (int)externalorder.getNdistrictcode());
			jsonObject.put("ninstitutioncatcode", (int)externalorder.getNinstitutioncatcode());

		}
		String query = "update externalorder set nproductcatcode=" + externalorder.getNproductcatcode()
						+ ",nproductcode= " + externalorder.getNproductcode() + ",ngendercode=" + externalorder.getNgendercode()
						+ "," + " ninstitutioncode=" + externalorder.getNinstitutioncode() + ",ninstitutionsitecode="
						+ externalorder.getNinstitutionsitecode() + "," + " ndiagnosticcasecode="
						+ externalorder.getNdiagnosticcasecode() + ",spatientid='" + jsonObject.get("spatientid")
						+ "',ssubmittercode='" + externalorder.getSsubmittercode() + "'," + " nusercode="
						+ userInfo.getNusercode() + " ,nallottedspeccode=" + externalorder.getNallottedspeccode()
						+ ", nexternalordertypecode="+ externalorder.getNexternalordertypecode()
						+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',jsondata='"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "' where nexternalordercode="
						+ externalorder.getNexternalordercode();
		jdbcTemplate.execute(query);
		return getOrderByType(externalorder.getNordertypecode(),
				needfilter == 2 ? externalorder.getNexternalordercode() : null, userInfo);
	}

	
	public ResponseEntity<Object> updatePortalOrderStatus(final String sorderseqno,final String sordersampleno, final short ntransactionstatus,final UserInfo userinfo) 
			throws Exception {
		
		Map<String,Object> returnMap = new HashMap<>();
		String ordernoArray = Arrays.stream(sorderseqno.split(",")).map(str -> "'"+ stringUtilityFunction.replaceQuote(str)+ "'").collect(Collectors.joining(","));
		String updateQuery = "";
		updateQuery= updateQuery+"update externalorder set ntransactionstatus = "+ntransactionstatus+",dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userinfo)+"' "
				+ " where sorderseqno in ("+ordernoArray+") and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"; ";
		if((int) ntransactionstatus == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()) {
			updateQuery = updateQuery + "update externalordersample set ntransactionstatus = "+ ntransactionstatus
					+ " where nexternalordercode=(select nexternalordercode from externalorder where sorderseqno in ("
					+ ordernoArray+"));";
		}
		
		if(sordersampleno != "")
		{
			String orderSampleArray = Arrays.stream(sordersampleno.split(",")).map(str -> "'"+ stringUtilityFunction.replaceQuote(str)+ "'").collect(Collectors.joining(","));
			updateQuery= updateQuery+"update externalordersample set ntransactionstatus = "+ntransactionstatus+",dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userinfo)+"' "
					+ " where sexternalsampleid in ("+orderSampleArray+") and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
					+ " ntransactionstatus <> "+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+"; ";
		}	
		jdbcTemplate.execute(updateQuery);
		
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		
		return new ResponseEntity<> (returnMap,HttpStatus.OK);
	}
	
	
	@Override
	public ResponseEntity<Object> getDraftExternalOrderDetails(final String sexternalorderid,final UserInfo userinfo,final int nexteralordertypecode) 
			throws Exception {
		
		Map<String,Object> returnMap = new HashMap<>();
		String alertString = "";
		
		final String query = "select eos.nexternalordersamplecode,eo.nexternalordercode,eo.sexternalorderid,eos.sexternalsampleid,"
							+ " eo.sorderseqno, eo.nexternalordertypecode from externalorder eo, externalordersample eos " + 
							" where eo.nexternalordercode = eos.nexternalordercode" + 
							" and eo.nsitecode= eos.nsitecode" + 
							" and eo.nstatus = eos.nstatus" + 
							" and eos.sexternalsampleid = '"+stringUtilityFunction.replaceQuote(sexternalorderid.trim())+"'"+
							" and eo.nordertypecode = "+Enumeration.OrderType.EXTERNAL.getOrderType()+"" + 
							" and eos.ntransactionstatus = "+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+"" + 
							" and eo.nsitecode= "+userinfo.getNtranssitecode()+"" + 
							" and eo.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"" + 
							" order by eo.nexternalordercode desc, eos.nexternalordersamplecode asc";
	
		List<ExternalOrder> lstPortalOrder = jdbcTemplate.query(query, new ExternalOrder());
		List<ExternalOrder> lstExternalOrder = lstPortalOrder.stream().filter(lstExternal -> lstExternal.getNexternalordertypecode() == nexteralordertypecode).collect(Collectors.toList());
		String externalOrderTypeCode = lstPortalOrder.stream().map(i->String.valueOf(i.getNexternalordertypecode())).collect(Collectors.joining(", "));
		if(lstPortalOrder.isEmpty() && lstExternalOrder.isEmpty()) {
			alertString = sexternalorderid+" "+commonFunction.getMultilingualMessage("IDS_NOTAVAILABLE", userinfo.getSlanguagefilename());
			returnMap.put("ExternalOrder", new ArrayList<>());
		} else {
			if(lstExternalOrder.isEmpty()) {
				List<String> lstExternalOrderType = jdbcTemplate.queryForList("select coalesce(jsondata->'sdisplayname'->>'"
													+userinfo.getSlanguagetypecode()+"', jsondata->'sdisplayname'->>'en-US') sexternalordertypename"
													+ " from externalordertype where nexternalordertypecode in ("+ externalOrderTypeCode+") and nstatus="
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
				String strExternalOrderTypeName = lstExternalOrderType.stream().collect(Collectors.joining(", "));
				returnMap.put("ExternalOrder", new ArrayList<>());
				alertString = sexternalorderid+" "+commonFunction.getMultilingualMessage("IDS_AVAILABLEIN", userinfo.getSlanguagefilename())+ " "+ strExternalOrderTypeName;
			} else {
				returnMap.put("ExternalOrder", lstExternalOrder);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}
		}
		returnMap.put("isRecordNotAvailable", alertString);
		return new ResponseEntity<> (returnMap,HttpStatus.OK);
	}
	
	@SuppressWarnings("serial")
	@Override
	public ResponseEntity<Object> onUpdateCancelExternalOrder(final String sexternalordercode,final String sexternalordersamplecode,final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception{
		
		if(sexternalordersamplecode != "") 
		{
			
			Map<String,Object> returnMap = new HashMap<>();
			final List<String> multilingualIDList  = new ArrayList<>();
			
			final String auidtquery = "select eos.nexternalordersamplecode,eos.nexternalordercode,eos.sexternalsampleid,eos.ntransactionstatus ,eo.ntransactionstatus as norderstatus,eo.sexternalorderid,eo.nexternalordertypecode, eo.sorderseqno  "
					+ " from externalordersample eos,externalorder eo where eos.nexternalordersamplecode in ("+sexternalordersamplecode+") and   eos.nexternalordercode=eo.nexternalordercode"
							+ " and eos.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"and eo.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
			final List<ExternalOrderSample> lstbeforeupdate = jdbcTemplate.query(auidtquery, new ExternalOrderSample());
			
			
			if(!lstbeforeupdate.isEmpty()) {
				if(lstbeforeupdate.get(0).getNexternalordertypecode()==Enumeration.ExternalOrderType.PREVENTTB.getExternalOrderType()) {
					
					final String PreventTb=" select * from externalordersample where   nexternalordercode="+lstbeforeupdate.get(0).getNexternalordercode()+""
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					List<ExternalOrderSample> lstSample=jdbcTemplate.query(PreventTb,new ExternalOrderSample());
					
					
					if(!lstSample.isEmpty()) {
						
						lstSample=lstSample.stream().filter(x->x.getNtransactionstatus()!=Enumeration.TransactionStatus.CANCELED.gettransactionstatus() )
								.filter(x->!sexternalordersamplecode.contains(String.valueOf(x.getNexternalordersamplecode()))).collect(Collectors.toList()); 
						
						if(lstSample.isEmpty()) {
							
							String orderId=(String) lstbeforeupdate.get(0).getSexternalorderid();
							RestTemplate restTemplate = new RestTemplate();
							final Map<String,Object> sendData=new HashMap();
							sendData.put("orderid", orderId);
							sendData.put("statusflag", "Cancel");
							//URI uri = new URI("https://tajik.preventtb.org/service/api/lims/cancelOrder");
							URI uri = new URI("http://localhost:8036/QuaLIS/registration/check");
							ResponseEntity<Object> res = restTemplate.postForEntity(uri,
									sendData, Object.class);
							final Map<String, Object> respo = (Map<String, Object>) res.getBody();
							if(res.getStatusCode()==HttpStatus.OK) {
								if(respo.containsKey("Status")) {
									
									if(!("SUCCESS").equals(respo.get("Status"))) {
										return new ResponseEntity<> (commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDCANCELORDER",
												userinfo.getSlanguagefilename())+" "+respo.get("Status"),HttpStatus.CONFLICT);
									}else {
										returnMap.put("PreventTb", respo.get("Status"));
									}	
								}
							}else {
								
								return new ResponseEntity<> (commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDCANCELORDERFAIL",
										userinfo.getSlanguagefilename()),HttpStatus.CONFLICT);
							}
							
						}						
					}	
					
				}				
			}

				
			
			final String query = "update externalordersample set ntransactionstatus  = "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+" where nexternalordersamplecode in ("+sexternalordersamplecode+") " + 
								" and ntransactionstatus = "+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			
			jdbcTemplate.execute(query);
			
			final String orderUpdateQuery = "DO $$ " + 
											"    BEGIN " + 
											"        IF EXISTS" + 
											"            ( select a.nexternalordercode from " + 
											" (select nexternalordercode,count(*) from externalordersample where nexternalordercode in ("+sexternalordercode+") group by  nexternalordercode)a," + 
											" (select nexternalordercode,count(*) from externalordersample where nexternalordercode in ("+sexternalordercode+") " + 
											" and ntransactionstatus = "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+" group by  nexternalordercode)b " + 
											" where a.nexternalordercode = b.nexternalordercode" + 
											" and a.count = b.count group by a.nexternalordercode )" + 
											"        THEN" + 
											"           update externalorder set ntransactionstatus  = "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+" where nexternalordercode = any (select a.nexternalordercode from " + 
											" (select nexternalordercode,count(*) from externalordersample where nexternalordercode in ("+sexternalordercode+") group by  nexternalordercode)a," + 
											" (select nexternalordercode,count(*) from externalordersample where nexternalordercode in ("+sexternalordercode+") " + 
											" and ntransactionstatus = "+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+" group by  nexternalordercode)b " + 
											" where a.nexternalordercode = b.nexternalordercode" + 
											" and a.count = b.count group by a.nexternalordercode);" + 
											"        END IF ;" + 
											"    END" + 
											"   $$ ;";
			
			jdbcTemplate.execute(orderUpdateQuery);
			
			
			
			final List<ExternalOrderSample> lstafterupdate =  jdbcTemplate.query(auidtquery, new ExternalOrderSample());
			multilingualIDList.add("IDS_CANCELEXTERNALORDERSAMPLEID");	
			
			auditUtilityFunction.fnInsertAuditAction(lstafterupdate, 2, lstbeforeupdate, multilingualIDList, userinfo);	
			
			List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
			final String strCancelledOrder = "select * from externalorder where nexternalordercode in ("+sexternalordercode+") and ntransactionstatus ="+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+" ";
			final List<ExternalOrder> lstCancelledOrder = jdbcTemplate.query(strCancelledOrder, new ExternalOrder());
			final String[] sexternalorderCode = sexternalordercode.split(",");
			
			Map<String,Object> prevent=new HashMap();
			
			for (String nexternalorderCode : sexternalorderCode) {
				lstCancelledOrder.stream().forEach(lst -> {
					if(lst.getNexternalordercode() == Integer.parseInt(nexternalorderCode)) {
						Map<String, Object> objMap = new HashMap<String, Object>() {{put("serialnumber",lst.getSorderseqno());} {put("statuscode",Enumeration.TransactionStatus.CANCELED.gettransactionstatus());}};
						objlst.add(objMap);
					}
				});
			}
			List<Map<String, Object>> objSampleLst = new ArrayList<Map<String, Object>>();
			String sexternalSampleId = lstbeforeupdate.stream().map(x -> x.getSexternalsampleid().toString()).collect(Collectors.joining(","))	;	
			final String[] sexternalOrderSampleCode = sexternalSampleId.split(",");
			for(ExternalOrderSample obj : lstbeforeupdate) {
				Map<String, Object> objMap = new HashMap<String, Object>() {{put("sexternalsampleid",obj.getSexternalsampleid());} {put("statuscode",Enumeration.TransactionStatus.CANCELED.gettransactionstatus());} {put("serialnumber",obj.getSorderseqno());}};
				objSampleLst.add(objMap);
			}
//			returnMap.put("PortalStatus", objlst);
			returnMap.put("SamplePortalStatus", objSampleLst);
			returnMap.put("methodUrl","UpdateSampleCancelStatus");
			returnMap.put("url",inputMap.get("url").toString());
			updateOrderSampleStatus(userinfo, returnMap);
			return new ResponseEntity<> (returnMap,HttpStatus.OK);
			
		}
		else
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEXTERNALSAMPLEID",userinfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<Object> outSourceTest(final RegistrationTest testInput,
			final int destinationSitecode, final int designTemplateMappingCode,
			final UserInfo userInfo) throws Exception
	{
		
		String queryString = "select * from registration r where r.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and r.nsitecode=" + userInfo.getNsitecode()
										+ " and r.npreregno=" + testInput.getNpreregno() + ";";
		final Registration registration = (Registration)jdbcTemplate.queryForObject(queryString, new Registration());
		
		queryString =  "select ra.* from registrationarno ra where ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nsitecode=" + userInfo.getNsitecode()
				+ " and ra.npreregno=" + testInput.getNpreregno() + ";";
		final RegistrationArno registrationARNO = (RegistrationArno)jdbcTemplate.queryForObject(queryString, new RegistrationArno());
		
		
		queryString = "select rs.* from registrationsample rs where rs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rs.nsitecode=" + userInfo.getNsitecode()
				+ " and rs.npreregno=" + testInput.getNpreregno() 
				+ " and rs.ntransactionsamplecode="+ testInput.getNtransactionsamplecode()  + ";";
		final RegistrationSample registrationSample = (RegistrationSample)jdbcTemplate.queryForObject(queryString, new RegistrationSample());
		
		
		queryString =  "select rsa.* from registrationsamplearno rsa where rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rsa.nsitecode=" + userInfo.getNsitecode()
				+ " and rsa.npreregno=" + testInput.getNpreregno()
				+ " and rsa.ntransactionsamplecode="+ testInput.getNtransactionsamplecode()  + ";";
		final RegistrationSampleArno registrationSampleARNO = (RegistrationSampleArno)jdbcTemplate.queryForObject(queryString, new RegistrationSampleArno());
		
	
		final String sequenceNoQuery = "select stablename,nsequenceno from seqnoregistration  where stablename "
										+ " in ('externalorder','externalordersample','externalordertest','registrationtesthistory') "
										+ " order by stablename ";

		final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequenceNoQuery, new SeqNoRegistration());
		
		Map<String, Object> returnMap = new HashMap();		
		returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		int externalOrderSeqNo = ((int) returnMap.get("externalorder")) + 1;
		int seqordertest = ((int) returnMap.get("externalordertest"))+1;
		int seqordersample = ((int) returnMap.get("externalordersample"))+1;
		int seqtesthistory = ((int) returnMap.get("registrationtesthistory"))+1;
		final JSONObject regJSON  = new JSONObject(registration.getJsondata());
		
		String orderId = "";
		int nexternalordercode = -1;
		if(regJSON.has("OrderIdData")) {
			orderId = regJSON.getString("OrderIdData");
			nexternalordercode = regJSON.getInt("OrderCodeData");
		}
		
		if (orderId.trim().length() > 0) {
			
			final String validateTestQry = " select * from externalordertest where "
					+ " npreregno =" + testInput.getNpreregno()
					+ " and ntransactionsamplecode="+testInput.getNtransactionsamplecode()
					+ " and ntransactiontestcode=" + testInput.getNtransactiontestcode()
					+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + destinationSitecode;
			
			final ExternalOrderTest externalOrderTest = (ExternalOrderTest) jdbcUtilityFunction.queryForObject(validateTestQry, ExternalOrderTest.class, jdbcTemplate);
			
			if(externalOrderTest == null) {
				
				final String validateSampleQry = " select * from externalordersample where "
												+ " npreregno =" + testInput.getNpreregno()
												+ " and ntransactionsamplecode="+testInput.getNtransactionsamplecode()
												+ " and sexternalsampleid='" + registrationSampleARNO.getSsamplearno()					
												+ "' and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and ntransactionstatus=" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
												+ " and nsitecode=" + destinationSitecode;
										
				final ExternalOrderSample externalOrderSample = (ExternalOrderSample) jdbcUtilityFunction.queryForObject(validateSampleQry, ExternalOrderSample.class, jdbcTemplate);
				
				if (externalOrderSample == null) 
				{				
					final String orderQryString = " INSERT INTO public.externalorder("
											+ " nexternalordercode, nordertypecode, nproductcatcode, nproductcode, "
											+ " ngendercode, ninstitutioncode, ninstitutionsitecode, ndiagnosticcasecode, nallottedspeccode, "
											+ " sorderseqno, spatientid, ssubmittercode, sexternalorderid, nusercode, jsondata,"
											+ " ntransactionstatus, ndefaultstatus, dmodifieddate, nsitecode, nstatus, nparentsitecode) "
											+ " select " + externalOrderSeqNo + " nexternalordercode, 2 nordertypecode,"
											+ " nproductcatcode, nproductcode, ngendercode, ninstitutioncode, "
											+ " ninstitutionsitecode, ndiagnosticcasecode, nallottedspeccode, "
											+ testInput.getNpreregno()
											+ " sorderseqno, spatientid, ssubmittercode, '" + registrationARNO.getSarno()  
											+ "' sexternalorderid, "
											+ " -1 nusercode, jsondata,"
											+  Enumeration.TransactionStatus.DRAFT.gettransactionstatus()  
											+ " ntransactionstatus, "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus()  
											+ " ndefaultstatus, "
											+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dmodifieddate, " 
											+ destinationSitecode + " nsitecode, " +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " nstatus, " + userInfo.getNsitecode() 
											+ " nparentsitecode from externalorder where sexternalorderid='" + orderId
											+ "' and nexternalordercode="+ nexternalordercode
											+ " and nparentsitecode=" + userInfo.getNsitecode() 
											+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(orderQryString);
		
					final String subSampleQryString = "INSERT INTO public.externalordersample("
							+ " nexternalordersamplecode, nexternalordercode, ncomponentcode, "
							+ " nsampleqty, nunitcode, sexternalsampleid, ntransactionstatus, "
							+ " dmodifieddate, nsitecode, nstatus, nparentsitecode, "
							+ " npreregno, ntransactionsamplecode) values "
							+ " ( " + 	seqordersample + " , "
							+ externalOrderSeqNo + " , "
							+ registrationSample.getNcomponentcode() + ", "
							+ " -1 ,-1 , '"
							+ registrationSampleARNO.getSsamplearno()+ "', "
							+  Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							+ ", "
							+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " 
							+ destinationSitecode + ", " 
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "," + userInfo.getNsitecode() 
							 + "," + testInput.getNpreregno()
							 + "," + testInput.getNtransactionsamplecode()
							+ ")";
						jdbcTemplate.execute(subSampleQryString);
						
						final String testQryString = "INSERT INTO public.externalordertest("
								 + " nexternalordertestcode, nexternalordersamplecode, nexternalordercode, "
								 + " ntestpackagecode, ncontainertypecode, ntestcode, dmodifieddate, "
								 + " nsitecode, nstatus, nparentsitecode, npreregno, ntransactionsamplecode, ntransactiontestcode)"
								 + " values ( " + seqordertest + ", "
								 + 	seqordersample + ", "
								 + externalOrderSeqNo + ", "
								 + " 1 , 0 , "
								 + testInput.getNtestcode() + ","
								 + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								 + destinationSitecode + ","
								 +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								 + "," + userInfo.getNsitecode() 
								 + "," + testInput.getNpreregno()
								 + "," + testInput.getNtransactionsamplecode()
								 + "," + testInput.getNtransactiontestcode()
								 + ")";
						
						jdbcTemplate.execute(testQryString);
						
						jdbcTemplate.execute(
								"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
										+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nstatus,nsitecode) "
										 + " values ( "+ seqtesthistory+","+ testInput.getNtransactiontestcode()+","+ testInput.getNtransactionsamplecode()+","+ testInput.getNpreregno()+","+ userInfo.getNformcode()+","
										+  Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+ ","
										+ userInfo.getNusercode() + ", "
										 + userInfo.getNuserrole() + ", "
										 + userInfo.getNdeputyusercode() + ", "
										 + userInfo.getNdeputyuserrole() + ",'"
										 + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
										+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()+ ",-1,"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ userInfo.getNtranssitecode() + ")"
										
										 
								);
						
//						
						final String externalAttachQry = "INSERT INTO public.externalorderattachment("
													 + " nexternalorderattachmentcode,nexternalordercode,sexternalorderid,sreportcomments,sfilename,ssystemfilename,nusercode,nuserrolecode,dmodifieddate,ntzmodifieddate,"
													 + " noffsetdmodifieddate,nsitecode,nparentsitecode,nstatus) "
													 + " select  nexternalordercode+Rank() over (order by nreleaseattachmentcode) as nexternalorderattachmentcode,"+externalOrderSeqNo+",'"+registrationARNO.getSarno()+"','',sfilename,ssystemfilename, "
													 + userInfo.getNusercode() + ", "
													 + userInfo.getNuserrole() + ", "
						                             + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						                             + userInfo.getNtimezonecode()+ ","
						                             + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+ ","
						                             + userInfo.getNsitecode()  + ","
						                             + userInfo.getNsitecode()  + ","
						                             +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													 + "from releaseattachment where releaseattachment.npreregno=30 ";
						
						jdbcTemplate.execute(externalAttachQry);
						
						final String updateSeqQuery = "update seqnoregistration set nsequenceno = " + externalOrderSeqNo
													+ " where stablename ='externalorder';"
													+ " update seqnoregistration  set nsequenceno =" + seqordersample
													+ " where stablename ='externalordersample';"
													+ " update seqnoregistration  set nsequenceno = "
													+ seqordertest + "" + " where stablename ='externalordertest';"
											        + " update seqnoregistration  set nsequenceno = "
											        + seqtesthistory + "" + " where stablename ='registrationtesthistory';";
											
						jdbcTemplate.execute(updateSeqQuery);
				}
				else {
					
					final String testQryString = "INSERT INTO public.externalordertest("
							 + " nexternalordertestcode, nexternalordersamplecode, nexternalordercode, "
							 + " ntestpackagecode, ncontainertypecode, ntestcode, dmodifieddate, "
							 + " nsitecode, nstatus, nparentsitecode, npreregno, ntransactionsamplecode, ntransactiontestcode)"
							 + " values ( " + seqordertest + ", "
							 + 	externalOrderSample.getNexternalordersamplecode() + ", "
							 + externalOrderSample.getNexternalordercode() + ", "
							 + " 1 , 0 , "
							 + testInput.getNtestcode() + ","
							 + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							 + destinationSitecode + ","
							 +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							 + "," + userInfo.getNsitecode() 
							 + "," + testInput.getNpreregno()
							 + "," + testInput.getNtransactionsamplecode()
							 + "," + testInput.getNtransactiontestcode()
							 + ")";
					
					jdbcTemplate.execute(testQryString);
					
					
					
					jdbcTemplate.execute(
							"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
									+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nstatus,nsitecode) "
									 + " values ( "+ seqtesthistory+","+ testInput.getNtransactiontestcode()+","+ testInput.getNtransactionsamplecode()+","+ testInput.getNpreregno()+","+ userInfo.getNformcode()+","
									+  Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+ ","
									+ userInfo.getNusercode() + ", "
									 + userInfo.getNuserrole() + ", "
									 + userInfo.getNdeputyusercode() + ", "
									 + userInfo.getNdeputyuserrole() + ",'"
									 + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()+ ",-1,"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ userInfo.getNtranssitecode() + ")"
									
									 
							);
					
					final String externalAttachQry = "INSERT INTO public.externalorderattachment("
													 + " nexternalorderattachmentcode,nexternalordercode,sexternalorderid,sreportcomments,sfilename,ssystemfilename,nusercode,nuserrolecode,dmodifieddate,ntzmodifieddate,"
													 + " noffsetdmodifieddate,nsitecode,nparentsitecode,nstatus) "
													 + " select  nexternalordercode+Rank() over (order by nreleaseattachmentcode) as nexternalorderattachmentcode,"+externalOrderSeqNo+",'"+registrationARNO.getSarno()+"','',sfilename,ssystemfilename, "
													 + userInfo.getNusercode() + ", "
													 + userInfo.getNuserrole() + ", "
						                             + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						                             + userInfo.getNtimezonecode()+ ","
						                             + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+ ","
						                             + userInfo.getNsitecode()  + ","
						                             + userInfo.getNsitecode()  + ","
						                             +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													 + " from releaseattachment where releaseattachment.npreregno=30 ";

					
					jdbcTemplate.execute(externalAttachQry);
					
					final String updateSeqQuery =  " update seqnoregistration  set nsequenceno = "
													+ seqordertest + "" + " where stablename ='externalordertest';";
					
					jdbcTemplate.execute(updateSeqQuery);
					
					final String updateTestSeqQuery =  " update seqnoregistration  set nsequenceno = "
														+ seqtesthistory + "" + " where stablename ='registrationtesthistory';";
					
					jdbcTemplate.execute(updateTestSeqQuery);
				}
				
			
				final String query ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+ "";
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);	
				boolean updateAnalyst = false;
				if(objSettings !=null) {
					updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;

				}	

					
				final String query1 = "select * from fn_registrationtestget('" + testInput.getNpreregno() + "'::text," + "'"
									+ testInput.getNtransactionsamplecode() + "'::text" + ",'" + testInput.getNtransactiontestcode()
									+ "'::text," +  " 3, " + userInfo.getNtranssitecode() + ",'"
									+ userInfo.getSlanguagetypecode() + "','"
									+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
									+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+ updateAnalyst +")";
				System.out.println("fn_registrationtestget:" + query1);

				final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
				final Map<String, Object> objMap = new HashMap();
				
				if (lstData2 != null) {
					// ObjectMapper obj = new ObjectMapper();
					List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							lstData2, userInfo, true, designTemplateMappingCode, "test");
					
					objMap.put("OutSourcedTestGet", lstData);
					objMap.put("Msg", commonFunction.getMultilingualMessage("IDS_TESTSUCCESSFULYOUTSOURCED", userInfo.getSlanguagefilename()));
				} 
				//SampleStatusToRelease(testInput,lsttestInput,destinationSitecode,designTemplateMappingCode,userInfo);
				return new ResponseEntity<>(objMap, HttpStatus.OK);

			}
			else
			{
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_TESTALREADYOUTSOURCED", userInfo.getSlanguagefilename()), 
						HttpStatus.CONFLICT);				
			}
		}	
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REQUESTFAILED", userInfo.getSlanguagefilename()), 
					HttpStatus.EXPECTATION_FAILED);
		}
		
	}

	public ResponseEntity<Object> getOutSourceSite(final RegistrationTest testInput, final UserInfo userInfo) throws Exception {
		
		String statuscount="select count(ncoaparentcode) from coaparent where ncoaparentcode in (select coachild.ncoaparentcode "
						 +" from coachild where npreregno in ("+ testInput.getNpreregno() +")) "
						 +" and coaparent.ntransactionstatus =33";
		int nstatuscount = jdbcTemplate.queryForObject(statuscount, Integer.class);
		
        if(nstatuscount!=0)
        {
            	 String countQuery="select count(ntransactiontestcode) from externalordertest where ntransactiontestcode="+testInput.getNtransactiontestcode();
            	 int count = jdbcTemplate.queryForObject(countQuery, Integer.class);
            	 if(count==0)
	             {
            		 final String strQuery = " select * from site s,siteconfig sc where s.nsitecode not in("
								+ " select nsitecode from externalordertest where npreregno=" +testInput.getNpreregno()
								+ " and ntransactionsamplecode= " +testInput.getNtransactionsamplecode()
								+ " and ntransactiontestcode=" +testInput.getNtransactiontestcode()
								+ " union select " + userInfo.getNtranssitecode() + ") and s.nsitecode=sc.nsitecode and sc.nisstandaloneserver =" + Enumeration.TransactionStatus.YES.gettransactionstatus() 
								+ " and s.nsitecode > 0 and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		                        + " and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<Site> lstSite= jdbcTemplate.query(strQuery,new  Site());
					return new ResponseEntity<Object>(lstSite,HttpStatus.OK);
				}
				else {
					final String returnString = commonFunction.getMultilingualMessage("IDS_TESTALREADYOUTSOURCED", userInfo.getSlanguagefilename());
					return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
				}
	
        }
         else {
     		final String returnString = commonFunction.getMultilingualMessage("IDS_ATLEASTONETESTINRELEASED", userInfo.getSlanguagefilename());
     		return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
     	}
	
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {
		final String strQuery = "select *, to_char(dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate from region "
				+ " where nsitecode="+userInfo.getNmastersitecode();
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Region()), HttpStatus.OK);
	}
	@Override
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select t.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
							  +"ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, to_char(t.dmodifieddate,'"
							  + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate from testcategory t,"
							  +"transactionstatus ts where t.ntestcategorycode>0 and ts.ntranscode=t.ndefaultstatus and t.nsitecode = "+userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new TestCategory()),HttpStatus.OK);		
	}
	@Override
	public ResponseEntity<Object> getCity(UserInfo userInfo) throws Exception {
		
		final String strCity = "select c.ncitycode, c.scityname, c.scitycode, c.nsitecode, c.nstatus ,d.sdistrictname ,c.ndistrictcode,"
				+ "to_char(c.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate from city c,district d where  d.ndistrictcode=c.ndistrictcode and "
				+" c.nsitecode = "+userInfo.getNmastersitecode();
		
		return new ResponseEntity<>(jdbcTemplate.query(strCity, new City()),HttpStatus.OK);
	}
	public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception {
		final String strQuery = "select d.ndistrictcode, d.sdistrictname, d.sdistrictcode, d.nsitecode,d.nstatus, d.nregioncode, r.sregionname,"
								+ " to_char(d.dmodifieddate, '"+userInfo.getSpgsitedatetime()+"') as smodifieddate from district d, region r"
								+ " where d.nsitecode="+userInfo.getNmastersitecode()+ " and d.nregioncode = r.nregioncode";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new District()), HttpStatus.OK);
	}
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception {
		final String strQuery = "select c.ncountrycode,c.scountryname,c.scountryshortname,c.stwocharcountry,c.sthreecharcountry,c.nsitecode,c.nstatus,"
									+"to_char(c.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate "
									+" from Country c where"
									+" c.nsitecode= "+userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<Country>) jdbcTemplate.query(strQuery,new Country()),HttpStatus.OK);
	}
	public ResponseEntity<Object> getSubmitterDetailByAll(Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {	
		final String strQuery = "select s.ssubmittercode,s.ninstitutiondeptcode,s.sfirstname,s.slastname,s.ssubmitterid,s.sshortname,s.swardname,s.stelephone,s.smobileno,s.sfaxno,s.semail,s.sspecialization,s.sreportrequirement,s.ssampletransport,s.ntransactionstatus,s.dcreateddate,s.ntzcreateddate,s.noffsetdcreateddate,s.dmodifieddate,s.nsitecode,s.nstatus,"
								+ " inscat.ninstitutioncatcode, inscat.sinstitutioncatname, ins.ninstitutioncode, ins.sinstitutionname, "
								+ "inssite.ninstitutionsitecode, inssite.sinstitutionsitename, insdept.ninstitutiondeptcode, insdept.sinstitutiondeptname, "
								+ "insdept.sinstitutiondeptcode, to_char(s.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate "
								+ "  from institutioncategory inscat, institution ins, institutionsite inssite, submitter s,submittermapping sm, institutiondepartment insdept where "
								+ "  sm.ssubmittercode = s.ssubmittercode and inscat.ninstitutioncatcode=ins.ninstitutioncatcode and "
								+ " ins.ninstitutioncode=inssite.ninstitutioncode and sm.ninstitutioncatcode=inscat.ninstitutioncatcode and "
								+ " sm.ninstitutioncode=ins.ninstitutioncode and sm.ninstitutionsitecode=inssite.ninstitutionsitecode and insdept.ninstitutiondeptcode=s.ninstitutiondeptcode";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Submitter()),HttpStatus.OK);
		
	}
	public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception {
		String str  = "select ins.ninstitutionsitecode,ins.ninstitutioncode,i.ninstitutioncatcode,ins.nregionalsitecode,ins.ncitycode,ins.ncountrycode,ins.sinstitutionsitename,ins.sinstitutionsiteaddress,ins.szipcode, "
				  + "ins.nstatus, ins.sstate,ins.stelephone,ins.sfaxno,ins.semail,ins.swebsite,i.sinstitutionname,s.ssitename,ins.scityname,c1.scountryname ,r.sregionname,d.sdistrictname ,ins.nregioncode,ins.ndistrictcode ,r.sregioncode,d.sdistrictcode, "
				  + "to_char(ins.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate  from institutionsite ins "
				  + "join institution  i on ins.ninstitutioncode = i.ninstitutioncode "
				  + "join site s on ins.nregionalsitecode = s.nsitecode "
				  + "join city c on ins.ncitycode = c.ncitycode  "
				  + "join country c1 on ins.ncountrycode = c1.ncountrycode "
				  + "join region r on ins.nregioncode=r.nregioncode "
				  + "join district d on ins.ndistrictcode=d.ndistrictcode";
		return new ResponseEntity<>( jdbcTemplate.query(str, new InstitutionSite()),HttpStatus.OK);
	}
	public ResponseEntity<Object> getInstitutionValues(UserInfo userInfo) throws Exception {
		final String str  = "select ninstitutioncode, ninstitutioncatcode, sinstitutionname, sinstitutioncode, sdescription, "
					+ "nsitecode, nstatus, to_char(dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")
					+"') as smodifieddate from institution ";	
		return new ResponseEntity<>(jdbcTemplate.query(str, new Institution()),HttpStatus.OK);
	}
	public ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception {
		final String strQuery="select ntestpackagecode,stestpackagename,ntestpackageprice,ntestpackagetatdays,sopenmrsrefcode,spreventtbrefcode,sportalrefcode,sdescription,nstatus, "
				+ "to_char(dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate from testpackage ";
		List<String> lstcolumns = new ArrayList<>();
		
		return new ResponseEntity<>((List<TestPackage>) 
                (List<TestPackage>) jdbcTemplate.query(strQuery, new TestPackage()),HttpStatus.OK);
	}
	public ResponseEntity<Object> getInstitutionDepartment(UserInfo userInfo) throws Exception {
		final String strQuery = "select ninstitutiondeptcode,sinstitutiondeptname,sinstitutiondeptcode,nsitecode,nstatus, to_char(dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate from institutiondepartment "
				+ " where ninstitutiondeptcode>0  and nsitecode="+userInfo.getNmastersitecode();
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InstitutionDepartment()), HttpStatus.OK);
	}
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception {
		final String strQuery = "select u.*,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, to_char(u.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate"
				+ " from Unit u,transactionstatus ts"
				+ " where ts.ntranscode=u.ndefaultstatus and u.nsitecode="+userInfo.getNmastersitecode();
		List<String> lstcolumns = new ArrayList<>();
		lstcolumns.add("sdisplaystatus");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Unit()), HttpStatus.OK);
	}
//	public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception {
//		
//		final String strQuery = "select pm.npackagemastercode,pm.npackagecategorycode,pm.spackagename,pm.sdescription,pc.spackagecategoryname,pm.nsitecode,pm.nstatus,"
//                + "to_char(pm.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate"
//                + " from packagemaster pm,packagecategory pc "
//                + " where pm.npackagecategorycode = pc.npackagecategorycode "
//                +" and pm.nsitecode="+userInfo.getNmastersitecode();
//		
//		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new PackageMaster()), HttpStatus.OK);
//
//	}
	public ResponseEntity<Object> getSiteScreen(Integer nsitecode,final UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		
		final String strQuery	= "select s.nsitecode,s.ntimezonecode,s.ndateformatcode,s.ssitename,s.ssiteaddress,sc.nissyncserver,"
									+ "case when s.scontactperson = '' then '-' else s.scontactperson end,"
									+ "case when s.ssitecode= 'NA' then '-' else s.ssitecode end,"
									+ "case when r.sregionname = 'NA' then '-' else r.sregionname end ,"
									+ "case when d.sdistrictname = 'NA' then '-' else d.sdistrictname end ,"
									+ "	s.sphoneno,s.sfaxno,s.semail,s.ndefaultstatus,s.nismultisite,s.nmastersitecode,s.nstatus,"
									+ "to_char(s.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate, "
									+ "  coalesce(df.jsondata->'sdateformat'->>'"+userInfo.getSlanguagetypecode()+"',"
									+ "df.jsondata->'sdateformat'->>'en-US')  as sdateformat ,"
									+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
									+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
									+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					        		+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as sdistributedstatus,"
					        		+ "coalesce(ts2.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					        		+ "ts2.jsondata->'stransdisplaystatus'->>'en-US') as sprimarysereverstatus"
									+ ",sc.nisstandaloneserver ,s.nregioncode,"
									+ "s.ndistrictcode, s.nstatus , "
									+ " case when tz.ntimezonecode=-1 then '-' else  tz.stimezoneid end  from Site s,transactionstatus ts,timezone tz,siteconfig sc,dateformat df,transactionstatus ts1,"
									+ "region r ,district d ,transactionstatus ts2 "
									+ " where s.ntimezonecode=tz.ntimezonecode and s.ndateformatcode=df.ndateformatcode "
									+ " and sc.nisstandaloneserver=ts1.ntranscode and s.nsitecode=sc.nsitecode and ts.ntranscode=s.ndefaultstatus and r.nregioncode=s.nregioncode and d.ndistrictcode =s.ndistrictcode  "
									+ " and sc.nissyncserver=ts2.ntranscode and s.nmastersitecode="+userInfo.getNmastersitecode()+" "
									+ " order by s.nsitecode asc";
		return new ResponseEntity<Object>(
				(List<Site>) jdbcTemplate.query(strQuery, new Site()), HttpStatus.OK);
	}
	public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo)throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String fromDate = "";
		String toDate = "";

		Patient selectedPatient = null;

		if (spatientid == null) {


			final String query = "select pm.spatientid,pm.sfirstname||' '||pm.slastname as spatientname,pm.sfirstname,pm.slastname,"
								+ " pm.sfathername,pm.sage,pm.ddob,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ," + " to_char(pm.ddob,'"
								+ userInfo.getSsitedate() + "') as sdob," + " pm.spostalcode,pm.sphoneno,pm.smobileno,pm.semail,"
								+ " pm.srefid,pm.spassportno,pm.sexternalid,pm.ndistrictcodetemp,pm.ncitycodetemp,pm.nregioncode,pm.nregioncodetemp,"
								+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
								+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
								+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
								+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
								+ " pm.scityname,cu.scountryname ,r.sregionname,r1.sregionname as sregionnametemp,"
								+ "d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,pm.scitynametemp,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,"
								+ "pm.sflatno,pm.sflatnotemp,pm.ncitycode,pm.nregioncode,pm.ndistrictcode,pm.spostalcodetemp,pm.ngendercode,pm.ncountrycode,"
								+ "to_char(pm.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,pm.nstatus  from patientmaster pm,gender g, city c ,city c1,country cu ,region r1, region r,district d1,district d,"
								+ "transactionstatus ts,transactionstatus ts1 "
								+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode"
								+ " and c.ncitycode=pm.ncitycode and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp and "
								+ " d.ndistrictcode=pm.ndistrictcode and d1.ndistrictcode=pm.ndistrictcodetemp and "
								+ "  c1.ncitycode=pm.ncitycodetemp and ts.ntranscode=pm.nneedmigrant  "
								+ "and ts1.ntranscode=pm.nneedcurrentaddress and  pm.nsitecode=" + userInfo.getNmastersitecode()+" "
			//					+  date+ " "
								+ "order by pm.dmodifieddate desc ";

			final List<Patient> patientList = jdbcTemplate.query(query, new Patient());

			String caseType = "select jsondata, npatientcasetypecode, coalesce(jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',jsondata->'sdisplayname'->>'en-US') as spatientcasetypename,jsondata->'sdisplayname'->>'en-US' as displayname ,nstatus from patientcasetype ";
			final List<PatientCaseType> patientCaseType = jdbcTemplate.query(caseType, new PatientCaseType());
			outputMap.put("PatientCaseType", patientCaseType);
//			outputMap.put("SelectedPatientCaseType", patientCaseType.get(0));

			if (patientList.isEmpty()) {
				outputMap.put("PatientList", patientList);
				outputMap.put("SelectedPatient", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				// selectedPatient = (Patient) patientList.get(patientList.size() - 1);
				selectedPatient = (Patient) patientList.get(0);
				spatientid = selectedPatient.getSpatientid();
				outputMap.put("PatientList", patientList);
			}
		} else {
			selectedPatient = getActivePatientById(spatientid, userInfo);
		}

		if (selectedPatient == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedPatient", selectedPatient);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}
	public Patient getActivePatientById(final String spatientid, final UserInfo userInfo) throws Exception {

		final String query = "select pm.spatientid,pm.sfirstname,c.ncitycode,cu.ncountrycode,g.ngendercode,"
							+ " pm.slastname,pm.sfirstname||' '||pm.slastname as spatientname,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "',"
							+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,"
							+ " case when pm.sfathername='null' then '' else pm.sfathername end ,pm.sage,pm.ddob," + " to_char(pm.ddob,'" + userInfo.getSsitedate() + "') as sdob,"
							+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
							+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as scurrentaddress,"
							+ " case when pm.spostalcode='null' then '' else pm.spostalcode end ,case when pm.spostalcodetemp='null' then '' else pm.spostalcodetemp end  ,case when pm.sphoneno='null' then '' else pm.sphoneno end ,"
							+ " case when pm.smobileno='null' then '' else pm.smobileno end ,case when pm.semail='null' then '' else pm.semail end,"
							+ " case when pm.srefid='null' then '' else pm.srefid end  ,case when pm.spassportno='null' then '' else pm.spassportno end "
							+ ",case when pm.sexternalid='null' then '' else pm.sexternalid end ," + " coalesce(g.jsondata->'sgendername'->>'"
							+ userInfo.getSlanguagetypecode() + "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
							+ " pm.scityname,cu.scountryname,pm.sstreet,pm.sstreettemp,pm.shouseno,pm.shousenotemp,pm.sflatno,pm.sflatnotemp,"
							+ "pm.nneedcurrentaddress,pm.nneedmigrant, r1.sregionname as sregionnametemp,r.sregionname as sregionname,pm.sexternalid ,pm.scitynametemp "
							+ ",pm.nregioncode,pm.nregioncodetemp,pm.ndistrictcode,pm.ndistrictcodetemp ,d.sdistrictname,d1.sdistrictname as sdistrictnametemp ,pm.ncitycodetemp  "
							+ "from transactionstatus ts,transactionstatus ts1, patientmaster pm,gender g, city c ,country cu,region r,district d,city c1,district d1,region r1"
							+ " where pm.ngendercode=g.ngendercode and cu.ncountrycode=pm.ncountrycode and ts.ntranscode=pm.nneedmigrant"
							+ " and c.ncitycode=pm.ncitycode and  c1.ncitycode=pm.ncitycodetemp  and r.nregioncode=pm.nregioncode and r1.nregioncode=pm.nregioncodetemp  and  ts1.ntranscode=pm.nneedcurrentaddress"
							+ "  and d.ndistrictcode=pm.ndistrictcode   and d1.ndistrictcode=pm.ndistrictcodetemp "
							+ "" + " and pm.spatientid='" + spatientid
							+ "'";

		return (Patient) jdbcUtilityFunction.queryForObject(query, Patient.class, jdbcTemplate);

	}
	
	public ResponseEntity<Object> getInstitutionCategory(UserInfo userInfo) throws Exception {
		
		String strInstitutionCat = "select ninstitutioncatcode, sinstitutioncatname, sdescription, nsitecode, nstatus, "
									+ "to_char(dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate"
									+ " from institutioncategory where nsitecode ="+userInfo.getNmastersitecode()+"";
							
		return new ResponseEntity<>(jdbcTemplate.query(strInstitutionCat, new InstitutionCategory()),HttpStatus.OK);
	}
	
	public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String strQuery="";
		if(nproductcode == null) {
		 strQuery = "select p.nproductcode, p.nproductcatcode, p.sproductname, p.sdescription, p.ndefaultstatus, p.nsitecode, p.nstatus,"
					+ " pc.sproductcatname,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, "
					+ " to_char(p.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate ,p.nstatus"
					+ " from product p, productcategory pc, transactionstatus ts"
					+ " where ts.ntranscode = p.ndefaultstatus and p.nproductcatcode = pc.nproductcatcode"
					+ " and p.nproductcode > 0 and p.nsitecode = " + userInfo.getNmastersitecode()+" order by p.nproductcode ";

		 List<Product> productList = jdbcTemplate.query(strQuery,
					new Product());
		 if (productList.isEmpty()) {
				outputMap.put("Product", productList);
				outputMap.put("SelectedProduct", null);
				outputMap.put("productFile",null);
				
				
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("Product", productList);
				
			}
		}
		else
		{
			
			strQuery = "select p.nproductcode, p.nproductcatcode, p.sproductname, p.sdescription, p.ndefaultstatus, p.nsitecode, p.nstatus,"
					+ " pc.sproductcatname,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
					+ " from product p, productcategory pc, transactionstatus ts"
					+ " where p.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" ts.ntranscode = p.ndefaultstatus and p.nproductcatcode = pc.nproductcatcode "
					+ " and p.nproductcode > 0 and p.nsitecode = " + userInfo.getNmastersitecode()+" order by p.nproductcode ";
			
			 List<Product> productLst = jdbcTemplate.query(strQuery,
					new Product());
			   
			 outputMap.put("Product", productLst);
			 
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSamplePriority(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select g.nstatus ,nsampleprioritycode,coalesce(g.jsondata->'samplepriorityname'->>'" + userInfo.getSlanguagetypecode() + "',"
                + " g.jsondata->'samplepriorityname'->>'en-US') as samplepriorityname, to_char(g.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate "
                + " from samplepriority g";
		List<SamplePriority> lstsamplepriority=jdbcTemplate.query(strQuery, new SamplePriority());
		outputMap.put("Samplepriority", lstsamplepriority);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		String strQuery	= "select * from site where nsitecode > 0 "
						+ " nmastersitecode= " +userInfo.getNmastersitecode()
						+" order by ndefaultstatus";
		List<Site> lstSite= jdbcTemplate.query(strQuery,new  Site());
		return new ResponseEntity<Object>(lstSite,HttpStatus.OK);
	}	
	
	public Map<String, Object> SampleStatusToRelease(final RegistrationTest testInput,List<RegistrationTest> lsttestInput,  
			final int destinationSitecode, final int designTemplateMappingCode,
			final UserInfo userInfo) throws Exception{
		
		final String transactiontestcode = lsttestInput.stream()
				.map(objpreregno -> String.valueOf(objpreregno.getNtransactiontestcode()))
				.collect(Collectors.joining(","));
		final String sequenceNoQuery = "select stablename,nsequenceno from seqnoregistration  where stablename "
									+ " in ('registrationhistory','registrationsamplehistory','registrationtesthistory') "
									+ " order by stablename ";

		final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequenceNoQuery, new SeqNoRegistration());

		Map<String, Object> returnMap = new HashMap();		
		returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		int regHistorySeqNo = ((int) returnMap.get("registrationhistory"));
		int seqregsamplehistory = ((int) returnMap.get("registrationsamplehistory"));
	
		final String approvedSampleQuery = "" + "	select historycount.npreregno from ("
											+ "		select npreregno,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
											+ "			select ntransactiontestcode from registrationtesthistory "
											+ "			where ntransactionstatus=" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + "" 
											+" and nsitecode="+userInfo.getNtranssitecode()
											+ " and npreregno in ("+testInput.getNpreregno()+") "
											//+ " and ntransactiontestcode !="+testInput.getNtransactiontestcode()+ " "
											+ " and ntransactiontestcode not in("+transactiontestcode+")"
											+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " 		group by npreregno,ntransactiontestcode" + "		)" 
											+" and nsitecode="+userInfo.getNtranssitecode()
											+ "		group by npreregno"
											+ "	)historycount,( " + "		select rth.npreregno,count(ntesthistorycode) testcount "
											+ "		from registrationtest rt,registrationtesthistory rth "
											+ "		where ntransactionstatus not in ("
											+ Enumeration.TransactionStatus.RETEST.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") "
											+" and rt.nsitecode=rth.nsitecode"
											+" and rt.nsitecode="+userInfo.getNtranssitecode()
											+ "		and rt.ntransactiontestcode=rth.ntransactiontestcode "
											+ "		and rth.ntesthistorycode = any( "
											+ "			select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
											//+ "			where npreregno in ("+testInput.getNpreregno()+") and ntransactiontestcode !="+testInput.getNtransactiontestcode()+"  and nstatus="
											+ "			where npreregno in ("+testInput.getNpreregno()+")  and ntransactiontestcode not in("+transactiontestcode+")  and nstatus="				
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode="+userInfo.getNtranssitecode()
											+ " 		group by ntransactiontestcode,npreregno)" + " group by rth.npreregno)testcount "
											+ "where historycount.npreregno=testcount.npreregno "
											+ "and historycount.testcount=testcount.testcount; ";

		final List<RegistrationTestHistory> approvedSampleList = // mapper.convertValue(
				jdbcTemplate.query(approvedSampleQuery, new RegistrationTestHistory());

		final String approvedSubSampleQuery = "" + "	select historycount.ntransactionsamplecode from ("
												+ "		select ntransactionsamplecode,count(ntransactiontestcode) testcount from registrationtest where ntransactiontestcode=any("
												+ "			select ntransactiontestcode from registrationtesthistory "
												+ "			where ntransactionstatus=" + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " and npreregno in ("
												//+ testInput.getNpreregno() + ")" + " and ntransactiontestcode !=" + testInput.getNtransactiontestcode()+ ""
												+ testInput.getNpreregno() + ")" + " and ntransactiontestcode not in("+transactiontestcode+") "
												+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " 		group by ntransactionsamplecode,ntransactiontestcode" + "		)" + " and nsitecode="
												+ userInfo.getNtranssitecode() + "		group by ntransactionsamplecode" + "	)historycount ";

		final List<RegistrationTestHistory> approvedSubSampleList = // mapper.convertValue(
				jdbcTemplate.query(approvedSubSampleQuery, new RegistrationTestHistory());
		if (approvedSampleList.size() > 0) {

			String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
										+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
										+ " select " + regHistorySeqNo + "+Rank() over (order by npreregno) as nreghistorycode, "
										+ " npreregno npreregno," + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
										+ " as ntransactionstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate,"
										+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
										+ "," + userInfo.getNdeputyuserrole() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
										+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
										+ " from  registration where npreregno in ( " + testInput.getNpreregno()+ ") ;";

			jdbcTemplate.execute(strRegistrationHistory);


			
			String strRegSampleHistory = "insert into registrationsamplehistory (nsamplehistorycode,npreregno,ntransactionsamplecode,ntransactionstatus,dtransactiondate,"
											+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
											+ "select " + seqregsamplehistory
											+ "+rank()over(order by rs.ntransactionsamplecode) nsamplehistorycode,rs.npreregno,rs.ntransactionsamplecode,"
											+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() 
											+ " ntransactionstatus," + "'"+ dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
											+ "'  dtransactiondate," + userInfo.getNusercode() 
											+ " nusercode," + userInfo.getNuserrole()
											+ " nuserrolecode," + "" + userInfo.getNdeputyusercode() 
											+ "ndeputyusercode,"+ userInfo.getNdeputyuserrole() 
											+ " ndeputyuserrolecode,'" + stringUtilityFunction.replaceQuote(userInfo.getSreason())
											+ "' scomments," + userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 	+ " nstatus "
											+ " from registrationsample rs, registrationsamplehistory rsh where rs.npreregno = " + testInput.getNpreregno() 
											+ "  and rs.npreregno=rsh.npreregno "
											+ "  and rs.nstatus=rsh.nstatus and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ "  and rs.nsitecode=rsh.nsitecode and rsh.nsitecode=" +  userInfo.getNtranssitecode()
											+ "  and rsh.nsamplehistorycode=any(select max(nsamplehistorycode) from registrationsamplehistory "
											+ " where npreregno=" + testInput.getNpreregno() 
											+ " and nsitecode= " +   userInfo.getNtranssitecode()
											+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ntransactionstatus not in(" 
											+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()  + ")) "
											+ " order by rs.ntransactionsamplecode" ;

			jdbcTemplate.execute(strRegSampleHistory);
		}

		final String updateSeqQuery = "update seqnoregistration set nsequenceno = " + (regHistorySeqNo+1)
				+ " where stablename ='registrationhistory';"
				+ " update seqnoregistration  set nsequenceno =" + ((seqregsamplehistory+approvedSubSampleList.size()))
				+ " where stablename ='registrationsamplehistory';";
		jdbcTemplate.execute(updateSeqQuery);
		
		returnMap.put("Success", Enumeration.TransactionStatus.SUCCESS.gettransactionstatus());
		return returnMap;
	}

	
	
	public ResponseEntity<Object> getOutSourceSiteAndTest(final RegistrationTest testInput, final UserInfo userInfo) throws Exception {
		
		String statuscount="select count(ncoaparentcode) from coaparent where ncoaparentcode in (select coachild.ncoaparentcode "
				 +" from coachild where npreregno in ("+ testInput.getNpreregno() +")) "
				 +" and coaparent.ntransactionstatus =33";
		int nstatuscount = jdbcTemplate.queryForObject(statuscount, Integer.class);
	
        if(nstatuscount!=0)
        {
            	 
            String samplestatuscount="select count(ntransactionsamplecode) from outsourcedetail where nstatus =1 and ntransactionsamplecode="+testInput.getNtransactionsamplecode();
        	int nsamplestatuscount = jdbcTemplate.queryForObject(samplestatuscount, Integer.class);
        	if(nsamplestatuscount == 0) 
        	{

	        	final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
	
	        	
	        	final String strQuery = " select * from site s,siteconfig sc where s.nsitecode=sc.nsitecode and sc.nisstandaloneserver =" + Enumeration.TransactionStatus.YES.gettransactionstatus() 
						+ " and s.nsitecode > 0 and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
                        + " and s.nsitecode!="+userInfo.getNsitecode()+" and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	        	final List<Site> lstSite= jdbcTemplate.query(strQuery,new  Site());

				final String strTestQuery = "select rt.jsondata->>'stestsynonym' stestsynonym,rt.ntransactionsamplecode,rt.ntransactiontestcode from registrationtest rt, registrationtesthistory rth "
						+ "  where ntesthistorycode in (select max(registrationtesthistory.ntesthistorycode) from registrationtesthistory group by ntransactiontestcode) and rt.ntransactionsamplecode=rth.ntransactionsamplecode and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.ntransactionstatus="+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+ " and rt.ntransactionsamplecode="+testInput.getNtransactionsamplecode();
				final List<RegistrationTest> lstTest= jdbcTemplate.query(strTestQuery,new  RegistrationTest());
				
				final String strRegDateQuery = "select TO_CHAR( max(dtransactiondate),  'yyyy-MM-dd' ) dtransactiondate from registrationsamplehistory where ntransactionstatus=18 and ntransactionsamplecode="+testInput.getNtransactionsamplecode();
				
				String regDate = jdbcTemplate.queryForObject(strRegDateQuery, String.class);
				outputMap.put("Site", lstSite);
				outputMap.put("Test", lstTest);
				outputMap.put("regDate", regDate);
		
				return new ResponseEntity<Object>(outputMap,HttpStatus.OK);
        	}
            else {
        		final String returnString = commonFunction.getMultilingualMessage("IDS_SOMETESTALREADYOUTSOURCE", userInfo.getSlanguagefilename());
        		return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
        	}	
        }
         else {
     		final String returnString = commonFunction.getMultilingualMessage("IDS_ATLEASTONETESTINRELEASED", userInfo.getSlanguagefilename());
     		return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
     	}	
	
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<Object> outSourceSampleTest(final RegistrationTest testInputnew,final RegistrationTest otherdetails,List<RegistrationTest> testInput, 
			final int destinationSitecode, final int designTemplateMappingCode,final Registration outsourceSampleData,final UserInfo userInfo) throws Exception
	{		
		
		final String transactiontestcode = testInput.stream()
				.map(objpreregno -> String.valueOf(objpreregno.getNtransactiontestcode()))
				.collect(Collectors.joining(","));

		
		String queryString = "select * from registration r where r.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and r.nsitecode=" + userInfo.getNsitecode()
										+ " and r.npreregno=" +testInputnew.getNpreregno() + ";";
		final Registration registration = (Registration)jdbcTemplate.queryForObject(queryString, new Registration());
		
		queryString =  "select ra.* from registrationarno ra where ra.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ra.nsitecode=" + userInfo.getNsitecode()
				+ " and ra.npreregno=" + testInputnew.getNpreregno() + ";";
		final RegistrationArno registrationARNO = (RegistrationArno)jdbcTemplate.queryForObject(queryString, new RegistrationArno());
		
		
		queryString = "select rs.* from registrationsample rs where rs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rs.nsitecode=" + userInfo.getNsitecode()
				+ " and rs.npreregno=" + testInputnew.getNpreregno() 
				+ " and rs.ntransactionsamplecode in ("+ testInputnew.getNtransactionsamplecode()  + ");";
		final RegistrationSample registrationSample = (RegistrationSample)jdbcTemplate.queryForObject(queryString, new RegistrationSample());
		
		
		queryString =  "select rsa.* from registrationsamplearno rsa where rsa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rsa.nsitecode=" + userInfo.getNsitecode()
				+ " and rsa.npreregno=" + testInputnew.getNpreregno()
				+ " and rsa.ntransactionsamplecode in("+ testInputnew.getNtransactionsamplecode() + ");";
		final RegistrationSampleArno registrationSampleARNO = (RegistrationSampleArno)jdbcTemplate.queryForObject(queryString, new RegistrationSampleArno());
		
			
		
		final String sequenceNoQuery = "select stablename,nsequenceno from seqnoregistration  where stablename "
										+ " in ('externalorder','externalordersample','externalordertest','registrationtesthistory','externalorderattachment') "
										+ " order by stablename ";

		final List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequenceNoQuery, new SeqNoRegistration());
		
		Map<String, Object> returnMap = new HashMap();		
		returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
				SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		int externalOrderSeqNo = ((int) returnMap.get("externalorder")) + 1;
		int seqordertest = ((int) returnMap.get("externalordertest"));
		int seqordersample = ((int) returnMap.get("externalordersample"))+1;
		int seqtesthistory = ((int) returnMap.get("registrationtesthistory"))+1;
		int externalorderattachmentSeqNo = ((int) returnMap.get("externalorderattachment"));
		final JSONObject regJSON  = new JSONObject(registration.getJsondata());
		
		
		String orderId = "";
		int nexternalordercode = -1;
		if(regJSON.has("OrderIdData")) {
			orderId = regJSON.getString("OrderIdData");
			nexternalordercode = regJSON.getInt("OrderCodeData");
		}

		
		if (orderId.trim().length() > 0) {
			
			final String validateTestQry = " select * from externalordertest where "
					+ " npreregno in(" + testInputnew.getNpreregno() +")"
					+ " and ntransactionsamplecode in("+testInputnew.getNtransactionsamplecode()+")"
					+ " and ntransactiontestcode in  ( "+transactiontestcode+")"
					+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + destinationSitecode;
			
			List<ExternalOrderTest> externalOrderTest = (List<ExternalOrderTest>) jdbcUtilityFunction.queryForObject(validateTestQry, ExternalOrderTest.class, jdbcTemplate);
			
			if(externalOrderTest == null) {
				
				final String validateSampleQry = " select count(nexternalordersamplecode) nexternalordersamplecode from externalordersample where "
												+ " npreregno =" +testInputnew.getNpreregno()
												//+ " and ntransactionsamplecode in("+testInputnew.getNtransactionsamplecode()+")"
												//+ " and sexternalsampleid='" + registrationSampleARNO.getSsamplearno()					
												+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												//+ " and ntransactionstatus=" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
												+ " and nsitecode=" + destinationSitecode;
										
				final int externalOrderSample = jdbcTemplate.queryForObject(validateSampleQry, Integer.class);
			
				final String orderQryString = " INSERT INTO public.externalorder("
											+ " nexternalordercode, nordertypecode, nexternalordertypecode, nproductcatcode, nproductcode, "
											+ " ngendercode, ninstitutioncode, ninstitutionsitecode, ndiagnosticcasecode, nallottedspeccode, "
											+ " sorderseqno, spatientid, ssubmittercode, sexternalorderid, nusercode, jsondata,"
											+ " ntransactionstatus, ndefaultstatus, dmodifieddate, nsitecode, nstatus, nparentsitecode) "
											+ " select " + externalOrderSeqNo + " nexternalordercode, 2 nordertypecode, 4 nexternalordertypecode,"
											+ " nproductcatcode, nproductcode, ngendercode, ninstitutioncode, "
											+ " ninstitutionsitecode, ndiagnosticcasecode, nallottedspeccode, "
											+ testInputnew.getNpreregno()
											+ " sorderseqno, spatientid, ssubmittercode,'"+registrationARNO.getSarno()
											+ "' sexternalorderid, "
											+ " -1 nusercode, jsondata,"
											+  Enumeration.TransactionStatus.DRAFT.gettransactionstatus()  
											+ " ntransactionstatus, "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus()  
											+ " ndefaultstatus, "
											+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dmodifieddate, " 
											+ destinationSitecode + " nsitecode, " +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " nstatus, " + userInfo.getNsitecode() 
											+ " nparentsitecode from externalorder where sexternalorderid='" + orderId
											+ "' and nexternalordercode="+ nexternalordercode
											//+ " and nparentsitecode=" + userInfo.getNsitecode() 
											+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(orderQryString);
		
					final String subSampleQryString = "INSERT INTO public.externalordersample("
													+ " nexternalordersamplecode, nexternalordercode, ncomponentcode, "
													+ " nsampleqty, nunitcode, sexternalsampleid, ntransactionstatus, "
													+ " dmodifieddate, nsitecode, nstatus, nparentsitecode, "
													+ " npreregno, ntransactionsamplecode) values "
													+ " ( " + 	seqordersample + " , "
													+ externalOrderSeqNo + " , "
													+ registrationSample.getNcomponentcode() + ", "
													+ " 0 ,-1 , '"
													+ registrationSampleARNO.getSsamplearno()+ "', "
													+  Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
													+ ", "
													+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " 
													+ destinationSitecode + ", " 
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ "," + userInfo.getNsitecode() 
													 + "," + testInputnew.getNpreregno()
													 + "," + testInputnew.getNtransactionsamplecode()
													+ ")";
					jdbcTemplate.execute(subSampleQryString);
						
					final String testQryString = "INSERT INTO public.externalordertest("
								 + " nexternalordertestcode, nexternalordersamplecode, nexternalordercode, "
								 + " ntestpackagecode, ncontainertypecode, ntestcode, dmodifieddate, "
								 + " nsitecode, nstatus, nparentsitecode, npreregno, ntransactionsamplecode, ntransactiontestcode)"
								 + "select " + seqordertest
								 + "+rank()over(order by ntransactiontestcode) nexternalordertestcode,"
								 + 	seqordersample + ", "
								 + externalOrderSeqNo + ", "
								 + " 1 , 0 , "
								 + " ntestcode,"
								 + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								 + destinationSitecode + ","
								 +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								 + "," + userInfo.getNsitecode() 
								 + ",npreregno "
								 + ",ntransactionsamplecode "
								 + ",ntransactiontestcode "
								 + " from registrationtest where ntransactiontestcode in ("+transactiontestcode+")";  
						
					 jdbcTemplate.execute(testQryString);
					 final int testCount=seqordertest+transactiontestcode.split(",").length;
						
						
						
					final String updateSeqQuery = "update seqnoregistration set nsequenceno = " + externalOrderSeqNo
												+ " where stablename ='externalorder';"
												+ " update seqnoregistration  set nsequenceno =" + seqordersample
												+ " where stablename ='externalordersample';"
												+ " update seqnoregistration  set nsequenceno = "
												+ testCount + "" + " where stablename ='externalordertest';";
										       
						
						jdbcTemplate.execute(updateSeqQuery);

				jdbcTemplate.execute(
						"insert into registrationtesthistory ( ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
								+ " ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,noffsetdtransactiondate,ntransdatetimezonecode,nsampleapprovalhistorycode,nstatus,nsitecode) "
								+ "select " + seqtesthistory
								+ "+rank()over(order by ntransactiontestcode) nexternalordertestcode,"
                                + "ntransactiontestcode,ntransactionsamplecode,npreregno,"+ userInfo.getNformcode()+","
								+  Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+ ","
								+ userInfo.getNusercode() + ", "
								 + userInfo.getNuserrole() + ", "
								 + userInfo.getNdeputyusercode() + ", "
								 + userInfo.getNdeputyuserrole() + ",'"
								 + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()+ ",-1,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ userInfo.getNtranssitecode() 
								+ " from registrationtest where ntransactiontestcode in("+transactiontestcode+")"		 
						);
				
				final String externalAttachQry = "INSERT INTO public.externalorderattachment("
											+ " nexternalorderattachmentcode,nexternalordercode,sexternalorderid,ncoaparentcode,sreportcomments,ssystemfilename,sreleaseno,nversionno,nusercode,nuserrolecode,dreleasedate,noffsetdreleasedate,nreleasedatetimezonecode,dmodifieddate,ntzmodifieddate,"
											+ " noffsetdmodifieddate,nsitecode,nsourcecoaparentcode,nsourcesitecode,nparentsitecode,nstatus) "
											+ " select  "+externalorderattachmentSeqNo +"+Rank() over (order by nreleaseoutsourceattachcode) as nexternalorderattachmentcode,"+externalOrderSeqNo+",'"+registrationARNO.getSarno()+"',ncoaparentcode,'',ssystemfilename,sreleaseno,nversionno, "
											+ " nusercode,nuserrolecode,dreleasedate,noffsetdreleasedate,nreleasedatetimezonecode," 
					                        + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					                        + userInfo.getNtimezonecode()+ ","
					                        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+ ","
					                        + destinationSitecode  + ","
					                        + "nsourcecoaparentcode,nsourcesitecode"  + ","
					                        + userInfo.getNsitecode()  + ","
					                        +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " from releaseoutsourceattachment  where  "
					//						 + "npreregno="+testInputnew.getNpreregno() + " and nsitecode="+userInfo.getNtranssitecode();
											+ " nreleaseoutsourceattachcode = any (select max(nreleaseoutsourceattachcode) from releaseoutsourceattachment "
											+ " where npreregno="+testInputnew.getNpreregno()
											+ " group by nexternalordercode, nsourcecoaparentcode)";
				
				jdbcTemplate.execute(externalAttachQry);
				int testhistorycount= seqtesthistory+transactiontestcode.split(",").length;
				
				final String updateTestSeqQuery =  " update seqnoregistration  set nsequenceno = "
				+ testhistorycount + "" + " where stablename ='registrationtesthistory';";
				jdbcTemplate.execute(updateTestSeqQuery);
				
				String attachcodecount="select count(nreleaseoutsourceattachcode) from (select max(nreleaseoutsourceattachcode) nreleaseoutsourceattachcode from releaseoutsourceattachment where npreregno="+testInputnew.getNpreregno()+" group by nexternalordercode, nsourcecoaparentcode) a";

				int nattachcodecount = jdbcTemplate.queryForObject(attachcodecount, Integer.class);
				externalorderattachmentSeqNo=externalorderattachmentSeqNo+nattachcodecount;
				final String updateattachSeqQuery =  " update seqnoregistration  set nsequenceno = "+ externalorderattachmentSeqNo + " where stablename ='externalorderattachment';";
				jdbcTemplate.execute(updateattachSeqQuery);
				
					jdbcTemplate.execute(
							"insert into outsourcedetail ( npreregno,ntransactionsamplecode,"
									+ "ntransactiontestcode,bflag,ssampleid,sremarks,sshipmenttracking,doutsourcedate,dmodifieddate, "
									+ " nsitecode,nstatus) "
									+ "select "
									+ " npreregno,"
	                                + "ntransactionsamplecode,ntransactiontestcode,true,'"
									+otherdetails.getSsampleid()+"','"+otherdetails.getSremarks()+"','"+otherdetails.getSshipmenttracking()+"','"
									+otherdetails.getDoutsourcedate()+ "','" 
									//+ dateUtilityFunction.getCurrentDateTime(userInfo) + "','" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," 
									+ userInfo.getNtranssitecode() + "," 
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " from registrationtest where ntransactiontestcode in("+transactiontestcode+")"						 
							);
				

				final String query ="select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()+ "";
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);	
				boolean updateAnalyst = false;
				if(objSettings !=null) {
					updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;

				}	

				final String query1 = "select * from fn_registrationtestget('" + testInputnew.getNpreregno() + "'::text," + "'"
						+ testInputnew.getNtransactionsamplecode() + "'::text" + ",'" + transactiontestcode
						+ "'::text," +  " 3, " + userInfo.getNtranssitecode() + ",'"
						+ userInfo.getSlanguagetypecode() + "','"
						+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
						+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+ updateAnalyst +")";
				System.out.println("fn_registrationtestget:" + query1);

				final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
				
				final String query2 = "select * from fn_registrationsubsampleget('" + testInputnew.getNpreregno() + "'::text," + "'"
						+ testInputnew.getNtransactionsamplecode() + "'::text" + "," 
						+  " 3, " + userInfo.getNtranssitecode() + ",'"
						+ userInfo.getSlanguagetypecode() + "','"
						+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
						+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "')";
				System.out.println("fn_registrationtestget:" + query2);

				final String lstData3 = jdbcTemplate.queryForObject(query2, String.class);
				final Map<String, Object> objMap = new HashMap();
				
				if (lstData2 != null) {
					List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							lstData2, userInfo, true, designTemplateMappingCode, "test");
					List<Map<String, Object>> lstSubSampleData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							lstData3, userInfo, true, designTemplateMappingCode, "subsample");
					objMap.put("OutSourcedTestGet", lstData);
					objMap.put("OutSourcedSubSampleGet", lstSubSampleData);
					objMap.put("Msg", commonFunction.getMultilingualMessage("IDS_TESTSUCCESSFULYOUTSOURCED", userInfo.getSlanguagefilename()));
				} 
				SampleStatusToRelease(testInputnew,testInput,destinationSitecode,designTemplateMappingCode,userInfo);
				
				final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
						userInfo.getSlanguagefilename());
				
				final String sampleGetQuery = "select * from fn_registrationsampleget('" + null + "'::text," + "'"
												+ null + "'::text" + "," + outsourceSampleData.getNsampletypecode() + ","
												+ userInfo.getNusercode() + "," + outsourceSampleData.getNregtypecode() + "," + outsourceSampleData.getNregsubtypecode()
												+ ", -1::integer," + userInfo.getNtranssitecode() + ",'"
												+ testInputnew.getNpreregno() + "'::text," + "'" + userInfo.getSlanguagetypecode() + "'::text," + ""
												+ designTemplateMappingCode + ",'" + registrationMultilingual + "'::text,"
												+ outsourceSampleData.getNapprovalversioncode() + ")";

				
				String lstData1 = jdbcTemplate.queryForObject(sampleGetQuery, String.class);
				
				List<Map<String, Object>> lstDataSample = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
						designTemplateMappingCode, "sample");
				
				objMap.put("OutSourcedSampleGet", lstDataSample);
				
				Map<String,Object> mapOutsourceDetails = (Map<String,Object>) transactionDAOSupport.getOutsourceDetails(Integer.toString(testInputnew.getNpreregno()),Integer.toString(testInputnew.getNtransactionsamplecode()), userInfo).getBody();
				objMap.putAll(mapOutsourceDetails);

				return new ResponseEntity<>(objMap, HttpStatus.OK);

			}
			else
			{
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_TESTALREADYOUTSOURCED", userInfo.getSlanguagefilename()), 
						HttpStatus.CONFLICT);				
			}
		}	
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REQUESTFAILED", userInfo.getSlanguagefilename()), 
					HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createExternalOrderPTB(ExternalOrder objExternalOrder, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final String slockQuery = " lock table lockexternalorder " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
				+ "";
		jdbcTemplate.execute(slockQuery);

		userInfo = new UserInfo();
		
		userInfo.setIsutcenabled(3);
		
		userInfo.setSsitedate("dd/MM/yyyy");
		userInfo.setNmastersitecode((short) -1);
		userInfo.setSlanguagetypecode("en-US");
		userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
		userInfo.setNformcode((short) 137);
		
		userInfo.setSlanguagefilename("Msg_en_US");
		Map<String, Object> returnMap = new HashMap();
		ObjectMapper mapper = new ObjectMapper();
		String externalOrderSampleQuery = "";
		String externalOrderTestQuery = "";
		LOGGER.info(objExternalOrder.toString());

		final String query = "select * from externalorder where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sexternalorderid='"
				+ objExternalOrder.getSexternalorderid() + "'";

		ExternalOrder objExternal = (ExternalOrder) jdbcUtilityFunction.queryForObject(query, ExternalOrder.class, jdbcTemplate);

		if (objExternal == null) {

			final String externalSample = "select * from externalordersample where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sexternalsampleid='"
					+ objExternalOrder.getSexternalsampleid() + "'";

			ExternalOrder objExternalId = (ExternalOrder) jdbcUtilityFunction.queryForObject(externalSample, ExternalOrder.class, jdbcTemplate);

			if (objExternalId == null) {

				final String gender = "select * from gender where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ngendercode='"
						+ objExternalOrder.getNgendercode() + "'";

				Gender objGender = (Gender) jdbcUtilityFunction.queryForObject(gender, Gender.class, jdbcTemplate);

				if (objGender != null) {

					final String preventTB = "select * from preventtbsettings where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc;";
					List<?> lstPreventTB = jdbcTemplate.queryForList(preventTB);

					if (!lstPreventTB.isEmpty()) {
						Map<String, Object> objMap = (Map<String, Object>) lstPreventTB.get(0);

						objExternalOrder.setNproductcatcode((int) objMap.get("nproductcatcode"));					
						
						
						final String testGroup = " select tsh.nspecificationhistorycode,tgt.*,tgts.nspecsampletypecode,tgts.ncomponentcode,tgtt.*,tgts.ncomponentcode "
												 +" from testgroupspecification tgt,"
												 +" testgroupspecificationhistory tsh,testgroupspecsampletype tgts ,preventtbsettings ptbs, testgrouptest tgtt where "
												 +" tgt.nallottedspeccode=tsh.nallottedspeccode  "
												 +" and tgts.ncomponentcode=ptbs.ncomponentcode "
												 +" and tgtt.ntestcode=ptbs.ntestcode "
												 +" and tgts.nspecsampletypecode=tgtt.nspecsampletypecode "
												 +" and tgts.nallottedspeccode=tgt.nallottedspeccode "
												 +" and tsh.nallottedspeccode=tgt.nallottedspeccode "
												 +" and tsh.nspecificationhistorycode ="
												 +" ( select max(th1.nspecificationhistorycode)  from  "
												 +"  testgroupspecificationhistory th1,"
												 +"  treetemplatemanipulation tm1,"
												 +"  testgroupspecification tgs1,"
												 +"  testgroupspecsampletype tgts1 ,"
												 +"  testgrouptest tgtt1,"
												 +"  preventtbsettings ptbs1"
												 +" where th1.ntransactionstatus   = any (select napprovalstatuscode from approvalconfig ap, "
												 +" approvalconfigrole apr,approvalconfigversion apv where  ap.napprovalsubtypecode=1 and "
												 +" ap.nregsubtypecode="+Enumeration.TransactionStatus.NA.gettransactionstatus()
												 +" and ap.nregtypecode="+Enumeration.TransactionStatus.NA.gettransactionstatus()
												 +" and ap.napprovalconfigcode=apv.napprovalconfigcode and  "
												 +" apr.napproveconfversioncode=apv.napproveconfversioncode   "
												 +" and apv.ntransactionstatus<>"+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" and "
												 + " apr.nlevelno="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
												 + " and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
												 + " and apr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
												 + " and apv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")  "
												 +" and tm1.nsampletypecode ="+Enumeration.SampleType.CLINICALSPEC.getType()+" and tm1.nproductcatcode=ptbs1.nproductcatcode  "
												 +" and tgts1.ncomponentcode=ptbs1.ncomponentcode and tgtt1.ntestcode=ptbs1.ntestcode "
												 +" and tm1.ntemplatemanipulationcode=tgs1.ntemplatemanipulationcode and tgs1.nallottedspeccode=th1.nallottedspeccode );";
						 
						List<TestGroupTest> objTSpec = jdbcTemplate.query(testGroup, new TestGroupTest());

						if (!objTSpec.isEmpty()) {

							objExternalOrder.setNallottedspeccode(objTSpec.get(0).getNallottedspeccode());

							JSONObject jsonObject = new JSONObject();

							jsonObject.put("sdob", objExternalOrder.getSdob());
							jsonObject.put("slastname", objExternalOrder.getSlastname());
							jsonObject.put("sfirstname", objExternalOrder.getSfirstname());
							jsonObject.put("ngendercode", objExternalOrder.getNgendercode());
							jsonObject.put("sfathername", objExternalOrder.getSfathername());
							jsonObject.put("sdistrictname", objExternalOrder.getSdistrictname());
							jsonObject.put("scityname", objExternalOrder.getScityname());
							jsonObject.put("dcollectiondate", objExternalOrder.getDcollectiondate());
							jsonObject.put("ssubmitterfirstname", objExternalOrder.getSsubmitterfirstname());
							jsonObject.put("ssubmitterlastname", objExternalOrder.getSsubmitterlastname());
							jsonObject.put("ssubmitteremail", objExternalOrder.getSsubmitteremail());
							jsonObject.put("sinstitutionname", objExternalOrder.getSinstitutionname());
							jsonObject.put("ssitecode", objExternalOrder.getSsitecode());

						

							Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(objExternalOrder.getSdob());
							DateFormat d = new SimpleDateFormat("dd/MM/yyyy");
							
							DateFormat d1f = new SimpleDateFormat("yyyy-MM-dd");

							String d1 = d.format(date1);
							jsonObject.put("sdob", d1);
						

							LocalDate d2 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							Period p = Period.between(d2, LocalDate.now());

							String sage = String.valueOf(p.getYears()) + " "
									+ commonFunction
											.getMultilingualMessage("IDS_YEARS", userInfo.getSlanguagefilename())
									+ " " + String.valueOf(p.getMonths()) + " "
									+ commonFunction.getMultilingualMessage("IDS_MONTHS",
											userInfo.getSlanguagefilename())
									+ " " + String.valueOf(p.getDays()) + " " + commonFunction
											.getMultilingualMessage("IDS_DAYS", userInfo.getSlanguagefilename());

							jsonObject.put("sage", sage);

						

							Patient objPatient = mapper.readValue(jsonObject.toString(), Patient.class);

							String sequencenoquery = "select stablename,nsequenceno from seqnoregistration  where stablename in ('externalorder','externalordertest','externalordersample') order by stablename ";

							List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequencenoquery,
									new SeqNoRegistration());
							returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
									SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

							int seqorderdetail = ((int) returnMap.get("externalorder")) + 1;
							int seqordertest = ((int) returnMap.get("externalordertest"));
							int seqordersample = ((int) returnMap.get("externalordersample"));
							int nexternalordertype = 2;

							String sDate1 = objPatient.getSdob();// .replace("T", " ").replace("Z", " ");
							Date date12 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
							objPatient.setDdob(date12);

							// objExternalOrder.setNparentsitecode(objExternalOrder.getNsitecode());

							objPatient.setSphoneno("-");
							Map<String, Object> patientMap = new HashMap<>();

							final String sQuery = "select spatientid from patientmaster where" + " spatientid = '"
									+ objPatient.getSpatientid() + "'" + " and  nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							final List<Patient> existingPatient = jdbcTemplate.query(sQuery, new Patient());
							String spatientid = "";

							String ssitecode = jsonObject.getString("ssitecode");

							final String site = "select * from site where ssitecode='" + ssitecode + "' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							Site s = (Site) jdbcUtilityFunction.queryForObject(site, Site.class, jdbcTemplate);
							
							
							if(s!=null) {

							objExternalOrder.setNproductcode(-1);
							objExternalOrder.setNordertypecode((short) 2);
							objExternalOrder.setNdiagnosticcasecode((short) 1);
							objExternalOrder.setNparentsitecode(s.getNsitecode());
							objExternalOrder.setNsitecode(s.getNsitecode());
							objExternalOrder.setSorderseqno(String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus()));

							if (existingPatient.isEmpty()) {
								patientMap = patientDAO.savePatient(objPatient, userInfo);
								spatientid = patientMap.containsKey("spatientid")
										? (String) patientMap.get("spatientid")
										: null;
							} else {
								spatientid = existingPatient.get(0).getSpatientid();
							}
							
							jsonObject.put("spatientid", spatientid);
							jsonObject.put("ddob", d1f.format(date1));
							
							objExternalOrder.setJsondata(jsonObject.toMap());

							String externalOrder = "Insert into externalorder(nexternalordercode,nordertypecode,nexternalordertypecode,nproductcatcode,nproductcode,ngendercode,ninstitutioncode,ninstitutionsitecode,"
									+ "ndiagnosticcasecode,nallottedspeccode,nusercode,sorderseqno,spatientid,ssubmittercode,sexternalorderid,jsondata,ndefaultstatus,dmodifieddate,ntransactionstatus,nsitecode,nstatus,nparentsitecode)"
									+ "values(" + seqorderdetail + "," + objExternalOrder.getNordertypecode() + ", "
									+ nexternalordertype + " ," + objExternalOrder.getNproductcatcode() + ","
									+ objExternalOrder.getNproductcode() + "," + objExternalOrder.getNgendercode() + ","
									+ (objExternalOrder.getNinstitutioncode() == 0 ? -1
											: objExternalOrder.getNinstitutioncode())
									+ ","
									+ (objExternalOrder.getNinstitutionsitecode() == 0 ? -1
											: objExternalOrder.getNinstitutionsitecode())
									+ "," + objExternalOrder.getNdiagnosticcasecode() + ","
									+ objExternalOrder.getNallottedspeccode() + "," + -1 + ",N'"
									+ objExternalOrder.getSorderseqno() + "', N'" +spatientid + "',"
									+ "N'" + stringUtilityFunction.replaceQuote(objExternalOrder.getSsubmittercode()) + "'," + "N'"
									+ stringUtilityFunction.replaceQuote(objExternalOrder.getSexternalorderid()) + "', '"
									+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',"
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
									+ objExternalOrder.getNsitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
									+ objExternalOrder.getNparentsitecode() + ")";

							jdbcTemplate.execute(externalOrder);

							String updateSeqQuery = "update seqnoregistration  set nsequenceno = " + seqorderdetail
									+ " where stablename ='externalorder';";

							String nspecTypecode = objTSpec.stream()
									.map(x -> String.valueOf(x.getNspecsampletypecode()))
									.collect(Collectors.joining(","));

							final String sampleType = "select * from testgroupspecsampletype where nspecsampletypecode in("
									+ nspecTypecode + ") and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
							List<TestGroupSpecSampleType> lstSpec = jdbcTemplate.query(sampleType,
									new TestGroupSpecSampleType());

							for (TestGroupSpecSampleType container : lstSpec) {
								// ALPD-3575
								String dsampleCollectionDateTime = null;
								int nsampleAppearanceCode = -1;
								if (jsonObject.has("dcollectiondate")) {
									dsampleCollectionDateTime = "'"+jsonObject.get("dcollectiondate")+"'";
								}		
								if(jsonObject.has("nsampleappearancecode")) {
									nsampleAppearanceCode = (int) jsonObject.get("nsampleappearancecode");
								}
								seqordersample++;
								externalOrderSampleQuery += " (" + seqordersample + "," + seqorderdetail + ","
										+ container.getNcomponentcode() + "," + 0 + "," + -1 + ",'"
										+ stringUtilityFunction.replaceQuote(objExternalOrder.getSexternalsampleid()) + "',"
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + objExternalOrder.getNsitecode()
										+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objExternalOrder.getNparentsitecode()+ ", "+ dsampleCollectionDateTime+", "
										+ nsampleAppearanceCode+"),";

								List<TestGroupTest> lts1 = objTSpec.stream()
										.filter(x -> x.getNspecsampletypecode() == container.getNspecsampletypecode())
										.collect(Collectors.toList());

								for (TestGroupTest test : lts1) {
									seqordertest++;
									externalOrderTestQuery += "(" + seqordertest + "," + seqordersample + ","
											+ seqorderdetail + "," + test.getNtestpackagecode() + ","
											+ test.getNcontainertypecode() + " ," + test.getNtestcode() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + objExternalOrder.getNsitecode()
											+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ objExternalOrder.getNparentsitecode() + "),";
								}
							}

							externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,ntransactionstatus,dmodifieddate,nsitecode,nstatus, nparentsitecode, dsamplecollectiondatetime, nsampleappearancecode) values "
									+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1)
									+ ";";

							externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
									+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";

							updateSeqQuery = updateSeqQuery + "update seqnoregistration  set nsequenceno ="
									+ seqordersample
									+ " where stablename ='externalordersample';update seqnoregistration  set nsequenceno = "
									+ seqordertest + "" + " where stablename ='externalordertest';";

							jdbcTemplate.execute(externalOrderSampleQuery);
							jdbcTemplate.execute(externalOrderTestQuery);
							jdbcTemplate.execute(updateSeqQuery);

							return new ResponseEntity<Object>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),
									HttpStatus.OK);

						}else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_SITEISNOTAVAILABLE",
											userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
							
						}else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_TESTSPECIFICATIONISNOTAVALIABLE",
											userInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONNOTAVAILABLE",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_GENDERISMISMATCHED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEORDERALREADYRECEIVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}	
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ORDERALREADYRECEIVED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	public ResponseEntity<Object> getExternalOrderType(final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();

		final String strQuery = "select nexternalordertypecode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sdisplayname'->>'en-US') as sexternalordertypename "
				+ " from externalordertype  where nstatus =1";

		final List<Map<String, Object>> lstExternalOrderType = jdbcTemplate.queryForList(strQuery);

		returnMap.put("ExternalOrderType", lstExternalOrderType);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
		
	public ResponseEntity<Object> cancelExternalOrderPreventTb(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub

		String orderId = (String) inputMap.get("orderid");
		String sampleId = (String) inputMap.get("sampleid");
		Map<String, Object> returnMap = new HashMap<String, Object>();

		String query = "select * from externalordersample es,externalorder eo where " + "es.sexternalsampleid='"
				+ sampleId + "'" + " and es.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and es.nexternalordercode=eo.nexternalordercode and" + " eo.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  eo.sexternalorderid='"
				+ orderId + "'";

		ExternalOrderSample externalOrderSample = (ExternalOrderSample) jdbcUtilityFunction.queryForObject(query,
				ExternalOrderSample.class, jdbcTemplate);

		if (externalOrderSample != null) {

			if (externalOrderSample.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {

				query = " update externalorder set " + " ntransactionstatus ="
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where  sexternalorderid='"
						+ orderId + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ";";

				query = query + " update externalordersample set ntransactionstatus="
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where  sexternalsampleid='"
						+ sampleId + "'";

				jdbcTemplate.execute(query);
				returnMap.put("Status", "SUCCESS");

			} else if (externalOrderSample.getNtransactionstatus() == Enumeration.TransactionStatus.PREREGISTER
					.gettransactionstatus()
					|| externalOrderSample.getNtransactionstatus() == Enumeration.TransactionStatus.REGISTERED
							.gettransactionstatus()) {

				Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
				JSONObject actionType = new JSONObject();
				JSONArray actionTypeTestArray = new JSONArray();
				JSONArray actionTypeSampleArray = new JSONArray();

				JSONArray actionTypeSubsampleArray = new JSONArray();
				JSONObject jsonAuditOld = new JSONObject();
				JSONObject jsonAuditNew = new JSONObject();

				query = " select * from registration r,registrationarno arno ,registrationhistory rh"
						+ " where r.jsondata->>'OrderIdData'='" + orderId + "'"
						+ " and r.npreregno=arno.npreregno and r.npreregno=rh.npreregno  and rh.nreghistorycode "
						+ " in (select max(nreghistorycode) from registrationhistory rh1,registration r2"
						+ " where r2.npreregno=rh1.npreregno and  r2.jsondata->>'OrderIdData'='" + orderId
						+ "' group by rh1.npreregno )" + " and rh.ntransactionstatus not in" + " ("
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")";

				List<Registration> registration = jdbcTemplate.query(query, new Registration());

				Map<String, Object> sendMapForGetSampleRecord = new HashMap<String, Object>();

				if (!registration.isEmpty()) {

					query = " select ntransactionsamplecode from registrationsample where npreregno='"
							+ registration.get(0).getNpreregno() + "'";

					List<RegistrationSample> rs = jdbcTemplate.query(query, new RegistrationSample());

					String ntransactionsamplecode = rs.stream().map(x -> String.valueOf(x.getNtransactionsamplecode()))
							.collect(Collectors.joining(","));

					query = " select ntransactiontestcode from registrationtest " + "where npreregno='"
							+ registration.get(0).getNpreregno() + "' and ntransactionsamplecode in ('"
							+ ntransactionsamplecode + "')";

					List<RegistrationTest> rt = jdbcTemplate.query(query, new RegistrationTest());

					String ntransactiontestcode = rt.stream().map(x -> String.valueOf(x.getNtransactiontestcode()))
							.collect(Collectors.joining(","));

					query = " select * from site s,siteconfig sc " + " where s.nsitecode=sc.nsitecode and s.nsitecode="
							+ registration.get(0).getNsitecode();

					SiteConfig sc = (SiteConfig) jdbcUtilityFunction.queryForObject(query, SiteConfig.class, jdbcTemplate);

					UserInfo uI = new UserInfo();
					uI.setSlanguagefilename("Msg_en_US");
					uI.setSlanguagetypecode("en-US");
					uI.setNmastersitecode((short) -1);
					uI.setNtranssitecode((short) registration.get(0).getNsitecode());
					uI.setNusercode(-1);
					uI.setNuserrole(-1);
					uI.setNformcode((short) 43);
					uI.setSsitedatetime(sc.getSsitedatetime());

					uI.setSsitedate(sc.getSsitedate());
					uI.setSsitereportdate(sc.getSsitereportdate());
					uI.setSsitereportdatetime(sc.getSsitereportdatetime());

					uI.setSpgsitedatetime(sc.getSpgdatetime());
					uI.setSpgdatetimeformat(sc.getSpgdatetime());

					uI.setSsitedate(sc.getSsitedate());

					uI.setSdatetimeformat(sc.getSsitedatetime());

					String npreregno = registration.stream().map(x -> String.valueOf(x.getNpreregno()))
							.collect(Collectors.joining(","));


					sendMapForGetSampleRecord.put("nregtypecode", registration.get(0).getNregtypecode());
					sendMapForGetSampleRecord.put("nregsubtypecode", registration.get(0).getNregsubtypecode());
					sendMapForGetSampleRecord.put("nsampletypecode", registration.get(0).getNsampletypecode());
					sendMapForGetSampleRecord.put("ncontrolcode", 403);
					sendMapForGetSampleRecord.put("nformcode", 43);
					sendMapForGetSampleRecord.put("npreregno", npreregno);
					sendMapForGetSampleRecord.put("ndesigntemplatemappingcode",
							registration.get(0).getNdesigntemplatemappingcode());
					sendMapForGetSampleRecord.put("napproveconfversioncode",
							registration.get(0).getNapprovalversioncode());
					sendMapForGetSampleRecord.put("nfilterstatus", (short) -1);
					sendMapForGetSampleRecord.put("FromDate", "");
					sendMapForGetSampleRecord.put("ToDate", "");
					sendMapForGetSampleRecord.put("userinfo", uI);
					sendMapForGetSampleRecord.put("checkBoxOperation", 3);
					sendMapForGetSampleRecord.put("nflag", 2);
					sendMapForGetSampleRecord.put("ntype", 3);
					sendMapForGetSampleRecord.put("withoutgetparameter", 3);
					sendMapForGetSampleRecord.put("nneedsubsample", true);
					sendMapForGetSampleRecord.put("activeTestTab", "IDS_PARAMETERRESULTS");
					sendMapForGetSampleRecord.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
					sendMapForGetSampleRecord.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
					sendMapForGetSampleRecord.put("ntransactionsamplecode", ntransactionsamplecode);

					sendMapForGetSampleRecord.put("ntransactiontestcode", ntransactiontestcode);

					Map<String, Object> rtnMap = new HashMap();

					
					rtnMap = transactionDAOSupport.seqNoSampleInsert(sendMapForGetSampleRecord);

					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
							.equals(rtnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
						return new ResponseEntity<>(rtnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
								HttpStatus.EXPECTATION_FAILED);
					}

					Map<String, Object> returnMap1 = registrationDAOSupport.getDynamicRegistration(sendMapForGetSampleRecord,
							uI);

					List<Map<String, Object>> oldRegistration = (List<Map<String, Object>>) returnMap1
							.get("RegistrationGetSample");

					List<Map<String, Object>> oldlstSubSample = (List<Map<String, Object>>) returnMap1
							.get("RegistrationGetSubSample");

					// oldlstSubSample.filter(x->x.get("ntransactionstatus").equalto);

					ArrayList<Map<String, Object>> lstSubSampleOld = (ArrayList<Map<String, Object>>) oldlstSubSample
							.stream()
							.filter(ss -> !(ss.get("ntransactionstatus")
									.equals(Enumeration.TransactionStatus.REJECTED.gettransactionstatus())
									|| ss.get("ntransactionstatus")
											.equals(Enumeration.TransactionStatus.CANCELED.gettransactionstatus())))
							.collect(Collectors.toList());

//					
					if (!lstSubSampleOld.isEmpty()) {

						List<Map<String, Object>> lstOldTest = transactionDAOSupport.testAuditGet(sendMapForGetSampleRecord,
								uI);

						int nsampleseqno = (int) rtnMap.get("registrationhistory");
						int nsubsampleseqno = (int) rtnMap.get("registrationsamplehistory");
						int ntestseqno = (int) rtnMap.get("registrationtesthistory");
						String spreregno = (String) rtnMap.get("insertpreregno");

						query = " update externalorder set " + " ntransactionstatus ="
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " where  sexternalorderid='" + orderId + "' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						query = query + " update externalordersample set ntransactionstatus="
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " where nexternalordercode in (select nexternalordercode from externalorder where sexternalorderid='"
								+ orderId + "' );";

						jdbcTemplate.execute(query);

						String insertReghistory = "insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,"
								+ "nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode, nstatus) "
								+ "select " + nsampleseqno
								+ "+rank()over(order by r.npreregno) nreghistorycode,r.npreregno,case when rh.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ " when rh.ntransactionstatus="
								+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " end  as ntransactionstatus," + "'" + dateUtilityFunction.getCurrentDateTime(uI) + "'  dtransactiondate,"
								+ uI.getNusercode() + " nusercode," + uI.getNuserrole() + " nuserrolecode," + ""
								+ uI.getNdeputyusercode() + "ndeputyusercode," + uI.getNdeputyuserrole()
								+ " ndeputyuserrolecode,'' scomments," + uI.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ "from registration r,registrationhistory rh   " + " where r.npreregno in ("
								+ spreregno + ") " + " and r.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and r.nsitecode = rh.nsitecode and rh.nsitecode=" + uI.getNtranssitecode()
								+ " and rh.nreghistorycode in (select max(nreghistorycode) from registrationhistory rh1 where "
								+ " rh1.npreregno=r.npreregno and r.npreregno in (" + spreregno + ") "
								+ " and rh1.nsitecode= " + uI.getNtranssitecode() + " group by rh1.npreregno) "
								+ " and	rh.npreregno in (" + spreregno + ")" + " and rh.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " order by npreregno;";
						jdbcTemplate.execute(insertReghistory);

						final String query1 = "insert into registrationsamplehistory(nsamplehistorycode,ntransactionsamplecode,"
								+ "npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,"
								+ "scomments,nsitecode,nstatus)" + "select " + nsubsampleseqno
								+ "+rank()over(order by rs.ntransactionsamplecode,rs.npreregno) nsamplehistorycode, rs.ntransactionsamplecode,"
								+ " rs.npreregno,case when rsh.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " end as ntransactionstatus,'" + dateUtilityFunction.getCurrentDateTime(uI) + "' dtransactiondate,"
								+ uI.getNusercode() + " nusercode," + "" + uI.getNuserrole() + " nuserrolecode,"
								+ uI.getNdeputyusercode() + "ndeputyusercode," + uI.getNdeputyuserrole()
								+ " ndeputyuserrolecode," + "'' scomments," + uI.getNtranssitecode() + " nsitecode,"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " From registrationsample rs,registrationsamplehistory rsh  where "
								+ " rsh.npreregno= rs.npreregno and rsh.ntransactionsamplecode=rs.ntransactionsamplecode "
								+ " and rs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  and rsh.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rs.nsitecode = rsh.nsitecode and rsh.nsitecode=" + uI.getNtranssitecode()
								+ " and rsh.nsamplehistorycode in (select max(nsamplehistorycode) from registrationsamplehistory rsh1 where  "
								+ " rsh1.npreregno=rs.npreregno and rs.npreregno in (" + spreregno
								+ ")  and rsh1.nsitecode=" + uI.getNtranssitecode()
								+ " group by rsh1.npreregno,rsh1.ntransactionsamplecode ) and rsh.npreregno in ("
								+ spreregno + ")  " + " and rsh.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " order by rs.npreregno,rs.ntransactionsamplecode ";
						jdbcTemplate.execute(query1);

						final String query2 = "insert into registrationtesthistory(ntesthistorycode,ntransactiontestcode,"
								+ "ntransactionsamplecode,npreregno,nformcode,ntransactionstatus,nusercode,nuserrolecode,"
								+ "ndeputyusercode,ndeputyuserrolecode,scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode,nstatus)"
								+ "select " + ntestseqno
								+ "+rank()over(order by  rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ) ntesthistorycode, rt.ntransactiontestcode,"
								+ " rt.ntransactionsamplecode," + " rt.npreregno," + uI.getNformcode() + " nformcode,"
								+ " case when rth.ntransactionstatus ="
								+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ " when rth.ntransactionstatus="
								+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " then "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " end  as ntransactionstatus," + uI.getNusercode() + " nusercode," + uI.getNuserrole()
								+ " nuserrolecode," + "" + uI.getNdeputyusercode() + "ndeputyusercode,"
								+ uI.getNdeputyuserrole() + " ndeputyuserrolecode,N'CANCELED' scomments," + "'"
								+ dateUtilityFunction.getCurrentDateTime(uI) + "' dtransactiondate,-1," + uI.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
								+ " from registrationtest rt,registrationtesthistory rth where rt.npreregno in ("
								+ spreregno + ") and rt.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
								+ " and rt.ntransactionsamplecode =rth.ntransactionsamplecode  "
								+ " and rt.nsitecode = rth.nsitecode and rth.nsitecode=" + uI.getNtranssitecode()
								+ " and rt.npreregno=rth.npreregno  "
								+ " and rth.ntesthistorycode in (select max(ntesthistorycode) from registrationtesthistory rth1  "
								+ " where rth1.ntransactiontestcode=rt.ntransactiontestcode " + " and rth1.nsitecode ="
								+ uI.getNtranssitecode() + " and rth1.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
								+ " and rth.ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and rt.npreregno in (" + spreregno + ")  "
								+ " order by rt.ntransactiontestcode,rt.ntransactionsamplecode,rt.npreregno ";

						jdbcTemplate.execute(query2);

						Map<String, Object> returnMapNew = registrationDAOSupport
								.getDynamicRegistration(sendMapForGetSampleRecord, uI);
						jsonAuditOld.put("registration", oldRegistration);
						jsonAuditNew.put("registration",
								(List<Map<String, Object>>) returnMapNew.get("RegistrationGetSample"));

						oldRegistration.forEach(x -> {
							if (Integer.parseInt(((Map<String, Object>) x).get("ntransactionstatus")
									.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
								actionTypeSampleArray.put("IDS_REJECTSAMPLE");
							} else {
								actionTypeSampleArray.put("IDS_CANCELSAMPLE");
							}
						});


						List<Map<String, Object>> lstSubSample = (List<Map<String, Object>>) returnMapNew
								.get("RegistrationGetSubSample");

						List<Map<String, Object>> lstSubSampleNew = (List<Map<String, Object>>) lstSubSample.stream()
								.filter(subSampleNew -> ((List<Map<String, Object>>) lstSubSampleOld).stream()
										.anyMatch(subSampleOld -> subSampleOld.get("ntransactionsamplecode")
												.equals(subSampleNew.get("ntransactionsamplecode"))))
								.collect(Collectors.toList());


						jsonAuditOld.put("registrationsample", lstSubSampleOld);
						jsonAuditNew.put("registrationsample", lstSubSampleNew);

						lstSubSampleOld.forEach(t -> {
							if (Integer.parseInt(((Map<String, Object>) t).get("ntransactionstatus")
									.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
								actionTypeSubsampleArray.put("IDS_REJECTSUBSAMPLE");
							} else {
								actionTypeSubsampleArray.put("IDS_CANCELSUBSAMPLE");
							}
						});

						Map<String, Object> objMap = (Map<String, Object>) transactionDAOSupport
								.getRegistrationTestAudit(sendMapForGetSampleRecord, uI).getBody();

						List<Map<String, Object>> lstTest = (List<Map<String, Object>>) objMap
								.get("RegistrationGetTest");

						List<Map<String, Object>> lstTestNew = (List<Map<String, Object>>) lstTest.stream()
								.filter(testNew -> ((List<Map<String, Object>>) lstOldTest).stream()
										.anyMatch(testOld -> testOld.get("ntransactiontestcode")
												.equals(testNew.get("ntransactiontestcode"))))
								.collect(Collectors.toList());
						jsonAuditNew.put("registrationtest", lstTestNew);

						auditmap.put("nregtypecode", sendMapForGetSampleRecord.get("nregtypecode"));
						auditmap.put("nregsubtypecode", sendMapForGetSampleRecord.get("nregsubtypecode"));
						auditmap.put("ndesigntemplatemappingcode",
								sendMapForGetSampleRecord.get("ndesigntemplatemappingcode"));

						lstOldTest.forEach(t -> {
							if (Integer.parseInt(((Map<String, Object>) t).get("ntransactionstatus")
									.toString()) == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()) {
								actionTypeTestArray.put("IDS_REJECTTEST");
							} else {
								actionTypeTestArray.put("IDS_CANCELTEST");
							}
						});
						jsonAuditOld.put("registrationtest", lstOldTest);

						actionType.put("registration", actionTypeSampleArray);
						actionType.put("registrationsample", actionTypeSubsampleArray);
						actionType.put("registrationtest", actionTypeTestArray);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, auditmap, false, uI);
						returnMap.put("Status", "SUCCESS");
					}
				} else {
					returnMap.put("Status", "ALREADY CANCELED");
				}
			} else {
				returnMap.put("Status",
						externalOrderSample.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
								.gettransactionstatus() ? "ALREADY CANCELED" : "CANNOT CANCEL");
			}

		} else {
			returnMap.put("Status", "SAMPLE NOT AVAILABLE");
		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	
	
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> SendToPortalReport (final UserInfo userInfo) throws Exception {
		final Map<String, Object> inputMap = new LinkedHashMap<String, Object>();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		List<String> objreportpresent = new ArrayList<String>();
		
		final String getGeneratedreport = "select chg.ncoareporthistorycode,ch.ncoaparentcode,cc.npreregno,eor.nsitecode,max(ch.ssystemfilename) ssystemfilename from "
				+ " coareporthistorygeneration chg,coareporthistory ch,coachild  cc,registration r,externalorderreleasestatus eor "
				+ " where (r.jsonuidata->>'nexternalordertypecode')::int="+Enumeration.ExternalOrderType.PORTAL.getExternalOrderType()+" and "
				+ " eor.ncoaparentcode=ch.ncoaparentcode "
				+ " and eor.npreregno=cc.npreregno and "
				+ " cc.ncoaparentcode = ch.ncoaparentcode and r.npreregno=cc.npreregno "
				+ " and chg.ncoareporthistorycode = ch.ncoareporthistorycode "
				+ " and eor.nportalstatus in ("+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+","+Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()+") and chg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and chg.nreportstatus = "+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				+ " and eor.nsitecode=chg.nsitecode "
				+ " and eor.nsitecode=ch.nsitecode "
				+ " and eor.nsitecode=cc.nsitecode "
				+ " and eor.nsitecode=r.nsitecode "
				+ " and chg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ch.ncoaparentcode,cc.npreregno,chg.ncoareporthistorycode,eor.nsitecode;";		
		
		List<Map<String, Object>> lstReportGen = jdbcTemplate.queryForList(getGeneratedreport);
		
		final String  strnpreregno = lstReportGen.stream().map(objpreregno -> String.valueOf(objpreregno.get("npreregno"))).collect(Collectors.joining(","));
		final List<String> lstNpreregno = Arrays.asList(strnpreregno);
		
		if (!lstReportGen.isEmpty()) {
			
			String strQuery = "select nreportsettingcode nsettingcode,ssettingvalue from reportsettings where nreportsettingcode = "+Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()+" "
					+ " union All "
					+ " select nsettingcode,ssettingvalue from settings where nsettingcode = 24;";
			
			List<Settings> lstSettingValue = jdbcTemplate.query(strQuery, new Settings());
			Map<Short, String> mapSeqno = lstSettingValue.stream().collect(Collectors.toMap(Settings::getNsettingcode,
					Settings -> (String)Settings.getSsettingvalue()));
			
			final String fileDownloadURL = mapSeqno.get((short)4).toString();
			final String portalUrlPath = mapSeqno.get((short)24).toString();
				
			inputMap.put("allPreregno",strnpreregno);
			final Map<String, Object> registrationDatas = registrationDAOSupport.getRegistrationLabel(inputMap, userInfo);
			String sorderseqno = "";
			
			if (!registrationDatas.isEmpty()) {
				HttpPost request = new HttpPost(portalUrlPath + "/portal/FileUploadReport");
				for (int k = 0; k < lstReportGen.size(); k++) {
					Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
							.get(String.valueOf(lstReportGen.get(k).get("npreregno")));
					//ALPD-4039 Added condition if (registrationData!=null) 22/05/2024 By Aravindh
					if(registrationData!=null)
					{
						File textFile = new File((fileDownloadURL + lstReportGen.get(k).get("ssystemfilename")).toString());
						MultipartEntityBuilder builder = MultipartEntityBuilder.create()
														.addPart("file", new FileBody(textFile))
														.addTextBody("filename", lstReportGen.get(k).get("ssystemfilename").toString())
														.addTextBody("sampleid", (String) registrationData.get("sorderseqno").toString());
						HttpEntity entity = builder.build();
						request.setEntity(entity);
						
						//Below lines of code to be updated as per the latest version --L.Subashini
//						HttpClient client = HttpClientBuilder.create().build();
//						HttpResponse response = client.execute(request);
//						int statusCode = response.getStatusLine().getStatusCode();
//					  
//			            if(statusCode == 200) {
//			            	
//			            	String str = "update externalorderreleasestatus set nportalstatus="+Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus()+" where ncoaparentcode="+(int)lstReportGen.get(k).get("ncoaparentcode") +" and npreregno="+(int)lstReportGen.get(k).get("npreregno")+" and nsitecode="+(int)lstReportGen.get(k).get("nsitecode");
//							jdbcTemplate.execute(str);
//
//			            } else {
//			            	String str="update externalorderreleasestatus set nportalstatus="+Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()+" where ncoaparentcode="+(int)lstReportGen.get(k).get("ncoaparentcode") +" and npreregno="+(int)lstReportGen.get(k).get("npreregno")+" and nsitecode="+(int)lstReportGen.get(k).get("nsitecode");
//							jdbcTemplate.execute(str);
//
//			            }
//			            
					}
			            
				}
			}	
		}
		
		return null;
	}

//	@Override
//	public ResponseEntity<Object> createExternalOrderOpenMrs(ExternalOrder objExternalOrder, UserInfo userInfo) throws Exception {
//		// TODO Auto-generated method stub
//		final String slockQuery = " lock table lockexternalorder " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
//				+ "";
//		jdbcTemplate.execute(slockQuery);
//		userInfo = new UserInfo();
//		userInfo.setIsutcenabled(3);
//		userInfo.setSsitedate("dd/MM/yyyy");
//		userInfo.setNmastersitecode((short) -1);
//		userInfo.setSlanguagetypecode("en-US");
//		userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
//		userInfo.setNformcode((short) 137);
//		userInfo.setSlanguagefilename("Msg_en_US");
//		Map<String, Object> returnMap = new HashMap();
//		ObjectMapper mapper = new ObjectMapper();
//		String externalOrderSampleQuery = "";
//		String externalOrderTestQuery = "";
//		logger.info(objExternalOrder.toString());
//		final String query = "select * from externalorder where nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sexternalorderid='"
//				+ objExternalOrder.getSexternalorderid() + "'";
//
//		ExternalOrder objExternal = (ExternalOrder) jdbcQueryForObject(query, ExternalOrder.class);
//		if (objExternal == null) {
//			final String externalSample = "select * from externalordersample where nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sexternalsampleid='"
//					+ objExternalOrder.getSexternalsampleid() + "'";
//
//			ExternalOrder objExternalId = (ExternalOrder) jdbcQueryForObject(externalSample, ExternalOrder.class);
//
//			if (objExternalId == null) {
//
//				final String gender = "select * from gender where nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ngendercode='"
//						+ objExternalOrder.getNgendercode() + "'";
//
//				Gender objGender = (Gender) jdbcQueryForObject(gender, Gender.class);
//				if (objGender != null) {
//						objExternalOrder.setNproductcatcode(1);
//						
//						final String testGroup = " select tsh.nspecificationhistorycode,tgt.nallottedspeccode "
//								 +" from testgroupspecification tgt,"
//								 +" testgroupspecificationhistory tsh where "
//								 +" tgt.nallottedspeccode=tsh.nallottedspeccode  "
//								 +" and tsh.nspecificationhistorycode ="
//								 +" ( select max(th1.nspecificationhistorycode)  from  "
//								 +"  testgroupspecificationhistory th1,"
//								 +"  treetemplatemanipulation tm1,"
//								 +"  testgroupspecification tgs1 "
//								 +" where th1.ntransactionstatus   = any (select napprovalstatuscode from approvalconfig ap, "
//								 +" approvalconfigrole apr,approvalconfigversion apv where  ap.napprovalsubtypecode=1 and "
//								 +" ap.nregsubtypecode="+Enumeration.TransactionStatus.NA.gettransactionstatus()
//								 +" and ap.nregtypecode="+Enumeration.TransactionStatus.NA.gettransactionstatus()
//								 +" and ap.napprovalconfigcode=apv.napprovalconfigcode and  "
//								 +" apr.napproveconfversioncode=apv.napproveconfversioncode   "
//								 +" and apv.ntransactionstatus<>"+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" and "
//								 + " apr.nlevelno="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//								 + " and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
//								 + " and apr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								 + " and apv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")  "
//								 +" and tm1.nsampletypecode ="+Enumeration.SampleType.CLINICALSPEC.getType()+" and tm1.nproductcatcode="+objExternalOrder.getNproductcatcode()
//								 +" and tm1.ntemplatemanipulationcode=tgs1.ntemplatemanipulationcode and tgs1.nallottedspeccode=th1.nallottedspeccode );";
//								 
//								List<TestGroupTest> objTSpec = jdbcTemplate.query(testGroup, new TestGroupTest());
//						
//						
//						if(!objTSpec.isEmpty()) {
//							
//							objExternalOrder.setNallottedspeccode(objTSpec.get(0).getNallottedspeccode());	
//						}else {
//							
//							objExternalOrder.setNallottedspeccode(-1);		
//						}
//												
//							JSONObject jsonObject = new JSONObject();
//							jsonObject.put("sdob", objExternalOrder.getSdob());
//							jsonObject.put("slastname", objExternalOrder.getSlastname());
//							jsonObject.put("sfirstname", objExternalOrder.getSfirstname());
//							jsonObject.put("ngendercode", objExternalOrder.getNgendercode());
//							jsonObject.put("sfathername", objExternalOrder.getSfathername());
//							jsonObject.put("sdistrictname", objExternalOrder.getSdistrictname());
//							jsonObject.put("scityname", objExternalOrder.getScityname());
//							jsonObject.put("dcollectiondate", objExternalOrder.getDcollectiondate());
//							jsonObject.put("ssubmitterfirstname", objExternalOrder.getSsubmitterfirstname());
//							jsonObject.put("ssubmitterlastname", objExternalOrder.getSsubmitterlastname());
//							jsonObject.put("ssubmitteremail", objExternalOrder.getSsubmitteremail());
//							jsonObject.put("sinstitutionname", objExternalOrder.getSinstitutionname());
//							jsonObject.put("ssitecode", objExternalOrder.getSsitecode());
//							
//							
//							Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(objExternalOrder.getSdob());
//							DateFormat d = new SimpleDateFormat("dd/MM/yyyy");				
//							DateFormat d1f = new SimpleDateFormat("yyyy-MM-dd");
//							String d1 = d.format(date1);
//							jsonObject.put("sdob", d1);			
//							LocalDate d2 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//							Period p = Period.between(d2, LocalDate.now());
//
//							String sage = String.valueOf(p.getYears()) + " "
//									+ commonFunction
//											.getMultilingualMessage("IDS_YEARS", userInfo.getSlanguagefilename())
//									+ " " + String.valueOf(p.getMonths()) + " "
//									+ commonFunction.getMultilingualMessage("IDS_MONTHS",
//											userInfo.getSlanguagefilename())
//									+ " " + String.valueOf(p.getDays()) + " " + commonFunction
//											.getMultilingualMessage("IDS_DAYS", userInfo.getSlanguagefilename());
//
//							jsonObject.put("sage", sage);		
//							Patient objPatient = mapper.readValue(jsonObject.toString(), Patient.class);
//							
//							jsonObject.put("spatientrefid", objExternalOrder.getSpatientrefid());
//							jsonObject.put("sordertestcomment", objExternalOrder.getSordertestcomment());
//							String sequencenoquery = "select stablename,nsequenceno from seqnoregistration  where stablename"
//									+ " in ('externalorder','externalordertest','externalordersample') order by stablename ";
//
//							List<SeqNoRegistration> lstSeqNo = jdbcTemplate.query(sequencenoquery,
//									new SeqNoRegistration());
//							returnMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
//									SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));
//
//							int seqorderdetail = ((int) returnMap.get("externalorder")) + 1;
//							//int seqordertest = ((int) returnMap.get("externalordertest"));
//							int seqordersample = ((int) returnMap.get("externalordersample"));
//							int nexternalordertype =3;
//							String sDate1 = objPatient.getSdob();// .replace("T", " ").replace("Z", " ");
//							Date date12 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//							objPatient.setDdob(date12);
//							objPatient.setSphoneno("-");
//							Map<String, Object> patientMap = new HashMap<>();
//							String spatientid = "";
//							String ssitecode = jsonObject.getString("ssitecode");
//							final String site = "select * from site where ssitecode='" + ssitecode + "' and nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//							Site s = (Site) jdbcQueryForObject(site, Site.class);										
//							if(s!=null) {
//							objExternalOrder.setNproductcode(-1);
//							objExternalOrder.setNordertypecode((short) 2);
//							objExternalOrder.setNdiagnosticcasecode((short) 1);
//							objExternalOrder.setNparentsitecode(s.getNsitecode());
//							objExternalOrder.setNsitecode(s.getNsitecode());
//							objExternalOrder.setSorderseqno(String.valueOf(Enumeration.TransactionStatus.NA.gettransactionstatus()));	
//								patientMap = patientDAO.savePatient(objPatient, userInfo);
//								spatientid = patientMap.containsKey("spatientid")
//										? (String) patientMap.get("spatientid")
//										: null;
//							
//							jsonObject.put("spatientid", spatientid);
//							jsonObject.put("ddob", d1f.format(date1));		
//							jsonObject.put("extras", objExternalOrder.getExtras());
//							objExternalOrder.setJsondata(jsonObject.toMap());
//							String externalOrder = "Insert into externalorder(nexternalordercode,nordertypecode,nexternalordertypecode,nproductcatcode,nproductcode,ngendercode,ninstitutioncode,ninstitutionsitecode,"
//									+ "ndiagnosticcasecode,nallottedspeccode,nusercode,sorderseqno,spatientid,ssubmittercode,sexternalorderid,jsondata,ndefaultstatus,dmodifieddate,ntransactionstatus,nsitecode,nstatus,nparentsitecode)"
//									+ "values(" + seqorderdetail + "," + objExternalOrder.getNordertypecode() + ", "
//									+ nexternalordertype + " ," + objExternalOrder.getNproductcatcode() + ","
//									+ objExternalOrder.getNproductcode() + "," + objExternalOrder.getNgendercode() + ","
//									+ (objExternalOrder.getNinstitutioncode() == 0 ? -1
//											: objExternalOrder.getNinstitutioncode())
//									+ ","
//									+ (objExternalOrder.getNinstitutionsitecode() == 0 ? -1
//											: objExternalOrder.getNinstitutionsitecode())
//									+ "," + objExternalOrder.getNdiagnosticcasecode() + ","
//									+ objExternalOrder.getNallottedspeccode() + "," + -1 + ",N'"
//									+ objExternalOrder.getSorderseqno() + "', N'" +spatientid + "',"
//									+ "N'" + ReplaceQuote(objExternalOrder.getSsubmittercode()) + "'," + "N'"
//									+ ReplaceQuote(objExternalOrder.getSexternalorderid()) + "', '"
//									+ ReplaceQuote(jsonObject.toString()) + "',"
//									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"
//									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
//									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
//									+ objExternalOrder.getNsitecode() + ", "
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
//									+ objExternalOrder.getNparentsitecode() + ")";
//
//							jdbcTemplate.execute(externalOrder);
//
//							String updateSeqQuery = "update seqnoregistration  set nsequenceno = " + seqorderdetail
//									+ " where stablename ='externalorder';";
//
//
//							
//							String dsampleCollectionDateTime = null;
//							int nsampleAppearanceCode = -1;
//							if (jsonObject.has("dcollectiondate")) {
//								dsampleCollectionDateTime = "'"+jsonObject.get("dcollectiondate")+"'";
//							}		
//							if(jsonObject.has("nsampleappearancecode")) {
//								nsampleAppearanceCode = (int) jsonObject.get("nsampleappearancecode");
//							}
//							seqordersample++;
//							externalOrderSampleQuery += " (" + seqordersample + "," + seqorderdetail + ","
//									+ -1 + "," + 0 + "," + -1 + ",'"
//									+ ReplaceQuote(objExternalOrder.getSexternalsampleid()) + "',"
//									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
//									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + objExternalOrder.getNsitecode()
//									+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
//									+ objExternalOrder.getNparentsitecode()+ ", "+ dsampleCollectionDateTime+", "
//									+ nsampleAppearanceCode+"),";
//
//							externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,ntransactionstatus,dmodifieddate,nsitecode,nstatus, nparentsitecode, dsamplecollectiondatetime, nsampleappearancecode) values "
//									+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1)
//									+ ";";
////
////							externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
////									+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";
//
//							updateSeqQuery = updateSeqQuery + "update seqnoregistration  set nsequenceno ="
//									+ seqordersample
//									+ " where stablename ='externalordersample';";
////									+ "update seqnoregistration  set nsequenceno = "
////									+ seqordertest + "" + " where stablename ='externalordertest';";
//
//							jdbcTemplate.execute(externalOrderSampleQuery);
//							jdbcTemplate.execute(updateSeqQuery);
//							
//							Map<String,Object> obj=new HashMap<String, Object>();
//							obj.put("nexternalordercode", seqorderdetail);
//							obj.put("ReturnStatus",Enumeration.ReturnStatus.SUCCESS.getreturnstatus() );
//
//							return new ResponseEntity<Object>(obj,
//									HttpStatus.OK);
//
//						}else {
//							return new ResponseEntity<>(
//									commonFunction.getMultilingualMessage("IDS_SITEISNOTAVAILABLE",
//											userInfo.getSlanguagefilename()),
//									HttpStatus.BAD_REQUEST);
//						}
//				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_GENDERISMISMATCHED",
//							userInfo.getSlanguagefilename()), HttpStatus.BAD_REQUEST);
//				}
//			} else {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEORDERALREADYRECEIVED",
//						userInfo.getSlanguagefilename()), HttpStatus.BAD_REQUEST);
//			}
//		}	
//		else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_ORDERALREADYRECEIVED", userInfo.getSlanguagefilename()),
//					HttpStatus.BAD_REQUEST);
//		}
//		
//
//	
//	}
	
	public ResponseEntity<Object> getSampleappearance(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select nsampleappearancecode,ssampleappearance,sdescription,ndefaultstatus,nstatus "
								+ " from sampleappearance where  nsampleappearancecode >0 and nsitecode= " +userInfo.getNmastersitecode();
		List<SampleAppearance> lstsamplepriority=jdbcTemplate.query(strQuery, new SampleAppearance());
		outputMap.put("Sampleappearance", lstsamplepriority);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	

	public ResponseEntity<Object> updateOrderSampleStatus(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception
	{
		 return externalOrderSupport.updateOrderSampleStatus(userInfo, inputMap);
		
	}
	
}
	
	
