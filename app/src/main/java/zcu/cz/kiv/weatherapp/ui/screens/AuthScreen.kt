package zcu.cz.kiv.weatherapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import zcu.cz.kiv.weatherapp.R
import zcu.cz.kiv.weatherapp.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val loading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()

    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val isEmailValid = remember(email) {
        email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = remember(password) {
        password.isBlank() || password.length >= 6
    }

    val isFormValid = email.isNotBlank() && isEmailValid && password.isNotBlank() && isPasswordValid

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearError() // Очищаем ошибку после показа
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = if (isLogin) Icons.Rounded.LockPerson else Icons.Rounded.PersonAddAlt1,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(if (isLogin) R.string.auth_welcome_back else R.string.auth_create_account),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(if (isLogin) R.string.auth_subtitle_login else R.string.auth_subtitle_register),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Email,
                        contentDescription = stringResource(R.string.cd_email_icon)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = !isEmailValid,
                supportingText = {
                    if (!isEmailValid) {
                        Text(stringResource(R.string.error_invalid_email))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.cd_password_icon)
                    )
                },
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            icon,
                            contentDescription = stringResource(R.string.cd_toggle_password_visibility)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isFormValid && !loading) {
                            if (isLogin) authViewModel.login(email, password, onSuccess)
                            else authViewModel.register(email, password, onSuccess)
                        }
                    }
                ),
                isError = !isPasswordValid,
                supportingText = {
                    if (!isPasswordValid) {
                        Text(stringResource(R.string.error_short_password))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isLogin) {
                        authViewModel.login(email, password, onSuccess)
                    } else {
                        authViewModel.register(email, password, onSuccess)
                    }
                },
                enabled = isFormValid && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(if (isLogin) R.string.login_button else R.string.create_account_button),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isLogin = !isLogin
                    email = ""
                    password = ""
                    authViewModel.clearError()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(if (isLogin) R.string.switch_to_register else R.string.switch_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}