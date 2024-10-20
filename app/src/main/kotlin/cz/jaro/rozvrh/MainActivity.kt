package cz.jaro.rozvrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.lifecycle.coroutineScope
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    Modifier.fillMaxSize()
                ) {
                    Column {
                        Text("Tato aplikace je zastaralá", style = MaterialTheme.typography.headlineLarge)
                        Text("Od poslední verze (2.5.0) je tato aplikace Multiplatform, což znamená, že funguje i v prohlížeči, na webu.")
                        Text("Bohužel s tím ale také přišlo to, že mobilní verze změnila svůj package name, takže je nutné:")
                        TextButton(
                            onClick = {
                                lifecycle.coroutineScope.launch(Dispatchers.IO) {
                                    val document = try {
                                        withContext(Dispatchers.IO) {
                                            Ksoup.parseGetRequest("https://raw.githubusercontent.com/jaro-jaro/gymceska-multiplatform/main/composeApp/version.txt")
                                        }
                                    } catch (e: SocketTimeoutException) {
                                        return@launch
                                    }

                                    val nejnovejsiVerze = document.text()

                                    CustomTabsIntent.Builder()
                                        .setShowTitle(true)
                                        .build()
                                        .launchUrl(this@MainActivity, Uri.parse("https://github.com/jaro-jaro/gymceska-multiplatform/releases/download/v$nejnovejsiVerze/Gymceska-$nejnovejsiVerze.apk"))
                                }
                            }
                        ) {
                            Text("1. Stáhnout si novou aplikaci")
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DELETE)
                                intent.setData(Uri.parse("package:cz.jaro.rozvrh"))
                                startActivity(intent)
                            }
                        ) {
                            Text("2. Odinstalovat tuto, starou")
                        }
                    }
                }
            }
        }
    }
}
