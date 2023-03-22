package com.itextpdf.forms;

import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;


/**
 * 加密算法(3DESC)
 */
public class CryptTool {
	
	/**
	 * 普通key,不能修改。
	 */
	public static String CRYPT_KEY="YQ365123YQ365123YQ365123";
	
	/** CA证书相关的key ，不能修改*/
	public static String CRYPT_KEY_CA="YUNQIAN1YUNQIAN2YUNQIAN3";

	
	private static final String CRYPT_ALGORITHM = "DESede";
	
	/** 加密模式 */
	public static final int ENCRYPT_MODE = 1;
	
	/** 解密模式 */
	public static final int DECRYPT_MODE = 2;
	
	public CryptTool() {
		
	}
	/**
	 * 3DES加密模式
	 */
	 public static String encrypt(String value,String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), CRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedByte = cipher.doFinal(value.getBytes());
            String encodedByte = byte2hex(encryptedByte);
            return encodedByte;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
	 }
    
    /**
	 * 二进制转十六进制字符串。每一个字节转为两位十六进制字符串。
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int i = 0; i < b.length; i++) {
			stmp = Integer.toHexString(b[i] & 0XFF);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	
	/**
	 * 调用JCE实现3DES算法
	 * @param mode 1:加密, 2:解密
	 * @param inStr 要加密或者解密的数据
	 * @param key 24个字节的密钥
	 * @return 操作成功，则返回加密或者解密后的字符串；否则，返回null
	 */
	public static String tripleDES(int mode, String inStr, String key) {
		if(StringUtils.isBlank(inStr)){
			return "";
		}
		
		String outStr = null;
		byte[] inBytes = null;
		
		if(mode == ENCRYPT_MODE) {//加密
			try {
				inBytes = inStr.getBytes("UTF-8");
				outStr = CryptTool.byte2hex(CryptTool.encode(inBytes, "DESede", key.getBytes()));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			//	logger.error("3des加密发生异常", e);
			} catch (Exception e) {
				e.printStackTrace();
			//	logger.error("3des加密发生异常", e);
			}
		}
		if(mode == DECRYPT_MODE) {//解密
			try {
				inBytes = CryptTool.hex2byte(inStr);
				outStr = new String(CryptTool.decode(inBytes, "DESede", key.getBytes()), "UTF-8");
			} catch (UnsupportedEncodingException e) {
			//	logger.error("3des解密发生异常", e);
			} catch(IllegalArgumentException ex) {
            //    logger.error("输入参数错误：" + inStr);
            } catch (Exception e) {
            //	logger.error("3des解密发生异常", e);
			}
		}
		return outStr;
	}
	
	/**
	 * 加密
	 */
	public static byte[] encode(byte[] input, String algorithm, byte[] key) {

		byte[] output = null;
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, algorithm));
			output = cipher.doFinal(input);
		} catch (NoSuchAlgorithmException ex) {
			//logger.error("加密发生异常", e);
		} catch (NoSuchPaddingException ex) {
			//logger.error("加密发生异常", e);
		} catch (InvalidKeyException ex) {
			//logger.error("加密发生异常", e);
		} catch (IllegalStateException ex) {
			//logger.error("加密发生异常", e);
		} catch (IllegalBlockSizeException ex) {
			//logger.error("加密发生异常", e);
		} catch (BadPaddingException ex) {
			//logger.error("加密发生异常", e);
		}
		return output;
	}
	
  /**
   * 解密 
   */
	public static byte[] decode(byte[] input, String algorithm, byte[] key) {
		byte[] output = null;
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, algorithm));
			output = cipher.doFinal(input);
		} catch (NoSuchAlgorithmException ex) {
			//logger.error("解密发生异常", e);
		} catch (NoSuchPaddingException ex) {
			//logger.error("解密发生异常", e);
		} catch (InvalidKeyException ex) {
			//logger.error("解密发生异常", e);
		} catch (IllegalStateException ex) {
			//logger.error("解密发生异常", e);
		} catch (IllegalBlockSizeException ex) {
			//logger.error("解密发生异常", e);
		} catch (BadPaddingException ex) {
			//logger.error("解密发生异常", e);
		}
		return output;
	}
	
	/**
	 * 十六进制字符串转二进制。两位十六进制字符串转为一个字节。
	 * @param hex
	 * @return
	 */
	public static byte[] hex2byte(String hex) throws IllegalArgumentException {
		if (hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		if (hex.startsWith("0x")) {
			hex = hex.substring(2);
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}
	
	private static final int fillchar = '=';

	private static final String cvt = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	+ "abcdefghijklmnopqrstuvwxyz"
	+ "0123456789+/";
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
	/**
	 * ʹ��sun��˾��jce��ȫ�㷨
	 */
	static {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
	}

	/**
	 * <b>概要：</b>
	 * 转换字节数组为16进制字串
	 * 	如：字节数组对应的整型为52，
	 * 则其对应的16进制为：34.
	 * <b>作者：</b>suxh </br>
	 * <b>日期：</b>2015-1-12 </br>
	 * @param b 字节数组
	 * @return 对应的十六进制串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * MD5 摘要计算(byte[]).
	 * @param src byte[]
	 * @throws Exception
	 * @return byte[] 16 bit digest
	 */
	public static byte[] md5Digest(byte[] src) throws Exception {
		MessageDigest alg = MessageDigest
				.getInstance("MD5"); // MD5 is 16 bit message digest

		return alg.digest(src);
	}
	/**
	 * MD5 摘要计算(String).
	 * @param src String
	 * @throws Exception
	 * @return String
	 */
	public static String md5Digest(String src) throws Exception {
		return byteArrayToHexString(md5Digest(src.getBytes()));
	}
	public static byte[] encode(byte[] data) {
		int c;
		int len = data.length;
		StringBuffer ret = new StringBuffer(((len / 3) + 1) * 4);
		for (int i = 0; i < len; ++i) {
			c = (data[i] >> 2) & 0x3f;
			ret.append(cvt.charAt(c));
			c = (data[i] << 4) & 0x3f;
			if (++i < len) {
				c |= (data[i] >> 4) & 0x0f;

			}
			ret.append(cvt.charAt(c));
			if (i < len) {
				c = (data[i] << 2) & 0x3f;
				if (++i < len) {
					c |= (data[i] >> 6) & 0x03;

				}
				ret.append(cvt.charAt(c));
			} else {
				++i;
				ret.append((char) fillchar);
			}

			if (i < len) {
				c = data[i] & 0x3f;
				ret.append(cvt.charAt(c));
			} else {
				ret.append((char) fillchar);
			}
		}

		return (getBinaryBytes(ret.toString()));
	}
	/**
	 * 以下代码由Base64Crypt转移过来
	 */
	public static String encode2Str(byte[] data) {
		int c;
		int len = data.length;
		StringBuffer ret = new StringBuffer(((len / 3) + 1) * 4);
		for (int i = 0; i < len; ++i) {
			c = (data[i] >> 2) & 0x3f;
			ret.append(cvt.charAt(c));
			c = (data[i] << 4) & 0x3f;
			if (++i < len) {
				c |= (data[i] >> 4) & 0x0f;

			}
			ret.append(cvt.charAt(c));
			if (i < len) {
				c = (data[i] << 2) & 0x3f;
				if (++i < len) {
					c |= (data[i] >> 6) & 0x03;

				}
				ret.append(cvt.charAt(c));
			} else {
				++i;
				ret.append((char) fillchar);
			}

			if (i < len) {
				c = data[i] & 0x3f;
				ret.append(cvt.charAt(c));
			} else {
				ret.append((char) fillchar);
			}
		}

		return ret.toString();
	}
	public static byte[] decode(byte[] data) {
		int c;
		int c1;
		int len = data.length;
		StringBuffer ret = new StringBuffer((len * 3) / 4);
		for (int i = 0; i < len; ++i) {
			c = cvt.indexOf(data[i]);
			++i;
			c1 = cvt.indexOf(data[i]);
			c = ((c << 2) | ((c1 >> 4) & 0x3));
			ret.append((char) c);
			if (++i < len) {
				c = data[i];
				if (fillchar == c) {
					break;
				}

				c = cvt.indexOf((char) c);
				c1 = ((c1 << 4) & 0xf0) | ((c >> 2) & 0xf);
				ret.append((char) c1);
			}

			if (++i < len) {
				c1 = data[i];
				if (fillchar == c1) {
					break;
				}

				c1 = cvt.indexOf((char) c1);
				c = ((c << 6) & 0xc0) | c1;
				ret.append((char) c);
			}
		}

		return (getBinaryBytes(ret.toString()));
	}
	
	public static String decode2Str(byte[] data) {
		int c;
		int c1;
		int len = data.length;
		StringBuffer ret = new StringBuffer((len * 3) / 4);
		for (int i = 0; i < len; ++i) {
			c = cvt.indexOf(data[i]);
			++i;
			c1 = cvt.indexOf(data[i]);
			c = ((c << 2) | ((c1 >> 4) & 0x3));
			ret.append((char) c);
			if (++i < len) {
				c = data[i];
				if (fillchar == c) {
					break;
				}

				c = cvt.indexOf((char) c);
				c1 = ((c1 << 4) & 0xf0) | ((c >> 2) & 0xf);
				ret.append((char) c1);
			}

			if (++i < len) {
				c1 = data[i];
				if (fillchar == c1) {
					break;
				}

				c1 = cvt.indexOf((char) c1);
				c = ((c << 6) & 0xc0) | c1;
				ret.append((char) c);
			}
		}

		return ret.toString();
	}

	public static String getString(byte[] arr) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			buf.append((char) arr[i]);
		}
		return (buf.toString());
	}

	public static byte[] getBinaryBytes(String str) {
		byte[] b = new byte[str.length()];
		for (int i = 0; i < b.length; ++i) {
			b[i] = (byte) str.charAt(i);
		}
		return (b);
	}
	/*public static void main(String args[]) {
		String str = "http://10.17.37.17:5010/singleloing/return.jsp$1154448212312";
		if (args.length > 0) {
			str = args[0];
		}
		String temp = encode2Str(str.getBytes());
		String temp1 = decode2Str(temp.getBytes());
	}*/
	
	/**
	 * 实现SHA-1消息摘要
	 * @param inStr 
	 * @return 成功，返回摘要，失败，返回null
	 */
	public static String sha1(String inStr) {
		String outStr = null;
		try {
			outStr = CryptTool.digest(inStr.getBytes("UTF-8"), "SHA-1");
		} catch (UnsupportedEncodingException e) {
		//	logger.error("sha1发生异常", e);
		} catch (Exception e) {
		//	logger.error("sha1发生异常", e);
		}
		return outStr;
	}
	
	/**
	 * 调用sun公司的base64编码实现
	 * @param mode 1:加密, 2:解密
	 * @param inStr 要加密或者解密的数据
	 * @return 操作成功，则返回加密或者解密后的字符串；否则，返回null
	 */
	public static String base64(int mode, String inStr) {
		String outStr = null;
		byte[] inBytes = null;
		if(mode == ENCRYPT_MODE) {
			try {
				inBytes  = inStr.getBytes("UTF-8");
//				outStr = new BASE64Encoder().encode(inBytes);
				outStr = CryptTool.encode2Str(inBytes);
			} catch (UnsupportedEncodingException e) {
            //    logger.error("不支持的编码：UTF-8", e);
			} catch (Exception e) {
			//	logger.error("base64加密发生异常", e);
			}
		}
		if(mode == DECRYPT_MODE) {
			try {
				outStr = new String(new BASE64Decoder().decodeBuffer(inStr), "UTF-8");
			} catch (UnsupportedEncodingException e) {
           //     logger.error("不支持的编码：UTF-8");
			} catch (IOException e) {
			//	logger.error("base64加密发生IO异常", e);
			} catch (Exception e) {
			//	logger.error("base64解密发生异常", e);
			}
		}
		return outStr;
	}

	public static String Encrypt3Mix(String inStr, String key)
	{
		String authStr = CryptTool.sha1(inStr);
    	authStr = CryptTool.tripleDES(CryptTool.ENCRYPT_MODE, authStr, key);
    	authStr = CryptTool.base64(CryptTool.ENCRYPT_MODE, authStr);
    	return authStr;
	}
	
	
	public static String base64Encode(byte b[]){
        try{
           return (new BASE64Encoder()).encode(b);
        	
        }catch(Exception e){
//            logger.error("使用增强型BASE64编码格式对字节数组进行编码时出错!@" + e.getMessage());
        }
        return null;
    }

	/*public static void main(String[] args) {
		//System.out.println(Cipher.md5("88888888"));
		System.out.println(Cipher.tripleDES(1, "ZK18906057300118-18906057300-18906057300", "fstimobilefstimobilefsti"));
		//System.out.println("中国中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@中国通信服务厦门新东创软件公司@@@".length());
	}*/
	
	/**
	 * 以下从CipherHelper.java转移过来
	 */

	/**
	 * ��ϢժҪ��ʹ��algorithmժҪ�㷨��inputStr������ϢժҪ��֧��MD5��SHA-1�㷨
	 * 
	 * @param inputBytes
	 * @param algorithm
	 * @return �ɹ�������ժҪ�ַ����򣬷���null
	 */
	public static String digest(byte[] inputBytes, String algorithm) {
		String outputStr = null;
		try {
			MessageDigest alg = MessageDigest.getInstance(algorithm);
			alg.update(inputBytes);
			byte[] digest = alg.digest();
			outputStr = byte2hex(digest);
		} catch (NoSuchAlgorithmException ex) {
//			logger.error("û�������㷨��" + algorithm);
//			Debugger.output(ex, logger);
		}
		return outputStr;
	}
	
	/**
	 * 以下由Digest.java转移过来
	 */
	//用MD5校验信息是否更改,返回true没有更改，false更改
	public static boolean checkMD5Digest(String myInfo, String digestInfo) {
		if (myInfo == null || digestInfo == null)
			return false;
		try {
			MessageDigest alga = MessageDigest.getInstance("MD5");
			alga.update(myInfo.getBytes());
			byte[] digesta = alga.digest();
			return digestInfo.equals(byte2hex(digesta));
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	/**
	 * 加密
	 * @param str
	 * @return
	 */
	public static String dataEncrypt(String str) {
		String re = "";
		if (null!=str && !str.equals("")) {
			char[] ch = str.toCharArray();
			char[] newch= new char[ch.length];
			for(int i=0; i<ch.length; i++) {
				newch[i]=(char)(ch[i]+3);
			}
			re = new String(newch);
		}
		return re;
	}
	
	/**
	 * 解密
	 * @param str
	 * @return
	 */
	public static String dataDecrypt (String str) {
		String re = "";
		if (null!=str && !str.equals("")) {
			char[] ch = str.toCharArray();
			char[] newch= new char[ch.length];
			for(int i=0; i<ch.length; i++) {
				newch[i]=(char)(ch[i]-3);
			}
			re = new String(newch);
		}
		return re;
	}
    //3DES===================================================
	/**
	 * 3DES解密模式 
	 * @param value
	 * @param key
	 * @return
	 */
    public static String decrypt(String value,String key) {
        try {
        	
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), CRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CRYPT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            //byte[] decodedByte = Base64.decodeBase64(value.getBytes());
            byte[] decryptedByte = cipher.doFinal(hex2byte(value));            
            return new String(decryptedByte);
        } catch(Exception e) {
            return null;
        }
    }
    
    
     //福富提供的方法
     public static byte[] base64Decode(String str) {
		try {
			byte a[] = (new BASE64Decoder()).decodeBuffer(str);
			return a;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    
}
