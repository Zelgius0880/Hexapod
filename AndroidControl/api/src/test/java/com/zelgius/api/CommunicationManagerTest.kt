package com.zelgius.api

import com.zelgius.api.model.Control
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CommunicationManagerTest {

    @Test
    fun `when status is received`() {
        val fakeServer = FakeServer()
        runBlocking {
            val latch = CountDownLatch(3)
            fakeServer.start()

            val communicationManager =
                CommunicationManager(SocketIOStreamProvider("127.0.0.1", 5432))
            communicationManager.startListening()

            val job = launch(Dispatchers.IO) {
                communicationManager.status.collect {
                    latch.countDown()
                }
            }


            latch.await(10, TimeUnit.SECONDS)

            fakeServer.stop()
            communicationManager.stop()
            job.cancel()

            Assert.assertEquals(latch.count, 0L)

        }

    }

    @Test
    fun `when control is sent`() {
        val fakeServer = FakeServer()
        runBlocking {
            val latch = CountDownLatch(1)
            fakeServer.start()

            val communicationManager =
                CommunicationManager(SocketIOStreamProvider("127.0.0.1", 5432))
            communicationManager.startListening()

            val control = Control(true, Control.Type.BUTTON_L)

            val job = launch(Dispatchers.IO) {
                fakeServer.control.collect {
                    if(it != null) {
                        latch.countDown()
                        Assert.assertEquals(it, control)
                    }
                }
            }

            communicationManager.sendControl(control)

            latch.await(2, TimeUnit.SECONDS)

            fakeServer.stop()
            communicationManager.stop()
            job.cancel()

            Assert.assertEquals(latch.count, 0L)

        }

    }

    @Test
    fun `test E2E`() {
        val fakeServer = FakeServer()
        runBlocking {
            fakeServer.start()

            val communicationManager =
                CommunicationManager(SocketIOStreamProvider("192.168.1.145", 5432))
            communicationManager.startListening()

            communicationManager.sendControl( Control(true, Control.Type.BUTTON_A))
            delay(500)
            communicationManager.sendControl( Control(false, Control.Type.BUTTON_A))
            delay(500)
            communicationManager.sendControl( Control(true, Control.Type.BUTTON_STICK_L))
            delay(500)
            communicationManager.sendControl( Control(false, Control.Type.BUTTON_STICK_L))


            fakeServer.stop()
            communicationManager.stop()
        }

    }
}