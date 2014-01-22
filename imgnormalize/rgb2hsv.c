#include "rgb2hsv.h"

#include "math.h"

// Credits: http://www.shervinemami.info/colorConversion.html

// Create a HSV image from the RGB image using the full 8-bits, since OpenCV only allows Hues up to 180 instead of 255.
// ref: "http://cs.haifa.ac.il/hagit/courses/ist/Lectures/Demos/ColorApplet2/t_convert.html"
// Remember to free the generated HSV image.
hsv_point_t rgb2hsv(const rgb_point_t* rgb)
{
    float fR, fG, fB;
    float fH, fS, fV;
    int bH, bS, bV;
    hsv_point_t hsv;
    float fDelta;
    float fMin, fMax;
    int iMax;

    const float FLOAT_TO_BYTE = 255.0f;
    const float BYTE_TO_FLOAT = 1.0f / FLOAT_TO_BYTE;
    float ANGLE_TO_UNIT = 1.0f / (6.0f * fDelta);	// Make the Hues between 0.0 to 1.0 instead of 6.0

    // Convert from 8-bit integers to floats.
    fR = rgb->r * BYTE_TO_FLOAT;
    fG = rgb->g * BYTE_TO_FLOAT;
    fB = rgb->b * BYTE_TO_FLOAT;

    // Get the min and max, but use integer comparisons for slight speedup.
    if (rgb->b < rgb->g) {
        if (rgb->b < rgb->r) {
            fMin = fB;
            if (rgb->r > rgb->g) {
                iMax = rgb->r;
                fMax = fR;
            }
            else {
                iMax = rgb->g;
                fMax = fG;
            }
        }
        else {
            fMin = fR;
            fMax = fG;
            iMax = rgb->g;
        }
    }
    else {
        if (rgb->g < rgb->r) {
            fMin = fG;
            if (rgb->b > rgb->r) {
                fMax = fB;
                iMax = rgb->b;
            }
            else {
                fMax = fR;
                iMax = rgb->r;
            }
        }
        else {
            fMin = fR;
            fMax = fB;
            iMax = rgb->b;
        }
    }
    fDelta = fMax - fMin;
    fV = fMax;                      // Value (Brightness).
    if (iMax != 0) {                // Make sure it's not pure black.
        fS = fDelta / fMax;         //Saturation.
        if (iMax == rgb->r) {       // between yellow and magenta.
            fH = (fG - fB) * ANGLE_TO_UNIT;
        }
        else if (iMax == rgb->g) {  // between cyan and yellow.
            fH = (2.0f/6.0f) + ( fB - fR ) * ANGLE_TO_UNIT;
        }
        else {                      // between magenta and cyan.
            fH = (4.0f/6.0f) + ( fR - fG ) * ANGLE_TO_UNIT;
        }
        // Wrap outlier Hues around the circle.
        if (fH < 0.0f)
            fH += 1.0f;
        if (fH >= 1.0f)
            fH -= 1.0f;
    }
    else {
        // color is pure Black.
        fS = 0;
        fH = 0;                     // undefined hue
    }

    // Convert from floats to 8-bit integers.
    bH = (int)(0.5f + fH * 255.0f);
    bS = (int)(0.5f + fS * 255.0f);
    bV = (int)(0.5f + fV * 255.0f);

    // Clip the values to make sure it fits within the 8bits.
    if (bH > 255)
        bH = 255;
    if (bH < 0)
        bH = 0;
    if (bS > 255)
        bS = 255;
    if (bS < 0)
        bS = 0;
    if (bV > 255)
        bV = 255;
    if (bV < 0)
        bV = 0;
    hsv.h = bH;
    hsv.s = bS;
    hsv.v = bV;
    return hsv;
}


// Create an RGB image from the HSV image using the full 8-bits, since OpenCV only allows Hues up to 180 instead of 255.
// ref: "http://cs.haifa.ac.il/hagit/courses/ist/Lectures/Demos/ColorApplet2/t_convert.html"
// Remember to free the generated RGB image.
rgb_point_t hsv2rgb(const hsv_point_t* hsv)
{
    float fH, fS, fV;
    float fR, fG, fB;

    rgb_point_t rgb;

    int iI;
    float fI, fF, p, q, t;

    const float FLOAT_TO_BYTE = 255.0f;
    const float BYTE_TO_FLOAT = 1.0f / FLOAT_TO_BYTE;

    // Convert from 8-bit integers to floats
    fH = (float)hsv->h * BYTE_TO_FLOAT;
    fS = (float)hsv->s * BYTE_TO_FLOAT;
    fV = (float)hsv->v * BYTE_TO_FLOAT;

    // Convert from HSV to RGB, using float ranges 0.0 to 1.0
    if( hsv->s == 0 ) {
        fR = fG = fB = fV;      // achromatic (grey)
    }
    else {
        // If Hue == 1.0, then wrap it around the circle to 0.0
        if (fH >= 1.0f)
            fH = 0.0f;

        fH *= 6.0;              // sector 0 to 5
        fI = floor( fH );       // integer part of h (0,1,2,3,4,5 or 6)
        iI = (int) fH;
        fF = fH - fI;           // factorial part of h (0 to 1)

        p = fV * ( 1.0f - fS );
        q = fV * ( 1.0f - fS * fF );
        t = fV * ( 1.0f - fS * ( 1.0f - fF ) );

        switch( iI ) {
            case 0:
                fR = fV;
                fG = t;
                fB = p;
                break;
            case 1:
                fR = q;
                fG = fV;
                fB = p;
                break;
            case 2:
                fR = p;
                fG = fV;
                fB = t;
                break;
            case 3:
                fR = p;
                fG = q;
                fB = fV;
                break;
            case 4:
                fR = t;
                fG = p;
                fB = fV;
                break;
            default:            // case 5 (or 6):
                fR = fV;
                fG = p;
                fB = q;
                break;
        }
    }

    // Convert from floats to 8-bit integers
    int bR = (int)(fR * FLOAT_TO_BYTE);
    int bG = (int)(fG * FLOAT_TO_BYTE);
    int bB = (int)(fB * FLOAT_TO_BYTE);

    // Clip the values to make sure it fits within the 8bits.
    if (bR > 255)
        bR = 255;
    if (bR < 0)
        bR = 0;
    if (bG > 255)
        bG = 255;
    if (bG < 0)
        bG = 0;
    if (bB > 255)
        bB = 255;
    if (bB < 0)
        bB = 0;

    rgb.r = bR;
    rgb.g = bG;
    rgb.b = bB;

    return rgb;
}
