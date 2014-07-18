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
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
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
    private static final Class<?> PKG = ParseJsonStringMeta.class ;
    private ParseJsonStringMeta meta;
    private ParseJsonStringData data;

    public ParseJsonString(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (ParseJsonStringMeta) smi;
        data = (ParseJsonStringData) sdi;

        return super.init(smi, sdi);
    }

    @Override
    public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta = (ParseJsonStringMeta) smi;
        data = (ParseJsonStringData) sdi;

        final Object[] r = this.getRow();

        if ( r == null ) {
            setOutputDone();
            return false;
        }

        if (this.first) {
            // Copy previous step metadata
            this.first         = false;
            data.outputRowMeta = getInputRowMeta().clone();
            data.fieldPos      = data.outputRowMeta.indexOfValue(meta.getFieldToParse());

            if (data.fieldPos < 0 ) {
                throw new KettleValueException(BaseMessages.getString(PKG, "ParseJsonString.Log.CouldNotFindFieldToParse",meta.getFieldToParse()));
            }

            // Check if parsing field is String type
            if( ! data.outputRowMeta.getValueMeta(data.fieldPos).isString()) {
                throw new KettleValueException((BaseMessages.getString(PKG, "ParseJsonString.Log.FieldToParseNotValid",meta.getFieldToParse())));
            }

            // Copy Input metadata to output metadata
            data.outputRowMeta = getInputRowMeta().clone();
            // Create extra fields in output metadata
            meta.getFields(data.outputRowMeta, this.getStepname(), null, null, this);
        }

        // Output buffer to output
        putRow(data.outputRowMeta, addRowData(r));

        return true ;
    }

    private Object[] addRowData(Object[] r) throws KettleException
    {
        // Parsing field
        final JSON json                 = JSONSerializer.toJSON(r[data.fieldPos].toString());
        final JXPathContext context     = JXPathContext.newContext(json);
        final String[] fieldNames       = meta.getFieldName();
        final RowMetaInterface rowMeta  = data.outputRowMeta;

        // Parsing each path into otuput rows
        for (int i = 0; i < fieldNames.length; i++) {
            final String fieldPath                   = meta.getXPath()[i];
            final String fieldName                   = meta.getFieldName()[i];
            final Object fieldValue                  = context.getValue(fieldPath);
            final Integer fieldIndex                 = rowMeta.indexOfValue(fieldNames[i]);
            final ValueMetaInterface valueMeta       = rowMeta.getValueMeta(fieldIndex);
            final DateFormat df                      = (valueMeta.getType() == ValueMetaInterface.TYPE_DATE) 
                ? new SimpleDateFormat(meta.getFieldFormat()[i])
                : null;

            // safely add the unique field at the end of the output row
            r = RowDataUtil.addValueData(r, fieldIndex, getRowDataValue(fieldName, valueMeta, valueMeta, fieldValue, df));
        }

        return r;
    }
    
    private Object getRowDataValue(final String fieldName, final ValueMetaInterface targetValueMeta, final ValueMetaInterface sourceValueMeta, final Object value, final DateFormat df) throws KettleException
    {
        if (value == null) {
            return value;
        }

        if (ValueMetaInterface.TYPE_STRING == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, value.toString());
        }
        
        if (ValueMetaInterface.TYPE_NUMBER == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, Double.valueOf(value.toString()));
        }
        
        if (ValueMetaInterface.TYPE_INTEGER == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, Long.valueOf(value.toString()));
        }
        
        if (ValueMetaInterface.TYPE_BIGNUMBER == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, new BigDecimal(value.toString()));
        }
        
        if (ValueMetaInterface.TYPE_BOOLEAN == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, Boolean.valueOf(value.toString()));
        }
        
        if (ValueMetaInterface.TYPE_BINARY == targetValueMeta.getType()) {
            return targetValueMeta.convertData(sourceValueMeta, value);
        }

        if (ValueMetaInterface.TYPE_DATE == targetValueMeta.getType()) {
            try {
                return targetValueMeta.convertData(sourceValueMeta, df.parse(value.toString()));
            } catch (final ParseException e) {
                throw new KettleValueException("Unable to convert data type of value");
            }
        }

        if (value instanceof JSONObject || value instanceof JSONArray) {
            return targetValueMeta.convertData(targetValueMeta, "{" + fieldName + ":" + value.toString() + "}");
        }

        throw new KettleValueException("Unable to convert data type of value");
    }
}
