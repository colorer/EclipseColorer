package net.sf.colorer.eclipse.outline;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import net.sf.colorer.Region;
import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.ImageStore;
import net.sf.colorer.editor.OutlineItem;
import net.sf.colorer.editor.Outliner;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Default outliner, used to filter parse stream for the specified
 * HRC Region. Extends default Outliner, implements workbench adapter
 * to work as outliner in eclipse environment.
 */
public class WorkbenchOutliner extends Outliner
        implements IWorkbenchAdapter, IWorkbenchOutlineSource
{
    Hashtable iconsHash = new Hashtable();
    boolean hierarchy = true;

    public WorkbenchOutliner(Region filter) {
        super(filter);
    }

    public void setHierarchy(boolean hierarchy) {
        this.hierarchy = hierarchy;
        notifyUpdate();
    }
    public void setSorting(boolean sorting) {
        //nop
    }
    
    public OutlineItem createItem(int lno, int sx, int length, int curLevel, String itemLabel, Region region) {
        return new OutlineElement(this, lno, sx, length, curLevel, itemLabel, region);
    }
    

    // represents root of Outline structure
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class){
            return this;
        }
        return null;
    }

    public Object[] getChildren(Object object) {
        Vector elements = new Vector();

        if (!hierarchy) {
            if (object == this) {
                for (int idx = 0; idx < itemCount(); idx++)
                    elements.addElement(getItem(idx));
            };
        } else {
            if (object == this) {
                int flevel = 0x100000;
                for (int idx = 0; idx < itemCount(); idx++) {
                    if (flevel > getItem(idx).level)
                        flevel = getItem(idx).level;
                    if (getItem(idx).level > flevel)
                        continue;
                    elements.addElement(getItem(idx));
                }
            } else if (object instanceof OutlineElement) {
                OutlineElement el = (OutlineElement) object;
                int idx = fOutlineStorage.indexOf(el);
                if (idx > -1) {
                    int flevel = 0x100000;
                    for (idx++; idx < itemCount(); idx++) {
                        if (getItem(idx).level <= el.level)
                            break;
                        if (flevel > getItem(idx).level)
                            flevel = getItem(idx).level;
                        if (getItem(idx).level > flevel)
                            continue;
                        elements.addElement(getItem(idx));
                    }
                }
            }
        }
        return elements.toArray();
    }

    final static String defaultIconName = "outline" + File.separator + "def" + File.separator + "Outlined";
    
    public ImageDescriptor getImageDescriptor(Object object) {
        ImageDescriptor id = null;
        
        if (object instanceof OutlineElement) {
            OutlineElement el = (OutlineElement) object;
            id = (ImageDescriptor)iconsHash.get(el.region);
            if (id == null) {
                String iconName = getIconName(el.region);
                
                /*
                 * Creating custom image, based on default one.
                 */
                if (iconName == null || iconName.equals(defaultIconName)){
                    int textindex = el.region.getName().indexOf(':');
                    String text = el.region.getName().substring(textindex+1, textindex+2).toUpperCase();
                    
                    Image i = new Image(Display.getCurrent(), 16, 16);
                    GC gc = new GC(i);
                    Image def = ImageStore.getID(defaultIconName).createImage();
                    int cw = gc.getFontMetrics().getAverageCharWidth();
                    int ch = gc.getFontMetrics().getHeight();
                    gc.drawImage(def, 0, 0);
                    gc.setAlpha(220);
                    gc.setTextAntialias(SWT.ON);
                    gc.setForeground(ColorerPlugin.getDefault().getColorManager().getColor(true, 0xDDDDDD));
                    gc.drawText(text, 16-cw-2, 16-ch-2, SWT.DRAW_TRANSPARENT);
                    gc.setForeground(ColorerPlugin.getDefault().getColorManager().getColor(true, 0x106010));
                    gc.drawText(text, 16-cw-1, 16-ch-1, SWT.DRAW_TRANSPARENT);
                    id = ImageDescriptor.createFromImageData(i.getImageData());
                    def.dispose();
                    gc.dispose();
                }else{
                    id = ImageStore.getID(iconName);
                }
                iconsHash.put(el.region, id);
            }
        }
        return id;
    }

    String getIconName(Region region) {
        if (region == null)
            return null;
        
        String iconName = null;
        
        for (; region != null; region = region.getParent())
        {
            iconName = "outline" + File.separator + region.getName().replace(':', File.separatorChar);
            ImageDescriptor id = ImageStore.getID(iconName);
            if (id != null) {
                break;
            }else{
                iconName = null;
            }
        }
        return iconName;
    }

    public String getLabel(Object object) {
        if (object instanceof OutlineElement) {
            OutlineElement el = (OutlineElement) object;
            return el.token.toString();
        }
        return null;
    }

    public Object getParent(Object o) {
        return null;
    }

    public Object getItemByLine(int line) {
        int s = -1;
        int e = itemCount();
        OutlineItem found = null;
        for (int idx = s+(e-s)/2; e-s > 1; idx = s+(e-s)/2) {
            OutlineItem item = getItem(idx);
            if (item.lno == line) return item;
            if (item.lno > line) {
                e = idx;
                continue;
            }
            if (item.lno < line) {
                found = item;
                s = idx;
                continue;
            }
        }
        return found;
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