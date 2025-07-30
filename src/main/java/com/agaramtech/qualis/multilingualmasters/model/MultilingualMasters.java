package com.agaramtech.qualis.multilingualmasters.model;

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
@Table(name="multilingualmasters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MultilingualMasters extends CustomizedResultsetRowMapper<MultilingualMasters> implements Serializable,RowMapper<MultilingualMasters>{

	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "nmultilingualmasterscode")
	private short nmultilingualmasterscode;
	
	@Column(name = "smultilingualmastername", length = 50, nullable = false)
	private String smultilingualmastername;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "nsorter", nullable = false)
	private short nsorter;
	
	@Column(name = "dmodifieddate")	
	private Instant dmodifieddate;	
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable = false) 
	private short nsitecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String sdefaultname;
	
	@Transient
	private transient String sneedheader;
	
	@Transient
	private transient String displayname;
	
	@Transient
	private transient int nattachmenttypecode;
	
	@Transient
	private transient int nftptypecode;
	
	public MultilingualMasters mapRow(ResultSet arg0, int arg1) throws SQLException {
		final MultilingualMasters objmultilingualmasters = new MultilingualMasters();
		objmultilingualmasters.setNmultilingualmasterscode(getShort(arg0, "nmultilingualmasterscode", arg1));
		objmultilingualmasters.setSmultilingualmastername(StringEscapeUtils.unescapeJava(getString(arg0, "smultilingualmastername", arg1)));
		objmultilingualmasters.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objmultilingualmasters.setNsorter(getShort(arg0, "nsorter", arg1));
		objmultilingualmasters.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objmultilingualmasters.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objmultilingualmasters.setNstatus(getShort(arg0, "nstatus", arg1));
		objmultilingualmasters.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objmultilingualmasters.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objmultilingualmasters.setSneedheader(getString(arg0, "sneedheader", arg1));
		objmultilingualmasters.setDisplayname(getString(arg0, "displayname", arg1));
		objmultilingualmasters.setNattachmenttypecode(getInteger(arg0, "nattachmenttypecode", arg1));
		objmultilingualmasters.setNftptypecode(getInteger(arg0, "nftptypecode", arg1));
		return objmultilingualmasters;
	}

}
