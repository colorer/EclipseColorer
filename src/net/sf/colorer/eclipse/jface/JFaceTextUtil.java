package net.sf.colorer.eclipse.jface;
/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;


/**
 * This class is here to help transitioning to the
 * new StyledText APIs.
 * 
 * @since 3.2
 */
public final class JFaceTextUtil {
    
    private JFaceTextUtil() {
        // Do not instantiate
    }
    

    /**
     * Computes the line height for the given line range.
     * 
     * @param textWidget the <code>StyledText</code> widget
     * @param startLine the start line
     * @param endLine the end line (exclusive)
     * @param lineCount the line count used by the old API
     * @return the height of all lines starting with <code>startLine</code> and ending above <code>endLime</code>
     */
    public static int computeLineHeight(StyledText textWidget, int startLine, int endLine, int lineCount) {
        return getLinePixel(textWidget, endLine) - getLinePixel(textWidget, startLine);
    }
    
    /**
     * Returns the last fully visible line of the widget. The exact semantics of "last fully visible
     * line" are:
     * <ul>
     * <li>the last line of which the last pixel is visible, if any
     * <li>otherwise, the only line that is partially visible
     * </ul>
     * 
     * @param widget the widget
     * @return the last fully visible line
     */
    public static int getBottomIndex(StyledText widget) {
        int lastPixel= computeLastVisiblePixel(widget);
        
        // bottom is in [0 .. lineCount - 1]
        int bottom= widget.getLineIndex(lastPixel);

        // bottom is the first line - no more checking
        if (bottom == 0)
            return bottom;
        
        int pixel= widget.getLinePixel(bottom);
        // bottom starts on or before the client area start - bottom is the only visible line
        if (pixel <= 0)
            return bottom;
        
        int offset= widget.getOffsetAtLine(bottom);
        int height= widget.getLineHeight(offset);
        
        // bottom is not showing entirely - use the previous line
        if (pixel + height - 1 > lastPixel)
            return bottom - 1;
        
        // bottom is fully visible and its last line is exactly the last pixel
        return bottom;
    }

    /**
     * Returns the index of the first (possibly only partially) visible line of the widget
     * 
     * @param widget the widget
     * @return the index of the first line of which a pixel is visible
     */
    public static int getPartialTopIndex(StyledText widget) {
        // see StyledText#getPartialTopIndex()
        int top= widget.getTopIndex();
        int pixels= widget.getLinePixel(top);
        
        // FIXME remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=123770 is fixed
        if (pixels == -widget.getLineHeight(widget.getOffsetAtLine(top))) {
            top++;
            pixels= 0;
        }

        if (pixels > 0)
            top--;
        
        return top;
    }

    /**
     * Returns the index of the last (possibly only partially) visible line of the widget
     * 
     * @param widget the text widget
     * @return the index of the last line of which a pixel is visible
     */
    public static int getPartialBottomIndex(StyledText widget) {
        // @see StyledText#getPartialBottomIndex()
        int lastPixel= computeLastVisiblePixel(widget);
        int bottom= widget.getLineIndex(lastPixel);
        return bottom;
    }

    /**
     * Returns the last visible pixel in the widget's client area.
     * 
     * @param widget the widget
     * @return the last visible pixel in the widget's client area
     */
    private static int computeLastVisiblePixel(StyledText widget) {
        int caHeight= widget.getClientArea().height;
        int lastPixel= caHeight - 1;
        // XXX what if there is a margin? can't take trim as this includes the scrollbars which are not part of the client area
//      if ((textWidget.getStyle() & SWT.BORDER) != 0)
//          lastPixel -= 4;
        return lastPixel;
    }
    
    /**
     * Returns the line index of the first visible model line in the viewer. The line may be only
     * partially visible.
     * 
     * @param viewer the text viewer
     * @return the first line of which a pixel is visible, or -1 for no line
     */
    public static int getPartialTopIndex(ITextViewer viewer) {
        StyledText widget= viewer.getTextWidget();
        int widgetTop= getPartialTopIndex(widget);
        return widgetLine2ModelLine(viewer, widgetTop);
    }
    
    /**
     * Returns the the last, possibly partially, visible line in the view port.
     *
     * @param viewer the text viewer
     * @return the last, possibly partially, visible line in the view port
     */
    public static int getPartialBottomIndex(ITextViewer viewer) {
        StyledText textWidget= viewer.getTextWidget();
        int widgetBottom= getPartialBottomIndex(textWidget);
        return widgetLine2ModelLine(viewer, widgetBottom);
    }

    /**
     * Returns the range of lines that is visible in the viewer, including any partially visible
     * lines.
     * 
     * @param viewer the viewer
     * @return the range of lines that is visible in the viewer, <code>null</code> if no lines are
     *         visible
     */
    public static ILineRange getVisibleModelLines(ITextViewer viewer) {
        int top= getPartialTopIndex(viewer);
        int bottom= getPartialBottomIndex(viewer);
        if (top == -1 || bottom == -1)
            return null;
        return new LineRange(top, bottom - top + 1);
    }

