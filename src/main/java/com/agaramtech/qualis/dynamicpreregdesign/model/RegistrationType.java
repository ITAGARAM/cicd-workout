package com.agaramtech.qualis.dynamicpreregdesign.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "registrationtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationType extends CustomizedResultsetRowMapper<RegistrationType> implements Serializable, RowMapper<RegistrationType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregtypecode")
	private short nregtypecode;
	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;
	@Column(name = "napprovalsubtypecode", nullable = false)
	private short napprovalsubtypecode;
	@Lob@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter =(short)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Lob@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Transient
	private transient String sregtypename;
	@Transient
	private transient String sdescription;
	@Transient
	private transient String ssampletypename;

	@Override
	public RegistrationType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final RegistrationType objRegistrationType = new RegistrationType();
		
		objRegistrationType.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objRegistrationType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationType.setSregtypename(getString(arg0, "sregtypename", arg1));
		objRegistrationType.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objRegistrationType.setNapprovalsubtypecode(getShort(arg0, "napprovalsubtypecode", arg1));
		objRegistrationType.setNsorter(getShort(arg0, "nsorter", arg1));
		objRegistrationType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objRegistrationType.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegistrationType.setSdescription(getString(arg0, "sdescription", arg1));
		objRegistrationType.setSsampletypename(getString(arg0, "ssampletypename", arg1));
		objRegistrationType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objRegistrationType.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));

		return objRegistrationType;
	}

}
