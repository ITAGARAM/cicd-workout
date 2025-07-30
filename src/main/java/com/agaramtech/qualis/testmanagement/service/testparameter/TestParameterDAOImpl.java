package com.agaramtech.qualis.testmanagement.service.testparameter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.testgroup.model.ResultAccuracy;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupmanagement;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.DynamicField;
import com.agaramtech.qualis.testmanagement.model.DynamicFormulaFields;
import com.agaramtech.qualis.testmanagement.model.Functions;
import com.agaramtech.qualis.testmanagement.model.Operators;
import com.agaramtech.qualis.testmanagement.model.PreDefinedFormula;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.service.testcategory.TestCategoryDAO;
import com.agaramtech.qualis.testmanagement.service.testmaster.TestMasterCommonFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository

public class TestParameterDAOImpl implements TestParameterDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestParameterDAOImpl.class);
	  
	  private final StringUtilityFunction stringUtilityFunction; 
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final ProjectDAOSupport projectDAOSupport; 
	  private final DateTimeUtilityFunction dateUtilityFunction; 
	  private final AuditUtilityFunction auditUtilityFunction;
	  private final TestMasterCommonFunction testMasterCommonFunction;
	  private final TestCategoryDAO objTestCategoryDAO;
	
	@Override
	public ResponseEntity<Object> createTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite)
			throws Exception {

		final String sTableLockQuery = " lock  table locktestmaster "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);
		
		Map<String, Object> outMap = new HashMap<String, Object>();
		if (objTestParameter != null) {
			String strQuery = "select * from testmaster where ntestcode = " + objTestParameter.getNtestcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final TestMaster objTest = (TestMaster) jdbcUtilityFunction.queryForObject(strQuery, TestMaster.class,jdbcTemplate); 
		 
			if (objTest != null) {
				strQuery = "select ntestparametercode from testparameter where sparametername=N'"
						+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametername()) + "' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode="
						+ objTestParameter.getNtestcode();
				final TestParameter objTestParam = (TestParameter) jdbcUtilityFunction.queryForObject(strQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParam != null) {
					// Conflict = 409 - Duplicate entry
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				} else {
					final List<Object> lstObject = new ArrayList<>();
					final List<String> lstString = new ArrayList<>();
					final String queryunit = "select * from unit where nunitcode = " + objTestParameter.getNunitcode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					final Unit activeunit = (Unit) jdbcUtilityFunction.queryForObject(queryunit, Unit.class,jdbcTemplate); 
					
					if (activeunit != null) {
						String seqtestparamquery = "select nsequenceno from SeqNoTestManagement where stablename='testparameter'";
						int nseqtestparamcount = jdbcTemplate.queryForObject(seqtestparamquery, Integer.class);
						
						nseqtestparamcount++;

						if (objTestParameter.getNoperatorcode() == 0) {
							objTestParameter.setNoperatorcode((short) -1);
						}
						// Added by sonia on 03-10-2022
						
						String inserttestparamquery = " Insert into testparameter(ntestparametercode,ntestcode,nparametertypecode,nunitcode,ndestinationunitcode,noperatorcode,nconversionfactor,sparametername,sparametersynonym,"
								+ " nroundingdigits,nisadhocparameter,ndeltachecklimitcode,ndeltacheck,ndeltacheckframe,ndeltaunitcode,nisvisible,dmodifieddate,nsitecode,nstatus,nresultaccuracycode)"
								+ " values (" + nseqtestparamcount + "," + objTestParameter.getNtestcode() + ","
								+ objTestParameter.getNparametertypecode() + "," + objTestParameter.getNunitcode() + ","
								+ objTestParameter.getNdestinationunitcode() + "," + objTestParameter.getNoperatorcode()
								+ "," + objTestParameter.getNconversionfactor() + ",N'"
								+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametername()) + "'," + " N'"
								+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym()) + "',"
								+ objTestParameter.getNroundingdigits() + "," + objTestParameter.getNisadhocparameter()
								+ "," + objTestParameter.getNdeltachecklimitcode() + ","
								+ objTestParameter.getNdeltacheck() + "," + objTestParameter.getNdeltacheckframe() + ","
								+ objTestParameter.getNdeltaunitcode() + "," + objTestParameter.getNisvisible() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objTestParameter.getNresultaccuracycode()+")";
						jdbcTemplate.execute(inserttestparamquery);

						String updatetestparamquery = "update SeqNoTestManagement set nsequenceno ="
								+ nseqtestparamcount + " where stablename ='testparameter'";
						jdbcTemplate.execute(updatetestparamquery);

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables. 
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							
					//ALPD-5269--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while add the parameter
							String stestgroupQuery1="select * from testgrouptestparameter where "
									+ " ntestgrouptestcode=(select ntestgrouptestcode from "
									+ " testgrouptest where  nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and  "
									+ "  ntestcode="+objTestParameter.getNtestcode()+" "
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" )";
							
							List<TestGroupTestParameter> objTestGrouptestParameter =  jdbcTemplate.query(stestgroupQuery1, new TestGroupTestParameter());
							
							if(objTestGrouptestParameter.isEmpty()){
								final String updateQuery="update testgrouptest set nstatus = "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
										+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
										+ " where ntestgrouptestcode=(select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+");";
								jdbcTemplate.execute(updateQuery);

							}
							
							
							
							StringBuilder testGroupBuilder=new StringBuilder();
							final String sQuery = "select nsequenceno,stablename from seqnotestgroupmanagement where stablename in (N'testgrouptest',"
									+ "N'testgrouptestcharparameter',N'testgrouptestfile',N'testgrouptestformula',N'testgrouptestnumericparameter',N'testgrouptestparameter',"
									+ "N'testgrouptestpredefparameter')";
							
							List<SeqNoTestGroupmanagement> lstSeqNo =  jdbcTemplate.query(sQuery,new SeqNoTestGroupmanagement());
							
							Map<String, Integer> seqMap = lstSeqNo.stream()	.collect(Collectors.toMap(SeqNoTestGroupmanagement::getStablename,seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
						
							int ntestgrouptestparametercode=seqMap.get("testgrouptestparameter")+1;

							
							
							testGroupBuilder.append("update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestparametercode+" where stablename ='testgrouptestparameter';");

							//ALPD-5269--Vignesh R(27-01-2025)-->Test Master-->500 error thrown while add the parameter
							String stestgroupQuery="select * from testgrouptest where nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and ntestcode="+objTestParameter.getNtestcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							
							    TestGroupTest objecTestGroupTest=(TestGroupTest) jdbcUtilityFunction.queryForObject(stestgroupQuery,TestGroupTest.class,jdbcTemplate);

							
								String testParameterSorterQuery="select * from testgrouptestparameter where ntestgrouptestcode="+objecTestGroupTest.getNtestgrouptestcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc;";
								int testParameterSorter=0;
								List<TestGroupTestParameter> objTestGroupTestParameter=jdbcTemplate.query(testParameterSorterQuery, new TestGroupTestParameter());
								if(objTestGroupTestParameter.isEmpty()){
									testParameterSorter=testParameterSorter+1;
								}else {
									testParameterSorter=objTestGroupTestParameter.get(0).getNsorter()+1;
									
								}
								testGroupBuilder.append( "insert into testgrouptestparameter (ntestgrouptestparametercode, ntestgrouptestcode, ntestparametercode, nparametertypecode, nunitcode, sparametersynonym,"
										+ "nroundingdigits, nresultmandatory, nreportmandatory, ngradingmandatory, nchecklistversioncode, sspecdesc, nsorter, nisadhocparameter, nisvisible,dmodifieddate, nstatus,nsitecode,nresultaccuracycode)"
										+ " values (" + ntestgrouptestparametercode + "," + objecTestGroupTest.getNtestgrouptestcode() + "," + nseqtestparamcount
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
						
						
						
						
						objTestParameter.setNtestparametercode(nseqtestparamcount);
						lstObject.add(objTestParameter);
						lstString.add("IDS_ADDTESTPARAMETER");
						if (objTestParameter.getObjPredefinedParameter() != null) {
							TestPredefinedParameter objPredefinedParam = objTestParameter.getObjPredefinedParameter();
							strQuery = "select ntestpredefinedcode from testpredefinedparameter where ntestparametercode = "
									+ objTestParameter.getNtestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							List<TestPredefinedParameter> lstpre = (List<TestPredefinedParameter>) jdbcTemplate.query(strQuery, new TestPredefinedParameter());
							if (lstpre != null & lstpre.size() == 0) {
								objPredefinedParam.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							} else {
								objPredefinedParam.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							}

							objPredefinedParam.setNtestparametercode(objTestParameter.getNtestparametercode());

							String seqtpredefinedquery = "select nsequenceno from SeqNoTestManagement where stablename='testpredefinedparameter'";
							int nseqpredefinedcount = jdbcTemplate.queryForObject(seqtpredefinedquery,
									Integer.class);
							nseqpredefinedcount++;

//						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,ndefaultstatus,nstatus)"
//													+" values ("+nseqpredefinedcount+","+objPredefinedParam.getNtestparametercode()+","+objPredefinedParam.getNgradecode()+",N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname())+"',"
//													+" "+objPredefinedParam.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
							// Commented by sonia on 03-10-2022
//						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,sresultparacomment,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,ndefaultstatus,nstatus)"
//								+" values ("+nseqpredefinedcount+","+objPredefinedParam.getNtestparametercode()+","+objPredefinedParam.getNgradecode()+",N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname())+"',N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSresultparacomment())+"','"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//								+", "+objPredefinedParam.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";

							// Added by sonia on 03-10-2022
//						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,sresultparacomment,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
//								+" values ("+nseqpredefinedcount+","+objPredefinedParam.getNtestparametercode()+","+objPredefinedParam.getNgradecode()+",N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname())+"',N'"+stringUtilityFunction.replaceQuote(objPredefinedParam.getSresultparacomment())+"','"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"' "
//								+", "+objPredefinedParam.getNdefaultstatus()+","+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
//
							String insertpredefinedquery = " Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,spredefinedsynonym,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
									+ " values (" + nseqpredefinedcount + ","
									+ objPredefinedParam.getNtestparametercode() + ","
									+ objPredefinedParam.getNgradecode() + ",N'"
									+ stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedname()) + "',N'"
									+ stringUtilityFunction.replaceQuote(objPredefinedParam.getSpredefinedsynonym()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + ", "
									+ objPredefinedParam.getNdefaultstatus() + "," + objUserInfo.getNmastersitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(insertpredefinedquery);

							String updatepredefinedquery = "update SeqNoTestManagement set nsequenceno ="
									+ nseqpredefinedcount + " where stablename ='testpredefinedparameter'";
							jdbcTemplate.execute(updatepredefinedquery);
							objPredefinedParam.setNtestpredefinedcode(nseqpredefinedcount);

							lstObject.add(objPredefinedParam);
							lstString.add("IDS_ADDTESTPREDEFINEDPARAMETER");
						}
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, lstString, objUserInfo);
						outMap = testMasterCommonFunction.getParameterDetailsById(testMasterCommonFunction.getTestParameter(objTestParameter.getNtestcode(),
								objTestParameter.getNtestparametercode(), objUserInfo).get(0), objUserInfo);
						outMap.put("TestParameter", testMasterCommonFunction.getTestParameter(objTestParameter.getNtestcode(), 0, objUserInfo));
						return new ResponseEntity<Object>(outMap, HttpStatus.OK);
					} else {
						// Conflict = 417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_UNITALREADYDELETED",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
			} else {
				// Conflict = 417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			outMap.put("TestParameter", new ArrayList<TestParameter>());
			return new ResponseEntity<Object>(outMap, HttpStatus.OK);
		}
	}
	
	
	@Override
	public Map<String, Object> updateTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite)
			throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();

		String strQuery = "select * from testmaster where ntestcode = " + objTestParameter.getNtestcode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final TestMaster objTest = (TestMaster) jdbcUtilityFunction.queryForObject(strQuery, TestMaster.class,jdbcTemplate); 

		if (objTest != null) {
			strQuery = "select ntestparametercode,nparametertypecode from testparameter where " + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestparametercode = "
					+ objTestParameter.getNtestparametercode();
			TestParameter objTP = (TestParameter) jdbcUtilityFunction.queryForObject(strQuery, TestParameter.class,jdbcTemplate);

			if (objTP == null) {
				objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus());
			} else {
				strQuery = "select ntestparametercode,nparametertypecode from testparameter where sparametername=N'"
						+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametername()) + "' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode="
						+ objTestParameter.getNtestcode() + " and ntestparametercode <> "
						+ objTestParameter.getNtestparametercode();
				TestParameter objTestParam = (TestParameter) jdbcUtilityFunction.queryForObject(strQuery, TestParameter.class,jdbcTemplate);
	
				if (objTestParam != null) {
					objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus());
				} else {
					final String strQuery1 = "select ntestpredefinedcode from testpredefinedparameter where ntestparametercode = "
							+ objTestParameter.getNtestparametercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();

					final List<TestPredefinedParameter> lstpre = (List<TestPredefinedParameter>) jdbcTemplate.query(strQuery1, new TestPredefinedParameter());
					
					String updateQuery = "";

					final List<Object> lstdeleteObject = new ArrayList<>();
					final List<String> lstdeleteString = new ArrayList<>();

					if (objTP.getNparametertypecode() != objTestParameter.getNparametertypecode()
							&& objTP.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype()) {
						String sQuery = "";

						sQuery = "select * from testparameternumeric where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						List<TestParameterNumeric> lstTestParameterNumeric = jdbcTemplate.query(sQuery,
								new TestParameterNumeric());

						// sQuery = sQuery + "select * from testformula where ntestparametercode =
						// "+objTestParameter.getNtestparametercode()+" and nstatus =
						// "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						sQuery = "select * from testformula where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						List<TestFormula> lstTestFormula = jdbcTemplate.query(sQuery, new TestFormula());
						List<Object> oldLst = new ArrayList<Object>();
						oldLst.add(lstTestParameterNumeric);
						oldLst.add(lstTestFormula);

						updateQuery = updateQuery + "update testparameternumeric set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + ";";
						updateQuery = updateQuery + "update testformula set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + ";";

						lstdeleteString.add("IDS_DELETESPECLIMIT");
						lstdeleteString.add("IDS_DELETETESTFORMULA");
						lstdeleteObject.addAll(oldLst);
						
							
					} else if (objTP.getNparametertypecode() != objTestParameter.getNparametertypecode() && objTP
							.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED.getparametertype()) {
						String sQuery = "select * from testpredefinedparameter where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						updateQuery = "update testpredefinedparameter set nstatus = "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + ";";
						
						final List<TestPredefinedParameter> lstPredefined = (List<TestPredefinedParameter>) jdbcTemplate.query(sQuery, new TestPredefinedParameter());
						
						
						lstdeleteObject.add(lstPredefined);
						lstdeleteString.add("IDS_DELETETESTPREDEFINEDPARAMETER");
					}
					
					final TestParameter oldTestParameter = (TestParameter) jdbcTemplate.queryForObject(
							"select tp.*, pt.sdisplaystatus from TestParameter tp, ParameterType pt where ntestparametercode ="
									+ objTestParameter.getNtestparametercode() + " and pt.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and pt.nparametertypecode=tp.nparametertypecode",
							new TestParameter());
//					updateQuery = updateQuery + "update testparameter set sparametername = N'"+ReplaceQuote(objTestParameter.getSparametername())
//								+"',sparametersynonym=N'"+ReplaceQuote(objTestParameter.getSparametersynonym())
//								+"',nparametertypecode="+objTestParameter.getNparametertypecode()+",nunitcode= "+objTestParameter.getNunitcode()
//								+","+ "nroundingdigits= "+objTestParameter.getNroundingdigits()+" where ntestparametercode = "+objTestParameter.getNtestparametercode()+";";

