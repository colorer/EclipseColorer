package net.sf.colorer.eclipse.jface;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;

/**
 * Stubbed ext5 interface for source viewers without projection support
 */
public class TextViewerExt5Stub implements ITextViewerExtension5 {

    public boolean exposeModelRange(IRegion modelRange) {
        return false;
    }

    public IRegion[] getCoveredModelRanges(IRegion modelRange) {
        return null;
    }

    public IRegion getModelCoverage() {
        return null;
    }

    public int modelLine2WidgetLine(int modelLine) {
        return modelLine;
    }

    public int modelOffset2WidgetOffset(int modelOffset) {
        return modelOffset;
    }

    public IRegion modelRange2WidgetRange(IRegion modelRange) {
        return modelRange;
    }

    public int widgetLine2ModelLine(int widgetLine) {
        return widgetLine;
    }

    public int widgetLineOfWidgetOffset(int widgetOffset) {
        return widgetOffset;
    }

    public int widgetOffset2ModelOffset(int widgetOffset) {
        return widgetOffset;
    }

    public IRegion widgetRange2ModelRange(IRegion widgetRange) {
        return widgetRange;
    }

    public int widgetlLine2ModelLine(int widgetLine) {
        return widgetLine;
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