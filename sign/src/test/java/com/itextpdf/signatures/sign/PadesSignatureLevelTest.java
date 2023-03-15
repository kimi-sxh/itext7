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
package com.itextpdf.signatures.sign;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.operator.AbstractOperatorCreationException;
import com.itextpdf.commons.bouncycastle.pkcs.AbstractPKCSException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.LtvVerification;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.SignaturesCompareTool;
import com.itextpdf.signatures.testutils.client.TestCrlClient;
import com.itextpdf.signatures.testutils.client.TestOcspClient;
import com.itextpdf.signatures.testutils.client.TestTsaClient;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.BouncyCastleIntegrationTest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BouncyCastleIntegrationTest.class)
public class PadesSignatureLevelTest extends ExtendedITextTest {

    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();

    private static final String certsSrc = "./src/test/resources/com/itextpdf/signatures/certs/";
    private static final String sourceFolder = "./src/test/resources/com/itextpdf/signatures/sign/PadesSignatureLevelTest/";
    private static final String destinationFolder = "./target/test/com/itextpdf/signatures/sign/PadesSignatureLevelTest/";

    private static final char[] password = "testpassphrase".toCharArray();

    @BeforeClass
    public static void before() {
        Security.addProvider(FACTORY.getProvider());
        createOrClearDestinationFolder(destinationFolder);
    }

    @Test
    public void padesSignatureLevelTTest01()
            throws GeneralSecurityException, IOException, AbstractPKCSException, AbstractOperatorCreationException {
        String outFileName = destinationFolder + "padesSignatureLevelTTest01.pdf";
        String srcFileName = sourceFolder + "helloWorldDoc.pdf";
        String signCertFileName = certsSrc + "signCertRsa01.pem";
        String tsaCertFileName = certsSrc + "tsCertRsa.pem";

        Certificate[] signRsaChain = PemFileHelper.readFirstChain(signCertFileName);
        PrivateKey signRsaPrivateKey = PemFileHelper.readFirstKey(signCertFileName, password);
        IExternalSignature pks =
                new PrivateKeySignature(signRsaPrivateKey, DigestAlgorithms.SHA256, FACTORY.getProviderName());

        Certificate[] tsaChain = PemFileHelper.readFirstChain(tsaCertFileName);
        PrivateKey tsaPrivateKey = PemFileHelper.readFirstKey(tsaCertFileName, password);

        PdfSigner signer = new PdfSigner(new PdfReader(srcFileName), new FileOutputStream(outFileName), new StampingProperties());
        signer.setFieldName("Signature1");
        signer.getSignatureAppearance()
                .setPageRect(new Rectangle(50, 650, 200, 100))
                .setReason("Test")
                .setLocation("TestCity")
                .setLayer2Text("Approval test signature.\nCreated by iText7.");


        TestTsaClient testTsa = new TestTsaClient(Arrays.asList(tsaChain), tsaPrivateKey);

        signer.signDetached(new BouncyCastleDigest(), pks, signRsaChain, null, null, testTsa, 0, PdfSigner.CryptoStandard.CADES);

        PadesSigTest.basicCheckSignedDoc(destinationFolder + "padesSignatureLevelTTest01.pdf", "Signature1");

        Assert.assertNull(SignaturesCompareTool.compareSignatures(
                outFileName, sourceFolder + "cmp_padesSignatureLevelTTest01.pdf"));
    }

    @Test
    public void padesSignatureLevelLTTest01()
            throws GeneralSecurityException, IOException, AbstractPKCSException, AbstractOperatorCreationException {
        String outFileName = destinationFolder + "padesSignatureLevelLTTest01.pdf";
        String srcFileName = sourceFolder + "signedPAdES-T.pdf";
        String caCertFileName = certsSrc + "rootRsa.pem";

        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(caCertFileName)[0];
        PrivateKey caPrivateKey = PemFileHelper.readFirstKey(caCertFileName, password);

        ICrlClient crlClient = new TestCrlClient().addBuilderForCertIssuer(caCert, caPrivateKey);
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, caPrivateKey);

        PdfDocument document = new PdfDocument(new PdfReader(srcFileName), new PdfWriter(outFileName), new StampingProperties().useAppendMode());
        LtvVerification ltvVerification = new LtvVerification(document);
        ltvVerification.addVerification("Signature1", ocspClient, crlClient, LtvVerification.CertificateOption.SIGNING_CERTIFICATE, LtvVerification.Level.OCSP_CRL, LtvVerification.CertificateInclusion.YES);
        ltvVerification.merge();
        document.close();

        Assert.assertNull(SignaturesCompareTool.compareSignatures(
                outFileName, sourceFolder + "cmp_padesSignatureLevelLTTest01.pdf"));
    }

    @Test
    public void padesSignatureLevelLTATest01()
            throws GeneralSecurityException, IOException, AbstractPKCSException, AbstractOperatorCreationException {
        String outFileName = destinationFolder + "padesSignatureLevelLTATest01.pdf";
        String srcFileName = sourceFolder + "signedPAdES-LT.pdf";
        String tsaCertFileName = certsSrc + "tsCertRsa.pem";

        Certificate[] tsaChain = PemFileHelper.readFirstChain(tsaCertFileName);
        PrivateKey tsaPrivateKey = PemFileHelper.readFirstKey(tsaCertFileName, password);

        PdfSigner signer = new PdfSigner(new PdfReader(srcFileName), new FileOutputStream(outFileName), new StampingProperties().useAppendMode());

        TestTsaClient testTsa = new TestTsaClient(Arrays.asList(tsaChain), tsaPrivateKey);
        signer.timestamp(testTsa, "timestampSig1");

        Assert.assertNull(SignaturesCompareTool.compareSignatures(
                outFileName, sourceFolder + "cmp_padesSignatureLevelLTATest01.pdf"));
    }
}
