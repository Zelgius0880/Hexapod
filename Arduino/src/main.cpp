//***********************************************************************
// Hexapo Program
// Code for Arduino Mega
// by Mark W
//***********************************************************************

//***********************************************************************
// IK and Hexapo gait references:
//  https://www.projectsofdan.com/?cat=4
//  http://www.gperco.com/2015/06/hex-inverse-kinematics.html
//  http://virtual-shed.blogspot.com/2012/12/hexapod-inverse-kinematics-part-1.html
//  http://virtual-shed.blogspot.com/2013/01/hexapod-inverse-kinematics-part-2.html
//  https://www.robotshop.com/community/forum/t/inverse-kinematic-equations-for-lynxmotion-3dof-legs/21336
//  http://arduin0.blogspot.com/2012/01/inverse-kinematics-ik-implementation.html?utm_source=rb-community&utm_medium=forum&utm_campaign=inverse-kinematic-equations-for-lynxmotion-3dof-legs
//***********************************************************************

//***********************************************************************
// Includes
//***********************************************************************
#include "main.h"


//***********************************************************************
// Variable Declarations
//***********************************************************************
int gamepad_error;                    //gamepad variables
byte gamepad_type;
byte gamepad_vibrate;

unsigned long currentTime;            //frame timer variables
unsigned long previousTime;
unsigned long previousBatteryTime;

int temp;                             //mode and control variables
int mode;
int gait;
int gait_speed;
int reset_position;
int capture_offsets;


float L0, L3;                         //inverse kinematics variables
float gamma_femur;
float phi_tibia, phi_femur;
float theta_tibia, theta_femur, theta_coxa;

int leg1_IK_control, leg6_IK_control; //leg lift mode variables
float leg1_coxa, leg1_femur, leg1_tibia;
float leg6_coxa, leg6_femur, leg6_tibia;

int leg_num;                          //positioning and walking variables
int z_height_LED_color;
int totalX, totalY, totalZ;
int tick, duration, numTicks;
int z_height_left, z_height_right;
int commandedX, commandedY, commandedR;
int translateX, translateY, translateZ;
float step_height_multiplier;
float strideX, strideY, strideR;
float sinRotX, sinRotY, sinRotZ;
float cosRotX, cosRotY, cosRotZ;
float rotOffsetX, rotOffsetY, rotOffsetZ;
float amplitudeX, amplitudeY, amplitudeZ;
float offset_X[6], offset_Y[6], offset_Z[6];
float current_X[6], current_Y[6], current_Z[6];

int tripod_case[6] = {1, 2, 1, 2, 1, 2};     //for tripod gait walking
int ripple_case[6] = {2, 6, 4, 1, 3, 5};     //for ripple gait
int wave_case[6] = {1, 2, 3, 4, 5, 6};     //for wave gait
int tetrapod_case[6] = {1, 3, 2, 1, 2, 3};     //for tetrapod gait


//***********************************************************************
// Object Declarations
//***********************************************************************

Servo coxa1_servo;      //18 servos
Servo femur1_servo;
Servo tibia1_servo;
Servo coxa2_servo;
Servo femur2_servo;
Servo tibia2_servo;
Servo coxa3_servo;
Servo femur3_servo;
Servo tibia3_servo;
Servo coxa4_servo;
Servo femur4_servo;
Servo tibia4_servo;
Servo coxa5_servo;
Servo femur5_servo;
Servo tibia5_servo;
Servo coxa6_servo;
Servo femur6_servo;
Servo tibia6_servo;

Frame frame;

//***********************************************************************
// Initialization Routine
//***********************************************************************

