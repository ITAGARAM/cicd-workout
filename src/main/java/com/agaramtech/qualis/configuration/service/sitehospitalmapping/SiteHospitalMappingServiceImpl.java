package com.agaramtech.qualis.configuration.service.sitehospitalmapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SiteHospitalMappingServiceImpl implements SiteHospitalMappingService{

	private final SiteHospitalMappingDAO siteHospitalMappingDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param SiteHospitalMappingDAO siteHospitalMappingDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SiteHospitalMappingServiceImpl(SiteHospitalMappingDAO siteHospitalMappingDAO, CommonFunction commonFunction) {
		this.siteHospitalMappingDAO = siteHospitalMappingDAO;
		this.commonFunction = commonFunction;
	}
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
	@Override
	public ResponseEntity<Object> getSiteHospitalMapping(UserInfo userInfo) {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.getSiteHospitalMapping(userInfo);
	}
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
	@Override
	public ResponseEntity<Object> editSiteAndBioBank(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.editSiteAndBioBank(inputMap ,userInfo);
	}
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
	@Transactional
	@Override
	public ResponseEntity<Object> createSiteAndBioBank(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.createSiteAndBioBank(inputMap ,userInfo);
	}
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
	@Override
	public ResponseEntity<Object> getHospitalMaster(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.getHospitalMaster(inputMap,userInfo);
	}
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
	@Transactional
	@Override
	public ResponseEntity<Object> createHospitalMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.createHospitalMaster(inputMap,userInfo);

	}
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteHospitalMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.deleteHospitalMaster(inputMap,userInfo);

	}
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
	@Override
	public ResponseEntity<Object> getSiteHospitalMappingRecord(int nmaapingsitecode, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return siteHospitalMappingDAO.getSiteHospitalMappingRecord(nmaapingsitecode,userInfo);

	}

}
