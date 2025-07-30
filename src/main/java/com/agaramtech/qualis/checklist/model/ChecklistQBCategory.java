package com.agaramtech.qualis.checklist.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "checklistqbcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChecklistQBCategory extends CustomizedResultsetRowMapper<ChecklistQBCategory>
		implements Serializable, RowMapper<ChecklistQBCategory> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nchecklistqbcategorycode")
	private int nchecklistqbcategorycode;
	
	@Column(name = "schecklistqbcategoryname", length = 100, nullable = false)
	private String schecklistqbcategoryname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Override
	public ChecklistQBCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistQBCategory objChecklistQBCategory = new ChecklistQBCategory();
		objChecklistQBCategory.setNchecklistqbcategorycode(getInteger(arg0, "nchecklistqbcategorycode", arg1));
		objChecklistQBCategory.setSchecklistqbcategoryname(StringEscapeUtils.unescapeJava(getString(arg0, "schecklistqbcategoryname", arg1)));
		objChecklistQBCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objChecklistQBCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objChecklistQBCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objChecklistQBCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objChecklistQBCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objChecklistQBCategory;
	}
}