void setup() {
    //start serial
    Serial.begin(9600);
    //Serial1.begin(9600, SERIAL_8N1);

    //attach servos
    coxa1_servo.attach(COXA1_SERVO, 610, 2400);
    femur1_servo.attach(FEMUR1_SERVO, 610, 2400);
    tibia1_servo.attach(TIBIA1_SERVO, 610, 2400);
    coxa2_servo.attach(COXA2_SERVO, 610, 2400);
    femur2_servo.attach(FEMUR2_SERVO, 610, 2400);
    tibia2_servo.attach(TIBIA2_SERVO, 610, 2400);
    coxa3_servo.attach(COXA3_SERVO, 610, 2400);
    femur3_servo.attach(FEMUR3_SERVO, 610, 2400);
    tibia3_servo.attach(TIBIA3_SERVO, 610, 2400);
    coxa4_servo.attach(COXA4_SERVO, 610, 2400);
    femur4_servo.attach(FEMUR4_SERVO, 610, 2400);
    tibia4_servo.attach(TIBIA4_SERVO, 610, 2400);
    coxa5_servo.attach(COXA5_SERVO, 610, 2400);
    femur5_servo.attach(FEMUR5_SERVO, 610, 2400);
    tibia5_servo.attach(TIBIA5_SERVO, 610, 2400);
    coxa6_servo.attach(COXA6_SERVO, 610, 2400);
    femur6_servo.attach(FEMUR6_SERVO, 610, 2400);
    tibia6_servo.attach(TIBIA6_SERVO, 610, 2400);

    //clear offsets
    for (leg_num = 0; leg_num < 6; leg_num++) {
        offset_X[leg_num] = 0.0;
        offset_Y[leg_num] = 0.0;
        offset_Z[leg_num] = 0.0;
    }
    capture_offsets = false;
    step_height_multiplier = 1.0;

    //initialize mode and gait variables
    mode = 0;
    gait = 0;
    gait_speed = 0;
    reset_position = true;
    leg1_IK_control = true;
    leg6_IK_control = true;

    Serial.println("==== Ready ====");
}


//***********************************************************************
// Main Program
//***********************************************************************
void loop() {
    //exit if no controller found or GuitarHero controller
    /*if((gamepad_error == 1) || (gamepad_type == 2))
    {
        Serial.println("Invalid Controller!");
        return;
    }*/

    frame.read();

    //set up frame time
    currentTime = millis();

    if ((currentTime - previousBatteryTime) > (30 * 1000) || previousBatteryTime == 0) {
        previousBatteryTime = currentTime;
        sendBatteryLevel();
    }

    if (frame.isReady()) {
        //Serial.println(analogRead(A0) * ((2*7.4))/1024);
        //Serial.println(analogRead(A0) * (2*7.4) /1024);
        //read controller and process inputs
        //ps2x.read_gamepad(false, gamepad_vibrate);
        if (frame.input() != STICKS && frame.isPressed()) {

            process_gamepad(frame.input());

            //reset legs to home position when commanded
            if (reset_position == true) {
                for (leg_num = 0; leg_num < 6; leg_num++) {
                    current_X[leg_num] = HOME_X[leg_num];
                    current_Y[leg_num] = HOME_Y[leg_num];
                    current_Z[leg_num] = HOME_Z[leg_num];
                }
                reset_position = false;
            }

            if (mode == 99) set_all_90();              //set all servos to 90 degrees mode}
            frame.reset();
        }

        //battery_monitor();                        //battery monitor and output to LEDs
        //print_debug();                            //print debug data

        if ((currentTime - previousTime) > FRAME_TIME_MS && frame.input() == STICKS) {
            //process modes (mode 0 is default 'home idle' do-nothing mode)
            previousTime = currentTime;

            //position legs using IK calculations - unless set all to 90 degrees mode
            if (mode < 99) {
                for (leg_num = 0; leg_num < 6; leg_num++)
                    leg_IK(leg_num, current_X[leg_num] + offset_X[leg_num], current_Y[leg_num] + offset_Y[leg_num],
                           current_Z[leg_num] + offset_Z[leg_num]);
            }

            //reset leg lift first pass flags if needed
            if (mode != 4) {
                leg1_IK_control = true;
                leg6_IK_control = true;
            }

            if (mode == 1)                             //walking mode
            {
                if (gait == 0)
                    tripod_gait(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                                frame.zL());            //walk using gait 0
                if (gait == 1)
                    wave_gait(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                              frame.zL());              //walk using gait 1
                if (gait == 2)
                    ripple_gait(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                                frame.zL());            //walk using gait 2
                if (gait == 3)
                    tetrapod_gait(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                                  frame.zL());          //walk using gait 3
            }
            /*if (frame.xL() > 100 || frame.yL() > 100){
                translate_control(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                                  frame.zL());        //joystick control x-y-z mode
            } else*/
           /* if (abs(frame.xR()) > 100 || abs(frame.yR()) > 100)
                translate_control(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                               frame.zL());           //joystick control y-p-r mode
            */if (mode == 4)
                one_leg_lift(frame.xR(), -frame.yR(), frame.zR(), frame.xL(), -frame.yL(),
                             frame.zL());             //one leg lift mode
        }
    }
}


