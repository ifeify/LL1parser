import java.util.*;

public class TableDrivenParser implements Parser {
    private ParseTable parseTable;
    private Grammar grammar;
    private Stack<String> stack = new Stack<>();

    public TableDrivenParser(Grammar grammar) throws BNFGrammarException {
        this.grammar = grammar;
        this.parseTable = new ParseTable(grammar);
        parseTable.generate();
    }

    @Override
    public boolean parse(final String inputString) throws SyntaxException {
        String input = inputString.trim().replaceAll("\\s+", "");
        input = input + Grammar.EOF;
        int index = 0;

        stack.push(Grammar.EOF);
        stack.push(grammar.getStartSymbol());

        String topOfStack = stack.peek();
        while(true) {
//            System.out.println("Stack contents");
//            stack.forEach(System.out::print);
//            System.out.println();

            if(topOfStack.equals(Grammar.EOF) &&
                                Grammar.EOF.equals(""+input.charAt(index))) {
                // input string is correct
                break;
            } else if(grammar.containsTerminalSymbol(topOfStack)) {
                if(topOfStack.equals(""+input.charAt(index))) {
                    stack.pop();
                    ++index;
                } else {
                    throw new SyntaxException("Syntax Error. Unrecognized symbol, " + input.charAt(index));
                }
            } else if(grammar.containsNonTerminal(topOfStack)) {
                Optional<String> result = parseTable.ruleToApply(topOfStack, ""+input.charAt(index));
                if(result.isPresent()) {
                    stack.pop();

                    String rule = result.get();
                    if(!grammar.isEpsilonProduction(rule)) {
                        List<String> symbols = Grammar.fromBNF(rule);
                        ListIterator<String> iterator = symbols.listIterator(symbols.size());

                        while(iterator.hasPrevious()) {
                            stack.push(iterator.previous());
                        }
                    }
                } else {
                    throw new SyntaxException("Unable to expand non-terminal, " + topOfStack
                                                + ", while processing, " + input.charAt(index) + ", in input");
                }
            }

            topOfStack = stack.peek();

        }

        stack.clear();
        return true;
    }
}
