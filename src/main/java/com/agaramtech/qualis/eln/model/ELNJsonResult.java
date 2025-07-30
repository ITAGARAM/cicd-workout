package com.agaramtech.qualis.eln.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.List;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name="elnjsonresult")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ELNJsonResult extends CustomizedResultsetRowMapper<ELNJsonResult> implements Serializable, RowMapper<ELNJsonResult>{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Lob	
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public ELNJsonResult mapRow(ResultSet arg0, int arg1) throws SQLException {
		ELNJsonResult objELNJsonResult = new ELNJsonResult();
		objELNJsonResult.setNbatchmastercode(getInteger(arg0, "nbatchmastercode", arg1));
		objELNJsonResult.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objELNJsonResult.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objELNJsonResult.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objELNJsonResult.setNstatus(getShort(arg0, "nstatus", arg1));
		return objELNJsonResult;
	}
	
}
