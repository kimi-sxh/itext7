/**
* 版权所有：深圳法大大网络科技有限公司 
*====================================================
* 文件名称: CellHorizontalAlignment.java
* 修订记录：
* No    日期				作者(操作:具体内容)
* 1.    2017-4-24			苏晓慧(创建:创建文件)
*====================================================
* 类描述：(说明未实现或其它不应生成javadoc的内容)
* 
*/
package com.itextpdf.forms.suxh.enumobj;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>概要:</h3> 
 *    
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * 		<li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * 		<li>2017-8-7[SUXH] 新建</li>
 * </ol>
 */
public enum CheckFlagEnum {

	NO_CHECK(0,"不检查"),
	
	CHECK(1,"检查");
	
	/** 字段值 */
    private Integer value;
    
    /** 字段值的实际意义 */
    private String valueInFact;
	
	public Integer getValue() {
		return value;
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
	public static CheckFlagEnum convertByValue(String value){
        for (CheckFlagEnum checkFlagEnum :CheckFlagEnum.values()) {
            if (checkFlagEnum.getValue().equals(value)) {
                return checkFlagEnum;
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
	public static List<Integer> getValueList(){
	   List<Integer> values  =new ArrayList<Integer>();
	   CheckFlagEnum[] checkFlagEnumArr = CheckFlagEnum.values();
       for(CheckFlagEnum i: checkFlagEnumArr){
    	   values.add(i.getValue());
       }
       return values;
	}
	
	CheckFlagEnum(Integer value, String valueInFact){
		this.value = value;
		this.valueInFact = valueInFact;
	}

}
