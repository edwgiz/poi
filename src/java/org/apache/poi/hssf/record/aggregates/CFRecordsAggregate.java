/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.CFHeader12Record;
import org.apache.poi.hssf.record.CFHeaderBase;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.ptg.AreaErrPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * <p>CFRecordsAggregate - aggregates Conditional Formatting records CFHeaderRecord 
 * and number of up CFRuleRecord records together to simplify access to them.</p>
 * <p>Note that Excel versions before 2007 can only cope with a maximum of 3
 *  Conditional Formatting rules per sheet. Excel 2007 or newer can cope with
 *  unlimited numbers, as can Apache OpenOffice. This is an Excel limitation,
 *  not a file format one.</p>
 */
public final class CFRecordsAggregate extends RecordAggregate {
    /** Excel 97-2003 allows up to 3 conditional formating rules */
    private static final int MAX_97_2003_CONDTIONAL_FORMAT_RULES = 3;
    private static final POILogger logger = POILogFactory.getLogger(CFRecordsAggregate.class);

    private final CFHeaderBase header;

    /** List of CFRuleRecord objects */
    private final List<CFRuleBase> rules;

    private CFRecordsAggregate(CFHeaderBase pHeader, CFRuleBase[] pRules) {
        if(pHeader == null) {
            throw new IllegalArgumentException("header must not be null");
        }
        if(pRules == null) {
            throw new IllegalArgumentException("rules must not be null");
        }
        if(pRules.length > MAX_97_2003_CONDTIONAL_FORMAT_RULES) {
            logger.log(POILogger.WARN, "Excel versions before 2007 require that "
                    + "No more than " + MAX_97_2003_CONDTIONAL_FORMAT_RULES 
                    + " rules may be specified, " + pRules.length + " were found,"
                    + " this file will cause problems with old Excel versions");
        }
        if (pRules.length != pHeader.getNumberOfConditionalFormats()) {
            throw new RecordFormatException("Mismatch number of rules");
        }
        header = pHeader;
        rules = new ArrayList<CFRuleBase>(pRules.length);
        for (int i = 0; i < pRules.length; i++) {
            checkRuleType(pRules[i]);
            rules.add(pRules[i]);
        }
    }

    public CFRecordsAggregate(CellRangeAddress[] regions, CFRuleBase[] rules) {
        this(createHeader(regions, rules), rules);
    }
    private static CFHeaderBase createHeader(CellRangeAddress[] regions, CFRuleBase[] rules) {
        if (rules.length == 0 || rules[0] instanceof CFRuleRecord) {
            return new CFHeaderRecord(regions, rules.length);
        }
        return new CFHeader12Record(regions, rules.length);
    }

    /**
     * Create CFRecordsAggregate from a list of CF Records
     * @param rs - the stream to read from
     * @return CFRecordsAggregate object
     */
    public static CFRecordsAggregate createCFAggregate(RecordStream rs) {
        Record rec = rs.getNext();
        if (rec.getSid() != CFHeaderRecord.sid &&
            rec.getSid() != CFHeader12Record.sid) {
            throw new IllegalStateException("next record sid was " + rec.getSid() 
                    + " instead of " + CFHeaderRecord.sid + " or " +
                    CFHeader12Record.sid + " as expected");
        }

        CFHeaderBase header = (CFHeaderBase)rec;
        int nRules = header.getNumberOfConditionalFormats();

        CFRuleBase[] rules = new CFRuleBase[nRules];
        for (int i = 0; i < rules.length; i++) {
            rules[i] = (CFRuleBase) rs.getNext();
        }

        return new CFRecordsAggregate(header, rules);
    }

    /**
     * Create a deep clone of the record
     */
    public CFRecordsAggregate cloneCFAggregate() {
        CFRuleBase[] newRecs = new CFRuleBase[rules.size()];
        for (int i = 0; i < newRecs.length; i++) {
            newRecs[i] = getRule(i).clone();
        }
        return new CFRecordsAggregate(header.clone(), newRecs);
    }

    /**
     * @return the header. Never <code>null</code>.
     */
    public CFHeaderBase getHeader() {
        return header;
    }

    private void checkRuleIndex(int idx) {
        if(idx < 0 || idx >= rules.size()) {
            throw new IllegalArgumentException("Bad rule record index (" + idx 
                    + ") nRules=" + rules.size());
        }
    }
    private void checkRuleType(CFRuleBase r) {
        if (header instanceof CFHeaderRecord &&
                 r instanceof CFRuleRecord) {
            return;
        }
        if (header instanceof CFHeader12Record &&
                 r instanceof CFRule12Record) {
           return;
        }
        throw new IllegalArgumentException("Header and Rule must both be CF or both be CF12, can't mix");
    }

