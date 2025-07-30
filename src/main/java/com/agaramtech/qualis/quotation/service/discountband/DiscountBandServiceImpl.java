package com.agaramtech.qualis.quotation.service.discountband;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.DiscountBand;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class DiscountBandServiceImpl implements DiscountBandService {

	private final DiscountBandDAO discountbandDAO;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available discountBand with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of discountBand records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDiscountBand(final UserInfo userInfo) throws Exception {

		return discountbandDAO.getDiscountBand(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to discountBand table.
	 * 
	 * @param objDiscountBand [DiscountBand] object holding details to be added in
	 *                        discountBand table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         discountBand object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createDiscountBand(final DiscountBand discountband, final UserInfo userInfo)
			throws Exception {
		return discountbandDAO.createDiscountBand(discountband, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active discountBand object based on the specified ndiscountbandcode.
	 * 
	 * @param ndiscountbandcode [int] primary key of discountBand object
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         discountBand object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDiscountBandById(final int ndiscountbandcode, final UserInfo userInfo)
			throws Exception {
		return discountbandDAO.getActiveDiscountBandById(ndiscountbandcode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in discountBand table.
	 * 
	 * @param objDiscountBand [DiscountBand] object holding details to be updated in
	 *                        discountBand table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         discountBand object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateDiscountBand(final DiscountBand quotationDiscountBand, final UserInfo userInfo)
			throws Exception {
		return discountbandDAO.updateDiscountBand(quotationDiscountBand, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in discountBand table.
	 * 
	 * @param objDiscountBand [DiscountBand] object holding detail to be deleted
	 *                        from discountBand table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         discountBand object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteDiscountBand(final DiscountBand quotationDiscountBand, final UserInfo userInfo)
			throws Exception {

		return discountbandDAO.deleteDiscountBand(quotationDiscountBand, userInfo);
	}

}
