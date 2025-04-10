package com.dragon.wheels.tools

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
动态网络注册*/
object NetworkStateManager {
    private lateinit var connectivityManager: ConnectivityManager
    private val networkStateFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            registerNetworkCallback()
        } else {
//            throw UnsupportedOperationException("API level 21 and above is required for network state monitoring.")
        }
    }

    fun getNetworkState(): StateFlow<NetworkStatus> {
        return networkStateFlow
    }

    @RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    private fun registerNetworkCallback() {
        val builder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                checkNetworkCapability(network)
            }

            override fun onLost(network: Network) {
                networkStateFlow.value = NetworkStatus.Unavailable
            }

            override fun onUnavailable() {
                networkStateFlow.value = NetworkStatus.Unavailable
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                checkNetworkCapability(network)
            }
        }

        connectivityManager.registerNetworkCallback(builder, networkCallback)
    }

    private fun checkNetworkCapability(network: Network) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        if (networkCapabilities != null) {
            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    networkStateFlow.value = NetworkStatus.Available(NetworkType.Wifi)
                }
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    networkStateFlow.value = NetworkStatus.Available(NetworkType.Cellular)
                }
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    networkStateFlow.value = NetworkStatus.Available(NetworkType.Ethernet)
                }
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                    networkStateFlow.value = NetworkStatus.Available(NetworkType.Bluetooth)
                }
                else -> {
                    networkStateFlow.value = NetworkStatus.Available(NetworkType.Unknown)
                }
            }
        } else {
            networkStateFlow.value = NetworkStatus.Unavailable
        }
    }
}

sealed class NetworkStatus {
    object Unavailable : NetworkStatus()
    data class Available(val type: NetworkType) : NetworkStatus()
}

enum class NetworkType {
    Wifi,
    Cellular,
    Ethernet,
    Bluetooth,
    Unknown
}