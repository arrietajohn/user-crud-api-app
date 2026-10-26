package com.jcaa.appcrudusers.domain.model;

import com.jcaa.appcrudusers.domain.vo.*;
import java.util.Objects;

/** New user data. The server chooses the initial status. */
public final class UserRegistration {
    private final UserId id;
    private final UserName name;
    private final Email email;
    private final Password password;
    private final UserRole role;

    public UserRegistration(UserId id, UserName name, Email email, Password password, UserRole role) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.password = Objects.requireNonNull(password, "password");
        this.role = Objects.requireNonNull(role, "role");
    }

    public UserId getId() { return id; }
    public UserName getName() { return name; }
    public Email getEmail() { return email; }
    public Password getPassword() { return password; }
    public UserRole getRole() { return role; }
}
