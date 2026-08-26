package com.fullmetalsonic.brightnessoffset.shizuku;

interface IPrivilegedBrightnessService {
    float readAdjustment() = 1;
    boolean writeAdjustment(float value) = 2;
    boolean applyTemporaryAdjustment(float value) = 3;
    boolean clearTemporaryAdjustment() = 4;
    boolean setTemporaryBrightness(int displayId, float value) = 5;
    boolean clearTemporaryBrightness(int displayId) = 6;
    float readAutomaticBrightnessTarget(float ambientLux) = 7;
    float[] readAutomaticBrightnessState(float ambientLux) = 8;
    void destroy() = 16777114;
}
