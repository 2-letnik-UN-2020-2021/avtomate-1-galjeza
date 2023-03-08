import java.io.InputStream

const val ERROR_STATE = 0
const val SKIP_SYMBOL = 0
const val INT_SYMBOL = 1
const val HEX_SYMBOL = 2
const val VARIABLE_SYMBOL = 3
const val PLUS_SYMBOL = 4
const val MINUS_SYMBOL = 5
const val TIMES_SYMBOL = 6
const val DIVIDE_SYMBOL = 7
const val BWAND_SYMBOL = 8
const val BWOR_SYMBOL = 9
const val LPAREN_SYMBOL = 10
const val RPAREN_SYMBOL = 11
const val EOF_SYMBOL =  -1
const val EOF = -1



const val NEWLINE = '\n'.code

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object ForForeachFFFAutomaton: DFA {
    override val states = (1 .. 15).toSet()
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(
        2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    )

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) {IntArray(numberOfCodes)}
    private val values = Array(numberOfStates) {SKIP_SYMBOL}

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setTransition(from: Int, charRange: CharRange, to: Int) {
        for (chr in charRange) {
           transitions[from][chr.code + 1] = to
        }
    }

    private fun setSymbol(state: Int, symbol: Int) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }
    init {
        // int
        setTransition(1, '0'..'9', 2)
        setTransition(2, '0'..'9', 2)

        // hex
        setTransition(1, '#', 3)
        setTransition(3, '0'..'9', 4)
        setTransition(3, 'a'..'f', 4)
        setTransition(3, 'A'..'F', 4)
        setTransition(4, '0'..'9', 4)
        setTransition(4, 'a'..'f', 4)
        setTransition(4, 'A'..'F', 4)


        // variable
        setTransition(1, 'a'..'z', 5)
        setTransition(1, 'A'..'Z', 5)
        setTransition(5, 'a'..'z', 5)
        setTransition(5, 'A'..'Z', 5)

        // variable with numbers at the end
        setTransition(5, '0'..'9', 6)
        setTransition(6, '0'..'9', 6)

        // plus
        setTransition(1, '+', 7)

        // minus
        setTransition(1, '-', 8)

        // times
        setTransition(1, '*', 9)

        // divide
        setTransition(1, '/', 10)

        // bitwise and
        setTransition(1, '&', 11)

        // bitwise or
        setTransition(1, '|', 12)

        // left parenthesis
        setTransition(1, '(', 13)

        // right parenthesis
        setTransition(1, ')', 14)

        // EOF
        setTransition(1, EOF, 15)


        setSymbol(15, EOF_SYMBOL)
        setSymbol(2, INT_SYMBOL)
        setSymbol(4, HEX_SYMBOL)
        setSymbol(5, VARIABLE_SYMBOL)
        setSymbol(6, VARIABLE_SYMBOL)
        setSymbol(7, PLUS_SYMBOL)
        setSymbol(8, MINUS_SYMBOL)
        setSymbol(9, TIMES_SYMBOL)
        setSymbol(10, DIVIDE_SYMBOL)
        setSymbol(11, BWAND_SYMBOL)
        setSymbol(12, BWOR_SYMBOL)
        setSymbol(13, LPAREN_SYMBOL)
        setSymbol(14, RPAREN_SYMBOL)










    }


}

data class Token(val symbol: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break // Longest match

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code // The code following the current lexeme is the first code of the next lexeme

        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)
            return if (symbol == SKIP_SYMBOL) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }
}

fun name(symbol: Int) =
    when (symbol) {
        EOF_SYMBOL -> "eof"
        INT_SYMBOL -> "int"
        HEX_SYMBOL -> "hex"
        VARIABLE_SYMBOL -> "variable"
        PLUS_SYMBOL -> "plus"
        MINUS_SYMBOL -> "minus"
        TIMES_SYMBOL -> "times"
        DIVIDE_SYMBOL -> "devide"
        BWAND_SYMBOL -> "bwand"
        BWOR_SYMBOL -> "bwor"
        LPAREN_SYMBOL -> "lparen"
        RPAREN_SYMBOL -> "rparen"
        else -> throw Error("Invalid symbol")
    }

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token.symbol != EOF_SYMBOL) {
        print("${name(token.symbol)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}

fun main(args: Array<String>) {
    printTokens(Scanner(ForForeachFFFAutomaton, "417549".byteInputStream()))
}
