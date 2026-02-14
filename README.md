UPI Payment Library:

---

📱 UPI Payment Integration for Android

https://jitpack.io/v/AdamNub/UPI-PAYMENT-INTEGRATION-IN-ANDROID.svg

A simple Android library to integrate UPI payments in your app. Supports all UPI apps like Google Pay, PhonePe, Paytm, BHIM, etc.

✨ Features

· ✅ Check installed UPI apps
· ✅ Make UPI payments easily
· ✅ Handle payment results (Success/Failure/Cancelled)
· ✅ Support for all UPI apps
· ✅ Simple and lightweight

📦 Installation

Step 1. Add JitPack repository to your settings.gradle

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency to your app/build.gradle

```gradle
dependencies {
    implementation 'com.github.AdamNub:UPI-PAYMENT-INTEGRATION-IN-ANDROID:1.0.0'
}
```

🚀 How to Use

Create a UPI Payment

```kotlin
val upiPayment = UpiPayment(
    payeeVpa = "recipient@okhdfcbank",    // Payee UPI ID
    payeeName = "Recipient Name",          // Payee name
    amount = "10.00",                       // Amount
    transactionNote = "Test Payment",       // Description
    transactionRef = "TXN" + System.currentTimeMillis()  // Unique ID
)
```

Check and Launch UPI Apps

```kotlin
// Check if any UPI apps are installed
val upiApps = UpiPaymentHelper.getInstalledUpiApps(context)

if (upiApps.isNotEmpty()) {
    // Launch payment
    UpiPaymentHelper.startPaymentForResult(activity, upiPayment, REQUEST_CODE)
} else {
    Toast.makeText(context, "No UPI apps installed", Toast.LENGTH_SHORT).show()
}
```

Handle Payment Result

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == REQUEST_CODE) {
        val result = UpiPaymentHelper.getPaymentResult(data)
        
        when {
            result.isSuccess -> {
                // Payment successful
                val txnId = result.transactionId
                showSuccess("Payment Successful!")
            }
            result.isCancelled -> {
                // User cancelled
                showMessage("Payment cancelled")
            }
            else -> {
                // Payment failed
                showError("Payment failed: ${result.errorMessage}")
            }
        }
    }
}
```

Test UPI IDs

UPI ID Purpose
success@hdfcbank Always returns success (for testing)
fail@hdfcbank Always returns failure (for testing)
recipient@okhdfcbank Your actual UPI ID

📋 Requirements

· Minimum SDK: 21 (Android 5.0)
· Compile SDK: 33
· Java 11

📄 License

```text
MIT License

Copyright (c) 2024 AdamNub

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first.

📧 Contact

· GitHub: @AdamNub
· Project Link: https://github.com/AdamNub/UPI-PAYMENT-INTEGRATION-IN-ANDROID

---

⭐ Star this repo if you find it helpful!

---
