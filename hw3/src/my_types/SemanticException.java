package my_types;

public class SemanticException extends Exception {

    public SemanticException(String message) {
        super("[SEM_ERROR] " + message);
    }
}
