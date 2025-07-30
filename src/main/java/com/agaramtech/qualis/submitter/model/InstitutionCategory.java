package com.agaramtech.qualis.submitter.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
 * This class is used to map the fields of InstitutionCategory table of the
 * Database.
 */
@Entity
@Table(name = "institutioncategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstitutionCategory extends CustomizedResultsetRowMapper<InstitutionCategory>
		implements Serializable, RowMapper<InstitutionCategory> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ninstitutioncatcode")
	private int ninstitutioncatcode;
	
	@Column(name = "sinstitutioncatname", length = 100, nullable = false)
	private String sinstitutioncatname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String smodifieddate;

	@Override
	public InstitutionCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		InstitutionCategory institutionCategory = new InstitutionCategory();
		institutionCategory.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		institutionCategory.setSinstitutioncatname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutioncatname", arg1)));
		institutionCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		institutionCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		institutionCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		institutionCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		institutionCategory.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return institutionCategory;
	}

}