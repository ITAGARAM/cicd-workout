package com.agaramtech.qualis.project.service.projectview;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ProjectViewDAOImpl implements ProjectViewDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectViewDAOImpl.class);
	
	private final ProjectTypeDAO projecttypeDAO;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;

	@Override
	public ResponseEntity<Object> getProjectView(String fromDate, String toDate, final String currentUIDate,
			UserInfo objUserInfo) throws Exception {
		int nprojectmastercode = -1;
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		List<SampleType> lstSampleType = new ArrayList<>();
		List<ProjectType> lstProjectMaster = new ArrayList<>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(objUserInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			map.put("FromDate", mapObject.get("FromDateWOUTC"));
			map.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(objUserInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			map.put("FromDate", fromDateUI);
			map.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, objUserInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, objUserInfo, true));
		}
		map.putAll((Map<String, Object>) getSampleTypeForProjectView(objUserInfo).getBody());
		lstProjectMaster = (List<ProjectType>) map.get("filterProjectType");
		lstSampleType = (List<SampleType>) map.get("filterSampleType");
		int nsampletypecode = -1;
		int nprojecttypecode = -1;
//		if (lstSampleType != null && lstSampleType.size() > 0) {
		LOGGER.info("getProjectView() called");
		if (lstSampleType != null && lstSampleType.size() > 0) {

			nsampletypecode = lstSampleType.get(0).getNsampletypecode();
			map.put("SelectedSampleType", lstSampleType.get(0));

		} else {
			map.put("SelectedSampleType", null);
		}

		if (lstProjectMaster != null && lstProjectMaster.size() > 0) {

			nprojecttypecode = lstProjectMaster.get(0).getNprojecttypecode();
			map.put("SelectedProjectType", lstProjectMaster.get(0));
		} else {
			map.put("SelectedProjectType", null);
		}

		final List<ProjectMaster> listProjectView = getProjectView(fromDate, toDate, nsampletypecode, nprojecttypecode,
				objUserInfo);
		map.put("ProjectView", listProjectView);
		if (listProjectView.size() > 0) {
			final ProjectMaster selectedProjectview = listProjectView.get(listProjectView.size() - 1);
			map.put("SelectedProjectView", selectedProjectview);
			nprojectmastercode = selectedProjectview.getNprojectmastercode();
			final List<ProjectMaster> listParentProjectView = (List<ProjectMaster>) getParentProjectView(
					nsampletypecode, nprojectmastercode, objUserInfo).getBody();
			map.put("ParentProjectView", listParentProjectView);
			final List<ProjectMaster> listChildProjectView = (List<ProjectMaster>) getChildProjectView(nsampletypecode,
					nprojectmastercode, objUserInfo).getBody();
			map.put("ChildProjectView", listChildProjectView);

		} else {

			map.put("SelectedProjectView", null);
			map.put("ParentProjectView", null);
			map.put("ChildProjectView", null);
		}

//		} else {
//
//			map.put("SelectedProjectView", null);
//			map.put("ParentProjectView", null);
//			map.put("ChildProjectView", null); 
//		} 
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	private ResponseEntity<List<ProjectMaster>> getParentProjectView(Integer nsampletypecode, int nprojectmastercode,
			UserInfo objUserInfo) throws Exception {

		final String strarnoQuery = "select pm.nprojectmastercode,coalesce(rte.jsondata->'sregtypename'->>'en-US',rte.jsondata->'sregtypename'->>'en-US') as sregtypename, "
				+ "coalesce( ts.jsondata->'stransdisplaystatus'->>'" + objUserInfo.getSlanguagetypecode()
				+ "', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'  ) stransdisplaystatus, rth.ntransactionstatus ,"
				+ "coalesce(rst.jsondata->'sregsubtypename'->>'en-US',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,rar.sarno,rar.npreregno, p.sproductname "
				+ "from registration r,registrationtest rt,registrationarno rar, "
				+ "registrationtype rte,registrationsubtype rst,projectmaster pm,registrationtesthistory rth ,product p , transactionstatus ts "
				+ "where r.npreregno=rt.npreregno and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and rte.nsampletypecode=r.nsampletypecode and rte.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rte.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and rte.nregtypecode=rst.nregtypecode and rst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and rar.npreregno=r.npreregno and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nsitecode="
				+ objUserInfo.getNmastersitecode() + " "
				+ "and pm.nprojectmastercode=r.nprojectmastercode and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and p.nproductcode = r.nproductcode and r.nregtypecode=rte.nregtypecode and r.nregsubtypecode=rst.nregsubtypecode and r.nsampletypecode="
				+ nsampletypecode + " " + "and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and rth.ntesthistorycode = any ( "
				+ " select max(ntesthistorycode) from registrationtest t1,registrationtesthistory t2 where t1.ntransactiontestcode=t2.ntransactiontestcode group by t2.npreregno )"
//				+ "select ntesthistorycode from ( "
//				+ "select ntesthistorycode,ROW_NUMBER() OVER(PARTITION BY ntransactiontestcode order by dtransactiondate desc)RN "
//				+ "from registrationtesthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
//				+ "t where t.rn=1) "
				+ " and ts.ntranscode = rth.ntransactionstatus " + "and rth.ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") " + "and pm.nprojectmastercode="
				+ nprojectmastercode + " ";
