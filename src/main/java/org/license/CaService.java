package org.license;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class CaService {

    private static final Logger log = LoggerFactory.getLogger(CaService.class);

    @Value("${ca.keystore.path}")
    private String caKeystorePath;

    @Value("${ca.keystore.password}")
    private String caKeystorePassword;

    @Value("${ca.keystore.alias}")
    private String caKeystoreAlias;

    private KeyStore caKeyStore;
    private X509Certificate caCertificate;
    private PrivateKey caPrivateKey;

    @PostConstruct
    public void init() {
        loadCaKeyStore();
        watchCaKeyStoreFile();
    }

    private void loadCaKeyStore() {
        try (FileInputStream fis = new FileInputStream(caKeystorePath)) {
            caKeyStore = KeyStore.getInstance("JKS");
            caKeyStore.load(fis, caKeystorePassword.toCharArray());

            caCertificate = (X509Certificate) caKeyStore.getCertificate(caKeystoreAlias);
            caPrivateKey = (PrivateKey) caKeyStore.getKey(caKeystoreAlias, caKeystorePassword.toCharArray());

            if (caCertificate == null || caPrivateKey == null) {
                throw new KeyStoreException("CA certificate or private key not found in keystore.");
            }
            log.info("CA KeyStore loaded successfully from: {}", caKeystorePath);
            log.info("CA Certificate Subject: {}", caCertificate.getSubjectX500Principal().getName());
            log.info("CA Certificate Not After: {}", caCertificate.getNotAfter());

        } catch (Exception e) {
            log.error("Failed to load CA KeyStore from {}: {}", caKeystorePath, e.getMessage());
            caKeyStore = null;
            caCertificate = null;
            caPrivateKey = null;
        }
    }

    private void watchCaKeyStoreFile() {
        try {
            Path path = Paths.get(caKeystorePath).getParent();
            if (path == null) {
                path = Paths.get("."); // Current directory if no parent
            }
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {
                try {
                    WatchKey key;
                    while ((key = watchService.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.context().toString().equals(Paths.get(caKeystorePath).getFileName().toString())) {
                                log.info("CA KeyStore file modified. Attempting to reload...");
                                int maxRetries = 5;
                                long retryDelayMs = 500; // 0.5 seconds
                                for (int i = 0; i < maxRetries; i++) {
                                    try {
                                        TimeUnit.MILLISECONDS.sleep(retryDelayMs); // Wait before retrying
                                        loadCaKeyStore();
                                        if (isCaLoaded()) {
                                            log.info("CA KeyStore reloaded successfully after {} retries.", i + 1);
                                            break; // Success, exit retry loop
                                        }
                                    } catch (Exception e) {
                                        log.warn("Attempt {} to reload CA KeyStore failed: {}", i + 1, e.getMessage());
                                    }
                                    if (i == maxRetries - 1 && !isCaLoaded()) {
                                        log.error("Failed to reload CA KeyStore after {} attempts.", maxRetries);
                                    }
                                }
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException | ClosedWatchServiceException e) {
                    log.info("CA KeyStore file watcher stopped.");
                }
            }).start();
            log.info("Watching CA KeyStore file for changes: {}", caKeystorePath);
        } catch (IOException e) {
            log.error("Failed to set up CA KeyStore file watcher: {}", e.getMessage());
        }
    }

    public X509Certificate getCaCertificate() {
        return caCertificate;
    }

    public PrivateKey getCaPrivateKey() {
        return caPrivateKey;
    }

    public boolean isCaLoaded() {
        return caCertificate != null && caPrivateKey != null;
    }

    public X509Certificate issueCertificate(String commonName, Date notBefore, Date notAfter, PublicKey publicKey) throws Exception {
        if (!isCaLoaded()) {
            throw new IllegalStateException("CA is not loaded. Cannot issue certificates.");
        }

        X500Name issuer = new X500Name(caCertificate.getSubjectX500Principal().getName());
        X500Name subject = new X500Name("CN=" + commonName);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                publicKeyInfo
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(caPrivateKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
    }
}
