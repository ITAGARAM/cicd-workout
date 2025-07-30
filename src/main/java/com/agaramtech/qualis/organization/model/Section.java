package com.agaramtech.qualis.organization.model;

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

/**
 * This class is used to map the fields of 'section' table of the Database.
 */
@Entity
@Table(name = "section")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Section extends CustomizedResultsetRowMapper<Section> implements Serializable,RowMapper<Section> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsectioncode")
	private int nsectioncode;

	@Column(name = "ssectionname", length = 100, nullable = false)
	private String ssectionname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode= (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String sdisplaystatus;

	
	@Override
	public Section mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Section section = new Section();

		section.setNstatus(getShort(arg0,"nstatus",arg1));
		section.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		section.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		section.setNsitecode(getShort(arg0,"nsitecode",arg1));
		section.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		section.setSsectionname(StringEscapeUtils.unescapeJava(getString(arg0,"ssectionname",arg1)));
		section.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		section.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return section;
	}
	
}
