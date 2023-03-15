/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

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
package com.itextpdf.forms.form.renderer;

import com.itextpdf.forms.logs.FormsLogMessageConstants;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.TextFormFieldBuilder;
import com.itextpdf.forms.form.FormProperty;
import com.itextpdf.forms.form.element.InputField;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.minmaxwidth.MinMaxWidth;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.LineRenderer;
import com.itextpdf.layout.renderer.ParagraphRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * The {@link AbstractOneLineTextFieldRenderer} implementation for input fields.
 */
public class InputFieldRenderer extends AbstractOneLineTextFieldRenderer {

    /**
     * Creates a new {@link InputFieldRenderer} instance.
     *
     * @param modelElement the model element
     */
    public InputFieldRenderer(InputField modelElement) {
        super(modelElement);
    }

    /* (non-Javadoc)
     * @see com.itextpdf.layout.renderer.IRenderer#getNextRenderer()
     */
    @Override
    public IRenderer getNextRenderer() {
        return new InputFieldRenderer((InputField) modelElement);
    }

    /**
     * Gets the size of the input field.
     *
     * @return the input field size
     */
    public int getSize() {
        Integer size = this.getPropertyAsInteger(FormProperty.FORM_FIELD_SIZE);
        return size == null ? (int) modelElement.<Integer>getDefaultProperty(FormProperty.FORM_FIELD_SIZE) : (int) size;
    }

    /**
     * Checks if the input field is a password field.
     *
     * @return true, if the input field is a password field
     */
    public boolean isPassword() {
        Boolean password = getPropertyAsBoolean(FormProperty.FORM_FIELD_PASSWORD_FLAG);
        return password == null ? (boolean) modelElement.
                <Boolean>getDefaultProperty(FormProperty.FORM_FIELD_PASSWORD_FLAG) : (boolean) password;
    }

    @Override
    IRenderer createParagraphRenderer(String defaultValue) {
        if (defaultValue.isEmpty() && null != ((InputField) modelElement).getPlaceholder()
                && !((InputField) modelElement).getPlaceholder().isEmpty()) {
            return ((InputField) modelElement).getPlaceholder().createRendererSubTree();
        }
        return super.createParagraphRenderer(defaultValue);
    }

    /* (non-Javadoc)
     * @see com.itextpdf.html2pdf.attach.impl.layout.form.renderer.AbstractFormFieldRenderer#adjustFieldLayout()
     */
    @Override
    protected void adjustFieldLayout(LayoutContext layoutContext) {
        List<LineRenderer> flatLines = ((ParagraphRenderer) flatRenderer).getLines();
        Rectangle flatBBox = flatRenderer.getOccupiedArea().getBBox();
        updatePdfFont((ParagraphRenderer) flatRenderer);
        if (flatLines.isEmpty() || font == null) {
            LoggerFactory.getLogger(getClass()).error(
                    MessageFormatUtil.format(
                            FormsLogMessageConstants.ERROR_WHILE_LAYOUT_OF_FORM_FIELD_WITH_TYPE,
                            "text input"));
            setProperty(FormProperty.FORM_FIELD_FLATTEN, true);
            flatBBox.setY(flatBBox.getTop()).setHeight(0);
        } else {
            cropContentLines(flatLines, flatBBox);
        }
        flatBBox.setWidth((float) retrieveWidth(layoutContext.getArea().getBBox().getWidth()));
    }

    /* (non-Javadoc)
     * @see AbstractFormFieldRenderer#createFlatRenderer()
     */
    @Override
    protected IRenderer createFlatRenderer() {
        String defaultValue = getDefaultValue();
        boolean flatten = isFlatten();
        boolean password = isPassword();
        if (flatten && password) {
            defaultValue = obfuscatePassword(defaultValue);
        }
        return createParagraphRenderer(defaultValue);
    }

    /* (non-Javadoc)
     * @see AbstractFormFieldRenderer#applyAcroField(com.itextpdf.layout.renderer.DrawContext)
     */
    @Override
    protected void applyAcroField(DrawContext drawContext) {
        font.setSubset(false);
        boolean password = isPassword();
        final String value = password ? "" : getDefaultValue();
        String name = getModelId();
        UnitValue fontSize = (UnitValue) this.getPropertyAsUnitValue(Property.FONT_SIZE);
        if (!fontSize.isPointValue()) {
            Logger logger = LoggerFactory.getLogger(InputFieldRenderer.class);
            logger.error(MessageFormatUtil.format(IoLogMessageConstant.PROPERTY_IN_PERCENTS_NOT_SUPPORTED,
                    Property.FONT_SIZE));
        }
        final PdfDocument doc = drawContext.getDocument();
        final Rectangle area = flatRenderer.getOccupiedArea().getBBox().clone();
        final PdfPage page = doc.getPage(occupiedArea.getPageNumber());
        final float fontSizeValue = fontSize.getValue();

        final PdfFormField inputField = new TextFormFieldBuilder(doc, name).setWidgetRectangle(area).createText()
                .setValue(value);
        inputField.setFont(font).setFontSize(fontSizeValue);
        if (password) {
            inputField.setFieldFlag(PdfFormField.FF_PASSWORD, true);
        } else {
            inputField.setDefaultValue(new PdfString(value));
        }
        applyDefaultFieldProperties(inputField);
        PdfAcroForm.getAcroForm(doc, true).addField(inputField, page);

        writeAcroFormFieldLangAttribute(doc);
    }

    @Override
    public <T1> T1 getProperty(int key) {
        if (key == Property.WIDTH) {
            T1 width = super.<T1>getProperty(Property.WIDTH);
            if (width == null) {
                UnitValue fontSize = (UnitValue) this.getPropertyAsUnitValue(Property.FONT_SIZE);
                if (!fontSize.isPointValue()) {
                    Logger logger = LoggerFactory.getLogger(InputFieldRenderer.class);
                    logger.error(MessageFormatUtil.format(IoLogMessageConstant.PROPERTY_IN_PERCENTS_NOT_SUPPORTED,
                            Property.FONT_SIZE));
                }
                int size = getSize();
                return (T1) (Object) UnitValue.createPointValue(
                        updateHtmlColsSizeBasedWidth(fontSize.getValue() * (size * 0.5f + 2) + 2));
            }
            return width;
        }
        return super.<T1>getProperty(key);
    }

    @Override
    protected boolean setMinMaxWidthBasedOnFixedWidth(MinMaxWidth minMaxWidth) {
        boolean result = false;
        if (hasRelativeUnitValue(Property.WIDTH)) {
            UnitValue widthUV = this.<UnitValue>getProperty(Property.WIDTH);
            boolean restoreWidth = hasOwnProperty(Property.WIDTH);
            setProperty(Property.WIDTH, null);
            Float width = retrieveWidth(0);
            if (width != null) {
                // the field can be shrinked if necessary so only max width is set here
                minMaxWidth.setChildrenMaxWidth((float) width);
                result = true;
            }
            if (restoreWidth) {
                setProperty(Property.WIDTH, widthUV);
            } else {
                deleteOwnProperty(Property.WIDTH);
            }
        } else {
            result = super.setMinMaxWidthBasedOnFixedWidth(minMaxWidth);
        }
        return result;
    }

    /**
     * Obfuscates the content of a password input field.
     *
     * @param text the password
     * @return a string consisting of '*' characters.
     */
    private String obfuscatePassword(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            builder.append('*');
        }
        return builder.toString();
    }
}
