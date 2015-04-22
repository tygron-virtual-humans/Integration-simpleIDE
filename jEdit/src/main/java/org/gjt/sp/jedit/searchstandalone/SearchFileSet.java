/*
 * SearchFileSet.java - Abstract file matcher interface
 * Copyright (C) 1999, 2001 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.searchstandalone;

import org.gjt.sp.jedit.*;

/**
 * An abstract interface representing a set of files.
 * @author Slava Pestov
 * @version $Id: SearchFileSet.java 12504 2008-04-22 23:12:43Z ezust $
 */
public interface SearchFileSet
{
	/**
	 * Returns the first file to search.
	 * @param view The view performing the search
	 */
	String getFirstFile(ViewInterface view);

	/**
	 * Returns the next file to search.
	 * @param view The view performing the search
	 * @param path The last file searched
	 */
	String getNextFile(ViewInterface view, String path);

	/**
	 * Returns all path names in this file set.
	 * @param view The view performing the search
	 */
	String[] getFiles(ViewInterface view);

	/**
	 * Returns the number of files in this file set.
	 */
	int getFileCount(ViewInterface view);

	/**
	 * Returns the BeanShell code that will recreate this file set.
	 */
	String getCode();
}
