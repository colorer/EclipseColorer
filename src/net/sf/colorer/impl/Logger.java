package net.sf.colorer.impl;

import java.util.Calendar;
import java.util.Hashtable;

/**
 * Logging Service class
 */
public class Logger {

    public final static boolean FILTER = true;

    public final static boolean TRACE = false;
    
    public final static boolean ERROR = true; 
    
    public static void trace(String comp, Object msg) {
        trace(comp, msg, null);
    }

    public static void error(String comp, Object msg) {
        error(comp, msg, null);
    }

    public static void trace(String comp, Object msg, Throwable ex) {
        if (TRACE) {
            
            if (FILTER && filterout(comp)) return;
            
            String print = "null";
            if (msg != null) {
                print = msg.toString();
            }
            System.out.println("[J]["+comp+"] "+print+" ["+getTimestamp()+"]");
            if (ex != null) {
                ex.printStackTrace(System.out);
            }
        }
    }

    public static void error(String comp, Object msg, Throwable ex) {
        if (ERROR) {
            String print = "null";
            if (msg != null) {
                print = msg.toString();
            }
            System.out.println("[J]["+comp+"] "+print+" ["+getTimestamp()+"]");
            if (ex != null) {
                ex.printStackTrace(System.out);
            }
        }
    }
    
    static String getTimestamp(){
        Calendar date = Calendar.getInstance();
        return date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" +
               date.get(Calendar.SECOND) + "." + date.get(Calendar.MILLISECOND);
    }
    
    // true - removes matches
    final static boolean FILTEROUT = true;
    
    private static boolean filterout(String comp) {
        if (filter.get(comp) != null) {
            return FILTEROUT;
        }else {
            return !FILTEROUT;
        }
    }

    static Hashtable filter = new Hashtable();
    
    static {
        //filter.put("RegionsTree", filter);
        //filter.put("BaseEditor", filter);
        //filter.put("ColorerEditor", filter);
        //filter.put("TextColorer", filter);
        //filter.put("FoldingReciever", filter);
        
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