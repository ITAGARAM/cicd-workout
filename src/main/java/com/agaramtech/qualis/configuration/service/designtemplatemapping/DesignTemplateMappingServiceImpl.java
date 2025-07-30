package com.agaramtech.qualis.configuration.service.designtemplatemapping;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DesignTemplateMappingServiceImpl implements DesignTemplateMappingService {

	private final DesignTemplateMappingDAO designtemplatemappingDAO;

	public DesignTemplateMappingServiceImpl(DesignTemplateMappingDAO designtemplatemappingDAO) {
		super();
		this.designtemplatemappingDAO = designtemplatemappingDAO;
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateMapping(final int nsampletypecode, final int nregtypecode,
			final int nregsubtypecode, final int ndesigntemplatemappingcode, short formCode, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.getDesignTemplateMapping(nsampletypecode, nregtypecode, nregsubtypecode,
				ndesigntemplatemappingcode, formCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDynamicPreRegDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.getDynamicPreRegDesign(inputMap, userInfo);
	}

	public ResponseEntity<Object> getTemplateRegType(final int nregtypecode, final int nsampletypecode,
			final UserInfo userInfo) throws Exception {

		return designtemplatemappingDAO.getTemplateRegType(nregtypecode, nsampletypecode, userInfo);
	}

	public ResponseEntity<Object> getTemplateRegSubType(final int nregtypecode, final UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.getTemplateRegSubType(nregtypecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.createDesignTemplateMapping(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.approveDesignTemplateMapping(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDesignTemplateMappingById(final int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.getDesignTemplateMappingById(ndesigntemplatemappingcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDesignTemplateMapping(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception {

		return designtemplatemappingDAO.deleteDesignTemplateMapping(designtemplatemapping, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMappedFieldProps(DesignTemplateMapping designtemplatemapping, UserInfo userInfo)
			throws Exception {

		return designtemplatemappingDAO.getMappedFieldProps(designtemplatemapping, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> configureDesignTemplateMapping(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception {

		return designtemplatemappingDAO.configureDesignTemplateMapping(designtemplatemapping, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAuditMappedFieldProps(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {

		return designtemplatemappingDAO.getAuditMappedFieldProps(designtemplatemapping, userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> configureDynamicAudit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		
		return designtemplatemappingDAO.configureDynamicAudit(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateConfigureSendToStore(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		
		return designtemplatemappingDAO.updateConfigureSendToStore(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMappedfield(final int ndesigntemplatemappingcode, final UserInfo userInfo)
			throws Exception {
		return designtemplatemappingDAO.getMappedfield(ndesigntemplatemappingcode, userInfo);

	}

	@Override
	public ResponseEntity<Object> getConfigureCheckList(int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception {
		return designtemplatemappingDAO.getConfigureCheckList(ndesigntemplatemappingcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getConfigureCheckListLatestVersion(Map<String, Object> inputMap, UserInfo userInfo) {
		return designtemplatemappingDAO.getConfigureCheckListLatestVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseSampleFilterFields(UserInfo userInfo) throws Exception {
		return designtemplatemappingDAO.getReleaseSampleFilterFields(userInfo);
	}

}
