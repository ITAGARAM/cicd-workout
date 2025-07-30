package com.agaramtech.qualis.storagemanagement.service.samplestoragemapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.basemaster.model.SeqNoBasemaster;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.SampleStorageCommons;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageMapping;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class SampleStorageMappingDAOImpl implements SampleStorageMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageCommons sampleStorageCommons;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		String sQuery = "";
		final int nstoragecategorycode = inputMap.containsKey("nstoragecategorycode")
				? (int) inputMap.get("nstoragecategorycode")
				: 0;
		final int nsamplestoragelocationcode = inputMap.containsKey("nsamplestoragelocationcode")
				? (int) inputMap.get("nsamplestoragelocationcode")
				: 0;
		final int nsamplestorageversioncode = inputMap.containsKey("nsamplestorageversioncode")
				? (int) inputMap.get("nsamplestorageversioncode")
				: 0;

		final List<StorageCategory> filterStorageCategory = sampleStorageCommons.getStorageCategory(userInfo).getBody();
		if (filterStorageCategory != null && filterStorageCategory.size() > 0) {
			outputMap.put("filterStorageCategory", filterStorageCategory);
			if (nstoragecategorycode == 0) {
				sQuery = "select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode="
						+ userInfo.getNtranssitecode() + " and ssl.nstoragecategorycode = "
						+ filterStorageCategory.get(0).getNstoragecategorycode()
						+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
						+ " samplestorageversion where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ ") and ts.ntranscode=ssl.nmappingtranscode "
						+ "   order by ssl.nsamplestoragelocationcode desc ";

				outputMap.put("selectedStorageCategory", filterStorageCategory.get(0));
				outputMap.put("selectedStorageCategoryName", filterStorageCategory.get(0).getSstoragecategoryname());
				outputMap.put("nfilterStorageCategory", filterStorageCategory.get(0).getNstoragecategorycode());
			} else {
				sQuery = "select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "' as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode="
						+ userInfo.getNtranssitecode() + " and ssl.nstoragecategorycode = " + nstoragecategorycode
						+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
						+ " samplestorageversion where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ ") and ts.ntranscode=ssl.nmappingtranscode "
						+ "   order by ssl.nsamplestoragelocationcode desc ";

				final Optional<StorageCategory> storageCategory = filterStorageCategory.stream()
						.filter(field -> field.getNstoragecategorycode() == nstoragecategorycode).findAny();
				if (storageCategory.isPresent()) {
					outputMap.put("selectedStorageCategory", storageCategory.get());
					outputMap.put("selectedStorageCategoryName", storageCategory.get().getSstoragecategoryname());
					outputMap.put("nfilterStorageCategory", nstoragecategorycode);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_STORAGECATEGORYALREADYDELETED", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			final List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);

			outputMap.put("sampleStorageLocation", list);
			if (list.size() > 0) {
				int nlocationCode = 0;
				if (nsamplestoragelocationcode == 0) {
					nlocationCode = (int) list.get(0).get("nsamplestoragelocationcode");
				} else {
					nlocationCode = nsamplestoragelocationcode;
				}
				outputMap.putAll((Map<String, Object>) sampleStorageCommons
						.getSelectedSampleStorageStructure(nlocationCode, nsamplestorageversioncode, userInfo)
						.getBody());
				outputMap.put("nsamplestoragelocationcode", nlocationCode);
				outputMap.putAll(getSampleStorageMappingData(outputMap, userInfo).getBody());
			} else {
				outputMap.put("sampleStorageVersion", null);
				outputMap.put("selectedSampleStorageLocation", null);
				outputMap.put("sampleStoragemapping", new ArrayList<>());
			}
		} else {
			outputMap.put("filterStorageCategory", null);
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	public ResponseEntity<Map<String, Object>> getSampleStorageMappingData(final Map<String, Object> inputMap,
			final UserInfo userInfo) {
		final Map<String, Object> outputMap = new HashMap<>();
		String concatStr = "";
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		if (nsamplestoragelocationcode != 0) {
			concatStr = " and ssp.nsamplestoragelocationcode=" + nsamplestoragelocationcode;
		}
		final String strQuery = "SELECT  u.sunitname,ssm.nquantity ,ssm.nsamplestoragemappingcode, ssm.nsamplestoragecontainerpathcode, ssm.ncontainertypecode, ssm.ncontainerstructurecode, ssm.nneedposition, ssm.nnoofcontainer,"
				+ " ssm.nunitcode, ssm.ndirectionmastercode, ssm.nprojecttypecode, ssm.nproductcode, ssm.nrow, ssm.ncolumn, ssm.sboxid,"
				+ " ssm.dmodifieddate, ssm.nsitecode, ssm.nstatus , ssp.jsondata->>'scontainerpath' as scontainerpath,p.sproductname,pt.sprojecttypename ,cts.scontainerstructurename,ct.scontainertype "
				+ " , ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus ,ssm.nrow as nrow,ssm.ncolumn as ncolumn ,u.sunitname from   samplestoragemapping ssm,"
				+ " samplestoragecontainerpath ssp,product p,containertype ct , "
				+ "	   containerstructure cts ,transactionstatus ts,  projecttype pt , samplestorageversion sv ,unit u where ssm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ssm.nsitecode="	+ userInfo.getNtranssitecode() 
				+ " and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and cts.nsitecode=" + userInfo.getNmastersitecode() 
				+ " and ct.nsitecode=" + userInfo.getNmastersitecode() 
				+ " and u.nsitecode=" + userInfo.getNmastersitecode()
				+ " and pt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ssp.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
				+ " and ssm.nproductcode=p.nproductcode and ssm.nprojecttypecode=pt.nprojecttypecode "
				+ " and u.nunitcode=ssm.nunitcode and ts.ntranscode=ssm.nneedposition "
				+ " and cts.ncontainerstructurecode=ssm.ncontainerstructurecode "
				+ " and ssm.ncontainertypecode=ct.ncontainertypecode " + concatStr
				+ " and ssp.nsamplestorageversioncode=sv.nsamplestorageversioncode "
				+ "	and ssp.nsamplestoragelocationcode=sv.nsamplestoragelocationcode " + "	and sv.napprovalstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " order by 1 desc; ";
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStoragemapping", list);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStoragemapping", list.get(0));
		}
		LOGGER.info("getSampleStorageMappingData:" + strQuery);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		if (!inputMap.containsKey("nalertvalidation")) {
			final int nmappingtranscode = jdbcTemplate.queryForObject(
					"select nmappingtranscode from samplestoragelocation where nsamplestoragelocationcode="
							+ nsamplestoragelocationcode,
					Short.class);
			if (nmappingtranscode == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		String strQuery = "select jsondata->>'scontainerpath' as scontainerpath,* from samplestoragecontainerpath where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestoragelocationcode="
				+ nsamplestoragelocationcode + " and nsamplestorageversioncode in (select nsamplestorageversioncode"
				+ " from samplestorageversion where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus  = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsamplestoragelocationcode="
				+ nsamplestoragelocationcode + ") "
				+ " and nsamplestoragecontainerpathcode not in (select nsamplestoragecontainerpathcode from samplestoragemapping where"
				+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ")";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("samplestoragecontainerpath", list);
		final String containertypeQuery = "select c.* from containertype c , containerstructure cts where "
				+ " c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode = "
				+ userInfo.getNmastersitecode() + " "
				+ " and cts.ncontainertypecode=c.ncontainertypecode  group by c.ncontainertypecode order by c.ncontainertypecode";
		final List<ContainerType> lstcontainerType = jdbcTemplate.query(containertypeQuery, new ContainerType());
		outputMap.put("containerType", lstcontainerType);
		final String directionmasterQuery = "select jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdirection,* from directionmaster where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<Map<String, Object>> directionmasterlist = jdbcTemplate.queryForList(directionmasterQuery);
		outputMap.put("directionmaster", directionmasterlist);
		if (lstcontainerType.size() > 0) {
			outputMap.put("ncontainertypecode", lstcontainerType.get(0).getNcontainertypecode());
			outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		} else {
			outputMap.put("ncontainertypecode", new HashMap<String, Object>());
			outputMap.put("containerStructure", new ArrayList<>());
		}
		strQuery = " select sl.*,p.*,pt.* from samplestoragelocation sl,product p, projecttype pt "
				+ " where sl.nsamplestoragelocationcode= " + nsamplestoragelocationcode
				+ " and sl.nproductcode=p.nproductcode and sl.nprojecttypecode=pt.nprojecttypecode "
				+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sl.nsitecode="
				+ userInfo.getNtranssitecode() + " and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and pt.nsitecode=" + userInfo.getNmastersitecode();
		Map<String, Object> object = new HashMap<String, Object>();
		try {
			object = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			object = new HashMap<String, Object>();
		}
		outputMap.put("samplestoragelocation", object);
		strQuery = " select b.* ,a.nsamplestoragelocationcode,a.nsamplestorageversioncode,"
				+ " case when a.nversionno = 0 then '-' else cast(a.nversionno as character varying(10)) end nversionno,"
				+ " a.napprovalstatus,a.jsondata, a.nstatus,b.ssamplestoragelocationname,b.nstoragecategorycode "
				+ " from samplestorageversion a,samplestoragelocation b, containertype ct,containerstructure cs "
				+ " where a.nsamplestoragelocationcode = b.nsamplestoragelocationcode"
				+ " and a.nsamplestorageversioncode =(select a1.nsamplestorageversioncode from samplestorageversion a1,samplestoragelocation b1"
				+ " where a1.nsamplestoragelocationcode=b1.nsamplestoragelocationcode and"
				+ " b1.nsamplestoragelocationcode=" + nsamplestoragelocationcode + " and a1.napprovalstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and a1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ) "
				+ " and b.ncontainertypecode=ct.ncontainertypecode and "
				+ " b.ncontainerstructurecode=cs.ncontainerstructurecode and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ct.nsitecode = " + userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageVersion", list.get(0));
			outputMap.put("ncontainertypecode", list.get(0).get("ncontainertypecode"));
			outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
		final String strQuery = "  select dm.jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdirection,  ssc.jsondata->>'scontainerpath' as scontainerpath, ct.ncontainertypecode, ct.scontainertype, ssm.nrow, ssm.ncolumn, "
				+ " cs.scontainerstructurename, p.nproductcode, pt.nprojecttypecode, p.sproductname, pt.sprojecttypename, ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus, ssm.* , u.sunitname  from  "
				+ " samplestoragemapping ssm,  samplestoragecontainerpath ssc, containertype ct, "
				+ " containerstructure cs, product p, transactionstatus ts, directionmaster dm, projecttype pt,unit u where "
				+ " ssm.nsamplestoragecontainerpathcode=ssc.nsamplestoragecontainerpathcode and "
				+ " ssm.ncontainertypecode=ct.ncontainertypecode and "
				+ " dm.ndirectionmastercode=ssm.ndirectionmastercode and "
				+ " ssm.ncontainerstructurecode=cs.ncontainerstructurecode and  "
				+ " ssm.nproductcode=p.nproductcode and ssm.nprojecttypecode=pt.nprojecttypecode and "
				+ " ssm.nneedposition=ts.ntranscode and ssm.nunitcode=u.nunitcode and "
				+ " ssm.nsamplestoragemappingcode=" + nsamplestoragemappingcode + " and ssm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		// ALPD-4934--Vignesh R(24-09-2024)
		try {
			final Map<String, Object> object = jdbcTemplate.queryForMap(strQuery);
			outputMap.put("editsampleStorageMapping", object);
			outputMap.put("ncontainertypecode", object.get("ncontainertypecode"));
			outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
			return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of available containerstructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 ncontainertypecode: 2
	 * @return response entity object holding response status and list of all
	 *         containerstructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = " select cts.*,ct.* from   containerstructure cts, containertype ct where  ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cts.ncontainertypecode= ct.ncontainertypecode and ct.ncontainertypecode="
				+ inputMap.get("ncontainertypecode") + " and ct.nsitecode=" + userInfo.getNmastersitecode()
				+ " and cts.nsitecode=" + userInfo.getNmastersitecode();
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestoragemappingcode:3}
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		outputMap.putAll((Map<String, Object>) sampleStorageCommons
				.getSelectedSampleStorageStructure(nsamplestoragelocationcode, 0, userInfo).getBody());
		outputMap.put("nsamplestoragelocationcode", nsamplestoragelocationcode);
		outputMap.putAll(getSampleStorageMappingData(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to create Partition  SampleStorageMapping table based on nprojecttypecode.
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 */
	public void createpartition(final Map<String, Object> inputMap) {
		jdbcTemplate.execute(" CREATE TABLE IF NOT EXISTS  samplestoragemapping_"
				+ ((int) inputMap.get("nprojecttypecode") == -1 ? 0 : inputMap.get("nprojecttypecode"))
				+ " PARTITION OF samplestoragemapping  " + "  FOR VALUES IN (" + inputMap.get("nprojecttypecode")
				+ ");");
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : 
	 *                 {containerpathsize: 1,ncolumn: 10,ncontainerstructurecode: 1,ncontainertypecode: 1,ndirectionmastercode: 1
	 *                 ,nneedposition: 3,nnoofcontainer: 100,nproductcode: 6,nprojecttypecode: 1,nquantity: 0,nrow: 10,
	 *                 nsamplestoragecontainerpathcode: "[4]",nsamplestoragelocationcode: 3,nsamplestoragemappingcode: 0,
	 *                 nunitcode: -1,sboxid: "",ssamplestoragemappingname: "-"}
	 *                 "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null}
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> createSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int ncontainertypecode = (int) inputMap.get("ncontainertypecode");
		final int nproductcode = (int) inputMap.get("nproductcode");
		final int nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		final int containerpathsize = (int) inputMap.get("containerpathsize");
		final int nneedposition = (int) inputMap.get("nneedposition");
		final int ncontainerstructurecode = (int) inputMap.get("ncontainerstructurecode");
		final int ndirectionmastercode = (int) inputMap.get("ndirectionmastercode");
		final int nunitcode = (int) inputMap.get("nunitcode");
		final int nnoofcontainer = (int) inputMap.get("nnoofcontainer") == 0 ? 1 : (int) inputMap.get("nnoofcontainer");
		final String sboxid = (String) inputMap.get("sboxid");
		final int nsitecode = inputMap.containsKey("nsitecode") ? (short) inputMap.get("nsitecode") : -1;
		int nrow;
		int ncolumn;
		if (nnoofcontainer == 1) {
			nrow = 1;
			ncolumn = 1;
		} else {
			nrow = (int) inputMap.get("nrow");
			ncolumn = (inputMap.get("ncolumn") instanceof String) ? Integer.parseInt((String) inputMap.get("ncolumn"))
					: (int) inputMap.get("ncolumn");
		}
		final JSONArray nsamplestoragecontainerpathcode = new JSONArray(
				inputMap.get("nsamplestoragecontainerpathcode").toString());
		createpartition(inputMap);
		String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragemapping' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq, SeqNoBasemaster.class,
				jdbcTemplate);
		final int nsamplestoragemappingcode = objseq.getNsequenceno() + 1;
		String insertQuery = " insert into samplestoragemapping  select generate_series(" + nsamplestoragemappingcode
				+ "," + (objseq.getNsequenceno() + containerpathsize)
				+ " ) as  nsamplestoragemappingcode, (json_array_elements('" + nsamplestoragecontainerpathcode
				+ "')#>>'{}')::INT " + "  AS nsamplestoragecontainerpathcode " + " ," + ncontainertypecode
				+ " AS ncontainertypecode, " + ncontainerstructurecode + "  as ncontainerstructurecode,"
				+ +nneedposition + " AS nneedposition," + inputMap.get("nquantity") + " as nquantity" + ", "
				+ nnoofcontainer + " as nnoofcontainer," + nunitcode + " as nunitcode," + ndirectionmastercode
				+ " as ndirectionmastercode," + nprojecttypecode + " as nprojecttypecode," + nproductcode
				+ " as nproductcode," + nrow + " as nrow," + ncolumn + " as ncolumn," + "'" + sboxid
				+ "' as sboxid,   '" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "' AS dmodifieddate  ," + nsitecode + "  AS nsitecode," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AS nstatus ;";
		String updateQuery = " update seqnobasemaster set nsequenceno= (select max(nsamplestoragemappingcode) "
				+ " from samplestoragemapping) where stablename='samplestoragemapping';";
		jdbcTemplate.execute(insertQuery + updateQuery);
		if (inputMap.containsKey("isImport") && (boolean) inputMap.get("isImport")) {
			sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq, SeqNoBasemaster.class, jdbcTemplate);
			insertQuery = " insert into samplestoragetransaction select " + objseq.getNsequenceno()
					+ "+ROW_NUMBER () OVER (ORDER BY " + inputMap.get("nsamplestoragelocationcode")
					+ ") as nsamplestoragetransactioncode," + inputMap.get("nsamplestoragelocationcode")
					+ " as nsamplestoragelocationcode,"
					+ " (select nsamplestoragemappingcode from samplestoragemapping where nsamplestoragecontainerpathcode=(clientdata->>'nsamplestoragecontainerpathcode')::int) as  nsamplestoragemappingcode,"
					+ "  (clientdata->>'nprojecttypecode')::int   as  nprojecttypecode,"
					+ " (clientdata->>'tube_type')::int as  ncollectiontubetypecode, (clientdata->>'sample_type')::int as  nproductcode,"
					+ " (clientdata->>'donor_type')::int as  nsampledonorcode,  (clientdata->>'visit_no')::int as  nvisitnumberocde,"
					+ " (clientdata->>'participant_id')::text as  nparticipantcode,"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  as  npositionfilled,"
					+ " (clientdata->>'pv_code')::text  as  sposition,  (clientdata->>'barcode')::text  as  spositionvalue,"
					+ " JSONB_build_array((clientdata->>'pv_code')::text||' : '||(clientdata->>'barcode')::text,'Visit No :'||(select vn.svisitnumber from visitnumber vn where vn.nvisitnumbercode=(clientdata->>'visit_no')::int)::text, "
					+ "	 'Participant Id :'||(clientdata->>'participant_id')::text, "
					+ "	 'Project Type :'||(clientdata->>'study_code')::text, "
					+ "	 'Sample Type :'||(select sproductname from product   where  nproductcode=(clientdata->>'sample_type')::int)::text "
					+ "	  )       AS jsondata ," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'  as  dmodifieddate," + "  " + userInfo.getNtranssitecode() + " as  nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as  nstatus "
					+ "  from jsonb_array_elements('" + inputMap.get("clientData") + "'::jsonb) clientdata;";
			updateQuery = " update seqnobasemaster set nsequenceno= (select max(nsamplestoragetransactioncode) "
					+ " from samplestoragetransaction) where stablename='samplestoragetransaction';";

			jdbcTemplate.execute(insertQuery + updateQuery);
		}
		return getSampleStorageMappingData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                containerpathsize: 1,ncolumn: 10,ncontainerstructurecode: 1,ncontainertypecode: 1,ndirectionmastercode: 1
	 *                 ,nneedposition: 3,nnoofcontainer: 100,nproductcode: 6,nprojecttypecode: 1,nquantity: 0,nrow: 10,
	 *                 nsamplestoragecontainerpathcode: "[4]",nsamplestoragelocationcode: 3,nsamplestoragemappingcode: 0,
	 *                 nunitcode: -1,sboxid: "",ssamplestoragemappingname: "-"}"userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String updateQuery = "";
		final int nnoofcontainer = (int) inputMap.get("nnoofcontainer") == 0 ? 1 : (int) inputMap.get("nnoofcontainer");
		int nrow;
		int ncolumn;
		if (nnoofcontainer == 1) {
			nrow = 1;
			ncolumn = 1;
		} else {
			nrow = (int) inputMap.get("nrow");
			ncolumn = (inputMap.get("ncolumn") instanceof String) ? Integer.parseInt((String) inputMap.get("ncolumn"))
					: (int) inputMap.get("ncolumn");
		}
		if (inputMap.containsKey("sheetUpdate")) {
			final String nsamplestoragemappingcode = (String) inputMap.get("nsamplestoragemappingcode");
			if ((boolean) inputMap.get("isMultiSampleAdd")) {
				updateQuery = " UPDATE samplestoragemapping s " + " SET jsondata=('"
						+ stringUtilityFunction.replaceQuote(inputMap.get("sheetData").toString())
						+ "'::jsonb->>subquery.nsamplestoragemappingcode::text)::jsonb  "
						+ " FROM (select nsamplestoragemappingcode  from samplestoragemapping where nsamplestoragemappingcode in ("
						+ nsamplestoragemappingcode
						+ ") ) AS subquery wHERE s.nsamplestoragemappingcode=subquery.nsamplestoragemappingcode;";
			} else {
				updateQuery = " UPDATE public.samplestoragemapping SET   jsondata='"
						+ stringUtilityFunction.replaceQuote(inputMap.get("sheetData").toString())
						+ "' 	WHERE nsamplestoragemappingcode=" + nsamplestoragemappingcode + ";";
			}
		} else {
			createpartition(inputMap);
			final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
			updateQuery = " UPDATE public.samplestoragemapping SET   nsamplestoragecontainerpathcode="
					+ inputMap.get("nsamplestoragecontainerpathcode") + ",ndirectionmastercode="
					+ inputMap.get("ndirectionmastercode") + ", ncontainertypecode="
					+ inputMap.get("ncontainertypecode") + ", ncontainerstructurecode="
					+ inputMap.get("ncontainerstructurecode") + ", nrow=" + nrow + ", ncolumn=" + ncolumn
					+ ", nprojecttypecode=" + inputMap.get("nprojecttypecode") + ", nunitcode="
					+ inputMap.get("nunitcode") + ", nnoofcontainer=" + nnoofcontainer + ", nproductcode="
					+ inputMap.get("nproductcode") + ", nneedposition=" + inputMap.get("nneedposition") + ", nquantity="
					+ inputMap.get("nquantity") + ",   dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + "	WHERE nsamplestoragemappingcode="
					+ nsamplestoragemappingcode + ";";
		}
		jdbcTemplate.execute(updateQuery);
		return getSampleStorageMappingData(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *              nsamplestoragelocationcode: 3 ,nsamplestoragemappingcode : 5 ,userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<? extends Object> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int nmappingtranscode = jdbcTemplate
				.queryForObject("select nmappingtranscode from samplestoragelocation where nsamplestoragelocationcode="
						+ inputMap.get("nsamplestoragelocationcode"), Short.class);
		if (nmappingtranscode == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
		final String query = "select * from samplestoragemapping where nsamplestoragemappingcode="
				+ nsamplestoragemappingcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageMapping objSampleStorageMapping = (SampleStorageMapping) jdbcUtilityFunction
				.queryForObject(query, SampleStorageMapping.class, jdbcTemplate);
		if (objSampleStorageMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final String updateQuery = "  delete from samplestoragemapping WHERE nsamplestoragemappingcode="
				+ nsamplestoragemappingcode + ";";
		jdbcTemplate.execute(updateQuery);
		return getSampleStorageMappingData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *              nsamplestoragelocationcode: 3 ,nsamplestoragemappingcode : 5 ,userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 197,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Integer nsamplestoragelocationcodeint = (Integer) inputMap.get("nsamplestoragelocationcode");
		final String nsamplestoragemappingcode = inputMap.get("nsamplestoragemappingcode").toString();
		final Short nsamplestoragelocationcode = nsamplestoragelocationcodeint.shortValue();
		String updateQuery = "";
		final String strQuery = "  select * from  samplestoragemapping  " + " where nsamplestoragemappingcode in("
				+ nsamplestoragemappingcode + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(strQuery);
		if (listDataList.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTAPPROVEEMPTYMAPPING",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			SampleStorageStructure samplestoragelocation;
			final String strcheck = "select nmappingtranscode ,ssamplestoragelocationname,nstoragecategorycode from samplestoragelocation "
					+ "where nsamplestoragelocationcode=" + nsamplestoragelocationcode + " " + " and  nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			samplestoragelocation = (SampleStorageStructure) jdbcUtilityFunction.queryForObject(strcheck,
					SampleStorageStructure.class, jdbcTemplate);
			if (samplestoragelocation == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTEMPLATETOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (samplestoragelocation.getNmappingtranscode() == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else {
				updateQuery = "  update   samplestoragelocation set nmappingtranscode="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where  nsamplestoragelocationcode=" + nsamplestoragelocationcode + ";";
				jdbcTemplate.execute(updateQuery);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				samplestoragelocation.setNsamplestoragelocationcode(nsamplestoragelocationcode);
				samplestoragelocation
						.setNtransactionstatus(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
				savedTestList.add(samplestoragelocation);
				multilingualIDList.add("IDS_SAMPLESTORAGEMAPPING");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
				return getActiveSampleStorageMappingById(inputMap, userInfo);
			}
		}
	}

//	@Override
//	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception {
//		final Map<String, Object> outputMap = new HashMap<>();
//		final String nsamplestoragemappingcode = (String) inputMap.get("nsamplestoragemappingcode");
//		String strQuery = "";
//		if ((boolean) inputMap.get("isMultiSampleAdd")) {
//			strQuery = "select jsonb_object_agg(nsamplestoragemappingcode,jsondata)   from samplestoragemapping where "
//					+ " nsamplestoragemappingcode in (" + nsamplestoragemappingcode + ") ";
//		} else {
//			strQuery = "select jsondata  from samplestoragemapping where nsamplestoragemappingcode="
//					+ nsamplestoragemappingcode;
//		}
//		final String strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);
//		outputMap.put("sheetData", strMaterial);
//		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
//	}
//
//	@Override
//	public ResponseEntity<Object> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception {
//		String tableName = "";
//		String getJSONKeysQuery = "";
//		final Map<String, Object> returnObject = new HashMap<>();
//		final String sourceName = (String) inputMap.get("source");
//		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
//				: "";
//		if (conditionString.isEmpty()) {
//			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
//		}
//		final String scollate = "collate \"default\"";
//		if (conditionString.contains("LIKE")) {
//			while (conditionString.contains("LIKE")) {
//				final String sb = conditionString;
//				String sQuery = conditionString;
//				final int colanindex = sb.indexOf("LIKE '");
//				final String str1 = sQuery.substring(0, colanindex + 6);
//				sQuery = sQuery.substring(colanindex + 6);
//				final StringBuilder sb3 = new StringBuilder(str1);
//				final StringBuilder sb4 = new StringBuilder(sQuery);
//				sb3.replace(colanindex, colanindex + 4, "ilike");
//				System.out.println(sQuery);
//				final int indexofsv = sQuery.indexOf("'");
//				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
//				conditionString = sb3.toString() + sb4.toString();
//			}
//		}
//		tableName = sourceName.toLowerCase();
//		final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
//				+ tableName + "' and data_type = 'jsonb'";
//		String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
//		jsonField = jsonField != null ? "||" + jsonField : "";
//		final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
//				+ tableName + "'";
//		final String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
//		if (fields.contains(inputMap.get("valuemember").toString())) {
//			getJSONKeysQuery = "select " + tableName + ".* from " + tableName + " where nstatus ="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
//					+ inputMap.get("valuemember") + "\" > '0' " + conditionString + " ;";
//		} else {
//			getJSONKeysQuery = "select  " + tableName + ".* from " + tableName + " where nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString + " ;";
//		}
//		final List<Map<String, Object>> data = jdbcTemplate.queryForList(getJSONKeysQuery);
//		returnObject.put((String) inputMap.get("label"), data);
//		return new ResponseEntity<>(returnObject, HttpStatus.OK);
//	}

}
