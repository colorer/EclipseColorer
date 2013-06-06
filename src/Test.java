
import net.sf.colorer.*;
import net.sf.colorer.editor.*;
import net.sf.colorer.impl.*;
import net.sf.colorer.viewer.*;

import java.util.*;
import java.io.*;

/**
 * Simple test routine, used to test Colorer library working.
 *
 * @author irusskih
 *
 */
class Test {

    public static void main(String[] arr) {

        /*
         * Initial parser factory is created from one of the predefined
         * locations It is used to created low-level parse objects and to
         * associate them with data files (HRC, HRD)
         */
        ParserFactory pf = new ParserFactory();

        /*
         * Trying to enumerate all available HRD styles from "rgb" coloring
         * class
         */
        for (Enumeration e = pf.enumerateHRDInstances("rgb"); e
                .hasMoreElements();) {
            String name = (String) e.nextElement();
            System.out.println(name + " - " + pf.getHRDescription("rgb", name));
        }

        try{
          String fileName = "Test.java";
          ReaderLineSource lineSource = new ReaderLineSource(new FileReader(fileName));

          BaseEditor be = new BaseEditorNative(pf, lineSource);
          be.setRegionCompact(true);
          
          be.setRegionMapper(pf.createTextMapper("text", "tags"));

          be.lineCountEvent(lineSource.getLineCount());
          be.visibleTextEvent(0, lineSource.getLineCount());
          be.chooseFileType(fileName);

          Writer commonWriter = new PrintWriter(System.out);
          Writer escapedWriter = commonWriter;
          boolean useLineNumbers = true;
        
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
            //ParsedLineWriter.htmlRGBWrite(commonWriter, escapedWriter, lineSource.getLine(idx), be.getLineRegions(idx));
            //ParsedLineWriter.tokenWrite(commonWriter, escapedWriter, lineSource.getLine(idx), be.getLineRegions(idx));
            ParsedLineWriter.markupWrite(commonWriter, escapedWriter, lineSource.getLine(idx), be.getLineRegions(idx));
            commonWriter.write("\n");
          };
          commonWriter.close();
        }catch(Exception e){ e.printStackTrace(); }

        /*
         * Creating BaseEditor - common object, which encapsulates internal
         * parsing algorithms and works as a facade for a couple of internal
         * objects and relations
         */
        BaseEditor be = new BaseEditorNative(pf, new LineSource() {
            // Using simple stub as an input source
            public String getLine(int no) {
                return null;
            };
        });

        /*
         * Enumerating all language types, available in the current HRC
         * database, compiling and loading them into our editor object
         */
        for (Enumeration e = pf.getHRCParser().enumerateFileTypes(); e.hasMoreElements();) {
            FileType type = (FileType) e.nextElement();
            System.out.println("loading: " + type.getDescription());
            String[] pars = type.getParameters();
            for (int i = 0; i < pars.length; i++) {
                String pname = pars[i];
                System.out.println("  param " + pname + " = "
                        + type.getParameterValue(pname));
            }
            be.setFileType(type);

        };
        //*/
    };

}
