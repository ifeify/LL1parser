import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseTable {
    private Grammar grammar;
    private Table<String, String, String> table = HashBasedTable.create();

    public ParseTable(Grammar grammar) {
        this.grammar = grammar;
        initialize();
    }

    private void initialize() {
        for(String nonTerminal : grammar.nonTerminals()) {
            for(String terminal : grammar.terminals()) {
                table.put(nonTerminal, terminal, null);
            }
        }
    }

    public void generate() throws BNFGrammarException {
        for(Map.Entry<String, String> rule : grammar.getRules().entries()) {
            String productionHead = rule.getKey();
            String productionBody = rule.getValue();

            Set<String> firstSets = grammar.firstOf(productionHead, productionBody);
            for(String terminal : firstSets) {
                table.put(productionHead, terminal, productionBody);
            }
        }
    }
}
