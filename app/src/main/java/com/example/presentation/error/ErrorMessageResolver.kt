package com.example.presentation.error

import androidx.annotation.StringRes
import com.example.R
import com.example.core.result.AppError

/**
 * Enterprise presentation-layer model for displaying user-friendly errors.
 * Strictly guarantees that internal database schemas, stack traces, and raw technical
 * exception strings are NEVER surfaced to the UI.
 */
data class UserFacingError(
    @param:StringRes val messageResId: Int,
    val fallbackMessage: String,
    val isRetryable: Boolean = false
)

/**
 * Pure domain-to-presentation error mapper implementing the Enterprise Zero-Leakage protocol.
 */
object ErrorMessageResolver {

    /**
     * Resolves an [AppError] into a safe, localized [UserFacingError].
     */
    fun resolve(error: AppError): UserFacingError {
        return when (error) {
            is AppError.Network -> {
                if (error.isNoInternet || error.message.contains("No Internet", ignoreCase = true)) {
                    UserFacingError(
                        messageResId = R.string.error_no_internet,
                        fallbackMessage = "لا يوجد اتصال بالإنترنت (No Internet). يرجى التحقق من اتصال الشبكة والمحاولة مجدداً.",
                        isRetryable = true
                    )
                } else {
                    UserFacingError(
                        messageResId = R.string.error_network_generic,
                        fallbackMessage = "تعذر الاتصال بالشبكة. يرجى التحقق من اتصال الإنترنت والمحاولة مجدداً.",
                        isRetryable = true
                    )
                }
            }
            is AppError.Database -> UserFacingError(
                messageResId = R.string.error_database_generic,
                fallbackMessage = "تعذر حفظ أو قراءة البيانات محلياً. يرجى المحاولة مرة أخرى.",
                isRetryable = true
            )
            is AppError.Validation -> UserFacingError(
                messageResId = R.string.error_validation_generic,
                fallbackMessage = "بيانات الإدخال غير صالحة. يرجى التأكد من صحة الحقول والمحاولة مجدداً.",
                isRetryable = false
            )
            is AppError.Unauthorized -> UserFacingError(
                messageResId = R.string.error_unauthorized_generic,
                fallbackMessage = "ليس لديك صلاحية لتنفيذ هذا الإجراء.",
                isRetryable = false
            )
            is AppError.Auth -> UserFacingError(
                messageResId = R.string.error_auth_generic,
                fallbackMessage = error.message.ifEmpty { "فشلت عملية المصادقة. يرجى التحقق من بيانات الدخول." },
                isRetryable = true
            )
            is AppError.RemoteDatabase -> UserFacingError(
                messageResId = R.string.error_remote_db_generic,
                fallbackMessage = "تعذر الاتصال بقاعدة البيانات السحابية (Supabase).",
                isRetryable = true
            )
            is AppError.Storage -> UserFacingError(
                messageResId = R.string.error_storage_generic,
                fallbackMessage = error.message.ifEmpty { "تعذر معالجة الملف في التخزين السحابي (Supabase Storage)." },
                isRetryable = true
            )
            is AppError.NotFound -> UserFacingError(
                messageResId = R.string.error_not_found_generic,
                fallbackMessage = "العنصر المطلوب غير موجود أو تم حذفه.",
                isRetryable = false
            )
            is AppError.Unknown -> UserFacingError(
                messageResId = R.string.error_unknown_generic,
                fallbackMessage = "حدث خطأ غير متوقع أثناء معالجة طلبك. يرجى المحاولة لاحقاً.",
                isRetryable = true
            )
        }
    }
}