//				+ "group by rar.sarno,rte.jsondata,rst.jsondata,pm.nprojectmastercode,rar.npreregno";

		return new ResponseEntity<>(jdbcTemplate.query(strarnoQuery, new ProjectMaster()), HttpStatus.OK);
	}

	private ResponseEntity<List<ProjectMaster>> getChildProjectView(Integer nsampletypecode, int nprojectmastercode,
			UserInfo objUserInfo) throws Exception {

		final String strTestQuery = "select pm.nprojectmastercode,rt.jsondata->>'ncost' as ncost, rt.jsondata->>'stestsynonym' as stestsynonym, "
				+ "rar.ssamplearno as ssamplearno, coalesce( ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "', ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'  ) stransdisplaystatus, rth.ntransactionstatus ,"
				+ "rt.npreregno from registration r,registrationtest rt, projectmaster pm,registrationsamplearno rar, "
				+ "registrationtesthistory rth ,transactionstatus ts where r.npreregno=rt.npreregno and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r,nsitecode="
				+ objUserInfo.getNmastersitecode() + "  and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode="
				+ objUserInfo.getNmastersitecode() + "  "
				+ "and pm.nprojectmastercode=r.nprojectmastercode and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and rar.npreregno=r.npreregno and r.nsampletypecode=" + nsampletypecode + " " + "and rar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rar.nsitecode="
				+ objUserInfo.getNmastersitecode()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.ntransactionstatus = ts.ntranscode and rar.ntransactionsamplecode = rt.ntransactionsamplecode and "
				+ " rth.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and rth.ntesthistorycode = any ( "
				+ " select max(ntesthistorycode) from registrationtest t1,registrationtesthistory t2 where t1.ntransactiontestcode=t2.ntransactiontestcode group by t2.ntransactiontestcode )"
				+ " and rth.ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") " + " and pm.nprojectmastercode="
				+ nprojectmastercode + "";

		return new ResponseEntity<>(jdbcTemplate.query(strTestQuery, new ProjectMaster()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSampleTypeForProjectView(final UserInfo objUserInfo) throws Exception {
		Map<String, Object> returnObj = new HashMap<>();
		final String getSampleType = "select nsampletypecode,nsorter,coalesce(jsondata->'sampletypename'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',jsondata->'sampletypename'->>'en-US') as ssampletypename "
				+ " from sampletype where napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode() + " and nsampletypecode="
				+ Enumeration.SampleType.PROJECTSAMPLETYPE.getType() + " order by nsorter;";

		List<SampleType> sampleTypeList = jdbcTemplate.query(getSampleType, new SampleType());

		if (sampleTypeList.isEmpty()) {
			return new ResponseEntity<>(sampleTypeList, HttpStatus.OK);
		} else {
			final List<ProjectType> regTypList = projecttypeDAO.getProjectType(objUserInfo)
					.getBody();
			returnObj.put("filterSampleType", sampleTypeList);
			returnObj.put("filterProjectType", regTypList);
			return new ResponseEntity<>(returnObj, HttpStatus.OK);
		}
	}

	private List<ProjectMaster> getProjectView(String fromDate, String toDate, int nsampletypecode,
			int nprojecttypecode, UserInfo objUserInfo) {

		final String strQuery = "select pm.nprojectmastercode,pm.sprojectcode as sprojectcode,pm.sprojecttitle as sprojecttitle,pt.nprojecttypecode,pm.sprojectname, pm.sprojectdescription ,"
				+ "COALESCE(TO_CHAR(pm.dprojectstartdate,'" + objUserInfo.getSsitedate()
				+ "'),'') as sprojectstartdate,COALESCE(TO_CHAR(pm.dexpectcompletiondate,'" + objUserInfo.getSsitedate()
				+ "'),'') as sexpectcompletiondate, "
				+ "sum((rt.jsondata->>'ncost')::real) as ncost,pt.sprojecttypename as sprojecttypename,coalesce(ts.jsondata->'stransdisplaystatus'->> '"
				+ objUserInfo.getSlanguagetypecode() + "') stransdisplaystatus,pmh.ntransactionstatus, c.sclientname "
				+ "from registration r,registrationtest rt,projectmaster pm,projectmasterhistory pmh,registrationtesthistory rth,projecttype pt,transactionstatus ts , client c "
				+ " where r.npreregno=rt.npreregno " + "and pm.nprojectmastercode=r.nprojectmastercode and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nsitecode="
				+ objUserInfo.getNmastersitecode() + " "
				+ "and pt.nprojecttypecode = pm.nprojecttypecode and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and c.nclientcode = pm.nclientcode "
				+ "and pmh.nprojectmastercode = pm.nprojectmastercode and pmh.ntransactionstatus = ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and r.nsampletypecode=" + nsampletypecode
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nsitecode="
				+ objUserInfo.getNmastersitecode() + " " + "and rth.ntesthistorycode = any ( "
				+ " select max(ntesthistorycode) from registrationtest t1,registrationtesthistory t2   "
				+ " where t1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and t2.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t1.ntransactiontestcode=t2.ntransactiontestcode group by t2.ntransactiontestcode )"
//						     + "select ntesthistorycode from ( "
//						     + "select ntesthistorycode,ROW_NUMBER() OVER(PARTITION BY ntransactiontestcode order by dtransactiondate desc)RN "
//						     + "from registrationtesthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
//						     + "t where t.rn=1) "
				+ "and rth.ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," + ""
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ") "
//							 + "and pt.nprojecttypecode="+nprojecttypecode+" "
				+ "and pmh.nprojectmasterhistorycode = any(select max(ph.nprojectmasterhistorycode) from projectmasterhistory ph, projectmaster pm where ph.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ph.nsitecode = "
				+ objUserInfo.getNtranssitecode()
				+ "and ph.nprojectmastercode = pm.nprojectmastercode and pm.nprojecttypecode= " + nprojecttypecode
				+ " group by ph.nprojectmastercode )" + "and pm.dprojectstartdate between '" + fromDate + "' and '"
				+ toDate
				+ "'group by pm.sprojectcode,pm.nprojectmastercode,ts.jsondata,pt.nprojecttypecode,pm.sprojecttitle,pmh.ntransactionstatus,pm.sprojectname, pm.sprojectdescription ,pm.dprojectstartdate,pm.dexpectcompletiondate,c.sclientname";
		ObjectMapper objMapper = new ObjectMapper();
		List<ProjectMaster> listProjectView = (List<ProjectMaster>) jdbcTemplate.query(strQuery, new ProjectMaster());
		return objMapper.convertValue(listProjectView, new TypeReference<List<ProjectMaster>>() {
		});
	}

	@Override
	public ResponseEntity<Object> getProjectViewBySampleType(String fromDate, String toDate, final String currentUIDate,
			Integer nsampletypecode, Integer nprojecttypecode, UserInfo objUserInfo) throws Exception {

		int nprojectmastercode = -1;
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		List<SampleType> lstSampleType = new ArrayList<>();
//		List<ProjectType> lstProjectMaster = new ArrayList<>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(objUserInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			map.put("FromDate", mapObject.get("FromDateWOUTC"));
			map.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(objUserInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			map.put("FromDate", fromDateUI);
			map.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, objUserInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, objUserInfo, true));
		}

		map.putAll((Map<String, Object>) getSampleTypeForProjectView(objUserInfo).getBody());
//		lstProjectMaster = (List<ProjectType>) map.get("filterProjectType");
		lstSampleType = (List<SampleType>) map.get("filterSampleType");
		if (lstSampleType != null && lstSampleType.size() > 0) {
			map.put("SelectedSampleType", lstSampleType.get(0));
//		    map.put("SelectedProjectType", lstProjectMaster.get(0));
			final List<ProjectMaster> listProjectView = getProjectView(fromDate, toDate, nsampletypecode,
					nprojecttypecode, objUserInfo);
			map.put("ProjectView", listProjectView);
			final ResponseEntity<Object> projectTypeResponse = projecttypeDAO.getActiveProjectTypeById(nprojecttypecode,
					objUserInfo);
			map.put("SelectedProjectType", projectTypeResponse.getBody());
			if (listProjectView.size() > 0) {

				final ProjectMaster selectedProjectview = listProjectView.get(listProjectView.size() - 1);
				map.put("SelectedProjectView", selectedProjectview);
//				map.put("SelectedProjectType", selectedProjectview);

				nprojectmastercode = selectedProjectview.getNprojectmastercode();
			} else {
				map.put("SelectedProjectView", null);
			}
			final List<ProjectMaster> listParentProjectView = (List<ProjectMaster>) getParentProjectView(
					nsampletypecode, nprojectmastercode, objUserInfo).getBody();
			map.put("ParentProjectView", listParentProjectView);
			final List<ProjectMaster> listChildProjectView = (List<ProjectMaster>) getChildProjectView(nsampletypecode,
					nprojectmastercode, objUserInfo).getBody();
			map.put("ChildProjectView", listChildProjectView);

		} else {
			map.put("SelectedProjectView", null);
			map.put("ParentProjectView", null);
			map.put("ChildProjectView", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveProjectViewById(Integer nsampletypecode, int nprojectmastercode,
			UserInfo userInfo) throws Exception {

		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final String strQuery = "select pm.nprojectmastercode,max(pm.sprojectcode)as sprojectcode,max(pm.sprojecttitle) as sprojecttitle,pm.sprojectname, "
				+ "sum((rt.jsondata->>'ncost')::real) as ncost,max(pt.sprojecttypename) as sprojecttypename,pm.sprojectdescription ,COALESCE(TO_CHAR(pm.dprojectstartdate,'dd/MM/yyyy'),'') as sprojectstartdate,COALESCE(TO_CHAR(pm.dexpectcompletiondate,'dd/MM/yyyy'),'') as sexpectcompletiondate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->> '" + userInfo.getSlanguagetypecode()
				+ "') stransdisplaystatus,pmh.ntransactionstatus ,c.sclientname "
				+ "from registration r,registrationtest rt,projectmaster pm,registrationtesthistory rth,projecttype pt,transactionstatus ts ,projectmasterhistory pmh, transactionstatus ts1, client c "
				+ " where r.npreregno=rt.npreregno and pm.nprojectmastercode = pmh.nprojectmastercode and ts1.ntranscode = pmh.ntransactionstatus and ts.ntranscode = rth.ntransactionstatus"
				+ " and c.nclientcode = pm.nclientcode and pm.nprojectmastercode=r.nprojectmastercode and pm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pt.nprojecttypecode = pm.nprojecttypecode and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pmh.ntransactionstatus = ts1.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and r.nsampletypecode="
				+ nsampletypecode + " and rt.ntransactiontestcode=rth.ntransactiontestcode and rth.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rth.ntesthistorycode = any ( "
//			     + "select ntesthistorycode from ( "
//			     + "select ntesthistorycode,ROW_NUMBER() OVER(PARTITION BY ntransactiontestcode order by dtransactiondate desc)RN "
//			     + "from registrationtesthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
//			     + "t where t.rn=1)	 "
				+ " select max(ntesthistorycode) from registrationtest t1,registrationtesthistory t2   "
				+ " where t1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and t2.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t1.ntransactiontestcode=t2.ntransactiontestcode group by t2.ntransactiontestcode )"
				+ " and rth.ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " )"
				+ " and pmh.nprojectmasterhistorycode=ANY (select max(nprojectmasterhistorycode) from projectmasterhistory  where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and nprojectmastercode = "
				+ nprojectmastercode + "and ntransactionstatus not in ("
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ") " + ") and pmh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nprojectmastercode = "
				+ nprojectmastercode + " "
				+ " group by pm.sprojectcode,pm.nprojectmastercode,ts1.jsondata,pt.nprojecttypecode,pmh.ntransactionstatus,pm.sprojectdescription,sprojectstartdate,sexpectcompletiondate,c.sclientname,pm.sprojectname ";

		List<ProjectMaster> listProjectView = (List<ProjectMaster>) jdbcTemplate.query(strQuery, new ProjectMaster());
		map.put("SelectedProjectView", listProjectView.get(0));
		final List<ProjectMaster> listParentProjectView = (List<ProjectMaster>) getParentProjectView(nsampletypecode,
				nprojectmastercode, userInfo).getBody();
		map.put("ParentProjectView", listParentProjectView);
		final List<ProjectMaster> listChildProjectView = (List<ProjectMaster>) getChildProjectView(nsampletypecode,
				nprojectmastercode, userInfo).getBody();
		map.put("ChildProjectView", listChildProjectView);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
}
