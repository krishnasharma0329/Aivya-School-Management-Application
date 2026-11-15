package com.aivya.schoolmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivya.schoolmanagement.auth.TeacherSession
import com.aivya.schoolmanagement.auth.TokenStore
import com.aivya.schoolmanagement.dashboard.OfflineScreen
import com.aivya.schoolmanagement.dashboard.TeacherDashboardScreen
import com.aivya.schoolmanagement.dashboard.data.RemoteTeacherDashboardRepository
import com.aivya.schoolmanagement.network.ApiClient
import com.aivya.schoolmanagement.network.LoginRequest
import com.aivya.schoolmanagement.network.NetworkResult
import com.aivya.schoolmanagement.network.rememberIsOnline
import com.aivya.schoolmanagement.network.safeApiCall
import kotlinx.coroutines.launch

private val Navy = Color(0xFF2D1B69)
private val LightBlue = Color(0xFFF8F6FD)
private val BorderBlue = Color(0xFFE6E1F5)
private val Muted = Color(0xFF6B6480)
private val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AivyaApp()
            }
        }
    }
}

@Composable
fun AivyaApp() {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val api = remember { ApiClient.create(tokenStore) }

    var session by remember { mutableStateOf(tokenStore.load()) }
    val isOnline by rememberIsOnline()

    Surface(modifier = Modifier.fillMaxSize(), color = LightBlue) {
        if (!isOnline) {
            OfflineScreen(onRetry = {})
        } else if (session == null) {
            LoginScreen(
                onLogin = { loggedIn -> session = loggedIn },
            )
        } else {
            val repository = remember(session) { RemoteTeacherDashboardRepository(api, tokenStore) }
            TeacherDashboardScreen(
                repository = repository,
                onLogoutClick = {
                    tokenStore.clear()
                    session = null
                },
            )
        }
    }
}

@Composable
fun LoginScreen(onLogin: (TeacherSession) -> Unit) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val api = remember { ApiClient.create(tokenStore) }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("teacher@aivya.edu") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun attemptLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password."
            return
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            when (val result = safeApiCall { api.login(LoginRequest(email.trim(), password)) }) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val session = TeacherSession(
                        token = response.token,
                        teacherId = response.teacherId,
                        fullName = response.fullName,
                        email = response.email,
                        role = response.role,
                    )
                    tokenStore.save(session)
                    isLoading = false
                    onLogin(session)
                }
                is NetworkResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlue)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(65.dp))

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = White,
            border = BorderStroke(1.dp, BorderBlue),
        ) {
            Text(
                "◇  AIVYA  ·  TEACHER PORTAL",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
                color = Navy,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(28.dp))

        Text("Let's Get Started", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Navy)

        Spacer(Modifier.height(8.dp))

        Text("Welcome to your academic classroom portal", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Hello, Teacher!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)

                Spacer(Modifier.height(8.dp))

                Text("Sign in to manage your classes and daily academic activities.", color = Muted)

                Spacer(Modifier.height(25.dp))

                Text("Email address", color = Navy, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("teacher@aivya.edu") },
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(16.dp))

                Text("Password", color = Navy, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                )

                errorMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(message, color = Color(0xFFE24A74), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(25.dp))

                Button(
                    onClick = { attemptLogin() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.height(20.dp))
                    } else {
                        Text("Sign in to Aivya")
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Demo credentials: teacher@aivya.edu / AivyaDemo@2026",
                    fontSize = 10.sp,
                    color = Muted,
                )
            }
        }

        Spacer(Modifier.height(35.dp))

        Text("Aivya · School Management", color = Muted, fontSize = 12.sp)

        Spacer(Modifier.height(24.dp))
    }
}
