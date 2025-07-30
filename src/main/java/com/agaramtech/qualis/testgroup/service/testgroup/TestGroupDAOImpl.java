package com.agaramtech.qualis.testgroup.service.testgroup;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.ApprovalRoleValidationDetail;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.configuration.model.TreetempTranstestGroup;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ProjectMasterCommonFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.materialmanagement.model.Material;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAOImpl;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupVersion;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupmanagement;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestMaterial;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class TestGroupDAOImpl implements TestGroupDAO {

	//private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectMasterCommonFunction projectMasterCommonFunction;
	private final TestGroupCommonFunction testGroupCommonFunction;
	private final ProjectTypeDAOImpl projectTypeDAOImpl;


	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestGroup(UserInfo objUserInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		List<SampleType> lstSampleType = objMapper.convertValue(
				commonFunction.getMultilingualMessageList(getSampleType(objUserInfo.getNmastersitecode(), objUserInfo),
						Arrays.asList("ssampletypename"), objUserInfo.getSlanguagefilename()),
				new TypeReference<List<SampleType>>() {
				});
		if (!lstSampleType.isEmpty()) {
			final SampleType objSampleType = lstSampleType.get(0);
			outputMap.put("SampleTypeDesign", getSampleTypeDesign(objSampleType.getNsampletypecode()));

			if (objSampleType.getNformcode() == Enumeration.FormCode.PRODUCTCATEGORY.getFormCode()) {
				List<ProductCategory> listProductCategory = getProductCategory(objUserInfo.getNmastersitecode(),
						lstSampleType.get(0));
				if (!listProductCategory.isEmpty()) {
					List<Product> listProduct = getProduct(objUserInfo.getNmastersitecode(),
							listProductCategory.get(0));
					int nprojectMasterCode = -1;
					if (objSampleType.getNsampletypecode() == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {

						final List<ProjectType> projectTypeList = (List<ProjectType>) projectTypeDAOImpl
								.getApprovedProjectType(objUserInfo).getBody();
						//ALPD-4037 Added by Subashini
						outputMap.put("ProjectTypeList", projectTypeList);

						if (projectTypeList.size() > 0) {
							final List<ProjectMaster> projectList = (List<ProjectMaster>)
									((Map<String, Object>) projectMasterCommonFunction.getApprovedProjectByProjectType(
											projectTypeList.get(0).getNprojecttypecode(),objUserInfo)									
											.getBody()).get("ProjectMasterList");
							//ALPD-4037 Added by Subashini
							outputMap.put("ProjectMasterList", projectList);
							if (projectList.size() > 0) {
								nprojectMasterCode = projectList.get(0).getNprojectmastercode();
							}
						}
					}
					if (!listProduct.isEmpty()) {
						//ALPD-4037 Added by Subashini
						outputMap.putAll(getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listProductCategory.get(0).getNproductcatcode(), listProduct.get(0).getNproductcode(),
								nprojectMasterCode, objUserInfo));
					} else {
						//ALPD-4037 Added by Subashini
						outputMap.putAll(getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listProductCategory.get(0).getNproductcatcode(), 0, nprojectMasterCode, objUserInfo));
					}
					outputMap.put("Product", listProduct);
					outputMap.put("ProductCategory", listProductCategory);
				}

			} else if (objSampleType.getNformcode() == Enumeration.FormCode.INSTRUMENTCATEGORY.getFormCode()) {
				List<InstrumentCategory> listInstrumentCategory = getInstrumentCategory(
						objUserInfo.getNmastersitecode(), lstSampleType.get(0));
				if (!listInstrumentCategory.isEmpty()) {
					List<Instrument> listInstrument = getInstrument(objUserInfo.getNmastersitecode(),
							listInstrumentCategory.get(0));
					if (!listInstrument.isEmpty()) {
						outputMap = getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listInstrumentCategory.get(0).getNinstrumentcatcode(),
								listInstrument.get(0).getNinstrumentcode(), -1, objUserInfo);
					} else {
						outputMap = getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listInstrumentCategory.get(0).getNinstrumentcatcode(), 0, -1, objUserInfo);
					}

					outputMap.put("Product", listInstrument);
					outputMap.put("ProductCategory", listInstrumentCategory);
				}
			} else if (objSampleType.getNformcode() == Enumeration.FormCode.MATERIALCATEGORY.getFormCode()) {
				List<MaterialCategory> listMaterialCategory = getMaterialCategory(objUserInfo.getNmastersitecode(),
						lstSampleType.get(0));
				if (!listMaterialCategory.isEmpty()) {
					// Need to include the material
					List<Material> listInstrument = new ArrayList<Material>();
					// if(listMaterialCategory.get(0).getNcategorybasedflow()==Enumeration.TransactionStatus.NO.gettransactionstatus())
					// {
					listInstrument = getMaterial(objUserInfo, listMaterialCategory.get(0));
					// }
					if (!listInstrument.isEmpty()) {
						outputMap = getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listMaterialCategory.get(0).getNmaterialcatcode(),
								listInstrument.get(0).getNmaterialcode(), -1, objUserInfo);
					} else {
						outputMap = getTestGroupFilterBasedDetail(lstSampleType.get(0),
								listMaterialCategory.get(0).getNmaterialcatcode(), 0, -1, objUserInfo);
					}

					outputMap.put("Product", listInstrument);
					outputMap.put("ProductCategory", listMaterialCategory);
				}
			}
			outputMap.put("SampleType", lstSampleType);
			outputMap.put("isrulesenginerequired", objSampleType.getNrulesenginerequired());
			return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_STUDYPLANTEMPLATEISNOTAVAILABLE", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo objUserInfo, final SampleType objSampleType)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();

		if (objSampleType.getNformcode() == Enumeration.FormCode.PRODUCTCATEGORY.getFormCode()) { // Product
			List<ProductCategory> listCategory = getProductCategory(objUserInfo.getNmastersitecode(), objSampleType);
			outputMap.put("ProductCategory", listCategory);
			List<Product> listProduct = new ArrayList<Product>();
			if (!listCategory.isEmpty()) {

				if (listCategory.get(0).getNcategorybasedflow() == Enumeration.TransactionStatus.NO
						.gettransactionstatus()) {
					listProduct = getProduct(objUserInfo.getNmastersitecode(), listCategory.get(0));
				}

				List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(
						objSampleType.getNsampletypecode(), objSampleType.getNformcode(),
						listCategory.get(0).getNproductcatcode(), objUserInfo.getNmastersitecode(), objUserInfo);
				outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);
			}

			outputMap.put("Product", listProduct);
			if (objSampleType.getNsampletypecode() == Enumeration.SampleType.PROJECTSAMPLETYPE.getType()) {
				final List<ProjectType> projectTypeList = (List<ProjectType>) projectTypeDAOImpl
						.getApprovedProjectType(objUserInfo).getBody();
				outputMap.put("ProjectTypeList", projectTypeList);
				if (projectTypeList.size() > 0) {

					final Map<String, Object> projectMasterList = (Map<String, Object>) projectMasterCommonFunction
							.getApprovedProjectByProjectType(projectTypeList.get(0).getNprojecttypecode(), objUserInfo)
							.getBody();

					outputMap.put("SelectedProjectType", projectTypeList.get(0));
					outputMap.putAll(projectMasterList);

				}

			} else {
				outputMap.put("ProjectTypeList", new ArrayList<>());
				outputMap.put("ProjectMasterList", new ArrayList<>());
			}
		} else if (objSampleType.getNformcode() == Enumeration.FormCode.INSTRUMENTCATEGORY.getFormCode()) { // Instrument
			List<InstrumentCategory> listCategory = getInstrumentCategory(objUserInfo.getNmastersitecode(),
					objSampleType);
			outputMap.put("ProductCategory", listCategory);
			List<Instrument> listInstrument = new ArrayList<Instrument>();
			if (!listCategory.isEmpty()) {

				if (listCategory.get(0).getNcategorybasedflow() == Enumeration.TransactionStatus.NO
						.gettransactionstatus()) {
					listInstrument = getInstrument(objUserInfo.getNmastersitecode(), listCategory.get(0));
				}

				List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(
						objSampleType.getNsampletypecode(), objSampleType.getNformcode(),
						listCategory.get(0).getNinstrumentcatcode(), objUserInfo.getNmastersitecode(), objUserInfo);
				outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);

			}
			outputMap.put("Product", listInstrument);
		} else if (objSampleType.getNformcode() == Enumeration.FormCode.MATERIALCATEGORY.getFormCode()) { // Material
			List<MaterialCategory> listCategory = getMaterialCategory(objUserInfo.getNmastersitecode(), objSampleType);
			outputMap.put("ProductCategory", listCategory);
			List<Material> listInstrument = new ArrayList<Material>();
			if (!listCategory.isEmpty()) {
				if (listCategory.get(0).getNcategorybasedflow() == Enumeration.TransactionStatus.NO
						.gettransactionstatus()) {
					listInstrument = getMaterial(objUserInfo, listCategory.get(0));
				}

				List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(
						objSampleType.getNsampletypecode(), objSampleType.getNformcode(),
						listCategory.get(0).getNmaterialcatcode(), objUserInfo.getNmastersitecode(), objUserInfo);
				outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);
			}
			outputMap.put("Product", listInstrument);
		} else {

		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProduct(final UserInfo objUserInfo, final Map<String, Object> categoryMap,
			final SampleType objSampleType) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		if (objSampleType.getNformcode() == Enumeration.FormCode.PRODUCTCATEGORY.getFormCode()) { // Product
			ProductCategory objProductCategory = objMapper.convertValue(categoryMap, ProductCategory.class);
			List<Product> listProduct = new ArrayList<Product>();
			if (objProductCategory.getNcategorybasedflow() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				listProduct = getProduct(objUserInfo.getNmastersitecode(), objProductCategory);
			}

			List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(objSampleType.getNsampletypecode(),
					objSampleType.getNformcode(), objProductCategory.getNproductcatcode(),
					objUserInfo.getNmastersitecode(), objUserInfo);
			outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);

			outputMap.put("Product", listProduct);
		} else if (objSampleType.getNformcode() == Enumeration.FormCode.INSTRUMENTCATEGORY.getFormCode()) { // Instrument
			InstrumentCategory objInstrumentCategory = objMapper.convertValue(categoryMap, InstrumentCategory.class);
			List<Instrument> listInstrument = new ArrayList<Instrument>();
			listInstrument = getInstrument(objUserInfo.getNmastersitecode(), objInstrumentCategory);

			List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(objSampleType.getNsampletypecode(),
					objSampleType.getNformcode(), objInstrumentCategory.getNproductcatcode(),
					objUserInfo.getNmastersitecode(), objUserInfo);
			outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);

			outputMap.put("Product", listInstrument);
		} else if (objSampleType.getNformcode() == Enumeration.FormCode.MATERIALCATEGORY.getFormCode()) { // Material
			MaterialCategory objMaterialCategory = objMapper.convertValue(categoryMap, MaterialCategory.class);
			List<Material> listMaterial = new ArrayList<Material>();
			listMaterial = getMaterial(objUserInfo, objMaterialCategory);
			List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(objSampleType.getNsampletypecode(),
					objSampleType.getNformcode(), objMaterialCategory.getNproductcatcode(),
					objUserInfo.getNmastersitecode(), objUserInfo);
			outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);
			outputMap.put("Product", listMaterial);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTreeVersionTemplate(final UserInfo objUserInfo, final SampleType objSampleType,
			final Map<String, Object> categoryMap, Map<String, Object> productMap, Map<String, Object> projecttMap)
					throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll(getTestGroupFilterBasedDetail(objSampleType, (int) categoryMap.get("nproductcatcode"),
				(int) productMap.get("nproductcode"), (int) productMap.get("nprojectmastercode"), objUserInfo));
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	private Map<String, Object> getTreedata(final UserInfo objUserInfo, final int nSampleTypeCode,
			final int nproductCatCode, final int nproductCode, final int nTreeVersionTempCode,
			final boolean isEditAction, String sNode, final int primarykey, final int nprojectCode,final boolean isCopySpec) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		List<TreeTemplateManipulation> lstTreeTemplateManipulation = getTreeTemplateManipulation(
				Enumeration.FormCode.TESTGROUP.getFormCode(), nproductCatCode, nproductCode, nTreeVersionTempCode,
				nSampleTypeCode, nprojectCode,isCopySpec);
		outputMap.putAll(testGroupCommonFunction.getHierarchicalList(lstTreeTemplateManipulation, isEditAction,
				sNode, primarykey));

		outputMap.put("TreeTemplateManipulation", lstTreeTemplateManipulation);
		return outputMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getTreeTemplateManipulation(final UserInfo objUserInfo, final int nSampleTypeCode,
			final int nproductCatCode, final int nproductCode, final int nTreeVersionTempCode,
			final boolean isEditAction, final int templatemanipulationcode, final String sNode, final int nprojectCode)
					throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();

		outputMap.putAll(getTreedata(objUserInfo, nSampleTypeCode, nproductCatCode, nproductCode, nTreeVersionTempCode,
				isEditAction, sNode, templatemanipulationcode, nprojectCode,false));

		final int ntemplatemanipulationcode = (int) outputMap.get("primarykey");
		List<TreeTemplateManipulation> lstTreeTemplateManipulation = (List<TreeTemplateManipulation>) outputMap
				.get("TreeTemplateManipulation");
		List<TestGroupSpecification> lstTestGroupSpecification = new ArrayList<TestGroupSpecification>();
		List<ApprovalRoleActionDetail> listActions = new ArrayList<ApprovalRoleActionDetail>();
		TreeTemplateManipulation selectedTreeTemplateManipulation = null;
		if (!lstTreeTemplateManipulation.isEmpty()) {
			selectedTreeTemplateManipulation = new TreeTemplateManipulation();

			selectedTreeTemplateManipulation = lstTreeTemplateManipulation.stream()
					.filter(x -> x.getNtemplatemanipulationcode() == ntemplatemanipulationcode)
					.collect(Collectors.toList()).get(0);

			lstTestGroupSpecification = testGroupCommonFunction.getTestGroupSpecification(
					selectedTreeTemplateManipulation.getNtemplatemanipulationcode(), objUserInfo.getNuserrole(),
					objUserInfo);
			TestGroupSpecification selectedSpecification = null;
			List<TestGroupSpecSampleType> lstTGSST = new ArrayList<TestGroupSpecSampleType>();
			if (!lstTestGroupSpecification.isEmpty()) {
				selectedSpecification = lstTestGroupSpecification.get(lstTestGroupSpecification.size() - 1);
				if (selectedSpecification.getNapproveconfversioncode() > 0) {
					listActions = getApprovalConfigAction(selectedSpecification, objUserInfo);
				}

				outputMap.putAll((Map<String, Object>) testGroupCommonFunction
						.getSpecificationFile(objUserInfo, selectedSpecification).getBody());
				outputMap.putAll((Map<String, Object>) testGroupCommonFunction
						.getSpecificationHistory(objUserInfo, selectedSpecification).getBody());
				lstTGSST = testGroupCommonFunction.getTestGroupSampleType(selectedSpecification);
				if (!lstTGSST.isEmpty()) {
					outputMap.put("SelectedComponent", lstTGSST.get(lstTGSST.size() - 1));
					outputMap.putAll(testGroupCommonFunction.getTestDetails(
							lstTGSST.get(lstTGSST.size() - 1).getNspecsampletypecode(), 0, objUserInfo, false));
				}
			} else {
				outputMap.putAll(testGroupCommonFunction.getEmptyMap());
			}
			outputMap.put("TestGroupSpecSampleType", lstTGSST);
			outputMap.put("SelectedSpecification", selectedSpecification);
		} else {
			outputMap.putAll(testGroupCommonFunction.getEmptyMap());
		}
		outputMap.put("selectedNode", selectedTreeTemplateManipulation);
		outputMap.put("TestGroupSpecification", lstTestGroupSpecification);
		outputMap.put("ApprovalRoleActionDetail", listActions);
		return outputMap;
	}


	private Map<String, Object> getTestGroupFilterBasedDetail(SampleType objSampleType, int nproductCatCode,
			int nproductCode, int nprojectMasterCode, UserInfo objUserInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		List<TreeVersionTemplate> lstTreeVersionTemplate = getTemplateVersion(objSampleType.getNsampletypecode(),
				objSampleType.getNformcode(), nproductCatCode, objUserInfo.getNmastersitecode(), objUserInfo);
		outputMap.put("TreeVersionTemplate", lstTreeVersionTemplate);
		if (!lstTreeVersionTemplate.isEmpty()) {
			outputMap.putAll(getTreeTemplateManipulation(objUserInfo, objSampleType.getNsampletypecode(),
					nproductCatCode, nproductCode, lstTreeVersionTemplate.get(0).getNtreeversiontempcode(), false, 0,
					"", nprojectMasterCode));
		}
		return outputMap;
	}

	/*
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fnTestGroupTestgetSequence(Map<String, Object> objMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		List<TestGroupTest> lstTGT = objMapper.convertValue(objMap.get("TestGroupTest"),
				new TypeReference<List<TestGroupTest>>() {
				});
		int nspecsampletypecode = (int) objMap.get("nspecsampletypecode");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String stestcode = "";
		int ntestcount = 0;
		String str1 = "select ntestcode from testgrouptest where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nspecsampletypecode="
				+ nspecsampletypecode + "";
		List<TestGroupTest> lstAvailTGT = (List<TestGroupTest>) jdbcTemplate.query(str1, new TestGroupTest());
		List<TestGroupTest> filteredList = lstTGT.stream().filter(
				source -> lstAvailTGT.stream().noneMatch(check -> source.getNtestcode() == check.getNtestcode()))
				.collect(Collectors.toList());

		ntestcount = (filteredList != null && filteredList.size() > 0) ? filteredList.size() : 0;
		stestcode = stringUtilityFunction.fnDynamicListToString(filteredList, "getntestcode");
		if (stestcode.isEmpty()) {
			stestcode = "0";
		}
		String sQuery = "select nparametertypecode from testparameter where ntestcode in (" + stestcode
				+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<TestParameter> lstTestParameter = jdbcTemplate.query(sQuery, new TestParameter());
		sQuery = "select count(ntestfilecode) as ntestfilecode from testfile where ntransactionstatus= "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")";
		List<TestFile> lstTestFile = jdbcTemplate.query(sQuery, new TestFile());

		sQuery = "select count(ntestformulacode) as ntestformulacode from testformula where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in (" + stestcode + ")";
		List<TestFormula> lstTestFormula = jdbcTemplate.query(sQuery, new TestFormula());

		List<TestParameter> lstTP = lstTestParameter;
		List<TestFile> lstTF = lstTestFile;
		List<TestFormula> lstTForm = lstTestFormula;
		sQuery = "select count(tpp.ntestpredefinedcode) "
				+ "from testpredefinedparameter tpp,testparameter tp where tp.ntestparametercode = tpp.ntestparametercode "
				+ "and tpp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tp.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " and tp.ntestcode in(" + stestcode + ");";
		int npredefparamcount = jdbcTemplate.queryForObject(sQuery, Integer.class,jdbcTemplate);
		int nparametercount = (lstTP != null && lstTP.size() > 0) ? lstTP.size() : 0;
		int ntestfilecount = (lstTF != null && lstTF.size() > 0) ? lstTF.get(0).getNtestfilecode() : 0;
		int ntestFormcount = (lstTForm != null && lstTForm.size() > 0) ? lstTForm.get(0).getNtestformulacode() : 0;
		int ncharparamcount = (int) lstTP.stream().filter(
				objTP -> objTP.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype())
				.count();
		int nnumericcount = (int) lstTP.stream()
				.filter(objTP -> objTP.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype())
				.count();
		returnMap.put("ntestcount", ntestcount);
		returnMap.put("nparametercount", nparametercount);
		returnMap.put("npredefparamcount", npredefparamcount);
		returnMap.put("ncharparamcount", ncharparamcount);
		returnMap.put("nnumericcount", nnumericcount);
		returnMap.put("ntestfilecount", ntestfilecount);
		returnMap.put("ntestFormcount", ntestFormcount);
		return returnMap;
	}

	@Override
	public Map<String, Object> fnTestGroupTestUpdateSequence(Map<String, Object> objMap) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		int ntestcount = (int) objMap.get("ntestcount");
		int nparametercount = (int) objMap.get("nparametercount");
		int npredefparamcount = (int) objMap.get("npredefparamcount");
		int ncharparamcount = (int) objMap.get("ncharparamcount");
		int nnumericcount = (int) objMap.get("nnumericcount");
		int ntestfilecount = (int) objMap.get("ntestfilecount");
		int ntestFormcount = (int) objMap.get("ntestFormcount");
		String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename in (N'testgrouptest',N'testgrouptestcharparameter',N'testgrouptestfile',"
				+ "N'testgrouptestformula',N'testgrouptestnumericparameter',N'testgrouptestparameter',N'testgrouptestpredefparameter')";
		List<Integer> lstCount = jdbcTemplate.queryForList(sQuery, Integer.class);
		int ntgtcount = lstCount.get(0);
		int ntgtcpcount = lstCount.get(1);
		int ntgttfcount = lstCount.get(2);
		int ntgttformcount = lstCount.get(3);
		int ntgtnpcount = lstCount.get(4);
		int ntgttpcount = lstCount.get(5);
		int ntgtppcount = lstCount.get(6);
		String seqUpdateQry = "update seqnotestgroupmanagement set nsequenceno=" + (ntgtcount + ntestcount)
				+ "  where stablename=N'testgrouptest';" + "update seqnotestgroupmanagement set nsequenceno="
				+ (ntgtcpcount + ncharparamcount) + "  where stablename=N'testgrouptestcharparameter';"
				+ "update seqnotestgroupmanagement set nsequenceno=" + (ntgttfcount + ntestfilecount)
				+ "  where stablename=N'testgrouptestfile';" + "update seqnotestgroupmanagement set nsequenceno="
				+ (ntgttformcount + ntestFormcount) + "  where stablename=N'testgrouptestformula';"
				+ "update seqnotestgroupmanagement set nsequenceno=" + (ntgtnpcount + nnumericcount)
				+ "  where stablename=N'testgrouptestnumericparameter';"
				+ "update seqnotestgroupmanagement set nsequenceno=" + (ntgttpcount + nparametercount)
				+ "  where stablename=N'testgrouptestparameter';" + "update seqnotestgroupmanagement set nsequenceno="
				+ (ntgtppcount + npredefparamcount) + "  where stablename=N'testgrouptestpredefparameter';";
		jdbcTemplate.execute(seqUpdateQry);
		returnMap.put("ntgtcount", ntgtcount);
		returnMap.put("ntgtcpcount", ntgtcpcount);
		returnMap.put("ntgttfcount", ntgttfcount);
		returnMap.put("ntgttformcount", ntgttformcount);
		returnMap.put("ntgtnpcount", ntgtnpcount);
		returnMap.put("ntgttpcount", ntgttpcount);
		returnMap.put("ntgtppcount", ntgtppcount);
		return returnMap;
	}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestGroupSpecification(final UserInfo objUserInfo,final TreeTemplateManipulation objTree, final boolean isTree,
			final TestGroupSpecification objTestGroupSpec) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		returnMap.putAll((Map<String, Object>) testGroupCommonFunction.getTestGroupSpecification(objUserInfo,objTree,isTree,objTestGroupSpec).getBody());
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public List<ApprovalRoleActionDetail> getApprovalConfigAction(final TestGroupSpecification objGroupSpecification,final UserInfo userInfo) throws Exception {
		final String sActionQry = "select acad.ntransactionstatus,acad.nlevelno,coalesce(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"+ "	 ts.jsondata->'sactiondisplaystatus'->>'en-US') as  sactiondisplaystatus, acr.nesignneed"
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and acad.nsitecode = acr.nsitecode and acr.nsitecode = ac.nsitecode "
				+ " and acr.nsitecode = "+userInfo.getNmastersitecode()+";";
		return (List<ApprovalRoleActionDetail>) jdbcTemplate.query(sActionQry, new ApprovalRoleActionDetail());
	}



	@Override
	public ResponseEntity<Object> getAddSpecification(UserInfo objUserInfo, int ntreeversiontempcode,
			int nprojectmastercode)// , final String currentUIDate)
					throws Exception {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {

			if (nprojectmastercode != -1) {
				map = (Map<String, Object>) testGroupCommonFunction
						.getProjectMasterStatusCode(nprojectmastercode, objUserInfo);
				strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {

				Map<String, Object> returnobj = new HashMap<>();
				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(objUserInfo.getSdatetimeformat());

				final Map<String, Object> outputMap = projectDAOSupport.getDateFromControlProperties(objUserInfo, "", // currentUIDate,
						"", "ExpiryDate");
				final String ExpiryDateUI = LocalDateTime.parse((String) outputMap.get("ExpiryDate"), dbPattern)
						.format(uiPattern);
				returnobj.put("ExpiryDate", ExpiryDateUI);

				return new ResponseEntity<>(returnobj, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveSpecificationById(UserInfo userInfo,TestGroupSpecification objTestGroupSpec, int ntreeversiontempcode) throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		// For_MSSQL
		// String queryformat="FORMAT(dexpirydate,'" + userInfo.getSsitedatetime()+ "')
		// ";

		// For_PostgreSQL
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			String queryformat = "TO_CHAR(dexpirydate,'" + userInfo.getSpgsitedatetime() + "') ";

			final String sSpecQry = "select tgs.*, " + queryformat + " as sexpirydate, tz.stimezoneid"
					+ " from testgroupspecification tgs, timezone tz where tz.ntimezonecode = tgs.ntzexpirydate and tz.nstatus = tgs.nstatus"
					+ " and tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode() + " ;";
			final TestGroupSpecification objTestGroupSpecification = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(sSpecQry, TestGroupSpecification.class,jdbcTemplate);

			if (objTestGroupSpecification != null) {
				if (objTestGroupSpecification.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(objTestGroupSpecification.getNprojectmastercode(), userInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
				else {

					if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()
							|| objTestGroupSpecification
							.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
						final Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.put("SelectedSpecification", objTestGroupSpecification);
						return new ResponseEntity<>(outputMap, HttpStatus.OK);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,final TreeTemplateManipulation objTree) throws Exception {
		
		if (objTestGroupSpec != null) {

			final String sQuery = " lock  table locktestgroupspec "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			objTestGroupSpec.setSexpirydate(dateUtilityFunction.instantDateToString(objTestGroupSpec.getDexpirydate()).replace("T", " "));
			boolean validInsert = false;
			if (objUserInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				if (objTestGroupSpec.getDexpirydate().isAfter(dateUtilityFunction.getCurrentDateTime(objUserInfo))) {
					validInsert = true;
				}

			} else {
				if (LocalDateTime
						.parse(objTestGroupSpec.getSexpirydate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
						.isAfter(LocalDateTime.now())) {
					validInsert = true;
				}
			}

			if (validInsert) {
				objTestGroupSpec = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objTestGroupSpec, Arrays.asList("sexpirydate"),
						Arrays.asList("ntzexpirydate"), true, objUserInfo),new TypeReference<TestGroupSpecification>() {});

				objTestGroupSpec.setSexpirydate(objTestGroupSpec.getSexpirydate().replace("T", " "));

				String sequnencenoquery = "select nsequenceno from SeqNoTestGroupmanagement where stablename ='testgroupspecification' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				int nsequenceno = (int)jdbcUtilityFunction.queryForObject(sequnencenoquery, Integer.class,jdbcTemplate);
				nsequenceno++;

				String insertquery = "Insert into TestGroupSpecification(nallottedspeccode,ntemplatemanipulationcode,napproveconfversioncode,ncomponentrequired, "
						+ "nprojectmastercode,ntransactionstatus,sspecname,sversion,dexpirydate,noffsetdexpirydate,ntzexpirydate,dmodifieddate,napprovalstatus,nstatus,nsitecode)  "
						+ "values(" + nsequenceno + "," + objTestGroupSpec.getNtemplatemanipulationcode() + ","
						+ objTestGroupSpec.getNapproveconfversioncode() + "," + objTestGroupSpec.getNcomponentrequired()
						+ "," + "" + objTestGroupSpec.getNprojectmastercode() + ","
						+ objTestGroupSpec.getNtransactionstatus() + ",N'"
						+ stringUtilityFunction.replaceQuote(objTestGroupSpec.getSspecname()) + "',N'" + objTestGroupSpec.getSversion()
						+ "','" + objTestGroupSpec.getSexpirydate() + "'," + objTestGroupSpec.getNoffsetdexpirydate()
						+ "," + "" + objTestGroupSpec.getNtzexpirydate() + ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
						+ "" + objTestGroupSpec.getNapprovalstatus() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ objUserInfo.getNmastersitecode() + "); ";

				jdbcTemplate.execute(insertquery);

				String updatequery = "update SeqNoTestGroupmanagement set nsequenceno =" + nsequenceno+ " where stablename ='testgroupspecification'and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(updatequery);
				objTestGroupSpec.setNallottedspeccode(nsequenceno);

				List<Object> lstNewObject = new ArrayList<Object>();
				lstNewObject.add(objTestGroupSpec);

				if (objTestGroupSpec.getNcomponentrequired() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {

					String sequnencenoquery1 = "select nsequenceno from SeqNoTestGroupmanagement where stablename ='testgroupspecsampletype' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					int nsequenceno1 = (int) jdbcUtilityFunction.queryForObject(sequnencenoquery1, Integer.class,jdbcTemplate);
					nsequenceno1++;

					String insertquery1 = "Insert into testgroupspecsampletype(nspecsampletypecode,nallottedspeccode,ncomponentcode,dmodifieddate,nstatus,nsitecode)  "
							+ "values(" + nsequenceno1 + "," + objTestGroupSpec.getNallottedspeccode() + ","
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ " "	+ objUserInfo.getNmastersitecode() + ") ";
					jdbcTemplate.execute(insertquery1);

					String updatequery1 = "update SeqNoTestGroupmanagement set nsequenceno =" + nsequenceno1+ " where stablename ='testgroupspecsampletype' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					jdbcTemplate.execute(updatequery1);
				}

				auditUtilityFunction.fnInsertAuditAction(lstNewObject, 1, null, Arrays.asList("IDS_ADDSPECIFICATION"), objUserInfo);

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_EXPIRYDATEMUSTMORETHANCURRENTDATE",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo, objTree, false, null).getBody(),HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Object> updateSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,TreeTemplateManipulation objTree) throws Exception {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);

		if (objTestGroupSpecification != null) {
			if (objTree.getNprojectmastercode() != -1) {
				map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
				strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			}

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			} else {
				if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {

					final ObjectMapper objMapper = new ObjectMapper();
					objMapper.registerModule(new JavaTimeModule());
					objTestGroupSpec.setSexpirydate(dateUtilityFunction.instantDateToString(objTestGroupSpec.getDexpirydate()).replace("T", " ").replace("Z", ""));

					boolean validInsert = false;
					if (objUserInfo.getIsutcenabled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						if (objTestGroupSpec.getDexpirydate().isAfter(dateUtilityFunction.getCurrentDateTime(objUserInfo))) {
							validInsert = true;
						}
					} else {
						if (LocalDateTime.parse(objTestGroupSpec.getSexpirydate().replace(" ", "T"))
								.isAfter(LocalDateTime.now())) {
							validInsert = true;
						}
					}

					if (validInsert) {

						objTestGroupSpec = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objTestGroupSpec, Arrays.asList("sexpirydate"), Arrays.asList("ntzexpirydate"), true,
								objUserInfo), new TypeReference<TestGroupSpecification>() {});
						TestGroupSpecSampleType objTGSST = null;
						objTestGroupSpec.setSexpirydate(objTestGroupSpec.getSexpirydate().replace("T", " "));
						objTestGroupSpec.setNtemplatemanipulationcode(objTestGroupSpecification.getNtemplatemanipulationcode());

						String queryExecute = "";

						queryExecute += "update testgroupspecification set sspecname = N'"
								+ stringUtilityFunction.replaceQuote(objTestGroupSpec.getSspecname()) + "'," + " ntransactionstatus = "
								+ objTestGroupSpec.getNtransactionstatus() + ", nprojectmastercode = "
								+ objTestGroupSpec.getNprojectmastercode() + ", dexpirydate = '"
								+ objTestGroupSpec.getDexpirydate() + "',noffsetdexpirydate="
								+ objTestGroupSpec.getNoffsetdexpirydate() + ", ncomponentrequired = "
								+ objTestGroupSpec.getNcomponentrequired() + ", " + " ntzexpirydate="
								+ objTestGroupSpec.getNtzexpirydate() + " " + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where nallottedspeccode = "
								+ objTestGroupSpec.getNallottedspeccode() + ";";
						
						queryExecute += queryExecute;
						
						
						if (objTestGroupSpecification.getNcomponentrequired() == Enumeration.TransactionStatus.NO.gettransactionstatus()
								&& objTestGroupSpec.getNcomponentrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							queryExecute = queryExecute+"delete from testgroupspecsampletype where nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode() + ";";
							

						} else if (objTestGroupSpecification.getNcomponentrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()
								&& objTestGroupSpec.getNcomponentrequired() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
							if (objTestGroupSpec.getNcomponentrequired() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
								objTGSST = new TestGroupSpecSampleType();
								objTGSST.setNcomponentcode(Enumeration.TransactionStatus.NA.gettransactionstatus());
								objTGSST.setNallottedspeccode(objTestGroupSpec.getNallottedspeccode());
								objTGSST.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
							}
						}
						
						jdbcTemplate.execute(queryExecute);

						if (objTGSST != null) {
							String sequencequery = "select nsequenceno from SeqNoTestGroupmanagement where stablename ='testgroupspecsampletype' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
							nsequenceno++;
							String insertquery = "Insert into testgroupspecsampletype (nspecsampletypecode,nallottedspeccode,ncomponentcode,dmodifieddate,nstatus,nsitecode)"
									+ "values(" + nsequenceno + "," + objTGSST.getNallottedspeccode() + ","
									+ objTGSST.getNcomponentcode() + ",'" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ objTGSST.getNstatus() + "," + objUserInfo.getNmastersitecode() + ")";
							jdbcTemplate.execute(insertquery);

							String updatequery = "update SeqNoTestGroupmanagement set nsequenceno =" + nsequenceno+ " where stablename='testgroupspecsampletype' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							jdbcTemplate.execute(updatequery);
						}
						List<Object> lstnewobject = new ArrayList<Object>();
						List<Object> lstoldobject = new ArrayList<Object>();
						lstnewobject.add(objTestGroupSpec);

						objTestGroupSpecification.setSexpirydate(objTestGroupSpecification.getSexpirydate().replace("T", " "));

						lstoldobject.add(objTestGroupSpecification);
						auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lstoldobject, Arrays.asList("IDS_EDITSPECIFICATION"),objUserInfo);
						return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo, objTree, true, objTestGroupSpec).getBody(),HttpStatus.OK);

					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_EXPIRYDATEMUSTMORETHANCURRENTDATE",objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}

			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ResponseEntity<Object> deleteSpecification(Map<String, Object> inputMap, UserInfo objUserInfo,TestGroupSpecification objTestGroupSpec, TreeTemplateManipulation objTree, int ntreeversiontempcode)
			throws Exception {
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			if (objTestGroupSpecification != null) {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (objTree.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);

				}

				else {

					if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {

						String sQuery = "select STRING_AGG(cast(tgt.ntestgrouptestcode as varchar(10)),',')  from testgroupspecsampletype tgsst, testgrouptest tgt"
								+ " where tgsst.nspecsampletypecode = tgt.nspecsampletypecode and tgsst.nstatus = tgt.nstatus"
								+ " and tgsst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgsst.nsitecode = tgt.nsitecode and tgt.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgsst.nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode();
						List<String> lstTestGroupTestCode = jdbcTemplate.queryForList(sQuery, String.class);

						final String sTestGroupTestCode = String.join(",", lstTestGroupTestCode);

						sQuery = "select * from testgroupspecification where  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ " and nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode() +";";

						List<TestGroupSpecification> lstTestGroupSpec = jdbcTemplate.query(sQuery,new TestGroupSpecification());

						List<Object> mapLists = new ArrayList<Object>();
						mapLists.add(lstTestGroupSpec);

						String sDeleteQuery = "update testgroupspecification set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " "
								+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode() + ";";

						sDeleteQuery = sDeleteQuery + "update testgroupspecsampletype set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode() + ";";

						sDeleteQuery = sDeleteQuery + "update testgroupspecfile set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + "  where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
								+ " and nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode() + ";";

						List<String> lstActionType = new ArrayList<String>();

						List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) mapLists.get(0);
						if (!lstTestGroupSpecification.isEmpty()) {
							if (lstTestGroupSpecification.get(0).getNcomponentrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

								Map obj = (Map) ((Map) ((Map) ((Map) inputMap.get("genericlabel"))
										.get("DeleteComponent")).get("jsondata")).get("sdisplayname");
								String deletecomponentlabel = (String) obj.get(objUserInfo.getSlanguagetypecode());

								lstActionType.addAll(Arrays.asList("IDS_DELETESPEC", deletecomponentlabel, "IDS_DELETESPECFILE"));
							} else {
								lstActionType.addAll(Arrays.asList("IDS_DELETESPEC", "IDS_DELETESPECFILE"));
							}
						}

						if (!sTestGroupTestCode.equals("null") && !sTestGroupTestCode.isEmpty()) {
							sDeleteQuery = sDeleteQuery + testGroupCommonFunction.getComponentChildDetailsUpdateQuery(sTestGroupTestCode, objUserInfo);
							lstActionType.addAll(Arrays.asList("IDS_DELETETEST", "IDS_DELETETESTPARAMETER","IDS_DELETEPREDEFINEDPARAMETER", "IDS_DELETETESTFORMULA", 
									"IDS_DELETETESTFILE","IDS_DELETETESTMATERIAL"));
						}

						jdbcTemplate.execute(sDeleteQuery);

						auditUtilityFunction.fnInsertListAuditAction(mapLists, 1, null, lstActionType, objUserInfo);
						return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo, objTree, false, null).getBody(), HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	// (isQualisLite) added by Gowtham - ALPD-5329 - In test group specification record getting Auto Approved when configuration for test group approval not done
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> completeSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,
			TreeTemplateManipulation objTree, int ntreeversiontempcode, int isQualisLite,final List<TestGroupTest> listTestGroupTest) throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final String Query = " lock  table locktestgrpspchistory " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);

		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objTree.getNprojectmastercode() != -1) {
			map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
			strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		}

		List<TestGroupSpecSampleType> lst = null;
		String sQuery1 = "select tgsst.nspecsampletypecode,c.scomponentname,'Component Name' as sparametersynonym,'1' as salert from testgroupspecification tgs "
				+ " left join testgroupspecsampletype tgsst on tgs.nallottedspeccode = tgsst.nallottedspeccode"
				+ " and tgsst.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgsst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgs.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join testgrouptest tgt on tgsst.nspecsampletypecode = tgt.nspecsampletypecode"
				+ " and tgt.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join component c on c.ncomponentcode = tgsst.ncomponentcode and c.nsitecode = "+objUserInfo.getNmastersitecode()+" and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where tgs.nallottedspeccode = "
				+ objTestGroupSpec.getNallottedspeccode() + " and tgt.ntestgrouptestcode is null";

		lst = (List<TestGroupSpecSampleType>) jdbcTemplate.query(sQuery1, new TestGroupSpecSampleType());

		if (objRetiredTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus() && lst.size() != 0) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);

			}

			else {

				if (objTestGroupSpecification != null) {
					if (objTestGroupSpecification.getNtransactionstatus() == Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()) {
						if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
								|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {

							// For_MSSQL
							// String queryformat="getutcdate()";

							// For_POSTGRESQL
							String queryformat = "now()";

							String query1 = "select  dexpirydate, " + queryformat + ", DATE_PART('day',  '"+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
							+ "'-dexpirydate) as ndate from TestGroupSpecification where nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode();

							TestGroupSpecification object = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(query1,TestGroupSpecification.class,jdbcTemplate);
							if (object != null && object.getNdate() > 0) {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTINVALIDATEDATE",objUserInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							} else {

								String query = "select tgsst.nspecsampletypecode,c.scomponentname,'Component Name' as sparametersynonym,'1' as salert from testgroupspecification tgs "
										+ " left join testgroupspecsampletype tgsst on tgs.nallottedspeccode = tgsst.nallottedspeccode"
										+ " and tgsst.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgsst.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tgs.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " left join testgrouptest tgt on tgsst.nspecsampletypecode = tgt.nspecsampletypecode"
										+ " and tgt.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " left join component c on c.ncomponentcode = tgsst.ncomponentcode "
										+ " and c.nsitecode = "+objUserInfo.getNmastersitecode()+" and c.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " where tgs.nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode()
										+ " and tgt.ntestgrouptestcode is null";

								lst = (List<TestGroupSpecSampleType>) jdbcTemplate.query(query,	new TestGroupSpecSampleType());

								if (lst.size() == 0) {

									query = "select tgp.sparametersynonym as scomponentname,'Parameter Name' as sparametersynonym,'2' as salert from testgroupspecification tgs "
											+ " left  join testgroupspecsampletype tgsst on tgs.nallottedspeccode = tgsst.nallottedspeccode "
											+ " and tgsst.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgsst.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgs.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " left  join testgrouptest tgt on tgsst.nspecsampletypecode = tgt.nspecsampletypecode"
											+ " and tgt.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " left join testgrouptestparameter tgp on tgp.ntestgrouptestcode=tgt.ntestgrouptestcode and tgp.nparametertypecode=2 and tgp.nsitecode = "+objUserInfo.getNmastersitecode()+" "
											+ " left join testgrouptestpredefparameter tgpdp on  tgpdp.ntestgrouptestparametercode=tgp.ntestgrouptestparametercode "
											+ " and tgpdp.nsitecode = "+objUserInfo.getNmastersitecode()+" and tgpdp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " where tgs.nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode()
											+ " and tgpdp.ntestgrouptestparametercode is null  and tgp.sparametersynonym is not null";
									lst = (List<TestGroupSpecSampleType>) jdbcTemplate.query(query,new TestGroupSpecSampleType());
									if (lst.size() == 0) {
										final String approvalVerQry = "select acaa.* from approvalconfigversion acv,approvalconfig ac,approvalconfigautoapproval acaa "
												+ " where "
												+ " acv.napprovalconfigcode=ac.napprovalconfigcode and acv.napproveconfversioncode = acaa.napprovalconfigversioncode "
												+ " and acaa.nsitecode = acv.nsitecode "
												+ " and acv.nsitecode = "+objUserInfo.getNmastersitecode()+" and ntransactionstatus= "+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
												+ " and acaa.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and ac.nregsubtypecode=-1 "
												+ " and ac.nregtypecode=-1 and ac.napprovalsubtypecode=1"
												+ " and ac.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and acv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

										final List<ApprovalConfigAutoapproval> lstApprovalConfigAutoapproval = jdbcTemplate.query(approvalVerQry, new ApprovalConfigAutoapproval());


										final String sSpecQry = "select * from testgroupspecification where napprovalstatus!="
												+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
												+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
												+ " and nsitecode = "+objUserInfo.getNmastersitecode()+" "
												+ " and nallottedspeccode= "+objTestGroupSpec.getNallottedspeccode()+";";
										final List<TestGroupSpecification> lstTestGroupSpecification = jdbcTemplate.query(sSpecQry, new TestGroupSpecification());

										Map<String, Object> mapObj = new HashMap<String, Object>();
										mapObj.put("ApprovalConfigAutoapproval", lstApprovalConfigAutoapproval);
										mapObj.put("TestGroupSpecification", lstTestGroupSpecification);
										List<ApprovalConfigAutoapproval> objApprovalConfigApproval = (List<ApprovalConfigAutoapproval>) mapObj
												.get(ApprovalConfigAutoapproval.class.getSimpleName());
										if (objApprovalConfigApproval != null && objApprovalConfigApproval.size() > 0 
												// ALPD-5329 - Gowtham R - In test group specification record getting Auto Approved when configuration for test group approval not done
												// commented by Gowtham R on 15 July 2025 - ALPDJ21-22 - Test Group auto approve specification without test group configuration
												// && (isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus() 
												&& objApprovalConfigApproval.get(0).getNautoapprovalcode() > 0 ) {

											final List<TestGroupTest> inactiveTestList1 = (List<TestGroupTest>) validateTestGroupComplete(
													objTestGroupSpec.getNallottedspeccode(), objUserInfo).getBody();
											//List<String> lstString = new ArrayList<String>();
											StringBuilder strBuilder = new StringBuilder();
											
											if(listTestGroupTest.size()!=inactiveTestList1.size()) {


												Object objversion = projectDAOSupport.fnGetVersion(objUserInfo.getNformcode(),
														objTree.getNtemplatemanipulationcode(), null,
														SeqNoTestGroupVersion.class, objUserInfo.getNmastersitecode(),
														objUserInfo);
												String specVersion = "";
												if (objversion != null) {
													specVersion = BeanUtils.getProperty(objversion, "versiondes");
												}

												if (objApprovalConfigApproval.get(0).getNneedautoapproval() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
													final String sRoleConfigQry = "select * from approvalconfigrole where napproveconfversioncode = "
															+ objApprovalConfigApproval.get(0).getNapprovalconfigversioncode()
															+ " and nsitecode = "+objUserInfo.getNmastersitecode()+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and nlevelno = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
													ApprovalConfigRole objACR = (ApprovalConfigRole) jdbcUtilityFunction
															.queryForObject(sRoleConfigQry, ApprovalConfigRole.class,jdbcTemplate);
													objTestGroupSpec
													.setNapprovalstatus((short) objACR.getNapprovalstatuscode());
													objTestGroupSpecification
													.setNapprovalstatus((short) objACR.getNapprovalstatuscode());

													final String sActionConfigQry = "select * from approvalroleactiondetail where ntransactionstatus="
															+ objTestGroupSpec.getNapprovalstatus() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and napprovalconfigrolecode in (select napprovalconfigrolecode  from approvalconfigrole"
															+ " where napproveconfversioncode="+ objApprovalConfigApproval.get(0).getNapprovalconfigversioncode()
															+ " and nsitecode = "+objUserInfo.getNmastersitecode()+" and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " LIMIT 1)";

													List<ApprovalRoleActionDetail> listActions = (List<ApprovalRoleActionDetail>) jdbcTemplate.query(sActionConfigQry, new ApprovalRoleActionDetail());
													if (listActions != null && listActions.size() > 0) {
														if (objTestGroupSpec
																.getNapprovalstatus() != Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
															final String sUpdateQry = "update testgroupspecification set napprovalstatus="
																	+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
																	+ "," + "ntransactionstatus="+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
																	+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo)
																	+ "'" + " where napprovalstatus not in ("+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
																	+ ","+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+ ","
																	+ +Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+ ","
																	+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()+ ","
																	+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus()+ ") and nstatus="
																	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and ntemplatemanipulationcode="
																	+ objTree.getNtemplatemanipulationcode()+ " and nsitecode = "+objUserInfo.getNmastersitecode()+" "
																	+ " and nallottedspeccode!="+ objTestGroupSpec.getNallottedspeccode() + ";";
															//lstString.add(sUpdateQry);
															strBuilder.append(sUpdateQry);														}
													}
													String sQuery = "select tgs.*,ts.jsondata->'stransdisplaystatus'->>'"
															+ objUserInfo.getSlanguagetypecode()+ "' as stransactionstatus from testgroupspecification tgs,transactionstatus ts"
															+ " where tgs.napprovalstatus=ts.ntranscode and tgs.napprovalstatus="+ objTestGroupSpec.getNapprovalstatus()+ "  "
															+ " and tgs.nsitecode = "+objUserInfo.getNmastersitecode()+" "
															+ " and tgs.nallottedspeccode="+ objTestGroupSpec.getNallottedspeccode()+";";

													TestGroupSpecification checkSpecStatus = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(
															sQuery, TestGroupSpecification.class,jdbcTemplate);

													if (checkSpecStatus == null) {
														final String sUpdateQry = "update testgroupspecification set napprovalstatus="+ objTestGroupSpec.getNapprovalstatus() + ","
																+ "napproveconfversioncode="+ objApprovalConfigApproval.get(0).getNapprovalconfigversioncode()
																+ "," + "sversion=N'" + specVersion + "' "+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
																+ " where nallottedspeccode="+ objTestGroupSpec.getNallottedspeccode() + " and nstatus="
																+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";
														//lstString.add(sUpdateQry);
														strBuilder.append(sUpdateQry);	

														sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
														int nseqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate)+
																Enumeration.TransactionStatus.SEQUENCEONE.gettransactionstatus();
														
														sQuery = "update seqnotestgroupmanagement set nsequenceno = "+ (nseqNo + 1)+ " where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
														//lstString.add(sQuery);
														strBuilder.append(sQuery);	
														
														final String sInsertQry = "insert into testgroupspecificationhistory (nspecificationhistorycode, nallottedspeccode,"
																+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate,noffsetdtransactiondate, scomments,ntztransactiondate,dmodifieddate,nstatus,nsitecode)"
																+ " values " + "(" + nseqNo++ + ", "+ objTestGroupSpec.getNallottedspeccode() + ", "
																+ (short) Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
																+ ", " + objUserInfo.getNusercode() + ","+ objUserInfo.getNuserrole() + ", '"
																+dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"	+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
																+ "," + " N'" +stringUtilityFunction.replaceQuote(objUserInfo.getSreason())
																+ "', " + objUserInfo.getNtimezonecode() + ", " + "'"+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
																+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "," + objUserInfo.getNmastersitecode() + " )," + "("
																+ nseqNo + ", " + objTestGroupSpec.getNallottedspeccode()+ ", " + objTestGroupSpec.getNapprovalstatus() + ", "
																+ "-1,"+objACR.getNuserrolecode()+", '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
																+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
																+ "," + " N'" + stringUtilityFunction.replaceQuote(objUserInfo.getSreason())
																+ "', " + objUserInfo.getNtimezonecode() + ", " + "'"+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
																+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "," + objUserInfo.getNmastersitecode() + ");";
														//lstString.add(sInsertQry);
														strBuilder.append(sInsertQry);	

														final String sStatusQuery = "select ts.jsondata->'sactiondisplaystatus'->>'"
																+ objUserInfo.getSlanguagetypecode()+ "' sactiondisplaystatus from transactionstatus ts"
																+ " where ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
																+ " and ts.ntranscode = "+ objTestGroupSpecification.getNapprovalstatus()+";" ;
														
														TransactionStatus objTransactionStatus = (TransactionStatus) jdbcUtilityFunction.queryForObject(sStatusQuery, TransactionStatus.class,jdbcTemplate);

														// For complete action
														final List<String> lstactionType = new ArrayList<String>();
														final List<Object> lstnewobject = new ArrayList<Object>();


														// For final level approve action
														final String actionStatus = commonFunction
																.getMultilingualMessage("IDS_AUTO",objUserInfo.getSlanguagefilename()).toUpperCase()+ " "
																+ objTransactionStatus.getSactiondisplaystatus().toUpperCase()+ " "+ commonFunction.getMultilingualMessage("IDS_SPECIFICATION",
																		objUserInfo.getSlanguagefilename()).toUpperCase();

														lstactionType.add(actionStatus);
														lstnewobject.add(Arrays.asList(objTestGroupSpecification));

														auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstactionType,objUserInfo);

													} else {
														final String returnMsg = commonFunction.getMultilingualMessage(
																"IDS_SPECALREADY", objUserInfo.getSlanguagefilename())+ " "+ commonFunction.getMultilingualMessage(
																		checkSpecStatus.getStransdisplaystatus(),objUserInfo.getSlanguagefilename());
														
														return new ResponseEntity<>(returnMsg,HttpStatus.EXPECTATION_FAILED);
													}
												} else {
													objTestGroupSpec.setNapprovalstatus(
															(short) Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());
													final String sSpecUpdateQry = "update testgroupspecification set napprovalstatus="+ objTestGroupSpec.getNapprovalstatus() + ",sversion=N'"
															+ specVersion + "',napproveconfversioncode="+ objApprovalConfigApproval.get(0).getNapprovalconfigversioncode()+ ","
															+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
															+ " where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
															+ " and nallottedspeccode= "+objTestGroupSpec.getNallottedspeccode() +";";
													//lstString.add(sSpecUpdateQry);
													strBuilder.append(sSpecUpdateQry);
													

													String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
													int nseqNo =(int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate)+ 
															Enumeration.TransactionStatus.SEQUENCEONE.gettransactionstatus();
													
													final String sSeqUpdateQry = "update seqnotestgroupmanagement set nsequenceno = "+ nseqNo+" where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
													//lstString.add(sSeqUpdateQry);
													strBuilder.append(sSeqUpdateQry);
													
													final String sInsertQry = "insert into testgroupspecificationhistory (nspecificationhistorycode, nallottedspeccode,"
															+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate,noffsetdtransactiondate, scomments,ntztransactiondate,dmodifieddate,nstatus,nsitecode)"
															+ " values (" + nseqNo + ", "+ objTestGroupSpec.getNallottedspeccode() + ", "
															+ objTestGroupSpec.getNapprovalstatus() + ", "+ objUserInfo.getNusercode() + "," + objUserInfo.getNuserrole()
															+ ", '" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"	+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ","
															+ " N'" + stringUtilityFunction.replaceQuote(objUserInfo.getSreason()) + "', "
															+ objUserInfo.getNtimezonecode() + ", " + "'"+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
															+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "," + objUserInfo.getNmastersitecode() + ");";
													//lstString.add(sInsertQry);
													strBuilder.append(sInsertQry);
													
													final List<String> lstactionType = new ArrayList<String>();
													lstactionType.add("IDS_COMPLETESPECIFICATION");

													final List<Object> lstnewobject = new ArrayList<Object>();
													lstnewobject.add(objTestGroupSpecification);
													auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
												}
											}

											if (!inactiveTestList1.isEmpty()) {
												final String testCodeString = stringUtilityFunction.fnDynamicListToString(inactiveTestList1, "getNtestgrouptestcode");
												final String removeTestString = "update testgrouptest set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
														+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
														+ " where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
														+ " and  ntestgrouptestcode in (" + testCodeString + ");";

												//lstString.add(removeTestString);
												strBuilder.append(removeTestString);
												jdbcTemplate.execute(strBuilder.toString());	
												
												//String[] strFinal = lstString.toArray(new String[0]);
												//executeBulkDatainSingleInsert(strFinal);


											} else {
												jdbcTemplate.execute(strBuilder.toString());
//												String[] strFinal = lstString.toArray(new String[0]);
//												executeBulkDatainSingleInsert(strFinal);
											}

											if (!inactiveTestList1.isEmpty()) {

												auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(inactiveTestList1), 1, null,Arrays.asList("IDS_DELETETEST"), objUserInfo);

											}

											return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo,objTree, true, objTestGroupSpec).getBody(), HttpStatus.OK);
										} else {
											return new ResponseEntity<>(
													commonFunction.getMultilingualMessage("IDS_PLEASEDOAPPROVALCONFIGURATIONFORTHISTYPE",
															objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
										}
									} else {
										return new ResponseEntity<>(
												commonFunction.getMultilingualMessage("IDS_PREDEFINEDPARAMETERNOTFOUND",objUserInfo.getSlanguagefilename()),
												HttpStatus.EXPECTATION_FAILED);
									}
								} else {
									return new ResponseEntity<>(
											commonFunction.getMultilingualMessage("IDS_TESTNOTFOUNT",objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
								}
							}
						} else if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYCOMPLETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION", objUserInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ACTIVESPECONLYCOMPLETED",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",objUserInfo.getSlanguagefilename()), 
							HttpStatus.EXPECTATION_FAILED);
				}

			}

		}

	}

	@Override
	public ResponseEntity<Object> approveValidateSpecification(final UserInfo objUserInfo,
			final TestGroupSpecification objTestGroupSpec) throws Exception {
		final String sSpecQuery = "select tgs.nallottedspeccode, tgs.napprovalstatus, tgs.napproveconfversioncode,ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode() + "' as stransdisplaystatus "
				+ " from testgroupspecification tgs, transactionstatus ts where ts.ntranscode = tgs.napprovalstatus and ts.nstatus = tgs.nstatus"
				+ " and tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgs.nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode();
		final TestGroupSpecification objTestGroupSpecification = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(sSpecQuery,
				TestGroupSpecification.class,jdbcTemplate);

		if (objTestGroupSpecification != null) {
			final String sActionQry = "select acvd.ntransactionstatus,acvd.napprovalvalidationcode,ts.jsondata->'stransdisplaystatus'->>'"
					+ objUserInfo.getSlanguagetypecode() + "'  as svalidationstatus"
					+ " from approvalrolevalidationdetail acvd,approvalconfigrole acr,approvalconfig ac, transactionstatus ts"
					+ " where acvd.nstatus = ac.nstatus  and acvd.nuserrolecode = " + objUserInfo.getNuserrole()
					+ " and acvd.napprovalconfigrolecode = acr.napprovalconfigrolecode"
					+ " and acr.napprovalconfigcode = ac.napprovalconfigcode and acr.napproveconfversioncode = "+ objTestGroupSpec.getNapproveconfversioncode()
					+ " and ac.nregtypecode = -1 and ac.nregsubtypecode = -1 and ac.napprovalsubtypecode = "+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + " "
					+ " and acvd.nsitecode = acr.nsitecode and acr.nsitecode = ac.nsitecode and ac.nsitecode  = "+objUserInfo.getNmastersitecode()+" "
					+ " and acr.nstatus = ac.nstatus and ts.ntranscode = acvd.ntransactionstatus and ts.nstatus = ac.nstatus and ac.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
			List<ApprovalRoleValidationDetail> listValidationActions = (List<ApprovalRoleValidationDetail>) jdbcTemplate
					.query(sActionQry, new ApprovalRoleValidationDetail());
			if (!listValidationActions.isEmpty()) {
				List<ApprovalRoleValidationDetail> listACVD = listValidationActions.stream().filter(
						objACVD -> objACVD.getNtransactionstatus() == objTestGroupSpecification.getNapprovalstatus())
						.collect(Collectors.toList());
				if (!listACVD.isEmpty()) {
					if (objTestGroupSpec.getNapprovalstatus() == objTestGroupSpecification.getNapprovalstatus()) {
						final String returnMessage = commonFunction.getMultilingualMessage("IDS_SPECALREADY",
								objUserInfo.getSlanguagefilename())
								+ " "
								+ commonFunction.getMultilingualMessage(
										objTestGroupSpecification.getStransdisplaystatus(),
										objUserInfo.getSlanguagefilename());
						return new ResponseEntity<Object>(returnMessage, HttpStatus.EXPECTATION_FAILED);
					} else {
						// For_MSSQL
						// String queryformat="getutcdate()";

						// For_POSTGRESQL
						String queryformat = "now()";
						
						String query1 = "select  dexpirydate, " + queryformat + ", DATE_PART('day',  '"+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+ "'-dexpirydate) as ndate from TestGroupSpecification" + " where nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode();
						TestGroupSpecification object = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(query1,TestGroupSpecification.class,jdbcTemplate);

						
						if (object != null && object.getNdate() > 1) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTINVALIDATEDATE",
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							return new ResponseEntity<>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(),
									HttpStatus.OK);
						}
					}
				} else {
					ObjectMapper objMapper = new ObjectMapper();
					final List<ApprovalRoleValidationDetail> listValidationStatus = objMapper.convertValue(
							commonFunction.getMultilingualMessageList(listValidationActions,
									Arrays.asList("svalidationstatus"), objUserInfo.getSlanguagefilename()),
							new TypeReference<List<ApprovalRoleValidationDetail>>() {
							});
					final String returnMessage = commonFunction.getMultilingualMessage("IDS_SELECT",
							objUserInfo.getSlanguagefilename()) + " "
							+ stringUtilityFunction.fnDynamicListToString(listValidationStatus, "getSvalidationstatus") + " "
							+ commonFunction.getMultilingualMessage("IDS_SPEC", objUserInfo.getSlanguagefilename());
					return new ResponseEntity<Object>(returnMessage, HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_APPROVALVALIDATIONSTATUSNOTAVAILABLE",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> approveSpecification(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec,
			TreeTemplateManipulation objTree, int ntreeversiontempcode) throws Exception {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
		if (objTestGroupSpecification != null) {

			// Integer nprojectCodeVersionStatus = -1;
			if (objTree.getNprojectmastercode() != -1) {
				map = (Map<String, Object>) testGroupCommonFunction
						.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
				strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			}

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {

				final String Query = " lock  table locktestgrpspchistory "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				
				jdbcTemplate.execute(Query);

				final String sStatusQuery = "select ts.jsondata->'sactiondisplaystatus'->>'"
						+ objUserInfo.getSlanguagetypecode() + "' sactiondisplaystatus from transactionstatus ts"
						+ " where ts.ntranscode = " + objTestGroupSpec.getNapprovalstatus() + " and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				TransactionStatus objTransactionStatus = (TransactionStatus) jdbcUtilityFunction.queryForObject(sStatusQuery, TransactionStatus.class,jdbcTemplate);
				
				if (objTestGroupSpecification.getSversion().equals("")) {
					final String returnMsg = commonFunction.getMultilingualMessage("IDS_PLEASECOMPLETESPECIFICATIONTO",objUserInfo.getSlanguagefilename()) + " "
							+ commonFunction.getMultilingualMessage(objTransactionStatus.getSactiondisplaystatus(),
									objUserInfo.getSlanguagefilename());
					
					return new ResponseEntity<>(returnMsg, HttpStatus.EXPECTATION_FAILED);
				} else {
					final String validationQuery = " select acvd.ntransactionstatus,acvd.napprovalvalidationcode"
							+ " from approvalrolevalidationdetail acvd,approvalconfigrole acr "
							+ " where "
							+ " acvd.nuserrolecode = " + objUserInfo.getNuserrole()
							+ " and acvd.napprovalconfigrolecode=acr.napprovalconfigrolecode"
							+ " and acvd.nsitecode = acr.nsitecode and acr.nsitecode = "+objUserInfo.getNmastersitecode()+" "
							+ " and acr.napproveconfversioncode = "+ objTestGroupSpecification.getNapproveconfversioncode() + " "
							+ " and acvd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and acr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					final List<ApprovalRoleValidationDetail> validationStatus = (List<ApprovalRoleValidationDetail>) jdbcTemplate
							.query(validationQuery, new ApprovalRoleValidationDetail());
					
					final List<ApprovalRoleValidationDetail> validationStatus1 = validationStatus.stream().filter(x -> x.getNtransactionstatus() == objTestGroupSpecification.getNapprovalstatus())
							.collect(Collectors.toList());

					if (validationStatus1.isEmpty()) {
						final String returnMsg = commonFunction.getMultilingualMessage("IDS_NOTPOSSIBLETO",
								objUserInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage(objTransactionStatus.getSactiondisplaystatus(),
										objUserInfo.getSlanguagefilename());
						return new ResponseEntity<>(returnMsg, HttpStatus.EXPECTATION_FAILED);
					} else {
						//List<String> lstString = new ArrayList<String>();
						StringBuilder strBuilder = new StringBuilder();
						
						String sQuery = "select * from approvalroleactiondetail where ntransactionstatus="
								+ objTestGroupSpec.getNapprovalstatus() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and napprovalconfigrolecode in (select  napprovalconfigrolecode  from approvalconfigrole"
								+ " where napproveconfversioncode=" + objTestGroupSpec.getNapproveconfversioncode()
								+ " and nsitecode  = "+objUserInfo.getNmastersitecode()+" "
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " LIMIT 1)";
						
						List<ApprovalRoleActionDetail> lstActions = (List<ApprovalRoleActionDetail>) jdbcTemplate.query(sQuery, new ApprovalRoleActionDetail());
						if (!lstActions.isEmpty()) {
							if (objTestGroupSpec.getNapprovalstatus() != Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
								
								final String sUpdateQuery = "update testgroupspecification tgs set napprovalstatus="
										+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
										+ ",ntransactionstatus = "+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
										+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
										+ "  where napprovalstatus not in ("
										+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.CORRECTION.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ")"
										+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and napprovalstatus not in (select distinct ntransactionstatus from approvalroleactiondetail"
										+ " where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and napprovalconfigrolecode in (select napprovalconfigrolecode  "
										+ " from approvalconfigrole where napproveconfversioncode = "+ objTestGroupSpec.getNapproveconfversioncode()
										+ " and nlevelno <> 1 and nsitecode = "+objUserInfo.getNmastersitecode()+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
										//ALPD-5206 Added by  Neeraj Sample registration screen -> Specification showing wrongly in specific scenario.
										+ " and tgs.napproveconfversioncode=napproveconfversioncode )) "
										+ " and nsitecode  = "+objUserInfo.getNmastersitecode()+" "
										+ " and ntemplatemanipulationcode = "+ objTestGroupSpec.getNtemplatemanipulationcode() + " "
										+ " and nallottedspeccode <> " + objTestGroupSpec.getNallottedspeccode() + ";";

								//lstString.add(sUpdateQuery);
								strBuilder.append(sUpdateQuery);						}
						}
						final String sAppStatusQuery = "select tgs.*,ts.jsondata->'sactiondisplaystatus'->>'"
								+ objUserInfo.getSlanguagetypecode()
								+ "' sactiondisplaystatus from testgroupspecification tgs,transactionstatus ts"
								+ " where tgs.nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode()
								+ " and tgs.napprovalstatus=" + objTestGroupSpec.getNapprovalstatus()
								+ " and tgs.napprovalstatus = ts.ntranscode and tgs.nstatus = ts.nstatus and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						
						TestGroupSpecification checkSpecStatus = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(sAppStatusQuery, TestGroupSpecification.class,jdbcTemplate);

						if (checkSpecStatus == null) {
							if (objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
								
								final String sUpdateQuery = "update testgroupspecification set napprovalstatus = "
										+ objTestGroupSpec.getNapprovalstatus() + ", sversion = N'' "
										+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
										+ " where nallottedspeccode = " + objTestGroupSpec.getNallottedspeccode()
										+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ ";";
								//lstString.add(sUpdateQuery);
								strBuilder.append(sUpdateQuery);	
							} else {
								final String sUpdateQuery = "update testgroupspecification set napprovalstatus = "
										+ objTestGroupSpec.getNapprovalstatus() + " " + ",dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
												+ " where nallottedspeccode = "+ objTestGroupSpec.getNallottedspeccode() + " "
												+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
								//lstString.add(sUpdateQuery);
								strBuilder.append(sUpdateQuery);	
							}

							sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							final int nseqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate)
									+ Enumeration.TransactionStatus.SEQUENCEONE.gettransactionstatus();
							sQuery = "update seqnotestgroupmanagement set nsequenceno = " + nseqNo+ " where stablename = 'testgroupspecificationhistory' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							//lstString.add(sQuery);
							strBuilder.append(sQuery);	

							final String sInsertQry = "insert into testgroupspecificationhistory (nspecificationhistorycode, nallottedspeccode,"
									+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate,noffsetdtransactiondate, scomments,ntztransactiondate,dmodifieddate,nstatus,nsitecode)"
									+ " values (" + nseqNo + ", " + objTestGroupSpec.getNallottedspeccode() + ", "
									+ objTestGroupSpec.getNapprovalstatus() + ", " + objUserInfo.getNusercode() + ","
									+ objUserInfo.getNuserrole() + ", '" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + "," + " N'"
									+ stringUtilityFunction.replaceQuote(objUserInfo.getSreason()) + "', " + objUserInfo.getNtimezonecode()
									+ ", " + "'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");";
							//lstString.add(sInsertQry);
							strBuilder.append(sInsertQry);
							jdbcTemplate.execute(strBuilder.toString());	
							
//							String[] strFinal = lstString.toArray(new String[0]);
//							executeBulkDatainSingleInsert(strFinal);

							final String actionStatus = objTransactionStatus.getSactiondisplaystatus().toUpperCase()+ " " + commonFunction.getMultilingualMessage("IDS_SPECIFICATION",
											objUserInfo.getSlanguagefilename()).toUpperCase();

							final List<String> lstactionType = new ArrayList<String>();
							lstactionType.add(actionStatus);

							final List<Object> lstnewobject = new ArrayList<Object>();
							final TestGroupSpecification objTestGroupSpecificationNew = testGroupCommonFunction.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
							lstnewobject.add(objTestGroupSpecificationNew);
							auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);

							return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo, objTree, true, objTestGroupSpec).getBody(),HttpStatus.OK);
						} else {
							final String returnMsg = commonFunction.getMultilingualMessage("IDS_SPECALREADY",objUserInfo.getSlanguagefilename()) + " "
									+ commonFunction.getMultilingualMessage(checkSpecStatus.getSactiondisplaystatus(),objUserInfo.getSlanguagefilename());
							return new ResponseEntity<>(returnMsg, HttpStatus.EXPECTATION_FAILED);
						}
					}
				}

			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getTemplateMasterDetails(final UserInfo objUserInfo, final int ncategorycode,
			final SampleType objSampleType, final int ntreeversiontempcode,
			final TreeTemplateManipulation objTreeTemplateManipulation, final int nprojectmastercode) throws Exception {
		final String sQuery = "select tc.slabelname, ttttg.ntemptranstestgroupcode, ttm.ntemplatecode, ttttg.slevelformat,"
				+ " ROW_NUMBER() OVER(ORDER BY ttttg.nlevelno desc) as nlevelno, ttttg.schildnode,"
				+ " ttttg.ntreeversiontempcode,ttm.nformcode,ttttg.schildnode from treetemptranstestgroup ttttg,treecontrol tc,treetemplatemaster ttm "
				+ " where ttttg.ntemplatecode = ttm.ntemplatecode and tc.ntreecontrolcode = ttttg.ntreecontrolcode "
				+ " and ttm.nstatus = tc.nstatus and ttm.nstatus = ttttg.nstatus and ttttg.nsitecode = tc.nsitecode and tc.nsitecode = ttm.nsitecode "
				+ " and ttm.nsitecode = "+objUserInfo.getNmastersitecode() +" "
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttttg.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and ttm.ncategorycode = "+ ncategorycode + " "
				+ " and ttm.nformcode = " + objSampleType.getNformcode()
				+ " and ttttg.nsampletypecode = " + objSampleType.getNsampletypecode()
				+ " and ttttg.ntreeversiontempcode = " + ntreeversiontempcode + " order by nlevelno desc;";
		List<TreetempTranstestGroup> listTreetempTranstestGroup = (List<TreetempTranstestGroup>) jdbcTemplate
				.query(sQuery, new TreetempTranstestGroup());
		if (!listTreetempTranstestGroup.isEmpty()) {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

			// Integer nprojectStatuscode=-1;
			if (nprojectmastercode != -1) {
				map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(nprojectmastercode, objUserInfo);
				strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			}

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);

			} else {

				Map<String, Object> outputMap = new HashMap<String, Object>();
				if (objTreeTemplateManipulation != null) {
					final String sTempManipQuery = ";with RECURSIVE tree (ntemplatemanipulationcode, ntreeversiontempcode, sleveldescription, nproductcatcode,"
							+ " nproductcode, nsampletypecode, schildnode, nparentnode, ntemptranstestgroupcode) as "
							+ " (select ntemplatemanipulationcode, ntreeversiontempcode, sleveldescription, nproductcatcode, nproductcode, nsampletypecode, schildnode,"
							+ " nparentnode, ntemptranstestgroupcode"
							+ " from treetemplatemanipulation where nsitecode = "+objUserInfo.getNmastersitecode()+" and ntemplatemanipulationcode = "
							+ objTreeTemplateManipulation.getNtemplatemanipulationcode() + " "
							+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " union all "
							+ " select ttm.ntemplatemanipulationcode, ttm.ntreeversiontempcode, ttm.sleveldescription, ttm.nproductcatcode, ttm.nproductcode,"
							+ " ttm.nsampletypecode, ttm.schildnode, ttm.nparentnode, ttm.ntemptranstestgroupcode"
							+ " from treetemplatemanipulation ttm, tree t"
							+ " where ttm.ntemplatemanipulationcode = t.nparentnode and ttm.nsitecode = "+ objUserInfo.getNmastersitecode() +" "
							+ " and ttm.nproductcatcode = "+ objTreeTemplateManipulation.getNproductcatcode() + ""
							+ " and ttm.nsampletypecode = "+ objTreeTemplateManipulation.getNsampletypecode() + " "
							+ " and ttm.nproductcode = "+ objTreeTemplateManipulation.getNproductcode() + " "
							+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) select * from tree;";

					final List<TreeTemplateManipulation> listTempManip = (List<TreeTemplateManipulation>) jdbcTemplate
							.query(sTempManipQuery, new TreeTemplateManipulation());

					List<TreetempTranstestGroup> listTreetemp = listTreetempTranstestGroup.stream().filter(x -> x
							.getNtemptranstestgroupcode() == objTreeTemplateManipulation.getNtemptranstestgroupcode())
							.collect(Collectors.toList());
					boolean isLastNode = false;
					if (!listTreetemp.isEmpty()) {

						for (TreetempTranstestGroup objTreetempTranstestGroup : listTreetempTranstestGroup) {

							final List<TreeTemplateManipulation> tempManipulation = listTempManip.stream().filter(
									tempManip -> tempManip.getNtemptranstestgroupcode() == objTreetempTranstestGroup
									.getNtemptranstestgroupcode())
									.collect(Collectors.toList());

							if (!tempManipulation.isEmpty()) {
								objTreetempTranstestGroup
								.setSleveldescription(tempManipulation.get(0).getSleveldescription());
								objTreetempTranstestGroup.setNtemplatemanipulationcode(
										tempManipulation.get(0).getNtemplatemanipulationcode());
								objTreetempTranstestGroup.setIsreadonly(true);
								isLastNode = true;
							} else {
								objTreetempTranstestGroup.setIsreadonly(false);
								isLastNode = false;
							}

						}

						if (isLastNode) {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_LASTLEVELNODE",
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							outputMap.put("TreetempTranstestGroup", listTreetempTranstestGroup);
						}
					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_LASTLEVELNODE",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					outputMap.put("TreetempTranstestGroup", listTreetempTranstestGroup);
				}
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			}

		} else {
			return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> updateCreateTreeSeqNo(List<TreeTemplateManipulation> listTree) throws Exception {

		final String Query = " lock  table locktestgrouprofile " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);

		final String sSeqQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'treetemplatemanipulation' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		final int nSeqNo =(int) jdbcUtilityFunction.queryForObject(sSeqQuery, Integer.class,jdbcTemplate);
		
		jdbcTemplate.execute("update seqnotestgroupmanagement set nsequenceno = " + (nSeqNo + listTree.size())
				+ " where stablename = 'treetemplatemanipulation' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"");
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.put("nSeqNo", nSeqNo);
		return outputMap;
	}

	@Override
	public ResponseEntity<Object> createTree(final UserInfo objUserInfo, final SampleType objSampleType,
			final int ncategorycode, final int nproductcode, final int ntreeversiontempcode,
			final int nprojectMasterCode, final List<TreeTemplateManipulation> listTree
			//			, final Map<String, Object> inputMap
			) throws Exception {

		final String sSeqQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'treetemplatemanipulation' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
		int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sSeqQuery, Integer.class,jdbcTemplate);
		
		String slevelcode = "/";
		int nparencode = -1;
		int ncount = 0;
		final StringBuilder sb = new StringBuilder();
		final String sntemplatemanipulationcode = stringUtilityFunction.fnDynamicListToString(listTree, "getNtemplatemanipulationcode");
		final String sQuery = "select * from treetemplatemanipulation where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ntemplatemanipulationcode in ("+ sntemplatemanipulationcode + ")";
		
		final List<TreeTemplateManipulation> oldTreeTemplateManipulation = (List<TreeTemplateManipulation>) jdbcTemplate.query(sQuery, new TreeTemplateManipulation());
		
		for (TreeTemplateManipulation objTreeTempManip : listTree) {
			boolean isOldTree = false;
			TreeTemplateManipulation objTreeTemplateManipulation = new TreeTemplateManipulation();
			
			if (!oldTreeTemplateManipulation.isEmpty()) {
				final List<TreeTemplateManipulation> oldTreeTemplateManipulation1 = oldTreeTemplateManipulation.stream()
						.filter(x -> x.getNtemptranstestgroupcode() == objTreeTempManip.getNtemptranstestgroupcode())
						.collect(Collectors.toList());
				if (!oldTreeTemplateManipulation1.isEmpty()) {
					isOldTree = true;
					objTreeTemplateManipulation = oldTreeTemplateManipulation1.get(0);
				}
			}

			if (isOldTree) {
				objTreeTempManip
				.setNtemplatemanipulationcode(objTreeTemplateManipulation.getNtemplatemanipulationcode());
				objTreeTempManip.setSchildnode(objTreeTemplateManipulation.getSchildnode());
				objTreeTempManip.setSlevelcode(objTreeTemplateManipulation.getSlevelcode());
			} else {
				nSeqNo = nSeqNo + 1;
				objTreeTempManip.setNtemplatemanipulationcode(nSeqNo);
			}

			final List<TreeTemplateManipulation> parentTreeTemplateManipulation = listTree.stream()
					.filter(x -> x.getNnextchildcode() == objTreeTempManip.getNtemptranstestgroupcode())
					.collect(Collectors.toList());
			if (parentTreeTemplateManipulation.isEmpty()) {
				if (ncount != 0) {
					nparencode = nSeqNo - 1;
				}
			} else {
				nparencode = parentTreeTemplateManipulation.get(0).getNtemplatemanipulationcode();
			}
			objTreeTempManip.setNparentnode(nparencode);

			ncount++;
		}

		for (TreeTemplateManipulation objTreeTempManip : listTree) {
			listTree.stream().forEach(x -> {
				if (objTreeTempManip.getNtemplatemanipulationcode() == x.getNparentnode()) {
					List<String> childList = Arrays.asList(objTreeTempManip.getSchildnode().split(","));
					if (!childList.contains(String.valueOf(x.getNtemplatemanipulationcode()))) {
						objTreeTempManip.setSchildnode(objTreeTempManip.getSchildnode().isEmpty()
								? String.valueOf(x.getNtemplatemanipulationcode())
										: objTreeTempManip.getSchildnode() + "," + x.getNtemplatemanipulationcode());
					}
				}
			});
		}

		for (TreeTemplateManipulation objTreeTempManip : listTree) {
			int i = 0;
			final List<TreeTemplateManipulation> checkOldTreeTemplateManipulation = oldTreeTemplateManipulation.stream()
					.filter(parent -> parent.getNtemptranstestgroupcode() == objTreeTempManip
					.getNtemptranstestgroupcode())
					.collect(Collectors.toList());

			if (checkOldTreeTemplateManipulation.isEmpty()) {
				if (!objTreeTempManip.getSchildnode().equals("")) {
					final List<TreeTemplateManipulation> findParentNode = listTree.stream().filter(
							parent -> parent.getNtemplatemanipulationcode() == objTreeTempManip.getNparentnode())
							.collect(Collectors.toList());
					if (!findParentNode.isEmpty()) {
						String[] at = findParentNode.get(0).getSchildnode().split(",");
						i = at.length - 1;
					}
				} else {
					i = 0;
				}
			} else {
				if (!objTreeTempManip.getSchildnode().equals("")) {
					final List<TreeTemplateManipulation> findParentNode = listTree.stream().filter(
							parent -> parent.getNtemplatemanipulationcode() == objTreeTempManip.getNparentnode())
							.collect(Collectors.toList());
					if (!findParentNode.isEmpty()) {
						String[] at = findParentNode.get(0).getSchildnode().split(",");
						i = at.length - 1;
					}
				} else {
					i = 0;
				}
			}
			slevelcode = slevelcode.concat("" + (i + 1) + "/");
			objTreeTempManip.setSlevelcode(slevelcode);
			objTreeTempManip.setNproductcatcode(ncategorycode);
			objTreeTempManip.setNproductcode(nproductcode);
			objTreeTempManip.setNprojectmastercode(nprojectMasterCode);
			objTreeTempManip.setNsampletypecode(objSampleType.getNsampletypecode());

			if (objTreeTempManip.isIsreadonly()) {
				final String sUpdateQuery = "update treetemplatemanipulation set schildnode = N'"
						+ objTreeTempManip.getSchildnode() + "'" + " " + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where ntemplatemanipulationcode = "
						+ objTreeTempManip.getNtemplatemanipulationcode() + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				sb.append(sUpdateQuery);
			} else {
				final String insQuery = "insert into treetemplatemanipulation (ntemplatemanipulationcode, nsampletypecode, nformcode, ntreeversiontempcode, ntemptranstestgroupcode,"
						+ "nproductcatcode, nproductcode, nprojectmastercode, slevelcode, sleveldescription, nparentnode, schildnode, nnextchildcode, npositioncode,dmodifieddate, nstatus,nsitecode)"
						+ "values (" + objTreeTempManip.getNtemplatemanipulationcode() + ","
						+ objSampleType.getNsampletypecode() + ", " + objTreeTempManip.getNformcode() + ", "
						+ objTreeTempManip.getNtreeversiontempcode() + ","
						+ objTreeTempManip.getNtemptranstestgroupcode() + "," + ncategorycode + "," + nproductcode + ","
						+ nprojectMasterCode + ",N'" + objTreeTempManip.getSlevelcode() + "'" + ",N'"
						+ stringUtilityFunction.replaceQuote(objTreeTempManip.getSleveldescription()) + "',"
						+ objTreeTempManip.getNparentnode() + ",N'" + objTreeTempManip.getSchildnode() + "',"
						+ objTreeTempManip.getNnextchildcode() + "," + objTreeTempManip.getNpositioncode() + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ objUserInfo.getNmastersitecode() + ");";
				sb.append(insQuery);
			}
		}
		jdbcTemplate.execute(sb.toString());
		jdbcTemplate.execute("update seqnotestgroupmanagement set nsequenceno = " + nSeqNo+ " where stablename = 'treetemplatemanipulation' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(listTree), 1, null, Arrays.asList("IDS_ADDPROFILETREE"), objUserInfo);
		return new ResponseEntity<Object>(getTreeTemplateManipulation(objUserInfo, objSampleType.getNsampletypecode(),
				ncategorycode, nproductcode, ntreeversiontempcode, false, 0, "", nprojectMasterCode), HttpStatus.OK);
	}

	
	private List<SampleType> getSampleType(final int nmasterSiteCode, UserInfo objUserInfo) throws Exception {
		final String sQuery = "select distinct st.nformcode, st.nsampletypecode,st.nsorter,st.nclinicaltyperequired,st.ncategorybasedflowrequired,st.nrulesenginerequired, st.nprojectspecrequired,coalesce(jsondata->'sampletypename'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sampletypename'->>'en-US') as  ssampletypename,st.ncomponentrequired,st.nportalrequired "
				+ " from treeversiontemplate tvt, treetemplatemaster ttm, sampletype st "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and st.nformcode = ttm.nformcode "
				+ " and tvt.nstatus = st.nstatus and ttm.nstatus = st.nstatus and tvt.nsitecode = ttm.nsitecode"
				+ " and ttm.nsitecode ="+ objUserInfo.getNmastersitecode()+ " "
				+ " and st.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tvt.ntransactionstatus = "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ " and st.nsitecode = "+ nmasterSiteCode + " order by st.nsorter asc";
		return (List<SampleType>) jdbcTemplate.query(sQuery, new SampleType());
	}

	private List<Map<String, Object>> getSampleTypeDesign(final int nsampletypecode) {
		
		final String strQuery = "select et.splainquery,et.sexistingtablename,std.sdisplaylabelname from sampletypedesign std, existinglinktable et"
				+ " where  std.nexistinglinkcode = et.nexistingcode and std.nsampletypecode = " + nsampletypecode + " "
				+ " and std.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and et.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by std.nsorter";
		return jdbcTemplate.queryForList(strQuery);
	}


	private List<ProductCategory> getProductCategory(final int nmasterSiteCode, final SampleType objSampleType)
			throws Exception {

		final String sQuery = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode"
				+ " and pc.nsitecode = tvt.nsitecode and tvt.nsitecode = ttm.nsitecode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nproductcatcode > 0 and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and pc.nsitecode = " + nmasterSiteCode
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = " + objSampleType.getNformcode();
		return (List<ProductCategory>) jdbcTemplate.query(sQuery, new ProductCategory());
	}

	
	private List<InstrumentCategory> getInstrumentCategory(final int nmasterSiteCode, final SampleType objSampleType)
			throws Exception {
		final String sQuery = "select ic.*, ic.sinstrumentcatname sproductcatname, ic.ninstrumentcatcode nproductcatcode"
				+ " from instrumentcategory ic, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = ic.ninstrumentcatcode "
				+ " and ic.nsitecode = tvt.nsitecode and tvt.nsitecode = ttm.nsitecode "
				+ " and ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ninstrumentcatcode > 0" + " and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ic.nsitecode = " + nmasterSiteCode
				+ " and nformcode = " + objSampleType.getNformcode() + " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " group by  ic.ninstrumentcatcode";
		return (List<InstrumentCategory>) jdbcTemplate.query(sQuery, new InstrumentCategory());
	}


	private List<MaterialCategory> getMaterialCategory(final int nmasterSiteCode, final SampleType objSampleType)
			throws Exception {
		final String sQuery = "select mc.*,mc.nmaterialtypecode,mc.nmaterialcatcode nproductcatcode,mc.smaterialcatname sproductcatname "
				+ " from materialcategory mc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = mc.nmaterialcatcode "
				+ " and mc.nsitecode = tvt.nsitecode and tvt.nsitecode = ttm.nsitecode "
				+ " and mc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nmaterialcatcode > 0" + " and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and mc.nsitecode = " + nmasterSiteCode
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = " + objSampleType.getNformcode() + " group by mc.nmaterialcatcode";
		return (List<MaterialCategory>) jdbcTemplate.query(sQuery, new MaterialCategory());
	}


	private List<Product> getProduct(final int nmasterSiteCode, final ProductCategory objProductCategory)
			throws Exception {

		String sQuery = "";
		// For_MSSQL_PostgreSQL
		if (objProductCategory.getNcategorybasedflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			sQuery = "select * from product where nproductcode = "+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " "
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

		} else {
			sQuery = "select * from product where nstatus =  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode = "+ nmasterSiteCode
					+ " and nproductcatcode = " + objProductCategory.getNproductcatcode() + " and nproductcatcode > "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " order by nproductcode";
		}

		return (List<Product>) jdbcTemplate.query(sQuery, new Product());
	}


	private List<Instrument> getInstrument(final int nSiteCode, final InstrumentCategory objInstrumentCategory)
			throws Exception {

		final String sQuery = "select i.ninstrumentnamecode nproductcode, i.sinstrumentname sproductname"
				+ ", i.ninstrumentcatcode nproductcatcode " // ALPD-5578 - Gowtham R - Test Group -> when copying a spec to another category or another material/Instrument - copied in same category.
				+ "from instrument i, instrumentname insn   where" 
				+ " i.ninstrumentcatcode="+ objInstrumentCategory.getNinstrumentcatcode() + " and  i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and insn.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " and  insn.ninstrumentnamecode=i.ninstrumentnamecode"
				+ " group by i.sinstrumentname, i.ninstrumentnamecode, "
				+ " i.ninstrumentcatcode"; // ALPD-5578 - Gowtham R - Test Group -> when copying a spec to another category or another material/Instrument - copied in same category.
		return (List<Instrument>) jdbcTemplate.query(sQuery, new Instrument());
	}


	private List<Material> getMaterial(final UserInfo userinfo, final MaterialCategory objMaterialCategory)
			throws Exception {

		final String sQuery = "select i.*,i.nmaterialcode nproductcode,i.jsonuidata->>'Material Name' sproductname, "
				+ "i.nmaterialcatcode nproductcatcode " // ALPD-5578 - Gowtham R - Test Group -> when copying a spec to another category or another material/Instrument - copied in same category.
				+ "from material i  where i.nmaterialcatcode = "
				+ objMaterialCategory.getNmaterialcatcode() + ""
				+ " and i.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and i.nsitecode="+ userinfo.getNmastersitecode() + "";

		return (List<Material>) jdbcTemplate.query(sQuery, new Material());
	}


	private List<TreeVersionTemplate> getTemplateVersion(int nsampletypecode, int nformCode, int nproductCatCode,
			int nmasterSiteCode, UserInfo objUserInfo) throws Exception {
		String sQuery = "select tvt.ntreeversiontempcode,CONCAT(tvt.sversiondescription,' (',cast(nversionno as character varying(50)),')' ) as sversiondescription,"
				+ "tvt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from treetemplatemaster ttm,treeversiontemplate tvt, transactionstatus ts"
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ts.ntranscode = tvt.ntransactionstatus and ttm.nstatus = tvt.nstatus"
				+ " and ts.nstatus = tvt.nstatus and ttm.nsitecode =  tvt.nsitecode"
				+ " and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.nsitecode = "+ nmasterSiteCode + " "
				+ " and ttm.ncategorycode = " + nproductCatCode + " "
				+ " and tvt.nsampletypecode="+ nsampletypecode + " "
				+ " and ttm.nformcode = " + nformCode + " "
				+ " and tvt.ntransactionstatus <> "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " order by tvt.ntreeversiontempcode desc";
		return (List<TreeVersionTemplate>) jdbcTemplate.query(sQuery, new TreeVersionTemplate());
	}

	private List<TreeTemplateManipulation> getTreeTemplateManipulation(final int nformCode, final int productCatCode,
			final int nproductCode, final int ntreeversiontempcode, final int nsampletypecode, final int nprojectCode,final boolean isCopySpec)
					throws Exception {
		
		//ALPD-5904--Added by Vignesh R(25-06-2025)-->Test Group screen -> System allowing to copy spec to retired Template version.
		String transQuery="";
		if(isCopySpec) {
			transQuery=" and ttv.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" ";
		}
		
		//ALPD-4758 done by Dhanushya RI,to load sample category only for categorybasedflow yes
		String concatQry="";
		if(ntreeversiontempcode!=-1) {
			concatQry="and ttv.ntreeversiontempcode ="+ ntreeversiontempcode+" order by ntemplatemanipulationcode" ;
		}
		else {
			concatQry="order by ntemplatemanipulationcode";
		}
		String sQuery = "select ttm.ntemplatemanipulationcode, ttm.nsampletypecode, ttm.nformcode, "
				+ " ttm.ntreeversiontempcode, ttm.ntemptranstestgroupcode, ttm.nprojectmastercode, "
				+ " ttm.nproductcatcode, ttm.nproductcode, ttm.slevelcode, ttm.sleveldescription, ttm.nparentnode, "
				+ " ttm.schildnode, ttm.nnextchildcode, ttm.npositioncode, ttm.nstatus, tc.slabelname"
				+ " from treetemplatemanipulation ttm,treeversiontemplate ttv,treetemptranstestgroup ttg,treecontrol tc"
				+ " where ttm.ntreeversiontempcode=ttv.ntreeversiontempcode and ttm.ntreeversiontempcode = ttg.ntreeversiontempcode"
				+ " and tc.ntreecontrolcode = ttg.ntreecontrolcode "
				+ " and ttg.ntemptranstestgroupcode=ttm.ntemptranstestgroupcode and ttm.nsitecode = ttv.nsitecode and ttg.nsitecode = tc.nsitecode "
				+ " and ttm.nformcode=" + nformCode + " "
				+ " and ttm.nproductcatcode= "+ productCatCode + " "
				+ " and ttv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ttg.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nproductcode=" + nproductCode
				+ " and ttv.nsampletypecode=" + nsampletypecode + " "
				+ " and nprojectmastercode = " + nprojectCode
				+ " "+transQuery+" "+ concatQry;
		return (List<TreeTemplateManipulation>) jdbcTemplate.query(sQuery, new TreeTemplateManipulation());
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public ResponseEntity<Object> deleteTree(final Map<String, Object> inputMap, final UserInfo objUserInfo,
			final TreeTemplateManipulation objTree, final int ntreeversiontempcode) throws Exception {
		final TreeTemplateManipulation objTreeTemplateManipulation = getActiveTree(objTree);
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objTreeTemplateManipulation != null) {

				Map<String, Object> map = new LinkedHashMap<String, Object>();
				String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (objTree.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} 
				else {

					List<Object> mapLists = new ArrayList<>();
					List<Object> val = new ArrayList<>();
					String sQuery = "";
					List<String> lstActionType = new ArrayList<String>();
					String sntemplatemanipulationcode = "";
					List<TreeTemplateManipulation> lstManipulations = new ArrayList<TreeTemplateManipulation>();
					if (objTree.getNnextchildcode() == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
						sntemplatemanipulationcode = String.valueOf(objTree.getNtemplatemanipulationcode());
						if (objTree.getNparentnode() == Enumeration.TransactionStatus.NA.gettransactionstatus()
								|| objTree.getNnextchildcode() == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
							sQuery = "select * from treetemplatemanipulation  " + " where nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ "and ntemplatemanipulationcode in (" + sntemplatemanipulationcode + ");";
							lstManipulations = (List<TreeTemplateManipulation>) jdbcTemplate.query(sQuery,
									new TreeTemplateManipulation());
						}
					} else {

						sQuery = ";with RECURSIVE tree (ntemplatemanipulationcode, ntreeversiontempcode, sleveldescription, nproductcatcode, nproductcode, nprojectmastercode, "
								+ " nsampletypecode, schildnode, nparentnode) as (select ntemplatemanipulationcode, ntreeversiontempcode, sleveldescription, nproductcatcode,"
								+ " nproductcode, nprojectmastercode, nsampletypecode, schildnode, nparentnode"
								+ " from treetemplatemanipulation where ntemplatemanipulationcode = "+ objTree.getNtemplatemanipulationcode() + " "
								+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
								+ " union all "
								+ " select ttm.ntemplatemanipulationcode, ttm.ntreeversiontempcode, ttm.sleveldescription, ttm.nproductcatcode, ttm.nproductcode,ttm.nprojectmastercode,"
								+ " ttm.nsampletypecode, ttm.schildnode, ttm.nparentnode from treetemplatemanipulation ttm, tree t"
								+ " where ttm.nparentnode = t.ntemplatemanipulationcode "//and ttm.nsitecode = t.nsitecode "
								//+ " and t.nsitecode = "+ objUserInfo.getNmastersitecode() +" "
								+ " and ttm.nproductcatcode = "+ objTree.getNproductcatcode() + " "
								+ " and ttm.nproductcode = " + objTree.getNproductcode()
								+ " and ttm.nsampletypecode = " + objTree.getNsampletypecode()
								+ " and ttm.nprojectmastercode = " + objTree.getNprojectmastercode() + " and ttm.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) select * from tree;";
						lstManipulations = (List<TreeTemplateManipulation>) jdbcTemplate.query(sQuery,new TreeTemplateManipulation());
						
						sntemplatemanipulationcode = stringUtilityFunction.fnDynamicListToString(lstManipulations,"getNtemplatemanipulationcode");
					}
					final String sCheckSpecifiation = "select napprovalstatus from testgroupspecification where ntemplatemanipulationcode in ("
							+ sntemplatemanipulationcode + ")" + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					
					final List<TestGroupSpecification> listSpecificationGroup = (List<TestGroupSpecification>) jdbcTemplate.query(sCheckSpecifiation, new TestGroupSpecification());
					
					if (!listSpecificationGroup.isEmpty()) {
						final List<TestGroupSpecification> draftCorrectionSpec = listSpecificationGroup.stream().filter(
								x -> x.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
								|| x.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus())
								.collect(Collectors.toList());
						
						if (draftCorrectionSpec.size() != listSpecificationGroup.size()) {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SPECUNDERNODEMUSTBEDRAFTCORRECTION",
											objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
						}
					}
					
					String sDeleteQuery = "";
					if (objTree.getNparentnode() == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
						sQuery = "select * from treetemplatemanipulation where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  "
								+ "ntemplatemanipulationcode in (" + sntemplatemanipulationcode + ");";

						sDeleteQuery = "update treetemplatemanipulation set nstatus ="+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
								+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ " where ntemplatemanipulationcode in ("+ sntemplatemanipulationcode + ");";
					} else {
						final String sParentNodeQuery = "select * from treetemplatemanipulation where ntemplatemanipulationcode = "
								+ objTree.getNparentnode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						
						TreeTemplateManipulation objtemp = (TreeTemplateManipulation) jdbcUtilityFunction.queryForObject(sParentNodeQuery,
								TreeTemplateManipulation.class,jdbcTemplate);
						
						if (objtemp != null) {
							String child = objtemp.getSchildnode();
							String[] str1Array = child.split(",");
							List<String> lstChild = new ArrayList<String>(Arrays.asList(str1Array));
							String toupdate = null;
							if (lstChild.size() > 1) {
								lstChild.remove("" + objTree.getNtemplatemanipulationcode() + "");
								String[] toupdate2 = lstChild.toArray(new String[lstChild.size()]);
								toupdate = Arrays.toString(toupdate2);
								toupdate = toupdate.replaceAll("\\[", "").replaceAll("\\]", "");
								toupdate = toupdate.replaceAll("\\s+", "");
								sDeleteQuery = "update treetemplatemanipulation set schildnode='" + toupdate + "'"
										+ ",dmodifieddate='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
										+ " where ntemplatemanipulationcode=" + objTree.getNparentnode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
							} else {
								sDeleteQuery = "update treetemplatemanipulation set schildnode= '',dmodifieddate='"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntemplatemanipulationcode="
										+ objTree.getNparentnode() + " and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
							}

							sDeleteQuery = sDeleteQuery + "update treetemplatemanipulation set nstatus = "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ",dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where ntemplatemanipulationcode in ("
									+ sntemplatemanipulationcode + ");";
						}

					}
					final String sQuery1 = " select * from testgroupspecification where ntemplatemanipulationcode in ("
							+ sntemplatemanipulationcode + " ) and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
					
					final List<TestGroupSpecification> testGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
							.query(sQuery1, new TestGroupSpecification());

					if (!testGroupSpecification.isEmpty()) {
						String salloctspeccode = stringUtilityFunction.fnDynamicListToString(testGroupSpecification, "getNallottedspeccode");

						final String sQuery2 = "select STRING_AGG(cast(tgt.ntestgrouptestcode as varchar(10)),',') from testgroupspecsampletype tgsst, testgrouptest tgt"
								+ " where tgsst.nspecsampletypecode = tgt.nspecsampletypecode and tgsst.nstatus = tgt.nstatus"
								+ " tgsst.nsitecode = tgt.nsitecode and tgt.nsitecode = "+objUserInfo.getNmastersitecode() +" "
								+ " and tgsst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgsst.nallottedspeccode in (" + salloctspeccode + ")";
						;
						List<String> lstTestGroupTestCode = jdbcTemplate.queryForList(sQuery2, String.class);
						final String sTestGroupTestCode = String.join(",", lstTestGroupTestCode);

//						sQuery = "select * from treetemplatemanipulation  where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//								+ " and ntemplatemanipulationcode in (" + sntemplatemanipulationcode + ");";
//						List<TreeTemplateManipulation> lstTreeTemplateManipulation = jdbcTemplate.query(sQuery,
//								new TreeTemplateManipulation());

						sDeleteQuery = sDeleteQuery + "update testgroupspecification set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where nallottedspeccode in ( " + salloctspeccode+ ");";
						sDeleteQuery = sDeleteQuery + "update testgroupspecsampletype set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where nallottedspeccode in ( " + salloctspeccode+ ");";
						sDeleteQuery = sDeleteQuery + "update testgroupspecfile set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where nallottedspeccode in ( " + salloctspeccode+ ");";

						Map obj = (Map) ((Map) ((Map) ((Map) inputMap.get("genericlabel")).get("DeleteComponent")).get("jsondata")).get("sdisplayname");
						String deletecomponentlabel = (String) obj.get(objUserInfo.getSlanguagetypecode());

						lstActionType.addAll(Arrays.asList("IDS_DELETETREE", "IDS_DELETESPEC", deletecomponentlabel,"IDS_DELETESPECFILE"));
						
						if (!sTestGroupTestCode.equals("null") && !sTestGroupTestCode.isEmpty()) {
							sDeleteQuery = sDeleteQuery + testGroupCommonFunction
									.getComponentChildDetailsUpdateQuery(sTestGroupTestCode, objUserInfo);
							lstActionType.addAll(Arrays.asList("IDS_DELETETEST", "IDS_DELETETESTPARAMETER",
									"IDS_DELETEPREDEFINEDPARAMETER", "IDS_DELETECHARACTERPARAMETER",
									"IDS_DELETENUMERICPARAMETER", "IDS_DELETETESTFORMULA", "IDS_DELETETESTFILE"));
						}
					} else {
						lstActionType.add("IDS_DELETETREE");
						val.add(lstManipulations);
					}

					jdbcTemplate.execute(sDeleteQuery);

					auditUtilityFunction.fnInsertListAuditAction(!testGroupSpecification.isEmpty() ? mapLists : val, 1, null, lstActionType,
							objUserInfo);
					return new ResponseEntity<Object>(
							getTreeTemplateManipulation(objUserInfo, objTree.getNsampletypecode(),
									objTree.getNproductcatcode(), objTree.getNproductcode(),
									objTree.getNtreeversiontempcode(), false, 0, "", objTree.getNprojectmastercode()),
							HttpStatus.OK);

				}

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",objUserInfo.getSlanguagefilename()), 
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private TreeTemplateManipulation getActiveTree(final TreeTemplateManipulation objTree) throws Exception {
		final String sTree = "select * from treetemplatemanipulation where ntemplatemanipulationcode = "
				+ objTree.getNtemplatemanipulationcode() + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		return (TreeTemplateManipulation) jdbcUtilityFunction.queryForObject(sTree, TreeTemplateManipulation.class,jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getTreeById(UserInfo objUserInfo, TreeTemplateManipulation objTree,
			int ntreeversiontempcode) throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final String sTreeEditQry = "select ttm.*, tc.slabelname from treetemplatemanipulation ttm, treetemptranstestgroup ttttg, treecontrol tc"
				+ " where ttm.ntemptranstestgroupcode = ttttg.ntemptranstestgroupcode and tc.ntreecontrolcode = ttttg.ntreecontrolcode"
				+ " and tc.nstatus = ttm.nstatus and ttttg.nstatus = tc.nstatus "
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ttm.ntemplatemanipulationcode = "+ objTree.getNtemplatemanipulationcode();
		
		final TreeTemplateManipulation objTreeTemplateManipulation = (TreeTemplateManipulation)jdbcUtilityFunction.queryForObject(
				sTreeEditQry, TreeTemplateManipulation.class,jdbcTemplate);
		
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			if (objTreeTemplateManipulation != null) {

				// Integer nprojectCodeVersionStatus = -1;
				if (objTree.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction.getProjectMasterStatusCode(objTree.getNprojectmastercode(), objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);

				} else {

					return new ResponseEntity<Object>(objTreeTemplateManipulation, HttpStatus.OK);

				}

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTree(UserInfo objUserInfo, TreeTemplateManipulation objTree, String sNode)
			throws Exception {
		final TreeTemplateManipulation objTreeTemplateManipulation = getActiveTree(objTree);
		if (objTreeTemplateManipulation != null) {
			final String sUpdateQuery = "update treetemplatemanipulation set sleveldescription = N'"
					+ stringUtilityFunction.replaceQuote(objTree.getSleveldescription()) + "',dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'where ntemplatemanipulationcode = "
					+ objTree.getNtemplatemanipulationcode();
			jdbcTemplate.update(sUpdateQuery);
			
			auditUtilityFunction.fnInsertAuditAction(Arrays.asList(objTree), 2, Arrays.asList(objTreeTemplateManipulation),
					Arrays.asList("IDS_EDITPROFILETREE"), objUserInfo);
			
			return new ResponseEntity<Object>(getTreeTemplateManipulation(objUserInfo, objTree.getNsampletypecode(), objTree.getNproductcatcode(),
							objTree.getNproductcode(), objTree.getNtreeversiontempcode(), true,objTree.getNtemplatemanipulationcode(), sNode, objTree.getNprojectmastercode()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> filterTestGroup(final UserInfo objUserInfo, final int nSampleTypeCode,final int nproductCatCode, 
			final int nproductCode, final int nTreeVersionTempCode, final int nprojectCode)	throws Exception {
		
		return new ResponseEntity<Object>(getTreeTemplateManipulation(objUserInfo, nSampleTypeCode, nproductCatCode,
				nproductCode, nTreeVersionTempCode, false, 0, "", nprojectCode), HttpStatus.OK);
	}

	// Copy Specification Start
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<Object> copySpecification(Map<String, Object> inputMap, final int ntreeversiontempcode)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ntemplatemanipulationcode= (int) inputMap.get("selectedCopyNodeManipulationCode");
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		if(ntemplatemanipulationcode == -1) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDPROFILE",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {

			objMapper.registerModule(new JavaTimeModule());
			int ntestgroupspeccode = (int) inputMap.get("testgroupspecification");
			ntestgroupspeccode = ntestgroupspeccode + 1;
			final TestGroupSpecification copySpec = (TestGroupSpecification) inputMap.get("testSpecification");
			final TestGroupSpecification selectedSpecification = objMapper
					.convertValue(inputMap.get("selectedspecification"), TestGroupSpecification.class);
			final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),
					TreeTemplateManipulation.class);
//			int nsampletypecode = (objTree.getNsampletypecode() == Enumeration.SampleType.CLINICALSPEC.getType()
//					? Enumeration.TransactionStatus.YES.gettransactionstatus(): Enumeration.TransactionStatus.NO.gettransactionstatus());
			copySpec.setSexpirydate(dateUtilityFunction.instantDateToString(copySpec.getDexpirydate()).replace("T", " ").replace("Z", " "));
			
			final TestGroupSpecification copySpecification = objMapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(copySpec, Arrays.asList("sexpirydate"),
							Arrays.asList("ntzexpirydate"), true, objUserInfo),	new TypeReference<TestGroupSpecification>() {});
			
			TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
			if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {

				if (copySpecification.getDexpirydate().isAfter(Instant.now())) {
					//String[] strQuery = new String[8];
					
					final String strQuery1 = " insert into testgroupspecification ( nallottedspeccode,ntemplatemanipulationcode,napproveconfversioncode,ncomponentrequired,"
							+ "nprojectmastercode,ntransactionstatus,sspecname,sversion,dexpirydate,noffsetdexpirydate,ntzexpirydate,dmodifieddate,napprovalstatus,nstatus,nsitecode)"
							+ " select " + ntestgroupspeccode + ","+ntemplatemanipulationcode+","
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
							+ selectedSpecification.getNcomponentrequired() + ","
							+ copySpecification.getNprojectmastercode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",N'"
							+ stringUtilityFunction.replaceQuote(copySpecification.getSspecname()) + "','','" + copySpecification.getDexpirydate()
							+ "'," + copySpecification.getNoffsetdexpirydate() + ",'" + copySpecification.getNtzexpirydate()
							+ "'" + ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + ","
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "" + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + "  from testgroupspecification "
							+ " where nallottedspeccode in (" + selectedSpecification.getNallottedspeccode() + ") "
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					final String strQuery2 = " insert into testgroupspecfile (  nspecfilecode,nallottedspeccode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,"
							+ "  dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nstatus,nsitecode)"
							+ " select " + inputMap.get("testgroupspecfile") + " +RANK() over(order by nspecfilecode),"
							+ ntestgroupspeccode + "," + " nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,"
							+ "  dcreateddate,noffsetdcreateddate," + objUserInfo.getNtimezonecode() + ",ssystemfilename,'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + " from testgroupspecfile" + " where nallottedspeccode in ("
							+ selectedSpecification.getNallottedspeccode() + ") and " + " nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					final String strQuery3 = " insert into testgroupspecsampletype (nspecsampletypecode, nallottedspeccode, ncomponentcode,dmodifieddate, nstatus,nsitecode)"
							+ " select " + inputMap.get("testgroupspecsampletype")
							+ "+RANK() over(order by nspecsampletypecode)," + "" + ntestgroupspeccode + ", ncomponentcode,"
							+ "'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + " "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + "  from testgroupspecsampletype where  nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nallottedspeccode in ("
							+ selectedSpecification.getNallottedspeccode() + ");";

					final String strQuery4 = " insert into testgrouptest (ntestgrouptestcode, nspecsampletypecode, ntestcode, nsectioncode,"// +"nsourcecode,"
							+ "nmethodcode,ninstrumentcatcode,ncontainertypecode,stestsynonym,nrepeatcountno,ncost,nsorter,nisadhoctest,nisvisible,dmodifieddate,nsitecode,nstatus,ntestpackagecode)"
							+ " select " + inputMap.get("testgrouptest")
							+ "+RANK() over(order by ntestgrouptestcode) ntestgrouptestcode" + ", "
							+ inputMap.get("testgroupspecsampletype")
							+ "+DENSE_RANK() over(order by ts.nspecsampletypecode) nspecsampletypecode,"
							+ " ntestcode, nsectioncode,"
							+ "nmethodcode,ninstrumentcatcode,ncontainertypecode,stestsynonym,nrepeatcountno,ncost,nsorter,"
							+ "nisadhoctest,nisvisible,'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",ntestpackagecode  from testgrouptest t,"
							+ "testgroupspecsampletype ts where   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  ts.nspecsampletypecode=t.nspecsampletypecode and t.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and   " + "ts.nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					
					jdbcTemplate.execute(strQuery1 + strQuery2 + strQuery3 + strQuery4);
					
					final String strQuery5 = " insert into testgrouptestfile (ntestgroupfilecode,ntestgrouptestcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,"
							+ "ssystemfilename,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,dmodifieddate,nstatus,nsitecode) "
							+ "  select  " + inputMap.get("testgrouptestfile")
							+ "+RANK() over(order by f.ntestgroupfilecode) ntestgroupfilecode," + " "
							+ inputMap.get("testgrouptest")
							+ "+DENSE_RANK() over(order by t.ntestgrouptestcode) ntestgrouptestcode,f.nlinkcode,"
							+ " f.nattachmenttypecode,f.sfilename,f.sdescription,f.ssystemfilename,f.nfilesize,f.dcreateddate,f.noffsetdcreateddate,f.ntzcreateddate,"
							+ " '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode()
							+ "  from testgrouptest t,testgroupspecsampletype ts,testgrouptestfile f  where  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  f.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  ts.nspecsampletypecode=t.nspecsampletypecode "
							+ " and t.ntestgrouptestcode=f.ntestgrouptestcode and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";

					final String strQuery6 = " insert into testgrouptestparameter (ntestgrouptestparametercode,ntestgrouptestcode,ntestparametercode,nparametertypecode,"
							+ "nunitcode,sparametersynonym,nroundingdigits,nresultmandatory,nreportmandatory,ngradingmandatory,nchecklistversioncode,"
							+ "sspecdesc,nsorter,nisadhocparameter,nisvisible,dmodifieddate,nstatus,nsitecode,nresultaccuracycode) select "
							+ inputMap.get("testgrouptestparameter")
							+ "+RANK() over(order by ntestgrouptestparametercode) ntestgrouptestparametercode," + ""
							+ inputMap.get("testgrouptest")
							+ "+DENSE_RANK() over(order by t.ntestgrouptestcode) ntestgrouptestcode,"
							+ " tp.ntestparametercode,tp.nparametertypecode,tp.nunitcode,tp.sparametersynonym,tp.nroundingdigits,tp.nresultmandatory,"
							+ "tp.nreportmandatory,tp.ngradingmandatory,tp.nchecklistversioncode,tp.sspecdesc,tp.nsorter,tp.nisadhocparameter,tp.nisvisible,"
							+ " '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + ""
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ " "+objUserInfo.getNmastersitecode()+", tp.nresultaccuracycode "
							+ " from  testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter"
							+ " tp where   tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and   t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "and   ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and   tp.ntestgrouptestcode=t.ntestgrouptestcode "
							+ " and  ts.nspecsampletypecode=t.nspecsampletypecode  and tp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and    nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					//ALPD-3597
					final String strQuery7 = "insert into testgrouptestformula (ntestgrouptestformulacode,ntestgrouptestcode,ntestgrouptestparametercode,ntestformulacode,"
							+ " sformulacalculationcode,sformulacalculationdetail,ntransactionstatus,dmodifieddate,nstatus,nsitecode)"
							+ "" + " select  " + inputMap.get("testgrouptestformula")
							+ "+RANK() over(order by c.ntestgrouptestparametercode) ntestgrouptestformulacode,d.ntestgrouptestcode,"
							+ " d.ntestgrouptestparametercode,c.ntestformulacode,c.sformulacalculationcode,c.sformulacalculationdetail,c.ntransactionstatus,"
							+ " '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + " from "
							+ " (select tp.ntestgrouptestparametercode ntestgrouptestparametercode,t.ntestgrouptestcode,tp.ntestparametercode, "
							+ " ts.nspecsampletypecode  from  testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp where"
							+ "  tp.nparametertypecode=1 and   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and   tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and   tp.ntestgrouptestcode=t.ntestgrouptestcode  and  ts.nspecsampletypecode=t.nspecsampletypecode and   nallottedspeccode="
							+ ntestgroupspeccode + ") d, "
							+ "(select a.ntestparametercode,b.ntestformulacode, a.ntestgrouptestparametercode, b.sformulacalculationcode,"
							+ " b.sformulacalculationdetail, b.ntransactionstatus,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus from "
							+ " (select ts.*,tp.ntestgrouptestparametercode ntestgrouptestparametercode,t.ntestgrouptestcode,tp.ntestparametercode,ts.nspecsampletypecode "
							+ " from  testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp where "
							+ " tp.nparametertypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and   t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and   ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ "  tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ "   tp.ntestgrouptestcode=t.ntestgrouptestcode "
							+ " and  ts.nspecsampletypecode=t.nspecsampletypecode and   nallottedspeccode="
							+ ntestgroupspeccode + ") a," + " (select tp.*,ts.*,"
							+ " tf.ntestformulacode,tf.sformulacalculationcode,tf.sformulacalculationdetail,tf.ntransactionstatus,ts.nspecsampletypecode from"
							+ " testgrouptestformula tf,testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp where tp.nparametertypecode="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tf.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + " ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + " tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ " tp.ntestgrouptestparametercode=tf.ntestgrouptestparametercode"
							+ " and tp.ntestgrouptestcode=t.ntestgrouptestcode "
							+ " and  ts.nspecsampletypecode=t.nspecsampletypecode and tf.ntestgrouptestcode=t.ntestgrouptestcode "
							+ " and  nallottedspeccode=" + selectedSpecification.getNallottedspeccode()
							+ ") b where a.ntestparametercode=b.ntestparametercode and a.ncomponentcode=b.ncomponentcode  "
							+ ") c  where c.ntestgrouptestparametercode=d.ntestgrouptestparametercode;";

					final String strQuery8 = "insert into testgrouptestmaterial (ntestgrouptestmaterialcode,ntestgrouptestcode,nmaterialtypecode,nmaterialcatcode,nmaterialcode,sremarks,dmodifieddate,nstatus,nsitecode)"
							+ " select " + inputMap.get("testgrouptestmaterial")
							+ "+RANK() over(order by ntestgrouptestmaterialcode)," + inputMap.get("testgrouptest")
							+ "+RANK() over(order by t.ntestgrouptestcode)" + " ntestgrouptestcode,nmaterialtypecode,"
							+ " nmaterialcatcode,nmaterialcode,sremarks,'" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ,"
							+ objUserInfo.getNmastersitecode()
							+ " from testgrouptestmaterial tm,testgrouptest t,testgroupspecsampletype ts where  t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tm.ntestgrouptestcode=t.ntestgrouptestcode"
							+ " and ts.nspecsampletypecode=t.nspecsampletypecode and ts.nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + "";

					jdbcTemplate.execute(strQuery5 + strQuery6 + strQuery7 + strQuery8);
					//executeBulkDatainSingleInsert(strQuery);

					String query = "select tp.*,ts.ncomponentcode from   testgrouptest t ,testgroupspecsampletype ts"
							+ " ,testgrouptestparameter tp" + "  where   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode   "
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  " + "  and  nallottedspeccode="
							+ ntestgroupspeccode + " order by tp.nparametertypecode asc;";

					List<TestGroupTestParameter> lstTestGroupTestParameter = jdbcTemplate.query(query,new TestGroupTestParameter());

					query = "select tp.*,ts.ncomponentcode from   testgrouptest t  ,testgroupspecsampletype ts"
							+ " ,testgrouptestparameter tp" + "  where   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode   "
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  " + "  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + "  order by tp.nparametertypecode asc;";
					List<TestGroupTestParameter> lstTestGroupTestParameter1 = jdbcTemplate.query(query,new TestGroupTestParameter());


					query = " select tp.ntestgrouptestparametercode,tp.ntestparametercode,COALESCE(NULLIF(tn.sminlod,''),NULL) sminlod,"
							+ "COALESCE(NULLIF(tn.smaxlod,''),NULL) smaxlod, COALESCE(NULLIF(tn.sminb,''),NULL)sminb,COALESCE(NULLIF(tn.smina,''),NULL)smina,"
							+ "COALESCE(NULLIF(tn.smaxa,''),NULL)smaxa,COALESCE(NULLIF(tn.smaxb,''),NULL)smaxb,COALESCE(NULLIF(tn.sminloq,''),NULL)sminloq,"
							+ "COALESCE(NULLIF(tn.smaxloq,''),NULL)smaxloq,tn.sdisregard,tn.sresultvalue,tn.nstatus,tn.ngradecode "
							+ "  from  testgrouptest t,testgroupspecsampletype ts,"
							+ " testgrouptestparameter tp,testgrouptestnumericparameter tn" + "  where   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ " tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
							+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate.query(query,new TestGroupTestNumericParameter());

					query = " select   tp.ntestgrouptestparametercode,tp.ntestparametercode,tn.scharname"
							+ "   from  testgrouptest t,testgroupspecsampletype ts,"
							+ "   testgrouptestparameter tp,testgrouptestcharparameter tn" + "   where    t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
							+ " tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode and"
							+ " tp.nparametertypecode=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate.query(query,new TestGroupTestCharParameter());


					query = "select  tn.ntestgrouptestpredefcode, tp.ntestgrouptestparametercode,"
							+ " tp.ntestparametercode,tn.ngradecode,tn.spredefinedname,tn.spredefinedsynonym,tn.spredefinedcomments,tn.salertmessage,tn.nneedresultentryalert,tn.nneedsubcodedresult,tn.ndefaultstatus,tn.nstatus"
							+ "   from  testgrouptest t,testgroupspecsampletype ts,"
							+ "   testgrouptestparameter tp,testgrouptestpredefparameter tn" + "   where    t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tn.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
							+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate.query(query, new TestGroupTestPredefinedParameter());

					query = "select  tps.ntestgrouptestpredefsubcode,tps.ntestgrouptestpredefcode,tps.ssubcodedresult,tps.nstatus "
							+ "   from  testgrouptest t,testgroupspecsampletype ts,"
							+ "   testgrouptestparameter tp,testgrouptestpredefparameter tn,testgrouptestpredefsubresult tps "
							+ "   where    t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + "  ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + "  tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + "  tn.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + "  tps.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tps.ntestgrouptestpredefcode=tn.ntestgrouptestpredefcode "
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
							+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					List<TestGroupTestPredefinedSubCode> lstTestGroupTestPredefinedSubcode = jdbcTemplate.query(query,new TestGroupTestPredefinedSubCode());

					query = "select tgs.* from   testgrouptest t  ,testgroupspecsampletype ts"
							+ " ,testgrouptestparameter tp,testgrouptestpredefparameter tgs" + "  where   t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tgs.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  tp.ntestgrouptestparametercode=tgs.ntestgrouptestparametercode   "
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode   "
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  " + "  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + "  order by tp.nparametertypecode asc;";
					List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefindParameter1 = jdbcTemplate.query(query, new TestGroupTestPredefinedParameter());


					query = "select  tn.* "
							+ "   from  testgrouptest t,testgroupspecsampletype ts,"
							+ "   testgrouptestparameter tp,testgrouptestclinicalspec tn" + "   where    t.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  tn.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
							+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
							+ selectedSpecification.getNallottedspeccode() + ";";
					List<TestGroupTestClinicalSpec> lstTestGroupTestClinicalSpec = jdbcTemplate.query(query, new TestGroupTestClinicalSpec());

					List<Object> lstquery = new ArrayList<>();
					lstquery.add(lstTestGroupTestParameter);
					lstquery.add(lstTestGroupTestParameter1);
					lstquery.add(lstTestGroupTestNumericParameter);
					lstquery.add(lstTestGroupTestCharParameter);
					lstquery.add(lstTestGroupTestPredefinedParameter);
					lstquery.add(lstTestGroupTestPredefinedSubcode);
					lstquery.add(lstTestGroupTestPredefindParameter1);
					lstquery.add(lstTestGroupTestClinicalSpec);

					List<TestGroupTestParameter> lstnewParameter = (List<TestGroupTestParameter>) lstquery.get(0);
					List<TestGroupTestParameter> lstoldParameter = (List<TestGroupTestParameter>) lstquery.get(1);
					List<TestGroupTestNumericParameter> lstnumeric = (List<TestGroupTestNumericParameter>) lstquery.get(2);
					List<TestGroupTestCharParameter> lstchar = (List<TestGroupTestCharParameter>) lstquery.get(3);
					List<TestGroupTestPredefinedParameter> lstpredef = (List<TestGroupTestPredefinedParameter>) lstquery.get(4);
					List<TestGroupTestPredefinedSubCode> lstpredefSubcode = (List<TestGroupTestPredefinedSubCode>) lstquery.get(5);
					List<TestGroupTestClinicalSpec> lstclinicalspec = (List<TestGroupTestClinicalSpec>) lstquery.get(7);
					
					int ntestgrouptestnumericparametercode = (int) inputMap.get("testgrouptestnumericparameter") + 1;
					int ntestgrouptestcharparametercode = (int) inputMap.get("testgrouptestcharparameter") + 1;
					int ntestgrouptestpredefparametercode = (int) inputMap.get("testgrouptestpredefparameter") + 1;
					int ntestgrouptestclinicspeccode = (int) inputMap.get("testgrouptestclinicalspec") + 1;
					int ntestgrouptestpredefsubcode = (int) inputMap.get("testgrouptestpredefsubresult") + 1;
					String str = "";

					List<String> lstString = new ArrayList<String>();
					
					for (int i = 0; i < lstnewParameter.size(); i++) {
						final int k=i;
						List<TestGroupTestParameter> lstparametercode  = lstoldParameter.stream()
								.filter(x -> x.getNtestparametercode() == lstnewParameter.get(k).getNtestparametercode()
								&& x.getNcomponentcode()==lstnewParameter.get(k).getNcomponentcode()).collect(Collectors.toList());

						int nparametercode = lstparametercode.get(0).getNtestgrouptestparametercode();
						if (lstnewParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.ACTIVE
								.gettransactionstatus()
								&& lstoldParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.ACTIVE
								.gettransactionstatus()) {

							List<TestGroupTestNumericParameter> lstnumstrm = lstnumeric.stream()
									.filter(x -> x.getNtestgrouptestparametercode() == nparametercode)
									.collect(Collectors.toList());
							//	ALPD-5913	Added resultValue for copy spec 500 issue by Vishakh
							String resultValue = (lstnumstrm.get(0).getSresultvalue() == null || lstnumstrm.get(0).getSresultvalue().isEmpty()) ? null 
									: "'"+lstnumstrm.get(0).getSresultvalue()+"'"; // added single quotes by gowtham on 03 july 2025 - ALPDJ21-5
							str = "insert into testgrouptestnumericparameter(ntestgrouptestnumericcode,ntestgrouptestparametercode,"
									+ "sminlod,smaxlod,sminb,smina,smaxa,smaxb,sminloq,smaxloq,sdisregard,sresultvalue,dmodifieddate,nstatus,nsitecode,ngradecode) values("
									+ ntestgrouptestnumericparametercode + ","
									+ lstnewParameter.get(i).getNtestgrouptestparametercode() + ","
									+ lstnumstrm.get(0).getSminlod() + "," + "" + lstnumstrm.get(0).getSmaxlod() + ","
									+ lstnumstrm.get(0).getSminb() + "," + lstnumstrm.get(0).getSmina() + ","
									+ lstnumstrm.get(0).getSmaxa() + "," + lstnumstrm.get(0).getSmaxb() + "," + ""
									+ lstnumstrm.get(0).getSminloq() + "," + lstnumstrm.get(0).getSmaxloq() + ","
									+ lstnumstrm.get(0).getSdisregard() + ",case when "+ resultValue
									+ " is null then null  else '"+ stringUtilityFunction.replaceQuote(lstnumstrm.get(0).getSresultvalue()) + "' end ," + "'"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() +","+lstnumstrm.get(0).getNgradecode() +");";

							lstString.add(str);
							ntestgrouptestnumericparametercode++;




							List<TestGroupTestClinicalSpec> lstclinical = lstclinicalspec.stream()
									.filter(x -> x.getNtestgrouptestparametercode() == nparametercode)
									.collect(Collectors.toList());


							for (int j = 0; j < lstclinical.size(); j++) {
								//ALPD-5054 added by Dhanushya RI,To insert fromage period and toage period                     
								str = "insert into testgrouptestclinicalspec(ntestgrouptestclinicspeccode,ntestgrouptestparametercode,ngendercode,nfromage,ntoage,shigha,shighb,slowa,slowb,sminlod,smaxlod,sminloq ,smaxloq,sdisregard,sresultvalue,nsitecode,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,nstatus,ngradecode,nfromageperiod,ntoageperiod) values("
										+ ntestgrouptestclinicspeccode + ","
										+ lstnewParameter.get(i).getNtestgrouptestparametercode() + "," + ""
										+ lstclinical.get(j).getNgendercode() + ","
										+ lstclinical.get(j).getNfromage() + ","
										+ lstclinical.get(j).getNtoage() + ",N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getShigha()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getShighb()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSlowa()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSlowb()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSminlod()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSmaxlod()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSminloq()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSmaxloq()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSdisregard()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstclinical.get(j).getSresultvalue()) + "',"
										+ objUserInfo.getNmastersitecode() +  ",'"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ objUserInfo.getNtimezonecode()+ ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())+ ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","
										+ lstclinical.get(j).getNgradecode()+","+lstclinical.get(j).getNfromageperiod()+","+lstclinical.get(j).getNtoageperiod()+");";


								lstString.add(str);
								ntestgrouptestclinicspeccode++;
							}

						} else if (lstnewParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.YES.gettransactionstatus()
								&& lstoldParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							List<TestGroupTestCharParameter> lstcharstrm = lstchar.stream().filter(x -> x.getNtestgrouptestparametercode() == nparametercode)
									.collect(Collectors.toList());
//							ALPD-5913	Added charName for copy spec 500 issue by Vishakh
							String charName = (lstcharstrm.get(0).getScharname()== null || lstcharstrm.get(0).getScharname().isEmpty()) ? null 
									: "'"+lstcharstrm.get(0).getScharname()+"'"; // added single quotes by gowtham on 03 july 2025 - ALPDJ21-5
							str = "insert into testgrouptestcharparameter(ntestgrouptestcharcode,ntestgrouptestparametercode,scharname,dmodifieddate,nstatus,nsitecode) values("
									+ ntestgrouptestcharparametercode + ","
									+ lstnewParameter.get(i).getNtestgrouptestparametercode() 
									// ALPD-4515	Added case to check string null or db null to insert by Vishakh -START
									+ ", case when "+ charName+ " is null then null else N'"
									+ stringUtilityFunction.replaceQuote(lstcharstrm.get(0).getScharname()) + "' end," + "'"	
									// END
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");";
							// sb1.append(str);
							lstString.add(str);
							ntestgrouptestcharparametercode++;
							
						} else if (lstnewParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
								&& lstoldParameter.get(i).getNparametertypecode() == Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) {

							List<TestGroupTestPredefinedParameter> lstpredefstrm = lstpredef.stream().filter(x -> x.getNtestgrouptestparametercode() == nparametercode)
									.collect(Collectors.toList());

							for (int j = 0; j < lstpredefstrm.size(); j++) {
								str = "insert into testgrouptestpredefparameter(ntestgrouptestpredefcode,ntestgrouptestparametercode,ngradecode,spredefinedname,spredefinedsynonym,spredefinedcomments,salertmessage,nneedresultentryalert,nneedsubcodedresult,ndefaultstatus,dmodifieddate,nstatus,nsitecode) "
										+ "values("
										+ ntestgrouptestpredefparametercode + ","
										+ lstnewParameter.get(i).getNtestgrouptestparametercode() + "," + ""
										+ lstpredefstrm.get(j).getNgradecode() + ",N'"
										+ stringUtilityFunction.replaceQuote(lstpredefstrm.get(j).getSpredefinedname()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstpredefstrm.get(j).getSpredefinedsynonym()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstpredefstrm.get(j).getSpredefinedcomments()) + "',N'"
										+ stringUtilityFunction.replaceQuote(lstpredefstrm.get(j).getSalertmessage()) + "',"
										+ lstpredefstrm.get(j).getNneedresultentryalert() + ","
										+ lstpredefstrm.get(j).getNneedsubcodedresult() + ","
										+ lstpredefstrm.get(j).getNdefaultstatus() + "," + "" + "'"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objUserInfo.getNmastersitecode() + ");";
								lstString.add(str);


								int testgrouptestpredefcode = lstpredefstrm.get(j).getNtestgrouptestpredefcode();
								List<TestGroupTestPredefinedSubCode> lstpredefSubcodestrm = lstpredefSubcode.stream()
										.filter(x -> x.getNtestgrouptestpredefcode() == testgrouptestpredefcode)
										.collect(Collectors.toList());

								for (int b = 0; b < lstpredefSubcodestrm.size(); b++) {

									String str1 = "insert into  testgrouptestpredefsubresult (ntestgrouptestpredefsubcode,ntestgrouptestpredefcode,ssubcodedresult,dmodifieddate,nsitecode,nstatus) values("
											+ ntestgrouptestpredefsubcode + "," + ntestgrouptestpredefparametercode + ",N'"
											+ stringUtilityFunction.replaceQuote(lstpredefSubcodestrm.get(b).getSsubcodedresult()) + "'," + "'"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode()
											+ "," + +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

									ntestgrouptestpredefsubcode++;
									lstString.add(str1);
								}

								ntestgrouptestpredefparametercode++;

							}
						}
					}

					String[] strFinals = lstString.toArray(new String[0]);
					projectDAOSupport.executeBulkDatainSingleInsert(strFinals);

					List<?> lstAuditRecord = getCopyAuditRecord(ntestgroupspeccode, ntemplatemanipulationcode);
					final List<TestGroupSpecification> listTestGroupSpecification = (List<TestGroupSpecification>) lstAuditRecord
							.get(0);

					List<Object> lstAfterSave = new ArrayList();
					lstAfterSave.add(listTestGroupSpecification);

					auditUtilityFunction.fnInsertListAuditAction(lstAfterSave, 1, null,Arrays.asList("IDS_COPYSPECIFICATION", "IDS_ADDTESTGROUPSPECTSAMPLETYPE",
									"IDS_ADDTESTGROUPSPECFILE", "IDS_ADDTESTGROUPTEST", "IDS_ADDTESTGROUPTESTFILE",
									"IDS_ADDTESTGROUPTEST", "IDS_ADDTESTGROUPTESTPARAMETER",
									"IDS_ADDTESTGROUPTESTNUMERICPARAMETER", "IDS_ADDTESTGROUPTESTCHARACTERPARAMETER",
									"IDS_ADDTESTGROUPTESTPREDEFINED", "IDS_ADDTESTGROUPTESTMATERIAL"),
							objUserInfo);
					return new ResponseEntity<Object>(getTestGroupSpecification(objUserInfo, objTree, false, null).getBody(), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_EXPIRYDATEMUSTMORETHANCURRENTDATE",objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}

			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public Map<String, Object> getcopySpecificationSeqno(Map<String, Object> objmap) throws Exception {
		Map<String, Object> mapSeq = new HashMap<String, Object>();

		final String Query = " lock  table locktestgroupcopy " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final TestGroupSpecification selectedSpecification = objMapper.convertValue(objmap.get("selectedspecification"),
				TestGroupSpecification.class);
		final TestGroupSpecification testSpecification = objMapper.convertValue(objmap.get("testgroupspecification"),
				TestGroupSpecification.class);
		final UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), UserInfo.class);

		String query = "select * from testgroupspecification where nallottedspeccode="
				+ selectedSpecification.getNallottedspeccode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		List<TestGroupSpecification> lsttestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate.query(query, new TestGroupSpecification());
		
		if (!lsttestGroupSpecification.isEmpty()) {

			String sQuery = "LOCK TABLE  locktestgroup ";
			jdbcTemplate.execute(sQuery);

			sQuery = "select * from seqnotestgroupmanagement where stablename in ('testgroupspecification','testgroupspecsampletype','testgroupspecfile','testgrouptest','testgrouptestfile','testgrouptestparameter','testgrouptestformula','testgrouptestcharparameter','testgrouptestnumericparameter','testgrouptestpredefparameter','testgrouptestmaterial','testgrouptestpredefsubresult','testgrouptestclinicalspec')"
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +";";
			List<SeqNoTestGroupmanagement> lstSeqNoTestMgmt = (List<SeqNoTestGroupmanagement>) jdbcTemplate.query(sQuery, new SeqNoTestGroupmanagement());

			Map<String, Integer> returnMap = lstSeqNoTestMgmt.stream().collect(Collectors.toMap(
					SeqNoTestGroupmanagement::getStablename, SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

			sQuery = "select count(nspecsampletypecode) nspecsampletypecode from testgroupspecsampletype where nallottedspeccode= "
					+ selectedSpecification.getNallottedspeccode() + " and nsitecode  = "+userInfo.getNmastersitecode()+""
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupSpecSampleType> lstTestGroupSpecSampleType = jdbcTemplate.query(sQuery,new TestGroupSpecSampleType());

			sQuery = "select count(nspecfilecode) nspecfilecode from testgroupspecfile where nallottedspeccode= "
					+ selectedSpecification.getNallottedspeccode() + " and nsitecode  = "+userInfo.getNmastersitecode()+" "
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupSpecFile> lstTestGroupSpecFile = jdbcTemplate.query(sQuery, new TestGroupSpecFile());

			sQuery = "select count(ntestgrouptestcode) ntestgrouptestcode from testgrouptest t,testgroupspecsampletype ts"
					+ " where t.nspecsampletypecode=ts.nspecsampletypecode and t.nsitecode = ts.nsitecode"
					+ " and ts.nsitecode  = "+userInfo.getNmastersitecode()+" "
					+ " and ts.nallottedspeccode= "+ selectedSpecification.getNallottedspeccode() + " "
					+ " and t.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ "  and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTest> lstTestGroupTest = jdbcTemplate.query(sQuery, new TestGroupTest());

			sQuery = "select count(f.ntestgroupfilecode) ntestgroupfilecode  "
					+ "  from testgrouptest t,testgroupspecsampletype ts,testgrouptestfile f  where"
					+ " ts.nspecsampletypecode=t.nspecsampletypecode and t.ntestgrouptestcode=f.ntestgrouptestcode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = f.nsitecode and f.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and  nallottedspeccode=" + selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and f.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestFile> lstTestGroupTestFile = jdbcTemplate.query(sQuery, new TestGroupTestFile());

			sQuery = "select count(tp.ntestgrouptestparametercode) ntestgrouptestparametercode  "
					+ "  from  testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp where tp.ntestgrouptestcode=t.ntestgrouptestcode "
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestParameter> lstTestGroupTestParameter = jdbcTemplate.query(sQuery,
					new TestGroupTestParameter());

			sQuery = "select count(tf.ntestgrouptestformulacode) ntestgrouptestformulacode  "
					+ "  from testgrouptestformula tf,testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp where tp.ntestgrouptestparametercode=tf.ntestgrouptestparametercode and tp.ntestgrouptestcode=t.ntestgrouptestcode "
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode and tf.ntestgrouptestcode=t.ntestgrouptestcode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tf.nsitecode and tf.nsitecode = tp.nsitecode and tf.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and  nallottedspeccode=" + selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sQuery,
					new TestGroupTestFormula());

			sQuery = "select count(tn.ntestgrouptestnumericcode) ntestgrouptestnumericcode  "
					+ " from  testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp,testgrouptestnumericparameter tn"
					+ " where  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = tn.nsitecode "
					+ " and tn.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate.query(sQuery,
					new TestGroupTestNumericParameter());


			sQuery = " select count(tn.ntestgrouptestcharcode) ntestgrouptestcharcode  " + "  from  "
					+ " testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp,testgrouptestcharparameter tn"
					+ " where  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = tn.nsitecode and tn.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode" + "  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate.query(sQuery,
					new TestGroupTestCharParameter());


			sQuery = " select count(tn.ntestgrouptestpredefcode) ntestgrouptestpredefcode from  "
					+ " testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp,testgrouptestpredefparameter tn"
					+ " where  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = tn.nsitecode and tn.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate.query(sQuery,
					new TestGroupTestPredefinedParameter());

			sQuery = "select count(tm.ntestgrouptestmaterialcode) ntestgrouptestmaterialcode  "
					+ "  from testgrouptestmaterial tm,testgroupspecsampletype ts,testgrouptest t  where"
					+ " ts.nspecsampletypecode=t.nspecsampletypecode and t.ntestgrouptestcode=tm.ntestgrouptestcode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tm.nsitecode and tm.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and  nallottedspeccode=" + selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  and tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestMaterial> lstTestGroupTestMaterial = jdbcTemplate.query(sQuery,
					new TestGroupTestMaterial());

			sQuery = " select count(tps.ntestgrouptestpredefsubcode) ntestgrouptestpredefsubcode  " + " from  "
					+ " testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp,testgrouptestpredefparameter tn,testgrouptestpredefsubresult tps"
					+ " where tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = tn.nsitecode and tn.nsitecode = tps.nsitecode"
					+ " and tn.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode and tps.ntestgrouptestpredefcode=tn.ntestgrouptestpredefcode  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tn.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tps.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestPredefinedSubCode> lstTestGroupTestPredefinedSubCode = jdbcTemplate.query(sQuery,
					new TestGroupTestPredefinedSubCode());

			sQuery = " select count(tn.ntestgrouptestclinicspeccode) ntestgrouptestclinicspeccode  " + " from  "
					+ " testgrouptest t,testgroupspecsampletype ts,testgrouptestparameter tp,testgrouptestclinicalspec tn"
					+ " where  tp.ntestgrouptestcode=t.ntestgrouptestcode  and tn.ntestgrouptestparametercode=tp.ntestgrouptestparametercode"
					+ " and t.nsitecode = ts.nsitecode and ts.nsitecode = tp.nsitecode and tp.nsitecode = tn.nsitecode and tn.nsitecode = "+userInfo.getNmastersitecode() +" "
					+ " and tp.nparametertypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and  ts.nspecsampletypecode=t.nspecsampletypecode  and  nallottedspeccode="
					+ selectedSpecification.getNallottedspeccode() + " and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and  tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tn.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			List<TestGroupTestClinicalSpec> lstTestGroupTestclinicalspec = jdbcTemplate.query(sQuery,
					new TestGroupTestClinicalSpec());

			List<Object> listSeqNo = new ArrayList<Object>();
			listSeqNo.add(lstTestGroupSpecSampleType);
			listSeqNo.add(lstTestGroupSpecFile);
			listSeqNo.add(lstTestGroupTest);
			listSeqNo.add(lstTestGroupTestFile);
			listSeqNo.add(lstTestGroupTestParameter);
			listSeqNo.add(lstTestGroupTestFormula);
			listSeqNo.add(lstTestGroupTestNumericParameter);
			listSeqNo.add(lstTestGroupTestCharParameter);
			listSeqNo.add(lstTestGroupTestPredefinedParameter);
			listSeqNo.add(lstTestGroupTestMaterial);
			listSeqNo.add(lstTestGroupTestPredefinedSubCode);
			listSeqNo.add(lstTestGroupTestclinicalspec);


			String sUpdateQuery = "";
			final List<TestGroupSpecSampleType> listTestGroupSpecSampleType = ((List<TestGroupSpecSampleType>) listSeqNo
					.get(0));
			final List<TestGroupSpecFile> listTestGroupSpecFile = (List<TestGroupSpecFile>) listSeqNo.get(1);
			final List<TestGroupTest> listTestGroupTest = (List<TestGroupTest>) listSeqNo.get(2);
			final List<TestGroupTestFile> listTestGroupTestFile = (List<TestGroupTestFile>) listSeqNo.get(3);
			final List<TestGroupTestParameter> listTestGroupTestParameter = (List<TestGroupTestParameter>) listSeqNo
					.get(4);
			final List<TestGroupTestFormula> listTestGroupTestFormula = (List<TestGroupTestFormula>) listSeqNo.get(5);
			final List<TestGroupTestNumericParameter> listTestGroupTestNumericParameter = (List<TestGroupTestNumericParameter>) listSeqNo
					.get(6);
			final List<TestGroupTestCharParameter> listTestGroupTestCharParameter = (List<TestGroupTestCharParameter>) listSeqNo
					.get(7);
			final List<TestGroupTestPredefinedParameter> listTestGroupTestPredefinedParameter = (List<TestGroupTestPredefinedParameter>) listSeqNo
					.get(8);
			final List<TestGroupTestMaterial> listTestGroupTestMaterial = (List<TestGroupTestMaterial>) listSeqNo
					.get(9);
			final List<TestGroupTestPredefinedSubCode> listTestGroupTestPredefinedSubCode = (List<TestGroupTestPredefinedSubCode>) listSeqNo
					.get(10);
			final List<TestGroupTestClinicalSpec> listTestGroupTestClinicalSpec = (List<TestGroupTestClinicalSpec>) listSeqNo
					.get(11);

			sUpdateQuery = "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgroupspecification") + 1) + " where stablename=N'testgroupspecification'"
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			if (!listTestGroupSpecSampleType.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgroupspecsampletype")
								+ listTestGroupSpecSampleType.get(0).getNspecsampletypecode())+ " where stablename=N'testgroupspecsampletype' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupSpecFile.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgroupspecfile") + listTestGroupSpecFile.get(0).getNspecfilecode())
						+ " where stablename=N'testgroupspecfile' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTest.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptest") + listTestGroupTest.get(0).getNtestgrouptestcode())
						+ " where stablename=N'testgrouptest' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestFile.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "
						+ (returnMap.get("testgrouptestfile") + listTestGroupTestFile.get(0).getNtestgroupfilecode())
						+ " where stablename=N'testgrouptestfile' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestParameter.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestparameter")
								+ listTestGroupTestParameter.get(0).getNtestgrouptestparametercode())+""
								+ "  where stablename=N'testgrouptestparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestFormula.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestformula")
								+ listTestGroupTestFormula.get(0).getNtestgrouptestformulacode()) + " where stablename=N'testgrouptestformula' "
										+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestNumericParameter.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestnumericparameter")
								+ listTestGroupTestNumericParameter.get(0).getNtestgrouptestnumericcode())
						+ " where stablename=N'testgrouptestnumericparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestCharParameter.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestcharparameter")
								+ listTestGroupTestCharParameter.get(0).getNtestgrouptestcharcode())
						+ " where stablename=N'testgrouptestcharparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestPredefinedParameter.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestpredefparameter")
								+ listTestGroupTestPredefinedParameter.get(0).getNtestgrouptestpredefcode())
						+ " where stablename=N'testgrouptestpredefparameter' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}
			if (!listTestGroupTestMaterial.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestmaterial")
								+ listTestGroupTestMaterial.get(0).getNtestgrouptestmaterialcode())
						+ " where stablename=N'testgrouptestmaterial' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}

			if (!listTestGroupTestPredefinedSubCode.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestpredefsubresult")
								+ listTestGroupTestPredefinedSubCode.get(0).getNtestgrouptestpredefsubcode())
						+ " where stablename=N'testgrouptestpredefsubresult' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}

			if (!listTestGroupTestClinicalSpec.isEmpty()) {
				sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+ (returnMap.get("testgrouptestclinicalspec")
								+ listTestGroupTestClinicalSpec.get(0).getNtestgrouptestclinicspeccode())
						+ " where stablename=N'testgrouptestclinicalspec' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			}


			jdbcTemplate.execute(sUpdateQuery);
			mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			mapSeq.putAll(returnMap);
			mapSeq.put("ntestGroupTestcount", lstTestGroupTest.get(0).getNtestgrouptestcode());
			mapSeq.put("testSpecification", testSpecification);
		} else {
			mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()));
		}
		return mapSeq;
	}

	public List<?> getCopyAuditRecord(int nallocatedspeccode, int ntemplatemanipulationcode) throws Exception {

		String sQuery = "";

		sQuery = "select *  from testgroupspecification where nallottedspeccode= " + nallocatedspeccode + " "
				+ " and ntemplatemanipulationcode = "+ ntemplatemanipulationcode+ " "
				+ "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<TestGroupSpecification> lstTestGroupSpecification = jdbcTemplate.query(sQuery,new TestGroupSpecification());

		List<Object> listSeqNo = new ArrayList<>();
		listSeqNo.add(lstTestGroupSpecification);

		return listSeqNo;

	}
	// Copy Specification End


	@Override
	public ResponseEntity<Object> getTestGroupSampleType(TestGroupSpecification objTestGroupSpec, UserInfo objUserInfo)
			throws Exception {
		
		List<TestGroupSpecSampleType> lstTGSST = testGroupCommonFunction.getTestGroupSampleType(objTestGroupSpec);
		return new ResponseEntity<Object>(lstTGSST, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> validateTestGroupComplete(final int nallottedSpecCode, final UserInfo userInfo)
			throws Exception {
		final String queryString = "select tgt.*, tm.stestname from testmaster tm, testgrouptest tgt, "
				+ " testgroupspecsampletype tgsst, testgroupspecification tgs  where  "
				+ " tgs.nallottedspeccode = tgsst.nallottedspeccode "
				+ " and tgsst.nspecsampletypecode = tgt.nspecsampletypecode and tgt.ntestcode=tm.ntestcode "
				+ " and tgs.nstatus = tgsst.nstatus and tgsst.nstatus=tgt.nstatus  and tgt.nstatus=tm.nstatus "
				+ " and tm.nsitecode = tgt.nsitecode and tgsst.nsitecode=tgt.nsitecode and tgt.nsitecode=tgs.nsitecode"
				+ " and tgs.nsitecode = tm.nsitecode and tm.nsitecode = "+userInfo.getNmastersitecode() +" "
				+ " and tm.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and tm.ntransactionstatus="+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()+" "
				+ " and tgs.nallottedspeccode = "+ nallottedSpecCode + ";";
		
		final List<TestGroupTest> inActiveTestList = (List<TestGroupTest>) jdbcTemplate.query(queryString,new TestGroupTest());
		
		if (inActiveTestList.isEmpty()) {
			// Inactive Test not exists in sample
			return new ResponseEntity<>(inActiveTestList, HttpStatus.OK);
		} else {
			// Inactive Test exists in sample
			return new ResponseEntity<>(inActiveTestList, HttpStatus.OK);
		}

	}
	//To get spec details for copy action --ALPD-4099 ,work done by Dhanushya R I
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSpecDetailsForCopy(final Map<String, Object>inputMap,final UserInfo objUserInfo)throws Exception {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final int nSampleTypeCode = (int) inputMap.get("nsampletypecode");
		final int nproductCatCode = (int) inputMap.get("nproductcatcode");
		final int nproductCode = (int) inputMap.get("nproductcode");
		final int nprojectCode = (int) inputMap.get("nprojectmastercode");
		final int nTreeVersionTempCode = (int) inputMap.get("ntreeversiontempcode");

		//ALPD-5904--Added by Vignesh R(25-06-2025)-->Test Group screen -> System allowing to copy spec to retired Template version.
				boolean isCopySpec=false;
				 if(inputMap.containsKey("isCopySpec")) {
					 isCopySpec=true;
				 }
				 
		outputMap.putAll(getTreedata(objUserInfo, nSampleTypeCode, nproductCatCode, nproductCode, 
				nTreeVersionTempCode,false, "", 0, nprojectCode,isCopySpec));

		final int ntemplatemanipulationcode = (int) outputMap.get("primarykey");
		List<TreeTemplateManipulation> lstTreeTemplateManipulation = (List<TreeTemplateManipulation>) outputMap
				.get("TreeTemplateManipulation");
		TreeTemplateManipulation selectedTreeTemplateManipulation = null;
		if (!lstTreeTemplateManipulation.isEmpty()) {
			selectedTreeTemplateManipulation = new TreeTemplateManipulation();
			selectedTreeTemplateManipulation = lstTreeTemplateManipulation.stream()
					.filter(x -> x.getNtemplatemanipulationcode() == ntemplatemanipulationcode)
					.collect(Collectors.toList()).get(0);
		} else {
			outputMap.putAll(testGroupCommonFunction.getEmptyMap());
		}

		outputMap.put("selectedNode", selectedTreeTemplateManipulation);
		return new ResponseEntity<Object>(
				outputMap, HttpStatus.OK);
	}

}