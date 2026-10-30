package com.jcaa.appcrudusers.adapter.cache;

import android.content.Context;
import android.util.AtomicFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/** Stored in noBackupFilesDir: excluded from both cloud backup and device transfer. */
public final class AtomicFileSessionBlobStore implements SessionBlobStore {
    private static final String SESSION_FILE_NAME = "session.v1";
    private static final String BACKUP_SUFFIX = ".bak";
    private static final String NEW_SUFFIX = ".new";
    private static final String SESSION_TOO_LARGE_MESSAGE = "Sesion demasiado grande.";
    private static final String WRITE_CONFIRMATION_ERROR_MESSAGE =
            "No se pudo confirmar la escritura de la sesion.";
    private static final String CLEAR_SESSION_ERROR_MESSAGE = "No se pudo borrar la sesion.";
    private static final int MAX_BYTES = 1024 * 1024;
    private static final int BUFFER_SIZE_BYTES = 4096;
    private static final int END_OF_STREAM = -1;

    private final AtomicFile file;

    public AtomicFileSessionBlobStore(final Context context) {
        file = new AtomicFile(new File(context.getApplicationContext().getNoBackupFilesDir(), SESSION_FILE_NAME));
    }

    @Override
    public synchronized Optional<byte[]> read() throws IOException {
        try (FileInputStream input = file.openRead();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[BUFFER_SIZE_BYTES];
            int count;
            while ((count = input.read(buffer)) != END_OF_STREAM) {
                if (output.size() + count > MAX_BYTES) {
                    throw new IOException(SESSION_TOO_LARGE_MESSAGE);
                }
                output.write(buffer, 0, count);
            }
            return Optional.of(output.toByteArray());
        } catch (final FileNotFoundException exception) {
            // AtomicFile also reports permission/read failures this way. Do not mask them.
            if (file.getBaseFile().exists() || siblingFile(BACKUP_SUFFIX).exists()) {
                throw exception;
            }
            return Optional.empty();
        }
    }

    @Override
    public synchronized void write(final byte[] encrypted) throws IOException {
        if (encrypted.length > MAX_BYTES) {
            throw new IOException(SESSION_TOO_LARGE_MESSAGE);
        }
        final FileOutputStream output = file.startWrite();
        boolean completed = false;
        try {
            output.write(encrypted);
            output.flush();
            output.getFD().sync();
            file.finishWrite(output);
            completed = true;
            // AtomicFile logs some rename failures instead of throwing them.
            final Optional<byte[]> persisted = read();
            if (!persisted.isPresent() || !Arrays.equals(encrypted, persisted.get())) {
                throw new IOException(WRITE_CONFIRMATION_ERROR_MESSAGE);
            }
        } catch (final IOException | RuntimeException exception) {
            if (!completed) {
                file.failWrite(output);
            }
            throw exception;
        }
    }

    @Override
    public synchronized void clear() throws IOException {
        file.delete();
        if (file.getBaseFile().exists()
                || siblingFile(BACKUP_SUFFIX).exists()
                || siblingFile(NEW_SUFFIX).exists()) {
            throw new IOException(CLEAR_SESSION_ERROR_MESSAGE);
        }
    }

    private File siblingFile(final String suffix) {
        return new File(file.getBaseFile().getPath() + suffix);
    }
}
