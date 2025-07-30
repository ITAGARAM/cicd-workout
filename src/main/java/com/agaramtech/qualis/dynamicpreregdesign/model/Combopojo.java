package com.agaramtech.qualis.dynamicpreregdesign.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Combopojo extends CustomizedResultsetRowMapper<Combopojo> implements Serializable, RowMapper<Combopojo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Transient
	private transient Map<String,Object> jsondata;

	@Override
	public Combopojo mapRow(ResultSet arg0, int arg1) throws SQLException {

		final Combopojo objCombopojo=new Combopojo();
		objCombopojo.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		return objCombopojo;
	}

}
