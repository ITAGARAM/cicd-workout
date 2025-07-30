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
@Table(name = "gender")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Gender extends CustomizedResultsetRowMapper<Gender> implements Serializable,RowMapper<Gender> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ngendercode ")
	private short ngendercode;
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = 4;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode;
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String sgendername;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String sdefaultname;
	
	public Gender mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Gender objGender = new Gender();
		objGender.setNgendercode(getShort(arg0,"ngendercode",arg1));
		objGender.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objGender.setNstatus(getShort(arg0,"nstatus",arg1));
		objGender.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objGender.setSgendername(getString(arg0,"sgendername",arg1));
		objGender.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objGender.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objGender.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		objGender.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objGender;
	}

}

