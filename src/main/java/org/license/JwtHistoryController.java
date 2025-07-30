package org.license;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/history")
public class JwtHistoryController {

    private final JwtSignHistoryRepository jwtSignHistoryRepository;

    @Value("${jwt.history.max-entries:100}")
    private int maxEntries;

    public JwtHistoryController(JwtSignHistoryRepository jwtSignHistoryRepository) {
        this.jwtSignHistoryRepository = jwtSignHistoryRepository;
    }

    // New API for JwtSignHistory
    @GetMapping
    public ResponseEntity<List<JwtSignHistory>> getAllSignHistory() {
        List<JwtSignHistory> history = jwtSignHistoryRepository.findAll();
        return ResponseEntity.ok(history);
    }

    @PostMapping
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSignHistory(@PathVariable Long id) {
        if (jwtSignHistoryRepository.existsById(id)) {
            jwtSignHistoryRepository.deleteById(id);
            return ResponseEntity.ok().body("Sign history with ID " + id + " deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sign history with ID " + id + " not found.");
        }
    }
}
