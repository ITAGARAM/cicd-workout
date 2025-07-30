package com.agaramtech.qualis.testmanagement.service.testmaster;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.basemaster.model.UnitConversion;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupmanagement;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testmanagement.model.InterfaceType;
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.ReportInfoTest;
import com.agaramtech.qualis.testmanagement.model.SeqNoTestManagement;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.model.TestContainerType;
import com.agaramtech.qualis.testmanagement.model.TestFile;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestInstrumentCategory;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestMethod;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.TestSection;
import com.agaramtech.qualis.testmanagement.service.testcategory.TestCategoryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class TestMasterDAOImpl implements TestMasterDAO {
	
	 private static final Logger LOGGER = LoggerFactory.getLogger(TestMasterDAOImpl.class);
	  
	  private final StringUtilityFunction stringUtilityFunction; 
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private ValidatorDel validatorDel;
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final ProjectDAOSupport projectDAOSupport; 
	  private final DateTimeUtilityFunction dateUtilityFunction; 
	  private final AuditUtilityFunction auditUtilityFunction;
	  private final TestCategoryDAO testCategoryDAO;
	  private final TestMasterCommonFunction testMasterCommonFunction;
	  private final FTPUtilityFunction ftpUtilityFunction;
	  
	  
		@Override
		public ResponseEntity<Object> getTestMaster(final UserInfo objUserInfo) throws Exception {
			final Map<String,Object> map = new LinkedHashMap<String,Object>();
			final List<TestCategory> lstTestCategory = (List<TestCategory>) testCategoryDAO.getTestCategory(objUserInfo).getBody();
			map.put("filterTestCategory", testCategoryDAO.getTestCategory(objUserInfo));
			LOGGER.info("UserInfo:"+ objUserInfo);	
			map.put("filterTestCategory", lstTestCategory);
			if(lstTestCategory != null && lstTestCategory.size() > 0) {
				int ntestcategorycode = 0;
				final List<TestCategory> defaultCategory = lstTestCategory.stream().
						filter(defTest->defTest.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()).collect(Collectors.toList());
				if(defaultCategory.isEmpty()) {
					ntestcategorycode = lstTestCategory.get(0).getNtestcategorycode();
					map.put("SelectedTestCat", lstTestCategory.get(0));
				} else {
					ntestcategorycode = defaultCategory.get(0).getNtestcategorycode();
					map.put("SelectedTestCat", defaultCategory.get(0));
				}
				final List<TestMaster> lstTestMaster = testMasterCommonFunction.getTestMaster(ntestcategorycode, objUserInfo.getNmastersitecode(), 0, objUserInfo);
				map.put("TestMaster", lstTestMaster);
				if(lstTestMaster.isEmpty()) {
					map.put("TestParameter", Collections.emptyList());
				} else {
					final TestMaster selectedTest = lstTestMaster.get(lstTestMaster.size()-1);
					final int ntestcode = selectedTest.getNtestcode();
					final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(ntestcode, 0,objUserInfo);
					if(!lstTestParameter.isEmpty()) {
						map.putAll(testMasterCommonFunction.getParameterDetailsById(lstTestParameter.get(lstTestParameter.size()-1), objUserInfo));
					}
					map.put("SelectedTest",  selectedTest);
					map.put("TestParameter", lstTestParameter);
					map.putAll(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo));
					map.putAll(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo));
					map.putAll(testMasterCommonFunction.getTestFile(ntestcode, objUserInfo));
					map.putAll(testMasterCommonFunction.getTestInstrumentCategory(ntestcode, objUserInfo));
					map.putAll(testMasterCommonFunction.getTestpackage(ntestcode, objUserInfo));
					map.putAll((Map<String,Object>) testMasterCommonFunction.getTestContainterType(ntestcode, objUserInfo).getBody());
				}
			}
			else{
				map.put("SelectedTestCat", null);
			}
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		}
		
		/**
		 * This method declaration is used to retrive a active test by test category and it's details of test parameter, test section, test method, test instrument category,
		 * test file, test formula, test parameter numeric, test predefined parameter
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity  object holding response status and list of active test and it's details
		 * @throws Exception 
		 */
		public TestCategory getTestCatBytestcat(int ntestCategoryCode) throws Exception {
			final String query="select * from testcategory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and ntestcategorycode="+ntestCategoryCode;
			final TestCategory testCategory = (TestCategory) jdbcUtilityFunction.queryForObject(query, TestCategory.class,jdbcTemplate);
			return testCategory;
		}
		
		@Override
		public ResponseEntity<Object> getTestMasterByCategory(final int ntestCategoryCode, final UserInfo objUserInfo) throws Exception {
			final Map<String,Object> map = new LinkedHashMap<String,Object>();
			final TestCategory lstTestCat=getTestCatBytestcat(ntestCategoryCode);
		
			if(lstTestCat!=null) {
				map.put("SelectedTestCat", lstTestCat);
			}else {
				map.put("SelectedTestCat", null);
			}
			final List<TestMaster> lstTestMaster = testMasterCommonFunction.getTestMaster(ntestCategoryCode, objUserInfo.getNmastersitecode(), 0, objUserInfo);
			map.put("TestMaster", lstTestMaster);
			if(!lstTestMaster.isEmpty()) {
				final TestMaster selectedTest = lstTestMaster.get(lstTestMaster.size()-1);
				final int ntestcode = selectedTest.getNtestcode();
				final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(ntestcode, 0,objUserInfo);
				if(!lstTestParameter.isEmpty()) {
					map.putAll(testMasterCommonFunction.getParameterDetailsById(lstTestParameter.get(lstTestParameter.size()-1), objUserInfo));
				}
				map.put("SelectedTest", selectedTest);
				map.put("TestParameter", lstTestParameter);
				map.putAll(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestFile(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestInstrumentCategory(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestpackage(ntestcode, objUserInfo));
				map.putAll((Map<String,Object>) testMasterCommonFunction.getTestContainterType(ntestcode, objUserInfo).getBody());

			}
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		}

		/**
		 * This method definition is used to retrive a test details of test section, test method, test instrument category and test file
		 * If nFlag = 1, to retrive a active test section
		 * If nFlag = 2, to retrive a active test method
		 * If nFlag = 3, to retrive a active test instrument category
		 * If nFlag = 4, to retrive a active test file
		 * If nFlag = 3, to retrive a active test parameter and it's details (test formula, test parameter numeric, test predefined parameter)
		 * @param nFlag [int] hold the value for case no
		 * @param ntestcode [int] holds the primary key of testmaster object
		 * @param ntestparametercode [int] holds the primary key of testparameter object
		 * @return response entity object holding response status and list of active test details
		 */
		@Override
		public ResponseEntity<Object> getOtherTestDetails(final int nFlag, final int ntestcode, final int ntestparametercode, final UserInfo objUserInfo,final int nclinicaltyperequired) throws Exception {
			Map<String,Object> map = new HashMap<String,Object>();
			switch (nFlag) {
				case 1: //Test Section
					map.putAll(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo));
					break;
		
				case 2: //Test Method
					map.putAll(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo));
					break;
					
				case 3: //Test Instrument Category
					map.putAll(testMasterCommonFunction.getTestInstrumentCategory(ntestcode, objUserInfo));
					break;
					
				case 4: //Test Attachment
					map.putAll(testMasterCommonFunction.getTestFile(ntestcode, objUserInfo));
					break;
				
				case 5: //Test Parameter Details
					final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(ntestcode, ntestparametercode,objUserInfo);
					if(!lstTestParameter.isEmpty()) {
						int nparametertypecode = lstTestParameter.get(0).getNparametertypecode();
						map.put("selectedParameter", testMasterCommonFunction.convertSelectedParameter(lstTestParameter.get(0), objUserInfo));
						if(nparametertypecode == Enumeration.ParameterType.NUMERIC.getparametertype()) {
							if(nclinicaltyperequired==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								map.put("TestMasterClinicalSpec", testMasterCommonFunction.getTestParameterClinialSpec(ntestparametercode,objUserInfo));
							}else {
								map.put("TestParameterNumeric", testMasterCommonFunction.getTestParameterNumeric(ntestparametercode,objUserInfo));
							}
							map.putAll(testMasterCommonFunction.getTestParameterFormula(ntestparametercode,objUserInfo));
						} else if(nparametertypecode == Enumeration.ParameterType.PREDEFINED.getparametertype()) {
							map.putAll(testMasterCommonFunction.getTestPredefinedParameter(ntestparametercode, objUserInfo));
						}
					}else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_ALREADYPARAMETERDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
					break;
				
				default:
					break;
			}
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		}

		/**
		 * This method definition is used to retrive a active test data based on test category and it's used in edqm safety marker
		 * @param siteCode [int] holds the mastersitecode value from the loggedin userinfo object
		 * @param ntestcategorycode [int] holds the primarykey of testcategory object
		 * @return response entity holds the list of test category
		 */
		@Override
		public ResponseEntity<Object> getTestMasterBasedOnTestCategory(final int siteCode, final int ntestcategorycode) throws Exception {
			final String strQuery = " select ntestcode,stestname from testmaster where nsitecode = " + siteCode + " and ntestcategorycode = " + ntestcategorycode + " "		       
	        + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	        return new ResponseEntity<>(jdbcTemplate.query(strQuery, new TestMaster()),HttpStatus.OK);
	        
		}


	@Override
	public ResponseEntity<Object> getAddTest(final UserInfo objUserInfo, final Integer ntestCode) throws Exception {
		String sQuery ="";
			   sQuery = " select spredefinedname from TestPredefinedParameter where "
					+ " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ntestpredefinedcode>0 group by spredefinedname;";
			   List<TestPredefinedParameter> lstTestPredefinedParameter = jdbcTemplate.query(sQuery, new TestPredefinedParameter());

			   sQuery = " select sparametername  from testparameter where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by sparametername;";
			   List<TestParameter> lstTestParameter = jdbcTemplate.query(sQuery, new TestParameter());
			   final Map<String, Object> mapList = new HashMap<String, Object>();
			   mapList.put("TestPredefinedParameter", lstTestPredefinedParameter);
			   mapList.put("TestParameter", lstTestParameter);
			   return new ResponseEntity<Object>(mapList, HttpStatus.OK);
	}
		
		
		@Override
		public ResponseEntity<Object> getActiveTestById(final UserInfo objUserInfo, final int ntestcode) throws Exception {

			final String sQuery = "select t.nchecklistversioncode,t.ntestcode,tc.stestcategoryname,t.sdescription,t.ntestcategorycode,"
					+"t.stestname,t.stestsynonym,t.sshortname,t.ncost,t.ntransactionstatus,t.ntrainingneed,"
					+" t.naccredited, tc.stestcategoryname, cl.schecklistname,t.stestplatform,t.ntat,coalesce(p.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"',p.jsondata->'speriodname'->>'en-US') as statperiodname,p.nperiodcode as ntatperiodcode,t.ninterfacetypecode, "
					+"ift.jsondata->'sinterfacetypename'->>'en-US' as sinterfacetypename "
					+" from testmaster t,testcategory tc, checklist cl, checklistversion cv,period p,interfacetype ift   "
					+" where t.ntestcategorycode = tc.ntestcategorycode and cl.nchecklistcode = cv.nchecklistcode "
					+" and cv.nchecklistversioncode = t.nchecklistversioncode and p.nperiodcode=t.ntatperiodcode and ift.ninterfacetypecode=t.ninterfacetypecode "
					+" and cl.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and cv.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and tc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and t.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and p.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and t.ntestcode = "+ntestcode;
			
			final TestMaster objTestMaster = (TestMaster) jdbcUtilityFunction.queryForObject(sQuery, TestMaster.class, jdbcTemplate);
			if(objTestMaster!=null) {
				return new ResponseEntity<Object>(objTestMaster, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		
		/**
		 * This method declaration is used to retrive the list of parameter, and its parameter details (testprefedefined parameter, test parameter numer)
		 * @param ntestCode [int] holds the primary key(ntestcode) of testmaster
		 * @return response entity object holding response status and list of active parameter and it's details
		 */
		@SuppressWarnings("unchecked")
		@Override
		public ResponseEntity<Object> getTestById(final int ntestCode, final UserInfo objUserInfo) throws Exception {
			final Map<String,Object> map = new HashMap<String,Object>();
			final List<TestMaster> lstTestMaster = testMasterCommonFunction.getTestMaster(0, -1, ntestCode, objUserInfo);
			if(!lstTestMaster.isEmpty()) {
				map.put("SelectedTest", lstTestMaster.get(0));
				final int ntestcode = lstTestMaster.get(0).getNtestcode();
				final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(ntestcode, 0,objUserInfo);
				map.put("TestParameter", lstTestParameter);
				if(!lstTestParameter.isEmpty()) {
					map.put("selectedParameter", testMasterCommonFunction.convertSelectedParameter(lstTestParameter.get(lstTestParameter.size()-1), objUserInfo));
					final int nparametertypecode = lstTestParameter.get(lstTestParameter.size()-1).getNparametertypecode();
					final int ntestparametercode = lstTestParameter.get(lstTestParameter.size()-1).getNtestparametercode();
					map.put("TestParameter", lstTestParameter);
					if(nparametertypecode == Enumeration.ParameterType.NUMERIC.getparametertype()) {
						map.put("TestParameterNumeric", testMasterCommonFunction.getTestParameterNumeric(ntestparametercode,objUserInfo));
						map.put("TestMasterClinicalSpec", testMasterCommonFunction.getTestParameterClinialSpec(ntestparametercode,objUserInfo));
						map.putAll(testMasterCommonFunction.getTestParameterFormula(ntestparametercode,objUserInfo));
					} else if(nparametertypecode == Enumeration.ParameterType.PREDEFINED.getparametertype()) {
						map.putAll(testMasterCommonFunction.getTestPredefinedParameter(ntestparametercode, objUserInfo));
					}
				}
				map.put("TestParameter", lstTestParameter);
				map.putAll(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestFile(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestInstrumentCategory(ntestcode, objUserInfo));
				map.putAll(testMasterCommonFunction.getTestpackage(ntestcode, objUserInfo));
				map.putAll((Map<String,Object>) testMasterCommonFunction.getTestContainterType(ntestcode, objUserInfo).getBody());
				return new ResponseEntity<Object>(map, HttpStatus.OK);

			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			
		}
		
		/**
		 * This method definition is used to create a new test
		 * If user entered testname is available in for this selected test category then returns already exists message.
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @param objTestParameter [TestParameter] holds the details of testparameter
		 * @param objSection [TestSection] holds the details of testsection
		 * @param objMethod [TestMethod] holds the details of testmethod
		 * @param objTestInstCat [TestInstrumentCategory] holds the details of test instrumentcategory
		 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
		 */
		@Override
		public ResponseEntity<Object> createTestMaster(final TestMaster objTestMaster, final UserInfo objUserInfo, final TestParameter objTestParameter, 
				final TestSection objSection, final TestMethod objMethod, final TestInstrumentCategory objTestInstCat,TestPackageTest objTestpackage,final int isQualisLite) throws Exception {
			
			final String sTableLockQuery = " lock  table locktestmaster "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sTableLockQuery);
	
		    
			final TestMaster objTestByName = testMasterCommonFunction.getTestByName(objTestMaster, objUserInfo);
			if(objTestByName == null) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				final TestCategory activeCategory = getTestCatBytestcat(objTestMaster.getNtestcategorycode());
				if(activeCategory != null) {
				//Insert Test
				String seqtestquery ="select nsequenceno from SeqNoTestManagement where stablename='testmaster'";
				int nseqtestcount = (int) jdbcUtilityFunction.queryForObject(seqtestquery, Integer.class, jdbcTemplate);
				
				nseqtestcount++;
				
				String inserttestquery =" Insert into testmaster(ntestcode,ntestcategorycode,nchecklistversioncode,naccredited,stestname,stestsynonym,sshortname, "
							+ " sdescription,ncost,stestplatform,ntat,ntatperiodcode,ninterfacetypecode,dmodifieddate,"
						   +" ntransactionstatus,ntrainingneed,nsitecode,nstatus)"
						   + " values ("+nseqtestcount+","+objTestMaster.getNtestcategorycode()
						   +","+objTestMaster.getNchecklistversioncode()+","+objTestMaster.getNaccredited()
						   +", N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestname())+"',"
						   + " N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestsynonym())
						   + "', N'"+stringUtilityFunction.replaceQuote(objTestMaster.getSshortname())+ "'"
						   + ",N'"+stringUtilityFunction.replaceQuote(objTestMaster.getSdescription())+"',"
						   +objTestMaster.getNcost()
						   +",N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestplatform())+"',"
						   +objTestMaster.getNtat()+","+objTestMaster.getNtatperiodcode()
						   +","+objTestMaster.getNinterfacetypecode()
						   +",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						   +objTestMaster.getNtransactionstatus()+","+objTestMaster.getNtrainingneed()
						   +","+objUserInfo.getNmastersitecode()+","
						   + " "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";

				jdbcTemplate.execute(inserttestquery);
				
				
				String reportinfotest ="Insert into reportinfotest(ntestcode,sconfirmstatement,sdecisionrule,ssopdescription,stestcondition,sdeviationcomments,smethodstandard,sreference,dmodifieddate,nsitecode,nstatus)"
						+ "values("+nseqtestcount+",N'None',N'None',N'None',N'None',N'None',N'None',N'None','"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") ";
				jdbcTemplate.execute(reportinfotest);			

				String updatetestquery="update SeqNoTestManagement set nsequenceno ="+nseqtestcount+" where stablename ='testmaster'";
				jdbcTemplate.execute(updatetestquery);
				

				
				objTestMaster.setNtestcode(nseqtestcount);
				
				savedTestList.add(objTestMaster);
				multilingualIDList.add("IDS_ADDTEST");
				//Insert Test Parameter 
				final int ntestcode = objTestMaster.getNtestcode();
				if(objTestParameter != null) {
					objTestParameter.setNtestcode(ntestcode);
					
					String seqtestparamquery ="select nsequenceno from SeqNoTestManagement where stablename='testparameter'";
					int nseqtestparamcount =(int) jdbcUtilityFunction.queryForObject(seqtestparamquery, Integer.class, jdbcTemplate);
					nseqtestparamcount++;
					
					if(objTestParameter.getNoperatorcode()==0) {
						objTestParameter.setNoperatorcode((short)-1);	
					}

					String inserttestparamquery =" Insert into testparameter(ntestparametercode,ntestcode,nparametertypecode,nunitcode,ndestinationunitcode,noperatorcode,nconversionfactor,sparametername,sparametersynonym,"
							+" nroundingdigits,nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible,dmodifieddate,nsitecode,nstatus,nresultaccuracycode)"
							+" values ("+nseqtestparamcount+","+objTestParameter.getNtestcode()+","+objTestParameter.getNparametertypecode()+","+objTestParameter.getNunitcode()+","+objTestParameter.getNdestinationunitcode()+","+objTestParameter.getNoperatorcode()+","
							+" "+objTestParameter.getNconversionfactor()+",N'"+stringUtilityFunction.replaceQuote(objTestParameter.getSparametername())+"',"
							+" N'"+stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym())+"',"+objTestParameter.getNroundingdigits()+","+objTestParameter.getNisadhocparameter()+","+objTestParameter.getNdeltachecklimitcode()+","+objTestParameter.getNdeltacheck()+","
							+objTestParameter.getNdeltacheckframe()+","+objTestParameter.getNdeltaunitcode()+","+objTestParameter.getNisvisible()+",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+","+objTestParameter.getNresultaccuracycode()+")";
		
					jdbcTemplate.execute(inserttestparamquery);

					String updatetestparamquery="update SeqNoTestManagement set nsequenceno ="+nseqtestparamcount+" where stablename ='testparameter'";
					jdbcTemplate.execute(updatetestparamquery);
					objTestParameter.setNtestparametercode(nseqtestparamcount);
					
					savedTestList.add(objTestParameter);
					multilingualIDList.add("IDS_ADDTESTPARAMETER");
					if(objTestParameter.getObjPredefinedParameter()!=null) {
						TestPredefinedParameter objPredefinedParam = objTestParameter.getObjPredefinedParameter();
						objPredefinedParam.setNtestparametercode(objTestParameter.getNtestparametercode());
						
						String seqtpredefinedquery ="select nsequenceno from SeqNoTestManagement where stablename='testpredefinedparameter'";
						int nseqpredefinedcount =(int) jdbcUtilityFunction.queryForObject(seqtpredefinedquery, Integer.class,jdbcTemplate);
						nseqpredefinedcount++;
						
						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,spredefinedsynonym,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
								+" values ("+nseqpredefinedcount+","+objPredefinedParam.getNtestparametercode()+","+objPredefinedParam.getNgradecode()+",N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname())+"',N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedsynonym())+"','"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
								+" "+objPredefinedParam.getNdefaultstatus()+","+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
						jdbcTemplate.execute(insertpredefinedquery);

						String updatepredefinedquery="update SeqNoTestManagement set nsequenceno ="+nseqpredefinedcount+" where stablename ='testpredefinedparameter'";
						jdbcTemplate.execute(updatepredefinedquery);
						
						objPredefinedParam.setNtestpredefinedcode(nseqpredefinedcount);
						
						savedTestList.add(objPredefinedParam);
						multilingualIDList.add("IDS_ADDTESTPREDEFINEDPARAMETER");
						

					}
					//ALPD-5873--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
											
							StringBuilder testGroupBuilder=new StringBuilder();
							final String sQuery = "select nsequenceno,stablename from seqnotestgroupmanagement where stablename in (N'testgrouptest',"
									+ "N'testgrouptestcharparameter',N'testgrouptestfile',N'testgrouptestformula',N'testgrouptestnumericparameter',N'testgrouptestparameter',"
									+ "N'testgrouptestpredefparameter')";
							
							List<SeqNoTestGroupmanagement> lstSeqNo =  jdbcTemplate.query(sQuery,new SeqNoTestGroupmanagement());
							
							Map<String, Integer> seqMap = lstSeqNo.stream()	.collect(Collectors.toMap(SeqNoTestGroupmanagement::getStablename,seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
							
							int ntestgrouptestcode=seqMap.get("testgrouptest")+1;
							int ntestgrouptestparametercode=seqMap.get("testgrouptestparameter")+1;

							int nmethodcode = -1;
							int ninstcatcode = -1;
							
							int ntestpackagecode=-1;
							
							testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestcode+" where stablename ='testgrouptest';");
							testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestparametercode+" where stablename ='testgrouptestparameter';");

							
							if(objMethod!=null && objMethod.getNmethodcode()!=0) {
								nmethodcode=objMethod.getNmethodcode();
							}
							if(objTestInstCat!=null && objTestInstCat.getNinstrumentcatcode()!=0) {
								ninstcatcode= objTestInstCat.getNinstrumentcatcode();
							}
							if(objTestpackage!=null && objTestpackage.getNtestpackagecode()!=0) {
								ntestpackagecode=objTestpackage.getNtestpackagecode();
							}
							//enumeration
							int nsorter=0;
							String nsortter="select * from testgrouptest where ntestgrouptestcode="+ntestgrouptestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(nsortter, TestGroupTest.class, jdbcTemplate);
							if(objecTestGroupTest==null) {
								nsorter=nsorter+1;
							}else {
								nsorter=objecTestGroupTest.getNsorter()+1;
							}
							testGroupBuilder.append("insert into testgrouptest (ntestgrouptestcode, nspecsampletypecode, ntestcode, nsectioncode, nmethodcode, ninstrumentcatcode, "
									+ "ncontainertypecode,ntestpackagecode,stestsynonym, nrepeatcountno, ncost, nsorter, nisadhoctest, nisvisible, dmodifieddate,nsitecode,nstatus) values ("
									+ ntestgrouptestcode + ",(select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),"
									+ objTestMaster.getNtestcode() + "," + objSection.getNsectioncode()+"," + nmethodcode + "," + ninstcatcode+","+Enumeration.TransactionStatus.NA.gettransactionstatus()+","+ntestpackagecode
									+ ",N'" + stringUtilityFunction.replaceQuote(objTestMaster.getStestname()) + "',1, " + objTestMaster.getNcost()
									+ "," + nsorter + "," +Enumeration.TransactionStatus.NO.gettransactionstatus() +","
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()
									+ "," +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
							
							if (objTestMaster.getNtestcode() == objTestParameter.getNtestcode()) {
								String testParameterSorterQ="select * from testgrouptestparameter where ntestgrouptestcode="+ntestgrouptestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
								int testParameterSorter=0;
								TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQ, TestGroupTestParameter.class, jdbcTemplate);
								if(objTestGroupTestParameter==null) {
									testParameterSorter=testParameterSorter+1;
								}else {
									testParameterSorter=objTestGroupTestParameter.getNsorter()+1;
									}
								testGroupBuilder.append( "insert into testgrouptestparameter (ntestgrouptestparametercode, ntestgrouptestcode, ntestparametercode, nparametertypecode, nunitcode, sparametersynonym,"
										+ "nroundingdigits, nresultmandatory, nreportmandatory, ngradingmandatory, nchecklistversioncode, sspecdesc, nsorter, nisadhocparameter, nisvisible,dmodifieddate, nstatus,nsitecode,nresultaccuracycode)"
										+ " values (" + ntestgrouptestparametercode + "," + ntestgrouptestcode + "," + objTestParameter.getNtestparametercode()
										+ "," + objTestParameter.getNparametertypecode() + "," + objTestParameter.getNunitcode()
										+ ",N'" + stringUtilityFunction.replaceQuote(objTestParameter.getSparametername()) + "',"
										+ objTestParameter.getNroundingdigits() + ","
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "," + " -1, NULL,"
										+ testParameterSorter + "," + Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+","+objTestParameter.getNresultaccuracycode()+");");
							
							
								if( objTestParameter.getNparametertypecode()==Enumeration.ParameterType.NUMERIC.getparametertype()) {
									int ntestgrouptestnumericcode=seqMap.get("testgrouptestnumericparameter")+1;

									testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestnumericcode+" where stablename ='testgrouptestnumericparameter';");

									testGroupBuilder.append("insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
											+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
											+ " values (" + ntestgrouptestnumericcode + ", " + ntestgrouptestparametercode
											+ ", NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,"
											+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+",-1);");
								}
								else if(objTestParameter.getNparametertypecode()==Enumeration.ParameterType.PREDEFINED.getparametertype()) {
									final TestPredefinedParameter objPredefinedParam = objTestParameter.getObjPredefinedParameter();
									int ntestgrouptestpredefparametercode=seqMap.get("testgrouptestpredefparameter")+1;

									testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestpredefparametercode+" where stablename ='testgrouptestpredefparameter';");

									testGroupBuilder.append( "insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
												+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode) values (" + (ntestgrouptestpredefparametercode) + "," + ntestgrouptestparametercode + ","
												+ objPredefinedParam.getNgradecode() + "," + "N'"
												+ stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname()) + "',N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedsynonym()) + "',"
												+ objPredefinedParam.getNdefaultstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+");");
								}
								else if(objTestParameter.getNparametertypecode()==Enumeration.ParameterType.CHARACTER.getparametertype()) {
									
									int ntestgrouptestcharparametercode=seqMap.get("testgrouptestcharparameter")+1;

									testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestcharparametercode+" where stablename ='testgrouptestcharparameter';");

									testGroupBuilder.append("insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate,nstatus,nsitecode)"
											+ " values (" + ntestgrouptestcharparametercode + "," + ntestgrouptestparametercode + ",NULL,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+");");
								}
								
							jdbcTemplate.execute(testGroupBuilder.toString());
						}
					}
				}
				
				
			
				
				//Insert Test Section
				if(objSection!=null && objSection.getNsectioncode()!=0) {
					objSection.setNtestcode(objTestMaster.getNtestcode());
					
					String seqsectionquery ="select nsequenceno from SeqNoTestManagement where stablename='testsection'";
					int nseqsectioncount = (int) jdbcUtilityFunction.queryForObject(seqsectionquery, Integer.class,jdbcTemplate);
					
					nseqsectioncount++;
					
					String insertsectionquery =" Insert into testsection(ntestsectioncode,ntestcode,nsectioncode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
										   + " values ("+nseqsectioncount+","+objSection.getNtestcode()+","+objSection.getNsectioncode()+","+objSection.getNdefaultstatus()
										   +",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
					jdbcTemplate.execute(insertsectionquery);

					String updatesectionquery="update SeqNoTestManagement set nsequenceno ="+nseqsectioncount+" where stablename ='testsection'";
					jdbcTemplate.execute(updatesectionquery);
					objSection.setNtestsectioncode(nseqsectioncount);
					
					savedTestList.add(objSection);
					//multilingualIDList.add("IDS_ADDTESTSECTION");
					multilingualIDList.add("IDS_ADDTESTMATERSECTION");
				}
				//Insert Test Method
				if(objMethod!=null && objMethod.getNmethodcode()!=0) {
					objMethod.setNtestcode(objTestMaster.getNtestcode());
					
					String seqmethodquery ="select nsequenceno from SeqNoTestManagement where stablename='testmethod'";
					int nseqmethodcount = (int) jdbcUtilityFunction.queryForObject(seqmethodquery, Integer.class,jdbcTemplate);
					
					nseqmethodcount++;
					
					String insertmethodquery =" Insert into testmethod(ntestmethodcode,ntestcode,nmethodcode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
										    + " values ("+nseqmethodcount+","+objMethod.getNtestcode()+","+objMethod.getNmethodcode()+","+objMethod.getNdefaultstatus()
										    +",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
					jdbcTemplate.execute(insertmethodquery);

					String updatemethodquery="update SeqNoTestManagement set nsequenceno ="+nseqmethodcount+" where stablename ='testmethod'";
					jdbcTemplate.execute(updatemethodquery);
					objMethod.setNtestmethodcode(nseqmethodcount);
					
					savedTestList.add(objMethod);
					multilingualIDList.add("IDS_ADDTESTMETHOD");
				}
				//Insert Test InstrumentCategory
				if(objTestInstCat!=null && objTestInstCat.getNinstrumentcatcode()!=0) {
					objTestInstCat.setNtestcode(objTestMaster.getNtestcode());
					
					String seqinstrumentcatquery ="select nsequenceno from SeqNoTestManagement where stablename='testinstrumentcategory'";
					int nseqinstrumentcatcount = (int) jdbcUtilityFunction.queryForObject(seqinstrumentcatquery, Integer.class,jdbcTemplate);
					
					nseqinstrumentcatcount++;
					
					String insertinstrumentcatquery =" Insert into testinstrumentcategory(ntestinstrumentcatcode,ntestcode,ninstrumentcatcode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
													+" values ("+nseqinstrumentcatcount+","+objTestInstCat.getNtestcode()+","+objTestInstCat.getNinstrumentcatcode()+","+objTestInstCat.getNdefaultstatus()+", '"
													+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
					jdbcTemplate.execute(insertinstrumentcatquery);

					String updateinstrumentcatquery="update SeqNoTestManagement set nsequenceno ="+nseqinstrumentcatcount+" where stablename ='testinstrumentcategory'";
					jdbcTemplate.execute(updateinstrumentcatquery);
					objTestInstCat.setNtestinstrumentcatcode(nseqinstrumentcatcount);
					
					savedTestList.add(objTestInstCat);
					multilingualIDList.add("IDS_ADDTESTINSTRUMENTCATEGORY");
				}

				
				if(objTestpackage!=null && objTestpackage.getNtestpackagecode()!=0) {
					objTestpackage.setNtestcode(objTestMaster.getNtestcode());
					
					String seqsectionquery ="select nsequenceno from SeqNoTestManagement where stablename='testpackagetest'";
					int nseqsectioncount = (int) jdbcUtilityFunction.queryForObject(seqsectionquery, Integer.class,jdbcTemplate);
				
					nseqsectioncount++;
					
					String insertsectionquery =" Insert into testpackagetest(ntestpackagetestcode,ntestcode,ntestpackagecode,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
										   + " values ("+nseqsectioncount+","+objTestpackage.getNtestcode()+","+objTestpackage.getNtestpackagecode()+",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',4,"+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
					jdbcTemplate.execute(insertsectionquery);

					String updatesectionquery="update SeqNoTestManagement set nsequenceno ="+nseqsectioncount+" where stablename ='testpackagetest'";
					jdbcTemplate.execute(updatesectionquery);
					objTestpackage.setNtestpackagetestcode(nseqsectioncount);
					
					savedTestList.add(objTestpackage);
					multilingualIDList.add("IDS_ADDTESTPACKAGE");
				}
				
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, objUserInfo);
				return testMasterCommonFunction.getTestMasterAfterInsert(objTestMaster, false, objUserInfo);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			} else {
				//Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}


		/**
		 * This method definition is used to update the testmaster details
		 * If user entered testname is available irrespective of test category  then returns already exists message.
		 * If the test is not active then returns already deleted message.
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
		 */
		@Override
		public ResponseEntity<Object> updateTestMaster(final TestMaster objTestMaster, final UserInfo objUserInfo,
				final boolean validateTest,final int isQualisLite) throws Exception {
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				boolean validUpdate = true;
				
				if (validUpdate)
				{
					final TestCategory activeCategory = getTestCatBytestcat(objTestMaster.getNtestcategorycode());
					if(activeCategory != null) {
					//testcategory code commented in below query as per LIMS Common FRS-00251
					final String strQuery = "select ntestcode from testmaster where stestname=N'"
				            + stringUtilityFunction.replaceQuote(objTestMaster.getStestname()) + "'"
							+ " and ntestcode !="+objTestMaster.getNtestcode()
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestMaster duplicateTest = (TestMaster) jdbcUtilityFunction.queryForObject(strQuery,TestMaster.class,jdbcTemplate); 
					 
					if(duplicateTest == null) 
					{
						final List<String> strLst = new ArrayList<String>();
					
						 String query = "update testmaster set ntestcategorycode=" + objTestMaster.getNtestcategorycode()
											+ ",stestname=N'" + stringUtilityFunction.replaceQuote(objTestMaster.getStestname()) 
											+ "',stestsynonym=N'"+ stringUtilityFunction.replaceQuote(objTestMaster.getStestsynonym())
											+ "',stestplatform=N'"+ stringUtilityFunction.replaceQuote(objTestMaster.getStestplatform()) 
											+ "',ncost= " + objTestMaster.getNcost() 
											+ ",nchecklistversioncode="+objTestMaster.getNchecklistversioncode() 
											+ ",ntat="+objTestMaster.getNtat()
											+ ",ntatperiodcode="+objTestMaster.getNtatperiodcode()
											+ ",sdescription=N'" + stringUtilityFunction.replaceQuote(objTestMaster.getSdescription())
											+ "'"+",naccredited=" + objTestMaster.getNaccredited()
											+ ",ntransactionstatus= "+objTestMaster.getNtransactionstatus()+""
											+ ",ntrainingneed= "+objTestMaster.getNtrainingneed()+""
											+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
											+ " ,sshortname = '" + stringUtilityFunction.replaceQuote(objTestMaster.getSshortname()) + "'"
								    		+ ",ninterfacetypecode="+objTestMaster.getNinterfacetypecode()							    	
											+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ntestcode=" + objTestMaster.getNtestcode() + ";";
						 //ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						 if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							 query = query+"update testgrouptest set  ncost = "
								+ objTestMaster.getNcost() + ", stestsynonym = N'"
								+ stringUtilityFunction.replaceQuote(objTestMaster.getStestsynonym()) + "'," 
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest where ntestcode="+objTestMaster.getNtestcode()+" and "
								+ " nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+");";
								
						 }
						 jdbcTemplate.execute(query);
			
						
						final List<Object> lstnewTestMaster= new ArrayList<Object>();//(List<Object>) jdbcTemplate.query(strQuery, TestMaster.class);
						final List<Object> lstoldTestMaster= new ArrayList<Object>();
						
						lstnewTestMaster.add(objTestMaster);
						lstoldTestMaster.add(testMasterById);
						strLst.add("IDS_EDITTESTMASTER");
						
						auditUtilityFunction.fnInsertAuditAction(lstnewTestMaster, 2, lstoldTestMaster, strLst, objUserInfo);
						
						return testMasterCommonFunction.getTestMasterAfterInsert(objTestMaster, true, objUserInfo);
					}
					else{
						//Conflict = 409 - Duplicate entry
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
				}
				else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage( "IDS_TESTEXISTSINTESTGROUP",
							objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		}
		
		public ResponseEntity<Object> validateTestExistenceInTestGroup(final int ntestCode, final UserInfo userInfo)  throws Exception
		{
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestCode);
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {

				//ALPD-5273--Vignesh R(27-01-2025)--Test master-->In test master and try to inactive that test without include test group, a confirmation box appeared 
				if(testMasterById.getNtransactionstatus() != Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus()) 
				{
					final String sQuery = "select tgt.*, tgs.napprovalstatus from testgrouptest tgt, "
										+ " testgroupspecsampletype tgsst, testgroupspecification tgs "
										+ " where tgt.nspecsampletypecode = tgsst.nspecsampletypecode "
										+ " and tgsst.nallottedspeccode = tgs.nallottedspeccode "
										+ " and tgt.ntestcode = "+ ntestCode
										+ " and tgs.napprovalstatus not in (" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
										+  Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()+ ") "
										+ " and tgt.nstatus =tgsst.nstatus "
										+ " and tgsst.nstatus= tgs.nstatus " 
										+ " and tgs.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tgt.nspecsampletypecode>0";
					final List<TestGroupTest> testGroup = (List<TestGroupTest>)  jdbcTemplate.query(sQuery, new TestGroupTest());
					if (testGroup.isEmpty()) {
						//Test not exists in test group
						return new ResponseEntity<>(false, HttpStatus.OK);
						//return updateTestMaster(testMaster, userInfo, false);
					}
					else {
						//Test exists in test group
						return new ResponseEntity<>(true, HttpStatus.OK);
					}
				}
				else {
					return new ResponseEntity<>(false, HttpStatus.OK);
				}
			}
		}

		/**
		 * This method is used to delete the testmaster
		 * Checks whether the test is already deleted
		 * Checks the delete validation whether it's used in other masters/transaction (It used in test group and edqmsafty marker)
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return  response entity holds the list of testmaster, selectedtest, testparameter, selectedparameter and parameter details based on parameter type
		 */
		@Override
		public ResponseEntity<Object> deleteTestMaster(final TestMaster objTestMaster, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();

			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				//For_MSSQL
				//		String str1 ="select  STUFF(" + 
				//				"(SELECT ',' +convert(nvarchar(15),ntestparametercode) FROM testparameter where ntestcode="+objTestMaster.getNtestcode()+" FOR XML PATH ('')), 1, 1,'')" ;
				//For_MSSQL_PostgreSQL
				String str1="select  STRING_AGG(CAST (ntestparametercode AS varchar),',') as ntestparametercode FROM testparameter where ntestcode="+objTestMaster.getNtestcode()+"";;
				String ntestparametercode= (String) jdbcUtilityFunction.queryForObject(str1, String.class, jdbcTemplate);
				//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
				String sdeletevalidationQuery="";
				if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					sdeletevalidationQuery=" and nspecsampletypecode>0";
				}
				final String queryDeleteValidation= "select 'IDS_TESTGROUP' as Msg from testgrouptest where ntestcode= " 
				        + objTestMaster.getNtestcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "+sdeletevalidationQuery+""
						+" union all "
						+ "select 'IDS_TESTPRICING' as Msg from testpricedetail where ntestcode= " 
						+ objTestMaster.getNtestcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " union all "
						+ "select 'IDS_TECHNIQUE' as Msg from techniquetest where ntestcode= "
						+ objTestMaster.getNtestcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " union all "
						+ "select 'IDS_REGISTRATION' as Msg from registrationtest where ntestcode= "
						+ objTestMaster.getNtestcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " union all "
						+"select 'IDS_INTERFACERTEST' as Msg from interfacermapping where ntestcode ="
						+ objTestMaster.getNtestcode() + "  and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						
//						+ " union all "
//						+ " SELECT 'IDS_REGISTRATION' as Msg FROM registration r "
//						+ " JOIN jsonb_each(r.jsondata) d ON true where "
//						+ " d.value->>'pkey' ='nunitcode' and d.value->>'nquerybuildertablecode'='253' "
//						+ " and d.value->>'value'='" + objUnit.getNunitcode()  + "'"
//						+ " union all "
//						+ " SELECT 'IDS_REGISTRATIONSAMPLE' as Msg FROM registrationsample rs "
//						+ " JOIN jsonb_each(rs.jsondata) d1 ON true where "
//						+ " d1.value->>'pkey' ='nunitcode' and d1.value->>'nquerybuildertablecode'='253' "
//						+ " and d1.value->>'value'='" + objUnit.getNunitcode()  + "'";
				
				validatorDel = projectDAOSupport.getTransactionInfo(queryDeleteValidation, objUserInfo);   	

				
				boolean validRecord = false;
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{		
					validRecord = true;
					//ALPD-4513--Vignesh R(05-09-2024)
					Map<String,Object> objOneToManyValidation=new HashMap<String,Object>();
					objOneToManyValidation.put("primaryKeyValue", Integer.toString(objTestMaster.getNtestcode()));
					objOneToManyValidation.put("stablename", "testmaster");				
					validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, objUserInfo);
					
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
					{					
						validRecord = true;
					}
					else {
						validRecord = false;
					}
				}
				
				if(validRecord) {
				
//				ValidatorDel objDeleteValidation = getTransactionInfo(sQuery, objUserInfo);
//				if (objDeleteValidation.getnreturnstatus() == Enumeration.Deletevalidator.SUCESS.getReturnvalue()) {
					String deletetest="update testmaster set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' "
//				    		+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//				    		+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
							+" where ntestcode="+objTestMaster.getNtestcode()+";";
					
					deletetest=deletetest+"update reportinfotest set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+",dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' "
										+ "where ntestcode ="+objTestMaster.getNtestcode()+";";									
					deletetest=deletetest+"update testparameter set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' "
//		    				+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//		    				+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
							+ " where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testsection set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+
							", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) +"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testmethod set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testinstrumentcategory set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testfile set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testformula set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestparametercode in ("+ntestparametercode+") ;";
					deletetest=deletetest+"update testpredefinedparameter set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' "
//		    				+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//		    				+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
							+" where ntestparametercode in ("+ntestparametercode+" );";
					deletetest=deletetest+"update testparameternumeric set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestparametercode in ("+ntestparametercode+" );";
					deletetest=deletetest+"update testcontainertype set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					        +", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testpackagetest set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					        +", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestcode="+objTestMaster.getNtestcode()+";";
					deletetest=deletetest+"update testmasterclinicalspec set nstatus ="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					         +", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestparametercode in ("+ntestparametercode+" );";
					//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						//ALPD-5268--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while deleting the test
						deletetest = deletetest+"update testgrouptest set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode = (select ntestgrouptestcode from "
								+ " testgrouptest where ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+");";
						
						deletetest = deletetest + "update testgrouptestparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest where ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+");";
						
						//ALPD-5268--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while deleting the test
						deletetest = deletetest + "update testgrouptestnumericparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where "
								+ " ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest where ntestcode="+objTestMaster.getNtestcode()+"  and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"));";
						
						//ALPD-5268--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while deleting the test
						deletetest = deletetest + "update testgrouptestpredefparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where "
								+ " ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest "
								+ " where ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"));";
						
						deletetest = deletetest + "update testgrouptestcharparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where "
								+ " ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest where ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"));";
						
											//ALPD-5268--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while deleting the test
						deletetest = deletetest + "update testgrouptestformula set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode =(select ntestgrouptestcode from testgrouptest where "
								+ " ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+");";
						
						deletetest = deletetest + "update testgrouptestfile set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode =(select ntestgrouptestcode from testgrouptest where "
								+ " ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+");";
					
											//ALPD-5268--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while deleting the test
						deletetest = deletetest + "update testgrouptestclinicalspec set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where "
								+ " ntestgrouptestcode = (select ntestgrouptestcode from testgrouptest where ntestcode="+objTestMaster.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"));";
						
					
					}
					
					jdbcTemplate.execute(deletetest);
					String query ="";
					
					query="select * from testmaster where ntestcode ="+objTestMaster.getNtestcode()+"";
					List <TestMaster> lstTestMaster =jdbcTemplate.query(query, new TestMaster());
					
					query="select tp.ntestparametercode, tp.ntestcode, tp.nparametertypecode, tp.nunitcode, tp.sparametername, tp.sparametersynonym"
							+ ", tp.nroundingdigits, tp.nisadhocparameter, tp.ndeltachecklimitcode, tp.ndeltacheck, tp.ndeltacheckframe, tp.ndeltaunitcode, tp.nisvisible, tp.nsitecode, tp.nstatus, pt.sdisplaystatus,tp.nresultaccuracycode from testparameter tp, parametertype pt where tp.ntestcode ="+objTestMaster.getNtestcode()+" and tp.nparametertypecode=pt.nparametertypecode";
