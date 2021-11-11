package org.apache.tvm.contrib;

/**
 * Wrapper runtime exception(Sonar qube bugfix).
 */
public class TVMRuntimeException extends RuntimeException {
    public TVMRuntimeException(String message) {
        super(message);
    }
}
