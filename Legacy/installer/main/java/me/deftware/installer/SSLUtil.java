package me.deftware.installer;

import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Credits to <a href="https://github.com/MinecraftForge/MinecraftForge/commit/d9c4446ccc0931ba1fdbbc5156c60740b97dea6e">Forge</a>
 */
public class SSLUtil {

    private final static String JKS_FILE = "/lekeystore.jks";
    private final static String PASSWORD = "supersecretpassword";


    private static Map<String, Certificate> getCertificate() throws Exception {
        try (InputStream jks = SSLUtil.class.getResourceAsStream(JKS_FILE)) {
            if (jks == null) {
                throw new NullPointerException("Could not find let's encrypt certificate");
            }
            System.out.println("Loading let's encrypt certificates...");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(jks, PASSWORD.toCharArray());
            return getAliases(keyStore);
        }
    }

    private static Map<String, Certificate> getSystemCertificates() throws Exception {
        System.out.println("Loading system certificates...");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        Path ksPath = Paths.get(System.getProperty("java.home"),"lib", "security", "cacerts");
        keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
        return getAliases(keyStore);
    }

    public static void init() throws Exception {
        System.out.println("Expanding trusted SSL certificates...");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, new char[0]);
        for (Map.Entry<String, Certificate> entry : getSystemCertificates().entrySet()) {
            keyStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Certificate> entry : getCertificate().entrySet()) {
            keyStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        // Create trust manager and load certificates
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        SSLContext tls = SSLContext.getInstance("TLS");
        tls.init(null, factory.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
        System.out.println("Let's encrypt certificates successfully loaded");
    }

    private static Map<String, Certificate> getAliases(KeyStore keyStore) throws Exception {
        Map<String, Certificate> map = new HashMap<>();
        for (String alias : Collections.list(keyStore.aliases())) {
            map.put(alias, keyStore.getCertificate(alias));
        }
        return map;
    }

}
