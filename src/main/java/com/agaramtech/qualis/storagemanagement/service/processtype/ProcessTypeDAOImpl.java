package com.agaramtech.qualis.storagemanagement.service.processtype;

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
import com.agaramtech.qualis.storagemanagement.model.ProcessType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "processtype" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class ProcessTypeDAOImpl implements ProcessTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTypeDAOImpl.class);
	
	//final ObjectMapper objmapper = new ObjectMapper();
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active processtype for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         processtypes
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getProcessType(UserInfo userInfo) throws Exception {
		final String strQuery = "select nprocesstypecode, sprocesstypename, sdescription,"
								+ " nsitecode, nstatus from processtype WHERE nprocesstypecode>0 and  nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode="
								+ userInfo.getNmastersitecode() + "";
		LOGGER.info("getProcessType() called");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProcessType()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to processtype table. Need to check
	 * for duplicate entry of processtype name for the specified site before saving
	 * into database. Need to check that there should be only one default
	 * processtype for a site
	 * 
	 * @param objprocesstype [processtype] object holding details to be added in
	 *                       processtype table
	 * @return inserted processtype object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	
		final ObjectMapper objMapper = new ObjectMapper();	
		final ProcessType objProcessType = objMapper.convertValue(inputMap.get("processtype"), ProcessType.class);
		
		final String checkDupilcate = "SELECT nprocesstypecode, sprocesstypename, sdescription "
									+ "FROM public.processtype WHERE sprocesstypename=LTRIM(RTRIM('"
									+ stringUtilityFunction.replaceQuote(objProcessType.getSprocesstypename().toString()) + "')) "
									+ "AND nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ userInfo.getNmastersitecode() + "  ;";
		final List<ProcessType> objcheckDupilcate = jdbcTemplate.query(checkDupilcate, new ProcessType());
		
		if (objcheckDupilcate.isEmpty()) {
			
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedTestList = new ArrayList<>();		
			
			final String sQuery = " lock  table processtype " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			
			final String sequencenoquery = "select nsequenceno from seqnostoragemanagement where stablename ='processtype'"
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			
			String insertQuery = "INSERT INTO public.processtype( "
									+ "nprocesstypecode, sprocesstypename, sdescription, dmodifieddate, nsitecode, nstatus)"
									+ "VALUES (" + nsequenceno + ", '"
									+ stringUtilityFunction.replaceQuote(objProcessType.getSprocesstypename().toString()) + "','"
									+ stringUtilityFunction.replaceQuote(objProcessType.getSdescription().toString()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + "," + ""
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			insertQuery = insertQuery
					+ "update seqnostoragemanagement set nsequenceno =(select max(nprocesstypecode) from processtype) where stablename='processtype';";
			jdbcTemplate.execute(insertQuery);

			savedTestList.add(objProcessType);
			multilingualIDList.add("IDS_ADDPROCESSTYPE");
			
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		return getProcessType(userInfo);
	}

	/**
	 * This method is used to retrieve active processtype object based on the
	 * specified nprocesstypeCode.
	 * 
	 * @param nprocesstypeCode [int] primary key of processtype object
	 * @return response entity object holding response status and data of
	 *         processtype object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ProcessType getActiveProcessTypeById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT nprocesstypecode, sprocesstypename, sdescription  "
								+ "FROM processtype WHERE nprocesstypecode=" + inputMap.get("nprocesstypecode") + " AND nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNmastersitecode() + "  ;";
		return (ProcessType) jdbcUtilityFunction.queryForObject(strQuery, ProcessType.class, jdbcTemplate);

	}

	/**
	 * This method is used to update entry in processtype table. Need to validate
	 * that the processtype object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of processtype name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default processtype for a site
	 * 
	 * @param objprocesstype [processtype] object holding details to be updated in
	 *                       processtype table
	 * @return response entity object holding response status and data of updated
	 *         processtype object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final ProcessType objProcessType = objMapper.convertValue(inputMap.get("processtype"), ProcessType.class);
		final List<ProcessType> objProcessTypeOld = getProcessTypeValidate(objProcessType.getNprocesstypecode(),
				userInfo);
		if (objProcessTypeOld.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String checkDupilcate = "SELECT nprocesstypecode, sprocesstypename, sdescription "
										+ "FROM public.processtype WHERE nprocesstypecode <> " + objProcessType.getNprocesstypecode()
										+ " and  sprocesstypename=LTRIM(RTRIM('"
										+ stringUtilityFunction.replaceQuote(objProcessType.getSprocesstypename().toString()) + "')) "
										+ "AND nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
										+ userInfo.getNmastersitecode() + "  ;";
			final List<ProcessType> objcheckDupilcate = jdbcTemplate.query(checkDupilcate, new ProcessType());
			if (objcheckDupilcate.isEmpty()) {
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				
				final String updateQuery = "UPDATE public.processtype " + "	SET  sprocesstypename='"
											+ stringUtilityFunction.replaceQuote(objProcessType.getSprocesstypename().toString())
											+ "', sdescription='"
											+ stringUtilityFunction.replaceQuote(objProcessType.getSdescription().toString())
											+ "', dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "'  WHERE nprocesstypecode=" + objProcessType.getNprocesstypecode() + "  AND nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nsitecode="+ userInfo.getNmastersitecode() + ";";
				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objProcessTypeOld);
				listBeforeUpdate.add(objProcessType);
				multilingualIDList.add("IDS_EDITPROCESSTYPE");
				
				auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 2, objProcessTypeOld, multilingualIDList,
						userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getProcessType(userInfo);
	}

	/**
	 * This method is used to fetch the active processtype objects for the specified
	 * processtype name and site.
	 * 
	 * @param sprocesstypename [String] name of the processtype
	 * @param nmasterSiteCode  [int] site code of the processtype
	 * @return list of active processtype code(s) based on the specified processtype
	 *         name and site
	 * @throws Exception
	 */

	private List<ProcessType> getProcessTypeValidate(int nprocesstypecode, UserInfo userInfo) throws Exception {

		final String validateCheck = "select * from processtype WHERE nprocesstypecode= " + nprocesstypecode
										+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		return (List<ProcessType>) jdbcTemplate.query(validateCheck, new ProcessType());
	}

	/**
	 * This method id used to delete an entry in processtype table Need to check the
	 * record is already deleted or not
	 * 
	 * @param objprocesstype [processtype] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an processtype
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteProcessType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	
		final ObjectMapper objMapper = new ObjectMapper();
		final ProcessType objProcessType = objMapper.convertValue(inputMap.get("processtype"), ProcessType.class);
		
		final List<ProcessType> objProcessTypeOld = getProcessTypeValidate(objProcessType.getNprocesstypecode(),
				userInfo);
		
		if (objProcessTypeOld.isEmpty()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String recordInProcessType = "select 'IDS_SAMPLEPROCESSMAPPING' as Msg from sampleprocesstype where "
											+ " nprocesstypecode=" + objProcessType.getNprocesstypecode() + "  " + " and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
											+ userInfo.getNmastersitecode() + "";
			valiDatorDel = projectDAOSupport.getTransactionInfo(recordInProcessType, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objProcessType.getNprocesstypecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				
				final String deleteQuery = "UPDATE public.processtype SET nstatus="
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nprocesstypecode="
											+ objProcessType.getNprocesstypecode() + " and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and nsitecode=" + userInfo.getNmastersitecode() + ";";
				jdbcTemplate.execute(deleteQuery);
				savedTestList.add(objProcessType);
				multilingualIDList.add("IDS_DELETEPROCESSTYPE");
				
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getProcessType(userInfo);
	}
}
