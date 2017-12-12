import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TableDrivenParserTest {
    private Parser parser;
    private Grammar grammar;

    public TableDrivenParserTest() throws IOException, BNFGrammarException {
        this.grammar = new Grammar("language.bnf");
        this.parser = new TableDrivenParser(grammar);
    }

    @Test
    public void recognizesSingleTerminalSymbol() throws SyntaxException {
        assertThat(parser.parse("0"), is(equalTo(true)));
    }


}
