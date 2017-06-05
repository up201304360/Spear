package com.example.nachito.spear;

/**
 * Created by ines on 5/5/17.
 */
public interface JoystickMovedListener {
     void OnMoved(float pan, float tilt);
     void OnReleased();
     void Thrust(float tilt);
}
