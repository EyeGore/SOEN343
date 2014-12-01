/*
    EZ Intranet Messenger

    Copyright (C) 2007 - 2014  Chun-Kwong Wong
    chunkwong.wong@gmail.com
    http://EzimNetwork.sourceforge.net/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ezim.core;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

public class EzimNetwork
{
	// D E F A U L T   C O N S T A N T S -----------------------------------
	// thread pool sizes and keep alive time (in minutes)
	public final static int thPoolSizeCore = 8;
	public final static int thPoolSizeMax = 16;
	public final static int thPoolKeepAlive = 30;

	// multicast group, port, TTL, and incoming buffer size
	// where group should be from 224.0.0.0 to 239.255.255.255
	public final static String mcGroupIPv4 = "229.0.0.1";
	public final static String mcGroupIPv6 = "ff15::657a:696d";
	public final static int mcPort = 5555;
	public final static int ttl = 1;
	public final static int inBuf = 4096;

	// maximum textfield lengths (for ACK messages)
	public final static int maxAckLength = inBuf / 4;

	// maximum textarea lengths (for DTX messages)
	public final static int maxMsgLength = Integer.MAX_VALUE / 4;

	// direct transmission port and timeout limit (in ms)
	public final static int dtxPort = 6666;
	public final static int dtxTimeout = 30000;

	// direct transmission message encoding
	public final static String dtxMsgEnc = "UTF-8";

	// direct transmission buffer length (in bytes)
	public final static int dtxBufLen = 1024;

	// time interval for the refresh button to be re-enabled after being
	// clicked (to avoid ACK flooding)
	public final static int rfhBtnTI = 5000;

	// thread pool termination timeout (in seconds) at exit
	public final static long exitTimeout = 15;

	// regexp for validating IPv4 address
	public final static String regexpIPv4
		= "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)"
		+ "(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";

	// regexp for validating IPv6 address
	public final static String regexpIPv6
		= "\\A("
		+ "(?:(?:[0-9A-Fa-f]{1,4}\\:){1,1}(?:\\:[0-9A-Fa-f]{1,4}){1,6})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,2}(?:\\:[0-9A-Fa-f]{1,4}){1,5})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,3}(?:\\:[0-9A-Fa-f]{1,4}){1,4})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,4}(?:\\:[0-9A-Fa-f]{1,4}){1,3})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,5}(?:\\:[0-9A-Fa-f]{1,4}){1,2})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,6}(?:\\:[0-9A-Fa-f]{1,4}){1,1})"
		+ "|(?:[0-9A-Fa-f]{1,4}(?:\\:[0-9A-Fa-f]{1,4}){7})"
		+ "|(?:(?:(?:[0-9A-Fa-f]{1,4}\\:){1,7}|\\:)\\:)"
		+ "|(?:\\:(?:\\:[0-9A-Fa-f]{1,4}){1,7})"
		+ "|(?:(?:(?:(?:[0-9A-Fa-f]{1,4}\\:){6})(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}))"
		+ "|(?:(?:(?:[0-9A-Fa-f]{1,4}\\:){5}[0-9A-Fa-f]{1,4}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}))"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){5}\\:[0-9A-Fa-f]{1,4}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,1}(?:\\:[0-9A-Fa-f]{1,4}){1,4}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,2}(?:\\:[0-9A-Fa-f]{1,4}){1,3}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,3}(?:\\:[0-9A-Fa-f]{1,4}){1,2}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:(?:[0-9A-Fa-f]{1,4}\\:){1,4}(?:\\:[0-9A-Fa-f]{1,4}){1,1}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:(?:(?:[0-9A-Fa-f]{1,4}\\:){1,5}|\\:)\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ "|(?:\\:(?:\\:[0-9A-Fa-f]{1,4}){1,5}\\:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
		+ ")(?:\\%[^\\%]+)?\\z"
		;
	
	// P R O P E R T I E S -------------------------------------------------
	private static Hashtable<NetworkInterface, List<InetAddress>>
	nifs = null;

	public static NetworkInterface localNI = null;
	public static InetAddress localAddress = null;
	public static int localDtxPort = 0;
	public static String localName = null;

	public static Thread thAckTaker = null;
	public static Thread thDtxTaker = null;
	
	// P R I V A T E   M E T H O D S ---------------------------------------
	/**
	 * scan all network interfaces
	 */
	private static void scanNetworkInterfaces()
	{
		Enumeration<NetworkInterface> enumNI = null;
		NetworkInterface niTmp = null;
		EzimNetwork.nifs = new Hashtable<NetworkInterface, List<InetAddress>>();

		try
		{
			enumNI = NetworkInterface.getNetworkInterfaces();

			while(enumNI.hasMoreElements())
			{
				niTmp = enumNI.nextElement();

				if (! niTmp.isUp() || ! niTmp.supportsMulticast()) continue;

				EzimNetwork.nifs.put
				(
					niTmp
					, new ArrayList<InetAddress>
					(
						Collections.list(niTmp.getInetAddresses())
					)
				);
			}
		}
		catch(Exception e)
		{
			EzimLogger.getInstance().severe(e.getMessage(), e);
			Ezim.exit(1);
		}
	}

	/**
	 * parse and instantiate an InetAddress
	 */
	private static InetAddress parseInetAddress(String strIn)
	{
		InetAddress iaOut = null;
		String[] strBytes = null;
		byte[] arrBytes = null;

		if
		(
			strIn.matches(EzimNetwork.regexpIPv4)
			|| strIn.matches(EzimNetwork.regexpIPv6)
		)
		{
			try
			{
				iaOut = InetAddress.getByName(strIn);
			}
			catch(Exception e)
			{
				// this should NEVER happen
				EzimLogger.getInstance().severe(e.getMessage(), e);
				iaOut = null;
			}
		}

		return iaOut;
	}

	/**
	 * set local network interface and address
	 */
	private static void setLocalNiAddress()
    {
        String strLclNi = EzimConf.NET_LOCALNI;
        String strLclAdr = EzimConf.NET_LOCALADDRESS;

        if (strLclNi != null && strLclNi.length() > 0)
        {
            applyNiSetting(strLclNi);					// refactored
        }

        if
        (
            strLclAdr != null && strLclAdr.length() > 0
            && EzimNetwork.localNI != null
        )
        {
            List<InetAddress> lTmp = EzimNetwork.nifs.get(EzimNetwork.localNI);

            applyLocalAddressSetting(strLclAdr, lTmp);	// refactored
        }

        // confine our selectable network interfaces
        Collection<List<InetAddress>> cTmp = null;

        cTmp = confineSelectableNi();					// refactored

        if (EzimNetwork.localAddress == null)					// refactored: duplicated loops removed
        {
            for(List<InetAddress> lTmp: cTmp)
            {
                for(InetAddress iaTmp: lTmp)
                {
                    if (iaTmp instanceof Inet6Address && ! iaTmp.isLoopbackAddress() && ! iaTmp.isLinkLocalAddress()) // try to pick an IPv6 non-loopback and non-link-locale address
                    {
                    	EzimNetwork.localAddress = iaTmp;
                        break;
                    }
                    
                    if (! iaTmp.isLoopbackAddress() && ! iaTmp.isLinkLocalAddress()) // try to pick a non-loopback and non-link-locale address
                    {
                    	EzimNetwork.localAddress = iaTmp;
                        break;
                    }
                    
                    if (iaTmp instanceof Inet6Address && ! iaTmp.isLoopbackAddress()) // try to pick an IPv6 non-loopback address
                    {
                    	EzimNetwork.localAddress = iaTmp;
                        break;
                    }
                    
                    if (! iaTmp.isLoopbackAddress())    // try to pick a non-loopback address
                    {
                    	EzimNetwork.localAddress = iaTmp;
                        break;
                    }
                    
                    if (iaTmp instanceof Inet6Address)    // try to pick an IPv6 address
                    {
                    	EzimNetwork.localAddress = iaTmp;
                        break;
                    }
                }
            }
            
            
        // pick the first available address when all failed
        if (EzimNetwork.localAddress == null)
        {
        	EzimNetwork.localAddress = EzimNetwork.nifs.elements().nextElement().get(0);
        }

        if (null == EzimNetwork.localNI)
            for(NetworkInterface niTmp: EzimNetwork.nifs.keySet())
                if (EzimNetwork.nifs.get(niTmp).contains(EzimNetwork.localAddress))
                	EzimNetwork.localNI = niTmp;
        }


        // save local network interface
        EzimConf.NET_LOCALNI = EzimNetwork.localNI.getName();

        // save local address
        EzimConf.NET_LOCALADDRESS = EzimNetwork.localAddress.getHostAddress();
    }

	private static void applyLocalAddressSetting(String strLclAdr,
			List<InetAddress> lTmp) {
		for(InetAddress iaTmp: lTmp)
		    if (strLclAdr.equals(iaTmp.getHostAddress()))
		    	EzimNetwork.localAddress = iaTmp;

		if (null == EzimNetwork.localAddress)
		{
		    EzimLogger.getInstance().warning
		    (
		        "Invalid local address setting \"" + strLclAdr
		            + "\"."
		    );
		}
	}

	private static void applyNiSetting(String strLclNi) {
		for(NetworkInterface niTmp: EzimNetwork.nifs.keySet())
		    if (strLclNi.equals(niTmp.getName()))
		    	EzimNetwork.localNI = niTmp;

		if (null == EzimNetwork.localNI)
		{
		    EzimLogger.getInstance().warning
		    (
		        "Invalid network interface setting \"" + strLclNi
		            + "\"."
		    );
		}
	}

	private static Collection<List<InetAddress>> confineSelectableNi() {
		Collection<List<InetAddress>> cTmp;
		if (null == EzimNetwork.localNI)
        {
            cTmp = EzimNetwork.nifs.values();
        }
        else
        {
            cTmp = new ArrayList<List<InetAddress>>();

            ((ArrayList<List<InetAddress>>) cTmp).add
            (
        		EzimNetwork.nifs.get(EzimNetwork.localNI)
            );
        }
		return cTmp;
	}

	/**
	 * set multicast group IP address
	 */
	private static void setMcGroup()
	{
		String strMcGroup = EzimConf.NET_MC_GROUP;

		// set multicast group to default if not yet determined or
		// inappropriate
		if
		(
			EzimNetwork.localAddress instanceof Inet6Address
			&& (
				strMcGroup == null
				|| ! strMcGroup.matches(EzimNetwork.regexpIPv6)
			)
		)
		{
			EzimConf.NET_MC_GROUP = EzimNetwork.mcGroupIPv6;
		}
		else if
		(
			EzimNetwork.localAddress instanceof Inet4Address
			&& (
				strMcGroup == null
				|| ! strMcGroup.matches(EzimNetwork.regexpIPv4)
			)
		)
		{
			EzimConf.NET_MC_GROUP = EzimNetwork.mcGroupIPv4;
		}
		else
		{
			InetAddress iaTmp = null;

			try
			{
				iaTmp = InetAddress.getByName(strMcGroup);
			}
			catch(Exception e)
			{
				// this should NEVER happen
				EzimLogger.getInstance().severe(e.getMessage(), e);
				iaTmp = null;
			}

			if (iaTmp == null || ! iaTmp.isMulticastAddress())
			{
				if (EzimNetwork.localAddress instanceof Inet6Address)
					EzimConf.NET_MC_GROUP = EzimNetwork.mcGroupIPv6;
				else if (EzimNetwork.localAddress instanceof Inet4Address)
					EzimConf.NET_MC_GROUP = EzimNetwork.mcGroupIPv4;
			}
		}
	}

	/**
	 * set operating network interface
	 */
	private static void setOperatingNI()
	{
		try
		{
			EzimNetwork.localNI = NetworkInterface.getByInetAddress
			(
				EzimNetwork.localAddress
			);
		}
		catch(Exception e)
		{
			EzimLogger.getInstance().severe(e.getMessage(), e);
			Ezim.exit(1);
		}
	}

	/**
	 * set local DTX port
	 */
	private static void setLocalDtxPort()
	{
		EzimNetwork.localDtxPort = EzimConf.NET_DTX_PORT;
	}
	
	// P U B L I C   M E T H O D S -----------------------------------------
	/**
	 * get all available network interfaces
	 */
	public static Collection<NetworkInterface> getLocalNIs()
	{
		return (Collection<NetworkInterface>)
			EzimNetwork.nifs.keySet();
	}

	/**
	 * get all addresses associated with the specified network interface
	 * @param network interface to retrieve addresses
	 */
	public static List<InetAddress> getNIAddresses(NetworkInterface niIn)
	{
		return EzimNetwork.nifs.get(niIn);
	}

	/**
	 * check if the address provided is local
	 * @param iaIn address to check against
	 */
	public static boolean isLocalAddress(InetAddress iaIn)
	{
		for(List<InetAddress> lAddrs: EzimNetwork.nifs.values())
			for(InetAddress iaddr: lAddrs)
				if (iaIn.equals(iaddr)) return true;

		return false;
	}

	// P R O T E C T E D   M E T H O D S -----------------------------------------
	/**
	 * set local name
	 */
	protected static void setLocalName()
	{
		EzimNetwork.localName = EzimConf.NET_LOCALNAME;

		// query username if isn't set yet
		if (EzimNetwork.localName == null || EzimNetwork.localName.length() == 0)
		{
			String strTmp = null;

			// obtain user name
			while(strTmp == null || strTmp.length() == 0)
			{
				strTmp = JOptionPane.showInputDialog
				(
					EzimLang.PleaseInputYourName
				);
			}

			EzimNetwork.localName = strTmp;

			// save username
			EzimConf.NET_LOCALNAME = EzimNetwork.localName;

			EzimConf.write();
		}
	}

	/**
	 * initialize network configurations
	 */
	protected static void initNetConf()
	{
		EzimNetwork.scanNetworkInterfaces();
		EzimNetwork.setLocalNiAddress();
		EzimNetwork.setMcGroup();
		EzimNetwork.setOperatingNI();
		EzimNetwork.setLocalDtxPort();
		EzimNetwork.setLocalName();
	}
}