//					updateQuery = updateQuery + "update testparameter set sparametername = N'"+ReplaceQuote(objTestParameter.getSparametername())
//					+"',sparametersynonym=N'"+ReplaceQuote(objTestParameter.getSparametersynonym())
//					+"',nparametertypecode="+objTestParameter.getNparametertypecode()+",nunitcode= "+objTestParameter.getNunitcode()+","+ "ndeltachecklimitcode= "+objTestParameter.getNdeltachecklimitcode()+","+ "ndeltacheck= "+objTestParameter.getNdeltacheck()+","+ "ndeltacheckframe= "+objTestParameter.getNdeltacheckframe()+","+ "ndeltaunitcode= "+objTestParameter.getNdeltaunitcode()
//					+","+ "nroundingdigits= "+objTestParameter.getNroundingdigits()
//					+ ", dmodifieddate ='" + getCurrentDateTime(objUserInfo)
//    				+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//    				+",noffsetdmodifieddate= "+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//					+" where ntestparametercode = "+objTestParameter.getNtestparametercode()+";";
					oldTestParameter.setSdisplaystatus(commonFunction.getMultilingualMessage(
							oldTestParameter.getSdisplaystatus(), objUserInfo.getSlanguagefilename()));

					final String queryunit = "select * from unit where nunitcode = " + objTestParameter.getNunitcode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					final Unit activeunit = (Unit) jdbcUtilityFunction.queryForObject(queryunit, Unit.class,jdbcTemplate); 
					if (activeunit != null) {

						updateQuery = updateQuery + "update testparameter set sparametername = N'"
								+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametername()) + "',sparametersynonym=N'"
								+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym()) + "',nparametertypecode="
								+ objTestParameter.getNparametertypecode() + ",nunitcode= "
								+ objTestParameter.getNunitcode() + "," + "  ndestinationunitcode ="
								+ objTestParameter.getNdestinationunitcode() + "," + " noperatorcode="
								+ objTestParameter.getNoperatorcode() + "" + ",nconversionfactor="
								+ objTestParameter.getNconversionfactor() + ", " + "ndeltachecklimitcode= "
								+ objTestParameter.getNdeltachecklimitcode() + "," + "ndeltacheck= "
								+ objTestParameter.getNdeltacheck() + "," + "ndeltacheckframe= "
								+ objTestParameter.getNdeltacheckframe() + "," + "ndeltaunitcode= "
								+ objTestParameter.getNdeltaunitcode() + "," + "nroundingdigits= "
								+ objTestParameter.getNroundingdigits() + "," + "nresultaccuracycode= "
								+ objTestParameter.getNresultaccuracycode() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + " where ntestparametercode = "
								+ objTestParameter.getNtestparametercode() + ";";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables. 
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							//ALPD-5525--Vignesh(07-03-2025)-->Test Master screen -> 500 error is occurring, when edit the rounding digits and result accuracy. ( Lucid )
							final String testParameterSorterQ = "SELECT * FROM testgrouptestparameter ttp, testgrouptest tgt " +
							         "WHERE ttp.ntestparametercode = " + objTestParameter.getNtestparametercode() + 
							         " AND tgt.ntestgrouptestcode = ttp.ntestgrouptestcode " +
							         " AND tgt.ntestcode = " + objTestParameter.getNtestcode() + 
							         " AND tgt.nspecsampletypecode = " + Enumeration.Default.DEFAULTSPEC.getDefault() + 
							         " AND tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + 
							         " AND ttp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							
							int testParameterSorter=0;
							TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQ,TestGroupTestParameter.class,jdbcTemplate); 
							
							if(objTestGroupTestParameter==null) {
								testParameterSorter=testParameterSorter+1;
							}else {
								testParameterSorter=objTestGroupTestParameter.getNsorter();
								}
							
						updateQuery=updateQuery+"update testgrouptestparameter set nparametertypecode = "
						+ objTestParameter.getNparametertypecode() + ", nunitcode = " + objTestParameter.getNunitcode()
						+ ", sparametersynonym = N'" + stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym()) + "',"
						+ " nroundingdigits = " + objTestParameter.getNroundingdigits() + ", nresultmandatory = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() +",nisadhocparameter="+objTestParameter.getNisadhocparameter()+",nreportmandatory = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ ", nchecklistversioncode = -1, sspecdesc =NULL, nsorter = "
						+ testParameterSorter+ ","
						+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
						+ " nresultaccuracycode="+objTestParameter.getNresultaccuracycode()
						+ " where ntestgrouptestparametercode = "
						+ objTestGroupTestParameter.getNtestgrouptestparametercode() + ";";
						
						final String stestgroupTestQuery="select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+""
								+ " and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						final int ntestgrouptestcode=jdbcTemplate.queryForObject(stestgroupTestQuery, Integer.class);
						
						
						String UpdateQueryTestGroup="";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(objTP.getNparametertypecode() != objTestParameter.getNparametertypecode() && objTP
							.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype()){
							
							UpdateQueryTestGroup ="update testgrouptestnumericparameter set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where  ntestgrouptestparametercode =any(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
											+""+ntestgrouptestcode + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
								
							UpdateQueryTestGroup = UpdateQueryTestGroup+"update testgrouptestformula set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where  ntestgrouptestcode ="+ntestgrouptestcode+";";
									
							jdbcTemplate.execute(UpdateQueryTestGroup);

							}
						else if(objTP.getNparametertypecode() != objTestParameter.getNparametertypecode() && objTP
							.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED.getparametertype()) {
						
							UpdateQueryTestGroup = UpdateQueryTestGroup+"update testgrouptestpredefparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where  ntestgrouptestparametercode =any(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
									+""+ntestgrouptestcode + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
							jdbcTemplate.execute(UpdateQueryTestGroup);

						}
						else if(objTP.getNparametertypecode() != objTestParameter.getNparametertypecode() && objTP
								.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype()) {
						
							UpdateQueryTestGroup = UpdateQueryTestGroup+"update testgrouptestcharparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where  ntestgrouptestparametercode =any(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
									+""+ntestgrouptestcode + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
							jdbcTemplate.execute(UpdateQueryTestGroup);

								}
						}
						
						
						final List<Object> lstObject = new ArrayList<>();
						final List<String> lstString = new ArrayList<>();
						lstObject.add(objTestParameter);

						List<Object> lstOldObject = new ArrayList<>();
						lstOldObject.add(oldTestParameter);
						lstString.add("IDS_EDITTESTPARAMETER");

						auditUtilityFunction.fnInsertAuditAction(lstObject, 2, lstOldObject, lstString, objUserInfo);
						if (objTP.getNparametertypecode() != Enumeration.ParameterType.PREDEFINED.getparametertype()
								&& objTestParameter.getObjPredefinedParameter() != null) {
							TestPredefinedParameter objtestpredefined = objTestParameter.getObjPredefinedParameter();
							if (lstpre.isEmpty()) {
								objtestpredefined.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.YES.gettransactionstatus());
							} else {
								objtestpredefined.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							}

							String seqtpredefinedquery = "select nsequenceno from SeqNoTestManagement where stablename='testpredefinedparameter'";
							int nseqpredefinedcount = jdbcTemplate.queryForObject(seqtpredefinedquery,
									Integer.class);
							nseqpredefinedcount++;

//						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,ndefaultstatus,nstatus)"
//													+" values ("+nseqpredefinedcount+","+objtestpredefined.getNtestparametercode()+","+objtestpredefined.getNgradecode()+",N'"+ReplaceQuote(objtestpredefined.getSpredefinedname())+"',"
//													+" "+objtestpredefined.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
							// Commented by sonia on 03-10-2022
//						String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,sresultparacomment,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,ndefaultstatus,nstatus)"
//								+" values ("+nseqpredefinedcount+","+objtestpredefined.getNtestparametercode()+","+objtestpredefined.getNgradecode()+",N'"+ReplaceQuote(objtestpredefined.getSpredefinedname())+"',N'"+ReplaceQuote(objtestpredefined.getSresultparacomment())+"','"+getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNtimezonecode()+","+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//								+", "+objtestpredefined.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";

							// Added by sonia on 03-10-2022
							String insertpredefinedquery = " Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,spredefinedsynonym,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
									+ " values (" + nseqpredefinedcount + ","
									+ objtestpredefined.getNtestparametercode() + ","
									+ objtestpredefined.getNgradecode() + ",N'"
									+ stringUtilityFunction.replaceQuote(objtestpredefined.getSpredefinedname()) + "',N'"
									+ stringUtilityFunction.replaceQuote(objtestpredefined.getSpredefinedsynonym()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objtestpredefined.getNdefaultstatus()
									+ "," + objUserInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
							
								//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
								if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									
									String testParameterSorterQ="select * from testgrouptestparameter where ntestparametercode="+objTestParameter.getNtestparametercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
									TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQ,TestGroupTestParameter.class,jdbcTemplate); 
									

									final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename in ('testgrouptestpredefparameter')";
							   		int ntestgrouptestpredefcode =  jdbcTemplate.queryForObject(sQuery,Integer.class);
							   		ntestgrouptestpredefcode++;
							   		
							   		insertpredefinedquery=insertpredefinedquery+"update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestpredefcode+" where stablename ='testgrouptestpredefparameter';";

									insertpredefinedquery= insertpredefinedquery+"insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
											+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode) values (" + ntestgrouptestpredefcode+ "," + objTestGroupTestParameter.getNtestgrouptestparametercode()+","
											+ objtestpredefined.getNgradecode() + "," + "N'"
											+ stringUtilityFunction.replaceQuote(objtestpredefined.getSpredefinedname()) + "',N'"+stringUtilityFunction.replaceQuote(objtestpredefined.getSpredefinedsynonym()) + "',"
											+ objtestpredefined.getNdefaultstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+");";
								
								}
								jdbcTemplate.execute(insertpredefinedquery);

							
							

							String updatepredefinedquery = "update SeqNoTestManagement set nsequenceno ="
									+ nseqpredefinedcount + " where stablename ='testpredefinedparameter'";
							jdbcTemplate.execute(updatepredefinedquery);

							lstdeleteObject.add(Arrays.asList(objtestpredefined));
							lstdeleteString.add("IDS_ADDTESTPREDEFINEDPARAMETER");
						} else if (objTP.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
								.getparametertype() && objTestParameter.getObjPredefinedParameter() != null) {
							final TestPredefinedParameter objTestPredfeinedParameter = objTestParameter
									.getObjPredefinedParameter();

//						updateQuery = updateQuery +"update testpredefinedparameter set ngradecode ="+objTestPredfeinedParameter.getNgradecode()
//									+" , spredefinedname = N'"+ReplaceQuote(objTestPredfeinedParameter.getSpredefinedname())+"' , sresultparacomment = N'"+ReplaceQuote(objTestPredfeinedParameter.getSresultparacomment())
//									+ "', dmodifieddate ='"+getCurrentDateTime(objUserInfo)
//									+"' where ntestpredefinedcode ="+objTestPredfeinedParameter.getNtestpredefinedcode()+";";
//						
							updateQuery = updateQuery + "update testpredefinedparameter set ngradecode ="
									+ objTestPredfeinedParameter.getNgradecode() + " , spredefinedname = N'"
									+ stringUtilityFunction.replaceQuote(objTestPredfeinedParameter.getSpredefinedname())
									+ "' , spredefinedsynonym = N'"
									+ stringUtilityFunction.replaceQuote(objTestPredfeinedParameter.getSpredefinedsynonym())
									+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "' where ntestpredefinedcode ="
									+ objTestPredfeinedParameter.getNtestpredefinedcode() + ";";
							//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
							if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								String predefupdateQuery="";
							  	 String query1="select * from testgrouptestpredefparameter where  ntestgrouptestparametercode=(select ttp.ntestgrouptestparametercode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
							   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredfeinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
							   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and spredefinedname=N'"+objTestPredfeinedParameter.getSpredefinedname()+"'";
							   		final TestGroupTestPredefinedParameter newTestGroupTestPredefinedParameter=(TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(query1,TestGroupTestPredefinedParameter.class,jdbcTemplate); 
									
							   		predefupdateQuery="update testgrouptestpredefparameter set ngradecode = "+objTestPredfeinedParameter.getNgradecode()+""
										+ ", spredefinedname = N'" + stringUtilityFunction.replaceQuote(objTestPredfeinedParameter.getSpredefinedname()) + "'"
										+ ", spredefinedsynonym = N'" + stringUtilityFunction.replaceQuote(objTestPredfeinedParameter.getSpredefinedsynonym()) + "'"							
										+ ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestgrouptestpredefcode = "
										+ newTestGroupTestPredefinedParameter.getNtestgrouptestpredefcode() + ";";
							   		jdbcTemplate.execute(predefupdateQuery);

							}
							final TestPredefinedParameter oldTestPredefinedParameter = (TestPredefinedParameter) jdbcTemplate
									.queryForObject(
											"select * from testpredefinedparameter where ntestpredefinedcode ="
													+ objTestPredfeinedParameter.getNtestpredefinedcode(),
											new TestPredefinedParameter());

							lstObject.clear();
							lstOldObject.clear();
							lstObject.add(objTestPredfeinedParameter);
							lstOldObject.add(oldTestPredefinedParameter);
							auditUtilityFunction.fnInsertAuditAction(lstObject, 2, lstOldObject,
									Arrays.asList("IDS_EDITTESTPREDEFINEDPARAMETER"), objUserInfo);
						}
					//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.	
				else if(objTP.getNparametertypecode()!=objTestParameter.getNparametertypecode() && objTestParameter.getNparametertypecode()==Enumeration.ParameterType.CHARACTER.getparametertype()&&isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			
				final String testParameterSorterQuery="select * from testgrouptestparameter tp,testgrouptest tgt where tp.ntestparametercode="+objTestParameter.getNtestparametercode()+" and "
											+ " tgt.ntestgrouptestcode=tp.ntestgrouptestcode and  ntestcode="+objTestParameter.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and "
											+ " tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQuery,TestGroupTestParameter.class,jdbcTemplate); 
			
			final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename ='testgrouptestcharparameter'";
			int ntestgrouptestcharparametercode=jdbcTemplate.queryForObject(sQuery, Integer.class);
			
			ntestgrouptestcharparametercode++;
			
			String updateQuery1="update seqnotestgroupmanagement set nsequenceno =" + ntestgrouptestcharparametercode
					+ " where stablename ='testgrouptestcharparameter';";
			
			updateQuery1=updateQuery1+"insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate,nstatus,nsitecode)"
					+ " values (" + ntestgrouptestcharparametercode + "," + objTestGroupTestParameter.getNtestgrouptestparametercode()+",NULL,'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+");";
	
			jdbcTemplate.execute(updateQuery1);
			
			}
			
				else if(objTP.getNparametertypecode()!=objTestParameter.getNparametertypecode() && objTestParameter.getNparametertypecode()==Enumeration.ParameterType.NUMERIC.getparametertype()&&isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							String testParameterSorterQuery="select * from testgrouptestparameter tp,testgrouptest tgt where tp.ntestparametercode="+objTestParameter.getNtestparametercode()+" and "
									+ " tgt.ntestgrouptestcode=tp.ntestgrouptestcode and  ntestcode="+objTestParameter.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and "
									+ " tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQuery,TestGroupTestParameter.class,jdbcTemplate); 
							
							final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename ='testgrouptestnumericparameter'";
							int ntestgrouptestnumericparametercode=jdbcTemplate.queryForObject(sQuery, Integer.class);
							ntestgrouptestnumericparametercode++;
							String updatenumericquery = "update seqnotestgroupmanagement set nsequenceno =" + ntestgrouptestnumericparametercode
									+ " where stablename ='testgrouptestnumericparameter';";
							jdbcTemplate.execute(updatenumericquery);


							updatenumericquery=updatenumericquery+"insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
									+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
									+ " values (" + ntestgrouptestnumericparametercode + ", " +objTestGroupTestParameter.getNtestgrouptestparametercode()+ ", NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,"
									+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+",-1);";
							jdbcTemplate.execute(updatenumericquery);

						}
						jdbcTemplate.execute(updateQuery);

