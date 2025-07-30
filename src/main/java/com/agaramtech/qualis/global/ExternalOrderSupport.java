package com.agaramtech.qualis.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.externalorder.model.ExternalOrderAttachment;
import com.agaramtech.qualis.externalorder.model.ExternalOrderSample;
import com.agaramtech.qualis.externalorder.model.ExternalOrderTest;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.service.RegistrationDAOSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class ExternalOrderSupport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOrderSupport.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final RegistrationDAOSupport registrationDAOSupport;
	private final CommonFunction commonFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateOrderSampleStatus(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		
		List<Map<String,Object>> lstPortalStatus = (List<Map<String, Object>>) inputMap.get("PortalStatus");
		List<Map<String,Object>> lstSamplePortalStatus = inputMap.containsKey("SamplePortalStatus") ? 
				(List<Map<String,Object>>) inputMap.get("SamplePortalStatus") : null;
		
		String methodUrl = "UpdateMultiSampleStatus";
		List<Map<String,Object>> lstValueForPortal = lstPortalStatus;
		
		if(lstSamplePortalStatus != null) {
			methodUrl = "UpdateSampleCancelStatus";
			lstValueForPortal = lstSamplePortalStatus;
		}
		try {
			
			HttpClient httpClient = HttpClients.createDefault();
	        HttpPost httpPost = new HttpPost(inputMap.get("url").toString() + "/portal/"+methodUrl+"");
	        httpPost.setHeader("Content-Type", "application/json");
	        JSONArray jsonArray = new JSONArray(lstValueForPortal);
	        String jsonParams = (String) jsonArray.toString();
	        StringEntity entityValue = new StringEntity(jsonParams);
	        httpPost.setEntity(entityValue);
	        HttpResponse responseValue = httpClient.execute(httpPost);
	        int statusCode = responseValue.getStatusLine().getStatusCode();
	        if(statusCode == 200) {
	        	LOGGER.info("Portal Status sent succesfully");
	        } else {
	        	LOGGER.info("Error in sending status to portal");
	        }
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
//        	logger.info("Error in sending status to portal");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> externalOrderSampleExtisting(Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
	
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		returnMap.put("nage", inputMap.get("AgeData"));
		returnMap.put("ngendercode", inputMap.get("ngendercode"));
		returnMap.put("npreregno", inputMap.get("npreregnocount"));
		
		//ALPD-5933->Added by Vignesh R(03-06-2025)--500 error thrown while pre-reg with external order.
		returnMap.put("sdob", inputMap.get("sDob"));
		
		List<Object> listSave = new ArrayList<>();
		listSave.add(returnMap);
		
		inputMap.put("ageData", listSave);
		inputMap.put("RegistrationSample", ((List<Map<String, Object>>) inputMap.get("RegistrationSample")).get(0));
		returnMap = inputMap;
		
	//	registrationServiceImpl.createSubSample(inputMap);
	
		inputMap = returnMap;
	
		final String squerystatus = "select npreregno, ntransactionstatus from registrationhistory where"
									+ " nreghistorycode =any (select max(nreghistorycode) from registrationhistory r"
									+ " where r.npreregno in (" + returnMap.get("npreregno") + ")"
									+ " and nsitecode="	+ userInfo.getNmastersitecode() 
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by r.npreregno)"
									+ " and nsitecode="	+ userInfo.getNmastersitecode() 
									+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									;
		final List<RegistrationHistory> lstStatus = jdbcTemplate.query(squerystatus, new RegistrationHistory());
		
		inputMap.put("nfilterstatus", lstStatus.get(0).getNtransactionstatus());
		inputMap = registrationDAOSupport.getDynamicRegistration(inputMap, userInfo);
		inputMap.put("nflag", 1);
		
		final StringJoiner joinerSample = new StringJoiner(",");
		joinerSample.add(String.valueOf(returnMap.get("ntransactionsamplecode")));
		returnMap.put("ssamplecode", joinerSample.toString());
		
		if (Integer.valueOf(returnMap.get("nfilterstatus").toString()) == Enumeration.TransactionStatus.PREREGISTER
				.gettransactionstatus()) {
			inputMap.putAll(
					(Map<String, Object>) sampleOrderUpdate(returnMap, String.valueOf(returnMap.get("npreregno")),
							userInfo, Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
							Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(), true).getBody());
		} else {
			inputMap.putAll(
					(Map<String, Object>) sampleOrderUpdate(returnMap, String.valueOf(returnMap.get("npreregno")),
							userInfo, Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus(),
							Enumeration.TransactionStatus.REGISTERED.gettransactionstatus(), true).getBody());
		}
		final String squery = "update registrationhistory set dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "' where npreregno=" + returnMap.get("npreregno") + " and ntransactionstatus="
				+ lstStatus.get(0).getNtransactionstatus();
		jdbcTemplate.execute(squery);
		return (Map<String, Object>) inputMap;
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> sampleOrderUpdate(Map<String, Object> inputMap, final String spreregno,
			UserInfo userInfo, final int nportalstatuscode, final int nlimsstatuscode, final boolean flag)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<>();

		// sample order update to portal
		if (spreregno != "") {
			returnMap.putAll(
					(Map<String, Object>) getPortalValues(inputMap, spreregno, userInfo, nportalstatuscode).getBody());
		} else {
			returnMap.putAll(
					(Map<String, Object>) getPortalSampleValues(inputMap, userInfo, nportalstatuscode).getBody());
		}

		if (!returnMap.isEmpty()) {
			List<Map<String, Object>> objList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> objListSample = new ArrayList<Map<String, Object>>();
			objList = (List<Map<String, Object>>) returnMap.get("PortalStatus");
			objListSample = (List<Map<String, Object>>) returnMap.get("PortalSampleStatus");

//			List<Integer> newList = Arrays.asList(preregnoArray).stream().map(s -> Integer.parseInt(s))
//					.collect(Collectors.toList());

			if (objList != null && !objList.isEmpty()) {
				final String sorderseqno = objList.stream()
						.map(serialNumber -> String.valueOf(serialNumber.get("serialnumber")))
						.collect(Collectors.joining(","));
				final String sordersampleno = objListSample.size() > 0 ? objListSample.stream()
						.map(sample -> String.valueOf(sample.get("externalsampleid"))).collect(Collectors.joining(","))
						: "";

				if (flag) {
					updatePortalOrderStatus(sorderseqno, sordersampleno, (short) nlimsstatuscode,
							userInfo);
				}
			} else if (objListSample != null && !objListSample.isEmpty()) {
				final String nexternalOrderSampleCode = objListSample.size() > 0
						? objListSample.stream().map(sample -> String.valueOf(sample.get("nexternalordersamplecode")))
								.collect(Collectors.joining(","))
						: "";
				if (flag) {
					final String strUpdateQuery = "update externalordersample set ntransactionstatus=" + nlimsstatuscode
							+ " where nexternalordersamplecode in (" + nexternalOrderSampleCode + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(strUpdateQuery);
				}
			}
		}

		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getPortalValues(Map<String, Object> inputMap,String npreregno, UserInfo userInfo,int statusCode) throws Exception 
	{

		Map<String, Object> returnMap = new HashMap<String,Object>();

		final String isPortal = "select nportalrequired from sampletype where nsampletypecode = "
						+ inputMap.get("nsampletypecode") 
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SampleType objPortal = (SampleType) jdbcUtilityFunction.queryForObject(isPortal, SampleType.class, jdbcTemplate);

		if (objPortal.getNportalrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			final String strRegistrationQuery = "select npreregno from registration where npreregno in ("+npreregno
											+ ") and jsonuidata->>'nexternalordertypecode'::text = '"+Enumeration.ExternalOrderType.PORTAL.getExternalOrderType()
											+ "' and nsampletypecode="+inputMap.get("nsampletypecode")
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode="+userInfo.getNtranssitecode();
			final List<Integer> lstPreRegNo = jdbcTemplate.queryForList(strRegistrationQuery,
					Integer.class);

			if(lstPreRegNo.size() > 0) {
				//				inputMap.put("allPreregno", npreregno);
				inputMap.put("allPreregno", lstPreRegNo.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(",")));
				
				final Map<String, Object> registrationDatas = registrationDAOSupport.getRegistrationLabel(inputMap, userInfo);
				
				returnMap.put("isPortalData", registrationDatas.containsKey("isPortalData") ? registrationDatas.get("isPortalData") : false);
				
				if (!registrationDatas.isEmpty()) {
					List<Map<String, Object>> objlst = new ArrayList<Map<String, Object>>();
					List<Map<String, Object>> objlstsample = new ArrayList<Map<String, Object>>();
					Map<String, Object> objMap = new LinkedHashMap<String, Object>();
					List<String> myList = new ArrayList<String>(Arrays.asList(npreregno.split(",")));
					
					List<RegistrationSample> lstRegistrationSample = jdbcTemplate.query("select npreregno,ntransactionsamplecode,jsondata->>'sampleorderid' as sexternalsampleid from registrationsample where ntransactionsamplecode in ("+inputMap.get("ssamplecode")+")",new RegistrationSample());

					for(String nregistrationno :myList)
					{
						for(RegistrationSample Obj:lstRegistrationSample){
							if(Obj.getNpreregno() == Integer.parseInt(nregistrationno))
							{
								if(Obj.getSexternalsampleid() != null)
								{
									objMap = new LinkedHashMap<String, Object>();							
									objMap.put("externalsampleid", Obj.getSexternalsampleid());
									objMap.put("ntransactionsamlecode",Obj.getNtransactionsamplecode());
									objMap.put("npreregno",Obj.getNpreregno());
									objlstsample.add(objMap);
								}
							}
						}

						objMap = new LinkedHashMap<String, Object>();
						@SuppressWarnings("unchecked")
						Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
						.get(String.valueOf(nregistrationno));
						if(registrationData != null) {
							objMap.put("serialnumber", registrationData.get("sorderseqno"));
							objMap.put("statuscode",statusCode);
							objlst.add(objMap);
						}
					}
					returnMap.put("PortalStatus", objlst);
					returnMap.put("PortalSampleStatus", objlstsample);
					returnMap.put("url", inputMap.get("url").toString());
					updateOrderSampleStatus(userInfo, returnMap);

				}
			}
		}

		return new ResponseEntity<Object> (returnMap, HttpStatus.OK);
	}


	public ResponseEntity<Object> getPortalSampleValues(Map<String, Object> inputMap, UserInfo userInfo,int statusCode) throws Exception {

		Map<String, Object> returnMap = new HashMap<String,Object>();
		final String isPortal = "select nportalrequired from sampletype where nsampletypecode = "
							+ inputMap.get("nsampletypecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SampleType objPortal = (SampleType) jdbcUtilityFunction.queryForObject(isPortal, SampleType.class, jdbcTemplate);

		if (objPortal.getNportalrequired() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {	
			List<Map<String, Object>> objlstsample = new ArrayList<Map<String, Object>>();
			
//			final List<RegistrationSample> lstRegistrationSample = jdbcTemplate.query(
//					"select npreregno,ntransactionsamplecode,"
//					+ " jsondata->>'sampleorderid' as sexternalsampleid from registrationsample "
//					+ " where ntransactionsamplecode in ("+inputMap.get("ssamplecode")+")"
//					+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and nsitecode="+userInfo.getNtranssitecode(),new RegistrationSample());
			
			final String registrationSampleStr = "select jsondata->>'sampleorderid' as sexternalsampleid "
												+ " from registrationsample where ntransactionsamplecode "
												+ " in ("+inputMap.get("ssamplecode")+") and jsondata->>'nordertypecode'='"
												+ Enumeration.OrderType.EXTERNAL.getOrderType()+"'"
												+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and nsitecode="+userInfo.getNtranssitecode();
			
			final List<ExternalOrderSample> lstOrderSample = jdbcTemplate.query("select eos.sexternalsampleid, eo.sorderseqno, eos.nexternalordersamplecode "
					+ " from externalordersample eos, externalorder eo "
					+ " where eos.nexternalordercode=eo.nexternalordercode "
					+ " and eos.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and eo.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and eo.nsitecode="+userInfo.getNtranssitecode()
					+ " and eos.nsitecode="+userInfo.getNtranssitecode()
					+ " and sexternalsampleid in ("+registrationSampleStr+")" ,new ExternalOrderSample());

			if(lstOrderSample.size()>0) {
				lstOrderSample.stream().forEach(lst -> {
					Map<String, Object> objMap = new HashMap<String, Object>() {
						{put("serialnumber",lst.getSorderseqno());} 
						{put("statuscode",statusCode);} 
						{put("externalsampleid",lst.getSexternalsampleid());}
						{put("nexternalordersamplecode",lst.getNexternalordersamplecode());}
					};
					objlstsample.add(objMap);
				});

				returnMap.put("PortalStatus", new ArrayList<>());
				returnMap.put("PortalSampleStatus", objlstsample);
			}
		}

		return new ResponseEntity<Object> (returnMap, HttpStatus.OK);
	}
	

	
	public ResponseEntity<Object> updatePortalOrderStatus(final String sorderseqno,final String sordersampleno, 
				final short ntransactionstatus,final UserInfo userInfo) 
						throws Exception {
		
		Map<String,Object> returnMap = new HashMap<>();
		final String ordernoArray = Arrays.stream(sorderseqno.split(",")).map(str -> "'"+ stringUtilityFunction.replaceQuote(str)+ "'").collect(Collectors.joining(","));
		
		String updateQuery = "";
		updateQuery= updateQuery +"update externalorder set ntransactionstatus = "+ntransactionstatus+",dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
								+ " where sorderseqno in ("+ordernoArray+") "
								+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+"; ";
		if((int) ntransactionstatus == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()) {
			updateQuery = updateQuery + "update externalordersample set ntransactionstatus = "+ ntransactionstatus
										+ " where nexternalordercode=(select nexternalordercode from externalorder "
										+ " where sorderseqno in ("	+ ordernoArray+")"
										+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode="+userInfo.getNtranssitecode()
										+ ");";
		}
		
		if(sordersampleno != "")
		{
			final String orderSampleArray = Arrays.stream(sordersampleno.split(",")).map(str -> "'"+ stringUtilityFunction.replaceQuote(str)+ "'").collect(Collectors.joining(","));
			
			updateQuery = updateQuery +"update externalordersample set "
					+ " ntransactionstatus = "+ntransactionstatus+",dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
					+ " where sexternalsampleid in ("+orderSampleArray+")"
					+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode="+userInfo.getNtranssitecode()
					+ " and ntransactionstatus <> "+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+"; ";
		}	
		jdbcTemplate.execute(updateQuery);
		
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		
		return new ResponseEntity<> (returnMap,HttpStatus.OK);
	}
	
	public void insertExternalOrderStatus(UserInfo userInfo,List<ExternalOrderTest>  lstexternalorderstatus)
	{
		try {
			String updateString ="";
			String queryString="insert into externalorderstatus (npreregno,nexternalordercode,sexternalorderid,ntransactionstatus,nsentstatus,dtransactiondate,nsitecode,nstatus) values ";						
			
			for (int f = 0; f < lstexternalorderstatus.size(); f++) {
				
				int limsstatus = (int)lstexternalorderstatus.get(f).getNtransactionstatus()== Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() ? Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() : (int)lstexternalorderstatus.get(f).getNtransactionstatus()==Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() ? Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() :(int)lstexternalorderstatus.get(f).getNtransactionstatus();
				
				queryString = queryString + "(" 
							+ lstexternalorderstatus.get(f).getNpreregno() + ","
							+ lstexternalorderstatus.get(f).getNexternalordercode() + ",'"
							+ lstexternalorderstatus.get(f).getSexternalorderid() + "',"
							+ lstexternalorderstatus.get(f).getNtransactionstatus() + ",8,'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				
				updateString = updateString +"update externalorder set ntransactionstatus="+limsstatus 
						+ " where nexternalordercode=" +lstexternalorderstatus.get(f).getNexternalordercode()
						+ " and nsitecode="+userInfo.getNtranssitecode()
						+";";

			}
			jdbcTemplate.execute(queryString.substring(0, queryString.length() - 1));
			
			jdbcTemplate.execute(updateString.substring(0, updateString.length() - 1));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public ResponseEntity<Object> getExternalOrderAttachment(String nexternalordercode, String npreregno,
			UserInfo userInfo) throws Exception {
		
		Map<String, Object> objMap = new HashMap<String, Object>();
		
		final String strValue = commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename());
		
		final String strExternalOrderAttachment = "select eoa.*, s.ssitename, concat(u.sfirstname,' ',u.slastname) as susername,"
												+ "TO_CHAR(eoa.dreleasedate ,'" + userInfo.getSpgdatetimeformat()
												+ "') sreleasedate, r.npreregno npreregno, "
												+ "case when ran.sarno='-' then concat(ran.npreregno,' ', '" + strValue
												+ "') else ran.sarno end sarno from externalorderattachment eoa, "
												+ " site s, users u, registration r, registrationarno ran "
												+ " where eoa.nexternalordercode in (" + nexternalordercode
												+ ") and r.npreregno in (" + npreregno + ")"
												+ " and eoa.nsourcesitecode=s.nsitecode and eoa.nusercode=u.nusercode "
												+ " and  eoa.nexternalordercode=(cast (r.jsonuidata->>'OrderCodeData' as integer)) "
												+ " and ran.npreregno=r.npreregno "
												+ " and eoa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and eoa.nsitecode=" + userInfo.getNtranssitecode() 
												+ " and s.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and u.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and r.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and ran.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and eoa.nsitecode="+ userInfo.getNtranssitecode()
												+ " and r.nsitecode = "+ userInfo.getNtranssitecode()
												+ " and ran.nsitecode="+ userInfo.getNtranssitecode()
												+ " and u.nsitecode="+ userInfo.getNmastersitecode()
												+ " and s.nsitecode="+ userInfo.getNmastersitecode();
		
		final List<Map<String, Object>> lstExternalOrderAttachment = jdbcTemplate.queryForList(strExternalOrderAttachment);
		
		objMap.put("ExternalOrderAttachmentList", lstExternalOrderAttachment);
		
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	
	
	public Map<String, Object> viewExternalOrderAttachment(Map<String, Object> objExternalOrderAttachmentFile,
			int ncontrolCode, UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<>();
		final ExternalOrderAttachment mapExternalOrderAttachment = (ExternalOrderAttachment) 
				jdbcUtilityFunction.queryForObject(""
								+ "select eoa.*, to_char(eoa.dreleasedate, '" 
								+ userInfo.getSpgsitedatetime().replace("'T'", " ")
								+ "') as sreleasedate, s.ssitename ssitename,"
								+ " concat(u.sfirstname, ' ', u.slastname) susername "
								+ " from externalorderattachment eoa, site s, users u "
								+ " where eoa.nexternalorderattachmentcode="
								+ objExternalOrderAttachmentFile.get("nexternalorderattachmentcode")
								+ " and eoa.nsourcesitecode=s.nsitecode "
								+ " and eoa.nusercode=u.nusercode"
								+ " and eoa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), 
								ExternalOrderAttachment.class, 
								jdbcTemplate);

		if (mapExternalOrderAttachment != null) {
			final List<Object> listObject = new ArrayList<Object>();
			map = ftpUtilityFunction.FileViewUsingFtp(mapExternalOrderAttachment.getSsystemfilename().toString(), ncontrolCode, userInfo,
					"", "");
			listObject.add(Arrays.asList(mapExternalOrderAttachment));
			
			auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, Arrays.asList("IDS_VIEWEXTERNALORDERREPORT"), userInfo);
		
		} else {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
			return map;
		}
		return map;

	}
	
	public ResponseEntity<Object> getExternalOrderForMapping(Map<String, Object> inputMap, UserInfo userInfo) {
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		
		final String updateReg = "select nallottedspeccode,(jsonuidata->>'nexternalordertypecode')::integer as "
									+ " nexternalordertypecode from registration where npreregno="
									+ inputMap.get("npreregno")
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode="+userInfo.getNtranssitecode();
		
		
		final List<Registration> values = jdbcTemplate.query(updateReg, new Registration());
		
		final String query = "select   nexternalordersamplecode,sexternalsampleid,ntransactionstatus,sexternalorderid, nexternalordercode, nordertypecode,"
							+ " nallottedspeccode,nusercode,nsitecode, ninstitutioncode,ninstitutionsitecode,spatientid,spatientfirstname,spatientlastname, "
							+ " spatientfathername, spostalcode, sstreet, shouseno,sflatno, smobileno,srefid, sphoneno, spassportno, scityname,sstreettemp,"
							+ " shousenotemp, sflatnotemp, scitynametemp,sexternalid,nproductcatcode,sproductcatname,nproductcode,sproductname,ngendercode,"
							+ " sgendername,ndiagnosticcasecode,sdiagnosticcasename,sdistrictname,nexternalordertypecode, scurrentadd,sinsdistrictcity,ssubmitterfirstname,"
							+ " ssubmitterlastname, ssubmitteremail, ssubmittershortname,ssubmittercode, ssubmitterid,submittertelephone,sinstitutionname, "
							+ " sinstitutioncatname,sinstitutioncode, sinstitutionsitename, sexternalordertypename from view_externalsample"
							+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nordertypecode = 2 and nexternalordertypecode =" + values.get(0).getNexternalordertypecode()
							+ " and nsitecode=" + userInfo.getNsitecode() + " and nallottedspeccode="
							+ values.get(0).getNallottedspeccode() + " and sexternalsampleid=N'"
							+ stringUtilityFunction.replaceQuote(inputMap.get("sampleorderid").toString()) 
							+ "' and ntransactionstatus="+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
		outputMap.put("externalOrderList", jdbcTemplate.query(query, new ExternalOrder()));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> orderMapping(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		int npreregno = (int) inputMap.get("npreregno");
		int ntransactionsamplecode = (int) inputMap.get("ntransactionsamplecode");
		int nexternalordercode = (int) inputMap.get("nexternalordercode");
		int nexternalordersamplecode = (int) inputMap.get("nexternalordersamplecode");
		String sexternalOrderID = (String) inputMap.get("sexternalOrderID");
		String sexternalSampleID = (String) inputMap.get("sexternalSampleID");

		final String updateReg = "select max(ntransactionstatus) from registrationhistory  where npreregno=" + npreregno;
		int ntransactionstatus = jdbcTemplate.queryForObject(updateReg, Integer.class);

		final String updateSubSampleReg = "select max(ntransactionstatus) from registrationsamplehistory  "
									+ " where npreregno=" + npreregno 
									+ " and ntransactionsamplecode=" + ntransactionsamplecode
									+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode="+ userInfo.getNtranssitecode();
		int ntransactionSubSampleStatus = jdbcTemplate.queryForObject(updateSubSampleReg, Integer.class);

		String updateExternalorder = "";
		if (ntransactionstatus != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				&& ntransactionstatus != Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				&& ntransactionstatus != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				&& ntransactionSubSampleStatus != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				&& ntransactionSubSampleStatus != Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				&& ntransactionSubSampleStatus != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {

			// String checkMaunalOrder="select count(*) from registrationsample where
			// jsondata->>'nordertypecode'='1' and npreregno="+npreregno;
			String checkMaunalOrder = " select count(rs.*) from registrationsample rs,registrationsamplehistory rsh "
									+ " where rsh.nsamplehistorycode in( "
									+ " select max(nsamplehistorycode) from  registrationsamplehistory  "
									+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode="+ userInfo.getNtranssitecode()
									+ " group by ntransactionsamplecode ) "
									+ " and rsh.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
									+ ") and  rsh.ntransactionsamplecode=rs.ntransactionsamplecode"
									+ " and  rsh.npreregno=" + npreregno 
									+ " and rs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rs.nsitecode="+ userInfo.getNtranssitecode()
									+ " and rsh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rsh.nsitecode="+ userInfo.getNtranssitecode()
									+ " and rs.jsondata->>'nordertypecode'='1' ";
			int count = jdbcTemplate.queryForObject(checkMaunalOrder, Integer.class);

			ntransactionstatus = ntransactionstatus == Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
					? Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
					: Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();

			if (count == 1) {
				int nRegistrationStatus = ntransactionSubSampleStatus == Enumeration.TransactionStatus.PREREGISTER
						.gettransactionstatus() ? Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
								: Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus();
				
				updateExternalorder = "update registration  set jsondata= jsondata ||jsonb_set(jsonb_set(jsonb_set( jsondata,"
						+ "   '{Order}', jsondata->'Order' || '{ \"label\": \"" + sexternalOrderID + "\", \"value\": "
						+ nexternalordercode + ",\"nexternalordercode\":" + nexternalordercode + "}'),"
						+ "    '{Order Type}', jsondata->'Order Type' || '{ \"label\": \"External\", \"value\": 2,\"nordertypecode\":  2}') ,"
						+ " '{External Sample Id}','{\"pkey\":\"nexternalordersamplecode\",\"label\":\""
						+ sexternalSampleID + "\",\"value\":" + nexternalordersamplecode
						+ ",\"source\":\"view_externalsample\",\"nquerybuildertablecode\": 260 ,\"nexternalordersamplecode\":"
						+ nexternalordersamplecode + "}')" + "||jsonb_build_object('OrderIdData', '" + sexternalOrderID
						+ "','OrderCodeData','" + nexternalordercode + "') "
						+ " ,jsonuidata= jsonuidata||jsonb_build_object('Order Type','External','orderTypeValue', 2 ,'OrderCodeData',"
						+ nexternalordercode + "," + " 'Order','" + sexternalOrderID + "','OrderIdData','"
						+ sexternalOrderID + "','External Sample Id','" + sexternalSampleID + "') "
						+ " where  npreregno=" + npreregno + ";";
				
				updateExternalorder = updateExternalorder + " update externalorder set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nexternalordercode="
						+ inputMap.get("manualOrderData") + ";";
				
				updateExternalorder = updateExternalorder + " update externalordersample set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where    nexternalordercode="
						+ inputMap.get("manualOrderData") + ";";

				updateExternalorder = updateExternalorder + " update externalordertest set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',  nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nexternalordercode="
						+ inputMap.get("manualOrderData") + ";";
				inputMap.put("npreregno", String.valueOf(npreregno));
				
				outputMap.putAll((Map<String, Object>) sampleOrderUpdate(inputMap, (String) inputMap.get("npreregno"),
						userInfo, nRegistrationStatus, nRegistrationStatus, true).getBody());
			}
			updateExternalorder = updateExternalorder
								+ "update  registrationsample set jsondata=jsondata||jsonb_build_object('nsampleordercode','"
								+ nexternalordersamplecode + "','sampleorderid','" + sexternalSampleID
								+ "','External Sample ID_child','" + sexternalSampleID + "',"
								+ "'nordertypecode','2','sordertypename','External','externalorderid','" + sexternalOrderID + "'),"
								+ "jsonuidata=jsonuidata||jsonb_build_object('nsampleordercode','" + nexternalordersamplecode
								+ "','sampleorderid','" + sexternalSampleID + "','External Sample ID_child','" + sexternalSampleID
								+ "','nordertypecode','2','sordertypename','External','externalorderid','" + sexternalOrderID
								+ "') where npreregno =" + npreregno + " and ntransactionsamplecode=" + ntransactionsamplecode + ";";
						
			updateExternalorder = updateExternalorder + " update externalorder set dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus=" + ntransactionstatus
								+ "   where  nexternalordercode=" + nexternalordercode + "; ";

			updateExternalorder = updateExternalorder + " update externalordersample set dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus=" + ntransactionstatus
								+ "  where   nexternalordersamplecode=" + nexternalordersamplecode + "  and nexternalordercode="
								+ nexternalordercode + "; ";

			jdbcTemplate.execute(updateExternalorder);
			
			inputMap.put("npreregno", String.valueOf(npreregno));
			inputMap.put("ntransactionsamplecode", String.valueOf(ntransactionsamplecode));
			outputMap.putAll(registrationDAOSupport.getDynamicRegistration(inputMap, userInfo));
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		
		} else {
			if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RELEASEDSAMPLESCONNOTBEMAPPED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (ntransactionSubSampleStatus == Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					|| ntransactionSubSampleStatus == Enumeration.TransactionStatus.REJECTED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANCELLEDSAMPLESCONNOTBEMAPPED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYEXTERNALMAPPED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		}
	}

}
