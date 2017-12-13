import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.text.MessageFormat;
import java.util.*;

public class ParseTable {
    private Grammar grammar;
    private Table<String, String, String> table = HashBasedTable.create();

    public ParseTable(Grammar grammar) {
        this.grammar = grammar;
    }

    public void generate() throws BNFGrammarException {
//        System.out.println("------ PARSE TABLE -------");
        for(Map.Entry<String, String> rule : grammar.getRules().entries()) {
            String productionHead = rule.getKey();
            String productionBody = rule.getValue();

//            System.out.println("Production Head: " + productionHead);
//            System.out.println("Production Body: " + productionBody);

            Set<String> firstSets = grammar.firstOf(productionHead, productionBody);
//            System.out.println("First sets are: ");
//            firstSets.forEach(System.out::print);

//            System.out.println();
            for(String terminal : firstSets) {
                System.out.println(MessageFormat.format("T[{0}, {1}] = {2}",
                                                            productionHead,
                                                                terminal, productionBody));
                table.put(productionHead, terminal, productionBody);
            }
        }
    }

    public Optional<String> ruleToApply(String nonTerminal, String terminal) {
//        System.out.println(MessageFormat.format("Searching for T[{0}, {1}]", nonTerminal, terminal));
        if(table.contains(nonTerminal, terminal)) {
            return Optional.of(table.get(nonTerminal, terminal));
        }
        return Optional.empty();
    }
}
