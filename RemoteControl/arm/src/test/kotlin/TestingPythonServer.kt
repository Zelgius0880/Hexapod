import org.junit.jupiter.api.Test
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.test.AfterTest

class TestingPythonServer {
    val socket: Socket by lazy {
        Socket("192.168.1.38", 5000)
    }

    @AfterTest
    fun disconnect() {
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun `leds none`() {
        sendLed()
    }

    @Test
    fun `leds all`() {
        sendLed(led1 = true, led2 = true, led3 = true, led4 = true)
    }

    @Test
    fun `leds 1`() {
        sendLed(led1 = true)
    }

    @Test
    fun `leds 2`() {
        sendLed(led2 = true)
    }

    @Test
    fun `leds 3`() {
        sendLed(led3 = true)
    }

    @Test
    fun `leds 4`() {
        sendLed(led4 = true)
    }

    @Test
    fun rumble() {
        val buffer = ByteBuffer.allocate(9)
        buffer.put(1)
        buffer.putLong(200)

        socket.getOutputStream().apply {
            write(buffer.array())
            flush()
        }
    }


    fun sendLed(led1: Boolean = false,
                led2: Boolean = false,
                led3: Boolean = false,
                led4: Boolean = false) {
        val buffer = ByteBuffer.allocate(5)
        buffer.put(0)
        buffer.put(led1.toByte())
        buffer.put(led2.toByte())
        buffer.put(led3.toByte())
        buffer.put(led4.toByte())

        socket.getOutputStream().apply {
            write(buffer.array())
            flush()
        }
    }

    fun Boolean.toByte() = (if (this) 1 else 0).toByte()
}
