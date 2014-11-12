package net.sf.colorer.eclipse.jface;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.FoldingBuilder;
import net.sf.colorer.editor.IFoldingReciever;
import net.sf.colorer.impl.Logger;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;

/**
 * Asynchronous folding structure reconcyler, 
 * To be run incrementally in background.
 */
public class ColorerFoldingProvider
{
    /*
     * Internal folding structure handler
     */
    class FoldingReciever implements IFoldingReciever
    {
        private int fLastLine;

        // -------------------------------------------------
        public void notifyInvalidate(int lineno) {
            if (Logger.TRACE){
                Logger.trace("FoldingReciever", "notifyInvalidate: "+lineno);
            }

            additions.clear();
            
            ProjectionAnnotationModel model = getModel();
            
            if (model == null) return;
            
            Iterator iter = model.getAnnotationIterator();
            while(iter.hasNext()){
                Annotation ann = (Annotation)iter.next();
                if (!(ann instanceof ProjectionAnnotation)) continue;
                    
                Position pos = model.getPosition(ann);
                try{
                    if (fDocument.getLineOfOffset(pos.offset) < lineno) continue;
                }catch(BadLocationException e){
                    Logger.error("FoldingReciever", "notifyInvalidate", e);
                    continue;
                }
                
                deletions.addElement(ann);
            }
            fLastLine = lineno;
        }

        public void notifyFoldingItem(int s_line, int s_offset, int e_line, int e_offset, String schema)
        {
            if (fLastLine >= e_line) return;
            fLastLine = e_line;

            ProjectionAnnotationModel model = getModel();
            if (model == null) return;

            ProjectionAnnotation ann = new ColorerProjectionAnnotation(schema);

            if (Logger.TRACE){
                Logger.trace("FoldingReciever", "notifyFoldingItem: "+s_line+":"+e_line);
            }

            try{
                int offset = fDocument.getLineOffset(s_line);
                int len = fDocument.getLineOffset(e_line) + fDocument.getLineLength(e_line) - offset;
            
                if (Logger.TRACE){
                    Logger.trace("FoldingReciever", "notifyFoldingItem:position: "+offset+":"+len);
                }
                
                Position newposition = new Position(offset, len);

                // Merge annotation folding states
                for(Enumeration e = deletions.elements(); e.hasMoreElements();){
                    ProjectionAnnotation deleted = (ProjectionAnnotation)e.nextElement();
                    if (newposition.equals(model.getPosition(deleted))){
                        if (deleted.isCollapsed()) ann.markCollapsed();
                        else ann.markExpanded();
                    }
                }

                additions.put(ann, newposition);

            }catch(BadLocationException e){
                Logger.error("FoldingReciever", "notifyFoldingItem", e);
            };
        }

    }

    static final int FOLDING_UPDATE_PERIOD = 1500;
    
    FoldingBuilder builder;
    private BaseEditor fBaseEditor;
    private IDocument fDocument;
    private IColorerEditorAdapter fEditor;
    private Display rootDisplay;
    
    private Vector deletions = new Vector();
    private HashMap additions = new HashMap();

    void updateAnnotations() {

        if (rootDisplay == null) return;
        
        ProjectionAnnotationModel model = getModel();
        if (model == null) return;

        if (deletions.size() == 0 && additions.size() == 0) return;
        
        if (Logger.TRACE){
            Logger.trace("AsyncFolding", "updateAnnotations");
        }
        model.modifyAnnotations((Annotation[])deletions.toArray(new Annotation[]{}), additions, null);

        deletions.setSize(0);
        additions.clear();
        
    }
    
    ProjectionAnnotationModel getModel() {
        if (fEditor == null) return null;
        return (ProjectionAnnotationModel)fEditor.getAdapter(ProjectionAnnotationModel.class);
    }


    public void install(IColorerEditorAdapter editor) {
        if (Logger.TRACE){
            Logger.trace("AsyncFolding", "install");
        }
        fEditor = editor;
        fDocument = (IDocument)fEditor.getAdapter(IDocument.class);
        fBaseEditor = fEditor.getBaseEditor();

        Assert.isNotNull(fBaseEditor);

        builder = new FoldingBuilder();
        
        builder.install(fBaseEditor, new FoldingReciever());
        
        rootDisplay = Display.getCurrent();

        new Thread(new Runnable(){
            
            public void run() {
                while(rootDisplay != null && !rootDisplay.isDisposed())
                {
                    rootDisplay.asyncExec(new Runnable(){
                        public void run() {
                            updateAnnotations();
                        };
                    });
                    try{
                        Thread.sleep(FOLDING_UPDATE_PERIOD);
                    }catch(InterruptedException e){}
                }
            }
        }, "ColorerFoldingProvider").start();
    }

    public void uninstall() {
        rootDisplay = null;
        builder.uninstall();
        builder = null;
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