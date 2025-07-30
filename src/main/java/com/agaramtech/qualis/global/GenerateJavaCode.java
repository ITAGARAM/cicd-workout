package com.agaramtech.qualis.global;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.AnnotationSpec.Builder;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.submitter.model.District;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/generatejavacode")
@AllArgsConstructor
public class GenerateJavaCode {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateJavaCode.class);
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	
	private static final String annotation = "org.springframework.web.bind.annotation";
	private static final String responseEntity = "org.springframework.http";
	private static final String fundamentalClassesAndInterfaces = "java.lang";
	private static final String logger = "org.slf4j";
	private static final String collections = "java.util";
	private static final String objectMapper = "com.fasterxml.jackson.databind";
	private static final String typeReference = "com.fasterxml.jackson.core.type";
	private static final String javaPersistenceAPI = "jakarta.persistence";
	private static final String commonObject = "com.agaramtech.qualis.global";

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/generateCode")
	public ResponseEntity<Object> generateCode() throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final String query = "select jsondata from codegenerationsettings where ncodegensetting=1";

		final District queryValues = jdbcTemplate.queryForObject(query, new District());

		final Map<String, Object> map = objMapper.convertValue(queryValues.getJsondata(),
				new TypeReference<Map<String, Object>>() {
				});

		String methodName = map.get("methodname").toString();
		String classURL = map.get("classurl").toString();
		String packageName = map.get("packagename").toString();
		String tableName = map.get("tablename").toString();
		String pkey = map.get("pkey").toString();
		String pkeyname = map.get("gettersetterpkname").toString();
		String seqnotable = map.get("seqnotable").toString();
		List<String> actionList = (List<String>) map.get("action");
		List<String> filerequiredList = (List<String>) map.get("filerequired");
		String pathsrc=map.get("srcpath").toString();
		List<Map<String, Object>> actionRequiredList = (List<Map<String, Object>>) map.get("actiondefinition");
		
		
		if(filerequiredList.stream().anyMatch(action -> action.contains("pojo") && action.equals("pojo"))) {
			createPojo(map,methodName,classURL,packageName,tableName,pkey,map,pathsrc);
		} if(filerequiredList.stream().anyMatch(action -> action.contains("controller") && action.equals("controller"))) {
			createController(map,methodName,classURL,packageName,pkey,actionList,actionRequiredList,pathsrc);
		} if(filerequiredList.stream().anyMatch(action -> action.contains("service") && action.equals("service"))) {
			createService(map,methodName,classURL,packageName,pkey,actionList,actionRequiredList,pathsrc);
		} if(filerequiredList.stream().anyMatch(action -> action.contains("serviceimpl") && action.equals("serviceimpl"))) {
			createServiceImpl(map,methodName,classURL,packageName,pkey,actionList,actionRequiredList,pathsrc);
		} if(filerequiredList.stream().anyMatch(action -> action.contains("dao") && action.equals("dao"))) {
			createDAO(map,methodName,classURL,packageName,pkey,actionList,actionRequiredList,pathsrc);
		} if(filerequiredList.stream().anyMatch(action -> action.contains("daoimpl") && action.equals("daoimpl"))) {
			createDAOImpl(map,methodName,classURL,packageName,pkey,actionList,actionRequiredList,pkeyname,seqnotable,tableName,map,pathsrc);
		}
		return null;
	}

	// Utility method to determine JDBC ResultSet getter method
	private static String getJdbcGetterMethod(Class<?> type) {
		if (type == int.class || type == Integer.class)
			return "Integer";
		if (type == short.class || type == Short.class)
			return "Short";
		if (type == String.class)
			return "String";
		if (type == java.time.Instant.class)
			return "Instant";
		return null;
	}

	// Capitalize field name for setter methods
	private static String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	public static Class<?> getPrimitiveClass(String typeName) {
        return switch (typeName) {
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "boolean" -> boolean.class;
            case "char" -> char.class;
          //  case "map" -> Map.class;
            default -> String.class; // fallback to String.class
        };
    }
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> createPojo(Map<String,Object> inputMap,String methodName, String classURL, String packageName, 
			String tableName,String pkey,Map<String,Object> map,String pathsrc) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> field = map.containsKey("fieldname")
				? (List<Map<String, Object>>) map.get("fieldname")
				: new ArrayList<>();
	
		List<Map<String, Object>> transientfield = map.containsKey("transientfield")
				? (List<Map<String, Object>>) map.get("transientfield")
				: new ArrayList<>();

		Map<String, Class<?>> fields = new LinkedHashMap<>();
		for (Map<String, Object> fieldName : field) {
			if(fieldName.get("datatype").toString().equals("map")) {
//				Type type=new TypeReference<Map<String, Object>>() {}.getType();
//				Class<?> clazz = (Class<?>) type;
			fields.put(fieldName.get("tablecolumnname").toString(),Map.class);
			}else {
			Class<?> clazz = getPrimitiveClass(fieldName.get("datatype").toString());
			fields.put(fieldName.get("tablecolumnname").toString(), clazz);
	     	}
		}
		
		fields.put("dmodifieddate", java.time.Instant.class);
		fields.put("nsitecode", short.class);
		fields.put("nstatus", short.class);
		
		// Transient Fields
		Map<String, Class<?>> transientFields = new LinkedHashMap<>();
		for (Map<String, Object> fieldName : transientfield) {
			transientFields.put(fieldName.get("columnname").toString(),
					fieldName.get("datatype").toString().getClass());
		}

		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(methodName).addModifiers(Modifier.PUBLIC)
				.addAnnotation(ClassName.get("lombok", "Data"))
				.addAnnotation(ClassName.get(javaPersistenceAPI, "Entity"))
				.addAnnotation(AnnotationSpec.builder(ClassName.get(javaPersistenceAPI, "Table"))
						.addMember("name", "$S", tableName).build())
				.addAnnotation(ClassName.get("lombok", "NoArgsConstructor"))
				.addAnnotation(ClassName.get("lombok", "AllArgsConstructor"))
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "EqualsAndHashCode"))
						.addMember("callSuper", "$L", false).build())
				.superclass(ClassName.get(commonObject, "CustomizedResultsetRowMapper")) // Extends
																							// CustomizedResultsetRowMapper
				.addSuperinterface(ClassName.get("java.io", "Serializable"))
				.addSuperinterface(
						ParameterizedTypeName.get(ClassName.get("org.springframework.jdbc.core", "RowMapper"),
								ClassName.get(packageName + ".model", methodName))); // Implements RowMapper<?>

		// Add serialVersionUID field
		classBuilder.addField(FieldSpec.builder(long.class, "serialVersionUID")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("1L").build());

		// Add fields with JPA annotations
		for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
			String fieldName = entry.getKey();
			Class<?> fieldType = entry.getValue();
			FieldSpec.Builder fieldBuilder;
		
			if(!fieldType.equals(Map.class)) {
		    fieldBuilder = FieldSpec.builder(entry.getValue(), entry.getKey())
					 .addModifiers(Modifier.PRIVATE);
			}else {
			fieldBuilder = FieldSpec.builder
					(ParameterizedTypeName.get(
		                    ClassName.get(Map.class),
		                    ClassName.get(String.class),
		                    ClassName.get(Object.class)),
		                "jsondata", Modifier.PRIVATE);
			}
			if (entry.getKey().equals(pkey)) {
				fieldBuilder.addAnnotation(ClassName.get(javaPersistenceAPI, "Id"));
			}
			Builder columnAnnotation = AnnotationSpec.builder(ClassName.get(javaPersistenceAPI, "Column"))
					.addMember("name", "$S", fieldName);
			for (Map<String, Object> fieldNamess : field) {

				if (fieldName.equals(fieldNamess.get("tablecolumnname"))
						&& !pkey.equals(fieldNamess.get("tablecolumnname"))) {
					if (fieldNamess.containsKey("nullable")) {
						columnAnnotation.addMember("nullable", "$L", fieldNamess.get("nullable"));
					}
					if (fieldNamess.containsKey("length")) {
						columnAnnotation.addMember("length", "$L", fieldNamess.get("length"));
					}
					if (fieldNamess.containsKey("columnDefinition")) {
						fieldBuilder.addAnnotation(ClassName.get("jakarta.persistence", "Lob"));
						columnAnnotation.addMember("columnDefinition", "$S", fieldNamess.get("columnDefinition"));
					}
					if (fieldNamess.containsKey("defaultvalue")) {
						fieldBuilder.addAnnotation( AnnotationSpec.builder(ClassName.get("org.hibernate.annotations", "ColumnDefault"))
							        .addMember("value", "$S", fieldNamess.get("defaultvalue"))
							        .build())
						           	.initializer("($T) $T.TransactionStatus."+fieldNamess.get("defaultname")+".gettransactionstatus()",
						           	fieldType, ClassName.get(commonObject, "Enumeration")) // Handles type casting
						           .build();
					}
					
					
				}
			}
			if(fieldName.equals("nsitecode")){
				fieldBuilder.addAnnotation( AnnotationSpec.builder(ClassName.get("org.hibernate.annotations", "ColumnDefault"))
				        .addMember("value", "$S", "-1")
				        .build())
						.initializer("($T) $T.TransactionStatus.NA.gettransactionstatus()",
			           	fieldType, ClassName.get(commonObject, "Enumeration")) // Handles type casting
			           .build();
			}else if(fieldName.equals("nstatus")){
				fieldBuilder.addAnnotation( AnnotationSpec.builder(ClassName.get("org.hibernate.annotations", "ColumnDefault"))
				        .addMember("value", "$S", "1")
				        .build())
						.initializer("($T) $T.TransactionStatus.ACTIVE.gettransactionstatus()",
			           	fieldType, ClassName.get(commonObject, "Enumeration")) // Handles type casting
			           .build();
			}

			fieldBuilder.addAnnotation(columnAnnotation.build());
			classBuilder.addField(fieldBuilder.build());
		}

		for (Map.Entry<String, Class<?>> entry : transientFields.entrySet()) {

			FieldSpec fieldBuilder = FieldSpec.builder(entry.getValue(), entry.getKey()).addModifiers(Modifier.PRIVATE)
					.addAnnotation(ClassName.get("jakarta.persistence", "Transient")).build();
			classBuilder.addField(fieldBuilder);
		}

		// Add mapRow() method
		MethodSpec.Builder mapRowMethod = MethodSpec.methodBuilder("mapRow").addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class).returns(ClassName.get(packageName + ".model", methodName))
				.addParameter(ClassName.get("java.sql", "ResultSet"), "rs").addParameter(int.class, "rowNum")
				.addException(ClassName.get("java.sql", "SQLException"))
				.addStatement("$T obj" + methodName + " = new $T()", ClassName.get(packageName + ".model", methodName),
						ClassName.get(packageName + ".model", methodName));

		for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
			String getterMethod = getJdbcGetterMethod(entry.getValue());
			if (getterMethod != null) {
				mapRowMethod.addStatement("obj"+methodName+".set$L(get$L(rs, $S, rowNum))", capitalize(entry.getKey()), getterMethod,
						entry.getKey());
			}
		}

		mapRowMethod.addStatement("return obj" + methodName);
		classBuilder.addMethod(mapRowMethod.build());

		// Build and write Java file
		JavaFile javaFile = JavaFile.builder(packageName + ".model", classBuilder.build()).build();
		javaFile.writeTo(Paths.get(pathsrc));
		
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		System.out.println("Java POJO class successfully!");
		return returnMap;
	}
	public Map<String, Object> createController(Map<String,Object> inputMap,String methodName, String classURL, String packageName, 
			String pkey,List<String> actionList,List<Map<String, Object>> actionRequiredList,String pathsrc) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		TypeSpec.Builder classBuilderController = TypeSpec.classBuilder(methodName + "Controller")
				.addModifiers(Modifier.PUBLIC).addAnnotation(ClassName.get(annotation, "RestController"))
				.addAnnotation(AnnotationSpec.builder(ClassName.get(annotation, "RequestMapping"))
						.addMember("value", "$S", "/" + classURL).build())
				.addField(FieldSpec.builder(ClassName.get(logger, "Logger"), "LOGGER")
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer("$T.getLogger($T.class)", ClassName.get(logger, "LoggerFactory"),
								ClassName.get("", methodName + "Controller"))
						.build())
				.addField(FieldSpec
						.builder(ClassName.get("", "RequestContext"), "requestContext")
						.addModifiers(Modifier.PRIVATE).build())
				.addField(FieldSpec
						.builder(ClassName.get(packageName + ".service", methodName + "Service"), classURL + "Service")
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
		;

		MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(ClassName.get("", "RequestContext"), "requestContext")
				.addParameter(ClassName.get("", methodName + "Service"), classURL + "Service")
				.addJavadoc("Need to Write")
				.addStatement("super()")
				.addStatement("this.requestContext = requestContext")
				.addStatement("this." + classURL + "Service = " + classURL + "Service");
		classBuilderController.addMethod(constructor.build());

		for (String actionName : actionList) {
			String actionField = actionName.equals("getActiveById") ? "getActive" + methodName + "ById"
					: actionName + methodName;
			Map<String, Object> values = actionRequiredList.stream().filter(x -> actionName.equals(x.get("name")))
					.collect(Collectors.toMap(x -> x.get("name").toString(), x -> x)); // Collect as a Map
			MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(ClassName.get(annotation, "PostMapping"))
							.addMember("value", "$S", "/" + actionField).build())
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ParameterSpec
							.builder(ParameterizedTypeName.get(ClassName.get(collections, "Map"),
									ClassName.get(fundamentalClassesAndInterfaces, "String"),
									ClassName.get(fundamentalClassesAndInterfaces, "Object")), "inputMap")
							.addAnnotation(ClassName.get(annotation, "RequestBody")).build())
					.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
					.addStatement("$T objMapper = new $T()", ClassName.get(objectMapper, "ObjectMapper"),
							ClassName.get(objectMapper, "ObjectMapper"))
					.addStatement("$T userInfo = objMapper.convertValue(inputMap.get($S), new $T<$T>(){})",
							ClassName.get(commonObject, "UserInfo"), "userinfo",
							ClassName.get(typeReference, "TypeReference"), ClassName.get(commonObject, "UserInfo"))
					.addStatement("requestContext.setUserInfo(userInfo)");

			String parameters = "";
			Map<String, Object> valuesObject = values.containsKey(actionName)
					? (Map<String, Object>) values.get(actionName)
					: new LinkedHashMap<String, Object>();
			if (valuesObject.containsKey("isneedpojocasting") && (boolean) valuesObject.get("isneedpojocasting")) {
				methods.addStatement(
						"$T obj" + methodName + " = objMapper.convertValue(inputMap.get($S), new $T<$T>(){})",
						ClassName.get(packageName + ".model", methodName), classURL,
						ClassName.get(typeReference, "TypeReference"),
						ClassName.get(packageName + ".model", methodName));
				parameters = " ,obj" + methodName;
			}
			if (valuesObject.containsKey("isneedpkfield") && (boolean) valuesObject.get("isneedpkfield")) {
				methods.addStatement("int " + pkey + " = (int) inputMap.get($S)", pkey);
				parameters = " ," + pkey;
			}
			methods.addStatement("return " + classURL + "Service." + actionField + "(userInfo " + parameters + ")");
			classBuilderController.addMethod(methods.build());
		}
		// Build and write Java file
		JavaFile javaFileController = JavaFile.builder("com.agaramtech.qualis.restcontroller", classBuilderController.build())
				.build();
		javaFileController.writeTo(Paths.get(pathsrc));
		System.out.println("javaFileController  class successfully!");
				return returnMap;
	}
	
	public Map<String, Object> createService(Map<String,Object> inputMap,String methodName, String classURL, String packageName, 
			String pkey,List<String> actionList,List<Map<String, Object>> actionRequiredList,String pathsrc) throws Exception {
		TypeSpec.Builder classBuilderService = TypeSpec.interfaceBuilder(methodName + "Service")
				.addModifiers(Modifier.PUBLIC);
		for (String actionName : actionList) {
			String actionField = actionName.equals("getActiveById") ? "getActive" + methodName + "ById"
					: actionName + methodName;
			Map<String, Object> values = actionRequiredList.stream().filter(x -> actionName.equals(x.get("name")))
					.collect(Collectors.toMap(x -> x.get("name").toString(), x -> x)); // Collect as a Map

			MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField)
					.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo");

			String parameters = "";
			Map<String, Object> valuesObject = values.containsKey(actionName)
					? (Map<String, Object>) values.get(actionName)
					: new LinkedHashMap<String, Object>();
			if (valuesObject.containsKey("isneedpojocasting") && (boolean) valuesObject.get("isneedpojocasting")) {
				methods.addParameter(ClassName.get(packageName + ".model", methodName), "obj" + methodName);
				parameters = " ,obj" + methodName;
			}
			if (valuesObject.containsKey("isneedpkfield") && (boolean) valuesObject.get("isneedpkfield")) {
				methods.addParameter(int.class, pkey);
				parameters = " ," + pkey;
			}
			classBuilderService.addMethod(methods.build());
		}
		// Build and write Java file
		JavaFile javaFileService = JavaFile.builder(packageName + ".service", classBuilderService.build()).build();
		javaFileService.writeTo(Paths.get(pathsrc));
		System.out.println("javaFileService  class successfully!");
		return inputMap;
	}
	
	public Map<String, Object> createServiceImpl(Map<String,Object> inputMap,String methodName, String classURL, String packageName, 
			String pkey,List<String> actionList,List<Map<String, Object>> actionRequiredList,String pathsrc) throws Exception {
		TypeSpec.Builder classBuilderServiceIMPL = TypeSpec.classBuilder(methodName + "ServiceImpl")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.transaction.annotation", "Transactional"))
						.addMember("readOnly", "true").addMember("rollbackFor", "$T.class", Exception.class).build())
				.addAnnotation(ClassName.get("org.springframework.stereotype", "Service"))
				.addSuperinterface(ClassName.get("", methodName + "Service"))
				.addField(FieldSpec.builder(ClassName.get("", methodName + "DAO"), classURL + "DAO")
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
				.addField(FieldSpec.builder(ClassName.get(commonObject, "CommonFunction"), "commonFunction")
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
				.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get("", methodName + "DAO"), classURL + "DAO")
						.addParameter(ClassName.get(commonObject, "CommonFunction"), "commonFunction")
						.addStatement("this." + classURL + "DAO = " + classURL + "DAO")
						.addStatement("this.commonFunction = commonFunction").build());

		for (String actionName : actionList) {
			String actionField = actionName.equals("getActiveById") ? "getActive" + methodName + "ById"
					: actionName + methodName;
			Map<String, Object> values = actionRequiredList.stream().filter(x -> actionName.equals(x.get("name")))
					.collect(Collectors.toMap(x -> x.get("name").toString(), x -> x)); // Collect as a Map
			MethodSpec.Builder methods;
			if (actionName.equals("getActiveById")) {
				methods = MethodSpec.methodBuilder(actionField).addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.returns(ParameterizedTypeName.get(ResponseEntity.class, Object.class))
						.addException(Exception.class)
						.addParameter(ClassName.get("", "UserInfo"), "userInfo")
						.addParameter(int.class, pkey)
						.addStatement("$T " + classURL + " = " + classURL + "DAO.getActive" + methodName + "ById(userInfo ,"
								+ pkey + ")", ClassName.get("", methodName))
						.beginControlFlow("if (" + classURL + " == null)")
						.addStatement(
								"return new $T<>($L.getMultilingualMessage($T.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), $T.EXPECTATION_FAILED)",
								ResponseEntity.class, ClassName.get("", "commonFunction"),
								ClassName.get(commonObject, "Enumeration"),
								ClassName.get("org.springframework.http", "HttpStatus"))
						.nextControlFlow("else").addStatement("return new $T<>(" + classURL + ", $T.OK)",
								ResponseEntity.class, ClassName.get("org.springframework.http", "HttpStatus"))
						.endControlFlow();
				classBuilderServiceIMPL.addMethod(methods.build());

			} else {
				methods = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
						.addAnnotation(Override.class)
						.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
						.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
								ClassName.get(fundamentalClassesAndInterfaces, "Object")))
						.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo");

				String parameters = "";
				Map<String, Object> valuesObject = values.containsKey(actionName)
						? (Map<String, Object>) values.get(actionName)
						: new LinkedHashMap<String, Object>();
				if (valuesObject.containsKey("isneedpojocasting") && (boolean) valuesObject.get("isneedpojocasting")) {
					methods.addParameter(ClassName.get(packageName + ".model", methodName), "obj" + methodName);
					parameters = " ,obj" + methodName;
				}
				if (valuesObject.containsKey("isneedpkfield") && (boolean) valuesObject.get("isneedpkfield")) {
					methods.addParameter(int.class, pkey);
					parameters = " ," + pkey;
				}
				methods.addStatement("return " + classURL + "DAO." + actionField + "(userInfo" + parameters + ")");
				classBuilderServiceIMPL.addMethod(methods.build());
			}
		}

		// Build and write Java file
		JavaFile javaFileServiceIMPL = JavaFile.builder(packageName + ".service", classBuilderServiceIMPL.build())
				.build();
		javaFileServiceIMPL.writeTo(Paths.get(pathsrc));
		System.out.println("javaFileServiceIMPL  class successfully!");
		return inputMap;
	}

	public Map<String, Object> createDAO(Map<String, Object> inputMap, String methodName, String classURL,
			String packageName, String pkey, List<String> actionList, List<Map<String, Object>> actionRequiredList,String pathsrc)
			throws Exception {
		TypeSpec.Builder classBuilderDAO = TypeSpec.interfaceBuilder(methodName + "DAO").addModifiers(Modifier.PUBLIC);
		for (String actionName : actionList) {
			String actionField = actionName.equals("getActiveById") ? "getActive" + methodName + "ById"
					: actionName + methodName;
			Map<String, Object> values = actionRequiredList.stream().filter(x -> actionName.equals(x.get("name")))
					.collect(Collectors.toMap(x -> x.get("name").toString(), x -> x)); // Collect as a Map

			if (actionName.equals("getActiveById")) {
				MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
						.returns(ClassName.get("", methodName))
						.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo")
						.addParameter(int.class, pkey);
				classBuilderDAO.addMethod(methods.build());
			} else {
				MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
						.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
								ClassName.get(fundamentalClassesAndInterfaces, "Object")))
						.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo");

				String parameters = "";
				Map<String, Object> valuesObject = values.containsKey(actionName)
						? (Map<String, Object>) values.get(actionName)
						: new LinkedHashMap<String, Object>();
				if (valuesObject.containsKey("isneedpojocasting") && (boolean) valuesObject.get("isneedpojocasting")) {
					methods.addParameter(ClassName.get(packageName + ".model", methodName), "obj" + methodName);
					parameters = " ,obj" + methodName;
				}
				if (valuesObject.containsKey("isneedpkfield") && (boolean) valuesObject.get("isneedpkfield")) {
					methods.addParameter(int.class, pkey);
					parameters = " ," + pkey;
				}
				classBuilderDAO.addMethod(methods.build());
			}
		}

		// Build and write Java file
		JavaFile javaFileDAO = JavaFile.builder(packageName + ".service", classBuilderDAO.build()).build();
		javaFileDAO.writeTo(Paths.get(pathsrc));
		System.out.println("javaFileDAO  class successfully!");
		return inputMap;
	}
	
	public Map<String, Object> createDAOImpl(Map<String,Object> inputMap,String methodName, String classURL, String packageName, 
			String pkey,List<String> actionList,List<Map<String, Object>> actionRequiredList,String pkeyname,String seqnotable,
			String tableName,Map<String,Object> map,String pathsrc) throws Exception {
		
		boolean isneeddefault=  map.containsKey("isneeddefaultlogic")? (boolean) map.get("isneeddefaultlogic"):false;
		TypeSpec.Builder classBuilderDAOImpl = TypeSpec.classBuilder(methodName + "DAOImpl")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(ClassName.get("org.springframework.stereotype", "Repository"))
			.addAnnotation(ClassName.get("lombok", "AllArgsConstructor"))
			.addSuperinterface(ClassName.get(packageName + ".service", methodName + "DAO"))
			.addField(FieldSpec.builder(ClassName.get(logger, "Logger"), "LOGGER")
					.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
					.initializer("$T.getLogger($T.class)", ClassName.get("org.slf4j", "LoggerFactory"),
							ClassName.get("", methodName + "DAOImpl"))
					.build())
			.addField(
					FieldSpec.builder(ClassName.get(commonObject, "StringUtilityFunction"), "stringUtilityFunction")
							.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(FieldSpec.builder(ClassName.get(commonObject, "CommonFunction"), "commonFunction")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(FieldSpec
					.builder(ClassName.get("org.springframework.jdbc.core", "JdbcTemplate"), "jdbcTemplate")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(FieldSpec.builder(ClassName.get(commonObject, "ValidatorDel"), "valiDatorDel")
					.addModifiers(Modifier.PRIVATE).build())
			.addField(FieldSpec
					.builder(ClassName.get(commonObject, "JdbcTemplateUtilityFunction"), "jdbcUtilityFunction")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(FieldSpec.builder(ClassName.get(commonObject, "ProjectDAOSupport"), "projectDAOSupport")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(
					FieldSpec.builder(ClassName.get(commonObject, "DateTimeUtilityFunction"), "dateUtilityFunction")
							.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build())
			.addField(FieldSpec.builder(ClassName.get(commonObject, "AuditUtilityFunction"), "auditUtilityFunction")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());


	for (String actionName : actionList) {
		String actionField = actionName.equals("getActiveById") ? "getActive" + methodName + "ById"
				: actionName + methodName;
		Map<String, Object> values = actionRequiredList.stream().filter(x -> actionName.equals(x.get("name")))
				.collect(Collectors.toMap(x -> x.get("name").toString(), x -> x)); // Collect as a Map

		if (actionName.equals("get")) {
			MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
					.addAnnotation(ClassName.get(Override.class))
					.addAnnotation(AnnotationSpec.builder(ClassName.get(SuppressWarnings.class))
							.addMember("value", "$S", "unchecked").build())
					.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo")
					.addStatement("$T query = \"select * from " + tableName + " where nstatus=1;\"",
							ClassName.get(fundamentalClassesAndInterfaces, "String"))
					.addStatement("$T<String> lstcolumns = new $T<>()", ClassName.get(collections, "List"),
							ClassName.get(collections, "ArrayList"))
					.addStatement("return new $T<>($N.query(query, new $T()), $T.OK)",
							ClassName.get(responseEntity, "ResponseEntity"), "jdbcTemplate",
							ClassName.get(packageName + ".model", methodName),
							ClassName.get(responseEntity, "HttpStatus"));
			classBuilderDAOImpl.addMethod(methods.build());
		} else if (actionName.equals("create")) {
			Map<String, Object> valuesObject = values.containsKey(actionName)
					? (Map<String, Object>) values.get(actionName)
					: new LinkedHashMap<String, Object>();
			String objectName = "obj" + methodName;

			MethodSpec.Builder methods = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
					.addAnnotation(ClassName.get(Override.class))
					.addException(ClassName.get(fundamentalClassesAndInterfaces, "Exception"))
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ClassName.get(commonObject, "UserInfo"), "userInfo")
					.addParameter(ClassName.get(packageName + ".model", methodName), objectName);

			if (valuesObject.containsKey("isneedmethod") && (boolean) valuesObject.get("isneedmethod")) {
				List<Map<String, Object>> methodNameList = (List<Map<String, Object>>) valuesObject
						.get("methodName");
				for (Map<String, Object> list : methodNameList) {
					methods.addStatement("final " + methodName + " " + classURL + "ByName = "
							+ list.get("methodname") + "(" + objectName + ".getS" + list.get("gettersetterfieldname") + "(), "
							+ objectName + ".getNsitecode());")
							.beginControlFlow("if (" + classURL + "ByName == null) ");

					MethodSpec.Builder getDuplicateByName = MethodSpec
							.methodBuilder(list.get("methodname").toString()).addModifiers(Modifier.PRIVATE)
							.returns(ClassName.get("", methodName))
							.addParameter(String.class, "s" + list.get("gettersetterfieldname"))
							.addParameter(int.class, "nmasterSiteCode").addException(Exception.class)
							.addStatement("final String strQuery = \" \";").addStatement(
									"return (" + methodName + ") jdbcUtilityFunction.queryForObject(strQuery, "
											+ methodName + ".class, jdbcTemplate)");

					classBuilderDAOImpl.addMethod(getDuplicateByName.build());

				}

				methods.addStatement("final String sQuery = \"lock table  " + classURL
						+ "  \" + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();")
						.addStatement("jdbcTemplate.execute(sQuery)")
						.addStatement("final List<String> multilingualIDList = new ArrayList<>()")
						.addStatement("final List<Object> saved" + methodName + "List = new ArrayList<>()");

			}
			

			if (isneeddefault) {

				methods.beginControlFlow("if (" + objectName
						+ ".getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())")
						.addStatement("final " + methodName + " default" + methodName + " = get" + methodName
								+ "ByDefaultStatus(" + objectName + ".getNsitecode())")
						.beginControlFlow("if (default" + methodName + " != null)")
						.addStatement("final " + methodName + "  " + classURL
								+ "BeforeSave = SerializationUtils.clone(default" + methodName + ")")
						.addStatement("final List<Object> defaultListBeforeSave = new ArrayList<>()")
						.addStatement("defaultListBeforeSave.add(" + classURL + "BeforeSave)")
						.addStatement("default" + methodName
								+ ".setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus())")
						.addStatement(
								"final String updateQueryString = $S + default" + methodName + ".getN" + pkeyname
										+ "() + $S + default" + methodName + ".getNsitecode()",
								" update " + tableName + " set ndefaultstatus="
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where " + pkey
										+ "=",
								" and nsitecode=")
						.addStatement("jdbcTemplate.execute(updateQueryString)")
						.addStatement("final List<Object> defaultListAfterSave = new ArrayList<>()")
						.addStatement("defaultListAfterSave.add(default" + methodName + ")")
						.addStatement("multilingualIDList.add($S)", "IDS_EDIT" + methodName + "")
						.addStatement(
								"auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave, multilingualIDList, userInfo)")
						.addStatement("multilingualIDList.clear()").endControlFlow().endControlFlow();

				MethodSpec.Builder defaultMethod = MethodSpec.methodBuilder("get" + methodName + "ByDefaultStatus")
						.addModifiers(Modifier.PRIVATE).addException(Exception.class)
						.returns(ClassName.bestGuess(methodName)).addParameter(int.class, "nmasterSiteCode")
						.addStatement("final String strQuery =\" \" ;")
						.addStatement("return (" + methodName + ") jdbcUtilityFunction.queryForObject(strQuery, "
								+ methodName + ".class, jdbcTemplate)");
				classBuilderDAOImpl.addMethod(defaultMethod.build());
			}

			methods.addStatement("final String sequenceNoQuery = $S",
					"select nsequenceno from " + seqnotable + " where stablename = '" + tableName + "'")
					.addStatement("int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class)")
					.addStatement("nsequenceNo++")
					.addStatement("final String insertQuery = \"insert into " + tableName
							+ " ( ) values ( \"+nsequenceNo+\" ,'\" + dateUtilityFunction.getCurrentDateTime(userInfo) + \"',\" + userInfo.getNmastersitecode() + \",\" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + \")\"")
					.addStatement("jdbcTemplate.execute(insertQuery)")
					.addStatement("final String updateQuery = \" update " + seqnotable
							+ " set nsequenceno =\" + nsequenceNo + \" where stablename='" + tableName + "'\"")
					.addStatement("jdbcTemplate.execute(updateQuery)")
					.addStatement("" + objectName + ".setN" + pkeyname + "(nsequenceNo)")
					.addStatement("saved" + methodName + "List.add(" + objectName + ")")
					.addStatement("multilingualIDList.add(\"IDS_ADD" + methodName.toUpperCase() + "\")")
					.addStatement("auditUtilityFunction.fnInsertAuditAction(saved" + methodName
							+ "List, 1, null, multilingualIDList, userInfo)")
					.addStatement("return get" + methodName + "(userInfo)")
					.endControlFlow()
					.beginControlFlow("else")
					.addStatement("// Conflict = 409 - Duplicate entry --getSlanguagetypecode")
					.addStatement("return new ResponseEntity<>(commonFunction.getMultilingualMessage("
							+ " Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), "
							+ " userInfo.getSlanguagefilename()), HttpStatus.CONFLICT)")
					.endControlFlow();

			classBuilderDAOImpl.addMethod(methods.build());

		} else if (actionName.equals("update")) {

			MethodSpec.Builder updateUnit = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ClassName.get("", "UserInfo"), "userInfo").addException(Exception.class)
					.addParameter(ClassName.get("", methodName), "obj" + methodName)
					.addStatement("final " + methodName + " " + classURL + " = getActive" + methodName + "ById(userInfo,obj"
							+ methodName + ".getN" + pkeyname + "())")
					.beginControlFlow("if (" + classURL + " == null)")
					.addStatement(
							"return new $T<>(commonFunction.getMultilingualMessage($T.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), $T.EXPECTATION_FAILED)",
							ClassName.get("org.springframework.http", "ResponseEntity"),
							ClassName.get(commonObject, "Enumeration"),
							ClassName.get("org.springframework.http", "HttpStatus"))
					.endControlFlow().beginControlFlow("else")
					.addStatement("final $T<String> multilingualIDList = new $T<>()",
							ClassName.get("java.util", "List"), ClassName.get("java.util", "ArrayList"))
					.addStatement("final $T<Object> listAfterUpdate = new $T<>()",
							ClassName.get("java.util", "List"), ClassName.get("java.util", "ArrayList"))
					.addStatement("final $T<Object> listBeforeUpdate = new $T<>()",
							ClassName.get("java.util", "List"), ClassName.get("java.util", "ArrayList"))
					.addStatement("final String queryString = \" \" ;")
					.addStatement("final " + methodName + " available" + methodName
							+ " = ($T) jdbcUtilityFunction.queryForObject(queryString, " + methodName
							+ ".class, jdbcTemplate)", ClassName.get("", methodName))
					.beginControlFlow("if (available" + methodName + " == null)");

			if (map.containsKey("isneedefault") && (boolean) map.get("isneedefault")) {
				updateUnit
						.beginControlFlow("if (obj" + methodName
								+ ".getNdefaultstatus() == $T.TransactionStatus.YES.gettransactionstatus())",
								ClassName.get("", "Enumeration"))
						.addStatement("final " + methodName + " default" + methodName + " = get" + methodName
								+ "ByDefaultStatus(obj" + methodName + ".getNsitecode())")
						.beginControlFlow("if (default" + methodName + " != null && default" + methodName + ".getN"
								+ pkeyname + "() != obj" + methodName + ".getN" + pkeyname + "())")
						.addStatement("final " + methodName + " BeforeSave = $T.clone(default" + methodName + ")",
								ClassName.get("org.apache.commons.lang3", "SerializationUtils"))
						.addStatement("listBeforeUpdate.add(BeforeSave)")
						.addStatement("default" + methodName
								+ ".setNdefaultstatus((short) $T.TransactionStatus.NO.gettransactionstatus())",
								ClassName.get("", "Enumeration"))
						.addStatement("final String updateQueryString = \" update " + tableName
								+ " set ndefaultstatus=\" + $T.TransactionStatus.NO.gettransactionstatus() + \" where "
								+ pkey + "=\" + default" + methodName + ".getN" + pkeyname
								+ "() + \" and nsitecode =\" + default" + methodName + ".getNsitecode() + \";\"",
								ClassName.get("", "Enumeration"))
						.addStatement("jdbcTemplate.execute(updateQueryString)")
						.addStatement("listAfterUpdate.add(default" + methodName + ")").endControlFlow().endControlFlow();
			}
			updateUnit
					.addStatement("final String updateQueryString = \"update " + tableName + " set \";")
					.addStatement("jdbcTemplate.execute(updateQueryString)")
					.addStatement("listAfterUpdate.add(obj" + methodName + ")")
					.addStatement("listBeforeUpdate.add(" + classURL + ")")
					.addStatement("multilingualIDList.add(\"IDS_EDIT" + methodName.toUpperCase() + "\")")
					.addStatement(
							"auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo)")
					.addStatement("return get" + methodName + "(userInfo)").nextControlFlow("else")
					.addStatement(
							"return new $T<>(commonFunction.getMultilingualMessage($T.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()), $T.CONFLICT)",
							ClassName.get("org.springframework.http", "ResponseEntity"),
							ClassName.get("", "Enumeration"),
							ClassName.get("org.springframework.http", "HttpStatus"))
					.endControlFlow().endControlFlow().build();
			classBuilderDAOImpl.addMethod(updateUnit.build());

		} else if (actionName.equals("delete")) {
			MethodSpec deleteUnit = MethodSpec.methodBuilder(actionField).addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(responseEntity, "ResponseEntity"),
							ClassName.get(fundamentalClassesAndInterfaces, "Object")))
					.addParameter(ClassName.get("", "UserInfo"), "userInfo").addException(Exception.class)
					.addParameter(ClassName.get("", methodName), "obj" + methodName)
					.addStatement("final " + methodName + " " + classURL + " = getActive" + methodName + "ById(userInfo,obj"
							+ methodName + ".getN" + pkeyname + "())")
					.beginControlFlow("if (" + classURL + " == null)")
					.addStatement(
							"return new $T<>(commonFunction.getMultilingualMessage($T.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), $T.EXPECTATION_FAILED)",
							ClassName.get("org.springframework.http", "ResponseEntity"),
							ClassName.get("", "Enumeration"),
							ClassName.get("org.springframework.http", "HttpStatus"))
					.endControlFlow().beginControlFlow("else").addStatement("final String query = \" \" ;")
					.addStatement("valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo)")
					.addStatement("boolean validRecord = false")
					.beginControlFlow(
							"if (valiDatorDel.getNreturnstatus() == $T.Deletevalidator.SUCCESS.getReturnvalue())",
							ClassName.get("", "Enumeration"))
					.addStatement("validRecord = true")
					.addStatement(
							"valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(obj"+methodName+".getN"+pkeyname+"()), userInfo)")
					.beginControlFlow(
							"if (valiDatorDel.getNreturnstatus() == $T.Deletevalidator.SUCCESS.getReturnvalue())",
							ClassName.get("", "Enumeration"))
					.addStatement("validRecord = true").nextControlFlow("else").addStatement("validRecord = false")
					.endControlFlow().endControlFlow().beginControlFlow("if (validRecord)")
					.addStatement("final $T<String> multilingualIDList = new $T<>()",
							ClassName.get("java.util", "List"), ClassName.get("java.util", "ArrayList"))
					.addStatement("final $T<Object> deleted" + methodName + "List = new $T<>()",
							ClassName.get("java.util", "List"), ClassName.get("java.util", "ArrayList"))
					.addStatement(
							"final String updateQueryString = \" update " + tableName
									+ " set dmodifieddate='\" + dateUtilityFunction.getCurrentDateTime(userInfo)"
									+ " + \"',nstatus = \" + $T.TransactionStatus.DELETED.gettransactionstatus()"
									+ " + \" where " + pkey + "=\" + obj" + methodName + ".getN" + pkeyname
									+ "() + \" and nsitecode =\" + obj" + methodName + ".getNsitecode() + \";\"",
							ClassName.get("", "Enumeration"))
					.addStatement("jdbcTemplate.execute(updateQueryString)")
					.addStatement(
							"obj" + methodName
									+ ".setNstatus((short) $T.TransactionStatus.DELETED.gettransactionstatus())",
							ClassName.get("", "Enumeration"))
					.addStatement("deleted" + methodName + "List.add(obj" + methodName + ")")
					.addStatement("multilingualIDList.add(\"IDS_DELETE" + methodName.toUpperCase() + "\")")
					.addStatement("auditUtilityFunction.fnInsertAuditAction(deleted" + methodName
							+ "List, 1, null, multilingualIDList, userInfo)")
					.addStatement("return get" + methodName + "(userInfo)").nextControlFlow("else")
					.addStatement("return new $T<>(valiDatorDel.getSreturnmessage(), $T.EXPECTATION_FAILED)",
							ClassName.get("org.springframework.http", "ResponseEntity"),
							ClassName.get("org.springframework.http", "HttpStatus"))
					.endControlFlow().endControlFlow().build();
			classBuilderDAOImpl.addMethod(deleteUnit);

		} else if (actionName.equals("getActiveById")) {
			MethodSpec getActiveUnitById = MethodSpec.methodBuilder(actionField).addAnnotation(Override.class)
					.addModifiers(Modifier.PUBLIC).returns(ClassName.get("", methodName))
					.addParameter(ClassName.get("", "UserInfo"), "userInfo")
					.addParameter(int.class, pkey)
					.addException(Exception.class).addStatement("final String strQuery = \" \" ;")
					.addStatement("return (" + methodName + ") jdbcUtilityFunction.queryForObject(strQuery, "
							+ methodName + ".class, jdbcTemplate)")
					.build();
			classBuilderDAOImpl.addMethod(getActiveUnitById);
		}

	}

	JavaFile javaFileDAOImpl = JavaFile.builder(packageName + ".service", classBuilderDAOImpl.build()).build();
	javaFileDAOImpl.writeTo(Paths.get(pathsrc));		
	System.out.println("javaFileDAOImpl  class successfully!");
		return inputMap;
	}

}
