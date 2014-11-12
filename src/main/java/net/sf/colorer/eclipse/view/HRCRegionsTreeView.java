package net.sf.colorer.eclipse.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.sf.colorer.FileType;
import net.sf.colorer.HRCParser;
import net.sf.colorer.Region;
import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.ImageStore;
import net.sf.colorer.eclipse.Messages;
import net.sf.colorer.eclipse.PreferencePage;
import net.sf.colorer.eclipse.jface.IColorerEditorAdapter;
import net.sf.colorer.eclipse.jface.TextColorer;
import net.sf.colorer.handlers.LineRegion;
import net.sf.colorer.handlers.RegionMapper;
import net.sf.colorer.handlers.StyledRegion;
import net.sf.colorer.impl.Logger;
import net.sf.colorer.swt.dialog.ResourceManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

/**
 * This view represents all HRC regions in a tree-like form,
 * allowing to navigate them and discover their relation.
 */
public class HRCRegionsTreeView extends ViewPart implements IPropertyChangeListener {

    private Composite composite;
    private TreeViewer treeViewer;
    
    private HRCParser hrcParser;

    private Action refreshAction, loadAllAction, linkToEditorAction;
    
    private IPreferenceStore prefStore;
    
    RegionMapper regionMapper;
    StyledRegion def_Text;
    Label fg_label, bg_label;
    Color foreColor, backColor;

    ISelectionListener thisSelectionListener = new ISelectionListener (){
        public void selectionChanged(org.eclipse.ui.IWorkbenchPart part,ISelection selection){
            TextColorer activeEditor = null;
            if (Logger.TRACE){
                Logger.trace("RegionsTree", "selection changed:"+part);
            }
            if (part instanceof IColorerEditorAdapter && linkToEditorAction.isChecked()){
                activeEditor = ((IColorerEditorAdapter)part).getTextColorer();
                LineRegion lr = activeEditor.getCaretRegion();
                if (lr == null || lr.region == null) return;
                treeViewer.expandToLevel(lr.region, 0);
                treeViewer.setSelection(new StructuredSelection(lr.region), true);
    
                if (Logger.TRACE){
                    Logger.trace("RegionsTree", "selected:"+lr.region);
                }
            }
        }
    };

    /**
     * The view constructor.
     */
    public HRCRegionsTreeView() {
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        
        prefStore = ColorerPlugin.getDefault().getPreferenceStore();
        prefStore.addPropertyChangeListener(this);

        composite = createComposite(parent);
        makeActions();
        contributeToActionBars();

        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(thisSelectionListener);

        propertyChange(null);
    }

    public void propertyChange(PropertyChangeEvent event) {
        
        if (regionMapper != null){
            regionMapper.dispose();
            regionMapper = null;
        }
        try{
            regionMapper = ColorerPlugin.getDefaultPF().
                createStyledMapper(StyledRegion.HRD_RGB_CLASS,
                        prefStore.getString(PreferencePage.HRD_SET));
        }catch(Exception e){
            if (Logger.ERROR){
                Logger.error("HRCRegionsTreeView", "createStyledMapper:", e);
            }
        }
        
        def_Text = (StyledRegion)regionMapper.getRegionDefine("def:Text");
        
    }

