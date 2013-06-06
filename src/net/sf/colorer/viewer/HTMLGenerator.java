/*
 * @version $Revision$ $Date$
 *
 */
package net.sf.colorer.viewer;

import java.io.IOException;
import java.io.Writer;

import net.sf.colorer.ParserFactory;
import net.sf.colorer.editor.BaseEditor;
import net.sf.colorer.handlers.StyledRegion;
import net.sf.colorer.impl.BaseEditorNative;
import net.sf.colorer.impl.ReaderLineSource;

/**
 * Generates colourised HTML output from input stream,
 * forwards output into output stream.
 * 
 * @author irusskih
 */
public class HTMLGenerator {

  ReaderLineSource lineSource;
  String hrdSchema;
  ParserFactory pf;
  
  /**
   * Constructor, used to pass initial common parameters of generation process
   * @param pf ParserFactory object, used to obtain all parsing resources
   * @param input LineSource input object, representing input source text, used for parsing
   * @param hrd Name of a color scheme, used to highlight text and transform it into HTML form
   *   
   */
  public HTMLGenerator(ParserFactory pf, ReaderLineSource input, String hrd){
    lineSource = input;
    hrdSchema = hrd;
    this.pf = pf;
  }
  
  /**
   * Common method, which generates HTML representation of the full source text and
   * writes it into the passed writer.
   * 
   * @param commonWriter Writer, used to write all source text data
   * @param escapedWriter Writer, used to write all the created HTML markup data (can be equals to commonWriter) 
   * @param fileName input file's name, used to determine, which HRC type must be used by parser
   * @param useLineNumbers If true, number of each line is printed before actual text data
   * @param useHtmlSubst If true, & and < HTML symbols have to be substituted in output
   * @param useInfoHeader If true, simple informational header is printed before actual source text
   * @param useHeaderFooter If true, standard HTML tags with header/footer are printed
   * @throws IOException
   */
  public void generate(Writer commonWriter, Writer escapedWriter,
                String fileName,
                boolean useLineNumbers,
                boolean useHtmlSubst,
                boolean useInfoHeader,
                boolean useHeaderFooter) throws IOException
  {
    
    BaseEditor be = null;
    try {
      be = new BaseEditorNative(pf, lineSource);
      be.setRegionCompact(true);
      be.setRegionMapper("rgb", hrdSchema);
      be.lineCountEvent(lineSource.getLineCount());
      be.visibleTextEvent(0, lineSource.getLineCount());
      be.chooseFileType(fileName);

      if (useHeaderFooter){
        commonWriter.write("<html>\n<head>\n<style></style>\n</head>\n<body style='");
        ParsedLineWriter.writeStyle(commonWriter, (StyledRegion)be.getBackground());
        commonWriter.write("'><pre>\n");
      };
                
      if (useInfoHeader){
        commonWriter.write("Created with Colorer-take5 Library. Type '"+be.getFileType().getName()+"'\n\n");
      };
    
      int lni = 0;
      int lwidth = 1;
      int lncount = lineSource.getLineCount();
      for(lni = lncount/10; lni > 0; lni = lni/10, lwidth++);

      for(int idx = 0; idx < lineSource.getLineCount(); idx++){
        if (useLineNumbers){
          int iwidth = 1;
          for(lni = idx/10; lni > 0; lni = lni/10, iwidth++);
          for(lni = iwidth; lni < lwidth; lni++) commonWriter.write(' ');
          commonWriter.write(String.valueOf(idx));
          commonWriter.write(": ");
        }
        ParsedLineWriter.htmlRGBWrite(commonWriter, escapedWriter, lineSource.getLine(idx), be.getLineRegions(idx));
        commonWriter.write("\n");
      };
      if (useHeaderFooter){
        commonWriter.write("</pre></body></html>\n");
      };
      commonWriter.close();
    }finally{
      if (be != null) be.dispose();
    }
  }
  
  
}
