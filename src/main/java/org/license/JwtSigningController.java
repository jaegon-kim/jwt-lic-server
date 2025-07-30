package org.license;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.security.cert.X509Certificate;
import java.io.StringWriter;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.util.io.pem.PemObject;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping
public class JwtSigningController {

    private final JwtSigningService jwtSigningService;
    private final CaService caService;
    private final JwtSignHistoryRepository jwtSignHistoryRepository;

    @Value("${jwt.history.max-entries:100}")
    private int maxEntries;

    public JwtSigningController(JwtSigningService jwtSigningService, CaService caService, JwtSignHistoryRepository jwtSignHistoryRepository) {
        this.jwtSigningService = jwtSigningService;
        this.caService = caService;
        this.jwtSignHistoryRepository = jwtSignHistoryRepository;
    }

    @PostMapping("/certificates/generate")
    public ResponseEntity<?> generateCertificate(@RequestParam String commonName, @RequestParam(defaultValue = "365") long validityDays) {
        try {
            JwtSigningService.CertificateInfo certInfo = jwtSigningService.generateAndSaveCertificate(commonName, validityDays);
            return ResponseEntity.ok(certInfo);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating certificate: " + e.getMessage());
        }
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<JwtSigningService.CertificateInfo>> getCertificates() {
        List<JwtSigningService.CertificateInfo> certs = jwtSigningService.getGeneratedCertificates();
        return ResponseEntity.ok(certs);
    }

    @DeleteMapping("/certificates/{commonName}")
    public ResponseEntity<?> deleteCertificate(@PathVariable String commonName) {
        try {
            jwtSigningService.deleteCertificate(commonName);
            return ResponseEntity.ok().body("Certificate " + commonName + " deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting certificate: " + e.getMessage());
        }
    }

    @PostMapping("/certificates/sign-jwt")
    public ResponseEntity<?> signJwt(@RequestBody JwtSignRequest request) {
        try {
            String signedJwt = jwtSigningService.signJwt(request.getCommonName(), request.getClaims());
            return ResponseEntity.ok(signedJwt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error signing JWT: " + e.getMessage());
        }
    }

    @GetMapping("/certificates/ca-certificate/pem")
    public ResponseEntity<?> getCaCertificatePem() {
        try {
            X509Certificate caCert = caService.getCaCertificate();
            if (caCert == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CA certificate not loaded.");
            }

            StringWriter sw = new StringWriter();
            try (PemWriter pw = new PemWriter(sw)) {
                pw.writeObject(new PemObject("CERTIFICATE", caCert.getEncoded()));
            }
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(sw.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving CA certificate: " + e.getMessage());
        }
    }

    @GetMapping("/certificates/{commonName}/pem")
    public ResponseEntity<?> getCertificatePem(@PathVariable String commonName) {
        try {
            X509Certificate cert = jwtSigningService.getCertificateByCommonName(commonName);
            StringWriter sw = new StringWriter();
            try (PemWriter pw = new PemWriter(sw)) {
                pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
            }
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(sw.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving certificate: " + e.getMessage());
        }
    }

    // New API for JwtSignHistory
    @GetMapping("/history")
    public ResponseEntity<List<JwtSignHistory>> getAllSignHistory() {
        List<JwtSignHistory> history = jwtSignHistoryRepository.findAll();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/history")
    public ResponseEntity<JwtSignHistory> addSignHistory(@RequestBody JwtSignHistory history) {
        // Check if the number of entries exceeds the maximum
        long currentCount = jwtSignHistoryRepository.count();
        if (currentCount >= maxEntries) {
            // Find and delete the oldest entry
            jwtSignHistoryRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"))
                                    .stream()
                                    .findFirst()
                                    .ifPresent(oldest -> jwtSignHistoryRepository.delete(oldest));
        }

        history.setTimestamp(LocalDateTime.now()); // Set timestamp on creation
        JwtSignHistory savedHistory = jwtSignHistoryRepository.save(history);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteSignHistory(@PathVariable Long id) {
        if (jwtSignHistoryRepository.existsById(id)) {
            jwtSignHistoryRepository.deleteById(id);
            return ResponseEntity.ok().body("Sign history with ID " + id + " deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sign history with ID " + id + " not found.");
        }
    }
}
