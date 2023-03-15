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
import com.itextpdf.commons.bouncycastle.asn1.IASN1ObjectIdentifier;
import com.itextpdf.commons.bouncycastle.asn1.IDEROctetString;
import com.itextpdf.commons.bouncycastle.asn1.esf.ISignaturePolicyId;
import com.itextpdf.commons.bouncycastle.asn1.esf.ISignaturePolicyIdentifier;
import com.itextpdf.commons.bouncycastle.asn1.x509.IAlgorithmIdentifier;
import com.itextpdf.commons.bouncycastle.operator.AbstractOperatorCreationException;
import com.itextpdf.commons.bouncycastle.pkcs.AbstractPKCSException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignaturePolicyInfo;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.SignaturesCompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.BouncyCastleIntegrationTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

@Category(BouncyCastleIntegrationTest.class)
public class PadesSigTest extends ExtendedITextTest {

    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();
    
    private static final String certsSrc = "./src/test/resources/com/itextpdf/signatures/certs/";
    private static final String sourceFolder = "./src/test/resources/com/itextpdf/signatures/sign/PadesSigTest/";
    private static final String destinationFolder = "./target/test/com/itextpdf/signatures/sign/PadesSigTest/";

    private static final char[] password = "testpassphrase".toCharArray();

    @BeforeClass
    public static void before() {
        Security.addProvider(FACTORY.getProvider());
        createOrClearDestinationFolder(destinationFolder);
    }

    @Test
    public void padesRsaSigTest01()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(certsSrc + "signCertRsa01.pem", destinationFolder + "padesRsaSigTest01.pdf");