//***********************************************************************
// Process gamepad controller inputs
//***********************************************************************
void process_gamepad(uint8_t input) {
    switch (input) {
        case CROSS_DOWN:
            mode = 0;
            gait = 0;
            reset_position = true;
            return;
        case CROSS_LEFT:
            mode = 0;
            gait = 1;
            reset_position = true;
            return;

        case CROSS_UP:
            mode = 0;
            gait = 2;
            reset_position = true;
            return;

        case CROSS_RIGHT:
            mode = 0;
            gait = 3;
            reset_position = true;
            return;

        case BUTTON_X:
            mode = 1;
            reset_position = true;
            return;

        case BUTTON_Y:
            mode = 2;
            reset_position = true;
            return;

        case BUTTON_A:
            mode = 3;
            reset_position = true;
            return;

        case BUTTON_B:
            mode = 4;
            reset_position = true;
            return;

        case BUTTON_PLUS:
            if (gait_speed == 0)
                gait_speed = 1;
            else
                gait_speed = 0;
            return;

        case BUTTON_HOME:
            mode = 99;
            return;

        case BUTTON_R:
        case BUTTON_L:
            capture_offsets = true;
            return;

        case BUTTON_ZR:
        case BUTTON_ZL:
            for (leg_num = 0; leg_num < 6; leg_num++)  //clear offsets
            {
                offset_X[leg_num] = 0;
                offset_Y[leg_num] = 0;
                offset_Z[leg_num] = 0;
            }
            leg1_IK_control = true;               //reset leg lift first pass flags
            leg6_IK_control = true;
            step_height_multiplier = 1.0;         //reset step height multiplier
            return;

        default:
            return;
    }
}


//***********************************************************************
// Leg IK Routine
//***********************************************************************
void leg_IK(int leg_number, float X, float Y, float Z) {
    //compute target femur-to-toe (L3) _length
    L0 = sqrt(sq(X) + sq(Y)) - COXA_LENGTH;
    L3 = sqrt(sq(L0) + sq(Z));

    //process only if reach is within possible range (not too long or too short!)
    if ((L3 < (TIBIA_LENGTH + FEMUR_LENGTH)) && (L3 > (TIBIA_LENGTH - FEMUR_LENGTH))) {
        //compute tibia angle
        phi_tibia = acos((sq(FEMUR_LENGTH) + sq(TIBIA_LENGTH) - sq(L3)) / (2 * FEMUR_LENGTH * TIBIA_LENGTH));
        theta_tibia = phi_tibia * RAD_TO_DEG - 23.0 + TIBIA_CAL[leg_number];
        theta_tibia = constrain(theta_tibia, 0.0, 180.0);

        //compute femur angle
        gamma_femur = atan2(Z, L0);
        phi_femur = acos((sq(FEMUR_LENGTH) + sq(L3) - sq(TIBIA_LENGTH)) / (2 * FEMUR_LENGTH * L3));
        theta_femur = (phi_femur + gamma_femur) * RAD_TO_DEG + 14.0 + 90.0 + FEMUR_CAL[leg_number];
        theta_femur = constrain(theta_femur, 0.0, 180.0);

        //compute coxa angle
        theta_coxa = atan2(X, Y) * RAD_TO_DEG + COXA_CAL[leg_number];

        //output to the appropriate leg
        switch (leg_number) {
            case 0:
                if (leg1_IK_control == true)                       //flag for IK or manual control of leg
                {
                    theta_coxa = theta_coxa + 45.0;                 //compensate for leg mounting
                    theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                    coxa1_servo.write(int(theta_coxa));
                    femur1_servo.write(int(theta_femur));
                    tibia1_servo.write(int(theta_tibia));
                }
                break;
            case 1:
                theta_coxa = theta_coxa + 90.0;                 //compensate for leg mounting
                theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                coxa2_servo.write(int(theta_coxa));
                femur2_servo.write(int(theta_femur));
                tibia2_servo.write(int(theta_tibia));
                break;
            case 2:
                theta_coxa = theta_coxa + 135.0;                 //compensate for leg mounting
                theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                coxa3_servo.write(int(theta_coxa));
                femur3_servo.write(int(theta_femur));
                tibia3_servo.write(int(theta_tibia));
                break;
            case 3:
                if (theta_coxa < 0)                                //compensate for leg mounting
                    theta_coxa = theta_coxa + 225.0;                // (need to use different
                else                                              //  positive and negative offsets
                    theta_coxa = theta_coxa - 135.0;                //  due to atan2 results above!)
                theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                coxa4_servo.write(int(theta_coxa));
                femur4_servo.write(int(180 - theta_femur));
                tibia4_servo.write(int(180 - theta_tibia));
                break;
            case 4:
                if (theta_coxa < 0)                                //compensate for leg mounting
                    theta_coxa = theta_coxa + 270.0;                // (need to use different
                else                                              //  positive and negative offsets
                    theta_coxa = theta_coxa - 90.0;                 //  due to atan2 results above!)
                theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                coxa5_servo.write(int(theta_coxa));
                femur5_servo.write(int(180 - theta_femur));
                tibia5_servo.write(int(180 - theta_tibia));
                break;
            case 5:

                if (leg6_IK_control == true)                       //flag for IK or manual control of leg
                {
                    if (theta_coxa < 0)                              //compensate for leg mounting
                        theta_coxa = theta_coxa + 315.0;              // (need to use different
                    else                                            //  positive and negative offsets
                        theta_coxa = theta_coxa - 45.0;               //  due to atan2 results above!)
                    theta_coxa = constrain(theta_coxa, 0.0, 180.0);
                    coxa6_servo.write(int(theta_coxa));
                    femur6_servo.write(int(180 - theta_femur));
                    tibia6_servo.write(int(180 - theta_tibia));
                }
                break;
        }
    }
}


