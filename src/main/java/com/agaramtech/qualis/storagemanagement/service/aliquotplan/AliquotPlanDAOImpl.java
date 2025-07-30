package com.agaramtech.qualis.storagemanagement.service.aliquotplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//import java.util.Properties;
//import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
//import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
//import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.storagemanagement.model.AliquotPlan;
import com.agaramtech.qualis.barcode.model.SampleDonor;
//import com.agaramtech.qualis.basemaster.model.Unit;
//import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 *  This class is used to perform CRUD Operation on "aliquotplan" table by implementing methods from its interface.
 * @author ATE234
 * @jira ALPD-4256 bAlliquot Plan -> Screen Development -Add, Edit, Delete.
 * @version 9.0.0.1
 * @since 28 - June -2024
 */

@AllArgsConstructor
@Repository
public class AliquotPlanDAOImpl implements AliquotPlanDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(AliquotPlanDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	//private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	final ObjectMapper objmapper = new ObjectMapper();



	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getAliquotPlan(UserInfo userInfo) {

		// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
		final String getAliquotPlan="select pc.npatientcatcode,pc.spatientcatname,vn.nvisitnumbercode,ap.nsamplecollectiontypecode, "
				+ "vn.svisitnumber,u.nunitcode,u.sunitname,ap.naliquotplancode,ap.squantity ,ap.saliquotno,  "
				+ "ap.sdescription,pjt.nprojecttypecode as nprojectcode,pjt.sprojecttypename,p.nproductcode,  "
				+ "p.sproductname,ctt.ncollectiontubetypecode,ctt.stubename,sd.nsampledonorcode,sd.ssampledonor  from  "
				+ "aliquotplan ap,projecttype pjt,product p,samplecollectiontype sct,collectiontubetype ctt,patientcategory pc, "
				+ "visitnumber vn,unit u,sampledonor sd where  "
				+ "ap.nprojecttypecode = pjt.nprojecttypecode  "
				+ "and ap.nsamplecollectiontypecode =sct.nsamplecollectiontypecode "
				+ "and sct.nproductcode = p.nproductcode "
				+ "and ap.ncollectiontubetypecode = ctt.ncollectiontubetypecode  "
				+ "and ap.npatientcatcode=pc.npatientcatcode "
				+ "and ap.nvisitnumbercode=vn.nvisitnumbercode "
				+ "and ap.nunitcode=u.nunitcode "
				+ "and ap.nsampledonorcode=sd.nsampledonorcode "
				+ "and ap.nsitecode = pjt.nsitecode  "
				+ "and ap.nsitecode = p.nsitecode  "
				+ "and ap.nsitecode = ctt.nsitecode "
				+ "and ap.nsitecode=pc.nsitecode "
				+ "and ap.nsitecode=vn.nsitecode "
				+ "and ap.nsitecode="+userInfo.getNmastersitecode()+" and pjt.nsitecode = "+userInfo.getNmastersitecode()
				+ " and p.nsitecode = "+userInfo.getNmastersitecode()+" and sct.nsitecode = "+userInfo.getNmastersitecode()
				+ " and ctt.nsitecode = "+userInfo.getNmastersitecode()+" and pc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and vn.nsitecode = "+userInfo.getNmastersitecode()+" and u.nsitecode = "+userInfo.getNmastersitecode()
				+ " and sd.nsitecode = "+userInfo.getNmastersitecode() + " "
				+ "and  pjt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  ctt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  pc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  vn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  sd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  sct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		LOGGER.info("Get Method:"+ getAliquotPlan);
		return new ResponseEntity<Object>(jdbcTemplate.query(getAliquotPlan, new AliquotPlan()), HttpStatus.OK);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProjecttype(UserInfo userinfo)throws Exception{
		final String strQuery = "select pt.nprojecttypecode as nprojectcode, pt.sprojecttypename from  projecttype pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and pt.nprojecttypecode > 0 and pt.nsitecode = " + userinfo.getNmastersitecode();

		final List<Map<String,Object>> projecttypeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(projecttypeList, HttpStatus.OK);

	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap,UserInfo userInfo)throws Exception{

		final String strQuery = "select p.sproductname,sct.nsamplecollectiontypecode as nproductsamplecode from samplecollectiontype sct,projecttype pt,product p  "
				+ "where sct.nprojecttypecode=pt.nprojecttypecode  "
				+ "and sct.nproductcode=p.nproductcode  "
				+ "and sct.nsitecode=pt.nsitecode  "
				+ "and sct.nsitecode=p.nsitecode  "
				+ "and sct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and sct.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and pt.nsitecode = "+userInfo.getNmastersitecode()+" and p.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and pt.nprojecttypecode="+inputMap.get("sampletypevalue")+"; ";

		final List<Map<String,Object>> sampletypeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(sampletypeList, HttpStatus.OK);

	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String strQuery = "select ncollectiontubetypecode as ncollectiontubecode,stubename from collectiontubetype"
				+ " where ncollectiontubetypecode>0 and nprojecttypecode="+inputMap.get("sampletypevalue")+" and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " ;";

		final List<Map<String,Object>> collectiontubeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(collectiontubeList, HttpStatus.OK);

	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getPatientCatgory(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String strQuery = "select  pc.npatientcatcode as npatientcode,pc.spatientcatname from patientcategory pc,projecttype pt where "
				+ "pc.nprojecttypecode=pt.nprojecttypecode and pc.npatientcatcode >0 and pc.nprojecttypecode="+inputMap.get("sampletypevalue")+" and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and pc.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nsitecode = "
				+ userInfo.getNmastersitecode() + " and pc.nsitecode ="+userInfo.getNmastersitecode()+" ;";

		final List<Map<String,Object>> patientcatgory = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(patientcatgory, HttpStatus.OK);

	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getVisitName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String strQuery = "select nvisitnumbercode as nvisitcode,svisitnumber from visitnumber where nvisitnumbercode >0 and nprojecttypecode="+inputMap.get("sampletypevalue")+"  "
				+ "and  nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="+userInfo.getNmastersitecode() + " ;";

		final List<Map<String,Object>> visitname = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(visitname, HttpStatus.OK);
	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getUnit(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String strQuery = "select nunitcode as nunitbasiccode,sunitname from unit where nunitcode >0 and  "
				+ " nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="+userInfo.getNmastersitecode() + " ;";

		final List<Map<String,Object>> unitlist = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(unitlist, HttpStatus.OK);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	@Override
	public ResponseEntity<Object> getSampleDonor(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String strQuery = "select sd.nsampledonorcode, sd.ssampledonor from sampledonor sd,projecttype pt where "
				+ "sd.nprojecttypecode=pt.nprojecttypecode and sd.nsampledonorcode >0 and sd.nprojecttypecode="
				+ inputMap.get("sampletypevalue")+" and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sd.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and sd.nsitecode ="+userInfo.getNmastersitecode()+" ;";

		final List<Map<String,Object>> sampleDonor = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(sampleDonor, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * Need to check for duplicate entry of aliquotplan name for the specified site before saving into database.
	 * Need to check that there should be only one default aliquotplan for a site
	 * @param objUnit [aliquotplan] object holding details to be added in aliquotplan table
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 * @jira ALPD-4595 Aliquot Plan -> 0 allowed in the Aliquot field
	 */

	@Override
	public ResponseEntity<Object> createAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final List<AliquotPlan> objAliqutoPlan =getAliquotPlanValidate(inputMap, userInfo);

		if(objAliqutoPlan.isEmpty()) {

			final String sQuery = " lock  table aliquotplan " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final String sGetSeqNoQuery = "select nsequenceno from seqnostoragemanagement where stablename = 'aliquotplan';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			nSeqNo++;

			// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
			String insertQuery="INSERT INTO public.aliquotplan(  "
					+ "naliquotplancode, nprojecttypecode, nsamplecollectiontypecode, ncollectiontubetypecode, npatientcatcode, nvisitnumbercode, saliquotno, squantity, nunitcode, sdescription, dmodifieddate, nsitecode, nstatus, nsampledonorcode)  "
					+ "VALUES ("+nSeqNo+", "+inputMap.get("projecttypevalue")+", "+inputMap.get("productvalue")+", "+inputMap.get("tubevalue")+","+inputMap.get("patientcatvalue")+", "+inputMap.get("visitnumber")+", '"+inputMap.get("saliquotno")+"', '"+inputMap.get("squantity")+"', "+inputMap.get("unitvalue")+", '"+stringUtilityFunction.replaceQuote(inputMap.get("sdescription").toString())+"', '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+","+inputMap.get("nsampledonorcode")+");";


			insertQuery =insertQuery +"update seqnostoragemanagement set nsequenceno =(select max(naliquotplancode) from aliquotplan) where stablename='aliquotplan';";
			jdbcTemplate.execute(insertQuery);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedTestList = new ArrayList<>();

			final AliquotPlan auditAliquotPlan=new AliquotPlan();

			auditAliquotPlan.setSprojecttypename((String) inputMap.get("projecttypename"));
			auditAliquotPlan.setSproductname((String) inputMap.get("productname"));
			auditAliquotPlan.setStubename((String) inputMap.get("tubename"));
			auditAliquotPlan.setSpatientcatname((String) inputMap.get("patientcatname"));
			auditAliquotPlan.setSvisitnumber((String) inputMap.get("visitname"));
			auditAliquotPlan.setSaliquotno((String) inputMap.get("saliquotno"));
			auditAliquotPlan.setSquantity((String) inputMap.get("squantity"));
			auditAliquotPlan.setSunitname((String) inputMap.get("unitname"));
			auditAliquotPlan.setSdescription((String) inputMap.get("sdescription"));
			auditAliquotPlan.setSsampledonor((String) inputMap.get("ssampledonor"));

			savedTestList.add(auditAliquotPlan);
			multilingualIDList.add("IDS_ADDALIQUOTPLAN");

			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage
					(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		return getAliquotPlan(userInfo);
	}

	/**
	 * This method is used to fetch the active aliquotplan objects for the specified aliquotplan name and site.
	 * @param saliquotplanname [String] name of the aliquotplan
	 * @param nmasterSiteCode [int] site code of the aliquotplan
	 * @return list of active aliquotplan code(s) based on the specified aliquotplan name and site
	 * @throws Exception
	 */

	private List<AliquotPlan> getAliquotPlanValidate(Map<String, Object> inputMap, UserInfo userInfo) {
		// TODO Auto-generated method stub
		//janakumar ALPD-5055 Aliquot Plan -> Combination of record alert.

		String validateCheck;

		if(inputMap.get("operation").equals("update")) {

			validateCheck="select * from aliquotplan "
					+ "where nprojecttypecode="+inputMap.get("projecttypevalue")+" and  nsamplecollectiontypecode="+inputMap.get("productvalue")+" "
					+ "and npatientcatcode="+inputMap.get("patientcatvalue")+"  and  ncollectiontubetypecode="+inputMap.get("tubevalue")+" and  nvisitnumbercode="+inputMap.get("visitnumber")+" "
					+ "and saliquotno = '"+inputMap.get("saliquotno")+"'  and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					// ALPD-5572,5573 - AliquotPlan allows duplicate record with diffrent sampleDonor's and impact in sample storage - shows wrong details.
					// +" and nsampledonorcode="+inputMap.get("nsampledonorcode")
					+" and naliquotplancode <>"+inputMap.get("naliquotplancode")+"  ";

		}else {

			validateCheck="select * from aliquotplan "
					+ "where nprojecttypecode="+inputMap.get("projecttypevalue")+" and  nsamplecollectiontypecode="+inputMap.get("productvalue")+" "
					+ "and npatientcatcode="+inputMap.get("patientcatvalue")+"  and  ncollectiontubetypecode="+inputMap.get("tubevalue")+" and  nvisitnumbercode="+inputMap.get("visitnumber")+" "
					+ "and saliquotno = '"+inputMap.get("saliquotno")+"'  and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"; ";
			// ALPD-5572,5573 - AliquotPlan allows duplicate record with diffrent sampleDonor's and impact in sample storage - shows wrong details.
			// +" and nsampledonorcode="+inputMap.get("nsampledonorcode")+"; ";
		}

		return jdbcTemplate.query(validateCheck, new AliquotPlan());
	}

	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> updateAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final List<AliquotPlan> objAliqutoPlan =getAliquotPlanValidate(inputMap, userInfo);

		if(objAliqutoPlan.isEmpty()) {


			final AliquotPlan getOldRecord =getActiveAliquotPlanById((int) inputMap.get("naliquotplancode"), userInfo);

			if(getOldRecord == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage
						(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			} else {

				// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
				final String updateQuery="UPDATE public.aliquotplan  "
						+ "SET  nprojecttypecode="+inputMap.get("projecttypevalue")+", nsamplecollectiontypecode="+inputMap.get("productvalue")+", ncollectiontubetypecode="+inputMap.get("tubevalue")+", npatientcatcode="+inputMap.get("patientcatvalue")+", nvisitnumbercode="+inputMap.get("visitnumber")+", saliquotno='"+inputMap.get("saliquotno")+"', squantity='"+inputMap.get("squantity")+"', nunitcode="+inputMap.get("unitvalue")+", nsampledonorcode="+inputMap.get("nsampledonorcode")+", sdescription='"+stringUtilityFunction.replaceQuote((String)inputMap.get("sdescription"))+"', dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'  "
						+ "WHERE naliquotplancode="+inputMap.get("naliquotplancode")+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				jdbcTemplate.execute(updateQuery);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				final AliquotPlan auditAliquotPlan=new AliquotPlan();


				auditAliquotPlan.setSprojecttypename((String) inputMap.get("projecttypename"));
				auditAliquotPlan.setSproductname((String) inputMap.get("productname"));
				auditAliquotPlan.setStubename((String) inputMap.get("tubename"));
				auditAliquotPlan.setSpatientcatname((String) inputMap.get("patientcatname"));
				auditAliquotPlan.setSvisitnumber((String) inputMap.get("visitname"));
				auditAliquotPlan.setSaliquotno((String) inputMap.get("saliquotno"));
				auditAliquotPlan.setSquantity((String) inputMap.get("squantity"));
				auditAliquotPlan.setSunitname((String) inputMap.get("unitname"));
				auditAliquotPlan.setSdescription((String) inputMap.get("sdescription"));
				auditAliquotPlan.setSsampledonor((String) inputMap.get("ssampledonor"));

				listAfterUpdate.add(getOldRecord);
				listBeforeUpdate.add(auditAliquotPlan);

				multilingualIDList.add("IDS_EDITALIQUOTPLAN");
				auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 2, listAfterUpdate, multilingualIDList, userInfo);
			}

		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage
					(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		return getAliquotPlan(userInfo);
	}

	/**
	 * This method is used to retrieve active aliquotplan object based on the specified naliquotplanCode.
	 * @param naliquotplanCode [int] primary key of aliquotplan object
	 * Input : {naliquotplancode:1,"userinfo":{nmastersitecode": -1}}
	 * @return response entity  object holding response status and data of aliquotplan object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public AliquotPlan getActiveAliquotPlanById(int naliquotplancode, UserInfo userInfo) throws Exception {

		// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
		final String getAliquotPlan="select pc.npatientcatcode as npatientcode,pc.spatientcatname,vn.nvisitnumbercode as nvisitcode,ap.nsamplecollectiontypecode as nproductsamplecode, "
				+ "vn.svisitnumber,u.nunitcode as nunitbasiccode,u.sunitname,ap.naliquotplancode,ap.squantity,ap.saliquotno,  "
				+ "ap.sdescription,pjt.nprojecttypecode as nprojectcode,pjt.sprojecttypename,p.nproductcode as nproductsamplecode,  "
				+ "p.sproductname,ctt.ncollectiontubetypecode  as ncollectiontubecode,ctt.stubename,sd.nsampledonorcode,sd.ssampledonor  from  "
				+ "aliquotplan ap,projecttype pjt,product p,samplecollectiontype sct,collectiontubetype ctt,patientcategory pc, "
				+ "visitnumber vn,unit u,sampledonor sd where  "
				+ "ap.nprojecttypecode = pjt.nprojecttypecode  "
				+ "and ap.nsamplecollectiontypecode =sct.nsamplecollectiontypecode "
				+ "and sct.nproductcode = p.nproductcode "
				+ "and ap.ncollectiontubetypecode = ctt.ncollectiontubetypecode  "
				+ "and ap.npatientcatcode=pc.npatientcatcode "
				+ "and ap.nvisitnumbercode=vn.nvisitnumbercode "
				+ "and ap.nunitcode=u.nunitcode "
				+ "and ap.nsampledonorcode=sd.nsampledonorcode "
				+ "and ap.nsitecode = pjt.nsitecode  "
				+ "and ap.nsitecode = p.nsitecode  "
				+ "and ap.nsitecode = ctt.nsitecode "
				+ "and ap.nsitecode=pc.nsitecode "
				+ "and ap.nsitecode=vn.nsitecode "
				+ "and ap.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "and  pjt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  ctt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  pc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  vn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ "and  sd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  sct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and ap.naliquotplancode="+naliquotplancode+" ;";


		return (AliquotPlan) jdbcUtilityFunction.queryForObject(getAliquotPlan, AliquotPlan.class, jdbcTemplate);
	}


	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> deleteAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub


		final AliquotPlan objAliquotPlan=objmapper.convertValue(inputMap.get("aliquotplan"), AliquotPlan.class);

		final AliquotPlan getActiveRecord =getActiveAliquotPlanById(objAliquotPlan.getNaliquotplancode(), userInfo);

		if(getActiveRecord == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}else {

			final String deleteQuery="UPDATE public.aliquotplan SET  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
					+ "WHERE naliquotplancode="+objAliquotPlan.getNaliquotplancode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			jdbcTemplate.execute(deleteQuery);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedTestList = new ArrayList<>();

			savedTestList.add(objAliquotPlan);
			multilingualIDList.add("IDS_DELETEALIQUOTPLAN");

			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
		}

		return getAliquotPlan(userInfo);
	}

	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	/**
	 * This method is used to retrieve active sampledonor object based on the specified nsampledonorcode.
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @return response entity  object holding response status and data of sampledonor object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public SampleDonor isSampleDonorActive(final int nsampledonorcode, UserInfo userInfo) throws Exception {
		final String validationQuery = "select * from sampledonor where nsampledonorcode="+nsampledonorcode
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (SampleDonor) jdbcUtilityFunction.queryForObject(validationQuery, SampleDonor.class, jdbcTemplate);
	}


}
