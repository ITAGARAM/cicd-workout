package com.agaramtech.qualis.archivalandpurging.restoreindividual.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
 * This class is used to map the fields of 'restorefilter' table of the Database.
 * 
 */
@Entity
@Table(name = "restorefiltertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RestoreFilter extends CustomizedResultsetRowMapper<RestoreFilter> implements Serializable,RowMapper<RestoreFilter> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nrestorefiltercode")
	private int nrestorefiltercode;
	
	@Column(name = "srestorefiltername", length = 50,nullable = false)
	private String srestorefiltername;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "nsorter", nullable = false)
	private short nsorter;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	

	@Override
	public RestoreFilter mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		RestoreFilter objRestoreFilter = new RestoreFilter();
		objRestoreFilter.setNrestorefiltercode(getInteger(arg0,"nrestorefiltercode",arg1));
		objRestoreFilter.setSrestorefiltername(getString(arg0,"srestorefiltername",arg1));
		objRestoreFilter.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objRestoreFilter.setNsorter(getShort(arg0,"nsorter",arg1));
		objRestoreFilter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objRestoreFilter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRestoreFilter.setNstatus(getShort(arg0,"nstatus",arg1));
				
		return objRestoreFilter;
	}
	
}