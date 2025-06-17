package com.wldst.ruder.module.encode;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataAccessException;

public class Md5PasswordEncoder extends BaseDigestPasswordEncoder implements PasswordEncoder {
    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws EncodingException {
	String pass1 = encPass;
	String pass2 = encodeInternal(mergePasswordAndSalt(rawPass, salt, false));
	return pass1.equals(pass2);
    }

    @Override
    public String encodePassword(String rawPass, Object salt) throws EncodingException {
	return encodeInternal(mergePasswordAndSalt(rawPass, salt, false));
    }

    private String encodeInternal(String input) {
	if (!getEncodeHashAsBase64())
	    return DigestUtils.md5Hex(input);
	byte[] encoded = Base64.encodeBase64(DigestUtils.md5(input));
	return new String(encoded);
    }

    @Override
    public String decodePassword(String rawPass, Object salt) throws DataAccessException, EncodingException {
	throw new EncodingException("");
    }
}
