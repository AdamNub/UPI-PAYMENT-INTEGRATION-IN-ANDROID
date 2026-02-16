
📱 UPI PAYMENT INTEGRATION FOR ANDROID

<div align="center">

A simple and powerful Android library to integrate UPI payments in your app with just a few lines of code

Easily accept payments through Google Pay, PhonePe, Paytm, BHIM, Amazon Pay and all other UPI apps without any hassle

</div>

This library handles all the complexity of UPI payment integration including app detection, payment intent creation, and result handling. Perfect for developers who want to add UPI payment functionality to their Android apps quickly and reliably.

---

📦 INSTALLATION

Step 1: Add JitPack Repository

📁 settings.gradle

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

Step 2: Add Dependency

📁 app/build.gradle

```gradle
dependencies
    'com.github.AdamNub:Adamz-UPi:1.0.7 
```

Step 3: Sync Project

Click "Sync Now" in Android Studio or run:

```bash
./gradlew build
```

---

📖 USAGE GUIDE

1️⃣ CREATE UPI PAYMENT

First, create a UPI payment object with the required details:

```kotlin
import com.adamsnub.upilib.models.UpiPayment
import com.adamsnub.upilib.helpers.UpiPaymentHelper

// Create payment details
val upiPayment = UpiPayment(
    payeeVpa = "recipient@okhdfcbank",        // Payee UPI ID (required)
    payeeName = "John Doe",                   // Payee name (required)
    amount = "99.50",                          // Amount to pay (required)
    transactionNote = "Payment for Order #123", // Description (optional)
    transactionRef = "TXN_${System.currentTimeMillis()}"  // Unique ID (optional)
)
```

📝 Instructions:

· payeeVpa: The UPI ID of the recipient (e.g., name@okhdfcbank, phone@paytm)
· payeeName: Name of the recipient that will be displayed
· amount: String value, can be in format "99" or "99.50"
· transactionNote: Brief description of the payment
· transactionRef: Unique transaction ID for tracking

---

2️⃣ CHECK INSTALLED UPI APPS

Check if the user has any UPI apps installed on their device:

```kotlin
// Get list of all installed UPI apps
val installedUpiApps = UpiPaymentHelper.getInstalledUpiApps(context)

if (installedUpiApps.isNotEmpty()) {
    // Show list of available UPI apps
    for (app in installedUpiApps) {
        println("App: ${app.appName}, Package: ${app.packageName}")
    }
} else {
    // No UPI apps found
    Toast.makeText(context, "No UPI apps installed", Toast.LENGTH_LONG).show()
}

// Check if specific UPI app is installed
val isGooglePayInstalled = UpiPaymentHelper.isAppInstalled(
    context, 
    "com.google.android.apps.nbu.paisa.user"
)

val isPhonePeInstalled = UpiPaymentHelper.isAppInstalled(
    context, 
    "com.phonepe.app"
)

val isPaytmInstalled = UpiPaymentHelper.isAppInstalled(
    context, 
    "net.one97.paytm"
)
```

📝 Instructions:

· Always check for UPI apps before initiating payment
· Show appropriate message if no UPI apps are found
· You can guide users to install a UPI app if none are available

---

3️⃣ LAUNCH UPI PAYMENT

Launch the payment with UPI app chooser:

```kotlin
// Method 1: Launch with default chooser
UpiPaymentHelper.startPaymentForResult(
    activity = this,              // Your activity
    upiPayment = upiPayment,      // Payment details
    requestCode = 1001            // Request code for result
)

// Method 2: Launch specific UPI app
UpiPaymentHelper.startPaymentWithApp(
    activity = this,
    upiPayment = upiPayment,
    packageName = "com.google.android.apps.nbu.paisa.user", // Google Pay
    requestCode = 1001
)

// Method 3: Get payment intent and launch manually
val intent = UpiPaymentHelper.getUpiPaymentIntent(upiPayment)
if (intent != null) {
    startActivityForResult(intent, 1001)
}
```

📝 Instructions:

· Use startPaymentForResult to let users choose their preferred UPI app
· Use startPaymentWithApp to open a specific UPI app directly
· Request code should be unique to identify this payment in onActivityResult

---

4️⃣ HANDLE PAYMENT RESULT

