package com.agaramtech.qualis.externalorder.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationTest;

@Service
public class ExternalOrderServiceImpl implements ExternalOrderService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOrderServiceImpl.class);
	
	private ExternalOrderDAO externalOrderDAO;
		
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param unitService UnitService
	 */
	public ExternalOrderServiceImpl(ExternalOrderDAO externalOrderDAO) {
		super();
		this.externalOrderDAO = externalOrderDAO;
	}
		
	@Override
	public ResponseEntity<Object> getExternalOrder(final int nexternalOrderCode,UserInfo userInfo) throws Exception {
		return externalOrderDAO.getExternalOrder(nexternalOrderCode,userInfo);
	}
	
	public ResponseEntity<Object> getExternalOrderClickDetails(final int nportalordercode,final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getExternalOrderClickDetails(nportalordercode,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> createExternalOrder(ExternalOrder objOrderDetail,int needfilter,UserInfo userInfo) throws Exception {
		
		Map<String,Object>  returnMap = new HashMap();
		try {
			return externalOrderDAO.createExternalOrder(objOrderDetail, needfilter,userInfo);
	
		}
		catch(Exception e)
		{
			LOGGER.info("failure cause--> "+e.getLocalizedMessage());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),e.getLocalizedMessage());
			return new ResponseEntity<> (returnMap,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@Override
	public ResponseEntity<Object> receivedExternalOrder(ExternalOrder objOrderDetail,UserInfo userInfo) throws Exception {
		return externalOrderDAO.receivedExternalOrder(objOrderDetail,userInfo);
	}

	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getProductCategory(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getComponent(userInfo);
	}
	@Override
	public ResponseEntity<Object> getTestMaster(final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getTestMaster(userInfo);
	}
	
	public ResponseEntity<Object> getDiagnosticcase(final UserInfo userInfo) throws Exception
	{
		return externalOrderDAO.getDiagnosticcase(userInfo);
	}
	
	public ResponseEntity<Object> getPriority(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getPriority(userInfo);
	}
	
	public ResponseEntity<Object> getTestPackageTest(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getTestPackageTest(userInfo);
	}
	
	public ResponseEntity<Object> getTestGroupProfile(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getTestGroupProfile(userInfo);
	}

	public ResponseEntity<Object> updatePortalOrderStatus(final String sorderseqno,final String sordersampleno, final short ntransactionstatus,final UserInfo userinfo) throws Exception{
		return externalOrderDAO.updatePortalOrderStatus(sorderseqno,sordersampleno,ntransactionstatus,userinfo);
	}

	@Override
	public ResponseEntity<Object> getActiveExternalOrderById(int nexternalordercode, UserInfo userInfo) throws Exception {
		return externalOrderDAO.getActiveExternalOrderById(nexternalordercode,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> updateExternalOrder(ExternalOrder externalorder,int needfilter, UserInfo userInfo) throws Exception {
		return externalOrderDAO.updateExternalOrder(externalorder, needfilter,userInfo);
	}
	
	public ResponseEntity<Object> getDraftExternalOrderDetails(final String sexternalorderid,final UserInfo userinfo,final int nexteralordertypecode) throws Exception{
		
		return externalOrderDAO.getDraftExternalOrderDetails(sexternalorderid,userinfo,nexteralordertypecode);
	}
	
	public ResponseEntity<Object> onUpdateCancelExternalOrder(final String sexternalordercode,final String sexternalordersamplecode,final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception{
		
		return externalOrderDAO.onUpdateCancelExternalOrder(sexternalordercode,sexternalordersamplecode,inputMap,userinfo);
	}
	
	public ResponseEntity<Object> outSourceTest(final RegistrationTest testInput, final int destinationSitecode,
			final int designTemplateMappingCode, final UserInfo userInfo) throws Exception
	{
		return externalOrderDAO.outSourceTest(testInput, destinationSitecode, designTemplateMappingCode, userInfo);
	}
		
	public ResponseEntity<Object> getOutSourceSite(final RegistrationTest testInput, final UserInfo userInfo) throws Exception {
		 return externalOrderDAO.getOutSourceSite(testInput, userInfo);		
	}
	
	public ResponseEntity<Object> getOutSourceSiteAndTest(final RegistrationTest testInput, final UserInfo userInfo) throws Exception {
		 return externalOrderDAO.getOutSourceSiteAndTest(testInput, userInfo);		
	}

	@Override
	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {	
		return externalOrderDAO.getRegion(userInfo);
	}
	
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getTestCategory(userInfo);
	}
	
	public ResponseEntity<Object> getCity(UserInfo userInfo) throws Exception{
		return externalOrderDAO.getCity(userInfo);
	}
	
	public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception{
		return externalOrderDAO.getDistrict(userInfo);
	}
	
	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getCountry(userInfo);
	}
	public ResponseEntity<Object> getSubmitterDetailByAll(Map<String, Object> inputMap, final UserInfo userInfo)throws Exception{
		return externalOrderDAO.getSubmitterDetailByAll(inputMap, userInfo);
	}
	
	public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getInstitutionSitebyAll(userInfo);
	}
	
	public ResponseEntity<Object> getInstitutionValues(UserInfo userInfo) throws Exception {
		return externalOrderDAO.getInstitutionValues(userInfo);
	}
	
	public ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getTestPackage(userInfo);
	}
	
	public ResponseEntity<Object> getInstitutionDepartment(UserInfo userInfo) throws Exception {
		return externalOrderDAO.getInstitutionDepartment(userInfo);
	}
	
	public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception {
		return externalOrderDAO.getUnit(userInfo);
	}
	
//	public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception{
//		return externalOrderDAO.getPackageMaster(userInfo);
//	}
	public ResponseEntity<Object> getSiteScreen(Integer nsitecode,final UserInfo userInfo) throws Exception {
		return externalOrderDAO.getSiteScreen(nsitecode, userInfo);
	}
	
	public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo)
			throws Exception{
		return externalOrderDAO.getPatient(spatientid, userInfo);
	}
	
	public Patient getActivePatientById(final String spatientid, final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getActivePatientById(spatientid, userInfo);
	}
	
	public ResponseEntity<Object> getInstitutionCategory(UserInfo userInfo) throws Exception {
		return externalOrderDAO.getInstitutionCategory(userInfo);
	}
	
	public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception {
		return externalOrderDAO.getProduct(nproductcode, userInfo);
	}
	public ResponseEntity<Object> getSamplePriority(UserInfo userInfo) throws Exception{
		return externalOrderDAO.getSamplePriority(userInfo);
	}
	
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception{
		return externalOrderDAO.getSite(userInfo);
	}
	
	public ResponseEntity<Object> outSourceSampleTest(final RegistrationTest testInputnew,final RegistrationTest otherdetails,List<RegistrationTest> testInput, final int destinationSitecode,
			final int designTemplateMappingCode, final Registration outsourceSampleData ,final UserInfo userInfo) throws Exception
	{
		return externalOrderDAO.outSourceSampleTest(testInputnew,otherdetails,testInput, destinationSitecode, designTemplateMappingCode,outsourceSampleData,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> createExternalOrderPTB(ExternalOrder objExternalOrder ,
			UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub		
		return externalOrderDAO.createExternalOrderPTB(objExternalOrder,userInfo);			
	}
	
	public ResponseEntity<Object> getExternalOrderType(final UserInfo userInfo) throws Exception
	{
		return externalOrderDAO.getExternalOrderType(userInfo);
	}
	
	public ResponseEntity<Object> updateOrderSampleStatus(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception
	{
		return externalOrderDAO.updateOrderSampleStatus(userInfo, inputMap);
	}	
	
	public ResponseEntity<Object> getSampleappearance(UserInfo userInfo) throws Exception{
		return externalOrderDAO.getSampleappearance(userInfo);
	}
	
	
	
// Sonia Commented this method for ALPD-4184 
//	public ResponseEntity<Object> SendToPortalReport (final UserInfo userInfo) throws Exception
//	{
//		// TODO Auto-generated method stub
//		return externalOrderDAO.SendToPortalReport(userInfo);
//	}
	
}
