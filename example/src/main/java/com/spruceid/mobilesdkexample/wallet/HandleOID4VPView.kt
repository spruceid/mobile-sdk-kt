package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.spruceid.mobile.sdk.rs.Holder
import com.spruceid.mobile.sdk.rs.ParsedCredential
import com.spruceid.mobile.sdk.rs.PermissionRequest
import com.spruceid.mobile.sdk.rs.SdJwt
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.utils.trustedDids


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HandleOID4VPView(
    navController: NavController,
    rawCredentialsViewModel: IRawCredentialsViewModel,
    url: String
) {
    val scope = rememberCoroutineScope()

    val rawCredentials by rawCredentialsViewModel.rawCredentials.collectAsState()

    var holder by remember { mutableStateOf<Holder?>(null) }
    var permissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    LaunchedEffect(Unit) {
        println("URL: $url")

        try {
            val credentials = rawCredentials.map { rawCredential ->
                ParsedCredential
                    // TODO: Update to use VDC collection in the future
                    // to detect the type of credential.
                    .newSdJwt(SdJwt.newFromCompactSdJwt(rawCredential.rawCredential))
                    .intoGenericForm()
            }

            withContext(Dispatchers.IO) {
                holder = Holder.newWithCredentials(credentials, trustedDids);
                permissionRequest = holder!!.authorizationRequest(url)
            }
        } catch (e: Exception) {
            println("Error: $e")
        }
    }

    if (permissionRequest == null) {
        // Show a loading screen
        MobileSdkTheme {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Loading... $url",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                )
            }
        }
    } else {
        // Load the Credential View
        CredentialSelector(navController, permissionRequest!!.credentials(), onSelectedCredential = { selectedCredentials ->
            scope.launch {
                try {
                    val selectedCredential = selectedCredentials.first()
                    val permissionResponse = permissionRequest!!.createPermissionResponse(selectedCredential)

                    println("Submitting permission response")

                    holder!!.submitPermissionResponse(permissionResponse)
                } catch (e: Exception) {
                    println("Error: $e")
                }
            }
        })
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialSelector(
    navController: NavController,
    credentials: List<ParsedCredential>,
    onSelectedCredential: (List<ParsedCredential>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCredentials = remember { mutableStateListOf<ParsedCredential>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp)
    ) {
        Text(
            text = "Select the credential(s) to share",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        ElevatedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (expanded) "Select All" else "Select Credentials")
        }

        if (expanded) {
            credentials.forEach { credential ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = credential in selectedCredentials,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedCredentials.add(credential)
                            } else {
                                selectedCredentials.remove(credential)
                            }
                        }
                    )
                    Text(
                        text = credential.format().toString(),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = {
                // navigate back to home screen
                navController.navigate(Screen.HomeScreen.route)
            }) {
                Text("Cancel")
            }
            Button(onClick = {
                onSelectedCredential(selectedCredentials)

                // Navigate
                navController.navigate(Screen.HomeScreen.route)
            }) {
                Text("Continue")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ConsentModal(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .requiredWidth(width = 390.dp)
            .requiredHeight(height = 844.dp)
            .background(color = Color(0xfffdfdfc))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .offset(x = 0.dp,
                    y = 60.dp)
                .requiredWidth(width = 330.dp)
                .requiredHeight(height = 736.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 330.dp)
                ) {
                    Text(
                        text = "SpruceID Demo Wallet",
                        color = Color(0xff0c0a09),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredSize(size = 36.dp)
                                .clip(shape = RoundedCornerShape(8.dp))
                                .background(color = Color(0xff0c0a09))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.expand),
                                contentDescription = "Icon",
                                tint = Color(0xff57534e),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(3.dp)))
                            Icon(
                                painter = painterResource(id = R.drawable.collapse),
                                contentDescription = "Vector",
                                modifier = Modifier
                                    .fillMaxSize())
                        }
                        Image(
                            painter = painterResource(id = R.drawable.collapse),
                            contentDescription = "Settings",
                            modifier = Modifier
                                .requiredSize(size = 36.dp)
                                .clip(shape = RoundedCornerShape(8.dp)))
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(height = 351.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.expand),
                                contentDescription = "IMG_5824 1",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .requiredSize(size = 330.dp)
                                    .clip(shape = RoundedCornerShape(9.327272415161133.dp))
                                    .border(border = BorderStroke(2.dp, Color(0xff2f6ae1)),
                                        shape = RoundedCornerShape(9.327272415161133.dp)))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(alignment = Alignment.TopCenter)
                                    .offset(x = (-0.48261260986328125).dp,
                                        y = 309.dp)
                                    .requiredWidth(width = 132.dp)
                                    .clip(shape = RoundedCornerShape(23.dp))
                                    .background(color = Color(0xff2f6ae1))
                                    .border(border = BorderStroke(1.dp, Color(0xff4389f2)),
                                        shape = RoundedCornerShape(23.dp))
                                    .padding(horizontal = 20.dp,
                                        vertical = 10.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(size = 11.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .requiredSize(size = 11.dp)
                                                .clip(shape = CircleShape)
                                                .border(border = BorderStroke(2.dp, Color(0xff243f84)),
                                                    shape = CircleShape))
                                        Image(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Ellipse 163",
                                            modifier = Modifier
                                                .align(alignment = Alignment.TopStart)
                                                .offset(x = 0.dp,
                                                    y = 3.01611328125.dp)
                                                .requiredWidth(width = 11.dp)
                                                .requiredHeight(height = 8.dp)
                                                .border(border = BorderStroke(2.dp, Color(0xfffbf9f6))))
                                    }
                                }
                                Text(
                                    text = "Detecting...",
                                    color = Color(0xfffbf9f6),
                                    lineHeight = 1.47.em,
                                    style = TextStyle(
                                        fontSize = 15.sp))
                            }
                        }
                    }
                    Text(
                        text = "Present this QR code to a verifier in order to share data. You will see a consent dialogue",
                        color = Color(0xff78716c),
                        textAlign = TextAlign.Center,
                        lineHeight = 1.29.em,
                        style = TextStyle(
                            fontSize = 14.sp),
                        modifier = Modifier
                            .fillMaxWidth())
                }
                SizemdColorBaseStateDefault()
            }
        }
        Box(
            modifier = Modifier
                .requiredWidth(width = 390.dp)
                .requiredHeight(height = 844.dp)
                .background(color = Color(0xff0c0a09).copy(alpha = 0.5f)))
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .offset(x = 0.dp,
                    y = 0.dp)
                .requiredWidth(width = 390.dp)
                .requiredHeight(height = 740.dp)
                .clip(shape = RoundedCornerShape(topStart = 16.000001907348633.dp, topEnd = 16.000001907348633.dp))
                .background(color = Color(0xfffdfdfc))
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 36.dp)
                    .requiredWidth(width = 342.dp)
                    .requiredHeight(height = 656.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .requiredHeight(height = 590.dp)
                ) {
                    Text(
                        text = "Select the credential(s) to share",
                        color = Color(0xff0c0a09),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 20.sp),
                        modifier = Modifier
                            .fillMaxWidth())
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .requiredWidth(width = 342.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                            modifier = Modifier
                                .requiredWidth(width = 342.dp)
                        ) {
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xff2f6ae1)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xfffdfdfc))
                                                .border(border = BorderStroke(1.25.dp, Color(0xffd6d3d1)),
                                                    shape = RoundedCornerShape(2.dp)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                            Text(
                                                text = "Data Field",
                                                color = Color(0xff57534e),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp),
                                                modifier = Modifier
                                                    .weight(weight = 0.5f)
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                        }
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xfffdfdfc))
                                                .border(border = BorderStroke(1.25.dp, Color(0xffd6d3d1)),
                                                    shape = RoundedCornerShape(2.dp)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xff2f6ae1)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xfffdfdfc))
                                                .border(border = BorderStroke(1.25.dp, Color(0xffd6d3d1)),
                                                    shape = RoundedCornerShape(2.dp)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.collapse),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xff2f6ae1)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.expand),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.expand),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xff2f6ae1)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.expand),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(color = Color(0xfffdfdfc))
                                        .border(border = BorderStroke(1.dp, Color(0xffe6e1d6)),
                                            shape = RoundedCornerShape(8.dp))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.expand),
                                            contentDescription = "Checkbox",
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp))
                                                .background(color = Color(0xff2f6ae1)))
                                        Text(
                                            text = "{Credential Title}",
                                            color = Color(0xff0c0a09),
                                            lineHeight = 6.94.em,
                                            style = TextStyle(
                                                fontSize = 18.sp),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                        Icon(
                                            painter = painterResource(id = R.drawable.expand),
                                            contentDescription = "Icon",
                                            tint = Color(0xff57534e),
                                            modifier = Modifier
                                                .requiredSize(size = 20.dp)
                                                .clip(shape = RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                        }
                    }
                }
                TextField(
                    value = "",
                    onValueChange = {},
                    label = {
                        Text(
                            text = "Continue",
                            color = Color(0xfffbf9f6),
                            textAlign = TextAlign.End,
                            lineHeight = 1.5.em,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium))
                    },
                    supportingText = {
                        Text(
                            text = "Cancel",
                            color = Color(0xff0c0a09),
                            textAlign = TextAlign.End,
                            lineHeight = 1.5.em,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium))
                    },
                    modifier = Modifier
                        .fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SizemdColorBaseStateDefault(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .requiredWidth(width = 330.dp)
            .clip(shape = RoundedCornerShape(6.dp))
            .background(color = Color(0xfffdfdfc))
            .border(border = BorderStroke(1.dp, Color(0xffd6d3d1)),
                shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 20.dp,
                vertical = 10.dp)
    ) {
        IconsNoneColorBlack()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun IconsNoneColorBlack(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "Cancel",
            color = Color(0xff0c0a09),
            textAlign = TextAlign.End,
            lineHeight = 1.5.em,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium))
    }
}

@Preview(widthDp = 390, heightDp = 844)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun ConsentModalPreview() {
    ConsentModal(Modifier)
}