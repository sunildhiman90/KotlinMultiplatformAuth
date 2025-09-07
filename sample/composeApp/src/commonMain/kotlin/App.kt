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


    //If you are using only supabase auth from kmauth, then you need to initialize KMAuthSupabase with KMAuthConfig.forSupabase
    KMAuthSupabase.initialize(
        KMAuthConfig.forSupabase(
            supabaseUrl = "YOUR_SUPABASE_URL",
            supabaseKey = "YOUR_SUPABASE_KEY",
            deepLinkHost = "YOUR_DEEP_LINK_HOST",
            deepLinkScheme = "YOUR_DEEP_LINK_SCHEME"
        )
    )

    MaterialTheme {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            //Ideally you will be using it from ViewModel or Repo
            val googleAuthManager = KMAuthGoogle.googleAuthManager
            val appleAuthManager = KMAuthApple.appleAuthManager

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
                            /*val result = googleAuthManager.signIn()
                            if (result.isSuccess) {
                                println("Login Successful user: ${result.getOrNull()}")
                                userName = result.getOrNull()?.name
                            } else {
                                println("Error in google Sign In: ${result.exceptionOrNull()}")
                            }*/

                            val result = KMAuthSupabase.getAuthManager().signInWith(
                                supabaseOAuthProvider = SupabaseOAuthProvider.GOOGLE,
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

                    Button(onClick = {
                        scope.launch {

                            //Without callback, Recommended way
                            val result = appleAuthManager.signIn()
                            if (result.isSuccess) {
                                println("Login Successful user: ${result.getOrNull()}")
                                userName = result.getOrNull()?.name
                            } else {
                                println("Error in apple Sign In: ${result.exceptionOrNull()}")
                            }

                            //Using callback
//                            appleAuthManager.signIn { user, error ->
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
                        Text("Apple Sign In")
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