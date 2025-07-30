package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dashboard.model.QueryBuilder;
import com.agaramtech.qualis.dashboard.service.sqlquery.SQLQueryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/querybuilder")

public class QueryBuilderController {

	private SQLQueryService sqlQueryService;
	private RequestContext requestContext;

	public QueryBuilderController(SQLQueryService sqlQueryService, RequestContext requestContext) {
		super();
		this.requestContext = requestContext;
		this.sqlQueryService = sqlQueryService;
	}

	@RequestMapping(value = "/getQueryBuilder", method = RequestMethod.POST)
	public ResponseEntity<Object> getQueryBuilder(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getQueryBuilder(userInfo);
	}

	@RequestMapping(value = "/getSelectedQueryBuilder", method = RequestMethod.POST)
	public ResponseEntity<Object> getSelectedQueryBuilder(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nQueryBuilderCode = (Integer) inputMap.get("nquerybuildercode");

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getSelectedQueryBuilder(nQueryBuilderCode, userInfo);
	}

	@PostMapping(value = "/createQueryBuilder")
	public ResponseEntity<Object> createQueryBuilder(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final QueryBuilder queryBuilder = objmapper.convertValue(inputMap.get("queryBuilder"), QueryBuilder.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sgeneratedQuery = (String) inputMap.get("sgeneratedquery");

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.createQueryBuilder(queryBuilder, userInfo, sgeneratedQuery);

	}

	@PostMapping(value = "/updateQueryBuilder")
	public ResponseEntity<Object> updateQueryBuilder(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final QueryBuilder queryBuilder = objmapper.convertValue(inputMap.get("queryBuilder"), QueryBuilder.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.updateQueryBuilder(queryBuilder, userInfo);

	}

	@PostMapping(value = "/deleteQueryBuilder")
	public ResponseEntity<Object> deleteQueryBuilder(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final QueryBuilder queryBuilder = objmapper.convertValue(inputMap.get("queryBuilder"), QueryBuilder.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.deleteQueryBuilder(queryBuilder, userInfo);

	}
}