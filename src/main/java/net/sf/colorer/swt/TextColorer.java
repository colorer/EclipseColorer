package net.sf.colorer.swt;

import java.util.Vector;

import net.sf.colorer.FileType;
import net.sf.colorer.LineSource;
import net.sf.colorer.ParserFactory;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.PairMatch;
import net.sf.colorer.handlers.LineRegion;
import net.sf.colorer.handlers.RegionDefine;
import net.sf.colorer.handlers.RegionMapper;
import net.sf.colorer.handlers.StyledRegion;
import net.sf.colorer.impl.BaseEditorNative;
import net.sf.colorer.impl.Logger;

import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * StyledText listener implementation with syntax highlighting support using
 * Colorer library. <a href='http://colorer.sf.net/'>http://colorer.sf.net/</a>
 */
public class TextColorer {

    public final static int HLS_NONE = 0;

    public final static int HLS_XOR = 1;

    public final static int HLS_OUTLINE = 2;

    public final static int HLS_OUTLINE2 = 3;

    private ColorManager cm;

    ParserFactory pf = null;

    BaseEditor baseEditor = null;

    StyledText text = null;

    boolean fullBackground = false;

    boolean vertCross = false;

    boolean horzCross = false;

    RegionDefine vertCrossColor = null;

    RegionDefine horzCrossColor = null;

    int highlightStyle = HLS_XOR;

    PairMatch currentPair = null;

    int prevLine = 0;

    int visibleStart, visibleEnd;

    boolean lineHighlighting = true;

    boolean pairsHighlighting = true;

    boolean backParserDelay = false;

    InternalHandler ml = new InternalHandler();

    /**
     * Common TextColorer creation constructor. Creates TextColorer object,
     * which is to be attached to the StyledText widget.
     * 
     * @param pf
     *            Parser factory, used to create all coloring text parsers.
     * @param cm
     *            Color Manager, used to store cached color objects
     */
    public TextColorer(ParserFactory pf, ColorManager cm) {
        this.pf = pf;
        this.cm = cm;

        setFullBackground(false);
        setCross(false, false);

        baseEditor = new BaseEditorNative(pf, new LineSource() {
            public String getLine(int lno) {
                if (text.getContent().getLineCount() <= lno)
                    return null;
                String line = text.getContent().getLine(lno);
                return line;
            }
        });
        baseEditor.setRegionCompact(true);
    }

