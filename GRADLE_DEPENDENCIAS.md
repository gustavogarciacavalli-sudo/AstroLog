# Configuração (Android Studio -> New Project -> "Empty Activity", minSdk 26)

1. Nome do pacote do projeto: `br.edu.up.astrolog` (se usar outro, troque a linha `package` dos 5 arquivos .kt).
2. Apague o MainActivity.kt gerado e copie os 5 arquivos .kt desta pasta para `app/src/main/java/<seu pacote>/`.
3. AndroidManifest.xml: adicione, acima de `<application>`:
   `<uses-permission android:name="android.permission.INTERNET"/>`
4. `app/build.gradle.kts`:

plugins {
    // ...os que já existem...
    id("com.google.devtools.ksp") version "<versão compatível com o Kotlin do seu projeto>"
    // Veja a versão certa em https://github.com/google/ksp/releases (formato <kotlin>-<ksp>)
}

dependencies {
    // ...os que já existem (compose, material3, etc.)...
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}

5. Sync Project -> Run no emulador (o emulador precisa de internet para a API).
