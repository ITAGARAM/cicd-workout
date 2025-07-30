package com.agaramtech.qualis.materialmanagement.service.materialgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
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
import com.agaramtech.qualis.materialmanagement.model.MaterialGrade;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on MaterialGrade table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class MaterialGradeDAOImpl implements MaterialGradeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialGradeDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available materialgrades for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         materialgrades
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getMaterialGrade(final UserInfo userInfo) throws Exception {
		final String strQuery = "select mg.nmaterialgradecode,mg.jsondata->>'smaterialgradename' as smaterialgradename"
				+ ",mg.ndefaultstatus, mg.jsondata->>'sdescription'  as sdescription , "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as sdisplaystatus "
				+ " from materialgrade mg,transactionstatus ts" + " where mg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mg.nmaterialgradecode>0 and ts.ntranscode=mg.ndefaultstatus and mg.nsitecode = "
				+ userInfo.getNmastersitecode();
		LOGGER.info("getMaterialGrade query -->"+strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new MaterialGrade()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active materialgrade object based on the
	 * specified nmaterialgradeCode.
	 * 
	 * @param nmaterialgradeCode [int] primary key of materialgradeobject
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         materialgrade object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public MaterialGrade getActiveMaterialGradeById(final int nmaterialgradecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select mg.nmaterialgradecode,mg.jsondata->>'smaterialgradename' as smaterialgradename"
				+ ",mg.ndefaultstatus, mg.jsondata->>'sdescription'  as sdescription , "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US')  as sdisplaystatus "
				+ " from materialgrade mg,transactionstatus ts" + " where mg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode=mg.ndefaultstatus and mg.nmaterialgradecode = "+ nmaterialgradecode+"";
		return (MaterialGrade) jdbcUtilityFunction.queryForObject(strQuery, MaterialGrade.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to materialgrade table. materialgrade
	 * Name is unique across the database. Need to check for duplicate entry of
	 * materialgrade name for the specified site before saving into database. * Need
	 * to check that there should be only one default materialgrade for a site.
	 * 
	 * @param objMaterialGrade [MaterialGrade] object holding details to be added in
	 *                         materialgrade table
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return saved unit object with status code 200 if saved successfully else if
	 *         the materialgrade already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MaterialGrade materialGrade = objmapper.convertValue(inputMap.get("materialgrade"), MaterialGrade.class);
		JSONObject jsonObject = new JSONObject(materialGrade.getJsondata());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedMaterialGradeList = new ArrayList<>();
		final MaterialGrade materialGradeListByName = getMaterialGradeByName(
				jsonObject.getString("smaterialgradename"), materialGrade.getNsitecode());
		if (materialGradeListByName == null) {
			if (materialGrade.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final MaterialGrade defaultMaterialGrade = getMaterialGradeByDefaultStatus(
						materialGrade.getNsitecode());
				if (defaultMaterialGrade != null) {
					final MaterialGrade materialGradeBeforeSave = SerializationUtils.clone(defaultMaterialGrade);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(materialGradeBeforeSave);
					defaultMaterialGrade
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update materialgrade set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nmaterialgradecode ="
							+ defaultMaterialGrade.getNmaterialgradecode()+"";
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultMaterialGrade);
					multilingualIDList.add("IDS_EDITMATERIALGRADE");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}

			}
			String sequencequery = "select nsequenceno from seqnomaterialmanagement where stablename ='materialgrade'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert  into materialgrade (nmaterialgradecode,jsondata,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,ndefaultstatus,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",'" + stringUtilityFunction.replaceQuote(jsonObject.toString())
					+ "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNtimezonecode() + "," + " "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ materialGrade.getNdefaultstatus() + "," + "" + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update seqnomaterialmanagement set nsequenceno=" + nsequenceno
					+ " where stablename ='materialgrade'";
			jdbcTemplate.execute(updatequery);
			materialGrade.setSmaterialgradename((String) materialGrade.getJsondata().get("smaterialgradename"));
			materialGrade.setSdescription((String) materialGrade.getJsondata().get("sdescription"));
			materialGrade.setNmaterialgradecode((short) nsequenceno);
			savedMaterialGradeList.add(materialGrade);
			multilingualIDList.add("IDS_ADDMATERIALGRADE");
			auditUtilityFunction.fnInsertAuditAction(savedMaterialGradeList, 1, null, multilingualIDList, userInfo);
			return getMaterialGrade(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get the default MaterialGrade object with respect to
	 * the site.
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @return MaterialGrade Object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private MaterialGrade getMaterialGradeByDefaultStatus(int nmasterSiteCode) throws Exception {
		final String strQuery = "select *, mg.jsondata->>'smaterialgradename' as smaterialgradename from materialgrade mg"
				+ " where mg.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mg.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and mg.nsitecode = " + nmasterSiteCode;
		return (MaterialGrade) jdbcUtilityFunction.queryForObject(strQuery, MaterialGrade.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the unit object for the specified materialgrade
	 * name and site.
	 * 
	 * @param sunitname       [String] name of the materialgrade
	 * @param nmasterSiteCode [int] site code of the materialgrade
	 * @return materialgrade object based on the specified materialgrade name and
	 *         site
	 * @throws Exception that are thrown from this DAO layer
	 */

	private MaterialGrade getMaterialGradeByName(final String smaterialgradename, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nmaterialgradecode from materialgrade "
				+ " where lower(jsondata->>'smaterialgradename')=lower('"
				+ stringUtilityFunction.replaceQuote(smaterialgradename) + "') and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (MaterialGrade) jdbcUtilityFunction.queryForObject(strQuery, MaterialGrade.class, jdbcTemplate);
	}

	/**
	 * lower This method is used to update entry in materialgrade table. Need to
	 * validate that the materialgrade object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * smaterialgradename for the specified site before saving into database.
	 * 
	 * @param materialgrade [MaterialGrade] object holding details to be updated in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of updated
	 *         materialgrade object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MaterialGrade materialGrade = objmapper.convertValue(inputMap.get("materialgrade"), MaterialGrade.class);
		JSONObject jsonObject = new JSONObject(materialGrade.getJsondata());
		final MaterialGrade objMaterialGrade = getActiveMaterialGradeById(materialGrade.getNmaterialgradecode(),
				userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (objMaterialGrade == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nmaterialgradecode from materialgrade "
					+ " where lower(jsondata->>'smaterialgradename')=lower('"
					+ stringUtilityFunction.replaceQuote(materialGrade.getSmaterialgradename()) + "')"
					+ " and nmaterialgradecode <> " + materialGrade.getNmaterialgradecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNmastersitecode()+"";
			final List<MaterialGrade> materialGradeList = (List<MaterialGrade>) jdbcTemplate.query(queryString,
					new MaterialGrade());
			if (materialGradeList.isEmpty()) {
				if (materialGrade.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final MaterialGrade defaultMaterialGrade = getMaterialGradeByDefaultStatus(
							userInfo.getNmastersitecode());
					if (defaultMaterialGrade != null
							&& defaultMaterialGrade.getNmaterialgradecode() != materialGrade.getNmaterialgradecode()) {
						final MaterialGrade materialGradeBeforeSave = SerializationUtils.clone(defaultMaterialGrade);
						listBeforeUpdate.add(materialGradeBeforeSave);
						defaultMaterialGrade
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update materialgrade set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nmaterialgradecode="
								+ defaultMaterialGrade.getNmaterialgradecode()+"";
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultMaterialGrade);
						multilingualIDList.add("IDS_EDITMATERIALGRADE");
					}
				}
				final String updateQueryString = "update materialgrade set jsondata=N'"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb," + " ndefaultstatus = "
						+ materialGrade.getNdefaultstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntzmodifieddate="
						+ userInfo.getNtimezonecode() + ", noffsetdmodifieddate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nmaterialgradecode=" + materialGrade.getNmaterialgradecode();
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(materialGrade);
				listBeforeUpdate.add(objMaterialGrade);
				multilingualIDList.add("IDS_EDITMATERIALGRADE");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getMaterialGrade(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to delete entry in materialgrade table. Need to validate
	 * that the specified materialgrade object is active and is not associated with
	 * any of its child tables before updating its nstatus to -1.
	 * 
	 * @param materialgrade [MaterialGrade] object holding detail to be deleted in
	 *                      materialgrade table
	 * @return response entity object holding response status and data of deleted
	 *         materialgrade object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteMaterialGrade(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MaterialGrade materialGrade = objmapper.convertValue(inputMap.get("materialgrade"), MaterialGrade.class);
		final MaterialGrade materialGradeByID = (MaterialGrade) getActiveMaterialGradeById(
				materialGrade.getNmaterialgradecode(), userInfo);
		if (materialGradeByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_MATERIALINVENTORY' as Msg from materialinventory where (jsondata->'Grade'->>'value')::int= "
					+ materialGrade.getNmaterialgradecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedMaterialGradeList = new ArrayList<>();
				final String updateQueryString = "update materialgrade set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntzmodifieddate="
						+ userInfo.getNtimezonecode() + ", noffsetdmodifieddate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nmaterialgradecode=" + materialGrade.getNmaterialgradecode();
				jdbcTemplate.execute(updateQueryString);
				materialGrade.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedMaterialGradeList.add(materialGrade);
				multilingualIDList.add("IDS_DELETEMATERIALGRADE");
				auditUtilityFunction.fnInsertAuditAction(savedMaterialGradeList, 1, null, multilingualIDList, userInfo);
				return getMaterialGrade(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
