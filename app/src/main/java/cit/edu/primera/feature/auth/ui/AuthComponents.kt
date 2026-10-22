package cit.edu.primera.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.R
import cit.edu.primera.core.theme.DividerColor
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.core.theme.ToggleTrack

enum class AuthTab(val label: String) {
    LOGIN("Log In"),
    SIGNUP("Sign Up")
}

@Composable
fun AuthTabToggle(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(ToggleTrack),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isSelected) SurfaceWhite else Color.Transparent)
                        .then(
                            if (isSelected) Modifier.shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(28.dp)
                            ) else Modifier
                        )
                        .clickable { onTabSelected(tab) }
                ) {
                    Text(
                        text = tab.label,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 15.sp,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        Text(
            text = "  Or continue with  ",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
    }
}

@Composable
fun SocialIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(BorderStroke(1.dp, InputBorder.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun SocialAuthRow(
    modifier: Modifier = Modifier,
    onGoogleClick: () -> Unit = {},
    onFacebookClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SocialIconButton(
            icon = painterResource(id = R.drawable.google),
            contentDescription = "Sign in with Google",
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f)
        )
        SocialIconButton(
            icon = painterResource(id = R.drawable.facebook),
            contentDescription = "Sign in with Facebook",
            onClick = onFacebookClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AccountLinkingDialog(
    providerName: String?,
    onDismiss: () -> Unit
) {
    val name = providerName ?: "another provider"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Account Already Exists",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "An account with this email address already exists. Please log in with your primary password or $name account to link your credentials.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = PrimeraViolet, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1FB)
@Composable
private fun AuthComponentsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthTabToggle(selectedTab = AuthTab.LOGIN, onTabSelected = {})
            OrDivider()
            SocialAuthRow()
        }
    }
}

@Composable
fun rememberFacebookLoginLauncher(
    onAuthComplete: (String) -> Unit,
    onAuthError: (Exception) -> Unit
): () -> Unit {
    val currentOnAuthComplete = rememberUpdatedState(onAuthComplete)
    val currentOnAuthError = rememberUpdatedState(onAuthError)

    val callbackManager = remember { CallbackManager.Factory.create() }
    val loginManager = remember { LoginManager.getInstance() }

    val launcher = rememberLauncherForActivityResult(
        contract = loginManager.createLogInActivityResultContract(callbackManager, null)
    ) { _ ->
        // The SDK handles the result internally via callbackManager
    }

    DisposableEffect(callbackManager, loginManager) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                currentOnAuthComplete.value(result.accessToken.token)
            }

            override fun onCancel() {
                currentOnAuthError.value(Exception("Facebook login canceled by user"))
            }

            override fun onError(error: FacebookException) {
                currentOnAuthError.value(error)
            }
        }
        loginManager.registerCallback(callbackManager, callback)
        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }

    return remember(launcher) {
        {
            try {
                launcher.launch(listOf("email", "public_profile"))
            } catch (e: Exception) {
                currentOnAuthError.value(e)
            }
        }
    }
}

@Composable
fun rememberGoogleLoginLauncher(
    onAuthComplete: (String) -> Unit,
    onAuthError: (Exception) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = androidx.compose.ui.res.stringResource(id = R.string.default_web_client_id)

    return {
        if (webClientId.isBlank() || webClientId == "YOUR_WEB_CLIENT_ID") {
            onAuthError(Exception("Google Web Client ID is not configured. Please set default_web_client_id in strings.xml or update google-services.json."))
        } else {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            scope.launch {
                try {
                    val result = try {
                        credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                    } catch (_: NoCredentialException) {
                        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                            .build()
                        val signInRequest = GetCredentialRequest.Builder()
                            .addCredentialOption(signInWithGoogleOption)
                            .build()
                        credentialManager.getCredential(
                            request = signInRequest,
                            context = context
                        )
                    }
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onAuthComplete(googleIdTokenCredential.idToken)
                    } else {
                        onAuthError(Exception("Unexpected credential type returned: ${credential.type}"))
                    }
                } catch (e: GetCredentialCancellationException) {
                    android.util.Log.w("GoogleLogin", "Google Sign-In canceled or dismissed: ${e.message}", e)
                    val msg = e.message ?: ""
                    if (msg.contains("16") || msg.contains("reauth", ignoreCase = true)) {
                        onAuthError(Exception("Google Sign-In error [16]: SHA-1 fingerprint or package name mismatch in Firebase Console."))
                    }
                } catch (e: GetCredentialException) {
                    android.util.Log.e("GoogleLogin", "CredentialException during Google sign-in: ${e.type} - ${e.message}", e)
                    onAuthError(Exception("Google Sign-In failed (${e.type}). Verify Web Client ID and SHA-1 fingerprint in Firebase Console."))
                } catch (e: Exception) {
                    android.util.Log.e("GoogleLogin", "Exception during Google sign-in", e)
                    onAuthError(e)
                }
            }
        }
    }
}
