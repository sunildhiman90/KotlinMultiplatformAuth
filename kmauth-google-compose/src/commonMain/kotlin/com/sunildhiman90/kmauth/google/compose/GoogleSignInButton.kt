package com.sunildhiman90.kmauth.google.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sunildhiman90.kmauth.core.KMAuthUser
import com.sunildhiman90.kmauth.google.GoogleAuthManager
import com.sunildhiman90.kmauth.google.KMAuthGoogle
import io.github.sunildhiman90.kmauth_google_compose.generated.resources.Res
import io.github.sunildhiman90.kmauth_google_compose.generated.resources.google_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onSignInResult: ((KMAuthUser?, Throwable?) -> Unit)? = null
) {

    val googleAuthManager = rememberGoogleAuthManager()
    val scope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        scope.launch {
            googleAuthManager.signIn { user, error ->
                onSignInResult?.invoke(user, error)
                if (error != null) {
                    println("GoogleSignInButton: Error in google Sign In: $error")
                }
                if (user != null) {
                    println("GoogleSignInButton: Login Successful")
                }
            }
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.google_icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign in with Google",
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun rememberGoogleAuthManager(): GoogleAuthManager {
    return remember {
        KMAuthGoogle.googleAuthManager
    }
}