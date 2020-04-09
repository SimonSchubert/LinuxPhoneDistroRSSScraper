package linuxphonebuilds

import java.io.File
import java.text.SimpleDateFormat

class RSSFileTool {
    fun createFile(id: String, feeds: MutableList<Pair<String, Long>>) {
        val folder = id.replace(" ", "_").toLowerCase()
        File("feeds/$folder").mkdirs()
        val fileName = "feeds/$folder/rss.xml"
        val file = File(fileName)
        file.createNewFile()

        val feedItems = feeds.sortedByDescending { it.second }.joinToString("") {
            val publishDate = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(it.second)
            val titleDate = SimpleDateFormat("dd.MMM.yyyy HH:mm").format(it.second)
            val url = it.first
            "<item>" +
                    "<title>Pinephone - $id - $titleDate</title>" +
                    "<link>$url</link>" +
                    "<pubDate>$publishDate</pubDate>" +
                    "</item>"
        }

        val content = "<rss version=\"2.0\">" +
                "<channel>" +
                "<title>$id Pinephone builds</title>" +
                "<link>https://linuxcommandlibrary.com/</link>" +
                "<description>Latest builds.</description>" +
                feedItems +
                "</channel>" +
                "</rss>"

        file.writeText(content)
    }
}