//					List <TestParameter> lstTestParameter = getJdbcTemplate().query(query, new TestParameter());
					List <TestParameter> lstTestParameter = objMapper.convertValue(commonFunction.getMultilingualMessageList(
							jdbcTemplate.query(query, new TestParameter()),Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),new TypeReference<List<TestParameter>>() {});

					query="select * from testsection where ntestcode="+objTestMaster.getNtestcode()+"";
					List <TestSection> lstTestSection =jdbcTemplate.query(query, new TestSection());

					query="select * from testmethod where ntestcode="+objTestMaster.getNtestcode()+"";
					List <TestMethod> lstTestMethod =jdbcTemplate.query(query, new TestMethod());
					
					query="select * from testinstrumentcategory where ntestcode ="+objTestMaster.getNtestcode()+"";
					List <TestInstrumentCategory> lstTestInstrumentCategory =jdbcTemplate.query(query, new TestInstrumentCategory());

					query="select * from testfile where ntestcode ="+objTestMaster.getNtestcode()+"";
					List <TestFile> lstTestFile =jdbcTemplate.query(query, new TestFile());
					
					query="select * from testpredefinedparameter where ntestparametercode in ("+ntestparametercode+")";
					List <TestPredefinedParameter> lstTestPredefinedParameter =jdbcTemplate.query(query, new TestPredefinedParameter());

					query="select * from testformula where ntestcode ="+objTestMaster.getNtestcode()+"";
					List <TestFormula> lstTestFormula =jdbcTemplate.query(query, new TestFormula());

					query="select * from testparameternumeric where ntestparametercode in ("+ntestparametercode+")";
					List <TestParameterNumeric> lstTestParameterNumeric =jdbcTemplate.query(query, new TestParameterNumeric());
					
					query="select * from testcontainertype where ntestcode ="+objTestMaster.getNtestcode()+";";
					List <TestContainerType> lstTestContainerType=jdbcTemplate.query(query, new TestContainerType());
					
					query="select * from testpackagetest where ntestcode ="+objTestMaster.getNtestcode()+";";
					List<TestPackageTest> lstTestPackageTest=jdbcTemplate.query(query, new TestPackageTest());
					
					query="select * from testmasterclinicalspec where ntestparametercode in ("+ntestparametercode+")";
					List <TestMasterClinicalSpec> lstTestParameterClinialSpec =jdbcTemplate.query(query, new TestMasterClinicalSpec());
					
					List<Object> lstAllObject =new ArrayList<Object>();
					lstAllObject.add(lstTestMaster);
					lstAllObject.add(lstTestParameter);
					lstAllObject.add(lstTestSection);
					lstAllObject.add(lstTestMethod);
					lstAllObject.add(lstTestInstrumentCategory);
					lstAllObject.add(lstTestFile);
					lstAllObject.add(lstTestPredefinedParameter);
					lstAllObject.add(lstTestFormula);
					lstAllObject.add(lstTestParameterNumeric);
					lstAllObject.add(lstTestContainerType);
					lstAllObject.add(lstTestPackageTest);
					lstAllObject.add(lstTestParameterClinialSpec);
					
					auditUtilityFunction.fnInsertListAuditAction(lstAllObject, 1,null, Arrays.asList("IDS_DELETETESTMASTER","IDS_DELETETESTPARAMETER","IDS_DELETETESTSECTION",
							"IDS_DELETETESTMETHOD","IDS_DELETETESTINSTRUMENTCATEGORY","IDS_DELETETESTFILE","IDS_DELETETESTPREDEFINEDPARAMETER","IDS_DELETETESTFORMULA",
							"IDS_DELETEPARAMETERSPECIFICATION","IDS_DELETETESTCONTAINERTYPE","IDS_DELETETESTPACKAGE","IDS_DELETETESTMASTERCLINICALSPEC"), objUserInfo);
					return testMasterCommonFunction.getTestMasterAfterInsert(objTestMaster, false, objUserInfo);
				}else {
					//status code:417
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

		/**
		 * This method definition is used to retrive the available section which is not available in testsection
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of section and response status
		 */
		@Override
		public ResponseEntity<Object> getAvailableSection(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				String sQuery = " select  " + objTestMaster.getNtestcode() + " as ntestcode,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " as nstatus,"+Enumeration.TransactionStatus.NO.gettransactionstatus()+" as ndefaultstatus,-1 as ntestsectioncode,s.nsectioncode,s.ssectionname"
								+ " from section s where s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and s.nsectioncode>0 and not exists (select 1 from testsection ts2 where ts2.nsectioncode= s.nsectioncode "
								+ " and ts2.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts2.ntestcode ="
								+ objTestMaster.getNtestcode() + ")";
				List<TestSection> lstAvailablelist = (List<TestSection>) jdbcTemplate.query(sQuery, new TestSection());
				if(!lstAvailablelist.isEmpty()) {
					return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
				} else{
					//status code:417
					//return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SECTIONNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTSECTIONNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		
				}
			}
		}

		/**
		 * This method definition is used to create a test section
		 * @param lstTestSection [List] holds the list of test section details
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of test section and response status
		 */
		@Override
		public ResponseEntity<Object> createTestSection(final List<TestSection> lstTestSection, final UserInfo objUserInfo) throws Exception {
			
			if(lstTestSection!=null && lstTestSection.size()>0) 
			{
				final int ntestcode = lstTestSection.get(0).getNtestcode();
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
				
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
					final String sTableLockQuery = " lock  table locktestmaster "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sTableLockQuery);
			    
				
		//			final StringBuilder sb = new StringBuilder();
					//For_MSSQL
		//			String sQuery = "select * from seqnotestmanagement with(tablockx)";
					//For_PostgreSQL
//					String sQuery = "LOCK TABLE seqnotestmanagement ";
//					getJdbcTemplate().execute(sQuery);
				    String sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testsection'";
					 int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
					 
					sQuery = "select "+nSeqNo+"+Rank()over(order by s.nsectioncode) ntestsectioncode,"+ntestcode+" ntestcode,s.nsectioncode,"
							+"case when (select count(ntestsectioncode) from testsection where ntestcode = "+ntestcode
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
							+"+Rank()over(order by s.nsectioncode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus,-1 nmasterauditcode"
							+" from section s where not exists (select 1 from testsection ts "
							+" where s.nsectioncode = ts.nsectioncode and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ts.ntestcode = "+ntestcode+")"+" and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and s.nsectioncode in ("+stringUtilityFunction.fnDynamicListToString(lstTestSection, "getNsectioncode")+")";
					List<TestSection> lstTestSection1 = (List<TestSection>) jdbcTemplate.query(sQuery, new TestSection());
					
					if(!lstTestSection1.isEmpty()) {
						String sInstQuery = "insert into testsection (ntestsectioncode, ntestcode, nsectioncode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)" 
								+"select "+nSeqNo+"+Rank()over(order by s.nsectioncode) ntestsectioncode,"+ntestcode+" ntestcode,s.nsectioncode,"
								+"case when (select count(ntestsectioncode) from testsection where ntestcode = "+ntestcode
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
								+"+Rank()over(order by s.nsectioncode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
								+"'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", "
								+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
								+" from section s where not exists (select 1 from testsection ts "
								+" where s.nsectioncode = ts.nsectioncode and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ts.ntestcode = "+ntestcode+")"+" and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and s.nsectioncode in ("+stringUtilityFunction.fnDynamicListToString(lstTestSection, "getNsectioncode")+");";
						jdbcTemplate.execute(sInstQuery);
		//				sb.append(sInstQuery);
						

//						 int temp=nSeqNo;
//						for(TestSection ts: lstTestSection1) {
//							temp++;
//							ts.setNtestsectioncode(temp);
//						}
						
						sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestSection1.size())+" where stablename = 'testsection';";
						jdbcTemplate.execute(sInstQuery);
		//				sb.append(sInstQuery);
		//				getJdbcTemplate().execute(sb.toString());
						final List<String> strTestSection= new ArrayList<>();
						final List<Object> objlstTestSection= new ArrayList<>();
						//commented by sonia on 17th April 2025 						
//						String auditquery="select "+nSeqNo+"+Rank()over(order by s.nsectioncode) ntestsectioncode,"+ntestcode+" ntestcode,s.nsectioncode,"
//						+"case when (select count(ntestsectioncode) from testsection where ntestcode = "+ntestcode
//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//						+"+Rank()over(order by s.nsectioncode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//						+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus "
//						+" from section s where not exists (select 1 from testsection ts "
//						+" where s.nsectioncode = ts.nsectioncode and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ts.ntestcode = "+ntestcode+")"+" and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and s.nsectioncode in ("+stringUtilityFunction.fnDynamicListToString(lstTestSection, "getNsectioncode")+");";
//						final List<TestSection> lstvalidate=(List<TestSection>) jdbcTemplate.query(auditquery, new TestSection());

						objlstTestSection.add(lstTestSection1);
						//strTestSection.add("IDS_ADDTESTSECTION");
						strTestSection.add("IDS_ADDTESTMATERSECTION");
						auditUtilityFunction.fnInsertListAuditAction(objlstTestSection, 1,null, strTestSection, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo), HttpStatus.OK);
					} else {
						return new ResponseEntity<>(testMasterCommonFunction.getTestSection(ntestcode, objUserInfo), HttpStatus.OK);
					}
				} 
			}
			else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SECTIONNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to delete a testsection
		 * Checks whether this test section is active or not. If the test section is not active then returns already deleted message
		 * If this test section is default then it can't be deleted, return the default cannot be deleted message
		 * @param objTestSection [TestSection] holds the details of test section
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of testsection and response status
		 */
		@Override
		public ResponseEntity<Object> deleteTestSection(final TestSection objTestSection, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestSection.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				String sQuery = "select * from testsection where ntestsectioncode = "+objTestSection.getNtestsectioncode()
							+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				TestSection objTestSection1 = (TestSection) jdbcUtilityFunction.queryForObject(sQuery, TestSection.class, jdbcTemplate);
				if(objTestSection1 != null) {
					
					final String query="select 'IDS_TESTGROUP' as Msg from testgrouptest where nsectioncode= " 
			        		  + objTestSection1.getNsectioncode() + " and ntestcode="+ objTestSection1.getNtestcode()
			        		  + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, objUserInfo);  
					
					if (objDeleteValidation.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
					
					if(objTestSection1.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final List<String> strArray= new ArrayList<>();
						final List<Object> objlst = new ArrayList<>();
		//				String sdeleteQuery = "delete from testsection where ntestsectioncode=" + objTestSection.getNtestsectioncode() + " and nstatus="
		//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						String sdeleteQuery = "update testsection set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) +"' where ntestsectioncode=" + objTestSection.getNtestsectioncode();
						jdbcTemplate.execute(sdeleteQuery);
						objlst.add(objTestSection);
						//strArray.add("IDS_DELETETESTSECTION");
						strArray.add("IDS_DELETETESTMATERSECTION");
			            auditUtilityFunction.fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);
			            return new ResponseEntity<>(testMasterCommonFunction.getTestSection(objTestSection.getNtestcode(), objUserInfo), HttpStatus.OK);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

		/**
		 * This method definition is used to retrive the available method which is not available in testmethod
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of method and response status
		 */
		@Override
		public ResponseEntity<Object> getAvailableMethod(final TestMaster objTestMaster, final UserInfo objUserInfo)
				throws Exception {
			String sQuery = "";
			if (objTestMaster == null) {
				sQuery = " select  m.* from method m where m.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+" and m.nmethodcode >0 and m.nneedvalidity="+Enumeration.TransactionStatus.NO.gettransactionstatus()
						+" union "
						+" select  m.* from method m,methodvalidity md WHERE  m.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and md.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and md.ntransactionstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+" AND m.nmethodcode = md.nmethodcode  AND ( md.dvalidityenddate > '"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ) ";
				List<Method> lstAvailablelist = (List<Method>) jdbcTemplate.query(sQuery, new Method());
				//if (!lstAvailablelist.isEmpty()) {
					return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
				/*} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_METHODNOTAVAILABLE",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}*/
			} else {
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
					sQuery = 
							"  select  "+ objTestMaster.getNtestcode() +" as ntestcode,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " as nstatus,4 as ntransactionstatus,-1 as ntestmethodcode,"
							+ " m.nmethodcode,m.smethodname from method m where m.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and m.nmethodcode >0 and m.nneedvalidity="+Enumeration.TransactionStatus.NO.gettransactionstatus()
							+ " and not exists (select 1 from testmethod ts2 where ts2.nmethodcode= m.nmethodcode "
							+ " and ts2.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ts2.ntestcode =" + objTestMaster.getNtestcode() + ") "
							+" union "
							+ 
							" select  " + objTestMaster.getNtestcode() + " as ntestcode,"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " as nstatus,4 as ntransactionstatus,-1 as ntestmethodcode,"
							+ " m.nmethodcode,m.smethodname from method m,methodvalidity md where m.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and m.nmethodcode>0 and not exists (select 1 from testmethod ts2 where ts2.nmethodcode= m.nmethodcode "
							+ " and ts2.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ts2.ntestcode =" + objTestMaster.getNtestcode() + ") and md.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
							+ " and m.nmethodcode=md.nmethodcode and  (md.dvalidityenddate > '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "')";
					List<TestMethod> lstAvailablelist = (List<TestMethod>) jdbcTemplate.query(sQuery, new TestMethod());
					if (!lstAvailablelist.isEmpty()) {
						return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_METHODNOTAVAILABLE",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
			}

		}		



		
		/**
		 * This method definition is used to create a test method
		 * @param lstTestMethod [List] holds the list of test method details
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of test method and response status
		 */
		@Override
		public ResponseEntity<Object> createTestMethod(final List<TestMethod> lstTestMethod, final UserInfo objUserInfo,final int isQualisLite) throws Exception {		
		    
			if(lstTestMethod != null && lstTestMethod.size() > 0) 
			{
				final int ntestcode = lstTestMethod.get(0).getNtestcode();
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
					final String sTableLockQuery = " lock  table testmethod "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				   jdbcTemplate.execute(sTableLockQuery);
				    
					final String smethodcode = stringUtilityFunction.fnDynamicListToString(lstTestMethod, "getNmethodcode");
					//For_MSSQL
					//			String sQuery = "select * from seqnotestmanagement with(tablockx)";
								//For_PostgreSQL
					//			String sQuery = "LOCK TABLE seqnotestmanagement ";
					//			getJdbcTemplate().execute(sQuery);
					String sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testmethod'";
					final int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
					sQuery = "select "+nSeqNo+"+Rank()over(order by m.nmethodcode) ntestmethodcode,"+ntestcode+" ntestcode,m.nmethodcode,"
							+" case when (select count(ntestmethodcode) from testmethod where ntestcode = "+ntestcode
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
							+" +Rank()over(order by m.nmethodcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
							+" from method m where not exists (select 1 from testmethod tm "
							+" where m.nmethodcode = tm.nmethodcode and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and tm.ntestcode = "+ntestcode+")"+" and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and m.nmethodcode in ("+smethodcode+");";
					List<TestMethod> lstTestMethod1 = (List<TestMethod>) jdbcTemplate.query(sQuery,new TestMethod());
					if(!lstTestMethod1.isEmpty()) {
		//				final StringBuilder sb = new StringBuilder();
						String sInstQuery = "insert into testmethod (ntestmethodcode, ntestcode, nmethodcode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)" 
								+" select "+nSeqNo+"+Rank()over(order by m.nmethodcode) ntestmethodcode,"+ntestcode+" ntestcode,m.nmethodcode,"
								+" case when (select count(ntestmethodcode) from testmethod where ntestcode = "+ntestcode
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
								+" +Rank()over(order by m.nmethodcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,'"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) +"', " + objUserInfo.getNmastersitecode()+","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
								+" from method m where not exists (select 1 from testmethod tm "
								+" where m.nmethodcode = tm.nmethodcode and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and tm.ntestcode = "+ntestcode+")"+" and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and m.nmethodcode in ("+smethodcode+");";
				//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.		
				if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+ntestcode+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNmethodcode()==Enumeration.TransactionStatus.NA.gettransactionstatus()) {
								String updateQuery="update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nmethodcode=(select m.nmethodcode "
										+ " from method m where not exists (select 1 from testmethod tm where "
										+ "  m.nmethodcode = tm.nmethodcode  and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"   and tm.ntestcode = "+ntestcode+" ) "
										+ "  and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  and m.nmethodcode in ("+smethodcode+")  order by  m.nmethodcode "
										+ "  limit 1) where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								jdbcTemplate.execute(updateQuery);

						}
						}
						
						jdbcTemplate.execute(sInstQuery);
						
//						int temp=nSeqNo;
//						for(TestMethod tm:lstTestMethod1) {
//							temp++;
//							tm.setNtestmethodcode(temp);
//						}
//						
						sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestMethod1.size())+" where stablename = 'testmethod';";
		//				sb.append(sInstQuery);
						jdbcTemplate.execute(sInstQuery);
		//				getJdbcTemplate().execute(sb.toString());
						final List<String> strTestMethod= new ArrayList<>();
						final List<Object> objlstTestMethod= new ArrayList<>();
						//commented by sonia on 17th April 2025 
//						String auditquery = "select "+nSeqNo+"+Rank()over(order by m.nmethodcode) ntestmethodcode,"+ntestcode+" ntestcode,m.nmethodcode,"
//								+"case when (select count(ntestmethodcode) from testmethod where ntestcode = "+ntestcode
//								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//								+"+Rank()over(order by m.nmethodcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus "
//								+" from method m where not exists (select 1 from testmethod tm "
//								+" where m.nmethodcode = tm.nmethodcode and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+" and tm.ntestcode = "+ntestcode+")"+" and m.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+" and m.nmethodcode in ("+smethodcode+");";
//						List<TestMethod> lstauditqry = (List<TestMethod>) jdbcTemplate.query(auditquery,new TestMethod());

						
						objlstTestMethod.add(lstTestMethod1);
						strTestMethod.add("IDS_ADDTESTMETHOD");
						auditUtilityFunction.fnInsertListAuditAction(objlstTestMethod, 1,null, strTestMethod, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo), HttpStatus.OK);
					} else {
						return new ResponseEntity<>(testMasterCommonFunction.getTestMethod(ntestcode, objUserInfo), HttpStatus.OK);
					}
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_METHODNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to delete a test method
		 * Checks whether the test method is active or not. 
		 * If the test method is not active then returns the already deleted message.
		 * @param objTestMethod [TestMethod] holds the details of testmethod
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of method and response status
		 */
		@Override
		public ResponseEntity<Object> deleteTestMethod(final TestMethod objTestMethod, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMethod.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select * from testmethod where ntestmethodcode = "+objTestMethod.getNtestmethodcode()
							+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestMethod objTestMethod1 = (TestMethod) jdbcUtilityFunction.queryForObject(sQuery, TestMethod.class,jdbcTemplate);
				if(objTestMethod1 != null) {
					
					final String query="select 'IDS_TESTGROUP' as Msg from testgrouptest where nmethodcode= " 
			        		  + objTestMethod1.getNmethodcode() + " and ntestcode="+ objTestMethod1.getNtestcode()
			        		  + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, objUserInfo);  
					
					if (objDeleteValidation.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
					
					if(objTestMethod1.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				
						//to set default to another record before deleting this record if this is the current default record
						
						final String queryString = "select * from testmethod where ntestmethodcode <> "+objTestMethod.getNtestmethodcode()
											+ " and ntestcode=" + objTestMethod.getNtestcode()
											+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						
						final List<TestMethod> methodCatList = (List<TestMethod>) jdbcTemplate.query(queryString, new TestMethod());
						if (!methodCatList.isEmpty()) 
						{
							final TestMethod categoryBeforeSave = methodCatList.get(0);
							
							final TestMethod categoryAfterSave = SerializationUtils.clone(categoryBeforeSave);
							categoryAfterSave.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							
							 String updateQuery = "update testmethod set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
													+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestmethodcode=" + categoryBeforeSave.getNtestmethodcode()+";";

							//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
							if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestMethod.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ;";
								
								TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
								
									updateQuery=updateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nmethodcode="+categoryBeforeSave.getNmethodcode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
								
							
							}
							jdbcTemplate.execute(updateQuery);	
							
							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(categoryAfterSave),2, Arrays.asList(categoryBeforeSave), 
								Arrays.asList("IDS_EDITTESTMETHOD"), objUserInfo);
						
						}
					} 
					//else {
					 String sdeleteQuery = "update testmethod set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestmethodcode=" + objTestMethod.getNtestmethodcode()+";";
		//				final String sdeleteQuery = "delete from testmethod where ntestmethodcode=" + objTestMethod.getNtestmethodcode() + " and nstatus="
		//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.	
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestMethod.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
						
						TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
						if(objecTestGroupTest.getNmethodcode()==objTestMethod.getNmethodcode()) {
							sdeleteQuery=sdeleteQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nmethodcode="+Enumeration.TransactionStatus.NA.gettransactionstatus()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
						}
						
					}
					
					jdbcTemplate.execute(sdeleteQuery);
						final List<String> strArray= new ArrayList<>();
						final List<Object> objlst = new ArrayList<>();
						objlst.add(objTestMethod);
						strArray.add("IDS_DELETETESTMETHOD");
			            auditUtilityFunction.fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);
			            return new ResponseEntity<>(testMasterCommonFunction.getTestMethod(objTestMethod.getNtestcode(), objUserInfo), HttpStatus.OK);
					//}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		/**
		 * This method definition is used to retrive the available instrument category
		 * @param objTestMaster [TestMaster] holds the details of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of instrumentcategory and response status
		 */
		@Override
		public ResponseEntity<Object> getAvailableInstrumentCategory(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
			
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select  " + objTestMaster.getNtestcode() + " as ntestcode,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " as nstatus,4 as ntransactionstatus,-1 as ntestinstrumentcatcode,m.ninstrumentcatcode,m.sinstrumentcatname "
										+ " from instrumentcategory m,instrumenttype it where m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and it.ninstrumenttypecode=m.ninstrumenttypecode "
										+ "	and m.ninstrumenttypecode="+Enumeration.InstrumentType.STANDARD_INSTRUMENT.getNinstrumenttypecode()+""
										+ "	and it.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
										+ " and m.ninstrumentcatcode>0 and not exists (select 1 from testinstrumentcategory ts2 "
										+ " where ts2.ninstrumentcatcode= m.ninstrumentcatcode and ts2.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ts2.ntestcode = "+ objTestMaster.getNtestcode() + ")";
				final List<TestInstrumentCategory> lstAvailablelist = (List<TestInstrumentCategory>) jdbcTemplate.query(sQuery, new TestInstrumentCategory());
				if(!lstAvailablelist.isEmpty()) {
					return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
				} else{
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTCATEGORYNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		
		
		
		@Override
		public ResponseEntity<Object> getAvailablePackage(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select  " + objTestMaster.getNtestcode() + " as ntestcode,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " as nstatus,4 as ntransactionstatus,-1 as ntestinstrumentcatcode,m.ntestpackagecode,m.stestpackagename "
										+ " from testpackage m where m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and m.ntestpackagecode>0 and not exists (select 1 from testpackagetest ts2 "
										+ " where ts2.ntestpackagecode= m.ntestpackagecode and ts2.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and ts2.ntestcode = "+ objTestMaster.getNtestcode() + ")";
				final List<TestPackageTest> lstAvailablelist = (List<TestPackageTest>) jdbcTemplate.query(sQuery, new TestPackageTest());
				if(!lstAvailablelist.isEmpty()) {
					return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
				} else{
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PACKAGENOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

		/**
		 * This method definition is used to create a test instrumentcategory
		 * @param lstTestInstrumentCategory [List] holds the list of test instrumentcategory details
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of test instrumentcategory and response status
		 */
		@Override
		public ResponseEntity<Object> createInstrumentCategory(final List<TestInstrumentCategory> lstTestInstrumentCategory, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				    
			if(lstTestInstrumentCategory != null && lstTestInstrumentCategory.size() > 0) 
			{
				final int ntestcode = lstTestInstrumentCategory.get(0).getNtestcode();
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
				
					final String sTableLockQuery = " lock  table testinstrumentcategory "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				    jdbcTemplate.execute(sTableLockQuery);
				    
				    //			final StringBuilder sb = new StringBuilder();
					final String sinstrCatcode = stringUtilityFunction.fnDynamicListToString(lstTestInstrumentCategory, "getNinstrumentcatcode");
					//For_MSSQL
					//			String sQuery = "select * from seqnotestmanagement with(tablockx)";
					//For_PostgreSQL
					//			String sQuery = "LOCK TABLE seqnotestmanagement ";
					//			getJdbcTemplate().execute(sQuery);
					
					String sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testinstrumentcategory'";
					final int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
					
					sQuery = "select "+nSeqNo+"+Rank()over(order by ic.ninstrumentcatcode) ntestinstrumentcatcode,"+ntestcode+" ntestcode,ic.ninstrumentcatcode,"
							+"case when (select count(ntestinstrumentcatcode) from testinstrumentcategory where ntestcode = "+ntestcode
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
							+"+Rank()over(order by ic.ninstrumentcatcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
							+" from instrumentcategory ic where not exists (select 1 from testinstrumentcategory tic "
							+" where ic.ninstrumentcatcode = tic.ninstrumentcatcode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ic.ninstrumentcatcode in ("+sinstrCatcode+");";
					final List<TestInstrumentCategory> lstTestInstrumentCategory1 = (List<TestInstrumentCategory>) jdbcTemplate.query(sQuery, new TestInstrumentCategory());
					
					if(!lstTestInstrumentCategory1.isEmpty()) 
					{
						String sInstQuery = "insert into testinstrumentcategory (ntestinstrumentcatcode, ntestcode, ninstrumentcatcode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)" 
								+"select "+nSeqNo+"+Rank()over(order by ic.ninstrumentcatcode) ntestinstrumentcatcode,"+ntestcode+" ntestcode,ic.ninstrumentcatcode,"
								+" case when (select count(ntestinstrumentcatcode) from testinstrumentcategory where ntestcode = "+ntestcode
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
								+"+Rank()over(order by ic.ninstrumentcatcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,'"
								+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", "
								+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
								+" from instrumentcategory ic where not exists (select 1 from testinstrumentcategory tic "
								+" where ic.ninstrumentcatcode = tic.ninstrumentcatcode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ic.ninstrumentcatcode in ("+sinstrCatcode+");";
		//				sb.append(sInstQuery);

						//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+ntestcode+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNinstrumentcatcode()==Enumeration.TransactionStatus.NA.gettransactionstatus()) {
								String updateQuery="update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ninstrumentcatcode=(select "
										+ "  ic.ninstrumentcatcode  "
										+ " from instrumentcategory ic "
										+ " where  not exists (select  1 from testinstrumentcategory tic  where  ic.ninstrumentcatcode = tic.ninstrumentcatcode "
										+ " and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tic.ntestcode = "+ntestcode+""+ ")"
										+ "  and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
										+ "  and ic.ninstrumentcatcode in ("+sinstrCatcode+") order by ic.ninstrumentcatcode limit 1) where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								jdbcTemplate.execute(updateQuery);

						
							}
						}
						jdbcTemplate.execute(sInstQuery);
						
//						int temp=nSeqNo;
//						for(TestInstrumentCategory tc:lstTestInstrumentCategory1) {
//							temp++;
//							tc.setNtestinstrumentcatcode(temp);
//						}
//						
						sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestInstrumentCategory1.size())+" where stablename = 'testinstrumentcategory';";
						jdbcTemplate.execute(sInstQuery);
						
		//				sb.append(sInstQuery);
		//				getJdbcTemplate().execute(sb.toString());
						final List<String> strTestInstrument= new ArrayList<>();
						final List<Object> objlstTestInstrument= new ArrayList<>();
						
//						String auditqry="select "+nSeqNo+"+Rank()over(order by ic.ninstrumentcatcode) ntestinstrumentcatcode,"+ntestcode+" ntestcode,ic.ninstrumentcatcode,"
//						+" case when (select count(ntestinstrumentcatcode) from testinstrumentcategory where ntestcode = "+ntestcode
//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//						+"+Rank()over(order by ic.ninstrumentcatcode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//						+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus "
//						+" from instrumentcategory ic where not exists (select 1 from testinstrumentcategory tic "
//						+" where ic.ninstrumentcatcode = tic.ninstrumentcatcode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ic.ninstrumentcatcode in ("+sinstrCatcode+");";
//						final List<TestInstrumentCategory> lstinstaudit = (List<TestInstrumentCategory>) jdbcTemplate.query(auditqry, new TestInstrumentCategory());

						objlstTestInstrument.add(lstTestInstrumentCategory1);
						strTestInstrument.add("IDS_ADDTESTINSTRUMENTCATEGORY");
						
						auditUtilityFunction.fnInsertListAuditAction(objlstTestInstrument, 1,null, strTestInstrument, objUserInfo);	
						
					} 	
					return new ResponseEntity<>(testMasterCommonFunction.getTestInstrumentCategory(ntestcode, objUserInfo), HttpStatus.OK);
				}
				
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTRUMENTCATEGORYNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}	
		
		
		@Override
		public ResponseEntity<Object> createTestpackage(final List<TestPackageTest> lstTestInstrumentCategory, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				    
			if(lstTestInstrumentCategory != null && lstTestInstrumentCategory.size() > 0) 
			{
				final int ntestcode = lstTestInstrumentCategory.get(0).getNtestcode();
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
				
		//			final StringBuilder sb = new StringBuilder();
					final String sinstrCatcode = stringUtilityFunction.fnDynamicListToString(lstTestInstrumentCategory, "getNtestpackagecode");
					//For_MSSQL
		//			String sQuery = "select * from seqnotestmanagement with(tablockx)";
					
					final String sTableLockQuery = " lock  table testpackagetest "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				    jdbcTemplate.execute(sTableLockQuery);
				    
					//For_PostgreSQL
//					String sQuery = "LOCK TABLE seqnotestmanagement ";
//					getJdbcTemplate().execute(sQuery);
					
					String sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testpackagetest'";
					final int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
					
					sQuery = "select "+nSeqNo+"+Rank()over(order by ic.ntestpackagecode) ntestinstrumentcatcode,"+ntestcode+" ntestcode,ic.ntestpackagecode,"
							+"case when (select count(ntestpackagetestcode) from testpackagetest where ntestcode = "+ntestcode
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
							+"+Rank()over(order by ic.ntestpackagecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
							+" from testpackage ic where not exists (select 1 from testpackagetest tic "
							+" where ic.ntestpackagecode = tic.ntestpackagecode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ic.ntestpackagecode in ("+sinstrCatcode+");";
					final List<TestPackageTest> lstTestInstrumentCategory1 = (List<TestPackageTest>) jdbcTemplate.query(sQuery, new TestPackageTest());
					
					if(!lstTestInstrumentCategory1.isEmpty()) 
					{
						String sInstQuery = "insert into testpackagetest (ntestpackagetestcode, ntestcode, ntestpackagecode, dmodifieddate,ndefaultstatus, nsitecode, nstatus)" 
								+"select "+nSeqNo+"+Rank()over(order by ic.ntestpackagecode) ntestpackagetestcode,"+ntestcode+" ntestcode,ic.ntestpackagecode,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', case when (select count(ntestpackagetestcode) from testpackagetest where ntestcode = "+ntestcode
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
								+"+Rank()over(order by ic.ntestpackagecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"+objUserInfo.getNmastersitecode()+","
								+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
								+" from testpackage ic where not exists (select 1 from testpackagetest tic "
								+" where ic.ntestpackagecode = tic.ntestpackagecode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ic.ntestpackagecode in ("+sinstrCatcode+");";
		//				sb.append(sInstQuery);
						
						//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+ntestcode+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNtestpackagecode()==Enumeration.TransactionStatus.NA.gettransactionstatus()) {
								sInstQuery=sInstQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ntestpackagecode="+lstTestInstrumentCategory.get(0).getNtestpackagecode()+" ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
						}
						}
						
						jdbcTemplate.execute(sInstQuery);
						
//						int temp=nSeqNo;
//	                    for(TestPackageTest tp: lstTestInstrumentCategory1) {
//	                    	    temp++;
//						    	tp.setNtestpackagetestcode(temp);
//					    }
						
						sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestInstrumentCategory1.size())+" where stablename = 'testpackagetest';";
						jdbcTemplate.execute(sInstQuery);
						
		//				sb.append(sInstQuery);
		//				getJdbcTemplate().execute(sb.toString());
					    
						final List<String> strTestInstrument= new ArrayList<>();
						final List<Object> objlstTestInstrument= new ArrayList<>();
						//commented by sonia on 17th April 2025 						
//						String auditqry="select "+nSeqNo+"+Rank()over(order by ic.ntestpackagecode) ntestpackagetestcode,"+ntestcode+" ntestcode,ic.ntestpackagecode,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', case when (select count(ntestpackagetestcode) from testpackagetest where ntestcode = "+ntestcode
//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//						+"+Rank()over(order by ic.ntestpackagecode)=1 then "+Enumeration.TransactionStatus.NO.gettransactionstatus()
//						+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"+objUserInfo.getNmastersitecode()+","
//						+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
//						+" from testpackage ic where not exists (select 1 from testpackagetest tic "
//						+" where ic.ntestpackagecode = tic.ntestpackagecode and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and tic.ntestcode = "+ntestcode+")"+" and ic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ic.ntestpackagecode in ("+sinstrCatcode+");";
//					final List<TestPackageTest> lstauditqry = (List<TestPackageTest>) jdbcTemplate.query(sQuery, new TestPackageTest());

					
						objlstTestInstrument.add(lstTestInstrumentCategory1);
						strTestInstrument.add("IDS_ADDTESTPACKAGE");
						
						auditUtilityFunction.fnInsertListAuditAction(objlstTestInstrument, 1,null, strTestInstrument, objUserInfo);
						
					}
					return new ResponseEntity<>(testMasterCommonFunction.getTestpackage(ntestcode, objUserInfo), HttpStatus.OK);
					
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PACKAGENOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to delete a test instrument category
		 * Checks whether the test instrument category is active or not. 
		 * If the test instrument category is not active then returns the already deleted message
		 * @param objTestInstrumentCategory [TestInstrumentCategory] holds the details of test instrumentcategory
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return response entity holds the list of test instrumentcategory and response status
		 */
		@Override
		public ResponseEntity<Object> deleteInstrumentCategory(final TestInstrumentCategory objTestInstrumentCategory, final UserInfo objUserInfo,final int isQualisLite) throws Exception 
		{
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestInstrumentCategory.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select * from testinstrumentcategory where ntestinstrumentcatcode = "+objTestInstrumentCategory.getNtestinstrumentcatcode()
							+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final TestInstrumentCategory objTestInstCat = (TestInstrumentCategory) jdbcUtilityFunction.queryForObject(sQuery, TestInstrumentCategory.class,jdbcTemplate);
				
				if(objTestInstCat != null) 
				{
					
					final String query="select 'IDS_TESTGROUP' as Msg from testgrouptest where ninstrumentcatcode= " 
			        		  + objTestInstCat.getNinstrumentcatcode() + " and ntestcode="+ objTestInstCat.getNtestcode()
			        		  + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, objUserInfo);  
					
					if (objDeleteValidation.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
					
					if(objTestInstCat.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) 
					{
						//to set default to another record before deleting this record if this is the current default record
						final String queryString = "select * from testinstrumentcategory where "
												+ " ntestinstrumentcatcode <> "+objTestInstrumentCategory.getNtestinstrumentcatcode()
												+ " and ntestcode = "+objTestInstrumentCategory.getNtestcode()
												+ " and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<TestInstrumentCategory> instCategoryList = (List<TestInstrumentCategory>) jdbcTemplate.query(queryString, new TestInstrumentCategory());
						if (!instCategoryList.isEmpty()) 
						{
							final TestInstrumentCategory categoryBeforeSave = instCategoryList.get(0);
							
							final TestInstrumentCategory categoryAfterSave =SerializationUtils.clone(categoryBeforeSave);
							categoryAfterSave.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							
							 String updateQuery = "update testinstrumentcategory set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
														+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestinstrumentcatcode=" + categoryBeforeSave.getNtestinstrumentcatcode()+";";

							//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestInstrumentCategory.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
								
								TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
												
									updateQuery=updateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ninstrumentcatcode="+categoryBeforeSave.getNinstrumentcatcode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
								
							
							}
							jdbcTemplate.execute(updateQuery);	
		
					        auditUtilityFunction.fnInsertAuditAction(Arrays.asList(categoryAfterSave),2, Arrays.asList(categoryBeforeSave), 
					        		Arrays.asList("IDS_EDITTESTINSTRUMENTCATEGORY"), objUserInfo);
							
						}
					} 
					//else {
					 String sdeleteQuery = "update testinstrumentcategory set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestinstrumentcatcode=" + objTestInstrumentCategory.getNtestinstrumentcatcode()+";";
		//			final String sdeleteQuery = "delete from testinstrumentcategory where ntestinstrumentcatcode=" + objTestInstrumentCategory.getNtestinstrumentcatcode() + " and nstatus="
		//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					//ALPD-4831--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.	
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestInstrumentCategory.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
						
						TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							if(objecTestGroupTest.getNinstrumentcatcode()==objTestInstrumentCategory.getNinstrumentcatcode()) {
								sdeleteQuery=sdeleteQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ninstrumentcatcode="+Enumeration.TransactionStatus.NA.gettransactionstatus()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
							}
					
					}
					jdbcTemplate.execute(sdeleteQuery);
					
					final List<String> strArray= new ArrayList<>();
					final List<Object> objlst = new ArrayList<>();
					objlst.add(objTestInstCat);
					strArray.add("IDS_DELETETESTINSTRUMENTCATEGORY");
		            auditUtilityFunction.fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);   
		            return new ResponseEntity<>(testMasterCommonFunction.getTestInstrumentCategory(objTestInstrumentCategory.getNtestcode(), objUserInfo), HttpStatus.OK);
		//			}	
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		
		@Override
		public ResponseEntity<Object> deletePackage(final TestPackageTest objTestInstrumentCategory, final UserInfo objUserInfo) throws Exception 
		{
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestInstrumentCategory.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select * from testpackagetest where ntestpackagetestcode = "+objTestInstrumentCategory.getNtestpackagetestcode()
							+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final TestPackageTest objTestInstCat = (TestPackageTest) jdbcUtilityFunction.queryForObject(sQuery, TestPackageTest.class,jdbcTemplate);
				
				if(objTestInstCat != null) 
				{
					
					final String query="select 'IDS_TESTGROUP' as Msg from testgrouptest where ntestpackagecode= " 
			        		  + objTestInstCat.getNtestpackagecode() + " and ntestcode="+ objTestInstCat.getNtestcode()
			        		  + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, objUserInfo);  
					
					if (objDeleteValidation.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
					
					if(objTestInstCat.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) 
					{
						//to set default to another record before deleting this record if this is the current default record
						final String queryString = "select * from testpackagetest where "
												+ " ntestpackagetestcode <> "+objTestInstrumentCategory.getNtestpackagetestcode()
												+ " and ntestcode = "+objTestInstrumentCategory.getNtestcode()
												+ " and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<TestPackageTest> instCategoryList = (List<TestPackageTest>) jdbcTemplate.query(queryString, new TestPackageTest());
						if (!instCategoryList.isEmpty()) 
						{
							final TestPackageTest categoryBeforeSave = instCategoryList.get(0);
							
							final TestPackageTest categoryAfterSave =SerializationUtils.clone(categoryBeforeSave);
							categoryAfterSave.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							
							final String updateQuery = "update testpackagetest set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
														+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
														+"' where ntestpackagetestcode=" + categoryBeforeSave.getNtestpackagetestcode();
		
							jdbcTemplate.execute(updateQuery);	
		
					        auditUtilityFunction.fnInsertAuditAction(Arrays.asList(categoryAfterSave),2, Arrays.asList(categoryBeforeSave), 
					        		Arrays.asList("IDS_EDITTESTINSTRUMENTCATEGORY"), objUserInfo);
							
						}
					} 
					//else {
					final String sdeleteQuery = "update testpackagetest set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					                  + ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
					                  +"' where ntestpackagetestcode=" + objTestInstrumentCategory.getNtestpackagetestcode();
		//			final String sdeleteQuery = "delete from testinstrumentcategory where ntestinstrumentcatcode=" + objTestInstrumentCategory.getNtestinstrumentcatcode() + " and nstatus="
		//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(sdeleteQuery);
					
					final List<String> strArray= new ArrayList<>();
					final List<Object> objlst = new ArrayList<>();
					objlst.add(objTestInstCat);
					strArray.add("IDS_DELETETESTPACKAGE");
					auditUtilityFunction.fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);   
		            return new ResponseEntity<>(testMasterCommonFunction.getTestpackage(objTestInstrumentCategory.getNtestcode(), objUserInfo), HttpStatus.OK);
		//			}	
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		/**
		 * This method is used to get and update the sequence no (testsection, testpredefinedparameter, testparameternumeric, testparameter, 
		 * testmethod, testinstrumentcategory, testformula, testfile) for copy the test
		 * @param objTestMaster [TestMaster] holds the details of test
		 * @param objUserInfo [UserInfo] holds the object of loggedin userinfo
		 * @return map holds the sequence no of test and their details
		 */
		@Override
		public Map<String, Object> getSeqNoToCopyTestMaser(TestMaster objTestMaster, UserInfo objUserInfo,final int isQualisLite) throws Exception {
			
			//For_MSSQL
//			String sQuery = " select nlockcode from locktestmaster "+Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+"";
			//For_PostgreSQL
			String sQuery = "LOCK TABLE locktestmaster ";
			jdbcTemplate.execute(sQuery);
			
			Map<String, Object> outputMap = new HashMap<String, Object>();
			
			final TestMaster objTest =  testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			
			if(objTest == null) {
				returnStr = commonFunction.getMultilingualMessage("IDS_SELECTEDTESTISALREADYDELETED", objUserInfo.getSlanguagefilename());
			} else {
				final TestMaster objTestByName = testMasterCommonFunction.getTestByName(objTestMaster, objUserInfo);
				if(objTestByName==null){
					sQuery = "select * from seqnotestmanagement where stablename in ('testfile','testformula','testinstrumentcategory','testmethod','testparameter',"
							+"'testparameternumeric','testpredefinedparameter', 'testsection','testcontainertype','testpackagetest','testmasterclinicalspec')";
					List<SeqNoTestManagement> lstSeqNoTestMgmt = (List<SeqNoTestManagement>) jdbcTemplate.query(sQuery, new SeqNoTestManagement());
			        Map<String, Integer> seqMap = lstSeqNoTestMgmt.stream()
			                .collect(Collectors.toMap(SeqNoTestManagement::getStablename,
			                        seqNoConfigurationMaster -> seqNoConfigurationMaster.getNsequenceno()));

					outputMap.put("nTestFileSeqNo", seqMap.get("testfile"));
					outputMap.put("nTestFormulaSeqNo", seqMap.get("testformula"));
					outputMap.put("nTestInstCatSeqNo", seqMap.get("testinstrumentcategory"));
					outputMap.put("nTestMethodSeqNo", seqMap.get("testmethod"));
					outputMap.put("nTestParameterSeqNo", seqMap.get("testparameter"));
					outputMap.put("nTestSpecSeqNo", seqMap.get("testparameternumeric"));
					outputMap.put("nCodedResSeqNo", seqMap.get("testpredefinedparameter"));
					outputMap.put("nTestSectionSeqNo", seqMap.get("testsection"));
					outputMap.put("nTestContainerSeqNo", seqMap.get("testcontainertype"));
					outputMap.put("nTestPackageTestSeqNo", seqMap.get("testpackagetest"));				
					outputMap.put("nTestmasterclinicalspecSeqNo", seqMap.get("testmasterclinicalspec"));				

					sQuery = " select count(ntestsectioncode) ntestsectioncode from testsection where ntestcode = "+objTestMaster.getNtestcode()+""
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				
					List<TestSection> lstTestSection =jdbcTemplate.query(sQuery, new TestSection());
					
	 				sQuery =  "select count(ntestmethodcode) ntestmethodcode from testmethod where ntestcode = "+objTestMaster.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestMethod> lstTestMethod =jdbcTemplate.query(sQuery, new TestMethod());

					sQuery =  "select count(ntestinstrumentcatcode) ntestinstrumentcatcode from testinstrumentcategory where ntestcode = "+objTestMaster.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestInstrumentCategory> lstTestInstrumentCategory =jdbcTemplate.query(sQuery, new TestInstrumentCategory());

					sQuery =  "select count(ntestfilecode) ntestfilecode from testfile where ntestcode = "+objTestMaster.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestFile> lstTestFile =jdbcTemplate.query(sQuery, new TestFile());
					
					sQuery =  "select count(ntestpackagetestcode) ntestpackagetestcode from testpackagetest where ntestcode = "+objTestMaster.getNtestcode()
					   	   +" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestPackageTest> lstTestPackageTest =jdbcTemplate.query(sQuery, new TestPackageTest());
					
					sQuery =  "select count(ntestcontainertypecode) ntestcontainertypecode from testcontainertype where ntestcode = "+objTestMaster.getNtestcode()
						   +" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestContainerType> lstTestContainerType =jdbcTemplate.query(sQuery, new TestContainerType());

					sQuery =  "select count(ntestparametercode) ntestparametercode from testparameter where ntestcode = "+objTestMaster.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestParameter> lstTestParameter =jdbcTemplate.query(sQuery, new TestParameter());

					sQuery = "select count(ntestparamnumericcode) ntestparamnumericcode from testparameter tp, testparameternumeric tpn"
							+ " where tp.ntestparametercode = tpn.ntestparametercode and tpn.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.ntestcode = "+objTestMaster.getNtestcode()+" and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestParameterNumeric> lstTestParameterNumeric =jdbcTemplate.query(sQuery, new TestParameterNumeric());

					sQuery =  "select count(ntestpredefinedcode) ntestpredefinedcode from testparameter tp, testpredefinedparameter tpp"
							+ " where tp.ntestparametercode = tpp.ntestparametercode and tpp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.ntestcode = "+objTestMaster.getNtestcode()+" and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					List<TestPredefinedParameter> lstTestPredefinedParameter =jdbcTemplate.query(sQuery, new TestPredefinedParameter());

					sQuery =  "select count(ntestformulacode) ntestformulacode from testparameter tp, testformula tf"
							+ " where tp.ntestparametercode = tf.ntestparametercode and tf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.ntestcode = "+objTestMaster.getNtestcode()+" and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					List<TestFormula> lstTestFormula =jdbcTemplate.query(sQuery, new TestFormula());
					
					sQuery = "select count(ntestmasterclinicspeccode) ntestmasterclinicspeccode from testparameter tp, testmasterclinicalspec tpn"
							+ " where tp.ntestparametercode = tpn.ntestparametercode and tpn.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.ntestcode = "+objTestMaster.getNtestcode()+" and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
					List<TestMasterClinicalSpec> lstTestMasterClinicalSpec =jdbcTemplate.query(sQuery, new TestMasterClinicalSpec());

					
					String sUpdateQuery = "";
					final List<TestSection> listTestSection = lstTestSection;
					final List<TestMethod> listTestMethod = lstTestMethod;
					final List<TestInstrumentCategory> listTestInstCat = lstTestInstrumentCategory;
					final List<TestFile> listTestFile = lstTestFile;
					final List<TestPackageTest> listTestPackageTest =lstTestPackageTest;
					final List<TestContainerType> listTestContainerTypes =lstTestContainerType;
					final List<TestParameter> listTestParam = lstTestParameter;
					final List<TestParameterNumeric> listTestParamNum =lstTestParameterNumeric;
					final List<TestPredefinedParameter> listTestPredefParam = lstTestPredefinedParameter;
					final List<TestFormula> listTestFormula = lstTestFormula;
					final List<TestMasterClinicalSpec> listTestMasterClinicalSpec = lstTestMasterClinicalSpec;

					if(!listTestSection.isEmpty()) {
						sUpdateQuery = "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testsection") + listTestSection.get(0).getNtestsectioncode())+" where stablename=N'testsection'"+";";	
					}
					if(!listTestMethod.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testmethod") + listTestMethod.get(0).getNtestmethodcode())+" where stablename=N'testmethod'"+";";
					}
					if(!listTestInstCat.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testinstrumentcategory") + listTestInstCat.get(0).getNtestinstrumentcatcode())+" where stablename=N'testinstrumentcategory'"+";";
					}
					if(!listTestFile.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testfile") + listTestFile.get(0).getNtestfilecode())+" where stablename=N'testfile'"+";";
					}
					if(!listTestPackageTest.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testpackagetest") + listTestPackageTest.get(0).getNtestpackagetestcode())+" where stablename=N'testpackagetest'"+";";
					}
					if(!listTestContainerTypes.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testcontainertype") + listTestContainerTypes.get(0).getNtestcontainertypecode())+" where stablename=N'testcontainertype'"+";";
					}
					if(!listTestParam.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testparameter") + listTestParam.get(0).getNtestparametercode())+" where stablename=N'testparameter'"+";";
					}
					if(!listTestParamNum.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testparameternumeric") + listTestParamNum.get(0).getNtestparamnumericcode())+" where stablename=N'testparameternumeric'"+";";
					}
					if(!listTestPredefParam.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testpredefinedparameter") + listTestPredefParam.get(0).getNtestpredefinedcode())+" where stablename=N'testpredefinedparameter'"+";";
					}
					if(!listTestFormula.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testformula") + listTestFormula.get(0).getNtestformulacode())+" where stablename=N'testformula'"+";";
					}
					
					if(!listTestMasterClinicalSpec.isEmpty()) {
						sUpdateQuery = sUpdateQuery + "update seqnotestmanagement set nsequenceno = "+(seqMap.get("testmasterclinicalspec") + listTestMasterClinicalSpec.get(0).getNtestmasterclinicspeccode())+" where stablename=N'testmasterclinicalspec'"+";";

					}
					//ALPD-4831--Vignesh R(10-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String sQuery1 = "select nsequenceno,stablename from seqnotestgroupmanagement where stablename in (N'testgrouptest',"
								+ "N'testgrouptestcharparameter',N'testgrouptestfile',N'testgrouptestformula',N'testgrouptestnumericparameter',N'testgrouptestparameter',"
								+ "N'testgrouptestpredefparameter','testgrouptestclinicalspec')";
						
						List<SeqNoTestGroupmanagement> lstSeqNo =  jdbcTemplate.query(sQuery1,new SeqNoTestGroupmanagement());
						Map<String, Integer> seqMap1 = lstSeqNo.stream().collect(Collectors.toMap(SeqNoTestGroupmanagement::getStablename,seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
						
						sUpdateQuery=sUpdateQuery+"update seqnotestgroupmanagement set nsequenceno ="+(seqMap1.get("testgrouptest")+1)+" where stablename ='testgrouptest';";
						
						outputMap.put("ntestgrouptestcode", seqMap1.get("testgrouptest"));
						
						//ALPD-4831--Vignesh R(10-10-2024)--sequence number passed to the insertion.
						
						outputMap.put("ntestgrouptestcharparametercode", seqMap1.get("testgrouptestcharparameter"));
						outputMap.put("ntestgrouptestfilecode", seqMap1.get("testgrouptestfile"));
						outputMap.put("ntestgrouptestformulacode", seqMap1.get("testgrouptestformula"));
						outputMap.put("ntestgrouptestnumericparametercode", seqMap1.get("testgrouptestnumericparameter"));
						outputMap.put("ntestgrouptestparametercode", seqMap1.get("testgrouptestparameter"));
						outputMap.put("ntestgrouptestpredefparametercode", seqMap1.get("testgrouptestpredefparameter"));
						outputMap.put("ntestgroupfilecode", seqMap1.get("testgrouptestfile"));
						outputMap.put("ntestgrouptestclinicalspeccode", seqMap1.get("testgrouptestclinicalspec"));

						if(!listTestParam.isEmpty()) {

								sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestparameter") + listTestParam.get(0).getNtestparametercode())+" where stablename='testgrouptestparameter';";
							
								
								//ALPD-4831--Vignesh R(10-10-2024)--testgrouptestcharparameter sequence updation only character parameter
								sQuery =  "select count(ntestparametercode) ntestparametercode from testparameter where ntestcode = "+objTestMaster.getNtestcode()
								+" and nparametertypecode="+Enumeration.ParameterType.CHARACTER.getparametertype()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
								List<TestParameter> lstTestParameter1 =jdbcTemplate.query(sQuery, new TestParameter());
								
								
								if(!lstTestParameter1.isEmpty()){
										sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestcharparameter") + lstTestParameter1.get(0).getNtestparametercode())+" where stablename='testgrouptestcharparameter';";

										}
							
								sQuery =  "select count(ntestparametercode) ntestparametercode from testparameter where ntestcode = "+objTestMaster.getNtestcode()
								+" and nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
								List<TestParameter> lstTestParameter2 =jdbcTemplate.query(sQuery, new TestParameter());
								
								//ALPD-4831--Vignesh R(10-10-2024)--testgrouptestnumericparameter  sequence updation only numeric parameter
								if(!lstTestParameter2.isEmpty()) {
									sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestnumericparameter") + lstTestParameter2.get(0).getNtestparametercode())+" where stablename='testgrouptestnumericparameter';";
									
								}
								
								//ALPD-4831,5873--Vignesh R(10-10-2024)--testgrouptestfile  sequence updation which file has ndefaulstatus set to 3.
								if(!lstTestPredefinedParameter.isEmpty()) {
									sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestpredefparameter") + lstTestPredefinedParameter.get(0).getNtestpredefinedcode())+" where stablename='testgrouptestpredefparameter';";

								}
								
								sQuery =  "select count(ntestfilecode) ntestfilecode from testfile where ntestcode = "+objTestMaster.getNtestcode()
								+" and ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
								List<TestFile> lstTestFile1 =jdbcTemplate.query(sQuery, new TestFile());
						
								//test file
								if(!lstTestFile1.isEmpty()) {
									sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestfile") + lstTestFile1.get(0).getNtestfilecode())+" where stablename='testgrouptestfile';";

									}
								
								
								//test formula
								sQuery =  "select count(ntestformulacode) ntestformulacode from testparameter tp, testformula tf"
										+ " where tp.ntestparametercode = tf.ntestparametercode and tf.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and  tf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tp.ntestcode = "+objTestMaster.getNtestcode()+" and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
								List<TestFormula> lstTestFormula1 =jdbcTemplate.query(sQuery, new TestFormula());
								
								if(!lstTestFormula1.isEmpty()) {
									sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestformula") + lstTestFormula1.get(0).getNtestformulacode())+" where stablename='testgrouptestformula';";

									}
								
								//testclinical
								if(!listTestMasterClinicalSpec.isEmpty()) {
									sUpdateQuery = sUpdateQuery + "update seqnotestgroupmanagement set nsequenceno = "+(seqMap1.get("testgrouptestclinicalspec") +  listTestMasterClinicalSpec.get(0).getNtestmasterclinicspeccode())+" where stablename=N'testgrouptestclinicalspec'"+";";

								}
								}
					}
					
					jdbcTemplate.execute(sUpdateQuery);
				} else {
					returnStr = commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), objUserInfo.getSlanguagefilename());
				}
				
			}
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), returnStr);
			return outputMap;
		}
		
		/**
		 * This method is used to copy the test details
		 * the details of selected test in UI is copied to the new test
		 * The selected test of testparameter, test section, test method, test instrumentcategory, test file, 
		 * testpredefinedparameter/ codedresult, test formula, testparameternumeric
		 * @param objTestMaster [TestMaster] object holds the test details
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testmaster', 'testparameter', 'testformula', 'testnumericparameter', 'testpredefinedparameter' entity
		 */
		@Override
		public ResponseEntity<Object> copyTestMaster(final TestMaster objTestMaster, final UserInfo objUserInfo, Map<String, Object> inputMap) throws Exception {
			
			final String sTableLockQuery = " lock  table locktestmaster "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sTableLockQuery);
			//final ObjectMapper objMapper = new ObjectMapper();

			final int nCopyTestcode=objTestMaster.getNtestcode();
			final int nParseqno = (int) inputMap.get("nTestParameterSeqNo");
			final int isQualisLite = (int) inputMap.get("isQualisLite");

			//Test Master
			String seqtestquery ="select nsequenceno from SeqNoTestManagement where stablename='testmaster'";
			int nseqtestcount =(int) jdbcUtilityFunction.queryForObject(seqtestquery, Integer.class,jdbcTemplate);
			nseqtestcount++;
			
