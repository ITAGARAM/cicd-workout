package com.agaramtech.qualis.basemaster.service.barcode;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'Barcode' table through its DAO layer.
 * 
 * @author ATE154
 * @version 9.0.0.1
 * @since 12- oct- 2020
 */
public interface BarcodeService {

	/**
	 * This interface declaration is used to retrieve list of all active barcode for
	 * the specified site through its DAO layer
	 * 
	 * @param userinfo [object] nmastersitecode primary key of site object from
	 *                 objUserInfo is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBarcode(final UserInfo userinfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in barcode table through
	 * its DAO layer.
	 * 
	 * @param barcode  [barcode] object holding detail to be deleted in barcode
	 *                 table
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 objUserInfo is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteBarcode(final Barcode barcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active barcode object based on
	 * the specified nbarcode through its DAO layer.
	 * 
	 * @param nbarcode [int] primary key of barcode object
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 objUserInfo is to be fetched
	 * @return response entity object holding response status and data of barcode
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveBarcodeById(final int nbarcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to barcode table
	 * through its DAO layer.
	 * 
	 * @param request     [request] object holding details to be added in barcoded
	 *                    table
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity object holding response status and data of added
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createBarcode(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active sqlquery
	 * and controlname for the specified site through its DAO layer
	 * 
	 * @param userinfo [object] nmastersitecode primary key of site object from
	 *                 objUserInfo is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         sqlquery
	 * @throws Exception that are thrown in the DAO layer
	 * 
	 */
	public ResponseEntity<Object> getSqlQuery(final UserInfo userinfo) throws Exception;

	/**
	 * This method declaration is used to update the barcode
	 * 
	 * @param request     [object] holds the details of barcode
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity holds the list of barcode and its parameter details
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateBarcode(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * This method declaration is view the file attached in barcode screen
	 * 
	 * @param objTestFile holds the file details to be viewed
	 * @param objUserInfo [object] nmastersitecode primary key of site object from
	 *                    objUserInfo is to be fetched
	 * @return response entity holds the list of barcode and its parameter details
	 * @throws Exception that are thrown from this DAO layer
	 */

	public ResponseEntity<Object> viewAttachedDownloadFile(final Barcode objTestFile, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getPrinter() throws Exception;

	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception;

	ResponseEntity<Object> getControlBasedBarcode(final int ncontrolcode) throws Exception;

}