//***********************************************************************
// Tripod Gait
// Group of 3 legs move forward while the other 3 legs provide support
//***********************************************************************
void tripod_gait(int xR, int yR, int zR, int xL, int yL, int zL) {
    //read commanded values from controller
    commandedX = yL;//map(yR, -1210, 1210, 127, -127);
    commandedY = xL;//map(xR, -1210, 1210, -127, 127);
    commandedR = xR; //map(xR, -1210, 1210, 127, -127);

    //if commands more than deadband then process
    if ((abs(commandedX) > 15) || (abs(commandedY) > 15) || (abs(commandedR) > 15) || (tick > 0)) {

        compute_strides();
        numTicks = round(duration / FRAME_TIME_MS / 2.0); //total ticks divided into the two cases
        for (leg_num = 0; leg_num < 6; leg_num++) {
            compute_amplitudes();
            switch (tripod_case[leg_num]) {
                case 1:                               //move foot forward (raise and lower)
                    current_X[leg_num] = HOME_X[leg_num] - amplitudeX * cos(M_PI * tick / numTicks);
                    current_Y[leg_num] = HOME_Y[leg_num] - amplitudeY * cos(M_PI * tick / numTicks);
                    current_Z[leg_num] = HOME_Z[leg_num] + abs(amplitudeZ) * sin(M_PI * tick / numTicks);
                    if (tick >= numTicks - 1) tripod_case[leg_num] = 2;
                    break;
                case 2:                               //move foot back (on the ground)
                    current_X[leg_num] = HOME_X[leg_num] + amplitudeX * cos(M_PI * tick / numTicks);
                    current_Y[leg_num] = HOME_Y[leg_num] + amplitudeY * cos(M_PI * tick / numTicks);
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) tripod_case[leg_num] = 1;
                    break;
            }
        }

        //increment tick
        if (tick < numTicks - 1) tick++;
        else tick = 0;
    }
}


//***********************************************************************
// Wave Gait
// Legs move forward one at a time while the other 5 legs provide support
//***********************************************************************
void wave_gait(int xR, int yR, int zR, int xL, int yL, int zL) {
    //read commanded values from controller
    commandedX = yL;//map(yR, -1210, 1210, 127, -127);
    commandedY = xL;//map(xR, -1210, 1210, -127, 127);
    commandedR = xR; //map(xR, -1210, 1210, 127, -127);

    //if commands more than deadband then process
    if ((abs(commandedX) > 15) || (abs(commandedY) > 15) || (abs(commandedR) > 15) || (tick > 0)) {
        compute_strides();
        numTicks = round(duration / FRAME_TIME_MS / 6.0); //total ticks divided into the six cases
        for (leg_num = 0; leg_num < 6; leg_num++) {
            compute_amplitudes();
            switch (wave_case[leg_num]) {
                case 1:                               //move foot forward (raise and lower)
                    current_X[leg_num] = HOME_X[leg_num] - amplitudeX * cos(M_PI * tick / numTicks);
                    current_Y[leg_num] = HOME_Y[leg_num] - amplitudeY * cos(M_PI * tick / numTicks);
                    current_Z[leg_num] = HOME_Z[leg_num] + abs(amplitudeZ) * sin(M_PI * tick / numTicks);
                    if (tick >= numTicks - 1) wave_case[leg_num] = 6;
                    break;
                case 2:                               //move foot back one-fifth (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.5;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.5;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) wave_case[leg_num] = 1;
                    break;
                case 3:                               //move foot back one-fifth (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.5;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.5;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) wave_case[leg_num] = 2;
                    break;
                case 4:                               //move foot back one-fifth (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.5;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.5;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1)
                        wave_case[leg_num] = 3;
                    break;
                case 5:                               //move foot back one-fifth (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.5;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.5;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) wave_case[leg_num] = 4;
                    break;
                case 6:                               //move foot back one-fifth (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.5;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.5;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) wave_case[leg_num] = 5;
                    break;
            }
        }
        //increment tick
        if (tick < numTicks - 1) tick++;
        else tick = 0;
    }
}


