package net.sf.colorer.viewer;

import java.io.IOException;
import java.io.Writer;

import net.sf.colorer.handlers.LineRegion;
import net.sf.colorer.handlers.StyledRegion;

/**
  Static service methods of LineRegion output.
*/
public class ParsedLineWriter {

  /** Writes given line of text using list of passed line regions.
      Formats output with class of each token, enclosed in
      \<span class='regionName'>...\</span>
      @param markupWriter Writer, used for markup output
      @param textWriter Writer, used for text output
      @param line Line of text
      @param lineRegions Linked list of LineRegion structures.
             Only region references are used there.
  */
  public static void tokenWrite(Writer markupWriter, Writer textWriter, String line, LineRegion[] lineRegions) throws IOException{
    int pos = 0;
    for(int idx = 0; idx < lineRegions.length; idx++){
      LineRegion l1 = lineRegions[idx];
      if (l1.special || l1.region == null) continue;
      if (l1.start == l1.end) continue;
      int end = l1.end;
      if (end == -1) end = line.length();
      if (l1.start > pos){
        textWriter.write(line, pos, l1.start - pos);
        pos = l1.start;
      };
      String token = l1.region.getName().replace(':', '_');
      markupWriter.write("<span class='");
      markupWriter.write(token);
      markupWriter.write("'>");
      textWriter.write(line, pos, end - l1.start);
      markupWriter.write("</span>");
      pos += end - l1.start;
    };
  };


  /** Write specified line of text using list of LineRegion's.
      This method uses text fields of LineRegion class to enwrap each line
      region.
      It uses two Writers - @c markupWriter and @c textWriter.
      @c markupWriter is used to write markup elements of LineRegion,
      and @c textWriter is used to write line content.
      @param markupWriter Writer, used for markup output
      @param textWriter Writer, used for text output
      @param line Line of text
      @param lineRegions Linked list of LineRegion structures
  */
  public static void markupWrite(Writer markupWriter, Writer textWriter, String line, LineRegion[] lineRegions) throws IOException{
    int pos = 0;
    for(int idx = 0; idx < lineRegions.length; idx++){
      LineRegion l1 = lineRegions[idx];
      if (l1.special || l1.rdef == null) continue;
      if (l1.start == l1.end) continue;
      int end = l1.end;
      if (end == -1) end = line.length();
      if (l1.start > pos){
        textWriter.write(line, pos, l1.start - pos);
        pos = l1.start;
      };
      if (l1.texted().sback != null) markupWriter.write(l1.texted().sback);
      if (l1.texted().stext != null) markupWriter.write(l1.texted().stext);
      textWriter.write(line, pos, end - l1.start);
      if (l1.texted().etext != null) markupWriter.write(l1.texted().etext);
      if (l1.texted().eback != null) markupWriter.write(l1.texted().eback);
      pos += end - l1.start;
    };
    if (pos < line.length()){
      textWriter.write(line, pos, line.length() - pos);
    };
  };


  /** Write specified line of text using list of LineRegion's.
      This method uses integer fields of LineRegion class
      to enwrap each line region with generated HTML markup.
      Each region is
      @param markupWriter Writer, used for markup output
      @param textWriter Writer, used for text output
      @param line Line of text
      @param lineRegions Linked list of LineRegion structures
  */
  public static void htmlRGBWrite(Writer markupWriter, Writer textWriter, String line, LineRegion[] lineRegions) throws IOException{
    int pos = 0;
    for(int idx = 0; idx < lineRegions.length; idx++){
      LineRegion l1 = lineRegions[idx];
      if (l1.special || l1.rdef == null) continue;
      if (l1.start == l1.end) continue;
      int end = l1.end;
      if (end == -1) end = line.length();
      if (l1.start > pos){
        textWriter.write(line, pos, l1.start - pos);
        pos = l1.start;
      };
      writeStart(markupWriter, l1.styled());
      textWriter.write(line, pos, end - l1.start);
      writeEnd(markupWriter, l1.styled());
      pos += end - l1.start;
    };
    if (pos < line.length()){
      textWriter.write(line, pos, line.length() - pos);
    };
  };

  static String hexPrint(int val, int width){
    String tv = Integer.toHexString(val);
    while (tv.length() < width){
      tv = "0"+tv; 
    };
    return tv;
  }
  
  /** Puts into stream style attributes from RegionDefine object.
  */
  public static void writeStyle(Writer writer, StyledRegion lr) throws IOException{
    StringBuffer span = new StringBuffer();
    if (lr.bfore) span.append("color:#"+ hexPrint(lr.fore, 6)+"; ");
    if (lr.bback) span.append("background:#"+ hexPrint(lr.back, 6)+"; ");
    if ((lr.style & StyledRegion.BOLD) != 0) span.append("font-weight:bold; ");
    if ((lr.style & StyledRegion.ITALIC) != 0) span.append("font-style:italic; ");
    if ((lr.style & StyledRegion.UNDERLINE) != 0) span.append("text-decoration:underline; ");
    writer.write(span.toString());
  };
  /** Puts into stream starting HTML \<span> tag with requested style specification
  */
  public static void writeStart(Writer writer, StyledRegion lr) throws IOException{
    if (!lr.bfore && !lr.bback) return;
    writer.write("<span style='");
    writeStyle(writer, lr);
    writer.write("'>");
  };
  /** Puts into stream ending HTML \</span> tag
  */
  public static void writeEnd(Writer writer, StyledRegion lr) throws IOException{
    if (!lr.bfore && !lr.bback) return;
    writer.write("</span>");
  };

}
