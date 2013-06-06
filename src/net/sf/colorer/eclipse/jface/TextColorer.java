package net.sf.colorer.eclipse.jface;

import net.sf.colorer.FileType;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.DeepLevelCounter;
import net.sf.colorer.editor.PairMatch;
import net.sf.colorer.handlers.LineRegion;
import net.sf.colorer.handlers.RegionDefine;
import net.sf.colorer.handlers.RegionMapper;
import net.sf.colorer.handlers.StyledRegion;
import net.sf.colorer.impl.Logger;
import net.sf.colorer.swt.ColorManager;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * JFace based syntax highlighting implementation using
 * Colorer library. <a href='http://colorer.sf.net/'>http://colorer.sf.net/</a>
 */
public class TextColorer
{

    /**
     * Reconcyler, merged with damager and repairer.
     * To be run incrementally in background
     */
    class AsyncReconcyler implements IPresentationReconciler,
                    ITextListener, ITextInputListener, Runnable {

        static final int MAX_SINGLE_PARSE_SIZE = 30000;
        static final int INCREMENTAL_PARSE_SIZE = 5000;
        
        private IRegion fDamage;
        private Display fDisplay;
        
        public void install(ITextViewer viewer) {
            if (Logger.TRACE){
                Logger.trace("CDR", "install");
            }
            fViewer = viewer;
            if (fViewer instanceof ITextViewerExtension5) {
                fProjectionViewer = (ProjectionViewer)fViewer;
            } else {
                fProjectionViewer = new TextViewerExt5Stub();  
            }
                
            fViewer.addTextListener(this);
            fViewer.addTextInputListener(this);
            
            fDisplay = Display.getCurrent();
            new Thread(this).start();

            attach(fViewer.getTextWidget());
            
        }

        public void uninstall() {
            if (fViewer != null){
                fViewer.removeTextListener(this);
                fViewer.removeTextInputListener(this);
            }
            detach();
            fViewer = null;
        }
        
        void setDocument(IDocument document)
        {
            if (Logger.TRACE){
                Logger.trace("CDR", "setDocument");
            }
            fDocument = document;

            fDeepLevelCounter = null;
            
            if (fDocument == null) {
                return;
            }
            
            fColorManager = fEditor.getColorManager();

            fBaseEditor = fEditor.getBaseEditor();
            fBaseEditor.setRegionCompact(true);
            fBaseEditor.lineCountEvent(fDocument.getNumberOfLines());
            
            // Re-init damage region
            fDamage = new Region(0, fDocument.getLength());
        }

        /**
         *  Forms damage region from damage start till end of the text,
         *  Sends document change notifications to baseEditor parser.
         */ 
        IRegion getDamageRegion(DocumentEvent event)
        {
//            long modStamp = -1;
            
            if (event == null) return null;
            
//            modStamp = docEvent.getModificationStamp();
//            if (modStamp != prevStamp){
                try {        
                    if (Logger.TRACE){
                        Logger.trace("CDR", "getDamageRegion: sending modify event: "+
                                fDocument.getLineOfOffset(event.getOffset()));
                    }
                    fBaseEditor.modifyEvent(fDocument.getLineOfOffset(event.getOffset()));
                }catch(BadLocationException e){};
//            }
            fBaseEditor.lineCountEvent(fDocument.getNumberOfLines());

            try{
//                prevStamp = event.getModificationStamp();
                int soffset = fDocument.getLineInformationOfOffset(event.getOffset()).getOffset();
                // Join with already existing damage
                if (fDamage != null && soffset > fDamage.getOffset()) {
                    soffset = fDamage.getOffset();
                }
                // Create new damage region
                if (Logger.TRACE){
                    Logger.trace("CDR", "getDamageRegion "+soffset+":"+(fDocument.getLength() - soffset));
                }
                return new Region(soffset, fDocument.getLength() - soffset);
            }catch(BadLocationException e){
                Logger.error("CDR", "getDamageRegion", e);
            }
            return null;
        }

        IRegion getVisualDamageRegion(TextEvent event) {
            if (Logger.TRACE){
                Logger.trace("CDR", "getVisualDamageRegion TextEvent: "+event.getOffset()+":"+event.getLength()+", replace:"+event.getReplacedText());
            }
            try {
                // Normalize offsets to line bounds
                int sOffset = fProjectionViewer.widgetOffset2ModelOffset(event.getOffset());
                if (sOffset == -1) sOffset = 0;
                sOffset = fDocument.getLineInformationOfOffset(sOffset).getOffset();

                int eOffset = fProjectionViewer.widgetOffset2ModelOffset(event.getOffset()+event.getLength());
                if (eOffset == -1) eOffset = fDocument.getLength();
                IRegion eOffsetLine = fDocument.getLineInformationOfOffset(eOffset);
                eOffset = eOffsetLine.getOffset() + eOffsetLine.getLength();

                if (event.getOffset() == 0 && event.getLength() == 0 && event.getText() == null) {
                    // redraw state change, damage the whole document
                    sOffset = 0; 
                    eOffset = fDocument.getLength();
                }

                //Take care of general damage region:
                if (fDamage != null && sOffset > fDamage.getOffset()) {
                    sOffset = fDamage.getOffset();
                }                

                if (Logger.TRACE){
                    Logger.trace("CDR", "getVisualDamageRegion return: "+sOffset+":"+(eOffset-sOffset));
                }
                return new Region(sOffset, eOffset-sOffset);
            }catch(BadLocationException e){
                Logger.error("CDR", "getVisualDamageRegion", e);
            }
            return null;
        }
        
        /**
         * Runs reconciling process for the damaged document.
         */
        public void textChanged(TextEvent event) {
            if (fDocument == null) return;
            
            if (Logger.TRACE) {
                Logger.trace("TextColorer", "textChanged");
            }
            
            updateViewport();
            
            IRegion newDamage = null;
            
            if (event.getDocumentEvent() != null) {
                // Document change requested
                newDamage = getDamageRegion(event.getDocumentEvent());
                if (newDamage != null)
                {
                    fDamage = newDamage;
                }
                repairPresentation(true);
                fModTimestamp = System.currentTimeMillis();
            }else{
                // Visual update requested
                newDamage = getVisualDamageRegion(event);
                if (newDamage != null)
                {
                    fDamage = newDamage;
                }
                repairPresentation(false);
            }
        }

        /**
         * Creates presentation for the passed in damage region.
         * @param presentation Object to fillup with styles
         * @param damage Region to recover (line-based)
         */
        void createPresentation(TextPresentation presentation, IRegion damage)
        {
            if (Logger.TRACE){
                Logger.trace("CDR", "createPresentation "+damage.getOffset()+":"+damage.getLength());
            }
            try{
                int l_start = fDocument.getLineOfOffset(damage.getOffset());
                int l_end = fDocument.getLineOfOffset(damage.getOffset()+damage.getLength());

                if (Logger.TRACE){
                    Logger.trace("CDR", "createPresentation: filling "+l_start+":"+l_end);
                }
                int fLastPos = -1;
            
                for (int lno = l_start; lno <= l_end; lno++) {
                    LineRegion[] lrarr = fBaseEditor.getLineRegions(lno);
                    
                    for (int idx = 0; idx < lrarr.length; idx++) {
                        LineRegion lr = lrarr[idx];
                        StyledRegion rdef = (StyledRegion) lr.rdef;
                        if (rdef == null) continue;
                        if (lr.special) continue;
                        
                        IRegion lineinfo = fDocument.getLineInformation(lno);

                        int start = lr.start;
                        int end = lr.end;
                        if (end == -1) end = lineinfo.getLength();
                        int length = end - start;
                        start = lineinfo.getOffset() + start;
                        
                        if (length == 0) {
                            continue;
                        }

//                        /*
                        Logger.trace("CDR", "trace line:"+lno+"lineofset="+lineinfo.getOffset()+"linelength="+lineinfo.getLength()+" start="+start+" length="+length);

                        if (fLastPos > start){
                            Logger.error("CDR", "sequence failure");
                        }
                        fLastPos = start+length;
//                        */

                        StyleRange sr = new StyleRange(start, length,
                                getDeepFGColor(fDeepLevelCounter, lno, rdef),
                                getDeepBGColor(fDeepLevelCounter, lno, rdef),
                                rdef.style);
                        presentation.addStyleRange(sr);
                    }
                }
                Logger.trace("CDR", "createPresentation: filled");
            }catch(BadLocationException e){
                Logger.error("CDR", "StyleRange fill error", e);
            };
        }


        /**
         * Activates asynchronous presentation repair.
         */
        public void run()
        {
            while(fViewer != null && !fDisplay.isDisposed())
            {
                fDisplay.asyncExec(new Runnable() {
                    public void run() {
                        if (System.currentTimeMillis() > fModTimestamp+ASYNC_DELAY-200)
                            repairPresentation(false);
                    }
                });

                try{
                    Thread.sleep(ASYNC_DELAY);
                }catch(Exception e){}
                
            }
        }
        
        public void repairPresentation(boolean visual)
        {
            if (fDamage == null || fViewer == null) return;

            if (Logger.TRACE){
                Logger.trace("CDR", "Presentation change started");
            }
            
            try{
                int newlen = fViewer.getBottomIndexEndOffset() - fDamage.getOffset();
                // Damage is below visible screen - stop repair if only visual part requested
                if (visual && newlen <= 0){
                    return; 
                }
                // Damage is below visible screen - parse incrementally
                if (newlen <= 0) newlen = INCREMENTAL_PARSE_SIZE;
                // Just to be sure
                if (newlen > fDamage.getLength()) newlen = fDamage.getLength();
                // Clip long damages to process them incrementally
                newlen = Math.min(newlen, MAX_SINGLE_PARSE_SIZE);

                // Align length by line's end
                IRegion endline = fDocument.getLineInformationOfOffset(fDamage.getOffset()+newlen);
                newlen = endline.getOffset()+endline.getLength()-fDamage.getOffset();

                IRegion visibleDamage = new Region(fDamage.getOffset(), newlen);
                if (visibleDamage.getLength() == 0) return;

                if (Logger.TRACE){
                    Logger.trace("CDR", "Presentation build for region: "+ visibleDamage.getOffset() + ":" +  visibleDamage.getLength());
                }

                TextPresentation presentation = new TextPresentation(visibleDamage, 10+newlen/10);

                createPresentation(presentation, visibleDamage);

                // Clear range:
                int widgetOffset = fProjectionViewer.modelOffset2WidgetOffset(visibleDamage.getOffset());
                int widgetLength = fProjectionViewer.modelOffset2WidgetOffset(visibleDamage.getOffset()+visibleDamage.getLength())-widgetOffset;
                if (Logger.TRACE){
                    Logger.trace("CDR", "text: " + text.getText().length());
                    Logger.trace("CDR", "text.setStyleRanges: " + widgetOffset +":"+widgetLength);
                }
                try{
                    if (widgetOffset >= 0 && widgetLength > 0){
                        text.setStyleRanges(widgetOffset, widgetLength, null, null);
                    }
                }catch(Throwable e){
                    Logger.error("CDR", "setStyleRanges: ", e);
                }

                int newstart = visibleDamage.getOffset()+visibleDamage.getLength();
                fDamage = new Region(newstart, fDamage.getOffset()+fDamage.getLength()-newstart);
                
                if (fDamage.getLength() <= 0){
                    fDamage = null;
                }

                fViewer.changeTextPresentation(presentation, false);

                if (Logger.TRACE){
                    Logger.trace("CDR", "Presentation change finished");
                }

            }catch(Exception e){
                Logger.error("CDR", "runnable repairer failed", e);
            }
        }
        
        public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
        }
        public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
            if (newInput != null){
                setDocument(newInput);
                fEditor.handleAttachComplete();
            }
        }
        public IPresentationDamager getDamager(String contentType) {
            return null;
        }
        public IPresentationRepairer getRepairer(String contentType) {
            return null;
        }
        
    }

    private Color getDeepFGColor(DeepLevelCounter deepLevelCounter, int lno, StyledRegion rdef) {
        
        if (!rdef.bfore) return null;

        int r = rdef.fore >> 16;
        int g = (rdef.fore&0xFF00) >> 8;
        int b = (rdef.fore&0xFF);

        if (deepLevelCounter != null && fBackgroundScale > 0)
        {
            int level = deepLevelCounter.getLineDeepLevel(lno);
            int maxlevel = deepLevelCounter.getMaxDeepLevel();
    
            r += (255-r)*level/maxlevel  *fBackgroundScale/10  /8;
            r = Math.max(0, Math.min(r, 255));
            g += (255-g)*level/maxlevel  *fBackgroundScale/10  /8;
            g = Math.max(0, Math.min(g, 255));
            b += (255-b)*level/maxlevel  *fBackgroundScale/10  /8;
            b = Math.max(0, Math.min(b, 255));
        }
        
        int newc = b + (g<<8) + (r<<16);
        
        return fColorManager.getColor(true, newc);
    }
    
    private Color getDeepBGColor(DeepLevelCounter deepLevelCounter, int lno, StyledRegion rdef) {
        
        if (!rdef.bback) return null;

        int r = rdef.back >> 16;
        int g = (rdef.back&0xFF00) >> 8;
        int b = (rdef.back&0xFF);

        if (deepLevelCounter != null && fBackgroundScale > 0)
        {
            int level = deepLevelCounter.getLineDeepLevel(lno);
            int maxlevel = deepLevelCounter.getMaxDeepLevel();
    
            r -= r*level/maxlevel/6  *fBackgroundScale/5;
            r = Math.max(0, Math.min(r, 255));
            g -= g*level/maxlevel/6  *fBackgroundScale/5;
            g = Math.max(0, Math.min(g, 255));
            b -= b*level/maxlevel/6  *fBackgroundScale/5;
            b = Math.max(0, Math.min(b, 255));
        }
        
        int newbg = b + (g<<8) + (r<<16);
        
        return fColorManager.getColor(true, newbg);
    }

    Color getDeepBGColor(DeepLevelCounter deepLevelCounter, int lno, Color color) {

        
        if (deepLevelCounter != null && fBackgroundScale > 0)
        {
            int level = deepLevelCounter.getLineDeepLevel(lno);
            int maxlevel = deepLevelCounter.getMaxDeepLevel();
    
            if (level == 0) return color;
            
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
    
            r -= r*level/maxlevel/6  *fBackgroundScale/5;
            r = Math.max(0, Math.min(r, 255));
            g -= g*level/maxlevel/6  *fBackgroundScale/5;
            g = Math.max(0, Math.min(g, 255));
            b -= b*level/maxlevel/6  *fBackgroundScale/5;
            b = Math.max(0, Math.min(b, 255));

            int newbg = b + (g<<8) + (r<<16);
            
            return fColorManager.getColor(true, newbg);
        }

        return color;
        
    }

    /**
     * General StyledText widget helper. Links StyledText with colorer's
     * BaseEditor API
     */
    class WidgetEventHandler implements KeyListener,
            DisposeListener, LineBackgroundListener,
            PaintListener, TraverseListener, ControlListener,
            MouseListener, ISelectionChangedListener, IViewportListener
    {
    
        public void widgetDisposed(DisposeEvent e) {
            detach();
        };
    
        public void lineGetBackground(LineBackgroundEvent e) {
            int lno = text.getLineAtOffset(e.lineOffset);
            int caret = text.getCaretOffset();
            int length = e.lineText.length();
    
            if (horzCrossColor != null && horzCross && lineHighlighting
                    && e.lineOffset <= caret && caret <= e.lineOffset + length) {
                e.lineBackground = fColorManager.getColor(
                        ((StyledRegion) horzCrossColor).bback,
                        ((StyledRegion) horzCrossColor).back);
                return;
            }
            if (!fullBackground)
                return;
            LineRegion[] lr = fBaseEditor.getLineRegions(fProjectionViewer.widgetLine2ModelLine(lno));
            for (int idx = 0; idx < lr.length; idx++) {
                StyledRegion rdef = (StyledRegion) lr[idx].rdef;
                if (lr[idx].end == -1 && rdef != null) {
                    e.lineBackground = fColorManager.getColor(rdef.bback, rdef.back);
                }
            }
            e.lineBackground = getDeepBGColor(fDeepLevelCounter, fProjectionViewer.widgetLine2ModelLine(lno),
                    e.lineBackground == null ? text.getBackground() : e.lineBackground);            
        }
    
        public void paintControl(PaintEvent e) {
//            stateChanged();
            if (!pairsHighlighting)
                return;
            pairsDraw(e.gc, currentPair);
        }
    
        // ------------------
        public void keyPressed(KeyEvent e) {
            stateChanged();
        }
    
        public void keyReleased(KeyEvent e) {
        }
    
        public void keyTraversed(TraverseEvent e) {
            stateChanged();
        }
    
        // ------------------
    
        public void controlMoved(ControlEvent e) {
        }
    
        public void controlResized(ControlEvent e) {
            stateChanged();
        }
    
        // ------------------
    
        public void mouseDoubleClick(MouseEvent e) {
        }
    
        public void mouseDown(MouseEvent e) {
            stateChanged();
        }
    
        public void mouseUp(MouseEvent e) {
        }
    
        // ------------------

        public void selectionChanged(SelectionChangedEvent event) {
            stateChanged();
        }

        public void viewportChanged(int verticalOffset) {
            stateChanged();
        }
    }


    final static int ASYNC_DELAY = 500;

    public final static int HLS_NONE = 0;
    public final static int HLS_XOR = 1;
    public final static int HLS_OUTLINE = 2;
    public final static int HLS_OUTLINE2 = 3;

    private int highlightStyle = HLS_XOR;

    private boolean fullBackground = false;
    private boolean vertCross = false;
    private boolean horzCross = false;

    private RegionDefine vertCrossColor = null;
    private RegionDefine horzCrossColor = null;

    private PairMatch currentPair = null;

    int prevLine = 0;
    int visibleStart, visibleEnd;
    long fModTimestamp;

    boolean lineHighlighting = true;
    boolean pairsHighlighting = true;

    private IDocument fDocument;

    private ColorManager fColorManager;
    private BaseEditor fBaseEditor;
    private IColorerEditorAdapter fEditor;
    private StyledText text;

    private ITextViewer fViewer;
    private ITextViewerExtension5 fProjectionViewer;

    private WidgetEventHandler fHandler = new WidgetEventHandler();
    private AsyncReconcyler fReconciler = new AsyncReconcyler();
    private DeepLevelCounter fDeepLevelCounter;

    private int fBackgroundScale = 0;

    
    /**
     * Common TextColorer creation constructor. Creates TextColorer object,
     * which is to be attached to the StyledText widget.
     * 
     * @param pf Parser factory, used to create all coloring text parsers.
     * @param cm Color Manager, used to store cached color objects
     */
    public TextColorer(IColorerEditorAdapter editor)
    {
        fEditor = editor;
    }


    /**
     * Installs this highlighted into the specified StyledText object. Client
     * can manually call detach() method, when wants to destroy this object.
     */
    void attach(StyledText parent) {

        text = parent;
        text.addDisposeListener(fHandler);
        text.addLineBackgroundListener(fHandler);
        text.addPaintListener(fHandler);

        text.addControlListener(fHandler);
        text.addKeyListener(fHandler);
        text.addTraverseListener(fHandler);
        text.addMouseListener(fHandler);
//        text.addSelectionListener(fHandler);
        fViewer.addViewportListener(fHandler);
//        fProjectionViewer.addSelectionChangedListener(fHandler);

//        ScrollBar sb = text.getVerticalBar();
//        if (sb != null)
//            sb.addSelectionListener(fHandler);
    }

    /**
     * Removes this object from the corresponding StyledText widget. Object
     * can't be used after this call, until another attach. This method is
     * called automatically, when StyledText widget is disposed
     */
    void detach() {
        if (text == null)
            return;
        text.removeDisposeListener(fHandler);
        text.removeLineBackgroundListener(fHandler);
        text.removePaintListener(fHandler);
        text.removeControlListener(fHandler);
        text.removeKeyListener(fHandler);
        text.removeTraverseListener(fHandler);
        text.removeMouseListener(fHandler);
//        fViewer.removeSelectionChangedListener(fHandler);
        fViewer.removeViewportListener(fHandler);
//        ScrollBar sb = text.getVerticalBar();
//        if (sb != null)
//            sb.removeSelectionListener(fHandler);
        text = null;
    }
    
    /**
     * Updates all the references to external colorer objects
     * and invalidates current text presentation
     */
    public void relink() {
        fBaseEditor = null;
        fReconciler.setDocument(fDocument);
        updateViewport();
        invalidateSyntax();
    }

    /**
     * Makes the syntax presentation invalid and forces redraw
     */
    public void invalidateSyntax() {
        //prevStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
        fBaseEditor.modifyEvent(0);
        fViewer.invalidateTextPresentation();
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
        FileType selected = fBaseEditor.chooseFileType(filename);
        if (Logger.TRACE){
            Logger.trace("TextColorer", "chooseFileType: "+ filename + " - " + selected.getName());
        }
        invalidateSyntax();
        return selected;
    }

    /**
     * Selects and installs specified file type.
     * 
     * @param typename
     *            Name or description of HRC filetype.
     */
    public void setFileType(FileType typename) {
        checkActive();
        fBaseEditor.setFileType(typename);
        invalidateSyntax();
    }

    /**
     * Returns currently used file type.
     */
    public FileType getFileType() {
        checkActive();
        return fBaseEditor.getFileType();
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
        fBaseEditor.setRegionMapper(regionMapper);

        StyledRegion sr = (StyledRegion) fBaseEditor.getBackground();
        text.setForeground(null);
        text.setBackground(null);
        if (useBackground) {
            text.setForeground(fColorManager.getColor(sr.bfore, sr.fore));
            text.setBackground(fColorManager.getColor(sr.bback, sr.back));
        };
        setCross(vertCross, horzCross);
        invalidateSyntax();
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
        fBaseEditor.setRegionMapper(StyledRegion.HRD_RGB_CLASS, hrdName);

        StyledRegion sr = (StyledRegion) fBaseEditor.getBackground();
        text.setForeground(null);
        text.setBackground(null);
        if (useBackground) {
            text.setForeground(fColorManager.getColor(sr.bfore, sr.fore));
            text.setBackground(fColorManager.getColor(sr.bback, sr.back));
        };
        setCross(vertCross, horzCross);
        invalidateSyntax();
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
        invalidateSyntax();
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
            horzCrossColor = fBaseEditor.getHorzCross();
        if (vertCross)
            vertCrossColor = fBaseEditor.getVertCross();
        fViewer.invalidateTextPresentation();
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
    public void setPairsPainter(int style) {
        highlightStyle = style;
        text.redraw();
    }

    /** Checks if caret positioned on highlighted pair. */
    public boolean pairAvailable() {
        return currentPair != null;
    }

    /** Moves caret to the position of currently active pair. */
    public boolean matchPair() {
        if (currentPair == null)
            return false;
        try{
//            int caret = fProjectionViewer.widgetOffset2ModelOffset(text.getCaretOffset());
//            int lno = fDocument.getLineOfOffset(caret);
//            PairMatch cp = fBaseEditor.getPairMatch(lno, caret-
//                    fDocument.getLineOffset(lno));
            if (currentPair.end == null){
                fBaseEditor.searchGlobalPair(currentPair);
            }
            if (currentPair.end == null)
                return false;
            int position = fDocument.getLineOffset(currentPair.eline);
            if (currentPair.topPosition){
                position += currentPair.end.end;
            }else{
                position += currentPair.end.start;
            }
            fEditor.selectAndReveal(position, 0);
            return true;
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
        return false;
    }

    /** Selects a content of the currently positioned pair. */
    public boolean selectPair() {
        if (currentPair == null)
            return false;
        try{
//            int caret = fProjectionViewer.widgetOffset2ModelOffset(text.getCaretOffset());
//            int lno = fDocument.getLineOfOffset(caret);
//            PairMatch cp = fBaseEditor.getPairMatch(lno, caret-
//                    fDocument.getLineOffset(lno));
            if (currentPair.end == null){
                fBaseEditor.searchGlobalPair(currentPair);
            }
            if (currentPair.end == null){
                return false;
            }
            if (currentPair.topPosition) {
                int offset = fDocument.getLineOffset(currentPair.sline)+currentPair.start.start;
                fEditor.selectAndReveal(offset,
                        fDocument.getLineOffset(currentPair.eline)+currentPair.end.end - offset);
            }else{
                int offset = fDocument.getLineOffset(currentPair.eline)+currentPair.end.start;
                fEditor.selectAndReveal(offset,
                        fDocument.getLineOffset(currentPair.sline)+currentPair.start.end - offset);
            }
            return true;
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
        return false;
    }

    /**
     * Selects an internal part of the currently selected paired content
     */
    public boolean selectContentPair() {
        if (currentPair == null)
            return false;
        try{
//            int caret = fProjectionViewer.widgetOffset2ModelOffset(text.getCaretOffset());
//            int lno = fDocument.getLineOfOffset(caret);
//            PairMatch currentPair = fBaseEditor.getPairMatch(lno, caret-
//                    fDocument.getLineOffset(lno));
            if (currentPair.end == null){
                fBaseEditor.searchGlobalPair(currentPair);
            }
            if (currentPair.end == null){
                return false;
            }
            if (currentPair.topPosition){
                int offset = fDocument.getLineOffset(currentPair.sline)+currentPair.start.end;
                fEditor.selectAndReveal(offset,
                        fDocument.getLineOffset(currentPair.eline)+currentPair.end.start - offset);
            }else{
                int offset = fDocument.getLineOffset(currentPair.eline)+currentPair.end.end;
                fEditor.selectAndReveal(offset,
                        fDocument.getLineOffset(currentPair.sline)+currentPair.start.start - offset);
            }
            return true;
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
        return false;
   }

    /**
     * Retrieves current LineRegion under caret.
     * 
     * @return LineRegion currently under Caret
     */
    public LineRegion getCaretRegion() {
        LineRegion caretRegion = null;
        try{
            int caret = fProjectionViewer.widgetOffset2ModelOffset(text.getCaretOffset());
            int lno = fDocument.getLineOfOffset(caret);
        
            int linepos = caret - fDocument.getLineOffset(lno);

            LineRegion[] arr = fBaseEditor.getLineRegions(lno);
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
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
        return null;
    }

    /**
     * Detects if there was enough idle time (no user activity) to process
     * asynchronously any model updates.
     * @return true, if there was enough idle
     */
    boolean canProcess() {
        return (System.currentTimeMillis() - fModTimestamp > ASYNC_DELAY);
    }
    
    /**
     * Informs colorer about visible state change of the editor
     */
    void stateChanged()
    {
//        fModTimestamp = System.currentTimeMillis();

        if (fDocument == null) return;

        if (Logger.TRACE){
            Logger.trace("TextColorer", "stateChanged");
        }
        
        updateViewport();
        fReconciler.repairPresentation(true);

        try{
            int curOffset = fProjectionViewer.widgetOffset2ModelOffset(text.getCaretOffset());
            int curLine = fDocument.getLineOfOffset(curOffset);

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
                int lineOffset = fDocument.getLineOffset(curLine);
                PairMatch newmatch = fBaseEditor.getPairMatch(curLine,
                        curOffset - lineOffset);
                if (newmatch != null)
                    fBaseEditor.searchLocalPair(newmatch);
                if ((newmatch == null && currentPair != null)
                        || (newmatch != null && !newmatch.equals(currentPair))) {
                    pairsDraw(null, currentPair);
                    pairsDraw(null, newmatch);
                }
                currentPair = newmatch;
            }
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
    }

    void updateViewport() {
        checkActive();
        
        if (fDocument == null) return;

        if (Logger.TRACE){
            Logger.trace("TextColorer", "updateViewport");
        }

        visibleStart = 0;
        try {
            visibleStart = fViewer.getTopIndex() - 1;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        if (visibleStart < 0) visibleStart = 0;
        visibleEnd = fViewer.getBottomIndex();
        int lc = fDocument.getNumberOfLines();
        if (visibleEnd > lc) visibleEnd = lc;
        if (visibleEnd <= visibleStart) visibleEnd = visibleStart+1;

        fBaseEditor.visibleTextEvent(visibleStart, visibleEnd - visibleStart + 1);
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
                            ^ fColorManager.getColor(text.getBackground());
                    if (text.getLineAtOffset(text.getCaretOffset()) == text.getLineAtOffset(start)
                            && horzCross
                            && horzCrossColor != null
                            && ((StyledRegion) horzCrossColor).bback)
                        resultColor = sr.fore ^ ((StyledRegion) horzCrossColor).back;
                    Color color = fColorManager.getColor(sr.bfore, resultColor);
                    gc.setBackground(color);
                    gc.setXORMode(true);
                    gc.fillRectangle(left.x, left.y, right.x - left.x, gc.getFontMetrics().getHeight());
                } else if (highlightStyle == HLS_OUTLINE) {
                    Color color = fColorManager.getColor(sr.bfore, sr.fore);
                    gc.setForeground(color);
                    gc.drawRectangle(left.x, left.y, right.x - left.x - 1, gc.getFontMetrics().getHeight() - 1);
                } else if (highlightStyle == HLS_OUTLINE2) {
                    Color color = fColorManager.getColor(sr.bfore, sr.fore);
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
        try{
            if (pm.start != null) {
                /* Do not draw pairs if currently invisible */
                if (pm.sline < visibleStart || pm.sline > visibleEnd) return;
    
                int lineOffset = fDocument.getLineOffset(pm.sline);
                int drawStart = fProjectionViewer.modelOffset2WidgetOffset(lineOffset);
                if (drawStart == -1) return;
                pairDraw(gc, (StyledRegion) pm.start.rdef, pm.start.start
                        + drawStart, pm.start.end + drawStart);
            }
            if (pm.end != null) {
                if (pm.eline < visibleStart || pm.eline > visibleEnd) return;
    
                int lineOffset = fDocument.getLineOffset(pm.eline);
                int drawStart = fProjectionViewer.modelOffset2WidgetOffset(lineOffset);
                if (drawStart == -1) return;
                pairDraw(gc, (StyledRegion) pm.end.rdef, pm.end.start + drawStart,
                        pm.end.end + drawStart);
            }
        }catch(BadLocationException e){
            Assert.isTrue(false, "Never reach");
        }
    }

    void drawLine(int lno) {
        if (lno < 0 || lno >= fDocument.getNumberOfLines())
            return;
        int widgetLine = fProjectionViewer.modelLine2WidgetLine(lno);
        if (widgetLine == -1) return;
        int y = text.getLocationAtOffset(text.getOffsetAtLine(widgetLine)).y;
        int height = 0;
        if (text.getLineCount() > widgetLine+1)
            height = text.getLocationAtOffset(text.getOffsetAtLine(widgetLine+1)).y - y;
        else
            height = text.getLocationAtOffset(text.getCharCount()).y
                    + text.getLineHeight();
        int width = text.getClientArea().width + text.getHorizontalPixel();
        text.redraw(0, y, width, height, false);
    }

    /**
     * Returns a PresentationReconciler this TextColorer provides
     * @return
     */
    public IPresentationReconciler getPresentationReconciler()
    {
        return fReconciler;
    }

    /**
     * Sets the required level of background darkening
     * @param back_scale Integer from 0 (off) to 10 (max)
     */
    public void setBackgroundScale(int back_scale) {
        fBackgroundScale = back_scale;
        
        if (fBackgroundScale == 0) {
            if (fDeepLevelCounter != null) {
                fDeepLevelCounter.uninstall();
                fDeepLevelCounter = null;
            }
        }else{
            if (fDeepLevelCounter == null) {
                fDeepLevelCounter = new DeepLevelCounter();
                fDeepLevelCounter.install(fBaseEditor);
            }
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