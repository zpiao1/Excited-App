package com.example.zpiao1.excited.logic;

public interface OnTouchActionListener {

  void onActionUp();

  void onActionDown();

  // -1.0f means not applicable
  void onActionMove(float startX, float currentX, float startY, float currentY);
}