    public CFRuleBase getRule(int idx) {
        checkRuleIndex(idx);
        return rules.get(idx);
    }
    public void setRule(int idx, CFRuleBase r) {
        if (r == null) {
            throw new IllegalArgumentException("r must not be null");
        }
        checkRuleIndex(idx);
        checkRuleType(r);
        rules.set(idx, r);
    }
    public void addRule(CFRuleBase r) {
        if (r == null) {
            throw new IllegalArgumentException("r must not be null");
        }
        if(rules.size() >= MAX_97_2003_CONDTIONAL_FORMAT_RULES) {
            logger.log(POILogger.WARN, "Excel versions before 2007 cannot cope with" 
                    + " any more than " + MAX_97_2003_CONDTIONAL_FORMAT_RULES 
                    + " - this file will cause problems with old Excel versions");
        }
        checkRuleType(r);
        rules.add(r);
        header.setNumberOfConditionalFormats(rules.size());
    }
    public int getNumberOfRules() {
        return rules.size();
    }

    /**
     * String representation of CFRecordsAggregate
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        String type = "CF";
        if (header instanceof CFHeader12Record) {
            type = "CF12";
        }

        buffer.append("[").append(type).append("]\n");
        if( header != null ) {
            buffer.append(header.toString());
        }
        for(int i=0; i<rules.size(); i++) {
            CFRuleBase cfRule = rules.get(i);
            buffer.append(cfRule.toString());
        }
        buffer.append("[/").append(type).append("]\n");
        return buffer.toString();
    }

    public void visitContainedRecords(RecordVisitor rv) {
        rv.visitRecord(header);
        for(int i=0; i<rules.size(); i++) {
            CFRuleBase rule = rules.get(i);
            rv.visitRecord(rule);
        }
    }

    /**
     * @return <code>false</code> if this whole {@link CFHeaderRecord} / {@link CFRuleRecord}s should be deleted
     */
    public boolean updateFormulasAfterCellShift(FormulaShifter shifter, int currentExternSheetIx) {
        CellRangeAddress[] cellRanges = header.getCellRanges();
        boolean changed = false;
        List<CellRangeAddress> temp = new ArrayList<CellRangeAddress>();
        for (int i = 0; i < cellRanges.length; i++) {
            CellRangeAddress craOld = cellRanges[i];
            CellRangeAddress craNew = shiftRange(shifter, craOld, currentExternSheetIx);
            if (craNew == null) {
                changed = true;
                continue;
            }
            temp.add(craNew);
            if (craNew != craOld) {
                changed = true;
            }
        }

        if (changed) {
            int nRanges = temp.size();
            if (nRanges == 0) {
                return false;
            }
            CellRangeAddress[] newRanges = new CellRangeAddress[nRanges];
            temp.toArray(newRanges);
            header.setCellRanges(newRanges);
        }

        for(int i=0; i<rules.size(); i++) {
            CFRuleBase rule = rules.get(i);
            Ptg[] ptgs;
            ptgs = rule.getParsedExpression1();
            if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                rule.setParsedExpression1(ptgs);
            }
            ptgs = rule.getParsedExpression2();
            if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                rule.setParsedExpression2(ptgs);
            }
            if (rule instanceof CFRule12Record) {
                CFRule12Record rule12 = (CFRule12Record)rule;
                ptgs = rule12.getParsedExpressionScale();
                if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
                    rule12.setParsedExpressionScale(ptgs);
                }
            }
        }
        return true;
    }

    private static CellRangeAddress shiftRange(FormulaShifter shifter, CellRangeAddress cra, int currentExternSheetIx) {
        // FormulaShifter works well in terms of Ptgs - so convert CellRangeAddress to AreaPtg (and back) here
        AreaPtg aptg = new AreaPtg(cra.getFirstRow(), cra.getLastRow(), cra.getFirstColumn(), cra.getLastColumn(), false, false, false, false);
        Ptg[] ptgs = { aptg, };

        if (!shifter.adjustFormula(ptgs, currentExternSheetIx)) {
            return cra;
        }
        Ptg ptg0 = ptgs[0];
        if (ptg0 instanceof AreaPtg) {
            AreaPtg bptg = (AreaPtg) ptg0;
            return new CellRangeAddress(bptg.getFirstRow(), bptg.getLastRow(), bptg.getFirstColumn(), bptg.getLastColumn());
        }
        if (ptg0 instanceof AreaErrPtg) {
            return null;
        }
        throw new IllegalStateException("Unexpected shifted ptg class (" + ptg0.getClass().getName() + ")");
    }
}
