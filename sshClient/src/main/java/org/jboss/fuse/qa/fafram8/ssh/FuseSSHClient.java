package org.jboss.fuse.qa.fafram8.ssh;

import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.io.InputStream;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Specific SSHClient for connecting to Fuse.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
@ToString
public class FuseSSHClient extends SSHClient {
	@Override
	public String executeCommand(String command, boolean suppressLog) throws KarafSessionDownException,
			SSHClientException {
		if (!suppressLog) {
			log.info("Executing command: " + command);
		}
		final int retriesCount = 2;
		final long commandRetryTimeout = 5000L;
		try {
			// If we should retry the command
			boolean retry;

			int retries = 0;

			String returnString = "";
			do {
				if (retries == retriesCount) {
					// If we retried it 2 times already, break
					break;
				}
				channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(command);

				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);

				final InputStream in = channel.getInputStream();

				channel.connect();

				returnString = convertStreamToString(in);
				if (returnString.contains("not found")) {
					if (!suppressLog) {
						log.debug("Retrying command in " + commandRetryTimeout + " miliseconds");
					}
					retry = true;
					retries++;
					// Wait for 5 sec before executing command
					try {
						Thread.sleep(commandRetryTimeout);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					retry = false;
				}
			} while (retry);

			returnString = returnString.replaceAll("\u001B\\[[;\\d]*m", "").trim();

			return returnString;
		} catch (JSchException ex) {
			if (ex.getMessage().contains("session is down")) {
				if (!suppressLog) {
					log.error("JschException caught - Session is down");
				}
				throw new KarafSessionDownException(ex);
			}

			log.error("Cannot execute Fuse ssh command: \"" + command + "\"", ex);
			throw new SSHClientException(ex);
		} catch (IOException ex) {
			log.error(ex.getLocalizedMessage());
			throw new SSHClientException(ex);
		}
	}
}
