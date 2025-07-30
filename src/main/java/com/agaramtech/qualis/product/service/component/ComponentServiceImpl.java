package com.agaramtech.qualis.product.service.component;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Component;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ComponentServiceImpl implements ComponentService {

	private final ComponentDAO componentDAO;
	private final CommonFunction commonFunction;

	public ComponentServiceImpl(ComponentDAO componentDAO, CommonFunction commonFunction) {
		this.componentDAO = componentDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception {
		return componentDAO.getComponent(userInfo);
	}

	@Override
	public ResponseEntity<Object> getComponentPortal(final UserInfo userInfo) throws Exception {
		return componentDAO.getComponentPortal(userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveComponentById(int ncomponentCode, final UserInfo userInfo) throws Exception {
		final Component component = componentDAO.getActiveComponentById(ncomponentCode, userInfo);
		if (component == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(component, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		return componentDAO.createComponent(inputMap, component, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		return componentDAO.updateComponent(inputMap, component, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		return componentDAO.deleteComponent(inputMap, component, userInfo);
	}

}
