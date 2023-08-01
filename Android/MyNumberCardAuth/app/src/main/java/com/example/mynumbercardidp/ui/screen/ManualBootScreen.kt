package com.example.mynumbercardidp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mynumbercardidp.R
import com.example.mynumbercardidp.data.URLTypes
import com.example.mynumbercardidp.ui.theme.MyNumberCardAuthTheme

@Composable
fun ManualBootScreen(
) {
    val uriHandler = LocalUriHandler.current
    val logTag = "ManualBootScreen"

    Log.d(logTag, "ManualBootScreen is View.")

    Column (
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        ){
        Text(stringResource(R.string.about_this_app),modifier = Modifier.padding(50.dp))

        Text(stringResource(R.string.about_this_app_detail))

        Button(
            onClick = {
                uriHandler.openUri(URLTypes.TermsOfService.toString())
            },
            modifier = Modifier.padding(10.dp)
        ) {
            Text(stringResource(R.string.terms_of_use))
        }

        Button(
            onClick = {
                uriHandler.openUri(URLTypes.PrivacyPolicy.toString())
            },
            modifier = Modifier.padding(10.dp)
        ) {
            Text(stringResource(R.string.privacy_policy))
        }

        Button(
            onClick = {
                uriHandler.openUri(URLTypes.ProtectionPolicy.toString())
            },
            modifier = Modifier.padding(10.dp)
        ) {
            Text(stringResource(R.string.person_Info_protection_policy))
        }
    }
}

@Preview(
    showBackground = true)
@Composable
fun ManualBootScreenPreview() {
    MyNumberCardAuthTheme {
        ManualBootScreen()
    }
}
