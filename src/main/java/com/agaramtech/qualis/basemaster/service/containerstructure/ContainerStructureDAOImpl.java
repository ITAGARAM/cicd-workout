package com.agaramtech.qualis.basemaster.service.containerstructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.ContainerStructure;
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
 * This class is used to perform CRUD Operation on "containerstructure" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class ContainerStructureDAOImpl implements ContainerStructureDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerStructureDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active containerstructure for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         containerstructure
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getContainerStructure(UserInfo userInfo) throws Exception {
		final String strQuery = "select cs.ncontainerstructurecode,cs.ncontainertypecode,cs.scontainerstructurename,cs.nrow,cs.ncolumn,cs.nsitecode,cs.nstatus,ct.scontainertype"
				+ " from containerstructure cs,containertype ct where cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cs.ncontainerstructurecode>0 and cs.ncontainertypecode = ct.ncontainertypecode and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ct.nsitecode = "
				+ userInfo.getNmastersitecode() + " and cs.nsitecode = " + userInfo.getNmastersitecode()
				+ "    and cs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " order by cs.ncontainerstructurecode desc";
		LOGGER.info("getContainerStructure() called");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ContainerStructure()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to containerstructure table. Need to
	 * check for duplicate entry of Container Structure name for the specified site
	 * before saving into database.
	 * 
	 * @param objSampleDonor [ContainerStructure] object holding details to be added
	 *                       in containerstructure table
	 * @return inserted ContainerStructure object with HTTP Status.
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createContainerStructure(ContainerStructure objContainerStructure, UserInfo userInfo)
			throws Exception {
		final ContainerStructure containerstructure = getContainerStructureName(objContainerStructure, userInfo);
		if (containerstructure.isIsduplicatename() == false) {
			if (objContainerStructure.getNrow() > 0 && objContainerStructure.getNcolumn() > 0) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedobjContainerStructureList = new ArrayList<>();
				final String sQuery = " lock  table containerstructure "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);
				String seqno = "select nsequenceno from seqnobasemaster where stablename='containerstructure' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = jdbcTemplate.queryForObject(seqno, Integer.class);
				nsequenceno++;
				final String insertquery = "insert into containerstructure (ncontainerstructurecode ,ncontainertypecode,scontainerstructurename,nrow,ncolumn,dmodifieddate,nsitecode,nstatus) "
						+ "values(" + nsequenceno + "," + objContainerStructure.getNcontainertypecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objContainerStructure.getScontainerstructurename()) + "',"
						+ objContainerStructure.getNrow() + ", " + objContainerStructure.getNcolumn() + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " " + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);
				String updatequery = "update seqnobasemaster set nsequenceno =" + nsequenceno
						+ " where stablename='containerstructure' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updatequery);
				objContainerStructure.setNcontainerstructurecode(nsequenceno);
				savedobjContainerStructureList.add(objContainerStructure);
				multilingualIDList.add("IDS_ADDCONTAINERSTRUCTURE");
				auditUtilityFunction.fnInsertAuditAction(savedobjContainerStructureList, 1, null, multilingualIDList,
						userInfo);
				return getContainerStructure(userInfo);
			} else {
				if (objContainerStructure.getNrow() == 0) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ROWGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COLUMNGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			String alert = "";
			alert = commonFunction.getMultilingualMessage("IDS_CONTAINERSTRUCTURENAME",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(alert + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve active ContainerStructure object based on the
	 * specified ncontainerstructurecode.
	 * 
	 * @param ncontainerstructurecode [int] primary key of ContainerStructure object
	 * @return response entity object holding response status and data of
	 *         containerstructure object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ContainerStructure getActiveContainerStructureById(final int nsampledonorcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select cs.ncontainerstructurecode,cs.ncontainertypecode,cs.scontainerstructurename,cs.nrow,cs.ncolumn,cs.dmodifieddate,cs.nsitecode,cs.nstatus,ct.scontainertype,ct.ncontainertypecode from containerstructure cs,containertype ct where ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cs.ncontainertypecode= ct.ncontainertypecode  and  cs.ncontainerstructurecode = "
				+ nsampledonorcode;
		return (ContainerStructure) jdbcUtilityFunction.queryForObject(strQuery, ContainerStructure.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in containerstructure table. Need to
	 * validate that the containerstructure object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * containerstructure name and row and column for the specified site before
	 * saving into database.
	 * 
	 * @param objContainerStructure [ContainerStructure] object holding details to
	 *                              be updated in containerstructure table
	 * @return response entity object holding response status and data of updated
	 *         containerstructure object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateContainerStructure(ContainerStructure objContainerStructure, UserInfo userInfo)
			throws Exception {
		ContainerStructure containerstructure = getActiveContainerStructureById(
				objContainerStructure.getNcontainerstructurecode(), userInfo);
		if (containerstructure == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ContainerStructure containerstructureList = getContainerStructureName(objContainerStructure,
					userInfo);
			if (containerstructureList.isIsduplicatename() == false) {
				if (objContainerStructure.getNrow() > 0 && objContainerStructure.getNcolumn() > 0) {
					final String updateQueryString = "update containerstructure	 set scontainerstructurename='"
							+ stringUtilityFunction.replaceQuote(objContainerStructure.getScontainerstructurename())
							+ "',ncontainertypecode=" + objContainerStructure.getNcontainertypecode() + ", " + "nrow ="
							+ objContainerStructure.getNrow() + ",ncolumn=" + objContainerStructure.getNcolumn()
							+ ",  dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
							+ " where ncontainerstructurecode  =" + objContainerStructure.getNcontainerstructurecode();
					jdbcTemplate.execute(updateQueryString);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITCONTAINERSTRUCTURE");
					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(objContainerStructure);
					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(containerstructure);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getContainerStructure(userInfo);
				} else {
					if (objContainerStructure.getNrow() == 0) {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ROWGREATERZERO",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COLUMNGREATERZERO",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
			} else {
				String alert = "";
				alert = commonFunction.getMultilingualMessage("IDS_CONTAINERSTRUCTURENAME",
						userInfo.getSlanguagefilename());
				return new ResponseEntity<>(alert + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in containerstructure table.
	 * 
	 * @param objContainerStructure [ContainerStructure] an Object holds the record
	 *                              to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         containerstructure object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteContainerStructure(ContainerStructure objContainerStructure, UserInfo userInfo)
			throws Exception {
		final ContainerStructure containerstructureResponse = getActiveContainerStructureById(
				objContainerStructure.getNcontainerstructurecode(), userInfo);
		if (containerstructureResponse == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_SAMPLESTORAGESTRUCTURE' as Msg from samplestoragelocation where ncontainerstructurecode= "
					+ objContainerStructure.getNcontainerstructurecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping where ncontainerstructurecode= "
					+ objContainerStructure.getNcontainerstructurecode() + "	and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objContainerStructure.getNcontainerstructurecode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Object> deletedContainerStructureList = new ArrayList<>();
				final String updateQueryString = "update containerstructure set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ncontainerstructurecode="
						+ objContainerStructure.getNcontainerstructurecode();
				jdbcTemplate.execute(updateQueryString);
				objContainerStructure.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedContainerStructureList.add(objContainerStructure);
				auditUtilityFunction.fnInsertAuditAction(deletedContainerStructureList, 1, null,
						Arrays.asList("IDS_DELETECONTAINERSTRUCTURE"), userInfo);
				return getContainerStructure(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active containerstructure objects for the
	 * specified containerstructure name and row and column.
	 * 
	 * @param scontainerstrcuture [String] and code it to be checked tha duplicate
	 *                            in the containerstructure table.
	 * @param row                 and column is to be checked in specified
	 *                            containertype
	 * @param nmasterSiteCode     [int] site code of the unit
	 * @return list of active containerstructure code(s) based on the containertype
	 *         containerstructure name and row and column.
	 * @throws Exception
	 */

	private ContainerStructure getContainerStructureName(ContainerStructure objContainerStructure, UserInfo userInfo)
			throws Exception {
		String scontainerstructurename = "";
		if (objContainerStructure.getNcontainerstructurecode() != 0) {
			scontainerstructurename = "and ncontainerstructurecode<>"
					+ objContainerStructure.getNcontainerstructurecode() + "";
		}
		String strQuery = "SELECT"
				+ " CASE WHEN EXISTS (SELECT scontainerstructurename  FROM containerstructure WHERE scontainerstructurename=N'"
				+ stringUtilityFunction.replaceQuote(objContainerStructure.getScontainerstructurename())
				+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " " + scontainerstructurename + ") THEN true " + " ELSE false"
				+ " END AS isduplicatename";
		return (ContainerStructure) jdbcUtilityFunction.queryForObject(strQuery, ContainerStructure.class,
				jdbcTemplate);
	}
}
