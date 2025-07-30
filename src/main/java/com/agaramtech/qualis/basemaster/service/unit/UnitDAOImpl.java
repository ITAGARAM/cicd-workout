package com.agaramtech.qualis.basemaster.service.unit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
//import com.twilio.Twilio;
//import com.twilio.rest.api.v2010.account.Message;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "unit" table by 
 * implementing methods from its interface. 
 */
//@Repository(value = "unitDAO")
@AllArgsConstructor
@Repository
public class UnitDAOImpl implements UnitDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
//	@Value("${MY_DATA}")
//	String myData;
	
	/**
	 * This method is used to retrieve list of all available units for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active units
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUnit(final UserInfo userInfo) throws Exception {
		
		//System.out.println("myData:"+myData);
		final String strQuery = "select u.nunitcode, u.sunitname, u.sunitsynonym, u.sdescription, "
								+ " u.ndefaultstatus, u.nsitecode, u.nstatus,"
								+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
								+ " from unit u,transactionstatus ts "
								+ " where u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.nunitcode > 0 and ts.ntranscode=u.ndefaultstatus and "
								+ " u.nsitecode="+userInfo.getNmastersitecode()
								+ " order by 1 desc";
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Unit()), HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve active unit object based on the specified nunitCode.
	 * @param nunitCode [int] primary key of unit object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of unit object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public Unit getActiveUnitById(int nunitCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select u.nunitcode, u.sunitname, u.sunitsynonym, u.sdescription,"
								+ "	u.ndefaultstatus, u.nsitecode, u.nstatus,"
								+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
								+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
								+ " from unit u,transactionstatus ts "
								+ " where u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.ntranscode = u.ndefaultstatus"
								+ " and u.nunitcode = " + nunitCode;
		return (Unit) jdbcUtilityFunction.queryForObject(strQuery, Unit.class, jdbcTemplate);
	}	

	/**
	 * This method is used to fetch the unit object for the specified unit name and site.
	 * @param sunitname [String] name of the unit
	 * @param nmasterSiteCode [int] site code of the unit
	 * @return unit object based on the specified unit name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Unit getUnitByName(String sunitname, int nmasterSiteCode) throws Exception {		
		final String strQuery = "select nunitcode from unit where sunitname = N'"
								+ stringUtilityFunction.replaceQuote(sunitname) + "' and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and nsitecode =" + nmasterSiteCode;
		return (Unit) jdbcUtilityFunction.queryForObject(strQuery, Unit.class, jdbcTemplate);
	}
	
	/**
	 * This method is used to get the default unit object with respect to the site.
	 * @param nmasterSiteCode [int] Site code
	 * @return Unit Object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Unit getUnitByDefaultStatus(int nmasterSiteCode) throws Exception {
		final String strQuery = "select u.nunitcode, u.sunitname, u.sunitsynonym, u.sdescription,"
								+ " u.ndefaultstatus, u.nsitecode, nstatus from unit u"
								+ " where u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and u.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and u.nsitecode = " + nmasterSiteCode;
		return (Unit) jdbcUtilityFunction.queryForObject(strQuery, Unit.class, jdbcTemplate);
	}
	
	/**
	 * This method is used to add a new entry to unit table.
	 * Unit Name is unique across the database. 
	 * Need to check for duplicate entry of unit name for the specified site before saving into database.	 * 
	 * Need to check that there should be only one default unit for a site.
	 * @param objUnit [Unit] object holding details to be added in unit table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved unit object with status code 200 if saved successfully else if the unit already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createUnit(Unit objUnit, UserInfo userInfo) throws Exception {
		
		final Unit unitByName = getUnitByName(objUnit.getSunitname(), objUnit.getNsitecode());
		if (unitByName == null) {
			
			final String sQuery = " lock  table unit "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);		
						
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedUnitList = new ArrayList<>();
			
			if(objUnit.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()){
				
				final Unit defaultUnit = getUnitByDefaultStatus(objUnit.getNsitecode());
				
				if(defaultUnit != null){
					
					//Copy of object before update
					final Unit unitBeforeSave = SerializationUtils.clone(defaultUnit);
					
					final List<Object> defaultListBeforeSave = new ArrayList<>();	
					defaultListBeforeSave.add(unitBeforeSave);					
					
					defaultUnit.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					
					final String updateQueryString = " update unit set ndefaultstatus=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
											         + " where nunitcode =" + defaultUnit.getNunitcode();
					jdbcTemplate.execute(updateQueryString);
					
					final List<Object> defaultListAfterSave = new ArrayList<>();	 
					defaultListAfterSave.add(defaultUnit);				
					
					multilingualIDList.add("IDS_EDITUNIT");
					
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave, multilingualIDList, userInfo);
					multilingualIDList.clear();
				}				
			}
	
			final String sequenceNoQuery ="select nsequenceno from seqnobasemaster where stablename ='unit'";
	   		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
	   		nsequenceNo++;
	   		
	   		final String insertQuery ="insert into unit (nunitcode,sunitname,sdescription,sunitsynonym,ndefaultstatus,dmodifieddate,nsitecode,nstatus) "
						   				+ " values(" + nsequenceNo 
						   				+ ",N'" + stringUtilityFunction.replaceQuote(objUnit.getSunitname()) + "',N'"
						   				+ stringUtilityFunction.replaceQuote(objUnit.getSdescription())+"',N'"
						   				+ stringUtilityFunction.replaceQuote(objUnit.getSunitsynonym())+"',"
						   				+ objUnit.getNdefaultstatus()+",'"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						   				+ userInfo.getNmastersitecode() + "," 
						   				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ ")";
	   		jdbcTemplate.execute(insertQuery);
	   		
	   		final String updateQuery ="update seqnobasemaster set nsequenceno ="+nsequenceNo+" where stablename='unit'";
	   		jdbcTemplate.execute(updateQuery);

	   		objUnit.setNunitcode(nsequenceNo);
			savedUnitList.add(objUnit);
			
			multilingualIDList.add("IDS_ADDUNIT");
			
			auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);
			
			//testSMS(nsequenceNo, objUnit.getSunitname());
			
			return getUnit(userInfo);			
		} 
		else 
		{
			//Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}	

	/**
	 * This method is used to update entry in unit  table.
	 * Need to validate that the unit object to be updated is active before updating 
	 * details in database.
	 * Need to check for duplicate entry of unit name for the specified site before saving into database.
	 * Need to check that there should be only one default unit for a site
	 * @param objUnit [Unit] object holding details to be updated in unit table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved unit object with status code 200 if saved successfully 
	 * 			else if the unit already exists, response will be returned as 'Already Exists' with status code 409
	 *          else if the unit to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateUnit(Unit objUnit,UserInfo userInfo) throws Exception {
		
		final Unit unit =  getActiveUnitById(objUnit.getNunitcode(), userInfo);	
		if (unit == null) 
		{
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else 
		{					
			final List<String> multilingualIDList  = new ArrayList<>();
			
			final List<Object> listAfterUpdate = new ArrayList<>();		
			final List<Object> listBeforeUpdate = new ArrayList<>();
			
			final String queryString = "select nunitcode from unit where sunitname = N'" 
										+ stringUtilityFunction.replaceQuote(objUnit.getSunitname()) 
										+ "' and nunitcode <> " + objUnit.getNunitcode()
										+ " and nsitecode =" + objUnit.getNsitecode()
										+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final Unit availableUnit = (Unit) jdbcUtilityFunction.queryForObject(queryString, Unit.class, jdbcTemplate);
			
			if (availableUnit == null) 
			{		
				//if yes need to set other default unit as not a default unit
				if(objUnit.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()){
					
					final Unit defaultUnit = getUnitByDefaultStatus(objUnit.getNsitecode());
					
					if(defaultUnit != null && defaultUnit.getNunitcode()!=objUnit.getNunitcode()){
						
						//Copy of object before update
						final Unit unitBeforeSave = SerializationUtils.clone(defaultUnit);						
						listBeforeUpdate.add(unitBeforeSave);						
						defaultUnit.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						
						final String updateQueryString = " update unit set ndefaultstatus="+Enumeration.TransactionStatus.NO.gettransactionstatus()
												  + " where nunitcode="+ defaultUnit.getNunitcode();
						jdbcTemplate.execute(updateQueryString);
						
						listAfterUpdate.add(defaultUnit);
						
						//multilingualIDList.add("IDS_EDITUNIT");
						
						//fnInsertAuditAction(updateUnitList, 2, unitBeforeSave, multilingualIDList, userInfo);	
					}						
				}
				final String updateQueryString = "update unit set sunitname=N'" 
												+ stringUtilityFunction.replaceQuote(objUnit.getSunitname()) 
												+ "', sdescription =N'" + stringUtilityFunction.replaceQuote(objUnit.getSdescription()) 
												+ "', sunitsynonym =N'" + stringUtilityFunction.replaceQuote(objUnit.getSunitsynonym()) 
												+ "', ndefaultstatus="+objUnit.getNdefaultstatus()
												+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"'"												
												+ " where nunitcode=" + objUnit.getNunitcode() +";";
				
				jdbcTemplate.execute(updateQueryString);
				
				listAfterUpdate.add(objUnit);
				listBeforeUpdate.add(unit);
				
				multilingualIDList.add("IDS_EDITUNIT");				
									
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);						
				
				//status code:200
				return getUnit(userInfo);
			} 
			else 
			{	
				//Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Unit table
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in other tables  such as 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * @param objUnit [Unit] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available unit objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteUnit(Unit objUnit,UserInfo userInfo) throws Exception {
		
		final Unit unit = getActiveUnitById(objUnit.getNunitcode(), userInfo);		
		if (unit == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else {
				
			final String query="select 'IDS_TESTPARAMETER' as Msg from testparameter where nunitcode= " 
			        		  + objUnit.getNunitcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        		  +" union all"
			        		  +" select 'IDS_TESTGROUPTESTPARAMETER' as Msg from testgrouptestparameter where nunitcode= " 
			        		  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        		  +" union all"
			        		  +" select 'IDS_TESTCONTAINERTYPE' as Msg from testcontainertype where nunitcode="
			        		  +objUnit.getNunitcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        		  +" union all"
			  				  +" select 'IDS_GOODSIN' as Msg from goodsinsample where nunitcode="
			  				  + objUnit.getNunitcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        		  +" union all"
			                  +" select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping where nunitcode= " 
    		                  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
    		                   +" union all"
    		                  +" select 'IDS_SAMPLESTORAGESTRUCTURE' as Msg from samplestoragelocation where nunitcode= " 
    		                  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							  +" union all"
							  +" select 'IDS_UNITCONVERSION' as Msg from unitconversion where nsourceunitcode= " 
							  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							  +" union all"
							  +" select 'IDS_UNITCONVERSION' as Msg from unitconversion where ndestinationunitcode= " 
							  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							  +" union all"
							  +" select 'IDS_SAMPLEPLANTMAPPING' as Msg from sampleplantmapping where nunitcode= " 
							  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							  +" union all"
							  +" select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where nunitcode= " 
							  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							  +" union all"
							  +" select 'IDS_STORAGESAMPLECOLLECTION' as Msg from storagesamplecollection where nunitcode= " 
							  + objUnit.getNunitcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);   			
			
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objUnit.getNunitcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else {
					validRecord = false;
				}
			}
			
			if(validRecord) {
				final List<String> multilingualIDList  = new ArrayList<>();
				final List<Object> deletedUnitList = new ArrayList<>();
				
				final String updateQueryString = "update unit set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)
												+ "',nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
												+ " where nunitcode=" + objUnit.getNunitcode()+";";
					
			    jdbcTemplate.execute(updateQueryString);
			    objUnit.setNstatus( (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							
			    deletedUnitList.add(objUnit);						
			    multilingualIDList.add("IDS_DELETEUNIT");
			    
			    auditUtilityFunction.fnInsertAuditAction(deletedUnitList, 1, null, multilingualIDList, userInfo);
				
				return getUnit(userInfo);
			}
			else{
				//status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
//	private void testSMS(final int nunitCode, final String sunitName) {
//		
//		final String ACCOUNT_SID="ACed025edc631c32a904ddae9d537eae9c";
//		final String AUTH_TOKEN ="8cc2db572b2042aa0b7fc313a3b3ce18";
//		 
//		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//		 
//		final String phNoArr[] = {"+919443047797","+919444560501"};
//		 
//		for (String phNo : phNoArr)
//		{
//	        final Message message = Message  
//	                              .creator(new com.twilio.type.PhoneNumber(phNo),
//	                                  new com.twilio.type.PhoneNumber("+12283002963"),
//	                                  "QuaLIS - Unit master created for unit name " + sunitName)
//	                              .create();
//
//	        System.out.println(message.getBody());
//	        
//	      //  ResourceSet messages = Message.reader().read();
//	      //  for (Message message1 : messages) {
//	            System.out.println(message.getSid() + " : " + message.getStatus());
//		 }
//	      //  }
//	            
//	            
////	            ListenableFuture<ResourceSet<Message>> future = Message.reader().readAsync();
////	            Futures.addCallback(
////	                future,
////	                new FutureCallback<ResourceSet<Message>>() {
////	                    public void onSuccess(ResourceSet<Message> messages) {
////	                        for (Message message : messages) {
////	                            System.out.println(message.getSid() + " : " + message.getStatus());
////	                         }
////	                     }
////	                     public void onFailure(Throwable t) {
////	                         System.out.println("Failed to get message status: " + t.getMessage());
////	                     }
////	                 });
//	}
}
