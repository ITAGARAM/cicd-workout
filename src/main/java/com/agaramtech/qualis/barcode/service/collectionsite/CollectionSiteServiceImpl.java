package com.agaramtech.qualis.barcode.service.collectionsite;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.CollectionSite;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'Collection Site' table
 * through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CollectionSiteServiceImpl implements CollectionSiteService {

	private final CollectionSiteDAO collectionSiteDAO;
	private final CommonFunction commonFunction;

	public CollectionSiteServiceImpl(CollectionSiteDAO collectionSiteDAO, CommonFunction commonFunction) {
		super();
		this.collectionSiteDAO = collectionSiteDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active Collection Site for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Collection Site
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionSite(UserInfo userInfo) throws Exception {
		return collectionSiteDAO.getCollectionSite(userInfo);
	}

	/**
	 * This method is used to add a new entry to Collection Site table.
	 * 
	 * @param collection Site [Collection Site] object holding details to be added
	 *                   in Collection Site table
	 * @return inserted Collection Site object with HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		return collectionSiteDAO.createCollectionSite(collectionSite, userInfo);
	}

	/**
	 * This method is used to retrieve active Collection Site object based on the
	 * specified ncollectionsitecode.
	 * 
	 * @param ncollectionsitecode [int] primary key of Collection Site object
	 * @return response entity object holding response status and data of Collection
	 *         Site object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveCollectionSiteById(int ncollectionsitecode, UserInfo userInfo)
			throws Exception {
		final CollectionSite objsiteno = collectionSiteDAO.getActiveCollectionSiteById(ncollectionsitecode, userInfo);
		if (objsiteno == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objsiteno, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in Collection Site table.
	 * 
	 * @param collection Site [Collection Site] object holding details to be updated
	 *                   in Collection Site table
	 * @return response entity object holding response status and data of updated
	 *         Collection Site object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		return collectionSiteDAO.updateCollectionSite(collectionSite, userInfo);
	}

	/**
	 * This method id used to delete an entry in Collection Site table
	 * 
	 * @param collection Site [Collection Site] an Object holds the record to be
	 *                   deleted
	 * @return a response entity with corresponding HTTP status and Collection Site
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		return collectionSiteDAO.deleteCollectionSite(collectionSite, userInfo);
	}
}
