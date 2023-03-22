/**
 * 包名：com.yq365.exception
 * 文件名：com.yq365.exception
 * 创建者：SUXH
 * 创建日：2015-5-25
 *
 * CopyRight 2015 ShenZhen Fabigbig Technology Co.Ltd All Rights Reserved
 */
package com.itextpdf.forms.suxh.exception;

/**
 * <h3>概要:</h3>
 *    TODO(请在此处填写概要)
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * 		<li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * 		<li>2015-5-25[SUXH] 新建</li>
 * </ol>
 */
public class ApplicationException extends RuntimeException {


	/**
	 * 概要：ApplicationException类的构造函数
	 * @param message
	 */
	public ApplicationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 概要：ApplicationException类的构造函数
	 * @param cause
	 */
	public ApplicationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 概要：ApplicationException类的构造函数
	 * @param message
	 * @param cause
	 */
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
