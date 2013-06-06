package net.sf.colorer.eclipse.editors;

import java.util.ArrayList;
import java.util.List;

import net.sf.colorer.eclipse.ColorerPlugin;
import net.sf.colorer.eclipse.PreferencePage;
import net.sf.colorer.eclipse.jface.ColorerContentAssistProcessor;
import net.sf.colorer.eclipse.jface.TextColorer;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class ColorerSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private TextColorer fTextColorer;
//    private ColorerEditor fEditor;
    private ColorerContentAssistProcessor fCAP;

    /** Copied from org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration */
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        List list = new ArrayList();
        // prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces

        int tabWidth = getTabWidth(sourceViewer);
        boolean useSpaces = ColorerPlugin.getDefault().getCombinedPreferenceStore().getBoolean(PreferencePage.SPACES_FOR_TABS);

        for (int i = 0; i <= tabWidth; i++) {
            StringBuffer prefix = new StringBuffer();
            if (useSpaces) {
                for (int j = 0; j + i < tabWidth; j++)
                    prefix.append(' ');
                if (i != 0)
                    prefix.append('\t');
            } else {
                for (int j = 0; j < i; j++)
                    prefix.append(' ');
                if (i != tabWidth)
                    prefix.append('\t');
            }
            list.add(prefix.toString());
        }
        list.add("");
        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Configuration is based on ColorerEditor and it's TextColorer adapter.
     * TODO: remove ColorerEditor??
     * @param uieditor
     * @param textColorer
     */
    public ColorerSourceViewerConfiguration(TextColorer textColorer) {
        super(ColorerPlugin.getDefault().getCombinedPreferenceStore());

        fTextColorer = textColorer;
    }
    /**
     * TODO: Content assistant implementation?
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant ca = new ContentAssistant();
        fCAP = new ColorerContentAssistProcessor();
        ca.setContentAssistProcessor(fCAP, IDocument.DEFAULT_CONTENT_TYPE);
        ca.enableAutoActivation(true);
        //ca.enableAutoInsert(true);
        ca.setAutoActivationDelay(500);
        ca.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
        return null;
    }
    
    /**
     * Presentation reconciler is be requested from JFace TextColorer
     * interface as adaptable 
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        return fTextColorer.getPresentationReconciler();
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