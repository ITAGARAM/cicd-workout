package com.agaramtech.qualis.materialmanagement.service.materialcategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
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
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class MaterialCategoryDAOImpl implements MaterialCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@Override
	public ResponseEntity<Object> getMaterialCategory(final UserInfo userInfo) throws Exception {

		final String strQuery = "select m.nmaterialcatcode,m.nmaterialtypecode,m.nuserrolecode,m.nbarcode,m.ncategorybasedflow,m.nactivestatus,m.needsectionwise,m.smaterialcatname,"
				+ " case when m.sdescription = null then '-' when m.sdescription = '' then '-' else m.sdescription end sdescription,m.ndefaultstatus,"
				+ "m.dmodifieddate,m.nsitecode,m.nstatus," + "coalesce(mt.jsondata->'smaterialtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "mt.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename,b.nbarcode,case when b.nbarcode = -1 then '-'else b.sbarcodename end sbarcodename, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') as scategorybasedflow,"
				+ "coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts2.jsondata->'stransdisplaystatus'->>'en-US') as sneedSectionwise,needSectionwise"
				+ " from materialcategory m,"
				+ "transactionstatus ts,transactionstatus ts1,transactionstatus ts2,materialtype mt, barcode b  where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "    AND b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and m.nbarcode=b.nbarcode and m.ncategorybasedflow = ts1.ntranscode"
				+ " and mt.nmaterialtypecode=m.nmaterialtypecode" + " and m.needSectionwise = ts2.ntranscode "
				+ " and mt.nmaterialtypecode!=5"
				+ " and m.nmaterialcatcode > 0 and ts.ntranscode=m.ndefaultstatus and m.nsitecode = "
				+ userInfo.getNmastersitecode()
				+ " and b.nsitecode="+ userInfo.getNmastersitecode();
		LOGGER.info(strQuery);
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new MaterialCategory()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialCategoryBySupplierCode(final int nmasterSiteCode, final int nsuppliercode,
			UserInfo userInfo) throws Exception {
		final String strQuery = "select m.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus" + " from materialcategory m,"
				+ "transactionstatus ts where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and m.nmaterialcatcode>0 and ts.ntranscode=m.ndefaultstatus and m.nsitecode = " + nmasterSiteCode
				+ " and m.nmaterialcatcode not in (Select ncategorycode from suppliermatrix where ntypecode = "
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + "" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsuppliercode = " + nsuppliercode
				+ ")";

		return new ResponseEntity<>((List<MaterialCategory>) jdbcTemplate.query(strQuery, new MaterialCategory()),
				HttpStatus.OK);
	}

	@Override
	public MaterialCategory getActiveMaterialCategoryById(final int nmaterialcatcode, UserInfo userInfo) throws Exception {
		MaterialCategory objMaterialCategory = new MaterialCategory();
		Integer needSectionCheck = jdbcTemplate
				.queryForObject("select count(*) from materialinventory where (jsondata->'nmaterialcatcode')::int"
						+ "  in (select nmaterialcatcode from materialcategory where  nmaterialcatcode="
						+ nmaterialcatcode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="
						+ userInfo.getNmastersitecode(), Integer.class);

		final String strQuery = "select m.* from materialcategory m where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nmaterialcatcode = "
				+ nmaterialcatcode;
		objMaterialCategory = (MaterialCategory) jdbcUtilityFunction.queryForObject(strQuery, MaterialCategory.class,
				jdbcTemplate);
		if (needSectionCheck > 0) {
			objMaterialCategory.setNeedSectionwisedisabled(true);
		}
		return (MaterialCategory) objMaterialCategory;
	}

	@Override
	public ResponseEntity<Object> createMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedMaterialCategoryList = new ArrayList<>();
		final List<MaterialCategory> materialCategoryListByName = getMaterialCategoryListByName(
				materialCategory.getSmaterialcatname(), materialCategory.getNsitecode(),
				materialCategory.getNmaterialtypecode());
		if (materialCategoryListByName.isEmpty()) {
			if (materialCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final MaterialCategory defaultMaterialCategory = getMaterialCategoryByDefaultStatus(
						materialCategory.getNsitecode(), materialCategory.getNmaterialtypecode());
				if (defaultMaterialCategory != null) {
					final MaterialCategory materialCategoryBeforeSave = SerializationUtils
							.clone(defaultMaterialCategory);

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(materialCategoryBeforeSave);
					defaultMaterialCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update materialcategory set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nmaterialcatcode ="
							+ defaultMaterialCategory.getNmaterialcatcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultMaterialCategory);
					multilingualIDList.add("IDS_EDITMATERIALCATEGORY");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
				}
			}
			String sequencequery = "select nsequenceno from SeqNoMaterialManagement where stablename ='materialcategory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;

			String insertquery = "Insert into materialcategory (nmaterialcatcode,nmaterialtypecode,nuserrolecode,nbarcode,ncategorybasedflow,nactivestatus,smaterialcatname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,needSectionwise,nstatus)"
					+ "values(" + nsequenceno + "," + materialCategory.getNmaterialtypecode() + ","
					+ materialCategory.getNuserrolecode() + "," + materialCategory.getNbarcode() + ","
					+ materialCategory.getNcategorybasedflow() + "," + materialCategory.getNactivestatus() + ",N'"
					+ stringUtilityFunction.replaceQuote(materialCategory.getSmaterialcatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(materialCategory.getSdescription()) + "',"
					+ materialCategory.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ " '," + userInfo.getNmastersitecode() + "," + materialCategory.getNeedSectionwise() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			String updatequery = "update SeqNoMaterialManagement set nsequenceno =" + nsequenceno
					+ " where stablename ='materialcategory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updatequery);

			String sequencenoquery1 = "select nsequenceno from SeqNoConfigurationMaster where stablename ='treetemplatemaster' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno1 = jdbcTemplate.queryForObject(sequencenoquery1, Integer.class);
			nsequenceno1++;
			String insertquery1 = "Insert into treetemplatemaster (ntemplatecode,ncategorycode,sdescription,nrootcode,nformcode,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno1 + "," + nsequenceno + ",N'root',"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "," + " " + userInfo.getNformcode()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery1);

			String updatequery1 = "update SeqNoConfigurationMaster set nsequenceno =" + nsequenceno1
					+ " where stablename ='treetemplatemaster' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updatequery1);

			materialCategory.setNmaterialcatcode(nsequenceno);
			savedMaterialCategoryList.add(materialCategory);

			auditUtilityFunction.fnInsertAuditAction(savedMaterialCategoryList, 1, null,
					Arrays.asList("IDS_ADDMATERIALCATEGORY"), userInfo);
			return getMaterialCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private MaterialCategory getMaterialCategoryByDefaultStatus(int nmasterSiteCode, int nmaterialtypecode)
			throws Exception {
		final String strQuery = "select m.* from materialcategory m" + " where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and m.nsitecode = " + nmasterSiteCode
				+ " and m.nmaterialtypecode=" + nmaterialtypecode + "";

		return (MaterialCategory) jdbcUtilityFunction.queryForObject(strQuery, MaterialCategory.class, jdbcTemplate);
	}

	private List<MaterialCategory> getMaterialCategoryListByName(final String smaterialcatname,
			final int nmasterSiteCode, final int nmaterialtypecode) throws Exception {
		final String strQuery = "select nmaterialcatcode from materialcategory where smaterialcatname = N'"
				+ stringUtilityFunction.replaceQuote(smaterialcatname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;

		return (List<MaterialCategory>) jdbcTemplate.query(strQuery, new MaterialCategory());
	}

	@Override
	public ResponseEntity<Object> updateMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		final MaterialCategory objMaterialCategory = getActiveMaterialCategoryById(
				materialCategory.getNmaterialcatcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (objMaterialCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nmaterialcatcode from materialcategory where smaterialcatname = N'"
					+ stringUtilityFunction.replaceQuote(materialCategory.getSmaterialcatname())
					+ "' and nmaterialcatcode <> " + materialCategory.getNmaterialcatcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialtypecode = "
					+ materialCategory.getNmaterialtypecode()+ " and nsitecode="+ userInfo.getNmastersitecode();

			final List<MaterialCategory> materialCategoryList = (List<MaterialCategory>) jdbcTemplate.query(queryString,
					new MaterialCategory());

			if (materialCategoryList.isEmpty()) {
				if (materialCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final MaterialCategory defaultMaterialCategory = getMaterialCategoryByDefaultStatus(
							materialCategory.getNsitecode(), materialCategory.getNmaterialtypecode());
					if (defaultMaterialCategory != null && defaultMaterialCategory
							.getNmaterialcatcode() != materialCategory.getNmaterialcatcode()) {
						final MaterialCategory materialCategoryBeforeSave = SerializationUtils
								.clone(defaultMaterialCategory);
						listBeforeUpdate.add(materialCategoryBeforeSave);
						defaultMaterialCategory
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update materialcategory set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nmaterialcatcode="
								+ defaultMaterialCategory.getNmaterialcatcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultMaterialCategory);
					}

				}
				final String updateQueryString = "update materialcategory set smaterialcatname=N'"
						+ stringUtilityFunction.replaceQuote(materialCategory.getSmaterialcatname())
						+ "', sdescription ='" + stringUtilityFunction.replaceQuote(materialCategory.getSdescription())
						+ "',ndefaultstatus = " + materialCategory.getNdefaultstatus() + ",nmaterialtypecode = "
						+ materialCategory.getNmaterialtypecode() + ",nuserrolecode = "
						+ materialCategory.getNuserrolecode() + ",nbarcode = " + materialCategory.getNbarcode()
						+ ",ncategorybasedflow = " + materialCategory.getNcategorybasedflow() + ",nactivestatus = "
						+ materialCategory.getNactivestatus() + ",needSectionwise="
						+ materialCategory.getNeedSectionwise() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcatcode="
						+ materialCategory.getNmaterialcatcode();

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(materialCategory);
				listBeforeUpdate.add(objMaterialCategory);
				multilingualIDList.add("IDS_EDITMATERIALCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getMaterialCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedMaterialCategoryList = new ArrayList<>();
		final MaterialCategory materialCategoryByID = getActiveMaterialCategoryById(
				materialCategory.getNmaterialcatcode(), userInfo);
		if (materialCategoryByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "Select 'IDS_SUPPLIERMATRIX' as Msg from suppliermatrix Where ncategorycode = "
					+ materialCategory.getNmaterialcatcode() + " and ntypecode=2 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+ userInfo.getNmastersitecode()
					+ " union all select 'IDS_STUDYPLANTEMPLATE' as Msg from treetemplatemaster tt,treeversiontemplate tv  Where tt.ncategorycode ="
					+ materialCategory.getNmaterialcatcode() + " and tt.nformcode =" + userInfo.getNformcode()
					+ " and  tt.ntemplatecode=tv.ntemplatecode  and  tt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and" + " tv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and tt.nsitecode="+ userInfo.getNmastersitecode()
					+ " and tv.nsitecode="+ userInfo.getNmastersitecode()
					+ " union all select 'IDS_MATERIAL' as Msg from material m, materialcategory mc Where m.nmaterialcatcode ="
					+ materialCategory.getNmaterialcatcode()
					+ " and m.nmaterialcatcode = mc.nmaterialcatcode and m.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nsitecode="+ userInfo.getNmastersitecode();

			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport
						.validateDeleteRecord(Integer.toString(materialCategory.getNmaterialcatcode()), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update materialcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nmaterialcatcode="
						+ materialCategory.getNmaterialcatcode();
				jdbcTemplate.execute(updateQueryString);
				materialCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedMaterialCategoryList.add(materialCategory);
				multilingualIDList.add("IDS_DELETEMATERIALCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(savedMaterialCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getMaterialCategory(userInfo);

			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getMaterialType(UserInfo userInfo) throws Exception {
		final String strQuery = "select m.nmaterialtypecode,coalesce(m.jsondata->'smaterialtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "m.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename,m.jsondata||json_build_object('nmaterialtypecode',nmaterialtypecode)"
				+ " ::jsonb as jsondata," + "m.ndefaultstatus from materialtype m where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and m.nmaterialtypecode > 0 and m.nmaterialtypecode!=5";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new MaterialType()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialCategoryByType(final short materialTypeCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select * from materialcategory " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialtypecode = "
				+ materialTypeCode+ " and nsitecode="+ userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new MaterialCategory()), HttpStatus.OK);
	}

}
