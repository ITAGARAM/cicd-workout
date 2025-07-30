package com.agaramtech.qualis.testmanagement.service.testmaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestMasterCommonFunction  {
	

	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final DateTimeUtilityFunction dateUtilityFunction;
	  private final StringUtilityFunction stringUtilityFunction;
	  
	  
	  /**
		 * This method is used to fetch a selected testparameter and testparameter
		 * details.
		 * 
		 * @param objTestParameter [TestParameter] holds the object of test parameter
		 *                         details
		 * @param objUserInfo      [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the object of selected testparameter and the list of
		 *         testparameternumeric, testformula and testpredefinedparameter details
		 * @throws Exception
		 */
		public Map<String, Object> getParameterDetailsById(final TestParameter objTestParameter, final UserInfo objUserInfo)
				throws Exception {
			final Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("selectedParameter", convertSelectedParameter(objTestParameter, objUserInfo));
			if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype()) {
				map.put("TestParameterNumeric", getTestParameterNumeric(objTestParameter.getNtestparametercode(),objUserInfo));
				map.put("TestMasterClinicalSpec", getTestParameterClinialSpec(objTestParameter.getNtestparametercode(),objUserInfo));
				map.putAll(getTestParameterFormula(objTestParameter.getNtestparametercode(),objUserInfo));
			} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
					.getparametertype()) {
				map.putAll(getTestPredefinedParameter(objTestParameter.getNtestparametercode(), objUserInfo));
			}
			return map;
		}

		/**
		 * This method is used to apply the multilingual to the selected parameter
		 * object details
		 * 
		 * @param objSelectedParameter [TestParameter] holds the object of test
		 *                             parameter details
		 * @param objUserInfo          [UserInfo] holds the object of loggedin user info
		 * @return [TestParameter] holds the object of selected testparameter details
		 * @throws Exception
		 */
		@SuppressWarnings("unchecked")
		public TestParameter convertSelectedParameter(final TestParameter objSelectedParameter, final UserInfo objUserInfo)
				throws Exception {
			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("sdisplaystatus");
			List<TestParameter> lstTestParameter = (List<TestParameter>) commonFunction.getMultilingualMessageList(
					Arrays.asList(objSelectedParameter), lstcolumns, objUserInfo.getSlanguagefilename());
			ObjectMapper objMapper = new ObjectMapper();
			return objMapper.convertValue(lstTestParameter.get(0), TestParameter.class);
		}

		/**
		 * This method is used fetch a list of spec limit for numeric parameter type
		 * 
		 * @param ntestparametercode [int] holds the primary key of test parameter
		 *                           object
		 * @return [List] holds the list of numeric test parameter spec limits
		 * @throws Exception
		 */
		
		public List<TestParameterNumeric> getTestParameterNumeric(final int ntestparametercode,final UserInfo objUserInfo) throws Exception {
			final String sQuery = "select coalesce(g.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + 
					"',g.jsondata->'sdisplayname'->>'en-US') as sgradename ,ntestparamnumericcode, ntestparametercode, COALESCE(sminlod, '-') sminlod,"
					+ "COALESCE(smaxlod, '-') smaxlod, COALESCE(sminb, '-') sminb, COALESCE(smina, '-') smina, COALESCE(smaxa, '-') smaxa,"
					+ "COALESCE(smaxb, '-') smaxb, COALESCE(sminloq, '-') sminloq, COALESCE(smaxloq, '-') smaxloq, COALESCE(sdisregard, '-') sdisregard,"
					+ "COALESCE(sresultvalue, '-') sresultvalue, tpn.nstatus ,g.ngradecode"
					+ " from testparameternumeric tpn,grade g where ntestparametercode = " + ntestparametercode + " and  g.ngradecode=tpn.ngradecode and "
					+ " g.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpn.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			return (List<TestParameterNumeric>) jdbcTemplate.query(sQuery, new TestParameterNumeric());
		}

		/**
		 * This method is used to fetch a list of test parameter formula based on
		 * primarykey
		 * 
		 * @param ntestparametercode [int] holds the primarykey of testparameter object
		 * @return [Map] holds the list of test formula
		 * @throws Exception
		 */


		public Map<String, Object> getTestParameterFormula(final int ntestparametercode,UserInfo objUserInfo) throws Exception {

			final Map<String, Object> outputMap = new HashMap<String, Object>();

			final String sQuery = "select tf.ntestcode,tf.ntestparametercode,tf.ntestformulacode,tf.sformulacalculationcode,tf.sformulaname,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
			        + " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, "
					+ " tf.ndefaultstatus,tf.nispredefinedformula,case when tf.nispredefinedformula=3 then pf.sdescription else tf.sformulacalculationdetail end as sformulacalculationdetail from testformula tf,predefinedformula pf,transactionstatus ts "
					+ " where tf.ndefaultstatus = ts.ntranscode and pf.npredefinedformulacode=tf.npredefinedformulacode and tf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.ntestparametercode = "
					+ ntestparametercode;
			outputMap.put("TestFormula", jdbcTemplate.query(sQuery, new TestFormula()));
			return outputMap;
		}

		/**
		 * This method is used to fetch a list of test predefined parameter
		 * 
		 * @param ntestparametercode [int] holds the primarykey of testparameter object
		 * @param objUserInfo        [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the list of test predefined parameter
		 * @throws Exception
		 */
		public Map<String, Object> getTestPredefinedParameter(final int ntestparametercode, final UserInfo objUserInfo)
				throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();

