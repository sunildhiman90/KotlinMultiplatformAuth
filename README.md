# KotlinMultiplatformAuth - A Kotlin Multiplatform Authentication Library

![Build](https://img.shields.io/github/actions/workflow/status/sunildhiman90/KotlinMultiplatformAuth/publish.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sunildhiman90/kmauth-google?color=blue)](https://central.sonatype.com/search?q=io.github.sunildhiman90+kmauth&smo=true)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

Kotlin Multiplatform Authentication library targeting Android, iOS, Desktop and Web(Kotlin/Js and Kotlin/Wasm Both).
Currently
supports Sign In with Google. Planning to add other providers in the future.

## Deepwiki Docs
[![Deepwiki Docs](https://img.shields.io/badge/DeepWiki-009485?style=for-the-badge&logo=readthedocs&logoColor=white)](https://deepwiki.com/sunildhiman90/KotlinMultiplatformAuth)

## Youtube Video
[How to Use Google Signin In Compose Multiplatform using KotlinMultiplatformAuth ](https://youtu.be/-5Ws4HSaYJc)

## Quick Start Sample Code

### KMAuthInitializer

First of all you need to initialize the KMAuthInitializer.

```kotlin

// For Android platform, Its better to call it from the activity, Becoz we need activity context.
KMAuthInitializer.initWithContext(
    webClientId = "YOUR_WEB_CLIENT_ID",
    kmAuthPlatformContext = KMAuthPlatformContext(this)
)

//For other platforms except android we can use this without context
KMAuthInitializer.init(webClientId = "YOUR_WEB_CLIENT_ID")

```

We need webClientId from Google Cloud Platform Console to setup the serverClientId in Google API for
identifying signed-in users in backend server.

### GoogleAuthManager

After initializing the KMAuthInitializer, you can use the KMAuthGoogle object to get the
GoogleAuthManager object to sign in the user.

```kotlin
Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

    //Ideally you will be using it from ViewModel or Repo
    val googleAuthManager = KMAuthGoogle.googleAuthManager

    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            //Without callback, Recommended way, available after 0.2.0
            val result = googleAuthManager.signIn()
            if (result.isSuccess) {
                println("Login Successful user: ${result.getOrNull()}")
                userName = result.getOrNull()?.name
            } else {
                println("Error in google Sign In: ${result.exceptionOrNull()}")
            }

            //With callback
            /*googleAuthManager.signIn { user, error ->
                if (error != null) {
                    println("Error in google Sign In: $error")
                }
                if (user != null) {
                    println("Login Successful user: $user")
                }
            }*/
        }
    }) {
        Text("Google Sign In")
    }
}
```

You can get the googleAuthManager from the KMAuthGoogle object and use it to sign in the user.

You can find the sample code in
the [sample](https://github.com/sunildhiman90/KotlinMultiplatformAuth/tree/main/sample)

**_NOTE:_**  To test the sample, Make sure to perform the Platform Setup for Android, ISO, Desktop
and Web(Kotlin/Js only)
as mentioned in the **Platform Setup** section.

### How to Use in Compose Implementation?

If you want to use it in Compose Multiplatform, you can use Either the above code
Or After setting up the KMAuthInitializer, you can use directly GoogleSignInButton composable if you
have installed the `kmauth-google-compose` module.

```kotlin

GoogleSignInButton(modifier = Modifier) { user, error ->
    if (error != null) {
        println("GoogleSignInButton: Error in google Sign In: $error")
    }
    if (user != null) {
        println("GoogleSignInButton: Login Successful")
        println("User Info: ${user.name}, ${user.email}, ${user.profilePicUrl}")
    }
}

```

## Features

- One Tap Sign In with Google(Without Firebase)

## Installation

KMPAuth is available on Maven Central. In your root project `build.gradle.kts` file (or
settings.gradle file), make sure to add mavenCentral() to repositories section.

```kotlin
repositories {
    mavenCentral()
}
```

Then you can add the required library module to your project as follows:

```kotlin
commonMain.dependencies {
    // For using in Pure KMP module without compose, We need to add this as api dependency
    api("io.github.sunildhiman90:kmauth-google:<version>")

    //For using in Compose Multiplatform app.
    implementation("io.github.sunildhiman90:kmauth-google:<version>")

    // Optional: Only if you want to use in built One Tap GoogleSignInButton composable directly
    implementation("io.github.sunildhiman90:kmauth-google-compose:<version>")
}
```

NOTE: You need to
add [Google Sign-In for iOS](https://developers.google.com/identity/sign-in/ios/start-integrating#swift-package-manager)
dependency in your xcode project.

## Library Modules

KotlinMultiplatformAuth has the following modules:

- **kmauth-core:** Core module, dont need to add as dependency, already included in other modules as
  dependency
- **kmauth-google:** Pure Kotlin Multiplatform module, If you want to use in kotlin multiplatform
  without compose multiplatform. This can be used in compose multiplatform as well.
- **kmauth-google-compose:** Compose implementation with in built GoogleSignInButton composable. If
  you
  want to use in kotlin multiplatform with compose multiplatform.

## Setup

### Google Sign In

First of all you need to setup OAuth 2.0 in your project using any of the given methods.

#### Using Google Cloud Platform Console

First of all, you need to set up OAuth 2.0 in Google Cloud Platform Console to get the web Client ID
and Client Secret.

You can follow
this [link](https://developers.google.com/identity/sign-in/web/sign-in#create_authorization_credentials)
to setup OAuth 2.0 in Google Cloud Platform Console.

Once you have set up OAuth 2.0, you need to get the Client ID and Client Secret from Google Cloud
Platform Console and add it to your project.

#### Using Firebase

You can setup OAuth 2.0 in Firebase as well.
If you Enable Google as a sign-in method in the Firebase console:
In the Firebase console, open the Auth section.
On the Sign in method tab, enable the Google sign-in method and click Save.

Once you have enabled Google sign-in in Firebase, Firebase will automatically generate OAuth client
IDs for each platform for your app and add in Google Cloud Platform Console for your firebase
project.
You can get the web Client ID from there from Web client
from [oauth clients](https://console.cloud.google.com/auth/clients)

In this case you can skip the step of setting up OAuth 2.0 in Google Cloud Platform Console. Becoz
it will be handled by Firebase.

### Platform Setup

#### Android

After setting up OAuth 2.0 in Google Cloud Platform Console, you dont need any specific setup for
Android except calling the KMAuthInitializer.initWithContext method as mentioned below.

```kotlin
KMAuthInitializer.initWithContext(
    webClientId = "YOUR_WEB_CLIENT_ID",
    kmAuthPlatformContext = KMAuthPlatformContext(this)
)
```

**Alternative Way (Recommended Way for Compose Multiplatform):**
Alternatively you can use the KMAuthInitializer.initContext method from android MainActivity, if you
want to setup webClientId from common code for all platforms(App composable).

```kotlin
KMAuthInitializer.initContext(
    kmAuthPlatformContext = KMAuthPlatformContext(this)
)
```

And then setup webClientId in common code for all platforms(App composable).

```kotlin   
KMAuthInitializer.init(
    webClientId = "YOUR_WEB_CLIENT_ID",
)
```

If you use This Alternative Way, then you dont need to initialize the KMAuthInitializer in each
platform except Android and JVM. You just need to call initContext in Android and need to call
initClientSecret in deskstop/jvm source set.

#### iOS

After setting up OAuth 2.0 in Google Cloud Platform Console, you need to call the
KMAuthInitializer.init method with webClientId if you are not using Alternative Way mentioned in
Android setup.
If you are using Alternative Way, then you don't need to call the KMAuthInitializer.init method from
iOS becoz we will call it from common code.

**Additional Setup:**
For iOS, we need to do some additional setups as well for Google Sign-In for iOS.

1. You need to add following code in your Info.plist file.

```xml  

<key>GIDClientID</key><string>YOUR_IOS_CLIENT_ID</string>

<key>CFBundleURLTypes</key><array>
<dict>
    <key>CFBundleURLSchemes</key>
    <array>
        <string>YOUR_IOS_REVERSED_CLIENT_ID</string>
    </array>
</dict>
</array>
```

Suppose your iOS client Id is :
`xyzzzzz.apps.googleusercontent.com`

Then your YOUR_REVERSED_CLIENT_ID value will be:
`com.googleusercontent.apps.xyzzzzz`

_**NOTE:_** You can get your YOUR_IOS_CLIENT_ID and YOUR_REVERSED_CLIENT_ID from
`GoogleService-Info.plist` or from Google Cloud Platform Console for iOS oauth client detail.
From ios oauth client detail, you can get your Client ID as YOUR_IOS_CLIENT_ID and iOS URL scheme as
YOUR_REVERSED_CLIENT_ID from additional information section.

2. You also need to add [Google Sign-In for iOS Swift Package](https://developers.google.com/identity/sign-in/ios/start-integrating#swift-package-manager)
   dependency in your xcode project, Otherwise you will get this error:
``
ld: warning: Could not find or use auto-linked framework 'GoogleSignIn': framework 'GoogleSignIn' not found``
#### Desktop

1. On Desktop/Jvm platform, you need to call the KMAuthInitializer.initClientSecret method with
clientSecret which you can get from web client id.
IF you go to the web client id detail and check additional information, It will show you client
secret there.
Make sure to call this method before calling App composable in main.kt in desktopMain source set.

```kotlin
KMAuthInitializer.initClientSecret(
    clientSecret = OAUTH_CLIENT_SECRET,
)
```
2. You also need to make sure you have added the redirect url in web client in google cloud platform oauth clients in **Authorized redirect URIs**: http://localhost:8080/callback, And this may take some time to reflect updates in your code while running your code.

#### Web (Kotlin/Js and Kotlin/Wasm)

You need to call the
MAuthInitializer.init method with webClientId if you are not using Alternative Way mentioned in
Android setup.
If you are using Alternative Way, then you don't need to call the KMAuthInitializer.init method from
web becoz we will call it from common code.

IMP:
Also make sure you have added your allowed origin urls in : Authorized JavaScript origins section in
Google Cloud Platform Console for web auth client, Otherwise you will get this error:
`Access blocked, Authorization error: The request is not allowed.`

For example if you are running on localhost:8080, add this: http://localhost:8080, IN case of your production url you need to add your prod url.

## Contributions
Feel free to contribute if you finds any issues or bugs or want to add a new auth provider support. Future plans are to add other providers as well such as Facebook, Github and Apple Login.

## Used By Projects List
Checkout a voluntary list of projects/companies using KotlinMultiplatformAuth: https://github.com/sunildhiman90/KotlinMultiplatformAuth/discussions/3. Feel free to add your project!


