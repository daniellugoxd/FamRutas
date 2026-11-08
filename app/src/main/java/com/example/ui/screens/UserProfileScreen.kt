package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.repository.UserProfileRepository
import com.example.ui.theme.*
import com.example.viewmodel.FamRutaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: FamRutaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { UserProfileRepository(context) }
    val currentUser = Firebase.auth.currentUser

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "Daniel Lugo") }
    var phoneNumber by remember { mutableStateOf("+57 310 445 9210") }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showCameraSelectionDialog by remember { mutableStateOf(false) }

    // Temporary URI for Full-Resolution Camera capture
    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 1. TakePicture (Full-res using FileProvider)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraImageUri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, tempCameraImageUri!!)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, tempCameraImageUri)
                }
                avatarBitmap = bitmap
                viewModel.showSnackbarAlert(
                    message = "Foto capturada desde la cámara",
                    iconEmoji = "📸"
                )
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo procesar la imagen de la cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. TakePicturePreview (Fallback direct thumbnail without FileProvider)
    val takeThumbnailLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            avatarBitmap = bitmap
            viewModel.showSnackbarAlert(
                message = "Foto de avatar actualizada desde la cámara",
                iconEmoji = "📸"
            )
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Launch camera
            takeThumbnailLauncher.launch()
        } else {
            Toast.makeText(context, "Permiso de cámara necesario para tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Android Photo Picker (Google Play policy compliant alternative)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                avatarBitmap = bitmap
                viewModel.showSnackbarAlert(
                    message = "Foto de perfil seleccionada",
                    iconEmoji = "🖼️"
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load initial user profile from Firestore
    LaunchedEffect(currentUser?.uid) {
        isLoadingProfile = true
        val profile = repository.getUserProfile()
        if (profile != null) {
            if (profile.displayName.isNotBlank()) displayName = profile.displayName
            if (profile.phoneNumber.isNotBlank()) phoneNumber = profile.phoneNumber
            if (profile.avatarBase64.isNotBlank()) {
                avatarBitmap = UserProfileRepository.base64ToBitmap(profile.avatarBase64)
            }
        }
        isLoadingProfile = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil Familiar",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (displayName.isBlank()) {
                                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            isSaving = true
                            coroutineScope.launch {
                                val success = repository.saveUserProfile(
                                    displayName = displayName.trim(),
                                    avatarBitmap = avatarBitmap,
                                    phoneNumber = phoneNumber.trim()
                                )
                                isSaving = false
                                if (success) {
                                    // Update locally in viewmodel as well
                                    val myMember = viewModel.uiState.value.familyMembers.firstOrNull { it.id == "user_me" }
                                    if (myMember != null) {
                                        viewModel.updateFamilyMember(myMember.copy(name = displayName.trim(), phoneNumber = phoneNumber.trim()))
                                    }
                                    viewModel.showSnackbarAlert(
                                        message = "Perfil guardado en Firestore exitosamente",
                                        iconEmoji = "💾"
                                    )
                                    Toast.makeText(context, "¡Perfil actualizado en Firestore!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Error al guardar perfil en Firestore", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = WazeCyanPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = WazeCyanPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar with camera badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    WazeCyanPrimary.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Avatar container
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(RoadSurface)
                        .border(3.dp, WazeCyanPrimary, CircleShape)
                        .clickable { showCameraSelectionDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap!!.asImageBitmap(),
                            contentDescription = "Avatar de usuario",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // Fallback emoji or initials
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayName.take(1).uppercase().ifBlank { "👨‍💼" },
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Loading indicator if fetching
                    if (isLoadingProfile) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = WazeCyanPrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // Camera Action Fab Button
                FloatingActionButton(
                    onClick = { showCameraSelectionDialog = true },
                    containerColor = WazeCyanPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(42.dp)
                        .testTag("avatar_camera_fab")
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Tomar foto con la cámara",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Toca el icono para tomar una foto con tu cámara",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Display Name and Details Form
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = WazeCyanLight
                    )

                    // Display Name Field
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Nombre para mostrar (Display Name)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WazeCyanPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("display_name_input")
                    )

                    // Phone Number Field (Used for quick calls by family)
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Teléfono de contacto familiar") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = WazeCyanPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input")
                    )

                    // Read-only email from Auth
                    if (currentUser?.email != null) {
                        OutlinedTextField(
                            value = currentUser.email ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Correo Google sincronizado") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = TrafficGreen)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Cloud Firestore sync badge
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RoadSurface.copy(alpha = 0.85f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = TrafficGreen)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Base de datos Cloud Firestore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Tu avatar y nombre se guardan directamente en la colección /users/{uid} y se replican a tu círculo.",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (displayName.isBlank()) {
                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    coroutineScope.launch {
                        val success = repository.saveUserProfile(
                            displayName = displayName.trim(),
                            avatarBitmap = avatarBitmap,
                            phoneNumber = phoneNumber.trim()
                        )
                        isSaving = false
                        if (success) {
                            val myMember = viewModel.uiState.value.familyMembers.firstOrNull { it.id == "user_me" }
                            if (myMember != null) {
                                viewModel.updateFamilyMember(myMember.copy(name = displayName.trim(), phoneNumber = phoneNumber.trim()))
                            }
                            viewModel.showSnackbarAlert(
                                message = "Perfil guardado en Firestore exitosamente",
                                iconEmoji = "💾"
                            )
                            Toast.makeText(context, "¡Perfil actualizado en Firestore!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Error al guardar en Firestore", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar en Firestore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Modal BottomSheet for selecting Camera or Gallery
    if (showCameraSelectionDialog) {
        ModalBottomSheet(
            onDismissRequest = { showCameraSelectionDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Foto de Avatar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selecciona cómo deseas capturar o actualizar tu avatar:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Take Photo with Camera
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCameraSelectionDialog = false
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                takeThumbnailLauncher.launch()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(WazeCyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = WazeCyanPrimary)
                        }
                        Column {
                            Text(
                                text = "Tomar foto con la Cámara",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Abre la cámara de tu teléfono para una selfie",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Gallery / Photo Picker
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCameraSelectionDialog = false
                            photoPickerLauncher.launch("image/*")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TrafficGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TrafficGreen)
                        }
                        Column {
                            Text(
                                text = "Elegir de la Galería",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Selecciona una foto existente de tu dispositivo",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