//					final List<Object> lstObject = new ArrayList<>();
//					final List<String> lstString = new ArrayList<>();
//					lstObject.add(objTestParameter);

//					List<Object> lstOldObject = new ArrayList<>();
//					lstOldObject.add(oldTestParameter);
//					lstString.add("IDS_EDITTESTPARAMETER");

						auditUtilityFunction.fnInsertListAuditAction(lstdeleteObject, 1, null, lstdeleteString, objUserInfo);

						objMap.put("TestParameter", objTestParameter);
						objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					} else {
//						String unit = "IDS_UNIT";
//						objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_UNIT"+ Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus());
						objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								commonFunction.getMultilingualMessage("IDS_UNIT", objUserInfo.getSlanguagefilename())
										+ " "
										+ commonFunction.getMultilingualMessage(
												Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
												objUserInfo.getSlanguagefilename()));
					}
				}
			}
		} else {
			objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_TESTALREADYDELETED");
		}
		return objMap;
	}
	

	@Override
	public ResponseEntity<Object> getTestParameterAfterUpdate(final TestParameter objTestParameter,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outMap = new HashMap<String, Object>();
		final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(objTestParameter.getNtestcode(), 0, objUserInfo);
		if (!lstTestParameter.isEmpty()) {
			outMap.put("TestParameter", lstTestParameter);
			final TestParameter objTestParameter1 = lstTestParameter.stream()
					.filter(source -> source.getNtestparametercode() == objTestParameter.getNtestparametercode())
					.collect(Collectors.toList()).get(0);
			outMap.putAll(testMasterCommonFunction.getParameterDetailsById(objTestParameter1, objUserInfo));
		}
		return new ResponseEntity<Object>(outMap, HttpStatus.OK);
	}
	

	@Override
	public ResponseEntity<Object> deleteTestParameter(final TestParameter objTestParameter, final UserInfo objUserInfo,final int isQualisLite)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String sQuery = "select * from testparameter where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestparametercode = "
				+ objTestParameter.getNtestparametercode();
		final TestParameter oldTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
		
		if (oldTestParameter != null) {
			
			//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
			String sdeleteQuery="";
			if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				sdeleteQuery=" and ntestgrouptestcode<>(select ntestgrouptestcode from testgrouptest where nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and ntestcode="+objTestParameter.getNtestcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
			}
			
			//ALPDJ21-26 - L.Subashini -16/07/2025 - Included delete validation for test formula
			final String scollate = "collate \"default\"";
			sQuery = " SELECT 'IDS_TESTFORMULA' as Msg from testformula where POSITION('$P" 
						+ objTestParameter.getNtestparametercode()
						+ "P$' IN (sformulacalculationcode " + scollate+ ") ) > 0 "					
					    + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        		+ " union all "
						+ " select 'IDS_TESTGROUP' as Msg from testgrouptestparameter where ntestparametercode = "
						+ objTestParameter.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "+sdeleteQuery+"";
			final ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(sQuery, objUserInfo);
			
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				// ALPD-5631 Added by Abdul on 01-Apr-2025 For validating last Test parameter as it shouldnt be deleted
				sQuery ="select count(ntestparametercode) from testparameter where ntestcode = "+objTestParameter.getNtestcode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int testParameterCount= jdbcTemplate.queryForObject(sQuery, Integer.class);
				if(testParameterCount > 1) {
								// ALPD-5631 End
				String updateQuery = "";
				if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
						.getparametertype()) {
					updateQuery = "update testpredefinedparameter set nstatus = "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparametercode = "
							+ objTestParameter.getNtestparametercode() + ";";
				//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
				if(isQualisLite== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						String stestgroupTestQuery="select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+""
								+ " and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						final int ntestgrouptestcode=jdbcTemplate.queryForObject(stestgroupTestQuery, Integer.class);
						
						updateQuery = updateQuery + "update testgrouptestpredefparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
								+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
					}
					
					
				} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
						.getparametertype()) {
					updateQuery = "update testformula set nstatus = "
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparametercode = "
							+ objTestParameter.getNtestparametercode() + ";";
					
				//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						String stestgroupTestQuery="select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+""
								+ " and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						final int ntestgrouptestcode=jdbcTemplate.queryForObject(stestgroupTestQuery, Integer.class);
						
					updateQuery = updateQuery + "update testgrouptestnumericparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
							+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
							+ " where ntestgrouptestparametercode =(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
							+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+"  and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
					
						updateQuery = updateQuery + "update testgrouptestformula set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
							+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
							+ " where ntestgrouptestparametercode in(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
							+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
					
						updateQuery = updateQuery + "update testgrouptestclinicalspec set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestparametercode in(select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
								+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
						
					}
				}
				else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
						.getparametertype()) {
				
					if(isQualisLite== Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						String stestgroupTestQuery="select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+""
								+ " and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						final int ntestgrouptestcode=jdbcTemplate.queryForObject(stestgroupTestQuery, Integer.class);
						
					updateQuery = updateQuery + "update testgrouptestcharparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
							+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
							+ " where ntestgrouptestparametercode = (select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
							+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+");";
					}
				}
//			updateQuery = updateQuery + "update testparameter set nstatus = "+Enumeration.TransactionStatus.NA.gettransactionstatus()
//			+ ", dmodifieddate ='" + getCurrentDateTime(objUserInfo)
//			+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//			+",noffsetdmodifieddate= "+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//			+" where ntestparametercode = "+objTestParameter.getNtestparametercode()+";";

				updateQuery = updateQuery + "update testparameter set nstatus = "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + " where ntestparametercode = "
						+ objTestParameter.getNtestparametercode() + ";";
				
				

				jdbcTemplate.execute(updateQuery);

			//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.	
			if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					
					String stestgroupTestQuery="select ntestgrouptestcode from testgrouptest where ntestcode="+objTestParameter.getNtestcode()+""
							+ " and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
					final int ntestgrouptestcode=jdbcTemplate.queryForObject(stestgroupTestQuery, Integer.class);
					
					 String updateQuery1 = "update testgrouptestparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
							+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
							+ " where ntestgrouptestparametercode=(select ntestgrouptestparametercode from testgrouptestparameter where  ntestgrouptestcode = "
							+ ntestgrouptestcode + " and ntestparametercode="+objTestParameter.getNtestparametercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
					
				
					 jdbcTemplate.execute(updateQuery1);
					
					String stestgroupQuery="select * from testgrouptestparameter where ntestgrouptestcode="+ntestgrouptestcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
					
					List<TestGroupTestParameter> objTestGroupTestParameter =  jdbcTemplate.query(stestgroupQuery, new TestGroupTestParameter());
					if(objTestGroupTestParameter.isEmpty()){
						updateQuery1="update testgrouptest set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
								+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
								+ " where ntestgrouptestcode="+ntestgrouptestcode+";";
						jdbcTemplate.execute(updateQuery1);

						
					}
					
				}
				List<Object> lstObject = new ArrayList<>();
				String str = "";
				str = "select tp.ntestparametercode, tp.ntestcode, tp.nparametertypecode, tp.nunitcode, tp.sparametername, tp.sparametersynonym"
						+ ", tp.nroundingdigits, tp.nisadhocparameter, tp.ndeltachecklimitcode, tp.ndeltacheck, tp.ndeltacheckframe, tp.ndeltaunitcode, tp.nisvisible, tp.nsitecode, tp.nstatus, pt.sdisplaystatus,tp.nresultaccuracycode from testparameter tp, parametertype pt where tp.ntestparametercode = "
						+ objTestParameter.getNtestparametercode()
						+ " and pt.nparametertypecode=tp.nparametertypecode ";
