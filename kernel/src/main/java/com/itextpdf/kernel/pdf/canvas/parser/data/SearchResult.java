/** 
 * 包名：fadada.test
 * 文件名：fadada.test
 * 创建者：zyb
 * 创建日：2015-6-9
 *
 * CopyRight 2015 ShenZhen Fabigbig Technology Co.Ltd All Rights Reserved
 */
package com.itextpdf.kernel.pdf.canvas.parser.data;

/**
 * 在pdf进行文本查询返回的结果
 */
public class SearchResult implements Comparable<SearchResult>{
	
	/** 关键字所属页码（从1开始） */
	private int pagenum;
	
	/** 关键字中心点x轴坐标，相对于左下角为坐标原点 */
	private double x;
	
	/** 关键字中心点y轴坐标，相对于左下角为坐标原点 */
	private double y;
	
	/**
	 * <b>概要：</b>
	 * 	关键字所属页码（从1开始）
	 * <b>作者：</b>SUXH </br>
	 * <b>日期：</b>2018-1-2 </br>
	 * @return
	 */
	public int getPagenum() {
		return pagenum;
	}
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "SearchResult{" +
				"pagenum=" + pagenum +
				", x=" + x +
				", y=" + y +
				'}';
	}

	public SearchResult(int pagenum, double x, double y) {
		this.pagenum = pagenum;
		this.x = x;
		this.y = y;
	}

	public SearchResult() {
		super();
	}
	@Override
	public int compareTo(SearchResult otherSearchResult) {
        if (this == otherSearchResult) return 0;
		if(this.getPagenum() != otherSearchResult.getPagenum()) {
			return this.getPagenum() > otherSearchResult.getPagenum() ? 1 : -1;
		}
		if(this.getY() != otherSearchResult.getY()){//y轴坐标大的在前
			return this.getY() > otherSearchResult.getY() ? -1 : 1;
		}
		if(this.getX() != otherSearchResult.getX()){
			return this.getX() > otherSearchResult.getX() ? 1 : -1;
		}	
		return 0;
	}


}
