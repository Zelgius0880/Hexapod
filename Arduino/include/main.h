//
// Created by Florian on 14/11/2020.
//

#ifndef UNTITLED_MAIN_H
#define UNTITLED_MAIN_H

#define MAIN_ENABLE_DEBUGGING true

#include <Arduino.h>
//#include <PS2X_lib.h>   //reference: http://www.billporter.info/
#include <Servo.h>
#include <math.h>
#include "Frame.h"

//***********************************************************************
// Constant Declarations
//***********************************************************************
const int BATT_VOLTAGE = 0;           //12V Battery analog voltage _input port

const int PS2_DAT = 2;                //gamepad port definitions
const int PS2_ATT = 3;
const int PS2_CMD = 4;
const int PS2_CLK = 5;
const int RUMBLE = true;
const int PRESSURES = false;

const int COXA1_SERVO = 19;          //servo port definitions
const int FEMUR1_SERVO = 21;
const int TIBIA1_SERVO = 23;
const int COXA2_SERVO = 25;
const int FEMUR2_SERVO = 27;
const int TIBIA2_SERVO = 29;
const int COXA3_SERVO = 31;
const int FEMUR3_SERVO = 33;
const int TIBIA3_SERVO = 35;
const int COXA4_SERVO = 37;
const int FEMUR4_SERVO = 39;
const int TIBIA4_SERVO = 41;
const int COXA5_SERVO = 43;
const int FEMUR5_SERVO = 45;
const int TIBIA5_SERVO = 47;
const int COXA6_SERVO = 49;
const int FEMUR6_SERVO = 51;
const int TIBIA6_SERVO = 53;

const int RED_LED1 = 22;            //LED port definitions
const int GREEN_LED1 = 24;
const int RED_LED2 = 26;
const int GREEN_LED2 = 28;
const int RED_LED3 = 30;
const int GREEN_LED3 = 32;
const int RED_LED4 = 34;
const int GREEN_LED4 = 36;
const int RED_LED5 = 38;
const int GREEN_LED5 = 40;
const int RED_LED6 = 42;
const int GREEN_LED6 = 44;
const int RED_LED7 = 46;
const int GREEN_LED7 = 48;
const int RED_LED8 = 50;
const int GREEN_LED8 = 52;


const int COXA_LENGTH = 51;           //leg part lengths
const int FEMUR_LENGTH = 65;
const int TIBIA_LENGTH = 121;

const int TRAVEL = 30;                //translate and rotate travel limit constant

const long A12DEG = 209440;           //12 degrees in radians x 1,000,000
const long A30DEG = 523599;           //30 degrees in radians x 1,000,000

const int FRAME_TIME_MS = 20;         //frame time (20msec = 50Hz)//
//const int FRAME_TIME_MS = 500;         //frame time (20msec = 50Hz)

const float HOME_X[6] = {82.0, 0.0, -82.0, -82.0, 0.0, 82.0};  //coxa-to-toe home positions
const float HOME_Y[6] = {82.0, 116.0, 82.0, -82.0, -116.0, -82.0};
const float HOME_Z[6] = {-80.0, -80.0, -80.0, -80.0, -80.0, -80.0};

const float BODY_X[6] = {110.4, 0.0, -110.4, -110.4, 0.0, 110.4};  //body center-to-coxa servo distances
const float BODY_Y[6] = {58.4, 90.8, 58.4, -58.4, -90.8, -58.4};
const float BODY_Z[6] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

const int COXA_CAL[6] = {2, -1, -1, -3, -2, -3};                       //servo calibration constants
const int FEMUR_CAL[6] = {4, -2, 0, -1, 0, 0};
const int TIBIA_CAL[6] = {0, -3, -3, -2, -3, -1};

void process_gamepad(uint8_t input);

void leg_IK(int leg_number, float X, float Y, float Z);

void tripod_gait(int xR, int yR, int zR, int xL, int yL, int zL);

void wave_gait(int xR, int yR, int zR, int xL, int yL, int zL);

void ripple_gait(int xR, int yR, int zR, int xL, int yL, int zL);

void tetrapod_gait(int xR, int yR, int zR, int xL, int yL, int zL);

void compute_strides();

void compute_amplitudes();

void translate_control(int xR, int yR, int zR, int xL, int yL, int zL);

void rotate_control(int xR, int yR, int zR, int xL, int yL, int zL);

void one_leg_lift(int xR, int yR, int zR, int xL, int yL, int zL);

void set_all_90();

void sendBatteryLevel();

#endif //UNTITLED_MAIN_H
