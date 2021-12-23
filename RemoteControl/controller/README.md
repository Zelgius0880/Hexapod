 
# Sending command
## Data format

| Input name     | Code number |
|----------------|-------------|
| CROSS_LEFT     | 0           |
| CROSS_RIGHT    | 1           |
| CROSS_UP       | 2           |
| CROSS_DOWN     | 3           |
| BUTTON_A       | 4           |
| BUTTON_B       | 5           |
| BUTTON_X       | 11          |
| BUTTON_Y       | 12          |
| BUTTON_MINUS   | 7           |
| BUTTON_PLUS    | 6           |
| BUTTON_HOME    | 8           |
| BUTTON_L       | 13          |
| BUTTON_R       | 14          |
| BUTTON_ZL      | 15          |
| BUTTON_ZR      | 16          |
| BUTTON_STICK_L | 17          |
| BUTTON_STICK_R | 18          |
| STICK          | 19          |

### Buttons

| Code number | Battery Level<sup>1</sup> | is Pressed |
|-------------|---------------------------|------------|
| uint8       | uint16                    | uint16     |

<sup>1</sup> Battery level send by XWiimote seems to be always invalid.
### Sticks

| Code number | Battery Level<sup>1</sup> | xL     | yL     | zL  <sup>2</sup> | xR     | yR     | zR  <sup>2</sup> |
|-------------|---------------------------|--------|--------|------------------|--------|--------|------------------|
| uint8       | uint16                    | uint16 | uint16 | uint16           | uint16 | uint16 | uint16           |

<sup>2</sup> z value should always be 0 because the Wii U Pro Controller does not have gyroscope.

**I need to confirm, but on r stick, x and y seems to be inverted ¯\\_(ツ)_/¯**