import java.io.InputStream;
import java.io.IOException;
import java.lang.Math;

public class CalculatorParser {

    static final private InputStream INPUT = System.in;
    static final private int EOF = -1;

    private int lookahead;

    public CalculatorParser() throws IOException, ParseError {
        consume();
    }

    /* Auxiliary functions: */
    private boolean isNotDigit(int x) {
        return (x < '0' || x > '9');
    }

    private boolean isVoid(int x) {
        return (x == '\n' || x == EOF);
    }

    private int evalDigit(int d) {
        return d - '0';
    }

    private void consume() throws IOException {
        // Consume next character but ignore whitespaces.
        do {
            lookahead = INPUT.read();
        } while (lookahead == ' ');
    }

    /* Recursive Descend functions: */
    private int numRest(int number) throws IOException, ParseError {
        if (lookahead == '+' || lookahead == '-' || lookahead == '*' ||
                lookahead == ')' || isVoid(lookahead))
            return number;

        if (isNotDigit(lookahead))
            throw new ParseError();

        int value = (number * 10) + evalDigit(lookahead);
        consume();
        return numRest(value);
    }

    private int num() throws IOException, ParseError {
        if (isNotDigit(lookahead) && lookahead != '(')
            throw new ParseError();

        if (lookahead == '(') {
            consume();
            int value = exp();
            if (lookahead != ')')
                throw new ParseError();
            consume();
            return value;
        } else {
            int value = evalDigit(lookahead);
            consume();
            return numRest(value);
        }
    }

    private int termRest(int num_value) throws IOException, ParseError {
        if (lookahead == '+' || lookahead == '-' ||
                lookahead == ')' || isVoid(lookahead))
            return num_value;

        if (lookahead != '*')
            throw new ParseError();

        consume();

        if (lookahead != '*')
            throw new ParseError();

        consume();
        return termRest((int) Math.pow(num_value, num()));
    }

    private int term() throws IOException, ParseError {
        if (isNotDigit(lookahead) && lookahead != '(')
            throw new ParseError();

        return termRest(num());
    }

    private int expRest(int term_value) throws IOException, ParseError {
        if (lookahead == ')' || isVoid(lookahead))
            return term_value;

        if (lookahead != '+' && lookahead != '-')
            throw new ParseError();

        if (lookahead == '+') {
            consume();
            return expRest(term_value + term());
        } else {
            consume();
            return expRest(term_value - term());
        }
    }

    private int exp() throws IOException, ParseError {
        if (isNotDigit(lookahead) && lookahead != '(')
            throw new ParseError();

        return expRest(term());
    }

    /* Caller function: */
    public int parse() throws IOException, ParseError {
        int value = exp();

        if (!isVoid(lookahead))
            throw new ParseError();

        return value;
    }

    public void parseAll() throws IOException {
        while (lookahead != EOF) {
            try {
                System.out.println(parse());
            } catch (IOException | ParseError exc) {
                do {
                    lookahead = INPUT.read();
                } while (lookahead != '\n');
                System.err.println(exc.getMessage());
            }
            consume();
        }
    }

    public static void main(String[] args) {
        try {
            CalculatorParser parser = new CalculatorParser();
            System.out.println(parser.parse());
//            parser.parseAll();
        } catch (IOException | ParseError exc) {
            System.err.println(exc.getMessage());
        }
    }
}
