/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;

public class GenericContainerInstantiator implements IContainerInstantiator {
	public static final String TCPCLIENT_NAME = "ecf.generic.client";

	public static final String TCPSERVER_NAME = "ecf.generic.server";

	public GenericContainerInstantiator() {
		super();
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.getDefault(), ECFProviderDebugOptions.DEBUG,
				msg);
	}
	
	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.getDefault(),
				ECFProviderDebugOptions.EXCEPTIONS_CATCHING, SOContainer.class,
				msg, e);
	}

	protected ID getIDFromArg(Object arg) throws IDCreateException {
		if (arg instanceof ID)
			return (ID) arg;
		if (arg instanceof String) {
			String val = (String) arg;
			if (val == null || val.equals("")) {
				return IDFactory.getDefault().createGUID();
			} else
				return IDFactory.getDefault().createStringID((String) arg);
		} else if (arg instanceof Integer) {
			return IDFactory.getDefault()
					.createGUID(((Integer) arg).intValue());
		} else
			return IDFactory.getDefault().createGUID();
	}

	protected Integer getIntegerFromArg(Object arg)
			throws NumberFormatException {
		if (arg instanceof Integer)
			return (Integer) arg;
		else if (arg != null) {
			return new Integer((String) arg);
		} else
			return new Integer(-1);
	}

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		boolean isClient = true;
		if (description.getName().equals(TCPSERVER_NAME)) {
			debug("creating server");
			isClient = false;
		} else {
			debug("creating client");
		}
		ID newID = null;
		try {
			String[] argDefaults = description.getArgDefaults();
			newID = (argDefaults == null || argDefaults.length == 0) ? null
					: getIDFromArg(description.getArgDefaults()[0]);
			Integer ka = (argDefaults == null || argDefaults.length < 2) ? null
					: getIntegerFromArg(description.getArgDefaults()[1]);
			if (args != null) {
				if (args.length > 0) {
					newID = getIDFromArg(args[0]);
					if (args.length > 1) {
						ka = getIntegerFromArg(args[1]);
					}
				}
			}
			debug("id=" + newID + ";keepAlive=" + ka);
			// new ID must not be null
			if (newID == null)
				throw new ContainerCreateException("id must be provided");
			if (isClient) {
				return new TCPClientSOContainer(new SOContainerConfig(newID),
						ka.intValue());
			} else {
				return new TCPServerSOContainer(new SOContainerConfig(newID),
						ka.intValue());
			}
		} catch (ClassCastException e) {
			traceStack("ClassCastException", e);
			throw new ContainerCreateException(
					"Parameter type problem creating container", e);
		} catch (Exception e) {
			traceStack("Exception", e);
			throw new ContainerCreateException(
					"Exception creating generic container with id " + newID, e);
		}
	}
}