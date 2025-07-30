package com.agaramtech.qualis.configuration.service.sitehierarchyconfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.SiteHierarchyConfig;
import com.agaramtech.qualis.configuration.model.SiteHierarchyConfigDetails;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SiteHierarchyConfigurationDAOImpl implements SiteHierarchyConfigurationDAO {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(SiteHierarchyConfigurationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private ValidatorDel valiDatorDel;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public ResponseEntity<Object> getSiteHierarchyConfiguration(final UserInfo userInfo, int nsitehierarchyconfigcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String siteConfigQuery = "select ss.nsitehierarchyconfigcode, ss.sconfigname,ss.jsondata,ss.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  from sitehierarchyconfig ss,transactionstatus ts "
				+ " where ss.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ss.ntransactionstatus= ts.ntranscode and ss.nsitecode=" + userInfo.getNmastersitecode()
				+ " order by 1 ";
		final List<SiteHierarchyConfig> siteConfigQueryList = (List<SiteHierarchyConfig>) jdbcTemplate
				.query(siteConfigQuery, new SiteHierarchyConfig());

		if (nsitehierarchyconfigcode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			outputMap.put("SiteHierarchyConfig", siteConfigQueryList);
			if (!siteConfigQueryList.isEmpty()) {
				outputMap.put("selectedSiteHierarchyConfig", siteConfigQueryList.get(siteConfigQueryList.size() - 1));
			} else {
				outputMap.put("selectedSiteHierarchyConfig", null);
			}
		} else {
			SiteHierarchyConfig selectedSiteHierarchyConfig = getActiveSiteHierarchyConfigById(nsitehierarchyconfigcode,
					userInfo);
			outputMap.put("selectedSiteHierarchyConfig", selectedSiteHierarchyConfig);
			outputMap.put("SiteHierarchyConfig", siteConfigQueryList);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSiteHierarchy(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		int nsitetypecode = (int) inputMap.get("nsitetypecode");
		final String nsitecode = inputMap.get("nsitecode").toString();
		String siteValue="";
		int norderNextNo = -1;
		int norderNo = jdbcTemplate.queryForObject(
				"select COALESCE(min(nhierarchicalorderno),-1) from sitetype where nhierarchicalorderno>0 and  nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		if((int)inputMap.get("nneedalltypesite")==Enumeration.TransactionStatus.YES.gettransactionstatus()){
			norderNextNo = jdbcTemplate.queryForObject(
				"select COALESCE(min(s.nhierarchicalorderno),-1) from siteconfig sc,sitetype s"
				+ "  where sc.nsitetypecode=s.nsitetypecode and s.nhierarchicalorderno > "+nsitetypecode+" and  s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and sc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		}
		if((int)inputMap.get("nneedalltypesite")==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		if (nsitetypecode != Enumeration.TransactionStatus.NA.gettransactionstatus() && nsitecode != "") {
			siteValue= " and bbt.nhierarchicalorderno >  " + nsitetypecode + " and sc.nsitecode not in ( " + nsitecode + " ) ";
		  } 
		}else {
			nsitetypecode=((int)inputMap.get("nneedalltypesite")==Enumeration.TransactionStatus.YES.gettransactionstatus()
					&& nsitetypecode == Enumeration.TransactionStatus.NA.gettransactionstatus())? norderNo :
						norderNextNo;
			String ssitecode= nsitecode != ""?" and sc.nsitecode not in ( " + nsitecode + " ) ":"";
			siteValue= " and bbt.nhierarchicalorderno =  " + nsitetypecode + " "+ssitecode;
			
		}
		
		final String	siteConfigQuery = "select  sc.nsitetypecode,s.nsitecode,concat(s.ssitename,'(',bbt.ssitetypename,')') as ssitename  ,bbt.nhierarchicalorderno as nhierarchicalorderno,"
					+ " bbt.ssitetypename,s.ssitename as ssiteconfigname from site s,siteconfig sc ,sitetype bbt where bbt.nsitetypecode=sc.nsitetypecode "
					+ " and s.nsitecode=sc.nsitecode and s.nsitecode > 0 and bbt.nsitetypecode>0  and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and sc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode="
					+ userInfo.getNmastersitecode() + " and bbt.nsitecode =" + userInfo.getNmastersitecode()+ " "
					+ siteValue+ " order by bbt.nhierarchicalorderno  ";

		final List<Site> siteConfigQueryList = (List<Site>) jdbcTemplate.query(siteConfigQuery, new Site());
		outputMap.put("SiteHierarchy", siteConfigQueryList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table locksitehierarchyconfig "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		String insertsitedetails = "";

		final SiteHierarchyConfig objSiteHierarchyConfig = objmapper.convertValue(inputMap.get("sitehierarchyconfig"),
				SiteHierarchyConfig.class);
		final List<SiteHierarchyConfigDetails> objSiteHierarchyConfigDetails = objmapper.convertValue(
				inputMap.get("sitehierarchyconfigdetails"), new TypeReference<List<SiteHierarchyConfigDetails>>() {
				});

		final SiteHierarchyConfig objSiteHierarchyConfigListByName = getSiteHierarchyConfigByName(
				objSiteHierarchyConfig.getSconfigname(), userInfo);

		if (objSiteHierarchyConfigListByName == null) {
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoconfigurationmaster where stablename='sitehierarchyconfig' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			nSeqNo++;

			int nversionSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoconfigurationmaster where stablename='sitehierarchyconfigdetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			final JSONObject jsonObject = new JSONObject(objSiteHierarchyConfig.getJsondata());
			final String siteHierarchyConfigInsert = "insert into sitehierarchyconfig(nsitehierarchyconfigcode, sconfigname, "
					+ " jsondata,dmodifieddate, nsitecode, nstatus,ntransactionstatus,nneedalltypesite)" + " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objSiteHierarchyConfig.getSconfigname()) + "','" + jsonObject
					+ "' ,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","+objSiteHierarchyConfig.getNneedalltypesite()+");";

			for (SiteHierarchyConfigDetails objSiteHierarchyConfigDetail : objSiteHierarchyConfigDetails) {
				nversionSeqNo++;

				insertsitedetails = insertsitedetails + " (" + nversionSeqNo + "," + nSeqNo + ","
						+ objSiteHierarchyConfigDetail.getNnodesitecode() + ","
						+ objSiteHierarchyConfigDetail.getNparentsitecode() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
			}
			insertsitedetails = "insert into sitehierarchyconfigdetails(nsitehierarchyconfigdetailcode,nsitehierarchyconfigcode,nnodesitecode,nparentsitecode"
					+ ",dmodifieddate,nstatus) values " + insertsitedetails.substring(0, insertsitedetails.length() - 1)
					+ ";";
			final String updateseqNo = "update seqnoconfigurationmaster set nsequenceno = " + nSeqNo
					+ " where stablename='sitehierarchyconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String updateVersionSeqNo = "update seqnoconfigurationmaster set nsequenceno = " + nversionSeqNo
					+ " where stablename='sitehierarchyconfigdetails' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(siteHierarchyConfigInsert + insertsitedetails + updateseqNo + updateVersionSeqNo);

			final String siteConfigName = getSiteHierarchyConfigName(inputMap, userInfo);

			objSiteHierarchyConfig.setNsitehierarchyconfigcode(nSeqNo);
			objSiteHierarchyConfig.setSsitename(siteConfigName);
			savedList.add(objSiteHierarchyConfig);
			multilingualIDList.add("IDS_ADDSITEHIERARCHYCONFIG");

			auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
			return getSiteHierarchyConfiguration(userInfo, -1);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private SiteHierarchyConfig getSiteHierarchyConfigByName(final String sconfigname, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select  sconfigname from sitehierarchyconfig where sconfigname = N'"
				+ stringUtilityFunction.replaceQuote(sconfigname) + "'" + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (SiteHierarchyConfig) jdbcUtilityFunction.queryForObject(strQuery, SiteHierarchyConfig.class,
				jdbcTemplate);
	}

	private String getSiteHierarchyConfigName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> siteValue = objmapper.convertValue(inputMap.get("ssitecode"),
				new TypeReference<Map<String, Object>>() {
				});
		final String siteConfig = "select STRING_AGG(concat(s.ssitename,'(',st.ssitetypename,')'), ', ') AS ssitename from site s ,siteconfig sc,sitetype st "
				+ " where s.nsitecode in (" + siteValue.get("key") + ") and  s.nsitecode=sc.nsitecode  and st.nsitetypecode=sc.nsitetypecode and s.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nmastersitecode="
				+ userInfo.getNmastersitecode() + " and st.nsitecode="+userInfo.getNmastersitecode()+" and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and "
				+ " sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" ;";

		return (String) jdbcUtilityFunction.queryForObject(siteConfig, String.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> approveSiteHierarchyConfig(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SiteHierarchyConfig objSiteHierarchyConfig = objmapper.convertValue(inputMap.get("sitehierarchyconfig"),
				SiteHierarchyConfig.class);

		final SiteHierarchyConfig isActiveSiteHierarchyConfig = getActiveSiteHierarchyConfigById(
				objSiteHierarchyConfig.getNsitehierarchyconfigcode(), userInfo);

		if (isActiveSiteHierarchyConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}else {
			if (isActiveSiteHierarchyConfig.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED",
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}else {
			String query = "select * from sitehierarchyconfigdetails where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " nsitehierarchyconfigcode =" + objSiteHierarchyConfig.getNsitehierarchyconfigcode();
			final List<SiteHierarchyConfigDetails> SiteHierarchyConfigDetailsList = jdbcTemplate.query(query,
					new SiteHierarchyConfigDetails());
			String updateQuery = "";
			String updateQueryString="";
			for (SiteHierarchyConfigDetails value : SiteHierarchyConfigDetailsList) {

				final Map<String, Object> reverseParent = objmapper.convertValue(inputMap.get("reverseParent"),
						new TypeReference<Map<String, Object>>() {
						});

				final Map<String, Object> ChildNode = objmapper.convertValue(inputMap.get("ChildNode"),
						new TypeReference<Map<String, Object>>() {
						});

				final String sparentsitecode = reverseParent.containsKey(String.valueOf(value.getNnodesitecode()))
						? (String) reverseParent.get(String.valueOf(value.getNnodesitecode()))
						: "";
				final String schildsitecode = ChildNode.containsKey(String.valueOf(value.getNnodesitecode()))
						? (String) ChildNode.get(String.valueOf(value.getNnodesitecode()))
						: "";

				updateQuery = updateQuery + "update sitehierarchyconfigdetails set sparentsitecode='" + sparentsitecode
						+ "' ,schildsitecode='" + schildsitecode + "' " + "  where nsitehierarchyconfigcode="
						+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + ""
						+ " and nsitehierarchyconfigdetailcode=" + value.getNsitehierarchyconfigdetailcode() + " ;";
			}
			updateQueryString = updateQueryString+"  update sitehierarchyconfig set  dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " where nsitehierarchyconfigcode="
					+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + " ;";
			
			if((boolean) inputMap.get("isneedtoretire")) {
				
				updateQueryString=updateQueryString+" update sitehierarchyconfig set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,ntransactionstatus = "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " where nsitehierarchyconfigcode in "
						+ " (select nsitehierarchyconfigcode from sitehierarchyconfig where ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and "
						+ " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitehierarchyconfigcode <> "
						+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + ") ;";
			}

			jdbcTemplate.execute(updateQueryString + updateQuery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedList = new ArrayList<>();

			final String siteConfigName = getSiteHierarchyConfigName(inputMap, userInfo);
			objSiteHierarchyConfig.setSsitename(siteConfigName);
			objSiteHierarchyConfig
					.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
			savedList.add(objSiteHierarchyConfig);
			multilingualIDList.add("IDS_APPROVESITEHIERARCHYCONFIG");
			auditUtilityFunction.fnInsertAuditAction(savedList, 2, Arrays.asList(isActiveSiteHierarchyConfig),
					multilingualIDList, userInfo);

			return getSiteHierarchyConfiguration(userInfo, objSiteHierarchyConfig.getNsitehierarchyconfigcode());
			
			}
		}
	}

	public ResponseEntity<Object> deleteSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SiteHierarchyConfig objSiteHierarchyConfig = objmapper.convertValue(inputMap.get("sitehierarchyconfig"),
				SiteHierarchyConfig.class);

		final SiteHierarchyConfig isActiveSiteHierarchyConfig = getActiveSiteHierarchyConfigById(
				objSiteHierarchyConfig.getNsitehierarchyconfigcode(), userInfo);

		if (isActiveSiteHierarchyConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		} else {

			final String query = "select 'IDS_PROJECTANDSITEHIERARCHYMAPPING' as Msg from projectsitehierarchymapping where"
					+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitehierarchyconfigcode = " + objSiteHierarchyConfig.getNsitehierarchyconfigcode();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue",
						Integer.toString(objSiteHierarchyConfig.getNsitehierarchyconfigcode()));
				objOneToManyValidation.put("stablename", "projectsitehierarchymapping");

				valiDatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);

				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				String updateQuery = "";
				updateQuery = "update sitehierarchyconfig set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nsitehierarchyconfigcode="
						+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + " ;";

				updateQuery = updateQuery + "update sitehierarchyconfigdetails set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where  nsitehierarchyconfigcode="
						+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + ";";
				jdbcTemplate.execute(updateQuery);

				final String siteConfigName = getSiteHierarchyConfigName(inputMap, userInfo);
				objSiteHierarchyConfig.setSsitename(siteConfigName);

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedList = new ArrayList<>();
				deletedList.add(objSiteHierarchyConfig);
				multilingualIDList.add("IDS_DELETESITEHIERARCHYCONFIG");

				auditUtilityFunction.fnInsertAuditAction(deletedList, 1, null, multilingualIDList, userInfo);

				return getSiteHierarchyConfiguration(userInfo, -1);
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	@Override
	public SiteHierarchyConfig getActiveSiteHierarchyConfigById(final int nsitehierarchyconfigcode,
			final UserInfo userInfo) throws Exception {

		final String strQuery = "select ss.nsitehierarchyconfigcode, ss.sconfigname,ss.jsondata,ss.ntransactionstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,ss.nneedalltypesite  from sitehierarchyconfig ss,transactionstatus ts "
				+ " where ss.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ss.ntransactionstatus= ts.ntranscode and ss.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ss.nsitehierarchyconfigcode =" + nsitehierarchyconfigcode;

		return (SiteHierarchyConfig) jdbcUtilityFunction.queryForObject(strQuery, SiteHierarchyConfig.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		String insertsitedetails = "";
		String updateQuery = "";

		final SiteHierarchyConfig objSiteHierarchyConfig = objmapper.convertValue(inputMap.get("sitehierarchyconfig"),
				SiteHierarchyConfig.class);
		final List<SiteHierarchyConfigDetails> objSiteHierarchyConfigDetails = objmapper.convertValue(
				inputMap.get("sitehierarchyconfigdetails"), new TypeReference<List<SiteHierarchyConfigDetails>>() {
				});

		final SiteHierarchyConfig isActiveSiteHierarchyConfig = getActiveSiteHierarchyConfigById(
				objSiteHierarchyConfig.getNsitehierarchyconfigcode(), userInfo);
		if (isActiveSiteHierarchyConfig != null) {

			final String strQuery = "select  sconfigname from sitehierarchyconfig where sconfigname = N'"
					+ stringUtilityFunction.replaceQuote(objSiteHierarchyConfig.getSconfigname()) + "'"
					+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nsitehierarchyconfigcode <> "
					+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final SiteHierarchyConfig objSiteHierarchyConfigListByName = (SiteHierarchyConfig) jdbcUtilityFunction
					.queryForObject(strQuery, SiteHierarchyConfig.class, jdbcTemplate);

			if (objSiteHierarchyConfigListByName == null) {

				String query = "select nnodesitecode ,nparentsitecode,nsitehierarchyconfigdetailcode from sitehierarchyconfigdetails "
						+ " where nsitehierarchyconfigcode=" + objSiteHierarchyConfig.getNsitehierarchyconfigcode();
				final List<SiteHierarchyConfigDetails> SiteHierarchyConfigDetailsList = jdbcTemplate.query(query,
						new SiteHierarchyConfigDetails());

				final List<SiteHierarchyConfigDetails> match = objSiteHierarchyConfigDetails.stream()
						.filter(x -> SiteHierarchyConfigDetailsList.stream()
								.noneMatch(y -> (y.getNnodesitecode() == x.getNnodesitecode()
										&& y.getNparentsitecode() == x.getNparentsitecode())))
						.collect(Collectors.toList());

				final List<SiteHierarchyConfigDetails> deleteNotMatch = SiteHierarchyConfigDetailsList.stream()
						.filter(x -> objSiteHierarchyConfigDetails.stream()
								.noneMatch(y -> (y.getNnodesitecode() == x.getNnodesitecode()
										&& y.getNparentsitecode() == x.getNparentsitecode())))
						.collect(Collectors.toList());

				final JSONObject jsonObject = new JSONObject(objSiteHierarchyConfig.getJsondata());
				final String updateQuerySiteHierarchyConfig = " update sitehierarchyconfig set " + " sconfigname= '"
						+ stringUtilityFunction.replaceQuote(objSiteHierarchyConfig.getSconfigname()) + "' ,"
						+ " jsondata= '" + stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'"
						+ " ,dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where nsitehierarchyconfigcode =" + objSiteHierarchyConfig.getNsitehierarchyconfigcode()
						+ ";";

				if (!match.isEmpty()) {
					int nversionSeqNo = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnoconfigurationmaster where stablename='sitehierarchyconfigdetails' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);

					for (SiteHierarchyConfigDetails objSiteHierarchyConfigDetail : match) {
						nversionSeqNo++;
						insertsitedetails = insertsitedetails + " (" + nversionSeqNo + ","
								+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + ","
								+ objSiteHierarchyConfigDetail.getNnodesitecode() + ","
								+ objSiteHierarchyConfigDetail.getNparentsitecode() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					}
					insertsitedetails = "insert into sitehierarchyconfigdetails(nsitehierarchyconfigdetailcode,nsitehierarchyconfigcode,nnodesitecode,nparentsitecode"
							+ ",dmodifieddate,nstatus) values "
							+ insertsitedetails.substring(0, insertsitedetails.length() - 1) + ";";
					updateQuery = updateQuery + " update seqnoconfigurationmaster set nsequenceno = " + nversionSeqNo
							+ " where stablename='sitehierarchyconfigdetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				}
				if (!deleteNotMatch.isEmpty()) {
					final String nsitehierarchyconfigdetailcode = deleteNotMatch.stream()
							.map(x -> String.valueOf(x.getNsitehierarchyconfigdetailcode()))
							.collect(Collectors.joining(","));
					updateQuery = updateQuery + " update sitehierarchyconfigdetails set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
							+ " where nsitehierarchyconfigdetailcode in (" + nsitehierarchyconfigdetailcode + " );";
				}
				jdbcTemplate.execute(updateQuerySiteHierarchyConfig + insertsitedetails + updateQuery);

				final String siteConfigName = getSiteHierarchyConfigName(inputMap, userInfo);
				objSiteHierarchyConfig.setSsitename(siteConfigName);
				final Map<String, Object> siteValueBefore = objmapper.convertValue(inputMap.get("ssitecodebefore"),
						new TypeReference<Map<String, Object>>() {
						});
				final String siteConfigBefore = "select STRING_AGG(concat(s.ssitename,'(',st.ssitetypename,')'), ', ') AS ssitename from site s,siteconfig sc,sitetype st "
						+ " where s.nsitecode in (" + siteValueBefore.get("key") + ") and st.nsitetypecode=sc.nsitetypecode "
						+ " and s.nsitecode=sc.nsitecode and s.nsitecode>0 and s.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nmastersitecode="
						+ userInfo.getNmastersitecode() + " and st.nsitecode="+userInfo.getNmastersitecode()+" and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				final String siteConfigBeforeName = jdbcTemplate.queryForObject(siteConfigBefore, String.class);
				isActiveSiteHierarchyConfig.setSsitename(siteConfigBeforeName);

				final List<String> multilingualIDList = new ArrayList<>();

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(isActiveSiteHierarchyConfig);

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objSiteHierarchyConfig);

				multilingualIDList.add("IDS_EDITSITEHIERARCHYCONFIG");

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getSiteHierarchyConfiguration(userInfo, objSiteHierarchyConfig.getNsitehierarchyconfigcode());
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> retireSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SiteHierarchyConfig objSiteHierarchyConfig = objmapper.convertValue(inputMap.get("sitehierarchyconfig"),
				SiteHierarchyConfig.class);
		final SiteHierarchyConfig isActiveSiteHierarchyConfig = getActiveSiteHierarchyConfigById(
				objSiteHierarchyConfig.getNsitehierarchyconfigcode(), userInfo);

		if (isActiveSiteHierarchyConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			if (isActiveSiteHierarchyConfig.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYRETIRE",
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}else {
			final String query = "select 'IDS_PROJECTANDSITEHIERARCHYMAPPING' as Msg from projectsitehierarchymapping where"
					+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitehierarchyconfigcode = " + objSiteHierarchyConfig.getNsitehierarchyconfigcode();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				// ALPD-4513--Vignesh R(05-09-2024)
				Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue",
						Integer.toString(objSiteHierarchyConfig.getNsitehierarchyconfigcode()));
				objOneToManyValidation.put("stablename", "projectsitehierarchymapping");

				valiDatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);

				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				String updateQuery = "";
				updateQuery = "update sitehierarchyconfig set ntransactionstatus="
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nsitehierarchyconfigcode="
						+ objSiteHierarchyConfig.getNsitehierarchyconfigcode() + " ;";

				jdbcTemplate.execute(updateQuery);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedList = new ArrayList<>();
				final String siteConfigName = getSiteHierarchyConfigName(inputMap, userInfo);
				objSiteHierarchyConfig.setSsitename(siteConfigName);
				objSiteHierarchyConfig
						.setNtransactionstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
				savedList.add(objSiteHierarchyConfig);
				multilingualIDList.add("IDS_RETIRESITEHIERARCHYCONFIG");
				auditUtilityFunction.fnInsertAuditAction(savedList, 2, Arrays.asList(isActiveSiteHierarchyConfig),
						multilingualIDList, userInfo);

				return getSiteHierarchyConfiguration(userInfo, objSiteHierarchyConfig.getNsitehierarchyconfigcode());
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		  }

		}
	}

}
