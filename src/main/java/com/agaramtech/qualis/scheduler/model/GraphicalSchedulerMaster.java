package com.agaramtech.qualis.scheduler.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "graphicalschedulermaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GraphicalSchedulerMaster extends CustomizedResultsetRowMapper<GraphicalSchedulerMaster> implements Serializable, RowMapper<GraphicalSchedulerMaster> {

private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nschedulecode")
	private short nschedulecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "stitle", length=100)
	private String stitle;
	
	@Column(name = "sscheduletype", length=255)
	private String sscheduletype;
	
	@Column(name = "nschedulestatus")
	private int nschedulestatus;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private int nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public GraphicalSchedulerMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		GraphicalSchedulerMaster objSch = new GraphicalSchedulerMaster();
		objSch.setNschedulecode(getShort(arg0, "nschedulecode", arg1));
		objSch.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSch.setNstatus(getShort(arg0, "nstatus", arg1));
		objSch.setSscheduletype(StringEscapeUtils.unescapeJava(getString(arg0, "sscheduletype", arg1)));
		objSch.setStitle(StringEscapeUtils.unescapeJava(getString(arg0, "stitle", arg1)));
		objSch.setNschedulestatus(getInteger(arg0, "nschedulestatus", arg1));
		objSch.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSch.setNsitecode(getInteger(arg0, "nstatus", arg1));
		return objSch;
		
	}

}

