package org.jboss.fuse.qa.fafram8.ssh;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.exceptions.CopyFileException;
import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Specific SSHClient for connecting to Node.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
@ToString
public class NodeSSHClient extends SSHClient {
	@Override
	public String executeCommand(String command, boolean suppressLog) throws KarafSessionDownException,
			SSHClientException {
		String returnString;

		if (!suppressLog) {
			log.debug("Command: " + command);
		}
		try {
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);

			final InputStream in = channel.getInputStream();

			channel.connect();

			returnString = convertStreamToString(in);

			returnString = returnString.replaceAll("\u001B\\[[;\\d]*m", "").trim();

			return returnString;
		} catch (JSchException ex) {
			log.error("Cannot execute ssh command: \"" + command + "\"", ex);
			throw new SSHClientException(ex);
		} catch (IOException ex) {
			log.error(ex.getLocalizedMessage());
			throw new SSHClientException(ex);
		}
	}

	/**
	 * Copies a file to the remote machine.
	 *
	 * @param localPath local path to file
	 * @param remotePath remote path to file
	 * @throws CopyFileException if there was problem with copying the file
	 */
	public void copyFileToRemote(String localPath, String remotePath) throws CopyFileException {
		log.info("Copying file " + localPath + " to remote machine path " + remotePath);

		try {
			final ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			final File file = new File(localPath);
			sftpChannel.cd(StringUtils.substringBeforeLast(remotePath, "/"));
			try (FileInputStream fis = new FileInputStream(file)) {
				sftpChannel.put(fis, StringUtils.substringAfterLast(remotePath, "/"));
			}
			sftpChannel.disconnect();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Exception thrown during uploading file to remote machine");
			throw new CopyFileException(ex);
		}
	}

	/**
	 * TODO(rjakubco): experimental method not used at the moment
	 * Convert remote file to String.
	 *
	 * @param remotePath absolute path to remote file
	 * @return content of rmeote file as String
	 * @throws IOException if there is problem in sftp
	 * @throws CopyFileException if copy fails
	 */
	public String readFileFromRemote(String remotePath) throws IOException, CopyFileException {
		log.info("Reading file from remote machine path " + remotePath);

		ChannelSftp sftpChannel;
		InputStream stream = null;
		String file = null;
		try {
			sftpChannel = (ChannelSftp) session.openChannel("sftp");
			stream = sftpChannel.get(remotePath);
			sftpChannel.connect();
			file = IOUtils.toString(new InputStreamReader(stream, "UTF-8"));

			sftpChannel.disconnect();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Exception thrown during uploading file to remote machine");
			throw new CopyFileException(ex);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return file;
	}
}
