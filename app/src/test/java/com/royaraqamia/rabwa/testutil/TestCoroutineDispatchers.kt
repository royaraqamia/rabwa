package com.royaraqamia.rabwa.testutil

import com.royaraqamia.rabwa.core.dispatcher.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineDispatchers(
    val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
) : CoroutineDispatchers {
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
}
