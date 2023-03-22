/** 
 * 包名：com.yq365.api.beans
 * 文件名：com.yq365.api.beans
 * 创建者：SUXH
 * 创建日：2018-6-27
 *
 * CopyRight 2015 ShenZhen Fabigbig Technology Co.Ltd All Rights Reserved
 */
package com.itextpdf.forms.suxh.param;

/**
 * <h3>概要:</h3> 
 *    二维码相关信息
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * 		<li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * 		<li>2018-6-27[SUXH] 新建</li>
 * </ol>
 */
public class QRCodeInfo {
	
	/** 二维码图片宽度 */
	private Integer width = 44;
	
	/** 二维码图片高度 */
	private Integer height = 44;
	
	/** 二维码水平位置信息，0：居中；1：左对齐；2：右对齐 */
	private Integer horizontalAlignment = 0;
	
	/** 二维码水平偏移量 */
	private Integer horizontalOffset = 0;
	
	/** 二维码垂直位置信息，0：头部；1：尾部 */
	private Integer verticalAlignment = 0;
	
	/** 二维码垂直偏移量 */
	private Integer verticalOffset = 0;
	
	/** 二维码扫描之后的内容 可以是文字，也可以是链接，有链接则调整到该链接所对应的页面 */
	private String content;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(Integer horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public Integer getHorizontalOffset() {
        return horizontalOffset;
    }

    public void setHorizontalOffset(Integer horizontalOffset) {
        this.horizontalOffset = horizontalOffset;
    }

    public Integer getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(Integer verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public Integer getVerticalOffset() {
        return verticalOffset;
    }

    public void setVerticalOffset(Integer verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
