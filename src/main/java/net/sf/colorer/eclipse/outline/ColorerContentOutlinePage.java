package net.sf.colorer.eclipse.outline;

import java.util.Vector;

import net.sf.colorer.HRCParser;
import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.ImageStore;
import net.sf.colorer.eclipse.Messages;
import net.sf.colorer.eclipse.editors.ColorerEditor;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.OutlineListener;
import net.sf.colorer.impl.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Content outline page for the Colorer editor.
 * Allows to view General Outline, Error and Internal Parse Tree for
 * currently selected editor window. 
 */
public class ColorerContentOutlinePage extends ContentOutlinePage
                                implements OutlineListener
{
    
    Action fLinkToEditorAction = new Action("Link", Action.AS_CHECK_BOX){
        public void run() {
            prefStore.setValue("outline.link", isChecked());
        }
    };
    
    ISelectionListener thisSelectionListener = new ISelectionListener (){

        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            ISelection sel = selection;
            
            if (Logger.TRACE){
                Logger.trace("ColorerContentOutlinePage", "selectionChanged:"+selection);
            }
            
            if (!fLinkToEditorAction.isChecked() || sel == null || !(sel instanceof ITextSelection))
                return;
            int line = ((ITextSelection)sel).getStartLine();
            Object element = fActiveOutlineSource.getItemByLine(line);

            if (element == null)
                return;

            fProgrammaticChange = true;
            getTreeViewer().expandToLevel(element, 0);
            getTreeViewer().setSelection(new StructuredSelection(element), true);

            if (Logger.TRACE){
                Logger.trace("ColorerContentOutlinePage", "selected:"+element);
            }
        }
    };


    class TreeAction extends Action {

        public TreeAction() {
            super(Messages.get("outline.tree"), ImageStore.getID("outline-tree"));
            setToolTipText(Messages.get("outline.tree.tooltip"));
            setHoverImageDescriptor(ImageStore.getID("outline-tree-hover"));
            setChecked(ColorerPlugin.getDefault().getPreferenceStore().getBoolean(
                    "Outline.Hierarchy"));
        };

        public void run() {
            setChecked(isChecked());
            ColorerPlugin.getDefault().getPreferenceStore().setValue("Outline.Hierarchy",
                    isChecked());
            fActiveOutlineSource.setHierarchy(isChecked());
        }
    }

    class SortAction extends Action {

        private ViewerSorter sorter = new WorkbenchViewerSorter();

        public SortAction() {
            super(Messages.get("outline.sort"), ImageStore.getID("outline-sort"));
            setToolTipText(Messages.get("outline.sort.tooltip"));
            setHoverImageDescriptor(ImageStore.getID("outline-sort-hover"));
            setChecked(ColorerPlugin.getDefault().getPreferenceStore().getBoolean("Outline.Sort"));
            getTreeViewer().setSorter(isChecked() ? sorter : null);
        };

        public void run() {
            setChecked(isChecked());
            BusyIndicator.showWhile(getControl().getDisplay(), new Runnable(){

                public void run() {
                    getTreeViewer().setSorter(isChecked() ? sorter : null);
                }
            });
            ColorerPlugin.getDefault().getPreferenceStore().setValue("Outline.Sort", isChecked());
            fActiveOutlineSource.setSorting(isChecked());
        }
    }

    class OutlineModeAction extends Action {
        
        String id;
        IWorkbenchOutlineSource outliner;

        OutlineModeAction(String id, IWorkbenchOutlineSource outliner) {
            this.id = id;
            this.outliner = outliner;
            this.setText(Messages.get("outline.options."+id));
            this.setImageDescriptor(ImageStore.getID("outline-options-"+id));
            setChecked(fActiveOutlineSource == outliner);
        }

        public void run() {
            if (fActiveOutlineSource != outliner){
                setActiveOutliner(outliner, this);
                update();
            }
        };
    }

    IWorkbenchOutlineSource fStructureOutline, fParseTreeOutline;
    IWorkbenchOutlineSource fActiveOutlineSource;

    OutlineModeAction structureModeAction, parseTreeModeAction;
    OutlineModeAction activeAction;
    private Vector selectionListeners = new Vector();

    Thread backgroundUpdater = null;

    boolean outlineModified = true;
    long prevTime = 0;
    int UPDATE_DELTA = 2000;
    boolean fProgrammaticChange = false;

    IPreferenceStore prefStore;
    BaseEditor fBaseEditor;
    ColorerEditor fEditor;
    
    public ColorerContentOutlinePage() {
        super();
        prefStore = ColorerPlugin.getDefault().getPreferenceStore();
    }

    public void attach(ColorerEditor editor){
        detach();

        fEditor = editor;
        fBaseEditor = editor.getBaseEditor();
        HRCParser hp = fBaseEditor.getParserFactory().getHRCParser();
        
        fStructureOutline = new WorkbenchOutliner(hp.getRegion("def:Outlined"));
        fParseTreeOutline = new ParseTreeOutliner();

        structureModeAction = new OutlineModeAction("Structure", fStructureOutline);
        parseTreeModeAction = new OutlineModeAction("ParseTree", fParseTreeOutline);

        fLinkToEditorAction.setImageDescriptor(ImageStore.getID("regions-tree-link"));
        fLinkToEditorAction.setToolTipText(Messages.get("outline.link"));
        fLinkToEditorAction.setChecked(prefStore.getBoolean("outline.link"));

        setActiveOutliner(fStructureOutline, structureModeAction);

        fActiveOutlineSource.setHierarchy(ColorerPlugin.getDefault().getPreferenceStore().getBoolean(
                "Outline.Hierarchy"));
        
        fEditor.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(thisSelectionListener);

        notifyUpdate();
        backgroundUpdater = new Thread("backgroundUpdater"){
            
            Display rootDisplay = Display.getCurrent();

            public void run() {
                while (true) {
                    try {
                        sleep(UPDATE_DELTA);
                    } catch (InterruptedException e) {
                        break;
                    };
                    if (Thread.currentThread() != backgroundUpdater)
                        break;
                    rootDisplay.syncExec(new Runnable(){
                        public void run() {
                            updateIfChanged();
                        }
                    });
                };
            };
        };
        backgroundUpdater.start();
    }

    public void detach() {
        if (fEditor != null){
            fEditor.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(thisSelectionListener);
        }
        setActiveOutliner(null, null);
        backgroundUpdater = null;
        fBaseEditor = null;
        fEditor = null;
        fStructureOutline = null;
        fParseTreeOutline = null;
    };

    public void dispose() {
        detach();
        super.dispose();
    }
    
    void setActiveOutliner(IWorkbenchOutlineSource newOutliner, OutlineModeAction action){
        if (fActiveOutlineSource != null){
            fActiveOutlineSource.removeUpdateListener(this);
            if (fBaseEditor != null){
                fActiveOutlineSource.detachOutliner(fBaseEditor);
            }
            if (activeAction != null){
                activeAction.setChecked(false);
            }
        }
        fActiveOutlineSource = newOutliner;
        activeAction = action;
        if (fActiveOutlineSource != null){
            fActiveOutlineSource.addUpdateListener(this);
            fActiveOutlineSource.clear();
            if (fBaseEditor != null){
                fActiveOutlineSource.attachOutliner(fBaseEditor);
            }
            // Sync UI
            activeAction.setChecked(true);
            // invalidate whole syntax to collect new outline
            fEditor.invalidateSyntax();
        }
    }

    void update() {
        if (getControl() == null) { return; }
        if (getControl().isDisposed()) { return; }
        getControl().setRedraw(false);
        int hpos = getTreeViewer().getTree().getHorizontalBar().getSelection();
        int vpos = getTreeViewer().getTree().getVerticalBar().getSelection();
        getTreeViewer().setInput(fActiveOutlineSource);
        getTreeViewer().expandAll();
        getControl().setRedraw(true);
        getTreeViewer().getTree().getHorizontalBar().setSelection(hpos);
        getTreeViewer().getTree().getVerticalBar().setSelection(vpos);
    }

    public void updateIfChanged() {
        if (outlineModified) {
            update();
            outlineModified = false;
        };
    }

    public void notifyUpdate() {
        outlineModified = true;
        long cTime = System.currentTimeMillis();
        if (cTime - prevTime > UPDATE_DELTA && fActiveOutlineSource != null) {
            updateIfChanged();
            prevTime = System.currentTimeMillis();
        };
    }
    
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        selectionListeners.remove(listener);
    }

    public void createControl(Composite parent) {
        super.createControl(parent);

        TreeViewer viewer = getTreeViewer();

        getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged(SelectionChangedEvent event) {
                if (fProgrammaticChange) {
                    fProgrammaticChange = false;
                    return;
                }
                Object[] list = selectionListeners.toArray();
                for(int idx = list.length-1; idx >=0; idx--) {
                    ((ISelectionChangedListener)list[idx]).selectionChanged(event);
                }
            }
        });

        IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager();
        if (toolBarManager != null) {
            toolBarManager.add(new TreeAction());
            toolBarManager.add(new SortAction());
            toolBarManager.add(fLinkToEditorAction);
        };

        IMenuManager menuManager = getSite().getActionBars().getMenuManager();
        if (menuManager != null) {
            menuManager.add(structureModeAction);
            menuManager.add(parseTreeModeAction);
        }

        viewer.setContentProvider(new WorkbenchContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(fActiveOutlineSource);
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