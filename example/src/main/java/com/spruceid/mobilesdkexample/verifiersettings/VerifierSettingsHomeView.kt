package com.spruceid.mobilesdkexample.verifiersettings

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.Primary
import com.spruceid.mobilesdkexample.ui.theme.TextBody
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.TextOnPrimary
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeBinaryBorder
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeBinaryFill
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeBinaryText
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeFieldBorder
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeFieldFill
import com.spruceid.mobilesdkexample.ui.theme.VerifierRequestBadgeFieldText

enum class VerifierSubSettings {
    VERIFICATION_ACTIVITY_LOG,
}

@Composable
fun VerifierSettingsHomeView(
    navController: NavController
) {

    var subpage by remember {
        mutableStateOf<VerifierSubSettings?>(null)
    }

    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        VerifierSettingsHomeHeader(
            onBack = {
                if(subpage != null) {
                    subpage = null
                } else {
                    navController.popBackStack()
                }
            }
        )
        VerifierSettingsHomeBody(
            subpage = subpage,
            changeSubPage = { sp ->
                subpage = sp
            }
        )
    }
}

@Composable
fun VerifierSettingsHomeHeader(
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onBack()
        }
    ) {
        Image(
            painter = painterResource(id = R.drawable.chevron),
            contentDescription = stringResource(id = R.string.chevron),
            modifier = Modifier
                .rotate(180f)
                .scale(0.7f)
        )
        Text(
            text = "Verifier Settings",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = TextHeader,
            modifier = Modifier.padding(start = 10.dp)
        )
        Spacer(Modifier.weight(1f))
    }

}

@Composable
fun VerifierSettingsHomeBody(
    subpage: VerifierSubSettings?,
    changeSubPage: (VerifierSubSettings?) -> Unit
) {
    if(subpage == null) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        changeSubPage(VerifierSubSettings.VERIFICATION_ACTIVITY_LOG)
                    },
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                Icons.AutoMirrored.Outlined.List,
                                contentDescription = stringResource(id = R.string.verification_activity_log),
                                modifier = Modifier.padding(end = 5.dp),
                            )
                            Text(
                                text = "Verification Activity Log",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextBody,
                                modifier = Modifier.padding(bottom = 5.dp, top = 5.dp),
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.chevron),
                            contentDescription = stringResource(id = R.string.chevron),
                            modifier = Modifier.scale(0.5f)
                        )
                    }

                    Text(
                        text = "view and export verification history",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextBody,
                    )
                }
            }
        }
    } else if(subpage == VerifierSubSettings.VERIFICATION_ACTIVITY_LOG) {
        VerificationActivityLogsScreen()
    }
}

@Composable
fun VerifierListItem(
    title: String,
    description: String,
    binary: Boolean,
    fields: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextHeader,
                modifier = Modifier.weight(2f)
            )
            VerifierListItemTag(binary = binary, fields = fields)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = stringResource(id = R.string.arrow_right),
                modifier = Modifier.width(24.dp)
            )
        }
        Text(
            text = description,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextBody,
        )
        HorizontalDivider()
    }
}

@Composable
fun VerifierListItemTag(
    binary: Boolean,
    fields: Int
) {
    if (binary) {
        Text(
            text = "Binary",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = VerifierRequestBadgeBinaryText,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = VerifierRequestBadgeBinaryBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp))
                .background(VerifierRequestBadgeBinaryFill)
                .padding(vertical = 2.dp)
                .padding(horizontal = 8.dp),
        )
    } else {
        Text(
            text = "$fields Fields",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = VerifierRequestBadgeFieldText,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = VerifierRequestBadgeFieldBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp))
                .background(VerifierRequestBadgeFieldFill)
                .padding(vertical = 2.dp)
                .padding(horizontal = 8.dp),
        )
    }
}