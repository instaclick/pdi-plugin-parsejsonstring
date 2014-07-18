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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

import org.apache.commons.jxpath.JXPathContext;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class ParseJsonString extends BaseStep implements StepInterface
{
    private static Class<?> PKG = ParseJsonStringMeta.class ;
    private ParseJsonStringMeta meta;
    private ParseJsonStringData data;

    public ParseJsonString(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(ParseJsonStringMeta)smi;
        data=(ParseJsonStringData)sdi;

        if (super.init(smi, sdi))
        {
            return true;
        }
        return false;
    }

    public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(ParseJsonStringMeta)smi;
        data=(ParseJsonStringData)sdi;

        Object[] r = this.getRow();
        if ( r == null )
        {
            setOutputDone();
            return false;
        }

        if (this.first)
        {
            this.first=false;

            // Copy previous step metadata
            data.previousMeta = getInputRowMeta().clone();

            // Find field Name
            data.fieldPos = data.previousMeta.indexOfValue(meta.getFieldToParse());
            if (data.fieldPos < 0 )
            {
                throw new KettleValueException(BaseMessages.getString(PKG, "ParseJsonString.Log.CouldNotFindFieldToParse",meta.getFieldToParse()));
            }

            // Check if parsing field is String type
            if(!data.previousMeta.getValueMeta(data.fieldPos).isString())
            {
                throw new KettleValueException((BaseMessages.getString(PKG, "ParseJsonString.Log.FieldToParseNotValid",meta.getFieldToParse())));
            }

            // Copy Input metadata to output metadata
            data.outputMeta= getInputRowMeta().clone();
            // Create extra fields in output metadata
            meta.getFields(data.outputMeta, this.getStepname(), null, null, this);
            // Create conversion metadata
            data.conversionMeta = data.outputMeta.clone();

            //for (ValueMetaInterface valueMeta : data.conversionMeta.getValueMetaList())
            //{
            //  valueMeta.setType(ValueMetaInterface.TYPE_STRING);
            //}

        }

        // Parsing field

        // Using SF's JSON lib
        JSON json = JSONSerializer.toJSON(r[data.fieldPos].toString());
        JXPathContext context = JXPathContext.newContext(json);

        // Row count
        int rownum = 0 ;
        for (Iterator<?> iter = context.iterate(meta.getXPath()[0]); iter.hasNext();)
        {
            Object item = iter.next();
            if(item != null)
            {
                rownum++ ;
            }
        }

        // Create output buffer area
        Object[] buffer = new Object[rownum];
        for (int i = 0; i < rownum; i++)
        {
            Object[] outputRowData = RowDataUtil.createResizedCopy(r, data.outputMeta.size());
            buffer[i] = outputRowData ;
        }

        // Parsing each path into otuput rows
        for (int i = 0; i < meta.getFieldName().length; i++)
        {
            ValueMetaInterface targetValueMeta = data.outputMeta.getValueMeta(data.previousMeta.size()+ i);
            ValueMetaInterface sourceValueMeta = data.conversionMeta.getValueMeta(data.previousMeta.size() + i);
            DateFormat df = null ;
            if (targetValueMeta.getType() == ValueMetaInterface.TYPE_DATE)
            {
                 df = new SimpleDateFormat(meta.getFieldFormat()[i]);
            }
            int j = 0 ;
            for (Iterator<?> iter = context.iterate(meta.getXPath()[i]);iter.hasNext();)
            {
                Object item = iter.next();
                if( item.getClass().getName() == JSONObject.class.getName() || item.getClass().getName() == JSONArray.class.getName())
                {
                    ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, "{" + meta.getFieldName()[i] + ":" + item.toString() + "}") ;
                }
                else
                {
                    switch(targetValueMeta.getType())
                    {
                        case ValueMetaInterface.TYPE_STRING:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, item.toString());
                            break;
                        case ValueMetaInterface.TYPE_NUMBER:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, Double.valueOf(item.toString()));
                            break;
                        case ValueMetaInterface.TYPE_INTEGER:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, Long.valueOf(item.toString()));
                            break;
                        case ValueMetaInterface.TYPE_DATE:
                            try
                            {
                                ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, df.parse(item.toString()));
                            }
                            catch (ParseException e)
                            {
                                throw new KettleValueException("Unable to convert data type of value");
                            }
                            break;
                        case ValueMetaInterface.TYPE_BIGNUMBER:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, new BigDecimal(item.toString()));
                            break;
                        case ValueMetaInterface.TYPE_BOOLEAN:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, Boolean.valueOf(item.toString()));
                            break;
                        case ValueMetaInterface.TYPE_BINARY:
                            ((Object[]) buffer[j])[data.previousMeta.size() + i] = targetValueMeta.convertData(sourceValueMeta, item);
                            break;
                        default:
                            throw new KettleValueException("Unable to convert data type of value");
                    }
                }
                j++ ;
            }
        }

        // Output buffer to output
        for (int i = 0; i < rownum; i++)
        {
            putRow(data.outputMeta, (Object[]) buffer[i]);
            this.incrementLinesOutput();
        }

        return true ;
    }

}
