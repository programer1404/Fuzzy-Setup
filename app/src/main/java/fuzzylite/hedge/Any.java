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
package fuzzylite.hedge;

/**
 The Any class is a special Hedge that always returns `1.0`. Its position with
 respect to the other hedges is last in the ordered set (Not, Seldom, Somewhat,
 Very, Extremely, Any). The Antecedent of a Rule considers Any to be a
 syntactically special hedge because it is not followed by a Term (e.g., `if
 Variable is any then...`). Amongst hedges, only Any has virtual methods to be
 overriden due to its particular case.

 @author Juan Rada-Vilela, Ph.D.
 @see Hedge
 @see fuzzylite.factory.HedgeFactory
 @since 4.0
 */
public class Any extends Hedge {

    /**
     Computes the hedge for the given value

     @param x is irrelevant
     @return `1.0`
     */
    @Override
    public double hedge(double x) {
        return 1.0;
    }

    @Override
    public Any clone() throws CloneNotSupportedException {
        return (Any) super.clone();
    }

}
