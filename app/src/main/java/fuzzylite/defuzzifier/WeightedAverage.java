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
package fuzzylite.defuzzifier;

import fuzzylite.term.Activated;
import fuzzylite.term.Aggregated;
import fuzzylite.term.Term;

/**
 The WeightedAverage class is a WeightedDefuzzifier that computes the weighted
 average of a fuzzy set represented in an Aggregated Term.

 @author Juan Rada-Vilela, Ph.D.
 @see WeightedAverageCustom
 @see WeightedSum
 @see WeightedSumCustom
 @see WeightedDefuzzifier
 @see Defuzzifier
 @since 4.0
 */
public class WeightedAverage extends WeightedDefuzzifier {

    public WeightedAverage() {
        super(Type.Automatic);
    }

    public WeightedAverage(Type type) {
        super(type);
    }

    public WeightedAverage(String type) {
        super(type);
    }

    /**
     Computes the weighted average of the given fuzzy set represented in an
     Aggregated term as `y = \dfrac{\sum_i w_iz_i}{\sum_i w_i} `, where

     `w_i` is the activation degree of term `i`, and

     `z_i = \mu_i(w_i) `.

     From version 6.0, the implication and aggregation operators are not
     utilized for defuzzification.

     @param term is the fuzzy set represented as an Aggregated Term
     @param minimum is the minimum value of the range (only used for Tsukamoto)
     @param maximum is the maximum value of the range (only used for Tsukamoto)
     @return the weighted average of the given fuzzy set
     */
    @Override
    public double defuzzify(Term term, double minimum, double maximum) {
        Aggregated fuzzyOutput = (Aggregated) term;
        if (fuzzyOutput.getTerms().isEmpty()) {
            return Double.NaN;
        }
        minimum = fuzzyOutput.getMinimum();
        maximum = fuzzyOutput.getMaximum();

        Type type = getType();
        if (type == Type.Automatic) {
            type = inferType(fuzzyOutput.getTerms().get(0));
        }

        double sum = 0.0;
        double weights = 0.0;
        if (type == Type.TakagiSugeno) {
            double w, z;
            for (Activated activated : fuzzyOutput.getTerms()) {
                w = activated.getDegree();
                z = activated.getTerm().membership(w);
                sum += w * z;
                weights += w;
            }
        } else {
            double w, z;
            for (Activated activated : fuzzyOutput.getTerms()) {
                w = activated.getDegree();
                z = activated.getTerm().tsukamoto(w, minimum, maximum);
                sum += w * z;
                weights += w;
            }
        }
        return sum / weights;
    }

    @Override
    public WeightedAverage clone() throws CloneNotSupportedException {
        return (WeightedAverage) super.clone();
    }

}
