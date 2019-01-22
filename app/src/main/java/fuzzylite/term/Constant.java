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
package fuzzylite.term;

import fuzzylite.Op;

/**
 The Constant class is a (zero) polynomial Term that represents a constant value

 ` f(x) = k `

 @author Juan Rada-Vilela, Ph.D.
 @see Term
 @see fuzzylite.variable.Variable
 @since 4.0
 */
public class Constant extends Term {

    private double value;

    public Constant() {
        this("");
    }

    public Constant(String name) {
        this(name, Double.NaN);
    }

    public Constant(String name, double value) {
        this.name = name;
        this.value = value;
    }

    /**
     Returns the parameters of the term

     @return `"value"`
     */
    @Override
    public String parameters() {
        return Op.str(value);
    }

    /**
     Configures the term with the parameters

     @param parameters as `"value"`
     */
    @Override
    public void configure(String parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        setValue(Op.toDouble(parameters));
    }

    /**
     Computes the membership function evaluated at `x`

     @param x is irrelevant
     @return `c`, where `c` is the constant value
     */
    @Override
    public double membership(double x) {
        return this.value;
    }

    /**
     Gets the constant value

     @return the constant value
     */
    public double getValue() {
        return value;
    }

    /**
     Sets the constant value

     @param value is the constant value
     */
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public Constant clone() throws CloneNotSupportedException {
        return (Constant) super.clone();
    }

}
