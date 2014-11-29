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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;

import org.ezim.ui.EzimMsgIn;

public class EzimDtxMessageSemantics
{
	// C L A S S   C O N S T A N T -----------------------------------------
//	private final static String CTYPE_MSGRT		= "Message-Return-Ticket";

	// header field "Subject"
	// valid content type: "Message"
	private final static String HDR_SBJ			= "Subject";

	// M E T H O D S   F O R   O U T G O I N G   T R A N S M I S S I O N ---
	/**
	 * format and output message
	 * @param sckIn outgoing socket
	 * @param strSbj subject line of the outgoing message
	 * @param strMsg message to be formatted and sent
	 */
	public static void sendMsg(Socket sckIn, String strSbj, String strMsg)
		throws Exception
	{
		EzimDtxSemantics.initByteArrays();

		byte[] bMsg = null;

		OutputStream osTmp = sckIn.getOutputStream();

		// convert message body in bytes
		bMsg = strMsg.getBytes(EzimNetwork.dtxMsgEnc);

		// create necessary header fields
		Hashtable<String, String> htTmp
			= new Hashtable<String, String>();

		htTmp.put
		(
			EzimDtxSemantics.HDR_CTYPE
			, EzimDtxSemantics.CTYPE_MSG
		);
		htTmp.put
		(
			EzimDtxSemantics.HDR_CLEN
			, Integer.toString(bMsg.length)
		);
		htTmp.put
		(
			EzimDtxMessageSemantics.HDR_SBJ
			, strSbj
		);

		// output header in bytes
		EzimDtxSemantics.sendHeaderBytes(sckIn, htTmp);

		// output message body
		osTmp.write(bMsg);

		// make sure everything is sent
		osTmp.flush();
	}

	// M E T H O D S   F O R   I N C O M I N G   T R A N S M I S S I O N ---
	/**
	 * retrieve message from incoming socket
	 * @param isIn input stream which streams raw incoming data
	 * @param iCLen length of the message in bytes
	 * @param ecIn peer user who made the direct transmission
	 * @param strSbj subject line of the incoming message
	 * @return
	 */
	private static void getMsg
	(
		InputStream isIn
		, int iCLen
		, EzimContact ecIn
		, String strSbj
	)
		throws Exception
	{
		byte[] bBuf = new byte[iCLen];
		int iTmp = 0;
		int iCnt = 0;
		String strTmp = null;

		while (! (iTmp < 0) && iCnt < iCLen)
		{
			iTmp = isIn.read();

			if (! (iTmp < 0))
			{
				bBuf[iCnt] = (byte) iTmp;
				iCnt ++;
			}
		}

		strTmp = new String(bBuf, 0, iCnt, EzimNetwork.dtxMsgEnc);

		new EzimMsgIn(ecIn, strSbj, strTmp);
	}
	
	protected static void messageParser
	(
		InputStream isData
		, EzimContact ecIn
		, Hashtable<String, String> htHdr
		, long lCLen
	) throws Exception
	{
		if (lCLen > (EzimNetwork.maxMsgLength * 4))
		{
			throw new EzimException
			(
				"Illegally large incoming message from \""
				+ ecIn.getName()
				+ " (" + ecIn.getAddress().getHostAddress()
				+ ")\" detected."
			);
		}

		String strSbj = htHdr.get
		(
			EzimDtxMessageSemantics.HDR_SBJ
		);

		EzimDtxMessageSemantics.getMsg
		(
			isData
			, (int) lCLen
			, ecIn
			, strSbj
		);
	}
}
