package com.yonyou.ucf.mdf.sample.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA2签名工具类
 */
public class RSA2Util {


    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    /**
     * 签名
     * @param content 待签名字符串
     * @param privateKey 私钥
     * @return 签名结果
     */
    public static String sign(String content, String privateKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                Base64.getDecoder().decode(privateKey)
        );
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(priKey);
        signature.update(content.getBytes("UTF-8"));
        byte[] signed = signature.sign();

        return Base64.getEncoder().encodeToString(signed);
    }

    /**
     * 验证签名
     * @param content 待验证字符串
     * @param sign 签名值
     * @param publicKey 公钥
     * @return 验证结果
     */
    public static boolean verify(String content, String sign, String publicKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
                Base64.getDecoder().decode(publicKey)
        );
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(content.getBytes("UTF-8"));

        return signature.verify(Base64.getDecoder().decode(sign));
    }
}
