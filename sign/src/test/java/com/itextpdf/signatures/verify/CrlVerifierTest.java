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
package com.itextpdf.signatures.verify;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.operator.AbstractOperatorCreationException;
import com.itextpdf.commons.bouncycastle.pkcs.AbstractPKCSException;
import com.itextpdf.commons.utils.DateTimeUtil;
import com.itextpdf.signatures.CRLVerifier;
import com.itextpdf.signatures.VerificationException;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.SignTestPortUtil;
import com.itextpdf.signatures.testutils.builder.TestCrlBuilder;
import com.itextpdf.signatures.testutils.client.TestCrlClient;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.BouncyCastleUnitTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;

@Category(BouncyCastleUnitTest.class)
public class CrlVerifierTest extends ExtendedITextTest {
    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();
    
    private static final String certsSrc = "./src/test/resources/com/itextpdf/signatures/certs/";
    private static final char[] password = "testpassphrase".toCharArray();

    @BeforeClass
    public static void before() {
        Security.addProvider(FACTORY.getProvider());
    }

    @Test
    public void validCrl01() throws GeneralSecurityException, IOException, AbstractPKCSException,
            AbstractOperatorCreationException {
        String caCertP12FileName = certsSrc + "rootRsa.pem";
        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(caCertP12FileName)[0];
        PrivateKey caPrivateKey = PemFileHelper.readFirstKey(caCertP12FileName, password);
        TestCrlBuilder crlBuilder = new TestCrlBuilder(caCert, caPrivateKey, DateTimeUtil.addDaysToDate(DateTimeUtil.getCurrentTimeDate(), -1));
        Assert.assertTrue(verifyTest(crlBuilder));
    }

    @Test
    public void invalidRevokedCrl01()
            throws GeneralSecurityException, IOException, AbstractPKCSException, AbstractOperatorCreationException {
        String caCertP12FileName = certsSrc + "rootRsa.pem";
        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(caCertP12FileName)[0];
        PrivateKey caPrivateKey = PemFileHelper.readFirstKey(caCertP12FileName, password);
        TestCrlBuilder crlBuilder = new TestCrlBuilder(caCert, caPrivateKey, DateTimeUtil.addDaysToDate(DateTimeUtil.getCurrentTimeDate(), -1));

        String checkCertFileName = certsSrc + "signCertRsa01.pem";
        X509Certificate checkCert = (X509Certificate) PemFileHelper.readFirstChain(checkCertFileName)[0];
        crlBuilder.addCrlEntry(checkCert, DateTimeUtil.addDaysToDate(DateTimeUtil.getCurrentTimeDate(), -40),
                FACTORY.createCRLReason().getKeyCompromise());

        Assert.assertThrows(VerificationException.class, () -> verifyTest(crlBuilder));
    }

    @Test
    public void invalidOutdatedCrl01()
            throws GeneralSecurityException, IOException, AbstractPKCSException, AbstractOperatorCreationException {
        String caCertP12FileName = certsSrc + "rootRsa.pem";
        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(caCertP12FileName)[0];
        PrivateKey caPrivateKey = PemFileHelper.readFirstKey(caCertP12FileName, password);
        TestCrlBuilder crlBuilder = new TestCrlBuilder(caCert, caPrivateKey, DateTimeUtil.addDaysToDate(DateTimeUtil.getCurrentTimeDate(), -2));
        crlBuilder.setNextUpdate(DateTimeUtil.addDaysToDate(DateTimeUtil.getCurrentTimeDate(), -1));

        Assert.assertFalse(verifyTest(crlBuilder));
    }

    private boolean verifyTest(TestCrlBuilder crlBuilder) throws GeneralSecurityException, IOException {
        String caCertFileName = certsSrc + "rootRsa.pem";
        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(caCertFileName)[0];
        String checkCertFileName = certsSrc + "signCertRsa01.pem";
        X509Certificate checkCert = (X509Certificate) PemFileHelper.readFirstChain(checkCertFileName)[0];


        TestCrlClient crlClient = new TestCrlClient().addBuilderForCertIssuer(crlBuilder);
        Collection<byte[]> crlBytesCollection = crlClient.getEncoded(checkCert, null);

        boolean verify = false;
        for (byte[] crlBytes : crlBytesCollection) {
            X509CRL crl = (X509CRL) SignTestPortUtil.parseCrlFromStream(new ByteArrayInputStream(crlBytes));
            CRLVerifier verifier = new CRLVerifier(null, null);
            verify = verifier.verify(crl, checkCert, caCert, DateTimeUtil.getCurrentTimeDate());
            break;
        }
        return verify;
    }
}
