package net.sf.colorer.editor;

import net.sf.colorer.handlers.LineRegion;

public class PairMatch{
  public LineRegion start;
  /** Region's end position */
  public LineRegion end;
  /** Starting Line of pair */
  public int sline;
  /** Ending Line of pair */
  public int eline;
  /** Identifies initial position of cursor in pair */
  public boolean topPosition;
  public int pairBalance;
  
  public PairMatch(LineRegion start, LineRegion end, int sline, int eline, int pairBalance, boolean topPosition){
    this.start = start;
    this.end = end;
    this.sline = sline;
    this.eline = eline;
    this.topPosition = topPosition;
    this.pairBalance = pairBalance;
  }
  public String toString(){
    return "s="+start+"; e="+end+"; sl="+sline+"; el="+eline;
  }
  public boolean equals(Object obj) {
    if (obj == this) return true;
    else if (obj instanceof PairMatch){
      PairMatch pm = (PairMatch)obj;
      if (pm.start == start && pm.end == end) return true;
      else return false;
    }else return super.equals(obj);
  }

  
};
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