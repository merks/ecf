/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;

public class GenericContainerInstantiator implements IContainerInstantiator {

	/**
	 * @since 2.0
	 */
	protected static final String[] genericProviderIntents = {"passByValue", "exactlyOnce", "ordered",}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String TCPCLIENT_NAME = "ecf.generic.client"; //$NON-NLS-1$

	public static final String TCPSERVER_NAME = "ecf.generic.server"; //$NON-NLS-1$

	private static final int CREATE_INSTANCE_ERROR_CODE = 4441;

	public GenericContainerInstantiator() {
		super();
	}

	protected ID getIDFromArg(Object arg) throws IDCreateException {
		if (arg == null)
			throw new IDCreateException(Messages.getString("GenericContainerInstantiator.ID_Cannot_Be_Null")); //$NON-NLS-1$
		String val = null;
		if (arg instanceof StringID)
			return (ID) arg;
		else if (arg instanceof GUID)
			val = ((GUID) arg).getName();
		else if (arg instanceof URIID)
			val = ((URIID) arg).toURI().toString();
		else if (arg instanceof LongID)
			val = ((LongID) arg).getName();
		if (arg instanceof String)
			val = (String) arg;
		if (arg instanceof Integer)
			val = IDFactory.getDefault().createGUID(((Integer) arg).intValue()).getName();
		if (val == null)
			val = IDFactory.getDefault().createGUID().getName();
		return IDFactory.getDefault().createStringID(val);
	}

	protected Integer getIntegerFromArg(Object arg) {
		if (arg == null)
			return new Integer(-1);
		if (arg instanceof Integer)
			return (Integer) arg;
		else if (arg instanceof String) {
			return new Integer((String) arg);
		} else
			return new Integer(-1);
	}

	protected class GenericContainerArgs {
		ID id;

		Integer keepAlive;

		public GenericContainerArgs(ID id, Integer keepAlive) {
			this.id = id;
			this.keepAlive = keepAlive;
		}

		public ID getID() {
			return id;
		}

		public Integer getKeepAlive() {
			return keepAlive;
		}
	}

	protected GenericContainerArgs getClientArgs(String[] argDefaults, Object[] args) throws IDCreateException {
		ID newID = null;
		Integer ka = null;
		if (argDefaults != null && argDefaults.length > 0) {
			if (argDefaults.length == 2) {
				newID = getIDFromArg(argDefaults[0]);
				ka = getIntegerFromArg(argDefaults[1]);
			} else
				ka = getIntegerFromArg(argDefaults[0]);
		}
		if (args != null && args.length > 0) {
			if (args.length == 2) {
				newID = getIDFromArg(args[0]);
				ka = getIntegerFromArg(args[1]);
			} else
				ka = getIntegerFromArg(args[0]);
		}
		if (newID == null)
			newID = IDFactory.getDefault().createStringID(IDFactory.getDefault().createGUID().getName());
		if (ka == null)
			ka = new Integer(0);
		return new GenericContainerArgs(newID, ka);
	}

	protected boolean isClient(ContainerTypeDescription description) {
		if (description.getName().equals(TCPSERVER_NAME))
			return false;
		return true;
	}

	protected GenericContainerArgs getServerArgs(String[] argDefaults, Object[] args) throws IDCreateException {
		ID newID = null;
		Integer ka = null;
		if (argDefaults != null && argDefaults.length > 0) {
			if (argDefaults.length == 2) {
				newID = getIDFromArg(argDefaults[0]);
				ka = getIntegerFromArg(argDefaults[1]);
			} else
				newID = getIDFromArg(argDefaults[0]);
		}
		if (args != null && args.length > 0) {
			if (args.length == 2) {
				newID = getIDFromArg(args[0]);
				ka = getIntegerFromArg(args[1]);
			} else
				newID = getIDFromArg(args[0]);
		}
		if (newID == null)
			newID = IDFactory.getDefault().createStringID(TCPServerSOContainer.DEFAULT_PROTOCOL + "://" + TCPServerSOContainer.DEFAULT_HOST + ":" + TCPServerSOContainer.DEFAULT_PORT + TCPServerSOContainer.DEFAULT_NAME); //$NON-NLS-1$ //$NON-NLS-2$
		if (ka == null)
			ka = new Integer(TCPServerSOContainer.DEFAULT_KEEPALIVE);
		return new GenericContainerArgs(newID, ka);
	}

	public IContainer createInstance(ContainerTypeDescription description, Object[] args) throws ContainerCreateException {
		boolean isClient = isClient(description);
		try {
			GenericContainerArgs gcargs = null;
			// XXX get arg defaults from description
			String[] argDefaults = null;
			if (isClient)
				gcargs = getClientArgs(argDefaults, args);
			else
				gcargs = getServerArgs(argDefaults, args);
			// new ID must not be null
			if (isClient) {
				return new TCPClientSOContainer(new SOContainerConfig(gcargs.getID()), gcargs.getKeepAlive().intValue());
			}
			return new TCPServerSOContainer(new SOContainerConfig(gcargs.getID()), gcargs.getKeepAlive().intValue());
		} catch (Exception e) {
			Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "createInstance", e); //$NON-NLS-1$
			ProviderPlugin.getDefault().log(new Status(IStatus.ERROR, ProviderPlugin.PLUGIN_ID, CREATE_INSTANCE_ERROR_CODE, "createInstance", e)); //$NON-NLS-1$
			Trace.throwing(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_THROWING, this.getClass(), "createInstance", e); //$NON-NLS-1$
			throw new ContainerCreateException("createInstance", e); //$NON-NLS-1$
		}
	}

	protected Set getAdaptersForClass(Class clazz) {
		Set result = new HashSet();
		IAdapterManager adapterManager = ProviderPlugin.getDefault().getAdapterManager();
		if (adapterManager != null)
			result.addAll(Arrays.asList(adapterManager.computeAdapterTypes(clazz)));
		return result;
	}

	protected Set getInterfacesForClass(Set s, Class clazz) {
		if (clazz.equals(Object.class))
			return s;
		s.addAll(getInterfacesForClass(s, clazz.getSuperclass()));
		s.addAll(Arrays.asList(clazz.getInterfaces()));
		return s;
	}

	protected Set getInterfacesForClass(Class clazz) {
		Set clazzes = getInterfacesForClass(new HashSet(), clazz);
		int index = 0;
		Set result = new HashSet();
		for (Iterator i = clazzes.iterator(); i.hasNext(); index++)
			result.add(((Class) i.next()).getName());
		return result;
	}

	protected String[] getInterfacesAndAdaptersForClass(Class clazz) {
		Set result = getAdaptersForClass(clazz);
		result.addAll(getInterfacesForClass(clazz));
		return (String[]) result.toArray(new String[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		if (!isClient(description))
			return getInterfacesAndAdaptersForClass(TCPServerSOContainer.class);
		return getInterfacesAndAdaptersForClass(TCPClientSOContainer.class);
	}

	/**
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 * @since 2.0
	 */
	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		if (!isClient(description))
			return new Class[][] { {ID.class}, {ID.class, Integer.class}};
		return new Class[][] { {}, {ID.class}, {ID.class, Integer.class}};
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return genericProviderIntents;
	}
}