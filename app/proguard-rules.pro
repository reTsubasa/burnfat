# Add project specific ProGuard rules here.
-keep class com.burnfat.data.local.entity.** { *; }
-keep class com.burnfat.data.remote.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Kotlinx Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.burnfat.**$$serializer { *; }
-keepclassmembers class com.burnfat.** {
    *** Companion;
}
-keepclasseswithmembers class com.burnfat.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Security (Tink)
-dontwarn com.google.errorprone.annotations.**