package com.example.data.repository

import com.example.core.result.AppResult
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.domain.model.auth.AuthProviderType
import com.example.domain.model.auth.AuthUser
import com.example.testutil.TestCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private class FakeUserDao : UserDao {
        private val usersMap = mutableMapOf<String, UserEntity>()
        private val usersFlow = MutableStateFlow<Map<String, UserEntity>>(emptyMap())

        override fun observeUserById(userId: String): Flow<UserEntity?> {
            return usersFlow.map { it[userId] }
        }

        override fun observeLatestUser(): Flow<UserEntity?> {
            return usersFlow.map { map ->
                map.values.maxByOrNull { it.lastCachedAt }
            }
        }

        override suspend fun getUserById(userId: String): UserEntity? {
            return usersMap[userId]
        }

        override suspend fun getLatestUser(): UserEntity? {
            return usersMap.values.maxByOrNull { it.lastCachedAt }
        }

        override suspend fun insertUser(user: UserEntity) {
            usersMap[user.id] = user
            usersFlow.value = usersMap.toMap()
        }

        override suspend fun deleteUserById(userId: String) {
            usersMap.remove(userId)
            usersFlow.value = usersMap.toMap()
        }

        override suspend fun clearAllUsers() {
            usersMap.clear()
            usersFlow.value = emptyMap()
        }
    }

    private lateinit var fakeDao: FakeUserDao
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeUserDao()
        repository = UserRepositoryImpl(
            userDao = fakeDao,
            dispatchers = TestCoroutineDispatchers()
        )
    }

    @Test
    fun `cacheUser successfully persists user profile in Room DAO`() = runBlocking {
        val user = AuthUser(
            id = "u1",
            email = "cached@enterprise.com",
            displayName = "Cached User",
            provider = AuthProviderType.EMAIL
        )

        val result = repository.cacheUser(user)

        assertTrue(result is AppResult.Success)
        val cached = repository.getCachedUser("u1")
        assertNotNull(cached)
        assertEquals("cached@enterprise.com", cached?.email)
        assertEquals("Cached User", cached?.displayName)
    }

    @Test
    fun `observeCurrentUser reactively emits latest cached user`() = runBlocking {
        val user1 = AuthUser(id = "u1", email = "first@enterprise.com")
        val user2 = AuthUser(id = "u2", email = "second@enterprise.com")

        repository.cacheUser(user1)
        var observed = repository.observeCurrentUser().first()
        assertEquals("u1", observed?.id)

        repository.cacheUser(user2)
        observed = repository.observeCurrentUser().first()
        assertEquals("u2", observed?.id)
    }

    @Test
    fun `clearUserCache removes all cached records`() = runBlocking {
        val user = AuthUser(id = "u1", email = "to_clear@enterprise.com")
        repository.cacheUser(user)

        val clearResult = repository.clearUserCache()

        assertTrue(clearResult is AppResult.Success)
        assertNull(repository.getCachedUser("u1"))
        assertNull(repository.getLatestCachedUser())
    }
}
