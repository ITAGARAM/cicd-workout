package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeTemplate;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BarcodeTemplateDAOImpl implements BarcodeTemplateDAO {

	//private static final Logger LOGGER = LoggerFactory.getLogger(BarcodeTemplateDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getBarcodeTemplate(Integer nbarcodetemplatecode, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();
		BarcodeTemplate objBarcodeTemplate = null;
		if (nbarcodetemplatecode == null) {
			final String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "' sformname,cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
					+ "' scontrolname,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'  sdisplayname ," + " t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'  sneedconfiguration , " + "t2.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'  sbarcodeprint ," + " t3.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'  ssqlqueryneed ," + " t4.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' sfiltersqlqueryneed,"
					+ " sql.ssqlqueryname  from barcodetemplate bt,qualisforms q,controlmaster cm,transactionstatus t,"
					+ "transactionstatus t1,transactionstatus t2,transactionstatus t3,transactionstatus t4,sqlquery sql "
					+ " where t.ntranscode=bt.ntransactionstatus and  cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nformcode=bt.nformcode "
					+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
					+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint'as boolean)  then 3 else 4 end "
					+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed'as boolean)  then 3 else 4 end "
					+ " and t4.ntranscode=case when cast (bt.jsondata->>'nfiltersqlqueryneed'as boolean)  then 3 else 4 end "
					+ " and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) and bt.nsitecode="+userInfo.getNmastersitecode()
					+ " order by bt.nbarcodetemplatecode desc ";

			List<BarcodeTemplate> lstBarcodeTemplate = jdbcTemplate.query(query, new BarcodeTemplate());
			objMap.put("BarcodeTemplate", lstBarcodeTemplate);

			if (lstBarcodeTemplate.isEmpty()) {
				objMap.put("SelectedBarcodeTemplate", null);
			} else {
				objBarcodeTemplate = lstBarcodeTemplate.get(0);
				objMap.put("SelectedBarcodeTemplate", objBarcodeTemplate);
			}

		} else {

			final String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "' sformname,cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
					+ "' scontrolname,t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'  sdisplayname ," + " t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'  sneedconfiguration , " + "t2.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'  sbarcodeprint ," + " t3.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'  ssqlqueryneed ," + " t4.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' sfiltersqlqueryneed,"
					+ " sql.ssqlqueryname  from barcodetemplate bt,qualisforms q,controlmaster cm,transactionstatus t,"
					+ "transactionstatus t1,transactionstatus t2,transactionstatus t3,transactionstatus t4,sqlquery sql "
					+ " where t.ntranscode=bt.ntransactionstatus and  cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nformcode=bt.nformcode "
					+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
					+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint' as boolean)  then 3 else 4 end "
					+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed' as boolean)  then 3 else 4 end "
					+ " and t4.ntranscode=case when cast (bt.jsondata->>'nfiltersqlqueryneed' as boolean)  then 3 else 4 end "
					+ " and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) "
					+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" and bt.nbarcodetemplatecode=" + nbarcodetemplatecode
					+ " order by bt.nbarcodetemplatecode desc ";

			BarcodeTemplate lstBarcodeTemplate = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query,
					BarcodeTemplate.class, jdbcTemplate);
			objMap.put("SelectedBarcodeTemplate", lstBarcodeTemplate);
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBarcodeTemplateModal(UserInfo userInfo) {
		Map<String, Object> objMap = new HashMap<String, Object>();
		String query = "select max(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "') sformname,q.nformcode from  qualisforms q,barcodecontrolconfig b where  q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode>0 and b.nformcode=q.nformcode" + " and b.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by q.nformcode  order by q.nformcode desc";

		List<BarcodeTemplate> lstBarcodeTemplate = jdbcTemplate.query(query, new BarcodeTemplate());

		objMap.put("QualisForm", lstBarcodeTemplate);

		query = "select * from   sqlquery q where  q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nsitecode="+userInfo.getNmastersitecode()+" and q.nquerytypecode="
				+ Enumeration.QueryType.BARCODE.getQuerytype() + "  order by 1 desc";

		List<SQLQuery> lstSQLQuery = jdbcTemplate.query(query, new SQLQuery());

		objMap.put("SqlQuery", lstSQLQuery);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBarcodeTemplateControl(Integer nformcode, UserInfo userInfo) {
		Map<String, Object> objMap = new HashMap<String, Object>();

		final String query = "select q.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "' sdisplayname,q.nformcode,q.ncontrolcode,qc.stablename,qc.sprimarykeyname stableprimarykeyname,qc.jnumericcolumns,qt.nquerybuildertablecode,bc.nisdynamic "
				+ " from  controlmaster q,sitecontrolmaster st,barcodecontrolconfig bc,"
				+ " querybuildertables qt,querybuildertablecolumns qc where " + " q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "   and q.nformcode=st.nformcode and q.ncontrolcode=st.ncontrolcode and st.nformcode=" + nformcode
				+ " and nisbarcodecontrol=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and bc.nformcode=" + nformcode + " and bc.nformcode=q.nformcode and q.ncontrolcode=bc.ncontrolcode"
				+ " and qt.nquerybuildertablecode=bc.nquerybuildertablecode and qt.nquerybuildertablecode=qc.nquerybuildertablecode"
				+ " order by q.nformcode desc";

		List<BarcodeTemplate> lstControl = jdbcTemplate.query(query, new BarcodeTemplate());

		objMap.put("controlList", lstControl);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table barcodetemplate "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final String bc = "select * from barcodetemplate" + " where ncontrolcode=" + barcodeTemplate.getNcontrolcode()
				+ " and nsitecode="+userInfo.getNmastersitecode()+" and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		BarcodeTemplate lst = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(bc, BarcodeTemplate.class,
				jdbcTemplate);

		if (lst == null) {

			final String getSequenceNo = "select nsequenceno+1 from seqnobasemaster where stablename ='barcodetemplate'";
			final int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);

			final String insertRegTemplate = "insert into barcodetemplate (nbarcodetemplatecode,nformcode,ncontrolcode,"
					+ "stableprimarykeyname,stablename,jsondata,nstatus,nsitecode,dmodifieddate,nquerybuildertablecode,ntransactionstatus) values ("
					+ seqNo + "," + barcodeTemplate.getNformcode() + "," + barcodeTemplate.getNcontrolcode() + ",N'"
					+ stringUtilityFunction.replaceQuote(barcodeTemplate.getStableprimarykeyname()) + "','"
					+ stringUtilityFunction.replaceQuote(barcodeTemplate.getStablename()) + "','"
					+ stringUtilityFunction.replaceQuote(barcodeTemplate.getJsonstring()) + "'::jsonb,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ barcodeTemplate.getNquerybuildertablecode() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertRegTemplate);
			String updatequery = "update seqnobasemaster set nsequenceno =" + seqNo
					+ " where stablename='barcodetemplate'";
			jdbcTemplate.execute(updatequery);
			barcodeTemplate.setNbarcodetemplatecode(seqNo);
			final List<Object> savedManufacturerList = new ArrayList<>();
			savedManufacturerList.add(barcodeTemplate);
			auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null,
					Arrays.asList("IDS_ADDBARCODETEMPLATE"), userInfo);
			return getBarcodeTemplate(null, userInfo);
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_ALREADYCONFIGURED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getEditBarcodeTemplateModal(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		Integer nbarcodetemplatecode = (Integer) inputMap.get("nbarcodetemplatecode");

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "' scontrolname,qt.jnumericcolumns,sql.ssqlqueryname,sql.nsqlquerycode "
				+ " from barcodetemplate bt,qualisforms q,controlmaster cm,querybuildertablecolumns qt ,sqlquery sql"
				+ " where sql.nsqlquerycode=(bt.jsondata->>'nsqlquerycode')::Integer and cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and qt.nquerybuildertablecode=bt.nquerybuildertablecode and bt.nbarcodetemplatecode="
				+ nbarcodetemplatecode + " order by bt.nbarcodetemplatecode desc";

		BarcodeTemplate bc = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
				jdbcTemplate);

		if (bc != null && bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			query = "select * from   sqlquery q where  q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nsitecode="+userInfo.getNmastersitecode()+" and q.nquerytypecode="
					+ Enumeration.QueryType.BARCODE.getQuerytype() + "  order by 1 desc";

			List<SQLQuery> lstSQLQuery = jdbcTemplate.query(query, new SQLQuery());

			objMap.put("SqlQuery", lstSQLQuery);

			query = "select q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "' sformname,q.nformcode from  " + " qualisforms q where  q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and q.nformcode>0  order by q.nformcode desc";

			List<BarcodeTemplate> lstBarcodeTemplate = jdbcTemplate.query(query, new BarcodeTemplate());

			objMap.put("QualisForm", lstBarcodeTemplate);

			if (!lstBarcodeTemplate.isEmpty()) {

				query = "select q.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
						+ "' sdisplayname,q.nformcode,q.ncontrolcode,qc.stablename,qc.sprimarykeyname stableprimarykeyname,qc.jnumericcolumns,qt.nquerybuildertablecode,bc.nisdynamic "
						+ " from  controlmaster q,sitecontrolmaster st,barcodecontrolconfig bc,"
						+ " querybuildertables qt,querybuildertablecolumns qc where " + " q.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "   and q.nformcode=st.nformcode and q.ncontrolcode=st.ncontrolcode and st.nformcode="
						+ bc.getNformcode() + " and nisbarcodecontrol="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and bc.nformcode="
						+ bc.getNformcode() + " and bc.nformcode=q.nformcode and q.ncontrolcode=bc.ncontrolcode"
						+ " and qt.nquerybuildertablecode=bc.nquerybuildertablecode and qt.nquerybuildertablecode=qc.nquerybuildertablecode"
						+ " order by q.nformcode desc";

				List<BarcodeTemplate> lstControl = jdbcTemplate.query(query, new BarcodeTemplate());

				objMap.put("controlList", lstControl);

			} else {
				objMap.put("controlList", lstBarcodeTemplate);

			}

		} else {

			if (bc == null)
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			else
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
		}
		objMap.put("SelectedBarcodeTemplate", bc);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)
			throws Exception {

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodetemplate bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" and q.nformcode=bt.nformcode and bt.nbarcodetemplatecode="
				+ barcodeTemplate.getNbarcodetemplatecode() + " order by bt.nbarcodetemplatecode desc";

		BarcodeTemplate bc = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
				jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				query = "select bt.*" + " from barcodetemplate bt  where bt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  bt.nbarcodetemplatecode !=" + barcodeTemplate.getNbarcodetemplatecode()
						+ " and ncontrolcode=" + barcodeTemplate.getNcontrolcode() + " and bt.nsitecode="+userInfo.getNmastersitecode()+" and bt.ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " order by bt.nbarcodetemplatecode desc";

				BarcodeTemplate bc1 = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
						jdbcTemplate);

				if (bc1 == null) {

					String update = "update barcodetemplate set nformcode=" + bc.getNformcode() + ",ncontrolcode="
							+ bc.getNcontrolcode() + " ,nquerybuildertablecode=" + bc.getNquerybuildertablecode()
							+ ",jsondata='" + stringUtilityFunction.replaceQuote(barcodeTemplate.getJsonstring())
							+ "'::jsonb where nbarcodetemplatecode=" + barcodeTemplate.getNbarcodetemplatecode();

					jdbcTemplate.execute(update);

				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(commonFunction
						.getMultilingualMessage("IDS_TEMPLATEALREADYCONFIGURED ", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeTemplate(barcodeTemplate.getNbarcodetemplatecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> approveBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)
			throws Exception {

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodetemplate bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nsitecode="+userInfo.getNmastersitecode()+" and bt.nbarcodetemplatecode="
				+ barcodeTemplate.getNbarcodetemplatecode() + " order by bt.nbarcodetemplatecode desc";

		BarcodeTemplate bc = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
				jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String update = "update barcodetemplate set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " where nbarcodetemplatecode="
						+ barcodeTemplate.getNbarcodetemplatecode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeTemplate(barcodeTemplate.getNbarcodetemplatecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> retireBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)
			throws Exception {

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodetemplate bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nsitecode="+userInfo.getNmastersitecode()+" and bt.nbarcodetemplatecode="
				+ barcodeTemplate.getNbarcodetemplatecode() + " order by bt.nbarcodetemplatecode desc";

		BarcodeTemplate bc = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
				jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

				String update = "update barcodetemplate set ntransactionstatus="
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " where nbarcodetemplatecode="
						+ barcodeTemplate.getNbarcodetemplatecode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeTemplate(barcodeTemplate.getNbarcodetemplatecode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)
			throws Exception {

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodetemplate bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nsitecode="+userInfo.getNmastersitecode()+" and bt.nbarcodetemplatecode="
				+ barcodeTemplate.getNbarcodetemplatecode() + " order by bt.nbarcodetemplatecode desc";

		BarcodeTemplate bc = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,
				jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String update = "update barcodetemplate set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nbarcodetemplatecode="
						+ barcodeTemplate.getNbarcodetemplatecode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeTemplate(null, userInfo);
	}

}
