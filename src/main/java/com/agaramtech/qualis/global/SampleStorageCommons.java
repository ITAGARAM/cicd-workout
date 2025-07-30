package com.agaramtech.qualis.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.ContainerStorageCondition;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageVersion;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleStorageCommons {

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	public ResponseEntity<List<StorageCategory>> getStorageCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = " select nstoragecategorycode,sstoragecategoryname,sdescription,nsitecode,nstatus  from storagecategory "
				+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstoragecategorycode>0  and nsitecode=" + userInfo.getNmastersitecode();
		return new ResponseEntity<List<StorageCategory>>(jdbcTemplate.query(strQuery, new StorageCategory()),
				HttpStatus.OK);
	}

	public StorageCategory getActiveStorageCategoryById(final int nstorageCategoryCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select nstoragecategorycode,sstoragecategoryname,sdescription,nsitecode,nstatus "
				+ " from storagecategory  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstoragecategorycode = "
				+ nstorageCategoryCode;
		return (StorageCategory) jdbcUtilityFunction.queryForObject(strQuery, StorageCategory.class, jdbcTemplate);
	}

	public ResponseEntity<Map<String, Object>> getContainerStorageCondition(final String scontainercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = "select a.sstorageconditionname, a.nstorageconditioncode,b.ncontainerstoragecode, b.scontainercode from storagecondition a, containerstoragecondition b where a.nstorageconditioncode = b.nstorageconditioncode and b.scontainercode = '"
				+ scontainercode + "' and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ContainerStorageCondition obj = (ContainerStorageCondition) jdbcUtilityFunction.queryForObject(strQuery,
				ContainerStorageCondition.class, jdbcTemplate);
		outputMap.put("storageContainer", obj);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<? extends Object> getSelectedSampleStorageStructure(final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		SampleStorageStructure deleteSampleStorageLocation = getActiveLocation((short) nsampleStorageLocationCode);

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = "select ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sneedautomapping,sc.sstoragecategoryname,p.sproductname,pt.sprojecttypename,d.jsondata->'sdirection'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' as sdirection,ssl.nrow,ssl.ncolumn, ct.scontainertype,cs.scontainerstructurename,ssl.*,ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,u.sunitname "
				+ " from storagecategory sc,samplestoragelocation ssl, transactionstatus ts,transactionstatus ts1, product p,"
				+ "containertype ct,containerstructure cs, projecttype pt,directionmaster d,unit u where ssl.nsamplestoragelocationcode = "
				+ nsampleStorageLocationCode + " and ts.ntranscode=ssl.nmappingtranscode "
				+ " and ts1.ntranscode=ssl.nneedautomapping and p.nproductcode=ssl.nproductcode "
				+ " and ssl.nstoragecategorycode=sc.nstoragecategorycode "
				+ " and pt.nprojecttypecode=ssl.nprojecttypecode "
				+ " and cs.ncontainerstructurecode=ssl.ncontainerstructurecode "
				+ " and ct.ncontainertypecode=ssl.ncontainertypecode "
				+ " and d.ndirectionmastercode=ssl.ndirectionmastercode " + " and u.nunitcode=ssl.nunitcode "
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageLocation", list.get(0));
			outputMap.putAll((Map<String, Object>) getAllSampleStorageVersion(nsampleStorageLocationCode,
					nsamplestorageversioncode, userInfo).getBody());
		} else {
			outputMap.put("selectedSampleStorageLocation", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public ResponseEntity<? extends Object> getActiveSampleStorageVersion(final int nsampleStorageVersionCode,
			final UserInfo userInfo) throws Exception {
		final SampleStorageVersion deletedSampleStorageVersion = getActiveVersion((short) nsampleStorageVersionCode);
		if (deletedSampleStorageVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final Map<String, Object> outputMap = new HashMap<>();
		// ATE234 Janakumar BGSI-10->Storage Structure ? Instrument id & Site combo load based on Setting table.
		final String sQuery = " select (a.jsondata->'additionalinfo'->>'nprojecttypecode')::integer as nprojecttypecode,(a.jsondata->'additionalinfo'->>'nunitcode')::integer as nunitcode,a.nsamplestoragelocationcode,a.nsamplestorageversioncode,"
				+ " (a.jsondata->'additionalinfo'->>'ncolumn')::integer as ncolumn,(a.jsondata->'additionalinfo'->>'nrow')::integer as nrow, case when a.nversionno = 0 then '-' else cast(a.nversionno as character varying(10)) end nversionno,"
				+ " (a.jsondata->'additionalinfo'->>'nquantity') as nquantity,(a.jsondata->'additionalinfo'->>'nproductcode')::integer as nproductcode,a.napprovalstatus,a.jsondata, a.nstatus,b.ssamplestoragelocationname,b.nstoragecategorycode, "
				+ " (a.jsondata->'additionalinfo'->>'nneedposition')::integer as nneedposition,(a.jsondata->'additionalinfo'->>'nnoofcontainer')::integer as nnoofcontainer,(a.jsondata->'additionalinfo'->>'nneedautomapping')::integer as nneedautomapping,(a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode, "
				+ " (a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode,(a.jsondata->'additionalinfo'->>'ndirectionmastercode')::integer as ndirectionmastercode, "
				+ " (a.jsondata->'additionalinfo'->>'nstoragecategorycode')::integer as nstoragecategorycode,(a.jsondata->'additionalinfo'->>'ncontainerstructurecode')::integer as ncontainerstructurecode,"
				+ "  (a.jsondata->'additionalinfo'->>'ssamplestoragelocationname') as ssamplestoragelocationname,"
				+ "  (a.jsondata->'additionalinfo'->>'sproductname') as sproductname, "
				+ "  (a.jsondata->'additionalinfo'->>'sdirection') as sdirection, "
				+ "  (a.jsondata->'additionalinfo'->>'scontainertype') as scontainertype, "
				+ "  (a.jsondata->'additionalinfo'->>'sprojecttypename') as sprojecttypename, "
				+ "  (a.jsondata->'additionalinfo'->>'scontainerstructurename') as scontainerstructurename, "
				+ "  (a.jsondata->'additionalinfo'->>'sstoragecategoryname') as sstoragecategoryname, "
				+ "  (a.jsondata->'additionalinfo'->>'sunitname') as sunitname,"
				+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' as sneedautomapping,"
				+ " a.napprovalstatus,b.nsitecode "
				+ " from samplestorageversion a left join transactionstatus ts on   ts.ntranscode=(a.jsondata->'additionalinfo'->>'nneedautomapping')::integer and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  ,samplestoragelocation b, containertype ct,containerstructure cs "
				+ "  where a.nsamplestoragelocationcode = b.nsamplestoragelocationcode"
				+ " and a.nsamplestorageversioncode = " + nsampleStorageVersionCode
				+ " and b.ncontainertypecode=ct.ncontainertypecode   "
				+ " and b.ncontainerstructurecode=cs.ncontainerstructurecode " + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageVersion", list.get(0));
		} else {
			outputMap.put("selectedSampleStorageVersion", null);
		}
		outputMap.put("storageContainer", null);
		outputMap.put("containers", null);
		outputMap.put("sampleStorageMaster", null);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Map<String, Object>> getAllSampleStorageVersion(final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = "select nsamplestoragelocationcode, nsamplestorageversioncode, case when nversionno = 0 "
				+ "then '-' else cast(nversionno as character varying(10)) end nversionno, napprovalstatus, jsondata, nstatus "
				+ "from samplestorageversion where nsamplestoragelocationcode = " + nsampleStorageLocationCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nsamplestorageversioncode asc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("sampleStorageVersion", list);
			if (nsamplestorageversioncode == 0) {
				outputMap.putAll((Map<String, Object>) getActiveSampleStorageVersion(
						(int) list.get(list.size() - 1).get("nsamplestorageversioncode"), userInfo).getBody());
			} else {
				outputMap
						.putAll((Map<String, Object>) getActiveSampleStorageVersion(nsamplestorageversioncode, userInfo)
								.getBody());
			}

		} else {
			outputMap.put("sampleStorageVersion", null);
			outputMap.put("selectedSampleStorageVersion", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public SampleStorageStructure getActiveLocation(final short sampleStorageLocationCode) throws Exception {
		final String insertQueryString = "select *  from samplestoragelocation where nsamplestoragelocationcode = "
				+ sampleStorageLocationCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageStructure sampleStorageLocation = (SampleStorageStructure) jdbcUtilityFunction
				.queryForObject(insertQueryString, SampleStorageStructure.class, jdbcTemplate);
		return sampleStorageLocation;
	}

	public SampleStorageVersion getActiveVersion(final short nsamplestorageversioncode) throws Exception {
		final String insertQueryString = "select *  from samplestorageversion where nsamplestorageversioncode = "
				+ nsamplestorageversioncode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageVersion sampleStorageVersion = (SampleStorageVersion) jdbcUtilityFunction
				.queryForObject(insertQueryString, SampleStorageVersion.class, jdbcTemplate);
		return sampleStorageVersion;
	}

}
