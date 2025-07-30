package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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

/**
 * This class is used to map the fields of 'temporarystorage' table of the Database.
 */

@Entity
@Table(name = "temporarystorage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TemporaryStorage extends CustomizedResultsetRowMapper<TemporaryStorage>
		implements Serializable, RowMapper<TemporaryStorage> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntemporarystoragecode ")
	private int ntemporarystoragecode;

	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;

	@Column(name = "sbarcodeid", length = 20, nullable = false)
	private String sbarcodeid;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dstoragedatetime", nullable = false)
	private Instant dstoragedatetime;

	@Column(name = "ntzstoragedatetime", nullable = false)
	private int ntzstoragedatetime;

	@Column(name = "noffsetstoragedatetime", nullable = false)
	private int noffsetstoragedatetime;

	@Column(name = "scomments", length = 255)
	private String scomments="";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient String sstoragedatetime;
	@Transient
	private transient String squantity;
	@Transient
	private transient String sunitname;
	@Transient
	private transient String sprocessduration;

	@Override
	public TemporaryStorage mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final TemporaryStorage objTemporaryStorage = new TemporaryStorage();
		objTemporaryStorage.setNtemporarystoragecode(getInteger(arg0, "ntemporarystoragecode", arg1));
		objTemporaryStorage.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objTemporaryStorage.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objTemporaryStorage.setSbarcodeid(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcodeid", arg1)));
		objTemporaryStorage.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objTemporaryStorage.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTemporaryStorage.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTemporaryStorage.setNstatus(getShort(arg0, "nstatus", arg1));
		objTemporaryStorage.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		objTemporaryStorage.setDstoragedatetime(getInstant(arg0, "dstoragedatetime", arg1));
		objTemporaryStorage.setNoffsetstoragedatetime(getInteger(arg0, "noffsetstoragedatetime", arg1));
		objTemporaryStorage.setNtzstoragedatetime(getInteger(arg0, "ntzstoragedatetime", arg1));
		objTemporaryStorage.setSstoragedatetime(getString(arg0, "sstoragedatetime", arg1));
		objTemporaryStorage.setSquantity(getString(arg0, "squantity", arg1));
		objTemporaryStorage.setSunitname(getString(arg0, "sunitname", arg1));
		objTemporaryStorage.setSprocessduration(getString(arg0, "sprocessduration", arg1));

		return objTemporaryStorage;
	}

}
