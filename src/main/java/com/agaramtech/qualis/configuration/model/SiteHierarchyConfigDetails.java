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

@Entity
@Table(name = "sitehierarchyconfigdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SiteHierarchyConfigDetails extends CustomizedResultsetRowMapper<SiteHierarchyConfigDetails>
		implements Serializable, RowMapper<SiteHierarchyConfigDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitehierarchyconfigdetailcode")
	private int nsitehierarchyconfigdetailcode;

	@Column(name = "nsitehierarchyconfigcode")
	private int nsitehierarchyconfigcode;

	@Column(name = "nnodesitecode")
	private int nnodesitecode;

	@Column(name = "nparentsitecode")
	private int nparentsitecode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sparentsitecode")
	private String sparentsitecode = "";

	@Column(name = "schildsitecode")
	private String schildsitecode = "";

	@Override
	public SiteHierarchyConfigDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteHierarchyConfigDetails SiteHierarchyConfigDetails = new SiteHierarchyConfigDetails();

		SiteHierarchyConfigDetails
				.setNsitehierarchyconfigdetailcode(getInteger(arg0, "nsitehierarchyconfigdetailcode", arg1));
		SiteHierarchyConfigDetails.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		SiteHierarchyConfigDetails.setNnodesitecode(getInteger(arg0, "nnodesitecode", arg1));
		SiteHierarchyConfigDetails.setNparentsitecode(getInteger(arg0, "nparentsitecode", arg1));
		SiteHierarchyConfigDetails.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		SiteHierarchyConfigDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		SiteHierarchyConfigDetails.setSparentsitecode(getString(arg0, "sparentsitecode", arg1));
		SiteHierarchyConfigDetails.setSchildsitecode(getString(arg0, "schildsitecode", arg1));

		return SiteHierarchyConfigDetails;
	}
}
