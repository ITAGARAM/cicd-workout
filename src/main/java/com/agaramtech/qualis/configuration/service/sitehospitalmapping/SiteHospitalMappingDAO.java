package com.agaramtech.qualis.configuration.service.sitehospitalmapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface SiteHospitalMappingDAO {
	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getSiteHospitalMapping(UserInfo userInfo) ;
	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}"nmappingcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> editSiteAndBioBank(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"SiteHospitalMapping" = {
	 *                 "nmappingsitecode": 1, "ssitecode": 'AS', "sdescription": '',
	 *                 "ssitetypename": 1, "nsitetypecode": 'Cancer', "ssitename":
	 *                 'LIMS' }}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> createSiteAndBioBank(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getHospitalMaster(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1,2,3,4,5, "nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalcode":
	 *                 'ASS,DF,GH,JK,LK', "shospitalname": 'JK,KL,LO,OI,IU,,}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> createHospitalMaster(Map<String, Object> inputMap,UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1"nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalcode":
	 *                 'ASS', "shospitalname": 'JK'}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> deleteHospitalMaster(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getSiteHospitalMappingRecord(int nmaapingsitecode, UserInfo userInfo)throws Exception;

}
