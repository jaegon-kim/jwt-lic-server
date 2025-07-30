package org.license;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jwt")
public class JwtSigningController {

    private final JwtSigningService jwtSigningService;
    private final JwtSignHistoryRepository jwtSignHistoryRepository;

    public JwtSigningController(JwtSigningService jwtSigningService, JwtSignHistoryRepository jwtSignHistoryRepository) {
        this.jwtSigningService = jwtSigningService;
        this.jwtSignHistoryRepository = jwtSignHistoryRepository;
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signJwt(@RequestBody JwtSignRequest request) {
        JwtSignHistory history = new JwtSignHistory();
        history.setTimestamp(java.time.LocalDateTime.now());
        try {
            String signedJwt = jwtSigningService.signJwt(request.getCommonName(), request.getClaims());
            history.setSuccess(true);
            history.setSignedJwtResult(signedJwt);
            jwtSignHistoryRepository.save(history);
            return ResponseEntity.ok(signedJwt);
        } catch (IllegalArgumentException e) {
            history.setSuccess(false);
            history.setFailureReason(e.getMessage());
            jwtSignHistoryRepository.save(history);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            history.setSuccess(false);
            history.setFailureReason(e.getMessage());
            jwtSignHistoryRepository.save(history);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            history.setSuccess(false);
            history.setFailureReason(e.getMessage());
            jwtSignHistoryRepository.save(history);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error signing JWT: " + e.getMessage());
        }
    }
}
