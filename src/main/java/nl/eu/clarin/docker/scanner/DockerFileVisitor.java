package nl.eu.clarin.docker.scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class DockerFileVisitor extends SimpleFileVisitor<Path> {
    
    private final Logger logger = LoggerFactory.getLogger(DockerFileVisitor.class);
    
    private long fileCount;
    private long directoryCount;
    
    private final List<File> dockerFiles = new ArrayList<>();
    private final List<File> makeFiles = new ArrayList<>();
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        File f = file.toFile();
        if(f.getName().equalsIgnoreCase("Dockerfile")) {
            dockerFiles.add(file.toFile());
        } else if(f.getName().equalsIgnoreCase("Makefile")) {
            makeFiles.add(file.toFile());
        }
        fileCount++;
        return CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        directoryCount++;
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        logger.error("", exc);
        return CONTINUE;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getDirectoryCount() {
        return directoryCount;
    }

    public List<File> getDockerFiles() {
        return dockerFiles;
    }

    public List<File> getMakeFiles() {
        return makeFiles;
    }
}
