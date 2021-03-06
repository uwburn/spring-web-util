package it.mgt.util.spring.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Md5HMacSignatureAuthInterceptor extends BaseHmacSignatureAuthInterceptor {

	Logger logger = LoggerFactory.getLogger(Md5HMacSignatureAuthInterceptor.class);

	private final static String AUTH_TYPE = "HMAC-MD5 Signature";
    private final static String ALGORITHM = "HmacMD5";

	@Override
	protected String getAuthType() {
		return AUTH_TYPE;
	}

	@Override
	protected String getAlgorithm() {
		return ALGORITHM;
	}
}
