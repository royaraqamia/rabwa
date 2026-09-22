package com.royaraqamia.rabwa.presentation.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.royaraqamia.rabwa.core.config.SupabaseConfig
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

/**
 * Enterprise Google Sign-In helper leveraging Android Credential Manager.
 * Handles nonce generation, SHA-256 hashing, credential retrieval, token parsing,
 * and graceful fallback for devices without Google accounts or Play Services.
 */
open class GoogleSignInHelper(
    private val context: Context,
    private val credentialManager: CredentialManager = CredentialManager.create(context)
) {

    data class GoogleSignInPayload(
        val idToken: String,
        val rawNonce: String
    )

    companion object {
        const val ERROR_CODE_NO_ACCOUNT = "google_no_account"
        const val ERROR_CODE_CANCELLED = "google_signin_cancelled"
        const val ERROR_CODE_CONFIG_MISSING = "google_config_missing"
        const val ERROR_CODE_PROVIDER_UNAVAILABLE = "google_provider_unavailable"
        const val ERROR_CODE_INTERRUPTED = "google_signin_interrupted"
        const val ERROR_CODE_FAILED = "google_signin_failed"

        /**
         * Creates an Intent to open the Android system settings for adding a Google Account.
         */
        fun createAddGoogleAccountIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    open suspend fun launchGoogleSignIn(serverClientId: String? = null): AppResult<GoogleSignInPayload> {
        val config = SupabaseConfig.loadFromBuildConfig()
        val clientId = serverClientId ?: config.googleWebClientId
        if (clientId.isNullOrBlank()) {
            return AppResult.Failure(
                AppError.Auth.configurationMissing(
                    "يتطلب تسجيل الدخول عبر Google إضافة GOOGLE_WEB_CLIENT_ID في لوحة الإعدادات (Secrets). يمكنك المتابعة بالبريد الإلكتروني أو الاستمرار كزائر."
                )
            )
        }

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return runCatching {
            val response = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    if (idToken.isBlank()) {
                        throw IllegalArgumentException("Google ID Token returned by Credential Manager is empty.")
                    }
                    GoogleSignInPayload(
                        idToken = idToken,
                        rawNonce = rawNonce
                    )
                } catch (e: GoogleIdTokenParsingException) {
                    throw IllegalArgumentException("تعذر استخراج بيانات حساب Google: ${e.message}", e)
                }
            } else {
                throw IllegalStateException("نوع بيانات الاعتماد المستلمة غير متوافق مع Google.")
            }
        }.fold(
            onSuccess = { payload -> AppResult.Success(payload) },
            onFailure = { throwable ->
                when (throwable) {
                    is GetCredentialCancellationException -> {
                        AppResult.Failure(
                            AppError.Auth(
                                code = ERROR_CODE_CANCELLED,
                                message = "تم إلغاء عملية تسجيل الدخول بـ Google."
                            )
                        )
                    }
                    is NoCredentialException -> {
                        AppResult.Failure(
                            AppError.Auth(
                                code = ERROR_CODE_NO_ACCOUNT,
                                message = "لم يتم العثور على حساب Google مناسب في هذا الجهاز. يرجى استخدام البريد الإلكتروني أو إضافة حساب Google."
                            )
                        )
                    }
                    is GetCredentialProviderConfigurationException -> {
                        AppResult.Failure(
                            AppError.Auth(
                                code = ERROR_CODE_PROVIDER_UNAVAILABLE,
                                message = "خدمات Google Play غير مهيأة أو غير متوفرة حالياً على هذا الجهاز. يمكنك تسجيل الدخول بالبريد الإلكتروني."
                            )
                        )
                    }
                    is GetCredentialInterruptedException -> {
                        AppResult.Failure(
                            AppError.Auth(
                                code = ERROR_CODE_INTERRUPTED,
                                message = "تمت مقاطعة الاتصال بخدمات Google. يرجى إعادة المحاولة."
                            )
                        )
                    }
                    else -> {
                        AppResult.Failure(
                            AppError.Auth(
                                code = ERROR_CODE_FAILED,
                                message = throwable.localizedMessage ?: "تعذر إتمام تسجيل الدخول عبر Google. يرجى المحاولة لاحقاً."
                            )
                        )
                    }
                }
            }
        )
    }
}


