/*
 * Copyright 2020 Simon Schubert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package linuxphonebuilds

import com.jcraft.jsch.*
import org.jsoup.Jsoup
import org.openqa.selenium.firefox.FirefoxDriver
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.concurrent.TimeUnit

val multipleWhitespaceRegex by lazy { "\\s\\s+".toRegex() }

enum class OS {
    UBPORTS,
    PUREOS,
    DEBIAN,
    POSTMARKET_OS,
    FEDORA,
    KDE_NEON
}

val feeds: MutableMap<OS, MutableList<Pair<String, Long>>> = mutableMapOf()

fun main(args: Array<String>) {
    if (args.contains("--help")) {
        println("Syntax for automatic sftp upload: [host] [username] [password]")
        println("Warning: geckodriver has to exist in the same directory")
        println("Info: XML feed files will be store in 'feeds' directory")
        return
    }
    val host = args.getOrNull(0)
    val username = args.getOrNull(1)
    val password = args.getOrNull(2)

    feeds[OS.UBPORTS] = getUbuntuTouchItems()
    feeds[OS.DEBIAN] = getDebianItems()
    feeds[OS.POSTMARKET_OS] = getPostmarketOSItems()
    feeds[OS.FEDORA] = getFedoraItems()
    feeds[OS.PUREOS] = getPureOsItems()
    feeds[OS.KDE_NEON] = getKdeNeonItems()

    feeds.forEach { (t, u) ->
        println("Fetched ${u.count()} ${t.getName()} items")
        if (u.isNotEmpty()) {
            createRSSFile(t.getName(), u)
        }
    }

    // createRSSFile("All", feeds.flatMap { it.value }.toMutableList())

    if (host != null && username != null && password != null) {
        println("Start uploading files")
        uploadToSFTP(host, username, password)
    }

    println("Job done")
}

fun uploadToSFTP(host: String, username: String, password: String) {
    val jsch = JSch()
    val session: Session?
    try {
        session = jsch.getSession(username, host, 22)
        session.setConfig("StrictHostKeyChecking", "no")
        session.setPassword(password)
        session.connect()
        val channel: Channel = session.openChannel("sftp")
        channel.connect()
        val sftpChannel = channel as ChannelSftp
        sftpChannel.cdOrMkdir("linuxcommandlibrary")
        sftpChannel.cdOrMkdir("linuxphone")
        File("feeds").listFiles()?.forEach { directory ->
            sftpChannel.cdOrMkdir(directory.name)
            directory.canonicalFile.listFiles()?.forEach {
                sftpChannel.put(it.absolutePath, it.name)
            }
            sftpChannel.cd("..")
        }
        sftpChannel.exit()
        session.disconnect()
    } catch (e: JSchException) {
        e.printStackTrace()
    } catch (e: SftpException) {
        e.printStackTrace()
    }
}

fun ChannelSftp.cdOrMkdir(folder: String) {
    try {
        this.cd(folder)
    } catch (e: SftpException) {
        this.mkdir(folder)
        this.cd(folder)
    }
}

fun createRSSFile(id: String, feeds: MutableList<Pair<String, Long>>) {
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

fun getFedoraItems(): MutableList<Pair<String, Long>> {
    val url = "https://github.com/nikhiljha/pp-fedora-sdsetup/releases/"
    val doc = Jsoup.connect(url).get()
    val rows = doc.select(".release-entry")
    return rows.mapNotNull { entry ->
        val itemUrl = entry.select(".Details-element").select("a").map { it.attr("abs:href") }.filter { it.contains(".img") }.firstOrNull()
        val dateTime = entry.select("relative-time").attr("datetime")
        val instant = Instant.parse(dateTime)
        if (itemUrl != null) {
            (itemUrl to instant.toEpochMilli())
        } else {
            null
        }
    }.toMutableList()
}

fun getKdeNeonItems(): MutableList<Pair<String, Long>> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val url = "https://images.plasma-mobile.org/pinephone/"
    val doc = Jsoup.connect(url).get()
    val rows = doc.select("tr")
    return rows.mapNotNull { entry ->
        val itemUrl = entry.select("a").map { it.attr("abs:href") }.filter { it.contains(".img") }.firstOrNull()
        if (itemUrl != null) {
            val date = entry.select("td").getOrNull(2)?.text()
            val timestamp = try {
                dateFormat.parse("$date")?.time ?: 0L
            } catch (e: ParseException) {
                0L
            }
            (itemUrl to timestamp)
        } else {
            null
        }
    }.toMutableList()
}

fun getPostmarketOSItems(): MutableList<Pair<String, Long>> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val url = "http://images.postmarketos.org/pinephone/"
    return getSimpleFileTreeTableItems(url, dateFormat, ".img")
}

fun getDebianItems(): MutableList<Pair<String, Long>> {
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm")
    val url = "http://pinephone.a-wai.com/images/"
    return getSimpleFileTreeTableItems(url, dateFormat, ".img")
}

fun getSimpleFileTreeTableItems(url: String, dateFormat: SimpleDateFormat, fileExtension: String): MutableList<Pair<String, Long>> {
    val doc = Jsoup.connect(url).get()
    val rows = doc.select("pre").firstOrNull()?.text()?.split("\n")?.filter { it.contains(fileExtension) }
    return rows?.mapNotNull {
        val parts = it.trim().replace(multipleWhitespaceRegex, " ").split(" ")
        val fileName = parts.getOrNull(0)
        val date = parts.getOrNull(1)
        val time = parts.getOrNull(2)
        if (fileName != null && date != null && time != null) {
            val timestamp = try {
                dateFormat.parse("$date $time")?.time ?: 0L
            } catch (e: ParseException) {
                0L
            }
            ("$url$fileName" to timestamp)
        } else {
            null
        }
    }?.toMutableList() ?: mutableListOf()
}

fun getUbuntuTouchItems(): MutableList<Pair<String, Long>> {
    val url = "https://ci.ubports.com/job/rootfs/job/rootfs-pinephone/"
    val doc = Jsoup.connect(url).get()
    val rows = doc.select(".build-row")
    return rows.mapNotNull {
        val itemUrl = it.select("a").getOrNull(1)?.attr("abs:href")
        val timestamp = it.select(".build-details").attr("time")?.toLongOrNull()
        if (itemUrl != null && timestamp != null) {
            (itemUrl to timestamp)
        } else {
            null
        }
    }.toMutableList()
}

fun getPureOsItems(): MutableList<Pair<String, Long>> {
    val url = "http://pureos.ironrobin.net/droppy/#/Images"
    System.setProperty("webdriver.gecko.driver", "geckodriver")

    val driver = try {
        FirefoxDriver()
    } catch (exception: Exception) {
        println("FirefoxDriver: " + exception.message)
        return mutableListOf()
    }

    val doc = try {
        driver.manage()?.timeouts()?.implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(url)

        // WebDriverWait didn't work. Workaround:
        Thread.sleep(30_000)

        Jsoup.parse(driver.pageSource)
    } catch (exception: Exception) {
        driver.close()
        println("Error fetching pureos: " + exception.message)
        return mutableListOf()
    }

    val rows = doc.select(".data-row")
    return rows.mapNotNull {
        val timestamp = it.select(".mtime").attr("data-timestamp").toLongOrNull()
        // abs:href didn't work. Workaround:
        val itemUrl = "http://pureos.ironrobin.net" + it.select("a").attr("href")
        if (timestamp != null) {
            (itemUrl to timestamp)
        } else {
            null
        }
    }.toMutableList() ?: mutableListOf()
}

fun OS.getName(): String {
    return when (this) {
        OS.UBPORTS -> "Ubuntu Touch"
        OS.PUREOS -> "PureOS"
        OS.DEBIAN -> "Debian"
        OS.POSTMARKET_OS -> "postmarketOS"
        OS.FEDORA -> "Fedora"
        OS.KDE_NEON -> "KDE Neon"
    }
}