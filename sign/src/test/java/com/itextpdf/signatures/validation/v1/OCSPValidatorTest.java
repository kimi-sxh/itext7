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
package com.itextpdf.signatures.validation.v1;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.cert.IX509CertificateHolder;
import com.itextpdf.commons.bouncycastle.cert.ocsp.IBasicOCSPResp;
import com.itextpdf.commons.bouncycastle.operator.AbstractOperatorCreationException;
import com.itextpdf.commons.bouncycastle.pkcs.AbstractPKCSException;
import com.itextpdf.commons.utils.DateTimeUtil;
import com.itextpdf.signatures.IssuingCertificateRetriever;
import com.itextpdf.signatures.TimestampConstants;
import com.itextpdf.signatures.logs.SignLogMessageConstant;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.TimeTestUtil;
import com.itextpdf.signatures.testutils.builder.TestOcspResponseBuilder;
import com.itextpdf.signatures.testutils.client.TestOcspClient;
import com.itextpdf.signatures.validation.v1.context.CertificateSource;
import com.itextpdf.signatures.validation.v1.context.CertificateSources;
import com.itextpdf.signatures.validation.v1.context.TimeBasedContext;
import com.itextpdf.signatures.validation.v1.context.TimeBasedContexts;
import com.itextpdf.signatures.validation.v1.context.ValidationContext;
import com.itextpdf.signatures.validation.v1.context.ValidatorContext;
import com.itextpdf.signatures.validation.v1.context.ValidatorContexts;
import com.itextpdf.signatures.validation.v1.report.ReportItem;
import com.itextpdf.signatures.validation.v1.report.ValidationReport;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.BouncyCastleUnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;

@Category(BouncyCastleUnitTest.class)
public class OCSPValidatorTest extends ExtendedITextTest {
    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/signatures/validation/v1/OCSPValidatorTest/";
    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();
    private static final char[] PASSWORD = "testpassphrase".toCharArray();

    private static X509Certificate caCert;
    private static PrivateKey caPrivateKey;
    private static X509Certificate checkCert;
    private static X509Certificate responderCert;
    private static PrivateKey ocspRespPrivateKey;
    private final ValidationContext baseContext = new ValidationContext(ValidatorContext.REVOCATION_DATA_VALIDATOR,
            CertificateSource.SIGNER_CERT, TimeBasedContext.PRESENT);
    private IssuingCertificateRetriever certificateRetriever;
    private SignatureValidationProperties parameters;
    private ValidatorChainBuilder validatorChainBuilder;
    private MockChainValidator mockCertificateChainValidator;

    @BeforeClass
    public static void before()
            throws CertificateException, IOException, AbstractOperatorCreationException, AbstractPKCSException {
        Security.addProvider(FACTORY.getProvider());

        String rootCertFileName = SOURCE_FOLDER + "rootCert.pem";
        String checkCertFileName = SOURCE_FOLDER + "signCert.pem";
        String ocspResponderCertFileName = SOURCE_FOLDER + "ocspResponderCert.pem";

        caCert = (X509Certificate) PemFileHelper.readFirstChain(rootCertFileName)[0];
        caPrivateKey = PemFileHelper.readFirstKey(rootCertFileName, PASSWORD);
        checkCert = (X509Certificate) PemFileHelper.readFirstChain(checkCertFileName)[0];
        responderCert = (X509Certificate) PemFileHelper.readFirstChain(ocspResponderCertFileName)[0];
        ocspRespPrivateKey = PemFileHelper.readFirstKey(ocspResponderCertFileName, PASSWORD);
    }

    @Before
    public void setUp() {
        certificateRetriever = new IssuingCertificateRetriever();
        parameters = new SignatureValidationProperties();
        mockCertificateChainValidator = new MockChainValidator();
        validatorChainBuilder = new ValidatorChainBuilder()
                .withSignatureValidationProperties(parameters)
                .withIssuingCertificateRetriever(certificateRetriever)
                .withCertificateChainValidator(mockCertificateChainValidator);
    }

