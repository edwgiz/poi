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
package org.apache.poi.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hsmf.extractor.OutlookTextExtactor;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xdgf.extractor.XDGFVisioExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test that the extractor factory plays nicely
 */
public class TestExtractorFactory {
    private static File txt;

    private static File xls;
    private static File xlsx;
    private static File xlsxStrict;
    private static File xltx;
    private static File xlsEmb;

    private static File doc;
    private static File doc6;
    private static File doc95;
    private static File docx;
    private static File dotx;
    private static File docEmb;
    private static File docEmbOOXML;

    private static File ppt;
    private static File pptx;

    private static File msg;
    private static File msgEmb;
    private static File msgEmbMsg;

    private static File vsd;
    private static File vsdx;

    private static File pub;

    private static File getFileAndCheck(POIDataSamples samples, String name) {
        File file = samples.getFile(name);

        assertNotNull("Did not get a file for " + name, file);
        assertTrue("Did not get a type file for " + name, file.isFile());
        assertTrue("File did not exist: " + name, file.exists());

        return file;
    }

    @BeforeClass
    public static void setUp() throws Exception {

        POIDataSamples ssTests = POIDataSamples.getSpreadSheetInstance();
        xls = getFileAndCheck(ssTests, "SampleSS.xls");
        xlsx = getFileAndCheck(ssTests, "SampleSS.xlsx");
        xlsxStrict = getFileAndCheck(ssTests, "SampleSS.strict.xlsx");
        xltx = getFileAndCheck(ssTests, "test.xltx");
        xlsEmb = getFileAndCheck(ssTests, "excel_with_embeded.xls");

        POIDataSamples wpTests = POIDataSamples.getDocumentInstance();
        doc = getFileAndCheck(wpTests, "SampleDoc.doc");
        doc6 = getFileAndCheck(wpTests, "Word6.doc");
        doc95 = getFileAndCheck(wpTests, "Word95.doc");
        docx = getFileAndCheck(wpTests, "SampleDoc.docx");
        dotx = getFileAndCheck(wpTests, "test.dotx");
        docEmb = getFileAndCheck(wpTests, "word_with_embeded.doc");
        docEmbOOXML = getFileAndCheck(wpTests, "word_with_embeded_ooxml.doc");

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        ppt = getFileAndCheck(slTests, "SampleShow.ppt");
        pptx = getFileAndCheck(slTests, "SampleShow.pptx");
        txt = getFileAndCheck(slTests, "SampleShow.txt");

        POIDataSamples dgTests = POIDataSamples.getDiagramInstance();
        vsd = getFileAndCheck(dgTests, "Test_Visio-Some_Random_Text.vsd");
        vsdx = getFileAndCheck(dgTests, "test.vsdx");

        POIDataSamples pubTests = POIDataSamples.getPublisherInstance();
        pub = getFileAndCheck(pubTests, "Simple.pub");

        POIDataSamples olTests = POIDataSamples.getHSMFInstance();
        msg = getFileAndCheck(olTests, "quick.msg");
        msgEmb = getFileAndCheck(olTests, "attachment_test_msg.msg");
        msgEmbMsg = getFileAndCheck(olTests, "attachment_msg_pdf.msg");
    }

    @Test
    public void testFile() throws Exception {
        // Excel
        POITextExtractor xlsExtractor = ExtractorFactory.createExtractor(xls);
        assertNotNull("Had empty extractor for " + xls, xlsExtractor);
        assertTrue("Expected instanceof ExcelExtractor, but had: " + xlsExtractor.getClass(), 
                xlsExtractor
                instanceof ExcelExtractor
        );
        assertTrue(
                xlsExtractor.getText().length() > 200
        );
        xlsExtractor.close();

        POITextExtractor extractor = ExtractorFactory.createExtractor(xlsx);
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xlsx);
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xltx);
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xltx);
        assertTrue(
                extractor.getText().contains("test")
        );
        extractor.close();

        // TODO Support OOXML-Strict, see bug #57699
        try {
            extractor = ExtractorFactory.createExtractor(xlsxStrict);
            fail("OOXML-Strict isn't yet supported");
        } catch (POIXMLException e) {
            // Expected, for now
        }
