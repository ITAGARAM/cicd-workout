package com.agaramtech.qualis.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleFilterDetail;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecificationHistory;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestMaterial;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
@Component
public class TestGroupCommonFunction {

	// private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupCommonFunction.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;

	public Map<String, Object> getHierarchicalList(final List<TreeTemplateManipulation> listTree, boolean sEditAction,
			String sNode, int primarykey) {
		Map<String, Object> outputMap = new TreeMap<String, Object>();
		List<TreeDataFormat> list = null;
		String activeKey = "";
		Map<Integer, String> openNodes = new HashMap<Integer, String>();
		openNodes.put(0, "root");
		if (!listTree.isEmpty()) {
			List<TreeDataFormat> originalList = generateTreeFormatData(listTree);
			List<TreeDataFormat> copyList = new ArrayList<TreeDataFormat>();
			originalList.forEach(o -> {
				copyList.add(new TreeDataFormat(o));
			});

			// Generate Open Nodes
			for (TreeDataFormat treeDataFormat : copyList) {
				copyList.stream().forEach(x -> {
					if (treeDataFormat.getPrimaryKey() == x.getParentKey()) {
						x.setKey(treeDataFormat.getKey() + "/" + x.getKey());
					}
				});
			}

			copyList.stream().forEach(m -> {
				openNodes.put(m.getPrimaryKey(), "root/" + m.getKey());
			});

			// Generate Tree Data
			Map<Integer, TreeDataFormat> nodes = originalList.stream()
					.collect(Collectors.toMap(treeFormat -> treeFormat.getPrimaryKey(), treeFormat -> treeFormat));
			originalList.stream().filter(o -> o.getParentKey() > -1)
					.sorted(Comparator.comparing(TreeDataFormat::getPrimaryKey).reversed()).forEach(h -> {
						if (nodes.get(h.getParentKey()) != null) {
							if (nodes.get(h.getParentKey()).getNodes() == null) {
								nodes.get(h.getParentKey()).setNodes(new ArrayList<TreeDataFormat>());
							}
							nodes.get(h.getParentKey()).getNodes().add(nodes.get(h.getPrimaryKey()));
							nodes.remove(h.getPrimaryKey());
						}
					});

			list = new ArrayList<TreeDataFormat>(nodes.values());
			if (sEditAction) {
				outputMap.put("primarykey", primarykey);
				activeKey = sNode;
			} else {
				List<TreeDataFormat> listTreeDataFormat = convertToFlatList(list);
				int key = listTreeDataFormat.get(listTreeDataFormat.size() - 1).getPrimaryKey();
				outputMap.put("primarykey", key);
				activeKey = openNodes.get(key);
			}
		} else {
			outputMap.put("primarykey", 0);
			activeKey = "root";
		}

		TreeDataFormat treeDataFormat = new TreeDataFormat();
		treeDataFormat.setKey("root");
		treeDataFormat.setLabel("root");
		treeDataFormat.setItem(null);
		treeDataFormat.setNodes(list);
		List<TreeDataFormat> lstTreeDataFormat = new ArrayList<TreeDataFormat>();
		lstTreeDataFormat.add(treeDataFormat);

		outputMap.put("AgaramTree", lstTreeDataFormat);
		outputMap.put("OpenNodes", new ArrayList<String>(openNodes.values()));
		outputMap.put("FocusKey", activeKey);
		outputMap.put("ActiveKey", activeKey);
		return outputMap;
	}

	private static List<TreeDataFormat> convertToFlatList(List<TreeDataFormat> listTreeDataFormat) {
		return listTreeDataFormat.stream().flatMap(i -> {
			if (Objects.nonNull(i.getNodes())) {
				return Stream.concat(Stream.of(i), convertToFlatList(i.getNodes()).stream());
			}
			return Stream.of(i);

		}).collect(Collectors.toList());
	}

	public List<TreeDataFormat> generateTreeFormatData(List<TreeTemplateManipulation> listTree) {
		List<TreeDataFormat> treeList = new ArrayList<TreeDataFormat>();
		for (TreeTemplateManipulation objTree : listTree) {
			TreeDataFormat treeDataFormat = new TreeDataFormat();
			treeDataFormat.setItem(objTree);
			treeDataFormat.setKey(String.valueOf(objTree.getNtemplatemanipulationcode()));
			treeDataFormat.setPrimaryKey(objTree.getNtemplatemanipulationcode());
			treeDataFormat.setParentKey(objTree.getNparentnode());
			treeDataFormat.setLabel(objTree.getSleveldescription());
			treeList.add(treeDataFormat);
		}
		return treeList;
	}

	public List<TestGroupSpecification> getTestGroupSpecification(final int ntemplatemanipulationcode,
			final int nuserrolecode, final UserInfo userInfo) throws Exception {

		String sQuery = "select f.ntransactionstatus from approvalconfigrole cr,approvalconfigversion v,approvalrolefilterdetail f,approvalconfig ac "
				+ " where cr.nuserrolecode=" + nuserrolecode
				+ " and cr.napprovalconfigcode=ac.napprovalconfigcode and cr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and f.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cr.napproveconfversioncode=v.napproveconfversioncode "
				+ " and cr.napprovalconfigrolecode=f.napprovalconfigrolecode and f.ntransactionstatus!=0 "
				+ " and ac.napprovalsubtypecode= " + Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + " "
				+ " and ac.nregtypecode= " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
				+ " and ac.nregsubtypecode= " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " group by f.ntransactionstatus";

		List<ApprovalRoleFilterDetail> lstACRF = (List<ApprovalRoleFilterDetail>) jdbcTemplate.query(sQuery,
				new ApprovalRoleFilterDetail());
		String sapprovalstatus = stringUtilityFunction.fnDynamicListToString(lstACRF, "getNtransactionstatus");
		String sSubQry = "";

		// For_PostgreSQL
		String queryformat = "TO_CHAR(dexpirydate,'" + userInfo.getSpgsitedatetime() + "') ";

		if (!sapprovalstatus.isEmpty()) {
			sSubQry = " and tgs.napprovalstatus in (" + sapprovalstatus + ")";
		}

		sQuery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
				+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
				+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
				+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
				+ "formwisestatuscolor fwc, colormaster cm"
				+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
				+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
				+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwc.nformcode = "
				+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
				+ ntemplatemanipulationcode + " " + sSubQry + " order by tgs.nallottedspeccode;";

		List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
				.query(sQuery, new TestGroupSpecification());

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
				Arrays.asList("sexpirydate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, true,
				Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);
		return objMapper.convertValue(listSpec, new TypeReference<List<TestGroupSpecification>>() {
		});
	}

	public List<TestGroupSpecSampleType> getTestGroupSampleType(final TestGroupSpecification objGroupSpecification)
			throws Exception {
		String sQuery = "select tgs.nspecsampletypecode,tgs.nallottedspeccode, tgs.ncomponentcode,  c.scomponentname "
				+ " from testgroupspecsampletype tgs, component c where tgs.ncomponentcode = c.ncomponentcode "
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgs.nstatus = c.nstatus and tgs.nallottedspeccode = "
				+ objGroupSpecification.getNallottedspeccode();
		return (List<TestGroupSpecSampleType>) jdbcTemplate.query(sQuery, new TestGroupSpecSampleType());
	}

