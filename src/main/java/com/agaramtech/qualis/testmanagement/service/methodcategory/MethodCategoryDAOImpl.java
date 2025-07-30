package com.agaramtech.qualis.testmanagement.service.methodcategory;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.testmanagement.model.MethodCategory;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "methodcategory" table by 
 * implementing methods from its interface. 
 */

@AllArgsConstructor
@Repository

public class MethodCategoryDAOImpl implements MethodCategoryDAO {
	
	 private static final Logger LOGGER = LoggerFactory.getLogger(MethodCategoryDAOImpl.class);
	  
	  private final StringUtilityFunction stringUtilityFunction; 
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final ProjectDAOSupport projectDAOSupport; 
	  private final DateTimeUtilityFunction dateUtilityFunction; 
	  private final AuditUtilityFunction auditUtilityFunction;
	  
	  
	  /**
		 * This method is used to retrieve list of all available method  for the specified site.
		 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
		 * 							  which the list is to be fetched
		 * @return response entity  object holding response status and list of all active methodcategories
		 * @throws Exception that are thrown from this DAO layer
		 */
	  
	  
	  @Override
	  public ResponseEntity<Object> getMethodCategory(final int nmasterSiteCode) throws Exception {
	  	final String strQuery = "select * from methodcategory m where m.nstatus = "
	  						  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	  						  + " and m.nsitecode = " + nmasterSiteCode
	  						  + " and m.nmethodcatcode > 0";
	  	LOGGER.info("Get Method:"+ strQuery);
	  	return new ResponseEntity<>((List<MethodCategory>)jdbcTemplate.query(strQuery, new MethodCategory()),HttpStatus.OK);
	  }
	  
	  /**
		 * This method is used to retrieve active objMethodCategory object based on the specified nmethodCatCode.
		 * @param nmethodCatCode [int] primary key of objMethodCategory object
		 * @param userInfo [UserInfo] holding logged in user details based on 
		 * which the list is to be fetched
		 * @return response entity  object holding response status and data of objMethodCategory object
		 * @throws Exception that are thrown from this DAO layer
		 */	
	  
	  @Override
	  public MethodCategory getActiveMethodCategoryById(int nmethodCatCode) throws Exception {
	  	final String strQuery = "select * from methodcategory m where m.nstatus = "
	  				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nmethodcatcode = "+ nmethodCatCode;
	  	MethodCategory objMethodCategory = (MethodCategory) jdbcUtilityFunction.queryForObject(strQuery,
	  			MethodCategory.class, jdbcTemplate);
	  	return objMethodCategory;
	  }

	  /**
		 * This method is used to add a new entry to methodcategory table.
		 * MethodCategory Name is unique across the database. 
		 * Need to check for duplicate entry of MethodCategory name for the specified site before saving into database. 
		 * @param methodCategory [MethodCategory] object holding details to be added in methodcategory table
		 * @param userInfo [UserInfo] holding logged in user details based on 
		 * which the list is to be fetched
		 * @return saved methodCategory object with status code 200 if saved successfully else if the methodcategory already exists, 
		 * response will be returned as 'Already Exists' with status code 409
		 * @throws Exception that are thrown from this DAO layer
		 */
	  
