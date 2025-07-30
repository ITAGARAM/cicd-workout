package com.agaramtech.qualis.configuration.service.templatemaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.configuration.model.SeqNoTemplateMasterVersion;
import com.agaramtech.qualis.configuration.model.TreeControl;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.configuration.model.TreetempTranstestGroup;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TemplateMasterDAOImpl implements TemplateMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateMasterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getProductCategoryTemplate(final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		final String strQuery = "select nsampletypecode,nsorter,coalesce(jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "') as ssampletypename"
				+ " ,nformcode from SampleType where nsampletypecode > 0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsampletypecode not in ("
				+ Enumeration.SampleType.GOODSIN.getType() + ", " + Enumeration.SampleType.STABILITY.getType() + ")" // ALPD-5412
				+ " order by nsorter;";
		LOGGER.info(strQuery);
		List<SampleType> sampleTypeList = (List<SampleType>) jdbcTemplate.query(strQuery, new SampleType());
		returnMap.put("lstSampleType", sampleTypeList);

		if (!sampleTypeList.isEmpty()) {
			returnMap.put("SelectedSampleFilter", sampleTypeList.get(0));
			returnMap.put("SelectedSample", sampleTypeList.get(0));
			returnMap.putAll(
					(Map<String, Object>) getSampleTypeProductCatrgory(sampleTypeList.get(0).getNsampletypecode(),
							userInfo).getBody());
			if (returnMap.get("SelectedTreeVersionTemplate") != null) {
				final TreeVersionTemplate selectedTreeVersionTemplate = (TreeVersionTemplate) returnMap
						.get("SelectedTreeVersionTemplate");
				final SampleType selectedSampleType = sampleTypeList.stream()
						.filter(item -> item.getNsampletypecode() == selectedTreeVersionTemplate.getNsampletypecode())
						.collect(Collectors.toList()).get(0);

				returnMap.put("SelectedSample", selectedSampleType);

			}
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleTypeProductCatrgory(final int nsampletypecode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		Map<String, Object> returnmap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		List<TreeVersionTemplate> lsttreeversiontemplate = new ArrayList<>();
		List<TreetempTranstestGroup> lstTreetempTranstestGroup = new ArrayList<>();

		String strQuery = " select et.splainquery,et.sexistingtablename,std.sdisplaylabelname, std.nsampletypecode from sampletypedesign std, existinglinktable et"
				+ " where  std.nexistinglinkcode = et.nexistingcode and std.nsampletypecode = " + nsampletypecode + " "
				+ " and std.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and et.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and std.nsitecode=" + userInfo.getNmastersitecode() + " and et.nsitecode="
				+ userInfo.getNmastersitecode();

		List<Map<String, Object>> lst = jdbcTemplate.queryForList(strQuery);
		if (!lst.isEmpty()) {
			final String squery = (String) lst.get(0).get("splainquery");
			final String sexistingtablename = (String) lst.get(0).get("sdisplaylabelname");
			List<?> lstExistingtable = jdbcTemplate.queryForList(squery);
			objMap.put(sexistingtablename, lstExistingtable);
			returnmap.put("lstcategory", objMap);

			if (!lstExistingtable.isEmpty()) {
				returnmap.put("SelectedCategoryFilter", lstExistingtable.get(0));
				Map<String, Object> map = objMapper.convertValue(lstExistingtable.get(0), Map.class);
				final int nformcode = (int) map.get("nformcode");
				final String categoryName = (String) map.get("1");
				returnmap.put("SelectedCategoryText", categoryName);
				returnmap.put("SelectedCategory", lstExistingtable.get(0));
				List<String> list = new ArrayList<String>(map.keySet());
				returnmap.put("SelectedCategoryFilterTextLabel", list.get(0));
				returnmap.put("SelectedCategoryFilterValueLabel", list.get(1));
				final int ncategory = (int) map.entrySet().stream().findFirst().get().getValue();
				returnmap.putAll((Map<String, Object>) getTemplateMasterVersion(-1, ncategory, nformcode,
						nsampletypecode, userInfo).getBody());

			} else {
				returnmap.put("SelectedCategoryFilter", null);
				returnmap.put("SelectedCategoryText", null);
				returnmap.put("SelectedCategory", null);
				returnmap.put("lstcategory", objMap);
				returnmap.put("lstTemplateMasterlevel", lsttreeversiontemplate);
				returnmap.put("lstTreeversionTemplate", lstTreetempTranstestGroup);
			}
		} else {
			returnmap.put("SelectedSampleFilter", null);
			returnmap.put("SelectedCategoryFilter", null);
			returnmap.put("SelectedCategoryText", null);
			returnmap.put("SelectedCategory", null);
			returnmap.put("lstcategory", objMap);
			returnmap.put("lstTemplateMasterlevel", lsttreeversiontemplate);
			returnmap.put("lstTreeversionTemplate", lstTreetempTranstestGroup);
		}

		return new ResponseEntity<>(returnmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSampleTypeComboChange(final int nsampletypecode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		Map<String, Object> returnmap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();

		String strQuery1 = "select nsampletypecode," + "coalesce(jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "jsondata->'sampletypename'->>'en-US') as ssampletypename"
				+ " ,nformcode from SampleType where nsampletypecode =" + nsampletypecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final SampleType sampleType = (SampleType) jdbcTemplate.queryForObject(strQuery1, new SampleType());
		returnmap.put("SelectedSampleFilter", sampleType);

		String strQuery = " select et.splainquery,et.sexistingtablename,std.sdisplaylabelname, std.nsampletypecode from sampletypedesign std, existinglinktable et"
				+ " where  std.nexistinglinkcode = et.nexistingcode and std.nsampletypecode = " + nsampletypecode + " "
				+ " and std.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and et.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and std.nsitecode=" + userInfo.getNmastersitecode() + " and et.nsitecode="
				+ userInfo.getNmastersitecode();

		List<Map<String, Object>> lst = jdbcTemplate.queryForList(strQuery);
		if (lst.size() > 0) {
			final String squery = (String) lst.get(0).get("splainquery");
			final String sexistingtablename = (String) lst.get(0).get("sdisplaylabelname");
			List<?> lstExistingtable = jdbcTemplate.queryForList(squery);
			objMap.put(sexistingtablename, lstExistingtable);
			returnmap.put("lstcategory", objMap);

			if (!lstExistingtable.isEmpty()) {
				returnmap.put("SelectedCategoryFilter", lstExistingtable.get(0));
				Map<String, Object> map = objMapper.convertValue(lstExistingtable.get(0), Map.class);
				final int nformcode = (int) map.get("nformcode");
				final int ncategory = (int) map.entrySet().stream().findFirst().get().getValue();
				List<String> list = new ArrayList<String>(map.keySet());

				returnmap.put("nformcode", nformcode);
				returnmap.put("ncategorycode", ncategory);
				returnmap.put("SelectedCategoryFilterTextLabel", list.get(0));
				returnmap.put("SelectedCategoryFilterValueLabel", list.get(1));
			} else {
				returnmap.put("SelectedCategoryFilter", null);
				returnmap.put("lstcategory", objMap);
			}
		} else {
			returnmap.put("SelectedCategoryFilter", null);
			returnmap.put("lstcategory", objMap);
		}
		return new ResponseEntity<>(returnmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTemplateMasterVersion(final int ntreeversiontempcode, final int ncategorycode,
			final int nformcode, final int nsampletypecode, final UserInfo userInfo) throws Exception {
		Map<String, Object> returnmap = new HashMap<String, Object>();
		final String squery = "select tvt.*, case when nversionno>0 then cast(nversionno as varchar(15)) else '-' end  sversionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus "
				+ " from treeversiontemplate tvt,treetemplatemaster ttm,"
				+ " transactionstatus ts where  ttm.ntemplatecode=tvt.ntemplatecode and ts.ntranscode=tvt.ntransactionstatus and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.ncategorycode ="
				+ ncategorycode + " and tvt.nsampletypecode=" + nsampletypecode + " and nformcode =" + nformcode + " "
				+ " and tvt.nsitecode = " + userInfo.getNmastersitecode() + " and ttm.nsitecode="
				+ userInfo.getNmastersitecode() + " and ttm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by tvt.ntreeversiontempcode;";

		List<TreeVersionTemplate> lsttreeversiontemplate = (List<TreeVersionTemplate>) jdbcTemplate.query(squery,
				new TreeVersionTemplate());
		returnmap.put("lstTreeversionTemplate", lsttreeversiontemplate);
		if (ntreeversiontempcode > 0 && ncategorycode > 0) {
			returnmap.put("SelectedTreeVersionTemplate", null);
			returnmap.putAll((Map<String, Object>) getTemplateVersionById(ntreeversiontempcode, userInfo).getBody());
		} else {
			if (!lsttreeversiontemplate.isEmpty()) {
				final TreeVersionTemplate selectedTreeVersionTemplate = lsttreeversiontemplate
						.get(lsttreeversiontemplate.size() - 1);
				returnmap.put("SelectedTreeVersionTemplate", selectedTreeVersionTemplate);
				returnmap.putAll((Map<String, Object>) getTemplateVersionById(
						selectedTreeVersionTemplate.getNtreeversiontempcode(), userInfo).getBody());
			} else {
				returnmap.put("SelectedTreeVersionTemplate", null);
				returnmap.put("lstTemplateMasterlevel", lsttreeversiontemplate);
			}
		}
		return new ResponseEntity<>(returnmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTemplateVersionById(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnmap = new HashMap<>();

		final String squery = "select tvt.*, case when nversionno>0 then cast(nversionno as varchar(15)) else '-' end  sversionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus "
				+ " from treeversiontemplate tvt, transactionstatus ts where  ts.ntranscode=tvt.ntransactionstatus and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.ntreeversiontempcode = "
				+ ntreeversiontempcode + " and tvt.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by tvt.ntreeversiontempcode";

		TreeVersionTemplate objtreeveriontemplate = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(squery,
				TreeVersionTemplate.class, jdbcTemplate);

		if (objtreeveriontemplate != null) {

			returnmap.put("selectedTempVersion", objtreeveriontemplate);
			returnmap.put("SelectedTreeVersionTemplate", objtreeveriontemplate);
			returnmap.putAll((Map<String, Object>) getTemplateMasterTree(ntreeversiontempcode, userInfo).getBody());
			return new ResponseEntity<>(returnmap, HttpStatus.OK);

		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getTemplateMasterTree(final int ntreeversiontempcode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		final String getstatus = "select * from treeversiontemplate where ntreeversiontempcode = "
				+ ntreeversiontempcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		TreeVersionTemplate objget = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(getstatus,
				TreeVersionTemplate.class, jdbcTemplate);
		if (objget != null) {
			final String str = " select tc.slabelname,tc.ntreecontrolcode,ttg.ntemptranstestgroupcode,ttg.nparentnode,ttg.nlevelno,tvt.sversiondescription,tvt.ntransactionstatus from treecontrol tc,treetemptranstestgroup ttg,treeversiontemplate tvt"
					+ " where tvt.ntreeversiontempcode=ttg.ntreeversiontempcode and tc.ntreecontrolcode=ttg.ntreecontrolcode and tc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tvt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode = "
					+ userInfo.getNmastersitecode() + " and tvt.ntreeversiontempcode=" + ntreeversiontempcode
					+ " and tc.nsitecode=" + userInfo.getNmastersitecode() + " and ttg.nsitecode="
					+ userInfo.getNmastersitecode();

			List<TreetempTranstestGroup> lsttreeversiontemplate = (List<TreetempTranstestGroup>) jdbcTemplate.query(str,
					new TreetempTranstestGroup());
			returnmap.put("lstTemplateMasterlevel", lsttreeversiontemplate);
			return new ResponseEntity<>(returnmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fnSequencenumberTemplateMasterUpdate(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> map = new HashMap<>();
		List<String> lstString = (List<String>) inputMap.get("treetemptranstestgroup");
		final int ntreetemplatecount = lstString.size();
		String sQuery = "LOCK TABLE locktreeversion;";
		jdbcTemplate.execute(sQuery);

		final String sequenceno = " select nsequenceno, stablename from seqnoconfigurationmaster"
				+ " where stablename in('treeversiontemplate','treecontrol','treetemptranstestgroup') and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		List<SeqNoConfigurationMaster> lstSeqNo = (List<SeqNoConfigurationMaster>) jdbcTemplate.query(sequenceno,
				new SeqNoConfigurationMaster());

		Map<String, Integer> seqMap = lstSeqNo.stream()
				.collect(Collectors.toMap(SeqNoConfigurationMaster::getStablename,
						seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));
		final int ntreeControl = seqMap.get("treecontrol");
		final int ntreetemptestgroup = seqMap.get("treetemptranstestgroup");
		final int ntreeversion = seqMap.get("treeversiontemplate");

		jdbcTemplate.execute("update seqnoconfigurationmaster set nsequenceno = " + (ntreeversion + 1)
				+ " where stablename='treeversiontemplate' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		jdbcTemplate.execute("update seqnoconfigurationmaster set nsequenceno=" + (ntreeControl + ntreetemplatecount)
				+ "  where stablename='treecontrol' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		jdbcTemplate.execute("update seqnoconfigurationmaster set nsequenceno="
				+ (ntreetemptestgroup + ntreetemplatecount) + "  where stablename='treetemptranstestgroup' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		map.put("ntreeControl", ntreeControl);
		map.put("ntreetemptestgroup", ntreetemptestgroup);
		map.put("ntreeversion", ntreeversion);
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createTemplatemaster(Map<String, Object> inputmap) throws Exception {

		final String sTableLockQuery = " lock  table locktemplatemaster "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		Map<String, Object> returnmap = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String insertQueryStr = "";
		final List<Object> lstaudit = new ArrayList<>();
		final int ncategorycode = (int) inputmap.get("ncategorycode");
		final int nformcode = (int) inputmap.get("nformcode");
		final int nsampletypecode = (int) inputmap.get("nsampletypecode");
		final String specname = (String) inputmap.get("specname");
		final List<String> lstString = (List<String>) inputmap.get("treetemptranstestgroup");
		final int ntreeversion = (int) inputmap.get("ntreeversion");
		int ntreeControl = (int) inputmap.get("ntreeControl");
		int ntreetemptestgroup = (int) inputmap.get("ntreetemptestgroup");
		int nparentnode = 0;
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputmap.get("userinfo"), UserInfo.class);
		String sQuery = "";
		String schildcode = "";
		String slevel = "/";
		String StrQuery = " select ttg.*,tvt.sversiondescription from treetemptranstestgroup ttg,treeversiontemplate tvt "
				+ " where tvt.sversiondescription =N'" + stringUtilityFunction.replaceQuote(specname)
				+ "'  and ttg.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ttg.nsampletypecode=" + (int) inputmap.get("nsampletypecode")
				+ " and ttg.ntreeversiontempcode=tvt.ntreeversiontempcode and " + "  tvt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode="
				+ userInfo.getNmastersitecode() + " and tvt.nsitecode=" + userInfo.getNmastersitecode();
		List<TreetempTranstestGroup> oldTreeversion = (List<TreetempTranstestGroup>) jdbcTemplate.query(StrQuery,
				new TreetempTranstestGroup());

		if (!oldTreeversion.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			String strTemplatemaseter = " select ntemplatecode from treetemplatemaster where ncategorycode ="
					+ ncategorycode + " and nformcode =" + nformcode + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			int ntemplatecode = jdbcTemplate.queryForObject(strTemplatemaseter, Integer.class);
			final String strInsertTreeVersion = "insert into treeversiontemplate (ntreeversiontempcode,ntransactionstatus,napprovalconfigcode,"
					+ "ntemplatecode,nsampletypecode,nversionno,sversiondescription,dmodifieddate,nsitecode,nstatus) values ("
					+ (ntreeversion + 1) + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",-1,"
					+ ntemplatecode + "," + nsampletypecode + ",-1,N'" + stringUtilityFunction.replaceQuote(specname)
					+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			insertQueryStr += strInsertTreeVersion + ";";
			StringBuilder sb = new StringBuilder(
					"Insert into Treecontrol (ntreecontrolcode,slabelname,dmodifieddate,nsitecode,nstatus) values ");
			StringBuilder sb1 = new StringBuilder(
					"Insert into treetemptranstestgroup (ntemptranstestgroupcode,ntreeversiontempcode,nsampletypecode,"
							+ "ntemplatecode,ntreecontrolcode,nparentnode,schildnode,nlevelno,slevelformat,ntransactionstatus,dmodifieddate,nsitecode,nstatus) values ");
			for (int i = 0; i < lstString.size(); i++) {
				String controllabel = String.valueOf(lstString.get(i));
				sb.append("(" + (ntreeControl + 1) + ",N'" + stringUtilityFunction.replaceQuote(controllabel) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
				ntreeControl = ntreeControl + 1;
				slevel = slevel.concat("1/");
				if (i == 0) {
					nparentnode = -1;
				} else {
					nparentnode = ntreetemptestgroup;
				}
				if (i == lstString.size() - 1) {
					schildcode = "-1";
				} else {
					schildcode = Integer.toString((ntreetemptestgroup) + 2);
				}

				sb1.append("(" + (ntreetemptestgroup + 1) + "," + (ntreeversion + 1) + "," + nsampletypecode + ","
						+ ntemplatecode + "," + ntreeControl + "," + nparentnode + "," + schildcode + "," + (i + 1)
						+ ",'" + slevel + "'," + "" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

				ntreetemptestgroup = ntreetemptestgroup + 1;

			}
			insertQueryStr += sb.substring(0, sb.length() - 1) + ";";
			insertQueryStr += sb1.substring(0, sb1.length() - 1) + ";";
			jdbcTemplate.execute(insertQueryStr);

			String sVerQuery = " select * from treeversiontemplate where  ntreeversiontempcode =" + (ntreeversion + 1)
					+ "" + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<TreeVersionTemplate> addTreeTransTempCode = (List<TreeVersionTemplate>) jdbcTemplate.query(sVerQuery,
					new TreeVersionTemplate());

			String auditstr = " select tvt.ntreeversiontempcode,ttg.ntemptranstestgroupcode,tc.ntreecontrolcode,tvt.sversiondescription,tc.slabelname,"
					+ " tvt.ntreeversiontempcode,tvt.ntransactionstatus from treeversiontemplate tvt, treetemptranstestgroup ttg,treecontrol tc"
					+ " where tvt.ntreeversiontempcode = ttg.ntreeversiontempcode and tc.ntreecontrolcode = ttg.ntreecontrolcode"
					+ " and tvt.ntreeversiontempcode = " + (ntreeversion + 1) + " and tvt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttg.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode="
					+ userInfo.getNmastersitecode() + " and tc.nsitecode=" + userInfo.getNmastersitecode();

			List<TreetempTranstestGroup> lstTreeVersionTemplate = (List<TreetempTranstestGroup>) jdbcTemplate
					.query(auditstr, new TreetempTranstestGroup());

			String ntreeControlNew = lstTreeVersionTemplate.stream().map(x -> String.valueOf(x.getNtreecontrolcode()))
					.collect(Collectors.joining(","));

			sQuery = " select * from treecontrol  where ntreecontrolcode in (" + ntreeControlNew + ") and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<TreeControl> addTreeControlCode = (List<TreeControl>) jdbcTemplate.query(sQuery, new TreeControl());

			lstaudit.add(addTreeTransTempCode);
			lstaudit.add(lstTreeVersionTemplate);
			lstaudit.add(addTreeControlCode);
			multilingualIDList.add("IDS_ADDTEMPLATEMASTER");
			multilingualIDList.add("IDS_ADDTREETEMPGROUP");
			multilingualIDList.add("IDS_ADDTREELEVEL");

			auditUtilityFunction.fnInsertListAuditAction(lstaudit, 1, null, multilingualIDList, userInfo);
			returnmap.putAll((Map<String, Object>) getTemplateMasterVersion((ntreeversion + 1), ncategorycode,
					nformcode, nsampletypecode, userInfo).getBody());
			return new ResponseEntity<>(returnmap, HttpStatus.OK);

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public String fnCheckLabelName(Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final String strQuery = "select slabelname from treecontrol tc,treetemptranstestgroup ttg where tc.ntreecontrolcode=ttg.ntreecontrolcode and ttg.ntreeversiontempcode="
				+ ntreeversiontempcode + " and" + " tc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<TreeControl> lsttreecontrol = (List<TreeControl>) jdbcTemplate.query(strQuery, new TreeControl());
		final List<String> lstString = (List<String>) inputMap.get("treetemptranstestgroup");
		int unavailable = 0;

		if (lstString.size() == lsttreecontrol.size()) {
			for (int i = 0; lsttreecontrol.size() - 1 >= i; i++) {
				if (lsttreecontrol.get(i).getSlabelname().equals(lstString.get(i))) {
					unavailable++;
				}
			}
			if (unavailable == lstString.size()) {
				return "Success";
			} else {
				return "Failure";
			}
		} else {
			return "Failure";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateEditTemplatemasterSubmit(Map<String, Object> inputMap, final boolean bflag)
			throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int nformcode = (int) inputMap.get("nformcode");
		final String specname = (String) inputMap.get("specname");
		final int ntemplatecode = (int) inputMap.get("ntemplatecode");
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final String ntreecontrolcode = (String) inputMap.get("ntreecontrolcode");
		final List<String> lstString = (List<String>) inputMap.get("treetemptranstestgroup");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> updatedtemplatemaster = new ArrayList<>();
		final List<Object> oldtemplatemaster = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		TreeVersionTemplate newTreeversion = new TreeVersionTemplate();
		int nparentcode = 0;
		String schildcode = "";
		String slevel = "/";

		final String str = "select * from treeversiontemplate where ntreeversiontempcode =" + ntreeversiontempcode
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		TreeVersionTemplate oldTreeversion = (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(str,
				TreeVersionTemplate.class, jdbcTemplate);

		if (oldTreeversion != null) {
			String StrQuery = " select ttg.*,tvt.sversiondescription from treetemptranstestgroup ttg,treeversiontemplate tvt "
					+ " where tvt.sversiondescription =N'" + stringUtilityFunction.replaceQuote(specname)
					+ "'  and ttg.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ttg.nsampletypecode=" + (int) inputMap.get("nsampletypecode")
					+ " and tvt.ntreeversiontempcode <> " + ntreeversiontempcode
					+ " and ttg.ntreeversiontempcode=tvt.ntreeversiontempcode and " + "  tvt.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode="
					+ userInfo.getNmastersitecode() + " and tvt.nsitecode=" + userInfo.getNmastersitecode();
			List<TreetempTranstestGroup> oldTreeversion1 = (List<TreetempTranstestGroup>) jdbcTemplate.query(StrQuery,
					new TreetempTranstestGroup());

			if (!oldTreeversion1.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else {
				if (oldTreeversion.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					if (!bflag) {
						final String strInsertTreeVersion = "Update treeversiontemplate set sversiondescription = N'"
								+ stringUtilityFunction.replaceQuote(specname) + "'," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
								+ " where ntreeversiontempcode =" + ntreeversiontempcode + "";
						jdbcTemplate.execute(strInsertTreeVersion);
						newTreeversion.setSversiondescription(specname);
						newTreeversion.setNtransactionstatus(oldTreeversion.getNtransactionstatus());
						newTreeversion.setNtreeversiontempcode(oldTreeversion.getNtreeversiontempcode());
						oldtemplatemaster.add(oldTreeversion);
						updatedtemplatemaster.add(newTreeversion);
						multilingualIDList.add("IDS_EDITTEMPLATEMASTER");
					} else {
						int ntreeControl = (int) inputMap.get("ntreeControl");
						int ntreetemptestgroup = (int) inputMap.get("ntreetemptestgroup");
						String insertQueryStr = "";

						insertQueryStr += "Update treeversiontemplate set sversiondescription = N'"
								+ stringUtilityFunction.replaceQuote(specname) + "'," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
								+ "where ntreeversiontempcode =" + ntreeversiontempcode + " ; ";
						insertQueryStr += " Update treecontrol set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
								+ " where ntreecontrolcode in (" + ntreecontrolcode + ");";
						insertQueryStr += " Delete from treetemptranstestgroup where ntreeversiontempcode ="
								+ ntreeversiontempcode + " ;";

						StringBuilder sb = new StringBuilder(
								"Insert into Treecontrol (ntreecontrolcode,slabelname,dmodifieddate,nsitecode,nstatus)  values ");
						StringBuilder sb1 = new StringBuilder(
								"Insert into treetemptranstestgroup (ntemptranstestgroupcode,ntreeversiontempcode,"
										+ "nsampletypecode,ntemplatecode,ntreecontrolcode,nparentnode,schildnode,nlevelno,"
										+ "slevelformat,ntransactionstatus,dmodifieddate,nsitecode,nstatus) values ");
						for (int i = 0; i < lstString.size(); i++) {
							final String controllabel = StringEscapeUtils
									.unescapeJava(String.valueOf(lstString.get(i)));
							sb.append(
									"(" + (ntreeControl + 1) + ",N'" + stringUtilityFunction.replaceQuote(controllabel)
											+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ userInfo.getNmastersitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
							ntreeControl = ntreeControl + 1;
							slevel = slevel.concat("1/");
							if (i == 0) {
								nparentcode = -1;
							} else {
								nparentcode = ntreetemptestgroup;
							}
							if (i == lstString.size() - 1) {
								schildcode = "-1";
							} else {
								schildcode = Integer.toString(ntreetemptestgroup + 2);
							}

							sb1.append("(" + (ntreetemptestgroup + 1) + "," + (ntreeversiontempcode) + ","
									+ nsampletypecode + "," + ntemplatecode + "," + ntreeControl + "," + nparentcode
									+ "," + schildcode + "," + (i + 1) + ",'" + slevel + "'," + ""
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + "'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

							ntreetemptestgroup = ntreetemptestgroup + 1;
						}
						insertQueryStr += sb.substring(0, sb.length() - 1) + ";";
						insertQueryStr += sb1.substring(0, sb1.length() - 1) + ";";
						jdbcTemplate.execute(insertQueryStr);

						final String newstr = "select tc.slabelname,tv.sversiondescription,tc.ntreecontrolcode,ttg.ntreecontrolcode,ttg.ntemptranstestgroupcode,"
								+ " tv.ntreeversiontempcode,tv.ntransactionstatus from treetemptranstestgroup ttg,treecontrol tc,treeversiontemplate tv "
								+ "where ttg.ntreecontrolcode = tc.ntreecontrolcode and tv.ntreeversiontempcode = ttg.ntreeversiontempcode and ttg.ntreeversiontempcode = "
								+ ntreeversiontempcode + " and ttg.nsitecode=" + userInfo.getNmastersitecode()
								+ " and tc.nsitecode=" + userInfo.getNmastersitecode();
						List<TreetempTranstestGroup> lstNewTreeversion = (List<TreetempTranstestGroup>) jdbcTemplate
								.query(newstr, new TreetempTranstestGroup());

						String ntreeControlNew = lstNewTreeversion.stream()
								.map(x -> String.valueOf(x.getNtreecontrolcode())).collect(Collectors.joining(","));
						String sQuery = "select * from treecontrol where ntreecontrolcode in (" + ntreeControlNew + ")";
						List<TreeControl> addTreeControlCode = (List<TreeControl>) jdbcTemplate.query(sQuery,
								new TreeControl());
						// spec name update
						multilingualIDList.add("IDS_EDITTEMPLATEMASTER");
						multilingualIDList.add("IDS_ADDTREELEVEL");
						newTreeversion.setNtreeversiontempcode(lstNewTreeversion.get(0).getNtreeversiontempcode());
						newTreeversion.setSversiondescription(lstNewTreeversion.get(0).getSversiondescription());
						newTreeversion.setNtransactionstatus(lstNewTreeversion.get(0).getNtransactionstatus());
						newTreeversion.setNstatus(lstNewTreeversion.get(0).getNstatus());

						// Role changes
						updatedtemplatemaster.add(lstNewTreeversion);
						updatedtemplatemaster.add(addTreeControlCode);
						auditUtilityFunction.fnInsertListAuditAction(updatedtemplatemaster, 1, null, multilingualIDList,
								userInfo);

					}
					returnmap.putAll((Map<String, Object>) getTemplateMasterVersion(ntreeversiontempcode, ncategorycode,
							nformcode, nsampletypecode, userInfo).getBody());

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteTemplateMaster(int ntreeversiontempcode, int nformcode, int ncategorycode,
			int nsampletypecode, UserInfo userInfo, String ntreecontrolcode) throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		final List<Object> deletedChecklistList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String StrQuery = " select ttg.*,tvt.sversiondescription from treetemptranstestgroup ttg,treeversiontemplate tvt "
				+ " where ttg.ntreeversiontempcode =" + ntreeversiontempcode + " " + "  and ttg.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ttg.ntreeversiontempcode=tvt.ntreeversiontempcode and " + "  tvt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode="
				+ userInfo.getNmastersitecode();
		List<TreetempTranstestGroup> oldTreeversion = (List<TreetempTranstestGroup>) jdbcTemplate.query(StrQuery,
				new TreetempTranstestGroup());
		if (oldTreeversion != null && oldTreeversion.size() > 0) {
			if (oldTreeversion.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				String sVerQuery = " select tvt.ntreeversiontempcode,ttg.ntemptranstestgroupcode,tc.ntreecontrolcode,tvt.sversiondescription,tc.slabelname,"
						+ " tvt.ntreeversiontempcode,tvt.ntransactionstatus from treeversiontemplate tvt, treetemptranstestgroup ttg,treecontrol tc"
						+ " where tvt.ntreeversiontempcode = ttg.ntreeversiontempcode and tc.ntreecontrolcode = ttg.ntreecontrolcode"
						+ " and tvt.ntreeversiontempcode = " + (ntreeversiontempcode) + " and tvt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttg.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode="
						+ userInfo.getNmastersitecode() + " and tc.nsitecode=" + userInfo.getNmastersitecode();
				List<TreetempTranstestGroup> delTreeTransVersionCode = (List<TreetempTranstestGroup>) jdbcTemplate
						.query(sVerQuery, new TreetempTranstestGroup());

				final String strQuery = "Update treeversiontemplate set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode ="
						+ ntreeversiontempcode + "; " + " update treetemptranstestgroup set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode = "
						+ ntreeversiontempcode + ";" + " update treecontrol set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreecontrolcode in ("
						+ ntreecontrolcode + ")";

				jdbcTemplate.execute(strQuery);
				TreeVersionTemplate newTreeversion = new TreeVersionTemplate();
				newTreeversion.setNtreeversiontempcode(oldTreeversion.get(0).getNtreeversiontempcode());
				newTreeversion.setSversiondescription(oldTreeversion.get(0).getSversiondescription());
				newTreeversion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				newTreeversion.setNtransactionstatus(oldTreeversion.get(0).getNtransactionstatus());
				newTreeversion.setNsampletypecode(oldTreeversion.get(0).getNsampletypecode());
				newTreeversion.setNtemplatecode(oldTreeversion.get(0).getNtemplatecode());

				String sQuery = "select * from treecontrol where ntreecontrolcode in (" + ntreecontrolcode + ")";
				List<TreeControl> delTreeControlCode = (List<TreeControl>) jdbcTemplate.query(sQuery,
						new TreeControl());

				deletedChecklistList.add(Arrays.asList(newTreeversion));
				deletedChecklistList.add(delTreeTransVersionCode);
				deletedChecklistList.add(delTreeControlCode);
				multilingualIDList.add("IDS_DELETETEMPLATEMASTERVERSION");
				multilingualIDList.add("IDS_DELETETRANSTREEVERSION");
				multilingualIDList.add("IDS_DELETETREETEMPLATE");
				auditUtilityFunction.fnInsertListAuditAction(deletedChecklistList, 1, null, multilingualIDList,
						userInfo);
				returnmap.putAll((Map<String, Object>) getTemplateMasterVersion(-1, ncategorycode, nformcode,
						nsampletypecode, userInfo).getBody());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> approveTemplateMasterVersion(final int ntreeversiontempcode, final int nformcode,
			final int ncategorycode, final int ntemplatecode, final UserInfo userInfo) throws Exception {
		Map<String, Object> returnmap = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> approvedUserroletemplate = new ArrayList<>();
		final List<Object> retiredUserroletemplate = new ArrayList<>();
		int nversion = 0;
		String strQuery = "";

		final String oldrecrordstr = "select * from treeversiontemplate where ntreeversiontempcode ="
				+ ntreeversiontempcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "";
		List<TreeVersionTemplate> lstapprovedtemplate = (List<TreeVersionTemplate>) jdbcTemplate.query(oldrecrordstr,
				new TreeVersionTemplate());
		if (!lstapprovedtemplate.isEmpty()) {
			if (lstapprovedtemplate.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()) {
				strQuery = " select ntreeversiontempcode,nversionno,ntransactionstatus,sversiondescription,nsampletypecode,ntemplatecode from treeversiontemplate where "
						+ " nsampletypecode = " + lstapprovedtemplate.get(0).getNsampletypecode()
						+ " and ntemplatecode =" + ntemplatecode + " and ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();
				List<TreeVersionTemplate> lstntreeversiontempcode = (List<TreeVersionTemplate>) jdbcTemplate
						.query(strQuery, new TreeVersionTemplate());

				Object objversion = projectDAOSupport.fnGetVersion(nformcode, ncategorycode, null,
						SeqNoTemplateMasterVersion.class, userInfo.getNmastersitecode(), userInfo);
				if (objversion != null) {
					nversion = Integer.parseInt(BeanUtils.getProperty(objversion, "versionno"));
				}
				String queryStr = "";
				if (!lstntreeversiontempcode.isEmpty()) {
					queryStr += "update treeversiontemplate set ntransactionstatus ="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "," + "dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode ="
							+ lstntreeversiontempcode.get(0).getNtreeversiontempcode() + ";";
					queryStr += " update treetemptranstestgroup set ntransactionstatus="
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "," + "dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntreeversiontempcode ="
							+ lstntreeversiontempcode.get(0).getNtreeversiontempcode() + ";";
				} else {

				}

				queryStr += "Update treeversiontemplate set ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",nversionno= " + nversion
						+ "," + "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where ntreeversiontempcode =" + ntreeversiontempcode + ";";
				queryStr += "update treetemptranstestgroup set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "where ntreeversiontempcode ="
						+ ntreeversiontempcode + ";";
				jdbcTemplate.execute(queryStr);

				if (!lstntreeversiontempcode.isEmpty()) {
					List<TreeVersionTemplate> lstobjapproved = (List<TreeVersionTemplate>) jdbcTemplate
							.query(oldrecrordstr, new TreeVersionTemplate());

					List<TreeVersionTemplate> lstretried = (List<TreeVersionTemplate>) jdbcTemplate.query(
							" select ntreeversiontempcode,nversionno,ntransactionstatus,sversiondescription,ntemplatecode from treeversiontemplate "
									+ "where ntreeversiontempcode = "
									+ lstntreeversiontempcode.get(0).getNtreeversiontempcode() + "",
							new TreeVersionTemplate());

					approvedUserroletemplate.add(lstapprovedtemplate);
					approvedUserroletemplate.add(lstntreeversiontempcode);
					retiredUserroletemplate.add(lstobjapproved);
					retiredUserroletemplate.add(lstretried);
					multilingualIDList.add("IDS_APPROVETEMPLATEMASTERVERSION");
					auditUtilityFunction.fnInsertListAuditAction(retiredUserroletemplate, 2, approvedUserroletemplate,
							multilingualIDList, userInfo);

				} else {
					List<TreeVersionTemplate> lstobjapproved = (List<TreeVersionTemplate>) jdbcTemplate
							.query(oldrecrordstr, new TreeVersionTemplate());
					approvedUserroletemplate.add(lstapprovedtemplate);
					retiredUserroletemplate.add(lstobjapproved);
					multilingualIDList.add("IDS_APPROVETEMPLATEMASTERVERSION");
					auditUtilityFunction.fnInsertListAuditAction(retiredUserroletemplate, 2, approvedUserroletemplate,
							multilingualIDList, userInfo);
				}
				returnmap.putAll((Map<String, Object>) getTemplateMasterVersion(ntreeversiontempcode, ncategorycode,
						nformcode, lstapprovedtemplate.get(0).getNsampletypecode(), userInfo).getBody());

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(returnmap, HttpStatus.OK);

	}

}
