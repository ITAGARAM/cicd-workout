package com.agaramtech.qualis.quotation.service.quotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
//import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.Quotation;
import com.agaramtech.qualis.quotation.model.QuotationTest;
import com.agaramtech.qualis.quotation.model.QuotationTotalAmount;

/**
 * This class holds methods to perform CRUD operation on 'quotation' table through its DAO layer.
 
 */
@Service
@Transactional(readOnly = true, rollbackFor=Exception.class)
public class QuotationServiceImpl implements QuotationService {

	private final QuotationDAO quotationDAO;
	private final CommonFunction commonFunction;	
	
	public QuotationServiceImpl(QuotationDAO quotationDAO, CommonFunction commonFunction) {
		this.quotationDAO = quotationDAO;
		this.commonFunction = commonFunction;
	}
	/**
	 * This method is used to retrieve list of all active quotations for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotations
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQuotation(UserInfo userInfo) throws Exception {
		
		return quotationDAO.getQuotation(userInfo);
	}

	/**
	 * This method is used to add a new entry to quotation table.
	 * @param objQuotation [Quotation] object holding details to be added in quotation table
	 * @return inserted quotation object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createQuotation(Quotation Quotation, UserInfo userInfo) throws Exception {

		return quotationDAO.createQuotation(Quotation, userInfo);
	}

	/**
	 * This method is used to update entry in quotation  table.
	 * @param objQuotation [Quotation] object holding details to be updated in quotation table
	 * @return response entity object holding response status and data of updated quotation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQuotation(Quotation quotation, UserInfo userInfo) throws Exception {
		
		return quotationDAO.updateQuotation(quotation, userInfo);
	}
	
	/**
	 * This method is used to retrieve active quotation object based on the specified nquotationcode.
	 * @param nquotationcode [int] primary key of quotation object
	 * @return response entity  object holding response status and data of quotation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveQuotationById(int nquotationcode, UserInfo userInfo)
			throws Exception {


		final ResponseEntity<Object> objquotationno = quotationDAO.getActiveQuotationById(nquotationcode, userInfo);
		if(objquotationno == null) {
			final Map<String, Object> returnMap = new HashMap<>();
			returnMap.put("rtn", commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(returnMap,HttpStatus.EXPECTATION_FAILED);
			
		} else {
			return new ResponseEntity<>(objquotationno.getBody(), HttpStatus.OK);
		}
	}
	
	/**
	 * This method id used to delete an entry in quotation table
	 * @param objQuotation [Quotation] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an quotation object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteQuotation(Quotation quotation, UserInfo userInfo) throws Exception {
		
		return quotationDAO.deleteQuotation(quotation, userInfo);
	}

	/**
	 * This method is used to approve entry in quotation table through its DAO layer.
	 * @param Quotation [Quotation] object holding detail to be approved in quotation table
	 * @return response entity object holding response status and data of approved quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveQuotation(Quotation quotation, UserInfo userInfo) throws Exception {
		return quotationDAO.approveQuotation(quotation, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active quotationtype for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtype
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationType(UserInfo userInfo) throws Exception {
		
		return quotationDAO.getQuotationType(userInfo);
	}

	/**
	 * This method is used to retrieve list of all active quotationtest for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtest
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationUnmappedTest(Integer nquotationcode, UserInfo userInfo) throws Exception {
		
		return quotationDAO.getQuotationUnmappedTest(nquotationcode, userInfo);
	}

	/**
	 * This method is used to add a new entry to quotationtest table.
	 * @param objQuotationTest [QuotationTest] object holding details to be added in quotationtest table
	 * @return inserted quotationtest object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createQuotationTest(List<QuotationTest> quotationTest, UserInfo userInfo)
			throws Exception {
		
		return quotationDAO.createQuotationTest(quotationTest, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active quotationprice for the specified site.
	 * @param userInfo [UserInfo] primary key of nquotationtestcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationprice
	 * @throws Exception that are thrown from this Service layer
	 */
	
