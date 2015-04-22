package edu.virginia.lib.findingaid.structure;

public class Path {

    private String[] pathElements;

    public Path(String path) {
        pathElements = path.split("/");
    }

    private Path(String[] pathElements) {
        this.pathElements = pathElements;
    }

    public Path relativeToFirst() {
        if (pathElements.length == 1) {
            return null;
        } else {
            String[] newPathElements = new String[pathElements.length - 1];
            System.arraycopy(pathElements, 1, newPathElements, 0, newPathElements.length);
            return new Path(newPathElements);
        }
    }

    public String getPathElement(int index) {
        return pathElements[index];
    }

    public int depth() {
        return pathElements.length;
    }

    public Path getParentPath() {
        if (pathElements.length == 1) {
            return null;
        } else {
            String[] newPathElements = new String[pathElements.length - 1];
            System.arraycopy(pathElements, 0, newPathElements, 0, newPathElements.length);
            return new Path(newPathElements);
        }
    }

    public String getLastPathElement() {
        return pathElements[pathElements.length -1];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String e : pathElements) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(e);
        }
        return sb.toString();
    }
}
