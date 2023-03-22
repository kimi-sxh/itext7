package com.itextpdf.forms.suxh.resource;

import com.itextpdf.forms.suxh.enumobj.FillInPdfReqFontTypeEnum;
import com.itextpdf.forms.suxh.exception.ApplicationException;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.util.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

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
 * <li>2018/12/25[SUXH] 新建</li>
 * </ol>
 */
public class FontResource {

    public static final String FONT_FOLDER = "./src/test/java/com/itextpdf/forms/suxh/resource/font/";

    /**
     * <b>概要：</b>:
     * 创建BaseFont实例（Base class for the several font types supported-看具体子类）
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2018/12/25 15:26 </br>
     *
     * @param name     the name of the font or its location on file
     * @param encoding the encoding to be applied to this font
     * @param embedded true if the font is to be embedded in the PDF
     * @return returns a new font. This font may come from the cache
     */
    public static PdfFont createBaseFont(String name, String encoding, boolean embedded) throws IOException {
        /*
        *Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM file,
        * a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier appended to the name.
        * These modifiers are: Bold, Italic and BoldItalic. An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in TrueType collections
         * are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes start at 0), in this case "MS PGothic".
            The fonts are cached and if they already exist they are extracted from the cache, not parsed again.
         */
        PdfFont baseFont = PdfFontFactory.createFont(name, encoding, embedded);
        return baseFont;
    }

    /**
     * <b>概要：</b>
     * 获取evidencepdf文档字体
     * <b>作者：</b>SUXH </br>
     * <b>日期：</b>2018-2-26 </br>
     *
     * @param fontType 参见枚举FillInPdfReqFontType
     * @return
     */
    public static PdfFont getFillFont(Integer fontType) {
        PdfFont bfChinese = null;
        try {
            if (null != fontType) {
                if (FillInPdfReqFontTypeEnum.SONG_TI.getValue().equals(fontType)) {
                    if (SystemUtils.IS_OS_WINDOWS) {//icepdf在windows下可以正常切adobe自带宋体
                        bfChinese = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
                    } else {//icepdf在linux下无法正常切adobe自带宋体，需要用到系统字体
                        byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simsun.ttf");
                        bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                    }
                } else if (FillInPdfReqFontTypeEnum.FANG_SONG.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simfang.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else if (FillInPdfReqFontTypeEnum.HEI_TI.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simhei.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else if (FillInPdfReqFontTypeEnum.KAI_TI.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simkai.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else if (FillInPdfReqFontTypeEnum.MICROSOFY_YA_HEI.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "msyh.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else if (FillInPdfReqFontTypeEnum.TIME_NEW_ROMAN.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "times.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else if (FillInPdfReqFontTypeEnum.ARIAL.getValue().equals(fontType)) {
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "arialuni.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                } else {
                    if (SystemUtils.IS_OS_WINDOWS) {//icepdf在windows下可以正常切adobe自带宋体
                        bfChinese = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
                    } else {//icepdf在linux下无法正常切adobe自带宋体，需要用到系统字体
                        byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simsun.ttf");
                        bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                    }
                }
            } else {
                if (SystemUtils.IS_OS_WINDOWS) {//icepdf在windows下可以正常切adobe自带宋体
                    bfChinese = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true);
                } else {//icepdf在linux下无法正常切adobe自带宋体，需要用到系统字体
                    byte[] bytes = FileUtil.getBytes(FONT_FOLDER + "simsun.ttf");
                    bfChinese = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
                }
            }
        } catch (IOException e) {
            throw new ApplicationException("获取字体文件失败：", e);
        }
        return bfChinese;
    }


}