Process the payment result in onActivityResult:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == 1001) {
        val result = UpiPaymentHelper.getPaymentResult(data)
        
        // Check payment status
        when {
            result.isSuccess -> {
                // Payment successful
                val transactionId = result.transactionId
                val responseCode = result.responseCode
                val approvalRef = result.approvalRefNo
                
                showSuccess("Payment Successful! TXN ID: $transactionId")
                
                // Update UI, send receipt, etc.
                updateOrderStatus("PAID")
                sendReceipt(transactionId)
            }
            
            result.isCancelled -> {
                // User cancelled the payment
                showMessage("Payment cancelled by user")
                
                // Ask user to try again
                promptRetry()
            }
            
            else -> {
                // Payment failed
                val errorMessage = result.errorMessage ?: "Payment failed"
                showError("Payment Failed: $errorMessage")
                
                // Log error for debugging
                Log.e("UPI_ERROR", errorMessage)
            }
        }
        
        // Get raw response for debugging
        val response = result.response
        val status = result.status
    }
}

// Helper methods for UI
private fun showSuccess(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    // Show success dialog
}

private fun showError(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    // Show error dialog with retry option
}

private fun showMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

📝 Instructions:

· Always handle all three cases: success, cancelled, and failure
· Save transaction ID for your records when payment succeeds
· Provide retry option when payment fails or is cancelled
· Log errors for debugging purposes

---

🧪 TESTING

Test UPI IDs

UPI ID Purpose Behavior
success@hdfcbank Testing success Always returns success response
fail@hdfcbank Testing failure Always returns failure response
yourname@okhdfcbank Real payments Your actual UPI ID

Test Implementation

```kotlin
// For testing success flow
val testSuccessPayment = UpiPayment(
    payeeVpa = "success@hdfcbank",
    payeeName = "Test Merchant",
    amount = "1.00",
    transactionNote = "Test Transaction",
    transactionRef = "TEST_SUCCESS"
)

// For testing failure flow
val testFailurePayment = UpiPayment(
    payeeVpa = "fail@hdfcbank",
    payeeName = "Test Merchant",
    amount = "1.00",
    transactionNote = "Test Transaction",
    transactionRef = "TEST_FAIL"
)
```

---

📋 API REFERENCE

UpiPayment Class

Property Type Required Description
payeeVpa String Yes Recipient's UPI ID
payeeName String Yes Recipient's name
amount String Yes Payment amount
transactionNote String No Payment description
transactionRef String No Unique transaction ID
currency String No Currency (default: INR)

UpiPaymentHelper Methods

Method Description
getInstalledUpiApps(context) Returns list of installed UPI apps
isAppInstalled(context, packageName) Checks if specific app is installed
startPaymentForResult(activity, upiPayment, requestCode) Launches UPI app chooser
startPaymentWithApp(activity, upiPayment, packageName, requestCode) Opens specific UPI app
getUpiPaymentIntent(upiPayment) Returns intent for manual launch
getPaymentResult(data) Parses payment result from intent

UpiPaymentResult Properties

Property Type Description
isSuccess Boolean True if payment succeeded
isCancelled Boolean True if user cancelled
transactionId String? Transaction ID on success
responseCode String? Response code from UPI app
approvalRefNo String? Approval reference number
errorMessage String? Error message on failure
status String Raw status string
response String Complete response

---

⚙️ REQUIREMENTS

Requirement Version
Minimum SDK 21 (Android 5.0)
Target SDK 33
Compile SDK 33
Java Version 11
AndroidX Required

---

🔧 TROUBLESHOOTING

Common Issues

Q: No UPI apps found on device

· Solution: Guide user to install any UPI app from Play Store
· Show install buttons for popular UPI apps

Q: Payment result not received

· Solution: Implement onActivityResult correctly
· Check request code matches
· Verify intent data is not null

Q: Transaction failed

· Solution: Check error message in result
· Verify UPI ID is correct
· Ensure sufficient balance

Q: App crashes on launch

· Solution: Check minimum SDK version
· Verify dependencies are added correctly
· Clear and rebuild project

---

📄 LICENSE

```
MIT License

Copyright (c) 2024 AdamNub

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

🤝 CONTRIBUTING

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (git checkout -b feature/AmazingFeature)
3. Commit your changes (git commit -m 'Add some AmazingFeature')
4. Push to the branch (git push origin feature/AmazingFeature)
5. Open a Pull Request

Reporting Issues

· Use the GitHub issues section
· Include device model and Android version
· Provide error logs if available
· Describe steps to reproduce

---

📞 SUPPORT

· GitHub Issues: Report a bug
· Project Link: https://github.com/AdamNub/UPI-PAYMENT-INTEGRATION-IN-ANDROID
· Author: @AdamNub

---

<div align="center">

⭐ Found this helpful? Star the repo! ⭐
