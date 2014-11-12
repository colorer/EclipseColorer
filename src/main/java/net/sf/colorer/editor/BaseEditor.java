package net.sf.colorer.editor;

import net.sf.colorer.FileType;
import net.sf.colorer.ParserFactory;
import net.sf.colorer.Region;
import net.sf.colorer.RegionHandler;
import net.sf.colorer.handlers.LineRegion;
import net.sf.colorer.handlers.RegionDefine;
import net.sf.colorer.handlers.RegionMapper;


public interface BaseEditor {

    /**
     * Dispose this Editor's resources
     */
    void dispose();

    /**
     * LineRegionsSupport object preferences. Installs specified RegionStore
     * (basically HRDRegionStore), which maps HRC Regions into color data, sets
     * default size (in lines) of Regions structure cache.
     * 
     * @param compact
     *            Creates LineRegionsSupport (false) or
     *            LineRegionsCompactSupport (true) object to store lists of
     *            RegionDefine's
     */
    void setRegionCompact(boolean compact);

    /** Changes used file type */
    void setFileType(FileType typeName);

    /** Chooses filetype according to the filename and first line of text */
    FileType chooseFileType(String fname);

    /** Returns Currently selected file type */
    FileType getFileType();

    /**
     * Specifies number of lines, for which parser would be able to run
     * continual processing without highlight invalidation.
     * 
     * @param backParse
     *            Number of lines. If <= 0, dropped into default value.
     */
    void setBackParse(int backParse);

    /** Installs specified external RegionMapper. */
    void setRegionMapper(RegionMapper regionMapper);

    /** Installs a styled RegionMapper with the provided HRD class and name. */
    void setRegionMapper(String hrdClass, String hrdName);

    /**
     * Adds specified RegionHandler object into the parse process.
     * 
     * @param filter If not null, RegionHandler.addRegion() will be activated only if passed regions
     * have specified <code>filter</code> parent. This allows to
     * optimize performance and disable unnecesary JNI context
     * switches.
     */
    void addRegionHandler(RegionHandler rh, Region filter);

    /**
     * Removes previously added region handler.
     */
    void removeRegionHandler(RegionHandler rh);
    
    /**
     * Adds specified EditorListener object into parse process.
     */
    void addEditorListener(EditorListener el);

    /**
     * Removes previously added EditorListener object.
     */
    void removeEditorListener(EditorListener el);

    /** Current Background Region (def:Text) */
    RegionDefine getBackground();

    /** Current Vertical Rule (def:VertCross) */
    RegionDefine getVertCross();

    /** Current Horizontal Rule (def:HorzCross) */
    RegionDefine getHorzCross();

    /**
     * Searches and creates pair match object. Returned object can be used later
     * in the pair search methods. This object is valid only until reparse of
     * it's line occured. After that event information about line region's
     * references in it becomes invalid and, if used, can produce faults.
     * 
     * @param lineNo
     *            Line number, where to search paired region.
     * @param pos
     *            Position in line, where paired region to be searched. Paired
     *            Region is found, if it includes specified position or ends
     *            directly at one char before line position.
     */
    PairMatch getPairMatch(int lineNo, int pos);

    /**
     * Searches pair match in currently visible text.
     * 
     * @param pm
     *            Unmatched pair match
     */
    void searchLocalPair(PairMatch pm);

    /**
     * Searches pair match in all available text, possibly, making additional
     * processing.
     * 
     * @param pm
     *            Unmatched pair match
     */
    void searchGlobalPair(PairMatch pm);

    /**
     * Return parsed and colored LineRegions of requested line. This method
     * validates current cache state and, if needed, calls Colorer parser to
     * validate modified block of text. Size of reparsed text is choosed
     * according to information about visible text range and modification
     * events.
     * 
     * @todo If number of lines, to be reparsed is more, than backParse
     *       parameter, then method will return null, until validate() method is
     *       called.
     */
    LineRegion[] getLineRegions(int lno);

    /**
     * Validates current state of the editor and runs parser, if needed. This
     * method can be called periodically in background thread to make possible
     * background parsing process.
     * 
     * @param lno
     *            Line number, for which validation is requested. If this number
     *            is in the current visible window range, the part of text is
     *            validated, which is required for visual repaint. If this
     *            number is equals to -1, all the text is validated. If this
     *            number is not in visible range, optimal partial validation is
     *            used
     */
    void validate(int lno);

    /**
     * Tries to do some parsing job while user is doing nothing.
     * 
     * @param time
     *            integer between 0 and 100, shows an abount of time, available
     *            for this job.
     */
    void idleJob(int time);

    /**
     * Informs BaseEditor object about text modification event. All the text
     * becomes invalid after the specified line.
     * 
     * @param topLine
     *            Topmost modified line of text.
     */
    void modifyEvent(int topLine);

    /**
     * Informs about single line modification event. Generally, this type of
     * event can be processed much faster because of pre-checking line's changed
     * structure and cancelling further parsing in case of unmodified text
     * structure.
     * 
     * @param line
     *            Modified line of text.
     * @todo Not used yet! This must include special 'try' parse method.
     */
    void modifyLineEvent(int line);

    /**
     * Informs about changes in visible range of text lines. This information is
     * used to make assumptions about text structure and to make faster parsing.
     * 
     * @param wStart
     *            Topmost visible line of text.
     * @param wSize
     *            Number of currently visible text lines. This number must
     *            includes all partially visible lines.
     */
    void visibleTextEvent(int wStart, int wSize);

    /**
     * Informs about total lines count change. This must include initial lines
     * number setting.
     */
    void lineCountEvent(int newLineCount);

    /**
     * Currently used parser factory.
     */
    ParserFactory getParserFactory();

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