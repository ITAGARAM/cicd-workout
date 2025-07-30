package com.agaramtech.qualis.basemaster.service.barcode;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'barcode' table through
 * its DAO layer.
 * 
 * @author ATE154
 * @version 9.0.0.1
 * @since 10- oct- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class BarcodeServiceImpl implements BarcodeService {

	private final BarcodeDAO objBarcodeDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active barcode for the specified
	 * site through its DAO layer
	 * 
	 * @param userInfo [nmasterSiteCode] primary key of site object for which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBarcode(final UserInfo userInfo) throws Exception {
		return objBarcodeDAO.getBarcode(userInfo);
	}

	/**
	 * This method is used to delete entry in barcode table through its DAO layer.
	 * 
	 * @param barcode  [barcode] object holding detail(primary key of selected
	 *                 record) to be deleted in barcode table
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 userInfo is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteBarcode(final Barcode barcode,final UserInfo userInfo) throws Exception {
		return objBarcodeDAO.deleteBarcode(barcode, userInfo);
	}

	/**
	 * This method is used to retrieve active barcode object based on the specified
	 * nbarcode through its DAO layer.
	 * 
	 * @param nbarcode [int] primary key of barcode object
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 userInfo is to be fetched
	 * @return response entity object holding response status and data of barcode
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveBarcodeById(final int nbarcode, final UserInfo userInfo) throws Exception {
		final Barcode barcode = (Barcode) objBarcodeDAO.getActiveBarcodeById(nbarcode,userInfo.getNmastersitecode());
		if (barcode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(barcode, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to barcode table through its DAO
	 * layer.
	 * 
	 * @param request     object holding details to be added in barcode table
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity object holding response status and data of added
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createBarcode(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return objBarcodeDAO.createBarcode(request, objUserInfo);
	}

	/**
	 * This method is used to retrieve list of all active records sqlquery and
	 * controlname for the specified site through its DAO layer
	 * 
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 userInfo is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         controlname
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getSqlQuery(final UserInfo userInfo) throws Exception {
		return objBarcodeDAO.getSqlQuery(userInfo);
	}

	/**
	 * This method definition is used to update the barcode
	 * 
	 * @param request  holds the details of barcode
	 * @param userInfo [UserInfo] holds the object of loggedin userinfo
	 * @return response entity holds the list of barcode and its parameter details
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> updateBarcode(final MultipartHttpServletRequest request,final UserInfo userInfo)
			throws Exception {

		return objBarcodeDAO.updateBarcode(request, userInfo);
	}

	/**
	 * This method declaration is view the file attached in barcode screen
	 * 
	 * @param objTestFile holds the file details to be viewed
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity holds the list of barcode and its parameter details
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedDownloadFile(final Barcode objTestFile,final UserInfo objUserInfo) throws Exception {
		return objBarcodeDAO.viewAttachedDownloadFile(objTestFile, objUserInfo);
	}

	
	@Override
	public ResponseEntity<Object> getPrinter() throws Exception {
		return objBarcodeDAO.getPrinter();
	}

	@Override
	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception {
		return objBarcodeDAO.PrintBarcode(inputMap);
	}

	@Override
	public ResponseEntity<Object> getControlBasedBarcode(final int ncontrolcode) throws Exception {
		return objBarcodeDAO.getControlBasedBarcode(ncontrolcode);
	}

}
