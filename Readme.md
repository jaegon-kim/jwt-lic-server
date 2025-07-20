JWT license server
---

# API usages
## Generating License Server's root CA keypair (ca.jks)
```
keytool -genkeypair -alias lic.ca -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=License CA" -validity 36500 -keystore ca.jks -storepass changeit -keypass changeit -ext san=dns:localhost,ip:127.0.0.1
keytool -list -v -keystore ./ca.jks -storepass changeit
```

## Generating a new certificate & key
```
curl -X POST "http://localhost:18080/certificates/generate?commonName=test-jwt-cert&validityDays=365"
```

## Listing generated certificates
```
curl http://localhost:18080/certificates
```

## Delete
```
curl -X DELETE http://localhost:18080/certificates/test-jwt-cert
```

## Singaturing JWT with generated certificate
```
curl -X POST -H "Content-Type: application/json" \
     -d '{
       "commonName": "test-jwt-cert",
       "claims": {
         "sub": "license",
         "name": "Jey company",
         "apps": ["app1", "app2"],
         "expire" : "2025/12/31"
       }
     }' \
   http://localhost:18080/certificates/sign-jwt | xargs -0 python3 decode_jwt.py
```

Decoded Result 
```
--- JWT Decoded ---

[Header]
{
  "alg": "RS256"
}

[Payload]
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022
}

[Signature (Encoded)]
ZTRVPxGvX7vu7k63qagCRMbnnDx9obHFCBf3Wx8Z3PrcLuEU15W3gr_Dw80dAHYBLYx5YuOF-YzgvZ7UCf9QuFfz3DT2IQZJXLXIVfIIkQjhjy7l_m02rP21bXU-FvFFGsGUC9TNo9aW3epjEYeRbzPpKCdPdgDjQVxoZrJD8SoydDemoi-IJDRjiPEtu5cYqyK9gYi0uzjvInRGxifsLdARreB-wXYW8uHoh3RaTLE0B7cgq9eQe-90U0Gcbti-AS4DIyUyRcyO8XkhwqtfFVEWF_bBuUwt6ml5R4L3TkR13j_rP_Ll7QNQe_Rez4HKXyRLKPJ5aOq6oh6-doSKQw

Note: The signature is a cryptographic hash and is not decoded.
--- End ---
```