//        extractor = ExtractorFactory.createExtractor(xlsxStrict);
//        assertTrue(
//                extractor
//                instanceof XSSFExcelExtractor
//        );
//        extractor.close();
//
//        extractor = ExtractorFactory.createExtractor(xlsxStrict);
//        assertTrue(
//                extractor.getText().contains("test")
//        );
//        extractor.close();


        // Word
        extractor = ExtractorFactory.createExtractor(doc);
        assertTrue(
                extractor
                instanceof WordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(doc6);
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(doc95);
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(docx);
        assertTrue(
                extractor instanceof XWPFWordExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(docx);
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(dotx);
        assertTrue(
                extractor instanceof XWPFWordExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(dotx);
        assertTrue(
                extractor.getText().contains("Test")
        );
        extractor.close();

        // PowerPoint (PPT)
        extractor = ExtractorFactory.createExtractor(ppt);
        assertTrue(
                extractor
                instanceof PowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // PowerPoint (PPTX)
        extractor = ExtractorFactory.createExtractor(pptx);
        assertTrue(
                extractor
                instanceof XSLFPowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // Visio - binary
        extractor = ExtractorFactory.createExtractor(vsd);
        assertTrue(
                extractor
                instanceof VisioTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Visio - vsdx
        extractor = ExtractorFactory.createExtractor(vsdx);
        assertTrue(
                extractor
                instanceof XDGFVisioExtractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        // Publisher
        extractor = ExtractorFactory.createExtractor(pub);
        assertTrue(
                extractor
                instanceof PublisherTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Outlook msg
        extractor = ExtractorFactory.createExtractor(msg);
        assertTrue(
                extractor
                instanceof OutlookTextExtactor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Text
        try {
            ExtractorFactory.createExtractor(txt);
            fail();
        } catch(IllegalArgumentException e) {
            // Good
        }
    }

    @Test
    public void testInputStream() throws Exception {
        // Excel
        POITextExtractor extractor = ExtractorFactory.createExtractor(new FileInputStream(xls));
        assertTrue(
                extractor
                instanceof ExcelExtractor
        );
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(xlsx));
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        assertTrue(
                extractor.getText().length() > 200
        );
        // TODO Support OOXML-Strict, see bug #57699
//        assertTrue(
//                ExtractorFactory.createExtractor(new FileInputStream(xlsxStrict))
//                instanceof XSSFExcelExtractor
//        );
//        assertTrue(
//                ExtractorFactory.createExtractor(new FileInputStream(xlsxStrict)).getText().length() > 200
//        );
        extractor.close();

        // Word
        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc));
        assertTrue(
                extractor
                instanceof WordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc6));
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc95));
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(docx));
        assertTrue(
                extractor
                instanceof XWPFWordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // PowerPoint
        extractor = ExtractorFactory.createExtractor(new FileInputStream(ppt));
        assertTrue(
                extractor
                instanceof PowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(pptx));
        assertTrue(
                extractor
                instanceof XSLFPowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // Visio
        extractor = ExtractorFactory.createExtractor(new FileInputStream(vsd));
        assertTrue(
                extractor
                instanceof VisioTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Visio - vsdx
        extractor = ExtractorFactory.createExtractor(new FileInputStream(vsdx));
        assertTrue(
                extractor
                instanceof XDGFVisioExtractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();
        
        // Publisher
        extractor = ExtractorFactory.createExtractor(new FileInputStream(pub));
        assertTrue(
                extractor
                instanceof PublisherTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Outlook msg
        extractor = ExtractorFactory.createExtractor(new FileInputStream(msg));
        assertTrue(
                extractor
                instanceof OutlookTextExtactor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Text
        try {
            FileInputStream stream = new FileInputStream(txt);
            try {
                ExtractorFactory.createExtractor(stream);
                fail();
            } finally {
                stream.close();
            }
        } catch(IllegalArgumentException e) {
            // Good
        }
    }

    @Test
    public void testPOIFS() throws Exception {
        // Excel
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)))
                instanceof ExcelExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls))).getText().length() > 200
        );

        // Word
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc)))
                instanceof WordExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc))).getText().length() > 120
        );

        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc6)))
                instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc6))).getText().length() > 20
        );

        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc95)))
                instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc95))).getText().length() > 120
        );

        // PowerPoint
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt)))
                instanceof PowerPointExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt))).getText().length() > 120
        );

        // Visio
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd)))
                instanceof VisioTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd))).getText().length() > 50
        );

        // Publisher
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(pub)))
                instanceof PublisherTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(pub))).getText().length() > 50
        );

        // Outlook msg
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(msg)))
                instanceof OutlookTextExtactor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(msg))).getText().length() > 50
        );

        // Text
        try {
            ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(txt)));
            fail();
        } catch(IOException e) {
            // Good
        }
    }

    @Test
    public void testPackage() throws Exception {
        // Excel
        POIXMLTextExtractor extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString()));
        assertTrue(extractor.getText().length() > 200);
        extractor.close();

        // Word
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(docx.toString()));
        assertTrue(
                extractor
                instanceof XWPFWordExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(docx.toString()));
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // PowerPoint
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString()));
        assertTrue(
                extractor
                instanceof XSLFPowerPointExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString()));
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();
        
        // Visio
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(vsdx.toString()));
        assertTrue(
                extractor
                instanceof XDGFVisioExtractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        // Text
        try {
            ExtractorFactory.createExtractor(OPCPackage.open(txt.toString()));
            fail();
        } catch(InvalidOperationException e) {
            // Good
        }
    }

    @Test
    public void testPreferEventBased() throws Exception {
        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertFalse(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setThreadPrefersEventExtractors(true);

        assertTrue(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setAllThreadsPreferEventExtractors(false);

        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertEquals(Boolean.FALSE, ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setAllThreadsPreferEventExtractors(null);

        assertTrue(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());


        // Check we get the right extractors now
        POITextExtractor extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor
                instanceof EventBasedExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(extractor instanceof XSSFEventBasedExcelExtractor);
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();


        // Put back to normal
        ExtractorFactory.setThreadPrefersEventExtractors(false);
        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertFalse(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        // And back
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor
                instanceof ExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString()));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();
    }

    /**
     * Test embeded docs text extraction. For now, only
     *  does poifs embeded, but will do ooxml ones 
     *  at some point.
     */
    @Test
    public void testEmbeded() throws Exception {
        POIOLE2TextExtractor ext;
        POITextExtractor[] embeds;

        // No embedings
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(xls);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);
        assertEquals(0, embeds.length);
        ext.close();

        // Excel
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(xlsEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        assertEquals(6, embeds.length);
        int numWord = 0, numXls = 0, numPpt = 0, numMsg = 0, numWordX;
        for(int i=0; i<embeds.length; i++) {
            assertTrue(embeds[i].getText().length() > 20);

            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
            else if(embeds[i] instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(2, numPpt);
        assertEquals(2, numXls);
        assertEquals(2, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Word
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(docEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(4, embeds.length);
        for(int i=0; i<embeds.length; i++) {
            assertTrue(embeds[i].getText().length() > 20);
            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
            else if(embeds[i] instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(1, numPpt);
        assertEquals(2, numXls);
        assertEquals(1, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Word which contains an OOXML file
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(docEmbOOXML);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0; numWordX = 0;
        assertEquals(3, embeds.length);
        for(int i=0; i<embeds.length; i++) {
            assertTrue(embeds[i].getText().length() > 20);
            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
            else if(embeds[i] instanceof OutlookTextExtactor) numMsg++;
            else if(embeds[i] instanceof XWPFWordExtractor) numWordX++;
        }
        assertEquals(1, numPpt);
        assertEquals(1, numXls);
        assertEquals(0, numWord);
        assertEquals(1, numWordX);
        assertEquals(0, numMsg);
        ext.close();

        // Outlook
        ext = (OutlookTextExtactor)
                ExtractorFactory.createExtractor(msgEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(1, embeds.length);
        for(int i=0; i<embeds.length; i++) {
            assertTrue(embeds[i].getText().length() > 20);
            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
            else if(embeds[i] instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(0, numPpt);
        assertEquals(0, numXls);
        assertEquals(1, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Outlook with another outlook file in it
        ext = (OutlookTextExtactor)
                ExtractorFactory.createExtractor(msgEmbMsg);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(1, embeds.length);
        for(int i=0; i<embeds.length; i++) {
            assertTrue(embeds[i].getText().length() > 20);
            if(embeds[i] instanceof PowerPointExtractor) numPpt++;
            else if(embeds[i] instanceof ExcelExtractor) numXls++;
            else if(embeds[i] instanceof WordExtractor) numWord++;
            else if(embeds[i] instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(0, numPpt);
        assertEquals(0, numXls);
        assertEquals(0, numWord);
        assertEquals(1, numMsg);
        ext.close();

        // TODO - PowerPoint
        // TODO - Publisher
        // TODO - Visio
    }
}
