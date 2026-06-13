# Keep Room entities
  -keep class com.aivos.echovault.data.db.** { *; }
  -keep class com.aivos.echovault.domain.model.** { *; }
  -keepattributes Signature
  -keepattributes *Annotation*
  -dontwarn kotlinx.**
  -dontwarn javax.annotation.**
  