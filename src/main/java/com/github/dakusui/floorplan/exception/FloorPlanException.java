package com.github.dakusui.floorplan.exception;

public abstract class FloorPlanException extends RuntimeException {
  FloorPlanException(String message) {
    super(message);
  }
}