    @Test
    public void happyPathTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;
        ValidationReport report = validateTest(checkDate);

        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.VALID));
    }

    @Test
    public void ocpsIssuerChainValidationsUsesCorrectParametersTest() throws CertificateException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;
        ValidationReport report = validateTest(checkDate);

        Assert.assertEquals(1, mockCertificateChainValidator.verificationCalls.size());
        Assert.assertEquals(responderCert, mockCertificateChainValidator.verificationCalls.get(0).certificate);
        Assert.assertEquals(ValidatorContext.OCSP_VALIDATOR, mockCertificateChainValidator.verificationCalls.get(0).context.getValidatorContext());
        Assert.assertEquals(CertificateSource.OCSP_ISSUER, mockCertificateChainValidator.verificationCalls.get(0).context.getCertificateSource());
        Assert.assertEquals(checkDate, mockCertificateChainValidator.verificationCalls.get(0).checkDate);
        Assert.assertEquals(DateTimeUtil.addDaysToDate(checkDate, 0), mockCertificateChainValidator.verificationCalls.get(0).checkDate);
    }

    @Test
    public void ocspForSelfSignedCertShouldNotValdateFurtherTest() throws GeneralSecurityException, IOException {
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp caBasicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(caCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, caCert, caBasicOCSPResp.getResponses()[0], caBasicOCSPResp,
                TimeTestUtil.TEST_DATE_TIME);
        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.VALID)
                .hasNumberOfLogs(1)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(RevocationDataValidator.SELF_SIGNED_CERTIFICATE)
                        .withCertificate(caCert))
        );
        Assert.assertEquals(0, mockCertificateChainValidator.verificationCalls.size());
    }

    @Test
    public void validationDateAfterNextUpdateTest() throws GeneralSecurityException, IOException {
        // Same next update is set in the test OCSP builder.
        Date nextUpdate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, 30);
        Date checkDate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, 45);
        ValidationReport report = validateTest(checkDate, TimeTestUtil.TEST_DATE_TIME, 50);

        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.OCSP_IS_NO_LONGER_VALID, l -> checkDate, l -> nextUpdate)));
    }


    @Test
    public void serialNumbersDoNotMatchTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;

        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setThisUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, 1)));
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp caBasicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(caCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.setTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();

        validator.validate(report, baseContext, checkCert, caBasicOCSPResp.getResponses()[0], caBasicOCSPResp,
                checkDate);

        AssertValidationReport.assertThat(report, a -> a
                .hasNumberOfLogs(1)
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.SERIAL_NUMBERS_DO_NOT_MATCH)
                        .withCertificate(checkCert))
        );
        Assert.assertEquals(0, mockCertificateChainValidator.verificationCalls.size());
    }


    @Test
    public void issuersDoNotMatchTest() throws GeneralSecurityException, IOException {
        String wrongRootCertFileName = SOURCE_FOLDER + "rootCertForOcspTest.pem";

        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        validatorChainBuilder.withIssuingCertificateRetriever(
                new TestIssuingCertificateRetriever(wrongRootCertFileName));
        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();

        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp,
                TimeTestUtil.TEST_DATE_TIME);

        AssertValidationReport.assertThat(report, a -> a
                .hasNumberOfFailures(1)
                .hasNumberOfLogs(1)
                .hasLogItem(la -> la
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.ISSUERS_DO_NOT_MATCH)
                        .withStatus(ReportItem.ReportItemStatus.INDETERMINATE)
                ));
    }

    @Test
    public void positiveFreshnessNegativeTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;
        Date thisUpdate = DateTimeUtil.addDaysToDate(checkDate, -3);
        ValidationReport report = validateTest(checkDate, thisUpdate, 2);
        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE)
                .hasNumberOfFailures(1)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.FRESHNESS_CHECK,
                                l -> thisUpdate, l -> checkDate, l -> Duration.ofDays(2))
                )
        );
    }

    @Test
    public void nextUpdateNotSetResultsInValidStatusTest() throws CertificateEncodingException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;

        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(caCert, caPrivateKey);
        builder.setThisUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, -20)));
        builder.setNextUpdate(DateTimeUtil.getCalendar((Date) TimestampConstants.UNDEFINED_TIMESTAMP_DATE));
        builder.setProducedAt(DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -20));
        TestOcspClient client = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(client.getEncoded(checkCert, caCert, ""))));

        certificateRetriever.addKnownCertificates(Collections.singleton(caCert));
        ValidationReport report = new ValidationReport();
        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();

        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp, checkDate);

        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.VALID));
    }

    @Test
    public void certificateWasRevokedBeforeCheckDateShouldFailTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;
        Date revocationDate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -1);

        ValidationReport report = validateRevokedTestMocked(checkDate, revocationDate);

        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.INVALID)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.CERT_IS_REVOKED)
                        .withCertificate(checkCert)));
    }

    @Test
    public void certificateWasRevokedAfterCheckDateShouldSucceedTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;
        Date revocationDate = DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, 10);

        ValidationReport report = validateRevokedTestMocked(checkDate, revocationDate);
        AssertValidationReport.assertThat(report, a -> a
                .hasLogItem(la -> la
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(SignLogMessageConstant.VALID_CERTIFICATE_IS_REVOKED,
                                l -> revocationDate)
                )
                .hasStatus(ValidationReport.ValidationResult.VALID));
    }

    @Test
    public void certificateStatusIsUnknownTest() throws GeneralSecurityException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;

        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setCertificateStatus(FACTORY.createUnknownStatus());
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp, checkDate);
        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE)
                .hasLogItem(al -> al
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.CERT_STATUS_IS_UNKNOWN)
                        .withCertificate(checkCert)));

        Assert.assertEquals(0, mockCertificateChainValidator.verificationCalls.size());
    }

    @Test
    public void ocspIssuerCertificateDoesNotVerifyWithCaPKTest()
            throws CertificateException, IOException, AbstractOperatorCreationException, AbstractPKCSException {
        String ocspResponderCertFileName = SOURCE_FOLDER + "ocspResponderCertForOcspTest.pem";
        X509Certificate responderCert = (X509Certificate) PemFileHelper.readFirstChain(ocspResponderCertFileName)[0];
        PrivateKey ocspRespPrivateKey = PemFileHelper.readFirstKey(ocspResponderCertFileName, PASSWORD);
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);

        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp,
                TimeTestUtil.TEST_DATE_TIME);
        AssertValidationReport.assertThat(report, a -> a
                .hasNumberOfFailures(1)
                .hasStatus(ValidationReport.ValidationResult.INVALID)
                .hasLogItem(al ->
                        al.withCheckName(OCSPValidator.OCSP_CHECK)
                                .withMessage(OCSPValidator.INVALID_OCSP)
                                // This should be the checked certificate, not the ocsp responder
                                //.withCertificate(checkCert)
                                .withCertificate(responderCert)
                )
        );
    }

    @Test
    public void noResponderFoundInCertsTest() throws GeneralSecurityException, IOException {
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setOcspCertsChain(new IX509CertificateHolder[]{FACTORY.createJcaX509CertificateHolder(caCert)});
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp,
                TimeTestUtil.TEST_DATE_TIME);

        AssertValidationReport.assertThat(report, a -> a
                .hasLogItem(la -> la
                        .withCheckName(OCSPValidator.OCSP_CHECK)
                        .withMessage(OCSPValidator.OCSP_COULD_NOT_BE_VERIFIED)
                )
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE));
    }

    @Test
    public void chainValidatorReportWrappingTest() throws CertificateException, IOException {
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;

        mockCertificateChainValidator.onCallDo(c -> {
                    c.report.addReportItem(
                            new ReportItem("test1", "test1", ReportItem.ReportItemStatus.INFO));
                    c.report.addReportItem(
                            new ReportItem("test2", "test2", ReportItem.ReportItemStatus.INDETERMINATE));
                    c.report.addReportItem(
                            new ReportItem("test3", "test3", ReportItem.ReportItemStatus.INVALID));
                }
        );
        ValidationReport report = validateTest(checkDate);

        AssertValidationReport.assertThat(report, a -> a
                .hasStatus(ValidationReport.ValidationResult.INDETERMINATE)
                .hasLogItems(0, 0, la -> la.withStatus(ReportItem.ReportItemStatus.INVALID))
                .hasLogItems(2, 2, la -> la.withStatus(ReportItem.ReportItemStatus.INDETERMINATE))
                .hasLogItem(la -> la.withStatus(ReportItem.ReportItemStatus.INFO)));
    }

    private ValidationReport validateTest(Date checkDate) throws CertificateException, IOException {
        return validateTest(checkDate, DateTimeUtil.addDaysToDate(checkDate, 1), 0);
    }

    private ValidationReport validateTest(Date checkDate, Date thisUpdate, long freshness)
            throws CertificateException, IOException {
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setThisUpdate(DateTimeUtil.getCalendar(thisUpdate));
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        parameters.setFreshness(ValidatorContexts.all(), CertificateSources.all(), TimeBasedContexts.all(),
                Duration.ofDays(freshness));
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp, checkDate);
        return report;
    }

    private ValidationReport validateRevokedTestMocked(Date checkDate, Date revocationDate)
            throws IOException, CertificateException {
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setCertificateStatus(FACTORY.createRevokedStatus(revocationDate,
                FACTORY.createCRLReason().getKeyCompromise()));
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp, checkDate);
        return report;
    }

    private ValidationReport validateOcspWithoutCertsTestMocked(boolean addResponderToTrusted)
            throws IOException, CertificateException {
        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setOcspCertsChain(new IX509CertificateHolder[0]);
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));
        if (addResponderToTrusted) {
            certificateRetriever.addTrustedCertificates(Collections.singletonList(responderCert));
        }

        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp,
                TimeTestUtil.TEST_DATE_TIME);
        return report;
    }

    private ValidationReport verifyResponderWithOcspMocked(boolean revokedOcsp)
            throws IOException, CertificateException, AbstractOperatorCreationException, AbstractPKCSException {
        String rootCertFileName = SOURCE_FOLDER + "rootCertForOcspTest.pem";
        String checkCertFileName = SOURCE_FOLDER + "signCertForOcspTest.pem";
        String ocspResponderCertFileName = SOURCE_FOLDER + "ocspResponderCertForOcspTest.pem";
        Date checkDate = TimeTestUtil.TEST_DATE_TIME;

        X509Certificate caCert = (X509Certificate) PemFileHelper.readFirstChain(rootCertFileName)[0];
        PrivateKey caPrivateKey = PemFileHelper.readFirstKey(rootCertFileName, PASSWORD);
        X509Certificate checkCert = (X509Certificate) PemFileHelper.readFirstChain(checkCertFileName)[0];
        X509Certificate responderCert = (X509Certificate) PemFileHelper.readFirstChain(ocspResponderCertFileName)[0];
        PrivateKey ocspRespPrivateKey = PemFileHelper.readFirstKey(ocspResponderCertFileName, PASSWORD);

        TestOcspResponseBuilder builder = new TestOcspResponseBuilder(responderCert, ocspRespPrivateKey);
        builder.setThisUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, -5)));
        builder.setNextUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, 5)));
        TestOcspClient ocspClient = new TestOcspClient().addBuilderForCertIssuer(caCert, builder);
        IBasicOCSPResp basicOCSPResp = FACTORY.createBasicOCSPResp(FACTORY.createBasicOCSPResponse(
                FACTORY.createASN1Primitive(ocspClient.getEncoded(checkCert, caCert, null))));

        ValidationReport report = new ValidationReport();
        certificateRetriever.addTrustedCertificates(Collections.singletonList(caCert));

        TestOcspResponseBuilder builder2 = revokedOcsp ? new TestOcspResponseBuilder(caCert, caPrivateKey,
                FACTORY.createRevokedStatus(
                        DateTimeUtil.addDaysToDate(TimeTestUtil.TEST_DATE_TIME, -5),
                        FACTORY.createCRLReason().getKeyCompromise())) :
                new TestOcspResponseBuilder(caCert, caPrivateKey);
        builder2.setThisUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, 20)));
        builder2.setNextUpdate(DateTimeUtil.getCalendar(DateTimeUtil.addDaysToDate(checkDate, 30)));
        TestOcspClient ocspClient2 = new TestOcspClient().addBuilderForCertIssuer(caCert, builder2);

        parameters.setRevocationOnlineFetching(ValidatorContexts.all(), CertificateSources.all(),
                        TimeBasedContexts.all(), SignatureValidationProperties.OnlineFetching.NEVER_FETCH)
                .setFreshness(ValidatorContexts.all(), CertificateSources.all(), TimeBasedContexts.all(),
                        Duration.ofDays(5));
        if (revokedOcsp) {
            parameters.setContinueAfterFailure(ValidatorContexts.all(), CertificateSources.all(), false);
        }
        OCSPValidator validator = validatorChainBuilder.buildOCSPValidator();
        validator.validate(report, baseContext, checkCert, basicOCSPResp.getResponses()[0], basicOCSPResp, checkDate);
        return report;
    }

    private static class TestIssuingCertificateRetriever extends IssuingCertificateRetriever {
        Certificate issuerCertificate;

        public TestIssuingCertificateRetriever(String issuerPath) throws CertificateException, IOException {
            super();
            this.issuerCertificate = PemFileHelper.readFirstChain(issuerPath)[0];
        }

        @Override
        public Certificate retrieveIssuerCertificate(Certificate certificate) {
            return issuerCertificate;
        }
    }
}
