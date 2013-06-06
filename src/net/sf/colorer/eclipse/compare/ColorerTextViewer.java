package net.sf.colorer.eclipse.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.colorer.eclipse.editors.ColorerSourceViewerConfiguration;
import net.sf.colorer.eclipse.jface.TextColorer;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ColorerTextViewer extends Viewer {


    private SourceViewer fSourceViewer;
    private Object fInput;
    private TextColorer fTextColorer;
    
    
    ColorerTextViewer(Composite parent) {
        fSourceViewer= new SourceViewer(parent, null, SWT.H_SCROLL + SWT.V_SCROLL);
        // TODO? Title??
        fTextColorer = new TextColorer(new StubTextEditor(fSourceViewer, ""));
        fSourceViewer.configure(new ColorerSourceViewerConfiguration(fTextColorer));

        fSourceViewer.setEditable(false);
    }
        
    public Control getControl() {
        return fSourceViewer.getControl();
    }
    
    public void setInput(Object input) {
        
        if (input instanceof IStreamContentAccessor) {
            Document document= new Document(getString(input));
            fSourceViewer.setDocument(document);
        }
        fInput= input;
    }
    
    public Object getInput() {
        return fInput;
    }
    
    public ISelection getSelection() {
        return null;
    }
    
    public void setSelection(ISelection s, boolean reveal) {
    }
    
    public void refresh() {
    }
    
    /**
     * A helper method to retrieve the contents of the given object
     * if it implements the IStreamContentAccessor interface.
     */
    private static String getString(Object input) {
        
        if (input instanceof IStreamContentAccessor) {
            IStreamContentAccessor sa= (IStreamContentAccessor) input;
            try {
                return readString(sa);
            } catch (CoreException ex) {
            }
        }
        return "";
    }

    
    /**
     * Copied from original Eclipse's JDT source.
     * TODO: Refactor
     */
    public static String readString(IStreamContentAccessor sa) throws CoreException {
        InputStream is= sa.getContents();
        if (is != null) {
            String encoding= null;
            if (sa instanceof IEncodedStreamContentAccessor) {
                try {
                    encoding= ((IEncodedStreamContentAccessor) sa).getCharset();
                } catch (Exception e) {
                }
            }
            if (encoding == null)
                encoding= ResourcesPlugin.getEncoding();
            return readString(is, encoding);
        }
        return null;
    }
    
    /**
     * Copied from original Eclipse's JDT source.
     * TODO: Refactor
     * 
     * Reads the contents of the given input stream into a string.
     * The function assumes that the input stream uses the platform's default encoding
     * (<code>ResourcesPlugin.getEncoding()</code>).
     * Returns null if an error occurred.
     * 
     */
    private static String readString(InputStream is, String encoding) {
        if (is == null)
            return null;
        BufferedReader reader= null;
        try {
            StringBuffer buffer= new StringBuffer();
            char[] part= new char[2048];
            int read= 0;
            reader= new BufferedReader(new InputStreamReader(is, encoding));

            while ((read= reader.read(part)) != -1)
                buffer.append(part, 0, read);
            
            return buffer.toString();
            
        } catch (IOException ex) {
            // NeedWork
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // silently ignored
                }
            }
        }
        return null;
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