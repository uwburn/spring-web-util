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
	protected String getAlgorithm() {
		return ALGORITHM;
	}
}
