package com.jcaa.appcrudusers.domain.port.out;


/**
 * Synchronous contract; application execution must run outside the Android main thread.
 * Adapters translate infrastructure failures into DomainException.
 */
public interface ClearSessionPort {
    void clear();
}