//			String inserttestquery =" Insert into testmaster(ntestcode,ntestcategorycode,nchecklistversioncode,naccredited,stestname,stestsynonym,sdescription,ncost,"
//								   +" ntransactionstatus,nsitecode,nstatus)"
//								   + " values ("+nseqtestcount+","+objTestMaster.getNtestcategorycode()+","+objTestMaster.getNchecklistversioncode()+","+objTestMaster.getNaccredited()+",N'"+ReplaceQuote(objTestMaster.getStestname())+"',"
//								   + " N'"+ReplaceQuote(objTestMaster.getStestsynonym())+"',N'"+ReplaceQuote(objTestMaster.getSdescription())+"',"+objTestMaster.getNcost()+","+objTestMaster.getNtransactionstatus()+","+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
			String inserttestquery =" Insert into testmaster(ntestcode,ntestcategorycode,nchecklistversioncode,naccredited,stestname,stestsynonym,sshortname,"
					+ " sdescription,ncost,stestplatform,ntat,ntatperiodcode,ninterfacetypecode,dmodifieddate"
					   +" ,ntransactionstatus,ntrainingneed,nsitecode,nstatus)"
					   + " values ("+nseqtestcount+","+objTestMaster.getNtestcategorycode()+","+objTestMaster.getNchecklistversioncode()+","+objTestMaster.getNaccredited()+",N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestname())+"',"
					   + " N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestsynonym())
					   + "', N'"+stringUtilityFunction.replaceQuote(objTestMaster.getSshortname())
					   +"',N'"+stringUtilityFunction.replaceQuote(objTestMaster.getSdescription())+"',"+objTestMaster.getNcost()+",N'"+stringUtilityFunction.replaceQuote(objTestMaster.getStestplatform())+"',"+objTestMaster.getNtat()+","+objTestMaster.getNtatperiodcode()+","+objTestMaster.getNinterfacetypecode()+",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objTestMaster.getNtransactionstatus()+","+objTestMaster.getNtrainingneed()+","+objUserInfo.getNmastersitecode()+","
					   + " "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";

			jdbcTemplate.execute(inserttestquery);

			String updatetestquery="update SeqNoTestManagement set nsequenceno ="+nseqtestcount+" where stablename ='testmaster'";
			jdbcTemplate.execute(updatetestquery);
			objTestMaster.setNtestcode(nseqtestcount);
			
			
			//insertObject(objTestMaster, SeqNoTestManagement.class, "nsequenceno");
			final int ntestcode=objTestMaster.getNtestcode();
			//Test Section		
			String sInsQuery = "insert into testsection (ntestsectioncode, ntestcode, nsectioncode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"
					+ " select "+(int) inputMap.get("nTestSectionSeqNo")+"+RANK() over(order by ntestsectioncode) ntestsectioncode, "+ ntestcode
					+ " ntestcode, nsectioncode, ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+", nstatus"
					+ " from testsection where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Test Method	
			sInsQuery = "insert into testmethod (ntestmethodcode, ntestcode, nmethodcode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"
					+ " select "+(int) inputMap.get("nTestMethodSeqNo")+"+RANK() over(order by ntestmethodcode) ntestmethodcode,"+ntestcode
					+ " ntestcode, nmethodcode, ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", nstatus"
					+ " from testmethod  where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Test InstrumentCategory			
			sInsQuery =  "insert into testinstrumentcategory (ntestinstrumentcatcode, ntestcode, ninstrumentcatcode, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"
					+ " select "+(int) inputMap.get("nTestInstCatSeqNo")+"+RANK() over(order by ntestinstrumentcatcode) ntestinstrumentcatcode,"+ntestcode
					+ " ntestcode, ninstrumentcatcode, ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+", nstatus"
					+ " from testinstrumentcategory where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(sInsQuery);
			//Test File	
			sInsQuery =  "insert into testfile (ntestfilecode, ntestcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate, ssystemfilename, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"
					+ " select "+(int) inputMap.get("nTestFileSeqNo")+"+RANK() over(order by ntestfilecode) ntestfilecode,"+ntestcode+" ntestcode, nlinkcode, nattachmenttypecode, sfilename,"
					+ " sdescription, nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate, ssystemfilename, ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", nstatus" 
					+ " from testfile where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Test Package
			sInsQuery =  "insert into testpackagetest(ntestpackagetestcode,ntestcode,ntestpackagecode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					  + "select "+(int)inputMap.get("nTestPackageTestSeqNo")+"+RANK() over(order by ntestpackagetestcode)ntestpackagetestcode,"+ntestcode+" ntestcode,"
					  + "ntestpackagecode,ndefaultstatus," 
					  + "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+",nstatus "
					  + "from testpackagetest where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Test ContainerType
			sInsQuery ="insert into testcontainertype (ntestcontainertypecode,ntestcode,ncontainertypecode,nquantity,nunitcode,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ " select "+(int)inputMap.get("nTestContainerSeqNo")+"+RANK() over(order by ntestcontainertypecode)ntestcontainertypecode,"+ntestcode+" ntestcode,"
					+ " ncontainertypecode,nquantity,nunitcode,ndefaultstatus,"
					+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+",nstatus "
					+ "from testcontainertype where ntestcode="+nCopyTestcode+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(sInsQuery);

		
		
			//Test Parameter	
