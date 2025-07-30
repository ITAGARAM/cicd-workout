package com.agaramtech.qualis.project.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reportinfoproject")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportInfoProject extends CustomizedResultsetRowMapper<ReportInfoProject>
		implements Serializable, RowMapper<ReportInfoProject> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode;

	@Column(name = "nreporttemplatecode ", nullable = false)
	private int nreporttemplatecode;

	@Column(name = "sreporttemplateversion", length = 50)
	private String sreporttemplateversion="";

	@Column(name = "srevision", length = 5)
	private String srevision="";

	@Column(name = "srevisionauthor", length = 100)
	private String srevisionauthor="";

	@Column(name = "sintroduction", length = 500)
	private String sintroduction="";

	@Column(name = "stestproductheadercomments", length = 255)
	private String stestproductheadercomments="";

	@Column(name = "stestproductfootercomments1", length = 500)
	private String stestproductfootercomments1="";

	@Column(name = "stestproductfootercomments2", length = 500)
	private String stestproductfootercomments2="";

	@Column(name = "stestproductfootercomments3", length = 500)
	private String stestproductfootercomments3="";

	@Column(name = "stestproductfootercomments4", length = 500)
	private String stestproductfootercomments4="";

	@Column(name = "ssamplingdetails", length = 500)
	private String ssamplingdetails="";

	@Column(name = "suncertainitymeasurement", length = 500)
	private String suncertainitymeasurement;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int nprojecttypecode;

	@Transient
	private transient String stemplatename;

	@Transient
	private transient int needjsontemplate;

	@Override
	public ReportInfoProject mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		ReportInfoProject obj = new ReportInfoProject();
		obj.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		obj.setSreporttemplateversion(StringEscapeUtils.unescapeJava(getString(arg0, "sreporttemplateversion", arg1)));
		obj.setSrevision(StringEscapeUtils.unescapeJava(getString(arg0, "srevision", arg1)));
		obj.setSrevisionauthor(StringEscapeUtils.unescapeJava(getString(arg0, "srevisionauthor", arg1)));
		obj.setSintroduction(StringEscapeUtils.unescapeJava(getString(arg0, "sintroduction", arg1)));
		obj.setStestproductheadercomments(StringEscapeUtils.unescapeJava(getString(arg0, "stestproductheadercomments", arg1)));
		obj.setStestproductfootercomments1(StringEscapeUtils.unescapeJava(getString(arg0, "stestproductfootercomments1", arg1)));
		obj.setStestproductfootercomments2(StringEscapeUtils.unescapeJava(getString(arg0, "stestproductfootercomments2", arg1)));
		obj.setStestproductfootercomments3(StringEscapeUtils.unescapeJava(getString(arg0, "stestproductfootercomments3", arg1)));
		obj.setStestproductfootercomments4(StringEscapeUtils.unescapeJava(getString(arg0, "stestproductfootercomments4", arg1)));
		obj.setSsamplingdetails(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplingdetails", arg1)));
		obj.setSuncertainitymeasurement(StringEscapeUtils.unescapeJava(getString(arg0, "suncertainitymeasurement", arg1)));
		obj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		obj.setNstatus(getShort(arg0, "nstatus", arg1));
		obj.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		obj.setNreporttemplatecode(getInteger(arg0, "nreporttemplatecode", arg1));
		obj.setStemplatename(getString(arg0, "stemplatename", arg1));
		obj.setNeedjsontemplate(getInteger(arg0, "needjsontemplate", arg1));

		return obj;
	}

}
