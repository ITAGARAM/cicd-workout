package com.agaramtech.qualis.quotation.service.quotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.contactmaster.model.ClientContactInfo;
import com.agaramtech.qualis.contactmaster.model.ClientSiteAddress;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
//import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.quotation.model.Quotation;
import com.agaramtech.qualis.quotation.model.QuotationTest;
import com.agaramtech.qualis.quotation.model.QuotationTotalAmount;
import com.agaramtech.qualis.quotation.model.QuotationType;
import com.agaramtech.qualis.quotation.model.QuotationVersionHistory;
import com.agaramtech.qualis.quotation.model.SeqnoQuotationManagement;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestPriceDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
/**
 *  This class is used to perform CRUD Operation on "quotation" table by implementing methods from its interface. 
 * @author ATE169
 * @version 9.0.0.1
 */
@AllArgsConstructor
@Repository
public class QuotationDAOImpl implements QuotationDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QuotationDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	//private static int dataBaseType = 0;
	
	/**
	 * This method is used to retrieve list of all active quotations for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotations
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getQuotation(UserInfo userInfo) throws Exception {
		
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		Quotation selectedQuotation = null;
		map.put("SelectedQuotation", selectedQuotation);
		int nquotationcode = -1;
		final ObjectMapper objMapper = new ObjectMapper();
		
		final String strQuery = "with quotation_data as"
				+ "("
				+ "select "
				+ "case when q.sdescription is null or q.sdescription='' then '-' else q.sdescription end as sdescription, "
				+ "case when q.sdeviationremarks is null or q.sdeviationremarks='' then '-' else q.sdeviationremarks end as sdeviationremarks, "
				+ "csa.sclientsitename,q.nquotationcode,q.nclientcatcode,q.nclientcode,q.nclientsitecode,q.nclientcontactcode, "
				+ "q.nproductcatcode,q.nproductcode,q.squotationno,q.dquotationdate,q.noffsetdquotationdate, "
				+ "q.ntzquotationdate,cc.sclientcatname,cl.sclientname,cci.scontactname as scontactname, "
				+ "case when cci.sphoneno is null or cci.sphoneno='' then '-' else cci.sphoneno end as sphoneno, "
				+ "case when cci.semail is null or cci.semail='' then '-' else cci.semail end as semail, "
				+ "to_char(q.dquotationdate,'"+userInfo.getSpgsitedatetime()+"') as squotationdate, "
				+ "pc.sproductcatname,qt.nquotationtypecode,qt.squotationname "
				+ "from "
				+ "quotation q,Clientcategory cc,client cl,clientsiteaddress csa,clientcontactinfo cci, "
				+ "quotationtype qt,productcategory pc "
				+ "where "
				+ " q.nclientcatcode = cc.nclientcatcode and cc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and cl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nclientcode = cl.nclientcode "
				+ " and cl.nclientcode=cci.nclientcode and cci.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and qt.nquotationtypecode=q.nquotationtypecode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nproductcatcode = pc.nproductcatcode and pc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and csa.nclientsitecode=cci.nclientsitecode and csa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nsitecode = "+ userInfo.getNtranssitecode()+" and cc.nsitecode = "+userInfo.getNmastersitecode()
				+" and cl.nsitecode = "+userInfo.getNmastersitecode()+" and csa.nsitecode = "+userInfo.getNmastersitecode()
				+" and cci.nsitecode = "+userInfo.getNmastersitecode()+" and qt.nsitecode = "+userInfo.getNmastersitecode()
				+" and pc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and q.nclientsitecode = csa.nclientsitecode and q.nclientcontactcode = cci.nclientcontactcode "
				+ " order by q.nquotationcode asc "
				+ " ),"
				+ " quotation_subdata as( "
				+ "	select case when o1.noemcode= -1 then '-' else o1.soemname end as soemnameview , case when o1.noemcode= -1 then '' else o1.soemname end as soemname,"
				+ " qon.nquotationcode,p.sproductname "
				+ " from oem o1,quotation qon,product p "
				+ "	 where o1.noemcode=qon.noemcode and o1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qon.nproductcode=p.nproductcode and p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ ")"
				+ " select * from ("
				+ " select qd.*,qsd.soemname,qsd.soemnameview,qsd.sproductname,qh.nquotationversioncode,ROW_NUMBER() OVER (PARTITION BY qh.nquotationcode order by qh.dtransactiondate desc) RN, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->> '"+userInfo.getSlanguagetypecode()+"') sversionstatus,qh.ntransactionstatus "
				+ " from quotation_data qd,quotationversionhistory qh ,transactionstatus ts,quotation_subdata qsd "
				+ " where ts.ntranscode = qh.ntransactionstatus and qsd.nquotationcode=qd.nquotationcode  and qsd.nquotationcode=qh.nquotationcode "
				+ " and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qh.nsitecode = "+ userInfo.getNtranssitecode()
				+ " and qh.nquotationcode=qd.nquotationcode and qh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") t where t.rn=1 ";
		
		LOGGER.info("Get Method:"+ strQuery);
		List<Quotation> lstQuotation = (List<Quotation>) jdbcTemplate.query(strQuery, new Quotation());
		objMapper.registerModule(new JavaTimeModule());

		final List<Quotation> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstQuotation,
						Arrays.asList("squotationdate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),new TypeReference<List<Quotation>>() {
				});
		map.put("Quotation", lstUTCConvertedDate);
		
		if (!lstQuotation.isEmpty()) {

			selectedQuotation = lstUTCConvertedDate.get(lstUTCConvertedDate.size() - 1);
			map.put("SelectedQuotation", selectedQuotation);
			
			nquotationcode=selectedQuotation.getNquotationcode();
			final List<QuotationTest> lstQuotationTest = (List<QuotationTest>) getQuotationTest(nquotationcode,userInfo).getBody();
			map.put("QuotationTest", lstQuotationTest);
			
			final List<QuotationTotalAmount> lstGrossQuotation = (List<QuotationTotalAmount>) getGrossQuotation(nquotationcode,userInfo).getBody();
			map.put("GrossQuotation", lstGrossQuotation);
			
			final List<QuotationVersionHistory> lstQuotationVersionHistory = (List<QuotationVersionHistory>) getQuotationHistory(nquotationcode,userInfo).getBody();
			map.put("QuotationHistory", lstQuotationVersionHistory);
		}	
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to quotation and quotationversionhistory tables.
	 * @param objQuotation [Quotation] object holding details to be added in quotation and quotationversionhistory tables
	 * @return inserted quotation object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> createQuotation(Quotation Quotation, UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> createQuotationrList = new ArrayList<>();

		final List<String> lstDateField = new ArrayList<String>();
		final List<String> lstDatecolumn = new ArrayList<String>();
	
		if (Quotation.getDquotationdate() != null) {
			Quotation.setSquotationdate(dateUtilityFunction.instantDateToString(Quotation.getDquotationdate()).replace("T", " ").replace("Z",""));
			lstDateField.add("squotationdate");
			lstDatecolumn.add("ntzquotationdate");
		}
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Quotation convertedObject = objmapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(Quotation, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<Quotation>() {
				});
		
		final String strQuery = "select noemcode from oem where noemcode= "+Quotation.getNoemcode()
			+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		//List<Quotation> activeOem = (List<Quotation>) jdbcTemplate.query(strQuery, new Quotation());
		List<Quotation> activeOem = jdbcTemplate.query(strQuery, new Quotation());

		if(activeOem.isEmpty()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_OEM",
					userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}else {
			
		final String getSeqNo = "select stablename,nsequenceno from seqnoquotationmanagement where stablename in (N'quotation',N'quotationversionhistory',N'quotationversion')";
		List<SeqnoQuotationManagement> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqnoQuotationManagement());
		Map<String, Integer> seqMap = lstSeqNo.stream().collect(
				Collectors.toMap(SeqnoQuotationManagement::getStablename, SeqnoQuotationManagement::getNsequenceno));
		int nquotationcode = seqMap.get("quotation");
		int nquotationhistorycode = seqMap.get("quotationversionhistory");
		int nquotationversioncode = seqMap.get("quotationversion");
		nquotationcode++;
		nquotationhistorycode++;
		nquotationversioncode++;
		
			String projectmasterInsert = "insert into quotation(nquotationcode,squotationno,nclientcatcode,nclientcode,nclientsitecode,nclientcontactcode,sclientsiteaddress,sinvoiceaddress,nquotationtypecode,nprojecttypecode,nprojectmastercode,"
					+ "sprojecttitle,noemcode,drfwdate,ntzrfwdate,noffsetdrfwdate,nproductcatcode,nproductcode,dquotationdate,ntzquotationdate,noffsetdquotationdate,sdescription,sdeviationremarks,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nquotationcode + ",'-',"+Quotation.getNclientcatcode() + ","+Quotation.getNclientcode()+","+Quotation.getNclientsitecode()+","+Quotation.getNclientcontactcode()+",N'"+stringUtilityFunction.replaceQuote(Quotation.getSclientsiteaddress())+"',N'"+stringUtilityFunction.replaceQuote(Quotation.getSinvoiceaddress())+"',"+Quotation.getNquotationtypecode()+","
					+ Quotation.getNprojecttypecode()+","+Quotation.getNprojectmastercode()+",N'"+stringUtilityFunction.replaceQuote(Quotation.getSprojecttitle())+"',"+Quotation.getNoemcode()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+convertedObject.getNtzrfwdate()+","+convertedObject.getNoffsetdrfwdate()+","+Quotation.getNproductcatcode()+","+Quotation.getNproductcode()+", "
					+ "'"+convertedObject.getSquotationdate()+"',"+convertedObject.getNtzquotationdate()+","+convertedObject.getNoffsetdquotationdate()+"," 
					+" N'"+stringUtilityFunction.replaceQuote(Quotation.getSdescription())+"',N'"+stringUtilityFunction.replaceQuote(Quotation.getSdeviationremarks())+"','"+dateUtilityFunction.getCurrentDateTime(userInfo)+ "'," 
					+ userInfo.getNtranssitecode() + ","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");  ";
					
			projectmasterInsert=projectmasterInsert+ "insert into quotationversion(nquotationversioncode, nquotationcode, squotationversionname, nsitecode, nstatus) "
					  + "values ("+ nquotationversioncode +","+nquotationcode+","+'1'+","
					  + userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"); ";
			
			projectmasterInsert=projectmasterInsert+ " insert into quotationversionhistory(nquotationhistorycode, nquotationcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,dmodifieddate, nsitecode, nstatus, nquotationversioncode) "
						  + "values ("+ nquotationhistorycode +","+nquotationcode+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","+userInfo.getNdeputyuserrole()+", "
						  + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+", '"
						  + dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+","+nquotationversioncode+");";
			
			jdbcTemplate.execute(projectmasterInsert);
			
			jdbcTemplate.execute(
					"update seqnoquotationmanagement set nsequenceno = " + nquotationhistorycode + " where stablename='quotationversionhistory'; "
							+ "update seqnoquotationmanagement set nsequenceno = " + nquotationversioncode + " where stablename='quotationversion';"
							+ "update seqnoquotationmanagement set nsequenceno = " + nquotationcode + " where stablename='quotation'");

			multilingualIDList.add("IDS_ADDQUOTATION");

			final String strnew="select  "
						+ "COALESCE(TO_CHAR(q.dquotationdate,'" + userInfo.getSsitedate() +"'),'') as squotationdate from Quotation q where q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
						+ "q.nquotationcode="+nquotationcode+" and q.nsitecode = "+ userInfo.getNtranssitecode()+"";
			
		    List<Quotation> Quotationlist = jdbcTemplate.query(strnew, new Quotation());
		    Quotation.setSquotationdate(Quotationlist.get(0).getSquotationdate());
		    Quotation.setNquotationcode(nquotationcode);
		    createQuotationrList.add(Quotation);
			auditUtilityFunction.fnInsertAuditAction(createQuotationrList, 1, null, multilingualIDList, userInfo);
			return getQuotation(userInfo);
		}
		 
	}
	
	/**
	 * This method is used to update entry in quotation table.
	 * Need to validate that the quotation object to be updated is active before updating details in database.
	 * @param objQuotation [Quotation] object holding details to be updated in quotation table
	 * @return response entity object holding response status and data of updated quotation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type", "unused" })
	public ResponseEntity<Object> updateQuotation(Quotation quotation, UserInfo userInfo) throws Exception {

		final List<Object> beforeSavedQuotationList = new ArrayList<>();
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final List<Object> afterQuotationList = new ArrayList<>();
		
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();


		final String strold="select  "
				+ "COALESCE(TO_CHAR(q.dquotationdate,'" + userInfo.getSsitedate() +"'),'') as squotationdate from Quotation q where q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ "q.nquotationcode="+quotation.getNquotationcode()+" and q.nsitecode = "+ userInfo.getNtranssitecode()+"";
	
        List<Quotation> Quotationlistold = jdbcTemplate.query(strold, new Quotation());
    
		final ResponseEntity<Object> quotationResponseEntity = (ResponseEntity<Object>) getActiveQuotationById(quotation.getNquotationcode(), userInfo);


		if (quotationResponseEntity == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {
			
			
			final List<String> lstDateField = new ArrayList<String>();
			final List<String> lstDatecolumn = new ArrayList<String>();

			if (quotation.getDquotationdate() != null) {
				quotation.setSquotationdate(dateUtilityFunction.instantDateToString(quotation.getDquotationdate()).replace("T", " ").replace("Z",""));
				lstDateField.add("squotationdate");
				lstDatecolumn.add("ntzquotationdate");
			}
			
			final ObjectMapper objmapper = new ObjectMapper();
			objmapper.registerModule(new JavaTimeModule());
			final Quotation convertedObject = objmapper.convertValue(
					dateUtilityFunction.convertInputDateToUTCByZone(quotation, lstDateField, lstDatecolumn, true, userInfo),
					new TypeReference<Quotation>() {
					});
			
			objMap = (Map<List, Object>) quotationResponseEntity.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			objquotation.setSquotationdate(Quotationlistold.get(0).getSquotationdate());
			
			if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String strQuery = "select noemcode from oem where noemcode= "+quotation.getNoemcode()
						+" and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				//List<Quotation> activeOem = (List<Quotation>) jdbcTemplate.query(strQuery, new Quotation());
				List<Quotation> activeOem = jdbcTemplate.query(strQuery, new Quotation());
				if(activeOem.isEmpty()) {
				
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_OEM",
									userInfo.getSlanguagefilename())+" "+commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}else {
				

				final String query = "update quotation set nclientcatcode=" + quotation.getNclientcatcode()+",nproductcatcode="+quotation.getNproductcatcode()+",nproductcode="+quotation.getNproductcode()+",noemcode="+quotation.getNoemcode()+", "
							+ "nclientcode=" + quotation.getNclientcode() + ",sclientsiteaddress='"
							+ stringUtilityFunction.replaceQuote(quotation.getSclientsiteaddress()) + "',nclientsitecode="+quotation.getNclientsitecode()+",nclientcontactcode="+quotation.getNclientcontactcode()+", " 
							+ "nquotationtypecode="+quotation.getNquotationtypecode()+",nprojecttypecode="
							+ quotation.getNprojecttypecode() + ",nprojectmastercode=" + quotation.getNprojectmastercode() + ", "
							+ "dquotationdate='" +convertedObject.getSquotationdate()+"',sdescription='"
							+ stringUtilityFunction.replaceQuote(quotation.getSdescription()) + "',sdeviationremarks='"
							+ stringUtilityFunction.replaceQuote(quotation.getSdeviationremarks()) +"',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
                            + "' where nquotationcode=" + quotation.getNquotationcode() +" and nsitecode = "+ userInfo.getNtranssitecode()+ " ";

					jdbcTemplate.execute(query);

					beforeSavedQuotationList.add(objquotation);
					
					final ResponseEntity<Object> selectedquotation = getActiveQuotationById(quotation.getNquotationcode(), userInfo);
					if(selectedquotation != null) {
					objMap = (Map<List, Object>) selectedquotation.getBody();
					objquotation = (Quotation) objMap.get("SelectedQuotation");
					
					
					String strnewnew="select  "
							+ "COALESCE(TO_CHAR(q.dquotationdate,'" + userInfo.getSsitedate() +"'),'') as squotationdate from Quotation q where q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
							+ "q.nquotationcode="+quotation.getNquotationcode()+" and q.nsitecode = "+ userInfo.getNtranssitecode()+" ";
				
					List<Quotation> Quotationlistnew = jdbcTemplate.query(strnewnew, new Quotation());
					objquotation.setSquotationdate(Quotationlistnew.get(0).getSquotationdate());
			    
					afterQuotationList.add(objquotation);
					auditUtilityFunction.fnInsertAuditAction(afterQuotationList, 2, beforeSavedQuotationList, Arrays.asList("IDS_EDITQUOTATION"), userInfo);
					map.put("SelectedQuotation", objMap.get("SelectedQuotation"));
					return new ResponseEntity<Object>(map, HttpStatus.OK);
					}
					return getQuotation(userInfo);

				}	  
					
			} else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		}

	}
	
	/**
	 * This method is used to retrieve active quotation object based on the specified nquotationCode.
	 * @param nquotationCode [int] primary key of quotation object
	 * @return response entity  object holding response status and data of quotation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveQuotationById(final int nquotationcode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		Quotation Quotation = new Quotation();

		final String strQuery = "with quotation_data as"
				+ "("
				+ "select "
				+ "case when q.sdescription is null or q.sdescription='' then '-' else q.sdescription end as sdescription, "
				+ "case when q.sdeviationremarks is null or q.sdeviationremarks='' then '-' else q.sdeviationremarks end as sdeviationremarks, "
				+ "case when q.noemcode=-1 then '' else o.soemname end as soemname,"
				+ "case when q.noemcode=-1 then '-' else o.soemname end as soemnameview, csa.sclientsitename,q.nquotationcode,q.nclientcatcode,q.nclientcode,q.nclientsitecode,q.nclientcontactcode,q.noemcode, "
				+ "q.nproductcatcode,q.nproductcode,q.squotationno,q.dquotationdate,q.noffsetdquotationdate, "
				+ "q.ntzquotationdate,cc.sclientcatname,cl.sclientname,cci.scontactname as scontactname, "
				+ "case when cci.sphoneno is null or cci.sphoneno='' then '-' else cci.sphoneno end as sphoneno, "
				+ "case when cci.semail is null or cci.semail='' then '-' else cci.semail end as semail,p.sproductname, "
				+ "to_char(q.dquotationdate,'"+userInfo.getSpgsitedatetime()+"') as squotationdate, "
				+ "pc.sproductcatname,qt.nquotationtypecode,qt.squotationname "
				+ "from "
				+ "quotation q,Clientcategory cc,client cl,clientsiteaddress csa,clientcontactinfo cci, "
				+ "quotationtype qt,product p,productcategory pc,oem o "
				+ "where "
				+ " q.nclientcatcode = cc.nclientcatcode and cc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and cl.nclientcatcode = cc.nclientcatcode and cl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nclientcode = cl.nclientcode "
				+ " and cl.nclientcode=cci.nclientcode and cci.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nproductcode=p.nproductcode and p.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and qt.nquotationtypecode=q.nquotationtypecode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nproductcatcode = pc.nproductcatcode and pc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and o.noemcode=q.noemcode and o.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and csa.nclientsitecode=cci.nclientsitecode and csa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and q.nsitecode = "+ userInfo.getNtranssitecode()+" and cc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and cl.nsitecode = "+userInfo.getNmastersitecode()+" and csa.nsitecode = "+userInfo.getNmastersitecode()
				+ " and cci.nsitecode = "+userInfo.getNmastersitecode()+" and qt.nsitecode = "+userInfo.getNmastersitecode()
				+ " and p.nsitecode = "+userInfo.getNmastersitecode()+" and pc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and o.nsitecode = "+userInfo.getNmastersitecode()
				+ " and q.nclientsitecode=csa.nclientsitecode and q.nclientcontactcode=cci.nclientcontactcode "
				+ " and q.nquotationcode="+nquotationcode+" "
				+ " ) select * from ("
				+ " select qd.*,qh.nquotationversioncode,ROW_NUMBER() OVER (PARTITION BY qh.nquotationcode order by qh.dtransactiondate desc) RN, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->> '"+userInfo.getSlanguagetypecode()+"') sversionstatus,qh.ntransactionstatus "
				+ " from quotation_data qd,quotationversionhistory qh ,transactionstatus ts "
				+ " where ts.ntranscode = qh.ntransactionstatus and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qh.nsitecode = "+ userInfo.getNtranssitecode()
				+ " and qh.nquotationcode=qd.nquotationcode and qh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") t where t.rn=1 ";
		
		
		//List<Quotation> lstQuotation = (List<Quotation>) jdbcTemplate.query(strQuery, new Quotation());
		List<Quotation> lstQuotation = jdbcTemplate.query(strQuery, new Quotation());
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<Quotation> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstQuotation,
						Arrays.asList("squotationdate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<Quotation>>() {
				});
		
		if(lstUTCConvertedDate.size()>0) {
			
			map.put("SelectedQuotation", lstUTCConvertedDate.get(0));
			
		}else {
			
			map.put("SelectedQuotation", null);
			
		}
		
	    Quotation = (Quotation) map.get("SelectedQuotation");
		
		if (Quotation == null) {
			return null;
		} 
		
		final List<QuotationTest> lstQuotationTest = (List<QuotationTest>) getQuotationTest(nquotationcode,userInfo).getBody();
		map.put("QuotationTest", lstQuotationTest);
		
		final List<QuotationTotalAmount> lstGrossQuotation = (List<QuotationTotalAmount>) getGrossQuotation(nquotationcode,userInfo).getBody();
		map.put("GrossQuotation", lstGrossQuotation); 
		
		final List<QuotationVersionHistory> lstQuotationVersionHistory = (List<QuotationVersionHistory>) getQuotationHistory(nquotationcode,userInfo).getBody();
		map.put("QuotationHistory", lstQuotationVersionHistory);
		
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	/**
	 * This method id used to delete an entry in quotation table
	 * Need to check the record is already deleted or not
	 * @param objQuotation [Quotation] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an quotation object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unlikely-arg-type", "rawtypes" })
	public ResponseEntity<Object> deleteQuotation(Quotation quotation, UserInfo userInfo) throws Exception {

		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		
		final ResponseEntity<Object> QuotationById = getActiveQuotationById(quotation.getNquotationcode(), userInfo);

		if (QuotationById == null) {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			
			objMap = (Map<List, Object>) QuotationById.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");

			final String strnew="select  "
					+ "COALESCE(TO_CHAR(q.dquotationdate,'" + userInfo.getSsitedate() +"'),'') as squotationdate from Quotation q where q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
					+ "q.nquotationcode="+quotation.getNquotationcode()+" and q.nsitecode = "+ userInfo.getNtranssitecode()+"";
		
			List<Quotation> Quotationlist = jdbcTemplate.query(strnew, new Quotation());
//	   
			objquotation.setSquotationdate(Quotationlist.get(0).getSquotationdate());
			
			if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				 String deleteQuery ="update quotation set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nquotationcode = " + quotation.getNquotationcode() +" and nsitecode = "+ userInfo.getNtranssitecode()+ "; ";

				 deleteQuery = deleteQuery+"update quotationversionhistory set dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nquotationcode = " + quotation.getNquotationcode() + " and nsitecode = "+ userInfo.getNtranssitecode()+"; ";
				 
				 deleteQuery = deleteQuery+"update quotationtest set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + "nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nquotationcode = " + quotation.getNquotationcode() + " and nsitecode = "+ userInfo.getNtranssitecode()+"; ";
				 
				 deleteQuery = deleteQuery+"update quotationtotalamount set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nquotationcode = " + quotation.getNquotationcode() +" and nsitecode = "+ userInfo.getNtranssitecode()+ "; ";
								
				jdbcTemplate.execute(deleteQuery);
				final List<Object> deletedQuotationList =new ArrayList<Object>();
				final List<Object> deletedQuotationTestList =new ArrayList<Object>();

				deletedQuotationTestList.add(objMap.get("SelectedQuotation"));
				deletedQuotationList.add(objMap.get("QuotationTest"));
				deletedQuotationList.add(deletedQuotationTestList);
				deletedQuotationList.add(objMap.get("GrossQuotation"));

				
  			 auditUtilityFunction.fnInsertListAuditAction(deletedQuotationList, 1, null, Arrays.asList("IDS_DELETEQUOTATIONTESTPRICE","IDS_DELETEQUOTATION","IDS_DELETEDISCOUNTANDVATCALCULATION"), userInfo);
				
				return getQuotation(userInfo);

			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		}

	}
	
	/**
	 * This method is used to delete entry in quotation, quotationversionhistory, quotationversion tables. 
	 * Need to validate that the specified quotation object is active.
	 * @param quotation [Quotation] object holding detail to be approved in quotation table
	 * @return response entity object holding response status
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> approveQuotation(Quotation quotation, UserInfo userInfo)
			throws Exception {

		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		Quotation objquotation = new Quotation();

		final List<Object> auditList = new ArrayList<Object>();
		final List<String> multilingualIDList = new ArrayList<String>();
		int nquotationcode = quotation.getNquotationcode();
		
		final ResponseEntity<Object> QuotationById = (ResponseEntity<Object>) getActiveQuotationById(quotation.getNquotationcode(), userInfo);


		if (QuotationById == null) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			
			
			final String strquotationtestamount = "select case when sum(ntotalamount) is null then 0 else 1 end as ntotalamount from quotationtest where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nquotationcode="+nquotationcode+" and nsitecode = "+ userInfo.getNtranssitecode()+"";
			int count = jdbcTemplate.queryForObject(strquotationtestamount, Integer.class);
						
			if (count > 0) {
				
			
				objMap = (Map<List, Object>) QuotationById.getBody();
				objquotation = (Quotation) objMap.get("SelectedQuotation");
	
				if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()) {
					
					final String strformat = projectDAOSupport.getSeqfnFormat("quotation", "seqnoformatgeneratorquotation",0,0, userInfo);
					
					final String getSeqNo = "select nsequenceno from seqnoquotationmanagement where stablename in (N'quotationversionhistory',N'quotationversion') order by stablename";
					List<SeqnoQuotationManagement> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqnoQuotationManagement());
					int nquotationhistorycode = lstSeqNo.get(1).getNsequenceno();
					int nquotationversioncode = quotation.getNquotationversioncode();
					nquotationhistorycode++;
					
					String updateQuery = "update quotation set dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ",squotationno='"+strformat+"' where nquotationcode="+quotation.getNquotationcode()+" and nsitecode = "+ userInfo.getNtranssitecode()+"; ";
	
					updateQuery=updateQuery+ "insert into quotationversionhistory(nquotationhistorycode, nquotationcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,dmodifieddate, nsitecode, nstatus, nquotationversioncode) "
							  + "values ("+ nquotationhistorycode +","+nquotationcode+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","+userInfo.getNdeputyuserrole()+", "
							  + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+", '"
							  + dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+","+nquotationversioncode+"); ";
					
				    updateQuery =updateQuery+ "update seqnoquotationmanagement set nsequenceno = " + nquotationhistorycode + " where stablename='quotationversionhistory'; ";
				    
				    jdbcTemplate.execute(updateQuery);
					
					quotation.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
					quotation.setSquotationno((String)strformat);
					
					
					final String strnew="select  "
							+ "COALESCE(TO_CHAR(q.dquotationdate,'" + userInfo.getSsitedate() +"'),'') as squotationdate from Quotation q where q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
							+ "q.nquotationcode="+nquotationcode+" and q.nsitecode = "+ userInfo.getNtranssitecode()+"";
				
					List<Quotation> Quotationlist = jdbcTemplate.query(strnew, new Quotation());
		   
					quotation.setSquotationdate(Quotationlist.get(0).getSquotationdate());
			    
					auditList.add(quotation);
					multilingualIDList.add("IDS_APPROVEQUOTATION");
					
					auditUtilityFunction.fnInsertAuditAction(auditList, 1, null, multilingualIDList, userInfo);
	
					final ResponseEntity<Object> selectedquotation = getActiveQuotationById(nquotationcode,userInfo);
					objMap = (Map<List, Object>) selectedquotation.getBody();
					map.put("SelectedQuotation", objMap.get("SelectedQuotation"));
					map.put("QuotationHistory", objMap.get("QuotationHistory"));
					return new ResponseEntity<Object>(map, HttpStatus.OK);
				
	
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
						.gettransactionstatus()) {
	
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}else {
			
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDTEST", 
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}}
	}

	/**
	 * This method is used to retrieve list of all active quotationtype for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtype
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationType(UserInfo userInfo) throws Exception {
		
		final String strQuery = "select * from quotationtype where nquotationtypecode>0 and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		// status code:200
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new QuotationType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active quotationtest for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtest
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unused", "unchecked", "rawtypes", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> getQuotationUnmappedTest(Integer nquotationcode, UserInfo userInfo) throws Exception {
		
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		
		final ResponseEntity<Object> QuotationById = (ResponseEntity<Object>) getActiveQuotationById(nquotationcode, userInfo);


		if (QuotationById == null) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			
			objMap = (Map<List, Object>) QuotationById.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			
			if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				final String queryString ="select tm.ntestcode,tm.stestsynonym from testmaster tm where ntestcode  not in "
						+ " (select qt.ntestcode from quotationtest qt where  qt.nquotationcode=" +nquotationcode
						+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
						+ " and tm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and  tm.ntestcode >-1 order by stestsynonym";

			    final List<TestMaster> quotationtestList = jdbcTemplate.query(queryString, new TestMaster());
			    return new ResponseEntity<>(quotationtestList, HttpStatus.OK);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} 
		
		

	}

	/**
	 * This method is used to retrieve active quotationtest object based on the specified nquotationCode.
	 * @param nquotationCode [int] primary key of quotationtest object
	 * @return response entity  object holding response status and data of quotationtest object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<QuotationTest> getTestByQuotation(final int nquotationcode,int nsitecode) throws Exception {

		final String strQuery = "Select * from quotationtest where nquotationcode = " + nquotationcode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+nsitecode +"";
		//return (List<QuotationTest>) jdbcTemplate.query(strQuery, new QuotationTest());
		return jdbcTemplate.query(strQuery, new QuotationTest());
	}
	
	/**
	 * This method is used to add a new entry to quotationtest table.
	 * @param quotationTest [quotationtest] object holding details to be added in quotation table
	 * @return inserted quotationtest object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes", "unused", "unlikely-arg-type" })
	public ResponseEntity<Object> createQuotationTest(List<QuotationTest> quotationTest, UserInfo userInfo)
			throws Exception {
		
			final Map<String, Object> map = new LinkedHashMap<String, Object>();
			final List<Object> savedQuotationList = new ArrayList<>();	
			String quotationInsert="";
			
			Map<List, Object> objMap = new LinkedHashMap<List, Object>();
			Quotation objquotation = new Quotation();

			List<QuotationTest> lstQuotationTest = getTestByQuotation(quotationTest.get(0).getNquotationcode(),userInfo.getNtranssitecode());
				
			List<QuotationTest> filteredList = quotationTest.stream().filter(
					source -> lstQuotationTest.stream().noneMatch(check -> source.getNtestcode() == check.getNtestcode()))
					.collect(Collectors.toList());
		
			final String categorycodevalue = filteredList.stream().map(x -> String.valueOf(x.getNtestcode()))
					.collect(Collectors.joining(","));
			
			final ResponseEntity<Object> QuotationById = (ResponseEntity<Object>) getActiveQuotationById(quotationTest.get(0).getNquotationcode(), userInfo);

			if (QuotationById == null ) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else {
			
				objMap = (Map<List, Object>) QuotationById.getBody();
				objquotation = (Quotation) objMap.get("SelectedQuotation");
				
				if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
	
						if (filteredList != null && filteredList.size() > 0) {
					
							final String testCode = stringUtilityFunction.fnDynamicListToString(quotationTest, "getNtestcode");
							
							final String str="select nsequenceno from seqnoquotationmanagement where stablename in('quotationtest')";
							//List<Map<String, Object>> lstData = getJdbcTemplate().queryForList(str);
							List<Map<String, Object>> lstData = jdbcTemplate.queryForList(str);
							Integer seqquotationtest =(Integer) lstData.get(0).get("nsequenceno");
							
							
							final String strTestPriceQuery = "select tpd.npriceversioncode,tpd.ncost,tpd.ntestcode from testpricedetail tpd,testpriceversion tpv where "
									+ "tpd.npriceversioncode=tpv.npriceversioncode and tpv.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
									+ "and tpd.ntestcode in ("+categorycodevalue+") and tpd.ncost>0 and tpd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+" and tpv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpd.nsitecode = "+ userInfo.getNmastersitecode()+ " and tpv.nsitecode = "+ userInfo.getNmastersitecode()+"";
							
							//List<TestPriceDetail> lstTestPrice=(List<TestPriceDetail>) findBySinglePlainSql(strTestPriceQuery,TestPriceDetail.class);
							List<TestPriceDetail> lstTestPrice=jdbcTemplate.query(strTestPriceQuery, new TestPriceDetail());
							List<QuotationTest> filteredTestmasterList = quotationTest.stream().filter(
									source -> lstTestPrice.stream().noneMatch(check -> source.getNtestcode() == check.getNtestcode()))
									.collect(Collectors.toList());
							
						    final String filteredTestmasterstr = filteredTestmasterList.size()>0 ? filteredTestmasterList.stream().map(x -> String.valueOf(x.getNtestcode()))
									.collect(Collectors.joining(",")) : "0";
							
						    final String filteredTestpricestr = lstTestPrice.size()>0 ? lstTestPrice.stream().map(x -> String.valueOf(x.getNtestcode()))
									.collect(Collectors.joining(",")) : "0";
							
						    Integer sequnencenotestprice = lstTestPrice.size()>0 ? lstTestPrice.size() : 0;
			
							
						   quotationInsert = "insert into quotationtest(nquotationtestcode,nquotationcode,ntestcode,ncost,nnoofsamples,ntotalamount,snotes,stestplatform,squotationleadtime,nsitecode,dmodifieddate,nstatus)"
									+ "(select " + seqquotationtest + " +rank()over(order by tpd.ntestcode)as nquotationtestcode,"
									+ quotationTest.get(0).getNquotationcode() + ",tpd.ntestcode as ntestcode,tpd.ncost,1,(tpd.ncost*1) ntotalamount,"+"''"+" as snotes,stestplatform,"+"''"+" as squotationleadtime, "
									+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from  testpricedetail tpd,testpriceversion tpv,testmaster tm "
									+ " where tpd.npriceversioncode=tpv.npriceversioncode and tpv.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and tpd.ntestcode=tm.ntestcode and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and tpd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpv.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
									+ " tpd.nsitecode = "+userInfo.getNmastersitecode()+ " and tpv.nsitecode = "+userInfo.getNmastersitecode()+ " and tm.nsitecode = "+userInfo.getNmastersitecode()
									+ " and tpd.ntestcode in("+filteredTestpricestr+"));";
							 
							
						   Integer sequnenceno = seqquotationtest + sequnencenotestprice;
							
						quotationInsert =quotationInsert+" insert into quotationtest(nquotationtestcode,nquotationcode,ntestcode,ncost,nnoofsamples,ntotalamount,snotes,stestplatform,squotationleadtime,nsitecode,dmodifieddate,nstatus)"
									+ "(select " + sequnenceno + " +rank()over(order by ntestcode)as nquotationtestcode,"
									+ quotationTest.get(0).getNquotationcode() + ",ntestcode as ntestcode,ncost,1,(ncost*1) ntotalamount,"+"''"+" as snotes,stestplatform,"+"''"+" as squotationleadtime, "
									+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from  testmaster where ntestcode in("
									+ filteredTestmasterstr + ") and nsitecode = "+userInfo.getNmastersitecode()+");";
							 
							sequnenceno = seqquotationtest + filteredList.size();
							 
							quotationInsert =quotationInsert+ "update seqnoquotationmanagement set nsequenceno ="+ sequnenceno+" where stablename='quotationtest';";
							//getJdbcTemplate().execute(quotationInsert);
							jdbcTemplate.execute(quotationInsert);
							
							final String insertedQuery = "select q.nclientcatcode,q.nclientcode,q.nclientsitecode,q.nclientcontactcode,dib.sdiscountbandname,case when qt.ntotalamount>0 then qt.ntotalamount else 0 end as ntotalamount,tm.stestsynonym,qt.* "
							             + "from quotation q,quotationtest qt,discountband dib,testmaster tm "
								                 +" where q.nquotationcode ="+quotationTest.get(0).getNquotationcode()
												 +" and qt.ntestcode in("+testCode+") "
												 //ALPD-959
												 +" and q.nquotationcode=qt.nquotationcode "
												 +" and q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
												 +" and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												 +" and q.nsitecode = "+ userInfo.getNtranssitecode()+" and qt.nsitecode = "+ userInfo.getNtranssitecode()
												 +" and dib.ndiscountbandcode=qt.ndiscountbandcode and dib.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												 +" and qt.ntestcode = tm.ntestcode and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
							final List<QuotationTest> quotationtestList = jdbcTemplate.query(insertedQuery, new QuotationTest());

							savedQuotationList.add(quotationtestList); 

					        auditUtilityFunction.fnInsertListAuditAction(savedQuotationList, 1, null, Arrays.asList("IDS_ADDQUOTATIONTEST"), userInfo);
								
					}
					}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
						
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}else {
						
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
			}
						
     		final ResponseEntity<Object> selectedquotation = getActiveQuotationById(quotationTest.get(0).getNquotationcode(),userInfo);
			objMap = (Map<List, Object>) selectedquotation.getBody();
			
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			final String str="update quotationtotalamount set ndiscountbandcode="+objquotation.getNdiscountbandcode()+",nvatbandcode="+objquotation.getNvatbandcode()+",ndiscountamount="+objquotation.getNdiscountamount()+",nvatamount="+objquotation.getNvatamount()+", "
	      			  +"ntotalgrossamount ="+objquotation.getNtotalgrossamount()+",ntotalnetamount="+objquotation.getNtotalnetamount()+",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' where nquotationcode="+quotationTest.get(0).getNquotationcode()+" and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
	      	jdbcTemplate.execute(str);
	      	
			map.put("SelectedQuotation", objMap.get("SelectedQuotation"));
			map.put("QuotationTest", objMap.get("QuotationTest"));
			map.put("GrossQuotation", objMap.get("GrossQuotation"));
			return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve list of all active quotationtest for the specified nquotationcode.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationtest
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> getQuotationTest(int nquotationcode, UserInfo objUserInfo) throws Exception {

			final String quotationQuery = " select tm.stestsynonym,case when (qt.squotationleadtime is null) or qt.squotationleadtime='' then '-' else qt.squotationleadtime end as squotationleadtime,case when (qt.stestplatform is null) or qt.stestplatform='' then '-' else qt.stestplatform "
					+ " end as stestplatform, case when (dib.sdiscountbandname is null) or dib.sdiscountbandname='NA' then '-'  "
					+ " else dib.sdiscountbandname end as sdiscountbandname,case when qt.snotes is null or qt.snotes='' then '-' else qt.snotes end as snotes,case when m.smethodname is null or m.smethodname='' then '-' else "
					+ " m.smethodname end as smethodname,qt.*,m.nmethodcode "
					+ " from quotationtest qt, discountband dib, "
					+ " testmaster tm left join testmethod tme on tme.ntestcode = tm.ntestcode and tme.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tme.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and tme.nsitecode="+objUserInfo.getNmastersitecode()+" and tm.ntransactionstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " left join method m  on tme.nmethodcode=m.nmethodcode and m.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
					+ " where qt.ntestcode = tm.ntestcode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.nsitecode= "+objUserInfo.getNmastersitecode()+" and qt.nsitecode= "+objUserInfo.getNtranssitecode()+" and dib.nsitecode= "+objUserInfo.getNmastersitecode()+" and dib.ndiscountbandcode=qt.ndiscountbandcode  "
					//+ " and m.nsitecode = "+objUserInfo.getNmastersitecode()
					+ " and dib.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nquotationcode="+nquotationcode+"; ";
				return new ResponseEntity<>(jdbcTemplate.query(quotationQuery, new QuotationTest()), HttpStatus.OK);
	
	}

	/**
	 * This method is used to retrieve list of all active grossquotation for the specified nquotationcode.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active grossquotation
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getGrossQuotation(int nquotationcode, UserInfo objUserInfo) throws Exception {

		final String grossquotationQuery = "select "
				+ " ((sum(qt.ntotalamount)*coalesce(dib.namount,0))/100) as ndiscountamount, "
				+ "((sum(ntotalamount)*coalesce(vab.namount,0))/100) as nvatamount,sum(ntotalamount) as ntotalgrossamount, "
				+ "(sum(ntotalamount)+coalesce(qta.nvatamount,0))-coalesce(qta.ndiscountamount,0) as ntotalnetamount, "
				+ "coalesce(dib.ndiscountbandcode,-1) as ndiscountbandcode,coalesce(vab.nvatbandcode,-1)as nvatbandcode , "
				+ "vab.namount as nvatpercentage, case when (dib.sdiscountbandname is null) or dib.sdiscountbandname='NA' then '-' else dib.sdiscountbandname end as sdiscountbandname, "
				+ "case when (vab.svatbandname is null) or vab.svatbandname='NA' then '-' else vab.svatbandname end as svatbandname, "
				+ "dib.namount as ndiscountpercentage "
				+ " from "
				+ " quotation q left join quotationtest qt on q.nquotationcode = qt.nquotationcode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ " left join   quotationtotalamount qta on q.nquotationcode = qta.nquotationcode  and qta.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " left join discountband dib on qta.ndiscountbandcode=dib.ndiscountbandcode and dib.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ " left join  vatband vab on  qta.nvatbandcode=vab.nvatbandcode and vab.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
				+ " where q.nquotationcode="+nquotationcode+" and q.nsitecode = "+objUserInfo.getNtranssitecode()+" and qt.nsitecode = "+objUserInfo.getNtranssitecode()+" and qta.nsitecode = "+objUserInfo.getNtranssitecode()+" "
				+ " and dib.nsitecode = "+objUserInfo.getNmastersitecode()+" and vab.nsitecode = "+objUserInfo.getNmastersitecode()
				+ " group by dib.namount,vab.namount,qta.nvatamount,qta.ndiscountamount,dib.ndiscountbandcode,vab.nvatbandcode ";

		return new ResponseEntity<>(jdbcTemplate.query(grossquotationQuery, new QuotationTotalAmount()), HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve list of all active quotationprice for the specified nquotationcode and nquotationtestcode.
	 * @param userInfo [UserInfo] primary key of nquotationcode and nquotationtestcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationprice
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unlikely-arg-type", "rawtypes", "unused" })
	@Override
	public ResponseEntity<Object> getQuotationPrice(int nquotationcode, Integer nquotationtestcode, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		
		final ResponseEntity<Object> QuotationById = (ResponseEntity<Object>) getActiveQuotationById(nquotationcode, userInfo);

		if (QuotationById == null) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			
			objMap = (Map<List, Object>) QuotationById.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			
			if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
	
				final String strcount = "select count(0) from quotationtest where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nquotationcode="+nquotationcode+"";
				int count = jdbcTemplate.queryForObject(strcount, Integer.class);
				
				if (count > 0) {
					
					if(nquotationtestcode == null) {
						
						final String priceListQuery = " select tm.stestsynonym,case when (qt.stestplatform is null) or qt.stestplatform='' then '-' else tm.stestplatform "
									+ " end as stestplatform,case when (qt.snotes is null) or qt.snotes='' then '-' else qt.snotes end as snotes, case when (dib.sdiscountbandname is null) or dib.sdiscountbandname='NA' then '-'  "
									+ " else dib.sdiscountbandname end as sdiscountbandname,qt.*,m.nmethodcode,m.smethodname,dib.namount from quotationtest qt, discountband dib, "
									+ " testmaster tm left join testmethod tme on tme.ntestcode = tm.ntestcode and tme.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and tme.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and tme.nsitecode="+userInfo.getNmastersitecode()+" and tm.ntransactionstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " left join method m  on tme.nmethodcode=m.nmethodcode and m.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
									+ " where qt.ntestcode = tm.ntestcode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nsitecode = "+userInfo.getNtranssitecode()+" and tm.nsitecode= "+userInfo.getNmastersitecode()+" and dib.ndiscountbandcode=qt.ndiscountbandcode  "
									+ " and dib.nsitecode = "+userInfo.getNmastersitecode()//+" and m.nsitecode = "+userInfo.getNmastersitecode()
									+ " and dib.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nquotationcode="+nquotationcode+"; ";

						
						final List<QuotationTest> priceList = jdbcTemplate.query(priceListQuery,new QuotationTest());
						outputMap.put("QuotationPrice", priceList);
					}
					else { 
						
						final String sQuery = " select tm.stestsynonym,case when (qt.stestplatform is null) or qt.stestplatform='' then '-' else qt.stestplatform "
									+ " end as stestplatform,case when (qt.squotationleadtime is null) or qt.squotationleadtime='' then '-' "
									+ " else qt.squotationleadtime end as squotationleadtime,case when (qt.snotes is null) or qt.snotes='' then '-' else qt.snotes end as snotes, case when (dib.sdiscountbandname is null) or dib.sdiscountbandname='NA' then ''  "
									+ " else dib.sdiscountbandname end as sdiscountbandname,qt.*,m.nmethodcode,m.smethodname,dib.namount from quotationtest qt, discountband dib, "
									+ " testmaster tm left join testmethod tme on tme.ntestcode = tm.ntestcode and tme.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " and tme.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and tme.nsitecode="+userInfo.getNmastersitecode()+" and tm.ntransactionstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									+ " left join method m  on tme.nmethodcode=m.nmethodcode and m.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
									+ " where qt.ntestcode = tm.ntestcode and qt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nsitecode = "+userInfo.getNtranssitecode()+" and tm.nsitecode= "+userInfo.getNmastersitecode()+" and dib.ndiscountbandcode=qt.ndiscountbandcode  "
									+ " and dib.nsitecode = "+userInfo.getNmastersitecode()//+" and m.nsitecode = "+userInfo.getNmastersitecode()
									+ " and dib.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nquotationtestcode="+nquotationtestcode+"; ";

						
						final QuotationTest testPriceById = (QuotationTest) jdbcUtilityFunction.queryForObject(sQuery, QuotationTest.class, jdbcTemplate);
						if(testPriceById != null) {
							outputMap.put("SelectedQuotationPrice", testPriceById);
						}else {
							String alert = commonFunction.getMultilingualMessage("IDS_TEST", userInfo.getSlanguagefilename());
							return new ResponseEntity<>(alert+" "+commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
						}
					}
					return new ResponseEntity<>(outputMap,HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDTEST",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

			
			
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		
	}

	/**
	 * This method is used to update entry in quotationtest  table.
	 * Need to validate that the quotationtest object to be updated is active before updating details in database.
	 * @param quotationTestList [quotationTest] object holding details to be updated in quotationtest table
	 * @return response entity object holding response status and data of updated quotationtest object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	public ResponseEntity<Object> updateQuotationTest(List<QuotationTest> quotationTestList, int nquotationcode,
			UserInfo userInfo) throws Exception {
		
	
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		Quotation objquotation = new Quotation();
		
		final List<Object> savedList = new ArrayList<>();
		
		final ResponseEntity<Object> QuotationById = getActiveQuotationById(quotationTestList.get(0).getNquotationcode(), userInfo);

			
		if (QuotationById == null) {
				
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} 
			else
			{
				objMap = (Map<List, Object>) QuotationById.getBody();
				objquotation = (Quotation) objMap.get("SelectedQuotation");
				
				if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) 
				{
					
					final String testCode = stringUtilityFunction.fnDynamicListToString(quotationTestList, "getNtestcode");
					
					
					final String insertedQuery = "select case when m.nmethodcode is null then -1 else m.nmethodcode end, case when m.smethodname is null then '-' else m.smethodname end ,qt.*,q.nprojectmastercode, q.nclientcode,q.squotationno,q.nclientcatcode,q.nclientsitecode,q.nclientcontactcode  "
							+ "from quotation q,quotationtest qt left join testmethod tm "
							+ "on tm.ntestcode = qt.ntestcode and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qt.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and "
							+ "tm.nsitecode="+userInfo.getNsitecode()+"  and qt.ntestcode in("+testCode+") left join method m on tm.nmethodcode=m.nmethodcode and m.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " where qt.nquotationcode =q.nquotationcode and q.nsitecode = "+userInfo.getNtranssitecode()+" and qt.nsitecode = "+userInfo.getNtranssitecode()+" and qt.nquotationcode ="+nquotationcode+" order by qt.nquotationtestcode desc";

					
					final List<QuotationTest> beforeTestList = jdbcTemplate.query(insertedQuery, new QuotationTest());
	
					final List<Object> beforeUpdateList = new ArrayList<>();		
					beforeUpdateList.add(beforeTestList);	
					
					String queryString = ""; 
					for(final QuotationTest testPrice : quotationTestList) { 
						
						queryString = queryString + ";update quotationtest set ncost = " + testPrice.getNcost()+",nnoofsamples="+testPrice.getNnoofsamples()+",ntotalamount="+testPrice.getNtotalamount()+",ndiscountbandcode="+testPrice.getNdiscountbandcode()+", "	
											 + "stestplatform='"+stringUtilityFunction.replaceQuote(testPrice.getStestplatform())+"',squotationleadtime='"+stringUtilityFunction.replaceQuote(testPrice.getSquotationleadtime())
											 +"',snotes='"+stringUtilityFunction.replaceQuote(testPrice.getSnotes())+"',dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
											 +"' where nquotationtestcode = " + testPrice.getNquotationtestcode()+" and nsitecode = "+userInfo.getNtranssitecode();
				}
					
					
			jdbcTemplate.execute(queryString);	
					
		
					savedList.add(quotationTestList);	
					final List<String> multiLingualIDList = new ArrayList<String>();
					multiLingualIDList.add("IDS_EDITQUOTATIONTESTPRICE");
					
					auditUtilityFunction.fnInsertListAuditAction(savedList, 2, beforeUpdateList, multiLingualIDList, userInfo);
					
					final ResponseEntity<Object> selectedquotation = getActiveQuotationById(quotationTestList.get(0).getNquotationcode(),userInfo);
					objMap = (Map<List, Object>) selectedquotation.getBody();
					
					List<QuotationTotalAmount> lstQuotationTotalAmount=(List<QuotationTotalAmount>) objMap.get("GrossQuotation");
					  
					if(!lstQuotationTotalAmount.isEmpty()) {
					final String str="update quotationtotalamount set ndiscountbandcode="+lstQuotationTotalAmount.get(0).getNdiscountbandcode()+",nvatbandcode="+lstQuotationTotalAmount.get(0).getNvatbandcode()+",ndiscountamount="+lstQuotationTotalAmount.get(0).getNdiscountamount()+",nvatamount="+lstQuotationTotalAmount.get(0).getNvatamount()+", "
			      			  +"ntotalgrossamount ="+lstQuotationTotalAmount.get(0).getNtotalgrossamount()+",ntotalnetamount="+lstQuotationTotalAmount.get(0).getNtotalnetamount()+",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' where nquotationcode="+quotationTestList.get(0).getNquotationcode()
			      			  +" and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			      	jdbcTemplate.execute(str);
					}
			      	
					map.put("SelectedQuotation", objMap.get("SelectedQuotation"));
					map.put("QuotationTest", objMap.get("QuotationTest"));
					map.put("GrossQuotation", objMap.get("GrossQuotation"));
					return new ResponseEntity<Object>(map, HttpStatus.OK);
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",	
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
			}
	}
	
	/**
	 * This method is used to retrieve list of all active quotationgrossamount for the specified nquotationcode.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationgrossamount
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	@Override
	public ResponseEntity<Object> getQuotationGrossAmount(int nquotationcode, UserInfo objUserInfo) throws Exception {

		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		
		final ResponseEntity<Object> QuotationById = getActiveQuotationById(nquotationcode, objUserInfo);

			
		if (QuotationById == null) {
				
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} 
			else
			{
				objMap = (Map<List, Object>) QuotationById.getBody();
				objquotation = (Quotation) objMap.get("SelectedQuotation");
				
				if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) 
				{
					
					final String strtotalamount = "select case when sum(ntotalamount) is null then 0 else 1 end as ntotalamount from quotationtest where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = "+objUserInfo.getNtranssitecode()+" and nquotationcode="+nquotationcode+"";
					int count = jdbcTemplate.queryForObject(strtotalamount, Integer.class);
					
					if (count > 0) {
						
							final String quotationQuery = "select a.* from (select qta.nquotationtotalamountcode,q.nquotationcode,qta.ndiscountbandcode,qta.nvatbandcode, "
								+ "(select (sum(ntotalamount)*coalesce(dbd.namount,0))/100 from quotationtest q3 where q3.nquotationcode=q.nquotationcode "
								+ "and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and q3.nsitecode = "+objUserInfo.getNtranssitecode()
								+" ) as ndiscountamount, "
								+ "(select (sum(ntotalamount)*coalesce(vbd.namount,0))/100 from quotationtest q4 where q4.nquotationcode=q.nquotationcode and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and q4.nsitecode = "+objUserInfo.getNtranssitecode()
								+") as nvatamount,(select "
								+ "(sum(ntotalamount)+coalesce(qta.nvatamount,0))-coalesce(qta.ndiscountamount,0) from quotationtest q2 where "
								+ "q2.nquotationcode=q.nquotationcode and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and q2.nsitecode = "+objUserInfo.getNtranssitecode()
								+") as ntotalnetamount,(select sum(ntotalamount) from quotationtest q1 where "
								+ "q1.nquotationcode=q.nquotationcode and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+" and q1.nsitecode = "+objUserInfo.getNtranssitecode()
								+") as ntotalgrossamount, "
								+ "dbd.namount as ndiscountpercentage,dbd.sdiscountbandname,vbd.svatbandname,vbd.namount as nvatpercentage "
								+ "from quotationtest q left join quotationtotalamount qta on q.nquotationcode = qta.nquotationcode  and q.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"  and qta.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ "left join discountband dbd on dbd.ndiscountbandcode = qta.ndiscountbandcode and dbd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ "left join vatband vbd on vbd.nvatbandcode = qta.nvatbandcode and vbd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" "
								+ "where q.nquotationcode="+nquotationcode
								+" and q.nsitecode = "+objUserInfo.getNtranssitecode()
								+" group by qta.nquotationtotalamountcode,ndiscountpercentage,nvatpercentage,dbd.sdiscountbandname,vbd.svatbandname,q.nquotationcode  ,qta.nvatamount,qta.ndiscountamount,qta.ndiscountbandcode,qta.nvatbandcode) a ";

						
						return new ResponseEntity<>(jdbcTemplate.query(quotationQuery, new QuotationTotalAmount()), HttpStatus.OK);
					}else {
						
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDTESTANDPRICE",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
					
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}else {
					
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",	
							objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}
				}					
	}

	/**
	 * This method is used to add a new entry to quotationtotalamount table.
	 * @param GrossQuotation [GrossQuotation] object holding details to be added in quotationtotalamount table
	 * @return inserted quotationtotalamount object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings({ "unchecked", "unlikely-arg-type", "rawtypes", "unused" })
	public ResponseEntity<Object> createGrossQuotation(QuotationTotalAmount GrossQuotation, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedGrossQuotationList = new ArrayList<>();
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		
		final ResponseEntity<Object> QuotationById = getActiveQuotationById(GrossQuotation.getNquotationcode(), userInfo);

		if (QuotationById == null) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			
			objMap = (Map<List, Object>) QuotationById.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			
		  if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
	
			
			final String strcount = "select count(0) from quotationtotalamount where nquotationcode="+GrossQuotation.getNquotationcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
            int count = jdbcTemplate.queryForObject(strcount, Integer.class);

			final String insertQuery = "select qta.*,q.nprojectmastercode,q.nclientcode,q.squotationno,q.nclientcatcode,q.nclientsitecode,q.nclientcontactcode from quotation q left join quotationtotalamount qta on q.nquotationcode = qta.nquotationcode "
							  +"and q.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qta.nstatus= "
							  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and qta.nsitecode = "+userInfo.getNtranssitecode()
							  +" where q.nquotationcode ="+GrossQuotation.getNquotationcode()+" and q.nsitecode = "+userInfo.getNtranssitecode()
							  +"";

            final List<QuotationTotalAmount> beforeSavedQuotationList = jdbcTemplate.query(insertQuery, new QuotationTotalAmount());

            if(count>0) {
            	
            	final String str="update quotationtotalamount set ndiscountbandcode="+GrossQuotation.getNdiscountbandcode()+",nvatbandcode="+GrossQuotation.getNvatbandcode()+",ndiscountamount="+GrossQuotation.getNdiscountamount()+",nvatamount="+GrossQuotation.getNvatamount()+", "
            			  +"ntotalgrossamount ="+GrossQuotation.getNtotalgrossamount()+",ntotalnetamount="+GrossQuotation.getNtotalnetamount()+",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)
            			  +"' where nquotationcode="+GrossQuotation.getNquotationcode()+" and nsitecode = "+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
            	jdbcTemplate.execute(str);
            	
            }else {
            
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoquotationmanagement where stablename='quotationtotalamount'", Integer.class);
			nSeqNo++;

			final String GrossQuotationInsert = "insert into quotationtotalamount(nquotationtotalamountcode,nquotationcode,ndiscountbandcode,nvatbandcode,ndiscountamount,nvatamount,ntotalgrossamount,"
					+ "ntotalnetamount,nsitecode,dmodifieddate,nstatus)"
					+ "values(" + nSeqNo + "," + GrossQuotation.getNquotationcode()+","+GrossQuotation.getNdiscountbandcode()+","+GrossQuotation.getNvatbandcode()+","+GrossQuotation.getNdiscountamount()+","+GrossQuotation.getNvatamount()+","
					+ GrossQuotation.getNtotalgrossamount()+","+GrossQuotation.getNtotalnetamount()+","
					+ userInfo.getNtranssitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					

			jdbcTemplate.execute(GrossQuotationInsert);
			jdbcTemplate.execute(
					"update seqnoquotationmanagement set nsequenceno = " + nSeqNo + " where stablename='quotationtotalamount'");

			GrossQuotation.setNquotationtotalamountcode(nSeqNo);
            }
			savedGrossQuotationList.add(GrossQuotation);
			multilingualIDList.add("IDS_EDITDISCOUNTANDVATCALCULATION");			
			auditUtilityFunction.fnInsertAuditAction(savedGrossQuotationList, 2, beforeSavedQuotationList, multilingualIDList, userInfo);
			
			final ResponseEntity<Object> selectedquotation = getActiveQuotationById(GrossQuotation.getNquotationcode(),userInfo);
			objMap = (Map<List, Object>) selectedquotation.getBody();
			map.put("SelectedQuotation", objMap.get("SelectedQuotation"));    	
			map.put("GrossQuotation", objMap.get("GrossQuotation")); 
			return new ResponseEntity<Object>(map, HttpStatus.OK);
			
		}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}}
		
	}
	
	/**
	 * This method id used to delete an entry in quotationtest table
	 * Need to check the record is already deleted or not
	 * @param quotationtestPrice [quotationTestPrice] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an quotationtest object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unlikely-arg-type" })
	public ResponseEntity<Object> deleteQuotationPrice(final QuotationTest quotationtestPrice, 
			final UserInfo userInfo) throws Exception{ 	
		
		Map<List, Object> objMap = new LinkedHashMap<List, Object>();
		Quotation objquotation = new Quotation();
		final Map<String, Object> map = new LinkedHashMap<String, Object>();

		final ResponseEntity<Object> QuotationById = getActiveQuotationById(quotationtestPrice.getNquotationcode(), userInfo);

		if (QuotationById == null) {
			
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else
		{
			objMap = (Map<List, Object>) QuotationById.getBody();
			objquotation = (Quotation) objMap.get("SelectedQuotation");
			
			if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) 
			{
				final List<String> multilingualIDList = new ArrayList<String>();
				final List<Object> savedList = new ArrayList<>();
				
	
				String sQuery = "select * from quotationtest where nstatus = "
									 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									 + " and nquotationtestcode = "+quotationtestPrice.getNquotationtestcode()+" and nsitecode = "+userInfo.getNtranssitecode()+""; 
				final QuotationTest quotationtestPriceById = (QuotationTest) jdbcUtilityFunction.queryForObject(sQuery,  QuotationTest.class,jdbcTemplate);
				
				if(quotationtestPriceById == null) 
				{
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED", 
							userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);				
				}
				else 
				{						
					sQuery = "update quotationtest set nstatus = "+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "' where nquotationtestcode = " +quotationtestPrice.getNquotationtestcode()+" and nsitecode = "+userInfo.getNtranssitecode()+"";
					jdbcTemplate.execute(sQuery);
					
					quotationtestPrice.setNstatus((short)Enumeration.TransactionStatus.DELETED.gettransactionstatus());
					
					savedList.add(quotationtestPrice);
					multilingualIDList.add("IDS_DELETEQUOTATIONTESTPRICE");
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
					
					final ResponseEntity<Object> selectedquotation = getActiveQuotationById(quotationtestPrice.getNquotationcode(),userInfo);
					objMap = (Map<List, Object>) selectedquotation.getBody();
					map.put("SelectedQuotation", objMap.get("SelectedQuotation"));
					map.put("QuotationTest", objMap.get("QuotationTest"));
					return new ResponseEntity<Object>(map, HttpStatus.OK);
					
				}			
	
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYAPPROVED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}else if (objquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDALREADYRETIRED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDMUSTBEDRAFTSTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
	/**
	 * This method is used to retrieve list of all active quotationhistory for the specified nquotationcode.
	 * @param userInfo [UserInfo] primary key of nquotationcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active quotationhistory
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ResponseEntity<Object> getQuotationHistory(int nquotationcode, UserInfo userInfo) throws Exception  {
		
		
		final String queryformat="TO_CHAR(qh.dtransactiondate,'" + userInfo.getSpgsitedatetime()+ "') ";
		
		final String historyQuery = "select qh.nquotationhistorycode, qh.ntransactionstatus, qh.nusercode, qh.nuserrolecode, qh.nsitecode, "
				+ ""+queryformat+" as sdtransactiondate,"
				+ "CONCAT( u.sfirstname,' ',u.slastname) as susername, ur.suserrolename, "
				+ " ur.suserrolename, coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "	 ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus , qh.sretireremarks "
				+ " from quotationversionhistory qh, users u, userrole ur, transactionstatus ts"
				+ " where u.nusercode = qh.nusercode and ur.nuserrolecode = qh.nuserrolecode and ts.ntranscode = qh.ntransactionstatus"
				+ " and u.nstatus = qh.nstatus and ur.nstatus = qh.nstatus and ts.nstatus = qh.nstatus"
				+ " and qh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ur.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nsitecode = "+userInfo.getNmastersitecode()+" and ur.nsitecode = "+userInfo.getNmastersitecode()
				+ " and qh.nsitecode = "+userInfo.getNtranssitecode()+" and qh.nquotationcode = " + nquotationcode;
		
		List<?> listQuotationHistory =dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(historyQuery, new QuotationVersionHistory()),
				Arrays.asList("sdtransactiondate"),
				null, userInfo, true, Arrays.asList("stransdisplaystatus"), false);
						
		return new ResponseEntity<Object>(listQuotationHistory, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active product for the specified nproductcatcode.
	 * @param userInfo [UserInfo] primary key of nproductcatcode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active product
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductByCategory(int nproductcatcode, UserInfo userInfo) throws Exception {
		final Map<String,Object> map = new LinkedHashMap<String,Object>();

		final String strQuery = "select * from product where nproductcatcode="+nproductcatcode+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+"";
		
		List<Product> lstProduct=jdbcTemplate.query(strQuery, new Product());
		map.put("Product", lstProduct);
		return new ResponseEntity<>( map,HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active clientcategory for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active clientcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getClientCategory(UserInfo userInfo) throws Exception {
		
		final String strQuery = "select c.nclientcatcode, c.sclientcatname, c.sdescription, c.ndefaultstatus, c.nsitecode, c.nstatus, ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"' as sdisplaystatus from clientcategory c, "
                +"transactionstatus ts,client ct where c.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
                +" and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
                +" and c.nclientcatcode<>-1 and ts.ntranscode=c.ndefaultstatus and c.nsitecode = "+userInfo.getNmastersitecode()+" and ct.nsitecode = "+userInfo.getNmastersitecode()
                +" and ct.nclientcatcode=c.nclientcatcode and ct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by c.nclientcatcode,sdisplaystatus";
		
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ClientCategory()),HttpStatus.OK);	
	}

	/**
	 * This method is used to retrieve list of all active productcategory for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active productcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductCategory(UserInfo userInfo) throws Exception {
		
		final String strQuery = "select p.*,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as scategorybasedflow,"
				+ "COALESCE(ts1.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts1.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,"
				+ "to_char(p.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate "
				+ " from productcategory p,transactionstatus ts,transactionstatus ts1,product pt where p.ncategorybasedflow=ts.ntranscode and ts1.ntranscode=p.ndefaultstatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode ="
				+ userInfo.getNmastersitecode() + " and pt.nsitecode = "+userInfo.getNmastersitecode()+" and pt.nproductcatcode=p.nproductcatcode and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and p.nproductcatcode >0 group by p.nproductcatcode,scategorybasedflow,sdisplaystatus";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ProductCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active client for the specified nclientCode.
	 * @param userInfo [UserInfo] primary key of nclientCode object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active client
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("null")
	@Override
	public ResponseEntity<Object> getSelectedClientDetail(UserInfo userInfo, int nclientCode) throws Exception {
		
	
		Map<String,Object> objMap = new LinkedHashMap<String, Object>();
		
		final String strSiteQuery = " select cd.nclientsitecode,cd.nclientcode,cd.ncountrycode,cd.sclientsitename, "
				+ "cd.saddress1,cd.saddress2,cd.saddress3,cd.ndefaultstatus,cd.nstatus,c.scountryname, "
				+ "coalesce(ts1.jsondata->'sactiondisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"', "
				+ "ts1.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus "
				+ "from clientsiteaddress cd,country c, transactionstatus ts1 where ts1.ntranscode = cd.ndefaultstatus "
				+ "and cd.ncountrycode = c.ncountrycode and ts1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and cd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and c.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and cd.nsitecode = "+userInfo.getNmastersitecode()+" and c.nsitecode = "+userInfo.getNmastersitecode()
				+ " and cd.nclientcode = " + nclientCode+" order by cd.nclientsitecode ";
		
		//List<ClientSiteAddress> lstClientSite= (List<ClientSiteAddress>) jdbcTemplate.query(strSiteQuery, new ClientSiteAddress());
		List<ClientSiteAddress> lstClientSite= jdbcTemplate.query(strSiteQuery, new ClientSiteAddress());
		objMap.put("ClientSite", lstClientSite);	
		
		
		lstClientSite = lstClientSite.stream().filter(x->x.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()).collect(Collectors.toList());
		int nclientSiteCode = lstClientSite.isEmpty()?-1:lstClientSite.get(0).getNclientsitecode();
		
		final String strContactQuery = " select cc.nclientcontactcode,cc.nclientsitecode,cc.nclientcode,cc.scontactname,cc.sphoneno,cc.smobileno,"
				+ " cc.semail,cc.sfaxno,cc.scomments,cc.ndefaultstatus,cc.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdefaultContact"
				+ " from clientcontactinfo cc, clientsiteaddress cd, transactionstatus ts where cc.ndefaultstatus = ts.ntranscode and  cc.nclientsitecode = cd.nclientsitecode and "
				+ " cd.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cc.nsitecode = "+userInfo.getNmastersitecode()+" and cd.nsitecode = "+userInfo.getNmastersitecode()+" and "
				+ "  cc.nstatus = cd.nstatus  and cc.nstatus = ts.nstatus and cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nclientcode = " + nclientCode + " and cc.nclientsitecode = " + nclientSiteCode+ " order by cc.nclientcontactcode";

		//List<ClientContactInfo> lstClientContactInfo= (List<ClientContactInfo>) jdbcTemplate.query(strContactQuery, new ClientContactInfo());
		List<ClientContactInfo> lstClientContactInfo= jdbcTemplate.query(strContactQuery, new ClientContactInfo());
		objMap.put("ClientContact", lstClientContactInfo);	

		return new ResponseEntity<Object>(objMap,HttpStatus.OK);	
    }
	
	/**
	 * This method id used to retire an entry in quotation table
	 * Need to check the record is already retired or not
	 * @param approvedquotation an Object holds the record to be retired
	 * @param approvedquotation Date is checked with Retired quotation Date
	 * @return a response entity with corresponding HTTP status and an quotation object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> retireQuotation(Quotation retireQuotation, UserInfo userInfo)throws Exception {

		final List<Object> auditList = new ArrayList<Object>();
		final List<String> multilingualIDList = new ArrayList<String>();
		final Map<String, Object> map = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		int nquotationcode = retireQuotation.getNquotationcode();	
		final Quotation approvedquotation = getActiveApprovedStatusByID(retireQuotation.getNquotationcode(),retireQuotation.getNquotationversioncode() ,userInfo);		
		if (approvedquotation == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else if(approvedquotation.getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()){
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYRETIRED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			
			if ((int)approvedquotation.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				if(retireQuotation.getDtransactiondate().isBefore(approvedquotation.getDtransactiondate())!= true) {
				final String getSeqNo = "select nsequenceno from seqnoquotationmanagement where stablename in (N'quotationversionhistory',N'quotationversion') order by stablename";
				List<SeqnoQuotationManagement> lstSeqNo = jdbcTemplate.query(getSeqNo, new SeqnoQuotationManagement());
				int nquotationhistorycode = lstSeqNo.get(1).getNsequenceno();
				nquotationhistorycode++;				
				String updateQuery = "update quotation set dmodifieddate ='" +dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ",squotationno=N'"+ stringUtilityFunction.replaceQuote(retireQuotation.getSquotationno())
								+"' where nquotationcode="+ retireQuotation.getNquotationcode()+" and nsitecode = "+userInfo.getNtranssitecode()+";";
					
				updateQuery=updateQuery+ "insert into quotationversionhistory(nquotationhistorycode, nquotationcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,dmodifieddate, nsitecode, nstatus,sretireremarks,nquotationversioncode) "
						  + "values ("+ nquotationhistorycode +","+nquotationcode+","+userInfo.getNusercode()+","+userInfo.getNuserrole()+","+userInfo.getNdeputyusercode()+","+userInfo.getNdeputyuserrole()+", "
						  + Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+",'"+retireQuotation.getDtransactiondate()+"',"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+", '"
						  +dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ userInfo.getNtranssitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+",N'"+stringUtilityFunction.replaceQuote(retireQuotation.getSretireremarks())+"',"+retireQuotation.getNquotationversioncode()+"); ";
				
				updateQuery =updateQuery+ "update seqnoquotationmanagement set nsequenceno = " + nquotationhistorycode + " where stablename='quotationversionhistory'; ";
				jdbcTemplate.execute(updateQuery);
				approvedquotation.setNtransactionstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
				objMapper.registerModule(new JavaTimeModule());
				final Quotation quotationObj = objMapper.convertValue(approvedquotation, Quotation.class);
				auditList.add(quotationObj);
				multilingualIDList.add("IDS_RETIREQUOTATION");
				auditUtilityFunction.fnInsertAuditAction(auditList, 1, null, multilingualIDList, userInfo);
				
				final Quotation selectedQuotationObj = getActiveApprovedStatusByID(retireQuotation.getNquotationcode(),retireQuotation.getNquotationversioncode(), userInfo);
				map.put("SelectedQuotation", selectedQuotationObj);
				final List<QuotationVersionHistory> lstQuotationVersionHistory = (List<QuotationVersionHistory>) getQuotationHistory(nquotationcode,userInfo).getBody();		
				map.put("QuotationHistory", lstQuotationVersionHistory);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATEGREATERTHANAPPROVEDATE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDQUOTATION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
	/**
	 * This method is used to retrieve active max quotation object based on the specified nquotationCode  and nquotationversioncode, .
	 * @param nquotationCode [int] primary key of quotation object
	 * @param nquotationversioncode [int] primary key of quotationversion object
	 * max record of specified nquotationcode and nquotationversioncode is fetched from quotationversionhistory table 
	 * @return response entity  object holding response status and data of quotation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	private Quotation getActiveApprovedStatusByID(int nquotationcode,int nquotationversioncode, UserInfo userInfo) throws Exception {
	
		final String strQuery = "select qvh.nquotationcode , qvh.ntransactionstatus,qvh.nquotationversioncode, q.squotationno,cc.sclientcatname, cl.sclientname,cci.scontactname ,qvh.dtransactiondate, o.soemname, "
				+ "case when q.sdescription is null or q.sdescription='' then '-' else q.sdescription end as sdescription, "
				+ "case when q.sdeviationremarks is null or q.sdeviationremarks='' then '-' else q.sdeviationremarks end as sdeviationremarks, "
				+ "case when o.noemcode= -1 then '-' else o.soemname end as soemnameview , case when o.noemcode= -1 then '' else o.soemname end as soemname,"
				+ "case when cci.sphoneno is null or cci.sphoneno='' then '-' else cci.sphoneno end as sphoneno, "
				+ "case when cci.semail is null or cci.semail='' then '-' else cci.semail end as semail, "
				+ "to_char(q.dquotationdate,'"+userInfo.getSpgsitedatetime()+"') as squotationdate, "
				+ "pc.sproductcatname ,p.sproductname ,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->> '"+userInfo.getSlanguagetypecode()+"') sversionstatus from quotationversionhistory qvh,quotationversion qv, quotation q ,transactionstatus ts ,client cl,clientcategory cc,clientcontactinfo cci, oem o,productcategory pc, product p "
				+ " where ts.ntranscode = qvh.ntransactionstatus and qvh.nquotationcode = qv.nquotationcode"
				+ " and cc.nclientcatcode = q.nclientcatcode and cl.nclientcode = q.nclientcode and cci.nclientcontactcode = q.nclientcontactcode "
				+ " and	o.noemcode = q.noemcode and pc.nproductcatcode = q.nproductcatcode and p.nproductcode = q.nproductcode "
				+ " and cl.nclientcode = q.nclientcode  and qvh.nquotationcode = q.nquotationcode "
				+ " and qvh.nquotationversioncode= qv.nquotationversioncode and qv.nquotationversioncode = "+ nquotationversioncode + " and cl.nclientcode = q.nclientcode "
				+ " and qvh.nquotationcode = q.nquotationcode and qvh.nquotationcode ="+ nquotationcode +" and qvh.nquotationhistorycode = any(select max(nquotationhistorycode) from quotationversionhistory where nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and nsitecode="+userInfo.getNtranssitecode() +" group by nquotationversioncode)"
				+" and q.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and qv.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and qvh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cl.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and q.nsitecode = "+userInfo.getNtranssitecode()+" and qv.nsitecode = "+userInfo.getNtranssitecode()+" and qvh.nsitecode = "+userInfo.getNtranssitecode()
				+ " and cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and cci.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and o.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cl.nsitecode = "+userInfo.getNmastersitecode()
				+ " and cc.nsitecode = "+userInfo.getNmastersitecode()+" and cci.nsitecode = "+userInfo.getNmastersitecode()
				+ " and o.nsitecode = "+userInfo.getNmastersitecode()+" and pc.nsitecode = "+userInfo.getNmastersitecode()
				+ " and p.nsitecode = "+userInfo.getNmastersitecode()+";";
		
		return (Quotation) jdbcUtilityFunction.queryForObject(strQuery, Quotation.class, jdbcTemplate);
		 
	}

}
