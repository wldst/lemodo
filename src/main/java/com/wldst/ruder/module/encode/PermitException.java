package com.wldst.ruder.module.encode;

 

public class PermitException extends Exception {
  public PermitException(String msg) {
    super(msg);
  }
  
  public PermitException(String msg, Throwable t) {
    super(msg, t);
  }
}
