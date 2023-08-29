package com.example.mynumbercardidp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.ui.theme.MyNumberCardAuthTheme
import com.example.mynumbercardidp.ui.viewmodel.StateViewModel

@Composable
fun ManualBootScreen(
    viewModel: StateViewModel
) {
    val receivedState by viewModel.uiState.collectAsState()
    Column (
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        ){
        Text(stringResource(R.string.about_this_app),modifier = Modifier.padding(50.dp))

        Text(stringResource(R.string.about_this_app_detail))

        // 利用規約
        OpenUrlButton(
            receivedState.externalUrls?.termsOfUse!!,
            stringResource(R.string.terms_of_use)
        )

        // プライバシーポリシー
        OpenUrlButton(
            receivedState.externalUrls?.privacyPolicy!!,
            stringResource(R.string.privacy_policy)
        )

        // 個人情報保護方針
        OpenUrlButton(
            receivedState.externalUrls?.protectionPolicy!!,
            stringResource(R.string.person_Info_protection_policy)
        )
    }
}

@Composable
private fun OpenUrlButton(url: String, buttonText:String){
    val uriHandler = LocalUriHandler.current
    Button(
        onClick = {
            uriHandler.openUri(url)
        },
        modifier = Modifier.padding(10.dp)
    ) {
        Text(buttonText)
    }
}

@Preview(
    showBackground = true)
@Composable
fun ManualBootScreenPreview() {
    MyNumberCardAuthTheme {
        ManualBootScreen(viewModel(factory = StateViewModel.Factory))
    }
}
