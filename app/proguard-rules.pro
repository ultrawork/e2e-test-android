# Add project specific ProGuard rules here.

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.ultrawork.notes.model.** { *; }
-keep class com.ultrawork.notes.data.remote.** { *; }
-keep class com.ultrawork.notes.data.model.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
