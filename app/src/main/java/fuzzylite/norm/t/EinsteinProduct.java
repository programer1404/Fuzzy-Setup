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

import fuzzylite.norm.TNorm;

/**
 The EinsteinProduct class is a TNorm that computes the Einstein product of any
 two values.

 @author Juan Rada-Vilela, Ph.D.
 @see EinsteinSum
 @see TNorm
 @see TNormFactory
 @see Norm
 @since 4.0
 */
public final class EinsteinProduct extends TNorm {

    /**
     Computes the Einstein product of two membership function values

     @param a is a membership function value
     @param b is a membership function value
     @return `(a\times b)/(2-(a+b-a\times b))`
     */
    @Override
    public double compute(double a, double b) {
        return (a * b) / (2 - (a + b - a * b));
    }

    @Override
    public EinsteinProduct clone() throws CloneNotSupportedException {
        return (EinsteinProduct) super.clone();
    }
}
