package net.sf.colorer;

/** Handles parse information, passed from TextParser.
    TextParser class generates calls of this class methods
    sequentially while parsing the text from top to bottom.
    All enterScheme and leaveScheme calls are properly enclosed,
    addRegion calls can inform about regions, overlapped with each other.
    All handler methods are called sequentially. It means, that
    if one of methods is called with some line number, all other calls
    (before endParsing event comes) can inform about events in the same,
    or lower line's numbers. This makes sequential tokens processing.
    @ingroup colorer
*/
public interface RegionHandler {

    /** Start of text parsing.
        Called only once, when TextParser starts
        parsing of the specified block of text.
        All other event messages comes between this call and
        endParsing call.
        @param lno Start line number
    */
    void startParsing(int lno);

    /** End of text parsing.
        Called only once, when TextParser stops
        parsing of the specified block of text.
        @param lno End line number
    */
    void endParsing(int lno);

    /** Clear line event.
        Called once for each parsed text line, when TextParser starts to parse
        specified line of text. This method is called before any of the region
        information passed, and used often to clear internal handler
        structure of this line before adding new one.
        @param lno Line number
    */
    void clearLine(int lno, String line);

    /** Informs handler about lexical region in line.
        This is a basic method, wich transfer information from
        parser to application. Positions of different passed regions
        can be overlapped.
        @param lno Current line number
        @param sx Start X position of region in line
        @param ex End X position of region in line
        @param region Region information
    */
    void addRegion(int lno, String line, int sx, int ex, Region region);

    /** Informs handler about entering into specified scheme.
        Parameter <code>region</code> is used to specify
        scheme background region information.
        If text is parsed not from the first line, this method is called
        with fake parameters to compensate required scheme structure.
        @param lno Current line number
        @param sx Start X position of region in line
        @param ex End X position of region in line
        @param region Scheme Region information (background)
        @param scheme Additional Scheme information
    */
    void enterScheme(int lno, String line, int sx, int ex, Region region, String scheme);

    /** Informs handler about leaveing specified scheme.
        Parameter <code>region</code> is used to specify
        scheme background region information.
        If text parse process ends, but current schemes stack is not balanced
        (this can happends because of bad balanced structure of source text,
        or partial text parse) this method is <b>not</b> called for unbalanced
        levels.
        @param lno Current line number
        @param sx Start X position of region in line
        @param ex End X position of region in line
        @param region Scheme Region information (background)
        @param scheme Additional Scheme information
    */
    void leaveScheme(int lno, String line, int sx, int ex, Region region, String scheme);


}
