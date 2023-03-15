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

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.exceptions.FormsExceptionMessageConstant;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormAnnotation;
import com.itextpdf.forms.fields.RadioFormFieldBuilder;
import com.itextpdf.forms.util.DrawingUtil;
import com.itextpdf.forms.form.FormProperty;
import com.itextpdf.forms.form.element.Radio;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.properties.Background;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.BoxSizingPropertyValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.ParagraphRenderer;

/**
 * The {@link AbstractOneLineTextFieldRenderer} implementation for radio buttons.
 */
public class RadioRenderer extends AbstractFormFieldRenderer {

    private static final Color DEFAULT_CHECKED_COLOR = ColorConstants.BLACK;
    private static final float DEFAULT_SIZE = 8.25f; // 11px
    private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;
    private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.MIDDLE;

    /**
     * Creates a new {@link RadioRenderer} instance.
     *
     * @param modelElement the model element
     */
    public RadioRenderer(Radio modelElement) {
        super(modelElement);
        setProperty(Property.VERTICAL_ALIGNMENT, VerticalAlignment.MIDDLE);
    }

    /* (non-Javadoc)
     * @see com.itextpdf.layout.renderer.IRenderer#getNextRenderer()
     */
    @Override
    public IRenderer getNextRenderer() {
        return new RadioRenderer((Radio) modelElement);
    }

    /**
     * {@inheritDoc}
     *
     * @param drawContext {@inheritDoc}
     */
    @Override
    public void drawBorder(DrawContext drawContext) {
        // Do not draw borders here, they will be drawn in flat renderer
    }

    /**
     * {@inheritDoc}
     *
     * @param drawContext {@inheritDoc}
     */
    @Override
    public void drawBackground(DrawContext drawContext) {
        // Do not draw a background here, it will be drawn in flat renderer
    }

    /**
     * {@inheritDoc}
     *
     * @param rect {@inheritDoc}
     * @param borders {@inheritDoc}
     * @param reverse {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Rectangle applyBorderBox(Rectangle rect, Border[] borders, boolean reverse) {
        // Do not apply borders here, they will be applied in flat renderer
        return rect;
    }

    @Override
    protected IRenderer createFlatRenderer() {
        UnitValue heightUV = getPropertyAsUnitValue(Property.HEIGHT);
        UnitValue widthUV = getPropertyAsUnitValue(Property.WIDTH);
        final float height = null == heightUV ? DEFAULT_SIZE : heightUV.getValue();
        final float width = null == widthUV ? DEFAULT_SIZE : widthUV.getValue();
        final float size = Math.min(height, width);

        // Set size to current renderer
        setProperty(Property.HEIGHT, UnitValue.createPointValue(height));
        setProperty(Property.WIDTH, UnitValue.createPointValue(width));

        Paragraph paragraph = new Paragraph()
                .setWidth(size)
                .setHeight(size)
                .setHorizontalAlignment(DEFAULT_HORIZONTAL_ALIGNMENT)
                .setVerticalAlignment(DEFAULT_VERTICAL_ALIGNMENT)
                .setMargin(0);
        paragraph.setProperty(Property.BOX_SIZING, this.<BoxSizingPropertyValue>getProperty(Property.BOX_SIZING));
        paragraph.setBorder(this.<Border>getProperty(Property.BORDER));
        paragraph.setProperty(Property.BACKGROUND, this.<Background>getProperty(Property.BACKGROUND));
        paragraph.setBorderRadius(new BorderRadius(UnitValue.createPercentValue(50)));

        return new FlatParagraphRenderer(paragraph);
    }

    /* (non-Javadoc)
     * @see AbstractFormFieldRenderer#adjustFieldLayout()
     */
    @Override
    protected void adjustFieldLayout(LayoutContext layoutContext) {
    }

    /**
     * Defines whether the radio is checked or not.
     *
     * @return the default value of the radio field
     */
    public boolean isBoxChecked() {
        return Boolean.TRUE.equals(this.<Boolean>getProperty(FormProperty.FORM_FIELD_CHECKED));
    }

