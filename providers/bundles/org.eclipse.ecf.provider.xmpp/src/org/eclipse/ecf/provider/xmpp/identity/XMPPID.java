/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.xmpp.smack.ECFConnection;
import org.eclipse.ecf.presence.im.IChatID;

public class XMPPID extends BaseID implements IChatID {

	private static final long serialVersionUID = 3257289140701049140L;
	public static final char USER_HOST_DELIMITER = '@';
	public static final char PORT_DELIMITER = ':';
	public static final char PATH_DELIMITER = '/';

	URI uri;
	String username;
	String hostname;
	String resourcename = ECFConnection.CLIENT_TYPE;
	int port = -1;

	protected static String fixEscape(String src) {
		if (src == null)
			return null;
		return src.replaceAll("%", "%25");
	}

	public XMPPID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace);
		unamehost = fixEscape(unamehost);
		if (unamehost == null)
			throw new URISyntaxException(unamehost, "username/host string cannot be null");
		// Handle parsing of user@host/resource string
		int atIndex = unamehost.lastIndexOf(USER_HOST_DELIMITER);
		if (atIndex == -1)
			throw new URISyntaxException(unamehost, "username/host string not valid.  Must be of form <username>@<hostname>[:port]");
		username = unamehost.substring(0, atIndex);
		final String remainder = unamehost.substring(atIndex + 1);
		// Handle parsing of host:port
		atIndex = remainder.lastIndexOf(PORT_DELIMITER);
		if (atIndex != -1) {
			try {
				final int slashLoc = remainder.indexOf(PATH_DELIMITER);
				if (slashLoc != -1)
					port = Integer.parseInt(remainder.substring(atIndex + 1, slashLoc));
				else
					port = Integer.parseInt(remainder.substring(atIndex + 1));
			} catch (final NumberFormatException e) {
				throw new URISyntaxException(unamehost, "invalid port value");
			}
			hostname = remainder.substring(0, atIndex);
		}
		atIndex = remainder.indexOf(PATH_DELIMITER);
		if (atIndex != -1) {
			if (hostname == null)
				hostname = remainder.substring(0, atIndex);
			resourcename = PATH_DELIMITER + remainder.substring(atIndex + 1);
		} else {
			resourcename = PATH_DELIMITER + ECFConnection.CLIENT_TYPE;
		}
		if (hostname == null)
			hostname = remainder;
		uri = new URI(namespace.getScheme(), username, hostname, port, resourcename, null, null);
	}

	protected int namespaceCompareTo(BaseID o) {
		return getName().compareTo(o.getName());
	}

	protected boolean namespaceEquals(BaseID o) {
		if (!(o instanceof XMPPID)) {
			return false;
		}
		final XMPPID other = (XMPPID) o;
		return getUsernameAtHost().equals(other.getUsernameAtHost());
	}

	protected String namespaceGetName() {
		return getUsernameAtHost();
	}

	protected int namespaceHashCode() {
		return getUsernameAtHost().hashCode();
	}

	public String getUsername() {
		return username;
	}

	public String getHostname() {
		return hostname;
	}

	public String getResourceName() {
		return resourcename;
	}

	public int getPort() {
		return port;
	}

	public String getUsernameAtHost() {
		return getUsername() + "@" + getHostname();
	}

	public String getFQName() {
		return getUsernameAtHost() + getResourceName();
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer("XMPPID[");
		sb.append(uri.toString()).append("]");
		return sb.toString();
	}

	public Object getAdapter(Class clazz) {
		if (clazz.isInstance(this)) {
			return this;
		} else
			return super.getAdapter(clazz);
	}
}
