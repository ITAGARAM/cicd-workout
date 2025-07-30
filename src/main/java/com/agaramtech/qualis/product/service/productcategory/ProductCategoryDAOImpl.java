package com.agaramtech.qualis.product.service.productcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;

import lombok.AllArgsConstructor;

/**
 * @date 22-Jun-2020
 * @time 9:30:08 AM
 */

@AllArgsConstructor
@Repository
public class ProductCategoryDAOImpl implements ProductCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This interface declaration is used to retrieve list of all active Product
	 * Category for the specified site through its DAO layer
	 * 
	 * @param userInfo as key for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Sample Category
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select p.*,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as scategorybasedflow,"
				+ " COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts2.jsondata->'stransdisplaystatus'->>'en-US') as scategorybasedprotocol,"
				+ " COALESCE(ts3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts3.jsondata->'stransdisplaystatus'->>'en-US') as sschedulerrequired,"
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus," + " to_char(p.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ " from productcategory p,transactionstatus ts,transactionstatus ts1,transactionstatus ts2,transactionstatus ts3 "
				+ " where p.ncategorybasedflow=ts.ntranscode and ts1.ntranscode=p.ndefaultstatus "
				+ " and p.ncategorybasedprotocol=ts2.ntranscode and p.nschedulerrequired = ts3.ntranscode and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode ="
				+ userInfo.getNmastersitecode() + " and p.nproductcatcode >0 order by p.nproductcatcode";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProductCategory()), HttpStatus.OK);
	}

	/**
	 * 
	 * This interface declaration is used to retrieve active Product Category object
	 * based on the specified nproductcatcode through its DAO layer.
	 * 
	 * @param nproductcatcode[int] is the primary key of the product
	 * @param UserInfo             as key for which the list is to be fetched
	 * @return response entity object holding response status and data of single
	 *         Product category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ProductCategory getActiveProductCategoryById(final int nproductcatcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select nproductcatcode, sproductcatname,"
				+ " sdescription, ncategorybasedflow, ncategorybasedprotocol,"
				+ " nschedulerrequired, ndefaultstatus, dmodifieddate, nsitecode,"
				+ " nstatus from productcategory  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode = "
				+ nproductcatcode;
		final ProductCategory objProductCategory = (ProductCategory) jdbcUtilityFunction.queryForObject(strQuery,
				ProductCategory.class, jdbcTemplate);
		return objProductCategory;
	}

	/**
	 * This interface declaration is used to create a new Product Category through
	 * its DAO layer.
	 * 
	 * @param productCategory [Product Category] is the selected product from the UI
	 * @param UserInfo        object entity.
	 * @return response entity for List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createProductCategory(final Map<String, Object> inputMap,
			final ProductCategory productCategory, final UserInfo userInfo) throws Exception {

		LOGGER.info("createProductCategory");

		final String sQuery = " lock  table lockproductcategory " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ProductCategory sampleCategoryListByName = getProductCategoryListByName(
				productCategory.getSproductcatname(), userInfo);

		final List<String> multilingualIDList = new ArrayList<>();

		final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();

		if (sampleCategoryListByName == null) {
			if (productCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final ProductCategory defaultProductCategory = getProductCategoryByDefaultStatus(
						productCategory.getNsitecode());
				if (defaultProductCategory != null) {
					final ProductCategory productCategoryBeforeSave = SerializationUtils.clone(defaultProductCategory);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(productCategoryBeforeSave);
					defaultProductCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update productcategory set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nproductcatcode ="
							+ defaultProductCategory.getNproductcatcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultProductCategory);
					final String editlabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo,
							"EditProductCategory");
					multilingualIDList.add(editlabel);
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoproductmanagement where stablename='productcategory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
					Integer.class);
			nSeqNo++;
			final String sdesc = productCategory.getSdescription() == null ? "" : productCategory.getSdescription();
			final String productCatInsert = "insert into productcategory(nproductcatcode,sproductcatname,sdescription,ncategorybasedflow,ncategorybasedprotocol,nschedulerrequired,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(productCategory.getSproductcatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(sdesc) + "'," + productCategory.getNcategorybasedflow() + ","
					+ productCategory.getNcategorybasedprotocol() + "," + productCategory.getNschedulerrequired() + ","
					+ productCategory.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(productCatInsert);
			jdbcTemplate.execute("update seqnoproductmanagement set nsequenceno = " + nSeqNo
					+ " where stablename='productcategory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "");
			final String addlabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo,
					"AddProductCategory");
			multilingualIDList.add(addlabel);
			final List<Object> savedManufacturerList = new ArrayList<>();
			savedManufacturerList.add(productCategory);
			final String sequencenoquery1 = "select nsequenceno from SeqNoConfigurationMaster where stablename ='treetemplatemaster' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno1 = jdbcTemplate.queryForObject(sequencenoquery1, Integer.class);
			nsequenceno1++;
			final String insertquery1 = "insert into treetemplatemaster (ntemplatecode,ncategorycode,sdescription,nrootcode,nformcode,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno1 + "," + nSeqNo + ",N'root',"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "," + " " + userInfo.getNformcode()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery1);

			final String updatequery1 = "update seqnoconfigurationmaster set nsequenceno =" + nsequenceno1
					+ " where stablename ='treetemplatemaster' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery1);
			productCategory.setNproductcatcode(nSeqNo);
			auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null, multilingualIDList, userInfo);
			return getProductCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get a default Product Category object with respect to
	 * the site code
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @param nmasterSiteCode [int] primary key of unit table nunitcode
	 * @return a ProductCategory Object
	 * @throws Exception that are from DAO layer
	 */
	private ProductCategory getProductCategoryByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from productcategory u where u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and u.nsitecode = " + nmasterSiteCode;
		return (ProductCategory) jdbcUtilityFunction.queryForObject(strQuery, ProductCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active Product Category objects for the
	 * specified manufacturer name and site.
	 * 
	 * @param Product  Category [String] Product Category for which the records are
	 *                 to be fetched
	 * @param siteCode [int] primary key of site object
	 * @return list of active edqmProduct Category based on the specified Product
	 *         Category name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ProductCategory getProductCategoryListByName(final String sproductcatname, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select nproductcatcode from Productcategory where sproductcatname = N'"
				+ stringUtilityFunction.replaceQuote(sproductcatname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		return (ProductCategory) jdbcUtilityFunction.queryForObject(strQuery, ProductCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to check the duplicate entry in the table other than the
	 * current object.
	 * 
	 * @param Product Category [sproductcatname] and nproductcatcode vzlue from the
	 *                object holding details to be updated in Product Category table
	 * @return response entity object holding response status and data of updated
	 *         Product Category object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ProductCategory duplicateCheckingForProductCategory(final String sproductcatname, final int nproductcatcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select nproductcatcode from Productcategory where sproductcatname = N'"
				+ stringUtilityFunction.replaceQuote(sproductcatname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode <> "
				+ nproductcatcode + " and nsitecode=" + userInfo.getNmastersitecode();
		return (ProductCategory) jdbcUtilityFunction.queryForObject(strQuery, ProductCategory.class, jdbcTemplate);
	}

	/**
	 * This interface declaration is used to update entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of productcategory [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateProductCategory(Map<String, Object> inputMap,
			final ProductCategory productCategory, final UserInfo userInfo) throws Exception {

		boolean treeTemplate = true;
		final String Qry = "select * from productcategory where nproductcatcode=" + productCategory.getNproductcatcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProductCategory objProductCategory = (ProductCategory) jdbcUtilityFunction.queryForObject(Qry,
				ProductCategory.class, jdbcTemplate);

        // ALPD-5640
		// Multi User Edit validation fixed		
		if(objProductCategory!=null)
		{
			if (objProductCategory.getNcategorybasedflow() != productCategory.getNcategorybasedflow()) {
				final String validationQry = "select * from treetemplatemanipulation where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode="
						+ productCategory.getNproductcatcode();
				List<TreeTemplateManipulation> lstTreeTemplate = (List<TreeTemplateManipulation>) jdbcTemplate
						.query(validationQry, new TreeTemplateManipulation());
				if (lstTreeTemplate.size() > 0) {
					treeTemplate = false;
				}
			}
		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (treeTemplate) {
			final ProductCategory productCategory2 = (ProductCategory) getActiveProductCategoryById(
					productCategory.getNproductcatcode(), userInfo);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
			if (productCategory2 == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final ProductCategory productCategoryByName = duplicateCheckingForProductCategory(
						productCategory.getSproductcatname(), productCategory.getNproductcatcode(), userInfo);
				if (productCategoryByName == null) {
					if (productCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final ProductCategory defaultProductCategory = getProductCategoryByDefaultStatus(
								productCategory.getNsitecode());

						if (defaultProductCategory != null && defaultProductCategory
								.getNproductcatcode() != productCategory.getNproductcatcode()) {
							final ProductCategory productCategoryBeforeSave = SerializationUtils
									.clone(defaultProductCategory);
							listBeforeUpdate.add(productCategoryBeforeSave);
							defaultProductCategory
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = " update productcategory set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus()
									+ " where nproductcatcode =" + defaultProductCategory.getNproductcatcode();
							jdbcTemplate.execute(updateQueryString);
							listAfterUpdate.add(defaultProductCategory);
						}
					}
					final String updateQueryString = "update productCategory set sproductcatname='"
							+ stringUtilityFunction.replaceQuote(productCategory.getSproductcatname())
							+ "', sdescription ='"
							+ stringUtilityFunction.replaceQuote(productCategory.getSdescription())
							+ "', ndefaultstatus =" + productCategory.getNdefaultstatus() + ", ncategorybasedflow ="
							+ productCategory.getNcategorybasedflow() + ", ncategorybasedprotocol="
							+ productCategory.getNcategorybasedprotocol() + ",nschedulerrequired = "
							+ productCategory.getNschedulerrequired() + ", dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcatcode ="
							+ productCategory.getNproductcatcode();
					jdbcTemplate.execute(updateQueryString);
					final String editlabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo,
							"EditProductCategory");
					multilingualIDList.add(editlabel);
					final List<Object> savedListAfterSave = new ArrayList<>();
					savedListAfterSave.add(productCategory);
					final List<Object> savedListBeforeSave = new ArrayList<>();
					savedListBeforeSave.add(productCategory2);
					auditUtilityFunction.fnInsertAuditAction(savedListAfterSave, 2, savedListBeforeSave,
							multilingualIDList, userInfo);
					return getProductCategory(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CONFIGUREDTESTGROUP", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This interface declaration is used to delete entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param mapObject [Map] object with keys of productcategory [Product Category]
	 *                  and UserInfo object entity.
	 * @return response entity of deleted Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteProductCategory(Map<String, Object> inputMap,
			final ProductCategory productCategory, final UserInfo userInfo) throws Exception {
		final ProductCategory productCategory2 = (ProductCategory) getActiveProductCategoryById(
				productCategory.getNproductcatcode(), userInfo);
		final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
		if (productCategory2 == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String msglabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo, "Product");
			final String query = "select '" + msglabel + "' as Msg from product where nproductcatcode= "
					+ productCategory.getNproductcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_QUOTATION' as Msg from quotation where nproductcatcode= "
					+ productCategory.getNproductcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_STUDYPLANTEMPLATE' as Msg from treetemplatemaster tt,treeversiontemplate tv  Where tt.ncategorycode ="
					+ productCategory.getNproductcatcode() + " and tt.nformcode =" + userInfo.getNformcode()
					+ " and  tt.ntemplatecode=tv.ntemplatecode  and  tt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(productCategory.getNproductcatcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedManufacturerList = new ArrayList<>();
				final String updateQueryString = "update productcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nproductcatcode ="
						+ productCategory.getNproductcatcode();
				jdbcTemplate.execute(updateQueryString);
				productCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedManufacturerList.add(productCategory);
				final String deletelabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo,
						"DeleteProductCategory");
				multilingualIDList.add(deletelabel);
				auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null, multilingualIDList, userInfo);
				return getProductCategory(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
