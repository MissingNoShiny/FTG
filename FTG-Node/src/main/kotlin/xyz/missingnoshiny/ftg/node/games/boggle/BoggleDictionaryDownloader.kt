package xyz.missingnoshiny.ftg.node.games.boggle

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import xyz.missingnoshiny.ftg.node.games.util.normalize
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/*
Answer structure :
{
    batchcomplete: String
    continue: {                 //Doesn't exist for last page
        cmcontinue: String
        continue: String
    }
    query: [
        {
            pageId: Int
            ns: Int
            title: String
        }
    ]
}
 */

@Serializable private data class Continue(val cmcontinue: String, val `continue`: String)
@Serializable private data class Page(val pageid: Int, val ns: Int, val title: String)
@Serializable private data class Query(val categorymembers: List<Page>)
@Serializable private data class Response(val batchcomplete: String, val `continue`: Continue? = null, val query: Query)

const val apiUrl = "https://fr.wiktionary.org/w/api.php"

val categories = listOf(
    "Adjectifs_en_français",
    "Formes_d’adjectifs_en_français",
    "Adverbes_en_français",
    "Noms_communs_en_français",
    "Formes_de_noms_communs_en_français",
    "Verbes_en_français",
    "Formes_de_verbes_en_français",
    "Prépositions_en_français"
)

val parameters = mutableMapOf(
    "action"        to "query",
    "list"          to "categorymembers",
    "cmlimit"       to "500",
    "cmnamespace"   to "0",
    "format"        to "json",
    "cmcontinue"    to ""
)

private fun addParameters(url: String, parameters: Map<String, String>) =
    "$url?" + parameters.toList().joinToString("&") { (key, value) -> "$key=" + URLEncoder.encode(value, "utf-8") }

fun downloadBoggleDictionary(): Dictionary {
    val dictionary = Dictionary {
        it.length > 2 && it.normalize().all { letter -> letter in 'a'..'z' }
    }

    categories.forEach {
        println(it)
        parameters["cmtitle"] = "Category:$it"
        do {
            with(URL(addParameters(apiUrl, parameters)).openConnection() as HttpURLConnection) {
                val data = this.inputStream.bufferedReader().readText()
                val response = Json.decodeFromString<Response>(data)

                response.query.categorymembers.forEach {
                    dictionary.insert(it.title)
                }

                parameters["cmcontinue"] = response.`continue`?.cmcontinue ?: ""
            }
            println(parameters["cmcontinue"])
        } while (parameters["cmcontinue"] != "")
    }

    return dictionary
}