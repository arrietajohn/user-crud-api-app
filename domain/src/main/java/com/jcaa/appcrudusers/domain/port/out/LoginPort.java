package com.jcaa.appcrudusers.domain.port.out;

import com.jcaa.appcrudusers.domain.model.*;

/**
 * Synchronous contract; application execution must run outside the Android main thread.
 * Adapters translate infrastructure failures into DomainException.
 */
public interface LoginPort {
    AuthSession login(Credentials credentials);
}
