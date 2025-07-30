package com.agaramtech.qualis.externalorder.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.Patient;
//import com.agaramtech.contactmaster.pojo.Patient;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationTest;

public interface ExternalOrderDAO
{
		public ResponseEntity<Object> getExternalOrder(final int nexternalOrderCode,UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> createExternalOrder(ExternalOrder objOrderDetail,int needfilter, UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> receivedExternalOrder(ExternalOrder objOrderDetail,UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getExternalOrderClickDetails(final int nportalordercode, UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getProductCategory(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getTestMaster(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getDiagnosticcase(final UserInfo userInfo) throws Exception;
		
		public ResponseEntity<Object> getPriority(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getTestPackageTest(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getTestGroupProfile(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> updatePortalOrderStatus(final String sorderseqno,final String sordersampleno, final short ntransactionstatus,final UserInfo userinfo) throws Exception;
		public ResponseEntity<Object> getActiveExternalOrderById(int nexternalordercode, UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> updateExternalOrder(ExternalOrder externalorder, int needfilter, UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getDraftExternalOrderDetails(final String sexternalorderid,final UserInfo userinfo,final int nexteralordertypecode) throws Exception;
		public ResponseEntity<Object> onUpdateCancelExternalOrder(final String sexternalordercode,final String sexternalordersamplecode,final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception;

		public ResponseEntity<Object> outSourceTest(final RegistrationTest testInput, final int destinationSitecode,final int designTemplateMappingCode,
				final UserInfo userInfo) throws Exception;
		
	    public ResponseEntity<Object> getOutSourceSite(final RegistrationTest testInput, final UserInfo userInfo) throws Exception;

		public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getCity(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getDistrict(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getSubmitterDetailByAll(Map<String, Object> inputMap, final UserInfo userInfo)throws Exception;
		public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getInstitutionValues(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getInstitutionDepartment(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getUnit(UserInfo userInfo) throws Exception;
	
		//public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getSiteScreen(Integer nsitecode,final UserInfo userInfo) throws Exception ;
		public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo)
				throws Exception;
		
		public Patient getActivePatientById(final String spatientid, final UserInfo userInfo) throws Exception;
		
		public ResponseEntity<Object> getInstitutionCategory(UserInfo userInfo) throws Exception ;
		public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getSamplePriority(UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception;
		ResponseEntity<Object> getOutSourceSiteAndTest(final RegistrationTest testInput, final UserInfo userInfo) throws Exception;
		 ResponseEntity<Object> outSourceSampleTest(final RegistrationTest testInputnew,final RegistrationTest otherdetails,final List<RegistrationTest> testInput, final int destinationSitecode,final int designTemplateMappingCode,
				 final Registration outsourceSampleData, final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> createExternalOrderPTB(ExternalOrder objExternalOrder ,
				UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> getExternalOrderType(final UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> updateOrderSampleStatus(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception;
		ResponseEntity<Object> getSampleappearance(UserInfo userInfo) throws Exception;
		//public ResponseEntity<Object> cancelExternalOrderPreventTb(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
		public ResponseEntity<Object> SendToPortalReport (final UserInfo userInfo) throws Exception;
	//	public ResponseEntity<Object> createExternalOrderOpenMrs(ExternalOrder objExternalOrder, UserInfo userInfo) throws Exception;

}
