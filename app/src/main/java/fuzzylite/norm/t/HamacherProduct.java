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
package fuzzylite.norm.t;

import fuzzylite.Op;
import fuzzylite.norm.TNorm;

/**
 The HamacherProduct class is a TNorm that computes the Hamacher product of any
 two values.

 @author Juan Rada-Vilela, Ph.D.
 @see HamacherSum
 @see TNorm
 @see TNormFactory
 @see Norm
 @since 4.0
 */
public final class HamacherProduct extends TNorm {

    /**
     Computes the Hamacher product of two membership function values

     @param a is a membership function value
     @param b is a membership function value
     @return `(a \times b) / (a+b- a \times b)`
     */
    @Override
    public double compute(double a, double b) {
        if (Op.isEq(a + b, 0.0)) return 0.0;
        return (a * b) / (a + b - a * b);
    }

    @Override
    public HamacherProduct clone() throws CloneNotSupportedException {
        return (HamacherProduct) super.clone();
    }

}
