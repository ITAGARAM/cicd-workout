package com.agaramtech.qualis.goodsin.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TimeZone;
import com.agaramtech.qualis.checklist.model.ChecklistQB;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.contactmaster.model.Courier;
import com.agaramtech.qualis.contactmaster.service.courier.CourierDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.goodsin.model.GoodsIn;
import com.agaramtech.qualis.goodsin.model.GoodsInChecklist;
import com.agaramtech.qualis.goodsin.model.GoodsInSample;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;



@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class GoodsInDAOImpl implements GoodsInDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoodsInDAOImpl.class);
	
	private final TransactionDAOSupport transactionDAOSupport;
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ReportDAOSupport reportDAOSupport;
	private final CourierDAO courierDAO;
	
	
	/*
	@Autowired
	CommonFunction commonFunction;
	
	@Autowired
	RegistrationDAO registrationDAO;

	@Autowired
	CourierDAO courierDAO;
	*/

	

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		String dfromdate ="";
		String dtodate ="";
		final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,(String)inputMap.get("currentdate"), "datetime", "FromDate");
		
		dfromdate = (String) mapObject.get("FromDate");
		dtodate = (String) mapObject.get("ToDate");
		inputMap.put("fromDate", dfromdate);
		inputMap.put("toDate", dtodate);		
		map.put("fromDate", mapObject.get("FromDateWOUTC"));
		map.put("toDate", mapObject.get("ToDateWOUTC"));
		map.put("realfromDate", mapObject.get("FromDateWOUTC"));
		map.put("realtoDate", mapObject.get("ToDateWOUTC"));
		map.putAll((Map<String, Object>)getGoodsInData(inputMap,userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getGoodsInData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception{
		final Map<String,Object>map =new HashMap<String, Object>();
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		
		String Query = " select g.*,gh.ntransactionstatus,gh.nusercode,gh.nuserrolecode,cc1.sclientcatname,c.sclientname,pt.sprojecttypename,pm.sprojectname,cc.scouriername, ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as stransdisplaystatus,u.sfirstname|| ' '||u.slastname as susername,ur.suserrolename,"
				 	 + " to_char(g.dgoodsindatetime,'" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as sgoodsindatetime,"
				 	 + " case when gh.ntransactionstatus ="+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" then g.ngoodsincode::text else g.sgoodsinid end as sgoodsinid1,"
				 	 + " ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sviewstatus "
				 	 + " from goodsin g "
				 	 + " join goodsinhistory gh on gh.ngoodsincode =g.ngoodsincode "
					 + " join clientcategory cc1 on cc1.nclientcatcode =g.nclientcatcode "
					 + " join client c on c.nclientcode =g.nclientcode "
					 + " join projecttype pt on pt.nprojecttypecode = g.nprojecttypecode "
				 	 + " join projectmaster pm on pm.nprojectmastercode =g.nprojectmastercode "
				 	 + " join courier cc on cc.ncouriercode =g.ncouriercode "
				 	 + " join users u on u.nusercode = gh.nusercode "
				 	 + " join userrole ur on ur.nuserrolecode =gh.nuserrolecode "
				 	 + " join transactionstatus ts on ts.ntranscode =gh.ntransactionstatus "
				 	 + " join transactionstatus ts1 on ts1.ntranscode =g.noutofhours "
				 	 + " where  "
				 	 + " gh.ngoodsinhistorycode =any(select max(ngoodsinhistorycode) as ngoodsinhistorycode from goodsinhistory where nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" group by ngoodsincode) "
				 	 + " and gh.dtransactiondate between '"+fromDate+"' and '"+toDate+"' "
				 	 + " and g.nsitecode="+userInfo.getNtranssitecode()+" and gh.nsitecode="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" "
				 	 + " and cc1.nsitecode="+userInfo.getNmastersitecode()+" and c.nsitecode="+userInfo.getNmastersitecode()+" and pt.nsitecode ="+userInfo.getNmastersitecode()+" "
				 	 + " and cc.nsitecode="+userInfo.getNmastersitecode()+" and u.nsitecode="+userInfo.getNmastersitecode()+" and ur.nsitecode ="+userInfo.getNmastersitecode()+" "
				 	 + " and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				 	 + " and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				 	 + " and ur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ngoodsincode ";
		List<GoodsIn> lst = jdbcTemplate.query(Query, new GoodsIn());
		if(lst.size()>0) {			
			map.putAll((Map<String, Object>)getApprovedVersionTemplateMapping(inputMap,userInfo).getBody());
			if(map.get("ndesigntemplatemappingcode") !=null) {
				Integer ndesigntemplatemappingcode = (Integer) map.get("ndesigntemplatemappingcode");
				map.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
				map.putAll((Map<String, Object>)getGoodsInSampleDetails(lst.get(lst.size()-1).getNgoodsincode(),ndesigntemplatemappingcode,userInfo).getBody());
			}		
			map.put("GoodsIn", lst);
			map.put("selectedGoodsIn",lst.get(lst.size()-1));
		}else {
			map.put("GoodsIn", null);
			map.put("selectedGoodsIn", lst);
		}
		
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object>getApprovedVersionTemplateMapping(Map<String,Object> inputMap,UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();

		String Query1 = " select * from reactregistrationtemplate rt "
					  + " join designtemplatemapping dm on dm.nreactregtemplatecode =rt.nreactregtemplatecode "
					  + " and dm.nsampletypecode ="+Enumeration.SampleType.GOODSIN.getType()+" and dm.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
					  + " and rt.nsitecode="+userInfo.getNmastersitecode()+" and dm.nsitecode ="+userInfo.getNmastersitecode()+" "
					  + " and dm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  ";
		List<DesignTemplateMapping> lst1 =jdbcTemplate.query(Query1, new DesignTemplateMapping());
		
		if(lst1.size() >0 ) {
			map.put("ndesigntemplatemappingcode", lst1.get(0).getNdesigntemplatemappingcode());	
			inputMap.put("ndesigntemplatemappingcode", lst1.get(0).getNdesigntemplatemappingcode());
			map.put("DesignTemplateMapping", lst1);	
		}else {
			map.put("DesignTemplateMapping", null);	
		}
		return new ResponseEntity<>(map,HttpStatus.OK);

	}	
	
	@Override
	public ResponseEntity<Object>getGoodsInAdd(Map<String,Object> inputMap,UserInfo userInfo) throws Exception {
		final Map<String,Object> map=new HashMap<String,Object>();
		
		String clientCatQuery="select * from clientcategory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and nclientcatcode >0";
		List<ClientCategory> objClientCat =jdbcTemplate.query(clientCatQuery, new ClientCategory());		

		
		String courierQuery="select * from courier c,country ct where ct.ncountrycode=c.ncountrycode and  ct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and"
				+ " c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  c.nsitecode="+userInfo.getNmastersitecode()+" and  ct.nsitecode="+userInfo.getNmastersitecode()+" and c.ncouriercode >0";
		List<Courier> objCourier =jdbcTemplate.query(courierQuery, new Courier());
		
		String timeZoneQuery ="select * from timezone where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ntimezonecode >0";
		List<TimeZone> objTimeZone =jdbcTemplate.query(timeZoneQuery, new TimeZone());

		map.put("ClientCategory", objClientCat);
		map.put("Courier", objCourier);
		map.put("TimeZone", objTimeZone);	
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getClient(Integer nClientCatCode, UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		
		String Query = "select nclientcode,sclientname from client where nclientcatcode ="+nClientCatCode+"  "
					 + "and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNmastersitecode()+" ";
		List<Client> lst =jdbcTemplate.query(Query, new Client());
		map.put("Client", lst);		
		
		if(lst.size() >0) {
			String Query1 = "select pt.nprojecttypecode,pt.sprojecttypename from projecttype pt "
				 	  	  + "join projectmaster pm on pm.nprojecttypecode =pt.nprojecttypecode "
				 	  	  + "join projectmasterhistory pmh  on pmh.nprojectmastercode =pm.nprojectmastercode "
				 	  	  + "where pm.nclientcatcode ="+nClientCatCode+" and pm.nclientcode ="+lst.get(0).getNclientcode()+"  "
				 	  	  + "and pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
				 	  	  + "projectmasterhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" group by nprojectmastercode) and pmh.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				 	  	  + " and  pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pmh.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				 	  	  + "and pmh.nsitecode ="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" and pt.nsitecode="+userInfo.getNmastersitecode()+" "
				 	  	  + "group by pt.nprojecttypecode,pt.sprojecttypename ";		
			List<ProjectType> lst1 =jdbcTemplate.query(Query1, new ProjectType());
			map.put("ProjectType", lst1);
			
			if(lst1.size() >0) {
				String Query2 =" select pm.* from projectmaster pm "
						  	  +" join projectmasterhistory pmh on pm.nprojectmastercode =pmh.nprojectmastercode "
						  	  +" where pm.nclientcatcode ="+nClientCatCode+" and pm.nclientcode ="+lst.get(0).getNclientcode()+" and pm.nprojecttypecode="+lst1.get(0).getNprojecttypecode()+" and pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
						  	  + " projectmasterhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+"  group by nprojectmastercode) and pmh.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						  	  +" and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pmh.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  	  +" and pmh.nsitecode ="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" ";

			List<ProjectMaster> lst2 =jdbcTemplate.query(Query2, new ProjectMaster());
			map.put("ProjectMaster", lst2);
			}
		}	
		
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getProjectType(Integer nClientCatCode,Integer nClientCode, UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		
		String Query = "select pt.nprojecttypecode,pt.sprojecttypename from projecttype pt "
					 + "join projectmaster pm on pm.nprojecttypecode =pt.nprojecttypecode "
					 + "join projectmasterhistory pmh  on pmh.nprojectmastercode =pm.nprojectmastercode "
					 + "where pm.nclientcatcode ="+nClientCatCode+" and pm.nclientcode ="+nClientCode+"  "
					 + "and pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
					 + "projectmasterhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" group by nprojectmastercode) and pmh.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					 +" and pt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pmh.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					 + "and pmh.nsitecode ="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" and pt.nsitecode="+userInfo.getNmastersitecode()+" "
					 + "group by pt.nprojecttypecode,pt.sprojecttypename ";		
		List<ProjectType> lst =jdbcTemplate.query(Query, new ProjectType());
		map.put("ProjectType", lst);
		
		if(lst.size() > 0) {
			String Query1 = " select pm.* from projectmaster pm "
						  + " join projectmasterhistory pmh on pm.nprojectmastercode =pmh.nprojectmastercode "
						  + " where pm.nclientcatcode ="+nClientCatCode+" and pm.nclientcode ="+nClientCode+" and pm.nprojecttypecode="+lst.get(0).getNprojecttypecode()+" and pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
						  + " projectmasterhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" group by nprojectmastercode) and pmh.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
						  + " and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pmh.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						  + " and pmh.nsitecode ="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" ";

		List<ProjectMaster> lst1 =jdbcTemplate.query(Query1, new ProjectMaster());
		map.put("ProjectMaster", lst1);	
		}
		
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getProjectMaster(Integer nClientCatCode,Integer nClientCode,Integer nProjectTypeCode, UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		
		
		String Query =" select pm.* from projectmaster pm "
					+ " join projectmasterhistory pmh on pm.nprojectmastercode =pmh.nprojectmastercode "
					+ " where pm.nclientcatcode ="+nClientCatCode+" and pm.nclientcode ="+nClientCode+" and pm.nprojecttypecode="+nProjectTypeCode+" "
					+ " and pmh.nprojectmasterhistorycode=any(select max(nprojectmasterhistorycode) from "
					+ "	projectmasterhistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+" group by nprojectmastercode) and pmh.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pmh.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and pmh.nsitecode ="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" ";

		List<ProjectMaster> lst =jdbcTemplate.query(Query, new ProjectMaster());
		map.put("ProjectMaster", lst);		
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getGoodsInEdit(int ngoodsincode, UserInfo userInfo) throws Exception {
		
		final Map<String,Object>map =new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		String editQuery =" select g.*,gh.ntransactionstatus,gh.nusercode,gh.nuserrolecode,cc1.sclientcatname,c.sclientname,pt.sprojecttypename,pm.sprojectname,cc.scouriername, "
						 +" ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as stransdisplaystatus,"
						 +" u.sfirstname|| ' '||u.slastname as susername,ur.suserrolename,"
						 +" to_char(g.dgoodsindatetime,'" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as sgoodsindatetime,"
						 +" case when gh.ntransactionstatus ="+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" then g.ngoodsincode::text else g.sgoodsinid end as sgoodsinid1"
						 +" from goodsin g "
						 +" join goodsinhistory gh on gh.ngoodsincode =g.ngoodsincode "
						 +" join clientcategory cc1 on cc1.nclientcatcode =g.nclientcatcode "
						 +" join client c on c.nclientcode =g.nclientcode "
						 +" join projecttype pt on pt.nprojecttypecode = g.nprojecttypecode "
						 +" join projectmaster pm on pm.nprojectmastercode =g.nprojectmastercode "
						 +" join courier cc on cc.ncouriercode =g.ncouriercode "
						 +" join users u on u.nusercode = gh.nusercode "
						 +" join userrole ur on ur.nuserrolecode =gh.nuserrolecode "
						 +" join transactionstatus ts on ts.ntranscode =gh.ntransactionstatus "
						 +" where"
						 +" gh.ntransactionstatus =(select max(ntransactionstatus) as ntransactionstatus from goodsinhistory where ngoodsincode ="+ngoodsincode+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+") "
						 +" and g.nsitecode= gh.nsitecode "
						 + " and g.nsitecode="+userInfo.getNtranssitecode()+" and gh.nsitecode="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" "
					 	 + " and cc1.nsitecode="+userInfo.getNmastersitecode()+" and c.nsitecode="+userInfo.getNmastersitecode()+" and pt.nsitecode ="+userInfo.getNmastersitecode()+" "
					 	 + " and cc.nsitecode="+userInfo.getNmastersitecode()+" and u.nsitecode="+userInfo.getNmastersitecode()+" and ur.nsitecode ="+userInfo.getNmastersitecode()+" "
						 +"	and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 +" and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 +" and ur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and g.ngoodsincode ="+ngoodsincode+" and g.ngoodsincode >0 ";
		
		List<GoodsIn> objGoodsIn = jdbcTemplate.query(editQuery, new GoodsIn());		
		final List<GoodsIn> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(objGoodsIn,Arrays.asList("sgoodsindatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<GoodsIn>>() {});
		
		if(lstUTCConvertedDate.size()>0) {			
			map.put("selectedGoodsIn", lstUTCConvertedDate.get(0));			
		}else {			
			map.put("selectedGoodsIn", null);			
		}
		GoodsIn GoodsIn = (GoodsIn) map.get("selectedGoodsIn");	
		
		final Map<String,Object>inputMap =new HashMap<String, Object>();

		map.putAll((Map<String, Object>)getApprovedVersionTemplateMapping(inputMap,userInfo).getBody());
		if(map.containsKey("ndesigntemplatemappingcode")) {
			Integer ndesigntemplatemappingcode = (Integer) map.get("ndesigntemplatemappingcode");
			map.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
			map.putAll((Map<String, Object>)getGoodsInSampleDetails(ngoodsincode,ndesigntemplatemappingcode,userInfo).getBody());	
		}
					
				
		if (GoodsIn == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
		
		if(GoodsIn.getNtransactionstatus() ==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECEIVERECORD",
					userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(map,HttpStatus.OK);

	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActiveGoodsInById(int ngoodsincode, UserInfo userInfo) throws Exception {
		
		final Map<String,Object>map =new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		String editQuery =" select g.*,gh.ntransactionstatus,gh.nusercode,gh.nuserrolecode,cc1.sclientcatname,c.sclientname,pt.sprojecttypename,pm.sprojectname,cc.scouriername, "
						 +" ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as stransdisplaystatus,"
						 +" u.sfirstname|| ' '||u.slastname as susername,ur.suserrolename,"
						 +" to_char(g.dgoodsindatetime,'" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as sgoodsindatetime,"
						 +" case when gh.ntransactionstatus ="+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" then g.ngoodsincode::text else g.sgoodsinid end as sgoodsinid1,"
						 + " ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sviewstatus "
						 +" from goodsin g "
						 +" join goodsinhistory gh on gh.ngoodsincode =g.ngoodsincode "
						 +" join clientcategory cc1 on cc1.nclientcatcode =g.nclientcatcode "
						 +" join client c on c.nclientcode =g.nclientcode "
						 +" join projecttype pt on pt.nprojecttypecode = g.nprojecttypecode "
						 +" join projectmaster pm on pm.nprojectmastercode =g.nprojectmastercode "
						 +" join courier cc on cc.ncouriercode =g.ncouriercode "
						 +" join users u on u.nusercode = gh.nusercode "
						 +" join userrole ur on ur.nuserrolecode =gh.nuserrolecode "
						 +" join transactionstatus ts on ts.ntranscode =gh.ntransactionstatus "
						 +" join transactionstatus ts1 on ts1.ntranscode =g.noutofhours "
						 +" where"
						 +" gh.ntransactionstatus =(select max(ntransactionstatus) as ntransactionstatus from goodsinhistory where ngoodsincode ="+ngoodsincode+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+") "
						 +" and g.nsitecode= gh.nsitecode "
						 + " and g.nsitecode="+userInfo.getNtranssitecode()+" and gh.nsitecode="+userInfo.getNtranssitecode()+" and pm.nsitecode ="+userInfo.getNtranssitecode()+" "
					 	 + " and cc1.nsitecode="+userInfo.getNmastersitecode()+" and c.nsitecode="+userInfo.getNmastersitecode()+" and pt.nsitecode ="+userInfo.getNmastersitecode()+" "
					 	 + " and cc.nsitecode="+userInfo.getNmastersitecode()+" and u.nsitecode="+userInfo.getNmastersitecode()+" and ur.nsitecode ="+userInfo.getNmastersitecode()+" "
						 +"	and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 +" and pm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						 +" and ur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and g.ngoodsincode ="+ngoodsincode+" and g.ngoodsincode >0 ";
		
		List<GoodsIn> objGoodsIn = jdbcTemplate.query(editQuery, new GoodsIn());		
		final List<GoodsIn> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(objGoodsIn,Arrays.asList("sgoodsindatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<GoodsIn>>() {});
		
		if(lstUTCConvertedDate.size()>0) {			
			map.put("selectedGoodsIn", lstUTCConvertedDate.get(0));			
		}else {			
			map.put("selectedGoodsIn", null);			
		}
		GoodsIn GoodsIn = (GoodsIn) map.get("selectedGoodsIn");	
		
		final Map<String,Object>inputMap =new HashMap<String, Object>();

		map.putAll((Map<String, Object>)getApprovedVersionTemplateMapping(inputMap,userInfo).getBody());
		if(map.containsKey("ndesigntemplatemappingcode")) {
			Integer ndesigntemplatemappingcode = (Integer) map.get("ndesigntemplatemappingcode");
			map.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userInfo.getNformcode()));
			map.putAll((Map<String, Object>)getGoodsInSampleDetails(ngoodsincode,ndesigntemplatemappingcode,userInfo).getBody());		
		}
							
		if (GoodsIn == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(map,HttpStatus.OK);
	}		

	@Override
	public ResponseEntity<Object> createGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		final String strQuery ="lock table lockgoodsin "+Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+" ";
		jdbcTemplate.execute(strQuery);
		
		
		
		
		
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedGoodsInList = new ArrayList<>();	
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		objmapper.registerModule(new JavaTimeModule());
		final GoodsIn objGoodsIn = objmapper.convertValue(inputMap.get("goodsin"), GoodsIn.class);	

		if(objGoodsIn.getNcouriercode()!=-1) {
			Courier  courierObj=courierDAO.getActiveCourierById(objGoodsIn.getNcouriercode());
			if(courierObj==null) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COURIERALREADYDELETED",
						userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				
		}
		}

		if (objGoodsIn.getDgoodsindatetime() != null) {
			objGoodsIn.setSgoodsindatetime(dateUtilityFunction.instantDateToString(objGoodsIn.getDgoodsindatetime()).replace("T", " ").replace("Z",""));
			lstDateField.add("sgoodindatetime");
			lstDatecolumn.add("ntzgoodsindatetime");
		}
		
		String goodsinDateTime;
		if(objGoodsIn.getDgoodsindatetime() ==null) {
			goodsinDateTime =null;
		}else {
			goodsinDateTime="'"+objGoodsIn.getSgoodsindatetime()+"'";
		}		
		
		final GoodsIn convertedObject = objmapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objGoodsIn, lstDateField, lstDatecolumn, true, userInfo),new TypeReference<GoodsIn>() {});
		
		
		
		int goodsinSeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnogoodsinmanagement where stablename='goodsin'", Integer.class, jdbcTemplate);
		goodsinSeqNo++;
		
		int goodsinhistorySeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnogoodsinmanagement where stablename='goodsinhistory'", Integer.class, jdbcTemplate);
		goodsinhistorySeqNo++;
		
		
		String goodsinInsert=" Insert into goodsin(ngoodsincode,sgoodsinid,nclientcatcode,nclientcode,nprojecttypecode,nprojectmastercode,ncouriercode,nnoofpackages,noutofhours,sconsignmentno,ssecurityrefno,"
				   			+" scomments,dgoodsindatetime,ntzgoodsindatetime,noffsetdgoodsindatetime,nsitecode,nstatus)"
				   			+" values("+goodsinSeqNo+",'-',"+objGoodsIn.getNclientcatcode()+","+objGoodsIn.getNclientcode()+","+objGoodsIn.getNprojecttypecode()+","+objGoodsIn.getNprojectmastercode()+","+objGoodsIn.getNcouriercode()+","+objGoodsIn.getNnoofpackages()+","
				   			+" "+objGoodsIn.getNoutofhours()+",N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getSconsignmentno())+"',N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getSsecurityrefno())+"',"
				   			+" N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getScomments())+"',"+goodsinDateTime+","+convertedObject.getNtzgoodsindatetime()+","+convertedObject.getNoffsetdgoodsindatetime()+","
				   			+" "+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")" ;
		jdbcTemplate.execute(goodsinInsert);				   

		
		String goodsinhistoryInsert = " Insert into goodsinhistory(ngoodsinhistorycode,ngoodsincode,nusercode,nuserrolecode,ndeputyusercode,"
									+ "ndeputyuserrolecode,ntransactionstatus,dtransactiondate,ntransdatetimezonecode,noffsetdtransactiondate,"
									+ "scomments,nsitecode,nstatus) "
									+ "values("+goodsinhistorySeqNo+","+goodsinSeqNo+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","
									+ " "+userInfo.getNdeputyuserrole()+","+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
									+ " "+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",N'"+userInfo.getSreason()+"',"+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
		jdbcTemplate.execute(goodsinhistoryInsert);				   

		jdbcTemplate.execute("update seqnogoodsinmanagement set nsequenceno = " + goodsinSeqNo + " where stablename='goodsin'");
		jdbcTemplate.execute("update seqnogoodsinmanagement set nsequenceno = " + goodsinhistorySeqNo + " where stablename='goodsinhistory'");

		objGoodsIn.setNgoodsincode(goodsinSeqNo);
		objGoodsIn.setSgoodsinid("-");
		objGoodsIn.setNtransactionstatus(Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

		
		 String strnew  = "select COALESCE(TO_CHAR(dgoodsindatetime,'" + userInfo.getSsitedate() +"'),'') as sgoodsindatetime "
						+ " from goodsin  where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNtranssitecode()+" and "
						+ " ngoodsincode="+goodsinSeqNo+"";
		
		List<GoodsIn> goodsinlist = jdbcTemplate.query(strnew, new GoodsIn());
		objGoodsIn.setSgoodsindatetime(goodsinlist.get(0).getSgoodsindatetime());
			    
		savedGoodsInList.add(objGoodsIn);
		multilingualIDList.add("IDS_ADDGOODSIN");
		auditUtilityFunction.fnInsertAuditAction(savedGoodsInList, 1, null, multilingualIDList, userInfo);
		return getGoodsInData(inputMap, userInfo);		
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> updateGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSavedGoodsInList = new ArrayList<>();
		final List<Object> afterSavedGoodsInList = new ArrayList<>();
		List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();
		objmapper.registerModule(new JavaTimeModule());
		final GoodsIn objGoodsIn = objmapper.convertValue(inputMap.get("goodsin"), GoodsIn.class);	
		GoodsIn oldgoodsin = new GoodsIn();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		

		
		ResponseEntity<Object> obj =getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
		if(obj.getBody().equals("Already Deleted")) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else {
			if(objGoodsIn.getNcouriercode()!=-1) {
				Courier  courierObj=courierDAO.getActiveCourierById(objGoodsIn.getNcouriercode());
				if(courierObj==null) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_COURIERALREADYDELETED",
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					
			}
			}
			objMap = (Map<List, Object>) obj.getBody();
			oldgoodsin = (GoodsIn) objMap.get("selectedGoodsIn");
			if(oldgoodsin.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus() || oldgoodsin.getNtransactionstatus() == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
				if (objGoodsIn.getDgoodsindatetime() != null) {
					objGoodsIn.setSgoodsindatetime(dateUtilityFunction.instantDateToString(objGoodsIn.getDgoodsindatetime()).replace("T", " ").replace("Z",""));
					lstDateField.add("sgoodsindatetime");
					lstDatecolumn.add("ntzgoodsindatetime");
				}
				
				final GoodsIn convertedObject = objmapper.convertValue(dateUtilityFunction.convertInputDateToUTCByZone(objGoodsIn,
						lstDateField, lstDatecolumn, true, userInfo),new TypeReference<GoodsIn>() {});			
				
				String strold= "select COALESCE(TO_CHAR(dgoodsindatetime,'" + userInfo.getSsitedate() +"'),'') as sgoodsindatetime "
							 + " from goodsin where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and "
							 + "ngoodsincode="+objGoodsIn.getNgoodsincode()+"";
			
			    List<GoodsIn> oldgoodsinlst = jdbcTemplate.query(strold, new GoodsIn());
			    oldgoodsin.setSgoodsindatetime(oldgoodsinlst.get(0).getSgoodsindatetime());
			    
			    String goodsinDateTime;
				if(objGoodsIn.getDgoodsindatetime() ==null) {
					goodsinDateTime =null;
				}else {
					goodsinDateTime="'"+objGoodsIn.getSgoodsindatetime()+"'";
				}
			    
				String updateQuery  = "update goodsin set nclientcatcode="+objGoodsIn.getNclientcatcode()+",nclientcode ="+objGoodsIn.getNclientcode()+",nprojecttypecode="+objGoodsIn.getNprojecttypecode()+",nprojectmastercode="+objGoodsIn.getNprojectmastercode()+",ncouriercode ="+objGoodsIn.getNcouriercode()+",nnoofpackages="+objGoodsIn.getNnoofpackages()+", "
									+ "noutofhours="+objGoodsIn.getNoutofhours()+",sconsignmentno=N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getSconsignmentno())+"',ssecurityrefno=N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getSsecurityrefno())+"',scomments=N'"+stringUtilityFunction.replaceQuote(objGoodsIn.getScomments())+"',dgoodsindatetime="+goodsinDateTime+" "
									+ "where nsitecode="+userInfo.getNtranssitecode()+" and ngoodsincode ="+objGoodsIn.getNgoodsincode()+"";
				
				jdbcTemplate.execute(updateQuery);
				
				String strnew= "select COALESCE(TO_CHAR(dgoodsindatetime,'" + userInfo.getSsitedate() +"'),'') as sgoodsindatetime "
							 + " from goodsin where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and "
							 + " ngoodsincode="+objGoodsIn.getNgoodsincode()+"";
			
			    List<GoodsIn> newgoodsinlst = jdbcTemplate.query(strnew, new GoodsIn());
			    objGoodsIn.setSgoodsindatetime(newgoodsinlst.get(0).getSgoodsindatetime());
			    
				beforeSavedGoodsInList.add(oldgoodsin);
				afterSavedGoodsInList.add(objGoodsIn);
				multilingualIDList.add("IDS_EDITGOODSIN");
				auditUtilityFunction.fnInsertAuditAction(afterSavedGoodsInList, 2, beforeSavedGoodsInList,multilingualIDList, userInfo);	
				return getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECEIVERECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		
			
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> deleteGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deleteGoodsInList = new ArrayList<>();		
		objmapper.registerModule(new JavaTimeModule());
		final GoodsIn objGoodsIn = objmapper.convertValue(inputMap.get("goodsin"), GoodsIn.class);	
		
		GoodsIn goodsin = new GoodsIn();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();

		
		ResponseEntity<Object> obj =getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
		if(obj.getBody().equals("Already Deleted")) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else {
			objMap = (Map<List, Object>) obj.getBody();
			goodsin = (GoodsIn) objMap.get("selectedGoodsIn");
			if (goodsin.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				String str  =" select COALESCE(TO_CHAR(dgoodsindatetime,'" + userInfo.getSsitedate() +"'),'') as sgoodsindatetime from goodsin "
							+" where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and "
							+" ngoodsincode="+objGoodsIn.getNgoodsincode()+"";
		
			    List<GoodsIn> goodsinlst = jdbcTemplate.query(str, new GoodsIn());
			    goodsin.setSgoodsindatetime(goodsinlst.get(0).getSgoodsindatetime());
		    
				String deleteQuery = " update goodsin set  nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								   + " where nsitecode="+userInfo.getNtranssitecode()+" and ngoodsincode = " + objGoodsIn.getNgoodsincode() + ";";
				
				deleteQuery +=" update goodsinhistory set nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
				 			+ " where nsitecode="+userInfo.getNtranssitecode()+" and ngoodsincode = " + objGoodsIn.getNgoodsincode() + "; ";
				
				deleteQuery +=" update goodsinsample set nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
	 						+ " where nsitecode="+userInfo.getNtranssitecode()+" and ngoodsincode = " + objGoodsIn.getNgoodsincode() + "; ";
			
				jdbcTemplate.execute(deleteQuery);
				deleteGoodsInList.add(goodsin);
				multilingualIDList.add("IDS_DELETEGOODSIN");			
				auditUtilityFunction.fnInsertAuditAction(deleteGoodsInList, 1,null,multilingualIDList , userInfo);
				return getGoodsInData(inputMap, userInfo);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}		
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> receiveGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSavedGoodsInList = new ArrayList<>();
		final List<Object> afterSavedGoodsInList = new ArrayList<>();
		objmapper.registerModule(new JavaTimeModule());
		final GoodsIn objGoodsIn = objmapper.convertValue(inputMap.get("goodsin"), GoodsIn.class);
		
		GoodsIn oldgoodsin = new GoodsIn();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();

		
		ResponseEntity<Object> obj =getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
		if(obj.getBody().equals("Already Deleted")) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else {
			objMap = (Map<List, Object>) obj.getBody();
			oldgoodsin = (GoodsIn) objMap.get("selectedGoodsIn");
			if (oldgoodsin.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				String str="select ngoodsinsamplecode from goodsinsample where ngoodsincode ="+objGoodsIn.getNgoodsincode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+"";
				List<GoodsInSample> object = jdbcTemplate.query(str,new GoodsInSample());
				//GoodsInSample object  =(GoodsInSample) jdbcQueryForObject(str,  GoodsInSample.class);
				
				if(object.size() >0) {
					int goodsinhistorySeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnogoodsinmanagement where stablename='goodsinhistory'", Integer.class, jdbcTemplate);
					goodsinhistorySeqNo++;
					
					String goodsinhistoryInsert = " Insert into goodsinhistory(ngoodsinhistorycode,ngoodsincode,nusercode,nuserrolecode,ndeputyusercode,"
												+ "ndeputyuserrolecode,ntransactionstatus,dtransactiondate,ntransdatetimezonecode,noffsetdtransactiondate,"
												+ "scomments,nsitecode,nstatus) "
												+ "values("+goodsinhistorySeqNo+","+objGoodsIn.getNgoodsincode()+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","
												+ " "+userInfo.getNdeputyuserrole()+","+Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
												+ " "+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",N'"+userInfo.getSreason()+"',"+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
					jdbcTemplate.execute(goodsinhistoryInsert);				   

					jdbcTemplate.execute("update seqnogoodsinmanagement set nsequenceno = " + goodsinhistorySeqNo + " where stablename='goodsinhistory'");
					
					
					final String strformat = projectDAOSupport.getSeqfnFormat("goodsin","seqnoformatgeneratorgoodsin",0,0,userInfo);				
					objGoodsIn.setSgoodsinid(strformat);
					objGoodsIn.setNtransactionstatus(Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());

					
					String updatequery ="update goodsin set sgoodsinid='"+objGoodsIn.getSgoodsinid()+"' where nsitecode ="+userInfo.getNtranssitecode()+" and ngoodsincode ="+objGoodsIn.getNgoodsincode()+" ";
					jdbcTemplate.execute(updatequery);				   

					beforeSavedGoodsInList.add(oldgoodsin);
					afterSavedGoodsInList.add(objGoodsIn);
					multilingualIDList.add("IDS_RECEIVEGOODSIN");	
					auditUtilityFunction.fnInsertAuditAction(afterSavedGoodsInList, 2, beforeSavedGoodsInList,multilingualIDList, userInfo);	
					return getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDSAMPLE",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				}
				
			
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> approveGoodsIn(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSavedGoodsInList = new ArrayList<>();
		final List<Object> afterSavedGoodsInList = new ArrayList<>();
		objmapper.registerModule(new JavaTimeModule());
		final GoodsIn objGoodsIn = objmapper.convertValue(inputMap.get("goodsin"), GoodsIn.class);
		
		GoodsIn oldgoodsin = new GoodsIn();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();

		
		ResponseEntity<Object> obj =getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
		if(obj.getBody().equals("Already Deleted")) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else {
			objMap = (Map<List, Object>) obj.getBody();
			oldgoodsin = (GoodsIn) objMap.get("selectedGoodsIn");
			if (oldgoodsin.getNtransactionstatus() == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {		    
				
				int goodsinhistorySeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnogoodsinmanagement where stablename='goodsinhistory'", Integer.class, jdbcTemplate);
				goodsinhistorySeqNo++;
				
				String goodsinhistoryInsert = " Insert into goodsinhistory(ngoodsinhistorycode,ngoodsincode,nusercode,nuserrolecode,ndeputyusercode,"
											+ "ndeputyuserrolecode,ntransactionstatus,dtransactiondate,ntransdatetimezonecode,noffsetdtransactiondate,"
											+ "scomments,nsitecode,nstatus) "
											+ "values("+goodsinhistorySeqNo+","+objGoodsIn.getNgoodsincode()+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","
											+ " "+userInfo.getNdeputyuserrole()+","+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
											+ " "+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",N'"+userInfo.getSreason()+"',"+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
				jdbcTemplate.execute(goodsinhistoryInsert);				   

				jdbcTemplate.execute("update seqnogoodsinmanagement set nsequenceno = " + goodsinhistorySeqNo + " where stablename='goodsinhistory'");
				beforeSavedGoodsInList.add(oldgoodsin);
				afterSavedGoodsInList.add(objGoodsIn);
				objGoodsIn.setNtransactionstatus(Enumeration.TransactionStatus.APPROVED.gettransactionstatus());				
				multilingualIDList.add("IDS_APPROVEGOODSIN");			
				auditUtilityFunction.fnInsertAuditAction(afterSavedGoodsInList, 2, beforeSavedGoodsInList,multilingualIDList, userInfo);	
				return getActiveGoodsInById(objGoodsIn.getNgoodsincode(), userInfo);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECEIVERECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
	
	@Override
	public ResponseEntity<Object> getChecklistDesign(int nchecklistversioncode,int ngoodsincode,  UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		
		final String countQuery1 = "select count(ngoodsincode) from goodsin where ngoodsincode="+ngoodsincode+"  and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode();
		int count1 = (int) jdbcUtilityFunction.queryForObject(countQuery1, Integer.class, jdbcTemplate);
		if(count1 !=0) {
			String goodsinQuery = " select gh.ntransactionstatus from goodsin gi  "
								+ " join goodsinhistory gh on gi.ngoodsincode =gh.ngoodsincode "
								+ " join transactionstatus ts on ts.ntranscode = gh.ntransactionstatus "
								+ " where "
								+ " gh.ngoodsinhistorycode =any (select max(ngoodsinhistorycode) from goodsinhistory where ngoodsincode ="+ngoodsincode+" and  nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
								+ " and gi.ngoodsincode ="+ngoodsincode+" and gi.nsitecode ="+userInfo.getNtranssitecode()+" and gh.nsitecode="+userInfo.getNtranssitecode()+" "
								+ " and gi.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			List<GoodsIn> list =jdbcTemplate.query(goodsinQuery, new GoodsIn());		
	
			if(list.get(0).getNtransactionstatus() != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				String str = "select clv.nchecklistversioncode,c.nchecklistcode,vqb.nchecklistqbcode,lqb.nchecklistcomponentcode,"
						+ " lqb.squestiondata,lqb.squestion,lqb.nmandatory as nmandatoryfield,"						
						+ " case when (gc.jsondata->>(vqb.nchecklistversionqbcode)::character varying) isnull  then  "
						+ " case when lqb.nchecklistcomponentcode ="+Enumeration.ChecklistComponent.DATEFEILD.getNchecklistcomponent()+"   then  to_char((vct.sdefaultvalue)::timestamp without time zone, 'dd/MM/yyyy HH24:mi:ss'::text) else vct.sdefaultvalue end "
						+ " else case when lqb.nchecklistcomponentcode ="+Enumeration.ChecklistComponent.DATEFEILD.getNchecklistcomponent()+" then to_char((gc.jsondata ->> (vqb.nchecklistversionqbcode)::character varying)::timestamp without time zone, 'dd/MM/yyyy HH24:mi:ss'::text) else gc.jsondata->>(vqb.nchecklistversionqbcode)::character varying "
						+ " end  end sdefaultvalue, vqb.nchecklistversionqbcode "
						+ " from checklistqb lqb,checklist c,checklistversion clv,checklistversionqb vqb"
						+ " left outer join checklistversiontemplate vct on vqb.nchecklistversionqbcode=vct.nchecklistversionqbcode and vct.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and vct.nsitecode="+userInfo.getNmastersitecode()+" "
						+ " left outer join goodsinchecklist gc on gc.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and gc.nsitecode="+userInfo.getNtranssitecode()+" and gc.ngoodsincode= " + ngoodsincode 
						+ " where clv.nchecklistcode=c.nchecklistcode and vqb.nchecklistversioncode=clv.nchecklistversioncode "
						+ " and vqb.nchecklistqbcode=lqb.nchecklistqbcode and clv.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and vqb.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and lqb.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and c.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and lqb.nsitecode="+userInfo.getNmastersitecode()+" and c.nsitecode="+userInfo.getNmastersitecode()+" and vqb.nsitecode="+userInfo.getNmastersitecode()+" and clv.nsitecode ="+userInfo.getNmastersitecode()+" "
						+ " and clv.nchecklistversioncode="+ nchecklistversioncode
						//ALPD-3556
						+" order by vqb.nchecklistversionqbcode";
	
					List<ChecklistQB> lstcheckqb = jdbcTemplate.query(str, new ChecklistQB());
					returnMap.put("ChecklistData", lstcheckqb);
				return new ResponseEntity<>(returnMap, HttpStatus.OK);
			}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECEVIVEDRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);	
		}
	}	
	
	@Override
	public ResponseEntity<Object> createGoodsInChecklist(final int ngoodsincode,final int controlCode,final int ndesigntemplatemappingcode,final GoodsInChecklist objGoodsInChecklist, final UserInfo userInfo) throws Exception {
		
		//List<GoodsInChecklist> lstGoodsInChecklistCheck = new ArrayList<GoodsInChecklist>();
		
		
		String goodsinQuery = " select gh.ntransactionstatus from goodsin gi  "
							+ " join goodsinhistory gh on gi.ngoodsincode =gh.ngoodsincode "
							+ " join transactionstatus ts on ts.ntranscode = gh.ntransactionstatus "
							+ " where "
							+ " gh.ngoodsinhistorycode =any (select max(ngoodsinhistorycode) from goodsinhistory where ngoodsincode ="+ngoodsincode+" and  nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
							+ " and gi.ngoodsincode ="+ngoodsincode+" and gi.nsitecode ="+userInfo.getNtranssitecode()+" and gh.nsitecode="+userInfo.getNtranssitecode()+" "
							+ " and gi.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		List<GoodsIn> list =jdbcTemplate.query(goodsinQuery, new GoodsIn());
		
		if(list.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
			ObjectMapper mapper = new ObjectMapper();			
			String insertQuery= "";
			String valuesString = "";
			GoodsInChecklist lstGoodsInChecklistCheck = null;			

			String str = "select * from goodsinchecklist where nsitecode ="+userInfo.getNtranssitecode()+" and ngoodsincode ="+ngoodsincode+" ";
			List<GoodsInChecklist> lstObject =jdbcTemplate.query(str, new GoodsInChecklist());
			
			if(!lstObject.isEmpty()) {
				lstGoodsInChecklistCheck = (GoodsInChecklist) lstObject.get(0);
				if (lstGoodsInChecklistCheck !=null) {
					String updateQuery = "update goodsinchecklist set jsondata='"+stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objGoodsInChecklist.getJsondata()))+"'"
									   + "where nsitecode ="+userInfo.getNtranssitecode()+" and ngoodsincode ="+ngoodsincode+" ";
					jdbcTemplate.execute(updateQuery);
				}
			}
			if(lstGoodsInChecklistCheck ==null) {
				insertQuery = "insert into goodsinchecklist (ngoodsincode,nchecklistversioncode,jsondata,nsitecode,nstatus) values ";
				
				valuesString += "(" + (ngoodsincode) + ", "+ objGoodsInChecklist.getNchecklistversioncode() + ",'"
						+  stringUtilityFunction.replaceQuote(mapper.writeValueAsString(objGoodsInChecklist.getJsondata())) + "',"+userInfo.getNtranssitecode()+","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				valuesString = valuesString.substring(0, valuesString.length() - 1);
				jdbcTemplate.execute(insertQuery + valuesString);
			}
			return getActiveGoodsInById(ngoodsincode, userInfo);

		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECEVIVEDRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);

		}
		
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object>getGoodsInSampleDetails(int ngoodsincode,int ndesigntemplatemappingcode,UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		List<Map<String, Object>> lstSampleGet = new ArrayList<>();
		String Query = " select json_agg(a.jsonuidata)  from (select "
					 + " gs.jsonuidata "
					 + " ||json_build_object('ngoodsinsamplecode',gs.ngoodsinsamplecode)::jsonb "
					 + " ||json_build_object('ngoodsincode',gs.ngoodsincode)::jsonb "
					 + " ||json_build_object('ndesigntemplatemappingcode',gs.ndesigntemplatemappingcode)::jsonb "
					 + " ||json_build_object('sexternalsampleid',gs.sexternalsampleid)::jsonb "
					 + " ||json_build_object('nquantity',gs.nquantity)::jsonb "
					 + " ||json_build_object('nunitcode',gs.nunitcode)::jsonb "
					 + " ||json_build_object('scomments',gs.scomments)::jsonb "
					 + " ||json_build_object('sunitname',u.sunitname):: jsonb as jsonuidata"
					 + " from goodsinsample gs,goodsin g,unit u,designtemplatemapping dm  "
					 + " where g.ngoodsincode =gs.ngoodsincode "
					 + " and u.nunitcode =gs.nunitcode "
					 + " and gs.ndesigntemplatemappingcode =dm.ndesigntemplatemappingcode"
					 + " and gs.ngoodsincode ="+ngoodsincode+" and gs.ndesigntemplatemappingcode="+ndesigntemplatemappingcode+" "
					 + " and gs.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and g.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and dm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					 + " and gs.nsitecode ="+userInfo.getNtranssitecode()+" and g.nsitecode ="+userInfo.getNtranssitecode()+" and dm.nsitecode ="+userInfo.getNmastersitecode()+" and u.nsitecode ="+userInfo.getNmastersitecode()+")a ";

		String testListString = (String) jdbcUtilityFunction.queryForObject(Query, String.class, jdbcTemplate);
		if (testListString != null) {
			lstSampleGet = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo,true, ndesigntemplatemappingcode, "sample");
		}		
		map.put("GoodsInSample", lstSampleGet);		
		return new ResponseEntity<>(map,HttpStatus.OK);

	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>>getGoodsInSampleAuditGet(int ngoodsinsamplecode, int ngoodsincode, int ndesigntemplatemappingcode,UserInfo userInfo) throws Exception {
		final Map<String,Object>map =new HashMap<String, Object>();
		List<Map<String, Object>> lstSampleGet = new ArrayList<>();
		String Query = " select json_agg(a.jsonuidata)  from (select "
					 + " gs.jsonuidata "
					 + " ||json_build_object('ngoodsinsamplecode',gs.ngoodsinsamplecode)::jsonb "
					 + " ||json_build_object('ngoodsincode',gs.ngoodsincode)::jsonb "
					 + " ||json_build_object('ndesigntemplatemappingcode',gs.ndesigntemplatemappingcode)::jsonb "
					 + " ||json_build_object('sexternalsampleid',gs.sexternalsampleid)::jsonb "
					 + " ||json_build_object('nquantity',gs.nquantity)::jsonb "
					 + " ||json_build_object('nunitcode',gs.nunitcode)::jsonb "
					 + " ||json_build_object('scomments',gs.scomments)::jsonb "
					 + " ||json_build_object('sunitname',u.sunitname):: jsonb as jsonuidata"
					 + " from goodsinsample gs,goodsin g,unit u,designtemplatemapping dm "
					 + " where g.ngoodsincode =gs.ngoodsincode "
					 + " and u.nunitcode =gs.nunitcode "
					 + " and gs.ndesigntemplatemappingcode =dm.ndesigntemplatemappingcode "
					 + " and gs.ngoodsincode ="+ngoodsincode+" and  gs.ndesigntemplatemappingcode ="+ndesigntemplatemappingcode+" and gs.ngoodsinsamplecode ="+ngoodsinsamplecode+" "
					 + " and gs.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and g.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and dm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					 + " and gs.nsitecode ="+userInfo.getNtranssitecode()+" and g.nsitecode ="+userInfo.getNtranssitecode()+" and dm.nsitecode ="+userInfo.getNmastersitecode()+" and u.nsitecode ="+userInfo.getNmastersitecode()+")a ";

		String testListString = (String) jdbcUtilityFunction.queryForObject(Query, String.class, jdbcTemplate);
		if (testListString != null) {
			lstSampleGet = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(testListString, userInfo,true, ndesigntemplatemappingcode, "sample");
		}		
		map.put("GoodsInSample", lstSampleGet);		
		return lstSampleGet;


	}	
	
	@Override
	public GoodsInSample getActiveGoodsInSampleById(int ngoodsinsamplecode, UserInfo userInfo) throws Exception {
		final String strQuery = " select gs.*,u.sunitname from goodsinsample gs,goodsin g, unit u "
							  + " where gs.ngoodsincode =g.ngoodsincode "
							  + " and gs.nunitcode =u.nunitcode "
							  + " and g.nsitecode ="+userInfo.getNtranssitecode()+" and gs.nsitecode ="+userInfo.getNtranssitecode()+" and u.nsitecode ="+userInfo.getNmastersitecode()+" and gs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + " and g.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
							  + " gs.ngoodsinsamplecode ="+ngoodsinsamplecode;
		return (GoodsInSample) jdbcUtilityFunction.queryForObject(strQuery, GoodsInSample.class, jdbcTemplate);
	}
			
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createGoodsInSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final String strQuery ="lock table lockgoodsin "+Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+" ";
		jdbcTemplate.execute(strQuery);
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		final List<Object> savedGoodsInList = new ArrayList<>();	
		final GoodsInSample objGoodsInSample = objmapper.convertValue(inputMap.get("GoodsInSample"), GoodsInSample.class);	
		
		JSONObject jsonObject = new JSONObject(objGoodsInSample.getJsondata());
		JSONObject jsonUIObject = new JSONObject(objGoodsInSample.getJsonuidata());
		final List<Map<String, Object>> UniqueValidation = objmapper.convertValue(inputMap.get("combinationunique"), new TypeReference<List<Map<String, Object>>>() {});
		
		Map<String, Object> map1 = projectDAOSupport.validateUniqueConstraint(UniqueValidation, (Map<String, Object>) inputMap.get("GoodsInSample"),
				userInfo, "create", GoodsInSample.class, "ngoodsinsamplecode", false);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),HttpStatus.CONFLICT);
		}else {

			int goodsinSampleSeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnogoodsinmanagement where stablename='goodsinsample'", Integer.class, jdbcTemplate);
			goodsinSampleSeqNo++;		
			
			String goodsinsampleInsert ="Insert into goodsinsample(ngoodsinsamplecode,ngoodsincode,ndesigntemplatemappingcode,sexternalsampleid,nquantity,nunitcode,"
									  + "scomments,jsondata,jsonuidata,nsitecode,nstatus)"
									  + "values("+goodsinSampleSeqNo+","+objGoodsInSample.getNgoodsincode()+","+objGoodsInSample.getNdesigntemplatemappingcode()+","
									  + "N'"+stringUtilityFunction.replaceQuote(objGoodsInSample.getSexternalsampleid())+"', "+objGoodsInSample.getNquantity()+","+objGoodsInSample.getNunitcode()+",N'"+stringUtilityFunction.replaceQuote(objGoodsInSample.getScomments())+"',"
									  + " '"+stringUtilityFunction.replaceQuote(jsonObject.toString())+"'::JSONB,'"+stringUtilityFunction.replaceQuote(jsonUIObject.toString())+"'::JSONB,"
									  + " "+userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" )";
			jdbcTemplate.execute(goodsinsampleInsert);				   

			jdbcTemplate.execute("update seqnogoodsinmanagement set nsequenceno = " + goodsinSampleSeqNo + " where stablename='goodsinsample'");

			objGoodsInSample.setNgoodsinsamplecode(goodsinSampleSeqNo);				    
			savedGoodsInList.add(objGoodsInSample);
			List<Map<String,Object>> lstDataTest = getGoodsInSampleAuditGet(objGoodsInSample.getNgoodsinsamplecode(),objGoodsInSample.getNgoodsincode(),objGoodsInSample.getNdesigntemplatemappingcode(), userInfo);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode",-1);
			auditmap.put("ndesigntemplatemappingcode",objGoodsInSample.getNdesigntemplatemappingcode());
			actionType.put("goodsinsample", "IDS_ADDGOODSINSAMPLE");
			jsonAuditObject.put("goodsinsample", lstDataTest);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);			
			return getGoodsInSampleDetails(objGoodsInSample.getNgoodsincode(),objGoodsInSample.getNdesigntemplatemappingcode(),userInfo);	
		}
			

	}
	
	@Override
	public ResponseEntity<Object> deleteGoodsInSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		
		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();	
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		
		final List<Object> deleteGoodsInList = new ArrayList<>();
		int ngoodsincode = (int) inputMap.get("ngoodsincode");
		int ngoodsinsamplecode = (int) inputMap.get("ngoodsinsamplecode");
		int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		int ntransactionstatus =  (int) inputMap.get("ntransactionstatus");	
		
		ResponseEntity<Object> obj =getActiveGoodsInById(ngoodsincode, userInfo);
		if(obj.getBody().equals("Already Deleted")) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
					userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else {
			final GoodsInSample goodsinsample = getActiveGoodsInSampleById(ngoodsinsamplecode, userInfo);

			if(goodsinsample==null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
						userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}else {
				if(ntransactionstatus ==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					
					List<Map<String,Object>> lstDataTest = getGoodsInSampleAuditGet(goodsinsample.getNgoodsinsamplecode(),goodsinsample.getNgoodsincode(),goodsinsample.getNdesigntemplatemappingcode(), userInfo);
							
					String deleteQuery = " update goodsinsample set nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									   + " where nsitecode ="+userInfo.getNtranssitecode()+" and ngoodsincode = " + ngoodsincode + " and ngoodsinsamplecode ="+ ngoodsinsamplecode +" ";

					jdbcTemplate.execute(deleteQuery);
					deleteGoodsInList.add(goodsinsample);	
					
				
					
					auditmap.put("nregtypecode", -1);
					auditmap.put("nregsubtypecode",-1);
					auditmap.put("ndesigntemplatemappingcode",goodsinsample.getNdesigntemplatemappingcode());
					
					actionType.put("goodsinsample", "IDS_DELETEGOODSINSAMPLE");
					jsonAuditObject.put("goodsinsample", lstDataTest);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);						
					return getGoodsInSampleDetails(ngoodsincode,ndesigntemplatemappingcode,userInfo);		
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}				
			}			

		}
	}
	
//	@SuppressWarnings({ "unused", "unchecked" })
//	@Override
//	public ResponseEntity<Object> importGoodsInSample(MultipartHttpServletRequest request, UserInfo userInfo)throws Exception {
//		
//		int ntransactionstatus = Integer.parseInt(request.getParameter("ntransactionstatus"));
//		
//		if(ntransactionstatus ==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECEIVERECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//		}else {
//			MultipartFile objmultipart = request.getFile("uploadedFile");
//			int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
//			int ngoodsincode = Integer.parseInt(request.getParameter("ngoodsincode"));
//			InputStream objinputstream = objmultipart.getInputStream();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte[] buffer = new byte[1024];
//			int len;
//			while ((len = objinputstream.read(buffer)) > -1) {
//				baos.write(buffer, 0, len);
//			}
//			baos.flush();
//			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
//			Workbook workbook = WorkbookFactory.create(is1);
//			Sheet sheet = workbook.getSheetAt(0);
//			int rowIndex = 0;
//			String query = " Insert into goodsinsample (ngoodsinsamplecode,ngoodsincode,ndesigntemplatemappingcode,"
//						 + " sexternalsampleid,nquantity,nunitcode,scomments,jsondata,jsonuidata,nsitecode,nstatus) values ";
//			List<String> lstHeader = new ArrayList<>();
//			ObjectMapper objectMapper = new ObjectMapper();
//
//			JSONArray dateConstraints = new JSONArray(request.getParameter("dateconstraints"));
//			JSONArray uniqueValidation = new JSONArray(request.getParameter("combinationunique"));
//			JSONArray dateList = new JSONArray(request.getParameter("datelist"));
//			JSONObject mandatoryFields =new JSONObject(request.getParameter("mandatoryFields"));
//			JSONArray comboComponents = new JSONArray(request.getParameter("comboComponents"));
//			JSONArray exportFields = new JSONArray(request.getParameter("exportFields"));
//			final List<Map<String, Object>> comboComponents1 = objectMapper.readValue(comboComponents.toString(),new TypeReference<List<Map<String, Object>>>() {});
//			final List<Map<String, Object>> exportFields1 = objectMapper.readValue(exportFields.toString(),new TypeReference<List<Map<String, Object>>>() {});
//
//			Map<String,Object> mapData=new HashMap<String,Object>();
//			
//			String sampleId="";
//			Integer Quantity=0;
//			Integer Unit=-1;
//			String Comments="";
//			
//			final String getSequenceNo = "select nsequenceno from seqnogoodsinmanagement where stablename ='goodsinsample'";
//			int seqNo = getJdbcTemplate().queryForObject(getSequenceNo, Integer.class);
//			
//			for(Row row :sheet) {
//				if(rowIndex >0) {
//					int cellIndex = 0;
//					JSONObject objJsonUiData = new JSONObject();
//					JSONObject objJsonData= new JSONObject();
//					for(String field : lstHeader) {
//						if(row.getCell(cellIndex)!=null && row.getCell(cellIndex).getCellType() != Cell.CELL_TYPE_BLANK) {
//							System.out.println(row.getCell(cellIndex).getClass());
//							Cell cell = row.getCell(cellIndex);
//							System.out.println("1"+cell);
//							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
//								objJsonUiData.put(field, cell.getStringCellValue());
//							}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//								if (DateUtil.isCellDateFormatted(cell)) {
//									SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//									objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
//								} else {
//									objJsonUiData.put(field, cell.getNumericCellValue());
//									
//								}
//							}else if(DateUtil.isCellDateFormatted(cell)) {
//								if(cell.getDateCellValue()!=null) {
//									SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//									objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
//								}else {
//									objJsonUiData.put(field,"");
//								}
//							}
//							objJsonData.put(field, objJsonUiData.get(field));
//
//
//						}else {
//							Cell cell1 = sheet.getRow(0).getCell(cellIndex);
//							String fieldString=String.valueOf(cell1.getStringCellValue());
//							if(fieldString.contains("(dd-mm-yyy)")) {
//								objJsonUiData.put(field,"");
//								objJsonData.put(field, "");
//							}else {
//								if((boolean)mandatoryFields.has(field) && (boolean)mandatoryFields.get(field)) {
//									 return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENTER" +' '+field, userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//								}
//								if(cell1.getCellType()==Cell.CELL_TYPE_NUMERIC) {
//									objJsonUiData.put(field,0);
//									objJsonData.put(field, 0);
//								}else if(cell1.getCellType()==Cell.CELL_TYPE_STRING){
//									objJsonUiData.put(field,"");
//									objJsonData.put(field, "");
//								}						
//							}
//						}
//							
//						List<Map<String, Object>> lst = comboComponents1.stream().filter(x -> ((String) x.get("label")).equals(field)).collect(Collectors.toList());
//						
//						if(!lst.isEmpty()) {
//							
//							 boolean multiLingual =false;	
//						     if(lst.get(0).containsKey("isMultiLingual")) {
//								 multiLingual = (boolean) lst.get(0).get("isMultiLingual");
//
//						     }
//							 String masterQuery = "";
//							
//							if(multiLingual) {
//								 masterQuery = "select * from "+lst.get(0).get("source")+" where jsondata -> '"+lst.get(0).get("displaymember")+"'->>'"+userInfo.getSlanguagetypecode()+"'='"+objJsonUiData.get(field)+"'"
//									 		+ "and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
//							}else {
//								 masterQuery = "select * from " + lst.get(0).get("source") + " where \""
//											+ lst.get(0).get("displaymember") + "\"='" + objJsonUiData.get(field) + "' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//							}
//							
//							
//							List<?> lstData =jdbcTemplate.queryForList(masterQuery);
//							if(!lstData.isEmpty()) {
//								mapData=(Map<String, Object>) lstData.get(0);
//								JSONObject newData=new JSONObject();
//								newData.put("pkey", lst.get(0).get("valuemember"));
//								
//								if(multiLingual) {
//									PGobject pgObject = (PGobject) mapData.get("jsondata");
//				                    Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
//				                    Map<String, Object> list1 = (Map<String, Object>) (list.get(lst.get(0).get("displaymember")));
//				                    String list2 = (String) (list1.get(userInfo.getSlanguagetypecode()));								
//									newData.put("label",list2);
//								}else {
//									newData.put("label",mapData.get(lst.get(0).get("displaymember")) );
//								}
//								
//								newData.put("value",mapData.get(lst.get(0).get("valuemember")));
//								newData.put("source", lst.get(0).get("source"));
//								newData.put("nquerybuildertablecode", lst.get(0).get("nquerybuildertablecode"));
//								
//								if(multiLingual) {
//									PGobject pgObject = (PGobject) mapData.get("jsondata");
//				                    Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
//				                    Map<String, Object> list1 = (Map<String, Object>) (list.get(lst.get(0).get("displaymember")));
//				                    String list2 = (String) (list1.get(userInfo.getSlanguagetypecode()));
//									objJsonUiData.put(field,list2);
//
//								}else {
//									objJsonUiData.put(field, mapData.get(lst.get(0).get("displaymember")));
//								}
//								
//								objJsonData.put(field, newData);
//								
//							}else {
//								return new ResponseEntity<>(objJsonUiData.get(field) + " " + commonFunction.getMultilingualMessage("IDS_VALUENOTPRESENTINPARENT",
//								userInfo.getSlanguagefilename())+" ("+lst.get(0).get("label")+")",HttpStatus.CONFLICT);
//							}
//							
//						}					
//						
//						if(field.equals("External Sample Id")) {
//							//sampleId=row.getCell(cellIndex).toString();							
//							Cell cell = row.getCell(cellIndex);
//							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
//								sampleId=cell.getStringCellValue();
//							}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {	
//								sampleId = NumberToTextConverter.toText(cell.getNumericCellValue());
//							}
//						}
//						if(field.equals("Quantity")) {
//							Cell cell1 = row.getCell(cellIndex);
//							 Quantity =(int) cell1.getNumericCellValue(); 
//						}
//						if(field.equals("Unit")) {
//							Unit=(Integer) mapData.get(lst.get(0).get("valuemember"));
//						}
//						if(field.equals("Comments")) {
//							if(row.getCell(cellIndex) !=null) {
//								//Comments=row.getCell(cellIndex).toString();
//								Cell cell = row.getCell(cellIndex);
//								if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
//									Comments=cell.getStringCellValue();
//								}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									Comments = NumberToTextConverter.toText(cell.getNumericCellValue());
//								}								
//							}else {
//								Comments="";
//							}
//						}						
//						cellIndex++;
//					}
//					List<String> dateList1 = objectMapper.readValue(dateList.toString(), new TypeReference<List<String>>() {
//					});
//
//					objJsonUiData = (JSONObject) convertInputDateToUTCByZone(objJsonUiData, dateList1, false, userInfo);
//					objJsonData = (JSONObject) convertInputDateToUTCByZone(objJsonData, dateList1, false, userInfo);
//					
//					JSONObject objJson1 = new JSONObject(objJsonUiData.toString());
//					List<Map<String, Object>> dateConstraints1 = objectMapper.readValue(dateConstraints.toString(), new TypeReference<List<Map<String, Object>>>() {});
//					final Map<String, Object> dateValidateMap = commonFunction.validateDynamicDateContraints(objJsonUiData,dateConstraints1, userInfo);
//					if (!((String) dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())).equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
//						return new ResponseEntity<>(dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),HttpStatus.CONFLICT);
//
//					}
//					Map<String, Object> map1 = new HashMap<>();
//					Map<String, Object> map2 = objectMapper.readValue(objJson1.toString(), Map.class);
//					map1.put("jsonuidata", map2);
//					List<Map<String, Object>> uniqueValidation1 = objectMapper.readValue(uniqueValidation.toString(), new TypeReference<List<Map<String, Object>>>() {});
//					
//					Map<String, Object> map = registrationDAO.validateUniqueConstraint(uniqueValidation1, map1,	userInfo, "create", GoodsInSample.class, "ngoodsinsamplecode", false);
//
//					if (!map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()).equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
//						return new ResponseEntity<>(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),HttpStatus.CONFLICT);
//					}
//					
//					
//					seqNo++;
//					query =  query +(rowIndex== 1 ?"(":",(")+(seqNo)+","+ngoodsincode+","+ndesigntemplatemappingcode+",'"+sampleId+"',"+Quantity+","+Unit+",'"+Comments+"', "
//						  + " '"+ReplaceQuote(objJsonData.toString())+"'::JSONB,'"+ReplaceQuote(objJsonUiData.toString())+"'::JSONB,"+userInfo.getNtranssitecode()+",1)";
//					
//					
//
//					
//				}else {
//					int cellIndex = 0;
//					for (Cell cell : row) {
//						String header = cell.getStringCellValue();	
//						header = header.substring(header.indexOf('(')+1,header.indexOf(')'));					
//						lstHeader.add(header);
//						cellIndex++;
//					}
//				}
//				rowIndex++;
//			}
//			
//			if(sheet.getLastRowNum()==0 && sheet.getRow(rowIndex) ==null) {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NORECORDINTHEEXCELSHEET",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
//			}
//			
//			getJdbcTemplate().execute(
//					query + "; update seqnogoodsinmanagement set nsequenceno = " + seqNo + " where stablename='goodsinsample'");
//			
//			Map<String,Object> inputMap =new HashMap<String,Object>();
//			inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
//			return getGoodsInSampleDetails(ngoodsincode,ndesigntemplatemappingcode,userInfo);		
//		}		
//	}
	
	
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public ResponseEntity<Object> importGoodsInSample(MultipartHttpServletRequest request, UserInfo userInfo)throws Exception {
		
		int ntransactionstatus = Integer.parseInt(request.getParameter("ntransactionstatus"));
		
		if(ntransactionstatus ==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECEIVERECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			MultipartFile objmultipart = request.getFile("uploadedFile");
			int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
			int ngoodsincode = Integer.parseInt(request.getParameter("ngoodsincode"));
			InputStream objinputstream = objmultipart.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = objinputstream.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			Workbook workbook = WorkbookFactory.create(is1);
			Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = 0;
			String query = " Insert into goodsinsample (ngoodsinsamplecode,ngoodsincode,ndesigntemplatemappingcode,"
						 + " sexternalsampleid,nquantity,nunitcode,scomments,jsondata,jsonuidata,nsitecode,nstatus) values ";
			List<String> lstHeader = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();

			JSONArray dateConstraints = new JSONArray(request.getParameter("dateconstraints"));
			JSONArray uniqueValidation = new JSONArray(request.getParameter("combinationunique"));
			JSONArray dateList = new JSONArray(request.getParameter("datelist"));
			JSONObject mandatoryFields =new JSONObject(request.getParameter("mandatoryFields"));
			JSONArray comboComponents = new JSONArray(request.getParameter("comboComponents"));
			JSONArray exportFields = new JSONArray(request.getParameter("exportFields"));
			JSONArray exportFieldProperties = new JSONArray(request.getParameter("exportFieldProperties"));
			final List<Map<String, Object>> comboComponents1 = objectMapper.readValue(comboComponents.toString(),new TypeReference<List<Map<String, Object>>>() {});
			final List<Map<String, Object>> exportFieldProperties1 = objectMapper.readValue(exportFieldProperties.toString(),new TypeReference<List<Map<String, Object>>>() {});

			Map<String,Object> mapData=new HashMap<String,Object>();
			Map<String,Object> responseMap = new LinkedHashMap<String,Object>();
			String responseString="";
			
			String sampleId="";
			String Quantity = "";
			Integer Unit=-1;
			String Comments="";
			
			final String getSequenceNo = "select nsequenceno from seqnogoodsinmanagement where stablename ='goodsinsample'";
			int seqNo = (int) jdbcUtilityFunction.queryForObject(getSequenceNo, Integer.class, jdbcTemplate);
			
			for(Row row :sheet) {
				if(rowIndex >0) {
					int cellIndex = 0;
					JSONObject objJsonUiData = new JSONObject();
					JSONObject objJsonData= new JSONObject();
					for(String field : lstHeader) {
						System.out.println("cell count" + cellIndex + " " + field);
						//logging.info(row.getCell(cellIndex));
						//Modified by SYED-22-APR-2025
						CellBase cell = (CellBase) row.getCell(cellIndex);
						
						Map<String, Object> objExpFldProps = new HashMap<String, Object>();						
						 if (!exportFieldProperties1.isEmpty()) {
								List<Map<String, Object>> expfieldprops = exportFieldProperties1.stream()
										.filter(x -> ((String) x.get("label")).equals(field)).collect(Collectors.toList());
								if (!expfieldprops.isEmpty()) {
									objExpFldProps = expfieldprops.get(0);
								}
							}
						 responseMap = (Map<String,Object>)transactionDAOSupport.importValidData(objExpFldProps, cell,field,userInfo);
							
						responseString=(String)responseMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
						
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()!=responseString) {
							return new ResponseEntity<>(responseString,HttpStatus.EXPECTATION_FAILED);
						}
						
						if(objExpFldProps.get("inputtype").toString().equals("combo")) {
							objJsonUiData.put(field, responseMap.get("displaymember"));
							objJsonData.put(field, responseMap.get("objJsonData"));
						}else {
							objJsonUiData.put(field, responseMap.get("cellData"));
							objJsonData.put(field, objJsonUiData.get(field));
						}					
						
						if(field.equals("External Sample Id")) {							
							sampleId =(String) responseMap.get("cellData");							
						}
						if(field.equals("Quantity")) {	
							 Quantity =(String) responseMap.get("cellData");		
							// Quantity = Integer.parseInt((String) responseMap.get("cellData"));							 
						}
						if(field.equals("Unit")) {
					        JSONObject jsonObj = (JSONObject) responseMap.get("objJsonData");
					        Unit= (Integer) jsonObj.get("value");					       
						}
						if(field.equals("Comments")) {
							Comments =(String) responseMap.get("cellData");							
						}						
						cellIndex++;
					}
			
					
					JSONObject objJson1 = new JSONObject(objJsonUiData.toString());
					
					Map<String, Object> map1 = new HashMap<>();
					Map<String, Object> map2 = objectMapper.readValue(objJson1.toString(), Map.class);
					map1.put("jsonuidata", map2);
					List<Map<String, Object>> uniqueValidation1 = objectMapper.readValue(uniqueValidation.toString(), new TypeReference<List<Map<String, Object>>>() {});
					Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(uniqueValidation1, map1,	userInfo, "create", GoodsInSample.class, "ngoodsinsamplecode", false);

					if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
						return new ResponseEntity<>(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),HttpStatus.CONFLICT);
					}
					
					
					seqNo++;
					query =  query +(rowIndex== 1 ?"(":",(")+(seqNo)+","+ngoodsincode+","+ndesigntemplatemappingcode+",'"+sampleId+"',"+Quantity+","+Unit+",'"+Comments+"', "
						  + " '"+stringUtilityFunction.replaceQuote(objJsonData.toString())+"'::JSONB,'"+stringUtilityFunction.replaceQuote(objJsonUiData.toString())+"'::JSONB,"+userInfo.getNtranssitecode()+",1)";
					
					

					
				}else {
					int cellIndex = 0;
					for (Cell cell : row) {
						String header = cell.getStringCellValue();	
						header = header.substring(header.indexOf('(')+1,header.indexOf(')'));					
						lstHeader.add(header);
						cellIndex++;
					}
				}
				rowIndex++;
			}
			
			if(sheet.getLastRowNum()==0 && sheet.getRow(rowIndex) ==null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NORECORDINTHEEXCELSHEET",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			
			jdbcTemplate.execute(
					query + "; update seqnogoodsinmanagement set nsequenceno = " + seqNo + " where stablename='goodsinsample'");
			
			Map<String,Object> inputMap =new HashMap<String,Object>();
			inputMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
			return getGoodsInSampleDetails(ngoodsincode,ndesigntemplatemappingcode,userInfo);		
		}		
	}
	
	
	

	@Override
	public ResponseEntity<Object> goodsinReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		Map<String, Object> sNodeServerStart = null;
		
		sNodeServerStart = reportDAOSupport.validationCheckForNodeServer(inputMap,userInfo);
			
		if(sNodeServerStart.get("rtn").equals("Failed")) {
		  return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			
			final String countQuery =" select * from goodsin gi,goodsinhistory gh "
									+" where gi.ngoodsincode= gh.ngoodsincode "
									+" and gh.ngoodsinhistorycode in(select max(ngoodsinhistorycode) from goodsinhistory "
									+" where ngoodsincode ="+inputMap.get("ngoodsincode")+" and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNtranssitecode()+") "
									+" and gi.ngoodsincode="+inputMap.get("ngoodsincode")+" and gi.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and gh.nsitecode = "+userInfo.getNtranssitecode()+" "
									+" and gi.nsitecode ="+userInfo.getNtranssitecode()+" ";

			
			List<GoodsIn> lst =jdbcTemplate.query(countQuery, new GoodsIn());
			
			if(lst.size() >0) {
				
				if(lst.get(0).getNtransactionstatus()== Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDOENLOADRECEIVEAPPROVE",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
				
				final String countQuery1 = "select count(ngoodsincode) from goodsinchecklist where ngoodsincode="+inputMap.get("ngoodsincode")+"  and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNtranssitecode();
				int count1 = (int) jdbcUtilityFunction.queryForObject(countQuery1, Integer.class, jdbcTemplate);
				
				if(count1 !=0) {
					Map<String, Object> returnMap = new HashMap<>();
					String sfileName = "";
					String sJRXMLname = "";
//					int nregTypeCode = -1;
//					int nregSubTypeCode = -1;
//					int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
//					int ncertificateTypeCode = -1;
//					String scertificateType = "";
//					String sversionno = "";
//					String sreportComments = "";
//					String query = "";
					int qType = 1;
					int ncontrolCode = -1;
//					int nreportDetailCode = -1;
//					String systemFileName = "";
//					String pdfPrefix = "";
//					ObjectMapper objMapper = new ObjectMapper();
					String sfilesharedfolder = "";
					String fileuploadpath = "";
					String subReportPath = "";
					String imagePath = "";
					String pdfPath = "";
					String sreportingtoolURL= "";
					
					final String getFileuploadpath = "select nreportsettingcode,ssettingvalue from reportsettings where nreportsettingcode in ("
							+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
							+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
							+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") "
							+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" order by nreportsettingcode";
					
					
					/*
					List<String> reportPaths = jdbcUtilityFunction.queryForList(getFileuploadpath, String.class);
					fileuploadpath = reportPaths.get(0);
					subReportPath = reportPaths.get(0);
					imagePath = reportPaths.get(0);
					pdfPath = reportPaths.get(1);
					sreportingtoolURL = reportPaths.get(2);
					 */
					
					//modified by SYED 22-APR-2025
					List<ReportSettings> lstreportPath = jdbcTemplate.query(getFileuploadpath, new ReportSettings());
					Map<Short, String> reportPaths = lstreportPath.stream().collect(
							Collectors.toMap(ReportSettings::getNreportsettingcode, ReportSettings::getSsettingvalue));
					fileuploadpath = reportPaths.get((short)1);
					subReportPath = reportPaths.get((short)1);
					imagePath = reportPaths.get((short)1);
					pdfPath = reportPaths.get((short)4);
					sreportingtoolURL = reportPaths.get((short)13);
										
					

//					if (!userInfo.getSreason().isEmpty()) {
//						sreportComments = userInfo.getSreason();
//					}
//					if (inputMap.containsKey("sreportcomments")) {
//						sreportComments = (String) inputMap.get("sreportcomments");
//					}
		//
//					if (inputMap.containsKey("nregtypecode")) {
//						nregTypeCode = (int) inputMap.get("nregtypecode");
//					}
					if (inputMap.containsKey("ncontrolcode")) {
						ncontrolCode = (int) inputMap.get("ncontrolcode");
					}
//					if (inputMap.containsKey("nregsubtypecode")) {
//						nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
//					}

//					if (inputMap.containsKey("ssystemfilename")) {
//						systemFileName = (String) inputMap.get("ssystemfilename");
//					}

					sJRXMLname = "SpecReport.jrxml";
					sfileName = "SpecReport_" + inputMap.get("nallottedspeccode");
					;
					inputMap.put("ncontrolcode",
							(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
									? inputMap.get("ncontrolcode")
									: ncontrolCode);

					UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
					userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
					Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap, userInfoWithReportFormCode);
					String folderName = "";

						sJRXMLname = (String) dynamicReport.get("JRXMLname");
//						nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
						folderName = (String) dynamicReport.get("folderName");
						fileuploadpath = fileuploadpath + folderName;

					sfilesharedfolder = fileuploadpath + sJRXMLname;
					File JRXMLFile = new File(sfilesharedfolder);
					if (sJRXMLname != null && !sJRXMLname.equals("")) {
						
						Map<String, Object> jasperParameter = new HashMap<>();
						jasperParameter.put("ssubreportpath", subReportPath + folderName);
						jasperParameter.put("simagepath", imagePath + folderName);
						jasperParameter.put("sreportingtoolURL",sreportingtoolURL);
						jasperParameter.put("language",userInfo.getSlanguagetypecode());
						jasperParameter.put("ngoodsincode", (int) inputMap.get("ngoodsincode"));
						jasperParameter.put("sprimarykeyname", (int) inputMap.get("ngoodsincode"));
						jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));


						returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName, userInfo,
									"", ncontrolCode, false, "", "", ""));
						
						String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
							returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
							String auditAction ="";
							String comments = "";

							
							   Map<String, Object> outputMap = new HashMap<>(); 
							  outputMap.put("stablename", "goodsin"); 
							  outputMap.put("sprimarykeyvalue", inputMap.get("ngoodsincode"));
							  auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
							 }else {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKPDFCONNECTION",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
							 }
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTVALUE",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

					}
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				}else {
					if(lst.get(0).getNtransactionstatus()== Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDTOCHECKLIST",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHECKLISTNOTADDED",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}
					
				}
				
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTVALUE",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
			
			
			

		}
		
		
		
    }
}
