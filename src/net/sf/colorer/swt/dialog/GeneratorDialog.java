package net.sf.colorer.swt.dialog;

import java.util.Vector;

import net.sf.colorer.eclipse.ImageStore;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * Dialog for managing HTML generation options.
 * @author irusskih
 *
 */
public class GeneratorDialog {

	private Label hrdSchemaLabel;
	private Button useCSSclasses;
	private Combo hrdSchema;
	private Shell shell;
  private Display display;
  
  private ActionListener runAction = null;
  private GeneratorDialog thisgd = this;
  
	private Combo outputEncoding;
	private Combo inputEncoding;
	private Button htmlHeaderFooter;
	private Button htmlSubst;
	private Button infoHeader;
	private Button useLineNumbers;
	private List fileList;
	private Text targetDirectory;
	private Text suffix;
	private Text prefix;
	private Text linkSource;
	private ProgressBar progressBar;
  
  public static final int CLOSE_ACTION = 1; 
  public static final int GENERATE_ACTION = 2; 
  
  /**
   * Creates dialog, all it's elements.
   * 
   */
	public GeneratorDialog() {
		display = Display.getCurrent();
		shell = new Shell(display, SWT.APPLICATION_MODAL|SWT.TITLE|SWT.RESIZE|SWT.CLOSE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.makeColumnsEqualWidth = true;
		shell.setLayout(gridLayout_2);
        shell.setText(GeneratorMessages.get("title"));
        shell.setSize(new Point(600,500));
        shell.setImage(ImageStore.getID("colorer_editor").createImage());
		{
			final Composite composite = new Composite(shell, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
			composite.setLayout(new FillLayout());
			{
				final Label label = new Label(composite, SWT.BORDER | SWT.SHADOW_NONE | SWT.WRAP);
				label.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
				label.setFont(ResourceManager.getFont("", 16, SWT.BOLD));
				label.setText(GeneratorMessages.get("title-long"));
			}
		}
		{
			final Composite composite = new Composite(shell, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL));
			final GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			composite.setLayout(gridLayout);
			{
				fileList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
				fileList.setToolTipText(GeneratorMessages.get("tip-fileList"));
				final GridData gridData = new GridData(GridData.FILL_BOTH);
				fileList.setLayoutData(gridData);
			}
			{
				final Composite composite_1 = new Composite(composite, SWT.NONE);
				final GridLayout gridLayout_1 = new GridLayout();
				gridLayout_1.makeColumnsEqualWidth = true;
				gridLayout_1.verticalSpacing = 2;
				composite_1.setLayout(gridLayout_1);
				composite_1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_VERTICAL));
				{
					final Button button = new Button(composite_1, SWT.NONE);
					button.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
              fileList.setSelection(0, fileList.getItemCount());
						}
					});
					button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
					button.setText(GeneratorMessages.get("select-all"));
				}
				{
					final Button button = new Button(composite_1, SWT.NONE);
					button.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
              fileList.deselectAll();
						}
					});
					button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
					button.setText(GeneratorMessages.get("deselect-all"));
				}
			}
		}
		{
			final Composite composite = new Composite(shell, SWT.NONE);
			final GridLayout gridLayout_3 = new GridLayout();
			gridLayout_3.marginHeight = 1;
			composite.setLayout(gridLayout_3);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
			{
				final Group group = new Group(composite, SWT.NONE);
				final GridLayout gridLayout_1 = new GridLayout();
				gridLayout_1.marginHeight = 2;
				group.setLayout(gridLayout_1);
				group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				group.setText(GeneratorMessages.get("group-file"));
				{
					final Composite composite_1 = new Composite(group, SWT.NONE);
					composite_1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					final GridLayout gridLayout = new GridLayout();
					gridLayout.marginHeight = 1;
					gridLayout.numColumns = 3;
					composite_1.setLayout(gridLayout);
					{
						final Label label = new Label(composite_1, SWT.NONE);
						final GridData gridData = new GridData();
						label.setLayoutData(gridData);
						label.setToolTipText(GeneratorMessages.get("targetDir.tip"));
						label.setText(GeneratorMessages.get("targetDir"));
					}
					{
						targetDirectory = new Text(composite_1, SWT.BORDER);
						targetDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						targetDirectory.setText(".");
					}
					{
						final Button button = new Button(composite_1, SWT.NONE);
						button.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog (shell);
                dialog.setMessage(GeneratorMessages.get("targetDir.message"));
                dialog.setText(GeneratorMessages.get("targetDir.text"));
                String pathName = dialog.open();
                if (pathName != null){
                  targetDirectory.setText(pathName);
                }
							}
						});
						button.setToolTipText(GeneratorMessages.get("targetDir.tip"));
						button.setLayoutData(new GridData());
						button.setText(GeneratorMessages.get("targetDir.button"));
					}
				}
				{
					final Composite composite_1 = new Composite(group, SWT.NONE);
					final GridLayout gridLayout = new GridLayout();
					gridLayout.marginHeight = 1;
					gridLayout.makeColumnsEqualWidth = true;
					gridLayout.horizontalSpacing = 20;
					gridLayout.numColumns = 4;
					composite_1.setLayout(gridLayout);
					composite_1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
					{
						final Label label = new Label(composite_1, SWT.NONE);
						label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
						label.setText(GeneratorMessages.get("prefix"));
					}
					{
						prefix = new Text(composite_1, SWT.BORDER);
						prefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					}
					{
						final Label label = new Label(composite_1, SWT.NONE);
						label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
						label.setText(GeneratorMessages.get("suffix"));
					}
					{
						suffix = new Text(composite_1, SWT.BORDER);
						suffix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						suffix.setText(".html");
					}
				}
			}
			{
				final Group group = new Group(composite, SWT.NONE);
				final GridLayout gridLayout_1 = new GridLayout();
				gridLayout_1.verticalSpacing = 1;
				gridLayout_1.makeColumnsEqualWidth = true;
				group.setLayout(gridLayout_1);
				group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
				group.setText(GeneratorMessages.get("group.processing"));
				{
					useLineNumbers = new Button(group, SWT.CHECK);
					useLineNumbers.setToolTipText(GeneratorMessages.get("useLineNumbers.tip"));
					useLineNumbers.setText(GeneratorMessages.get("useLineNumbers"));
				}
				{
					infoHeader = new Button(group, SWT.CHECK);
					infoHeader.setToolTipText(GeneratorMessages.get("infoHeader.tip"));
					infoHeader.setText(GeneratorMessages.get("infoHeader"));
				}
				{
					htmlSubst = new Button(group, SWT.CHECK);
					htmlSubst.setToolTipText(GeneratorMessages.get("htmlSubst.tip"));
					htmlSubst.setText(GeneratorMessages.get("htmlSubst"));
				}
				{
					htmlHeaderFooter = new Button(group, SWT.CHECK);
					htmlHeaderFooter.setToolTipText(GeneratorMessages.get("htmlHeaderFooter.tip"));
					htmlHeaderFooter.setText(GeneratorMessages.get("htmlHeaderFooter"));
				}
				{
					final Composite composite_1 = new Composite(group, SWT.NONE);
					composite_1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
					final GridLayout gridLayout = new GridLayout();
					gridLayout.marginHeight = 1;
					gridLayout.numColumns = 3;
					composite_1.setLayout(gridLayout);
					{
						final Label label = new Label(composite_1, SWT.NONE);
						label.setText(GeneratorMessages.get("docLink"));
            label.setEnabled(false);
					}
					{
						linkSource = new Text(composite_1, SWT.BORDER);
						linkSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            linkSource.setEnabled(false);
          }
					{
						final Button button = new Button(composite_1, SWT.NONE);
						button.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog (new Shell(), SWT.OPEN);
                dialog.setFilterNames (new String [] {"XML Files", "All files"});
                dialog.setFilterExtensions (new String [] {"*.xml", "*.*"});
                String fileName = dialog.open();
                if (fileName == null) return;
                linkSource.setText(fileName);
							}
						});
						button.setText(GeneratorMessages.get("docLink.choose"));
					}
				}
				{
					final Composite composite_1 = new Composite(group, SWT.NONE);
					composite_1.setLayoutData(new GridData());
					final GridLayout gridLayout = new GridLayout();
					gridLayout.makeColumnsEqualWidth = true;
					gridLayout.marginHeight = 3;
					gridLayout.verticalSpacing = 1;
					gridLayout.numColumns = 4;
					composite_1.setLayout(gridLayout);
					{
						final Label label = new Label(composite_1, SWT.NONE);
						label.setLayoutData(new GridData());
						label.setText(GeneratorMessages.get("enc.input"));
            label.setEnabled(false);
					}
					{
						inputEncoding = new Combo(composite_1, SWT.READ_ONLY);
						inputEncoding.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
						inputEncoding.setText("combo");
            inputEncoding.add("Default encoding");
            inputEncoding.add("UTF-8");
            inputEncoding.add("UTF-16");
            inputEncoding.add("ISO-8859-1");
            inputEncoding.add("Windows-1251");
            inputEncoding.add("KOI8-R");
            inputEncoding.select(0);
            inputEncoding.setEnabled(false);
					}
					{
						final Label label = new Label(composite_1, SWT.NONE);
						label.setLayoutData(new GridData());
						label.setText(GeneratorMessages.get("enc.output"));
					}
					{
						outputEncoding = new Combo(composite_1, SWT.READ_ONLY);
						outputEncoding.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
						outputEncoding.setText("combo");
            outputEncoding.add("Default encoding");
            outputEncoding.add("UTF-8");
            outputEncoding.add("UTF-16");
            outputEncoding.add("ISO-8859-1");
            outputEncoding.add("Windows-1251");
            outputEncoding.add("KOI8-R");
            outputEncoding.select(0);
					}
				}
			}
			{
				final Composite composite_1 = new Composite(composite, SWT.NONE);
				composite_1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
				final GridLayout gridLayout = new GridLayout();
				gridLayout.marginHeight = 1;
				gridLayout.numColumns = 3;
				composite_1.setLayout(gridLayout);
				{
					hrdSchemaLabel = new Label(composite_1, SWT.NONE);
					hrdSchemaLabel.setText(GeneratorMessages.get("HRDSchema"));
				}
				{
					hrdSchema = new Combo(composite_1, SWT.READ_ONLY);
					hrdSchema.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					hrdSchema.setToolTipText(GeneratorMessages.get("HRDSchema.tip"));
					{
						useCSSclasses = new Button(composite_1, SWT.CHECK);
						useCSSclasses.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
						useCSSclasses.setToolTipText(GeneratorMessages.get("cssClasses.tip"));
						useCSSclasses.setText(GeneratorMessages.get("cssClasses"));
            useCSSclasses.addSelectionListener(new SelectionAdapter(){
              public void widgetSelected(SelectionEvent e) {
                if (useCSSclasses.getSelection()){
                  hrdSchema.setEnabled(false);
                  hrdSchemaLabel.setEnabled(false);
                }else{
                  hrdSchema.setEnabled(true);
                  hrdSchemaLabel.setEnabled(true);
                }
              };
            });
					}
					hrdSchema.setText("combo");
				}
			}
		}
		{
			final Composite composite = new Composite(shell, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final GridLayout gridLayout = new GridLayout();
			gridLayout.marginHeight = 1;
			gridLayout.numColumns = 2;
			composite.setLayout(gridLayout);
			{
				final Button button = new Button(composite, SWT.CENTER);
				button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				button.setText(GeneratorMessages.get("close"));
        button.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            if (runAction != null){
              runAction.action(thisgd, CLOSE_ACTION);
            }
          }
        });
			}
			{
				final Button button = new Button(composite, SWT.NONE);
				final GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        shell.setDefaultButton(button);
				button.setLayoutData(gridData);
				button.setText(GeneratorMessages.get("generate"));
        button.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            if (runAction != null){
              runAction.action(thisgd, GENERATE_ACTION);
            }
          }
        });
			}
    }
    {
			progressBar = new ProgressBar(shell, SWT.NONE);
			progressBar.setSelection(0);
			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// DESIGNER: Add controls before this line.
	}
  
  /**
   * Activates dialog, shows it on the screen, and
   * calls action listener when events are occurs.
   * 
   * @param action Listener, to be notified about some actions
   * @see GeneratorDialog.CLOSE_ACTION
   * @see GeneratorDialog.GENERATE_ACTION
   */
  public void run(ActionListener action){
    runAction = action;
    shell.open();
    
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }
  
  /**
   * @return Returns the files, selected for processing.
   */
  public String[] getFileList() {
    return fileList.getSelection();
  }

  /**
   * @param fileList The fileList to set - Vector with list of file paths
   */
  public void setFileList(final Vector list) {
    fileList.removeAll();
    for(int idx = 0; idx < list.size(); idx++){
      fileList.add((String)list.elementAt(idx));
    }
    fileList.select(0, fileList.getItemCount());
  }

  /**
   * @return Returns the htmlHeaderFooter.
   */
  public boolean isHtmlHeaderFooter() {
    return !htmlHeaderFooter.getSelection();
  }

  /**
   * @param htmlHeaderFooter The htmlHeaderFooter to set.
   */
  public void setHtmlHeaderFooter(boolean val) {
    htmlHeaderFooter.setSelection(!val);
  }

  /**
   * @return Returns the htmlSubst.
   */
  public boolean isHtmlSubst() {
    return !htmlSubst.getSelection();
  }

  /**
   * @param htmlSubst The htmlSubst to set.
   */
  public void HtmlSubst(boolean val) {
    htmlSubst.setSelection(!val);
  }

  /**
   * @return Returns the infoHeader.
   */
  public boolean isInfoHeader() {
    return !infoHeader.getSelection();
  }

  /**
   * @param infoHeader The infoHeader to set.
   */
  public void setInfoHeader(boolean val) {
    infoHeader.setSelection(!val);
  }

  /**
   * @return Returns the inputEncoding.
   */
  public String getInputEncoding() {
    if (outputEncoding.getSelectionIndex() == 0) return null;
    return inputEncoding.getText();
  }

  /**
   * @param inputEncoding The inputEncoding to set.
   */
  public void setInputEncoding(String inputEncoding) {
    for(int idx = 0; idx < this.outputEncoding.getItemCount(); idx++){
      if (this.outputEncoding.getItem(idx).equals(outputEncoding)){
        this.outputEncoding.select(idx);
      }
    }
  }

  /**
   * @return Returns the linkSource.
   */
  public String getLinkSource() {
    if (linkSource.getText().length() == 0) return null;
    return linkSource.getText();
  }

  /**
   * @param linkSource The linkSource to set.
   */
  public void setLinkSource(String linkSource) {
    this.linkSource.setText(linkSource);
  }

  /**
   * @return Returns the outputEncoding.
   */
  public String getOutputEncoding() {
    if (outputEncoding.getSelectionIndex() == 0) return "default";
    return outputEncoding.getText();
  }

  /**
   * @param outputEncoding The outputEncoding to set.
   */
  public void setOutputEncoding(String outputEncoding) {
    for(int idx = 0; idx < this.outputEncoding.getItemCount(); idx++){
      if (this.outputEncoding.getItem(idx).equals(outputEncoding)){
        this.outputEncoding.select(idx);
      }
    }
  }

  /**
   * @return Returns the prefix.
   */
  public String getPrefix() {
    return prefix.getText();
  }

  /**
   * @param prefix The prefix to set.
   */
  public void setPrefix(String val) {
    prefix.setText(val);
  }

  /**
   * @return Returns the progressBar.
   */
  public int getProgress() {
    return progressBar.getSelection();
  }

  /**
   * @param progressBar The progressBar to set.
   */
  public void setProgress(int progress) {
    if (progress > 100) progress = 100;
    progressBar.setSelection(progress);
  }

  /**
   * @return Returns the suffix.
   */
  public String getSuffix() {
    return suffix.getText();
  }

  /**
   * @param suffix The suffix to set.
   */
  public void setSuffix(String val) {
    suffix.setText(val);
  }

  /**
   * @return Returns the targetDirectory.
   */
  public String getTargetDirectory() {
    return targetDirectory.getText();
  }

  /**
   * @param targetDirectory The targetDirectory to set.
   */
  public void setTargetDirectory(String val) {
    targetDirectory.setText(val);
  }

  /**
   * @return Returns the useLineNumbers.
   */
  public boolean isUseLineNumbers() {
    return useLineNumbers.getSelection();
  }
  /**
   * @param useLineNumbers The useLineNumbers to set.
   */
  public void setUseLineNumbers(boolean val) {
    useLineNumbers.setSelection(val);
  }

  /**
   * @return Returns the hrdSchema.
   */
  public String getHRDSchema() {
    return (String)hrdSchema.getData(hrdSchema.getText());
  }
  /**
   * @param list The list of hrd schemas to set.
   * Consists of two items per list entry - 
   * user friendly entry name of the item and
   * internal name.
   */
  public void setHRDSchema(Vector list) {
    for(int idx = 0; idx < list.size(); idx+=2){
      hrdSchema.add((String)list.elementAt(idx));
      hrdSchema.setData((String)list.elementAt(idx), (String)list.elementAt(idx+1));
    }
    hrdSchema.select(0);
  }
  /**
   * Set currently selected HRD schema in list
   * @param schemaid Schema name
   */
  public void setHRDSchema(String schemaid) {
    for(int idx = 0; idx < hrdSchema.getItemCount(); idx++){
      final String entry = hrdSchema.getItem(idx);
      if (hrdSchema.getData(entry).equals(schemaid)){
        hrdSchema.select(idx);
      }
    }
  }
  
  public Shell getShell(){
    return shell;
  }
  
  
  
  
  

  public static void main(String[] args){

    Vector list = new Vector();
    for(int i = 0; i < 10; i++){
      list.addElement("xxxx xxxxxxxxxx "+i);
    }
    GeneratorDialog gd = new GeneratorDialog();

    gd.setFileList(list);
    gd.setHRDSchema(list);
    gd.setHRDSchema("xxxx xxxxxxxxxx 4");
    
    gd.run(new ActionListener(){
      public void action(GeneratorDialog gd, int action) {
        switch(action){
          case GeneratorDialog.CLOSE_ACTION:
            gd.getShell().close();
            break;
          case GeneratorDialog.GENERATE_ACTION:
            for(int idx = 0; idx < 100; idx++){
              gd.setProgress(idx);
              try{
                Thread.sleep(20);
              }catch(InterruptedException e){}
            }
            break;
        }
      }
    });
  }
}
