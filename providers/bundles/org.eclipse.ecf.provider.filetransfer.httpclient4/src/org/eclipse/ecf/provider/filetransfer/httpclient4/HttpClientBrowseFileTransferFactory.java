/****************************************************************************
 * Copyright (c) 2007 IBM, Composent Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Thomas Joiner - HttpClient 4 implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient4;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemRequest;
import org.eclipse.ecf.filetransfer.RemoteFileSystemException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowser;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;
import org.eclipse.osgi.util.NLS;

public class HttpClientBrowseFileTransferFactory implements IRemoteFileSystemBrowserFactory {

	public IRemoteFileSystemBrowser newInstance() {
		return new IRemoteFileSystemBrowser() {

			private Proxy proxy;
			private IConnectContext connectContext;

			public Namespace getBrowseNamespace() {
				return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
			}

			public IRemoteFileSystemRequest sendBrowseRequest(IFileID directoryOrFileId, IRemoteFileSystemListener listener) throws RemoteFileSystemException {
				Assert.isNotNull(directoryOrFileId);
				Assert.isNotNull(listener);
				URL url;
				try {
					url = directoryOrFileId.getURL();
				} catch (final MalformedURLException e) {
					throw new RemoteFileSystemException(NLS.bind("Exception creating URL for {0}", directoryOrFileId)); //$NON-NLS-1$
				}

				HttpClientFileSystemBrowser browser = new HttpClientFileSystemBrowser(new DefaultHttpClient() {
					@Override
					protected ClientConnectionManager createClientConnectionManager() {
						SSLSocketFactory factory = new SSLSocketFactory(SSLContexts.createSystemDefault(), SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

						final SchemeRegistry registry = new SchemeRegistry();
						registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
						registry.register(new Scheme("https", 443, factory));

						return new BasicClientConnectionManager(registry);
					}
				}, directoryOrFileId, listener, url, connectContext, proxy);
				return browser.sendBrowseRequest();
			}

			public void setConnectContextForAuthentication(IConnectContext connectContext) {
				this.connectContext = connectContext;
			}

			public void setProxy(Proxy proxy) {
				this.proxy = proxy;
			}

			public Object getAdapter(Class adapter) {
				return null;
			}

		};

	}
}
