import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Grammar {
    private Set<String> terminalSymbols = new TreeSet<>();
    private Multimap<String, List<String>> rules = ArrayListMultimap.create();

    public Grammar(String fileName) throws IOException, BNFGrammarException {
        Path file = Paths.get(fileName);
        if(!Files.exists(file)) {
            throw new FileNotFoundException("Could not find " + fileName);
        }
        parse(file);
    }

    private void parse(Path file) throws IOException, BNFGrammarException {
        List<String> productions = Files.lines(file).collect(Collectors.toList());
        for(String line : productions) {
            String[] production = line.split("::=");

            // first index has the production head. Second index has the body
            String head = production[0].trim();
            if(!head.startsWith("<") || !head.endsWith(">")) {
                throw new BNFGrammarException("Production head must be enclosed in angular brackets <>");
            }
            String productionHead = head.substring(1, head.length() - 1);

            char[] body = production[1].trim().toCharArray();
            List<String> productionBody = new ArrayList<>();

            // if rule body is epsilon, put an empty list as its body
            if(body.length == 2 && body[0] == '\'' && body[1] == '\'') {
                rules.put(productionHead, new ArrayList<>());
                continue;
            }
            for(int i = 0; i < body.length; i++) {
                if(body[i] == '\'') { // terminal symbols are enclosed in '' e.g '+'
                    String terminalSymbol = "" + body[i+1];
                    terminalSymbols.add(terminalSymbol);
                    productionBody.add(terminalSymbol);
                    i += 2; // move to the next symbol in the body
                } else if(body[i] == '<') { // encountered a non-terminal
                    ++i;
                    String nonTerminal = "";
                    while(body[i] != '>') {
                        nonTerminal += body[i];
                        ++i;
                    }
                    productionBody.add(nonTerminal);
                }
            }
            rules.put(productionHead, productionBody);
        }
    }

    public String[] findFirstSet(String production) {
        return null;
    }

    public Set<String> nonTerminals() {
        return rules.keySet();
    }

    public Set<String> terminals() {
        return terminalSymbols;
    }

    public boolean containsTerminalSymbol(String symbol) {
        return terminalSymbols.contains(symbol);
    }

    public boolean containsNonTerminal(String symbol) {
        return rules.containsKey(symbol);
    }

    public int totalNumberOfProductions() {
        return rules.size();
    }
}
