package com.example.staffregister.ui.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

import androidx.compose.ui.tooling.preview.Preview
import com.example.staffregister.ui.theme.StaffRegisterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailScreen(
    viewModel: StaffDetailViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        Dialog(
            onDismissRequest = { showCamera = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraCaptureScreen(
                    onPhotoCaptured = { uri ->
                        viewModel.updatePhoto(uri)
                        showCamera = false
                    },
                    onBack = { showCamera = false }
                )
            }
        }
    }

    StaffDetailContent(
        name = viewModel.name,
        onNameChange = { viewModel.name = it },
        role = viewModel.role,
        onRoleChange = { viewModel.role = it },
        contact = viewModel.contact,
        onContactChange = { viewModel.contact = it },
       // credentials = viewModel.credentials,
       // onCredentialsChange = { viewModel.credentials = it },
        dailyRate = viewModel.dailyRate,
        onDailyRateChange = { viewModel.dailyRate = it },
        photoUri = viewModel.photoUri,
        onPhotoClick = { showCamera = true },
        onSaveClick = { viewModel.saveStaff(onSaveSuccess) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailContent(
    name: String,
    onNameChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    contact: String,
    onContactChange: (String) -> Unit,
    credentials: String,
    onCredentialsChange: (String) -> Unit,
    dailyRate: String,
    monthlyRate: String,
    onDailyRateChange: (String) -> Unit,
    photoUri: String?,
    onPhotoClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (name.isEmpty()) "Add Staff" else "Edit Staff") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSaveClick) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPhotoClick),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Rounded.AddAPhoto,
                            contentDescription = "Add Photo",
                            modifier = Modifier.padding(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = role,
                onValueChange = onRoleChange,
                label = { Text("Job Role") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = onContactChange,
                label = { Text("Contact Information") },
                modifier = Modifier.fillMaxWidth()
            )

          /*  OutlinedTextField(
                value = credentials,
                onValueChange = onCredentialsChange,
                label = { Text("Credentials / Qualifications") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )*/

            OutlinedTextField(
                value = monthlyRate,
                onValueChange = onDailyRateChange,
                label = { Text("Monthly Rate (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StaffDetailScreenAddPreview() {
    StaffRegisterTheme {
        StaffDetailContent(
            name = "",
            onNameChange = {},
            role = "",
            onRoleChange = {},
            contact = "",
            onContactChange = {},
            credentials = "",
            onCredentialsChange = {},
            dailyRate = "",
            monthlyRate = "",
            onDailyRateChange = {},
            photoUri = null,
            onPhotoClick = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StaffDetailScreenEditPreview() {
    StaffRegisterTheme {
        StaffDetailContent(
            name = "Jane Doe",
            onNameChange = {},
            role = "Senior Designer",
            onRoleChange = {},
            contact = "+1 555-0199",
            onContactChange = {},
            credentials = "B.Des in UI/UX Design",
            onCredentialsChange = {},
            dailyRate = "150.00",
            monthlyRate="4000",
            onDailyRateChange = {},
            photoUri = null,
            onPhotoClick = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}
