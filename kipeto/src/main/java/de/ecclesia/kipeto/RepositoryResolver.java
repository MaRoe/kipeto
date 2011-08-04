/*
 * #%L
 * Kipeto
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Assert;

public class RepositoryResolver {

	private static final Logger log = LoggerFactory
			.getLogger(RepositoryResolver.class);

	/** Name des Distribution-Verzeichnisses im Repository */
	private static final String DIST_DIR = "dist";

	private static final String RESOLVE_CONFIG_FILE = "repos_resolve.properties";

	private final String defaultRepositoryUrl;
	private File keyFile;

	public RepositoryResolver(String defaultRepositoryUrl) {
		this.defaultRepositoryUrl = defaultRepositoryUrl;
	}

	/**
	 * Versucht, im übergebenen Repository die Konfigurations-Datei zu finden
	 * und daraus ein passendes Repository abzuleiten. Schlägt dies fehlt oder
	 * tritt ein Fehler auf, wird dieser Fehler gelogt, und das übergebene
	 * Repository zurückgegeben.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String resolveReposUrl() throws IOException {
		try {
			URL url;
			FileName filename;
			FileSystemManager fsm = VFS.getManager();
			filename = fsm.resolveURI(defaultRepositoryUrl);

			if (!filename.getScheme().equalsIgnoreCase("http")
					&& !filename.getScheme().equalsIgnoreCase("sftp")) {
				log.info(
						"Resolving repository-config not implemented for protocol {} yet",
						filename.getScheme());
				return defaultRepositoryUrl;
			}

			Properties config = loadVfsConfig();

			if (config == null) {
				return defaultRepositoryUrl;
			}

			String localIp = determinateLocalIP();

			return resolveRepos(localIp, config);
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return defaultRepositoryUrl;
		}
	}

	/**
	 * Ermittelt anhand der Default-Repository-URL die IP-Adresse der
	 * Netzwerkkarte, die Verbindung zum Repository hat.
	 */
	private String determinateLocalIP() throws IOException {
		Socket socket = null;

		try {

			int port;
			String hostname;

			if (isSftp()) {
				port = 22;
				hostname = getHostname();
			} else {
				URL url = new URL(defaultRepositoryUrl);
				port = url.getPort() > -1 ? url.getPort() : url
						.getDefaultPort();
				hostname = url.getHost();
			}

			log.debug("Determinating local IP-Adress by connect to {}:{}",
					defaultRepositoryUrl, port);
			InetAddress address = Inet4Address.getByName(hostname);

			socket = new Socket();
			socket.connect(new InetSocketAddress(address, port), 3000);
			InputStream stream = socket.getInputStream();
			InetAddress localAddress = socket.getLocalAddress();
			stream.close();

			String localIp = localAddress.getHostAddress();

			log.info("Local IP-Adress is {}", localIp);

			return localIp;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	/**
	 * Läd die Config vom Default-Repository herunter.
	 */
	private Properties loadVfsConfig() throws IOException {
		String configUrl = String.format("%s/%s/%s", defaultRepositoryUrl,
				DIST_DIR, RESOLVE_CONFIG_FILE);
		log.info("Looking for repository-config at {}", configUrl);
		Properties properties = new Properties();
		FileSystemOptions fso = new FileSystemOptions();
		if (isSftp()) {
			Assert.isTrue(this.keyFile.isFile(), "Keyfile is not a file");
			File[] files = { this.keyFile };
			SftpFileSystemConfigBuilder.getInstance().setIdentities(fso, files);
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
					fso, "yes");
		}

		FileObject fo;
		try {
			fo = VFS.getManager().resolveFile(configUrl, fso);

			BufferedInputStream inputStream = new BufferedInputStream(fo
					.getContent().getInputStream());

			properties.load(inputStream);
		} catch (FileSystemException e) {
			log.info("No repository-config found at {}", configUrl);
			throw new RuntimeException(e);
		}

		return properties;
	}

	/**
	 * Ermittelt anhand der lokalen IP-Adresse und der übergebenen
	 * Konfiguration, welches Repository für den Update-Vorgang verwendet werden
	 * soll.
	 */
	private String resolveRepos(String localIp, Properties config) {
		for (Object key : config.keySet()) {
			String ipPraefix = (String) key;
			String repos = config.getProperty(ipPraefix);

			if (localIp.startsWith(ipPraefix)) {
				log.info("Local IP " + localIp
						+ " starts with '{}', selecting [{}]", ipPraefix, repos);
				return repos;
			} else {
				log.debug("Local IP " + localIp
						+ " does not start with '{}' --> {}", ipPraefix, repos);
			}
		}

		log.warn(
				"No matching config-entry found for {}, falling back to default-repository {}",
				localIp, defaultRepositoryUrl);

		return defaultRepositoryUrl;
	}

	public void setKeyFile(File keyFile) {
		this.keyFile = keyFile;
	}

	private boolean isSftp() throws FileSystemException {
		if (VFS.getManager().resolveURI(defaultRepositoryUrl).getScheme()
				.equals("sftp")) {
			return true;
		}
		return false;
	}

	private String getHostname() throws FileSystemException {
		String hostname = VFS.getManager().resolveURI(defaultRepositoryUrl)
				.getRootURI().replaceAll("sftp://", "");
		if (hostname.contains("@")) {
			int idx = hostname.indexOf("@");
			hostname = hostname.substring(idx + 1, hostname.length());
		}

		if (hostname.endsWith("/")) {
			hostname = hostname.substring(0, hostname.length() - 1);
		}
		return hostname;
	}
}
