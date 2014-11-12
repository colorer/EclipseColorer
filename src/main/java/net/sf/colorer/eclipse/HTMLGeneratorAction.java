package net.sf.colorer.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import net.sf.colorer.ParserFactory;
import net.sf.colorer.impl.ReaderLineSource;
import net.sf.colorer.swt.dialog.ActionListener;
import net.sf.colorer.swt.dialog.GeneratorDialog;
import net.sf.colorer.viewer.HTMLGenerator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

class EscapedWriter extends Writer {
    Writer writer;

    EscapedWriter(Writer corewriter) {
        writer = corewriter;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int idx = off; idx < off + len; idx++) {
            if (cbuf[idx] == '<')
                writer.write("&lt;");
            else if (cbuf[idx] == '&')
                writer.write("&amp;");
            else
                writer.write(cbuf[idx]);
        }
    }

    public void close() throws IOException {
    }

    public void flush() throws IOException {
    }
}

public class HTMLGeneratorAction implements IObjectActionDelegate  {

    IWorkbenchPart workbenchPart;

    public void setActivePart(IAction action, IWorkbenchPart part) {
        workbenchPart = part;
    }

	public void selectionChanged(IAction arg0, ISelection arg1) {}

    public void run(IAction action) {
        ISelectionProvider selectionProvider = workbenchPart.getSite()
                .getSelectionProvider();
        ISelection isel = selectionProvider.getSelection();
        if (!(isel instanceof StructuredSelection)) {
            return;
        }
        StructuredSelection selection = (StructuredSelection) selectionProvider
                .getSelection();

//        StringBuffer fileNames = new StringBuffer();
//        String lastFileName = "";
//        String filePath = "./";
//        int num = 0;

        GeneratorDialog gd = new GeneratorDialog();

        Vector fileList = new Vector();
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            IResource adapted = null;

            if (obj instanceof IAdaptable) {
                adapted = (IResource)((IAdaptable) obj).getAdapter(IResource.class);
            }
            if (adapted == null) continue;
            
            addElement(adapted, fileList);
            
        }
        gd.setFileList(fileList);

        final ParserFactory pf = ColorerPlugin.getDefaultPF();
        Vector hrdSchemas = new Vector();
        for (Enumeration hrds = pf.enumerateHRDInstances("rgb"); hrds
                .hasMoreElements();) {
            final String hrd_name = (String) hrds.nextElement();
            final String hrd_descr = pf.getHRDescription("rgb", hrd_name);
            hrdSchemas.addElement(hrd_descr);
            hrdSchemas.addElement(hrd_name);
        }

        gd.setHRDSchema(hrdSchemas);

        IPreferenceStore ps = ColorerPlugin.getDefault().getPreferenceStore();
        gd.setPrefix(ps.getString("g.Prefix"));
        gd.setSuffix(ps.getString("g.Suffix"));
        gd.setHRDSchema(ps.getString("g.HRDSchema"));
        gd.setHtmlHeaderFooter(ps.getBoolean("g.HtmlHeaderFooter"));
        gd.setInfoHeader(ps.getBoolean("g.InfoHeader"));
        gd.setUseLineNumbers(ps.getBoolean("g.UseLineNumbers"));
        gd.setOutputEncoding(ps.getString("g.OutputEncoding"));
        gd.setTargetDirectory(ps.getString("g.TargetDirectory"));
        gd.setLinkSource(ps.getString("g.LinkSource"));

        gd.run(new Generator());
    }
    
    void addElement(IResource resource, Vector list){

        if (resource instanceof IFile) {
            IFile iFile = (IFile) resource;
            String fileLocation = iFile.getLocation().toString();
            list.addElement(fileLocation);
        }
        
        if (resource instanceof IContainer) {
            IContainer folder = (IContainer)resource;
            try{
                IResource[] items = folder.members(false);
                for(int idx = 0; idx < items.length; idx++){
                    addElement(items[idx], list);
                }
            }catch(CoreException e){}
        }
    }

    class Generator implements ActionListener {
        
        public void action(GeneratorDialog gd, int action)
        {
            switch (action) {
            case GeneratorDialog.CLOSE_ACTION:
                IPreferenceStore ps = ColorerPlugin.getDefault().getPreferenceStore();
                ps.setValue("g.Prefix", gd.getPrefix());
                ps.setValue("g.Suffix", gd.getSuffix());
                ps.setValue("g.HRDSchema", gd.getHRDSchema());
                ps.setValue("g.HtmlHeaderFooter", gd.isHtmlHeaderFooter());
                ps.setValue("g.InfoHeader", gd.isInfoHeader());
                ps.setValue("g.UseLineNumbers", gd.isUseLineNumbers());
                ps.setValue("g.OutputEncoding", gd.getOutputEncoding());
                ps.setValue("g.TargetDirectory", gd.getTargetDirectory());
                if (gd.getLinkSource() != null) {
                    ps.setValue("g.LinkSource", gd.getLinkSource());
                }
                gd.getShell().close();
                break;

            case GeneratorDialog.GENERATE_ACTION:
                String[] fileList = gd.getFileList();
                String lastFileName = null;
                StringBuffer fileNames = new StringBuffer();
                String filePath = gd.getTargetDirectory();
                int num = 0;
                try {
                    for (int i = 0; i < fileList.length; i++) {
                        gd.setProgress((i + 1) * 100 / fileList.length);
                        String fileLocation = fileList[i];
                        File file = new File(fileLocation);
                        lastFileName = file.getName();
                        fileNames.append(lastFileName).append("\n");

                        ReaderLineSource rls = new ReaderLineSource(
                                new FileReader(file));
                        final String targetName = filePath + "/"
                                + gd.getPrefix() + file.getName()
                                + gd.getSuffix();
                        Writer commonWriter = null;
                        if ("default".equals(gd.getOutputEncoding())) {
                            commonWriter = new OutputStreamWriter(
                                    new FileOutputStream(targetName));
                        } else {
                            commonWriter = new OutputStreamWriter(
                                    new FileOutputStream(targetName), gd
                                            .getOutputEncoding());
                        }
                        Writer escapedWriter = null;
                        if (gd.isHtmlSubst()) {
                            escapedWriter = new EscapedWriter(commonWriter);
                        } else {
                            escapedWriter = commonWriter;
                        }

                        ParserFactory pf = ColorerPlugin.getDefaultPF();
                        HTMLGenerator hg = new HTMLGenerator(pf, rls, gd
                                .getHRDSchema());

                        hg.generate(commonWriter, escapedWriter,
                                file.getName(), gd.isUseLineNumbers(), gd
                                        .isHtmlSubst(), gd.isInfoHeader(), gd
                                        .isHtmlHeaderFooter());
                        num++;
                    };
                    MessageDialog.openInformation(null, Messages
                            .get("htmlgen.done"), Messages.format(
                            "htmlgen.done.msg", new Object[] {
                                    String.valueOf(num), filePath,
                                    fileNames.toString() }));
                } catch (Exception e) {
                    MessageDialog.openError(null,
                            Messages.get("htmlgen.fault"), Messages.format(
                                    "htmlgen.fault.msg", new Object[] {
                                            String.valueOf(num), filePath, e,
                                            lastFileName }));

                }

                gd.setProgress(0);
                break;
            }
        }
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