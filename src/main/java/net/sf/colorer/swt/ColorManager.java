package net.sf.colorer.swt;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorManager{

  private Hashtable colorsHash = new Hashtable();
  
  public int getColor(Color c){
    return c.getBlue() + (c.getGreen()<<8) +  (c.getRed()<<16);
  }
  
  public Color getColor(boolean has, int rgb){
    if (!has) return null;
    Color color = (Color)colorsHash.get(String.valueOf(rgb));
    if (color == null){
      color = new Color(Display.getCurrent(), rgb>>16, (rgb>>8)&0xFF, rgb&0xFF);
      colorsHash.put(new Integer(rgb), color);
    }
    return color;
  }
  
  public void clean(){
    for(Iterator e = colorsHash.values().iterator(); e.hasNext();)
      ((Color)e.next()).dispose();
  }

}