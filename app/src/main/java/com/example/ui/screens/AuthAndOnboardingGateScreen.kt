package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    if (auth?.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In কনফিগারেশন পাওয়া যায়নি (default_web_client_id অনুপস্থিত)।")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("অপ্রত্যাশিত ক্রেডেনশিয়াল টাইপ। অনুগ্রহ করে আবার চেষ্টা করুন।")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            val msg = e.localizedMessage ?: ""
            val friendly = if (msg.contains("NoCredential", ignoreCase = true) || msg.contains("no credentials", ignoreCase = true)) {
                "এই ডিভাইসে/ইমুলেটরে কোনো Google অ্যাকাউন্ট লগইন করা নেই। ক্লাউড সিঙ্কের জন্য ডিভাইসের Settings > Accounts-এ Google অ্যাকাউন্ট যুক্ত করুন, অথবা নিচের 'অফলাইন ডিভাইস মোডে শুরু করো' বাটনে চাপ দিয়ে এখনই শেখা শুরু করুন!"
            } else {
                msg.ifBlank { "Google লগইন সম্পন্ন করা যায়নি। নিচের অফলাইন বাটনে চাপ দিয়ে সরাসরি অ্যাপে প্রবেশ করতে পারেন।" }
            }
            onAuthError(friendly)
        }
    }
}

fun signOutFromGoogleAndFirebase(
    context: Context,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    try {
        FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {}
    val credentialManager = CredentialManager.create(context)
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthAndOnboardingGateScreen(
    onAuthSuccess: () -> Unit,
    onContinueOfflineLocal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EmeraldDeep, EmeraldDark, Color(0xFF064E3B))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .border(3.dp, GoldBright, RoundedCornerShape(28.dp))
                .testTag("auth_gate_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(GoldSoft)
                        .border(2.5.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 46.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "শিশুদের ইসলাম শিক্ষা",
                    style = MaterialTheme.typography.displayMedium,
                    color = EmeraldDeep,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Islamic Education for Kids",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🌟 আনন্দময় ৬-ধাপের কোরআন ও আরবি শেখার যাত্রা:",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text("১. অক্ষরের বন্ধু (২৯টি অক্ষর, মাখরাজ ও আঙুল দিয়ে আঁকা)", style = MaterialTheme.typography.bodyMedium)
                        Text("২. হরকতের জাদু (যবর, যের, পেশ ও তানভীন)", style = MaterialTheme.typography.bodyMedium)
                        Text("৩. অক্ষর জোড়ার অভিযান (যুক্তাক্ষর, সুকূন, তাশদীদ ও মদ)", style = MaterialTheme.typography.bodyMedium)
                        Text("৪. শব্দ পড়ার অভিযান (সহজ থেকে কঠিন শব্দ অনুশীলন)", style = MaterialTheme.typography.bodyMedium)
                        Text("৫. আমপারা অভিযান (সূরা আন-নাবা থেকে আন-নাস)", style = MaterialTheme.typography.bodyMedium)
                        Text("৬. পূর্ণ কোরআনের যাত্রা (সূরা আল-ফাতিহা, আল-বাকারা ও তাজবীদ)", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নিরাপদ ক্লাউড ব্যাকআপ, অফলাইন সিঙ্ক ও অভিভাবক প্যানেলের জন্য Google অ্যাকাউন্ট দিয়ে সাইন-ইন করো অথবা সরাসরি অফলাইন মোডে শুরু করো।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF78350F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (authErrorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFE4E6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ $authErrorMessage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF881337),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        isLoading = true
                        authErrorMessage = null
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { err ->
                                isLoading = false
                                authErrorMessage = err
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                                authErrorMessage = "লগইন বাতিল করা হয়েছে। আপনি চাইলে নিচের অফলাইন মোড বাটনেও শুরু করতে পারেন।"
                            }
                        )
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Default.CloudDone, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google (সাইন-ইন করো)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onContinueOfflineLocal,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldBright,
                        contentColor = EmeraldDeep
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_offline_local_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অফলাইন ডিভাইস মোডে শুরু করো",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
