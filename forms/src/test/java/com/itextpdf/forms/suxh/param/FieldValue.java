package com.itextpdf.forms.suxh.param;

/**
 * <h3>概要:</h3>
 * TODO(请在此处填写概要)
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * <li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * <li>2020/11/18[SUXH] 新建</li>
 * </ol>
 */
public class FieldValue {

    /** 表单域域名 */
    private String name;

    /** 填入值 */
    private String value;

    /** 字体类型 参见枚举：FillInPdfReqFontTypeEnum */
    private Integer fontType;

    /** 字体大小 */
    private Float fontSize;

    /** 是否设置透明 */
    private Boolean transparencyFlag;

    public FieldValue() {
    }

    public FieldValue(String name, String value, Integer fontType, Float fontSize, Boolean transparencyFlag) {
        this.name = name;
        this.value = value;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.transparencyFlag = transparencyFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getFontType() {
        return fontType;
    }

    public void setFontType(Integer fontType) {
        this.fontType = fontType;
    }

    public Float getFontSize() {
        return fontSize;
    }

    public void setFontSize(Float fontSize) {
        this.fontSize = fontSize;
    }

    public Boolean getTransparencyFlag() {
        return transparencyFlag;
    }

    public void setTransparencyFlag(Boolean transparencyFlag) {
        this.transparencyFlag = transparencyFlag;
    }

    @Override
    public String toString() {
        return "FieldValue{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", fontType=" + fontType +
                ", fontSize=" + fontSize +
                ", transparencyFlag=" + transparencyFlag +
                '}';
    }
}
