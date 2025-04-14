# 🛒 Kotlin eCommerce App (Latest Kotlin Compatible)

This is a fully working eCommerce app built using the **latest version of Kotlin**. I created this project because I couldn’t find any working examples online or on YouTube that were up-to-date or compatible with the current Kotlin and Android versions. So here’s one that just works.

> ![Preview1](images/appshow1.png)
> ![Preview2](images/appshow2.png)

## 🚀 Getting Started

Follow these steps to run the app on your system:

### 1. Install Android Studio

If you don’t already have Android Studio installed, download it from here:  
👉 [https://developer.android.com/studio](https://developer.android.com/studio)

Once installed, open Android Studio and **create a new project**. Name it anything you want.  
> (I named mine `myecommerceapp2` – don’t forget to change the project name and package in the `AndroidManifest.xml` and other necessary files.)

### 2. Set Up Firebase

You’ll need to connect your app to **Firebase Realtime Database** and **Firebase Authentication**.

> I'm not explaining the full Firebase setup here, but you can find many tutorials online for that.

Just make sure Firebase is properly connected **before** running the app.

### 3. Wait for Gradle to Sync

Let Android Studio finish syncing and setting up the project. Wait until the Gradle build is done before doing anything else.

### 4. Set Up a Virtual Device

To run the app, add a virtual device.  
I recommend using **Pixel 8a** (or any recent emulator).  

### 5. Replace Default Files

Navigate to:  
`app > java > com.example.yourprojectname`

Then, **copy-paste all files** from the `app` folder in this GitHub repo into your project’s `com.example.yourprojectname` folder.

> ![Home Screen](images/app.png)

### 6. Copy Resource Folders

Next, replace your project’s resource folders with mine.

Copy-paste these from my GitHub repo to your project’s `res` folder:

- `anim`
- `drawable`
- `layout`
- `mipmap`
- `raw`
- `values`
- `xml`

> ![Folders](images/folders.png)

### 7. Update Gradle Files

Use the versions from my project – they’re stable and don’t use any unnecessary libraries. This makes the app more efficient.

Here’s what to do:

- Replace the contents of your **Project-level `build.gradle`** and **App-level `build.gradle`** with the ones in this repo.
- Make sure to sync Gradle after updating.

### 8. Run the App

If everything is set up correctly and Firebase is connected, **run the app on your virtual device**.

Make sure you've created a user via Firebase Authentication, otherwise login/signup features won’t work.

### 9. Add Product Data

To see product data in the app:

- Go to Firebase Realtime Database
- Paste the contents of `products.json` (from this repo) into your Firebase database

That’s it!

---



- The app is designed to work with the latest Kotlin and Android versions.
- Minimal and clean structure – you can build more on top of it!
- 
---
