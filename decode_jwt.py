import base64
import sys
import json

def base64url_decode(input_str):
    """Decodes a Base64Url encoded string, adding padding if necessary."""
    input_bytes = input_str.encode('utf-8')
    # Replace URL-safe characters, then add padding.
    # The standard library's urlsafe_b64decode can handle the replacements,
    # but requires correct padding.
    rem = len(input_bytes) % 4
    if rem > 0:
        input_bytes += b'=' * (4 - rem)
    
    return base64.urlsafe_b64decode(input_bytes).decode('utf-8')

def main():
    """Main function to decode and print JWT parts."""
    if len(sys.argv) != 2:
        print("Usage: python3 decode_jwt.py <jwt_string>")
        sys.exit(1)

    jwt_string = sys.argv[1]
    try:
        header_encoded, payload_encoded, signature_encoded = jwt_string.split('.')
    except ValueError:
        print("Error: Invalid JWT format. Expected three parts separated by dots.")
        sys.exit(1)

    print("--- JWT Decoded ---")

    try:
        # Decode Header
        header_decoded = base64url_decode(header_encoded)
        header_json = json.loads(header_decoded)
        print("\n[Header]")
        print(json.dumps(header_json, indent=2))

        # Decode Payload
        payload_decoded = base64url_decode(payload_encoded)
        payload_json = json.loads(payload_decoded)
        print("\n[Payload]")
        print(json.dumps(payload_json, indent=2))

    except Exception as e:
        print(f"\nError decoding JWT part: {e}")
        print("Please ensure the JWT string is correct.")
        sys.exit(1)

    print("\n[Signature (Encoded)]")
    print(signature_encoded)
    print("\nNote: The signature is a cryptographic hash and is not decoded.")
    print("--- End ---")


if __name__ == "__main__":
    main()
