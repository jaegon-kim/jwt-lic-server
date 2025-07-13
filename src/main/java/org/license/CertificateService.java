package org.license;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);

    @Value("${generated.keystore.path}")
    private String generatedKeystorePath;

    @Value("${generated.keystore.password}")
    private String generatedKeystorePassword;

    private final CaService caService;
    private KeyStore generatedCertKeyStore;

    public CertificateService(CaService caService) {
        this.caService = caService;
    }

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        loadGeneratedCertKeyStore();
    }

    private void loadGeneratedCertKeyStore() {
        try {
            generatedCertKeyStore = KeyStore.getInstance("JKS");
            Path keystorePath = Paths.get(generatedKeystorePath);

            if (Files.exists(keystorePath)) {
                try (FileInputStream fis = new FileInputStream(keystorePath.toFile())) {
                    generatedCertKeyStore.load(fis, generatedKeystorePassword.toCharArray());
                    log.info("Generated certificates KeyStore loaded successfully from: {}", generatedKeystorePath);
                }
            } else {
                generatedCertKeyStore.load(null, generatedKeystorePassword.toCharArray()); // Create new empty keystore
                log.info("New generated certificates KeyStore created at: {}", generatedKeystorePath);
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            log.error("Failed to load or create generated certificates KeyStore from {}: {}", generatedKeystorePath, e.getMessage());
            generatedCertKeyStore = null;
        }
    }

    private void saveGeneratedCertKeyStore() {
        try (FileOutputStream fos = new FileOutputStream(Paths.get(generatedKeystorePath).toFile())) {
            generatedCertKeyStore.store(fos, generatedKeystorePassword.toCharArray());
            log.info("Generated certificates KeyStore saved to: {}", generatedKeystorePath);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            log.error("Failed to save generated certificates KeyStore to {}: {}", generatedKeystorePath, e.getMessage());
        }
    }

    public CertificateInfo generateAndSaveCertificate(String commonName, long validityDays) throws Exception {
        if (!caService.isCaLoaded()) {
            throw new IllegalStateException("CA is not loaded. Cannot generate certificates.");
        }
        if (generatedCertKeyStore == null) {
            throw new IllegalStateException("Generated certificates KeyStore is not loaded or created.");
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(validityDays));

        X509Certificate newCert = caService.issueCertificate(commonName, notBefore, notAfter, keyPair.getPublic());

        // Store the new certificate and private key in the generated KeyStore
        Certificate[] chain = {newCert, caService.getCaCertificate()}; // Include CA cert in chain
        generatedCertKeyStore.setKeyEntry(commonName, keyPair.getPrivate(), generatedKeystorePassword.toCharArray(), chain);
        saveGeneratedCertKeyStore();

        log.info("Generated certificate for {} saved to KeyStore: {}", commonName, generatedKeystorePath);
        return new CertificateInfo(commonName, generatedKeystorePath, null, newCert.getNotAfter());
    }

    public List<CertificateInfo> getGeneratedCertificates() {
        List<CertificateInfo> certs = new ArrayList<>();
        if (generatedCertKeyStore == null) {
            log.warn("Generated certificates KeyStore is not loaded. Returning empty list.");
            return certs;
        }

        try {
            Enumeration<String> aliases = generatedCertKeyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (generatedCertKeyStore.isKeyEntry(alias)) {
                    X509Certificate cert = (X509Certificate) generatedCertKeyStore.getCertificate(alias);
                    certs.add(new CertificateInfo(alias, generatedKeystorePath, null, cert.getNotAfter()));
                }
            }
        } catch (KeyStoreException e) {
            log.error("Failed to list aliases from generated certificates KeyStore: {}", e.getMessage());
        }
        return certs;
    }

    public void deleteCertificate(String commonName) throws KeyStoreException {
        if (generatedCertKeyStore == null) {
            throw new IllegalStateException("Generated certificates KeyStore is not loaded.");
        }
        if (!generatedCertKeyStore.containsAlias(commonName)) {
            throw new IllegalArgumentException("Certificate with commonName " + commonName + " not found.");
        }

        generatedCertKeyStore.deleteEntry(commonName);
        saveGeneratedCertKeyStore();
        log.info("Certificate with commonName {} deleted from KeyStore.", commonName);
    }

    public static class CertificateInfo {
        private String commonName;
        private String keystorePath;
        private String keyPath; // Kept for compatibility, will be null
        private Date notAfter;

        public CertificateInfo(String commonName, String keystorePath, String keyPath, Date notAfter) {
            this.commonName = commonName;
            this.keystorePath = keystorePath;
            this.keyPath = keyPath;
            this.notAfter = notAfter;
        }

        public String getCommonName() {
            return commonName;
        }

        public String getKeystorePath() {
            return keystorePath;
        }

        public String getKeyPath() {
            return keyPath;
        }

        public Date getNotAfter() {
            return notAfter;
        }
    }
}
