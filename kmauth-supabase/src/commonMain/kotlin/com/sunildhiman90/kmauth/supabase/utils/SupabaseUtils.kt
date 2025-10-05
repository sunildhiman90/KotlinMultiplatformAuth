package com.sunildhiman90.kmauth.supabase.utils

import com.sunildhiman90.kmauth.core.KMAuthSupabaseFlowType
import io.github.jan.supabase.auth.FlowType

fun KMAuthSupabaseFlowType.toFlowType(): FlowType = when (this) {
    KMAuthSupabaseFlowType.IMPLICIT -> FlowType.IMPLICIT
    KMAuthSupabaseFlowType.PKCE -> FlowType.PKCE
}
