package org.febit.lang.protocal;

@SuppressWarnings({
        "squid:S1609" // @FunctionalInterface annotation should be used to flag Single Abstract Method interfaces
})
public interface HttpStatusAware {

    void setHttpStatus(int status);
}
