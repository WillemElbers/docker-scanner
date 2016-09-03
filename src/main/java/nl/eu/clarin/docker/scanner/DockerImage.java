package nl.eu.clarin.docker.scanner;

import java.util.Objects;

/**
 *
 * @author wilelb
 */
public class DockerImage implements Comparable<DockerImage> {

    public static String CLARIN_REPOSITORY = "docker.clarin.eu";
    
    public static DockerImage fromString(String s) {
        String imageRepository = null;
        String imageName = null;
        String imageTag = null;
        int repoSeparator = s.indexOf("/");
        int tagSeparator = s.indexOf(":");
        if(repoSeparator >= 0) {
            imageRepository = s.substring(0, repoSeparator);
            if(tagSeparator > 0) {
                imageName = s.substring(repoSeparator+1, tagSeparator);
                imageTag = s.substring(tagSeparator+1);
            } else {
                imageName = s.substring(repoSeparator+1, tagSeparator);
            }
        } else {
            if(tagSeparator > 0) {
                imageName = s.substring(0, tagSeparator);
                imageTag = s.substring(tagSeparator+1);
            } else {
                imageName = s;
            }
        }
        return new DockerImage(imageRepository, imageName, imageTag);
    }
    
    private String imageRepository;
    private String imageName;
    private String imageTag;
    private DockerImage parent;
    
    public DockerImage() {}
    
    public DockerImage(String imageRepository, String imageName, String imageTag) {
        this.imageRepository = imageRepository;
        this.imageName = imageName;
        this.imageTag = imageTag;
    }
    
    @Override
    public String toString() {
        return (this.imageRepository == null ? "" : this.imageRepository+"/") +
                this.imageName+
                (this.imageTag == null ? "" : ":"+this.imageTag);
    }

    public boolean hasRepository() {
        return this.imageRepository != null;
    }

    public boolean hasTag() {
        return this.imageTag != null;
    }
    
    public boolean isClarinRepository() {
        return this.hasRepository() && this.imageRepository.equalsIgnoreCase(CLARIN_REPOSITORY);

    }
    public String getImageRepository() {
        return imageRepository;
    }

    public void setImageRepository(String imageRepository) {
        this.imageRepository = imageRepository;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }
    
    public boolean isValid() {
        return hasImageName();
    }
    
    public boolean hasImageName() {
        return this.imageName != null;
    }

    public DockerImage getParent() {
        return parent;
    }

    public void setParent(DockerImage parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(DockerImage img) {
        return toString().compareTo(img.toString());
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof DockerImage) {
            return this.hashCode() == o.hashCode();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.imageRepository);
        hash = 73 * hash + Objects.hashCode(this.imageName);
        hash = 73 * hash + Objects.hashCode(this.imageTag);
        hash = 73 * hash + Objects.hashCode(this.parent);
        return hash;
    }
}
