package ro.bogdan_mierloiu.websocketserver.exception;

public class ParseFailedException extends RuntimeException {
    public ParseFailedException(String message) {
        super(message);
    }
}
