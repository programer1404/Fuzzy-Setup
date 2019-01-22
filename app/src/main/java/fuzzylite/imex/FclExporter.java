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
package fuzzylite.imex;

import fuzzylite.Engine;
import fuzzylite.FuzzyLite;
import fuzzylite.Op;
import fuzzylite.defuzzifier.*;
import fuzzylite.norm.Norm;
import fuzzylite.norm.s.*;
import fuzzylite.norm.t.*;
import fuzzylite.rule.Rule;
import fuzzylite.rule.RuleBlock;
import fuzzylite.term.Constant;
import fuzzylite.term.Discrete;
import fuzzylite.term.Term;
import fuzzylite.variable.InputVariable;
import fuzzylite.variable.OutputVariable;

import java.util.Iterator;

/**
 The FclExporter class is an Exporter that translates an Engine and its
 components to the Fuzzy Control Language specification.

 @author Juan Rada-Vilela, Ph.D.
 @see FclImporter
 @see Exporter
 @since 4.0
 */
public class FclExporter extends Exporter {

    private String indent;

    public FclExporter() {
        this("  ");
    }

    public FclExporter(String indent) {
        this.indent = indent;
    }

    /**
     Gets the indentation string within blocks

     @return the indentation string within blocks
     */
    public String getIndent() {
        return indent;
    }

    /**
     Sets the indentation string within blocks

     @param indent is the indentation string within blocks
     */
    public void setIndent(String indent) {
        this.indent = indent;
    }

    @Override
    public String toString(Engine engine) {
        StringBuilder result = new StringBuilder();
        result.append("//Code automatically generated with " + FuzzyLite.LIBRARY + ".\n\n");
        result.append(String.format(
                "FUNCTION_BLOCK %s\n", engine.getName()));

        result.append("\n");

        result.append("VAR_INPUT\n");
        for (InputVariable inputVariable : engine.getInputVariables()) {
            result.append(String.format(indent + "%s: REAL;\n", inputVariable.getName()));
        }
        result.append("END_VAR\n");

        result.append("\n");

        result.append("VAR_OUTPUT\n");
        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            result.append(String.format(indent + "%s: REAL;\n", outputVariable.getName()));
        }
        result.append("END_VAR\n");

        result.append("\n");

        for (InputVariable inputVariable : engine.getInputVariables()) {
            result.append(toString(inputVariable)).append("\n");
        }

        for (OutputVariable outputVariable : engine.getOutputVariables()) {
            result.append(toString(outputVariable)).append("\n");
        }

        for (RuleBlock ruleBlock : engine.getRuleBlocks()) {
            result.append(toString(ruleBlock)).append("\n");
        }

        result.append("END_FUNCTION_BLOCK\n");

