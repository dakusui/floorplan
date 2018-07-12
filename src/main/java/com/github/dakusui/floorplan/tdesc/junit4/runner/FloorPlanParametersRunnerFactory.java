package com.github.dakusui.floorplan.tdesc.junit4.runner;

import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.List;

public class FloorPlanParametersRunnerFactory extends BlockJUnit4ClassRunnerWithParametersFactory {
  @Override
  public Runner createRunnerForTestWithParameters(TestWithParameters test)
      throws InitializationError {
    return new BlockJUnit4ClassRunnerWithParameters(test) {
      protected void collectInitializationErrors(List<Throwable> errors) {
        // We do not want to perform validation here since it's already done by parent side
      }
    };

  }
}
