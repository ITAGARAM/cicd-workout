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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "samplingpoint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SamplingPoint extends CustomizedResultsetRowMapper<SamplingPoint>
		implements Serializable, RowMapper<SamplingPoint> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsamplingpointcode")
	private int nsamplingpointcode;

	@Column(name = "nsamplingpointcatcode")
	private int nsamplingpointcatcode;

	@Column(name = "ssamplingpointname", length = 100, nullable = false)
	private String ssamplingpointname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssamplingpointcatname;

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient String smodifieddate;

	@Override
	public SamplingPoint mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SamplingPoint objSamplingPoint = new SamplingPoint();
		objSamplingPoint.setNsamplingpointcode(getInteger(arg0, "nsamplingpointcode", arg1));
		objSamplingPoint.setNsamplingpointcatcode(getInteger(arg0, "nsamplingpointcatcode", arg1));
		objSamplingPoint.setSsamplingpointname(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplingpointname", arg1)));
		objSamplingPoint.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSamplingPoint.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSamplingPoint.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSamplingPoint.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSamplingPoint.setNstatus(getShort(arg0, "nstatus", arg1));
		objSamplingPoint.setSsamplingpointcatname(getString(arg0, "ssamplingpointcatname", arg1));
		objSamplingPoint.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objSamplingPoint.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objSamplingPoint;
	}

}
