## Generating License Server CA crt/key
```
keytool -genkeypair -alias lic.ca -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=License CA" -validity 36500 -keystore ca.jks -storepass changeit -keypass changeit -ext san=dns:localhost,ip:127.0.0.1
```

## Generating a new certificate & key
curl -X POST "http://localhost:18080/certificates/generate?commonName=my-customer&validityDays=365"

## Listing generated certificates
curl http://localhost:18080/certificates

## Delete 
curl -X DELETE http://localhost:18080/certificates/cert-to-delete