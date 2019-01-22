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
package fuzzylite.variable;

import fuzzylite.imex.FllExporter;

/**
 The InputVariable class is a Variable that represents an input of the fuzzy
 logic controller.

 @author Juan Rada-Vilela, Ph.D.
 @see Variable
 @see OutputVariable
 @see fuzzylite.term.Term
 @since 4.0
 */
public class InputVariable extends Variable {

    public InputVariable() {
        this("");
    }

    public InputVariable(String name) {
        this(name, Double.NaN, Double.NaN);
    }

    public InputVariable(String name, double minimum, double maximum) {
        super(name, minimum, maximum);
    }

    /**
     Evaluates the membership function of the current input value `x` for
     each term `i`, resulting in a fuzzy input value in the form

     `\tilde{x}=\sum_i{\mu_i(x)/i}`. This is equivalent to a call to
     Variable::fuzzify() passing `x` as input value

     @return the fuzzy input value expressed as `\sum_i{\mu_i(x)/i}`
     */
    public String fuzzyInputValue() {
        return fuzzify(this.getValue());
    }

    @Override
    public Type type() {
        return Type.Input;
    }

    @Override
    public String toString() {
        return new FllExporter().toString(this);
    }

    @Override
    public InputVariable clone() throws CloneNotSupportedException {
        return (InputVariable) super.clone();
    }

}
