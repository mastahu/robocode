/*******************************************************************************
 * Copyright (c) 2001, 2009 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Pavel Savara
 *     - Initial implementation
 *******************************************************************************/
package net.sf.robocode.repository2.items;


import net.sf.robocode.repository2.root.IRepositoryRoot;

import java.net.URL;
import java.util.List;


/**
 * @author Pavel Savara (original)
 */
public class IgnoredItem extends BaseItem {
	private static final long serialVersionUID = 1L;

	public IgnoredItem(URL url, IRepositoryRoot root) {
		super(url, root);
	}

	public List<String> getFriendlyUrls() {
		return null;
	}

	public void update(long lastModified, boolean force) {// no action
	}

	public boolean isValid() {
		return false;
	}

	public String toString() {
		return url.toString();
	}
}