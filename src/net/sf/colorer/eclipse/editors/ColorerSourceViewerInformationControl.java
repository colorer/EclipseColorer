package net.sf.colorer.eclipse.editors;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ColorerSourceViewerInformationControl implements IInformationControl,
                                    IInformationControlExtension, DisposeListener {
    /** The control's shell */
    private Shell fShell;
    
    /** Border thickness in pixels. */
    private static final int BORDER= 1;
    
    /** The control's source viewer */
    private SourceViewer fViewer;
    
    /** The control's text widget */
    private StyledText fText;
    
    public ColorerSourceViewerInformationControl(Shell parent) {
        GridLayout layout;
        GridData gd;

        fShell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | SWT.NO_TRIM);
        Display display= fShell.getDisplay();       
        fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

        Composite composite= fShell;
        layout= new GridLayout(1, false);
        int border= ((SWT.NO_TRIM & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        layout.marginHeight= border;
        layout.marginWidth= border;
        composite.setLayout(layout);
        gd= new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);
        fViewer= createViewer(composite);
        
        fText= fViewer.getTextWidget();
        gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
        fText.setLayoutData(gd);
        fText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        fText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            
        fText.addKeyListener(new KeyListener() {
                
            public void keyPressed(KeyEvent e)  {
                if (e.character == 0x1B) // ESC
                    fShell.dispose();
            }
                
            public void keyReleased(KeyEvent e) {}
        });
    }
    
    
    private SourceViewer createViewer(Composite parent) {
        SourceViewer viewer = new SourceViewer(parent, null, SWT.NONE);
          
        SourceViewerConfiguration configuration = new SourceViewerConfiguration();
        viewer.configure(configuration);
        viewer.setEditable(false);  
        Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
        viewer.getTextWidget().setFont(font);
                
        return viewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
     */
    public void setInformation(String content) {
        if (content == null) {
            fViewer.setInput(null);
            return;
        }
        IDocument document = new Document(content);       
        fViewer.setDocument(document);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)
     */
    public void setSizeConstraints(int maxWidth, int maxHeight) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
     */
    public Point computeSizeHint() {
        return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        fShell.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setSize(int, int)
     */
    public void setSize(int width, int height) {
        fShell.setSize(width, height);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setLocation(org.eclipse.swt.graphics.Point)
     */
    public void setLocation(Point location) {
        Rectangle trim= fShell.computeTrim(0, 0, 0, 0);
        Point textLocation= fText.getLocation();                
        location.x += trim.x - textLocation.x;      
        location.y += trim.y - textLocation.y;      
        fShell.setLocation(location);       
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#dispose()
     */
    public void dispose() {
        if (fShell != null && !fShell.isDisposed()) {
            fShell.dispose();
        } else {
            widgetDisposed(null);
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#addDisposeListener(org.eclipse.swt.events.DisposeListener)
     */
    public void addDisposeListener(DisposeListener listener) {
        fShell.addDisposeListener(listener);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
     */
    public void removeDisposeListener(DisposeListener listener) {
        fShell.removeDisposeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setForegroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setForegroundColor(Color foreground) {
        fText.setForeground(foreground);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setBackgroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setBackgroundColor(Color background) {
        fText.setBackground(background);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#isFocusControl()
     */
    public boolean isFocusControl() {
        return fText.isFocusControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setFocus()
     */
    public void setFocus() {
        fShell.forceFocus();
        fText.setFocus();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#addFocusListener(org.eclipse.swt.events.FocusListener)
     */
    public void addFocusListener(FocusListener listener) {
        fText.addFocusListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#removeFocusListener(org.eclipse.swt.events.FocusListener)
     */
    public void removeFocusListener(FocusListener listener) {
        fText.removeFocusListener(listener);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
     */
    public boolean hasContents() {
        return fText.getCharCount() > 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e) {
        fShell= null;
        fText= null;
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