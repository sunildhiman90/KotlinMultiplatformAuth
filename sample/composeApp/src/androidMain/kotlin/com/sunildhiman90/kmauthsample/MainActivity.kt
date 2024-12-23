package com.sunildhiman90.kmauthsample

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            //Initialize the KMAuthInitializer
            KMAuthInitializer.init(
                webClientId = "YOUR_WEB_CLIENT_ID",
                kmAuthPlatformContext = KMAuthPlatformContext(this)
            )

            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}