package net.sf.colorer.eclipse.outline;

import java.util.Stack;
import java.util.Vector;

import net.sf.colorer.Region;
import net.sf.colorer.RegionHandler;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.editor.EditorListener;
import net.sf.colorer.editor.OutlineItem;
import net.sf.colorer.editor.OutlineListener;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * Object used to store parse tree and showup it in Outline view 
 */
public class ParseTreeOutliner implements IWorkbenchOutlineSource,
                            RegionHandler, EditorListener, IWorkbenchAdapter {

    Vector listeners = new Vector();
    Vector regionsList = new Vector();
    Vector cList = regionsList;

    Stack schemeStack = new Stack();
    
    boolean changed = false;
    int modifiedLine = -1;
    int curLevel;

    public void clear() {
        clear(regionsList, 0);
    }

    public void clear(Vector list, int topLine) {
        int csize = 0;
        while(csize < list.size() &&
                ((OutlineElement)(list.elementAt(csize))).lno < topLine)
        {
            csize++;
        }
        list.setSize(csize);
    }

    public void startParsing(int lno) {
        curLevel = 0;
        schemeStack.clear();
        cList = regionsList;
    }

    public void endParsing(int lno) {
        curLevel = 0;
        if (modifiedLine < lno){
            modifiedLine = lno+1;
        }        
        if (changed) {
            notifyUpdate();
            changed = false;
        }
    }

    public void clearLine(int lno, String line) {
    }

    public void addRegion(int lno, String line, int sx, int ex, Region region) {
        if (lno < modifiedLine) {
            return;
        }
        cList.addElement(new OutlineElement(this, lno, sx, ex-sx, curLevel, null, region));
        changed = true;
    }

    public void enterScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
        curLevel++;

        OutlineSchemeElement sel = null;
        Vector elements = null;

        if (cList.size() > 0 && cList.lastElement() instanceof OutlineSchemeElement){
            sel = (OutlineSchemeElement)cList.lastElement();
            /* determine, if this is a last entered schema, or a new one */
            if (sel.level != curLevel){
                sel = null;
            }else if (sel.lno == lno && sel.pos <= sx){
                sel = null;
            }else if (sel.l2no != 0 && sel.l2no < lno){
                sel = null;
            }
            if (sel != null){
                elements = sel.elements;
                clear(elements, lno);
            }
        }
        if (sel == null || elements == null){
            elements = new Vector();
            sel = new OutlineSchemeElement(this, lno, sx, ex-sx, curLevel, scheme, region, elements);
            cList.addElement(sel);
        }
        sel.l2no = sel.pos2 = 0;
        schemeStack.push(cList);
        cList = elements;
    }

    public void leaveScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
        curLevel--;
        cList = (Vector)schemeStack.pop();
        
        if (cList.size() > 0 && (cList.lastElement() instanceof OutlineSchemeElement)){
            OutlineSchemeElement lastScheme = (OutlineSchemeElement)cList.lastElement();
            lastScheme.l2no = lno;
            lastScheme.pos2 = ex;
        }
    }

    public void modifyEvent(int topLine) {
        clear(regionsList, topLine);
        modifiedLine = topLine;
        changed = true;
    }

    public Object[] getChildren(Object o) {
        if (o == this) {
            return regionsList.toArray();
        }
        if (o instanceof OutlineSchemeElement) { 
            return ((OutlineSchemeElement)o).elements.toArray();
        }
        return new Object[] {};
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    public String getLabel(Object o) {
        if (o instanceof OutlineSchemeElement) {
            StringBuffer scheme = ((OutlineSchemeElement)o).token;
            return "scheme: " + scheme;
        }
        return ((OutlineItem)o).region.getName();
    }

    public Object getParent(Object o) {
        return null;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class){
            return this;
        }
        return null;
    }

    public void setHierarchy(boolean hierarchy) {
    }
    public void setSorting(boolean sorting) {
    }

    public void attachOutliner(BaseEditor editor) {
        editor.addRegionHandler(this, null);
        editor.addEditorListener(this);
    }
    public void detachOutliner(BaseEditor editor) {
        editor.removeRegionHandler(this);
        editor.removeEditorListener(this);
    }

    public void addUpdateListener(OutlineListener listener) {
        listeners.addElement(listener);
    }

    public void removeUpdateListener(OutlineListener listener) {
        listeners.removeElement(listener);
    }
    
    protected void notifyUpdate(){
        for (int idx = 0; idx < listeners.size(); idx++)
            ((OutlineListener) listeners.elementAt(idx)).notifyUpdate();
    }

    public Object getItemByLine(int line) {
        // TODO Auto-generated method stub
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