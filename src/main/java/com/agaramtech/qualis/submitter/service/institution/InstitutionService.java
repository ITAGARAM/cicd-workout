package com.agaramtech.qualis.submitter.service.institution;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionFile;
import com.agaramtech.qualis.submitter.model.InstitutionSite;

public interface InstitutionService {

	public ResponseEntity<Object> getInstitution(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionCategory(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionByCategory(final int InstitutionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveInstitutionById(final Integer InstitutionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getInstitutionSiteCombo(final int ninstitutioncode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveInstitutionSiteById(final int nInstitutionCode,
			final int nInstitutionSiteCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getInstitutionFile(final int ninstitutionCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<? extends Object> createInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> updateInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> deleteInstitutionFile(final InstitutionFile objInstitutionFile,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionFileById(final InstitutionFile objInstitutionFile,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> viewInstitutionFile(final InstitutionFile objInstitutionFile, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSelectedInstitutionDetail(final UserInfo userInfo, final int ninstitutionCode)
			throws Exception;

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<List<InstitutionSite>> getInstitutionSitebyInstitution(final int ninstitutioncode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionValues(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionDistrict(final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getInstitutionCategoryByDistrict(final int districtCode, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getInstitutionByMappedCategory(final int ninstitutionCatCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception;

	public ResponseEntity<Object> getInstitutionSiteByMappedInstitution(final int ninstitutionCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception;
}
