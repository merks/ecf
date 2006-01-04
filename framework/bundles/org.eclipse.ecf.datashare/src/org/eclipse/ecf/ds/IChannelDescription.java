package org.eclipse.ecf.ds;

import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ITransactionConfiguration;

public interface IChannelDescription {
	public ID getCreateID();
	public IChannelListener getChannelListener();
	public ITransactionConfiguration getTransactionConfiguration();
	public Map getCreateProperties();
}
