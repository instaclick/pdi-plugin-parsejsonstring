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

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

public class ParseJsonStringMeta extends BaseStepMeta implements StepMetaInterface
{
    private static final Class<?> PKG = ParseJsonStringMeta.class;
    private String fieldToParse ;
    private String[] fieldName ;
    private String[] xPath ;
    private int[] fieldType ;
    private String[] fieldFormat ;
    private int[] fieldLength ;
    private int[] fieldPrecision ;
    private String[] fieldCurrency ;
    private String[] fieldDecimal ;
    private String[] fieldGroup ;
    private int[] fieldTrimType ;
    private boolean[] fieldRepeat ;

    public ParseJsonStringMeta()
    {
        super() ;
    }

    @Override
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info)
    {
        String error_message = "" ;
        CheckResult cr;
        if(prev != null && prev.size() > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta);
            remarks.add(cr);
            error_message = "";

            int i = prev.indexOfValue(fieldToParse);
            if ( i < 0 )
            {
                error_message=BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.FieldToParseNotPresentInInputStream",fieldToParse);
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.FieldToParseFoundInInputStream",fieldToParse), stepMeta);
                remarks.add(cr);
            }
        }
        else
        {
            error_message=BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR;
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }

        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.StepReceivingInfoFromOtherStep"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ParsexStringMeta.CheckResult.NoInputReceivedFromOtherStep"), stepMeta);
            remarks.add(cr);
        }
    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new ParseJsonString(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    @Override
    public StepDataInterface getStepData()
    {
        return new ParseJsonStringData();
    }

    @Override
    public void loadXML(Node stepNode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepNode);
    }

    @Override
    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            fieldToParse  = rep.getStepAttributeString(id_step, "fieldToParse");
            int nbrFields = rep.countNrStepAttributes(id_step, "fieldName");
            allocate(nbrFields);
            for (int i=0;i < nbrFields;i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "fieldName");
                xPath[i] = rep.getStepAttributeString(id_step, i, "xPath");
                fieldType[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "fieldType"));
                fieldFormat[i] = rep.getStepAttributeString(id_step, i, "fieldFormat");
                fieldLength[i] = Const.toInt(rep.getStepAttributeString(id_step, i, "fieldLength"),-1);
                fieldPrecision[i] = Const.toInt(rep.getStepAttributeString(id_step, i, "fieldPrecision"),-1);
                fieldCurrency[i] = rep.getStepAttributeString(id_step, i, "fieldCurrency");
                fieldDecimal[i] = rep.getStepAttributeString(id_step, i, "fieldDecimal");
                fieldGroup[i] = rep.getStepAttributeString(id_step, i, "fieldGroup");
                fieldTrimType[i] = ValueMeta.getTrimTypeByCode(rep.getStepAttributeString(id_step, i, "fieldTrimType"));
                fieldRepeat[i] = "Y".equalsIgnoreCase(rep.getStepAttributeString(id_step, i, "fieldRepeat"));
            }
        } catch (KettleException e) {
            throw new KettleException(BaseMessages.getString(PKG, "ParsexStringMeta.Exception.UnexpectedErrorInReadingStepInfo"), e);
        }
    }

    @Override
    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "fieldToParse", fieldToParse);
            for (int i = 0; i < fieldName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldName", fieldName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "xPath", xPath[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldType", ValueMeta.getTypeDesc(fieldType[i]));
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldFormat", fieldFormat[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldLength", fieldLength[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldPrecision", fieldPrecision[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldCurrency", fieldCurrency[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldDecimal", fieldDecimal[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldGroup", fieldGroup[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldTrimType", ValueMeta.getTrimTypeCode(fieldTrimType[i]));
                rep.saveStepAttribute(id_transformation, id_step, i, "fieldRepeat", fieldRepeat[i]?"Y":"N");
            }
        } catch (Exception e) {
            throw new KettleException(BaseMessages.getString(PKG, "ParsexStringMeta.Exception.UnalbeToSaveStepInfoToRepository")+id_step, e);
        }
    }

    @Override
    public void setDefault()
    {
        fieldToParse = "" ;
        allocate(0) ;
    }

    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }

    public String[] getFieldName()
    {
        return fieldName;
    }

    public void setXPath(String[] xPath)
    {
        this.xPath = xPath;
    }

    public String[] getXPath()
    {
        return xPath;
    }

    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }

    public int[] getFieldType()
    {
        return fieldType;
    }

    public void setFieldFormat(String[] fieldFormat)
    {
        this.fieldFormat = fieldFormat;
    }

    public String[] getFieldFormat()
    {
        return fieldFormat;
    }

    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }

    public int[] getFieldLength()
    {
        return fieldLength;
    }

    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }

    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }

    public void setFieldCurrency(String[] fieldCurrency)
    {
        this.fieldCurrency = fieldCurrency;
    }

    public String[] getFieldCurrency()
    {
        return fieldCurrency;
    }

    public void setFieldDecimal(String[] fieldDecimal)
    {
        this.fieldDecimal = fieldDecimal;
    }

    public String[] getFieldDecimal()
    {
        return fieldDecimal;
    }

    public void setFieldGroup(String[] fieldGroup)
    {
        this.fieldGroup = fieldGroup;
    }

    public String[] getFieldGroup()
    {
        return fieldGroup;
    }

    public void setFieldTrimType(int[] fieldTrimType)
    {
        this.fieldTrimType = fieldTrimType;
    }

    public int[] getFieldTrimType()
    {
        return fieldTrimType;
    }

    public void setFieldRepeat(boolean[] fieldRepeat)
    {
        this.fieldRepeat = fieldRepeat;
    }

    public boolean[] getFieldRepeat()
    {
        return fieldRepeat;
    }

    public void setFieldToParse(String fieldToParse)
    {
        this.fieldToParse = fieldToParse;
    }

    public String getFieldToParse()
    {
        return fieldToParse;
    }

    private void readData(Node stepNode) throws KettleXMLException
    {
        try
        {
            fieldToParse = XMLHandler.getTagValue(stepNode, "fieldToParse");
            final Node fields = XMLHandler.getSubNode(stepNode, "fields");
            final int nbrFields = XMLHandler.countNodes(fields, "field");
            allocate(nbrFields);
            for (int i=0;i < nbrFields;i++)
            {
                final Node subNode = XMLHandler.getSubNodeByNr(fields, "field", i);
                fieldName[i] = XMLHandler.getTagValue(subNode, "fieldName");
                xPath[i] = XMLHandler.getTagValue(subNode, "xPath");
                fieldType[i] = ValueMeta.getType(XMLHandler.getTagValue(subNode, "fieldType"));
                fieldFormat[i] = XMLHandler.getTagValue(subNode, "fieldFormat");
                fieldLength[i] = Const.toInt(XMLHandler.getTagValue(subNode, "fieldLength"),-1);
                fieldPrecision[i] = Const.toInt(XMLHandler.getTagValue(subNode, "fieldPrecision"),-1);
                fieldCurrency[i] = XMLHandler.getTagValue(subNode, "fieldCurrency");
                fieldDecimal[i] = XMLHandler.getTagValue(subNode, "fieldDecimal");
                fieldGroup[i] = XMLHandler.getTagValue(subNode, "fieldGroup");
                fieldTrimType[i] = ValueMeta.getTrimTypeByCode(XMLHandler.getTagValue(subNode, "fieldTrimType"));
                fieldRepeat[i] = "Y".equalsIgnoreCase(XMLHandler.getTagValue(subNode, "fieldRepeat"));
            }
        } catch(Exception e) {
            throw new KettleXMLException(BaseMessages.getString(PKG, "ParsexStringMeta.Exception.UnableToLoadStepInfoFromXML"), e);
        }
    }

    public void allocate(int nbrFields)
    {
        fieldName       = new String[nbrFields];
        xPath           = new String[nbrFields];
        fieldType       = new int[nbrFields];
        fieldFormat     = new String[nbrFields];
        fieldLength     = new int[nbrFields];
        fieldPrecision  = new int[nbrFields];
        fieldCurrency   = new String[nbrFields];
        fieldDecimal    = new String[nbrFields];
        fieldGroup      = new String[nbrFields];
        fieldTrimType   = new int[nbrFields];
        fieldRepeat     = new boolean[nbrFields];
    }

    @Override
    public Object clone()
    {
        final ParseJsonStringMeta retval = (ParseJsonStringMeta)super.clone();
        final int nbrFields              = fieldName.length;

        retval.fieldToParse = fieldToParse ;
        retval.allocate(nbrFields);

        for (int i=0;i < nbrFields;i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.xPath[i] = xPath[i];
            retval.fieldType[i] = fieldType[i];
            retval.fieldFormat[i] = fieldFormat[i];
            retval.fieldLength[i] = fieldLength[i];
            retval.fieldPrecision[i] = fieldPrecision[i];
            retval.fieldCurrency[i] = fieldCurrency[i];
            retval.fieldDecimal[i] = fieldDecimal[i];
            retval.fieldGroup[i] = fieldGroup[i];
            retval.fieldTrimType[i] = fieldTrimType[i];
            retval.fieldRepeat[i] = fieldRepeat[i];
        }

        return retval;
    }

    @Override
    public String getXML()
    {
        final StringBuilder retval = new StringBuilder(32768);
        retval.append(XMLHandler.addTagValue("fieldToParse", fieldToParse));
        retval.append("<fields>");
        for (int i = 0; i < fieldName.length; i++)
        {
            retval.append("<field>");
            retval.append(XMLHandler.addTagValue("fieldName", fieldName[i]));
            retval.append(XMLHandler.addTagValue("xPath", xPath[i]));
            retval.append(XMLHandler.addTagValue("fieldType", ValueMeta.getTypeDesc(fieldType[i])));
            retval.append(XMLHandler.addTagValue("fieldFormat", fieldFormat[i]));
            retval.append(XMLHandler.addTagValue("fieldLength", fieldLength[i]));
            retval.append(XMLHandler.addTagValue("fieldPrecision", fieldPrecision[i]));
            retval.append(XMLHandler.addTagValue("fieldCurrency", fieldCurrency[i]));
            retval.append(XMLHandler.addTagValue("fieldDecimal", fieldDecimal[i]));
            retval.append(XMLHandler.addTagValue("fieldGroup", fieldGroup[i]));
            retval.append(XMLHandler.addTagValue("fieldTrimType", ValueMeta.getTrimTypeCode(fieldTrimType[i])));
            retval.append(XMLHandler.addTagValue("fieldRepeat", fieldRepeat[i]?"Y":"N"));
            retval.append("</field>");
        }
        retval.append("</fields>");
        return retval.toString();
    }

    @Override
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
    {
        // Remove the field to parse
        final int idx = r.indexOfValue(fieldToParse);

        if (idx<0) {
            throw new RuntimeException(BaseMessages.getString(PKG, "ParseJsonString.Log.CouldNotFindFieldToParse",fieldToParse));
        }

        // Add the new fields at the place of the index --> replace!
        for (int i = 0; i < fieldName.length; i++)
        {
            final ValueMetaInterface v = new ValueMeta(fieldName[i], fieldType[i]);
            v.setLength(fieldLength[i], fieldPrecision[i]);
            v.setOrigin(name);
            v.setConversionMask(fieldFormat[i]);
            v.setDecimalSymbol(fieldDecimal[i]);
            v.setGroupingSymbol(fieldGroup[i]);
            v.setCurrencySymbol(fieldCurrency[i]);
            v.setTrimType(fieldTrimType[i]);
            r.addValueMeta(v);
        }
    }

}
