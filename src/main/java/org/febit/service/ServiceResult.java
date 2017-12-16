/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zqq90
 * @param <T>
 */
public class ServiceResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int OK = 0;
    public static final int SUCCESS = OK;
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
    //未登录
    public static final int ERROR_NOT_LOGIN = 5403;

    public static final int ERROR_ADD = 6100;
    public static final int ERROR_DEL = 6200;
    public static final int ERROR_DEL_NOTFOUND = 6201;
    public static final int ERROR_DEL_UNABLE = 6202;
    public static final int ERROR_MODIFY = 6300;
    public static final int ERROR_MODIFY_NOTFOUND = 6301;
    public static final int ERROR_MODIFY_UNABLE = 6302;
    public static final int ERROR_QUERY = 6400;
    public static final int ERROR_QUERY_NOTFOUND = 6404;

    public static final ServiceResult SUCCESS_RESULT = new ServiceResult(OK);

    public final int code;
    public final String msg;
    public final T value;
    private final Object[] args;
    private Map<Object, Object> datas;

    protected ServiceResult(T value) {
        this.code = OK;
        this.msg = null;
        this.args = null;
        this.value = value;
    }

    protected ServiceResult(int code, String message, Object... arguments) {
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

    public boolean success() {
        return code == OK;
    }

    public boolean failed() {
        return code != OK;
    }

    public ServiceResult put(Object key, Object value) {
        if (this.datas == null) {
            this.datas = new HashMap<>();
        }
        this.datas.put(key, value);
        return this;
    }

    public Object get(Object key) {
        return datas != null ? datas.get(key) : null;
    }

    public Map<Object, Object> getDatas() {
        return datas;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "ServiceResult{" + "code=" + code + ", message=" + msg + ", arguments=" + Arrays.toString(args) + '}';
    }

    public static <T> ServiceResult<T> success(T val) {
        return new ServiceResult<>(val);
    }

    public static ServiceResult successResult() {
        return new ServiceResult(OK);
    }

    public static ServiceResult error(int code) {
        return new ServiceResult(code);
    }

    public static ServiceResult error(String msg) {
        return new ServiceResult(ERROR, msg);
    }

    public static ServiceResult error(String msg, Object... args) {
        return new ServiceResult(ERROR, msg, args);
    }

    public static ServiceResult error(int code, String msg, Object... args) {
        return new ServiceResult(code, msg, args);
    }
}
