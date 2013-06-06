package net.sf.colorer.eclipse;

import java.net.URL;
import java.util.Hashtable;

import net.sf.colorer.impl.Logger;

import org.eclipse.jface.resource.ImageDescriptor;

public class ImageStore {

    static final URL BASE_URL = ColorerPlugin.getDefault().getBundle().getEntry("/");

    static Hashtable hash = new Hashtable();

    public static final ImageDescriptor EDITOR_UPDATEHRC;
    public static final ImageDescriptor EDITOR_UPDATEHRC_A;
    public static final ImageDescriptor EDITOR_FILETYPE;
    public static final ImageDescriptor EDITOR_FILETYPE_A;
    public static final ImageDescriptor EDITOR_CUR_FILETYPE;
    public static final ImageDescriptor EDITOR_CUR_GROUP;
    public static final ImageDescriptor EDITOR_GROUP;
    public static final ImageDescriptor EDITOR_PAIR_MATCH;
    public static final ImageDescriptor EDITOR_PAIR_SELECT;
    public static final ImageDescriptor EDITOR_PAIR_SELECTCONTENT;

    static String iconPath = "icons/";
    static String prefix = iconPath;

    static {
        EDITOR_UPDATEHRC = createImageDescriptor(prefix + "updatehrc.gif");
        EDITOR_UPDATEHRC_A = createImageDescriptor(prefix + "updatehrc_a.gif");
        EDITOR_FILETYPE = createImageDescriptor(prefix + "filetype.gif");
        EDITOR_FILETYPE_A = createImageDescriptor(prefix + "filetype_a.gif");

        EDITOR_CUR_FILETYPE = createImageDescriptor(prefix + "filetype/filetype.current.gif");
        EDITOR_CUR_GROUP = createImageDescriptor(prefix + "filetype/group.current.gif");
        EDITOR_GROUP = createImageDescriptor(prefix + "filetype/group.gif");

        EDITOR_PAIR_MATCH = createImageDescriptor(prefix + "pair-match.gif");
        EDITOR_PAIR_SELECT = createImageDescriptor(prefix + "pair-select.gif");
        EDITOR_PAIR_SELECTCONTENT = createImageDescriptor(prefix + "pair-select-content.gif");
    }

    private static ImageDescriptor createImageDescriptor(String path) {
        URL url = null;
        try {
            url = new URL(BASE_URL, path);
            // check this url
            url.openStream().close();
            return ImageDescriptor.createFromURL(url);
        } catch (Exception e) {
            Logger.trace("ImageStore", "Can't open URL: "+url);
        }
        return null;
    }

    public static ImageDescriptor getID(String name) {
        ImageDescriptor id = (ImageDescriptor) hash.get(name);
        if (id == null) {
            id = createImageDescriptor(prefix + name + ".gif");
            if (id == null)
                return null;
            hash.put(name, id);
        }
        ;
        return id;
    };
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