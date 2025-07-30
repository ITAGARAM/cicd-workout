package com.agaramtech.qualis.storagemanagement.service.samplestoragestructure;

import java.util.ArrayList;
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
import com.agaramtech.qualis.basemaster.model.ContainerStorageCondition;
import com.agaramtech.qualis.basemaster.model.SeqNoBasemaster;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.SampleStorageCommons;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageVersion;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import com.agaramtech.qualis.storagemanagement.service.samplestoragemapping.SampleStorageMappingDAO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleStorageStructureDAOImpl implements SampleStorageStructureDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageStructureDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageCommons sampleStorageCommons;
	private final SampleStorageMappingDAO sampleStorageMappingDAO;

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	// ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.
	@Override
	public ResponseEntity<Object> getSampleStorageStructure(final int nsampleStorageLocationCode,
			final int nstorageCategoryCode, final int nsamplestorageversioncode, final UserInfo userInfo)
			throws Exception {
		LOGGER.info("getSampleStorageStructure()");
		final Map<String, Object> outputMap = new HashMap<>();
		String sQuery = "";

		final List<StorageCategory> filterStorageCategory = sampleStorageCommons.getStorageCategory(userInfo).getBody();
		if (filterStorageCategory != null && filterStorageCategory.size() > 0) {
			outputMap.put("filterStorageCategory", filterStorageCategory);

			if (nstorageCategoryCode == 0) {
				sQuery = "select * from samplestoragelocation where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nstoragecategorycode = "
						+ filterStorageCategory.get(0).getNstoragecategorycode() + " "
						+ " and nmastersitecode="+ userInfo.getNmastersitecode() + " "
						+ " order by nsamplestoragelocationcode desc ";

				outputMap.put("selectedStorageCategory", filterStorageCategory.get(0));
				outputMap.put("selectedStorageCategoryName", filterStorageCategory.get(0).getSstoragecategoryname());
				outputMap.put("nfilterStorageCategory", filterStorageCategory.get(0).getNstoragecategorycode());

			} else {
					// ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.
				sQuery = "select * from samplestoragelocation where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstoragecategorycode = "
						+ nstorageCategoryCode + " "
						+ " and nmastersitecode=" + userInfo.getNmastersitecode()
						+ " order by nsamplestoragelocationcode desc ";

				final Optional<StorageCategory> storageCategory = filterStorageCategory.stream()
						.filter(field -> field.getNstoragecategorycode() == nstorageCategoryCode).findAny();

				outputMap.put("selectedStorageCategory", storageCategory.get());
				outputMap.put("selectedStorageCategoryName", storageCategory.get().getSstoragecategoryname());
				outputMap.put("nfilterStorageCategory", nstorageCategoryCode);
			}
			final List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);

			outputMap.put("sampleStorageLocation", list);
			if (list.size() > 0) {
				int nlocationCode = 0;
				if (nsampleStorageLocationCode == 0) {
					nlocationCode = (int) list.get(0).get("nsamplestoragelocationcode");
				} else {
					nlocationCode = nsampleStorageLocationCode;
				}
				outputMap.putAll((Map<String, Object>) sampleStorageCommons
						.getSelectedSampleStorageStructure(nlocationCode, nsamplestorageversioncode, userInfo)
						.getBody());
			} else {
				outputMap.put("sampleStorageVersion", null);
				outputMap.put("selectedSampleStorageLocation", null);
			}
		} else {
			outputMap.put("filterStorageCategory", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1},nsampleStorageLocationCode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<? extends Object> getSelectedSampleStorageStructure(final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		return sampleStorageCommons.getSelectedSampleStorageStructure(nsampleStorageLocationCode,
				nsamplestorageversioncode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<? extends Object> getActiveSampleStorageVersion(final int nsampleStorageVersionCode,
			final UserInfo userInfo) throws Exception {
		return sampleStorageCommons.getActiveSampleStorageVersion(nsampleStorageVersionCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSampleStorageVersionByID(final int nsampleStorageVersionCode,
			final int nsampleStorageLocationCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final SampleStorageStructure deleteSampleStorageLocation = sampleStorageCommons
				.getActiveLocation((short) nsampleStorageLocationCode);

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		final SampleStorageVersion deletedSampleStorageVersion = sampleStorageCommons
				.getActiveVersion((short) nsampleStorageVersionCode);
		if (deletedSampleStorageVersion == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		// ALPD-4493-Vignesh R(20-07-2024)--storage structure->approved records also
		// edited after copy the version.
		// ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.
		final String sQuery = " select (a.jsondata->'additionalinfo'->>'nprojecttypecode')::integer as nprojecttypecode,(a.jsondata->'additionalinfo'->>'nunitcode')::integer as nunitcode,"
				+ " a.nsamplestoragelocationcode,a.nsamplestorageversioncode,b.nneedautomapping, b.ncontainerstructurecode, "
				+ " (a.jsondata->'additionalinfo'->>'ncolumn')::integer as ncolumn,(a.jsondata->'additionalinfo'->>'nrow')::integer as nrow, case when a.nversionno = 0 then '-' else cast(a.nversionno as character varying(10)) end nversionno,"
				+ " (a.jsondata->'additionalinfo'->>'nquantity') as nquantity,(a.jsondata->'additionalinfo'->>'nproductcode')::integer as nproductcode,a.napprovalstatus,a.jsondata, a.nstatus,b.ssamplestoragelocationname,b.nstoragecategorycode, "
				+ " (a.jsondata->'additionalinfo'->>'nneedposition')::integer as nneedposition,(a.jsondata->'additionalinfo'->>'nnoofcontainer')::integer as nnoofcontainer,(a.jsondata->'additionalinfo'->>'nneedautomapping')::integer as nneedautomapping,(a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode, "
				+ " (a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode,(a.jsondata->'additionalinfo'->>'ndirectionmastercode')::integer as ndirectionmastercode, "
				+ " (a.jsondata->'additionalinfo'->>'nstoragecategorycode')::integer as nstoragecategorycode,(a.jsondata->'additionalinfo'->>'ncontainerstructurecode')::integer as ncontainerstructurecode,"
				+ " (a.jsondata->'additionalinfo'->>'ssamplestoragelocationname') as ssamplestoragelocationname,a.napprovalstatus ,b.nsitecode,b.nmastersitecode,"
				+ " s.ssitename "
				+ " from samplestorageversion a,samplestoragelocation b, containertype ct,containerstructure cs, site s "
				+ " where a.nsamplestoragelocationcode = b.nsamplestoragelocationcode"
				+ " and a.nsamplestorageversioncode = " + nsampleStorageVersionCode
				+ " and s.nsitecode = b.nsitecode "
				+ " and b.ncontainertypecode=ct.ncontainertypecode and b.ncontainerstructurecode=cs.ncontainerstructurecode and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ct.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and cs.nsitecode=" + userInfo.getNmastersitecode();

		final List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			if ((int) list.get(0).get("napprovalstatus") == Enumeration.TransactionStatus.RETIRED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else if ((int) list.get(0).get("napprovalstatus") == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);

			} else {
				outputMap.put("selectedSampleStorageVersion", list.get(0));
				outputMap.put("ncontainertypecode", list.get(0).get("ncontainertypecode"));
				outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
			}
			//ALPDJ21-32-comment by Vignesh(23-07-2025)-->There is no need to fetch the Site ID and Instrument ID.
			/*// ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.
			final String sQuerysite = "select s.ssitename as sregionalsitename,s.nsitecode as nregionalsitecode "
					+ "from site s where s.nsitecode="+list.get(0).get("nsitecode")+" and s.nsitecode>0  order by 1 desc ;";
			
			final List<Map<String, Object>> listSite = jdbcTemplate.queryForList(sQuerysite);
			
			outputMap.put("getSite", listSite.get(0));*/

		} else {
			outputMap.put("selectedSampleStorageVersion", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = " select cts.*,ct.* from   containerstructure cts, containertype ct where  ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nsitecode="
				+ userInfo.getNmastersitecode() + " and cts.nsitecode=ct.nsitecode "
				+ "and cts.ncontainertypecode= ct.ncontainertypecode and ct.ncontainertypecode="
				+ inputMap.get("ncontainertypecode");
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	private SampleStorageStructure getSampleStorageStructureName(final String sampleStorageLocationName,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select nsamplestoragelocationcode from samplestoragelocation where ssamplestoragelocationname = N'"
				+ stringUtilityFunction.replaceQuote(sampleStorageLocationName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();
		return (SampleStorageStructure) jdbcUtilityFunction.queryForObject(strQuery, SampleStorageStructure.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
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
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		String insertQueryString = "";

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();

		final SampleStorageStructure sampleStorageLocationListByName = getSampleStorageStructureName(
				sampleStorageLocation.getSsamplestoragelocationname(), userInfo);

		if (sampleStorageLocationListByName == null) {
			final String sQuery = " lock table locksamplestoragelocation "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);

			final StorageCategory objStorageCat = sampleStorageCommons
					.getActiveStorageCategoryById(sampleStorageLocation.getNstoragecategorycode(), userInfo);
			if (objStorageCat == null) {
				final String returnString = commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
				return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
			}
			String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragelocation' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
					SeqNoBasemaster.class, jdbcTemplate);
			final int nsampleStorageLocationCode = objseq.getNsequenceno() + 1;

			// ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.
			insertQueryString = " insert into samplestoragelocation (nsamplestoragelocationcode, ncontainertypecode,  "
					+ "	ncontainerstructurecode,nneedposition ,nrow,ncolumn, nneedautomapping ,"
					+ " nnoofcontainer, nquantity, nunitcode,"
					+ "	ndirectionmastercode ,nproductcode,nprojecttypecode, ssamplestoragelocationname, "
					+ " nstoragecategorycode, nstatus, nsitecode,  dmodifieddate,nmastersitecode) values ( "
					+ nsampleStorageLocationCode + "," + sampleStorageLocation.getNcontainertypecode() + ","
					+ sampleStorageLocation.getNcontainerstructurecode() + ","
					+ sampleStorageLocation.getNneedposition() + "," + sampleStorageLocation.getNrow() + ","
					+ sampleStorageLocation.getNcolumn() + "," + sampleStorageLocation.getNneedautomapping() + ","
					+ sampleStorageLocation.getNnoofcontainer() + "," + sampleStorageLocation.getNquantity() + ","
					+ sampleStorageLocation.getNunitcode() + "," + sampleStorageLocation.getNdirectionmastercode() + ","
					+ sampleStorageLocation.getNproductcode() + "," + sampleStorageLocation.getNprojecttypecode()
					+ " , N'"
					+ stringUtilityFunction.replaceQuote(sampleStorageLocation.getSsamplestoragelocationname()) + "' , "
					+ sampleStorageLocation.getNstoragecategorycode() + ",1, "
					+ sampleStorageLocation.getNmastersitecode()
					+ ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() 
					+ ") ";

			jdbcTemplate.execute(insertQueryString);

			sectSeq = "update seqnobasemaster set nsequenceno=" + nsampleStorageLocationCode
					+ " where stablename='samplestoragelocation'";
			jdbcTemplate.execute(sectSeq);
			sampleStorageLocation.setNsamplestoragelocationcode((short) nsampleStorageLocationCode);
			savedUnitList.add(sampleStorageLocation);

			//multilingualIDList.add("IDS_STORAGESTRUCTUREAUDIT");
			multilingualIDList.add("IDS_ADDSTORAGESTRUCTURE");

			auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);

			createSampleStorageVersion(nsampleStorageLocationCode, sampleStorageVersion, userInfo);

			return new ResponseEntity<>(
					getSampleStorageStructure(0, sampleStorageLocation.getNstoragecategorycode(), 0, userInfo)
							.getBody(),
					HttpStatus.OK);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private ResponseEntity<Object> createSampleStorageVersion(final int nsampleStorageLocationCode,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		String insertQueryString = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();

		String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestorageversion' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
				SeqNoBasemaster.class, jdbcTemplate);
		final int nsampleStorageVersionCode = objseq.getNsequenceno() + 1;

		final JSONObject jsonObject = new JSONObject(sampleStorageVersion.getJsondata());
		insertQueryString = " insert into samplestorageversion (nsamplestorageversioncode, nsamplestoragelocationcode, nversionno, napprovalstatus, jsondata, nstatus, nsitecode, dmodifieddate) values ( "
				+ nsampleStorageVersionCode + " , " + "" + nsampleStorageLocationCode + ", 0, "
				+ sampleStorageVersion.getNapprovalstatus() + ",'"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',1, " + userInfo.getNtranssitecode()
				+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' )";

		jdbcTemplate.execute(insertQueryString);

		sectSeq = "update seqnobasemaster set nsequenceno=" + nsampleStorageVersionCode
				+ " where stablename='samplestorageversion'";
		jdbcTemplate.execute(sectSeq);
		sampleStorageVersion.setNsamplestorageversioncode((short) nsampleStorageVersionCode);
		savedUnitList.add(sampleStorageVersion);

		multilingualIDList.add("IDS_ADDSAMPLESTORAGEVERSION");

		auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);

		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
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
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> updateSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		String insertQueryString = "";
		final SampleStorageStructure deleteSampleStorageLocation = sampleStorageCommons
				.getActiveLocation(sampleStorageLocation.getNsamplestoragelocationcode());

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		final SampleStorageVersion deletedSampleStorageVersion = sampleStorageCommons
				.getActiveVersion(sampleStorageVersion.getNsamplestorageversioncode());
		if (deletedSampleStorageVersion == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		if (deletedSampleStorageVersion != null && deletedSampleStorageVersion
				.getNapprovalstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final StorageCategory objStorageCat = sampleStorageCommons
				.getActiveStorageCategoryById(sampleStorageLocation.getNstoragecategorycode(), userInfo);
		if (objStorageCat == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		}
		if (sampleStorageVersion.getNapprovalstatus() == Enumeration.TransactionStatus.APPROVED
				.gettransactionstatus()) {

			final String sQuery = " lock table locksamplestoragelocation "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);

			sampleStorageVersion.setNapprovalstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
			createSampleStorageVersion(sampleStorageLocation.getNsamplestoragelocationcode(), sampleStorageVersion,
					userInfo);

			return new ResponseEntity<>(
					sampleStorageCommons
							.getAllSampleStorageVersion(sampleStorageLocation.getNsamplestoragelocationcode(),
									sampleStorageVersion.getNsamplestorageversioncode(), userInfo)
							.getBody(),
					HttpStatus.OK);
		} else {

			final String strQuery = "select nsamplestoragelocationcode from samplestoragelocation where ssamplestoragelocationname = N'"
					+ stringUtilityFunction.replaceQuote(sampleStorageLocation.getSsamplestoragelocationname())
					+ "' and nsamplestoragelocationcode <> " + sampleStorageLocation.getNsamplestoragelocationcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nmastersitecode=" + userInfo.getNmastersitecode()
					+ " and nsitecode=" + sampleStorageLocation.getNmastersitecode();

			final List<SampleStorageStructure> sampleStorageLocationList = jdbcTemplate.query(strQuery,
					new SampleStorageStructure());
			if (sampleStorageLocationList.isEmpty()) {

				final String sQuery = " lock table locksamplestoragelocation "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
				jdbcTemplate.execute(sQuery);

				final Map<String, Object> selectedSamplLocation = (Map<String, Object>) getSelectedSampleStorageStructure(
						sampleStorageLocation.getNsamplestoragelocationcode(), 0, userInfo).getBody();

				insertQueryString = " update samplestoragelocation set "
						+ " nsitecode="+sampleStorageLocation.getNmastersitecode()+" ,"
						+ " ssamplestoragelocationname = '"+ stringUtilityFunction.replaceQuote(sampleStorageLocation.getSsamplestoragelocationname())
						+ "'" + ",nstoragecategorycode = " + sampleStorageLocation.getNstoragecategorycode()
						+ " where nsamplestoragelocationcode = " + sampleStorageLocation.getNsamplestoragelocationcode()
						+ "";

				jdbcTemplate.execute(insertQueryString);

				final JSONObject jsonObject = new JSONObject(sampleStorageVersion.getJsondata());

				insertQueryString = " update samplestorageversion set  jsondata = '"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString())
						+ "' where nsamplestorageversioncode = " + sampleStorageVersion.getNsamplestorageversioncode()
						+ "";

				jdbcTemplate.execute(insertQueryString);

				listAfterUpdate.add(sampleStorageLocation);
				listBeforeUpdate.add(selectedSamplLocation.get("selectedSampleStorageLocation"));

				//multilingualIDList.add("IDS_STORAGESTRUCTUREAUDIT");
				multilingualIDList.add("IDS_EDITSTORAGESTRUCTURE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				boolean bcheckCategoryUpdate = true;
				if (selectedSamplLocation.get("selectedSampleStorageLocation") != null) {

					final Map<String, Object> getselectedSamplLocation = (Map<String, Object>) selectedSamplLocation
							.get("selectedSampleStorageLocation");
					if ((int) getselectedSamplLocation.get("nstoragecategorycode") == sampleStorageLocation
							.getNstoragecategorycode()) {
						bcheckCategoryUpdate = false;
					}
				}
				int nversionCode = 0;
				if (bcheckCategoryUpdate == true) {
					nversionCode = 0;
				} else {
					nversionCode = sampleStorageVersion.getNsamplestorageversioncode();
				}
				return new ResponseEntity<>(
						getSampleStorageStructure(sampleStorageLocation.getNsamplestoragelocationcode(),
								sampleStorageLocation.getNstoragecategorycode(), nversionCode, userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
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
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> deleteSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDListVersion = new ArrayList<>();
		final List<Object> deletedVersionList = new ArrayList<>();

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedLocationList = new ArrayList<>();

		String insertQueryString = "";

		String query = "";

		final SampleStorageStructure deleteSampleStorageLocation = sampleStorageCommons
				.getActiveLocation(sampleStorageVersion.getNsamplestoragelocationcode());

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		final SampleStorageVersion deletedSampleStorageVersion = sampleStorageCommons
				.getActiveVersion(sampleStorageVersion.getNsamplestorageversioncode());
		if (deletedSampleStorageVersion == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		query = "select napprovalstatus  from SampleStorageVersion where nsamplestoragelocationcode = "
				+ sampleStorageVersion.getNsamplestoragelocationcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode();
		final List<SampleStorageVersion> list = jdbcTemplate.query(query, new SampleStorageVersion());

		if (list.size() == 1) {

			if (list.get(0).getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				insertQueryString = " update samplestorageversion set  nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nsamplestorageversioncode = " + sampleStorageVersion.getNsamplestorageversioncode()
						+ "";

				jdbcTemplate.execute(insertQueryString);

				sampleStorageVersion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedVersionList.add(sampleStorageVersion);
				multilingualIDListVersion.add("IDS_DELETESAMPLESTORAGEVERSION");
				auditUtilityFunction.fnInsertAuditAction(deletedVersionList, 1, null, multilingualIDListVersion,
						userInfo);

				insertQueryString = " update samplestoragelocation set  nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nsamplestoragelocationcode = " + sampleStorageVersion.getNsamplestoragelocationcode()
						+ "";

				jdbcTemplate.execute(insertQueryString);

				sampleStorageLocation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedLocationList.add(sampleStorageLocation);

				multilingualIDList.add("IDS_DELETESAMPLESTORAGELOCATION");
				auditUtilityFunction.fnInsertAuditAction(deletedLocationList, 1, null, multilingualIDList, userInfo);

				return new ResponseEntity<>(
						getSampleStorageStructure(0, sampleStorageLocation.getNstoragecategorycode(), 0, userInfo)
								.getBody(),
						HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			if (sampleStorageVersion.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				insertQueryString = " update samplestorageversion set  nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nsamplestorageversioncode = " + sampleStorageVersion.getNsamplestorageversioncode()
						+ "";

				jdbcTemplate.execute(insertQueryString);

				sampleStorageVersion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedVersionList.add(sampleStorageVersion);
				multilingualIDListVersion.add("IDS_DELETESAMPLESTORAGEVERSION");
				auditUtilityFunction.fnInsertAuditAction(deletedVersionList, 1, null, multilingualIDListVersion,
						userInfo);

				return new ResponseEntity<>(
						getSampleStorageStructure(sampleStorageVersion.getNsamplestoragelocationcode(),
								sampleStorageLocation.getNstoragecategorycode(), 0, userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "containerlastnode": ["Label"], "containerpath": [ {
	 *                 "scontainerpath": "root > Label > Label > Label > Label" } ],
	 *                 "containerpathsize": 1, "containers": [],
	 *                 "propertyValidation": { "locationEnd": false, "storageStart":
	 *                 false, "storageEnd": true }, "sampleStorageLocation": {
	 *                 "nstatus": 1, "nstoragecategorycode": 3,
	 *                 "nsamplestoragelocationcode": 46 }, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 8,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46 },
	 *                 "selectedSampleStorageLocation": { "dmodifieddate":
	 *                 "2025-06-10T06:40:10.000+00:00", "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nmappingtranscode": 8,
	 *                 "nneedautomapping": 4, "nneedposition": 4, "nnoofcontainer":
	 *                 1, "nproductcode": -1, "nprojecttypecode": -1, "nquantity":
	 *                 0, "nrow": 1, "nsamplestoragelocationcode": 46, "nsitecode":
	 *                 1, "nstatus": 1, "nstoragecategorycode": 3, "nunitcode": -1,
	 *                 "scontainerstructurename": "NA", "scontainertype": "NA",
	 *                 "sdirection": "Left to Right", "sneedautomapping": "No",
	 *                 "sproductname": "NA", "sprojecttypename": "NA",
	 *                 "ssamplestoragelocationname": "Ss", "sstoragecategoryname":
	 *                 "Deep Freezer (-80°C)", "stransdisplaystatus": "Draft",
	 *                 "sunitname": "NA" }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
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
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> approveSampleStorageVersion(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String strcheck = "select sl.* from samplestoragelocation sl, samplestorageversion sv, "
				+ " samplestoragecontainerpath ssc, samplestoragetransaction sst, samplestoragemapping ssm "
				+ " where sl.nsamplestoragelocationcode=sv.nsamplestoragelocationcode "
				+ " and sl.nsamplestoragelocationcode = ssc.nsamplestoragelocationcode and sv.napprovalstatus= "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
				+ " and sst.nsamplestoragemappingcode=ssm.nsamplestoragemappingcode "
				+ " and sl.nsamplestoragelocationcode=" + sampleStorageLocation.getNsamplestoragelocationcode();
		final List<Map<String, Object>> lstcheck = jdbcTemplate.queryForList(strcheck);
		if (lstcheck.size() > 0) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RETRIEVEDISPOSEFORPREVIOUVERSION",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		final List<String> multilingualIDListApproved = new ArrayList<>();
		final List<Object> approvedVersionList = new ArrayList<>();

		final List<String> multilingualIDListRetired = new ArrayList<>();
		final List<Object> retiredVersionList = new ArrayList<>();

		String insertQueryString = "";

		String query = "";

		final SampleStorageStructure deleteSampleStorageLocation = sampleStorageCommons
				.getActiveLocation(sampleStorageVersion.getNsamplestoragelocationcode());

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		final SampleStorageVersion deletedSampleStorageVersion = sampleStorageCommons
				.getActiveVersion(sampleStorageVersion.getNsamplestorageversioncode());
		if (deletedSampleStorageVersion == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		query = "select napprovalstatus from SampleStorageVersion where nsamplestoragelocationcode = "
				+ sampleStorageVersion.getNsamplestoragelocationcode() + " and nsamplestorageversioncode = "
				+ sampleStorageVersion.getNsamplestorageversioncode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SampleStorageVersion versionApprovalStatus = (SampleStorageVersion) jdbcUtilityFunction
				.queryForObject(query, SampleStorageVersion.class, jdbcTemplate);

		if (versionApprovalStatus.getNapprovalstatus() == Enumeration.TransactionStatus.APPROVED
				.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		} else if (versionApprovalStatus.getNapprovalstatus() == Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTVERSION.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {

			if (inputMap.get("propertyValidation") != null) {

				final Map<String, Object> propertyValidation = (Map<String, Object>) inputMap.get("propertyValidation");
				if ((boolean) propertyValidation.get("storageEnd") == false) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTSTORAGEEND",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}

			final String sQuery = " lock table locksamplestoragelocation "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);

			query = "select nsamplestorageversioncode from SampleStorageVersion where nsamplestoragelocationcode = "
					+ sampleStorageVersion.getNsamplestoragelocationcode() + " and napprovalstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final SampleStorageVersion previousVersionApproval = (SampleStorageVersion) jdbcUtilityFunction
					.queryForObject(query, SampleStorageVersion.class, jdbcTemplate);

			if (previousVersionApproval != null) {

				insertQueryString = " update samplestorageversion set  napprovalstatus = "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
						+ " where nsamplestorageversioncode = " + previousVersionApproval.getNsamplestorageversioncode()
						+ "; ";
				insertQueryString += " update samplestoragelocation set nmappingtranscode="
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ " where nsamplestoragelocationcode=" + sampleStorageVersion.getNsamplestoragelocationcode()
						+ "; ";

				jdbcTemplate.execute(insertQueryString);

				previousVersionApproval
						.setNapprovalstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
				retiredVersionList.add(previousVersionApproval);
				multilingualIDListRetired.add("IDS_RETIREDSAMPLESTORAGEVERSION");
				auditUtilityFunction.fnInsertAuditAction(retiredVersionList, 1, null, multilingualIDListRetired,
						userInfo);

			}

			query = "select COALESCE(max(nversionno) , 0) + 1 as nversionno  from SampleStorageVersion where nsamplestoragelocationcode = "
					+ sampleStorageVersion.getNsamplestoragelocationcode() + "";
			final List<Map<String, Object>> list = jdbcTemplate.queryForList(query);

			final int nversionNo = (int) list.get(0).get("nversionno");

			insertQueryString = " update samplestorageversion set  napprovalstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", nversionno = " + nversionNo
					+ " where nsamplestorageversioncode = " + sampleStorageVersion.getNsamplestorageversioncode() + "";

			jdbcTemplate.execute(insertQueryString);

			sampleStorageVersion
					.setNapprovalstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
			sampleStorageVersion.setNversionno((short) nversionNo);
			approvedVersionList.add(sampleStorageVersion);
			multilingualIDListApproved.add("IDS_APPROVEDSAMPLESTORAGEVERSION");
			auditUtilityFunction.fnInsertAuditAction(approvedVersionList, 1, null, multilingualIDListApproved,
					userInfo);

			if (inputMap.get("containers") != null) {

				final List<Map<String, Object>> listOfContainers = (List<Map<String, Object>>) inputMap
						.get("containers");
				final int nlocationCode = sampleStorageVersion.getNsamplestoragelocationcode();
				for (int i = 0; i < listOfContainers.size(); i++) {

					final String scontainercode = (String) listOfContainers.get(i).get("scontainercode");
					final String slocationHierarchy = (String) listOfContainers.get(i).get("itemHierarchy");

					final Map<String, Object> containerStorageCondition = sampleStorageCommons
							.getContainerStorageCondition(scontainercode, userInfo).getBody();

					if (containerStorageCondition.get("storageContainer") != null) {

						insertQueryString = "update containerstoragecondition set slocationhierarchy='"
								+ slocationHierarchy + "' where scontainercode='" + scontainercode + "'";
						jdbcTemplate.execute(insertQueryString);
					} else {

						final ContainerStorageCondition containerStorageConditionInsert = new ContainerStorageCondition();
						containerStorageConditionInsert.setScontainercode(scontainercode);
						containerStorageConditionInsert.setSlocationhierarchy(slocationHierarchy);
						containerStorageConditionInsert.setNsamplestoragelocationcode((short) nlocationCode);
						containerStorageConditionInsert.setNstorageconditioncode((short) -1);

						String sectSeq = "select nsequenceno from seqnobasemaster where stablename='containerstoragecondition' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
								SeqNoBasemaster.class, jdbcTemplate);

						final int ncontainerStorageCode = objseq.getNsequenceno() + 1;

						insertQueryString = " insert into containerstoragecondition (ncontainerstoragecode, scontainercode, nstorageconditioncode,  nstatus, dmodifieddate, slocationhierarchy, nsamplestoragelocationcode,nsitecode) values ( "
								+ ncontainerStorageCode + " , '" + containerStorageConditionInsert.getScontainercode()
								+ "' , " + containerStorageConditionInsert.getNstorageconditioncode() + ", 1, '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "','"
								+ containerStorageConditionInsert.getSlocationhierarchy() + "', "
								+ containerStorageConditionInsert.getNsamplestoragelocationcode() + ", "
								+ userInfo.getNtranssitecode() + " )";

						jdbcTemplate.execute(insertQueryString);

						sectSeq = "update seqnobasemaster set nsequenceno=" + ncontainerStorageCode
								+ " where stablename='containerstoragecondition'";
						jdbcTemplate.execute(sectSeq);
					}
				}

			}
			if (inputMap.get("containerpath") != null) {
				final int nsamplestoragelocationcode = (int) ((Map<String, Object>) inputMap
						.get("sampleStorageVersion")).get("nsamplestoragelocationcode");

				final int nsamplestorageversioncode = (int) ((Map<String, Object>) inputMap.get("sampleStorageVersion"))
						.get("nsamplestorageversioncode");
				final int nprojecttypecode = (int) ((Map<String, Object>) inputMap.get("selectedSampleStorageLocation"))
						.get("nprojecttypecode");
				final String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragecontainerpath' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
						SeqNoBasemaster.class, jdbcTemplate);

				final int nsamplestoragecontainerpathcode = objseq.getNsequenceno() + 1;

				String insertQuery = " insert into samplestoragecontainerpath " + " select generate_series("
						+ nsamplestoragecontainerpathcode + ","
						+ (objseq.getNsequenceno() + (int) inputMap.get("containerpathsize"))
						+ " ) as  nsamplestoragecontainerpathcode," + nsamplestoragelocationcode
						+ " as nsamplestoragelocationcode, " + nsamplestorageversioncode
						+ "  as nsamplestorageversioncode, " + " json_array_elements('" + inputMap.get("containerpath")
						+ "')  as jsondata, " + " json_array_elements('" + inputMap.get("containerlastnode")
						+ "')#>>'{}' as scontainerlastnode,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' as dmodifieddate, " + deleteSampleStorageLocation.getNsitecode() +  " as nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus ;";
				insertQuery += " CREATE TABLE IF NOT EXISTS  samplestoragetransaction_" + nsamplestoragelocationcode
						+ " PARTITION OF samplestoragetransaction  " + "  FOR VALUES IN (" + nsamplestoragelocationcode
						+ ");";
				insertQuery += " CREATE TABLE IF NOT EXISTS  samplestoragemapping_"
						+ (nprojecttypecode == -1 ? 0 : nprojecttypecode) + " PARTITION OF samplestoragemapping  "
						+ "  FOR VALUES IN (" + nprojecttypecode + ");";
				System.out.print(insertQuery);
				final String updateQuery = " update seqnobasemaster set nsequenceno= (select max(nsamplestoragecontainerpathcode) "
						+ " from samplestoragecontainerpath) where stablename='samplestoragecontainerpath';";

				jdbcTemplate.execute(insertQuery + updateQuery);
				
				final String sSetting = "select ssettingvalue from settings where nsettingcode ="+Enumeration.Settings.INSTRUMENT_BASED_STORAGE.getNsettingcode()+" and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				//int nsiteNeed = 4;

				final String sNeedStorageFlow = (String) jdbcUtilityFunction.queryForObject(sSetting, String.class, jdbcTemplate);
				final int nNeedStorageFlow = Integer.parseInt(sNeedStorageFlow);

				if ((int) ((Map<String, Object>) inputMap.get("selectedSampleStorageLocation"))
						.get("nneedautomapping") == Enumeration.TransactionStatus.YES.gettransactionstatus() && nNeedStorageFlow==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					Map<String, Object> outputMap = new HashMap<>();
					final String ssamplestoragecontainerpathcode = jdbcTemplate
							.queryForObject(" select JSONb_AGG(x.arrays) from  " + "	(select (generate_series("
									+ nsamplestoragecontainerpathcode + ","
									+ (objseq.getNsequenceno() + (int) inputMap.get("containerpathsize"))
									+ ")) as arrays )x", String.class);

					outputMap = (Map<String, Object>) inputMap.get("selectedSampleStorageLocation");
					outputMap.put("containerpathsize", inputMap.get("containerpathsize"));
					outputMap.put("nsamplestoragecontainerpathcode", ssamplestoragecontainerpathcode);
					outputMap.put("sboxid", "");
					outputMap.put("nsitecode", deleteSampleStorageLocation.getNsitecode());

					sampleStorageMappingDAO.createSampleStorageMapping(outputMap, userInfo);
				}
			}
			return new ResponseEntity<>(getSampleStorageStructure(sampleStorageVersion.getNsamplestoragelocationcode(),
					sampleStorageLocation.getNstoragecategorycode(),
					sampleStorageVersion.getNsamplestorageversioncode(), userInfo).getBody(), HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "nsamplestoragelocationcode": 46
	 *                 }, "nsamplestoragelocationcode": 46, "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 31,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46, "jsondata": {
	 *                 "nprojecttypecode": -1, "nquantity": 0, "nunitcode": -1,
	 *                 "nproductcode": -1, "nneedautomapping": 4, "nneedposition":
	 *                 4, "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "nrow": 1, "ncolumn": 1, "ndirectionmastercode": 1,
	 *                 "nnoofcontainer": 1 } }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagetypecode": "en-US", "slanguagename":
	 *                 "English", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
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
	 *                 * @return response entity object holding response status and
	 *                 list of all StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> copySampleStorageVersion(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();

		final SampleStorageVersion deletedSampleStorageVersion = sampleStorageCommons
				.getActiveVersion(sampleStorageVersion.getNsamplestorageversioncode());
		if (deletedSampleStorageVersion == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		String query = "";

		query = "select *  from SampleStorageVersion where nsamplestoragelocationcode = "
				+ sampleStorageVersion.getNsamplestoragelocationcode() + " and nsamplestorageversioncode = "
				+ sampleStorageVersion.getNsamplestorageversioncode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageVersion sampleStorageVersionGet = (SampleStorageVersion) jdbcUtilityFunction
				.queryForObject(query, SampleStorageVersion.class, jdbcTemplate);

		final String sQuery = " lock table locksamplestoragelocation "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);

		String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestorageversion' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
				SeqNoBasemaster.class, jdbcTemplate);
		final int nsampleStorageVersionCode = objseq.getNsequenceno() + 1;

		final JSONObject jsonObject = new JSONObject(sampleStorageVersionGet.getJsondata());

		jsonObject.put("additionalinfo", sampleStorageVersion.getJsondata());

		query = " insert into samplestorageversion (nsamplestorageversioncode, nsamplestoragelocationcode, nversionno, napprovalstatus, jsondata, nstatus, nsitecode, dmodifieddate) values ( "
				+ nsampleStorageVersionCode + " , " + "" + sampleStorageVersion.getNsamplestoragelocationcode()
				+ ", 0, " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + jsonObject + "',1,"
				+ userInfo.getNtranssitecode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "') ";

		jdbcTemplate.execute(query);

		sectSeq = "update seqnobasemaster set nsequenceno=" + nsampleStorageVersionCode
				+ " where stablename='samplestorageversion'";
		jdbcTemplate.execute(sectSeq);

		savedUnitList.add(sampleStorageVersion);

		multilingualIDList.add("IDS_COPYSAMPLESTORAGEVERSION");

		auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);

		return new ResponseEntity<>(getSampleStorageStructure(sampleStorageVersion.getNsamplestoragelocationcode(),
				sampleStorageLocation.getNstoragecategorycode(), 0, userInfo).getBody(), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> getProjectType(final UserInfo userInfo) throws Exception {
		final String strQuery = "select * from ProjectType  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nprojecttypecode >0 ";

		final List<String> lstcolumns = new ArrayList<>();
		lstcolumns.add("sdisplaystatus");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProjectType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> getProduct(final UserInfo userInfo) throws Exception {
		final String strQuery = "select * from product  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nproductcode >0 ";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Product()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> addinfoSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		String insertQueryString = "";
		final SampleStorageStructure deleteSampleStorageLocation = sampleStorageCommons
				.getActiveLocation(sampleStorageLocation.getNsamplestoragelocationcode());

		if (deleteSampleStorageLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		final String strQuery = "select nsamplestoragelocationcode from samplestoragelocation where ssamplestoragelocationname = N'"
				+ stringUtilityFunction.replaceQuote(sampleStorageLocation.getSsamplestoragelocationname())
				+ "' and nsamplestoragelocationcode <> " + sampleStorageLocation.getNsamplestoragelocationcode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNtranssitecode();

		final List<SampleStorageStructure> sampleStorageLocationList = jdbcTemplate.query(strQuery,
				new SampleStorageStructure());

		if (sampleStorageLocationList.isEmpty()) {

			final String sQuery = " lock table locksamplestoragelocation "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
			jdbcTemplate.execute(sQuery);

			final Map<String, Object> selectedSamplLocation = (Map<String, Object>) getSelectedSampleStorageStructure(
					sampleStorageLocation.getNsamplestoragelocationcode(), 0, userInfo).getBody();
			final int nnoofcontainer = sampleStorageLocation.getNnoofcontainer() == 0 ? 1
					: (int) sampleStorageLocation.getNnoofcontainer();

			int nrow;
			int ncolumn;
			if (nnoofcontainer == 1) {
				nrow = 1;
				ncolumn = 1;
			} else {
				nrow = sampleStorageLocation.getNrow();
				ncolumn = sampleStorageLocation.getNcolumn();
			}
			insertQueryString = " update samplestoragelocation set  " + " 	ncontainertypecode = "
					+ sampleStorageLocation.getNcontainertypecode() + " ,	nrow = " + nrow + " ,	ncolumn = "
					+ ncolumn + ",	ncontainerstructurecode = " + sampleStorageLocation.getNcontainerstructurecode()
					+ ",	nneedposition=  " + sampleStorageLocation.getNneedposition() + " ,   nquantity= "
					+ sampleStorageLocation.getNquantity() + " ,   nnoofcontainer= " + nnoofcontainer
					+ " ,   nunitcode= " + sampleStorageLocation.getNunitcode() + " , nneedautomapping="
					+ sampleStorageLocation.getNneedautomapping() + "	,ndirectionmastercode ="
					+ sampleStorageLocation.getNdirectionmastercode() + ",nprojecttypecode = "
					+ sampleStorageLocation.getNprojecttypecode() + ",nproductcode = "
					+ sampleStorageLocation.getNproductcode() + " where nsamplestoragelocationcode = "
					+ sampleStorageLocation.getNsamplestoragelocationcode() + "";

			jdbcTemplate.execute(insertQueryString);

			final JSONObject jsondataObject = new JSONObject(sampleStorageVersion.getJsondata());
			jsondataObject.put("nrow", nrow);
			jsondataObject.put("ncolumn", ncolumn);
			jsondataObject.put("nnoofcontainer", nnoofcontainer);

			final String versionupdateQuery = " update samplestorageversion set  "
					+ " 	jsondata = jsonb_set(jsondata,'{additionalinfo}','"
					+ stringUtilityFunction.replaceQuote(jsondataObject.toString()) + "',false)"
					+ " where nsamplestorageversioncode = " + sampleStorageVersion.getNsamplestorageversioncode() + "";
			jdbcTemplate.execute(versionupdateQuery);

			listAfterUpdate.add(sampleStorageLocation);
			listBeforeUpdate.add(selectedSamplLocation.get("selectedSampleStorageLocation"));

			multilingualIDList.add("IDS_ADDITIONALINFORMATION");

			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);

			boolean bcheckCategoryUpdate = true;
			if (selectedSamplLocation.get("selectedSampleStorageLocation") != null) {
				final Map<String, Object> getselectedSamplLocation = (Map<String, Object>) selectedSamplLocation
						.get("selectedSampleStorageLocation");
				if ((int) getselectedSamplLocation.get("nstoragecategorycode") == sampleStorageLocation
						.getNstoragecategorycode()) {
					bcheckCategoryUpdate = false;
				}
			}
			int nversionCode = 0;
			if (bcheckCategoryUpdate == true) {
				nversionCode = 0;
			} else {
				nversionCode = sampleStorageVersion.getNsamplestorageversioncode();
			}
			return new ResponseEntity<>(
					getSampleStorageStructure(sampleStorageLocation.getNsamplestoragelocationcode(),
							sampleStorageLocation.getNstoragecategorycode(), nversionCode, userInfo).getBody(),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
	
	//ALPDJ21-32-comment by Vignesh(23-07-2025)-->There is no need to fetch the Site ID and Instrument ID.
	/*
	//ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table. -- Start

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1,"ninstrumentcode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 
	@Override
	public ResponseEntity<Object> getInstrumentID(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		//ALPDJ21-23
		//Storage Category, Instrument Category and Storage Structure Screens mapping enhancements
		final String sQuery = "select i.ninstrumentcode,i.sinstrumentid from instrumentcategory ic,instrument i ,storagecategory sc  "
				+ " where sc.ninstrumentcatcode=ic.ninstrumentcatcode and i.ninstrumentcatcode=ic.ninstrumentcatcode and sc.nstoragecategorycode="
				+ inputMap.get("nstoragecategorycode") + " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ic.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and sc.nsitecode="+userInfo.getNmastersitecode()+" and "
				+ " i.nsitecode= "+userInfo.getNmastersitecode()+" order by 1 desc;";

		return new ResponseEntity<Object>(jdbcTemplate.query(sQuery, new Instrument()), HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1,"ninstrumentcode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	
	@Override
	public ResponseEntity<Object> getsite(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		String sQuery = "";

		final String query = "select ssettingvalue from settings where nsettingcode =84 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		int nsiteNeed = 4;

		final Settings settings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);
		nsiteNeed = Integer.parseInt(settings.getSsettingvalue());
		//ALPDJ21-23
		//Storage Category, Instrument Category and Storage Structure Screens mapping enhancements
		if (nsiteNeed == 3) {
			sQuery = " select s.ssitename as sregionalsitename,s.nsitecode as nregionalsitecode from site s, instrument i,instrumentcategory ic ,storagecategory sc "
					+ " where sc.ninstrumentcatcode=ic.ninstrumentcatcode and i.ninstrumentcatcode=ic.ninstrumentcatcode"
					+ " and s.nsitecode=i.nregionalsitecode and i.ninstrumentcode=" + inputMap.get("ninstrumentid")
					+ " and  sc.nstoragecategorycode=" + inputMap.get("nstoragecategorycode") + " and s.nsitecode>0 "
					+ " and i.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
					+ " ic.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "	 and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and ic.nsitecode="+userInfo.getNmastersitecode()
					+ " and sc.nsitecode="+userInfo.getNmastersitecode()+" and "
					+ "	i.nsitecode= "+userInfo.getNmastersitecode()+" and s.nmastersitecode="+userInfo.getNmastersitecode()+" order by 1 desc;";
		} else {
			sQuery = "select s.ssitename as sregionalsitename,s.nsitecode as nregionalsitecode from site s where  s.nsitecode>0  order by 1 desc ";
		}

		return new ResponseEntity<Object>(jdbcTemplate.query(sQuery, new Instrument()), HttpStatus.OK);

	}
	
	//ATE234 Janakumar BGSI-10->Storage Structure → Instrument id & Site combo load based on Setting table.   -- END
	*/
}