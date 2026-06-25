package com.eztech.feature.profile.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.component.EzTechTopBar
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSuccess()
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onBackClick()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EzTechTopBar(
                title = "Edit profile",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = viewModel::save,
                        enabled = state.canSave,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Save profile",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.email.isBlank() && state.name.isBlank() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Profile unavailable",
                    message = "Sign in again to edit your profile.",
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                content = {
                    EditProfileContent(
                        state = state,
                        onNameChanged = viewModel::onNameChanged,
                        onAvatarUrlChanged = viewModel::onAvatarUrlChanged,
                        onSave = viewModel::save,
                    )
                },
            )
        }
    }
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    onNameChanged: (String) -> Unit,
    onAvatarUrlChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(EzTechDimens.ScreenPadding)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceLarge),
    ) {
        ProfileAvatar(
            name = state.name,
            avatarUrl = state.avatarUrlInput.ifBlank { null },
        )
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            singleLine = true,
            supportingText = {
                Text("This name appears on your profile and leaderboard.")
            },
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            enabled = false,
        )
        OutlinedTextField(
            value = state.avatarUrlInput,
            onValueChange = onAvatarUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Avatar URL") },
            singleLine = true,
            supportingText = {
                Text("Leave empty to use the default avatar.")
            },
        )
        Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = EzTechDimens.SpaceSmall),
                    strokeWidth = 2.dp,
                )
            }
            Text("Save changes")
        }
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl.isNullOrBlank()) {
            Text(
                text = name.take(1).ifBlank { "E" }.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        } else {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile avatar",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
