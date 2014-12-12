/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * BufferPrinter1_4.java - Main class that controls printing
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001 Slava Pestov
 * Portions copyright (C) 2002 Thomas Dilts
 * Modified W.Pasman 25feb09 to fit GOAL IDE as some needed functionality is private.
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

//package org.gjt.sp.jedit.print;
package nl.tudelft.goal.SimpleIDE;

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.ViewSubstitute;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.util.Log;

/**
 * Apparently this is a utility class for ModifiedBufferPrintable
 * 
 */
public class ModifiedBufferPrinter1_4 {

	/**
	 * This apparently is a utility class. Hide the constructor.
	 */
	private ModifiedBufferPrinter1_4() {
	}

	private static PrinterJob getPrintJob(String jobName) {
		job = PrinterJob.getPrinterJob();

		format = new HashPrintRequestAttributeSet();

		String settings = ".jedit"; //$NON-NLS-1$
		jEdit.getSettingsDirectory();
		if (settings != null) {
			String printSpecPath = MiscUtilities.constructPath(settings,
					"printspec"); //$NON-NLS-1$
			File filePrintSpec = new File(printSpecPath);

			if (filePrintSpec.exists()) {
				try {
					FileInputStream fileIn = new FileInputStream(filePrintSpec);
					ObjectInputStream obIn = new ObjectInputStream(fileIn);
					format = (HashPrintRequestAttributeSet) obIn.readObject();
					obIn.close();
				} catch (Exception e) {
					Log.log(Log.ERROR, ModifiedBufferPrinter1_4.class, e);
				}
				// for backwards compatibility, the color variable is stored
				// also as a property
				// disabled, getBooleanProperty seems not to work as
				// expected, probably because we don't have full jEdit here.

				// if(jEdit.getBooleanProperty("print.color"))
				// #trac 636
				Chromaticity cc = (Chromaticity) format.get(Chromaticity.class);
				if (cc != null) {
					jEdit.setBooleanProperty("print.color", //$NON-NLS-1$
							cc.getValue() == Chromaticity.COLOR.getValue());
				} else {
					// default if no value was set before.
					jEdit.setBooleanProperty("print.color", true); //$NON-NLS-1$
				}
				// no need to always keep the same job label for every printout.
				format.add(new JobName(jobName, null));
			}
		}

		return job;
	} // }}}

	/**
	 * DOC
	 * 
	 * @param view
	 */
	public static void pageSetup(View view) {
		PrinterJob prnJob = getPrintJob("PageSetupOnly"); //$NON-NLS-1$
		if (prnJob.pageDialog(format) != null) {
			savePrintSpec();
		}
	}

	// {{{ print() method
	public static void print(final ViewSubstitute view,
			final JEditBuffer buffer, boolean selection) {
		job = getPrintJob(buffer.getPath());

		boolean header = jEdit.getBooleanProperty("print.header"); //$NON-NLS-1$
		boolean footer = jEdit.getBooleanProperty("print.footer"); //$NON-NLS-1$
		boolean lineNumbers = jEdit.getBooleanProperty("print.lineNumbers"); //$NON-NLS-1$
		boolean color = jEdit.getBooleanProperty("print.color"); //$NON-NLS-1$

		Font font = jEdit.getFontProperty("print.font"); //$NON-NLS-1$

		ModifiedBufferPrintable printable = new ModifiedBufferPrintable(job,
				format, view, buffer, font, header, footer, lineNumbers, color);
		job.setPrintable(printable);

		if (!job.printDialog(format)) {
			return;
		}

		savePrintSpec();

		printable.print();
	} // }}}

	// {{{ getPageFormat() method
	public static PageFormat getPageFormat() {
		// convert from PrintRequestAttributeSet to the pageFormat
		PrinterJob prnJob = getPrintJob(" "); //$NON-NLS-1$
		PageFormat pf = prnJob.defaultPage();
		Paper pap = pf.getPaper();

		MediaSizeName media = (MediaSizeName) format.get(Media.class);
		MediaSize ms = MediaSize.getMediaSizeForName(media);

		MediaPrintableArea mediaarea = (MediaPrintableArea) format
				.get(MediaPrintableArea.class);
		if (mediaarea != null) {
			pap.setImageableArea(
					(mediaarea.getX(MediaPrintableArea.INCH) * 72),
					(mediaarea.getY(MediaPrintableArea.INCH) * 72),
					(mediaarea.getWidth(MediaPrintableArea.INCH) * 72),
					(mediaarea.getHeight(MediaPrintableArea.INCH) * 72));
		}
		if (ms != null) {
			pap.setSize((ms.getX(Size2DSyntax.INCH) * 72),
					(ms.getY(Size2DSyntax.INCH) * 72));
		}
		pf.setPaper(pap);

		OrientationRequested orientation = (OrientationRequested) format
				.get(OrientationRequested.class);
		if (orientation != null) {
			if (orientation.getValue() == OrientationRequested.LANDSCAPE
					.getValue()) {
				pf.setOrientation(PageFormat.LANDSCAPE);
			} else if (orientation.getValue() == OrientationRequested.REVERSE_LANDSCAPE
					.getValue()) {
				pf.setOrientation(PageFormat.REVERSE_LANDSCAPE);
			} else if (orientation.getValue() == OrientationRequested.PORTRAIT
					.getValue()) {
				pf.setOrientation(PageFormat.PORTRAIT);
			} else if (orientation.getValue() == OrientationRequested.REVERSE_PORTRAIT
					.getValue()) {
				// doesnt exist??
				// pf.setOrientation(PageFormat.REVERSE_PORTRAIT);
				// then just do the next best thing
				pf.setOrientation(PageFormat.PORTRAIT);
			}
		}
		return pf;
	} // }}}

	// {{{ savePrintSpec() method
	private static void savePrintSpec() {
		// String settings = jEdit.getSettingsDirectory();
		// if(settings == null)
		// return;
		String settings = ".jedit"; //$NON-NLS-1$

		String printSpecPath = MiscUtilities.constructPath(settings,
				"printspec"); //$NON-NLS-1$
		File filePrintSpec = new File(printSpecPath);
		ObjectOutputStream obOut = null;
		try {
			FileOutputStream fileOut = new FileOutputStream(filePrintSpec);
			obOut = new ObjectOutputStream(fileOut);
			obOut.writeObject(format);
			// for backwards compatibility, the color variable is stored also as
			// a property
			Chromaticity cc = (Chromaticity) format.get(Chromaticity.class);
			if (cc != null) {
				jEdit.setBooleanProperty("print.color", //$NON-NLS-1$
						cc.getValue() == Chromaticity.COLOR.getValue());
			}
		} catch (Exception e) {
			new Warning(
					Resources.get(WarningStrings.FAILED_PRINT_SETTINGS_SAVE), e);
		} finally {
			if (obOut != null) {
				try {
					obOut.close();
				} catch (IOException e) {
					new Warning(
							Resources
									.get(WarningStrings.FAILED_OUTSTREAM_CLOSE),
							e);
				}
			}
		}
	}

	// }}}

	// {{{ Private members
	private static PrintRequestAttributeSet format;
	private static PrinterJob job;
	// }}}
}