    /* (non-Javadoc)
     * @see AbstractFormFieldRenderer#applyAcroField(com.itextpdf.layout.renderer.DrawContext)
     */
    @Override
    protected void applyAcroField(DrawContext drawContext) {
        PdfDocument doc = drawContext.getDocument();
        PdfAcroForm form = PdfAcroForm.getAcroForm(doc, true);
        Rectangle area = flatRenderer.getOccupiedArea().getBBox().clone();

        PdfPage page = doc.getPage(occupiedArea.getPageNumber());
        String groupName = this.<String>getProperty(FormProperty.FORM_FIELD_RADIO_GROUP_NAME);
        if (groupName == null || groupName.isEmpty()) {
            throw new PdfException(FormsExceptionMessageConstant.EMPTY_RADIO_GROUP_NAME);
        }

        PdfButtonFormField radioGroup = (PdfButtonFormField) form.getField(groupName);
        boolean addNew = false;
        if (null == radioGroup) {
            radioGroup = new RadioFormFieldBuilder(doc, groupName).createRadioGroup();
            radioGroup.setValue(PdfFormAnnotation.OFF_STATE_VALUE);
            addNew = true;
        }
        if (isBoxChecked()) {
            radioGroup.setValue(getModelId());
        }

        PdfFormAnnotation radio =
                new RadioFormFieldBuilder(doc, null).createRadioButton(getModelId(), area);
        // Set background color
        Background background = this.<Background>getProperty(Property.BACKGROUND);
        if (background != null) {
            radio.setBackgroundColor(background.getColor());
        }

        // Set border color and border width
        Border border = this.<Border>getProperty(Property.BORDER);
        if (border != null) {
            radio.setBorderColor(border.getColor());
            radio.setBorderWidth(border.getWidth());
        }

        radioGroup.addKid(radio);

        if (addNew) {
            form.addField(radioGroup, page);
        } else {
            form.replaceField(groupName, radioGroup);
        }

        writeAcroFormFieldLangAttribute(doc);
    }

    @Override
    protected boolean isLayoutBasedOnFlatRenderer() {
        return false;
    }

    private boolean isDrawCircledBorder() {
        return Boolean.TRUE.equals(this.<Boolean>getProperty(FormProperty.FORM_FIELD_RADIO_BORDER_CIRCLE));
    }

    private class FlatParagraphRenderer extends ParagraphRenderer {

        public FlatParagraphRenderer(Paragraph modelElement) {
            super(modelElement);
        }

        @Override
        public void drawChildren(DrawContext drawContext) {
            if (!isBoxChecked()) {
                // Nothing to draw
                return;
            }

            PdfCanvas canvas = drawContext.getCanvas();
            Rectangle rectangle = getOccupiedArea().getBBox().clone();
            Border border = this.<Border>getProperty(Property.BORDER);
            if (border != null) {
                rectangle.applyMargins(border.getWidth(), border.getWidth(), border.getWidth(), border.getWidth(),
                        false);
            }
            final float radius = Math.min(rectangle.getWidth(), rectangle.getHeight()) / 2;
            canvas.saveState();
            canvas.setFillColor(DEFAULT_CHECKED_COLOR);
            DrawingUtil.drawCircle(
                    canvas, rectangle.getLeft() + radius, rectangle.getBottom() + radius, radius / 2);
            canvas.restoreState();
        }

        /**
         * {@inheritDoc}
         *
         * @param drawContext {@inheritDoc}
         */
        @Override
        public void drawBorder(DrawContext drawContext) {
            Border border = getBorders()[0];
            if (border == null || !isDrawCircledBorder()) {
                super.drawBorder(drawContext);
                return;
            }

            // TODO: DEVSIX-7425 - Remove the following workaround once the ticket is fixed.
            // The rounded border/background is drawn lousy. It's not an exact circle for border radius 50%.
            // That is why we draw a real circle here by default
            final float borderWidth = border.getWidth();
            if (borderWidth > 0 && border.getColor() != null) {
                Rectangle rectangle = getOccupiedArea().getBBox().clone();
                rectangle.applyMargins(borderWidth, borderWidth, borderWidth, borderWidth, false);

                final float cx = rectangle.getX() + rectangle.getWidth() / 2;
                final float cy = rectangle.getY() + rectangle.getHeight() / 2;
                final float r = (Math.min(rectangle.getWidth(), rectangle.getHeight()) + borderWidth) / 2;
                drawContext.getCanvas()
                        .setStrokeColor(border.getColor())
                        .setLineWidth(borderWidth)
                        .circle(cx, cy, r)
                        .stroke();
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param drawContext {@inheritDoc}
         */
        @Override
        public void drawBackground(DrawContext drawContext) {
            Border border = getBorders()[0];
            if (border == null || !isDrawCircledBorder()) {
                super.drawBackground(drawContext);
                return;
            }

            // TODO: DEVSIX-7425 - Remove the following workaround once the ticket is fixed.
            // The rounded border/background is drawn lousy. It's not an exact circle for border radius 50%.
            // That is why we draw a real circle here by default
            // Draw a circle
            final float borderWidth = border.getWidth();
            Background background = this.<Background>getProperty(Property.BACKGROUND);
            final Color backgroundColor = background == null ? null : background.getColor();
            if (backgroundColor != null) {
                Rectangle rectangle = getOccupiedArea().getBBox().clone();
                rectangle.applyMargins(borderWidth, borderWidth, borderWidth, borderWidth, false);

                final float cx = rectangle.getX() + rectangle.getWidth() / 2;
                final float cy = rectangle.getY() + rectangle.getHeight() / 2;
                final float r = (Math.min(rectangle.getWidth(), rectangle.getHeight()) + borderWidth) / 2;
                drawContext.getCanvas()
                        .setFillColor(backgroundColor)
                        .circle(cx, cy, r)
                        .fill();
            }
        }
    }
}
