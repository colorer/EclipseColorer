package net.sf.colorer.eclipse.editors;

import net.sf.colorer.FileType;
import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.Messages;
import net.sf.colorer.eclipse.PreferencePage;
import net.sf.colorer.eclipse.jface.IColorerEditorAdapter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action to control word wrapping in colorer's editor.
 */
public class WordWrapAction extends Action implements IUpdate {

    private IColorerEditorAdapter fTargetEditor;

    public WordWrapAction(IColorerEditorAdapter targetEditor) {
        setActionDefinitionId(ColorerActionContributor.ACTION_ID_WORD_WRAP);
        setText(Messages.get("WordWrapAction"));
        setToolTipText(Messages.get("WordWrapAction.tooltip"));
        
        setEditor(targetEditor);
    }
    
    public void setEditor(IColorerEditorAdapter targetEditor) {
        fTargetEditor = targetEditor;
    }
    
    public void run(){
        ColorerPlugin.getDefault().setPropertyWordWrap(fTargetEditor.getTextColorer().getFileType(), isChecked() ? 1 : 0);
    }

    public void update() {
        if (fTargetEditor == null) return;
        IPreferenceStore prefStore = ColorerPlugin.getDefault().getPreferenceStore();
        int ww = ColorerPlugin.getDefault().getPropertyWordWrap(fTargetEditor.getTextColorer().getFileType());
        if (ww == -1) {
            ww = prefStore.getBoolean(PreferencePage.WORD_WRAP) ? 1 : 0;
        }
        setChecked(ww == 1);
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
 * The Original Code is the Colorer Library
 *
 * The Initial Developer of the Original Code is
 * Igor Russkih <irusskih at gmail dot com>.
 * Portions created by the Initial Developer are Copyright (C) 1999-2007
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