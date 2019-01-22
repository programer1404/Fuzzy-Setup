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
package fuzzylite.activation;

import fuzzylite.FuzzyLite;
import fuzzylite.Op;
import fuzzylite.norm.SNorm;
import fuzzylite.norm.TNorm;
import fuzzylite.rule.Rule;
import fuzzylite.rule.RuleBlock;

import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

/**
 The Last class is a RuleBlock Activation method that activates the last

 `n` rules whose activation degrees are greater than or equal to the given
 threshold. The rules are iterated in the reverse order in which they were added
 to the rule block.

 @author Juan Rada-Vilela, Ph.D.
 @see First
 @see Rule
 @see RuleBlock
 @see fuzzylite.factory.ActivationFactory
 @since 6.0
 */
public class Last extends Activation {

    private int numberOfRules;
    private double threshold;

    public Last() {
        this(1);
    }

    public Last(int numberOfRules) {
        this(numberOfRules, 0.0);
    }

    public Last(int numberOfRules, double threshold) {
        this.numberOfRules = numberOfRules;
        this.threshold = threshold;
    }

    /**
     Returns the number of rules and the threshold of the activation method

     @return "numberOfRules threshold"
     */
    @Override
    public String parameters() {
        return Op.str(getNumberOfRules()) + " " + Op.str(getThreshold());
    }

    /**
     Configures the activation method with the given number of rules and
     threshold

     @param parameters as "numberOfRules threshold"
     */
    @Override
    public void configure(String parameters) {
        if (parameters.isEmpty()) {
            return;
        }
        List<String> values = Op.split(parameters, " ", true);
        final int required = 2;
        if (values.size() < required) {
            throw new RuntimeException(MessageFormat.format(
                    "[configuration error] activation {0} requires {1} parameters",
                    this.getClass().getSimpleName(), required));
        }

        setNumberOfRules(Integer.parseInt(values.get(0)));
        setThreshold(Op.toDouble(values.get(1)));
    }

    /**
     Activates the last `n` rules whose activation degrees are greater than
     the given threshold. The rules are iterated in the reverse order that the
     rules were added to the rule block.

     @param ruleBlock is the rule block to activate
     */
    @Override
    public void activate(RuleBlock ruleBlock) {
        if (FuzzyLite.isDebugging()) {
            FuzzyLite.logger().log(Level.FINE, "Activation: {0} {1}",
                    new String[]{getClass().getName(), parameters()});
        }
        TNorm conjunction = ruleBlock.getConjunction();
        SNorm disjunction = ruleBlock.getDisjunction();
        TNorm implication = ruleBlock.getImplication();

        int activated = 0;
        int lastIndex = ruleBlock.getRules().size();
        ListIterator<Rule> rit = ruleBlock.getRules().listIterator(lastIndex);
        while (rit.hasPrevious()) {
            Rule rule = rit.previous();
            rule.deactivate();

            if (rule.isLoaded()) {
                double activationDegree = rule.activateWith(conjunction, disjunction);
                if (activated < numberOfRules
                        && Op.isGt(activationDegree, 0.0)
                        && Op.isGE(activationDegree, threshold)) {
                    rule.trigger(implication);
                    ++activated;
                }
            }
        }
    }

    /**
     Gets the number of rules for the activation degree

     @return the number of rules for the activation degree
     */
    public int getNumberOfRules() {
        return numberOfRules;
    }

    /**
     Sets the number of rules for the activation degree

     @param numberOfRules is the number of rules for the activation degree
     */
    public void setNumberOfRules(int numberOfRules) {
        this.numberOfRules = numberOfRules;
    }

    /**
     Gets the threshold for the activation degree

     @return the threshold for the activation degree
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     Sets the threshold for the activation degree

     @param threshold is the threshold for the activation degree
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public Last clone() throws CloneNotSupportedException {
        return (Last) super.clone();
    }
}
