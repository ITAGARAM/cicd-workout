package com.agaramtech.qualis.basemaster.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'containertype' table of the
 * Database.
 * 
 * @author ATE121
 * @version 11.0.0.1
 * @since 09-Apr-2025
 */
@Entity
@Table(name = "containertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContainerType extends CustomizedResultsetRowMapper<ContainerType> implements Serializable, RowMapper<ContainerType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncontainertypecode")
	private int ncontainertypecode;
	@Column(name = "scontainertype", length = 100, nullable = false)
	private String scontainertype;
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public ContainerType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ContainerType containerType = new ContainerType();
		containerType.setNstatus(getShort(arg0, "nstatus", arg1));
		containerType.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));
		containerType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		containerType.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		containerType.setScontainertype(StringEscapeUtils.unescapeJava(getString(arg0, "scontainertype", arg1)));
		containerType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return containerType;
	}

}