	@Override
	public ResponseEntity<Object> getQuotationPrice(int nquotationcode, Integer nquotationtestcode, UserInfo userInfo) throws Exception{
		
		return quotationDAO.getQuotationPrice(nquotationcode, nquotationtestcode, userInfo);
	}

	/**
	 * This method is used to update entry in quotationtest table.
	 * @param objQuotationTest [QuotationTest] object holding details to be updated in quotationtest table
	 * @return response entity object holding response status and data of updated quotationtest object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQuotationTest(final List<QuotationTest> QuotationTestList, final int nquotationcode, 
			UserInfo userInfo) throws Exception {
		
		return quotationDAO.updateQuotationTest(QuotationTestList, nquotationcode, userInfo);
		
	}

	/**
	 * This method is used to retrieve list of all active quotationtest for the specified site.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtest
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationTest(int nquotationcode, UserInfo userInfo)
			throws Exception {

		return quotationDAO.getQuotationTest(nquotationcode, userInfo);
	}
	
	/**
	 * This method is used to retrieve list of all active quotationgrossamount for the specified site.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationgrossamount
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationGrossAmount(int nquotationcode, UserInfo userInfo)
			throws Exception {

		return quotationDAO.getQuotationGrossAmount(nquotationcode, userInfo);
	}

	/**
	 * This method is used to add a new entry to quotationtotalamount table.
	 * @param objQuotationTotalAmount [QuotationTotalAmount] object holding details to be added in quotationtotalamount table
	 * @return inserted quotationtotalamount object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createGrossQuotation(QuotationTotalAmount GrossQuotation, UserInfo userInfo) throws Exception {

		return quotationDAO.createGrossQuotation(GrossQuotation, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active grossquotation for the specified site.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active grossquotation
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getGrossQuotation(int nquotationcode, UserInfo userInfo) throws Exception {
		
		return quotationDAO.getGrossQuotation(nquotationcode, userInfo);
	}

	/**
	 * This method id used to delete an entry in quotationtest table
	 * @param objQuotationPrice [QuotationPrice] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an quotationprice object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteQuotationPrice(QuotationTest quotationtestPrice, UserInfo userInfo) throws Exception {
		
		return quotationDAO.deleteQuotationPrice(quotationtestPrice, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active product for the specified productcategory.
	 * @param userInfo [UserInfo] primary key of nproductcatcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active product
	 * @throws Exception that are thrown from this Service layer
	 */	
	@Override
	public ResponseEntity<Object> getProductByCategory(int nproductcatcode, UserInfo userInfo) throws Exception {
		
		return quotationDAO.getProductByCategory(nproductcatcode, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active clientcategory for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active clientcategory
	 * @throws Exception that are thrown from this Service layer
	 */	
	@Override
	public ResponseEntity<Object> getClientCategory(UserInfo userInfo) throws Exception {
		
		return quotationDAO.getClientCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of all active productcategory for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active productcategory
	 * @throws Exception that are thrown from this Service layer
	 */	
	@Override
	public ResponseEntity<Object> getProductCategory(UserInfo userInfo) throws Exception {
		
		return quotationDAO.getProductCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of all active clientdetails for the specified site.
	 * @param userInfo [UserInfo] primary key of nclientCode object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active clientdetails
	 * @throws Exception that are thrown from this Service layer
	 */	
	@Override
	public ResponseEntity<Object> getSelectedClientDetail(UserInfo userInfo, int nclientCode) throws Exception {
		
		return quotationDAO.getSelectedClientDetail(userInfo, nclientCode);

	}
	
	/**
	 * This method is used to retire entry in quotation table through its DAO layer.
	 * @param Quotation [retireQuotation] object holding detail to be retired in quotation table
	 * @return response entity object holding response status and data of retired quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> retireQuotation(Quotation retireQuotation, UserInfo userInfo)throws Exception {
		return quotationDAO.retireQuotation(retireQuotation, userInfo);
	}
	
}