//			List<TestParameter> lsttp = (List<TestParameter>) commonFunction.getMultilingualMessageList(getJdbcTemplate().query(str,new TestParameter()), Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename());
				List<TestParameter> lsttp = objMapper.convertValue(
						commonFunction.getMultilingualMessageList(jdbcTemplate.query(str, new TestParameter()),
								Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),
						new TypeReference<List<TestParameter>>() {
						});

				str = "select * from testpredefinedparameter where ntestparametercode in ("
						+ objTestParameter.getNtestparametercode() + ")";
				List<TestPredefinedParameter> lstTestPredefinedParameter = jdbcTemplate.query(str,
						new TestPredefinedParameter());

				str = "select * from testformula where ntestparametercode in ("
						+ objTestParameter.getNtestparametercode() + ");";
				List<TestFormula> lstTestFormula = jdbcTemplate.query(str, new TestFormula());
				lstObject.add(lsttp);
				lstObject.add(lstTestPredefinedParameter);
				lstObject.add(lstTestFormula);
				auditUtilityFunction.fnInsertListAuditAction(lstObject, 1, null, Arrays.asList("IDS_DELETETESTPARAMETER",
						"IDS_DELETETESTPREDEFINEDPARAMETER", "IDS_DELETETESTFORMULA"), objUserInfo);
				Map<String, Object> outMap = new HashMap<String, Object>();
				final List<TestParameter> lstTestParameter = testMasterCommonFunction.getTestParameter(objTestParameter.getNtestcode(), 0,
						objUserInfo);
				if (lstTestParameter != null && lstTestParameter.size() > 0) {
					outMap = testMasterCommonFunction.getParameterDetailsById(lstTestParameter.get(lstTestParameter.size() - 1), objUserInfo);
				}
				outMap.put("TestParameter", lstTestParameter);
				return new ResponseEntity<Object>(outMap, HttpStatus.OK);
			}else {
				// ALPD-5631 Added by Abdul on 01-Apr-2025 For validating last Test parameter as it shouldnt be deleted
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ATLEASTONEPARAMETERSHOULDBEAVAILABLEFORTEST",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
                 // ALPD-5631 End
			}
			} else {
				// status code:417
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	

	@Override
	public ResponseEntity<Object> getActiveParameterById(int ntestParameterCode, UserInfo objUserInfo)
			throws Exception {
		String sQuery = "";


		sQuery = " select tp.ntestcode,tp.ntestparametercode,tp.nparametertypecode,tp.nunitcode,pt.sparametertypename,pt.sdisplaystatus,"
				+ " u.sunitname,tp.sparametername,tp.sparametersynonym,tp.nroundingdigits,tp.ndeltachecklimitcode,"
				+ " tp.ndeltacheck,tp.ndeltacheckframe,p.jsondata->'speriodname'->>'en-US' as sdeltaunitname,p.nperiodcode as ndeltaunitcode ,"
				+ " tp.ndestinationunitcode,u1.sunitname as sunitname1,tp.noperatorcode,op.soperator,tp.nconversionfactor,tp.nresultaccuracycode,ra.sresultaccuracyname  "
				+ " from testparameter tp,unit u,parametertype pt,period p,unitconversion uc,unit u1,operators op,resultaccuracy ra "
				+ "  where tp.nunitcode = u.nunitcode and tp.nparametertypecode=pt.nparametertypecode "
				+ " and uc.ndestinationunitcode = tp.ndestinationunitcode "
				+ " and tp.nresultaccuracycode=ra.nresultaccuracycode "
				+ " and tp.ndestinationunitcode = u1.nunitcode " + " and p.nperiodcode=tp.ndeltaunitcode and "
				+ " op.noperatorcode=uc.noperatorcode and uc.noperatorcode=tp.noperatorcode" + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and op.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.ntestparametercode = "
				+ ntestParameterCode + ";";
		List<TestParameter> lsttp = jdbcTemplate.query(sQuery, new TestParameter());
		LOGGER.info("Get Method:"+ sQuery);

//		sQuery ="select tpp.spredefinedname, g.sgradename, coalesce(g.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//				+ "	g.jsondata->'sdisplayname'->>'en-US') sdisplaystatus, tpp.ndefaultstatus, g.ngradecode, ntestpredefinedcode, ntestparametercode,tpp.sresultparacomment "
//				+ " from testpredefinedparameter tpp, grade g where g.ngradecode = tpp.ngradecode"
//				+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpp.ntestpredefinedcode > 0 and tpp.nstatus ="
//				+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpp.ntestparametercode ="+ntestParameterCode+";";
		sQuery = "select tpp.spredefinedname, g.sgradename, coalesce(g.jsondata->'sdisplayname'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	g.jsondata->'sdisplayname'->>'en-US') sdisplaystatus, tpp.ndefaultstatus, g.ngradecode, ntestpredefinedcode, ntestparametercode,tpp.spredefinedsynonym "
				+ " from testpredefinedparameter tpp, grade g where g.ngradecode = tpp.ngradecode" + " and g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpp.ntestpredefinedcode > 0 and tpp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpp.ntestparametercode ="
				+ ntestParameterCode + ";";

		List<TestPredefinedParameter> lsttpp = jdbcTemplate.query(sQuery, new TestPredefinedParameter());

		final Map<String, Object> mapList = new HashMap<String, Object>();
		mapList.put("TestParameter", lsttp);
		mapList.put("TestPredefinedParameter", lsttpp);
		final List<TestParameter> lstTestParameter = (List<TestParameter>) mapList.get("TestParameter");
		final List<TestPredefinedParameter> lstTestPredefinedParameter = (List<TestPredefinedParameter>) mapList
				.get("TestPredefinedParameter");
		if (!lstTestParameter.isEmpty()) {
			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("sdisplaystatus");
			mapList.put("TestParameter", commonFunction.getMultilingualMessageList(lstTestParameter, lstcolumns,
					objUserInfo.getSlanguagefilename()));
			mapList.put("TestPredefinedParameter", commonFunction.getMultilingualMessageList(lstTestPredefinedParameter,
					lstcolumns, objUserInfo.getSlanguagefilename()));
			return new ResponseEntity<Object>(mapList, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	
	@Override
	public ResponseEntity<Object> getActivePredefinedParameterById(final int ntestPredefinedCode,
			final UserInfo objUserInfo) throws Exception {
//		final String sQuery = "select tp.ntestpredefinedcode,tp.ngradecode,tp.nstatus,tp.ntestparametercode,tp.spredefinedname,"
//				+ " coalesce(jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//				+ " jsondata->'sdisplayname'->>'en-US')sdisplaystatus , tp.ndefaultstatus,tp.sresultparacomment "
//				+ " from testpredefinedparameter tp,grade g where tp.ngradecode=g.ngradecode"
//				+ " and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and tp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and tp.ntestpredefinedcode="+ntestPredefinedCode;
//		final TestPredefinedParameter objPredefinedParameter = (TestPredefinedParameter) jdbcQueryForObject(sQuery, TestPredefinedParameter.class);
		final String sQuery = "select tp.ntestpredefinedcode,tp.ngradecode,tp.nstatus,tp.ntestparametercode,tp.spredefinedname,"
				+ " coalesce(jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sdisplayname'->>'en-US')sdisplaystatus , tp.ndefaultstatus,case tp.spredefinedsynonym when  'null' then '' else tp.spredefinedsynonym end spredefinedsynonym "
				+ " from testpredefinedparameter tp,grade g where tp.ngradecode=g.ngradecode" + " and g.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.ntestpredefinedcode="
				+ ntestPredefinedCode;
		final TestPredefinedParameter objPredefinedParameter = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(sQuery,
				TestPredefinedParameter.class,jdbcTemplate); 
		if (objPredefinedParameter != null) {
			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("sdisplaystatus");
			final List<TestPredefinedParameter> lstCodedResult = (List<TestPredefinedParameter>) commonFunction
					.getMultilingualMessageList(Arrays.asList(objPredefinedParameter), lstcolumns,
							objUserInfo.getSlanguagefilename());
			ObjectMapper objMapper = new ObjectMapper();
			return new ResponseEntity<Object>(
					objMapper.convertValue(lstCodedResult.get(0), TestPredefinedParameter.class), HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	
	@Override
	public ResponseEntity<Object> createTestPredefinedParameter(
			final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		final String squery = "select * from testparameter where ntestparametercode = "
				+ objTestPredefinedParameter.getNtestparametercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(squery, TestParameter.class,jdbcTemplate); 
		if (objTestParameter != null) {
			final String strQuery = "select ntestpredefinedcode from testpredefinedparameter where spredefinedname=N'"
					+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname()) + "' and ntestparametercode = "
					+ objTestPredefinedParameter.getNtestparametercode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<TestPredefinedParameter> lstpre = (List<TestPredefinedParameter>) jdbcTemplate.query(strQuery,
					new TestPredefinedParameter());
			if (lstpre.isEmpty()) {
				final String strQuery1 = "select ntestpredefinedcode from testpredefinedparameter where ntestparametercode = "
						+ objTestPredefinedParameter.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ "  and spredefinedname is not null";
				lstpre = (List<TestPredefinedParameter>) jdbcTemplate.query(strQuery1,
						new TestPredefinedParameter());
				// if(lstpre.isEmpty()) {
				// objTestPredefinedParameter.setNdefaultstatus((short)
				// Enumeration.TransactionStatus.YES.gettransactionstatus());
				// } else {
				objTestPredefinedParameter
						.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
				// }

				String seqtpredefinedquery = "select nsequenceno from SeqNoTestManagement where stablename='testpredefinedparameter'";
				int nseqpredefinedcount = jdbcTemplate.queryForObject(seqtpredefinedquery, Integer.class);
				nseqpredefinedcount++;

//				String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,ndefaultstatus,nstatus)"
//											+" values ("+nseqpredefinedcount+","+objTestPredefinedParameter.getNtestparametercode()+","+objTestPredefinedParameter.getNgradecode()+",N'"+ReplaceQuote(objTestPredefinedParameter.getSpredefinedname())+"',"
//											+" "+objTestPredefinedParameter.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
				// Commented by sonia on 03-10-2022
//				String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,sresultparacomment,dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,ndefaultstatus,nstatus)"
//						+" values ("+nseqpredefinedcount+","+objTestPredefinedParameter.getNtestparametercode()+","+objTestPredefinedParameter.getNgradecode()+",N'"+ReplaceQuote(objTestPredefinedParameter.getSpredefinedname())+"',N'"+ReplaceQuote(objTestPredefinedParameter.getSresultparacomment())+"','"+getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNtimezonecode()+","+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//						+", "+objTestPredefinedParameter.getNdefaultstatus()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
//				getJdbcTemplate().execute(insertpredefinedquery);

				// Added by sonia on 03-10-2022
//				String insertpredefinedquery =" Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,sresultparacomment,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
//						+" values ("+nseqpredefinedcount+","+objTestPredefinedParameter.getNtestparametercode()+","+objTestPredefinedParameter.getNgradecode()+",N'"+ReplaceQuote(objTestPredefinedParameter.getSpredefinedname())+"',N'"+ReplaceQuote(objTestPredefinedParameter.getSresultparacomment())+"','"+getCurrentDateTime(objUserInfo)+"',"
//						+" "+objTestPredefinedParameter.getNdefaultstatus()+","+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
				String insertpredefinedquery = " Insert into testpredefinedparameter(ntestpredefinedcode,ntestparametercode,ngradecode,spredefinedname,spredefinedsynonym,dmodifieddate,ndefaultstatus,nsitecode,nstatus)"
						+ " values (" + nseqpredefinedcount + "," + objTestPredefinedParameter.getNtestparametercode()
						+ "," + objTestPredefinedParameter.getNgradecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedsynonym()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + " " + objTestPredefinedParameter.getNdefaultstatus()
						+ "," + objUserInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				
				//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
				if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final String query="select * from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
			   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredefinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
			   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
			   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
			   		final TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(query,TestGroupTestParameter.class,jdbcTemplate); 
					
			   		final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename in ('testgrouptestpredefparameter')";
			   		int ntestgrouptestpredefcode =  jdbcTemplate.queryForObject(sQuery,Integer.class);
			   		ntestgrouptestpredefcode++;
			   		insertpredefinedquery=insertpredefinedquery+"update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestpredefcode+" where stablename ='testgrouptestpredefparameter';";

			   		insertpredefinedquery=insertpredefinedquery+ "insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
					+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode) values (" + ntestgrouptestpredefcode+ "," + objTestGroupTestParameter.getNtestgrouptestparametercode()+","
					+ objTestPredefinedParameter.getNgradecode() + "," + "N'"
					+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname()) + "',N'"+stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedsynonym()) + "',"
					+ objTestPredefinedParameter.getNdefaultstatus() + ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+");";
				}

				jdbcTemplate.execute(insertpredefinedquery);

				String updatepredefinedquery = "update SeqNoTestManagement set nsequenceno =" + nseqpredefinedcount
						+ " where stablename ='testpredefinedparameter'";
				jdbcTemplate.execute(updatepredefinedquery);

				final List<Object> lstObject = new ArrayList<>();
				lstObject.add(objTestPredefinedParameter);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, Arrays.asList("IDS_ADDPREDEFINEDPARAMETER"), objUserInfo);
				return new ResponseEntity<Object>(
						testMasterCommonFunction.getTestPredefinedParameter(objTestPredefinedParameter.getNtestparametercode(), objUserInfo),
						HttpStatus.OK);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
			
	}
	
	@Override
	public ResponseEntity<Object> updateTestPredefinedParameter(
			final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
		final String squery = "select * from testparameter where ntestparametercode = "
				+ objTestPredefinedParameter.getNtestparametercode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(squery, TestParameter.class,jdbcTemplate); 
		if (objTestParameter != null) {
			String selectQuery = "select * from testpredefinedparameter" + " where ntestpredefinedcode = "
					+ objTestPredefinedParameter.getNtestpredefinedcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final TestPredefinedParameter objpredef = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(selectQuery,
					TestPredefinedParameter.class,jdbcTemplate); 
			if (objpredef != null) {
				String strQuery = "select ntestpredefinedcode from testpredefinedparameter"
						+ " where spredefinedname=N'" + stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname())
						+ "'" + " and ntestparametercode = " + objTestPredefinedParameter.getNtestparametercode()
						+ " and ntestpredefinedcode<> " + objTestPredefinedParameter.getNtestpredefinedcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestPredefinedParameter objTestPredefined = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(strQuery,
						TestPredefinedParameter.class,jdbcTemplate); 
				if (objTestPredefined == null) {
					// Commented by sonia on 03-10-2022
//					final String updatequery = "update testpredefinedparameter set spredefinedname=N'"+ReplaceQuote(objTestPredefinedParameter.getSpredefinedname())+"',"
//							+ "ngradecode = "+objTestPredefinedParameter.getNgradecode()
//							+ ", dmodifieddate ='" + getCurrentDateTime(objUserInfo)
//		    				+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//		    				+",noffsetdmodifieddate= "+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//							+"  where ntestpredefinedcode="+objTestPredefinedParameter.getNtestpredefinedcode()+";";

					// Added by sonia on 03-10-2022
//					final String updatequery = "update testpredefinedparameter set spredefinedname=N'"+ReplaceQuote(objTestPredefinedParameter.getSpredefinedname())+"',"
//											 + "ngradecode = "+objTestPredefinedParameter.getNgradecode()
//											 + ",sresultparacomment=N'"+ReplaceQuote(objTestPredefinedParameter.getSresultparacomment())+"', dmodifieddate ='" + getCurrentDateTime(objUserInfo)+"' "
//											 + "  where ntestpredefinedcode="+objTestPredefinedParameter.getNtestpredefinedcode()+";";
					 String updatequery = "update testpredefinedparameter set spredefinedname=N'"
							+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname()) + "'," + "ngradecode = "
							+ objTestPredefinedParameter.getNgradecode() + ",spredefinedsynonym=N'"
							+ stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedsynonym()) + "', dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + "  where ntestpredefinedcode="
							+ objTestPredefinedParameter.getNtestpredefinedcode() + ";";
					//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						 String query="select * from testgrouptestpredefparameter where  ntestgrouptestparametercode=(select ttp.ntestgrouptestparametercode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredefinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and spredefinedname=N'"+objpredef.getSpredefinedname()+"'";
					   		final TestGroupTestPredefinedParameter objTestGroupTestPredefinedParameter=(TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(query,TestGroupTestPredefinedParameter.class,jdbcTemplate); 
							
					   		updatequery=updatequery+"update testgrouptestpredefparameter set ngradecode = "+objTestPredefinedParameter.getNgradecode()+""
							+ ", spredefinedname = N'" + stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedname()) + "'"
							+ ", spredefinedsynonym = N'" + stringUtilityFunction.replaceQuote(objTestPredefinedParameter.getSpredefinedsynonym()) + "'"							
							+ ", dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestgrouptestpredefcode = "
							+ objTestGroupTestPredefinedParameter.getNtestgrouptestpredefcode() + ";";
					}

					jdbcTemplate.execute(updatequery);
					final List<Object> lstObject = new ArrayList<>();
					final List<Object> lstOldObject = new ArrayList<>();
					lstObject.add(objTestPredefinedParameter);
					lstOldObject.add(objpredef);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 2, lstOldObject, Arrays.asList("IDS_EDITPREDEFINEDPARAMETER"),
							objUserInfo);
					return new ResponseEntity<Object>(
							testMasterCommonFunction.getTestPredefinedParameter(objTestPredefinedParameter.getNtestparametercode(), objUserInfo),
							HttpStatus.OK);
				} else {
					// Conflict = 409 - Duplicate entry
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				// Conflict = 417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
		@Override
		public ResponseEntity<Object> deleteTestPredefinedParameter(
				final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
			final String squery = "select * from testparameter where ntestparametercode = "
					+ objTestPredefinedParameter.getNtestparametercode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(squery, TestParameter.class,jdbcTemplate); 
			if (objTestParameter != null) {
				final String selectQuery = "select ntestpredefinedcode, ndefaultstatus from testpredefinedparameter where ntestpredefinedcode = "
						+ objTestPredefinedParameter.getNtestpredefinedcode() + " and  nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestPredefinedParameter objpredef = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(selectQuery,
						TestPredefinedParameter.class,jdbcTemplate); 
				if (objpredef == null) {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (objpredef.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						// Conflict = 409 - Duplicate entry
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					} else {
						final String strPredefined = "select * from testpredefinedparameter where ntestpredefinedcode ="
								+ objTestPredefinedParameter.getNtestpredefinedcode();
						final TestPredefinedParameter objTestPredefinedparameter = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(strPredefined, TestPredefinedParameter.class,jdbcTemplate); 
						// Commented by sonia on 03-10-2022
//						final String updatequery = "update testpredefinedparameter set nstatus= "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
//						+ ", dmodifieddate ='" + getCurrentDateTime(objUserInfo)
//	    				+"',ntzmodifieddate= "+objUserInfo.getNtimezonecode()
//	    				+",noffsetdmodifieddate= "+getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
//						+"  where ntestpredefinedcode = "+objTestPredefinedParameter.getNtestpredefinedcode()+";";

						// Added by sonia on 03-10-2022
						 String updatequery = "update testpredefinedparameter set nstatus= "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + "where ntestpredefinedcode = "
								+ objTestPredefinedParameter.getNtestpredefinedcode() + ";";
					//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.	
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							 String query="select * from testgrouptestpredefparameter where  ntestgrouptestparametercode=(select ttp.ntestgrouptestparametercode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredefinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and spredefinedname=N'"+objTestPredefinedParameter.getSpredefinedname()+"'";
					   		final TestGroupTestPredefinedParameter objTestGroupTestPredefinedParameter=(TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(query,TestGroupTestPredefinedParameter.class,jdbcTemplate); 
							
					   		 updatequery = updatequery+"update testgrouptestpredefparameter set nstatus= "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + "where ntestgrouptestpredefcode = "
									+ objTestGroupTestPredefinedParameter.getNtestgrouptestpredefcode()+"";
						}

					    jdbcTemplate.execute(updatequery);
						final List<Object> lstObject = new ArrayList<>();
						lstObject.add(objTestPredefinedparameter);
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, Arrays.asList("IDS_DELETEPREDEFINEDPARAMETER"),
								objUserInfo);
						return new ResponseEntity<Object>(
								testMasterCommonFunction.getTestPredefinedParameter(objTestPredefinedParameter.getNtestparametercode(), objUserInfo),
								HttpStatus.OK);
					}
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		
		
		@Override
		public ResponseEntity<Object> getParameterSpecificationByCount(int ntestParameterCode, UserInfo objUserInfo)
				throws Exception {
			List<TestParameterNumeric> lstTestParameterNumeric = testMasterCommonFunction.getTestParameterNumeric(ntestParameterCode, objUserInfo);
			if (!lstTestParameterNumeric.isEmpty()) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ONLYONESPECLIMITCANADD",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<Object>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), HttpStatus.OK);
			}
		}
		
		
			@Override
			public ResponseEntity<Object> createTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				final String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParameter != null) {

					String seqnumericquery = "select nsequenceno from SeqNoTestManagement where stablename='testparameternumeric'";
					int nseqnumericcount = jdbcTemplate.queryForObject(seqnumericquery, Integer.class);
					nseqnumericcount++;

					String insertnumericquery = " Insert into testparameternumeric(ntestparamnumericcode,ntestparametercode,smina,sminb,smaxb,smaxa,sminlod,smaxlod,sminloq,smaxloq,sdisregard,sresultvalue,dmodifieddate,nsitecode,nstatus,ngradecode)" 
							+" values ("+nseqnumericcount+","+objTestParameterNumeric.getNtestparametercode()+","
							+ " case when N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina())+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina())+"' end,"
							+ " case when N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb())+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb())+"' end,"
							+ " case when N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa())+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard())+"' end,"
							+ " case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()))+"'='null' then NULL else N'"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue())+"' end,"
							+ " '"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"+objUserInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+","+objTestParameterNumeric.getNgradecode()+")";
					jdbcTemplate.execute(insertnumericquery);

					//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						//ALPD-5001--Added by vignesh(04-03-2025)--Test group screen -> System allow to save the spec without having the test in Multi-tab.
						
							final String sparamterQuery="select ntestgrouptestnumericcode from testgrouptestnumericparameter where  ntestgrouptestparametercode = "
								+ " (select ntestgrouptestparametercode from testgrouptestparameter  tgtp,testgrouptest tgt "
								+ "  where ntestparametercode="+objTestParameterNumeric.getNtestparametercode()+" "
								+ "  and tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
								+ " and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+""
								+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ")"
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						
						Integer ntestgrouptestnumericcode = (Integer) jdbcTemplate.queryForObject(sparamterQuery,Integer.class);

						if(ntestgrouptestnumericcode!=null) {
							
						final String updateQuery="update testgrouptestnumericparameter "
										+ " set sminlod=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod())+"'='null' then  NULL else '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod())+"' end," 
								        + " smaxlod=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod()) + "'='null' then  NULL else '"+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod()) + "' end ,"
										+ " sminb=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "'='null' then NULL else '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "' end ," 
								        + " smina=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "'='null' then  NULL else '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "' end ," 
									    + " smaxa=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "' end ," 
								        + " smaxb= case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb()) + "' end ,"
									    + " sminloq=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "' end ,"
								        + " smaxloq=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())+ "' end ," 
									    + " sdisregard=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "' end ," 
								        + " sresultvalue=case when '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "'='null' then NULL else  '"+stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "' end ,"
										+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",nsitecode="+objUserInfo.getNmastersitecode()+","
										+ " ngradecode="+objTestParameterNumeric.getNgradecode()+" where ntestgrouptestnumericcode="+ntestgrouptestnumericcode+"";
						
						jdbcTemplate.execute(updateQuery);
						
						}

					}
					

					String updatenumericquery = "update SeqNoTestManagement set nsequenceno =" + nseqnumericcount
							+ " where stablename ='testparameternumeric'";
					jdbcTemplate.execute(updatenumericquery);

					final List<Object> lstnewobject = new ArrayList<>();
					final List<String> lstactionType = new ArrayList<>();
					lstnewobject.add(objTestParameterNumeric);
					lstactionType.add("IDS_ADDSPECLIMIT");
					auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
					final Map<String, Object> outMap = new HashMap<String, Object>();
					outMap.put("TestParameterNumeric",
							testMasterCommonFunction.getTestParameterNumeric(objTestParameterNumeric.getNtestparametercode(), objUserInfo));
					return new ResponseEntity<Object>(outMap, HttpStatus.OK);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
	
			
		

			@Override
			public ResponseEntity<Object> updateTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParameter != null) {
					final String strQuery = "select * from testparameternumeric  where ntestparamnumericcode = "
							+ objTestParameterNumeric.getNtestparamnumericcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<TestParameterNumeric> lst1 = (List<TestParameterNumeric>) jdbcTemplate.query(strQuery,
							new TestParameterNumeric());
					if (lst1.isEmpty()) {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else {
						sQuery = "update testparameternumeric set " + "sminlod = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "' end," + "smaxlod = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod()) + "' end," + "sminb = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "' end," + "smina = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "' end," + "smaxa = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "' end," + "smaxb = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb()) + "' end," + "sminloq = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "' end," + "smaxloq = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq()) + "' end," + "sdisregard = case when N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "'='null' then NULL else N'"
								+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "' end,"
								+ "sresultvalue = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue())
								+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue())
								+ "' end," + "dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ,ngradecode="
								+ objTestParameterNumeric.getNgradecode() + " where ntestparamnumericcode = "
								+ objTestParameterNumeric.getNtestparamnumericcode();
						jdbcTemplate.execute(sQuery);

						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String sparamterQuery="select ntestgrouptestnumericcode from testgrouptestnumericparameter where  ntestgrouptestparametercode = "
									+ " (select ntestgrouptestparametercode from testgrouptestparameter  tgtp,testgrouptest tgt "
									+ "  where ntestparametercode="+objTestParameterNumeric.getNtestparametercode()+" "
									+ "  and tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
									+ " and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+""
									+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
									+ " and tgtp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ")"
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							
							Integer ntestgrouptestnumericcode = (Integer) jdbcTemplate.queryForObject(sparamterQuery,Integer.class);

							if(ntestgrouptestnumericcode!=null) {
								
							final String updateQuery=
									"update testgrouptestnumericparameter   "
											+ " set sminlod=case when N'"+(stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()))+"'='null' then  NULL else N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod())) + "' end," + " smaxlod=case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())) + "'='null' then  NULL else N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())) + "' end ," + " sminb=case when N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "'='null' then NULL else N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminb()) + "' end ," + " smina=case when N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "'='null' then  NULL else N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmina()) + "' end ," + " smaxa=case when N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "'='null' then NULL else  N'"
											+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxa()) + "' end ," + " smaxb=case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb())) + "'='null' then NULL else  N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxb())) + "' end ," + " sminloq=case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq())) + "'='null' then NULL else  N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq())) + "' end ," + " smaxloq=case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())) + "'='null' then NULL else  N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq()))+ "' end ," + "sdisregard= case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard())) + "'='null' then NULL else  N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard())) + "' end ," + " sresultvalue=case when N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue())) + "'='null' then NULL else  N'"
											+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue())) + "' end ," + " "
											+ " dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',nstatus= " 
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",nsitecode="+objUserInfo.getNmastersitecode()+",ngradecode="+objTestParameterNumeric.getNgradecode()+" where ntestgrouptestnumericcode="+ntestgrouptestnumericcode+"";
							
							jdbcTemplate.execute(updateQuery);
							}
						}
						final List<Object> lstnewobject = new ArrayList<>();
						final List<String> lstactionType = new ArrayList<>();
						lstnewobject.add(objTestParameterNumeric);
						lstactionType.add("IDS_EDITSPECLIMIT");
						auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lst1, lstactionType, objUserInfo);
						final Map<String, Object> outMap = new HashMap<String, Object>();
						outMap.put("TestParameterNumeric",
								testMasterCommonFunction.getTestParameterNumeric(objTestParameterNumeric.getNtestparametercode(), objUserInfo));
						return new ResponseEntity<Object>(outMap, HttpStatus.OK);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
	
			@Override
			public ResponseEntity<Object> deleteTestParameterNumeric(final TestParameterNumeric objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParameter != null) {
					final String strQuery = "select * from testparameternumeric  where ntestparamnumericcode = "
							+ objTestParameterNumeric.getNtestparamnumericcode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestParameterNumeric objParameterNumeric = (TestParameterNumeric) jdbcUtilityFunction.queryForObject(strQuery,
							TestParameterNumeric.class,jdbcTemplate); 
					if (objParameterNumeric == null) {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else {
						sQuery = "update testparameternumeric set   nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestparamnumericcode = "
								+ objTestParameterNumeric.getNtestparamnumericcode()+";";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus() ) {
							sQuery = sQuery + "update testgrouptestnumericparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
									+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
									+ " where   ntestgrouptestnumericcode=( select ntestgrouptestnumericcode from   testgrouptestnumericparameter where ntestgrouptestparametercode = (select tgtp.ntestgrouptestparametercode from testgrouptest tgt, testgrouptestparameter tgtp "
									+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and tgtp.ntestparametercode =  "
									+ objTestParameterNumeric.getNtestparametercode() + " ) and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
						}
						jdbcTemplate.execute(sQuery);
						final List<Object> lstnewobject = new ArrayList<>();
						final List<String> lstactionType = new ArrayList<>();
						lstactionType.add("IDS_DELETESPECLIMIT");
						lstnewobject.add(objParameterNumeric);
						auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
						final Map<String, Object> outMap = new HashMap<String, Object>();
						outMap.put("TestParameterNumeric",
								testMasterCommonFunction.getTestParameterNumeric(objTestParameterNumeric.getNtestparametercode(), objUserInfo));
						return new ResponseEntity<Object>(outMap, HttpStatus.OK);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> getParameterSpecificationById(int ntestParamNumericCode, UserInfo objUserInfo)
					throws Exception {
				final String sQuery = "select tp.*, coalesce(gg.jsondata->'sdisplayname'->>'"
						+ objUserInfo.getSlanguagetypecode()
						+ "',gg.jsondata->'sdisplayname'->>'en-US') as sgradename from testparameternumeric tp,grade gg where gg.ngradecode=tp.ngradecode and  ntestparamnumericcode = "
						+ ntestParamNumericCode + " and tp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and gg.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameterNumeric objTestParameterNumeric = (TestParameterNumeric) jdbcUtilityFunction.queryForObject(sQuery,
						TestParameterNumeric.class,jdbcTemplate); 
				if (objTestParameterNumeric != null) {
					return new ResponseEntity<Object>(objTestParameterNumeric, HttpStatus.OK);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
		
			@Override
			public ResponseEntity<Object> addTestFormula(final UserInfo objUserInfo) throws Exception {
				Map<String, List<?>> outMapList = new HashMap<String, List<?>>();
				String sQuery = "";

				sQuery = "select noperatorcode,soperator,soperatordesc from operators" + " where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				List<Operators> lstOperators = jdbcTemplate.query(sQuery, new Operators());
				sQuery = "select nfunctioncode,coalesce(jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
						+ "'," + " jsondata->'sdisplayname'->>'en-US') as sfunctionname ,sfunctionsyntax from functions"
						+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				List<Functions> lstFunctions = jdbcTemplate.query(sQuery, new Functions());

				outMapList.put("Operators", lstOperators);
				outMapList.put("Functions", lstFunctions);

				List<TestCategory> lstTestCategory = (List<TestCategory>) objTestCategoryDAO
						.getTestCategoryForTestMaster(objUserInfo).getBody();
				lstTestCategory = lstTestCategory.stream().filter(x -> x.getNtestcategorycode() != -2)
						.collect(Collectors.toList());
				outMapList.put("TestCategory", lstTestCategory);
				List<TestMaster> lstTest = new ArrayList<TestMaster>();
				List<TestParameter> ldst = new ArrayList<TestParameter>();
				if (!lstTestCategory.isEmpty()) {
					lstTest = testMasterCommonFunction.getTestForFormula(lstTestCategory.get(0).getNtestcategorycode());
					if (!lstTest.isEmpty()) {
						ldst = testMasterCommonFunction.getTestParameterForFormula(lstTest.get(0).getNtestcode(), objUserInfo);
					}
				}
				outMapList.put("TestMaster", lstTest);
				outMapList.put("DynamicFormulaFields", ldst);
				return new ResponseEntity<Object>(outMapList, HttpStatus.OK);
			}
			
			
			@Override
			public ResponseEntity<Object> changeTestCatgoryInFormula(final int ntestCategoryCode, final UserInfo objUserInfo)
					throws Exception {
				Map<String, List<?>> outMapList = new HashMap<String, List<?>>();
				List<TestMaster> lstTest = testMasterCommonFunction.getTestForFormula(ntestCategoryCode);
				outMapList.put("TestMaster", lstTest);
				if (!lstTest.isEmpty()) {
					List<TestParameter> ldst = testMasterCommonFunction.getTestParameterForFormula(lstTest.get(0).getNtestcode(), objUserInfo);
					outMapList.put("DynamicFormulaFields", ldst);
				}
				return new ResponseEntity<Object>(outMapList, HttpStatus.OK);
			}

			/**
			 * This method definition is used to fetch DynamicFormulaFields
			 * 
			 * @param ntestCode [int] primarykey of test object
			 * @return response entity holds list of DynamicFormulaFields
			 */
			@Override
			public ResponseEntity<Object> changeTestInFormula(final int ntestCode, final UserInfo objUserInfo)
					throws Exception {
				Map<String, List<?>> outMapList = new HashMap<String, List<?>>();
				outMapList.put("DynamicFormulaFields", testMasterCommonFunction.getTestParameterForFormula(ntestCode, objUserInfo));
				return new ResponseEntity<Object>(outMapList, HttpStatus.OK);
			}

			/**
			 * This method definition is used to create a new test formula
			 * 
			 * @param objTestFormula [TestFormula] holds the object of test formula details
			 * @param objUserInfo    [UserInfo] holds the object of loggedin userinfo
			 * @return response entity holds the list of test formula
			 */
			
			@Override
			public ResponseEntity<Object> createTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite)
					throws Exception {
				final String strquery1 = "select ntestformulacode from testformula where sformulaname=N'"
						+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulaname()) + "'" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode="
						+ objTestFormula.getNtestcode() + "";
				final List<TestFormula> lst1 = (List<TestFormula>) jdbcTemplate.query(strquery1, new TestFormula());

				if (lst1.isEmpty()) {

					final String sQuery = "select npredefinedformulacode from testformula where ntestcode="
							+ objTestFormula.getNtestcode() + " " + " and npredefinedformulacode="
							+ objTestFormula.getNpredefinedformulacode() + " and ntestparametercode="
							+ objTestFormula.getNtestparametercode() + "" + " and nispredefinedformula="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					final List<TestFormula> lstPredefined = (List<TestFormula>) jdbcTemplate.query(sQuery,
							new TestFormula());

					if (lstPredefined.isEmpty()) {

						final String strquery = "select ntestformulacode from testformula where ntestparametercode="
								+ objTestFormula.getNtestparametercode() + " and ndefaultstatus="
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						final List<TestFormula> lst = (List<TestFormula>) jdbcTemplate.query(strquery, new TestFormula());
						if (lst != null && lst.size() == 0) {
							objTestFormula.setNdefaultstatus((short) 3);
						}
						String seqtestforquery = "select nsequenceno from SeqNoTestManagement where stablename='testformula'";
						int nseqtestforcount = jdbcTemplate.queryForObject(seqtestforquery, Integer.class);
						nseqtestforcount++;

						String inserttestforquery = " Insert into testformula(ntestformulacode,ntestcode,ntestparametercode,sformulaname,sformulacalculationcode,sformulacalculationdetail,npredefinedformulacode,nispredefinedformula,ndefaultstatus,dmodifieddate, nsitecode, nstatus)"
								+ " values (" + nseqtestforcount + "," + objTestFormula.getNtestcode() + ","
								+ objTestFormula.getNtestparametercode() + ",N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulaname()) + "',N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationcode()) + "',N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationdetail()) + "'," + " "
								+ objTestFormula.getNpredefinedformulacode() + "," + objTestFormula.getNispredefinedformula()
								+ "," + objTestFormula.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
								+ objUserInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							final String query="select * from testgrouptestparameter ttp,testgrouptest tgt where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and ntestparametercode="+objTestFormula.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and tgt.ntestcode="+objTestFormula.getNtestcode()+" "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					   		TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(query,TestGroupTestParameter.class,jdbcTemplate); 
					   		
				   			
					   		String query1="select * from testgrouptestformula where ntestgrouptestcode="+objTestGroupTestParameter.getNtestgrouptestcode()+" and ntestgrouptestparametercode="+objTestGroupTestParameter.getNtestgrouptestparametercode()+" "
					   				+ " and ntransactionstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nstatus=1";
					   		TestGroupTestFormula objTestGroupTestFormula=(TestGroupTestFormula) jdbcUtilityFunction.queryForObject(query1,TestGroupTestFormula.class,jdbcTemplate); 
					   		
					   		if(objTestGroupTestFormula==null) {
					   			
					   			final String sequencequery="select nsequenceno from seqnotestgroupmanagement where stablename in (N'testgrouptestformula')";
						   		int ntestgrouptestformulacode = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						   		ntestgrouptestformulacode++;
						   		
						   		final String updateFormulaseq = "update seqnotestgroupmanagement set nsequenceno =" + ntestgrouptestformulacode
										+ " where stablename ='testgrouptestformula'";
						   		jdbcTemplate.execute(updateFormulaseq);
								
						   	
					   		inserttestforquery = inserttestforquery+"insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode,"
								+ " sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, dmodifieddate,nstatus,nsitecode)"
								+ " values (" + ntestgrouptestformulacode + "," + objTestGroupTestParameter.getNtestgrouptestcode()+"," + objTestGroupTestParameter.getNtestgrouptestparametercode()+","
								+ nseqtestforcount + "," + "N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationcode()) + "'" + ",N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationdetail()) + "',"
								+ objTestFormula.getNdefaultstatus() + ","
								+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","+objUserInfo.getNmastersitecode()+ ");";
					   		}
						}
//						sb.append(sinsQuery);
						
						jdbcTemplate.execute(inserttestforquery);
						
						

						String updatetestforquery = "update SeqNoTestManagement set nsequenceno =" + nseqtestforcount
								+ " where stablename ='testformula'";
						jdbcTemplate.execute(updatetestforquery);

						objTestFormula.setNtestformulacode(nseqtestforcount);

						final List<Object> lstObject = new ArrayList<>();
						final List<String> lstActiontype = new ArrayList<>();
						lstObject.add(objTestFormula);
						lstActiontype.add("IDS_ADDTESTFORMULA");
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, lstActiontype, objUserInfo);
						return new ResponseEntity<Object>(
								testMasterCommonFunction.getTestParameterFormula(objTestFormula.getNtestparametercode(), objUserInfo), HttpStatus.OK);
					}

					else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREDEFINEDFORMULAISALREADYEXIST",
								objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

					}
				} else {
					// Conflict = 409 - Duplicate entry
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									objUserInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> deleteTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite)
					throws Exception {
					// janakumar ALPD-ALPD-4185   Reason -> Test master -> Formula used in test group can't allow to delete .
				//janakumar ALPD-4568 Test master->500 error when delete the formula.
				final String validateTestGroup = "select tf.ntestformulacode from testgrouptest tg,testgrouptestformula tf,testgrouptestparameter tp, testformula tfo   "
						+ "where tg.ntestgrouptestcode=tf.ntestgrouptestcode and tfo.ntestformulacode=tf.ntestformulacode "
						+ "and tf.ntestgrouptestparametercode=tp.ntestgrouptestparametercode  " + "and tg.ntestcode="
						+ objTestFormula.getNtestcode() + " and tp.ntestparametercode="
						+ objTestFormula.getNtestparametercode() + "  "
						+ " and tfo.ntestformulacode="+objTestFormula.getNtestformulacode()+"  "
						+ "and tg.nsitecode=tf.nsitecode and tf.nsitecode=tp.nsitecode   " + "and tp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tg.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						//ALPD-5543--Added by Vignesh R(08-04-2025)-->can't deleteing the formula
						+ "and tf.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tg.nspecsampletypecode>0;";

//				final TestFormula objFormulaMappedInTestGroup = (TestFormula) jdbcQueryForObject(validateTestGroup,
//				TestFormula.class);		

					final List<TestFormula> objFormulaMappedInTestGroup = jdbcTemplate.query(validateTestGroup, new TestFormula());

					if (objFormulaMappedInTestGroup.isEmpty()){

					final String strQuery = "select ntestformulacode from testformula where ntestformulacode = "
							+ objTestFormula.getNtestformulacode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestFormula objFormula = (TestFormula) jdbcUtilityFunction.queryForObject(strQuery, TestFormula.class,jdbcTemplate); 
					if (objFormula != null) {
						//ALPD-4565 test master>Allow to delete default formula ,another formula was not changed to default
						 String sdefaultquery = "select ntestformulacode from testformula where ntestparametercode="
								+ objTestFormula.getNtestparametercode() + " and ntestformulacode = "+objTestFormula.getNtestformulacode()+" and ndefaultstatus="
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						 List<TestFormula> lstDefault = (List<TestFormula>) jdbcTemplate.query(sdefaultquery, new TestFormula());
						if (lstDefault != null && lstDefault.size() > 0) {
							 sdefaultquery = "select ntestformulacode from testformula where ntestparametercode="
									+ objTestFormula.getNtestparametercode() + " and ntestformulacode != "+objTestFormula.getNtestformulacode()+" and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntestformulacode desc";
							 lstDefault = (List<TestFormula>) jdbcTemplate.query(sdefaultquery, new TestFormula());
						}
						
						
						 String updatequery = "update testformula set nstatus="
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'  where ntestformulacode="
								+ objTestFormula.getNtestformulacode() + ";";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
					if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							
							final String sformulacodeQuery="select * from testgrouptestformula where ntestgrouptestcode in(select ttp.ntestgrouptestcode from testgrouptestparameter ttp,testgrouptest tgt where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and ntestparametercode="+objTestFormula.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and tgt.ntestcode="+objTestFormula.getNtestcode()+" "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and ntestformulacode="+objTestFormula.getNtestformulacode()+" "
					   				+ "and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							TestGroupTestFormula objTestGroupTestFormula = (TestGroupTestFormula) jdbcUtilityFunction.queryForObject(sformulacodeQuery, TestGroupTestFormula.class,jdbcTemplate); 
							
							if(objTestGroupTestFormula!=null) {
							updatequery =updatequery+ "update testgrouptestformula set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+","
									+ "dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"'"
									+ " where ntestgrouptestformulacode = "
									+ objTestGroupTestFormula.getNtestgrouptestformulacode();
							}
						}
						
						
					jdbcTemplate.execute(updatequery);
						//ALPD-4565 test master>Allow to delete default formula ,another formula was not changed to default
						if (lstDefault != null && lstDefault.size() > 0) {
							int ntestformulacode=lstDefault.get(0).getNtestformulacode();
							 sdefaultquery = "update testformula set ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'  where ntestformulacode="
									+ ntestformulacode + ";";
							 jdbcTemplate.execute(sdefaultquery);
						}
						
						final List<Object> lstObject = new ArrayList<>();
						final List<String> lstActiontype = new ArrayList<>();
						lstObject.add(objTestFormula);
						lstActiontype.add("IDS_DELETETESTFORMULA");
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, lstActiontype, objUserInfo);
						
					} else {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}

				} else {
					
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_MAPPEDINTESTGROUP",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
					
				}
				return new ResponseEntity<Object>(
						testMasterCommonFunction.getTestParameterFormula(objTestFormula.getNtestparametercode(), objUserInfo), HttpStatus.OK);
			}
			
			/**
			 * This method definition is used to validate the entered test formula
			 * 
			 * @param sformula  [String] holds the formulacalculationcode based on entered
			 *                  formula
			 * @param ntestcode [int] holds the primarykey of testmaster object
			 * @return response entity holds the list of DynamicFormulaFields
			 */
			@Override
			public ResponseEntity<Object> validateTestFormula(String sformula, final int ntestcode) throws Exception {
				final List<DynamicFormulaFields> lst = new ArrayList<>();
				while (sformula.contains("$P")) {
					int index = sformula.indexOf("$P");
					int LastIndex = sformula.indexOf("P$");
					String actualText = sformula.substring(index + 2, LastIndex);
					int ntestparametercode = Integer.parseInt(actualText);
					if (ntestparametercode > 0 && ntestcode > 0) {
						final String strQuery = "select * from testparameter where ntestparametercode=" + ntestparametercode
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						final TestParameter objparameter = (TestParameter) jdbcUtilityFunction.queryForObject(strQuery, TestParameter.class,jdbcTemplate);
						if (objparameter != null) {
							DynamicFormulaFields objform = new DynamicFormulaFields();
							objform.setNdynamicformulafieldcode(ntestparametercode);
							String str = "$P" + ntestparametercode + "P$";
							objform.setSdescription(str);
							objform.setSdynamicfieldname(objparameter.getSparametername());
							sformula = stringUtilityFunction.replaceofString(sformula, str, "");
							lst.add(objform);
						}
					}
					System.err.println(sformula);
				}
				String s = sformula;
				while (s.contains("$D")) {
					int index = s.indexOf("$D");
					int LastIndex = s.indexOf("D$");
					String actualText = s.substring(index + 2, LastIndex);
					int parametercode = Integer.parseInt(actualText);
					if (actualText != null) {
						String strQuery = "select * from dynamicformulafields where ndynamicformulafieldcode=" + parametercode
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
						DynamicFormulaFields objdynamic = (DynamicFormulaFields) jdbcUtilityFunction.queryForObject(strQuery,
								DynamicFormulaFields.class,jdbcTemplate); 
						if (objdynamic != null) {
							DynamicFormulaFields objform = new DynamicFormulaFields();
							objform.setNdynamicformulafieldcode(parametercode);
							String str = "$D" + parametercode + "D$";
							objform.setSdescription(str);
							objform.setSdynamicfieldname(objdynamic.getSdatatype());
							StringBuffer sb = new StringBuffer(s);
							sb.replace(index, LastIndex + 2, "");
							s = sb.toString();
							lst.add(objform);
						} else {
							break;
						}
					}

				}
				return new ResponseEntity<Object>(lst, HttpStatus.OK);
			}
			
			
			
			@Override
			public ResponseEntity<Object> calculateFormula(String sformulacalculationcode,
					final List<DynamicField> lstDynamicField, Map<String, Object> inputMap) throws Exception {
				final Map<String, Object> set = new HashMap<String, Object>();
				ObjectMapper mapper = new ObjectMapper();
				final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

				if (lstDynamicField != null && lstDynamicField.size() > 0) {
					for (DynamicField objFields : lstDynamicField) {
						if (sformulacalculationcode.contains("$P")) {
							sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, objFields.getSparameter(),
									objFields.getSvalues());
						} else if (sformulacalculationcode.contains("$D")) {
							String s = sformulacalculationcode;
							int index = s.indexOf("$D");
							int LastIndex = s.indexOf("D$");
							StringBuffer sb = new StringBuffer(s);
							sb.replace(index, LastIndex + 2, objFields.getSvalues());
							s = sb.toString();
							sformulacalculationcode = s;
						}
					}
				}

				String strQuery = "select * from operators where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				List<Operators> lstOperator = (List<Operators>) jdbcTemplate.query(strQuery, new Operators());

				String strQuery1 = "select * from functions where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				List<Functions> lstFunction = (List<Functions>) jdbcTemplate.query(strQuery1, new Functions());

				while (sformulacalculationcode.contains("$O")) {
					int index = sformulacalculationcode.indexOf("$O");
					int LastIndex = sformulacalculationcode.indexOf("O$");
					String actualText = sformulacalculationcode.substring(index + 2, LastIndex);
					final int noperatorcode = Integer.parseInt(actualText);
					if (noperatorcode > 0) {
						Collection<Operators> filtered = Collections2.filter(lstOperator, new Predicate<Operators>() {
							public boolean apply(Operators operators) {
								if (noperatorcode == operators.getNoperatorcode())
									return true;
								else
									return false;
							}
						});
						if (filtered.size() > 0) {
							String str1 = filtered.iterator().next().getSoperator();
							String str = "$O" + noperatorcode + "O$";
							sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, str, str1);
						}
					}
				}
				while (sformulacalculationcode.contains("$F")) {
					int index = sformulacalculationcode.indexOf("$F");
					int LastIndex = sformulacalculationcode.indexOf("F$");
					String actualText = sformulacalculationcode.substring(index + 2, LastIndex);
					final int nfunctioncode = Integer.parseInt(actualText);
					if (nfunctioncode > 0) {
						Collection<Functions> filtered1 = Collections2.filter(lstFunction, new Predicate<Functions>() {
							@Override
							public boolean apply(Functions functions) {
								if (nfunctioncode == functions.getNfunctioncode())
									return true;
								else
									return false;
							}
						});
		//ALPD-4061
//						if(filtered1.size()>0)
//						{
//							String str1=filtered1.iterator().next().getSfunctionname();
//							String str="$F"+nfunctioncode+"F$";
//							sformulacalculationcode = replaceofString(sformulacalculationcode, str, str1);
//						}
						if (filtered1.size() > 0) {
							// ALPD-4061-----Nasrin------18-APR-2024
							JSONObject actionType = new JSONObject(filtered1.iterator().next().getJsondata());
							JSONObject outerObject = (JSONObject) actionType.get("sdisplayname");
							String str1 = outerObject.get(userInfo.getSlanguagetypecode()).toString();
							// String str1 = innerObject.toString();)
							String str = "$F" + nfunctioncode + "F$";
							sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, str, str1);
						}
					}
				}
				if (sformulacalculationcode.contains("$V")) {
					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, "$V", "");
				}
				if (sformulacalculationcode.contains("V$")) {
					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, "V$", "");
				}
				StringTokenizer st = new StringTokenizer(sformulacalculationcode, "+-*/%&|^=><>=<=<>!=!<!>", true); // "+-*/%&|^=><>=<=<>!=!<!>"
				if (inputMap.containsKey("dynamicfield")) { // don't change this code....
					// StringTokenizer st = new StringTokenizer(query, "+-*/", true);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.contains("AVERAGE")) {
							String strQuery123 = token;
							String stavg = "(select AVG(col1) from (values (";
							int index = strQuery123.indexOf("AVERAGE(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 8, indexout);
							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}

						} else if (token.contains("MINIMUM")) {
							String strQuery123 = token;
							String stavg = "(select MIN(col1) from (values (";
							int index = strQuery123.indexOf("MINIMUM(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 8, indexout);
							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}

						} else if (token.contains("MAXIMUM")) {
							String strQuery123 = token;
							String stavg = "(select MAX(col1) from (values (";
							int index = strQuery123.indexOf("MAXIMUM(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 8, indexout);
							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}
						} else if (token.contains("STDDEV")) {
							String strQuery123 = token;
							String stavg = "(select STDEV(col1) from (values (";
							int index = strQuery123.indexOf("STDDEV(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 7, indexout);
							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}
						}
					}
				} else {

					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.contains("AVERAGE")) {
							String strQuery123 = token;
							String stavg = "(select AVG(col1) from (values (";
							int index = strQuery123.indexOf("AVERAGE(");
							int indexout = strQuery123.indexOf(")");
							// ALPD-4067------------NASRIN------added if(indexout ==
							// -1)------08-MAY-2024------validated -1 index issue.
							if (indexout == -1) {
								set.put("Result", commonFunction.getMultilingualMessage("IDS_INVALIDDATA",
										userInfo.getSlanguagefilename()));
								return new ResponseEntity<Object>(set, HttpStatus.EXPECTATION_FAILED);
							} else {
								strQuery123 = token.substring(index + 8, indexout);
								if (strQuery123.contains(",")) {
									strQuery123 = strQuery123.replaceAll(",", "),(");
									stavg = stavg + strQuery123 + ")) x(col1))";
									sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
								} else {
									stavg = stavg + strQuery123 + ")) x(col1))";
									sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
								}
							}
							// ALPD-4067------------NASRIN------end

							// ALPD-4067------------NASRIN------added else
							// if(token.contains("SCIENTIFICNOTATION")){------08-MAY-2024------Scientific
							// notation.
						} else if (token.contains("SCIENTIFICNOTATION")) {
							String strQuery123 = token;
							String stavg = "(select to_char(col1,'9.999EEEE'))";
							int index = strQuery123.indexOf("to_char(");
							int indexout = strQuery123.indexOf(")");
							if (indexout == -1) {
								set.put("Result", commonFunction.getMultilingualMessage("IDS_INVALIDDATA",
										userInfo.getSlanguagefilename()));
								return new ResponseEntity<Object>(set, HttpStatus.EXPECTATION_FAILED);
							} else {
								strQuery123 = token.substring(index + 20, indexout);
								sformulacalculationcode = stavg.replace("col1", strQuery123);
							}
							// ALPD-4067------------NASRIN------end
						} else if (token.contains("MINIMUM")) {
							String strQuery123 = token;
							String stavg = "(select MIN(col1) from (values (";
							int index = strQuery123.indexOf("MINIMUM(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 8, indexout);

							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}

						} else if (token.contains("MAXIMUM")) {
							String strQuery123 = token;
							String stavg = "(select MAX(col1) from (values (";
							int index = strQuery123.indexOf("MAXIMUM(");
							int indexout = strQuery123.indexOf(")");
							strQuery123 = token.substring(index + 8, indexout);
							if (strQuery123.contains(",")) {
								strQuery123 = strQuery123.replaceAll(",", "),(");
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							} else {
								stavg = stavg + strQuery123 + ")) x(col1))";
								sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
							}
						} else if (token.contains("STDDEV")) {
							String strQuery123 = token;
							// ALPD-4067---------NASRIN------modified----STDEV(col1) for
							// MSSQL--to---STDDEV(col1) for POSTGRESQL----08-MAY-2024----not working in
							// POSTGRSQL.
							String stavg = "(select STDDEV(col1) from (values (";
							int index = strQuery123.indexOf("STDDEV(");
							int indexout = strQuery123.indexOf(")");
							// ALPD-4067------------NASRIN------added if(indexout ==
							// -1)------08-MAY-2024------validated -1 index issue.
							if (indexout == -1) {
								set.put("Result", commonFunction.getMultilingualMessage("IDS_INVALIDDATA",
										userInfo.getSlanguagefilename()));
								return new ResponseEntity<Object>(set, HttpStatus.EXPECTATION_FAILED);
							} else {
								strQuery123 = token.substring(index + 7, indexout);
								if (strQuery123.contains(",")) {
									strQuery123 = strQuery123.replaceAll(",", "),(");
									stavg = stavg + strQuery123 + ")) x(col1))";
									sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
								} else {
									stavg = stavg + strQuery123 + ")) x(col1))";
									sformulacalculationcode = sformulacalculationcode.replace(token, stavg);
								}
							} // ALPD-4067------------NASRIN------end
						}
					}
				}

				while (sformulacalculationcode.contains("AVERAGE")) {
					String strQuery123 = sformulacalculationcode;
					String stavg = "(select AVG(col1) from (values ";
					int index = strQuery123.indexOf("AVERAGE(");
					// int indexout = strQuery123.indexOf(")");
					String strdefault = sformulacalculationcode.substring(index, strQuery123.length());
					strQuery123 = sformulacalculationcode.substring(index + 8, strQuery123.length() - 1);
					while (strQuery123.contains("(")) {

					}

					if (strQuery123.contains(",")) {
						strQuery123 = strQuery123 + ",";
						while (strQuery123.contains(",")) {
							int index123 = strQuery123.indexOf(",");
							stavg = stavg + "(" + strQuery123.substring(0, index123) + "),";
							strQuery123 = strQuery123.substring(index123 + 1, strQuery123.length());
						}
						stavg = stavg.substring(0, stavg.length() - 1);
						stavg = stavg + ") x(col1))";
					} else {
						stavg = stavg + "(" + strQuery123 + ")) x(col1))";
					}

					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, strdefault, stavg);
				}
				while (sformulacalculationcode.contains("MINIMUM")) {
					String strQuery123 = sformulacalculationcode;
					String stavg = "(select MIN(col1) from (values ";
					int index = strQuery123.indexOf("MINIMUM(");
					// int indexout = strQuery123.indexOf(")");
					String strdefault = sformulacalculationcode.substring(index, strQuery123.length());
					strQuery123 = sformulacalculationcode.substring(index + 8, strQuery123.length() - 1);
					while (strQuery123.contains("(")) {
					}
					if (strQuery123.contains(",")) {
						strQuery123 = strQuery123 + ",";
						while (strQuery123.contains(",")) {
							int index123 = strQuery123.indexOf(",");
							stavg = stavg + "(" + strQuery123.substring(0, index123) + "),";
							strQuery123 = strQuery123.substring(index123 + 1, strQuery123.length());
						}
						stavg = stavg.substring(0, stavg.length() - 1);
						stavg = stavg + ") x(col1))";
					} else {
						stavg = stavg + "(" + strQuery123 + ")) x(col1))";
					}
					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, strdefault, stavg);
				}
				while (sformulacalculationcode.contains("MAXIMUM")) {
					String strQuery123 = sformulacalculationcode;
					String stavg = "(select MAX(col1) from (values ";
					int index = strQuery123.indexOf("MAXIMUM(");
					// int indexout = strQuery123.indexOf(")");
					String strdefault = sformulacalculationcode.substring(index, strQuery123.length());
					strQuery123 = sformulacalculationcode.substring(index + 8, strQuery123.length() - 1);
					while (strQuery123.contains("(")) {
					}
					if (strQuery123.contains(",")) {
						strQuery123 = strQuery123 + ",";
						while (strQuery123.contains(",")) {
							int index123 = strQuery123.indexOf(",");
							stavg = stavg + "(" + strQuery123.substring(0, index123) + "),";
							strQuery123 = strQuery123.substring(index123 + 1, strQuery123.length());
						}
						stavg = stavg.substring(0, stavg.length() - 1);
						stavg = stavg + ") x(col1))";
					} else {
						stavg = stavg + "(" + strQuery123 + ")) x(col1))";
					}
					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, strdefault, stavg);
				}
				while (sformulacalculationcode.contains("STDEV")) {
					String strQuery123 = sformulacalculationcode;
					String stavg = "(select STDDEV(col1) from (values ";
					int index = strQuery123.indexOf("STDDEV(");
					// int indexout = strQuery123.indexOf(")");
					String strdefault = sformulacalculationcode.substring(index, strQuery123.length());
					strQuery123 = sformulacalculationcode.substring(index + 7, strQuery123.length() - 1);
					while (strQuery123.contains("(")) {

					}

					if (strQuery123.contains(",")) {
						strQuery123 = strQuery123 + ",";
						while (strQuery123.contains(",")) {
							int index123 = strQuery123.indexOf(",");
							stavg = stavg + "(" + strQuery123.substring(0, index123) + "),";
							strQuery123 = strQuery123.substring(index123 + 1, strQuery123.length());
						}
						stavg = stavg.substring(0, stavg.length() - 1);
						stavg = stavg + ") x(col1))";
					} else {
						stavg = stavg + "(" + strQuery123 + ")) x(col1))";
					}

					sformulacalculationcode = stringUtilityFunction.replaceofString(sformulacalculationcode, strdefault, stavg);
				}
				// logging.info("select " + query + " as result");
				Connection con = null;
				Statement statement = null;
				ResultSet resultSet = null;
				DataSource ds = null;
				String res = null;
				try {
					ds = jdbcTemplate.getDataSource();
					con = DataSourceUtils.getConnection(ds);
					statement = con.createStatement();
					if (inputMap.containsKey("dynamicfield")) {
						resultSet = statement.executeQuery("Select ( " + sformulacalculationcode + " )");
					} else {
						resultSet = statement.executeQuery("Select  " + sformulacalculationcode);
					}
					while (resultSet.next()) {
						res = resultSet.getString(1);
					}
				} catch (Exception e) {

					set.put("Query", sformulacalculationcode);
					// set.put("Result",e.getMessage());
					set.put("Result",
							commonFunction.getMultilingualMessage("IDS_INVALIDDATA", userInfo.getSlanguagefilename()));
					return new ResponseEntity<Object>(set, HttpStatus.EXPECTATION_FAILED);
				} finally {
					//ALPD-4793 done by Dhanushya RI,Throw alert if the formula syntax or result set is invalid
					if(res==null) {
						set.put("Query", sformulacalculationcode);
						set.put("Result",
								commonFunction.getMultilingualMessage("IDS_INVALIDDATA", userInfo.getSlanguagefilename()));
						return new ResponseEntity<Object>(set, HttpStatus.EXPECTATION_FAILED);
					}
					else {
					DataSourceUtils.releaseConnection(con, ds);
					}
				}
				set.put("Query", sformulacalculationcode);
				set.put("Result", res);
				return new ResponseEntity<Object>(set, HttpStatus.OK);
			}
			
			
			@Override
			public ResponseEntity<Object> setDefaultTestFormula(final TestFormula objTestFormula, final UserInfo objUserInfo,final int isQualisLite)
					throws Exception {
				final TestMaster objTestMaster = testMasterCommonFunction.checkTestIsPresent(objTestFormula.getNtestcode());
				if (objTestMaster != null) {
					String sQuery = "select * from testformula where ntestformulacode = " + objTestFormula.getNtestformulacode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestFormula objTF = (TestFormula) jdbcUtilityFunction.queryForObject(sQuery, TestFormula.class,jdbcTemplate); 
					if (objTF != null) {
						if (objTF.getNdefaultstatus() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
							final List<String> multilingualIDList = new ArrayList<>();
							final List<Object> lstOldObject = new ArrayList<>();
							final List<Object> lstNewObject = new ArrayList<>();
							sQuery = "select * from testformula where ntestcode = " + objTestFormula.getNtestcode()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntestformulacode <> " + objTestFormula.getNtestformulacode()
									+ " and ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<TestFormula> lstTestFormula = (List<TestFormula>) jdbcTemplate.query(sQuery,
									new TestFormula());
							String sUpdateQuery = "";
							if (lstTestFormula != null && lstTestFormula.size() > 0) {
								lstOldObject.add(SerializationUtils.clone(lstTestFormula.get(0)));
								TestFormula objNewTestFormula = new TestFormula();
								objNewTestFormula = lstTestFormula.get(0);
								sUpdateQuery = "update testformula set ndefaultstatus = "
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestformulacode = "
										+ objNewTestFormula.getNtestformulacode() + ";";
								objNewTestFormula
										.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
								lstNewObject.add(objNewTestFormula);
								multilingualIDList.add("IDS_DEFAULTTESTFORMULA");
							}
							//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
							if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								final String sformulacodeQuery="select * from testgrouptestformula where ntestgrouptestcode in(select ttp.ntestgrouptestcode from testgrouptestparameter ttp,testgrouptest tgt where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and ntestparametercode="+objTestFormula.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and tgt.ntestcode="+objTestFormula.getNtestcode()+" "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							TestGroupTestFormula objTestGroupTestFormula =  (TestGroupTestFormula)jdbcUtilityFunction.queryForObject(sformulacodeQuery,  TestGroupTestFormula.class,jdbcTemplate); 

					   			if(objTestGroupTestFormula.getNtestformulacode()!=objTestFormula.getNtestformulacode()) {
					   				
					   				sUpdateQuery=sUpdateQuery+"update testgrouptestformula set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" , ntransactionstatus="+ Enumeration.TransactionStatus.NO.gettransactionstatus()+","
											 +" dmodifieddate ='" +dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestgrouptestformulacode = "+objTestGroupTestFormula.getNtestgrouptestformulacode()+";";
					   			}
					   			final String sequencequery="select nsequenceno from seqnotestgroupmanagement where stablename in (N'testgrouptestformula')";
						   		int ntestgrouptestformulacode = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						   		ntestgrouptestformulacode++;
						   		
						   		final String updateFormulaseq = "update seqnotestgroupmanagement set nsequenceno =" + ntestgrouptestformulacode
										+ " where stablename ='testgrouptestformula'";
						   		jdbcTemplate.execute(updateFormulaseq);
								
								sUpdateQuery=sUpdateQuery+"insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode,"
								+ " sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, dmodifieddate,nstatus,nsitecode)"
								+ " values (" + ntestgrouptestformulacode + "," + objTestGroupTestFormula.getNtestgrouptestcode()+"," + objTestGroupTestFormula.getNtestgrouptestparametercode()+","
								+ objTestFormula.getNtestformulacode() + "," + "N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationcode()) + "'" + ",N'"
								+ stringUtilityFunction.replaceQuote(objTestFormula.getSformulacalculationdetail()) + "',"
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
								+ "'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","+objUserInfo.getNmastersitecode()+ ");";
					   		
							}
							sUpdateQuery = sUpdateQuery + "update testformula set ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestformulacode = "
									+ objTestFormula.getNtestformulacode() + ";";
							jdbcTemplate.execute(sUpdateQuery);
							multilingualIDList.add("IDS_DEFAULTTESTFORMULA");
							lstNewObject.add(objTestFormula);
							lstOldObject.add(objTF);
							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
							return new ResponseEntity<>(
									testMasterCommonFunction.getTestParameterFormula(objTestFormula.getNtestparametercode(), objUserInfo),
									HttpStatus.OK);
						} else {
							// status code:417
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYDEFAULT.getreturnstatus(),
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> setDefaultTestPredefinedParameter(
					final TestPredefinedParameter objTestPredefinedParameter, final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				String sQuery = "select * from testpredefinedparameter where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestpredefinedcode = "
						+ objTestPredefinedParameter.getNtestpredefinedcode();
				final TestPredefinedParameter objTestPredefined = (TestPredefinedParameter) jdbcUtilityFunction.queryForObject(sQuery,
						TestPredefinedParameter.class,jdbcTemplate); 
				if (objTestPredefined != null) {
					// if(objTestPredefined.getNdefaultstatus() ==
					// Enumeration.TransactionStatus.NO.gettransactionstatus()) {
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstOldObject = new ArrayList<>();
					final List<Object> lstNewObject = new ArrayList<>();
					sQuery = "select * from testpredefinedparameter where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntestparametercode = "
							+ objTestPredefinedParameter.getNtestparametercode() + " and ntestpredefinedcode <> "
							+ objTestPredefinedParameter.getNtestpredefinedcode();
					List<TestPredefinedParameter> lstPredef = (List<TestPredefinedParameter>) jdbcTemplate.query(sQuery,
							new TestPredefinedParameter());
					String sUpdateQuery = "";

					if (lstPredef != null && lstPredef.size() > 0) {
						lstOldObject.add(SerializationUtils.clone(lstPredef.get(0)));
						TestPredefinedParameter objNewPredefined = new TestPredefinedParameter();
						objNewPredefined = lstPredef.get(0);
						sUpdateQuery = "update testpredefinedparameter set ndefaultstatus = "
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestpredefinedcode = "
								+ objNewPredefined.getNtestpredefinedcode() + ";";
						objNewPredefined.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						// }

//						sUpdateQuery = sUpdateQuery + "update testpredefinedparameter set ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//										+" where ntestpredefinedcode = "+objTestPredefinedParameter.getNtestpredefinedcode()+";";
						sUpdateQuery = sUpdateQuery + "update testpredefinedparameter set ndefaultstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestpredefinedcode = "
								+ objTestPredefinedParameter.getNtestpredefinedcode() + ";";
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						 String query="select * from testgrouptestpredefparameter where  ntestgrouptestparametercode=(select ttp.ntestgrouptestparametercode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredefinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and spredefinedname=N'"+objNewPredefined.getSpredefinedname()+"'";
					   		final TestGroupTestPredefinedParameter oldTestGroupTestPredefinedParameter=(TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(query,TestGroupTestPredefinedParameter.class,jdbcTemplate); 
							
					   	 String query1="select * from testgrouptestpredefparameter where  ntestgrouptestparametercode=(select ttp.ntestgrouptestparametercode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestPredefinedParameter.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and spredefinedname=N'"+objTestPredefinedParameter.getSpredefinedname()+"'";
					   		final TestGroupTestPredefinedParameter newTestGroupTestPredefinedParameter=(TestGroupTestPredefinedParameter) jdbcUtilityFunction.queryForObject(query1,TestGroupTestPredefinedParameter.class,jdbcTemplate); 
							
					   		sUpdateQuery = sUpdateQuery+"update testgrouptestpredefparameter set ndefaultstatus= "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ",dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + "where ntestgrouptestpredefcode = "
									+ oldTestGroupTestPredefinedParameter.getNtestgrouptestpredefcode()+";";	
					   		
					   		sUpdateQuery = sUpdateQuery+"update testgrouptestpredefparameter set ndefaultstatus= "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + "where ntestgrouptestpredefcode = "
											+ newTestGroupTestPredefinedParameter.getNtestgrouptestpredefcode()+";";
						}	
						jdbcTemplate.execute(sUpdateQuery);

						lstNewObject.add(objNewPredefined);
						multilingualIDList.add("IDS_EDITCODEDRESULT");
						lstOldObject.add(objTestPredefined);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(
								testMasterCommonFunction.getTestPredefinedParameter(objTestPredefinedParameter.getNtestparametercode(), objUserInfo),
								HttpStatus.OK);
//						multilingualIDList.add("IDS_EDITCODEDRESULT");
//						lstNewObject.add(objTestPredefinedParameter);
//						lstOldObject.add(objTestPredefined);
//						fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
//						return new ResponseEntity<>(getTestPredefinedParameter(objTestPredefinedParameter.getNtestparametercode(), objUserInfo), HttpStatus.OK);
					} else {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDEFAULT.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> createTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				final String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate);
				int formAge = objTestParameterNumeric.getNfromage();
				int toAge = objTestParameterNumeric.getNtoage();
				if (objTestParameter != null) {
					if (formAge > toAge) {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_TOAGEMUSTMORETHANFROM",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String sDuplicateCheckQry = " (select generate_series(nfromage,ntoage) from testmasterclinicalspec where ngendercode="
								+ objTestParameterNumeric.getNgendercode() + " and ntestparametercode="
								+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " intersect "
								+ " SELECT * FROM generate_series(" + objTestParameterNumeric.getNfromage() + ","
								+ objTestParameterNumeric.getNtoage() + ")";
						List<Integer> objPredefinedParameter = jdbcTemplate.queryForList(sDuplicateCheckQry, Integer.class);
						if (objPredefinedParameter.size() == 0) {
							String seqnumericquery = "select nsequenceno from SeqNoTestManagement where stablename='testmasterclinicalspec'";
							int nseqnumericcount = jdbcTemplate.queryForObject(seqnumericquery, Integer.class);
							nseqnumericcount++;

							
					   		
		        //ALPD-5053 added by Dhanushya RI,To insert fromage period and toage period
							String insertnumericquery = " Insert into testmasterclinicalspec(ntestmasterclinicspeccode, ntestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb,"
									+ " sresultvalue, ngradecode, sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate, dmodifieddate, nsitecode, nstatus,nfromageperiod,ntoageperiod)"
									+ " values (" + nseqnumericcount + "," + objTestParameterNumeric.getNtestparametercode()
									+ "," + objTestParameterNumeric.getNgendercode() + ","
									+ objTestParameterNumeric.getNfromage() + "," + objTestParameterNumeric.getNtoage() + ",N'"
									+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShigha()) + "',N'"
									+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShighb()) + "'," + " N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowa())) + "',N'"
									+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowb()) + "',N'"
									+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "',"
									+ objTestParameterNumeric.getNgradecode() + " ,N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod())) + "',N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())) + "',N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq())) + "',N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())) + "'," + " N'"
									+ (stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard())) + "',"
									+ objUserInfo.getNtimezonecode() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", '"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +","+objTestParameterNumeric.getNfromageperiod()+","+objTestParameterNumeric.getNtoageperiod()+ ");";
							jdbcTemplate.execute(insertnumericquery);

							//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
							if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String testParameterSorterQuery="select * from testgrouptestparameter tp,testgrouptest tgt where tp.ntestparametercode="+objTestParameter.getNtestparametercode()+" and "
									+ " tgt.ntestgrouptestcode=tp.ntestgrouptestcode and  ntestcode="+objTestParameter.getNtestcode()+" and nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+" and "
									+ " tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							TestGroupTestParameter objTestGroupTestParameter=(TestGroupTestParameter) jdbcUtilityFunction.queryForObject(testParameterSorterQuery,TestGroupTestParameter.class,jdbcTemplate); 

							
							final String seqQuery = "select nsequenceno from seqnotestgroupmanagement where stablename in ('testgrouptestclinicalspec')";
					   		int ntestgrouptestclinicspeccode =  jdbcTemplate.queryForObject(seqQuery,Integer.class);
					   		ntestgrouptestclinicspeccode++;
					   		String query="";
		        //ALPD-5053 added by Dhanushya RI,To insert fromage period and toage period
					   		query="insert into testgrouptestclinicalspec (ntestgrouptestclinicspeccode, ntestgrouptestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb, sresultvalue,"
									+ " sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate,dmodifieddate, nstatus,nsitecode, ngradecode,nfromageperiod,ntoageperiod)"
									+ " values (" + ntestgrouptestclinicspeccode + "," + objTestGroupTestParameter.getNtestgrouptestparametercode() + "," + objTestParameterNumeric.getNgendercode()+","+objTestParameterNumeric.getNfromage()+","+objTestParameterNumeric.getNtoage()+","
									+ "case when N'"+ objTestParameterNumeric.getShigha() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getShigha() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getShighb() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getShighb() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSlowa() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSlowa() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSlowb() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSlowb() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSresultvalue() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSresultvalue() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSminlod() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSminlod() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSmaxlod() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSmaxlod() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSminloq() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSminloq() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSmaxloq() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSmaxloq() + "' end," 
									+ "case when N'"+ objTestParameterNumeric.getSdisregard() + "'='null' then  NULL else N'"+ objTestParameterNumeric.getSdisregard() + "' end," 
									+ objUserInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())+ ",'"+dateUtilityFunction.getCurrentDateTime(objUserInfo)+"',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","+objUserInfo.getNmastersitecode()+","+objTestParameterNumeric.getNgradecode()+","
									+ " "+objTestParameterNumeric.getNfromageperiod()+","+objTestParameterNumeric.getNtoageperiod()+");";

							
							 query=query+"update seqnotestgroupmanagement set nsequenceno ="+ntestgrouptestclinicspeccode+" where stablename ='testgrouptestclinicalspec';";
							 jdbcTemplate.execute(query);
							}

							String updatenumericquery = "update SeqNoTestManagement set nsequenceno =" + nseqnumericcount
									+ " where stablename ='testmasterclinicalspec'";
							jdbcTemplate.execute(updatenumericquery);

							final List<Object> lstnewobject = new ArrayList<>();
							final List<String> lstactionType = new ArrayList<>();
							lstnewobject.add(objTestParameterNumeric);
							lstactionType.add("IDS_ADDSPECLIMIT");
							auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
							final Map<String, Object> outMap = new HashMap<String, Object>();
							outMap.put("TestMasterClinicalSpec",
									testMasterCommonFunction.getTestParameterClinialSpec(objTestParameterNumeric.getNtestparametercode(), objUserInfo));
							return new ResponseEntity<Object>(outMap, HttpStatus.OK);
						} else {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.CLINICALSPECALREADYEXISTS.getreturnstatus(),
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> updateTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParameter != null) {
					final String strQuery = "select * from testmasterclinicalspec  where ntestmasterclinicspeccode = "
							+ objTestParameterNumeric.getNtestmasterclinicspeccode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<TestMasterClinicalSpec> lst1 = (List<TestMasterClinicalSpec>) jdbcTemplate.query(strQuery,
							new TestMasterClinicalSpec());
					if (lst1.isEmpty()) {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else {
						if (objTestParameterNumeric.getNfromage() > objTestParameterNumeric.getNtoage()) {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_TOAGEMUSTMORETHANFROM",
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						} else {
							String sDuplicateCheckQry = " (select generate_series(nfromage,ntoage) from testmasterclinicalspec where ngendercode="
									+ objTestParameterNumeric.getNgendercode() + " and ntestparametercode="
									+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntestmasterclinicspeccode <> "
									+ objTestParameterNumeric.getNtestmasterclinicspeccode() + ") " + " intersect "
									+ " SELECT * FROM generate_series(" + objTestParameterNumeric.getNfromage() + ","
									+ objTestParameterNumeric.getNtoage() + ")";
							List<Integer> objPredefinedParameter = jdbcTemplate.queryForList(sDuplicateCheckQry, Integer.class);
							if (objPredefinedParameter.size() == 0) {
		        //ALPD-5053 added by Dhanushya RI,To update fromage period and toage period
								sQuery = "update testmasterclinicalspec set " + "sminlod = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "' end,"
										+ "smaxlod = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())
										+ "' end," + "shigha = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShigha())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShigha())
										+ "' end," + "shighb = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShighb())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShighb())
										+ "' end," + "slowa = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowa())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowa())
										+ "' end," + "slowb = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowb())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowb())
										+ "' end," + "sminloq = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "' end,"
										+ "smaxloq = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())
										+ "' end," + "sdisregard = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "' end,"
										+ "sresultvalue = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "' end,"
										+ " dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ,ngradecode="
										+ objTestParameterNumeric.getNgradecode() + ",nfromage="
										+ objTestParameterNumeric.getNfromage() + " ,ntoage="
										+ objTestParameterNumeric.getNtoage() + ", ngendercode="
										+ objTestParameterNumeric.getNgendercode() + ",nfromageperiod="+objTestParameterNumeric.getNfromageperiod()+""
										+ " ,ntoageperiod="+objTestParameterNumeric.getNtoageperiod()+" where ntestmasterclinicspeccode = "
										+ objTestParameterNumeric.getNtestmasterclinicspeccode()+";";
								
								//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
								if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
									
								final String query1="select * from testgrouptestclinicalspec where  ntestgrouptestclinicspeccode=(select tgtc.ntestgrouptestclinicspeccode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp,testgrouptestclinicalspec tgtc where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
							   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestParameterNumeric.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
							   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.ntestgrouptestparametercode=tgtc.ntestgrouptestparametercode and tgtc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ngendercode="+lst1.get(0).getNgendercode()+" and nfromage="+lst1.get(0).getNfromage()+" and ntoage="+lst1.get(0).getNtoage()+")"
							   				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
							   				
								 TestGroupTestClinicalSpec objTestGroupTestClinicalSpec=(TestGroupTestClinicalSpec) jdbcUtilityFunction.queryForObject(query1,TestGroupTestClinicalSpec.class,jdbcTemplate); 

								sQuery = sQuery+"update testgrouptestclinicalspec set " + "sminlod = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminlod()) + "' end,"
										+ "smaxlod = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxlod())
										+ "' end," + "shigha = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShigha())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShigha())
										+ "' end," + "shighb = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShighb())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getShighb())
										+ "' end," + "slowa = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowa())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowa())
										+ "' end," + "slowb = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowb())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSlowb())
										+ "' end," + "sminloq = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSminloq()) + "' end,"
										+ "smaxloq = case when N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())
										+ "'='null' then NULL else N'" + stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSmaxloq())
										+ "' end," + "sdisregard = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSdisregard()) + "' end,"
										+ "sresultvalue = case when N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "'='null' then NULL else N'"
										+ stringUtilityFunction.replaceQuote(objTestParameterNumeric.getSresultvalue()) + "' end,"
										+ " dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ,ngradecode="
										+ objTestParameterNumeric.getNgradecode() + ",nfromage="
										+ objTestParameterNumeric.getNfromage() + " ,ntoage="
										+ objTestParameterNumeric.getNtoage() + ", ngendercode="
										+ objTestParameterNumeric.getNgendercode() + " where ntestgrouptestclinicspeccode = "
										+ objTestGroupTestClinicalSpec.getNtestgrouptestclinicspeccode();
								
								}
								
								jdbcTemplate.execute(sQuery);
								final List<Object> lstnewobject = new ArrayList<>();
								final List<String> lstactionType = new ArrayList<>();
								lstnewobject.add(objTestParameterNumeric);
								lstactionType.add("IDS_EDITSPECLIMIT");
								auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lst1, lstactionType, objUserInfo);
								final Map<String, Object> outMap = new HashMap<String, Object>();
								outMap.put("TestMasterClinicalSpec", testMasterCommonFunction.getTestParameterClinialSpec(
										objTestParameterNumeric.getNtestparametercode(), objUserInfo));
								return new ResponseEntity<Object>(outMap, HttpStatus.OK);
							} else {
								return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
										Enumeration.ReturnStatus.CLINICALSPECALREADYEXISTS.getreturnstatus(),
										objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
							}
						}
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			
			@Override
			public ResponseEntity<Object> deleteTestParameterClinicalSpec(final TestMasterClinicalSpec objTestParameterNumeric,
					final UserInfo objUserInfo,final int isQualisLite) throws Exception {
				String sQuery = "select * from testparameter where ntestparametercode = "
						+ objTestParameterNumeric.getNtestparametercode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestParameter objTestParameter = (TestParameter) jdbcUtilityFunction.queryForObject(sQuery, TestParameter.class,jdbcTemplate); 
				if (objTestParameter != null) {
					final String strQuery = "select * from testmasterclinicalspec  where ntestmasterclinicspeccode = "
							+ objTestParameterNumeric.getNtestmasterclinicspeccode() + " and  nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final TestMasterClinicalSpec objParameterNumeric = (TestMasterClinicalSpec) jdbcUtilityFunction.queryForObject(strQuery,
							TestMasterClinicalSpec.class,jdbcTemplate); 
					if (objParameterNumeric == null) {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} else {
						
						sQuery = "update testmasterclinicalspec set   nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestmasterclinicspeccode = "
								+ objTestParameterNumeric.getNtestmasterclinicspeccode()+";";
						//ALPD-4831,5873--Vignesh R(08-10-2024)--When nsettingcode 71 is set to 4, then QualisLite= true and the Test Group is not required and is automatically updated by default in the relevant test group-related tables.
						if(isQualisLite==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							final String query1="select * from testgrouptestclinicalspec where  ntestgrouptestclinicspeccode=(select tgtc.ntestgrouptestclinicspeccode from testgrouptestparameter ttp,testgrouptest tgt,testparameter tp,testgrouptestclinicalspec tgtc where ttp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					   				+ " and tp.ntestparametercode=ttp.ntestparametercode and   ttp.ntestparametercode="+objTestParameterNumeric.getNtestparametercode()+" and tgt.nspecsampletypecode="+Enumeration.Default.DEFAULTSPEC.getDefault()+"  "
					   				+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					   				+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ttp.ntestgrouptestparametercode=tgtc.ntestgrouptestparametercode and tgtc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ngendercode="+objParameterNumeric.getNgendercode()+" and nfromage="+objParameterNumeric.getNfromage()+" and ntoage="+objParameterNumeric.getNtoage()+")"
					   				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
							
							 TestGroupTestClinicalSpec objTestGroupTestClinicalSpec=(TestGroupTestClinicalSpec) jdbcUtilityFunction.queryForObject(query1,TestGroupTestClinicalSpec.class,jdbcTemplate); 

							sQuery = sQuery+"update testgrouptestclinicalspec set   nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where ntestgrouptestclinicspeccode = "
									+ objTestGroupTestClinicalSpec.getNtestgrouptestclinicspeccode();
						}
						
						jdbcTemplate.execute(sQuery);
						final List<Object> lstnewobject = new ArrayList<>();
						final List<String> lstactionType = new ArrayList<>();
						lstactionType.add("IDS_DELETESPECLIMIT");
						lstnewobject.add(objParameterNumeric);
						auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
						final Map<String, Object> outMap = new HashMap<String, Object>();
						outMap.put("TestMasterClinicalSpec",
								testMasterCommonFunction.getTestParameterClinialSpec(objTestParameterNumeric.getNtestparametercode(), objUserInfo));
						return new ResponseEntity<Object>(outMap, HttpStatus.OK);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTPARAMETERALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			@Override
			public ResponseEntity<Object> getTestParameterClinicalSpecById(int ntestmasterclinicspeccode, UserInfo objUserInfo)
					throws Exception {
				final String sQuery = "select tp.*, coalesce(gg.jsondata->'sdisplayname'->>'"
						+ objUserInfo.getSlanguagetypecode()
						+ "',gg.jsondata->'sdisplayname'->>'en-US') as sgradename,coalesce(g.jsondata->'sgendername'->>'"
						+ objUserInfo.getSlanguagetypecode() + "',g.jsondata->'sgendername'->>'en-US') as sgendername, "
						+ " COALESCE(p1.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"', p1.jsondata->'speriodname'->>'en-US')  AS sfromageperiod, "
						+"  COALESCE(p2.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"', p2.jsondata->'speriodname'->>'en-US')  AS stoageperiod "
						+ "  from testmasterclinicalspec tp,grade gg ,gender g,period p1,period p2 where gg.ngradecode=tp.ngradecode and g.ngendercode=tp.ngendercode"
						+ " and p1.nperiodcode=tp.nfromageperiod and p2.nperiodcode=tp.ntoageperiod and ntestmasterclinicspeccode = "
						+ ntestmasterclinicspeccode + " and tp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and p1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p2.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and g.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and gg.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final TestMasterClinicalSpec objTestParameterNumeric = (TestMasterClinicalSpec) jdbcUtilityFunction.queryForObject(sQuery,
						TestMasterClinicalSpec.class,jdbcTemplate);
				if (objTestParameterNumeric != null) {
					return new ResponseEntity<Object>(objTestParameterNumeric, HttpStatus.OK);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}

			
			@Override
			public ResponseEntity<Object> getPreDefinedFormula(final UserInfo objUserInfo) throws Exception {
				Map<String, List<?>> outMapList = new HashMap<String, List<?>>();

				final String sQuery = "select spredefinedformula,npredefinedformulacode,sdescription from predefinedformula "
						+ " where npredefinedformulacode>0 and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				List<PreDefinedFormula> lstPredefinedformula = jdbcTemplate.query(sQuery, new PreDefinedFormula());

				outMapList.put("PreDefinedFormula", lstPredefinedformula);

				return new ResponseEntity<Object>(outMapList, HttpStatus.OK);
			}
			//ALPD-4363
			@Override
			public ResponseEntity<Object> getResultAccuracy(UserInfo userInfo) throws Exception {
				final String sQry = "select nresultaccuracycode,sresultaccuracyname from resultaccuracy  where "
						+ "  nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				return new ResponseEntity<Object>(jdbcTemplate.query(sQry, new ResultAccuracy()), HttpStatus.OK);
			}


			
}
