package com.example;

import com.google.auto.value.AutoAnnotation;

final class Annotations {
  @AutoAnnotation
  static MultibindingMapNoUnwrap.TableKey tableKey(int row, int col) {
    return new AutoAnnotation_Annotations_tableKey(row, col);
  }
}
