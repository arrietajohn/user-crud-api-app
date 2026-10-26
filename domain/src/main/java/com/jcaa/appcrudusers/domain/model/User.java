package com.jcaa.appcrudusers.domain.model;

import com.jcaa.appcrudusers.domain.vo.*;
import java.util.Objects;

/** User returned by the API. Credentials are deliberately absent. */
public final class User {
    private final UserId id;
    private final UserName name;
    private final Email email;
    private final UserRole role;
    private final UserStatus status;

    public User(UserId id, UserName name, Email email, UserRole role, UserStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.role = Objects.requireNonNull(role, "role");
        this.status = Objects.requireNonNull(status, "status");
    }

    public UserId getId() { return id; }
    public UserName getName() { return name; }
    public Email getEmail() { return email; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
}
