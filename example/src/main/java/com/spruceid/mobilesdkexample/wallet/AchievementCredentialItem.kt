package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.ui.theme.Bg
import com.spruceid.mobilesdkexample.ui.theme.CredentialBorder
import com.spruceid.mobilesdkexample.ui.theme.GreenValid
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.TextBody
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.utils.mockAchievementCredential
import org.json.JSONObject

class AchievementCredentialItem {
    private var credential: JSONObject

    constructor(credential: JSONObject) {
        this.credential = credential
    }

    constructor(rawCredential: String) {
        // @TODO: remove mock
        this.credential = JSONObject(mockAchievementCredential)
    }

    @Composable
    fun listComponent() {
        Row(
            Modifier.height(intrinsicSize = IntrinsicSize.Max)
        ) {
            // Leading icon
            Column {
                // Title
                Text(
                    text = "[Team Membership]",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp,
                    color = TextHeader,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Description
                Column {
                    Text(
                        text = "[Development Council]",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextBody
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.valid),
                            contentDescription = stringResource(id = R.string.valid),
                            modifier = Modifier.width(15.dp)
                        )
                        Text(
                            text = "Valid",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = GreenValid
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
            // Trailing action button
        }
    }

    @Composable
    fun detailsComponent() {
        Row(
            Modifier.padding(horizontal = 12.dp)
        ) {
            Column {
                listOf(1, 2, 3, 4).map {
                    Text(
                        text = "[Field]",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextBody,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "[Value]"
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
    }

    @Composable
    fun borderedListComponent() {
        Box(
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = CredentialBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            listComponent()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun component() {
        var sheetOpen by remember {
            mutableStateOf(false)
        }

        Column(
            Modifier
                .padding(vertical = 10.dp)
                .border(
                    width = 1.dp,
                    color = CredentialBorder,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Box(
                Modifier
                    .padding(all = 12.dp)
                    .clickable {
                        sheetOpen = true
                    }
            ) {
//                GenericCredentialListItem(credentialPack = credentialPack)
                listComponent()
            }
        }
        if (sheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    sheetOpen = false
                },
                modifier = Modifier
                    .fillMaxHeight(0.8f),
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = null,
                containerColor = Bg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    Modifier
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Review Info",
                        textAlign = TextAlign.Center,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextHeader,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                    )
                    borderedListComponent()
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f, false)
                    ) {
                        detailsComponent()
                    }
                }
            }
        }
    }
}