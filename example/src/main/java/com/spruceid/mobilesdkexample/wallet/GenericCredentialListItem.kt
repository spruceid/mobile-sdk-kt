package com.spruceid.mobilesdkexample.wallet

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spruceid.mobile.sdk.CredentialPack
import com.spruceid.mobile.sdk.rs.JsonVc
import com.spruceid.mobile.sdk.rs.vcToSignedVp
import com.spruceid.mobile.sdk.ui.BaseCard
import com.spruceid.mobile.sdk.ui.CardRenderingDetailsField
import com.spruceid.mobile.sdk.ui.CardRenderingDetailsView
import com.spruceid.mobile.sdk.ui.CardRenderingListView
import com.spruceid.mobile.sdk.ui.toCardRendering
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.rememberQrBitmapPainter
import com.spruceid.mobilesdkexample.ui.theme.Bg
import com.spruceid.mobilesdkexample.ui.theme.CredentialBorder
import com.spruceid.mobilesdkexample.ui.theme.GreenValid
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.Primary
import com.spruceid.mobilesdkexample.ui.theme.TextBody
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.TextOnPrimary
import com.spruceid.mobilesdkexample.utils.ed25519_2020_10_18
import com.spruceid.mobilesdkexample.utils.small_vc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericCredentialListItems(
    vc_json: String
) {
    val credentialPack = CredentialPack()
    credentialPack.addJsonVc(JsonVc.newFromJson(vc_json))

    var sheetOpen by remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetOpen = false
            },
            modifier = Modifier
                .fillMaxHeight(0.8f),
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Bg,
            shape = RoundedCornerShape(8.dp)
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
            Column(
                Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
                    GenericCredentialListItem(credentialPack = credentialPack)
                }
                GenericCredentialDetailsItem(credentialPack = credentialPack)
            }
        }
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
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
                .clickable {
                    sheetOpen = true
                }
        ) {
            GenericCredentialListItem(credentialPack = credentialPack)
        }
        GenericCredentialListItemQRCode()
    }
}

