package net.sf.colorer.eclipse.ftpp;

import java.util.Vector;

import net.sf.colorer.FileType;
import net.sf.colorer.ParserFactory;
import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.Messages;
import net.sf.colorer.impl.Logger;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Preferences page for specific HRC file type settings
 * @author Igor Russkih
 */
public class FileTypePreferencePage extends PreferencePage implements IWorkbenchPreferencePage{

    class FileTypeCellModifier implements ICellModifier {
        /**
         * Returns the current selected value - index in the cell editor choice box
         * 
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
         */
        public Object getValue(Object element, String property) {
            if (Logger.TRACE){
                Logger.trace("Preference", "getValue:element="+element.toString() + ", property=" + property);
            }

            if (element.equals(ColorerPlugin.HRD_SIGNATURE)) {
                String hrd = ColorerPlugin.getDefault().getPropertyHRD(currentType);
                return new Integer(hrdList.indexOf(hrd)+1);
            }
            if (element.equals(ColorerPlugin.WORD_WRAP_SIGNATURE)) {
                return new Integer(ColorerPlugin.getDefault().getPropertyWordWrap(currentType)+1);
            }
            // Default choice - list of parameters
            {
                int val = ColorerPlugin.getDefault().getPropertyParameter(currentType, element.toString());
                return new Integer(val);
            }
        }
        
        /**
         * Changes the cell editor items and checks if the appropriate fields are ok
         * to edit
         * 
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
         */
        public boolean canModify(Object element, String property) {
            if (Logger.TRACE){
                Logger.trace("Preference", "canModify:element="+element.toString() + ", property=" + property);
            }
            if (element.equals(ColorerPlugin.HRD_SIGNATURE)) {
                paramCellEditor.setItems(valuesHRD);
                return true;
            }
            if (element.equals(ColorerPlugin.WORD_WRAP_SIGNATURE)) {
                paramCellEditor.setItems(values_TrueFalseDefault);
                return true;
            }
            // Default choice - list of parameters
            {
                paramCellEditor.setItems(values_TrueFalse);
                String pval = typePropertiesProvider.type.getParameterDefaultValue(element.toString());
                return pval.equals("true") || pval.equals("false");
            }
        }
        
        public void modify(Object element, String property, Object value) {
            if (Logger.TRACE){
                Logger.trace("Preference", "modify: element="+element + ", property=" + property + ", value=" + value);
            }
            if (element instanceof Item) {
                element = ((Item) element).getData();
            }
            
            if (element.equals(ColorerPlugin.HRD_SIGNATURE)) {
                int i = ((Integer)value).intValue();
                if (i == 0){
                    ColorerPlugin.getDefault().setPropertyHRD(currentType, "");
                }else{
                    ColorerPlugin.getDefault().setPropertyHRD(currentType, (String)hrdList.elementAt(i-1));
                }
            }
            if (element.equals(ColorerPlugin.WORD_WRAP_SIGNATURE)) {
                int i = ((Integer)value).intValue();
                ColorerPlugin.getDefault().setPropertyWordWrap(currentType, i-1);
            }
            // Default choice - list of parameters
            {
                int i = ((Integer)value).intValue();
                ColorerPlugin.getDefault().setPropertyParameter(currentType, element.toString(), i);
            }
            typePropertiesViewer.refresh();
        }
    }
    