    /**
     * Installs this highlighter into the specified StyledText object. Client
     * can manually call detach() method, then wants to destroy this object.
     */
    public void attach(StyledText parent) {
        if (baseEditor == null) {
            throw new RuntimeException("Attach after detach");
        }
        text = parent;
        text.addDisposeListener(ml);
        text.addLineStyleListener(ml);
        text.addLineBackgroundListener(ml);
        text.addPaintListener(ml);
        text.addVerifyListener(ml);
        text.addControlListener(ml);
        text.addKeyListener(ml);
        text.addTraverseListener(ml);
        text.addMouseListener(ml);
        text.addSelectionListener(ml);
        text.getContent().addTextChangeListener(ml);
        ScrollBar sb = text.getVerticalBar();
        if (sb != null)
            sb.addSelectionListener(ml);
        updateViewport();

        new Thread() {
            public void run() {
                // setPriority(Thread.NORM_PRIORITY-1);
                while (true) {
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                    }
                    if (baseEditor == null || text == null)
                        break;
                    if (backParserDelay) {
                        backParserDelay = false;
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    ;
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            if (baseEditor == null || text == null)
                                return;
                            if (text.isDisposed())
                                return;
                            // System.out.println(System.currentTimeMillis());
                            baseEditor.idleJob(80);
                            // redrawFrom(text.getLineAtOffset(text.getCaretOffset()));
                        }
                    });
                }
                ;
            };
        }.start();
    }

    /**
     * Removes this object from the corresponding StyledText widget. Object
     * can't be used after this call, until another attach. This method is
     * called automatically, when StyledText widget is disposed
     */
    public void detach() {
        if (text == null)
            return;
        text.removeDisposeListener(ml);
        text.removeLineStyleListener(ml);
        text.removeLineBackgroundListener(ml);
        text.removePaintListener(ml);
        text.removeVerifyListener(ml);
        text.removeControlListener(ml);
        text.removeKeyListener(ml);
        text.removeTraverseListener(ml);
        text.removeMouseListener(ml);
        text.removeSelectionListener(ml);
        text.getContent().removeTextChangeListener(ml);
        ScrollBar sb = text.getVerticalBar();
        if (sb != null)
            sb.removeSelectionListener(ml);
        baseEditor.dispose();
        baseEditor = null;
    }

    void checkActive() {
        if (text == null) {
            throw new RuntimeException("Object is not attached to StyledText");
        }
    }

    /**
     * Selects and installs coloring style (filetype) according to filename
     * string and current first line of text.
     * 
     * @param filename
     *            File name to be used to autodetect filetype
     */
    public FileType chooseFileType(String filename) {
        checkActive();
        return baseEditor.chooseFileType(filename);
    }

    /**
     * Selects and installs specified file type.
     * 
     * @param typename
     *            Name or description of HRC filetype.
     */
    public void setFileType(FileType typename) {
        checkActive();
        baseEditor.setFileType(typename);
    }

    /**
     * Returns currently used file type.
     */
    public FileType getFileType() {
        checkActive();
        return baseEditor.getFileType();
    }

    /**
     * Returns currently used ParserFactory object
     */
    public ParserFactory getParserFactory() {
        return pf;
    }

    /**
     * Returns current low-level BaseEditor object implementation
     */
    public BaseEditor getBaseEditor() {
        return baseEditor;
    }

    /**
     * Changes style/coloring scheme into the specified.
     * 
     * @param regionMapper
     *            External RegionMapper object
     * @param useBackground
     *            If true, native HRD background properties would be assigned to
     *            colored StyledText.
     */
    public void setRegionMapper(RegionMapper regionMapper, boolean useBackground) {
        baseEditor.setRegionMapper(regionMapper);

        StyledRegion sr = (StyledRegion) baseEditor.getBackground();
        text.setForeground(null);
        text.setBackground(null);
        if (useBackground) {
            text.setForeground(cm.getColor(sr.bfore, sr.fore));
            text.setBackground(cm.getColor(sr.bback, sr.back));
        }
        ;
        setCross(vertCross, horzCross);
    }

    /**
     * Changes style/coloring scheme into the specified.
     * 
     * @param name
     *            Name of color scheme (HRD name).
     * @param useBackground
     *            If true, native HRD background properties would be assigned to
     *            colored StyledText.
     */
    public void setRegionMapper(String hrdName, boolean useBackground) {
        baseEditor.setRegionMapper(StyledRegion.HRD_RGB_CLASS, hrdName);

        StyledRegion sr = (StyledRegion) baseEditor.getBackground();
        text.setForeground(null);
        text.setBackground(null);
        if (useBackground) {
            text.setForeground(cm.getColor(sr.bfore, sr.fore));
            text.setBackground(cm.getColor(sr.bback, sr.back));
        }
        ;
        setCross(vertCross, horzCross);
    }

    /**
     * Inlined languages background coloring.
     * 
     * @param full
     *            If true, background color of other language insertions (jsp,
     *            php) would be painted till end of line. If false, only text
     *            will be painted with marked color.
     */
    public void setFullBackground(boolean full) {
        fullBackground = full;
    }

    /**
     * Specifies visibility of cross at the cursor position.
     * 
     * @param vert
     *            Not used
     */
    public void setCross(boolean horz, boolean vert) {
        horzCross = horz;
        vertCross = vert;
        vertCrossColor = null;
        horzCrossColor = null;
        if (horzCross)
            horzCrossColor = baseEditor.getHorzCross();
        if (vertCross)
            vertCrossColor = baseEditor.getVertCross();
    }

    /**
     * Paint paired constructions or not.
     * 
     * @param paint
     *            Paint Matched pairs or not.
     * @param style
     *            One of TextColorer.HLS_XOR, TextColorer.HLS_OUTLINE or
     *            TextColorer.HLS_OUTLINE2
     */
    public void setPairsPainter(boolean paint, int style) {
        highlightStyle = style;
        if (!paint)
            highlightStyle = HLS_NONE;
    }

    /** Checks if caret positioned on highlighted pair. */
    public boolean pairAvailable() {
        return currentPair != null;
    }

    /** Moves caret to the position of currently active pair. */
    public boolean matchPair() {
        if (currentPair == null)
            return false;
        int caret = text.getCaretOffset();
        int lno = text.getLineAtOffset(caret);
        PairMatch cp = baseEditor.getPairMatch(lno, caret
                - text.getOffsetAtLine(lno));
        baseEditor.searchGlobalPair(cp);
        if (cp.end == null)
            return false;
        if (cp.topPosition)
            text.setSelection(text.getOffsetAtLine(cp.eline) + cp.end.end);
        else
            text.setSelection(text.getOffsetAtLine(cp.eline) + cp.end.start);
        return true;
    }

    /** Selects a content of the currently positioned pair. */
    public boolean selectPair() {
        if (currentPair == null)
            return false;
        int caret = text.getCaretOffset();
        int lno = text.getLineAtOffset(caret);
        PairMatch cp = baseEditor.getPairMatch(lno, caret
                - text.getOffsetAtLine(lno));
        baseEditor.searchGlobalPair(cp);
        if (cp.end == null)
            return false;
        if (cp.topPosition)
            text.setSelection(text.getOffsetAtLine(cp.sline) + cp.start.start,
                    text.getOffsetAtLine(cp.eline) + cp.end.end);
        else
            text.setSelection(text.getOffsetAtLine(cp.eline) + cp.end.start,
                    text.getOffsetAtLine(cp.sline) + cp.start.end);
        return true;
    }

    /** Selects an internal part of the currently selected paired content */
    public boolean selectContentPair() {
        if (currentPair == null)
            return false;
        int caret = text.getCaretOffset();
        int lno = text.getLineAtOffset(caret);
        PairMatch cp = baseEditor.getPairMatch(lno, caret
                - text.getOffsetAtLine(lno));
        baseEditor.searchGlobalPair(cp);
        if (cp.end == null)
            return false;
        if (cp.topPosition)
            text.setSelection(text.getOffsetAtLine(cp.sline) + cp.start.end,
                    text.getOffsetAtLine(cp.eline) + cp.end.start);
        else
            text.setSelection(text.getOffsetAtLine(cp.eline) + cp.end.end, text
                    .getOffsetAtLine(cp.sline)
                    + cp.start.start);
        return true;
    }

    /**
     * Returns visible text start line
     */
    public int getVisibleStart() {
        return visibleStart;
    }

    /**
     * Returns visible text end line
     */
    public int getVisibleEnd() {
        return visibleEnd;
    }

    /**
     * Retrieves current LineRegion under caret.
     * 
     * @return LineRegion currently under Caret
     */
    public LineRegion getCaretRegion() {
        LineRegion caretRegion = null;
        int caret = text.getCaretOffset();
        int lno = text.getLineAtOffset(caret);
        int linepos = caret - text.getOffsetAtLine(lno);

        LineRegion[] arr = baseEditor.getLineRegions(lno);
        if (arr == null) {
            return null;
        }
        for (int idx = 0; idx < arr.length; idx++) {
            if (arr[idx].start <= linepos && arr[idx].end > linepos
                    && !arr[idx].special) {
                caretRegion = arr[idx];
            }
        }
        return caretRegion;
    }

    /**
     * Informs colorer about visible state change of the editor
     */
    public void stateChanged() {
        backParserDelay = true;
        int curLine = text.getLineAtOffset(text.getCaretOffset());
        if (lineHighlighting && text.getSelectionRange().y != 0) {
            lineHighlighting = false;
            drawLine(prevLine);
            pairsHighlighting = false;
            pairsDraw(null, currentPair);
            return;
        }
        if (text.getSelectionRange().y != 0) {
            return;
        }
        /* do not analize pairs if cursor is out of visible area */
        if (curLine < visibleStart || curLine > visibleEnd) {
            return;
        }
        if (!lineHighlighting) {
            // drawing current line
            lineHighlighting = true;
            drawLine(curLine);
        } else if (curLine != prevLine) {
            drawLine(prevLine);
            drawLine(curLine);
            prevLine = curLine;
        }
        // drawing current pairs
        if (!pairsHighlighting) {
            pairsHighlighting = true;
            pairsDraw(null, currentPair);
        } else {
            int lineOffset = text.getOffsetAtLine(curLine);
            PairMatch newmatch = baseEditor.getPairMatch(curLine, text
                    .getCaretOffset()
                    - lineOffset);
            if (newmatch != null)
                baseEditor.searchLocalPair(newmatch);
            if ((newmatch == null && currentPair != null)
                    || (newmatch != null && !newmatch.equals(currentPair))) {
                pairsDraw(null, currentPair);
                pairsDraw(null, newmatch);
            }
            currentPair = newmatch;
        }
    }

    /**
     * Tells parser that there were some modifications in source text. Causes
     * parser to reparse text again
     * 
     * @param lno
     *            Modified line number
     */
    public void modifyEvent(int lno) {
        checkActive();
        updateViewport();
        baseEditor.modifyEvent(lno);
        redrawFrom(lno);
        stateChanged();
    }

    void updateViewport() {
        checkActive();
        baseEditor.lineCountEvent(text.getLineCount());
        visibleStart = 0;
        try {
            visibleStart = text.getTopIndex() - 1;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        if (visibleStart < 0) visibleStart = 0;
        visibleEnd = visibleStart + text.getClientArea().height / text.getLineHeight();
        int lc = text.getLineCount();
        if (visibleEnd > lc) visibleEnd = lc;

        baseEditor.visibleTextEvent(visibleStart, visibleEnd - visibleStart + 2);
    }

    void pairDraw(GC gc, StyledRegion sr, int start, int end) {
        if (start > text.getCharCount() || end > text.getCharCount())
            return;
        if (gc != null) {
            Point left = text.getLocationAtOffset(start);
            Point right = text.getLocationAtOffset(end);
            if (sr != null) {
                if (highlightStyle == HLS_XOR) {
                    int resultColor = sr.fore
                            ^ cm.getColor(text.getBackground());
                    if (text.getLineAtOffset(text.getCaretOffset()) == text.getLineAtOffset(start)
                            && horzCross
                            && horzCrossColor != null
                            && ((StyledRegion) horzCrossColor).bback)
                        resultColor = sr.fore ^ ((StyledRegion) horzCrossColor).back;
                    Color color = cm.getColor(sr.bfore, resultColor);
                    gc.setBackground(color);
                    gc.setXORMode(true);
                    gc.fillRectangle(left.x, left.y, right.x - left.x, gc.getFontMetrics().getHeight());
                } else if (highlightStyle == HLS_OUTLINE) {
                    Color color = cm.getColor(sr.bfore, sr.fore);
                    gc.setForeground(color);
                    gc.drawRectangle(left.x, left.y, right.x - left.x - 1, gc.getFontMetrics().getHeight() - 1);
                } else if (highlightStyle == HLS_OUTLINE2) {
                    Color color = cm.getColor(sr.bfore, sr.fore);
                    gc.setForeground(color);
                    gc.setLineWidth(2);
                    gc.drawRectangle(left.x + 1, left.y + 1, right.x - left.x - 2, gc.getFontMetrics().getHeight() - 2);
                }
            }
        } else {
            text.redrawRange(start, end - start, true);
        }
    }

    void pairsDraw(GC gc, PairMatch pm) {
        if (pm == null)
            return;
        if (pm.start != null) {
            /* Do not draw pairs if currently invisible */
            if (pm.sline < visibleStart || pm.sline > visibleEnd) return;

            int lineOffset = text.getOffsetAtLine(pm.sline);
            pairDraw(gc, (StyledRegion) pm.start.rdef, pm.start.start
                    + lineOffset, pm.start.end + lineOffset);
        }
        if (pm.end != null) {
            if (pm.eline < visibleStart || pm.eline > visibleEnd) return;

            int lineOffset = text.getOffsetAtLine(pm.eline);
            pairDraw(gc, (StyledRegion) pm.end.rdef, pm.end.start + lineOffset,
                    pm.end.end + lineOffset);
        }
    }

    void redrawFrom(int lno) {
        if (lno < 0 || lno >= text.getLineCount())
            return;
        int y = text.getLocationAtOffset(text.getOffsetAtLine(lno)).y;
        int height = text.getClientArea().height - y;
        int width = text.getClientArea().width + text.getHorizontalPixel();
        text.redraw(0, y, width, height, false);
    }

    void drawLine(int lno) {
        if (lno < 0 || lno >= text.getLineCount())
            return;
        int y = text.getLocationAtOffset(text.getOffsetAtLine(lno)).y;
        int height = 0;
        if (text.getLineCount() > lno + 1)
            height = text.getLocationAtOffset(text.getOffsetAtLine(lno + 1)).y
                    - y;
        else
            height = text.getLocationAtOffset(text.getCharCount()).y
                    + text.getLineHeight();
        int width = text.getClientArea().width + text.getHorizontalPixel();
        text.redraw(0, y, width, height, false);
    }

    class InternalHandler implements VerifyListener, ControlListener,
            TextChangeListener, SelectionListener, MouseListener, KeyListener,
            DisposeListener, LineStyleListener, LineBackgroundListener,
            PaintListener, TraverseListener {

        public void widgetDisposed(DisposeEvent e) {
            detach();
        };

        public void lineGetStyle(LineStyleEvent e) {
            int lno = text.getLineAtOffset(e.lineOffset);
            updateViewport();
            if (lno < visibleStart || lno > visibleEnd+1) {
                e.styles = new StyleRange[0];
                return;
            }
            LineRegion[] lrarr = baseEditor.getLineRegions(lno);
            Vector styles = new Vector();
            for (int idx = 0; idx < lrarr.length; idx++) {
                LineRegion lr = lrarr[idx];
                StyledRegion rdef = (StyledRegion) lr.rdef;
                if (rdef == null)
                    continue;
                if (lr.special)
                    continue;
                int start = lr.start;
                int end = lr.end;
                if (end == -1)
                    end = text.getContent().getLine(lno).length();
                end = end - start;
                start = e.lineOffset + start;

                StyleRange sr = new StyleRange(start, end, cm.getColor(
                        rdef.bfore, rdef.fore), cm.getColor(rdef.bback,
                        rdef.back), rdef.style);
                styles.addElement(sr);
            }
            ;
            e.styles = (StyleRange[]) styles.toArray(new StyleRange[] {});
        }

        public void lineGetBackground(LineBackgroundEvent e) {
            int lno = text.getLineAtOffset(e.lineOffset);
            int caret = text.getCaretOffset();
            int length = e.lineText.length();

            if (horzCrossColor != null && horzCross && lineHighlighting
                    && e.lineOffset <= caret && caret <= e.lineOffset + length) {
                e.lineBackground = cm.getColor(
                        ((StyledRegion) horzCrossColor).bback,
                        ((StyledRegion) horzCrossColor).back);
                return;
            }
            if (!fullBackground)
                return;
            LineRegion[] lr = baseEditor.getLineRegions(lno);
            for (int idx = 0; idx < lr.length; idx++) {
                StyledRegion rdef = (StyledRegion) lr[idx].rdef;
                if (lr[idx].end == -1 && rdef != null)
                    e.lineBackground = cm.getColor(rdef.bback, rdef.back);
            }
        }

        public void paintControl(PaintEvent e) {
            stateChanged();
            if (!pairsHighlighting)
                return;
            pairsDraw(e.gc, currentPair);
        }

        public void verifyText(VerifyEvent e) {
            baseEditor.modifyEvent(text.getLineAtOffset(e.start));
        }

        /*
         * public void modifyText(ExtendedModifyEvent e) { if (Logger.TRACE) {
         * Logger.trace("TextColorer",
         * "modifyEvent:"+text.getLineAtOffset(e.start)); } }
         */
        public void textChanged(TextChangedEvent event) {
            if (Logger.TRACE) {
                Logger.trace("TextColorer", "textChanged");
            }
            modifyEvent(text.getLineAtOffset(text.getCaretOffset()));
        }

        public void textChanging(TextChangingEvent event) {
        }

        // Used with complex text change events (tabbing, replacement, etc.)
        public void textSet(TextChangedEvent event) {
            if (Logger.TRACE) {
                Logger.trace("TextColorer", "textSet");
            }
            modifyEvent(0);
        }

        public void controlMoved(ControlEvent e) {
        };

        public void controlResized(ControlEvent e) {
            updateViewport();
        }

        public void widgetSelected(SelectionEvent e) {
            updateViewport();
            stateChanged();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            stateChanged();
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTraversed(TraverseEvent e) {
            updateViewport();
            stateChanged();
        }

        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
            stateChanged();
        }

        public void mouseUp(MouseEvent e) {
        }
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
 * The Original Code is the Colorer Library.
 *
 * The Initial Developer of the Original Code is
 * Cail Lomecb <cail@nm.ru>.
 * Portions created by the Initial Developer are Copyright (C) 1999-2005
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