package com.jcaa.appcrudusers.domain.port.out;

import com.jcaa.appcrudusers.domain.model.*;
import java.util.List;

/**
 * Synchronous contract; application execution must run outside the Android main thread.
 * Adapters translate infrastructure failures into DomainException.
 */
public interface ListUsersPort {
    List<User> list();
}
