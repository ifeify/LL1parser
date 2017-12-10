import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.IOException;

public class GrammarTest {
    private Grammar ll1grammar;

    public GrammarTest() throws IOException, BNFGrammarException {
        ll1grammar = new Grammar("language.bnf");
    }

    @Test
    public void readsNonTerminalSymbolWithAngularBracketsCorrectly() {
        assertThat(ll1grammar.containsNonTerminal("S"), is(equalTo(true)));
    }

    @Test
    public void recognizesTerminalSymbolThatBelongsToIt() {
        assertThat(ll1grammar.containsTerminalSymbol("~"), is(equalTo(true)));
    }

    @Test
    public void doesNotRecognizeTerminalSymbol() {
        assertThat(ll1grammar.containsTerminalSymbol("+"), is(equalTo(false)));
    }

    @Test
    public void totalNumberOfProductionsIsCorrect() {
        assertThat(ll1grammar.totalNumberOfProductions(), is(equalTo(12)));
    }
}
