package com.agaramtech.qualis.instrumentmanagement.service.instrument;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCalibration;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentFile;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentMaintenance;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentSection;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentValidation;

/**
 * This interface holds declarations to perform CRUD operation on 'instrument
 * and instrument' table through its implementation class.
 * 
 * @author ATE154
 * @version 9.0.0.1
 * @since 03- nov- 2020
 */
public interface InstrumentDAO {

	/**
	 * This interface declaration is used to retrieve list of all active instrument
	 * for the specified site.
	 * 
	 * @param nbarcode [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         instrument
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getInstrument(final Integer ninstCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> validateOpenDate(final Integer ninstCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in instrument table.
	 * 
	 * @param materialcategory [Instrument] object holding detail to be deleted in
	 *                         instrument table
	 * @return response entity object holding response status and data of deleted
	 *         instrument object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInstrument(final Instrument inst,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active instrument object based
	 * on the specified ninstrumentcode.
	 * 
	 * @param nmaterialcatcode [int] primary key of instrument object
	 * @return response entity object holding response status and data of instrument
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public Instrument getActiveInstrumentById(final int ninstrumentcode,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to instrument table.
	 * 
	 * @param materialcategory [Instrument] object holding details to be added in
	 *                         instrument table
	 * @return response entity object holding response status and data of added inst
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInstrument(final Map<String, Object> inputMap,final UserInfo userInfo,final List<InstrumentSection> instSect)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in instrument table.
	 * 
	 * @param instrument [Instrument] object holding details to be updated in
	 *                   instrument table
	 * @return response entity object holding response status and data of updated
	 *         inst object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInstrument(final Instrument inst,final UserInfo userInfo,final InstrumentSection instSect)
			throws Exception;

	/**
	 * This method is used to get transaction status for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */

	public ResponseEntity<Object> getInstrumentStatus(final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to get period for instrument.
	 * 
	 * @param ncontrolCode
	 * @param mapObject    [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */

	//public ResponseEntity<Object> getPeriod(Integer ncontrolCode, final UserInfo userInfo) throws Exception;

	public Map<String, Object> validateAndInsertSeqNoForInstrument(final Map<String, Object> inputMap,final UserInfo userInfo,final List<InstrumentSection> instSect) throws Exception;

	public ResponseEntity<Object> getSectionBasedUser(final int sectionCode,final int nregionalsitecode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsers(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSection(final InstrumentSection inst,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSection(final InstrumentSection instSec,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> setDefaultSection(final InstrumentSection objSection, final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getManufacturer(final Integer ncontrolCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInsByInstrumentCat(final Integer ninstrumentcatcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSupplier(final Integer ncontrolCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> addInstrmentDate(final Integer ncontrolCode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentValidationStatus(final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> createInstrumentValidation(final InstrumentValidation inst,final UserInfo userInfo) throws Exception;
	
	public InstrumentValidation getActiveInstrumentValidationById(final int insvalcode,final UserInfo userInfo)  throws Exception ;

	public ResponseEntity<Object> updateInstrumentValidation(final InstrumentValidation inst,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteInstrumentValidation(final InstrumentValidation inst,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getOtherTabDetails(final int nFlag,final int ninstrumentcode,final int tabprimarycode,final UserInfo objUserInfo)throws Exception;

	public ResponseEntity<Object> createInstrumentFile(final MultipartHttpServletRequest request,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> editInstrumentFile(final InstrumentFile objInstrumentFile,final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> updateInstrumentFile(final MultipartHttpServletRequest request,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> deleteInstrumentValidationFile(final InstrumentFile objInstrumentFile,final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getInstrumentCalibrationStatus(final UserInfo userinfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentLastCalibrationDate(final UserInfo userinfo,final  Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createInstrumentCalibration(final InstrumentCalibration inst,final UserInfo userInfo) throws Exception;
	
	public InstrumentCalibration getActiveInstrumentCalibrationById(final int inst,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateInstrumentCalibration(final InstrumentCalibration inst,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteInstrumentCalibration(final Map<String, Object> inputMap,final InstrumentCalibration inst,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentCalibrationOpenDateStatus(final UserInfo userinfo,final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createInstrumentCalibrationFile(final MultipartHttpServletRequest request,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> updateInstrumentCalibrationFile(final MultipartHttpServletRequest request,final  UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> deleteInstrumentCalibrationFile(final InstrumentFile objInstrumentFile,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentCalibrationValidation(final UserInfo userinfo,final int nFlag,final int ninstrumentcode,final int ninstrumentcalibrationcode) throws Exception;

	public ResponseEntity<Object> getInstrumentMaintenanceStatus(final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentLastMaintenanceDate(final UserInfo userinfo, final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createInstrumentMaintenance(final InstrumentMaintenance inst, final UserInfo userInfo) throws Exception;
	
	public InstrumentMaintenance getActiveInstrumentMaintenanceById(final int insvalcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateInstrumentMaintenance(final InstrumentMaintenance inst,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteInstrumentMaintenance(final InstrumentMaintenance objInstVal,final UserInfo userInfo)throws Exception;
	
	public ResponseEntity<Object> getInstrumentMaintenanceOpenCloseDateStatus(final UserInfo userinfo,final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createInstrumentMaintenanceFile(final MultipartHttpServletRequest request,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> updateInstrumentMaintenanceFile(final MultipartHttpServletRequest request,final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> deleteInstrumentMaintenanceFile(final InstrumentFile objInstrumentFile,final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getInstrumentMaintenanceValidation(final UserInfo userinfo,final int nFlag,final int ninstrumentcode,final int ninstrumentmaintenancecode) throws Exception;

	public Map<String, Object> viewAttachedInstrumentFile(final InstrumentFile objInstrumentFile,final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getCalibrationRequired(final Integer ninstrumentCatCode, final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getInstrumentName( final UserInfo userinfo) throws Exception;
	
	public ResponseEntity<Object> getInstrumentLocation( final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getSiteBasedSection(final int nsitecode,final UserInfo userinfo) throws Exception;
	
	//Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	public ResponseEntity<Object> getInstrumentBySchedulerDetail(final int instrumentCode,final UserInfo objUserInfo)throws Exception;
	
	//Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	public ResponseEntity<Object> updateAutoCalibrationInstrument(final int instrumentCode,final int autoCalibration,final UserInfo objUserInfo)throws Exception;
	
	// ALPD-5330 - Gowtham R - Section not loaded initially - 08-02-2025
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception;

}
