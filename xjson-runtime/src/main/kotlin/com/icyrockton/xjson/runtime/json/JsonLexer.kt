package com.icyrockton.xjson.runtime.json

internal const val COMMA = ','
internal const val COLON = ':'
internal const val BEGIN_OBJ = '{'
internal const val END_OBJ = '}'
internal const val BEGIN_LIST = '['
internal const val END_LIST = ']'
internal const val STRING = '"'
internal const val STRING_ESC = '\\'
internal const val NULL = "null"


class JsonLexer(val str: CharSequence) {

    private var current = 0 // current read position

    fun expectNextToken(expected: Char): Int {
        skipEmptyContent()
        if (str[current] != expected) {
            error("expected next token ${expected}, but found ${str[current]}")
        }
        return current++
    }

    fun tryConsumeComma(): Boolean {
        skipEmptyContent()
        if (str[current] == COMMA) {
            current += 1
            return true
        }
        return false
    }

    fun canConsumeValue(): Boolean {
        var cur = current
        while (cur < str.length) {
            val ch = str[cur]
            // Inlined skipWhitespaces without field spill and nested loop. Also faster then char2TokenClass
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                ++cur
                continue
            }
            current = cur
            return when (ch) {
                '}', ']', ':', ',' -> false
                else -> true
            }
        }
        current = cur
        return false
    }

    fun consumeNextToken(): Char {
        skipEmptyContent()
        if (current >= str.length)
            error("expected consume next char, but read EOF")
        return str[current++]
    }

    fun skipEmptyContent() {
        var cur = current
        while (cur < str.length) {
            val ch = str[cur]
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                cur++
                continue
            }
            break
        }
        current = cur
    }

    fun test() {
        str[0]
    }

    fun consumeStringKey(): String {
        val startPos = expectNextToken(STRING) + 1
        val endPos = str.indexOf(STRING, startPos)
        if (endPos == -1) {
            error("expected string, but found ${str.substring(startPos)}")
        }
        val subStr = str.substring(startPos, endPos)
        current = endPos + 1
        return subStr
    }

    fun consumeString(): String {
        skipEmptyContent()
        var cur = current
        if (str[cur] == STRING)
            return consumeStringKey()
        while (true) {
            val ch = str[cur]
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == ',' || ch == '}' || ch == '{') {
                break
            }
            cur += 1
            continue
        }
        val str = str.substring(current, cur)
        current = cur
        return str
    }

    fun consumeDouble(): Double {
        val str = consumeString()
        try {
            return str.toDouble()
        } catch (e: NumberFormatException) {
            error("expected Double, but found $str")
        }
    }

    fun consumeFloat(): Float {
        val str = consumeString()
        try {
            return str.toFloat()
        } catch (e: NumberFormatException) {
            error("expected Float, but found $str")
        }
    }


    fun consumeBoolean(): Boolean {
        val start = consumeNextToken().lowercaseChar()
        val leftStr = when (start) {
            't' -> "true"
            'f' -> "false"
            else -> error("expected boolean literal, but found $start")
        }

        var accumulator = start.toString()
        for (expectedCh in leftStr.substring(1)) {
            val ch = consumeNextToken()
            accumulator += ch
            if (ch.lowercaseChar() != expectedCh.lowercaseChar())
                error("expected boolean $leftStr literal, but found $accumulator")
        }

        return leftStr.toBoolean()
    }

    fun consumeNumeric(): Long {
        skipEmptyContent()
        var isNegative = false
        var cur = current
        var accumulator = 0L
        while (true) {
            val ch = str[cur]
            if (ch == '-') {
                isNegative = true
                cur += 1
                continue
            } else if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == ',' || ch == '}' || ch == '{') {
                break
            }

            cur += 1
            val num = ch - '0'
            if (num !in 0..9)
                error("expected number, but found char $ch")
            accumulator = accumulator * 10 + num
        }
        current = cur
        if (isNegative)
            accumulator = -accumulator
        return accumulator
    }
}