//			sInsQuery =  "insert into testparameter (ntestparametercode, ntestcode, nparametertypecode, nunitcode, sparametername, "
//					+ "sparametersynonym, nroundingdigits, nisadhocparameter, nisvisible, nstatus)"
//					+ " select "+nParseqno+"+RANK() over(order by ntestparametercode) ntestparametercode, "+ntestcode+" ntestcode, nparametertypecode,"
//					+ " nunitcode, sparametername, sparametersynonym, nroundingdigits, nisadhocparameter, nisvisible, nstatus"
//					+ " from testparameter where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			
			
//			sInsQuery =  "insert into testparameter (ntestparametercode, ntestcode, nparametertypecode, nunitcode, sparametername, "
//					+ "sparametersynonym, nroundingdigits, nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible,nstatus,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate)"
//					+ " select "+nParseqno+"+RANK() over(order by ntestparametercode) ntestparametercode, "+ntestcode+" ntestcode, nparametertypecode,"
//					+ " nunitcode, sparametername, sparametersynonym, nroundingdigits, nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible, nstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//					+ " from testparameter where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			
			sInsQuery =  "insert into testparameter (ntestparametercode, ntestcode, nparametertypecode, nunitcode,ndestinationunitcode,noperatorcode,nconversionfactor, sparametername, "
					+ "sparametersynonym, nroundingdigits, nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible,nsitecode,nstatus,dmodifieddate,nresultaccuracycode)"
					+ " select "+nParseqno+"+RANK() over(order by ntestparametercode) ntestparametercode, "+ntestcode+" ntestcode, nparametertypecode,"
					+ " nunitcode,ndestinationunitcode,noperatorcode,nconversionfactor, sparametername, sparametersynonym, nroundingdigits, nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible,"+objUserInfo.getNmastersitecode()+", nstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nresultaccuracycode "
					+ " from testparameter where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			
			jdbcTemplate.execute(sInsQuery);
			//Test ParameterNumeric	
			// ALPD-5423 - added tpn.ngradecode by Gowtham R on 24/03/2025 - Specification Grade is not copied in the new test master record.
			sInsQuery =  " insert into testparameternumeric (ntestparamnumericcode, ntestparametercode, sminlod, smaxlod, sminb, smina, smaxa, smaxb, "
					+ " sminloq, smaxloq, sdisregard, ngradecode, sresultvalue, dmodifieddate, nsitecode, nstatus)"
					+ " select "+(int) inputMap.get("nTestSpecSeqNo")+"+RANK() over(order by tpn.ntestparamnumericcode) ntestparamnumericcode, tp.c_ntestparametercode, tpn.sminlod, tpn.smaxlod, "
					+ " tpn.sminb,tpn.smina, tpn.smaxa, tpn.smaxb, tpn.sminloq, tpn.smaxloq, tpn.sdisregard, tpn.ngradecode, tpn.sresultvalue,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+", tpn.nstatus "
					+ " from testparameternumeric tpn join (select "+nParseqno+"+RANK() over(order by ntestparametercode) c_ntestparametercode, ntestparametercode"
					+ " from testparameter where ntestcode= "+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") tp"
					+ " on tp.ntestparametercode = tpn.ntestparametercode and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			jdbcTemplate.execute(sInsQuery);
			
	        //ALPD-5053 added by Dhanushya RI,To insert fromage period and toage period
			sInsQuery= " Insert into testmasterclinicalspec(ntestmasterclinicspeccode, ntestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb,"
					+ " sresultvalue, ngradecode, sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate, dmodifieddate, nsitecode, nstatus,nfromageperiod,ntoageperiod)"
					+ " select " + (int) inputMap.get("nTestmasterclinicalspecSeqNo")+"+RANK() over(order by tpn.ntestmasterclinicspeccode) ntestmasterclinicspeccode,tp.c_ntestparametercode"
					+ ",ngendercode,nfromage,ntoage,shigha,shighb,slowa,slowb,sresultvalue,ngradecode ,sminlod,smaxlod,sminloq,smaxloq,sdisregard,ntzmodifieddate,noffsetdmodifieddate,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nsitecode,nstatus,nfromageperiod,ntoageperiod "
					+" from testmasterclinicalspec tpn join (select "+nParseqno+"+RANK() over(order by ntestparametercode) c_ntestparametercode, ntestparametercode"
					+ " from testparameter where ntestcode= "+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") tp"
					+ " on tp.ntestparametercode = tpn.ntestparametercode and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					
			jdbcTemplate.execute(sInsQuery);

			//Test PredefinedParameter	
