package org.license;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateCertificate(@RequestParam String commonName, @RequestParam(defaultValue = "365") long validityDays) {
        try {
            CertificateService.CertificateInfo certInfo = certificateService.generateAndSaveCertificate(commonName, validityDays);
            return ResponseEntity.ok(certInfo);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating certificate: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CertificateService.CertificateInfo>> getCertificates() {
        List<CertificateService.CertificateInfo> certs = certificateService.getGeneratedCertificates();
        return ResponseEntity.ok(certs);
    }

    @DeleteMapping("/{commonName}")
    public ResponseEntity<?> deleteCertificate(@PathVariable String commonName) {
        try {
            certificateService.deleteCertificate(commonName);
            return ResponseEntity.ok().body("Certificate " + commonName + " deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting certificate: " + e.getMessage());
        }
    }
}
