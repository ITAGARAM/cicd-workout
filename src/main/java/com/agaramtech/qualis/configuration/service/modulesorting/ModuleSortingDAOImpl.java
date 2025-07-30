package com.agaramtech.qualis.configuration.service.modulesorting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ModuleSortingDAOImpl implements ModuleSortingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleSortingDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getModuleSorting(UserInfo userInfo) throws Exception {
		final String strFormsQuery1 = "select qf.nformcode, qm.nmodulecode,qm.smodulename, qm.nsorter, qf.sformname,qf.nsorter,"
				+ "coalesce(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', qf.jsondata->'sdisplayname'->>'en-US') as sformdisplayname,"
				+ " q.smenuname, q.nmenucode, coalesce(qm.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', qm.jsondata->'sdisplayname'->>'en-US') as smoduledisplayname "
				+ " from qualisforms qf, qualismodule qm, qualismenu q where qf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and qf.nmodulecode>0 and qf.nmenucode>0 and qm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qm.nmodulecode>0 and qf.nmodulecode=qm.nmodulecode and q.nmenucode=qm.nmenucode and q.nmenucode = qf.nmenucode and "
				+ " qm.nmenucode=qf.nmenucode and q.nmenucode>0 order by qf.nsorter ";
		LOGGER.info("getModuleSorting:" + strFormsQuery1);
		return new ResponseEntity<>(jdbcTemplate.query(strFormsQuery1, new QualisForms()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveModuleSortingById(UserInfo userInfo, Integer nformCode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strFormsQuery = "select qf.nmodulecode, qf.nmenucode, qf.nformcode, qf.sformname, qf.nstatus, qm.nmodulecode, qm.smodulename,"
				+ "coalesce(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', qf.jsondata->'sdisplayname'->>'en-US') as sformdisplayname,"
				+ " coalesce(qm.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "', qm.jsondata->'sdisplayname'->>'en-US') as smoduledisplayname, "
				+ " qm.nsorter, q.smenuname, q.nmenucode from qualisforms qf, qualismodule qm, qualismenu q where qf.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qf.nmodulecode=qm.nmodulecode and "
				+ " q.nmenucode=qm.nmenucode and q.nmenucode=qf.nmenucode and qm.nmenucode=qf.nmenucode and qf.nformcode ="
				+ nformCode + " and qm.nmodulecode>0 and q.nmenucode>0 order by qf.nformcode";
		List<QualisForms> lstQualisForms1 = jdbcTemplate.query(strFormsQuery, new QualisForms());
		outputMap.put("selectedForms", lstQualisForms1);
		final String moduleList = "select nmodulecode, nmenucode, smodulename, "
				+ "coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as smoduledisplayname" + " from qualismodule where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nmodulecode>0 order by nmenucode, nsorter";
		List<QualisForms> moduleList1 = jdbcTemplate.query(moduleList, new QualisForms());
		outputMap.put("moduleList", moduleList1);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateModuleSorting(UserInfo userInfo, Integer nformCode, Integer nmoduleCode,
			Integer nmenuCode) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final ResponseEntity<Map<String,Object>> moduleSortingLstBfrUpdate = (ResponseEntity<Map<String,Object>>) getActiveModuleSortingById(
				userInfo, nformCode);
		Map<String, Object> moduleSortingListBfrUpdate = (Map<String, Object>) moduleSortingLstBfrUpdate.getBody();
		List<QualisForms> lstBfrUpdate = (List<QualisForms>) moduleSortingListBfrUpdate.get("selectedForms");
		final String updateQuery1 = "update qualisforms set nmodulecode =" + nmoduleCode + ", "
				+ " nsorter = (select max(nsorter)+1 from qualisforms where nmodulecode=" + nmoduleCode + ")"
				+ ", nmenucode =" + nmenuCode + ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "' where nformcode =" + nformCode;
		jdbcTemplate.execute(updateQuery1);
		final String updateQuery2 = "update sitequalisforms set nsorter = (select max(nsorter) from qualisforms"
				+ " where nmodulecode=" + nmoduleCode + "), dmodifieddate ='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where " + " nformcode =" + nformCode;
		jdbcTemplate.execute(updateQuery2);
		final String updateQueryTableDetails = "update querytabledetails set nmodulecode =" + nmoduleCode
				+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where " + " nformcode ="
				+ nformCode;
		jdbcTemplate.execute(updateQueryTableDetails);
		final ResponseEntity<Map<String,Object>> moduleSortingLstAftrUpdate = (ResponseEntity<Map<String,Object>>) getActiveModuleSortingById(
				userInfo, nformCode);
		Map<String, Object> moduleSortingListAftrUpdate = (Map<String, Object>) moduleSortingLstAftrUpdate.getBody();
		List<QualisForms> lstAftrUpdate = (List<QualisForms>) moduleSortingListAftrUpdate.get("selectedForms");
		listBeforeUpdate.add(lstBfrUpdate.get(0));
		listAfterUpdate.add(lstAftrUpdate.get(0));
		multilingualIDList.add("IDS_EDITFORMTRANSFER");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);
		return getModuleSorting(userInfo);
	}

}