//			sInsQuery =  "insert into testpredefinedparameter (ntestpredefinedcode, ntestparametercode, ngradecode, spredefinedname,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,ndefaultstatus, nstatus)"
//					+ " select "+(int) inputMap.get("nCodedResSeqNo")+"+RANK() over(order by tpp.ntestpredefinedcode) ntestpredefinedcode, tp.c_ntestparametercode, tpp.ngradecode,"
//					+ "tpp.spredefinedname,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())+",tpp.ndefaultstatus, tpp.nstatus"
//					+ " from testpredefinedparameter tpp join (select "+nParseqno+"+RANK() over(order by ntestparametercode) c_ntestparametercode, ntestparametercode"
//					+ " from testparameter where ntestcode="+nCopyTestcode+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") tp on tp.ntestparametercode = tpp.ntestparametercode and tpp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			sInsQuery =  "insert into testpredefinedparameter (ntestpredefinedcode, ntestparametercode, ngradecode, spredefinedname,spredefinedsynonym,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
					+ " select "+(int) inputMap.get("nCodedResSeqNo")+"+RANK() over(order by tpp.ntestpredefinedcode) ntestpredefinedcode, tp.c_ntestparametercode, tpp.ngradecode,"
					+ "tpp.spredefinedname,tpp.spredefinedsynonym,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',tpp.ndefaultstatus,"+objUserInfo.getNmastersitecode()+", tpp.nstatus"
					+ " from testpredefinedparameter tpp join (select "+nParseqno+"+RANK() over(order by ntestparametercode) c_ntestparametercode, ntestparametercode"
					+ " from testparameter where ntestcode="+nCopyTestcode+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") tp on tp.ntestparametercode = tpp.ntestparametercode and tpp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Test Formula	
			sInsQuery =  "insert into testformula (ntestformulacode, ntestcode, ntestparametercode, sformulaname, sformulacalculationcode, sformulacalculationdetail, npredefinedformulacode, nispredefinedformula, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"+
					"select "+(int) inputMap.get("nTestFormulaSeqNo")+"+RANK() over(order by tf.ntestformulacode) ntestformulacode,"+ntestcode+" as ntestcode,tp.c_ntestparametercode,"+
					"tf.sformulaname, tf.sformulacalculationcode, tf.sformulacalculationdetail,tf.npredefinedformulacode ,tf.nispredefinedformula ,tf.ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+", tf.nstatus" +
					" from testformula tf join (select * from(select "+nParseqno+"+RANK() over(order by ntestparametercode) c_ntestparametercode, nparametertypecode,"+
					" ntestparametercode from testparameter where ntestcode="+nCopyTestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
					") a where nparametertypecode="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") tp on tp.ntestparametercode = tf.ntestparametercode " +
					" and tf.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			jdbcTemplate.execute(sInsQuery);
			//Report Info Test
			sInsQuery = " insert into reportinfotest (ntestcode, sconfirmstatement, sdecisionrule, ssopdescription, stestcondition, sdeviationcomments, smethodstandard, sreference, dmodifieddate, nsitecode, nstatus) " +
					 	" select "+ntestcode+" as  ntestcode,sconfirmstatement, sdecisionrule, ssopdescription, stestcondition, " +
					 	" sdeviationcomments, smethodstandard, sreference, '"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', "+objUserInfo.getNmastersitecode()+", nstatus " +
					 	" from  reportinfotest where ntestcode = "+nCopyTestcode+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " ";
			jdbcTemplate.execute(sInsQuery);

			
			String query ="";
			
			query="select * from testmaster where ntestcode ="+ntestcode+"";
			List <TestMaster> lstTestMaster =jdbcTemplate.query(query, new TestMaster());
			//commented by sonia on 17th April 2025 						
//			query="select tp.ntestparametercode, tp.ntestcode, tp.nparametertypecode, tp.nunitcode, tp.sparametername, tp.sparametersynonym,"
//					+ " tp.nroundingdigits, tp.nisadhocparameter, tp.ndeltachecklimitcode, tp.ndeltacheck, tp.ndeltacheckframe, tp.ndeltaunitcode,"
//					+ " tp.nisvisible, tp.nsitecode, tp.nstatus, pt.sdisplaystatus,tp.nresultaccuracycode  from testparameter tp, parametertype pt where ntestcode ="+ntestcode
//					+ " and tp.nparametertypecode=pt.nparametertypecode";
//			List <TestParameter> lstTestParameter = objMapper.convertValue(commonFunction.getMultilingualMessageList(jdbcTemplate.query(
//					query, new TestParameter()),Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),new TypeReference<List<TestParameter>>() {});

			query="select * from testsection where ntestcode="+ntestcode+"";
			List <TestSection> lstTestSection =jdbcTemplate.query(query, new TestSection());

			query="select * from testmethod where ntestcode="+ntestcode+"";
			List <TestMethod> lstTestMethod =jdbcTemplate.query(query, new TestMethod());
			
			query="select * from testinstrumentcategory where ntestcode ="+ntestcode+"";
			List <TestInstrumentCategory> lstTestInstrumentCategory =jdbcTemplate.query(query, new TestInstrumentCategory());

			//commented by sonia on 17th April 2025 						
//			query="select * from testfile where ntestcode ="+ntestcode+"";
//			List <TestFile> lstTestFile =jdbcTemplate.query(query, new TestFile());
			
			query="select * from testpackagetest where ntestcode ="+ntestcode+"";
			List <TestPackageTest> lstTestPackageTest =jdbcTemplate.query(query, new TestPackageTest());
			
			query="select * from testcontainertype where ntestcode ="+ntestcode+"";
			List <TestContainerType> lstTestContainerType =jdbcTemplate.query(query, new TestContainerType());

			//commented by sonia on 17th April 2025 						
//			query="select tpp.* from testpredefinedparameter tpp, testparameter tp where tp.ntestparametercode = tpp.ntestparametercode and tp.ntestcode = "+ntestcode+"";
//			List <TestPredefinedParameter> lstTestPredefinedParameter =jdbcTemplate.query(query, new TestPredefinedParameter());
//
//			query="select * from testformula where ntestcode ="+ntestcode+"";
//			List <TestFormula> lstTestFormula =jdbcTemplate.query(query, new TestFormula());

