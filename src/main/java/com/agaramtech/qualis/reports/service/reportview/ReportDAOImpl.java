package com.agaramtech.qualis.reports.service.reportview;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportModule;
import com.agaramtech.qualis.reports.model.ReportParameter;
import com.agaramtech.qualis.reports.model.ReportParameterMapping;
import com.agaramtech.qualis.reports.service.reportconfig.ReportMasterDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;



@AllArgsConstructor
@Repository
public class ReportDAOImpl implements ReportDAO{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final ReportMasterDAO reportMasterDAO;
	private final CommonFunction commonFunction;
	private final ReportDAOSupport reportDAOSupport;
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getReportView(final Integer reportModuleCode, final UserInfo userInfo) throws Exception
	{
		final Map<String, Object> outputMap = new HashMap<String, Object>();	
		ReportModule reportModule = null;
		if (reportModuleCode == null) {
			 List<ReportModule> moduleList = (List<ReportModule>)reportMasterDAO.getReportModule(userInfo).getBody();
			if (!moduleList.isEmpty()) {
				reportModule = moduleList.get(0);
			}
			outputMap.put("ViewReportModuleList", moduleList);
		}
		else {
			final String queryString = "select *,coalesce(jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',jsondata->'sdisplayname'->>'en-US') as sdisplayname from reportmodule where nreportmodulecode ="+ reportModuleCode
							+ "  and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			 List<ReportModule> lstReportModule=  jdbcTemplate.query(queryString, new ReportModule());
			reportModule=lstReportModule.get(0);
			
		}
		outputMap.put("SelectedReportModule", reportModule);
		
		if (reportModule == null) {			
			outputMap.put("ViewReportMaster", new ArrayList<>());
		}
		else {
			final String query = " select rm.*, rd.nversionno, rd.ntransactionstatus, ret.sreporttypename, "
							   + " coalesce(ret.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',ret.jsondata->'sdisplayname'->>'en-US') as sreportdisplayname,"
							   + " coalesce(rt.jsondata->'sregtypename'->>'"+userInfo.getSlanguagetypecode()+"',rt.jsondata->'sregtypename'->>'en-US') as sregtypename, "
									   + " coalesce(rst.jsondata->'sregsubtypename'->>'"+userInfo.getSlanguagetypecode()+"',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename, "
							   + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus, rmd.sreportmodulename, " 
							   + " coalesce(rmd.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',rmd.jsondata->'sdisplayname'->>'en-US') as smoduledisplayname from reportmaster rm, reportdetails rd, reportrights rr, registrationtype rt, "
							   + " registrationsubtype rst, reporttype ret, reportmodule rmd, transactionstatus ts " 
							   + " where rm.nreporttypecode=ret.nreporttypecode "
							   + " and rr.nreportcode = rm.nreportcode"
							   + " and rm.nregtypecode=rt.nregtypecode and rm.nregsubtypecode=rst.nregsubtypecode " 
							   + " and rm.nreportmodulecode=rmd.nreportmodulecode " 
							   + " and rm.nreportcode = rd.nreportcode "
							   + " and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							   + " and rm.ntransactionstatus=ts.ntranscode " 
							   + " and rmd.nstatus = rr.nstatus "
							   + " and rr.nstatus=rt.nstatus "
							   + " and rt.nstatus=rst.nstatus "
							   + " and rst.nstatus=ret.nstatus "
							   + " and ret.nstatus=ts.nstatus"
							   + " and ts.nstatus = rm.nstatus"
							   + " and rm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and rm.nsitecode =" +userInfo.getNmastersitecode() 
							   + " and rm.ntransactionstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and rm.nreportmodulecode =" +reportModule.getNreportmodulecode()
							   + " and rr.nuserrolecode = " + userInfo.getNuserrole()
							   + " order by nreportcode asc";
			List<ReportMaster> reportList = jdbcTemplate.query(query, new ReportMaster());			

			outputMap.put("ViewReportMaster", reportList);
			
			if (!reportList.isEmpty()) {
				outputMap.put("SelectedViewReportMaster", reportList.get(reportList.size()-1));
				
				outputMap.putAll((Map<String, Object>) viewReport(reportList.get(reportList.size()-1), userInfo).getBody());
			}
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getStimulsoftView(final Integer reportModuleCode, final UserInfo userInfo) throws Exception
	{
		final Map<String, Object> outputMap = new HashMap<String, Object>();	
		ReportModule reportModule = null;
		if (reportModuleCode == null) {
			 List<ReportModule> moduleList = (List<ReportModule>)reportMasterDAO.getReportModule(userInfo).getBody();
			if (!moduleList.isEmpty()) {
				reportModule = moduleList.get(0);
			}
			outputMap.put("ViewReportModuleList", moduleList);
		}
		else {
			final String queryString = "select *,coalesce(jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',jsondata->'sdisplayname'->>'en-US') as sdisplayname from reportmodule where nreportmodulecode ="+ reportModuleCode
							+ "  and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			 List<ReportModule> lstReportModule=  jdbcTemplate.query(queryString, new ReportModule());
			reportModule=lstReportModule.get(0);
			
		}
		outputMap.put("SelectedReportModule", reportModule);
		
		if (reportModule == null) {			
			outputMap.put("ViewReportMaster", new ArrayList<>());
		}
		else {
			final String query = " select rm.*, rd.nversionno, rd.ntransactionstatus, ret.sreporttypename, "
							   + " coalesce(ret.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',ret.jsondata->'sdisplayname'->>'en-US') as sreportdisplayname,"
							   + " coalesce(rt.jsondata->'sregtypename'->>'"+userInfo.getSlanguagetypecode()+"',rt.jsondata->'sregtypename'->>'en-US') as sregtypename, "
									   + " coalesce(rst.jsondata->'sregsubtypename'->>'"+userInfo.getSlanguagetypecode()+"',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename, "
							   + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as sactivestatus, rmd.sreportmodulename, " 
							   + " coalesce(rmd.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',rmd.jsondata->'sdisplayname'->>'en-US') as smoduledisplayname from reportmaster rm, reportdetails rd, reportrights rr, registrationtype rt, "
							   + " registrationsubtype rst, reporttype ret, reportmodule rmd, transactionstatus ts " 
							   + " where rm.nreporttypecode=ret.nreporttypecode "
							   + " and rr.nreportcode = rm.nreportcode"
							   + " and rm.nregtypecode=rt.nregtypecode and rm.nregsubtypecode=rst.nregsubtypecode " 
							   + " and rm.nreportmodulecode=rmd.nreportmodulecode " 
							   + " and rm.nreportcode = rd.nreportcode "
							   + " and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							   + " and rm.ntransactionstatus=ts.ntranscode " 
							   + " and rmd.nstatus = rr.nstatus "
							   + " and rr.nstatus=rt.nstatus "
							   + " and rt.nstatus=rst.nstatus "
							   + " and rst.nstatus=ret.nstatus "
							   + " and ret.nstatus=ts.nstatus"
							   + " and ts.nstatus = rm.nstatus"
							   + " and rm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and rm.nsitecode =" +userInfo.getNmastersitecode() 
							   + " and rm.ntransactionstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and rm.nreportmodulecode =" +reportModule.getNreportmodulecode()
							   + " and rr.nuserrolecode = " + userInfo.getNuserrole()
							   + " order by nreportcode asc";
			List<ReportMaster> reportList = jdbcTemplate.query(query, new ReportMaster());			

			outputMap.put("ViewReportMaster", reportList);
			
			if (!reportList.isEmpty()) {
				outputMap.put("SelectedViewReportMaster", reportList.get(reportList.size()-1));
				
				outputMap.putAll((Map<String, Object>) viewReport(reportList.get(reportList.size()-1), userInfo).getBody());
			}
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> searchViewReport(final int nreportCode, final UserInfo userInfo) throws Exception{
	//ALPD-2449 janakumar r
		final String query = "select rm.*,rd.nversionno,   "
				+ "rd.ntransactionstatus,ret.sreporttypename,"
				+ "ret.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"' AS sreportdisplayname,  "
				+ "rt.jsondata->'sregtypename'->>'"+userInfo.getSlanguagetypecode()+"' AS sregtypename,  "
				+ "COALESCE(rt.jsondata->'stransdisplaystatus'->>' "+userInfo.getSlanguagetypecode()+"', 'NA') AS sactivestatu,   "
				+ "rst.jsondata->'sregsubtypename'->>' "+userInfo.getSlanguagetypecode()+"' AS sregsubtypename,  "
				+ "rmd.sreportmodulename,rmd.jsondata->'sdisplayname'->>' "+userInfo.getSlanguagetypecode()+"' AS smoduledisplayname  "
				+ "from reportmaster rm, reportdetails rd, reportrights rr, registrationtype rt, "
				+ "registrationsubtype rst, reporttype ret, reportmodule rmd, transactionstatus ts " 
				+ "where rm.nreporttypecode=ret.nreporttypecode "
				+ "and rr.nreportcode = rm.nreportcode  "
				+ "and rm.nregtypecode=rt.nregtypecode and rm.nregsubtypecode=rst.nregsubtypecode " 
				+ "and rm.nreportmodulecode=rmd.nreportmodulecode " 
				+ "and rm.nreportcode = rd.nreportcode "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "and rm.ntransactionstatus=ts.ntranscode  " 
				+ "and rmd.nstatus = rr.nstatus  "
				+ "and rr.nstatus=rt.nstatus  "
				+ "and rt.nstatus=rst.nstatus  "
				+ "and rst.nstatus=ret.nstatus  "
				+ "and ret.nstatus=ts.nstatus  "
				+ "and ts.nstatus = rm.nstatus  "
				+ "and rm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ "and rm.nsitecode =" +userInfo.getNmastersitecode() 
				+ "and rm.ntransactionstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ "and rm.nreportcode =" + nreportCode
				+ "and rr.nuserrolecode = " + userInfo.getNuserrole();	

		final ReportMaster master = (ReportMaster) jdbcTemplate.queryForObject(query, new  ReportMaster());
		
		return viewReport(master, userInfo);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> viewReport1(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception{
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();	
		
		String query = "select rd.*, rm.* from "
				+ " reportmaster rm, reportdetails rd "
				+ " where rd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nreportcode=rd.nreportcode "
				+ " and rd.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rm.nreportcode="+ reportMaster.getNreportcode();
		
		final ReportDetails reportDetails = (ReportDetails) jdbcTemplate.queryForObject(query, new ReportDetails());
		
		outputMap.put("SelectedReportDetails", reportDetails);
		outputMap.put("SelectedViewReportMaster", reportMaster);
		if(reportDetails != null)
		{				
			final Map<String, Object> designMap = (Map<String, Object>) reportMasterDAO.getReportParameter(reportDetails.getNreportdetailcode(), userInfo).getBody();
			final List<ReportParameter> parameterList= (List<ReportParameter>) designMap.get("ReportParameter");
			
			if (parameterList.isEmpty()) {
					String attachedFile = null;
					final Map<String,Object> fileMap = ftpUtilityFunction.FileViewUsingFtp(reportDetails.getSsystemfilename(), -1, userInfo, "", "");
					attachedFile = (String) fileMap.get("AttachFile");			
					
					final String outputFile = userInfo.getSloginid()+"\\"+UUID.randomUUID().toString()+"_"+(String)reportDetails.getSreportname()+".pdf";				
					
					//ALPD-4436 
					//To get path value from system's environment variables instead of absolutepath
					final String homePath=ftpUtilityFunction.getFileAbsolutePath();
					final String absolutePath1 = System.getenv(homePath)//new File("").getAbsolutePath() 
							+ Enumeration.FTP.UPLOAD_PATH.getFTP()+"\\";
					//File file1 = new File(absolutePath1);
					final String path = absolutePath1 + attachedFile;
					
					final Path path1 = Paths.get(path);					
					final InputStream reportFile = Files.newInputStream(path1);
					
					final Map<String,Object> jasperParameter = new HashMap<String,Object>();
					
					final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);
					jasperParameter.put("REPORT_RESOURCE_BUNDLE", resourcebundle);
					final Connection connection = jdbcTemplate.getDataSource().getConnection();
					
					//need to discuss,check and change for below   jasperDesign,JasperPrint,JasperReport ,JasperExportManager,JasperCompileManager,JasperFillManager,JRXmlLoader
//					final JasperDesign jasperDesign = JRXmlLoader.load(reportFile);
//					jasperDesign.setLanguage("java");
//					final JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//					final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParameter, connection);	
//					final String sDestinationFile = absolutePath1 + outputFile;
//					JasperExportManager.exportReportToPdfFile(jasperPrint, sDestinationFile); 						   
						
					
					outputMap.put("ReportPDFFile", outputFile);
			}
			else {
				 
				final Map<String, Object> configMap = (Map<String, Object>) reportMasterDAO.getReportDesignConfig(reportDetails.getNreportdetailcode(), userInfo).getBody();
				final List<ReportDesignConfig> configList =  (List<ReportDesignConfig>)configMap.get("ReportDesignConfig");
							
				final StringBuffer queryBuffer = new StringBuffer();
				final List<String> fieldList = new ArrayList<String>();
				
				final Map<String, String> dateFieldMap = new HashMap<String,String>();
				
				for(ReportDesignConfig designConfig : configList) 
				{						
					if (designConfig.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD.gettype()) {
												
						Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo);//objGeneral.getUTCDateTime();					
						if (designConfig.getNdays() >= 0) {
							instantDate = instantDate.plus(designConfig.getNdays(), ChronoUnit.DAYS);
						}
						else {
							instantDate = instantDate.minus(Math.abs(designConfig.getNdays()), ChronoUnit.DAYS);
						}
						
						final String stringDate = dateUtilityFunction.convertUtcToLocalSiteTime(instantDate, userInfo);
						designConfig.setDataList(Arrays.asList(stringDate));
						dateFieldMap.put(designConfig.getSreportparametername(), stringDate);
					}
				}
				
				for(ReportDesignConfig designConfig : configList) 
				{								
					if (designConfig.getNsqlquerycode() != -1) 
					{
						if (designConfig.getSsqlquery().contains("<@")) {
							String sqlQuery = designConfig.getSsqlquery();
							for(Map.Entry<String, String> dateField : dateFieldMap.entrySet()) {
								sqlQuery = sqlQuery.replace("<@"+dateField.getKey()+"@>","'"+dateField.getValue()+"'");
							}	
							if (!sqlQuery.contains("<@")) {
								queryBuffer.append(sqlQuery+";");	
								fieldList.add(designConfig.getSdisplayname());
							}
						}
						else {
							queryBuffer.append(designConfig.getSsqlquery()+";");	
							fieldList.add(designConfig.getSdisplayname());
						}
					}
				}				

				//need to discuss,check and change for below  findByMultiQueryPlainSql method 
				final Map<String, Object> queryMap = new HashMap<>();
						//(Map<String, Object>)findByMultiQueryPlainSql(queryBuffer.toString(),  fieldList.toArray());
			
				for(ReportDesignConfig designConfig : configList) {
			
					if (fieldList.contains(designConfig.getSdisplayname())) {
						designConfig.setDataList((List<?>)queryMap.get(designConfig.getSdisplayname()));
					}
				}				
				outputMap.put("ViewReportDesignConfig", configMap.get("ReportDesignConfig"));
				outputMap.put("ReportPDFFile", "");
			}
		}			
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> getChildDataList(final ReportDesignConfig parentConfig,final Map<String, Object> inputFieldData, final String parentValue, final UserInfo userInfo) throws Exception{
		final String query = " select rpm.*, rdc.sdisplayname as schildparametername, rpm.sfieldname as sparentparametername ,"
							+ " sq.ssqlquery from reportparametermapping rpm, "
							+ " reportdesignconfig rdc,sqlquery sq where "
							+ "	rpm.nchildreportdesigncode = rdc.nreportdesigncode "
							+ " and rdc.nsqlquerycode = sq.nsqlquerycode and rpm.nstatus = rdc.nstatus "
							+ " and rdc.nstatus = sq.nstatus and sq.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "	and rpm.nreportdetailcode=" + parentConfig.getNreportdetailcode()
							+ " and rpm.nisactionparent = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and rpm.nparentreportdesigncode=" + parentConfig.getNreportdesigncode();
						
		final List<ReportParameterMapping> actionList = jdbcTemplate.query(query, new ReportParameterMapping());
		

		Map<String, Object> outputMap = new HashMap<String, Object>();
		
		final StringBuffer queryBuffer = new StringBuffer();
		final List<String> childFieldList = new ArrayList<String>();
		
		for(ReportParameterMapping parameterMapping : actionList) {
			
			//final StringBuffer queryBuffer = new StringBuffer();
		
			final String sqlquery = parameterMapping.getSsqlquery();
			queryBuffer.append(sqlquery+";");
			childFieldList.add(parameterMapping.getSchildparametername());
			
			int count = StringUtils.countMatches(sqlquery, "<@");
			int count1 = StringUtils.countMatches(sqlquery, "<#");
			if (count > 1 || count1>1) {
				for(Map.Entry<String, Object> entrySet : inputFieldData.entrySet()) {
					String key = entrySet.getKey();
					String fieldValue = "";
					if(sqlquery.contains("<@".concat(key.concat("@>")))) {
					if (!entrySet.getKey().endsWith("_componentcode") && !entrySet.getKey().endsWith("_componentname")) {
						if (inputFieldData.get(entrySet.getKey().concat("_componentcode")) != null && (Integer)inputFieldData.get(entrySet.getKey().concat("_componentcode")) == Enumeration.DesignComponent.DATEFEILD.gettype())
						{
		
							Instant instantDate = dateUtilityFunction.convertStringDateToUTC((String)inputFieldData.get(entrySet.getKey()),
									userInfo, false);;
							
							final String dateValue = dateUtilityFunction.instantDateToString(instantDate);
							fieldValue = "'" + dateValue + "'";
						}
						else {
							//fieldValue = Integer.toString((Integer)entrySet.getValue());
							if (entrySet.getValue() instanceof Integer )
							{
								fieldValue =  Integer.toString((Integer)entrySet.getValue()) ;
							}
							else {
								fieldValue =  (String)entrySet.getValue() ;
							}
						}
						final String parameterName = "<@".concat(key.concat("@>"));
						if (queryBuffer.indexOf(parameterName) != -1) {
							queryBuffer.replace(queryBuffer.indexOf(parameterName), 
												(queryBuffer.indexOf(parameterName)+parameterName.length()),fieldValue);
						}
					}
				}else if(sqlquery.contains("<#".concat(key.concat("#>")))) {
					if (!entrySet.getKey().endsWith("_componentcode") && !entrySet.getKey().endsWith("_componentname")) {
						if (inputFieldData.get(entrySet.getKey().concat("_componentcode")) != null && (Integer)inputFieldData.get(entrySet.getKey().concat("_componentcode")) == Enumeration.DesignComponent.DATEFEILD.gettype())
						{
		
							Instant instantDate = dateUtilityFunction.convertStringDateToUTC((String)inputFieldData.get(entrySet.getKey()),
									userInfo, false);;
							
							final String dateValue = dateUtilityFunction.instantDateToString(instantDate);
							fieldValue = "'" + dateValue + "'";
						}
						else {
							//fieldValue = Integer.toString((Integer)entrySet.getValue());
							if (entrySet.getValue() instanceof Integer )
							{
								fieldValue =  Integer.toString((Integer)entrySet.getValue()) ;
							}
							else {
								fieldValue =  (String)entrySet.getValue() ;
							}
						}
						final String parameterName = "<#".concat(key.concat("#>"));
						if (queryBuffer.indexOf(parameterName) != -1) {
							queryBuffer.replace(queryBuffer.indexOf(parameterName), 
												(queryBuffer.indexOf(parameterName)+parameterName.length()),fieldValue);
						}
					}
				}	
				}
			}
			else {
					String fieldValue = parentValue;
					if (parentConfig.getNdesigncomponentcode()== Enumeration.DesignComponent.DATEFEILD.gettype()) {
											
						//Convert Input Date to UTC based on timezone from UserInfo
						Instant instantDate = dateUtilityFunction.convertStringDateToUTC(parentValue, 
								userInfo, false);
					
						fieldValue = "'" + dateUtilityFunction.instantDateToString(instantDate) + "'";
					}
					final String parameterName = "<@".concat(parameterMapping.getSparentparametername().concat("@>"));
					if(queryBuffer.indexOf(parameterName)>0) {

						queryBuffer.replace(queryBuffer.indexOf(parameterName), 
											(queryBuffer.indexOf(parameterName)+parameterName.length()),fieldValue);
						
					}else if(queryBuffer.indexOf("<#".concat(parameterMapping.getSparentparametername().concat("#>")))>0){
						final String parameterName1 = "<#".concat(parameterMapping.getSparentparametername().concat("#>"));
						queryBuffer.replace(queryBuffer.indexOf(parameterName1), 
								(queryBuffer.indexOf(parameterName1)+parameterName1.length()),fieldValue);
					}
			}
			
		}
		
		if (queryBuffer.length() > 0) {
			
			//need to discuss,check and change for below  findByMultiQueryPlainSql method 
			final Map<String, Object> defaultObject = new HashMap<>();
					//(Map<String, Object>) findByMultiQueryPlainSql(queryBuffer.toString(),  childFieldList.toArray());
			final ObjectMapper objMapper = new ObjectMapper();
			
			for(String fieldName : childFieldList) 
			{
				List<?> list = (List<?>)defaultObject.get(fieldName);
				
				if (list != null &&list.size() > 0) {
					Map<String, Object> mapcolumnfileds = objMapper.convertValue(list.get(0), Map.class);
					List<String> multilingualColumnList = new ArrayList<String>();
					
					for (Map.Entry<String, Object> entry : mapcolumnfileds.entrySet()) {
						if (entry.getValue() != null) {
							if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("String")
									&& entry.getValue().toString().length() > 4
									&& entry.getValue().toString().substring(0, 4).equals("IDS_")) {
								multilingualColumnList.add(entry.getKey());
							}					
						}
					}						
				
					list = objMapper.convertValue(commonFunction.getMultilingualMessageList(list, 
							multilingualColumnList, userInfo.getSlanguagefilename()), new TypeReference<List<Map<String,Object>>>() {
							});
				}
//				if (list != null)
//				{
//					defaultObject.put(fieldName, list);
//				}	
				outputMap.put(fieldName, list);
			}
		}	
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	
	@SuppressWarnings({ "unchecked", "unused"})
	public ResponseEntity<Object> viewReportWithParameters(final ReportMaster reportMaster,final Map<String, Object> inputFieldData, final UserInfo userInfo) throws Exception{
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();	
		
		String query = "select rd.*, rm.sreportname from "
				+ " reportmaster rm, reportdetails rd "
				+ " where rd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nreportcode=rd.nreportcode "
				+ " and rd.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rm.nreportcode="+ reportMaster.getNreportcode();
		
		final ReportDetails reportDetails = jdbcTemplate.queryForObject(query, new ReportDetails());
		outputMap.put("SelectedReportDetails", reportDetails);
		outputMap.put("SelectedViewReportMaster", reportMaster);
		if(reportDetails != null)
		{				
			final Map<String, Object> designMap = (Map<String, Object>) reportMasterDAO.getReportParameter(reportDetails.getNreportdetailcode(), userInfo).getBody();
			final List<ReportParameter> parameterList= (List<ReportParameter>) designMap.get("ReportParameter");
				//ALPD-4436
			//final String homePath=getFileAbsolutePath();
//			String absolutePath1 = System.getenv(homePath)//new File("").getAbsolutePath() 
			//+ Enumeration.FTP.UPLOAD_PATH.getFTP()+"\\";
//						
//			String attachedFile = null;
//			final Map<String,Object> fileMap = FileViewUsingFtp(reportDetails.getSsystemfilename(), -1, userInfo, "", "");
//			attachedFile = (String) fileMap.get("AttachFile");			
//			
//			String outputFile = userInfo.getSloginid()+"\\"+UUID.randomUUID().toString()+"_"+(String)reportDetails.getSreportname()+".pdf";				
//			
//			String path = absolutePath1 + attachedFile;
//					
//			Path path1 = Paths.get(path);					
//			InputStream reportFile = Files.newInputStream(path1);
//			JRProperties.setProperty( JRQueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX+"plsql","com.jaspersoft.jrx.query.PlSqlQueryExecuterFactory");
//			
//			Map<String,Object> jasperParameter = new HashMap<String,Object>();
//			
//			ResourceBundle resourcebundle = new PropertyResourceBundle(
//							new InputStreamReader(getClass().getClassLoader().getResourceAsStream("/com/properties/"+userInfo.getSlanguagefilename()+".properties"), "UTF-8"));
//			jasperParameter.put("REPORT_RESOURCE_BUNDLE", resourcebundle);
//			
//			final List<SubReportDetails> subReportList = (List<SubReportDetails>)((Map<String, Object>) reportMasterDAO.getSubReportDetails(reportDetails.getNreportdetailcode()).getBody()).get("SubReportDetails");	
//			
//			if (!subReportList.isEmpty()) {
//				String subReportFile = null;
//				final Map<String,Object> subReportMap = FileViewUsingFtp(subReportList.get(0).getSsystemfilename(), -1, userInfo, "", "");
//				subReportFile = (String) subReportMap.get("AttachFile");
//				
//				 JasperReport jasperSubReport = JasperCompileManager
//				            .compileReport(absolutePath1+subReportFile);
//				 
//				 jasperParameter.put("subreportParameter", jasperSubReport);
//			}	
			
			
			Map<String, Object> inputMap = new HashMap<String, Object>();
			
			int nregTypeCode = reportMaster.getNregtypecode();
			int nregSubTypeCode = reportMaster.getNregsubtypecode();
			int nreportDetailCode = reportDetails.getNreportdetailcode();
			int ncoareporttypecode = reportMaster.getNcoareporttypecode();
			
			
			inputMap.put("nreporttypecode", reportMaster.getNreporttypecode());
			inputMap.put("ncoareporttypecode",ncoareporttypecode);
			inputMap.put("nregtypecode", nregTypeCode);
			inputMap.put("nregsubtypecode", nregSubTypeCode);
			inputMap.put("nsectioncode",reportMaster.getNsectioncode());
			inputMap.put("ncertificatetypecode",reportMaster.getNcertificatetypecode());
			inputMap.put("ndecisionstatus",reportMaster.getNreportdecisiontypecode());
			inputMap.put("ncontrolcode", reportMaster.getNcontrolcode());
			inputMap.put("nreportdetailcode", nreportDetailCode);
			
			Map<String,Object> jasperParameter = new HashMap<String,Object>();
			
			for(Map.Entry<String, Object> fieldMap:inputFieldData.entrySet())						
			{	
				//System.out.println("field key:"+ fieldMap.getKey());
				if (!fieldMap.getKey().endsWith("_componentcode") && !fieldMap.getKey().endsWith("_componentname"))
				{
					if (inputFieldData.get(fieldMap.getKey().concat("_componentcode")) != null) {
						if ( (Integer)inputFieldData.get(fieldMap.getKey().concat("_componentcode")) == Enumeration.DesignComponent.DATEFEILD.gettype()) {
							
							//Convert Input Date to UTC based on timezone from UserInfo
							final String dateValue = dateUtilityFunction.instantDateToString(
									dateUtilityFunction.convertStringDateToUTC((String)fieldMap.getValue(), 
											userInfo, false));
							jasperParameter.put(fieldMap.getKey(), dateValue.replace("T"," "));
							
							
						}
						else {
							if (fieldMap.getKey().startsWith("n") && !(fieldMap.getValue() instanceof Integer)) {
								fieldMap.setValue(Integer.parseInt((String)fieldMap.getValue()));
							}
							
							jasperParameter.put(fieldMap.getKey(),fieldMap.getValue());
						}
					}
				}
				//System.out.println("field key:"+ fieldMap.getKey() +  "end--------------");
			}
			
			
			String sfileName = "";
			String sJRXMLname = "";
			int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
			int ncertificateTypeCode = -1;
			String sreportComments = "";
			int qType = 1;
			int ncontrolCode=-1;		
			String systemFileName="";			
			String sfilesharedfolder = "";
			String fileuploadpath = "";
			String subReportPath = "";
			String imagePath = "";
			String pdfPath = "";

			//ALPD-3633 properly set the pdf path and report path 
			final String getFileuploadpath = "select nreportsettingcode,ssettingvalue from reportsettings where nreportsettingcode in ("+Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode()+","+Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()+")";
			
			//final List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
			
			List<ReportSettings> lstreportPath = jdbcTemplate.query(getFileuploadpath, new ReportSettings());
			Map<Short, String> reportPath = lstreportPath.stream().collect(
					Collectors.toMap(ReportSettings::getNreportsettingcode, ReportSettings::getSsettingvalue));
			fileuploadpath = reportPath.get((short)1);
			subReportPath = reportPath.get((short)1);
			imagePath = reportPath.get((short)1);
			pdfPath = reportPath.get((short)4);
			UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
			userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
			final Map<String,Object> dynamicReport= reportDAOSupport.getDynamicReports(inputMap, userInfoWithReportFormCode);
			String folderName = "";
			if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals((String)dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))){
				
				sJRXMLname=(String) dynamicReport.get("JRXMLname");
				nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
				folderName = (String) dynamicReport.get("folderName");
				imagePath = subReportPath;
				fileuploadpath= fileuploadpath+ folderName;
			}
			
			sfilesharedfolder = fileuploadpath + sJRXMLname;
			File JRXMLFile = new File(sfilesharedfolder);
			
			if (sJRXMLname != null && !sJRXMLname.equals("") && JRXMLFile.exists()) 
			{	
				//String sImageName= jdbcTemplate.queryForObject("select sfilename from reportimages "
				//		+ " where nreportdetailcode="+nreportDetailCode+" and nreportcode="+reportDetails.getNreportcode(), String.class);
				//Map<String, Object> jasperParameter = new HashMap<String, Object>();
				jasperParameter.put("ssubreportpath", subReportPath + folderName);
				//jasperParameter.put("simagename",sImageName);
				jasperParameter.put("simagepath", imagePath + folderName);
				jasperParameter.put("language",userInfo.getSlanguagetypecode());
				//jasperParameter.put("pversionno", sversionno);
				///jasperParameter.put("pcertificatename", scertificateType);
				jasperParameter.put("certificatetypecode", ncertificateTypeCode);
				//jasperParameter.put((String) inputMap.get("sprimarykeyname"), inputMap.get("nprimarykey"));
				jasperParameter.put("needlogo", naccredit);
				jasperParameter.put("ndecisionstatus", inputMap.get("ndecisionstatus"));
				//jasperParameter.put("nregtypecode", nregTypeCode);
				//jasperParameter.put("nregsubtypecode", nregSubTypeCode);
				jasperParameter.put("sreportcomments", sreportComments);
				jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
				jasperParameter.put("nreportdetailcode", reportDetails.getNreportdetailcode());
				jasperParameter.put("sprimarykeyname", inputFieldData.get("sprimarykeyname"));
				
				int controlcode;
				if( inputMap.get("nreporttypecode").equals(Enumeration.ReportType.MIS.getReporttype()))
				{
					controlcode=-1;
				}else {
					controlcode=(int) inputFieldData.get("ncontrolcode");
				}

				outputMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName, userInfo,systemFileName, controlcode, false, "","",""));
				
				String uploadStatus = (String)outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)){
					outputMap.put("ReportPDFPath", pdfPath);
					outputMap.put("ReportPDFFile", outputMap.get("outputFilename"));
				}
			
			}	
		
			
			
//			final Connection connection = jdbcTemplate.getDataSource().getConnection();
//			
//			JasperDesign jasperDesign = JRXmlLoader.load(reportFile);
//			jasperDesign.setLanguage("java");
//			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParameter, connection);	
//			String sDestinationFile = absolutePath1+outputFile;
//			
//			JasperExportManager.exportReportToPdfFile(jasperPrint, sDestinationFile); 	   
//			
//			//JasperViewer.viewReport(jasperPrint, false);
//			
//			//System.out.println("view report");
//			
//			outputMap.put("ReportPDFFile", outputFile);		
		}			
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> viewReport(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception
	{
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();	
		
		String query = "select rd.*, rm.sreportname from "
				+ " reportmaster rm, reportdetails rd "
				+ " where rd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nreportcode=rd.nreportcode "
				+ " and rd.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and rm.nreportcode="+ reportMaster.getNreportcode();
		
		final ReportDetails reportDetails = jdbcTemplate.queryForObject(query, new ReportDetails());
		outputMap.put("SelectedReportDetails", reportDetails);
		outputMap.put("SelectedViewReportMaster", reportMaster);
		
		String ssystemfilename = reportDetails.getSsystemfilename();
		String sFile = FilenameUtils.getExtension(ssystemfilename.toString());
		
		if (sFile.equals("jrxml")){
			outputMap.put("filetype", "jrxml");
		}else if (sFile.equals("mrt")){
			outputMap.put("filetype", "mrt");
		}
		if(reportDetails != null)
		{				
			final Map<String, Object> designMap = (Map<String, Object>) reportMasterDAO.getReportParameter(reportDetails.getNreportdetailcode(), userInfo).getBody();
			final List<ReportParameter> parameterList= (List<ReportParameter>) designMap.get("ReportParameter");
			
			if (parameterList.isEmpty()) {
					Map<String, Object> inputMap = new HashMap<String, Object>();
					
					int nregTypeCode = reportMaster.getNregtypecode();
					int nregSubTypeCode = reportMaster.getNregsubtypecode();
					int nreportDetailCode = reportDetails.getNreportdetailcode();
					int ncoareporttypecode = reportMaster.getNcoareporttypecode();
					
					
					inputMap.put("nreporttypecode", reportMaster.getNreporttypecode());
					inputMap.put("ncoareporttypecode",ncoareporttypecode);
					inputMap.put("nregtypecode", nregTypeCode);
					inputMap.put("nregsubtypecode", nregSubTypeCode);
					inputMap.put("nsectioncode",reportMaster.getNsectioncode());
					inputMap.put("ncertificatetypecode",reportMaster.getNcertificatetypecode());
					inputMap.put("ndecisionstatus",reportMaster.getNreportdecisiontypecode());
					inputMap.put("ncontrolcode", reportMaster.getNcontrolcode());
					inputMap.put("nreportdetailcode", nreportDetailCode);
					
					String sfileName = "";
					String sJRXMLname = "";
					int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
					int ncertificateTypeCode = -1;
					String sreportComments = "";
					int qType = 1;
					int ncontrolCode=-1;
				
					//boolean isRegerate=false;
					String systemFileName="";
					//String pdfSuffix="";
					
					String sfilesharedfolder = "";
					String fileuploadpath = "";
					String subReportPath = "";
					String imagePath = "";
					String pdfPath = "";
					final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("+Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode()+","+Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()+")";
					List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
					fileuploadpath = reportPaths.get(0);
					subReportPath = reportPaths.get(0);
					imagePath = reportPaths.get(0);
					pdfPath = reportPaths.get(1);
					
					Map<String,Object> dynamicReport= reportDAOSupport.getDynamicReports(inputMap, userInfo);
					String folderName = "";
					if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals((String)dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))){
						
						sJRXMLname=(String) dynamicReport.get("JRXMLname");
						nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
						folderName = (String) dynamicReport.get("folderName");
						imagePath = subReportPath;
						fileuploadpath= fileuploadpath+ folderName;
					}
					
					sfilesharedfolder = fileuploadpath + sJRXMLname;
					File JRXMLFile = new File(sfilesharedfolder);
					if (sJRXMLname != null && !sJRXMLname.equals("")) 
					{						
						Map<String, Object> jasperParameter = new HashMap<String, Object>();
						jasperParameter.put("ssubreportpath", subReportPath + folderName);
						jasperParameter.put("simagepath", imagePath + folderName);
						jasperParameter.put("certificatetypecode", ncertificateTypeCode);
						jasperParameter.put("needlogo", naccredit);
						jasperParameter.put("ndecisionstatus", inputMap.get("ndecisionstatus"));
						jasperParameter.put("nregtypecode", nregTypeCode);
						jasperParameter.put("nregsubtypecode", nregSubTypeCode);
						jasperParameter.put("sreportcomments", sreportComments);
						jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
						jasperParameter.put("nreportdetailcode", inputMap.get("nreportdetailcode"));
						
						outputMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName, userInfo,systemFileName,ncontrolCode, false,"","",""));
						String uploadStatus = (String)outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
						if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)){
							outputMap.put("ReportPDFFile", outputMap.get("outputFilename"));
						}
					}		
			}
			else {
				 
				final Map<String, Object> configMap = (Map<String, Object>) reportMasterDAO.getReportDesignConfig(reportDetails.getNreportdetailcode(), userInfo).getBody();
				final List<ReportDesignConfig> configList =  (List<ReportDesignConfig>)configMap.get("ReportDesignConfig");
							
				final StringBuffer queryBuffer = new StringBuffer();
				final List<String> fieldList = new ArrayList<String>();
				
				final Map<String, String> dateFieldMap = new HashMap<String,String>();
				
				for(ReportDesignConfig designConfig : configList) 
				{						
					if (designConfig.getNdesigncomponentcode() == Enumeration.DesignComponent.DATEFEILD.gettype()) {
												
						Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo);//objGeneral.getUTCDateTime();					
						if (designConfig.getNdays() >= 0) {
							instantDate = instantDate.plus(designConfig.getNdays(), ChronoUnit.DAYS);
						}
						else {
							instantDate = instantDate.minus(Math.abs(designConfig.getNdays()), ChronoUnit.DAYS);
						}
						
						final String stringDate = dateUtilityFunction.convertUtcToLocalSiteTime(instantDate, userInfo);
						designConfig.setDataList(Arrays.asList(stringDate));
						dateFieldMap.put(designConfig.getSreportparametername(), stringDate);
					}
				}
				
				for(ReportDesignConfig designConfig : configList) 
				{								
					if (designConfig.getNsqlquerycode() != -1) 
					{
						if (designConfig.getSsqlquery().contains("<@")||designConfig.getSsqlquery().contains("<#")) {
							String sqlQuery = designConfig.getSsqlquery();
							for(Map.Entry<String, String> dateField : dateFieldMap.entrySet()) {
								if(sqlQuery.contains("<@"+dateField.getKey()+"@>")) {
									sqlQuery = sqlQuery.replace("<@"+dateField.getKey()+"@>","'"+dateField.getValue()+"'");	
								}else if(sqlQuery.contains("<#"+dateField.getKey()+"#>")) {
									sqlQuery = sqlQuery.replace("<#"+dateField.getKey()+"#>","'"+dateField.getValue()+"'");
								}
							
							}	
							if (!sqlQuery.contains("<@")&&!sqlQuery.contains("<#")) {
								//ALPD-6047 L.Subashini
								//queryBuffer.append(sqlQuery+";");
								queryBuffer.append(sqlQuery);	
								if(!sqlQuery.endsWith(";")){
									queryBuffer.append(";");
								}
								fieldList.add(designConfig.getSdisplayname());
							}
						}
						else {
							
							//ALPD-6047 L.Subashini
							//queryBuffer.append(designConfig.getSsqlquery()+";");	
							queryBuffer.append(designConfig.getSsqlquery());
							if(!designConfig.getSsqlquery().endsWith(";")){
								queryBuffer.append(";");
							}
							fieldList.add(designConfig.getSdisplayname());
						}
					}
				}			
				
				//need to discuss,check and change for below  findByMultiQueryPlainSql method 
				final Map<String, Object> queryMap = new HashMap<>();
						//(Map<String, Object>)findByMultiQueryPlainSql(queryBuffer.toString(),  fieldList.toArray());
				final ObjectMapper objMapper = new ObjectMapper();
			
				for(ReportDesignConfig designConfig : configList) {
			
					if (fieldList.contains(designConfig.getSdisplayname())) {
						designConfig.setDataList((List<?>)queryMap.get(designConfig.getSdisplayname()));
						
						List<?> list = (List<?>)queryMap.get(designConfig.getSdisplayname());
						
						if (list != null &&list.size() > 0) {
							Map<String, Object> mapcolumnfileds = objMapper.convertValue(list.get(0), Map.class);
							List<String> multilingualColumnList = new ArrayList<String>();
							
							for (Map.Entry<String, Object> entry : mapcolumnfileds.entrySet()) {
								if (entry.getValue() != null) {
									if (entry.getValue().getClass().getSimpleName().equalsIgnoreCase("String")
											&& entry.getValue().toString().length() > 4
											&& entry.getValue().toString().substring(0, 4).equals("IDS_")) {
										multilingualColumnList.add(entry.getKey());
									}					
								}
							}						
						
							list = objMapper.convertValue(commonFunction.getMultilingualMessageList(list, 
									multilingualColumnList, userInfo.getSlanguagefilename()), new TypeReference<List<Map<String,Object>>>() {
									});
						}
						if (list != null)
						{
							designConfig.setDataList(list);
						}						
					}
				}				
				outputMap.put("ViewReportDesignConfig", configMap.get("ReportDesignConfig"));	
				outputMap.put("ReportPDFFile", "");			
			}
		}			
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
