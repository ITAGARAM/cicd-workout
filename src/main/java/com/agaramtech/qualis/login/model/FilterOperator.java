package com.agaramtech.qualis.login.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "filteroperator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FilterOperator extends CustomizedResultsetRowMapper<FilterOperator> implements Serializable, RowMapper<FilterOperator>{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nfilteroperatorcode")
	private short nfilteroperatorcode;
	
	@Column(name = "sfilteroperator", length = 100, nullable = false)
	private String sfilteroperator;
	
	@Column(name = "sdescription", length = 255, nullable = false)
	private String sdescription;
	
	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name ="nsitecode", nullable=false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@Override
	public FilterOperator mapRow(ResultSet arg0, int arg1) throws SQLException {
		final FilterOperator objFilterOperator = new FilterOperator();
		objFilterOperator.setNfilteroperatorcode(getShort(arg0,"nfilteroperatorcode",arg1));
		objFilterOperator.setSfilteroperator(StringEscapeUtils.unescapeJava(getString(arg0,"sfilteroperator",arg1)));
		objFilterOperator.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objFilterOperator.setNstatus(getShort(arg0,"nstatus",arg1));
		objFilterOperator.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objFilterOperator.setNsitecode(getShort(arg0,"nsitecode",arg1)); 
		objFilterOperator.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));

		return objFilterOperator;
	}

}
