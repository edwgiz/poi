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

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIgnoredError;

/**
 * Types of ignored error.
 */
public enum IgnoredErrorType {

    CALCULATED_COLUMN,
    
    EMPTY_CELL_REFERENCE,
    
    EVALUATION_ERROR,
    
    FORMULA,
    
    FORMULA_RANGE,
    
    LIST_DATA_VALIDATION,
    
    NUMBER_STORED_AS_TEXT,
    
    TWO_DIGIT_TEXT_YEAR,
    
    UNLOCKED_FORMULA;
    
    // Methods below are not part of the public API
    
    boolean isSet(CTIgnoredError error) {
        switch(this) {
            case CALCULATED_COLUMN:
                return error.isSetCalculatedColumn();
            case EMPTY_CELL_REFERENCE:
                return error.isSetEmptyCellReference();
            case EVALUATION_ERROR:
                return error.isSetEvalError();
            case FORMULA:
                return error.isSetFormula();
            case FORMULA_RANGE:
                return error.isSetFormulaRange();
            case LIST_DATA_VALIDATION:
                return error.isSetListDataValidation();
            case NUMBER_STORED_AS_TEXT:
                return error.isSetNumberStoredAsText();
            case TWO_DIGIT_TEXT_YEAR:
                return error.isSetTwoDigitTextYear();
            case UNLOCKED_FORMULA:
                return error.isSetUnlockedFormula();
            default:
                throw new IllegalStateException();
            }
    }
    
    void set(CTIgnoredError error) {
        switch(this) {
        case CALCULATED_COLUMN:
            error.setCalculatedColumn(true);
            break;
        case EMPTY_CELL_REFERENCE:
            error.setEmptyCellReference(true);
            break;
        case EVALUATION_ERROR:
            error.setEvalError(true);
            break;
        case FORMULA:
            error.setFormula(true);
            break;
        case FORMULA_RANGE:
            error.setFormulaRange(true);
            break;
        case LIST_DATA_VALIDATION:
            error.setListDataValidation(true);
            break;
        case NUMBER_STORED_AS_TEXT:
            error.setNumberStoredAsText(true);
            break;
        case TWO_DIGIT_TEXT_YEAR:
            error.setTwoDigitTextYear(true);
            break;
        case UNLOCKED_FORMULA:
            error.setUnlockedFormula(true);
            break;
        default:
            throw new IllegalStateException();
        }
    }
    
}
