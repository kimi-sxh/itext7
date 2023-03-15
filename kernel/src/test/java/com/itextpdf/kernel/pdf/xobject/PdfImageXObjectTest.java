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
package com.itextpdf.kernel.pdf.xobject;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfImageXObjectTest extends ExtendedITextTest {

    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/kernel/pdf/xobject/PdfImageXObjectTest/";
    private static final String DESTINATION_FOLDER = "./target/test/com/itextpdf/kernel/pdf/xobject/PdfImageXObjectTest/";

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    public void addFlushedImageXObjectToCanvas() throws IOException, InterruptedException {
        String filename = DESTINATION_FOLDER + "addFlushedImageXObjectToCanvas.pdf";
        String cmpfile = SOURCE_FOLDER + "cmp_addFlushedImageXObjectToCanvas.pdf";
        String image = SOURCE_FOLDER + "image.png";

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filename));

        PdfImageXObject imageXObject = new PdfImageXObject(ImageDataFactory.create(image));
        // flushing pdf object directly
        imageXObject.getPdfObject().makeIndirect(pdfDoc).flush();

        PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());

        canvas.addXObjectFittedIntoRectangle(imageXObject, new Rectangle(50, 500, 200, 200));
        pdfDoc.close();

        Assert.assertNull(new CompareTool().compareByContent(filename, cmpfile, DESTINATION_FOLDER));
    }

    @Test
    public void indexedColorPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "indexed.pdf",
                SOURCE_FOLDER + "cmp_indexed.pdf",
                SOURCE_FOLDER + "indexed.png");
    }

    @Test
    public void indexedColorSimpleTransparencyPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "indexedSimpleTransparency.pdf",
                SOURCE_FOLDER + "cmp_indexedSimpleTransparency.pdf",
                SOURCE_FOLDER + "indexedSimpleTransparency.png");
    }

    @Test
    public void grayPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "grayscale16Bpc.pdf",
                SOURCE_FOLDER + "cmp_grayscale16Bpc.pdf",
                SOURCE_FOLDER + "grayscale16Bpc.png");
    }

    @Test
    public void grayAlphaPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "graya8Bpc.pdf",
                SOURCE_FOLDER + "cmp_graya8Bpc.pdf",
                SOURCE_FOLDER + "graya8Bpc.png");
    }

    @Test
    public void grayAlphaPngWithoutEmbeddedProfileImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "graya8BpcWithoutProfile.pdf",
                SOURCE_FOLDER + "cmp_graya8BpcWithoutProfile.pdf",
                SOURCE_FOLDER + "graya8BpcWithoutProfile.png");
    }

    @Test
    public void graySimpleTransparencyPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "grayscaleSimpleTransparencyImage.pdf",
                SOURCE_FOLDER + "cmp_grayscaleSimpleTransparencyImage.pdf",
                SOURCE_FOLDER + "grayscaleSimpleTransparencyImage.png");
    }

    @Test
    public void rgbPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "rgb16Bpc.pdf",
                SOURCE_FOLDER + "cmp_rgb16Bpc.pdf",
                SOURCE_FOLDER + "rgb16Bpc.png");
    }

    @Test
    public void rgbAlphaPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "rgba16Bpc.pdf",
                SOURCE_FOLDER + "cmp_rgba16Bpc.pdf",
                SOURCE_FOLDER + "rgba16Bpc.png");
    }

    @Test
    public void rgbSimpleTransparencyPngImageXObjectTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "rgbSimpleTransparencyImage.pdf",
                SOURCE_FOLDER + "cmp_rgbSimpleTransparencyImage.pdf",
                SOURCE_FOLDER + "rgbSimpleTransparencyImage.png");
    }

    @Test
    public void sRgbImageTest() throws IOException {
        convertAndCompare(DESTINATION_FOLDER + "sRGBImage.pdf",
                SOURCE_FOLDER + "cmp_sRGBImage.pdf",
                SOURCE_FOLDER + "sRGBImage.png");
    }

    @Test
    public void group3CompressionTiffImageTest() throws IOException {
        String image = SOURCE_FOLDER + "group3CompressionImage.tif";
        convertAndCompare(DESTINATION_FOLDER + "group3CompressionTiffImage.pdf",
                SOURCE_FOLDER + "cmp_group3CompressionTiffImage.pdf",
                new PdfImageXObject(ImageDataFactory.create(UrlUtil.toURL(image))));
    }

    @Test
    public void group3CompTiffImgRecoverErrorAndDirectTest() throws IOException, InterruptedException {
        String filename = DESTINATION_FOLDER + "group3CompTiffImgRecoverErrorAndDirect.pdf";
        String cmpFile = SOURCE_FOLDER + "cmp_group3CompTiffImgRecoverErrorAndDirect.pdf";
        String image = SOURCE_FOLDER + "group3CompressionImage.tif";

        try (PdfWriter writer = new PdfWriter(filename);
                PdfDocument pdfDoc = new PdfDocument(writer)) {

            PdfImageXObject imageXObject = new PdfImageXObject(ImageDataFactory.createTiff(UrlUtil.toURL(image),
                    true, 1, true));

            PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());

            canvas.addXObjectFittedIntoRectangle(imageXObject, new Rectangle(50, 500, 200, 200));
        }

        Assert.assertNull(new CompareTool().compareByContent(filename, cmpFile, DESTINATION_FOLDER));
    }

    @Test
    public void group3CompTiffImgNoRecoverErrorAndNotDirectTest() throws IOException {
        String image = SOURCE_FOLDER + "group3CompressionImage.tif";

        convertAndCompare(DESTINATION_FOLDER + "group3CompTiffImgNoRecoverErrorAndNotDirect.pdf",
                SOURCE_FOLDER + "cmp_group3CompTiffImgNoRecoverErrorAndNotDirect.pdf",
                new PdfImageXObject(ImageDataFactory.createTiff(UrlUtil.toURL(image),
                        false, 1, false)));
    }

    @Test
    public void redundantDecodeParmsTest() throws IOException, InterruptedException {
        String srcFilename = SOURCE_FOLDER + "redundantDecodeParms.pdf";
        String destFilename = DESTINATION_FOLDER + "redundantDecodeParms.pdf";
        String cmpFilename = SOURCE_FOLDER + "cmp_redundantDecodeParms.pdf";

        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(srcFilename),
                new PdfWriter(new FileOutputStream(destFilename)),
                new StampingProperties())) {
        }

        Assert.assertNull(new CompareTool().compareByContent(destFilename, cmpFilename, DESTINATION_FOLDER));
    }

    private void convertAndCompare(String outFilename, String cmpFilename, String imageFilename)
            throws IOException {

        System.out.println("Out pdf: " + UrlUtil.getNormalizedFileUriString(outFilename));
        System.out.println("Cmp pdf: " + UrlUtil.getNormalizedFileUriString(cmpFilename)+ "\n");

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFilename));

        PdfImageXObject imageXObject = new PdfImageXObject(ImageDataFactory.create(imageFilename));

        PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());
        canvas.addXObjectFittedIntoRectangle(imageXObject, new Rectangle(50, 500, 346, imageXObject.getHeight()));
        pdfDoc.close();

        PdfDocument outDoc = new PdfDocument(new PdfReader(outFilename));

        PdfStream outStream = outDoc.getFirstPage().getResources().getResource(PdfName.XObject).getAsStream(new PdfName("Im1"));

        PdfDocument cmpDoc = new PdfDocument(new PdfReader(cmpFilename));
        PdfStream cmpStream = cmpDoc.getFirstPage().getResources().getResource(PdfName.XObject).getAsStream(new PdfName("Im1"));


        Assert.assertNull(new CompareTool().compareStreamsStructure(outStream, cmpStream));

        cmpDoc.close();
        outDoc.close();
    }

    private void convertAndCompare(String outFilename, String cmpFilename,PdfImageXObject imageXObject )
            throws IOException {

        System.out.println("Out pdf: " + UrlUtil.getNormalizedFileUriString(outFilename));
        System.out.println("Cmp pdf: " + UrlUtil.getNormalizedFileUriString(cmpFilename)+ "\n");

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFilename));


        PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());
        canvas.addXObjectFittedIntoRectangle(imageXObject, new Rectangle(10, 20, 575 , 802));
        pdfDoc.close();

        PdfDocument outDoc = new PdfDocument(new PdfReader(outFilename));

        PdfStream outStream = outDoc.getFirstPage().getResources().getResource(PdfName.XObject).getAsStream(new PdfName("Im1"));

        PdfDocument cmpDoc = new PdfDocument(new PdfReader(cmpFilename));
        PdfStream cmpStream = cmpDoc.getFirstPage().getResources().getResource(PdfName.XObject).getAsStream(new PdfName("Im1"));


        Assert.assertNull(new CompareTool().compareStreamsStructure(outStream, cmpStream));

        cmpDoc.close();
        outDoc.close();
    }
}
