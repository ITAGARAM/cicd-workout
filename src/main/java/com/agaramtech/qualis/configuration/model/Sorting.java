package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qualismenu")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Sorting extends CustomizedResultsetRowMapper<Sorting> implements Serializable, RowMapper<Sorting> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmenucode")
	private int nmenucode;
	
	@Column(name = "smenuname")
	private String smenuname;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "nsorter")
	private int nsorter;
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private int nstatus=Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	@Override
	public Sorting mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Sorting sorting = new Sorting();
		sorting.setNmenucode(getInteger(arg0, "nmenucode", arg1));
		sorting.setSmenuname(getString(arg0, "smenuname", arg1));
		sorting.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		sorting.setNsorter(getInteger(arg0, "nsorter", arg1));
		sorting.setNstatus(getInteger(arg0, "nstatus", arg1));
		return sorting;
	}
}
