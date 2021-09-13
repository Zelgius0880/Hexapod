//
// Created by Florian on 27/11/2020.
//

#ifndef HEXAPOD_FRAME_H
#define HEXAPOD_FRAME_H

//****************************
//         REMOTE
//****************************
#define CROSS_LEFT      0
#define CROSS_RIGHT     1
#define CROSS_UP        2
#define CROSS_DOWN      3
#define BUTTON_A        4
#define BUTTON_B        5
#define BUTTON_X        11
#define BUTTON_Y        12
#define BUTTON_MINUS    7
#define BUTTON_PLUS     6
#define BUTTON_HOME     8
#define BUTTON_L        13
#define BUTTON_R        14
#define BUTTON_ZL       15
#define BUTTON_ZR       16
#define BUTTON_STICK_L  17
#define BUTTON_STICK_R  18
#define STICKS          19

#define FRAME_ENABLE_DEBUGGING false
#define FRAME_ENABLE_DEBUGGING_BAD_FRAME false

#include "Buffer.h"

class Frame {
private:
    bool frameStarted = false;
    uint8_t _input = 0;
    bool pressed = false;
    int _xL, _yL, _zL, _xR, _yR, _zR;
    Buffer buffer;
    bool ready = false;

    void parse();

    void print();

    void printBuffer();

public:
    bool isReady() const;

    void read();

    void reset();

    uint8_t input() {return _input; }

    int xL() { return _xL; }

    int yL() { return _yL; }

    int zL() { return _zL; }

    int xR() { return _xR; }

    int yR() { return _yR; }

    int zR() { return _zR; }

    bool isPressed() { return pressed;}
};

#endif //HEXAPOD_FRAME_H
