package linuxphonebuilds

import com.jcraft.jsch.*
import java.io.File

class SftpTool {
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

    private fun ChannelSftp.cdOrMkdir(folder: String) {
        try {
            cd(folder)
        } catch (e: SftpException) {
            mkdir(folder)
            cd(folder)
        }
    }

}