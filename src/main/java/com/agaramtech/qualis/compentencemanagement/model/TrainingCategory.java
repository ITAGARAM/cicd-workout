package com.agaramtech.qualis.compentencemanagement.model;

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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trainingcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TrainingCategory extends CustomizedResultsetRowMapper<TrainingCategory>implements Serializable, RowMapper<TrainingCategory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntrainingcategorycode")
	private int ntrainingcategorycode;
	@Column(name = "strainingcategoryname", columnDefinition = "nvarchar", length = 100)
	private String strainingcategoryname = "";
	@Column(name = "sdescription", columnDefinition = "nvarchar", length = 255)
	private String sdescription = "";
	@Column(name = "ndefaultstatus")
	private short ndefaultstatus;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	@Override
	public TrainingCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TrainingCategory objTrainingCategory = new TrainingCategory();
		objTrainingCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objTrainingCategory.setNtrainingcategorycode(getInteger(arg0, "ntrainingcategorycode", arg1));
		objTrainingCategory.setStrainingcategoryname(StringEscapeUtils.unescapeJava(getString(arg0, "strainingcategoryname", arg1)));
		objTrainingCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objTrainingCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objTrainingCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTrainingCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objTrainingCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objTrainingCategory;
	}

}
