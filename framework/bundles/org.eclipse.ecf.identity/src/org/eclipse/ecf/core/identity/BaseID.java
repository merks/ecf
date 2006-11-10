/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.identity;

/**
 * Base class for ID implementation classes
 * 
 * Plugin providers wishing to provide new Namespaces and ID implementations are
 * recommended (but not required) to use this class as a super class for their
 * ID implementation class
 * 
 */
public abstract class BaseID implements ID {
	private static final long serialVersionUID = -6242599410460002514L;

	Namespace namespace;

	protected BaseID() {
	}

	protected BaseID(Namespace namespace) {
		if (namespace == null)
			throw new RuntimeException(new InstantiationException(
					"namespace cannot be null"));
		this.namespace = namespace;
	}

	public int compareTo(Object o) {
		if (o == null || !(o instanceof BaseID))
			throw new ClassCastException("incompatible types for compare");
		return namespace.getCompareToForObject(this, (BaseID) o);
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof BaseID)) {
			return false;
		}
		return namespace.testIDEquals(this, (BaseID) o);
	}

	public String getName() {
		return namespace.getNameForID(this);
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public int hashCode() {
		return namespace.getHashCodeForID(this);
	}

	public String toExternalForm() {
		return namespace.toExternalForm(this);
	}

	protected abstract int namespaceCompareTo(BaseID o);

	protected abstract boolean namespaceEquals(BaseID o);

	protected abstract String namespaceGetName();

	protected abstract int namespaceHashCode();

	protected String namespaceToExternalForm() {
		return namespace.getScheme() + Namespace.SCHEME_SEPARATOR
				+ namespaceGetName();
	}

	public Object getAdapter(Class clazz) {
		return null;
	}
}