package com.itextpdf.forms.suxh.resource;

import com.itextpdf.kernel.crypto.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <h3>概要:</h3>
 *      PDF释放资源
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
public class PdfResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PdfResource.class);

     /**
      * <b>概要：</b>:
      *     获取PdfReader对象
      * <b>作者：</b>SUXH</br>
      * <b>日期：</b>2018/12/25 14:38 </br>
      * @param src 原文件路径
      * @return PdfReader实例
      */
     public static PdfReader getPdfReader(String src) {
         try {
             PdfReader pdfReader = new PdfReader(src);
             return pdfReader;
         } catch(BadPasswordException e) {
        	 LOGGER.error("请检查文档是否加密：",e);
         } catch (IOException e) {
             LOGGER.error("构造PdfReader出异常：" + src, e);
         }
         return null;
     }

}
