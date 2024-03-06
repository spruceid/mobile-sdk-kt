package com.spruceid.walletsdkexample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spruceid.walletsdkexample.ui.theme.WalletSdkTheme

@Composable
fun NamespaceField(namespace: Map.Entry<String, Boolean>, isChecked: Boolean, onCheck: (Boolean) -> Unit) {
    WalletSdkTheme {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(namespace.key)
            Checkbox(
                isChecked,
                onCheckedChange = onCheck
            )
        }
    }
}