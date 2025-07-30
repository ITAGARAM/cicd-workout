package com.agaramtech.qualis.basemaster.service.samplingpoint;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.SamplingPoint;
import com.agaramtech.qualis.basemaster.model.SamplingPointCategory;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplingpoint" table by
 * implementing methods from its interface.
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@AllArgsConstructor
@Repository
public class SamplingPointDAOImpl implements SamplingPointDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingPointDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active samplingpoint for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplingpoint
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getSamplingPoint(final UserInfo userInfo) throws Exception {
		final String strQuery = "select sp.*,spc.ssamplingpointcatname, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, to_char(sp.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate from samplingpoint sp "
				+ "join samplingpointcategory spc on spc.nsamplingpointcatcode=sp.nsamplingpointcatcode "
				+ "join transactionstatus ts on ts.ntranscode=sp.ndefaultstatus where sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsamplingpointcode > 0 and spc.nsitecode=" + userInfo.getNmastersitecode()
				+ " and sp.nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getSamplingPoint -> " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SamplingPoint()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active samplingpointcategory for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplingpointcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getSamplingPointCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select nsamplingpointcatcode,ssamplingpointcatname " + "from samplingpointcategory  "
				+ "where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsamplingpointcatcode > 0   and nsitecode=" + userInfo.getNmastersitecode();
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SamplingPointCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active samplingpoint object based on the
	 * specified nsamplingPointCode.
	 * 
	 * @param nsamplingPointCode [int] primary key of samplingpoint object
	 * @return response entity object holding response status and data of
	 *         samplingpoint object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public SamplingPoint getActiveSamplingPointById(final int nsamplingPointCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sp.*,spc.ssamplingpointcatname,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, " + "to_char(sp.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate " + "from samplingpoint sp "
				+ "join samplingpointcategory spc on spc.nsamplingpointcatcode=sp.nsamplingpointcatcode "
				+ "join transactionstatus ts on ts.ntranscode=spc.ndefaultstatus " + "where sp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsamplingpointcode > 0 and sp.nsamplingpointcode=" + nsamplingPointCode
				+ " and spc.nsitecode=" + userInfo.getNmastersitecode() + " and sp.nsitecode="
				+ userInfo.getNmastersitecode();
		return (SamplingPoint) jdbcUtilityFunction.queryForObject(strQuery, SamplingPoint.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to samplingpoint table. Need to check
	 * for duplicate entry of samplingpoint name for the specified site before
	 * saving into database. Need to check that there should be only one default
	 * samplingpoint for a site
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be added in
	 *                         samplingpoint table
	 * @return inserted samplingpoint object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table samplingpoint " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSPList = new ArrayList<>();

		final SamplingPoint samplingPointByName = getSamplingPointByName(objSamplingPoint.getSsamplingpointname(),
				objSamplingPoint.getNsamplingpointcatcode(), objSamplingPoint.getNsitecode());
		if (samplingPointByName == null) {
			if (objSamplingPoint.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final SamplingPoint defaultSamplingPoint = getSamplingPointByDefaultStatus(
						objSamplingPoint.getNsitecode());
				if (defaultSamplingPoint != null) {
					final SamplingPoint spBeforeSave = SerializationUtils.clone(defaultSamplingPoint);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(spBeforeSave);
					defaultSamplingPoint
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update samplingpoint set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsamplingpointcode ="
							+ defaultSamplingPoint.getNsamplingpointcode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultSamplingPoint);

					multilingualIDList.add("IDS_EDITSAMPLINGPOINT");

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}

			}
			final String sequencenoquery = "select nsequenceno from seqnobasemaster where stablename ='samplingpoint' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			final String insertquery = "Insert into samplingpoint (nsamplingpointcode,nsamplingpointcatcode,ssamplingpointname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus) "
					+ " values(" + nsequenceno + "," + objSamplingPoint.getNsamplingpointcatcode() + ",N'"
					+ stringUtilityFunction.replaceQuote(objSamplingPoint.getSsamplingpointname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSamplingPoint.getSdescription()) + "',"
					+ objSamplingPoint.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnobasemaster set nsequenceno =" + nsequenceno
					+ " where stablename='samplingpoint' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			jdbcTemplate.execute(updatequery);

			objSamplingPoint.setNsamplingpointcode(nsequenceno);
			savedSPList.add(objSamplingPoint);
			multilingualIDList.add("IDS_ADDSAMPLINGPOINT");
			auditUtilityFunction.fnInsertAuditAction(savedSPList, 1, null, multilingualIDList, userInfo);
			return getSamplingPoint(userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in samplingpoint table. Need to validate
	 * that the samplingpoint object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of samplingpoint name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default samplingpoint for a site
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be updated
	 *                         in samplingpoint table
	 * @return response entity object holding response status and data of updated
	 *         samplingpoint object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		final SamplingPoint samplingpoint = getActiveSamplingPointById(objSamplingPoint.getNsamplingpointcode(),
				userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (samplingpoint == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsamplingpointcode from samplingpoint where ssamplingpointname = '"
					+ stringUtilityFunction.replaceQuote(objSamplingPoint.getSsamplingpointname())
					+ "' and nsamplingpointcode <> " + objSamplingPoint.getNsamplingpointcode()
					+ " and nsamplingpointcatcode <> " + objSamplingPoint.getNsamplingpointcatcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();

			final List<SamplingPoint> samplingpointList = (List<SamplingPoint>) jdbcTemplate.query(queryString,
					new SamplingPoint());

			if (samplingpointList.isEmpty()) {
				if (objSamplingPoint.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final SamplingPoint defaultSamplingPoint = getSamplingPointByDefaultStatus(
							objSamplingPoint.getNsitecode());

					if (defaultSamplingPoint != null && defaultSamplingPoint.getNsamplingpointcode() != objSamplingPoint
							.getNsamplingpointcode()) {

						final SamplingPoint spBeforeSave = SerializationUtils.clone(defaultSamplingPoint);
						listBeforeUpdate.add(spBeforeSave);

						defaultSamplingPoint
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update samplingpoint set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsamplingpointcode="
								+ defaultSamplingPoint.getNsamplingpointcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultSamplingPoint);
					}

				}
				final String updateQueryString = "update samplingpoint set ssamplingpointname=N'"
						+ stringUtilityFunction.replaceQuote(objSamplingPoint.getSsamplingpointname())
						+ "', nsamplingpointcatcode =" + objSamplingPoint.getNsamplingpointcatcode()
						+ ", sdescription ='" + stringUtilityFunction.replaceQuote(objSamplingPoint.getSdescription())
						+ "',ndefaultstatus=" + objSamplingPoint.getNdefaultstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsamplingpointcode="
						+ objSamplingPoint.getNsamplingpointcode() + ";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objSamplingPoint);
				listBeforeUpdate.add(samplingpoint);
				multilingualIDList.add("IDS_EDITSAMPLINGPOINT");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getSamplingPoint(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in samplingpoint table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'testparameter'
	 * 
	 * @param objSamplingPoint [SamplingPoint] an Object holds the record to be
	 *                         deleted
	 * @return a response entity with corresponding HTTP status and an samplingpoint
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		final SamplingPoint samplingpoint = getActiveSamplingPointById(objSamplingPoint.getNsamplingpointcode(),
				userInfo);
		if (samplingpoint == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> deletedSamplingPointList = new ArrayList<>();
			final String updateQueryString = "update samplingpoint set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsamplingpointcode="
					+ objSamplingPoint.getNsamplingpointcode();
			jdbcTemplate.execute(updateQueryString);
			objSamplingPoint.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedSamplingPointList.add(objSamplingPoint);
			multilingualIDList.add("IDS_DELETESAMPLINGPOINT");
			auditUtilityFunction.fnInsertAuditAction(deletedSamplingPointList, 1, null, multilingualIDList, userInfo);
			return getSamplingPoint(userInfo);
		}
	}

	/**
	 * This method is used to get a default samplingpoint object with respect to the
	 * site code
	 * 
	 * @param nmasterSiteCode    [int] Site code
	 * @param nsamplingPointCode [int] primary key of samplingpoint table
	 *                           nsamplingpointcode
	 * @return a samplingpoint Object
	 * @throws Exception that are from DAO layer
	 */
	private SamplingPoint getSamplingPointByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = " select * from samplingpoint sp where sp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and sp.nsitecode = " + nmasterSiteCode;
		return (SamplingPoint) jdbcUtilityFunction.queryForObject(strQuery, SamplingPoint.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active samplingpoint objects for the
	 * specified samplingpoint name and site.
	 * 
	 * @param ssamplingpointname [String] name of the samplingpoint
	 * @param nmasterSiteCode    [int] site code of the samplingpoint
	 * @return list of active samplingpoint code(s) based on the specified
	 *         samplingpoint name and site
	 * @throws Exception
	 */
	private SamplingPoint getSamplingPointByName(final String ssamplingPointName, final int nsamplingPointCatCode,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsamplingpointcode from samplingpoint where ssamplingpointname = N'"
				+ stringUtilityFunction.replaceQuote(ssamplingPointName) + "' and nsamplingpointcatcode="
				+ nsamplingPointCatCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (SamplingPoint) jdbcUtilityFunction.queryForObject(strQuery, SamplingPoint.class, jdbcTemplate);
	}

}
