package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.ui.icon.AlertTriangle
import com.example.ui.icon.ArrowLeft
import com.example.ui.icon.CheckCircle2
import com.example.ui.icon.Eye
import com.example.ui.icon.EyeOff
import com.example.ui.icon.Lock
import com.example.ui.icon.LogIn
import com.example.ui.icon.LucideIcons
import com.example.ui.icon.Mail
import com.example.ui.icon.UserPlus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.auth.GoogleSignInHelper
import com.example.presentation.auth.state.AuthUiState
import com.example.ui.component.AppCircularProgressIndicator
import com.example.ui.theme.spacing
import kotlinx.coroutines.launch
import android.content.ActivityNotFoundException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    googleSignInHelper: GoogleSignInHelper = GoogleSignInHelper(LocalContext.current)
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val actionMessage by authViewModel.actionMessage.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Sign In, 1: Sign Up
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var forgotPasswordEmail by rememberSaveable { mutableStateOf("") }
    var forgotPasswordError by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    var isGoogleNoAccountError by rememberSaveable { mutableStateOf(false) }

    val isLoading = authUiState is AuthUiState.Loading

    // Navigate back automatically when authenticated
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Authenticated) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) {
                            stringResource(R.string.login_screen_title)
                        } else {
                            stringResource(R.string.signup_screen_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.action_back_to_home)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Tab Switcher (Sign In vs Sign Up)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_mode_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        emailError = null
                        passwordError = null
                        authViewModel.clearMessages()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_sign_in),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_sign_in")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        emailError = null
                        passwordError = null
                        authViewModel.clearMessages()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_sign_up),
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_sign_up")
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Messages & Feedback Banner
            AnimatedVisibility(
                visible = actionMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                actionMessage?.let { msg ->
                    AuthStatusBanner(
                        message = msg,
                        isError = false,
                        onDismiss = { authViewModel.clearMessages() }
                    )
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { err ->
                    AuthStatusBanner(
                        message = err,
                        isError = true,
                        isGoogleNoAccountError = isGoogleNoAccountError,
                        onAddAccount = {
                            runCatching {
                                context.startActivity(GoogleSignInHelper.createAddGoogleAccountIntent())
                            }.onFailure {
                                authViewModel.reportAuthError(
                                    AppError.Auth(
                                        code = "google_intent_failed",
                                        message = "تعذر فتح إعدادات الحسابات تلقائياً. يمكنك إضافة حساب Google من إعدادات جهازك أو المتابعة بالبريد."
                                    )
                                )
                            }
                        },
                        onSwitchToEmail = {
                            selectedTab = 0
                            authViewModel.clearMessages()
                            isGoogleNoAccountError = false
                            runCatching { focusRequester.requestFocus() }
                        },
                        onDismiss = {
                            authViewModel.clearMessages()
                            isGoogleNoAccountError = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium)
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text(stringResource(R.string.input_email_label)) },
                        placeholder = { Text(stringResource(R.string.input_email_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = LucideIcons.Mail,
                                contentDescription = stringResource(R.string.input_email_label)
                            )
                        },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        singleLine = true,
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(
                             keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("auth_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text(stringResource(R.string.input_password_label)) },
                        placeholder = { Text(stringResource(R.string.input_password_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = LucideIcons.Lock,
                                contentDescription = stringResource(R.string.input_password_label)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("auth_password_visibility_toggle")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) LucideIcons.Eye else LucideIcons.EyeOff,
                                    contentDescription = "تبديل إظهار كلمة المرور"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        singleLine = true,
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                submitAuth(
                                    isSignUp = selectedTab == 1,
                                    email = email,
                                    password = password,
                                    authViewModel = authViewModel,
                                    onEmailError = { emailError = it },
                                    onPasswordError = { passwordError = it }
                                )
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Forgot Password option (Only in Sign In mode)
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    forgotPasswordEmail = email
                                    forgotPasswordError = null
                                    showForgotPasswordDialog = true
                                },
                                shape = CircleShape,
                                modifier = Modifier.testTag("auth_forgot_password_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.action_forgot_password),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Primary Auth Button (Sign In / Sign Up)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submitAuth(
                                isSignUp = selectedTab == 1,
                                email = email,
                                password = password,
                                authViewModel = authViewModel,
                                onEmailError = { emailError = it },
                                onPasswordError = { passwordError = it }
                            )
                        },
                        enabled = !isLoading,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) {
                            AppCircularProgressIndicator(
                                size = 20.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                testTag = "auth_loading_indicator"
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) {
                                    stringResource(R.string.action_sign_in)
                                } else {
                                    stringResource(R.string.action_sign_up)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Divider "أو"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = stringResource(R.string.or_divider_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val result = googleSignInHelper.launchGoogleSignIn()
                                when (result) {
                                    is AppResult.Success -> {
                                        isGoogleNoAccountError = false
                                        authViewModel.signInWithGoogle(
                                            idToken = result.data.idToken,
                                            rawNonce = result.data.rawNonce
                                        )
                                    }
                                    is AppResult.Failure -> {
                                        val authError = result.error as? AppError.Auth
                                        val isCancelled = authError?.code == GoogleSignInHelper.ERROR_CODE_CANCELLED
                                        if (!isCancelled) {
                                            isGoogleNoAccountError = authError?.code == GoogleSignInHelper.ERROR_CODE_NO_ACCOUNT
                                            authViewModel.reportAuthError(result.error)
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_google_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = stringResource(R.string.action_google_sign_in),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Option to go back to homepage / continue as guest
            OutlinedButton(
                onClick = onNavigateBack,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_continue_guest_button")
            ) {
                Text(
                    text = stringResource(R.string.action_continue_as_guest),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }

    // Forgot Password Bottom Sheet
    if (showForgotPasswordDialog) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showForgotPasswordDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("forgot_password_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.large)
                    .padding(bottom = MaterialTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.forgot_password_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = forgotPasswordEmail,
                    onValueChange = {
                        forgotPasswordEmail = it
                        forgotPasswordError = null
                    },
                    label = { Text(stringResource(R.string.input_email_label)) },
                    placeholder = { Text(stringResource(R.string.input_email_placeholder)) },
                    singleLine = true,
                    isError = forgotPasswordError != null,
                    supportingText = forgotPasswordError?.let { { Text(it) } },
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_password_email_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false },
                        shape = CircleShape,
                        modifier = Modifier.testTag("forgot_password_cancel_button")
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Button(
                        onClick = {
                            val emailValidation = AuthValidator.validateEmail(forgotPasswordEmail)
                            if (emailValidation is AppResult.Failure) {
                                forgotPasswordError = emailValidation.error.message
                                return@Button
                            }
                            authViewModel.resetPassword(forgotPasswordEmail)
                            showForgotPasswordDialog = false
                        },
                        shape = CircleShape,
                        modifier = Modifier.testTag("forgot_password_submit_button")
                    ) {
                        Text(stringResource(R.string.action_send_reset_link))
                    }
                }
            }
        }
    }
}

private fun submitAuth(
    isSignUp: Boolean,
    email: String,
    password: String,
    authViewModel: AuthViewModel,
    onEmailError: (String) -> Unit,
    onPasswordError: (String) -> Unit
) {
    val emailValidation = AuthValidator.validateEmail(email)
    if (emailValidation is AppResult.Failure) {
        onEmailError(emailValidation.error.message)
        return
    }

    val passwordValidation = AuthValidator.validatePassword(password)
    if (passwordValidation is AppResult.Failure) {
        onPasswordError(passwordValidation.error.message)
        return
    }

    if (isSignUp) {
        authViewModel.signUpWithEmail(email, password)
    } else {
        authViewModel.signInWithEmail(email, password)
    }
}

@Composable
private fun AuthStatusBanner(
    message: String,
    isError: Boolean,
    isGoogleNoAccountError: Boolean = false,
    onAddAccount: () -> Unit = {},
    onSwitchToEmail: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val bgColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val icon = if (isError) LucideIcons.AlertTriangle else LucideIcons.CheckCircle2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_feedback_banner"),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isError) "خطأ" else "نجاح",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Text("✕", color = textColor, fontWeight = FontWeight.Bold)
                }
            }

            if (isGoogleNoAccountError) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Button(
                        onClick = onAddAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auth_banner_add_google_account_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_google_add_account),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onSwitchToEmail,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auth_banner_switch_to_email_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_switch_to_email),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
