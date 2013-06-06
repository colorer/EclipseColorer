/*
 * Created on 05.02.2006
 *
 */
package net.sf.colorer.eclipse.ftpp;

import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.ImageStore;
import net.sf.colorer.eclipse.Messages;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Labels for specific parameter values
 *
 * @author Igor Russkih
 */
class TypeLabelProvider extends LabelProvider implements ITableLabelProvider{
    Image grImage = ImageStore.EDITOR_CUR_GROUP.createImage();
    Image grImageDis = ImageStore.EDITOR_GROUP.createImage();
    Image hrdImage = ImageStore.EDITOR_UPDATEHRC_A.createImage();
    Image wwImage = ImageStore.EDITOR_PAIR_SELECT.createImage();

    TypeContentProvider contentProvider;

    public TypeLabelProvider(TypeContentProvider cp){
        contentProvider = cp;
    }

    public Image getColumnImage(Object element, int columnIndex) {
        if (ColorerPlugin.HRD_SIGNATURE.equals(element)) {
            return hrdImage;
        }
        if (ColorerPlugin.WORD_WRAP_SIGNATURE.equals(element)) {
            return wwImage;
        }
        // Parameters
        int value = ColorerPlugin.getDefault().getPropertyParameter(contentProvider.type, element.toString());
        if (value == 1) {
            return grImage;
        }
        if (value == 0) {
            return grImageDis;
        }
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        if (contentProvider == null) {
            return null;
        }
        /* Value of HRD scheme */
        if (ColorerPlugin.HRD_SIGNATURE.equals(element)) {
            if (columnIndex == 0) {
                return Messages.get("ftpp.hrd_set");
            }else {
                String hrd = ColorerPlugin.getDefault().getPropertyHRD(contentProvider.type);
                String hrd_descr = null;
                if (hrd == null) hrd_descr = Messages.get("ftpp.default");
                else hrd_descr = ColorerPlugin.getDefaultPF().getHRDescription("rgb", hrd); 
                return hrd_descr;
            }
        }
        /* Word Wrap */
        if (ColorerPlugin.WORD_WRAP_SIGNATURE.equals(element)) {
            if (columnIndex == 0) {
                return Messages.get("ftpp.word_wrap");
            }else {
                int ww = ColorerPlugin.getDefault().getPropertyWordWrap(contentProvider.type);
                return getDefaultTrueFalse(ww);
            }
        }
        /* or HRC parameter value */
        if (columnIndex == 0) {
            return contentProvider.type.getParameterDescription((String)element);
        }else {
            int val = ColorerPlugin.getDefault().getPropertyParameter(contentProvider.type, element.toString());
            if (val == -1){
                String value = contentProvider.type.getParameterValue((String)element);
                if (value.equals("true")) val = 1;
                else if (value.equals("false")) val = 1;
                else return value;
            }
            return getDefaultTrueFalse(val);
        }
    }

    private String getDefaultTrueFalse(int ww) {
        if (ww == -1) return Messages.get("ftpp.default");
        if (ww == 0) return Messages.get("ftpp.false");
        if (ww == 1) return Messages.get("ftpp.true");
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
