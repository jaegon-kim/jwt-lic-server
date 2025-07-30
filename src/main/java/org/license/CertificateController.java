package org.license;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;
import java.security.cert.X509Certificate;
import java.io.StringWriter;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.util.io.pem.PemObject;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final JwtSigningService jwtSigningService;
    private final CaService caService;

    public CertificateController(JwtSigningService jwtSigningService, CaService caService) {
        this.jwtSigningService = jwtSigningService;
        this.caService = caService;
    }

    @PostMapping("/generate")
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

    @GetMapping
    public ResponseEntity<List<JwtSigningService.CertificateInfo>> getCertificates() {
        List<JwtSigningService.CertificateInfo> certs = jwtSigningService.getGeneratedCertificates();
        return ResponseEntity.ok(certs);
    }

    @DeleteMapping("/{commonName}")
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

    @GetMapping("/ca-certificate/pem")
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

    @GetMapping("/{commonName}/pem")
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
}
