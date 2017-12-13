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

    @Test
    public void recognizesNegationOperator() throws SyntaxException {
        assertThat(parser.parse("~1"), is(equalTo(true)));
    }

    @Test(expected = SyntaxException.class)
    public void throwsSyntaxExceptionWhenInputContainsUnrecognizedSymbols() throws SyntaxException {
        parser.parse("1 + 1");
    }

    @Test
    public void recognizesTheOrOperator() throws SyntaxException {
        assertThat(parser.parse("1 | 0"), is(equalTo(true)));
    }

    @Test
    public void recognizesTheAndOperator() throws SyntaxException {
        assertThat(parser.parse("1 ^ 0"), is(equalTo(true)));
    }

    @Test(expected = SyntaxException.class)
    public void throwsSyntaxErrorWhenThereIsNoMatchingClosingBracket() throws SyntaxException {
        parser.parse("(1");
    }

    @Test(expected = SyntaxException.class)
    public void throwsSyntaxErrorWhenThereIsNoMatchingOpeningBracket() throws SyntaxException {
        parser.parse("1)");
    }

    @Test
    public void recognizesExpressionsWithMatchingBrackets() throws SyntaxException {
        assertThat(parser.parse("(1)"), is(equalTo(true)));
    }

    @Test
    public void recognizesChainedOperatorsAndOperands() throws SyntaxException {
        assertThat(parser.parse("~(1 | 0) ^ 1"), is(equalTo(true)));
    }

    @Test
    public void recognizesComplexBooleanExpression() throws SyntaxException {
        assertThat(parser.parse("1 | 1 ^ ~0 | ((1 ^ 0) | ~1)"), is(equalTo(true)));
    }
}
