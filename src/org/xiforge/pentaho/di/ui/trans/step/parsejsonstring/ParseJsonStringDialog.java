/*
 * Copyright (c) 2011 XiForge Team.  All rights reserved. 
 * This software was developed by XiForge Team and is provided under the license of Pentaho Data Integration 
 * Community version, the terms of the GNU Lesser General Public License, Version 2.1. You may not use this file 
 * except in compliance with the license. If you need a copy of the license, please go to 
 * http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is XiForge PDI Parse JSON String plugin. The 
 * Initial Developer is Reid Lai.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" basis, WITHOUT WARRANTY
 * OF ANY KIND, either express or  implied. Please refer to the license for the specific language governing your 
 * rights and limitations.
 */

package org.xiforge.pentaho.di.ui.trans.step.parsejsonstring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.xiforge.pentaho.di.trans.step.parsejsonstring.ParseJsonStringMeta;


public class ParseJsonStringDialog extends BaseStepDialog implements StepDialogInterface 
{
	private static Class<?> PKG = ParseJsonStringMeta.class ;
	private ParseJsonStringMeta meta ;
	private ModifyListener lsMod;
	private int middle;
	private int margin;
	
	private Label labelStepName ;
	private FormData posLabelStepName ;
	
	private Text textStepName ;
	private FormData posTextStepName ;
	
	private Label labelFieldToParse ;
	private FormData posLabelFieldToParse ;
	
	private CCombo comboFieldToParse ;
	private FormData posComboFieldToParse ;
	
	private boolean gotPreviousFields=false;
	
	private Label labelFields ;
	private FormData posLabelFields ;
	
	private TableView tableviewFields ;
	private FormData posTableViewFields ;
	
	private Button buttonOK ;
	private Button buttonCancel ;
	
	public ParseJsonStringDialog(Shell parent, Object in, TransMeta transMeta, String stepname) 
	{
		super(parent, (BaseStepMeta) in, transMeta, stepname);
		meta = (ParseJsonStringMeta) baseStepMeta ;
	}

