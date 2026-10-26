package com.jcaa.appcrudusers.domain.model;

import com.jcaa.appcrudusers.domain.vo.*;
import java.util.Objects;
import java.util.Optional;

/** Empty password means preserve the existing password, never send an empty string. */
public final class UserUpdate {
    private final UserName name;
    private final Email email;
    private final Optional<Password> password;
    private final UserRole role;
    private final UserStatus status;

    public UserUpdate(UserName name, Email email, Optional<Password> password, UserRole role, UserStatus status) {
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.password = Objects.requireNonNull(password, "password");
        this.role = Objects.requireNonNull(role, "role");
        this.status = Objects.requireNonNull(status, "status");
    }

    public UserName getName() { return name; }
    public Email getEmail() { return email; }
    public Optional<Password> getPassword() { return password; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
}
