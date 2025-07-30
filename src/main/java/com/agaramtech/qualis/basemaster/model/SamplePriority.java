package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
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
@Table(name = "samplepriority")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SamplePriority extends CustomizedResultsetRowMapper<SamplePriority> implements Serializable,RowMapper<SamplePriority> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsampleprioritycode")
	private int nsampleprioritycode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = 4;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String samplepriorityname;
	
	@Transient
	private transient String smodifieddate;
	
	public SamplePriority mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		final SamplePriority objSamplePriority = new SamplePriority();
		objSamplePriority.setNsampleprioritycode(getInteger(arg0,"nsampleprioritycode",arg1));
		objSamplePriority.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSamplePriority.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objSamplePriority.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSamplePriority.setNstatus(getShort(arg0,"nstatus",arg1));
		objSamplePriority.setSamplepriorityname(getString(arg0,"samplepriorityname",arg1));
		objSamplePriority.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSamplePriority.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		return objSamplePriority;
	}
	
	

 
}
