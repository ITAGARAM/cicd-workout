package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "storagecondition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StorageCondition extends CustomizedResultsetRowMapper<StorageCondition> implements Serializable, RowMapper<StorageCondition> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstorageconditioncode")
	private int nstorageconditioncode;

	@Column(name = "sstorageconditionname", length = 100, nullable = false)
	private String sstorageconditionname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplaystatus;

	@Override
	public StorageCondition mapRow(ResultSet arg0, int arg1) throws SQLException {
		final StorageCondition storagecondition = new StorageCondition();
		storagecondition.setNstatus(getShort(arg0, "nstatus", arg1));
		storagecondition.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		storagecondition.setNsitecode(getShort(arg0, "nsitecode", arg1));
		storagecondition.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		storagecondition.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		storagecondition.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		storagecondition.setSstorageconditionname(StringEscapeUtils.unescapeJava(getString(arg0, "sstorageconditionname", arg1)));
		storagecondition.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return storagecondition;
	}

}