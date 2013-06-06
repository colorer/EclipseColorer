package net.sf.colorer.eclipse.jface;

import java.util.Iterator;

import net.sf.colorer.Region;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.OutlineItem;
import net.sf.colorer.editor.OutlineListener;
import net.sf.colorer.editor.Outliner;
import net.sf.colorer.impl.Logger;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Region-based annotation collector  
 */
public class ColorerAnnotationProvider {
    
    class AnnotationOutlineListener implements OutlineListener{

        public void notifyUpdate() {
            fAnnotationsUpdated = false;
        }
    
    }

    static final int UPDATE_PERIOD = 1000;
    
    private volatile Outliner builder;
    private BaseEditor fBaseEditor;
    private IDocument fDocument;
    private IColorerEditorAdapter fEditor;
    private boolean fAnnotationsUpdated = false;
    
    OutlineListener outlineListener = new AnnotationOutlineListener();
    
    Region def_TODO;
    Region def_ErrorText;
    Region def_Debug;
    
    void updateAnnotations() {

        if (fAnnotationsUpdated) return;
        
        IAnnotationModel model = getModel();
        if (model == null) return;
        
        Iterator iter = model.getAnnotationIterator();
        while(iter.hasNext()){
            Annotation ann = (Annotation)iter.next();
            if (ann instanceof ColorerAnnotation){
                model.removeAnnotation(ann);
            }
        }

        if (!(fEditor.getTextColorer().canProcess())) return;
        
        try{
            
            for(int idx = 0; idx < builder.itemCount(); idx++){
                OutlineItem item = builder.getItem(idx);

                int offset = fDocument.getLineOffset(item.lno) + item.pos;
                Position position = new Position(offset, item.token.length());
                
                String type = ColorerAnnotation.ERROR;
                if (item.region.hasParent(def_TODO)) type = ColorerAnnotation.TASK;
                if (item.region.hasParent(def_ErrorText)) type = ColorerAnnotation.WARNING;
                if (item.region.hasParent(def_Debug)) type = ColorerAnnotation.INFO;
                Annotation annotation = new ColorerAnnotation(type, item.region.getDescription()+" : "+item.token);
                
                model.addAnnotation(annotation, position);
            }
        }catch(Exception e){
            Logger.error("ColorerAnnotationProvider", "updateAnnotations", e);
        }
        fAnnotationsUpdated = true;
    }
    
    IAnnotationModel getModel() {
        if (fEditor == null) return null;
        return ((ISourceViewer)fEditor.getAdapter(ISourceViewer.class)).getAnnotationModel();
    }


    public void install(IColorerEditorAdapter editor) {
        if (Logger.TRACE){
            Logger.trace("AsyncFolding", "install");
        }
        fEditor = editor;
        fDocument = (IDocument)fEditor.getAdapter(IDocument.class);
        fBaseEditor = fEditor.getBaseEditor();
        
        Assert.isNotNull(fBaseEditor);
        
        builder = new Outliner(fBaseEditor.getParserFactory().getHRCParser().getRegion("def:Error"));
        builder.attachOutliner(fBaseEditor);
        builder.addUpdateListener(outlineListener);
        
        def_TODO = fBaseEditor.getParserFactory().getHRCParser().getRegion("def:TODO");
        def_ErrorText = fBaseEditor.getParserFactory().getHRCParser().getRegion("def:ErrorText");
        def_Debug = fBaseEditor.getParserFactory().getHRCParser().getRegion("def:Debug");

        
        new Thread(new Runnable(){
            
            Display rootDisplay = Display.getCurrent();

            public void run() {
                while(builder != null && rootDisplay != null && !rootDisplay.isDisposed())
                {
                    synchronized (builder) {
//                    rootDisplay.asyncExec(new Runnable(){
  //                      public void run() {
                        if (builder != null) updateAnnotations();
    //                    };
      //              });
                    }
                    try{
                        Thread.sleep(UPDATE_PERIOD);
                    }catch(InterruptedException e){}
                }
            }
        }, "ColorerAnnotationProvider").start();
    }

    public void uninstall() {
        synchronized (builder) {
            builder.detachOutliner(fBaseEditor);
            builder.removeUpdateListener(outlineListener);
            builder = null;
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