package com.agaramtech.qualis.basemaster.service.barcode;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'Barcode' table through its implementation class.
 * 
 */
public interface BarcodeDAO {
	/**
	 * This interface declaration is used to retrieve list of all active barcode for
	 * the specified site.
	 * 
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 userInfo is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         barcode
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getBarcode(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in barcode table.
	 * 
	 * @param barcode  [Barcode] object holding detail to be deleted in barcode
	 *                 table
	 * @param userInfo [object] nmastersitecode primary key of site object from
	 *                 userInfo is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteBarcode(final Barcode barcode,final  UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active barcode based on the
	 * specified nbarcode.
	 * 
	 * @param nbarcode [int] primary key of barcode
	 * @return response entity object holding response status and data of barcode
	 * @throws Exception that are thrown in the DAO layer
	 */

	public Barcode getActiveBarcodeById(final int nbarcode,final int nmasterSiteCode) throws Exception;

	/**
	 * This method is used to create new file/ link
	 * 
	 * @param request     holds the list of test file details, file count,
	 *                    uploadedfile and its uniquevalue
	 * @param objUserInfo [UserInfo] holds the loggedin userinformation
	 * @return response entity of 'barcode attachment' entity
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> createBarcode(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active sqlquery
	 * and controlname for the specified site.
	 * 
	 * @param userInfo [UserInfo] holds the loggedin userinformation
	 * @return response entity object holding response status and list of all active
	 *         sqlquery
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getSqlQuery(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in barcode table.
	 * 
	 * @param request  [Barcode] object holding details to be updated in barcode
	 *                 table
	 * @param userInfo [UserInfo] holds the loggedin userinformation
	 * @return response entity object holding response status and data of updated
	 *         barcode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateBarcode(final MultipartHttpServletRequest request,final  UserInfo userInfo)
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

	public ResponseEntity<Object> getPrinter();

	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception;

	ResponseEntity<Object> getControlBasedBarcode(final int ncontrolcode) throws Exception;

}