//***********************************************************************
// Ripple Gait
// Left legs move forward rear-to-front while right also do the same,
// but right side is offset so RR starts midway through the LM stroke
//***********************************************************************
void ripple_gait(int xR, int yR, int zR, int xL, int yL, int zL) {
    //read commanded values from controller
    commandedX = yL;//map(yR, -1210, 1210, 127, -127);
    commandedY = xL;//map(xR, -1210, 1210, -127, 127);
    commandedR = xR; //map(xR, -1210, 1210, 127, -127);

    //if commands more than deadband then process
    if ((abs(commandedX) > 15) || (abs(commandedY) > 15) || (abs(commandedR) > 15) || (tick > 0)) {
        compute_strides();
        numTicks = round(duration / FRAME_TIME_MS / 6.0); //total ticks divided into the six cases
        for (leg_num = 0; leg_num < 6; leg_num++) {
            compute_amplitudes();
            switch (ripple_case[leg_num]) {
                case 1:                               //move foot forward (raise)
                    current_X[leg_num] = HOME_X[leg_num] - amplitudeX * cos(M_PI * tick / (numTicks * 2));
                    current_Y[leg_num] = HOME_Y[leg_num] - amplitudeY * cos(M_PI * tick / (numTicks * 2));
                    current_Z[leg_num] = HOME_Z[leg_num] + abs(amplitudeZ) * sin(M_PI * tick / (numTicks * 2));
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 2;
                    break;
                case 2:                               //move foot forward (lower)
                    current_X[leg_num] =
                            HOME_X[leg_num] - amplitudeX * cos(M_PI * (numTicks + tick) / (numTicks * 2));
                    current_Y[leg_num] =
                            HOME_Y[leg_num] - amplitudeY * cos(M_PI * (numTicks + tick) / (numTicks * 2));
                    current_Z[leg_num] =
                            HOME_Z[leg_num] + abs(amplitudeZ) * sin(M_PI * (numTicks + tick) / (numTicks * 2));
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 3;
                    break;
                case 3:                               //move foot back one-quarter (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.0;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.0;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 4;
                    break;
                case 4:                               //move foot back one-quarter (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.0;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.0;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 5;
                    break;
                case 5:                               //move foot back one-quarter (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.0;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.0;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 6;
                    break;
                case 6:                               //move foot back one-quarter (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks / 2.0;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks / 2.0;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) ripple_case[leg_num] = 1;
                    break;
            }
        }
        //increment tick
        if (tick < numTicks - 1) tick++;
        else tick = 0;
    }
}


