import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    KMAuthInitializer.initialize(KMAuthConfig.forGoogle(webClientId = "YOUR_WEB_CLIENT_ID"))


    MaterialTheme {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            //Ideally you will be using it from ViewModel or Repo
            val googleAuthManager = KMAuthGoogle.googleAuthManager

            var userName: String? by remember {
                mutableStateOf(
                    null
                )
            }

            val scope = rememberCoroutineScope()
            AnimatedVisibility(
                userName == null
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        scope.launch {

                            //Without callback, Recommended way
                            val result = googleAuthManager.signIn()
                            if (result.isSuccess) {
                                println("Login Successful user: ${result.getOrNull()}")
                                userName = result.getOrNull()?.name
                            } else {
                                println("Error in google Sign In: ${result.exceptionOrNull()}")
                            }

                            //Using callback
//                            googleAuthManager.signIn { user, error ->
//                                if (error != null) {
//                                    println("Error in google Sign In: $error")
//                                }
//                                if (user != null) {
//                                    println("Login Successful user: $user")
//                                    userName = user.name
//                                }
//                            }
                        }
                    }) {
                        Text("Google Sign In")
                    }
                }
            }

            AnimatedVisibility(userName != null) {
                userName?.let {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("User Name: $userName")
                        TextButton(
                            onClick = {
                                scope.launch {
                                    googleAuthManager.signOut(userName)
                                    userName = null
                                }
                            }
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}