/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.pdfa;

import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import com.itextpdf.test.pdfa.VeraPdfValidator; // Android-Conversion-Skip-Line (TODO DEVSIX-7377 introduce pdf\a validation on Android)

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfA3CatalogCheckTest extends ExtendedITextTest {
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/pdfa/";
    public static final String cmpFolder = sourceFolder + "cmp/PdfA3CatalogCheckTest/";
    public static final String destinationFolder = "./target/test/com/itextpdf/pdfa/PdfA3CatalogCheckTest/";

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(destinationFolder);
    }

    @Test
    public void catalogCheck01() throws IOException, InterruptedException {
        String outPdf = destinationFolder + "pdfA3b_catalogCheck01.pdf";
        String cmpPdf = cmpFolder + "cmp_pdfA3b_catalogCheck01.pdf";

        PdfWriter writer = new PdfWriter(outPdf);
        InputStream is = new FileInputStream(sourceFolder + "sRGB Color Space Profile.icm");
        PdfADocument doc = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3B, new PdfOutputIntent("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", is));
        doc.addNewPage();

        PdfDictionary ocProperties = new PdfDictionary();

        PdfDictionary d = new PdfDictionary();
        d.put(PdfName.Name, new PdfString("CustomName"));

        PdfDictionary orderItem = new PdfDictionary();
        orderItem.put(PdfName.Name, new PdfString("CustomName2"));

        PdfArray ocgs = new PdfArray();
        ocgs.add(orderItem);

        ocProperties.put(PdfName.OCGs, ocgs);
        ocProperties.put(PdfName.D, d);
        doc.getCatalog().put(PdfName.OCProperties, ocProperties);

        doc.close();
        Assert.assertNull(new VeraPdfValidator().validate(outPdf)); // Android-Conversion-Skip-Line (TODO DEVSIX-7377 introduce pdf\a validation on Android)
        Assert.assertNull(new CompareTool().compareByContent(outPdf, cmpPdf, destinationFolder));

    }

}
