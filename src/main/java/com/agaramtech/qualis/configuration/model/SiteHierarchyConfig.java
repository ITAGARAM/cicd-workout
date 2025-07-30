package com.agaramtech.qualis.configuration.model;

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
@Table(name = "sitehierarchyconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SiteHierarchyConfig extends CustomizedResultsetRowMapper<SiteHierarchyConfig>
		implements Serializable, RowMapper<SiteHierarchyConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitehierarchyconfigcode")
	private int nsitehierarchyconfigcode;

	@Column(name = "sconfigname", length = 100, nullable = false)
	private String sconfigname;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("8")
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nneedalltypesite")
	private short nneedalltypesite = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String ssitename;

	@Override
	public SiteHierarchyConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteHierarchyConfig objSiteHierarchycConfig = new SiteHierarchyConfig();

		objSiteHierarchycConfig.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objSiteHierarchycConfig.setSconfigname(getString(arg0, "sconfigname", arg1));
		objSiteHierarchycConfig.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSiteHierarchycConfig.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSiteHierarchycConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSiteHierarchycConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSiteHierarchycConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		objSiteHierarchycConfig.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSiteHierarchycConfig.setSsitename(getString(arg0, "ssitename", arg1));
		objSiteHierarchycConfig.setNneedalltypesite(getShort(arg0, "nneedalltypesite", arg1));

		return objSiteHierarchycConfig;
	}
}
