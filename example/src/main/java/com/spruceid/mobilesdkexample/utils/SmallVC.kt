package com.spruceid.mobilesdkexample.utils

val small_vc = """
{
  "@context": [
    "https://www.w3.org/ns/credentials/v2",
    "https://www.w3.org/ns/credentials/examples/v2"
  ],
  "id": "urn:uuid:58172aac-d8ba-11ed-83dd-0b3aef56cc33",
  "type": [
    "VerifiableCredential",
    "AlumniCredential"
  ],
  "name": "Alumni Credential",
  "description": "A minimum viable example of an Alumni Credential.",
  "issuer": "https://vc.example/issuers/5678",
  "validFrom": "2023-01-01T00:00:00Z",
  "credentialSubject": {
    "id": "did:example:abcdefgh",
    "alumniOf": "The School of Examples"
  },
  "proof": {
    "type": "DataIntegrityProof",
    "cryptosuite": "ecdsa-rdfc-2019",
    "created": "2023-02-24T23:36:38Z",
    "verificationMethod": "https://vc.example/issuers/5678#zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP",
    "proofPurpose": "assertionMethod",
    "proofValue": "z4KKHqaD4F7GHyLA6f3wK9Ehxtogv5jQRFpQBM4sXkSf7Bozd7bAf7dF6UkfM2aSCBMm24mPvaFXmzQmimzaEC3SL"
  }
}
"""