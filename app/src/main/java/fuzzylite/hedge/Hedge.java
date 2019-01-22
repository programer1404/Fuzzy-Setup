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

import fuzzylite.Op;

import java.util.Locale;

/**
 The Hedge class is the abstract class for hedges. Hedges are utilized within
 the Antecedent and Consequent of a Rule in order to modify the membership
 function of a linguistic Term.

 @author Juan Rada-Vilela, Ph.D.
 @see fuzzylite.rule.Antecedent
 @see fuzzylite.rule.Consequent
 @see fuzzylite.rule.Rule
 @see fuzzylite.factory.HedgeFactory
 @since 4.0
 */
public abstract class Hedge implements Op.Cloneable {

    /**
     Computes the hedge for the membership function value `x`

     @param x is a membership function value
     @return the hedge of `x`
     */
    public abstract double hedge(double x);

    /**
     Returns the name of the hedge

     @return the name of the hedge
     */
    public String getName() {
        return getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     Creates a clone of the hedge

     @return a clone of the hedge.
     */
    @Override
    public Hedge clone() throws CloneNotSupportedException {
        return (Hedge) super.clone();
    }

}
