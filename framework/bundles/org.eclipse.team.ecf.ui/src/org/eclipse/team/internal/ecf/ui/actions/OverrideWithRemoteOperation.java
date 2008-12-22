/******************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ecf.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

class OverrideWithRemoteOperation extends SynchronizeModelOperation {

	protected OverrideWithRemoteOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		super(configuration, elements);
	}

	private ISchedulingRule createSchedulingRule(Collection rules) {
		if (rules.size() == 1) {
			return (ISchedulingRule) rules.iterator().next();
		} else {
			return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
		}
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		SyncInfoSet syncInfoSet = getSyncInfoSet();
		SyncInfo[] syncInfos = syncInfoSet.getSyncInfos();

		Set projects = new HashSet();
		for (int i = 0; i < syncInfos.length; i++) {
			projects.add(syncInfos[i].getLocal().getProject());
		}

		try {
			ResourcesPlugin.getWorkspace().run(new OverrideWithRemoteRunnable(syncInfos), createSchedulingRule(projects), IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	class OverrideWithRemoteRunnable implements IWorkspaceRunnable {

		private SyncInfo[] syncInfos;

		public OverrideWithRemoteRunnable(SyncInfo[] syncInfos) {
			this.syncInfos = syncInfos;
		}

		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.beginTask("", syncInfos.length); //$NON-NLS-1$
			monitor.subTask("Overriding resources with remote copy...");

			for (int i = 0; i < syncInfos.length; i++) {
				if (monitor.isCanceled()) {
					return;
				}

				IResourceVariant remoteVariant = syncInfos[i].getRemote();
				IResource resource = syncInfos[i].getLocal();

				switch (syncInfos[i].getKind() & SyncInfo.CHANGE_MASK) {
					case SyncInfo.ADDITION :
						monitor.subTask("Creating " + resource.getName() + "...");
						switch (resource.getType()) {
							case IResource.FILE :
								IStorage storage = remoteVariant.getStorage(null);
								// create parent folders of the resource if applicable
								createParents(resource);
								((IFile) resource).create(storage.getContents(), true, new SubProgressMonitor(monitor, 1));
								break;
							case IResource.FOLDER :
								// technically, the folder shouldn't exist if we're supposed
								// to be adding the resource, however, we precreate parents
								// of files when creating files and the parent folder may be
								// created as a side effect of that, so we add this check
								// here, note, not having this call was causing problems in
								// RemoteSyncInfo's calculateKind() method
								if (!resource.exists()) {
									((IFolder) resource).create(true, true, new SubProgressMonitor(monitor, 1));
								}
								break;
							default :
								monitor.worked(1);
								break;
						}
						break;
					case SyncInfo.CHANGE :
						switch (resource.getType()) {
							case IResource.FILE :
								monitor.subTask("Replacing " + resource.getName() + " with remote content...");
								IStorage storage = remoteVariant.getStorage(null);
								((IFile) resource).setContents(storage.getContents(), true, true, new SubProgressMonitor(monitor, 1));
								break;
							default :
								monitor.worked(1);
								break;
						}
						break;
					case SyncInfo.DELETION :
						if (resource.exists()) {
							monitor.subTask("Deleting " + resource.getName() + "...");
						}
						resource.delete(true, new SubProgressMonitor(monitor, 1));
						break;
				}
			}
		}

		private void createParents(IResource resource) throws CoreException {
			IContainer container = resource.getParent();
			while (!container.exists() && container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder) container;
				folder.create(true, true, null);
				container = folder.getParent();
			}
		}

	}
}