	public List<TestGroupTest> getTestGroupTest(final int nspecsampletypecode, final int testgrouptestcode,
			final UserInfo userInfo) throws Exception {
		String sSubQry = "";
		if (testgrouptestcode != 0) {
			sSubQry = " and tgt.ntestgrouptestcode = " + testgrouptestcode + " ";
		}

		String sQuery = "SELECT COUNT(tgtp.ntestgrouptestparametercode) AS nparametercount, tgt.ntestgrouptestcode,tgt.ntestcode,tgt.nspecsampletypecode,"
				+ "  tgt.stestsynonym,tgt.nsectioncode, tgt.nmethodcode,tgt.ninstrumentcatcode,tgt.ncontainertypecode,tgt.ntestpackagecode,tgt.nsorter,"
				+ "  tm.ntransactionstatus,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS steststatus,"
				+ "  tgt.nrepeatcountno,tgt.nstatus,CASE WHEN tgt.nmethodcode = -1 THEN '-' ELSE m.smethodname END AS smethodname,tgtf.nlinkcode,tm.stestname,"
				+ "  CASE WHEN tgt.ninstrumentcatcode = -1 THEN '-' ELSE ic.sinstrumentcatname END AS sinstrumentcatname,tgtf.ntestgroupfilecode,"
				+ "  CASE WHEN tgt.nsectioncode = -1 THEN '-' ELSE s.ssectionname END AS ssectionname,"
				+ "  CASE WHEN tgt.ncontainertypecode = -1 THEN '-' ELSE ct.scontainertype END AS scontainertype,"
				+ "  CASE WHEN tgt.ntestpackagecode = -1 THEN '-' ELSE tp.stestpackagename END AS stestpackagename, "
				+ "  COALESCE(tgtf.sfilename,'-') AS sfilename, tgtf.ssystemfilename, tgtf.nattachmenttypecode, "
				+ "  COALESCE(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode()
				+ "', at.jsondata->'sattachmenttype'->>'en-US') AS stypename,"
				+ "  lm.jsondata->>'slinkname' AS slinkname,  tgt.ncost  "
				+ " FROM testgrouptest tgt JOIN  testmaster tm ON tm.ntestcode = tgt.ntestcode AND tgt.nstatus = tm.nstatus "
				+ "  JOIN transactionstatus ts ON ts.ntranscode = tm.ntransactionstatus AND tm.nstatus = ts.nstatus "
				+ "  JOIN  method m ON m.nmethodcode = tgt.nmethodcode AND m.nstatus = tgt.nstatus"
				+ "  JOIN section s ON s.nsectioncode = tgt.nsectioncode AND m.nstatus = tgt.nstatus"
				+ "  JOIN  instrumentcategory ic ON ic.ninstrumentcatcode = tgt.ninstrumentcatcode AND ic.nstatus = tgt.nstatus "
				+ "  JOIN containertype ct ON ct.ncontainertypecode = tgt.ncontainertypecode AND ct.ncontainertypecode = tgt.ncontainertypecode "
				+ "  JOIN testpackage tp ON tp.ntestpackagecode = tgt.ntestpackagecode AND tp.nstatus = tgt.nstatus "
				+ "  LEFT OUTER JOIN testgrouptestparameter tgtp ON tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode AND tgtp.nstatus = tgt.nstatus "
				+ "  LEFT OUTER JOIN testgrouptestfile tgtf ON tgtf.ntestgrouptestcode = tgt.ntestgrouptestcode AND tgtf.nstatus = tgt.nstatus "
				+ "  LEFT OUTER JOIN linkmaster lm ON lm.nlinkcode = tgtf.nlinkcode AND lm.nstatus = tgt.nstatus "
				+ "  LEFT OUTER JOIN attachmenttype at ON at.nattachmenttypecode = tgtf.nattachmenttypecode AND at.nstatus = tgt.nstatus "
				+ "  WHERE  tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND tgt.nisvisible = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " AND tgt.nspecsampletypecode = " + nspecsampletypecode + " " + sSubQry
				+ "  GROUP BY tgt.ntestgrouptestcode, tgt.ntestcode,tgt.nspecsampletypecode,tgt.stestsynonym,tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
				+ "   tgt.ncontainertypecode, tgt.ntestpackagecode,tgt.nsorter, tm.ntransactionstatus,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', ts.jsondata->'stransdisplaystatus'->>'en-US'),"
				+ "   tgt.nrepeatcountno,tgt.nstatus,CASE WHEN tgt.nmethodcode = -1 THEN '-' ELSE m.smethodname END,tgtf.nlinkcode,"
				+ "   tm.stestname,CASE WHEN tgt.ninstrumentcatcode = -1 THEN '-' ELSE ic.sinstrumentcatname END,"
				+ "   tgtf.ntestgroupfilecode,CASE WHEN tgt.nsectioncode = -1 THEN '-' ELSE s.ssectionname END,"
				+ "   CASE WHEN tgt.ncontainertypecode = -1 THEN '-' ELSE ct.scontainertype END,"
				+ "   CASE WHEN tgt.ntestpackagecode = -1 THEN '-' ELSE tp.stestpackagename END, COALESCE(tgtf.sfilename,'-'), "
				+ "    tgtf.ssystemfilename, tgtf.nattachmenttypecode, COALESCE(at.jsondata->'sattachmenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "', at.jsondata->'sattachmenttype'->>'en-US'),"
				+ "    lm.jsondata->>'slinkname', tgt.ncost ORDER BY  tgt.nsorter DESC";

		final ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(jdbcTemplate.query(sQuery, new TestGroupTest()),
				new TypeReference<List<TestGroupTest>>() {
				});
	}

	public List<TestGroupTestParameter> getTestGroupTestParameter(final int ntestgrouptestcode,
			final int ntestgrouptestparametercode, final UserInfo userInfo) throws Exception {

		String sSubQry = "";
		if (ntestgrouptestparametercode != 0) {
			sSubQry = " and ntestgrouptestparametercode = " + ntestgrouptestparametercode;
		}
		final String sQuery = "select cl.nchecklistcode,tgp.ntestparametercode,tgp.ntestgrouptestparametercode,tgp.nchecklistversioncode,tgp.ntestgrouptestcode,tgp.sparametersynonym,"
				+ "tgp.nparametertypecode,tgp.nunitcode,tgp.nroundingdigits,tp.sparametername,"
				+ "u.sunitname,pt.sdisplaystatus,tgp.nsorter,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	ts1.jsondata->'stransdisplaystatus'->>'en-US')  as sgradingmandatory,"
				+ "coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	 ts2.jsondata->'stransdisplaystatus'->>'en-US') as sresultmandatory,coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	 ts3.jsondata->'stransdisplaystatus'->>'en-US') as  sreportmandatory,"
				+ " coalesce(ts4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "	 ts4.jsondata->'stransdisplaystatus'->>'en-US') as sisadhocparameter"
				+ " ,tgp.nreportmandatory,cl.schecklistname,"
				+ "COALESCE(v.schecklistversionname,'') as schecklistversionname,tp.nroundingdigits as nroundingdigits,"
				+ "case when tgp.sspecdesc = '' or tgp.sspecdesc = 'null' then '-' else COALESCE(tgp.sspecdesc, '-') end as sspecdesc, tgt.stestsynonym,"
				+ " tm.stestname,tgp.nresultaccuracycode,ra.sresultaccuracyname "
				+ " from testgrouptestparameter tgp, testparameter tp, testmaster tm, testgrouptest tgt, checklist cl,checklistversion v,"
				+ " unit u, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3,transactionstatus ts4, parametertype pt,resultaccuracy ra"
				+ " where tp.ntestparametercode = tgp.ntestparametercode and v.nchecklistversioncode=tgp.nchecklistversioncode and cl.nchecklistcode=v.nchecklistcode and u.nunitcode = tgp.nunitcode"
				+ " and ts1.ntranscode = tgp.ngradingmandatory and ts2.ntranscode = tgp.nresultmandatory and ts3.ntranscode = tgp.nreportmandatory  AND ts4.ntranscode = tgp.nisadhocparameter"
				+ " and tgt.ntestgrouptestcode = tgp.ntestgrouptestcode and tgt.ntestcode = tm.ntestcode and ra.nresultaccuracycode=tgp.nresultaccuracycode "
				+ " and pt.nparametertypecode = tgp.nparametertypecode and pt.nstatus = tgp.nstatus and u.nstatus = tgp.nstatus and ts1.nstatus = tgp.nstatus and ts2.nstatus = tgp.nstatus "
				+ " and ts3.nstatus = tgp.nstatus  AND ts4.nstatus = tgp.nstatus and v.nstatus = tgp.nstatus and cl.nstatus = tgp.nstatus and tgt.nstatus = tgp.nstatus "
				+ " and tm.nstatus = tgp.nstatus and ra.nstatus=tgp.nstatus " + " and tgp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgp.nisvisible = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + " and tgp.ntestgrouptestcode = "
				+ ntestgrouptestcode + " " + sSubQry + " order by tgp.nsorter";

