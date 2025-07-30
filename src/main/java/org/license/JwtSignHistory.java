package org.license;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class JwtSignHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private boolean success;

    @Column(length = 1000) // Adjust length as needed
    private String failureReason;

    @Column(length = 2000) // Adjust length as needed
    private String originalJwt;

    @Column(length = 2000) // Adjust length as needed
    private String signedJwtResult;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOriginalJwt() {
        return originalJwt;
    }

    public void setOriginalJwt(String originalJwt) {
        this.originalJwt = originalJwt;
    }

    public String getSignedJwtResult() {
        return signedJwtResult;
    }

    public void setSignedJwtResult(String signedJwtResult) {
        this.signedJwtResult = signedJwtResult;
    }
}
