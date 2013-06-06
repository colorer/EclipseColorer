package net.sf.colorer.editor;

import java.util.Vector;

import net.sf.colorer.Region;
import net.sf.colorer.RegionHandler;

/**
 * Basic outliner class, used to capture parser output and
 * filter out only required structure elements.
 *
 */
public class Outliner implements IOutlineSource
{
    protected Vector fOutlineStorage = new Vector();
    Region searchRegion = null;
    
    int curLevel = 0;
    int modifiedLine = -1;
    
    private boolean changed = true;
    private boolean lineIsEmpty = true;
    
    private RegionHandler fRegionHandler = new RegionHandler()
    {
        public void startParsing(int lno) {
            curLevel = 0;
        }

        public void endParsing(int lno) {
            if (modifiedLine < lno){
                modifiedLine = lno+1;
            }
            curLevel = 0;
            if (changed) {
                notifyUpdate();
                changed = false;
            }
        }

        public void clearLine(int lno, String line) {
            lineIsEmpty = true;
        }

        public void addRegion(int lno, String line, int sx, int ex, Region region) {
            if (lno < modifiedLine) {
                return;
            }
            if (!isOutlined(region)) {
                return;
            }

            String itemLabel = null;
            if (line != null)
                itemLabel = line.substring(sx, ex);

            if (lineIsEmpty) {
                fOutlineStorage.addElement(createItem(lno, sx, ex-sx, curLevel, itemLabel, region));
            } else {
                OutlineItem thisItem = (OutlineItem) fOutlineStorage.lastElement();
                if (itemLabel != null && thisItem.token != null && thisItem.lno == lno) {
                    if (itemLabel.length() > 1)
                        thisItem.token.append(" ");
                    thisItem.token.append(itemLabel);
                }
            }
            changed = true;
            lineIsEmpty = false;
        }

        public void enterScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
            curLevel++;
        }

        public void leaveScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
            curLevel--;
        }
    };

    private EditorListener fEditorListener = new EditorListener(){
        public void modifyEvent(int topLine) {
            int new_size;
            for (new_size = fOutlineStorage.size() - 1; new_size >= 0; new_size--) {
                if (((OutlineItem) fOutlineStorage.elementAt(new_size)).lno < topLine)
                    break;
            }
            fOutlineStorage.setSize(new_size + 1);
            modifiedLine = topLine;
            changed = true;
        }
    };

    public Outliner(Region searchRegion) {
        this.searchRegion = searchRegion;
    }
    
    public void attachOutliner(BaseEditor be) {
        be.addRegionHandler(fRegionHandler , searchRegion);
        be.addEditorListener(fEditorListener);
    }

    public void detachOutliner(BaseEditor be) {
        be.removeRegionHandler(fRegionHandler);
        be.removeEditorListener(fEditorListener);
    }
    
    public OutlineItem getItem(int idx) {
        return (OutlineItem) fOutlineStorage.elementAt(idx);
    }

    public int itemCount() {
        return fOutlineStorage.size();
    }

    public Region getFilter() {
        return searchRegion;
    }

    static int manageTree(Vector treeStack, int newLevel) {
        while (treeStack.size() > 0 && newLevel < ((Integer) treeStack.lastElement()).intValue())
            treeStack.removeElementAt(treeStack.size() - 1);
        if (treeStack.size() == 0 || newLevel > ((Integer) treeStack.lastElement()).intValue()) {
            treeStack.addElement(new Integer(newLevel));
            return treeStack.size() - 1;
        };
        if (newLevel == ((Integer) treeStack.lastElement()).intValue())
            return treeStack.size() - 1;
        return 0;
    }

    public boolean isOutlined(Region region) {
        return region.hasParent(searchRegion);
    }
    
    /**
     * Cleans out current outline elements.
     */
    public void clear(){
        fOutlineStorage.setSize(0);
    }

    /**
     * Creates an outline Item to 
     * @param lno
     * @param sx
     * @param length
     * @param curLevel
     * @param itemLabel
     * @param region
     * @return should return created outline item. Can be redefined.
     */
    protected OutlineItem createItem(int lno, int sx, int length, int curLevel, String itemLabel, Region region) {
        return new OutlineItem(lno, sx, length, curLevel, itemLabel, region);
    }
    
    
    // -----------------------------------------
    Vector listeners = new Vector();
    
    public void addUpdateListener(OutlineListener listener) {
        listeners.addElement(listener);
    }

    public void removeUpdateListener(OutlineListener listener) {
        listeners.removeElement(listener);
    }
    
    protected void notifyUpdate()
    {
        for (int idx = 0; idx < listeners.size(); idx++)
            ((OutlineListener) listeners.elementAt(idx)).notifyUpdate();
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
