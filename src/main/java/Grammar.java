import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Grammar {
    public static final String EOF = "$";
    public static final String EPSILON = "@#&";

    private String startSymbol; // always a non terminal
    private Set<String> terminals = new HashSet<>();
    private Multimap<String, String> rules = ArrayListMultimap.create();

    public Grammar(String fileName) throws IOException, BNFGrammarException {
        Path file = Paths.get(fileName);
        if(!Files.exists(file)) {
            throw new FileNotFoundException("Could not find " + fileName);
        }
        parse(file);
    }

    private void parse(Path file) throws IOException, BNFGrammarException {
        List<String> productions = Files.lines(file).collect(Collectors.toList());
        int ruleNumber = 1;

        // save start symbol
        String firstProduction = productions.get(0);
        startSymbol = firstProduction.substring(1, firstProduction.indexOf(">", 0));

        for(String line : productions) {
            String[] production = line.split("::=");

            // first index has the production head. Second index has the body
            String head = production[0].trim();
            if(!head.startsWith("<") || !head.endsWith(">")) {
                throw new BNFGrammarException("Production head must be enclosed in angular brackets <>");
            }
            String productionHead = head.substring(1, head.length() - 1);

            String productionBody = production[1].trim();
            char[] body = productionBody.toCharArray();

            // if rule body is epsilon, put an empty list as its body
            if(body.length == 2 && body[0] == '\'' && body[1] == '\'') {
                rules.put(productionHead, "");
                continue;
            }
            for(int i = 0; i < body.length; i++) {
                if(body[i] == '\'') { // terminal symbols are enclosed in '' e.g '+'
                    String terminalSymbol = "" + body[i+1];
                    terminals.add(terminalSymbol);
                    i += 2; // move to the next symbol in the body
                }
            }
            rules.put(productionHead, productionBody);
        }

        terminals.add(EOF);
    }

    /**
     * TODO: Enhancement
     *       We're assumming terminal symbols are one character long.
     *       Change so that it recognizes terminals that are n characters long.
     * @param nonTerminal a non terminal symbol
     * @return the first sets of the non terminal symbol
     * @throws BNFGrammarException if the non terminal symbol is not in the grammar
     */
    public Set<String> firstOf(String nonTerminal) throws BNFGrammarException {
        if(!rules.containsKey(nonTerminal)) {
            throw new BNFGrammarException("Non-terminal symbol, " + nonTerminal
                                            + ", is not in the grammar");
        }
        Set<String> firstSets = new HashSet<>();
        for(String rule : rules.get(nonTerminal)) {

            if(rule.startsWith("\'")) { // a terminal
                String terminalSymbol = rule.substring(1, 2);
                firstSets.add(terminalSymbol);
            } else if(rule.startsWith("<")) { // a nonterminal
                String nonTerminalSymbol = rule.substring(1, rule.indexOf(">", 0));
                firstSetOf(nonTerminalSymbol, firstSets);
            } else { // epsilon production
                firstSets.add(EPSILON);
            }
        }
        return firstSets;
    }

    public Set<String> firstOf(final String nonTerminal, final String productionBody) throws BNFGrammarException {
        if(!rules.containsKey(nonTerminal)) {
            throw new BNFGrammarException("Non-terminal symbol, " + nonTerminal
                    + ", is not in the grammar");
        }
        Set<String> firsts = new HashSet<>();
        if(productionBody.startsWith("\'")) { // starts with a terminal
            String terminalSymbol = productionBody.substring(1, 2);
            firsts.add(terminalSymbol);
        } else if(productionBody.startsWith("<")) { // starts with a non-terminal
            String nonTerminalSymbol = productionBody.substring(1, productionBody.indexOf(">", 0));
            firsts = firstOf(nonTerminalSymbol);
        } else if(productionBody.isEmpty()) { // epsilon transition
            firsts = followSetOf(nonTerminal);
        }
        return firsts;
    }

    private void firstSetOf(String nonTerminal, Set<String> firsts) {
        for(String rule : rules.get(nonTerminal)) {
            if(rule.startsWith("\'")) { // a terminal
                String terminalSymbol = rule.substring(1, 2);
                firsts.add(terminalSymbol);
            } else if(rule.startsWith("<")) { // a nonterminal
                String nonTerminalSymbol = rule.substring(1, rule.indexOf(">", 0));
                firstSetOf(nonTerminalSymbol, firsts);
            }
        }
    }

    public Set<String> followSetOf(String nonTerminal) throws BNFGrammarException {
        Set<String> follows = new HashSet<>();
        followOf(nonTerminal, follows);
        return follows;
    }

    private void followOf(String nonTerminal, Set<String> follows) throws BNFGrammarException {
        if(isStartSymbol(nonTerminal)) {
            follows.add(EOF);
        }
        Multimap<String, String> rulesWithNonTerminalInBody = findRulesWithNonTerminalInBody(nonTerminal);
        for(Map.Entry<String, String> production : rulesWithNonTerminalInBody.entries()) {
            String head = production.getKey();
            String body = production.getValue();

            // for rules S -> A where S is the start symbol
            String bnfFormat = "<" + nonTerminal + ">";
//            System.out.println("BNF: " + bnfFormat);

            if(body.matches("^.*" + bnfFormat + "$") && isStartSymbol(head)) {
                follows.add(EOF);
            }
//            } else if(body.matches("^.*" + bnfFormat + "'.'.*$")) {
                // for rules such as A -> <B>'+' where there a terminal immediately after the nonterminal
//                int stringEndIndex = body.lastIndexOf(bnfFormat);
//                follows.add(body.substring(stringEndIndex + bnfFormat.length() + 1, stringEndIndex + bnfFormat.length() + 2));
            /*}*/ else if(body.matches("^.+" + bnfFormat + ".*$")) {
                // for recursive rules such as A -> BA' or A -> +A') or A -> +
                // if there is a nonterminal directly after the symbol we're checking,
                // then get the first of the nonterminal
                int index = body.lastIndexOf(bnfFormat) + bnfFormat.length();

                if(index < body.length()) { // possibly some
                    if(body.charAt(index) == '\'') { // a terminal
                        int stringEndIndex = body.lastIndexOf(bnfFormat);
                        follows.add(body.substring(index + 1, index + 2));
                    }
                    if(body.charAt(index) == '<') { // a non-terminal
                        String nextNonterminal = body.substring(index + 1, body.indexOf('>', index));
                        firstSetOf(nextNonterminal, follows);

                        if(hasEpsilonTransition(nextNonterminal)) {
                            followOf(head, follows);
                        }
                    }
                } else {
                    followOf(head, follows);
                }
            } else {
//                System.out.println("Ignoring this rule... " + body);
            }
        }
    }

    Multimap<String, String> findRulesWithNonTerminalInBody(String nonTerminal) {
        Multimap<String, String> list = ArrayListMultimap.create();

        for(Map.Entry<String, String> entry : rules.entries()) {
            String body = entry.getValue();
            String head = entry.getKey();

            // we don't want to include productions that are the head symbol and
            // also in the production body to prevent infinite loops when
            // computing the follow sets
            if(body.contains("<" + nonTerminal + ">") && !head.equals(nonTerminal)) {
                list.put(head, body);
            }
        }
        return list;
    }

    public Set<String> nonTerminals() {
        return rules.keySet();
    }

    public Set<String> terminals() {
        return terminals;
    }

    public List<String> productionHeadsWith(String nonTerminal) throws BNFGrammarException {
        if(!rules.containsKey(nonTerminal)) {
            throw new BNFGrammarException("Non-terminal symbol, " + nonTerminal
                                                + ", is not in the grammar");
        }
        return (List<String>)rules.get(nonTerminal);
    }

    public boolean containsTerminalSymbol(String symbol) {
        return terminals.contains(symbol);
    }

    public boolean containsNonTerminal(String symbol) {
        return rules.containsKey(symbol);
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public boolean isStartSymbol(String nonTerminal) throws BNFGrammarException {
        if(!rules.containsKey(nonTerminal)) {
            throw new BNFGrammarException("Non-terminal symbol, " + nonTerminal
                    + ", is not in the grammar");
        }
        return startSymbol.equals(nonTerminal);
    }

    public boolean isEpsilonProduction(String ruleBody) {
        return ruleBody.isEmpty();
    }

    public boolean hasEpsilonTransition(String nonTerminal) {
        // productions that go to epsilon have an empty body
        // They are represented using the empty string ""
        List<String> productions = (List<String>)rules.get(nonTerminal);
        return productions.contains("");
    }

    public int numberOfProductions() {
        return rules.size();
    }


    public Multimap<String, String> getRules() {
        return rules;
    }

    public static List<String> fromBNF(String rule) throws BNFGrammarException {
        List<String> symbols = new ArrayList<>();
        for(int i = 0; i < rule.length(); i++) {
            if(rule.charAt(i) == '<') {
                // find closing brackets
                boolean found = false;
                int j = i + 1;
                while(j < rule.length()) {
                    if(rule.charAt(j) == '>') {
                        found = true;
                        break;
                    }
                    ++j;
                }
                if(!found) {
                    throw new BNFGrammarException("Missing closing brackets > in rule " + rule);
                }
                String nonTerminal = rule.substring(i+1, j);
                symbols.add(nonTerminal);
                i = j;
            } else if(rule.charAt(i) == '\'') { // terminals are one character long
                // check for closing apostrophe
                if(rule.charAt(i+2) != '\'') {
                    throw new BNFGrammarException("Missing closing apostrophe ' in rule " + rule);
                }
                String terminal = rule.substring(i+1, i+2);
                symbols.add(terminal);
                i += 2;
            }
        }
        return symbols;
    }
}