package com.jitou.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.data.auth.AuthRepository
import com.jitou.app.ui.theme.jitouColors

private val LoginBackground: Color
    @Composable get() = MaterialTheme.jitouColors.background
private val LoginSurface: Color
    @Composable get() = MaterialTheme.jitouColors.surface
private val LoginInk: Color
    @Composable get() = MaterialTheme.jitouColors.ink
private val LoginYellow: Color
    @Composable get() = MaterialTheme.jitouColors.accent
private val LoginMuted: Color
    @Composable get() = MaterialTheme.jitouColors.mutedInk
private val LoginLine: Color
    @Composable get() = MaterialTheme.jitouColors.line
private val LoginDanger: Color
    @Composable get() = MaterialTheme.jitouColors.danger

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSignIn: (account: String, password: String) -> Unit,
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val normalizedAccount = AuthRepository.normalizeJitouEmail(account)

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LoginBackground)
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.isInitializing) {
                CircularProgressIndicator(color = LoginInk)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("JITOU", color = LoginInk, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("几头", color = LoginInk, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    Text("几时剪头，先登录同步一下", color = LoginMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LoginSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, LoginLine, RoundedCornerShape(24.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = account,
                            onValueChange = { account = it },
                            label = { Text("账号") },
                            singleLine = true,
                            supportingText = {
                                if (account.isNotBlank()) {
                                    Text("将使用 $normalizedAccount 登录")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                    onSignIn(account, password)
                                },
                            ),
                        )
                        uiState.errorMessage?.let { message ->
                            Text(message, color = LoginDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                keyboard?.hide()
                                onSignIn(account, password)
                            },
                            enabled = !uiState.isLoading && account.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LoginYellow,
                                contentColor = LoginInk,
                                disabledContainerColor = LoginLine,
                                disabledContentColor = LoginMuted,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = LoginInk, strokeWidth = 2.dp)
                            } else {
                                Text("登录", fontWeight = FontWeight.Black, letterSpacing = 0.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
