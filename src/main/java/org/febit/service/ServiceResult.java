package org.febit.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class ServiceResult {

  public static final int SUCCESS = 0;
  public static final int ERROR_SYS = 100;
  public static final int REDIRECT = 3020;

  public static final int ERROR = 5000;
  //参数错误:
  public static final int ERROR_PARAM = 5100;
  //参数错误:不能为空
  public static final int ERROR_PARAM_REQUIRED = 5101;
  //参数错误:格式错误
  public static final int ERROR_PARAM_FORMAT = 5102;
  //文件上传错误
  public static final int ERROR_UPLOAD = 5200;
  //文件上传错误:文件太大
  public static final int ERROR_UPLOAD_TOOBIG = 5201;
  //文件上传错误:文件无法写入
  public static final int ERROR_UPLOAD_CANTWRITE = 5202;
  //文件上传错误:文件类型错误
  public static final int ERROR_UPLOAD_TYPE = 5203;
  //权限错误
  public static final int ERROR_RIGHT = 5400;
  //XSRF
  public static final int ERROR_XSRF = 5401;
  //验证码错误
  public static final int ERROR_VERCODE = 5402;
  
  public static final int ERROR_ADD = 6100;
  public static final int ERROR_DEL = 6200;
  public static final int ERROR_DEL_NOTFOUND = 6201;
  public static final int ERROR_DEL_UNABLE = 6202;
  public static final int ERROR_MODIFY= 6300;
  public static final int ERROR_MODIFY_NOTFOUND = 6301;
  public static final int ERROR_MODIFY_UNABLE = 6302;
  public static final int ERROR_QUERY = 6400;
  public static final int ERROR_QUERY_NOTFOUND = 6404;

  public static final ServiceResult SUCCESS_RESULT = new ServiceResult(SUCCESS);

  public final int code;
  public final String msg;
  public final Object[] args;
  public Map<Object, Object> map;
  public final Object value;

  protected ServiceResult(Object value) {
    this.code = SUCCESS;
    this.msg = null;
    this.args = null;
    this.value = value;
  }

  protected ServiceResult(int code, String message, Object[] arguments) {
    this.code = code;
    this.msg = message;
    this.args = arguments;
    this.value = null;
  }

  protected ServiceResult(int code) {
    this.code = code;
    this.msg = null;
    this.args = null;
    this.value = null;
  }

  public final boolean success() {
    return code == SUCCESS;
  }

  public final boolean failed() {
    return code != SUCCESS;
  }

  public ServiceResult put(Object key, Object value) {
    Map myMap = this.map;
    if (myMap == null) {
      this.map = myMap = new HashMap<>();
    }
    myMap.put(key, value);
    return this;
  }

  public Object get(Object key) {
    return map != null ? map.get(key) : null;
  }

  public Map<Object, Object> getMap() {
    return map;
  }

  @Override
  public String toString() {
    return "ServiceResult{" + "code=" + code + ", message=" + msg + ", arguments=" + Arrays.toString(args) + '}';
  }

  public static ServiceResult success(Object val) {
    return new ServiceResult(val);
  }

  public static ServiceResult successResult() {
    return new ServiceResult(SUCCESS);
  }

  public static ServiceResult error(int code) {
    return new ServiceResult(code);
  }

  public static ServiceResult error(String msg) {
    return new ServiceResult(ERROR, msg, null);
  }

  public static ServiceResult error(String msg, Object... args) {
    return new ServiceResult(ERROR, msg, args);
  }

  public static ServiceResult error(int code, String msg, Object... args) {
    return new ServiceResult(code, msg, args);
  }
}
