package com.jcaa.appcrudusers.domain;

import com.jcaa.appcrudusers.domain.exception.ValidationException;
import com.jcaa.appcrudusers.domain.model.*;
import com.jcaa.appcrudusers.domain.vo.*;
import java.time.Instant;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

public class DomainRulesTest {
    @Test public void rejectsMissingIdentifiersAndShortNames() {
        assertThrows(ValidationException.class, () -> new UserId("  "));
        assertThrows(ValidationException.class, () -> new UserId(null));
        assertThrows(ValidationException.class, () -> new UserName("ab"));
        assertThrows(ValidationException.class, () -> new UserName("\u2003\u2003\u2003"));
    }

    @Test public void validatesEmailWithoutChangingItsRepresentation() {
        assertThrows(ValidationException.class, () -> new Email("invalid"));
        assertThrows(ValidationException.class, () -> new Email("a b@example.com"));
        assertEquals("Name@example.com", new Email("Name@example.com").getValue());
        assertEquals(new Email("a@example.com"), new Email("a@example.com"));
    }

    @Test public void enforcesPasswordMinimumWithoutTrimmingSecrets() {
        assertThrows(ValidationException.class, () -> new Password("1234567"));
        assertThrows(ValidationException.class, () -> new Password("        "));
        assertEquals(" secret ", new Password(" secret ").getValue());
    }

    @Test public void secretsDoNotAppearInTextRepresentations() {
        assertFalse(new Password("secret123").toString().contains("secret123"));
        assertFalse(new AccessToken("sensitive-token").toString().contains("sensitive-token"));
    }

    @Test public void roleAndStatusAcceptServerValuesWithoutInventedEnums() {
        assertEquals("SERVER_ROLE", new UserRole("SERVER_ROLE").getValue());
        assertEquals("SERVER_STATUS", new UserStatus("SERVER_STATUS").getValue());
        assertThrows(ValidationException.class, () -> new UserRole(""));
        assertThrows(ValidationException.class, () -> new UserStatus(null));
    }

    @Test public void passwordCanBeOmittedWhenUpdating() {
        UserUpdate update = new UserUpdate(new UserName("Ana"), new Email("ana@example.com"),
                Optional.empty(), new UserRole("USER"), new UserStatus("ACTIVE"));
        assertFalse(update.getPassword().isPresent());
    }

    @Test public void expiresExactlyAtTheBoundary() {
        Instant received = Instant.parse("2026-10-26T12:00:00Z");
        AuthSession session = new AuthSession(new AccessToken("token"), "bearer", 60, received);
        assertFalse(session.isExpired(received.plusSeconds(59)));
        assertTrue(session.isExpired(received.plusSeconds(60)));
        assertTrue(session.isExpired(received.plusSeconds(61)));
        assertEquals("Bearer", session.getTokenType());
    }

    @Test public void rejectsInvalidSessionMetadata() {
        Instant now = Instant.parse("2026-10-26T12:00:00Z");
        assertThrows(ValidationException.class,
                () -> new AuthSession(new AccessToken("token"), "Basic", 60, now));
        assertThrows(ValidationException.class,
                () -> new AuthSession(new AccessToken("token"), "Bearer", 0, now));
        assertThrows(ValidationException.class,
                () -> new AuthSession(new AccessToken("token"), "Bearer", Long.MAX_VALUE, now));
    }
}
