/*
 * StandaloneIO, to support StandAlone load/save functionality.
 * @author W.Pasman 4dec08
 * 
 */
package org.gjt.sp.jedit.textarea;

import java.io.BufferedOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;

import javax.swing.text.Segment;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.bufferio.BufferIORequest;
import org.gjt.sp.jedit.io.Encoding;
import org.gjt.sp.jedit.io.EncodingServer;

/**
 * jEdit's standalone IO support.
 * @author W.Pasman
 * See also JEditBuffer.java and org.gjt.sp.jedit.bufferio.BufferIORequest.java
 */
public class StandaloneIO
{

	public static int getByteIOBufferSize()
	{
		// 2 is sizeof char in byte;
		return BufferIORequest.IOBUFSIZE * 2;
	}
	
	/**
	 * Wouter: copied from BufferIORequest. Still not sure whether we
	 * should really work this way (a.o. respect character Encoding)
	 * or just dump the buffer contents to a file like we do when reading.
	 * 
	 * @param buffer is the JEditBuffer to be written to the out stream
	 * @param out is the OutputStream to be written to.
	 * @throws IOException
	 */
	public static void write(JEditBuffer buffer, OutputStream out)
	throws IOException
	{
		String encodingName
			= buffer.getStringProperty(JEditBuffer.ENCODING);
		Encoding encoding = EncodingServer.getEncoding(encodingName);
		Writer writer = encoding.getTextWriter(
			new BufferedOutputStream(out, getByteIOBufferSize()));
	
		Segment lineSegment = new Segment();
		String newline = buffer.getStringProperty(JEditBuffer.LINESEP);
		if(newline == null)
			newline = System.getProperty("line.separator");
	
		final int bufferLineCount = buffer.getLineCount();
		//Wouter: progress reporting disabled....
		//setMaximum(bufferLineCount / BufferIORequest.PROGRESS_INTERVAL);
		//setValue(0);
	
		int i = 0;
		while(i < bufferLineCount)
		{
			buffer.getLineText(i,lineSegment);
			try
			{
				writer.write(lineSegment.array,
					lineSegment.offset,
					lineSegment.count);
				if(i < bufferLineCount - 1
					|| (jEdit.getBooleanProperty("stripTrailingEOL")
						&& buffer.getBooleanProperty(Buffer.TRAILING_EOL)))
				{
					writer.write(newline);
				}
			}
			catch(CharacterCodingException e)
			{
				String message = getWriteEncodingErrorMessage(
					encodingName, encoding,
					lineSegment, i);
				IOException wrapping = new CharConversionException(message);
				wrapping.initCause(e);
				throw wrapping;
			}
	
			// Wouter: progress reporting disabled.
			//if(++i % BufferIORequest.PROGRESS_INTERVAL == 0)
			//	setValue(i / BufferIORequest.PROGRESS_INTERVAL);
		}
		writer.flush();
	} 

	private static String getWriteEncodingErrorMessage(
			String encodingName, Encoding encoding,
			Segment line, int lineIndex)
		{
			String args[] = {
				encodingName,
				Integer.toString(lineIndex + 1),
				"UNKNOWN", // column
				"UNKNOWN"  // the character
			};
			try
			{
				int charIndex = getFirstGuiltyCharacterIndex(encoding, line);
				if(0 <= charIndex && charIndex < line.count)
				{
					char c = line.array[line.offset + charIndex];
					args[2] = Integer.toString(charIndex + 1);
					args[3] = "'" + c + "' (U+" + Integer.toHexString(c).toUpperCase() + ")";
				}
			}
			catch(Exception e)
			{
				// Ignore.
			}
			return jEdit.getProperty("ioerror.write-encoding-error", args);
		}
	
	private static int getFirstGuiltyCharacterIndex(Encoding encoding,
			Segment line) throws IOException
		{
			if(line.count < 1)
			{
				return -1;
			}
			else if(line.count == 1)
			{
				return 0;
			}

			Writer tester = encoding.getTextWriter(
				new OutputStream() {
					public void write(int b) {}
				});
			for(int i = 0; i < line.count; ++i)
			{
				try
				{
					tester.write(line.array[line.offset + i]);
				}
				catch(CharacterCodingException e)
				{
					return i;
				}
			}
			return -1;
		}
}



