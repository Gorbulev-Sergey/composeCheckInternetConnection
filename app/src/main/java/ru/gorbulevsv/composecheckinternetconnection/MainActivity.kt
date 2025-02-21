package ru.gorbulevsv.composecheckinternetconnection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
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
    var isInternetConnected = mutableStateOf(false)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeCheckInternetConnectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {

                    // Работа данного функционала взята отсюда: https://rommansabbir.com/monitor-internet-connectivity-in-jetpack-compose

                    MonitorNetworkStatus { result ->
                        isInternetConnected.value = result
                    }

                    Text(
                        text = if (isInternetConnected.value) "Связь есть" else "Связи нет",
                        modifier = Modifier.padding(it)
                    )

                }
            }
        }
    }
}

@Composable
fun MonitorNetworkStatus(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNetworkStatusChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isNetworkFunctional by remember { mutableStateOf(context.isInternetFunctional()) }
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    DisposableEffect(lifecycleOwner) {
        // Наблюдатель жизненного цикла для обработки событий жизненного цикла
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("Мониторинг состояния сети", "Запущено событие ON_RESUME.")
                    try {
                        // Update the network status on resume
                        val currentNetworkStatus = context.isInternetFunctional()
                        if (isNetworkFunctional != currentNetworkStatus) {
                            isNetworkFunctional = currentNetworkStatus
                            Log.d(
                                "Мониторинг состояния сети",
                                "Состояние проверки сети: $isNetworkFunctional"
                            )
                            onNetworkStatusChanged(isNetworkFunctional)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "Мониторинг состояния сети",
                            "Исключение, проверяющее состояние сети: ${e.localizedMessage}",
                            e
                        )
                    }
                }

                else -> {
                    Log.d(
                        "Мониторинг состояния сети",
                        "Необработанное событие жизненного цикла: $event"
                    )
                }
            }
        }

        // Обратный вызов по сети для отслеживания изменений в сети
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                val isFunctional =
                    networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (isFunctional != isNetworkFunctional) {
                    isNetworkFunctional = isFunctional
                    Log.d(
                        "Мониторинг состояния сети",
                        "Изменено состояние проверки сети: $isNetworkFunctional"
                    )
                    onNetworkStatusChanged(isNetworkFunctional)
                }
            }

            override fun onLost(network: Network) {
                if (isNetworkFunctional) {
                    isNetworkFunctional = false
                    Log.d("Мониторинг состояния сети", "Потеряно сетевое соединение")
                    onNetworkStatusChanged(false)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isFunctional =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (isFunctional != isNetworkFunctional) {
                    isNetworkFunctional = isFunctional
                    Log.d(
                        "Мониторинг состояния сети",
                        "Изменились сетевые возможности: $isNetworkFunctional"
                    )
                    onNetworkStatusChanged(isNetworkFunctional)
                }
            }
        }

        // Зарегистрируйте обратный вызов по сети, чтобы отслеживать изменения в подключении к Интернету
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Добавьте наблюдателя в жизненный цикл
        lifecycleOwner.lifecycle.addObserver(observer)
        Log.d("Мониторинг состояния сети", "Наблюдатель добавлен в жизненный цикл")

        // Очистка при утилизации
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.d("Мониторинг состояния сети", "Удален наблюдатель и сетевой обратный вызов")
        }
    }
}

fun Context.isInternetFunctional(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

@Composable
fun MyAppScreen() {
    MonitorNetworkStatus { isNetworkFunctional ->
        if (isNetworkFunctional) {
            // Сеть присутствует
            Log.d("Экран моего приложения", "Интернет функционирует и проверен")
        } else {
            Log.d("Экран моего приложения", "Интернет не работает и не проверен")
            // Сеть отсутствует
        }
    }

    // Другой контент пользовательского интерфейса
}