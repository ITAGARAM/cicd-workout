package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.integration.service.IntegrationService;

@RestController
@RequestMapping("/storagecategory")
public class IntegrationController {

	private final IntegrationService integrationService;
	
	public IntegrationController(IntegrationService integrationService) {
		super();
		this.integrationService = integrationService;
	}
	
	@PostMapping(value = "/updateInterfacerResult")
	public ResponseEntity<Object> updateInterfacerResult(@RequestBody Map<String, Object> inputMap) throws Exception {
		return integrationService.updateInterfacerResult(inputMap);
	}
}
