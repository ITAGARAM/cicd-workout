package com.agaramtech.qualis.contactmaster.model;

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
 * This class is used to map the fields of 'clientcategory' table of the
 * Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 08- Sep- 2020
 */
@Entity
@Table(name = "clientcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientCategory extends CustomizedResultsetRowMapper<ClientCategory> implements Serializable, RowMapper<ClientCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nclientcatcode")
	private int nclientcatcode;

	@Column(name = "sclientcatname", length = 100, nullable = false)
	private String sclientcatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplaystatus;

	@Override
	public ClientCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ClientCategory objClientCategory = new ClientCategory();
		objClientCategory.setNclientcatcode(getInteger(arg0, "nclientcatcode", arg1));
		objClientCategory.setSclientcatname(StringEscapeUtils.unescapeJava(getString(arg0, "sclientcatname", arg1)));
		objClientCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objClientCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objClientCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objClientCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objClientCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objClientCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objClientCategory;
	}

}
