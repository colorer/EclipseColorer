package net.sf.colorer.editor;

import java.util.Stack;

import net.sf.colorer.Region;
import net.sf.colorer.RegionHandler;
import net.sf.colorer.impl.Logger;

/**
 * Folding Builder allows to collect folding related information from
 * colorer's parser stream.
 * 
 * Folding is based on scheme hierarchy, can also take into account
 * outline items.
 */
public class FoldingBuilder {

    public class FoldingElement {

        int s_line;
        int s_offset;
        String scheme;

        FoldingElement(int line, int offset, String scheme){
            this.scheme = scheme;
            s_line = line;
            s_offset = offset;
        }
    }

	class InternalRegionHandler implements RegionHandler, EditorListener {
        
        Stack schemeStack = new Stack();
        private int fLastLine, fFirstLine;
        private int compensateReparse;
        private boolean compensationRun;
        
        public void addRegion(int lno, String line, int sx, int ex, Region region) {
        }

        public void clearLine(int lno, String line) {
        }

        public void startParsing(int lno) {
            //schemeVector.setSize(0);
            compensateReparse = 0;
            compensationRun = true;
        }
        public void endParsing(int lno) {
        }

        public void enterScheme(int lno, String line, int sx, int ex,
                Region region, String scheme)
        {
            if (compensationRun && compensateReparse < schemeStack.size()){
                compensateReparse++;
            }else{
                compensationRun = false;
                schemeStack.push(new FoldingElement(lno, sx, scheme));
            }
        }

        /** 
         * @see net.sf.colorer.RegionHandler#leaveScheme(int, java.lang.String, int, int, net.sf.colorer.Region, java.lang.String)
         */
        public void leaveScheme(int lno, String line, int sx, int ex,
                Region region, String scheme)
        {
            if (compensationRun) {
                //Logger.error("FoldingBuilder", "leaveScheme: compensateReparse >0 !!!");
                compensationRun = false;
            }

            FoldingElement last = (FoldingElement)schemeStack.pop();
            
            if (!schemeStack.empty() &&
                    last.s_line == ((FoldingElement)schemeStack.peek()).s_line) return;
            
            if (last.s_line < lno && last.s_line < lno-fThreshold) {
                fReciever.notifyFoldingItem(last.s_line, last.s_offset, lno, ex, last.scheme);
                fLastLine = lno;
            }
        }

        //----------------------------------------------
        
        public void modifyEvent(int topLine) {
            fReciever.notifyInvalidate(topLine);
            fLastLine = -1;
            fFirstLine = -1;
        }
    }

	private BaseEditor fBaseEditor;
    private InternalRegionHandler fHandler = new InternalRegionHandler();
    private IFoldingReciever fReciever;
    private int fThreshold = 1;
    
    
    public void getFoldingItems() {
        //
    }

    /**
     * Sets the folding items minimum separation.
     * 
     * @param linesThreshold In lines. By default threshold is 1, which
     * means two lines can both contain folding items.
     */
    public void setThreshold(int linesThreshold) {
        fThreshold = linesThreshold;
    }
    
    /**
     * Installs this builder over specified BaseEditor
     */
    public void install(BaseEditor baseEditor, IFoldingReciever reciever) {
        fBaseEditor = baseEditor;
        fReciever = reciever;
        fBaseEditor.addRegionHandler(fHandler,
                fBaseEditor.getParserFactory().getHRCParser().getRegion("def:EmbeddedTag"));
        fBaseEditor.addEditorListener(fHandler);
    }
    
    /**
     * Stops this builder, no folding structure is built anymore.
     */
    public void uninstall() {
        fBaseEditor.removeRegionHandler(fHandler);
        fBaseEditor.removeEditorListener(fHandler);
        fBaseEditor = null;
        fReciever = null;
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