package com.jcaa.appcrudusers.adapter.cache;

import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    SecretKey getOrCreate() throws GeneralSecurityException;
}
