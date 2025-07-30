package com.agaramtech.qualis.quotation.service.quotation;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.Quotation;
import com.agaramtech.qualis.quotation.model.QuotationTest;
import com.agaramtech.qualis.quotation.model.QuotationTotalAmount;

/**
 * This interface declaration holds methods to perform CRUD operation on 'quotation' table
  
 */
public interface QuotationService {

	/**
	 * This interface declaration is used to get the over all quotations with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotation with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotation(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to quotation  table.
	 * @param objQuotation [Quotation] object holding details to be added in quotation table
	 * @return response entity object holding response status and data of added quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createQuotation(Quotation quotation, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in quotation table.
	 * @param objQuotation [Quotation] object holding detail to be deleted in quotation table
	 * @return response entity object holding response status and data of deleted quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteQuotation(Quotation quotation, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to approve entry in quotation table through its DAO layer.
	 * @param objQuotation [Quotation] object holding detail to be approved in quotation table
	 * @return response entity object holding response status and data of approved quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> approveQuotation(Quotation approveQuotation, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active quotation object based
	 * on the specified nquotationcode.
	 * @param nquotationcode [int] primary key of quotation object
	 * @return response entity  object holding response status and data of quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveQuotationById(final int nquotationcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in quotation table.
	 * @param objQuotation [Quotation] object holding details to be updated in quotation table
	 * @return response entity object holding response status and data of updated quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQuotation(Quotation quotation, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all quotationtype with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotationtype with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationType(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all quotationtest with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotationtest with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationUnmappedTest(Integer nquotationcode, UserInfo userInfo)  throws Exception;

	/**
	 * This interface declaration is used to add a new entry to quotationTest table.
	 * @param objQuotationTest [QuotationTest] object holding details to be added in quotationtest table
	 * @return response entity object holding response status and data of added quotationtest object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createQuotationTest(List<QuotationTest> quotationTest, UserInfo userInfo)  throws Exception;

	/**
	 * This interface declaration is used to get the over all quotationprice with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotationprice with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationPrice(int nquotationcode, Integer nquotationtestcode,
			UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in quotationtest table.
	 * @param objQuotationTest [QuotationTest] object holding details to be updated in quotationtest table
	 * @return response entity object holding response status and data of updated quotationtest object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateQuotationTest(final List<QuotationTest> quotationTestList, final int nquotationcode, 
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all quotationtest with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotationtest with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationTest(int nquotationcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all quotationgrossamount with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of quotationgrossamount with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationGrossAmount(int nquotationcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to quotationtotalamount table.
	 * @param objQuotationTotalAmount [QuotationTotalAmount] object holding details to be added in quotationtotalamount table
	 * @return response entity object holding response status and data of added quotationtotalamount object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createGrossQuotation(QuotationTotalAmount grossQuotation, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all grossquotation with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of grossquotation with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getGrossQuotation(int nquotationcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in quotationtest table.
	 * @param objQuotationPrice [QuotationPrice] object holding detail to be deleted in quotationtest table
	 * @return response entity object holding response status and data of deleted quotationtest object 
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteQuotationPrice(QuotationTest quotationtestPrice, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all product with respect to productcategory
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of product with respect to productcategory and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductByCategory(int nproductcatcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all clientcategory with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of clientcategory with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getClientCategory(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all productcategory with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of productcategory with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductCategory(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all client with respect to nclientCode
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of client with respect to nclientCode and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSelectedClientDetail(UserInfo userInfo, int nclientCode) throws Exception;
	
	/**
	 * This interface declaration is used to retire entry in quotation table through its DAO layer.
	 * @param Quotation [retireQuotation] object holding detail to be retired in quotation table
	 * @return response entity object holding response status and data of retired quotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> retireQuotation(Quotation retireQuotation, UserInfo userInfo)throws Exception;

	
}
