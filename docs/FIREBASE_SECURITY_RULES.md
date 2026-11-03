# Cloud Firestore Security Rules & Data Model

This document describes the Cloud Firestore data model and security rules required by **Kaloriq (`CalorieTrackingApp`)**.

## Data Ownership & Collections

- **Primary Data Store**: Google Cloud Firestore.
- **User Profiles**: Stored under `users/{uid}`.
- **Meal Entries**: Stored under the subcollection `users/{uid}/meal_entries/{mealId}`.
- **Daily Activity Stats**: Water intake, steps, and exercise calories are stored under `users/{uid}/daily_stats/{date}`.
- **Food Catalog**: Predefined food items are stored under `foods/{foodId}` and seeded once per session if needed.
- **Diagnostics**: Optional connectivity check document under `diagnostics/{docId}`.

## Firestore Security Rules

Copy and publish the following rules in **Firebase Console → Firestore Database → Rules**:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return signedIn() && request.auth.uid == userId;
    }

    // User profile and private subcollections — strictly accessible by the account owner
    match /users/{userId} {
      allow read, create, update, delete: if isOwner(userId);

      match /meal_entries/{mealId} {
        allow read, create, update, delete: if isOwner(userId);
      }

      match /daily_stats/{date} {
        allow read, create, update, delete: if isOwner(userId);
      }
    }

    // Global food catalog — readable and writable by authenticated users
    match /foods/{foodId} {
      allow read, write: if signedIn();
    }

    // Connectivity diagnostics — accessible by authenticated users
    match /diagnostics/{docId} {
      allow read, write: if signedIn();
    }
  }
}
```

## Verification Checklist

1. **User Isolation**: Authenticated User A can read/write only `users/{uid_A}` and its subcollections (`meal_entries`, `daily_stats`), and cannot access `users/{uid_B}`.
2. **Unauthenticated Access**: Unauthenticated requests to any collection are rejected.
3. **Catalog Access**: Authenticated users can read and seed `foods/{foodId}`.
