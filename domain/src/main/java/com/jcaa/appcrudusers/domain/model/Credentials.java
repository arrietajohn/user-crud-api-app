package com.jcaa.appcrudusers.domain.model;

import com.jcaa.appcrudusers.domain.vo.*;
import java.util.Objects;

public final class Credentials {
    private final Email email;
    private final Password password;

    public Credentials(Email email, Password password) {
        this.email = Objects.requireNonNull(email, "email");
        this.password = Objects.requireNonNull(password, "password");
    }

    public Email getEmail() { return email; }
    public Password getPassword() { return password; }
}
