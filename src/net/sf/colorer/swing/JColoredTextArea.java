package net.sf.colorer.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Hashtable;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import net.sf.colorer.FileType;
import net.sf.colorer.LineSource;
import net.sf.colorer.ParserFactory;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.handlers.StyledRegion;
import net.sf.colorer.impl.BaseEditorNative;

/**
 * 
 * NOT YET IMPLEMENTED.
 *  
 * Superclass of swing JTextArea.
 * Uses colorer library to make syntax highlighting.
 * http://colorer.sf.net/
 */
public class JColoredTextArea extends JTextArea{

  private static final long serialVersionUID = -1316654352760531864L;
    
  private Hashtable colorsHash = new Hashtable();
  private BaseEditor baseEditor;

  /**
   * Common StyledText creation constructor.
   * @see org.eclipse.swt.widgets.Control#Control(Composite, int)
   */
  public JColoredTextArea(){
    super();

    LineSource lineSource = new LineSource(){
      public String getLine(int lno){
        String line = null;
        try{
          int start = getLineStartOffset(lno);
          int end = getLineEndOffset(lno);
          line = getText(start, end-start);
        }catch(BadLocationException e){};
        return line;
      };
    };
    ParserFactory pf = new ParserFactory();
    baseEditor = new BaseEditorNative(pf, lineSource);
    baseEditor.setRegionCompact(true);
    updateParameters();

/*    LineStyleListener lineStyleListener = new LineStyleListener(){
      public void lineGetStyle(LineStyleEvent e){
        updateParameters();
        int lno = text.getLineAtOffset(e.lineOffset);
        LineRegion lr = editColorer.getLineRegions(lno);
        Vector styles = new Vector();
        for(;lr != null; lr = lr.next){
          if (lr.special) continue;
          StyledRegion rdef = (StyledRegion)lr.rdef;
          if (rdef == null) continue;
          int end = lr.end;
          if (end == -1) end = text.getContent().getLine(lno).length();
          end = end - lr.start;
          StyleRange sr = new StyleRange(e.lineOffset+lr.start, end,
                              getColor(rdef.bfore, rdef.fore), getColor(rdef.bback, rdef.back),
                              rdef.style);
          styles.addElement(sr);
        };
        e.styles = (StyleRange[]) styles.toArray(new StyleRange[]{});
      }
    };
    ExtendedModifyListener extendedModifyListener = new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent e) {
        int lno = text.getLineAtOffset(e.start);
        editColorer.modifyEvent(lno);
        updateParameters();
        text.redraw();
      }
    };
    ControlListener controlListener = new ControlListener() {
      public void controlMoved(ControlEvent e) {
      };
      public void controlResized(ControlEvent e) {
        updateParameters();
      };
    };
    text.addLineStyleListener(lineStyleListener);
    text.addExtendedModifyListener(extendedModifyListener);
    text.addControlListener(controlListener);

    ScrollBar sb = text.getVerticalBar();
    if (sb != null){
      sb.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
          updateParameters();
        };
        public void widgetDefaultSelected(SelectionEvent e) {}
      });
    };
  */
//    getUI().
  };


  void updateParameters(){
/*    editColorer.lineCountEvent(text.getLineCount());
    int start = text.getTopIndex()-1;
    if (start < 0) start = 0;
    int end = start + text.getClientArea().height / text.getLineHeight();
    editColorer.visibleTextEvent(start, end-start+1);
*/
  }

  /**
   * Selects and installs coloring style (filetype) according
   * to filename string and current first line of text.
   *
   * @param filename File name to be used to autodetect filetype
   */
  public void chooseFileType(String filename){
    int count = getDocument().getLength();
    if (count > 300) count = 300;
    try{
      String fline = getText(0, count-1);
      baseEditor.chooseFileType(filename);
    }catch(BadLocationException e){};
  };

  /**
   * Selects and installs specified file type.
   *
   * @param typename Name or description of HRC filetype.
   */
  public void setFileType(FileType typename){
    baseEditor.setFileType(typename);
  };

  /**
   * Changes style/color scheme into one, specified with 'name' paramenter
   * 
   * @param name Name of color scheme (HRD name)
   */
    public void setColoringStyle(String name) throws IllegalArgumentException {
        baseEditor.setRegionMapper("rgb", name);
        StyledRegion sr = (StyledRegion) baseEditor.getBackground();
        setForeground(getColor(sr.bfore, sr.fore));
        setBackground(getColor(sr.bback, sr.back));
    };

  public void paint(Graphics g){
    int idx = 0;
    super.paint(g);
  };

  private Color getColor(boolean has, int rgb){
    if (!has){
      return null;
    }
    Color color = (Color)colorsHash.get(new Integer(rgb));
    if (color == null){
      color = new Color(rgb>>16, (rgb>>8)&0xFF, rgb&0xFF);
      colorsHash.put(new Integer(rgb), color);
    }
    return color;
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