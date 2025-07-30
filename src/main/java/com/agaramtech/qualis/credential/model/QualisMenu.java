package com.agaramtech.qualis.credential.model;
/**
 * This class is used to map fields of 'qualismenu' table of database
*/
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="qualismenu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QualisMenu extends CustomizedResultsetRowMapper<QualisMenu> implements Serializable,RowMapper<QualisMenu>{
	private static final long serialVersionUID = 1L; 
	@Id
	@Column(name="nsorter", nullable=false) 
	private short nsorter;
	
	@ColumnDefault("1")
    @Column(name="nmenucode") 
	private short nmenucode = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="smenuname",length = 100, nullable=false) 
	private String smenuname;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false) 
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String sdisplayname;
	@Transient
    private transient List<QualisModule> lstmodule;
	@Transient
	private  String sdefaultname;
	@Transient
	private transient String smodulename;
	@Transient
	private transient int nmodulecode;
	@Transient
	private transient String sformname;
	@Transient
	private transient int nformcode;
	@Transient
	private transient String sshortdesc;
	
	
	@Override
	public QualisMenu mapRow(ResultSet arg0, int arg1) throws SQLException {
		 final QualisMenu objQualisMenu = new QualisMenu();
		 objQualisMenu.setNsorter(getShort(arg0,"nsorter",arg1));
		 objQualisMenu.setNmenucode(getShort(arg0,"nmenucode",arg1));
		 objQualisMenu.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		 objQualisMenu.setNstatus(getShort(arg0,"nstatus",arg1));
		 objQualisMenu.setSmenuname(getString(arg0,"smenuname",arg1));
		 objQualisMenu.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		 objQualisMenu.setSdefaultname(getString(arg0,"sdefaultname",arg1));	 
		 objQualisMenu.setSmodulename(getString(arg0,"smodulename",arg1));
		 objQualisMenu.setSformname(getString(arg0,"sformname",arg1));
		 objQualisMenu.setNmodulecode(getInteger(arg0,"nmodulecode",arg1));
		 objQualisMenu.setNformcode(getInteger(arg0,"nformcode",arg1));
		 objQualisMenu.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		 objQualisMenu.setSshortdesc(getString(arg0,"sshortdesc",arg1));

		return objQualisMenu;
	}
	
}

