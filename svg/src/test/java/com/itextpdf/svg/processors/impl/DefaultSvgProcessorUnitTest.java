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
package com.itextpdf.svg.processors.impl;

import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.styledxmlparser.logs.StyledXmlParserLogMessageConstant;
import com.itextpdf.styledxmlparser.jsoup.nodes.Attributes;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.parser.Tag;
import com.itextpdf.styledxmlparser.node.IElementNode;
import com.itextpdf.styledxmlparser.node.INode;
import com.itextpdf.styledxmlparser.node.impl.jsoup.node.JsoupElementNode;
import com.itextpdf.svg.SvgConstants;
import com.itextpdf.svg.dummy.processors.impl.DummySvgConverterProperties;
import com.itextpdf.svg.dummy.renderers.impl.DummyBranchSvgNodeRenderer;
import com.itextpdf.svg.dummy.renderers.impl.DummySvgNodeRenderer;
import com.itextpdf.svg.exceptions.SvgExceptionMessageConstant;
import com.itextpdf.svg.logs.SvgLogMessageConstant;
import com.itextpdf.svg.exceptions.SvgProcessingException;
import com.itextpdf.svg.processors.ISvgConverterProperties;
import com.itextpdf.svg.processors.ISvgProcessor;
import com.itextpdf.svg.renderers.IBranchSvgNodeRenderer;
import com.itextpdf.svg.renderers.ISvgNodeRenderer;
import com.itextpdf.svg.renderers.impl.CircleSvgNodeRenderer;
import com.itextpdf.svg.renderers.impl.PathSvgNodeRenderer;
import com.itextpdf.svg.renderers.impl.SvgTagSvgNodeRenderer;
import com.itextpdf.test.AssertUtil;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class DefaultSvgProcessorUnitTest extends ExtendedITextTest {

    //Main success scenario

    /**
     * Simple correct example
     */
    @Test
    public void dummyProcessingTestCorrectSimple() {
        //Setup nodes
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        Element jsoupSVGPath = new Element(Tag.valueOf("path"), "");
        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(new JsoupElementNode(jsoupSVGPath));
        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        ISvgConverterProperties props = new DummySvgConverterProperties();
        ISvgNodeRenderer rootActual = processor.process(root, props).getRootRenderer();
        //setup expected
        IBranchSvgNodeRenderer rootExpected = new DummyBranchSvgNodeRenderer("svg");
        rootExpected.addChild(new DummySvgNodeRenderer("circle"));
        rootExpected.addChild(new DummySvgNodeRenderer("path"));
        //Compare
        Assert.assertEquals(rootActual, rootExpected);
    }

    @Test()
    public void dummyProcessingTestCorrectNested() {
        //Setup nodes
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        Element jsoupSVGPath = new Element(Tag.valueOf("path"), "");
        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(new JsoupElementNode(jsoupSVGPath));
        INode nestedSvg = new JsoupElementNode(jsoupSVGRoot);
        nestedSvg.addChild(new JsoupElementNode(jsoupSVGCircle));
        nestedSvg.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(nestedSvg);

        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        ISvgConverterProperties props = new DummySvgConverterProperties();
        ISvgNodeRenderer rootActual = processor.process(root, props).getRootRenderer();
        //setup expected
        IBranchSvgNodeRenderer rootExpected = new DummyBranchSvgNodeRenderer("svg");
        rootExpected.addChild(new DummySvgNodeRenderer("circle"));
        rootExpected.addChild(new DummySvgNodeRenderer("path"));

        IBranchSvgNodeRenderer nestedSvgRend = new DummyBranchSvgNodeRenderer("svg");
        nestedSvgRend.addChild(new DummySvgNodeRenderer("circle"));
        nestedSvgRend.addChild(new DummySvgNodeRenderer("circle"));

        rootExpected.addChild(nestedSvgRend);
        //Compare
        Assert.assertEquals(rootActual, rootExpected);
    }

    //Edge cases
    @Test()
    /*
      Invalid input: null
     */
    @LogMessages(messages = {
            @LogMessage(messageTemplate = StyledXmlParserLogMessageConstant.ERROR_ADDING_CHILD_NODE),
    })
    public void dummyProcessingTestNodeHasNullChild() {
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(null);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        ISvgConverterProperties props = new DummySvgConverterProperties();
        ISvgNodeRenderer rootActual = processor.process(root, props).getRootRenderer();
        //setup expected
        ISvgNodeRenderer rootExpected = new DummySvgNodeRenderer("svg");
        Assert.assertEquals(rootExpected, rootActual);
    }

    @Test
    public void dummyProcessingSvgTagIsNotRootOfInput() {
        Element jsoupRandomElement = new Element(Tag.valueOf("body"), "");
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        INode root = new JsoupElementNode(jsoupRandomElement);
        INode svg = new JsoupElementNode(jsoupSVGRoot);
        svg.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(svg);
        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        ISvgConverterProperties props = new DummySvgConverterProperties();
        ISvgNodeRenderer rootActual = processor.process(root, props).getRootRenderer();
        //setup expected
        IBranchSvgNodeRenderer rootExpected = new DummyBranchSvgNodeRenderer("svg");
        rootExpected.addChild(new DummySvgNodeRenderer("circle"));
        Assert.assertEquals(rootActual, rootExpected);
    }

    @Test
    public void dummyProcessingNoSvgTagInInput() {
        Element jsoupSVGRoot = new Element(Tag.valueOf("polygon"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        ISvgConverterProperties props = new DummySvgConverterProperties();

        Exception e = Assert.assertThrows(SvgProcessingException.class,
                () -> processor.process(root, props).getRootRenderer()
        );
        Assert.assertEquals(SvgExceptionMessageConstant.NO_ROOT, e.getMessage());
    }

    @Test
    public void dummyProcessingTestNullInput() {
        DefaultSvgProcessor processor = new DefaultSvgProcessor();

        Assert.assertThrows(SvgProcessingException.class, () -> processor.process(null, null));
    }

    @Test
    public void processWithNullPropertiesTest() {
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        INode root = new JsoupElementNode(jsoupSVGRoot);

        DefaultSvgProcessor processor = new DefaultSvgProcessor();

        SvgConverterProperties convProps = new SvgConverterProperties();
        convProps.setRendererFactory(null);
        convProps.setCharset(null);

        ISvgNodeRenderer rootRenderer = processor.process(root, convProps).getRootRenderer();

        Assert.assertTrue(rootRenderer instanceof SvgTagSvgNodeRenderer);
        Assert.assertEquals(0, ((SvgTagSvgNodeRenderer) rootRenderer).getChildren().size());
    }

    @Test
    public void defaultProcessingCorrectlyNestedRenderersTest() {
        //Setup nodes
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Element jsoupSVGCircle = new Element(Tag.valueOf("circle"), "");
        Element jsoupSVGPath = new Element(Tag.valueOf("path"), "");

        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGCircle));
        root.addChild(new JsoupElementNode(jsoupSVGPath));
        //Run
        DefaultSvgProcessor processor = new DefaultSvgProcessor();

        SvgConverterProperties convProps = new SvgConverterProperties();

        ISvgNodeRenderer rootRenderer = processor.process(root, convProps).getRootRenderer();

        Assert.assertTrue(rootRenderer instanceof SvgTagSvgNodeRenderer);

        List<ISvgNodeRenderer> children = ((SvgTagSvgNodeRenderer) rootRenderer).getChildren();

        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.get(0) instanceof CircleSvgNodeRenderer);
        Assert.assertTrue(children.get(1) instanceof PathSvgNodeRenderer);
    }

    @Test
    public void findFirstElementNullTest() {
        DefaultSvgProcessor processor = new DefaultSvgProcessor();
        IElementNode actual = processor.findFirstElement(null, "name");
        Assert.assertNull(actual);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = SvgLogMessageConstant.UNMAPPED_TAG),
    })
    public void depthFirstNullRendererTest() {
        Element jsoupNonExistingElement = new Element(Tag.valueOf("nonExisting"), "");
        INode root = new JsoupElementNode(jsoupNonExistingElement);
        DefaultSvgProcessor dsp = new DefaultSvgProcessor();
        ISvgConverterProperties scp = new SvgConverterProperties();
        dsp.performSetup(root, scp);
        // below method must not throw a NullPointerException
        AssertUtil.doesNotThrow(() -> dsp.executeDepthFirstTraversal(root));
    }

    @Test
    public void xLinkAttributeBaseDirDoesNotExistTest() {
        INode root = createSvgContainingImage();
        String resolvedBaseUrl = "/i7j/itextcore";
        String baseUrl = resolvedBaseUrl + "/wrongDirName";
        ISvgConverterProperties props = new SvgConverterProperties().setBaseUri(baseUrl);
        SvgTagSvgNodeRenderer rootActual = (SvgTagSvgNodeRenderer) processor().process(root, props).getRootRenderer();

        String fileName = resolvedBaseUrl + "/img.png";
        final String expectedURL = UrlUtil.toNormalizedURI(fileName).toString();
        final String expectedURLAnotherValidVersion = createAnotherValidUrlVersion(expectedURL);

        ISvgNodeRenderer imageRendered = rootActual.getChildren().get(0);
        String url = imageRendered.getAttribute(SvgConstants.Attributes.XLINK_HREF);

        // Both variants(namely with triple and single slashes) are valid.
        Assert.assertTrue(expectedURL.equals(url) || expectedURLAnotherValidVersion.equals(url));
    }

    @Test
    public void xLinkAttributeResolveNonEmptyBaseUrlTest() {
        INode root = createSvgContainingImage();
        String baseUrl = "./src/test/resources/com/itextpdf/svg/processors/impl/DefaultSvgProcessorIntegrationTest";
        ISvgConverterProperties props = new SvgConverterProperties().setBaseUri(baseUrl);
        SvgTagSvgNodeRenderer rootActual = (SvgTagSvgNodeRenderer) processor().process(root, props).getRootRenderer();

        String fileName = baseUrl + "/img.png";
        final String expectedURL = UrlUtil.toNormalizedURI(fileName).toString();
        final String expectedURLAnotherValidVersion = createAnotherValidUrlVersion(expectedURL);

        ISvgNodeRenderer imageRendered = rootActual.getChildren().get(0);
        String url = imageRendered.getAttribute(SvgConstants.Attributes.XLINK_HREF);

        // Both variants(namely with triple and single slashes) are valid.
        Assert.assertTrue(expectedURL.equals(url) || expectedURLAnotherValidVersion.equals(url));
    }

    private INode createSvgContainingImage() {
        Element jsoupSVGRoot = new Element(Tag.valueOf("svg"), "");
        Attributes attr = new Attributes();
        attr.put(SvgConstants.Attributes.XLINK_HREF, "img.png");
        Element jsoupSVGImage = new Element(Tag.valueOf("image"), "", attr);
        INode root = new JsoupElementNode(jsoupSVGRoot);
        root.addChild(new JsoupElementNode(jsoupSVGImage));
        return root;
    }

    private static String createAnotherValidUrlVersion(String url) {
        if (url.startsWith("file:///")) {
            return "file:/" + url.substring("file:///".length());
        } else if (url.startsWith("file:/")) {
            return "file:///" + url.substring("file:/".length());
        } else {
            return url;
        }
    }

    private static ISvgProcessor processor() {
        return new DefaultSvgProcessor();
    }
}
