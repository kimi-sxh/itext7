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
package com.itextpdf.io.util;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
// Android-Conversion-Skip-Line (Security provider is required for working getFinalConnection through SSL on Android)
// Android-Conversion-Replace import java.security.Security;
// Android-Conversion-Replace import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class UrlUtilTest extends ExtendedITextTest {

    private static final String destinationFolder = "./target/test/com/itextpdf/io/UrlUtilTest/";

    @BeforeClass
    public static void beforeClass() {
        // Android-Conversion-Skip-Line (Security provider is required for working getFinalConnection through SSL on Android)
        // Android-Conversion-Replace Security.addProvider(new BouncyCastleProvider());
        createDestinationFolder(destinationFolder);
    }


    // Tests that after invocation of the getFinalURL method for local files, no handles are left open and the file
    // is free to be removed.
    @Test
    public void getFinalURLDoesNotLockFileTest() throws IOException {
        File tempFile = FileUtil.createTempFile(destinationFolder);

        UrlUtil.getFinalURL(UrlUtil.toURL(tempFile.getAbsolutePath()));

        Assert.assertTrue(FileUtil.deleteFile(tempFile));
    }

    // Tests, that getFinalConnection will be redirected some times for other urls, and initialUrl will be different
    // from final url.
    @Test
    public void getFinalConnectionWhileRedirectingTest() throws IOException {
        URL initialUrl = new URL("http://itextpdf.com");
        URL expectedURL = new URL("https://itextpdf.com/");
        URLConnection finalConnection = null;

        try {
            finalConnection = UrlUtil.getFinalConnection(initialUrl);

            Assert.assertNotNull(finalConnection);
            Assert.assertNotEquals(initialUrl, finalConnection.getURL());
            Assert.assertEquals(expectedURL, finalConnection.getURL());
        } finally {
            finalConnection.getInputStream().close();
        }
    }

    // This test checks that when we pass invalid url and trying get stream related to final redirected url,exception
    // would be thrown.
    @Test
    public void getInputStreamOfFinalConnectionThrowExceptionTest() throws IOException {
        URL invalidUrl = new URL("http://itextpdf");

        Assert.assertThrows(UnknownHostException.class, () -> UrlUtil.getInputStreamOfFinalConnection(invalidUrl));
    }

    // This test checks that when we pass valid url and trying get stream related to final redirected url, it would
    // not be null.
    @Test
    public void getInputStreamOfFinalConnectionTest() throws IOException {
        URL initialUrl = new URL("http://itextpdf.com");
        InputStream streamOfFinalConnectionOfInvalidUrl = UrlUtil.getInputStreamOfFinalConnection(initialUrl);

        Assert.assertNotNull(streamOfFinalConnectionOfInvalidUrl);
    }

    @Test
    // Android-Conversion-Ignore-Test (TODO DEVSIX-7371 investigate different behavior of a few iTextCore tests on Java and Android)
    public void getBaseUriTest() throws IOException {
        String absolutePathRoot = Paths.get("").toAbsolutePath().toUri().toURL().toExternalForm();
        // Android-Conversion-Skip-Line (TODO DEVSIX-7371 investigate different behavior of a few iTextCore tests on Java and Android)
        String expected = absolutePathRoot + destinationFolder; // Android-Conversion-Replace String expected = absolutePathRoot + destinationFolder.substring(1);
        File tempFile = FileUtil.createTempFile(destinationFolder);
        Assert.assertEquals(expected, FileUtil.getParentDirectoryUri(tempFile));
    }

    @Test
    public void nullBaseUriTest() throws IOException {
        String expected = "";
        File tempFile = null;
        Assert.assertEquals(expected, FileUtil.getParentDirectoryUri(tempFile));
    }

    @Test
    public void openStreamTest() throws IOException {
        String resPath = "./src/test/resources/com/itextpdf/io/util/textFile.dat";
        InputStream openStream = UrlUtil.openStream(new File(resPath).toURI().toURL());

        String actual = new String(StreamUtil.inputStreamToArray(openStream), StandardCharsets.UTF_8);
        Assert.assertEquals("Hello world from text file!", actual);

    }
}
