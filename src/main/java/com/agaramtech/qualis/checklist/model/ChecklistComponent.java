package com.agaramtech.qualis.checklist.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@Entity
@Table(name="checklistcomponent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ChecklistComponent extends CustomizedResultsetRowMapper<ChecklistComponent> implements RowMapper<ChecklistComponent>,Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nchecklistcomponentcode" ) 
	private short nchecklistcomponentcode;
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false )
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Transient
	private transient String scomponentname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;

	
	@Override
	public ChecklistComponent mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistComponent checklistComponent=new ChecklistComponent();
		checklistComponent.setNchecklistcomponentcode(getShort(arg0,"nchecklistcomponentcode",arg1));
		checklistComponent.setScomponentname(getString(arg0,"scomponentname",arg1));
		checklistComponent.setNstatus(getShort(arg0,"nstatus",arg1));
		checklistComponent.setNsitecode(getShort(arg0,"nsitecode",arg1));
		checklistComponent.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		checklistComponent.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		checklistComponent.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		checklistComponent.setSdefaultname(getString(arg0, "sdefaultname", arg1));

		return checklistComponent;
	}

}
