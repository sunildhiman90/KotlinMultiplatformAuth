import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.sunildhiman90.kmauth.core.KMAuthConfig
import com.sunildhiman90.kmauth.apple.KMAuthApple
import com.sunildhiman90.kmauth.core.KMAuthInitializer
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import com.sunildhiman90.kmauth.supabase.KMAuthSupabase
import com.sunildhiman90.kmauth.supabase.model.SupabaseOAuthProvider
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {


    //For Google
    KMAuthInitializer.initialize(
        config = KMAuthConfig.forGoogle(
            "YOUR_WEB_CLIENT_ID"
        )
    )


    // If you are using kmauth-supabase auth or kmauth-apple auth , then you need to initialize KMAuthSupabase with KMAuthConfig.forSupabase
    // TO test this, add your supabase supabaseUrl, supabaseKey, deepLinkHost, deepLinkScheme. Otherwise you can comment this code and its related code of supabaseAuthManager, otherwise app will crash
    try {
        KMAuthSupabase.initialize(
            KMAuthConfig.forSupabase(
                supabaseUrl = "YOUR_SUPABASE_URL",
                supabaseKey = "YOUR_SUPABASE_KEY",
                deepLinkHost = "YOUR_DEEP_LINK_HOST",
                deepLinkScheme = "YOUR_DEEP_LINK_SCHEME"
            )
        )
    } catch (e: Exception) {
        println("Error in KMAuthSupabase.initialize: ${e.message}")
    }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            //Ideally you will be using it from ViewModel or Repo
            val googleAuthManager = KMAuthGoogle.googleAuthManager
            val appleAuthManager = KMAuthApple.appleAuthManager
            val supabaseAuthManager = KMAuthSupabase.getAuthManager()

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
                            val result = googleAuthManager.signIn()
                            if (result.isSuccess) {
                                println("Login Successful user: ${result.getOrNull()}")
                                userName = result.getOrNull()?.name
                            } else {
                                println("Error in google Sign In: ${result.exceptionOrNull()}")
                            }
                        }
                    }) {
                        Text("Google Sign In")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Apple Sign In")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        scope.launch {

                            val result = supabaseAuthManager.signInWith(
                                supabaseOAuthProvider = SupabaseOAuthProvider.FACEBOOK,
                            )

                            if (result.isSuccess && result.getOrNull() != null) {
                                val user = result.getOrNull()
                                println("Login Successful user: ${result.getOrNull()}")
                                userName = user?.name
                            } else {
                                println("Error in google Sign In: ${result.exceptionOrNull()}")
                            }

                            //Optionally we can use this to get the result for supabase auth
                            KMAuthSupabase.supabaseUserResult.collect {
                                if (it.isSuccess && it.getOrNull() != null) {
                                    println("Login Successful user: ${it.getOrNull()}")
                                } else {
                                    println("Error in google Sign In: ${it.exceptionOrNull()}")
                                }
                            }

                        }
                    }) {
                        Text("Facebook Sign In")
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
                                    googleAuthManager.signOut()
                                    appleAuthManager.signOut()
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