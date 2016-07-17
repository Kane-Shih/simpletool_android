package tw.kaneshih.simpletool.utility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Base64;

/**
 * Private key in PKCS#8<br/>
 * If using OpenSSL, follow steps below:<br/>
 * 1. openssl genrsa -out rsa_private_key.pem 2048<br/>
 * 2. openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -pubout<br/>
 * 3. openssl pkcs8 -topk8 -in rsa_private_key.pem -out
 * pkcs8_rsa_private_key.pem -nocrypt<br/>
 */
public class RsaUtil {

    /**
     * format should be X.509
     * 
     * @param publicKeyBase64
     * @return
     */
    public static PublicKey getPublicKey(String publicKeyBase64) {
        if (Validator.isEmpty(publicKeyBase64)) {
            return null;
        }
        publicKeyBase64 = publicKeyBase64.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
        if (Validator.isEmpty(publicKeyBase64)) {
            return null;
        }
        byte[] pubicKeyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP);
        return getPublicKey(pubicKeyBytes);
    }

    /**
     * format should be X.509
     * 
     * @param publicKeyFile
     * @return
     */
    public static PublicKey getPublicKey(File publicKeyFile) {
        String publicKeyBase64 = FileUtil.getStringFromFile(publicKeyFile, "UTF-8");
        return getPublicKey(publicKeyBase64);
    }

    /**
     * format should be X.509
     * 
     * @param publicKeyBytes
     * @return
     */
    public static PublicKey getPublicKey(byte[] publicKeyBytes) {
        if (Validator.isNull(publicKeyBytes)) {
            return null;
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey(String privateKeyBase64) {
        if (Validator.isEmpty(privateKeyBase64)) {
            return null;
        }
        privateKeyBase64 = privateKeyBase64
                .replaceAll("(-+BEGIN PRIVATE KEY-+\\r?\\n|-+END PRIVATE KEY-+\\r?\\n?)", ""); // PKCS#8
        if (Validator.isEmpty(privateKeyBase64)) {
            return null;
        }
        byte[] privateKeyBytes = Base64.decode(privateKeyBase64, Base64.NO_WRAP);
        return getPrivateKey(privateKeyBytes);
    }

    public static PrivateKey getPrivateKey(File privateKeyFile) {
        String privateKeyBase64 = FileUtil.getStringFromFile(privateKeyFile, "UTF-8");
        return getPrivateKey(privateKeyBase64);
    }

    public static PrivateKey getPrivateKey(byte[] privateKeyBytes) {
        if (Validator.isNull(privateKeyBytes)) {
            return null;
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(Key key, String dataBase64) {
        if (Validator.isNull(key) || Validator.isEmpty(dataBase64)) {
            return null;
        }
        return encrypt(key, Base64.decode(dataBase64, Base64.NO_WRAP));
    }

    public static byte[] encrypt(Key key, byte[] data) {
        if (Validator.isNull(key) || Validator.isNull(data) || data.length > key.getEncoded().length - 11) {
            return null;
        }

        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, key);
            return c.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(Key key, String encryptedDataBase64) {
        if (Validator.isNull(key) || Validator.isEmpty(encryptedDataBase64)) {
            return null;
        }
        return decrypt(key, Base64.decode(encryptedDataBase64, Base64.NO_WRAP));
    }

    public static byte[] decrypt(Key key, byte[] data) {
        if (Validator.isNull(key) || Validator.isNull(data)) {
            return null;
        }
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, key);
            return c.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSingature(PrivateKey key, String data) {
        try {
            return getSignature(key, data.getBytes("UTF-8"), "SHA256withRSA");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSignature(PrivateKey key, byte[] data) {
        return getSignature(key, data, "SHA256withRSA");
    }

    public static byte[] getSingature(PrivateKey key, String data, String algorithm) {
        try {
            return getSignature(key, data.getBytes("UTF-8"), algorithm);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSignature(PrivateKey key, byte[] data, String algorithm) {
        if (Validator.isNull(key) || Validator.isEmpty(algorithm) || Validator.isNull(data)) {
            return null;
        }
        try {
            Signature s = Signature.getInstance(algorithm);
            s.initSign(key);
            s.update(data);
            return s.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignature(PublicKey key, String data, String signatureBase64) {
        return verifySignature(key, data, signatureBase64, "SHA256withRSA");
    }

    public static boolean verifySignature(PublicKey key, byte[] data, byte[] signature) {
        return verifySignature(key, data, signature, "SHA256withRSA");
    }

    public static boolean verifySignature(PublicKey key, String data, String signatureBase64, String algorithm) {
        try {
            return verifySignature(
                    key,
                    data.getBytes("UTF-8"),
                    Base64.decode(signatureBase64, Base64.NO_WRAP),
                    algorithm);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifySignature(PublicKey key, byte[] data, byte[] signature, String algorithm) {
        if (Validator.isNull(key) || Validator.isEmpty(algorithm) || Validator.isNull(signature)
                || Validator.isNull(data)) {
            return false;
        }
        try {
            Signature s = Signature.getInstance(algorithm);
            s.initVerify(key);
            s.update(data);
            return s.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static KeyPair genKeyPair() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            random.setSeed(String.valueOf(System.currentTimeMillis()).getBytes());
            keygen.initialize(2048, random);
            KeyPair keyPair = keygen.genKeyPair();
            return keyPair;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveToFile(PublicKey key, File file) {
        if (Validator.isNull(key) || Validator.isNull(file)) {
            return false;
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key.getEncoded());
        return FileUtil.writeBytesToFile(Base64.encode(keySpec.getEncoded(), Base64.NO_WRAP), file);
    }

    public static boolean saveToFile(PrivateKey key, File file) {
        if (Validator.isNull(key) || Validator.isNull(file)) {
            return false;
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key.getEncoded());
        return FileUtil.writeBytesToFile(Base64.encode(keySpec.getEncoded(), Base64.NO_WRAP), file);
    }
}
