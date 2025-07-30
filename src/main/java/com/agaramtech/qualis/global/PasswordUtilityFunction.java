package com.agaramtech.qualis.global;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PasswordUtilityFunction {
	
	
	private final JdbcTemplate jdbcTemplate;

	

	/**
	 * This method is used to convert the byte array to the string.
	 * 
	 * @author Sudharshanan
	 * @param data a byte array
	 * @return a String value
	 */
	private String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}
	
	/**
	 * This method is used to encrypt the plain text password to a encrypted
	 * password and update the encrypted password to the corresponding column of the
	 * table.
	 * 
	 * @author Sudharshanan
	 * @param stablename      a String value, table name where the encrypted
	 *                        password need to be updated.
	 * @param sprimaryField   a String value,primary key field name of the table
	 *                        where the encrypted password need to be updated.
	 * @param nprimaryValue   a String value,primary key value of the record where
	 *                        the encrypted password need to be updated.
	 * @param sPassword       a String value,plain text password.
	 * @param spasswordColumn a String value,column name of the table where the
	 *                        encrypted password need to be updated.
	 * @throws Exception
	 */
	public void encryptPassword(String stablename, String sprimaryField, int nprimaryValue, String sPassword,
			String spasswordColumn) throws Exception {

		byte[] sPasswordBytes = sPassword.getBytes();
		Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, getIV("agaraminitvector"));
		encryptionCipher.init(Cipher.ENCRYPT_MODE, getKey("smilsilauqsymmetrickey"), spec);
		byte[] encryptedBytes = encryptionCipher.doFinal(sPasswordBytes);
		String encryptedPassword = encode(encryptedBytes);
		final String updateQry = "update " + stablename + " set " + spasswordColumn + " = N'" + encryptedPassword
				+ "'  WHERE " + sprimaryField + " = " + nprimaryValue + ";";
		jdbcTemplate.execute(updateQry);

	}
	
	/**
	 * This method is used to decrypt the cipher text password to a plain text
	 * password .
	 * 
	 * @author Sudharshanan
	 * @param stablename      a String value, table name where the encrypted
	 *                        password need to be fetched.
	 * @param sprimaryField   a String value,primary key field name of the table
	 *                        where the encrypted password need to be fetched.
	 * @param nprimaryValue   a String value,primary key value of the record where
	 *                        the encrypted password need to be fetched.
	 * @param spasswordcolumn a String value,column name of the table where the
	 *                        encrypted password need to be fetched.
	 * @return the plain text password as a String.
	 * @throws Exception
	 */
	public String decryptPassword(String stablename, String sprimaryField, int nprimaryValue, String spasswordcolumn)
			throws Exception {

		final String getPassQry = "select " + sprimaryField + "," + spasswordcolumn + " as spassword from " + stablename
				+ " where " + sprimaryField + "=" + nprimaryValue + ";";
		List<Map<String, Object>> lstResult = jdbcTemplate.queryForList(getPassQry);
		String sPassword = "";
		if (!lstResult.isEmpty()) {
			Map<String, Object> objMap = lstResult.get(0);
			sPassword = (String) objMap.get("spassword");
		}
		if (sPassword != null) {
			byte[] messageInBytes = decode(sPassword);
			Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec spec = new GCMParameterSpec(128, getIV("agaraminitvector"));
			decryptionCipher.init(Cipher.DECRYPT_MODE, getKey("smilsilauqsymmetrickey"), spec);
			byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
			return new String(decryptedBytes);
		} else {
			return null;
		}
	}
	
	/**
	 * This method is used to convert a string to the byte array.
	 * 
	 * @author Sudharshanan
	 * @param data a string value
	 * @return a byte array
	 */
	private byte[] decode(String data) {
		return Base64.getMimeDecoder().decode(data);
	}
	
	/**
	 * This method is used to create a new Secret key spec for the cipher.
	 * 
	 * @author Sudharshanan
	 * @param secretKey
	 * @return SecretKeySpec
	 */
	public SecretKeySpec getKey(String secretKey) {
		return new SecretKeySpec(decode(secretKey), "AES");
	}

	/**
	 * This method is used to get the byte array for Initialization vector string.
	 * 
	 * @author Sudharshanan
	 * @param IV a String to store a Initialization vector
	 * @return a byte array of Initialization vector String
	 */
	public byte[] getIV(String IV) {
		return decode(IV);
	}

}
