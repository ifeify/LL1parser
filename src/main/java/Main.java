import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    private static final String BANNER_FILE = "banner.txt";

    public static void main(String[] args) throws IOException, BNFGrammarException {
        Grammar grammar = new Grammar("language.bnf");
        Parser parser = new TableDrivenParser(grammar);
        Scanner scanner = new Scanner(System.in);

        printBanner();

        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            try {
                parser.parse(input);
                System.out.println("Valid input");
            } catch (SyntaxException e) {
                System.out.println("Syntax error.");
                System.out.println(e.getMessage());
                System.out.println();
            }
        }
    }

    private static void printBanner() throws IOException {
        Path path = Paths.get(BANNER_FILE);
        if(Files.exists(path)) {
            Stream<String> banner = Files.lines(path);
            banner.forEach(System.out::println);
            System.out.println("Enter a boolean expression");
        }
    }
}
