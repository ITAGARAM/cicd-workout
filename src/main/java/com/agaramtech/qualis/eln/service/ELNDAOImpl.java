package com.agaramtech.qualis.eln.service;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.batchcreation.model.BatchHistory;
import com.agaramtech.qualis.configuration.model.LimsElnSiteMapping;
import com.agaramtech.qualis.eln.model.ELNOrders;
import com.agaramtech.qualis.eln.model.SDMSLabSheetParameter;
import com.agaramtech.qualis.eln.model.SDMSLabSheetTest;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ELNDAOImpl implements ELNDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ELNDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	@Override
	public ResponseEntity<Object> getLimsSDMSRecords(final String nSiteCode) throws Exception {
		final String siteqry = "select nlimssitecode,nelnsitecode from limselnsitemapping where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nelnsitecode in(" + nSiteCode + ")";
		final LimsElnSiteMapping objelnsite = (LimsElnSiteMapping) jdbcUtilityFunction.queryForObject(siteqry,
				LimsElnSiteMapping.class, jdbcTemplate);
		final String strQuery = "select sld.ntransactionresultcode as limsprimarycode,slm.suuid as groupid,slm.sarno,"
				+ " CONCAT(jrsa.ssamplearno,'-[',rt.ntestrepeatno,'][',rt.ntestretestno,']') ssamplearno,slm.ntestcode,tm.stestname,sld.ntestparametercode,"
				+ " tp.sparametername, "
				+ " rt.jsondata->>'stestsynonym' testsynonym,slm.nsitecode,slm.sllinterstatus ,"
				+ " CONCAT(tp.sparametername,' (',rt.jsondata->>'stestsynonym',')') as testparametersynonym,"
				+ " CONCAT(slm.sarno,'-',jrsa.ssamplearno,'-',rt.jsondata->>'stestsynonym') batchid,"
				+ " sld.ntransactiontestcode,sld.nbatchmastercode from sdmslabsheetmaster slm,sdmslabsheetdetails sld,"
				+ " registrationsamplearno jrsa,testmaster tm,testparameter tp,registrationtest rt "
				+ " where slm.suuid=sld.suuid and jrsa.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ " and tm.ntestcode = slm.ntestcode and tp.ntestparametercode=sld.ntestparametercode and rt.ntransactiontestcode=sld.ntransactiontestcode "
				+ " and slm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sld.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and jrsa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " slm.sllinterstatus ='A' and slm.nsitecode=" + objelnsite.getNlimssitecode();
		final List<ELNOrders> lstSDMSRecords = jdbcTemplate.query(strQuery, new ELNOrders());
		return new ResponseEntity<>(lstSDMSRecords, HttpStatus.OK);
	}

	@Override
	public String updateLimsSDMSInprogress(final String ntransactionresultcode) throws Exception {
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("update sdmslabsheetdetails set sllinterstatus='I' where  ntransactionresultcode in( "
					+ ntransactionresultcode + ");");
			sb.append(
					"update sdmslabsheetmaster set sllinterstatus='I' where suuid in (select suuid from sdmslabsheetdetails "
							+ " where ntransactionresultcode in( " + ntransactionresultcode + " ));");
			jdbcTemplate.execute(sb.toString());
			final String Str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			return Str;
		} catch (final Exception e) {
			e.printStackTrace();
			LOGGER.error("updateSdmsLabsheetInprogress Error:" + (e));
			final String Str = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			return Str;
		}

	}

	@Override
	public String updateLimsSDMSComplete(final Map<String, Object> inputMap) throws Exception {
		final List<Map<String, Object>> Objlstmap = (List<Map<String, Object>>) inputMap.get("lssampleresult");
		final Object jsonResultMap = inputMap.containsKey("jsondata") ? inputMap.get("jsondata") : null;
		try {
			String getUpdateSql = "";
			String Str = "";
			int i = 0;
			while (i < Objlstmap.size()) {
				String sresult = "";
				String ntransactionresultcode = "";
				String stransactionresultcode = "";
				ntransactionresultcode = String.valueOf(Objlstmap.get(i).get("limsprimarycode"));
				sresult = (String) Objlstmap.get(i).get("sresult");

				getUpdateSql += " update sdmslabsheetdetails set sresult=N'" + sresult
						+ "',sllinterstatus='C' where ntransactionresultcode=" + ntransactionresultcode + ";";
				stransactionresultcode += stransactionresultcode;
				i++;
			}
			Str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			jdbcTemplate.execute(getUpdateSql);
			if (jsonResultMap != null) {
				final String jsonResult = createLimsTestResults(inputMap);
				Str = jsonResult;
			}
			return Str;
		} catch (final Exception e) {
			e.printStackTrace();
			LOGGER.error("updateSdmsLabsheetInprogress Error:" + (e));
			final String Str = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			return Str;
		}
	}

	@Override
	public ResponseEntity<Object> getLimsTests() throws Exception {
		final String strQuery = "select t.ntestcode,t.ntestcategorycode,tc.stestcategoryname,t.stestname,t.stestsynonym,t.sdescription, "
				+ "	t.nstatus,t.ntransactionstatus,t.naccredited from testmaster t,testcategory tc where tc.ntestcategorycode=t.ntestcategorycode "
				+ " and t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SDMSLabSheetTest> lstTestRecords = jdbcTemplate.query(strQuery, new SDMSLabSheetTest());
		return new ResponseEntity<>(lstTestRecords, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getLimsTestParameters() throws Exception {
		final String strQuery = "select tp.ntestparametercode,tp.ntestcode,tp.nparametertypecode,tp.nunitcode,tp.sparametername,"
				+ " tp.sparametersynonym,tm.stestname,tm.stestsynonym from testparameter tp,testmaster tm where tm.ntestcode=tp.ntestcode "
				+ " and tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SDMSLabSheetParameter> lstTestParameterRecords = jdbcTemplate.query(strQuery,
				new SDMSLabSheetParameter());
		return new ResponseEntity<>(lstTestParameterRecords, HttpStatus.OK);
	}

	public String createLimsTestResults(final Map<String, Object> inputMap) throws Exception {
		final JSONArray jsonObject = new JSONArray(inputMap.get("jsondata").toString());
		final String str = "select * from batchhistory where sbatcharno='" + inputMap.get("batcharno") + "' "
				+ " and ntransactionstatus = " + Enumeration.TransactionStatus.INITIATED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final BatchHistory objBatchmaster = (BatchHistory) jdbcUtilityFunction.queryForObject(str, BatchHistory.class,
				jdbcTemplate);
		String Str = "";
		try {
			if (objBatchmaster != null) {
				final String strQuery = "insert into elnjsonresult (nbatchmastercode,ntestcode,jsondata,nsitecode,nstatus) values"
						+ " (" + objBatchmaster.getNbatchmastercode() + "," + inputMap.get("ntestcode") + "," + " '"
						+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "' ::jsonb ,"
						+ objBatchmaster.getNsitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ";
				jdbcTemplate.execute(strQuery);

				final String updateSeqno = "update seqnobatchcreation set nsequenceno="
						+ objBatchmaster.getNbatchmastercode() + " where stablename='elnjsonresult'";
				jdbcTemplate.execute(updateSeqno);
				Str = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			} else {
				Str = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			}
			return Str;

		} catch (final Exception e) {
			e.printStackTrace();
			LOGGER.error("ElnJsonResult Error:" + (e));
			Str = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			return Str;
		}
	}

	@Override
	public ResponseEntity<Object> getLimsInstrument(final int nsitecode) throws Exception {
		final String getInstrumentQuery = "select site ,instrumentcategory,instrumentid ,instrumentname,"
				+ " calibrationduedate ,maintenanceduedate,"
				+ " instrumentstatus ,validationstatus ,maintenancestatus ,calibrationstatus "
				+ " from view_eln_instrumenttransaction "
				+ " where ninstrumentsitecode = (select nlimssitecode from limselnsitemapping where nelnsitecode = "
				+ nsitecode + ") and calibrationstatuscode = "
				+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + ""
				+ " and maintenancestatuscode = " + Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()
				+ " and instrumentstatuscode = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<Object>(jdbcTemplate.queryForList(getInstrumentQuery), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getLimsMaterial(final int nsitecode) throws Exception {
		final String getMaterialget = "select materialinventorycode,materialtype ,materialcategory ,materialname ,inventoryid "
				+ " ,batchno ,lotno ,grade ,expirydatetime ,materiainventorystatus "
				+ " from view_eln_materialtransaction where materiainventorystatuscode = "
				+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				+ " and sitecode = (select nlimssitecode from limselnsitemapping where nelnsitecode = " + nsitecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") order by materialinventorycode;";
		return new ResponseEntity<Object>(jdbcTemplate.queryForList(getMaterialget), HttpStatus.OK);
	}

}
