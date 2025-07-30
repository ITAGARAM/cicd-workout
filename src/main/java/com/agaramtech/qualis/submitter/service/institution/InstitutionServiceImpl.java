package com.agaramtech.qualis.submitter.service.institution;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Institution;
import com.agaramtech.qualis.submitter.model.InstitutionFile;
import com.agaramtech.qualis.submitter.model.InstitutionSite;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

	private final InstitutionDAO institutionDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getInstitution(final UserInfo userInfo) throws Exception {
		return institutionDAO.getInstitution(userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionCategory(final UserInfo userInfo) throws Exception {
		return institutionDAO.getInstitutionCategory(userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByCategory(final int ninstitutionCatCode, final UserInfo objUserInfo)
			throws Exception {
		return institutionDAO.getInstitutionByCategory(ninstitutionCatCode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.createInstitution(objInstitution, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.updateInstitution(objInstitution, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstitution(final Institution objInstitution, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.deleteInstitution(objInstitution, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveInstitutionById(final Integer ninstitutioncode, final UserInfo userInfo)
			throws Exception {
		final Institution institution = institutionDAO.getActiveInstitutionById(ninstitutioncode,
				userInfo);
		if (institution == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(institution, HttpStatus.OK);

		}
	}

	@Override
	public ResponseEntity<Object> getInstitutionSiteCombo(final int ninstitutioncode, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.getInstitutionSiteCombo(ninstitutioncode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.createInstitutionSite(objInstitutionSite, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveInstitutionSiteById(final int nInstitutionCode,
			final int nInstitutionSiteCode, final UserInfo userInfo) throws Exception {
		final InstitutionSite objInstitutionSite = institutionDAO.getActiveInstitutionSiteById(nInstitutionCode,
				nInstitutionSiteCode, userInfo);
		if (objInstitutionSite != null) {
			return new ResponseEntity<Object>(objInstitutionSite, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.updateInstitutionSite(objInstitutionSite, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstitutionSite(final InstitutionSite objInstitutionSite,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.deleteInstitutionSite(objInstitutionSite, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getInstitutionFile(final int ninstitutionCode, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.getInstitutionFile(ninstitutionCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> createInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.createInstitutionFile(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateInstitutionFile(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.updateInstitutionFile(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteInstitutionFile(final InstitutionFile objInstitutionFile,
			final UserInfo userInfo) throws Exception {
		return institutionDAO.deleteInstitutionFile(objInstitutionFile, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewInstitutionFile(final InstitutionFile objInstitutionFile, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.viewInstitutionFile(objInstitutionFile, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionFileById(final InstitutionFile objInstitutionFile,
			final UserInfo userInfo) throws Exception {
		final InstitutionFile objInstitutionFile1 = institutionDAO.getInstitutionFileById(objInstitutionFile, userInfo);
		if (objInstitutionFile1 != null) {
			return new ResponseEntity<Object>(objInstitutionFile1, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getSelectedInstitutionDetail(final UserInfo userInfo, final int ninstitutionCode)
			throws Exception {
		return institutionDAO.getSelectedInstitutionDetail(userInfo, ninstitutionCode);
	}

	@Override
	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		return institutionDAO.getDistrict(nregioncode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception {
		return institutionDAO.getCity(ndistrictcode, userInfo);
	}

	@Override
	public ResponseEntity<List<InstitutionSite>> getInstitutionSitebyInstitution(final int ninstitutioncode, final UserInfo userInfo)
			throws Exception {
		return institutionDAO.getInstitutionSitebyInstitution(ninstitutioncode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionSitebyAll(final UserInfo userInfo) throws Exception {
		return institutionDAO.getInstitutionSitebyAll(userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionValues(final UserInfo userInfo) throws Exception {
		return institutionDAO.getInstitutionValues(userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionDistrict(final UserInfo objUserInfo) throws Exception {
		return institutionDAO.getInstitutionDistrict(objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionCategoryByDistrict(final int districtCode, final UserInfo objUserInfo)
			throws Exception {
		return institutionDAO.getInstitutionCategoryByDistrict(districtCode, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getInstitutionByMappedCategory(final int ninstitutionCatCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception {
		return institutionDAO.getInstitutionByMappedCategory(ninstitutionCatCode, objUserInfo, ndistrictCode);

	}

	@Override
	public ResponseEntity<Object> getInstitutionSiteByMappedInstitution(final int ninstitutionCode,
			final UserInfo objUserInfo, final int ndistrictCode) throws Exception {
		return institutionDAO.getInstitutionSiteByMappedInstitution(ninstitutionCode, objUserInfo, ndistrictCode);
	}

}
