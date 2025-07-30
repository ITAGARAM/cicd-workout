package com.agaramtech.qualis.product.service.product;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.GenericLabel;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "product" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class ProductDAOImpl implements ProductDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * This method is used to retrieve list of active product based upon Approval
	 * Configuration for the specified site.
	 *
	 * @param inputMap [Map] map object with "nmastersitecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@Override
	public ResponseEntity<Object> getProduct(Integer nproductcode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		
		Product selectedProduct = null;
		String strQuery = "";
		
		if (nproductcode == null) {
			strQuery = "select p.nproductcode, p.nproductcatcode, p.sproductname, p.sdescription,"
						+ " p.ndefaultstatus, p.nsitecode, p.nstatus,"
						+ " pc.sproductcatname,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, "
						+ " to_char(p.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
						+ "') as smodifieddate from product p, productcategory pc, transactionstatus ts"
						+ " where ts.ntranscode = p.ndefaultstatus and p.nproductcatcode = pc.nproductcatcode "
						+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and p.nproductcode > 0 and p.nsitecode = " + userInfo.getNmastersitecode()
						+ " order by p.nproductcode ";

			final List<Product> productList = jdbcTemplate.query(strQuery, new Product());
			if (productList.isEmpty()) {
				outputMap.put("Product", productList);
				outputMap.put("SelectedProduct", null);
				outputMap.put("productFile", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("Product", productList);
				selectedProduct = productList.get(productList.size() - 1);
				nproductcode = selectedProduct.getNproductcode();

				final String query = "select p.noffsetdcreateddate,p.nproductfilecode,"
									+ " (select  count(nproductfilecode) from productfile where nproductfilecode>0 and nproductcode = "
									+ nproductcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ ") as ncount,p.sdescription,"
									+ " p.nproductfilecode as nprimarycode,p.sfilename,p.nproductcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,p.ssystemfilename,"
									+ " p.ndefaultstatus,p.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when p.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
									+ " end slinkname, p.nfilesize," + " case when p.nattachmenttypecode= "
									+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else"
									+ " COALESCE(TO_CHAR(p.dcreateddate,'" + Enumeration.DatabaseDateFormat.FORMAT_1.getDateFormat()
									+ "'),'-') end  as screateddate, "
									+ " p.nlinkcode, case when p.nlinkcode = -1 then p.nfilesize::varchar(1000) else '-' end sfilesize"
									+ " from productfile p,transactionstatus ts,attachmenttype at, linkmaster lm  "
									+ " where at.nattachmenttypecode = p.nattachmenttypecode and at.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and lm.nlinkcode = p.nlinkcode and lm.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and p.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and p.ndefaultstatus=ts.ntranscode and ts.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and p.nproductcode="
									+ nproductcode  + " and p.nsitecode = " + userInfo.getNmastersitecode()
									+ " order by p.nproductfilecode;";
				final List<ProductFile> productFileList = (List<ProductFile>) jdbcTemplate.query(query,
						new ProductFile());

				final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(productFileList,
						Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
						true);
				final ObjectMapper objMapper = new ObjectMapper();
				outputMap.put("productFile",
						objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<ProductFile>>() {

						}));

			}
		} else {
			final Product productList = getActiveProductById(nproductcode, userInfo);
			selectedProduct = productList;
			if (selectedProduct != null) {
				nproductcode = selectedProduct.getNproductcode();
			}

			strQuery = "select p.nproductcode, p.nproductcatcode, p.sproductname, p.sdescription, p.ndefaultstatus, p.nsitecode, p.nstatus,"
					+ " pc.sproductcatname,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
					+ " from product p, productcategory pc, transactionstatus ts"
					+ " where ts.ntranscode = p.ndefaultstatus and p.nproductcatcode = pc.nproductcatcode and p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and p.nproductcode > 0 and p.nsitecode = pc.nsitecode and pc.nsitecode = " + userInfo.getNmastersitecode()
					+ " order by p.nproductcode ";

			final List<Product> productLst = jdbcTemplate.query(strQuery, new Product());

			outputMap.put("Product", productLst);

			String query = "select p.noffsetdcreateddate,p.nproductfilecode,(select  count(nproductfilecode) from productfile where nproductfilecode>0 and nproductcode = "
							+ nproductcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") as ncount,p.sdescription,"
							+ " p.nproductfilecode as nprimarycode,p.sfilename,p.nproductcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "',"
							+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,p.ssystemfilename,"
							+ " p.ndefaultstatus,p.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
							+ userInfo.getSlanguagetypecode() + "',"
							+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when p.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
							+ " end slinkname, p.nfilesize," + " case when p.nattachmenttypecode= "
							+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else"
							+ " COALESCE(TO_CHAR(p.dcreateddate,'" + Enumeration.DatabaseDateFormat.FORMAT_1.getDateFormat()
							+ "'),'-') end  as screateddate, "
							+ " p.nlinkcode, case when p.nlinkcode = -1 then p.nfilesize::varchar(1000) else '-' end sfilesize"
							+ " from productfile p,transactionstatus ts,attachmenttype at, linkmaster lm  "
							+ " where at.nattachmenttypecode = p.nattachmenttypecode and at.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and lm.nlinkcode = p.nlinkcode and lm.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and p.ndefaultstatus=ts.ntranscode and ts.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nproductcode="
							+ nproductcode 
							+ " and p.nsitecode = " + userInfo.getNmastersitecode()
							+ " order by p.nproductfilecode;";
			final List<ProductFile> productFileList = (List<ProductFile>) jdbcTemplate.query(query, new ProductFile());

			final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(productFileList,
					Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
					true);
			final ObjectMapper objMapper = new ObjectMapper();
			outputMap.put("productFile",
					objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<ProductFile>>() {

					}));

		}
		if (selectedProduct == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedProduct", selectedProduct);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to insert the product based upon Approval Configuration
	 * for the specified site.
	 *
	 * @param inputMap [Map] map object with "product" object and userinfo as key
	 *                 for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@Override
	public ResponseEntity<Object> createProduct(Map<String, Object> obj, Product product, UserInfo userInfo)
			throws Exception {

		LOGGER.info("createProduct:"+product);
		
		final String sQueryLock = " lock  table lockproduct " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);
		

		final List<Product> productListByName = getProductListByName(product.getSproductname(), product.getNsitecode());

		if (productListByName.isEmpty()) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedProductList = new ArrayList<>();
			
			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();

			if (product.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final Product defaultProduct = getProductByDefaultStatus(product.getNsitecode(),
						product.getNproductcatcode());

				if (defaultProduct != null) {

					// Copy of object before update
					final Product productBeforeSave = SerializationUtils.clone(defaultProduct);

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(productBeforeSave);

					defaultProduct.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update product set ndefaultstatus="
													+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
													+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcode = "
													+ defaultProduct.getNproductcode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultProduct);

					final String editlabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo, "EditProduct");
					
					multilingualIDList.add(editlabel);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave, multilingualIDList, userInfo);
					multilingualIDList.clear();

				}

			}

			final String sGetSeqNoQuery = "select nsequenceno from seqnoproductmanagement where stablename = 'product';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			product.setNproductcode(nSeqNo++);

			final String sInsertQuery = "insert into product (nproductcode, nproductcatcode, sproductname, sdescription, ndefaultstatus, dmodifieddate, nsitecode, nstatus) "
										+ "values (" + nSeqNo + ", " + product.getNproductcatcode() + ", N'"
										+ stringUtilityFunction.replaceQuote(product.getSproductname()) + "', N'" + stringUtilityFunction.replaceQuote(product.getSdescription())
										+ "', " + product.getNdefaultstatus() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
										+ userInfo.getNmastersitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ ");";
			jdbcTemplate.execute(sInsertQuery);

			final String sUpdateSeqNoQuery = "update seqnoproductmanagement set nsequenceno = " + nSeqNo
											+ " where stablename = 'product';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);

			savedProductList.add(product);

			
			final String addlabel = projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo, "AddProduct");
			
			multilingualIDList.add(addlabel);

			auditUtilityFunction.fnInsertAuditAction(savedProductList, 1, null, multilingualIDList, userInfo);

			// status code:200
			return getProduct(null, userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active Product objects for the site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active Product based on site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<Product> getProductListByName(String productname, int nmasterSiteCode) throws Exception {
		// ALPD-144
		final String strQuery = "select nproductcode from product where sproductname = N'" + stringUtilityFunction.replaceQuote(productname)
								+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode =" + nmasterSiteCode;

		return (List<Product>) jdbcTemplate.query(strQuery, new Product());
	}

	/**
	 * This method is used to get a default Product object with respect to the site
	 * code
	 * 
	 * @param nsiteCode    [int] Site code
	 * @param nproductcode [int] primary key of product table nproductcode
	 * @return a Product Object
	 * @throws Exception that are from DAO layer
	 */
	private Product getProductByDefaultStatus(int nmasterSiteCode, int nproductCatCode) throws Exception {
		final String strQuery = "select nproductcode, nproductcatcode, sproductname, sdescription, ndefaultstatus, nsitecode, nstatus from product p"
								+ " where p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and p.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and nproductcatcode = " + nproductCatCode + " and p.nsitecode = " + nmasterSiteCode;
		return (Product) jdbcUtilityFunction.queryForObject(strQuery, Product.class, jdbcTemplate);
	}

	/**
	 * This method is used to delete the product based upon Approval Configuration
	 * for the specified site.
	 *
	 * @param Product  [Map] map object with "product" object
	 * @param userinfo as key for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@Override
	public ResponseEntity<Object> deleteProduct(Map<String, Object> obj, Product product, UserInfo userInfo)
			throws Exception {
		final Product productbyID = getActiveProductById(product.getNproductcode(), userInfo);

		if (productbyID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			
			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();

			//ATE234 Janakumar ALPD-5542 Sample type screen -> Alert showing wrongly when delete the existing data.
			final String query = "select 'IDS_TESTGROUP' as Msg from treetemplatemanipulation where nproductcode= "
								+ productbyID.getNproductcode() 
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " union all select 'IDS_SAMPLECOLLECTIONTYPE' as Msg from samplecollectiontype where nproductcode="
								+ product.getNproductcode() 
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " union all select 'IDS_QUOTATION' as Msg from quotation where nproductcode= "
								+ product.getNproductcode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ""
								+ " union all select 'IDS_SAMPLESTORAGESTRUCTURE' as Msg from samplestoragelocation where nproductcode= "
								+ product.getNproductcode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ""
								 + " union all select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping where nproductcode= "
								+ product.getNproductcode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ""
								+ " union all"
								+ " select 'IDS_SAMPLEPLANTMAPPING' as Msg from sampleplantmapping where nproductcode= " 
							    + product.getNproductcode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();							
			
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);  

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedProductList = new ArrayList<>();

				final String updateQueryString = "update product set nstatus = "
												+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " 
												+ "dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) 
												+ "' where nproductcode=" + product.getNproductcode();

				jdbcTemplate.execute(updateQueryString);

				product.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedProductList.add(product);			

				
				final String deleteLabel = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo, "DeleteProduct");
				multilingualIDList.add(deleteLabel);

				auditUtilityFunction.fnInsertAuditAction(deletedProductList, 1, null, multilingualIDList, userInfo);

				String StrProductFileQuery = "select * from productfile where nproductcode ="
												+ product.getNproductcode() + " and nstatus ="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and nsitecode="+ userInfo.getNmastersitecode();
				final List<ProductFile> objProductFile = jdbcTemplate.query(StrProductFileQuery, new ProductFile());

				final List<String> multilingualPFList = new ArrayList<>();
				final List<Object> deletedPFList = new ArrayList<>();

				final String updateString = "update productfile set nstatus ="
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + "dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcode=" + product.getNproductcode() 
											+ " ";

				jdbcTemplate.execute(updateString);
	
				deletedPFList.add(objProductFile);

				final String deleteLabelFile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList, userInfo, "DeleteProductFile");
				
				multilingualPFList.add(deleteLabelFile);

				auditUtilityFunction.fnInsertListAuditAction(deletedPFList, 1, null, multilingualPFList, userInfo);

				// status code:200
				return getProduct(null, userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This interface declaration is used to update entry in Product table through
	 * its DAO layer.
	 *
	 * @param inputMap [Map] object with keys of product [Product] and UserInfo
	 *                 object entity.
	 * @return response entity List of Product entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateProduct(Map<String, Object> obj, Product product, UserInfo userInfo)
			throws Exception {
		final Product objProduct = getActiveProductById(product.getNproductcode(), userInfo);

		if (objProduct == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			// ALPD-144
			final String queryString = "select nproductcode from product where sproductname = N'"
										+ stringUtilityFunction.replaceQuote(product.getSproductname()) 
										+ "' and nproductcode <> " + product.getNproductcode()
										+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode = " + userInfo.getNmastersitecode();

			final List<Product> productList = (List<Product>) jdbcTemplate.query(queryString, new Product());

			if (productList.isEmpty()) { // if yes need to set other default product as not a default product

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();

				if (product.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final Product defaultProduct = getProductByDefaultStatus(product.getNsitecode(),
							product.getNproductcatcode());

					if (defaultProduct != null && defaultProduct.getNproductcode() != product.getNproductcode()) {

						// Copy of object before update
						final Product productBeforeSave = SerializationUtils.clone(defaultProduct);
						listBeforeUpdate.add(productBeforeSave);

						defaultProduct.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update product set ndefaultstatus = "
															+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
															+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcode = "
															+ defaultProduct.getNproductcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultProduct);
					}

				}

				final String updateQueryString = "update product set sproductname=N'"
													+ stringUtilityFunction.replaceQuote(product.getSproductname()) + "', nproductcatcode = "
													+ product.getNproductcatcode() + ", sdescription =N'" + stringUtilityFunction.replaceQuote(product.getSdescription())
													+ "', ndefaultstatus = " + product.getNdefaultstatus() + ", dmodifieddate ='"
													+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcode=" + product.getNproductcode();

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(product);
				listBeforeUpdate.add(objProduct);

				final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();

				final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,userInfo, "EditProduct");
				
				multilingualIDList.add(multilingualData);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

				// status code:200
				return getProduct(product.getNproductcode(), userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active product object based on the specified
	 * nproductcode.
	 * 
	 * @param nproductcode [int] primary key of product object
	 * @return response entity object holding response status and data of product
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Product getActiveProductById(int nproductcode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select p.nproductcode, p.nproductcatcode, p.sproductname, p.sdescription,"
								+ " p.ndefaultstatus, p.nsitecode, p.nstatus,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode()
								+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, pc.sproductcatname from product p, transactionstatus ts, productcategory pc "
								+ " where p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and pc.nproductcatcode = p.nproductcatcode " + " and ts.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = p.ndefaultstatus"
								+ " and p.nproductcode = " + nproductcode;

		return (Product) jdbcUtilityFunction.queryForObject(strQuery, Product.class, jdbcTemplate);

	}

	/**
     * Creates and stores a new product file from a multipart request.
     * Stores metadata and handles file upload logic (to disk, FTP, or DB).
     *
     * @param request   contains file and metadata.
     * @param userInfo  user performing the upload.
     * @return result indicating success or stored path.
     * @throws Exception on file or DB failure.
     */
	@Override
	public ResponseEntity<Object> createProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final String sQueryLock = " lock  table productfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQueryLock);

		final List<ProductFile> lstReqProductFile = objMapper.readValue(request.getParameter("productfile"),
				new TypeReference<List<ProductFile>>() {
				});
		if (lstReqProductFile != null && lstReqProductFile.size() > 0) {
			final Product objProduct = checkProductIsPresent(lstReqProductFile.get(0).getNproductcode());
			
			final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
			
			String addproductfile = "";

			if (objProduct != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqProductFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name - master
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
											+ " from productfile where nproductcode = " + lstReqProductFile.get(0).getNproductcode()
											+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus();
					final ProductFile lstProductFiles = (ProductFile) jdbcUtilityFunction.queryForObject(sQuery, ProductFile.class, jdbcTemplate);

					ProductFile objProductFile = lstReqProductFile.get(0);

					if (lstProductFiles != null) {

						if (objProductFile.getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final ProductFile ProductFileBeforeSave = SerializationUtils.clone(lstProductFiles);

							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(ProductFileBeforeSave);

							final String updateQueryString = " update productfile set ndefaultstatus=" + " "
															+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
															+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nproductfilecode ="
															+ lstProductFiles.getNproductfilecode();
							jdbcTemplate.execute(updateQueryString);

							lstProductFiles
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final List<Object> defaultListAfterSave = new ArrayList<>();
							final List<String> multilingualIDList = new ArrayList<>();
							defaultListAfterSave.add(lstProductFiles);

							final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "EditProductFile");
														
							multilingualIDList.add(multilingualData);

							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave, multilingualIDList,
									objUserInfo);

						}

					}

					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());

					lstReqProductFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}

					});

					String sequencequery = "select nsequenceno from seqnoproductmanagement where stablename ='productfile'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;
					final String insertquery = "Insert into productfile(nproductfilecode,nproductcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,ntzcreateddate,noffsetdcreateddate,ssystemfilename,dmodifieddate,ndefaultstatus,nstatus,nsitecode)"
											+ "values (" + nsequenceno + "," + lstReqProductFile.get(0).getNproductcode() + ","
											+ lstReqProductFile.get(0).getNlinkcode() + ","
											+ lstReqProductFile.get(0).getNattachmenttypecode() + "," + " N'"
											+ stringUtilityFunction.replaceQuote(lstReqProductFile.get(0).getSfilename()) + "',N'"
											+ stringUtilityFunction.replaceQuote(lstReqProductFile.get(0).getSdescription()) + "',"
											+ lstReqProductFile.get(0).getNfilesize() + "," + " '"
											+ lstReqProductFile.get(0).getDcreateddate() + "'," + objUserInfo.getNtimezonecode() + ","
											+ lstReqProductFile.get(0).getNoffsetdcreateddate() + ",N'"
											+ lstReqProductFile.get(0).getSsystemfilename() + "','" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "', " + lstReqProductFile.get(0).getNdefaultstatus() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ objUserInfo.getNmastersitecode() + ")";
					jdbcTemplate.execute(insertquery);

					String updatequery = "update seqnoproductmanagement set nsequenceno =" + nsequenceno
										+ " where stablename ='productfile'";
					jdbcTemplate.execute(updatequery);

					final List<String> multilingualIDList = new ArrayList<>();

					if (lstReqProductFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				
						addproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "AddProductFile");
						
						
					} else if (lstReqProductFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.LINK
							.gettype()) {
				
						addproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "AddProductLink");
						
					}
					multilingualIDList.add(addproductfile);

					final List<Object> listObject = new ArrayList<Object>();

					final String auditqry = "select * from productfile where nproductcode = "
											+ lstReqProductFile.get(0).getNproductcode() + " and nproductfilecode = " + nsequenceno
											+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<ProductFile> lstvalidate = (List<ProductFile>) jdbcTemplate.query(auditqry,
							new ProductFile());

					listObject.add(lstvalidate);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ProductAlreadyDeleted");

				return new ResponseEntity<>(multilingualData, HttpStatus.EXPECTATION_FAILED);
			}
			return (getProductFile(lstReqProductFile.get(0).getNproductcode(), objUserInfo));
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Product checkProductIsPresent(final int nproductcode) throws Exception {
		final String strQuery = "select nproductcode from product where nproductcode = " + nproductcode + " and  nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Product objProduct = (Product) jdbcUtilityFunction.queryForObject(strQuery, Product.class, jdbcTemplate);
		return objProduct;
	}

	/**
     * Retrieves list of all files attached to a product.
     * Filters by product ID and user’s site code.
     *
     * @param nproductcode product whose files to fetch.
     * @param userInfo     context of current user.
     * @return list of product file records.
     * @throws Exception on query failure.
     */
	public ResponseEntity<Object> getProductFile(Integer nproductcode, UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String query = "select p.noffsetdcreateddate,p.nproductfilecode,(select  count(nproductfilecode) from productfile where nproductfilecode>0 and nproductcode = "
						+ nproductcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ") as ncount,p.sdescription,"
						+ " p.nproductfilecode as nprimarycode,p.sfilename,p.nproductcode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
						+ objUserInfo.getSlanguagetypecode() + "',"
						+ "	ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,p.ssystemfilename,"
						+ " p.ndefaultstatus,p.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
						+ objUserInfo.getSlanguagetypecode() + "',"
						+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when p.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
						+ " end slinkname, p.nfilesize," + " case when p.nattachmenttypecode= "
						+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(p.dcreateddate,'"
						+ Enumeration.DatabaseDateFormat.FORMAT_1.getDateFormat() + "'),'-') end  as screateddate, "
						+ " p.nlinkcode, case when p.nlinkcode = -1 then p.nfilesize::varchar(1000) else '-' end sfilesize"
						+ " from productfile p,transactionstatus ts,attachmenttype at, linkmaster lm  "
						+ " where at.nattachmenttypecode = p.nattachmenttypecode and at.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and lm.nlinkcode = p.nlinkcode and lm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and p.ndefaultstatus=ts.ntranscode and ts.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nproductcode=" + nproductcode
						+ " order by p.nproductfilecode;";
		final List<ProductFile> productFileList = (List<ProductFile>) jdbcTemplate.query(query, new ProductFile());

		final List<?> lstUTCConvertedDate = dateUtilityFunction.getSiteLocalTimeFromUTC(productFileList,
				Arrays.asList("screateddate"), Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null,
				true);
		final ObjectMapper objMapper = new ObjectMapper();
		outputMap.put("productFile",
				objMapper.convertValue(lstUTCConvertedDate, new TypeReference<List<ProductFile>>() {

				}));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
     * Edits metadata (filename, description) of an existing product file.
     *
     * @param productFile file object with updated fields.
     * @param userInfo    user making the edit.
     * @return update result.
     * @throws Exception on update error.
     */
	@Override
	public ResponseEntity<Object> editProductFile(final Map<String, Object> inputMap, final ProductFile objProductFile,
			final UserInfo objUserInfo) throws Exception {

		final Product product = checkProductIsPresent(objProductFile.getNproductcode());
		
		final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
		
		final String labelName = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ProductAlreadyDeleted");


		if (product != null) {
			final String sEditQuery = "select pf.nproductfilecode, pf.nproductcode, pf.nlinkcode, pf.nattachmenttypecode, pf.sfilename, pf.sdescription, pf.nfilesize,"
									+ " pf.ssystemfilename, pf.ndefaultstatus, lm.jsondata->>'slinkname' as slinkname"
									+ " from productfile pf, linkmaster lm where lm.nlinkcode = pf.nlinkcode" + " and pf.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pf.nproductfilecode = "
									+ objProductFile.getNproductfilecode();
			final ProductFile objPF = (ProductFile) jdbcUtilityFunction.queryForObject(sEditQuery, ProductFile.class, jdbcTemplate);

			if (objPF != null) {
				return new ResponseEntity<Object>(objPF, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(labelName, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
     * Updates the file content by replacing the existing file with a new upload.
     *
     * @param request   multipart request with new file.
     * @param userInfo  user performing the replacement.
     * @return result indicating success.
     * @throws Exception on file I/O or DB error.
     */
	@Override
	public ResponseEntity<Object> updateProductFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		String editproductfile = "";		
		final List<GenericLabel> genericLabelList = projectDAOSupport.getGenericLabel();
		
		
		final List<ProductFile> lstProductFile = objMapper.readValue(request.getParameter("productfile"),
				new TypeReference<List<ProductFile>>() {
				});
		if (lstProductFile != null && lstProductFile.size() > 0) {
			final ProductFile objProductFile = lstProductFile.get(0);
			final Product objProduct = checkProductIsPresent(objProductFile.getNproductcode());

			if (objProduct != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
										+ " from productfile where nproductfilecode = " + objProductFile.getNproductfilecode()
										+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final ProductFile objPF = (ProductFile) jdbcUtilityFunction.queryForObject(sQuery, ProductFile.class, jdbcTemplate);

//					final String sCheckDefaultQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
//														+ " from productfile where nproductcode = " + objProductFile.getNproductcode()
//														+ " and nproductfilecode!=" + objProductFile.getNproductfilecode() + " and nstatus = "
//														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					final List<ProductFile> lstDefProductFiles = (List<ProductFile>) jdbcTemplate
//							.query(sCheckDefaultQuery, new ProductFile());

					if (objPF != null) {
						String ssystemfilename = "";
						if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objProductFile.getSsystemfilename();
						}

						if (lstProductFile.get(0).getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final String sDefaultQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
															+ " from productfile where nproductcode = " + objProductFile.getNproductcode()
															+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and ndefaultstatus = "
															+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<ProductFile> lstProductFiles = (List<ProductFile>) jdbcTemplate
									.query(sDefaultQuery, new ProductFile());

							if (!lstProductFiles.isEmpty()) {
								final String updateQueryString = " update productfile set ndefaultstatus=" + " "
																	+ Enumeration.TransactionStatus.NO.gettransactionstatus()
																	+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
																	+ "' where nproductfilecode =" + lstProductFiles.get(0).getNproductfilecode();
								jdbcTemplate.execute(updateQueryString);
							}
						} else {
							final String sDefaultQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
														+ " from productfile where nproductcode = " + objProductFile.getNproductcode()
														+ " and nProductfilecode=" + objProductFile.getNproductfilecode() + ""
														+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
														+ " and ndefaultstatus = "
														+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<ProductFile> lstProductFiles = (List<ProductFile>) jdbcTemplate
									.query(sDefaultQuery, new ProductFile());

							if (!lstProductFiles.isEmpty()) {
								final String sEditDefaultQuery = "update productfile set " + " ndefaultstatus = "
																+ Enumeration.TransactionStatus.YES.gettransactionstatus()
																+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
																+ "' where nproductfilecode = " + objProductFile.getNproductfilecode();
								jdbcTemplate.execute(sEditDefaultQuery);
							}

						}
						final String sUpdateQuery = "update productfile set sfilename=N'"
													+ stringUtilityFunction.replaceQuote(objProductFile.getSfilename()) + "'," + " sdescription=N'"
													+ stringUtilityFunction.replaceQuote(objProductFile.getSdescription()) + "', ssystemfilename= N'"
													+ ssystemfilename + "'," + " nattachmenttypecode = "
													+ objProductFile.getNattachmenttypecode() + ", nlinkcode="
													+ objProductFile.getNlinkcode() + "," + " nfilesize = " + objProductFile.getNfilesize()
													+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + "ndefaultstatus = "
													+ objProductFile.getNdefaultstatus() + "" + " where nproductfilecode = "
													+ objProductFile.getNproductfilecode();
						objProductFile.setDcreateddate(objPF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();

						if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						
							editproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "EditProductFile");							

						} else if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.LINK
								.gettype()) {
						
							editproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "EditProductLink");
						}

						multilingualIDList.add(editproductfile);
						lstOldObject.add(objPF);

						auditUtilityFunction.fnInsertAuditAction(lstProductFile, 2, lstOldObject, multilingualIDList, objUserInfo);
						
						return (getProductFile(objProductFile.getNproductcode(), objUserInfo));
						
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
		
				final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ProductAlreadyDeleted");
				return new ResponseEntity<>(multilingualData, HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
     * Deletes the specified product file.
     * May include physical file deletion if configured.
     *
     * @param productFile file to delete.
     * @param userInfo    user performing the deletion.
     * @return confirmation of deletion.
     * @throws Exception if file not found or delete fails.
     */
	@Override
	public ResponseEntity<Object> deleteProductFile(Map<String, Object> inputMap, ProductFile objProductFile,
			UserInfo objUserInfo) throws Exception {
		final Product product = checkProductIsPresent(objProductFile.getNproductcode());

		final List<GenericLabel> genericLabelList = (List<GenericLabel>) projectDAOSupport.getGenericLabel();
		
		if (product != null) {			
			
			if (objProductFile != null) {
				final String sQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
									+ " from productfile where nproductfilecode = " + objProductFile.getNproductfilecode()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ProductFile objPF = (ProductFile) jdbcUtilityFunction.queryForObject(sQuery, ProductFile.class, jdbcTemplate);
				
				if (objPF != null) {
					if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
//						deleteFTPFile(Arrays.asList(objPF.getSsystemfilename()), "master", objUserInfo);
					} else {
						objProductFile.setScreateddate(null);
					}

					if (objPF.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						final String sDeleteQuery = "select nproductfilecode, nproductcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, nfilesize, ssystemfilename, ndefaultstatus, nstatus"
													+ " from productfile where " + " nproductfilecode !="
													+ objProductFile.getNproductfilecode() + "" + " and nproductcode="
													+ objProductFile.getNproductcode() + "" + " and nstatus = "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						List<ProductFile> lstProductFiles = (List<ProductFile>) jdbcTemplate.query(sDeleteQuery,
								new ProductFile());
						
						String sDefaultQuery = "";
						if (lstProductFiles.isEmpty()) {
							sDefaultQuery = " update productfile set  " + "  dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' ,nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+ " where nproductfilecode = " + objProductFile.getNproductfilecode();
						} else {
							sDefaultQuery = "update productfile set " + "  dmodifieddate ='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', ndefaultstatus = "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus()
											+ " where nproductfilecode = "
											+ lstProductFiles.get(lstProductFiles.size() - 1).getNproductfilecode();
						}
						jdbcTemplate.execute(sDefaultQuery);
					}
					final String sUpdateQuery = "update productfile set dmodifieddate ='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
												+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
												+ " where nproductfilecode = " + objProductFile.getNproductfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					String deleteproductfile = "";

					if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						
						deleteproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "DeleteProductFile");

					} else if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
					
						deleteproductfile = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "DeleteProductLink");
					}
					multilingualIDList.add(deleteproductfile);
					
					lstObject.add(objProductFile);
					
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getProductFile(objProductFile.getNproductcode(), objUserInfo);
		} else {
			// status code:417
		
			final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ProductAlreadyDeleted");
			
			return new ResponseEntity<>(multilingualData, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
     * Fetches and returns metadata or binary content of a file for download/view.
     *
     * @param productFile file to retrieve.
     * @param userInfo    requesting user.
     * @return map with file content, metadata, path.
     * @throws Exception on access failure.
     */
	@Override
	public Map<String, Object> viewAttachedProductFile(Map<String, Object> inputMap, ProductFile objProductFile,
			UserInfo objUserInfo) throws Exception {
		
		Map<String, Object> map = new HashMap<>();

		final Product objProduct = checkProductIsPresent(objProductFile.getNproductcode());
		
		final List<GenericLabel> genericLabelList = (List<GenericLabel>) projectDAOSupport.getGenericLabel();
		
		if (objProduct != null) {
			String sQuery = "select * from productfile where nproductfilecode = " + objProductFile.getNproductfilecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ProductFile objPF = (ProductFile) jdbcUtilityFunction.queryForObject(sQuery, ProductFile.class, jdbcTemplate);
			
			if (objPF != null) {
				if (objPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objPF.getSsystemfilename(), -1, objUserInfo, "", "");// Folder Name - master
				} else {
					
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objPF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class, jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objPF.getSfilename());
					
					objProductFile.setScreateddate(null);
				}
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				String viewproductfile = "";
				
				if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
		
					viewproductfile= (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ViewProductFile");

				} else if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype()) {
				
					viewproductfile= (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ViewProductLink");
				}
				multilingualIDList.add(viewproductfile);
				
				lstObject.add(objProductFile);
				
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()));
			}
		} else {
			
			final String multilingualData = (String)projectDAOSupport.getSpecificGenericLabel(genericLabelList,objUserInfo, "ProductAlreadyDeleted");
			
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), multilingualData);
		}
		return map;

	}

}
