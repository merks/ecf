/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class TopologyManager extends AbstractTopologyManager implements
		EventHook, EndpointListener {

	private ServiceRegistration endpointListenerRegistration;

	private ServiceRegistration eventHookRegistration;

	public TopologyManager(BundleContext context) {
		super(context);
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				"("
						+ org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
						+ "=*)");
		endpointListenerRegistration = getContext().registerService(
				EndpointListener.class.getName(), this, (Dictionary) props);

		// Register as EventHook, so that we get notified when remote services
		// are registered
		eventHookRegistration = getContext().registerService(
				EventHook.class.getName(), this, null);
	}

	public void close() {
		if (eventHookRegistration != null) {
			eventHookRegistration.unregister();
			eventHookRegistration = null;
		}
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		super.close();
	}

	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription)
			handleEndpointAdded((EndpointDescription) endpoint);
		else
			logWarning("endpointAdded",
					"ECF Topology Manager:  Ignoring Non-ECF endpointAdded="
							+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription)
			handleEndpointRemoved((EndpointDescription) endpoint);
		else
			logWarning("endpointRemoved",
					"ECF Topology Manager:  Ignoring Non-ECF endpointRemoved="
							+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	public void event(ServiceEvent event, Collection contexts) {
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
			handleServiceModifying(event.getServiceReference());
			break;
		case ServiceEvent.MODIFIED_ENDMATCH:
			break;
		case ServiceEvent.REGISTERED:
			handleServiceRegistering(event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			handleServiceUnregistering(event.getServiceReference());
			break;
		default:
			break;
		}

	}

}
