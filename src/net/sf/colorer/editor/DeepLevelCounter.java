package net.sf.colorer.editor;

import java.util.Vector;

import org.eclipse.core.runtime.Assert;

import net.sf.colorer.Region;

/**
 * DeepLevelCounter could be used to calculate 'weight' of each line in the parsed file.
 * Weight is about the level of scheme inclusion, this particular line is.
 * Could be used for level visualization.
 */
public class DeepLevelCounter {

    private Vector lineVector = new Vector();
    private BaseEditor fBaseEditor;
    private int fDeepLevel;
    private int fMaxDeepLevel = 1;
    
    /*
     * Installs this DeepLevelCounter over the specified base editor
     */
    public void install(BaseEditor baseEditor) {
        fBaseEditor = baseEditor;
        // TODO: Using quite a rare region to prevent invocation of addRegion() at all
        fBaseEditor.addRegionHandler(fRegionHandler,
                fBaseEditor.getParserFactory().getHRCParser().getRegion("def:EmbeddedTag"));
        fMaxDeepLevel = 1;
    }
    
    /*
     * Uninstalls this DeepLevelCounter from the base editor
     */
    public void uninstall() {
        if (fBaseEditor != null) {
            fBaseEditor.removeRegionHandler(fRegionHandler);
            fBaseEditor = null;
        }
    }
    
    /*
     * Request current level from the line number
     */
    public int getLineDeepLevel(int lineNo) {
        if (lineNo < 0 || lineNo >= lineVector.size()){
            return 0;
        }
        Integer deepLevel = (Integer)lineVector.elementAt(lineNo);
        Assert.isNotNull(deepLevel);
        return deepLevel.intValue();
    }
    
    /*
     * maximum reached level for the parse
     */
    public int getMaxDeepLevel() {
        return fMaxDeepLevel;
    }
    
    private RegionHandler fRegionHandler = new RegionHandler();

    private class RegionHandler implements net.sf.colorer.RegionHandler {
        
        public void addRegion(int lno, String line, int sx, int ex, Region region) {
        }

        public void clearLine(int lno, String line) {
            ensureCapacity(lno);
            lineVector.setElementAt(new Integer(fDeepLevel), lno);
        }

        public void startParsing(int lno) {
            fDeepLevel = 0;
        }
        public void endParsing(int lno) {
            ensureCapacity(lno);
        }

        public void enterScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
            ensureCapacity(lno);
            fDeepLevel++;
            fMaxDeepLevel = Math.max(fDeepLevel, fMaxDeepLevel);
            // check fake enterScheme calls
            if (sx == 0 && ex == 0) return;
            lineVector.setElementAt(new Integer(fDeepLevel), lno);
        }

        public void leaveScheme(int lno, String line, int sx, int ex, Region region, String scheme) {
            ensureCapacity(lno);
            fDeepLevel--;
            lineVector.setElementAt(new Integer(fDeepLevel), lno);
        }
        
        private void ensureCapacity(int lno) {
            int oldSize = lineVector.size();
            if (oldSize <= lno) {
                Integer lastValue;
                if (oldSize > 0) {
                    lastValue = (Integer)lineVector.lastElement();
                }else{
                    lastValue = new Integer(0);
                }
                lineVector.setSize(lno+1);
                for (int idx = oldSize; idx < lineVector.size(); idx++) {
                    lineVector.setElementAt(lastValue, idx);
                }
            }
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