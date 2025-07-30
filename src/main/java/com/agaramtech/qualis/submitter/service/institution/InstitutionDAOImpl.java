package com.agaramtech.qualis.submitter.service.institution;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Country;
import com.agaramtech.qualis.credential.model.Site;
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
import com.agaramtech.qualis.submitter.model.City;
import com.agaramtech.qualis.submitter.model.District;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;
import com.agaramtech.qualis.submitter.model.InstitutionFile;
import com.agaramtech.qualis.submitter.model.InstitutionSite;
import com.agaramtech.qualis.submitter.model.Region;
import com.agaramtech.qualis.submitter.service.institutioncategory.InstitutionCategoryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class InstitutionDAOImpl implements InstitutionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionDAOImpl.class);

	private final InstitutionCategoryDAO institutioncategoryDAO;
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getInstitution(final UserInfo userInfo) throws Exception {
		LOGGER.info("getInstitution");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InstitutionCategory> lstInstitutionCategory = (List<InstitutionCategory>) institutioncategoryDAO
				.getInstitutionCategory(userInfo).getBody();
		outputMap.put("FilterInstitutionCategory", lstInstitutionCategory);

		if (!lstInstitutionCategory.isEmpty()) {
			int ninstitutioncatcode = 0;
			ninstitutioncatcode = lstInstitutionCategory.get(lstInstitutionCategory.size() - 1)
					.getNinstitutioncatcode();
			outputMap.put("SelectedInstitutionCategory", lstInstitutionCategory.get(lstInstitutionCategory.size() - 1));
			outputMap.put("defaultInstitutionCategory", lstInstitutionCategory.get(lstInstitutionCategory.size() - 1));

			final List<Institution> lstInstitution = getInstitutionbyInstitutionCategory(ninstitutioncatcode, userInfo)
					.getBody();
			outputMap.put("Institution", lstInstitution);
			if (!lstInstitution.isEmpty()) {
				int ninstitutioncode = 0;
				ninstitutioncode = lstInstitution.get(lstInstitution.size() - 1).getNinstitutioncode();
				outputMap.put("selectedInstitution", lstInstitution.get(lstInstitution.size() - 1));
				final List<InstitutionSite> lstInstitutionSite = getInstitutionSitebyInstitution(ninstitutioncode,
						userInfo).getBody();
				outputMap.put("InstitutionSite", lstInstitutionSite);
				final List<InstitutionFile> lstInstitutionFile = getInstitutionFilebyInstitution(ninstitutioncode,
						userInfo).getBody();
				outputMap.put("InstitutionFile", lstInstitutionFile);

				if (!lstInstitutionSite.isEmpty()) {
					outputMap.put("selectedInstitutionSite", lstInstitutionSite.get(lstInstitutionSite.size() - 1));
				} else {
					outputMap.put("selectedInstitutionSite", null);
				}
				if (!lstInstitutionFile.isEmpty()) {
					outputMap.put("selectedInstitutionFile", lstInstitutionFile.get(lstInstitutionFile.size() - 1));
				} else {
					outputMap.put("selectedInstitutionFile", null);
				}
			} else {
				outputMap.put("InstitutiontSite", lstInstitution);
				outputMap.put("InstitutionFile", lstInstitution);
				outputMap.put("selectedInstitution", null);
				outputMap.put("selectedInstitutionSite", null);
				outputMap.put("selectedInstitutionFile", null);
			}
		} else {
			outputMap.put("Institution", lstInstitutionCategory);
			outputMap.put("InstitutionSite", lstInstitutionCategory);
			outputMap.put("InstitutionFile", lstInstitutionCategory);
			outputMap.put("SelectedInstitutionCategory", null);
			outputMap.put("SelectedInstitution", null);
			outputMap.put("SelectedInstitutionSite", null);
			outputMap.put("selectedInstitutionFile", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<List<Institution>> getInstitutionbyInstitutionCategory(final int ninstitutioncatcode,
			final UserInfo userInfo) throws Exception {
		final String str = " select i.ninstitutioncode,i.ninstitutioncatcode,i.sinstitutionname,i.sinstitutioncode,"
				+ "case when i.sdescription ='' then '-' else i.sdescription end as sdescription,ic.sinstitutioncatname,"
				+ "to_char(i.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate from institution i "
				+ " join institutioncategory ic on ic.ninstitutioncatcode = i.ninstitutioncatcode "
				+ " where i.ninstitutioncatcode=" + ninstitutioncatcode + " and i.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nsitecode ="
				+ userInfo.getNmastersitecode() + " and i.nsitecode=ic.nsitecode;";
		final List<Institution> lstInstitution = jdbcTemplate.query(str, new Institution());
		return new ResponseEntity<>(lstInstitution, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<InstitutionSite>> getInstitutionSitebyInstitution(final int ninstitutioncode,
			final UserInfo userInfo) throws Exception {
		final String str = "select ins.ninstitutionsitecode,ins.ninstitutioncode,ins.nregionalsitecode,ins.ncitycode,"
				+ "ins.ncountrycode,ins.sinstitutionsitename,ins.sinstitutionsiteaddress,ins.szipcode, "
				+ "ins.sstate,ins.stelephone,ins.sfaxno,ins.semail,ins.swebsite,i.sinstitutionname,s.ssitename,"
				+ "s.ssitecode,ins.scityname,c.scitycode,c1.scountryname,r.sregionname,r.sregioncode,d.sdistrictcode,"
				+ "d.sdistrictname,ins.nregioncode,ins.ndistrictcode from institutionsite ins "
				+ "join institution i on i.ninstitutioncode =ins.ninstitutioncode "
				+ "join site s on s.nsitecode =ins.nregionalsitecode " + "join city c on c.ncitycode=ins.ncitycode "
				+ "join country c1 on c1.ncountrycode =ins.ncountrycode "
				+ "join region r on r.nregioncode=ins.nregioncode "
				+ " join district d on d.ndistrictcode=ins.ndistrictcode " + "where ins.ninstitutioncode="
				+ ninstitutioncode + " and ins.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c1.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InstitutionSite> lstInstitutionSite = jdbcTemplate.query(str, new InstitutionSite());
		return new ResponseEntity<>(lstInstitutionSite, HttpStatus.OK);
	}

	private ResponseEntity<List<InstitutionFile>> getInstitutionFilebyInstitution(final int ninstitutioncode,
			final UserInfo userInfo) throws Exception {
		final String str = "select if.ninstitutionfilecode,if.ninstitutioncode,if.nfilesize,if.sfilename,if.sfiledesc,if.ssystemfilename,"
				+ "if.nlinkcode,if.nattachmenttypecode,i.sinstitutionname,coalesce(at.jsondata->'sattachmenttype') as sattachmenttype,"
				+ " case when if.nlinkcode=" + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " then '-' else lm.jsondata->>'slinkname' end slinkname,case when if.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(if.dcreateddate,'"
				+ userInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate from institutionfile if "
				+ "join institution i on if.ninstitutioncode =i.ninstitutioncode "
				+ "join linkmaster lm on lm.nlinkcode =if.nlinkcode "
				+ "join attachmenttype at on at.nattachmenttypecode = if.nattachmenttypecode "
				+ "where i.ninstitutioncode =" + ninstitutioncode + " and  if.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InstitutionFile> lstInstitutionFile = jdbcTemplate.query(str, new InstitutionFile());
		return new ResponseEntity<>(lstInstitutionFile, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionCategory(final UserInfo userInfo) throws Exception {
		final String strInstitutionCat = "select ninstitutioncatcode,sinstitutioncatname from institutioncategory "
				+ "where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ninstitutioncatcode <>-1 and nsitecode =" + userInfo.getNmastersitecode() + ";";
		return new ResponseEntity<Object>(jdbcTemplate.query(strInstitutionCat, new InstitutionCategory()),
				HttpStatus.OK);
	}

	private Institution getInstitutionByName(final int institutionCatCode, final String institutionName,
			final String institutioncode, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ninstitutioncode from institution where(sinstitutionname = N'"
				+ stringUtilityFunction.replaceQuote(institutionName) + "' or sinstitutioncode=N'"
				+ stringUtilityFunction.replaceQuote(institutioncode) + "') and ninstitutioncatcode="
				+ institutionCatCode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode =" + nmasterSiteCode;
		return (Institution) jdbcUtilityFunction.queryForObject(strQuery, Institution.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> createInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table institution " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionList = new ArrayList<>();
		final InstitutionCategory lstInstitutionCat = getInstitutionCatByinstitutioncat(
				objInstitution.getNinstitutioncatcode());
		if (lstInstitutionCat != null) {
			final Institution InstitutionByName = getInstitutionByName(objInstitution.getNinstitutioncatcode(),
					objInstitution.getSinstitutionname(), objInstitution.getSinstitutioncode(),
					objInstitution.getNsitecode());
			if (InstitutionByName == null) {
				final String seqquery = "select nsequenceno from seqnosubmittermanagement where stablename ='institution' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nsequenceno = jdbcTemplate.queryForObject(seqquery, Integer.class);
				nsequenceno++;
				final String insertquery = "Insert into institution (ninstitutioncode,ninstitutioncatcode,sinstitutionname,"
						+ "sinstitutioncode,sdescription,dmodifieddate,nsitecode,nstatus)" + "values(" + nsequenceno
						+ "," + objInstitution.getNinstitutioncatcode() + "," + "N'"
						+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutionname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutioncode()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objInstitution.getSdescription()) + "'," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + "" + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);
				final String updatequery = "update seqnosubmittermanagement set nsequenceno=" + nsequenceno
						+ " where stablename='institution'";
				jdbcTemplate.execute(updatequery);
				objInstitution.setNinstitutioncode(nsequenceno);
				savedInstitutionList.add(objInstitution);
				multilingualIDList.add("IDS_ADDINSTITUTION");
				auditUtilityFunction.fnInsertAuditAction(savedInstitutionList, 1, null, multilingualIDList, userInfo);
				return getInstitution(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final Institution institution = getActiveInstitutionById(objInstitution.getNinstitutioncode(), userInfo);
		if (institution == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final InstitutionCategory lstInstitutionCat = getInstitutionCatByinstitutioncat(
					objInstitution.getNinstitutioncatcode());
			if (lstInstitutionCat != null) {
				final String queryString = "select ninstitutioncode from institution where (sinstitutionname = N'"
						+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutionname())
						+ "' or sinstitutioncode = N'"
						+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutionname()) + "')"
						+ " and ninstitutioncatcode =" + objInstitution.getNinstitutioncatcode()
						+ " and ninstitutioncode <> " + objInstitution.getNinstitutioncode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();
				final List<Institution> institutionList = jdbcTemplate.query(queryString, new Institution());
				if (institutionList.isEmpty()) {
					final String updateQueryString = "update institution set ninstitutioncatcode="
							+ objInstitution.getNinstitutioncatcode() + "" + ", sinstitutionname=N'"
							+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutionname())
							+ "' , sinstitutioncode=N'"
							+ stringUtilityFunction.replaceQuote(objInstitution.getSinstitutioncode())
							+ "', sdescription =N'"
							+ stringUtilityFunction.replaceQuote(objInstitution.getSdescription()) + "', "
							+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
							+ " where ninstitutioncode=" + objInstitution.getNinstitutioncode();

					jdbcTemplate.execute(updateQueryString);
					multilingualIDList.add("IDS_EDITINSTITUTION");
					listAfterSave.add(objInstitution);
					listBeforeSave.add(institution);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getInstitution(userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedInstitutionList = new ArrayList<>();
		final Institution institution = getActiveInstitutionById(objInstitution.getNinstitutioncode(), userInfo);
		if (institution == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String dltquery = "select 'IDS_SUBMITTER' as Msg from submittermapping where ninstitutioncode= "
					+ objInstitution.getNinstitutioncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(dltquery, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				final Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue", Integer.toString(objInstitution.getNinstitutioncode()));
				objOneToManyValidation.put("stablename", "institution");
				validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				String updateQueryString = " update institution set  dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninstitutioncode="
						+ objInstitution.getNinstitutioncode() + ";";

				updateQueryString = updateQueryString + " update institutionsite set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninstitutioncode="
						+ objInstitution.getNinstitutioncode() + ";";

				updateQueryString = updateQueryString + " update institutionfile set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninstitutioncode="
						+ objInstitution.getNinstitutioncode() + ";";

				jdbcTemplate.execute(updateQueryString);

				String query = "select s.*,-1 as nstatus from institutionsite s where ninstitutioncode="
						+ objInstitution.getNinstitutioncode() + " and nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus();
				final List<InstitutionSite> lstInsSite = jdbcTemplate.query(query, new InstitutionSite());

				query = "select f.*,-1 as nstatus from institutionfile f where ninstitutioncode="
						+ objInstitution.getNinstitutioncode() + " and nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus();
				final List<InstitutionFile> lstInsFile = jdbcTemplate.query(query, new InstitutionFile());

				final List<Institution> ins = new ArrayList<Institution>();
				ins.add(institution);
				objInstitution.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedInstitutionList.add(ins);
				deletedInstitutionList.add(lstInsSite);
				deletedInstitutionList.add(lstInsFile);
				multilingualIDList.add("IDS_DELETEINSTITUTION");
				auditUtilityFunction.fnInsertListAuditAction(deletedInstitutionList, 1, null, Arrays.asList(
						"IDS_DELETEINSTITUTION", "IDS_DELETEINSTITUTIONSITE", "IDS_DELETEINSTITUTIONFILE"), userInfo);
				return getInstitutionByCategory(objInstitution.getNinstitutioncatcode(), userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public Institution getActiveInstitutionById(final int ninstitutioncode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select i.ninstitutioncode,i.ninstitutioncatcode,i.sinstitutionname,i.sdescription,ic.sinstitutioncatname,i.sinstitutioncode from institution i "
				+ "join institutioncategory ic on ic.ninstitutioncatcode =i.ninstitutioncatcode " + "where i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.ninstitutioncode="
				+ ninstitutioncode;
		return (Institution) jdbcUtilityFunction.queryForObject(strQuery, Institution.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByCategory(final int ninstitutionCategoryCode,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final InstitutionCategory lstInstitutionCat = getInstitutionCatByinstitutioncat(ninstitutionCategoryCode);
		if (lstInstitutionCat != null) {
			map.put("defaultInstitutionCategory", lstInstitutionCat);
		} else {
			map.put("defaultInstitutionCategory", null);
		}
		final List<Institution> lstInstitution = getInstitutionbyInstitutionCategory(ninstitutionCategoryCode,
				objUserInfo).getBody();
		map.put("Institution", lstInstitution);
		if (!lstInstitution.isEmpty()) {
			map.put("selectedInstitution", lstInstitution.get(lstInstitution.size() - 1));
			final int ninstitutioncode = lstInstitution.get(lstInstitution.size() - 1).getNinstitutioncode();
			final List<InstitutionSite> lstInstitutionSiteGet = getInstitutionSitebyInstitution(ninstitutioncode,
					objUserInfo).getBody();
			final List<InstitutionFile> lstInstitutionFileGet = getInstitutionFilebyInstitution(ninstitutioncode,
					objUserInfo).getBody();

			if (lstInstitutionSiteGet.size() > 0) {
				map.put("selectedInstitutionSite", lstInstitutionSiteGet.get(lstInstitutionSiteGet.size() - 1));
			} else {
				map.put("selectedInstitutionSite", null);
			}
			if (lstInstitutionFileGet.size() > 0) {
				map.put("selectedInstitutionFile", lstInstitutionFileGet.get(lstInstitutionFileGet.size() - 1));
			} else {
				map.put("selectedInstitutionFile", null);

			}
			map.put("InstitutionSite", lstInstitutionSiteGet);
			map.put("InstitutionFile", lstInstitutionFileGet);
		} else {

			map.put("InstitutionSite", Arrays.asList());
			map.put("InstitutionFile", Arrays.asList());
			map.put("selectedInstitution", null);
			map.put("selectedInstitutionSite", null);
			map.put("selectedInstitutionFile", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public InstitutionCategory getInstitutionCatByinstitutioncat(final int ninstitutionCategoryCode) throws Exception {
		final String query = "select * from institutioncategory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstitutioncatcode="
				+ ninstitutionCategoryCode;
		final InstitutionCategory lstInstitutionCat = (InstitutionCategory) jdbcUtilityFunction.queryForObject(query,
				InstitutionCategory.class, jdbcTemplate);
		return lstInstitutionCat;
	}

	public List<InstitutionSite> getInstitutionSite(final int ninstitutionCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select ins.ninstitutionsitecode,ins.ninstitutioncode,ins.nregionalsitecode,ins.ncitycode,"
				+ " ins.ncountrycode,ins.sinstitutionsitename,ins.sinstitutionsiteaddress,ins.szipcode,"
				+ " ins.sstate,ins.stelephone,ins.sfaxno,ins.semail,ins.swebsite,i.sinstitutionname,s.ssitename,ins.scityname,"
				+ " c1.scountryname ,r.sregionname,d.sdistrictname ,ins.nregioncode,ins.ndistrictcode ,r.sregioncode,d.sdistrictcode"
				+ " from institutionsite ins " + "join institution  i on ins.ninstitutioncode = i.ninstitutioncode"
				+ " join site s on ins.nregionalsitecode = s.nsitecode " + "join city c on ins.ncitycode = c.ncitycode"
				+ " join country c1 on ins.ncountrycode = c1.ncountrycode "
				+ " join region r on ins.nregioncode=r.nregioncode "
				+ " join district d on ins.ndistrictcode=d.ndistrictcode " + "where ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.ninstitutioncode ="
				+ ninstitutionCode;
		final List<InstitutionSite> lstinstitutionSite = jdbcTemplate.query(strQuery, new InstitutionSite());
		return lstinstitutionSite;
	}

	@Override
	public ResponseEntity<Object> getInstitutionSiteCombo(final int ninstitutioncode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> map = new HashMap<String, Object>();

		final Institution objInstitution = getActiveInstitutionById(ninstitutioncode, userInfo);

		if (objInstitution != null) {

			final String siteQuery = "select nsitecode,nsitecode as nregionalsitecode,ssitename,ssitecode,ndefaultstatus"
					+ " from site where nsitecode>0 and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode ="
					+ userInfo.getNmastersitecode();
			final List<Site> lstSite = jdbcTemplate.query(siteQuery, new Site());
			map.put("Site", lstSite);

			final String regionQuery = "select * from region where nregioncode >0 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<Region> lstRegion = jdbcTemplate.query(regionQuery, new Region());
			map.put("Region", lstRegion);

			final String countryQuery = "select * from country where ncountrycode >0 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<Country> lsCountry = jdbcTemplate.query(countryQuery, new Country());
			map.put("Country", lsCountry);

			return new ResponseEntity<Object>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INSTITUTIONISDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> createInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table institutionsite "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionSiteList = new ArrayList<>();
		final Institution objInstitution = getActiveInstitutionById(objInstitutionSite.getNinstitutioncode(), userInfo);

		if (objInstitution != null) {

			final InstitutionSite InstitutionSiteByName = getInstitutionSiteByName(
					objInstitutionSite.getSinstitutionsitename(), objInstitutionSite.getNinstitutioncode());
			if (InstitutionSiteByName == null) {

				final String institutiontSiteSeq = "select nsequenceno from seqnosubmittermanagement where stablename='institutionsite' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int seqNo = jdbcTemplate.queryForObject(institutiontSiteSeq, Integer.class);
				seqNo = seqNo + 1;
				final String institutionSiteInsert = "insert into institutionsite (ninstitutionsitecode, ninstitutioncode, nregionalsitecode, nregioncode, ndistrictcode, ncitycode, ncountrycode, sinstitutionsitename, sinstitutionsiteaddress, "
						+ " szipcode, sstate, stelephone, sfaxno, semail, swebsite, dmodifieddate, nstatus,nsitecode,scityname)"
						+ " values (" + seqNo + "," + objInstitutionSite.getNinstitutioncode() + ","
						+ objInstitutionSite.getNregionalsitecode() + "," + objInstitutionSite.getNregioncode() + ","
						+ objInstitutionSite.getNdistrictcode() + ",-1," + objInstitutionSite.getNcountrycode() + ", N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSinstitutionsitename()) + "'," + "N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSinstitutionsiteaddress()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSzipcode()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSstate()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getStelephone()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSfaxno()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSemail()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSwebsite()) + "'," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ userInfo.getNmastersitecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getScityname()) + "')";
				jdbcTemplate.execute(institutionSiteInsert);

				final String institutionSiteSeqUpdate = "update seqnosubmittermanagement set nsequenceno=" + seqNo
						+ " where stablename='institutionsite'";
				jdbcTemplate.execute(institutionSiteSeqUpdate);

				multilingualIDList.add("IDS_ADDINSTITUTIONSITE");
				objInstitutionSite.setNinstitutionsitecode(seqNo);
				savedInstitutionSiteList.add(objInstitutionSite);
				auditUtilityFunction.fnInsertAuditAction(savedInstitutionSiteList, 1, null, multilingualIDList,
						userInfo);
				final List<InstitutionSite> InstSite = getInstitutionSite(objInstitutionSite.getNinstitutioncode(),
						userInfo);
				objMap.put("InstitutionSite", InstSite);
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INSTITUTIONISDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private InstitutionSite getInstitutionSiteByName(final String insitutionSiteName, final int ninstitutionCode)
			throws Exception {
		final String strQuery = "select  sinstitutionsitename from institutionsite where sinstitutionsitename = N'"
				+ stringUtilityFunction.replaceQuote(insitutionSiteName) + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstitutioncode = "
				+ ninstitutionCode;
		return (InstitutionSite) jdbcUtilityFunction.queryForObject(strQuery, InstitutionSite.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final InstitutionSite institutionSiteByID = getActiveInstitutionSiteById(
				objInstitutionSite.getNinstitutioncode(), objInstitutionSite.getNinstitutionsitecode(), userInfo);
		if (institutionSiteByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select sinstitutionsitename from institutionsite where sinstitutionsitename = N'"
					+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSinstitutionsitename())
					+ "' and ninstitutioncode = " + objInstitutionSite.getNinstitutioncode()
					+ " and ninstitutionsitecode <> " + objInstitutionSite.getNinstitutionsitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<InstitutionSite> institutionSite = jdbcTemplate.query(queryString, new InstitutionSite());
			if (institutionSite.isEmpty()) {
				final String updateQueryString = "update institutionsite set nregionalsitecode ="
						+ objInstitutionSite.getNregionalsitecode() + ", ncitycode= -1 , ncountrycode ="
						+ objInstitutionSite.getNcountrycode() + "," + "sinstitutionsitename=N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSinstitutionsitename())
						+ "',sinstitutionsiteaddress=N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getSinstitutionsiteaddress()) + "',"
						+ "szipcode =N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getSzipcode())
						+ "',sstate=N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getSstate()) + "',"
						+ "stelephone=N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getStelephone())
						+ "',sfaxno=N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getSfaxno())
						+ "',semail=N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getSemail()) + "',"
						+ "swebsite=N'" + stringUtilityFunction.replaceQuote(objInstitutionSite.getSwebsite()) + "',"
						+ " nregioncode=" + objInstitutionSite.getNregioncode() + ",ndistrictcode="
						+ objInstitutionSite.getNdistrictcode() + "," + " dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',scityname =N'"
						+ stringUtilityFunction.replaceQuote(objInstitutionSite.getScityname()) + "' "
						+ " where ninstitutioncode =" + objInstitutionSite.getNinstitutioncode()
						+ " and ninstitutionsitecode =" + objInstitutionSite.getNinstitutionsitecode();

				jdbcTemplate.execute(updateQueryString);
				multilingualIDList.add("IDS_EDITINSTITUTIONSITE");
				listAfterUpdate.add(objInstitutionSite);
				listBeforeUpdate.add(institutionSiteByID);
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				final List<InstitutionSite> lstClientSiteAddress = getInstitutionSite(
						objInstitutionSite.getNinstitutioncode(), userInfo);
				outputMap.put("InstitutionSite", lstClientSiteAddress);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public InstitutionSite getActiveInstitutionSiteById(final int ninstitutionCode, final int ninstitutionSiteCode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select ins.ninstitutionsitecode,ins.ninstitutioncode,ins.nregionalsitecode,ins.nregionalsitecode as nsitecode, ins.ncitycode,ins.ncountrycode,ins.sinstitutionsitename,ins.sinstitutionsiteaddress,ins.szipcode, "
				+ "ins.sstate,ins.stelephone,ins.sfaxno,ins.semail,ins.swebsite,i.sinstitutionname,s.ssitename,s.ssitecode,ins.scityname,c.scitycode, c1.scountryname,r.sregionname,d.sdistrictname,ins.nregioncode,ins.ndistrictcode ,r.sregioncode,d.sdistrictcode "
				+ " from institutionsite ins " + " join institution  i on ins.ninstitutioncode = i.ninstitutioncode "
				+ " join site s on ins.nregionalsitecode = s.nsitecode "
				+ " join city c on ins.ncitycode = c.ncitycode  "
				+ " join country c1 on ins.ncountrycode = c1.ncountrycode "
				+ " join region r on ins.nregioncode=r.nregioncode "
				+ " join district d on ins.ndistrictcode=d.ndistrictcode where ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.ninstitutioncode ="
				+ ninstitutionCode + "  and ins.ninstitutionsitecode= " + ninstitutionSiteCode;
		return (InstitutionSite) jdbcUtilityFunction.queryForObject(strQuery, InstitutionSite.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> deleteInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionSiteList = new ArrayList<>();
		final InstitutionSite institutionSiteByID = getActiveInstitutionSiteById(
				objInstitutionSite.getNinstitutioncode(), objInstitutionSite.getNinstitutionsitecode(), userInfo);
		if (institutionSiteByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_SUBMITTER' as Msg FROM submittermapping sm where sm.ninstitutionsitecode="
					+ objInstitutionSite.getNinstitutionsitecode() + " and sm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sm.nsitecode="
					+ userInfo.getNmastersitecode();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;

				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objInstitutionSite.getNinstitutionsitecode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = " update institutionsite set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninstitutioncode="
						+ objInstitutionSite.getNinstitutioncode() + " and ninstitutionsitecode="
						+ objInstitutionSite.getNinstitutionsitecode();
				jdbcTemplate.execute(updateQueryString);
				objInstitutionSite.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				multilingualIDList.add("IDS_DELETEINSTITUTIONSITE");
				savedInstitutionSiteList.add(Arrays.asList(objInstitutionSite));
				auditUtilityFunction.fnInsertListAuditAction(savedInstitutionSiteList, 1, null, multilingualIDList,
						userInfo);
				final List<InstitutionSite> InstSite = getInstitutionSite(objInstitutionSite.getNinstitutioncode(),
						userInfo);
				objMap.put("InstitutionSite", InstSite);
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<? extends Object> createInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table institutionfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listObject = new ArrayList<Object>();
		final List<InstitutionFile> lstInstitutionFile = objMapper.readValue(request.getParameter("institutionfile"),
				new TypeReference<List<InstitutionFile>>() {
				});
		if (lstInstitutionFile != null && lstInstitutionFile.size() > 0) {
			final Institution lstInstitution = getActiveInstitutionById(lstInstitutionFile.get(0).getNinstitutioncode(),
					userInfo);
			if (lstInstitution != null) {

				if (lstInstitutionFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
				}
				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int offset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
					if (lstInstitutionFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
							.gettype()) {
						lstInstitutionFile.forEach(objIf -> {
							objIf.setDcreateddate(instantDate);
							objIf.setNoffsetdcreateddate(offset);
							objIf.setScreateddate(sattachmentDate.replace("T", " "));
						});
					}
					final String sequencequery = "select nsequenceno from seqnosubmittermanagement where stablename ='institutionfile' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;

					final String insertquery = "Insert into institutionfile (ninstitutionfilecode,ninstitutioncode,nfilesize,sfilename,sfiledesc,ssystemfilename,dcreateddate,"
							+ "ntzcreateddate,noffsetdcreateddate, dmodifieddate,ntzmodifieddate,noffsetdmodifieddate,"
							+ " nlinkcode,nattachmenttypecode,nstatus,nsitecode)" + " values (" + nsequenceno + ","
							+ lstInstitutionFile.get(0).getNinstitutioncode() + ","
							+ lstInstitutionFile.get(0).getNfilesize() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstInstitutionFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstInstitutionFile.get(0).getSfiledesc()) + "',"
							+ " N'" + lstInstitutionFile.get(0).getSsystemfilename() + "'," + " '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode()
							+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ userInfo.getNtimezonecode() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " "
							+ lstInstitutionFile.get(0).getNlinkcode() + ","
							+ lstInstitutionFile.get(0).getNattachmenttypecode() + ", " + ""
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ userInfo.getNmastersitecode() + ")";
					jdbcTemplate.execute(insertquery);

					final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
							+ " where stablename ='institutionfile'";
					jdbcTemplate.execute(updatequery);
					multilingualIDList
							.add(lstInstitutionFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_ADDINSTITUTIONFILE" : "IDS_ADDINSTITUTIONLINK");
					lstInstitutionFile.get(0).setNinstitutionfilecode(nsequenceno);
					listObject.add(lstInstitutionFile);
					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
					return getInstitutionFile(lstInstitutionFile.get(0).getNinstitutioncode(), userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INSTITUTIONALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<? extends Object> updateInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<Object>();
		final List<Object> lstNewObject = new ArrayList<Object>();
		final List<InstitutionFile> lstInstitutionFile = objMapper.readValue(request.getParameter("institutionfile"),
				new TypeReference<List<InstitutionFile>>() {
				});
		if (lstInstitutionFile != null && lstInstitutionFile.size() > 0) {
			final InstitutionFile objInstitutionFile = lstInstitutionFile.get(0);
			final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
			String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				if (objInstitutionFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
				}
			}
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
				final String sQuery = "select * from institutionfile where ninstitutionfilecode = "
						+ objInstitutionFile.getNinstitutionfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final InstitutionFile objIF = (InstitutionFile) jdbcUtilityFunction.queryForObject(sQuery,
						InstitutionFile.class, jdbcTemplate);

				if (objIF != null) {
					String ssystemfilename = "";
					if (objInstitutionFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						ssystemfilename = objInstitutionFile.getSsystemfilename();
					}
					final String sUpdateQuery = "update institutionfile set sfilename=N'"
							+ stringUtilityFunction.replaceQuote(objInstitutionFile.getSfilename()) + "',"
							+ " sfiledesc=N'" + stringUtilityFunction.replaceQuote(objInstitutionFile.getSfiledesc())
							+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
							+ objInstitutionFile.getNattachmenttypecode() + ",nlinkcode="
							+ objInstitutionFile.getNlinkcode() + "," + " nfilesize = "
							+ objInstitutionFile.getNfilesize() + "," + " dcreateddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',ntzcreateddate="
							+ userInfo.getNtimezonecode() + ",noffsetdcreateddate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "',ntzmodifieddate=" + userInfo.getNtimezonecode() + ",noffsetdmodifieddate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " "
							+ " where ninstitutionfilecode = " + objInstitutionFile.getNinstitutionfilecode();

					objInstitutionFile.setDcreateddate(objIF.getDcreateddate());
					objIF.setScreateddate(objInstitutionFile.getScreateddate());
					jdbcTemplate.execute(sUpdateQuery);

					lstNewObject.add(objInstitutionFile);

					multilingualIDList
							.add(objInstitutionFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_EDITINSTITUTIONFILE"
									: "IDS_EDITINSTITUTIONLINK");
					lstOldObject.add(objIF);

					auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
							userInfo);
					return getInstitutionFile(lstInstitutionFile.get(0).getNinstitutioncode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteInstitutionFile(final InstitutionFile objInstitutionFile,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstObject = new ArrayList<>();
		final int ninstitutionCode = objInstitutionFile.getNinstitutioncode();
		final InstitutionFile objInstFile = getInstitutionFileById(objInstitutionFile, userInfo);
		if (objInstFile != null) {
			if (objInstFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				ftpUtilityFunction.deleteFTPFile(Arrays.asList(objInstFile.getSsystemfilename()), "", userInfo);
			} else {
				objInstFile.setScreateddate(null);
			}
			final String sUpdateQuery = "update institutionfile set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninstitutionfilecode = "
					+ objInstFile.getNinstitutionfilecode();
			jdbcTemplate.execute(sUpdateQuery);
			multilingualIDList.add(objInstFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_DELETEINSTITUTIONFILE"
					: "IDS_DELETEINSTITUTIONLINK");
			lstObject.add(objInstFile);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			return getInstitutionFile(ninstitutionCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public InstitutionFile getInstitutionFileById(final InstitutionFile objInstitutionFile, final UserInfo userInfo)
			throws Exception {
		final String sSpecFileQuery = "select inf.ninstitutionfilecode,inf.ninstitutioncode,inf.nfilesize,inf.sfilename,inf.sfiledesc,inf.ssystemfilename,inf.nlinkcode,inf.nattachmenttypecode "
				+ ", coalesce(at.jsondata->'sattachmenttype'->>'en-US'  ,at.jsondata->'sattachmenttype'->>'en-US') as stypename, lm.jsondata->>'slinkname' as slinkname "
				+ " from institutionfile inf "
				+ " join  attachmenttype at on at.nattachmenttypecode = inf.nattachmenttypecode "
				+ " join linkmaster lm on lm.nlinkcode = inf.nlinkcode " + " where   at.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and inf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and inf.ninstitutionfilecode ="
				+ objInstitutionFile.getNinstitutionfilecode();
		return (InstitutionFile) jdbcUtilityFunction.queryForObject(sSpecFileQuery, InstitutionFile.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> viewInstitutionFile(final InstitutionFile objInstitutionFile, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstObject = new ArrayList<>();
		String sQuery = "select * from institutionfile where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstitutionfilecode = "
				+ objInstitutionFile.getNinstitutionfilecode();
		final InstitutionFile objINF = (InstitutionFile) jdbcUtilityFunction.queryForObject(sQuery,
				InstitutionFile.class, jdbcTemplate);
		if (objINF != null) {
			if (objINF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
				map = ftpUtilityFunction.FileViewUsingFtp(objINF.getSsystemfilename(), -1, userInfo, "", "");
			} else {
				sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
						+ objINF.getNlinkcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
						LinkMaster.class, jdbcTemplate);
				map.put("AttachLink", objlinkmaster.getSlinkname() + objINF.getSfilename());
			}

			multilingualIDList.add(objINF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
					? "IDS_VIEWINSTITUTIONFILE"
					: "IDS_VIEWINSTITUTIONLINK");
			lstObject.add(objINF);
			auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			return new ResponseEntity<Object>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionFile(final int institutionCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String sSpecFileQry = "select inf.ninstitutionfilecode,inf.ninstitutioncode,inf.nfilesize,inf.sfilename,"
				+ " inf.sfiledesc,inf.ssystemfilename,inf.nlinkcode,inf.nattachmenttypecode,"
				+ " COALESCE(at.jsondata->'sattachmenttype'->>'en-US', at.jsondata->'sattachmenttype'->>'en-US') as stypename,"
				+ " case when inf.nlinkcode = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " then '-' else lm.jsondata->>'slinkname' end slinkname, case when inf.nlinkcode = "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " then cast(inf.nfilesize as text) else '-' end sfilesize, case when inf.nlinkcode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " then '-' else lm.jsondata->>'slinkname' end slinkname,case when inf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else COALESCE(TO_CHAR(inf.dcreateddate,'"
				+ userInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate from institutionfile inf "
				+ " join  attachmenttype at on at.nattachmenttypecode = inf.nattachmenttypecode "
				+ " join linkmaster lm on lm.nlinkcode = inf.nlinkcode " + " where at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and inf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and inf.ninstitutioncode = "
				+ institutionCode + " ";
		final List<InstitutionFile> lstInstitutionFile = jdbcTemplate.query(sSpecFileQry, new InstitutionFile());
		outputMap.put("InstitutionFile", lstInstitutionFile);
		if (!lstInstitutionFile.isEmpty()) {
			outputMap.put("selectedInstitutionFile", lstInstitutionFile.get(lstInstitutionFile.size() - 1));
		} else {
			outputMap.put("selectedInstitutionFile", null);
		}
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSelectedInstitutionDetail(final UserInfo userInfo, final int ninstitutionCode)
			throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final Institution lstInstitution = getActiveInstitutionById(ninstitutionCode, userInfo);
		objMap.put("selectedInstitution", lstInstitution);
		final List<InstitutionSite> lstInstitutionSite = getInstitutionSite(ninstitutionCode, userInfo);
		objMap.put("InstitutionSite", lstInstitutionSite);
		objMap.putAll(getInstitutionFile(ninstitutionCode, userInfo).getBody());
		if (lstInstitutionSite.size() > 0) {
			objMap.put("selectedInstitutionSite", lstInstitutionSite.get(lstInstitutionSite.size() - 1));
		} else {
			objMap.put("selectedInstitutionSite", null);
		}
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select  * from district where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregioncode =" + nregioncode
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		outputMap.put("districtList", jdbcTemplate.query(query, new District()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String query = "select  * from city" + " where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndistrictcode =" + ndistrictcode
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		outputMap.put("cityList", jdbcTemplate.query(query, new City()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception {
		final String str = "select ins.ninstitutionsitecode,ins.ninstitutioncode,ins.nregionalsitecode,ins.ncitycode,"
				+ " ins.ncountrycode,ins.sinstitutionsitename,ins.sinstitutionsiteaddress,ins.szipcode,ins.sstate,"
				+ " ins.stelephone,ins.sfaxno,ins.semail,ins.swebsite,i.sinstitutionname,s.ssitename,ins.scityname,"
				+ " c1.scountryname ,r.sregionname,d.sdistrictname ,ins.nregioncode,ins.ndistrictcode ,r.sregioncode,"
				+ " d.sdistrictcode, to_char(ins.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate  from institutionsite ins "
				+ " join institution  i on ins.ninstitutioncode = i.ninstitutioncode "
				+ " join site s on ins.nregionalsitecode = s.nsitecode "
				+ " join city c on ins.ncitycode = c.ncitycode  "
				+ " join country c1 on ins.ncountrycode = c1.ncountrycode "
				+ " join region r on ins.nregioncode=r.nregioncode "
				+ " join district d on ins.ndistrictcode=d.ndistrictcode where ins.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nsitecode="
				+ userInfo.getNmastersitecode() + " and i.nsitecode=" + userInfo.getNmastersitecode()
				+ " and s.nsitecode=" + userInfo.getNmastersitecode() + " and c.nsitecode="
				+ userInfo.getNmastersitecode() + " and c1.nsitecode=" + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(str, new InstitutionSite()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionValues(final UserInfo userInfo) throws Exception {
		final String str = "select ninstitutioncode, ninstitutioncatcode, sinstitutionname, sinstitutioncode, sdescription, "
				+ " nsitecode, nstatus, to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate from institution where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(str, new Institution()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionDistrict(final UserInfo objUserInfo) throws Exception {
		final String query = "select d.ndistrictcode, d.sdistrictname, d.nstatus "
				+ " from submittermapping sm, institutionsite ist, district d "
				+ " where sm.ninstitutionsitecode=ist.ninstitutionsitecode " + " and ist.ndistrictcode=d.ndistrictcode "
				+ " and sm.nstatus=ist.nstatus and ist.nstatus=d.nstatus and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and d.nsitecode=ist.nsitecode"
				+ " and ist.nsitecode=sm.nsitecode group by d.ndistrictcode";
		return new ResponseEntity<>(jdbcTemplate.query(query, new District()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionCategoryByDistrict(final int districtCode, final UserInfo objUserInfo)
			throws Exception {
		final String query = "select  sm.ninstitutioncatcode, ic.sinstitutioncatname,"
				+ " d.ndistrictcode, d.sdistrictname, d.sdistrictcode, d.nstatus "
				+ " from submittermapping sm,institutionsite ist, institutioncategory ic , district d"
				+ " where sm.ninstitutionsitecode=ist.ninstitutionsitecode"
				+ " and sm.ninstitutioncatcode = ic.ninstitutioncatcode" + " and ist.ndistrictcode=d.ndistrictcode"
				+ " and d.ndistrictcode=" + districtCode + " and sm.nstatus=ist.nstatus and ist.nstatus=ic.nstatus "
				+ " and ic.nstatus=d.nstatus and d.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by  sm.ninstitutioncatcode,ic.sinstitutioncatname,d.ndistrictcode";
		return new ResponseEntity<>(jdbcTemplate.query(query, new InstitutionCategory()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByMappedCategory(final int ninstitutionCatCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception {
		final String query = "select  i.sinstitutionname, i.ninstitutioncode, i.nstatus "
				+ " from submittermapping sm, district d,institutionsite ist, institution i"
				+ " where sm.ninstitutioncode =i.ninstitutioncode and ist.ndistrictcode=d.ndistrictcode "
				+ " and  sm.ninstitutionsitecode=ist.ninstitutionsitecode and d.ndistrictcode=" + ndistrictCode
				+ " and sm.ninstitutioncatcode=" + ninstitutionCatCode + " and sm.nstatus=i.nstatus and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by  i.sinstitutionname , i.ninstitutioncode";
		return new ResponseEntity<>(jdbcTemplate.query(query, new Institution()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getInstitutionSiteByMappedInstitution(final int ninstitutionCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception {
		final String query = "select  ist.sinstitutionsitename, ist.ninstitutionsitecode, ist.nstatus "
				+ " from submittermapping sm,institutionsite ist, " + " institution i,district d"
				+ " where sm.ninstitutionsitecode=ist.ninstitutionsitecode"
				+ " and sm.ninstitutioncode =i.ninstitutioncode and i.ninstitutioncode=ist.ninstitutioncode  "
				+ " and d.ndistrictCode=" + ndistrictCode
				+ " and ist.ndistrictcode=d.ndistrictcode and sm.ninstitutioncode=" + ninstitutionCode
				+ " and sm.nstatus=ist.nstatus and ist.nstatus=i.nstatus and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ist.nregionalsitecode="
				+ objUserInfo.getNsitecode() + " group by  ist.sinstitutionsitename, ist.ninstitutionsitecode";
		return new ResponseEntity<>(jdbcTemplate.query(query, new InstitutionSite()), HttpStatus.OK);
	}
}
