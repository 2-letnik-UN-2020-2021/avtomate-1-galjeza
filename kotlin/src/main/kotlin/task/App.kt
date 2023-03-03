package task

import java.io.File
import java.io.InputStream
import java.util.LinkedList

fun echo(prefix: String, stream: InputStream) {
    val buffer = LinkedList<Byte>()

    while (true) {
        val symbol = stream.read()
        if (symbol == -1) break
        buffer.add(symbol.toByte())

    }
    println(prefix + String(buffer.toByteArray()))
}

fun main(args: Array<String>) {
    echo("ANSWER", File(args[0]).inputStream())
}
