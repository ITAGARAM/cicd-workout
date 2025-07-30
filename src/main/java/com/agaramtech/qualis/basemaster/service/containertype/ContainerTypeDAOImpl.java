package com.agaramtech.qualis.basemaster.service.containertype;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "containertype" table by
 * implementing methods from its interface.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 29- Jun- 2020
 */
@AllArgsConstructor
@Repository
public class ContainerTypeDAOImpl implements ContainerTypeDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerTypeDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active containertype for the
	 * specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         containertype
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getContainerType(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from containertype c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncontainertypecode > 0 "
				+ " and c.nsitecode = " + nmasterSiteCode + " order by ncontainertypecode";
		LOGGER.info("Get Container Type Qry :--->"+strQuery);
		return new ResponseEntity<>((List<ContainerType>) jdbcTemplate.query(strQuery, new ContainerType()),
				HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to containertype table. Need to check
	 * for duplicate entry of containertype for the specified site before saving
	 * into database.
	 * 
	 * @param containerType [Containertype] object holding details to be added in
	 *                      ContainerType table
	 * @return response entity object holding response status and data of added
	 *         ContainerType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createContainerType(ContainerType containerType, UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table containertype " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedContainerTypeList = new ArrayList<>();
		final ContainerType ContainerTypeByName = getContainerTypeByName(containerType.getScontainertype(),
				containerType.getNsitecode());
		if (ContainerTypeByName == null) {
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobasemaster where stablename='containertype'", Integer.class);
			nSeqNo++;
			String containerTypeInsert = "insert into containertype(ncontainertypecode,scontainertype,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(containerType.getScontainertype()) + "',N'"
					+ stringUtilityFunction.replaceQuote(containerType.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(containerTypeInsert);
			jdbcTemplate.execute(
					"update seqnobasemaster set nsequenceno = " + nSeqNo + " where stablename='containertype'");
			savedContainerTypeList.add(containerType);
			multilingualIDList.add("IDS_ADDCONTAINERTYPE");
			auditUtilityFunction.fnInsertAuditAction(savedContainerTypeList, 1, null, multilingualIDList, userInfo);
			return getContainerType(containerType.getNsitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active containerType objects for the
	 * specified ContainerType name and site.
	 * 
	 * @param containertypename [String] containertypename for which the records are
	 *                          to be fetched
	 * @param nmasterSiteCode   [int] primary key of site object
	 * @return list of active containerType based on the specified containerType and
	 *         site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ContainerType getContainerTypeByName(final String containertypename, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ncontainertypecode from containertype where scontainertype = N'"
				+ stringUtilityFunction.replaceQuote(containertypename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (ContainerType) jdbcUtilityFunction.queryForObject(strQuery, ContainerType.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in containertype table. Need to validate
	 * that the containertype object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of containertype name for the
	 * specified site before saving into database.
	 * 
	 * @param containertype [Containertype] object holding details to be updated in
	 *                      containertype table
	 * @return response entity object holding response status and data of updated
	 *         containertype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateContainerType(ContainerType containerType, UserInfo userInfo) throws Exception {
		final ContainerType objContainerType = getActiveContainerTypeById(containerType.getNcontainertypecode(),
				userInfo);
		if (objContainerType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String queryString = "select ncontainertypecode from containertype where scontainertype = '"
					+ stringUtilityFunction.replaceQuote(containerType.getScontainertype())
					+ "' and ncontainertypecode <> " + containerType.getNcontainertypecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			final List<ContainerType> containertypeList = (List<ContainerType>) jdbcTemplate.query(queryString,
					new ContainerType());
			if (containertypeList.isEmpty()) {
				final String updateQueryString = "update containertype set scontainertype='"
						+ stringUtilityFunction.replaceQuote(containerType.getScontainertype()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(containerType.getSdescription()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ncontainertypecode="
						+ containerType.getNcontainertypecode() + "";
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITCONTAINERTYPE");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(containerType);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objContainerType);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getContainerType(containerType.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active ContainerType object based on the
	 * specified ncontainertypecode.
	 * 
	 * @param ncontainertypecode [int] primary key of ContainerType object
	 * @return response entity object holding response status and data of
	 *         ContainerType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ContainerType getActiveContainerTypeById(final int ncontainertypecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select * from containertype c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncontainertypecode = "
				+ ncontainertypecode + "";
		return (ContainerType) jdbcUtilityFunction.queryForObject(strQuery, ContainerType.class, jdbcTemplate);
	}

	/**
	 * This method is used to delete entry in ContainerType table. Need to validate
	 * that the specified ContainerType object is active and is not associated with
	 * any of its child tables before updating its nstatus to -1.
	 * 
	 * @param ContainerType [ContainerType] object holding detail to be deleted in
	 *                      ContainerType table
	 * @return response entity object holding response status and data of deleted
	 *         ContainerType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteContainerType(ContainerType containerType, UserInfo userInfo) throws Exception {
		final ContainerType containerTypebyID = (ContainerType) getActiveContainerTypeById(
				containerType.getNcontainertypecode(), userInfo);
		if (containerTypebyID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = " select 'IDS_TESTMASTER' as Msg from testcontainertype where"
					+ " ncontainertypecode= " + containerTypebyID.getNcontainertypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode()
					+ " union all  select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping where"
					+ " ncontainertypecode= " + containerTypebyID.getNcontainertypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode()
					+ " union all  select 'IDS_SAMPLESTORAGESTRUCTURE' as Msg from samplestoragelocation where"
					+ " ncontainertypecode= " + containerTypebyID.getNcontainertypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode()
					+ "  union all select 'IDS_CONTAINERSTRUCTURENAME' as Msg from containerstructure where ncontainertypecode= "
					+ containerTypebyID.getNcontainertypecode() + "	and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(containerTypebyID.getNcontainertypecode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedContainerTypeList = new ArrayList<>();
				final String updateQueryString = "update containertype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncontainertypecode="
						+ containerType.getNcontainertypecode() + "";
				jdbcTemplate.execute(updateQueryString);
				containerType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedContainerTypeList.add(containerType);
				multilingualIDList.add("IDS_DELETECONTAINERTYPE");
				auditUtilityFunction.fnInsertAuditAction(savedContainerTypeList, 1, null, multilingualIDList, userInfo);
				return getContainerType(containerType.getNsitecode());
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
