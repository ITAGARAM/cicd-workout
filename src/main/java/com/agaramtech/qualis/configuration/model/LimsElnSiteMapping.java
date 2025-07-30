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

@Entity
@Table(name = "limselnsitemapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LimsElnSiteMapping extends CustomizedResultsetRowMapper<LimsElnSiteMapping>
		implements Serializable, RowMapper<LimsElnSiteMapping> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nelnsitemappingcode")
	private int nelnsitemappingcode;

	@Column(name = "nlimssitecode")
	private int nlimssitecode;

	@Column(name = "nelnsitecode")
	private int nelnsitecode;

	@Column(name = "selnsitename", length = 510, nullable = false)
	private String selnsitename;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String ssitename;
	@Transient
	private transient String sitename;
	@Transient
	private transient int sitecode;

	@Override
	public LimsElnSiteMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final LimsElnSiteMapping limsElnSiteMapping = new LimsElnSiteMapping();
		limsElnSiteMapping.setNelnsitemappingcode(getShort(arg0, "nelnsitemappingcode", arg1));
		limsElnSiteMapping.setNlimssitecode(getShort(arg0, "nlimssitecode", arg1));
		limsElnSiteMapping.setNelnsitecode(getShort(arg0, "nelnsitecode", arg1));
		limsElnSiteMapping.setSelnsitename(StringEscapeUtils.unescapeJava(getString(arg0, "selnsitename", arg1)));
		limsElnSiteMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		limsElnSiteMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		limsElnSiteMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		limsElnSiteMapping.setSsitename(getString(arg0, "ssitename", arg1));
		limsElnSiteMapping.setSitename(getString(arg0, "sitename", arg1));
		limsElnSiteMapping.setSitecode(getInteger(arg0, "sitecode", arg1));
		return limsElnSiteMapping;
	}
}
