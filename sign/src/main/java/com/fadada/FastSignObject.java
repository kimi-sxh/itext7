package com.fadada;

import com.itextpdf.layout.element.Image;

import java.util.List;

public class FastSignObject {

	/** svg图片 */
	private Image imageObj;

	/** 图片要放置的页码列表 */
	private List<VisibleSignature> vslist;

	public Image getImageObj() {
		return imageObj;
	}

	public void setImageObj(Image imageObj) {
		this.imageObj = imageObj;
	}

	public List<VisibleSignature> getVslist() {
		return vslist;
	}

	public void setVslist(List<VisibleSignature> vslist) {
		this.vslist = vslist;
	}
}
