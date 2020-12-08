import socket
import errno
from time import sleep
from select import poll, POLLIN
from inspect import getmembers
import xwiimote
import struct
import threading
import traceback
import serial
import binascii
import time

UDP_IP = "127.0.0.1"
UDP_PORT = 5005

ser = serial.Serial(
    port='/dev/ttyAMA1',
    baudrate=9600,
    bytesize=serial.EIGHTBITS,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE
)

lastFrame = 0

def handle(data):
    print(" == Data == ")
    print(list(data))

    if data[0] == 0:
        dev.set_led(1, data[1] == 1)
        dev.set_led(2, data[2] == 1)
        dev.set_led(3, data[3] == 1)
        dev.set_led(4, data[4] == 1)
    elif data[0] == 1:
        milli = struct.unpack(">q", bytearray(data[1:9]))

        def rumble(time):
            dev.rumble(True)
            sleep(time / 1000.0)
            dev.rumble(False)

        thread = threading.Thread(target=rumble, args=milli)
        thread.start()

def server():
    print("Starting server...")
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('0.0.0.0', 5000))
    s.listen(5)
    while True:
        (conn, address) = s.accept()

        looping = True
        while looping:
            try:
                data = bytearray(128)

                nb = conn.recv_into(data, 128)
                if nb > 0:
                    handle(list(data[0:nb]))
                else:
                    looping = False
                    try:
                        conn.close()
                    except Exception as ex:
                        traceback.print_exc(ex)

            except Exception as ex:
                traceback.print_exc(ex)
                looping = False
                try:
                    conn.close()
                except Exception as ex:
                    traceback.print_exc(ex)


t = threading.Thread(target=server)
t.daemon = True
t.start()

def send_input(type, data):
    # print("UDP target IP: %s" % UDP_IP)
    # print("UDP target port: %s" % UDP_PORT)
    sock = socket.socket(socket.AF_INET,  # Internet
                         socket.SOCK_DGRAM)  # UDP

    if type == 6 and len(data) > 4:
        type = 19

    # print(type, data)
    message = struct.pack("b", type) + struct.pack(str(len(data)) + "h", *data)
    sock.sendto(message, (UDP_IP, UDP_PORT))

    millis = int(round(time.time() * 1000))
    if type != 19 or millis - lastFrame > 20:
        data = data[1:len(data)]
        data.insert(0, type)

        ser.write(str(data) + '\n')
        ser.flush()


# display a constant
print("=== " + xwiimote.NAME_CORE + " ===")


def get_remote():
    # list wiimotes and remember the first one
    try:
        mon = xwiimote.monitor(True, True)
        print("mon fd", mon.get_fd(False))
        ent = mon.poll()
        remote = ent
        while ent is not None:
            print("Found device: " + ent)
            ent = mon.poll()

        return remote
    except SystemError as e:
        print("ooops, cannot create monitor (", e, ")")


# continue only if there is a wiimote
firstwiimote = get_remote()
while firstwiimote is None:
    print("No wiimote to read, Pooling")
    sleep(2)
    firstwiimote = get_remote()
    # exit(0)

# create a new iface
try:
    dev = xwiimote.iface(firstwiimote)
except IOError as e:
    print("ooops,", e)
    exit(1)

# display some information and open the iface
try:
    print("syspath:" + dev.get_syspath())
    fd = dev.get_fd()
    print("fd:", fd)
    print("opened mask:", dev.opened())
    dev.open(dev.available() | xwiimote.IFACE_WRITABLE)
    print("opened mask:", dev.opened())

    dev.rumble(True)
    sleep(1 / 4.0)
    dev.rumble(False)
    dev.set_led(1, True)
    dev.set_led(2, False)
    dev.set_led(3, False)
    dev.set_led(4, False)
    # dev.set_led(2, dev.get_led(3))
    # dev.set_led(3, dev.get_led(4))
    # dev.set_led(4, dev.get_led(4) == False)
    print("capacity:", dev.get_battery(), "%")
    print("devtype:", dev.get_devtype())
    print("extension:", dev.get_extension())
except SystemError as e:
    print("ooops", e)
    exit(1)

dev.set_mp_normalization(10, 20, 30, 40)
x, y, z, factor = dev.get_mp_normalization()
print("mp", x, y, z, factor)

# read some values
p = poll()
p.register(fd, POLLIN)
evt = xwiimote.event()
n = 0
while n < 2:
    p.poll()
    try:
        dev.dispatch(evt)
        if evt.type == xwiimote.EVENT_KEY:
            code, state = evt.get_key()
            send_input(code, [state])
            n += 1
        elif evt.type == xwiimote.EVENT_GONE:
            print("Gone")
            n = 2
        elif evt.type == xwiimote.EVENT_WATCH:
            print("Watch")
        elif evt.type == xwiimote.EVENT_PRO_CONTROLLER_KEY:
            code, state = evt.get_key()
            tv_sec, tv_usec = evt.get_time()
            send_input(code, [dev.get_battery(), state])
        elif evt.type == xwiimote.EVENT_PRO_CONTROLLER_MOVE:
            x1, y1, z1 = evt.get_abs(0)
            x2, y2, z2 = evt.get_abs(1)
            send_input(xwiimote.EVENT_PRO_CONTROLLER_MOVE, [dev.get_battery(), x1, y1, z1, x2, y2, z2])
        else:
            if evt.type != xwiimote.EVENT_ACCEL:
                print("type:", evt.type)
    except IOError as e:
        if e.errno != errno.EAGAIN:
            print("Bad")

exit(0)
