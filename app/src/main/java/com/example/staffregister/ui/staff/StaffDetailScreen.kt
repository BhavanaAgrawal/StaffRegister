package com.example.staffregister.ui.staff

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.staffregister.ui.theme.StaffRegisterTheme
import java.text.SimpleDateFormat
import java.util.*

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
        dailyRate = viewModel.dailyRate,
        monthlyRate = viewModel.monthlyRate,
        joiningDate = viewModel.joiningDate,
        onDailyRateChange = { viewModel.dailyRate = it },
        onMonthlyRateChange = { viewModel.monthlyRate = it },
        onJoiningDateChange = { viewModel.joiningDate = it },
        photoUri = viewModel.photoUri,
        onPhotoClick = { showCamera = true },
        onSaveClick = { viewModel.saveStaff(onSaveSuccess) },
        onBack = onBack,
        credentials = viewModel.credentials,
        onCredentialsChange = { viewModel.credentials = it }
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
    dailyRate: String,
    monthlyRate: String,
    joiningDate: Long,
    onDailyRateChange: (String) -> Unit,
    onMonthlyRateChange: (String) -> Unit,
    onJoiningDateChange: (Long) -> Unit,
    photoUri: String?,
    onPhotoClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    credentials: String = "",
    onCredentialsChange: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = role,
                onValueChange = onRoleChange,
                label = { Text("Job Role") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = contact,
                onValueChange = onContactChange,
                label = { Text("Contact Information") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = monthlyRate,
                onValueChange = onMonthlyRateChange,
                label = { Text("Monthly Rate (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val cal = Calendar.getInstance().apply { timeInMillis = joiningDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                onJoiningDateChange(newCal.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Joining Date", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateFormat.format(Date(joiningDate)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SELECT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = "Select Joining Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
            dailyRate = "",
            monthlyRate = "",
            joiningDate = System.currentTimeMillis(),
            onDailyRateChange = {},
            onMonthlyRateChange = {},
            onJoiningDateChange = {},
            photoUri = null,
            onPhotoClick = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
            dailyRate = "150.00",
            monthlyRate = "4000",
            joiningDate = System.currentTimeMillis(),
            onDailyRateChange = {},
            onMonthlyRateChange = {},
            onJoiningDateChange = {},
            photoUri = null,
            onPhotoClick = {},
            onSaveClick = {},
            onBack = {}
        )
    }
}
