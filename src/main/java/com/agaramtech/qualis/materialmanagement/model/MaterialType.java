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

@Entity
@Table(name = "materialtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialType extends CustomizedResultsetRowMapper<MaterialType> implements Serializable, RowMapper<MaterialType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialtypecode")
	private short nmaterialtypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String smaterialtypename;

	@Transient
	private transient String sdescription;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String sdefaultname;

	@Override
	public MaterialType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final MaterialType materialtype = new MaterialType();
		materialtype.setNmaterialtypecode(getShort(arg0, "nmaterialtypecode", arg1));
		materialtype.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		materialtype.setSmaterialtypename(getString(arg0, "smaterialtypename", arg1));
		materialtype.setSdescription(getString(arg0, "sdescription", arg1));
		materialtype.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		materialtype.setNsitecode(getShort(arg0, "nsitecode", arg1));
		materialtype.setNstatus(getShort(arg0, "nstatus", arg1));
		materialtype.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		materialtype.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		materialtype.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return materialtype;
	}

}