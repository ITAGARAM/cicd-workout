package com.agaramtech.qualis.scheduler.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

//import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
//import com.agaramtech.qualis.scheduler.service.SchedulerDAO;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.model.GraphicalSchedulerMaster;
import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterMonthly;
import com.agaramtech.qualis.scheduler.model.ScheduleMasterWeekly;
import com.agaramtech.qualis.scheduler.model.SchedulerRecurringMonthlyPeriod;
import com.agaramtech.qualis.scheduler.model.SchedulerTransaction;
import com.agaramtech.qualis.scheduler.model.SchedulerType;
import com.agaramtech.qualis.scheduler.model.SchedulerTypeRecurring;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SchedulerDAOImpl implements SchedulerDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	//private final RegistrationDAO registrationDAO;

	@Override
	public ResponseEntity<Object> getScheduler(Integer nscheduleCode, final UserInfo userInfo,String sscheduleType) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		ScheduleMaster selectedScheduler = null;
		String stempType="";

		final String strQuery ="select nschedulertypecode,coalesce(g.jsondata->'sschedulertypename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sschedulertypename'->>'en-US') as sschedulertypename,ndefaultstatus,nsitecode,nstatus "
				+ " from schedulertype g where g.nsitecode = "+userInfo.getNmastersitecode() + " and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("Get Method:"+ strQuery);

		List<SchedulerType> schedulerTypeList = jdbcTemplate.query(strQuery, new SchedulerType());
		outputMap.put("filterScheduleType",   schedulerTypeList);

		if(sscheduleType == null || sscheduleType == "" )
		{
			if(!schedulerTypeList.isEmpty())
			{
				if(schedulerTypeList.get(0).getNschedulertypecode()==1)
				{
					stempType="One Time";
				}
				else
				{
					stempType="Recurring";
				}
			}
		}
		else if(sscheduleType.equals("One Time") || sscheduleType.equals("O"))
		{
			stempType="One Time";
		}
		else
		{
			stempType="Recurring";
		}
		HashMap<Object, Object> subTypeValue = new HashMap<>();
		subTypeValue.put("label", stempType);

		outputMap.put("nfilterScheduleType",   subTypeValue);
		if (nscheduleCode == null) {

			String query = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
					+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
					+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
					+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,a.ntzstartdatetimezone,a.ntzstarttimetimezone,a.ntzenddatetimezone,a.ntzendtimetimezone,"
					+"COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
					+"COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
					+ "coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus, "
					+ "case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
					+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
					+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
					+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
					+ " a.ntransactionstatus = t.ntranscode and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and "
					+ " c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and a.nsitecode = "+userInfo.getNmastersitecode()
					+ " and b.nsitecode = "+userInfo.getNmastersitecode()+" and c.nsitecode = "+userInfo.getNmastersitecode()+" and ";
			if(stempType == "One Time")
			{
				query+= " a.sscheduletype = 'O'";
			}
			else
			{
				query+= " a.sscheduletype != 'O'";
			}
			query+=" and a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by a.nschedulecode ";


			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final List<ScheduleMaster> schedulerList = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new ScheduleMaster()),
							Arrays.asList("sstartdate","sstarttime","senddate","sendtime"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
					new TypeReference<List<ScheduleMaster>>() {
					});


			if (schedulerList.isEmpty()) {
				outputMap.put("Scheduler", schedulerList);
				outputMap.put("SelectedScheduler", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("Scheduler", schedulerList);
				selectedScheduler = schedulerList.get(schedulerList.size() - 1);
				nscheduleCode = selectedScheduler.getNschedulecode();
				query="select COALESCE(TO_CHAR(st.dscheduleoccurrencedate,'dd/MM/yyyy HH24:MI:ss'),'') as sscheduleoccurencedate,rg.jsonuidata->>'Product'as sproduct,rg.jsonuidata->>'Sampling Point'as ssamplingpoint ";
				query+=" from schedulertransaction st,registration rg where st.npreregno = rg.npreregno and st.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+" and st.nsitecode = "+userInfo.getNmastersitecode()+" and rg.nsitecode = "+userInfo.getNmastersitecode()+" and st.nschedulecode = "+nscheduleCode;
				List<SchedulerTransaction> schedulertranLst = jdbcTemplate.query(query,
						new SchedulerTransaction());
				outputMap.put("SchedularTransaction", schedulertranLst);
			}
			//Saravanan
			//				List<RegistrationSubType> lstRegistrationSubType= registrationDAO.getRegistrationSubType(1, userInfo);
			//
			//
			//	      if(!lstRegistrationSubType.isEmpty()) {
			//	    	  outputMap.put("nregsubtypeversioncode",lstRegistrationSubType.get(0).getNregsubtypeversioncode());
			//	    	  ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) registrationDAO.getRegistrationTemplate(1,1).getBody();
			//
			//
			//				if (lstTemplate != null) {
			//					outputMap.put("schedulerTemplate", lstTemplate);
			//					outputMap.put("schedulerSubSampleTemplate",
			//							registrationDAO.getRegistrationSubSampleTemplate(lstTemplate.getNdesigntemplatemappingcode())
			//									.getBody());
			//					outputMap.put("DynamicDesign", registrationDAO.getTemplateDesign(lstTemplate.getNdesigntemplatemappingcode(),
			//							43));
			//					outputMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
			//				}
			//				}
			//Saravanan end

		} else {
			List<ScheduleMaster> schedulerList  = getActiveSchedulerByIdList(nscheduleCode, userInfo);
			final ObjectMapper objmap=new ObjectMapper();
			objmap.registerModule(new JavaTimeModule());

			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			schedulerList = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(schedulerList,
							Arrays.asList("sstartdate","sstarttime","senddate","sendtime"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
					new TypeReference<List<ScheduleMaster>>() {
					});
			selectedScheduler=schedulerList.get(0);
			nscheduleCode = selectedScheduler.getNschedulecode();

			String query = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
					+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
					+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
					+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,a.ntzstartdatetimezone,a.ntzstarttimetimezone,a.ntzenddatetimezone,a.ntzendtimetimezone,"
					+ "COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
					+ "COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
					+ "coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus, "
					+ " case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
					+ " when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
					+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
					+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
					+ " a.ntransactionstatus = t.ntranscode and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and "
					+ " c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and a.nsitecode = "+userInfo.getNmastersitecode()
					+ "	and b.nsitecode = "+userInfo.getNmastersitecode()+" and c.nsitecode = "+userInfo.getNmastersitecode()+" and ";
			if(stempType == "One Time")
			{
				query+= " a.sscheduletype = 'O'";
			}
			else
			{
				query+= " a.sscheduletype != 'O'";
			}

			query+=" and a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by a.nschedulecode ";

			objMapper.registerModule(new JavaTimeModule());
			final List<ScheduleMaster> schedulerLst = objMapper.convertValue(
					dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new ScheduleMaster()),
							Arrays.asList("sstartdate","sstarttime","senddate","sendtime"),
							Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
					new TypeReference<List<ScheduleMaster>>() {
					});
			outputMap.put("Scheduler", schedulerLst);
			query="select COALESCE(TO_CHAR(st.dscheduleoccurrencedate,'dd/MM/yyyy HH24:MI:ss'),'') as sscheduleoccurencedate,rg.jsonuidata->>'Product'as sproduct,rg.jsonuidata->>'Sampling Point'as ssamplingpoint "
					+ " from schedulertransaction st,registration rg where st.npreregno = rg.npreregno and st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nsitecode = "+userInfo.getNmastersitecode()+" and rg.nsitecode = "+userInfo.getNmastersitecode()+" and st.nschedulecode = "+nscheduleCode;
			List<SchedulerTransaction> schedulertranLst = jdbcTemplate.query(query,
					new SchedulerTransaction());
			outputMap.put("SchedularTransaction", schedulertranLst);
		}
		if (selectedScheduler == null) {
			final String returnString = commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedScheduler", selectedScheduler);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}



	@Override
	public ScheduleMaster getActiveSchedulerById(final int nscheduleCode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
				+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
				+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
				+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
				+"COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
				+"COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
				+" coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus ,"
				+" case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
				+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
				+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
				+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
				+ " a.ntransactionstatus = t.ntranscode and a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and a.nschedulecode = "+nscheduleCode;


		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		ScheduleMaster sch= null;
		final List<ScheduleMaster> schedulerList = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new ScheduleMaster()),
						Arrays.asList("sstartdate","sstarttime","senddate","sendtime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ScheduleMaster>>() {
				});
		sch= schedulerList.size() > 0 ? schedulerList.get(0) : sch;
		return sch;
	}

	public List<ScheduleMaster> getActiveSchedulerByIdList(final int nscheduleCode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
				+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
				+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
				+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
				+"COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
				+"COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
				+" coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus ,"
				+" case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
				+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
				+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
				+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
				+ " a.ntransactionstatus = t.ntranscode and a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and a.nschedulecode = "+nscheduleCode;

		final List<ScheduleMaster> schedulerList = jdbcTemplate.query(strQuery,
				new ScheduleMaster());
		return schedulerList;
	}

	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSchedulerByScheduleType(Integer nscheduleTypeCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		ScheduleMaster selectedScheduler = null;
		Integer nscheduleCode=0;
		final String strQuery ="select nschedulertypecode,coalesce(g.jsondata->'sschedulertypename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sschedulertypename'->>'en-US') as sschedulertypename,ndefaultstatus,nsitecode,nstatus "
				+ " from schedulertype g where g.nsitecode = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		outputMap.put("filterScheduleType",   jdbcTemplate.query(strQuery, new SchedulerType()));
		String query="";
		if (nscheduleTypeCode == 1) {

			query = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
					+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
					+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
					+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
					+"COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
					+"COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
					+" coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus, "
					+" case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
					+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
					+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
					+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
					+ " a.ntransactionstatus = t.ntranscode and a.sscheduletype ='O' and "
					+ "a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and a.nsitecode = "
					+ userInfo.getNmastersitecode()+" and b.nsitecode = "+userInfo.getNmastersitecode()+" and "
					+ " c.nsitecode = "+userInfo.getNmastersitecode()+" order by a.nschedulecode ";
		}
		else
		{
			query = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
					+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
					+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
					+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
					+"COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
					+"COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
					+" coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ " t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus, "
					+" case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
					+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
					+ " from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
					+ "a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
					+ " a.ntransactionstatus = t.ntranscode and a.sscheduletype !='O' and "
					+ "a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and a.nsitecode = "
					+ userInfo.getNmastersitecode()+" and b.nsitecode = "+userInfo.getNmastersitecode()+" and "
					+ " c.nsitecode = "+userInfo.getNmastersitecode()+" order by a.nschedulecode ";
		}

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<ScheduleMaster> schedulerList = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new ScheduleMaster()),
						Arrays.asList("sstartdate","sstarttime","senddate","sendtime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ScheduleMaster>>() {
				});
		if (schedulerList.isEmpty()) {
			outputMap.put("Scheduler", schedulerList);
			outputMap.put("SelectedScheduler", null);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			outputMap.put("Scheduler", schedulerList);
			selectedScheduler = schedulerList.get(schedulerList.size() - 1);
			nscheduleCode = selectedScheduler.getNschedulecode();
			query="select COALESCE(TO_CHAR(st.dscheduleoccurrencedate,'dd/MM/yyyy HH24:MI:ss'),'') as sscheduleoccurencedate,rg.jsonuidata->>'Product'as sproduct,rg.jsonuidata->>'Sampling Point'as ssamplingpoint ";
			query+=" from schedulertransaction st,registration rg where st.npreregno = rg.npreregno and st.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+ " and rg.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and st.nsitecode = "+userInfo.getNmastersitecode()
			+" and rg.nsitecode = "+userInfo.getNmastersitecode()+" and st.nschedulecode = "+nscheduleCode;
			List<SchedulerTransaction> schedulertranLst = jdbcTemplate.query(query,
					new SchedulerTransaction());
			outputMap.put("SchedularTransaction", schedulertranLst);

		}
		//Saravanan
		//				List<RegistrationSubType> lstRegistrationSubType= registrationDAO.getRegistrationSubType(1, userInfo);
		//
		//
		//			      if(!lstRegistrationSubType.isEmpty()) {
		//			    	  outputMap.put("nregsubtypeversioncode",lstRegistrationSubType.get(0).getNregsubtypeversioncode());
		//			    	  ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) registrationDAO.getRegistrationTemplate(1,1).getBody();
		//
		//
		//						if (lstTemplate != null) {
		//							outputMap.put("schedulerTemplate", lstTemplate);
		//							outputMap.put("schedulerSubSampleTemplate",
		//									registrationDAO.getRegistrationSubSampleTemplate(lstTemplate.getNdesigntemplatemappingcode())
		//											.getBody());
		//							outputMap.put("DynamicDesign", registrationDAO.getTemplateDesign(lstTemplate.getNdesigntemplatemappingcode(),
		//									43));
		//							outputMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
		//						}
		//						}
		//Saravanan end

		if (selectedScheduler == null) {
			final String returnString = commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedScheduler", selectedScheduler);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getSchedulerType(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select nschedulertypecode,coalesce(g.jsondata->'sschedulertypename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'sschedulertypename'->>'en-US') as sschedulertypename,ndefaultstatus,nsitecode,nstatus "
				+ " from schedulertype g where g.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		outputMap.put("SchedulerType",   jdbcTemplate.query(strQuery, new SchedulerType()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSchedulerTypeRecurring(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select ntyperecurringcode,nschedulertypecode,coalesce(g.jsondata->'srecurringmodename'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'srecurringmodename'->>'en-US') as srecurringmodename,ndefaultstatus,nsitecode,nstatus "
				+ " from schedulertyperecurring g where g.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		outputMap.put("SchedulerTypeRecurring",   jdbcTemplate.query(strQuery, new SchedulerTypeRecurring()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSchedulerRecurringMonthlyPeriod(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select nrecurringperiodcode,ntyperecurringcode,coalesce(g.jsondata->'srecurrenceperiod'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'srecurrenceperiod'->>'en-US') as srecurrenceperiod,ndefaultstatus,nsitecode,nstatus "
				+ " from schedulerrecurringmonthlyperiod g where g.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		outputMap.put("SchedulerRecurringMonthlyPeriod",   jdbcTemplate.query(strQuery, new SchedulerRecurringMonthlyPeriod()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getGraphicalScheduler(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery ="select nschedulecode,coalesce(g.jsondata) as jsondata,stitle,sscheduletype,nschedulestatus,nsitecode,nstatus "
				+ " from graphicalschedulermaster g where g.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


		outputMap.put("ScheduleData",   jdbcTemplate.query(strQuery, new GraphicalSchedulerMaster()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}


	public ScheduleMaster getActiveSchedulerByName(final String sscheduleName, final UserInfo userInfo) throws Exception {

		final String strQuery = "select a.nschedulecode,a.sschedulename,a.sscheduletype,Case when a.sremarks = 'null' or a.sremarks = '' then '-' else a.sremarks end as sremarks,a.dstartdate,a.dstarttime,a.noccurencenooftimes,"
				+ "a.soccurencehourwiseinterval,a.noccurencedaywiseinterval,a.denddate,a.dendtime,b.nexactday,b.nmonthlyoccurrencetype,b.njan,"
				+ "b.nfeb,b.nmar,b.napr,b.nmay,b.njun,b.njul,b.naug,b.nsep,b.noct,b.nnov,b.ndec,c.nsunday,c.nmonday,c.ntuesday,c.nwednesday,"
				+ "c.nthursday,c.nfriday,c.nsaturday,a.nstatus,a.noffsetdstartdate,a.noffsetdstarttime,a.noffsetdenddate,a.noffsetdendtime,"
				+" COALESCE(TO_CHAR(a.dstartdate,'" + userInfo.getSpgsitedatetime() +"'),'') as sstartdate,COALESCE(TO_CHAR(a.dstarttime,'" + userInfo.getSpgsitedatetime() +"'),'') as sstarttime,"
				+" COALESCE(TO_CHAR(a.denddate,'" + userInfo.getSpgsitedatetime() +"'),'') as senddate,COALESCE(TO_CHAR(a.dendtime,'" + userInfo.getSpgsitedatetime() +"'),'') as sendtime,"
				+" coalesce(t.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+" t.jsondata->'stransdisplaystatus'->>'en-US') as stransstatus,a.ntransactionstatus, "
				+" case when a.sscheduletype ='O' then 'One Time' when a.sscheduletype ='D' then 'Daily'"
				+" when a.sscheduletype ='W' then 'Weekly' else 'Monthly' end as stempscheduleType "
				+" from schedulemaster a ,schedulemastermonthly b ,schedulemasterweekly c,transactionstatus t where "
				+" a.nschedulecode = b.nschedulecode and b.nschedulecode = c.nschedulecode and c.nschedulecode = a.nschedulecode and "
				+" a.ntransactionstatus = t.ntranscode and a.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode = "+userInfo.getNmastersitecode()
				+" and b.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and b.nsitecode = "+userInfo.getNmastersitecode()+" and c.nsitecode = "+userInfo.getNmastersitecode()+" and a.sschedulename = '"+sscheduleName+"'";

		return (ScheduleMaster) jdbcUtilityFunction.queryForObject(strQuery, ScheduleMaster.class, jdbcTemplate);

	}

	@Override
	//@SuppressWarnings("unused")
	public ResponseEntity<Object> createScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly schedulerweek,ScheduleMasterMonthly schedulermonth, UserInfo userInfo)
			throws Exception {
		//Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> savedInstList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final String sQueryLock = " lock table schedulemaster "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();

		jdbcTemplate.execute(sQueryLock);

		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
		final List<String> lstTZcolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		if (scheduler.getDstartdate() != null) {
			scheduler.setSstartdate(dateUtilityFunction.instantDateToString(scheduler.getDstartdate()).replace("T", " "));
			lstDateField.add("sstartdate");
			lstDatecolumn.add(scheduler.getStzstartdate());
			lstTZcolumn.add("ntzstartdatetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdstartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSstartdate(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDenddate() != null) {
			scheduler.setSenddate(dateUtilityFunction.instantDateToString(scheduler.getDenddate()).replace("T", " "));
			lstDateField.add("senddate");
			lstDatecolumn.add(scheduler.getStzenddate());
			lstTZcolumn.add("ntzenddatetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSenddate(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDstarttime() != null) {
			scheduler.setSstarttime(dateUtilityFunction.instantDateToString(scheduler.getDstarttime()).replace("T", " "));
			lstDateField.add("sstarttime");
			lstDatecolumn.add(scheduler.getStzstarttime());
			lstTZcolumn.add("ntzstarttimetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdstarttime(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSstarttime(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDendtime() != null) {
			scheduler.setSendtime(dateUtilityFunction.instantDateToString(scheduler.getDendtime()).replace("T", " "));
			lstDateField.add("sendtime");
			lstDatecolumn.add(scheduler.getStzendtime());
			lstTZcolumn.add("ntzendtimetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdendtime(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSendtime(),userInfo.getStimezoneid()));
			}
		}


		final ScheduleMaster convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(scheduler, lstDateField, lstTZcolumn, true, userInfo),
				new TypeReference<ScheduleMaster>() {
				});

		final ScheduleMaster schmaster = getActiveSchedulerByName(scheduler.getSschedulename(), userInfo);

		if (schmaster != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			String schSeq = "select nsequenceno from seqnoscheduler where stablename='schedulemaster'";

			int seqNo = jdbcTemplate.queryForObject(schSeq, Integer.class);
			seqNo++;


			String schedulerInsert = "";

			schedulerInsert = "insert into schedulemaster(nschedulecode,sschedulename,sscheduletype,sremarks,"
					+ " dstartdate,dstarttime,nsitecode,nstatus,noccurencenooftimes,soccurencehourwiseinterval,"
					+ "noccurencedaywiseinterval,denddate,dendtime,nmonthyweek,noffsetdstartdate,noffsetdstarttime,noffsetdenddate,noffsetdendtime,ntransactionstatus,ntzstartdatetimezone,ntzstarttimetimezone,ntzenddatetimezone,ntzendtimetimezone,dmodifieddate)"
					+ " values(" + seqNo + ",'" + scheduler.getSschedulename() + "','" + scheduler.getSscheduletype() + "','"
					+ scheduler.getSremarks()+ "','" + convertedObject.getSstartdate() + "','" + convertedObject.getSstarttime() + "'"
					+ "," + scheduler.getNsitecode() + ",1," + scheduler.getNoccurencenooftimes() + ",'" + scheduler.getSoccurencehourwiseinterval() + "',"
					+ scheduler.getNoccurencedaywiseinterval() + ",'" + convertedObject.getSenddate() + "'" + ",'"
					+ convertedObject.getSendtime() + "',1,"+scheduler.getNoffsetdstartdate()+","+scheduler.getNoffsetdstarttime()+","
					+ scheduler.getNoffsetdenddate()+","+scheduler.getNoffsetdendtime()+","+Enumeration.TransactionStatus.DRAFT.gettransactionstatus() +","+scheduler.getNtzstartdatetimezone()+","+scheduler.getNtzstarttimetimezone()+","+scheduler.getNtzenddatetimezone()+","
					+scheduler.getNtzendtimetimezone()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo) + "')";


			jdbcTemplate.execute(schedulerInsert);

			schedulerInsert = "insert into schedulemasterweekly(nschedulecode,nstatus,nsunday,nmonday,"
					+ " ntuesday,nwednesday,nthursday,nfriday,nsaturday,nmonthyweek,dmodifieddate,nsitecode)"
					+ " values(" + seqNo + ",1," + schedulerweek.getNsunday() + "," + schedulerweek.getNmonday() + ","
					+ schedulerweek.getNtuesday() + ","+ schedulerweek.getNwednesday() + "," + schedulerweek.getNthursday() + ","
					+ schedulerweek.getNfriday()+","+schedulerweek.getNsaturday() + ",1,'"+dateUtilityFunction.getCurrentDateTime(userInfo) + "',"+ userInfo.getNmastersitecode() +")";

			jdbcTemplate.execute(schedulerInsert);

			schedulerInsert = "insert into schedulemastermonthly(nschedulecode,nstatus,nexactday,nmonthlyoccurrencetype,"
					+ "njan,nfeb,nmar,napr,nmay,njun,njul,naug,nsep,noct,nnov,ndec,nmonthyweek,dmodifieddate,nsitecode)"
					+ " values(" + seqNo + ",1," + schedulermonth.getNexactday() + "," + schedulermonth.getNmonthlyoccurrencetype() + ","
					+ schedulermonth.getNjan() + ","+ schedulermonth.getNfeb() + "," + schedulermonth.getNmar() + ","
					+ schedulermonth.getNapr()+","+schedulermonth.getNmay() +","+schedulermonth.getNjun()+","+schedulermonth.getNjul()+","
					+ schedulermonth.getNaug()+","+schedulermonth.getNsep()+","+schedulermonth.getNoct()+","+schedulermonth.getNnov()+","
					+ schedulermonth.getNdec()+ ",1,'"+dateUtilityFunction.getCurrentDateTime(userInfo) + "',"+ userInfo.getNmastersitecode() +")";

			jdbcTemplate.execute(schedulerInsert);

			schedulerInsert = "update seqnoscheduler set nsequenceno=" + seqNo
					+ " where stablename='schedulemaster'";
			jdbcTemplate.execute(schedulerInsert);

			savedInstList.add(scheduler);

			multilingualIDList.add("IDS_ADDSCHEDULER");
			auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, multilingualIDList, userInfo);



			return new ResponseEntity<>(getScheduler(null, userInfo,scheduler.getSscheduletype()).getBody(), HttpStatus.OK);
		}



	}

	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateScheduler(ScheduleMaster scheduler,ScheduleMasterWeekly scheduleWeekly,ScheduleMasterMonthly scheduleMonthly, UserInfo userInfo)
			throws Exception {
		final List<Object> beforeSavedInstList = new ArrayList<>();
		final List<Object> savedInstList = new ArrayList<>();
		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		if (scheduler.getDstartdate() != null) {
			scheduler.setSstartdate(dateUtilityFunction.instantDateToString(scheduler.getDstartdate()).replace("T", " "));
			lstDateField.add("sstartdate");
			lstDatecolumn.add("ntzstartdatetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdstartdate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSstartdate(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDenddate() != null) {
			scheduler.setSenddate(dateUtilityFunction.instantDateToString(scheduler.getDenddate()).replace("T", " "));
			lstDateField.add("senddate");
			lstDatecolumn.add("ntzenddatetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdenddate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSenddate(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDstarttime() != null) {
			scheduler.setSstarttime(dateUtilityFunction.instantDateToString(scheduler.getDstarttime()).replace("T", " "));
			lstDateField.add("sstarttime");
			lstDatecolumn.add("ntzstarttimetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdstarttime(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSstarttime(),userInfo.getStimezoneid()));
			}
		}

		if (scheduler.getDendtime() != null) {
			scheduler.setSendtime(dateUtilityFunction.instantDateToString(scheduler.getDendtime()).replace("T", " "));
			lstDateField.add("sendtime");
			lstDatecolumn.add("ntzendtimetimezone");
			if(userInfo.getNtimezonecode()>0)
			{
				scheduler.setNoffsetdendtime(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(scheduler.getSendtime(),userInfo.getStimezoneid()));
			}
		}


		final ScheduleMaster convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(scheduler, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<ScheduleMaster>() {
				});

		final ScheduleMaster schmaster =  getActiveSchedulerById(scheduler.getNschedulecode(), userInfo);

		if (schmaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			String query="";



			if(scheduler.getSscheduletype().equals("O"))
			{
				query= "update schedulemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + "sschedulename='"+scheduler.getSschedulename()+"',"
						+ "sscheduletype = '"+scheduler.getSscheduletype()+"',sremarks = '"+scheduler.getSremarks()+"',"
						+ "dstartdate = '"+convertedObject.getSstartdate()+"',dstarttime ='"+convertedObject.getSstarttime()+"',"
						+ "noccurencenooftimes = 0,soccurencehourwiseinterval='null ',"
						+ "noccurencedaywiseinterval=0,denddate='"+convertedObject.getSenddate()+"',"
						+ "dendtime='"+convertedObject.getSendtime()+"',noffsetdstartdate = "+scheduler.getNoffsetdstartdate()+","
						+ " noffsetdstarttime = "+scheduler.getNoffsetdstarttime()+",noffsetdenddate = "+scheduler.getNoffsetdenddate()+","
						+ " noffsetdendtime = "+scheduler.getNoffsetdendtime()+" where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemasterweekly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + "nsunday=0,nmonday = 0,ntuesday = 0,nwednesday = 0,nthursday =0,"
						+ "nfriday = 0,nsaturday=0"
						+ " where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemastermonthly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + "nexactday=0,nmonthlyoccurrencetype = 0,njan = 0,nfeb = 0,"
						+ "nmar =0,napr=0,nmay=0,njun=0,njul=0,naug=0,nsep=0,noct=0,nnov=0,ndec=0 where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);
			}
			if(scheduler.getSscheduletype().equals("D"))
			{
				query= "update schedulemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , " + "sschedulename='"+scheduler.getSschedulename()+"',"
						+ "sscheduletype = '"+scheduler.getSscheduletype()+"',sremarks = '"+scheduler.getSremarks()+"',"
						+ "dstartdate = '"+convertedObject.getSstartdate()+"',dstarttime ='"+convertedObject.getSstarttime()+"',"
						+ "noccurencenooftimes = "+scheduler.getNoccurencenooftimes()+",soccurencehourwiseinterval='"+scheduler.getSoccurencehourwiseinterval()+"',"
						+ "noccurencedaywiseinterval="+scheduler.getNoccurencedaywiseinterval()+",denddate='"+convertedObject.getSenddate()+"',"
						+ "dendtime='"+convertedObject.getSendtime()+"',noffsetdstartdate = "+scheduler.getNoffsetdstartdate()+","
						+ " noffsetdstarttime = "+scheduler.getNoffsetdstarttime()+",noffsetdenddate = "+scheduler.getNoffsetdenddate()+","
						+ " noffsetdendtime = "+scheduler.getNoffsetdendtime()+" where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemasterweekly  set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + "nsunday=0,nmonday = 0,ntuesday = 0,nwednesday = 0,nthursday =0,"
						+ "nfriday = 0,nsaturday=0"
						+ " where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemastermonthly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' ," + "nexactday=0,nmonthlyoccurrencetype = 0,njan = 0,nfeb = 0,"
						+ "nmar =0,napr=0,nmay=0,njun=0,njul=0,naug=0,nsep=0,noct=0,nnov=0,ndec=0 where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

			}
			if(scheduler.getSscheduletype().equals("W"))
			{
				query= "update schedulemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , " + "sschedulename='"+scheduler.getSschedulename()+"',"
						+ "sscheduletype = '"+scheduler.getSscheduletype()+"',sremarks = '"+scheduler.getSremarks()+"',"
						+ "dstartdate = '"+convertedObject.getSstartdate()+"',dstarttime ='"+convertedObject.getSstarttime()+"',"
						+ "noccurencenooftimes = 0,soccurencehourwiseinterval='null ',"
						+ "noccurencedaywiseinterval=0,denddate='"+convertedObject.getSenddate()+"',"
						+ "dendtime='"+convertedObject.getSendtime()+"',noffsetdstartdate = "+scheduler.getNoffsetdstartdate()+","
						+ " noffsetdstarttime = "+scheduler.getNoffsetdstarttime()+",noffsetdenddate = "+scheduler.getNoffsetdenddate()+","
						+ " noffsetdendtime = "+scheduler.getNoffsetdendtime()+" where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemasterweekly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + "nsunday="+scheduleWeekly.getNsunday()+","
						+ "nmonday = "+scheduleWeekly.getNmonday()+",ntuesday = "+scheduleWeekly.getNtuesday()+","
						+ "nwednesday = "+scheduleWeekly.getNwednesday()+",nthursday ="+scheduleWeekly.getNthursday()+","
						+ "nfriday = "+scheduleWeekly.getNfriday()+",nsaturday="+scheduleWeekly.getNsaturday()+""
						+ " where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemastermonthly set  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + "nexactday=0,nmonthlyoccurrencetype = 0,njan = 0,nfeb = 0,"
						+ "nmar =0,napr=0,nmay=0,njun=0,njul=0,naug=0,nsep=0,noct=0,nnov=0,ndec=0 where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);
			}
			if(scheduler.getSscheduletype().equals("M"))
			{
				query= "update schedulemaster set  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' ," + "sschedulename='"+scheduler.getSschedulename()+"',"
						+ "sscheduletype = '"+scheduler.getSscheduletype()+"',sremarks = '"+scheduler.getSremarks()+"',"
						+ "dstartdate = '"+convertedObject.getSstartdate()+"',dstarttime ='"+convertedObject.getSstarttime()+"',"
						+ "noccurencenooftimes = 0,soccurencehourwiseinterval='null ',"
						+ "noccurencedaywiseinterval=0,denddate='"+convertedObject.getSenddate()+"',"
						+ "dendtime='"+convertedObject.getSendtime()+"',noffsetdstartdate = "+scheduler.getNoffsetdstartdate()+","
						+ " noffsetdstarttime = "+scheduler.getNoffsetdstarttime()+",noffsetdenddate = "+scheduler.getNoffsetdenddate()+","
						+ " noffsetdendtime = "+scheduler.getNoffsetdendtime()+" where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemasterweekly set  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + "nsunday=0,nmonday = 0,ntuesday = 0,nwednesday = 0,nthursday =0,"
						+ "nfriday = 0,nsaturday=0"
						+ " where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);

				query = "update schedulemastermonthly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + "nexactday="+scheduleMonthly.getNexactday()+","
						+ "nmonthlyoccurrencetype = "+scheduleMonthly.getNmonthlyoccurrencetype()+",njan = "+scheduleMonthly.getNjan()+","
						+ "nfeb = "+scheduleMonthly.getNfeb()+",nmar ="+scheduleMonthly.getNmar()+",napr="+scheduleMonthly.getNapr()+","
						+ "nmay="+scheduleMonthly.getNmay()+",njun="+scheduleMonthly.getNjun()+",njul="+scheduleMonthly.getNjul()+","
						+ "naug="+scheduleMonthly.getNaug()+",nsep="+scheduleMonthly.getNsep()+",noct="+scheduleMonthly.getNoct()+","
						+ "nnov="+scheduleMonthly.getNnov()+",ndec="+scheduleMonthly.getNdec()+" where nschedulecode=" + scheduler.getNschedulecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				jdbcTemplate.execute(query);
			}

			savedInstList.add(scheduler);
			beforeSavedInstList.add(schmaster);

			auditUtilityFunction.fnInsertAuditAction(savedInstList, 2, beforeSavedInstList, Arrays.asList("IDS_EDITSCHEDULER"), userInfo);
			return new ResponseEntity<>(getScheduler(scheduler.getNschedulecode(), userInfo,scheduler.getSscheduletype()).getBody(), HttpStatus.OK);



		}
	}

	@Override
	public ResponseEntity<Object> deleteScheduler(ScheduleMaster scheduler, UserInfo userInfo)
			throws Exception {

		final List<Object> deletedSchedulerList = new ArrayList<>();

		final ScheduleMaster schMaster = getActiveSchedulerById(scheduler.getNschedulecode(), userInfo);

		if (schMaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String updateQuery = "";

			updateQuery = "update schedulemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , nstatus = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nschedulecode = "
					+ scheduler.getNschedulecode()+ ";";

			updateQuery = updateQuery + "update schedulemasterweekly set  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,nstatus = "
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nschedulecode = "
					+ scheduler.getNschedulecode() + " ;";

			updateQuery =updateQuery + "update schedulemastermonthly set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.NA.gettransactionstatus()
			+ " where nschedulecode="+scheduler.getNschedulecode()+";";

			jdbcTemplate.execute(updateQuery);


			deletedSchedulerList.add(scheduler);
			auditUtilityFunction.fnInsertAuditAction(deletedSchedulerList, 1, null, Arrays.asList("IDS_DELETESCHEDULER"), userInfo);
			return new ResponseEntity<Object>(getScheduler(null, userInfo,scheduler.getSscheduletype()).getBody(), HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> approveScheduler(ScheduleMaster scheduler, UserInfo userInfo)
			throws Exception {

		final List<Object> approvedSchedulerList = new ArrayList<>();

		final ScheduleMaster schMaster = getActiveSchedulerById(scheduler.getNschedulecode(), userInfo);

		if (schMaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String updateQuery = "";

			updateQuery = "update schedulemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " where nschedulecode = "
					+ scheduler.getNschedulecode()+ ";";


			jdbcTemplate.execute(updateQuery);


			approvedSchedulerList.add(scheduler);

			auditUtilityFunction.fnInsertAuditAction(approvedSchedulerList, 1, null, Arrays.asList("IDS_APPROVESCHEDULER"), userInfo);
			return new ResponseEntity<Object>(getScheduler(scheduler.getNschedulecode(), userInfo,scheduler.getSscheduletype()).getBody(), HttpStatus.OK);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createGraphicalScheduler(Map<String, Object> inputMap) throws Exception {


		final String sQueryLock = " lock table graphicalschedulermaster "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();

		jdbcTemplate.execute(sQueryLock);

		ObjectMapper objmap = new ObjectMapper();
		final List<Object> savedInstList = new ArrayList<>();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});


		Map<String, Object> inputMaping=(Map<String, Object>) inputMap.get("scheduleData");


		GraphicalSchedulerMaster scheduler = objmap.convertValue(inputMaping.get("graphicalschedulermaster"),
				new TypeReference<GraphicalSchedulerMaster>() {
		});


		String stitle = "";
		String sscheduletype="";
		if (inputMap.get("stitle") != null) {
			stitle = (String) inputMap.get("stitle");
		}
		if (inputMap.get("sscheduletype") != null) {
			sscheduletype = (String) inputMap.get("sscheduletype");
		}

		JSONObject jsonScheduler = new JSONObject(scheduler.getJsondata());

		String schSeq = "select nsequenceno from seqnoscheduler where stablename='graphicalschedulermaster'";
		int nseqNo = jdbcTemplate.queryForObject(schSeq, Integer.class);
		nseqNo++;


		jsonScheduler.put("TaskId", nseqNo);
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

		sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
		String strdate= sdf.format( new java.util.Date() ) ;

		String sdate=strdate.substring(0,10);    //java.time.LocalDate.now().toString();
		sdate=sdate + "T00:00:00.000Z";
		String senddate=strdate.substring(0,10)+"T23:59:00.000Z";     //java.time.LocalDate.now().toString()+"T23:59:00.000Z";

		String sQuery="insert into graphicalschedulermaster(nschedulecode,jsondata,nstatus,stitle,sscheduletype,dmodifieddate,nsitecode,nschedulestatus) values("+nseqNo+",'" + jsonScheduler.toString() + "'::JSONB,"
				+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+",'"+stitle+"','"+sscheduletype+"','"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode()+",1)";

		jdbcTemplate.execute(sQuery);

		sQuery = "update seqnoscheduler set nsequenceno=" + nseqNo
				+ " where stablename='graphicalschedulermaster'";

		jdbcTemplate.execute(sQuery);

		sQuery="select public.fn_schedulertransactions('"+sdate+"','"+senddate+"')";

		jdbcTemplate.execute(sQuery);
		savedInstList.add(scheduler);


		auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_ADDSCHEDULER"), userInfo);
		return new ResponseEntity<>(getGraphicalScheduler(userInfo).getBody(), HttpStatus.OK);//.getBody()
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> updateGraphicalScheduler(Map<String, Object> inputMap) throws Exception {

		ObjectMapper objmap = new ObjectMapper();
		final List<Object> savedInstList = new ArrayList<>();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		Map<String, Object> inputMaping=(Map<String, Object>) inputMap.get("scheduleData");


		GraphicalSchedulerMaster scheduler = objmap.convertValue(inputMaping.get("graphicalschedulermaster"),
				new TypeReference<GraphicalSchedulerMaster>() {
		});


		String stitle = "";
		String sscheduletype="";
		Integer nTaskId=0;
		if (inputMap.get("stitle") != null) {
			stitle = (String) inputMap.get("stitle");
		}
		if (inputMap.get("sscheduletype") != null) {
			sscheduletype = (String) inputMap.get("sscheduletype");
		}
		if (inputMap.get("ntaskid") != null) {
			nTaskId = (Integer) inputMap.get("ntaskid");
		}

		JSONObject jsonScheduler = new JSONObject(scheduler.getJsondata());

		jsonScheduler.put("TaskId", nTaskId);

		String sQuery="update graphicalschedulermaster set jsondata = '" + jsonScheduler.toString() + "'::JSONB,stitle = '"+stitle+"',sscheduletype = '"+sscheduletype+"',dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo) +"' "
				+ " where nschedulecode = "+nTaskId;

		jdbcTemplate.execute(sQuery);


		savedInstList.add(scheduler);


		auditUtilityFunction.fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_UPDATESCHEDULER"), userInfo);
		return new ResponseEntity<>(getGraphicalScheduler(userInfo).getBody(), HttpStatus.OK);//.getBody()
	}

	@Override
	//@SuppressWarnings("unchecked")
	public ResponseEntity<Object> deleteGraphicalScheduler(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmap = new ObjectMapper();
		//final List<Object> savedInstList = new ArrayList<>();
		objmap.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		Integer nTaskId=0;

		if (inputMap.get("ntaskid") != null) {
			nTaskId = (Integer) inputMap.get("ntaskid");
		}

		String sQuery="update graphicalschedulermaster set nstatus = -1,dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo) +"' where nschedulecode = "+nTaskId;

		jdbcTemplate.execute(sQuery);
		//savedInstList.add(scheduler);
		//fnInsertAuditAction(savedInstList, 1, null, Arrays.asList("IDS_UPDATESCHEDULER"), userInfo);
		return new ResponseEntity<>(getGraphicalScheduler(userInfo).getBody(), HttpStatus.OK);//.getBody()
	}

	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getGraphicalSchedulerView(Integer nscheduleCode, final UserInfo userInfo,String sscheduleType) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		GraphicalSchedulerMaster selectedScheduler = null;

		String sQuery="";
		String sSchType = "";
		List<GraphicalSchedulerMaster> schedulerList=null;

		final String strQuery ="select distinct sscheduletype from graphicalschedulermaster where nsitecode = "+userInfo.getNmastersitecode()
		+" and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		schedulerList = jdbcTemplate.query(strQuery,
				new GraphicalSchedulerMaster());

		if (schedulerList.isEmpty()) {
			outputMap.put("filterGrapicalScheduleType", schedulerList);
			outputMap.put("Scheduleriew", null);
			outputMap.put("SelectedSchedulerView", null);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else
		{
			selectedScheduler = schedulerList.get(schedulerList.size() - 1);

			if(sscheduleType == null)
			{
				sSchType=  selectedScheduler.getSscheduletype();
			}
			else
			{
				sSchType=sscheduleType;
			}
			if(nscheduleCode == null)
			{

				sQuery ="select nschedulecode,coalesce(g.jsondata) as jsondata,nstatus,stitle,sscheduletype "
						+ " from graphicalschedulermaster g where g.nsitecode = "+userInfo.getNmastersitecode()
						+ " and g.sscheduletype = '"+sSchType+"' and "
						+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			}
			else
			{
				sQuery ="select nschedulecode,coalesce(g.jsondata) as jsondata,nstatus,stitle,sscheduletype "
						+ " from graphicalschedulermaster g where g.nschedulecode = "+nscheduleCode+" and g.sscheduletype = '"+sSchType+"' and "
						+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				List<GraphicalSchedulerMaster> grapschedulerLst = jdbcTemplate.query(sQuery,
						new GraphicalSchedulerMaster());
				selectedScheduler = grapschedulerLst.get(grapschedulerLst.size() - 1);
				sscheduleType=sSchType;
				sQuery ="select nschedulecode,coalesce(g.jsondata) as jsondata,nstatus,stitle,sscheduletype "
						+ " from graphicalschedulermaster g where g.nsitecode = "+userInfo.getNmastersitecode()
						+ " and g.sscheduletype = '"+sSchType+"' and "
						+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			}
		}

		List<GraphicalSchedulerMaster> grapschedulerList = jdbcTemplate.query(sQuery,
				new GraphicalSchedulerMaster());

		if (grapschedulerList.isEmpty()) {
			outputMap.put("filterGrapicalScheduleType", schedulerList);
			outputMap.put("SchedulerView", grapschedulerList);
			outputMap.put("SelectedSchedulerView", null);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			outputMap.put("filterGrapicalScheduleType", schedulerList);
			outputMap.put("SchedulerView", grapschedulerList);
			if(sscheduleType == null)
			{
				selectedScheduler = grapschedulerList.get(grapschedulerList.size() - 1);
			}

		}


		if (selectedScheduler == null) {
			final String returnString = commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedSchedulerView", selectedScheduler);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getGraphicalSchedulerByScheduleType(String sscheduleType, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		GraphicalSchedulerMaster selectedScheduler = null;

		final String strQuery ="select distinct sscheduletype from graphicalschedulermaster where nsitecode = "+userInfo.getNmastersitecode()
		+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		outputMap.put("filterGrapicalScheduleType",   jdbcTemplate.query(strQuery, new GraphicalSchedulerMaster()));
		String query="";


		query ="select nschedulecode,coalesce(g.jsondata) as jsondata,nstatus,stitle,sscheduletype "
				+ " from graphicalschedulermaster g where g.nsitecode = "+userInfo.getNmastersitecode()
				+ " and g.sscheduletype = '"+sscheduleType+"' and "
				+ " g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


		List<GraphicalSchedulerMaster> schedulerList = jdbcTemplate.query(query,
				new GraphicalSchedulerMaster());

		if (schedulerList.isEmpty()) {
			outputMap.put("SchedulerView", schedulerList);
			outputMap.put("SelectedSchedulerView", null);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			outputMap.put("SchedulerView", schedulerList);
			selectedScheduler = schedulerList.get(schedulerList.size() - 1);


		}

		if (selectedScheduler == null) {
			final String returnString = commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedSchedulerView", selectedScheduler);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

}
