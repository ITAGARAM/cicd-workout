package com.agaramtech.qualis.global;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import com.agaramtech.qualis.global.Enumeration.StringTokens;
import lombok.Data;

@Component
@Data
public class SQLQueryCreator {

	protected List<Class<?>> classCollection = null;

	public String multiPlainQueryFormation(String multipleplainSqlQuery, Class<?>... multipleTables) {
		StringBuilder objBuilder = null;
		if (multipleplainSqlQuery != null) {
			if (multipleplainSqlQuery != "") {
				multipleplainSqlQuery = multipleplainSqlQuery.trim().endsWith(StringTokens.SEMICOLON.getToken()) == true
						? multipleplainSqlQuery.trim()
						: multipleplainSqlQuery.trim() + StringTokens.SEMICOLON.getToken();
				String query[] = multipleplainSqlQuery.split(StringTokens.SEMICOLON.getToken());
				classCollection = Arrays.asList(multipleTables);
				if (query.length == classCollection.size()) {
					if (classCollection != null) {
						if (classCollection.size() > 0) {
							for (int i = 0; i < classCollection.size(); i++) {
								if (objBuilder == null)
									objBuilder = new StringBuilder();
								objBuilder.append(query[i] + StringTokens.SEMICOLON.getToken());
							}
						}

					}
				}
			}
		}
		return objBuilder == null ? "" : objBuilder.toString();
	}
}
