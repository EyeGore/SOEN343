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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;

import org.ezim.ui.EzimFileIn;
import org.ezim.ui.EzimFileOut;

public class EzimDtxFileSemantics
{
	// C L A S S   C O N S T A N T -----------------------------------------
	// header field "Content-Type"
	private final static String CTYPE_FILE		= "File";
	private final static String CTYPE_FILEREQ	= "File-Request";
	private final static String CTYPE_FILERES	= "File-Response";
	private final static String CTYPE_FILECFM	= "File-Confirm";

	// header field "File-Request-ID"
	// valid content type: "File", "File-Request", "File-Response"
	private final static String HDR_FILEREQID	= "File-Request-ID";

	// header field "Filename"
	// valid content type: "File-Request"
	private final static String HDR_FILENAME	= "Filename";

	// header field "Filesize"
	// valid content type: "File-Request"
	private final static String HDR_FILESIZE	= "Filesize";

	// header field "File-Response"
	// valid content type: "File-Response"
	private final static String HDR_FILERES		= "File-Response";
	private final static String OK				= "OK";
	private final static String NG				= "NG";

	// header field "File-Confirm"
	// valid content type: "File-Confirm"
	private final static String HDR_FILECFM		= "File-Confirm";

	// M E T H O D S   F O R   O U T G O I N G   T R A N S M I S S I O N ---
	/**
	 * format and output file request
	 * @param sckIn outgoing socket
	 * @param strId file request ID
	 * @param efoIn the associated outgoing file GUI
	 */
	public static void sendFileReq
	(
		Socket sckIn
		, String strId
		, EzimFileOut efoIn
	)
		throws Exception
	{
		if
		(
			efoIn != null
			&& strId != null && strId.length() > 0
		)
		{
			EzimDtxSemantics.initByteArrays();

			long lSize = 0;

			FileInputStream fisTmp = null;

			try
			{
				OutputStream osTmp = sckIn.getOutputStream();
				lSize = efoIn.getFile().length();
				fisTmp = new FileInputStream(efoIn.getFile());

				efoIn.setSize(lSize);

				// create necessary header fields
				Hashtable<String, String> htTmp
					= new Hashtable<String, String>();

				htTmp.put
				(
					EzimDtxSemantics.HDR_CTYPE
					, EzimDtxFileSemantics.CTYPE_FILEREQ
				);
				htTmp.put
				(
					EzimDtxSemantics.HDR_CLEN
					, "0"
				);
				htTmp.put
				(
					EzimDtxFileSemantics.HDR_FILEREQID
					, strId
				);
				htTmp.put
				(
					EzimDtxFileSemantics.HDR_FILENAME
					, efoIn.getFile().getName()
				);
				htTmp.put
				(
					EzimDtxFileSemantics.HDR_FILESIZE
					, Long.toString(lSize)
				);

				// output header in bytes
				EzimDtxSemantics.sendHeaderBytes(sckIn, htTmp);

				// make sure everything is sent
				osTmp.flush();
			}
			finally
			{
				try
				{
					if (fisTmp != null) fisTmp.close();
				}
				catch(Exception e)
				{
					EzimLogger.getInstance().severe(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * format and output file response
	 * @param sckIn outgoing socket
	 * @param strId File-Request-ID to respond to
	 * @param blnRes response (true: OK; false: NG)
	 */
	public static void sendFileRes
	(
		Socket sckIn
		, String strId
		, boolean blnRes
	)
		throws Exception
	{
		if
		(
			strId != null && strId.length() > 0
		)
		{
			EzimDtxSemantics.initByteArrays();

			String strRes = blnRes
				? EzimDtxFileSemantics.OK
				: EzimDtxFileSemantics.NG;

			OutputStream osTmp = sckIn.getOutputStream();

			// create necessary header fields
			Hashtable<String, String> htTmp
				= new Hashtable<String, String>();

			htTmp.put
			(
				EzimDtxSemantics.HDR_CTYPE
				, EzimDtxFileSemantics.CTYPE_FILERES
			);
			htTmp.put
			(
				EzimDtxSemantics.HDR_CLEN
				, "0"
			);
			htTmp.put
			(
				EzimDtxFileSemantics.HDR_FILEREQID
				, strId
			);
			htTmp.put
			(
				EzimDtxFileSemantics.HDR_FILERES
				, strRes
			);

			// output header in bytes
			EzimDtxSemantics.sendHeaderBytes(sckIn, htTmp);

			// make sure everything is sent
			osTmp.flush();
		}
	}

	/**
	 * format and output file confirmation
	 * @param sckIn outgoing socket
	 * @param strId File-Request-ID to respond to
	 * @param blnConfirm confirmation (true: OK; false: NG)
	 */
	public static void sendFileConfirm
	(
		Socket sckIn
		, String strId
		, boolean blnConfirm
	)
		throws Exception
	{
		if
		(
			strId != null && strId.length() > 0
		)
		{
			EzimDtxSemantics.initByteArrays();

			String strConfirm = blnConfirm
				? EzimDtxFileSemantics.OK
				: EzimDtxFileSemantics.NG;

			OutputStream osTmp = sckIn.getOutputStream();

			// create necessary header fields
			Hashtable<String, String> htTmp
				= new Hashtable<String, String>();

			htTmp.put
			(
				EzimDtxSemantics.HDR_CTYPE
				, EzimDtxFileSemantics.CTYPE_FILECFM
			);
			htTmp.put
			(
				EzimDtxSemantics.HDR_CLEN
				, "0"
			);
			htTmp.put
			(
				EzimDtxFileSemantics.HDR_FILEREQID
				, strId
			);
			htTmp.put
			(
				EzimDtxFileSemantics.HDR_FILECFM
				, strConfirm
			);

			// output header in bytes
			EzimDtxSemantics.sendHeaderBytes(sckIn, htTmp);

			// make sure everything is sent
			osTmp.flush();
		}
	}

	/**
	 * format and output file
	 * @param sckIn outgoing socket
	 * @param strId file request ID
	 * @param efoIn the associated outgoing file window
	 */
	public static void sendFile
	(
		Socket sckIn
		, String strId
		, EzimFileOut efoIn
	)
		throws Exception
	{
		if (efoIn != null)
		{
			efoIn.setSocket(sckIn);

			EzimDtxSemantics.initByteArrays();

			byte[] bTmp = new byte[EzimNetwork.dtxBufLen];
			int iTmp = 0;
			long lCnt = 0;
			long lCLen = 0;

			FileInputStream fisTmp = null;

			try
			{
				OutputStream osTmp = sckIn.getOutputStream();
				lCLen = efoIn.getFile().length();
				fisTmp = new FileInputStream(efoIn.getFile());

				// create necessary header fields
				Hashtable<String, String> htTmp
					= new Hashtable<String, String>();

				htTmp.put
				(
					EzimDtxSemantics.HDR_CTYPE
					, EzimDtxFileSemantics.CTYPE_FILE
				);
				htTmp.put
				(
					EzimDtxSemantics.HDR_CLEN
					, Long.toString(lCLen)
				);
				htTmp.put
				(
					EzimDtxFileSemantics.HDR_FILEREQID
					, strId
				);

				// output header in bytes
				EzimDtxSemantics.sendHeaderBytes(sckIn, htTmp);

				efoIn.setSize(lCLen);
				efoIn.setProgressed(lCnt);
				// convert and output file contents in bytes
				while(! ((iTmp = fisTmp.read(bTmp)) < 0))
				{
					osTmp.write(bTmp, 0, iTmp);
					lCnt += iTmp;
					efoIn.setProgressed(lCnt);
				}

				// make sure everything is sent
				osTmp.flush();
			}
			catch(SocketException se)
			{
				// connection closed by remote
			}
			finally
			{
				try
				{
					if (fisTmp != null) fisTmp.close();
				}
				catch(Exception e)
				{
					EzimLogger.getInstance().severe(e.getMessage(), e);
				}

				String strSysMsg = null;

				if (lCnt < lCLen)
					strSysMsg = EzimLang.TransmissionAbortedByRemote;
				else if (lCnt == lCLen)
					strSysMsg = EzimLang.Done;

				efoIn.endProgress(strSysMsg);
			}
		}
	}

	// M E T H O D S   F O R   I N C O M I N G   T R A N S M I S S I O N ---
	/**
	 * retrieve file from incoming socket
	 * @param isIn input stream which streams raw incoming data
	 * @param lCLen length of the file in bytes
	 * @param efiIn the associated incoming file window
	 * @return
	 */
	private static void getFile
	(
		InputStream isIn
		, long lCLen
		, EzimFileIn efiIn
	)
		throws Exception
	{
		FileOutputStream fosTmp = null;
		byte[] bBuf = new byte[EzimNetwork.dtxBufLen];
		int iTmp = 0;
		long lCnt = 0;

		try
		{
			fosTmp = new FileOutputStream(efiIn.getFile());
			efiIn.setSize(lCLen);
			efiIn.setProgressed(lCnt);

			while(! ((iTmp = isIn.read(bBuf)) < 0) && lCnt < lCLen)
			{
				fosTmp.write(bBuf, 0, iTmp);
				lCnt += iTmp;
				efiIn.setProgressed(lCnt);
			}

			fosTmp.flush();
		}
		catch(SocketException se)
		{
			// connection closed by remote
		}
		finally
		{
			if (fosTmp != null) fosTmp.close();

			String strSysMsg = null;

			if (lCnt < lCLen)
				strSysMsg = EzimLang.TransmissionAbortedByRemote;
			else if (lCnt == lCLen)
				strSysMsg = EzimLang.Done;

			efiIn.endProgress(strSysMsg);
		}
	}
	
	protected static void fileParser
	(
		InputStream isData
		, Socket sckIn
		, EzimContact ecIn
		, Hashtable<String, String> htHdr
		, String strCType
		, long lCLen
	) throws Exception
	{
		// receive incoming file
		if (strCType.equals(EzimDtxFileSemantics.CTYPE_FILE))
		{
			String strFileReqId = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILEREQID
			);

			EzimFileIn efiTmp = EzimFrxList.getInstance()
				.get(strFileReqId);

			if (efiTmp != null)
			{
				efiTmp.setSocket(sckIn);

				EzimDtxFileSemantics.getFile
				(
					isData
					, lCLen
					, efiTmp
				);
			}
		}
		// receive incoming file request
		else if (strCType.equals(EzimDtxFileSemantics.CTYPE_FILEREQ))
		{
			String strFilename = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILENAME
			);
			String strFileReqId = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILEREQID
			);
			String strFilesize = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILESIZE
			);

			EzimFileIn efiTmp = new EzimFileIn
			(
				ecIn
				, strFileReqId
				, strFilename
			);

			// this is just a previewed size and may different from
			// the actual one
			efiTmp.setSize(Long.parseLong(strFilesize));
		}
		// receive incoming file confirmation
		else if (strCType.equals(EzimDtxFileSemantics.CTYPE_FILECFM))
		{
			String strFileReqId = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILEREQID
			);
			String strFileCfm = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILECFM
			);

			EzimFileIn efiTmp = EzimFrxList.getInstance()
				.get(strFileReqId);

			if (efiTmp != null)
			{
				if (strFileCfm.equals(EzimDtxFileSemantics.OK))
				{
					efiTmp.setSysMsg(EzimLang.Receiving);
				}
				else
				{
					efiTmp.endProgress
					(
						EzimLang.TransmissionAbortedByRemote
					);
				}
			}
		}
		// receive incoming file response
		else if (strCType.equals(EzimDtxFileSemantics.CTYPE_FILERES))
		{
			String strFileReqId = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILEREQID
			);
			String strFileRes = htHdr.get
			(
				EzimDtxFileSemantics.HDR_FILERES
			);

