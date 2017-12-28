package it.mgt.util.spring.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Sha256HMacSignatureAuthInterceptor extends BaseHmacSignatureAuthInterceptor {

	Logger logger = LoggerFactory.getLogger(Sha256HMacSignatureAuthInterceptor.class);

	private final static String AUTH_TYPE = "HMAC-SHA-256 Signature";
    private final static String ALGORITHM = "HmacSHA256";

	@Override
	protected String getAuthType() {
		return AUTH_TYPE;
	}

	@Override
	protected String hmac(String input, String key) {
		try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(), ALGORITHM));
            byte[] hmac = mac.doFinal(input.getBytes());
			return Base64.getEncoder().encodeToString(hmac);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unable to hash password", e);
			return null;
		} catch (InvalidKeyException e) {
            logger.error("Unable to hash password", e);
            return null;
        }
    }
}
