package com.agaramtech.qualis.audittrail.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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

/**
 * This entity class is used to map the fields with auditcomments table of database.
 */
@Entity
@Table(name = "auditcomments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditComments  extends CustomizedResultsetRowMapper<AuditComments> implements Serializable, RowMapper<AuditComments>{
		
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nauditcode")
	private long nauditcode;	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata; 
	
	@Lob
	@Column(name = "jsoncomments", columnDefinition = "jsonb")
	private Map<String, Object> jsoncomments;
	
	
	@Column(name = "scomments",columnDefinition="text", nullable=false)
	private String scomments;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short  nstatus =( short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
 
 
	public Map<String, Object> getJsoncomments() {
		return jsoncomments;
	}
	public void setJsoncomments(Map<String, Object> jsoncomments) {
		this.jsoncomments = jsoncomments;
	}

	@Override
	public AuditComments mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditComments objAuditComments = new AuditComments();
		
		objAuditComments.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objAuditComments.setNauditcode(getLong(arg0, "nauditcode", arg1));
		objAuditComments.setScomments(getString(arg0, "scomments", arg1));
		objAuditComments.setNstatus(getShort(arg0, "nstatus", arg1)); 
		objAuditComments.setJsoncomments(unescapeString(getJsonObject(arg0, "jsoncomments", arg1)));

		return objAuditComments;
	}
}
