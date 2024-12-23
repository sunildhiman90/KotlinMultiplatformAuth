import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            //Ideally you will be using it from ViewModel
            val googleAuthManager = KMAuthGoogle.getGoogleAuthManager()

            val scope = rememberCoroutineScope()
            Button(onClick = {
                scope.launch {
                    val user = googleAuthManager.signIn()
                    println("user: ${user?.toString()}")
                }
            }) {
                Text("Google Sign In")
            }
        }
    }
}