//			final String sQuery = "select tp.ntestpredefinedcode,tp.ngradecode,tp.nstatus,tp.ndefaultstatus,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US')  as stransactionstatus,"
//					+ "tp.ntestparametercode,tp.spredefinedname,coalesce(g.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//					+ "g.jsondata->'sdisplayname'->>'en-US')sdisplaystatus,tp.sresultparacomment  from testpredefinedparameter tp,transactionstatus ts1,grade g "
//					+ "where tp.ndefaultstatus=ts1.ntranscode and tp.ngradecode=g.ngradecode and " + "ts1.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "g.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.ntestparametercode="
//					+ ntestparametercode;
//			final String sQuery = "select tp.ntestpredefinedcode,tp.ngradecode,tp.nstatus,tp.ndefaultstatus,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US')  as stransactionstatus,"
//					+ "tp.ntestparametercode,tp.spredefinedname,coalesce(g.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
//					+ "g.jsondata->'sdisplayname'->>'en-US')sdisplaystatus,case tp.sresultparacomment when  'null' then '' else tp.sresultparacomment end sresultparacomment  from testpredefinedparameter tp,transactionstatus ts1,grade g "
//					+ "where tp.ndefaultstatus=ts1.ntranscode and tp.ngradecode=g.ngradecode and " + "ts1.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "g.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.ntestparametercode="
//					+ ntestparametercode;
			final String sQuery = "select tp.ntestpredefinedcode,tp.ngradecode,tp.nstatus,tp.ndefaultstatus,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US')  as stransactionstatus,"
					+ "tp.ntestparametercode,tp.spredefinedname,coalesce(g.jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
					+ "g.jsondata->'sdisplayname'->>'en-US')sdisplaystatus,case tp.spredefinedsynonym when  'null' then '' else tp.spredefinedsynonym end spredefinedsynonym  from testpredefinedparameter tp,transactionstatus ts1,grade g "
					+ "where tp.ndefaultstatus=ts1.ntranscode and tp.ngradecode=g.ngradecode and " + "ts1.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "g.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "tp.ntestparametercode="
					+ ntestparametercode;

			List<String> lstcolumns = new ArrayList<>();
			lstcolumns.add("sdisplaystatus");
			outputMap.put("TestPredefinedParameter",
					commonFunction.getMultilingualMessageList((List<TestPredefinedParameter>)jdbcTemplate.query(sQuery,new TestPredefinedParameter()),
							lstcolumns, objUserInfo.getSlanguagefilename()));
			
			
			return outputMap;
		}

		/**
		 * This method is used to check whether the test is present or not
		 * 
		 * @param ntestcode [int] holds the primary key of Testmaster object
		 * @return [TestMaster] holds the object of test master
		 * @throws Exception
		 */
		public TestMaster checkTestIsPresent(final int ntestcode) throws Exception {
			String strQuery = "select ntestcode,ntrainingneed from testmaster where ntestcode = " + ntestcode + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			TestMaster objTest = (TestMaster) jdbcUtilityFunction.queryForObject(strQuery, TestMaster.class,jdbcTemplate); 
			return objTest;
		}

		/**
		 * This method is used to fetch a list of test parameter
		 * 
		 * @param ntestcode          [int] holds the primary key of test master object
		 * @param ntestparametercode [int] holds the primay key of test parameter object
		 * @return [List] holds the list of test parameter
		 * @throws Exception
		 */
		
		public List<TestParameter> getTestParameter(final int ntestcode, final int ntestparametercode,final UserInfo userInfo) throws Exception {
			String sQuery = "select tp.ntestparametercode,tp.ntestcode,tp.nparametertypecode,tp.nunitcode,t.stestname,t.sshortname,pt.sparametertypename,"
					+ "pt.sdisplaystatus,u.sunitname,tp.sparametername,tp.sparametersynonym,tp.nroundingdigits,tp.ndeltacheck,tp.ndestinationunitcode,case when tp.ndestinationunitcode<>-1  then u1.sunitname else '-' end  as sunitname1,"
					
					+ "tp.noperatorcode,case when tp.noperatorcode<>-1 then op.soperator else '-' end as soperator , tp.nconversionfactor, "
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"') as stransdisplaystatus,ra.sresultaccuracyname,tp.nresultaccuracycode from testparameter tp,testmaster t,"
					+ "unit u,parametertype pt,transactionstatus ts,unitconversion uc, unit u1,operators op,resultaccuracy ra where tp.ntestcode = t.ntestcode and tp.nunitcode = u.nunitcode "
					+ "and tp.nparametertypecode=pt.nparametertypecode and ts.ntranscode=tp.ndeltacheck "
					+ " and tp.ndestinationunitcode =uc.ndestinationunitcode and tp.ndestinationunitcode = u1.nunitcode and op.noperatorcode=tp.noperatorcode and tp.nresultaccuracycode=ra.nresultaccuracycode and tp.noperatorcode=uc.noperatorcode  and u1.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and t.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ra.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and op.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"" 
					+ " and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tp.ntestcode=" + ntestcode
					+ " and nisvisible=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
			if (ntestparametercode > 0) {
				sQuery = sQuery + " and tp.ntestparametercode = " + ntestparametercode;
			}
			sQuery += " order by tp.ntestparametercode;";
			return (List<TestParameter>) jdbcTemplate.query(sQuery, new TestParameter());
		}

		/**
		 * This method is used to fetch the list of test section
		 * 
		 * @param ntestcode   [int] holds the primary key of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the list of test section
		 * @throws Exception
		 */
		public Map<String, Object> getTestSection(final int ntestcode, final UserInfo objUserInfo) throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();

				final String query = "select ts.ntestsectioncode,ts.ntestcode,ts.ndefaultstatus,coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
					+ "	ts1.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
					+ " ts.nsectioncode,s.ssectionname from testsection ts,"
					+ " transactionstatus ts1,section s where ts.ndefaultstatus=ts1.ntranscode"
					+ " and s.nsectioncode>0 and  ts.nsectioncode=s.nsectioncode " + " and ts1.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
					+ " and ts.ntestcode=" + ntestcode
					+ " order by ts.ntestsectioncode;";
			outputMap.put("TestSection",jdbcTemplate.query(query, new TestSection()));
			return outputMap;
		}

		/**
		 * This method is used to fetch the list of test method
		 * 
		 * @param ntestcode   [int] holds the primary key of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the list of test method
		 * @throws Exception
		 */
		public Map<String, Object> getTestMethod(final int ntestcode, final UserInfo objUserInfo) throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();
			final String query = "select tm.ntestmethodcode,tm.ntestcode,tm.nmethodcode,m.smethodname,tm.ndefaultstatus,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
					+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from testmethod tm,method m,"
					+ " transactionstatus ts where tm.nmethodcode = m.nmethodcode "
					+ " and tm.ndefaultstatus=ts.ntranscode and ts.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and m.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" 
					+ " and tm.ntestcode=" + ntestcode
					+ " order by tm.ntestmethodcode;";
			outputMap.put("TestMethod", 
					jdbcTemplate.query(query, new TestMethod()));
			return outputMap;
		}

		/**
		 * This method is used to fetch the list of test file
		 * 
		 * @param ntestcode   [int] holds the primary key of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the list of test file
		 * @throws Exception
		 */
		public Map<String, Object> getTestFile(final int ntestcode, final UserInfo objUserInfo) throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();
		    String query = "select tf.noffsetdcreateddate,tf.ntestfilecode,(select  count(ntestfilecode) from testfile where ntestfilecode>0 and ntestcode = "
	                + ntestcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                + ") as ncount,tf.sdescription,"
	                + " tf.ntestfilecode as nprimarycode,tf.sfilename,tf.ntestcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
	                + "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tf.ssystemfilename,"
	                + " tf.ndefaultstatus,tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"+objUserInfo.getSlanguagetypecode()+"',"
	                + "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
	                + " end slinkname, tf.nfilesize,"
	                + " case when tf.nattachmenttypecode= "+ Enumeration.AttachmentType.LINK.gettype()+" then '-' else"
	                + " COALESCE(TO_CHAR(tf.dcreateddate,'"+objUserInfo.getSpgdatetimeformat()+"'),'-') end  as screateddate, "
	                + " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
	                + " from testfile tf,transactionstatus ts,attachmenttype at, linkmaster lm  "
	                + " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                + " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                + " and tf.ndefaultstatus=ts.ntranscode and ts.nstatus="
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.ntestcode=" + ntestcode
	                + " order by tf.ntestfilecode;";

			//final List<?> lstUTCConvertedDate = objGeneral.getSiteLocalTimeFromUTC(
					//jdbcTemplate.query(query, new TestFile()), Arrays.asList("screateddate"), null, objUserInfo, true,
					//Arrays.asList("stransdisplaystatus"),false);
			//ObjectMapper objMapper = new ObjectMapper();
			//outputMap.put("TestFile", objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<TestFile>>() {
		//	}));
		    outputMap.put("TestFile",  dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new TestFile()), Arrays.asList("screateddate"), null, objUserInfo, true,Arrays.asList("stransdisplaystatus"),false));	
		    return outputMap;
		}

		/**
		 * This method is used to fetch the list of test instrumentcategory
		 * 
		 * @param ntestcode   [int] holds the primary key of testmaster
		 * @param objUserInfo [UserInfo] holds the object of loggedin user info
		 * @return [Map] holds the list of test instrumentcategory
		 * @throws Exception
		 */
		public Map<String, Object> getTestInstrumentCategory(final int ntestcode, final UserInfo objUserInfo)
				throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();
			final String query = "select tc.ntestinstrumentcatcode,tc.ntestcode,tc.ninstrumentcatcode,tc.ndefaultstatus,"
					+ " ic.sinstrumentcatname,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from"
					+ " testinstrumentcategory tc,instrumentcategory ic,transactionstatus ts"
					+ " where tc.ninstrumentcatcode=ic.ninstrumentcatcode " + "and tc.ndefaultstatus=ts.ntranscode"
					+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.ntestcode=" + ntestcode
					+ " order by tc.ntestinstrumentcatcode;";
			
			outputMap.put("TestInstrumentCategory",
					jdbcTemplate.query(query,new TestInstrumentCategory()));
			return outputMap;
		}
		
		
		public Map<String, Object> getTestpackage(final int ntestcode, final UserInfo objUserInfo)
				throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();
			final String query = "select tc.ntestpackagetestcode,tc.ntestcode,tc.ntestpackagecode,tc.ndefaultstatus,"
					+ " ic.stestpackagename,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from"
					+ " testpackagetest tc,testpackage ic,transactionstatus ts"
					+ " where tc.ntestpackagecode=ic.ntestpackagecode " + "and tc.ndefaultstatus=ts.ntranscode"
					+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.ntestcode=" + ntestcode
					+ " order by tc.ntestpackagetestcode;";
			
			outputMap.put("TestPackage",jdbcTemplate.query(query,new TestPackageTest()));
			return outputMap;
		}
		

		public ResponseEntity<Object> getTestContainterType(final int ntestcode, final UserInfo objUserInfo) throws Exception {
			final Map<String, Object> outputMap = new HashMap<String, Object>();
			final String query = "select tc.ntestcode,tc.ncontainertypecode,tc.ntestcontainertypecode,tc.nquantity,tc.ndefaultstatus,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"') as stransdisplaystatus ,"
					+ " ct.scontainertype,tc.nunitcode, case when u.sunitname ='NA' then '-' else u.sunitname  end as sunitname from"
					+ " testcontainertype tc,containertype ct,transactionstatus ts,unit u "
					+ " where tc.ncontainertypecode=ct.ncontainertypecode " + "and tc.ndefaultstatus=ts.ntranscode and u.nunitcode=tc.nunitcode"
					+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tc.ntestcode=" + ntestcode;
			outputMap.put("Containertype",jdbcTemplate.query(query, new TestContainerType()));
			return new ResponseEntity<Object>(outputMap,HttpStatus.OK) ;
		}
		/**
		 * This method is used to fetch a list of testmaster based on test category
		 * 
		 * @param ntestCategoryCode [int] holds the primarykey of testcategory
		 * @param nmasterSiteCode   [int] holds the primarykey of loggedin user
		 *                          mastersite code
		 * @param ntestcode         [int] holds the primarykey of testmaster
		 * @param objUserInfo       [UserInfo] holds the object of loggedin user info
		 * @return [List] holds the list of testmaster details
		 * @throws Exception
		 */
		public List<TestMaster> getTestMaster(final int ntestCategoryCode, final int nmasterSiteCode, final int ntestcode,
				final UserInfo objUserInfo) throws Exception {
			String sQuery = "select t.ntestcode,t.ntatperiodcode, "
//					+ "UPPER(left(t.stestname,1))+UPPER(SUBSTRING(t.stestname,iif(charindex(' ',t.stestname,1)=0,0,charindex(' ',t.stestname,1)+1),1)) as stestshortname, "
					+ " t.nchecklistversioncode,t.stestname,t.stestsynonym,t.ncost,"
					+ " case when t.sshortname='' then '-'else coalesce(t.sshortname, '-') end as sshortname,"
					+ " case when t.sdescription='' then '-'else coalesce(t.sdescription, '-') end as sdescription,"
					+ " case when t.stestplatform='' then '-'else coalesce(t.stestplatform, '-') end as stestplatform,"
					+ " to_char(t.dmodifieddate, '"+objUserInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate, cl.schecklistname,"
					+ " t.ntransactionstatus,tc.stestcategoryname,"
					+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " ts1.jsondata->'stransdisplaystatus'->>'en-US') as saccredited, "
		            + " coalesce(ts2.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
					+ " t.naccredited,t.ntestcategorycode, "
					+ " coalesce(ts3.jsondata->'stransdisplaystatus'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " ts3.jsondata->'stransdisplaystatus'->>'en-US') as strainingneed, "
		            + " coalesce(p.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"',"
		            + " p.jsondata->'speriodname'->>'en-US') as statperiodname, t.ntat,"
		            + " t.ninterfacetypecode, coalesce(ift.jsondata->'sinterfacetypename'->>'"
		    		+ objUserInfo.getSlanguagetypecode() + "'," + " ift.jsondata->'sinterfacetypename'->>'en-US') "
		            + " as sinterfacetypename,tc.nclinicaltyperequired  "
					+ " from testmaster t,transactionstatus ts1,transactionstatus ts2,transactionstatus ts3,"
		            + " checklist cl,checklistversion clv,testcategory tc, period p, interfacetype ift "
					+ " where t.naccredited=ts1.ntranscode and t.ntransactionstatus=ts2.ntranscode and t.ntestcategorycode = tc.ntestcategorycode "
					+ " and clv.nchecklistversioncode = t.nchecklistversioncode "
					+ " and cl.nchecklistcode = clv.nchecklistcode "
					+ " and t.ntrainingneed = ts3.ntranscode "
					+ " and t.ntatperiodcode = p.nperiodcode "
					+ " and t.ninterfacetypecode = ift.ninterfacetypecode "
					+ " and t.ntestcode > 0 "
					+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and clv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ift.nstatus=" +  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
			if (ntestCategoryCode == -2) {
				sQuery = sQuery + " and t.nsitecode = " + nmasterSiteCode;
			} else if (ntestCategoryCode > 0) {
				sQuery = sQuery + " and t.ntestcategorycode = " + ntestCategoryCode;
			} else {
				sQuery = sQuery + " and t.ntestcode = " + ntestcode;
			}
			sQuery +=  " order by t.ntestcode";
			ObjectMapper objMapper = new ObjectMapper();
			List<TestMaster> listTestMaster = (List<TestMaster>) jdbcTemplate.query(sQuery, new TestMaster());
			return objMapper.convertValue(listTestMaster, new TypeReference<List<TestMaster>>() {
			});
		}

		/**
		 * This method is used to check whether the test name is already present or not.
		 * "Test Category" based duplicate "Test Name" validation removed.
		 * Test Name should be unique across the database
		 * as per common FRS-00251 
		 * 
		 * @param objTestMaster [TestMaster] holds the details of test master
		 * @param objUserInfo   [UserInfo] holds the details of loggedin user
		 * @return [TestMaster] holds the details of testmaster
		 * @throws Exception
		 */
		public TestMaster getTestByName(final TestMaster objTestMaster, final UserInfo objUserInfo) throws Exception {
			final String strQuery = "select ntestcode from testmaster where stestname=N'"
							+ stringUtilityFunction.replaceQuote(objTestMaster.getStestname()) + "'"
						//	+ " and ntestcategorycode=" + objTestMaster.getNtestcategorycode() //FRS-00251
							+ " and nsitecode = " + objUserInfo.getNmastersitecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			return (TestMaster) jdbcUtilityFunction.queryForObject(strQuery, TestMaster.class,jdbcTemplate); 
		}

		/**
		 * This method is used to fetch a testmaster details after add
		 * 
		 * @param objTestMaster [TestMaster] holds the details of test master
		 * @param isEditAction  holds the boolean value, If true - edit action, false -
		 *                      add action
		 * @param objUserInfo   [UserInfo] holds the details of loggedin user
		 * @return response entity holds the list of testmaster, selectedtest,
		 *         testparameter, selectedparameter and parameter details
		 * @throws Exception
		 */
		public ResponseEntity<Object> getTestMasterAfterInsert(final TestMaster objTestMaster, final boolean isEditAction,
				final UserInfo objUserInfo) throws Exception {
			final Map<String, Object> map = new LinkedHashMap<String, Object>();
			final List<TestMaster> lstTestMaster = getTestMaster(objTestMaster.getNtestcategorycode(), -1, 0, objUserInfo);
			map.put("TestMaster", lstTestMaster);
			final String query="select * from testcategory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ntestcategorycode="+objTestMaster.getNtestcategorycode();
			final TestCategory lstTestCat=(TestCategory) jdbcUtilityFunction.queryForObject(query, TestCategory.class,jdbcTemplate);
			
			if(lstTestCat!=null) {
				map.put("SelectedTestCat", lstTestCat);
			}else {
				map.put("SelectedTestCat", null);
			}
			if (!lstTestMaster.isEmpty()) {
				TestMaster selectedTest = new TestMaster();
				if (isEditAction) {
					selectedTest = lstTestMaster.stream()
							.filter(test -> test.getNtestcode() == objTestMaster.getNtestcode())
							.collect(Collectors.toList()).get(0);
				} else {
					selectedTest = lstTestMaster.get(lstTestMaster.size() - 1);
				}

				final List<TestParameter> lstTestParameter = getTestParameter(selectedTest.getNtestcode(), 0,objUserInfo);
				map.put("SelectedTest", selectedTest);
				map.put("TestParameter", lstTestParameter);
				map.putAll(getTestSection(selectedTest.getNtestcode(), objUserInfo));
				map.putAll(getTestMethod(selectedTest.getNtestcode(), objUserInfo));
				map.putAll(getTestFile(selectedTest.getNtestcode(), objUserInfo));
				map.putAll(getTestInstrumentCategory(selectedTest.getNtestcode(), objUserInfo));
				map.putAll(getTestpackage(selectedTest.getNtestcode(), objUserInfo));
				map.putAll((Map<String,Object>) getTestContainterType(selectedTest.getNtestcode(), objUserInfo).getBody());

				if (!lstTestParameter.isEmpty()) {
					map.putAll(getParameterDetailsById(lstTestParameter.get(lstTestParameter.size() - 1), objUserInfo));
				}
			}
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

		/**
		 * This method is ued to fetch a list of test based on test category while add
		 * formula
		 * 
		 * @param ntestCategoryCode [int] holds the primary key of test category
		 * @return [List] holds the list of testmaster
		 * @throws Exception
		 */
		
		public List<TestMaster> getTestForFormula(final int ntestCategoryCode) throws Exception {
			final String sQuery = "select distinct tc.ntestcategorycode,tm.ntestcode,tm.stestname,tm.stestsynonym"
					+ " from testmaster tm ,testcategory tc,testparameter tp"
					+ " where tm.ntestcode=tp.ntestcode and tm.ntestcategorycode=tc.ntestcategorycode"
					+ " and tc.ntestcategorycode = " + ntestCategoryCode + " and tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nparametertypecode= "
					+ Enumeration.ParameterType.NUMERIC.getparametertype() + " order by ntestcode desc";
			return (List<TestMaster>) jdbcTemplate.query(sQuery, new TestMaster());
		}

		/**
		 * This method is used to fetch a list of formula fields
		 * 
		 * @param ntestCode [int] holds the primarykey of testmaster
		 * @return [List] holds the list of test parameter details as formula fields
		 * @throws Exception
		 */
		
		public List<TestParameter> getTestParameterForFormula(final int ntestCode,UserInfo userInfo) throws Exception {
			final String sQuery = "select tp.sparametername as sparametername , t.stestname as stestname,t.ntestcode,tp.ntestparametercode,"
					+ "'' as sformulacalculationcode,2 as isformula "
					+ " from testparameter tp,testmaster t,unit u,parametertype pt where tp.ntestcode = t.ntestcode and tp.nunitcode = u.nunitcode"
					+ " and tp.nparametertypecode=pt.nparametertypecode and t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.ntestcode = " + ntestCode
					+ " and tp.nparametertypecode = " + Enumeration.ParameterType.NUMERIC.getparametertype() + " union all"
					+ " select sdescription as stestname,coalesce(jsondata->'sdisplayname'->>'"+ userInfo.getSlanguagetypecode()+ "', jsondata->'sdisplayname'->>'en-US')  as sparametername," + ntestCode
					+ " as ntestcode,"
					+ " ndynamicformulafieldcode as ntestparametercode,'' as sformulacalculationcode,3 as isformula"
					+ " from dynamicformulafields where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select case when tf.sformulacalculationcode='' then '' else 'Formula-Field' end stestname,"
					+ " tf.sformulacalculationdetail  as sparametername,tf.ntestcode,tf.ntestparametercode,"
					+ " tf.sformulacalculationcode ,1 as isformula "
					+ " from testformula tf,transactionstatus ts,testmaster tm"
					+ " where tf.ndefaultstatus=ts.ntranscode and tf.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tf.ntestcode = tm.ntestcode and tm.ntestcode = " + ntestCode;
			return (List<TestParameter>) jdbcTemplate.query(sQuery, new TestParameter());
		}
		
		
		

		
		public List<TestMasterClinicalSpec> getTestParameterClinialSpec(final int ntestparametercode,final UserInfo objUserInfo) throws Exception {
			final String sQuery = "select coalesce(g.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + 
					"',g.jsondata->'sdisplayname'->>'en-US') as sgradename,coalesce(gg.jsondata->'sgendername'->>'" + objUserInfo.getSlanguagetypecode()+"',gg.jsondata->'sgendername'->>'en-US') as sgendername"
					+ " ,ntestmasterclinicspeccode, ntestparametercode, COALESCE(sminlod, '-') sminlod,"
					+ "COALESCE(smaxlod, '-') smaxlod, COALESCE(shighb, '-') shighb, COALESCE(shigha, '-') shigha, COALESCE(slowa, '-') slowa,"
					+ "COALESCE(slowb, '-') slowb, COALESCE(sminloq, '-') sminloq, COALESCE(smaxloq, '-') smaxloq, COALESCE(sdisregard, '-') sdisregard,"
					+ "COALESCE(sresultvalue, '-') sresultvalue, tpn.nstatus ,tpn.ngradecode,tpn.ngendercode ,tpn.nfromage,tpn.ntoage,"
					+ " tpn.ntoageperiod,tpn.nfromageperiod,tpn.ntoageperiod,"
					+ " COALESCE(p1.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"', p1.jsondata->'speriodname'->>'en-US')  AS sfromageperiod, "
					+"  COALESCE(p2.jsondata->'speriodname'->>'"+objUserInfo.getSlanguagetypecode()+"', p2.jsondata->'speriodname'->>'en-US')  AS stoageperiod "
					+ " from testmasterclinicalspec tpn,grade g,gender gg,period p1,period p2 where ntestparametercode = " + ntestparametercode + " and  "
					+ " p1.nperiodcode=tpn.nfromageperiod and p2.nperiodcode=tpn.ntoageperiod and g.ngradecode=tpn.ngradecode and  gg.ngendercode=tpn.ngendercode "
					+ " and p1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and p2.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and g.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpn.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			return (List<TestMasterClinicalSpec>) jdbcTemplate.query(sQuery, new TestMasterClinicalSpec());
		}
		

	  

}
