package com.github.dakusui.floorplan.exception;

abstract class FloorPlanException extends RuntimeException {
  FloorPlanException(String message) {
    super(message);
  }
}
