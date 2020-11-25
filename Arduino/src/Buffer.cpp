//
// Created by Florian on 25/11/2020.
//

#include "Buffer.h"

uint8_t Buffer::put(uint8_t byte) {
    if (putPosition + 1 >= BUFFER_MAX_LENGTH) return 0;
    array[putPosition] = byte;
    ++putPosition;
    ++_length;

    return 1;
}

uint8_t Buffer::put(uint16_t bytes) {
    if (putPosition + 2 >= BUFFER_MAX_LENGTH) return 0;
    put((uint8_t) (bytes & 0xffu));
    put((uint8_t) ((bytes & 0xff00u) >> 8u));
    return 1;
}

uint8_t Buffer::put(uint32_t bytes) {
    if (putPosition + 4 >= BUFFER_MAX_LENGTH) return 0;
    put((uint8_t) (bytes & 0xffu));
    put((uint8_t) ((bytes & 0xff00u) >> 8u));
    put((uint8_t) ((bytes & 0xff0000u) >> 16u));
    put((uint8_t) ((bytes & 0xff000000u) >> 24u));
    return 1;
}

uint8_t Buffer::put(uint8_t *bytes, unsigned int length) {
    if (putPosition + length >= BUFFER_MAX_LENGTH) return 0;

    for (int i = 0; i < length; ++i) {
        put(bytes[i]);
    }

    return 1;
}

uint8_t Buffer::get() {
    uint8_t v = array[getPosition];
    ++getPosition;
    return v;
}

uint16_t Buffer::getShort() {
    return get() | get() << 8u;
}

float Buffer::getFloat() {
    return get()
    | get() << 8u
    | get() << 16u
    | get() << 24u;
}

void Buffer::reset() {
    _length = 0;
    putPosition = 0;
    getPosition = 0;
}