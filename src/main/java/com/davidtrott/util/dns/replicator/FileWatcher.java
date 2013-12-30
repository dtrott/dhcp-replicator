package com.davidtrott.util.dns.replicator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class FileWatcher {

    private final Path watchedDir;
    private final File watchedFile;
    private final WatchService watchService;

    public FileWatcher(File watchedFile) throws FileWatcherException {
        try {
            final FileSystem fileSystem = FileSystems.getDefault();
            this.watchedDir = fileSystem.getPath(watchedFile.getParentFile().getAbsolutePath());
            this.watchedFile = watchedFile;
            this.watchService = fileSystem.newWatchService();
        } catch (IOException e) {
            throw new FileWatcherException("Failed to create FileWatcher", e);
        }
    }

    public void watch(final Callback callback) throws FileWatcherException {
        try {
            watchedDir.register(watchService, ENTRY_MODIFY);
        } catch (IOException e) {
            throw new FileWatcherException("Failed to register watch", e);
        }
        final String filename = watchedFile.getName();
        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                if (filename.equals(ev.context().toString())) {
                    callback.changed();
                }
            }

            if (!key.reset()) {
                break;
            }
        }
    }

    public interface Callback {
        public void changed();
    }
}