	public String open() 
	{
		// Initialize Layout
		
		Shell parent = getParent();
		Display display = parent.getDisplay();
		this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		this.props.setLook(shell);
 		
        this.setShellImage(shell, meta);
		lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e) 
			{
				meta.setChanged();				
			}		
		};
		this.changed = meta.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout(formLayout);
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		// Build Widgets
		
		labelStepName = new Label(shell, SWT.RIGHT);
		labelStepName.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		this.props.setLook(labelStepName);
 		
 		posLabelStepName = new FormData();
 		posLabelStepName.left = new FormAttachment(0, 0);
 		posLabelStepName.top  = new FormAttachment(0, margin);
 		posLabelStepName.right= new FormAttachment(middle, -margin);
 		labelStepName.setLayoutData(posLabelStepName);
 		
 		textStepName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER) ;
 		textStepName.setText(this.stepname);
 		this.props.setLook(textStepName);
 		
 		posTextStepName = new FormData();
 		posTextStepName.left = new FormAttachment(middle, 0);
 		posTextStepName.top  = new FormAttachment(0, margin);
 		posTextStepName.right= new FormAttachment(100, 0);
 		textStepName.setLayoutData(posTextStepName);
 		
 		labelFieldToParse = new Label(shell, SWT.RIGHT);
 		labelFieldToParse.setText(BaseMessages.getString(PKG, "ParseJsonStringDialog.FieldToParse.Label"));
 		this.props.setLook(labelFieldToParse);
 		
		posLabelFieldToParse = new FormData();
		posLabelFieldToParse.left = new FormAttachment(0, 0);
		posLabelFieldToParse.right= new FormAttachment(middle, -margin);
		posLabelFieldToParse.top  = new FormAttachment(textStepName, margin);
		labelFieldToParse.setLayoutData(posLabelFieldToParse);
		
		comboFieldToParse = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		comboFieldToParse.setToolTipText(BaseMessages.getString(PKG, "ParseJsonStringDialog.FieldToParse.Tooltip"));
		comboFieldToParse.setText("");
		comboFieldToParse.addModifyListener(lsMod);
		
		posComboFieldToParse=new FormData();
		posComboFieldToParse.left = new FormAttachment(middle, 0);
		posComboFieldToParse.top  = new FormAttachment(labelStepName, margin);
		posComboFieldToParse.right= new FormAttachment(100, 0);
		comboFieldToParse.setLayoutData(posComboFieldToParse);
		
		labelFields=new Label(shell, SWT.RIGHT);
		labelFields.setText(BaseMessages.getString(PKG, "ParseJsonStringDialog.Fields.Label"));
		this.props.setLook(labelFields);
		
		posLabelFields=new FormData();
		posLabelFields.left = new FormAttachment(0, 0);
		posLabelFields.top  = new FormAttachment(labelFieldToParse, margin);
		labelFields.setLayoutData(posLabelFields);
 		
		buttonOK = new Button(shell, SWT.PUSH) ;
		buttonOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		buttonCancel = new Button(shell, SWT.PUSH) ;
		buttonCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		setButtonPositions(new Button[] { buttonOK, buttonCancel }, margin, null);

		tableviewFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, getColumnInfo(), meta.getFieldName().length, lsMod, this.props) ; 
		posTableViewFields = new FormData();
		posTableViewFields.left = new FormAttachment(0, 0);
		posTableViewFields.top  = new FormAttachment(labelFields, margin);
		posTableViewFields.right  = new FormAttachment(100, 0);
		posTableViewFields.bottom = new FormAttachment(buttonOK, -2*margin);
		tableviewFields.setLayoutData(posTableViewFields);
		
		addListeners() ;
		this.setSize();
		getData();
		meta.setChanged(changed);
		this.shell.open();
		while (!this.shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		
		return this.stepname;
				
	}

	private void addListeners()
	{
		comboFieldToParse.addFocusListener(new FocusListener() 
		{
			public void focusGained(FocusEvent e) 
			{
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setComboFieldToParseList();
                shell.setCursor(null);
                busy.dispose();		
			}

			public void focusLost(FocusEvent e) { }
			
		}) ;
		buttonOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); }});
		buttonCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); }});
		textStepName.addSelectionListener( new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } } );
		this.shell.addShellListener(new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	}
	
	private void setComboFieldToParseList()
	{
		if(!gotPreviousFields)
		{
			try
			{
				String field=comboFieldToParse.getText();
				RowMetaInterface r = transMeta.getPrevStepFields(this.stepname) ;
				if( r != null )
				{
					comboFieldToParse.setItems(r.getFieldNames());
				}
				if( field != null) comboFieldToParse.setText(field);
		 	}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ParseJsonStringDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ParseJsonStringDialog.FailedToGetFields.DialogMessage"), ke);
			}
			
			gotPreviousFields=true;
		}
	}
	
	private ColumnInfo[] getColumnInfo() 
	{
		ColumnInfo[] return_value = new ColumnInfo[] 
		{
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldName"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.XPath"), ColumnInfo.COLUMN_TYPE_TEXT, false), 
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldType"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldFormat"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldLength"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldPrecision"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldCurrency"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldDecimal"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldGroup"), ColumnInfo.COLUMN_TYPE_TEXT, false),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldTrimType"),ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTrimTypeDescriptions(), true),
			new ColumnInfo(BaseMessages.getString(PKG, "ParseJsonStringDialog.ColumnInfo.FieldRepeat"),ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "Y", "N" })
		} ;
		return return_value ;
	}

	private void getData() 
	{
		if (meta.getFieldToParse() != null) comboFieldToParse.setText(meta.getFieldToParse()) ;
		
        for (int i = 0; i < meta.getFieldName().length; i++)
		{
        	final TableItem item = tableviewFields.table.getItem(i);
        	if(meta.getFieldName()[i] != null) item.setText(1, meta.getFieldName()[i]);
        	if(meta.getXPath()[i] != null) item.setText(2, meta.getXPath()[i]);
        	if(meta.getFieldType()[i] >= 0) item.setText(3, ValueMeta.getTypeDesc(meta.getFieldType()[i]));
        	if(meta.getFieldFormat()[i] != null) item.setText(4, meta.getFieldFormat()[i]);
        	if(meta.getFieldLength()[i] >=0) item.setText(5, "" + meta.getFieldLength()[i]);
        	if(meta.getFieldPrecision()[i] >=0) item.setText(6, "" + meta.getFieldPrecision()[i]);
        	if(meta.getFieldCurrency()[i] != null) item.setText(7, meta.getFieldCurrency()[i]);
        	if(meta.getFieldDecimal()[i] != null) item.setText(8, meta.getFieldDecimal()[i]);
        	if(meta.getFieldGroup()[i] != null) item.setText(9, meta.getFieldGroup()[i]);
        	if(meta.getFieldTrimType()[i] >= 0) item.setText(10, ValueMeta.getTrimTypeDesc(meta.getFieldTrimType()[i]));
        	if(meta.getFieldRepeat()[i]) item.setText(11, meta.getFieldRepeat()[i]? "Y" : "N") ;        	
		}
	}
	
	private void ok()
	{
		if (Const.isEmpty(textStepName.getText())) return;
		this.stepname = textStepName.getText();
		meta.setFieldToParse(comboFieldToParse.getText());
		int nbrFields = tableviewFields.nrNonEmpty();
		meta.allocate(nbrFields);
		for (int i = 0; i < meta.getFieldName().length; i++)
		{
			final TableItem item = tableviewFields.getNonEmpty(i);
			meta.getFieldName()[i] = item.getText(1);
			meta.getXPath()[i] = item.getText(2);
			meta.getFieldType()[i] = ValueMeta.getType(item.getText(3));
			meta.getFieldFormat()[i] = item.getText(4);
			meta.getFieldLength()[i] = Const.toInt(item.getText(5),-1);
			meta.getFieldPrecision()[i] = Const.toInt(item.getText(6),-1);
			meta.getFieldCurrency()[i] = item.getText(7);
			meta.getFieldDecimal()[i] = item.getText(8);
			meta.getFieldGroup()[i] = item.getText(9);
			meta.getFieldTrimType()[i] = ValueMeta.getTrimTypeByDesc(item.getText(10));
			meta.getFieldRepeat()[i] = "Y".equalsIgnoreCase(item.getText(11));
		}
		this.dispose();
	}
	
	private void cancel()
	{
		this.stepname=null;
		meta.setChanged(changed);
		this.dispose();
	}
}
