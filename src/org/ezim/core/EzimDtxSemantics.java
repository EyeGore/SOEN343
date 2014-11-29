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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import org.ezim.core.EzimContact;
import org.ezim.core.EzimLogger;

public class EzimDtxSemantics
{
	// C L A S S   C O N S T A N T -----------------------------------------
	// header field name and value separator
	protected final static String HDRSPR			= ": ";
	protected final static String CRLF			= "\r\n";

	// header terminator
	protected final static String HDRTRM			= "\r\n";

	// header field "Content-Type"
	protected final static String HDR_CTYPE		= "Content-Type";
	protected final static String CTYPE_MSG		= "Message";

	// header field "Content-Length"
	protected final static String HDR_CLEN		= "Content-Length";

	// C L A S S   V A R I A B L E -----------------------------------------
	protected static byte[] bHDRSPR	= null;
	protected static byte[] bCRLF		= null;
	protected static byte[] bHDRTRM	= null;

	// M E T H O D S   F O R   O U T G O I N G   T R A N S M I S S I O N ---
	/**
	 * initialize byte array class variables
	 */
	protected static void initByteArrays()
		throws Exception
	{
		EzimDtxSemantics.bHDRSPR
			= EzimDtxSemantics.HDRSPR.getBytes(EzimNetwork.dtxMsgEnc);

		EzimDtxSemantics.bCRLF
			= EzimDtxSemantics.CRLF.getBytes(EzimNetwork.dtxMsgEnc);

		EzimDtxSemantics.bHDRTRM
			= EzimDtxSemantics.HDRTRM.getBytes(EzimNetwork.dtxMsgEnc);
	}

	/**
	 * transform and output header in bytes to the output stream given
	 * @param sckIn outgoing socket
	 * @param htIn hashtable containing the header fields
	 * @return
	 */
	protected static void sendHeaderBytes
	(
		Socket sckIn
		, Hashtable<String, String> htIn
	)
		throws Exception
	{
		OutputStream osIn = sckIn.getOutputStream();
		Enumeration enumKeys = null;
		String strKey = null;
		String strVal = null;

		if (htIn != null)
		{
			enumKeys = htIn.keys();

			// header fields
			while(enumKeys.hasMoreElements())
			{
				strKey = (String) enumKeys.nextElement();
				strVal = htIn.get(strKey);

				osIn.write(strKey.getBytes(EzimNetwork.dtxMsgEnc));
				osIn.write(EzimDtxSemantics.bHDRSPR);
				osIn.write(strVal.getBytes(EzimNetwork.dtxMsgEnc));
				osIn.write(EzimDtxSemantics.bCRLF);
			}

			// header terminator
			osIn.write(EzimDtxSemantics.bHDRTRM);
		}
	}

	// M E T H O D S   F O R   I N C O M I N G   T R A N S M I S S I O N ---
	/**
	 * retrieve header from the incoming socket
	 * @param fIn file which contains the header
	 * @return hashtabled header
	 */
	private static Hashtable<String, String> getHeader
	(
		File fIn
	)
		throws Exception
	{
		Hashtable<String, String> htOut = null;
		FileInputStream fisHdr = new FileInputStream(fIn);
		byte[] bBuf = null;
		String strHdr = null;
		String[] arrLines = null;
		String[] arrHdrFldParts = null;

		htOut = new Hashtable<String, String>();

		try
		{
			fisHdr = new FileInputStream(fIn);
			bBuf = new byte[fisHdr.available()];
			fisHdr.read(bBuf);
		}
		finally
		{
			fisHdr.close();
		}

		strHdr = new String(bBuf, EzimNetwork.dtxMsgEnc);
		arrLines = strHdr.split(EzimDtxSemantics.CRLF);

		for(int iX = 0; iX < arrLines.length; iX ++)
		{
			arrHdrFldParts = arrLines[iX].split(EzimDtxSemantics.HDRSPR, 2);

			if (arrHdrFldParts.length > 1)
				htOut.put(arrHdrFldParts[0], arrHdrFldParts[1]);
		}

		return htOut;
	}

	/**
	 * parse all incoming direct transmissions and react accordingly
	 * @param fIn file containing the header
	 * @param sckIn incoming socket
	 * @param ecIn peer user who made the direct transmission
	 */
	public static void parser(File fIn, Socket sckIn, EzimContact ecIn)
	{
		Hashtable<String, String> htHdr = null;
		InputStream isData = null;
		String strCType = null;
		String strCLen = null;
		long lCLen = 0;

		try
		{
			isData = sckIn.getInputStream();
			htHdr = EzimDtxSemantics.getHeader(fIn);
			strCType = htHdr.get(EzimDtxSemantics.HDR_CTYPE);
			strCLen = htHdr.get(EzimDtxSemantics.HDR_CLEN);

			if (strCType == null)
			{
				throw new Exception
				(
					"Header field \"Content-Type\" is missing."
				);
			}
			else if (strCLen == null)
			{
				throw new Exception
				(
					"Header field \"Content-Length\" is missing."
				);
			}
			else
			{
				lCLen = Long.parseLong(strCLen);

				// receive incoming message
				if (strCType.equals(EzimDtxSemantics.CTYPE_MSG))
				{
					EzimDtxMessageSemantics.messageParser
					( 
						isData
						, ecIn
						, htHdr
						, lCLen
					);
				}
				else
				{
					EzimDtxFileSemantics.fileParser
					(
						isData
						, sckIn
						, ecIn
						, htHdr
						, strCType
						, lCLen
					);
				}
			}
		}
		catch(EzimException ee)
		{
			EzimLogger.getInstance().warning(ee.getMessage(), ee);
		}
		catch(Exception e)
		{
			EzimLogger.getInstance().severe(e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (isData != null) isData.close();
			}
			catch(Exception e)
			{
				EzimLogger.getInstance().severe(e.getMessage(), e);
			}
		}
	}
}
