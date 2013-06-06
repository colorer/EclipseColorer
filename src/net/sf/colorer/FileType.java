package net.sf.colorer;

import java.util.Vector;

/**
 * HRC FileType (or prototype) instance
 * @ingroup colorer
 */
public class FileType{

    long iptr;
    String name;
    String group;
    String description;
    String[] parameters;

    FileType(long _iptr, final String _name, final String _group,
            final String _description) {
        iptr = _iptr;
        name = _name;
        group = _group;
        description = _description;
    };

    /**
     * Public name of file type (HRC 'name' attribute)
     * 
     * @return File type Name
     */
    public String getName() {
        return name;
    }
    /**
     * Public group name of file type (HRC 'group' attribute)
     * 
     * @return File type Group
     */
    public String getGroup() {
        return group;
    }
    /**
     * Public description of file type (HRC 'description' attribute)
     * 
     * @return File type Description
     */
    public String getDescription() {
        return description;
    }
    /**
     * Returns the base scheme of this file type. Basically, this is the scheme
     * with same public name, as it's type. @return File type base scheme, to be
     * used as root scheme of text parsing.
     */
    public Scheme getBaseScheme() {
        return getBaseScheme(iptr);
    }
    
    private native Scheme getBaseScheme(long iptr);

    /**
     * Returns all available parameters for this file type. Each element in
     * enumeration contains a reference to parameter name.
     */
    public String[] getParameters() {
        if (parameters == null) {
            Vector list = new Vector();
            for(int idx = 0; ; idx++) {
                String par = enumerateParameters(iptr, idx);
                if (par == null) {
                    break;
                }
                list.add(par);
            }
            parameters = (String[])list.toArray(new String[0]);
        }
        return parameters;
    }
    native String enumerateParameters(long iptr, int idx);
    
    /**
     * Retrieves parameter's user description string
     * @param name
     * @return Description for this parameter, can be null.
     */
    public String getParameterDescription(String name) {
        return getParameterDescription(iptr, name);
    }
    native String getParameterDescription(long iptr, String name);
    
    /**
     * Returns parameter's value of this file type. Parameters are stored in
     * prototypes as
     * 
     * <pre>
     * 
     *      \&lt;parameters&gt;
     *      \&lt;param name=&quot;name&quot; value=&quot;value&quot; description=&quot;...&quot;/&gt;
     *      \&lt;/parameter&gt;
     *      
     * </pre>
     * 
     * Parameters can be used to store application specific information about
     * each type of file. Also parameters are accessible from the HRC definition
     * using <code>if/unless</code> attributes of scheme elements. This allows
     * portable customization of HRC loading.
     * 
     * @param name
     *            Parameter's name
     * @return Value (changed or default) of this parameter
     */
    public String getParameterValue(String name) {
        return getParamValue(iptr, name);
    }
    native String getParamValue(long iptr, String name);

    /**
     * Returns parameter's default value of this file type. Default values are
     * the values, explicitly pointed with \c value attribute.
     * 
     * @param name
     *            Parameter's name
     * @return Default value of this parameter
     */
    public String getParameterDefaultValue(String name) {
        return getParamDefaultValue(iptr, name);
    }
    native String getParamDefaultValue(long iptr, String name);

    /**
     * Changes value of the parameter with specified name. Note, that changed
     * parameter values are not stored in HRC base - they remains active only
     * during this HRC session. Application should use its own mechanism to save
     * these values between sessions (if needed).
     * 
     * @param name
     *            Parameter's name
     * @param value
     *            New value of this parameter.
     */
    public void setParameterValue(String name, String value) {
        setParamValue(iptr, name, value);
    }
    native void setParamValue(long iptr, String name, String value);

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
