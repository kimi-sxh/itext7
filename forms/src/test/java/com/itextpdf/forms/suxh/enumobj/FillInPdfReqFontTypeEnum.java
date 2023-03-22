package com.itextpdf.forms.suxh.enumobj;

/**
* 版权所有：深圳法大大网络科技有限公司 
*====================================================
* 文件名称: EnumCacheContainer.java
* 修订记录：
* No    日期				作者(操作:具体内容)
* 1.    2015-3-17			苏晓慧(创建:创建文件)
*====================================================
* 类描述：(说明未实现或其它不应生成javadoc的内容)
* 
*/

import java.util.ArrayList;
import java.util.List;


/**
 * <h3>概要:</h3> 
 *   填充pdf字体设定：0：宋体；1：仿宋；2、黑体；3、楷体。
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * 		<li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * 		<li>2015-3-17[SUXH] 新建</li>
 * </ol>
 */
public enum FillInPdfReqFontTypeEnum {
	/** 宋体 */
	SONG_TI(0,"宋体"),
	
	/** 仿宋 */
	FANG_SONG(1,"仿宋"),
	
	/** 黑体 */
	HEI_TI(2,"黑体"),
	
	/** 楷体 */
	KAI_TI(3,"楷体"),
	
	/** 微软雅黑 */
	MICROSOFY_YA_HEI(4,"微软雅黑"),

	/** TIME_NEW_ROMAN */
	TIME_NEW_ROMAN(5,"TIME_NEW_ROMAN"),

	/** arial */
	ARIAL(6,"arial");
	
	/** 字段值 */
    private Integer value;
    
    /** 字段值的实际意义 */
    private String valueInFact;
	
	public Integer getValue(){
		return this.value;
	}
	
	public void setValue(Integer value) {
		this.value = value;
	}
	
	public String getValueInFact() {
		return valueInFact;
	}

	public void setValueInFact(String valueInFact) {
		this.valueInFact = valueInFact;
	}

	/**
	 * <b>概要：</b>
	 * 	根据属性值匹配属性
	 * <b>作者：</b>SUXH </br>
	 * <b>日期：</b>2015-3-17 </br>
	 * @param value 需要匹配的属性值
	 * @return
	 */
	public static FillInPdfReqFontTypeEnum convertByValue(Integer value){
        for (FillInPdfReqFontTypeEnum fillInPdfReqFontTypeEnum :FillInPdfReqFontTypeEnum.values()) {
            if (fillInPdfReqFontTypeEnum.getValue().equals(value)) {
                return fillInPdfReqFontTypeEnum;
            }  
        }  
        return null;  
	}
	
	/**
	 * <b>概要：</b>
	 * 	获得枚举的所有值（all value）
	 * <b>作者：</b>SUXH </br>
	 * <b>日期：</b>2015-3-31 </br>
	 * @return 枚举的所有值
	 */
	public static List<String> getValueList(){
	   List<String> values=new ArrayList<String>();
	   FillInPdfReqFontTypeEnum[] fillInPdfReqFontTypeEnumArr =FillInPdfReqFontTypeEnum.values();
       for(FillInPdfReqFontTypeEnum i: fillInPdfReqFontTypeEnumArr){
    	   values.add(i.getValue().toString());
       }
       return values;
	}
	
	FillInPdfReqFontTypeEnum(Integer value, String valueInFact){
		this.value=value;
		this.valueInFact=valueInFact;
	}

}
