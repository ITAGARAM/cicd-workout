package com.agaramtech.qualis.storagemanagement.model;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "retrievalcertificate")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RetrievalCertificate extends CustomizedResultsetRowMapper<RetrievalCertificate>
implements Serializable, RowMapper<RetrievalCertificate> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nretrievalcertificatecode")
	private int nretrievalcertificatecode;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "nprojectmastercode")
	private int nprojectmastercode;

	@Column(name = "nstorageconditioncode")
	private int nstorageconditioncode;

	@Column(name = "sretrievalcertificateno", length = 30, nullable = false)
	private String sretrievalcertificateno;

	@Column(name = "sbiomaterialtype", length = 255, nullable = false)
	private String sbiomaterialtype;

	@Column(name = "srequestid", length = 30, nullable = false)
	private String srequestid;

	@Column(name = "stestingmethod", length = 255, nullable = false)
	private String stestingmethod;

	@Column(name = "spreparationmethod", length = 255, nullable = false)
	private String spreparationmethod;

	@Column(name = "sinvestigatorname", length = 50, nullable = false)
	private String sinvestigatorname;

	@Column(name = "sorganizationaddress", length = 255, nullable = false)
	private String sorganizationaddress;

	@Column(name = "sphoneno", length = 50, nullable = false)
	private String sphoneno;

	@Column(name = "semail", length = 50, nullable = false)
	private String semail;

	@Column(name = "scomment", length = 255, nullable = false)
	private String scomment;

	@Column(name = "dretrievalcertificatedate")
	private Instant dretrievalcertificatedate;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "noffsetdretrievalcertificatedate")
	private int noffsetdretrievalcertificatedate;

	@Column(name = "ntzretrievalcertificatedate")
	private short ntzretrievalcertificatedate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Transient
	private transient String sretrievalcertificatedate;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int nretrievalcertificatehistorycode;
	@Transient
	private transient String sprojectname;
	@Transient
	private transient String sstorageconditionname;
	@Transient
	private transient String susername;
	@Transient
	private transient int nusercode;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String sprojecttypename;

	@Override
	public RetrievalCertificate mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RetrievalCertificate objRetrievalCertificate = new RetrievalCertificate();

		objRetrievalCertificate.setNretrievalcertificatecode(getInteger(arg0, "nretrievalcertificatecode", arg1));
		objRetrievalCertificate.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objRetrievalCertificate.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objRetrievalCertificate.setSretrievalcertificateno(
				StringEscapeUtils.unescapeJava(getString(arg0, "sretrievalcertificateno", arg1)));
		objRetrievalCertificate
		.setSbiomaterialtype(StringEscapeUtils.unescapeJava(getString(arg0, "sbiomaterialtype", arg1)));
		objRetrievalCertificate.setSrequestid(StringEscapeUtils.unescapeJava(getString(arg0, "srequestid", arg1)));
		objRetrievalCertificate.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objRetrievalCertificate.setDretrievalcertificatedate(getInstant(arg0, "dretrievalcertificatedate", arg1));
		objRetrievalCertificate.setNtzretrievalcertificatedate(getShort(arg0, "ntzretrievalcertificatedate", arg1));
		objRetrievalCertificate
		.setNoffsetdretrievalcertificatedate(getInteger(arg0, "noffsetdretrievalcertificatedate", arg1));
		objRetrievalCertificate.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objRetrievalCertificate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objRetrievalCertificate.setNstatus(getShort(arg0, "nstatus", arg1));
		objRetrievalCertificate
		.setStestingmethod(StringEscapeUtils.unescapeJava(getString(arg0, "stestingmethod", arg1)));
		objRetrievalCertificate
		.setSpreparationmethod(StringEscapeUtils.unescapeJava(getString(arg0, "spreparationmethod", arg1)));
		objRetrievalCertificate
		.setSinvestigatorname(StringEscapeUtils.unescapeJava(getString(arg0, "sinvestigatorname", arg1)));
		objRetrievalCertificate
		.setSorganizationaddress(StringEscapeUtils.unescapeJava(getString(arg0, "sorganizationaddress", arg1)));
		objRetrievalCertificate.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		objRetrievalCertificate.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objRetrievalCertificate.setSretrievalcertificatedate(getString(arg0, "sretrievalcertificatedate", arg1));
		objRetrievalCertificate.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objRetrievalCertificate
		.setNretrievalcertificatehistorycode(getInteger(arg0, "nretrievalcertificatehistorycode", arg1));
		objRetrievalCertificate.setSprojectname(getString(arg0, "sprojectname", arg1));
		objRetrievalCertificate.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objRetrievalCertificate.setSusername(getString(arg0, "susername", arg1));
		objRetrievalCertificate.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRetrievalCertificate.setScomment(StringEscapeUtils.unescapeJava(getString(arg0, "scomment", arg1)));
		objRetrievalCertificate.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objRetrievalCertificate.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));

		return objRetrievalCertificate;

	}

}