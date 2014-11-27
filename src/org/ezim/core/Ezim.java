/*
    EZ Intranet Messenger

    Copyright (C) 2007 - 2014  Chun-Kwong Wong
    chunkwong.wong@gmail.com
    http://ezim.sourceforge.net/

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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.ezim.core.EzimAckSemantics;
import org.ezim.core.EzimAckSender;
import org.ezim.core.EzimAckTaker;
import org.ezim.core.EzimConf;
import org.ezim.core.EzimDtxTaker;
import org.ezim.core.EzimLang;
import org.ezim.core.EzimLogger;
import org.ezim.core.EzimNetwork;
import org.ezim.core.EzimThreadPool;
import org.ezim.ui.EzimMain;

public class Ezim
{
	// D E F A U L T   C O N S T A N T S -----------------------------------
	// application name and version
	public final static String appName = "EZ Intranet Messenger";
	public final static String appAbbrev = "ezim";
	public final static String appVer = "1.2.27";

	// self-entry background color on the contact list
	public final static int colorSelf = (int) 0xDEEFFF;

	// available locales
	public final static Locale[] locales =
	{
		Locale.US
		, Locale.JAPAN
		, Locale.SIMPLIFIED_CHINESE
		, Locale.TRADITIONAL_CHINESE
		, new Locale("es")			// Spanish
		, new Locale("pt", "BR")	// Portuguese (Brazil)
		, Locale.ITALY
		, Locale.FRANCE
		, Locale.GERMANY
		, new Locale("nl", "NL")	// Dutch (Netherlands)
		, new Locale("el", "GR")	// Greek (Greece)
	};

	// regexp for validating RGB color
	public final static String regexpRgb = "\\A[0-9A-Fa-f]{6}\\z";

	// regexp for validating boolean values
	public final static String regexpBool = "\\A(true|false)\\z";

	// regexp for validating integer values
	public final static String regexpInt = "\\A([1-9][0-9]*|0)\\z";

	// valid state icon sizes
	public final static Integer[] stateiconSizes = {16, 24, 32};
	
	// P R O P E R T I E S -------------------------------------------------
	private static volatile boolean running = true;
	private static volatile boolean shutdown = false;

	// P R I V A T E   M E T H O D S ---------------------------------------
	/**
	 * locale change has to be here in order to work properly
	 */
	private static void setDefaultLocale()
	{
		String strLocale = EzimConf.UI_USER_LOCALE;

		for(int iCnt = 0; iCnt < Ezim.locales.length; iCnt ++)
		{
			if (strLocale.equals(Ezim.locales[iCnt].toString()))
			{
				Locale.setDefault(Ezim.locales[iCnt]);
				break;
			}
		}
	}

	// P U B L I C   M E T H O D S -----------------------------------------
	/**
	 * perform cleanup and exit
	 * @param iIn exit code
	 */
	public static void exit(final int iIn)
	{
		if (! Ezim.running) return;

		Ezim.running = false;

		EzimNetwork.thAckTaker.interrupt();
		EzimAckTaker.getInstance().closeSocket();
		EzimNetwork.thDtxTaker.interrupt();
		EzimDtxTaker.getInstance().closeSocket();

		EzimThreadPool etpTmp = EzimThreadPool.getInstance();

		// acknowledge other peers we're going offline
		EzimAckSender easOff = new EzimAckSender
		(
			EzimAckSemantics.offline()
		);
		etpTmp.execute(easOff);

		etpTmp.shutdown();

		try
		{
			etpTmp.awaitTermination(EzimNetwork.exitTimeout, TimeUnit.SECONDS);
		}
		catch(InterruptedException ie)
		{
			EzimLogger.getInstance().warning(ie.getMessage(), ie);
		}

		if (! Ezim.shutdown) System.exit(iIn);
	}

	/**
	 * the main function which gets executed
	 * @param arrArgs command line arguments
	 */
	public static void main(String[] arrArgs)
	{
		Ezim.setDefaultLocale();

		UIManager.put("Button.defaultButtonFollowsFocus", true);

		EzimLang.init();
		EzimImage.init();

		EzimNetwork.initNetConf();

		EzimMain emTmp = EzimMain.getInstance();

		EzimThreadPool etpTmp = EzimThreadPool.getInstance();

		EzimAckSender.prepareSocket();

		EzimNetwork.thDtxTaker = new Thread(EzimDtxTaker.getInstance());
		EzimNetwork.thDtxTaker.setDaemon(true);
		EzimNetwork.thDtxTaker.start();

		EzimNetwork.thAckTaker = new Thread(EzimAckTaker.getInstance());
		EzimNetwork.thAckTaker.setDaemon(true);
		EzimNetwork.thAckTaker.start();

		try
		{
			Thread.sleep(500);
		}
		catch(Exception e)
		{
			EzimLogger.getInstance().severe(e.getMessage(), e);
		}

		// FIXME:2013-11-17:Chun:This make EZIM halts on exit on Win7
/*
		// execute proper ending processes when JVM shuts down
		Runtime.getRuntime().addShutdownHook
		(
			new Thread()
			{
				public void run()
				{
					Ezim.shutdown = true;
					EzimMain.getInstance().panic(0);
				}
			}
		);
*/

		EzimAckSemantics.sendAllInfo();
		emTmp.freshPoll();
	}
}
