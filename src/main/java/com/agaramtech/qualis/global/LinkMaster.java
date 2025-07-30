package com.agaramtech.qualis.global;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

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


/**
 * This class is used to map the fields of 'linkmaster' table of the Database.
 */
@Entity
@Table(name = "linkmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LinkMaster extends CustomizedResultsetRowMapper<LinkMaster> implements Serializable,RowMapper<LinkMaster>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nlinkcode")
	private int nlinkcode;
	
	@Column(name="slinkname",length=50,columnDefinition="nvarchar")
	private String slinkname;
	
	@Column(name="sdesc",length=50,columnDefinition="nvarchar")
	private String sdesc;
	
	@Column(name="ndefaultlink")
	private short ndefaultlink;
	
	@Column(name="nsitecode")
	private short nsitecode;
	
	@Column(name="nstatus")
	private short nstatus;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")	
	private Map<String, Object> jsondata;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;	
	
	@Transient
	private transient short nattachmenttypecode;

	@Override
	public LinkMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final LinkMaster objLinkMaster = new LinkMaster();
		objLinkMaster.setNlinkcode(getInteger(arg0,"nlinkcode",arg1));
		objLinkMaster.setSlinkname(StringEscapeUtils.unescapeJava(getString(arg0,"slinkname",arg1)));
		objLinkMaster.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objLinkMaster.setSdesc(StringEscapeUtils.unescapeJava(getString(arg0,"sdesc",arg1)));
		objLinkMaster.setNdefaultlink(getShort(arg0,"ndefaultlink",arg1));
		objLinkMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objLinkMaster.setNstatus(getShort(arg0,"nstatus",arg1)); 
		objLinkMaster.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objLinkMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return objLinkMaster;
	}

	

}
