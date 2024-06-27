package com.fadada;


import com.itextpdf.kernel.geom.Rectangle;

public class VisibleSignature {
	private Rectangle rect;
	private int page;
	private String fieldName;
	public Rectangle getRect() {
		return rect;
	}
	public void setRect(Rectangle rect) {
		this.rect = rect;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public VisibleSignature(Rectangle rect, int page, String fieldName) {
		super();
		this.rect = rect;
		this.page = page;
		this.fieldName = fieldName;
	}
	
}
