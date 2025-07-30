package com.agaramtech.qualis.storagemanagement.service.aliquotplan;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.barcode.model.SampleDonor;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.AliquotPlan;
/**
 * This interface holds declarations to perform CRUD operation on 'aliquotplan' table
 * @author ATE234
 * @version 9.0.0.1
 * @since
 */
public interface AliquotPlanDAO {

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
	public ResponseEntity<Object> getAliquotPlan(UserInfo userInfo)throws Exception;
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
	public ResponseEntity<Object> getProjecttype(UserInfo userInfo) throws Exception;
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
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
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
	public ResponseEntity<Object> getCollectionTubeType(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
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
	public ResponseEntity<Object> getPatientCatgory(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
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
	public ResponseEntity<Object> getVisitName(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
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
	public ResponseEntity<Object> getUnit(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
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
	public ResponseEntity<Object> getSampleDonor(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to add a new entry to aliquotplan  table.
	 * @param objaliquotplan [aliquotplan] object holding details to be added in aliquotplan table
	 * @return response entity object holding response status and data of added aliquotplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This interface declaration is used to update entry in aliquotplan  table.
	 * @param objaliquotplan [aliquotplan] object holding details to be updated in aliquotplan table
	 * @return response entity object holding response status and data of updated aliquotplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve active aliquotplan object based on the specified naliquotplanCode.
	 * @param naliquotplanCode [int] primary key of aliquotplan object
	 * Input : {naliquotplancode:1,"userinfo":{nmastersitecode": -1}}
	 * @return response entity  object holding response status and data of aliquotplan object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public AliquotPlan getActiveAliquotPlanById(int naliquotplancode, UserInfo userInfo)throws Exception;

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

	public ResponseEntity<Object> deleteAliquotPlan(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	// ALPD-5513 - added by Gowtham R on 10/3/25 - added sample donor field from sampledonor table
	/**
	 * This method is used to retrieve active sampledonor object based on the specified nsampledonorcode.
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @return response entity  object holding response status and data of sampledonor object
	 * @throws Exception that are thrown from this Service layer
	 */
	public SampleDonor isSampleDonorActive(final int nsampledonorcode, UserInfo userInfo) throws Exception;

}