        return result.toString();
    }

    /**
     Returns a string representation of the InputVariable according to the Fuzzy
     Control Language specification

     @param inputVariable is the input variable
     @return a string representation of the input variable according to the
     Fuzzy Control Language specification
     */
    public String toString(InputVariable inputVariable) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("FUZZIFY %s\n", inputVariable.getName()));
        result.append(String.format(indent + "RANGE := (%s .. %s);\n",
                Op.str(inputVariable.getMinimum()), Op.str(inputVariable.getMaximum())));

        for (Term term : inputVariable.getTerms()) {
            result.append(String.format(indent + "TERM %s := %s;\n",
                    term.getName(), toString(term)));
        }
        result.append("END_FUZZIFY\n");
        return result.toString();
    }

    /**
     Returns a string representation of the OutputVariable according to the
     Fuzzy Control Language specification

     @param outputVariable is the output variable
     @return a string representation of the output variable according to the
     Fuzzy Control Language specification
     */
    public String toString(OutputVariable outputVariable) {
        StringBuilder result = new StringBuilder();

        result.append(String.format("DEFUZZIFY %s\n", outputVariable.getName()));
        result.append(String.format(indent + "RANGE := (%s .. %s);\n",
                Op.str(outputVariable.getMinimum()), Op.str(outputVariable.getMaximum())));
        for (Term term : outputVariable.getTerms()) {
            result.append(String.format(indent + "TERM %s := %s;\n", term.getName(), toString(term)));
        }
        if (outputVariable.getDefuzzifier() != null) {
            result.append(String.format(indent + "METHOD : %s;\n",
                    toString(outputVariable.getDefuzzifier())));
        }
        if (outputVariable.fuzzyOutput().getAggregation() != null) {
            result.append(String.format(indent + "ACCU : %s;\n",
                    toString(outputVariable.fuzzyOutput().getAggregation())));
        }
        result.append(String.format(indent + "DEFAULT := %s",
                Op.str(outputVariable.getDefaultValue())));
        if (outputVariable.isLockPreviousValue()) {
            result.append(" | NC");
        }
        result.append(";\n");
        result.append("END_DEFUZZIFY\n");
        return result.toString();
    }

    /**
     Returns a string representation of the RuleBlock according to the Fuzzy
     Control Language specification

     @param ruleBlock is the rule block
     @return a string representation of the rule block according to the Fuzzy
     Control Language specification
     */
    public String toString(RuleBlock ruleBlock) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("RULEBLOCK %s\n", ruleBlock.getName()));
        if (ruleBlock.getConjunction() != null) {
            result.append(String.format(indent + "AND : %s;\n", toString(ruleBlock.getConjunction())));
        }
        if (ruleBlock.getDisjunction() != null) {
            result.append(String.format(indent + "OR : %s;\n", toString(ruleBlock.getDisjunction())));
        }
        if (ruleBlock.getImplication() != null) {
            result.append(String.format(indent + "ACT : %s;\n", toString(ruleBlock.getImplication())));
        }

        int index = 1;
        for (Rule rule : ruleBlock.getRules()) {
            result.append(String.format(indent + "RULE %d : %s\n", index++, rule.getText()));
        }
        result.append("END_RULEBLOCK\n");
        return result.toString();
    }

    /**
     Returns a string representation of the Norm according to the Fuzzy Control
     Language specification

     @param norm is the norm
     @return a string representation of the norm according to the Fuzzy Control
     Language specification
     */
    public String toString(Norm norm) {
        if (norm == null) {
            return "NONE";
        }
        //T-Norms
        if (norm instanceof Minimum) {
            return "MIN";
        }
        if (norm instanceof AlgebraicProduct) {
            return "PROD";
        }
        if (norm instanceof BoundedDifference) {
            return "BDIF";
        }
        if (norm instanceof DrasticProduct) {
            return "DPROD";
        }
        if (norm instanceof EinsteinProduct) {
            return "EPROD";
        }
        if (norm instanceof HamacherProduct) {
            return "HPROD";
        }
        if (norm instanceof NilpotentMinimum) {
            return "NMIN";
        }

        //S-Norms
        if (norm instanceof Maximum) {
            return "MAX";
        }
        if (norm instanceof AlgebraicSum) {
            return "ASUM";
        }
        if (norm instanceof NormalizedSum) {
            return "NSUM";
        }
        if (norm instanceof BoundedSum) {
            return "BSUM";
        }
        if (norm instanceof DrasticSum) {
            return "DSUM";
        }
        if (norm instanceof EinsteinSum) {
            return "ESUM";
        }
        if (norm instanceof HamacherSum) {
            return "HSUM";
        }
        if (norm instanceof NilpotentMaximum) {
            return "NMAX";
        }
        return norm.getClass().getSimpleName();
    }

    /**
     Returns a string representation of the Defuzzifier according to the Fuzzy
     Control Language specification

     @param defuzzifier is the defuzzifier
     @return a string representation of the defuzzifier according to the Fuzzy
     Control Language specification
     */
    public String toString(Defuzzifier defuzzifier) {
        if (defuzzifier == null) {
            return "NONE";
        }
        if (defuzzifier instanceof Centroid) {
            return "COG";
        }
        if (defuzzifier instanceof Bisector) {
            return "COA";
        }
        if (defuzzifier instanceof SmallestOfMaximum) {
            return "LM";
        }
        if (defuzzifier instanceof LargestOfMaximum) {
            return "RM";
        }
        if (defuzzifier instanceof MeanOfMaximum) {
            return "MM";
        }
        if (defuzzifier instanceof WeightedAverage) {
            return "COGS";
        }
        if (defuzzifier instanceof WeightedSum) {
            return "COGSS";
        }
        return defuzzifier.getClass().getSimpleName();
    }

    /**
     Returns a string representation of the Term according to the Fuzzy Control
     Language specification

     @param term is the term
     @return a string representation of the term according to the Fuzzy Control
     Language specification
     */
    public String toString(Term term) {
        if (term == null) {
            return "";
        }

        if (term instanceof Discrete) {
            StringBuilder result = new StringBuilder();
            Discrete discrete = (Discrete) term;
            Iterator<Discrete.Pair> it = discrete.iterator();
            while (it.hasNext()) {
                Discrete.Pair xy = it.next();
                result.append(String.format("(%s, %s)",
                        Op.str(xy.getX()), Op.str(xy.getY())));
                if (it.hasNext()) {
                    result.append(" ");
                }
            }
            return result.toString();
        }

        if (term instanceof Constant) {
            Constant constant = (Constant) term;
            return Op.str(constant.getValue());
        }

        return term.getClass().getSimpleName() + " " + term.parameters();
    }

    @Override
    public FclExporter clone() throws CloneNotSupportedException {
        return (FclExporter) super.clone();
    }

}