		final List<TestGroupTestParameter> list = (List<TestGroupTestParameter>) jdbcTemplate.query(sQuery,
				new TestGroupTestParameter());
		final ObjectMapper mapper = new ObjectMapper();
		return mapper
				.convertValue(
						commonFunction.getMultilingualMessageList(list,
								Arrays.asList("sresultmandatory", "sreportmandatory"), userInfo.getSlanguagefilename()),
						new TypeReference<List<TestGroupTestParameter>>() {
						});
	}

	public Map<String, Object> getTestGroupTestPredefinedParameter(final int ntestgrouptestparametercode,
			final String slanguagefilename) throws Exception {

		String sQuery = "select ttpp.ntestgrouptestpredefcode, ttpp.ntestgrouptestparametercode, ttpp.ngradecode, ttpp.ndefaultstatus, ttpp.spredefinedname,ttpp.spredefinedsynonym,ttpp.nneedresultentryalert,ttpp.nneedsubcodedresult,"
				+ "  coalesce(g.jsondata->'sdisplayname'->>'en-US',g.jsondata->'sdisplayname'->>'en-US')sdisplaystatus from testgrouptestpredefparameter ttpp, grade g"
				+ " where ttpp.ngradecode=g.ngradecode and g.nstatus = ttpp.nstatus" + " and ttpp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ttpp.ntestgrouptestparametercode = " + ntestgrouptestparametercode;
		Map<String, Object> outputMap = new HashMap<String, Object>();

		outputMap.put("TestGroupTestPredefinedParameter",
				commonFunction.getMultilingualMessageList(
						jdbcTemplate.query(sQuery, new TestGroupTestPredefinedParameter()),
						Arrays.asList("sdisplaystatus"), slanguagefilename));
		return outputMap;
	}

	public List<TestGroupTestNumericParameter> getTestGroupTestNumericParameter(TestGroupTestParameter objTGTP)
			throws Exception {
		String sQuery = "select tnp.ntestgrouptestnumericcode,tgp.ntestgrouptestcode,tnp.ntestgrouptestparametercode,"
				+ "COALESCE(tnp.smaxa, '-') smaxa, COALESCE(tnp.smaxb, '-') smaxb, COALESCE(tnp.smaxlod, '-') smaxlod,"
				+ "COALESCE(tnp.smaxloq, '-') smaxloq, COALESCE(tnp.smina, '-') smina, COALESCE(tnp.sminb, '-') sminb,"
				+ "COALESCE(tnp.sminlod, '-') sminlod, COALESCE(tnp.sminloq, '-') sminloq, COALESCE(tnp.sdisregard, '-') sdisregard,"
				+ "COALESCE(tnp.sresultvalue, '-') sresultvalue, tnp.nstatus,tgp.sparametersynonym,gd.ngradecode,"
				+ " coalesce(gd.jsondata->'sdisplayname'->>'en-US',gd.jsondata->'sdisplayname'->>'en-US') sgradename "
				+ " from testgrouptestnumericparameter tnp,testgrouptestparameter tgp,grade gd where tnp.ntestgrouptestparametercode=tgp.ntestgrouptestparametercode and gd.ngradecode=tnp.ngradecode "
				+ " and tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and gd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tnp.ntestgrouptestparametercode = " + objTGTP.getNtestgrouptestparametercode();

		return (List<TestGroupTestNumericParameter>) jdbcTemplate.query(sQuery, new TestGroupTestNumericParameter());
	}

	public List<TestGroupTestCharParameter> getTestGroupTestCharParameter(TestGroupTestParameter objTGTP)
			throws Exception {
		String sQuery = "select ntestgrouptestcharcode, ntestgrouptestparametercode, scharname, nsitecode, nstatus from testgrouptestcharparameter where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestparametercode = "
				+ objTGTP.getNtestgrouptestparametercode() + ";";

		return (List<TestGroupTestCharParameter>) jdbcTemplate.query(sQuery, new TestGroupTestCharParameter());
	}

	public Map<String, Object> getTestGroupTestFormula(final int ntestgrouptestparametercode,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String sQuery = "select tgtf.ntestgrouptestparametercode, tgtf.ntestgrouptestformulacode, tgtf.sformulacalculationcode,"
				+ "tgtf.sformulacalculationdetail, tf.sformulaname, coalesce(ts.jsondata->'stransdisplaystatus'->>'\"\r\n"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus , tgtf.ntransactionstatus "
				+ " from testgrouptestformula tgtf,testformula tf,transactionstatus ts where tgtf.ntransactionstatus=ts.ntranscode"
				+ " and tgtf.ntestformulacode=tf.ntestformulacode and tgtf.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtf.ntestgrouptestparametercode="
				+ ntestgrouptestparametercode;

		outputMap.put("TestGroupTestFormula", jdbcTemplate.query(sQuery, new TestGroupTestFormula()));
		return outputMap;
	}

	public List<TestGroupTestMaterial> getTestGroupTestMaterial(TestGroupTest objTestGroupTest) throws Exception {
		final String sQuery = "select tgtm.ntestgrouptestcode, tgtm.ntestgrouptestmaterialcode,tgtm.nmaterialcatcode,tgtm.nmaterialcode,tgtm.nmaterialtypecode,tgtm.nqty,"
				+ "tgtm.nunitcode,tgtm.nstatus,tgtm.sremarks,tgtm.sinventorycode,m.smaterialname,u.sunitname,mc.smaterialcatname,mt.smaterialtypename "
				+ " from testgrouptestmaterial tgtm,material m,unit u,materialcategory mc,materialtype mt "
				+ " where tgtm.nmaterialcode=m.nmaterialcode and tgtm.nunitcode=u.nunitcode and tgtm.nmaterialcatcode=mc.nmaterialcatcode"
				+ " and tgtm.nmaterialtypecode=mt.nmaterialtypecode and m.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtm.ntestgrouptestcode="
				+ objTestGroupTest.getNtestgrouptestcode();

		return (List<TestGroupTestMaterial>) jdbcTemplate.query(sQuery, new TestGroupTestMaterial());
	}

	public TestGroupSpecification getActiveSpecification(final int nallottedspeccode, // final String sdateformat,
			final UserInfo userInfo) throws Exception {

		// For_MSSQL
		// String queryformat="FORMAT(dexpirydate, 'yyyy-MM-dd HH:mm:ss') ";

		// For_PostgreSQL
		String queryformat = "TO_CHAR(dexpirydate,'" + userInfo.getSpgsitedatetime() + "') ";

		final String sQuery = "select *, " + queryformat + " as sexpirydate"
				+ " from testgroupspecification where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nallottedspeccode = "
				+ nallottedspeccode;

		return (TestGroupSpecification) jdbcUtilityFunction.queryForObject(sQuery, TestGroupSpecification.class,
				jdbcTemplate);
	}

	public Map<String, Object> getTestGroupParameterDetails(final TestGroupTestParameter objTestGroupTestParameter,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (objTestGroupTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype()) {
			List<TestGroupTestNumericParameter> lstTGTNP = getTestGroupTestNumericParameter(objTestGroupTestParameter);
			outputMap.put("TestGroupTestNumericParameter", lstTGTNP);
			outputMap.putAll(getTestGroupTestClinicalSpec(objTestGroupTestParameter.getNtestgrouptestparametercode(),
					objUserInfo));
			outputMap.putAll(
					getTestGroupTestFormula(objTestGroupTestParameter.getNtestgrouptestparametercode(), objUserInfo));
		} else if (objTestGroupTestParameter.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
				.getparametertype()) {
			outputMap.putAll(getTestGroupTestPredefinedParameter(
					objTestGroupTestParameter.getNtestgrouptestparametercode(), objUserInfo.getSlanguagefilename()));
		} else if (objTestGroupTestParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
				.getparametertype()) {
			List<TestGroupTestCharParameter> lstTGTCP = getTestGroupTestCharParameter(objTestGroupTestParameter);
			outputMap.put("TestGroupTestCharParameter", lstTGTCP);
		} else if (objTestGroupTestParameter.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
				.getparametertype()) {
			// Need to get Testgrouptest attachment get
		}
		final List<TestGroupTestParameter> listParameter = new ArrayList<TestGroupTestParameter>();
		listParameter.add(objTestGroupTestParameter);
		outputMap.put("selectedParameter", commonFunction.getMultilingualMessageList(listParameter,
				Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()).get(0));
		return outputMap;
	}

	public TestGroupTest getActiveTestGroupTest(final int ntestgrouptestcode) throws Exception {
		final String sQuery = "select * from testgrouptest where ntestgrouptestcode = " + ntestgrouptestcode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (TestGroupTest) jdbcUtilityFunction.queryForObject(sQuery, TestGroupTest.class, jdbcTemplate);
	}

	public Map<String, Object> getTestDetails(final int specsampletypecode, final int ntestgrouptestcode,
			final UserInfo objUserInfo, final boolean isEditAction) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<TestGroupTest> listTestGroupTest = getTestGroupTest(specsampletypecode,
				isEditAction ? 0 : ntestgrouptestcode, objUserInfo);
		outputMap.put("TestGroupTest", listTestGroupTest);
		if (!listTestGroupTest.isEmpty()) {
			TestGroupTest selectedTest = new TestGroupTest();
			if (isEditAction) {
				selectedTest = listTestGroupTest.stream().filter(x -> x.getNtestgrouptestcode() == ntestgrouptestcode)
						.collect(Collectors.toList()).get(0);
			} else {
				selectedTest = listTestGroupTest.get(0);
			}
			outputMap.put("SelectedTest", selectedTest);
			List<TestGroupTestParameter> listTGTP = getTestGroupTestParameter(selectedTest.getNtestgrouptestcode(), 0,
					objUserInfo);
			outputMap.put("TestGroupTestParameter", listTGTP);
			List<TestGroupTestMaterial> listTGTM = getTestGroupTestMaterial(selectedTest.getNtestgrouptestcode(), 0,
					objUserInfo);
			outputMap.put("TestGroupTestMaterial", listTGTM);
			if (!listTGTP.isEmpty()) {
				outputMap.putAll(getTestGroupParameterDetails(listTGTP.get(0), objUserInfo));
			}
			if (!listTGTM.isEmpty()) {

				outputMap.put("selectedMaterial", commonFunction.getMultilingualMessageList(listTGTM,
						Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()).get(listTGTM.size() - 1));
			}
			outputMap.put("ntestgrouptestcode", selectedTest.getNtestgrouptestcode());
			outputMap.putAll((Map<String, Object>) getTestGroupRulesEngine(outputMap, objUserInfo));
		} else {
			outputMap.put("SelectedTest", null);
			outputMap.put("TestGroupTestParameter", null);
			outputMap.put("selectedParameter", null);
			outputMap.put("TestGroupTestMaterial", null);
			outputMap.put("selectedMaterial", null);
		}
		return outputMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getComponentDetails(final TestGroupSpecification objTestGroupSpecification,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("SelectedSpecification", objTestGroupSpecification);
		final List<TestGroupSpecSampleType> listSpecSampleType = getTestGroupSampleType(objTestGroupSpecification);
		List<TestGroupTest> listTestGroupTest = new ArrayList<TestGroupTest>();
		List<Map<String, Object>> listRulesEngine = new ArrayList<Map<String, Object>>();
		List<TestGroupTestParameter> listTGTP = new ArrayList<TestGroupTestParameter>();
		List<TestGroupTestMaterial> listTGTM = new ArrayList<TestGroupTestMaterial>();

		TestGroupTest selectedTestGroupTest = null;
		Map<String, Object> SelectedRulesEngine = null;
		if (!listSpecSampleType.isEmpty()) {
			outputMap.put("SelectedComponent", listSpecSampleType.get(listSpecSampleType.size() - 1));
			listTestGroupTest = getTestGroupTest(
					listSpecSampleType.get(listSpecSampleType.size() - 1).getNspecsampletypecode(), 0, objUserInfo);
			outputMap.put("TestGroupTest", listTestGroupTest);
			if (!listTestGroupTest.isEmpty()) {
				selectedTestGroupTest = listTestGroupTest.get(0);
				listTGTP = getTestGroupTestParameter(selectedTestGroupTest.getNtestgrouptestcode(), 0, objUserInfo);
				if (!listTGTP.isEmpty()) {
					outputMap.putAll(getTestGroupParameterDetails(listTGTP.get(0), objUserInfo));
				}
				listTGTM = getTestGroupTestMaterial(selectedTestGroupTest.getNtestgrouptestcode(), 0, objUserInfo);
				outputMap.put("TestGroupTest", listTestGroupTest);
				if (!listTGTM.isEmpty()) {
					outputMap.put("selectedMaterial",
							commonFunction.getMultilingualMessageList(listTGTM, Arrays.asList("sdisplaystatus"),
									objUserInfo.getSlanguagefilename()).get(listTGTM.size() - 1));
				}
				outputMap.put("ntestgrouptestcode", selectedTestGroupTest.getNtestgrouptestcode());
				outputMap.putAll((Map<String, Object>) getTestGroupRulesEngine(outputMap, objUserInfo));

				listRulesEngine = (List<Map<String, Object>>) outputMap.get("RulesEngine");

				if (!listRulesEngine.isEmpty()) {
					SelectedRulesEngine = listRulesEngine.get(0);
				}
			}
		} else {
			outputMap.put("SelectedComponent", null);
		}
		outputMap.put("SelectedTest", selectedTestGroupTest);
		outputMap.put("TestGroupTestParameter", listTGTP);
		outputMap.put("TestGroupTest", listTestGroupTest);
		outputMap.put("TestGroupSpecSampleType", listSpecSampleType);
		outputMap.put("TestGroupTestMaterial", listTGTM);
		outputMap.put("RulesEngine", listRulesEngine);
		outputMap.put("SelectedRulesEngine", SelectedRulesEngine);

		return outputMap;
	}

	public ResponseEntity<Object> getTestGroupParameter(final UserInfo objUserInfo,
			final TestGroupTestParameter objParameter, final Boolean isParameterBasedGet) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		List<TestGroupTestParameter> listTGTP = getTestGroupTestParameter(objParameter.getNtestgrouptestcode(),
				isParameterBasedGet ? objParameter.getNtestgrouptestparametercode() : 0, objUserInfo);
		outputMap.put("TestGroupTestParameter", listTGTP);
		if (!listTGTP.isEmpty()) {
			List<TestGroupTestParameter> selectedParameter = new ArrayList<TestGroupTestParameter>();
			if (isParameterBasedGet) {
				selectedParameter = Arrays.asList(listTGTP.get(listTGTP.size() - 1));
			} else {
				selectedParameter = listTGTP.stream().filter(
						x -> x.getNtestgrouptestparametercode() == objParameter.getNtestgrouptestparametercode())
						.collect(Collectors.toList());
			}
			if (!selectedParameter.isEmpty()) {
				outputMap.putAll(getTestGroupParameterDetails(selectedParameter.get(0), objUserInfo));
				outputMap.put("selectedParameter", commonFunction.getMultilingualMessageList(selectedParameter,
						Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()).get(0));
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_PARAMETERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public TestGroupSpecification getActiveSpecByTestId(final int ntestgrouptestcode) throws Exception {
		final String sSpecQuery = "select tgs.* from testgroupspecification tgs, testgroupspecsampletype tgsst, testgrouptest tgt"
				+ " where tgs.nallottedspeccode = tgsst.nallottedspeccode and tgsst.nspecsampletypecode = tgt.nspecsampletypecode"
				+ " and tgt.nstatus = tgs.nstatus and tgt.nstatus = tgsst.nstatus and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.ntestgrouptestcode = "
				+ ntestgrouptestcode;
		return (TestGroupSpecification) jdbcUtilityFunction.queryForObject(sSpecQuery, TestGroupSpecification.class,
				jdbcTemplate);
	}

	public TestGroupTest getActiveTest(final int ntestgrouptestcode) throws Exception {
		final String sChectTestQry = "select ntestgrouptestcode from testgrouptest where ntestgrouptestcode = "
				+ ntestgrouptestcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (TestGroupTest) jdbcUtilityFunction.queryForObject(sChectTestQry, TestGroupTest.class, jdbcTemplate);
	}

	public TestGroupTestPredefinedParameter getPredefinedParameterByName(
			final TestGroupTestPredefinedParameter objPredefParameter) throws Exception {
		final String sDuplicateCheckQry = "select ntestgrouptestpredefcode from testgrouptestpredefparameter"
				+ " where spredefinedname = N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedname()) + "'"
				+ " and ntestgrouptestparametercode = " + objPredefParameter.getNtestgrouptestparametercode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(sDuplicateCheckQry,
				TestGroupTestPredefinedParameter.class, jdbcTemplate);
	}

	public String updatePredefinedParameterQuery(final TestGroupTestPredefinedParameter objPredefParameter,
			final UserInfo userInfo) throws Exception {

		return "update testgrouptestpredefparameter set ngradecode = " + objPredefParameter.getNgradecode()
				+ ", spredefinedname = N'" + stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedname())
				+ "'" + ", spredefinedsynonym = N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedsynonym()) + "'"
				+ ", spredefinedcomments = N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedcomments()) + "'"
				+ ", salertmessage = N'" + stringUtilityFunction.replaceQuote(objPredefParameter.getSalertmessage())
				+ "'" + ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
				+ ",nneedresultentryalert=" + objPredefParameter.getNneedresultentryalert() + ",nneedsubcodedresult="
				+ objPredefParameter.getNneedsubcodedresult() + " where ntestgrouptestpredefcode = "
				+ objPredefParameter.getNtestgrouptestpredefcode() + ";";
	}

	public String insertPredefinedParameterQuery(final TestGroupTestPredefinedParameter objPredefParameter,
			final UserInfo userInfo) throws Exception {

		return "insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode,"
				+ " spredefinedname,spredefinedsynonym,spredefinedcomments,salertmessage,nneedresultentryalert,nneedsubcodedresult,dmodifieddate, ndefaultstatus, nstatus,nsitecode) values ("

				+ objPredefParameter.getNtestgrouptestpredefcode() + ", "
				+ objPredefParameter.getNtestgrouptestparametercode() + "," + objPredefParameter.getNgradecode()
				+ ", N'" + stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedname()) + "',N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedsynonym()) + "',N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedcomments()) + "',N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSalertmessage()) + "',"
				+ objPredefParameter.getNneedresultentryalert() + "," + objPredefParameter.getNneedsubcodedresult()
				+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
				+ objPredefParameter.getNdefaultstatus() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + userInfo.getNmastersitecode()
				+ ");";
	}

	public TestGroupTestPredefinedParameter getPredefinedObjectByName(
			final TestGroupTestPredefinedParameter objPredefParameter) throws Exception {
		final String sDuplicateCheckQry = "select ntestgrouptestpredefcode, ndefaultstatus from testgrouptestpredefparameter"
				+ " where spredefinedname = N'"
				+ stringUtilityFunction.replaceQuote(objPredefParameter.getSpredefinedname()) + "'"
				+ " and ntestgrouptestparametercode = " + objPredefParameter.getNtestgrouptestparametercode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestpredefcode <> " + objPredefParameter.getNtestgrouptestpredefcode();
		return (TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(sDuplicateCheckQry,
				TestGroupTestPredefinedParameter.class, jdbcTemplate);
	}

	public String getComponentChildDetailsSelectQuery(final String sTestGroupTestCode) {

		String sChildDetailsQuery = "select * from testgrouptest where ntestgrouptestcode in (" + sTestGroupTestCode
				+ ")" + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		sChildDetailsQuery = sChildDetailsQuery + "select * from testgrouptestparameter where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		sChildDetailsQuery = sChildDetailsQuery
				+ "select * from testgrouptestpredefparameter tgtp, testgrouptestparameter tgp "
				+ " where tgp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgp.nstatus = tgtp.nstatus"
				+ " and tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgp.ntestgrouptestcode in (" + sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery
				+ "select * from testgrouptestcharparameter tgcp, testgrouptestparameter tgp where tgp.ntestgrouptestparametercode = tgcp.ntestgrouptestparametercode"
				+ " and tgp.nstatus = tgcp.nstatus and tgp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgp.ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery
				+ "select * from testgrouptestnumericparameter tgnp, testgrouptestparameter tgp where tgp.ntestgrouptestparametercode = tgnp.ntestgrouptestparametercode"
				+ " and tgp.nstatus = tgnp.nstatus and tgp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgp.ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery + "select * from testgrouptestformula where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		sChildDetailsQuery = sChildDetailsQuery + "select * from testgrouptestfile where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		sChildDetailsQuery = sChildDetailsQuery + "select * from testgrouptestmaterial where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		return sChildDetailsQuery;
	}

	public String getComponentChildDetailsUpdateQuery(final String sTestGroupTestCode, final UserInfo userInfo)
			throws Exception {
		String sChildDetailsQuery = "update testgrouptest set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestparameter set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestpredefparameter set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
				+ " where ntestgrouptestparametercode in (select tgtp.ntestgrouptestparametercode from testgrouptest tgt, testgrouptestparameter tgtp"
				+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tgt.ntestgrouptestcode in ("
				+ sTestGroupTestCode + ")) and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ";";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestcharparameter set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
				+ " where ntestgrouptestparametercode in (select ntestgrouptestcode from testgrouptest where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ") );";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestnumericparameter set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
				+ " where ntestgrouptestparametercode in (select ntestgrouptestcode from testgrouptest where ntestgrouptestcode in ("
				+ sTestGroupTestCode + "));";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestformula set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestfile set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		sChildDetailsQuery = sChildDetailsQuery + "update testgrouptestmaterial set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "where ntestgrouptestcode in ("
				+ sTestGroupTestCode + ");";
		return sChildDetailsQuery;
	}

	public TreeVersionTemplate checkTemplateIsRetiredOrNot(final int ntreeversiontempcode) throws Exception {
		String sRetiredValidation = "select ntransactionstatus from treeversiontemplate where ntreeversiontempcode="
				+ ntreeversiontempcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (TreeVersionTemplate) jdbcUtilityFunction.queryForObject(sRetiredValidation, TreeVersionTemplate.class,
				jdbcTemplate);
	}

	public List<TestGroupTestMaterial> getTestGroupTestMaterial(final int ntestgrouptestcode,
			final int ntestgrouptestmaterialcode, final UserInfo userInfo) throws Exception {

		final String sQuery = "select tgtm.ntestgrouptestmaterialcode,tgtm.sremarks,tgtm.ntestgrouptestcode,tgtm.nmaterialtypecode,tgtm.nmaterialcatcode,tgtm.nmaterialcode,"
				+ " mt.nmaterialtypecode,coalesce(mt.jsondata->'smaterialtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "' ,"
				+ " mt.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename,mc.nmaterialcatcode,mc.smaterialcatname,m.nmaterialcode,"
				+ " coalesce(m.jsondata->>'Material Name') as smaterialname "
				+ " from testgrouptestmaterial tgtm,materialtype mt,materialcategory mc,material m "
				+ " where mt.nmaterialtypecode=tgtm.nmaterialtypecode and mc.nmaterialcatcode=tgtm.nmaterialcatcode and"
				+ " tgtm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " mt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " mc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and" + " m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and m.nmaterialcode=tgtm.nmaterialcode and tgtm.ntestgrouptestcode=" + ntestgrouptestcode
				+ " order by tgtm.ntestgrouptestmaterialcode";
		// + " " + sSubQry ;
		final List<TestGroupTestMaterial> list = (List<TestGroupTestMaterial>) jdbcTemplate.query(sQuery,
				new TestGroupTestMaterial());
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(commonFunction.getMultilingualMessageList(list, Arrays.asList("smaterialname"),
				userInfo.getSlanguagefilename()), new TypeReference<List<TestGroupTestMaterial>>() {
				});
	}

	public ResponseEntity<Object> getTestGroupMaterial(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		boolean isMaterialBasedGet = false;
		ObjectMapper objMapper = new ObjectMapper();
		final TestGroupTestMaterial objMaterial = objMapper.convertValue(inputMap.get("testgrouptestmaterial"),
				TestGroupTestMaterial.class);

		Map<String, Object> outputMap = new HashMap<String, Object>();
		List<TestGroupTestMaterial> listTGTM = getTestGroupTestMaterial(objMaterial.getNtestgrouptestcode(), 0,
				userInfo);
		outputMap.put("TestGroupTestMaterial", listTGTM);
		if (!listTGTM.isEmpty()) {
			List<TestGroupTestMaterial> selectedMaterial = new ArrayList<TestGroupTestMaterial>();
			if (isMaterialBasedGet) {
				selectedMaterial = Arrays.asList(listTGTM.get(listTGTM.size() - 1));
			} else {
				selectedMaterial = listTGTM.stream()
						.filter(x -> x.getNtestgrouptestmaterialcode() == objMaterial.getNtestgrouptestmaterialcode())
						.collect(Collectors.toList());
			}
			if (!selectedMaterial.isEmpty()) {
				outputMap.put("selectedMaterial", commonFunction.getMultilingualMessageList(selectedMaterial,
						Arrays.asList("sdisplaystatus"), userInfo.getSlanguagefilename()).get(0));
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public String insertClinicalSpecQuery(final TestGroupTestClinicalSpec objPredefParameter, UserInfo objUserInfo)
			throws Exception {

		// ALPD-5054 added by Dhanushya RI,To insert fromage period and toage period
		return "insert into testgrouptestclinicalspec (ntestgrouptestclinicspeccode, ntestgrouptestparametercode, ngendercode,nfromage,ntoage,shigha,shighb"
				+ ",slowa, slowb,sminlod, smaxlod,sminloq, smaxloq,sdisregard,sresultvalue,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate, nstatus,nsitecode,ngradecode,nfromageperiod,ntoageperiod) values ("
				+ objPredefParameter.getNtestgrouptestclinicspeccode() + ", "
				+ objPredefParameter.getNtestgrouptestparametercode() + ", " + objPredefParameter.getNgendercode() + ","
				+ objPredefParameter.getNfromage() + "," + objPredefParameter.getNtoage() + ",'"
				+ objPredefParameter.getShigha() + "'," + " '" + objPredefParameter.getShighb() + "','"
				+ objPredefParameter.getSlowa() + "','" + objPredefParameter.getSlowb() + "','"
				+ objPredefParameter.getSminlod() + "','" + objPredefParameter.getSmaxlod() + "','"
				+ objPredefParameter.getSminloq() + "','" + objPredefParameter.getSmaxloq() + "','"
				+ objPredefParameter.getSdisregard() + "','" + objPredefParameter.getSresultvalue() + "','"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNtimezonecode() + ","
				+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + objUserInfo.getNmastersitecode()
				+ "," + objPredefParameter.getNgradecode() + "," + objPredefParameter.getNfromageperiod() + ","
				+ objPredefParameter.getNtoageperiod() + ");";
	}

	public Map<String, Object> getTestGroupTestClinicalSpec(final int ntestgrouptestparametercode,
			final UserInfo userInfo) throws Exception {
		String sQuery = "select ttpp.ntestgrouptestclinicspeccode, ttpp.ntestgrouptestparametercode, ttpp.nfromage,ttpp.ntoage,case when  ttpp.shigha='null' then '' else ttpp.shigha  end as shigha,"
				+ " case when  ttpp.shighb='null' then '' else ttpp.shighb  end as shighb, case when  ttpp.slowa='null' then '' else ttpp.slowa  end as slowa, case when  ttpp.slowb='null' then '' else ttpp.slowb  end as slowb,"
				+ " case when  ttpp.sresultvalue='null' then '' else ttpp.sresultvalue  end as sresultvalue,"

				+ " case when  ttpp.sminlod='null' then '' else ttpp.sminlod  end as sminlod,"
				+ " case when  ttpp.smaxlod='null' then '' else ttpp.smaxlod  end as smaxlod,"
				+ " case when  ttpp.sminloq='null' then '' else ttpp.sminloq  end as sminloq,"
				+ " case when  ttpp.smaxloq='null' then '' else ttpp.smaxloq  end as smaxloq,"
				+ " case when  ttpp.sdisregard='null' then '' else ttpp.sdisregard  end as sdisregard,"
				+ " coalesce(g.jsondata->'sgendername'->>'" + userInfo.getSlanguagetypecode()
				+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
				+ " coalesce(gd.jsondata->'sdisplayname'->>'en-US',gd.jsondata->'sdisplayname'->>'en-US') sgradename,ttpp.ngradecode,"
				+ " COALESCE(p1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "', p1.jsondata->'speriodname'->>'en-US')  AS sfromageperiod, "
				+ "	COALESCE(p2.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "', p2.jsondata->'speriodname'->>'en-US')  AS stoageperiod "
				+ " from testgrouptestclinicalspec ttpp, gender g,grade gd,period p1,period p2 "
				+ " where gd.ngradecode=ttpp.ngradecode and gd.nstatus=g.nstatus and ttpp.ngendercode=g.ngendercode"
				+ " and p1.nperiodcode=ttpp.nfromageperiod and p2.nperiodcode=ttpp.ntoageperiod and g.nstatus = ttpp.nstatus and p1.nstatus=ttpp.nstatus and p2.nstatus=ttpp.nstatus and ttpp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ttpp.ntestgrouptestparametercode = " + ntestgrouptestparametercode
				+ " order by ttpp.nfromage asc";
		Map<String, Object> outputMap = new HashMap<String, Object>();

		outputMap.put("TestGroupTestClinicalSpec", jdbcTemplate.query(sQuery, new TestGroupTestClinicalSpec()));
		return outputMap;
	}

	public String updateClinicalSpecQuery(final TestGroupTestClinicalSpec objPredefParameter, UserInfo objUserInfo)
			throws Exception {
		// ALPD-5054 added by Dhanushya RI,To update fromage period and toage period
		return "update testgrouptestclinicalspec set ngendercode = " + objPredefParameter.getNgendercode()
				+ ",nfromage = " + objPredefParameter.getNfromage() + ",ntoage = " + objPredefParameter.getNtoage()
				+ ",shigha = '" + objPredefParameter.getShigha() + "'," + " shighb ='" + objPredefParameter.getShighb()
				+ "',slowa = '" + objPredefParameter.getSlowa() + "'," + " slowb = '" + objPredefParameter.getSlowb()
				+ "',sminlod = '" + objPredefParameter.getSminlod() + "'," + " smaxlod = '"
				+ objPredefParameter.getSmaxlod() + "',sminloq = '" + objPredefParameter.getSminloq() + "',"
				+ " smaxloq = '" + objPredefParameter.getSmaxloq() + "',sdisregard = '"
				+ objPredefParameter.getSdisregard() + "',ngradecode=" + objPredefParameter.getNgradecode() + ","
				+ " sresultvalue = '" + objPredefParameter.getSresultvalue() + "'," + " dmodifieddate ='"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',ntzmodifieddate= "
				+ objUserInfo.getNtimezonecode() + ",nfromageperiod=" + objPredefParameter.getNfromageperiod()
				+ ",ntoageperiod=" + objPredefParameter.getNtoageperiod() + ",noffsetdmodifieddate= "
				+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
				+ " where ntestgrouptestclinicspeccode = " + objPredefParameter.getNtestgrouptestclinicspeccode() + ";";
	}

	public List<Integer> clinicalSpecValidation(final TestGroupTestClinicalSpec objPredefParameter) throws Exception {

		final String sDuplicateCheckQry = " (select generate_series(nfromage,ntoage) from testgrouptestclinicalspec where ngendercode="
				+ objPredefParameter.getNgendercode() + " and ntestgrouptestparametercode="
				+ objPredefParameter.getNtestgrouptestparametercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " intersect "
				+ " SELECT * FROM generate_series(" + objPredefParameter.getNfromage() + ","
				+ objPredefParameter.getNtoage() + ")";
		return jdbcTemplate.queryForList(sDuplicateCheckQry, Integer.class);
	}

	public List<Integer> clinicalSpecObjectByGender(final TestGroupTestClinicalSpec objPredefParameter)
			throws Exception {
		final String sDuplicateCheckQry = " (select generate_series(nfromage,ntoage) from testgrouptestclinicalspec where ngendercode="
				+ objPredefParameter.getNgendercode() + " and ntestgrouptestparametercode="
				+ objPredefParameter.getNtestgrouptestparametercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestclinicspeccode <> "
				+ objPredefParameter.getNtestgrouptestclinicspeccode() + ") " + " intersect "
				+ " SELECT * FROM generate_series(" + objPredefParameter.getNfromage() + ","
				+ objPredefParameter.getNtoage() + ")";
		return jdbcTemplate.queryForList(sDuplicateCheckQry, Integer.class);
	}

	public Map<String, Object> getProjectMasterStatusCode(final int nprojectMasterCode, UserInfo userInfo) {
		final String strQuery = "select ntransactionstatus from projectmasterhistory where nprojectmastercode="
				+ nprojectMasterCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nprojectmasterhistorycode desc limit 1";

		Integer nprojectcodeStatus = jdbcTemplate.queryForObject(strQuery, Integer.class);

		final Map<String, Object> map = new HashMap<String, Object>();
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		if (nprojectcodeStatus == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTALREADYCOMPLETE");
		} else if (nprojectcodeStatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTALREADYRETIRE");

		} else if (nprojectcodeStatus == Enumeration.TransactionStatus.CLOSED.gettransactionstatus()) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_PROJECTALREADYCLOSE");

		}
//		return new ResponseEntity<Object>(map, HttpStatus.OK);
		return map;

	}

	public Map<String, Object> getTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String jsonstr = "";
		List<Map<String, Object>> lstrulesEngine = new ArrayList<>();
		ObjectMapper objmapper = new ObjectMapper();

		try {

			jsonstr = jdbcTemplate.queryForObject("select json_agg(x.*) from "
					+ "(select  t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus,tre.* from testgrouprulesengine tre,transactionstatus t "
					+ " where tre.ntransactionstatus=t.ntranscode " + "  and tre.ntestgrouptestcode= "
					+ inputMap.get("ntestgrouptestcode") + " and tre.nsitecode = " + userInfo.getNmastersitecode() + " "
					+ " and tre.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nruleexecorder asc)x", String.class);

		} catch (Exception e) {
			jsonstr = null;
		}
		if (jsonstr != null) {
			lstrulesEngine = objmapper.readValue(jsonstr.toString(), new TypeReference<List<Map<String, Object>>>() {
			});
		}

		outputMap.put("RulesEngine", lstrulesEngine);
		if (lstrulesEngine.size() > 0) {
			outputMap.put("SelectedRulesEngine", lstrulesEngine.get(0));
		} else {
			outputMap.put("SelectedRulesEngine", new HashMap<>());
		}

		return outputMap;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getTestGroupSpecification(final UserInfo objUserInfo,
			final TreeTemplateManipulation objTree, final boolean isEditAction,
			final TestGroupSpecification objTestGroupSpec) throws Exception {

		Map<String, Object> outputMap = new HashMap<String, Object>();

		List<TestGroupSpecification> lstTestGroupSpecification = getTestGroupSpecification(
				objTree.getNtemplatemanipulationcode(), objUserInfo.getNuserrole(), objUserInfo);
		outputMap.put("TestGroupSpecification", lstTestGroupSpecification);

		if (lstTestGroupSpecification != null && lstTestGroupSpecification.size() > 0) {
			TestGroupSpecification selectedSpecification = null;

			if (isEditAction) {
				List<TestGroupSpecification> selectedSpecificationList = lstTestGroupSpecification.stream()
						.filter(x -> x.getNallottedspeccode() == objTestGroupSpec.getNallottedspeccode())
						.collect(Collectors.toList());
				if (!selectedSpecificationList.isEmpty()) {
					selectedSpecification = selectedSpecificationList.get(0);
				} else { // ALPD-1878
					List<TestGroupSpecification> selectedSpecList = lstTestGroupSpecification.stream()
							.filter(x -> x.getNallottedspeccode() != objTestGroupSpec.getNallottedspeccode())
							.collect(Collectors.toList());
					selectedSpecification = selectedSpecList.get(selectedSpecList.size() - 1);
				}
			} else {
				selectedSpecification = lstTestGroupSpecification.get(lstTestGroupSpecification.size() - 1);
			}
			List<ApprovalRoleActionDetail> listActions = new ArrayList<ApprovalRoleActionDetail>();
			List<TestGroupSpecSampleType> lstTGSST = new ArrayList<TestGroupSpecSampleType>();

			if (selectedSpecification != null) {
				outputMap.putAll(
						(Map<String, Object>) getSpecificationFile(objUserInfo, selectedSpecification).getBody());

				outputMap.putAll(
						(Map<String, Object>) getSpecificationHistory(objUserInfo, selectedSpecification).getBody());

				outputMap.put("SelectedSpecification", selectedSpecification);

				lstTGSST = getTestGroupSampleType(selectedSpecification);

				if (selectedSpecification.getNapproveconfversioncode() > 0) {
					listActions = getApprovalConfigAction(selectedSpecification, objUserInfo);
				}
			}
			outputMap.put("ApprovalRoleActionDetail", listActions);

			if (lstTGSST != null && lstTGSST.size() > 0) {
				outputMap.putAll(getTestDetails(lstTGSST.get(lstTGSST.size() - 1).getNspecsampletypecode(), 0,
						objUserInfo, false));
				outputMap.put("SelectedComponent", lstTGSST.get(lstTGSST.size() - 1));

			} else {
				outputMap.put("TestGroupTest", null);
				outputMap.put("TestGroupTestParameter", null);
				outputMap.put("SelectedTest", null);
				outputMap.put("SelectedComponent", null);
				outputMap.put("TestGroupTestMaterial", null);
				outputMap.put("RulesEngine", null);
			}
			outputMap.put("TestGroupSpecSampleType", lstTGSST);
		} else {
			outputMap.putAll(getEmptyMap());
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	public List<ApprovalRoleActionDetail> getApprovalConfigAction(final TestGroupSpecification objGroupSpecification,
			final UserInfo userInfo) throws Exception {
		final String sActionQry = "select acad.ntransactionstatus,acad.nlevelno,coalesce(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	 ts.jsondata->'sactiondisplaystatus'->>'en-US') as  sactiondisplaystatus, acr.nesignneed"
				+ " from approvalroleactiondetail acad,approvalconfigrole acr,approvalconfig ac,transactionstatus ts "
				+ " where acad.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acad.nuserrolecode = " + userInfo.getNuserrole()
				+ " and acad.napprovalconfigrolecode = acr.napprovalconfigrolecode and acr.napprovalconfigcode = ac.napprovalconfigcode "
				+ " and acr.napproveconfversioncode = " + objGroupSpecification.getNapproveconfversioncode()
				+ " and acad.ntransactionstatus = ts.ntranscode" + " and ac.nregtypecode = "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and ac.nregsubtypecode = "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and ac.napprovalsubtypecode = "
				+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + " and ts.nstatus =  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and acad.nsitecode = acr.nsitecode and acr.nsitecode = ac.nsitecode " + " and acr.nsitecode = "
				+ userInfo.getNmastersitecode() + ";";

		return (List<ApprovalRoleActionDetail>) jdbcTemplate.query(sActionQry, new ApprovalRoleActionDetail());
	}

	public Map<String, Object> getEmptyMap() {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("SelectedTest", null);
		outputMap.put("TestGroupTest", null);
		outputMap.put("TestGroupTestParameter", null);
		outputMap.put("TestGroupSpecFile", null);
		outputMap.put("TestGroupSpecificationHistory", null);
		outputMap.put("TestGroupSpecSampleType", null);
		outputMap.put("SelectedSpecification", null);
		outputMap.put("TestGroupTestMaterial", null);
		outputMap.put("RulesEngine", null);
		outputMap.put("SelectedRulesEngine", null);

		return outputMap;
	}

	public ResponseEntity<Object> getSpecificationFile(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String queryformat = "COALESCE(TO_CHAR(tgsf.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') ";

		final String sSpecFileQry = "select tgsf.noffsetdcreateddate,tgsf.nallottedspeccode, tgsf.nspecfilecode, tgsf.nlinkcode, tgsf.nattachmenttypecode,"
				+ " tgsf.sfilename, tgsf.sdescription, COALESCE(at.jsondata->'sattachmenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " at.jsondata->'sattachmenttype'->>'en-US') as stypename, case when tgsf.nlinkcode = -1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname,"
				+ " case when tgsf.nlinkcode = -1 then cast(tgsf.nfilesize as text) else '-' end sfilesize,"
				+ " case when tgsf.nlinkcode = -1 then " + queryformat + " else '-' end screateddate"
				+ " from testgroupspecfile tgsf, attachmenttype at, linkmaster lm"
				+ " where at.nattachmenttypecode = tgsf.nattachmenttypecode and lm.nlinkcode = tgsf.nlinkcode"
				+ " and at.nstatus = tgsf.nstatus and lm.nstatus = tgsf.nstatus" + " and tgsf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsf.nallottedspeccode = "
				+ objSpecification.getNallottedspeccode() + " and tgsf.nsitecode=" + userInfo.getNmastersitecode()
				+ " and at.nsitecode=" + userInfo.getNmastersitecode() + " and lm.nsitecode="
				+ userInfo.getNmastersitecode() + ";";
		outputMap.put("TestGroupSpecFile",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(sSpecFileQry, new TestGroupSpecFile()),
						Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
						false));
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSpecificationHistory(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "TO_CHAR(tgsh.dtransactiondate,'" + userInfo.getSpgsitedatetime() + "') ";

		final String historyQuery = "select tgsh.nallottedspeccode, tgsh.ntransactionstatus, "
				+ "tgsh.nusercode, tgsh.nuserrolecode," + queryformat + " as sdtransactiondate,"
				+ "tgsh.scomments,CONCAT( u.sfirstname,' ',u.slastname) as susername,"
				+ " ur.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "	 ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from testgroupspecificationhistory tgsh, users u, userrole ur, transactionstatus ts"
				+ " where u.nusercode = tgsh.nusercode and ur.nuserrolecode = tgsh.nuserrolecode and ts.ntranscode = tgsh.ntransactionstatus"
				+ " and u.nstatus = tgsh.nstatus and ur.nstatus = tgsh.nstatus and ts.nstatus = tgsh.nstatus"
				+ " and tgsh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgsh.nallottedspeccode = " + objSpecification.getNallottedspeccode() + " and tgsh.nsitecode="
				+ userInfo.getNmastersitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ur.nsitecode=" + userInfo.getNmastersitecode() + "";
		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(
				jdbcTemplate.query(historyQuery, new TestGroupSpecificationHistory()),
				Arrays.asList("sdtransactiondate"), null, userInfo, true, Arrays.asList("stransdisplaystatus"), false);
		outputMap.put("TestGroupSpecificationHistory", listSpec);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
}
