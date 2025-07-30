package com.agaramtech.qualis.compentencemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'technique' table of the Database.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "technique")
@Data
public class Technique extends CustomizedResultsetRowMapper<Technique> implements Serializable, RowMapper<Technique> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntechniquecode")
	private int ntechniquecode;
	
	@Column(name = "stechniquename", length = 100, nullable = false)
	private String stechniquename;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient 
	private transient String sdisplaystatus;

	@Override
	public Technique mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Technique objTechnique = new Technique();
		
		objTechnique.setNtechniquecode(getInteger(arg0, "ntechniquecode", arg1));
		objTechnique.setStechniquename(StringEscapeUtils.unescapeJava(getString(arg0, "stechniquename", arg1)));
		objTechnique.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objTechnique.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTechnique.setNstatus(getShort(arg0, "nstatus", arg1));
		objTechnique.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));

		return objTechnique;
	}
}