package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This entity class is used to map the fields with genericlabel table of database.
 */
@Entity
@Table(name = "genericlabel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GenericLabel extends CustomizedResultsetRowMapper<GenericLabel> implements Serializable, RowMapper<GenericLabel> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ngenericlabelcode")
	private short ngenericlabelcode;

	@Column(name = "sGenericLabel", length = 25, nullable = false)
	private String sgenericlabel;

	@Column(name = "sidsfieldname", length = 50, nullable = false)
	private String sidsfieldname;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	//@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	//@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String sdefaultname;

	@Override
	public GenericLabel mapRow(ResultSet arg0, int arg1) throws SQLException {
		final GenericLabel objGenericLabel = new GenericLabel();
		objGenericLabel.setNgenericlabelcode(getShort(arg0, "ngenericlabelcode", arg1));
		objGenericLabel.setSgenericlabel(StringEscapeUtils.unescapeJava(getString(arg0, "sgenericlabel", arg1)));
		objGenericLabel.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objGenericLabel.setNstatus(getShort(arg0, "nstatus", arg1));
		objGenericLabel.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objGenericLabel.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objGenericLabel.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objGenericLabel.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objGenericLabel.setSidsfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sidsfieldname", arg1)));
		return objGenericLabel;
	}
}
