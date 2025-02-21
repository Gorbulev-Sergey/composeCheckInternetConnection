package ru.gorbulevsv.composecheckinternetconnection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.gorbulevsv.composecheckinternetconnection.ui.theme.ComposeCheckInternetConnectionTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    var message = mutableStateOf("")

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Проверяем подключение к интернет (это делается до вызова UI
            val connectivityManager = getSystemService(ConnectivityManager::class.java)
            connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                // Интернет доступен
                override fun onAvailable(network: Network) {
                    //"Теперь используется сеть по умолчанию: ${network}\n"
                }

                // Интернет потерян
                override fun onLost(network: Network) {
                    //"У приложения больше нет сети по умолчанию. Последней сетью по умолчанию была ${network}\n"
                }

                // Состояние сети изменилось
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    //"Возможности сети по умолчанию изменились: ${networkCapabilities}\n" + networkCapabilities
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    //"Сеть по умолчанию изменила свойства соединения: ${linkProperties}\n" + linkProperties
                }
            })

            ComposeCheckInternetConnectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {

                    Text(
                        text = message.value,
                        modifier = Modifier.padding(it)
                    )

                }
            }
        }
    }
}

