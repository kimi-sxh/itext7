/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.kernel.utils;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class ValidationContextTest extends ExtendedITextTest {
    @Test
    public void withDocumentsCheckTest() {
        ValidationContext context = new ValidationContext();
        Assert.assertNull(context.getPdfDocument());
        context.withPdfDocument(new PdfDocument(new PdfWriter(new ByteArrayOutputStream())));
        Assert.assertNotNull(context.getPdfDocument());
    }

    @Test
    public void withFontsCheckTest() {
        ValidationContext context = new ValidationContext();
        Assert.assertNull(context.getFonts());
        List<PdfFont> fonts = new ArrayList<>();
        context.withFonts(fonts);
        Assert.assertNotNull(context.getFonts());
    }
}
