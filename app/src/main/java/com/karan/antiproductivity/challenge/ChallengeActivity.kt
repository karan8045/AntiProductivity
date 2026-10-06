package com.karan.antiproductivity.challenge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karan.antiproductivity.MainActivity
import kotlinx.coroutines.delay

class ChallengeActivity : ComponentActivity() {

    private val viewModel: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(this@ChallengeActivity, MainActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        })

        val targetPackage = intent.getStringExtra("target_package") ?: ""
        var appName = targetPackage

        try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(targetPackage, 0)
            appName = pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {}

        setContent {
            val state by viewModel.state.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val s = state) {
                        is ChallengeState.Active -> {
                            ActiveChallengeContent(
                                appName = appName,
                                targetString = s.targetString,
                                userInput = s.userInput,
                                onInputChanged = { viewModel.updateUserInput(it) },
                                onSubmit = { viewModel.submitChallenge(targetPackage) },
                                onRegenerate = { viewModel.regenerateChallenge() }
                            )
                        }
                        is ChallengeState.Success -> {
                            SuccessContent(
                                appName = appName,
                                minutes = s.minutesUnlocked,
                                onDismiss = {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
                                    if (launchIntent != null) {
                                        startActivity(launchIntent)
                                    } else {
                                        val homeIntent = Intent(this@ChallengeActivity, MainActivity::class.java)
                                        startActivity(homeIntent)
                                    }
                                    finish()
                                }
                            )
                        }
                        is ChallengeState.Failure -> {
                            FailureContent(
                                roast = s.roastMessage,
                                onRetry = { viewModel.retry() },
                                onBackHome = {
                                    val homeIntent = Intent(this@ChallengeActivity, MainActivity::class.java)
                                    startActivity(homeIntent)
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChallengeContent(
    appName: String,
    targetString: String,
    userInput: String,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ACCESS DENIED.",
            color = Color.Red,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You tried to open $appName.",
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Earn 15 minutes first.",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TYPE THIS EXACTLY:",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = targetString,
                    color = Color.Green,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type here...", color = Color.DarkGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Green,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onRegenerate) {
                        Text("New String", color = Color.Gray)
                    }

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Submit", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessContent(
    appName: String,
    minutes: Int,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FINE. YOU EARNED IT.",
            color = Color.Green,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$appName unlocked for $minutes minutes.",
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = Color.Green)
    }
}

@Composable
fun FailureContent(
    roast: String,
    onRetry: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NOPE.",
            color = Color.Red,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = roast,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("TRY AGAIN", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("BACK TO PRODUCTIVITY", fontWeight = FontWeight.Bold)
        }
    }
}
