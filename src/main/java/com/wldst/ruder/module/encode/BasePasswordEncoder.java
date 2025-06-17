package com.wldst.ruder.module.encode;

public abstract class BasePasswordEncoder implements PasswordEncoder {
    protected String[] demergePasswordAndSalt(String mergedPasswordSalt) throws EncodingException {
	if (mergedPasswordSalt == null || "".equals(mergedPasswordSalt))
	    throw new EncodingException("密码串mergedPasswordSalt为空，操作出错。");
	String password = mergedPasswordSalt;
	String salt = "";
	int saltBegins = mergedPasswordSalt.lastIndexOf("{");
	if (saltBegins != -1 && saltBegins + 1 < mergedPasswordSalt.length()) {
	    salt = mergedPasswordSalt.substring(saltBegins + 1, mergedPasswordSalt.length() - 1);
	    password = mergedPasswordSalt.substring(0, saltBegins);
	}
	return new String[] { password, salt };
    }

    protected String mergePasswordAndSalt(String password, Object salt, boolean strict) throws EncodingException {
	if (password == null)
	    password = "";
	if (strict && salt != null
		&& (salt.toString().lastIndexOf("{") != -1 || salt.toString().lastIndexOf("}") != -1))
	    throw new EncodingException("密码算子对象salt异常，无法使用salt.toString(),操作出错。");
	if (salt == null || "".equals(salt))
	    return password;
	return String.valueOf(password) + "{" + salt.toString() + "}";
    }
}
