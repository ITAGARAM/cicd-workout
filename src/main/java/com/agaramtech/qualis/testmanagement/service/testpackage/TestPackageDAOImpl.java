package com.agaramtech.qualis.testmanagement.service.testpackage;

import java.util.ArrayList;
import java.util.List;

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
import com.agaramtech.qualis.testmanagement.model.TestPackage;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestPackageDAOImpl implements TestPackageDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPackageDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available testpackages for the specified
	 * site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         testpackages
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception {
		final String strQuery = "select ntestpackagecode,stestpackagename,ntestpackageprice,ntestpackagetatdays,sopenmrsrefcode,spreventtbrefcode,sportalrefcode,sdescription, "
				+ "to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate from testpackage where ntestpackagecode>0 "
				+ "and nsitecode="+userInfo.getNmastersitecode()+" and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		LOGGER.info("Get Method:"+ strQuery);

		return new ResponseEntity<>(
				(List<TestPackage>) (List<TestPackage>) jdbcTemplate.query(strQuery, new TestPackage()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createTestPackage(TestPackage packages, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table testpackage " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final TestPackage packageListByName = getPackageByName(packages.getStestpackagename(),userInfo);

		if (packageListByName==null) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedPackageList = new ArrayList<>();
			final String sequencequery = "select nsequenceno from SeqNoTestManagement where stablename ='testpackage'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;

			final String insertQuery = "Insert  into testpackage (ntestpackagecode,stestpackagename,ntestpackageprice,ntestpackagetatdays,sopenmrsrefcode,spreventtbrefcode,sportalrefcode,sdescription,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(packages.getStestpackagename()) + "',"
					+ packages.getNtestpackageprice() + "," + packages.getNtestpackagetatdays() + ",N'"
					+ stringUtilityFunction.replaceQuote(packages.getSopenmrsrefcode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(packages.getSpreventtbrefcode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(packages.getSportalrefcode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(packages.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			
			//LOGGER.info("Insert Method:"+ insertQuery);
	   		jdbcTemplate.execute(insertQuery);

			final String updatequery = "update SeqNoTestManagement set nsequenceno=" + nsequenceno
					+ " where stablename ='testpackage'";
			jdbcTemplate.execute(updatequery);
			packages.setNtestpackagecode(nsequenceno);
			savedPackageList.add(packages);
			multilingualIDList.add("IDS_ADDPACKAGE");

			auditUtilityFunction.fnInsertAuditAction(savedPackageList, 1, null, multilingualIDList, userInfo);

			// status code:200
			return getTestPackage(userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	private TestPackage getPackageByName(String spackagename, UserInfo userInfo) throws Exception {
		final String strQuery = "select ntestpackagecode from testpackage where stestpackagename = N'"
				+ stringUtilityFunction.replaceQuote(spackagename) + "' and nsitecode="+userInfo.getNmastersitecode()+"  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (TestPackage)jdbcUtilityFunction.queryForObject(strQuery, TestPackage.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> deleteTestPackage(TestPackage packages, UserInfo userInfo) throws Exception {
		final TestPackage packageID = (TestPackage) getActiveTestPackageById(packages.getNtestpackagecode(), userInfo);

		if (packageID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			// deleteValidation
			final String query = "select 'IDS_TESTMASTER' as Msg from testpackagetest where ntestpackagecode= "
					+ packageID.getNtestpackagecode() + " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(packages.getNtestpackagecode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedPackageList = new ArrayList<>();
				final String updateQueryString = "update testpackage set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsitecode="+userInfo.getNmastersitecode()+" and  ntestpackagecode="
						+ packages.getNtestpackagecode();
				
				LOGGER.info("Delete Method:"+ updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				packages.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedPackageList.add(packages);
				multilingualIDList.add("IDS_DELETEPACKAGE");

				auditUtilityFunction.fnInsertAuditAction(deletedPackageList, 1, null, multilingualIDList, userInfo);

				// status code:200
				return getTestPackage(userInfo);
			} else {
//				//status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public TestPackage getActiveTestPackageById(int npackagecode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select m.ntestpackagecode,m.stestpackagename,m.ntestpackageprice,m.ntestpackagetatdays,m.sopenmrsrefcode,m.spreventtbrefcode,m.sportalrefcode,m.sdescription from testpackage m "
				+ " where m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and m.nsitecode="+userInfo.getNmastersitecode()+"  and m.ntestpackagecode = " + npackagecode+";";

		return (TestPackage) jdbcUtilityFunction.queryForObject(strQuery, TestPackage.class,jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> updateTestPackage(TestPackage packages, UserInfo userInfo) throws Exception {

		final TestPackage objPackage = getActiveTestPackageById(packages.getNtestpackagecode(), userInfo);

		if (objPackage == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String queryString = "select ntestpackagecode from testpackage where stestpackagename = '"
					+ stringUtilityFunction.replaceQuote(packages.getStestpackagename()) + "' and ntestpackagecode <> "
					+ packages.getNtestpackagecode() + " and nsitecode="+userInfo.getNmastersitecode()+" and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final TestPackage packageList = (TestPackage)jdbcUtilityFunction.queryForObject(queryString, TestPackage.class, jdbcTemplate);

			if (packageList==null) { // if yes need to set other default method as not a default method

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();

				final String updateQueryString = "update testpackage set stestpackagename=N'"
						+ stringUtilityFunction.replaceQuote(packages.getStestpackagename()) + "',ntestpackageprice= "
						+ packages.getNtestpackageprice() + ",ntestpackagetatdays= " + packages.getNtestpackagetatdays()
						+ ", sopenmrsrefcode =N'" + stringUtilityFunction.replaceQuote(packages.getSopenmrsrefcode())
						+ "', spreventtbrefcode =N'"
						+ stringUtilityFunction.replaceQuote(packages.getSpreventtbrefcode()) + "', sportalrefcode =N'"
						+ stringUtilityFunction.replaceQuote(packages.getSportalrefcode()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(packages.getSdescription()) + "', dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "',nsitecode="+userInfo.getNmastersitecode()+"  where ntestpackagecode=" + packages.getNtestpackagecode();

				LOGGER.info("Update Method:"+ updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				multilingualIDList.add("IDS_EDITPACKAGE");
				listAfterUpdate.add(packages);
				listBeforeUpdate.add(objPackage);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				// status code:200
				return getTestPackage(userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

}