@Composable
fun GenericCredentialDetailsItem(credentialPack: CredentialPack) {
    val detailsRendering = CardRenderingDetailsView(
        fields = listOf(
            CardRenderingDetailsField(
                // it's also possible just request the credentialSubject and cast it to JSONObject
                keys = listOf(
                    "credentialSubject.image",
                    "credentialSubject.givenName",
                    "credentialSubject.familyName",
                    "credentialSubject.birthDate",
                    "credentialSubject.driversLicense",
                    "credentialSubject.driversLicense.portrait",
                    "credentialSubject.driversLicense.given_name",
                    "credentialSubject.driversLicense.family_name",
                    "credentialSubject.driversLicense.birth_date",
                ),
                formatter = {values ->
                    val w3cvc = values.toList()
                        .first { (key, _) ->
                            val credential = credentialPack.getCredentialById(key)
                            (credential?.asJwtVc() != null || credential?.asJsonVc() != null)
                        }.second

                    var portrait = ""
                    var firstName = ""
                    var lastName = ""
                    var birthDate = ""
                    if(w3cvc["credentialSubject.driversLicense"].toString().isNotEmpty()) {
                        portrait = w3cvc["credentialSubject.driversLicense.portrait"].toString()
                        firstName = w3cvc["credentialSubject.driversLicense.given_name"].toString()
                        lastName = w3cvc["credentialSubject.driversLicense.family_name"].toString()
                        birthDate = w3cvc["credentialSubject.driversLicense.birth_date"].toString()
                    } else {
                        portrait = w3cvc["credentialSubject.image"].toString()
                        firstName = w3cvc["credentialSubject.givenName"].toString()
                        lastName = w3cvc["credentialSubject.familyName"].toString()
                        birthDate = w3cvc["credentialSubject.birthDate"].toString()
                    }

                    Row {
                        val byteArray = Base64.decode(
                            portrait
                                .replace("data:image/png;base64,", ""),
                            Base64.DEFAULT
                        ).apply {
                            BitmapFactory.decodeByteArray(this, 0, size)
                        }
                        Column(
                            Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Portrait",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextBody,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            BitmapImage(
                                byteArray,
                                contentDescription =  w3cvc["issuer.name"].toString(),
                                modifier = Modifier
                                    .width(90.dp)
                                    .padding(end = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        Column {
                            Text(
                                text = "First Name",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextBody
                            )
                            Text(
                                text = firstName,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Text(
                                text = "Last Name",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextBody
                            )
                            Text(
                                text = lastName,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Text(
                                text = "Birth Date",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextBody
                            )
                            Text(
                                text = birthDate,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                    }
                }
            ),
            CardRenderingDetailsField(
                keys = listOf("issuanceDate"),
                formatter = { values ->
                    val w3cvc = values.toList()
                        .first { (key, _) ->
                            val credential = credentialPack.getCredentialById(key)
                            (credential?.asJwtVc() != null || credential?.asJsonVc() != null)
                        }.second

                    Row {
                        Column {
                            Text(
                                text = "Issuance Date",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextBody
                            )
                            Text(
                                text = w3cvc["issuanceDate"].toString(),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                    }
                }
            )
        )
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        BaseCard(
            credentialPack = credentialPack,
            rendering = detailsRendering.toCardRendering()
        )
    }
}

@Composable
fun GenericCredentialListItem(credentialPack: CredentialPack) {
    val listRendering = CardRenderingListView(
        titleKeys = listOf("name"),
        titleFormatter = {values ->
            val w3cvc = values.toList()
                .first { (key, _) ->
                    val credential = credentialPack.getCredentialById(key)
                    (credential?.asJwtVc() != null || credential?.asJsonVc() != null)
                }.second

            Text(
                text = w3cvc["name"].toString(),
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = TextHeader,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        descriptionKeys = listOf("description", "valid"),
        descriptionFormatter = {values ->
            val w3cvc = values.toList()
                .first { (key, _) ->
                    val credential = credentialPack.getCredentialById(key)
                    (credential?.asJwtVc() != null || credential?.asJsonVc() != null)
                }.second

            Column {
                Text(
                    text = w3cvc["description"].toString(),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextBody
                )
                Spacer(modifier = Modifier.height(16.dp))
                if(w3cvc["valid"].toString() == "true") {
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
        },
        leadingIconKeys = listOf("issuer.image", "issuer.name"),
        leadingIconFormatter = { values ->
            val w3cvc = values.toList()
                .first { (key, _) ->
                    val credential = credentialPack.getCredentialById(key)
                    (credential?.asJwtVc() != null || credential?.asJsonVc() != null)
                }.second
            val byteArray = Base64.decode(
                w3cvc["issuer.image"]
                    .toString()
                    .replace("data:image/png;base64,", ""),
                Base64.DEFAULT
            ).apply {
                BitmapFactory.decodeByteArray(this, 0, size)
            }
            Column(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BitmapImage(
                    byteArray,
                    contentDescription =  w3cvc["issuer.name"].toString(),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(end = 12.dp)
                )
            }

        }
    )


    BaseCard(
        credentialPack = credentialPack,
        rendering = listRendering.toCardRendering()
    )
}

@Composable
fun GenericCredentialListItemQRCode() {
//    val vc = small_vc

    var vc by remember {
        mutableStateOf<String?>(null)
    }

    var showError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = vc) {
        vc = vcToSignedVp(vc = small_vc, keyStr = ed25519_2020_10_18)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
            .background(Primary)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showError = !showError
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.qrcode),
                contentDescription = stringResource(R.string.toggle_display_qr_code),
                modifier = Modifier.width(12.dp)
            )
            Text(
                text = if (showError) "Hide QR code" else "Show QR code",
                modifier = Modifier.padding(start = 6.dp),
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextOnPrimary
            )
        }

        AnimatedVisibility(visible = showError) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(vc != null) {
                    Image(
                        painter = rememberQrBitmapPainter(vc!!, size = 300.dp),
                        contentDescription = stringResource(id = R.string.vp_qr_code),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    text = "Shares your credential online or\nin-person, wherever accepted.",
                    modifier = Modifier.padding(start = 6.dp),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = TextOnPrimary
                )
            }

        }
    }
}