//***********************************************************************
// Tetrapod Gait
// Right front and left rear legs move forward together, then right  
// rear and left middle, and finally right middle and left front.
//***********************************************************************
void tetrapod_gait(int xR, int yR, int zR, int xL, int yL, int zL) {
    //read commanded values from controller
    commandedX = yL;//map(yR, -1210, 1210, 127, -127);
    commandedY = xL;//map(xR, -1210, 1210, -127, 127);
    commandedR = xR; //map(xR, -1210, 1210, 127, -127);

    //if commands more than deadband then process
    if ((abs(commandedX) > 15) || (abs(commandedY) > 15) || (abs(commandedR) > 15) || (tick > 0)) {
        compute_strides();
        numTicks = round(duration / FRAME_TIME_MS / 3.0); //total ticks divided into the three cases
        for (leg_num = 0; leg_num < 6; leg_num++) {
            compute_amplitudes();
            switch (tetrapod_case[leg_num]) {
                case 1:                               //move foot forward (raise and lower)
                    current_X[leg_num] = HOME_X[leg_num] - amplitudeX * cos(M_PI * tick / numTicks);
                    current_Y[leg_num] = HOME_Y[leg_num] - amplitudeY * cos(M_PI * tick / numTicks);
                    current_Z[leg_num] = HOME_Z[leg_num] + abs(amplitudeZ) * sin(M_PI * tick / numTicks);
                    if (tick >= numTicks - 1) tetrapod_case[leg_num] = 2;
                    break;
                case 2:                               //move foot back one-half (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) tetrapod_case[leg_num] = 3;
                    break;
                case 3:                               //move foot back one-half (on the ground)
                    current_X[leg_num] = current_X[leg_num] - amplitudeX / numTicks;
                    current_Y[leg_num] = current_Y[leg_num] - amplitudeY / numTicks;
                    current_Z[leg_num] = HOME_Z[leg_num];
                    if (tick >= numTicks - 1) tetrapod_case[leg_num] = 1;
                    break;
            }
        }

        //increment tick
        if (tick < numTicks - 1) tick++;
        else tick = 0;
    }
}


//***********************************************************************
// Compute walking stride lengths
//***********************************************************************
void compute_strides() {
    //compute stride lengths
    strideX = (float) 90 * (float) commandedX / 1210;
    strideY = (float) 90 * (float) commandedY / 1210;
    strideR = (float) 35 * (float) commandedR / 1210;

    //compute rotation trig
    sinRotZ = sin(radians(strideR));
    cosRotZ = cos(radians(strideR));

    //set duration for normal and slow speed modes
    if (gait_speed == 0) duration = 1080;
    else duration = 3240;
}


//***********************************************************************
// Compute walking amplitudes
//***********************************************************************
void compute_amplitudes() {
    //compute total distance from center of body to toe
    totalX = HOME_X[leg_num] + BODY_X[leg_num];
    totalY = HOME_Y[leg_num] + BODY_Y[leg_num];

    //compute rotational offset
    rotOffsetX = totalY * sinRotZ + totalX * cosRotZ - totalX;
    rotOffsetY = totalY * cosRotZ - totalX * sinRotZ - totalY;

    //compute X and Y amplitude and constrain to prevent legs from crashing into each other
    amplitudeX = ((strideX + rotOffsetX) / 2.0);
    amplitudeY = ((strideY + rotOffsetY) / 2.0);

    amplitudeX = constrain(amplitudeX, -50, 50);
    amplitudeY = constrain(amplitudeY, -50, 50);

    //compute Z amplitude
    if (abs(strideX + rotOffsetX) > abs(strideY + rotOffsetY))
        amplitudeZ = step_height_multiplier * (strideX + rotOffsetX) / 4.0;
    else
        amplitudeZ = step_height_multiplier * (strideY + rotOffsetY) / 4.0;
}


//***********************************************************************
// Body translate with controller (xyz axes)
//***********************************************************************
void translate_control(int xR, int yR, int zR, int xL, int yL, int zL) {
    //compute X direction move
    translateX = map(yR, -1210, 1210, -2 * TRAVEL, 2 * TRAVEL);
    for (leg_num = 0; leg_num < 6; leg_num++)
        current_X[leg_num] = HOME_X[leg_num] + translateX;

    //compute Y direction move
    translateY = map(xR, -1210, 1210, 2 * TRAVEL, -2 * TRAVEL);

    for (leg_num = 0; leg_num < 6; leg_num++)
        current_Y[leg_num] = HOME_Y[leg_num] + translateY;

    //compute Z direction move
    translateZ = yR;
    if (translateZ > 127)
        translateZ = map(translateZ, 128, 255, 0, TRAVEL);
    else
        translateZ = map(translateZ, 0, 127, -3 * TRAVEL, 0);
    for (leg_num = 0; leg_num < 6; leg_num++)
        current_Z[leg_num] = HOME_Z[leg_num] + translateZ;

    //lock in offsets if commanded
    if (capture_offsets == true) {
        for (leg_num = 0; leg_num < 6; leg_num++) {
            offset_X[leg_num] = offset_X[leg_num] + translateX;
            offset_Y[leg_num] = offset_Y[leg_num] + translateY;
            offset_Z[leg_num] = offset_Z[leg_num] + translateZ;
            current_X[leg_num] = HOME_X[leg_num];
            current_Y[leg_num] = HOME_Y[leg_num];
            current_Z[leg_num] = HOME_Z[leg_num];
        }
    }

    //if offsets were commanded, exit current mode
    if (capture_offsets == true) {
        capture_offsets = false;
        mode = 0;
    }
}


