package com.agaramtech.qualis.organization.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "labsection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LabSection extends CustomizedResultsetRowMapper<LabSection> implements Serializable, RowMapper<LabSection> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nlabsectioncode")
	private int nlabsectioncode;

	@Column(name = "ndeptlabcode", nullable = false)
	private int ndeptlabcode;

	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssectionname;
	@Transient
	private transient int nsectionusercode;
	@Transient
	private transient String slabname;

	@Override
	public LabSection mapRow(ResultSet arg0, int arg1) throws SQLException {

		final LabSection labSection = new LabSection();

		labSection.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		labSection.setNsectionusercode(getInteger(arg0, "nsectionusercode", arg1));
		labSection.setNdeptlabcode(getInteger(arg0, "ndeptlabcode", arg1));
		labSection.setSsectionname(getString(arg0, "ssectionname", arg1));
		labSection.setSlabname(getString(arg0, "slabname", arg1));
		labSection.setNstatus(getShort(arg0, "nstatus", arg1));
		labSection.setNlabsectioncode(getInteger(arg0, "nlabsectioncode", arg1));
		labSection.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		labSection.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return labSection;
	}

}
