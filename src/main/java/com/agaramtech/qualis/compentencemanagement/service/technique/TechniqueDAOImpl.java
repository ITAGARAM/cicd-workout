package com.agaramtech.qualis.compentencemanagement.service.technique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TechniqueTest;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.testmanagement.model.TestMaster;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "technique" table by
 * implementing methods from its interface.
 */

@Repository
@AllArgsConstructor
public class TechniqueDAOImpl implements TechniqueDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TechniqueDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active Technique for the
	 * specified site.
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Technique
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getTechnique(Integer ntechniquecode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Technique selectedTechnique = null;
		String strQuery = "";
		if (ntechniquecode == null) {
			strQuery = "select t.*" + "  from Technique t" + " where t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and t.ntechniquecode>0  and t.nsitecode=" + userInfo.getNmastersitecode()
					+ " order by t.ntechniquecode";

			LOGGER.info("getTechnique-->" + strQuery);
			final List<Technique> techniqueList = jdbcTemplate.query(strQuery, new Technique());
			if (techniqueList.isEmpty()) {
				outputMap.put("Technique", techniqueList);
				outputMap.put("SelectedTechnique", null);
				outputMap.put("TechniqueTest", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("Technique", techniqueList);
				selectedTechnique = techniqueList.get(techniqueList.size() - 1);
				ntechniquecode = selectedTechnique.getNtechniquecode();

				strQuery = "select tt.ntechniquetestcode,tt.ntechniquecode,tm.ntestcode,tm.stestname,tt.nstatus from techniquetest tt,testmaster tm "
						+ " where tt.ntestcode = tm.ntestcode and tt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.ntechniquecode = "
						+ ntechniquecode + " and tm.nsitecode =" + userInfo.getNmastersitecode();
			    List<TechniqueTest> techniqueTestList = jdbcTemplate.query(strQuery, new TechniqueTest());
				outputMap.put("TechniqueTest", techniqueTestList);
			}
		} else {
			final Technique techniqueList = getActiveTechniqueById(ntechniquecode);
			selectedTechnique = techniqueList;
			ntechniquecode = selectedTechnique != null ? selectedTechnique.getNtechniquecode() : null;

			strQuery = "select t.*" + "  from Technique t" + " where t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and t.ntechniquecode>0  and t.nsitecode=" + userInfo.getNmastersitecode()
					+ " order by t.ntechniquecode";

			final List<Technique> techniqueLst = jdbcTemplate.query(strQuery, new Technique());

			outputMap.put("Technique", techniqueLst);

			if (ntechniquecode != null) {
				strQuery = "select tt.ntechniquetestcode,tt.ntechniquecode,tm.ntestcode,tm.stestname,tt.nstatus from techniquetest tt,testmaster tm "
						+ " where tt.ntestcode = tm.ntestcode and tt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.ntechniquecode = "
						+ ntechniquecode + " and tm.nsitecode =" + userInfo.getNmastersitecode();
				List<TechniqueTest> techniqueTestList = jdbcTemplate.query(strQuery, new TechniqueTest());
				outputMap.put("TechniqueTest", techniqueTestList);
			}

		}
		if (selectedTechnique == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedTechnique", selectedTechnique);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve active Technique object based on the
	 * specified ntechniquecode.
	 * 
	 * @param ntechniquecode [int] primary key of Technique object
	 * @return response entity object holding response status and data of Technique
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public Technique getActiveTechniqueById(final int ntechniquecode) throws Exception {
		final String strQuery = "select t.*  from Technique t " + " where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.ntechniquecode = "
				+ ntechniquecode;
		return (Technique) jdbcUtilityFunction.queryForObject(strQuery, Technique.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to Technique table. Need to check for
	 * duplicate entry of Technique name for the specified site before saving into
	 * database. Need to check that there should be only one default Technique for a
	 * site
	 * 
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @param objTechnique [Technique] object holding details to be added in
	 *                     Technique table
	 * @return inserted Technique object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createTechnique(final Technique objTechnique, final UserInfo userInfo)
			throws Exception {

		final List<Object> savedTechniqueList = new ArrayList<>();

		final Technique techniqueListByName = (Technique) getTechniqueByName(objTechnique.getStechniquename(),
				objTechnique.getNsitecode());
		if (techniqueListByName == null) {
			String seqquery = "select nsequenceno from SeqNoCompetenceManagement where stablename ='technique'";
			int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into technique (ntechniquecode,stechniquename,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objTechnique.getStechniquename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objTechnique.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			String updatequery = "update SeqNoCompetenceManagement set nsequenceno=" + nsequenceno
					+ " where stablename='technique'";
			jdbcTemplate.execute(updatequery);

			objTechnique.setNtechniquecode(nsequenceno);

			savedTechniqueList.add(objTechnique);

			auditUtilityFunction.fnInsertAuditAction(savedTechniqueList, 1, null, Arrays.asList("IDS_ADDTECHNIQUE"),
					userInfo);

			return getTechnique(null, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in Technique table. Need to validate that
	 * the Technique object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of Technique name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default Technique for a site
	 * 
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @param objTechnique [Technique] object holding details to be updated in
	 *                     Technique table
	 * @return response entity object holding response status and data of updated
	 *         Technique object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateTechnique(final Technique objTechnique, final UserInfo userInfo)
			throws Exception {

		final Technique technique = getActiveTechniqueById(objTechnique.getNtechniquecode());

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (technique == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ntechniquecode from technique where stechniquename = N'"
					+ stringUtilityFunction.replaceQuote(objTechnique.getStechniquename()) + "' and ntechniquecode <> "
					+ objTechnique.getNtechniquecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode();

			final List<Technique> techniqueList = (List<Technique>) jdbcTemplate.query(queryString, new Technique());

			if (techniqueList.isEmpty()) {

				final String updateQueryString = "update technique set stechniquename=N'"
						+ stringUtilityFunction.replaceQuote(objTechnique.getStechniquename()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(objTechnique.getSdescription()) + "', dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntechniquecode="
						+ objTechnique.getNtechniquecode() + ";";

				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(objTechnique);
				listBeforeUpdate.add(technique);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
						Arrays.asList("IDS_EDITTECHNIQUE"), userInfo);

				return getTechnique(objTechnique.getNtechniquecode(), userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Technique table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'instrumentcategory'
	 * 
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @param objTechnique [Technique] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an Technique
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteTechnique(final Technique objTechnique, final UserInfo userInfo)
			throws Exception {
		final Technique technique = getActiveTechniqueById(objTechnique.getNtechniquecode());

		if (technique == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_TRAININGCERTIFICATE' as Msg from trainingcertification where ntechniquecode= "
					+ objTechnique.getNtechniquecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objTechnique.getNtechniquecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Object> deletedTechniqueList = new ArrayList<>();
				String updateQueryString = "update technique set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntechniquecode="
						+ objTechnique.getNtechniquecode();

				jdbcTemplate.execute(updateQueryString);
				objTechnique.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedTechniqueList.add(objTechnique);

				final String testListQuery = "select * from techniquetest where ntechniquecode="
						+ objTechnique.getNtechniquecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
						+ userInfo.getNmastersitecode();
				final List<TechniqueTest> testList = jdbcTemplate.query(testListQuery, new TechniqueTest());

				updateQueryString = "update techniquetest set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntechniquecode="
						+ objTechnique.getNtechniquecode();
				jdbcTemplate.execute(updateQueryString);
				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(testList, Arrays.asList(objTechnique)), 1,
						null, Arrays.asList("IDS_DELETETEST", "IDS_DELETETECHNIQUE"), userInfo);

				return getTechnique(null, userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active Technique objects for the specified
	 * Technique name and site.
	 * 
	 * @param stechniquename  [String] name of the Technique
	 * @param nmasterSiteCode [int] site code of the Technique
	 * @return list of active Technique code(s) based on the specified Technique
	 *         name and site
	 * @throws Exception
	 */
	private Technique getTechniqueByName(final String stechniquename, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ntechniquecode from technique where stechniquename = N'"
				+ stringUtilityFunction.replaceQuote(stechniquename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Technique) jdbcUtilityFunction.queryForObject(strQuery, Technique.class, jdbcTemplate);
	}

	public ResponseEntity<Object> getTechniqueTest( int ntechniqueCode, final UserInfo userInfo) throws Exception {

		final Technique technique = getActiveTechniqueById(ntechniqueCode);

		if (technique == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String queryString = "select tm.ntestcode,tm.stestname from testmaster tm where ntrainingneed = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntestcode  not in "
				+ " (select tt.ntestcode from techniquetest tt where  ntechniquecode=" + ntechniqueCode
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
				+ " and tm.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + userInfo.getNmastersitecode()
				+ " and  tm.ntestcode >-1 order by stestname";

		final List<TestMaster> testList = jdbcTemplate.query(queryString, new TestMaster());

		return new ResponseEntity<>(testList, HttpStatus.OK);

	}

	public ResponseEntity<Object> getTechniqueConducted(final int ntechniqueCode, final UserInfo userInfo)
			throws Exception {

		final String queryString = "select * from trainingcertification where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.CONDUCTED.gettransactionstatus() + "  and ntechniquecode = "
				+ ntechniqueCode + " and nsitecode =" + userInfo.getNmastersitecode();

		final List<TrainingCertification> trainingConduct = jdbcTemplate.query(queryString,
				new TrainingCertification());

		return new ResponseEntity<>(trainingConduct, HttpStatus.OK);

	}

	public ResponseEntity<Object> getTechniqueScheduled(final int ntechniqueCode, final UserInfo userInfo)
			throws Exception {

		final String queryString = "select * from trainingcertification where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + "  and ntechniquecode = "
				+ ntechniqueCode + " and nsitecode =" + userInfo.getNmastersitecode();

		final List<TrainingCertification> trainingScheduled = jdbcTemplate.query(queryString,
				new TrainingCertification());

		return new ResponseEntity<>(trainingScheduled, HttpStatus.OK);

	}

	public ResponseEntity<Object> createTechniqueTest(final List<TechniqueTest> techniqueTestList,
			final int ntechniqueCode, UserInfo userInfo) throws Exception {

		final Technique techniqueById = getActiveTechniqueById(ntechniqueCode);

		if (techniqueById == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TECHNIQUEALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {

			final String testCode = stringUtilityFunction.fnDynamicListToString(techniqueTestList, "getNtestcode");

			final String seqString = "select nsequenceno from SeqNoCompetenceManagement  where stablename ='techniquetest'";
			int nsequenceNo = jdbcTemplate.queryForObject(seqString, Integer.class) + 1;

			final int addedTestCount = (nsequenceNo - 1) + techniqueTestList.size();

			String queryString = "INSERT INTO techniquetest (ntechniquetestcode, ntechniquecode, ntestcode,dmodifieddate, nsitecode, nstatus) "
					+ " SELECT rank() over(order by ntestcode asc)+ " + nsequenceNo + " as ntechniquetestcode, "
					+ ntechniqueCode + " , ntestcode, " + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ techniqueById.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from testmaster tm 	where ntrainingneed = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode ="
							+ userInfo.getNmastersitecode()+" and ntestcode in (" + testCode
					+ ") and NOT EXISTS " + " ( SELECT * FROM techniquetest tt WHERE tm.ntestcode = tt.ntestcode "
					+ " and tt.ntechniquecode=" + ntechniqueCode + " and tt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + ")";

			jdbcTemplate.execute(queryString);

			queryString = "update SeqNoCompetenceManagement set nsequenceno = " + addedTestCount
					+ " where stablename = 'techniquetest'";
			jdbcTemplate.execute(queryString);

			String qry = " SELECT rank() over(order by ntestcode asc)+ " + nsequenceNo + " as ntechniquetestcode, "
					+ ntechniqueCode + " as ntechniquecode , ntestcode, " + "dmodifieddate,"
					+ techniqueById.getNsitecode() + " as nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " as nstatus from testmaster tm 	where ntrainingneed = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntestcode in (" + testCode + ")";

			final List<TechniqueTest> testList = (List<TechniqueTest>) jdbcTemplate.query(qry, new TechniqueTest());

			final List<Object> savedList = new ArrayList<>();
			savedList.add(testList);

			final List<String> multiLingualIDList = new ArrayList<String>();
			multiLingualIDList.add("IDS_ADDTECHNIQUETEST");

			auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

			return getTechnique(ntechniqueCode, userInfo);
		}
	}

	public ResponseEntity<Object> deleteTechniqueTest(final TechniqueTest techniquetest, final int ntechniqueCode,
			final UserInfo userInfo) throws Exception {
		final Technique techniqueById = getActiveTechniqueById(ntechniqueCode);

		if (techniqueById == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TECHNIQUEALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<String>();
			final List<Object> savedList = new ArrayList<>();

			String sQuery = "select * from techniquetest where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntechniquetestcode = "
					+ techniquetest.getNtechniquetestcode();
			final TechniqueTest techniquetestById = (TechniqueTest) jdbcUtilityFunction.queryForObject(sQuery,
					TechniqueTest.class, jdbcTemplate);

			if (techniquetestById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				sQuery = "update techniquetest set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntechniquetestcode = "
						+ techniquetest.getNtechniquetestcode();
				jdbcTemplate.execute(sQuery);

				techniquetest.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedList.add(techniquetest);
				multilingualIDList.add("IDS_DELETETECHNIQUETEST");
				auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

				return getTechnique(ntechniqueCode, userInfo);
			}
		}
	}
}
