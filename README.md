# KotlinMultiplatformAuth - A Kotlin Multiplatform Authentication Library

![Build](https://img.shields.io/github/actions/workflow/status/sunildhiman90/KotlinMultiplatformAuth/publish.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sunildhiman90/kmauth-google?color=blue)](https://central.sonatype.com/search?q=io.github.sunildhiman90+kmauth&smo=true)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

Kotlin Multiplatform Authentication library targeting Android, iOS, Desktop and Web(Kotlin/Js and Kotlin/Wasm Both).
It supports following Sign In Features:
- Sign In with Google(Without 3rd Party Auth Library) 
- Sign In With Apple/Facebook/GitHub/Twitter and other OAuthProviders [SupabaseOAuthProvider](kmauth-supabase/src/commonMain/kotlin/com/sunildhiman90/kmauth/supabase/model/SupabaseOAuthProvider.kt) Using Supabase.
- Sign In With Email/Phone/IDToken etc. all [SupabaseDefaultAuthProvider](kmauth-supabase/src/commonMain/kotlin/com/sunildhiman90/kmauth/supabase/model/SupabaseDefaultAuthProvider.kt) Using Supabase.

NOTE: For using Sign In With Apple or other OAuthProviders with SupabaseAuthManager, you need to setup the supabase for specific provider, refer to the supabase docs.

## Deepwiki Docs
[![Deepwiki Docs](https://img.shields.io/badge/DeepWiki-009485?style=for-the-badge&logo=readthedocs&logoColor=white)](https://deepwiki.com/sunildhiman90/KotlinMultiplatformAuth)

## Youtube Video
Youtube vide for using Google Signin In Compose Multiplatform using KotlinMultiplatformAuth 
[How to Use Google Signin In Compose Multiplatform using KotlinMultiplatformAuth ](https://youtu.be/-5Ws4HSaYJc)

## Quick Start Sample Code

### KMAuthInitializer

First of all you need to initialize the KMAuthInitializer.

```kotlin

// For Android platform, Its better to call it from the activity, Becoz we need activity context. We dont need to cal this from other platforms except android.
KMAuthInitializer.initContext(
    kmAuthPlatformContext = KMAuthPlatformContext(this)
)

//For Sign In With Google, Then we need to call initialize method from common code
KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = "YOUR_WEB_CLIENT_ID"))

// For Sign In With Apple or other providers, we need to call KMAuthSupabase.initialize method from common code
KMAuthSupabase.initialize(
    KMAuthConfig.forSupabase(
        supabaseUrl = "YOUR_SUPABASE_URL",
        supabaseKey = "YOUR_SUPABASE_KEY",
        deepLinkHost = "YOUR_DEEP_LINK_HOST",
        deepLinkScheme = "YOUR_DEEP_LINK_SCHEME",
    )
)
```

We need webClientId from Google Cloud Platform Console to setup the serverClientId in Google API for
identifying signed-in users in backend server.

### GoogleAuthManager, AppleAuthManager and SupabaseAuthManager

After initializing the KMAuthInitializer, you can use the KMAuthGoogle object to get the
GoogleAuthManager Or KMAuthApple to get the AppleAuthManager Or KMAuthSupabase object to get the SupabaseAuthManager to sign in the user according to your requirement.

```kotlin
Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

    //Ideally you will be using these from ViewModel or Repo
    
    // For Sign In With Google
    val googleAuthManager = KMAuthGoogle.googleAuthManager

    // For Sign In With Apple: Available only after 0.3.0
    val appleAuthManager = KMAuthApple.appleAuthManager
  
    // For Sign In With other providers such as Facebook, Github, Twitter etc. : Available only after 0.3.0
    // For these providers using supabase, for setting up the supabase for specific provider, refer to the supabase docs   
    val supabaseAuthManager = KMAuthSupabase.getAuthManager()

    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            val result = googleAuthManager.signIn()
            if (result.isSuccess) {
                println("Login Successful user: ${result.getOrNull()}")
                userName = result.getOrNull()?.name
            } else {
                println("Error in google Sign In: ${result.exceptionOrNull()}")
            }
        }
    }) {
        Text("Sign In With Google")
    }

    Button(onClick = {
      scope.launch {
        val result = appleAuthManager.signIn()
        if (result.isSuccess) {
          println("Login Successful user: ${result.getOrNull()}")
          userName = result.getOrNull()?.name
        } else {
          println("Error in apple Sign In: ${result.exceptionOrNull()}")
        }
      }
    }) {
      Text("Sign In With Apple")
    }

    Button(onClick = {
      scope.launch {
        val oauthProvider = SupabaseOAuthProvider.Github
        val result = supabaseAuthManager.signInWith(supabaseOAuthProvider = oauthProvider)
        if (result.isSuccess) {
          println("Login Successful user: ${result.getOrNull()}")
          userName = result.getOrNull()?.name
        } else {
          println("Error in supabase oauth provider $oauthProvider Sign In: ${result.exceptionOrNull()}")
        }
      }
    }) {
      Text("Sign In With Github")
    }
}


// For all OAuthProviders except Apple, you need to call handleDeepLink from iOSApp file(ie. entry point)

// Call this method in onOpenUrl method of iOSApp file, this is not needed for sign in with apple for iOS
// becoz for iOS we are using AuthenticationServices + supabase signInWithIdToken but not signInWith oauth provider, but on Android, its needed:

ContentView()
  .onOpenURL { url in
  
    // Handle supabase deep link url
    //If using kmauth-supabase 
    KMAuthSupabase.shared.deepLinkHandler().handleDeepLinks(url: url)
  }

//Similarly for android, call this method in MainActivity, We need Android deep link handling code for Apple oauth provider as well along with other oauth providers of supabase:


override fun onNewIntent(
  intent: Intent,
  caller: ComponentCaller,
) {
  super.onNewIntent(intent, caller)
  
  //If using only kmauth-apple
  KMAuthApple.shared.deepLinkHandler().handleDeepLinks(intent)

    //If using kmauth-supabase directly
  //KMAuthSupabase.shared.deepLinkHandler().handleDeepLinks(intent)
}


```
**NOTE:** For deep links setup we can follow these links for android and ios:
**Android:** https://developer.android.com/training/app-links/deep-linking
**iOS:** https://developer.apple.com/documentation/xcode/defining-a-custom-url-scheme-for-your-app
Also make sure to add these deep link urls(with this combination -> scheme://host) in **Redirect URLs** in URL Configuration section of supabase auth dashboard.

You can find these sample codes in
the [sample](https://github.com/sunildhiman90/KotlinMultiplatformAuth/tree/main/sample)

**_NOTE:_**  To test the sample, Make sure to perform the Platform Setup for Android, ISO, Desktop
and Web(Kotlin/Js only)
as mentioned in the **Platform Setup** section.

### How to Use in Compose Implementation?

If you want to use GoogleSignIn in Compose Multiplatform, you can use Either the above code
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

- Sign In with Google(Without 3rd Party Auth Library)
- Sign In With Apple: iOS(Native Sign in Using AuthenticationServices + Supabase), Android, Jvm, Js and WasmJs(Using Supabase)
- Sign In With Facebook/GitHub/Twitter and other OAuthProviders [SupabaseOAuthProvider](kmauth-supabase/src/commonMain/kotlin/com/sunildhiman90/kmauth/supabase/model/SupabaseOAuthProvider.kt) Using Supabase.
- Sign In With Email/Phone/IDToken etc. all [SupabaseDefaultAuthProvider](kmauth-supabase/src/commonMain/kotlin/com/sunildhiman90/kmauth/supabase/model/SupabaseDefaultAuthProvider.kt) Using Supabase.

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

    // For using Sign In With Apple, We need to add this as api dependency to handle deep linking from ios
    api("io.github.sunildhiman90:kmauth-apple:<version>")

    // For using Sign In With  OAuthProviders such as Google/Facebook/GitHub/Twitter etc. with SupabaseAuthManager, We need to add this as api dependency to handle deep linking from ios
    api("io.github.sunildhiman90:kmauth-supabase:<version>")
  
    //For using Google Sign In in Compose Multiplatform app.
    implementation("io.github.sunildhiman90:kmauth-google:<version>")

    // Optional: Only if you want to use in built One Tap GoogleSignInButton composable directly
    implementation("io.github.sunildhiman90:kmauth-google-compose:<version>")
}

//And for Sign In With Google or other OAuthProviders, you need to export api dependencies using export
listOf(
  iosX64(),
  iosArm64(),
  iosSimulatorArm64()
).forEach { iosTarget ->
  iosTarget.binaries.framework {
    baseName = "ComposeApp"
    isStatic = true

    // Here: Export it to iosApp xcode project for calling handleDeepLink
    export("io.github.sunildhiman90:kmauth-supabase:<version>") // if using Sign In with other OAuthProviders
  }
}
```

NOTE: For Google Sign In in iOS, You need to
add [Google Sign-In for iOS](https://developers.google.com/identity/sign-in/ios/start-integrating#swift-package-manager)
dependency in your xcode project.

## Library Modules

KotlinMultiplatformAuth has the following modules:

- **kmauth-core:** Core module, dont need to add as dependency, already included in other modules as
  dependency
- **kmauth-google:** Pure Kotlin Multiplatform module, If you want to use in kotlin multiplatform
  without compose multiplatform. This can be used in compose multiplatform as well. It provides Sign In with Google authentication feature.
- **kmauth-google-compose:** Compose implementation with in built GoogleSignInButton composable. If
  you want to use in kotlin multiplatform with compose multiplatform.
- **kmauth-supabase:** Pure Kotlin Multiplatform module for supabase authentication. It provides following auth features:
  - Sign In with Supabase OAuthProvider: Google/Apple/Facebook/GitHub ... etc
  - Sign In with Supabase DefaultAuthProviders: Email & Password, Phone & Password and IDToken etc.
  - Sign Out
  - Reset Password
  - Link Identity
- ** kmauth-apple:** Pure Kotlin Multiplatform module for apple authentication. It provides Sign In with Apple authentication feature.

# Setup & Integration

<details>
  <summary>
    Google Sign In
  </summary>

## Google Sign In

### Google Cloud Platform Console Setup

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
Android except calling the KMAuthInitializer.initContext method as mentioned below.

```kotlin
KMAuthInitializer.initContext(
    kmAuthPlatformContext = KMAuthPlatformContext(this)
)
```

And then setup webClientId in common code for all platforms(App composable).

```kotlin   
 KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = "YOUR_WEB_CLIENT_ID"))
```

If you use KMAuthInitializer.initialize from common code, then you dont need to initialize the KMAuthInitializer in each
platform except Android and JVM. You just need to call initContext in Android and need to call
initClientSecret in desktop/jvm source set.

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
Alternatively, you can setup clientSecret in KMAuthInitializer.initialize method itself from app composable. Then you dont need to set it here.
2. You also need to make sure you have added the redirect url in web client in google cloud platform oauth clients in **Authorized redirect URIs**: http://localhost:8080/callback, And this may take some time to reflect updates in your code while running your code.

#### Web (Kotlin/Js and Kotlin/Wasm)

You need to call the
MAuthInitializer.initialize method with webClientId if you are not using KMAuthInitializer.initialize from common code mentioned in
Android setup.
If you are using KMAuthInitializer.initialize from common code, then you don't need to call the KMAuthInitializer.init method from
web becoz we will call it from common code.

IMP:
Also make sure you have added your allowed origin urls in : Authorized JavaScript origins section in
Google Cloud Platform Console for web auth client, Otherwise you will get this error:
`Access blocked, Authorization error: The request is not allowed.`

For example if you are running on localhost:8080, add this: http://localhost:8080, IN case of your production url you need to add your prod url.

</details>

<details>
  <summary>
    Apple Sign In
  </summary>


## Apple Sign In

IMP: First pre requisite step is to make sure You have Apple Developer Account. If you dont have it, you can get it from [Apple Developer](https://developer.apple.com/). Then you need to get membership for Apple Developer Program.: https://developer.apple.com/programs/enroll/

### Supabase Auth Dashboard Setup

First of all, You need to setup Apple Sign In Provider in Supabase Auth Dashboard.

1. You need to enable Apple Sign In Provider in Supabase Auth Dashboard
2. Add your Apple iOS App Bundle ID(for iOS) and Apple Service ID(for Android, Jvm and Web) identifier with comma separated in client IDs field of Apple Sign In Provider.
3. While creating Service ID Identifier in Apple Developer Center, Please make sure that you have enabled Sign In With Apple capability and added Callback Url(which you can copy from Supabase Apple Sign In Provider) to **Website URLs** section in  **Web Authentication Configuration** of Sign In With Apple in Apple Developer Center: https://developer.apple.com/account/resources/identifiers
4. We also need to add Secret Key in Supabase Apple Sign In Provider in **Secret Key (for OAuth)** section. We can refer these docs/steps for creating JWT secret Key for Apple Sign In:
  - First of all create Private Key in Apple Developer Center with Sign In With Apple capability/service enabled: https://developer.apple.com/account/resources/authkeys/list
  - Then follow these steps and along with all jwt fields, you need to create a jwt token from jwt.io: https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret   
5. Finally we can add that jwt secret key in Supabase Apple Sign In Provider in **Secret Key (for OAuth)** section.

**NOTE:** Make sure to add your ios deep link scheme in **Redirect URLs** in URL Configuration section of supabase auth dashboard. 

### Add Sign In With Apple Capability
  Make sure to add Sign In With Apple capability in your app in Xcode for iOS in **Signing & Capabilities** section.

</details>

<details>
  <summary>
    Other Supabase OAuthProviders Sign In(Facebook, Github, Twitter etc.)
  </summary>

For all other providers, we can use SupabaseAuthManager to handle sign in with other OAuth providers.
And you can follow the https://supabase.com/docs/guides/auth/social-login to setup other providers in supabase auth dashboard.

**NOTE:** Make sure to add your android/ios deep link schemes and web redirect urls of used provider in **Redirect URLs** in URL Configuration section of supabase auth dashboard.

</details>

<details>
  <summary>
    Other Supabase DefaultOAuthProviders Sign In (Email, Phone, IDToken etc.)
  </summary>

For all DefaultOAuthProviders, You can simply use SupabaseAuthManager **signInWith** method.

```kotlin
val supabaseAuthManager = KMAuthSupabase.getAuthManager()

val result = supabaseAuthManager.signInWith(
    supabaseDefaultAuthProvider = SupabaseDefaultAuthProvider.EMAIL_PASSWORD,
    config = SupabaseAuthConfig().apply {
      email = "test@gmail.com"
      password = "123"
    }
)

if (result.isSuccess) {
    println("Login Successful user: ${result.getOrNull()}")
    userName = result.getOrNull()?.name
} else {
    println("Error in email password Sign In: ${result.exceptionOrNull()}")
}

// NOTE: IN case of web, It will redirect back to our app and reload the app after sign in from oauth provider url, then we will not be able to reflect sign in result due to reload,
// So in that case, we can collect supabaseUserResult and use this logic where we need to check sign in status.
KMAuthSupabase.supabaseUserResult.collect { result ->
  Logger.d("currentSupabaseUser: $result")
  if (result.isSuccess) {
    _currentUser.value = result.getOrNull()?.toUser()
  } else {
    _currentUser.value = null
  }
}
```
 
</details>

## Contributions
Feel free to contribute if you finds any issues or bugs or want to add a new auth provider support. Future plans are to add other providers as well such as Facebook, Github and Apple Login.

## Used By Projects List
Checkout a voluntary list of projects/companies using KotlinMultiplatformAuth: https://github.com/sunildhiman90/KotlinMultiplatformAuth/discussions/3. Feel free to add your project!


