package com.itextpdf.forms.suxh.param;

import java.util.List;

/**
 * <h3>概要:</h3>
 *      请求填充表单域的bean
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * <li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * <li>2020/7/15[SUXH] 新建</li>
 * </ol>
 */
public class FillInPdfReqBean {

    /** 文件名称uuid 不传则由sdk生成 */
    private String uuid;

    /** pdf模板本地路径 */
    private String pdfTemplatePath;

    /** 目标文件目录 */
    private String destFolder;

    /** 填充表单域 */
    private List<FieldValue> fieldValueList;

    /** 是否使表单域扁平化，也即设置为true时，表单域将无法编辑，下次调用填充无法找到表单域信息；设置为false，则表单域依然可编辑 默认值为true */
    private Boolean formFlattening = true;

    /** 是否检查表单域时候存在 false-不检查；true-检查 */
    private Boolean formFieldCheckFlag;

    /** 二维码信息 */
    private QRCodeInfo qrCodeInfo;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPdfTemplatePath() {
        return pdfTemplatePath;
    }

    public void setPdfTemplatePath(String pdfTemplatePath) {
        this.pdfTemplatePath = pdfTemplatePath;
    }

    public String getDestFolder() {
        return destFolder;
    }

    public void setDestFolder(String destFolder) {
        this.destFolder = destFolder;
    }

    public List<FieldValue> getFieldValueList() {
        return fieldValueList;
    }

    public void setFieldValueList(List<FieldValue> fieldValueList) {
        this.fieldValueList = fieldValueList;
    }

    public Boolean getFormFlattening() {
        return formFlattening;
    }

    public void setFormFlattening(Boolean formFlattening) {
        this.formFlattening = formFlattening;
    }

    public Boolean getFormFieldCheckFlag() {
        return formFieldCheckFlag;
    }

    public void setFormFieldCheckFlag(Boolean formFieldCheckFlag) {
        this.formFieldCheckFlag = formFieldCheckFlag;
    }

    public QRCodeInfo getQrCodeInfo() {
        return qrCodeInfo;
    }

    public void setQrCodeInfo(QRCodeInfo qrCodeInfo) {
        this.qrCodeInfo = qrCodeInfo;
    }
}
