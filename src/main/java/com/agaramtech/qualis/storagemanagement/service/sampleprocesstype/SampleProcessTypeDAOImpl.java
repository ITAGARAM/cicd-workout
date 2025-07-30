package com.agaramtech.qualis.storagemanagement.service.sampleprocesstype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.storagemanagement.model.SampleProcessType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "sampleprocesstype" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class SampleProcessTypeDAOImpl implements SampleProcessTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleProcessTypeDAOImpl.class);
	final ObjectMapper objmapper = new ObjectMapper();

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This Method is used to get the over all sampleprocesstype with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of sampleprocesstype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getSampleProcessType(final UserInfo userInfo) throws Exception {

		final String getSampleProcessType = "select " + " spt.ngracetime || ' ' ||(pd1.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "') AS ngracetimeresult ,  "
				+ " spt.nprocesstime || ' ' ||(pd1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "') AS nprocesstimeresult ,  " + " spt.nsampleprocesstypecode,spt.ngracetime ,   "
				+ " spt.nprocesstime,spt.sdescription,  "
				+ " spt.nexecutionorder,pjt.nprojecttypecode as nprojectcode,pjt.sprojecttypename,p.nproductcode,p.sproductname, "
				+ " ctt.ncollectiontubetypecode,ctt.stubename,pt.nprocesstypecode,pt.sprocesstypename,  sct.nsamplecollectiontypecode "
				+ " from sampleprocesstype spt ,projecttype pjt,product p, samplecollectiontype sct,collectiontubetype ctt,processtype pt,period pd1,period pd2 "
				+ " where spt.nprojecttypecode=pjt.nprojecttypecode "
				+ " and spt.nsamplecollectiontypecode =sct.nsamplecollectiontypecode "
				+ " and sct.nproductcode = p.nproductcode and spt.nprocessperiodtime= pd1.nperiodcode  "
				+ " and spt.ngraceperiodtime= pd2.nperiodcode  "
				+ " and spt.ncollectiontubetypecode=ctt.ncollectiontubetypecode "
				+ " and spt.nprocesstypecode=pt.nprocesstypecode  " + " "
				+ " and pjt.nsitecode= "+userInfo.getNmastersitecode()+"  "
				+ " and spt.nsitecode="+userInfo.getNmastersitecode()+""
				+ " and p.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and ctt.nsitecode= "+userInfo.getNmastersitecode()+""
				+ " and pt.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and spt.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and  spt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and  pjt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and  p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and  ctt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and  pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and  pd1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and  pd2.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
		LOGGER.info("getSampleProcessType() called");
		
		return new ResponseEntity<Object>(jdbcTemplate.query(getSampleProcessType, new SampleProcessType()),HttpStatus.OK);
	}

	/**
	 * This Method is used to get the over all Project Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of projecttype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProjecttype(final UserInfo userinfo) throws Exception {
		final String strQuery = "select pt.nprojecttypecode as nprojectcode, pt.sprojecttypename from  projecttype pt where pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nprojecttypecode > 0 and pt.nsitecode = " + userinfo.getNmastersitecode();
		final List<Map<String, Object>> projecttypeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(projecttypeList, HttpStatus.OK);

	}

	/**
	 * This Method is used to get the over all Sample Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Sample Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	
	@Override
	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {

		final String strQuery = "select p.sproductname,sct.nsamplecollectiontypecode as nproductsamplecode "
				+ "from samplecollectiontype sct,projecttype pt, product p  "
				+ " where sct.nprojecttypecode=pt.nprojecttypecode  and sct.nproductcode=p.nproductcode  "
				+ " and sct.nsitecode="+ userInfo.getNmastersitecode() + " "
				+ " and pt.nsitecode ="+userInfo.getNmastersitecode()+" "
				+ " and p.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and sct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and p.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and sct.nsitecode="+ userInfo.getNmastersitecode() + " "
				+ " and pt.nprojecttypecode=" + inputMap.get("sampletypevalue")+ "; ";
		
		final List<Map<String, Object>> sampletypeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(sampletypeList, HttpStatus.OK);

	}

	/**
	 * This Method is used to get the over all Collection Tube Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Collection Tube Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	
	@Override
	public ResponseEntity<Object> getCollectionTubeType(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select ncollectiontubetypecode as ncollectiontubecode,stubename from collectiontubetype"
				+ " where ncollectiontubetypecode>0 and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode = "+ userInfo.getNmastersitecode() + " "
				+ " and nprojecttypecode=" + inputMap.get("sampletypevalue")+";";
		
		final List<Map<String, Object>> collectiontubeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(collectiontubeList, HttpStatus.OK);

	}

	/**
	 * This Method is used to get the over all Process Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Process Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	
	@Override
	public ResponseEntity<Object> getProcessType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {

		final String strQuery = "select nprocesstypecode as nprocesscode,sprocesstypename from processtype where nprocesstypecode>0  "
				+ "and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsitecode = "+ userInfo.getNmastersitecode() + " ;";
		
		final List<Map<String, Object>> processtypeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(processtypeList, HttpStatus.OK);

	}

	/**
	 * This method is used to add a new entry to sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * 
	 * @return inserted sampleprocesstype object and HTTP Status on successive
	 *  insert otherwise corresponding HTTP Status
	 *  
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> createSampleProcessType(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		final List<SampleProcessType> objSampleProcessType = getSampleProcessTypeValidate(inputMap, userInfo);
		if (objSampleProcessType.isEmpty()) {
			final String sExectionOrderCheck = "select * from sampleprocesstype where nprojecttypecode="+ inputMap.get("projecttypevalue") + " "
					+ " and nsamplecollectiontypecode="+ inputMap.get("productvalue") + " "
					+ " and ncollectiontubetypecode=" + inputMap.get("tubevalue")
					+ " and nexecutionorder=" + inputMap.get("executionorder") + " "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode="+ userInfo.getNmastersitecode() + "    ; ";
			
			final List<SampleProcessType> exectionOrderValidate = jdbcTemplate.query(sExectionOrderCheck,new SampleProcessType());
			
			if (exectionOrderValidate.isEmpty()) {
				
				final String sQuery = " lock  table sampleprocesstype "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);
				
				final String sGetSeqNoQuery = "select nsequenceno from seqnostoragemanagement where stablename = 'sampleprocesstype' "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
				nSeqNo++;
				
				final String sdescription = inputMap.get("sdescription") == null ? "": (String) inputMap.get("sdescription");
				
				String insertQuery = "INSERT INTO public.sampleprocesstype(  "
						+ "nsampleprocesstypecode, nprojecttypecode, nsamplecollectiontypecode, ncollectiontubetypecode, "
						+ "nprocesstypecode, nprocesstime,nprocessperiodtime, ngracetime,ngraceperiodtime, nexecutionorder, sdescription, "
						+ "dmodifieddate, nsitecode, nstatus)  " + "VALUES (" + nSeqNo + ","
						+ inputMap.get("projecttypevalue") + "," + inputMap.get("productvalue") + ","
						+ inputMap.get("tubevalue") + ",  " + "" + inputMap.get("processtypevalue") + ","
						+ inputMap.get("processtime") + "," + inputMap.get("processperiodtime") + " , "
						+ inputMap.get("gracetime") + "," + inputMap.get("graceperiodtime") + ","
						+ inputMap.get("executionorder") + "," + "'" + stringUtilityFunction.replaceQuote(sdescription)
						+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNmastersitecode() + "," + ""
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				
				insertQuery = insertQuery
						+ "update seqnostoragemanagement set nsequenceno =(select max(nsampleprocesstypecode) from sampleprocesstype) "
						+ "where stablename='sampleprocesstype' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(insertQuery);
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				final SampleProcessType auditSampleProcessType = new SampleProcessType();
				final String pedirodTime = (String) inputMap.get("commonPeriod");
				final int processtimeCast = (int) inputMap.get("processtime");
				final int gracenameCast = (int) inputMap.get("gracetime");
				final int executionorderCast = (int) inputMap.get("executionorder");
				auditSampleProcessType.setSprojecttypename((String) inputMap.get("projecttypename"));
				auditSampleProcessType.setSproductname((String) inputMap.get("productname"));
				auditSampleProcessType.setStubename((String) inputMap.get("tubename"));
				auditSampleProcessType.setSprocesstypename((String) inputMap.get("processtypename"));
				auditSampleProcessType.setNprocesstimeresult(processtimeCast + " " + pedirodTime);
				auditSampleProcessType.setNgracetimeresult(gracenameCast + " " + pedirodTime);
				auditSampleProcessType.setNexecutionorder(executionorderCast);
				auditSampleProcessType.setSdescription(sdescription);
				savedTestList.add(auditSampleProcessType);
				multilingualIDList.add("IDS_ADDSAMPLEPROCESSMAPPING");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
						userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
					userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);
		}
		return getSampleProcessType(userInfo);
	}

	/**
	 * This method is used to check whether a sample process mapping exists or not respective site.
	 * 
	 * @param inputMap [Map] hold the following keys
	 * The projecttypevalue key holds the value of projecttype.
	 * The productvalue key holds the value of product.
	 * The tubevalue key holds the value of collection tube type.
	 * The processtypevalue key holds the value of process type.
	 *  "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code.
	 *  
	 * @return list of active sampleprocesstype code(s) based on the specified
	 *         processtype name and site
	 *         
	 * @throws Exception
	 */

	private List<SampleProcessType> getSampleProcessTypeValidate(final Map<String, Object> inputMap,final UserInfo userInfo) {

		String validateCheck;
		if (inputMap.get("operation").equals("update")) {
			validateCheck = "select * from sampleprocesstype "
					+ " where nprojecttypecode="+ inputMap.get("projecttypevalue") + " "
					+ " and nsamplecollectiontypecode="+ inputMap.get("productvalue") + " "
					+ " and ncollectiontubetypecode=" + inputMap.get("tubevalue")+" "
					+ " and nprocesstypecode=" + inputMap.get("processtypevalue") + " "
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode="+ userInfo.getNmastersitecode() + " "
					+ " and nsampleprocesstypecode <>"+ inputMap.get("nsampleprocesstypecode") + ";";
		} else {
			validateCheck = "select * from sampleprocesstype "
					+ " where nprojecttypecode="+ inputMap.get("projecttypevalue") + " "
					+ " and  nsamplecollectiontypecode="+ inputMap.get("productvalue") + " "
					+ " and  ncollectiontubetypecode=" + inputMap.get("tubevalue")+" "
					+ " and  nprocesstypecode=" + inputMap.get("processtypevalue") + " "
					+ " and  nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode="+ userInfo.getNmastersitecode() + "; ";
		}

		return (List<SampleProcessType>) jdbcTemplate.query(validateCheck, new SampleProcessType());
	}

	/**
	 * This method id used to delete an entry in sampleprocesstype table
	 * 
	 * @param inputMap [Map] holds the following keys
	 * "userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * The sampleProcessType holds the objects used to delete the sample processing mapping-related data. 
	 * @return response entity object holding response status and data of deleted
	 *         sampleprocesstype object
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> deleteSampleProcessType(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {

		final SampleProcessType objSampleProcessType = objmapper.convertValue(inputMap.get("sampleprocesstype"),SampleProcessType.class);
		
		final String deleteDupicate = "SELECT * FROM public.sampleprocesstype "
				+ " where nsampleprocesstypecode="+ objSampleProcessType.getNsampleprocesstypecode() + " "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsitecode="+ userInfo.getNmastersitecode() + " ;";
		
		List<SampleProcessType> duplicateRecord = jdbcTemplate.query(deleteDupicate, new SampleProcessType());
		
		if (duplicateRecord.isEmpty()) {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);
		} else {
			final String recordInProcessType = "select 'IDS_STORAGESAMPLEPROCESSING' as Msg from storagesampleprocessing where "
					+ "nsampleprocesstypecode=" + objSampleProcessType.getNsampleprocesstypecode() + "  "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			valiDatorDel = projectDAOSupport.getTransactionInfo(recordInProcessType, userInfo);
			
			boolean validRecord = false;
			
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objSampleProcessType.getNsampleprocesstypecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String deleteQuery = "UPDATE public.sampleprocesstype " + "SET  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus="+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "  "
						+ " WHERE nsampleprocesstypecode=" + objSampleProcessType.getNsampleprocesstypecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.execute(deleteQuery);
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				savedTestList.add(objSampleProcessType);
				multilingualIDList.add("IDS_DELETESAMPLEPROCESSMAPPING");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getSampleProcessType(userInfo);
	}

	/**
	 * This method is used to retrieve active sampleprocesstype object based on the
	 * specified nprocesstypeCode.
	 * 
	 *@param inputMap [Map] holds the following keys
	 *"userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * nsampleprocesstypecode holds the value used to identify which record to retrieve.
	 *
	 * @return response entity object holding response status and data of
	 *         sampleprocesstype object
	 *         
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public SampleProcessType getActiveSampleProcessTypeById(final Map<String, Object> inputMap,final UserInfo userInfo)	throws Exception {

		final String getSampleProcessType = "select pd1.nperiodcode," + " pd1.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "' as nprocesstimeresult," + " pd2.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "' as ngracetimeresult,"
				+ " spt.nsampleprocesstypecode,spt.ngracetime, spt.nprocesstime,spt.sdescription,spt.nexecutionorder,"
				+ " pjt.nprojecttypecode as nprojectcode,pjt.sprojecttypename,  "
				+ " sct.nsamplecollectiontypecode as nproductsamplecode,p.sproductname,  "
				+ " ctt.ncollectiontubetypecode as ncollectiontubecode,ctt.stubename,  "
				+ " pt.nprocesstypecode nprocesscode,pt.sprocesstypename "
				+ " from sampleprocesstype spt ,projecttype pjt,product p,"
				+ " collectiontubetype ctt,processtype pt, period pd1,period pd2,samplecollectiontype sct "
				+ " where spt.nprojecttypecode=pjt.nprojecttypecode "
				+ " and spt.nsamplecollectiontypecode =sct.nsamplecollectiontypecode "
				+ " and sct.nproductcode = p.nproductcode " + " and spt.nprocessperiodtime= pd1.nperiodcode "
				+ " and spt.ngraceperiodtime= pd2.nperiodcode "
				+ " and spt.ncollectiontubetypecode=ctt.ncollectiontubetypecode "
				+ " and spt.nprocesstypecode=pt.nprocesstypecode "
				+ " and spt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and pjt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and p.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and ctt.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and pt.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and sct.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and spt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and pjt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ctt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and pd1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and pd2.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and spt.nsampleprocesstypecode=" + inputMap.get("nsampleprocesstypecode") + ";";
		
		return (SampleProcessType) jdbcUtilityFunction.queryForObject(getSampleProcessType, SampleProcessType.class,jdbcTemplate);
	}

	/**
	 * This method is used to update entry in sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * 
	 * @return response entity object holding response status and data of updated
	 *         sampleprocesstype object
	 *         
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> updateSampleProcessType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {

		final List<SampleProcessType> objSampleProcessTypeValidate = getSampleProcessTypeValidate(inputMap, userInfo);
		if (objSampleProcessTypeValidate.isEmpty()) {
			final String sExectionOrderCheck = "select * from sampleprocesstype "
					+ " where nprojecttypecode="+ inputMap.get("projecttypevalue") + " "
					+ " and nsamplecollectiontypecode="+ inputMap.get("productvalue") + ""
					+ " and ncollectiontubetypecode=" + inputMap.get("tubevalue")
					+ " and nexecutionorder=" + inputMap.get("executionorder") + ""
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode="+ userInfo.getNmastersitecode() + " "
					+ " and nsampleprocesstypecode <>"+ inputMap.get("nsampleprocesstypecode") + "; ";
			
			final List<SampleProcessType> exectionOrderValidate = jdbcTemplate.query(sExectionOrderCheck,new SampleProcessType());
			
			if (exectionOrderValidate.isEmpty()) {
				
				final SampleProcessType auditSampleProcessTypeOld = getAuditSampleProcessType(inputMap, userInfo);
				
				if (auditSampleProcessTypeOld == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				} else {
					final String sdescription = inputMap.get("sdescription") == null ? " "
							: (String) inputMap.get("sdescription");
					final String updateQuery = "UPDATE public.sampleprocesstype SET  nprojecttypecode="
							+ inputMap.get("projecttypevalue") + ", " + "nsamplecollectiontypecode="
							+ inputMap.get("productvalue") + ", ncollectiontubetypecode=" + inputMap.get("tubevalue")
							+ ", " + "nprocesstypecode=" + inputMap.get("processtypevalue") + ", nprocesstime="
							+ inputMap.get("processtime") + ", " + "ngracetime=" + inputMap.get("gracetime")
							+ ", nexecutionorder=" + inputMap.get("executionorder") + ", " + "sdescription='"
							+ stringUtilityFunction.replaceQuote(inputMap.get("sdescription").toString())
							+ "' , dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
							+ "WHERE nsampleprocesstypecode=" + inputMap.get("nsampleprocesstypecode") + " "
							+ " and nsitecode  = "+userInfo.getNmastersitecode()+" "
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					
					jdbcTemplate.execute(updateQuery);
					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> listAfterUpdate = new ArrayList<>();
					final List<Object> listBeforeUpdate = new ArrayList<>();
					final SampleProcessType auditSampleProcessType = new SampleProcessType();
					final String pedirodTime = (String) inputMap.get("commonPeriod");
					final int processtimeCast = (int) inputMap.get("processtime");
					final int gracenameCast = (int) inputMap.get("gracetime");
					final int executionorderCast = (int) inputMap.get("executionorder");
					auditSampleProcessType.setSprojecttypename((String) inputMap.get("projecttypename"));
					auditSampleProcessType.setSproductname((String) inputMap.get("productname"));
					auditSampleProcessType.setStubename((String) inputMap.get("tubename"));
					auditSampleProcessType.setSprocesstypename((String) inputMap.get("processtypename"));
					auditSampleProcessType.setNprocesstimeresult(processtimeCast + " " + pedirodTime);
					auditSampleProcessType.setNgracetimeresult(gracenameCast + " " + pedirodTime);
					auditSampleProcessType.setNexecutionorder(executionorderCast);
					auditSampleProcessType.setSdescription(sdescription);
					listAfterUpdate.add(auditSampleProcessTypeOld);
					listBeforeUpdate.add(auditSampleProcessType);
					multilingualIDList.add("IDS_EDITSAMPLEPROCESSMAPPING");
					
					auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 2, listAfterUpdate, multilingualIDList,userInfo);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		return getSampleProcessType(userInfo);
	}

	/**
	 * This method is used to retrieve active sampleprocesstype object based on the
	 * specified nsampleprocesstypecode and to use for Audit purpose.
	 * 
	 *@param inputMap [Map] holds the following keys
	 *"userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * nsampleprocesstypecode holds the value used to identify which record to retrieve.
	 *
	 * @return response entity object holding response status and data of
	 *         sampleprocesstype object
	 *         
	 * @throws Exception that are thrown from this DAO layer
	 */
	public SampleProcessType getAuditSampleProcessType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {

		final String getSampleProcessType = "select spt.nsampleprocesstypecode,spt.nsampleprocesstypecode,  "
				+ "spt.ngracetime || ' ' ||(pe.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "') AS ngracetimeresult ,  " + "spt.nprocesstime || ' ' ||(pe.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "') AS nprocesstimeresult ,  " + "spt.ngracetime,  "
				+ " spt.nprocesstime,  " + "spt.sdescription,  " + "spt.nexecutionorder,  " + "pjt.sprojecttypename,  "
				+ " p.sproductname,  " + "ctt.stubename,  " + "pt.sprocesstypename  "
				+ " from sampleprocesstype spt ,projecttype pjt,product p,collectiontubetype ctt, samplecollectiontype sct,processtype pt, period pe "
				+ " where spt.nprocessperiodtime=pe.nperiodcode " + "and spt.nprojecttypecode=pjt.nprojecttypecode "
				+ " and spt.nsamplecollectiontypecode =sct.nsamplecollectiontypecode "
				+ " and sct.nproductcode = p.nproductcode "
				+ " and spt.ncollectiontubetypecode=ctt.ncollectiontubetypecode "
				+ " and spt.nprocesstypecode=pt.nprocesstypecode "
				+ " and pjt.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and p.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and ctt.nsitecode = "+userInfo.getNmastersitecode()+" "
				+ " and pt.nsitecode= "+userInfo.getNmastersitecode()+" "
				+ " and sct.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and spt.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and spt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and pjt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and ctt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and spt.nsampleprocesstypecode =" + inputMap.get("nsampleprocesstypecode") + "  ;";
		
		return (SampleProcessType) jdbcUtilityFunction.queryForObject(getSampleProcessType, SampleProcessType.class,jdbcTemplate);
	}
}
