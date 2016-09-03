package nl.eu.clarin.docker.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.eu.clarin.docker.scanner.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class DockerScanner {

    private final Logger logger = LoggerFactory.getLogger(DockerScanner.class);

    private DockerFileVisitor dfv = new DockerFileVisitor();
    private List<Project> projects = new ArrayList<>();

    public List<Project> getFilterRestult() {
        return projects;
    }

    /**
     * Scan the input directory and index all Dockerfiles and Makefiles.
     *
     * @param f
     * @return
     */
    public DockerScanner scan(File f) {
        if (f.isDirectory()) {
            //throw exception
        }

        long t1 = System.nanoTime();
        doScan(f.toPath());
        long t2 = System.nanoTime();
        logger.info(
                "Scanned {} files and {} directories in {} ms",
                dfv.getFileCount(),
                dfv.getDirectoryCount(),
                String.format("%.4f", nsToMs(t1, t2)));
        return this;
    }

    /**
     * Filter the scan result and keep those directories which both contain a
     * Dockerfile and a Makefile. These are considered projects.
     *
     * @return
     */
    public DockerScanner filter() {
        long t1 = System.nanoTime();
        doFilter(dfv.getDockerFiles(), dfv.getMakeFiles());
        long t2 = System.nanoTime();
        logger.info(
                "Filtered {} dockerfiles and {} makefiles in {} ms",
                dfv.getDockerFiles().size(),
                dfv.getMakeFiles().size(),
                String.format("%.4f", nsToMs(t1, t2)));
        return this;
    }

    /**
     * Analuze each project.
     *
     * @return
     */
    public DockerScanner analyze() {
        long t1 = System.nanoTime();
        doAnalyze(projects);
        long t2 = System.nanoTime();
        logger.info(
                "Analyzed {} projects in {} ms",
                projects.size(),
                String.format("%.4f", nsToMs(t1, t2)));
        return this;
    }

    /**
     * Convert an interval in nanoseconds to a duration in milliseconds.
     *
     * @param t1
     * @param t2
     * @return
     */
    private double nsToMs(long t1, long t2) {
        return nsToMs(t2 - t1);
    }

    /**
     * Convert a duration in nanoseconds to a duration in milliseconds.
     *
     * @param ns
     * @return
     */
    private double nsToMs(long ns) {
        return ns / 1000000.0;
    }

    private void doScan(Path p) {
        dfv = new DockerFileVisitor();
        try {
            long t1 = System.nanoTime();
            Files.walkFileTree(p, dfv);
            long t2 = System.nanoTime();
        } catch (IOException ex) {
            logger.error("", ex);
        }
    }

    private void doFilter(List<File> dockerFiles, List<File> makeFiles) {
        projects = new ArrayList<>();
        for (File dockerFile : dockerFiles) {
            File parent = dockerFile.getParentFile();
            for (File makeFile : makeFiles) {
                if (parent.compareTo(makeFile.getParentFile()) == 0) {
                    projects.add(new Project(parent, dockerFile, makeFile));
                }
            }
        }
    }

    private void doAnalyze(List<Project> projects) {
        List<DockerImage> images = new ArrayList<>();
        Set<DockerImage> roots = new HashSet<>();

        for (Project p : projects) {
            DockerImage main = getImageFromMakefile(p.getMakeFile());
            if (main != null) {
                DockerImage from = getDependencyFromDockerfile(p.getDockerFile());
                if (from != null) {
                    main.setParent(from);
                    images.add(main);

                    if (!from.isClarinRepository()) {
                        roots.add(from);
                    }
                }
            }
        }

        for (DockerImage img : roots) {
            print(img, 0);
            printImagesWithParent(images, img, 1);
        }
    }

    private void printImagesWithParent(List<DockerImage> images, DockerImage parent, int level) {
        for(DockerImage img : images) {
            if(img.getParent().equals(parent)) {
                int nextLevel = level+1;
                print(img, nextLevel);
                printImagesWithParent(images, img, nextLevel);
            }
        }
    }

    private void print(DockerImage img, int level) {
        String prefix = "";
        for (int i = 0; i < level; i++) {
            prefix += "\t";
        }
        logger.info("{}{}", prefix, img);
    }

    private DockerImage getDependencyFromDockerfile(File dockerFile) {
        DockerImage fromDependency = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dockerFile)));
            String line = null;
            while ((line = br.readLine()) != null && fromDependency == null) {
                if (line.startsWith("FROM")) {
                    fromDependency = DockerImage.fromString(line.substring(4, line.length()).trim());
                }
            }
        } catch (IOException ex) {
            logger.error("Failed to read Dockerfile: " + dockerFile, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    logger.error("Failed to close Dockerfile: " + dockerFile, ex);
                }
            }
        }
        return fromDependency;
    }

    private DockerImage getImageFromMakefile(File makeFile) {
        DockerImage img = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(makeFile)));
            String line = null;
            img = new DockerImage();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("REPOSITORY")) {
                    img.setImageRepository(getStringValue(line));
                } else if (line.startsWith("NAME")) {
                    img.setImageName(getStringValue(line));
                } else if (line.startsWith("VERSION")) {
                    img.setImageTag(getStringValue(line));
                }
            }

            if (!img.isValid()) {
                img = null;
            }
        } catch (IOException ex) {
            logger.error("Failed to read Dockerfile: " + makeFile, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    logger.error("Failed to close Dockerfile: " + makeFile, ex);
                }
            }
        }
        return img;
    }

    private String getStringValue(String var) {
        String value = var.split("=")[1];
        if (value == null) {
            throw new RuntimeException("Variable has no value");
        }
        return value.trim().replaceAll("\"", "");
    }
}
