package com.example.mynumbercardidp

import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynumbercardidp.ui.ExternalUrls
import com.example.mynumbercardidp.ui.screen.CertReadScreen
import com.example.mynumbercardidp.ui.screen.ManualBootScreen
import com.example.mynumbercardidp.ui.ScreenModeState
import com.example.mynumbercardidp.ui.UriParameters
import com.example.mynumbercardidp.ui.theme.MyNumberCardAuthTheme
import com.example.mynumbercardidp.ui.viewmodel.StateViewModel
import java.util.Properties

class MainActivity : ComponentActivity() {
    var nfcAdapter: NfcAdapter? = null
    var activity = this@MainActivity
    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        var uriParameters = getUriParameters(intent.data)
        Log.i(logTag, "uriParameters: ${uriParameters.toString()}")
        var screenMode = getScreenMode(uriParameters)
        Log.i(logTag, "screenMode: $screenMode")
        var externalUrls = getExternalUrls()
        Log.i(logTag, "externalUrls: $externalUrls")

        setContent {
            MyNumberCardAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background,
                ) {
                    val viewModel: StateViewModel = viewModel(factory = StateViewModel.Factory)
                    if (screenMode == ScreenModeState.ManualBoot){
                        viewModel.setExternalUrls(externalUrls)
                        ManualBootScreen(viewModel)
                    }
                    else {
                        viewModel.changeViewMode(screenMode)
                        viewModel.setUriParameters(uriParameters)
                        viewModel.setExternalUrls(externalUrls)
                        CertReadScreen(nfcAdapter, activity, viewModel)
                    }
                }
            }
        }
    }

    private fun getScreenMode(uriParameters: UriParameters?): ScreenModeState {
        return if (uriParameters == null){
            ScreenModeState.ManualBoot
        }else{
            if (uriParameters.mode == "login"){
                ScreenModeState.UserCertRead
            } else {
                ScreenModeState.SignCertRead
            }
        }
    }

    private fun getUriParameters(data: Uri?): UriParameters? {
        return if (!data?.getQueryParameter("nonce").isNullOrEmpty()){
            UriParameters(
                data?.getQueryParameter("nonce"),
                data?.getQueryParameter("mode"),
                data?.getQueryParameter("action_url"),
                data?.getQueryParameter("error_url")
            )
        }
        else {
            null
        }
    }

    private fun getExternalUrls():ExternalUrls {
        val properties = Properties()
        val inputStream = assets.open("external_urls.properties")
        properties.load(inputStream)
        inputStream.close()

        return ExternalUrls(
            properties.getProperty("inquiry_url"),
            properties.getProperty("privacy_policy_url"),
            properties.getProperty("protection_policy_url"),
            properties.getProperty("terms_of_use_url")
        )
    }
}
