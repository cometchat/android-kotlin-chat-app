
<p align="center">
  <img alt="CometChat" src="https://assets.cometchat.io/website/images/logos/banner.png">
</p>

# Android Sample App by CometChat

This is a reference application showcasing the integration of [CometChat's Android UI Kit](https://www.cometchat.com/docs/ui-kit/android/5.0/overview) in a native Android application using Kotlin. It demonstrates how to implement real-time messaging and voice/video calling features with ease.

<div style="display: flex; align-items: center; justify-content: center">
   <img src="../screenshots/overview_cometchat_screens.png" />
</div>

## Prerequisites

- Ensure you have the following installed:
    - Android Studio (latest stable version)
    - Java Development Kit (JDK 8 or later)

- Sign up for a [CometChat](https://app.cometchat.com/) account to obtain your app credentials: _`App ID`_, _`Region`_, and _`Auth Key`_

## Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/cometchat/cometchat-uikit-android.git
   ```

2. Checkout v5 branch:
   ```sh
   git checkout v5
   ```

3. Open the project in Android Studio by navigating to the cloned directory and open the `sample-app-kotlin+push-notification` folder.

4. Sync Gradle to ensure all dependencies are downloaded.

5. `[Optional]` Enter your CometChat _`App ID`_, _`Region`_, and _`Auth Key`_ in the [AppCredentials.java](src/main/java/com/cometchat/sampleapp/kotlin/fcm/AppCredentials.kt) file:
   file:https://github.com/cometchat/cometchat-sample-app-android-kotlin/blob/b7b7c0d76eb70960728e6622ed7f70ab4e45b4af/sample-app-kotlin/src/main/java/com/cometchat/sampleapp/kotlin/fcm/AppCredentials.kt#L3-L11

6. Set up Firebase Cloud Messaging (FCM):
    - Go to the [Firebase Console](https://console.firebase.google.com/) and create a project.
    - Add your Android app to the Firebase project and download the `google-services.json` file.
    - Place the `google-services.json` file in the `sample-app-kotlin+push-notification/` directory of your project.

7. Set up CometChat Push Notification:
   - Go to the [Notification Documentation](https://www.cometchat.com/docs/notifications/push-integration) and follow integration steps.

8. Run the project Use an emulator or a physical device to build and run the sample app.

## Help and Support

For issues running the project or integrating with our UI Kits, consult our [documentation](https://www.cometchat.com/docs/ui-kit/android/5.0/getting-started) or create a [support ticket](https://help.cometchat.com/hc/en-us). You can also access real-time support via the [CometChat Dashboard](http://app.cometchat.com/).