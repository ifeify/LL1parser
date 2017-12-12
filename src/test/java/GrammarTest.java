import com.google.common.collect.Multimap;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    public void recognizesAnotherTerminalSymbolThatBelongsToIt() {
        assertThat(ll1grammar.containsTerminalSymbol(")"), is(equalTo(true)));
    }

    @Test
    public void doesNotRecognizeTerminalSymbol() {
        assertThat(ll1grammar.containsTerminalSymbol("+"), is(equalTo(false)));
    }

    @Test
    public void totalNumberOfProductionsIsCorrect() {
        assertThat(ll1grammar.numberOfProductions(), is(equalTo(12)));
    }

    @Test
    public void computesTheFirstSetsOfProductionsThatBeginWithTerminals() throws BNFGrammarException {
        Set<String> firstSets = ll1grammar.firstOf("D");
        assertThat(firstSets, hasItems("(", "0", "1"));
    }

    // TODO: Only one assert per test. Chain them together
    @Test
    public void computesTheFirstSetsOfProductionsThatBeginWithNonTerminals() throws BNFGrammarException {
        Set<String> firstSets = ll1grammar.firstOf("S");
        assertThat(firstSets.size(), is(equalTo(4)));
        assertThat(firstSets, hasItems("~", "(", "0", "1"));
    }

    @Test
    public void recognizesStartSymbol() throws BNFGrammarException {
        assertThat(ll1grammar.isStartSymbol("S"), is(equalTo(true)));
    }

    @Test
    public void findsTheCorrespondingRulesGivenNonterminalInProductionBody() {
        Multimap<String, String> list = ll1grammar.findRulesWithNonTerminalInBody(("D"));
        assertThat(list.size(), is(equalTo(2)));
        assertThat(list.containsKey("C"), is(equalTo(true)));
    }

    @Test
    public void computesFollowSetsOfProductionBodyEndingWithTerminals() throws BNFGrammarException {
        Set<String> follows = ll1grammar.followSetOf("A");
        assertThat(follows.size(), is(equalTo(2)));
        assertThat(follows, hasItems("$", ")"));
    }

    @Test
    public void computesFollowSetsOfRecursiveProductionRules() throws BNFGrammarException {
//        Set<String> followsOfBprime = ll1grammar.followSetOf("Bprime");
        Set<String> followsOfB = ll1grammar.followSetOf("B");
        assertThat(followsOfB.size(), is(equalTo(2)));
        assertThat(followsOfB, hasItems("$", ")"));
    }

    @Test
    public void computesFollowSetOfEpsilonProductionAsItsFirstSet() throws BNFGrammarException {
        Set<String> firstOfB = ll1grammar.firstOf("B", "");
        Set<String> followsOfB = ll1grammar.followSetOf("B");

        assertEquals(firstOfB, followsOfB);

    }

    @Test(expected = BNFGrammarException.class)
    public void throwsExceptionWhenNonTerminalIsMissingAClosingBracket() throws BNFGrammarException {
        Grammar.nonTerminalsInBNF("'+'<A><B><C");
    }

    @Test
    public void correctlyReturnsNonterminalsFromBNFString() throws BNFGrammarException {
        List<String> nonterminals = Grammar.nonTerminalsInBNF("'+'<B><C>");
        assertThat(nonterminals, hasItems("B", "C"));
    }
}
