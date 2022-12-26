import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.random.Random

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
    } else {
        val (words, wordsFileName) = args.getInfo(0)
        if (words.isNotEmpty()) {
            val (candidates, candidatesFileName) = args.getInfo(1)
            if (candidates.isNotEmpty()) {
                val wordsErrors = words.checkAllWords(wordsFileName)
                if (wordsErrors == 0) {
                    val candidatesErrors = candidates.checkAllWords(candidatesFileName)
                    if (candidatesErrors == 0) {
                        val wordsSet = words.map { it.lowercase() }.toSet()
                        val candidatesSet = candidates.map { it.lowercase() }.toSet()
                        if (wordsSet.containsAll(candidatesSet)) {
                            println("Words Virtuoso")
                            var turns = 1
                            var startTime = 0L
                            val allTries = mutableListOf<String>()
                            var wrongLetters = ""
                            val secretWord = candidates[Random.nextInt(candidates.size)].lowercase()
                            while (true) {
                                println("Input a 5-letter word:")
                                if (turns == 1) {
                                    startTime = System.currentTimeMillis()
                                }
                                val guessWord = readln().lowercase()
                                when {
                                    guessWord == "exit" -> {
                                        println("The game is over.")
                                        break
                                    }

                                    guessWord.hasNotCorrectLength() ->
                                        println("The input isn't a 5-letter word.")

                                    guessWord.hasNotCorrectCharacters() ->
                                        println("One or more letters of the input aren't valid.")

                                    guessWord.hasDuplicates() ->
                                        println("The input has duplicate letters.")

                                    !words.contains(guessWord) ->
                                        println("The input word isn't included in my words list.")

                                    else -> {
                                        println()
                                        if (guessWord == secretWord && turns == 1) {
                                            println(
                                                guessWord
                                                    .map { "\u001B[48:5:10m${it.uppercase()}\u001B[0m" }
                                                    .joinToString("")
                                            )
                                            println("Correct!")
                                            println("Amazing luck! The solution was found at once.")
                                            break
                                        }
                                        val (basicColoredString, coloredClueString) =
                                            guessWord.getClueString(secretWord)
                                        allTries.add(coloredClueString)
                                        allTries.forEach { println(it) }
                                        if (guessWord != secretWord) {
                                            wrongLetters = wrongLetters.addWrongLetters(basicColoredString, guessWord)
                                            println("\u001B[48:5:14m$wrongLetters\u001B[0m\n")
                                        } else {
                                            val endTime = System.currentTimeMillis()
                                            println("Correct!")
                                            println(
                                                "The solution was found after $turns tries in " +
                                                        "${(endTime - startTime) / 1000} seconds."
                                            )
                                            break
                                        }
                                    }
                                }
                                turns++
                            }
                        } else {
                            val subSet = wordsSet.filter { candidatesSet.contains(it) }.toSet()
                            println(
                                "Error: ${(candidatesSet - subSet).size} candidate words are not included in the " +
                                        "$wordsFileName file."
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Array<String>.getInfo(arrayIndex: Int): Pair<List<String>, String> {
    val fileName = this[arrayIndex]
    val words = try {
        Files.readAllLines(Path(fileName))
    } catch (_: IOException) {
        println(
            "Error: The${
                if (arrayIndex == 1) " candidate"
                else ""
            } words file $fileName doesn't exist."
        )
        listOf()
    }
    return Pair(words, fileName)
}

fun List<String>.checkAllWords(fileName: String): Int {
    var errors = 0
    forEach { word ->
        if (word.hasNotCorrectLength() || word.hasNotCorrectCharacters() || word.hasDuplicates()) {
            errors++
        }
    }
    if (errors != 0) {
        println("Error: $errors invalid words were found in the $fileName file.")
    }
    return errors
}

fun String.hasNotCorrectLength() = length != 5

fun String.hasNotCorrectCharacters(): Boolean {
    for (ch in this) {
        if (ch.lowercase() !in "a".."z") {
            return true
        }
    }
    return false
}

fun String.hasDuplicates() = map { it.lowercase() }.toSet().size != 5

fun String.getClueString(secretWord: String): Pair<String, String> {
    var basicResult = ""
    var coloredResult = ""
    for (i in secretWord.indices) {
        when {
            this[i] == secretWord[i] -> {
                coloredResult += "\u001B[48:5:10m${secretWord[i].uppercase()}\u001B[0m"
                basicResult += secretWord[i]
            }

            secretWord.contains(this[i]) -> {
                coloredResult += "\u001B[48:5:11m${this[i].uppercase()}\u001B[0m"
                basicResult += this[i]
            }

            else -> {
                coloredResult += "\u001B[48:5:7m${this[i].uppercase()}\u001B[0m"
                basicResult += "_"
            }
        }
    }
    return Pair(basicResult, coloredResult)
}

fun String.addWrongLetters(clueString: String, guessWord: String): String {
    var result = this
    for (ch in guessWord) {
        if (!clueString.lowercase().contains(ch)) {
            result += ch
        }
    }
    return result.uppercase().toSet().sorted().joinToString("")
}