//***********************************************************************
// Body rotate with controller (xyz axes)
//***********************************************************************
void rotate_control(int xR, int yR, int zR, int xL, int yL, int zL) {
    //compute rotation sin/cos values using controller inputs
    sinRotX = sin((map(xR, 1210, -1210, A12DEG, -A12DEG)) / 1000000.0);
    cosRotX = cos((map(xR, 1210, -1210, A12DEG, -A12DEG)) / 1000000.0);
    sinRotY = sin((map(yR, 1210, -1210, A12DEG, -A12DEG)) / 1000000.0);
    cosRotY = cos((map(yR, 1210, -1210, A12DEG, -A12DEG)) / 1000000.0);
    sinRotZ = sin((map(xL, 1210, -1210, -A30DEG, A30DEG)) / 1000000.0);
    cosRotZ = cos((map(xL, 1210, -1210, -A30DEG, A30DEG)) / 1000000.0);

    //compute Z direction move
    translateZ = yL;
    if (translateZ > 0)
        translateZ = map(translateZ, 0, 1210, 0, TRAVEL);
    else
        translateZ = map(translateZ, -1210, 1, -3 * TRAVEL, 0);

    for (int leg_num = 0; leg_num < 6; leg_num++) {
        //compute total distance from center of body to toe
        totalX = HOME_X[leg_num] + BODY_X[leg_num];
        totalY = HOME_Y[leg_num] + BODY_Y[leg_num];
        totalZ = HOME_Z[leg_num] + BODY_Z[leg_num];

        //perform 3 axis rotations
        rotOffsetX =
                totalX * cosRotY * cosRotZ + totalY * sinRotX * sinRotY * cosRotZ + totalY * cosRotX * sinRotZ -
                totalZ * cosRotX * sinRotY * cosRotZ + totalZ * sinRotX * sinRotZ - totalX;
        rotOffsetY =
                -totalX * cosRotY * sinRotZ - totalY * sinRotX * sinRotY * sinRotZ + totalY * cosRotX * cosRotZ +
                totalZ * cosRotX * sinRotY * sinRotZ + totalZ * sinRotX * cosRotZ - totalY;
        rotOffsetZ = totalX * sinRotY - totalY * sinRotX * cosRotY + totalZ * cosRotX * cosRotY - totalZ;

        // Calculate foot positions to achieve desired rotation
        current_X[leg_num] = HOME_X[leg_num] + rotOffsetX;
        current_Y[leg_num] = HOME_Y[leg_num] + rotOffsetY;
        current_Z[leg_num] = HOME_Z[leg_num] + rotOffsetZ + translateZ;

        //lock in offsets if commanded
        if (capture_offsets == true) {
            offset_X[leg_num] = offset_X[leg_num] + rotOffsetX;
            offset_Y[leg_num] = offset_Y[leg_num] + rotOffsetY;
            offset_Z[leg_num] = offset_Z[leg_num] + rotOffsetZ + translateZ;
            current_X[leg_num] = HOME_X[leg_num];
            current_Y[leg_num] = HOME_Y[leg_num];
            current_Z[leg_num] = HOME_Z[leg_num];
        }
    }

    //if offsets were commanded, exit current mode
    if (capture_offsets == true) {
        capture_offsets = false;
        mode = 0;
    }
}


