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

package org.xiforge.pentaho.di.trans.step.parsejsonstring;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class ParseJsonStringData extends BaseStepData implements StepDataInterface 
{
	public RowMetaInterface previousMeta;
	public RowMetaInterface outputMeta;
	public RowMetaInterface conversionMeta;
	public ValueMetaInterface valueMeta;
	
	public int fieldNbr ;
	public int fieldPos ;
	
	public ParseJsonStringData()
	{
		super();
	}
	
}