        basicCheckSignedDoc(destinationFolder + "padesRsaSigTest01.pdf", "Signature1");
        Assert.assertNull(SignaturesCompareTool.compareSignatures(destinationFolder
                + "padesRsaSigTest01.pdf", sourceFolder + "cmp_padesRsaSigTest01.pdf"));
    }

    @Test
    public void padesRsaSigTestWithChain01()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(certsSrc + "signCertRsaWithChain.pem", destinationFolder + "padesRsaSigTestWithChain01.pdf");

        basicCheckSignedDoc(destinationFolder + "padesRsaSigTestWithChain01.pdf", "Signature1");
        Assert.assertNull(SignaturesCompareTool.compareSignatures(destinationFolder
                + "padesRsaSigTestWithChain01.pdf", sourceFolder + "cmp_padesRsaSigTestWithChain01.pdf"));
    }

    @Test
    @Ignore("DEVSIX-1620: For some reason signatures created with the given cert (either by iText or acrobat) are considered invalid")
    public void padesDsaSigTest01()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(certsSrc + "signCertDsa01.pem", destinationFolder + "padesDsaSigTest01.pdf");
    }

    @Test
    public void padesEccSigTest01()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(certsSrc + "signCertEcc01.pem",
                destinationFolder + "padesEccSigTest01.pdf");

        basicCheckSignedDoc(destinationFolder + "padesEccSigTest01.pdf", "Signature1");
        Assert.assertNull(SignaturesCompareTool.compareSignatures(destinationFolder
                + "padesEccSigTest01.pdf", sourceFolder + "cmp_padesEccSigTest01.pdf"));
    }

    @Test
    public void padesEpesProfileTest01()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        String notExistingSignaturePolicyOid = "2.16.724.631.3.1.124.2.29.9";
        IASN1ObjectIdentifier asn1PolicyOid = FACTORY.createASN1ObjectIdentifierInstance(
                FACTORY.createASN1ObjectIdentifier(notExistingSignaturePolicyOid));
        IAlgorithmIdentifier hashAlg = FACTORY.createAlgorithmIdentifier(
                FACTORY.createASN1ObjectIdentifier(DigestAlgorithms.getAllowedDigest("SHA1")));

        // indicate that the policy hash value is not known; see ETSI TS 101 733 V2.2.1, 5.8.1
        byte[] zeroSigPolicyHash = {0};
        IDEROctetString hash = FACTORY.createDEROctetString(zeroSigPolicyHash);

        ISignaturePolicyId signaturePolicyId =
                FACTORY.createSignaturePolicyId(asn1PolicyOid, FACTORY.createOtherHashAlgAndValue(hashAlg, hash));
        ISignaturePolicyIdentifier sigPolicyIdentifier = FACTORY.createSignaturePolicyIdentifier(signaturePolicyId);

        signApproval(certsSrc + "signCertRsa01.pem", destinationFolder + "padesEpesProfileTest01.pdf", sigPolicyIdentifier);

        basicCheckSignedDoc(destinationFolder + "padesEpesProfileTest01.pdf", "Signature1");
        Assert.assertNull(SignaturesCompareTool.compareSignatures(destinationFolder +
                "padesEpesProfileTest01.pdf", sourceFolder + "cmp_padesEpesProfileTest01.pdf"));
    }

    @Test
    public void signaturePolicyInfoUnavailableUrlTest()
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        String signedFileName = destinationFolder + "signaturePolicyInfoUnavailableUrl_signed.pdf";

        SignaturePolicyInfo spi = new SignaturePolicyInfo("1.2.3.4.5.6.7.8.9.10",
                "aVRleHQ0TGlmZVJhbmRvbVRleHQ=", "SHA-1",
                "https://signature-policy.org/not-available");

        signApproval(certsSrc + "signCertRsa01.pem", signedFileName, spi);

        basicCheckSignedDoc(signedFileName, "Signature1");
        Assert.assertNull(SignaturesCompareTool.compareSignatures(signedFileName,
                sourceFolder + "cmp_signaturePolicyInfoUnavailableUrl_signed.pdf"));
    }

    private void signApproval(String signCertFileName, String outFileName)
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(signCertFileName, outFileName, null, null);
    }

    private void signApproval(String signCertFileName, String outFileName, SignaturePolicyInfo signaturePolicyInfo)
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(signCertFileName, outFileName, null, signaturePolicyInfo);
    }

    private void signApproval(String signCertFileName, String outFileName, ISignaturePolicyIdentifier signaturePolicyId)
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        signApproval(signCertFileName, outFileName, signaturePolicyId, null);
    }

    private void signApproval(String signCertFileName, String outFileName,
            ISignaturePolicyIdentifier sigPolicyIdentifier,
            SignaturePolicyInfo sigPolicyInfo)
            throws IOException, GeneralSecurityException, AbstractPKCSException, AbstractOperatorCreationException {
        String srcFileName = sourceFolder + "helloWorldDoc.pdf";
        Certificate[] signChain = PemFileHelper.readFirstChain(signCertFileName);
        PrivateKey signPrivateKey = PemFileHelper.readFirstKey(signCertFileName, password);
        IExternalSignature pks =
                new PrivateKeySignature(signPrivateKey, DigestAlgorithms.SHA256, FACTORY.getProviderName());

        PdfSigner signer = new PdfSigner(new PdfReader(srcFileName), new FileOutputStream(outFileName), new StampingProperties());
        signer.setFieldName("Signature1");
        signer.getSignatureAppearance()
                .setPageRect(new Rectangle(50, 650, 200, 100))
                .setReason("Test")
                .setLocation("TestCity")
                .setLayer2Text("Approval test signature.\nCreated by iText7.");

        if (sigPolicyIdentifier != null) {
            signer.signDetached(new BouncyCastleDigest(), pks, signChain, null, null, null, 0,
                    PdfSigner.CryptoStandard.CADES, sigPolicyIdentifier);
        } else if (sigPolicyInfo != null) {
            signer.signDetached(new BouncyCastleDigest(), pks, signChain, null, null, null, 0,
                    PdfSigner.CryptoStandard.CADES, sigPolicyInfo);
        } else {
            signer.signDetached(new BouncyCastleDigest(), pks, signChain, null, null, null, 0,
                    PdfSigner.CryptoStandard.CADES);
        }
    }

    static void basicCheckSignedDoc(String filePath, String signatureName) throws GeneralSecurityException, IOException {
        PdfDocument outDocument = new PdfDocument(new PdfReader(filePath));

        SignatureUtil sigUtil = new SignatureUtil(outDocument);
        PdfPKCS7 signatureData = sigUtil.readSignatureData(signatureName);
        Assert.assertTrue(signatureData.verifySignatureIntegrityAndAuthenticity());

        outDocument.close();
    }
}
