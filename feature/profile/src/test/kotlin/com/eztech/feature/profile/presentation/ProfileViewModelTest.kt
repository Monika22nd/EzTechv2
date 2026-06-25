package com.eztech.feature.profile.presentation

import com.eztech.core.common.Resource
import com.eztech.core.domain.model.AppSettings
import com.eztech.core.domain.model.ThemePreference
import com.eztech.core.domain.model.User
import com.eztech.core.domain.repository.AuthRepository
import com.eztech.core.domain.repository.SettingsRepository
import com.eztech.core.domain.repository.UserRepository
import com.eztech.feature.profile.presentation.edit.EditProfileViewModel
import com.eztech.feature.profile.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings updates theme notifications and logout`() = runTest(dispatcher) {
        val authRepository = FakeAuthRepository()
        val settingsRepository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(authRepository, settingsRepository)
        advanceUntilIdle()

        assertEquals(ThemePreference.SYSTEM, viewModel.uiState.value.settings.themePreference)

        viewModel.setThemePreference(ThemePreference.DARK)
        viewModel.setNotificationsEnabled(false)
        viewModel.logout()
        advanceUntilIdle()

        assertEquals(ThemePreference.DARK, viewModel.uiState.value.settings.themePreference)
        assertFalse(viewModel.uiState.value.settings.notificationsEnabled)
        assertEquals(1, authRepository.logoutCalls)
    }

    @Test
    fun `edit profile loads saves and stores avatar url`() = runTest(dispatcher) {
        val userRepository = FakeUserRepository()
        val viewModel = EditProfileViewModel(FakeAuthRepository(), userRepository)
        advanceUntilIdle()

        assertEquals("Ada", viewModel.uiState.value.name)

        viewModel.onNameChanged("Ada Lovelace")
        viewModel.onAvatarUrlChanged(FakeUserRepository.AvatarUrl)
        viewModel.save()
        advanceUntilIdle()

        assertEquals("Ada Lovelace", userRepository.updatedName)
        assertEquals(FakeUserRepository.AvatarUrl, userRepository.updatedAvatarUrl)
        assertTrue(viewModel.uiState.value.saved)
        assertEquals(FakeUserRepository.AvatarUrl, viewModel.uiState.value.avatarUrl)
    }

    @Test
    fun `edit profile rejects short names`() = runTest(dispatcher) {
        val userRepository = FakeUserRepository()
        val viewModel = EditProfileViewModel(FakeAuthRepository(), userRepository)
        advanceUntilIdle()

        viewModel.onNameChanged("A")
        viewModel.save()
        advanceUntilIdle()

        assertEquals("Display name must be at least 2 characters.", viewModel.uiState.value.errorMessage)
        assertNull(userRepository.updatedName)
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val state = MutableStateFlow(AppSettings())

        override val settings: Flow<AppSettings> = state

        override suspend fun setThemePreference(themePreference: ThemePreference) {
            state.value = state.value.copy(themePreference = themePreference)
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean) {
            state.value = state.value.copy(notificationsEnabled = enabled)
        }
    }

    private class FakeAuthRepository : AuthRepository {
        var logoutCalls = 0

        override fun observeCurrentUser(): Flow<User?> = flowOf(TestUser)

        override suspend fun login(
            email: String,
            password: String,
        ): Resource<User> = Resource.Error("Not used.")

        override suspend fun register(
            name: String,
            email: String,
            password: String,
        ): Resource<User> = Resource.Error("Not used.")

        override suspend fun sendPasswordReset(email: String): Resource<Unit> =
            Resource.Success(Unit)

        override suspend fun logout() {
            logoutCalls += 1
        }
    }

    private class FakeUserRepository : UserRepository {
        private val profile = MutableStateFlow<Resource<User>>(Resource.Success(TestUser))
        var updatedName: String? = null
        var updatedAvatarUrl: String? = null

        override fun observeUserProfile(userId: String): Flow<Resource<User>> = profile

        override suspend fun updateProfile(
            userId: String,
            name: String,
        ): Resource<Unit> {
            updatedName = name
            profile.value = Resource.Success(TestUser.copy(name = name))
            return Resource.Success(Unit)
        }

        override suspend fun updateAvatarUrl(
            userId: String,
            avatarUrl: String,
        ): Resource<String> {
            updatedAvatarUrl = avatarUrl
            profile.value = Resource.Success(TestUser.copy(avatarUrl = avatarUrl.ifBlank { null }))
            return Resource.Success(avatarUrl)
        }

        companion object {
            const val AvatarUrl = "https://example.com/avatar.jpg"
        }
    }

    private companion object {
        val TestUser = User(
            uid = "user-1",
            name = "Ada",
            email = "ada@example.com",
        )
    }
}
