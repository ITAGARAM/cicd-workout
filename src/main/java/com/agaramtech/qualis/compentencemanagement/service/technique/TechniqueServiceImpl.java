package com.agaramtech.qualis.compentencemanagement.service.technique;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TechniqueTest;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TechniqueServiceImpl implements TechniqueService {
	/**
	 * This class holds methods to perform CRUD operation on 'unit' table through
	 * its DAO layer.
	 */

	private final TechniqueDAO techniqueDAO;

	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active Technique for the
	 * specified site.
	 * 
	 * @param userInfo object is used for fetched the list of active records based
	 *                 on site
	 * @return response entity object holding response status and list of all active
	 *         Technique
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getTechnique(final Integer ntechniquecode, final UserInfo userInfo) throws Exception {

		return techniqueDAO.getTechnique(ntechniquecode, userInfo);
	}

	/**
	 * This method is used to retrieve active Technique object based on the
	 * specified ntechniqueCode.
	 * 
	 * @param ntechniquecode [int] primary key of Technique object
	 * @param userInfo       object is used for fetched the list of active records
	 *                       based on site
	 * @return response entity object holding response status and data of Technique
	 *         object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTechniqueById(final int ntechniquecode, final UserInfo userInfo) throws Exception {

		final Technique technique = (Technique) techniqueDAO.getActiveTechniqueById(ntechniquecode);
		if (technique == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(technique, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to Technique table. On successive
	 * insert get the new inserted record along with default status from transaction
	 * status
	 * 
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @param objTechnique [Technique] object holding details to be added in
	 *                     Technique table
	 * @return inserted Technique object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> createTechnique(final Technique objTechnique, final UserInfo userInfo) throws Exception {
		return techniqueDAO.createTechnique(objTechnique, userInfo);
	}

	/**
	 * This method is used to update entry in Technique table.
	 * 
	 * @param objTechnique [Technique] object holding details to be updated in
	 *                     Technique table
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @return response entity object holding response status and data of updated
	 *         Technique object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateTechnique(final Technique objTechnique, final UserInfo userInfo) throws Exception {

		return techniqueDAO.updateTechnique(objTechnique, userInfo);
	}

	/**
	 * This method id used to delete an entry in Technique table
	 * 
	 * @param objTechnique [Technique] an Object holds the record to be deleted
	 * @param userInfo     object is used for fetched the list of active records
	 *                     based on site
	 * @return a response entity with corresponding HTTP status and an Technique
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteTechnique(final Technique objTechnique, final UserInfo userInfo) throws Exception {
		return techniqueDAO.deleteTechnique(objTechnique, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTechniqueTest(final int ntechniqueCode, final UserInfo userInfo) throws Exception {
		return techniqueDAO.getTechniqueTest(ntechniqueCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTechniqueConducted(final int ntechniqueCode, final UserInfo userInfo) throws Exception {
		return techniqueDAO.getTechniqueConducted(ntechniqueCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTechniqueScheduled(final int ntechniqueCode, final UserInfo userInfo) throws Exception {
		return techniqueDAO.getTechniqueScheduled(ntechniqueCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTechniqueTest(List<TechniqueTest> techniqueTestList, final int ntechniqueCode,
			UserInfo userInfo) throws Exception {
		return techniqueDAO.createTechniqueTest(techniqueTestList, ntechniqueCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTechniqueTest(final TechniqueTest techniqueTest, final int ntechniqueCode,
			UserInfo userInfo) throws Exception {
		return techniqueDAO.deleteTechniqueTest(techniqueTest, ntechniqueCode, userInfo);
	}
}
