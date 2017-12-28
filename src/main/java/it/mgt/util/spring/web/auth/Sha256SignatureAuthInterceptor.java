package it.mgt.util.spring.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Sha256SignatureAuthInterceptor extends BaseSignatureAuthInterceptor {

	Logger logger = LoggerFactory.getLogger(Sha256SignatureAuthInterceptor.class);

	private final static String AUTH_TYPE = "SHA-256 Signature";
    private final static String ALGORITHM = "SHA-256";

	@Override
	protected String getAuthType() {
		return AUTH_TYPE;
	}

	@Override
	protected String hash(String input) {
		try {
			MessageDigest sha256 = MessageDigest.getInstance(ALGORITHM);
			sha256.update(input.getBytes());
			byte[] sha = sha256.digest();
			return Base64.getEncoder().encodeToString(sha);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unable to hash password", e);
			return null;
		}
	}
}
