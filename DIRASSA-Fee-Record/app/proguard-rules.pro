# Add project specific ProGuard rules here.

# Room Database — keep entity classes
-keep class com.dirassa.feerecord.data.entity.** { *; }
-keep class com.dirassa.feerecord.data.dao.** { *; }

# iText PDF library
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn org.w3c.dom.**

# ViewBinding
-keep class **.databinding.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
