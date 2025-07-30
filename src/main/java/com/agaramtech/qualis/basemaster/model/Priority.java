package com.agaramtech.qualis.basemaster.model;

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

@Entity
@Table(name = "priority")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Priority extends CustomizedResultsetRowMapper<Priority> implements Serializable, RowMapper<Priority> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprioritycode")
	private short nprioritycode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String spriorityname;

	@Transient
	private transient String sdescription;

	@Override
	public Priority mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Priority objDisposition = new Priority();
		objDisposition.setNprioritycode(getShort(arg0, "nprioritycode", arg1));
		objDisposition.setSpriorityname(StringEscapeUtils.unescapeJava(getString(arg0, "spriorityname", arg1)));
		objDisposition.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objDisposition.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objDisposition.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objDisposition.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objDisposition.setNstatus(getShort(arg0, "nstatus", arg1));
		objDisposition.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objDisposition;
	}

}
