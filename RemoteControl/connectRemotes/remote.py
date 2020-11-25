import socket
import errno
from time import sleep
from select import poll, POLLIN
from inspect import getmembers
import xwiimote
import struct

UDP_IP = "127.0.0.1"
UDP_PORT = 5005


def send_input(type, data):
    print("UDP target IP: %s" % UDP_IP)
    print("UDP target port: %s" % UDP_PORT)
    sock = socket.socket(socket.AF_INET,  # Internet
                         socket.SOCK_DGRAM)  # UDP

    print(type, data)
    message =  struct.pack("b", type) + struct.pack(str(len(data)) + "h",  *data)
    sock.sendto(message, (UDP_IP, UDP_PORT))


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
