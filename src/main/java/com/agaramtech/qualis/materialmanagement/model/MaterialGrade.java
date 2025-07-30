package com.agaramtech.qualis.materialmanagement.model;

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

/**
 * This class is used to map the fields of MaterialGrade table of the Database.
 */

@Entity
@Table(name = "materialgrade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialGrade extends CustomizedResultsetRowMapper<MaterialGrade> implements Serializable, RowMapper<MaterialGrade> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialgradecode")
	private short nmaterialgradecode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "ntzmodifieddate", nullable = false)
	private short ntzmodifieddate;

	@Column(name = "noffsetdmodifieddate", nullable = false)
	private int noffsetdmodifieddate;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String smaterialgradename;
	@Transient
	private transient String sdescription;

	@Override
	public MaterialGrade mapRow(ResultSet arg0, int arg1) throws SQLException {

		final MaterialGrade materialGrade = new MaterialGrade();		
		materialGrade.setNmaterialgradecode(getShort(arg0, "nmaterialgradecode", arg1));
		materialGrade.setSmaterialgradename(getString(arg0, "smaterialgradename", arg1));
		materialGrade.setSdescription(getString(arg0, "sdescription", arg1));
		materialGrade.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		materialGrade.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		materialGrade.setNstatus(getShort(arg0, "nstatus", arg1));
		materialGrade.setNsitecode(getShort(arg0, "nsitecode", arg1));
		materialGrade.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		materialGrade.setNoffsetdmodifieddate(getInteger(arg0, "noffsetdmodifieddate", arg1));
		materialGrade.setNtzmodifieddate(getShort(arg0, "ntzmodifieddate", arg1));
		materialGrade.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return materialGrade;

	}
}
