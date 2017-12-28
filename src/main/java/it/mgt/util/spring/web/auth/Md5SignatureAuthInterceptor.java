package it.mgt.util.spring.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Md5SignatureAuthInterceptor extends BaseSignatureAuthInterceptor {

	Logger logger = LoggerFactory.getLogger(Md5SignatureAuthInterceptor.class);

	private final static String AUTH_TYPE = "MD5 Signature";
    private final static String ALGORITHM = "MD5";

	@Override
	protected String getAuthType() {
		return AUTH_TYPE;
	}

	@Override
	protected String hash(String input) {
		try {
			MessageDigest md5 = MessageDigest.getInstance(ALGORITHM);
			md5.update(input.getBytes());
			byte[] sha = md5.digest();
			return Base64.getEncoder().encodeToString(sha);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unable to hash password", e);
			return null;
		}
	}
}
