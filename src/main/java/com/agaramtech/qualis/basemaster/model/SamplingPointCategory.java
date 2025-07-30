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
@Table(name = "samplingpointcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SamplingPointCategory extends CustomizedResultsetRowMapper<SamplingPointCategory>
		implements Serializable, RowMapper<SamplingPointCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplingpointcatcode")
	private int nsamplingpointcatcode;

	@Column(name = "ssamplingpointcatname", length = 100, nullable = false)
	private String ssamplingpointcatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient String smodifieddate;

	@Override
	public SamplingPointCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SamplingPointCategory objSamplingPointCategory = new SamplingPointCategory();
		objSamplingPointCategory.setNsamplingpointcatcode(getInteger(arg0, "nsamplingpointcatcode", arg1));
		objSamplingPointCategory.setSsamplingpointcatname(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplingpointcatname", arg1)));
		objSamplingPointCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSamplingPointCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSamplingPointCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSamplingPointCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSamplingPointCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objSamplingPointCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objSamplingPointCategory.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objSamplingPointCategory;
	}

}
