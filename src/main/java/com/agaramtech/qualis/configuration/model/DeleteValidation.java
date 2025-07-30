package com.agaramtech.qualis.configuration.model;

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
 * This class is used to map the fields of 'deletevalidation' table of the
 * Database.
*/
@Entity
@Table(name = "deletevalidation")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DeleteValidation extends CustomizedResultsetRowMapper<DeleteValidation>
		implements Serializable, RowMapper<DeleteValidation> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ndeletevalidationcode")
	private int ndeletevalidationcode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "smastertablename", length = 150, nullable = false)
	private String smastertablename;

	@Column(name = "smasterprimarykeyname", length = 150, nullable = false)
	private String smasterprimarykeyname;

	@Column(name = "ntransformcode", nullable = false)
	private short ntransformcode;

	@Column(name = "stranstablename", length = 150, nullable = false)
	private String stranstablename;

	@Column(name = "stranstableforeignkeyname", length = 150, nullable = false)
	private String stranstableforeignkeyname;

	@Column(name = "sjsonfieldname", length = 50, nullable = false)
	private String sjsonfieldname;

	@Column(name = "nisdynamicmaster", nullable = false)
	private short nisdynamicmaster;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;	

	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient short nquerybuildertablecode;

	@Override
	public DeleteValidation mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final DeleteValidation objValidation = new DeleteValidation();
		
		objValidation.setNdeletevalidationcode(getInteger(arg0, "ndeletevalidationcode", arg1));
		objValidation.setNquerybuildertablecode(getShort(arg0, "nquerybuildertablecode", arg1));
		objValidation.setNformcode(getShort(arg0, "nformcode", arg1));
		objValidation.setNtransformcode(getShort(arg0, "ntransformcode", arg1));
		objValidation.setSmastertablename(StringEscapeUtils.unescapeJava(getString(arg0, "smastertablename", arg1)));
		objValidation.setSmasterprimarykeyname(StringEscapeUtils.unescapeJava(getString(arg0, "smasterprimarykeyname", arg1)));
		objValidation.setStranstablename(StringEscapeUtils.unescapeJava(getString(arg0, "stranstablename", arg1)));
		objValidation.setStranstableforeignkeyname(StringEscapeUtils.unescapeJava(getString(arg0, "stranstableforeignkeyname", arg1)));
		objValidation.setSjsonfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sjsonfieldname", arg1)));
		objValidation.setNisdynamicmaster(getShort(arg0, "nisdynamicmaster", arg1));
		objValidation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objValidation.setNstatus(getShort(arg0, "nstatus", arg1));
		objValidation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		
		return objValidation;
	}

}