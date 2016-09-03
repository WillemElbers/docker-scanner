package nl.eu.clarin.docker.scanner.model;

import java.io.File;

/**
 *
 * @author wilelb
 */
public class Project {

    private final File directory;
    private final File dockerFile;
    private final File makeFile;

    public Project(File directory, File dockerFile, File makeFile) {
        this.directory = directory;
        this.dockerFile = dockerFile;
        this.makeFile = makeFile;
    }

    public File getDockerFile() {
        return dockerFile;
    }

    public File getMakeFile() {
        return makeFile;
    }

    public File getDirectory() {
        return directory;
    }
}
