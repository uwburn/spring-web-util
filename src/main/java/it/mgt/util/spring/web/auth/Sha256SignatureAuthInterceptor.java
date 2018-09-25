package it.mgt.util.spring.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Sha256SignatureAuthInterceptor extends BaseSignatureAuthInterceptor {

	private final static String AUTH_TYPE = "SHA-256 Signature";
    private final static String ALGORITHM = "SHA-256";

	@Override
	protected String getAuthType() {
		return AUTH_TYPE;
	}

	@Override
	protected String getAlgorithm() {
		return ALGORITHM;
	}
}