//			query="select tpn.* from testparameter tp, testparameternumeric tpn where tp.ntestparametercode = tpn.ntestparametercode and tp.ntestcode = "+ntestcode+"";
//			List <TestParameterNumeric> lstTestParameterNumeric =jdbcTemplate.query(query, new TestParameterNumeric());
//			
//			query="select * from reportinfotest where ntestcode ="+ntestcode+"";
//			List <ReportInfoTest> lstReportInfoTest =jdbcTemplate.query(query, new ReportInfoTest());
			
			//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and, the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
			if(isQualisLite== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				
				final String sQuery1 =  "select * from testparameter where ntestcode = "+nCopyTestcode
				+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";	
				List<TestParameter> lstTestParameter1 =jdbcTemplate.query(sQuery1, new TestParameter()); 
				
				int nstatus=Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				 if(lstTestParameter1.isEmpty()){
					nstatus=Enumeration.TransactionStatus.NA.gettransactionstatus();
				}
				
				
				int nsectioncode = lstTestSection.stream()
					    .filter(a -> a.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					    .findFirst()
					    .map(a -> a.getNsectioncode())
					    .orElse( Enumeration.TransactionStatus.NA.gettransactionstatus());
				int nmethodcode = lstTestMethod.stream()
					    .filter(a -> a.getNdefaultstatus() ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
					    .findFirst()
					    .map(a -> a.getNmethodcode())
					    .orElse( Enumeration.TransactionStatus.NA.gettransactionstatus());
				int ninstcatcode = lstTestInstrumentCategory.stream()
					    .filter(a -> a.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					    .findFirst()
					    .map(a -> a.getNinstrumentcatcode())
					    .orElse( Enumeration.TransactionStatus.NA.gettransactionstatus());
				
				int ncontainertypecode = lstTestContainerType.stream()
					    .filter(a -> a.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					    .findFirst()
					    .map(a -> a.getNcontainertypecode())
					    .orElse( Enumeration.TransactionStatus.NA.gettransactionstatus());
				
				int ntestpackagecode = lstTestPackageTest.stream()
					    .filter(a -> a.getNdefaultstatus() ==  Enumeration.TransactionStatus.YES.gettransactionstatus())
					    .findFirst()
					    .map(a -> a.getNtestpackagecode())
					    .orElse( Enumeration.TransactionStatus.NA.gettransactionstatus());
				int nsorter=0;
				String nsortter="select * from testgrouptest where ntestgrouptestcode="+(int) inputMap.get("nTestFormulaSeqNo")+" and nstatus=1";
				TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(nsortter,TestGroupTest.class,jdbcTemplate);
				if(objecTestGroupTest==null) {
					nsorter=nsorter+1;
				}else {
					nsorter=objecTestGroupTest.getNsorter()+1;
				}
				final int ntestgrouptestcode=(int)inputMap.get("ntestgrouptestcode")+1;
				
				String sInsQuery1="insert into testgrouptest (ntestgrouptestcode, nspecsampletypecode, ntestcode, nsectioncode, nmethodcode, ninstrumentcatcode, "
						+ "ncontainertypecode,ntestpackagecode,stestsynonym, nrepeatcountno, ncost, nsorter, nisadhoctest, nisvisible, dmodifieddate,nsitecode,nstatus) values ("
						+ (ntestgrouptestcode) + ",(select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),"
						+ objTestMaster.getNtestcode() + "," + nsectioncode+"," + nmethodcode + "," + ninstcatcode+","+ncontainertypecode+","+ntestpackagecode
						+ ",N'" + stringUtilityFunction.replaceQuote(objTestMaster.getStestname()) + "',1, " + objTestMaster.getNcost()
						+ "," + nsorter + "," +Enumeration.TransactionStatus.NO.gettransactionstatus() +","
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()
						+ "," +nstatus+ ");";
				jdbcTemplate.execute(sInsQuery1);
				
				//ALPD-4831,5873--Vignesh R(10-10-2024)--Atleast one parameter should be available for the insert.
				
				if(!lstTestParameter1.isEmpty()){

				int testParameterSorter=0;
				sInsQuery1="insert into testgrouptestparameter (ntestgrouptestparametercode, ntestgrouptestcode, ntestparametercode, nparametertypecode, nunitcode, sparametersynonym,"
						+ "nroundingdigits, nresultmandatory, nreportmandatory, ngradingmandatory, nchecklistversioncode, sspecdesc, nsorter, nisadhocparameter, nisvisible,dmodifieddate, nstatus,nsitecode,nresultaccuracycode)"
						+ " select " + (int) inputMap.get("ntestgrouptestparametercode") + "+RANK() over(order by ntestparametercode) ntestgrouptestparametercode," + ntestgrouptestcode + ",ntestparametercode,nparametertypecode,nunitcode"
						+ ",sparametersynonym,nroundingdigits,"
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "," + " -1, NULL,"
						+  testParameterSorter + "+RANK() over(order by ntestparametercode),"+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+"nstatus,nsitecode,nresultaccuracycode  from testparameter where ntestcode="+ntestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntestparametercode;";
				
			
				jdbcTemplate.execute(sInsQuery1);

				
				
				sInsQuery1="insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate,nstatus,nsitecode)"
						+ " select " + (int) inputMap.get("ntestgrouptestcharparametercode") + "+Rank() over (order by ntestgrouptestparametercode) ntestgrouptestcharcode,ntestgrouptestparametercode ,NULL,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+" from testgrouptestparameter where nparametertypecode="+Enumeration.ParameterType.CHARACTER.getparametertype()+" "
						+ " and ntestgrouptestcode="+ntestgrouptestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntestgrouptestparametercode;";

				jdbcTemplate.execute(sInsQuery1);


				sInsQuery1="insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
						+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
						+ " select " + (int) inputMap.get("ntestgrouptestnumericparametercode") + "+Rank() over (order by ntestparamnumericcode) ntestgrouptestnumericcode, ntestgrouptestparametercode"
						+ ", sminlod, smaxlod, sminb, smina, smaxa,smaxb, sminloq, smaxloq, sdisregard, sresultvalue,"
						+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+",ngradecode "
						+ " from testgrouptestparameter tgt, testparameter tp, testparameternumeric tpn where tpn.ntestparametercode=tp.ntestparametercode "
						+ " and tgt.ntestparametercode=tp.ntestparametercode and tgt.ntestparametercode=tpn.ntestparametercode and  tgt.ntestgrouptestcode="+ntestgrouptestcode+" "
						+ " and tgt.nparametertypecode=tp.nparametertypecode "
						+ "  and  tgt.nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+" "
						+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by tpn.ntestparamnumericcode;";
				
				jdbcTemplate.execute(sInsQuery1);

				sInsQuery1="insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
						+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode) select " + (int) inputMap.get("ntestgrouptestpredefparametercode") + "+Rank() over(order by ntestpredefinedcode) ntestgrouptestpredefcode, ntestgrouptestparametercode,ngradecode,spredefinedname,"
						+ " spredefinedsynonym,ndefaultstatus,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+" from testpredefinedparameter tpp,testparameter tp,testgrouptestparameter tgtp where tp.ntestparametercode=tpp.ntestparametercode  "
						+ " and tgtp.ntestparametercode=tp.ntestparametercode and tgtp.ntestgrouptestcode = "+ntestgrouptestcode+"  and tgtp.nparametertypecode=tp.nparametertypecode and  tgtp.nparametertypecode="+Enumeration.ParameterType.PREDEFINED.getparametertype()+" "
						+ " and  tp.ntestcode="+ntestcode+" order by ntestpredefinedcode;";
				
				jdbcTemplate.execute(sInsQuery1);
				
				sInsQuery1="insert into testgrouptestfile (ntestgroupfilecode, ntestgrouptestcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, ssystemfilename, nfilesize, dcreateddate,noffsetdcreateddate,ntzcreateddate, dmodifieddate,nstatus,nsitecode)"
						+ " select " + (int) inputMap.get("ntestgroupfilecode") + "+Rank() over(order by ntestfilecode) ntestgroupfilecode," + ntestgrouptestcode+",nlinkcode,nattachmenttypecode,sfilename,sdescription, ssystemfilename, nfilesize,dcreateddate,noffsetdcreateddate"
						+ ",ntzcreateddate,dmodifieddate,nstatus,nsitecode from testfile where ntestcode = "+ntestcode+" and ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntestfilecode";
						
				jdbcTemplate.execute(sInsQuery1);

				 
				
				sInsQuery1="insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode,"
						+ " sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, dmodifieddate,nstatus,nsitecode)"
						+ " select " + (int) inputMap.get("ntestgrouptestformulacode") + "+Rank() over(order by ntestformulacode) ntestgrouptestformulacode," + ntestgrouptestcode+",ntestgrouptestparametercode,ntestformulacode,"
						+ " sformulacalculationcode,sformulacalculationdetail,"
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
						+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","+objUserInfo.getNmastersitecode()+ " from testgrouptestparameter tgt, testparameter tp, testformula tpn "
						+ "	where tpn.ntestparametercode=tp.ntestparametercode "
						+ " and tgt.ntestparametercode=tp.ntestparametercode and tgt.ntestparametercode=tpn.ntestparametercode and  tgt.ntestgrouptestcode="+ntestgrouptestcode+""
						+ "	and tgt.nparametertypecode=tp.nparametertypecode and  tpn.ntestcode="+ntestcode+"  and tpn.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
						+ "	and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ "	and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ "	and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntestformulacode;";
			   		
				jdbcTemplate.execute(sInsQuery1);
				
				
				
				sInsQuery1="insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
						+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
						+ " select (select max(ntestgrouptestnumericcode) from testgrouptestnumericparameter) +Rank() over (order by ntestgrouptestparametercode) ntestgrouptestnumericcode, ntestgrouptestparametercode"
						+ ", NULL, NULL, NULL, NULL, NULL,NULL, NULL, NULL, NULL, NULL,"
						+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+",-1 from testgrouptestparameter where ntestparametercode not in( "
						+ " select  tgt.ntestparametercode  from testgrouptestparameter tgt, testparameter tp, testparameternumeric tpn where tpn.ntestparametercode=tp.ntestparametercode "
						+ " and tgt.ntestparametercode=tp.ntestparametercode and tgt.ntestparametercode=tpn.ntestparametercode and  tgt.ntestgrouptestcode="+ntestgrouptestcode+" "
						+ " and tgt.nparametertypecode=tp.nparametertypecode "
						+ "  and  tgt.nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+" "
						+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ " and nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+" and ntestgrouptestcode="+ntestgrouptestcode+"  order by ntestgrouptestparametercode";
			
				jdbcTemplate.execute(sInsQuery1);

	        //ALPD-5053 added by Dhanushya RI,To insert fromage period and toage period
				sInsQuery1="insert into testgrouptestclinicalspec (ntestgrouptestclinicspeccode, ntestgrouptestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb, sresultvalue,"
						+ " sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate,dmodifieddate, nstatus,nsitecode, ngradecode,nfromageperiod,ntoageperiod)"
						+ " select "+(int) inputMap.get("ntestgrouptestclinicalspeccode")+"+Rank() over(order by ntestmasterclinicspeccode) ntestgrouptestclinicspeccode,ntestgrouptestparametercode,ngendercode,nfromage,ntoage,shigha,shighb,slowa,slowb,sresultvalue," 
						+ "sminlod,smaxloq,smaxlod,sminloq,sdisregard,ntzmodifieddate,noffsetdmodifieddate,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+",ngradecode,nfromageperiod,ntoageperiod "
						+ " from testgrouptestparameter tgt, testparameter tp, testmasterclinicalspec tpn where tpn.ntestparametercode=tp.ntestparametercode "
				     	+ " and tgt.ntestparametercode=tp.ntestparametercode and tgt.ntestparametercode=tpn.ntestparametercode and  tgt.ntestgrouptestcode="+ntestgrouptestcode+" "
				     	+ " and tgt.nparametertypecode=tp.nparametertypecode "
				     	+ "  and  tgt.nparametertypecode="+Enumeration.ParameterType.NUMERIC.getparametertype()+" "
				     	+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				     	+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				     	+ " and tpn.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by tpn.ntestmasterclinicspeccode;";
				
				jdbcTemplate.execute(sInsQuery1);

		}

			
			}
				
			
			
			List<Object> lstAllObject =new ArrayList<Object>();
			lstAllObject.add(lstTestMaster);
//			ALPD-5423 - Gowtham R - Test master --> While copy the test 500 occurs for specific scenario. Check description.
//			lstAllObject.add(lstTestParameter);
//			lstAllObject.add(lstTestSection);
//			lstAllObject.add(lstTestMethod);
//			lstAllObject.add(lstTestInstrumentCategory);
//			lstAllObject.add(lstTestFile);
//			lstAllObject.add(lstTestPackageTest);
//			lstAllObject.add(lstTestContainerType);
//			lstAllObject.add(lstTestPredefinedParameter);
//			lstAllObject.add(lstTestFormula);
//			lstAllObject.add(lstTestParameterNumeric);
//			lstAllObject.add(lstReportInfoTest);
//			
//			fnInsertListAuditAction(lstAllObject, 1,null, Arrays.asList("IDS_COPYTESTMASTER", "IDS_COPYTESTPARAMETER", "IDS_COPYTESTSECTION", "IDS_COPYTESTMETHOD",
//						"IDS_COPYTESTINSTRUMENTCATEGORY", "IDS_COPYTESTFILE","IDS_COPYTESTPACKAGETEST","IDS_COPYTESTCONTAINERTYPE", "IDS_COPYTESTPREDEFINEDPARAMETER", "IDS_COPYTESTFORMULA","IDS_COPYSPECLIMIT","IDS_COPYREPORTINFOTEST"), objUserInfo);
			
			auditUtilityFunction.fnInsertListAuditAction(lstAllObject, 1,null, Arrays.asList("IDS_COPYTESTMASTER"), objUserInfo);
			
			return testMasterCommonFunction.getTestMasterAfterInsert(objTestMaster, false, objUserInfo);
		}

		/**
		 * This method definition is used to set a default test section
		 * Need to validate that the specified test, it's test section is active before updating the default status.
		 * Need to validate the specified test section is already default
		 * @param objTestSection [TestSection] object holds the details for test section to set default
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testsection' entity
		 * @throws Exception
		 */
		@Override
		public ResponseEntity<Object> setDefaultTestSection(final TestSection objTestSection, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestSection.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testsection where ntestsectioncode = "+objTestSection.getNtestsectioncode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestSection objTS = (TestSection) jdbcUtilityFunction.queryForObject(sQuery, TestSection.class,jdbcTemplate);
				if(objTS != null) {
					if(objTS.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						sQuery = "select * from testsection where ntestcode = "+objTestSection.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ntestsectioncode <> "+objTestSection.getNtestsectioncode()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final List<TestSection> lstTestSection = (List<TestSection>) jdbcTemplate.query(sQuery, new TestSection());
						String sUpdateQuery = "";
						if(lstTestSection != null && lstTestSection.size() > 0) {
							lstOldObject.add(SerializationUtils.clone(lstTestSection.get(0)));
							TestSection objNewTestSection = new TestSection();
							objNewTestSection = lstTestSection.get(0);
							sUpdateQuery = "update testsection set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
									+", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) +"' where ntestsectioncode = "+objNewTestSection.getNtestsectioncode()+";";
							jdbcTemplate.execute(sUpdateQuery);
							objNewTestSection.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestSection);
							//multilingualIDList.add("IDS_EDITTESTLAB");
							multilingualIDList.add("IDS_EDITTESTSECTION");
						}
						sUpdateQuery =  "update testsection set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)	+"' where ntestsectioncode = "+objTestSection.getNtestsectioncode()+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestSection.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
								sUpdateQuery=sUpdateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nsectioncode="+objTestSection.getNsectioncode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
						
						}
						jdbcTemplate.execute(sUpdateQuery);
						//multilingualIDList.add("IDS_EDITTESTLAB");
						multilingualIDList.add("IDS_EDITTESTSECTION");
						if(objTestSection.getNdefaultstatus()==3)
							objTestSection.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						else
							objTestSection.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						lstNewObject.add(objTestSection);
						lstOldObject.add(objTS);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestSection(objTestSection.getNtestcode(), objUserInfo), HttpStatus.OK);
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT" , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to set a default test method
		 * Need to validate that the specified test, it's test method is active before updating the default status.
		 * Need to validate the specified test method is already default
		 * @param objTestMethod [TestMethod] object holds the details for test method to set default
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testmethod' entity
		 * @throws Exception
		 */
		@Override
		public ResponseEntity<Object> setDefaultTestMethod(final TestMethod objTestMethod, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
	        final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestMethod.getNtestcode());
	        if(objTestMaster != null) {
	            String sQuery = "select * from testmethod where ntestmethodcode = "+objTestMethod.getNtestmethodcode()
	                    +" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	            final TestMethod objTM = (TestMethod) jdbcUtilityFunction.queryForObject(sQuery, TestMethod.class,jdbcTemplate);
	            if(objTM != null) {
	                if(objTM.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
	                    final List<String> multilingualIDList = new ArrayList<>();
	                    final List<Object> lstOldObject = new ArrayList<>();
	                    final List<Object> lstNewObject = new ArrayList<>();
	                    sQuery = "select * from testmethod where ntestcode = "+objTestMethod.getNtestcode()
	                        +" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                        +" and ntestmethodcode <> "+objTestMethod.getNtestmethodcode()
	                        +" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
	                    final List<TestMethod> lstTestMethod = (List<TestMethod>) jdbcTemplate.query(sQuery, new TestMethod());
	                    String sUpdateQuery = "";
	                    if(lstTestMethod != null && lstTestMethod.size() > 0) {
	                        lstOldObject.add(SerializationUtils.clone(lstTestMethod.get(0)));
	                        TestMethod objNewTestMethod = new TestMethod();
	                        objNewTestMethod = lstTestMethod.get(0);
	                        sUpdateQuery = "update testmethod set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
	                                +", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestmethodcode = "+objNewTestMethod.getNtestmethodcode()+";";
	                        jdbcTemplate.execute(sUpdateQuery);
	                        objNewTestMethod.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
	                        lstNewObject.add(objNewTestMethod);
	                        multilingualIDList.add("IDS_EDITTESTMETHOD");
	                    }
	                    sUpdateQuery =  "update testmethod set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
	                            +", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestmethodcode = "+objTestMethod.getNtestmethodcode()+";";
	                   
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
	                    if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestMethod.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNmethodcode()!=objTestMethod.getNtestmethodcode()) {
								sUpdateQuery=sUpdateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nmethodcode="+objTestMethod.getNmethodcode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
						}
	                    }
	                    jdbcTemplate.execute(sUpdateQuery);
	                    multilingualIDList.add("IDS_EDITTESTMETHOD");
	                	if(objTestMethod.getNdefaultstatus()==3)
	                		objTestMethod.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						else
							objTestMethod.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
	                    lstNewObject.add(objTestMethod);
	                    lstOldObject.add(objTM);
	                    auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
	                    return new ResponseEntity<>(testMasterCommonFunction.getTestMethod(objTestMethod.getNtestcode(), objUserInfo), HttpStatus.OK);
	                } else {
	                    //status code:417
	                    return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT" , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	                }
	            } else {
	                //status code:417
	                return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	            }
	        } else {
	            //status code:417
	            return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	        }
	    }

		/**
		 * This method definition is used to set a default test instrumentcategory
		 * Need to validate that the specified test, it's test instrumentcategory is active before updating the default status.
		 * Need to validate the specified test instrumentcategory is already default
		 * @param objTestInstrumentCategory [TestInstrumentCategory] object holds the details for test instrumentcategory to set default
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testinstrumentcategory' entity
		 * @throws Exception
		 */
		@Override
		public ResponseEntity<Object> setDefaultTestInstrumentCategory(final TestInstrumentCategory objTestInstrumentCategory, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestInstrumentCategory.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testinstrumentcategory where ntestinstrumentcatcode = "+objTestInstrumentCategory.getNtestinstrumentcatcode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestInstrumentCategory objTIC = (TestInstrumentCategory) jdbcUtilityFunction.queryForObject(sQuery, TestInstrumentCategory.class,jdbcTemplate);
				if(objTIC != null) {
					if(objTIC.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						sQuery = "select * from testinstrumentcategory where ntestcode = "+objTestInstrumentCategory.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ntestinstrumentcatcode <> "+objTestInstrumentCategory.getNtestinstrumentcatcode()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final List<TestInstrumentCategory> lstTestInstrumentCat = (List<TestInstrumentCategory>) jdbcTemplate.query(sQuery, new TestInstrumentCategory());
						String sUpdateQuery = "";
						if(lstTestInstrumentCat != null && lstTestInstrumentCat.size() > 0) {
							lstOldObject.add(SerializationUtils.clone(lstTestInstrumentCat.get(0)));
							TestInstrumentCategory objNewTestInstCat = new TestInstrumentCategory();
							objNewTestInstCat = lstTestInstrumentCat.get(0);
							sUpdateQuery = "update testinstrumentcategory set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
									+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestinstrumentcatcode = "+objNewTestInstCat.getNtestinstrumentcatcode()+";";
							jdbcTemplate.execute(sUpdateQuery);
							objNewTestInstCat.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestInstCat);
							multilingualIDList.add("IDS_EDITTESTINSTRUMENTCATEGORY");
						}
						sUpdateQuery =  "update testinstrumentcategory set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestinstrumentcatcode = "+objTestInstrumentCategory.getNtestinstrumentcatcode()+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.					
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestInstrumentCategory.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNinstrumentcatcode()!=objTestInstrumentCategory.getNtestinstrumentcatcode()) {
								sUpdateQuery=sUpdateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ninstrumentcatcode="+objTestInstrumentCategory.getNinstrumentcatcode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
						     }
						}
						
						jdbcTemplate.execute(sUpdateQuery);
						multilingualIDList.add("IDS_EDITTESTINSTRUMENTCATEGORY");
						if(objTestInstrumentCategory.getNdefaultstatus()== Enumeration.TransactionStatus.YES.gettransactionstatus())
							objTestInstrumentCategory.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						else
							objTestInstrumentCategory.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						lstNewObject.add(objTestInstrumentCategory);
						lstOldObject.add(objTIC);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestInstrumentCategory(objTestInstrumentCategory.getNtestcode(), objUserInfo), HttpStatus.OK);
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT" , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		
		
		//ALPD-3510
		@Override
		public ResponseEntity<Object> setDefaultPackage(final TestPackageTest objTestPackage, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestPackage.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testpackagetest where ntestpackagetestcode = "+objTestPackage.getNtestpackagetestcode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestPackageTest objTIC = (TestPackageTest) jdbcUtilityFunction.queryForObject(sQuery, TestPackageTest.class,jdbcTemplate);
				if(objTIC != null) {
					if(objTIC.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						sQuery = "select * from testpackagetest where ntestcode = "+objTestPackage.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ntestpackagetestcode <> "+objTestPackage.getNtestpackagetestcode()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final List<TestPackageTest> lstTestInstrumentCat = (List<TestPackageTest>) jdbcTemplate.query(sQuery, new TestPackageTest());
						String sUpdateQuery = "";
						if(lstTestInstrumentCat != null && lstTestInstrumentCat.size() > 0) {
							lstOldObject.add(SerializationUtils.clone(lstTestInstrumentCat.get(0)));
							TestPackageTest objNewTestInstCat = new TestPackageTest();
							objNewTestInstCat = lstTestInstrumentCat.get(0);
							sUpdateQuery = "update testpackagetest set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
										+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
//										+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//										+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
									+"' where ntestpackagetestcode = "+objNewTestInstCat.getNtestpackagetestcode()+";";
							
							
							jdbcTemplate.execute(sUpdateQuery);
							objNewTestInstCat.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestInstCat);
							multilingualIDList.add("IDS_EDITTESTPACKAGE");
						}
						sUpdateQuery =  "update testpackagetest set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
										+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
//										+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//										+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
								+"' where ntestpackagetestcode = "+objTestPackage.getNtestpackagetestcode()+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.					
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestPackage.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNtestpackagecode()!=objTestPackage.getNtestpackagetestcode()) {
								sUpdateQuery=sUpdateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ntestpackagecode="+objTestPackage.getNtestpackagetestcode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
						}
						}
						jdbcTemplate.execute(sUpdateQuery);
						multilingualIDList.add("IDS_EDITTESTPACKAGE");
						if(objTestPackage.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus())
							objTestPackage.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						else
							objTestPackage.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						lstNewObject.add(objTestPackage);
						lstOldObject.add(objTIC);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestpackage(objTestPackage.getNtestcode(), objUserInfo), HttpStatus.OK);
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT"  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to create new file/ link
		 * @param request holds the loggedin userinfo, list of test file details, file count, uploadedfile and its uniquevalue
		 * @return response entity holds the list of test file details
		 */
		@Override
		public ResponseEntity<Object> createTestFile(MultipartHttpServletRequest request, final UserInfo objUserInfo) throws Exception {
			
				    
			final ObjectMapper objMapper = new ObjectMapper();
			final List<TestFile> lstReqTestFile = objMapper.readValue(request.getParameter("testfile"), new TypeReference<List<TestFile>>() {});
			
			if(lstReqTestFile != null && lstReqTestFile.size() > 0)
			{
				final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(lstReqTestFile.get(0).getNtestcode());
				
				if(objTestMaster != null) 
				{				
					final String sTableLockQuery = " lock  table testfile "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				    jdbcTemplate.execute(sTableLockQuery);
				    
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					if(lstReqTestFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request,-1, objUserInfo); //Folder Name - master
					}
					
					if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final String sQuery = "select * from testfile where ntestcode = "+lstReqTestFile.get(0).getNtestcode()
												+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final TestFile lstTestFiles = (TestFile) jdbcUtilityFunction.queryForObject(sQuery,  TestFile.class,jdbcTemplate);
										
							TestFile objTestFile=lstReqTestFile.get(0);
							
							if(objTestFile.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) 
							{
								if(lstTestFiles!=null) {
										
									final TestFile TestFileBeforeSave = SerializationUtils.clone(lstTestFiles);

									final List<Object> defaultListBeforeSave = new ArrayList<>();
									defaultListBeforeSave.add(TestFileBeforeSave);
									
									
								  final String updateQueryString =" update testfile set ndefaultstatus="
								  		+ " "+Enumeration.TransactionStatus.NO.gettransactionstatus()
								  		+ ",dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
								  		+ "' where ntestfilecode ="+lstTestFiles.getNtestfilecode();
								  jdbcTemplate.execute(updateQueryString);
										  
										  lstTestFiles.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
											final List<Object> defaultListAfterSave = new ArrayList<>();
											defaultListAfterSave.add(lstTestFiles);

											auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
													Arrays.asList("IDS_EDITTESTFILE"), objUserInfo);
								
						    }
						
						}
						
//						final Instant utcDate = objGeneral.getUTCDateTime();
//						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//						LocalDateTime ldt = Date.from(utcDate).toInstant().atZone(ZoneId.of(objUserInfo.getStimezoneid())).toLocalDateTime();
						
						final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
						final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
	                    final int noffset=dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
						
						lstReqTestFile.forEach(objtf-> {
							//objtf.setDcreateddate(utcDate);
							objtf.setDcreateddate(instantDate);
							if(objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								//objtf.setScreateddate(formatter.format(ldt));
								objtf.setDcreateddate(instantDate);
								objtf.setNoffsetdcreateddate(noffset);
								objtf.setScreateddate(sattachmentDate.replace("T", " "));
							}
							
						});
						
						String sequencequery ="select nsequenceno from SeqNoTestManagement where stablename ='testfile'";
						int nsequenceno =jdbcTemplate.queryForObject(sequencequery, Integer.class);
						nsequenceno++;
						String insertquery= "Insert into testfile(ntestfilecode,ntestcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
										  + "values ("+nsequenceno+","+lstReqTestFile.get(0).getNtestcode()+","+lstReqTestFile.get(0).getNlinkcode()+","+lstReqTestFile.get(0).getNattachmenttypecode()+","
										  + " N'"+stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSfilename())+"',N'"+stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSdescription())+"',"+lstReqTestFile.get(0).getNfilesize()+","
										  + " '"+lstReqTestFile.get(0).getDcreateddate()+"',"+lstReqTestFile.get(0).getNoffsetdcreateddate()+","+objUserInfo.getNtimezonecode()+",N'"+lstReqTestFile.get(0).getSsystemfilename()+"',"
										  + ""+lstReqTestFile.get(0).getNdefaultstatus()+",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
						jdbcTemplate.execute(insertquery);
						
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					final String updateQuery="update SeqNoTestManagement set nsequenceno="+nsequenceno+" where stablename='testfile'";
					jdbcTemplate.execute(updateQuery);

						if(lstReqTestFile.get(0).getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()&&lstReqTestFile.get(0).getNqualislite()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+lstReqTestFile.get(0).getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							String str="select * from testgrouptestfile where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							
							TestGroupTestFile objTestGroupTestFile=(TestGroupTestFile) jdbcUtilityFunction.queryForObject(str,TestGroupTestFile.class,jdbcTemplate);
							if(objTestGroupTestFile==null) {
							final String sQuery1 = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestfile'";
							int ntgttfseq=(int) jdbcUtilityFunction.queryForObject(sQuery1,Integer.class,jdbcTemplate);
									ntgttfseq++;
						String insertQuery="insert into testgrouptestfile (ntestgroupfilecode, ntestgrouptestcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, ssystemfilename, nfilesize, dcreateddate,noffsetdcreateddate,ntzcreateddate, dmodifieddate,nstatus,nsitecode)"
						+ " values (" + ntgttfseq + "," + objecTestGroupTest.getNtestgrouptestcode()+","+ lstReqTestFile.get(0).getNlinkcode() + ","
						+ lstReqTestFile.get(0).getNattachmenttypecode() + ",N'" + stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSfilename())
						+ "',N'" + stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSdescription()) + "',N'"
						+ lstReqTestFile.get(0).getSsystemfilename() + "', " + lstReqTestFile.get(0).getNfilesize() + ", case when N'"
						+ lstReqTestFile.get(0).getDcreateddate() + "'='null' then NULL else N'" + lstReqTestFile.get(0).getDcreateddate() + "'::timestamp end,"
						+ ""+lstReqTestFile.get(0).getNoffsetdcreateddate()+" ,"+objUserInfo.getNtimezonecode()+",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","+objUserInfo.getNmastersitecode() +");";
						
						String updatequery="update seqnotestgroupmanagement set nsequenceno ="+ntgttfseq+" where stablename ='testgrouptestfile';";
						jdbcTemplate.execute(updatequery+insertQuery);
							}
							else {
								String insertQuery="update testgrouptestfile set nlinkcode = " + lstReqTestFile.get(0).getNlinkcode() + ","
										+ " nattachmenttypecode = " + lstReqTestFile.get(0).getNattachmenttypecode() + ", sfilename = N'"
										+ stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSfilename()) + "'," + " sdescription = N'"
										+ stringUtilityFunction.replaceQuote(lstReqTestFile.get(0).getSdescription()) + "', ssystemfilename = N'"
										+ lstReqTestFile.get(0).getSsystemfilename() + "'," + " nfilesize =" + lstReqTestFile.get(0).getNfilesize()
										+ ", dcreateddate = '" + lstReqTestFile.get(0).getDcreateddate() + "',"
										+ " noffsetdcreateddate="+lstReqTestFile.get(0).getNoffsetdcreateddate()+",ntzcreateddate="+objUserInfo.getNtimezonecode()+","
										+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
										+ " where ntestgroupfilecode = (select  ntestgroupfilecode from testgrouptestfile where ntestgrouptestcode="+ objecTestGroupTest.getNtestgrouptestcode() + ");";
								jdbcTemplate.execute(insertQuery);

							}
						}
						
						
						final List<String> multilingualIDList = new ArrayList<>();
						
						multilingualIDList.add(lstReqTestFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()?"IDS_ADDTESTFILE": "IDS_ADDTESTLINK");
						final List<Object> listObject = new ArrayList<Object>();
						lstReqTestFile.get(0).setNtestfilecode(nsequenceno);
						listObject.add(lstReqTestFile);
						
						auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
					} 
					else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				return new ResponseEntity<Object>(testMasterCommonFunction.getTestFile(lstReqTestFile.get(0).getNtestcode(), objUserInfo), HttpStatus.OK);
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		
		/**
		 * This method definition is used to update a file/ link
		 * @param request holds the list of test file details, file count, uploadedfile and its uniquevalue
		 * @param objUserInfo [UserInfo] holds the loggedin userinfo
		 * @return response entity of 'testfile' entity
		 */
		@Override
		public ResponseEntity<Object> updateTestFile(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception
		{
			final ObjectMapper objMapper = new ObjectMapper();
			
			final List<TestFile> lstTestFile = objMapper.readValue(request.getParameter("testfile"), new TypeReference<List<TestFile>>() {});
			if(lstTestFile != null && lstTestFile.size() > 0) 
			{
				final TestFile objTestFile = lstTestFile.get(0);
				final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestFile.getNtestcode());
				
				if(objTestMaster != null) 
				{
					final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					
					if(isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
						}
					}
					
					if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final String sQuery = "select * from testfile where ntestfilecode = "+objTestFile.getNtestfilecode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final TestFile objTF = (TestFile) jdbcUtilityFunction.queryForObject(sQuery, TestFile.class,jdbcTemplate);
						
						final String sCheckDefaultQuery = "select * from testfile where ntestcode = "+objTestFile.getNtestcode()
							+" and ntestfilecode="+objTestFile.getNtestfilecode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<TestFile> lstDefTestFiles = (List<TestFile>) jdbcTemplate.query(sCheckDefaultQuery, new TestFile());
						
						
						if(objTF != null) {
							String ssystemfilename = "";
							if(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								ssystemfilename = objTestFile.getSsystemfilename();
							}
								
						if(lstTestFile.get(0).getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							
							final String sDefaultQuery = "select * from testfile where ntestcode = "+objTestFile.getNtestcode()
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<TestFile> lstTestFiles = (List<TestFile>) jdbcTemplate.query(sDefaultQuery, new TestFile());
							
							if(!lstTestFiles.isEmpty()) {
							  final String updateQueryString =" update testfile set ndefaultstatus="
								  		+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								  		+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
								  		+ "' where ntestfilecode ="+lstTestFiles.get(0).getNtestfilecode();
										  jdbcTemplate.execute(updateQueryString);
							}
						}
						else {
							final String sDefaultQuery = "select * from testfile where ntestcode = "+objTestFile.getNtestcode()
								+" and ntestfilecode="+objTestFile.getNtestfilecode()+""
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<TestFile> lstTestFiles = (List<TestFile>) jdbcTemplate.query(sDefaultQuery, new TestFile());
						
							//if(lstDefTestFiles.size()>0) {
								 if(!lstTestFiles.isEmpty()) 
								 {
									 final String sEditDefaultQuery = "update testfile set "
											+ " ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
											+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "' where ntestfilecode = "+lstDefTestFiles.get(lstDefTestFiles.size()-1).getNtestfilecode();
									 jdbcTemplate.execute(sEditDefaultQuery);
								 }
//							}
//							else {
//								return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
//										objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//							 }
					  }
					  final String sUpdateQuery = "update testfile set sfilename=N'"+stringUtilityFunction.replaceQuote(objTestFile.getSfilename())+"',"
									+ " sdescription=N'"+stringUtilityFunction.replaceQuote(objTestFile.getSdescription())+"', ssystemfilename= N'"+ssystemfilename+"',"
									+ " nattachmenttypecode = "+objTestFile.getNattachmenttypecode()+", nlinkcode="+objTestFile.getNlinkcode()+","
									+ " nfilesize = "+objTestFile.getNfilesize()+",ndefaultstatus = "+objTestFile.getNdefaultstatus()
									+ ",dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestfilecode = "+objTestFile.getNtestfilecode();
					 objTestFile.setDcreateddate(objTF.getDcreateddate());
					 jdbcTemplate.execute(sUpdateQuery);

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.				 
					 if(objTestFile.getNqualislite()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						 final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestFile.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
						 if(objTestFile.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					
						
								String updateQuery="update testgrouptestfile set nlinkcode = " + objTestFile.getNlinkcode() + ","
										+ " nattachmenttypecode = " + objTestFile.getNattachmenttypecode() + ", sfilename = N'"
										+ stringUtilityFunction.replaceQuote(objTestFile.getSfilename()) + "'," + " sdescription = N'"
										+ stringUtilityFunction.replaceQuote(objTestFile.getSdescription()) + "', ssystemfilename = N'"
										+ objTestFile.getSsystemfilename() + "'," + " nfilesize =" + objTestFile.getNfilesize()
										+ ", dcreateddate = '" + objTestFile.getDcreateddate() + "',"
										+ " noffsetdcreateddate="+objTestFile.getNoffsetdcreateddate()+",ntzcreateddate="+objUserInfo.getNtimezonecode()+","
										+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
										+ " where ntestgroupfilecode = (select  ntestgroupfilecode from testgrouptestfile where ntestgrouptestcode="+ objecTestGroupTest.getNtestgrouptestcode() + ");";
								jdbcTemplate.execute(updateQuery);

							}else {
								final String sDefaultQuery = "select * from testfile where ntestcode = "+objTestFile.getNtestcode()
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<TestFile> lstTestFiles = (List<TestFile>) jdbcTemplate.query(sDefaultQuery, new TestFile());
							if(lstTestFiles.isEmpty()) {
								String updateQuery="update testgrouptestfile set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+""
										+ ",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
										+ " where ntestgroupfilecode = (select  ntestgroupfilecode from testgrouptestfile where ntestgrouptestcode="+ objecTestGroupTest.getNtestgrouptestcode() + ");";
								jdbcTemplate.execute(updateQuery);
								}
							}
					 }
						
							
							final List<String> multilingualIDList = new ArrayList<>();
							final List<Object> lstOldObject = new ArrayList<Object>();
							multilingualIDList.add(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()?"IDS_EDITTESTFILE": "IDS_EDITTESTLINK");
							lstOldObject.add(objTF);
							
							auditUtilityFunction.fnInsertAuditAction(lstTestFile, 2, lstOldObject, multilingualIDList, objUserInfo);
							return new ResponseEntity<Object>(testMasterCommonFunction.getTestFile(objTestFile.getNtestcode(), objUserInfo), HttpStatus.OK);
						} else {
							//status code:417
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to delete a file/ link
		 * @param objTestFile [TestFile] object holds the details of test file
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testfile' entity
		 */
		@Override
		public ResponseEntity<Object> deleteTestFile(TestFile objTestFile, UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestFile.getNtestcode());
			if(objTestMaster != null) {
				if(objTestFile != null) {
					final String sQuery = "select * from testfile where ntestfilecode = "+objTestFile.getNtestfilecode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestFile objTF = (TestFile) jdbcUtilityFunction.queryForObject(sQuery, TestFile.class,jdbcTemplate);
					if(objTF != null) {
						if(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
//							deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "master", objUserInfo);
						} else {
							objTestFile.setScreateddate(null);
						}
						
						if(objTF.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String sDeleteQuery = "select * from testfile where "
							+ " ntestfilecode !="+objTestFile.getNtestfilecode()+""
							+ " and ntestcode="+objTestFile.getNtestcode()+""
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							List<TestFile> lstTestFiles = (List<TestFile>) jdbcTemplate.query(sDeleteQuery, new TestFile());
							String sDefaultQuery="";
				        if(lstTestFiles.isEmpty()) {
				        	 sDefaultQuery = " update testfile set "
									 	+ " nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									 	+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
										+ "' where ntestfilecode = "+objTestFile.getNtestfilecode()+";";	
				  
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
	      	 if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				        		 final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestFile.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
									
									TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
									sDefaultQuery=sDefaultQuery+"update testgrouptestfile set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
											+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
											+ " where ntestgroupfilecode = (select  ntestgroupfilecode from testgrouptestfile where ntestgrouptestcode="+ objecTestGroupTest.getNtestgrouptestcode() + ");";
				        	 }
				        }else {
							 sDefaultQuery = "update testfile set "
							 	+ " ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							 	+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+"' where ntestfilecode = "+lstTestFiles.get(lstTestFiles.size()-1).getNtestfilecode()+";";
							 
							 if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								 
									final String queryTestGroupTest="select * from testgrouptest where ntestcode="+lstTestFiles.get(lstTestFiles.size()-1).getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
									
									TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
									
								  sDefaultQuery=sDefaultQuery+"update testgrouptestfile set nlinkcode = " + lstTestFiles.get(lstTestFiles.size()-1).getNlinkcode() + ","
											+ " nattachmenttypecode = " + lstTestFiles.get(lstTestFiles.size()-1).getNattachmenttypecode() + ", sfilename = N'"
											+ stringUtilityFunction.replaceQuote(lstTestFiles.get(lstTestFiles.size()-1).getSfilename()) + "'," + " sdescription = N'"
											+ stringUtilityFunction.replaceQuote(lstTestFiles.get(lstTestFiles.size()-1).getSdescription()) + "', ssystemfilename = N'"
											+ lstTestFiles.get(lstTestFiles.size()-1).getSsystemfilename() + "'," + " nfilesize =" + lstTestFiles.get(lstTestFiles.size()-1).getNfilesize()
											+ ", dcreateddate = '" + lstTestFiles.get(lstTestFiles.size()-1).getDcreateddate() + "',"
											+ " noffsetdcreateddate="+lstTestFiles.get(lstTestFiles.size()-1).getNoffsetdcreateddate()+",ntzcreateddate="+objUserInfo.getNtimezonecode()+","
											+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
											+ " where ntestgroupfilecode= (select  ntestgroupfilecode from testgrouptestfile where ntestgrouptestcode="+ objecTestGroupTest.getNtestgrouptestcode() + ");";
								 }
							 }
				        jdbcTemplate.execute(sDefaultQuery);
						}				
						 final String sUpdateQuery = "update testfile set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestfilecode = "+objTestFile.getNtestfilecode();
						 jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstObject = new ArrayList<>();
						multilingualIDList.add(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()? "IDS_DELETETESTFILE": "IDS_DELETETESTLINK");
						lstObject.add(objTestFile);
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
				return new ResponseEntity<Object>(testMasterCommonFunction.getTestFile(objTestFile.getNtestcode(), objUserInfo), HttpStatus.OK);
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This method definition is used to fetch a active test file using primarykey
		 * @param objTestFile [TestFile] object holds the details of test file
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testfile' entity
		 */
		@Override
		public ResponseEntity<Object> editTestFile(final TestFile objTestFile, final UserInfo objUserInfo) throws Exception {
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestFile.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sEditQuery = "select tf.ntestfilecode, tf.ntestcode, tf.nlinkcode, tf.nattachmenttypecode, tf.sfilename, tf.sdescription, tf.nfilesize,"		
										+ " tf.ssystemfilename, tf.ndefaultstatus, lm.jsondata->>'slinkname' as slinkname"
										+ " from testfile tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode"
										+ " and tf.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and lm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and tf.ntestfilecode = "+objTestFile.getNtestfilecode();
				final TestFile objTF = (TestFile) jdbcUtilityFunction.queryForObject(sEditQuery, TestFile.class,jdbcTemplate);
				if(objTF != null) {
					return new ResponseEntity<Object>(objTF, HttpStatus.OK);
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}

		/**
		 * This method definition is used to fetch a file/ link which need to view
		 * @param objTestFile [TestFile] object holds the details of test file
		 * @param objUserInfo [UserInfo] object holds the loggedin user info
		 * @return response entity of 'testfile' entity
		 */
		@Override
		public Map<String, Object> viewAttachedTestFile(TestFile objTestFile, UserInfo objUserInfo) throws Exception {
			Map<String,Object> map=new HashMap<>();
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestFile.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testfile where ntestfilecode = "+objTestFile.getNtestfilecode()
				+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestFile objTF = (TestFile) jdbcUtilityFunction.queryForObject(sQuery, TestFile.class,jdbcTemplate);
				if(objTF != null) {
					if(objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");//Folder Name - master
					} else {
						sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="+objTF.getNlinkcode()
								+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,jdbcTemplate);
						map.put("AttachLink", objlinkmaster.getSlinkname()+objTF.getSfilename());
						objTestFile.setScreateddate(null);
					}
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add(objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()? "IDS_VIEWTESTFILE": "IDS_VIEWTESTLINK");
					lstObject.add(objTestFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
					
//					return new ResponseEntity<Object>(map, HttpStatus.OK);
				} else {
					//status code:417
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()));
					return map;
				}
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()));			//status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return map;
		}

		/**
		 * This method is used to set a default test file to selected test
		 * @param objTestFile [TestFile] object holding details of test file
		 * @param objUserInfo [UserInfo] object holding details of logged in user
		 * @return response entity with response status and list of the test file
		 */
		@Override
		public ResponseEntity<Object> setDefaultTestFile(final TestFile objTestFile, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestFile.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testfile where ntestfilecode = "+objTestFile.getNtestfilecode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestFile objTF = (TestFile) jdbcUtilityFunction.queryForObject(sQuery, TestFile.class,jdbcTemplate);
				if(objTF != null) {
					if(objTF.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						sQuery = "select * from testfile where ntestcode = "+objTestFile.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ntestfilecode <> "+objTestFile.getNtestfilecode()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final List<TestFile> lstTestFile = (List<TestFile>) jdbcTemplate.query(sQuery, new TestFile());
						String sUpdateQuery = "";
						if(lstTestFile != null && lstTestFile.size() > 0) {
							lstOldObject.add(SerializationUtils.clone(lstTestFile.get(0)));
							TestFile objNewTestFile = new TestFile();
							objNewTestFile = lstTestFile.get(0);
							sUpdateQuery = "update testfile set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
									+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestfilecode = "+objNewTestFile.getNtestfilecode()+";";
							jdbcTemplate.execute(sUpdateQuery);
							objNewTestFile.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestFile);
							multilingualIDList.add("IDS_EDITTESTFILE");
						}
						sUpdateQuery = "update testfile set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+", dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' where ntestfilecode = "+objTestFile.getNtestfilecode()+";";
						jdbcTemplate.execute(sUpdateQuery);
						multilingualIDList.add("IDS_EDITTESTFILE");
						objTF.setScreateddate(objTestFile.getScreateddate());
						lstNewObject.add(objTestFile);
						lstOldObject.add(objTF);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestFile(objTestFile.getNtestcode(), objUserInfo), HttpStatus.OK);
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT"  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		/**
		 * This interface declaration is used to fetch a list of test section based on test
		 * @param ntestcode [int] primary key of test object for which the records are to be fetched
		 * @return response entity with response status and list of the test section
		 */
		@Override
		public ResponseEntity<Object> getSection(final int ntestcode) throws Exception {
			final String sSectionQry = "select s.* from testsection ts, section s where s.nsectioncode = ts.nsectioncode and s.nstatus = ts.nstatus"
					+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.ntestcode = "+ntestcode;
			return new ResponseEntity<Object>((List<Section>)jdbcTemplate.query(sSectionQry, new Section()), HttpStatus.OK);
		}

		/**
		 * This method is used to fetch a list of test method based on test
		 * @param ntestcode [int] primary key of test object for which the records are to be fetched
		 * @return response entity with response status and list of the test method
		 * @throws Exception
		 */
		@Override
		public ResponseEntity<Object> getMethod(final int ntestcode) throws Exception {
			final String sMethodQry = "select m.* from testmethod tm, method m where m.nmethodcode = tm.nmethodcode and m.nstatus = tm.nstatus"
					+ " and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.ntestcode = "+ntestcode;
			return new ResponseEntity<Object>(jdbcTemplate.query(sMethodQry, new Method()), HttpStatus.OK);
		}


		/**
		 * This method is used to fetch a list of test instrument category based on test
		 * @param ntestcode [int] primary key of test object for which the records are to be fetched
		 * @return response entity with response status and list of the test instrument category
		 */
		@Override
		public ResponseEntity<Object> getInstrumentCategory(final int ntestcode) throws Exception {
			final String sInstCatQry = "select ic.* from testinstrumentcategory tic, instrumentcategory ic where ic.ninstrumentcatcode = tic.ninstrumentcatcode"
					+ " and ic.nstatus = tic.nstatus and tic.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tic.ntestcode = "+ntestcode;
			return new ResponseEntity<Object>((List<InstrumentCategory>)jdbcTemplate.query(sInstCatQry, new InstrumentCategory()), HttpStatus.OK);
		}

		/**
		 * This method is used to fetch a list of test file based on test
		 * @param ntestcode [int] primary key of test object for which the records are to be fetched
		 * @return response entity with response status and list of the test file
		 */
		@Override
		public ResponseEntity<Object> getTestAttachment(final int ntestcode) throws Exception {
			final String sTestFileQry = "select * from testfile where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and ntestcode = "+ntestcode;
			return new ResponseEntity<Object>(jdbcTemplate.query(sTestFileQry, new TestFile()), HttpStatus.OK);
		}

		/**
		 * This method is used to validate the selected test is active
		 * @param objUserInfo [UserInfo] object holding details of logged in user
		 * @param ntestcode [int] primary key of test object for which the records are to be fetched
		 * @return response object with response status and validation message
		 */
		@Override
		public ResponseEntity<Object> validateCopyTest(final UserInfo objUserInfo, final int ntestcode) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(ntestcode);
			if(objTestMaster == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTESTISALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<Object>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), HttpStatus.OK);
			}
		}

		@Override
		public ResponseEntity<Object> getAvailableContainerType(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestMaster.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select  " + objTestMaster.getNtestcode() + " as ntestcode,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " as nstatus,4 as ntransactionstatus,-1 as ntestcontainertypecode,c.ncontainertypecode,c.scontainertype "
									+ " from containertype c where c.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and c.ncontainertypecode>0 and not exists (select 1 from testcontainertype tc "
									+ " where tc.ncontainertypecode= c.ncontainertypecode and tc.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tc.ntestcode = "+ objTestMaster.getNtestcode() + ")";
				final List<TestContainerType> lstAvailablelist = (List<TestContainerType>) jdbcTemplate.query(sQuery, new TestContainerType());
				if(!lstAvailablelist.isEmpty()) {
					return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
				} else{
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CONTAINERTYPENOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		@Override
		public ResponseEntity<Object> createTestContainerType(final TestContainerType objTestContainerType, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			
			if(objTestContainerType != null ) 
			{
				final int ntestcode = objTestContainerType.getNtestcode();
				//ALPD-833
				final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
				if(testMasterById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
						
					final String sTableLockQuery = " lock  table testcontainertype "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
					jdbcTemplate.execute(sTableLockQuery);
					
					String sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testcontainertype'";
		
					final int nSeqNo = (int) jdbcUtilityFunction.queryForObject(sQuery, Integer.class,jdbcTemplate);
					
					sQuery = "select "+nSeqNo+"+Rank()over(order by c.ncontainertypecode) ntestcontainertypecode,"+ntestcode+" ntestcode,c.ncontainertypecode,"
							+" case when(select count(ntestcontainertypecode) from testcontainertype where ntestcode = "+ntestcode
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
							+"+Rank()over(order by c.ncontainertypecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
							+" from containertype c where not exists (select 1 from testcontainertype tc "
							+" where c.ncontainertypecode = tc.ncontainertypecode and tc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and tc.ntestcode = "+ntestcode+")"+" and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and c.ncontainertypecode in ("+objTestContainerType.getNcontainertypecode()+");";
					
					final List<TestContainerType> lstTestContainerType = (List<TestContainerType>) jdbcTemplate.query(sQuery, new TestContainerType());
					
					if(!lstTestContainerType.isEmpty()) {
		//				String sInstQuery = "insert into testcontainertype (ntestcontainertypecode, ntestcode, ncontainertypecode,nquantity, ndefaultstatus, nstatus,noutsourcecode)" 
		//						+"select "+nSeqNo+"+Rank()over(order by c.ncontainertypecode) ntestcontainertypecode,"+ntestcode+" ntestcode,c.ncontainertypecode,"+objTestContainerType.getNquantity()+" nquantity,"
		//						+" case when (select count(ntestcontainertypecode) from testcontainertype where ntestcode = "+ntestcode
		//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//						+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
		//						+"+Rank()over(order by c.ncontainertypecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
		//						+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
		//						+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus,"+objTestContainerType.getNoutsourcecode()
		//						+" from containertype c where not exists (select 1 from testcontainertype tc "
		//						+" where c.ncontainertypecode = tc.ncontainertypecode and tc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//						+" and tc.ntestcode = "+ntestcode+")"+" and  c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		//						+" and c.ncontainertypecode = "+objTestContainerType.getNcontainertypecode()+";";
		//				getJdbcTemplate().execute(sInstQuery);
						
						String sInstQuery = "insert into testcontainertype (ntestcontainertypecode, ntestcode,dmodifieddate,ncontainertypecode,nquantity,nunitcode ,ndefaultstatus, nsitecode, nstatus)" 
								+"select "+nSeqNo+"+Rank()over(order by c.ncontainertypecode) ntestcontainertypecode,"+ntestcode+" ntestcode,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',c.ncontainertypecode,"+objTestContainerType.getNquantity()+" nquantity,"+objTestContainerType.getNunitcode()+" nunitcode,"
								+" case when (select count(ntestcontainertypecode) from testcontainertype where ntestcode = "+ntestcode
								+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
								+"+Rank()over(order by c.ncontainertypecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
								+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus "
								+" from containertype c where not exists (select 1 from testcontainertype tc "
								+" where c.ncontainertypecode = tc.ncontainertypecode and tc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and tc.ntestcode = "+ntestcode+")"+" and  c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and c.ncontainertypecode = "+objTestContainerType.getNcontainertypecode()+";";
						jdbcTemplate.execute(sInstQuery);
						sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestContainerType.size())+" where stablename = 'testcontainertype';";
						jdbcTemplate.execute(sInstQuery);
						
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String queryTestGroupTest="select * from testgrouptest where ntestcode="+ntestcode+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNcontainertypecode()==Enumeration.TransactionStatus.NA.gettransactionstatus()) {
								sInstQuery="update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ncontainertypecode="+objTestContainerType.getNcontainertypecode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
								jdbcTemplate.execute(sInstQuery);

							}
						}
						final List<String> strTestContainerType= new ArrayList<>();
						final List<Object> objlstTestContainerType= new ArrayList<>();
						
						objTestContainerType.setNtestcontainertypecode(nSeqNo+lstTestContainerType.size());
						
						objlstTestContainerType.add(objTestContainerType);
						strTestContainerType.add("IDS_ADDTESTCONTAINERTYPE");
						auditUtilityFunction.fnInsertAuditAction(objlstTestContainerType, 1,null, strTestContainerType, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestContainterType(ntestcode, objUserInfo).getBody(), HttpStatus.OK);
					} else {
						// return new ResponseEntity<>(getTestContainterType(ntestcode, objUserInfo).getBody(), HttpStatus.OK);
						// added by gowtham on 07/02/25 - ALPD-5353 Test Master Multi Tab adding same container type alert not thrown
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} 
			}
			else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CONTAINERTYPENOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		//ALPD-3510
		@Override
		public ResponseEntity<Object> setDefaultTestContainerType(final TestContainerType objTestContainerType, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestContainerType.getNtestcode());
			if(objTestMaster != null) {
				String sQuery = "select * from testcontainertype where ntestcontainertypecode = "+objTestContainerType.getNtestcontainertypecode()
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestContainerType objTS = (TestContainerType) jdbcUtilityFunction.queryForObject(sQuery, TestContainerType.class,jdbcTemplate);
				if(objTS != null) {
					if(objTS.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						sQuery = "select * from testcontainertype where ntestcode = "+objTestContainerType.getNtestcode()
							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+" and ntestcontainertypecode <> "+objTestContainerType.getNtestcontainertypecode()
							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
						final List<TestContainerType> lstTestContainerType = (List<TestContainerType>) jdbcTemplate.query(sQuery, new TestContainerType());
						String sUpdateQuery = "";
						if(lstTestContainerType != null && lstTestContainerType.size() > 0) {
							lstOldObject.add(SerializationUtils.clone(lstTestContainerType.get(0)));
							TestContainerType objNewTestContainerType = new TestContainerType();
							objNewTestContainerType = lstTestContainerType.get(0);
							sUpdateQuery = "update testcontainertype set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
							+"' where ntestcontainertypecode = "+objNewTestContainerType.getNtestcontainertypecode()+";";
							jdbcTemplate.execute(sUpdateQuery);
							objNewTestContainerType.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestContainerType);
							multilingualIDList.add("IDS_EDITTESTCONTAINERTYPE");
						}
						sUpdateQuery =  "update testcontainertype set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
//								+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//								+",noffsetdmodifieddate= "+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
								+"' where ntestcontainertypecode = "+objTestContainerType.getNtestcontainertypecode()+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestContainerType.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
							
							TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
							
							if(objecTestGroupTest.getNcontainertypecode()!=objTestContainerType.getNtestcontainertypecode()) {
								final String supdaeQuery="update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ncontainertypecode="+objTestContainerType.getNcontainertypecode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";
								
								jdbcTemplate.execute(supdaeQuery);

							}
						}
						jdbcTemplate.execute(sUpdateQuery);

						multilingualIDList.add("IDS_EDITTESTCONTAINERTYPE");
						if(objTestContainerType.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus())
							objTestContainerType.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						else
							objTestContainerType.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						lstNewObject.add(objTestContainerType);
						lstOldObject.add(objTS);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(testMasterCommonFunction.getTestContainterType(objTestContainerType.getNtestcode(), objUserInfo), HttpStatus.OK).getBody();
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT"  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		
		@Override
		public ResponseEntity<Object> deleteTestContainerType(final TestContainerType objTestContainerType, final UserInfo objUserInfo,final int isQualisLite) throws Exception 
		{
			////ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objTestContainerType.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				final String sQuery = "select * from testcontainertype where ntestcontainertypecode = "+objTestContainerType.getNtestcontainertypecode()
							+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				final TestContainerType objTestContType = (TestContainerType) jdbcUtilityFunction.queryForObject(sQuery, TestContainerType.class,jdbcTemplate);
				
				if(objTestContType != null) 
				{
					
					final String query="select 'IDS_TESTGROUP' as Msg from testgrouptest where ncontainertypecode= " 
			        		  + objTestContType.getNcontainertypecode() + " and ntestcode="+ objTestContType.getNtestcode()
			        		  + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, objUserInfo);  
					
					if (objDeleteValidation.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
					
					if(objTestContType.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) 
					{
						final String queryString = "select * from testcontainertype where "
												+ " ntestcontainertypecode <> "+objTestContainerType.getNtestcontainertypecode()
												+ " and ntestcode = "+objTestContainerType.getNtestcode()
												+ " and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<TestContainerType> containerTypeList = (List<TestContainerType>) jdbcTemplate.query(queryString, new TestContainerType());
						if (!containerTypeList.isEmpty()) 
						{
							final TestContainerType containerTypeBeforeSave = containerTypeList.get(0);
							
							final TestContainerType containerTypeAfterSave = SerializationUtils.clone(containerTypeBeforeSave);
							containerTypeAfterSave.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							
							 String updateQuery = "update testcontainertype set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
														+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
														+"' where ntestcontainertypecode=" + containerTypeBeforeSave.getNtestcontainertypecode()+";";
		

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestContainerType.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" ";
								
								TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
								
									updateQuery=updateQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ncontainertypecode="+containerTypeBeforeSave.getNcontainertypecode()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
								
							}
							
							
							jdbcTemplate.execute(updateQuery);	
		
					        auditUtilityFunction.fnInsertAuditAction(Arrays.asList(containerTypeAfterSave),2, Arrays.asList(containerTypeBeforeSave), 
					        		Arrays.asList("IDS_EDITTESTCONTAINERTYPE"), objUserInfo);
							
						}
					} 
					//else {
					 String sdeleteQuery = "update testcontainertype set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
								+"' where ntestcontainertypecode=" + objTestContainerType.getNtestcontainertypecode()+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestContainerType.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"";
						
						TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
						if(objecTestGroupTest.getNcontainertypecode()==objTestContainerType.getNcontainertypecode()) {
							sdeleteQuery=sdeleteQuery+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',ncontainertypecode="+Enumeration.TransactionStatus.NA.gettransactionstatus()+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
						}
					
					}
					jdbcTemplate.execute(sdeleteQuery);
					
					final List<String> strArray= new ArrayList<>();
					final List<Object> objlst = new ArrayList<>();
					objlst.add(objTestContType);
					strArray.add("IDS_DELETETESTCONTAINERTYPE");
		            auditUtilityFunction.fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);   
		            return new ResponseEntity<>(testMasterCommonFunction.getTestContainterType(objTestContainerType.getNtestcode(), objUserInfo).getBody(), HttpStatus.OK);
		//			}	
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		
		
		@Override
		public TestContainerType getActiveTestContainerTypeById(int ntestcontainertypecode,final UserInfo userInfo) throws Exception {
			
//			final String strQuery = "select tct.*,ct.scontainertype,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from testcontainertype tct,containertype ct,transactionstatus ts " 
//					+" where tct.ncontainertypecode=ct.ncontainertypecode " +  "and tct.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+" and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
//					+" and ts.ntranscode = tct.ndefaultstatus"
//				    + " and tct.ntestcontainertypecode = " + ntestcontainertypecode;
			
			final String strQuery = "select tct.*,ct.scontainertype,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, case when u.sunitname ='NA' then '-' else u.sunitname  end as sunitname from testcontainertype tct,containertype ct,transactionstatus ts,unit u " 
					+" where tct.ncontainertypecode=ct.ncontainertypecode " +  "and tct.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+" and ts.ntranscode = tct.ndefaultstatus and u.nunitcode=tct.nunitcode"
				    + " and tct.ntestcontainertypecode = " + ntestcontainertypecode;
			return (TestContainerType) jdbcUtilityFunction.queryForObject(strQuery, TestContainerType.class,jdbcTemplate);

		}
		
		@Override
		public ResponseEntity<Object> updateTestContainerType(Map<String, Object> inputMap, UserInfo userInfo,final int isQualislite) throws Exception {
			
			
			final int ncontainertypecode = (Integer) inputMap.get("ncontainertypecode");
			final int ntestcontainertypecode = (Integer) inputMap.get("ntestcontainertypecode");
			final int nquantity = (Integer) inputMap.get("nquantity");
			//final int noutsourcecode = (Integer) inputMap.get("noutsourcecode");
			final int nunitcode = (Integer) inputMap.get("nunitcode");
			final int ntestcode = (Integer) inputMap.get("ntestcode");
			//ALPD-833
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {

				final TestContainerType objTestContainerType = getActiveTestContainerTypeById(ntestcontainertypecode,userInfo);
				
				if (objTestContainerType == null) 
				{
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				} 
				else 
				{				
					final String queryString = "select ntestcontainertypecode from testcontainertype where ncontainertypecode = " +ncontainertypecode
												+ " and ntestcontainertypecode <> " + ntestcontainertypecode 
												+ " and  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and ntestcode="+ntestcode;
					
					final List<TestContainerType> testContainerTypeList = (List<TestContainerType>) jdbcTemplate.query(queryString, new TestContainerType());
			
					if (testContainerTypeList.isEmpty()) 
					{	
						
						final List<String> multilingualIDList  = new ArrayList<>();				
						final List<Object> listAfterUpdate = new ArrayList<>();		
						final List<Object> listBeforeUpdate = new ArrayList<>();				
													
					     String updateQueryString = "update testcontainertype set ncontainertypecode=" + ncontainertypecode
			    				+ ", nquantity = " + nquantity
			    				+", nunitcode="+nunitcode
			    				+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+"' where ntestcontainertypecode=" + ntestcontainertypecode+";";

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.				     
					     if(isQualislite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String queryTestGroupTest="select * from testgrouptest where ntestcode="+objTestContainerType.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
								
								TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(queryTestGroupTest,TestGroupTest.class,jdbcTemplate);
									
								updateQueryString=updateQueryString+"update testgrouptest set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',ncontainertypecode="+ncontainertypecode+" where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+";";		
							}
					     jdbcTemplate.execute(updateQueryString);
						final TestContainerType objNewTestContainerType = getActiveTestContainerTypeById(ntestcontainertypecode,userInfo);
					
					    multilingualIDList.add("IDS_EDITTESTCONTAINERTYPE");
					    listAfterUpdate.add(objNewTestContainerType);
		
						listBeforeUpdate.add(objTestContainerType);
												
						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);						
					
						return testMasterCommonFunction.getTestContainterType(ntestcode,userInfo);
					} 
					else 
					{	
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			}
		}
		
		
		
//		@Override
//		public ResponseEntity<Object> getPeriod(UserInfo userInfo) throws Exception {
////			String strQuery = "select u.*,"
////					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
////					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
////					+ " from Unit u,transactionstatus ts"
////					+ " where u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
////					" and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
////					" and u.nunitcode>0 and ts.ntranscode=u.ndefaultstatus and u.nsitecode="+userInfo.getNmastersitecode();
//			final String strQuery = " select p.nperiodcode as ndeltaunitcode," + " coalesce(p.jsondata->'speriodname'->>'"
//					+ userInfo.getSlanguagetypecode() + "'," + " p.jsondata->'speriodname'->>'en-US') as sdeltaunitname"
//					+", p.nperiodcode as ntatperiodcode," + " coalesce(p.jsondata->'speriodname'->>'"
//					+ userInfo.getSlanguagetypecode() + "'," + " p.jsondata->'speriodname'->>'en-US') as statunitname"
//					+ " from period p where p.nperiodcode in(2,4) and p.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			List<String> lstcolumns = new ArrayList<>();
//			lstcolumns.add("sdisplaystatus");
//			return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Period()), HttpStatus.OK);
////			return new ResponseEntity<Object>(commonFunction.getMultilingualMessageList((List<Unit>) findBySinglePlainSql(strQuery, Unit.class),lstcolumns,userInfo.getSlanguagefilename()), HttpStatus.OK);
//		}
		
		
//		@SuppressWarnings("unchecked")
//		@Override
//		public ResponseEntity<Object> getAvailableTechnique(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
//			String sQuery = " select  " + objTestMaster.getNtestcode() + " as ntestcode,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+ " as nstatus,"+Enumeration.TransactionStatus.NO.gettransactionstatus()+" as ndefaultstatus,-1 as ntesttechniquecode,s.ntechniquecode,s.stechniquename"
//			+ " from technique s where s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			+ " and s.ntechniquecode>0 and not exists (select 1 from testtechnique ts2 where ts2.ntechniquecode= s.ntechniquecode "
//			+ " and ts2.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts2.ntestcode ="
//			+ objTestMaster.getNtestcode() + ")";
//			List<TestTechnique> lstAvailablelist = (List<TestTechnique>) jdbcTemplate.query(sQuery, new TestTechnique());
//			if(!lstAvailablelist.isEmpty()) {
//				return new ResponseEntity<>(lstAvailablelist, HttpStatus.OK);
//			} else{
//				//status code:417
//				//return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SECTIONNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTTECHNIQUENOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	//
//			}
//		}
		
		
		
		
		
//		@SuppressWarnings("unchecked")
//		@Override
//		public ResponseEntity<Object> createTestTechnique(final List<TestTechnique> lstTestTechnique, final UserInfo objUserInfo) throws Exception {
//			if(lstTestTechnique!=null && lstTestTechnique.size()>0) {
//				final int ntestcode = lstTestTechnique.get(0).getNtestcode();
////				final StringBuilder sb = new StringBuilder();
//				//For_MSSQL
////				String sQuery = "select * from seqnotestmanagement with(tablockx)";
//				//For_PostgreSQL
//				String sQuery = "LOCK TABLE seqnotestmanagement ";
//				getJdbcTemplate().execute(sQuery);
//				sQuery = "select nsequenceno from seqnotestmanagement where stablename = 'testtechnique'";
//				final int nSeqNo = getJdbcTemplate().queryForObject(sQuery, Integer.class);
//				sQuery = "select "+nSeqNo+"+Rank()over(order by s.ntechniquecode) ntestsectioncode,"+ntestcode+" ntestcode,s.ntechniquecode,"
//						+"case when (select count(ntesttechniquecode) from testtechnique where ntestcode = "+ntestcode
//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//						+"+Rank()over(order by s.ntechniquecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//						+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
//						+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus,-1 nmasterauditcode"
//						+" from technique s where not exists (select 1 from testtechnique ts "
//						+" where s.ntechniquecode = ts.ntechniquecode and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and ts.ntestcode = "+ntestcode+")"+" and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+" and s.ntechniquecode in ("+fndynamiclisttostring(lstTestTechnique, "getNtechniquecode")+")";
//				List<TestTechnique> lstTestTechnique1 = (List<TestTechnique>) jdbcTemplate.query(sQuery, new TestTechnique());
//				if(!lstTestTechnique1.isEmpty()) {
//					String sInstQuery = "insert into testtechnique (ntesttechniquecode, ntestcode, ntechniquecode, ndefaultstatus, nstatus)" 
//							+"select "+nSeqNo+"+Rank()over(order by s.ntechniquecode) ntesttechniquecode,"+ntestcode+" ntestcode,s.ntechniquecode,"
//							+"case when (select count(ntesttechniquecode) from testtechnique where ntestcode = "+ntestcode
//							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()+")"
//							+"+Rank()over(order by s.ntechniquecode)=1 then "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//							+" else "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" end as ndefaultstatus,"
//							+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nstatus"
//							+" from technique s where not exists (select 1 from testtechnique ts "
//							+" where s.ntechniquecode = ts.ntechniquecode and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and ts.ntestcode = "+ntestcode+")"+" and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and s.ntechniquecode in ("+fndynamiclisttostring(lstTestTechnique, "getNtechniquecode")+");";
//					getJdbcTemplate().execute(sInstQuery);
////					sb.append(sInstQuery);
//					sInstQuery = "update seqnotestmanagement set nsequenceno = "+(nSeqNo+lstTestTechnique1.size())+" where stablename = 'testtechnique';";
//					getJdbcTemplate().execute(sInstQuery);
////					sb.append(sInstQuery);
////					getJdbcTemplate().execute(sb.toString());
//					final List<String> strTestTechnique= new ArrayList<>();
//					final List<Object> objlstTestTechnique= new ArrayList<>();
//					objlstTestTechnique.add(lstTestTechnique1);
//					//strTestSection.add("IDS_ADDTESTSECTION");
//					strTestTechnique.add("IDS_ADDTESTMATERSECTION");
//					fnInsertListAuditAction(objlstTestTechnique, 1,null, strTestTechnique, objUserInfo);
//					return new ResponseEntity<>(getTestTechnique(ntestcode, objUserInfo), HttpStatus.OK);
//				} else {
//					return new ResponseEntity<>(getTestTechnique(ntestcode, objUserInfo), HttpStatus.OK);
//				}
//			} else {
//				//status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SECTIONNOTAVAILABLE", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			}
//			
	//
//		}
		
		
		
//		@SuppressWarnings("unchecked")
//		@Override
//		public ResponseEntity<Object> setDefaultTestTechnique(final TestTechnique objTestTechnique, final UserInfo objUserInfo) throws Exception {
//			final TestMaster objTestMaster = checkTestIsPresent(objTestTechnique.getNtestcode());
//			if(objTestMaster != null) {
//				String sQuery = "select * from testtechnique where ntesttechniquecode = "+objTestTechnique.getNtesttechniquecode()
//						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//				final TestTechnique objTS = (TestTechnique) jdbcQueryForObject(sQuery, TestTechnique.class);
//				if(objTS != null) {
//					if(objTS.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
//						final List<String> multilingualIDList = new ArrayList<>();
//						final List<Object> lstOldObject = new ArrayList<>();
//						final List<Object> lstNewObject = new ArrayList<>();
//						sQuery = "select * from testtechnique where ntestcode = "+objTestTechnique.getNtestcode()
//							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and ntesttechniquecode <> "+objTestTechnique.getNtesttechniquecode()
//							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
//						final List<TestTechnique> lstTestSection = (List<TestTechnique>) jdbcTemplate.query(sQuery, new TestTechnique());
//						String sUpdateQuery = "";
//						if(lstTestSection != null && lstTestSection.size() > 0) {
//							lstOldObject.add(new TestTechnique(lstTestSection.get(0)));
//							TestTechnique objNewTestSection = new TestTechnique();
//							objNewTestSection = lstTestSection.get(0);
//							sUpdateQuery = "update testtechnique set ndefaultstatus = "+Enumeration.TransactionStatus.NO.gettransactionstatus()
//									+" where ntesttechniquecode = "+objNewTestSection.getNtesttechniquecode()+";";
//							getJdbcTemplate().execute(sUpdateQuery);
//							objNewTestSection.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
//							lstNewObject.add(objNewTestSection);
//							//multilingualIDList.add("IDS_EDITTESTLAB");
//							multilingualIDList.add("IDS_EDITTESTSECTION");
//						}
//						sUpdateQuery =  "update testtechnique set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//								+" where ntesttechniquecode = "+objTestTechnique.getNtesttechniquecode()+";";
//						getJdbcTemplate().execute(sUpdateQuery);
//						//multilingualIDList.add("IDS_EDITTESTLAB");
//						multilingualIDList.add("IDS_EDITTESTSECTION");
//						if(objTestTechnique.getNdefaultstatus()==3)
//							objTestTechnique.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
//						else
//							objTestTechnique.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
//						lstNewObject.add(objTestTechnique);
//						lstOldObject.add(objTS);
//						fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
//						return new ResponseEntity<>(getTestTechnique(objTestTechnique.getNtestcode(), objUserInfo), HttpStatus.OK);
//					} else {
//						//status code:417
//						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONERECORDMUSTSETDEFAULT" , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//					}
//				} else {
//					//status code:417
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()  , objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//				}
//			} else {
//				//status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			}
//		}
	//	
	//	
	//	
//		@Override
//		public ResponseEntity<Object> deleteTestTechnique(final TestTechnique objTestTechnique, final UserInfo objUserInfo) throws Exception {
//			String sQuery = "select * from testtechnique where ntesttechniquecode = "+objTestTechnique.getNtesttechniquecode()
//						+" and nstatus  = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			TestTechnique objTestSection1 = (TestTechnique) jdbcQueryForObject(sQuery, TestTechnique.class);
//			String traningneed ="select ntraningneed from testmaster where ntestcode="+objTestTechnique.getNtestcode();
//			int ntraningneed =getJdbcTemplate().queryForObject(traningneed, Integer.class);
//			if(objTestSection1 != null) {
//				if(objTestSection1.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus() && ntraningneed==3) {
//					//status code:417
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//				} else {
//					final List<String> strArray= new ArrayList<>();
//					final List<Object> objlst = new ArrayList<>();
////					String sdeleteQuery = "delete from testsection where ntestsectioncode=" + objTestSection.getNtestsectioncode() + " and nstatus="
////							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					String sdeleteQuery = "update testtechnique set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
//							+" where ntesttechniquecode=" + objTestTechnique.getNtesttechniquecode();
//					getJdbcTemplate().execute(sdeleteQuery);
//					objlst.add(objTestTechnique);
//					//strArray.add("IDS_DELETETESTSECTION");
//					strArray.add("IDS_DELETETESTMATERSECTION");
//		            fnInsertAuditAction(objlst,1, null, strArray, objUserInfo);
//		            return new ResponseEntity<>(getTestTechnique(objTestTechnique.getNtestcode(), objUserInfo), HttpStatus.OK);
//				}
//			} else {
//				//status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			}
//		}
	//	
		
		
		@Override
		public ResponseEntity<Object> getInterfaceType(UserInfo userInfo) throws Exception {
//			String strQuery = "select u.*,"
//					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
//					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
//					+ " from Unit u,transactionstatus ts"
//					+ " where u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
//					" and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+
//					" and u.nunitcode>0 and ts.ntranscode=u.ndefaultstatus and u.nsitecode="+userInfo.getNmastersitecode();
			final String strQuery = " select ninterfacetypecode," + " coalesce(jsondata->'sinterfacetypename'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " jsondata->'sinterfacetypename'->>'en-US') as sinterfacetypename"
					+ " from interfacetype where  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ninterfacetypecode>0";
			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("sdisplaystatus");
			return new ResponseEntity<Object>((List<InterfaceType>)jdbcTemplate.query(strQuery, new InterfaceType()), HttpStatus.OK);
//			return new ResponseEntity<Object>(commonFunction.getMultilingualMessageList((List<Unit>) findBySinglePlainSql(strQuery, Unit.class),lstcolumns,userInfo.getSlanguagefilename()), HttpStatus.OK);
		}
		@Override
		public ResponseEntity<Object> getContainerType(final int ntestcode) throws Exception {
			final String sContTypeQry = "select ct.* from testcontainertype tct, containertype ct where ct.ncontainertypecode = tct.ncontainertypecode"
					+ " and ct.nstatus = tct.nstatus and tct.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tct.ntestcode = "+ntestcode;
			return new ResponseEntity<Object>((List<ContainerType>)jdbcTemplate.query(sContTypeQry, new ContainerType()), HttpStatus.OK);

		}
		
		
		@Override
		public ResponseEntity<Object> getEditReportInfoTest(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception {
			Map<String,Object> map = new HashMap<String,Object>();
			int ntestcode = (int) inputMap.get("ntestcode");
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(ntestcode);
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				String getEditQuery ="select * from reportinfotest where ntestcode ="+ntestcode+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				ReportInfoTest lst =(ReportInfoTest) jdbcUtilityFunction.queryForObject(getEditQuery, ReportInfoTest.class,jdbcTemplate);
				if(lst==null) {
					 return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THISRECORDHASNOREPORTFOUND", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else {
					map.put("ReportInfoTest", lst);
				}
			}
			return  new ResponseEntity<Object>(map, HttpStatus.OK);

		}
		
		@Override
		public ResponseEntity<Object> updateReportInfoTest(ReportInfoTest objReportInfoTest,UserInfo objUserInfo) throws Exception {
			final List<String> multilingualIDList  = new ArrayList<>();		
			final List<Object> listAfterUpdate = new ArrayList<>();		
			final List<Object> listBeforeUpdate = new ArrayList<>();
			
			final TestMaster testMasterById = testMasterCommonFunction.checkTestIsPresent(objReportInfoTest.getNtestcode());
			if(testMasterById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.TESTALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else {
				
				String oldquery ="select * from reportinfotest where ntestcode ="+objReportInfoTest.getNtestcode()+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				ReportInfoTest unit =(ReportInfoTest) jdbcUtilityFunction.queryForObject(oldquery, ReportInfoTest.class,jdbcTemplate);
				
				String  updatequery = " update reportinfotest set sconfirmstatement =N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSconfirmstatement())+"',"
									+ " sdecisionrule=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSdecisionrule())+"',ssopdescription=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSsopdescription())+"',"
									+ " stestcondition=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getStestcondition())+"', sdeviationcomments=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSdeviationcomments())+"',"
									+ " smethodstandard=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSmethodstandard())+"',sreference=N'"+stringUtilityFunction.replaceQuote(objReportInfoTest.getSreference())+"' "
									+ " where ntestcode ="+objReportInfoTest.getNtestcode()+"";
				jdbcTemplate.execute(updatequery);
				
				String newquery ="select * from reportinfotest where ntestcode ="+objReportInfoTest.getNtestcode()+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				ReportInfoTest objUnit =(ReportInfoTest) jdbcUtilityFunction.queryForObject(newquery, ReportInfoTest.class,jdbcTemplate);

				listAfterUpdate.add(objUnit);
			    listBeforeUpdate.add(unit);
				
				multilingualIDList.add("IDS_REPORTINFOTEST");			
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, objUserInfo);						
				return getActiveTestById(objUserInfo,objReportInfoTest.getNtestcode());

				
			}
			
		}
		
		@Override
		public ResponseEntity<Object> getTestPackage(final int ntestcode) throws Exception {
			final String sContTypeQry = "select tp.*,tp.ntestpackagecode from testpackage tp, testpackagetest tpt where tpt.ntestpackagecode = tp.ntestpackagecode"
					+ " and tp.ntestpackagecode>0 and  tpt.nstatus = tp.nstatus and tpt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpt.ntestcode = "+ntestcode;
			return new ResponseEntity<Object>(jdbcTemplate.query(sContTypeQry, new TestPackageTest()), HttpStatus.OK);
		}
		
		@Override
		public ResponseEntity<Object> getDestinationUnit(final int nunitcode,UserInfo userInfo) throws Exception {
			Map<String,Object> map =new HashMap<String, Object>();
			final String sContTypeQry = " select uc.*,u1.sunitname as sunitname1  from unitconversion uc "
									  + " join unit u on uc.nsourceunitcode = u.nunitcode "
									  + " join unit u1 on uc.ndestinationunitcode = u1.nunitcode "
									  + " where uc.nsourceunitcode="+nunitcode+" and uc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									  + " and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and uc.nsitecode= "+userInfo.getNmastersitecode()+" ";
			List<UnitConversion> lst =jdbcTemplate.query(sContTypeQry, new UnitConversion());
			map.put("DestinationUnit",lst);
			return new ResponseEntity<>(map,HttpStatus.OK);
			
			
		}
		
	//ALPD-3521
		@Override
		public ResponseEntity<Object> getConversionOperator(final int nsourceunitcode,final int ndestinationunitcode,UserInfo userInfo) throws Exception {
			Map<String,Object> map =new HashMap<String, Object>();
			final String sContTypeQry = " select uc.ndestinationunitcode,uc.nsourceunitcode,uc.nunitconversioncode,uc.nconversionfactor,uc.noperatorcode,op.soperator from unitconversion uc,operators op  where nsourceunitcode = "+nsourceunitcode+" and ndestinationunitcode = "+ndestinationunitcode+" "
									  + " and op.noperatorcode=uc.noperatorcode and  op.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									  +" and uc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									  + " and uc.nsitecode= "+userInfo.getNmastersitecode()+" ";
			List<UnitConversion> lst =jdbcTemplate.query(sContTypeQry, new UnitConversion());
			map.put("UnitConversion",lst);
			return new ResponseEntity<>(map,HttpStatus.OK);
			
			
		}

	

}