//***********************************************************************
// One leg lift mode
// also can set z step height using capture offsets
//***********************************************************************
void one_leg_lift(int xR, int yR, int zR, int xL, int yL, int zL) {
    //read current leg servo 1 positions the first time
    if (leg1_IK_control == true) {
        leg1_coxa = coxa1_servo.read();
        leg1_femur = femur1_servo.read();
        leg1_tibia = tibia1_servo.read();
        leg1_IK_control = false;
    }

    //read current leg servo 6 positions the first time
    if (leg6_IK_control == true) {
        leg6_coxa = coxa6_servo.read();
        leg6_femur = femur6_servo.read();
        leg6_tibia = tibia6_servo.read();
        leg6_IK_control = false;
    }

    //process right joystick left/right axis
    temp = xR;
    temp = map(temp, 0, 255, 45, -45);
    coxa1_servo.write(constrain(int(leg1_coxa+temp), 45, 135));

    //process right joystick up/down axis
    temp = yR;
    if (temp < 117)                                //if joystick moved up
    {
        temp = map(temp, 116, 0, 0, 24);                //move leg 1
        femur1_servo.write(constrain(int(leg1_femur+temp), 0, 170));
        tibia1_servo.write(constrain(int(leg1_tibia+4 * temp), 0, 170));
    } else                                          //if joystick moved down
    {
        z_height_right = constrain(temp, 140, 255);   //set Z step height
        z_height_right = map(z_height_right, 140, 255, 1, 8);
    }

    //process left joystick left/right axis
    temp = xL;
    temp = map(temp, 0, 255, 45, -45);
    coxa6_servo.write(constrain(int(leg6_coxa+temp), 45, 135));

    //process left joystick up/down axis
    temp = yL;
    if (temp < 117)                                //if joystick moved up
    {
        temp = map(temp, 116, 0, 0, 24);                //move leg 6
        femur6_servo.write(constrain(int(leg6_femur+temp), 0, 170));
        tibia6_servo.write(constrain(int(leg6_tibia+4 * temp), 0, 170));
    } else                                          //if joystick moved down
    {
        z_height_left = constrain(temp, 140, 255);    //set Z step height
        z_height_left = map(z_height_left, 140, 255, 1, 8);
    }

    //process z height adjustment
    if (z_height_left > z_height_right)
        z_height_right = z_height_left;             //use max left or right value

    else z_height_LED_color = 1;                    //use green LEDs if battery weak

    if (capture_offsets == true)                   //lock in Z height if commanded
    {
        step_height_multiplier = 1.0 + ((z_height_right - 1.0) / 3.0);
        capture_offsets = false;
    }
}


//***********************************************************************
// Set all servos to 90 degrees
// Note: this is useful for calibration/alignment of the servos
// i.e: set COXA_CAL[6], FEMUR_CAL[6], and TIBIA_CAL[6] values in  
//      constants section above so all angles appear as 90 degrees
//***********************************************************************
void set_all_90() {
    coxa1_servo.write(90 + COXA_CAL[0]);
    femur1_servo.write(90 + FEMUR_CAL[0]);
    tibia1_servo.write(90 + TIBIA_CAL[0]);

    coxa2_servo.write(90 + COXA_CAL[1]);
    femur2_servo.write(90 + FEMUR_CAL[1]);
    tibia2_servo.write(90 + TIBIA_CAL[1]);

    coxa3_servo.write(90 + COXA_CAL[2]);
    femur3_servo.write(90 + FEMUR_CAL[2]);
    tibia3_servo.write(90 + TIBIA_CAL[2]);

    coxa4_servo.write(90 + COXA_CAL[3]);
    femur4_servo.write(90 + FEMUR_CAL[3]);
    tibia4_servo.write(90 + TIBIA_CAL[3]);

    coxa5_servo.write(90 + COXA_CAL[4]);
    femur5_servo.write(90 + FEMUR_CAL[4]);
    tibia5_servo.write(90 + TIBIA_CAL[4]);

    coxa6_servo.write(90 + COXA_CAL[5]);
    femur6_servo.write(90 + FEMUR_CAL[5]);
    tibia6_servo.write(90 + TIBIA_CAL[5]);
}

void sendBatteryLevel() {
    int level;
    float voltage = analogRead(A0) * 5.0 / 1024 * 2;

    if (voltage > 8.3)
        level = 100;
    else if (voltage > 8.22)
        level = 95;
    else if (voltage > 8.16)
        level = 90;
    else if (voltage > 8.05)
        level = 85;
    else if (voltage > 7.97)
        level = 80;
    else if (voltage > 7.91)
        level = 75;
    else if (voltage > 7.83)
        level = 70;
    else if (voltage > 7.75)
        level = 65;
    else if (voltage > 7.71)
        level = 60;
    else if (voltage > 7.67)
        level = 55;
    else if (voltage > 7.63)
        level = 50;
    else if (voltage > 7.59)
        level = 45;
    else if (voltage > 7.57)
        level = 40;
    else if (voltage > 7.53)
        level = 35;
    else if (voltage > 7.49)
        level = 30;
    else if (voltage > 7.45)
        level = 25;
    else if (voltage > 7.41)
        level = 20; // state critical
    else if (voltage > 7.37)
        level = 15;
    else if (voltage > 7.22)
        level = 10;
    else if (voltage > 6.55)
        level = 5;
    else level = 0;


    char str[16];
    sprintf(str, "[20,%d]", level);
    Serial.println(str);
}
