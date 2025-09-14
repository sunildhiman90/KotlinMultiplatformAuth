package com.sunildhiman90.kmauthsample

import App
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.core.KMAuthPlatformContext
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            KMAuthInitializer.initContext(
                kmAuthPlatformContext = KMAuthPlatformContext(this)
            )

            App()
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        KMAuthSupabase.deepLinkHandler().handleDeepLinks(intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}