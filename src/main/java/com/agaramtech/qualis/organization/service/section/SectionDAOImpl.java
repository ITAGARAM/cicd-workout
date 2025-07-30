package com.agaramtech.qualis.organization.service.section;

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

import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.organization.model.Section;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SectionDAOImpl implements SectionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;


	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available section(s) with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of section object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of section records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSection(final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsectioncode, s.ssectionname, s.sdescription, s.ndefaultstatus, s.nsitecode, s.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
				+ " from section s,transactionstatus ts where s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nsectioncode > 0 and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.ndefaultstatus=ts.ntranscode and s.nsitecode = " + userInfo.getNmastersitecode()
				+ " order by nsectioncode;";

		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Section()), HttpStatus.OK);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active section object based
	 * on the specified nsectionCode.
	 * @param nsectionCode [int] primary key of section object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public Section getActiveSectionById(final int nsectionCode,final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsectioncode, s.ssectionname, s.sdescription, s.ndefaultstatus, s.nsitecode, s.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
				+ " from section s,transactionstatus ts where s.ndefaultstatus=ts.ntranscode and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and s.nsectioncode = " + nsectionCode+" and nsitecode = "+userInfo.getNmastersitecode()+";";

		return (Section) jdbcUtilityFunction.queryForObject(strQuery, Section.class, jdbcTemplate);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to section table.
	 * @param objSection [Section] object holding details to be added in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added section object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSection(Section objSection,final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table section "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSectionList = new ArrayList<>();

		final Section objSectionName = getSectionListByName(objSection.getSsectionname(),objSection.getNsitecode());

		if (objSectionName == null) {
			if (objSection.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final Section defaultSection = getSectionByDefaultStatus(objSection.getNsitecode());

				if (defaultSection != null) {

					// Copy of object before update
					final Section sectionBeforeSave = SerializationUtils.clone(defaultSection);

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(sectionBeforeSave);

					defaultSection.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update section set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() +", dmodifieddate ='"
							+dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nsectioncode ="
							+ defaultSection.getNsectioncode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ " and nsitecode = "+userInfo.getNmastersitecode()+";";

					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultSection);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,Arrays.asList("IDS_EDITSECTION"), userInfo);
				}
			}

			String sequencenoquery ="select nsequenceno from seqnoorganisation where stablename ='section';";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);

			nsequenceno++;
			final String insertquery = "Insert into section (nsectioncode, ssectionname, sdescription, ndefaultstatus, dmodifieddate, nsitecode, nstatus) "
					+ "values("+nsequenceno+",N'"+stringUtilityFunction.replaceQuote(objSection.getSsectionname())+"',N'"+stringUtilityFunction.replaceQuote(objSection.getSdescription())+"',"
					+ objSection.getNdefaultstatus()+", '" +dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
					+ " "+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
			jdbcTemplate.execute(insertquery);

			final String updatequery ="update seqnoorganisation set nsequenceno ="+nsequenceno+" where stablename='section';";
			jdbcTemplate.execute(updatequery);

			objSection.setNsectioncode(nsequenceno);
			savedSectionList.add(objSection);

			multilingualIDList.add("IDS_ADDSECTION");

			auditUtilityFunction.fnInsertAuditAction(savedSectionList, 1, null, multilingualIDList, userInfo);

			return getSection(userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get a default Section object with respect to the site
	 * @param nmasterSiteCode [int] Site code
	 * @return a Section Object
	 * @throws Exception that are from DAO layer
	 */
	private Section getSectionByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select s.nsectioncode, s.ssectionname, s.sdescription, s.ndefaultstatus, s.nsitecode, s.nstatus from section s "
				+ " where s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and s.nsitecode = " + nmasterSiteCode+";";

		return (Section) jdbcUtilityFunction.queryForObject(strQuery, Section.class,jdbcTemplate);
	}


	/**
	 * This method is used to fetch the active section objects for the specified
	 * section name and site.
	 * @param sectionName     [String] section name for which the records are to be
	 *                        fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active sections based on the specified section name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Section getSectionListByName(final String sectionName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsectioncode from section where ssectionname = N'" + stringUtilityFunction.replaceQuote(sectionName)
		+ "' and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
		+ nmasterSiteCode;

		return (Section) jdbcUtilityFunction.queryForObject(strQuery, Section.class,jdbcTemplate);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in section table.
	 * @param objSection [Section] object holding details to be updated in section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated section object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSection(final Section objSection,final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		final Section checkSectionId = getActiveSectionById(objSection.getNsectioncode(), userInfo);

		if (checkSectionId == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsectioncode from section where ssectionname = '"
					+ stringUtilityFunction.replaceQuote(objSection.getSsectionname()) + "' and nsectioncode <> "
					+ objSection.getNsectioncode() + " and  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and nsitecode = "+userInfo.getNmastersitecode()+";";

			final List<Section> sectionList = (List<Section>) jdbcTemplate.query(queryString, new Section());

			if (sectionList.isEmpty()) {
				// if yes need to set other default Section as not a default Section
				if (objSection.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final Section defaultSection = getSectionByDefaultStatus(objSection.getNsitecode());

					if (defaultSection != null
							&& defaultSection.getNsectioncode() != objSection.getNsectioncode()) {

						// Copy of object before update
						final Section sectionBeforeSave = SerializationUtils.clone(defaultSection);
						listBeforeUpdate.add(sectionBeforeSave);

						defaultSection.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = "update section set ndefaultstatus="+ defaultSection.getNdefaultstatus() 
						+ ", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo) +"' where nsectioncode="
						+ defaultSection.getNsectioncode()+" and nsitecode = "+userInfo.getNmastersitecode()+";";

						jdbcTemplate.execute(updateQueryString);

						listAfterUpdate.add(defaultSection);
					}
				}
				final String updateQueryString = "update section set ssectionname='"
						+ stringUtilityFunction.replaceQuote(objSection.getSsectionname()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objSection.getSdescription()) + "', ndefaultstatus="
						+ objSection.getNdefaultstatus() + ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsectioncode=" + objSection.getNsectioncode() + " and nsitecode = "+userInfo.getNmastersitecode()+";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objSection);
				listBeforeUpdate.add(checkSectionId);

				multilingualIDList.add("IDS_EDITSECTION");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

				return getSection(userInfo);
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
	 * This service implementation method will access the DAO layer that is used to delete an entry in section table.
	 * @param objSection [Section] object holding detail to be deleted from section table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted section object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> deleteSection(final Section objSection,UserInfo userInfo) throws Exception {		

		final Section objSectionId = getActiveSectionById(objSection.getNsectioncode(),userInfo);

		if (objSectionId == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// deleteValidation
			final String query = "select 'IDS_ORGANISATION' as Msg from labsection where nsectioncode= "
					+ objSectionId.getNsectioncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNmastersitecode()+""
					+ " union all"
					+ " select 'IDS_TESTMASTER' as Msg from testsection where nsectioncode= "
					+ objSectionId.getNsectioncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+userInfo.getNmastersitecode()+"";

			 valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objSectionId.getNsectioncode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else {
					validRecord = false;
				}
			}

			if(validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedSectionList = new ArrayList<>();
				final String updateQueryString = "update section set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsectioncode="
						+ objSection.getNsectioncode()+" and nsitecode = "+userInfo.getNmastersitecode()+";";

				jdbcTemplate.execute(updateQueryString);

				objSection.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedSectionList.add(objSection);
				multilingualIDList.add("IDS_DELETESECTION");

				auditUtilityFunction.fnInsertAuditAction(deletedSectionList, 1, null, multilingualIDList, userInfo);

				return getSection(userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
