package my_types;

public class FileHandler {

    private final int maxCols = 60;
    private final String sepChar = "-";

    private int id;
    private String filePath;

    public FileHandler() {
        this.id = -1;
        this.filePath = "";
    }

    public FileHandler(int id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileNameExt() {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    public String getFileName() {
        return filePath.substring(filePath.lastIndexOf('/') + 1,
                filePath.lastIndexOf('.'));
    }

    public void printTitleSep() {
        StringBuilder sep = new StringBuilder();
        sep.append(sepChar.repeat(10));
        sep.append(" File #").append(id).append(": ");
        sep.append(this.getFileNameExt()).append(" ");
        sep.append(sepChar.repeat(maxCols - sep.length()));
        System.out.println(sep);
    }

    public void printSep() {
        StringBuilder sep = new StringBuilder();
        sep.append(sepChar.repeat(maxCols - sep.length()));
        System.out.println(sep);
    }
}
