package com.wldst.ruder.module.encode;

public class EncodingException extends PermitException {
    public EncodingException(String msg) {
      super(msg);
    }
    
    public EncodingException(String msg, Throwable t) {
      super(msg, t);
    }
  }
