package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'seqnouserroletemplateversion' table
 * of the Database.
 */
@Entity
@Table(name = "seqnouserroletemplateversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoUserRoleTemplateVersion extends CustomizedResultsetRowMapper<SeqNoUserRoleTemplateVersion>
		implements Serializable, RowMapper<SeqNoUserRoleTemplateVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;

	@Override
	public SeqNoUserRoleTemplateVersion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SeqNoUserRoleTemplateVersion objSeqNoUserRoleTemplateVersion = new SeqNoUserRoleTemplateVersion();

		objSeqNoUserRoleTemplateVersion.setStablename(getString(arg0, "stablename", arg1));
		objSeqNoUserRoleTemplateVersion.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoUserRoleTemplateVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		objSeqNoUserRoleTemplateVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSeqNoUserRoleTemplateVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objSeqNoUserRoleTemplateVersion;
	}

}
