package com.agaramtech.qualis.storagemanagement.service.aliquotplan;

//import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.barcode.model.SampleDonor;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.AliquotPlan;

/**
 * This class holds methods to perform CRUD operation on 'aliquotplan' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class AliquotPlanServicesImpl implements AliquotPlanService {

	private final AliquotPlanDAO aliquotPlanDAO;
	private final CommonFunction commonFunction;

	public AliquotPlanServicesImpl(AliquotPlanDAO aliquotPlanDAO, CommonFunction commonFunction) {
		this.aliquotPlanDAO = aliquotPlanDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getAliquotPlan(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getAliquotPlan(userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 *
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getProjecttype(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getProjecttype(userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getSampleType(inputMap, userInfo);
	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getCollectionTubeType(inputMap, userInfo);
	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getPatientCatgory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getPatientCatgory(inputMap, userInfo);
	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getVisitName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getVisitName(inputMap, userInfo);
	}
	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getUnit(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getUnit(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all aliquotplans with respect to site
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * Input : {{sampletypevalue: 9, sampletypename: "Neonatal Sepsis"},"userinfo":{nmastersitecode": -1}}
	 * 	sampletypevalue value is nprojecttype primary key value.
	 * @return a response entity which holds the list of aliquotplan with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	@Override
	public ResponseEntity<Object> getSampleDonor(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.getSampleDonor(inputMap, userInfo);
	}

	// ALPD-5513 - added (sampleDonor check) by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * On successive insert get the new inserted record along with default status from transaction status
	 * @param objaliquotplan [aliquotplan] object holding details to be added in aliquotplan table
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final SampleDonor sampleDonor = aliquotPlanDAO.isSampleDonorActive((int) inputMap.get("nsampledonorcode"), userInfo);
		if(sampleDonor == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage
					("IDS_SAMPLEDONORDELETED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {
			return aliquotPlanDAO.createAliquotPlan(inputMap, userInfo);
		}
	}

	// ALPD-5513 - added (sampleDonor check) by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final SampleDonor sampleDonor = aliquotPlanDAO.isSampleDonorActive((int) inputMap.get("nsampledonorcode"), userInfo);
		if(sampleDonor == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage
					("IDS_SAMPLEDONORDELETED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {
			return aliquotPlanDAO.updateAliquotPlan(inputMap, userInfo);
		}
	}


	/**
	 * This method is used to retrieve active aliquotplan object based on the specified naliquotplanCode.
	 * @param naliquotplanCode [int] primary key of aliquotplan object
	 * Input : {naliquotplancode:1,"userinfo":{nmastersitecode": -1}}
	 * @return response entity  object holding response status and data of aliquotplan object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getActiveAliquotPlanById(int naliquotplancode, UserInfo userInfo) throws Exception {
		// TODO Auto-vreturn null;

		final AliquotPlan objAliquotPlan =aliquotPlanDAO.getActiveAliquotPlanById(naliquotplancode, userInfo);

		if(objAliquotPlan == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objAliquotPlan, HttpStatus.OK);
		}

	}


	/**
	 * This method is used to add a new entry to aliquotplan table.
	 * @param inputMap [Map] holds the aliquotplan object to be inserted
	 * Input : {ncontrolCode:234,naliquotplancode:1,nsampledonorcode:1,operation:"update",patientcatvalue:-1,productname:"Blood",productvalue:1,projecttypename:"PTB",
		projecttypevalue:1,saliquotno:"09",sdescription:"Rak",squantity:"34.09",ssampledonor:"Mother",tubename:"EDTA",tubevalue:1,
		unitname:"nos",unitvalue:6,visitname:"26-28 weeks (Visit-3)",visitnumber:3	,"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 234,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return inserted aliquotplan object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return aliquotPlanDAO.deleteAliquotPlan(inputMap, userInfo);
	}


}
