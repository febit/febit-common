package org.febit.service;

/**
 *
 * @author zqq90
 */
public class ServiceResultException extends RuntimeException implements ServiceResultCarrier {

    protected final ServiceResult serviceResult;

    public ServiceResultException(String msg) {
        this(ServiceResult.error(msg));
    }

    public ServiceResultException(ServiceResult serviceResult) {
        this.serviceResult = serviceResult;
    }

    public ServiceResultException(ServiceResult serviceResult, String message) {
        super(message);
        this.serviceResult = serviceResult;
    }

    public ServiceResultException(ServiceResult serviceResult, String message, Throwable cause) {
        super(message, cause);
        this.serviceResult = serviceResult;
    }

    public ServiceResultException(ServiceResult serviceResult, Throwable cause) {
        super(cause);
        this.serviceResult = serviceResult;
    }

    public ServiceResultException(ServiceResult serviceResult, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.serviceResult = serviceResult;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public ServiceResult getServiceResult() {
        return serviceResult;
    }
}
