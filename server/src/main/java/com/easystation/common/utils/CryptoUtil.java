package com.easystation.common.utils;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@ApplicationScoped
@Startup
public class CryptoUtil {

    private PrivateKey privateKey;

    public CryptoUtil() {
        try {
            loadPrivateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private void loadPrivateKey() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("privateKey.pem")) {
            if (is == null) {
                throw new RuntimeException("privateKey.pem not found in classpath");
            }
            String keyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String privateKeyPEM = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // Remove newlines and spaces

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            this.privateKey = keyFactory.generatePrivate(keySpec);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
