package net.sf.colorer.eclipse;

import java.net.URL;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import net.sf.colorer.FileType;
import net.sf.colorer.ParserFactory;
import net.sf.colorer.impl.Logger;
import net.sf.colorer.swt.ColorManager;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class ColorerPlugin extends AbstractUIPlugin {
    //The shared instance.
    private static ColorerPlugin plugin;
    private ResourceBundle resourceBundle;
    
    private String catalogPath;
    private ParserFactory parserFactory;
    private ColorManager colorManager = new ColorManager();
    private Vector reloadListeners = new Vector();
    private Vector hrdSetsList;
    private IPreferenceStore fCombinedPreferenceStore;
    
    public final static String WORD_WRAP_SIGNATURE = "@@WORD_WRAP@@";
    public final static String HRD_SIGNATURE = "@@HRD@@";

    /**
     * The constructor.
     */
    public ColorerPlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        plugin = this;
        Logger.trace("ColorerPlugin", "Loaded");
    
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(PreferencePage.SPACES_FOR_TABS, false);
        store.setDefault(PreferencePage.WORD_WRAP, false);
        store.setDefault(PreferencePage.WORD_WRAP_PATCH, true);
    
        store.setDefault(PreferencePage.FULL_BACK, true);
        store.setDefault(PreferencePage.USE_BACK, true);
        store.setDefault(PreferencePage.VERT_CROSS, false);
        store.setDefault(PreferencePage.HORZ_CROSS, true);
        store.setDefault(PreferencePage.PAIRS_MATCH, "PAIRS_OUTLINE");
    
        store.setDefault(PreferencePage.HRD_SET, "default");

        store.setDefault(PreferencePage.PROJECTION, true);
        
        store.setDefault(PreferencePage.BACK_SCALE, 1);

        store.setDefault("Outline.Hierarchy", true);
        store.setDefault("Outline.Sort", false);

        store.setDefault("RegionsTree.Link", false);

        store.setDefault("g.Prefix", "");
        store.setDefault("g.Suffix", ".html");
        store.setDefault("g.HRDSchema", store.getString(PreferencePage.HRD_SET));
        store.setDefault("g.HtmlHeaderFooter", true);
        store.setDefault("g.InfoHeader", true);
        store.setDefault("g.UseLineNumbers", true);
        store.setDefault("g.OutputEncoding", "default");
        store.setDefault("g.TargetDirectory", "/");
        store.setDefault("g.LinkSource", "");        
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        resourceBundle = null;
    }

    /**
     * Returns the shared instance.
     */
    public static ColorerPlugin getDefault() {
        return plugin;
    }
    
    /**
     * @return Default parser factory from default plugin instance
     */
    public static ParserFactory getDefaultPF() {
        return getDefault().getParserFactory();
    }
    
    public synchronized ParserFactory getParserFactory() {
        if (parserFactory != null) {
            return parserFactory;
        }
        try {
            catalogPath = new URL(Platform.resolve(
                    getBundle().getEntry("/")), "colorer/catalog.xml").toExternalForm();
            Logger.trace("EclipsecolorerPlugin", "Catalog: "+catalogPath);
            parserFactory = new ParserFactory(catalogPath);
        } catch (Throwable e) {
            Logger.trace("EclipsecolorerPlugin", "Fault in getting parser factory, (will try default location)", e);
            boolean error = true;
            Throwable exc = e;
            try {
                parserFactory = new ParserFactory();
                error = false;
            } catch (Throwable e1) {
                error = true;
                exc = e1;
            }
            if (error) {
                MessageDialog.openError(null, Messages.get("init.error.title"),
                                Messages.get("init.error.pf") + "\n"
                                        + exc.getMessage());
            }
        }
        if (parserFactory != null) {
            hrdSetsList = new Vector();
            for (Enumeration hrds = parserFactory.enumerateHRDInstances("rgb"); hrds.hasMoreElements();) {
                String hrd_name = (String) hrds.nextElement();
                String hrd_descr = parserFactory.getHRDescription("rgb", hrd_name);
                hrdSetsList.add(hrd_name);
            }
            
            initHRCParameters();
        }
        return parserFactory;
    }

    /**
     * Makes initial HRC base initialization with parameters from the
     * preference store.
     */
    void initHRCParameters() {
        Enumeration fte = parserFactory.getHRCParser().enumerateFileTypes();
        while(fte.hasMoreElements()) {
            FileType type = (FileType)fte.nextElement();
            
            getPreferenceStore().setDefault(HRD_SIGNATURE+type.getName(), "");
            getPreferenceStore().setDefault(WORD_WRAP_SIGNATURE+type.getName(), -1);

            String parameters[] = type.getParameters();
            int pindex = 0;
            while (parameters != null && pindex < parameters.length){
                String propname = getParameterPropertyName(type, parameters[pindex]);

                getPreferenceStore().setDefault(propname, type.getParameterDefaultValue(parameters[pindex]));

                if (getPreferenceStore().contains(propname)){
                    type.setParameterValue(parameters[pindex], getPreferenceStore().getString(propname));
                }
                pindex++;
            }
        }
    }

    String getParameterPropertyName(FileType type, String parameter) {
        return "parameters." + type.getName() + "." + parameter;
    }
    
    /**
     * Resets all the settings for the filetype-specific attributes and
     * HRC parameters to the default values
     */
    public void resetHRCParameters() {
        Enumeration fte = parserFactory.getHRCParser().enumerateFileTypes();
        while(fte.hasMoreElements()) {
            FileType type = (FileType)fte.nextElement();
            
            getPreferenceStore().setToDefault(HRD_SIGNATURE+type.getName());
            getPreferenceStore().setToDefault(WORD_WRAP_SIGNATURE+type.getName());

            String parameters[] = type.getParameters();
            int pindex = 0;
            while (parameters != null && pindex < parameters.length){
                String pname = getParameterPropertyName(type, parameters[pindex]);
                getPreferenceStore().setToDefault(pname);
                pindex++;
            }
        }
    }
    
    /**
     * Returns HRD scheme, assigned with this file type
     * @return HRD for this type, null if no specific HRD selected for this type
     */
    public String getPropertyHRD(FileType type) {
        String hrd = getPreferenceStore().getString(HRD_SIGNATURE+type.getName());
        if (hrd.equals(""))
            hrd = null;
        return hrd;
    }
    /**
     * Changes specific HRD settings for the type
     * @param type
     * @param value
     */
    public void setPropertyHRD(FileType type, String value) {
        getPreferenceStore().setValue(HRD_SIGNATURE+type.getName(), value);
    }
    
    /**
     * Returns word wrap property within this file type
     * @return word wrap on (1), off (0) or default (-1)
     */
    public int getPropertyWordWrap(FileType type) {
        return getPreferenceStore().getInt(WORD_WRAP_SIGNATURE+type.getName());
    }
    public void setPropertyWordWrap(FileType type, int value) {
        getPreferenceStore().setValue(WORD_WRAP_SIGNATURE+type.getName(), value);
    }

    
    /**
     * Returns parameter value for this type
     * @return TRUE (1), FALSE (0) or DEFAULT (-1)
     */
    public int getPropertyParameter(FileType type, String param) {
        String paramValue = getPreferenceStore().getString( getParameterPropertyName(type, param.toString()));
        if ("true".equals(paramValue)) return 1;
        if ("false".equals(paramValue)) return 0;
        return -1;
    }
    
    public void setPropertyParameter(FileType type, String param, int value) {
        if (value != 0 && value != 1){
            getPreferenceStore().setToDefault(getParameterPropertyName(type, param));
        }else{
            getPreferenceStore().setValue(getParameterPropertyName(type, param), value == 0 ? "false" : "true");
        }
    }

    
    /**
     * Reloads whole colorer native part.
     * All Java mapped objects become invalid after this operation
     * and will cause segfault since refer to the destructed native objects
     */
    public synchronized void reloadParserFactory() {
        // informs all the editors about ParserFactory reloading
        if (parserFactory != null && !parserFactory.isDisposed()) {
            parserFactory.dispose();
        }
        parserFactory = null;
        parserFactory = getParserFactory();
        notifyReloadListeners();
    }
    
    /**
     * Returns list of available HRD names in "RGB" class
     * @return Vector of String's
     */
    public Vector getHRDList() {
        getParserFactory();
        return hrdSetsList;
    }

    void notifyReloadListeners() {
        for(int idx = 0; idx < reloadListeners.size(); idx++) {
            ((IColorerReloadListener)reloadListeners.elementAt(idx)).notifyReload();
        }
            
    }
    
    /**
     * Adds HRC database reload action listener
     */
    public void addReloadListener(IColorerReloadListener listener) {
        if (!reloadListeners.contains(listener)) {
            reloadListeners.add(listener);
        }
    }
    
    /**
     * Adds HRC database reload action listener
     */
    public void removeReloadListener(IColorerReloadListener listener) {
        reloadListeners.remove(listener);
    }

    /**
     * Color manager to share between all Colorer Editor instances
     */
    public ColorManager getColorManager() {
        return colorManager;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }
    
    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle.getBundle("net.sf.colorer.eclipse.EclipsecolorerPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    public IPreferenceStore getCombinedPreferenceStore() {
        if (fCombinedPreferenceStore == null) {
            IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore(); 
            fCombinedPreferenceStore= new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(), generalTextStore });
        }
        return fCombinedPreferenceStore;
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