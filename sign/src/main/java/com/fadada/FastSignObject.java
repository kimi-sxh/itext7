package com.fadada;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.svg.element.SvgImage;

import java.util.List;

public class FastSignObject {

	/** 非svg图片 */
	private ImageData image;

	/** svg图片 */
	private SvgImage svgImage;

	/** 图片要放置的页码列表 */
	private List<VisibleSignature> vslist;

	public ImageData getImage() {
		return image;
	}

	public void setImage(ImageData image) {
		this.image = image;
	}

	public SvgImage getSvgImage() {
		return svgImage;
	}

	public void setSvgImage(SvgImage svgImage) {
		this.svgImage = svgImage;
	}

	public List<VisibleSignature> getVslist() {
		return vslist;
	}

	public void setVslist(List<VisibleSignature> vslist) {
		this.vslist = vslist;
	}
}
