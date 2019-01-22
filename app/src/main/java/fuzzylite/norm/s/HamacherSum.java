/*
 jfuzzylite (TM), a fuzzy logic control library in Java.
 Copyright (C) 2010-2017 FuzzyLite Limited. All rights reserved.
 Author: Juan Rada-Vilela, Ph.D. <jcrada@fuzzylite.com>

 This file is part of jfuzzylite.

 jfuzzylite is free software: you can redistribute it and/or modify it under
 the terms of the FuzzyLite License included with the software.

 You should have received a copy of the FuzzyLite License along with
 jfuzzylite. If not, see <http://www.fuzzylite.com/license/>.

 jfuzzylite is a trademark of FuzzyLite Limited.
 fuzzylite (R) is a registered trademark of FuzzyLite Limited.
 */
package fuzzylite.norm.s;

import fuzzylite.Op;
import fuzzylite.norm.SNorm;

/**
 The HamacherSum class is an SNorm that computes the Hamacher sum of any two
 values.

 @author Juan Rada-Vilela, Ph.D.
 @see fuzzylite.norm.t.HamacherProduct
 @see SNorm
 @see fuzzylite.factory.SNormFactory
 @see fuzzylite.norm.Norm
 @since 4.0
 */
public final class HamacherSum extends SNorm {

    /**
     Computes the Hamacher sum of two membership function values

     @param a is a membership function value
     @param b is a membership function value
     @return `a+b-(2\times a \times b)/(1-a\times b)`
     */
    @Override
    public double compute(double a, double b) {
        if (Op.isEq(a * b, 1.0)) return 1.0;
        return (a + b - 2 * a * b) / (1 - a * b);
    }

    @Override
    public HamacherSum clone() throws CloneNotSupportedException {
        return (HamacherSum) super.clone();
    }

}