    class TreeViewSelection implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (selection != null && selection instanceof FileType) {
                currentType = (FileType) selection;
                typePropertiesViewer.setInput(selection);
            } else {
                currentType = null;
                typePropertiesViewer.setInput(null);
            }
            typePropertiesViewer.refresh();
        }
    }

    TypeContentProvider typePropertiesProvider;
    TableViewer typePropertiesViewer;
    Table typePropertiesTable;
    TreeViewer typeTreeViewer;
    ComboBoxCellEditor paramCellEditor;
    
    FileType currentType;
    Vector   hrdList = ColorerPlugin.getDefault().getHRDList();
    String[] values_TrueFalseDefault = new String[3];;
    String[] values_TrueFalse = new String[2];
    String[] valuesHRD;

    public FileTypePreferencePage(){
        setPreferenceStore(ColorerPlugin.getDefault().getPreferenceStore());
        values_TrueFalseDefault[0] = Messages.get("ftpp.default");
        values_TrueFalseDefault[1] = Messages.get("ftpp.false");
        values_TrueFalseDefault[2] = Messages.get("ftpp.true");
        values_TrueFalse[0] = Messages.get("ftpp.false");
        values_TrueFalse[1] = Messages.get("ftpp.true");
    }
    
    public void init(IWorkbench iworkbench){}

    public void applyData(Object data) {
        if (data != null && data instanceof FileType && typeTreeViewer != null){
            typeTreeViewer.expandAll();
            typeTreeViewer.setSelection(new StructuredSelection(data), true);
        }
    }
    
    public boolean performOk() {
        currentType = null;
        typePropertiesViewer.setInput(null);
        typeTreeViewer.setInput(null);
        ColorerPlugin.getDefault().reloadParserFactory();

        typeTreeViewer.setInput(ColorerPlugin.getDefaultPF());        
        return true;
    }
    public boolean performCancel() {
        return true;
    }
    protected void performDefaults() {
        currentType = null;
        typePropertiesViewer.setInput(null);
        typeTreeViewer.setInput(null);
        ColorerPlugin.getDefault().resetHRCParameters();
        ColorerPlugin.getDefault().reloadParserFactory();
        super.performDefaults();
        
        typeTreeViewer.setInput(ColorerPlugin.getDefaultPF());        
    }

    
    /**
     * Creates visual tree and preference page
     */
    public Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        ParserFactory pf = ColorerPlugin.getDefaultPF();
        typePropertiesProvider = new TypeContentProvider();

        {
            PatternFilter patternFilter = new PatternFilter();
            final FilteredTree filter = new FilteredTree(composite,
                    SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION,
                    patternFilter);
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.minimumHeight = 100;
            gd.grabExcessVerticalSpace = true;
            filter.setLayoutData(gd);
            
            typeTreeViewer = filter.getViewer();

            typeTreeViewer.setContentProvider(new FileTypesContentProvider());
            typeTreeViewer.setLabelProvider(new FileTypesLabelProvider());
            typeTreeViewer.setInput(pf);
            typeTreeViewer.addPostSelectionChangedListener(new TreeViewSelection());
            typeTreeViewer.addDoubleClickListener(new IDoubleClickListener(){
                public void doubleClick(DoubleClickEvent event) {
                    Object source = ((IStructuredSelection)event.getSelection()).getFirstElement();
                    ((TreeViewer)event.getViewer()).setExpandedState(source, !((TreeViewer)event.getViewer()).getExpandedState(source));
                };
            });
        }
        {
            typePropertiesViewer = new TableViewer(composite,
                    SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
            typePropertiesTable = typePropertiesViewer.getTable();
            typePropertiesViewer.setContentProvider(typePropertiesProvider);
            typePropertiesViewer.setLabelProvider(new TypeLabelProvider(typePropertiesProvider));

            TableColumn tc = new TableColumn(typePropertiesTable, SWT.LEFT);
            tc.setText(Messages.get("ftpp.Parameter"));
            tc.setWidth(300);
            tc = new TableColumn(typePropertiesTable, SWT.LEFT);
            tc.setText(Messages.get("ftpp.Value"));
            tc.setWidth(150);

            CellEditor cellEditors[] = new CellEditor[typePropertiesViewer
                    .getTable().getColumnCount()];
            paramCellEditor = new ComboBoxCellEditor(typePropertiesTable,
                    values_TrueFalseDefault, SWT.READ_ONLY);

            cellEditors[1] = paramCellEditor;
            typePropertiesViewer.setCellEditors(cellEditors);
            typePropertiesViewer.setCellModifier(new FileTypeCellModifier());

            typePropertiesViewer.setColumnProperties(new String[] { "name",
                    "value" });

            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 100;
            gd.grabExcessVerticalSpace = true;
            typePropertiesTable.setLayoutData(gd);
            
            typePropertiesTable.setHeaderVisible(true);
            typePropertiesTable.setLinesVisible(true);

            typePropertiesViewer.setInput(null);

            valuesHRD = new String[hrdList.size()+1];
            valuesHRD[0] = Messages.get("ftpp.default");
            for (int idx = 1; idx < valuesHRD.length; idx++) {
                valuesHRD[idx] = pf.getHRDescription("rgb", (String) hrdList.elementAt(idx-1));
            }
        }
        return composite;
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
 * Igor Russkih <irusskih at gmail.com>.
 * Portions created by the Initial Developer are Copyright (C) 1999-2003
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
