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
package com.itextpdf.forms;

import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.AbstractPdfFormField;
import com.itextpdf.forms.logs.FormsLogMessageConstants;
import com.itextpdf.forms.fields.PdfFormFieldMergeUtil;
import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.pdf.IPdfPageExtraCopier;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A sample implementation of the {#link IPdfPageExtraCopier} interface which
 * copies only AcroForm fields to a new page.
 *
 * <p>
 * NOTE: While it's absolutely not necessary to use the same PdfPageFormCopier instance for copying operations,
 * it is still worth to know that PdfPageFormCopier uses some caching logic which can potentially improve performance
 * in case of the reusing of the same instance.
 */
public class PdfPageFormCopier implements IPdfPageExtraCopier {

    private PdfAcroForm formFrom;
    private PdfAcroForm formTo;
    private PdfDocument documentFrom;
    private PdfDocument documentTo;
    private static Logger logger = LoggerFactory.getLogger(PdfPageFormCopier.class);

    @Override
    public void copy(PdfPage fromPage, PdfPage toPage) {
        if (documentFrom != fromPage.getDocument()) {
            documentFrom = fromPage.getDocument();
            formFrom = PdfAcroForm.getAcroForm(documentFrom, false);
        }
        if (documentTo != toPage.getDocument()) {
            documentTo = toPage.getDocument();
        }
        // We should always regenerate the acroform if we expect the same result when the old or new
        // PdfPageFormCopier instance is used because getAcroForm changes the fields structure,
        // e.g. removes wrong field keys from the pure widget annotations dictionaries.
        formTo = PdfAcroForm.getAcroForm(documentTo, true);

        if (formFrom == null) {
            return;
        }

        //duplicate AcroForm dictionary
        List<PdfName> excludedKeys = new ArrayList<>();
        excludedKeys.add(PdfName.Fields);
        excludedKeys.add(PdfName.DR);

        PdfDictionary dict = formFrom.getPdfObject().copyTo(documentTo, excludedKeys, false);
        formTo.getPdfObject().mergeDifferent(dict);

        Map<String, PdfFormField> fieldsFrom = formFrom.getAllFormFields();
        if (fieldsFrom.size() <= 0) {
            return;
        }
        Map<String, PdfFormField> fieldsTo = formTo.getDirectFormFields();

        List<PdfAnnotation> annots = toPage.getAnnotations();

        for (PdfAnnotation annot : annots) {
            if (!annot.getSubtype().equals(PdfName.Widget)) {
                continue;
            }
            copyField(toPage, fieldsFrom, fieldsTo, annot);
        }
    }

    private AbstractPdfFormField makeFormField(PdfObject fieldDict) {
        AbstractPdfFormField field = PdfFormField.makeFormFieldOrAnnotation(fieldDict, documentTo);
        if (field == null) {
            logger.warn(MessageFormatUtil.format(FormsLogMessageConstants.CANNOT_CREATE_FORMFIELD,
                    fieldDict.getIndirectReference()));
        }
        return field;
    }

    private void copyField(PdfPage toPage, Map<String, PdfFormField> fieldsFrom,
                           Map<String, PdfFormField> fieldsTo, PdfAnnotation currentAnnot) {
        PdfDictionary parent = currentAnnot.getPdfObject().getAsDictionary(PdfName.Parent);
        if (parent != null) {
            PdfFormField parentField = getParentField(parent, documentTo);
            if (parentField == null) {
                return;
            }
            PdfString parentName = parentField.getFieldName();
            if (parentName == null) {
                return;
            }
            copyParentFormField(toPage, fieldsTo, currentAnnot, parentField);
        } else {
            PdfString annotName = currentAnnot.getPdfObject().getAsString(PdfName.T);
            String annotNameString = null;
            if (annotName != null) {
                annotNameString = annotName.toUnicodeString();
            }
            if (annotNameString != null && fieldsFrom.containsKey(annotNameString)) {
                // In this piece on code we expect annotation with T field
                // It could mean only merged form field and annotation
                // This cast must be ok
                PdfFormField field = (PdfFormField)makeFormField(currentAnnot.getPdfObject());
                if (field == null) {
                    return;
                }
                if (fieldsTo.get(annotNameString) != null) {
                    field = mergeFieldsWithTheSameName(field);
                }
                // Form may be already added to the page. PdfAcroForm will take care about it.
                formTo.addField(field, toPage, true);
                field.updateDefaultAppearance();
            }
        }
    }

    private void copyParentFormField(PdfPage toPage, Map<String, PdfFormField> fieldsTo,
                                     PdfAnnotation annot, PdfFormField parentField) {
        PdfString parentName = parentField.getFieldName();
        // parentField should be the root field
        if (!fieldsTo.containsKey(parentName.toUnicodeString())) {
            // no such field, hence we should simply add it
            PdfFormField field = createParentFieldCopy(annot.getPdfObject(), documentTo);
            PdfArray kids = field.getKids();
            field.getPdfObject().remove(PdfName.Kids);
            formTo.addField(field, toPage, true);
            field.getPdfObject().put(PdfName.Kids, kids);
        } else {
            // annot is either a field (field name will not be null) or a widget (field name is null)
            AbstractPdfFormField field = makeFormField(annot.getPdfObject());
            if (field == null) {
                return;
            }
            PdfString fieldName = field.getFieldName();
            if (fieldName != null) {
                PdfFormField existingField = fieldsTo.get(fieldName.toUnicodeString());
                if (existingField != null) {
                    PdfFormField mergedField = mergeFieldsWithTheSameName(field);
                    formTo.getDirectFormFields().put(mergedField.getFieldName().toUnicodeString(), mergedField);
                } else {
                    HashSet<String> existingFields = new HashSet<>();
                    getAllFieldNames(formTo.getFields(), existingFields);
                    addChildToExistingParent(annot.getPdfObject(), existingFields,
                            fieldsTo);
                }
            } else {
                if (!parentField.getKids().contains(field.getPdfObject())
                        && formTo.getFields().contains(parentField.getPdfObject())) {
                    // annot's parent is already a field of the resultant document,
                    // hence we only need to update its children
                    HashSet<String> existingFields = new HashSet<>();
                    getAllFieldNames(formTo.getFields(), existingFields);
                    addChildToExistingParent(annot.getPdfObject(), existingFields);
                } else {
                    // its parent is not a field of the resultant document, but the latter contains
                    // a field of the same name, therefore we should merge them (note that merging in this context
                    // differs from merging a widget and an annotation into a single entity)
                    PdfFormField mergedField = mergeFieldsWithTheSameName(field);
                    // we need to add the field not to its representation (#getFormFields()), but to
                    // /Fields entry of the acro form
                    formTo.addField(mergedField, toPage, true);
                }
            }
        }
    }

    private PdfFormField mergeFieldsWithTheSameName(AbstractPdfFormField newField) {
        PdfString fieldName = newField.getPdfObject().getAsString(PdfName.T);
        
        PdfDictionary parent = newField.getParent();
        if (parent != null) {
            newField.setParent(PdfFormField.makeFormField(parent, newField.getDocument()));
            if (fieldName == null) {
                if (newField.isTerminalFormField()) {
                    fieldName = new PdfString(parent.getAsString(PdfName.T).toUnicodeString() + ".");
                } else {
                    fieldName = parent.getAsString(PdfName.T);
                }
            }
        }
        
        String fullFieldName = fieldName.toUnicodeString();
        if (null != newField.getFieldName()) {
            fullFieldName = newField.getFieldName().toUnicodeString();
        }

        logger.warn(MessageFormatUtil.format(IoLogMessageConstant.DOCUMENT_ALREADY_HAS_FIELD, fullFieldName));

        PdfFormField existingField = formTo.getField(fullFieldName);
        if (existingField.isFlushed() && newField instanceof PdfFormField) {
            int index = 0;
            do {
                index++;
                ((PdfFormField)newField).setFieldName(fieldName.toUnicodeString() + "_#" + index);
                fullFieldName = newField.getFieldName().toUnicodeString();
            } while (formTo.getField(fullFieldName) != null);
            return (PdfFormField)newField;
        }

        formTo.getFields().remove(existingField.getPdfObject());

        if (newField instanceof PdfFormField) {
            PdfFormFieldMergeUtil.mergeTwoFieldsWithTheSameNames(existingField, (PdfFormField) newField, true);
        } else {
            existingField.addKid(newField);
        }

        return existingField;
    }

    private static PdfFormField getParentField(PdfDictionary parent, PdfDocument pdfDoc) {
        PdfDictionary parentOfParent = parent.getAsDictionary(PdfName.Parent);
        if (parentOfParent != null) {
            return getParentField(parentOfParent, pdfDoc);
        }

        return PdfFormField.makeFormField(parent, pdfDoc);
    }

    private PdfFormField createParentFieldCopy(PdfDictionary fieldDic, PdfDocument pdfDoc) {
        PdfDictionary parent = fieldDic.getAsDictionary(PdfName.Parent);
        PdfFormField field;

        if (parent != null) {
            field = createParentFieldCopy(parent, pdfDoc);
            PdfArray kids = (PdfArray) parent.get(PdfName.Kids);
            if (kids == null) {
                parent.put(PdfName.Kids, new PdfArray(fieldDic));
            } else {
                kids.add(fieldDic);
            }
        } else {
            field = PdfFormField.makeFormField(fieldDic, pdfDoc);
        }

        return field;
    }

    private void addChildToExistingParent(PdfDictionary fieldDic, Set<String> existingFields) {
        PdfDictionary parent = fieldDic.getAsDictionary(PdfName.Parent);
        if (parent == null) {
            return;
        }

        PdfString parentName = parent.getAsString(PdfName.T);
        if (parentName != null) {
            String name = parentName.toUnicodeString();
            if (existingFields.contains(name)) {
                PdfArray kids = parent.getAsArray(PdfName.Kids);
                kids.add(fieldDic);
            } else {
                parent.put(PdfName.Kids, new PdfArray(fieldDic));
                addChildToExistingParent(parent, existingFields);
            }
        }
    }

    private void addChildToExistingParent(PdfDictionary fieldDic, Set<String> existingFields,
                                          Map<String, PdfFormField> fieldsTo) {
        PdfDictionary parent = fieldDic.getAsDictionary(PdfName.Parent);
        if (parent == null) {
            return;
        }

        PdfString parentName = parent.getAsString(PdfName.T);
        if (parentName != null) {
            String name = parentName.toUnicodeString();
            if (existingFields.contains(name)) {
                PdfArray kids = parent.getAsArray(PdfName.Kids);
                for (PdfObject kid : kids) {
                    if (((PdfDictionary) kid).get(PdfName.T) != null &&
                            ((PdfDictionary) kid).get(PdfName.T).equals(fieldDic.get(PdfName.T))) {
                        AbstractPdfFormField kidField = makeFormField(kid);
                        AbstractPdfFormField field = makeFormField(fieldDic);
                        if (kidField == null || field == null) {
                            continue;
                        }
                        fieldsTo.put(kidField.getFieldName().toUnicodeString(), (PdfFormField)kidField);
                        PdfFormField mergedField = mergeFieldsWithTheSameName(field);
                        formTo.getDirectFormFields().put(mergedField.getFieldName().toUnicodeString(), mergedField);
                        return;
                    }
                }
                kids.add(fieldDic);
            } else {
                parent.put(PdfName.Kids, new PdfArray(fieldDic));
                addChildToExistingParent(parent, existingFields);
            }
        }
    }

    private void getAllFieldNames(PdfArray fields, Set<String> existingFields) {
        for (PdfObject field : fields) {
            if (field.isFlushed()) {
                continue;
            }
            PdfDictionary dic = (PdfDictionary) field;
            PdfString name = dic.getAsString(PdfName.T);
            if (name != null) {
                existingFields.add(name.toUnicodeString());
            }
            PdfArray kids = dic.getAsArray(PdfName.Kids);
            if (kids != null) {
                getAllFieldNames(kids, existingFields);
            }
        }
    }
}
