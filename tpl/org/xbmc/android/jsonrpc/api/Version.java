/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */
package org.xbmc.android.jsonrpc.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A class containing version information about a specific XBMC build.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Version {

	private final String hash;
	private final Date date;
	private final Branch branch;
	private final Type type;
	
	private static Version VERSION;
	
	/**
	 * Returns the version of XBMC against which the JSON-RPC library was built.
	 * @return XBMC version
	 */
	public static Version get() {
		if (VERSION == null) {
			VERSION = new Version("%hash%", "%date%", Branch.UNKNOWN, Type.UNKNOWN);
		}
		return VERSION;
	}

	/**
	 * Version definition
	 * 
	 * @param revision Revision hash
	 * @param date Date in format 2012-11-10 09:33:15 +0100
	 * @param branch
	 * @param type
	 */
	private Version(String hash, String date, Branch branch, Type type) {
		final SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
		this.hash = hash;
		try {
			this.date = sfd.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Date must be in the form: yyyy-MM-dd HH:mm:ss Z (like: 2012-11-10 09:33:15 +0100).");
		}
		this.branch = branch;
		this.type = type;
	}

	/**
	 * Returns the git hash of the commit the library was built against.
	 * @return
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Returns the date of the XBMC build the library was built against.
	 * @return
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Returns the branch of the XBMC build the library was built against.
	 * @return
	 */
	public Branch getBranch() {
		return branch;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return hash + " (" + branch.toString().toLowerCase(Locale.US) + ")";
	}

	/**
	 * Defines XBMC branch
	 * @author freezy <freezy@xbmc.org>
	 */
	public enum Branch {
		EDEN, FRODO, MASTER, UNKNOWN;
	}

	/**
	 * Defines the type of the XBMC build
	 * @author freezy <freezy@xbmc.org>
	 */
	public enum Type {
		SNAPSHOT, NIGHTLY, RELEASE, BETA, RC, UNKNOWN;
	}
}
