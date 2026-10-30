package com.jcaa.appcrudusers.adapter.cache;

import java.io.IOException;
import java.util.Optional;

/** Internal storage seam; one encrypted blob, no plaintext preferences. */
public interface SessionBlobStore {
    Optional<byte[]> read() throws IOException;
    void write(byte[] encrypted) throws IOException;
    void clear() throws IOException;
}
