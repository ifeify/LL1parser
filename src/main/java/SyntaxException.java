import java.util.List;

public class SyntaxException extends Throwable {
    private String invalidSymbol;

    public SyntaxException(String errorMessage) {
        super(errorMessage);
    }

    public SyntaxException(String invalidSymbol, String errorMessage) {
        super(errorMessage);
        this.invalidSymbol = invalidSymbol;
    }


}
