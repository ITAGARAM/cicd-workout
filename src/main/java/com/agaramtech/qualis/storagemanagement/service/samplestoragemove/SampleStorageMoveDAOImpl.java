package com.agaramtech.qualis.storagemanagement.service.samplestoragemove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.SampleStorageCommons;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplestoragetransacation" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleStorageMoveDAOImpl implements SampleStorageMoveDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageMoveDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageCommons sampleStorageCommons;

	/**
	 * This method is used to retrieve the list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Map<String, Object>> getsamplestoragemove(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		LOGGER.info("getsamplestoragemove()");
		final Map<String, Object> outputMap = new HashMap<>();

		final String str = "select ssettingvalue from settings where nsettingcode=40 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class, jdbcTemplate);
		String conditonstr = "";
		if (Integer.parseInt(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			conditonstr = "  AND  ssl.nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		}

		String strQuery = " select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
				+ " samplestorageversion where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + ") and ssl.nsitecode="
				+ userInfo.getNtranssitecode() + " and ts.ntranscode=ssl.nmappingtranscode " + conditonstr
				+ " order by ssl.nsamplestoragelocationcode desc ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStorageLocation", list);

		strQuery = "select * from product where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleType", list);

		strQuery = "select * from containertype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerType", list);

		strQuery = "select * from containerstructure where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);

		strQuery = "select * from projecttype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("projectType", list);
		outputMap.put("selectedProjectType", list.get(0));
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);

	}

	/*
	@Override
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");

		final String strQuery = "select jsondata->>'scontainerpath' as scontainerpath,* from samplestoragecontainerpath where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nsamplestoragelocationcode=" + nsamplestoragelocationcode
				+ " and nsamplestorageversioncode in (select nsamplestorageversioncode from samplestorageversion where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus  = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsamplestoragelocationcode="
				+ nsamplestoragelocationcode + ") and nsamplestoragecontainerpathcode not in "
				+ "(select nsamplestoragecontainerpathcode from samplestoragemapping where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ")";
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("samplestoragecontainerpath", list);

		final String containertypeQuery = "select c.* from containertype c , containerstructure cts where "
				+ " c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode = "
				+ userInfo.getNmastersitecode() + " and cts.ncontainertypecode=c.ncontainertypecode "
				+ " group by c.ncontainertypecode order by c.ncontainertypecode";
		final List<ContainerType> lstcontainerType = jdbcTemplate.query(containertypeQuery, new ContainerType());

		outputMap.put("containerType", lstcontainerType);

		final String directionmasterQuery = "select jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdirection,* from directionmaster where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Map<String, Object>> directionmasterlist = jdbcTemplate.queryForList(directionmasterQuery);
		outputMap.put("directionmaster", directionmasterlist);

		if (lstcontainerType.size() > 0) {
			outputMap.put("ncontainertypecode", lstcontainerType.get(0).getNcontainertypecode());
			outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		} else {
			outputMap.put("ncontainertypecode", new HashMap<String, Object>());
			outputMap.put("containerStructure", new ArrayList<>());
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");

		final String strQuery = "  select dm.jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdirection, ssc.jsondata->>'scontainerpath' as scontainerpath, ,ct.ncontainertypecode ,ct.scontainertype,cs.nrow,cs.ncolumn, "
				+ " cs.scontainerstructurename, " + " p.sproductname,  " + " ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,   ssm.*   from  "
				+ " samplestoragemapping ssm,  samplestoragecontainerpath ssc,  containertype ct, "
				+ " containerstructure cs,  product p,   transactionstatus ts,directionmaster dm   where "
				+ " ssm.nsamplestoragecontainerpathcode=ssc.nsamplestoragecontainerpathcode and "
				+ " ssm.ncontainertypecode=ct.ncontainertypecode and "
				+ " dm.ndirectionmastercode=ssm.ndirectionmastercode and "
				+ " ssm.ncontainerstructurecode=cs.ncontainerstructurecode and  "
				+ " ssm.nproductcode=p.nproductcode and  ssm.nneedposition=ts.ntranscode and "
				+ " ssm.nsamplestoragemappingcode=" + nsamplestoragemappingcode + " and ssm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final Map<String, Object> object = jdbcTemplate.queryForMap(strQuery);
		outputMap.put("editsampleStorageMapping", object);
		outputMap.put("ncontainertypecode", object.get("ncontainertypecode"));
		outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = " select cts.*,ct.* from   containerstructure cts, containertype ct where  ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cts.ncontainertypecode= ct.ncontainertypecode and ct.ncontainertypecode="
				+ inputMap.get("ncontainertypecode");
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		outputMap.putAll((Map<String, Object>) sampleStorageCommons
				.getSelectedSampleStorageStructure(nsamplestoragelocationcode, 0, userInfo).getBody());
		outputMap.put("nsamplestoragelocationcode", nsamplestoragelocationcode);
		outputMap.putAll(getsamplestoragemove(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSampleStorageTransaction(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		String Query = "";
		String updateQuery = "";
		Map<String, Object> barcodeDetails = new HashMap<>();
		boolean checkValue = false;

		if ((int) inputMap.get("nbarcodedescription") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			barcodeDetails = readBarcode(inputMap, userInfo);
			inputMap.put("jsondata", barcodeDetails.get("jsonValue"));
		}
		try {
			final String strCheckPosition = "select * from samplestoragetransaction where "
					+ " nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode")
					+ " and nsamplestoragemappingcode=" + inputMap.get("nsamplestoragemappingcode") + " and sposition='"
					+ inputMap.get("sposition") + "' and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strCheckPosition);
			checkValue = false;
			inputMap.put("isAlreadyExists", true);
		} catch (final Exception e) {
			checkValue = true;
		}

		if (checkValue == true) {
			final String strCheckValue = "select * from samplestoragetransaction where spositionvalue='"
					+ inputMap.get("spositionvalue") + "' and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<Map<String, Object>> lstCheck = jdbcTemplate.queryForList(strCheckValue);
			if (lstCheck.isEmpty()) {

				final String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstaus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Integer objseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);

				final int nsamplestoragetransactioncode = objseq + 1;
				Query = " insert into samplestoragetransaction (nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode, nprojecttypecode, sposition,"
						+ "	spositionvalue, jsondata, npositionfilled, dmodifieddate, nsitecode, nstatus)  values ( "
						+ nsamplestoragetransactioncode + "," + inputMap.get("nsamplestoragelocationcode") + ","
						+ inputMap.get("nsamplestoragemappingcode") + "," + inputMap.get("nprojecttypecode") + " ,'"
						+ stringUtilityFunction.replaceQuote(inputMap.get("sposition").toString()) + "','"
						+ stringUtilityFunction.replaceQuote(inputMap.get("spositionvalue").toString()) + "', '"
						+ stringUtilityFunction.replaceQuote(inputMap.get("jsondata").toString()) + "',"
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " ,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ;";
				updateQuery = " update seqnobasemaster set nsequenceno= " + nsamplestoragetransactioncode
						+ " where stablename='samplestoragetransaction';";
				jdbcTemplate.execute(Query + updateQuery);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();

				final SampleStorageTransaction samplestoragetransaction = new SampleStorageTransaction();

				final Integer nsamplestoragelocationcodeint = (Integer) inputMap.get("nsamplestoragelocationcode");
				final Short nsamplestoragelocationcodeintshort = nsamplestoragelocationcodeint.shortValue();

				samplestoragetransaction.setNsamplestoragelocationcode(nsamplestoragelocationcodeintshort);
				samplestoragetransaction.setNsamplestoragetransactioncode(nsamplestoragetransactioncode);
				samplestoragetransaction.setNsamplestoragemappingcode((int) inputMap.get("nsamplestoragemappingcode"));
				samplestoragetransaction.setSposition(inputMap.get("sposition").toString());
				samplestoragetransaction.setSpositionvalue(inputMap.get("spositionvalue").toString());
				samplestoragetransaction.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
				samplestoragetransaction.setNsitecode(userInfo.getNtranssitecode());
				samplestoragetransaction
						.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				savedTestList.add(samplestoragetransaction);
				multilingualIDList.add("IDS_STORAGESTRUCTURENAME");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		return getSheetData(inputMap, userInfo);
	}

	private ResponseEntity<Object> getSheetData(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		String strQuery = "select *, jsondata  as \"additionalInfo\" from samplestoragetransaction where nsamplestoragelocationcode="
				+ inputMap.get("nsamplestoragelocationcode") + " and nsamplestoragemappingcode="
				+ inputMap.get("nsamplestoragemappingcode") + " and sposition='" + inputMap.get("sposition")
				+ "' and npositionfilled=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("cellData", map);

		strQuery = "(select ssm.nnoofcontainer-count(st.sposition) as navailablespace from samplestoragetransaction st ,samplestoragemapping ssm "
				+ "	where   ssm.nsamplestoragemappingcode=st.nsamplestoragemappingcode "
				+ " and st.nsamplestoragemappingcode= " + inputMap.get("nsamplestoragemappingcode") + " and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.spositionvalue!='' "
				+ " group by ssm.nnoofcontainer " + "		)";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("navailablespace", map);

		if (inputMap.containsKey("isAlreadyExists")) {
			outputMap.put("isAlreadyExists", inputMap.get("isAlreadyExists"));
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}
*/
	/**
	 * This method is used to transfer a container from one container to another. ,  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched,nsourcemappingcode:Indicates the primary key or value field used for identifying the record from the source container mapping code,
	 * nsourcesamplestoragelocationcode:Indicates the primary key or value field used for identifying the record from the source container structure code,
	 * nsourceprojecttypecode:Indicates the primary key or value field used for identifying the record from the source project type code,
	 * nsamplestoragelocationcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage location code,
	 * nsamplestoragemappingcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage mapping code,
	 * nprojecttypecode:Indicates the primary key or value field used for identifying the record to be transferred to the source project type code,
	 * ssamplestoragelocationname:Indicates the primary key or value field used for identifying from the source Sample Storage location Name,
	 * ssamplestoragepathname:Indicates the primary key or value field used for identifying from the source Sample Storage location Path,
	 * stosamplestoragelocationname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Location Name,
	 * stosamplestoragepathname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Path,
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"nsourcemappingcode":3,"nsourcesamplestoragelocationcode":2,"nsourceprojecttypecode":1,"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,"nprojecttypecode":1,"ssamplestoragelocationname":"FZ02",
	 * "ssamplestoragepathname":"root > Label > Label > Label","stosamplestoragelocationname":"FZ01","stosamplestoragepathname":"FZ > Rack > Tray > Cell Position"}	
	 * 			
	 * @return response entity  object holding response status and list of available Container Location.
	 * 
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> updateSampleStorageTransaction(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String updateQuery = "";
		final List<String> multilingualIDList = new ArrayList<>();
		SampleStorageTransaction sampleStorageTransactionnew = new SampleStorageTransaction();
		final List<Object> savedTestList = new ArrayList<>();

		final Map<String, Object> outputMap = new HashMap<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		if (inputMap.containsKey("isMultiContainermove")) {
			final String availablePathQuery = " select sl.ssamplestoragelocationname,ssm.*,ssc.nsamplestoragelocationcode,ssm.sboxid,ssc.jsondata->>'scontainerpath' as scontainerpath "
					+ " from samplestoragemapping ssm,samplestoragecontainerpath ssc,samplestorageversion ssv,samplestoragelocation sl  "
					+ " where ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
					+ " and ssc.nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode") + ""
					+ " and ssv.nsamplestoragelocationcode=ssc.nsamplestoragelocationcode"
					+ " and ssv.nsamplestorageversioncode=ssc.nsamplestorageversioncode"
					+ " and sl.nsamplestoragelocationcode=ssv.nsamplestoragelocationcode"
					+ " and sl.nsamplestoragelocationcode=ssc.nsamplestoragelocationcode and ssm.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssc.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssv.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sl.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssv.napprovalstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and ssm.nsamplestoragemappingcode not in (select nsamplestoragemappingcode from samplestoragetransaction "
					+ " where nsamplestoragelocationcode = " + inputMap.get("nsamplestoragelocationcode")
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			final List<Map<String, Object>> lstavailablePath = jdbcTemplate.queryForList(availablePathQuery);
			final List<Map<String, Object>> lstsourcePath = objectMapper
					.readValue(inputMap.get("selectedContainers").toString(), List.class);

			for (int i = 0; i < lstsourcePath.size(); i++) {

				final int ncontainertypecode = (int) lstsourcePath.get(i).get("ncontainertypecode");
				final int ncontainerstructurecode = (int) lstsourcePath.get(i).get("ncontainerstructurecode");
				final Optional<Map<String, Object>> mapobj = lstavailablePath.stream()
						.filter(item -> ((int) item.get("ncontainertypecode") == ncontainertypecode)
								&& ((int) item.get("ncontainerstructurecode") == ncontainerstructurecode))
						.findFirst();
				if (mapobj.isPresent()) {
					lstavailablePath.remove(mapobj.get());
					sampleStorageTransactionnew = new SampleStorageTransaction();
					updateQuery += " UPDATE public.samplestoragetransaction SET   " + " nsamplestoragelocationcode= "
							+ inputMap.get("nsamplestoragelocationcode") + " ,nsamplestoragemappingcode="
							+ mapobj.get().get("nsamplestoragemappingcode") + ",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'	WHERE nsamplestoragetransactioncode "
							+ " in ( select nsamplestoragetransactioncode from samplestoragetransaction where nsamplestoragemappingcode in ("
							+ lstsourcePath.get(i).get("nsamplestoragemappingcode") + ") and nprojecttypecode="
							+ inputMap.get("nsourceprojecttypecode") + " and nsamplestoragelocationcode="
							+ inputMap.get("nsourcesamplestoragelocationcode") + " ); ";

					sampleStorageTransactionnew.setSsamplestoragelocationname(
							lstsourcePath.get(i).get("ssamplestoragelocationname").toString());
					sampleStorageTransactionnew
							.setSsamplestoragepathname(lstsourcePath.get(i).get("scontainerpath").toString());
					sampleStorageTransactionnew.setSboxid(lstsourcePath.get(i).get("sboxid").toString());

					sampleStorageTransactionnew
							.setStosamplestoragelocationname(mapobj.get().get("ssamplestoragelocationname").toString());
					sampleStorageTransactionnew
							.setStosamplestoragepathname(mapobj.get().get("scontainerpath").toString());
					sampleStorageTransactionnew.setStoboxid(mapobj.get().get("sboxid").toString());
					multilingualIDList.add("IDS_MOVECONTAINER");
					savedTestList.add(sampleStorageTransactionnew);

				} else {
					if (inputMap.containsKey("isok")) {
						continue;
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SOURCEANDDESTINATIONMISMATCH",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					}
				}
			}
		} else {
			updateQuery = " UPDATE public.samplestoragetransaction SET   " + " nsamplestoragelocationcode= "
					+ inputMap.get("nsamplestoragelocationcode") + " ,nsamplestoragemappingcode="
					+ inputMap.get("nsamplestoragemappingcode") + ",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'"
					//ALPD-5097-Comment by Vignesh R(12-06-2025)-->Sample info fields showing wrongly in sample box, which is configured in barcode formatting
					//+ ",nprojecttypecode="+ inputMap.get("nprojecttypecode") + "	"
					+ " WHERE nsamplestoragetransactioncode "
					+ " in ( select nsamplestoragetransactioncode from samplestoragetransaction where nsamplestoragemappingcode = "
					+ inputMap.get("nsourcemappingcode") + " "
					+ " and nprojecttypecode="+ inputMap.get("nsourceprojecttypecode") + " "
					+ " and nsamplestoragelocationcode="+ inputMap.get("nsourcesamplestoragelocationcode") + ") "
					+ " and nprojecttypecode="+ inputMap.get("nsourceprojecttypecode") + " "
					+ " " + " and nsamplestoragelocationcode="+ inputMap.get("nsourcesamplestoragelocationcode") + "";
			
			multilingualIDList.add("IDS_MOVECONTAINER");
			sampleStorageTransactionnew
					.setSsamplestoragelocationname(inputMap.get("ssamplestoragelocationname").toString());
			sampleStorageTransactionnew.setSsamplestoragepathname(inputMap.get("ssamplestoragepathname").toString());
			sampleStorageTransactionnew
					.setStosamplestoragelocationname(inputMap.get("stosamplestoragelocationname").toString());
			sampleStorageTransactionnew
					.setStosamplestoragepathname(inputMap.get("stosamplestoragepathname").toString());

			sampleStorageTransactionnew.setSboxid(inputMap.get("sboxid").toString());
			sampleStorageTransactionnew.setStoboxid(inputMap.get("stoboxid").toString());
			savedTestList.add(sampleStorageTransactionnew);

		}
		if (updateQuery.equals("")) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOAVAILABLESPACEINDESTINATIONSTORAGESTRUCTURE",
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			jdbcTemplate.execute(updateQuery);
		}

		inputMap.put("label", "sampleStoragetransaction");
		inputMap.put("valuemember", "nsamplestoragetransactioncode");
		inputMap.put("nprojecttypecode", inputMap.get("filterprojecttypecode"));
		inputMap.put("source", "view_samplestoragelocation");
		outputMap.putAll(getDynamicFilterExecuteData(inputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/*
	@Override
	public ResponseEntity<Object> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String updateQuery = "";
		final Map<String, Object> mapObj = new HashMap<String, Object>();
		final String strcheck = "select count(nsamplestoragemappingcode) from samplestoragemapping ssm,samplestoragecontainerpath ssc "
				+ "where ssm.sboxid=N'" + stringUtilityFunction.replaceQuote(inputMap.get("sboxid").toString()) + "'  "
				+ " and ssm.nsamplestoragemappingcode <> " + inputMap.get("nsamplestoragemappingcode")
				+ "and ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
				+ "and ssc.nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode")
				+ " and ssc.nsitecode=" + userInfo.getNmastersitecode() + " and ssm.nsitecode="
				+ userInfo.getNmastersitecode() + " and ssc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		Integer check = null;
		boolean isExist = false;
		try {
			check = jdbcTemplate.queryForObject(strcheck, Integer.class);
		} catch (final Exception e) {
			check = null;
		}
		if (check == 0) {
			final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
			updateQuery = " UPDATE public.samplestoragemapping SET   sboxid='"
					+ stringUtilityFunction.replaceQuote(inputMap.get("sboxid").toString())
					+ "'	WHERE nsamplestoragemappingcode=" + nsamplestoragemappingcode + ";";
			jdbcTemplate.execute(updateQuery);
			isExist = false;
			mapObj.put("sboxid", inputMap.get("sboxid").toString());
		} else {
			final String strGetQuery = "select sboxid from samplestoragemapping where nsamplestoragemappingcode="
					+ inputMap.get("nsamplestoragemappingcode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String boxValue = jdbcTemplate.queryForObject(strGetQuery, String.class);
			mapObj.put("sboxid", boxValue);
			isExist = true;
		}
		mapObj.put("existCheck", isExist);
		return new ResponseEntity<>(mapObj, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");

		final String updateQuery = "  delete from samplestoragemapping WHERE nsamplestoragemappingcode="
				+ nsamplestoragemappingcode + ";";

		jdbcTemplate.execute(updateQuery);
		return getsamplestoragemove(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		String updateQuery = "";
		final int nmappingtranscode = jdbcTemplate
				.queryForObject("select nmappingtranscode from samplestoragelocation where nsamplestoragelocationcode="
						+ nsamplestoragelocationcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Short.class);

		if (nmappingtranscode == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			updateQuery = "  update   samplestoragelocation set nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " where  nsamplestoragelocationcode=" + nsamplestoragelocationcode + ";";

			jdbcTemplate.execute(updateQuery);

			return getActiveSampleStorageMappingById(inputMap, userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();

		final String nsamplestoragemappingcode = (String) inputMap.get("nsamplestoragemappingcode");

		String strQuery = "";

		if ((boolean) inputMap.get("isMultiSampleAdd")) {
			strQuery = "select jsonb_object_agg(x.nsamplestoragemappingcode,x.jsondata)   from "
					+ "(select nsamplestoragemappingcode,jsonb_object_agg( sposition,jsonb_build_object('spositionvalue',spositionvalue, "
					+ "	  'additionalInfo', jsondata " + " ) )  as jsondata  "
					+ "from  samplestoragetransaction where nsamplestoragemappingcode in (" + nsamplestoragemappingcode
					+ ")  and npositionfilled= " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by nsamplestoragemappingcode)x";
		} else {
			strQuery = "select jsonb_object_agg( sposition,jsonb_build_object('spositionvalue',spositionvalue, "
					+ " 'additionalInfo', jsondata ) ) as jsondata "
					+ " from samplestoragetransaction where nsamplestoragemappingcode=" + nsamplestoragemappingcode
					+ " and npositionfilled= " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		System.out.println(strQuery);
		String strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);
		outputMap.put("sheetData", strMaterial);
		strQuery = " SELECT Jsonb_object_agg(x.nsamplestoragemappingcode, x.sboxid) "
				+ " FROM (SELECT nsamplestoragemappingcode,sboxid FROM samplestoragemapping "
				+ " WHERE  nsamplestoragemappingcode IN ( " + nsamplestoragemappingcode + " ) and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " GROUP  BY nsamplestoragemappingcode, sboxid)x ";
		strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);
		outputMap.put("AdditionalFieldsComponentData", strMaterial);

		strQuery = "select ssm.nnoofcontainer-count(st.sposition) as navailablespace from samplestoragetransaction st ,samplestoragemapping ssm "
				+ " where ssm.nsamplestoragemappingcode=st.nsamplestoragemappingcode "
				+ " and st.nsamplestoragemappingcode= " + nsamplestoragemappingcode
				+ "	and st.spositionvalue!='' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ssm.nnoofcontainer";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("navailablespace", map);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	*/
	/**
	 * This method is used to list of available Sample Storage data.. ,  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 	which the list is to be fetched,nsourcemappingcode:Indicates the primary key or value field used for identifying the record from the source container mapping code,
	 * nsourcesamplestoragelocationcode:Indicates the primary key or value field used for identifying the record from the source container structure code,
	 * nsourceprojecttypecode:Indicates the primary key or value field used for identifying the record from the source project type code,
	 * nsamplestoragelocationcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage location code,
	 * nsamplestoragemappingcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage mapping code,
	 * nprojecttypecode:Indicates the primary key or value field used for identifying the record to be transferred to the source project type code,
	 * ssamplestoragelocationname:Indicates the primary key or value field used for identifying from the source Sample Storage location Name,
	 * ssamplestoragepathname:Indicates the primary key or value field used for identifying from the source Sample Storage location Path,
	 * stosamplestoragelocationname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Location Name,
	 * stosamplestoragepathname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Path,
	 * source:Indicates the table name of sample storage transaction. 
	 * valuemember:Indicates the primary key of sample storage transaction code.
	 * label:Indicates the label of sample storage transaction.
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"nsourcemappingcode":3,"nsourcesamplestoragelocationcode":2,"nsourceprojecttypecode":1,"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,"nprojecttypecode":1,"ssamplestoragelocationname":"FZ02",
	 * "ssamplestoragepathname":"root > Label > Label > Label","stosamplestoragelocationname":"FZ01","stosamplestoragepathname":"FZ > Rack > Tray > Cell Position","source":"view_samplestoragelocation","valuemember":"nsamplestoragetransactioncode","label":"sampleStoragetransaction"}	
	 * 			
	 * @return response entity  object holding response status and list of available available Sample Storage Data.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String tableName = "";
		String getJSONKeysQuery = "";
		final Map<String, Object> returnObject = new HashMap<>();

		String nprojecttypecode = null;
		if (inputMap.get("nprojecttypecode") != null) {

			nprojecttypecode = "where nprojecttypecode=" + inputMap.get("nprojecttypecode").toString() + "";
		} else {
			nprojecttypecode = "";
		}

		final String sourceName = (String) inputMap.get("source");
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
		}

		final String scollate = "collate \"default\"";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				final String sb = conditionString;
				String sQuery = conditionString;
				final int colanindex = sb.indexOf("LIKE '");
				final String str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				final StringBuilder sb3 = new StringBuilder(str1);
				final StringBuilder sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				System.out.println(sQuery);
				final int indexofsv = sQuery.indexOf("'");

				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		tableName = sourceName.toLowerCase();

		final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'jsonb'";
		String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
		jsonField = jsonField != null ? "||" + jsonField : "";
		final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		final String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
		if (fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select " + tableName + ".* from " + tableName + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
					+ inputMap.get("valuemember") + "\" > '0' " + conditionString
					+ " and  nsamplestoragemappingcode  in  "
					+ "(select nsamplestoragemappingcode from samplestoragetransaction ) ;";
		} else {
			getJSONKeysQuery = "select  " + tableName + ".* from " + tableName + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString
					+ " and  nsamplestoragemappingcode  in  "
					+ "(select nsamplestoragemappingcode from samplestoragetransaction  " + nprojecttypecode + " ) ;";
		}
		final List<Map<String, Object>> data = jdbcTemplate.queryForList(getJSONKeysQuery);
		returnObject.put((String) inputMap.get("label"), data);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}
	
	/**
	 * This method is used to read the barcode based on the project type. ,  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 	which the list is to be fetched,spositionvalue:Indicates the  value field used for identifying the record of barcode id,
	 * nprojecttypecode:Indicates the primary key or value field used for identifying the record to be transferred to the source project type code,
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"spositionvalue":3,"P1001156009234":2,"nprojecttypecode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available barcode id based on the project type.
	 * 
	 * @throws Exception exception
	 */

	private Map<String, Object> readBarcode(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String barcode = inputMap.get("spositionvalue").toString().replaceAll("\\s", "");
		final int nprojectcode = (int) inputMap.get("nprojecttypecode");
		final int nbarcodeLength = barcode.length();
		final Map<String, Object> returnMap = new HashMap<>();
		final JSONObject objJsonDataList = new JSONObject(inputMap.get("jsondata").toString());
		String value = "";
		String subBarcode = "";
		final String query = "select * from bulkbarcodeconfigdetails where nprojecttypecode= " + nprojectcode
				+ " and  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter ";
		final List<BulkBarcodeConfigDetails> barcodelist = jdbcTemplate.query(query, new BulkBarcodeConfigDetails());
		if (!barcodelist.isEmpty() && (nbarcodeLength == (int) inputMap.get("nbarcodeLength"))) {
			for (int i = 0; i < barcodelist.size(); i++) {
				subBarcode = "";
				if (barcodelist.get(i).getJsondata().containsKey("barcoderelation")) {
					final List<Map<String, Object>> listRelationValue = (List<Map<String, Object>>) barcodelist.get(i)
							.getJsondata().get("barcoderelation");
					for (int j = 0; j < listRelationValue.size(); j++) {
						final Map<String, Object> parentValueCode = (Map<String, Object>) barcodelist.get(i)
								.getJsondata().get("parentdata");
						final int nfieldstartposition = Integer
								.parseInt(listRelationValue.get(j).get("nfieldstartposition").toString());
						final int nfieldlength = Integer
								.parseInt(listRelationValue.get(j).get("nfieldlength").toString());
						final String parentvalue = barcode.substring(
								Integer.valueOf(parentValueCode.get("nfieldstartposition").toString()),
								Integer.valueOf(parentValueCode.get("nfieldstartposition").toString())
										+ Integer.valueOf(parentValueCode.get("nfieldlength").toString()));
						final List<String> lstConstraintdata = Arrays
								.asList(listRelationValue.get(j).get("sconstraintdata").toString().split(","));
						final Boolean isconstraintdata = lstConstraintdata.stream()
								.anyMatch(x -> x.equals(parentvalue));

						if (isconstraintdata && nfieldstartposition != -1 && nfieldlength != -1) {
							subBarcode = barcode.substring(nfieldstartposition, nfieldstartposition + nfieldlength);
							break;
						}

					}
				} else {
					subBarcode = barcode.substring(barcodelist.get(i).getNfieldstartposition(),
							barcodelist.get(i).getNfieldstartposition() + barcodelist.get(i).getNfieldlength());
				}
				final String skeyname = barcodelist.get(i).getJsondata().get("sfieldname").toString();
				String querys = "";
				if (!subBarcode.isEmpty()) {
					if (barcodelist.get(i).getNneedmaster() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final String stablename = barcodelist.get(i).getStablename();
						final String stablecoumnname = barcodelist.get(i).getStablecolumnname();
						final String stablefieldname = barcodelist.get(i).getJsondata().get("stablefieldname")
								.toString();

						if (barcodelist.get(i).getNqueryneed() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							String squery = barcodelist.get(i).getSquery();
							final StringBuilder sbuilder1 = new StringBuilder();
							if (squery != null) {
								sbuilder1.append(squery);
								while (squery.contains("<#")) {
									final int nStart = squery.indexOf("<#");
									final int nEnd = squery.indexOf("#>");
									sbuilder1.replace(nStart, nEnd + 2, subBarcode);
									squery = sbuilder1.toString();
								}
								querys = squery + ";";
							}
						} else {
							querys = "select " + stablefieldname + "  from  " + stablename + " where " + stablecoumnname
									+ "='" + subBarcode + "' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						}
						try {
							value = jdbcTemplate.queryForObject(querys, String.class);
						} catch (final Exception e) {
							value = "-";
						}
						objJsonDataList.put(skeyname, value);

					} else {
						objJsonDataList.put(skeyname, subBarcode);
					}
				} else {
					objJsonDataList.put(skeyname, "-");
				}

			}
		}
		returnMap.put("jsonValue", objJsonDataList);
		return returnMap;
	}

	/**
	 * This method is used to retrieve the list of available Storage Structure and Sample Storage Container Path.
	 * 
	 * 	@param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nsamplestoragemappingcode: Primary key of sample storage mapping,ncontainerstructurecode: Indicates the primary key or value field used for identifying Container Structure,
	 * ncontainertypecode: Indicates the primary key or value field used for identifying Container Type.	
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"ncontainertypecode":1,"nsamplestoragemappingcode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available Storage Structure and Sample Storage Container Path.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Object> getsamplemovedata(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		int nsamplestoragelocationcode = 0;
		String conditionalstr = "";
		String strQuery = " select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
				+ " samplestorageversion where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode() + ") and ts.ntranscode=ssl.nmappingtranscode"
				+ " order by ssl.nsamplestoragelocationcode desc ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		returnObject.put("sampleStorageLocation", list);
		if (inputMap.containsKey("nsamplestoragelocationcode")
				&& (int) inputMap.get("nsamplestoragelocationcode") != 0) {
			nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		} else {
			nsamplestoragelocationcode = (int) list.get(0).get("nsamplestoragelocationcode");
		}

		if (inputMap.containsKey("nprojecttypecode") && (int) inputMap.get("nprojecttypecode") != 0) {
			conditionalstr = " and ssm.nprojecttypecode=" + inputMap.get("nprojecttypecode");
		}

		if (inputMap.containsKey("nproductcode") && (int) inputMap.get("nproductcode") != 0) {
			if (conditionalstr != "") {
				conditionalstr += " and ssm.nproductcode=" + inputMap.get("nproductcode");
			} else {
				conditionalstr = " and ssm.nproductcode=" + inputMap.get("nproductcode");
			}
		}

		strQuery = " select ssm.*,ssc.nsamplestoragecontainerpathcode,ssc.jsondata->>'scontainerpath' as scontainerpath from  "
				+ " samplestoragelocation ssl,  samplestoragecontainerpath ssc, samplestoragemapping ssm where "
				+ " ssl.nsamplestoragelocationcode=ssc.nsamplestoragelocationcode "
				+ " and ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
				+ " and ssm.ncontainertypecode=" + inputMap.get("ncontainertypecode")
				+ " and ssm.ncontainerstructurecode=" + inputMap.get("ncontainerstructurecode")
				+ " and ssl.nsamplestoragelocationcode=" + nsamplestoragelocationcode + " and ssl.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssc.nsamplestorageversioncode = (select nsamplestorageversioncode from samplestorageversion ssv"
				+ " where ssv.nsamplestoragelocationcode= " + nsamplestoragelocationcode + " and ssv.napprovalstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ssv.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssv.nsitecode="
				+ userInfo.getNtranssitecode() + ") " + conditionalstr
				+ " and ssm.nsamplestoragemappingcode not in (select nsamplestoragemappingcode from samplestoragetransaction where  "
				+ "	nsamplestoragelocationcode=" + nsamplestoragelocationcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " group by nsamplestoragemappingcode) ";
		list = jdbcTemplate.queryForList(strQuery);
		returnObject.put("samplestoragecontainerpath", list);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

}