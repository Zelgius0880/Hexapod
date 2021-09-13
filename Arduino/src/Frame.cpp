//
// Created by Florian on 27/11/2020.
//
#include "Frame.h"

void Frame::printBuffer() {
#if FRAME_ENABLE_DEBUGGING
    Serial.println("----->");
    for (uint16_t i = 0; i < buffer.length(); ++i) {
        Serial.print((char) buffer.array[i]);
    }
    Serial.println();
#endif
}

void Frame::print() {
#if FRAME_ENABLE_DEBUGGING
    Serial.print("\nInput : ");
    Serial.print(_input);
    if(_input != STICKS) {
        Serial.println(pressed ? " is Pressed" : " is Released");
    } else {
        Serial.print("{");
        Serial.print("_xL: ");
        Serial.print(_xL, DEC);
        Serial.print(" ,_yL: ");
        Serial.print(_yL, DEC);
        Serial.print(" ,_zL: ");
        Serial.print(_zL, DEC);
        Serial.print(" ,_xR: ");
        Serial.print(_xR, DEC);
        Serial.print(" ,_yR: ");
        Serial.print(_yR, DEC);
        Serial.print(" ,_zR: ");
        Serial.print(_zR, DEC);
        Serial.println("}");
    }
#endif
}

int nextInt(Buffer *buffer, char delimiter) {
    uint8_t b;
    char temp[16] = {0};
    for (uint16_t i = 0; i < buffer->length()
                    && (b = buffer->get()) != delimiter; ++i) {
        temp[i] = b;
    }
    return (int) strtol(temp, nullptr, 10);
}

uint16_t countChar(const char *array, size_t size, char c, uint16_t start = 0) {
    uint16_t count = 0;
    for (int i = start; i < size; ++i) {
        if (array[i] == c) ++count;
    }
    return count;
}

void Frame::parse() {
    _input = (uint8_t) nextInt(&buffer, ',');
    if (_input < STICKS && _input >= CROSS_LEFT) {
        if (countChar((char *) buffer.array, buffer.length(), ',', 0) != 1) {
#if FRAME_ENABLE_DEBUGGING_BAD_FRAME
            Serial.println(" !!!!! Bad Frame !!!!!!");
            printBuffer();
#endif

            reset(); // corrupted frame
            return;
        }

        int i = nextInt(&buffer, 0);
        pressed =  i == 1;
    } else if (_input == STICKS) {
        if (countChar((char *) buffer.array, buffer.length(), ',', 0) != 6) {

#if FRAME_ENABLE_DEBUGGING_BAD_FRAME
            Serial.println(" !!!!! Bad Frame !!!!!!");
            printBuffer();
#endif

            reset(); // corrupted frame
            return;
        }
        _xL = nextInt(&buffer, ',');
        _yL = nextInt(&buffer, ',');
        _zL = nextInt(&buffer, ',');
        _xR = nextInt(&buffer, ',');
        _yR = nextInt(&buffer, ',');
        _zR = nextInt(&buffer, 0);
    }

    print();

    ready = true;
}

bool Frame::isReady() const {
    return ready;
}

void Frame::read() {
    if (Serial.available()) {
        int data = Serial.read();
        if (data != -1) {
#if FRAME_ENABLE_DEBUGGING
            Serial.print((char) data);
#endif
            if (data == '[') {
                buffer.reset();
                frameStarted = true;
            } else if (data == ']' && frameStarted) {
                buffer.put((uint8_t) 0);
                frameStarted = false;
                parse();
            } else {
                buffer.put((uint8_t) data);
            }
        }
    }
}

void Frame::reset() {
    buffer.reset();
    _input = 0;
    _xL = 0;
    _yL = 0;
    _zL = 0;
    _xR = 0;
    _yR = 0;
    _zR = 0;
    pressed = false;
    ready = false;
}