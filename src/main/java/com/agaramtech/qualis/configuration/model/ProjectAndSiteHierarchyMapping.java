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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'projectsitehierarchymapping' table
 * of the Database.
 * 
 * @author Mullai Balaji.V BGSI-7 3/07/2025
 */
@Entity
@Table(name = "projectsitehierarchymapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectAndSiteHierarchyMapping extends CustomizedResultsetRowMapper<ProjectAndSiteHierarchyMapping>
		implements Serializable, RowMapper<ProjectAndSiteHierarchyMapping> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprojectsitehierarchymapcode")
	private int nprojectsitehierarchymapcode;//reduce column name

	@Column(name = "nbioprojectcode")
	private int nbioprojectcode;

	@Column(name = "nsitehierarchyconfigcode")
	private int nsitehierarchyconfigcode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprojecttitle;

	@Transient
	private transient String sconfigname;

	@Override
	public ProjectAndSiteHierarchyMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ProjectAndSiteHierarchyMapping objProjectAndSiteHierarchyMapping = new ProjectAndSiteHierarchyMapping();

		objProjectAndSiteHierarchyMapping
				.setNprojectsitehierarchymapcode(getInteger(arg0, "nprojectsitehierarchymapcode", arg1));
		objProjectAndSiteHierarchyMapping.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objProjectAndSiteHierarchyMapping
				.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objProjectAndSiteHierarchyMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProjectAndSiteHierarchyMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProjectAndSiteHierarchyMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objProjectAndSiteHierarchyMapping.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objProjectAndSiteHierarchyMapping.setSconfigname(getString(arg0, "sconfigname", arg1));

		return objProjectAndSiteHierarchyMapping;
	}

}