    Composite createComposite(Composite parent){
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        {
            final Composite composite_1 = new Composite(composite, SWT.NONE);
            composite_1.setLayout(new GridLayout());
            composite_1.setLayoutData(new GridData(GridData.FILL_BOTH));

            PatternFilter patternFilter = new PatternFilter();
            final FilteredTree filter = new FilteredTree(composite_1,
                    SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
                    patternFilter);
            filter.setLayoutData(new GridData(GridData.FILL_BOTH));
            
            treeViewer = filter.getViewer();

            treeViewer.setContentProvider(new RegionContentProvider());
            treeViewer.setLabelProvider(new RegionTreeLabelProvider());

            hrcParser = ColorerPlugin.getDefaultPF().getHRCParser();
            treeViewer.setInput(hrcParser);

            treeViewer.addDoubleClickListener(new IDoubleClickListener(){
                public void doubleClick(DoubleClickEvent event) {
                    Region sel = getSelectedRegion(event.getSelection());
                    if (sel == null){
                        return;
                    }
                    Logger.trace("doubleClick", sel);
                    if (treeViewer.getExpandedState(sel)){
                        treeViewer.collapseToLevel(sel, 1);
                    }else{
                        treeViewer.expandToLevel(sel, 1);
                    }
                    
                }
            });

            treeViewer.addPostSelectionChangedListener(new ISelectionChangedListener(){
                public void selectionChanged(SelectionChangedEvent event) {
                    Region sel = getSelectedRegion(event.getSelection());
                    if (sel == null){
                        return;
                    }

                    StyledRegion sr = (StyledRegion)regionMapper.getRegionDefine(sel);

                    foreColor = ResourceManager.newColor(def_Text.fore);
                    backColor = ResourceManager.newColor(def_Text.back);

                    if (sr != null){
                        if (sr.bfore){
                            foreColor.dispose();
                            foreColor = ResourceManager.newColor(sr.fore);
                        }
                        if (sr.bback){
                            backColor.dispose();
                            backColor = ResourceManager.newColor(sr.back);
                        }
                    }

                    fg_label.setForeground(backColor);
                    fg_label.setBackground(foreColor);

                    bg_label.setForeground(foreColor);
                    bg_label.setBackground(backColor);
                    
                }
            });
            
        }
        
        {
            final Composite composite_1 = new Composite(composite, SWT.BORDER);
            composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            final GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 3;
            composite_1.setLayout(gridLayout);
            {
                fg_label = new Label(composite_1, SWT.BORDER | SWT.CENTER | SWT.VERTICAL);
                final GridData gridData = new GridData(GridData.GRAB_HORIZONTAL
                        | GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
                gridData.verticalSpan = 2;
                gridData.heightHint = 30;
                fg_label.setLayoutData(gridData);
                fg_label.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                fg_label.setText("Foreground");
            }
            {
                bg_label = new Label(composite_1, SWT.BORDER | SWT.CENTER | SWT.VERTICAL);
                bg_label.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                final GridData gridData = new GridData(GridData.GRAB_HORIZONTAL
                        | GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
                gridData.verticalSpan = 2;
                gridData.heightHint = 30;
                bg_label.setLayoutData(gridData);
                bg_label.setText("Background");
            }
/*            {
                final Button button = new Button(composite_1, SWT.NONE);
                button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
                final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
                gridData.verticalSpan = 2;
                button.setLayoutData(gridData);
                button.setText("<- restore");
            }
            {
                final Label label = new Label(composite_1, SWT.BORDER);
                label.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
                final GridData gridData = new GridData(GridData.GRAB_HORIZONTAL
                        | GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
                gridData.verticalSpan = 2;
                label.setLayoutData(gridData);
                label.setText("stored color");
            }
*/            
        }
/*
        {
            final Button button = new Button(composite, SWT.NONE);
            button.setText("Save HRD As...");
            button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
        }
*/        

        return composite;
    }
    
    Region getSelectedRegion(ISelection selection){
        if (selection instanceof StructuredSelection){
            StructuredSelection sel = (StructuredSelection)selection;
            if (sel.isEmpty()){
                return null;
            }
            Region region = (Region)sel.getFirstElement();
            return region;
        }
        return null;
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        //fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(refreshAction);
        manager.add(loadAllAction);
        manager.add(linkToEditorAction);
    }

    private void makeActions() {

        refreshAction = new Action("Refresh", ImageStore.getID("regions-tree-refresh"))
        {
            public void run() {
                hrcParser = ColorerPlugin.getDefaultPF().getHRCParser();
                treeViewer.setInput(hrcParser);
            }
        };
        refreshAction.setToolTipText(Messages.get("regions-tree.refresh"));
        
        loadAllAction = new Action("Load All", ImageStore.getID("regions-tree-loadall"))
        {
            public void run() {
                BusyIndicator.showWhile(treeViewer.getTree().getDisplay(), new Runnable(){
                    public void run() {
                        for(Enumeration e = hrcParser.enumerateFileTypes(); e.hasMoreElements();){
                            FileType ft = (FileType)e.nextElement();
                            ft.getBaseScheme();
                        }
                        refreshAction.run();
                    }
                });
            }
        };
        loadAllAction.setToolTipText(Messages.get("regions-tree.loadall"));
        
        linkToEditorAction = new Action("Link", Action.AS_CHECK_BOX){
            public void run() {
                prefStore.setValue("RegionsTree.Link", isChecked());
            }
        };
        linkToEditorAction.setImageDescriptor(ImageStore.getID("regions-tree-link"));
        linkToEditorAction.setToolTipText(Messages.get("regions-tree.link"));
        linkToEditorAction.setChecked(prefStore.getBoolean("RegionsTree.Link"));
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        composite.setFocus();
    }
    
    public void dispose() {
        if (foreColor != null){
            foreColor.dispose();
        }
        if (backColor != null){
            backColor.dispose();
        }
        regionMapper.dispose();
        getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(thisSelectionListener);
        prefStore.removePropertyChangeListener(this);
        super.dispose();
    }
}


class RegionContentProvider implements ITreeContentProvider {

    HRCParser hp;
    
    Hashtable children = new Hashtable();
    
    public void dispose() {
        children = null;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        hp = (HRCParser)newInput;
        children.clear();
    }

    public Object[] getElements(Object inputElement) {
        children.clear();
        HRCParser hp = (HRCParser)inputElement;
        if (inputElement instanceof HRCParser){
            Vector list = new Vector();
            int idx = 1;
            while(true){
                Region region = hp.getRegion(idx);
                Logger.trace("RegionsTree", "Region: "+region);
                if (region == null){
                    break;
                }
                if (region.getParent() == null){
                    list.add(region);
                }else{
                    addChild(region.getParent(), region);
                }
                idx++;
            }
            return list.toArray();
        }else{
            return null;
        }
    }
    
    void addChild(Region parent, Region child){
        Vector list = (Vector)children.get(parent);
        if (list == null){
            list = new Vector();
            children.put(parent, list);
        }
        list.add(child);
    }

    public Object[] getChildren(Object parentElement) {
        Vector list = (Vector)children.get(parentElement);
        if (list == null){
            return null;
        }else{
            return list.toArray();
        }
    }

    public Object getParent(Object element) {
        return ((Region)element).getParent();
    }

    public boolean hasChildren(Object element) {
        Vector list = (Vector)children.get(element);
        return list != null;
    }
}


class RegionTreeLabelProvider extends LabelProvider {
    
    public String getText(Object element) {
        if (element instanceof Region){
            Region reg = (Region)element;
            if (reg.getDescription() == null){
                return reg.getName();
            }else{
                return reg.getName() + " - " + reg.getDescription();
            }
        }else{
            return super.getText(element);
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