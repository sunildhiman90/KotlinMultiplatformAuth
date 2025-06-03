import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sunildhiman90.kmauth.core.KMAuthInitializer

fun main() = application {

    KMAuthInitializer.initClientSecret(
        clientSecret = "YOUR_OAUTH_CLIENT_SECRET",
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "sample",
    ) {
        App()
    }
}