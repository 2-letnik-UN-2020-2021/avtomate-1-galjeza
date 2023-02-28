package task

import java.io.File
import java.io.InputStream
import java.util.LinkedList

fun echo(stream: InputStream) {
    val buffer = LinkedList<Byte>()

    while (true) {
        val symbol = stream.read()
        if (symbol == -1) break
        buffer.add(symbol.toByte())

    }
    println(String(buffer.toByteArray()))
}

fun main(args: Array<String>) {
    echo(File(args[0]).inputStream())
}