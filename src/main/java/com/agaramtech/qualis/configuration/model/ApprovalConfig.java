package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
@Table(name = "approvalconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalConfig extends CustomizedResultsetRowMapper<ApprovalConfig> implements Serializable,RowMapper<ApprovalConfig>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "napprovalconfigcode")
	private short napprovalconfigcode;
	@Column(name = "nregsubtypecode",nullable=false)
	private short nregsubtypecode;
	@Column(name = "nregtypecode",nullable=false)
	private short  nregtypecode;
	@Column(name = "napprovalsubtypecode",nullable=false)
	private short napprovalsubtypecode;
	@Column(name = "sdescription",length = 255)
	private String  sdescription="";
	@Column(name = "nneedanalyst",nullable=false) 
	private short nneedanalyst;
	@ColumnDefault("1")	
	@Column(name = "nstatus",nullable=false)
	private short nstatus; 
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = -1;
	
	@Transient
	private transient String sneedsubsample;
	@Transient	
	private transient String sneedscheduler;
	@Transient	
	private transient String sneedsampledby;
	@Transient	
	private transient String ssampleformat;
	@Transient	
	private transient String sresetduration;
	@Transient	
	private transient int nsubsamplesequenceno;
	@Transient	
	private transient String sseqresetdate;

	@Override
	public ApprovalConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		

		final ApprovalConfig objApprovalConfig = new ApprovalConfig();
		objApprovalConfig.setNapprovalsubtypecode(getShort(arg0,"napprovalsubtypecode",arg1));
		objApprovalConfig.setNstatus(getShort(arg0,"nstatus",arg1));
		objApprovalConfig.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objApprovalConfig.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objApprovalConfig.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		objApprovalConfig.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1))); 
		objApprovalConfig.setNneedanalyst(getShort(arg0,"nneedanalyst",arg1));
		objApprovalConfig.setSneedsubsample(getString(arg0,"sneedsubsample",arg1));
		objApprovalConfig.setSneedsampledby(getString(arg0,"sneedsampledby",arg1));
		objApprovalConfig.setSneedscheduler(getString(arg0,"sneedscheduler",arg1));
		objApprovalConfig.setSsampleformat(getString(arg0,"ssampleformat",arg1));
		objApprovalConfig.setNsubsamplesequenceno(getInteger(arg0,"nsubsamplesequenceno",arg1));
		objApprovalConfig.setSresetduration(getInteger(arg0,"nresetduration",arg1)+" "+getString(arg0,"speriodname",arg1));
		objApprovalConfig.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objApprovalConfig.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objApprovalConfig;
	}





}
