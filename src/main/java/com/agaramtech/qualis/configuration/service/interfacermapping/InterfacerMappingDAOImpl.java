package com.agaramtech.qualis.configuration.service.interfacermapping;

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
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.configuration.model.InterfacerMapping;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "interfacerMapping" table by
 * implementing methods from its interface.
 * 
 * @author At-E234
 *
 */
@AllArgsConstructor
@Repository
public class InterfacerMappingDAOImpl implements InterfacerMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterfacerMappingDAO.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active interfacerMapping for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         interfacerMappings
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getInterfacerMapping(UserInfo userInfo) throws Exception {
		final String strQuery = "select im.ninterfacemappingcode,im.stestname, im.ninterfacetypecode, tm.stestsynonym, "
				+ " it.jsondata->'sinterfacetypename'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sinterfacetypename,tm.ninterfacetypecode ,it.ninterfacetypecode ,   "
				+ " im.ntestcode from interfacermapping im,interfacetype it,testmaster tm"
				+ " where im.ninterfacetypecode=it.ninterfacetypecode and tm.ntestcode=im.ntestcode  "
				+ " and im.nsitecode=" + userInfo.getNmastersitecode() + " and im.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and it.nsitecode="
				+ userInfo.getNmastersitecode() + " and it.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and tm.nsitecode="
				+ userInfo.getNmastersitecode() + " and tm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getInterfacerMapping-->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InterfacerMapping()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to interfacerMapping table. Need to
	 * check for duplicate entry of interfacerMapping name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default interfacerMapping for a site
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          added in interfacerMapping table
	 * @return inserted interfacerMapping object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList = new ArrayList<>();
		final InterfacerMapping interfacerByName = getInterfacerByName(interfacerMapping.getNinterfacetypecode(),
				userInfo.getNmastersitecode(), interfacerMapping.getStestname());
		if (interfacerByName == null) {
			String sequencenoquery = "select nsequenceno from seqnotestmanagement where stablename ='interfacermapping' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "INSERT INTO interfacermapping(ninterfacemappingcode, "
					+ "stestname, ntestcode, ninterfacetypecode, dmodifieddate, nsitecode, nstatus)" + "VALUES ("
					+ nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(interfacerMapping.getStestname()) + "' ,"
					+ interfacerMapping.getNtestcode() + "," + interfacerMapping.getNinterfacetypecode() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update seqnotestmanagement set nsequenceno =" + nsequenceno
					+ " where stablename='interfacermapping' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery);
			savedTestList.add(interfacerMapping);
			multilingualIDList.add("IDS_ADDINTERFACERMAPPING");
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			return getInterfacerMapping(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get a default interfacerMapping object with respect to
	 * the site code
	 * 
	 * @param nmasterSiteCode        [int] Site code
	 * @param ninterfacerMappingCode [int] primary key of interfacerMapping table
	 *                               ninterfacerMappingcode
	 * @return a interfacerMapping Object
	 * @throws Exception that are from DAO layer
	 */
	private InterfacerMapping getInterfacerByName(int ninterfacetypecode, short nsitecode, String Stestname) {
		final String strQuery = "select * from interfacermapping im where im.stestname=N'"
				+ stringUtilityFunction.replaceQuote(Stestname) + "' and im.ninterfacetypecode=" + ninterfacetypecode
				+ " and im.nsitecode=" + nsitecode + "" + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
		return (InterfacerMapping) jdbcTemplate.query(strQuery, new InterfacerMapping());
	}

	/**
	 * This method id used to delete an entry in interfacerMapping table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param interfacerMapping [interfacerMapping] an Object holds the record to be
	 *                          deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         interfacerMapping object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedinterfacermapping = new ArrayList<>();
		final InterfacerMapping interfacermappingdelete = getInterfacerByName(interfacerMapping.getNinterfacetypecode(),
				userInfo.getNmastersitecode(), interfacerMapping.getStestname());
		if (interfacermappingdelete == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String deletequery = "UPDATE interfacermapping SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE ninterfacemappingcode="
					+ interfacerMapping.getNinterfacemappingcode() + ";";
			jdbcTemplate.execute(deletequery);
		}
		interfacerMapping.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
		deletedinterfacermapping.add(interfacerMapping);
		multilingualIDList.add("IDS_DELETEINTERFACERMAPPING");
		auditUtilityFunction.fnInsertAuditAction(deletedinterfacermapping, 1, null, multilingualIDList, userInfo);
		return getInterfacerMapping(userInfo);
	}

	/**
	 * This method is used to update entry in interfacerMapping table. Need to
	 * validate that the interfacerMapping object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * interfacerMapping name for the specified site before saving into database.
	 * Need to check that there should be only one default interfacerMapping for a
	 * site
	 * 
	 * @param interfacerMapping [interfacerMapping] object holding details to be
	 *                          updated in interfacerMapping table
	 * @return response entity object holding response status and data of updated
	 *         interfacerMapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInterfacerMapping(InterfacerMapping interfacerMapping, UserInfo userInfo)
			throws Exception {
		final InterfacerMapping interfacermappingaudit = getActiveInterfacerMappingById(
				interfacerMapping.getNinterfacemappingcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		userInfo.getNmastersitecode();
		final List<InterfacerMapping> objInterfaceMapping = getInterfacerupdateId(
				interfacerMapping.getNinterfacetypecode(), userInfo.getNmastersitecode(),
				interfacerMapping.getNtestcode(), interfacerMapping.getStestname(),
				interfacerMapping.getNinterfacemappingcode());
		if (interfacermappingaudit == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (objInterfaceMapping.get(0).isIsinterfacername() == false
					&& objInterfaceMapping.get(0).isIstestcode() == false) {
				String updateQuery = "update interfacermapping set ninterfacetypecode="
						+ interfacerMapping.getNinterfacetypecode() + "," + " ntestcode="
						+ interfacerMapping.getNtestcode() + ",stestname=N'"
						+ stringUtilityFunction.replaceQuote(interfacerMapping.getStestname()) + "',"
						+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninterfacemappingcode=" + interfacerMapping.getNinterfacemappingcode() + "";
				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(interfacerMapping);
				listBeforeUpdate.add(interfacermappingaudit);
				multilingualIDList.add("IDS_EDITINTERFACERMAPPING");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getInterfacerMapping(userInfo);
			} else {
				String alert = "";
				final boolean isinterfacername = objInterfaceMapping.get(0).isIsinterfacername();
				final boolean istestCode = objInterfaceMapping.get(0).isIstestcode();
				if (isinterfacername == true && istestCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_LIMSTEST", userInfo.getSlanguagefilename())
							+ " and " + commonFunction.getMultilingualMessage("IDS_INTERFACERTEST",
									userInfo.getSlanguagefilename());
				} else if (isinterfacername == true) {
					alert = commonFunction.getMultilingualMessage("IDS_INTERFACERTEST",
							userInfo.getSlanguagefilename());
				} else if (istestCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_LIMSTEST", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * @param ninterfacetypecode
	 * @param nmastersitecode
	 * @param ntestcode
	 * @param stestname
	 * @param ninterfacemappingcode
	 * @return
	 */
	private List<InterfacerMapping> getInterfacerupdateId(int ninterfacetypecode, short nmastersitecode, int ntestcode,
			String stestname, int ninterfacemappingcode) {
		String updateinterfacemappingcode=" and ninterfacemappingcode <> " + ninterfacemappingcode + "";
		final String strQuery = "select CASE WHEN EXISTS(select stestname from interfacermapping "
				+ "where stestname =N'" + stringUtilityFunction.replaceQuote(stestname) + "' and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ninterfacetypecode="
				+ ninterfacetypecode + " and nsitecode=" + nmastersitecode + "" + updateinterfacemappingcode + " )  "
				+ " THEN TRUE ELSE FALSE  End AS isinterfacername,"
				+ " CASE when exists (select ntestcode from interfacermapping where ntestcode =" + ntestcode + " and  "
				+ " nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ nmastersitecode + "" + updateinterfacemappingcode + ")" + " THEN TRUE ELSE FALSE  End AS istestcode;";
		return (List<InterfacerMapping>) jdbcTemplate.query(strQuery, new InterfacerMapping());
	}

	/**
	 * This method is used to retrieve active interfacerMapping object based on the
	 * specified ninterfacerMappingCode.
	 * 
	 * @param ninterfacerMappingCode [int] primary key of interfacerMapping object
	 * @return response entity object holding response status and data of
	 *         interfacerMapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public InterfacerMapping getActiveInterfacerMappingById(int ninterfacemappingcode, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select im.ninterfacemappingcode,im.stestname,tm.stestsynonym,im.ninterfacemappingcode,tm.ntestcode,it.ninterfacetypecode,it.jsondata->'sinterfacetypename'->>'"
				+ userInfo.getSlanguagetypecode() + "'  "
				+ " as sinterfacetypename from interfacermapping im,interfacetype it, "
				+ " testmaster tm  where im.ntestcode=tm.ntestcode  and it.ninterfacetypecode=im.ninterfacetypecode and "
				+ " im.ninterfacemappingcode=" + ninterfacemappingcode + " and im.nsitecode="
				+ userInfo.getNmastersitecode() + " and im.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nsitecode="
				+ userInfo.getNmastersitecode() + " and tm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="
				+ userInfo.getNmastersitecode() + " and it.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (InterfacerMapping) jdbcUtilityFunction.queryForObject(strQuery, InterfacerMapping.class, jdbcTemplate);
	}

	/**
	 * In this method, 'getInterfaceType' combo of value loaded
	 */
	@Override
	public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception {
		final String strQuery = "select ninterfacetypecode ,jsondata->'sinterfacetypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' as sinterfacetypename  from interfacetype where ninterfacetypecode>1";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InterfacerMapping()), HttpStatus.OK);
	}

	/**
	 * In this method, 'getTestMaster' combo of LIMS Test value loaded
	 */
	@Override
	public ResponseEntity<Object> getTestMaster(Map<String, Object> inputMap, UserInfo userInfo) {
		final String strQuery = "select tm.ntestcode, tm.stestsynonym, tm.stestsynonym,tm.ninterfacetypecode from testmaster tm "
				+ " where tm.ninterfacetypecode=" + inputMap.get("ninterfacetypecode")
				+ " and tm.ntestcode not in(select tm.ntestcode from interfacermapping im,testmaster tm  "
				+ " where im.ntestcode=tm.ntestcode  and im.nsitecode=" + userInfo.getNmastersitecode()
				+ " and im.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") and tm.nsitecode=" + userInfo.getNmastersitecode() + " and tm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new TestMaster()), HttpStatus.OK);
	}
}
