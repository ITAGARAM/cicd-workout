package com.agaramtech.qualis.storagemanagement.service.samplestoragestructure;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageVersion;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SampleStorageStructureServiceImpl implements SampleStorageStructureService {

	private final SampleStorageStructureDAO sampleStorageStructureDAO;

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSampleStorageStructure(final int nsampleStorageLocationCode,
			final int nstorageCategoryCode, final int nsampleStorageVersionCode, final UserInfo userInfo)
					throws Exception {
		return sampleStorageStructureDAO.getSampleStorageStructure(nsampleStorageLocationCode, nstorageCategoryCode,
				nsampleStorageVersionCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1},nsampleStorageLocationCode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<? extends Object> getSelectedSampleStorageStructure(final int nsampleStorageLocationCode,
			final int nsampleStorageVersionCode, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.getSelectedSampleStorageStructure(nsampleStorageLocationCode,
				nsampleStorageVersionCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.createSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.updateSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1 },
	 *                 "sampleStorageVersion": { "nstatus": 1, "jsondata": { "data":
	 *                 [ { "text": "root", "expanded": false, "editable": false,
	 *                 "root": true, "selected": false, "id":
	 *                 "3a4fefa9-41ec-4146-8e14-babca2400079", "items": [ { "id":
	 *                 "1094c8ac-f481-4271-be78-dea50c4493e6", "text": "Label",
	 *                 "expanded": false, "editable": false } ] } ] } },
	 *                 "additionalinfo": { "nstatus": 1,
	 *                 "ssamplestoragelocationname": "2eqqwe",
	 *                 "nstoragecategorycode": 3, "nproductcode": -1, "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nneedautomapping": 4,
	 *                 "nneedposition": 4, "nnoofcontainer": 1, "nprojecttypecode":
	 *                 -1, "nquantity": 0, "nrow": 1, "nunitcode": -1,
	 *                 "sstoragecategoryname": "Deep Freezer (-80°C)" }, "userinfo":
	 *                 { "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71, "nformcode": 168,
	 *                 "nmastersitecode": -1, "nsitecode": 1, "ntranssitecode": 1,
	 *                 "nusersitecode": -1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nisstandaloneserver": 4, "nissyncserver": 3,
	 *                 "nlogintypecode": 1, "nsiteadditionalinfo": 4,
	 *                 "ntimezonecode": -1, "istimezoneshow": 4, "isutcenabled": 4,
	 *                 "slanguagefilename": "Msg_en_US", "slanguagename": "English",
	 *                 "slanguagetypecode": "en-US", "activelanguagelist":
	 *                 ["en-US"], "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.deleteSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<? extends Object> getActiveSampleStorageVersion(final int nsampleStorageVersionCode,
			final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.getActiveSampleStorageVersion(nsampleStorageVersionCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "containerlastnode": ["Label"], "containerpath": [ {
	 *                 "scontainerpath": "root > Label > Label > Label > Label" } ],
	 *                 "containerpathsize": 1, "containers": [],
	 *                 "propertyValidation": { "locationEnd": false, "storageStart":
	 *                 false, "storageEnd": true }, "sampleStorageLocation": {
	 *                 "nstatus": 1, "nstoragecategorycode": 3,
	 *                 "nsamplestoragelocationcode": 46 }, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 8,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46 },
	 *                 "selectedSampleStorageLocation": { "dmodifieddate":
	 *                 "2025-06-10T06:40:10.000+00:00", "ncolumn": 1,
	 *                 "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "ndirectionmastercode": 1, "nmappingtranscode": 8,
	 *                 "nneedautomapping": 4, "nneedposition": 4, "nnoofcontainer":
	 *                 1, "nproductcode": -1, "nprojecttypecode": -1, "nquantity":
	 *                 0, "nrow": 1, "nsamplestoragelocationcode": 46, "nsitecode":
	 *                 1, "nstatus": 1, "nstoragecategorycode": 3, "nunitcode": -1,
	 *                 "scontainerstructurename": "NA", "scontainertype": "NA",
	 *                 "sdirection": "Left to Right", "sneedautomapping": "No",
	 *                 "sproductname": "NA", "sprojecttypename": "NA",
	 *                 "ssamplestoragelocationname": "Ss", "sstoragecategoryname":
	 *                 "Deep Freezer (-80°C)", "stransdisplaystatus": "Draft",
	 *                 "sunitname": "NA" }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveSampleStorageVersion(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.approveSampleStorageVersion(sampleStorageLocation, sampleStorageVersion,
				inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSampleStorageVersionByID(final int nsampleStorageVersionCode,
			final int nsampleStorageLocationCode, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.getSampleStorageVersionByID(nsampleStorageVersionCode,
				nsampleStorageLocationCode, userInfo);
	}

	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input : {
	 *                 "sampleStorageLocation": { "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "nsamplestoragelocationcode": 46
	 *                 }, "nsamplestoragelocationcode": 46, "nstatus": 1,
	 *                 "nstoragecategorycode": 3, "sampleStorageVersion": {
	 *                 "nstatus": 1, "napprovalstatus": 31,
	 *                 "nsamplestorageversioncode": 48,
	 *                 "nsamplestoragelocationcode": 46, "jsondata": {
	 *                 "nprojecttypecode": -1, "nquantity": 0, "nunitcode": -1,
	 *                 "nproductcode": -1, "nneedautomapping": 4, "nneedposition":
	 *                 4, "ncontainerstructurecode": -1, "ncontainertypecode": -1,
	 *                 "nrow": 1, "ncolumn": 1, "ndirectionmastercode": 1,
	 *                 "nnoofcontainer": 1 } }, "userinfo": { "nusercode": -1,
	 *                 "nuserrole": -1, "ndeputyusercode": -1, "ndeputyuserrole":
	 *                 -1, "nmodulecode": 71, "nformcode": 168, "nmastersitecode":
	 *                 -1, "nsitecode": 1, "ntranssitecode": 1, "nusersitecode": -1,
	 *                 "ndeptcode": -1, "nreasoncode": 0, "nisstandaloneserver": 4,
	 *                 "nissyncserver": 3, "nlogintypecode": 1,
	 *                 "nsiteadditionalinfo": 4, "ntimezonecode": -1,
	 *                 "istimezoneshow": 4, "isutcenabled": 4, "slanguagefilename":
	 *                 "Msg_en_US", "slanguagetypecode": "en-US", "slanguagename":
	 *                 "English", "activelanguagelist": ["en-US"],
	 *                 "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sdeptname":
	 *                 "NA", "sdeputyid": "system", "sdeputyusername": "QuaLIS
	 *                 Admin", "sdeputyuserrolename": "QuaLIS Admin", "sfirstname":
	 *                 "QuaLIS", "slastname": "Admin", "sloginid": "system",
	 *                 "sformname": "Storage Structure", "smodulename": "Storage
	 *                 Management", "spredefinedreason": null, "sreason": "",
	 *                 "sreportingtoolfilename": "en.xml", "sreportlanguagecode":
	 *                 "en-US", "ssessionid": "8339746ACC3BA62170603EBECDA5E8BB",
	 *                 "ssitecode": "SYNC", "ssitename": "THSTI BRF", "stimezoneid":
	 *                 "Europe/London", "shostip": "0:0:0:0:0:0:0:1", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "spassword":
	 *                 null } }
	 *
	 *                 * @return response entity object holding response status and
	 *                 list of all StorageStructure
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> copySampleStorageVersion(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.copySampleStorageVersion(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> getProjectType(final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.getProjectType(userInfo);
	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US",nsamplestoragelocationcode:3,nsamplestorageversioncode:42},nsamplestorageversioncode:1}
	 * @return response entity object holding response status and list of all
	 *         StorageStructure
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> getProduct(final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.getProduct(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> addinfoSampleStorageStructure(final SampleStorageStructure sampleStorageLocation,
			final SampleStorageVersion sampleStorageVersion, final UserInfo userInfo) throws Exception {
		return sampleStorageStructureDAO.addinfoSampleStorageStructure(sampleStorageLocation, sampleStorageVersion,
				userInfo);
	}
	//ALPDJ21-32-comment by Vignesh(23-07-2025)-->There is no need to fetch the Site ID and Instrument ID.
	/*
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 
	
	@Override
	public ResponseEntity<Object> getInstrumentID(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return sampleStorageStructureDAO.getInstrumentID(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available storagestructure(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 inputMap:[{"userinfo":{nmastersitecode":
	 *                 -1},"nstoragecategorycode":1,"ninstrumentcode":1}]
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 
	@Override
	public ResponseEntity<Object> getsite(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return sampleStorageStructureDAO.getsite(inputMap,userInfo);
	}
	*/
}
