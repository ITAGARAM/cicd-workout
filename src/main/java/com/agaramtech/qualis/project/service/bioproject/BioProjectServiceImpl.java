package com.agaramtech.qualis.project.service.bioproject;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.project.model.BioProject;
import com.agaramtech.qualis.project.model.DiseaseCategory;
import com.agaramtech.qualis.configuration.model.InterfacerMapping;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'bioproject' table through its DAO layer.
 */
/**
 * @author ATE234 Janakumar
 * BGSI-3
 * 26/06/2025
 */
@Service
@Transactional(readOnly= true, rollbackFor= Exception.class)
public class BioProjectServiceImpl implements BioProjectService{

	private final BioProjectDAO bioProjectDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param diseaseCategoryDAO DiseaseCategoryDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public BioProjectServiceImpl(BioProjectDAO bioProjectDAO, CommonFunction commonFunction) {
		super();
		this.bioProjectDAO = bioProjectDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) {
		// TODO Auto-generated method stub
		return bioProjectDAO.getBioProject(userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 * inputMap:[{"userinfo":{nmastersitecode": -1},"ndiseasecategory":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getDiseaseByCatgory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioProjectDAO.getDiseaseByCatgory(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *        {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioProjectDAO.getUsers(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": null,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioProjectDAO.createBioProject(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"bioproject":{"nbioprojectcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getActiveBioProjectById(int nbioprojectcode, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final BioProject objinterfacermapping = bioProjectDAO.getActiveBioProjectById(nbioprojectcode, userInfo);
		if (objinterfacermapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objinterfacermapping, HttpStatus.OK);
		}
		//return bioProjectDAO.getActiveBioProjectById(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioProjectDAO.deleteBioProject(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available bioproject(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "bioproject": { "nsitecode": -1, "sdiseasecategoryname": "2",
	 *                 "ndiseasecategorycode": 2, "nbioprojectcode": 1,
	 *                 "ndiseasecode": 2, "nusercode": -1, "sdescription": "ss",
	 *                 "sdiseasename": "qwewqeqwe", "sprojectcode": "ss",
	 *                 "sprojecttitle": "sss", "suserName": "QuaLIS Admin" },
	 *                 "userinfo": { "nusercode": -1, "nuserrole": -1,
	 *                 "ndeputyusercode": -1, "ndeputyuserrole": -1, "nmodulecode":
	 *                 54 "activelanguagelist": ["en-US"], "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "ndeptcode": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 255,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 4,
	 *                 "nlogintypecode": 1, "nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0, "nsiteadditionalinfo": 4, "nsitecode":
	 *                 1, "ntimezonecode": -1, "ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1, "nusersitecode": -1, "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=BGSI27-06;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sdeptname": "NA",
	 *                 "sdeputyid": "system", "sdeputyusername": "QuaLIS Admin",
	 *                 "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "sformname": "Bio Project", "sgmtoffset": "UTC
	 *                 +00:00", "shostip": "0:0:0:0:0:0:0:1", "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "slastname": "Admin", "sloginid": "system",
	 *                 "smodulename": "Project", "spassword": null,
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "6C3788716A7237005E1E962B3105ACF1",
	 *                 "ssitecode": "SYNC", "ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss", "ssitename": "LIMS",
	 *                 "ssitereportdate": "dd/MM/yyyy", "ssitereportdatetime":
	 *                 "dd/MM/yyyy HH:mm:ss", "stimezoneid": "Europe/London",
	 *                 "susername": "QuaLIS Admin", "suserrolename": "QuaLIS Admin"
	 *                 }
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateBioProject(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioProjectDAO.updateBioProject(inputMap,userInfo);
	}

	
}
