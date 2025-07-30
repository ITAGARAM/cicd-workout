package com.agaramtech.qualis.release.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectMaster;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ReportTemplateDAOImpl {

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	public Map<String, Object> writeJSONTemplate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		String squery = "select r.nprojectmastercode from registration r,coachild cc,coaparent cp where cp.ncoaparentcode = cc.ncoaparentcode and "
				+ " cc.npreregno = r.npreregno and cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cp.ncoaparentcode = "
				+ inputMap.get("ncoaparentcode")+ " and cc.nsitecode = " + userInfo.getNtranssitecode() 
				+ " and cp.nsitecode = " + userInfo.getNtranssitecode()+ " and r.nsitecode = " + userInfo.getNtranssitecode()
				+ " LIMIT 1";
		int nprojectmastercode = jdbcTemplate.queryForObject(squery, Integer.class);

		squery = "select nusercode,sprojectname from projectmaster where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nprojectmastercode = "
				+ nprojectmastercode + " and nsitecode = " + userInfo.getNtranssitecode() + "";
		ProjectMaster pmaster = (ProjectMaster) jdbcUtilityFunction.queryForObject(squery, ProjectMaster.class,
				jdbcTemplate);

		squery = "select CONCAT(sfirstname,' ',slastname) as susername from users where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nusercode = "
				+ pmaster.getNusercode()+ " and " + " nsitecode = " + userInfo.getNmastersitecode();
		String sAuthorName = jdbcTemplate.queryForObject(squery, String.class);

		squery = "select sreportfieldvalue::int as nreporttemplatecode from reportinforelease where  nformcode = 172 and nparentmastercode = "
				+ nprojectmastercode + " " + "and lower(sreportfieldname) = lower('nreporttemplatecode') and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " ncoaparentcode = "
				+ inputMap.get("ncoaparentcode") + " and nsitecode = " + userInfo.getNtranssitecode()+ "";
		Integer nTemplateCode = jdbcTemplate.queryForObject(squery, Integer.class);

		squery = "select distinct acv.sviewname from approvalconfigversion acv,registrationarno rn, registration r,coachild cc,coaparent cp "
				+ " where cp.ncoaparentcode = cc.ncoaparentcode and cc.npreregno = r.npreregno and r.npreregno = rn.npreregno "
				+ " and rn.napprovalversioncode = acv.napproveconfversioncode and acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " rn.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and acv.nsitecode = " + userInfo.getNmastersitecode()+ " and rn.nsitecode = " + userInfo.getNtranssitecode()
				+ " and cc.nsitecode = " + userInfo.getNtranssitecode()+ " and r.nsitecode = " + userInfo.getNtranssitecode()
				+ " and cp.nsitecode = " + userInfo.getNtranssitecode()+ " and cp.ncoaparentcode = "
				+ inputMap.get("ncoaparentcode");
		String sViewName = jdbcTemplate.queryForObject(squery, String.class);

		squery = "SELECT to_char(CURRENT_DATE, 'ddth Month, yyyy') ReleaseDate;";
		String sReleaseDate = jdbcTemplate.queryForObject(squery, String.class);
		String sTempReleaseDate = sReleaseDate.substring(0, sReleaseDate.indexOf(","));
		String sTempYear = sReleaseDate.substring(sReleaseDate.indexOf(",") + 1);
		sReleaseDate = sTempReleaseDate.trim() + "," + sTempYear.trim();

		squery = "SELECT * FROM information_schema.columns WHERE table_name='" + sViewName + "' and column_name in"
				+ " ('Client Name','Supplier','Manufacturer','Product Name','Product Description','Catalogue Number','Batch Number','Pack Size','Number of Packs','Date of Receipt','sarno');";
		List<Map<String, Object>> lstViewColumns = jdbcTemplate.queryForList(squery);
		String sViewCols = "";
		if (lstViewColumns != null && lstViewColumns.size() > 0) {
			for (int n = 0; n < lstViewColumns.size(); n++) {
				if (n == lstViewColumns.size() - 1) {
					sViewCols += "max(cpv.\"" + lstViewColumns.get(n).get("column_name") + "\") \""
							+ lstViewColumns.get(n).get("column_name") + "\"";
				} else {
					sViewCols += "max(cpv.\"" + lstViewColumns.get(n).get("column_name") + "\") \""
							+ lstViewColumns.get(n).get("column_name") + "\",";
				}
			}
		}

		List<Map<String, Object>> lstView = null;
		if (sViewCols.length() > 0) {
			squery = "select cpv.npreregno," + sViewCols + " from " + sViewName + " cpv,coachild cc where "
					+ "cc.npreregno = cpv.npreregno and cc.ntransactiontestcode = cpv.ntransactiontestcode and "
					+ " cc.ntransactionsamplecode = cpv.ntransactionsamplecode and cc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cc.ncoaparentcode = "
					+ inputMap.get("ncoaparentcode")+ " and cc.nsitecode = " + userInfo.getNtranssitecode() + " group by cpv.npreregno ";
			lstView = jdbcTemplate.queryForList(squery);
		}

		squery = "select max(csa.saddress1) saddress1,max(csa.saddress2) saddress2,max(csa.saddress3) saddress3 "
				+ "from clientsiteaddress csa," + sViewName + " cpv,coachild cc "
				+ "where (cpv.jsondata->'Client Site'->'nclientsitecode')::int = csa.nclientsitecode and "
				+ "cc.npreregno = cpv.npreregno and cc.ntransactiontestcode = cpv.ntransactiontestcode and "
				+ "cc.ntransactionsamplecode = cpv.ntransactionsamplecode and cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and cc.nsitecode = " + userInfo.getNtranssitecode()
				+ " and csa.nsitecode = " + userInfo.getNmastersitecode()+ " and " + " csa.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "cc.ncoaparentcode = "
				+ inputMap.get("ncoaparentcode") + " group by cpv.npreregno";
		List<Map<String, Object>> lstClientAddress = jdbcTemplate.queryForList(squery);

		String sClientAddress = "";
		String output = "";
		if (lstClientAddress != null && lstClientAddress.size() > 0) {
			for (int n = 0; n < lstClientAddress.size(); n++) {
				output = ((String) lstClientAddress.get(n).get("saddress1") != null
						&& !((String) lstClientAddress.get(n).get("saddress1")).equals(""))
								? (String) lstClientAddress.get(n).get("saddress1")
								: "";
				sClientAddress = output.length() > 0 ? output + "<br/>" : "";
				output = ((String) lstClientAddress.get(n).get("saddress2") != null
						&& !((String) lstClientAddress.get(n).get("saddress2")).equals(""))
								? (String) lstClientAddress.get(n).get("saddress2")
								: "";
				sClientAddress += output.length() > 0 ? output + "<br/>" : "";
				output = ((String) lstClientAddress.get(n).get("saddress3") != null
						&& !((String) lstClientAddress.get(n).get("saddress3")).equals(""))
								? (String) lstClientAddress.get(n).get("saddress3")
								: "";
				sClientAddress += output.length() > 0 ? output + "<br/><br/>" : "";
			}

		}

		//squery = "select cpv.npreregno ,cci.semail " + "from clientcontactinfo cci," + sViewName + " cpv,coachild cc "
		squery = "select cci.semail " + "from clientcontactinfo cci," + sViewName + " cpv,coachild cc "
				+ "where (cpv.jsondata->'Client Site'->'nclientsitecode')::int = cci.nclientsitecode and "
				+ "cc.npreregno = cpv.npreregno and cc.ntransactiontestcode = cpv.ntransactiontestcode and "
				+ "cc.ntransactionsamplecode = cpv.ntransactionsamplecode and cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and cci.nsitecode = " + userInfo.getNmastersitecode()
				+ " and cc.nsitecode = " + userInfo.getNtranssitecode()+ " and " + " cci.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "cc.ncoaparentcode = "
				//+ inputMap.get("ncoaparentcode") + " group by cpv.npreregno,cci.semail;";
				+ inputMap.get("ncoaparentcode") + " group by cci.semail;";
		List<Map<String, Object>> lstClientEmail = jdbcTemplate.queryForList(squery);

		if (lstClientEmail != null && lstClientEmail.size() > 0) {
			for (int n = 0; n < lstClientEmail.size(); n++) {
				output = ((String) lstClientEmail.get(n).get("semail") != null
						&& !((String) lstClientEmail.get(n).get("semail")).equals(""))
								? (String) lstClientEmail.get(n).get("semail")
								: "";
				sClientAddress += output.length() > 0 ? output + "<br/>" : "";

			}

		}

		squery = "select to_char(min(rth.dtransactiondate),'yyyy-MM-dd HH:mi:ss') min,to_char(max(rth.dtransactiondate),'yyyy-MM-dd HH:mi:ss') max from registrationtesthistory rth,coachild cc,coaparent cp where cp.ncoaparentcode = cc.ncoaparentcode and "
				+ "cc.npreregno = rth.npreregno and rth.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and rth.nsitecode = " + userInfo.getNtranssitecode()+ " and cp.nsitecode = " + userInfo.getNtranssitecode()
				+ " and cc.nsitecode = " + userInfo.getNtranssitecode()+ " and " + " cp.ncoaparentcode = "
				+ inputMap.get("ncoaparentcode") + " and rth.ntransactionstatus in("
				+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ");";

		Map<String, Object> objMap = jdbcTemplate.queryForList(squery).get(0);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatterDate = new SimpleDateFormat("dd MMMM yyyy");
		final String sDateInitiate = (String) formatterDate.format((Date) dateFormat.parse((String) objMap.get("min")));
		final String sDateComplete = (String) formatterDate.format((Date) dateFormat.parse((String) objMap.get("max")));

		squery = "select coalesce(rh.jreportheader->'sheader'->>'" + userInfo.getSlanguagetypecode()
				+ "', rh.jreportheader->'sheader'->>'en-US') as sheader, rh.nreportheadercode, rh.jreportheader, rh.nsorter, rh.dmodifieddate, rh.nsitecode, rh.nstatus, rthc.nsorter sort "
				+ "from reportheader rh,reporttemplateheaderconfig rthc,reporttemplate rt "
				+ "where rt.nreporttemplatecode = rthc.nreporttemplatecode and rthc.nreportheadercode = rh.nreportheadercode and "
				+ " rh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rthc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rh.nsitecode = " + userInfo.getNmastersitecode()+ " and rthc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rt.nreporttemplatecode = " + nTemplateCode + " order by rthc.nsorter";

		List<Map<String, Object>> lstTestComments = jdbcTemplate.queryForList(squery);

		squery = "select * from view_generaltemplate where ncoaparentcode =" + inputMap.get("ncoaparentcode")
				+ " order by ntestcode";
		List<Map<String, Object>> lstGeneral = jdbcTemplate.queryForList(squery);

		String sTitleLogo = lstGeneral.stream()
				.anyMatch(map -> ((String) map.get("saccredited")).toLowerCase().equals("yes")) == true ? "Yes" : "No";

		squery = "select nreportsettingcode, ssettingname, ssettingvalue, dmodifieddate, nisvisible, nstatus from reportsettings where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nreportsettingcode in (8,9,10,14,17) order by nreportsettingcode;";
		List<Map<String, Object>> lstTitle = jdbcTemplate.queryForList(squery);

		String sImageURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

		squery = "select replace(ssettingvalue collate \"default\",'/','\\\\') ssettingvalue1,ssettingvalue from settings where nsettingcode = 6 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		Map<String, Object> objMapSettings = jdbcTemplate.queryForList(squery).get(0);

		String sRootDir = System.getProperty("user.dir");
		sRootDir = sRootDir + "\\webapps\\ROOT" + objMapSettings.get("ssettingvalue1");

		sImageURL = sImageURL.substring(0, sImageURL.lastIndexOf("/")) + objMapSettings.get("ssettingvalue");

		squery = "select crh.nversionno,CONCAT(u.sfirstname,' ',u.slastname) as Author from coareporthistory crh,users u "
				+ " where crh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " crh.nusercode = u.nusercode and ncoareporthistorycode = ( "
				+ " select max(ncoareporthistorycode) from coareporthistory where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncoaparentcode =  "
				+ inputMap.get("ncoaparentcode") + " and nsitecode = " + userInfo.getNtranssitecode() + ")"
				+ " and crh.nsitecode = " + userInfo.getNtranssitecode()+ " and u.nsitecode = " + userInfo.getNmastersitecode();
		List<Map<String, Object>> lstRevisionAuthor = jdbcTemplate.queryForList(squery);
		Integer nrev = lstRevisionAuthor.isEmpty() ? 0 : (Integer) lstRevisionAuthor.get(0).get("nversionno");
		String sRevision = nrev == 0 ? "1" : String.valueOf(nrev + 1);
		String sRevisionAuthor = lstRevisionAuthor.isEmpty() ? "-"
				: (String) lstRevisionAuthor.get(0).get("Author") == null
						|| (String) lstRevisionAuthor.get(0).get("Author") == "" ? "-"
								: (String) lstRevisionAuthor.get(0).get("Author");
		if (nTemplateCode == 2 || nTemplateCode == 3) {
			
			squery = "select coalesce(jsondata->>'ssystemfilename','') ssystemfilename from releasetestattachment where ncoaparentcode = "
					+ inputMap.get("ncoaparentcode") + " and (jsondata->'nneedreport'):: INT = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and nsitecode = " + userInfo.getNtranssitecode()+"";
			List<Map<String, Object>> lstSysFile = jdbcTemplate.queryForList(squery);

			File oSysFile = null;
			String sSysFile = "";

			for (int nlen = 0; nlen < lstSysFile.size(); nlen++) {
				sSysFile = (String) lstSysFile.get(nlen).get("ssystemfilename");
				oSysFile = new File(sRootDir + sSysFile);
				if (!oSysFile.exists()) {
					// ALPD-3631 added by Saravanan ReportTemplate java file.
					ftpUtilityFunction.FileViewUsingFtp(oSysFile.getName(), 0, userInfo, sRootDir, "");
				}
			}
		}

		String scurrentdir = (String) lstTitle.get(4).get("ssettingvalue");
		BufferedWriter myWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(scurrentdir), "UTF-8"));
		output = "";
		String sTempStandard = "";

		if (lstTestComments != null && lstTestComments.size() > 0) {
			myWriter.write("{\n \"headerlabel\" : \"" + pmaster.getSprojectname() + " Report\", \n");
			myWriter.write(
					" \"headervalue\" : \"Report No : " + (String) lstGeneral.get(0).get("sreportNo") + "\", \n");
			
			myWriter.write(" \"footerlabel\" : \"Revision : " + sRevision + "\", \n");
			myWriter.write(" \"titlelogo\" : \"" + sTitleLogo + "\", \n");
			myWriter.write(" \"reportTitle01\" : \"" + (String) lstTitle.get(0).get("ssettingvalue") + "\", \n");
			myWriter.write(" \"reportTitle02\" : \"Test Report\", \n");
			myWriter.write(" \"reportTitle03\" : \"" + pmaster.getSprojectname() + " Report\", \n");
			myWriter.write(" \"reportNo\" : \"" + (String) lstGeneral.get(0).get("sreportNo") + "\",  \n");
			myWriter.write(" \"reportDate\" : \"" + sReleaseDate + "\",  \n");
			myWriter.write(" \"Authors\" : \"" + sAuthorName + "\",  \n");
			myWriter.write(" \"Revision Template Version\" : \""
					+ (String) lstGeneral.get(0).get("sreporttemplateversion") + "\",\n");
			
			myWriter.write(" \"Revision\" : \"" + sRevision + "\",  \n");
			myWriter.write(" \"Revision date\" : \"" + sReleaseDate + "\",  \n");
			
			myWriter.write(" \"Revision Author\" : \"" + sRevisionAuthor + "\",  \n");

			myWriter.write(" \"address\" : \"" + (String) lstTitle.get(1).get("ssettingvalue") + "\",  \n");

			myWriter.write(" \"Web\" : \"" + (String) lstTitle.get(3).get("ssettingvalue") + "\",  \n");
			String sEmail = (String) lstTitle.get(2).get("ssettingvalue");
			String[] sarrEmail = sEmail.split(";");

			if (sarrEmail != null && sarrEmail.length > 0) {
				myWriter.write(" \"Email\" : \"" + sarrEmail[0] + "\",  \n");
				myWriter.write(" \"Tel\" : \"" + sarrEmail[1] + "\",  \n");
				myWriter.write(" \"Fax\" : \"" + sarrEmail[2] + "\",  \n");
			} else {
				myWriter.write(" \"Email\" : \"info@smtl.co.uk\",  \n");
				myWriter.write(" \"Tel\" : \"01656 752820\",  \n");
				myWriter.write(" \"Fax\" : \"01656 752830\",  \n");
			}

			myWriter.write(" \"Table01Name\" : [  \n");
			myWriter.write("{ \"slno\" : 1,  \n");
			myWriter.write(" \"label\" : \"" + (String) lstTestComments.get(0).get("sheader") + "\",  \n");
			String sValue = "";
			if (lstView == null) {
				sValue = "None";
			} else {
				sValue = (String) lstView.get(0).get("Client Name") != null ? (String) lstView.get(0).get("Client Name")
						: "None";
			}
			String sValue1 = sClientAddress;
			if (sValue1.length() > 0) {
				BufferedReader reader = new BufferedReader(new StringReader(sValue1));
				output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
			} else {
				output = sValue1;
			}

			myWriter.write(" \"Value\" : \"" + sValue + "\\n" + output + "\"  \n");
			myWriter.write("},\n");
			myWriter.write("{ \"slno\" : 2,  \n");
			myWriter.write(" \"label\" : \"" + (String) lstTestComments.get(1).get("sheader") + "\",  \n");
			sTempStandard = (String) lstGeneral.get(0).get("sintroduction");
			if (sTempStandard.length() > 0) {
				BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
				output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
			} else {
				output = sTempStandard;
			}
			myWriter.write(" \"Value\" : \"" + output + "\"  \n");
			myWriter.write("}\n],\n");
			myWriter.write(" \"Table02TestProducts\" : [  \n");
			myWriter.write("{ \"slno\" : 3,  \n");
			myWriter.write(" \"label\" : \"" + (String) lstTestComments.get(2).get("sheader") + "\",  \n");
			myWriter.write(" \"Value\" : \"\",  \n");
			myWriter.write(" \"Table2tab01\" : [{\"slno\" : \"\", \n");
			myWriter.write(" \"label\" : \"Table 1: " + (String) lstGeneral.get(0).get("stestproductheadercomments")
					+ "\", \n");
			myWriter.write(" \"Value\" : \"\",  \n");
			myWriter.write(" \"Table2tab01detail\" : [ \n");
			Integer nPackSize = 0;
			Integer nNoPacks = 0;
			if (lstView != null) {
				for (int k = 0; k < lstView.size(); k++) {
					myWriter.write("{\"slno\" : \"3.0.0.0.0\",\n");
					sValue = (String) lstView.get(k).get("Manufacturer") != null
							? (String) lstView.get(k).get("Manufacturer")
							: "-";
					
					myWriter.write(" \"Manufacturer\" : \"" + sValue + "\", \n");
					
					sValue = (String) lstView.get(k).get("Supplier") != null ? (String) lstView.get(k).get("Supplier")
							: "-";
					myWriter.write(" \"Supplier\" : \"" + sValue + "\", \n");
					sValue = (String) lstView.get(k).get("Product Name") != null
							? (String) lstView.get(k).get("Product Name")
							: "-";
					myWriter.write(" \"Product Name\" : \"" + sValue + "\", \n");
					
					sValue = (String) lstView.get(k).get("Product Description") != null
							? (String) lstView.get(k).get("Product Description")
							: "-";
					BufferedReader reader = new BufferedReader(new StringReader(sValue));
					sValue = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
					myWriter.write(" \"Description\" : \"" + sValue + "\", \n");
					sValue = (String) lstView.get(k).get("Catalogue Number") != null
							? (String) lstView.get(k).get("Catalogue Number")
							: "-";
					myWriter.write(" \"Catalogue Number\" : \"" + sValue + "\", \n");
		
					sValue = (String) lstView.get(k).get("Batch Number") != null
							? (String) lstView.get(k).get("Batch Number")
							: "-";
					myWriter.write(" \"Batch/Lot Number\" : \"" + sValue + "\", \n");

					sValue = ((String) lstView.get(k).get("Pack Size") != null
							&& !((String) lstView.get(k).get("Pack Size")).equals(""))
									? (String) lstView.get(k).get("Pack Size")
									: "0";
					nPackSize = Integer.parseInt(sValue);
					sValue = ((String) lstView.get(k).get("Number of Packs") != null
							&& !((String) lstView.get(k).get("Number of Packs")).equals(""))
									? (String) lstView.get(k).get("Number of Packs")
									: "0";
					nNoPacks = Integer.parseInt(sValue);
					nNoPacks = nPackSize * nNoPacks;

					myWriter.write(" \"Quantity\" : \"" + String.valueOf(nNoPacks) + "\", \n");

					sValue = (String) lstView.get(k).get("Date of Receipt") != null
							? (String) lstView.get(k).get("Date of Receipt")
							: "-";
					if (sValue.length() > 1) {
						sValue = sValue.substring(0, 10);
					}
					myWriter.write(" \"Date received\" : \"" + sValue + "\", \n");
					sValue = (String) lstView.get(k).get("sarno") != null ? (String) lstView.get(k).get("sarno") : "-";
					myWriter.write(" \"SMTLSampleID\" : \"" + sValue + "\" \n");

					if (k == lstView.size() - 1) {
						myWriter.write(" }] \n");
					} else {
						myWriter.write(" }, \n");
					}
				}
			} else {
				myWriter.write("{\"slno\" : \"3.0.0.0.0\",\n");
				myWriter.write(" \"Manufacturer\" : \"-\", \n");
				myWriter.write(" \"Supplier\" : \"-\", \n");
				myWriter.write(" \"Product Name\" : \"-\", \n");
				myWriter.write(" \"Description\" : \"-\", \n");
				myWriter.write(" \"Catalogue Number\" : \"-\", \n");
				myWriter.write(" \"Batch/Lot Number\" : \"-\", \n");
				myWriter.write(" \"Quantity\" : \"-\", \n");
				myWriter.write(" \"Date received\" : \"-\", \n");
				myWriter.write(" \"SMTLSampleID\" : \"-\" \n");
				myWriter.write(" }] \n");

			}
			myWriter.write(" }], \n");
			myWriter.write(" \"Table2notes\" : [   { \"slno\" : \"\", \n");
			myWriter.write(" \"label\" : \"NOTE:\", \n");
			myWriter.write(" \"Value\" : \"\", \n");
			sTempStandard = "";
			Boolean bNoteFlag = true;
			if (nTemplateCode == 3) {
				sTempStandard = (String) lstGeneral.get(0).get("stestproductfootercomments1");
				if (sTempStandard.length() > 0 && sTempStandard.toUpperCase().equals("NONE") == false) {
					myWriter.write(" \"Table2notesdetail\" :[{  \"slno\" : \"3.0.0.0.n1\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>" + sTempStandard + "</li></ul>" + "\" \n");
					myWriter.write(" } ");
					bNoteFlag = false;
				}
				sTempStandard = (String) lstGeneral.get(0).get("stestproductfootercomments2");
				if (sTempStandard.length() > 0 && sTempStandard.toUpperCase().equals("NONE") == false) {
					if (bNoteFlag == false) {
						myWriter.write(", \n");
						myWriter.write(" {   \"slno\" : \"3.0.0.0.n2\", \n");
						myWriter.write(" \"label\" : \"\", \n");
						myWriter.write(" \"Value\" : \"" + "<ul><li>"
								+ (String) lstGeneral.get(0).get("stestproductfootercomments2") + "</li></ul>"
								+ "\" \n");
						myWriter.write(" } \n");
						myWriter.write(" ] \n");
					} else {
						myWriter.write(" \"Table2notesdetail\" :[{  \"slno\" : \"3.0.0.0.n1\", \n");
						myWriter.write(" \"label\" : \"\", \n");
						myWriter.write(" \"Value\" : \"" + "<ul><li>" + sTempStandard + "</li></ul>" + "\" \n");
						myWriter.write(" } ");
						myWriter.write(" ] \n");
					}

				} else {
					if (bNoteFlag == false) {
						myWriter.write(" ] \n");
					}
				}
				myWriter.write(" }], \n");
				sTempStandard = "";
			} else {
				if (nTemplateCode == 4) {
					myWriter.write(" \"Table2notesdetail\" :[{  \"slno\" : \"3.0.0.0.n1\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>"
							+ (String) lstGeneral.get(0).get("stestproductfootercomments1") + "</li></ul>" + "\" \n");
					myWriter.write(" }, \n");
					myWriter.write(" {   \"slno\" : \"3.0.0.0.n2\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>"
							+ (String) lstGeneral.get(0).get("stestproductfootercomments2") + "</li></ul>" + "\" \n");
					myWriter.write(" }, \n");
					myWriter.write(" {   \"slno\" : \"3.0.0.0.n3\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>"
							+ "All samples were selected and supplied by the client." + "</li></ul>" + "\" \n");
					myWriter.write(" } \n");
					myWriter.write(" ] \n");
					myWriter.write(" }], \n");

				} else {
					myWriter.write(" \"Table2notesdetail\" :[{  \"slno\" : \"3.0.0.0.n1\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>"
							+ (String) lstGeneral.get(0).get("stestproductfootercomments1") + "</li></ul>" + "\" \n");
					myWriter.write(" }, \n");
					myWriter.write(" {   \"slno\" : \"3.0.0.0.n2\", \n");
					myWriter.write(" \"label\" : \"\", \n");
					myWriter.write(" \"Value\" : \"" + "<ul><li>"
							+ (String) lstGeneral.get(0).get("stestproductfootercomments2") + "</li></ul>" + "\" \n");
					myWriter.write(" } \n");
					myWriter.write(" ] \n");
					myWriter.write(" }], \n");
				}
			}

			myWriter.write(" \"Table2child\" :[    { \"slno\" : 3.1, \n");
			myWriter.write(" \"label\" : \"" + (String) lstTestComments.get(3).get("sheader") + "\", \n");
			myWriter.write(" \"Value\" : \"" + (String) lstGeneral.get(0).get("stestproductfootercomments3") + "\" \n");
			myWriter.write(" } \n");
			myWriter.write(" ] \n");
			myWriter.write(" } \n");
			myWriter.write(" ], \n");
			myWriter.write(" \"Table03DateofTesting\" :[  \n");
			myWriter.write(" {  \"slno\" : 4,  \n");
			myWriter.write("  \"label\" : \"" + (String) lstTestComments.get(4).get("sheader") + "\",  \n");
			myWriter.write("  \"Value\" : \"" + sDateInitiate + " to " + sDateComplete + "\"  \n");
			myWriter.write("  },  \n");
			myWriter.write(" {  \"slno\" : 5,  \n");
			myWriter.write("  \"label\" : \"" + (String) lstTestComments.get(5).get("sheader") + "\",  \n");
			String sProductFooter=(String) lstGeneral.get(0).get("stestproductfootercomments4");
			BufferedReader reader11 = new BufferedReader(new StringReader(sProductFooter)); 
			output = reader11.lines()
					.map(line -> line + "<br/>")
					.collect(Collectors.joining(""));
			//myWriter.write(" \"Value\" : \"" + (String) lstGeneral.get(0).get("stestproductfootercomments4") + "\" \n");
			myWriter.write(" \"Value\" : \""+output+"\" \n");
			myWriter.write(" }], \n");
			sTempStandard = "";
			if (nTemplateCode == 3) {
				sTempStandard = (String) lstGeneral.get(0).get("sintroduction");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}
			} else {
				output = "";
			}
			sTempStandard = "";
			myWriter.write(" \"Table04TestingDetails\":[\n" + "		{	\"slno\"  : 6,\n" + "			\"label\" : \""
					+ (String) lstTestComments.get(6).get("sheader") + "\",\n" + "			\"Value\" : \"" + output
					+ "\",\n" + "			\"Table04Detail\" :[");
			int nStdCnt = lstGeneral.size();

			for (int i = 0; i < lstGeneral.size(); i++) {
				myWriter.write("{\"slno\"  : 6." + Integer.toString(i + 1) + ",\n");
				sTitleLogo = (String) lstGeneral.get(i).get("saccredited");
				sTitleLogo = sTitleLogo.equals("Yes") ? " - UKAS" : " ";
				myWriter.write("\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + sTitleLogo + " \", \n");
				sTempStandard = (String) lstGeneral.get(i).get("sdescription");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Value\" : \"" + output + "\" \n");
				if (i == lstGeneral.size() - 1) {
					myWriter.write(" }] \n }],\n");
				} else {
					myWriter.write(" }, \n");
				}
			}
			myWriter.write("\"Table05Standards\":[\n");
			myWriter.write("{	\"slno\"  : 6." + Integer.toString(nStdCnt + 1) + ",\n" + "			\"label\" : \""
					+ (String) lstTestComments.get(7).get("sheader") + "\",\n" + "			\"Value\" : \"\",\n"
					+ "			\"Table5detail\":[\n");

			for (int i = 0; i < lstGeneral.size(); i++) {
				myWriter.write(
						"{\"slno\"  : \"6." + Integer.toString(nStdCnt + 1) + "." + Integer.toString(i + 1) + "\",\n");
				myWriter.write("\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + "\", \n");
				sTempStandard = (String) lstGeneral.get(i).get("smethodstandard");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}
				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				if (i == lstGeneral.size() - 1) {
					myWriter.write(" }] \n }],\n");
				} else {
					myWriter.write(" }, \n");
				}
			}
			nStdCnt = lstGeneral.size() + 2;
			myWriter.write(
					"\"Table06TestingConditions\":[			\n" + "		{	\"slno\"  : 6." + Integer.toString(nStdCnt)
							+ ",\n" + "		\"label\" : \"" + (String) lstTestComments.get(8).get("sheader") + "\",\n"
							+ "		\"Value\" : \"\",\n" + "		\"Table06child\" :[\n");

			for (int i = 0; i < lstGeneral.size(); i++) {
				myWriter.write(
						"{\"slno\"  : \"6." + Integer.toString(nStdCnt) + "." + Integer.toString(i + 1) + "\",\n");
				myWriter.write("\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + "\", \n");
				sTempStandard = (String) lstGeneral.get(i).get("stestcondition");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}
				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				if (i == lstGeneral.size() - 1) {
					myWriter.write(" }] \n }],\n");
				} else {
					myWriter.write(" }, \n");
				}
			}
			nStdCnt = lstGeneral.size() + 3;
			myWriter.write(
					"\"Table06Deviationcomments\":[			\n" + "{	\"slno\"  : 6." + Integer.toString(nStdCnt)
							+ ",\n" + "				\"label\" : \"" + (String) lstTestComments.get(9).get("sheader")
							+ "\",\n" + "				\"Value\" : \"\",\n" + "				\"Table06child\" :[\n");
			for (int i = 0; i < lstGeneral.size(); i++) {
				myWriter.write(
						"{\"slno\"  : \"6." + Integer.toString(nStdCnt) + "." + Integer.toString(i + 1) + "\",\n");
				myWriter.write("\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + "\", \n");
				sTempStandard = (String) lstGeneral.get(i).get("sdeviationcomments");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}
				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				if (i == lstGeneral.size() - 1) {
					myWriter.write(" }] \n  }],\n");
				} else {
					myWriter.write(" }, \n");
				}
			}
			if (nTemplateCode == 4) {
				nStdCnt = lstGeneral.size() + 4;

				myWriter.write("\"Table07SamplingDetails\":[\n");

				myWriter.write("{	\"slno\"  : \"6." + Integer.toString(nStdCnt) + "\", \n");
				myWriter.write("\"label\" : \"" + (String) lstTestComments.get(10).get("sheader") + "\", \n");

				sTempStandard = (String) lstGeneral.get(0).get("ssamplingdetails");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				myWriter.write(" }], \n");
			} else {
				nStdCnt = lstGeneral.size() + 4;

				sTempStandard = (String) lstGeneral.get(0).get("suncertainitymeasurement");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Table07Uncertainty\":[" + "{\"slno\"  : \"6." + Integer.toString(nStdCnt) + "\",\n"
						+ "	\"label\" : \"" + (String) lstTestComments.get(10).get("sheader") + "\",\n"
						+ "	\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\"\n");
				myWriter.write(" }, \n");
				myWriter.write("{	\"slno\"  : \"6." + Integer.toString(nStdCnt + 1) + "\", \n");
				myWriter.write("\"label\" : \"" + (String) lstTestComments.get(11).get("sheader") + "\", \n");

				sTempStandard = (String) lstGeneral.get(0).get("ssamplingdetails");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				myWriter.write(" }], \n");
			}
			if (nTemplateCode == 4) {
				myWriter.write("\"Table08Results\":[\n" + "{	\"slno\"  : \"7\", \n" + " \"label\" : \""
						+ (String) lstTestComments.get(11).get("sheader") + "\",\n");
			} else {
				myWriter.write("\"Table08Results\":[\n" + "{	\"slno\"  : \"7\", \n" + " \"label\" : \""
						+ (String) lstTestComments.get(12).get("sheader") + "\",\n");
			}
			Boolean bResultLabelFlag = false;
			Boolean bResultFlag = false;
			Integer nTempValue = 0;
			Integer nTempTestCnt=-1;
			for (int nr = 0; nr < lstGeneral.size(); nr++) {

				Integer nTestCode = (int) lstGeneral.get(nr).get("ntestcode");

				squery = "select vg.ntestcode,vg.stestname,e.jsondata "
						+ " from elnjsonresult e,view_generaltemplate vg,batchsample b,coachild cc "
						+ "where vg.ncoaparentcode = cc.ncoaparentcode and b.npreregno = cc.npreregno and "
						+ "b.ntransactionsamplecode = cc.ntransactionsamplecode and b.ntransactiontestcode = cc.ntransactiontestcode and "
						+ "vg.ntestcode = e.ntestcode "
						+ " and e.nbatchmastercode = b.nbatchmastercode and e.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "b.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and e.nsitecode = " + userInfo.getNtranssitecode()+ " and b.nsitecode = " + userInfo.getNtranssitecode()
						+ " and cc.nsitecode = " + userInfo.getNtranssitecode()
						+ " and " + "vg.ncoaparentcode = "
						+ inputMap.get("ncoaparentcode") + " " + " and vg.ntestcode = " + nTestCode + " "
						+ " group by vg.ntestcode,vg.stestname,e.jsondata";
				List<Map<String, Object>> lstELNResults = jdbcTemplate.queryForList(squery);

				String sJSONData = "";

				if (lstELNResults.size() > 0) {
					if (bResultLabelFlag == false) {
						myWriter.write(" \"Value\" : \"\", \n");
						bResultLabelFlag = true;

					}
					bResultFlag = true;
					nTempValue++;
					Boolean bKeyFlag = false;
					for (int r = 0; r < lstELNResults.size(); r++) {
						nTempTestCnt++;
						//if (nTempValue > 1) {
						if(nTempTestCnt>0) {
							//myWriter.write(",\"Table08Table0" + Integer.toString(nr) + "\" : \n [ \n {\"slno\"  : \"7."
									//+ Integer.toString(nr + 1) + "\",\n"
							myWriter.write(",\"Table08Table0"+Integer.toString(nTempTestCnt)+"\" : \n [ \n {\"slno\"  : \"7."+Integer.toString(nTempTestCnt+1)+"\",\n"
									+ "				 \"label\" : \""
									+ (String) lstELNResults.get(r).get("stestname") + "\",\n"
									+ "				 \"Value\" : \"\",\n");
						} else {
							//myWriter.write("\"Table08Table0" + Integer.toString(nr) + "\" : \n [ \n {\"slno\"  : \"7."
								//	+ Integer.toString(nr + 1) + "\",\n"
							myWriter.write("\"Table08Table0"+Integer.toString(nTempTestCnt)+"\" : \n [ \n {\"slno\"  : \"7."+Integer.toString(nTempTestCnt+1)+"\",\n"
									+ "				 \"label\" : \""
									+ (String) lstELNResults.get(r).get("stestname") + "\",\n"
									+ "				 \"Value\" : \"\",\n");
						}
						sJSONData = lstELNResults.get(r).get("jsondata").toString();
						bKeyFlag = false;
						squery = "select json_array_elements('" + sJSONData + "')";
						List<Map<String, Object>> lstJSON = jdbcTemplate.queryForList(squery);
						squery = "";

						for (int i = 0; i < lstJSON.size(); i++) {
							sJSONData = lstJSON.get(i).get("json_array_elements").toString();
							squery += "(WITH KEY_VALUE_ARRAY AS ("
									+ "SELECT string_to_array((select string_agg(param,',') as param from "
									+ "(SELECT json_array_elements_text((" + "select json_array_elements('[" + sJSONData
									+ "]'))) as param)a),',') AS arr" + ")"
									+ "SELECT jsonb_object_agg('KEY' || (index), value)"
									+ "FROM unnest((SELECT arr FROM KEY_VALUE_ARRAY)) WITH ORDINALITY AS t(value, index) where value != '' )";
							if (i < lstJSON.size() - 1) {
								squery += " union all ";
							}
						}

						List<Map<String, Object>> lstKey = jdbcTemplate.queryForList(squery);

						for (int m = 0; m < lstKey.size(); m++) {
							String sKeyData = "";
							if (lstKey.get(m).get("jsonb_object_agg") != null) {
								sKeyData = lstKey.get(m).get("jsonb_object_agg").toString();
							} else {
								sKeyData = "";
							}

							if (m == lstKey.size() - 1) {
								// ALPD-5633 SMTL Report > connection refused error while generating the report,
								// it occurs when generating the report with a test multiple parameters. occurs
								// for ELN with lims batch flow
								if (m == 0) {

									myWriter.write(" \"TableName\" : \"\", \n");
									//myWriter.write("\"Table08table0" + Integer.toString(nr) + "detail\" :[\n");
									myWriter.write("\"Table08table0"+Integer.toString(nTempTestCnt)+"detail\" :[\n");
									myWriter.write(sKeyData + "\n]");

								} else {

									myWriter.write(sKeyData + "\n]");

								}
							} else {
								if (m == 0) {
									if (sKeyData.length() > 0) {
										String[] strArray = sKeyData.split(",");
										String sTableName = "";
										String sTempTable = "";
										for (int i = 0; i < strArray.length; i++) {
											if (strArray[i].length() > 0) {
												if (i == strArray.length - 1) {
													sTempTable = strArray[i].substring(strArray[i].indexOf(":") + 3,
															strArray[i].length() - 2);
												} else {
													sTempTable = strArray[i].substring(strArray[i].indexOf(":") + 3,
															strArray[i].length() - 1);
												}
												sTableName = sTableName + " " + sTempTable;
											}
										}
										myWriter.write(" \"TableName\" : \"" + sTableName + "\", \n");
										//myWriter.write("\"Table08table0" + Integer.toString(nr) + "detail\" :[\n");
										myWriter.write("\"Table08table0"+Integer.toString(nTempTestCnt)+"detail\" :[\n");
									} else {
										myWriter.write(" \"TableName\" : \"\", \n");
										//myWriter.write("\"Table08table0" + Integer.toString(nr) + "detail\" :[\n");
										myWriter.write("\"Table08table0"+Integer.toString(nTempTestCnt)+"detail\" :[\n");
									}
								} else {
									if (sKeyData.length() > 0) {
										// ALPD-5633 SMTL Report > connection refused error while generating the report,
										// it occurs when generating the report with a test multiple parameters. occurs
										// for ELN with lims batch flow
										if (bKeyFlag == false) {
											myWriter.write(sKeyData + "\n");
											bKeyFlag = true;
										} else {
											myWriter.write("," + sKeyData + "\n");
										}
									}
								}
							}

						}

						if (nTemplateCode == 2 || nTemplateCode == 4) {
							nTestCode = (int) lstELNResults.get(r).get("ntestcode");

							squery = " select coalesce(rta.jsondata->>'ssystemfilename','') ssystemfilename,rs.sarno,rt.ntestcode, "
									+ " coalesce(rta.jsondata->>'sfilename','') sfilename,coalesce(rta.jsondata->>'sdescription','') sdescription "
									+ " from releasetestattachment rta,registrationarno rs, registrationtest rt "
									+ " where  rs.npreregno = rta.npreregno and rt.ntransactiontestcode = rta.ntransactiontestcode and rt.ntransactionsamplecode = rta.ntransactionsamplecode"
									+ " and rt.npreregno = rta.npreregno and (rta.jsondata->'nneedreport'):: INT = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and rta.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
									+ " rs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rta.nsitecode = " + userInfo.getNtranssitecode()+ " and rs.nsitecode = " + userInfo.getNtranssitecode()
									+ " and rt.nsitecode = " + userInfo.getNtranssitecode()
									+ " " + " and rt.ntestcode = " + nTestCode + " and rta.ncoaparentcode = "
									+ inputMap.get("ncoaparentcode") + " " + " order by rt.ntransactiontestcode;";

							List<Map<String, Object>> lstSample = jdbcTemplate.queryForList(squery);
							if (lstSample.size() > 0) {
								for (int jj = 0; jj < lstSample.size(); jj++) {
									if (jj == 0) {
										myWriter.write(",\"Table08sample0" + Integer.toString(nr) + "detail\" :[\n");
									}

									String sSysFileName = (String) lstSample.get(jj).get("ssystemfilename");
									sSysFileName = sImageURL + sSysFileName;
									String sFileName = (String) lstSample.get(jj).get("sfilename");
									String sDesc = (String) lstSample.get(jj).get("sdescription");
									myWriter.write("{\"slno\" : \"\",\n");
									if (jj == 0) {
										myWriter.write("\"notehead\" : \"NOTES:\",\n");
									}
									myWriter.write(
											"\"label\" : \"" + (String) lstSample.get(jj).get("sarno") + "\",\n");
									myWriter.write("\"Value\" : \"" + sSysFileName + "\",\n");
									myWriter.write("\"sFileName\" : \"" + sFileName + "\",\n");
									myWriter.write("\"sDesc\" : \"" + sDesc + "\"\n");
									if (jj == lstSample.size() - 1) {

										myWriter.write("\n}]\n}]");
									} else {
										myWriter.write("},\n");
									}
								}



							} else {
								myWriter.write("}]\n");
							}
						} else {
							myWriter.write("}]\n");
						}

					} 
				} else {
					if (nTemplateCode == 2 || nTemplateCode == 4) {

						nTestCode = (int) lstGeneral.get(nr).get("ntestcode");

						squery = " select coalesce(rta.jsondata->>'ssystemfilename','') ssystemfilename,rs.sarno,rt.ntestcode, "
								+ " coalesce(rta.jsondata->>'sfilename','') sfilename,coalesce(rta.jsondata->>'sdescription','') sdescription "
								+ " from releasetestattachment rta,registrationarno rs, registrationtest rt "
								+ " where  rs.npreregno = rta.npreregno and rt.ntransactiontestcode = rta.ntransactiontestcode and rt.ntransactionsamplecode = rta.ntransactionsamplecode"
								+ " and rt.npreregno = rta.npreregno and (rta.jsondata->'nneedreport'):: INT = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and rta.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
								+ " rs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and rta.nsitecode = " + userInfo.getNtranssitecode()+ " and rs.nsitecode = " + userInfo.getNtranssitecode()
								+ " and rt.nsitecode = " + userInfo.getNtranssitecode()
								+ " " + " and rt.ntestcode = " + nTestCode + " and rta.ncoaparentcode = "
								+ inputMap.get("ncoaparentcode") + " " + " order by rt.ntransactiontestcode;";

						List<Map<String, Object>> lstSample = jdbcTemplate.queryForList(squery);

						if (lstSample.size() > 0) {
							nTempValue++;
							if (bResultLabelFlag == false) {
								myWriter.write(" \"Value\" : \"\", \n");
								bResultLabelFlag = true;

							}
							bResultFlag = true;
							if (nTempValue > 1) {
								//myWriter.write(",\"Table08Table0" + Integer.toString(nr)
									//	+ "\" : \n [ \n {\"slno\"  : \"7." + Integer.toString(nr + 1) + "\",\n"
								myWriter.write(",\"Table08Table0"+Integer.toString(nTempTestCnt)+"\" : \n [ \n {\"slno\"  : \"7."+Integer.toString(nTempTestCnt+1)+"\",\n"
										//+ "				 \"label\" : \"" + (String) lstGeneral.get(nr).get("stestname")
										+ "				 \"label\" : \""+(String) lstGeneral.get(nTempTestCnt).get("stestname")+"\",\n"
										+ "\",\n" + "				 \"Value\" : \"\",\n");
							} else {
								//myWriter.write("\"Table08Table0" + Integer.toString(nr)
								//		+ "\" : \n [ \n {\"slno\"  : \"7." + Integer.toString(nr + 1) + "\",\n"
								myWriter.write("\"Table08Table0"+Integer.toString(nTempTestCnt)+"\" : \n [ \n {\"slno\"  : \"7."+Integer.toString(nTempTestCnt+1)+"\",\n"
										//+ "				 \"label\" : \"" + (String) lstGeneral.get(nr).get("stestname")
										+ "				 \"label\" : \""+(String) lstGeneral.get(nTempTestCnt).get("stestname")+"\",\n"
										+ "\",\n" + "				 \"Value\" : \"\",\n");
							}
							for (int jj = 0; jj < lstSample.size(); jj++) {
								if (jj == 0) {
									//myWriter.write("\"Table08sample0" + Integer.toString(nr) + "detail\" :[\n");
									myWriter.write("\"Table08sample0"+Integer.toString(nTempTestCnt)+"detail\" :[\n");
								}

								String sSysFileName = (String) lstSample.get(jj).get("ssystemfilename");
								sSysFileName = sImageURL + sSysFileName;
								String sFileName = (String) lstSample.get(jj).get("sfilename");
								String sDesc = (String) lstSample.get(jj).get("sdescription");
								myWriter.write("{\"slno\" : \"\",\n");
								if (jj == 0) {
									myWriter.write("\"notehead\" : \"NOTES:\",\n");
								}
								myWriter.write("\"label\" : \"" + (String) lstSample.get(jj).get("sarno") + "\",\n");
								myWriter.write("\"Value\" : \"" + sSysFileName + "\",\n");
								myWriter.write("\"sFileName\" : \"" + sFileName + "\",\n");
								myWriter.write("\"sDesc\" : \"" + sDesc + "\"\n");
								if (jj == lstSample.size() - 1) {
									myWriter.write("\n}]\n}]");

								} else {
									myWriter.write("},\n");
								}
							}

						}

					}
				}
			}

			if (bResultFlag == false) {
				myWriter.write(" \"Value\" : \"\" \n");
				myWriter.write("}],\n");
			} else {
				myWriter.write("\n}],");
			}
			Integer nSlno = 8;
			Integer nHeaderIndex = 13;
			Boolean bMonitorImageFlag = false;
			if (nTemplateCode == 3) {
				myWriter.write("\"Table08TrendGraph\":[\n" + "{	\"slno\"  : \"" + nSlno + "\", \n" + " \"label\" : \""
						+ (String) lstTestComments.get(13).get("sheader") + "\",\n");
				for (int i = 0; i < lstGeneral.size(); i++) {
					Integer nTestCode = (int) lstGeneral.get(i).get("ntestcode");

					squery = " select coalesce(rta.jsondata->>'ssystemfilename','') ssystemfilename,rs.sarno,rt.ntestcode "
							+ " from releasetestattachment rta,registrationarno rs, registrationtest rt "
							+ " where  rs.npreregno = rta.npreregno and rt.ntransactiontestcode = rta.ntransactiontestcode and rt.ntransactionsamplecode = rta.ntransactionsamplecode"
							+ " and rt.npreregno = rta.npreregno and (rta.jsondata->'nneedreport'):: INT = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and rta.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " rs.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and rta.nsitecode = " + userInfo.getNtranssitecode()+ " and rs.nsitecode = " + userInfo.getNtranssitecode()
							+ " and rt.nsitecode = " + userInfo.getNtranssitecode()+ " " + " and rt.ntestcode = "
							+ nTestCode + " and rta.ncoaparentcode = " + inputMap.get("ncoaparentcode") + " "
							+ " order by rt.ntransactiontestcode;";
					List<Map<String, Object>> lstSample = jdbcTemplate.queryForList(squery);
					if (lstSample.size() > 0) {
						myWriter.write("\"Table08Graph0" + Integer.toString(i) + "\" : \n [ \n {\"slno\"  : \"8."
								+ Integer.toString(i + 1) + "\",\n" + "				 \"label\" : \""
								+ (String) lstGeneral.get(i).get("stestname") + "\",\n"
								+ "				 \"Value\" : \"\",\n");

						myWriter.write("\"Table08Graph0" + Integer.toString(i) + "detail\" :[\n");
						bMonitorImageFlag = true;
						for (int jj = 0; jj < lstSample.size(); jj++) {
							String sSysFileName = (String) lstSample.get(jj).get("ssystemfilename");
							sSysFileName = sImageURL + sSysFileName;

							myWriter.write("{\"slno\" : \"\",\n");
							myWriter.write("\"label\" : \"\",\n");
							myWriter.write("\"Value\" : \"" + sSysFileName + "\"\n");
							if (jj == lstSample.size() - 1) {
								if (i == lstGeneral.size() - 1) {
									myWriter.write("\n}]\n}]");

								} else {
									myWriter.write("\n}]\n}],");
								}
							} else {
								myWriter.write("},\n");
							}
						}

						if (i == lstGeneral.size() - 1) {
							myWriter.write("\n}],");

						}
					}

				}
				nSlno += 1;
				nHeaderIndex += 1;
				if (bMonitorImageFlag == false) {
					myWriter.write("\n}],");
				}
			}

			if (nTemplateCode == 4) {
				nStdCnt = lstGeneral.size() + 4;

				sTempStandard = (String) lstGeneral.get(0).get("suncertainitymeasurement");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Table07Uncertainty\":[" + "{\"slno\"  : \"8\",\n" + "	\"label\" : \""
						+ (String) lstTestComments.get(12).get("sheader") + "\",\n" + "	\"Value\" : \""
						+ (output.length() > 0 ? output : "None.") + "\"\n");
				myWriter.write(" }, \n");
				myWriter.write("{	\"slno\"  : \"8.1\", \n");
				myWriter.write("\"label\" : \"" + (String) lstTestComments.get(13).get("sheader") + "\", \n");

				sTempStandard = (String) lstGeneral.get(0).get("ssamplingdetails");
				if (sTempStandard.length() > 0) {
					BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
					output = reader.lines().map(line -> line + "<br/>").collect(Collectors.joining(""));
				} else {
					output = sTempStandard;
				}

				myWriter.write("\"Value\" : \"" + (output.length() > 0 ? output : "None.") + "\" \n");
				myWriter.write(" }], \n");
			}

			if (nTemplateCode == 4) {
				myWriter.write("\"Table09Authorisation\":[\n" + "{	\"slno\"  : 9,\n" + "				\"label\" : \""
						+ (String) lstTestComments.get(14).get("sheader") + "\",\n" + "				\"Value\" : \"\",\n"
						+ "				\"Table09child\" :[\n");
			} else {
				myWriter.write("\"Table09Authorisation\":[\n" + "{	\"slno\"  : " + nSlno + ",\n"
						+ "				\"label\" : \"" + (String) lstTestComments.get(nHeaderIndex).get("sheader")
						+ "\",\n" + "				\"Value\" : \"\",\n" + "				\"Table09child\" :[\n");
			}
			myWriter.write("{\"slno\"  : \"\",\n");

			squery = "select (u.sfirstname || ' ' || u.slastname) susername,ur.suserrolename from coareporthistory crh,users u,userrole ur "
					+ " where crh.nuserrolecode = ur.nuserrolecode and crh.nusercode = u.nusercode and "
					+ " crh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and crh.nsitecode = " + userInfo.getNtranssitecode()
					+ " and u.nsitecode = " + userInfo.getNmastersitecode()
					+ " and ur.nsitecode = " + userInfo.getNmastersitecode()
					+ " and crh.ncoareporthistorycode = "
					+ " (select max(ncoareporthistorycode) from coareporthistory where ncoaparentcode = "
					+ inputMap.get("ncoaparentcode") + " and " + "	nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			List<Map<String, Object>> lstUserRole = jdbcTemplate.queryForList(squery);
			String sUserName = "";
			if (lstUserRole.size() > 0) {
				sUserName = (String) lstUserRole.get(0).get("susername");
				String sRoleName = (String) lstUserRole.get(0).get("suserrolename");

				squery = "select ssettingvalue from reportsettings where ssettingname = 'Authorisation Desc' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				String sAuthDesc = jdbcTemplate.queryForObject(squery, String.class);

				myWriter.write("\"label\" : \"" + sAuthDesc + "\", \n");
				myWriter.write("\"Value\" : \"" + sUserName + ", " + sRoleName + ", SMTL.\" \n");
			} else {
				myWriter.write("\"label\" : \"\", \n");
				myWriter.write("\"Value\" : \"\" \n");
			}

			nHeaderIndex += 1;
			myWriter.write(" }] \n  }] ,\n");

			String sReference = "";
			if (nTemplateCode == 4) {
				myWriter.write("\"Table11References\":[			\n" + "{	\"slno\"  : \"\",\n"
						+ "				\"label\" : \"" + (String) lstTestComments.get(15).get("sheader") + "\",\n"
						+ "				\"Value\" : \"\",\n" + "				\"Table11child\" :[\n");
			} else {
				myWriter.write("\"Table11References\":[			\n" + "{	\"slno\"  : \"\",\n"
						+ "				\"label\" : \"" + (String) lstTestComments.get(nHeaderIndex).get("sheader")
						+ "\",\n" + "				\"Value\" : \"\",\n" + "				\"Table11child\" :[\n");
			}
			Boolean bRefblag = true;
			for (int i = 0; i < lstGeneral.size(); i++) {
				bRefblag = true;

				sTempStandard = (String) lstGeneral.get(i).get("smethodstandard");
				if (sTempStandard.length() > 0 && sTempStandard.toUpperCase().equals("NONE") == false) {
					sReference += "{\"slno\"  : \"\",\n";
					sReference += "\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + "\", \n";
					bRefblag = false;
					if (sTempStandard.length() > 0) {
						BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
						output = reader.lines().map(line -> line + "\\n").collect(Collectors.joining(""));
					} else {
						output = sTempStandard;
					}
					sReference += "\"Value\" : \"" + "" + output + "" + "\", \n";
				}
				sTempStandard = (String) lstGeneral.get(i).get("sreference");

				if (sTempStandard.length() > 0 && sTempStandard.toUpperCase().equals("NONE") == false) {
					if (bRefblag == true) {
						sReference += "{\"slno\"  : \"\",\n";
						sReference += "\"label\" : \"" + (String) lstGeneral.get(i).get("stestname") + "\", \n";
					}
					if (sTempStandard.length() > 0) {
						BufferedReader reader = new BufferedReader(new StringReader(sTempStandard));
						output = reader.lines().map(line -> line + "\\n").collect(Collectors.joining(""));
					} else {
						output = sTempStandard;
					}
					sReference += "\"Reference\" : \"" + "<ul><li>" + output + "</li></ul>" + "\" \n";
				}
				if (i == lstGeneral.size() - 1) {
					if (sReference != null && sReference.length() > 0) {
						String sTempRef = sReference.trim().substring(sReference.trim().length() - 1);
						if (sTempRef.equals(",")) {
							sReference = sReference.trim().substring(0, sReference.trim().length() - 1);
						}
						sReference += " }] \n  }] }\n";
						myWriter.write(sReference);
					} else {
						sReference += " ] \n  }] }\n";
						myWriter.write(sReference);
					}
				} else {
					if (bRefblag == false) {
						sReference += " }, \n";
					}
				}
			}
			myWriter.close();
		}
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return returnMap;
	}

}