			EzimFileOut efoTmp = EzimFtxList.getInstance()
				.get(strFileReqId);

			EzimThreadPool etpTmp = EzimThreadPool.getInstance();

			if (efoTmp != null)
			{
				// the remote user has accepted the request
				if (strFileRes.equals(EzimDtxFileSemantics.OK))
				{
					// everything looks fine
					if (efoTmp.getFile().exists())
					{
						efoTmp.setSysMsg(EzimLang.Sending);

						EzimFileConfirmer efcTmp
							= new EzimFileConfirmer
							(
								ecIn.getAddress()
								, ecIn.getPort()
								, strFileReqId
								, true
							);
						etpTmp.execute(efcTmp);

						EzimFileSender efsTmp = new EzimFileSender
						(
							efoTmp
							, ecIn.getAddress()
							, ecIn.getPort()
						);
						etpTmp.execute(efsTmp);
					}
					// the file doesn't exist.  i.e. deleted
					else
					{
						efoTmp.endProgress
						(
							EzimLang.FileNotFoundTransmissionAborted
						);

						EzimFileConfirmer efcTmp
							= new EzimFileConfirmer
							(
								ecIn.getAddress()
								, ecIn.getPort()
								, strFileReqId
								, false
							);
						etpTmp.execute(efcTmp);
					}
				}
				// the remote user has refused the request
				else
				{
					efoTmp.endProgress(EzimLang.RefusedByRemote);
				}
			}
			// the outgoing file window is closed
			else if (strFileRes.equals(EzimDtxFileSemantics.OK))
			{
				EzimFileConfirmer efcTmp = new EzimFileConfirmer
				(
					ecIn.getAddress()
					, ecIn.getPort()
					, strFileReqId
					, false
				);
				etpTmp.execute(efcTmp);
			}
		}
	}
}
