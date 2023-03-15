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
package com.itextpdf.kernel.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.tagging.IStructureNode;
import com.itextpdf.kernel.pdf.tagging.PdfStructElem;
import com.itextpdf.kernel.pdf.tagging.PdfStructTreeRoot;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class PdfStructTreeRootTest extends ExtendedITextTest {
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/kernel/pdf/PdfStructTreeRootTest/";
    public static final String destinationFolder = "./target/test/com/itextpdf/kernel/pdf/PdfStructTreeRootTest/";

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(destinationFolder);
    }

    @Test
    public void directStructTreeRootReadingModeTest() throws IOException {
        PdfDocument document = new PdfDocument(new PdfReader(sourceFolder + "directStructTreeRoot.pdf"));
        assertTrue(document.isTagged());
        document.close();
    }

    @Test
    public void directStructTreeRootStampingModeTest() throws IOException {
        PdfDocument document = new PdfDocument(new PdfReader(sourceFolder + "directStructTreeRoot.pdf"),
                new PdfWriter(new ByteArrayOutputStream()));
        assertTrue(document.isTagged());
        document.close();
    }

    @Test
    public void severalSameElementsInStructTreeRootTest() throws IOException {
        String inFile = sourceFolder + "severalSameElementsInStructTreeRoot.pdf";

        PdfDocument doc = new PdfDocument(new PdfReader(inFile), new PdfWriter(new ByteArrayOutputStream()));

        PdfStructTreeRoot structTreeRoot = doc.getStructTreeRoot();

        List<PdfStructElem> kidsOfStructTreeRootKids = new ArrayList<>();
        for (IStructureNode kid : structTreeRoot.getKids()) {
            for (IStructureNode kidOfKid : kid.getKids()) {
                if (kidOfKid instanceof PdfStructElem) {
                    kidsOfStructTreeRootKids.add((PdfStructElem) kidOfKid);
                }
            }
        }

        structTreeRoot.flush();

        for (PdfStructElem kidsOfStructTreeRootKid : kidsOfStructTreeRootKids) {
            Assert.assertTrue(kidsOfStructTreeRootKid.isFlushed());
        }
    }

    @Test
    public void idTreeIsLazyTest() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(os).setCompressionLevel(CompressionConstants.NO_COMPRESSION);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setTagged();

        pdfDoc.addNewPage().getFirstContentStream().setData("q Q".getBytes(StandardCharsets.UTF_8));

        pdfDoc.getStructTreeRoot().getIdTree();
        pdfDoc.close();

        // we've retrieved the ID tree but not used it -> it should be left out in the resulting file
        PdfReader r = new PdfReader(new ByteArrayInputStream(os.toByteArray()));
        PdfDocument readPdfDoc = new PdfDocument(r);
        Assert.assertFalse(readPdfDoc.getStructTreeRoot().getPdfObject().containsKey(PdfName.IDTree));

    }
}
