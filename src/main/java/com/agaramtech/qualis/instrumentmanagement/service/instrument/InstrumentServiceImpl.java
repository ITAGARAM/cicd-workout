package com.agaramtech.qualis.instrumentmanagement.service.instrument;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCalibration;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentFile;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentMaintenance;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentSection;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentValidation;

/**
 * This class holds methods to perform CRUD operation on 'instrument and
 * instrument related' table through its DAO layer.
 * 
 * @author ATE154
 * @version 9.0.0.1
 * @since 03- nov- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InstrumentServiceImpl implements InstrumentService {

	private final InstrumentDAO objInstrumentDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param objInstrumentDAO InstrumentDAO Interface
	 * @param commonFunction   CommonFunction holding common utility functions
	 */

	public InstrumentServiceImpl(InstrumentDAO objInstrumentDAO, CommonFunction commonFunction) {
		this.objInstrumentDAO = objInstrumentDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active instrument for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         instrument
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getInstrument(final Integer ninstCode, final UserInfo userInfo) throws Exception {
		return objInstrumentDAO.getInstrument(ninstCode, userInfo);
	}

	public ResponseEntity<Object> validateOpenDate(final Integer ninstCode, final UserInfo userInfo) throws Exception {
		return objInstrumentDAO.validateOpenDate(ninstCode, userInfo);
	}

	/**
	 * This method is used to delete entry in instrument table through its DAO
	 * layer.
	 * 
	 * @param barcode [barcode] object holding detail to be deleted in instrument
	 *                table
	 * @return response entity object holding response status and data of deleted
	 *         instrument object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> deleteInstrument(final Instrument inst, final UserInfo userInfo) throws Exception {

		return objInstrumentDAO.deleteInstrument(inst, userInfo);
	}

	/**
	 * This method is used to add a new entry to materialcategory table through its
	 * DAO layer.
	 * 
	 * @param materialcategory [MaterialCategory] object holding details to be added
	 *                         in materialcategory table
	 * @return response entity object holding response status and data of added
	 *         materialcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createInstrument(final Map<String, Object> inputMap, final UserInfo userInfo,
			final List<InstrumentSection> instSect) throws Exception {

		Map<String, Object> objmap = objInstrumentDAO.validateAndInsertSeqNoForInstrument(inputMap, userInfo, instSect);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			objmap.putAll(objmap);
			return objInstrumentDAO.createInstrument(inputMap, userInfo, instSect);
		} else {
			return new ResponseEntity<Object>(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in materialcategory table through its DAO
	 * layer.
	 * 
	 * @param materialcategory [MaterialCategory] object holding details to be
	 *                         updated in materialcategory table
	 * @return response entity object holding response status and data of updated
	 *         materialcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrument(final Instrument inst, final UserInfo userInfo,
			final InstrumentSection instSect) throws Exception {

		return objInstrumentDAO.updateInstrument(inst, userInfo, instSect);
	}

	/**
	 * This method is used to get transaction status for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */
	public ResponseEntity<Object> getInstrumentStatus(final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentStatus(userinfo);
	}

	/**
	 * This method is used to get period for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */
//	public ResponseEntity<Object> getPeriod(Integer ncontrolCode, final UserInfo userinfo) throws Exception {
//		return objInstrumentDAO.getPeriod(ncontrolCode, userinfo);
//	}

	/**
	 * This method is used to get active record for instrument.
	 * 
	 * @param mapObject [Map] object with keys of instrument entity.
	 * @return response entity of active instrument entity
	 */

	@Override
	public ResponseEntity<Object> getActiveInstrumentById(final int ninstCode, final UserInfo userInfo)
			throws Exception {
		final Instrument inst = (Instrument) objInstrumentDAO.getActiveInstrumentById(ninstCode, userInfo);

		if (inst == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (inst.getNinstrumentstatus() != Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {
				return new ResponseEntity<>(inst, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DISPOSEDINSTRUMENT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getSectionBasedUser(final int sectionCode, final int nregionalsitecode,
			final UserInfo userInfo) throws Exception {
		return objInstrumentDAO.getSectionBasedUser(sectionCode, nregionalsitecode, userInfo);

	}

	public ResponseEntity<Object> getUsers(final Map<String, Object> inputMap, final UserInfo userinfo)
			throws Exception {
		return objInstrumentDAO.getUsers(inputMap, userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSection(final InstrumentSection inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.createSection(inst, userInfo);

	}

	@Transactional
	public ResponseEntity<Object> deleteSection(final InstrumentSection instSec, final UserInfo userInfo)
			throws Exception {

		return objInstrumentDAO.deleteSection(instSec, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> setDefaultSection(final InstrumentSection objSection, final UserInfo objUserInfo)
			throws Exception {
		return objInstrumentDAO.setDefaultSection(objSection, objUserInfo);
	}

	public ResponseEntity<Object> getManufacturer(final Integer ncontrolCode, final UserInfo userinfo)
			throws Exception {
		return objInstrumentDAO.getManufacturer(ncontrolCode, userinfo);
	}

	@Override
	public ResponseEntity<Object> getInsByInstrumentCat(final Integer ninstrumentcatcode, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.getInsByInstrumentCat(ninstrumentcatcode, userInfo);
	}

	public ResponseEntity<Object> getSupplier(final Integer ncontrolCode, final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getSupplier(ncontrolCode, userinfo);
	}

	@Override
	public ResponseEntity<Object> addInstrmentDate(final Integer ncontrolCode, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.addInstrmentDate(ncontrolCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getOtherTabDetails(final int nFlag, final int ninstrumentcode,
			final int tabprimarycode, final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.getOtherTabDetails(nFlag, ninstrumentcode, tabprimarycode, objUserInfo);
	}

	public ResponseEntity<Object> getInstrumentValidationStatus(final Map<String, Object> inputMap,
			final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentValidationStatus(inputMap, userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentValidation(final InstrumentValidation inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.createInstrumentValidation(inst, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveInstrumentValidationById(final int insvalcode, final UserInfo userInfo)
			throws Exception {
		final InstrumentValidation instval = (InstrumentValidation) objInstrumentDAO
				.getActiveInstrumentValidationById(insvalcode, userInfo);
		if (instval == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			return new ResponseEntity<>(instval, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentValidation(final InstrumentValidation inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.updateInstrumentValidation(inst, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentValidation(final InstrumentValidation inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.deleteInstrumentValidation(inst, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.createInstrumentFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editInstrumentFile(final InstrumentFile objInstrumentFile, final UserInfo objUserInfo)
			throws Exception {
		return objInstrumentDAO.editInstrumentFile(objInstrumentFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.updateInstrumentFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentValidationFile(final InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.deleteInstrumentValidationFile(objInstrumentFile, objUserInfo);
	}

	public ResponseEntity<Object> getInstrumentCalibrationStatus(final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentCalibrationStatus(userinfo);
	}

	public ResponseEntity<Object> getInstrumentLastCalibrationDate(final UserInfo userinfo,
			Map<String, Object> inputMap) throws Exception {
		return objInstrumentDAO.getInstrumentLastCalibrationDate(userinfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentCalibration(final InstrumentCalibration inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.createInstrumentCalibration(inst, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveInstrumentCalibrationById(final int insvalcode, final UserInfo userInfo)
			throws Exception {
		final InstrumentCalibration instval = (InstrumentCalibration) objInstrumentDAO
				.getActiveInstrumentCalibrationById(insvalcode, userInfo);
		if (instval == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(instval, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentCalibration(final InstrumentCalibration inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.updateInstrumentCalibration(inst, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentCalibration(final Map<String, Object> inputMap,
			final InstrumentCalibration inst, final UserInfo userInfo) throws Exception {
		return objInstrumentDAO.deleteInstrumentCalibration(inputMap, inst, userInfo);
	}

	public ResponseEntity<Object> getInstrumentCalibrationOpenDateStatus(final UserInfo userinfo,
			final Map<String, Object> inputMap) throws Exception {
		return objInstrumentDAO.getInstrumentCalibrationOpenDateStatus(userinfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentCalibrationFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.createInstrumentCalibrationFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentCalibrationFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.updateInstrumentCalibrationFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentCalibrationFile(final InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.deleteInstrumentCalibrationFile(objInstrumentFile, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentCalibrationValidation(final UserInfo userinfo, final int nFlag,
			final int ninstrumentcode, final int ninstrumentcalibrationcode) throws Exception {
		return objInstrumentDAO.getInstrumentCalibrationValidation(userinfo, nFlag, ninstrumentcode,
				ninstrumentcalibrationcode);
	}

	public ResponseEntity<Object> getInstrumentMaintenanceStatus(final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentMaintenanceStatus(userinfo);
	}

	public ResponseEntity<Object> getInstrumentLastMaintenanceDate(final UserInfo userinfo,
			final Map<String, Object> inputMap) throws Exception {
		return objInstrumentDAO.getInstrumentLastMaintenanceDate(userinfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentMaintenance(final InstrumentMaintenance inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.createInstrumentMaintenance(inst, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveInstrumentMaintenanceById(final int insvalcode, final UserInfo userInfo)
			throws Exception {
		final InstrumentMaintenance instval = (InstrumentMaintenance) objInstrumentDAO
				.getActiveInstrumentMaintenanceById(insvalcode, userInfo);
		if (instval == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(instval, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentMaintenance(final InstrumentMaintenance inst, final UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.updateInstrumentMaintenance(inst, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentMaintenance(final InstrumentMaintenance inst, UserInfo userInfo)
			throws Exception {
		return objInstrumentDAO.deleteInstrumentMaintenance(inst, userInfo);
	}

	public ResponseEntity<Object> getInstrumentMaintenanceOpenCloseDateStatus(final UserInfo userinfo,
			Map<String, Object> inputMap) throws Exception {
		return objInstrumentDAO.getInstrumentMaintenanceOpenCloseDateStatus(userinfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentMaintenanceFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.createInstrumentMaintenanceFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentMaintenanceFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.updateInstrumentMaintenanceFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentMaintenanceFile(final InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.deleteInstrumentMaintenanceFile(objInstrumentFile, objUserInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewAttachedInstrumentFile(final InstrumentFile objInstrumentFile,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.viewAttachedInstrumentFile(objInstrumentFile, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentMaintenanceValidation(final UserInfo userinfo, final int nFlag,
			final int ninstrumentcode, final int ninstrumentmaintenancecode) throws Exception {
		return objInstrumentDAO.getInstrumentMaintenanceValidation(userinfo, nFlag, ninstrumentcode,
				ninstrumentmaintenancecode);
	}

	public ResponseEntity<Object> getCalibrationRequired(final Integer ninstrumentCatCode, final UserInfo userinfo)
			throws Exception {
		return objInstrumentDAO.getCalibrationRequired(ninstrumentCatCode, userinfo);
	}

	public ResponseEntity<Object> getInstrumentName(final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentName(userinfo);
	}

	public ResponseEntity<Object> getInstrumentLocation(final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getInstrumentLocation(userinfo);
	}

	public ResponseEntity<Object> getSiteBasedSection(final int nsitecode, final UserInfo userinfo) throws Exception {
		return objInstrumentDAO.getSiteBasedSection(nsitecode, userinfo);
	}

	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	public ResponseEntity<Object> getInstrumentBySchedulerDetail(final int instrumentCode, final UserInfo objUserInfo)
			throws Exception {
		return objInstrumentDAO.getInstrumentBySchedulerDetail(instrumentCode, objUserInfo);

	}

	@Transactional
	// Added by sonia on 30th Sept 2024 for Jira idL:ALPD-4940
	public ResponseEntity<Object> updateAutoCalibrationInstrument(final int instrumentCode, final int autoCalibration,
			final UserInfo objUserInfo) throws Exception {
		return objInstrumentDAO.updateAutoCalibrationInstrument(instrumentCode, autoCalibration, objUserInfo);

	}

	// ALPD-5330 - Gowtham R - Section not loaded initially - 08-02-2025
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception {
		return objInstrumentDAO.getSection(userInfo);
	}

}
