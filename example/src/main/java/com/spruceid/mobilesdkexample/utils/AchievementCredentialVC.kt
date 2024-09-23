package com.spruceid.mobilesdkexample.utils

val mockAchievementCredential = """
{
  "@context": [
    "https://www.w3.org/ns/credentials/v2",
    "https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.3.json"
  ],
  "achievement": {
    "name": "Team Membership",
    "type": "Achievement"
  },
  "credentialSubject": {
    "identity": [
      {
            "hashed": false,
            "identityHash": "John Smith",
            "identityType": "name",
            "salt": "not-used",
            "type": "IdentityObject"
      },
      {
            "hashed": false,
            "identityHash": "john.smith@example.com",
            "identityType": "emailAddress",
            "salt": "not-used",
            "type": "IdentityObject"
      }
    ]
  },
  "issuer": {
    "id": "did:jwk:eyJhbGciOiJFUzI1NiIsImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoibWJUM2dqOWFvOGNuS280M0prcVRPUmNJQVI4MFgwTUFXQWNGYzZvR1JMYyIsInkiOiJiOFVOY0hDMmFHQ3J1STZ0QlRWSVY0dW5ZWEVyS0M4ZDRnRTFGZ0s0Q05JIn0#0",
    "name": "Development Council",
    "type": "Profile"
  },
  "name": "TeamMembership",
  "type": [
    "VerifiableCredential",
    "OpenBadgeCredential"
  ]
}
"""