	  @Override
	  public ResponseEntity<Object> createMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception {
	  	
	  	final String sQuery = " lock  table methodcategory "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
	  	jdbcTemplate.execute(sQuery);
	  	
	  	final List<String> multilingualIDList = new ArrayList<>();
	  	final List<Object> savedMethodCategoryList = new ArrayList<>();
	  	final MethodCategory methodCategoryByName = getMethodCategoryByName(methodCategory.getSmethodcatname(), methodCategory.getNsitecode());
	  	if (methodCategoryByName == null) {
	  		String sequencenoquery ="select nsequenceno from SeqNoTestManagement where stablename ='methodcategory'";
	  		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	  		nsequenceno++;
	  		String insertquery ="Insert into methodcategory (nmethodcatcode,smethodcatname,sdescription,dmodifieddate,nsitecode,nstatus) "
	  						   +"values("+nsequenceno+",N'"+stringUtilityFunction.replaceQuote(methodCategory.getSmethodcatname())+"',N'"+stringUtilityFunction.replaceQuote(methodCategory.getSdescription())+"','"
	  						   + dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
	  		jdbcTemplate.execute(insertquery);
	  		
	  		String updatequery ="update SeqNoTestManagement set nsequenceno ="+nsequenceno+" where stablename='methodcategory'";
	  		jdbcTemplate.execute(updatequery);
	  		methodCategory.setNmethodcatcode(nsequenceno);
	  		savedMethodCategoryList.add(methodCategory);
	  		multilingualIDList.add("IDS_ADDTESTMANAGEMENTMETHODCATEGORY");
	  		auditUtilityFunction.fnInsertAuditAction(savedMethodCategoryList, 1, null, multilingualIDList, userInfo);
	  		return getMethodCategory(methodCategory.getNsitecode());
	  	}else {
	  		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.CONFLICT);
	  	  }
	  }
	  
	  /**
		 * This method is used to fetch the methodcategory object for the specified methodcategory name and site.
		 * @param smethodcatname [String] name of the methodcategory
		 * @param nmasterSiteCode [int] site code of the methodcategory
		 * @return methodcategory object based on the specified methodcategory name and site
		 * @throws Exception that are thrown from this DAO layer
		 */
	  private MethodCategory getMethodCategoryByName(final String methodcategory, final int nmasterSiteCode)throws Exception {
			final String strQuery = "select nmethodcatcode from methodcategory where smethodcatname = N'"
					+ stringUtilityFunction.replaceQuote(methodcategory) + "' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (MethodCategory) jdbcUtilityFunction.queryForObject(strQuery,MethodCategory.class,jdbcTemplate); 
	}
	  
	 
	  /**
		 * This method is used to update entry in methodcategory  table.
		 * Need to validate that the methodCategory object to be updated is active before updating 
		 * details in database.
		 * Need to check for duplicate entry of methodcategory name for the specified site before saving into database.
		 * @param objUnit [MethodCategory] object holding details to be updated in methodcategory table
		 * @param userInfo [UserInfo] holding logged in user details based on 
		 * which the list is to be fetched
		 * @return saved methodCategory object with status code 200 if saved successfully 
		 * else if the methodcategory already exists, response will be returned as 'Already Exists' with status code 409
		 * else if the methodcategory to be updated is not available, response will be returned as 'Already Deleted' with status code 417
		 * @throws Exception that are thrown from this DAO layer
		 */
	  
	  @Override
	  public ResponseEntity<Object> updateMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception {
	  	final MethodCategory methodcategory = getActiveMethodCategoryById(methodCategory.getNmethodcatcode());
	  	if (methodcategory == null) {
	  		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
	  	} else {
	  		final String queryString = "select nmethodcatcode from methodcategory where smethodcatname = '"
	  								 + stringUtilityFunction.replaceQuote(methodCategory.getSmethodcatname()) + "' and nmethodcatcode <> "
	  								 + methodCategory.getNmethodcatcode() + " and  nstatus = "
	  								 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	  		final List<MethodCategory> methodcategoryList = jdbcTemplate.query(queryString, new MethodCategory());
	  		
		  	//final MethodCategory methodCategoryByName = getMethodCategoryByName(methodCategory.getSmethodcatname(), methodCategory.getNsitecode());

	  		if (methodcategoryList .isEmpty()) {
	  			
	  				final String updateQueryString = "update methodcategory set smethodcatname='"+ stringUtilityFunction.replaceQuote(methodCategory.getSmethodcatname()) + "', "
	  					+ " sdescription = '"+ stringUtilityFunction.replaceQuote(methodCategory.getSdescription()) + "', dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'"
	  					+ " where nmethodcatcode="+ methodCategory.getNmethodcatcode();
	  				jdbcTemplate.execute(updateQueryString);
	  				
	  				final List<String> multilingualIDList = new ArrayList<>();
	  				multilingualIDList.add("IDS_EDITTESTMANAGEMENTMETHODCATEGORY");
	  				final List<Object> listAfterSave = new ArrayList<>();
	  				listAfterSave.add(methodCategory);
	  				final List<Object> listBeforeSave = new ArrayList<>();
	  				listBeforeSave.add(methodcategory);
	  				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
	  				return getMethodCategory(methodCategory.getNsitecode());

	  		} else {
	  			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),	HttpStatus.CONFLICT);
	  		}
	  	}
	  }
	  
	  
	  /**
		 * This method id used to delete an entry in methodcategory table
		 * Need to check the record is already deleted or not
		 * Need to check whether the record is used in other tables 
		 * @param methodCategory [MethodCategory] an Object holds the record to be deleted
		 * @param userInfo [UserInfo] holding logged in user details based on 
		 *  which the list is to be fetched
		 * @return a response entity with list of available methodCategory objects
		 * @exception Exception that are thrown from this DAO layer
		 */
	  
	  @Override
	  public ResponseEntity<Object> deleteMethodCategory(MethodCategory methodCategory, UserInfo userInfo)	throws Exception {
	  	final List<String> multilingualIDList = new ArrayList<>();
	  	final List<Object> savedMethodCategoryList = new ArrayList<>();
	  	final MethodCategory methodcategory = getActiveMethodCategoryById(methodCategory.getNmethodcatcode());
	  	if (methodcategory == null) {
	  		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
	  	} else {
	  		final String query = "select 'IDS_METHOD' as Msg from method where nmethodcatcode= "+ methodcategory.getNmethodcatcode() + " and nstatus="
	  						   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	  		
	             ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);   			
	  			
	  			boolean validRecord = false;
	  			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
	  			{		
	  				validRecord = true;
	  				objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(methodcategory.getNmethodcatcode()), userInfo);
	  				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
	  				{					
	  					validRecord = true;
	  				}
	  				else {
	  					validRecord = false;
	  				}
	  			}
	  			
	  			if(validRecord) {
	  				
	  			final String updateQueryString = "update methodcategory set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus = "	+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nmethodcatcode="+ methodCategory.getNmethodcatcode();
	  			jdbcTemplate.execute(updateQueryString);
	  			methodCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
	  		    savedMethodCategoryList.add(methodCategory);
	  			multilingualIDList.add("IDS_DELETETESTMANAGEMENTMETHODCATEGORY");
	  			auditUtilityFunction.fnInsertAuditAction(savedMethodCategoryList, 1, null, multilingualIDList, userInfo);
	  			return getMethodCategory(methodCategory.getNsitecode());

	  		} else {
	  			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
	  			//return new ResponseEntity<>(commonFunctionObject.getMultilingualMessage("IDS_USEDMETHOD", userInfo.getSlanguagefilename()),	HttpStatus.EXPECTATION_FAILED);		
	  		}
	  	}
	  }

	  
}