    /**
     * Converts a widget line into a model (i.e. {@link IDocument}) line using the
     * {@link ITextViewerExtension5} if available, otherwise by adapting the widget line to the
     * viewer's {@link ITextViewer#getVisibleRegion() visible region}.
     * 
     * @param viewer the viewer
     * @param widgetLine the widget line to convert.
     * @return the model line corresponding to <code>widgetLine</code> or -1 to signal that there
     *         is no corresponding model line
     */
    public static int widgetLine2ModelLine(ITextViewer viewer, int widgetLine) {
        int modelLine;
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
            modelLine= extension.widgetLine2ModelLine(widgetLine);
        } else {
            try {
                IRegion r= viewer.getVisibleRegion();
                IDocument d= viewer.getDocument();
                modelLine= widgetLine + d.getLineOfOffset(r.getOffset());
            } catch (BadLocationException x) {
                modelLine= widgetLine;
            }
        }
        return modelLine;
    }
    
    /**
     * Converts a model (i.e. {@link IDocument}) line into a widget line using the
     * {@link ITextViewerExtension5} if available, otherwise by adapting the model line to the
     * viewer's {@link ITextViewer#getVisibleRegion() visible region}.
     * 
     * @param viewer the viewer
     * @param modelLine the model line to convert.
     * @return the widget line corresponding to <code>modelLine</code> or -1 to signal that there
     *         is no corresponding widget line
     */
    public static int modelLineToWidgetLine(ITextViewer viewer, final int modelLine) {
        int widgetLine;
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
            widgetLine= extension.modelLine2WidgetLine(modelLine);
        } else {
            IRegion region= viewer.getVisibleRegion();
            IDocument document= viewer.getDocument();
            try {
                int visibleStartLine= document.getLineOfOffset(region.getOffset());
                int visibleEndLine= document.getLineOfOffset(region.getOffset() + region.getLength());
                if (modelLine < visibleStartLine || modelLine > visibleEndLine)
                    widgetLine= -1;
                else
                widgetLine= modelLine - visibleStartLine;
            } catch (BadLocationException x) {
                // ignore and return -1
                widgetLine= -1;
            }
        }
        return widgetLine;
    }


    /**
     * Returns the number of hidden pixels of the first partially visible line. If there is no
     * partially visible line, zero is returned.
     * 
     * @param textWidget the widget
     * @return the number of hidden pixels of the first partial line, always &gt;= 0
     */
    public static int getHiddenTopLinePixels(StyledText textWidget) {
        int top= getPartialTopIndex(textWidget);
        return -textWidget.getLinePixel(top);
    }
    
    /**
     * Returns the number of lines in the view port.
     * 
     * @param textWidget 
     * @return the number of lines visible in the view port <code>-1</code> if there's no client area
     * @deprecated this method should not be used - it relies on the widget using a uniform line height
     */
    public static int getVisibleLinesInViewport(StyledText textWidget) {
        if (textWidget != null) {
            Rectangle clArea= textWidget.getClientArea();
            if (!clArea.isEmpty()) {
                int firstPixel= 0;
                int lastPixel= clArea.height - 1; // XXX what about margins? don't take trims as they include scrollbars
                int first= getLineIndex(textWidget, firstPixel);
                int last= getLineIndex(textWidget, lastPixel);
                return last - first;
            }
        }
        return -1;
    }

    /*
     * @see StyledText#getLinePixel(int)
     */
    public static int getLinePixel(StyledText textWidget, int line) {
        return textWidget.getLinePixel(line);
    }
    
    /*
     * @see StyledText#getLineIndex(int)
     */
    public static int getLineIndex(StyledText textWidget, int y) {
        int lineIndex= textWidget.getLineIndex(y);
        return lineIndex;
    }

    /**
     * Returns <code>true</code> if the widget displays the entire contents, i.e. it cannot
     * be vertically scrolled.
     * 
     * @param widget the widget 
     * @return <code>true</code> if the widget displays the entire contents, i.e. it cannot
     *         be vertically scrolled, <code>false</code> otherwise
     */
    public static boolean isShowingEntireContents(StyledText widget) {
        if (widget.getTopPixel() != 0) // more efficient shortcut
            return false;
        
        int lastVisiblePixel= computeLastVisiblePixel(widget);
        int lastPossiblePixel= widget.getLinePixel(widget.getLineCount());
        return lastPossiblePixel <= lastVisiblePixel;
    }

}


/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Colorer Library
 *
 * The Initial Developer of the Original Code is
 * Igor Russkih <irusskih at gmail dot com>.
 * Portions created by the Initial Developer are Copyright (C) 1999-2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */