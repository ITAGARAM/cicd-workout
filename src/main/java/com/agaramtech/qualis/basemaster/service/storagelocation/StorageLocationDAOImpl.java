package com.agaramtech.qualis.basemaster.service.storagelocation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.StorageLocation;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "storagelocation" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class StorageLocationDAOImpl implements StorageLocationDAO {
	
	  private static final Logger LOGGER = LoggerFactory.getLogger(StorageLocationDAOImpl.class);
	  
	  private final StringUtilityFunction stringUtilityFunction; 
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	  private ValidatorDel validatorDel; 
	  private final JdbcTemplateUtilityFunction jdbcUtilityFunction; 
	  private final ProjectDAOSupport projectDAOSupport; 
	  private final DateTimeUtilityFunction dateUtilityFunction; 
	  private final AuditUtilityFunction auditUtilityFunction;

	  /**
		 * This method is used to retrieve list of all available storagelocations for the specified site.
		 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
		 * 							  which the list is to be fetched
		 * @return response entity  object holding response status and list of all active storagelocations
		 * @throws Exception that are thrown from this DAO layer
		 */
	@Override
	public ResponseEntity<Object> getStorageLocation(final int nmasterSiteCode) throws Exception {

		final String strQuery = "select s.nstoragelocationcode, s.sstoragelocationname, s.sdescription,"
								+ "	s.nsitecode, s.nstatus from storagelocation s where s.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and nstoragelocationcode > 0 "
								+ " and s.nsitecode = " + nmasterSiteCode
								+ " order by 1 desc";
	
		LOGGER.info("Get Method:"+ strQuery);

		return new ResponseEntity<>((List<StorageLocation>) jdbcTemplate.query(strQuery, new StorageLocation()),
				HttpStatus.OK); 
	}

	/**
	 * This method is used to retrieve active storagelocation object based on the specified nstorageLocationCode.
	 * @param nstorageLocationCode [int] primary key of storagelocation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of storagelocation object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public StorageLocation getActiveStorageLocationById(final int nstorageLocationCode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select s.nstoragelocationcode, s.sstoragelocationname, s.sdescription,"
								+ "	s.nsitecode, s.nstatus from storagelocation s where s.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and s.nstoragelocationcode = " + nstorageLocationCode;

		return (StorageLocation) jdbcUtilityFunction.queryForObject(strQuery,
				StorageLocation.class, jdbcTemplate);

	}
	
	/**
	 * This method is used to fetch the storagelocation object for the specified storagelocation name and site.
	 * @param sstoragelocationname [String] name of the storagelocation
	 * @param nmasterSiteCode [int] site code of the storagelocation
	 * @return storagelocation object based on the specified storagelocation name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private StorageLocation getStorageLocationByName(final String storageLocationName, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nstoragelocationcode from storagelocation "
								+ " where sstoragelocationname = N'"
								+ stringUtilityFunction.replaceQuote(storageLocationName) 
								+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode =" + nmasterSiteCode;
		return (StorageLocation) jdbcUtilityFunction.queryForObject(strQuery, StorageLocation.class,jdbcTemplate);
	}


	/**
	 * This method is used to add a new entry to storagelocation table.
	 * Storage Location Name is unique across the database. 
	 * Need to check for duplicate entry of storage location name for the specified site before saving into database. 
	 * @param storageLocation [StorageLocation] object holding details to be added in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved storagelocation object with status code 200 if saved successfully else if the storagelocation already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStorageLocationList = new ArrayList<>();

		final StorageLocation storageLocationByName = getStorageLocationByName(
				storageLocation.getSstoragelocationname(), storageLocation.getNsitecode());

		if (storageLocationByName == null) {
		
			final String sequenceNoQuery ="select nsequenceno from seqnobasemaster where stablename ='storagelocation'";
	   		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
	   		nsequenceNo++;
	   		
	   		final String insertQuery ="insert into storagelocation (nstoragelocationcode, sstoragelocationname, sdescription,dmodifieddate,"
	   									+ "	nsitecode, nstatus) "
						   				+ " values(" + nsequenceNo 
						   				+ ",N'" + stringUtilityFunction.replaceQuote(storageLocation.getSstoragelocationname()) + "',N'"
						   				+ stringUtilityFunction.replaceQuote(storageLocation.getSdescription())+"','"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						   				+ userInfo.getNmastersitecode() + "," 
						   				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ ")";
	   		jdbcTemplate.execute(insertQuery);
	   		
	   		final String updateQuery ="update seqnobasemaster set nsequenceno ="+nsequenceNo+" where stablename='storagelocation'";
	   		jdbcTemplate.execute(updateQuery);

	   		storageLocation.setNstoragelocationcode(nsequenceNo);

			savedStorageLocationList.add(storageLocation);
			multilingualIDList.add("IDS_ADDSTORAGELOCATION");

			auditUtilityFunction.fnInsertAuditAction(savedStorageLocationList, 1, null, multilingualIDList, userInfo);
			
			return getStorageLocation(storageLocation.getNsitecode());
		} 
		else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	
	/**
	 * This method is used to update entry in storagelocation  table.
	 * Need to validate that the storagelocation object to be updated is active before updating 
	 * details in database.
	 * Need to check for duplicate entry of storagelocation name for the specified site before saving into database.
	 * @param objUnit [StorageLocation] object holding details to be updated in storagelocation table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved storagelocation object with status code 200 if saved successfully 
	 * 			else if the storagelocation already exists, response will be returned as 'Already Exists' with status code 409
	 *          else if the storagelocation to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {

		final StorageLocation storageLocationById = getActiveStorageLocationById(
				storageLocation.getNstoragelocationcode(), userInfo);

		if (storageLocationById == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		
		} else {
			final String queryString = "select nstoragelocationcode from storagelocation where sstoragelocationname = N'"
										+ stringUtilityFunction.replaceQuote(storageLocation.getSstoragelocationname())
										+ "' and nstoragelocationcode <> " + storageLocation.getNstoragelocationcode()
										+ " and nsitecode =" + storageLocation.getNsitecode()
										+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final StorageLocation availableStoragelocation = (StorageLocation) jdbcUtilityFunction.queryForObject(queryString,
					StorageLocation.class,jdbcTemplate);

			if (availableStoragelocation == null) {
				final String updateQueryString = "update storagelocation set dmodifieddate='"
												+ dateUtilityFunction.getCurrentDateTime(userInfo)+"', sstoragelocationname=N'"
												+ stringUtilityFunction.replaceQuote(storageLocation.getSstoragelocationname()) 
												+ "', sdescription =N'"
												+ stringUtilityFunction.replaceQuote(storageLocation.getSdescription()) 
												+ "' where nstoragelocationcode="
												+ storageLocation.getNstoragelocationcode();

				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITSTORAGELOCATION");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(storageLocation);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(storageLocationById);

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

				return getStorageLocation(storageLocation.getNsitecode());
			} 
			else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in storageLocation table
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in other tables 
	 * @param storageLocation [StorageLocation] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available storagelocation objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteStorageLocation(final StorageLocation storageLocation, final UserInfo userInfo)
			throws Exception {

		final StorageLocation activeStorageLocation = getActiveStorageLocationById(
				storageLocation.getNstoragelocationcode(), userInfo);
		
		if (activeStorageLocation == null) {
			// status code: 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			
			boolean validRecord = false;
			
			validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(storageLocation.getNstoragelocationcode()), userInfo);
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{					
				validRecord = true;
			}
		
			if(validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedStorageLocationList = new ArrayList<>();
				
				final String updateQueryString = "update storagelocation set dmodifieddate='"
											        + dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus = "
													+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() 
													+ " where nstoragelocationcode=" + storageLocation.getNstoragelocationcode()+";";
	
				jdbcTemplate.execute(updateQueryString);

				storageLocation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedStorageLocationList.add(storageLocation);
				multilingualIDList.add("IDS_DELETESTORAGELOCATION");

				auditUtilityFunction.fnInsertAuditAction(deletedStorageLocationList, 1, null, multilingualIDList, userInfo);
				
				return getStorageLocation(storageLocation.getNsitecode());
			}
			else
			{
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
